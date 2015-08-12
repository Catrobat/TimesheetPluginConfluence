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
    final JsonCategory other = (JsonCategory) obj;
    if (this.categoryID != other.categoryID) {
      return false;
    }
    if ((this.categoryName == null) ? (other.categoryName != null) : !this.categoryName.equals(other.categoryName)) {
      return false;
    }
    return true;
  }
}
