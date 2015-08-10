package org.catrobat.confluence.activeobjects;

import net.java.ao.Entity;
import net.java.ao.ManyToMany;
import net.java.ao.OneToMany;

public interface Team extends Entity {

  String getTeamName();
  void setTeamName(String name);

  @ManyToMany(value=CategoryToTeam.class, through="getCategory", reverse = "getTeam")
  Category[] getCategories();

  @OneToMany(reverse = "getTeam")
  TimesheetEntry[] getEntries();

}
