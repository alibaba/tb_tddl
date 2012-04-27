/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.config.object;

import com.taobao.tddl.common.util.TStringUtil;import com.taobao.tddl.jdbc.atom.common.TAtomConstants;
/**
 * 数据库状态枚举类型
 * @author qihao
 *
 */
public enum AtomDbStatusEnum {

	R_STAUTS(TAtomConstants.DB_STATUS_R), W_STATUS(TAtomConstants.DB_STATUS_W), RW_STATUS(TAtomConstants.DB_STATUS_RW), NA_STATUS(
			TAtomConstants.DB_STATUS_NA);

	private String status;

	AtomDbStatusEnum(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public static AtomDbStatusEnum getAtomDbStatusEnumByType(String type) {
		AtomDbStatusEnum statusEnum = null;
		if (type!=null&&!"".equals(type.trim())) {
			String typeStr = type.toUpperCase().trim();
			if (typeStr.length() > 1) {
				if (AtomDbStatusEnum.NA_STATUS.getStatus().equals(typeStr)) {
					statusEnum = AtomDbStatusEnum.NA_STATUS;
				} else if (!TStringUtil.contains(typeStr, AtomDbStatusEnum.NA_STATUS.getStatus())
								&&TStringUtil.contains(typeStr, AtomDbStatusEnum.R_STAUTS.getStatus())
								&& TStringUtil.contains(typeStr, AtomDbStatusEnum.W_STATUS.getStatus())) {
					statusEnum = AtomDbStatusEnum.RW_STATUS;
				}
			} else {
				if (AtomDbStatusEnum.R_STAUTS.getStatus().equals(typeStr)) {
					statusEnum = AtomDbStatusEnum.R_STAUTS;
				} else if (AtomDbStatusEnum.W_STATUS.getStatus().equals(typeStr)) {
					statusEnum = AtomDbStatusEnum.W_STATUS;
				}
			}
		}
		return statusEnum;
	}

	public boolean isNaStatus() {
		return this == AtomDbStatusEnum.NA_STATUS;
	}

	public boolean isRstatus() {
		return this == AtomDbStatusEnum.R_STAUTS || this == AtomDbStatusEnum.RW_STATUS;
	}

	public boolean isWstatus() {
		return this == AtomDbStatusEnum.W_STATUS || this == AtomDbStatusEnum.RW_STATUS;
	}

	public boolean isRWstatus() {
		return this == AtomDbStatusEnum.RW_STATUS;
	}
}
