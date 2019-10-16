package com.cmcglobal.Utils;

import com.atlassian.jira.bc.project.ProjectCreationData;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.UpdateProjectParameters;
import com.atlassian.jira.project.template.ProjectTemplate;
import com.atlassian.jira.project.template.ProjectTemplateKey;
import com.atlassian.jira.project.template.ProjectTemplateManager;
import com.atlassian.jira.project.type.ProjectType;
import com.atlassian.jira.project.type.ProjectTypeKey;
import com.atlassian.jira.project.type.ProjectTypeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@ExportAsService({IProjectUtils.class})
@Named
public class ProjectUtils implements IProjectUtils {

    private ProjectManager _projectManager;
    private ProjectService _projectService;
    private UserManager _userManager;
    private ProjectTypeManager _projectTypeManager;
    private ApplicationUser _user;

    public ProjectUtils(){
        _projectManager = ComponentAccessor.getProjectManager();
        _projectService = ComponentAccessor.getComponent(ProjectService.class);
        _userManager = ComponentAccessor.getUserManager();
        _projectTypeManager = ComponentAccessor.getComponent(ProjectTypeManager.class);
        _user = _userManager.getUserByName(UtilConstaints.USERNAME);
    }

    @Override
    public Project create(String name, String userNameOfLead, String key, String description, ProjectTypeKey projectTypeKey, Long assigneeTypeId, Long avatarId, String url) {
        if(name == null ||
                key == null)
            return null;
        Project existsProject = _projectManager.getProjectObjByName(name);
        ApplicationUser user = _userManager.getUserByName(userNameOfLead);
        if(user == null)
            return null;
        if(existsProject != null)
            return existsProject;
        ProjectCreationData.Builder newProjectBuilder = new ProjectCreationData.Builder();
        newProjectBuilder.withName(name)
                .withLead(user)
                .withKey(key.toUpperCase())
                .withType(projectTypeKey);
        if(assigneeTypeId!=null)
            newProjectBuilder.withAssigneeType(assigneeTypeId);
        if(avatarId!=null)
            newProjectBuilder.withAvatarId(avatarId);
        if(url!=null)
            newProjectBuilder.withUrl(url);
        if(description!=null)
            newProjectBuilder.withDescription(description);
        ProjectCreationData newProject = newProjectBuilder.build();
        ProjectService.CreateProjectValidationResult validationResult = _projectService.validateCreateProject(_user, newProject);
        if(!validationResult.isValid())
            return null;
        return _projectService.createProject(validationResult);
    }

    @Override
    public Boolean delete(String key) {
        ProjectService.DeleteProjectValidationResult validationResult = _projectService.validateDeleteProject(_user, key);
        if(!validationResult.isValid())
            return null;
        ProjectService.DeleteProjectResult ret = _projectService.deleteProject(_user, validationResult);
        if(ret.isValid())
            return true;
        return false;
    }

    @Override
    public List<ProjectType> getAllProjectType() {
        return _projectTypeManager.getAllProjectTypes();
    }

    @Override
    public Project update(Project oldProject, String name, String userNameOfLead, String key, String description, String url, Long assigneeType) {
        if(oldProject==null)
            return null;
        ProjectService.UpdateProjectRequest updateRequest = new ProjectService.UpdateProjectRequest(oldProject);
        if(name!=null)
            updateRequest.name(name);
        if(userNameOfLead!= null) {
            ApplicationUser lead = _userManager.getUserByName(userNameOfLead);
            if(lead == null)
                return null;
            updateRequest.leadUserKey(lead.getKey());
        }
        if(key!=null)
            updateRequest.key(key);
        if(description!=null)
            updateRequest.description(description);
        if(url!=null)
            updateRequest.url(url);
        if(assigneeType!=null)
            updateRequest.assigneeType(assigneeType);
        ProjectService.UpdateProjectValidationResult validationResult = _projectService.validateUpdateProject(_user, updateRequest);
        if(!validationResult.isValid())
            return null;
        return _projectService.updateProject(validationResult);
    }
}
