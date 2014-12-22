package com.shufanliu.imagelab;

import com.shufanliu.imagelab.R;
import com.shufanliu.imagelab.Settings.CaptureLayout;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SettingsFragment extends Fragment {
	
	private static final String TAG = "SettingsFragment";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_settings, container,
				false);
		RadioGroup radioGroup = (RadioGroup) rootView
				.findViewById(R.id.captureLayoutRG);
		
		if (Settings.getInstance().captureLayout == CaptureLayout.CIRCLE) {
			radioGroup.check(R.id.radioCircle);
		} else if (Settings.getInstance().captureLayout == CaptureLayout.SQUARE) {
			radioGroup.check(R.id.radioSquare);
		} else {
			//radioGroup.check(R.id.radioTestStrip);
		}
		
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// find which radio button is selected
				if (checkedId == R.id.radioCircle) {
					Settings.getInstance().captureLayout = CaptureLayout.CIRCLE;
				} else if (checkedId == R.id.radioSquare) {
					Settings.getInstance().captureLayout = CaptureLayout.SQUARE;
				} else {
					// TODO: test strip option
				}
				Log.e(TAG, Settings.getInstance().captureLayout.toString());
			}
		});
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(4);
	}
}
