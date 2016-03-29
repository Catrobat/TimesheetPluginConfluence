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

import com.atlassian.confluence.renderer.template.TemplateRenderer;
import com.atlassian.sal.api.auth.LoginUriProvider;
import org.catrobat.confluence.services.PermissionService;
import org.catrobat.confluence.services.TimesheetService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

public class TimesheetServlet extends HttpServlet {

    private final LoginUriProvider loginUriProvider;
    private TemplateRenderer templateRenderer;
    private final TimesheetService sheetService;
    private final PermissionService permissionService;

    public TimesheetServlet(LoginUriProvider loginUriProvider, TemplateRenderer templateRenderer, TimesheetService sheetService, PermissionService permissionService) {
        this.loginUriProvider = loginUriProvider;
        //this.templateRenderer = templateRenderer;
        this.sheetService = sheetService;
        this.permissionService = permissionService;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    /*TODO:
    try {
      UserProfile userProfile = permissionService.checkIfUserExists(request);
      String userKey = userProfile.getUserKey().getStringValue();
      Timesheet sheet = sheetService.getTimesheetByUser(userKey);

      if (sheet == null) {
        sheet = sheetService.add(userKey, 150, 0, "Confluence Timesheet");
      }

      Map<String, Object> paramMap = Maps.newHashMap();
      paramMap.put("timesheetid", sheet.getID());
      response.setContentType("text/html;charset=utf-8");
      //TODO: fix it: argumente sind falsch!
      //templateRenderer.render("timesheet.vm", paramMap, response.getWriter());

    } catch (NotAuthorizedException e) {
      redirectToLogin(request, response);
    }
    */
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
