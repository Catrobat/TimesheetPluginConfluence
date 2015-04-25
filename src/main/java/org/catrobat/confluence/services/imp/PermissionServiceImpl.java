package org.catrobat.confluence.services.imp;

import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.services.PermissionService;
import org.catrobat.confluence.services.TeamService;

public class PermissionServiceImpl implements PermissionService {
  
  private UserManager userManager;
  private TeamService teamService;

  public PermissionServiceImpl(UserManager userManager, TeamService teamService) {
    this.userManager = userManager;
    this.teamService = teamService;
  }
  
  public Response checkPermission(HttpServletRequest request) {
    //just a stub... not done!
    UserKey userKey = userManager.getRemoteUserKey(request);

    if (userKey == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
//    } else if (!userManager.isAdmin(userKey)) {
//      return Response.status(Response.Status.UNAUTHORIZED).build();
//    } else if (!permissionCondition.isApproved(userKey)) {
//      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

//    permissionManager.hasPermission(null, Permission.VIEW, null); 
//    permissionManager. 
    
    return null;
  }

  @Override
  public boolean userCanEditTimesheet(UserProfile user, Timesheet sheet) {
    if (sheet.getUserKey().equals(user.getUserKey().getStringValue())
        || (userManager.isAdmin(user.getUserKey())))
      return true; 
    
    UserProfile owner = userManager.getUserProfile(sheet.getUserKey());
    if(owner == null)
      return false; 
    
    Set<Team> ownerTeams = teamService.getTeamsOfUser(owner.getUsername());
    Set<Team> userTeams = teamService.getCoordinatorTeamsOfUser(user.getUsername());
    
    ownerTeams.retainAll(userTeams);
        
    return ownerTeams.size() > 0;
  } 
  
}
