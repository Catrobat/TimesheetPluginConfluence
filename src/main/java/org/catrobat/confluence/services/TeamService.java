package org.catrobat.confluence.services;

import com.atlassian.activeobjects.tx.Transactional;
import org.catrobat.confluence.activeobjects.Team;
import javax.annotation.Nonnull;


import java.util.List;
import java.util.Set;

@Transactional
public interface TeamService {

	Team add(String name);

	List<Team> all();
  
  Team getTeamByID(int id);
  
  Team getTeamByName(String name);
  
  @Nonnull
  Set<Team> getTeamsOfUser(String userName);
  
  @Nonnull
  Set<Team> getCoordinatorTeamsOfUser(String userName);
  
}
