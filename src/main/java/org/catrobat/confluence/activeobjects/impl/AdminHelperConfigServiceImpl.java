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

package org.catrobat.confluence.activeobjects.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.Query;
import org.catrobat.confluence.activeobjects.*;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class AdminHelperConfigServiceImpl implements AdminHelperConfigService {

    private final ActiveObjects ao;

    public AdminHelperConfigServiceImpl(ActiveObjects ao) {
        this.ao = ao;
    }

    @Override
    public AdminHelperConfig editMail(String mailFromName, String mailFrom, String mailSubject, String mailBody) {
        AdminHelperConfig config = getConfiguration();
        config.setMailFromName(mailFromName);
        config.setMailFrom(mailFrom);
        config.setMailSubject(mailSubject);
        config.setMailBody(mailBody);
        config.save();
        return config;
    }

    @Override
    public AdminHelperConfig getConfiguration() {
        AdminHelperConfig[] config = ao.find(AdminHelperConfig.class);
        if (config.length == 0) {
            ao.create(AdminHelperConfig.class).save();
            config = ao.find(AdminHelperConfig.class);
        }

        return config[0];
    }

    @Override
    public AdminHelperConfig setPublicApiToken(String publicApiToken) {
        AdminHelperConfig configuration = getConfiguration();
        configuration.setPublicGithubApiToken(publicApiToken);
        configuration.save();
        return configuration;
    }

    @Override
    public AdminHelperConfig setApiToken(String apiToken) {
        AdminHelperConfig configuration = getConfiguration();
        configuration.setGithubApiToken(apiToken);
        configuration.save();
        return configuration;
    }

    @Override
    public AdminHelperConfig setOrganisation(String organisation) {
        AdminHelperConfig configuration = getConfiguration();
        configuration.setGithubOrganisation(organisation);
        configuration.save();
        return configuration;
    }

    @Override
    public AdminHelperConfig setUserDirectoryId(long userDirectoryId) {
        AdminHelperConfig config = getConfiguration();
        config.setUserDirectoryId(userDirectoryId);
        config.save();
        return config;
    }

    @Override
    public AdminHelperConfig setDefaultGithubTeamId(int defaultGithubTeamId) {
        AdminHelperConfig config = getConfiguration();
        config.setDefaultGithubTeamId(defaultGithubTeamId);
        config.save();
        return config;
    }

    @Override
    public Team addTeam(String teamName, List<String> coordinatorGroups, List<String> seniorGroups, List<String> developerGroups) {
        if (teamName == null || teamName.trim().length() == 0) {
            return null;
        }
        teamName = teamName.trim();

        Team[] teamArray = ao.find(Team.class, Query.select().where("upper(\"TEAM_NAME\") = upper(?)", teamName));
        if (teamArray.length != 0) {
            return null;
        }

        AdminHelperConfig configuration = getConfiguration();
        Team team = ao.create(Team.class);
        team.setConfiguration(configuration);
        team.setTeamName(teamName);

        fillTeam(team, TeamToGroup.Role.COORDINATOR, coordinatorGroups);
        fillTeam(team, TeamToGroup.Role.SENIOR, seniorGroups);
        fillTeam(team, TeamToGroup.Role.DEVELOPER, developerGroups);
        team.save();

        return team;
    }

    private void fillTeam(Team team, TeamToGroup.Role role, List<String> teamList) {
        if (teamList == null) {
            return;
        }

        for (String groupName : teamList) {
            Group[] groupArray = ao.find(Group.class, Query.select().where("upper(\"GROUP_NAME\") = upper(?)", groupName));
            Group group;
            if (groupArray.length == 0) {
                group = ao.create(Group.class);
            } else {
                group = groupArray[0];
            }

            group.setGroupName(groupName);
            group.save();

            TeamToGroup mapper = ao.create(TeamToGroup.class);
            mapper.setGroup(group);
            mapper.setTeam(team);
            mapper.setRole(role);
            mapper.save();
        }
    }

    @Override
    public void clearApprovedGroups() {
        for (ApprovedGroup approvedGroup : ao.find(ApprovedGroup.class)) {
            ao.delete(approvedGroup);
        }
    }

    @Override
    public void clearApprovedUsers() {
        for (ApprovedUser approvedUser : ao.find(ApprovedUser.class)) {
            ao.delete(approvedUser);
        }
    }

    @Override
    public AdminHelperConfig editTeam(String oldTeamName, String newTeamName) {
        if (oldTeamName == null || newTeamName == null) {
            return null;
        }

        Team[] tempTeamArray = ao.find(Team.class, Query.select().where("upper(\"TEAM_NAME\") = upper(?)", oldTeamName));
        if (tempTeamArray.length == 0) {
            return null;
        }
        Team team = tempTeamArray[0];

        tempTeamArray = ao.find(Team.class, Query.select().where("upper(\"TEAM_NAME\") = upper(?)", newTeamName));
        if (tempTeamArray.length != 0) {
            return null;
        }

        team.setTeamName(newTeamName);
        team.save();

        return getConfiguration();
    }

    @Override
    public AdminHelperConfig addResource(String resourceName, String groupName) {
        if (resourceName == null || resourceName.trim().length() == 0) {
            return null;
        }
        String tempResourceName = escapeHtml4(resourceName.trim());
        Resource[] resources = ao.find(Resource.class, Query.select().where("upper(\"RESOURCE_NAME\") = upper(?)", tempResourceName));
        if (resources.length != 0) {
            return null;
        }

        AdminHelperConfig config = getConfiguration();
        Resource resource = ao.create(Resource.class);
        resource.setResourceName(tempResourceName);
        resource.setGroupName(groupName);
        resource.setConfiguration(config);
        resource.save();

        return config;
    }

    @Override
    public AdminHelperConfig editResource(String resourceName, String newGroupName) {
        if (resourceName == null || resourceName.trim().length() == 0) {
            return null;
        }
        String tempResourceName = escapeHtml4(resourceName.trim());
        Resource[] resources = ao.find(Resource.class, Query.select().where("upper(\"RESOURCE_NAME\") = upper(?)", tempResourceName));
        if (resources.length == 0) {
            return null;
        }
        resources[0].setGroupName(newGroupName);
        resources[0].save();

        return getConfiguration();
    }

    @Override
    public AdminHelperConfig removeResource(String resourceName) {
        String tempResourceName = null;
        if (resourceName != null) {
            tempResourceName = escapeHtml4(resourceName.trim());
        }

        Resource[] resources = ao.find(Resource.class, Query.select().where("upper(\"RESOURCE_NAME\") = upper(?)", tempResourceName));
        ao.delete(resources);

        return getConfiguration();
    }

    @Override
    public AdminHelperConfig removeTeam(String teamName) {
        Team[] teamArray = ao.find(Team.class, Query.select().where("upper(\"TEAM_NAME\") = upper(?)", teamName));
        if (teamArray.length == 0) {
            return null;
        }
        Team team = teamArray[0];
        Group[] groupArray = team.getGroups();
        TeamToGroup[] teamToGroupArray = ao.find(TeamToGroup.class, Query.select().where("\"TEAM_ID\" = ?", team.getID()));
        for (TeamToGroup teamToGroup : teamToGroupArray) {
            ao.delete(teamToGroup);
        }

        for (Group group : groupArray) {
            if (group.getTeams().length == 0) {
                ao.delete(group);
            }
        }

        ao.delete(team);

        return getConfiguration();
    }

    @Override
    public List<String> getGroupsForRole(String teamName, TeamToGroup.Role role) {
        List<String> groupList = new ArrayList<String>();
        TeamToGroup[] teamToGroupArray = ao.find(TeamToGroup.class, Query.select()
                        .where("\"ROLE\" = ?", role)
        );

        for (TeamToGroup teamToGroup : teamToGroupArray) {
            if (teamToGroup.getTeam().getTeamName().toLowerCase().equals(teamName.toLowerCase())) {
                groupList.add(teamToGroup.getGroup().getGroupName());
            }
        }

        // TODO issues with postgresql
//        Group[] groupArray = ao.find(Group.class, Query.select()
//                        .alias(Group.class, "jiragroup")
//                        .alias(TeamToGroup.class, "mapper")
//                        .alias(Team.class, "team")
//                        .join(TeamToGroup.class, "jiragroup.ID = mapper.GROUP_ID")
//                        .join(Team.class, "mapper.TEAM_ID = team.ID")
//                        .where("upper( team.TEAM_NAME ) = upper(?) and mapper.ROLE = ?", teamName, role)
//        );
//
//        for (Group group : groupArray) {
//            groupList.add(group.getGroupName());
//        }

        return groupList;
    }

    @Override
    public ApprovedGroup addApprovedGroup(String approvedGroupName) {
        if (approvedGroupName == null || approvedGroupName.trim().length() == 0) {
            return null;
        }
        approvedGroupName = approvedGroupName.trim();

        ApprovedGroup[] approvedGroupArray = ao.find(ApprovedGroup.class, Query.select()
                .where("upper(\"GROUP_NAME\") = upper(?)", approvedGroupName));
        if (approvedGroupArray.length == 0) {
            ApprovedGroup approvedGroup = ao.create(ApprovedGroup.class);
            approvedGroup.setGroupName(approvedGroupName);
            approvedGroup.setConfiguration(getConfiguration());
            approvedGroup.save();

            return approvedGroup;
        } else {
            return approvedGroupArray[0];
        }
    }

    @Override
    public ApprovedUser addApprovedUser(String approvedUserKey) {
        if (approvedUserKey == null || approvedUserKey.trim().length() == 0) {
            return null;
        }
        approvedUserKey = approvedUserKey.trim();

        ApprovedUser[] approvedUserArray = ao.find(ApprovedUser.class, Query.select()
                .where("upper(\"USER_KEY\") = upper(?)", approvedUserKey));
        if (approvedUserArray.length == 0) {
            ApprovedUser approvedUser = ao.create(ApprovedUser.class);
            approvedUser.setUserKey(approvedUserKey);
            approvedUser.setConfiguration(getConfiguration());
            approvedUser.save();

            return approvedUser;
        } else {
            return approvedUserArray[0];
        }
    }

    @Override
    public boolean isGroupApproved(String groupName) {
        if (groupName != null) {
            groupName = groupName.trim();
        }

        return (ao.find(ApprovedGroup.class).length == 0 && ao.find(ApprovedUser.class).length == 0) ||
                ao.find(ApprovedGroup.class, Query.select()
                        .where("upper(\"GROUP_NAME\") = upper(?)", groupName)).length != 0;
    }

    @Override
    public boolean isUserApproved(String userKey) {
        if (userKey != null) {
            userKey = userKey.trim();
        }

        return ao.find(ApprovedUser.class, Query.select().where("upper(\"USER_KEY\") = upper(?)", userKey)).length != 0;
    }

    @Override
    public AdminHelperConfig removeApprovedGroup(String approvedGroupName) {
        if (approvedGroupName != null) {
            approvedGroupName = approvedGroupName.trim();
        }

        ApprovedGroup[] approvedGroupArray = ao.find(ApprovedGroup.class, Query.select()
                .where("upper(\"GROUP_NAME\") = upper(?)", approvedGroupName));
        if (approvedGroupArray.length == 0) {
            return null;
        }
        ao.delete(approvedGroupArray[0]);

        return getConfiguration();
    }

    @Override
    public AdminHelperConfig removeApprovedUser(String approvedUserKey) {
        if (approvedUserKey != null) {
            approvedUserKey = approvedUserKey.trim();
        }

        ApprovedUser[] approvedUserArray = ao.find(ApprovedUser.class, Query.select()
                .where("upper(\"USER_KEY\") = upper(?)", approvedUserKey));
        if (approvedUserArray.length == 0) {
            return null;
        }
        ao.delete(approvedUserArray[0]);

        return getConfiguration();
    }
}
