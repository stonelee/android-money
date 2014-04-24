/* global Android, Chart, _ */

(function() {
  'use strict';

  //helper
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
  var ANDROID = {
    getBills: function(callback) {
      loadJSON('bills.json', callback);
    },
    onLoad: function() {
      console.log('onLoad');
    },
    onFinish: function() {
      console.log('onFinish');
    }
  };

  if (window.Android) {
    ANDROID = {
      getBills: function(callback) {
        var json = Android.getBills();
        callback(JSON.parse(json));
      },
      onLoad: function() {
        Android.onLoad();
      },
      onFinish: function() {
        Android.onFinish();
      }
    };
  }

  var App = (function() {
    var myChart = document.getElementById('myChart');
    var ctx = myChart.getContext('2d');
    //canvas自适应大小显示
    var canvasWidth = window.innerWidth - 20;
    var canvasHeight = window.innerHeight - myChart.offsetTop - 20;

    function parseBills(data) {
      var datetime = 'datetime(created, \'localtime\')';

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

    function init(bills) {
      var total = _.reduce(bills, function(result, money) {
        return result += money;
      }, 0);
      document.getElementById('total').innerHTML = total;

      var dates = _.keys(bills);
      var minDate = dates[0];
      var maxDate = dates.slice(-1)[0];
      document.getElementById('datetime').innerHTML = minDate + ' 至 ' + maxDate;
    }

    var chartData;

    function getChartData(bills) {
      chartData = {
        labels: _.keys(bills),
        datasets: [{
          fillColor: 'rgba(151,187,205,0.5)',
          strokeColor: 'rgba(151,187,205,1)',
          pointColor: 'rgba(151,187,205,1)',
          pointStrokeColor: '#fff',
          data: _.values(bills)
        }]
      };
    }

    return {
      init: function(callback) {
        ANDROID.getBills(function(data) {
          var bills = parseBills(data);
          init(bills);
          getChartData(bills);
          callback();
        });
      },
      //为导出图像特别订制
      drawChartForBitmap: function() {
        //固定为250*250，如果随意可能会无法导出图像
        var width = 250,
          height = 250;
        ctx.canvas.style.width = width + 'px';
        ctx.canvas.style.height = height + 'px';
        ctx.canvas.width = width;
        ctx.canvas.height = height;
        ctx.scale(1, 1);

        new Chart(ctx, true).Line(chartData, {
          animation: false
        });
      },
      drawChart: function() {
        ctx.canvas.width = canvasWidth;
        ctx.canvas.height = canvasHeight,

        new Chart(ctx).Line(chartData);
      },
      clearChart: function() {
        ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
      }
    };
  })();

  App.init(function() {
    App.drawChartForBitmap();
    //做个延时，否则导出的图像为空白
    setTimeout(function() {
      ANDROID.onLoad();
      App.clearChart();
      App.drawChart();
      ANDROID.onFinish();
    }, 100);
  });

  //供android调用
  window.JS = {};

})();
