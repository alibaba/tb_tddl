/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	/**
 * 
 */
package com.taobao.tddl.common.sequence;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.taobao.tddl.common.sequence.Config.Route.ExpressionFactory;

/*
 * @author guangxia
 * @since 1.0, 2009-4-27 下午02:03:07
 */
public class Config {
	public static final int DEFAULT = 1;
	public static final int Long2DATE = 2;
	
	private Config.Route tableRoute;
	private Config.Route databaseRoute;
	private boolean positionRight = true;
	private boolean overFlowCheck = true;
	private int totalSize = 0;
	private int type;

	public Config.Route getTableRoute() {
		return tableRoute;
	}

	public void setTablerRoute(Config.Route tablerRoute) {
		this.tableRoute = tablerRoute;
	}

	public Config.Route getDatabaseRoute() {
		return databaseRoute;
	}

	public void setDatabaseRoute(Config.Route databaseRoute) {
		this.databaseRoute = databaseRoute;
	}

	public void setPositionRight(boolean positionRight) {
		this.positionRight = positionRight;
	}

	public boolean isPositionRight() {
		return positionRight;
	}
	
	public static class Factory {
		private Map<String, Config> configs = new TreeMap<String, Config>();
		
		public Factory(String path) throws DocumentException, ConfigException {
			configs = new TreeMap<String, Config>();
			//启用dtd验证
			SAXReader reader = new SAXReader(true);
			reader.setEntityResolver(new LocalEntityResolver());
			Document root = reader.read(getClass().getResource(path));
	        List<?> nodes = root.selectNodes("/generators/generator");
	        for(Object nodeObj : nodes) {
	        	Node node = (Node)nodeObj;
	        	configs.put(node.valueOf("@id").toLowerCase(), Config.createConfig(node));
	        }
		}
		
		public Config newInstance(String id) {
			return configs.get(id.toLowerCase());
		}
	}
	
	public static Config createConfig(Node generator) throws ConfigException {
		Config config = new Config();
		
		String typeStr = generator.valueOf("@type");
		if(typeStr == null || "".equals(typeStr) || "default".equals(typeStr)) {
			config.setType(DEFAULT);
		} else if("long2date".equals(typeStr)) {
			config.setType(Long2DATE);
		} else {
			throw new Config.ConfigException("unsupported generator type!");
		}
		
		Node route = generator.selectSingleNode("route");
		if(route != null) {
			String position = route.valueOf("@position");
			if("right".equals(position) || "".equals(position)) {
				config.setPositionRight(true);
			} else if("left".equals(position)) {
				config.setPositionRight(false);
			} else {
				throw new Config.ConfigException();
			}
			String overFlowCheckStr = route.valueOf("@overflowcheck");
			if("on".equals(overFlowCheckStr) || "".equals(overFlowCheckStr)) {
				config.setOverFlowCheck(true);
			} else if("off".equals(overFlowCheckStr)) {
				config.setOverFlowCheck(false);
			} else {
				throw new Config.ConfigException();
			}
			String totalSizeStr = route.valueOf("@size");
			if (totalSizeStr != null && !"".equals(totalSizeStr)) {
				try {
					config.setTotalSize(Integer.parseInt(totalSizeStr));
				} catch (NumberFormatException e) {
					throw new Config.ConfigException();
				}
			}

			Node database = route.selectSingleNode("database");
			int routeSize = 0;
			if(database != null) {
				Route dbRoute = new Route();
				try {
					dbRoute.setSize(Integer.parseInt(database.valueOf("@size")));
					routeSize += dbRoute.getSize();
				} catch (NumberFormatException e) {
					throw new Config.ConfigException();
				}
				dbRoute.setExpression(ExpressionFactory.create(database.selectSingleNode("*")));
				config.setDatabaseRoute(dbRoute);
			}
			
			Node table = route.selectSingleNode("table");
			if(table != null) {
				Route tableRoute = new Route();
				try {
					tableRoute.setSize(Integer.parseInt(table.valueOf("@size")));
					routeSize += tableRoute.getSize();
				} catch (NumberFormatException e) {
					throw new Config.ConfigException();
				}
				tableRoute.setExpression(ExpressionFactory.create(table.selectSingleNode("*")));
				config.setTablerRoute(tableRoute);
			}
			
			if (config.isOverFlowCheck() && routeSize > 8) {
				throw new Config.ConfigException("散库散表信息所占用的id位数(十进制)不能超过8");
			}
			if(config.getTotalSize() != 0 && routeSize != 0 && config.getTotalSize() != routeSize) {
				throw new Config.ConfigException();
			}
		}
		return config;
	}


	public void setOverFlowCheck(boolean overFlowCheck) {
		this.overFlowCheck = overFlowCheck;
	}

	public boolean isOverFlowCheck() {
		return overFlowCheck;
	}

	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
	}

	public int getTotalSize() {
		return totalSize;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public static class Route {
		private int size;
		private Route.Expression<?> expression;

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public Route.Expression<?> getExpression() {
			return expression;
		}

		public void setExpression(Route.Expression<?> expression) {
			this.expression = expression;
		}

		public static interface Expression<ArgType> {
			/*
			 * @param arg 这个参数是运行时传过来的散库散表字段
			 * @return 运算后的散库散表信息
			 */
			int execute(ArgType arg);
		}

		public static abstract class AbstractExpression<ArgType> implements
				Route.Expression<ArgType> {
		}
		
		/*
		 * 这是最原始的不进行任何计算直接作为分库分表信息
		 */
		public static class SimpleExp extends Route.AbstractExpression<Integer> {
			public int execute(Integer arg) {
				return arg;
			}
		}

		/*
		 * 取模运算
		 */
		public static class ModExp extends Route.AbstractExpression<Integer> {

			private int arg0;

			ModExp(int arg0) {
				this.arg0 = arg0;
			}

			public int execute(Integer arg) {
				return arg % arg0;
			}

		}

		/*
		 * @see #execute
		 */
		public static class DayOfYear extends Route.AbstractExpression<Date> {
			public boolean isLeapYear(int year) {
				if(year % 4 == 0){
					if(year % 100 == 0) {
						if(year % 400 == 0) {
							return true;
						} else {
							return false;
						}
					} else {
						return true;
					}
				} else {
					return false;
				}
			}
			
			/*
			 * 这个和Calendar的dayofyear是不同的
			 * @see com.taobao.tddl.common.utils.sequenceGenerator.routed.Config.Route.Expression#execute(java.lang.Object)
			 */
			public int execute(Date arg) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(arg);
				int dayofyear = cal.get(Calendar.DAY_OF_YEAR);
				//为了使得闰年和非闰年的dayofyear相同
				/*
				if(isLeapYear(cal.get(Calendar.YEAR)) == false) {
					if(dayofyear > 59) {
						dayofyear++;
					}
				}
				*/
				return dayofyear;
			}

		}

		public static class DayOfMonth extends Route.AbstractExpression<Date> {
			public int execute(Date arg) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(arg);
				return cal.get(Calendar.DAY_OF_MONTH);
			}
		}

		public static class DayOfWeek extends Route.AbstractExpression<Date> {
			public int execute(Date arg) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(arg);
				return cal.get(Calendar.DAY_OF_WEEK);
			}
		}
		public static class DayOfWeek_sun_is_7 extends Route.AbstractExpression<Date> {
			public int execute(Date arg) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(arg);
				int ret = cal.get(Calendar.DAY_OF_WEEK)-1;
				if(ret == 0){
					ret = 7;
				}
				return ret;
			}
		}

		public static class MonthOfYear extends Route.AbstractExpression<Date> {
			public int execute(Date arg) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(arg);
				return cal.get(Calendar.MONTH);
			}
		}
		
		public static class MonthOfYear_JanuaryIs1 extends Route.AbstractExpression<Date> {
			public int execute(Date arg) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(arg);
				return cal.get(Calendar.MONTH) + 1;
			}
		}


		public static class ExpressionFactory {
			static Route.Expression<?> create(Node node)
					throws Config.ConfigException {
				if(node == null) {
					return new SimpleExp();
				} else if ("mod".equals(node.getName())) {
					try {
						return new ModExp(Integer.parseInt(node.getText()));
					} catch (NumberFormatException e) {
						throw new Config.ConfigException();
					}
				} else if ("dayofyear".equals(node.getName())) {
					return new DayOfYear();
				} else if ("dayofmonth".equals(node.getName())) {
					return new DayOfMonth();
				} else if ("dayofweek".equals(node.getName())) {
					return new DayOfWeek();
				} else if ("monthofyear".equals(node.getName())) {
					return new MonthOfYear();
				} else if("dayofweek-sun-is-7".equals(node.getName())){
					return new DayOfWeek_sun_is_7();
				} else if("monthofyear-january-is-1".equals(node.getName())) {
					return new MonthOfYear_JanuaryIs1();
				} else {
					throw new Config.ConfigException();
				}
			}
		}
	}

	public static class ConfigException extends Exception {
		private static final long serialVersionUID = 1L;

		public ConfigException() {
			super();
		}

		public ConfigException(String msg) {
			super(msg);
		}
	}
	
}
