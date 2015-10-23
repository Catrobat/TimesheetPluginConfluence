package ut.org.catrobat.confluence.services.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import junit.framework.Assert;
import org.apache.wink.common.categories.Categories;
import org.catrobat.confluence.activeobjects.Category;
import org.catrobat.confluence.services.CategoryService;
import org.catrobat.confluence.services.impl.CategoryServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;

public class CategoryServiceImplTest {

  private CategoryServiceImpl categoryService;

  private ActiveObjects activeObjects;

  @Before
  public void setUp() throws Exception
  {
    activeObjects = Mockito.mock(ActiveObjects.class);

    categoryService = new CategoryServiceImpl(activeObjects);
  }

  @Test(expected = NullPointerException.class)
  public void testGetCategoryByIDNotOk() throws Exception
  {
    categoryService.getCategoryByID(1);
  }

  @Test(expected = NullPointerException.class)
  public void testAllNotOk() throws Exception
  {
    categoryService.all();
  }
}
