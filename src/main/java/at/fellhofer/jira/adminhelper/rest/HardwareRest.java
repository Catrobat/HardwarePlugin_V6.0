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

import at.fellhofer.jira.adminhelper.activeobject.HardwareModel;
import at.fellhofer.jira.adminhelper.activeobject.HardwareModelService;
import at.fellhofer.jira.adminhelper.activeobject.Lending;
import at.fellhofer.jira.adminhelper.activeobject.LendingService;
import at.fellhofer.jira.adminhelper.rest.json.JsonDevice;
import at.fellhofer.jira.adminhelper.rest.json.JsonHardwareModel;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.sal.api.user.UserManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("/hardware")
public class HardwareRest {

    private final UserManager userManager;
    private final HardwareModelService hardwareModelService;
    private final LendingService lendingService;

    public HardwareRest(UserManager userManager, HardwareModelService hardwareModelService, LendingService lendingService) {
        this.userManager = checkNotNull(userManager);
        this.hardwareModelService = checkNotNull(hardwareModelService);
        this.lendingService = checkNotNull(lendingService);
    }

    @GET
    @Path("/getHardwareList")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHardwareList(@Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<JsonHardwareModel> hardwareModelList = new ArrayList<JsonHardwareModel>();
        for (HardwareModel hardwareModel : hardwareModelService.all()) {
            try {
                hardwareModelList.add(new JsonHardwareModel(hardwareModel, lendingService));
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                return Response.serverError().entity(e.getMessage()).build();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return Response.serverError().entity(e.getMessage()).build();
            }
        }

        return Response.ok(hardwareModelList).build();
    }

    @GET
    @Path("/getLentOutList")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLentOutList(@Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        com.atlassian.jira.user.util.UserManager jiraUserManager = ComponentAccessor.getUserManager();

        List<JsonDevice> jsonDeviceList = new ArrayList<JsonDevice>();
        for (Lending lending : lendingService.all()) {
            try {
                System.out.println(lending.getDevice().getSerialNumber() + ": " + lending.getBegin() + ", " + lending.getEnd());
                JsonDevice device = new JsonDevice(lending.getDevice());
                device.setCurrentlyLentOutFrom(jiraUserManager.getUserByKey(lending.getLendingUserKey()).getDisplayName());
                device.setCurrentlyLentOutSince(lending.getBegin());
                jsonDeviceList.add(device);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                return Response.serverError().entity(e.getMessage()).build();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return Response.serverError().entity(e.getMessage()).build();
            }
        }

        return Response.ok(jsonDeviceList).build();
    }
}
