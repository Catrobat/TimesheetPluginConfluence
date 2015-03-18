package org.catrobat.confluence.activeobjects;

import net.java.ao.Entity;

public interface GroupToTeam extends Entity {

	Group getGroup();
	void setGroup(Group group);

	Team getTeam();
	void setTeam(Team team);

}
