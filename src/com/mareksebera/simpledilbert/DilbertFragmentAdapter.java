package com.mareksebera.simpledilbert;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public final class DilbertFragmentAdapter extends FragmentPagerAdapter {

	static {
		/**
		 * Set default time-zone, because strips are published in New York
		 * timezone on midnight
		 * */
		DateTimeZone.setDefault(DilbertPreferences.TIME_ZONE);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return getDateForPosition(position).toString(
				DilbertPreferences.DATE_FORMATTER);
	}

	public DilbertFragmentAdapter(FragmentManager fm) {
		super(fm);
		this.countCache = Days.daysBetween(
				DilbertPreferences.getFirstStripDate(),
				DateMidnight.now(DilbertPreferences.TIME_ZONE)).getDays() + 1;
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

	public DateMidnight getDateForPosition(int position) {
		return DateMidnight.now(DilbertPreferences.TIME_ZONE).minusDays(
				(getCount() - position) - 1);
	}

	private int countCache = 0;

	@Override
	public int getCount() {
		return countCache;
	}

	public int getPositionForDate(DateMidnight date) {
		return getCount()
				- Days.daysBetween(date,
						DateMidnight.now(DilbertPreferences.TIME_ZONE))
						.plus(Days.ONE).getDays();
	}

}
