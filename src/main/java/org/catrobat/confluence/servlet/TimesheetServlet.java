package org.catrobat.confluence.servlet;

import com.atlassian.confluence.core.service.NotAuthorizedException;
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
import com.atlassian.templaterenderer.TemplateRenderer;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.services.PermissionService;
import org.catrobat.confluence.services.TimesheetService;

public class TimesheetServlet extends HttpServlet {

  private final LoginUriProvider loginUriProvider;
  private final TemplateRenderer templateRenderer;
  private final TimesheetService sheetService; 
	private final PermissionService permissionService;

	public TimesheetServlet(LoginUriProvider loginUriProvider, TemplateRenderer templateRenderer, TimesheetService sheetService, PermissionService permissionService) {
		this.loginUriProvider = loginUriProvider;
		this.templateRenderer = templateRenderer;
		this.sheetService = sheetService;
		this.permissionService = permissionService;
	}

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
			UserProfile userProfile = permissionService.checkIfUserExists(request);
			String userKey = userProfile.getUserKey().getStringValue(); 
			Timesheet sheet = sheetService.getTimesheetByUser(userKey);
			
			if(sheet == null) {
				sheet = sheetService.add(userKey, 150, 0, "");
			}
			
			Map<String, Object> paramMap = Maps.newHashMap();
			paramMap.put("timesheetid", sheet.getID());
			response.setContentType("text/html;charset=utf-8");
			templateRenderer.render("timesheet.vm", paramMap, response.getWriter());
		
		} catch (NotAuthorizedException e) {
			redirectToLogin(request, response);
		}
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
