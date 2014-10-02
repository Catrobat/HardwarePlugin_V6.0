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

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.manager.directory.DirectoryPermissionException;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.sal.api.user.UserManager;
import org.apache.commons.validator.routines.EmailValidator;
import org.catrobat.jira.adminhelper.activeobject.AdminHelperConfigService;
import org.catrobat.jira.adminhelper.helper.GithubHelper;
import org.catrobat.jira.adminhelper.rest.json.JsonConfig;
import org.catrobat.jira.adminhelper.rest.json.JsonTeam;
import org.catrobat.jira.adminhelper.rest.json.JsonUser;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/user")
public class UserRest extends RestHelper {
    public static final String GITHUB_PROPERTY = "github";
    public static final String DISABLED_GROUP = "Disabled";
    public static final String DEFAULT_PASSWORD = "catrobat";

    private final UserPreferencesManager userPreferencesManager;
    private final AdminHelperConfigService configService;
    private final DirectoryManager directoryManager;

    public UserRest(final UserManager userManager, final UserPreferencesManager userPreferencesManager,
                    final AdminHelperConfigService configService, final PermissionManager permissionManager,
                    final GroupManager groupManager, final DirectoryManager directoryManager) {
        super(permissionManager, configService, userManager, groupManager);
        this.userPreferencesManager = userPreferencesManager;
        this.configService = configService;
        this.directoryManager = directoryManager;
    }

    @PUT
    @Path("/createUser")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(final JsonUser jsonUser, @Context HttpServletRequest request) {
        Response unauthorized = checkPermission(request);
        if (unauthorized != null) {
            return unauthorized;
        }

        if (jsonUser.getFirstName() == null || jsonUser.getLastName() == null ||
                jsonUser.getUserName() == null || !EmailValidator.getInstance().isValid(jsonUser.getEmail())) {
            return Response.serverError().entity("Please check all input fields.").build();
        }

        UserUtil userUtil = ComponentAccessor.getUserUtil();
        if (userUtil.getUserByName(jsonUser.getUserName()) != null) {
            return Response.serverError().entity("User already exists!").build();
        }

        JsonConfig config = new JsonConfig(configService.getConfiguration(), configService);

        try {
            UserTemplate user = new UserTemplate(jsonUser.getUserName(), config.getUserDirectoryId());
            user.setFirstName(jsonUser.getFirstName());
            user.setLastName(jsonUser.getLastName());
            user.setEmailAddress(jsonUser.getEmail());
            user.setDisplayName(jsonUser.getFirstName() + " " + jsonUser.getLastName());
            user.setActive(true);
            PasswordCredential credential = new PasswordCredential(DEFAULT_PASSWORD);
            directoryManager.addUser(config.getUserDirectoryId(), user, credential);
        } catch (DirectoryNotFoundException e) {
            e.printStackTrace();
            return Response.serverError().entity("User-Directory was not found. Please check the settings!").build();
        } catch (InvalidCredentialException e) {
            e.printStackTrace();
            return Response.serverError().entity("User's credential do not meet requirements of directory.").build();
        } catch (InvalidUserException e) {
            e.printStackTrace();
            return Response.serverError().entity("Required user properties are not given.").build();
        } catch (UserAlreadyExistsException e) {
            e.printStackTrace();
            return Response.serverError().entity("User already exists in this directory!").build();
        } catch (OperationFailedException e) {
            e.printStackTrace();
            return Response.serverError().entity("Operation failed on directory implementation.").build();
        } catch (DirectoryPermissionException e) {
            e.printStackTrace();
            return Response.serverError().entity("Not enough permissions to create user in directory").build();
        }

        ApplicationUser jiraUser = userUtil.getUserByName(jsonUser.getUserName());
        if (jiraUser == null) {
            return Response.serverError().entity("User creation failed.").build();
        }

        ExtendedPreferences extendedPreferences = userPreferencesManager.getExtendedPreferences(jiraUser);
        try {
            extendedPreferences.setText(GITHUB_PROPERTY, jsonUser.getGithubName());
        } catch (AtlassianCoreException e) {
            e.printStackTrace();
        }

        Response errorResponse = addUserToGithubAndJiraGroups(jsonUser, ApplicationUsers.toDirectoryUser(jiraUser), config);
        if (errorResponse != null) {
            return errorResponse;
        }

        return Response.ok().build();
    }

    @GET
    @Path("/getUsers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@Context HttpServletRequest request) {
        Response unauthorized = checkPermission(request);
        if (unauthorized != null) {
            return unauthorized;
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
        Response unauthorized = checkPermission(request);
        if (unauthorized != null) {
            return unauthorized;
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

        JsonConfig config = new JsonConfig(configService.getConfiguration(), configService);

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
        ExtendedPreferences extendedPreferences = userPreferencesManager
                .getExtendedPreferences(ApplicationUsers.from(applicationUser.getDirectoryUser()));
        jsonUser.setGithubName(extendedPreferences.getText(GITHUB_PROPERTY));
        addUserToGithubAndJiraGroups(jsonUser, applicationUser.getDirectoryUser(), config);


        return Response.ok().build();
    }

    @PUT
    @Path("/inactivateUser")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response inactivateUser(final String inactivateUser, @Context HttpServletRequest request) {
        Response unauthorized = checkPermission(request);
        if (unauthorized != null) {
            return unauthorized;
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
            GithubHelper githubHelper = new GithubHelper(configService);
            String error = githubHelper.removeUserFromAllTeams(githubName);
            if (error != null) {
                return Response.serverError().entity(error).build();
            }
        }

        // remove user from all groups and add user to DISABLED_GROUP
        try {
            removeFromAllGroups(ApplicationUsers.toDirectoryUser(applicationUser));
            Response error = addToGroups(applicationUser.getDirectoryUser(), Arrays.asList(DISABLED_GROUP));
            if (error != null) {
                return error;
            }
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

    private Response addToGroups(User user, List<String> groupList) throws UserNotFoundException, OperationFailedException,
            GroupNotFoundException, OperationNotPermittedException, InvalidGroupException {
        if (user == null || groupList == null)
            return Response.serverError().entity("user/group may not be null").build();

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
                return Response.serverError().entity("Group \"" + groupString + "\" does not exist").build();
            }
        }

        return null; // everything ok
    }

    public Response addUserToGithubAndJiraGroups(JsonUser jsonUser, User jiraUser, JsonConfig config) {
        Set<String> githubTeamSet = new HashSet<String>();
        try {
            if (jsonUser.getCoordinatorList() != null) {
                for (String coordinatorOf : jsonUser.getCoordinatorList()) {
                    for (JsonTeam team : config.getTeams()) {
                        if (team.getName().equals(coordinatorOf)) {
                            Response error = addToGroups(jiraUser, team.getCoordinatorGroups());
                            if (error != null) {
                                return error;
                            }
                            githubTeamSet.addAll(team.getGithubTeams());
                        }
                    }
                }
            }
            if (jsonUser.getSeniorList() != null) {
                for (String seniorOf : jsonUser.getSeniorList()) {
                    for (JsonTeam team : config.getTeams()) {
                        if (team.getName().equals(seniorOf)) {
                            Response error = addToGroups(jiraUser, team.getSeniorGroups());
                            if (error != null) {
                                return error;
                            }
                            githubTeamSet.addAll(team.getGithubTeams());
                        }
                    }
                }
            }
            if (jsonUser.getDeveloperList() != null) {
                for (String developerOf : jsonUser.getDeveloperList()) {
                    for (JsonTeam team : config.getTeams()) {
                        if (team.getName().equals(developerOf)) {
                            Response error = addToGroups(jiraUser, team.getDeveloperGroups());
                            if (error != null) {
                                return error;
                            }
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

        GithubHelper githubHelper = new GithubHelper(configService);
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
