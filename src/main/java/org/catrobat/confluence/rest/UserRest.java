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

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.core.user.preferences.UserPreferences;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.manager.directory.DirectoryPermissionException;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.util.UserUtils;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.SingleMailQueueItem;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.user.Group;
import com.atlassian.user.GroupManager;
import com.atlassian.user.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.catrobat.confluence.activeobjects.AdminHelperConfig;
import org.catrobat.confluence.activeobjects.AdminHelperConfigService;
import org.catrobat.confluence.rest.json.JsonConfig;
import org.catrobat.confluence.rest.json.JsonUser;
import org.catrobat.confluence.services.TeamService;
import org.catrobat.confluence.services.impl.PermissionServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Path("/user")
public class UserRest extends PermissionServiceImpl {
  public static final String DISABLED_GROUP = "Disabled";
  private final AdminHelperConfigService configService;
  private final DirectoryManager directoryManager;
  private final UserAccessor userAccessor;
  private final UserManager userManager;

  public UserRest(final UserManager userManager, final AdminHelperConfigService configService, final TeamService teamService,
                  final DirectoryManager directoryManager, final UserAccessor userAccessor) {
    super(userManager, teamService, configService, userAccessor);
    this.configService = configService;
    this.directoryManager = directoryManager;
    this.userAccessor = userAccessor;
    this.userManager = userManager;
  }

  @PUT
  @Path("/createUser")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(final JsonUser jsonUser, @Context HttpServletRequest request) {
    Response unauthorized = checkPermission(request);
    if (unauthorized != null) {
      return unauthorized;
    }

    if (jsonUser.getFirstName() == null || jsonUser.getLastName() == null ||
            jsonUser.getUserName() == null || !UserUtils.isValidEmail(jsonUser.getEmail())) {
      return Response.serverError().entity("Please check all input fields.").build();
    }

    if (checkIfUserExists(jsonUser.getUserName()) == true) {
      return Response.serverError().entity("User already exists!").build();
    }

    JsonConfig config = new JsonConfig(configService);

    try {
      UserTemplate user = new UserTemplate(jsonUser.getUserName(), config.getUserDirectoryId());
      user.setFirstName(jsonUser.getFirstName());
      user.setLastName(jsonUser.getLastName());
      user.setEmailAddress(jsonUser.getEmail());
      user.setDisplayName(jsonUser.getFirstName() + " " + jsonUser.getLastName());
      user.setActive(true);
      String password = RandomStringUtils.random(16, 0, 0, true, true, null, new SecureRandom());
      PasswordCredential credential = new PasswordCredential(password);
      directoryManager.addUser(config.getUserDirectoryId(), user, credential);
      sendEmail(jsonUser.getFirstName(), jsonUser.getUserName(), jsonUser.getEmail(), password);
    } catch (DirectoryNotFoundException e) {
      e.printStackTrace();
      return Response.serverError().entity("User-Directory was not found. Please check the settings!").build();
    } catch (InvalidCredentialException e) {
      e.printStackTrace();
      return Response.serverError().entity("User's credential do not meet requirements of directory.").build();
    } catch (InvalidUserException e) {
      e.printStackTrace();
      return Response.serverError().entity("Required user properties are not given.").build();
    } catch (UserAlreadyExistsException e) {
      e.printStackTrace();
      return Response.serverError().entity("User already exists in this directory!").build();
    } catch (OperationFailedException e) {
      e.printStackTrace();
      return Response.serverError().entity("Operation failed on directory implementation.").build();
    } catch (DirectoryPermissionException e) {
      e.printStackTrace();
      return Response.serverError().entity("Not enough permissions to create user in directory").build();
    }

    if (checkIfUserExists(jsonUser.getUserName()) == false) {
      return Response.serverError().entity("User creation failed.").build();
    }

    return Response.ok().build();
  }

  private void sendEmail(String name, String username, String emailAddress, String password) {
    AdminHelperConfig config = configService.getConfiguration();

    Email email = new Email(emailAddress);
    email.setFromName(config.getMailFromName() != null && config.getMailFromName().length() != 0 ?
            config.getMailFromName() : "Confluence Administrators");
    email.setFrom(config.getMailFrom() != null && config.getMailFrom().length() != 0 ?
            config.getMailFrom() : "no-reply@catrob.at");
    email.setEncoding("UTF-8");
    email.setMimeType("text/plain");
    email.setSubject(config.getMailSubject() != null && config.getMailSubject().length() != 0
            ? config.getMailSubject() : "Welcome to Catrobat");

    String body = config.getMailBody();
    if (body == null || body.length() == 0) {
      email.setBody("Hi " + name + ",\n" +
              "Your account has been created and you may login to Jira " +
              "(https://jira.catrob.at/) and other resources with following credentials:\n\n" +
              "Username: \t" + username + "\n" +
              "Password: \t" + password + "\n\n" +
              "Best regards,\n" +
              "Catrobat-Admins");
    } else {
      body = body.replaceAll("\\{\\{name\\}\\}", name);
      body = body.replaceAll("\\{\\{username\\}\\}", username);
      body = body.replaceAll("\\{\\{password\\}\\}", password);
      email.setBody(body);
    }

    SingleMailQueueItem item = new SingleMailQueueItem(email);

    //ToDo: check if this is possible
    try {
      item.send();
    } catch (MailException e) {
      e.printStackTrace();
    }
  }

  @GET
  @Path("/getUsers")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUsers(@Context HttpServletRequest request) {
    Response unauthorized = checkPermission(request);
    if (unauthorized != null) {
      return unauthorized;
    }

    List<JsonUser> jsonUserList = new ArrayList<JsonUser>();

    List<User> allUsers = userAccessor.getUsersWithConfluenceAccessAsList();
    for (User user : allUsers) {

      if (userIsAdmin(user.getFullName())) {
        continue;
      }

      JsonUser jsonUser = new JsonUser();
      jsonUser.setEmail(user.getEmail());
      jsonUser.setUserName(user.getName());

      String displayName = user.getFullName();
      int lastSpaceIndex = displayName.lastIndexOf(' ');
      if (lastSpaceIndex >= 0) {
        jsonUser.setFirstName(displayName.substring(0, lastSpaceIndex));
        jsonUser.setLastName(displayName.substring(lastSpaceIndex + 1));
      } else {
        jsonUser.setFirstName(displayName);
      }

      boolean isActive = true;
      for (Group group : userAccessor.getGroupsAsList(user)) {
        if (group.getName().toLowerCase().equals(DISABLED_GROUP.toLowerCase())) {
          isActive = false;
          break;
        }
      }

      jsonUser.setActive(isActive);
      jsonUserList.add(jsonUser);
    }

    return Response.ok(jsonUserList).build();
  }

  @GET
  @Path("/search")
  @Produces(MediaType.APPLICATION_JSON)
  public Response searchUserGet(@QueryParam("query") String query, @Context HttpServletRequest request) {
    return searchUser(query, request);
  }

  @POST
  @Path("/search")
  @Produces(MediaType.APPLICATION_JSON)
  public Response searchUserPost(@FormParam("query") String query, @Context HttpServletRequest request) {
    return searchUser(query, request);
  }

  private Response searchUser(String query, HttpServletRequest request) {
    Response unauthorized = checkPermission(request);
    if (unauthorized != null) {
      return unauthorized;
    }
    if (query == null || query.length() < 1) {
      return Response.ok(new ArrayList<JsonUser>()).build();
    }

    query = StringEscapeUtils.escapeHtml4(query);

    TreeMap<String, JsonUser> jsonUsers = new TreeMap<String, JsonUser>();

    for (User user : userAccessor.getUsers()) {
      if (user.getName().toLowerCase().contains(query.toLowerCase()) ||
              user.getFullName().toLowerCase().contains(query.toLowerCase())) {
        JsonUser jsonUser = new JsonUser();
        jsonUser.setUserName(userManager.getRemoteUser().getUserKey().toString());
        jsonUser.setDisplayName(userManager.getRemoteUser().getFullName());
        jsonUsers.put(user.getName().toLowerCase(), jsonUser);
      }
    }

    return Response.ok(jsonUsers.values()).build();
  }

  @PUT
  @Path("/activateUser")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response activateUser(final JsonUser jsonUser, @Context HttpServletRequest request) {
    Response unauthorized = checkPermission(request);
    if (unauthorized != null) {
      return unauthorized;
    }

    if (jsonUser == null) {
      return Response.serverError().entity("User not given").build();
    }
    //ToDo: create workaround
    /*
    ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByName(jsonUser.getUserName());
    if (applicationUser == null) {
      return Response.serverError().entity("User not found").build();
    }

    if (jsonUser.getCoordinatorList().size() == 0 && jsonUser.getSeniorList().size() == 0 && jsonUser.getDeveloperList().size() == 0
            && jsonUser.getResourceList().size() == 0) {
      return Response.serverError().entity("No Team and no resource selected").build();
    }

    JsonConfig config = new JsonConfig(configService);

    // remove user from all groups (especially from DISABLED_GROUP) since he will be added to chosen groups afterwards
    try {
      removeFromAllGroups(ApplicationUsers.toDirectoryUser(applicationUser));
    } catch (RemoveException e) {
      e.printStackTrace();
      return Response.serverError().entity(e.getMessage()).build();
    } catch (PermissionException e) {
      e.printStackTrace();
      return Response.serverError().entity(e.getMessage()).build();
    }

    // add user to all desired GitHub teams and groups
    ExtendedPreferences extendedPreferences = userPreferencesManager
            .getExtendedPreferences(ApplicationUsers.from(applicationUser.getDirectoryUser()));
    jsonUser.setGithubName(extendedPreferences.getText(GITHUB_PROPERTY));
    addUserToGithubAndJiraGroups(jsonUser, applicationUser.getDirectoryUser(), config);
    */

    return Response.ok().build();
  }

  @PUT
  @Path("/inactivateUser")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response inactivateUser(final String inactivateUser, @Context HttpServletRequest request) {
    Response unauthorized = checkPermission(request);
    if (unauthorized != null) {
      return unauthorized;
    }

    if (inactivateUser == null) {
      return Response.serverError().entity("User not given").build();
    }

    //ToDo: find workaround
    /*
    ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByName(inactivateUser);
    if (applicationUser == null) {
      return Response.serverError().entity("User not found").build();
    }

    // remove user from all GitHub teams
    ExtendedPreferences extendedPreferences = userPreferencesManager.getExtendedPreferences(applicationUser);
    String githubName = extendedPreferences.getText(GITHUB_PROPERTY);
    if (githubName != null) {
      GithubHelper githubHelper = new GithubHelper(configService);
      String error = githubHelper.removeUserFromOrganization(githubName);
      if (error != null) {
        return Response.serverError().entity(error).build();
      }
    }

    // remove user from all groups and add user to DISABLED_GROUP
    try {
      removeFromAllGroups(ApplicationUsers.toDirectoryUser(applicationUser));
      Response error = addToGroups(applicationUser.getDirectoryUser(), Arrays.asList(DISABLED_GROUP));
      if (error != null) {
        return error;
      }
    } catch (NotAuthorizedException e) {
      e.printStackTrace();
      return Response.serverError().entity(e.getMessage()).build();
    } catch (PermissionException e) {
      e.printStackTrace();
      return Response.serverError().entity(e.getMessage()).build();
    } catch (UserNotFoundException e) {
      e.printStackTrace();
      return Response.serverError().entity(e.getMessage()).build();
    } catch (InvalidGroupException e) {
      e.printStackTrace();
      return Response.serverError().entity(e.getMessage()).build();
    } catch (OperationNotPermittedException e) {
      e.printStackTrace();
      return Response.serverError().entity(e.getMessage()).build();
    } catch (GroupNotFoundException e) {
      e.printStackTrace();
      return Response.serverError().entity(e.getMessage()).build();
    } catch (OperationFailedException e) {
      e.printStackTrace();
      return Response.serverError().entity(e.getMessage()).build();
    }
    */
    return Response.ok().build();
  }

  private Response addToGroups(User user, List<String> groupList) throws UserNotFoundException, OperationFailedException,
          GroupNotFoundException, OperationNotPermittedException, InvalidGroupException {
    if (user == null || groupList == null)
      return Response.serverError().entity("user/group may not be null").build();

    //ToDo: workaround
    /*
    GroupManager groupManager = ComponentAccessor.getGroupManager();
    Collection<Group> groupCollection;

    for (String groupString : groupList) {
      boolean groupExists = false;
      groupCollection = groupManager.getAllGroups();
      for (Group group : groupCollection) {
        if (group != null && group.getName() != null && groupString != null &&
                groupString.toLowerCase().equals(group.getName().toLowerCase())) {
          groupManager.addUserToGroup(user, group);
          groupExists = true;
          break;
        }
      }
      if (!groupExists) {
        return Response.serverError().entity("Group \"" + groupString + "\" does not exist").build();
      }
    }
    */
    return null; // everything ok
  }

  private void removeFromAllGroups(User user) throws PermissionException {
    if (user == null)
      return;
    //ToDo: workaround

        /*
        GroupManager groupManager = ComponentAccessor.getGroupManager();
        Collection<Group> groupCollection = groupManager.getGroupsForUser(user);

        UserUtil userUtil = ComponentAccessor.getUserUtil();
        userUtil.removeUserFromGroups(groupCollection, user);
        */
  }
}
