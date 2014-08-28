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

package at.fellhofer.jira.adminhelper;

import at.fellhofer.jira.adminhelper.activeobject.*;
import at.fellhofer.jira.adminhelper.rest.json.JsonDevice;
import at.fellhofer.jira.adminhelper.rest.json.JsonHardwareModel;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

public class BlubTodoServlet extends HttpServlet {
    private final HardwareModelService hardwareModelService;
    private final DeviceService deviceService;
    private final LendingService lendingService;

    public BlubTodoServlet(HardwareModelService hardwareModelService, DeviceService deviceService, LendingService lendingService) {
        this.hardwareModelService = checkNotNull(hardwareModelService);
        this.deviceService = checkNotNull(deviceService);
        this.lendingService = checkNotNull(lendingService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (hardwareModelService.all().size() == 0) {
            hardwareModelService.add("Nexus 4", "Smartphone", "16 GB", "200€", "LG", "Android", "blub");
            hardwareModelService.add("Nexus 7", "Tablet", "16 GB", "200€", "Asus", "Android", "blib");
        }

        if (lendingService.all().size() == 0) {
            UserManager userManager = ComponentAccessor.getUserManager();
            HardwareModel model = hardwareModelService.all().get(0);
            Device device1 = deviceService.add(model, "imei 1", "serial 1", "inventory 1", "received 1", new Date(), "2");
            Device device2 = deviceService.add(model, "imei 2", "serial 2", "inventory 2", "received 2", new Date(), "2");
            Device device3 = deviceService.add(model, "imei 3", "serial 3", "inventory 3", "received 3", new Date(), "2");

            String userKey = "user key";
            for (ApplicationUser user : userManager.getAllApplicationUsers()) {
                userKey = user.getKey();
            }

            Lending lending1 = lendingService.add(device1, userKey, "purpose 1", "comment 1", new Date());
            lending1.setEnd(new Date());
            Lending lending2 = lendingService.add(device2, userKey, "purpose 2", "comment 2", new Date());
            Lending lending3 = lendingService.add(device3, userKey, "purpose 3", "comment 3", new Date());
        }

        if (deviceService.getSortedOutDevices().size() == 0) {
            lendingService.bringBack(lendingService.all().get(0), "back purpose 1", "back comment 1", new Date());

            int id = deviceService.all().get(0).getID();
            deviceService.sortOutDevice(id, new Date(), "sortedOutComment 1");
        }


        final PrintWriter w = res.getWriter();
        w.write("<h1>Todos</h1>");

        // the form to post more TODOs
        w.write("<form method=\"post\">");
        w.write("<input type=\"text\" name=\"task\" size=\"25\"/>");
        w.write("<input type=\"text\" name=\"producer\" size=\"25\"/>");
        w.write("&nbsp;&nbsp;");
        w.write("<input type=\"submit\" name=\"submit\" value=\"Add\"/>");
        w.write("</form>");

        w.write("<ol>");

        for (HardwareModel hardwareModel : hardwareModelService.all()) // (2)
        {
            JsonHardwareModel jsonHardwareModel = null;
            jsonHardwareModel = new JsonHardwareModel(hardwareModel, lendingService, deviceService);
            w.printf("<li>%s, %s, %s</li>", jsonHardwareModel.getID(), jsonHardwareModel.getName(), jsonHardwareModel.getTypeOfDevice());
        }
        w.write("</ol>");

        w.write("<table><tr><td>hardware</td><td>id</td><td>serial</td><td>received</td><td>sortedout</td></tr>");
        for (Device device : deviceService.all()) // (2)
        {
            JsonDevice jsonDevice = null;
            jsonDevice = new JsonDevice(device);
            w.printf("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>", jsonDevice.getHardwareModelName(), jsonDevice.getID(), jsonDevice.getSerialNumber(), jsonDevice.getReceivedDate(), jsonDevice.getSortedOutDate());
        }
        w.write("</table>");


        w.write("<script language='javascript'>document.forms[0].elements[0].focus();</script>");

        w.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final String name = req.getParameter("task");
        final String producer = req.getParameter("producer");
        hardwareModelService.add(name, producer, null, null, null, null, null);

        res.sendRedirect(req.getContextPath() + "/plugins/servlet/todo/list");
    }
}