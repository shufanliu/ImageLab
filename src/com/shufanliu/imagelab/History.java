package com.shufanliu.imagelab;

public class History {
	private long id;
	private String history;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getHistory() {
		return history;
	}

	public void setHistory(String history) {
		this.history = history;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return history;
	}
}
