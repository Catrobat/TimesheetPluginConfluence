package org.catrobat.confluence.jobs;

import com.atlassian.quartz.jobs.AbstractJob;
import org.catrobat.confluence.activeobjects.Category;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * Job for doing something on a regular basis.
 */
public class SampleJob extends AbstractJob {
    public void doExecute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SampleJobDetail jobDetail = (SampleJobDetail) jobExecutionContext.getJobDetail();
        List<Category> categoryList = jobDetail.getCategoryService().all();
        for(Category category : categoryList)
            System.out.println("category ID: " + category.getID() + " category name: " +category.getName());

        System.out.println("firetime: " + jobExecutionContext.getFireTime());
        System.out.println("next firetime: " + jobExecutionContext.getNextFireTime());
    }
}
