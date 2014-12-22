package com.shufanliu.imagelab;

import android.graphics.Color;

public class Settings {
	
	private static Settings mSettings= null;
	
	// Preview settings
	public int previewWidth = 480;
	public int previewHeight = 640;
	public int previewDisplayOrientation = 90;

	// Focusing frame layout settings
	public int forcusingFrameID = 0;
	public int focusingFrameColor = Color.RED;
	public int focusingFrameBorder = 2;
	
	// RGB channel parameters
	public int redChannel = 100;
	public int greenChannel = 100;
	public int blueChannel = 100;
	
	// Bootstrapping parameters
	public int sampleSize = 1000;
	
	// Number of Sig. Figs
	public int numOfSigFig = 4;
	
	public static synchronized Settings getInstance(){
    	if(null == mSettings){
    		mSettings = new Settings();
    	}
    	return mSettings;
    }
	
	// Capture Layout Settings
	public enum CaptureLayout {
		SQUARE,
		CIRCLE,
		TEST_STRIP
	}
	
	public CaptureLayout captureLayout = CaptureLayout.CIRCLE;
	
	// Settings.getInstance().
}
