package com.mareksebera.simpledilbert;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerTitleStrip;
import android.text.Html;
import android.util.Log;
import android.widget.DatePicker;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class DilbertFragmentActivity extends SherlockFragmentActivity {

	private static final int MENU_DATEPICKER = 1, MENU_ABOUT = 2,
			MENU_LATEST = 3, MENU_LICENSE = 4, MENU_SHOW_FAVORITES = 5,
			MENU_SHUFFLE = 6, MENU_HIGHQUALITY = 7;
	private static final String TAG = "DilbertFragmentActivity";

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

	private CharSequence getLicenseText() {
		String rtn = "";
		try {
			InputStream stream = getAssets().open("LICENSE.txt");
			java.util.Scanner s = new java.util.Scanner(stream)
					.useDelimiter("\\A");
			rtn = s.hasNext() ? s.next() : "";
		} catch (Exception e) {
			Log.e(TAG, "License couldn't be retrieved", e);
		} catch (Error e) {
			Log.e(TAG, "License couldn't be retrieved", e);
		}
		return rtn;
	}

	private FixedViewPager viewPager;
	private DilbertFragmentAdapter adapter;
	private DilbertPreferences preferences;
	private PagerTitleStrip titles;

	private void setCurrentDate(DateMidnight date) {
		preferences.saveCurrentDate(date);
		viewPager.setCurrentItem(adapter.getPositionForDate(date));
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_dilbert_fragments);
		viewPager = (FixedViewPager) findViewById(R.id.view_pager);
		titles = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
		preferences = new DilbertPreferences(this);
		adapter = new DilbertFragmentAdapter(getSupportFragmentManager());
		titles.setTextColor(Color.WHITE);
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(adapter.getPositionForDate(preferences
				.getCurrentDate()));
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu.findItem(MENU_HIGHQUALITY) != null) {
			menu.findItem(MENU_HIGHQUALITY).setChecked(
					preferences.isHighQualityOn());
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int category = 0;
		menu.add(category, MENU_DATEPICKER, 4,
				R.string.menu_datepicker)
				.setIcon(R.drawable.ic_menu_datepicker)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
		menu.add(category, MENU_SHUFFLE, 1, R.string.menu_random)
				.setIcon(R.drawable.ic_menu_shuffle)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(category, MENU_SHOW_FAVORITES, 6,
				R.string.menu_show_favorite).setShowAsActionFlags(
				MenuItem.SHOW_AS_ACTION_NEVER);
		menu.add(category, MENU_LATEST, 5, R.string.menu_latest)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
		menu.add(category, MENU_HIGHQUALITY, 5,
				R.string.menu_high_quality).setCheckable(true)
				.setChecked(preferences.isHighQualityOn());
		menu.add(category, MENU_ABOUT, 7, R.string.menu_about)
				.setIcon(R.drawable.ic_menu_about)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(category, MENU_LICENSE, 8, R.string.menu_license)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
		return super.onCreateOptionsMenu(menu);
	}

	private void showAboutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.about_title);
		builder.setMessage(Html.fromHtml(getString(R.string.about_contents)));
		builder.setNeutralButton(android.R.string.cancel,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
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

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case MENU_DATEPICKER:
			showDatePicker();
			return true;
		case MENU_ABOUT:
			showAboutDialog();
			return true;
		case MENU_LATEST:
			setCurrentDate(DateMidnight.now(DilbertPreferences.TIME_ZONE));
			return true;
		case MENU_LICENSE:
			showLicenseDialog();
			return true;
		case MENU_HIGHQUALITY:
			preferences.toggleHighQuality();
			return true;
		case MENU_SHOW_FAVORITES:
			startActivity(new Intent(this, FavoritedActivity.class));
			return true;
		case MENU_SHUFFLE:
			setCurrentDate(DilbertPreferences.getRandomDateMidnight());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showLicenseDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.apache_license_2_0);
		builder.setMessage(getLicenseText());
		builder.setNeutralButton(android.R.string.cancel,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

}
