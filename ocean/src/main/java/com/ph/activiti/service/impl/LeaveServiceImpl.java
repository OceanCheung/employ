package com.ph.activiti.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ph.activiti.dao.LeaveDao;
import com.ph.activiti.model.CustomSysException;
import com.ph.activiti.model.Leave;
import com.ph.activiti.model.ProcessKey;
import com.ph.activiti.service.LeaveService;
import com.ph.activiti.util.CommUtils;

@Service
public class LeaveServiceImpl implements LeaveService {

	@Autowired
	private LeaveDao leaveDao;

	@Autowired
	private RepositoryService repositoryService;
	@SuppressWarnings("unused")
	@Autowired
	private ManagementService managementService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private ProcessEngineConfiguration processEngineConfiguration;
	@SuppressWarnings("unused")
	@Autowired
	private ProcessEngineFactoryBean processEngine;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private TaskService taskService;

	@Override
	public Boolean applyLeave(Leave leave) {
		if (leaveDao.addLeave(leave) > 0)
			return true;
		else
			return false;
	}

	/**
	 * 1.启动请假流程 会产生一个真正的请假实例ID为7501(请假实例表act_ru_execution) 设置流程变量variables 保存leaveId
	 * 在流程表act_ru_variable中 2.根据当前的实例ID查询出当前员工申请请假的任务,根据任务ID结束当前任务。
	 */
	@Override
	public Boolean commitLeaveProcess(Integer leaveId) {
		boolean ifOk = true;
		// 请假单的Id //将流程实例的id存入请假表
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("leaveId", leaveId);
		try {
			// 根据请假的ID启动流程
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ProcessKey.LEAVE_KEY, variables);
			// 得到流程实例Id
			String processInstanceId = processInstance.getProcessInstanceId();
			// 根据流程实例Id查询任务
			Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
			// 完成员工填写请假单任务
			taskService.complete(task.getId());
			leaveDao.updateLeaveProcess(processInstanceId, leaveId);
		} catch (Exception e) {
			e.printStackTrace();
			ifOk = false;
		}
		return ifOk;
	}

	@Override
	public List<Leave> listWaitMatterPage(String userName, Integer page, Integer rows) {
		// 根据processIntanceId查询所有的办事集合
		List<Leave> listLeaveApplication = new ArrayList<>();
		try {
			// 获得角色所对应所有的task
			List<Task> listWaitMatterPage = taskService.createTaskQuery().taskAssignee(userName).listPage(page, rows);
			int size = taskService.createTaskQuery().taskAssignee(userName).list().size();
			System.err.println("所有的task"+listWaitMatterPage);
			// 根据task查询对应的processIntanceId
			List<String> listProcessInstanceIds = new ArrayList<>();
			if (!CommUtils.isEmpty(listWaitMatterPage)) {
				for (Task task : listWaitMatterPage) {
					listProcessInstanceIds.add(task.getProcessInstanceId());
				}
			}
			// 遍历根据流程实例id查询所有的事项
			if (!CommUtils.isEmpty(listProcessInstanceIds)) {
				for (String processInstanceId : listProcessInstanceIds) {
					Leave leave = leaveDao.listLeaveApplication(processInstanceId);
					listLeaveApplication.add(leave);
				}
			}
			System.out.println("总记录数："+size);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomSysException("程序未知异常");
		}
		return listLeaveApplication;
	}

	@Override
	public List<HistoricTaskInstance> listHistoryMatterPage(String userName, Integer page, Integer rows) {
		List<HistoricTaskInstance> listHistoryMatterPage = new ArrayList<>();
		try {
			listHistoryMatterPage = historyService.createHistoricTaskInstanceQuery()
					.taskAssignee(userName).listPage(page, rows);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listHistoryMatterPage;
	}

	@Override
	public Boolean examineByHeadmanVariableMsg(String userName, String processInstanceId, String msg) {
		Boolean ifOk = true;
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("msg", msg);
		try {
			Task task = taskService // 任务相关Service
					.createTaskQuery().processInstanceId(processInstanceId) // 创建任务查询
					.taskAssignee(userName) // 指定某个人
					.singleResult();
			System.out.println("任务ID:" + task.getId());
			System.out.println("任务名称:" + task.getName());
			System.out.println("任务创建时间:" + task.getCreateTime());
			System.out.println("任务委派人:" + task.getAssignee());
			System.out.println("流程实例ID:" + task.getProcessInstanceId());
			// 完成项目组长的审核任务
			taskService.complete(task.getId(), variables);
		} catch (Exception e) {
			ifOk = false;
		}
		return ifOk;
	}

	@Override
	public Boolean examineByHeadmanVariableDay(String userName, String processInstanceId, String day) {
		Boolean ifOk = true;
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("dasy", day);
		try {
			Task task = taskService // 任务相关Service
					.createTaskQuery().processInstanceId(processInstanceId) // 创建任务查询
					.taskAssignee(userName) // 指定某个人
					.singleResult();
			System.out.println("任务ID:" + task.getId());
			System.out.println("任务名称:" + task.getName());
			System.out.println("任务创建时间:" + task.getCreateTime());
			System.out.println("任务委派人:" + task.getAssignee());
			System.out.println("流程实例ID:" + task.getProcessInstanceId());
			// 完成项目组长的审核任务
			taskService.complete(task.getId(), variables);
		} catch (Exception e) {
			ifOk = false;
		}
		return ifOk;
	}

	@Override
	public Boolean examineByHeadmanVariableNull(String userName, String processInstanceId) {
		Boolean ifOk = true;
		try {
			Task task = taskService // 任务相关Service
					.createTaskQuery().processInstanceId(processInstanceId) // 创建任务查询
					.taskAssignee(userName) // 指定某个人
					.singleResult();
			System.out.println("任务ID:" + task.getId());
			System.out.println("任务名称:" + task.getName());
			System.out.println("任务创建时间:" + task.getCreateTime());
			System.out.println("任务委派人:" + task.getAssignee());
			System.out.println("流程实例ID:" + task.getProcessInstanceId());
			// 完成项目组长的审核任务
			taskService.complete(task.getId());
		} catch (Exception e) {
			ifOk = false;
		}
		return ifOk;
	}

	@Override
	public InputStream lookWholeProcessImage(String processInstanceId) {
		// 根据流程实例ID获取历史流程实例
		HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
				.processInstanceId(processInstanceId).singleResult();
		// 根据流程定义ID获取流程图
		BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
		// processEngineConfiguration = processEngine.getProcessEngineConfiguration();
		Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);
		ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
		ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity) repositoryService
				.getProcessDefinition(processInstance.getProcessDefinitionId());
		List<HistoricActivityInstance> highLightedActivitList = historyService.createHistoricActivityInstanceQuery()
				.processInstanceId(processInstanceId).list();
		// 高亮环节id集合
		List<String> highLightedActivitis = new ArrayList<String>();
		// 高亮线路id集合
		List<String> highLightedFlows = getHighLightedFlows(definitionEntity, highLightedActivitList);
		for (HistoricActivityInstance tempActivity : highLightedActivitList) {
			String activityId = tempActivity.getActivityId();
			highLightedActivitis.add(activityId);
		}
		// 中文显示的是口口口，设置字体就好了
		InputStream imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivitis,
				highLightedFlows, "宋体", "宋体", null, 1.0);
		// 单独返回流程图，不高亮显示
		// InputStream imageStream = diagramGenerator.generatePngDiagram(bpmnModel);
		return imageStream;
	}

	private List<String> getHighLightedFlows(ProcessDefinitionEntity processDefinitionEntity,
			List<HistoricActivityInstance> historicActivityInstances) {

		List<String> highFlows = new ArrayList<String>();// 用以保存高亮的线flowId
		for (int i = 0; i < historicActivityInstances.size() - 1; i++) {// 对历史流程节点进行遍历
			ActivityImpl activityImpl = processDefinitionEntity
					.findActivity(historicActivityInstances.get(i).getActivityId());// 得到节点定义的详细信息
			List<ActivityImpl> sameStartTimeNodes = new ArrayList<ActivityImpl>();// 用以保存后需开始时间相同的节点
			ActivityImpl sameActivityImpl1 = processDefinitionEntity
					.findActivity(historicActivityInstances.get(i + 1).getActivityId());
			// 将后面第一个节点放在时间相同节点的集合里
			sameStartTimeNodes.add(sameActivityImpl1);
			for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
				HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);// 后续第一个节点
				HistoricActivityInstance activityImpl2 = historicActivityInstances.get(j + 1);// 后续第二个节点
				if (activityImpl1.getStartTime().equals(activityImpl2.getStartTime())) {
					// 如果第一个节点和第二个节点开始时间相同保存
					ActivityImpl sameActivityImpl2 = processDefinitionEntity
							.findActivity(activityImpl2.getActivityId());
					sameStartTimeNodes.add(sameActivityImpl2);
				} else {
					// 有不相同跳出循环
					break;
				}
			}
			List<PvmTransition> pvmTransitions = activityImpl.getOutgoingTransitions();// 取出节点的所有出去的线
			for (PvmTransition pvmTransition : pvmTransitions) {
				// 对所有的线进行遍历
				ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition.getDestination();
				// 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
				if (sameStartTimeNodes.contains(pvmActivityImpl)) {
					highFlows.add(pvmTransition.getId());
				}
			}
		}
		return highFlows;
	}

	@Override
	public InputStream lookCurrentProcessImage(String processInstanceId) {
		Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
		// 流程定义
		BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
		// 正在活动节点
		List<String> activeActivityIds = runtimeService.getActiveActivityIds(task.getExecutionId());
		// 得到图片输出流（这样加可防止生成的流程图片乱码）
		InputStream inputStream = processEngineConfiguration.getProcessDiagramGenerator().generateDiagram(bpmnModel,
				"png", activeActivityIds, new ArrayList<>(), "宋体", "宋体", null, 1.0);
		return inputStream;
	}

	@Override
	public boolean deleteProcessByprocessInstanceId(String processInstanceId) {
		boolean ifOk = true;
		try {
			// 根据流程实例得到当前任务
			Task task = taskService // 任务相关Service
					.createTaskQuery().processInstanceId(processInstanceId) // 创建任务查询
					.singleResult();
			// 根据任务得到流程定义id
			String processDefinitionId = task.getProcessDefinitionId();
			// 得到流程定义id得到流程定义
			ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
			// 根据流程定义得到流程部署id
			String deploymentId = processDefinition.getDeploymentId();
			// 级联删除 已经在使用的流程实例信息也会被级联删除 (一般项目都使用级联删除)
			repositoryService.deleteDeployment(deploymentId, true);
		} catch (Exception e) {
			e.printStackTrace();
			ifOk = false;
		}
		return ifOk;
	}
}
