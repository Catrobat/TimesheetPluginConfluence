package org.catrobat.confluence.jobs;

import com.atlassian.quartz.jobs.AbstractJob;
import com.atlassian.user.User;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.joda.time.DateTime;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;
import java.util.List;

/**
 * Job for doing something on a regular basis.
 */
public class ActivityNotificationJob extends AbstractJob {
    private String emailTo = "";
    private String mailSubject = "";
    private String mailBody = "";

    public void doExecute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        ActivityNotificationJobDetail jobDetail = (ActivityNotificationJobDetail) jobExecutionContext.getJobDetail();
        List<Timesheet> timesheetList = jobDetail.getTimesheetService().all();
        List<User> userList = jobDetail.getUserAccessor().getUsersWithConfluenceAccessAsList();

        for (User user : userList) {
            for (Timesheet timesheet : timesheetList) {
                if (timesheet.getUserKey().equals(jobDetail.getUserAccessor().
                        getUserByName(user.getName()).getKey().toString())) {
                    if (!timesheet.getIsActive()) {
                        //email to admin + coordinators
                        //MailQueueItem item = new ConfluenceMailQueueItem(emailTo, mailSubject, mailBody, MIME_TYPE_TEXT);
                        //jobDetail.getMailService().sendEmail(item);
                    } else if (dateIsOlderThanTwoMonths(jobDetail.getTimesheetEntryService().getEntriesBySheet(timesheet)[0].getBeginDate())) {
                        //email to admin after 2 monts
                        //MailQueueItem item = new ConfluenceMailQueueItem(emailTo, mailSubject, mailBody, MIME_TYPE_TEXT);
                        //jobDetail.getMailService().sendEmail(item);
                    }
                }
            }
        }
        System.out.println("ActivityNotificationJob: " + jobExecutionContext.getFireTime());
        System.out.println("ActivityNotificationJob next: " + jobExecutionContext.getNextFireTime());
    }

    private boolean dateIsOlderThanTwoMonths(Date date) {
        DateTime twoMonthsAgo = new DateTime().minusMonths(2);
        DateTime datetime = new DateTime(date);
        return (datetime.compareTo(twoMonthsAgo) < 0);
    }
}
