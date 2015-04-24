/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.catrobat.confluence.rest;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.user.UserManager;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import org.catrobat.confluence.activeobjects.Category;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;
import org.catrobat.confluence.rest.json.JsonTimesheetEntry;
import org.catrobat.confluence.services.CategoryService;
import org.catrobat.confluence.services.TeamService;
import org.catrobat.confluence.services.TimesheetEntryService;
import org.catrobat.confluence.services.TimesheetService;

@Path("/")
@Produces({MediaType.APPLICATION_JSON})
public class TimesheetRest {
  
  TimesheetEntryService entryService;
  TimesheetService sheetService;
  CategoryService categoryService;
  TeamService teamService;
  UserManager userManager;
  UserAccessor userAccessor; 

  public TimesheetRest(TimesheetEntryService entryService, TimesheetService sheetService, CategoryService categoryService, TeamService teamService, UserManager userManager, UserAccessor userAccessor) {
    this.entryService = entryService;
    this.sheetService = sheetService;
    this.categoryService = categoryService;
    this.teamService = teamService;
    this.userManager = userManager;
    this.userAccessor = userAccessor;
  }
  
  @GET
  @Path("timesheets")
  public Response doHelloWorld() {
    return Response.ok("Hello World").build();
  }

  @POST
  @Path("timesheets/{timesheetID}/entries")
  public Response postTimesheetEntry(final JsonTimesheetEntry entry, 
      @PathParam("timesheetID") int timesheetID) {
    
    
    //TODO checks
    //user logged in?
    //user has write access to timesheet?
    //is chosen category available by team?
    
    Timesheet sheet   = sheetService.getTimesheetByID(timesheetID);
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
  public Response postTimesheetEntry(final JsonTimesheetEntry entry, 
      @PathParam("timesheetID") int timesheetID, 
      @PathParam("entryID") int entryID) {
    
    Timesheet sheet   = sheetService.getTimesheetByID(timesheetID);
    Category category = categoryService.getCategoryByID(entry.getCategoryID());
    Team team         = teamService.getTeamByID(entry.getTeamID());

    entryService.edit(entryID, sheet, entry.getBeginDate(), 
        entry.getEndDate(), category, entry.getDescription(), 
        entry.getPauseMinutes(), team);
    
    return Response.ok(entry).build();
  }
}