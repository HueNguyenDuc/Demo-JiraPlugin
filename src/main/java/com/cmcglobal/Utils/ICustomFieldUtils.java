package com.cmcglobal.Utils;

import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.issuetype.IssueType;

import java.util.Collection;
import java.util.List;

public interface ICustomFieldUtils {
    CustomField create(String name,
                       String description,
                       CustomFieldType<?,?> customFieldType,
                       List<String> optionsForMultiSelect,
                       List<Long> projectId,
                       Boolean isGlobal);

    CustomField update(Long customFieldId, String name, String description, CustomFieldSearcher searcher);

    Boolean delete(Long customFieldId);

    List<CustomFieldType<?, ?>> getAllCustomFieldType();

    Collection<NavigableField> getAllSystemField();

    Boolean lockCustomField(CustomField fieldToLock);
}
