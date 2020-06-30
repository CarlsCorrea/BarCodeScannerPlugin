package com.carlscorrea.cordova.plugin;

import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import com.camerakit.CameraKitView;

import io.cordova.hellocordova.R;

public class BaseCameraActivity extends AppCompatActivity{

    protected CameraKitView _cameraView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carlscorrea_base_camera);
        _cameraView = findViewById(R.id.camera);
        _cameraView.setImageMegaPixels(2f);
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


    public void setUpBottomSheet(int id){
        ViewStub stub = findViewById(R.id.stubView);
        stub.setLayoutResource(id);
        View inflatedView = stub.inflate();
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) inflatedView.getLayoutParams();
        layoutParams.setBehavior(new BottomSheetBehavior());
        inflatedView.setLayoutParams(layoutParams);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(inflatedView);
        bottomSheetBehavior.setPeekHeight(224);
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        _cameraView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}