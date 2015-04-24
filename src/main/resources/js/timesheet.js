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
        beginDate : new Date("2015-03-02 09:00"),
        endDate   : new Date("2015-03-02 10:30"),
        description : "Weekly Meeting",
        pauseMinutes : 15,
        durationDate : new Date(75 * 60 * 1000),
        teamID : 7,
        categoryID : 7
      },
      {
        entryID: 2,
        beginDate : new Date("2015-03-02 10:30"),
        endDate :   new Date("2015-03-02 12:30"),
        description : "Pair Programming with X",
        pauseMinutes : 0,
        durationDate : new Date(120 * 60 * 1000),
        teamID : 8,
        categoryID : 1
      }
    ], 
    teams: {
      7: {teamName: "Pocket Code", teamCategories : [1, 3, 7, 8, 5]},
      8: {teamName: "Scratch MIT HTML5", teamCategories : [1, 3, 7]}
    },
    categories : {
      1: {categoryName : "Pair Programming"}, 
      3: {categoryName : "Programming"}, 
      7: {categoryName : "Meeting"}, 
      8: {categoryName : "Theory (MT)"}, 
      5: {categoryName : "Other"} 
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

  var firstForm = renderFormRow(timesheetData.timesheetID, {
      entryID : "new-id",
      date    : "",
      begin   : "",
      end     : "",
      pause   : "00:00",
      description: "",
      duration: ""
    }, timesheetData.teams, timesheetData.categories, 'post'); 
  
  timesheetTableBody.append(firstForm);
  
  //prepare view
  timesheetData.entries.map(function(entry) {
    var entryRow = renderEntryRow(timesheetData.timesheetID, entry, timesheetData.categories, timesheetData.teams); 
    timesheetTableBody.append(entryRow.viewRow);
    timesheetTableBody.append(entryRow.formRow);
  });  
}

/**
 * creates a form with working ui components and instrumented buttons
 * @param {int} timesheetID
 * @param {object} entry
 * @param {object} teams
 * @param {object} categories
 * @param {string} mode
 *      'post': creates a new entry
 *      'put' : updates an existing entry
 * @returns {jquery} form
 */
function renderFormRow(timesheetID, entry, teams, categories, mode) {
  
  var ajaxUrl, saveCallback;
  
  var form = prepareFormTemplate(entry, teams, categories);
 
  if(mode === 'post') {
    
    ajaxUrl = restBaseUrl + "timesheets/" + timesheetID + "/entries"; 

    saveCallback = function(entry) {
      var entryRow = renderEntryRow(timesheetID, entry, categories, teams);
      var beginTime = form.beginTimeField.timepicker('getTime');
      var endTime   = form.endTimeField.timepicker('getTime');
      form.row.after(entryRow.formRow); 
      form.row.after(entryRow.viewRow); 
      form.beginTimeField.timepicker('setTime', endTime);
      form.endTimeField.timepicker(  'setTime', new Date(2 * endTime - beginTime));
      form.pauseTimeField.val("00:00").trigger('change');
      
    };
    
  } else if (mode === 'put') {
    
    ajaxUrl = restBaseUrl + "timesheets/" + timesheetID + "/entries/" + entry.entryID;
    
    saveCallback = function(entry) {
      var newViewRow = renderViewRow(entry, categories, teams); 
           
      form.row.prev().remove();
      form.row.before(newViewRow).hide();

      newViewRow.find("button.edit").click(function() {
        newViewRow.hide();
        form.row.show();
      });
    };
    
  }
  
  form.saveButton.click(function() {
    
    form.saveButton.prop('disabled', true);
    
    var date      = form.dateField.val();
    var beginTime = form.beginTimeField.timepicker('getTime');
    var endTime   = form.endTimeField.timepicker('getTime');
    var pauseTime = form.pauseTimeField.timepicker('getTime');
    
    var beginDate     = new Date(date + " " + toTimeString(beginTime));
    var endDate       = new Date(date + " " + toTimeString(endTime));
    var pauseMin      = pauseTime.getHours() * 60 + pauseTime.getMinutes();
    var durationDate  = calculateDuration(beginTime, endTime, pauseTime);
    
    var entry = {
        beginDate    : beginDate,
        endDate      : endDate,
        description  : form.descriptionField.val(),
        pauseMinutes : pauseMin,
        durationDate : durationDate,
        teamID       : form.teamSelect.val(),
        categoryID   : form.categorySelect.val()
    };
    
    form.loadingSpinner.show();
    
    AJS.$.ajax({
      type: mode,
      url: ajaxUrl,
      contentType: "application/json",
      data: JSON.stringify(entry)
    })
    .then(saveCallback)
    .fail(function(error){
      AJS.messages.error({
          title: 'There was an error while saving.',
          body: '<p>Reason: ' + error.responseText + '</p>'
      });
      console.log(error);
    }) 
    .always(function(){
      form.loadingSpinner.hide();
      form.saveButton.prop('disabled', false);
    });
  });
  
  return form.row;
  
}

/**
 * create form for editing a entry & instrument ui components
 * @param {object} entry
 * @param {object} teams
 * @param {object} categories
 * @returns {object of jquery objects} 
 */
function prepareFormTemplate(entry, teams, categories) {
  
  var row = $(Confluence.Templates.Timesheet.timesheetEntryForm(
      {entry : entry, teams : teams})
  );
  
  var loadingSpinner   = row.find('span.aui-icon-wait').hide();
  var saveButton       = row.find('button.save');
  var dateField        = row.find('input.date');
  var beginTimeField   = row.find('input.time.start');
  var endTimeField     = row.find('input.time.end');
  var pauseTimeField   = row.find('input.time.pause');
  var durationField    = row.find('input.duration');
  var descriptionField = row.find('input.description');
  var categorySelect   = row.find('span.category');
  var teamSelect       = row.find('select.team');
  
  //date time columns
  dateField.datePicker(
    {overrideBrowserDefault: true, languageCode : 'de'}
  );
    
  row.find('input.time.start, input.time.end')
    .timepicker({
      showDuration: false,
      timeFormat: 'H:i',
      scrollDefault: 'now',
      step: 15
    }); 
  
  pauseTimeField.timepicker({timeFormat: 'H:i',step: 15})
    .change(function(){
      if(this.value === '') {
        this.value = '00:00';
      }
    })
    .on('timeFormatError', function() {
      this.value = '00:00';
    });
  
  new Datepair(row.find(".time-picker")[0]);  

  row.find('input.time').change(function(){
    
    //todo: fix duration update without setTimeout
    setTimeout(function() {
      var duration = calculateDuration(
          beginTimeField.timepicker('getTime'),  
          endTimeField.timepicker('getTime'),
          pauseTimeField.timepicker('getTime')); 

      if (duration < 0) duration = new Date(0);

      durationField.val(toUTCTimeString(duration)); 
    }, 10);
  });  
 
  //team and category select
  var updateCategoryOptions = function(selectedTeamID){
    
    if(selectedTeamID !== null && teams[selectedTeamID] !== undefined) {
      
      var categoriesPerTeam = [];
      
      teams[selectedTeamID].teamCategories.map( function(categoryID) {
        categoriesPerTeam.push(
          {id : categoryID, text : categories[categoryID].categoryName}
        );
      });
      
      categorySelect.auiSelect2({data : categoriesPerTeam});
      
      var selectedCategoryID = (entry.categoryID === undefined || selectedTeamID != entry.teamID )
          ? teams[selectedTeamID].teamCategories[0]
          : entry.categoryID;
      
      categorySelect.auiSelect2("val", selectedCategoryID);
    } else {
      categorySelect.auiSelect2();
    }
  };
  
  var initTeamId = (entry.teamID !== undefined) ? entry.teamID : Object.keys(teams)[0]; 
  
  teamSelect
    .auiSelect2()
    .change(function(){
      updateCategoryOptions(this.value);
    })
    .auiSelect2("val", initTeamId)
    .trigger('change');
    
  return  {
    row              : row,
    loadingSpinner   : loadingSpinner,
    saveButton       : saveButton,
    dateField        : dateField,
    beginTimeField   : beginTimeField,
    endTimeField     : endTimeField,
    pauseTimeField   : pauseTimeField, 
    durationField    : durationField,
    descriptionField : descriptionField,
    categorySelect   : categorySelect,
    teamSelect       : teamSelect
  };
}

/**
 * creates a view row (for viewing) and a form row (for editing)
 * @param {type} timesheetID
 * @param {type} entry
 * @param {type} categories
 * @param {type} teams
 * @returns {viewrow : jquery, formrow : jquery}
 */
function renderEntryRow(timesheetID, entry, categories, teams) {

  prepareEntryObjectForView(entry, categories, teams);
  
  var viewRow = renderViewRow(entry, categories, teams);
  var formRow = renderFormRow(timesheetID, entry, teams, categories, 'put');
  formRow.hide();
  
  viewRow.find("button.edit").click(function() {
    viewRow.hide();
    formRow.show();
  });
   
  return {viewRow: viewRow, formRow: formRow };
}

function prepareEntryObjectForView(entry, categories, teams) {
  entry.date  = toDateString(new Date(entry.beginDate));
  entry.begin = toTimeString(new Date(entry.beginDate));
  entry.end   = toTimeString(new Date(entry.endDate));

  var pauseDate  = new Date(entry.pauseMinutes * 1000 * 60);
  entry.pause    = toUTCTimeString(pauseDate);
  entry.duration = toUTCTimeString(new Date(entry.durationDate));

  entry.category = categories[entry.categoryID].categoryName;
  entry.team     = teams[entry.teamID].teamName;
}

/**
 * Updates the viewrow
 * @param {type} entry
 * @param {type} categories
 * @param {type} teams
 */
function renderViewRow(entry, categories, teams) {
  
  prepareEntryObjectForView(entry, categories, teams);
  
  return AJS.$(Confluence.Templates.Timesheet.timesheetEntry(
          {entry : entry, teams : teams}));
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
