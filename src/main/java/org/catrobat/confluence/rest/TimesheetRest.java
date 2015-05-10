/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.catrobat.confluence.rest;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import org.catrobat.confluence.activeobjects.Category;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;
import org.catrobat.confluence.rest.json.JsonTimesheet;
import org.catrobat.confluence.rest.json.JsonTimesheetEntry;
import org.catrobat.confluence.rest.json.JsonCategory;
import org.catrobat.confluence.rest.json.JsonTeam;
import org.catrobat.confluence.services.CategoryService;
import org.catrobat.confluence.services.DBFillerService;
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
  private final PermissionService permissionService; 
  private final DBFillerService dbfiller;

  public TimesheetRest(TimesheetEntryService es, TimesheetService ss, 
      CategoryService cs, UserAccessor ua, TeamService teamService, 
      UserManager um, TeamService ts, PermissionService ps, DBFillerService df) {
    this.userManager = um;
    this.teamService = ts;
    this.entryService = es;
    this.sheetService = ss;
    this.categoryService = cs;
    this.permissionService = ps;
    this.dbfiller = df;
  }
  
  @GET
  @Path("timesheets")
  public Response getTimesheets() {
    return Response.ok("Hello World").build();
  }

  @GET
  @Path("helloworld")
  public Response doHelloWorld() {
    return Response.ok("Hello World").build();
  }

  @GET
  @Path("teams")
  public Response getTeams(@Context HttpServletRequest request) {
    Response unauthorized = permissionService.checkPermission(request);  
    if(unauthorized != null) return unauthorized;
    
    List<JsonTeam> teams = new LinkedList<JsonTeam>();
    
    String userName = userManager.getRemoteUser().getUsername();

    for(Team team : teamService.getTeamsOfUser(userName)) {
      Category[] categories = team.getCategories();
      int[] categoryIDs = new int[categories.length];
      for(int i = 0; i < categories.length; i++) {
        categoryIDs[i] = categories[i].getID();
      } 
      teams.add(new JsonTeam(team.getID(), team.getTeamName(), categoryIDs));
    }
    
    return Response.ok(teams).build();
  }
  
  @GET
  @Path("categories")
  public Response getCategories(@Context HttpServletRequest request) {
    Response unauthorized = permissionService.checkPermission(request);  
    if(unauthorized != null) return unauthorized;
    
    List<JsonCategory> categories = new LinkedList<JsonCategory>();
    
    for(Category category : categoryService.all()) {
      categories.add(new JsonCategory(category.getID(), category.getName()));
    }
    
    return Response.ok(categories).build();
  }

  @GET
  @Path("timesheets/{timesheetID}")
  public Response getTimesheet(@Context HttpServletRequest request, 
      @PathParam("timesheetID") int timesheetID) {
    
    Timesheet sheet  = sheetService.getTimesheetByID(timesheetID);
    UserProfile user = userManager.getRemoteUser(request); 
    
    if(sheet == null || !permissionService.userCanViewTimesheet(user, sheet)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    
    JsonTimesheet jsonTimesheet = new JsonTimesheet(timesheetID, 
        sheet.getTargetHoursPractice(), sheet.getTargetHoursTheory(),
        sheet.getLecture(), sheet.getIsActive());
    
    return Response.ok(jsonTimesheet).build();
  }
  
  @GET
  @Path("timesheets/{timesheetID}/entries")
  public Response getTimesheetEntries(@Context HttpServletRequest request, 
      @PathParam("timesheetID") int timesheetID) {
    
    Timesheet sheet  = sheetService.getTimesheetByID(timesheetID);
    UserProfile user = userManager.getRemoteUser(request); 
    
    if(sheet == null || !permissionService.userCanViewTimesheet(user, sheet)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    
    TimesheetEntry[] entries = entryService.getEntriesBySheet(sheet);
    
    List<JsonTimesheetEntry> jsonEntries = new ArrayList<JsonTimesheetEntry>(entries.length); 
    
    for(TimesheetEntry entry : entries) {
      jsonEntries.add(new JsonTimesheetEntry(entry.getID(), entry.getBeginDate(), 
          entry.getEndDate(), entry.getPauseMinutes(), 
          entry.getDescription(), entry.getTeam().getID(), 
          entry.getCategory().getID()));
    }
    
    return Response.ok(jsonEntries).build();
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
  
  @GET
  @Path("cleanandinitdb")
  public Response cleanAndInitDB() {
    
    if (userManager.isAdmin(userManager.getRemoteUserKey())) {
      System.out.println("clean db. before cleaning: ");
      dbfiller.printDBStatus();

      dbfiller.cleanDB();

      System.out.println("after cleaning: ");
      dbfiller.printDBStatus();

      dbfiller.insertDefaultData();

      System.out.println("after default data: ");
      dbfiller.printDBStatus();
    }
    return Response.ok("cleanandinitdb").build();
  }
}