package com.shufanliu.imagelab;

import java.util.ArrayList;
import java.util.List;

import com.shufanliu.imagelab.Common;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;

public class ODRValue {
	
	private static final String TAG = "ODRValue";
	
	public static int numOfStrips = 4;
	public static int numOfSigFig = 3;
	private float[] meanI = new float[numOfStrips];
	private float[] sdI = new float[numOfStrips];
	private float[] ODR = new float[numOfStrips];
	private float[] eODR = new float[numOfStrips];
	private float bg;
	private float bgSd;
	private List<SampleStats> RGB;

	public void calculateRGB(Bitmap image) {
		
		// rotate the bitmap
		image = Common.rotateBitmap(image, 90);
		
		// setup the parameters
		int width = image.getWidth();
		int height = image.getHeight();
		
		// fit the parameters by calculating its ratio comparing to 480x640
		double hRatio = width / 480.0;
		double vRatio = height / 640.0 ;
		int sWidth = (int) (160 * hRatio);
		int sHeight = (int) (160 * vRatio);
		int YPos = (int) (240 * vRatio);
		int xPos = (int) (160 * hRatio);
		
		// Log.e(TAG, width + " : " + height + ", Ratio: " + hRatio + " : " + vRatio);
		
		Common.storeImage(image, "S");

		// get average RGB values
		int[] pixels = new int[sWidth * sHeight];
		image.getPixels(pixels, 0, sWidth, xPos, YPos, sWidth, sHeight);
		RGB = getMeanRGB(pixels);
		
		Common.storeImage(Bitmap.createBitmap(pixels, sWidth, sHeight, Bitmap.Config.RGB_565), "S0");
	}
	
	public void calculateRGBCircle(Bitmap image) {
		
		// rotate the bitmap
		image = Common.rotateBitmap(image, 90);
		
		// setup the parameters
		int width = image.getWidth();
		int height = image.getHeight();
		
		// fit the parameters by calculating its ratio comparing to 480x640
		double hRatio = width / 480.0;
		double vRatio = height / 640.0 ;
		int sWidth = (int) (160 * hRatio);
		int sHeight = (int) (160 * vRatio);
		int YPos = (int) (240 * vRatio);
		int xPos = (int) (160 * hRatio);
		
		// Log.e(TAG, width + " : " + height + ", Ratio: " + hRatio + " : " + vRatio);
		
		Common.storeImage(image, "S");

		// get average RGB values
		int[] pixels = new int[sWidth * sHeight];
		image.getPixels(pixels, 0, sWidth, xPos, YPos, sWidth, sHeight);
		
		// get pixels inside the circle
		int r = sWidth / 2;
		int x0 = xPos + r;
		int y0 = YPos + r;
		int n = 0;
		int[] pixelsC = new int[20079];
		for (int x = xPos; x < xPos + sWidth; x++) {
			for (int y = YPos; y < YPos + sHeight; y++) {
				if (Math.pow(x - x0, 2.0) + Math.pow(y - y0, 2.0) <= Math.pow(r, 2.0)) {
					pixelsC[n] = image.getPixel(x, y);
					n = n + 1;
				}
			}
		}
		Log.e(TAG, "n = " + n + " r = " + r);
		RGB = getMeanRGB(pixelsC);
		
		Common.storeImage(Bitmap.createBitmap(pixels, sWidth, sHeight, Bitmap.Config.RGB_565), "S0");
	}
	
	private class SampleStats {
		
		private float mean;
		private float sd;
		
		public SampleStats(float[] values) {
			mean = Common.getMean(values);
			sd = Common.getSampleStdDev(values);
		}
		
		public float getMean() {
			return mean;
		}
		
		public float getSD() {
			return sd;
		}
	}
	
	private List<SampleStats> getMeanRGB(int[] pixels) {
		List<SampleStats> sampleRGB = new ArrayList<SampleStats>();
		float[] sampleR = new float[pixels.length];
		float[] sampleG = new float[pixels.length];
		float[] sampleB = new float[pixels.length];
		for (int i = 0; i < pixels.length; i++) {
			sampleR[i] = Color.red(pixels[i]);
			sampleG[i] = Color.green(pixels[i]);
			sampleB[i] = Color.blue(pixels[i]);
		}
		sampleRGB.add(new SampleStats(sampleR));
		sampleRGB.add(new SampleStats(sampleG));
		sampleRGB.add(new SampleStats(sampleB));
		return sampleRGB;
	}
	
	private SampleStats getLuminosity(int[] pixels) {
		float[] sampleLuminosity = new float[pixels.length];
		for (int i = 0; i < pixels.length; i++) {
			float r = Color.red(pixels[i]);
			float g = Color.green(pixels[i]);
			float b = Color.blue(pixels[i]);
			sampleLuminosity[i] = (float) (0.3 * r + 0.59 * g + 0.11 * b);
		}
		SampleStats ss = new SampleStats(sampleLuminosity);
		return ss;
	}

	private static float getODR(float I, float bg) {
		// in case bg - I happens to be an extremely small number
		if (Math.abs(bg - I) < 0.00001) {
			return 0.0f;
		}
		return (bg - I) / bg;
	}
	
	private static float getODRError(float ODR, float IS, float IError) {
		//float ans = Math.abs(ODR * IError / IS);
		//Log.e(TAG, String.format("%f, %f, %f, %f", ODR, IS, IError, ans));
		return Math.abs(ODR * IError / IS);
	}

	public static int getNumOfStrips() {
		return numOfStrips;
	}
	
	public String getMeanRGBStr(int pos) {
		return Common.getSigFig(RGB.get(pos).mean, numOfSigFig);
	}
	
	public String getMeanRGBErrStr(int pos) {
		return Common.getSigFig(RGB.get(pos).sd, numOfSigFig);
	}
	
	public String getBgStr() {
		return Common.getSigFig(bg, numOfSigFig);
	}
	
	public String getBgSdStr() {
		return Common.getSigFig(bgSd, numOfSigFig);
	}

	public String[] getODRStr() {
		//String [] ODRStr = Common.getSigFig(ODR, numOfSigFig);
		//for (int i=0; i<ODR.length; i++) {
		//	Log.e(TAG, String.valueOf(ODR[i]) + " " + ODRStr[i]);
		//}
		return Common.getSigFig(ODR, numOfSigFig);
	}
	
	public String[] getEODRStr() {
		return Common.getSigFig(eODR, numOfSigFig);
	}
	
	public String[] getMeanIStr() {
		return Common.getSigFig(meanI, numOfSigFig);
	}
	
	public String[] getSDIStr() {
		return Common.getSigFig(sdI, numOfSigFig);
	}

	public static void setNumOfStrips(int numOfStrips) {
		ODRValue.numOfStrips = numOfStrips;
	}
	
}
