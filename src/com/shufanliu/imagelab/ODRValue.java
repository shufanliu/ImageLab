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

	/*
	 * The preview size for Nexus 7 is 2592 x 1944.
	 * The following properties are for Nexus 7
	 * sWidth = 41
	 * sHeight = 115
	 * initYPos = 1006
	 * yOffset = 230
	 */
	public void calculateODR(Bitmap image) {
		
		// rotate the bitmap
		image = Common.rotateBitmap(image, 90);
		
		// setup the parameters
		int width = image.getWidth();
		int height = image.getHeight();
		
		// fit the parameters by comparing with Nexus 7
		double hRatio = width / 1944.0;
		double vRatio = height / 2592.0 ;
		int sWidth = (int) (41 * hRatio);
		int sHeight = (int) (115 * vRatio);
		int initYPos = (int) (1006 * vRatio);
		int yOffset = (int) (230 * vRatio);
		int xPos0 = (int) (951 * hRatio);
		int xPos1 = (int) (666 * hRatio);
		int xPos2 = (int) (834 * hRatio);
		int xPos3 = (int) (1087 * hRatio);
		int xPos4 = (int) (1255 * hRatio); 
		
		// Log.e(TAG, width + " : " + height + ", Ratio: " + hRatio + " : " + vRatio);
		
		Common.storeImage(image, "S");

		// get the background darkness
		List<SampleStats> bgClusters = new ArrayList<SampleStats>();
		
		int[] pixelsBg = new int[sWidth * sHeight];
		image.getPixels(pixelsBg, 0, sWidth, xPos0, initYPos, sWidth, sHeight);
		bgClusters.add(getLuminosity(pixelsBg));
		
		Common.storeImage(Bitmap.createBitmap(pixelsBg, sWidth, sHeight, Bitmap.Config.RGB_565), "S0-1");
		
		image.getPixels(pixelsBg, 0, sWidth, xPos0, initYPos + yOffset, sWidth, sHeight);
		bgClusters.add(getLuminosity(pixelsBg));
		
		Common.storeImage(Bitmap.createBitmap(pixelsBg, sWidth, sHeight, Bitmap.Config.RGB_565), "S0-2");
		
		image.getPixels(pixelsBg, 0, sWidth, xPos0, initYPos + yOffset * 2, sWidth, sHeight);
		bgClusters.add(getLuminosity(pixelsBg));
		
		Common.storeImage(Bitmap.createBitmap(pixelsBg, sWidth, sHeight, Bitmap.Config.RGB_565), "S0-3");
		
		float[] bgMeans = new float[3];
		float[] bgSDs = new float[3];
		for (int i = 0; i < bgMeans.length; i++) {
			bgMeans[i] = bgClusters.get(i).getMean();
			bgSDs[i] = bgClusters.get(i).getSD();
		}
		bg = Common.getMean(bgMeans);
		bgSd = Common.getMean(bgSDs);

		// get ODR of strip #1
		List<SampleStats> I1Clusters = new ArrayList<SampleStats>();
		
		int[] pixels1 = new int[sWidth * sHeight];
		image.getPixels(pixels1, 0, sWidth, xPos1, initYPos, sWidth, sHeight);
		I1Clusters.add(getLuminosity(pixels1));
		
		Common.storeImage(Bitmap.createBitmap(pixels1, sWidth, sHeight, Bitmap.Config.RGB_565), "S1-1");
		
		image.getPixels(pixels1, 0, sWidth, xPos1, initYPos + yOffset, sWidth, sHeight);
		I1Clusters.add(getLuminosity(pixels1));
		
		Common.storeImage(Bitmap.createBitmap(pixels1, sWidth, sHeight, Bitmap.Config.RGB_565), "S1-2");
		
		image.getPixels(pixels1, 0, sWidth, xPos1, initYPos + yOffset * 2, sWidth, sHeight);
		I1Clusters.add(getLuminosity(pixels1));
		
		Common.storeImage(Bitmap.createBitmap(pixels1, sWidth, sHeight, Bitmap.Config.RGB_565), "S1-3");
		
		float[] I1Means = new float[3];
		float[] I1SDs = new float[3];
		for (int i = 0; i < I1Means.length; i++) {
			I1Means[i] = I1Clusters.get(i).getMean();
			I1SDs[i] = I1Clusters.get(i).getSD();
		}
		meanI[0] = Common.getMean(I1Means);
		ODR[0] = getODR(meanI[0], bg);
		sdI[0] = Common.getMean(I1SDs);
		eODR[0] = getODRError(ODR[0], meanI[0], sdI[0]);

		// get ODR of strip #2
		List<SampleStats> I2Clusters = new ArrayList<SampleStats>();
		
		int[] pixels2 = new int[sWidth * sHeight];
		image.getPixels(pixels2, 0, sWidth, xPos2, initYPos, sWidth, sHeight);
		I2Clusters.add(getLuminosity(pixels2));
		
		Common.storeImage(Bitmap.createBitmap(pixels2, sWidth, sHeight, Bitmap.Config.RGB_565), "S2-1");
		
		image.getPixels(pixels2, 0, sWidth, xPos2, initYPos + yOffset, sWidth, sHeight);
		I2Clusters.add(getLuminosity(pixels2));
		
		Common.storeImage(Bitmap.createBitmap(pixels2, sWidth, sHeight, Bitmap.Config.RGB_565), "S2-2");
		
		image.getPixels(pixels2, 0, sWidth, xPos2, initYPos + yOffset * 2, sWidth, sHeight);
		I2Clusters.add(getLuminosity(pixels2));
		
		Common.storeImage(Bitmap.createBitmap(pixels2, sWidth, sHeight, Bitmap.Config.RGB_565), "S2-3");
		
		float[] I2Means = new float[3];
		float[] I2SDs = new float[3];
		for (int i = 0; i < I2Means.length; i++) {
			I2Means[i] = I2Clusters.get(i).getMean();
			I2SDs[i] = I2Clusters.get(i).getSD();
		}
		meanI[1] = Common.getMean(I2Means);
		ODR[1] = getODR(meanI[1], bg);
		sdI[1] = Common.getMean(I2SDs);
		eODR[1] = getODRError(ODR[1], meanI[1], sdI[1]);

		// get ODR of strip #3
		List<SampleStats> I3Clusters = new ArrayList<SampleStats>();
		
		int[] pixels3 = new int[sWidth * sHeight];
		image.getPixels(pixels3, 0, sWidth, xPos3, initYPos, sWidth, sHeight);
		I3Clusters.add(getLuminosity(pixels3));
		
		Common.storeImage(Bitmap.createBitmap(pixels3, sWidth, sHeight, Bitmap.Config.RGB_565), "S3-1");
		
		image.getPixels(pixels3, 0, sWidth, xPos3, initYPos + yOffset, sWidth, sHeight);
		I3Clusters.add(getLuminosity(pixels3));
		
		Common.storeImage(Bitmap.createBitmap(pixels3, sWidth, sHeight, Bitmap.Config.RGB_565), "S3-2");
		
		image.getPixels(pixels3, 0, sWidth, xPos3, initYPos + yOffset * 2, sWidth, sHeight);
		I3Clusters.add(getLuminosity(pixels3));
		
		Common.storeImage(Bitmap.createBitmap(pixels3, sWidth, sHeight, Bitmap.Config.RGB_565), "S3-3");
		
		float[] I3Means = new float[3];
		float[] I3SDs = new float[3];
		for (int i = 0; i < I3Means.length; i++) {
			I3Means[i] = I3Clusters.get(i).getMean();
			I3SDs[i] = I3Clusters.get(i).getSD();
		}
		meanI[2] = Common.getMean(I3Means);
		ODR[2] = getODR(meanI[2], bg);
		sdI[2] = Common.getMean(I3SDs);
		eODR[2] = getODRError(ODR[2], meanI[2], sdI[2]);

		// get ODR of strip #4
		List<SampleStats> I4Clusters = new ArrayList<SampleStats>();
		
		int[] pixels4 = new int[sWidth * sHeight];
		image.getPixels(pixels4, 0, sWidth, xPos4, initYPos, sWidth, sHeight);
		I4Clusters.add(getLuminosity(pixels4));
		
		Common.storeImage(Bitmap.createBitmap(pixels4, sWidth, sHeight, Bitmap.Config.RGB_565), "S4-1");
		
		image.getPixels(pixels4, 0, sWidth, xPos4, initYPos + yOffset, sWidth, sHeight);
		I4Clusters.add(getLuminosity(pixels4));
		
		Common.storeImage(Bitmap.createBitmap(pixels4, sWidth, sHeight, Bitmap.Config.RGB_565), "S4-2");
		
		image.getPixels(pixels4, 0, sWidth, xPos4, initYPos + yOffset * 2, sWidth, sHeight);
		I4Clusters.add(getLuminosity(pixels4));
		
		Common.storeImage(Bitmap.createBitmap(pixels4, sWidth, sHeight, Bitmap.Config.RGB_565), "S4-3");
		
		float[] I4Means = new float[3];
		float[] I4SDs = new float[3];
		for (int i = 0; i < I4Means.length; i++) {
			I4Means[i] = I4Clusters.get(i).getMean();
			I4SDs[i] = I4Clusters.get(i).getSD();
		}
		meanI[3] = Common.getMean(I4Means);
		ODR[3] = getODR(meanI[3], bg);
		sdI[3] = Common.getMean(I4SDs);
		eODR[3] = getODRError(ODR[3], meanI[3], sdI[3]);
	}
	
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
	
	private float getConcentration(float ODR) {
		return ODR * ODR * 10000;
	}
	
	/* Assume the array has length of 4 */
	public float getCurve() {
		
		float[] avgODR = new float[numOfStrips];
		avgODR[0] = (ODR[0] + ODR[2]) / 2;
		avgODR[1] = (ODR[1] + ODR[3]) / 2;
		
		// First we find the maximum of the average ODRs
		int maxIndex = 0;
		float max = 0;
		for (int i = 0; i < avgODR.length; i++) {
			if (max < avgODR[i]) {
				max = avgODR[i];
				maxIndex = i;
			}
		}
		
		// Calculate hCG concentration in mIU.
		if (Math.abs(avgODR[0] - avgODR[1]) / avgODR[0] < 0.03) {
			
			// if difference between 1,3 and 2,4 are < 3%.
			return getConcentration((avgODR[0] + avgODR[1]) / 2);
		}
		if (maxIndex == 0) {
			
			// Strip 1 and Strip 3 
			
			return getConcentration(avgODR[0]);
		} else {
			
			// Strip 2 and Strip 4
			
			return getConcentration(avgODR[1]);
		}
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
