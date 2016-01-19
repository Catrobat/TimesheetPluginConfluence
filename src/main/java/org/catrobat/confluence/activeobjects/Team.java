package org.catrobat.confluence.activeobjects;

import net.java.ao.Entity;
import net.java.ao.ManyToMany;
import net.java.ao.OneToMany;

public interface Team extends Entity {

  String getTeamName();

  void setTeamName(String name);

  AdminHelperConfig getConfiguration();

  void setConfiguration(AdminHelperConfig configuration);

  @ManyToMany(value = TeamToGroup.class, reverse = "getTeam", through = "getGroup")
  Group[] getGroups();

  @ManyToMany(value=CategoryToTeam.class, through="getCategory", reverse = "getTeam")
  Category[] getCategories();

  @OneToMany(reverse = "getTeam")
  TimesheetEntry[] getEntries();

}
