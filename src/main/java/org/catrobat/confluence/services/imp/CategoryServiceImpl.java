package org.catrobat.confluence.services.imp;

import com.atlassian.activeobjects.external.ActiveObjects;
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
}
