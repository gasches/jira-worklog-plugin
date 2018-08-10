package io.github.gasches.jiraworklog.listeners;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.google.common.collect.ImmutableList;
import io.github.gasches.jiraworklog.service.CustomWorkLogService;
import io.github.gasches.jiraworklog.storage.WorkTrackStorage;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class IssueProgressStatusListener implements InitializingBean, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(IssueProgressStatusListener.class);

    // TODO Customize
    private static final String MESSAGE = "Automated WorkLog";

    // TODO Customize
    private static final List<Long> STOP_EVENT_TYPE_IDS =
            ImmutableList.of(EventType.ISSUE_CLOSED_ID, EventType.ISSUE_RESOLVED_ID, EventType.ISSUE_WORKSTOPPED_ID);

    @JiraImport
    private final EventPublisher eventPublisher;

    private final CustomWorkLogService customWorkLogService;

    private final WorkTrackStorage workTrackStorage;

    @Autowired
    public IssueProgressStatusListener(EventPublisher eventPublisher, CustomWorkLogService customWorkLogService,
            WorkTrackStorage workTrackStorage) {
        this.eventPublisher = eventPublisher;
        this.customWorkLogService = customWorkLogService;
        this.workTrackStorage = workTrackStorage;
    }

    @Override
    public void afterPropertiesSet() {
        eventPublisher.register(this);
    }

    @Override
    public void destroy() {
        eventPublisher.unregister(this);
    }

    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {
        logger.debug("Get event: {}", issueEvent.getEventTypeId());

        Issue issue = issueEvent.getIssue();
        ApplicationUser user = issue.getAssignee();
        Date eventTime = issueEvent.getTime();

        logger.debug("Issue: {}, user: {}, eventTime: {}", issue, user, eventTime);

        if (user == null) {
            logger.warn("No assignee for issue: " + issue.getKey());
        }

        Long eventTypeId = issueEvent.getEventTypeId();
        if (EventType.ISSUE_WORKSTARTED_ID.equals(eventTypeId)) {
            logger.debug("Work started for " + issue);
            workTrackStorage.store(issue, user, eventTime);
        } else if (STOP_EVENT_TYPE_IDS.contains(eventTypeId)) {
            logger.debug("Work stopped for " + issue);
            Optional<Date> startWorkDate = workTrackStorage.getStartWorkDate(issue, user);
            if (startWorkDate.isPresent()) {
                DateTime startDate = new DateTime(startWorkDate.get());
                DateTime eventDate = new DateTime(eventTime);
                long diff = Seconds.secondsBetween(startDate, eventDate).getSeconds();

                logger.debug("Create worklog for {}, timeSpent - {}", issue, diff);
                customWorkLogService.createWorkLog(issue, user, MESSAGE, eventTime, diff);
                workTrackStorage.remove(issue, user);
            }
        }
    }
}
