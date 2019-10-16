package com.cmcglobal.Utils;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.IssueTypeService;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.*;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.ProjectContext;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@ExportAsService({IIssueUtils.class})
@Named
public class IssueUtils implements IIssueUtils {

    private IssueTypeService _issueTypeService;
    private IssueService _issueService;
    private IssueManager _issueManager;
    private IssueTypeSchemeManager _issueTypeSchemeManager;
    private IssueTypeManager _issueTypeManager;
    private UserManager _userManager;
    private FieldConfigSchemeManager _fieldConfigSchemeManager;
    private ApplicationUser _user;

    public IssueUtils(){
        _issueTypeService = ComponentAccessor.getComponent(IssueTypeService.class);
        _issueManager = ComponentAccessor.getIssueManager();
        _issueService = ComponentAccessor.getIssueService();
        _issueTypeSchemeManager = ComponentAccessor.getIssueTypeSchemeManager();
        _issueTypeManager = ComponentAccessor.getComponent(IssueTypeManager.class);
        _userManager = ComponentAccessor.getUserManager();
        _fieldConfigSchemeManager = ComponentAccessor.getFieldConfigSchemeManager();
        _user = _userManager.getUserByName(UtilConstaints.USERNAME);
    }

    @Override
    public IssueTypeService.IssueTypeCreateInput.Type STANDARD() {
        return IssueTypeService.IssueTypeCreateInput.Type.STANDARD;
    }

    @Override
    public IssueTypeService.IssueTypeCreateInput.Type SUBTASK() {
        return IssueTypeService.IssueTypeCreateInput.Type.SUBTASK;
    }

    @Override
    public Collection<ScreenableIssueOperation> getAllOperation() {
        return IssueOperations.getIssueOperations();
    }

    @Override
    public Collection<IssueType> getAllIssueType() {
        return _issueTypeManager.getIssueTypes();
    }

    @Override
    public FieldConfigScheme AddIssueTypeSchemeToProject(FieldConfigScheme scheme ,Project project) {
        //_issueTypeSchemeManager.addOptionToDefault();
        JiraContextNode projectContext = new ProjectContext(project.getId());
        List<JiraContextNode> existsContextNode = scheme.getContexts();
        List<JiraContextNode> newContextNode = new ArrayList<>();
        Stream<JiraContextNode> sContextNode = existsContextNode.stream().filter(e -> e.getProjectId().equals(project.getId()));
        if(sContextNode != null && sContextNode.count() > 0)
            return scheme;
        newContextNode.addAll(existsContextNode);
        newContextNode.add(projectContext);
        FieldConfigScheme ret =  _fieldConfigSchemeManager.updateFieldConfigScheme(scheme, newContextNode, scheme.getField());
        return ret;
    }

    @Override
    public IssueType createIssueType(String name, String description, Long avatarId, IssueTypeService.IssueTypeCreateInput.Type type) {
        if(name == null)
            return null;
        Stream<IssueType> existsTypes = _issueTypeManager.getIssueTypes().stream().filter(e->e.getName().equals(name));
        if(existsTypes != null && existsTypes.count() > 0)
            return _issueTypeManager.getIssueTypes().stream().filter(e->e.getName().equals(name)).findFirst().get();
        IssueTypeService.IssueTypeCreateInput.Builder newTypeBuilder = IssueTypeService.IssueTypeCreateInput.builder();
        newTypeBuilder.setName(name);
        if (type != null)
            newTypeBuilder.setType(type);
        if (description != null)
            newTypeBuilder.setDescription(description);
        IssueTypeService.CreateValidationResult validationResult = _issueTypeService.validateCreateIssueType(_user, newTypeBuilder.build());
        if (!validationResult.isValid())
            return null;
        IssueTypeService.IssueTypeResult result = _issueTypeService.createIssueType(_user, validationResult);
        if (result == null)
            return null;
        return result.getIssueType();
    }

    @Override
    public Issue create(String userName, IssueInputParameters params) {
        if(params==null)
            return null;
        IssueService.CreateValidationResult validationResult = _issueService.validateCreate(_user, params);
        if(!validationResult.isValid())
            return null;
        IssueService.IssueResult result = _issueService.create(_user, validationResult);
        return result.isValid() ? result.getIssue() : null;
    }

    @Override
    public IssueType updateIssueType(IssueType issueType, String name, String description, Long avatarId) {
        IssueTypeService.IssueTypeUpdateInput.Builder updateBuilder = IssueTypeService.IssueTypeUpdateInput.builder();
        if(name!=null)
            updateBuilder.setName(name);
        if(description!=null)
            updateBuilder.setDescription(description);
        if(avatarId!=null)
            updateBuilder.setAvatarId(avatarId);
        updateBuilder.setIssueTypeToUpdateId(Long.parseLong(issueType.getId()));
        IssueTypeService.UpdateValidationResult validationResult = _issueTypeService.validateUpdateIssueType(_user, issueType.getId(), updateBuilder.build());
        if(!validationResult.isValid())
            return null;
        IssueTypeService.IssueTypeResult result = _issueTypeService.updateIssueType(_user, validationResult);
        return result.getIssueType();
    }

    @Override
    public FieldConfigScheme createIssueTypeScheme(String schemeName, String schemeDescription, List<String> optionIDs) {
        Stream<FieldConfigScheme> existsScheme = _issueTypeSchemeManager.getAllSchemes().stream().filter(e -> e.getName().equals(schemeName));
        if(existsScheme != null && existsScheme.count() > 0)
            return _issueTypeSchemeManager.getAllSchemes().stream().filter(e -> e.getName().equals(schemeName)).findFirst().get();
        FieldConfigScheme result = _issueTypeSchemeManager.create(schemeName, schemeDescription, optionIDs);
        return result;
    }

    @Override
    public Boolean deleteIssueType(String typeId) {
        IssueTypeService.IssueTypeDeleteInput deleteParam = new IssueTypeService.IssueTypeDeleteInput(typeId, null);
        IssueTypeService.DeleteValidationResult validationResult = _issueTypeService.validateDeleteIssueType(_user, deleteParam);
        if(!validationResult.isValid())
            return false;
        _issueTypeService.deleteIssueType(_user, validationResult);
        return true;
    }

    @Override
    public Issue update(String userName, Long issueId, IssueInputParameters params) {
        if(params == null)
            return null;
        IssueService.UpdateValidationResult validationResult = _issueService.validateUpdate(_user, issueId, params);
        if(!validationResult.isValid())
            return null;
        IssueService.IssueResult result = _issueService.update(_user, validationResult);
        return result.isValid() ? result.getIssue() : null;
    }

    @Override
    public Boolean delete(Long issueId) {
        IssueService.DeleteValidationResult validationResult = _issueService.validateDelete(_user, issueId);
        if(!validationResult.isValid())
            return false;
        ErrorCollection result = _issueService.delete(_user, validationResult);
        if(result.hasAnyErrors())
            return false;
        return true;
    }

    @Override
    public IssueInputParameters newInputParameters() {
        return _issueService.newIssueInputParameters();
    }

}
