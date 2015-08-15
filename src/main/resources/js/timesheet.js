"use strict";

//var baseUrl, timesheetTable, timesheetForm, restBaseUrl;
var restBaseUrl;

AJS.toInit(function () {
	var baseUrl = AJS.$("meta[id$='-base-url']").attr("content");
	restBaseUrl = baseUrl + "/rest/timesheet/1.0/";

	var timesheetForm = AJS.$("#timesheet-form");
	timesheetForm
		.submit(preventFormSubmission)
		.keydown(preventButtonClickOnEnter); 
		
	fetchData();
});

function preventFormSubmission(event) {
	event.preventDefault();
	return false;
}
	
function preventButtonClickOnEnter(event){
	if(event.keyCode === 13) {	 
		return false;	
	}
}

function fetchData() {
	var timesheetFetched = AJS.$.ajax({
		type: 'GET',
		url: restBaseUrl + 'timesheets/' + timesheetID,
		contentType: "application/json"
	});

	var entriesFetched = AJS.$.ajax({
		type: 'GET',
		url: restBaseUrl + 'timesheets/' + timesheetID + '/entries',
		contentType: "application/json"
	});

	var categoriesFetched = AJS.$.ajax({
		type: 'GET',
		url: restBaseUrl + 'categories',
		contentType: "application/json"
	});

	var teamsFetched = AJS.$.ajax({
		type: 'GET',
		url: restBaseUrl + 'teams',
		contentType: "application/json"
	});

	AJS.$.when(timesheetFetched, categoriesFetched, teamsFetched, entriesFetched)
		.done(handletimesheetData)
		.fail(function (error) {
			AJS.messages.error({
				title: 'There was an error.',
				body: '<p>Reason: ' + error.responseText + '</p>'
			});
			console.log(error);
		});
}

function handletimesheetData(timesheetReply, categoriesReply, teamsReply, entriesReply) {
	var timesheetData = timesheetReply[0];

	timesheetData.entries = entriesReply[0];
	timesheetData.categories = [];
	timesheetData.teams = [];

	categoriesReply[0].map(function (category) {
		timesheetData.categories[category.categoryID] = {
			categoryName: category.categoryName
		};
	});

	teamsReply[0].map(function (team) {
		timesheetData.teams[team.teamID] = {
			teamName: team.teamName,
			teamCategories: team.teamCategories
		};
	});

	populateTable(timesheetData);
}

function populateTable(timesheetData) {

	var timesheetTable = AJS.$("#timesheet-table");
	var timesheetTableHeader = timesheetTable.find("thead");
	timesheetTableHeader.append(Confluence.Templates.Timesheet.timesheetHeader(
					{teams: timesheetData.teams}
	));

	var timesheetTableBody = timesheetTable.find("tbody");
	timesheetTableBody.empty();

	var emptyEntry = {
		entryID: "new-id",
		date: "",
		begin: "",
		end: "",
		pause: "00:00",
		description: "",
		duration: ""
	};

	var addNewEntryOptions = {
		httpMethod : "post",
		callback   : addNewEntryCallback,
		ajaxUrl    : restBaseUrl + "timesheets/" + timesheetData.timesheetID + "/entries/"
	};
	
	var emptyForm = renderFormRow(timesheetData, emptyEntry, addNewEntryOptions);

	timesheetTableBody.append(emptyForm);

	//prepare view
	timesheetData.entries.map(function (entry) {
		var viewRow = renderViewRow(timesheetData, entry);
		timesheetTableBody.append(viewRow);
	});
}

/**
 * Callback after creating new Entry
 * @param {Object} entry
 * @param {Object} timesheetData
 * @param {jQuery} form
 */
function addNewEntryCallback(entry, timesheetData, form) {
	var viewRow = renderViewRow(timesheetData, entry);
	var beginTime = form.beginTimeField.timepicker('getTime');
	var endTime = form.endTimeField.timepicker('getTime');

	form.row.after(viewRow);
	form.beginTimeField.timepicker('setTime', endTime);
	form.endTimeField.timepicker('setTime', new Date(2 * endTime - beginTime));
	form.pauseTimeField.val("00:00").trigger('change');
}

/**
 * Callback after editing an entry
 * @param {Object} entry
 * @param {Object} timesheetData
 * @param {jQuery} form
 */
function editEntryCallback(entry, timesheetData, form) {
	var newViewRow = prepareViewRow(timesheetData, entry); //todo check if entry is augmented
	var oldViewRow = form.row.prev();
	
	newViewRow.find("button.edit").click(function () {
	newViewRow.hide();
//		newViewRow.css("opacity", ".2"); //todo remove
	form.row.show();
//	form.row.css("opacity", "1"); //todo remove
	});

	newViewRow.find("button.delete").click(function () {
		deleteEntryClicked(newViewRow, entry.entryID);
	});
	
	oldViewRow.after(newViewRow);
	oldViewRow.remove();
	
	form.row.hide(); 
///	form.row.css("opacity", ".2"); //todo remove
}

/**
 * Handles saving an entry
 * @param {Object} timesheetData
 * @param {Object} saveOptions
 *           callback   : Function(entry, timesheetData, form)
 *           ajaxUrl    : String
 *           httpMethod : String
 * @param {jQuery} form
 * @returns {undefined}
 */
function saveEntryClicked(timesheetData, saveOptions, form) {
	form.saveButton.prop('disabled', true);

	var date      = form.dateField.val();
	var beginTime = form.beginTimeField.timepicker('getTime');
	var endTime   = form.endTimeField.timepicker('getTime');
	var pauseTime = form.pauseTimeField.timepicker('getTime');

	var beginDate = new Date(date + " " + toTimeString(beginTime));
	var endDate   = new Date(date + " " + toTimeString(endTime));
	var pauseMin  = pauseTime.getHours() * 60 + pauseTime.getMinutes();

	var entry = {
		beginDate: beginDate,
		endDate: endDate,
		description: form.descriptionField.val(),
		pauseMinutes: pauseMin,
		teamID: form.teamSelect.val(),
		categoryID: form.categorySelect.val()
	};

	form.loadingSpinner.show();

	AJS.$.ajax({
		type: saveOptions.httpMethod,
		url:  saveOptions.ajaxUrl,
		contentType: "application/json",
		data: JSON.stringify(entry)
	})
	.then(function(entry) {
		var augmentedEntry = augmentEntry(timesheetData, entry);
		saveOptions.callback(augmentedEntry, timesheetData, form);
	})
	.fail(function (error) {
		AJS.messages.error({
			title: 'There was an error while saving.',
			body: '<p>Reason: ' + error.responseText + '</p>'
		});
		console.log(error);
	})
	.always(function () {
		form.loadingSpinner.hide();
		form.saveButton.prop('disabled', false);
	});
}

/**
 * creates a form with working ui components and instrumented buttons
 * @param {Object} timesheetData
 * @param {Object} entry
 * @param {Object} saveOptions
 *           callback   : Function(entry, timesheetData, form)
 *           ajaxUrl    : String
 *           httpMethod : String
 * @returns {jquery} form
 */
function renderFormRow(timesheetData, entry, saveOptions) {

	if (entry.pause === "") {
		entry.pause = "00:00";
	}

	var form = prepareForm(entry, timesheetData);

	form.saveButton.click(function () {
		saveEntryClicked(timesheetData, saveOptions, form);
	});

	return form.row;
}

/**
 * Create form for editing a entry & instrument ui components
 * @param {object} entry
 * @param {object} timesheetData
 * @returns {object of jquery objects} 
 */
function prepareForm(entry, timesheetData) {

	var teams = timesheetData.teams;
	var row = $(Confluence.Templates.Timesheet.timesheetEntryForm(
					{entry: entry, teams: teams})
	);

	var form = {
		row: row,
		loadingSpinner:   row.find('span.aui-icon-wait').hide(),
		saveButton:       row.find('button.save'),
		dateField:        row.find('input.date'),
		beginTimeField:   row.find('input.time.start'),
		endTimeField:     row.find('input.time.end'),
		pauseTimeField:   row.find('input.time.pause'),
		durationField:    row.find('input.duration'),
		descriptionField: row.find('input.description'),
		categorySelect:   row.find('span.category'),
		teamSelect:       row.find('select.team')
	};

	//date time columns
	form.dateField
		.bind("input propertychange", function(){
			var newValue = form.dateField.val();
			handleDateChange(newValue, form);
		})
		.datePicker(
			{overrideBrowserDefault: true, languageCode: 'de'}
		);

	row.find('input.time.start, input.time.end')
		.timepicker({
			showDuration: false,
			timeFormat: 'H:i',
			scrollDefault: 'now',
			step: 15
		});

	form.pauseTimeField.timepicker({timeFormat: 'H:i', step: 15})
		.change(changePauseTimeField)
		.on('timeFormatError', function () {
			this.value = '00:00';
		});

	new Datepair(row.find(".time-picker")[0]);

	row.find('input.time')
		.change(function () {
			updateTimeField(form);
		});

	var initTeamID = (entry.teamID !== undefined)
				? entry.teamID : Object.keys(teams)[0];

	form.teamSelect.auiSelect2()
		.change(function() {
			var selectedTeamID = this.value;
			updateCategorySelect(form.categorySelect, selectedTeamID, entry, timesheetData);
		})
		.auiSelect2("val", initTeamID)
		.trigger("change");

	if (countDefinedElementsInArray(teams) < 2) {
		row.find("td.team").hide();
	}
	
	return form;
}

function handleDateChange(newValue, form) {
	
	var valueArray = newValue.split("\t");

	while(valueArray.length > 0) {
		var arrayLength = (valueArray.length > 8) ? 8 : valueArray.length;  
		
		var newLineDate = undefined; 
		
		switch(arrayLength) {
			case 8: 
				arrayLength--;
				var descriptionAndDate = valueArray[6].split(" ");
				newLineDate = new Date(descriptionAndDate.pop());
				valueArray[6] = descriptionAndDate.join(" ");
			case 7:
				form.descriptionField.val(valueArray[6]);
			case 6:
			case 5:
				var pauseVal = valueArray[4]; 
				if(pauseVal.trim() === "") pauseVal = "00:00";
				form.pauseTimeField.val(pauseVal);
			case 4:
			case 3:
				form.endTimeField.val(valueArray[2]);
			case 2:
				form.beginTimeField.val(valueArray[1]);
			case 1:
				form.dateField.val(valueArray[0]);
		}
		
		if(arrayLength === 7) {
			form.saveButton.trigger("click");
		}
		
		valueArray.reverse();
		while(arrayLength > 0) {
			valueArray.pop();
			arrayLength--;
		}
		
		if(isValidDate(newLineDate)) {
			valueArray.push(toDateString(newLineDate));
		}
		
		valueArray.reverse();
	}
}

/**
 * Updates the Category Seletion Box depending on the selected team
 * @param {jQuery} categorySelect
 * @param {int} selectedTeamID
 * @param {Object} entry
 * @param {Object} timesheetData
 */
function updateCategorySelect(categorySelect, selectedTeamID, entry, timesheetData) {

	var selectedTeam = timesheetData.teams[selectedTeamID];
	var categoryPerTeam = filterCategoriesPerTeam(selectedTeam, timesheetData.categories);

	categorySelect.auiSelect2({data : categoryPerTeam});

	var selectedCategoryID = (entry.categoryID === undefined || selectedTeamID != entry.teamID)
		? selectedTeam.teamCategories[0]
		: entry.categoryID;

	categorySelect.val(selectedCategoryID).trigger("change");
}

/**
 * Creates an array with the categories of seletedTeam
 * @param {Object} selectedTeam
 * @param {Object} categories
 * @returns {Array of Objects}
 */
function filterCategoriesPerTeam(selectedTeam, categories) {

	var categoriesPerTeam = [];

	selectedTeam.teamCategories.map(function (categoryID) {
		categoriesPerTeam.push(
						{id: categoryID, text: categories[categoryID].categoryName}
		);
	});

	return categoriesPerTeam;
}

function updateTimeField(form) {
	//todo: fix duration update without setTimeout
	setTimeout(function () {
		var duration = calculateDuration(
						form.beginTimeField.timepicker('getTime'),
						form.endTimeField.timepicker('getTime'),
						form.pauseTimeField.timepicker('getTime'));

		if (duration < 0) {
			duration = new Date(0);
		}

		form.durationField.val(toUTCTimeString(duration));
	}, 10);
}

function changePauseTimeField() {
	if (this.value === '') {
		this.value = '00:00';
	}
}

/**
 * creates a view row with working ui components
 * @param {Object} timesheetData
 * @param {Object} entry
 * @returns {viewrow : jquery, formrow : jquery}
 */
function renderViewRow(timesheetData, entry) {

	var augmentedEntry = augmentEntry(timesheetData, entry);

	var editEntryOptions = {
		httpMethod : "put",
		callback   : editEntryCallback,
		ajaxUrl    : restBaseUrl + "entries/" + entry.entryID
	};

	var viewRow = prepareViewRow(timesheetData, augmentedEntry);
	viewRow.find("button.edit").click(function () {
		editEntryClicked(timesheetData, augmentedEntry, editEntryOptions, viewRow);
	});

	viewRow.find("button.delete").click(function () {
		deleteEntryClicked(viewRow, entry.entryID);
	});

	return viewRow;
}

function editEntryClicked(timesheetData, augmentedEntry, editEntryOptions, viewRow) {
	
	var formRow = getFormRow(viewRow);
	
	if (formRow === undefined) {
		formRow = renderFormRow(timesheetData, augmentedEntry, editEntryOptions);
		viewRow.after(formRow);
	} 
	
	viewRow.hide();
//		viewRow.css("opacity", ".2"); //todo remove
	formRow.show();
//		formRow.css("opacity", "1"); //todo remove
}

function deleteEntryClicked(viewRow, entryID) {

	var ajaxUrl = restBaseUrl + "entries/" + entryID;

	var spinner = viewRow.find('span.aui-icon-wait');
	spinner.show();

	AJS.$.ajax({
		type: 'DELETE',
		url: ajaxUrl,
		contentType: "application/json"
	})
	.then(function () {
		var formRow = getFormRow(viewRow);
		if(formRow !== undefined) formRow.remove();
		viewRow.remove();
	})
	.fail(function (error) {
		AJS.messages.error({
			title: 'There was an error while deleting.',
			body: '<p>Reason: ' + error.responseText + '</p>'
		});
		console.log(error);
		spinner.hide();
	});
}

/**
 * Finds and returns the form row that belongs to a view row
 * @param {jQuery} viewRow
 * @returns {jQuery} formRow or undefined if not found
 */
function getFormRow(viewRow) {
	var formRow = viewRow.next(".entry-form");
	if(formRow.data("id") === viewRow.data("id")) {
		return formRow;
	}
}

/**
 * Augments an entry object wth a few attributes by deriving them from its
 * original attributes
 * @param {Object} timesheetData
 * @param {Object} entry
 * @returns {Object} augmented entry
 */
function augmentEntry(timesheetData, entry) {

	var pauseDate = new Date(entry.pauseMinutes * 1000 * 60);

	return {
		date         : toDateString(new Date(entry.beginDate)),
		begin        : toTimeString(new Date(entry.beginDate)),
	  end          : toTimeString(new Date(entry.endDate)),
		pause        : (entry.pauseMinutes > 0) ? toUTCTimeString(pauseDate) : "",
		duration     : toTimeString(calculateDuration(entry.beginDate, entry.endDate, pauseDate)),
		category     : timesheetData.categories[entry.categoryID].categoryName,
		team         : timesheetData.teams[entry.teamID].teamName,
		entryID      : entry.entryID,
		beginDate    : entry.beginDate,
		endDate      : entry.endDate,
		description  : entry.description ,
		pauseMinutes : entry.pauseMinutes ,
		teamID       : entry.teamID ,
		categoryID   : entry.categoryID
	};
}

/**
 * Creates the viewrow
 * @param {Object} timesheetData
 * @param {Object} entry
 */
function prepareViewRow(timesheetData, entry) {

  //todo: dont augment entry twice.
	var augmentedEntry = augmentEntry(timesheetData, entry);

	var viewRow = AJS.$(Confluence.Templates.Timesheet.timesheetEntry(
					{entry: augmentedEntry, teams: timesheetData.teams}));

	viewRow.find('span.aui-icon-wait').hide();

	return viewRow;
} 

function toUTCTimeString(date) {
	var h = date.getUTCHours(), m = date.getUTCMinutes();
	var string =
		((h < 10) ? "0" : "") + h + ":" +
		((m < 10) ? "0" : "") + m;
	return string;
}

function toTimeString(date) {
	var h = date.getHours(), m = date.getMinutes();
	var string =
		((h < 10) ? "0" : "") + h + ":" +
		((m < 10) ? "0" : "") + m;
	return string;
}

function toDateString(date) {
	var y = date.getFullYear(), d = date.getDate(), m = date.getMonth() + 1;
	var string = y + "-" +
		((m < 10) ? "0" : "") + m + "-" +
		((d < 10) ? "0" : "") + d;
	return string;
}

function calculateDuration(begin, end, pause) {
	var pauseDate = new Date(pause);
	return new Date(end - begin - (pauseDate.getHours() * 60 + pauseDate.getMinutes()) * 60 * 1000);
}

function countDefinedElementsInArray(array) {
	return array.filter(function (v) {return v !== undefined}).length;
}

/**
 * Check if date is a valid Date
 * source: http://stackoverflow.com/questions/1353684/detecting-an-invalid-date-date-instance-in-javascript
 * @param {type} date
 * @returns {boolean} true, if date is valid 
 */
function isValidDate(date) {
	if ( Object.prototype.toString.call(date) === "[object Date]" ) {
		if ( isNaN( date.getTime() ) ) {
			return false;
		}
		else {
			return true;
		}
	}
	else {
		return false;
	}
}