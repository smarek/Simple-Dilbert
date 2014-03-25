package com.mareksebera.simpledilbert.utilities;

import android.graphics.Bitmap;
import android.util.Log;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

public final class FailSafeBitmapDisplayer implements BitmapDisplayer {

    @Override
    public void display(Bitmap bitmap, ImageAware imageView,
                        LoadedFrom loadedFrom) {
        try {
            imageView.setImageBitmap(bitmap);
        } catch (IllegalStateException e) {
            Log.e("debug", e.getMessage(), e);
        }
    }
}