/*
 * Copyright 2015 Atlassian.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ut.org.catrobat.confluence.services.imp;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.EntityManager;
import org.catrobat.confluence.services.TimesheetEntryService;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.atlassian.activeobjects.test.TestActiveObjects;
import java.util.Date;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.jdbc.DatabaseUpdater;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.catrobat.confluence.activeobjects.Category;
import org.catrobat.confluence.activeobjects.Project;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;
import org.catrobat.confluence.services.imp.TimesheetEntryServiceImpl;

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(TimesheetEntryServiceImplTest.MyDatabaseUpdater.class)

public class TimesheetEntryServiceImplTest {

  private EntityManager entityManager;
	private TimesheetEntryService service;
  private ActiveObjects ao;

	@Before
	public void setUp() throws Exception
	{
		assertNotNull(entityManager);
		ao = new TestActiveObjects(entityManager);
		service = new TimesheetEntryServiceImpl(ao);
	}

	public static class MyDatabaseUpdater implements DatabaseUpdater {

		@Override
		public void update(EntityManager em) throws Exception {
			em.migrate(Timesheet.class);
			em.migrate(Category.class);
			em.migrate(Project.class);
			em.migrate(TimesheetEntry.class);
		}
	}

	@Test
	public void testAdd() throws Exception
	{
		//Arrange
		long oneHourInMS   = 60 * 60 * 1000;
		Timesheet sheet    = ao.create(Timesheet.class);
		Category  category = ao.create(Category.class);
		Project   project  = ao.create(Project.class);
		Date      begin    = new Date();
		Date      end      = new Date(begin.getTime() + oneHourInMS);
		String    desc     = "Debugged this thingy...";
		long			pause		 = 0;

		//Act
		service.add(sheet, begin, end, category, desc, pause, project);
		TimesheetEntry[] entries = ao.find(TimesheetEntry.class);

		//Assert
		assertEquals(1,        entries.length);
		assertEquals(sheet,    entries[0].getTimeSheet());
		assertEquals(category, entries[0].getCategory());
		assertEquals(project,  entries[0].getProject());
		assertEquals(begin,    entries[0].getBeginDate());
		assertEquals(end,      entries[0].getEndDate());
		assertEquals(desc,     entries[0].getDescription());
		assertEquals(pause,    entries[0].getPause());
	}
}


