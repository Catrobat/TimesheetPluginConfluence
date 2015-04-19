"use strict";

var baseUrl;
var timesheetTable;

AJS.toInit(function () {
  baseUrl = AJS.$("meta[id$='-base-url']").attr("content");
  timesheetTable = AJS.$("table#timesheet-table");
  initTable();
});

function initTable() {
  //do something with ajax over here
  //sample data
  var timesheetData = {
    timesheetID: 1,
    userKey: "admin",
    targetHourPractice: 150,
    targetHourTheory: 0,
    lecture: "Project Softwaretechnologie",
    entries: [
      {
        entryID: 1,
        beginDate : new Date(0),
        endDate : new Date(1000),
        description : "so und so hab ich gemacht",
        pauseMinutes : 200,
        duration : new Date(1000),
        teamID : 7,
        categoryID : 7
      },
      {
        entryID: 2,
        beginDate : new Date(1000),
        endDate : new Date(1000 + 60 * 60 * 1000),
        description : "und was anderes",
        pauseMinutes : 50,
        duration : new Date(60 * 60 * 1000),
        teamID : 8,
        categoryID : 1
      }
    ], 
    teams: {
      7: {teamName: "Scratch MIT Html5", teamCategories : [1, 7, 8]},
      8: {teamName: "Catrobat", teamCategories : [1, 3]}
    },
    categories : {
      1: {categoryName : "Händewaschen"}, 
      3: {categoryName : "Beten"}, 
      7: {categoryName : "Klatschen"}, 
      8: {categoryName : "Luftdruckchecken"}, 
      5: {categoryName : "Glück haben"} 
    }
  };
  
  populateTable(timesheetData);
  
}

function populateTable(timesheetData) {
  
//  timesheetTable.find("tbody:last-child").remove();
  var timesheetTableBody = timesheetTable.find("tbody");
  timesheetTableBody.empty();
  
  timesheetData.entries.map( function(entry) {
    
    entry.date  = entry.beginDate.toLocaleDateString();
    entry.begin = entry.beginDate.toLocaleTimeString();
    entry.end   = entry.endDate.toLocaleTimeString();
    
    var pauseDate = new Date(entry.pauseMinutes * 1000 * 60);
    entry.pause = pauseDate.getUTCHours() + ":" + pauseDate.getUTCMinutes();
    entry.duration = entry.duration.getUTCHours() + ":" + entry.duration.getUTCMinutes();
    
    entry.category = timesheetData.categories[entry.categoryID].categoryName;
    entry.team     = timesheetData.teams[entry.teamID].teamName;
    
    var entryTemplate = Confluence.Templates.Timesheet.timesheetEntry({entry : entry});		
    timesheetTableBody.append(entryTemplate);
    
  });
    
  
  
}