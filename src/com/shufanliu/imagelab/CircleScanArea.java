package com.shufanliu.imagelab;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

public class CircleScanArea implements ScanArea {
	
	private static final String TAG = "CircleScanArea";

	private int x1;
	private int y1;
	private int radius;
	private int width;
	private int ID;
	private String name;
	
	public CircleScanArea(int ID, String name, int x1, int y1, int radius) {
		this.ID = ID;
		this.name = name;
		this.x1 = x1;
		this.y1 = y1;
		this.radius = radius;
		width = radius * 2;
	}
	
	@Override
	public int[] getPixels(Bitmap image) {
		// rotate the bitmap
		image = Common.rotateBitmap(image, 90);
		
		int x0 = x1 - radius;
		int y0 = y1 - radius;
		int n = 0;
		int[] pixels = new int[20079]; // TODO: change the size according to image size
		for (int x = x0; x < x0 + width; x++) {
			for (int y = y0; y < y0 + width; y++) {
				if (Math.pow(x - x1, 2.0) + Math.pow(y - y1, 2.0) <= Math.pow(radius, 2.0)) {
					pixels[n] = image.getPixel(x, y);
					n = n + 1;
				}
			}
		}
		Log.e(TAG, "w = " + image.getWidth() + " h = " + image.getHeight());
		Log.e(TAG, "n = " + n + " r = " + radius);
		return pixels;
	}

	public int getX1() {
		return x1;
	}

	public int getY1() {
		return y1;
	}

	public int getRadius() {
		return radius;
	}

	@Override
	public int getID() {
		return ID;
	}

	@Override
	public String getName() {
		return name;
	}

}
