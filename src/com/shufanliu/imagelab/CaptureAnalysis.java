package com.shufanliu.imagelab;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Color;

public class CaptureAnalysis {
	
	private static final String TAG = "CaptureAnalysis";
	
	private String taskName;
	private List<ScanArea> scanAreaList;
	private List<SummaryStatistics> summaryStatsList;
	private Bitmap image;
	private Date timeStamp;
	
	public CaptureAnalysis(String taskName, Bitmap image, List<ScanArea> scanAreaList) {
		this.taskName = taskName;
		this.image = image;
		this.scanAreaList = scanAreaList;
		summaryStatsList = new ArrayList<SummaryStatistics>();
		timeStamp = new Date();
		analyze();
	}
	
	private void analyze() {
		for (ScanArea scanArea : scanAreaList) {
			summaryStatsList.add(getChannelStats(scanArea));
		}
	}
	
	private SummaryStatistics getChannelStats(ScanArea scanArea) {
		int[] pixels = scanArea.getPixels(image);
		
		// Want to get mean values
		int size = pixels.length;
		double RSum = 0;
		double GSum = 0;
		double BSum = 0;
		double luminositySum = 0;
		double meanR = 0;
		double meanG = 0;
		double meanB = 0;
		double meanLuminosity = 0;
		for (int pixel : pixels) {
			int red = Color.red(pixel);
			int green = Color.green(pixel);
			int blue = Color.blue(pixel);
			RSum += red;
			GSum += green;
			BSum += blue;
			luminositySum += 0.3 * red + 0.59 * green + 0.11 * blue;
		}
		meanR = RSum / size;
		meanG = GSum / size;
		meanB = BSum / size;
		meanLuminosity = luminositySum / size;
		
		// Want to get standard errors
		double RSSE = 0;
		double GSSE = 0;
		double BSSE = 0;
		double luminositySSE = 0;
		double RVar;
		double GVar;
		double BVar;
		double luminosityVar;
		for (int pixel : pixels) {
			int red = Color.red(pixel);
			int green = Color.green(pixel);
			int blue = Color.blue(pixel);
			RSSE += Common.getSE((double) red, meanR);
			GSSE += Common.getSE((double) green, meanG);
			BSSE += Common.getSE((double) blue, meanB);
			luminositySSE += Common.getSE((double) (0.3 * red + 0.59 * green + 0.11 * blue), meanLuminosity);
		}
		RVar = RSSE / (size - 1);
		GVar = GSSE / (size - 1);
		BVar = BSSE / (size - 1);
		luminosityVar = luminositySSE / (size - 1);
		SummaryStatistics ss = new SummaryStatistics(scanArea.getName());
		ss.addSummary("R", meanR, Math.sqrt(RVar));
		ss.addSummary("G", meanG, Math.sqrt(GVar));
		ss.addSummary("B", meanB, Math.sqrt(BVar));
		ss.addSummary("I", meanLuminosity, Math.sqrt(luminosityVar));

		return ss;
	}

	public String getTaskName() {
		return taskName;
	}
	
	public Date getTimeStamp() {
		return timeStamp;
	}

	public List<SummaryStatistics> getSummaryStatsList() {
		return summaryStatsList;
	}
	

}
