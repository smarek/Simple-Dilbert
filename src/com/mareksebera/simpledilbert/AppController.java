package com.mareksebera.simpledilbert;

import java.lang.reflect.Field;

import org.joda.time.DateTimeZone;

import android.app.Application;
import android.content.Context;
import android.view.ViewConfiguration;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class AppController extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		forceMenuOverflow();
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
					.cacheInMemory(true).cacheOnDisc(true)
					.displayer(new FailSafeBitmapDisplayer()).build();
			ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
					c).defaultDisplayImageOptions(displayOptions).build();
			ImageLoader.getInstance().init(configuration);
		}
	}

	private void forceMenuOverflow() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Throwable t) {
		}
	}

}
