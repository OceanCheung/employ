package com.ph.activiti.util;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	/**
	 * 获取timestamp时间戳
	 */
	public static Timestamp getTimestamp(String pattern) {
		if(pattern==null || "".equals(pattern))
			pattern = "yyyy-MM-dd HH:mm:ss";
		return Timestamp.valueOf(new SimpleDateFormat(pattern).format(new Date()));
	}
	/**
	 * 获取timestamp时间戳
	 */
	public static Timestamp getTimes(String str,String pattern) {
		if(pattern==null || "".equals(pattern))
			pattern = "yyyy-MM-dd HH:mm:ss";
		return Timestamp.valueOf(new SimpleDateFormat(pattern).format(new Date()));
	}
}
