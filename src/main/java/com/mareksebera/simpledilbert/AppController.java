package com.mareksebera.simpledilbert;

import android.app.Application;
import android.view.ViewConfiguration;

import com.mareksebera.simpledilbert.preferences.DilbertPreferences;

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

    @Override
    public void onCreate() {
        super.onCreate();
        forceMenuOverflow();
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
