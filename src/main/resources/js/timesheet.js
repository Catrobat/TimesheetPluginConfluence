"use strict";

var baseUrl, timesheetTable, timesheetForm, restBaseUrl;

AJS.toInit(function () {
  baseUrl = AJS.$("meta[id$='-base-url']").attr("content");
  restBaseUrl = baseUrl + "/rest/timesheet/1.0/";

  timesheetForm = AJS.$("#timesheet-form");
  timesheetForm.submit(function(event) {
    //todo: validate
    event.preventDefault();
    return false;
  });
  timesheetTable = AJS.$("#timesheet-table");
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

  prepareForm(timesheetTableBody, {entryID : "new-id"}, timesheetData.teams, 
          timesheetData.categories ); 

  //prepare view
  timesheetData.entries.map( function(entry) {
    var entryRow = renderEntryRow(entry, timesheetData.categories, timesheetData.teams); 
    timesheetTableBody.append(entryRow);
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
  var entryFormTR = tableBody.find("#entry-" + entry.entryID);

  //date time columns
  var dateField = entryFormTR.find('.aui-date-picker').datePicker(
    {overrideBrowserDefault: true, languageCode : 'de'}
  );
  
  var beginTimeField = entryFormTR.find('input.time.start');
  var endTimeField   = entryFormTR.find('input.time.end');
  var pauseTimeField = entryFormTR.find('input.time.pause');
  
  entryFormTR.find('input.time')
    .timepicker({
      showDuration: true,
      timeFormat: 'H:i',
      scrollDefault: 'now',
      step: 15
    }); 
    
  pauseTimeField
    .change(function(){
      if(this.value === '') {
        this.value = '00:00';
      }
    })
    .on('timeFormatError', function() {
      this.value = '00:00';
    })
  ;
  
  var datepair = new Datepair(entryFormTR.find(".time-picker")[0]);  

  entryFormTR.find('input.time').change(function(){
    
    //todo: fix duration update without setTimeout
    setTimeout(function() {
      var duration = calculateDuration(
          beginTimeField.timepicker('getTime'),  
          endTimeField.timepicker('getTime'),
          pauseTimeField.timepicker('getTime')); 

      if (duration < 0) duration = new Date(0);

      entryFormTR.find('.duration').val(toUTCTimeString(duration)); 
    }, 10);
  });  
 
  //team and category select
  var categorySelect = entryFormTR.find("span.category");
  
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
  
  var teamSelect = entryFormTR.find("select.team")
    .auiSelect2()
    .change(function(){
      updateCategoryOptions(this.value);
    });
  
  updateCategoryOptions(Object.keys(teams)[0]);
 
  var descriptionField = entryFormTR.find("input.description");
 
  //buttons
  var saveButton = entryFormTR.find("button#save");
  var loadingSpinner = entryFormTR.find("span.aui-icon-wait");
  
  saveButton.click(function() {
    
    var date = dateField.getDate();
    var beginDate = new Date(date.toDateString() + " " + toTimeString(beginTimeField.timepicker('getTime')));
    var endDate   = new Date(date.toDateString() + " " + toTimeString(endTimeField.timepicker('getTime')));
    var pauseMinutes = pauseTimeField.timepicker('getTime').getHours() * 60 + pauseTimeField.timepicker('getTime').getMinutes();
    var duration = calculateDuration(beginTimeField.timepicker('getTime'), endTimeField.timepicker('getTime'), pauseTimeField.timepicker('getTime'));
    
    var entry = {
        beginDate : beginDate,
        endDate : endDate,
        description : descriptionField.val(),
        pauseMinutes : pauseMinutes,
        duration : duration,
        teamID : teamSelect.val(),
        categoryID : categorySelect.val()
    };
    
    loadingSpinner.show();
    
    var timesheetID = 123;
    
    AJS.$.ajax({
      type: "post",
      url: restBaseUrl + "timesheets/" + timesheetID + "/entries",
      contentType: "application/json",
      data: JSON.stringify(entry)
    })
    .then(function(entry){
      loadingSpinner.hide();
      var entryRow = renderEntryRow(entry, categories, teams);
      entryFormTR.after(entryRow);      
    })
    .fail(function(error){
      AJS.messages.error({
          title: 'There was an error',
          body: '<p>Your record could not be saved... :(.</p>'
      });
      console.log(error);
      loadingSpinner.hide();
    }); 
  });

  loadingSpinner.hide();
  
}

function renderEntryRow(entry, categories, teams) {

  entry.date  = new Date(entry.beginDate).toLocaleDateString();
  entry.begin = toTimeString(new Date(entry.beginDate));
  entry.end   = toTimeString(new Date(entry.endDate));

  var pauseDate  = new Date(entry.pauseMinutes * 1000 * 60);
  entry.pause    = toUTCTimeString(pauseDate);
  entry.duration = toUTCTimeString(new Date(entry.duration));

  entry.category = categories[entry.categoryID].categoryName;
  entry.team     = teams[entry.teamID].teamName;

  return Confluence.Templates.Timesheet.timesheetEntry(
          {entry : entry, teams : teams});		

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

function calculateDuration(begin, end, pause) {
  var pauseDate = new Date(pause);
  return new Date(end - begin - (pauseDate.getHours() * 60 + pauseDate.getMinutes()) * 60 * 1000);
}
