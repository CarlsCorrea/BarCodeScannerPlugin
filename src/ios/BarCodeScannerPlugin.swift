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
var callbackId:String=""

@objc(BarCodeScannerPlugin) class BarCodeScannerPlugin : CDVPlugin, CameraViewControllerDelegate {
    
    func sendResult(result:String) {
        print("resultsText = \(result)")
        
        var pluginResult = CDVPluginResult (status: CDVCommandStatus_ERROR);
        
        
        pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: cameraManager.resultsText);
        self.commandDelegate!.send(pluginResult, callbackId: callbackId);
    }
    
    @objc(scan:)
    func scan(command: CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult (status: CDVCommandStatus_ERROR);
        callbackId = command.callbackId

        UIApplication.shared.isIdleTimerDisabled = true
        parentView = nil;

        parentView = UIView(frame: self.webView.frame)
        webView?.superview?.addSubview(parentView!)
        parentView!.addSubview(view)
        parentView!.isUserInteractionEnabled = true

        webView?.isOpaque = false

        parentView?.addSubview(cameraManager.view)
        
        cameraStarted = true;

        pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: cameraManager.resultsText);
        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId);
    }
    
}

