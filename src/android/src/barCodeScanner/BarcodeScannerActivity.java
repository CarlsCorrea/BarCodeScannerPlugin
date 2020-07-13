package com.carlscorrea.cordova.plugin;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.jpegkit.Jpeg;

import java.util.List;

public class BarcodeScannerActivity extends AppCompatActivity{

    private CameraKitView _cameraView;
    private View _line;
    private Runnable r;
    private Handler h;
    private boolean _frontFacingCamera;
    private boolean _flashEnabled;
    private boolean _drawLine;
    private BarcodePreviewListener _listener;

    public final static String BarcodeObject = "BarcodeObject";
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResources().getIdentifier("carlscorrea_base_camera", "layout", getPackageName()));
        _cameraView = findViewById(getResources().getIdentifier("camera", "id", getPackageName()));
        _line = findViewById(getResources().getIdentifier("line","id",getPackageName()));

        getSettings();
        setOptions();
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        h = new Handler();
        r = new Runnable() {
            @Override
            public void run() {
                Log.e("BARCODETHREAD", "Started capturing");
                Capture();
                Log.e("BARCODETHREAD", "Finished capturing");
            }
        };

        _listener = new BarcodePreviewListener(this);
    }


    private void setOptions(){
        setCameraOptions();
        setAddtionalUI();
    }

    private void setAddtionalUI() {

        Log.e("BARCODEACT", "_drawLine:" + _drawLine);
        if(_drawLine){
            _line.setVisibility(View.VISIBLE);
        }
        else{
            Log.e("BARCODEACT", "Line is beings set to invis");
            _line.setVisibility(View.GONE);
        }
    }

    private void setCameraOptions() {
        _cameraView.setImageMegaPixels(2f);
        if(_frontFacingCamera){
            _cameraView.setFacing(CameraKit.FACING_FRONT);
        }
        else{
            _cameraView.setFacing(CameraKit.FACING_BACK);
        }
      // if(_flashEnabled){
      //     _cameraView.setFlash(CameraKit.FLASH_ON);
      // }
    }

    private void getSettings() {
        _frontFacingCamera = BooleanToParse(getIntent().getIntExtra("frontFacingCamera",0));
        _flashEnabled = BooleanToParse(getIntent().getIntExtra("flashEnabled",0));
        _drawLine = BooleanToParse(getIntent().getIntExtra("drawLine",0));
    }

    private boolean BooleanToParse(int value) {
        if(value != 0){
            return true;
        }
        return false;
    }

    private void Capture() {
        _cameraView.captureImage(new CameraKitView.ImageCallback() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onImage(CameraKitView cameraKitView, byte[] bytes) {
                Log.d("BarcodeScanner", "capturing");

               InputImage image = InputImage.fromBitmap(BitmapFactory.decodeByteArray(bytes,0,bytes.length),90);
                BarcodeScanner scanner = BarcodeScanning.getClient();
                Task<List<Barcode>> result = scanner.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            Log.d("BarcodeScanner", "Success");
                            Log.e("BARCODETHREAD", "StartScan");
                            Scan(barcodes);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //for now does nothing

                            Log.e("BarcodeScanner", e.getMessage());

                            h.postDelayed(r, 0);
                        }
                    });
            }
        });

    }

    private void Scan(List<Barcode> barcodes) {
        Log.d("BARCODEACT", "barcode length:" + barcodes.size());
        if(barcodes.size() == 0){

            h.postDelayed(r, 0);
        }
        else {
            for (Barcode barcode : barcodes) {

                Log.d("BarcodeScanner", "processing");
                String raw = barcode.getRawValue();
                Intent data = new Intent();
                data.putExtra(BarcodeObject, raw);

                if (raw.length() != 0) {

                    setResult(CommonStatusCodes.SUCCESS, data);
                } else {

                    setResult(CommonStatusCodes.ERROR, data);
                }

                Log.e("SCANNERFINISHED", "sending result");
                finish();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("BARCODEACT", "OnStart");
        _cameraView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            Log.e("PERMISSION" , "GRANTED");

            Log.e("BARCODEACT", "OnResume");

            _cameraView.setErrorListener(new CameraKitView.ErrorListener() {
                @Override
                public void onError(CameraKitView cameraKitView, CameraKitView.CameraException e) {
                    Log.e("ONCAMERAKITERROR", e.getMessage());
                }
            });

          if(_cameraView.getPreviewListener() == null){
              _listener = new BarcodePreviewListener(this);
              _cameraView.setPreviewListener(_listener);
          }
            Log.e("BARCODEACT", "PreviewListenerObtained");
            _cameraView.onResume();
            Log.e("BARCODEACT", "cameraResumed");
        }

    }

    @Override
    protected void onPause() {
        _cameraView.removeCameraListener();
        _cameraView.onPause();
        Log.e("BARCODEACT", "OnPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        _cameraView.onStop();
        Log.e("BARCODEACT", "OnStop");
        super.onStop();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        _cameraView.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    public void onBackPressed() {
        setResult(CommonStatusCodes.SUCCESS);
        Log.e("BackPRESSED", "userpressed back");
        finish();
    }

    public void startHandler() {
        h.postDelayed(r, 50);
    }

    public void stopHandler() {
        Log.e("BarcodeAPP", "removing callbacks");
        h.removeCallbacks(r);
    }
}