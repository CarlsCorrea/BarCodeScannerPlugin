//
//  BarCodeScannerPlugin.swift
//  HelloCordova
//
//  Created by Carlos Correa on 24/06/2020.
//

import Foundation

let cameraManager = CameraViewController()
var parentView: UIView? = nil;
var view = UIView(frame: parentView!.bounds);
var cameraStarted: Bool = false;

@objc(BarCodeScannerPlugin) class BarCodeScannerPlugin : CDVPlugin { // Declare the namespace you want to expose to cordova, when you call the Plugin
    
    @objc(startCamera:)
    func startCamera(command: CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult (status: CDVCommandStatus_ERROR);

        UIApplication.shared.isIdleTimerDisabled = true
        parentView = nil;

        parentView = UIView(frame: self.webView.frame)
        webView?.superview?.addSubview(parentView!)
        parentView!.addSubview(view)
        parentView!.isUserInteractionEnabled = true

        webView?.isOpaque = false

        parentView?.addSubview(cameraManager.view)
        
        cameraStarted = true;

        print("resultsText \(cameraManager.resultsText)")
        
        
        pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: cameraManager.resultsText);
        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId);
    }

}

