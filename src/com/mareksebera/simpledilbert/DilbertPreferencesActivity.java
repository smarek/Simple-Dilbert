package com.mareksebera.simpledilbert;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.lamerman.FileDialog;

import java.io.File;
import java.io.InputStream;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.getInstance;

public final class DilbertPreferencesActivity extends SherlockFragmentActivity {

    private CheckBox force_landscape, enable_hq, force_dark, hide_toolbars,
            force_dark_widget, share_image, mobile_network;
    private TextView download_path;
    private DilbertPreferences preferences;
    private static final int REQUEST_DOWNLOAD_TARGET = 1;
    private static final String TAG = "DilbertPreferencesActivity";
    private OnClickListener licenseOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            showLicenseDialog();
        }
    };
    private OnClickListener ratingOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            try {
                startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.mareksebera.simpledilbert")));
            } catch (Throwable t) {
                t.printStackTrace();
                Toast.makeText(DilbertPreferencesActivity.this,
                        "Cannot open Google Play", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private OnClickListener downloadPathClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent downloadPathSelector = new Intent(
                    DilbertPreferencesActivity.this, FileDialog.class);
            downloadPathSelector.putExtra(FileDialog.CAN_SELECT_DIR, true);
            downloadPathSelector.putExtra(FileDialog.START_PATH,
                    preferences.getDownloadTarget());
            startActivityForResult(downloadPathSelector,
                    REQUEST_DOWNLOAD_TARGET);
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_DOWNLOAD_TARGET) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        if(resultCode != RESULT_OK)
            return;
        if (data != null && data.getExtras() != null) {
            String result = data.getExtras().getString(FileDialog.RESULT_PATH);
            if (result != null) {
                File tmp = new File(result);
                if (!tmp.isDirectory())
                    tmp = tmp.getParentFile();
                if (tmp != null)
                    preferences.setDownloadTarget(tmp.getAbsolutePath());
            }
        }
    }

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
        preferences = new DilbertPreferences(this);
        if (preferences.isForceLandscape())
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setTheme(preferences.isDarkLayoutEnabled() ? R.style.AppThemeDark
                : R.style.AppThemeLight);
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.preferences);
        setTitle(R.string.title_preferences);
        force_landscape = (CheckBox) findViewById(R.id.pref_force_landscape);
        enable_hq = (CheckBox) findViewById(R.id.pref_enable_high_quality);
        force_dark = (CheckBox) findViewById(R.id.pref_force_dark_background);
        force_dark_widget = (CheckBox) findViewById(R.id.pref_force_dark_background_widget);
        hide_toolbars = (CheckBox) findViewById(R.id.pref_hide_toolbars);
        share_image = (CheckBox) findViewById(R.id.pref_share_image);
        mobile_network = (CheckBox) findViewById(R.id.pref_mobile_network);
        download_path = (TextView) findViewById(R.id.pref_download_path);
        LinearLayout download_path_layout = (LinearLayout) findViewById(R.id.pref_download_path_layout);
        TextView license = (TextView) findViewById(R.id.pref_show_license);
        TextView rating = (TextView) findViewById(R.id.pref_rating);
        download_path_layout.setOnClickListener(downloadPathClickListener);
        license.setOnClickListener(licenseOnClickListener);
        rating.setOnClickListener(ratingOnClickListener);

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
        force_dark_widget.setChecked(preferences.isDarkWidgetLayoutEnabled());
        hide_toolbars.setChecked(preferences.isToolbarsHidden());
        download_path.setText(preferences.getDownloadTarget());
        share_image.setChecked(preferences.isSharingImage());
        mobile_network.setChecked(preferences.isSlowNetwork());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        preferences.setIsDarkLayoutEnabled(force_dark.isChecked());
        preferences.setIsForceLandscape(force_landscape.isChecked());
        preferences.setIsToolbarsHidden(hide_toolbars.isChecked());
        preferences.setIsHighQualityOn(enable_hq.isChecked());
        preferences.setIsDarkWidgetLayoutEnabled(force_dark_widget.isChecked());
        preferences.setIsSharingImage(share_image.isChecked());
        preferences.setIsSlowNetwork(mobile_network.isChecked());
        updateWidgets();
    }

    private void updateWidgets() {
        try {
            int[] ids = getInstance(this).getAppWidgetIds(
                    new ComponentName(this, WidgetProvider.class));
            if (ids != null)
                for (int id : ids) {
                    Intent updateIntent = new Intent();
                    updateIntent
                            .setAction(ACTION_APPWIDGET_UPDATE);
                    updateIntent.putExtra(EXTRA_APPWIDGET_ID,
                            id);
                    sendBroadcast(updateIntent);
                }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
