package com.mareksebera.simpledilbert.favorites;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mareksebera.simpledilbert.R;
import com.mareksebera.simpledilbert.core.DilbertFragmentInterface;
import com.mareksebera.simpledilbert.preferences.DilbertPreferences;
import com.mareksebera.simpledilbert.utilities.ActionBarUtility;

import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;

public final class DilbertFavoritedActivity extends AppCompatActivity implements DilbertFragmentInterface {

    public static final String INTENT_OFFLINE = "intent_extra_offline_mode";
    private ViewPager viewPager;
    private final Random random = new Random();
    private DilbertFavoritedFragmentAdapter adapter;
    public static final int MENU_RANDOM = 1;

    private final ViewPager.OnPageChangeListener pageChangedListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            setTitle(adapter.getPageTitle(position));
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstance) {
        DilbertPreferences preferences = new DilbertPreferences(this);
        if (preferences.isForceLandscape())
            setRequestedOrientation(preferences.getLandscapeOrientation());
        setTheme(preferences.isDarkLayoutEnabled() ? R.style.AppThemeDark
                : R.style.AppThemeLight);
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_dilbert_fragments);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        boolean isOfflineMode = getIntent().getBooleanExtra(INTENT_OFFLINE, false);
        viewPager = findViewById(R.id.view_pager);
        adapter = new DilbertFavoritedFragmentAdapter(
                getSupportFragmentManager(), isOfflineMode ? preferences.getCachedDates() : preferences.getFavoritedItems());
        if (adapter.getCount() == 0) {
            Toast.makeText(this, R.string.toast_no_favorites, Toast.LENGTH_LONG)
                    .show();
            finish();
        }
        viewPager.addOnPageChangeListener(pageChangedListener);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(adapter.getCount());
        if (preferences.isToolbarsHidden())
            ActionBarUtility.toggleActionBar(this, viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_RANDOM, 1, R.string.menu_random)
                .setIcon(R.drawable.ic_menu_shuffle)
                .setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RANDOM:
                viewPager.setCurrentItem(random.nextInt(adapter.getCount()));
                return true;
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
