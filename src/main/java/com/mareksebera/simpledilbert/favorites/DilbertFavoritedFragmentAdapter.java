package com.mareksebera.simpledilbert.favorites;

import android.os.Bundle;

import com.mareksebera.simpledilbert.core.DilbertFragment;
import com.mareksebera.simpledilbert.preferences.DilbertPreferences;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

final class DilbertFavoritedFragmentAdapter extends FragmentPagerAdapter {

    private List<FavoritedItem> favorites = null;

    DilbertFavoritedFragmentAdapter(FragmentManager fm,
                                    List<FavoritedItem> list) {
        super(fm);
        this.favorites = list;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return favorites.get(position).getDate().toString(
                DilbertPreferences.DATE_FORMATTER);
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
