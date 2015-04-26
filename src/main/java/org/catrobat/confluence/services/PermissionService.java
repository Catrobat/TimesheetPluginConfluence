package org.catrobat.confluence.services;

import com.atlassian.sal.api.user.UserProfile;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;
import org.catrobat.confluence.rest.json.JsonTimesheetEntry;

public interface PermissionService {

  public Response checkPermission(HttpServletRequest request);
  
  public boolean userCanEditTimesheet(UserProfile user, Timesheet sheet);

  public Response userCanAddTimesheetEntry(UserProfile user, Timesheet sheet, JsonTimesheetEntry entry);
  
  public Response userCanEditTimesheetEntry(UserProfile user, TimesheetEntry entry);
  
}
