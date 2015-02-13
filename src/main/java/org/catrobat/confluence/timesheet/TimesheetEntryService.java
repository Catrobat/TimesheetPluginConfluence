package org.catrobat.confluence.timesheet;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.sal.api.user.UserKey;

import java.util.List;

@Transactional
public interface TimesheetEntryService
{
    TimesheetEntry add(String date, String startTime, String endTime, String duration, String pause, boolean theory, String description, String category, String username);

    List<TimesheetEntry> all();
    List<TimesheetEntry> allForUser(String username);
}