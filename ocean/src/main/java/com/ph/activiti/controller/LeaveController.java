package com.ph.activiti.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.history.HistoricTaskInstance;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ph.activiti.model.Constant;
import com.ph.activiti.model.CustomSysException;
import com.ph.activiti.model.Leave;
import com.ph.activiti.model.ReturnValue;
import com.ph.activiti.service.LeaveService;
import com.ph.activiti.util.CommUtils;

@RestController
@RequestMapping("/leave")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LeaveController {

	private Logger logger = Logger.getLogger(LeaveController.class);

	@Autowired
	private LeaveService leaveService;

	/** 申请请假 */
	@RequestMapping(value = "/applyLeave", method = { RequestMethod.POST, RequestMethod.GET })
	public ReturnValue applyLeave(Leave leave, HttpServletRequest request) {
		logger.debug("请假单：" + leave.toString());
		Boolean isTrue = leaveService.applyLeave(leave);
		if (isTrue) {
			return new ReturnValue(Constant.SUCCESS, "填写请假单成功");
		} else {
			return new ReturnValue(Constant.ERROR, "填写请假单失败");
		}
	}

	/** 提交請假流程申請 */
	@RequestMapping("/commitLeave")
	public ReturnValue commitLeave(Integer leaveId) throws Exception {
		Boolean ifTrue = leaveService.commitLeaveProcess(leaveId);
		if (ifTrue) {
			return new ReturnValue(Constant.SUCCESS, "提交申请成功");
		} else {
			return new ReturnValue(Constant.ERROR, "提交申请失败");
		}
	}

	/** 根据角色查询待办事项 */
	@RequestMapping("/listWaitMatterPage/{userName}/{page}/{rows}")
	public ReturnValue listWaitMatterPage(@PathVariable String userName, @PathVariable Integer page,
			@PathVariable Integer rows) {
		List<Leave> listWaitMatterPage = leaveService.listWaitMatterPage(userName, page, rows);
		//listWaitMatterPage =null;
		if(CommUtils.isEmpty(listWaitMatterPage)) {
			throw new CustomSysException("没有查询到任何记录");
		}
		return new ReturnValue(Constant.SUCCESS, "查询成功", listWaitMatterPage);
	}

	/** 查询历史事项 */
	@RequestMapping("/listHistoryMatterPage/{userName}/{page}/{rows}")
	public ReturnValue listHistoryMatterPage(@PathVariable String userName, @PathVariable Integer page,
			@PathVariable Integer rows) {
		List<HistoricTaskInstance> listHistoryMatterPage = leaveService.listHistoryMatterPage(userName, page, rows);
		for (HistoricTaskInstance t : listHistoryMatterPage) {
			System.out.println(t.toString());
			System.out.println();
		}
		return new ReturnValue(Constant.SUCCESS, "查询成功", null);
	}

	/** 有分叉审批,根据流程实例Id得到当前任务 然后获得task 项目组长审核通过还是不通过 */
	@RequestMapping("/examineByHeadmanVariableMsg/{processInstanceId}/{userName}/{msg}")
	public ReturnValue examineByHeadmanVariableMsg(@PathVariable String userName,
			@PathVariable String processInstanceId, @PathVariable String msg) {
		Boolean ifTrue = leaveService.examineByHeadmanVariableMsg(userName, processInstanceId, msg);
		if (ifTrue)
			return new ReturnValue(Constant.SUCCESS, userName + "审核成功", null);
		else
			return new ReturnValue(Constant.ERROR, userName + "审核失败", null);
	}

	/*** 有分叉审批,根据流程实例Id得到当前任务 然后获得task 人事部审批 */
	@RequestMapping("/examineByHeadmanVariableDay/{processInstanceId}/{userName}/{day}")
	public ReturnValue examineByHeadmanVariableDay(@PathVariable String userName,
			@PathVariable String processInstanceId, @PathVariable String day) {
		Boolean ifTrue = leaveService.examineByHeadmanVariableDay(userName, processInstanceId, day);
		if (ifTrue)
			return new ReturnValue(Constant.SUCCESS, userName + "审核成功", null);
		else
			return new ReturnValue(Constant.ERROR, userName + "审核失败", null);
	}

	/** 无分叉审批,根据流程实例Id得到当前任务 然后获得task 项目组长无意见审批 */
	@RequestMapping("/examineByHeadmanVariableNull/{processInstanceId}/{userName}")
	public ReturnValue examineByHeadmanVariableNull(@PathVariable String userName,
			@PathVariable String processInstanceId) {
		Boolean ifTrue = leaveService.examineByHeadmanVariableNull(userName, processInstanceId);
		if (ifTrue)
			return new ReturnValue(Constant.SUCCESS, userName + "审核成功", null);
		else
			return new ReturnValue(Constant.ERROR, userName + "审核失败", null);
	}

	/** 查看某个流程的历史流程图 */
	@RequestMapping("/lookWholeProcessImage")
	public void lookWholeProcessImage(HttpServletRequest request, HttpServletResponse response,
			String processInstanceId) throws IOException {
		InputStream imageStream = leaveService.lookWholeProcessImage(processInstanceId);
		byte[] b = new byte[1024];
		int len;
		while ((len = imageStream.read(b, 0, 1024)) != -1) {
			response.getOutputStream().write(b, 0, len);
		}
	}

	/** 查看当前流程图 */
	@RequestMapping("/lookCurrentProcessImage")
	public void lookCurrentProcessImage(HttpServletRequest request, HttpServletResponse response,
			String processInstanceId) throws IOException {
		InputStream imageStream = leaveService.lookCurrentProcessImage(processInstanceId);
		byte[] b = new byte[1024];
		int len;
		while ((len = imageStream.read(b, 0, 1024)) != -1) {
			response.getOutputStream().write(b, 0, len);
		}
	}

	/** 根据流程实例id删除流程 */
	@RequestMapping("/deleteProcessByprocessInstanceId")
	public ReturnValue deleteProcessByprocessInstanceId(HttpServletRequest request, HttpServletResponse response,
			String processInstanceId) throws IOException {
		boolean ifTrue = leaveService.deleteProcessByprocessInstanceId(processInstanceId);
		if (ifTrue)
			return new ReturnValue(Constant.SUCCESS, "删除流程成功", null);
		else
			return new ReturnValue(Constant.ERROR, "删除流程失败", null);
	}

}
