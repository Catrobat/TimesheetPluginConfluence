package org.catrobat.confluence.services.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import static com.google.common.collect.Lists.newArrayList;
import java.util.List;

import com.atlassian.confluence.core.service.NotAuthorizedException;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.services.TimesheetService;

public class TimesheetServiceImpl implements TimesheetService {

	private final ActiveObjects ao;

	public TimesheetServiceImpl(ActiveObjects ao) {
		this.ao = ao;
	}

	@Override
	public Timesheet editTimesheet(String userKey, int targetHoursPractice,
																 int targetHoursTheory, String lecture) {
		Timesheet[] found = ao.find(Timesheet.class, "USER_KEY = ?", userKey);
		if((found.length == 1)) {
			Timesheet sheet = found[0];

			sheet.setUserKey(userKey);
			sheet.setTargetHoursPractice(targetHoursPractice);
			sheet.setTargetHoursTheory(targetHoursTheory);
			sheet.setLecture(lecture);
			sheet.setIsActive(true);
			sheet.save();
			return sheet;
		}
		return null;
	}

	@Override
	public Timesheet add(String userKey, int targetHoursPractice,
					int targetHoursTheory, String lecture) {
		Timesheet sheet = ao.create(Timesheet.class);
		sheet.setUserKey(userKey);
		sheet.setTargetHoursPractice(targetHoursPractice);
		sheet.setTargetHoursTheory(targetHoursTheory);
		sheet.setLecture(lecture);
		sheet.setIsActive(true);
		sheet.save();
		return sheet;
	}

	@Override
	public List<Timesheet> all() {
		return newArrayList(ao.find(Timesheet.class));
	}

	@Override
	public Timesheet getTimesheetByUser(String userKey) {
		Timesheet[] found = ao.find(Timesheet.class, "USER_KEY = ?", userKey);

		if (found.length > 1) {
			throw new NotAuthorizedException("Multiple Timesheets with the same User.");
		}

		return (found.length > 0)? found[0] : null;
	}

  @Override
  public Timesheet getTimesheetByID(int id) {
		Timesheet[] found = ao.find(Timesheet.class, "ID = ?", id);

		if (found.length > 1) {
			throw new NotAuthorizedException("Multiple Timesheets with the same ID.");
		}

		return (found.length > 0)? found[0] : null;
  }
}
