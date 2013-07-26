package com.mareksebera.simpledilbert;

import java.util.Calendar;
import java.util.Locale;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.FrameLayout;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class DilbertFragmentActivity extends SherlockFragmentActivity {

	private static final int MENU_DATEPICKER = 1, MENU_LATEST = 3,
			MENU_SHOW_FAVORITES = 5, MENU_SHUFFLE = 6, MENU_SETTINGS = 8;
	protected static final String TAG = "DilbertFragmentActivity";

	static {
		DateTimeZone.setDefault(DilbertPreferences.TIME_ZONE);
	}

	private OnDateSetListener dilbertOnDateSetListener = new OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			DateMidnight selDate = DateMidnight.parse(String.format(new Locale(
					"en"), "%d-%d-%d", year, monthOfYear + 1, dayOfMonth),
					DilbertPreferences.DATE_FORMATTER);
			setCurrentDate(selDate);
		}
	};

	private FixedViewPager viewPager;
	private DilbertFragmentAdapter adapter;
	private DilbertPreferences preferences;
	private PagerTitleStrip titles;
	private OnPageChangeListener pageChangedListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			preferences.saveCurrentDate(adapter.getDateForPosition(position));
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	};

	private void setCurrentDate(DateMidnight date) {
		preferences.saveCurrentDate(date);
		viewPager.setCurrentItem(adapter.getPositionForDate(date));
	}

	@Override
	protected void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		preferences = new DilbertPreferences(this);
		if (preferences.isForceLandscape())
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setTheme(preferences.isDarkLayoutEnabled() ? R.style.AppThemeDark
				: R.style.AppThemeLight);
		setContentView(R.layout.activity_dilbert_fragments);
		viewPager = (FixedViewPager) findViewById(R.id.view_pager);
		titles = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
		adapter = new DilbertFragmentAdapter(getSupportFragmentManager());
		titles.setTextColor(Color.WHITE);
		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(pageChangedListener);
		if (preferences.isToolbarsHidden())
			toggleActionBar();
	}

	@Override
	protected void onResume() {
		super.onResume();
		viewPager.setCurrentItem(adapter.getPositionForDate(preferences
				.getCurrentDate()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int category = 0;
		menu.add(category, MENU_DATEPICKER, 4, R.string.menu_datepicker)
				.setIcon(R.drawable.ic_menu_datepicker)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
		menu.add(category, MENU_SHUFFLE, 1, R.string.menu_random)
				.setIcon(R.drawable.ic_menu_shuffle)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(category, MENU_SHOW_FAVORITES, 6, R.string.menu_show_favorite)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
		menu.add(category, MENU_LATEST, 5, R.string.menu_latest)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
		menu.add(category, MENU_SETTINGS, 8, R.string.menu_settings).setShowAsActionFlags(
				MenuItem.SHOW_AS_ACTION_NEVER);
		return super.onCreateOptionsMenu(menu);
	}

	private void showDatePicker() {
		Calendar c = Calendar.getInstance();
		c.setTime(adapter.getDateForPosition(viewPager.getCurrentItem())
				.toDate());
		DatePickerDialog dialog = new DatePickerDialog(this,
				dilbertOnDateSetListener, c.get(Calendar.YEAR),
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		dialog.show();
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

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case MENU_DATEPICKER:
			showDatePicker();
			return true;
		case MENU_LATEST:
			setCurrentDate(DateMidnight.now(DilbertPreferences.TIME_ZONE));
			return true;
		case MENU_SHOW_FAVORITES:
			startActivity(new Intent(this, FavoritedActivity.class));
			return true;
		case MENU_SHUFFLE:
			setCurrentDate(DilbertPreferences.getRandomDateMidnight());
			return true;
		case MENU_SETTINGS:
			startActivity(new Intent(this, DilbertPreferencesActivity.class));
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
