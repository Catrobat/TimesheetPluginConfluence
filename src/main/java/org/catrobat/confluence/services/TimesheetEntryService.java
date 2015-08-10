package org.catrobat.confluence.services;



import com.atlassian.activeobjects.tx.Transactional;
import java.util.Date;

import org.catrobat.confluence.activeobjects.Category;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;

@Transactional
public interface TimesheetEntryService
{
	TimesheetEntry add(Timesheet sheet, Date begin, Date end, Category category,
					String description, int pause, Team team);
	
  TimesheetEntry edit(int entryID, Timesheet sheet, Date begin, Date end, 
      Category category, String description, int pause, Team team);
  
  TimesheetEntry getEntryByID(int entryID);
  
  TimesheetEntry[] getEntriesBySheet(Timesheet sheet);
  
  void delete(TimesheetEntry entry);
  
}