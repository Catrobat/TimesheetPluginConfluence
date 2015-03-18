package org.catrobat.confluence.timesheet;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.user.Group;
import com.atlassian.user.GroupManager;
//import com.atlassian.user.impl.ldap.LDAPGroupManagerReadOnly;
import com.atlassian.user.search.page.Pager;
import com.google.common.collect.Maps;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Map;

public class TestServlet extends HttpServlet {

	private final UserAccessor userAccessor;
	private final GroupManager groupManager;
	//private final LDAPGroupManagerReadOnly ldapManager;
	private final UserManager userManager;
	private final LoginUriProvider loginUriProvider;
	private final TemplateRenderer templateRenderer;
	//private final ActiveObjects ao;

	public TestServlet(UserAccessor userAccessor, GroupManager groupManager, UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer templateRenderer)
	{
		//this.ao = checkNotNull(ao);
		this.userAccessor = userAccessor;
		this.groupManager = groupManager;
		//this.ldapManager = ldapManager;
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.templateRenderer = templateRenderer;
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
			response.setContentType("text/html;charset=utf-8");
			PrintWriter writer = response.getWriter();
			writer.write("<h1>Test</h1>");
			try {
				Pager<Group> groupsPager = userAccessor.getGroups(userAccessor.getUserByName(loggedInUsername));
				for (Group group : groupsPager) {
					writer.write(group.getName() + "<br>");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			writer.close();
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
