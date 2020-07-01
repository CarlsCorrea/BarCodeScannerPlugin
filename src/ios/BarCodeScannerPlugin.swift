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
    
    @objc(sendResult:)
    func sendResult(result:String) {
        var pluginResult = CDVPluginResult (status: CDVCommandStatus_ERROR);
        
        
        pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: result);
        self.commandDelegate!.send(pluginResult, callbackId: callbackId);

        viewController.dismiss(animated: true, completion: nil)
    }
    
    @objc(scan:)
    func scan(command: CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult (status: CDVCommandStatus_ERROR);
        
        callbackId = command.callbackId

        cameraManager.delegate = self
        
        UIApplication.shared.isIdleTimerDisabled = true
        
        let navigationController = UINavigationController(rootViewController: cameraManager)
        navigationController.modalPresentationStyle = .fullScreen
        navigationController.setNavigationBarHidden(true, animated: false)
        viewController.present(navigationController, animated: true)
        
        cameraStarted = true;

        pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: cameraManager.resultsText);
        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId);
    }
    
}

