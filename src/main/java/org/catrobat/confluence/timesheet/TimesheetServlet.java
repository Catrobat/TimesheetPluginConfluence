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
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.services.TimesheetService;

public class TimesheetServlet extends HttpServlet {

  private final UserManager userManager;
  private final LoginUriProvider loginUriProvider;
  private final TemplateRenderer templateRenderer;
  private final TimesheetService sheetService; 

  public TimesheetServlet(UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer templateRenderer, TimesheetService sheetService) {
    this.userManager = userManager;
    this.loginUriProvider = loginUriProvider;
    this.templateRenderer = templateRenderer;
    this.sheetService = sheetService;
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

    Map<String, Object> paramMap = Maps.newHashMap();
    Timesheet sheet = sheetService.getTimesheetByUser(userKey.getStringValue());
    paramMap.put("timesheetid", sheet.getID());
    response.setContentType("text/html;charset=utf-8");
    templateRenderer.render("timesheet.vm", paramMap, response.getWriter());
  }

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

}
