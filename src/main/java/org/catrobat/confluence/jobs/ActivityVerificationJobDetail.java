package org.catrobat.confluence.jobs;

import com.atlassian.confluence.user.UserAccessor;
import org.catrobat.confluence.services.TimesheetService;
import org.quartz.JobDetail;

/**
 * This class allows Spring dependencies to be injected into {@link ActivityVerificationJob}.
 * A bug in Confluence's auto-wiring prevents  Job components from being auto-wired.
 */
public class ActivityVerificationJobDetail extends JobDetail {
    private final TimesheetService ts;
    private final UserAccessor ua;

    public ActivityVerificationJobDetail(TimesheetService ts, UserAccessor ua) {
        super();
        this.ts = ts;
        this.ua = ua;

        setName(ActivityVerificationJobDetail.class.getSimpleName());
        setJobClass(ActivityVerificationJob.class);
    }

    public UserAccessor getUserAccessor() {
        return ua;
    }

    public TimesheetService getTimesheetService() {
        return ts;
    }

}
