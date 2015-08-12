package org.catrobat.confluence.services.imp;

import com.atlassian.activeobjects.external.ActiveObjects;
import java.util.Date;
import javax.annotation.Nullable;

import com.atlassian.confluence.core.service.NotAuthorizedException;
import net.java.ao.Query;
import org.catrobat.confluence.activeobjects.Category;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;
import org.catrobat.confluence.services.TimesheetEntryService;

public class TimesheetEntryServiceImpl implements TimesheetEntryService {

	private final ActiveObjects ao;

	public TimesheetEntryServiceImpl(ActiveObjects ao) {
		this.ao = ao;
	}

	@Override
	public TimesheetEntry add(Timesheet sheet, Date begin, Date end, 
					Category category, String description, int pause, Team team) {
		
		TimesheetEntry entry = ao.create(TimesheetEntry.class);

		entry.setTimeSheet(sheet);
		entry.setBeginDate(begin);
		entry.setEndDate(end);
		entry.setCategory(category);
		entry.setDescription(description);
		entry.setPauseMinutes(pause);
		entry.setTeam(team);

		entry.save();

		return entry;
		
	}

  @Override
  @Nullable
  public TimesheetEntry getEntryByID(int entryID) {
    TimesheetEntry[] found = ao.find(TimesheetEntry.class, "ID = ?", entryID);

		if (found.length > 1) {
			throw new NotAuthorizedException("Multiple Timesheet Entries with the same ID.");
		}

		return (found.length > 0)? found[0] : null;
  }
  
  @Override
  @Nullable
	public TimesheetEntry edit(int entryId, Timesheet sheet, Date begin, Date end,
					Category category, String description, int pause, Team team) {
		
		TimesheetEntry entry = getEntryByID(entryId);
    
    if(entry == null) {
      return null;
    }

		entry.setTimeSheet(sheet);
		entry.setBeginDate(begin);
		entry.setEndDate(end);
		entry.setCategory(category);
		entry.setDescription(description);
		entry.setPauseMinutes(pause);
		entry.setTeam(team);
		entry.save();

		return entry;
		
	}

  @Override
  public TimesheetEntry[] getEntriesBySheet(Timesheet sheet) {
    if(sheet == null) return new TimesheetEntry[0];
    return ao.find(
        TimesheetEntry.class, 
        Query.select()
            .where("TIME_SHEET_ID = ?", sheet.getID())
            .order("BEGIN_DATE DESC")
    );
  }

  @Override
  public void delete(TimesheetEntry entry) {
    ao.delete(entry);
  }
  
}
