package org.catrobat.confluence.services.imp;

import com.atlassian.confluence.core.service.NotAuthorizedException;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import java.util.Date;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;
import org.catrobat.confluence.rest.json.JsonTimesheetEntry;
import org.catrobat.confluence.services.PermissionService;
import org.catrobat.confluence.services.TeamService;
import org.joda.time.DateTime;

public class PermissionServiceImpl implements PermissionService {
  
  private final UserManager userManager;
  private final TeamService teamService;

  public PermissionServiceImpl(UserManager userManager, TeamService teamService) {
    this.userManager = userManager;
    this.teamService = teamService;
  }
  
  public UserProfile checkIfUserExists(HttpServletRequest request) {
    UserProfile userProfile = userManager.getRemoteUser(request);

    if (userProfile == null) {
      throw new NotAuthorizedException("User does not exist.");
    }
    return userProfile;
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
  
  private boolean dateIsOld(Date date) {
    DateTime aMonthAgo = new DateTime().minusDays(30);
    DateTime datetime  = new DateTime(date);
    return (datetime.compareTo(aMonthAgo) < 0);
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
      if (dateIsOld(entry.getBeginDate()) || dateIsOld(entry.getEndDate())) {
        throw new NotAuthorizedException("You can not add an old entry.");
      }
    } else if (!userIsAdmin(user)) {
      throw new NotAuthorizedException("You are not Admin.");
    }
  }

  @Override
  public void userCanDeleteTimesheetEntry(UserProfile user, TimesheetEntry entry) {

    if (userOwnsSheet(user, entry.getTimeSheet())) {
      if (dateIsOld(entry.getBeginDate()) || dateIsOld(entry.getEndDate())) {
        throw new NotAuthorizedException("You can not add an old entry.");
      }
    } else if (!userIsAdmin(user)) {
      throw new NotAuthorizedException("You are not Admin");
    }
  }
}
