/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.catrobat.confluence.activeobjects;

import net.java.ao.Entity;

public interface CategoryToProject extends Entity {

  Category getCategory();
  void setCategory(Category category);

  Project getProject();
  void setProject(Project project);
}
