//
//  CameraViewController.swift
//  HelloCordova
//
//  Created by Carlos Correa on 25/06/2020.
//

import AVFoundation
import CoreVideo
import MLKit

protocol CameraViewControllerDelegate: class {
    func sendResult(result:String)
}

@objc(CameraViewController)
class CameraViewController: UIViewController {
    
    private var previewLayer: AVCaptureVideoPreviewLayer!
    private lazy var captureSession = AVCaptureSession()
    private lazy var sessionQueue = DispatchQueue(label: Constant.sessionQueueLabel, qos: .background)
    private var isFrontCamera = false
    private var lastFrame: CMSampleBuffer?
    
    weak var delegate: CameraViewControllerDelegate?
    
    /// A string holding current results from detection.
    var resultsText = ""
    
    private lazy var previewOverlayView: UIImageView = {
        precondition(isViewLoaded)
        let previewOverlayView = UIImageView(frame: .zero)
        previewOverlayView.contentMode = UIView.ContentMode.scaleAspectFill
        previewOverlayView.translatesAutoresizingMaskIntoConstraints = false
        return previewOverlayView
    }()

    private lazy var annotationOverlayView: UIView = {
        precondition(isViewLoaded)
        let annotationOverlayView = UIView(frame: .zero)
        annotationOverlayView.translatesAutoresizingMaskIntoConstraints = false
        return annotationOverlayView
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(didInterrupted),
                                               name: .AVCaptureSessionWasInterrupted,
                                               object: captureSession)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(didStarted),
                                               name: .AVCaptureSessionDidStartRunning,
                                               object: captureSession)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(retrieveError),
                                               name: .AVCaptureSessionRuntimeError,
                                               object: captureSession)
        
        
        
        self.previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        previewLayer.videoGravity = .resizeAspectFill
        self.view.layer.addSublayer(previewLayer)
        
        setUpPreviewOverlayView()
        setUpAnnotationOverlayView()
        setUpCaptureSessionInput()
        setUpCaptureSessionOutput()
    }

    @objc
    func retrieveError() {
        print("retrieveError: \(captureSession.debugDescription)")
    }
    
    @objc
    func didStarted() {
        print("started: \(captureSession.isRunning)")
    }
    
    @objc
    func didInterrupted() {
        print("didInterrupted: \(captureSession.isInterrupted)")
    }
    
    private func setUpPreviewOverlayView() {
      self.view.addSubview(previewOverlayView)
      NSLayoutConstraint.activate([
        previewOverlayView.centerXAnchor.constraint(equalTo: self.view.centerXAnchor),
        previewOverlayView.centerYAnchor.constraint(equalTo: self.view.centerYAnchor),
        previewOverlayView.leadingAnchor.constraint(equalTo: self.view.leadingAnchor),
        previewOverlayView.trailingAnchor.constraint(equalTo: self.view.trailingAnchor),

      ])
    }

    private func setUpAnnotationOverlayView() {
      self.view.addSubview(annotationOverlayView)
      NSLayoutConstraint.activate([
        annotationOverlayView.topAnchor.constraint(equalTo: self.view.topAnchor),
        annotationOverlayView.leadingAnchor.constraint(equalTo: self.view.leadingAnchor),
        annotationOverlayView.trailingAnchor.constraint(equalTo: self.view.trailingAnchor),
        annotationOverlayView.bottomAnchor.constraint(equalTo: self.view.bottomAnchor),
      ])
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        self.previewLayer.frame = self.view.frame;
        self.previewLayer.position = CGPoint(x: self.previewLayer.frame.midX, y: self.previewLayer.frame.midY);
    }
    
    override func viewWillAppear(_ animated: Bool) {
        startSession()
        
    }

    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)

        stopSession()
    }
    
    private func startSession() {
        sessionQueue.async{
        self.captureSession.startRunning()
      }
    }

    private func stopSession() {
      sessionQueue.async {
        self.captureSession.stopRunning()
      }
    }
        
    private func setUpCaptureSessionOutput() {
      sessionQueue.async {
        self.captureSession.beginConfiguration()
        self.captureSession.sessionPreset = AVCaptureSession.Preset.medium

        let output = AVCaptureVideoDataOutput()
        output.videoSettings = [
          (kCVPixelBufferPixelFormatTypeKey as String): kCVPixelFormatType_32BGRA,
        ]
        output.alwaysDiscardsLateVideoFrames = true
        let outputQueue = DispatchQueue(label: Constant.videoDataOutputQueueLabel)
        output.setSampleBufferDelegate(self, queue: outputQueue)
        guard self.captureSession.canAddOutput(output) else {
          print("Failed to add capture session output.")
          return
        }
        self.captureSession.addOutput(output)
        self.captureSession.commitConfiguration()
      }
    }
    
    private func setUpCaptureSessionInput() {
      sessionQueue.async {
        let cameraPosition: AVCaptureDevice.Position = self.isFrontCamera ? .front : .back
        guard let device = self.captureDevice(forPosition: cameraPosition) else {
          print("Failed to get capture device for camera position: \(cameraPosition)")
          return
        }
        do {
          self.captureSession.beginConfiguration()
          let currentInputs = self.captureSession.inputs
          for input in currentInputs {
            self.captureSession.removeInput(input)
          }

          let input = try AVCaptureDeviceInput(device: device)
          guard self.captureSession.canAddInput(input) else {
            print("Failed to add capture session input.")
            return
          }
          self.captureSession.addInput(input)
          self.captureSession.commitConfiguration()
        } catch {
          print("Failed to create capture device input: \(error.localizedDescription)")
        }
      }
    }

    private func captureDevice(forPosition position: AVCaptureDevice.Position) -> AVCaptureDevice? {
      if #available(iOS 10.0, *) {
        let discoverySession = AVCaptureDevice.DiscoverySession(
          deviceTypes: [.builtInWideAngleCamera],
          mediaType: .video,
          position: .unspecified
        )
        return discoverySession.devices.first { $0.position == position }
      }
      return nil
    }
    
    // MARK: On-Device Detections

    private func scanBarcodesOnDevice(in image: VisionImage, width: CGFloat, height: CGFloat) {
      // Define the options for a barcode detector.
      let format = BarcodeFormat.all
      let barcodeOptions = BarcodeScannerOptions(formats: format)

      // Create a barcode scanner.
      let barcodeScanner = BarcodeScanner.barcodeScanner(options: barcodeOptions)
      var barcodes: [Barcode]
      do {
        barcodes = try barcodeScanner.results(in: image)
      } catch let error {
        print("Failed to scan barcodes with error: \(error.localizedDescription).")
        return
      }
      DispatchQueue.main.sync {
        self.updatePreviewOverlayView()
        self.removeDetectionAnnotations()
      }
      guard !barcodes.isEmpty else {
        print("Barcode scanner returrned no results.")
        return
      }
      DispatchQueue.main.sync {
        for barcode in barcodes {
          let normalizedRect = CGRect(
            x: barcode.frame.origin.x / width,
            y: barcode.frame.origin.y / height,
            width: barcode.frame.size.width / width,
            height: barcode.frame.size.height / height
          )
          let convertedRect = self.previewLayer.layerRectConverted(
            fromMetadataOutputRect: normalizedRect
          )
          UIUtilities.addRectangle(
            convertedRect,
            to: self.annotationOverlayView,
            color: UIColor.green
          )
          let label = UILabel(frame: convertedRect)
          label.text = barcode.rawValue
          label.adjustsFontSizeToFitWidth = true
            
          self.resultsText = barcode.rawValue!
          self.annotationOverlayView.addSubview(label)
          delegate?.sendResult(result: self.resultsText)
        }
      }
    }
    
    /// Removes the detection annotations from the annotation overlay view.
    private func removeDetectionAnnotations() {
      for annotationView in annotationOverlayView.subviews {
        annotationView.removeFromSuperview()
      }
    }
    
}

@objc(CameraViewController)
extension CameraViewController: AVCaptureVideoDataOutputSampleBufferDelegate {
    
    @objc
    func captureOutput(_ output: AVCaptureOutput, didDrop sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        print("passou")
    }
    
    @objc
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        guard let imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else {
          print("Failed to get image buffer from sample buffer.")
          return
        }
        lastFrame = sampleBuffer
        let visionImage = VisionImage(buffer: sampleBuffer)
        let orientation = UIUtilities.imageOrientation(
          fromDevicePosition: isFrontCamera ? .front : .back
        )

        visionImage.orientation = orientation
        let imageWidth = CGFloat(CVPixelBufferGetWidth(imageBuffer))
        let imageHeight = CGFloat(CVPixelBufferGetHeight(imageBuffer))
        
        scanBarcodesOnDevice(in: visionImage, width: imageWidth, height: imageHeight)
    }
    
    private func updatePreviewOverlayView() {
        guard let lastFrame = lastFrame,
            let imageBuffer = CMSampleBufferGetImageBuffer(lastFrame)
        else {
            return
        }
        let ciImage = CIImage(cvPixelBuffer: imageBuffer)
        let context = CIContext(options: nil)
        guard let cgImage = context.createCGImage(ciImage, from: ciImage.extent) else {
            return
        }
        let rotatedImage = UIImage(cgImage: cgImage, scale: Constant.originalScale, orientation: .right)
        if isFrontCamera {
            guard let rotatedCGImage = rotatedImage.cgImage else {
                return
        }
        let mirroredImage = UIImage(cgImage: rotatedCGImage, scale: Constant.originalScale, orientation: .leftMirrored)
            previewOverlayView.image = mirroredImage
        } else {
            previewOverlayView.image = rotatedImage
        }
    }
    
}


private enum Constant {
    static let sessionQueueLabel = "com.google.mlkit.visiondetector.SessionQueue"
    static let videoDataOutputQueueLabel = "com.google.mlkit.visiondetector.VideoDataOutputQueue"
    static let originalScale: CGFloat = 1.0
}
