package org.catrobat.confluence.jobs;

import com.atlassian.quartz.jobs.AbstractJob;
import com.atlassian.user.User;
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
public class OutOfTimeVerificationJob extends AbstractJob {
    public void doExecute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        OutOfTimeVerificationJobDetail jobDetail = (OutOfTimeVerificationJobDetail) jobExecutionContext.getJobDetail();
        List<Timesheet> timesheetList = jobDetail.getTimesheetService().all();
        List<User> userList = jobDetail.getUserAccessor().getUsersWithConfluenceAccessAsList();

        for (User user : userList) {
            for (Timesheet timesheet : timesheetList) {
                if (timesheet.getUserKey().equals(jobDetail.getUserAccessor().
                        getUserByName(user.getName()).getKey().toString())) {
                    //if ((timesheet.getHours() - timesheet.getHoursDone) < 80) {
                        //MailQueueItem item = new ConfluenceMailQueueItem(emailTo, mailSubject, mailBody, MIME_TYPE_TEXT);
                        //jobDetail.getMailService().sendEmail(item);
                    //}
                }
            }
        }
        /*
        System.out.println("OutOfTimeVerificationJob: " + jobExecutionContext.getFireTime());
        System.out.println("OutOfTimeVerificationJob next: " + jobExecutionContext.getNextFireTime());
        */
    }

    private boolean dateIsOlderThanTwoWeeks(Date date) {
        DateTime twoWeeksAgo = new DateTime().minusWeeks(2);
        DateTime datetime = new DateTime(date);
        return (datetime.compareTo(twoWeeksAgo) < 0);
    }
}
