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

var tableSkeleton = "<table class=\"aui\">\n" +
    "<thead>\n" +
    "<tr>\n" +
    "<th id=\"basic-team\">Team</th>\n" +
    "<th id=\"basic-coordinator\">Coordinator</th>\n" +
    "<th id=\"basic-senior\">Senior</th>\n" +
    "<th id=\"basic-developer\">Developer</th>\n" +
    "<th id=\"basic-none\">None</th>\n" +
    "</tr>\n" +
    "</thead>\n" +
    "<tbody id=\"team-body\">\n" +
    "</tbody>\n" +
    "</table>";

function getGithubForm(githubUsername, checkBoxSet) {
    return "<form id=\"d\" class=\"aui\">\n" +
        "    <fieldset>\n" +
        "        <div class=\"field-group\">\n" +
        "            <label for=\"github-name\">GitHub Name<span class=\"aui-icon icon-required\"> required</span></label>\n" +
        "            <input class=\"text\" type=\"text\" id=\"github-name\" name=\"github-name\" title=\"github-name\" value=\"" + githubUsername + "\">\n" +
        "        </div>\n" +
        checkBoxSet +
        "    </fieldset>\n" +
        " </form>   ";
}

AJS.toInit(function () {
    var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");

    var dialog = new AJS.Dialog({
        width: 840,
        height: 400,
        id: "activate-dialog",
        closeOnOutsideClick: true
    });

    dialog.addHeader("Activate User");
    dialog.addPanel("Panel 1", tableSkeleton, "panel-body");

    dialog.addButton("OK", function (dialog) {
        getTeamList(baseUrl, modifyUser);
        dialog.hide();
    });
    dialog.addLink("Cancel", function (dialog) {
        dialog.hide();
    }, "#");

    function populateTable() {
        AJS.$.ajax({
            url: baseUrl + "/rest/admin-helper/1.0/user/getUsers",
            dataType: "json",
            success: function (users) {
                AJS.$("#user-body").empty();
                for (var i = 0; i < users.length; i++) {
                    var obj = users[i];
                    var username = obj['active'] ? obj['userName'] : "<del>" + obj['userName'] + "</del>";
                    var actionClass = obj['active'] ? "inactivate" : "activate";
                    var githubColumnText = obj['githubName'] ? obj['githubName'] : "add GitHub name";
                    var githubColumn = "<a id=\"" + obj['userName'] + "\" class=\"change-github\" href=\"#\">" + githubColumnText + "</a>";
                    AJS.$("#user-body").append("<tr><td headers=\"basic-username\" class=\"username\">" + username + "</td>" +
                        "<td headers=\"basic-first-name\" class=\"first-name\">" + obj['firstName'] + "</td>" +
                        "<td headers=\"basic-last-name\" class=\"last-name\">" + obj['lastName'] + "</td>" +
                        "<td headers=\"basic-email\" class=\"email\">" + obj['email'] + "</td>" +
                        "<td headers=\"basic-github\" class=\"github\">" + githubColumn + "</td>" +
                        "<td headers=\"basic-action\" class=\"action\"><a id=\"" + obj['userName'] + "\" class=\"" + actionClass + "\" href=\"#\">" + actionClass + "</a></tr>");
                }

                AJS.$("#user-table").trigger("update");
                var userList = new List("modify-user", {valueNames: ["username", "first-name", "last-name", "email", "github", "action"]});
            },
            error: function () {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!"
                });
            }
        }).done(function () {
            AJS.$(".inactivate").click(function (event) {
                event.preventDefault();
                inactivateUser(event.target.id);
            });
            AJS.$(".activate").click(function (event) {
                event.preventDefault();
                activateUser(event.target.id);
            });
            AJS.$(".change-github").click(function (event) {
                event.preventDefault();
                showChangeGithubDialog(event.target.id, AJS.$(event.target).text());
            });
        });
    }

    function showChangeGithubDialog(userName, githubName) {
        getTeamList(baseUrl, function (teamList) {
            var checkboxSet = "<fieldset class=\"group\">\n" +
                "<legend><span>Team</span></legend>\n";
            for (var i = 0; i < teamList.length; i++) {
                checkboxSet += "<div class=\"checkbox\">\n" +
                    "<input class=\"checkbox\" type=\"checkbox\" name=\"" + teamList[i] + "\" id=\"" + teamList[i] + "\">\n" +
                    "<label for=\"" + teamList[i] + "\">" + teamList[i] + "</label>\n" +
                    "</div>\n";
            }
            checkboxSet += "</fieldset>";

            var dialog = new AJS.Dialog({
                width: 600,
                height: 400,
                id: "change-github-dialog",
                closeOnOutsideClick: true
            });

            githubName = githubName == "add GitHub name" ? "" : githubName;

            dialog.addHeader("Change GitHub User");
            dialog.addPanel("Panel 1", getGithubForm(githubName, checkboxSet), "panel-body");

            dialog.addSubmit("OK", function (dialog) {
                var selectedTeamList = [];
                for (var i = 0; i < teamList.length; i++) {
                    if (AJS.$("#" + teamList[i]).prop("checked")) {
                        selectedTeamList.push(teamList[i]);
                    }
                }
                changeGithubUser(userName, AJS.$("#github-name").val(), selectedTeamList);
                dialog.remove();
            });
            dialog.addLink("Cancel", function (dialog) {
                dialog.remove();
            }, "#");

            dialog.show();
        });
    }

    function changeGithubUser(userName, githubName, teamList) {
        var jsonUser = {userName: userName, githubName: githubName, developerList: teamList};
        AJS.$.ajax({
            url: baseUrl + "/rest/admin-helper/1.0/github/changeGithubname",
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify(jsonUser),
            success: function () {
                populateTable();
                AJS.messages.success({
                    title: "Success!",
                    body: "GitHub Name changed!"
                });
            },
            error: function (e) {
                AJS.messages.error({
                    title: "Error!",
                    body: e.responseText
                });
            }
        });
    }

    function modifyUser(teamList) {
        var userToModify = {};
        userToModify.userName = dialog.userName;
        userToModify.coordinatorList = [];
        userToModify.seniorList = [];
        userToModify.developerList = [];
        for (var i = 0; i < teamList.length; i++) {
            var value = AJS.$("input[name='" + teamList[i] + "']:checked").val();
            if (value == "coordinator") {
                userToModify.coordinatorList.push(teamList[i]);
            } else if (value == "senior") {
                userToModify.seniorList.push(teamList[i]);
            } else if (value == "developer") {
                userToModify.developerList.push(teamList[i]);
            }
        }

        AJS.$.ajax({
            url: baseUrl + "/rest/admin-helper/1.0/user/activateUser",
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify(userToModify),
            success: function () {
                populateTable();
                AJS.messages.success({
                    title: "Success!",
                    body: "User activated!"
                });
            },
            error: function (e) {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!<br />" + e.responseText
                });
            }
        });
    }

    function inactivateUser(userName) {
        AJS.$.ajax({
            url: baseUrl + "/rest/admin-helper/1.0/user/inactivateUser",
            type: "PUT",
            contentType: "application/json",
            data: userName,
            success: function () {
                populateTable();
                AJS.messages.success({
                    title: "Success!",
                    body: "User inactivated!"
                });
            },
            error: function (e) {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!<br />" + e.responseText
                });
            }
        });
    }

    function activateUser(userName) {
        dialog.gotoPage(0);
        dialog.gotoPanel(0);
        dialog.userName = userName;
        dialog.show();

        populateTeamTable(baseUrl, "#team-body");
    }

    populateTable();


});