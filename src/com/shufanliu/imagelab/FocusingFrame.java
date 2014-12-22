package com.shufanliu.imagelab;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class FocusingFrame {

	private static FocusingFrame mFocusingFrame = null;
	private String name;
	private List<ScanArea> scanAreaList;
	private CaptureAnalysis captureAnalysis;

	public static FocusingFrame getFocusingFrame() {
		mFocusingFrame = new FocusingFrame();
		mFocusingFrame.load();
		return mFocusingFrame;
	}

	public void load() {
		scanAreaList = new ArrayList<ScanArea>();
		// TODO: handle this in a db

		switch (Settings.getInstance().captureLayout) {
		case SQUARE:
			name = "Square";
			scanAreaList.add(new RectScanArea(0, "Main", 160, 240, 320, 400));
			break;
		case CIRCLE:
			name = "Circle";
			scanAreaList.add(new CircleScanArea(0, "Main", 240, 320, 80));
			break;
		case TEST_STRIP:
			name = "Test Strip";
			//scanAreaList.add(new TestStripScanArea(0, "Main"));
		}
	}

	public Bitmap getBitmap() {
		Bitmap myBitmap = Bitmap.createBitmap(Settings.getInstance().previewWidth,
				Settings.getInstance().previewHeight, Bitmap.Config.ARGB_8888);

		for (ScanArea scanArea : scanAreaList) {
			if (scanArea instanceof RectScanArea) {
				drawRectScanArea(myBitmap, (RectScanArea) scanArea);
			}
			if (scanArea instanceof CircleScanArea) {
				drawCircleScanArea(myBitmap, (CircleScanArea) scanArea);
			}
		}
		return myBitmap;
	}

	private void drawRectScanArea(Bitmap myBitmap, RectScanArea scanArea) {
		Canvas tempCanvas = new Canvas(myBitmap);
		Paint myPaint = new Paint();
		myPaint.setStyle(Paint.Style.STROKE);
		myPaint.setColor(Settings.getInstance().focusingFrameColor);
		tempCanvas.drawRect(scanArea.getRect(), myPaint);
	}

	private void drawCircleScanArea(Bitmap myBitmap, CircleScanArea scanArea) {
		Canvas tempCanvas = new Canvas(myBitmap);
		Paint myPaint = new Paint();
		myPaint.setStyle(Paint.Style.STROKE);
		myPaint.setColor(Settings.getInstance().focusingFrameColor);
		tempCanvas.drawCircle(scanArea.getX1(), scanArea.getY1(),
				scanArea.getRadius(), myPaint);
	}

	public CaptureAnalysis analyze(Bitmap image) {
		captureAnalysis = new CaptureAnalysis(name, image, scanAreaList);
		return captureAnalysis;
	}

}
