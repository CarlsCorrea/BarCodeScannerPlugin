//
//  BarCodeScannerPlugin.swift
//  HelloCordova
//
//  Created by Carlos Correa on 24/06/2020.
//

import Foundation
import AVFoundation

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
        
        cameraManager.stopSession()
        
        viewController.dismiss(animated: true, completion: nil)
    }
    
    @objc(scan:)
    func scan(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
        
        let lens:Int = command.arguments![0] as! Int;
        let canvas:Int = command.arguments![1] as! Int;
        let flash:Int = command.arguments![2] as! Int;
        
        cameraManager.canvas = (canvas == 1)
        cameraManager.isFrontCamera = (lens == 1)
        cameraManager.flash = (flash == 1)
        
        cameraManager.delegate = self
        UIApplication.shared.isIdleTimerDisabled = true
        
        let navigationController = UINavigationController(rootViewController: cameraManager)
        navigationController.modalPresentationStyle = .fullScreen

        cameraManager.navigationController?.setNavigationBarHidden(false, animated: true)
        cameraManager.navigationController?.navigationBar.barStyle = UIBarStyle.default
        cameraManager.navigationController?.navigationBar.barTintColor = UIColor.black
        
        let rightBtn = UIButton(type: .system)
        rightBtn.setTitle("Close", for: .normal)
        rightBtn.addTarget(self, action: #selector(closeTapped), for: .touchUpInside)
        
        let rightButton = UIBarButtonItem(customView: rightBtn)
        cameraManager.navigationItem.rightBarButtonItem = rightButton
        
        let leftBtn = UIButton(type: .system)
        leftBtn.setTitle("Flash", for: .normal)
        leftBtn.addTarget(self, action: #selector(flashTapped), for: .touchUpInside)
        
        let leftButton = UIBarButtonItem(customView: leftBtn)
        cameraManager.navigationItem.leftBarButtonItem = leftButton
        
        cameraManager.setUpPreviewOverlayView()
        cameraManager.setUpAnnotationOverlayView()
        cameraManager.setUpCaptureSessionInput()
        cameraManager.setUpCaptureSessionOutput()
        
        cameraManager.startSession()
            
        viewController.present(navigationController, animated: true)
    
    }
    
    @objc func closeTapped() {
        sendResult(result: "", error: "")
    }
    
    @objc func flashTapped() {
        cameraManager.toggleFlash()
    }

}

