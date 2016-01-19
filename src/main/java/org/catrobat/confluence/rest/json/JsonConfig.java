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

package org.catrobat.confluence.rest.json;

import org.catrobat.confluence.activeobjects.AdminHelperConfig;
import org.catrobat.confluence.activeobjects.AdminHelperConfigService;
import org.catrobat.confluence.activeobjects.Resource;
import org.catrobat.confluence.activeobjects.Team;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("unused")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class JsonConfig {

    @XmlElement
    private String githubToken;
    @XmlElement
    private String githubTokenPublic;
    @XmlElement
    private String githubOrganization;
    @XmlElement
    private String defaultGithubTeam;
    @XmlElement
    private List<JsonResource> resources;
    @XmlElement
    private List<JsonTeam> teams;
    @XmlElement
    private List<String> approvedGroups;
    @XmlElement
    private List<String> approvedUsers;
    @XmlElement
    private List<String> availableGithubTeams;
    @XmlElement
    private long userDirectoryId;
    @XmlElement
    private String userDirectoryName;
    @XmlElement
    private String mailFromName;
    @XmlElement
    private String mailFrom;
    @XmlElement
    private String mailSubject;
    @XmlElement
    private String mailBody;

    public JsonConfig() {

    }

    public JsonConfig(AdminHelperConfigService configService) {
        AdminHelperConfig toCopy = configService.getConfiguration();
        if (toCopy.getGithubApiToken() != null && toCopy.getGithubApiToken().length() != 0) {
            this.githubToken = "enter token if you want to change it";
        } else {
            this.githubToken = null;
        }

        this.githubTokenPublic = toCopy.getPublicGithubApiToken();
        this.githubOrganization = toCopy.getGithubOrganisation();

        //ToDo: hier crasht es weil Resource empty ist
        Map<String, JsonResource> tempMap = new TreeMap<String, JsonResource>();
        for (Resource resource : toCopy.getResources()) {
            tempMap.put(resource.getResourceName().toLowerCase(), new JsonResource(resource));
        }
        this.resources = new ArrayList<JsonResource>(tempMap.values());

        Map<String, JsonTeam> teamMap = new TreeMap<String, JsonTeam>();
        for (Team team : toCopy.getTeams()) {
            teamMap.put(team.getTeamName(), new JsonTeam(team, configService));
        }

        this.teams = new ArrayList<JsonTeam>();
        this.teams.addAll(teamMap.values());

        this.approvedUsers = new ArrayList<String>();
        /*
        UserManager userManager = ComponentAccessor.getUserManager();

        for (ApprovedUser approvedUser : toCopy.getApprovedUsers()) {
            if (userManager.getUserByKey(approvedUser.getUserKey()) != null) {
                ApplicationUser user = userManager.getUserByKey(approvedUser.getUserKey());
                if (user != null) {
                    approvedUsers.add(user.getUsername());
                }
            }
        }
        */

        this.approvedGroups = new ArrayList<String>();
        /*
        for (ApprovedGroup approvedGroup : toCopy.getApprovedGroups()) {
            approvedGroups.add(approvedGroup.getGroupName());
        }
        */

        this.availableGithubTeams =  new ArrayList<String>();
        this.defaultGithubTeam =  "";

        this.userDirectoryId = toCopy.getUserDirectoryId();
        /*
        DirectoryManager directoryManager = ComponentAccessor.getComponent(DirectoryManager.class);
        try {
            this.userDirectoryName = directoryManager.findDirectoryById(userDirectoryId).getName();
        } catch (DirectoryNotFoundException e) {
            this.userDirectoryId = -1;
            this.userDirectoryName = null;
        }
        */
        this.userDirectoryId = -1;
        this.userDirectoryName = null;

        this.mailFromName = toCopy.getMailFromName();
        this.mailFrom = toCopy.getMailFrom();
        this.mailSubject = toCopy.getMailSubject();
        this.mailBody = toCopy.getMailBody();
    }

    public String getGithubToken() {
        return githubToken;
    }

    public void setGithubToken(String githubToken) {
        this.githubToken = githubToken;
    }

    public String getGithubOrganization() {
        return githubOrganization;
    }

    public void setGithubOrganization(String githubOrganization) {
        this.githubOrganization = githubOrganization;
    }

    public List<JsonTeam> getTeams() {
        return teams;
    }

    public void setTeams(List<JsonTeam> teams) {
        this.teams = teams;
    }

    public List<String> getApprovedGroups() {
        return approvedGroups;
    }

    public void setApprovedGroups(List<String> approvedGroups) {
        this.approvedGroups = approvedGroups;
    }

    public List<String> getAvailableGithubTeams() {
        return availableGithubTeams;
    }

    public void setAvailableGithubTeams(List<String> availableGithubTeams) {
        this.availableGithubTeams = availableGithubTeams;
    }

    public String getGithubTokenPublic() {
        return githubTokenPublic;
    }

    public void setGithubTokenPublic(String githubTokenPublic) {
        this.githubTokenPublic = githubTokenPublic;
    }

    public List<String> getApprovedUsers() {
        return approvedUsers;
    }

    public void setApprovedUsers(List<String> approvedUsers) {
        this.approvedUsers = approvedUsers;
    }

    public long getUserDirectoryId() {
        return userDirectoryId;
    }

    public void setUserDirectoryId(long userDirectoryId) {
        this.userDirectoryId = userDirectoryId;
    }

    public String getUserDirectoryName() {
        return userDirectoryName;
    }

    public void setUserDirectoryName(String userDirectoryName) {
        this.userDirectoryName = userDirectoryName;
    }

    public List<JsonResource> getResources() {
        return resources;
    }

    public void setResources(List<JsonResource> resources) {
        this.resources = resources;
    }

    public String getDefaultGithubTeam() {
        return defaultGithubTeam;
    }

    public void setDefaultGithubTeam(String defaultGithubTeam) {
        this.defaultGithubTeam = defaultGithubTeam;
    }

    public String getMailFromName() {
        return mailFromName;
    }

    public void setMailFromName(String mailFromName) {
        this.mailFromName = mailFromName;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public String getMailBody() {
        return mailBody;
    }

    public void setMailBody(String mailBody) {
        this.mailBody = mailBody;
    }
}
