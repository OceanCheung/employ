package com.java.activiti.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.java.activiti.model.Leave;
import com.java.activiti.model.ReturnObject;
import com.java.activiti.service.LeaveService;

@RestController
@RequestMapping("/leave")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LeaveController {

	private Logger logger = Logger.getLogger(LeaveController.class);

	@Autowired
	private LeaveService leaveService;

	// 注入请假流程业务
	@Autowired
	RepositoryService repositoryService;
	@Autowired
	ManagementService managementService;
	@Autowired
	protected RuntimeService runtimeService;
	@Autowired
	ProcessEngineConfiguration processEngineConfiguration;
	@Autowired
	ProcessEngineFactoryBean processEngine;
	@Autowired
	HistoryService historyService;
	@Autowired
	TaskService taskService;

	/**
	 * 申请请假
	 * 
	 * @param leave
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/addLeave", method = { RequestMethod.POST, RequestMethod.GET })
	public ReturnObject addLeave(Leave leave, HttpServletRequest request) {
		logger.debug("请假单：" + leave.toString());
		Boolean isTrue = leaveService.addLeave(leave);
		if (isTrue) {
			return new ReturnObject(1, "填写请假单成功", null);
		} else {
			return new ReturnObject(-1, "填写请假单失败", null);
		}

	}

	/**
	 * 提交請假流程申請
	 * 
	 * 1.启动请假流程 会产生一个真正的请假实例ID为7501(请假实例表act_ru_execution) 设置流程变量variables 保存leaveId
	 * 在流程表act_ru_variable中 2.根据当前的实例ID查询出当前员工申请请假的任务,根据任务ID结束当前任务。
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/startLeave")
	public ReturnObject startLeave(Integer leaveId) throws Exception {
		// 请假单的Id //将流程实例的id存入请假表
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("leaveId", leaveId);
		// 根据请假的ID启动流程
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("activitiemployeeProcess",
				variables);
		// 得到流程实例Id
		String processInstanceId = processInstance.getProcessInstanceId();
		// 根据流程实例Id查询任务
		Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
		// 完成员工填写请假单任务
		taskService.complete(task.getId());
		Boolean isTrue = leaveService.updateLeaveProcess(processInstanceId, leaveId);
		if (isTrue) {
			return new ReturnObject(1, "提交申请成功", null);
		} else {
			return new ReturnObject(-1, "提交申请失败", null);
		}
	}

	@RequestMapping("/seeCurrentImage")
	public void queryProPlan(HttpServletRequest request, HttpServletResponse response,String processInstanceId) throws IOException {
		// 根据流程实例ID获取历史流程实例
		HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
				.processInstanceId(processInstanceId).singleResult();
		//根据流程定义ID获取流程图
		BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
		processEngineConfiguration = processEngine.getProcessEngineConfiguration();
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
		// 输出资源内容到相应对象
		byte[] b = new byte[1024];
		int len;
		while ((len = imageStream.read(b, 0, 1024)) != -1) {
			response.getOutputStream().write(b, 0, len);
		}
	}
	 /**  
     * 获取需要高亮的线  
     * @param processDefinitionEntity  
     * @param historicActivityInstances  
     * @return  
     */  
    private List<String> getHighLightedFlows(  
            ProcessDefinitionEntity processDefinitionEntity,  
            List<HistoricActivityInstance> historicActivityInstances) {  
          
        List<String> highFlows = new ArrayList<String>();// 用以保存高亮的线flowId  
        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {// 对历史流程节点进行遍历  
            ActivityImpl activityImpl = processDefinitionEntity  
                    .findActivity(historicActivityInstances.get(i)  
                            .getActivityId());// 得到节点定义的详细信息  
            List<ActivityImpl> sameStartTimeNodes = new ArrayList<ActivityImpl>();// 用以保存后需开始时间相同的节点  
            ActivityImpl sameActivityImpl1 = processDefinitionEntity  
                    .findActivity(historicActivityInstances.get(i + 1)  
                            .getActivityId());  
            // 将后面第一个节点放在时间相同节点的集合里  
            sameStartTimeNodes.add(sameActivityImpl1);  
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {  
                HistoricActivityInstance activityImpl1 = historicActivityInstances  
                        .get(j);// 后续第一个节点  
                HistoricActivityInstance activityImpl2 = historicActivityInstances  
                        .get(j + 1);// 后续第二个节点  
                if (activityImpl1.getStartTime().equals(  
                        activityImpl2.getStartTime())) {  
                    // 如果第一个节点和第二个节点开始时间相同保存  
                    ActivityImpl sameActivityImpl2 = processDefinitionEntity  
                            .findActivity(activityImpl2.getActivityId());  
                    sameStartTimeNodes.add(sameActivityImpl2);  
                } else {  
                    // 有不相同跳出循环  
                    break;  
                }  
            }  
            List<PvmTransition> pvmTransitions = activityImpl  
                    .getOutgoingTransitions();// 取出节点的所有出去的线  
            for (PvmTransition pvmTransition : pvmTransitions) {  
                // 对所有的线进行遍历  
                ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition  
                        .getDestination();  
                // 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示  
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {  
                    highFlows.add(pvmTransition.getId());  
                }  
            }  
        }  
        return highFlows;  
    }  
}  

