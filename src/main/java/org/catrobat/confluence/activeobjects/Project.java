/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

  @ManyToMany(value=CategoryToProject.class)
  Category[] getCategories();

  @OneToMany
  TimesheetEntry[] getEntries();

}
