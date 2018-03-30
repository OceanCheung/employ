package com.ph.activiti.model;

/**
* <p>Title: 返回值</p>
* <p>Description: </p>
* <p>Company: 常州培涵信息科技有限公司</p> 
* @author ocean
* @date 2017年12月22日
 */
public class ReturnValue {
	private int code;
	private String msg;
	private Object data;

	public ReturnValue() {
		
	}

	public ReturnValue(int code, String msg, Object data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
	}
	
	public ReturnValue(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	
}
