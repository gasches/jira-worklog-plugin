package io.github.gasches.jiraworklog.service;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogResultFactory;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class CustomWorkLogService {

    @JiraImport
    private final WorklogManager worklogManager;

    @JiraImport
    private final WorklogService worklogService;

    @Autowired
    public CustomWorkLogService(WorklogManager worklogManager, WorklogService worklogService) {
        this.worklogManager = worklogManager;
        this.worklogService = worklogService;
    }

    public Worklog createWorkLog(Issue issue, ApplicationUser user, String message, Date eventDate,
            Long timeSpentInSeconds) {
        Worklog worklog = newWorkLog(issue, user.getKey(), message, eventDate, timeSpentInSeconds);
        WorklogResult worklogResult = WorklogResultFactory.create(worklog, false);
        JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(user);
        return worklogService.createAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, true);
    }

    private Worklog newWorkLog(Issue issue, String userKey, String message, Date eventDate, Long timeSpentInSeconds) {
        return new WorklogImpl(worklogManager, issue, null, userKey, message, eventDate, null, null, timeSpentInSeconds,
                null, null, null);
    }
}
