package org.catrobat.confluence.activeobjects;

import net.java.ao.Entity;

public interface CategoryToProject extends Entity {

  Category getCategory();
  void setCategory(Category category);

  Project getProject();
  void setProject(Project project);
}
