package com.mareksebera.simpledilbert;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class FailSafeBitmapDisplayer extends FadeInBitmapDisplayer {

	public FailSafeBitmapDisplayer(int durationMillis) {
		super(durationMillis);
	}

	@Override
	public Bitmap display(Bitmap bitmap, ImageView imageView, LoadedFrom loadedFrom) {
		try {
			return super.display(bitmap, imageView, loadedFrom);
		} catch (IllegalStateException e) {
			Log.e("debug", e.getMessage(), e);
			return bitmap;
		}
	}
	
}