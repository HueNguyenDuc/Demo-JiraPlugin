package com.cmcglobal.Utils;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.customfield.CreateValidationResult;
import com.atlassian.jira.bc.customfield.CustomFieldDefinition;
import com.atlassian.jira.bc.customfield.CustomFieldService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.managedconfiguration.ConfigurationItemAccessLevel;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemBuilder;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import org.ofbiz.core.entity.GenericEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ExportAsService({ICustomFieldUtils.class})
@Named
public class CustomFieldUtils implements ICustomFieldUtils {
    private static final Logger log = LoggerFactory.getLogger(CustomFieldUtils.class);

    private CustomFieldService _customFieldService;
    private CustomFieldManager _customFieldManager;
    private ProjectManager _projectManager;
    private OptionsManager _optionsManager;
    private FieldManager _fieldManager;
    private UserManager _userManager;
    private ApplicationUser _user;

    public CustomFieldUtils() {
        _customFieldService = ComponentAccessor.getComponent(CustomFieldService.class);
        _customFieldManager = ComponentAccessor.getCustomFieldManager();
        _projectManager = ComponentAccessor.getProjectManager();
        _fieldManager = ComponentAccessor.getFieldManager();
        _userManager = ComponentAccessor.getUserManager();
        _optionsManager = ComponentAccessor.getOptionsManager();
        _user = _userManager.getUserByName(UtilConstaints.USERNAME);
    }

    @Override
    public CustomField create(String name,
                              String description,
                              CustomFieldType<?,?> customFieldType,
                              List<String> optionsForMultiSelect,
                              List<Long> projectId,
                              Boolean isGlobal) throws Exception {
        if(name == null ||
                customFieldType == null)
            throw new Exception(UtilConstaints.ERROR_PARAMINPUTINVALID);
        Collection<CustomField> existsFields = _customFieldManager.getCustomFieldObjectsByName(name);
        if(existsFields!=null && existsFields.size() > 0)
            return existsFields.stream().findFirst().get();
        CustomFieldDefinition.Builder newCustomFieldBuilder = CustomFieldDefinition.builder();
        newCustomFieldBuilder
                .name(name)
                .isGlobal(isGlobal)
                .defaultSearcher()
                .isAllIssueTypes(true)
                .cfType(customFieldType.getKey());
        if(projectId!=null)
            projectId.forEach(newCustomFieldBuilder::addProjectId);
        if(description != null)
            newCustomFieldBuilder.description(description);
        CustomFieldDefinition newCustomField = newCustomFieldBuilder.build();
        ServiceOutcome<CreateValidationResult> validationCreate = _customFieldService.validateCreate(_user, newCustomField);
        if(!validationCreate.isValid()) {
            log.error(UtilConstaints.ERROR_CUSTOMFIELD_CREATEVALIDATEFAIL);
            throw new Exception(UtilConstaints.ERROR_CUSTOMFIELD_CREATEVALIDATEFAIL);
        }
        ServiceOutcome<CustomField> result = _customFieldService.create(validationCreate.get());
        if(!result.isValid()) {
            log.error(UtilConstaints.ERROR_CUSTOMFIELD_CREATEFAIL);
            throw new Exception(UtilConstaints.ERROR_CUSTOMFIELD_CREATEFAIL);
        }
        CustomField ret = result.get();
        if((customFieldType.getKey().contains(UtilConstaints.MULTICHECKBOX_KEY) || customFieldType.getKey().contains(UtilConstaints.RADIOBUTTONS_KEY)) && optionsForMultiSelect != null){
            createOption(ret,optionsForMultiSelect);
        }
        log.info(UtilConstaints.CREATE_SUCCESS_FORMAT, "CustomField", ret.getId(), ret.getName());
        return ret;
    }

    @Override
    public CustomField update(Long customFieldId, String name, String description, CustomFieldSearcher searcher) throws Exception {
        if(customFieldId < 1 ||
                name == null)
            throw new Exception(UtilConstaints.ERROR_PARAMINPUTINVALID);
        CustomField oldField = _customFieldManager.getCustomFieldObject(customFieldId);
        if(name==null)
            name = oldField.getName();
        if(description==null)
            description = oldField.getDescription();
        _customFieldManager.updateCustomField(customFieldId, name, description, null);
        CustomField ret = _customFieldManager.getCustomFieldObject(customFieldId);
        log.info(UtilConstaints.UPDATE_SUCCESS_FORMAT, "CustomField", ret.getId(), ret.getName());
        return ret;
    }

    @Override
    public Boolean delete(Long customFieldId) {
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(_user);
        if (jiraServiceContext.getErrorCollection().hasAnyErrors())
            return false;
        CustomField removeItem = _customFieldManager.getCustomFieldObject(customFieldId);
        if (removeItem == null)
            return false;
        try {
            _customFieldManager.removeCustomField(removeItem);
        } catch (RemoveException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public List<CustomFieldType<?, ?>> getAllCustomFieldType() {
        return _customFieldManager == null ? null : _customFieldManager.getCustomFieldTypes();
    }

    @Override
    public Collection<NavigableField> getAllSystemField() {
        return _fieldManager.getNavigableFields();
    }

    @Override
    public Boolean lockCustomField(CustomField fieldToLock) {
        ManagedConfigurationItemService managedConfigurationItemService = ComponentAccessor.getComponent(ManagedConfigurationItemService.class);
        ManagedConfigurationItem managedField = managedConfigurationItemService.getManagedCustomField( fieldToLock );
        if ( managedField != null ) {
            ManagedConfigurationItemBuilder builder = ManagedConfigurationItemBuilder.builder( managedField );
            builder.setManaged( true );
            builder.setConfigurationItemAccessLevel( ConfigurationItemAccessLevel.LOCKED );
            managedField = builder.build();
            managedConfigurationItemService.updateManagedConfigurationItem( managedField );
            log.info("Lock CustomField: %s -- %s Successfully.", fieldToLock.getId(), fieldToLock.getName());
            return true;
        }
        return false;
    }

    @Override
    public CustomField getCustomFieldByName(String name) throws Exception {
        if(name == null)
            throw new Exception(UtilConstaints.ERROR_PARAMINPUTINVALID);
        Collection<CustomField> existsFields = _customFieldManager.getCustomFieldObjectsByName(name);
        if(existsFields != null && existsFields.size() > 0)
            return existsFields.stream().findFirst().get();
        return null;
    }

    private void createOption(CustomField customField, List<String> option) throws Exception {
        if(customField == null ||
                option == null)
            throw new Exception(UtilConstaints.ERROR_PARAMINPUTINVALID);
        FieldConfigSchemeManager fieldConfigSchemeManager =
                ComponentAccessor.getComponent(FieldConfigSchemeManager.class);
        List<FieldConfigScheme> schemes =
                fieldConfigSchemeManager.getConfigSchemesForField(customField);
        FieldConfigScheme fieldConfigScheme = schemes.get(0);
        FieldConfig config = fieldConfigScheme.getOneAndOnlyConfig();
        _optionsManager.createOptions(config, null, Long.valueOf(1), option);
    }

}
