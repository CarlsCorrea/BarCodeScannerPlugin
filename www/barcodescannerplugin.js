// ----------------------------------------------------------------------------
// |  Imports
// ----------------------------------------------------------------------------
var exec = require('cordova/exec');

// ----------------------------------------------------------------------------
// |  Public interface
// ----------------------------------------------------------------------------
exports.getDefaultSettings = function() {
  return getDefaultSettings();
};

exports.scan = function (p_OnSuccess, p_OnError, p_Settings) {
  if (!p_Settings) {
    p_OnError("p_Settings can't be undefined. Use getDefaultSettings() to get a new settings object");
    return;
  }
  return scan(p_OnSuccess, p_OnError, p_Settings);
};

// ----------------------------------------------------------------------------
// |  Functions
// ----------------------------------------------------------------------------

function getDefaultSettings() {
  var settings = {
    camerafacing: false,
    canvas: false,
    flash: false
  }; 

  return settings;
}
function scan(p_OnSuccess, p_OnError, p_Settings) {

   var camerafacing = 0;
   var flash = 0;
   var drawline = 0;

   if(p_Settings.camerafacing === true){
     camerafacing = 1;
   }
   if(p_Settings.canvas === true){
     drawline = 1;
   }
   if(p_Settings.flash === true){
    flash = 1;
   }

  var settingArray = [
    camerafacing,
    drawline,
    flash
  ];

  
  exec(p_Result => {
    p_OnSuccess(p_Result[0]);
  }, p_OnError, 'cordova-plugin-mlkit-barcode-scanner','scan',settingArray);
};