package org.catrobat.confluence.jobs;

import com.atlassian.quartz.jobs.AbstractJob;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;
import org.joda.time.DateTime;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;
import java.util.List;


/**
 * Job for doing something on a regular basis.
 */
public class ActivityVerificationJob extends AbstractJob {
    public void doExecute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        ActivityVerificationJobDetail jobDetail = (ActivityVerificationJobDetail) jobExecutionContext.getJobDetail();
        List<Timesheet> timesheetList = jobDetail.getTimesheetService().all();

        for (Timesheet timesheet : timesheetList) {
            if (timesheet.getEntries().length != 0) {
                TimesheetEntry[] entries = jobDetail.getTimesheetEntryService().getEntriesBySheet(timesheet);
                if (dateIsOlderThanTwoWeeks(entries[0].getBeginDate()) ||
                        dateIsOlderThanTwoWeeks(entries[0].getEndDate())) {
                    timesheet.setIsActive(false);
                    timesheet.save();
                } else {
                    //latest entry is not older than 2 weeks
                    timesheet.setIsActive(true);
                    timesheet.save();
                }
            } else {
                //no entry available
                timesheet.setIsActive(false);
                timesheet.save();
            }
        }

        for(Timesheet abc : jobDetail.getTimesheetService().all()){
            System.out.println("State: " + abc.getIsActive());
        }

        System.out.println("ActivityVerificationJob: " + jobExecutionContext.getFireTime());
        System.out.println("ActivityVerificationJob next: " + jobExecutionContext.getNextFireTime());
    }

    private boolean dateIsOlderThanTwoWeeks(Date date) {
        DateTime twoWeeksAgo = new DateTime().minusWeeks(2);
        DateTime datetime = new DateTime(date);
        return (datetime.compareTo(twoWeeksAgo) < 0);
    }
}
