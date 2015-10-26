package ut.org.catrobat.confluence.services.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.EntityManager;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.atlassian.activeobjects.test.TestActiveObjects;
import java.util.List;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.jdbc.DatabaseUpdater;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.services.TimesheetService;
import org.catrobat.confluence.services.impl.TimesheetServiceImpl;

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(TimesheetServiceImplTest.MyDatabaseUpdater.class)

public class TimesheetServiceImplTest {

	private EntityManager entityManager;
	private TimesheetService service;
	private ActiveObjects ao;

	final String userKey          = "USER_001";
	final int targetHoursPractice = 150;
	final int targetHoursTheory   = 0;
	final	String lecture          = "Mobile Applications (705.881)";

	@Before
	public void setUp() throws Exception
	{
		assertNotNull(entityManager);
		ao = new TestActiveObjects(entityManager);
		service = new TimesheetServiceImpl(ao);
	}

	public static class MyDatabaseUpdater implements DatabaseUpdater {

		@Override
		public void update(EntityManager em) throws Exception {
			em.migrate(Timesheet.class);

			Timesheet sheet = em.create(Timesheet.class);
			sheet.setUserKey("USER_000");
			sheet.save();
		}
	}

	@Test
	public void testAdd() throws Exception
	{
		//Act
		service.add(userKey, targetHoursPractice, targetHoursTheory, lecture);
		Timesheet[] entries = ao.find(Timesheet.class, "USER_KEY = ?", userKey);

		//Assert
		assertEquals(1,                   entries.length);
		assertEquals(userKey,             entries[0].getUserKey());
		assertEquals(targetHoursPractice, entries[0].getTargetHoursPractice());
		assertEquals(targetHoursTheory,   entries[0].getTargetHoursTheory());
	}

	@Test
	public void testAll() throws Exception
	{
		//Arange
		Timesheet sheet = ao.create(Timesheet.class);
		sheet.setUserKey(userKey);
		sheet.setTargetHoursPractice(targetHoursPractice);
		sheet.setTargetHoursTheory(targetHoursTheory);
		sheet.setLecture(lecture);
		sheet.setIsActive(true);
		sheet.save();
		ao.flushAll();

		//Act
		List<Timesheet> timesheets = service.all();

		//Assert
		assertEquals(timesheets.size(), 2);
	}

	@Test
	public void testGetTimesheetByUser() throws Exception
	{
		//Arange
		Timesheet sheet = ao.create(Timesheet.class);
		sheet.setUserKey(userKey);
		sheet.setTargetHoursPractice(targetHoursPractice);
		sheet.setTargetHoursTheory(targetHoursTheory);
		sheet.setLecture(lecture);
		sheet.setIsActive(true);
		sheet.save();
		ao.flushAll();

		//Act
		Timesheet sheet0 = service.getTimesheetByUser("USER_000");

		//Assert
		assertNotNull(sheet0);
		assertEquals(sheet0.getUserKey(), "USER_000");

		//Act
		Timesheet sheet1 = service.getTimesheetByUser("USER_001");

		//Assert
		assertNotNull(sheet1);
		assertEquals(sheet1.getUserKey(), "USER_001");
	}

	@Test
	public void testGetTimesheetByUserNotFound() throws Exception
	{
		//Act
		Timesheet missingSheet = service.getTimesheetByUser("USER_DOES_NOT_EXIST");

		//Assert
		assertNull(missingSheet);
	}

	@Test
	public void testGetTimesheetByID() throws Exception
	{
		//Act
		Timesheet[] sheets = ao.find(Timesheet.class, "USER_KEY = ?", "USER_000");
		Timesheet refSheet = sheets[0];
		Timesheet checkSheet = service.getTimesheetByID(refSheet.getID());

		//Assert
		assertEquals(refSheet, checkSheet);
	}

	@Test
	public void testGetTimesheetByMissingID() throws Exception
	{
		//Act
		Timesheet sheet = service.getTimesheetByID(12345);

		//Assert
		assertNull(sheet);
	}
}
