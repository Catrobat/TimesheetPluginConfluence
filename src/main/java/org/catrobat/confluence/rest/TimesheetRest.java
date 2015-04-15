/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.catrobat.confluence.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/")
@Produces({MediaType.APPLICATION_JSON})
public class TimesheetRest {
    @GET
    @Path("timesheets")
    public Response getUncompletedUsers() {
        return Response.ok("hello world").build();
    }
}