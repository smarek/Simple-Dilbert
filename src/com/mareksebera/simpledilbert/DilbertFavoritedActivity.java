package com.mareksebera.simpledilbert;

import org.joda.time.DateTimeZone;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerTitleStrip;
import android.util.Log;
import android.util.TypedValue;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class DilbertFavoritedActivity extends SherlockFragmentActivity {

	protected static final String TAG = "DilbertFragmentActivity";

	static {
		DateTimeZone.setDefault(DilbertPreferences.TIME_ZONE);
	}

	private FixedViewPager viewPager;
	private DilbertFavoritedFragmentAdapter adapter;
	private DilbertPreferences preferences;
	private PagerTitleStrip titles;

	@Override
	protected void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		preferences = new DilbertPreferences(this);
		setTitle(R.string.title_favorited);
		if (preferences.isForceLandscape())
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setTheme(preferences.isDarkLayoutEnabled() ? R.style.AppThemeDark
				: R.style.AppThemeLight);
		setContentView(R.layout.activity_dilbert_fragments);
		viewPager = (FixedViewPager) findViewById(R.id.view_pager);
		titles = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
		adapter = new DilbertFavoritedFragmentAdapter(
				getSupportFragmentManager(), preferences.getFavoritedItems());
		if (adapter.getCount() == 0) {
			Toast.makeText(this, R.string.toast_no_favorites, Toast.LENGTH_LONG)
					.show();
			finish();
		}
		titles.setTextColor(Color.WHITE);
		viewPager.setAdapter(adapter);
		if (preferences.isToolbarsHidden())
			toggleActionBar();
	}

	public void toggleActionBar() {
		try {
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.WRAP_CONTENT);
			if (getSupportActionBar().isShowing()) {
				getSupportActionBar().hide();
				lp.topMargin = 0;
			} else {
				getSupportActionBar().show();
				TypedValue tv = new TypedValue();
				getTheme().resolveAttribute(android.R.attr.actionBarSize, tv,
						true);
				lp.topMargin = getResources().getDimensionPixelSize(
						tv.resourceId);
			}
			viewPager.setLayoutParams(lp);
		} catch (Throwable t) {
			Log.e("DilbertFragmentActivity", "Toggle ActionBar failed", t);
		}
	}

}
