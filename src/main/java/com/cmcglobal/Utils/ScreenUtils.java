package com.cmcglobal.Utils;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.screen.*;
import com.atlassian.jira.issue.fields.screen.issuetype.*;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import org.apache.commons.compress.archivers.zip.X7875_NewUnix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.awt.AWTAccessor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@ExportAsService({IScreenUtils.class})
@Named
public class ScreenUtils implements IScreenUtils {
    private static final Logger log = LoggerFactory.getLogger(ScreenUtils.class);

    private FieldScreenManager _fieldScreenManager;
    private FieldScreenSchemeManager _fieldScreenSchemeManager;
    private IssueTypeScreenSchemeManager _issueTypeScreenSchemeManager;
    private ConstantsManager _constantsManager;
    private UserManager _userManager;
    private ApplicationUser _user;

    public ScreenUtils() {
        _fieldScreenManager = ComponentAccessor.getFieldScreenManager();
        _fieldScreenSchemeManager = ComponentAccessor.getComponent(FieldScreenSchemeManager.class);
        _issueTypeScreenSchemeManager = ComponentAccessor.getIssueTypeScreenSchemeManager();
        _constantsManager = ComponentAccessor.getConstantsManager();
        _userManager = ComponentAccessor.getUserManager();
        _user = _userManager.getUserByName(UtilConstaints.USERNAME);
    }

    public List<FieldScreenTab> getAllScreenTab() {
        ArrayList<FieldScreenTab> tabs = new ArrayList<FieldScreenTab>();
        _fieldScreenManager.getFieldScreens().forEach(e->{
            tabs.addAll(_fieldScreenManager.getFieldScreenTabs(e));
        });
        return tabs;
    }

    @Override
    public FieldScreenTab createScreenTab(FieldScreen screen, String name, List<String> listOfField) throws Exception {
        if (name == null ||
                screen==null)
            throw new Exception(UtilConstaints.ERROR_PARAMINPUTINVALID);
        Stream<FieldScreenTab> existsScreenTabs = _fieldScreenManager.getFieldScreenTabs(screen).stream().filter(e -> e.getName().equals(name));
        if(existsScreenTabs != null && existsScreenTabs.count() > 0)
            return _fieldScreenManager.getFieldScreenTabs(screen).stream().filter(e -> e.getName().equals(name)).findFirst().get();
        FieldScreenTab newTab = new FieldScreenTabImpl(_fieldScreenManager);
        newTab.setFieldScreen(screen);
        newTab.setName(name);
        _fieldScreenManager.createFieldScreenTab(newTab);
        if(listOfField != null && listOfField.size() > 0)
            listOfField.forEach(e -> addFieldToScreenTab(newTab, e));
        log.info("Create SreenTab: %d -- %s For Screen: %d -- %s Successful.", newTab.getId(), newTab.getName(), screen.getId(), screen.getName());
        return newTab;
    }

    @Override
    public FieldScreenSchemeItem createSchemeItem(FieldScreen screen, FieldScreenScheme scheme, ScreenableIssueOperation operation) throws Exception {
        if(scheme==null||
                scheme==null||
                operation==null)
            throw new Exception(UtilConstaints.ERROR_PARAMINPUTINVALID);
        FieldScreenSchemeItem newSchemeItem = new FieldScreenSchemeItemImpl(_fieldScreenSchemeManager, _fieldScreenManager);
        newSchemeItem.setFieldScreen(screen);
        if (scheme != null)
            newSchemeItem.setFieldScreenScheme(scheme);
        if (operation != null)
            newSchemeItem.setIssueOperation(operation);
        _fieldScreenSchemeManager.createFieldScreenSchemeItem(newSchemeItem);
        log.info("Create Scheme Item: %d For \n Screen: %d -- %s\n Screen Scheme: %d -- %s \n With Operation: %s",
                newSchemeItem.getId(),
                screen.getId(),
                screen.getName(),
                scheme.getId(),
                scheme.getName(),
                operation.getNameKey()
        );
        return newSchemeItem;
    }

    @Override
    public FieldScreenScheme createScreenScheme(String name, String description) throws Exception {
        if(name == null)
            throw new Exception(UtilConstaints.ERROR_PARAMINPUTINVALID);
        Stream<FieldScreenScheme> existsSchemes = _fieldScreenSchemeManager.getFieldScreenSchemes().stream().filter(e -> e.getName().equals(name));
        if(existsSchemes!= null && existsSchemes.count() > 0)
            return _fieldScreenSchemeManager.getFieldScreenSchemes().stream().filter(e -> e.getName().equals(name)).findFirst().get();
        FieldScreenScheme newScheme = new FieldScreenSchemeImpl(_fieldScreenSchemeManager);
        newScheme.setName(name);
        if(description!=null)
            newScheme.setDescription(description);
        _fieldScreenSchemeManager.createFieldScreenScheme(newScheme);
        log.info(UtilConstaints.CREATE_SUCCESS_FORMAT, "Scheme", newScheme.getId().toString(), newScheme.getName());
        return newScheme;
    }

    @Override
    public FieldScreen createFieldScreen(String name, String description) throws Exception {
        if(name==null)
            throw new Exception(UtilConstaints.ERROR_PARAMINPUTINVALID);
        Stream<FieldScreen> existsScreen = _fieldScreenManager.getFieldScreens().stream().filter(e -> e.getName().equals(name));
        if(existsScreen!=null && existsScreen.count() > 0)
            return _fieldScreenManager.getFieldScreens().stream().filter(e -> e.getName().equals(name)).findFirst().get();
        FieldScreen newScreen = new FieldScreenImpl(_fieldScreenManager);
        newScreen.setName(name);
        if(description!=null)
            newScreen.setDescription(description);
        _fieldScreenManager.createFieldScreen(newScreen);
        log.info(UtilConstaints.CREATE_SUCCESS_FORMAT, "Screen", newScreen.getId().toString(), newScreen.getName());
        return newScreen;
    }

    @Override
    public IssueTypeScreenScheme createIssueScreenScheme(String name, String description) throws Exception {
        if(name==null)
            throw new Exception(UtilConstaints.ERROR_PARAMINPUTINVALID);
        Stream<IssueTypeScreenScheme> existsTypeScreen = _issueTypeScreenSchemeManager.getIssueTypeScreenSchemes().stream().filter(e -> e.getName().equals(name));
        if(existsTypeScreen!=null &&existsTypeScreen.count() > 0)
            return _issueTypeScreenSchemeManager.getIssueTypeScreenSchemes().stream().filter(e -> e.getName().equals(name)).findFirst().get();
        IssueTypeScreenScheme newTypeScheme = new IssueTypeScreenSchemeImpl(_issueTypeScreenSchemeManager);
        newTypeScheme.setName(name);
        if(description!=null)
            newTypeScheme.setDescription(description);
        _issueTypeScreenSchemeManager.createIssueTypeScreenScheme(newTypeScheme);
        log.info(UtilConstaints.CREATE_SUCCESS_FORMAT, "Screen Scheme", newTypeScheme.getId().toString(), newTypeScheme.getName());
        return newTypeScheme;
    }

    @Override
    public IssueTypeScreenSchemeEntity createIssueScreenSchemeEntity(IssueTypeScreenScheme issueTypeScreenScheme, FieldScreenScheme screenScheme, String issueTypeId) {
        Collection<IssueTypeScreenSchemeEntity> entities = issueTypeScreenScheme.getEntities();
        if(entities != null && entities.size() > 0){
            Stream<IssueTypeScreenSchemeEntity> sEntities = entities.stream().filter(e ->
                    e.getFieldScreenSchemeId().equals(screenScheme.getId()) &&
                            e.getIssueTypeScreenScheme().getId().equals(issueTypeScreenScheme.getId()));
            if(sEntities != null && sEntities.count() > 0)
                return entities.stream().filter(e ->
                        e.getFieldScreenSchemeId().equals(screenScheme.getId()) &&
                                e.getIssueTypeScreenScheme().getId().equals(issueTypeScreenScheme.getId())).findFirst().get();
        }
        IssueTypeScreenSchemeEntity schemeEntity = new IssueTypeScreenSchemeEntityImpl(_issueTypeScreenSchemeManager, _fieldScreenSchemeManager, _constantsManager);
        if(issueTypeId != null)
            schemeEntity.setIssueTypeId(issueTypeId);
        schemeEntity.setFieldScreenScheme(screenScheme);
        schemeEntity.setIssueTypeScreenScheme(issueTypeScreenScheme);
        _issueTypeScreenSchemeManager.createIssueTypeScreenSchemeEntity(schemeEntity);
        log.info(UtilConstaints.CREATE_SUCCESS_FORMAT, "Screen Scheme Entity", schemeEntity.getId().toString(), screenScheme.getName());
        return schemeEntity;
    }

    @Override
    public void addIssueTypeScreenToProject(IssueTypeScreenScheme scheme, Project project) {
        _issueTypeScreenSchemeManager.addSchemeAssociation(project, scheme);
    }

    @Override
    public FieldScreenLayoutItem addFieldToScreenTab(FieldScreenTab fieldScreenTab, String systemFieldId){
        Stream<FieldScreenLayoutItem> existsLayout = _fieldScreenManager.getFieldScreenLayoutItems(fieldScreenTab).stream().filter(e->e.getFieldId().equals(systemFieldId));
        if(existsLayout != null && existsLayout.count() > 0)
            return _fieldScreenManager.getFieldScreenLayoutItems(fieldScreenTab).stream().filter(e -> e.getFieldId().equals(systemFieldId)).findFirst().get();
        FieldScreenLayoutItem addLayout = _fieldScreenManager.buildNewFieldScreenLayoutItem(systemFieldId);
        addLayout.setFieldScreenTab(fieldScreenTab);
        _fieldScreenManager.createFieldScreenLayoutItem(addLayout);
        log.info(UtilConstaints.CREATE_SUCCESS_FORMAT, "Tab Layout", addLayout.getId().toString(), fieldScreenTab.getName());
        return addLayout;
    }
}
