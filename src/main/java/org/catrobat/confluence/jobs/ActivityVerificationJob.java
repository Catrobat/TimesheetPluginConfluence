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
public class ActivityVerificationJob extends AbstractJob {
    public void doExecute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        ActivityVerificationJobDetail jobDetail = (ActivityVerificationJobDetail) jobExecutionContext.getJobDetail();
        List<Timesheet> timesheetList = jobDetail.getTimesheetService().all();
        List<User> userList = jobDetail.getUserAccessor().getUsersWithConfluenceAccessAsList();

        for(User user : userList) {
            for (Timesheet timesheet : timesheetList) {
                if (timesheet.getUserKey().equals(jobDetail.getUserAccessor().
                        getUserByName(user.getName()).getKey().toString())) {
                    System.out.println("Owner " + user.getFullName() + " State " + timesheet.getIsActive());
                }
            }
        }
        System.out.println("firetime: " + jobExecutionContext.getFireTime());
        System.out.println("next firetime: " + jobExecutionContext.getNextFireTime());
    }
}
