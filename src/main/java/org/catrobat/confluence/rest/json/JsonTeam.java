package org.catrobat.confluence.rest.json;

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
}
