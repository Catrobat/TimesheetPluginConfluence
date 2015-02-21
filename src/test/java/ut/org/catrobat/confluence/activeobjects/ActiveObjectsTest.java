package ut.org.catrobat.confluence.activeobjects;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.EntityManager;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.atlassian.activeobjects.test.TestActiveObjects;
import java.text.SimpleDateFormat;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.jdbc.DatabaseUpdater;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.catrobat.confluence.activeobjects.Category;
import org.catrobat.confluence.activeobjects.CategoryToProject;
import org.catrobat.confluence.activeobjects.Project;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(ActiveObjectsTest.MyDatabaseUpdater.class)

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

	public static class MyDatabaseUpdater implements DatabaseUpdater {

		@Override
		public void update(EntityManager em) throws Exception {
			em.migrate(Timesheet.class);
			em.migrate(Category.class);
			em.migrate(CategoryToProject.class);
			em.migrate(Project.class);
			em.migrate(TimesheetEntry.class);

			Timesheet chrisSheet = em.create(Timesheet.class);
			chrisSheet.setUserKey("chris");
			chrisSheet.save();
			
			Timesheet johSheet = em.create(Timesheet.class);
			johSheet.setUserKey("joh");
			johSheet.save();

			Project scratchProject = em.create(Project.class);
			scratchProject.setProjectKey("SCRATCH");
			scratchProject.save();

			Project confluenceProject = em.create(Project.class);
			confluenceProject.setProjectKey("CONFLUENCE");
			confluenceProject.save();

			Project catrobatProject = em.create(Project.class);
			catrobatProject.setProjectKey("CATROBAT");
			catrobatProject.save();

			Category meetingCategory = em.create(Category.class);
			meetingCategory.setName("Meeting");
			meetingCategory.save();

			Category programmingCategory = em.create(Category.class);
			programmingCategory.setName("Programming");
			programmingCategory.save();

			CategoryToProject catHasMee = em.create(CategoryToProject.class);
			catHasMee.setProject(catrobatProject);
			catHasMee.setCategory(meetingCategory);
			catHasMee.save();

			CategoryToProject catHasPro = em.create(CategoryToProject.class);
			catHasPro.setProject(catrobatProject);
			catHasPro.setCategory(programmingCategory);
			catHasPro.save();

			CategoryToProject conHasPro = em.create(CategoryToProject.class);
			conHasPro.setProject(confluenceProject);
			conHasPro.setCategory(programmingCategory);
			conHasPro.save();

			CategoryToProject scrHasMee = em.create(CategoryToProject.class);
			scrHasMee.setProject(scratchProject);
			scrHasMee.setCategory(meetingCategory);
			scrHasMee.save();

			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy hh:mm");

			TimesheetEntry entry1 = em.create(TimesheetEntry.class);
			entry1.setCategory(meetingCategory);
			entry1.setBeginDate(sdf.parse("01-01-2015 09:00"));
			entry1.setEndDate(  sdf.parse("01-01-2015 10:00"));
			entry1.setTimeSheet(chrisSheet);
			entry1.setProject(scratchProject);
			entry1.setDescription("Besprechung: Project Fetcher");
			entry1.save();

			TimesheetEntry entry2 = em.create(TimesheetEntry.class);
			entry2.setCategory(programmingCategory);
			entry2.setBeginDate(sdf.parse("02-01-2015 10:30"));
			entry2.setEndDate(  sdf.parse("02-01-2015 11:45"));
			entry2.setTimeSheet(johSheet);
			entry2.setProject(catrobatProject);
			entry2.setDescription("Master Fixen");
			entry2.save();

		}
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
}
