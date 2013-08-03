package com.mareksebera.simpledilbert;

import org.joda.time.DateTimeZone;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class AppController extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		configureImageLoader(this);
	}

	static {
		/**
		 * Set default time-zone, because strips are published in New York
		 * timezone on midnight
		 * */
		DateTimeZone.setDefault(DilbertPreferences.TIME_ZONE);
	}

	public static void configureImageLoader(Context c) {
		if (!ImageLoader.getInstance().isInited()) {
			DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
					.cacheInMemory(false).cacheOnDisc(true)
					.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
					.displayer(new FailSafeBitmapDisplayer()).build();
			ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
					c).defaultDisplayImageOptions(displayOptions).build();
			ImageLoader.getInstance().init(configuration);
		}
	}

}
