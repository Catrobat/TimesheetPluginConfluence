/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.catrobat.confluence.rest;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import org.catrobat.confluence.activeobjects.Category;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;
import org.catrobat.confluence.rest.json.JsonTimesheetEntry;
import org.catrobat.confluence.services.CategoryService;
import org.catrobat.confluence.services.PermissionService;
import org.catrobat.confluence.services.TeamService;
import org.catrobat.confluence.services.TimesheetEntryService;
import org.catrobat.confluence.services.TimesheetService;

@Path("/")
@Produces({MediaType.APPLICATION_JSON})
public class TimesheetRest {
  
  private final TimesheetEntryService entryService;
  private final TimesheetService sheetService;
  private final CategoryService categoryService;
  private final TeamService teamService;
  private final UserManager userManager;
  private final UserAccessor userAccessor;
  private final PermissionService permissionService; 

  public TimesheetRest(TimesheetEntryService es, TimesheetService ss, 
      CategoryService cs, UserAccessor ua, TeamService teamService, 
      UserManager um, TeamService ts, PermissionService ps) {
    this.userManager = um;
    this.teamService = ts;
    this.entryService = es;
    this.sheetService = ss;
    this.categoryService = cs;
    this.userAccessor = ua;
    this.permissionService = ps;
  }
  
  @GET
  @Path("timesheets")
  public Response doHelloWorld() {
    return Response.ok("Hello World").build();
  }

  @GET
  @Path("helloworld")
  public Response getTimesheet() {
    return Response.ok("Hello World").build();
  }

  @POST
  @Path("timesheets/{timesheetID}/entries")
  public Response postTimesheetEntry(@Context HttpServletRequest request, 
      final JsonTimesheetEntry entry, @PathParam("timesheetID") int timesheetID) {
    
    Response unauthorized = permissionService.checkPermission(request);
    if(unauthorized != null) return unauthorized;
    
    Timesheet sheet = sheetService.getTimesheetByID(timesheetID);
    UserProfile user = userManager.getRemoteUser(request); 
    
    unauthorized = permissionService.userCanAddTimesheetEntry(user, sheet, entry);
    if(unauthorized != null) return unauthorized;

    Category category = categoryService.getCategoryByID(entry.getCategoryID());
    Team team         = teamService.getTeamByID(entry.getTeamID());

    TimesheetEntry newEntry = entryService.add(sheet, entry.getBeginDate(), 
        entry.getEndDate(), category, entry.getDescription(), 
        entry.getPauseMinutes(), team);
    
    entry.setEntryID(newEntry.getID());
    
    return Response.ok(entry).build();
  }

  @PUT
  @Path("timesheets/{timesheetID}/entries/{entryID}")
  public Response putTimesheetEntry(@Context HttpServletRequest request, 
      final JsonTimesheetEntry jsonEntry, @PathParam("timesheetID") int timesheetID, 
      @PathParam("entryID") int entryID) {
    
    Response unauthorized = permissionService.checkPermission(request);
    if(unauthorized != null) return unauthorized;
    
    UserProfile    user  = userManager.getRemoteUser(request); 
    TimesheetEntry entry = entryService.getEntryByID(entryID);
    Timesheet      sheet = sheetService.getTimesheetByID(timesheetID);
    
    if(!entry.getTimeSheet().equals(sheet)) {
      return Response.status(Response.Status.FORBIDDEN)
        .entity("You cannot add a timesheet entry to a different timesheet").build();
    }
    
    unauthorized = permissionService.userCanEditTimesheetEntry(user, entry);
    if(unauthorized != null) return unauthorized;
    
    Category category = categoryService.getCategoryByID(jsonEntry.getCategoryID());
    Team team         = teamService.getTeamByID(jsonEntry.getTeamID());

    entryService.edit(entryID, sheet, jsonEntry.getBeginDate(), 
        jsonEntry.getEndDate(), category, jsonEntry.getDescription(), 
        jsonEntry.getPauseMinutes(), team);
    
    return Response.ok(jsonEntry).build();
  }
}