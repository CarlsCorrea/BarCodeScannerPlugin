function BarCodeScannerPlugin() {}

BarCodeScannerPlugin.prototype.scan = function(successCallback, errorCallback) {
  var options = {};
  
  if (errorCallback == null) {
    errorCallback = function () {};
  }

  if (typeof errorCallback != "function") {
      console.log("BarCodeScannerPlugin.scan failure: failure parameter not a function");
      return;
  }

  if (typeof successCallback != "function") {
      console.log("BarCodeScannerPlugin.scan failure: success callback parameter must be a function");
      return;
  }

  exec(function (result) {
      successCallback(result);
  }, function (error) {
      errorCallback(error);
  }, 'BarCodeScannerPlugin', 'scan', config);

}

BarCodeScannerPlugin.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.barCodeScannerPlugin = new BarCodeScannerPlugin();
  return window.plugins.barCodeScannerPlugin;
};
cordova.addConstructor(BarCodeScannerPlugin.install);

