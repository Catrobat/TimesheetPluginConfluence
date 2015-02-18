package org.catrobat.confluence.timesheet;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static com.google.common.base.Preconditions.*;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.apache.commons.lang.StringEscapeUtils.unescapeHtml;

public class MyPluginServlet extends HttpServlet
{
	private final UserManager userManager;
	private final LoginUriProvider loginUriProvider;
	private final TemplateRenderer templateRenderer;
	private final ActiveObjects ao;
	private final TimesheetEntryService timesheetEntryService;
	
	public MyPluginServlet(UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer templateRenderer, ActiveObjects ao, TimesheetEntryService timesheetEntryService)
	{
		this.ao = checkNotNull(ao);
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.templateRenderer = templateRenderer;
		this.timesheetEntryService = checkNotNull(timesheetEntryService);
	}
		
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		UserKey userKey = userManager.getRemoteUserKey(request);
		UserProfile userProfile = userManager.getUserProfile(userKey);
		String username = userProfile.getUsername();
		if (username == null)
		{
			redirectToLogin(request, response);
			return;
		}
		else if (userManager.isSystemAdmin(userKey))
		{
			// show Adminpage
			System.out.println("SHOW ADMIN PAGE");
		}
		Map<String, Object> paramMap = Maps.newHashMap();
		Map<String, Object> entryMap = Maps.newHashMap();
		for (TimesheetEntry entry : timesheetEntryService.allForUser(username))
		{
			String data = "<td>" + entry.getDate() + "</td><td>" +
					entry.getStartTime() + "</td><td>" +
					entry.getEndTime() + "</td><td>" +
					entry.getDuration() + "</td><td>" +
					entry.getPause() + "</td><td>" +
					entry.isTheory() + "</td><td>" +
					entry.getDescription() + "</td><td>" +
					entry.getCategory() + "</td>" +
					"<td><!--save button--></td>\n";
			entryMap.put(entry.getID() + "", data);
		}
		String fullname = userProfile.getFullName();
		paramMap.put("username", fullname.isEmpty() ? username : fullname);
		paramMap.put("entries", entryMap);
		response.setContentType("text/html;charset=utf-8");
		templateRenderer.render("admin.vm", paramMap, response.getWriter());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		final String date = request.getParameter("date");
		final String startTime = request.getParameter("startTime");
		final String endTime = request.getParameter("endTime");
		final String duration = request.getParameter("duration");
		final String pause = request.getParameter("pause");
		final boolean theory = request.getParameter("theory") != null;
		final String description = request.getParameter("description");
		final String category = request.getParameter("category");

		String username = userManager.getRemoteUsername(request);

		timesheetEntryService.add(date, startTime, endTime, duration, pause, theory, description, category, username);

		resp.sendRedirect(request.getContextPath() + "/plugins/servlet/timesheet");
	}

	private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
	}
	private URI getUri(HttpServletRequest request)
	{
		StringBuffer builder = request.getRequestURL();
		if (request.getQueryString() != null)
		{
			builder.append("?");
			builder.append(request.getQueryString());
		}
		return URI.create(builder.toString());
	}
}
