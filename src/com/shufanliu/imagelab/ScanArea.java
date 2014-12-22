package com.shufanliu.imagelab;

import android.graphics.Bitmap;

public interface ScanArea {
	
	public int getID();
	public String getName();
	public int[] getPixels(Bitmap bitmap);
	
}
