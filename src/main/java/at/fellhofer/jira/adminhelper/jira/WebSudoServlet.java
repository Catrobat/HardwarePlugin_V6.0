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

package at.fellhofer.jira.adminhelper.jira;

import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by felly on 05/06/14.
 */
public class WebSudoServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(WebSudoServlet.class);
    private final LoginUriProvider loginUriProvider;
    private final UserManager userManager;
    private final TemplateRenderer templateRenderer;
    private final WebSudoManager webSudoManager;
    private final GlobalPermissionManager globalPermissionManager;
    private final PluginSettingsFactory pluginSettingsFactory;


    public WebSudoServlet(final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory, final LoginUriProvider loginUriProvider, final TemplateRenderer templateRenderer, final WebSudoManager webSudoManager, final GlobalPermissionManager globalPermissionManager) {
        //this.userManager = userManager;
        this.templateRenderer = checkNotNull(templateRenderer, "templateRenderer");
        this.userManager = checkNotNull(userManager, "userManager");
        this.loginUriProvider = loginUriProvider;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.globalPermissionManager = checkNotNull(globalPermissionManager, "globalPermissionManager");
        this.webSudoManager = checkNotNull(webSudoManager, "webSudoManager");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = userManager.getRemoteUsername(request);
        if (username == null) {
            redirectToLogin(request, response);
            return;
        } else if (username != null && !userManager.isSystemAdmin(username)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!webSudoManager.canExecuteRequest(request)) {
            webSudoManager.enforceWebSudoProtection(request, response);
            return;
        }

        templateRenderer.render("create_user.vm", response.getWriter());

        /*
        try {
            webSudoManager.willExecuteWebSudoRequest(request);


            // This request will be WebSudo protected
            Map<String, Object> renderContainer = new HashMap<String, Object>();

            List<Team> teams = new ArrayList<Team>();
            Team testTeam = new Team();
            testTeam.setTeamName("Catroid");
            teams.add(testTeam);
            testTeam = new Team();
            testTeam.setTeamName("Paintroid");
            teams.add(testTeam);

            List<Team> settingsTeams = settingsHelper.getTeams();

            testTeam = new Team();
            log.error("Blub: " + settingsTeams.get(0).getTeamName());
            testTeam.setTeamName(settingsTeams.get(0).getTeamName());
            settingsTeams.add(testTeam);
            testTeam = new Team();
            testTeam.setTeamName("Paintroid");
            settingsTeams.add(testTeam);

            renderContainer.put("teams", settingsTeams);
            //renderContainer.put("teams", teams);
            //renderContainer.put("number", settingsTeams.size());


            templateRenderer.render("create_user.vm", renderContainer, response.getWriter());
            //templateRenderer.render("create_user.vm", response.getWriter());

            templateRenderer.render("create_user.vm", response.getWriter());
        } catch (WebSudoSessionException wes) {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
        */
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            redirectToLogin(request, response);
            return;
        }

        try {
            webSudoManager.willExecuteWebSudoRequest(request);

            String requestedAction = request.getParameter("action");

//            if (requestedAction != null && requestedAction.equals("github_search")) {
//                String userToSearch = request.getParameter("user_search");
//                if (userToSearch == null)
//                    return;
//                GithubHelperRest helper = new GithubHelperRest();
//                response.getWriter().write(helper.searchUsers(userToSearch));
//                return;
//            }

            StringBuffer sb = new StringBuffer();
            sb.append("<html><head></head><body>Yeah, I did it! post<br />");
            sb.append(request.getParameterMap() + "<br />");
            Enumeration<String> parameterNames = request.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                // for(Map.Entry<String, Object> entry : map.entrySet()) {
                String parameter = parameterNames.nextElement();
                sb.append(parameter + ": " + request.getParameter(parameter) + "<br />");

            }

            sb.append("</body></html>");
            response.getWriter().write(sb.toString());
        } catch (WebSudoSessionException wes) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
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
