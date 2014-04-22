/* global Android, Chart, _ */
(function() {
  'use strict';

  function loadJSON(url, callback) {
    var xhr = new XMLHttpRequest();
    xhr.overrideMimeType('application/json');
    xhr.open('GET', url, true);
    xhr.onreadystatechange = function() {
      if (xhr.readyState == 4 && xhr.status == '200') {
        callback(JSON.parse(xhr.responseText));
      }
    };
    xhr.send(null);
  }

  //mock android interface
  var App = {
    getBills: function(callback) {
      loadJSON('bills.json', callback);
    }
  };
  if (window.Android) {
    App = {
      getBills: function(callback) {
        var json = Android.getBills();
        callback(JSON.parse(json));
      }
    };
  }

  function parseBills(data) {
    var datetime = "datetime(created, 'localtime')";

    var result = _.chain(data).groupBy(function(bill) {
      return bill[datetime].split(' ')[0];
    }).reduce(function(result, bills, date) {
      result[date] = _.reduce(bills, function(sum, bill) {
        return sum + bill.money;
      }, 0);
      return result;
    }, {}).value();
    return result;
  }

  var ctx = document.getElementById('myChart').getContext('2d');
  //adjust canvas scale
  ctx.canvas.width = window.innerWidth;
  ctx.canvas.height = window.innerHeight;

  App.getBills(function(data) {
    var bills = parseBills(data);

    var options = {
      labels: _.keys(bills),
      datasets: [{
        fillColor: 'rgba(151,187,205,0.5)',
        strokeColor: 'rgba(151,187,205,1)',
        pointColor: 'rgba(151,187,205,1)',
        pointStrokeColor: '#fff',
        data: _.values(bills)
      }]
    };

    new Chart(ctx).Line(options);
  });
})();
