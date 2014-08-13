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


import at.fellhofer.jira.adminhelper.rest.json.JsonConfig;
import at.fellhofer.jira.adminhelper.rest.json.JsonTeam;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/config")
public class ConfigResourceRest {
    public static final String KEY_BASE = "at.fellhofer.jira.adminhelper.";
    public static final String KEY_TOKEN = KEY_BASE + "githubToken";
    public static final String KEY_ORGANIZATION = KEY_BASE + "githubOrganization";
    public static final String KEY_TEAMS = KEY_BASE + "teams";

    public static final String SUBKEY_COORDINATORS = ".coordinators";
    public static final String SUBKEY_GITHUB = ".github";
    public static final String SUBKEY_DEVELOPERS = ".developers";
    public static final String SUBKEY_SENIORS = ".seniors";

    private final UserManager userManager;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;

    public ConfigResourceRest(UserManager userManager, PluginSettingsFactory pluginSettingsFactory,
                              TransactionTemplate transactionTemplate) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @GET
    @Path("/getConfig")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfig(@Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(getConfigFromSettings()).build();
    }

    public JsonConfig getConfigFromSettings() {
        return (JsonConfig) transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

                JsonConfig config = new JsonConfig();
                config.setGithubToken((String) settings.get(KEY_TOKEN));
                config.setGithubOrganization((String) settings.get(KEY_ORGANIZATION));


                if (settings.get(KEY_TEAMS) == null)
                    settings.put(KEY_TEAMS, new ArrayList<String>());

                List<JsonTeam> teamList = new ArrayList<JsonTeam>();
                @SuppressWarnings("unchecked")
                List<String> settingsTeamList = (List<String>) settings.get(KEY_TEAMS);
                for (String teamName : settingsTeamList) {
                    JsonTeam tempTeam = new JsonTeam(teamName);

                    @SuppressWarnings("unchecked")
                    List<String> githubTeams = (List<String>) settings.get(KEY_BASE + teamName + SUBKEY_GITHUB);
                    tempTeam.setGithubTeams(githubTeams);
                    @SuppressWarnings("unchecked")
                    List<String> developerGroups = (List<String>) settings.get(KEY_BASE + teamName + SUBKEY_DEVELOPERS);
                    tempTeam.setDeveloperGroups(developerGroups);
                    @SuppressWarnings("unchecked")
                    List<String> seniorGroups = (List<String>) settings.get(KEY_BASE + teamName + SUBKEY_SENIORS);
                    tempTeam.setSeniorGroups(seniorGroups);
                    @SuppressWarnings("unchecked")
                    List<String> coordinatorGroups = (List<String>) settings.get(KEY_BASE + teamName + SUBKEY_COORDINATORS);
                    tempTeam.setCoordinatorGroups(coordinatorGroups);
                    teamList.add(tempTeam);
                }

                config.setTeams(teamList);

                return config;
            }
        });
    }

    @GET
    @Path("/getTeamList")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamList(@Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
                @SuppressWarnings("unchecked")
                List<String> teamList = (List<String>) settings.get(KEY_TEAMS);

                return teamList;
            }
        })).build();
    }

    @PUT
    @Path("/saveConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setConfig(final JsonConfig config, @Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
                pluginSettings.put(KEY_TOKEN, config.getGithubToken());
                pluginSettings.put(KEY_ORGANIZATION, config.getGithubOrganization());

                if (config.getTeams() == null)
                    config.setTeams(new ArrayList<JsonTeam>());

                List<String> teamNames = new ArrayList<String>();
                for (JsonTeam team : config.getTeams()) {
                    if (teamNames.contains(team.getName())) {
                        continue;
                    }

                    teamNames.add(team.getName());
                    pluginSettings.put(KEY_BASE + team.getName() + SUBKEY_GITHUB, team.getGithubTeams());
                    pluginSettings.put(KEY_BASE + team.getName() + SUBKEY_DEVELOPERS, team.getDeveloperGroups());
                    pluginSettings.put(KEY_BASE + team.getName() + SUBKEY_SENIORS, team.getSeniorGroups());
                    pluginSettings.put(KEY_BASE + team.getName() + SUBKEY_COORDINATORS, team.getCoordinatorGroups());
                }
                pluginSettings.put(KEY_TEAMS, teamNames);

                return null;
            }
        });
        return Response.noContent().build();
    }

    @PUT
    @Path("/addTeam")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addTeam(final String modifyTeam, @Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }


        boolean successful = (Boolean) transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
                @SuppressWarnings("unchecked")
                List<String> teamList = (List<String>) pluginSettings.get(ConfigResourceRest.KEY_TEAMS);

                for (String teamName : teamList) {
                    if (teamName.equals(modifyTeam)) {
                        return Boolean.FALSE;
                    }
                }

                teamList.add(modifyTeam);
                pluginSettings.put(ConfigResourceRest.KEY_TEAMS, teamList);

                return Boolean.TRUE;
            }
        });

        if (successful)
            return Response.noContent().build();

        return Response.serverError().build();
    }

    @PUT
    @Path("/removeTeam")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeTeam(final String modifyTeam, @Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }


        boolean successful = (Boolean) transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
                @SuppressWarnings("unchecked")
                List<String> teamList = (List<String>) pluginSettings.get(ConfigResourceRest.KEY_TEAMS);

                for (String teamName : teamList) {
                    if (teamName.equals(modifyTeam)) {
                        teamList.remove(teamName);
                        pluginSettings.put(ConfigResourceRest.KEY_TEAMS, teamList);
                        pluginSettings.remove(KEY_BASE + teamName + SUBKEY_GITHUB);
                        pluginSettings.remove(KEY_BASE + teamName + SUBKEY_DEVELOPERS);
                        pluginSettings.remove(KEY_BASE + teamName + SUBKEY_SENIORS);
                        pluginSettings.remove(KEY_BASE + teamName + SUBKEY_COORDINATORS);

                        return Boolean.TRUE;
                    }
                }

                return Boolean.FALSE;
            }
        });

        if (successful)
            return Response.noContent().build();

        return Response.serverError().build();
    }
}