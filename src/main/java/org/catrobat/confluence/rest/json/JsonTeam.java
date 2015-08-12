package org.catrobat.confluence.rest.json;

import java.util.Arrays;
import javax.xml.bind.annotation.XmlElement;

public class JsonTeam {
  
  @XmlElement
	private int teamID;
  @XmlElement
	private String teamName;
  @XmlElement
	private int[] teamCategories;

  public JsonTeam(int teamID, String teamName, int[] teamCategories) {
    this.teamID = teamID;
    this.teamName = teamName;
    this.teamCategories = teamCategories;
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
