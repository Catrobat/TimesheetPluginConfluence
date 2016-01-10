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

package org.catrobat.confluence.helper;

import org.catrobat.confluence.activeobjects.AdminHelperConfigService;
import org.catrobat.confluence.rest.json.JsonTeam;
import org.catrobat.confluence.rest.json.JsonUser;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.*;

public class GithubHelper {

    private final AdminHelperConfigService configService;
    private final String token;
    private final String organizationName;

    public GithubHelper(AdminHelperConfigService configService) {
        this.configService = configService;
        this.token = configService.getConfiguration().getGithubApiToken();
        this.organizationName = configService.getConfiguration().getGithubOrganisation();
    }

    public boolean doesUserExist(final String userName) {
        if (userName == null || userName.equals("")) {
            return false;
        }

        try {
            GitHub gitHub = GitHub.connectUsingOAuth(token);
            GHUser user = gitHub.getUser(userName);

            if (user != null) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String removeUserFromAllOldGroups(final JsonUser jsonUser) {
        org.catrobat.confluence.rest.json.JsonConfig config = new org.catrobat.confluence.rest.json.JsonConfig(configService);
        Set<String> githubTeamSet = new HashSet<String>();
        if (jsonUser.getDeveloperList() != null) {
            for (String developerOf : jsonUser.getDeveloperList()) {
                for (JsonTeam team : config.getTeams()) {
                    if (team.getTeamName().equals(developerOf)) {
                        githubTeamSet.addAll(team.getGithubTeams());
                    }
                }
            }
        } else {
            return "Developer-List must be given";
        }

        if (jsonUser.isAddToDefaultGithubTeam()) {
            githubTeamSet.add(config.getDefaultGithubTeam());
        }

        try {
            GitHub gitHub = GitHub.connectUsingOAuth(token);
            GHOrganization organization = gitHub.getOrganization(organizationName);
            GHUser user = gitHub.getUser(jsonUser.getGithubName());

            if (organization == null || user == null)
                return "User and/or Organization is null";

            for (Map.Entry<String, GHTeam> entrySet : organization.getTeams().entrySet()) {
                boolean delete = true;
                for (String team : githubTeamSet) {
                    if (entrySet.getValue().getName().toLowerCase().equals(team.toLowerCase())) {
                        // do nothing - the user is still in this team
                        delete = false;
                        break;
                    }
                }

                if (delete) {
                    entrySet.getValue().remove(user);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return null; // everything went fine
    }

    public String removeUserFromOrganization(final String userName) {
        if (!doesUserExist(userName)) {
            return null; // does not exist - nobody to remove
        }

        try {
            GitHub gitHub = GitHub.connectUsingOAuth(token);
            GHOrganization organization = gitHub.getOrganization(organizationName);
            GHUser user = gitHub.getUser(userName);

            if (organization.hasMember(user)) {
                organization.remove(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return null; // everything went fine
    }

    public String addUserToDefaultTeam(final String userName) {
        if (userName == null || !doesUserExist(userName)) {
            return "User does not exist on GitHub";
        }
        int teamId = configService.getConfiguration().getDefaultGithubTeamId();

        try {
            GitHub gitHub = GitHub.connectUsingOAuth(token);
            GHOrganization organization = gitHub.getOrganization(organizationName);
            GHTeam team = null;
            for (Map.Entry<String, GHTeam> entrySet : organization.getTeams().entrySet()) {
                if (entrySet.getValue().getId() == teamId) {
                    team = entrySet.getValue();
                    break;
                }
            }
            GHUser user = gitHub.getUser(userName);
            if (team != null && user != null) {
                team.add(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return null; //everything went fine
    }

    public String addUserToTeam(final String userName, final String teamName) {
        if (userName == null || !doesUserExist(userName)) {
            return "User does not exist on GitHub";
        }

        try {
            GitHub gitHub = GitHub.connectUsingOAuth(token);
            GHOrganization organization = gitHub.getOrganization(organizationName);
            GHTeam team = organization.getTeamByName(teamName);
            GHUser user = gitHub.getUser(userName);
            team.add(user);
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return null; // everything went fine

    }

    public List<String> getAvailableTeams() {
        if (organizationName == null || organizationName.length() == 0) {
            return null;
        }

        Map<String, GHTeam> teams = new TreeMap<String, GHTeam>();
        try {
            GitHub gitHub = GitHub.connectUsingOAuth(token);
            GHOrganization organization = gitHub.getOrganization(organizationName);
            teams = organization.getTeams();
        } catch (IOException e) {
            // is ok - return the list anyway
        }

        return new ArrayList<String>(teams.keySet());
    }

    public String getTeamName(int githubTeamId) {
        try {
            GitHub gitHub = GitHub.connectUsingOAuth(token);
            GHOrganization organization = gitHub.getOrganization(organizationName);
            for (GHTeam team : organization.listTeams()) {
                if (team.getId() == githubTeamId) {
                    return team.getName();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;    // team with given id not found
    }
}
