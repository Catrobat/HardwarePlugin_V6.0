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

package at.fellhofer.jira.adminhelper.rest;

import at.fellhofer.jira.adminhelper.rest.json.JsonUser;
import com.atlassian.core.AtlassianCoreException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import org.eclipse.egit.github.core.Team;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.TeamService;
import org.eclipse.egit.github.core.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

@Path("/github")
public class GithubHelperRest {
    private final UserManager userManager;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;

    public GithubHelperRest(UserManager userManager, PluginSettingsFactory pluginSettingsFactory,
                            TransactionTemplate transactionTemplate) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @PUT
    @Path("/searchUser")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final String searchString, @Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String returnValue = doesUserExist(searchString) ? "success" : "failure";

        return Response.ok(returnValue).build();
    }

    @PUT
    @Path("/changeGithubname")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeGithubnameRest(final JsonUser jsonUser, @Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (jsonUser.getUserName() == null || jsonUser.getGithubName() == null || jsonUser.getDeveloperList() == null) {
            return Response.serverError().entity("JSON User Object not complete").build();
        }

        if (jsonUser.getDeveloperList().size() == 0) {
            return Response.serverError().entity("No team selected").build();
        }

        if (!doesUserExist(jsonUser.getGithubName())) {
            return Response.serverError().entity("Github User does not exist").build();
        }

        UserUtil userUtil = ComponentAccessor.getUserUtil();
        ApplicationUser applicationUser = userUtil.getUserByName(jsonUser.getUserName());

        if (applicationUser == null) {
            return Response.serverError().entity("Jira User does not exist").build();
        }

        UserPreferencesManager userPreferencesManager = ComponentAccessor.getUserPreferencesManager();
        ExtendedPreferences extendedPreferences = userPreferencesManager.getExtendedPreferences(applicationUser);
        String oldGithubName = extendedPreferences.getText(UserRest.GITHUB_PROPERTY);

        if (oldGithubName != null) {
            if (oldGithubName.toLowerCase().equals(jsonUser.getGithubName().toLowerCase())) {
                return Response.serverError().entity("Github Username not changed").build();
            }

            boolean stillExists = false;
            for (ApplicationUser tempApplicationUser : userUtil.getAllApplicationUsers()) {
                if (tempApplicationUser.getUsername().toLowerCase().equals(applicationUser.getUsername().toLowerCase())) {
                    continue;
                }

                extendedPreferences = userPreferencesManager.getExtendedPreferences(tempApplicationUser);
                String tempUserGithubname = extendedPreferences.getText(UserRest.GITHUB_PROPERTY);
                if (tempUserGithubname != null && oldGithubName.toLowerCase().equals(tempUserGithubname.toLowerCase())) {
                    stillExists = true;
                    break;
                }
            }

            if (!stillExists) {
                removeUserFromAllTeams(oldGithubName);
            }
        }

        extendedPreferences = userPreferencesManager.getExtendedPreferences(applicationUser);
        try {
            extendedPreferences.setText(UserRest.GITHUB_PROPERTY, jsonUser.getGithubName());
        } catch (AtlassianCoreException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }

        ConfigResourceRest configResourceRest = new ConfigResourceRest(userManager, pluginSettingsFactory, transactionTemplate);
        UserRest userRest = new UserRest(userManager, pluginSettingsFactory, transactionTemplate, userPreferencesManager);
        userRest.addUserToGithubAndJiraGroups(jsonUser, applicationUser.getDirectoryUser(), configResourceRest.getConfigFromSettings());

        return Response.ok().build();
    }

    public boolean doesUserExist(final String userName) {
        if (userName == null || userName.equals("")) {
            return false;
        }

        return (Boolean) transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

                String token = (String) settings.get(ConfigResourceRest.KEY_TOKEN);

                try {
                    UserService userService = new UserService();
                    userService.getClient().setOAuth2Token(token);

                    User user = userService.getUser(userName);
                    if (user != null) {
                        return Boolean.TRUE;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return Boolean.FALSE;
            }
        });
    }

    public String addUserToTeam(final String user, final String team) {
        if (user == null || !doesUserExist(user)) {
            return "User does not exist on GitHub";
        }

        return (String) transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

                String token = (String) settings.get(ConfigResourceRest.KEY_TOKEN);
                String organization = (String) settings.get(ConfigResourceRest.KEY_ORGANIZATION);

                try {
                    TeamService teamService = new TeamService();
                    teamService.getClient().setOAuth2Token(token);

                    List<Team> teamList = teamService.getTeams(organization);
                    Integer id = null;
                    for(Team githubTeam : teamList) {
                        if(githubTeam.getName().toLowerCase().equals(team.toLowerCase())) {
                            id = githubTeam.getId();
                            break;
                        }
                    }
                    if(id == null)
                        return "Team not found";

                    teamService.addMember(id, user);
                } catch (IOException e) {
                    e.printStackTrace();
                    return e.getCause();
                }

                return null; // everything went fine
            }
        });
    }

    public String removeUserFromAllTeams(final String user) {
        if (!doesUserExist(user)) {
            return "User does not exist on GitHub";
        }

        return (String) transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

                String token = (String) settings.get(ConfigResourceRest.KEY_TOKEN);
                String organization = (String) settings.get(ConfigResourceRest.KEY_ORGANIZATION);

                try {
                    TeamService teamService = new TeamService();
                    teamService.getClient().setOAuth2Token(token);

                    List<Team> teamList = teamService.getTeams(organization);
                    for(Team githubTeam : teamList) {
                        // Owner group must not me empty
                        // getMembersCount() just filled when getting single team
                        if(githubTeam.getName().equals("Owners") && teamService.getTeam(githubTeam.getId()).getMembersCount() <= 1) {
                            continue;
                        }

                        if(teamService.isMember(githubTeam.getId(), user)) {
                            teamService.removeMember(githubTeam.getId(), user);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return e.getCause();
                }

                return null; // everything went fine
            }
        });
    }
}
