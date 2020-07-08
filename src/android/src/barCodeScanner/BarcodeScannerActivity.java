package com.carlscorrea.cordova.plugin;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.List;

public class BarcodeScannerActivity extends AppCompatActivity {

    private CameraKitView _cameraView;
    private View _line;
    private Runnable r;
    private Handler h;
    private boolean _frontFacingCamera;
    private boolean _flashEnabled;
    private boolean _drawLine;

    public final static String BarcodeObject = "BarcodeObject";
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
                Capture();
                h.postDelayed(r, 1000);
            }
        };

        _cameraView.setPreviewListener(new CameraKitView.PreviewListener() {
            @Override
            public void onStart() {
                h.postDelayed(r,0);
            }

            @Override
            public void onStop() {
                h.removeCallbacks(r);
            }
        });
    }

    private void setOptions(){
        setCameraOptions();
        setAddtionalUI();
    }

    private void setAddtionalUI() {
        if(_drawLine){
            _line.setVisibility(View.VISIBLE);
        }
        else{
            _line.setVisibility(View.INVISIBLE);
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
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                //assuming at this point a portrait orientation
                InputImage input = InputImage.fromBitmap(bitmap, 90);
                BarcodeScanner scanner = BarcodeScanning.getClient();
                Task<List<Barcode>> result = scanner.process(input)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            Scan(barcodes);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //for now does nothing
                        }
                    });
            }
        });

    }

    private void Scan(List<Barcode> barcodes) {
        for (Barcode barcode: barcodes) {

            String raw = barcode.getRawValue();
            Intent data = new Intent();
            data.putExtra(BarcodeObject, raw);

            if(raw.length() != 0){

                setResult(CommonStatusCodes.SUCCESS, data);
            }
            else{

                setResult(CommonStatusCodes.ERROR, data);
            }

            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        _cameraView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _cameraView.onResume();
    }

    @Override
    protected void onPause() {
        _cameraView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        _cameraView.onStop();
        super.onStop();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        _cameraView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra(BarcodeObject, "backPressed");
        finish();
    }
}