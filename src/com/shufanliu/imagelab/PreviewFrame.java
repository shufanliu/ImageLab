package com.shufanliu.imagelab;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class PreviewFrame extends FrameLayout {

	public PreviewFrame(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
	    // MotionEvent reports input details from the touch screen
	    // and other input controls. In this case, you are only
	    // interested in events where the touch position changed.

	    float x = e.getX();
	    float y = e.getY();

	    switch (e.getAction()) {
	        case MotionEvent.ACTION_MOVE:
	        	CaptureFragment.mCamera.autoFocus(CaptureFragment.autoFocusCB);
	    }
	    return true;
	}
		
}
