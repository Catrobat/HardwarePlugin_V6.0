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

"use strict";

function populateTeamTable(baseUrl, tableId) {
    AJS.$.ajax({
        url: baseUrl + "/rest/admin-helper/1.0/config/getConfig",
        dataType: "json",
        success: function (config) {
            AJS.$(tableId).empty();
            for (var key in config.teams) {
                var obj = config.teams[key];
                AJS.$(tableId).append("<tr><td headers=\"basic-team\">" + obj['name'] +
                    "</td><td headers=\"basic-coordinator\"><input class=\"radio\" type=\"radio\" name=\"" + obj['name'] +
                    "\" id=\"" + obj['name'] + "-coordinator\" value=\"coordinator\"></td><td headers=\"basic-senior\"><input class=\"radio\" type=\"radio\" name=\"" +
                    obj['name'] + "\" id=\"" + obj['name'] + "-senior\" value=\"senior\"></td><td headers=\"basic-developer\"><input class=\"radio\" type=\"radio\" name=\"" +
                    obj['name'] + "\" id=\"" + obj['name'] + "-developer\" value=\"developer\"></td><td headers=\"basic-none\"><input class=\"radio\" type=\"radio\" checked=\"checked\" name=\"" +
                    obj['name'] + "\" id=\"" + obj['name'] + "-none\" value=\"none\"></td></tr>");
            }
        },
        error: function () {
            AJS.messages.error({
                title: "Error!",
                body: "Something went wrong!"
            });
        }
    });
}

function getTeamList(baseUrl, callme) {
    AJS.$.ajax({
        url: baseUrl + "/rest/admin-helper/1.0/config/getTeamList",
        type: "GET",
        contentType: "application/json",
        success: function (result) {
            callme(result);
        }
    });
}
