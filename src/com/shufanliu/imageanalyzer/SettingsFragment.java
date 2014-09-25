package com.shufanliu.imageanalyzer;

import ca.sfu.mobileodr.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class SettingsFragment extends Fragment {
	
	TextView textView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
		
		// Setup TextView
		textView = (TextView) rootView.findViewById(R.id.settingTextView);
		
		// Setup radio buttons
		RadioGroup radioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup1);
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radio0) {
					ODRValue.setNumOfStrips(4);
					textView.setText("Number of Strips is now set to 4.");
				} else {
					ODRValue.setNumOfStrips(2);
					textView.setText("Number of Strips is now set to 2.");
				}
			}
			
		});
		
		// Hide the radio group. Number of strips option is under construction
		radioGroup.setVisibility(View.GONE);
		return rootView;
	}
	
}
