package org.catrobat.confluence.activeobjects;

import net.java.ao.Entity;
import net.java.ao.ManyToMany;
import net.java.ao.OneToMany;

public interface Category extends Entity {

  String getName();
  void setName(String name);

  @ManyToMany(value = CategoryToTeam.class, through = "getTeam", reverse = "getCategory")
  Team[] getTeams();

	@OneToMany(reverse = "getCategory")
	TimesheetEntry[] getEntries();
}
