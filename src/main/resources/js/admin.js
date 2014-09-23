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
                if (config.githubToken)
                    AJS.$("#github_token").attr("placeholder", config.githubToken);
                if (config.githubTokenPublic)
                    AJS.$("#github_token_public").val(config.githubTokenPublic);
                if (config.githubOrganization)
                    AJS.$("#github_organization").val(config.githubOrganization);
                teams = [];
                for (var i = 0; i < config.teams.length; i++) {
                    var team = config.teams[i];
                    teams.push(team['name']);
                    AJS.$("#teams").append("<h3>" + team['name'] + "</h3><fieldset>");
                    AJS.$("#teams").append("<div class=\"field-group\"><label for=\"" + team['name'] + "-github-teams\">GitHub Teams</label><input class=\"text github\" type=\"text\" id=\"" + team['name'] + "-github-teams\" name=\"github-teams\" value=\"" + team["githubTeams"] + "\"></div>");
                    AJS.$("#teams").append("<div class=\"field-group\"><label for=\"" + team['name'] + "-coordinator\">Coordinator</label><input class=\"text jira-group\" type=\"text\" id=\"" + team['name'] + "-coordinator\" value=\"" + team['coordinatorGroups'] + "\"></div>");
                    AJS.$("#teams").append("<div class=\"field-group\"><label for=\"" + team['name'] + "-senior\">Senior</label><input class=\"text jira-group\" type=\"text\" id=\"" + team['name'] + "-senior\" value=\"" + team['seniorGroups'] + "\"></div>");
                    AJS.$("#teams").append("<div class=\"field-group\"><label for=\"" + team['name'] + "-developer\">Developer</label><input class=\"text jira-group\" type=\"text\" id=\"" + team['name'] + "-developer\" value=\"" + team['developerGroups'] + "\"></div>");
                    AJS.$("#teams").append("</fieldset>");
                }

                if(config.availableGithubTeams) {
                    AJS.$(".github").auiSelect2({
                        placeholder: "Search for teams",
                        tags: config.availableGithubTeams,
                        tokenSeparators: [",", " "]
                    });
                }

                AJS.$("#plugin-permission").auiSelect2({
                    placeholder: "Search for users and groups",
                    minimumInputLength: 0,
                    tags: true,
                    tokenSeparators: [",", " "],
                    ajax: {
                        url: baseUrl + "/rest/api/2/groupuserpicker",
                        dataType: "json",
                        data: function (term, page) {
                            return {query: term};
                        },
                        results: function (data, page) {
                            var select2data = [];
                            for (var i = 0; i < data.groups.groups.length; i++) {
                                select2data.push({id: "groups-" + data.groups.groups[i].name, text: data.groups.groups[i].name});
                            }
                            for (var i = 0; i < data.users.users.length; i++) {
                                select2data.push({id: "users-" + data.users.users[i].name, text: data.users.users[i].name});
                            }
                            return {results: select2data};
                        }
                    },
                    initSelection: function (elements, callback) {
                        var data = [];
                        var array = elements.val().split(",");
                        for (var i = 0; i < array.length; i++) {
                            data.push({id: array[i], text: array[i].replace(/^users-/i, "").replace(/^groups-/i, "")});
                        }
                        callback(data);
                    }
                });

                AJS.$(".jira-group").auiSelect2({
                    placeholder: "Search for groups",
                    minimumInputLength: 0,
                    tags: true,
                    tokenSeparators: [",", " "],
                    ajax: {
                        url: baseUrl + "/rest/api/2/groups/picker",
                        dataType: "json",
                        data: function (term, page) {
                            return {query: term};
                        },
                        results: function (data, page) {
                            var select2data = [];
                            for (var i = 0; i < data.groups.length; i++) {
                                select2data.push({id: data.groups[i].name, text: data.groups[i].name});
                            }
                            return {results: select2data};
                        }
                    },
                    initSelection: function (elements, callback) {
                        var data = [];
                        var array = elements.val().split(",");
                        for (var i = 0; i < array.length; i++) {
                            data.push({id: array[i], text: array[i].replace(/^users-/i, "").replace(/^groups-/i, "")});
                        }
                        callback(data);
                    }
                });

                var approved = [];
                if (config.approvedGroups) {
                    for (var i = 0; i < config.approvedGroups.length; i++) {
                        approved.push({id: "groups-" + config.approvedGroups[i], text: config.approvedGroups[i]});
                    }
                }

                if (config.approvedUsers) {
                    for (var i = 0; i < config.approvedUsers.length; i++) {
                        approved.push({id: "users-" + config.approvedUsers[i], text: config.approvedUsers[i]});
                    }
                }

                AJS.$("#plugin-permission").auiSelect2("data", approved);
            },
            error: function (error) {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!"
                });
            }
        });
    }

    function updateConfig() {
        if ((!AJS.$("#github_token").val() && !AJS.$("#github_token").attr("placeholder")) || !AJS.$("#github_organization").val()
            || !AJS.$("#github_token_public").val()) {
            AJS.messages.error({
                title: "Error!",
                body: "API Tokens and Organisation must be filled out"
            });
            return;
        }

        var config = {};
        config.githubToken = AJS.$("#github_token").val();
        config.githubTokenPublic = AJS.$("#github_token_public").val();
        config.githubOrganization = AJS.$("#github_organization").val();

        var usersAndGroups = AJS.$("#plugin-permission").auiSelect2("val");
        var approvedUsers = [];
        var approvedGroups = [];
        for (var i = 0; i < usersAndGroups.length; i++) {
            if (usersAndGroups[i].match("^users-")) {
                approvedUsers.push(usersAndGroups[i].split("users-")[1]);
            } else if (usersAndGroups[i].match("^groups-")) {
                approvedGroups.push(usersAndGroups[i].split("groups-")[1]);
            }
        }

        config.approvedUsers = approvedUsers;
        config.approvedGroups = approvedGroups;
        config.teams = [];
        for (var i = 0; i < teams.length; i++) {
            var tempTeam = {};
            tempTeam.name = teams[i];
            tempTeam.githubTeams = AJS.$("#" + tempTeam.name + "-github-teams").auiSelect2("val");

            tempTeam.coordinatorGroups = AJS.$("#" + tempTeam.name + "-coordinator").auiSelect2("val");
            for(var i = 0; i < tempTeam.coordinatorGroups.length; i++) {
                tempTeam.coordinatorGroups[i] = tempTeam.coordinatorGroups[i].replace(/^groups-/i, "");
            }

            tempTeam.seniorGroups = AJS.$("#" + tempTeam.name + "-senior").auiSelect2("val");
            for(var i = 0; i < tempTeam.seniorGroups.length; i++) {
                tempTeam.seniorGroups[i] = tempTeam.seniorGroups[i].replace(/^groups-/i, "");
            }

            tempTeam.developerGroups = AJS.$("#" + tempTeam.name + "-developer").auiSelect2("val");
            for(var i = 0; i < tempTeam.developerGroups.length; i++) {
                tempTeam.developerGroups[i] = tempTeam.developerGroups[i].replace(/^groups-/i, "");
            }

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
            error: function (error) {
                AJS.messages.error({
                    title: "Error!",
                    body: error.responseText
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
    });
});