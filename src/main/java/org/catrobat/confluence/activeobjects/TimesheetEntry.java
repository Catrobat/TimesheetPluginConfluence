package org.catrobat.confluence.activeobjects;

import org.catrobat.confluence.activeobjects.impl.TimesheetEntryImpl;
import java.util.Date;
import net.java.ao.Entity;
import net.java.ao.Implementation;

@Implementation(TimesheetEntryImpl.class)
public interface TimesheetEntry extends Entity {

	public Timesheet getTimeSheet();
	public void setTimeSheet(Timesheet sheet);

	public Date getBeginDate();
	public void setBeginDate(Date date);

	public Date getEndDate();
	public void setEndDate(Date date);

	public Category getCategory();
	public void setCategory(Category category);

	public String getDescription();
	public void setDescription(String description);

	public int getPauseMinutes();
	public void setPauseMinutes(int pause);

	public Project getProject();
	public void setProject(Project project);

	public int getDurationMinutes();
	public void setDurationMinutes(int duration);

}
