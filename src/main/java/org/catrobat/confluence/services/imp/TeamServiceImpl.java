package org.catrobat.confluence.services.imp;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.Query;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.services.TeamService;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class TeamServiceImpl implements TeamService{

	private final ActiveObjects ao;

	public TeamServiceImpl(ActiveObjects ao) {
		this.ao = ao;
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
  public Team getTeamByID(int id) {
    Team[] found = ao.find(Team.class, "ID = ?", id);
		assert(found.length <= 1);
		return (found.length > 0)? found[0] : null;
  }
}
