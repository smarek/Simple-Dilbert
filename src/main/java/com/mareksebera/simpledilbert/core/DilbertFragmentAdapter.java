package com.mareksebera.simpledilbert.core;

import android.os.Bundle;

import com.mareksebera.simpledilbert.preferences.DilbertPreferences;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

final class DilbertFragmentAdapter extends FragmentPagerAdapter {

    private int countCache;
    private DilbertPreferences preferences;

    DilbertFragmentAdapter(FragmentManager fm, DilbertPreferences preferences) {
        super(fm);
        this.countCache = Days.daysBetween(
                DilbertPreferences.getFirstStripDate(),
                LocalDate.now()).getDays() + 1;
        this.preferences = preferences;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String cachedTitle = preferences.getCachedTitle(getDateForPosition(position));
        return getDateForPosition(position).toString(
                DilbertPreferences.NICE_DATE_FORMATTER) + ((cachedTitle == null || cachedTitle.isEmpty()) ? "" : " : " + cachedTitle);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment f = new DilbertFragment();
        Bundle bundle = new Bundle();
        bundle.putString(
                DilbertFragment.ARGUMENT_DATE,
                getDateForPosition(position).toString(
                        DilbertPreferences.DATE_FORMATTER));
        f.setArguments(bundle);
        return f;
    }

    LocalDate getDateForPosition(int position) {
        return LocalDate.now().minusDays(
                (getCount() - position) - 1);
    }

    @Override
    public int getCount() {
        return countCache;
    }

    int getPositionForDate(LocalDate date) {
        return getCount()
                - Days.daysBetween(date,
                LocalDate.now())
                .plus(Days.ONE).getDays();
    }

}
