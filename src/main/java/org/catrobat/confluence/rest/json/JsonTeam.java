package org.catrobat.confluence.rest.json;

import org.catrobat.confluence.activeobjects.AdminHelperConfigService;
import org.catrobat.confluence.activeobjects.GithubTeam;
import org.catrobat.confluence.activeobjects.Team;
import org.catrobat.confluence.activeobjects.TeamToGroup;
import org.catrobat.confluence.helper.GithubHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("unused")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JsonTeam {
  
  @XmlElement
	private int teamID;
  @XmlElement
	private String teamName;
  @XmlElement
	private int[] teamCategories;
  @XmlElement
  private List<String> githubTeams;
  @XmlElement
  private List<String> coordinatorGroups;
  @XmlElement
  private List<String> seniorGroups;
  @XmlElement
  private List<String> developerGroups;

  public JsonTeam(int teamID, String teamName, int[] teamCategories) {
    this.teamID = teamID;
    this.teamName = teamName;
    this.teamCategories = teamCategories;
  }

  public JsonTeam(String name) {
    this.teamName = name;
    githubTeams = new ArrayList<String>();
    coordinatorGroups = new ArrayList<String>();
    seniorGroups = new ArrayList<String>();
    developerGroups = new ArrayList<String>();
  }

  public JsonTeam(Team toCopy, AdminHelperConfigService configService) {
    this.teamName = toCopy.getTeamName();
    GithubHelper githubHelper = new GithubHelper(configService);

    this.githubTeams = new ArrayList<String>();
    for (GithubTeam githubTeam : toCopy.getGithubTeams()) {
      githubTeams.add(githubHelper.getTeamName(githubTeam.getGithubId()));
    }

    this.coordinatorGroups = configService.getGroupsForRole(this.teamName, TeamToGroup.Role.COORDINATOR);
    this.seniorGroups = configService.getGroupsForRole(this.teamName, TeamToGroup.Role.SENIOR);
    this.developerGroups = configService.getGroupsForRole(this.teamName, TeamToGroup.Role.DEVELOPER);
  }

  public int getTeamID() {
    return teamID;
  }

  public void setTeamID(int teamID) {
    this.teamID = teamID;
  }

  public String getTeamName() {
    return teamName;
  }

  public void setTeamName(String teamName) {
    this.teamName = teamName;
  }

  public int[] getTeamCategories() {
    return teamCategories;
  }

  public void setTeamCategories(int[] teamCategories) {
    this.teamCategories = teamCategories;
  }

  public List<String> getGithubTeams() {
    return githubTeams;
  }

  public void setGithubTeams(List<String> githubTeams) {
    this.githubTeams = githubTeams;
  }

  public List<String> getCoordinatorGroups() {
    return coordinatorGroups;
  }

  public void setCoordinatorGroups(List<String> coordinatorGroups) {
    this.coordinatorGroups = coordinatorGroups;
  }

  public List<String> getSeniorGroups() {
    return seniorGroups;
  }

  public void setSeniorGroups(List<String> seniorGroups) {
    this.seniorGroups = seniorGroups;
  }

  public List<String> getDeveloperGroups() {
    return developerGroups;
  }

  public void setDeveloperGroups(List<String> developerGroups) {
    this.developerGroups = developerGroups;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final JsonTeam other = (JsonTeam) obj;
    if (this.teamID != other.teamID) {
      return false;
    }
    if ((this.teamName == null) ? (other.teamName != null) : !this.teamName.equals(other.teamName)) {
      return false;
    }
    if (!Arrays.equals(this.teamCategories, other.teamCategories)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "JsonTeam{" +
            "teamID=" + teamID +
            ", teamName='" + teamName + '\'' +
            ", teamCategories=" + Arrays.toString(teamCategories) +
            '}';
  }
}
