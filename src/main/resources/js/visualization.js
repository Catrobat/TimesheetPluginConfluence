"use strict";

//var baseUrl, visualizationTable, timesheetForm, restBaseUrl;
var restBaseUrl;

AJS.toInit(function () {
	var baseUrl = AJS.$("meta[id$='-base-url']").attr("content");
	restBaseUrl = baseUrl + "/rest/visualization/1.0/";
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
		.done(assembleTimesheetData)
		.done(populateTable)
		.fail(function (error) {
			AJS.messages.error({
				title: 'There was an error while fetching data.',
				body: '<p>Reason: ' + error.responseText + '</p>'
			});
			console.log(error);
		});
}

function assembleTimesheetData(timesheetReply, categoriesReply, teamsReply, entriesReply) {
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
	return timesheetData;
}

function populateTable(timesheetDataReply) {

	var timesheetData = timesheetDataReply[0];

	var visualizationTable = AJS.$("#visualization-table");
	visualizationTable.empty();

	visualizationTable.append(Confluence.Templates.Visualization.visualizationHeader(
					{teams: timesheetData.teams}
	));

	appendEntriesToTable(timesheetData);
}

Array.prototype.contains = function(k) {
  for ( var p in this)
    if (this[p] === k)
      return true;
  return false;
};


function appendEntriesToTable(timesheetData) {

  var visualizationTable = AJS.$("#visualization-table");
  var availableEntries = timesheetData.entries;

  var pos = 0;
  var i = 0;
  //variables for the time calculation
  var totalHours = 0;
  var totalMinutes = 0;
  var totalTimeHours = 0;
  var totalTimeMinutes = 0;
  //save data in an additional array
  var index = 0;
  var dataArray = [];
  var dataPoints = []


  while(i < availableEntries.length) {
    var referenceEntryDate = new Date(availableEntries[pos].beginDate);
    var compareToDate = new Date(availableEntries[i].beginDate);
    var oldPos = pos;

    if((referenceEntryDate.getFullYear() == compareToDate.getFullYear()) &&
       (referenceEntryDate.getMonth() == compareToDate.getMonth())) {
         //add all times for the same year-month pairs
         var hours = calculateDuration(availableEntries[i].beginDate, availableEntries[i].endDate,
         availableEntries[i].pauseMinutes).getHours();
         var minutes = calculateDuration(availableEntries[i].beginDate, availableEntries[i].endDate,
         availableEntries[i].pauseMinutes).getMinutes();
         var pause = availableEntries[i].pauseMinutes;
         var calculatedTime = hours * 60 + minutes - pause;

         totalMinutes = totalMinutes + calculatedTime;

         if(totalMinutes >= 60) {
            var minutesToFullHours = Math.floor(totalMinutes / 60) ; //get only full hours
            totalHours = totalHours + minutesToFullHours;
            totalMinutes = totalMinutes - minutesToFullHours * 60;
         }

      } else {
          pos = i;
          i = i - 1;
        }

        if(oldPos != pos || i == availableEntries.length - 1) {
          //create a new table entry and add it to the table
          var newVisualizationEntry = {
           entryID: index,
           date: referenceEntryDate.getFullYear() + "-" + (referenceEntryDate.getMonth() + 1),
           begin: totalHours+"h"+totalMinutes+"min",
           end: "",
           pause: "",
           description: "",
           duration: ""
          };

          //add points
          var dataX = referenceEntryDate.getFullYear() + "-" + (referenceEntryDate.getMonth() + 1);
          var dataY = totalHours + totalMinutes / 60;
          dataPoints.push(dataX);
          dataPoints.push(dataY);

          dataArray.push(newVisualizationEntry);
          index = index + 1;

          var viewRow = AJS.$(Confluence.Templates.Visualization.visualizationEntry(
            {entry: newVisualizationEntry, teams: timesheetData.teams}));
            visualizationTable.append(viewRow);

         //overall sum of spent time
         totalTimeHours = totalTimeHours + totalHours;
         totalTimeMinutes = totalTimeMinutes + totalMinutes;

         if(totalTimeMinutes >= 60) {
            var minutesToFullHours = Math.floor(totalTimeMinutes / 60) ; //get only full hours
            totalTimeHours = totalTimeHours + minutesToFullHours;
            totalTimeMinutes = totalTimeMinutes - minutesToFullHours * 60;
         }
            totalHours = 0;
            totalMinutes = 0;
        }
        i = i + 1;
    }

    //entry for whole time
    var newVisualizationEntry = {
     entryID: index,
     date: "Gesamtdauer",
     begin: totalTimeHours+"h"+totalTimeMinutes+"min",
     end: "",
     pause: "",
     description: "",
     duration: ""
    };

    dataArray.push(newVisualizationEntry);

    var viewRow = AJS.$(Confluence.Templates.Visualization.visualizationEntry(
      {entry: newVisualizationEntry, teams: timesheetData.teams}));
      visualizationTable.append(viewRow);

    //entry for average time
    var averageMinutesPerMonth =  (totalTimeHours*60 + totalTimeMinutes) / (dataArray.length - 1);
    var averageTimeHours = 0;
    var averageTimeMinutes = 0;

     if(averageMinutesPerMonth >= 60) {
        var minutesToFullHours = Math.floor(averageMinutesPerMonth / 60) ; //get only full hours
        averageTimeHours = minutesToFullHours;
        averageTimeMinutes = averageMinutesPerMonth - minutesToFullHours * 60;
     }

    newVisualizationEntry = {
     entryID: index,
     date: "Zeit / Monat",
     begin:  averageTimeHours+"h"+averageTimeMinutes+"min",
     end: "",
     pause: "",
     description: "",
     duration: ""
    };

    dataArray.push(newVisualizationEntry);

    var viewRow = AJS.$(Confluence.Templates.Visualization.visualizationEntry(
      {entry: newVisualizationEntry, teams: timesheetData.teams}));
      visualizationTable.append(viewRow);

    diagram(dataPoints);
}

function diagram(dataPoints) {

   var data = [];
   for(var i = 0; i < dataPoints.length; i = i + 2) {
     data.push({
         year : dataPoints[i],
         value  : dataPoints[i+1]
     });
   }
   drawDiagram(data);
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

	var viewRow = AJS.$(Confluence.Templates.Visualization.visualizationEntry(
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

function getMinutesFromTimeString(timeString) {
	var pieces = timeString.split(":");
	if(pieces.length === 2) {
		var hours = parseInt(pieces[0]);		
		var minutes = parseInt(pieces[1]);
		return hours * 60 + minutes;
	} else {
		return 0; 
	}
}
