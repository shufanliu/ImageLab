package com.shufanliu.imagelab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

public class Common {

	public static boolean DEBUG = false;
	
	public static float getMean(float[] values) {
		float sum = 0;
		for (float value : values) {
			sum += value;
		}
		return sum / values.length;
	}
	
	public static float getSampleVariance(float[] values)
    {
		float mean = getMean(values);
		float sum = 0;
        for (float value : values) {
        	sum += (value - mean) * (value - mean);
        }
        return sum / (values.length - 1);
    }

	public static float getSampleStdDev(float[] values)
    {
        return (float) Math.sqrt(getSampleVariance(values));
    }
	

	
	public static String getSigFig(float value, int n) {
		String result = String.format("%.10f", value);
		int k = (int) (Math.log(Math.abs(value)) / Math.log(10));
		if ((k + 1) >= n) {
			// Assume value > 0
			result = result.substring(0, n);
		} else {
			if (value < 0) {
				n = n + 1;
			}
			if (0 < Math.abs(value) && Math.abs(value) < 1){
				n = n + 1;
			}
			if (value != 0) {
				result = result.substring(0, n + 1);
			} else {
				n = n + 2;
				result = "0.0";
			}
			// Add trailing zeros
			while (result.length() < n) {
				result = result + "0";
			}
		}
		return result;
	}
	
	public static String[] getSigFig(float[] values, int n) {
		String[] result = new String[values.length];
		for (int i = 0; i < values.length; i++) {
		    result[i] = getSigFig(values[i], n);
		}
		return result;
	}
	
	public static Bitmap rotateBitmap(Bitmap source, float angle)
	{
	      Matrix matrix = new Matrix();
	      matrix.postRotate(angle);
	      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}
	
	public static void storeImage(Bitmap image, String name) {
		if (DEBUG) {
		    String TAG = "MobileODR";
		    File pictureFile = getOutputMediaFile(name);
		    if (pictureFile == null) {
		        Log.d(TAG, "Error creating media file, check storage permissions: ");// e.getMessage());
		        return;
		    } 
		    try {
		        FileOutputStream fos = new FileOutputStream(pictureFile);
		        image.compress(Bitmap.CompressFormat.PNG, 90, fos);
		        fos.close();
		    } catch (FileNotFoundException e) {
		        Log.d(TAG, "File not found: " + e.getMessage());
		    } catch (IOException e) {
		        Log.d(TAG, "Error accessing file: " + e.getMessage());
		    }
		}
	}
	
	/** Create a File for saving an image */
	public static  File getOutputMediaFile(String name){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this. 
	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
	            + "/Android/data/ca.sfu.mobileodr/Files"); 

	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            return null;
	        }
	    } 
	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
	    String mImageName = timeStamp + "_" + name + ".jpg";
	    File mediaFile;
	    mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);  
	    return mediaFile;
	} 
	
}
