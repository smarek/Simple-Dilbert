package com.mareksebera.simpledilbert.core;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mareksebera.simpledilbert.preferences.DilbertPreferences;

import org.joda.time.Days;
import org.joda.time.LocalDate;

final class DilbertFragmentAdapter extends FragmentPagerAdapter {

    private int countCache = 0;

    public DilbertFragmentAdapter(FragmentManager fm) {
        super(fm);
        this.countCache = Days.daysBetween(
                DilbertPreferences.getFirstStripDate(),
                LocalDate.now()).getDays() + 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return getDateForPosition(position).toString(
                DilbertPreferences.NICE_DATE_FORMATTER);
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

    public LocalDate getDateForPosition(int position) {
        return LocalDate.now().minusDays(
                (getCount() - position) - 1);
    }

    @Override
    public int getCount() {
        return countCache;
    }

    public int getPositionForDate(LocalDate date) {
        return getCount()
                - Days.daysBetween(date,
                LocalDate.now())
                .plus(Days.ONE).getDays();
    }

}
