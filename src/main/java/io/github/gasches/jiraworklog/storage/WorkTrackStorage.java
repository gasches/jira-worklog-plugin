package io.github.gasches.jiraworklog.storage;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Date;
import java.util.Optional;

@Transactional
public interface WorkTrackStorage {

    void store(Issue issue, ApplicationUser user, Date date);

    Optional<Date> getStartWorkDate(Issue issue, ApplicationUser user);

    void remove(Issue issue, ApplicationUser user);
}
