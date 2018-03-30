package com.ph.activiti.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class MyScheduledJob extends QuartzJobBean {
	
	private SynchronizeBean synchronizeBean;

	public void setSynchronizeBean(SynchronizeBean synchronizeBean) {
		this.synchronizeBean = synchronizeBean;
	}

	@Override
	protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
		this.synchronizeBean.printAnotherMessage();
	}
}