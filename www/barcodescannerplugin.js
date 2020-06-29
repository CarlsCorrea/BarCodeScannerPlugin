// Empty constructor
function BarCodeScannerPlugin() {}

// The function that passes work along to native shells
// Message is a string, duration may be 'long' or 'short'
BarCodeScannerPlugin.prototype.scan = function(successCallback, errorCallback) {
  var options = {};
  cordova.exec(successCallback, errorCallback, 'BarCodeScannerPlugin', 'scan', [options]);
}

// Installation constructor that binds ToastyPlugin to window
BarCodeScannerPlugin.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.barCodeScannerPlugin = new BarCodeScannerPlugin();
  return window.plugins.barCodeScannerPlugin;
};
cordova.addConstructor(BarCodeScannerPlugin.install);