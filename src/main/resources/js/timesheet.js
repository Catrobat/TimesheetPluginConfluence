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
      8: {teamName: "Catrobat", teamCategories : [3, 8]}
    },
    categories : {
      1: {categoryName : "Hoffen"}, 
      3: {categoryName : "Beten"}, 
      7: {categoryName : "Klatschen"}, 
      8: {categoryName : "Luftdruckchecken"}, 
      5: {categoryName : "Denken"} 
    }
  };
  
  populateTable(timesheetData);
  
}

function populateTable(timesheetData) {

  var timesheetTableHeader = timesheetTable.find("thead");
  timesheetTableHeader.append(Confluence.Templates.Timesheet.timesheetHeader(
          {teams : timesheetData.teams}
      ));
  
  var timesheetTableBody = timesheetTable.find("tbody");
  timesheetTableBody.empty();

  prepareForm(timesheetTableBody, {id : "newId"}, timesheetData.teams, 
          StimesheetData.categories ); 

  //prepare view
  timesheetData.entries.map( function(entry) {
    
    entry.date  = entry.beginDate.toLocaleDateString();
    entry.begin = toTimeString(entry.beginDate);
    entry.end   = toTimeString(entry.endDate);
    
    var pauseDate  = new Date(entry.pauseMinutes * 1000 * 60);
    entry.pause    = toTimeString(pauseDate);
    entry.duration = toTimeString(entry.duration);
    
    entry.category = timesheetData.categories[entry.categoryID].categoryName;
    entry.team     = timesheetData.teams[entry.teamID].teamName;
    
    var entryTemplate = Confluence.Templates.Timesheet.timesheetEntry(
            {entry : entry, teams : timesheetData.teams});		
    timesheetTableBody.append(entryTemplate);
    
  });  
  
}

function prepareForm(tableBody, entry, teams, categories) {
  
  var newEntryTemplate = Confluence.Templates.Timesheet.timesheetEntryForm(
      {
        entry : entry, 
        teams : teams
      }
  );
  
  //prepare empty form
  tableBody.append(newEntryTemplate);
  var newEntryTR = tableBody.last();

  newEntryTR.find('.aui-date-picker').datePicker(
    {overrideBrowserDefault: true, languageCode : 'de'}
  );
  
  var startTime = newEntryTR.find('input.time.start');
  var endTime   = newEntryTR.find('input.time.end');
  var pauseTime = newEntryTR.find('input.time.pause');
  
  newEntryTR.find('input.time')
    .timepicker({
      showDuration: true,
      timeFormat: 'H:i',
      step: 15
    }); 
    
  pauseTime
    .change(function(){
      if(this.value === '') {
        this.value = '00:00';
      }
    })
    .on('timeFormatError', function() {
      this.value = '00:00';
    })
  ;
    
  newEntryTR.find('input.time').change(function(){
      datepair.refresh(); 
      
      var pause = new Date(pauseTime.timepicker('getTime'));
      var duration = new Date(
        endTime.timepicker('getTime') 
        - startTime.timepicker('getTime')
        - (pause.getHours() * 60 + pause.getMinutes()) * 60 * 1000
      );
      
      if (duration < 0) duration = new Date(0);
          
      newEntryTR.find('.duration').val(toTimeString(duration)); 
    })
  ;
  
//  var now = new Date(); 
  startTime.timepicker(
//      "setTime" , new Date(new Date() - new Date())
//      "setTime" , new Date(new Date() - (new Date() % (5 * 60 * 1000)))
      "setTime" , new Date("1 1 1970 09:00")
//      "setTime" , new Date((now.getUTCHours() * 60 + now.getUTCMinutes()) * 60 * 1000 )
  );
  
  var datepair = new Datepair(newEntryTR.find(".time-picker")[0]);  
  
  var categorySelect = newEntryTR.find("span.category");
  
  var updateCategoryOptions = function(selectedTeamID){
    if(selectedTeamID !== null && teams[selectedTeamID] !== undefined) {
      var categoriesPerTeam = [];
      teams[selectedTeamID].teamCategories.map( function(categoryID) {
        categoriesPerTeam.push({id : categoryID, text : categories[categoryID].categoryName});
      });
      categorySelect.auiSelect2({data : categoriesPerTeam});
      categorySelect.auiSelect2("val", teams[selectedTeamID].teamCategories[0]);
    } else {
      categorySelect.auiSelect2();
    }
  };
  
  newEntryTR.find("select.team")
    .auiSelect2()
    .change(function(){
      updateCategoryOptions(this.value);
    });
  
  updateCategoryOptions(Object.keys(teams)[0]);
 
}

function toTimeString(date) {
  var h = date.getUTCHours(), m = date.getUTCMinutes();
  var string = 
    ((h < 10) ? "0" : "") + h + ":" +  
    ((m < 10) ? "0" : "") + m;
  return string;
}