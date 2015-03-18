package org.catrobat.confluence.activeobjects;

import net.java.ao.Entity;
import net.java.ao.ManyToMany;

public interface Group extends Entity {

	String getName();
	void setName(String name);

	@ManyToMany(value = GroupToTeam.class, through = "getTeam", reverse = "getGroup")
	Team[] getTeams();
}

