package org.catrobat.confluence.activeobjects;

import java.util.Date;
import net.java.ao.Entity;

public interface TimesheetEntry extends Entity {

	Timesheet getTimeSheet();
	void setTimeSheet(Timesheet sheet);

	Date getBeginDate();
	void setBeginDate(Date date);

	Date getEndDate();
	void setEndDate(Date date);

	Category getCategory();
	void setCategory(Category category);

	String getDescription();
	void setDescription(String description);

	int getPauseMinutes();
	void setPauseMinutes(int pause);

	Project getProject();
	void setProject(Project project);

}
