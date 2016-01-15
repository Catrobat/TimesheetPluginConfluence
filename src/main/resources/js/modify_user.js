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

function getGithubForm(githubUsername, checkBoxSet) {
    return "<form id=\"d\" class=\"aui\">\n" +
        "<fieldset>\n" +
        "<div class=\"field-group\">\n" +
        "<label for=\"github-name\">GitHub Name<span class=\"aui-icon icon-required\"> required</span></label>\n" +
        "<input class=\"text\" type=\"text\" id=\"github-name\" name=\"github-name\" title=\"github-name\" value=\"" + githubUsername + "\">\n" +
        "</div>\n" +
        "<div class=\"field-group\">" +
        "<div class=\"checkbox\">" +
        "<input class=\"checkbox\" type=\"checkbox\" name=\"defaultGithubTeam\" id=\"defaultGithubTeam\">" +
        "<label for=\"defaultGithubTeam\">Default GitHub Team</label>" +
        "</div>" +
        "<div class=\"description\">" +
        "User will be added to default GitHub team (important for Jenkins white listing)" +
        "</div>" +
        "</div>" +
        "<hr>" +
        "<p class=\"padding-bottom\">For experienced GitHub Users, Seniors and Coordinators only!<br>" +
        "The user will be granted write access to the repositories of the following team(s).</p>" +
        checkBoxSet +
        "</fieldset>\n" +
        "</form>";
}

AJS.toInit(function () {
    //AJS.$(document).ajaxStart(function () {
    //    AJS.$(".loadingDiv").show();
    //});
    //AJS.$(document).ajaxStop(function () {
    //    AJS.$(".loadingDiv").hide();
    //});

    //var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");
    var baseUrl = AJS.$("meta[id$='-base-url']").attr("content");
    restBaseUrl = baseUrl + "/rest/timesheet/latest/";

    var config;
    getConfigAndCallback(baseUrl, function (ajaxConfig) {
        config = ajaxConfig;
    });

    var dialog = new AJS.Dialog({
        width: 840,
        height: 400,
        id: "activate-dialog",
        closeOnOutsideClick: true
    });

    dialog.addHeader("Enable User");
    dialog.addPanel("Panel 1", tableSkeleton, "panel-body");

    dialog.addButton("OK", function (dialog) {
        getTeamList(baseUrl, modifyUser);
        dialog.hide();
    });
    dialog.addLink("Cancel", function (dialog) {
        dialog.hide();
    }, "#");

    function populateTable() {
        //AJS.$(".loadingDiv").show();
        AJS.$.ajax({
            url: restBaseUrl + 'user/getUsers',
            dataType: "json",
            success: function (users) {
                AJS.$("#user-body").empty();
                for (var i = 0; i < users.length; i++) {
                    var obj = users[i];
                    var username = obj['active'] ? obj['userName'] : "<del>" + obj['userName'] + "</del>";
                    var actionClass = obj['active'] ? "disable" : "enable";
                    var githubColumnText = obj['githubName'] ? obj['githubName'] : "add GitHub name";
                    var githubColumn = obj['active'] ?
                    "<a id=\"" + obj['userName'] + "\" class=\"change-github\" href=\"#\">" + githubColumnText + "</a>" :
                        (obj['githubName'] ? obj['githubName'] : "");
                    AJS.$("#user-body").append("<tr><td headers=\"basic-username\" class=\"username\">" + username + "</td>" +
                    "<td headers=\"basic-first-name\" class=\"first-name\">" + obj['firstName'] + "</td>" +
                    "<td headers=\"basic-last-name\" class=\"last-name\">" + obj['lastName'] + "</td>" +
                    "<td headers=\"basic-email\" class=\"email\">" + obj['email'] + "</td>" +
                    "<td headers=\"basic-github\" class=\"github\">" + githubColumn + "</td>" +
                    "<td headers=\"basic-action\" class=\"action\"><a id=\"" + obj['userName'] + "\" class=\"" + actionClass + "\" href=\"#\">" + actionClass + "</a></tr>");
                }

                AJS.$("#user-table").trigger("update");
                var userList = new List("modify-user", {
                    page: Number.MAX_VALUE,
                    valueNames: ["username", "first-name", "last-name", "email", "github", "action"]
                });

                userList.on('updated', function () {
                    AJS.$("#user-table").trigger("update");
                });

                //AJS.$(".loadingDiv").hide();
            },
            error: function () {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!"
                });
                //AJS.$(".loadingDiv").hide();
            }
        }).done(function () {
            AJS.$(".disable").click(function (event) {
                event.preventDefault();
                inactivateUser(event.target.id);
            });
            AJS.$(".enable").click(function (event) {
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
        var teamList = config.teams;
        var checkboxSet = "<fieldset class=\"group\">\n" +
            "<legend><span>Team</span></legend>\n";
        for (var i = 0; i < teamList.length; i++) {
            checkboxSet += "<div class=\"checkbox\">\n" +
            "<input class=\"checkbox\" type=\"checkbox\" name=\"" + teamList[i].name + "\" id=\"change-github-dialog-" + teamList[i].name.replace(/\W/g, '-') + "\">\n" +
            "<label for=\"change-github-dialog-" + teamList[i].name.replace(/\W/g, '-') + "\">" + teamList[i].name + "</label>\n" +
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
                if (AJS.$("#change-github-dialog-" + teamList[i].name.replace(/\W/g, '-')).prop("checked")) {
                    selectedTeamList.push(teamList[i].name);
                }
            }
            changeGithubUser(userName, AJS.$("#github-name").auiSelect2("val"), AJS.$("#defaultGithubTeam").prop("checked"), selectedTeamList);
            dialog.remove();
        });
        dialog.addLink("Cancel", function (dialog) {
            dialog.remove();
        }, "#");

        dialog.show();

        AJS.$("#github-name").auiSelect2({
            placeholder: "Search for user",
            ajax: {
                url: "https://api.github.com/search/users",
                dataType: "json",
                data: function (term, page) {
                    return "q=" + term + "+type:User&order=asc&access_token=" + config.githubTokenPublic;
                },
                results: function (data, page) {
                    var select2data = [];
                    select2data.push({id: '', text: 'delete name', it: true});
                    for (var i = 0; i < data.items.length; i++) {
                        select2data.push({id: data.items[i].login, text: data.items[i].login});
                    }
                    return {results: select2data};
                }
            },
            formatResult: function(result){
                if(result.it) {
                    return '<i><b>' + result.text + '</b></i>';
                }

                return result.text;
            }
        });

        if (githubName.length != 0) {
            AJS.$("#github-name").auiSelect2("data", {id: githubName, text: githubName});
        }
    }

    function changeGithubUser(userName, githubName, defaultTeam, teamList) {
        var jsonUser = {
            userName: userName,
            githubName: githubName,
            addToDefaultGithubTeam: defaultTeam,
            developerList: teamList
        };
        AJS.$(".loadingDiv").show();
        AJS.$.ajax({
            url: restBaseUrl + 'github/changeGithubname',
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify(jsonUser),
            success: function () {
                populateTable();
                AJS.messages.success({
                    title: "Success!",
                    body: "GitHub User changed!"
                });

                AJS.$(".loadingDiv").hide();
            },
            error: function (e) {
                AJS.messages.error({
                    title: "Error!",
                    body: e.responseText
                });
                AJS.$(".loadingDiv").hide();
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
            var value = AJS.$("input[name='" + teamList[i].replace(/\W/g, "-") + "']:checked").val();
            if (value == "coordinator") {
                userToModify.coordinatorList.push(teamList[i]);
            } else if (value == "senior") {
                userToModify.seniorList.push(teamList[i]);
            } else if (value == "developer") {
                userToModify.developerList.push(teamList[i]);
            }
        }

        AJS.$(".loadingDiv").show();
        AJS.$.ajax({
            url: restBaseUrl + 'user/activateUser',
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify(userToModify),
            success: function () {
                populateTable();
                AJS.messages.success({
                    title: "Success!",
                    body: "User enabled!"
                });
                AJS.$(".loadingDiv").hide();
            },
            error: function (e) {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!<br />" + e.responseText
                });
                AJS.$(".loadingDiv").hide();
            }
        });
    }

    function inactivateUser(userName) {
        AJS.$(".loadingDiv").show();
        AJS.$.ajax({
            url: restBaseUrl + 'user/inactivateUser',
            type: "PUT",
            contentType: "application/json",
            data: userName,
            success: function () {
                populateTable();
                AJS.messages.success({
                    title: "Success!",
                    body: "User disabled!"
                });
                AJS.$(".loadingDiv").hide();
            },
            error: function (e) {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!<br />" + e.responseText
                });
                AJS.$(".loadingDiv").hide();
            }
        });
    }

    function activateUser(userName) {
        dialog.gotoPage(0);
        dialog.gotoPanel(0);
        dialog.userName = userName;
        dialog.show();
        populateTeamTable(config, "#team-body", "#individual-resources");
    }

    populateTable();


});