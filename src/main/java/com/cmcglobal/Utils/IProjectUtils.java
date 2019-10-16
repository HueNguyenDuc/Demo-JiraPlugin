package com.cmcglobal.Utils;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.type.ProjectType;
import com.atlassian.jira.project.type.ProjectTypeKey;
import com.atlassian.jira.user.ApplicationUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface IProjectUtils {
    Project create(String name,
                   String userNameOfLead,
                   String key,
                   String description,
                   ProjectTypeKey projectTypeKey,
                   @Nullable Long assigneeTypeId,
                   @Nullable Long avatarId,
                   @Nullable String url);

    Boolean delete(String key);

    List<ProjectType> getAllProjectType();

    Project update(@Nonnull Project oldProject,
                   @Nullable String name,
                   @Nullable String userNameOfLead,
                   @Nullable String key,
                   @Nullable String description,
                   @Nullable String url,
                   @Nullable Long assigneeType);
}
