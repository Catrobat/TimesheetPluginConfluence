package org.catrobat.confluence.services;

import com.atlassian.sal.api.user.UserProfile;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.catrobat.confluence.activeobjects.Timesheet;

public interface PermissionService {

  public Response checkPermission(HttpServletRequest request);
  
  public boolean userCanEditTimesheet(UserProfile user, Timesheet sheet);
  
}
