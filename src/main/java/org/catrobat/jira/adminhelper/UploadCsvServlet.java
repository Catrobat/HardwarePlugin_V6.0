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
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.catrobat.jira.adminhelper.activeobject.*;
import org.catrobat.jira.adminhelper.helper.CsvImporter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class UploadCsvServlet extends HelperServlet {

    private final HardwareModelService hardwareModelService;
    private final LendingService lendingService;
    private final DeviceService deviceService;
    private final DeviceCommentService deviceCommentService;
    private final com.atlassian.jira.user.util.UserManager jiraUserManager;

    public UploadCsvServlet(UserManager userManager, LoginUriProvider loginUriProvider, WebSudoManager webSudoManager,
                            GroupManager groupManager, AdminHelperConfigService configurationService,
                            DeviceService deviceService, com.atlassian.jira.user.util.UserManager jiraUserManager,
                            LendingService lendingService, DeviceCommentService deviceCommentService,
                            HardwareModelService hardwareModelService) {
        super(userManager, loginUriProvider, webSudoManager, groupManager, configurationService);
        this.deviceService = deviceService;
        this.jiraUserManager = jiraUserManager;
        this.lendingService = lendingService;
        this.deviceCommentService = deviceCommentService;
        this.hardwareModelService = hardwareModelService;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        super.doGet(request, response);

        PrintWriter writer = response.getWriter();
        writer.print("<html>" +
                "<body>" +
                "<h1>Dangerzone!</h1>" +
                "Just upload files when you know what you're doing - this upload will manipulate the database!<br />" +
                "<form action=\"upload\" method=\"post\"><br />" +
                "<textarea name=\"csv\" rows=\"20\" cols=\"200\" wrap=\"off\">" +
                "# lines beginning with '#' are comments and will be ignored,,,,,,,,,,,,,,,,,,,,,,,\n" +
                "# Name,Version,Type of Device,Operating System,Producer,Article Number,Price,IMEI,Serial Number,Inventory Number,Received Date,Received From,Useful Life Of Asset,Sorted Out Comment,Sorted Out Date,Lending Begin,Lending End,Lending Purpose,Lending Comment,Lending Issuer,Lent By,Device Comment,Device Comment Author,Device Comment Date\n" +
                "Nexus 6,32 GB,Smartphone,Android Lollipop,Motorola,1337,600,123123,123123,123123,14/10/14,Google Inc.,3 Years,Sorted Out Comment,24/10/14,15/10/14,16/10/14,testing,just lending,issuer,lent by,Device Comment,comment author,14/10/14\n" +
                "Nexus 6,32 GB,Smartphone,Android Lollipop,Motorola,1337,600,234234,234234,234234,14/10/14,Google Inc.,3 Years,,,,,,,,,,,\n" +
                "Nexus 6,32 GB,Smartphone,Android Lollipop,Motorola,1337,600,345345,345345,345345,14/10/14,Google Inc.,3 Years,,,15/10/14,,testing 3,just lending 3,issuer 3,lent by 3,Device Comment 3,comment author 3,14/10/14\n" +
                "</textarea><br />\n" +
                "<input type=\"submit\" />" +
                "</form>" +
                "</body>" +
                "</html>");
        writer.flush();
        writer.close();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String csvString = request.getParameter("csv");
        System.out.println(csvString);
        CsvImporter csvImporter = new CsvImporter(hardwareModelService, deviceService, lendingService, deviceCommentService);
        String errorString = csvImporter.importCsv(csvString);

        response.getWriter().print("Successfully executed following string:<br />" +
                "<textarea rows=\"20\" cols=\"200\" wrap=\"off\" disabled>" + csvString + "</textarea>" +
                "<br /><br />" +
                "Following errors occurred:<br />" + errorString);

//        // solution taken from: http://stackoverflow.com/a/2424824
//        try {
////            String csvString = "<empty>";
//            List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
//            for (FileItem item : items) {
//                // item.isFormField() --> Process regular form field (input type="text|radio|checkbox|etc", select, etc)
//                if (!item.isFormField()) {
//                    // Process form file field (input type="file").
//                    StringWriter writer = new StringWriter();
//                    IOUtils.copy(item.getInputStream(), writer);
//                    csvString = writer.toString();
//                    CsvImporter csvImporter = new CsvImporter(hardwareModelService, deviceService, lendingService, deviceCommentService);
//                    csvImporter.importCsv(csvString);
//                }
//            }
//            response.getWriter().print(csvString);
////            RequestDispatcher dispatcher = request.getRequestDispatcher("hardware");
////            dispatcher.forward(request, response);
//        } catch (FileUploadException e) {
//            throw new ServletException("Cannot parse multipart request.", e);
//        }
    }
}