package org.catrobat.confluence.services.impl;

import com.atlassian.confluence.core.service.NotAuthorizedException;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.catrobat.confluence.activeobjects.*;
import org.catrobat.confluence.rest.json.JsonTimesheetEntry;
import org.catrobat.confluence.services.PermissionService;
import org.catrobat.confluence.services.TeamService;
import com.atlassian.confluence.user.UserAccessor;
import org.joda.time.DateTime;

public class PermissionServiceImpl implements PermissionService {
  
  private final UserManager userManager;
  private final TeamService teamService;
  private final AdminHelperConfigService adminHelperConfigService;
  private final UserAccessor userAccessor;

  public PermissionServiceImpl(UserManager userManager, TeamService teamService,
                               AdminHelperConfigService adminHelperConfigService, UserAccessor userAccessor) {
    this.userManager = userManager;
    this.teamService = teamService;
    this.adminHelperConfigService = adminHelperConfigService;
    this.userAccessor = userAccessor;
  }
  
  public UserProfile checkIfUserExists(HttpServletRequest request) {
    UserProfile userProfile = userManager.getRemoteUser(request);

    if (userProfile == null) {
      throw new NotAuthorizedException("User does not exist.");
    }
    return userProfile;
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
    UserKey userKey = userManager.getRemoteUser(request).getUserKey();

    if (userKey == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } else if (!userManager.isSystemAdmin(userKey)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } else if (!isApproved(userAccessor.getUserByKey(userKey).getFullName())) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    return null;
  }

  public boolean isApproved(UserProfile applicationUser) {
    if (applicationUser == null || !userManager.isSystemAdmin(applicationUser.getUserKey())) {
      return false;
    }

    // check if permissions are set
    AdminHelperConfig config = adminHelperConfigService.getConfiguration();
    if(config.getApprovedGroups().length == 0 && config.getApprovedUsers().length == 0){
      return true;
    }

    if (adminHelperConfigService.isUserApproved(applicationUser.getUserKey().getStringValue())) {
      return true;
    }

    Collection<String> groupNameCollection = userAccessor.getGroupNamesForUserName(applicationUser.getUsername());
    for (String groupName : groupNameCollection) {
      if (adminHelperConfigService.isGroupApproved(groupName))
        return true;
    }

    return false;
  }

  public boolean isApproved(String userName) {
    return isApproved(userManager.getUserProfile(userName));
  }

  public boolean userIsAdmin(String userName) {
    return userManager.isAdmin(userName);
  }

  private boolean userOwnsSheet(UserProfile user, Timesheet sheet) {
    if(sheet == null || user == null) {
      return false; 
    }
    
    String sheetKey = sheet.getUserKey(); 
    String userKey  = user.getUserKey().getStringValue(); 
    return sheetKey.equals(userKey); 
  }
  
  private boolean userIsAdmin(UserProfile user) {
    return userManager.isAdmin(user.getUserKey());  
  }
  
  private boolean dateIsOlderThanAMonth(Date date) {
    DateTime aMonthAgo = new DateTime().minusDays(30);
    DateTime datetime  = new DateTime(date);
    return (datetime.compareTo(aMonthAgo) < 0);
  }

  private boolean dateIsOlderThanFiveYears(Date date) {
    DateTime fiveYearsAgo = new DateTime().minusYears(5);
    DateTime datetime  = new DateTime(date);
    return (datetime.compareTo(fiveYearsAgo) < 0);
  }

  private boolean userCoordinatesTeamsOfSheet(UserProfile user, Timesheet sheet) {
    UserProfile owner = userManager.getUserProfile(sheet.getUserKey());
    if(owner == null)
      return false; 
    
    Set<Team> ownerTeams = teamService.getTeamsOfUser(owner.getUsername());
    Set<Team> userTeams = teamService.getCoordinatorTeamsOfUser(user.getUsername());
    
    ownerTeams.retainAll(userTeams);
        
    return ownerTeams.size() > 0;
  }
  
  @Override
  public boolean userCanViewTimesheet(UserProfile user, Timesheet sheet) {
    return user != null && sheet != null && 
        (userOwnsSheet(user, sheet) 
        || userIsAdmin(user) 
        || userCoordinatesTeamsOfSheet(user, sheet));
  } 
  
  @Override
  public void userCanEditTimesheetEntry(UserProfile user, Timesheet sheet, JsonTimesheetEntry entry) {
  
    if (userOwnsSheet(user, sheet)) {
      if(!entry.getIsGoogleDocImport()) {
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

  @Override
  public void userCanDeleteTimesheetEntry(UserProfile user, TimesheetEntry entry) {

    if (userOwnsSheet(user, entry.getTimeSheet())) {
      if(!entry.getIsGoogleDocImport()) {
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
