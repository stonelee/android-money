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

    var result = _.chain(data).reverse().groupBy(function(bill) {
      return bill[datetime].split(' ')[0];
    }).reduce(function(result, bills, date) {
      result[date] = _.reduce(bills, function(sum, bill) {
        return sum + bill.money;
      }, 0);
      return result;
    }, {}).reduce(function(result, num, date) {
      var key = date.split('-').slice(1).join('-');
      result[key] = num;
      return result;
    }, {}).value();

    return result;
  }

  var myChart = document.getElementById('myChart');
  var ctx = myChart.getContext('2d');
  //adjust canvas scale
  ctx.canvas.width = window.innerWidth;
  ctx.canvas.height = window.innerHeight - myChart.offsetTop - 10;

  function drawChart(bills) {
    var data = {
      labels: _.keys(bills),
      datasets: [{
        fillColor: 'rgba(151,187,205,0.5)',
        strokeColor: 'rgba(151,187,205,1)',
        pointColor: 'rgba(151,187,205,1)',
        pointStrokeColor: '#fff',
        data: _.values(bills)
      }]
    };

    new Chart(ctx).Line(data);
  }

  App.getBills(function(data) {
    var bills = parseBills(data);

    var total = _.reduce(bills, function(result, money) {
      return result += money;
    }, 0);
    document.getElementById('total').innerHTML = total;

    var dates = _.keys(bills);
    var minDate = dates[0];
    var maxDate = dates.slice(-1)[0];
    document.getElementById('datetime').innerHTML = minDate + ' 至 ' + maxDate;

    drawChart(bills);
  });
})();
