/*
 * Copyright 2015 Christof Rabensteiner
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
import org.catrobat.confluence.activeobjects.*;
import org.catrobat.confluence.services.DBUtil;
import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.base.Preconditions.checkNotNull;

public class DBUtilImpl implements DBUtil {

    private ActiveObjects ao;

    public DBUtilImpl() {
    }

    @Autowired
    public void setActiveObjects(ActiveObjects ao) {
        this.ao = checkNotNull(ao);
    }

    public void cleanDB() {
        ao.deleteWithSQL(ApprovedGroup.class, "1=?", "1");
        ao.deleteWithSQL(ApprovedUser.class, "1=?", "1");
        ao.deleteWithSQL(Category.class, "1=?", "1");
        ao.deleteWithSQL(CategoryToTeam.class, "1=?", "1");
        ao.deleteWithSQL(Config.class, "1=?", "1");
        ao.deleteWithSQL(Group.class, "1=?", "1");
        ao.deleteWithSQL(Team.class, "1=?", "1");
        ao.deleteWithSQL(TeamToGroup.class, "1=?", "1");
        ao.deleteWithSQL(Timesheet.class, "1=?", "1");
        ao.deleteWithSQL(TimesheetEntry.class, "1=?", "1");
    }

    public void insertDefaultData() {
    /*
    UserKey key = um.getRemoteUserKey();
    if (key != null) {
      String userKey = key.getStringValue();
      Timesheet sheet = tss.add(userKey, 150, 0, "Confluence Timesheet");
      System.out.println("user key was " + userKey);
      System.out.println("created timesheet: " + sheet.getID());
    }
    */
    }

    public void printDBStatus() {
        System.out.println("  ApprovedGroup:  " + ao.find(ApprovedGroup.class).length);
        System.out.println("  ApprovedUser:   " + ao.find(ApprovedUser.class).length);
        System.out.println("  Category:       " + ao.find(Category.class).length);
        System.out.println("  CategoryToTeam: " + ao.find(CategoryToTeam.class).length);
        System.out.println("  Config:         " + ao.find(Config.class).length);
        System.out.println("  Group:          " + ao.find(Group.class).length);
        System.out.println("  Team:           " + ao.find(Team.class).length);
        System.out.println("  TeamToGroup:    " + ao.find(TeamToGroup.class).length);
        System.out.println("  Timesheet:      " + ao.find(Timesheet.class).length);
        System.out.println("  TimesheetEntry: " + ao.find(TimesheetEntry.class).length);
    }
}
