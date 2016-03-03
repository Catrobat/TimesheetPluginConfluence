package org.catrobat.confluence.jobs;

import com.atlassian.quartz.jobs.AbstractJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Job for doing something on a regular basis.
 */
public class SampleJob extends AbstractJob {
    public void doExecute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SampleJobDetail jobDetail = (SampleJobDetail) jobExecutionContext.getJobDetail();
        System.out.println(jobDetail.getCategoryService().all());

        /*
        //execution time is defined in the atlassian-plugin.xml
        String[] keys = jobExecutionContext.getMergedJobDataMap().getKeys();
        for(int i = 0; i < keys.length; i++)
            System.out.println(jobExecutionContext.getMergedJobDataMap().getString(keys[i]));

        System.out.println("firetime: " + jobExecutionContext.getFireTime());
        System.out.println("next firetime: " + jobExecutionContext.getNextFireTime());
        */
    }
}
