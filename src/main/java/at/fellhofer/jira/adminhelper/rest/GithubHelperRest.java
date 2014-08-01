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

import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;

import javax.json.stream.JsonParser;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

@Path("/github")
public class GithubHelperRest {
    private final static String GITHUB_URL = "https://api.github.com";

    private final UserManager userManager;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;

    public GithubHelperRest(UserManager userManager, PluginSettingsFactory pluginSettingsFactory,
                            TransactionTemplate transactionTemplate) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @PUT
    @Path("/searchUser")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final String searchString, @Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String returnValue = doesUserExist(searchString) ? "success" : "failure";

        return Response.ok(returnValue).build();
    }

    public boolean doesUserExist(final String searchString) {
        return (Boolean) transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

                String clientId = (String) settings.get(ConfigResourceRest.KEY_ID);
                String clientSecret = (String) settings.get(ConfigResourceRest.KEY_SECRET);

                String jsonUsers = connect(clientId, clientSecret, "/search/users", "q=" + searchString + "+type:User");
                jsonUsers = jsonUsers.substring(jsonUsers.indexOf("{"));

                try {
                    JSONObject jsonObject = new JSONObject(jsonUsers);
                    JSONArray jsonArray = jsonObject.getJSONArray("items");
                    int length = jsonObject.getInt("total_count");
                    for (int i = 0; i < length; i++) {
                        if (jsonArray.getJSONObject(i).getString("login").equals(searchString)) {
                            return Boolean.TRUE;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return Boolean.FALSE;
            }
        });
    }


    public String connect(String clientId, String clientSecret, String urlAppendix, String... params) {
        StringBuilder sb = new StringBuilder();

        URL url = createURL(clientId, clientSecret, urlAppendix, params);
        if (url == null)
            return sb.append("fail").toString();

        sb.append("success\n");

        InputStream is = null;
        JsonParser parser = null;
        try {
            is = url.openStream();
            //parser = Json.createParser(is);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException ex) {

        } finally {
            try {
                if (parser != null)
                    parser.close();
                if (is != null)
                    is.close();
            } catch (Exception ex) {

            }
        }

        return sb.toString();
    }

    private URL createURL(String clientId, String clientSecret, String appendix, String... params) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("?client_id=" + clientId + "&client_secret=" + clientSecret);
            for (int i = 0; i < params.length; i++) {
                sb.append("&" + params[i]);
            }
            URL url = new URL(GITHUB_URL + appendix + sb.toString());
            return url;
        } catch (MalformedURLException ex) {
            return null;
        }
    }
}
