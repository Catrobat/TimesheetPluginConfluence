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

    public SampleJobDetail(CategoryService categoryService, ActiveObjects ao)
    //public SampleJobDetail()
    {
        super();
        this.ao = ao;
        this.categoryService = categoryService;

        setName(SampleJobDetail.class.getSimpleName());
        setJobClass(SampleJob.class);
    }

    public CategoryService getCategoryService() {
        return categoryService;
    }
}
