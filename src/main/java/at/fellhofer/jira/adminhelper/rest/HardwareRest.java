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

import at.fellhofer.jira.adminhelper.activeobject.*;
import at.fellhofer.jira.adminhelper.rest.json.JsonDevice;
import at.fellhofer.jira.adminhelper.rest.json.JsonHardwareModel;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.user.UserManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("/hardware")
public class HardwareRest {

    private final UserManager userManager;
    private final HardwareModelService hardwareModelService;
    private final DeviceService deviceService;
    private final LendingService lendingService;

    public HardwareRest(UserManager userManager, HardwareModelService hardwareModelService, DeviceService deviceService, LendingService lendingService) {
        this.userManager = checkNotNull(userManager);
        this.hardwareModelService = checkNotNull(hardwareModelService);
        this.deviceService = checkNotNull(deviceService);
        this.lendingService = checkNotNull(lendingService);
    }

    @GET
    @Path("/hardwares")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHardwares(@Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<JsonHardwareModel> hardwareModelList = new ArrayList<JsonHardwareModel>();
        for (HardwareModel hardwareModel : hardwareModelService.all()) {
            hardwareModelList.add(new JsonHardwareModel(hardwareModel, lendingService, deviceService));
        }

        return Response.ok(hardwareModelList).build();
    }

    @PUT
    @Path("/hardwares")
    @Produces(MediaType.APPLICATION_JSON)
    public Response putHardware(@Context HttpServletRequest request, JsonHardwareModel jsonHardwareModel) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // TODO error detection here

        hardwareModelService.add(jsonHardwareModel.getName(), jsonHardwareModel.getTypeOfDevice(),
                jsonHardwareModel.getVersion(), jsonHardwareModel.getPrice(), jsonHardwareModel.getProducer(),
                jsonHardwareModel.getOperatingSystem(), jsonHardwareModel.getArticleNumber());

        return Response.noContent().build();
    }

    @GET
    @Path("/hardwares/{hardwareId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHardware(@Context HttpServletRequest request, @PathParam("hardwareId") int hardwareId) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        JsonHardwareModel jsonHardwareModel = new JsonHardwareModel(hardwareModelService.get(hardwareId), lendingService, deviceService);
        return Response.ok(jsonHardwareModel).build();
    }

    @PUT
    @Path("/hardwares/{hardwareId}/devices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addDevice(@Context HttpServletRequest request, @PathParam("hardwareId") int hardwareId, JsonDevice jsonDevice) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (((jsonDevice.getSerialNumber() == null || jsonDevice.getSerialNumber().equals("")) &&
                (jsonDevice.getImei() == null || jsonDevice.getImei().equals("")) &&
                (jsonDevice.getInventoryNumber() == null || jsonDevice.getInventoryNumber().equals("")))) {
            return Response.serverError().entity("At least one unique identifier must be given").build();
        }

        HardwareModel hardwareModel = hardwareModelService.get(hardwareId);
        if (hardwareModel == null) {
            return Response.serverError().entity("Hardware Device not found").build();
        }

        Device device = deviceService.add(hardwareModel, jsonDevice.getImei(), jsonDevice.getSerialNumber(),
                jsonDevice.getInventoryNumber(), jsonDevice.getReceivedFrom(), jsonDevice.getReceivedDate(),
                jsonDevice.getUsefulLiveOfAsset());

        if(device == null) {
            return Response.serverError().entity("Device not created - maybe serial/inventory/imei already exists?").build();
        }

        return Response.noContent().build();
    }

    @GET
    @Path("/hardwares/{hardwareId}/devices/available")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevicesAvailableForHardware(@Context HttpServletRequest request, @PathParam("hardwareId") int hardwareId) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<JsonDevice> jsonDeviceList = new ArrayList<JsonDevice>();
        for (Device device : deviceService.getCurrentlyAvailableDevices(hardwareId)) {
            jsonDeviceList.add(new JsonDevice(device));
        }

        return Response.ok(jsonDeviceList).build();
    }

//    @GET
//    @Path("/lendings")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getLendings(@Context HttpServletRequest request) {
//        String username = userManager.getRemoteUsername(request);
//        if (username == null || !userManager.isSystemAdmin(username)) {
//            return Response.status(Response.Status.UNAUTHORIZED).build();
//        }
//
//        com.atlassian.jira.user.util.UserManager jiraUserManager = ComponentAccessor.getUserManager();
//
//        List<JsonDevice> jsonDeviceList = new ArrayList<JsonDevice>();
//        for (Lending lending : lendingService.all()) {
//            JsonDevice device = new JsonDevice(lending.getDevice());
//            device.setCurrentlyLentOutFrom(jiraUserManager.getUserByKey(lending.getLendingUserKey()).getDisplayName());
//            device.setCurrentlyLentOutSince(lending.getBegin());
//            jsonDeviceList.add(device);
//        }
//
//        return Response.ok(jsonDeviceList).build();
//    }

    @GET
    @Path("/devices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevices() {
        return null;
    }

    @GET
    @Path("/devices/sorted-out")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSortedOutDevices(@Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<JsonDevice> jsonDeviceList = new ArrayList<JsonDevice>();
        for (Device device : deviceService.getSortedOutDevices()) {
            jsonDeviceList.add(new JsonDevice(device));
        }

        return Response.ok(jsonDeviceList).build();
    }

    @GET
    @Path("/devices/ongoing-lendings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLentOutList(@Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        com.atlassian.jira.user.util.UserManager jiraUserManager = ComponentAccessor.getUserManager();

        List<JsonDevice> jsonDeviceList = new ArrayList<JsonDevice>();
        for (Lending lending : lendingService.currentlyLentOut()) {
            JsonDevice device = new JsonDevice(lending.getDevice());
            ApplicationUser user = jiraUserManager.getUserByKey(lending.getLendingUserKey());
            if (user != null)
                device.setCurrentlyLentOutFrom(user.getDisplayName());
            device.setCurrentlyLentOutSince(lending.getBegin());
            jsonDeviceList.add(device);
        }

        return Response.ok(jsonDeviceList).build();
    }

    @GET
    @Path("/devices/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevice(@PathParam("deviceId") int deviceId) {
        return null;
    }

//    @GET
//    @Path("/devices/{deviceId}/comments")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getDeviceComments() {
//        return null;
//    }
}
