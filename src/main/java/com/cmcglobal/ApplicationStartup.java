package com.cmcglobal;


import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.screen.*;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.cmcglobal.Utils.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    public void destroy() {
        // remove custom field

    }


    @Override
    public void afterPropertiesSet() {
        // create custome fields
        //importWorkflow();
        try {
            Init();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void Init() throws Exception {

        //get workflow
        JiraWorkflow workflow = _iWorkflowUtils.getWorkflowByName("Global Ssc Wf Ver5");

        //CustomField
        ArrayList<String> listOfSystemField = new ArrayList<>();
        List<CustomFieldType<?, ?>> customFieldTypes = _iCustomFieldUtils.getAllCustomFieldType();

        //create custom field
        CustomField involvedMenbers = _iCustomFieldUtils.create(
                "Involved Members",
                "description of Involved Members",
                customFieldTypes.get(18),
                null,
                null,
                true);
        _iCustomFieldUtils.lockCustomField(involvedMenbers);

        listOfSystemField.add(IssueFieldConstants.ISSUE_TYPE);
        listOfSystemField.add(IssueFieldConstants.SUMMARY);
        listOfSystemField.add(IssueFieldConstants.DESCRIPTION);
        listOfSystemField.add(IssueFieldConstants.ATTACHMENT);
        listOfSystemField.add(IssueFieldConstants.PRIORITY);
        listOfSystemField.add(IssueFieldConstants.DUE_DATE);
        listOfSystemField.add(IssueFieldConstants.SECURITY);
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
        Project newProject = _iProjectUtils.create(
                "FIS GR Project",
                "admin",
                "FISGR",
                "Description of FIS GR Project",
                _iProjectUtils.getAllProjectType().get(0).getKey(),
                null,
                null,
                null);
        _iScreenUtils.addIssueTypeScreenToProject(issueTypeScreenScheme, newProject);

        //create Issue Type scheme
        IssueType fisGenRequest = _iIssueUtils.createIssueType("FIS General Request", "Description of FIS General Request", null, _iIssueUtils.STANDARD());
        ArrayList<String> listOfType = new ArrayList<>();
        _iIssueUtils.getAllIssueType().forEach(e -> listOfType.add(e.getId()));
        FieldConfigScheme projectScheme = _iIssueUtils.createIssueTypeScheme("FIS Scheme of project", "Description of FIS", listOfType);
        _iIssueUtils.AddIssueTypeSchemeToProject(projectScheme, newProject);
        AssignableWorkflowScheme rzz = _iWorkflowUtils.createWorkflowScheme("Global Ssc Wf Ver5 Scheme", "Description Global Ssc Wf Ver5", workflow);
        _iWorkflowUtils.addWorkflowSchemeToProject(rzz, newProject);
    }

    private JiraWorkflow importWorkflow() throws Exception
    {
        return _iWorkflowUtils.ImportFromXMLFile("/Workflow/GLobal SSC WF ver5.xml", "GLobal SSC WF");
    }

}