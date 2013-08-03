package com.mareksebera.simpledilbert;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;

public class FailSafeBitmapDisplayer implements BitmapDisplayer {

	@Override
	public Bitmap display(Bitmap bitmap, ImageView imageView,
			LoadedFrom loadedFrom) {
		try {
			imageView.setImageBitmap(bitmap);
			return bitmap;
		} catch (IllegalStateException e) {
			Log.e("debug", e.getMessage(), e);
			return bitmap;
		}
	}

}