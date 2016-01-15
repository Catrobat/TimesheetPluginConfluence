/*
 * Copyright 2015 Christof Rabensteiner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.catrobat.confluence.rest.json;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("unused")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class JsonTimesheet {

	@XmlElement
	private int timesheetID;
	@XmlElement
	private String lectures;
	@XmlElement
	private int targetHourPractice;
	@XmlElement
	private int targetHourTheory;
	@XmlElement
	private boolean isActive;

  public JsonTimesheet(int timesheetID, int targetHourPractice, int targetHourTheory, String lectures, boolean isActive) {
    this.timesheetID = timesheetID;
    this.targetHourPractice = targetHourPractice;
    this.targetHourTheory = targetHourTheory;
    this.lectures = lectures;
    this.isActive = isActive;
  }

  public JsonTimesheet() {
  }

  public int getTimesheetID() {
    return timesheetID;
  }

  public void setTimesheetID(int timesheetID) {
    this.timesheetID = timesheetID;
  }

  public int getTargetHourPractice() {
    return targetHourPractice;
  }

  public void setTargetHourPractice(int targetHourPractice) {
    this.targetHourPractice = targetHourPractice;
  }

  public int getTargetHourTheory() {
    return targetHourTheory;
  }

  public void setTargetHourTheory(int targetHourTheory) {
    this.targetHourTheory = targetHourTheory;
  }

  public String getLectures() {
    return lectures;
  }

  public void setLectures(String lectures) {
    this.lectures = lectures;
  }

  public boolean isIsActive() {
    return isActive;
  }

  public void setIsActive(boolean isActive) {
    this.isActive = isActive;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    JsonTimesheet that = (JsonTimesheet) o;

    if (timesheetID != that.timesheetID) return false;
    if (targetHourPractice != that.targetHourPractice) return false;
    if (targetHourTheory != that.targetHourTheory) return false;
    if (isActive != that.isActive) return false;
    return lectures.equals(that.lectures);

  }

  @Override
  public int hashCode() {
    int result = timesheetID;
    result = 31 * result + lectures.hashCode();
    result = 31 * result + targetHourPractice;
    result = 31 * result + targetHourTheory;
    result = 31 * result + (isActive ? 1 : 0);
    return result;
  }
}
