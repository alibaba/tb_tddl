/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.ReentrantLock;


public class MD5Maker {
	 private static MD5Maker md5Maker       = new MD5Maker();
	private static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	private static MessageDigest mHasher;

	private ReentrantLock opLock = new ReentrantLock();

	public MD5Maker() {
		try {
			mHasher = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(
					"should not be here,cant find md5 instance?", e);
		}
	}

	public static MD5Maker getInstance(){
		return md5Maker;
	}
	/**
	 * 将一个字节数组转化为可见的字符串
	 * 
	 * @param bt
	 * @return
	 */
	public String bytes2string(byte[] bt) {
		int l = bt.length;

		char[] out = new char[l << 1];

		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = digits[(0xF0 & bt[i]) >>> 4];
			out[j++] = digits[0x0F & bt[i]];
		}

		return new String(out);
	}

	/**
	 * 对字符串进行md5
	 * 
	 * @param str
	 * @return md5 byte[16]
	 */
	public byte[] getMD5bytes(String str) {
		opLock.lock();
		try {
			byte[] bt = mHasher.digest(str.getBytes("UTF-8"));
			if (null == bt || bt.length != 16) {
				throw new IllegalArgumentException("md5 need");
			}
			return bt;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("unsupported utf-8 encoding", e);
		} finally {
			opLock.unlock();
		}
	}
	public String getMD5(String str){
		return bytes2string(getMD5bytes(str));
	}
}
