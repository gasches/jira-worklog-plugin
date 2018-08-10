package io.github.gasches.jiraworklog.storage;

import net.java.ao.Accessor;
import net.java.ao.Entity;
import net.java.ao.Mutator;
import net.java.ao.schema.Table;

import java.util.Date;

@Table("WORK_TRACK")
public interface WorkTrack extends Entity {
    String PROJECT_ID = "PROJECT_ID";
    String USER_ID = "USER_ID";
    String ISSUE_ID = "ISSUE_ID";
    String START_WORK_DATE = "START_WORK_DATE";

    @Accessor(PROJECT_ID)
    Long getProjectId();

    @Mutator(PROJECT_ID)
    void setProjectId(Long id);

    @Accessor(USER_ID)
    Long getUserId();

    @Mutator(USER_ID)
    void setUserId(Long id);

    @Accessor(ISSUE_ID)
    Long getIssueId();

    @Mutator(ISSUE_ID)
    void setIssueId(Long id);

    @Accessor(START_WORK_DATE)
    Date getStartWorkDate();

    @Mutator(START_WORK_DATE)
    void setStartWorkDate(Date startWorkDate);
}
