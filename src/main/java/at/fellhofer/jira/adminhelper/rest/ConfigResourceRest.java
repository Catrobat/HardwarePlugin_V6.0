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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@Path("/config")
public class ConfigResourceRest {
    public static final String KEY_BASE = Config.class.getName() + ".";
    public static final String KEY_ID = KEY_BASE + "githubId";
    public static final String KEY_SECRET = KEY_BASE + "githubSecret";
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

    public Config getConfigFromSettings() {
        return (Config) transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

                Config config = new Config();
                config.setGithubId((String) settings.get(KEY_ID));
                config.setGithubSecret((String) settings.get(KEY_SECRET));

                if((List<String>)settings.get(KEY_TEAMS) == null)
                    settings.put(KEY_TEAMS, new ArrayList<String>());

                List<Config.Team> teamList = new ArrayList<Config.Team>();
                for (String teamName : (List<String>)settings.get(KEY_TEAMS) ) {
                    Config.Team tempTeam = new Config.Team(teamName);
                    tempTeam.setGithubTeams((List<String>) settings.get(KEY_BASE + teamName + SUBKEY_GITHUB));
                    tempTeam.setDeveloperGroups((List<String>) settings.get(KEY_BASE + teamName + SUBKEY_DEVELOPERS));
                    tempTeam.setSeniorGroups((List<String>) settings.get(KEY_BASE + teamName + SUBKEY_SENIORS));
                    tempTeam.setCoordinatorGroups((List<String>) settings.get(KEY_BASE + teamName + SUBKEY_COORDINATORS));
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
                List<String> teamNames = (ArrayList<String>) settings.get(KEY_TEAMS);

                return teamNames;
            }
        })).build();
    }

    @PUT
    @Path("/saveConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setConfig(final Config config, @Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
                pluginSettings.put(KEY_ID, config.getGithubId());
                pluginSettings.put(KEY_SECRET, config.getGithubSecret());

                if (config.getTeams() == null)
                    config.setTeams(new ArrayList<Config.Team>());

                List<String> teamNames = new ArrayList<String>();
                for (Config.Team team : config.getTeams()) {
                    if(teamNames.contains(team.getName())) {
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


    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Config {
        @XmlElement
        private String githubId;
        @XmlElement
        private String githubSecret;
        @XmlElement
        private List<Team> teams;

        public String getGithubId() {
            return githubId;
        }

        public void setGithubId(String githubId) {
            this.githubId = githubId;
        }

        public String getGithubSecret() {
            return githubSecret;
        }

        public void setGithubSecret(String githubSecret) {
            this.githubSecret = githubSecret;
        }

        public List<Team> getTeams() {
            return teams;
        }

        public void setTeams(List<Team> teams) {
            this.teams = teams;
        }

        @XmlRootElement
        @XmlAccessorType(XmlAccessType.FIELD)
        public static final class Team {
            @XmlElement
            private String name;
            @XmlElement
            private List<String> githubTeams;
            @XmlElement
            private List<String> coordinatorGroups;
            @XmlElement
            private List<String> seniorGroups;
            @XmlElement
            private List<String> developerGroups;

            public Team() {

            }

            public Team(String name) {
                this.name = name;
                githubTeams = new ArrayList<String>();
                coordinatorGroups = new ArrayList<String>();
                seniorGroups = new ArrayList<String>();
                developerGroups = new ArrayList<String>();
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public List<String> getGithubTeams() {
                return githubTeams;
            }

            public void setGithubTeams(List<String> githubTeams) {
                this.githubTeams = githubTeams;
            }

            public List<String> getCoordinatorGroups() {
                return coordinatorGroups;
            }

            public void setCoordinatorGroups(List<String> coordinatorGroups) {
                this.coordinatorGroups = coordinatorGroups;
            }

            public List<String> getSeniorGroups() {
                return seniorGroups;
            }

            public void setSeniorGroups(List<String> seniorGroups) {
                this.seniorGroups = seniorGroups;
            }

            public List<String> getDeveloperGroups() {
                return developerGroups;
            }

            public void setDeveloperGroups(List<String> developerGroups) {
                this.developerGroups = developerGroups;
            }
        }
    }
}