package org.catrobat.confluence.services;

import com.atlassian.activeobjects.tx.Transactional;

import javax.annotation.Nullable;
import org.catrobat.confluence.activeobjects.Category;

@Transactional
public interface CategoryService
{
	@Nullable
  Category getCategoryByID(int id);

}