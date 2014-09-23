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

package org.catrobat.jira.adminhelper.helper;

import org.catrobat.jira.adminhelper.activeobject.AdminHelperConfigService;
import org.eclipse.egit.github.core.Team;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.TeamService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GithubHelper {

    private final AdminHelperConfigService configService;

    public GithubHelper(AdminHelperConfigService configService) {
        this.configService = configService;
    }

    public boolean doesUserExist(final String userName) {
        if (userName == null || userName.equals("")) {
            return false;
        }

        String token = configService.getConfiguration().getGithubApiToken();

        try {
            UserService userService = new UserService();
            userService.getClient().setOAuth2Token(token);

            User user = userService.getUser(userName);
            if (user != null) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String removeUserFromAllTeams(final String user) {
        if (!doesUserExist(user)) {
            return "User does not exist on GitHub";
        }

        String token = configService.getConfiguration().getGithubApiToken();
        String organisation = configService.getConfiguration().getGithubOrganisation();

        try {
            TeamService teamService = new TeamService();
            teamService.getClient().setOAuth2Token(token);

            List<Team> teamList = teamService.getTeams(organisation);
            for (Team githubTeam : teamList) {
                // Owner group must not me empty
                // getMembersCount() just filled when getting single team
                if (githubTeam.getName().equals("Owners") && teamService.getTeam(githubTeam.getId()).getMembersCount() <= 1) {
                    continue;
                }

                if (teamService.isMember(githubTeam.getId(), user)) {
                    teamService.removeMember(githubTeam.getId(), user);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return null; // everything went fine
    }

    public String addUserToTeam(final String user, final String team) {
        if (user == null || !doesUserExist(user)) {
            return "User does not exist on GitHub";
        }

        String token = configService.getConfiguration().getGithubApiToken();
        String organisation = configService.getConfiguration().getGithubOrganisation();

        try {
            TeamService teamService = new TeamService();
            teamService.getClient().setOAuth2Token(token);

            List<Team> teamList = teamService.getTeams(organisation);
            Integer id = null;
            for (Team githubTeam : teamList) {
                if (githubTeam.getName().toLowerCase().equals(team.toLowerCase())) {
                    id = githubTeam.getId();
                    break;
                }
            }
            if (id == null)
                return "Team not found";

            teamService.addMember(id, user);
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return null; // everything went fine

    }

    public List<String> getAvailableTeams() {
        String token = configService.getConfiguration().getGithubApiToken();
        String organisation = configService.getConfiguration().getGithubOrganisation();
        if (organisation == null || organisation.length() == 0) {
            return null;
        }

        List<String> teamNameList = new ArrayList<String>();
        try {
            TeamService teamService = new TeamService();
            teamService.getClient().setOAuth2Token(token);

            List<Team> teamList = teamService.getTeams(organisation);
            for (Team githubTeam : teamList) {
                teamNameList.add(githubTeam.getName());
            }
        } catch (IOException e) {
            // is ok - return the list anyway
        }

        return teamNameList;
    }
}
