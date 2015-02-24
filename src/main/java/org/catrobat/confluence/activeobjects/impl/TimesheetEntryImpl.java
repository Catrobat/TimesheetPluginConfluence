package org.catrobat.confluence.activeobjects.impl;


import java.text.SimpleDateFormat;
import java.util.Date;
import org.catrobat.confluence.activeobjects.TimesheetEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimesheetEntryImpl {

	private final TimesheetEntry entry;
	private static final Logger log = LoggerFactory.getLogger(TimesheetEntryImpl.class);

	public TimesheetEntryImpl(TimesheetEntry entry) {
		this.entry = entry;
	}

	public void setBeginDate(Date date) {
		entry.setBeginDate(date);
		entry.setDurationMinutes(getDuration());
	}

	public void setEndDate(Date date) {
		entry.setEndDate(date);
		entry.setDurationMinutes(getDuration());
	}

	public void setPauseMinutes(int pause) {
		entry.setPauseMinutes(pause);
		entry.setDurationMinutes(getDuration());
	}

	public void setDurationMinutes(int duration) {
		log.warn("You should not invoke setDurationMinutes(), because its a stub "
				+ "that prevents inconsistency");
	}

	private int getDuration() {

		Date beginDate = entry.getBeginDate();
		Date endDate   = entry.getEndDate();

		if(beginDate == null || endDate == null)
			return 0;
		
		long diff = endDate.getTime() - beginDate.getTime();
		int durationMinutes = (int)(diff / (60 * 1000)) - entry.getPauseMinutes(); 
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy hh:mm");
		log.debug("getDuration() invoked ");
		log.debug("begin date: " + sdf.format(beginDate));
		log.debug("end date  : " + sdf.format(endDate));
		log.debug("pause     : " + entry.getPauseMinutes());
		log.debug("duration  : " + durationMinutes);

		return durationMinutes;
	}

}
