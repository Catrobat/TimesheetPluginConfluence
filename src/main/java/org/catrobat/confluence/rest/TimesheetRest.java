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

package org.catrobat.confluence.rest;

import com.atlassian.confluence.core.service.NotAuthorizedException;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.catrobat.confluence.activeobjects.*;
import org.catrobat.confluence.rest.json.*;
import org.catrobat.confluence.services.*;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
  private final AdminHelperConfigService configService;

  public TimesheetRest(TimesheetEntryService es, TimesheetService ss,
                       CategoryService cs, UserManager um, TeamService ts,
                       PermissionService ps, DBFillerService df, final AdminHelperConfigService ahcs) {
    this.userManager = um;
    this.teamService = ts;
    this.entryService = es;
    this.sheetService = ss;
    this.categoryService = cs;
    this.permissionService = ps;
    this.dbfiller = df;
    this.configService = ahcs;
  }

  private void checkIfCategoryIsAssociatedWithTeam(@Nullable Team team, @Nullable Category category) {

    if (team == null) {
      throw new NotAuthorizedException("Team not found.");
    }

    if (category == null) {
      throw new NotAuthorizedException("Category not found.");
    }

    if (!Arrays.asList(team.getCategories()).contains(category)) {
      throw new NotAuthorizedException("Category is not associated with Team.");
    }
  }

  @GET
  @Path("helloworld")
  public Response doHelloWorld() {
    return Response.ok("Hello World").build();
  }

  @GET
  @Path("teams")
  public Response getTeams(@Context HttpServletRequest request) {

    List<JsonTeam> teams = new LinkedList<JsonTeam>();
    UserProfile user;

    try {
      user = permissionService.checkIfUserExists(request);
    } catch (NotAuthorizedException e) {
      return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
    }

    String userName = user.getUsername();

    for (Team team : teamService.getTeamsOfUser(userName)) {
      Category[] categories = team.getCategories();
      int[] categoryIDs = new int[categories.length];
      for (int i = 0; i < categories.length; i++) {
        categoryIDs[i] = categories[i].getID();
      }
      teams.add(new JsonTeam(team.getID(), team.getTeamName(), categoryIDs));
    }

    return Response.ok(teams).build();
  }

  @GET
  @Path("categories")
  public Response getCategories(@Context HttpServletRequest request) {

    List<JsonCategory> categories = new LinkedList<JsonCategory>();

    for (Category category : categoryService.all()) {
      categories.add(new JsonCategory(category.getID(), category.getName()));
    }

    return Response.ok(categories).build();
  }

  @GET
  @Path("timesheetID/fromUser/{userName}")
  public Response getTimesheetIDForUser(@Context HttpServletRequest request,
                                        @PathParam("userName") String userName) {

    Timesheet sheet;
    UserProfile user;

    try {
      user = permissionService.checkIfUsernameExists(userName);
      sheet = sheetService.getTimesheetByUser(user.getUserKey().getStringValue());
    } catch (NotAuthorizedException e) {
      return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
    }


    if (sheet == null || !permissionService.userCanViewTimesheet(user, sheet)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    return Response.ok(sheet.getID()).build();
  }

  @GET
  @Path("timesheets/owner/{timesheetID}")
  public Response getTimesheetOwner(@Context HttpServletRequest request,
                                    @PathParam("timesheetID") int timesheetID) {

    Timesheet sheet;
    UserProfile user;

    try {
      user = permissionService.checkIfUserExists(request);
      sheet = sheetService.getTimesheetByID(timesheetID);
    } catch (NotAuthorizedException e) {
      return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
    }

    if (sheet == null || !permissionService.userCanViewTimesheet(user, sheet)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    JsonUser jsonUser = new JsonUser();
    jsonUser.setUserName(user.getUsername());

    return Response.ok(jsonUser).build();
  }

  @GET
  @Path("timesheets/{timesheetID}")
  public Response getTimesheet(@Context HttpServletRequest request,
                               @PathParam("timesheetID") int timesheetID) {

    Timesheet sheet;
    UserProfile user;

    try {
      user = permissionService.checkIfUserExists(request);
      sheet = sheetService.getTimesheetByID(timesheetID);
    } catch (NotAuthorizedException e) {
      return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
    }

    if (sheet == null || !permissionService.userCanViewTimesheet(user, sheet)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    JsonTimesheet jsonTimesheet = new JsonTimesheet(timesheetID,
            sheet.getTargetHoursPractice(), sheet.getTargetHoursTheory(),
            sheet.getLecture(), sheet.getIsActive());

    return Response.ok(jsonTimesheet).build();
  }

  @GET
  @Path("coordinator/{timesheetID}/entries")
  public Response getTimesheetEntriesCoordinator(@Context HttpServletRequest request,
                                                 @PathParam("timesheetID") int timesheetID) {

    Timesheet sheet;

    try {
      sheet = sheetService.getTimesheetByID(timesheetID);
    } catch (NotAuthorizedException e) {
      return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
    }

    if (sheet == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    TimesheetEntry[] entries = entryService.getEntriesBySheet(sheet);

    List<JsonTimesheetEntry> jsonEntries = new ArrayList<JsonTimesheetEntry>(entries.length);

    for (TimesheetEntry entry : entries) {
      jsonEntries.add(new JsonTimesheetEntry(entry.getID(), entry.getBeginDate(),
              entry.getEndDate(), entry.getPauseMinutes(),
              entry.getDescription(), entry.getTeam().getID(),
              entry.getCategory().getID(), entry.getIsGoogleDocImport()));
    }
    return Response.ok(jsonEntries).build();
  }

  @GET
  @Path("coordinator/{timesheetID}")
  public Response getTimesheetCoordinator(@Context HttpServletRequest request,
                                          @PathParam("timesheetID") int timesheetID) {

    Timesheet sheet;

    try {
      sheet = sheetService.getTimesheetByID(timesheetID);
    } catch (NotAuthorizedException e) {
      return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
    }

    if (sheet == null) {
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

    Timesheet sheet;
    UserProfile user;

    try {
      user = permissionService.checkIfUserExists(request);
      sheet = sheetService.getTimesheetByID(timesheetID);
    } catch (NotAuthorizedException e) {
      return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
    }

    if (sheet == null || !permissionService.userCanViewTimesheet(user, sheet)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    TimesheetEntry[] entries = entryService.getEntriesBySheet(sheet);

    List<JsonTimesheetEntry> jsonEntries = new ArrayList<JsonTimesheetEntry>(entries.length);

    for (TimesheetEntry entry : entries) {
      jsonEntries.add(new JsonTimesheetEntry(entry.getID(), entry.getBeginDate(),
              entry.getEndDate(), entry.getPauseMinutes(),
              entry.getDescription(), entry.getTeam().getID(),
              entry.getCategory().getID(), entry.getIsGoogleDocImport()));
    }
    return Response.ok(jsonEntries).build();
  }

  @POST
  @Path("timesheets/{timesheetID}/entry")
  public Response postTimesheetEntry(@Context HttpServletRequest request,
                                     final JsonTimesheetEntry entry, @PathParam("timesheetID") int timesheetID) {

    Timesheet sheet;
    UserProfile user;
    Category category;
    Team team;

    try {
      user = permissionService.checkIfUserExists(request);
      sheet = sheetService.getTimesheetByID(timesheetID);
      category = categoryService.getCategoryByID(entry.getCategoryID());
      team = teamService.getTeamByID(entry.getTeamID());
      checkIfCategoryIsAssociatedWithTeam(team, category);
      permissionService.userCanEditTimesheetEntry(user, sheet, entry);
    } catch (NotAuthorizedException e) {
      return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
    }

    TimesheetEntry newEntry = entryService.add(sheet, entry.getBeginDate(),
            entry.getEndDate(), category, entry.getDescription(),
            entry.getPauseMinutes(), team, entry.getIsGoogleDocImport());

    entry.setEntryID(newEntry.getID());

    return Response.ok(entry).build();
  }

  @POST
  @Path("timesheets/{timesheetID}/entries")
  public Response postTimesheetEntries(@Context HttpServletRequest request,
                                       final JsonTimesheetEntry[] entries, @PathParam("timesheetID") int timesheetID) {

    Timesheet sheet;
    UserProfile user;

    try {
      user = permissionService.checkIfUserExists(request);
      sheet = sheetService.getTimesheetByID(timesheetID);
    } catch (NotAuthorizedException e) {
      return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
    }

    List<JsonTimesheetEntry> newEntries = new LinkedList<JsonTimesheetEntry>();
    List<String> errorMessages = new LinkedList<String>();

    for (JsonTimesheetEntry entry : entries) {
      try {
        permissionService.userCanEditTimesheetEntry(user, sheet, entry);
        Category category = categoryService.getCategoryByID(entry.getCategoryID());
        Team team = teamService.getTeamByID(entry.getTeamID());
        checkIfCategoryIsAssociatedWithTeam(team, category);

        TimesheetEntry newEntry = entryService.add(sheet, entry.getBeginDate(),
                entry.getEndDate(), category, entry.getDescription(),
                entry.getPauseMinutes(), team, entry.getIsGoogleDocImport());

        entry.setEntryID(newEntry.getID());
        newEntries.add(entry);

      } catch (NotAuthorizedException e) {
        errorMessages.add(entry.toReadableString() + ": " + e.getMessage());
      }
    }

    JsonTimesheetEntries jsonNewEntries = new JsonTimesheetEntries(
            newEntries.toArray(new JsonTimesheetEntry[newEntries.size()]),
            errorMessages.toArray(new String[errorMessages.size()])
    );

    return Response.ok(jsonNewEntries).build();
  }

  @POST
  @Path("timesheets/{timesheetID}/changeHours")
  public Response postTimesheetHours(@Context HttpServletRequest request,
                                     final JsonTimesheet jsonTimesheet, @PathParam("timesheetID") int timesheetID) {

    Timesheet sheet;
    UserProfile user;

    try {
      user = permissionService.checkIfUserExists(request);
      sheet = sheetService.getTimesheetByID(timesheetID);
    } catch (NotAuthorizedException e) {
      return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
    }

    if (sheet == null || !permissionService.userCanViewTimesheet(user, sheet)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    sheetService.editTimesheet(user.getUserKey().getStringValue(), jsonTimesheet.getTargetHourPractice(),
            jsonTimesheet.getTargetHourTheory(), jsonTimesheet.getLectures());

    JsonTimesheet newJsonTimesheet = new JsonTimesheet(sheet.getID(),
            sheet.getTargetHoursPractice(), sheet.getTargetHoursTheory(),
            sheet.getLecture(), sheet.getIsActive());

    return Response.ok(newJsonTimesheet).build();
  }

  @PUT
  @Path("entries/{entryID}")
  public Response putTimesheetEntry(@Context HttpServletRequest request,
                                    final JsonTimesheetEntry jsonEntry, @PathParam("entryID") int entryID) {
    UserProfile user;
    TimesheetEntry entry;
    Category category;
    Team team;

    try {
      user = permissionService.checkIfUserExists(request);
      entry = entryService.getEntryByID(entryID);
      category = categoryService.getCategoryByID(jsonEntry.getCategoryID());
      team = teamService.getTeamByID(jsonEntry.getTeamID());
      checkIfCategoryIsAssociatedWithTeam(team, category);
      permissionService.userCanEditTimesheetEntry(user, entry.getTimeSheet(), jsonEntry);
    } catch (NotAuthorizedException e) {
      return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
    }

    entryService.edit(entryID, entry.getTimeSheet(), jsonEntry.getBeginDate(),
            jsonEntry.getEndDate(), category, jsonEntry.getDescription(),
            jsonEntry.getPauseMinutes(), team, jsonEntry.getIsGoogleDocImport());

    return Response.ok(jsonEntry).build();
  }

  @DELETE
  @Path("entries/{entryID}")
  public Response deleteTimesheetEntry(@Context HttpServletRequest request,
                                       @PathParam("entryID") int entryID) {
    UserProfile user;
    TimesheetEntry entry;

    try {
      user = permissionService.checkIfUserExists(request);
      entry = entryService.getEntryByID(entryID);
      permissionService.userCanDeleteTimesheetEntry(user, entry);
    } catch (NotAuthorizedException e) {
      return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
    }
    entryService.delete(entry);
    return Response.ok().build();
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
      return Response.ok("cleanandinitdb").build();
    } else {
      return Response.ok("you're not admin").build();
    }
  }
}