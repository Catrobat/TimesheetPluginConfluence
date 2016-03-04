package org.catrobat.confluence.jobs;

import org.catrobat.confluence.services.TimesheetEntryService;
import org.catrobat.confluence.services.TimesheetService;
import org.quartz.JobDetail;

/**
 * This class allows Spring dependencies to be injected into {@link ActivityVerificationJob}.
 * A bug in Confluence's auto-wiring prevents  Job components from being auto-wired.
 */
public class ActivityVerificationJobDetail extends JobDetail {
    private final TimesheetService ts;
    private final TimesheetEntryService te;

    public ActivityVerificationJobDetail(TimesheetService ts, TimesheetEntryService te) {
        super();
        this.ts = ts;
        this.te = te;

        setName(ActivityVerificationJobDetail.class.getSimpleName());
        setJobClass(ActivityVerificationJob.class);
    }

    public TimesheetService getTimesheetService() {
        return ts;
    }

    public TimesheetEntryService getTimesheetEntryService() {
        return te;
    }
}
