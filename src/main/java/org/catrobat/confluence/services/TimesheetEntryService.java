package org.catrobat.confluence.services;



import com.atlassian.activeobjects.tx.Transactional;
import java.util.Date;

import org.catrobat.confluence.activeobjects.Category;
import org.catrobat.confluence.activeobjects.Project;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;

@Transactional
public interface TimesheetEntryService
{
	TimesheetEntry add(Timesheet sheet, Date begin, Date end, Category category,
					String description, int pause, Project project);
}