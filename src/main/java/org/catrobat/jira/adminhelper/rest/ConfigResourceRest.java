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

package org.catrobat.jira.adminhelper.rest;


import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.user.UserManager;
import org.catrobat.jira.adminhelper.activeobject.AdminHelperConfigService;
import org.catrobat.jira.adminhelper.activeobject.Team;
import org.catrobat.jira.adminhelper.rest.json.JsonConfig;
import org.catrobat.jira.adminhelper.rest.json.JsonTeam;
import org.eclipse.egit.github.core.service.TeamService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Path("/config")
public class ConfigResourceRest extends RestHelper {
    private final AdminHelperConfigService configService;
    private final DirectoryManager directoryManager;

    public ConfigResourceRest(final UserManager userManager, final AdminHelperConfigService configService,
                              final PermissionManager permissionManager, final GroupManager groupManager, final DirectoryManager directoryManager) {
        super(permissionManager, configService, userManager, groupManager);
        this.configService = configService;
        this.directoryManager = directoryManager;
    }

    @GET
    @Path("/getConfig")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfig(@Context HttpServletRequest request) {
        Response unauthorized = checkPermission(request);
        if (unauthorized != null) {
            return unauthorized;
        }

        return Response.ok(new JsonConfig(configService.getConfiguration(), configService)).build();
    }

    @GET
    @Path("/getDirectories")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDirectories(@Context HttpServletRequest request) {
        Response unauthorized = checkPermission(request);
        if (unauthorized != null) {
            return unauthorized;
        }

        List<Directory> directoryList = directoryManager.findAllDirectories();
        List<JsonConfig> jsonDirectoryList = new ArrayList<JsonConfig>();
        for(Directory directory : directoryList) {
            JsonConfig config = new JsonConfig();
            config.setUserDirectoryId(directory.getId());
            config.setUserDirectoryName(directory.getName());
            jsonDirectoryList.add(config);
        }

        return Response.ok(jsonDirectoryList).build();
    }

    @GET
    @Path("/getTeamList")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamList(@Context HttpServletRequest request) {
        Response unauthorized = checkPermission(request);
        if (unauthorized != null) {
            return unauthorized;
        }

        List<String> teamList = new ArrayList<String>();
        for (Team team : configService.getConfiguration().getTeams()) {
            teamList.add(team.getTeamName());
        }

        return Response.ok(teamList).build();
    }

    @PUT
    @Path("/saveConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setConfig(final JsonConfig jsonConfig, @Context HttpServletRequest request) {
        Response unauthorized = checkPermission(request);
        if (unauthorized != null) {
            return unauthorized;
        }

        if (jsonConfig.getGithubToken() != null && jsonConfig.getGithubToken().length() != 0) {
            configService.setApiToken(jsonConfig.getGithubToken());
        }
        configService.setPublicApiToken(jsonConfig.getGithubTokenPublic());
        configService.setOrganisation(jsonConfig.getGithubOrganization());
        configService.setUserDirectoryId(jsonConfig.getUserDirectoryId());

        if (jsonConfig.getApprovedGroups() != null) {
            configService.clearApprovedGroups();
            for (String approvedGroupName : jsonConfig.getApprovedGroups()) {
                configService.addApprovedGroup(approvedGroupName);
            }
        }

        com.atlassian.jira.user.util.UserManager jiraUserManager = ComponentAccessor.getUserManager();
        if (jsonConfig.getApprovedUsers() != null) {
            configService.clearApprovedUsers();
            for (String approvedUserName : jsonConfig.getApprovedUsers()) {
                ApplicationUser user = jiraUserManager.getUserByName(approvedUserName);
                if (user != null) {
                    configService.addApprovedUser(user.getKey());
                }
            }
        }

        if (jsonConfig.getTeams() != null) {
            String token = configService.getConfiguration().getGithubApiToken();
            String organisation = configService.getConfiguration().getGithubOrganisation();
            TeamService teamService = new TeamService();
            teamService.getClient().setOAuth2Token(token);
            try {
                List<org.eclipse.egit.github.core.Team> githubTeamList = teamService.getTeams(organisation);

                for (JsonTeam jsonTeam : jsonConfig.getTeams()) {
                    configService.removeTeam(jsonTeam.getName());

                    List<Integer> githubIdList = new ArrayList<Integer>();
                    for (String teamName : jsonTeam.getGithubTeams()) {
                        for (org.eclipse.egit.github.core.Team githubTeam : githubTeamList) {
                            if (teamName.toLowerCase().equals(githubTeam.getName().toLowerCase())) {
                                githubIdList.add(githubTeam.getId());
                                break;
                            }
                        }
                    }

                    configService.addTeam(jsonTeam.getName(), githubIdList, jsonTeam.getCoordinatorGroups(),
                            jsonTeam.getSeniorGroups(), jsonTeam.getDeveloperGroups());
                }
            } catch (IOException e) {
                e.printStackTrace();
                return Response.serverError().entity("Some error with GitHub API (e.g. maybe wrong tokens, organisation, teams) occured").build();
            }
        }

        return Response.noContent().build();
    }

    @PUT
    @Path("/addTeam")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addTeam(final String modifyTeam, @Context HttpServletRequest request) {
        Response unauthorized = checkPermission(request);
        if (unauthorized != null) {
            return unauthorized;
        }

        boolean successful = configService.addTeam(modifyTeam, null, null, null, null) != null;

        if (successful)
            return Response.noContent().build();

        return Response.serverError().build();
    }

    @PUT
    @Path("/removeTeam")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeTeam(final String modifyTeam, @Context HttpServletRequest request) {
        Response unauthorized = checkPermission(request);
        if (unauthorized != null) {
            return unauthorized;
        }

        boolean successful = configService.removeTeam(modifyTeam) != null;

        if (successful)
            return Response.noContent().build();

        return Response.serverError().build();
    }
}