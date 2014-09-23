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

package ut.at.fellhofer.jira.adminhelper.activeobject;

import at.fellhofer.jira.adminhelper.activeobject.*;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import net.java.ao.EntityManager;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(AdminHelperDatabaseUpdater.class)
public class AdminHelperConfigServiceImplTest {

    private EntityManager entityManager;
    private ActiveObjects ao;
    private AdminHelperConfigService configurationService;

    @Before
    public void setUp() throws Exception {
        assertNotNull(entityManager);
        ao = new TestActiveObjects(entityManager);
        configurationService = new AdminHelperConfigServiceImpl(ao);
    }

    @Test
    public void testGetConfiguration() {
        assertEquals(0, ao.find(AdminHelperConfig.class).length);
        assertNotNull(configurationService.getConfiguration());
        ao.flushAll();
        configurationService.getConfiguration();
        configurationService.getConfiguration();
        ao.flushAll();
        assertEquals(1, ao.find(AdminHelperConfig.class).length);

        AdminHelperConfig configuration = configurationService.getConfiguration();
        assertTrue(configuration.getID() != 0);
        assertNull(configuration.getGithubApiToken());
        assertNull(configuration.getGithubOrganisation());
        assertEquals(0, configuration.getApprovedGroups().length);
        assertEquals(0, configuration.getTeams().length);
    }

    @Test
    public void testSetPublicApiToken() {
        AdminHelperConfig config = configurationService.getConfiguration();
        assertEquals(null, config.getPublicGithubApiToken());

        assertNotNull(configurationService.setPublicApiToken(" "));
        ao.flushAll();
        config = configurationService.getConfiguration();
        assertEquals(" ", config.getPublicGithubApiToken());

        assertNotNull(configurationService.setPublicApiToken(""));
        ao.flushAll();
        config = configurationService.getConfiguration();
        assertEquals("", config.getPublicGithubApiToken());

        assertNotNull(configurationService.setPublicApiToken(null));
        ao.flushAll();
        config = configurationService.getConfiguration();
        assertEquals(null, config.getPublicGithubApiToken());

        assertNotNull(configurationService.setPublicApiToken("blob"));
        ao.flushAll();
        config = configurationService.getConfiguration();
        assertEquals("blob", config.getPublicGithubApiToken());
    }

    @Test
    public void testSetApiToken() {
        AdminHelperConfig config = configurationService.getConfiguration();
        assertEquals(null, config.getGithubApiToken());

        assertNotNull(configurationService.setApiToken(" "));
        ao.flushAll();
        config = configurationService.getConfiguration();
        assertEquals(" ", config.getGithubApiToken());

        assertNotNull(configurationService.setApiToken(""));
        ao.flushAll();
        config = configurationService.getConfiguration();
        assertEquals("", config.getGithubApiToken());

        assertNotNull(configurationService.setApiToken(null));
        ao.flushAll();
        config = configurationService.getConfiguration();
        assertEquals(null, config.getGithubApiToken());

        assertNotNull(configurationService.setApiToken("blob"));
        ao.flushAll();
        config = configurationService.getConfiguration();
        assertEquals("blob", config.getGithubApiToken());
    }

    @Test
    public void testSetOrganisation() {
        AdminHelperConfig config = configurationService.getConfiguration();
        assertEquals(null, config.getGithubOrganisation());

        assertNotNull(configurationService.setOrganisation(" "));
        ao.flushAll();
        config = configurationService.getConfiguration();
        assertEquals(" ", config.getGithubOrganisation());

        assertNotNull(configurationService.setOrganisation(""));
        ao.flushAll();
        config = configurationService.getConfiguration();
        assertEquals("", config.getGithubOrganisation());

        assertNotNull(configurationService.setOrganisation(null));
        ao.flushAll();
        config = configurationService.getConfiguration();
        assertEquals(null, config.getGithubOrganisation());

        assertNotNull(configurationService.setOrganisation("blob"));
        ao.flushAll();
        config = configurationService.getConfiguration();
        assertEquals("blob", config.getGithubOrganisation());
    }

    @Test
    public void testAddTeam() {
        AdminHelperConfig config;

        List<Integer> githubIdList = new ArrayList<Integer>();
        githubIdList.add(1);
        githubIdList.add(2);

        assertNotNull(configurationService.addTeam("team1", githubIdList,
                Arrays.asList("coordinator", "got", "usually", "more groups"),
                Arrays.asList("seniors", "got", "some less"),
                Arrays.asList("developers")));
        assertNotNull(configurationService.addTeam("team2", githubIdList,
                Arrays.asList("coordinator", "got", "usually", "more groups"),
                Arrays.asList("seniors", "got", "some less"),
                Arrays.asList("developers")));
        ao.flushAll();
        config = configurationService.getConfiguration();
        assertEquals(2, config.getTeams().length);
        assertEquals("team1", config.getTeams()[0].getTeamName());
        assertEquals(2, config.getTeams()[0].getGithubTeams().length);
        assertEquals(8, config.getTeams()[0].getGroups().length);
        assertEquals(7, ao.find(Group.class).length);
        assertEquals(TeamToGroup.Role.COORDINATOR, config.getTeams()[0].getGroups()[1].getTeamToGroups()[0].getRole());
        assertEquals("got", config.getTeams()[0].getGroups()[1].getGroupName());
        assertEquals(TeamToGroup.Role.SENIOR, config.getTeams()[0].getGroups()[5].getTeamToGroups()[1].getRole());
        assertEquals("got", config.getTeams()[0].getGroups()[5].getGroupName());

        assertNotNull(configurationService.addTeam("team3", null, null, null, null));
        assertNull(configurationService.addTeam(null, null, null, null, null));
        assertNull(configurationService.addTeam("", null, null, null, null));
        assertNull(configurationService.addTeam("  ", null, null, null, null));
    }

    @Test
    public void testRemoveTeam() {
        testAddTeam();
        assertEquals(3, configurationService.getConfiguration().getTeams().length);
        assertNotNull(configurationService.removeTeam("team1"));
        ao.flushAll();
        assertEquals(2, configurationService.getConfiguration().getTeams().length);

        assertNull(configurationService.removeTeam(null));
        assertNull(configurationService.removeTeam("blob"));
        ao.flushAll();
        assertEquals(2, configurationService.getConfiguration().getTeams().length);
    }

    @Test
    public void testGetGroupsForRole() {
        testAddTeam();
        assertEquals(3, configurationService.getConfiguration().getTeams().length);

        List<String> groupList = configurationService.getGroupsForRole("team1", TeamToGroup.Role.COORDINATOR);
        assertEquals(4, groupList.size());
        assertEquals("coordinator", groupList.get(0));
        assertEquals("got", groupList.get(1));
        assertEquals("usually", groupList.get(2));
        assertEquals("more groups", groupList.get(3));
    }

    @Test
    public void testIsGroupApproved() {
        // should return true when no group and no user is defined (or you'll get locked out)
        assertTrue(configurationService.isGroupApproved("blob"));
        assertTrue(configurationService.isGroupApproved(null));
        assertTrue(configurationService.isGroupApproved("  "));

        configurationService.addApprovedUser("blub");
        ao.flushAll();
        assertFalse(configurationService.isGroupApproved("blob"));
        assertFalse(configurationService.isGroupApproved(null));
        assertFalse(configurationService.isGroupApproved("  "));

        assertEquals(0, ao.find(ApprovedGroup.class).length);
        assertNotNull(configurationService.addApprovedGroup("blob"));
        ao.flushAll();

        assertTrue(configurationService.isGroupApproved("blob"));
        assertTrue(configurationService.isGroupApproved("BlOb"));
        assertTrue(configurationService.isGroupApproved(" BLOB  "));
        assertFalse(configurationService.isGroupApproved(null));
        assertFalse(configurationService.isGroupApproved(""));
        assertFalse(configurationService.isGroupApproved("  "));
        assertFalse(configurationService.isGroupApproved("blab"));
    }

    @Test
    public void testAddApprovedGroup() {
        assertEquals(0, ao.find(ApprovedGroup.class).length);
        assertNotNull(configurationService.addApprovedGroup("blob"));
        ao.flushAll();
        assertEquals(1, ao.find(ApprovedGroup.class).length);

        assertNotNull(configurationService.addApprovedGroup("blob"));
        ao.flushAll();
        assertEquals(1, ao.find(ApprovedGroup.class).length);

        assertNull(configurationService.addApprovedGroup(null));
        assertNull(configurationService.addApprovedGroup(""));
        assertNull(configurationService.addApprovedGroup("  "));
        ao.flushAll();
        assertEquals(1, ao.find(ApprovedGroup.class).length);
    }

    @Test
    public void testClearApprovedGroups() {
        assertNotNull(configurationService.addApprovedGroup("1"));
        assertNotNull(configurationService.addApprovedGroup("2"));
        assertNotNull(configurationService.addApprovedGroup("3"));
        assertNotNull(configurationService.addApprovedGroup("4"));
        ao.flushAll();
        assertEquals(4, ao.find(ApprovedGroup.class).length);

        configurationService.clearApprovedGroups();
        ao.flushAll();
        assertEquals(0, ao.find(ApprovedGroup.class).length);
    }

    @Test
    public void testRemoveApprovedGroup() {
        assertNotNull(configurationService.addApprovedGroup("blob"));
        assertNotNull(configurationService.addApprovedGroup("BLAB"));
        ao.flushAll();
        assertEquals(2, ao.find(ApprovedGroup.class).length);

        assertNotNull(configurationService.removeApprovedGroup(" BLOB   "));
        ao.flushAll();
        assertEquals(1, ao.find(ApprovedGroup.class).length);

        assertNull(configurationService.removeApprovedGroup(null));
        assertNull(configurationService.removeApprovedGroup("  "));
        assertNull(configurationService.removeApprovedGroup(""));
        assertNull(configurationService.removeApprovedGroup("blob"));
        assertNull(configurationService.removeApprovedGroup("blah"));
        ao.flushAll();
        assertEquals(1, ao.find(ApprovedGroup.class).length);

        assertNotNull(configurationService.removeApprovedGroup("blab"));
        ao.flushAll();
        assertEquals(0, ao.find(ApprovedGroup.class).length);
    }

    @Test
    public void testIsUserApproved() {
        // should return false when no user is defined
        assertFalse(configurationService.isUserApproved("blob"));
        assertFalse(configurationService.isUserApproved(null));
        assertFalse(configurationService.isUserApproved("  "));

        assertEquals(0, ao.find(ApprovedUser.class).length);
        assertNotNull(configurationService.addApprovedUser("blob"));
        ao.flushAll();

        assertTrue(configurationService.isUserApproved("blob"));
        assertTrue(configurationService.isUserApproved("BlOb"));
        assertTrue(configurationService.isUserApproved(" BLOB  "));
        assertFalse(configurationService.isUserApproved(null));
        assertFalse(configurationService.isUserApproved(""));
        assertFalse(configurationService.isUserApproved("  "));
        assertFalse(configurationService.isUserApproved("blab"));
    }

    @Test
    public void testAddApprovedUser() {
        assertEquals(0, ao.find(ApprovedUser.class).length);
        assertNotNull(configurationService.addApprovedUser("blob"));
        ao.flushAll();
        assertEquals(1, ao.find(ApprovedUser.class).length);

        assertNotNull(configurationService.addApprovedUser("blob"));
        ao.flushAll();
        assertEquals(1, ao.find(ApprovedUser.class).length);

        assertNull(configurationService.addApprovedUser(null));
        assertNull(configurationService.addApprovedUser(""));
        assertNull(configurationService.addApprovedUser("  "));
        ao.flushAll();
        assertEquals(1, ao.find(ApprovedUser.class).length);
    }

    @Test
    public void testClearApprovedUsers() {
        assertNotNull(configurationService.addApprovedUser("1"));
        assertNotNull(configurationService.addApprovedUser("2"));
        assertNotNull(configurationService.addApprovedUser("3"));
        assertNotNull(configurationService.addApprovedUser("4"));
        ao.flushAll();
        assertEquals(4, ao.find(ApprovedUser.class).length);

        configurationService.clearApprovedUsers();
        ao.flushAll();
        assertEquals(0, ao.find(ApprovedUser.class).length);
    }

    @Test
    public void testRemoveApprovedUser() {
        assertNotNull(configurationService.addApprovedUser("blob"));
        assertNotNull(configurationService.addApprovedUser("BLAB"));
        ao.flushAll();
        assertEquals(2, ao.find(ApprovedUser.class).length);

        assertNotNull(configurationService.removeApprovedUser(" BLOB   "));
        ao.flushAll();
        assertEquals(1, ao.find(ApprovedUser.class).length);

        assertNull(configurationService.removeApprovedUser(null));
        assertNull(configurationService.removeApprovedUser("  "));
        assertNull(configurationService.removeApprovedUser(""));
        assertNull(configurationService.removeApprovedUser("blob"));
        assertNull(configurationService.removeApprovedUser("blah"));
        ao.flushAll();
        assertEquals(1, ao.find(ApprovedUser.class).length);

        assertNotNull(configurationService.removeApprovedUser("blab"));
        ao.flushAll();
        assertEquals(0, ao.find(ApprovedUser.class).length);
    }
}
