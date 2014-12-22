package com.shufanliu.imagelab;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class RectScanArea implements ScanArea {
	
	private int x1;
	private int y1;
	private int x2;
	private int y2;
	private int width;
	private int height;
	private int ID;
	private String name;
	
	public RectScanArea(int ID, String name, int x1, int y1, int x2, int y2) {
		this.ID = ID;
		this.name = name;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		width = x2 - x1;
		height = y2 - y1;
	}
	
	public RectF getRect() {
		return new RectF(x1, y1, x2, y2);
	}

	@Override
	public int[] getPixels(Bitmap image) {
		// rotate the bitmap
		image = Common.rotateBitmap(image, 90);
		
		int[] pixels = new int[width * height];
		image.getPixels(pixels, 0, width, x1, y1, width, height);
		return pixels;
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
