/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sync;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import com.taobao.tddl.interact.rule.bean.DBType;

/**
 * <master name="feed_receive">
 * <master-column></master-column>
 *	<slaves>
 *		<slave name="feed_send" type="oracle">
 *			<data-source-name>REP_SLAVE_ORACLE</data-source-name>     <!-- mandatory -->
 *          <database-shard-column>xxx</database-shard-column>        <!-- optional -->
 *			<table-shard-column>RATER_UID</table-shard-column>        <!-- optional -->
 *          <columns>                                                 <!-- optional -->
 *             <column>id</column>
 *             <column>name</column>
 *          </columns>
 *		</slave>
 *	</slaves>
 * </master>
 * 
 * setter中保证databaseShardColumn、tableShardColumn、name、type、columns都为小写
 * 
 */
public class SlaveInfo {
	private String name; // 备库逻辑表名，Setter方法保证了小写
	private DBType dbType; // 备库数据库类型：oracle/mysql（SyncConstants.DATABASE_TYPE_MYSQL/DATABASE_TYPE_ORACLE）
	private String databaseShardColumn; // 备库分库小写列名，Setter方法保证了小写
	private String tableShardColumn; // 备库分表小写列名，Setter方法保证了小写
	private String[] columns; // 需要复制的列的小写列名，缺省为主库的全部列；Setter方法保证了小写
	private JdbcTemplate jdbcTemplate; // 备库TDataSource对应的JdbcTemplate，根据dataSourceName手工设入
	/**
	 * 备库TDataSource名称
	 * 2.4.1之后SlaveInfo的 DataSourceName属性必须设置
	 * 复制配置推送的方式下，不默认复制到分库时，用来指定主TDS中的dbIndex，以复制到对应的数据库中
	 */
	private String dataSourceName;
	private SlaveReplicater slaveReplicater; // 同步(复制)目标不是数据库的情况，设置该接口实现定制功能
	private String slaveReplicaterName; //slaveReplicater的父spring中的beanId
    /**
     * application may want do something special
     * thing like change column value,add column
     * and so on ,they could inject this interface
     * implementation to make their logic work
     * 
     * default,this attribute is null
     */
	private SlaveDataHandler slaveDataHandler=null;
	
	public String getIdentity() {
		return "_" + dataSourceName + "_" + name;
	}
	
	private volatile boolean allowSync = true;

	/**
	 * 以下属性，默认false
	 */
	private boolean isDisableUpdate; // 是否关闭update
	private boolean isDisableInsert; // 是否关闭insert
	private boolean isNoSyncVersion; // 是否不关心sync_version(主库或备库没有sync_version字段的时候设为true)
	/**
	 * true 更新时，若主库有记录而分库记录不存在，自动插入分库，返回成功 
	 * false 更新时，若主库有记录而分库记录不存在，抛出异常。日志会保留。默认false
	 */
	private boolean isAutoInsert; // 更新时，若主库有记录而分库记录不存在，是否自动插入分库。否则报错

	/**
	 * 默认true：本slave失败直接中断，不再进行后续其他slave的复制。保留日志库记录，留待补偿服务器重试（这时忽略isRetryOnFail）
	 * 如果设置为false，那么当前slave失败，后续其他slave会继续复制。是否要求保留日志，看isRetryOnFail设置
	 */
	private boolean isBreakOnFail = true; //当前slave目标同步失败，是否直接中断，不再进行后续其他slave的复制。

	/**
	 * 默认true：当前slave目标同步失败后，保留日志库记录，留待补偿服务器重试
	 * 如果设置为false：那么其他slave都成功或者都不要求保留日志，日志就直接删除了。这样本slave的这次更新将会永远丢失。
	 * 如果设置为false, 当其他slave失败且要求保留日志时，补偿服务器仍然会顺带重试本slave
	 */
	private boolean isRetryOnFail = true;//当前slave目标同步失败，是否通过补偿服务器重试（也即是否保留日志库记录）
	
	private Map<String/*columnName*/,Object/*更新到null时自动换成的默认值*/> defaultNullValues = Collections.EMPTY_MAP;
	private Map<String/*columnName*/, Long[]/*0:最小值，1：最大值*/> columRanges = Collections.EMPTY_MAP;

	/**
     * 插入或更新null到一个列时，按这个配置自动转为指定的默认值。数据类型只支持数值long和String,例如：
	 * sku_id:0,item_id:65,seller_id:63
	 * sku_id:0,item_id:65,name:'aaa'
	 */
	public void setDefaultValuesOnNull(String defaultValuesOnNull) {
		String[] cols = defaultValuesOnNull.split(",");
		Map<String, Object> colvalues = new HashMap<String, Object>(cols.length);
		for (int i = 0; i < cols.length; i++) {
			String col = cols[i];
			String[] nv = col.split("\\:");
			if (nv[1].startsWith("'") && nv[1].endsWith("'")) {
				colvalues.put(nv[0], nv[1].substring(1, nv[1].length() - 1));// 字符串
			} else {
				colvalues.put(nv[0], Long.parseLong(nv[1]));// 数字
			}
		}
		this.defaultNullValues = colvalues;
	}

	/**
     * 插入或更新数字到一个列时，按这个配置限制范围，超过范围自动转为边界值。数据类型只支持数值long和String,例如：
	 * sku_id:0_8,item_id:_65,seller_id:0_
	 */
	public void setColumRestrictRanges(String defaultValuesOnNull) {
		String[] cols = defaultValuesOnNull.split(",");
		Map<String, Long[]> colRanges = new HashMap<String, Long[]>(cols.length);
		for (int i = 0; i < cols.length; i++) {
			String col = cols[i];
			String[] nv = col.split("\\:");
			String[] range = nv[1].split("_");
			Long min = null, max = null;
			if (range.length == 1) {
				min = Long.parseLong(range[0].trim());
			} else {
				if (!"".equals(range[0].trim())) {
					min = Long.parseLong(range[0].trim());
				}
				if (!"".equals(range[1].trim())) {
					max = Long.parseLong(range[1].trim());
				}
			}
			colRanges.put(nv[0], new Long[] { min, max });
		}
		this.columRanges = colRanges;
	}
	
	public Map<String, Long[]> getColumRanges() {
		return columRanges;
	}

	public Map<String, Object> getDefaultNullValues() {
		return defaultNullValues;
	}
	
	/*public Long getRestrictValue(Number value){
		
	}*/
	
	public Object changeToDefaultOnNull(String colName, Object value) {
		if (defaultNullValues == null) {
			return value;
		}
		if (value != null) {
			return value;
		}
		return defaultNullValues.get(colName);
	}

	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Slave {").append("\n");
		buffer.append("name: ").append(name).append("\n");
		buffer.append("dbType: ").append(dbType).append("\n");
		buffer.append("dataSourceName: ").append(dataSourceName).append("\n");
		if (databaseShardColumn != null) {
			buffer.append("databaseShardColumn: ").append(databaseShardColumn).append("\n");
		}
		if (tableShardColumn != null) {
			buffer.append("tableShardColumn: ").append(tableShardColumn).append("\n");
		}
		if (columns != null) {
			buffer.append("columns: ").append(Arrays.asList(columns)).append("\n");
		}
		buffer.append("}").append("\n");

		return buffer.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.toLowerCase();
	}

	public DBType getDbType() {
		return dbType;
	}

	public String getType() {
		return dbType == null ? null : dbType.toString();
	}

	public void setType(String type) {
		if (type != null) {
			type = type.toUpperCase();
		}
		this.dbType = DBType.valueOf(type);
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public String getDatabaseShardColumn() {
		return databaseShardColumn;
	}

	public void setDatabaseShardColumn(String databaseShardColumn) {
		this.databaseShardColumn = databaseShardColumn.toLowerCase();
	}

	public String getTableShardColumn() {
		return tableShardColumn;
	}

	public void setTableShardColumn(String tableShardColumn) {
		this.tableShardColumn = tableShardColumn.toLowerCase();
	}

	public String[] getColumns() {
		return columns;
	}

	public void setColumns(String[] columns) {
		this.columns = columns;
		for (int i = 0; i < this.columns.length; i++) {
			this.columns[i] = this.columns[i].toLowerCase();
		}
	}

	public void setCommaSeparatedColumns(String commaSeparatedColumns) {
		this.columns = commaSeparatedColumns.split(",");
		for (int i = 0; i < this.columns.length; i++) {
			this.columns[i] = this.columns[i].toLowerCase();
		}
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate slaveJdbcTemplate) {
		this.jdbcTemplate = slaveJdbcTemplate;
	}

	public SlaveReplicater getSlaveReplicater() {
		return slaveReplicater;
	}

	public void setSlaveReplicater(SlaveReplicater slaveReplicater) {
		this.slaveReplicater = slaveReplicater;
	}

	public String getSlaveReplicaterName() {
		return slaveReplicaterName;
	}

	public void setSlaveReplicaterName(String slaveReplicaterName) {
		this.slaveReplicaterName = slaveReplicaterName;
	}

	public boolean isDisableUpdate() {
		return isDisableUpdate;
	}

	public void setDisableUpdate(boolean isDisableUpdate) {
		this.isDisableUpdate = isDisableUpdate;
	}

	public boolean isDisableInsert() {
		return isDisableInsert;
	}

	public void setDisableInsert(boolean isDisableInsert) {
		this.isDisableInsert = isDisableInsert;
	}

	/**
	 * TODO:NO sync version还需要能够最终决定主库是否需要sync_version.
	 * 
	 * @return
	 */
	public boolean isNoSyncVersion() {
		return isNoSyncVersion;
	}

	public void setNoSyncVersion(boolean isNoSyncVersion) {
		this.isNoSyncVersion = isNoSyncVersion;
	}

	public boolean isAutoInsert() {
		return isAutoInsert;
	}

	public void setAutoInsert(boolean isAutoInsert) {
		this.isAutoInsert = isAutoInsert;
	}

	public boolean isBreakOnFail() {
		return isBreakOnFail;
	}

	public void setBreakOnFail(boolean isBreakOnFail) {
		this.isBreakOnFail = isBreakOnFail;
	}

	public boolean isRetryOnFail() {
		return isRetryOnFail;
	}

	public void setRetryOnFail(boolean isRetryOnFail) {
		this.isRetryOnFail = isRetryOnFail;
	}

	public boolean isAllowSync() {
		return allowSync;
	}

	public void setAllowSync(boolean allowSync) {
		this.allowSync = allowSync;
	}

	public SlaveDataHandler getSlaveDataHandler() {
		return slaveDataHandler;
	}

	public void setSlaveDataHandler(SlaveDataHandler slaveDataHandler) {
		this.slaveDataHandler = slaveDataHandler;
	}

	public static void main(String[] args) {
		System.out.println(Arrays.toString("_8".split("_")));
		System.out.println(Arrays.toString("0_".split("_")));
		System.out.println(Arrays.toString("0_9".split("_")));
	}
}
