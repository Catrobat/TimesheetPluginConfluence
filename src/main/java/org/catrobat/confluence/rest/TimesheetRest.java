/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.catrobat.confluence.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import org.catrobat.confluence.rest.json.JsonTimesheetEntry;

@Path("/")
@Produces({MediaType.APPLICATION_JSON})
public class TimesheetRest {
    @GET
    @Path("timesheets")
    public Response doHelloWorld() {
        return Response.ok("hello world").build();
    }

    @POST
    @Path("timesheets/{timesheetID}/entries")
    public Response postTimesheetEntry(final JsonTimesheetEntry entry, @PathParam("timesheetID") String timesheetID) {
      return Response.ok(entry).build();
    }
    
    @PUT
    @Path("timesheets/{timesheetID}/entries/{entryID}")
    public Response postTimesheetEntry(final JsonTimesheetEntry entry, @PathParam("timesheetID") String timesheetID, @PathParam("entryID") String entryID) {
      return Response.ok(entry).build();
    }
}