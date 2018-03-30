package com.ph.activiti.model;

import java.io.Serializable;

/**
 * 自定义全局异常类
 */
public class CustomSysException extends RuntimeException implements Serializable{

	private static final long serialVersionUID = 1361000171094574599L;
	
	public CustomSysException() {
		super();
	}

	public CustomSysException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public CustomSysException(String msg) {
		super(msg);
	}

	public CustomSysException(Throwable throwable) {
		super(throwable);
	}
}