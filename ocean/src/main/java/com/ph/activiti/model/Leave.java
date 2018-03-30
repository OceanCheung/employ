package com.ph.activiti.model;

import java.sql.Timestamp;

public class Leave {
	private int id;
	private Timestamp leaveDate;
	private String leaveDays;
	private String leaveReason;
	private String processInstanceId;
	private User user;

	public Leave() {
		super();
	}

	public Leave(int id, Timestamp leaveDate, String leaveDays, String leaveReason, String processInstanceId,
			User user) {
		this.id = id;
		this.leaveDate = leaveDate;
		this.leaveDays = leaveDays;
		this.leaveReason = leaveReason;
		this.processInstanceId = processInstanceId;
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Timestamp getLeaveDate() {
		return leaveDate;
	}

	public void setLeaveDate(Timestamp leaveDate) {
		this.leaveDate = leaveDate;
	}

	public String getLeaveDays() {
		return leaveDays;
	}

	public void setLeaveDays(String leaveDays) {
		this.leaveDays = leaveDays;
	}

	public String getLeaveReason() {
		return leaveReason;
	}

	public void setLeaveReason(String leaveReason) {
		this.leaveReason = leaveReason;
	}

	public String getProcessIntanceId() {
		return processInstanceId;
	}

	public void setProcessIntanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	@Override
	public String toString() {
		return "Leave [id=" + id + ", leaveDate=" + leaveDate + ", leaveDays=" + leaveDays + ", leaveReason="
				+ leaveReason + ", processInstanceId=" + processInstanceId + ", user=" + user + "]";
	}

}
