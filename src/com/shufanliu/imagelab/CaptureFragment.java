package com.shufanliu.imagelab;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import com.shufanliu.imagelab.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ZoomControls;

public class CaptureFragment extends Fragment {

	private static final String TAG = "CameraFragment";

	private View rootView;
	private Camera mCamera;
	private CameraPreview mPreview;
	private Button snapButton;
	private int currentZoomLevel = 0, maxZoomLevel = 0;
	private TextView statusText;
	private ImageView focusingFrameView;
	private FocusingFrame mFocusingFrame;
	private static ArrayAdapter<SummaryStatistics.Summary> adapter;

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
		final FrameLayout previewFrame = (FrameLayout) rootView
				.findViewById(R.id.previewFrameLayout);

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
			try {
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
			} catch (Exception exception) {
				Log.e(TAG, exception.getMessage());
			}
		} else {
			zoomControls.setVisibility(View.GONE);
		}
		mCamera.setParameters(params);

		previewFrame.addView(mPreview);

		// Setup focusing frame
		focusingFrameView = (ImageView) rootView
				.findViewById(R.id.focusingFrameView);

		mFocusingFrame = FocusingFrame.getFocusingFrame();
		Bitmap myBitmap = mFocusingFrame.getBitmap();

		// Attach the canvas to the ImageView
		focusingFrameView.setImageDrawable(new BitmapDrawable(getResources(),
				myBitmap));

		// Bring focusing frame to the front
		previewFrame.bringChildToFront(focusingFrameView);

		return rootView;
	}

	private void postAutoFocus() {
		rootView.findViewById(R.id.previewFrameLayout).postDelayed(doAutoFocus,
				1000);
	}

	// Mimic continuous auto-focusing
	private AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			boolean onPreview = mPreview.isOnPreview();
			Log.e(TAG,
					"onAutoFocus, onPreview = " + Boolean.toString(onPreview));
			postAutoFocus();
		}
	};

	private PictureCallback pictureCb = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			// Analyze picture taken
			FocusingFrame ff = FocusingFrame.getFocusingFrame();
			CaptureAnalysis captureAnalysis = ff.analyze(BitmapFactory
					.decodeByteArray(data, 0, data.length));
			// TODO: handle multiple ss
			SummaryStatistics ss = captureAnalysis.getSummaryStatsList().get(0);

			// Display result in a popup dialog
			new AlertDialog.Builder(rootView.getContext())
		    .setTitle("Capture Result")
		    .setMessage(ss.toString())
		    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // continue with delete
		        }
		     })
		     .show();

			// TODO:
			// save the result in db
			HistoryDataSource datasource = new HistoryDataSource(getActivity());
			datasource.open();
			datasource.createHistory(ss.toString());
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
				// onPreview = false;
				// mCamera.setPreviewCallback(null);
				// mCamera.stopPreview();

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
			Log.e(TAG,
					"try to focus, onPreview = " + Boolean.toString(onPreview));
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
