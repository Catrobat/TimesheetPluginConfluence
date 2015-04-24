package org.catrobat.confluence.services;

import com.atlassian.activeobjects.tx.Transactional;
import org.catrobat.confluence.activeobjects.Team;

import java.util.List;

@Transactional
public interface TeamService {

	Team add(String name);

	List<Team> all();
  
  Team getTeamByID(int id);
}
