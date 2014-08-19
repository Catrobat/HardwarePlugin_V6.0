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
AJS.toInit(function () {
    var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");
    var teams = [];

    function scrollToAnchor(aid) {
        var aTag = AJS.$("a[name='" + aid + "']");
        AJS.$('html,body').animate({scrollTop: aTag.offset().top}, 'slow');
    }

    function populateForm() {
        AJS.$.ajax({
            url: baseUrl + "/rest/admin-helper/1.0/config/getConfig",
            dataType: "json",
            success: function (config) {
                AJS.$("#github_token").attr("value", config.githubToken);
                AJS.$("#github_organization").attr("value", config.githubOrganization);
                teams = [];
                for (var i = 0; i < config.teams.length; i++) {
                    var obj = config.teams[i];
                    teams.push(obj['name']);
                    AJS.$("#teams").append("<h3>" + obj['name'] + "</h3><fieldset>");
                    AJS.$("#teams").append("<div class=\"field-group\"><label for=\"" + obj['name'] + "-github-teams\">GitHub Teams</label><input class=\"text\" type=\"text\" id=\"" + obj['name'] + "-github-teams\" name=\"github-teams\" value=\"" + obj['githubTeams'] + "\"><div class=\"description\">User gets added to those comma separated teams.</div></div>");
                    AJS.$("#teams").append("<div class=\"field-group\"><label for=\"" + obj['name'] + "-coordinator\">Coordinator</label><input class=\"text\" type=\"text\" id=\"" + obj['name'] + "-coordinator\" value=\"" + obj['coordinatorGroups'] + "\"><div class=\"description\">User gets added to those comma separated groups.</div></div>");
                    AJS.$("#teams").append("<div class=\"field-group\"><label for=\"" + obj['name'] + "-senior\">Senior</label><input class=\"text\" type=\"text\" id=\"" + obj['name'] + "-senior\" value=\"" + obj['seniorGroups'] + "\"><div class=\"description\">User gets added to those comma separated groups.</div></div>");
                    AJS.$("#teams").append("<div class=\"field-group\"><label for=\"" + obj['name'] + "-developer\">Developer</label><input class=\"text\" type=\"text\" id=\"" + obj['name'] + "-developer\" value=\"" + obj['developerGroups'] + "\"><div class=\"description\">User gets added to those comma separated groups.</div></div>");
                    AJS.$("#teams").append("</fieldset>");
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

    function updateConfig() {
        var config = {};
        config.githubToken = AJS.$("#github_token").attr("value");
        config.githubOrganization = AJS.$("#github_organization").attr("value");
        config.teams = [];
        for (var i = 0; i < teams.length; i++) {
            var tempTeam = {};
            tempTeam.name = teams[i];
            tempTeam.githubTeams = AJS.$("#" + tempTeam.name + "-github-teams").attr("value").split(",");
            tempTeam.coordinatorGroups = AJS.$("#" + tempTeam.name + "-coordinator").attr("value").split(",");
            tempTeam.seniorGroups = AJS.$("#" + tempTeam.name + "-senior").attr("value").split(",");
            tempTeam.developerGroups = AJS.$("#" + tempTeam.name + "-developer").attr("value").split(",");
            config.teams.push(tempTeam);
        }

        AJS.$.ajax({
            url: baseUrl + "/rest/admin-helper/1.0/config/saveConfig",
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify(config),
            processData: false,
            success: function () {
                AJS.messages.success({
                    title: "Success!",
                    body: "Settings saved!"
                });
            },
            error: function () {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!"
                });
            }
        });
    }

    function addTeam() {
        AJS.$.ajax({
            url: baseUrl + "/rest/admin-helper/1.0/config/addTeam",
            type: "PUT",
            contentType: "application/json",
            data: AJS.$("#team").attr("value"),
            processData: false,
            success: function () {
                AJS.messages.success({
                    title: "Success!",
                    body: "Team added!"
                });
            },
            error: function () {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!"
                });
            }
        });
    }

    function removeTeam() {
        AJS.$.ajax({
            url: baseUrl + "/rest/admin-helper/1.0/config/removeTeam",
            type: "PUT",
            contentType: "application/json",
            data: AJS.$("#team").attr("value"),
            processData: false,
            success: function () {
                AJS.messages.success({
                    title: "Success!",
                    body: "Team removed!"
                });
            },
            error: function () {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!"
                });
            }
        });
    }

    populateForm();

    AJS.$("#general").submit(function (e) {
        e.preventDefault();
        updateConfig();
        scrollToAnchor('top');
    });

    AJS.$("#modify-teams").submit(function (e) {
        e.preventDefault();
        addTeam();
        scrollToAnchor('top');
    });

    AJS.$("#remove").click(function (e) {
        e.preventDefault();
        removeTeam();
        scrollToAnchor('top');
    });

    AJS.$("a[href='#tabs-general']").click(function () {
        AJS.$("#teams").html("");
        populateForm();
    })
});