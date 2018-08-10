package io.github.gasches.jiraworklog.storage;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@Service
public class WorkTrackStorageImpl implements WorkTrackStorage {
    private static final String FIND_BY_ID =
            String.format("%s = ? AND %s = ? AND %s = ?", WorkTrack.PROJECT_ID, WorkTrack.USER_ID, WorkTrack.ISSUE_ID);

    @JiraImport
    private final ActiveObjects ao;

    @Autowired
    public WorkTrackStorageImpl(ActiveObjects ao) {
        this.ao = ao;
    }

    @Override
    public void store(Issue issue, ApplicationUser user, Date date) {
        Optional<WorkTrack> wl = find(issue, user);
        if (!wl.isPresent()) {
            WorkTrack workTrack = ao.create(WorkTrack.class);
            workTrack.setProjectId(issue.getProjectId());
            workTrack.setUserId(user.getId());
            workTrack.setIssueId(issue.getId());
            workTrack.setStartWorkDate(date);
            workTrack.save();
        }
    }

    @Override
    public Optional<Date> getStartWorkDate(Issue issue, ApplicationUser user) {
        return find(issue, user).map(WorkTrack::getStartWorkDate);
    }

    @Override
    public void remove(Issue issue, ApplicationUser user) {
        find(issue, user).ifPresent(ao::delete);
    }

    private Optional<WorkTrack> find(Issue issue, ApplicationUser user) {
        WorkTrack[] workTracks = ao.find(WorkTrack.class,
                Query.select().where(FIND_BY_ID, issue.getProjectId(), user.getId(), issue.getId()));
        return Arrays.stream(workTracks).findFirst();
    }
}
