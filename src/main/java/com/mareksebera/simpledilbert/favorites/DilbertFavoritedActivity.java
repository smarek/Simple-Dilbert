package com.mareksebera.simpledilbert.favorites;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.mareksebera.simpledilbert.R;
import com.mareksebera.simpledilbert.core.DilbertFragmentInterface;
import com.mareksebera.simpledilbert.preferences.DilbertPreferences;
import com.mareksebera.simpledilbert.utilities.ActionBarUtility;

public final class DilbertFavoritedActivity extends ActionBarActivity implements DilbertFragmentInterface {

    private ViewPager viewPager;
    private DilbertFavoritedFragmentAdapter adapter;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        adapter = new DilbertFavoritedFragmentAdapter(
                getSupportFragmentManager(), preferences.getFavoritedItems());
        if (adapter.getCount() == 0) {
            Toast.makeText(this, R.string.toast_no_favorites, Toast.LENGTH_LONG)
                    .show();
            finish();
        }
        viewPager.setOnPageChangeListener(pageChangedListener);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(adapter.getCount());
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
