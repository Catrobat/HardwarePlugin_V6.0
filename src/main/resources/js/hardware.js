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

var lendingOutDialog;
var createHardwareDialog;
var returnDialog;
var removeHardwareDialog;
var sortOutDialog;

var urlRest = "/rest/admin-helper/latest/hardware";

var urlSuffixHardwareModels = urlRest;
var urlSuffixSingleHardwareModel = urlRest + "/{0}";
var urlSuffixSingleHardwareModelDevices = urlRest + "/{0}/devices";
var urlSuffixSingleHardwareModelDevicesAvailable = urlRest + "/{0}/devices/available";

var urlSuffixDevices = urlRest + "/devices";
var urlSuffixDevicesOngoingLending = urlRest + "/devices/ongoing-lending";
var urlSuffixDevicesSortedOut = urlRest + "/devices/sorted-out";
var urlSuffixSingleDevice = urlRest + "/devices/{0}";
var urlSuffixSingleDeviceLendOut = urlRest + "/devices/{0}/lend-out";
var urlSuffixSingleDeviceCurrentLending = urlRest + "/devices/{0}/current-lending";
var urlSuffixSingleDeviceSortOut = urlRest + "/devices/{0}/sort-out";

var urlSuffixTypes = urlRest + "/types";
var urlSuffixProducers = urlRest + "/producers";
var urlSuffixOperatingSystems = urlRest + "/operating-systems";

AJS.toInit(function () {
    var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");
    fillOutAllTables(baseUrl);
    handleEvents(baseUrl);
});

function fillOutAllTables(baseUrl) {
    AJS.$.ajax({
        url: baseUrl + urlSuffixHardwareModels,
        type: "GET",
        success: function (hardwareList) {
            populateOverviewTable(hardwareList);
            populateHardwareManagementTable(hardwareList);
        },
        error: function (error) {
            AJS.messages.error({
                title: "Error!",
                body: error.responseText
            });
        }
    });

    AJS.$.ajax({
        url: baseUrl + urlSuffixDevicesOngoingLending,
        type: "GET",
        success: function (deviceList) {
            populateLentOutTable(deviceList);
        },
        error: function (error) {
            AJS.messages.error({
                title: "Error!",
                body: error.responseText
            });
        }
    });

    AJS.$.ajax({
        url: baseUrl + urlSuffixDevicesSortedOut,
        type: "GET",
        success: function (deviceList) {
            populateSortedOutTable(deviceList);
        },
        error: function (error) {
            AJS.messages.error({
                title: "Error!",
                body: error.responseText
            });
        }
    });

    AJS.$.ajax({
        url: baseUrl + urlSuffixDevices,
        type: "GET",
        success: function (deviceList) {
            populateAllDevicesTable(deviceList);
        },
        error: function (error) {
            AJS.messages.error({
                title: "Error!",
                body: error.responseText
            });
        }
    });
}

function handleEvents(baseUrl) {
    AJS.$(document).on("click", ".lending_out", function (e) {
        e.preventDefault();
        showLendoutDialog(baseUrl, e.target.id);
    });

    AJS.$(document).on("click", ".device_sort_out", function (e) {
        e.preventDefault();
        showSortoutDialog(baseUrl, e.target.id);
    });

    AJS.$(document).on("click", ".device_return", function (e) {
        e.preventDefault();
        showReturnDialog(baseUrl, e.target.id);
    });

    AJS.$(document).on("click", ".device_details", function (e) {
        e.preventDefault();
        showDeviceDetailDialog(baseUrl, e.target.id);
    });

    AJS.$(document).on("click", ".edit_model", function (e) {
        e.preventDefault();
        showNewHardwareDialog(baseUrl, e.target.id);
    });

    AJS.$(document).on("click", "#new_model", function (e) {
        e.preventDefault();
        showNewHardwareDialog(baseUrl);
    });

    AJS.$(document).on("click", ".remove_model", function (e) {
        e.preventDefault();
        showRemoveHardwareDialog(baseUrl, e.target.id);
    });

    AJS.$("#new_device").click(function (e) {
        e.preventDefault();
        showNewDeviceDialog(baseUrl);
    });
}

function getShortDate(dateString) {
    if (typeof dateString === "string") {
        return dateString.split("T")[0];
    }

    return "";
}

function formatDateForForm(dateString) {
    var date = new Date(dateString);
    var day = ("0" + date.getDate()).slice(-2);
    var month = ("0" + (date.getMonth() + 1)).slice(-2);
    return date.getFullYear() + "-" + (month) + "-" + (day);
}
