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

function createLendoutDialog() {
    // Note this is a small dialog, so it fits in the Sandbox panel
// Standard sizes are 400, 600 and 840 pixels wide
    var dialog = new AJS.Dialog({
        width: 600,
        height: 400,
        id: "example-dialog",
        closeOnOutsideClick: true
    });

    var content = "<form action=\"#\" method=\"post\" id=\"d\" class=\"aui\">\n" +
        "    <fieldset>\n" +
        "        <legend><span>Dropdowns and multi select</span></legend>\n" +
        "        <div class=\"field-group\">\n" +
        "            <label for=\"dBase\">Device<span class=\"aui-icon icon-required\"> required</span></label>\n" +
        "            <select class=\"select\" id=\"dBase\" name=\"dBase\" title=\"database select\">\n" +
        "                <option>Select</option>\n" +
        "                <option>SSN 1</option>\n" +
        "                <option>SSN 2</option>\n" +
        "                <option>SSN 3</option>\n" +
        "                <option>SSN 4</option>\n" +
        "            </select>\n" +
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
    dialog.addPanel("Panel 1", content, "panel-body");
    dialog.get("panel:0").setPadding(0);

    dialog.addButton("Save", function (dialog) {
        dialog.hide();
    });
    dialog.addLink("Cancel", function (dialog) {
        dialog.hide();
    }, "#");

    return dialog;
}

function createReturnDialog() {
    // Note this is a small dialog, so it fits in the Sandbox panel
// Standard sizes are 400, 600 and 840 pixels wide
    var dialog = new AJS.Dialog({
        width: 600,
        height: 350,
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
    dialog.get("panel:0").setPadding(0);

    dialog.addButton("Save", function (dialog) {
        dialog.hide();
    });
    dialog.addLink("Cancel", function (dialog) {
        dialog.hide();
    }, "#");

    return dialog;
}

function createDeviceDetailDialog() {
    // Note this is a small dialog, so it fits in the Sandbox panel
// Standard sizes are 400, 600 and 840 pixels wide
    var dialog = new AJS.Dialog({
        width: 600,
        height: 400,
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
    dialog.addPanel("Comments", commentContent, "panel-body");
    dialog.addPanel("History", historyContent, "panel-body");

    dialog.addButton("OK", function (dialog) {
        dialog.hide();
    });

    return dialog;
}

AJS.toInit(function () {
    var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");

    AJS.$(".lending_out").click(function (e) {
        e.preventDefault();

        var dialog = createLendoutDialog();
        dialog.gotoPage(0);
        dialog.gotoPanel(0);
        dialog.show();
    });

    AJS.$(".device_return").click(function (e) {
        e.preventDefault();

        var dialog = createReturnDialog();
        dialog.gotoPage(0);
        dialog.gotoPanel(0);
        dialog.show();
    });

    AJS.$(".device_details").click(function (e) {
        e.preventDefault();

        var dialog = createDeviceDetailDialog() ;
        dialog.gotoPage(0);
        dialog.gotoPanel(0);
        dialog.show();
    });
});