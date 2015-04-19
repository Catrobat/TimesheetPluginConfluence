package org.catrobat.confluence.timesheet;

import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.Maps;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;

public class TimesheetServlet extends HttpServlet {

  private final UserManager userManager;
  private final LoginUriProvider loginUriProvider;
  private final TemplateRenderer templateRenderer;
  //private final ActiveObjects ao;
//  private final TimesheetEntryService timesheetEntryService;

//  public TimesheetServlet(UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer templateRenderer, TimesheetEntryService timesheetEntryService) {
  public TimesheetServlet(UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer templateRenderer) {
    //this.ao = checkNotNull(ao);
    this.userManager = userManager;
    this.loginUriProvider = loginUriProvider;
    this.templateRenderer = templateRenderer;
//    this.timesheetEntryService = checkNotNull(timesheetEntryService);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    
    UserKey userKey = userManager.getRemoteUserKey(request);
    UserProfile userProfile = userManager.getUserProfile(userKey);
    String loggedInUsername = (userProfile != null) ? userProfile.getUsername() : null;

    if (loggedInUsername == null) {
      redirectToLogin(request, response);
      return;
    } else if (userManager.isSystemAdmin(userKey)) {
      System.out.println("SHOW ADMIN PAGE");
    }

    String username = getQueryUsername(request);
    System.out.println("username: " + username);

    if (username != null && userManager.getUserProfile(username) == null) {
      Map<String, Object> paramMap = Maps.newHashMap();
      paramMap.put("messageWithHtml", "User does not exist!");
      response.setContentType("text/html;charset=utf-8");
      templateRenderer.render("error.vm", paramMap, response.getWriter());
      return;
    } else {
      username = loggedInUsername;
    }

    Map<String, Object> paramMap = Maps.newHashMap();
    Map<String, Object> entryMap = Maps.newHashMap();
    
//    for (TimesheetEntry entry : timesheetEntryService.allForUser(username)) {
//      String data = "<td>" + entry.getDate() + "</td><td>"
//          + entry.getStartTime() + "</td><td>"
//          + entry.getEndTime() + "</td><td>"
//          + entry.getDuration() + "</td><td>"
//          + entry.getPause() + "</td><td>"
//          + entry.isTheory() + "</td><td>"
//          + entry.getDescription() + "</td><td>"
//          + entry.getCategory() + "</td>"
//          + "<td><!--save button--></td>\n";
//      entryMap.put(entry.getID() + "", data);
//    }
    
    String fullname = userProfile.getFullName();
    paramMap.put("username", fullname.isEmpty() ? username : fullname);
    paramMap.put("entries", entryMap);
    response.setContentType("text/html;charset=utf-8");
    templateRenderer.render("timesheet.vm", paramMap, response.getWriter());
  }

//  @Override
//  protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
//    final String date = request.getParameter("date");
//    final String startTime = request.getParameter("startTime");
//    final String endTime = request.getParameter("endTime");
//    final String duration = request.getParameter("duration");
//    final String pause = request.getParameter("pause");
//    final boolean theory = request.getParameter("theory") != null;
//    final String description = request.getParameter("description");
//    final String category = request.getParameter("category");
//
//    UserKey userKey = userManager.getRemoteUserKey(request);
//    UserProfile userProfile = userManager.getUserProfile(userKey);
//    String username = getQueryUsername(request);
//    if (username == null && userProfile != null) {
//      username = userProfile.getUsername();
//    }
//
//    timesheetEntryService.add(date, startTime, endTime, duration, pause, theory, description, category, username);
//
//    resp.sendRedirect(request.getContextPath() + "/plugins/servlet/timesheet/" + username);
//  }

  private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
  }

  private URI getUri(HttpServletRequest request) {
    StringBuffer builder = request.getRequestURL();
    if (request.getQueryString() != null) {
      builder.append("?");
      builder.append(request.getQueryString());
    }
    return URI.create(builder.toString());
  }

  private String getQueryUsername(HttpServletRequest request) {
    String requestUri = request.getRequestURI();
    String servletPath = request.getServletPath();
    String[] split = requestUri.split(servletPath + "/");
    return (split.length > 1) ? split[1] : null;
  }
}
