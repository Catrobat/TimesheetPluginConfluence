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
import net.java.ao.schema.Table;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.services.TimesheetService;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

@Table("Timesheets")
public class TimesheetServiceImpl implements TimesheetService {

    private final ActiveObjects ao;

    public TimesheetServiceImpl(ActiveObjects ao) {
        this.ao = checkNotNull(ao);
    }

    public Timesheet editTimesheet(String userKey, int targetHoursPractice,
                                   int targetHoursTheory, String lecture) {
        Timesheet[] found = ao.find(Timesheet.class, "USER_KEY = ?", userKey);
        if ((found.length == 1)) {
            Timesheet sheet = found[0];

            sheet.setUserKey(userKey);
            sheet.setTargetHoursPractice(targetHoursPractice);
            sheet.setTargetHoursTheory(targetHoursTheory);
            sheet.setLecture(lecture);
            sheet.setIsActive(true);
            sheet.save();
            return sheet;
        }
        return null;
    }

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

    public List<Timesheet> all() {
        return newArrayList(ao.find(Timesheet.class));
    }

    public Timesheet getTimesheetByUser(String userKey) {
        Timesheet[] found = ao.find(Timesheet.class, "USER_KEY = ?", userKey);

        if (found.length > 1) {
            throw new NotAuthorizedException("Multiple Timesheets with the same User.");
        }

        return (found.length > 0) ? found[0] : null;
    }

    public Timesheet getTimesheetByID(int id) {
        Timesheet[] found = ao.find(Timesheet.class, "ID = ?", id);

        if (found.length > 1) {
            throw new NotAuthorizedException("Multiple Timesheets with the same ID.");
        }

        return (found.length > 0) ? found[0] : null;
    }
}
