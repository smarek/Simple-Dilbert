package com.mareksebera.simpledilbert.preferences;

import android.Manifest;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mareksebera.simpledilbert.R;
import com.mareksebera.simpledilbert.core.DilbertFragmentActivity;
import com.mareksebera.simpledilbert.picker.FolderPickerActivity;
import com.mareksebera.simpledilbert.widget.WidgetProvider;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.getInstance;

public final class DilbertPreferencesActivity extends AppCompatActivity {

    private static final int REQUEST_DOWNLOAD_TARGET = 1;
    private static final String TAG = "DilbertPreferencesAct";
    private final OnClickListener licenseOnClickListener = v -> showLicenseDialog();
    private final OnClickListener authorOnClickListener = v -> {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "marek@msebera.cz", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Simple Dilbert");
        startActivity(Intent.createChooser(emailIntent, "Simple Dilbert"));
    };
    private CheckBox force_landscape, force_dark, hide_toolbars,
            share_image, reverse_landscape,
            open_at_latest_strip, widget_always_show_latest, widget_show_title;
    private TextView download_path;
    private Button export_urls;
    private DilbertPreferences preferences;
    private final OnClickListener downloadPathClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (!isStoragePermissionGranted()) {
                Toast.makeText(DilbertPreferencesActivity.this, "Storage permission denied, cannot continue", Toast.LENGTH_LONG).show();
                return;
            }
            Intent downloadPathSelector = new Intent(
                    DilbertPreferencesActivity.this, FolderPickerActivity.class);
            downloadPathSelector.setData(Uri.fromFile(new File(preferences.getDownloadTarget())));
            startActivityForResult(downloadPathSelector,
                    REQUEST_DOWNLOAD_TARGET);
        }
    };
    private final OnClickListener defaultZoomLevelClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(DilbertPreferencesActivity.this)
                    .setTitle(R.string.pref_default_zoom_level)
                    .setSingleChoiceItems(new CharSequence[]{"Minimum", "Medium", "Maximum"}, preferences.getDefaultZoomLevel(), (dialog, which) -> preferences.setDefaultZoomLevel(which))
                    .setCancelable(true)
                    .setNeutralButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        }
    };
    private final OnClickListener exportUrlsListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> e : preferences.getCachedUrls().entrySet()) {
                sb.append(String.format("%s :: %s\n", e.getKey(), e.getValue()));
            }
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.menu_share)));
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
            if (path != null && path.getPath() != null) {
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
                (dialog, which) -> dialog.dismiss());
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
        force_landscape = findViewById(R.id.pref_force_landscape);
        force_dark = findViewById(R.id.pref_force_dark_background);
        hide_toolbars = findViewById(R.id.pref_hide_toolbars);
        share_image = findViewById(R.id.pref_share_image);
        download_path = findViewById(R.id.pref_download_path);
        reverse_landscape = findViewById(R.id.pref_reverse_landscape);
        open_at_latest_strip = findViewById(R.id.pref_open_at_latest_strip);
        widget_always_show_latest = findViewById(R.id.pref_widget_always_latest);
        widget_show_title = findViewById(R.id.pref_widget_show_title);
        export_urls = findViewById(R.id.pref_export_urls);
        TextView default_zoom_level = findViewById(R.id.pref_default_zoom_level);
        TextView author = findViewById(R.id.app_author);
        LinearLayout download_path_layout = findViewById(R.id.pref_download_path_layout);
        TextView license = findViewById(R.id.pref_show_license);
        default_zoom_level.setOnClickListener(defaultZoomLevelClickListener);
        download_path_layout.setOnClickListener(downloadPathClickListener);
        license.setOnClickListener(licenseOnClickListener);
        author.setOnClickListener(authorOnClickListener);
        force_landscape.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reverse_landscape.setEnabled(isChecked);
            reverse_landscape.setChecked(reverse_landscape.isChecked() && isChecked);
        });
        export_urls.setOnClickListener(exportUrlsListener);
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
        reverse_landscape.setVisibility(View.VISIBLE);
        reverse_landscape.setEnabled(preferences.isForceLandscape());
        reverse_landscape.setChecked(preferences.isReversedLandscape() && preferences.isForceLandscape());
        open_at_latest_strip.setChecked(preferences.isShouldOpenAtLatestStrip());
        widget_always_show_latest.setChecked(preferences.isWidgetAlwaysShowLatest());
        widget_show_title.setChecked(preferences.isWidgetShowTitle());
        export_urls.setEnabled(true);
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
        preferences.setWidgetShowTitle(widget_show_title.isChecked());
        export_urls.setEnabled(false);
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

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

}
