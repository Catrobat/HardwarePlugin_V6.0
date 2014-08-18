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

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HardwareServlet extends HelperServlet {

    private final TemplateRenderer renderer;
    private final String dialogContent = "<form action=\"#\" method=\"post\" id=\"d\" class=\"aui\">\n" +
            "    <fieldset>\n" +
            "        <div class=\"field-group\">\n" +
            "            <label for=\"comment\">Device Comment</label>\n" +
            "            <textarea class=\"textarea\" name=\"comment\" id=\"comment\" placeholder=\"Your comment here...\"></textarea>\n" +
            "        </div>\n" +
            "        <div class=\"field-group\">\n" +
            "            <label for=\"d-fname\">Begin Date</label>\n" +
            "            <input class=\"text\" type=\"text\" id=\"d-fname\" name=\"d-fname\" title=\"first name\" placeholder=\"2014-08-18\">\n" +
            "        </div>\n" +
            "        <div class=\"field-group\">\n" +
            "            <label for=\"d-fname\">End Date</label>\n" +
            "            <input class=\"text\" type=\"text\" id=\"d-fname\" name=\"d-fname\" title=\"first name\" placeholder=\"2014-08-18\">\n" +
            "        </div>\n" +
            "        <div class=\"field-group\">\n" +
            "            <label for=\"d-lname\">Lending Purpose</label>\n" +
            "            <input class=\"text long-field\" type=\"text\" id=\"d-lname\" name=\"d-lname\" title=\"last name\">\n" +
            "        </div>\n" +
            "        <div class=\"field-group\">\n" +
            "            <label for=\"comment\">Lending Comment</label>\n" +
            "            <textarea class=\"textarea\" name=\"comment\" id=\"comment\" placeholder=\"Your comment here...\"></textarea>\n" +
            "        </div>\n" +
            "    </fieldset>\n" +
            " </form> ";

    public HardwareServlet(UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer renderer){
        super(userManager, loginUriProvider);
        this.renderer = renderer;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        super.doGet(request, response);
        renderer.render("hardware_overview.vm", response.getWriter());
    }
}