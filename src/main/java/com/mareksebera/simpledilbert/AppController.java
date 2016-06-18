package com.mareksebera.simpledilbert;

import android.app.Application;
import android.content.Context;
import android.view.ViewConfiguration;

import com.mareksebera.simpledilbert.preferences.DilbertPreferences;
import com.mareksebera.simpledilbert.utilities.FailSafeBitmapDisplayer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.joda.time.DateTimeZone;

import java.lang.reflect.Field;

public final class AppController extends Application {

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
                    .cacheInMemory(true).cacheOnDisk(true)
                    .displayer(new FailSafeBitmapDisplayer()).build();
            ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
                    c)
                    .defaultDisplayImageOptions(displayOptions).build();
            ImageLoader.getInstance().init(configuration);
            ImageLoader.getInstance().handleSlowNetwork(true);

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        forceMenuOverflow();
        configureImageLoader(this);
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
        } catch (Throwable ignored) {
        }
    }

}
