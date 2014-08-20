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
import at.fellhofer.jira.adminhelper.rest.json.JsonUser;
import com.atlassian.core.AtlassianCoreException;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import org.apache.commons.validator.routines.EmailValidator;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/user")
public class UserRest {
    public static final String GITHUB_PROPERTY = "github";
    public static final String DISABLED_GROUP = "Disabled";
    public static final String DEFAULT_PASSWORD = "catrobat";

    private final UserManager userManager;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;
    private final UserPreferencesManager userPreferencesManager;

    public UserRest(final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory,
                    final TransactionTemplate transactionTemplate, final UserPreferencesManager userPreferencesManager) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
        this.userPreferencesManager = userPreferencesManager;
    }

    @PUT
    @Path("/createUser")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(final JsonUser jsonUser, @Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (jsonUser.getFirstName() == null || jsonUser.getLastName() == null ||
                jsonUser.getUserName() == null || !EmailValidator.getInstance().isValid(jsonUser.getEmail())) {
            return Response.serverError().entity("Please check all input fields.").build();
        }

        ConfigResourceRest configResourceRest = new ConfigResourceRest(userManager, pluginSettingsFactory, transactionTemplate);
        JsonConfig config = configResourceRest.getConfigFromSettings();

        User jiraUser = null;
        UserUtil userUtil = ComponentAccessor.getUserUtil();
        try {
            jiraUser = userUtil.createUserNoNotification(jsonUser.getUserName(), DEFAULT_PASSWORD, jsonUser.getEmail(),
                    jsonUser.getFirstName() + " " + jsonUser.getLastName());
        } catch (PermissionException e) {
            e.printStackTrace();
        } catch (CreateException e) {
            e.printStackTrace();
        }

        if (jiraUser == null) {
            return Response.serverError().build();
        }

        ExtendedPreferences extendedPreferences = userPreferencesManager.getExtendedPreferences(ApplicationUsers.from(jiraUser));
        try {
            extendedPreferences.setText(GITHUB_PROPERTY, jsonUser.getGithubName());
        } catch (AtlassianCoreException e) {
            e.printStackTrace();
        }

        Response errorResponse = addUserToGithubAndJiraGroups(jsonUser, jiraUser, config);
        if (errorResponse != null) {
            return errorResponse;
        }

        return Response.ok().build();
    }

    @GET
    @Path("/getUsers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        UserUtil userUtil = ComponentAccessor.getUserUtil();
        List<JsonUser> jsonUserList = new ArrayList<JsonUser>();
        Collection<User> allUsers = ComponentAccessor.getUserManager().getAllUsers();
        Collection<User> systemAdmins = userUtil.getJiraSystemAdministrators();
        for (User user : allUsers) {
            if (systemAdmins.contains(user)) {
                continue;
            }

            JsonUser jsonUser = new JsonUser();
            jsonUser.setEmail(user.getEmailAddress());
            jsonUser.setUserName(user.getName());

            String displayName = user.getDisplayName();
            int lastSpaceIndex = displayName.lastIndexOf(' ');
            if (lastSpaceIndex >= 0) {
                jsonUser.setFirstName(displayName.substring(0, lastSpaceIndex));
                jsonUser.setLastName(displayName.substring(lastSpaceIndex + 1));
            } else {
                jsonUser.setFirstName(displayName);
            }

            boolean isActive = true;
            for (Group group : userUtil.getGroupsForUser(user.getName())) {
                if (group.getName().toLowerCase().equals(DISABLED_GROUP.toLowerCase())) {
                    isActive = false;
                    break;
                }
            }
            jsonUser.setActive(isActive);

            ExtendedPreferences extendedPreferences = userPreferencesManager.getExtendedPreferences(ApplicationUsers.from(user));
            jsonUser.setGithubName(extendedPreferences.getText(GITHUB_PROPERTY));

            jsonUserList.add(jsonUser);
        }

        return Response.ok(jsonUserList).build();
    }

    @PUT
    @Path("/activateUser")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response activateUser(final JsonUser jsonUser, @Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (jsonUser == null) {
            return Response.serverError().entity("User not given").build();
        }

        ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByName(jsonUser.getUserName());
        if (applicationUser == null) {
            return Response.serverError().entity("User not found").build();
        }

        if (jsonUser.getCoordinatorList().size() == 0 && jsonUser.getSeniorList().size() == 0 && jsonUser.getDeveloperList().size() == 0) {
            return Response.serverError().entity("No Team selected").build();
        }

        ConfigResourceRest configResourceRest = new ConfigResourceRest(userManager, pluginSettingsFactory, transactionTemplate);
        JsonConfig config = configResourceRest.getConfigFromSettings();

        // remove user from all groups (especially from DISABLED_GROUP) since he will be added to chosen groups afterwards
        try {
            removeFromAllGroups(ApplicationUsers.toDirectoryUser(applicationUser));
        } catch (RemoveException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        } catch (PermissionException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }

        // add user to all desired GitHub teams and groups
        ExtendedPreferences extendedPreferences = userPreferencesManager.getExtendedPreferences(ApplicationUsers.from(applicationUser.getDirectoryUser()));
        jsonUser.setGithubName(extendedPreferences.getText(GITHUB_PROPERTY));
        addUserToGithubAndJiraGroups(jsonUser, applicationUser.getDirectoryUser(), config);


        return Response.ok().build();
    }

    @PUT
    @Path("/inactivateUser")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response inactivateUser(final String inactivateUser, @Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (inactivateUser == null) {
            return Response.serverError().entity("User not given").build();
        }

        ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByName(inactivateUser);
        if (applicationUser == null) {
            return Response.serverError().entity("User not found").build();
        }

        // remove user from all GitHub teams
        ExtendedPreferences extendedPreferences = userPreferencesManager.getExtendedPreferences(applicationUser);
        String githubName = extendedPreferences.getText(GITHUB_PROPERTY);
        if (githubName != null) {
            GithubHelperRest githubHelper = new GithubHelperRest(userManager, pluginSettingsFactory, transactionTemplate);
            String error = githubHelper.removeUserFromAllTeams(githubName);
            if (error != null) {
                return Response.serverError().entity(error).build();
            }
        }

        // remove user from all groups and add user to DISABLED_GROUP
        try {
            removeFromAllGroups(ApplicationUsers.toDirectoryUser(applicationUser));
            addToGroups(applicationUser.getDirectoryUser(), Arrays.asList(DISABLED_GROUP));
        } catch (RemoveException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        } catch (PermissionException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        } catch (UserNotFoundException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        } catch (InvalidGroupException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        } catch (OperationNotPermittedException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        } catch (OperationFailedException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }

        return Response.ok().build();
    }

    private void addToGroups(User user, List<String> groupList) throws UserNotFoundException, OperationFailedException,
            GroupNotFoundException, OperationNotPermittedException, InvalidGroupException {
        if (user == null || groupList == null)
            return;

        GroupManager groupManager = ComponentAccessor.getGroupManager();
        Collection<Group> groupCollection;

        for (String groupString : groupList) {
            boolean groupExists = false;
            groupCollection = groupManager.getAllGroups();
            for (Group group : groupCollection) {
                if (groupString.toLowerCase().equals(group.getName().toLowerCase())) {
                    groupManager.addUserToGroup(user, group);
                    groupExists = true;
                    break;
                }
            }
            if (!groupExists) {
                Group newGroup = groupManager.createGroup(groupString);
                groupManager.addUserToGroup(user, newGroup);
            }
        }
    }

    public Response addUserToGithubAndJiraGroups(JsonUser jsonUser, User jiraUser, JsonConfig config) {
        Set<String> githubTeamSet = new HashSet<String>();
        GithubHelperRest githubHelper = new GithubHelperRest(userManager, pluginSettingsFactory, transactionTemplate);
        try {
            if (jsonUser.getCoordinatorList() != null) {
                for (String coordinatorOf : jsonUser.getCoordinatorList()) {
                    for (JsonTeam team : config.getTeams()) {
                        if (team.getName().equals(coordinatorOf)) {
                            addToGroups(jiraUser, team.getCoordinatorGroups());
                            githubTeamSet.addAll(team.getGithubTeams());
                        }
                    }
                }
            }
            if (jsonUser.getSeniorList() != null) {
                for (String seniorOf : jsonUser.getSeniorList()) {
                    for (JsonTeam team : config.getTeams()) {
                        if (team.getName().equals(seniorOf)) {
                            addToGroups(jiraUser, team.getSeniorGroups());
                            githubTeamSet.addAll(team.getGithubTeams());
                        }
                    }
                }
            }
            if (jsonUser.getDeveloperList() != null) {
                for (String developerOf : jsonUser.getDeveloperList()) {
                    for (JsonTeam team : config.getTeams()) {
                        if (team.getName().equals(developerOf)) {
                            addToGroups(jiraUser, team.getDeveloperGroups());
                            githubTeamSet.addAll(team.getGithubTeams());
                        }
                    }
                }
            }
        } catch (UserNotFoundException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        } catch (OperationFailedException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        } catch (OperationNotPermittedException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        } catch (InvalidGroupException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }

        if (jsonUser.getGithubName() != null && !jsonUser.getGithubName().equals("")) {
            StringBuilder errors = new StringBuilder();
            for (String team : githubTeamSet) {
                String returnValue = githubHelper.addUserToTeam(jsonUser.getGithubName(), team);
                if (returnValue != null) {
                    errors.append(returnValue);
                }
            }

            if (errors.length() != 0) {
                return Response.serverError().entity(errors.toString()).build();
            }
        }

        return Response.ok().build();
    }

    private void removeFromAllGroups(User user) throws RemoveException, PermissionException {
        if (user == null)
            return;

        GroupManager groupManager = ComponentAccessor.getGroupManager();
        Collection<Group> groupCollection = groupManager.getGroupsForUser(user);

        UserUtil userUtil = ComponentAccessor.getUserUtil();
        userUtil.removeUserFromGroups(groupCollection, user);
    }
}
