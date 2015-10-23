package ut.org.catrobat.confluence.services.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.user.UserManager;
import org.catrobat.confluence.services.CategoryService;
import org.catrobat.confluence.services.TeamService;
import org.catrobat.confluence.services.TimesheetService;
import org.catrobat.confluence.services.impl.DBFillerServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DBFillerServiceImplTest {

  private DBFillerServiceImpl dbFillerService;

  private ActiveObjects activeObjects;
  private CategoryService categoryService;
  private TeamService teamService;
  private TimesheetService sheetService;
  private UserManager userManager;

  @Before
  public void setUp() throws Exception {

    activeObjects = Mockito.mock(ActiveObjects.class);
    categoryService = Mockito.mock(CategoryService.class);
    teamService = Mockito.mock(TeamService.class);
    sheetService = Mockito.mock(TimesheetService.class);
    userManager = Mockito.mock(UserManager.class);

    dbFillerService = new DBFillerServiceImpl(activeObjects, categoryService, teamService, sheetService, userManager);

  }

  @Test(expected = NullPointerException.class)
  public void testInsertDefaultData() throws Exception
  {
    dbFillerService.insertDefaultData();
  }

  @Test(expected = NullPointerException.class)
  public void testPrintDB() throws Exception
  {
    dbFillerService.printDBStatus();
  }
}
