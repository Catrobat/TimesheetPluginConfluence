package org.catrobat.confluence.services;

import com.atlassian.activeobjects.tx.Transactional;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.catrobat.confluence.activeobjects.Timesheet;

@Transactional
public interface TimesheetService
{
	/**
	 * Edits a existing Timesheet
	 * @param userKey identifies the user
	 * @param targetHoursPractice specifies the amount of hours the user has to
	 *	solve in practical work
	 * @param targetHoursTheory specifies the amount of hours the user has to
	 *	invest in theoretical work
	 * @param lecture describes the lecture in which the user is enrolled
	 * @return the new Timesheet, or null
	 */
	@Nullable
	Timesheet editTimesheet(String userKey, int targetHoursPractice, int targetHoursTheory, String lecture);

	/**
	 * Adds a new Timesheet
	 * @param userKey identifies the user
	 * @param targetHoursPractice specifies the amount of hours the user has to
	 *	solve in practical work
	 * @param targetHoursTheory specifies the amount of hours the user has to
	 *	invest in theoretical work
	 * @param lecture describes the lecture in which the user is enrolled
	 * @return the new Timesheet
	 */
	@Nonnull
	Timesheet add(String userKey, int targetHoursPractice, int targetHoursTheory, String lecture);

	/**
	 * Return all Timesheets
	 * @return
	 */
	@Nonnull
	List<Timesheet> all();

	/**
	 * Returns Timesheet corresponding to a User
	 *
	 * @param userKey
	 * @return Timesheet, null if unknown user
	 */
	@Nullable
	Timesheet getTimesheetByUser(String userKey);
  
  /**
   * Returns a timesheet with the corresponding id
   * @param id
   * @return 
   */
	@Nullable
  Timesheet getTimesheetByID(int id);

}