package com.shufanliu.imagelab;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import com.shufanliu.imagelab.R;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
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
	private Camera mCamera;
	private CameraPreview mPreview;
	private Button snapButton;
	private Button focusButton;
	private int currentZoomLevel = 0, maxZoomLevel = 0;
	private TextView statusText;

	ImageScanner scanner;
	
	private boolean barcodeScanned = false;

	static {
		System.loadLibrary("iconv");
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
	            mPreview.startPreview();
	        }
	    });

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
				boolean onPreview = mPreview.isOnPreview();
				if (onPreview) {
					mCamera.takePicture(null, null, pictureCb);
					snapButton.setText("Recapture");
				} else {
	                if (barcodeScanned) {
	                    barcodeScanned = false;
	                    statusText.setText("Scanning...");
	                }
					mPreview.startPreview();
					postAutoFocus();
					snapButton.setText("Capture");
				}
			}
		});
		
		// Setup the focus button
		focusButton = (Button) rootView.findViewById(R.id.button1);
		focusButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean onPreview = mPreview.isOnPreview();
				if (onPreview) {
					Log.e(TAG, "try to focus");
					mCamera.autoFocus(autoFocusCB);
				}
			}
		});
		
		// Create our Preview view and set it as the content of our
		// activity.
		mCamera = getCameraInstance(); 
		mPreview = new CameraPreview(getActivity(), mCamera, previewCb,
				autoFocusCB);
		
		// Setup the ZoomControl
		Camera.Parameters params = mCamera.getParameters();

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

		previewFrame.addView(mPreview);
		ImageView grid = (ImageView) rootView.findViewById(R.id.imageView1);
		previewFrame.bringChildToFront(grid);

		return rootView;
	}
	
	private void postAutoFocus() {
		rootView.findViewById(R.id.frameLayout1).postDelayed(doAutoFocus, 1000);
	}

	// Mimic continuous auto-focusing
	private AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			boolean onPreview = mPreview.isOnPreview();
			Log.e(TAG, "onAutoFocus, onPreview = " + Boolean.toString(onPreview));
			postAutoFocus();
		}
	};

	private PictureCallback pictureCb = new PictureCallback() {

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
			
			mPreview.stopPreview();
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

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
        	boolean onPreview = mPreview.isOnPreview();
        	Log.e(TAG, "try to focus, onPreview = " + Boolean.toString(onPreview));
            if (onPreview)
                mCamera.autoFocus(autoFocusCB);
        }
    };
       
	@Override
	public void onPause() {
		super.onPause();
		mPreview.stopPreview();
		mPreview.getHolder().removeCallback(mPreview);
		mCamera.setPreviewCallback(null);
		mCamera.release();
		mCamera = null;
	}

	@Override
	public void onResume() {
		super.onResume();
		mCamera.setPreviewCallback(previewCb);
		mPreview.startPreview();
		postAutoFocus();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(2);
	}
}
