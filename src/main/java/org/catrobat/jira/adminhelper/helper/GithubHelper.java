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
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
            GitHub gitHub = GitHub.connectUsingOAuth(token);
            GHUser user = gitHub.getUser(userName);

            if (user != null) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String removeUserFromAllTeams(final String userName) {
        if (!doesUserExist(userName)) {
            return "User does not exist on GitHub";
        }

        String token = configService.getConfiguration().getGithubApiToken();
        String organizationName = configService.getConfiguration().getGithubOrganisation();

        try {
            GitHub gitHub = GitHub.connectUsingOAuth(token);
            GHOrganization organization = gitHub.getOrganization(organizationName);
            GHUser user = gitHub.getUser(userName);
            organization.remove(user);
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return null; // everything went fine
    }

    public String addUserToTeam(final String userName, final String teamName) {
        if (userName == null || !doesUserExist(userName)) {
            return "User does not exist on GitHub";
        }

        String token = configService.getConfiguration().getGithubApiToken();
        String organizationName = configService.getConfiguration().getGithubOrganisation();

        try {
            GitHub gitHub = GitHub.connectUsingOAuth(token);
            GHOrganization organization = gitHub.getOrganization(organizationName);
            GHTeam team = organization.getTeamByName(teamName);
            GHUser user = gitHub.getUser(userName);
            team.add(user);
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return null; // everything went fine

    }

    public List<String> getAvailableTeams() {
        String token = configService.getConfiguration().getGithubApiToken();
        String organizationName = configService.getConfiguration().getGithubOrganisation();
        if (organizationName == null || organizationName.length() == 0) {
            return null;
        }

        Map<String, GHTeam> teams = new TreeMap<String, GHTeam>();
        try {
            GitHub gitHub = GitHub.connectUsingOAuth(token);
            GHOrganization organization = gitHub.getOrganization(organizationName);
            teams = organization.getTeams();
        } catch (IOException e) {
            // is ok - return the list anyway
        }

        return new ArrayList<String>(teams.keySet());
    }
}
