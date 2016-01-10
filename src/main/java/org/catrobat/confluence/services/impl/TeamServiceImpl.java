package org.catrobat.confluence.services.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.confluence.core.service.NotAuthorizedException;
import com.atlassian.confluence.user.UserAccessor;
import net.java.ao.Query;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.services.TeamService;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import java.util.Set;
import java.util.HashSet;

public class TeamServiceImpl implements TeamService{

	private final ActiveObjects ao;
  private final UserAccessor userAccessor;

  public TeamServiceImpl(ActiveObjects ao, UserAccessor userAccessor) {
    this.ao = ao;
    this.userAccessor = userAccessor;
  }

	@Override
	public Team add(String name) {
		Team team = ao.create(Team.class);
		team.setTeamName(name);
		team.save();

		return team;
	}

	@Override
	public List<Team> all() {
		return newArrayList(ao.find(Team.class, Query.select().order("TEAM_NAME ASC")));
	}

  @Override
  public Team getTeamByID(int id) {
    Team[] found = ao.find(Team.class, "ID = ?", id);

    if (found.length > 1) {
      throw new NotAuthorizedException("Multiple Teams with the same ID");
    }

		return (found.length > 0)? found[0] : null;
  }

  @Override
  public Team getTeamByName(String name) {		
    Team[] found = ao.find(Team.class, "TEAM_NAME = ?", name);

    if (found.length > 1) {
      throw new NotAuthorizedException("Multiple Teams with the same Name");
    }

		return (found.length > 0)? found[0] : null;
	}
    
  @Override
  public Set<Team> getTeamsOfUser(String userName) {
    
    Set<Team> teams = new HashSet<Team>();
    
    for(String groupName : userAccessor.getGroupNamesForUserName(userName)) {
      String teamName = groupName.split("-")[0];
      Team team = getTeamByName(teamName);
      if(team != null) {
        teams.add(team);
      }
    }
    
    return teams;
  }

  @Override
  public Set<Team> getCoordinatorTeamsOfUser(String userName) {
    Set<Team> teams = new HashSet<Team>();
    for(String groupName : userAccessor.getGroupNamesForUserName(userName)) {
      String[] pieces = groupName.split("-");
      
      String teamName = pieces[0];
      String roleName = (pieces.length > 1) ? pieces[1].toLowerCase() : "";
      
      if (!roleName.equalsIgnoreCase("administrators") 
          && !roleName.equalsIgnoreCase("coordinators")) {
        continue; 
      }
      
      Team team = getTeamByName(teamName);
      if(team != null) {
        teams.add(team);
      }
    }
    
    return teams;
  }
}
