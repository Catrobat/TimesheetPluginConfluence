package org.catrobat.confluence.servlet;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.user.GroupManager;
import com.google.common.collect.Maps;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.services.TeamService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public class TeamManagementServlet extends HttpServlet {
	private final UserAccessor userAccessor;
	private final GroupManager groupManager;
	private final UserManager userManager;
	private final LoginUriProvider loginUriProvider;
	private final TemplateRenderer templateRenderer;
	private final TeamService teamService;

	public TeamManagementServlet(UserAccessor userAccessor, GroupManager groupManager, UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer templateRenderer, TeamService teamService)
	{
		this.userAccessor = userAccessor;
		this.groupManager = groupManager;
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.templateRenderer = templateRenderer;
		this.teamService = checkNotNull(teamService);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		UserKey userKey = userManager.getRemoteUserKey(request);
		UserProfile userProfile = userManager.getUserProfile(userKey);
		String loggedInUsername = (userProfile != null) ? userProfile.getUsername() : null;

		if (loggedInUsername == null)
		{
			redirectToLogin(request, response);
		}
		else if (userManager.isSystemAdmin(userKey))
		{
			Map<String, Object> paramMap = Maps.newHashMap();
			Map<String, Object> entryMap = Maps.newTreeMap();

			for (Team team : teamService.all()) {
				String html = "<div>";
				html += team.getTeamName();
				html += "</div>";
				entryMap.put(team.getID() + "", html);
				System.out.println(team.getID() + ": " + team.getTeamName());
			}

			paramMap.put("entries", entryMap);
			response.setContentType("text/html;charset=utf-8");
			templateRenderer.render("teams.vm", paramMap, response.getWriter());
		}
		else
		{
			Map<String, Object> paramMap = Maps.newHashMap();
			paramMap.put("messageWithHtml", "You don't have sufficient permission to access this page!");
			response.setContentType("text/html;charset=utf-8");
			templateRenderer.render("error.vm", paramMap, response.getWriter());
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		final String teamName = request.getParameter("team");
		if(!teamName.isEmpty())
			teamService.add(teamName);

		resp.sendRedirect(request.getContextPath() + "/plugins/servlet/teams");
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
