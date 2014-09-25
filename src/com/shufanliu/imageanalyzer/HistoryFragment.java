package com.shufanliu.imageanalyzer;

import java.util.List;

import ca.sfu.mobileodr.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class HistoryFragment extends Fragment {

	private static HistoryDataSource datasource;
	public static ArrayAdapter<History> adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_history, container,
				false);
		
		// Setup Refresh Button
		Button refreshButton = (Button) rootView.findViewById(R.id.refreshButton);
		refreshButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				updateAdapter();
			}
		});
		
		// Setup Clear History Button
		Button clearHistoryButton = (Button) rootView.findViewById(R.id.clearHistoryButton);
		clearHistoryButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				datasource = new HistoryDataSource(getActivity());
				datasource.open();
				datasource.clearHistory();
				datasource.close();
				updateAdapter();
			}
		});

		// Setup ListView
		ListView listView = (ListView) rootView.findViewById(R.id.listView1);

		datasource = new HistoryDataSource(getActivity());
		datasource.open();

		List<History> values = datasource.getAllComments();
		datasource.close();

		// use the SimpleCursorAdapter to show the
		// elements in a ListView
		adapter = new ArrayAdapter<History>(
				getActivity(), android.R.layout.simple_list_item_1, values);
		listView.setAdapter(adapter);

		// List<CaptureHistory> allHistory = CaptureHistory.getAllHistory();
		// String[] histStrArray = new String[allHistory.size()];
		// for (int i = 0; i < allHistory.size(); i++) {
		// histStrArray[i] = allHistory.get(i).getDesc();
		// }
		//
		// ArrayAdapter<String> adapter = new
		// ArrayAdapter<String>(getActivity(),
		// android.R.layout.simple_list_item_1, histStrArray);
		//
		// // Assign adapter to ListView
		// listView.setAdapter(adapter);

		return rootView;
	}

	public static void updateAdapter() {
		datasource.open();
		List<History> values = datasource.getAllComments();
		datasource.close();
		adapter.clear();
		if (values != null) {
			for (History history : values) {
				adapter.add(history);
			}
		}
		adapter.notifyDataSetChanged();
	}
}
