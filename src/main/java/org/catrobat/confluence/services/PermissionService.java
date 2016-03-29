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

package org.catrobat.confluence.services;

import com.atlassian.sal.api.user.UserProfile;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;
import org.catrobat.confluence.rest.json.JsonTimesheetEntry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

public interface PermissionService {

    public UserProfile checkIfUserExists(HttpServletRequest request);

    public UserProfile checkIfUsernameExists(String userName);

    public boolean checkIfUserExists(String userName);

    public Response checkPermission(HttpServletRequest request);

    public boolean userCanViewTimesheet(UserProfile user, Timesheet sheet);

    public void userCanEditTimesheetEntry(UserProfile user, Timesheet sheet, JsonTimesheetEntry entry);

    public void userCanDeleteTimesheetEntry(UserProfile user, TimesheetEntry entry);
}
