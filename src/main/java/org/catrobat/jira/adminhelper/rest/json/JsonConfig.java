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

package org.catrobat.jira.adminhelper.rest.json;

import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import org.catrobat.jira.adminhelper.activeobject.*;
import org.catrobat.jira.adminhelper.helper.GithubHelper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("unused")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class JsonConfig {

    @XmlElement
    private String githubToken;
    @XmlElement
    private String githubTokenPublic;
    @XmlElement
    private String githubOrganization;
    @XmlElement
    private List<JsonTeam> teams;
    @XmlElement
    private List<String> approvedGroups;
    @XmlElement
    private List<String> approvedUsers;
    @XmlElement
    private List<String> availableGithubTeams;
    @XmlElement
    private long userDirectoryId;
    @XmlElement
    private String userDirectoryName;
    @XmlElement
    private String roomCalendarGroup;
    @XmlElement
    private String meetingCalendarGroup;
    @XmlElement
    private String masterStudentGroup;
    @XmlElement
    private String phdStudentGroup;

    public JsonConfig() {

    }

    public JsonConfig(AdminHelperConfig toCopy, AdminHelperConfigService configService) {
        if (toCopy.getGithubApiToken() != null && toCopy.getGithubApiToken().length() != 0) {
            this.githubToken = "enter token if you want to change it";
        } else {
            this.githubToken = null;
        }
        this.githubTokenPublic = toCopy.getPublicGithubApiToken();
        this.githubOrganization = toCopy.getGithubOrganisation();

        this.roomCalendarGroup = toCopy.getRoomCalendarGroup();
        this.meetingCalendarGroup = toCopy.getMeetingCalendarGroup();
        this.masterStudentGroup = toCopy.getMasterStudentGroup();
        this.phdStudentGroup = toCopy.getPhdStudentGroup();

        Map<String, JsonTeam> teamMap = new TreeMap<String, JsonTeam>();
        for (Team team : toCopy.getTeams()) {
            teamMap.put(team.getTeamName(), new JsonTeam(team, configService));
        }

        this.teams = new ArrayList<JsonTeam>();
        this.teams.addAll(teamMap.values());

        this.approvedUsers = new ArrayList<String>();
        UserManager userManager = ComponentAccessor.getUserManager();
        for (ApprovedUser approvedUser : toCopy.getApprovedUsers()) {
            if (userManager.getUserByKey(approvedUser.getUserKey()) != null) {
                ApplicationUser user = userManager.getUserByKey(approvedUser.getUserKey());
                if (user != null) {
                    approvedUsers.add(user.getUsername());
                }
            }
        }

        this.approvedGroups = new ArrayList<String>();
        for (ApprovedGroup approvedGroup : toCopy.getApprovedGroups()) {
            approvedGroups.add(approvedGroup.getGroupName());
        }

        GithubHelper githubHelper = new GithubHelper(configService);
        this.availableGithubTeams = githubHelper.getAvailableTeams();

        this.userDirectoryId = toCopy.getUserDirectoryId();
        DirectoryManager directoryManager = ComponentAccessor.getComponent(DirectoryManager.class);
        try {
            this.userDirectoryName = directoryManager.findDirectoryById(userDirectoryId).getName();
        } catch (DirectoryNotFoundException e) {
            this.userDirectoryId = -1;
            this.userDirectoryName = null;
        }

    }

    public String getGithubToken() {
        return githubToken;
    }

    public void setGithubToken(String githubToken) {
        this.githubToken = githubToken;
    }

    public String getGithubOrganization() {
        return githubOrganization;
    }

    public void setGithubOrganization(String githubOrganization) {
        this.githubOrganization = githubOrganization;
    }

    public List<JsonTeam> getTeams() {
        return teams;
    }

    public void setTeams(List<JsonTeam> teams) {
        this.teams = teams;
    }

    public List<String> getApprovedGroups() {
        return approvedGroups;
    }

    public void setApprovedGroups(List<String> approvedGroups) {
        this.approvedGroups = approvedGroups;
    }

    public List<String> getAvailableGithubTeams() {
        return availableGithubTeams;
    }

    public void setAvailableGithubTeams(List<String> availableGithubTeams) {
        this.availableGithubTeams = availableGithubTeams;
    }

    public String getGithubTokenPublic() {
        return githubTokenPublic;
    }

    public void setGithubTokenPublic(String githubTokenPublic) {
        this.githubTokenPublic = githubTokenPublic;
    }

    public List<String> getApprovedUsers() {
        return approvedUsers;
    }

    public void setApprovedUsers(List<String> approvedUsers) {
        this.approvedUsers = approvedUsers;
    }

    public long getUserDirectoryId() {
        return userDirectoryId;
    }

    public void setUserDirectoryId(long userDirectoryId) {
        this.userDirectoryId = userDirectoryId;
    }

    public String getUserDirectoryName() {
        return userDirectoryName;
    }

    public void setUserDirectoryName(String userDirectoryName) {
        this.userDirectoryName = userDirectoryName;
    }

    public String getRoomCalendarGroup() {
        return roomCalendarGroup;
    }

    public void setRoomCalendarGroup(String roomCalendarGroup) {
        this.roomCalendarGroup = roomCalendarGroup;
    }

    public String getMeetingCalendarGroup() {
        return meetingCalendarGroup;
    }

    public void setMeetingCalendarGroup(String meetingCalendarGroup) {
        this.meetingCalendarGroup = meetingCalendarGroup;
    }

    public String getMasterStudentGroup() {
        return masterStudentGroup;
    }

    public void setMasterStudentGroup(String masterStudentGroup) {
        this.masterStudentGroup = masterStudentGroup;
    }

    public String getPhdStudentGroup() {
        return phdStudentGroup;
    }

    public void setPhdStudentGroup(String phdStudentGroup) {
        this.phdStudentGroup = phdStudentGroup;
    }
}
