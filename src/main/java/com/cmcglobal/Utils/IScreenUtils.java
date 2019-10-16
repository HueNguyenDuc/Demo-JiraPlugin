package com.cmcglobal.Utils;

import com.atlassian.jira.issue.fields.screen.*;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.project.Project;

import java.util.List;

public interface IScreenUtils {


    List<FieldScreenTab> getAllScreenTab();

    FieldScreenTab createScreenTab(FieldScreen screen, String name, List<String> listOfField) throws Exception;

    FieldScreenSchemeItem createSchemeItem(FieldScreen screen, FieldScreenScheme scheme, ScreenableIssueOperation operation) throws Exception;

    FieldScreenScheme createScreenScheme(String name, String description) throws Exception;

    FieldScreen createFieldScreen(String name, String description) throws Exception;

    IssueTypeScreenScheme createIssueScreenScheme(String name, String description) throws Exception;

    IssueTypeScreenSchemeEntity createIssueScreenSchemeEntity(IssueTypeScreenScheme issueTypeScreenScheme, FieldScreenScheme screenScheme, String issueTypeId);

    void addIssueTypeScreenToProject(IssueTypeScreenScheme scheme, Project project);

    FieldScreenLayoutItem addFieldToScreenTab(FieldScreenTab fieldScreenTab, String systemFieldId);
}
