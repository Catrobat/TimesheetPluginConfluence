package org.catrobat.confluence.activeobjects;

import net.java.ao.Entity;

public interface CategoryToTeam extends Entity {

  Category getCategory();
  void setCategory(Category category);

  Team getTeam();
  void setTeam(Team team);
}
