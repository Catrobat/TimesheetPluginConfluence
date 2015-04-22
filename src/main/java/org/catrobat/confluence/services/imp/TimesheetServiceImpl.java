package org.catrobat.confluence.services.imp;

import com.atlassian.activeobjects.external.ActiveObjects;
import static com.google.common.collect.Lists.newArrayList;
import java.util.List;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.services.TimesheetService;

public class TimesheetServiceImpl implements TimesheetService {

	private final ActiveObjects ao;

	public TimesheetServiceImpl(ActiveObjects ao) {
		this.ao = ao;
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
		assert(found.length <= 1);
		return (found.length > 0)? found[0] : null;
	}

  @Override
  public Timesheet getTimesheetByID(int id) {
		Timesheet[] found = ao.find(Timesheet.class, "ID = ?", id);
		assert(found.length <= 1);
		return (found.length > 0)? found[0] : null;
  }
}
