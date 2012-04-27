/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sync;

import java.sql.Timestamp;

import org.springframework.jdbc.core.JdbcTemplate;
import com.taobao.tddl.interact.rule.bean.SqlType;

/**
 * 一个主库组，可以对应多个备库组
 * 一个主库组对应一个tddl-rule，每个备库组对应各自的tddl-rule
 * <master name="feed_receive">
 *	<slaves>
 *		<slave name="feed_send" type="oracle">
 *			<data-source-name>REP_SLAVE_ORACLE</data-source-name>
 *			<table-shard-column>RATER_UID</table-shard-column>
 *		</slave>
 *	</slaves>
 * </master>
 * @author nianbing
 */
public class RowBasedReplicationContext {
	public RowBasedReplicationContext() {/* 加默认构造函数方便搜索有new的地方 */
	}

	private String syncLogDsKey;
	private JdbcTemplate syncLogJdbcTemplate; //日志库原始数据源
	private JdbcTemplate masterJdbcTemplate; //主库组的TDataSource对应的JdbcTemplate
	private SlaveInfo[] slaveInfos; //备库信息
	private SqlType sqlType;
	private String primaryKeyColumn; //主键列明,Setter方法保证了小写
	private Object primaryKeyValue; //主键值
	private String masterLogicTableName; //主库逻辑表名,Setter方法保证了小写
	private String masterDatabaseShardColumn; //主库组分库列名,Setter方法保证了小写
	private Object masterDatabaseShardValue; //主库组分库列值
	private String masterTableShardColumn; //主库组分表列名,Setter方法保证了小写
	private Object masterTableShardValue; //主库组分表列值
	private String syncLogId;
	private Timestamp createTime; //日志创建时间
	private Timestamp nextSyncTime; //日志下次同步时间

	private long afterMainDBSqlExecuteTime; //执行主库插入成功后的时间点
	private String sql; //当前的时间数据
	private long replicationStartTime; //真正的复制任务开始执行时的时间点。

	private String masterColumns;

	public JdbcTemplate getMasterJdbcTemplate() {
		return masterJdbcTemplate;
	}

	public void setMasterJdbcTemplate(JdbcTemplate masterJdbcTemplate) {
		this.masterJdbcTemplate = masterJdbcTemplate;
	}

	public SqlType getSqlType() {
		return sqlType;
	}

	public void setSqlType(SqlType sqlType) {
		this.sqlType = sqlType;
	}

	public String getPrimaryKeyColumn() {
		return primaryKeyColumn;
	}

	public void setPrimaryKeyColumn(String primaryKeyColumn) {
		this.primaryKeyColumn = primaryKeyColumn == null ? null : primaryKeyColumn.toLowerCase();
	}

	public Object getPrimaryKeyValue() {
		return primaryKeyValue;
	}

	public void setPrimaryKeyValue(Object primaryKeyValue) {
		this.primaryKeyValue = primaryKeyValue;
	}

	public String getMasterLogicTableName() {
		return masterLogicTableName;
	}

	public void setMasterLogicTableName(String masterLogicTableName) {
		this.masterLogicTableName = masterLogicTableName == null ? null : masterLogicTableName.toLowerCase();
	}

	public String getMasterDatabaseShardColumn() {
		return masterDatabaseShardColumn;
	}

	public void setMasterDatabaseShardColumn(String masterDatabaseShardColumn) {
		this.masterDatabaseShardColumn = masterDatabaseShardColumn == null ? null : masterDatabaseShardColumn.toLowerCase();
	}

	public Object getMasterDatabaseShardValue() {
		return masterDatabaseShardValue;
	}

	public void setMasterDatabaseShardValue(Object masterDatabaseShardValue) {
		this.masterDatabaseShardValue = masterDatabaseShardValue;
	}

	public String getMasterTableShardColumn() {
		return masterTableShardColumn;
	}

	public void setMasterTableShardColumn(String masterTableShardColumn) {
		this.masterTableShardColumn = masterTableShardColumn == null ? null : masterTableShardColumn.toLowerCase();
	}

	public Object getMasterTableShardValue() {
		return masterTableShardValue;
	}

	public void setMasterTableShardValue(Object masterTableShardValue) {
		this.masterTableShardValue = masterTableShardValue;
	}

	public JdbcTemplate getSyncLogJdbcTemplate() {
		return syncLogJdbcTemplate;
	}

	public void setSyncLogJdbcTemplate(JdbcTemplate syncLogJdbcTemplate) {
		this.syncLogJdbcTemplate = syncLogJdbcTemplate;
	}

	public String getSyncLogId() {
		return syncLogId;
	}

	public void setSyncLogId(String syncLogId) {
		this.syncLogId = syncLogId;
	}

	public SlaveInfo[] getSlaveInfos() {
		return slaveInfos;
	}

	public void setSlaveInfos(SlaveInfo[] slaveInfos) {
		this.slaveInfos = slaveInfos;
	}

	public long getAfterMainDBSqlExecuteTime() {
		return afterMainDBSqlExecuteTime;
	}

	public void setAfterMainDBSqlExecuteTime(long afterMainDBSqlExecuteTime) {
		this.afterMainDBSqlExecuteTime = afterMainDBSqlExecuteTime;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public long getReplicationStartTime() {
		return replicationStartTime;
	}

	public void setReplicationStartTime(long replicationStartTime) {
		this.replicationStartTime = replicationStartTime;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Timestamp getNextSyncTime() {
		return nextSyncTime;
	}

	public void setNextSyncTime(Timestamp nextSyncTime) {
		this.nextSyncTime = nextSyncTime;
	}

	public String getMasterColumns() {
		return masterColumns;
	}

	public void setMasterColumns(String masterColumns) {
		this.masterColumns = masterColumns;
	}

	public String getSyncLogDsKey() {
		return syncLogDsKey;
	}

	public void setSyncLogDsKey(String syncLogDsKey) {
		this.syncLogDsKey = syncLogDsKey;
	}
}
