package ut.org.catrobat.confluence.activeobjects;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.EntityManager;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.atlassian.activeobjects.test.TestActiveObjects;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.catrobat.confluence.activeobjects.Category;
import org.catrobat.confluence.activeobjects.Project;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(MySampleDatabaseUpdater.class)

/**
 * This Test Suite verifies that active object entities are correctly mapped
 * to each other
 */
public class ActiveObjectsTest {

  private EntityManager entityManager;
  private ActiveObjects ao;

	@Before
	public void setUp() throws Exception
	{
		assertNotNull(entityManager);
		ao = new TestActiveObjects(entityManager);
	}

	@Test
	public void testProjectToCategoryMapping() throws Exception
	{
		Project[] projects = ao.find(Project.class, "PROJECT_KEY = ?", "CATROBAT");

		assertEquals(projects.length, 1);

		Project catrobatProject = projects[0];

		assertEquals(catrobatProject.getEntries().length, 1);
		assertEquals(catrobatProject.getCategories().length, 2);

	}

	@Test
	public void testSheetToProjectMapping() throws Exception
	{
		Timesheet[] sheets = ao.find(Timesheet.class, "USER_KEY = ?", "chris");

		assertEquals(sheets.length, 1);

		Timesheet chrisSheet = sheets[0];
		TimesheetEntry[] entries = chrisSheet.getEntries();

		assertEquals(entries.length, 1);

		TimesheetEntry entry1 = entries[0];

		assertEquals(entry1.getDescription(), "Besprechung: Project Fetcher");

		Project scratchProject = entry1.getProject();

		assertEquals(scratchProject.getProjectKey(), "SCRATCH");

		Category meetingCategory = entry1.getCategory();

		assertEquals(meetingCategory.getName(), "Meeting");

		Project[] projectsOfMeeting = meetingCategory.getProjects();

		assertEquals(projectsOfMeeting.length, 2);
	}

	@Test
	public void testDateQueries() throws Exception {
		//get all time sheet entries where duration < 20 minutes
		SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
		tf.setTimeZone(TimeZone.getTimeZone("UTC"));

		TimesheetEntry[] entries = ao.find(TimesheetEntry.class, 
						"DATEDIFF('minute', BEGIN_DATE, END_DATE) - PAUSE_MINUTES < 20" );

		//assert
		assertEquals(entries.length, 1);
		assertEquals(entries[0].getDescription(), "Master Fixen");
	}

}
