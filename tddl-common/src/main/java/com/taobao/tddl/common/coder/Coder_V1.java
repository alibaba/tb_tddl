/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//package com.taobao.tddl.common.coder;
//
//import java.util.LinkedList;
//import java.util.List;
//
//import com.taobao.tddl.common.SyncCommand;
//
///**
// * @author huali
// * 
// *         数据库指令编解码器的V1版本的实现 这个版本是一个简单的实现
// *         按照命令的顺序，转换命令，一条命令内的元素用共同的分隔符分开，命令之间用另外一个分隔符 命令的格式是 I|U tableName
// *         columnName S|L value
// */
//public class Coder_V1 implements Coder {
//	private static final String SPLITTER_1 = "" + (char) 1;
//	private static final String SPLITTER_2 = "" + (char) 2;
//
//	private static final String UPDATE_FLAG = "U";
//	private static final String INSERT_FLAG = "I";
//
//	private static final String STRING_FLAG = "S";
//	private static final String LONG_FLAG = "L";
//
//	public List<SyncCommand> decode(String content) {
//		String[] elements = content.split(SPLITTER_2);
//		List<SyncCommand> commands = new LinkedList<SyncCommand>();
//
//		for (String element : elements) {
//			String[] arr = element.split(SPLITTER_1);
//			if (5 != arr.length) {
//				continue;
//			}
//			SyncCommand command = new SyncCommand();
//			if (UPDATE_FLAG.equals(arr[0])) {
//				command.setType(SyncCommand.TYPE.UPDATE);
//			} else {
//				command.setType(SyncCommand.TYPE.INSERT);
//			}
//
//			command.setTableName(arr[1]);
//			command.setColumnName(arr[2]);
//			if (STRING_FLAG.equals(arr[3])) {
//				command.setValue(arr[4]);
//			} else {
//				command.setValue(Long.parseLong(arr[4]));
//			}
//
//			commands.add(command);
//		}
//
//		return commands;
//	}
//
//	public String encode(List<SyncCommand> commands) {
//		if (null == commands) {
//			return "";
//		}
//
//		StringBuilder sb = new StringBuilder();
//
//		for (SyncCommand command : commands) {
//			if (sb.length() > 0) {
//				sb.append(SPLITTER_2);
//			}
//			switch (command.getType()) {
//			case INSERT:
//				sb.append(INSERT_FLAG);
//				break;
//
//			case UPDATE:
//				sb.append(UPDATE_FLAG);
//				break;
//			}
//			sb.append(SPLITTER_1);
//
//			sb.append(command.getTableName());
//			sb.append(SPLITTER_1);
//
//			sb.append(command.getColumnName());
//			sb.append(SPLITTER_1);
//
//			if (command.getValue() instanceof String) {
//				sb.append(STRING_FLAG);
//			} else {
//				sb.append(LONG_FLAG);
//			}
//			sb.append(SPLITTER_1);
//
//			sb.append(command.getValue().toString());
//		}
//
//		return sb.toString();
//	}
//
//	public String getId() {
//		return "1";
//	}
//}
