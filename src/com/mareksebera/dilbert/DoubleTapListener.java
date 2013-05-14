package com.mareksebera.dilbert;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class DoubleTapListener implements OnTouchListener {

	public DoubleTapListener(Context context) {
		gestureDetector = new GestureDetector(context,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onDoubleTap(MotionEvent e) {
						return super.onDoubleTap(e);
					}
				});
	}

	private GestureDetector gestureDetector;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

}
