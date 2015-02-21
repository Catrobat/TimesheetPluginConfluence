package org.catrobat.confluence.services;

import org.catrobat.confluence.timesheet.*;
import com.atlassian.activeobjects.tx.Transactional;

import java.util.List;
import org.catrobat.confluence.activeobjects.Timesheet;

@Transactional
public interface TimesheetService
{
	Timesheet add(String userKey, int targetHoursPractice, int targetHoursTheory, String lecture);

	List<Timesheet> all();

	Timesheet getTimesheetByUser(String userKey);

}