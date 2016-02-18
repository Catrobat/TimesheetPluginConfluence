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
    var teams = [];
    var editNameDialog;

    function scrollToAnchor(aid) {
        var aTag = AJS.$("a[name='" + aid + "']");
        AJS.$('html,body').animate({scrollTop: aTag.offset().top}, 'slow');
    }

    function fetchData() {

        var allUsersFetched = AJS.$.ajax({
            type: 'GET',
            url: restBaseUrl + 'user/getUsers',
            contentType: "application/json"
        });

        var categoriesFetched = AJS.$.ajax({
             type: 'GET',
             url: restBaseUrl + 'config/getCategories',
             contentType: "application/json"
        });

        AJS.$.when(allUsersFetched, categoriesFetched)
            .done(populateForm)
            .fail(function (error) {
                AJS.messages.error({
                  title: 'There was an error while fetching user data.',
                  body: '<p>Reason: ' + error.responseText + '</p>'
                });
                console.log(error);
            });
    }

    function populateForm(allUsers, allCategories) {
        AJS.$(".loadingDiv").show();
        AJS.$.ajax({
            url: restBaseUrl + 'config/getConfig',
            dataType: "json",
            success: function (config) {
                if (config.mailFromName)
                    AJS.$("#mail-from-name").val(config.mailFromName);
                if (config.mailFrom)
                    AJS.$("#mail-from").val(config.mailFrom);

                if (config.mailSubject)
                    AJS.$("#mail-subject-out-of-time").val(config.mailSubject);
                if (config.mailSubject)
                    AJS.$("#mail-subject-inactive").val(config.mailSubject);
                if (config.mailSubject)
                    AJS.$("#mail-subject-entry-change").val(config.mailSubject);


                if (config.mailBody)
                    AJS.$("#mail-body-out-of-time").val(config.mailBody);
                if (config.mailBody)
                    AJS.$("#mail-body-inactive").val(config.mailBody);
                if (config.mailBody)
                    AJS.$("#mail-body-entry-change").val(config.mailBody);

                teams = [];
                AJS.$("#teams").empty();
                for (var i = 0; i < config.teams.length; i++) {
                    var team = config.teams[i];
                    teams.push(team['teamName']);

                    var tempTeamName = team['teamName'].replace(/\W/g, '-');
                    AJS.$("#teams").append("<h3>" + team['teamName'] +
                    "<button class=\"aui-button aui-button-subtle\" value=\"" + team['teamName'] + "\">" +
                    "<span class=\"aui-icon aui-icon-small aui-iconfont-edit\">Editing</span> Edit</button></h3><fieldset>");
                    AJS.$("#teams").append("<div class=\"field-group\"><label for=\"" + tempTeamName + "-coordinator\">Coordinator</label><input class=\"text coordinator\" type=\"text\" id=\"" + tempTeamName + "-coordinator\" value=\"" + team['coordinatorGroups'] + "\"></div>");
                    AJS.$("#teams").append("<div class=\"field-group\"><label for=\"" + tempTeamName + "-senior\">Senior</label><input class=\"text senior\" type=\"text\" id=\"" + tempTeamName + "-senior\" value=\"" + team['seniorGroups'] + "\"></div>");
                    AJS.$("#teams").append("<div class=\"field-group\"><label for=\"" + tempTeamName + "-developer\">User</label><input class=\"text user\" type=\"text\" id=\"" + tempTeamName + "-developer\" value=\"" + team['developerGroups'] + "\"></div>");
                    AJS.$("#teams").append("<div class=\"field-group\"><label for=\"" + tempTeamName + "-category\">Category</label><input class=\"text category\" type=\"text\" id=\"" + tempTeamName + "-category\" value=\"" + team['teamCategoryNames'] + "\"></div>");
                    AJS.$("#teams").append("</fieldset>");
                }

                var userNameList = [];
                for (var i = 0; i < allUsers[0].length; i++) {
                    userNameList.push(allUsers[0][i]['userName']);
                }

                var categoryList = [];
                for (var i = 0; i < allCategories[0].length; i++) {
                    categoryList.push(allCategories[0][i]['categoryName']);
                }

                if (config.approvedUsers) {
                    AJS.$(".coordinator").auiSelect2({
                        placeholder: "Search for user",
                        tags: userNameList.sort(),
                        tokenSeparators: [",", " "]
                    });
                    AJS.$(".senior").auiSelect2({
                        placeholder: "Search for user",
                        tags: userNameList.sort(),
                        tokenSeparators: [",", " "]
                    });
                    AJS.$(".user").auiSelect2({
                        placeholder: "Search for user",
                        tags: userNameList.sort(),
                        tokenSeparators: [",", " "]
                    });
                    AJS.$(".category").auiSelect2({
                        placeholder: "Search for category",
                        tags: categoryList.sort(),
                        tokenSeparators: [",", " "]
                    });
                }

               AJS.$("#userdirectory").auiSelect2({
                    placeholder: "Search for directories",
                    minimumInputLength: 0,
                    ajax: {
                        url: restBaseUrl + 'config/getDirectories',
                        dataType: "json",
                        data: function (term, page) {
                            return {query: term};
                        },
                        results: function (data, page) {
                            var select2data = [];
                            for (var i = 0; i < data.length; i++) {
                                select2data.push({id: data[i].userDirectoryId, text: data[i].userDirectoryName});
                            }
                            return {results: select2data};
                        }
                    }
                });
                /*)
                AJS.$(".single-jira-group").auiSelect2({
                    placeholder: "Search for group",
                    minimumInputLength: 0,
                    ajax: {
                        //url: baseUrl + 'rest/api/2/groups/picker',
                        url: baseUrl + 'rest/prototype/1/search/user',
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
                    }
                });

                AJS.$("#plugin-permission").auiSelect2({
                    placeholder: "Search for users and groups",
                    minimumInputLength: 0,
                    tags: true,
                    tokenSeparators: [",", " "],
                    ajax: {
                        url: baseUrl + '/rest/prototype/1/search/user-or-group',
                        //url: baseUrl + 'rest/prototype/1/search/user',
                        dataType: "json",
                        data: function (term, page) {
                            return {query: term};
                        },
                        results: function (data, page) {
                            var select2data = [];
                            for (var i = 0; i < data.group.length; i++) {
                                select2data.push({
                                    id: "groups-" + data.group[i].name,
                                    text: data.group[i].name
                                });
                            }
                            for (var i = 0; i < data.result.length; i++) {
                                select2data.push({
                                    id: "users-" + data.result[i].name,
                                    text: data.result[i].name
                                });
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
                */
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
                AJS.$("#userdirectory").auiSelect2("data", {
                    id: config.userDirectoryId,
                    text: config.userDirectoryName
                });

                AJS.$(".loadingDiv").hide();
            },
            error: function (error) {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!"
                });

                AJS.$(".loadingDiv").hide();
            }
        });
    }

    function updateConfig() {
        var config = {};
        config.mailFromName = AJS.$("#mail-from-name").val();
        config.mailFrom = AJS.$("#mail-from").val();

        config.mailSubject = AJS.$("#mail-subject-out-of-time").val();
        config.mailSubject = AJS.$("#mail-subject-inactive").val();
        config.mailSubject = AJS.$("mail-subject-entry-change").val();

        config.mailBody = AJS.$("#mail-body-out-of-time").val();
        config.mailBody = AJS.$("#mail-body-inactive").val();
        config.mailBody = AJS.$("#mail-body-entry-change").val();

        var usersAndGroups = AJS.$("#plugin-permission").auiSelect2("val");
        var approvedUsers = [];
        var approvedGroups = [];

        /*
        for (var i = 0; i < usersAndGroups.length; i++) {
            if (usersAndGroups[i].match("^users-")) {
                approvedUsers.push(usersAndGroups[i].split("users-")[1]);
            } else if (usersAndGroups[i].match("^groups-")) {
                approvedGroups.push(usersAndGroups[i].split("groups-")[1]);
            }
        }*/

        config.approvedUsers = approvedUsers;
        config.approvedGroups = approvedGroups;
        config.teams = [];
        for (var i = 0; i < teams.length; i++) {
            var tempTeamName = teams[i].replace(/\W/g, '-');
            var tempTeam = {};
            tempTeam.teamName = teams[i];

            tempTeam.coordinatorGroups = AJS.$("#" + tempTeamName + "-coordinator").auiSelect2("val");
            for (var j = 0; j < tempTeam.coordinatorGroups.length; j++) {
                tempTeam.coordinatorGroups[j] = tempTeam.coordinatorGroups[j].replace(/^groups-/i, "");
            }

            tempTeam.seniorGroups = AJS.$("#" + tempTeamName + "-senior").auiSelect2("val");
            for (var j = 0; j < tempTeam.seniorGroups.length; j++) {
                tempTeam.seniorGroups[j] = tempTeam.seniorGroups[j].replace(/^groups-/i, "");
            }

            tempTeam.developerGroups = AJS.$("#" + tempTeamName + "-developer").auiSelect2("val");
            for (var j = 0; j < tempTeam.developerGroups.length; j++) {
                tempTeam.developerGroups[j] = tempTeam.developerGroups[j].replace(/^groups-/i, "");
            }

            tempTeam.teamCategoryNames = AJS.$("#" + tempTeamName + "-category").auiSelect2("val");
            for (var j = 0; j < tempTeam.teamCategoryNames.length; j++) {
                tempTeam.teamCategoryNames[j] = tempTeam.teamCategoryNames[j].replace(/^groups-/i, "");
            }
            config.teams.push(tempTeam);
        }

        AJS.$(".loadingDiv").show();
        AJS.$.ajax({
            url: restBaseUrl + 'config/saveConfig',
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify(config),
            processData: false,
            success: function () {
                AJS.messages.success({
                    title: "Success!",
                    body: "Settings saved!"
                });
                AJS.$(".loadingDiv").hide();
            },
            error: function (error) {
                AJS.messages.error({
                    title: "Error!",
                    body: error.responseText
                });
                AJS.$(".loadingDiv").hide();
            }
        });
    }

    function addTeam() {
        AJS.$(".loadingDiv").show();
        AJS.$.ajax({
            url: restBaseUrl + 'config/addTeamPermission',
            type: "PUT",
            contentType: "application/json",
            data: AJS.$("#team-name").attr("value"),
            processData: false,
            success: function () {
                AJS.messages.success({
                    title: "Success!",
                    body: "Team added!"
                });
                AJS.$(".loadingDiv").hide();
            },
            error: function (error) {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!<br />" + error.responseText
                });
                AJS.$(".loadingDiv").hide();
            }
        });
    }

    function addCategory() {
        AJS.$(".loadingDiv").show();
        AJS.$.ajax({
            url: restBaseUrl + 'config/addCategory',
            type: "PUT",
            contentType: "application/json",
            data: AJS.$("#category-name").attr("value"),
            processData: false,
            success: function () {
                AJS.messages.success({
                    title: "Success!",
                    body: "Category added!"
                });
                AJS.$(".loadingDiv").hide();
            },
            error: function (error) {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!<br />" + error.responseText
                });
                AJS.$(".loadingDiv").hide();
            }
        });
    }

    /*
    function addTeam() {
        AJS.$(".loadingDiv").show();
        AJS.$.ajax({
            url: restBaseUrl + 'config/addTeam',
            type: "PUT",
            contentType: "application/json",
            data: AJS.$("#team-name").attr("value"),
            processData: false,
            success: function () {
                AJS.messages.success({
                    title: "Success!",
                    body: "Team added!"
                });
                AJS.$(".loadingDiv").hide();
            },
            error: function (error) {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!<br />" + error.responseText
                });
                AJS.$(".loadingDiv").hide();
            }
        });
    }
    */

    function editTeam(teamName) {
        // may be in background and therefore needs to be removed
        if (editNameDialog) {
            try {
                editNameDialog.remove();
            } catch (err) {
                // may be removed already
            }
        }

        editNameDialog = new AJS.Dialog({
            width: 600,
            height: 200,
            id: "edit-name-dialog",
            closeOnOutsideClick: true
        });

        var content = "<form class=\"aui\">\n" +
            "    <fieldset>\n" +
            "        <div class=\"field-group\">\n" +
            "            <label for=\"new-name\">New Team Name</label>\n" +
            "            <input class=\"text\" type=\"text\" id=\"new-name\" name=\"new-name\" title=\"new-name\">\n" +
            "        </div>\n" +
            "    </fieldset>\n" +
            " </form> ";

        editNameDialog.addHeader("New Team Name for " + teamName);
        editNameDialog.addPanel("Panel 1", content, "panel-body");

        editNameDialog.addButton("Save", function (dialog) {
            AJS.$(".loadingDiv").show();
            AJS.$.ajax({
                url: restBaseUrl + 'config/editTeamPermission',
                type: "PUT",
                contentType: "application/json",
                data: JSON.stringify([teamName, AJS.$("#new-name").val()]),
                processData: false,
                success: function () {
                    AJS.messages.success({
                        title: "Success!",
                        body: "Team edited!"
                    });
                    fetchData();
                    scrollToAnchor('top');
                    AJS.$(".loadingDiv").hide();
                },
                error: function (error) {
                    AJS.messages.error({
                        title: "Error!",
                        body: "Something went wrong!<br />" + error.responseText
                    });
                    scrollToAnchor('top');
                    AJS.$(".loadingDiv").hide();
                }
            });

            dialog.remove();
        });
        editNameDialog.addLink("Cancel", function (dialog) {
            dialog.remove();
        }, "#");

        editNameDialog.gotoPage(0);
        editNameDialog.gotoPanel(0);
        editNameDialog.show();
    }

    function removeTeam() {
        AJS.$(".loadingDiv").show();
        AJS.$.ajax({
            url: restBaseUrl + 'config/removeTeamPermission',
            type: "PUT",
            contentType: "application/json",
            data: AJS.$("#team-name").attr("value"),
            processData: false,
            success: function () {
                AJS.messages.success({
                    title: "Success!",
                    body: "Team removed!"
                });
                AJS.$(".loadingDiv").hide();
            },
            error: function () {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!"
                });
                AJS.$(".loadingDiv").hide();
            }
        });
    }

    function removeCategory() {
        AJS.$(".loadingDiv").show();
        AJS.$.ajax({
            url: restBaseUrl + 'config/removeCategory',
            type: "PUT",
            contentType: "application/json",
            data: AJS.$("#category-name").attr("value"),
            processData: false,
            success: function () {
                AJS.messages.success({
                    title: "Success!",
                    body: "Category removed!"
                });
                AJS.$(".loadingDiv").hide();
            },
            error: function () {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!"
                });
                AJS.$(".loadingDiv").hide();
            }
        });
    }

    /*
    function removeTeam() {
        AJS.$(".loadingDiv").show();
        AJS.$.ajax({
            url: restBaseUrl + 'config/removeTeam',
            type: "PUT",
            contentType: "application/json",
            data: AJS.$("#team-name").attr("value"),
            processData: false,
            success: function () {
                AJS.messages.success({
                    title: "Success!",
                    body: "Team removed!"
                });
                AJS.$(".loadingDiv").hide();
            },
            error: function () {
                AJS.messages.error({
                    title: "Error!",
                    body: "Something went wrong!"
                });
                AJS.$(".loadingDiv").hide();
            }
        });
    }
    */

    fetchData();

    AJS.$("#general").submit(function (e) {
        e.preventDefault();
        if (AJS.$(document.activeElement).val() === 'Save') {
            updateConfig();
            scrollToAnchor('top');
        } else {
            editTeam(AJS.$(document.activeElement).val());
        }
    });

    AJS.$("#modify-team").submit(function (e) {
        e.preventDefault();
        addTeam();
        scrollToAnchor('top');
    });

    AJS.$("#removeTeam").click(function (e) {
        e.preventDefault();
        removeTeam();
        scrollToAnchor('top');
    });

    AJS.$("#modify-categoryList").submit(function (e) {
        e.preventDefault();
        addCategory();
        scrollToAnchor('top');
    });

    AJS.$("#removeCategory").click(function (e) {
        e.preventDefault();
        removeCategory();
        scrollToAnchor('top');
    });

    AJS.$("a[href='#tabs-general']").click(function () {
        AJS.$("#teams").html("");
        fetchData();
    });

    function unescapeHtml(safe) {
        if(safe) {
            return AJS.$('<div />').html(safe).text();
        } else {
            return '';
        }
    }
});