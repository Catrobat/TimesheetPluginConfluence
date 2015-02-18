package org.catrobat.confluence.timesheet;

import net.java.ao.Entity;
import net.java.ao.Preload;

@Preload
public interface TimesheetEntry extends Entity
{
	String getDate();
	void setDate(String date);

	String getStartTime();
	void setStartTime(String startTime);

	String getEndTime();
	void setEndTime(String endTime);

	String getDuration();
	void setDuration(String duration);

	String getPause();
	void setPause(String pause);

	String getDescription();
	void setDescription(String description);

	boolean isTheory();
	void setTheory(boolean theory);

	String getCategory();
	void setCategory(String category);

	String getUser();
	void setUser(String username);
}