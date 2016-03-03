package org.catrobat.confluence.jobs;

import com.atlassian.activeobjects.external.ActiveObjects;
import org.catrobat.confluence.services.CategoryService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

;

/**
 * This class allows Spring dependencies to be injected into {@link SampleJob}.
 * A bug in Confluence's auto-wiring prevents  Job components from being auto-wired.
 */
public class SampleJobDetail extends JobDetail {
    private final CategoryService categoryService;
    private final ActiveObjects ao;
    private final JobDataMap jobDataMap = new JobDataMap();

    public SampleJobDetail(CategoryService categoryService, ActiveObjects ao)
    //public SampleJobDetail()
    {
        super();
        this.ao = ao;
        this.categoryService = categoryService;

        setName(SampleJobDetail.class.getSimpleName());
        setJobClass(SampleJob.class);

        //System.out.println(categoryService.all());
        /*
        for(Category category : categoryService.all()){
            jobDataMap.put("key"+category.getID(), category.getName());
        }*/

        /*
        for(int i = 0; i < 5; i++){
            jobDataMap.put("key"+i, "value"+i);
        }
        */
        //hand data to the job
        //setJobDataMap(jobDataMap);
    }

    public CategoryService getCategoryService() {
        return categoryService;
    }
}
