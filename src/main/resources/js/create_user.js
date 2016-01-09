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
    //AJS.$(document).ajaxStart(function () {
    //    AJS.$(".loadingDiv").show();
    //});
    //AJS.$(document).ajaxStop(function () {
    //    AJS.$(".loadingDiv").hide();
    //});

    var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");
    var config;

    getConfigAndCallback(baseUrl, function (ajaxConfig) {
        config = ajaxConfig;
        populateTeamTable(config, "#team-body", "#individual-resources");
        AJS.$("#github").auiSelect2({
            placeholder: "Search for user",
            minimumInputLength: 1,
            ajax: {
                url: "https://api.github.com/search/users",
                dataType: "json",
                data: function (term, page) {
                    return "q=" + term + "+type:User&order=asc&access_token=" + config.githubTokenPublic;
                },
                results: function (data, page) {
                    var select2data = [];
                    for (var i = 0; i < data.items.length; i++) {
                        select2data.push({id: data.items[i].login, text: data.items[i].login});
                    }
                    return {results: select2data};
                }
            }
        });
    });

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
        AJS.$("#create")[0].reset();
        AJS.$("#github").auiSelect2("data", null);
    }

    function createUser(teamList) {
        var userToCreate = {};
        userToCreate.userName = AJS.$("#username").attr("value");
        userToCreate.firstName = AJS.$("#firstname").attr("value");
        userToCreate.lastName = AJS.$("#lastname").attr("value");
        userToCreate.email = AJS.$("#email").attr("value");
        userToCreate.githubName = AJS.$("#github").auiSelect2("val");
        userToCreate.addToDefaultGithubTeam = AJS.$("#defaultGithubTeam").prop("checked");
        userToCreate.coordinatorList = [];
        userToCreate.seniorList = [];
        userToCreate.developerList = [];

        for (var i = 0; i < teamList.length; i++) {
            var value = AJS.$("input[name='" + teamList[i].replace(/\W/g, "-") + "']:checked").val();
            if (value == "coordinator") {
                userToCreate.coordinatorList.push(teamList[i]);
            } else if (value == "senior") {
                userToCreate.seniorList.push(teamList[i]);
            } else if (value == "developer") {
                userToCreate.developerList.push(teamList[i]);
            }
        }
        userToCreate.resourceList = [];
        for (i = 0; i < config.resources.length; i++) {
            var resource = config.resources[i];
            if (AJS.$('#' + resource.resourceName.replace(/\W/g, "-")).attr('checked')) {
                userToCreate.resourceList.push(resource);
            }
        }

        AJS.$(".loadingDiv").show();
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

    AJS.$("#create").submit(function (e) {
        e.preventDefault();
        getTeamList(baseUrl, createUser);
    });
    AJS.$(".cancel").click(function (e) {
        e.preventDefault();
        resetForm();
    });
});