package com.cmcglobal.Utils;

import com.atlassian.jira.config.IssueTypeService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Collection;
import java.util.List;

public interface IIssueUtils {
    IssueTypeService.IssueTypeCreateInput.Type STANDARD();

    IssueTypeService.IssueTypeCreateInput.Type SUBTASK();

    Collection<ScreenableIssueOperation> getAllOperation();

    Collection<IssueType> getAllIssueType();

    FieldConfigScheme AddIssueTypeSchemeToProject(FieldConfigScheme scheme, Project project) throws Exception;

    IssueType createIssueType(String name, String description, Long avatarId, IssueTypeService.IssueTypeCreateInput.Type type) throws Exception;

    Issue create(IssueInputParameters params) throws Exception;

    IssueType updateIssueType(IssueType issueType, String name, String description, Long avatarId) throws Exception;

    FieldConfigScheme createIssueTypeScheme(String schemeName, String schemeDescription, List<String> optionIDs);

    Boolean deleteIssueType(String typeId);

    Issue update(String userName, Long issueId, IssueInputParameters params) throws Exception;

    Boolean delete(Long issueId);

    IssueInputParameters newInputParameters();

}
