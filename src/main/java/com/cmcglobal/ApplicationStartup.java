package com.cmcglobal;

import com.atlassian.jira.bc.project.ProjectCreationData;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.security.login.LoginResult;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.bc.user.ApplicationUserBuilder;
import com.atlassian.jira.bc.user.ApplicationUserBuilderImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.IssueTypeService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.screen.*;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.type.ProjectType;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.cmcglobal.Utils.*;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import com.atlassian.crowd.embedded.api.User.*;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Scanned
@ExportAsService({ApplicationStartup.class})
@Component("componentListener")
public class ApplicationStartup implements InitializingBean, DisposableBean
{
    final static Logger logger = Logger.getLogger(ApplicationStartup.class);
    private IIssueUtils _iIssueUtils;
    private ICustomFieldUtils _iCustomFieldUtils;
    private IProjectUtils _iProjectUtils;
    private IScreenUtils _iScreenUtils;
    private IWorkflowUtils _iWorkflowUtils;

    @Inject
    public ApplicationStartup(
            IIssueUtils iIssueUtils,
            ICustomFieldUtils iCustomFieldUtils,
            IProjectUtils iProjectUtils,
            IScreenUtils iScreenUtils,
            IWorkflowUtils iWorkflowUtils
    ) {
        _iIssueUtils = iIssueUtils;
        _iCustomFieldUtils = iCustomFieldUtils;
        _iProjectUtils = iProjectUtils;
        _iScreenUtils = iScreenUtils;
        _iWorkflowUtils = iWorkflowUtils;
    }

    @Override
    public void destroy() throws Exception {
        // remove custom field

    }


    @Override
    public void afterPropertiesSet() throws Exception {
        // create custome fields
        //importWorkflow();
        Init();
    }

    private void Init() throws Exception {
        //CustomField
        ArrayList<String> listOfSystemField = new ArrayList<>();
        Collection<NavigableField> systemFields = _iCustomFieldUtils.getAllSystemField();
        List<CustomFieldType<?, ?>> customFieldTypes = _iCustomFieldUtils.getAllCustomFieldType();

        //create custom field
        CustomField involvedMenbers = _iCustomFieldUtils.create(
                "Involved Members",
                "description of Involved Members",
                customFieldTypes.get(18),
                null,
                null,
                true);

        listOfSystemField.add(systemFields.stream().filter(e->e.getId().contains("issuetype")).findFirst().get().getId());
        listOfSystemField.add(systemFields.stream().filter(e->e.getId().contains("summary")).findFirst().get().getId());
        listOfSystemField.add(systemFields.stream().filter(e->e.getId().contains("description")).findFirst().get().getId());
        listOfSystemField.add(systemFields.stream().filter(e->e.getId().contains("components")).findFirst().get().getId());
        listOfSystemField.add(systemFields.stream().filter(e->e.getId().contains("priority")).findFirst().get().getId());
        listOfSystemField.add(systemFields.stream().filter(e->e.getId().contains("duedate")).findFirst().get().getId());
        listOfSystemField.add(systemFields.stream().filter(e->e.getId().contains("security")).findFirst().get().getId());
        listOfSystemField.add(involvedMenbers.getId());

        //create screen
        FieldScreen fisGRCreate = _iScreenUtils.createFieldScreen("FIS GE Create", "Create Action");
        FieldScreen fisGREdit = _iScreenUtils.createFieldScreen("FIS GE Edit", "Edit Action");
        FieldScreen fisGRView = _iScreenUtils.createFieldScreen("FIS GE View", "View Action");

        //create screen tab
        FieldScreenTab createTab = _iScreenUtils.createScreenTab(fisGRCreate, "Create Action", listOfSystemField);
        FieldScreenTab editTab = _iScreenUtils.createScreenTab(fisGREdit, "Edit Action", listOfSystemField);
        FieldScreenTab viewTab = _iScreenUtils.createScreenTab(fisGRView, "View Action", listOfSystemField);

        //create scheme && scheme items
        FieldScreenScheme scheme = _iScreenUtils.createScreenScheme("FIS Scheme", "FIS Scheme Description");
        Collection<ScreenableIssueOperation> operations =_iIssueUtils.getAllOperation();
        FieldScreenSchemeItem createSchemeItem = _iScreenUtils.createSchemeItem(fisGRCreate, scheme, operations.stream().filter(e->e.getNameKey().contains("create")).findFirst().get());
        FieldScreenSchemeItem editSchemeItem = _iScreenUtils.createSchemeItem(fisGREdit, scheme, operations.stream().filter(e->e.getNameKey().contains("edit")).findFirst().get());
        FieldScreenSchemeItem viewSchemeItem = _iScreenUtils.createSchemeItem(fisGRView, scheme, operations.stream().filter(e->e.getNameKey().contains("view")).findFirst().get());

        //Create Issue Type Screen Scheme
        IssueTypeScreenScheme issueTypeScreenScheme = _iScreenUtils.createIssueScreenScheme("FIS IssueType Screen Scheme", "Description of FIS IssueType Screen Scheme");
        IssueTypeScreenSchemeEntity issueScreenSchemeEntity = _iScreenUtils.createIssueScreenSchemeEntity(issueTypeScreenScheme, scheme, null);

        //create project and assign to issue scheme
        Project newProject = _iProjectUtils.create("FIS GR Project", "admin", "FISGR", "Description of FIS GR Project", _iProjectUtils.getAllProjectType().get(0).getKey(), null, null, null);
        _iScreenUtils.addIssueTypeScreenToProject(issueTypeScreenScheme, newProject);

        //create Issue Type scheme
        ArrayList<String> listOfType = new ArrayList<>();
        _iIssueUtils.getAllIssueType().forEach(e -> listOfType.add(e.getId()));
        FieldConfigScheme projectScheme = _iIssueUtils.createIssueTypeScheme("FIS Scheme of project", "Description of FIS", listOfType);
        _iIssueUtils.AddIssueTypeSchemeToProject(projectScheme, newProject);
        JiraWorkflow workflow = _iWorkflowUtils.getWorkflowByName("Global Ssc Wf Ver5");
        AssignableWorkflowScheme rzz = _iWorkflowUtils.createWorkflowScheme("Global Ssc Wf Ver5 Scheme", "Description Global Ssc Wf Ver5", workflow);
        _iWorkflowUtils.addWorkflowSchemeToProject(rzz, newProject);
    }

    private JiraWorkflow importWorkflow() throws Exception
    {
        return _iWorkflowUtils.ImportFromXMLFile("/Workflow/GLobal SSC WF ver5.xml", "GLobal SSC WF");
    }

}