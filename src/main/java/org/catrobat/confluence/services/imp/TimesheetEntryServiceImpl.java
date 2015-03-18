package org.catrobat.confluence.services.imp;

import com.atlassian.activeobjects.external.ActiveObjects;
import static com.google.common.collect.Lists.newArrayList;
import java.util.Date;
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
}
