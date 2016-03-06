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

var restBaseUrl;

var tableSkeleton = "<h3>Teams</h3>" +
    "<table class=\"aui\">\n" +
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
    "</table>" +
    "<h3>Individual Resources</h3>" +
    "<fieldset id=\"individual-resources\" class=\"group\"></fieldset>";

AJS.toInit(function () {
    //AJS.$(document).ajaxStart(function () {
    //    AJS.$(".loadingDiv").show();
    //});
    //AJS.$(document).ajaxStop(function () {
    //    AJS.$(".loadingDiv").hide();
    //});

    var baseUrl = AJS.$("meta[id$='-base-url']").attr("content");
    restBaseUrl = baseUrl + "/rest/timesheet/latest/";

    var config;
    getConfigAndCallback(baseUrl, function (ajaxConfig) {
        config = ajaxConfig;
    });

    function populateTable(allUsers, allStates) {
        var users = allUsers[0];
        var timesheetState = allStates[0];
        AJS.$(".loadingDiv").show();
        AJS.$("#user-body").empty();
        for (var i = 0; i < users.length; i++) {
            var obj = users[i];
            var username = obj['active'] ? obj['userName'] : "<del>" + obj['userName'] + "</del>";
            var state = obj['active'] ? "active" : "inactive";
            var tsState = timesheetState[i]['active'] ? "active" : "inactive";

            AJS.$("#user-body").append("<tr><td headers=\"basic-username\" class=\"username\">" + username + "</td>" +
                "<td headers=\"basic-email\" class=\"email\">" + obj['email'] + "</td>" +
                "<td headers=\"basic-state\" class=\"status\">" + state + "</td>" +
                "<td headers=\"basic-timesheet-state\" class=\"timesheet\">" + tsState + "</td>" +
                "<td headers=\"basic-timesheet-disable\" class=\"timsheet\"><input class=\"checkbox\" type=\"checkbox\" name=\"checkBoxOne\" id=\"checkBoxOne\"></td></tr>");
                //last entry date hier noch einfügen
        }

        //enable checkbox
        //AJS.$("#checkBoxOne")[0].checked = true

        AJS.$("#user-table").trigger("update");
        var userList = new List("modify-user", {
            page: Number.MAX_VALUE,
            valueNames: ["username", "email", "status", "timesheet"]
        });

        userList.on('updated', function () {
            AJS.$("#user-table").trigger("update");
        });
        AJS.$(".loadingDiv").hide();
    }

    function fetchData() {

        var allUserFetched = AJS.$.ajax({
            type: 'GET',
            url: restBaseUrl + 'user/getUsers',
            contentType: "application/json"
        });

        var allStatesFetched = AJS.$.ajax({
            type: 'GET',
            url: restBaseUrl + 'timesheets/getStates',
            contentType: "application/json"
        });

        AJS.$.when(allUserFetched, allStatesFetched)
            .done(populateTable)
            .fail(function (error) {
                AJS.messages.error({
                    title: 'There was an error while fetching the required data.',
                    body: '<p>Reason: ' + error.responseText + '</p>'
                });
                console.log(error);
            });
    }

    fetchData();

});
