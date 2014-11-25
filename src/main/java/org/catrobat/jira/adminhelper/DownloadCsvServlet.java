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

package org.catrobat.jira.adminhelper;

import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.websudo.WebSudoManager;
import org.catrobat.jira.adminhelper.activeobject.AdminHelperConfigService;
import org.catrobat.jira.adminhelper.activeobject.DeviceService;
import org.catrobat.jira.adminhelper.helper.CsvExporter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class DownloadCsvServlet extends HelperServlet {

    private final DeviceService deviceService;
    private final com.atlassian.jira.user.util.UserManager jiraUserManager;

    public DownloadCsvServlet(UserManager userManager, LoginUriProvider loginUriProvider, WebSudoManager webSudoManager,
                              GroupManager groupManager, AdminHelperConfigService configurationService,
                              DeviceService deviceService, com.atlassian.jira.user.util.UserManager jiraUserManager) {
        super(userManager, loginUriProvider, webSudoManager, groupManager, configurationService);
        this.deviceService = deviceService;
        this.jiraUserManager = jiraUserManager;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        super.doGet(request, response);

        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"lending.csv\"");

        CsvExporter csvExporter = new CsvExporter(deviceService.all(), jiraUserManager);
        PrintStream printStream = new PrintStream(response.getOutputStream(), false, "UTF-8");
        printStream.print(csvExporter.getCsvString());
        printStream.flush();
        printStream.close();
    }
}