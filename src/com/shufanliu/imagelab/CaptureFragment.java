package com.shufanliu.imagelab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.shufanliu.imagelab.R;
import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ZoomControls;

public class CaptureFragment extends Fragment {

	private static final String TAG = "CameraFragment";
	
	private View rootView;
	public static Camera mCamera;
	private CameraPreview mPreview;
	private Button snapButton;
	private boolean onPreview = true;
	private int currentZoomLevel = 0, maxZoomLevel = 0;
	private TextView statusText;

	ImageScanner scanner;
	private Handler autoFocusHandler;
	private boolean barcodeScanned = false;

	static {
		System.loadLibrary("iconv");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater
				.inflate(R.layout.fragment_capture, container, false);
		
		// Make camera preview to have ratio of 4:3
		final FrameLayout previewFrame = (FrameLayout) rootView.findViewById(R.id.frameLayout1);
		previewFrame.post(new Runnable() {

	        @Override
	        public void run() {             
	            LayoutParams lp = previewFrame.getLayoutParams();
	            lp.width = (int) (previewFrame.getHeight() / 4.0 * 3);
	            previewFrame.setLayoutParams(lp);
	        }
	    });

		// Setup Camera
		autoFocusHandler = new Handler();
		cameraSetup();

		// Setup Barcode Scanner
		/* Instance barcode scanner */
		scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 3);
		scanner.setConfig(0, Config.Y_DENSITY, 3);

		// Setup status text
		statusText = (TextView) rootView.findViewById(R.id.statusText);

		// Setup the capture button
		snapButton = (Button) rootView.findViewById(R.id.refreshButton);
		snapButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (onPreview) {
					onPreview = false;
					mCamera.takePicture(null, null, mPicture);
					snapButton.setText("Recapture");
				} else {
					onPreview = true;
	                if (barcodeScanned) {
	                    barcodeScanned = false;
	                    statusText.setText("Scanning...");
	                }
					mCamera.setPreviewCallback(previewCb);
					mCamera.startPreview();
					snapButton.setText("Capture");
					mCamera.autoFocus(autoFocusCB);
				}
			}
		});

		return rootView;
	}

	private void cameraSetup() {

		// Create an instance of Camera
		mCamera = getCameraInstance();

		// Set Camera parameters
		Camera.Parameters params = mCamera.getParameters();
		//params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		params.setPreviewSize(640, 480);

		// Setup the ZoomControl
		ZoomControls zoomControls = (ZoomControls) rootView
				.findViewById(R.id.zoomControls1);

		if (params.isZoomSupported()) {
			maxZoomLevel = params.getMaxZoom();

			zoomControls.setIsZoomInEnabled(true);
			zoomControls.setIsZoomOutEnabled(true);

			zoomControls.setOnZoomInClickListener(new OnClickListener() {
				public void onClick(View v) {
					Camera.Parameters params = mCamera.getParameters();
					int currentZoomLevel = params.getZoom();
					if (currentZoomLevel < maxZoomLevel) {
						currentZoomLevel++;
						params.setZoom(currentZoomLevel);
						mCamera.setParameters(params);
					}
				}
			});

			zoomControls.setOnZoomOutClickListener(new OnClickListener() {
				public void onClick(View v) {
					Camera.Parameters params = mCamera.getParameters();
					currentZoomLevel = params.getZoom();
					if (currentZoomLevel > 0) {
						currentZoomLevel--;
						params.setZoom(currentZoomLevel);
						mCamera.setParameters(params);
					}
				}
			});
		} else {
			zoomControls.setVisibility(View.GONE);
		}

		mCamera.setParameters(params);

		// Create our Preview view and set it as the content of our
		// activity.
		mPreview = new CameraPreview(getActivity(), mCamera, previewCb,
				autoFocusCB);

		FrameLayout preview = (FrameLayout) rootView
				.findViewById(R.id.frameLayout1);
		preview.addView(mPreview);
		ImageView grid = (ImageView) rootView.findViewById(R.id.imageView1);
		preview.bringChildToFront(grid);

	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
		}
		return c; // returns null if camera is unavailable
	}

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			// calculate ODR
			ODRValue odrValue = new ODRValue();
			odrValue.calculateRGBCircle(BitmapFactory.decodeByteArray(data, 0,
					data.length));
			String meanR = odrValue.getMeanRGBStr(0);
			String meanG = odrValue.getMeanRGBStr(1);
			String meanB = odrValue.getMeanRGBStr(2);
			
			String meanRErr = odrValue.getMeanRGBErrStr(0);
			String meanGErr = odrValue.getMeanRGBErrStr(1);
			String meanBErr = odrValue.getMeanRGBErrStr(2);

			// display the result
			String outputText = String.format(
					"t = %s, R = %s กำ %s, G = %s กำ %s, B = %s กำ %s",
					new SimpleDateFormat("HH:mm:ss").format(new Date()),
					meanR, meanRErr, meanG, meanGErr, meanB,
					meanBErr);
			
			((TextView) getActivity().findViewById(R.id.sText1)).setText(String
					.format(" %s กำ %s ", meanR, meanRErr));
			((TextView) getActivity().findViewById(R.id.sText2)).setText(String
					.format(" %s กำ %s ", meanG, meanGErr));
			((TextView) getActivity().findViewById(R.id.sText3)).setText(String
					.format(" %s กำ %s ", meanB, meanRErr));


			// save the result in db
			HistoryDataSource datasource = new HistoryDataSource(getActivity());
			datasource.open();
			History history = datasource.createHistory(outputText);
			datasource.close();
			
//			// Scan for barcode
//			Camera.Parameters parameters = camera.getParameters();
//			Size size = parameters.getPreviewSize();
//			Image barcode = new Image(size.width, size.height, "Y800");
//			barcode.setData(data);
//
//			int result = scanner.scanImage(barcode);
//
//			if (result != 0) {
//				onPreview = false;
//				mCamera.setPreviewCallback(null);
//				mCamera.stopPreview();
//
//				SymbolSet syms = scanner.getResults();
//				for (Symbol sym : syms) {
//					statusText.setText("barcode result " + sym.getData());
//					barcodeScanned = true;
//				}
//			}
		}
	};

	PreviewCallback previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			/* Auto Scan Feature */
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();

			Image barcode = new Image(size.width, size.height, "Y800");
			barcode.setData(data);

			int result = scanner.scanImage(barcode);

			if (result != 0) {
//				onPreview = false;
//				mCamera.setPreviewCallback(null);
//				mCamera.stopPreview();

				SymbolSet syms = scanner.getResults();
				for (Symbol sym : syms) {
					statusText.setText("barcode result " + sym.getData());
					barcodeScanned = true;
				}
			}
		}
	};

	// Mimic continuous auto-focusing
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};
	
    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (onPreview)
                mCamera.autoFocus(autoFocusCB);
        }
    };

	@Override
	public void onPause() {
		super.onPause();
		releaseCamera(); // release the camera immediately on pause event
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mCamera == null) {
			cameraSetup();
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mPreview.getHolder().removeCallback(mPreview);
			mCamera.setPreviewCallback(null);
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

}
