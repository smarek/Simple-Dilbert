package com.mareksebera.simpledilbert.favorites;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mareksebera.simpledilbert.preferences.DilbertPreferences;
import com.mareksebera.simpledilbert.core.DilbertFragment;

import java.util.List;

final class DilbertFavoritedFragmentAdapter extends FragmentPagerAdapter {

	@Override
	public CharSequence getPageTitle(int position) {
		return favorites.get(position).getDate().toString(
				DilbertPreferences.DATE_FORMATTER);
	}

	private List<FavoritedItem> favorites = null;

	public DilbertFavoritedFragmentAdapter(FragmentManager fm,
			List<FavoritedItem> list) {
		super(fm);
		this.favorites = list;
	}

	@Override
	public Fragment getItem(int position) {
		Fragment f = new DilbertFragment();
		Bundle bundle = new Bundle();
		bundle.putString(DilbertFragment.ARGUMENT_DATE, favorites.get(position)
				.getDate().toString(DilbertPreferences.DATE_FORMATTER));
		f.setArguments(bundle);
		return f;
	}

	@Override
	public int getCount() {
		return favorites == null ? 0 : favorites.size();
	}

}
