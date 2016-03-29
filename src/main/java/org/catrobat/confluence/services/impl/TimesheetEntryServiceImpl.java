/*
 * Copyright 2016 Adrian Schnedlitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.catrobat.confluence.services.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.confluence.core.service.NotAuthorizedException;
import net.java.ao.Query;
import net.java.ao.schema.Table;
import org.catrobat.confluence.activeobjects.Category;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;
import org.catrobat.confluence.services.TimesheetEntryService;

import javax.annotation.Nullable;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

@Table("Timesheet_Entries")
public class TimesheetEntryServiceImpl implements TimesheetEntryService {

    private final ActiveObjects ao;

    public TimesheetEntryServiceImpl(ActiveObjects ao) {
        this.ao = checkNotNull(ao);
    }

    public TimesheetEntry add(Timesheet sheet, Date begin, Date end,
                              Category category, String description, int pause, Team team, boolean isGoogleDocImport) {

        TimesheetEntry entry = ao.create(TimesheetEntry.class);

        entry.setTimeSheet(sheet);
        entry.setBeginDate(begin);
        entry.setEndDate(end);
        entry.setCategory(category);
        entry.setDescription(description);
        entry.setPauseMinutes(pause);
        entry.setTeam(team);
        entry.setIsGoogleDocImport(isGoogleDocImport);

        entry.save();

        return entry;

    }

    @Nullable
    public TimesheetEntry getEntryByID(int entryID) {
        TimesheetEntry[] found = ao.find(TimesheetEntry.class, "ID = ?", entryID);

        if (found.length > 1) {
            throw new NotAuthorizedException("Multiple Timesheet Entries with the same ID.");
        }

        return (found.length > 0) ? found[0] : null;
    }

    @Nullable
    public TimesheetEntry edit(int entryId, Timesheet sheet, Date begin, Date end,
                               Category category, String description, int pause, Team team, boolean isGoogleDocImport) {

        TimesheetEntry entry = getEntryByID(entryId);

        if (entry == null) {
            return null;
        }

        entry.setTimeSheet(sheet);
        entry.setBeginDate(begin);
        entry.setEndDate(end);
        entry.setCategory(category);
        entry.setDescription(description);
        entry.setPauseMinutes(pause);
        entry.setTeam(team);
        entry.setIsGoogleDocImport(isGoogleDocImport);

        entry.save();

        return entry;
    }

    public TimesheetEntry[] getEntriesBySheet(Timesheet sheet) {
        if (sheet == null) return new TimesheetEntry[0];
        return ao.find(
                TimesheetEntry.class,
                Query.select()
                        .where("TIME_SHEET_ID = ?", sheet.getID())
                        .order("BEGIN_DATE DESC")
        );
    }

    public void delete(TimesheetEntry entry) {
        ao.delete(entry);
    }
}
