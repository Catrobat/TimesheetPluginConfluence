"use strict";

var baseUrl, timesheetTable, timesheetForm, restBaseUrl;

AJS.toInit(function () {
	baseUrl = AJS.$("meta[id$='-base-url']").attr("content");
	restBaseUrl = baseUrl + "/rest/timesheet/1.0/";

	timesheetForm = AJS.$("#timesheet-form");
	timesheetForm.submit(function (event) {
		//todo: validate
		event.preventDefault();
		return false;
	});
	timesheetTable = AJS.$("#timesheet-table");
	fetchData();
});

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
		var entryRow = renderEntryRow(timesheetData, entry);
		timesheetTableBody.append(entryRow.viewRow);
		timesheetTableBody.append(entryRow.formRow);
	});
}

/**
 * Callback after creating new Entry
 * @param {Object} entry
 * @param {Object} timesheetData
 * @param {jQuery} form
 */
function addNewEntryCallback(entry, timesheetData, form) {
	var entryRow = renderEntryRow(timesheetData, entry);
	var beginTime = form.beginTimeField.timepicker('getTime');
	var endTime = form.endTimeField.timepicker('getTime');

	form.row.after(entryRow.formRow);
	form.row.after(entryRow.viewRow);
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
	var newViewRow = renderViewRow(timesheetData, entry);

	form.row.prev().remove();
	form.row.before(newViewRow).hide();

	newViewRow.find("button.edit").click(function () {
		newViewRow.hide();
		form.row.show();
	});
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
		saveOptions.callback(entry, timesheetData, form);
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
	form.dateField.datePicker(
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

	row.find('input.time').change(function () {
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
 * creates a view row (for viewing) and a form row (for editing)
 * @param {Object} timesheetData
 * @param {Object} entry
 * @returns {viewrow : jquery, formrow : jquery}
 */
function renderEntryRow(timesheetData, entry) {

	var timesheetID = timesheetData.timesheetID;

	var augmentedEntry = augmentEntryObject(timesheetData, entry);

	var editEntryOptions = {
		httpMethod : "put",
		callback   : editEntryCallback,
		ajaxUrl    : restBaseUrl + "timesheets/" + timesheetData.timesheetID + "/entries/" + entry.entryID
	};

	var entryRow = {};
	entryRow.formRow = renderFormRow(timesheetData, augmentedEntry, editEntryOptions);
	entryRow.viewRow = renderViewRow(timesheetData, augmentedEntry);

	entryRow.formRow.hide();

	entryRow.viewRow.find("button.edit").click(function () {
		editEntryClicked(entryRow);
	});

	entryRow.viewRow.find("button.delete").click(function () {
		deleteEntryClicked(entryRow, timesheetID, entry.entryID);
	});

	return entryRow;
}

function editEntryClicked(entryRow) {
	entryRow.viewRow.hide();
	entryRow.formRow.show();
}

function deleteEntryClicked(entryRow, timesheetID, entryID) {

	var ajaxUrl = restBaseUrl + "timesheets/" + timesheetID + "/entries/" + entryID;

	var spinner = entryRow.viewRow.find('span.aui-icon-wait');
	spinner.show();

	AJS.$.ajax({
		type: 'DELETE',
		url: ajaxUrl,
		contentType: "application/json"
	})
	.then(function () {
		entryRow.viewRow.remove();
		entryRow.formRow.remove();
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
 * Augments an entry object wth a few attributes by deriving them from its
 * original attributes
 * @param {Object} timesheetData
 * @param {Object} entry
 * @returns {Object} augmented entry
 */
function augmentEntryObject(timesheetData, entry) {

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
function renderViewRow(timesheetData, entry) {

	var augmentedEntry = augmentEntryObject(timesheetData, entry);

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
