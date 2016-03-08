package org.catrobat.confluence.jobs;

import com.atlassian.confluence.user.UserAccessor;
import org.catrobat.confluence.services.MailService;
import org.catrobat.confluence.services.TimesheetEntryService;
import org.catrobat.confluence.services.TimesheetService;
import org.quartz.JobDetail;

/**
 * This class allows Spring dependencies to be injected into {@link ActivityVerificationJob}.
 * A bug in Confluence's auto-wiring prevents  Job components from being auto-wired.
 */
public class ActivityNotificationJobDetail extends JobDetail {
    private final TimesheetService ts;
    private final UserAccessor ua;
    private final MailService ms;
    private final TimesheetEntryService te;

    public ActivityNotificationJobDetail(TimesheetService ts, UserAccessor ua, MailService ms,
                                         TimesheetEntryService te) {
        super();
        this.ts = ts;
        this.ua = ua;
        this.ms = ms;
        this.te = te;

        setName(ActivityNotificationJobDetail.class.getSimpleName());
        setJobClass(ActivityNotificationJob.class);
    }

    public UserAccessor getUserAccessor() {
        return ua;
    }

    public TimesheetService getTimesheetService() {
        return ts;
    }

    public TimesheetEntryService getTimesheetEntryService() {
        return te;
    }

    public MailService getMailService() {
        return ms;
    }
}
