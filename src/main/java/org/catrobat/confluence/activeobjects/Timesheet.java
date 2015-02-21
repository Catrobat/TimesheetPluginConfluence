package org.catrobat.confluence.activeobjects;

import java.util.Date;
import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.Preload;

public interface Timesheet extends Entity
{
	String getUserKey();
	void setUserKey(String key);

	int getTargetHoursPractice();
	void setTargetHoursPractice(int hours);

	int getTargetHoursTheory();
	void setTargetHoursTheory(int hours);

	boolean getIsActive();
	void setIsActive(boolean isActive);

	String getLecture();
	void setLecture(String lecture);

	@OneToMany(reverse = "getTimeSheet")
	TimesheetEntry[] getEntries();

}