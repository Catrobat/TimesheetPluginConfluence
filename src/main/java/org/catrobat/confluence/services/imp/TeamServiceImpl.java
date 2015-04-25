package org.catrobat.confluence.services.imp;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.confluence.user.UserAccessor;
import net.java.ao.Query;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.services.TeamService;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import java.util.Set;
import java.util.HashSet;
import javax.annotation.Nullable;

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
		return newArrayList(ao.find(Team.class, Query.select().order("\"TEAM_NAME\" ASC")));
	}

  @Override
  @Nullable
  public Team getTeamByID(int id) {
    Team[] found = ao.find(Team.class, "ID = ?", id);
		assert(found.length <= 1);
		return (found.length > 0)? found[0] : null;
  }

  @Override
  @Nullable
  public Team getTeamByName(String name) {		
    Team[] found = ao.find(Team.class, "TEAM_NAME = ?", name);
		assert(found.length <= 1);
		return (found.length > 0)? found[0] : null;
	}
    
  @Override
  public Team[] getTeamsOfUser(String userName) {
    
    Set<Team> teams = new HashSet<Team>();
    
    for(String groupName : userAccessor.getGroupNamesForUserName(userName)) {
      String teamName = groupName.split("-")[0].toLowerCase();
      Team team = getTeamByName(teamName);
      if(team != null) {
        teams.add(team);
      }
    }
    
    Team[] teamArray = new Team[teams.size()]; 
    
    return teams.toArray(teamArray);
  }
}
