/*
 * Copyright 2016 Adrian Schnedlitz
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
package org.catrobat.confluence.servlet;

import com.atlassian.confluence.core.service.NotAuthorizedException;
import com.atlassian.confluence.renderer.template.TemplateRenderer;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.user.util.Assert;
import com.google.common.collect.Maps;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.services.PermissionService;
import org.catrobat.confluence.services.TimesheetService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Map;

public class VisualizationServlet extends HttpServlet {

    private final LoginUriProvider loginUriProvider;

    @Autowired
    public void setRenderer(TemplateRenderer renderer) {
        this.renderer = renderer;
    }

    private TemplateRenderer renderer;
    private final TimesheetService sheetService;
    private final PermissionService permissionService;

    public VisualizationServlet(LoginUriProvider loginUriProvider, TimesheetService sheetService, PermissionService permissionService) {
        this.loginUriProvider = loginUriProvider;
        this.sheetService = sheetService;
        this.permissionService = permissionService;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            UserProfile userProfile = permissionService.checkIfUserExists(request);
            Assert.notNull(userProfile, "userProfile is NULL!"); // Okey, userProfile is Null, I don't know why
            String username = userProfile.getUsername();
            Assert.notNull(username, "username is NULL!");
            Timesheet timesheet = sheetService.getTimesheetByUser(username);
            Assert.notNull(timesheet, "timesheet is NULL!");

            if (timesheet == null) {
                timesheet = sheetService.add(username, 150, 0, "Confluence Timesheet");
            }

            Map<String, Object> paramMap = Maps.newHashMap();
            paramMap.put("timesheetid", timesheet.getID());
            response.setContentType("text/html;charset=utf-8");
            //TODO: fix it: argumente sind falsch!
            //renderer.render("visualization.vm", paramMap, response.getWriter());

            response.setContentType("text/html");
            PrintWriter out = response.getWriter();

            out.println("<html>");
            out.println("<head>");
            out.println("<title>Hola</title>");
            out.println("</head>");
            out.println("<body bgcolor=\"white\">");
            out.println("Der Render sollte hier die Visualisierung anzeigen!");
            out.println("</body>");
            out.println("</html>");

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
