package org.catrobat.confluence.activeobjects;

import net.java.ao.Entity;
import net.java.ao.ManyToMany;
import net.java.ao.OneToMany;

/**
 *
 * @author chri
 */
public interface Project extends Entity {

  String getProjectKey();
  void setProjectKey(String key);

  @ManyToMany(value=CategoryToProject.class, through="getCategory", reverse = "getProject")
  Category[] getCategories();

  @OneToMany(reverse = "getProject")
  TimesheetEntry[] getEntries();

}
