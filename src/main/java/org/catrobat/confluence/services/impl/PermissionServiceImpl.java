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
import com.atlassian.confluence.user.ConfluenceUserManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import net.java.ao.schema.Table;
import org.catrobat.confluence.activeobjects.ConfigService;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;
import org.catrobat.confluence.rest.json.JsonTimesheetEntry;
import org.catrobat.confluence.services.PermissionService;
import org.catrobat.confluence.services.TeamService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@Table("Permissions")
public class PermissionServiceImpl implements PermissionService {

    protected UserManager userManager;
    private final TeamService teamService;
    private final ConfigService configService;
    protected UserAccessor userAccessor;
    protected ActiveObjects ao;
    protected ConfluenceUserManager confluenceUserManager;

    public PermissionServiceImpl(TeamService teamService,
                                 ConfigService configService) {
        this.teamService = teamService;
        this.configService = configService;
    }

    @Autowired
    public void setActiveObjects(ActiveObjects ao) {
        this.ao = checkNotNull(ao);
    }

    @Autowired
    public void setUserAccessor(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }

    @Autowired
    public void setUserAccessor(UserManager userManager) {
        this.userManager = userManager;
    }

    /*@Autowired
    public void setConfluenceUserManager(ConfluenceUserManager confluenceUserManager){
        this.confluenceUserManager = confluenceUserManager;
    }*/

    public UserProfile checkIfUserExists(HttpServletRequest request) {
        //TODO: fix it
        //UserProfile userProfile = userManager.getRemoteUser(request);

        /*
        if (userProfile == null) {
            throw new NotAuthorizedException("User does not exist.");
        }
        return userProfile;
*/
        return null;
    }

    public UserProfile checkIfUsernameExists(String userName) {
        UserProfile userProfile = userManager.getUserProfile(userName);

        if (userProfile == null) {
            throw new NotAuthorizedException("User does not exist.");
        }
        return userProfile;
    }

    public boolean checkIfUserExists(String userName) {
        UserProfile userProfile = userManager.getUserProfile(userName);
        if (userProfile == null) {
            return false;
        }
        return true;
    }

    public Response checkPermission(HttpServletRequest request) {
        //TODO: fix it, Use UserProfile.getUserKey() instead
        /*
        UserKey userKey = userManager.getRemoteUser(request).getUserKey();

        if (userKey == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } else if (!userManager.isSystemAdmin(userKey)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } else if (!isApproved(userAccessor.getUserByKey(userKey).getFullName())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        */

        return null;
    }

    public boolean isApproved(UserProfile applicationUser) {
        //TODO: fix it
        /*
        if (applicationUser == null || !userManager.isSystemAdmin(applicationUser.getUserKey())) {
            return false;
        }

        Config config = configService.getConfiguration();
        if (config.getApprovedGroups().length == 0 && config.getApprovedUsers().length == 0) {
            return true;
        }

        if (configService.isUserApproved(applicationUser.getUserKey().getStringValue())) {
            return true;
        }

        Collection<String> groupNameCollection = userAccessor.getGroupNamesForUserName(applicationUser.getUsername());
        for (String groupName : groupNameCollection) {
            if (configService.isGroupApproved(groupName))
                return true;
        }
        */

        return false;
    }

    public boolean isApproved(String userName) {
        return isApproved(userManager.getUserProfile(userName));
    }

    public boolean userIsAdmin(String userName) {
        return userManager.isAdmin(userName);
    }

    private boolean userOwnsSheet(UserProfile user, Timesheet sheet) {
        if (sheet == null || user == null) {
            return false;
        }

        //TODO:
        /*
        String sheetKey = sheet.getUserKey();
        String userKey = user.getUserKey().getStringValue();
        return sheetKey.equals(userKey);
        */
        return false;
    }

    private boolean userIsAdmin(UserProfile user) {
        //TODO:
        //return userManager.isAdmin(user.getUserKey());
        return false;
    }

    private boolean dateIsOlderThanAMonth(Date date) {
        DateTime aMonthAgo = new DateTime().minusDays(30);
        DateTime datetime = new DateTime(date);
        return (datetime.compareTo(aMonthAgo) < 0);
    }

    private boolean dateIsOlderThanFiveYears(Date date) {
        DateTime fiveYearsAgo = new DateTime().minusYears(5);
        DateTime datetime = new DateTime(date);
        return (datetime.compareTo(fiveYearsAgo) < 0);
    }

    private boolean userCoordinatesTeamsOfSheet(UserProfile user, Timesheet sheet) {
        UserProfile owner = userManager.getUserProfile(sheet.getUserKey());
        if (owner == null)
            return false;

        Set<Team> ownerTeams = teamService.getTeamsOfUser(owner.getUsername());
        Set<Team> userTeams = teamService.getCoordinatorTeamsOfUser(user.getUsername());

        ownerTeams.retainAll(userTeams);

        return ownerTeams.size() > 0;
    }

    public boolean userCanViewTimesheet(UserProfile user, Timesheet sheet) {
        return user != null && sheet != null &&
                (userOwnsSheet(user, sheet)
                        || userIsAdmin(user)
                        || userCoordinatesTeamsOfSheet(user, sheet));
    }

    public void userCanEditTimesheetEntry(UserProfile user, Timesheet sheet, JsonTimesheetEntry entry) {

        if (userOwnsSheet(user, sheet)) {
            if (!entry.getIsGoogleDocImport()) {
                if (dateIsOlderThanAMonth(entry.getBeginDate()) || dateIsOlderThanAMonth(entry.getEndDate())) {
                    throw new NotAuthorizedException("You can not edit an entry that is older than 30 days.");
                }
            } else {
                if (dateIsOlderThanFiveYears(entry.getBeginDate()) || dateIsOlderThanFiveYears(entry.getEndDate())) {
                    throw new NotAuthorizedException("You can not edit an imported entry that is older than 5 years.");
                }
            }
        } else if (!userIsAdmin(user)) {
            throw new NotAuthorizedException("You are not Admin.");
        }
    }

    public void userCanDeleteTimesheetEntry(UserProfile user, TimesheetEntry entry) {

        if (userOwnsSheet(user, entry.getTimeSheet())) {
            if (!entry.getIsGoogleDocImport()) {
                if (dateIsOlderThanAMonth(entry.getBeginDate()) || dateIsOlderThanAMonth(entry.getEndDate())) {
                    throw new NotAuthorizedException("You can not delete an that is older than 30 days.");
                }
            } else {
                if (dateIsOlderThanFiveYears(entry.getBeginDate()) || dateIsOlderThanFiveYears(entry.getEndDate())) {
                    throw new NotAuthorizedException("You can not delete an imported entry that is older than 5 years.");
                }
            }
        } else if (!userIsAdmin(user)) {
            throw new NotAuthorizedException("You are not Admin");
        }
    }
}
