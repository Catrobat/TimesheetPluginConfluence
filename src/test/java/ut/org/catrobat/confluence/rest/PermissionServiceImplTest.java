package ut.org.catrobat.confluence.rest;

import net.java.ao.EntityManager;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import java.util.HashSet;
import java.util.Set;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.jdbc.DatabaseUpdater;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.services.imp.PermissionServiceImpl;
import org.catrobat.confluence.services.TeamService;
import org.junit.Rule;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.catrobat.confluence.activeobjects.Timesheet;

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(PermissionServiceImplTest.MyDatabaseUpdater.class)

public class PermissionServiceImplTest {

  private PermissionServiceImpl permissionService; 
  private TeamService teamService;
  private UserManager userManager;
  private EntityManager entityManager;
	private static Team catroid, html5, drone;
  private UserProfile coord, owner, eve, admin;
  private Timesheet sheet;
  
  @Rule public org.mockito.junit.MockitoRule mockitoRule = MockitoJUnit.rule();
  @Before
	public void setUp() throws Exception
	{
    teamService = Mockito.mock(TeamService.class);
    userManager = Mockito.mock(UserManager.class);
    
		assertNotNull(entityManager);
				
    permissionService = new PermissionServiceImpl(userManager, teamService);
    
     //arrange
    coord = Mockito.mock(UserProfile.class);
    owner = Mockito.mock(UserProfile.class);
    eve   = Mockito.mock(UserProfile.class);
    admin = Mockito.mock(UserProfile.class);
    sheet = Mockito.mock(Timesheet.class);
    
    UserKey coord_key = new UserKey("coord_key");
    UserKey owner_key = new UserKey("owner_key");
    UserKey eve_key   = new UserKey("eve_key");
    UserKey admin_key = new UserKey("admin_key");
    
    Mockito.when(sheet.getUserKey()).thenReturn("owner_key");
        
    Mockito.when(coord.getUserKey()).thenReturn(coord_key);
    Mockito.when(owner.getUserKey()).thenReturn(owner_key);
    Mockito.when(eve.getUserKey())  .thenReturn(eve_key);
    Mockito.when(admin.getUserKey()).thenReturn(admin_key);
    
    Mockito.when(coord.getUsername()).thenReturn("coord");
    Mockito.when(owner.getUsername()).thenReturn("owner");
    Mockito.when(eve.getUsername())  .thenReturn("eve");
    Mockito.when(admin.getUsername()).thenReturn("admin");

    Mockito.when(userManager.isAdmin(owner_key)).thenReturn(false);
    Mockito.when(userManager.isAdmin(coord_key)).thenReturn(false);
    Mockito.when(userManager.isAdmin(eve_key))  .thenReturn(false);
    Mockito.when(userManager.isAdmin(admin_key)).thenReturn(true);
    
    Mockito.when(userManager.getUserProfile(owner_key.getStringValue())).thenReturn(owner);
    Mockito.when(userManager.getUserProfile(admin_key.getStringValue())).thenReturn(admin);
    Mockito.when(userManager.getUserProfile(eve_key.getStringValue()))  .thenReturn(eve);
    Mockito.when(userManager.getUserProfile(coord_key.getStringValue())).thenReturn(coord);
    
    Set<Team> owner_teams  = new HashSet<Team>();
    Set<Team> eve_teams    = new HashSet<Team>();
    Set<Team> coord_cteams = new HashSet<Team>();
    Set<Team> no_teams     = new HashSet<Team>();

    owner_teams.add(html5);
    owner_teams.add(drone);
    eve_teams.add(catroid);
    coord_cteams.add(html5);
    
    Mockito.when(teamService.getTeamsOfUser("owner")).thenReturn(owner_teams);
    Mockito.when(teamService.getTeamsOfUser("eve"))  .thenReturn(eve_teams);
    Mockito.when(teamService.getCoordinatorTeamsOfUser("coord")).thenReturn(coord_cteams);

    Mockito.when(teamService.getTeamsOfUser("coord")).thenReturn(no_teams);
    Mockito.when(teamService.getTeamsOfUser("admin")).thenReturn(no_teams);
    Mockito.when(teamService.getCoordinatorTeamsOfUser("owner")).thenReturn(no_teams);
    Mockito.when(teamService.getCoordinatorTeamsOfUser("eve")).thenReturn(no_teams);
    Mockito.when(teamService.getCoordinatorTeamsOfUser("admin")).thenReturn(no_teams);
    
	}

	public static class MyDatabaseUpdater implements DatabaseUpdater {

		@Override
		public void update(EntityManager em) throws Exception {
			em.migrate(Team.class);
      catroid = em.create(Team.class);
      catroid.setTeamName("catroid");
      catroid.save();
      
      html5 = em.create(Team.class);
      html5.setTeamName("html5");
      html5.save();
      
      drone = em.create(Team.class);
      drone.setTeamName("drone");
      drone.save();
		}
	}
	
	@Test
	public void testOwnerCanEditTimesheet() throws Exception
	{
    assertTrue(permissionService.userCanEditTimesheet(owner, sheet));
	}
  
	@Test
	public void testCoordinatorCanEditTimesheet() throws Exception
	{
    assertTrue(permissionService.userCanEditTimesheet(coord, sheet));
	}
  
	@Test
	public void testAdminCanEditTimesheet() throws Exception
	{
    assertTrue(permissionService.userCanEditTimesheet(admin, sheet));
	}
  
	@Test
	public void testEveCantEditTimesheet() throws Exception
	{
    assertFalse(permissionService.userCanEditTimesheet(eve, sheet));
	}
	
}
