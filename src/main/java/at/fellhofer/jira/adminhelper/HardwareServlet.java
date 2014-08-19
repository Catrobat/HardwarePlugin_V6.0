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
    private final String dialogContent = "<form class=\"aui\">\n" +
            "        <p>Are you sure to delete this Hardware Model?</p>\n" +
            "\n" +
            "        <p>All allocated devices need to be moved to another Hardware Model.</p>\n" +
            "        <fieldset>\n" +
            "            <div class=\"field-group\">\n" +
            "                <label for=\"dBase\">Move to</label>\n" +
            "                <select class=\"select\" id=\"dBase\" name=\"dBase\" title=\"database select\">\n" +
            "                    <option>Select</option>\n" +
            "                    <optgroup label=\"Nexus 4\">\n" +
            "                        <option>8 GB</option>\n" +
            "                        <option>16 GB</option>\n" +
            "                    </optgroup>\n" +
            "                    <option>Nexus 7</option>\n" +
            "                </select>\n" +
            "            </div>\n" +
            "        </fieldset>\n" +
            "    </form>";

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