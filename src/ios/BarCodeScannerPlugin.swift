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
var callbackId:String=""

@objc(BarCodeScannerPlugin) class BarCodeScannerPlugin : CDVPlugin, CameraViewControllerDelegate {
    
    @objc(sendResult:error:)
    func sendResult(result:String,error:String) {
        var pluginResult = CDVPluginResult (status: CDVCommandStatus_ERROR);
        if error.isEmpty {
            let resultArray = [result]
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: resultArray)
        } else {
            let resultErrorArray = [error]
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: resultErrorArray);
        }
        self.commandDelegate!.send(pluginResult, callbackId: callbackId);
        
        viewController.dismiss(animated: true, completion: nil)
    }
    
    @objc(scan:)
    func scan(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
        
        cameraManager.delegate = self
        UIApplication.shared.isIdleTimerDisabled = true
        
        let navigationController = UINavigationController(rootViewController: cameraManager)
        navigationController.modalPresentationStyle = .fullScreen
        navigationController.setNavigationBarHidden(true, animated: false)
        viewController.present(navigationController, animated: true)
    }
    
}

