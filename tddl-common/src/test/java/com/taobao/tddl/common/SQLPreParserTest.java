/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

public class SQLPreParserTest {
	private static final Log log = LogFactory.getLog("preparse");
	private static final Log faillog = LogFactory.getLog("preparseFail");

	@Test
	public void test() {
	}

	public static void main1(String[] args) throws IOException {
		String fileName = "D:/12_code/tddl/trunk/tddl/tddl-parser/test.xls";
		Workbook wb = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(fileName)));
		Sheet sheet = wb.getSheetAt(0);
		for (Row row : sheet) {
			Cell cell = row.getCell(2);
			System.out.println(cell.getStringCellValue());
		}
	}

	public static void main(String[] args) throws IOException {
		//String fileName = "D:/12_code/tddl/trunk/tddl/tddl-parser/sqlsummary-icsg-db0-db15-group-20100901100337-export.xlsx";
		//String fileName = "D:/12_code/tddl/trunk/tddl/tddl-parser/sqlsummary-tcsg-instance-group-20100901100641-export.xlsx";

		int count=0;
		long time=0;
		
		File home = new File(System.getProperty("user.dir")+"/appsqls");
		for (File f : home.listFiles()) {
			if (f.isDirectory() || !f.getName().endsWith(".xlsx")) {
				continue;
			}
			log.info("---------------------- " + f.getAbsolutePath());
			faillog.info("---------------------- " + f.getAbsolutePath());
			Workbook wb = new XSSFWorkbook(new FileInputStream(f));
			Sheet sheet = wb.getSheetAt(0);
			for (Row row : sheet) {
				Cell cell = row.getCell(2);
				if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING) {
					String sql = cell.getStringCellValue();
					
					long t0 = System.currentTimeMillis();
					String tableName = SQLPreParser.findTableName(sql);
					time += System.currentTimeMillis() - t0;
					count++;
					
					log.info(tableName + " <-- " + sql);
					if (tableName == null) {
						sql = sql.trim().toLowerCase();
						if (isCRUD(sql)) {
							System.out.println("failed:" + sql);
							faillog.error("failed:" + sql);
						}
					}
				}
			}
			wb = null;
		}
		faillog.fatal("------------------------------- finished --------------------------");
		faillog.fatal(count + " sql parsed, total time:" + time +". average time use per sql:"+ (double)time/count  +"ms/sql");
	}
	
	private static boolean isCRUD0(String sql) {
		if (!sql.startsWith("begin") && !sql.startsWith("declare") && !sql.startsWith("lock")
				&& !sql.startsWith("merge") && !sql.startsWith("explain") && !sql.startsWith("call")
				&& !sql.startsWith("alter") && !sql.startsWith("sql_text") && !sql.startsWith("with")
				&& !sql.startsWith("create")) {
			return true;
		}
		return false;
	}

	private static boolean isCRUD(String sql0) {
		if (sql0 == null)
			return false;
		sql0 = sql0.trim(); //trim可以去掉\\s,包括换行符、制表符等
		if (sql0.length() < 7) {
			return false;
		}

		if (sql0.indexOf("/*") != -1) {
			//去除hint
			//System.out.println("hint:"+sql0);
			sql0 = sql0.replaceAll("/\\*.*?\\*/", "").trim(); //懒惰匹配(最短匹配)
			//System.out.println(sql0);
		}
		sql0 = sql0.toLowerCase();

		if (sql0.startsWith("update") || sql0.startsWith("delete") || sql0.startsWith("insert")
				|| sql0.startsWith("select")) {
			return true;
		}

		return false;
	}
}
