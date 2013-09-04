package com.mareksebera.simpledilbert;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.widget.FrameLayout;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public final class ActionBarUtility {

	public static int getDip(Context c, int pixel) {
		float scale = c.getResources().getDisplayMetrics().density;
		return (int) (pixel * scale + 0.5f);
	}

	@SuppressLint("InlinedApi")
	public static void toggleActionBar(SherlockFragmentActivity activity,
			ViewPager viewPager) {
		try {
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) viewPager
					.getLayoutParams();
            if(lp == null)
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
					activity.getTheme().resolveAttribute(
							android.R.attr.actionBarSize, tv, true);
					lp.topMargin = activity.getResources()
							.getDimensionPixelSize(tv.resourceId);
					viewPager.setLayoutParams(lp);
				} else {
					viewPager.setPadding(0, getDip(activity, 40), 0, 0);
				}
			}
		} catch (Throwable t) {
			Log.e("DilbertFragmentActivity", "Toggle ActionBar failed", t);
		}
	}

}
