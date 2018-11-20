package com.mareksebera.simpledilbert.core;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.mareksebera.simpledilbert.R;
import com.mareksebera.simpledilbert.favorites.DilbertFavoritedActivity;
import com.mareksebera.simpledilbert.preferences.DilbertPreferences;
import com.mareksebera.simpledilbert.preferences.DilbertPreferencesActivity;
import com.mareksebera.simpledilbert.utilities.ActionBarUtility;
import com.mareksebera.simpledilbert.utilities.FindUrls;

import org.joda.time.LocalDate;

import java.util.Calendar;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;

public final class
DilbertFragmentActivity extends AppCompatActivity implements DilbertFragmentInterface {

    private static final int MENU_DATEPICKER = 1, MENU_LATEST = 3, MENU_OLDEST = 4,
            MENU_SHOW_FAVORITES = 5, MENU_SHUFFLE = 6, MENU_SETTINGS = 7, MENU_SHOW_OFFLINE = 8;
    private ViewPager viewPager;
    private DilbertFragmentAdapter adapter;
    private DilbertPreferences preferences;
    private final DatePickerDialog.OnDateSetListener dilbertOnDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
        LocalDate selDate = LocalDate.parse(String.format(new Locale(
                        "en"), "%d-%d-%d", year, monthOfYear + 1, dayOfMonth),
                DilbertPreferences.DATE_FORMATTER);
        setCurrentDate(selDate);
    };
    private final ViewPager.OnPageChangeListener pageChangedListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            preferences.saveCurrentDate(adapter.getDateForPosition(position));
            setTitle(adapter.getPageTitle(position));
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setTitle(adapter.getPageTitle(viewPager.getCurrentItem()));
        }
    };

    private void setCurrentDate(LocalDate date) {
        preferences.saveCurrentDate(date);
        viewPager.setCurrentItem(adapter.getPositionForDate(date));
    }

    @Override
    protected void onCreate(Bundle savedInstance) {
        preferences = new DilbertPreferences(this);
        setTheme(preferences.isDarkLayoutEnabled() ? R.style.AppThemeDark
                : R.style.AppThemeLight);
        super.onCreate(savedInstance);
        if (preferences.isForceLandscape())
            setRequestedOrientation(preferences.getLandscapeOrientation());
        setContentView(R.layout.activity_dilbert_fragments);
        viewPager = findViewById(R.id.view_pager);
        adapter = new DilbertFragmentAdapter(getSupportFragmentManager(), preferences);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(pageChangedListener);
        if (preferences.isToolbarsHidden())
            ActionBarUtility.toggleActionBar(this, viewPager);
        tryHandleUrlIntent();
    }

    private void tryHandleUrlIntent() {
        if (getIntent() != null && getIntent().getData() != null) {
            Uri path = getIntent().getData();
            LocalDate intentDate = FindUrls.extractCurrentDateFromIntentUrl(path);
            if (intentDate != null)
                setCurrentDate(intentDate);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(DilbertFragment.BROADCAST_TITLE_UPDATE));
        viewPager.setCurrentItem(adapter.getPositionForDate(preferences
                .getCurrentDate()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final int category = 0;
        menu.add(category, MENU_DATEPICKER, 1, R.string.menu_datepicker)
                .setIcon(R.drawable.ic_menu_datepicker)
                .setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
        menu.add(category, MENU_SHUFFLE, 2, R.string.menu_random)
                .setIcon(R.drawable.ic_menu_shuffle)
                .setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
        menu.add(category, MENU_SHOW_FAVORITES, 6, R.string.menu_show_favorite)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
        menu.add(category, MENU_LATEST, 5, R.string.menu_latest)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
        menu.add(category, MENU_OLDEST, 5, R.string.menu_oldest)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
        menu.add(category, MENU_SHOW_OFFLINE, 6, R.string.menu_show_offline)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
        menu.add(category, MENU_SETTINGS, 8, R.string.menu_settings)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_DATEPICKER:
                showDatePicker();
                return true;
            case MENU_LATEST:
                setCurrentDate(LocalDate.now());
                return true;
            case MENU_OLDEST:
                setCurrentDate(DilbertPreferences.getFirstStripDate());
                return true;
            case MENU_SHOW_FAVORITES:
                startActivity(new Intent(this, DilbertFavoritedActivity.class));
                return true;
            case MENU_SHUFFLE:
                setCurrentDate(DilbertPreferences.getRandomDate());
                return true;
            case MENU_SHOW_OFFLINE:
                Intent offlineIntent = new Intent(this, DilbertFavoritedActivity.class);
                offlineIntent.putExtra(DilbertFavoritedActivity.INTENT_OFFLINE, true);
                startActivity(offlineIntent);
                return true;
            case MENU_SETTINGS:
                startActivity(new Intent(this, DilbertPreferencesActivity.class));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Compat helper method
    public void toggleActionBar() {
        preferences.setIsToolbarsHidden(!preferences.isToolbarsHidden());
        ActionBarUtility.toggleActionBar(this, viewPager);
    }

}
