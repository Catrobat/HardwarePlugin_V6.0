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
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import org.apache.commons.validator.routines.EmailValidator;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.List;

@Path("/user")
public class UserRest {

    private final UserManager userManager;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;

    public UserRest(final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory,
                    final TransactionTemplate transactionTemplate) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
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

        try {
            UserUtil userUtil = ComponentAccessor.getUserUtil();

            User jiraUser = userUtil.createUserNoNotification(jsonUser.getUserName(), "catroid", jsonUser.getEmail(),
                    jsonUser.getFirstName() + " " + jsonUser.getLastName());

            ConfigResourceRest configResourceRest = new ConfigResourceRest(userManager, pluginSettingsFactory, transactionTemplate);
            ConfigResourceRest.Config config = configResourceRest.getConfigFromSettings();

            for (String coordinatorOf : jsonUser.getCoordinatorList()) {
                for (ConfigResourceRest.Config.Team team : config.getTeams()) {
                    if (team.getName().equals(coordinatorOf)) {
                        addToGroups(jiraUser, team.getCoordinatorGroups());
                    }
                }
            }
            for (String seniorOf : jsonUser.getSeniorList()) {
                for (ConfigResourceRest.Config.Team team : config.getTeams()) {
                    if (team.getName().equals(seniorOf)) {
                        addToGroups(jiraUser, team.getSeniorGroups());
                    }
                }
            }
            for (String developerOf : jsonUser.getDeveloperList()) {
                for (ConfigResourceRest.Config.Team team : config.getTeams()) {
                    if (team.getName().equals(developerOf)) {
                        addToGroups(jiraUser, team.getDeveloperGroups());
                    }
                }
            }
        } catch (PermissionException e) {
            e.printStackTrace();
            return Response.serverError().build();
        } catch (CreateException e) {
            e.printStackTrace();
            return Response.serverError().entity("User already exists").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();
        }

        System.out.println(jsonUser.getGithubName());

        return Response.ok().build();
    }

    private void addToGroups(User user, List<String> groupList) throws UserNotFoundException, OperationFailedException,
            GroupNotFoundException, OperationNotPermittedException, InvalidGroupException {
        if(user == null || groupList == null)
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

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class JsonUser {
        @XmlElement
        private String userName;
        @XmlElement
        private String firstName;
        @XmlElement
        private String lastName;
        @XmlElement
        private String email;
        @XmlElement
        private String githubName;
        @XmlElement
        private List<String> coordinatorList;
        @XmlElement
        private List<String> seniorList;
        @XmlElement
        private List<String> developerList;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getGithubName() {
            return githubName;
        }

        public void setGithubName(String githubName) {
            this.githubName = githubName;
        }

        public List<String> getCoordinatorList() {
            return coordinatorList;
        }

        public void setCoordinatorList(List<String> coordinatorList) {
            this.coordinatorList = coordinatorList;
        }

        public List<String> getSeniorList() {
            return seniorList;
        }

        public void setSeniorList(List<String> seniorList) {
            this.seniorList = seniorList;
        }

        public List<String> getDeveloperList() {
            return developerList;
        }

        public void setDeveloperList(List<String> developerList) {
            this.developerList = developerList;
        }
    }
}
