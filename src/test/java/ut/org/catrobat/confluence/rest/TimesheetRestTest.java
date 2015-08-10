package ut.org.catrobat.confluence.rest;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import junit.framework.Assert;
import org.catrobat.confluence.activeobjects.Category;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.rest.TimesheetRest;
import org.catrobat.confluence.rest.json.JsonTeam;
import org.catrobat.confluence.services.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Adrian Schnedlitz on 10.08.2015.
 */
public class TimesheetRestTest {

  private TimesheetEntryService entryService;
  private TimesheetService sheetService;
  private CategoryService categoryService;
  private TeamService teamService;
  private UserManager userManager;
  private PermissionService permissionService;
  private DBFillerService dbfiller;
  private TimesheetRest timesheetRest;
  private Response response;
  private HttpServletRequest request;
  private UserProfile userProfile;

  @Before
  public void setUp() throws Exception
  {
    entryService = Mockito.mock(TimesheetEntryService.class);
    sheetService = Mockito.mock(TimesheetService.class);
    categoryService = Mockito.mock(CategoryService.class);
    teamService = Mockito.mock(TeamService.class);
    userManager = Mockito.mock(UserManager.class);
    permissionService = Mockito.mock(PermissionService.class);
    dbfiller = Mockito.mock(DBFillerService.class);
    request = Mockito.mock(HttpServletRequest.class);
    userProfile = Mockito.mock(UserProfile.class);

    timesheetRest = new TimesheetRest(entryService, sheetService, categoryService, userManager, teamService, permissionService, dbfiller);

    Mockito.when(userManager.getRemoteUser()).thenReturn(userProfile);
    Mockito.when(userProfile.getUsername()).thenReturn("testUser");
  }

  @Test
  public void testHelloWorld() throws Exception
  {
    response = timesheetRest.doHelloWorld();
    Assert.assertEquals("Hello World", response.getEntity().toString());
  }

  @Test
  public void testGetTeams() throws Exception
  {
    List<JsonTeam> expectedTeams = new LinkedList<JsonTeam>();
    expectedTeams.add(new JsonTeam(1, "Catroid", new int[0]));
    expectedTeams.add(new JsonTeam(2, "IRC", new int[0]));

    Team team1 = Mockito.mock(Team.class);
    Mockito.when(team1.getID()).thenReturn(2);
    Mockito.when(team1.getTeamName()).thenReturn("IRC");
    Mockito.when(team1.getCategories()).thenReturn(new Category[0]);

    Team team2 = Mockito.mock(Team.class);
    Mockito.when(team2.getID()).thenReturn(1);
    Mockito.when(team2.getTeamName()).thenReturn("Catroid");
    Mockito.when(team2.getCategories()).thenReturn(new Category[0]);

    Set<Team> teams = new HashSet<Team>();
    teams.add(team1);
    teams.add(team2);

    Mockito.when(teamService.getTeamsOfUser("testUser")).thenReturn(teams);

    response = timesheetRest.getTeams(request);
    Assert.assertEquals(expectedTeams, response.getEntity());
  }

  @Test
  public void testGetCategories() throws Exception
  {

  }
}
