package com.cmcglobal.Utils;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.JiraWorkflow;

import java.util.stream.Stream;

public interface IWorkflowUtils {
    JiraWorkflow ImportFromXMLFile(String pathOfFile,String workflowName) throws Exception;

    AssignableWorkflowScheme createWorkflowScheme(String name, String description, JiraWorkflow workflow) throws Exception;

    void addWorkflowSchemeToProject(AssignableWorkflowScheme workflowScheme, Project project);

    JiraWorkflow getWorkflowByName(String name);
}
