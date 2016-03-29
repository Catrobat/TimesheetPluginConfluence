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

package org.catrobat.confluence.activeobjects.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimesheetEntry {

    private final org.catrobat.confluence.activeobjects.TimesheetEntry entry;
    private static final Logger log = LoggerFactory.getLogger(TimesheetEntry.class);

    public TimesheetEntry(org.catrobat.confluence.activeobjects.TimesheetEntry entry) {
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
        Date endDate = entry.getEndDate();

        if (beginDate == null || endDate == null)
            return 0;

        long diff = endDate.getTime() - beginDate.getTime();
        int durationMinutes = (int) (diff / (60 * 1000)) - entry.getPauseMinutes();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy hh:mm");
        log.debug("getDuration() invoked ");
        log.debug("begin date: " + sdf.format(beginDate));
        log.debug("end date  : " + sdf.format(endDate));
        log.debug("pause     : " + entry.getPauseMinutes());
        log.debug("duration  : " + durationMinutes);

        return durationMinutes;
    }

}
