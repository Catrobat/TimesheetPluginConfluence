package org.catrobat.confluence.services;

import com.atlassian.activeobjects.tx.Transactional;
import org.catrobat.confluence.activeobjects.Team;
import javax.annotation.Nonnull;


import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

@Transactional
public interface TeamService {

	Team add(String name);

	List<Team> all();
  
	@Nullable
  Team getTeamByID(int id);
  
	@Nullable
  Team getTeamByName(String name);
  
  Set<Team> getTeamsOfUser(String userName);
  
  Set<Team> getCoordinatorTeamsOfUser(String userName);
  
}
