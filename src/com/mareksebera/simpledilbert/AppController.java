package com.mareksebera.simpledilbert;

import android.app.Application;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class AppController extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		configureImageLoader();
	}

	private void configureImageLoader() {
		if (!ImageLoader.getInstance().isInited()) {
			DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
					.cacheInMemory().cacheOnDisc().build();
			ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
					this).defaultDisplayImageOptions(displayOptions).build();
			ImageLoader.getInstance().init(configuration);
		}
	}

}
