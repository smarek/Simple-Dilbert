package com.mareksebera.dilbert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class DilbertPreferences {

	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;

	private static final String PREF_CURRENT_DATE = "dilbert_current_date";
	private static final String PREF_CURRENT_URL = "dilbert_current_url";
	private static final String PREF_HIGH_QUALITY_ENABLED = "dilbert_use_high_quality";

	public static final DateTimeFormatter dateFormatter = DateTimeFormat
			.forPattern("yyyy-MM-dd");

	public DilbertPreferences(Context _context) {
		preferences = PreferenceManager.getDefaultSharedPreferences(_context);
		editor = preferences.edit();
	}

	public DateMidnight getCurrentDate() {
		String savedDate = preferences.getString(PREF_CURRENT_DATE, null);
		if (savedDate == null)
			return DateMidnight.now();
		else
			return DateMidnight.parse(savedDate, dateFormatter);
	}

	public boolean saveCurrentDate(DateMidnight currentDate) {
		editor.putString(PREF_CURRENT_DATE,
				currentDate.toString(DilbertPreferences.dateFormatter));
		return editor.commit();
	}

	public String getLastUrl() {
		return preferences.getString(PREF_CURRENT_URL, null);
	}

	public boolean isHighQualityOn() {
		return preferences.getBoolean(PREF_HIGH_QUALITY_ENABLED, true);
	}

	public boolean saveCurrentUrl(String date, String s) {
		editor.putString(PREF_CURRENT_URL, s);
		editor.putString(date, s);
		return editor.commit();
	}

	public boolean toggleHighQuality() {
		return editor.putBoolean(PREF_HIGH_QUALITY_ENABLED, !isHighQualityOn())
				.commit();
	}

	public String getCachedUrl(String dateKey) {
		return preferences.getString(dateKey, null);
	}

	public boolean removeCache(DateMidnight currentDate) {
		return editor.remove(
				currentDate.toString(DilbertPreferences.dateFormatter))
				.commit();
	}

	public boolean isFavorited(DateMidnight currentDay) {
		return preferences.getBoolean(toFavoritedKey(currentDay), false);
	}

	public boolean toggleIsFavorited(DateMidnight currentDay) {
		boolean newState = !isFavorited(currentDay);
		editor.putBoolean(toFavoritedKey(currentDay), newState).commit();
		return newState;
	}

	private String toFavoritedKey(DateMidnight currentDay) {
		return "favorite_"
				+ currentDay.toString(DilbertPreferences.dateFormatter);
	}

	public List<FavoritedItem> getFavoritedItems() {
		List<FavoritedItem> favorites = new ArrayList<FavoritedItem>();
		Map<String, ?> allPreferences = preferences.getAll();
		for (String key : allPreferences.keySet()) {
			if (key.startsWith("favorite_")
					&& (Boolean) allPreferences.get(key)) {
				String date = key.replace("favorite_", "");
				favorites.add(new FavoritedItem(DateMidnight.parse(date,
						dateFormatter), (String) allPreferences.get(date)));
			}
		}
		return favorites;
	}

	public void downloadImageViaManager(Activity activity) {
		try {
			DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Request request = new DownloadManager.Request(
					Uri.parse(getLastUrl()));
			request.setVisibleInDownloadsUi(true);
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			dm.enqueue(request);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(activity, R.string.download_manager_unsupported,
					Toast.LENGTH_LONG).show();
		}
	}

}
