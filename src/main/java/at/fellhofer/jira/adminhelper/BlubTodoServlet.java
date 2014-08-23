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
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.beanutils.BeanUtils.copyProperties;

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
        if(hardwareModelService.all().size() == 0) {
            hardwareModelService.add("Nexus 4", "Smartphone", "16 GB", "200€", "LG", "Android", "blub");
            hardwareModelService.add("Nexus 7", "Tablet", "16 GB", "200€", "Asus", "Android", "blib");
        }

        if(lendingService.all().size() == 0) {
            UserManager userManager = ComponentAccessor.getUserManager();
            HardwareModel model = hardwareModelService.all().get(0);
            Device device1 = deviceService.add(model, "imei 1", "serial 1", "inventory 1", "received 1", new Date());
            Device device2 = deviceService.add(model, "imei 2", "serial 2", "inventory 2", "received 2", new Date());
            Device device3 = deviceService.add(model, "imei 3", "serial 3", "inventory 3", "received 3", new Date());

            String userKey = "user key";
            for(ApplicationUser user : userManager.getAllApplicationUsers()) {
                userKey = user.getKey();
            }

            Lending lending1 = lendingService.add(device1, userKey, "purpose 1", "comment 1", new Date());
            lending1.setEnd(new Date());
            Lending lending2 = lendingService.add(device2, userKey, "purpose 2", "comment 2", new Date());
            Lending lending3 = lendingService.add(device3, userKey, "purpose 3", "comment 3", new Date());
        }

        if(lendingService.all().size() == 3) {
            lendingService.bringBack(lendingService.all().get(0), "back purpose 1", "back comment 1", new Date());
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
            try {
                jsonHardwareModel = new JsonHardwareModel(hardwareModel, lendingService);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            w.printf("<li>%s, %s</li>", jsonHardwareModel.getName(), jsonHardwareModel.getTypeOfDevice());
        }

        w.write("</ol>");
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