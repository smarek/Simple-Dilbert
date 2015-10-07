package com.mareksebera.simpledilbert.preferences;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mareksebera.simpledilbert.R;
import com.mareksebera.simpledilbert.core.DilbertFragmentActivity;
import com.mareksebera.simpledilbert.picker.FolderPickerActivity;
import com.mareksebera.simpledilbert.widget.WidgetProvider;

import java.io.File;
import java.io.InputStream;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.getInstance;

public final class DilbertPreferencesActivity extends AppCompatActivity {

    private CheckBox force_landscape, force_dark, hide_toolbars,
            share_image, reverse_landscape,
            open_at_latest_strip, widget_always_show_latest;
    private TextView download_path;
    private DilbertPreferences preferences;
    private static final int REQUEST_DOWNLOAD_TARGET = 1;
    private static final String TAG = "DilbertPreferencesAct";
    private final OnClickListener licenseOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            showLicenseDialog();
        }
    };

    private final OnClickListener downloadPathClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent downloadPathSelector = new Intent(
                    DilbertPreferencesActivity.this, FolderPickerActivity.class);
            downloadPathSelector.setData(Uri.fromFile(new File(preferences.getDownloadTarget())));
            startActivityForResult(downloadPathSelector,
                    REQUEST_DOWNLOAD_TARGET);
        }
    };
    private final OnClickListener authorOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "marek@msebera.cz", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Simple Dilbert");
            startActivity(Intent.createChooser(emailIntent, "Simple Dilbert"));
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_DOWNLOAD_TARGET) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        if (resultCode != RESULT_OK)
            return;
        if (data != null) {
            Uri path = data.getData();
            if (path != null) {
                preferences.setDownloadTarget(new File(path.getPath()).getAbsolutePath());
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
        } catch (Exception | Error e) {
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
            setRequestedOrientation(preferences.getLandscapeOrientation());
        setTheme(preferences.isDarkLayoutEnabled() ? R.style.AppThemeDark
                : R.style.AppThemeLight);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.preferences);
        setTitle(R.string.title_preferences);
        force_landscape = (CheckBox) findViewById(R.id.pref_force_landscape);
        force_dark = (CheckBox) findViewById(R.id.pref_force_dark_background);
        hide_toolbars = (CheckBox) findViewById(R.id.pref_hide_toolbars);
        share_image = (CheckBox) findViewById(R.id.pref_share_image);
        download_path = (TextView) findViewById(R.id.pref_download_path);
        reverse_landscape = (CheckBox) findViewById(R.id.pref_reverse_landscape);
        open_at_latest_strip = (CheckBox) findViewById(R.id.pref_open_at_latest_strip);
        widget_always_show_latest = (CheckBox) findViewById(R.id.pref_widget_always_latest);
        TextView author = (TextView) findViewById(R.id.app_author);
        LinearLayout download_path_layout = (LinearLayout) findViewById(R.id.pref_download_path_layout);
        TextView license = (TextView) findViewById(R.id.pref_show_license);
        download_path_layout.setOnClickListener(downloadPathClickListener);
        license.setOnClickListener(licenseOnClickListener);
        author.setOnClickListener(authorOnClickListener);
        force_landscape.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reverse_landscape.setEnabled(isChecked);
                reverse_landscape.setChecked(reverse_landscape.isChecked() && isChecked);
            }
        });
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
        force_dark.setChecked(preferences.isDarkLayoutEnabled());
        hide_toolbars.setChecked(preferences.isToolbarsHidden());
        download_path.setText(preferences.getDownloadTarget());
        share_image.setChecked(preferences.isSharingImage());
        reverse_landscape.setVisibility(Build.VERSION.SDK_INT >= 9 ? View.VISIBLE : View.GONE);
        reverse_landscape.setEnabled(preferences.isForceLandscape());
        reverse_landscape.setChecked(preferences.isReversedLandscape() && preferences.isForceLandscape());
        open_at_latest_strip.setChecked(preferences.isShouldOpenAtLatestStrip());
        widget_always_show_latest.setChecked(preferences.isWidgetAlwaysShowLatest());
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
        preferences.setIsSharingImage(share_image.isChecked());
        preferences.setIsReversedLandscape(reverse_landscape.isChecked());
        preferences.setShouldOpenAtLatestStrip(open_at_latest_strip.isChecked());
        preferences.setWidgetAlwaysShowLatest(widget_always_show_latest.isChecked());
        updateWidgets();
    }

    private void updateWidgets() {
        try {
            AppWidgetManager awm = getInstance(this);
            int[] ids = awm == null ? new int[0] : awm.getAppWidgetIds(
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
