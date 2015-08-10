package org.catrobat.confluence.services;

import com.atlassian.activeobjects.tx.Transactional;
import java.util.List;

import javax.annotation.Nullable;
import org.catrobat.confluence.activeobjects.Category;

@Transactional
public interface CategoryService
{
	@Nullable
  Category getCategoryByID(int id);

  public List<Category> all();

  Category add(String name);
  
}