package com.mareksebera.simpledilbert.utilities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.widget.FrameLayout;

public final class ActionBarUtility {

    public static int getActionBarHeightDip(Context c) {
        float scale = c.getResources().getDisplayMetrics().density;
        return (int) ((48 * scale) + 0.5);
    }

    public static void toggleActionBar(AppCompatActivity actionBarActivity,
                                       ViewPager viewPager) {
        try {
            if (actionBarActivity == null || actionBarActivity.getSupportActionBar() == null)
                return;
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) viewPager
                    .getLayoutParams();
            if (lp == null)
                return;
            if (actionBarActivity.getSupportActionBar().isShowing()) {
                actionBarActivity.getSupportActionBar().hide();
                if (android.os.Build.VERSION.SDK_INT >= 11) {
                    lp.topMargin = 0;
                    viewPager.setLayoutParams(lp);
                } else {
                    viewPager.setPadding(0, 0, 0, 0);
                }
            } else {
                actionBarActivity.getSupportActionBar().show();
                if (android.os.Build.VERSION.SDK_INT >= 11) {
                    lp.topMargin = getActionBarHeightCompat(actionBarActivity);
                    viewPager.setLayoutParams(lp);
                } else {
                    viewPager.setPadding(0, getActionBarHeightDip(actionBarActivity), 0, 0);
                }
            }
        } catch (Throwable t) {
            Log.e("DilbertFragmentActivity", "Toggle ActionBar failed", t);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static int getActionBarHeightCompat(AppCompatActivity actionBarActivity) {
        TypedValue tv = new TypedValue();
        Resources.Theme activityTheme = actionBarActivity.getTheme();
        boolean resolved = false;
        if (activityTheme != null) {
            resolved = activityTheme.resolveAttribute(
                    android.R.attr.actionBarSize, tv, true);
        }
        return resolved ? actionBarActivity.getResources()
                .getDimensionPixelSize(tv.resourceId) : 40;

    }

}
