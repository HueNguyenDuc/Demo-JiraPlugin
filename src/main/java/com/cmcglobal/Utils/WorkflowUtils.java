package com.cmcglobal.Utils;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.*;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.InputStream;
import java.util.Collection;
import java.util.stream.Stream;

@ExportAsService({IWorkflowUtils.class})
@Named
public class WorkflowUtils implements IWorkflowUtils {
    private static final Logger log = LoggerFactory.getLogger(WorkflowUtils.class);

    private PluginAccessor _pluginAccessor;
    private WorkflowManager _workflowManager;
    private UserManager _userManager;
    private WorkflowSchemeManager _workflowSchemeManager;
    private ApplicationUser _user;
    private IssueTypeManager _issueTypeManager;

    public WorkflowUtils() {
        _pluginAccessor = ComponentAccessor.getPluginAccessor();
        _workflowManager = ComponentAccessor.getWorkflowManager();
        _userManager = ComponentAccessor.getUserManager();
        _workflowSchemeManager = ComponentAccessor.getWorkflowSchemeManager();
        _issueTypeManager = ComponentAccessor.getComponent(IssueTypeManager.class);
        _user = _userManager.getUserByName(UtilConstaints.USERNAME);
    }

    @Override
    public JiraWorkflow ImportFromXMLFile(String pathOfFile,String workflowName) throws Exception {
        if(pathOfFile == null||
                workflowName == null)
            throw new Exception(UtilConstaints.ERROR_FILENOTFOUND);
        Stream<JiraWorkflow> existsWorkflow = _workflowManager.getActiveWorkflows().stream().filter(e -> e.getName().equals(workflowName));
        if(existsWorkflow!=null&&existsWorkflow.count() > 0)
            return _workflowManager.getActiveWorkflows().stream().filter(e -> e.getName().equals(workflowName)).findFirst().get();
        try {
            //JiraWorkflow existsWorkflow = _workflowManager
            WorkflowDescriptor workflowDescriptor;
            InputStream inputStream = _pluginAccessor.getDynamicResourceAsStream(pathOfFile);
            workflowDescriptor = WorkflowLoader.load(inputStream, true);
            ConfigurableJiraWorkflow newWorkflow = new ConfigurableJiraWorkflow(workflowName, _workflowManager);
            newWorkflow.setDescriptor(workflowDescriptor);
            _workflowManager.createWorkflow(_user, newWorkflow);
            return newWorkflow;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(UtilConstaints.ERROR_IMPORTXMLFAIL);
        }
    }

    @Override
    public AssignableWorkflowScheme createWorkflowScheme(String name, String description, JiraWorkflow workflow) throws Exception {
        if(workflow == null)
            throw new Exception(UtilConstaints.ERROR_WORKFLOW_NOTFOUND);
        AssignableWorkflowScheme.Builder newScheme = _workflowSchemeManager.assignableBuilder();
        newScheme.setName(name);
        if(description != null)
            newScheme.setDescription(description);
        newScheme.setDefaultWorkflow(workflow.getName());
        Collection<IssueType> issueTypes = _issueTypeManager.getIssueTypes();
        issueTypes.forEach(e -> newScheme.setMapping(e.getId(), workflow.getName()));
        AssignableWorkflowScheme ret = _workflowSchemeManager.createScheme(newScheme.build());
        log.info(UtilConstaints.CREATE_SUCCESS_FORMAT, "Workflow Scheme", ret.getId().toString(), ret.getName());
        return ret;
    }

    @Override
    public void addWorkflowSchemeToProject(AssignableWorkflowScheme workflowScheme, Project project) {
        Stream<Scheme> wSchemeSearcher = _workflowSchemeManager.getSchemeObjects().stream().filter(e -> e.getName().equals(workflowScheme.getName()));
        if(wSchemeSearcher != null && wSchemeSearcher.count() > 0)
        {
            Scheme wScheme = _workflowSchemeManager.getSchemeObjects().stream().filter(e -> e.getName().equals(workflowScheme.getName())).findFirst().get();
            _workflowSchemeManager.addSchemeToProject(project, wScheme);
        }
    }

    @Override
    public JiraWorkflow getWorkflowByName(String name) throws Exception {
        Stream<JiraWorkflow> existsWorkflow = _workflowManager.getWorkflows().stream().filter(e -> e.getName().equals(name));
        if(existsWorkflow != null && existsWorkflow.count() > 0)
            return _workflowManager.getWorkflows().stream().filter(e -> e.getName().equals(name)).findFirst().get();
        throw new Exception("Workflow doesn't exists.");
    }
}
