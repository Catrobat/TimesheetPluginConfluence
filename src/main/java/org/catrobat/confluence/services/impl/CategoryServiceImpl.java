package org.catrobat.confluence.services.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import static com.google.common.collect.Lists.newArrayList;
import java.util.List;

import com.atlassian.confluence.core.service.NotAuthorizedException;
import net.java.ao.Query;
import org.catrobat.confluence.activeobjects.Category;
import org.catrobat.confluence.services.CategoryService;

public class CategoryServiceImpl implements CategoryService {

	private final ActiveObjects ao;

	public CategoryServiceImpl(ActiveObjects ao) {
		this.ao = ao;
	}

	@Override
  public Category getCategoryByID(int id) {
		Category[] found = ao.find(Category.class, "ID = ?", id);
		assert(found.length <= 1);
		return (found.length > 0)? found[0] : null;
  }

  @Override
  public List<Category> all() {
    return newArrayList(ao.find(Category.class, Query.select().order("NAME ASC")));
  }

  @Override
  public Category add(String name) {
    Category category = ao.create(Category.class);
    category.setName(name);
    category.save();
    return category;
  }

  @Override
  public boolean removeCategory(String name) {
    Category[] found = ao.find(Category.class, "NAME = ?", name);

    if (found.length > 1) {
      throw new NotAuthorizedException("Multiple Categories with the same Name");
    }

    ao.delete(found);
    return true;
  }
  
}
