//
//  UIUtilities.swift
//  HelloCordova
//
//  Created by Carlos Correa on 26/06/2020.
//

import Foundation
import AVFoundation
import CoreVideo
import MLKit

public class UIUtilities {
    public static func addRectangle(_ rectangle: CGRect, to view: UIView, color: UIColor) {
        let rectangleView = UIView(frame: rectangle)
        rectangleView.layer.cornerRadius = Constants.rectangleViewCornerRadius
        rectangleView.alpha = Constants.rectangleViewAlpha
        rectangleView.backgroundColor = color
        view.addSubview(rectangleView)
    }
    
    public static func imageOrientation(
      fromDevicePosition devicePosition: AVCaptureDevice.Position = .back
    ) -> UIImage.Orientation {
        var deviceOrientation = UIDevice.current.orientation
        if deviceOrientation == .faceDown || deviceOrientation == .faceUp || deviceOrientation == .unknown
        {
            deviceOrientation = currentUIOrientation()
        }
        switch deviceOrientation {
            case .portrait:
        return devicePosition == .front ? .leftMirrored : .right
            case .landscapeLeft:
        return devicePosition == .front ? .downMirrored : .up
            case .portraitUpsideDown:
        return devicePosition == .front ? .rightMirrored : .left
            case .landscapeRight:
        return devicePosition == .front ? .upMirrored : .down
            case .faceDown, .faceUp, .unknown:
        return .up
            @unknown default:
            fatalError()
        }
    }

    // MARK: - Private

    private static func currentUIOrientation() -> UIDeviceOrientation {
      let deviceOrientation = { () -> UIDeviceOrientation in
        switch UIApplication.shared.statusBarOrientation {
        case .landscapeLeft:
          return .landscapeRight
        case .landscapeRight:
          return .landscapeLeft
        case .portraitUpsideDown:
          return .portraitUpsideDown
        case .portrait, .unknown:
          return .portrait
        @unknown default:
          fatalError()
        }
      }
      guard Thread.isMainThread else {
        var currentOrientation: UIDeviceOrientation = .portrait
        DispatchQueue.main.sync {
          currentOrientation = deviceOrientation()
        }
        return currentOrientation
      }
      return deviceOrientation()
    }
    
    // MARK: - Constants
    private enum Constants {
        static let circleViewAlpha: CGFloat = 0.7
        static let rectangleViewAlpha: CGFloat = 0.3
        static let shapeViewAlpha: CGFloat = 0.3
        static let rectangleViewCornerRadius: CGFloat = 10.0
    }
}
