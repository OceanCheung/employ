package com.ph.activiti.start;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
/**
 * @description:结合spring自动部署activit流程定义文件
 * @version：1.0
 * @author：liumeiwu
 */
public class WorkflowDeployer implements InitializingBean,ApplicationContextAware{
    private static final Logger logger = LoggerFactory.getLogger(WorkflowDeployer.class);
    private Resource[] deploymentResources;
    private String category;
    private ApplicationContext applicationContext;
    public WorkflowDeployer() {
        super();
        System.err.println(WorkflowDeployer.class.getName());
    }
    public void setapplicationcontext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    public void afterPropertiesSet() throws Exception {
        logger.info("visitor.bpmn20.xml 文件自动部署");
        if (category == null) { throw new FatalBeanException("缺失属性 : category"); }
        if (deploymentResources != null) {
            RepositoryService repositoryService = applicationContext.getBean(RepositoryService.class);
            for (Resource r : deploymentResources) {
                String deploymentName = category + "_" + r.getFilename();
                String resourceName = r.getFilename();
                boolean dodeploy = true;
                List<Deployment> deployments = repositoryService.createDeploymentQuery().deploymentName(deploymentName).orderByDeploymenTime().desc().list();
                if (!deployments.isEmpty()) {
                    Deployment existing = deployments.get(0);
                    try {
                        InputStream in = repositoryService.getResourceAsStream(existing.getId(), resourceName);
                        if (in != null) {
                            File f = File.createTempFile("deployment", "xml", new File(System.getProperty("java.io.tmpdir")));
                            f.deleteOnExit();
                            OutputStream out = new FileOutputStream(f);
                            IOUtils.copy(in, out);
                            in.close();
                            out.close();
                            dodeploy = (FileUtils.checksumCRC32(f) != FileUtils.checksumCRC32(r.getFile()));
                        } else throw new ActivitiException("不能读取资源 " + resourceName + ", 输入流为空");
                    } catch (ActivitiException ex) {
                        logger.error("unable to read " + resourceName + " of deployment " + existing.getName() + ", id: " + existing.getId() + ", will re-deploy");
                    }
                }
                if (dodeploy) {
                    repositoryService.createDeployment().name(deploymentName).addInputStream(resourceName, r.getInputStream()).deploy();
                    logger.warn("文件部署成功 : " + r.getFilename());
                }
            }
        }
    }
    public Resource[] getDeploymentResources() {
        return deploymentResources;
    }
    public void setDeploymentResources(Resource[] deploymentResources) {
        this.deploymentResources = deploymentResources;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    @Override
    public String toString() {
        return "WorkflowDeployer [deploymentResources=" + Arrays.toString(deploymentResources) + ", category=" + category + ", applicationContext=" + applicationContext + "]";
    }
}