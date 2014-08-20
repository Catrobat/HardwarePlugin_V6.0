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

import at.fellhofer.jira.adminhelper.activeobject.entity.HardwareModel;
import at.fellhofer.jira.adminhelper.activeobject.service.HardwareModelService;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.transaction.TransactionCallback;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.google.common.base.Preconditions.checkNotNull;

public class BlubTodoServlet extends HttpServlet {
    private final HardwareModelService hardwareModelService;

    public BlubTodoServlet(HardwareModelService hardwareModelService) {
        this.hardwareModelService = checkNotNull(hardwareModelService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
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
            w.printf("<li>%s, %s</li>", hardwareModel.getName(), hardwareModel.getTypeOfDevice());
        }

        w.write("</ol>");
        w.write("<script language='javascript'>document.forms[0].elements[0].focus();</script>");

        w.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        final String name = req.getParameter("task");
        final String producer = req.getParameter("producer");
        hardwareModelService.add(name, producer, null, null, null, null, null);

        res.sendRedirect(req.getContextPath() + "/plugins/servlet/todo/list");
    }
}