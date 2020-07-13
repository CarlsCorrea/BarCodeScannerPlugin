package com.carlscorrea.cordova.plugin;
// The native Toast API
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
// Cordova-required packages
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.mlkit.vision.barcode.Barcode;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;

public class BarCodeScannerPlugin extends CordovaPlugin {

    private  static int BARCODE_REQ = 9001;
    private CallbackContext _callCallbackContext;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args,
        final CallbackContext callbackContext) {

        _callCallbackContext =  callbackContext;
        Context context = cordova.getActivity().getApplicationContext();
        if(action.equals("scan")){
          Thread thread = new Thread(new StartBarCodeTask(context,args));
          thread.start();
          return true;
        }
        return false;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.e("BARCODEREQCODE", ""+ requestCode);
        if(requestCode == BARCODE_REQ){
            Log.e("RESULTCODE", ""+ resultCode);
            boolean isDataNull = data == null;
            Log.e("DATA", "isNull:"+ isDataNull);

            if(resultCode == CommonStatusCodes.SUCCESS){
                if(data != null){

                    JSONArray result = new JSONArray();
                    //BARCODE RESULT GOES IN THE ARRAY
                    String raw = data.getStringExtra(BarcodeScannerActivity.BarcodeObject);
                    result.put(raw);
                    Log.d("barcodeDEBUG","RAW obj: " + raw);
                    _callCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
                }
            }
            else {
            JSONArray result = new JSONArray();
            result.put("err");
            Log.e("BarcodeScannerError", "Error on act result");
            _callCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, result));
            }

        }
    }

    public void StartBarCodeActivity(Context context, JSONArray args){
        cordova.setActivityResultCallback(this);
        Intent intent = new Intent(context, BarcodeScannerActivity.class);
        intent.putExtra("frontFacingCamera", args.optInt(0,0));
        intent.putExtra("drawLine", args.optInt(1,0));
        intent.putExtra("flashEnabled", args.optInt(2,0));
        cordova.startActivityForResult(this, intent, BARCODE_REQ);
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



      
  
