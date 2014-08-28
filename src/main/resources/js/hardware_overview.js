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

function populateOverviewTable(hardwareList) {
    var tableBody = "";

    for (var i = 0; i < hardwareList.length; i++) {
        tableBody += "<tr>\n" +
            "<td>" + hardwareList[i].name + "</td>\n" +
            "<td>" + hardwareList[i].version + "</td>\n" +
            "<td>" + hardwareList[i].typeOfDevice + "</td>\n" +
            "<td>" + hardwareList[i].operatingSystem + "</td>\n" +
            "<td>" + hardwareList[i].available + "/" + hardwareList[i].sumOfDevices + "</td>\n" +
            "<td><a class=\"lending_out\" id=\"" + hardwareList[i].ID + "\" href=\"#\">Lending out</a></td>\n" +
            "</tr>";
    }

    AJS.$("#table-overview").html(tableBody);
    AJS.$("#table-overview").trigger("update");
}

function populateLentOutTable(hardwareList) {
    var tableBody = "";

    for (var i = 0; i < hardwareList.length; i++) {
        var currentlyLentOutSince = new Date(hardwareList[i].currentlyLentOutSince);
        tableBody += "<tr>\n" +
            "<td>" + hardwareList[i].hardwareModelName + "</td>\n" +
            "<td>" + hardwareList[i].serialNumber + "</td>\n" +
            "<td>" + hardwareList[i].imei + "</td>\n" +
            "<td>" + hardwareList[i].inventoryNumber + "</td>\n" +
            "<td>" + hardwareList[i].currentlyLentOutFrom + "</td>\n" +
            "<td>" + currentlyLentOutSince.toISOString().split("T")[0] + "</td>\n" +
            "<td><a class=\"device_details\" id=\"" + hardwareList[i].ID + "\" href=\"#\">Details</a></td>\n" +
            "<td><a class=\"device_return\" id=\"" + hardwareList[i].ID + "\" href=\"#\">Return</a></td>\n" +
            "</tr>";
    }

    AJS.$("#table-lent-out").html(tableBody);
    AJS.$("#table-lent-out").trigger("update");
}

function populateSortedOutTable(deviceList) {
    var tableBody = "";

    for (var i = 0; i < deviceList.length; i++) {
        var sortedOut = new Date(deviceList[i].sortedOutDate);
        tableBody += "<tr>\n" +
            "<td>" + deviceList[i].hardwareModelName + "</td>\n" +
            "<td>" + deviceList[i].serialNumber + "</td>\n" +
            "<td>" + deviceList[i].imei + "</td>\n" +
            "<td>" + deviceList[i].inventoryNumber + "</td>\n" +
            "<td>" + sortedOut.toISOString().split("T")[0] + "</td>\n" +
            "<td><a class=\"device_details\" id=\"" + deviceList[i].ID + "\" href=\"#\">Details</a></td>\n" +
            "</tr>";
    }

    AJS.$("#table-sorted-out").html(tableBody);
    AJS.$("#table-sorted-out").trigger("update");
}

function populateHardwareManagementTable(hardwareList) {
    var tableBody = "";

    for (var i = 0; i < hardwareList.length; i++) {
        tableBody += "<tr>\n" +
            "<td>" + hardwareList[i].name + "</td>\n" +
            "<td>" + hardwareList[i].typeOfDevice + "</td>\n" +
            "<td>" + hardwareList[i].operatingSystem + "</td>\n" +
            "<td>" + hardwareList[i].sumOfDevices + "</td>\n" +
            "<td><a class=\"edit_model\" id=\"" + hardwareList[i].ID + "\" href=\"#\">Edit</a></td>\n" +
            "<td><a class=\"remove_model\" id=\"" + hardwareList[i].ID + "\" href=\"#\">Remove</a></td>\n" +
            "</tr>";
    }

    AJS.$("#table-management").html(tableBody);
    AJS.$("#table-management").trigger("update");
}

function showLendoutDialog(baseUrl, hardwareId) {
    alert("ID: " + hardwareId);
    AJS.$.ajax({
        url: baseUrl + "/rest/admin-helper/1.0/hardware/hardwares/" + hardwareId + "/devices/available",
        type: "GET",
        success: function (deviceList) {
            showLendOutDialogAjax(hardwareId, deviceList)
        },
        error: function (error) {
            // TODO real error message
            alert("Error: " + error.responseText);
        }
    });
}

function showLendOutDialogAjax(hardwareId, deviceList) {
    var dialog = new AJS.Dialog({
        width: 840,
        height: 600,
        id: "lend-out-dialog",
        closeOnOutsideClick: true
    });

    var selectedDeviceId = 0;

    var contentDevices = "<table class=\"aui aui-table-interactive aui-table-sortable\">\n" +
        "<thead>\n" +
        "<tr>\n" +
        "<th>Serialnumber</th>\n" +
        "<th>IMEI</th>\n" +
        "<th>Inventorynumber</th>\n" +
        "<th class=\"aui-table-column-unsortable\">Choose</th>\n" +
        "</tr>\n" +
        "</thead>\n" +
        "<tbody id=\"table-lent-out\">\n";
    deviceList.map = [];
    for (var i = 0; i < deviceList.length; i++) {
        contentDevices += "<tr>\n" +
            "<td class=\"serial\">" + deviceList[i].serialNumber + "</td>\n" +
            "<td class=\"imei\">" + deviceList[i].imei + "</td>\n" +
            "<td class=\"inventory\">" + deviceList[i].inventoryNumber + "</td>\n" +
            "<td><a class=\"choose\" id=\"" + deviceList[i].ID + "\" href=\"#\">Choose...</a></td>\n" +
            "</tr>\n";

        deviceList.map[deviceList[i].ID] = i;
    }
    contentDevices += "</tbody>\n" +
        "</table>";

    var contentDetails = "<form action=\"#\" method=\"post\" id=\"d\" class=\"aui\">\n" +
        "    <fieldset>\n" +
        "        <div class=\"field-group\">\n" +
        "            <label for=\"serial\">Serialnumber</label>\n" +
        "            <input class=\"text\" type=\"text\" id=\"serial\" name=\"serial\" title=\"serial\" disabled>\n" +
        "        </div>\n" +
        "        <div class=\"field-group\">\n" +
        "            <label for=\"imei\">IMEI</label>\n" +
        "            <input class=\"text\" type=\"text\" id=\"imei\" name=\"imei\" title=\"imei\" disabled>\n" +
        "        </div>\n" +
        "        <div class=\"field-group\">\n" +
        "            <label for=\"inventory\">Inventorynumber</label>\n" +
        "            <input class=\"text\" type=\"text\" id=\"inventory\" name=\"inventory\" title=\"inventory\" disabled>\n" +
        "        </div>\n" +
        "        <div class=\"field-group\">\n" +
        "            <label for=\"d-fname\">User<span class=\"aui-icon icon-required\"> required</span></label>\n" +
        "            <input class=\"text\" type=\"text\" id=\"d-fname\" name=\"d-fname\" title=\"first name\">\n" +
        "        </div>\n" +
        "        <div class=\"field-group\">\n" +
        "            <label for=\"comment\">Device Comment</label>\n" +
        "            <textarea class=\"textarea\" name=\"comment\" id=\"comment\" placeholder=\"Your comment here...\"></textarea>\n" +
        "        </div>\n" +
        "        <div class=\"field-group\">\n" +
        "            <label for=\"d-fname\">Begin Date</label>\n" +
        "            <input class=\"text\" type=\"text\" id=\"d-fname\" name=\"d-fname\" title=\"first name\" placeholder=\"2014-08-18\">\n" +
        "        </div>\n" +
        "        <div class=\"field-group\">\n" +
        "            <label for=\"d-lname\">Lending Purpose</label>\n" +
        "            <input class=\"text\" type=\"text\" id=\"d-lname\" name=\"d-lname\" title=\"last name\">\n" +
        "        </div>\n" +
        "        <div class=\"field-group\">\n" +
        "            <label for=\"comment\">Lending Comment</label>\n" +
        "            <textarea class=\"textarea\" name=\"comment\" id=\"comment\" placeholder=\"Your comment here...\"></textarea>\n" +
        "        </div>\n" +
        "    </fieldset>\n" +
        "</form>";

    dialog.addHeader("Lending Out");
    dialog.addPanel("Choose device", contentDevices, "panel-body-device");

    dialog.addPanel("Details", contentDetails, "panel-body-details");

    dialog.addButton("Save", function (dialog) {
        dialog.remove();
    });
    dialog.addLink("Cancel", function (dialog) {
        dialog.remove();
    }, "#");

    AJS.$(document).on("click", ".choose", function (e) {
        e.preventDefault();

        dialog.gotoPanel(1);

        AJS.$("#lend-out-dialog").find("a#" + selectedDeviceId).closest("tr").css("background-color", "");
        selectedDeviceId = e.target.id;
        AJS.$("#lend-out-dialog").find("a#" + selectedDeviceId).closest("tr").css("background-color", "#e0e0e0");

        AJS.$("#serial").val(deviceList[deviceList.map[selectedDeviceId]].serialNumber);
        AJS.$("#imei").val(deviceList[deviceList.map[selectedDeviceId]].imei);
        AJS.$("#inventory").val(deviceList[deviceList.map[selectedDeviceId]].inventoryNumber);
    });

    dialog.gotoPage(0);
    dialog.gotoPanel(0);
    dialog.show();
}

function createReturnDialog() {
    var dialog = new AJS.Dialog({
        width: 600,
        height: 400,
        id: "example-dialog",
        closeOnOutsideClick: true
    });

    var content = "<form action=\"#\" method=\"post\" id=\"d\" class=\"aui\">\n" +
        "    <fieldset>\n" +
        "        <div class=\"field-group\">\n" +
        "            <label for=\"comment\">Device Comment</label>\n" +
        "            <textarea class=\"textarea\" name=\"comment\" id=\"comment\" placeholder=\"Your comment here...\"></textarea>\n" +
        "        </div>\n" +
        "        <div class=\"field-group\">\n" +
        "            <label for=\"d-fname\">End Date</label>\n" +
        "            <input class=\"text\" type=\"text\" id=\"d-fname\" name=\"d-fname\" title=\"first name\" placeholder=\"2014-08-18\">\n" +
        "        </div>\n" +
        "        <div class=\"field-group\">\n" +
        "            <label for=\"d-lname\">Lending Purpose</label>\n" +
        "            <input class=\"text\" type=\"text\" id=\"d-lname\" name=\"d-lname\" title=\"last name\">\n" +
        "        </div>\n" +
        "        <div class=\"field-group\">\n" +
        "            <label for=\"comment\">Lending Comment</label>\n" +
        "            <textarea class=\"textarea\" name=\"comment\" id=\"comment\" placeholder=\"Your comment here...\">Maybe some comment might be in here from the lending out interface</textarea>\n" +
        "        </div>\n" +
        "    </fieldset>\n" +
        " </form> ";

    dialog.addHeader("Returning Device");
    dialog.addPanel("Panel 1", content, "panel-body");

    dialog.addButton("Save", function (dialog) {
        dialog.hide();
    });
    dialog.addLink("Cancel", function (dialog) {
        dialog.hide();
    }, "#");

    return dialog;
}

function createNewHardwareDialog() {
    var dialog = new AJS.Dialog({
        width: 600,
        height: 600,
        id: "example-dialog",
        closeOnOutsideClick: true
    });

    var content = "<form action=\"#\" method=\"post\" id=\"d\" class=\"aui\">\n" +
        "        <fieldset>\n" +
        "            <div class=\"field-group\">\n" +
        "                <label for=\"d-fname\">Name<span class=\"aui-icon icon-required\"> required</span></label>\n" +
        "                <input class=\"text\" type=\"text\" id=\"d-fname\" name=\"d-fname\" title=\"first name\">\n" +
        "\n" +
        "                <div class=\"description\">Name or short term for describing the hardware type</div>\n" +
        "            </div>\n" +
        "\n" +
        "            <div class=\"field-group\">\n" +
        "                <label for=\"d-fname\">Type of device</label>\n" +
        "                <input class=\"text\" type=\"text\" id=\"d-fname\" name=\"d-fname\" title=\"first name\">\n" +
        "\n" +
        "                <div class=\"description\">Type of device class for better grouping</div>\n" +
        "            </div>\n" +
        "\n" +
        "            <div class=\"field-group\">\n" +
        "                <label for=\"d-fname\">Version</label>\n" +
        "                <input class=\"text\" type=\"text\" id=\"d-fname\" name=\"d-fname\" title=\"first name\">\n" +
        "\n" +
        "                <div class=\"description\">e.g. 8 GB/16 GB Version</div>\n" +
        "            </div>\n" +
        "\n" +
        "            <div class=\"field-group\">\n" +
        "                <label for=\"d-fname\">Price</label>\n" +
        "                <input class=\"text\" type=\"text\" id=\"d-fname\" name=\"d-fname\" title=\"first name\">\n" +
        "            </div>\n" +
        "\n" +
        "            <div class=\"field-group\">\n" +
        "                <label for=\"d-fname\">Producer</label>\n" +
        "                <input class=\"text\" type=\"text\" id=\"d-fname\" name=\"d-fname\" title=\"first name\">\n" +
        "\n" +
        "                <div class=\"description\">Main Producer or brand</div>\n" +
        "            </div>\n" +
        "\n" +
        "            <div class=\"field-group\">\n" +
        "                <label for=\"d-fname\">Operating System</label>\n" +
        "                <input class=\"text\" type=\"text\" id=\"d-fname\" name=\"d-fname\" title=\"first name\">\n" +
        "\n" +
        "                <div class=\"description\">Android/iOS/Windows Phone</div>\n" +
        "            </div>\n" +
        "\n" +
        "            <div class=\"field-group\">\n" +
        "                <label for=\"d-fname\">Item number</label>\n" +
        "                <input class=\"text\" type=\"text\" id=\"d-fname\" name=\"d-fname\" title=\"first name\">\n" +
        "\n" +
        "                <div class=\"description\">Unique item number for product (e.g. GTIN)</div>\n" +
        "            </div>\n" +
        "        </fieldset>\n" +
        "    </form>";

    dialog.addHeader("Create/Edit Hardware Model");
    dialog.addPanel("Panel 1", content, "panel-body");

    dialog.addButton("Save", function (dialog) {
        dialog.hide();
    });
    dialog.addLink("Cancel", function (dialog) {
        dialog.hide();
    }, "#");

    return dialog;
}

function showNewDeviceDialog(baseUrl) {
    AJS.$.ajax({
        url: baseUrl + "/rest/admin-helper/1.0/hardware/hardwares/",
        type: "GET",
        success: function (deviceList) {
            showNewDeviceDialogAjax(baseUrl, deviceList)
        },
        error: function (error) {
            AJS.messages.error({
                title: "Error!",
                body: error.responseText
            });
        }
    });
}

function showNewDeviceDialogAjax(baseUrl, hardwareList) {
    var dialog = new AJS.Dialog({
        width: 600,
        height: 500,
        id: "example-dialog",
        closeOnOutsideClick: true
    });

    var content = "<div id=\"dialog_error\"></div><form action=\"#\" method=\"post\" id=\"d\" class=\"aui\">\n" +
        "<fieldset>\n" +
        "<div class=\"field-group\">\n" +
        "<label for=\"hardware_selection\">Hardware Model<span class=\"aui-icon icon-required\"> required</span></label>\n" +
        "<select class=\"select\" id=\"hardware_selection\" name=\"hardware\" title=\"hardware\">\n" +
        "<option>Select</option>\n";
    for (var i = 0; i < hardwareList.length; i++) {
        content += "<option value=\"" + hardwareList[i].ID + "\">" + hardwareList[i].name;
        if (hardwareList[i].version) {
            content += " (" + hardwareList[i].version + ")";
        }
        content += "</option>\n";
    }
    content += "</select>\n" +
        "<div class=\"error\" id=\"select_hardware_error\">Select a hardware model</div>\n" +
        "</div>\n" +
        "\n" +
        "<div class=\"field-group\">\n" +
        "<label for=\"serial\">Serial number</label>\n" +
        "<input class=\"text device_id\" type=\"text\" id=\"serial\" name=\"serial\" title=\"serial number\">\n" +
        "</div>\n" +
        "\n" +
        "<div class=\"field-group\">\n" +
        "<label for=\"imei\">IMEI</label>\n" +
        "<input class=\"text device_id\" type=\"text\" id=\"imei\" name=\"imei\" title=\"imei\">\n" +
        "</div>\n" +
        "\n" +
        "<div class=\"field-group\">\n" +
        "<label for=\"inventory\">Inventory number</label>\n" +
        "<input class=\"text device_id\" type=\"text\" id=\"inventory\" name=\"inventory\" title=\"inventory number\">\n" +
        "<div class=\"error\" id=\"unique_id\">At least one unique identifier must be filled out (Serial/IMEI/Inventory)</div>\n" +
        "</div>\n" +
        "\n" +
        "<div class=\"field-group\">\n" +
        "<label for=\"received_date\">Received date</label>\n" +
        "<input class=\"text\" type=\"date\" id=\"received_date\" name=\"received_date\"/>\n" +
        "</div>\n" +
        "\n" +
        "<div class=\"field-group\">\n" +
        "<label for=\"received_from\">Received from</label>\n" +
        "<input class=\"text\" type=\"text\" id=\"received_from\" name=\"received_from\" title=\"received from\">\n" +
        "<div class=\"description\">Main Producer or brand</div>\n" +
        "</div>\n" +
        "\n" +
        "<div class=\"field-group\">\n" +
        "<label for=\"life_of_asset\">Useful life of asset</label>\n" +
        "<input class=\"text\" type=\"text\" id=\"life_of_asset\" name=\"life_of_asset\" title=\"life of asset\">\n" +
        "<div class=\"description\">Amount of time when this device is obsolete</div>\n" +
        "</div>\n" +
        "</fieldset>\n" +
        "</form>";

    dialog.addHeader("Create New Device");
    dialog.addPanel("Panel 1", content, "panel-body");

    dialog.addButton("Save", function (dialog) {
        if ((AJS.$("#serial").val() || AJS.$("#imei").val() || AJS.$("#inventory").val()) && AJS.$("#hardware_selection").val()) {
            var device = {
                serialNumber: AJS.$("#serial").val(),
                imei: AJS.$("#imei").val(),
                inventoryNumber: AJS.$("#inventory").val(),
                receivedDate: new Date(AJS.$("#received_date").val()),
                receivedFrom: AJS.$("#received_from").val(),
                usefulLiveOfAsset: AJS.$("#life_of_asset").val()
            }
            alert("device: " + JSON.stringify(device));
            AJS.$.ajax({
                url: baseUrl + "/rest/admin-helper/1.0/hardware/hardwares/" + AJS.$("#hardware_selection").val() + "/devices",
                type: "PUT",
                contentType: "application/json",
                data: JSON.stringify(device),
                dateType: "json",
                success: function () {
                    AJS.messages.success({
                        title: "Success!",
                        body: "Device added successfully"
                    });
                    fillOutAllTables(baseUrl);
                },
                error: function (error) {
                    AJS.messages.error({
                        title: "Error!",
                        body: error.responseText
                    });
                }
            });
            dialog.remove();
        } else {
            AJS.messages.error("#dialog_error", {
                title: "Error!",
                body: "Please fill out above fields"
            });
        }
    }, "dialog_submit_button");
    dialog.addLink("Cancel", function (dialog) {
        dialog.remove();
    }, "#");

    dialog.gotoPage(0);
    dialog.gotoPanel(0);
    dialog.show();

    AJS.$(".device_id").change(function () {
        if (AJS.$("#serial").val() || AJS.$("#imei").val() || AJS.$("#inventory").val()) {
            AJS.$("#unique_id").hide();
        } else {
            AJS.$("#unique_id").show();
        }
    });

    AJS.$("#hardware_selection").change(function () {
        if (AJS.$(this).val()) {
            AJS.$("#select_hardware_error").hide();
        } else {
            AJS.$("#select_hardware_error").show();
        }
    });
}

function createRemoveModelDialog() {
    var dialog = new AJS.Dialog({
        width: 600,
        height: 250,
        id: "example-dialog",
        closeOnOutsideClick: true
    });

    var content = "<form class=\"aui\">\n" +
        "        <p>Are you sure to delete this Hardware Model?</p>\n" +
        "\n" +
        "        <p>All allocated devices need to be moved to another Hardware Model.\n" +
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
        "        </fieldset></p>\n" +
        "    </form>";

    dialog.addHeader("Remove Hardware Model");
    dialog.addPanel("Panel 1", content, "panel-body");

    dialog.addButton("Remove", function (dialog) {
        dialog.hide();
    });
    dialog.addLink("Cancel", function (dialog) {
        dialog.hide();
    }, "#");

    return dialog;
}


function createDeviceDetailDialog() {
    var dialog = new AJS.Dialog({
        width: 600,
        height: 450,
        id: "example-dialog",
        closeOnOutsideClick: true
    });

    var modelContent = "<table class=\"aui\">\n" +
        "    <tbody>\n" +
        "        <tr>\n" +
        "            <td>Name</td>\n" +
        "            <td>Nexus 7</td>\n" +
        "        </tr>\n" +
        "        <tr>\n" +
        "            <td>Type</td>\n" +
        "            <td>Tablet</td>\n" +
        "        </tr>\n" +
        "        <tr>\n" +
        "            <td>Version</td>\n" +
        "            <td>16 GB</td>\n" +
        "        </tr>\n" +
        "        <tr>\n" +
        "            <td>Price</td>\n" +
        "            <td>200 USD</td>\n" +
        "        </tr>\n" +
        "        <tr>\n" +
        "            <td>Producer</td>\n" +
        "            <td>Asus/Google</td>\n" +
        "        </tr>\n" +
        "        <tr>\n" +
        "            <td>OS</td>\n" +
        "            <td>Android</td>\n" +
        "        </tr>\n" +
        "        <tr>\n" +
        "            <td>Item Model Number</td>\n" +
        "            <td>NEXUS7 ASUS-2B16</td>\n" +
        "        </tr>\n" +
        "    </tbody>\n" +
        "</table>";

    var deviceContent = "<table class=\"aui\">\n" +
        "    <tbody>\n" +
        "        <tr>\n" +
        "            <td>Serial Number</td>\n" +
        "            <td>B7OKAS241194</td>\n" +
        "        </tr>\n" +
        "        <tr>\n" +
        "            <td>IMEI</td>\n" +
        "            <td>358240051927168</td>\n" +
        "        </tr>\n" +
        "        <tr>\n" +
        "            <td>Inventory Number</td>\n" +
        "            <td>113791</td>\n" +
        "        </tr>\n" +
        "        <tr>\n" +
        "            <td>Received Date</td>\n" +
        "            <td>2013-04-04</td>\n" +
        "        </tr>\n" +
        "        <tr>\n" +
        "            <td>Received By</td>\n" +
        "            <td>Google</td>\n" +
        "        </tr>\n" +
        "        <tr>\n" +
        "            <td>Useful life of asset</td>\n" +
        "            <td>3 years</td>\n" +
        "        </tr>\n" +
        "        <tr>\n" +
        "            <td>Sorted Out Date</td>\n" +
        "            <td>2014-04-04</td>\n" +
        "        </tr>\n" +
        "        <tr>\n" +
        "            <td>Sorted Out Comment</td>\n" +
        "            <td>Hat xy verwurschtelt</td>\n" +
        "        </tr>\n" +
        "    </tbody>\n" +
        "</table>";

    var commentContent = "<table class=\"aui\">\n" +
        "    <thead>\n" +
        "        <tr>\n" +
        "            <th id=\"basic-author\">Author</th>\n" +
        "            <th id=\"basic-date\">Date</th>\n" +
        "            <th id=\"basic-comment\">Comment</th>\n" +
        "        </tr>\n" +
        "    </thead>\n" +
        "    <tbody>\n" +
        "        <tr>\n" +
        "            <td headers=\"basic-author\">Felly</td>\n" +
        "            <td headers=\"basic-date\"><nobr>2014-08-18</nobr></td>\n" +
        "            <td headers=\"basic-comment\">Ich schreib mal ein längeres Kommentar zum schauen was er macht und ab wann er umbricht.</td>\n" +
        "        </tr>\n" +
        "        <tr>\n" +
        "            <td headers=\"basic-author\">Felly</td>\n" +
        "            <td headers=\"basic-date\"><nobr>2014-08-18</nobr></td>\n" +
        "            <td headers=\"basic-comment\">Blah blub</td>\n" +
        "        </tr>\n" +
        "    </tbody>\n" +
        "</table>";

    var historyContent = "<table class=\"aui\">\n" +
        "    <thead>\n" +
        "        <tr>\n" +
        "            <th id=\"basic-author\">User</th>\n" +
        "            <th id=\"basic-begin\">Begin</th>\n" +
        "            <th id=\"basic-end\">End</th>\n" +
        "            <th id=\"basic-purpose\">Purpose</th>\n" +
        "            <th id=\"basic-comment\">Comment</th>\n" +
        "        </tr>\n" +
        "    </thead>\n" +
        "    <tbody>\n" +
        "        <tr>\n" +
        "            <td headers=\"basic-author\">Felly</td>\n" +
        "            <td headers=\"basic-begin\"><nobr>2014-06-18</nobr></td>\n" +
        "            <td headers=\"basic-end\"><nobr>2014-07-17</nobr></td>\n" +
        "            <td headers=\"basic-purpose\">Catroid</td>\n" +
        "            <td headers=\"basic-comment\">Alles OK</td>\n" +
        "        </tr>\n" +
        "        <tr>\n" +
        "            <td headers=\"basic-author\">Felly</td>\n" +
        "            <td headers=\"basic-begin\"><nobr>2014-07-18</nobr></td>\n" +
        "            <td headers=\"basic-end\"><nobr>2014-08-18</nobr></td>\n" +
        "            <td headers=\"basic-purpose\">Catroid</td>\n" +
        "            <td headers=\"basic-comment\">Display zerkratzt</td>\n" +
        "        </tr>\n" +
        "    </tbody>\n" +
        "</table>";

    dialog.addHeader("Device Details");
    dialog.addPanel("Model", modelContent, "panel-body");
    dialog.addPanel("Device", deviceContent, "panel-body");
    dialog.addPanel("Device Comments", commentContent, "panel-body");
    dialog.addPanel("Lending History", historyContent, "panel-body");

    dialog.addButton("OK", function (dialog) {
        dialog.hide();
    });

    return dialog;
}

AJS.toInit(function () {
    var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");
    fillOutAllTables(baseUrl);
    handleEvents(baseUrl);
});

function fillOutAllTables(baseUrl) {
    AJS.$.ajax({
        url: baseUrl + "/rest/admin-helper/1.0/hardware/hardwares",
        type: "GET",
        success: function (hardwareList) {
            populateOverviewTable(hardwareList);
            populateHardwareManagementTable(hardwareList);
        },
        error: function (error) {
            // TODO real error message
            alert("Error: " + error.responseText);
        }
    });

    AJS.$.ajax({
        url: baseUrl + "/rest/admin-helper/1.0/hardware/devices/ongoing-lendings",
        type: "GET",
        success: function (deviceList) {
            populateLentOutTable(deviceList);
        },
        error: function (error) {
            // TODO real error message
            alert("Error: " + error.responseText);
        }
    });

    AJS.$.ajax({
        url: baseUrl + "/rest/admin-helper/1.0/hardware/devices/sorted-out",
        type: "GET",
        success: function (deviceList) {
            populateSortedOutTable(deviceList);
        },
        error: function (error) {
            // TODO real error message
            alert("Error: " + error.responseText);
        }
    });
}

function handleEvents(baseUrl) {
    AJS.$(document).on("click", ".lending_out", function (e) {
        e.preventDefault();
        showLendoutDialog(baseUrl, e.target.id);
    });

    AJS.$(document).on("click", ".device_return", function (e) {
        e.preventDefault();

        var dialog = createReturnDialog();
        dialog.gotoPage(0);
        dialog.gotoPanel(0);
        dialog.show();
    });

    AJS.$(document).on("click", ".device_details", function (e) {
        e.preventDefault();

        var dialog = createDeviceDetailDialog();
        dialog.gotoPage(0);
        dialog.gotoPanel(0);
        dialog.show();
    });

    AJS.$(document).on("click", ".edit_model, #new_model", function (e) {
        e.preventDefault();

        var dialog = createNewHardwareDialog();
        dialog.gotoPage(0);
        dialog.gotoPanel(0);
        dialog.show();
    });

    AJS.$(document).on("click", ".remove_model", function (e) {
        e.preventDefault();

        var dialog = createRemoveModelDialog();
        dialog.gotoPage(0);
        dialog.gotoPanel(0);
        dialog.show();
    });

    AJS.$("#new_device").click(function (e) {
        e.preventDefault();
        showNewDeviceDialog(baseUrl);
    });
}