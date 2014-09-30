package com.shufanliu.imagelab;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class HistoryDataSource {

	// Database fields
	private SQLiteDatabase database;
	private DBHelper dbHelper;
	private String[] allColumns = { DBHelper.COLUMN_ID,
			DBHelper.COLUMN_HISTORY };

	public HistoryDataSource(Context context) {
		dbHelper = new DBHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public History createHistory(String history) {
		ContentValues values = new ContentValues();
		values.put(DBHelper.COLUMN_HISTORY, history);
		long insertId = database.insert(DBHelper.TABLE_HISTORY, null,
				values);
		Cursor cursor = database.query(DBHelper.TABLE_HISTORY,
				allColumns, DBHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		History newHistory = cursorToHistory(cursor);
		cursor.close();
		return newHistory;
	}

	public void deleteHistory(History history) {
		long id = history.getId();
		System.out.println("Comment deleted with id: " + id);
		database.delete(DBHelper.TABLE_HISTORY, DBHelper.COLUMN_ID
				+ " = " + id, null);
	}
	
	public void clearHistory() {
		System.out.println("Clear databse");
		database.delete(DBHelper.TABLE_HISTORY, "1", null);
	}

	public List<History> getAllComments() {
		List<History> histories = new ArrayList<History>();

		Cursor cursor = database.query(DBHelper.TABLE_HISTORY,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			History history = cursorToHistory(cursor);
			histories.add(history);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return histories;
	}

	private History cursorToHistory(Cursor cursor) {
		History history = new History();
		history.setId(cursor.getLong(0));
		history.setHistory(cursor.getString(1));
		return history;
	}
}