/*
 * Copyright 2014 Stephan Fellhofer
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


import com.atlassian.confluence.api.model.people.User;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.user.GroupManager;

import com.atlassian.sal.api.user.UserManager;
import org.catrobat.confluence.activeobjects.AdminHelperConfigService;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.rest.json.JsonConfig;
import org.catrobat.confluence.rest.json.JsonResource;
import org.catrobat.confluence.rest.json.JsonTeam;
import org.catrobat.confluence.services.PermissionService;
import org.catrobat.confluence.services.TeamService;
import org.catrobat.confluence.services.impl.PermissionServiceImpl;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GitHub;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Path("/config")
public class ConfigResourceRest extends PermissionServiceImpl {
  private final AdminHelperConfigService configService;
  private final DirectoryManager directoryManager;
  private final UserAccessor userAccessor;
  private final TeamService teamService;

  public ConfigResourceRest(final UserManager userManager, final AdminHelperConfigService configService,
                            final DirectoryManager directoryManager, final TeamService teamService,
                            final UserAccessor userAccessor) {
    super(userManager, teamService, configService, userAccessor);
    this.configService = configService;
    this.directoryManager = directoryManager;
    this.teamService = teamService;
    this.userAccessor = userAccessor;
  }

  @GET
  @Path("/getConfig")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getConfig(@Context HttpServletRequest request) {
    Response unauthorized = checkPermission(request);
    if (unauthorized != null) {
      return unauthorized;
    }

    return Response.ok(new JsonConfig(configService)).build();
  }

  @GET
  @Path("/getDirectories")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getDirectories(@Context HttpServletRequest request) {
    Response unauthorized = checkPermission(request);
    if (unauthorized != null) {
      return unauthorized;
    }

    List<Directory> directoryList = directoryManager.findAllDirectories();
    List<JsonConfig> jsonDirectoryList = new ArrayList<JsonConfig>();
    for (Directory directory : directoryList) {
      JsonConfig config = new JsonConfig();
      config.setUserDirectoryId(directory.getId());
      config.setUserDirectoryName(directory.getName());
      jsonDirectoryList.add(config);
    }

    return Response.ok(jsonDirectoryList).build();
  }

  @GET
  @Path("/getTeamList")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTeamList(@Context HttpServletRequest request) {
    Response unauthorized = checkPermission(request);
    if (unauthorized != null) {
      return unauthorized;
    }

    List<String> teamList = new ArrayList<String>();
    for (Team team : configService.getConfiguration().getTeams()) {
      teamList.add(team.getTeamName());
    }

    return Response.ok(teamList).build();
  }

  @PUT
  @Path("/saveConfig")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response setConfig(final JsonConfig jsonConfig, @Context HttpServletRequest request) {
    Response unauthorized = checkPermission(request);
    if (unauthorized != null) {
      return unauthorized;
    }

    if (jsonConfig.getGithubToken() != null && jsonConfig.getGithubToken().length() != 0) {
      configService.setApiToken(jsonConfig.getGithubToken());
    }
    configService.setPublicApiToken(jsonConfig.getGithubTokenPublic());
    configService.setOrganisation(jsonConfig.getGithubOrganization());
    configService.setUserDirectoryId(jsonConfig.getUserDirectoryId());
    configService.editMail(jsonConfig.getMailFromName(), jsonConfig.getMailFrom(),
            jsonConfig.getMailSubject(), jsonConfig.getMailBody());

    /*
    for (JsonResource jsonResource : jsonConfig.getResources()) {
      configService.editResource(jsonResource.getResourceName(), jsonResource.getGroupName());
    }
    */

    if (jsonConfig.getApprovedGroups() != null) {
      configService.clearApprovedGroups();
      for (String approvedGroupName : jsonConfig.getApprovedGroups()) {
        configService.addApprovedGroup(approvedGroupName);
      }
    }

    /*
    com.atlassian.jira.user.util.UserManager userManager = ComponentAccessor.getUserManager();
    if (jsonConfig.getApprovedUsers() != null) {
      configService.clearApprovedUsers();
      for (String approvedUserName : jsonConfig.getApprovedUsers()) {
        ApplicationUser user = jiraUserManager.getUserByName(approvedUserName);
        if (user != null) {
          configService.addApprovedUser(user.getKey());
        }
      }
    }
    */

    if (jsonConfig.getTeams() != null) {
      String token = configService.getConfiguration().getGithubApiToken();
      String organizationName = configService.getConfiguration().getGithubOrganisation();

      try {
        GitHub gitHub = GitHub.connectUsingOAuth(token);
        GHOrganization organization = gitHub.getOrganization(organizationName);
        Collection<GHTeam> teamList = organization.getTeams().values();

        if (jsonConfig.getDefaultGithubTeam() != null) {
          for (GHTeam team : teamList) {
            if (jsonConfig.getDefaultGithubTeam().toLowerCase().equals(team.getName().toLowerCase())) {
              configService.setDefaultGithubTeamId(team.getId());
              break;
            }
          }
        }

        for (JsonTeam jsonTeam : jsonConfig.getTeams()) {
          configService.removeTeam(jsonTeam.getTeamName());

          List<Integer> githubIdList = new ArrayList<Integer>();
          for (String teamName : jsonTeam.getGithubTeams()) {
            for (GHTeam team : teamList) {
              if (teamName.toLowerCase().equals(team.getName().toLowerCase())) {
                githubIdList.add(team.getId());
                break;
              }
            }
          }

          configService.addTeam(jsonTeam.getTeamName(), githubIdList, jsonTeam.getCoordinatorGroups(),
                  jsonTeam.getSeniorGroups(), jsonTeam.getDeveloperGroups());
        }
      } catch (IOException e) {
        e.printStackTrace();
        return Response.serverError().entity("Some error with GitHub API (e.g. maybe wrong tokens, organisation, teams) occured").build();
      }
    }

    return Response.noContent().build();
  }

  @PUT
  @Path("/addTeam")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response addTeam(final String modifyTeam, @Context HttpServletRequest request) {
    Response unauthorized = checkPermission(request);
    if (unauthorized != null) {
      return unauthorized;
    }

    boolean successful = configService.addTeam(modifyTeam, null, null, null, null) != null;

    if (successful)
      return Response.noContent().build();

    return Response.serverError().build();
  }

  @PUT
  @Path("/editTeam")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response editTeam(final String[] teams, @Context HttpServletRequest request) {
    Response unauthorized = checkPermission(request);
    if (unauthorized != null) {
      return unauthorized;
    }

    if (teams == null || teams.length != 2) {
      return Response.serverError().build();
    } else if (teams[1].trim().length() == 0) {
      return Response.serverError().entity("Team name must not be empty").build();
    }

    boolean successful = configService.editTeam(teams[0], teams[1]) != null;

    if (successful)
      return Response.noContent().build();

    return Response.serverError().build();
  }

  @PUT
  @Path("/removeTeam")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response removeTeam(final String modifyTeam, @Context HttpServletRequest request) {
    Response unauthorized = checkPermission(request);
    if (unauthorized != null) {
      return unauthorized;
    }

    boolean successful = configService.removeTeam(modifyTeam) != null;

    if (successful)
      return Response.noContent().build();

    return Response.serverError().build();
  }

  @PUT
  @Path("/addResource")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response addResource(final String resourceName, @Context HttpServletRequest request) {
    Response unauthorized = checkPermission(request);
    if (unauthorized != null) {
      return unauthorized;
    }

    if (resourceName == null || resourceName.trim().length() == 0) {
      return Response.serverError().entity("Resource-Name must not be empty").build();
    }
    /*
    boolean successful = configService.addResource(resourceName, null) != null;

    if (successful)
      return Response.noContent().build();
    */

    return Response.serverError().entity("Maybe name already taken?").build();
  }

  @PUT
  @Path("/removeResource")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response removeResource(final String resourceName, @Context HttpServletRequest request) {
    Response unauthorized = checkPermission(request);
    if (unauthorized != null) {
      return unauthorized;
    }

    if (resourceName == null || resourceName.trim().length() == 0) {
      return Response.serverError().entity("Resource-Name must not be empty").build();
    }

    /*
    boolean successful = configService.removeResource(resourceName) != null;

    if (successful)
      return Response.noContent().build();
    */

    return Response.serverError().entity("Maybe no resource with given name?").build();
  }
}