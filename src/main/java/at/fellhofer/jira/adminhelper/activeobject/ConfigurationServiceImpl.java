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

package at.fellhofer.jira.adminhelper.activeobject;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.Query;

import java.util.List;

public class ConfigurationServiceImpl implements ConfigurationService {

    private final ActiveObjects ao;

    public ConfigurationServiceImpl(ActiveObjects ao) {
        this.ao = ao;
    }

    @Override
    public Configuration getConfiguration() {
        Configuration[] configurationArray = ao.find(Configuration.class);
        Configuration configuration;
        if (configurationArray.length == 0) {
            configuration = ao.create(Configuration.class);
            configuration.save();
        } else {
            configuration = configurationArray[0];
        }

        return configuration;
    }

    @Override
    public Configuration setApiToken(String apiToken) {
        Configuration configuration = getConfiguration();
        configuration.setGithubApiToken(apiToken);
        configuration.save();
        return configuration;
    }

    @Override
    public Configuration setOrganisation(String organisation) {
        Configuration configuration = getConfiguration();
        configuration.setGithubOrganisation(organisation);
        configuration.save();
        return configuration;
    }

    @Override
    public Team addTeam(String teamName, List<Integer> githubTeamIdList, List<String> coordinatorGroups, List<String> seniorGroups, List<String> developerGroups) {
        Team[] teamArray = ao.find(Team.class, Query.select().where("upper(TEAM_NAME) = upper(?)", teamName));
        if (teamArray.length != 0) {
            return null;
        }

        Configuration configuration = getConfiguration();
        Team team = ao.create(Team.class);
        team.setConfiguration(configuration);
        team.setTeamName(teamName);
        for (int githubId : githubTeamIdList) {
            GithubTeam[] githubTeamArray = ao.find(GithubTeam.class, Query.select().where("GITHUB_ID = ?", githubId));
            GithubTeam githubTeam;
            if (githubTeamArray.length == 0) {
                githubTeam = ao.create(GithubTeam.class);
            } else {
                githubTeam = githubTeamArray[0];
            }

            githubTeam.setGithubId(githubId);
            githubTeam.save();

            TeamToGithubTeam mapper = ao.create(TeamToGithubTeam.class);
            mapper.setGithubTeam(githubTeam);
            mapper.setTeam(team);
            mapper.save();
        }
        fillTeam(team, TeamToGroup.Role.COORDINATOR, coordinatorGroups);
        fillTeam(team, TeamToGroup.Role.SENIOR, seniorGroups);
        fillTeam(team, TeamToGroup.Role.DEVELOPER, developerGroups);
        team.save();

        return team;
    }

    private void fillTeam(Team team, TeamToGroup.Role role, List<String> teamList) {
        for (String groupName : teamList) {
            Group[] groupArray = ao.find(Group.class, Query.select().where("upper(GROUP_NAME) = upper(?)", groupName));
            Group group;
            if (groupArray.length == 0) {
                group = ao.create(Group.class);
            } else {
                group = groupArray[0];
            }

            group.setGroupName(groupName);
            group.save();

            TeamToGroup mapper = ao.create(TeamToGroup.class);
            mapper.setGroup(group);
            mapper.setTeam(team);
            mapper.setRole(role);
            mapper.save();
        }
    }

    @Override
    public Configuration removeTeam(String teamName) {
        Team[] teamArray = ao.find(Team.class, Query.select().where("upper(TEAM_NAME) = upper(?)", teamName));
        if (teamArray.length == 0) {
            return null;
        }
        Team team = teamArray[0];
        Group[] groupArray = team.getGroups();
        TeamToGroup[] teamToGroupArray = ao.find(TeamToGroup.class, Query.select().where("TEAM_ID = ?", team.getID()));
        for (TeamToGroup teamToGroup : teamToGroupArray) {
            ao.delete(teamToGroup);
        }

        for (Group group : groupArray) {
            if (group.getTeams().length == 0) {
                ao.delete(group);
            }
        }

        return getConfiguration();
    }

    @Override
    public ApprovedGroup addApprovedGroup(String approvedGroupName) {
        ApprovedGroup[] approvedGroupArray = ao.find(ApprovedGroup.class, Query.select()
                .where("upper(GROUP_NAME) = upper(?)", approvedGroupName));
        ApprovedGroup approvedGroup;
        if (approvedGroupArray.length == 0) {
            approvedGroup = ao.create(ApprovedGroup.class);
        } else {
            approvedGroup = approvedGroupArray[0];
        }

        approvedGroup.setGroupName(approvedGroupName);
        approvedGroup.setConfiguration(getConfiguration());
        approvedGroup.save();

        return approvedGroup;
    }

    @Override
    public boolean isApproved(String groupName) {
        if (ao.find(ApprovedGroup.class).length == 0) {
            return true;
        }

        return ao.find(ApprovedGroup.class, Query.select()
                .where("upper(GROUP_NAME) = upper(?)", groupName)).length != 0;
    }

    @Override
    public Configuration removeApprovedGroup(String approvedGroupName) {
        ApprovedGroup[] approvedGroupArray = ao.find(ApprovedGroup.class, Query.select()
                .where("upper(GROUP_NAME) = upper(?)", approvedGroupName));
        if (approvedGroupArray.length == 0) {
            return null;
        }
        ao.delete(approvedGroupArray[0]);

        return getConfiguration();
    }
}
