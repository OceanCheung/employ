package com.ph.activiti.dao;

import org.apache.ibatis.annotations.Param;

import com.ph.activiti.model.Leave;

public interface LeaveDao {
	public int addLeave(Leave leave);

	public int updateLeaveProcess(@Param("processInstanceId") String processInstanceId,
			@Param("leaveId") Integer leaveId);
	
	public Leave listLeaveApplication(@Param("processInstanceId") String processInstanceId);
}
