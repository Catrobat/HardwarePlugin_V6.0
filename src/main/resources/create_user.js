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

    AJS.$('#firstname').change(function () {
        updateUsername();
    });
    AJS.$('#lastname').change(function () {
        updateUsername();
    });

    function updateUsername() {
        var first = AJS.$('#firstname').val();
        first = replaceUmlauts(first);

        var last = AJS.$('#lastname').val();
        last = replaceUmlauts(last);

        AJS.$('#username').val(first + last);
    }

    function replaceUmlauts(str) {
        str = str.replace(/\u00e4/g, "ae").replace(/\u00f6/g, "oe")
            .replace(/\u00fc/g, "ue").replace(/\u00c4/g, "Ae").replace(/\u00d6/g, "Oe")
            .replace(/\u00dc/g, "Ue").replace(/\u00df/g, "ss");

        return str;
    }

    function resetForm() {
        getTeamList(baseUrl, resetFormAjax);
    }

    function resetFormAjax(teamList) {
        AJS.$("#username").attr("value", "");
        AJS.$("#firstname").attr("value", "");
        AJS.$("#lastname").attr("value", "");
        AJS.$("#email").attr("value", "");
        AJS.$("#github").attr("value", "");

        for (var i in teamList) {
            AJS.$("#" + teamList[i] + "-none").prop("checked", true);
        }
    }

    function createUser(teamList) {
        var userToCreate = new Object();
        userToCreate.userName = AJS.$("#username").attr("value");
        userToCreate.firstName = AJS.$("#firstname").attr("value");
        userToCreate.lastName = AJS.$("#lastname").attr("value");
        userToCreate.email = AJS.$("#email").attr("value");
        userToCreate.githubName = AJS.$("#github").attr("value");
        userToCreate.coordinatorList = [];
        userToCreate.seniorList = [];
        userToCreate.developerList = [];

        for (var i in teamList) {
            var value = AJS.$("input[name='" + teamList[i] + "']:checked").val();
            if (value == "coordinator") {
                userToCreate.coordinatorList.push(teamList[i]);
            } else if (value == "senior") {
                userToCreate.seniorList.push(teamList[i]);
            } else if (value == "developer") {
                userToCreate.developerList.push(teamList[i]);
            }
        }

        AJS.$.ajax({
            url: baseUrl + "/rest/admin-helper/1.0/user/createUser",
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify(userToCreate),
            success: function () {
                AJS.messages.success({
                    title: "Success!",
                    body: "User created!"
                });
                resetForm();
            },
            error: function (e) {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!<br />" + e.responseText
                });
            }
        });
    }

    populateTeamTable(baseUrl, "#team-body");

    AJS.$('#github').change(function () {
        var user_input = AJS.$(this).val();
        jQuery.ajax({
            url: baseUrl + "/rest/admin-helper/1.0/github/searchUser",
            type: "PUT",
            contentType: "application/json",
            data: user_input,
            success: function (response) {
                if (response == "success") {
                    AJS.$('#github-error').hide();
                } else {
                    error: AJS.$('#github-error').show();
                }
            }
        });
    });

    AJS.$("#create").submit(function (e) {
        e.preventDefault();
        getTeamList(baseUrl, createUser);
    });
    AJS.$(".cancel").click(function (e) {
        e.preventDefault();
        resetForm();
    });
});