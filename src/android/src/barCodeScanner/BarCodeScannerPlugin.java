package com.carlscorrea.cordova.plugin;
// The native Toast API
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
// Cordova-required packages
import com.google.android.gms.common.api.CommonStatusCodes;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class BarCodeScannerPlugin extends CordovaPlugin {

    private  static int BARCODE_REQ = 9001;
    private CallbackContext _callCallbackContext;
  @Override
  public boolean execute(String action, JSONArray args,
    final CallbackContext callbackContext) {

      _callCallbackContext =  callbackContext;
      Context context = cordova.getActivity().getApplicationContext();
      if(action.equals("show")){
          Thread thread = new Thread(new StartBarCodeTask(context,args));
          thread.start();
          return true;
      }
      return false;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == BARCODE_REQ && resultCode == CommonStatusCodes.SUCCESS && data != null){

            JSONArray result = new JSONArray();
            //BARCODE RESULT GOES IN THE ARRAY
            _callCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
        }
        else {
            JSONArray result = new JSONArray();
            result.put("err");
            _callCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, result));
        }
    }

    public void StartBarCodeActivity(Context context, JSONArray args){
        cordova.setActivityResultCallback(this);
        cordova.startActivityForResult(this,new Intent(context, BarCodeCaptureActivity.class), BARCODE_REQ);
    }

    private class StartBarCodeTask implements Runnable {
        private Context _context;
        private JSONArray _args;

        private StartBarCodeTask(Context context, JSONArray args){
            _context = context;
            _args = args;
        }
        public void run(){
            StartBarCodeActivity(_context,_args);
        }
    }
  }



      
  
