package com.mareksebera.simpledilbert.utilities;

import android.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.widget.FrameLayout;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public final class ActionBarUtility {

    private static int getActionBarHeightDip(Context c) {
        float scale = c.getResources().getDisplayMetrics().density;
        return (int) (40 * scale + 0.5f);
    }

    @SuppressLint("InlinedApi")
    public static void toggleActionBar(SherlockFragmentActivity activity,
                                       ViewPager viewPager) {
        try {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) viewPager
                    .getLayoutParams();
            if (lp == null)
                return;
            if (activity.getSupportActionBar().isShowing()) {
                activity.getSupportActionBar().hide();
                if (android.os.Build.VERSION.SDK_INT >= 11) {
                    lp.topMargin = 0;
                    viewPager.setLayoutParams(lp);
                } else {
                    viewPager.setPadding(0, 0, 0, 0);
                }
            } else {
                activity.getSupportActionBar().show();
                if (android.os.Build.VERSION.SDK_INT >= 11) {
                    TypedValue tv = new TypedValue();
                    Resources.Theme activityTheme = activity.getTheme();
                    if (activityTheme == null) return;
                    final boolean resolved = activityTheme.resolveAttribute(
                            R.attr.actionBarSize, tv, true);
                    lp.topMargin = resolved ? activity.getResources()
                            .getDimensionPixelSize(tv.resourceId) : 40;
                    viewPager.setLayoutParams(lp);
                } else {
                    viewPager.setPadding(0, getActionBarHeightDip(activity), 0, 0);
                }
            }
        } catch (Throwable t) {
            Log.e("DilbertFragmentActivity", "Toggle ActionBar failed", t);
        }
    }

}
