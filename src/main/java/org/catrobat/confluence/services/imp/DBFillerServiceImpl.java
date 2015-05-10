package org.catrobat.confluence.services.imp;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import org.catrobat.confluence.activeobjects.Category;
import org.catrobat.confluence.activeobjects.CategoryToTeam;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.activeobjects.Timesheet;
import org.catrobat.confluence.activeobjects.TimesheetEntry;
import org.catrobat.confluence.services.CategoryService;
import org.catrobat.confluence.services.DBFillerService;
import org.catrobat.confluence.services.TeamService;
import org.catrobat.confluence.services.TimesheetService;

/**
 * @author chri
 */
public class DBFillerServiceImpl implements DBFillerService{

  private final ActiveObjects ao; 
  private final CategoryService cs;
  private final TeamService ts;
  private final TimesheetService tss;
  private final UserManager um;

  public DBFillerServiceImpl(ActiveObjects ao, CategoryService cs, TeamService ts, TimesheetService tss, UserManager um) {
    this.ao = ao;
    this.cs = cs;
    this.ts = ts;
    this.tss = tss;
    this.um = um;
  }

  @Override
  public void cleanDB() {
    ao.deleteWithSQL(TimesheetEntry.class, "1=?", "1");
    ao.deleteWithSQL(Timesheet.class, "1=?", "1");
    ao.deleteWithSQL(CategoryToTeam.class, "1=?", "1");
    ao.deleteWithSQL(Team.class, "1=?", "1");
    ao.deleteWithSQL(Category.class, "1=?", "1");
  }

  @Override
  public void insertDefaultData() {
    Category c1 = cs.add("Theory (MT)");
    Category c2 = cs.add("Meeting");
    Category c3 = cs.add("Pair Programming");
    Category c4 = cs.add("Programming");
    Category c5 = cs.add("Research");
    Category c6 = cs.add("Other");
    Category c7 = cs.add("Planning Game");
    Category c8 = cs.add("Refactoring");
    
    Team t1 = ts.add("Catroid");
    Team t2 = ts.add("HTML5");

    //categories of team1
    CategoryToTeam c2t1 = ao.create(CategoryToTeam.class);
    c2t1.setTeam(t1);
    c2t1.setCategory(c1);
    c2t1.save();
    
    CategoryToTeam c2t2 = ao.create(CategoryToTeam.class);
    c2t2.setTeam(t1);
    c2t2.setCategory(c2);
    c2t2.save();

    CategoryToTeam c2t3 = ao.create(CategoryToTeam.class);
    c2t3.setTeam(t1);
    c2t3.setCategory(c3);
    c2t3.save();
        
    CategoryToTeam c2t4 = ao.create(CategoryToTeam.class);
    c2t4.setTeam(t1);
    c2t4.setCategory(c4);
    c2t4.save();

    CategoryToTeam c2t5 = ao.create(CategoryToTeam.class);
    c2t5.setTeam(t1);
    c2t5.setCategory(c5);
    c2t5.save();
    
    //categories of team2
    CategoryToTeam c2t6 = ao.create(CategoryToTeam.class);
    c2t6.setTeam(t2);
    c2t6.setCategory(c5);
    c2t6.save();
    CategoryToTeam c2t7 = ao.create(CategoryToTeam.class);
    c2t7.setTeam(t2);
    c2t7.setCategory(c6);
    c2t7.save();
    CategoryToTeam c2t8 = ao.create(CategoryToTeam.class);
    c2t8.setTeam(t2);
    c2t8.setCategory(c7);
    c2t8.save();
    CategoryToTeam c2t9 = ao.create(CategoryToTeam.class);
    c2t9.setTeam(t2);
    c2t9.setCategory(c8);
    c2t9.save();
    
    UserKey key = um.getRemoteUserKey();
    if(key != null) {
      String userKey = key.getStringValue();
      Timesheet sheet = tss.add(userKey, 150, 0, "Project Softwareentwicklung");
      System.out.println("user key was " + userKey);
      System.out.println("created timesheet: " + sheet.getID());
    } 
  }
  
  @Override
  public void printDBStatus() {
    System.out.println("  Timesheet:      " + ao.find(Timesheet.class).length);
    System.out.println("  TimesheetEntry: " + ao.find(TimesheetEntry.class).length);
    System.out.println("  Team:           " + ao.find(Team.class).length);
    System.out.println("  Category:       " + ao.find(Category.class).length);
    System.out.println("  CategoryToTeam: " + ao.find(CategoryToTeam.class).length);
  }
}
