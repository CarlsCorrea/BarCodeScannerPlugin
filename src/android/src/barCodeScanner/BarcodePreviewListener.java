package com.carlscorrea.cordova.plugin;


import com.camerakit.CameraKitView;

public class BarcodePreviewListener implements CameraKitView.PreviewListener{

    private BarcodeScannerActivity _activity;
    public BarcodePreviewListener(BarcodeScannerActivity activity){
        _activity = activity;
    }
    @Override
    public void onStart() {
        _activity.startHandler();
    }

    @Override
    public void onStop() {

        _activity.stopHandler();
    }
}