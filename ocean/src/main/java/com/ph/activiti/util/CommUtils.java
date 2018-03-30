package com.ph.activiti.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;


/**
 * 通用工具类
 * @author fiver
 *
 */
public class CommUtils {

	/**
	 * 判断一个对象(包括String、以及Collection下的各个子类集合) 是否为空, 为空则返回true, 否则会返回false
	 * @param obj
	 * @return
	 */
	public static boolean isEmpty(Object obj) {
		if(obj == null)
			return true;
		if(obj instanceof String) {
			if( ((String)obj).equals("") || ((String)obj).equals("null") ||((String)obj).equals("undefined"))
				return true;
		}
		if(obj instanceof Collection<?>) {
			if( ((Collection<?>)obj).size() <=0 )
				return true;
		}
		if(obj instanceof String[]) {
			if ( ((String[]) obj).length <= 0) {
				return true;
			}
		}
		if(obj instanceof Object[]) {
			if ( ((Object[]) obj).length <= 0) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * @param braceStr
	 * @return
	 */
	public static List<Integer> braceStr2List(String braceStr) {
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(braceStr);
		List<Integer> list = new ArrayList<Integer>();
		while (matcher.find()) {
			list.add(Integer.parseInt(matcher.group()));
		}
		
		return list;
	}
	
	
	/**
	 * 生成下载文件路径
	 * @return
	 */
	public static String getDownpath(HttpServletRequest request) {
//		String tempfilePath = request.getSession().getServletContext().getRealPath("/");
		String tempfilePath = "downfile";
		System.out.println("1 tempfilePath = " + tempfilePath);
		tempfilePath = tempfilePath.substring(tempfilePath.lastIndexOf("downfile"));
		tempfilePath = tempfilePath.replace("downfile", "");
		System.out.println("2 tempfilePath = " + tempfilePath);
		
		tempfilePath = tempfilePath + "downfile/" + CommUtils.getUUID();
		System.out.println("3 tempfilePath = " + tempfilePath);
		File tryfie = new File(tempfilePath);
		if (!tryfie.exists()) {
			tryfie.mkdirs();
		}
		return tempfilePath;
	}
	
	private static String getUUID() {
		return null;
	}


	/**
	 * 将指定日期按照指定的格式以字符串的形式进行返回
	 * @param pattern
	 * @param date
	 * @return
	 */
	public static String getDate(String pattern, Date date) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.format(date);
	}
	
	/**
	 * 按照指定的格式将当前的日期以字符串的形式返回
	 * @param pattern
	 * @return
	 */
	public static String getDate(String pattern) {
		return getDate(pattern, new Date());
	}
	
	/**
	 * 将当前日期按照 "年-月-日 时:分:秒" 的日期格式以字符串的形式返回
	 * @return
	 */
	public static String getDate() {
		return getDate("yyyy-MM-dd hh:mm:ss", new Date());
	}
	
	
	
	// 得到某一目录下的所有文件夹
	public static List<File> visitAll(File root) {
		List<File> list = new ArrayList<File>();
		File[] dirs = root.listFiles();
		if (dirs != null) {
			for (int i = 0; i < dirs.length; i++) {
				if (dirs[i].isDirectory()) {
					System.out.println("name:" + dirs[i].getPath());
					list.add(dirs[i]);
				}
				visitAll(dirs[i]);
			}
		}
		return list;
	}
	
	/**
	   * 删除空的文件夹
	   * @param list
	   */
	public static void deleteEmptyDirectory(String pathname) {
		List<File> list = visitAll(new File(pathname));
		for (int i = 0; i < list.size(); i++) {
			File temp = list.get(i);
			// 是目录且为空
			if (temp.isDirectory() && temp.listFiles().length <= 0) {
				temp.delete();
			}
		}
	}
	
	
}