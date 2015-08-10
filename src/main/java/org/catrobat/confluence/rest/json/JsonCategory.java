package org.catrobat.confluence.rest.json;

import javax.xml.bind.annotation.XmlElement;

public class JsonCategory {
  @XmlElement
	private int categoryID;
  @XmlElement
	private String categoryName;

  public JsonCategory(int categoryID, String categoryName) {
    this.categoryID = categoryID;
    this.categoryName = categoryName;
  }

  public int getCategoryID() {
    return categoryID;
  }

  public void setCategoryID(int categoryID) {
    this.categoryID = categoryID;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }
}
