package com.carlscorrea.cordova.plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseIntArray;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.camerakit.CameraKitView;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import io.cordova.hellocordova.R;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.w3c.dom.Text;

import java.util.List;

public class BarCodeCaptureActivity extends BaseCameraActivity{

    private TextView _data;
    private Runnable r;
    private Handler h;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpBottomSheet(R.layout.carlscorrea_barcode_capture);
        _data = findViewById(R.id.codeData);
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

    private void Capture() {
        _cameraView.captureImage(new CameraKitView.ImageCallback() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onImage(CameraKitView cameraKitView, byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                //assuming at this point a portrait orientation
                InputImage input = InputImage.fromBitmap(bitmap, 90);
                BarcodeScanner scanner = BarcodeScanning.getClient();
                Task<List<Barcode>> result = scanner.process(input).addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
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
            data.putExtra("BarcodeObject", raw);
            setResult(CommonStatusCodes.SUCCESS, data);

            finish();
        }
    }


}