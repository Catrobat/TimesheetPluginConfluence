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
import java.util.List;

@SuppressWarnings("unused")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class JsonTimesheet {

	@XmlElement
	private int timesheetID;
	@XmlElement
	private String userKey;
	@XmlElement
	private int targetHourPractice;
	@XmlElement
	private int targetHourTheory;
	@XmlElement
	private boolean isActive;

	public JsonTimesheet() {
	}
	
	public JsonTimesheet(int timesheetID, String userKey, int targetHourPractice, int targetHourTheory, boolean isActive) {
		this.timesheetID = timesheetID;
		this.userKey = userKey;
		this.targetHourPractice = targetHourPractice;
		this.targetHourTheory = targetHourTheory;
		this.isActive = isActive;
	}
	
	public int getTimesheetID() {
		return timesheetID;
	}

	public void setTimesheetID(int timesheetID) {
		this.timesheetID = timesheetID;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
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

	public boolean isIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

}
