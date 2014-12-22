package com.shufanliu.imagelab;

import java.util.ArrayList;
import java.util.List;

public class SummaryStatistics {

	private String name;
	private List<Summary> summaryList;

	public SummaryStatistics(String name) {
		this.name = name;
		summaryList = new ArrayList<Summary>();
	}

	public void addSummary(String parameterName, double meanValue,
			double errorValue) {
		summaryList.add(new Summary(parameterName, meanValue, errorValue));
	}

	public List<Summary> getSummaryList() {
		return summaryList;
	}

	public String getName() {
		return name;
	}

	public class Summary {
		private String name;
		private double meanValue;
		private double errorValue;

		public Summary(String name, double meanValue, double errorValue) {
			this.name = name;
			this.meanValue = meanValue;
			this.errorValue = errorValue;
		}

		@Override
		public String toString() {
			return String.format("%s : %s กำ %s ", name,
					Common.getSigFig(meanValue, Settings.getInstance().numOfSigFig),
					Common.getSigFig(errorValue, Settings.getInstance().numOfSigFig));
		}
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		String str = name + "\n";
		for (Summary s : summaryList) {
			str += s.toString() + "\n";
		}
		return str;
	}
}
