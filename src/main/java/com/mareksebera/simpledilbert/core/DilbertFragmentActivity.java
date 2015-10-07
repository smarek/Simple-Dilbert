package com.mareksebera.simpledilbert.core;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;

import com.mareksebera.simpledilbert.R;
import com.mareksebera.simpledilbert.favorites.DilbertFavoritedActivity;
import com.mareksebera.simpledilbert.preferences.DilbertPreferences;
import com.mareksebera.simpledilbert.preferences.DilbertPreferencesActivity;
import com.mareksebera.simpledilbert.utilities.ActionBarUtility;
import com.mareksebera.simpledilbert.utilities.FindUrls;

import org.joda.time.LocalDate;

import java.util.Calendar;
import java.util.Locale;

public final class
        DilbertFragmentActivity extends AppCompatActivity implements DilbertFragmentInterface {

    private static final int MENU_DATEPICKER = 1, MENU_LATEST = 3, MENU_OLDEST = 4,
            MENU_SHOW_FAVORITES = 5, MENU_SHUFFLE = 6, MENU_SETTINGS = 7;
    private final DatePickerDialog.OnDateSetListener dilbertOnDateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            LocalDate selDate = LocalDate.parse(String.format(new Locale(
                            "en"), "%d-%d-%d", year, monthOfYear + 1, dayOfMonth),
                    DilbertPreferences.DATE_FORMATTER);
            setCurrentDate(selDate);
        }
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
    private ViewPager viewPager;
    private DilbertFragmentAdapter adapter;
    private DilbertPreferences preferences;

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
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        adapter = new DilbertFragmentAdapter(getSupportFragmentManager());
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
        viewPager.setCurrentItem(adapter.getPositionForDate(preferences
                .getCurrentDate()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final int category = 0;
        MenuItemCompat.setShowAsAction(
                menu.add(category, MENU_DATEPICKER, 1, R.string.menu_datepicker)
                        .setIcon(R.drawable.ic_menu_datepicker),
                MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setShowAsAction(
                menu.add(category, MENU_SHUFFLE, 2, R.string.menu_random)
                        .setIcon(R.drawable.ic_menu_shuffle),
                MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setShowAsAction(
                menu.add(category, MENU_SHOW_FAVORITES, 6, R.string.menu_show_favorite),
                MenuItemCompat.SHOW_AS_ACTION_NEVER);
        MenuItemCompat.setShowAsAction(
                menu.add(category, MENU_LATEST, 5, R.string.menu_latest),
                MenuItemCompat.SHOW_AS_ACTION_NEVER);
        MenuItemCompat.setShowAsAction(
                menu.add(category, MENU_OLDEST, 5, R.string.menu_oldest),
                MenuItemCompat.SHOW_AS_ACTION_NEVER);
        MenuItemCompat.setShowAsAction(
                menu.add(category, MENU_SETTINGS, 8, R.string.menu_settings),
                MenuItemCompat.SHOW_AS_ACTION_NEVER);
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
            case MENU_SETTINGS:
                startActivity(new Intent(this, DilbertPreferencesActivity.class));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Compat helper method
    public void toggleActionBar() {
        ActionBarUtility.toggleActionBar(this, viewPager);
    }

}
