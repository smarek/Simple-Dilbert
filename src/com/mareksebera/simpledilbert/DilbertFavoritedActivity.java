package com.mareksebera.simpledilbert;

import org.joda.time.DateTimeZone;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerTitleStrip;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public final class DilbertFavoritedActivity extends SherlockFragmentActivity implements DilbertFragmentInterface {

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
		preferences = new DilbertPreferences(this);
		setTitle(R.string.title_favorited);
		if (preferences.isForceLandscape())
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setTheme(preferences.isDarkLayoutEnabled() ? R.style.AppThemeDark
				: R.style.AppThemeLight);
		super.onCreate(savedInstance);
		setContentView(R.layout.activity_dilbert_fragments);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
			ActionBarUtility.toggleActionBar(this, viewPager);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Compat helper method
	public void toggleActionBar() {
		ActionBarUtility.toggleActionBar(this, viewPager);
	}
}
