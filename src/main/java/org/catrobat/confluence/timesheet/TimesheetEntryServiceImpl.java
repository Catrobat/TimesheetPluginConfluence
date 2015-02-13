package org.catrobat.confluence.timesheet;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.user.UserKey;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

public class TimesheetEntryServiceImpl implements TimesheetEntryService
{
    private final ActiveObjects ao;

    public TimesheetEntryServiceImpl(ActiveObjects ao){
        this.ao = checkNotNull(ao);
    }

    @Override
    public TimesheetEntry add(String date, String startTime, String endTime, String duration, String pause, boolean theory, String description, String category, String username) {
        TimesheetEntry entry = ao.create(TimesheetEntry.class);
        entry.setDate(date);
        entry.setStartTime(startTime);
        entry.setEndTime(endTime);
        entry.setDuration(duration);
        entry.setPause(pause);
        entry.setDescription(description);
        entry.setTheory(theory);
        entry.setCategory(category);
        entry.setUser(username);
        entry.save();
        return entry;
    }

    @Override
    public List<TimesheetEntry> all() {
        return newArrayList(ao.find(TimesheetEntry.class));
    }

    @Override
    public List<TimesheetEntry> allForUser(String username) {
        return newArrayList(ao.find(TimesheetEntry.class, "USER = ?", username));
    }
}
