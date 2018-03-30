package com.ph.activiti.service;

import java.io.InputStream;
import java.util.List;

import org.activiti.engine.history.HistoricTaskInstance;

import com.ph.activiti.model.Leave;

public interface LeaveService {
	/** 申请请假 */
	Boolean applyLeave(Leave leave);
	/** 提交请假申请*/
	Boolean commitLeaveProcess(Integer leaveId);
	/** 查询待办事项*/
	List<Leave> listWaitMatterPage(String userName,Integer page,Integer rows);
	/** 查询历史事项*/
	List<HistoricTaskInstance> listHistoryMatterPage(String userName,Integer page,Integer rows);
	/** 项目组长审核通过还是不通过*/
	Boolean examineByHeadmanVariableMsg(String userName,String processInstanceId,String msg);
	/** 人事部审核请假天数*/
	Boolean examineByHeadmanVariableDay(String userName,String processInstanceId,String day);
	/** 项目组长审批 无意见*/
	Boolean examineByHeadmanVariableNull(String userName,String processInstanceId);
	/**查看整个流程过程图*/
	InputStream lookWholeProcessImage(String processInstanceId);
	/**根据流程实例Id查看活动节点的流程图*/
	InputStream lookCurrentProcessImage(String processInstanceId);
	/**根据流程实例Id删除流程*/
	boolean deleteProcessByprocessInstanceId(String processInstanceId);
}
