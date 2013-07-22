package com.mareksebera.simpledilbert;

import java.io.InputStream;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class DilbertPreferencesActivity extends SherlockFragmentActivity {

	private CheckBox force_landscape, enable_hq, force_dark, hide_toolbars;
	private TextView license;
	private DilbertPreferences preferences;
	private static final String TAG = "DilbertPreferencesActivity";
	private OnClickListener licenseOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			showLicenseDialog();
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

	private void showLicenseDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.apache_license_2_0);
		builder.setMessage(getLicenseText());
		builder.setNeutralButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferences = new DilbertPreferences(this);
		if (preferences.isForceLandscape())
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setTheme(preferences.isDarkLayoutEnabled() ? R.style.AppThemeDark
				: R.style.AppThemeLight);
		setContentView(R.layout.preferences);
		setTitle(R.string.title_preferences);
		force_landscape = (CheckBox) findViewById(R.id.pref_force_landscape);
		enable_hq = (CheckBox) findViewById(R.id.pref_enable_high_quality);
		force_dark = (CheckBox) findViewById(R.id.pref_force_dark_background);
		hide_toolbars = (CheckBox) findViewById(R.id.pref_hide_toolbars);
		license = (TextView) findViewById(R.id.pref_show_license);
		license.setOnClickListener(licenseOnClickListener);
	}
	
	@Override
	public void onBackPressed() {
		startActivity(new Intent(this, DilbertFragmentActivity.class));
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		force_landscape.setChecked(preferences.isForceLandscape());
		enable_hq.setChecked(preferences.isHighQualityOn());
		force_dark.setChecked(preferences.isDarkLayoutEnabled());
		hide_toolbars.setChecked(preferences.isToolbarsHidden());
	}

	@Override
	protected void onPause() {
		super.onPause();
		preferences.setIsDarkLayoutEnabled(force_dark.isChecked());
		preferences.setIsForceLandscape(force_landscape.isChecked());
		preferences.setIsToolbarsHidden(hide_toolbars.isChecked());
		preferences.setIsHighQualityOn(enable_hq.isChecked());
	}

}
