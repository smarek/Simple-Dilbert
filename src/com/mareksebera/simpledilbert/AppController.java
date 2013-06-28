package com.mareksebera.simpledilbert;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class AppController extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		configureImageLoader(this);
	}

	public static void configureImageLoader(Context c) {
		if (!ImageLoader.getInstance().isInited()) {
			DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
					.cacheInMemory().cacheOnDisc().build();
			ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
					c).defaultDisplayImageOptions(displayOptions).build();
			ImageLoader.getInstance().init(configuration);
		}
	}

}
