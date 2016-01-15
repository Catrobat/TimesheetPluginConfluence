package org.catrobat.confluence.services;

import com.atlassian.sal.api.user.UserProfile;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;
import org.catrobat.confluence.rest.json.JsonTimesheetEntry;

public interface PermissionService {

  public UserProfile checkIfUserExists(HttpServletRequest request);

  public UserProfile checkIfUsernameExists(String userName);

  public boolean checkIfUserExists(String userName);

  public Response checkPermission(HttpServletRequest request);
  
  public boolean userCanViewTimesheet(UserProfile user, Timesheet sheet);

  public void userCanEditTimesheetEntry(UserProfile user, Timesheet sheet, JsonTimesheetEntry entry);

  public void userCanDeleteTimesheetEntry(UserProfile user, TimesheetEntry entry);
}
