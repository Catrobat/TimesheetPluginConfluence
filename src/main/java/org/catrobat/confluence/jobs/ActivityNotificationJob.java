package org.catrobat.confluence.jobs;

import com.atlassian.quartz.jobs.AbstractJob;
import com.atlassian.user.User;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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
                    System.out.println("OWNER " + user.getFullName());
                    System.out.println("TS STATE " + timesheet.getIsActive());
                    System.out.println("--");
                    if (!timesheet.getIsActive()) {
                        //MailQueueItem item = new ConfluenceMailQueueItem(emailTo, mailSubject, mailBody, MIME_TYPE_TEXT);
                        //jobDetail.getMailService().sendEmail(item);
                    }
                }
            }
        }
        System.out.println("ActivityNotificationJob: " + jobExecutionContext.getFireTime());
        System.out.println("ActivityNotificationJob next: " + jobExecutionContext.getNextFireTime());
    }
}
