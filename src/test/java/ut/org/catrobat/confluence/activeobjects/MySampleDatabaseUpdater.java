package ut.org.catrobat.confluence.activeobjects;

import java.text.SimpleDateFormat;
import net.java.ao.EntityManager;
import net.java.ao.test.jdbc.DatabaseUpdater;
import org.catrobat.confluence.activeobjects.Category;
import org.catrobat.confluence.activeobjects.CategoryToProject;
import org.catrobat.confluence.activeobjects.Project;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;

public class MySampleDatabaseUpdater implements DatabaseUpdater {

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
			entry1.setPauseMinutes(10);
			entry1.setDescription("Besprechung: Project Fetcher");
			entry1.save();

			TimesheetEntry entry2 = em.create(TimesheetEntry.class);
			entry2.setCategory(programmingCategory);
			entry2.setBeginDate(sdf.parse("02-01-2015 10:30"));
			entry2.setEndDate(  sdf.parse("02-01-2015 10:45"));
			entry2.setPauseMinutes(5);
			entry2.setTimeSheet(johSheet);
			entry2.setProject(catrobatProject);
			entry2.setDescription("Master Fixen");
			entry2.save();

		}
	}