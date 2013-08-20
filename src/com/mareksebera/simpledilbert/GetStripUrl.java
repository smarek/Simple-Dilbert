package com.mareksebera.simpledilbert;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateMidnight;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.FailReason.FailType;

final class GetStripUrl extends AsyncTask<Void, Void, String> {

	private static final String TAG = "GetStripUrl";
	private DilbertPreferences preferences;
	private ProgressBar progressBar;
	private DateMidnight currDate;
	private GetStripUrlInterface listener;

	public GetStripUrl(GetStripUrlInterface listener,
			DilbertPreferences preferences, DateMidnight currDate) {
		this(listener, preferences, currDate, null);
	}

	public GetStripUrl(GetStripUrlInterface listener,
			DilbertPreferences preferences, DateMidnight currDate,
			ProgressBar progressBar) {
		this.preferences = preferences;
		this.progressBar = progressBar;
		this.currDate = currDate;
		this.listener = listener;
	}

	@Override
	protected String doInBackground(Void... params) {
		if (this.currDate == null) {
			Log.e(TAG, "Cannot load for null date");
			return null;
		}
		String cached = this.preferences.getCachedUrl(this.currDate);
		if (cached != null) {
			return cached;
		}
		HttpGet get = new HttpGet("http://www.dilbert.com/strips/comic/"
				+ currDate.toString(DilbertPreferences.DATE_FORMATTER) + "/");
		HttpResponse response = null;
		try {
			HttpClient client = new DefaultHttpClient();
			response = client.execute(get);
		} catch (Exception e) {
			Log.e(TAG, "HttpGet failed", e);
		}
		if (response == null)
			return null;
		if (preferences.isSlowNetwork()) {
			return handleSlowNetworkParse(response);
		} else {
			return handleFastNetworkParse(response);
		}
	}

	private String handleFastNetworkParse(HttpResponse response) {
		for (String s : FindUrls.extractUrls(response)) {
			/**
			 * This method can only accept gif URLs with appropriate suffixes
			 * */
			if (s.endsWith(".strip.gif") || s.endsWith(".sunday.gif")
					|| s.endsWith(".strip.zoom.gif")) {
				s = s.replace(".strip.gif", ".strip.zoom.gif");
				s = s.replace(".sunday.gif", ".strip.zoom.gif");
				s = s.replace(".strip.strip", ".strip");
				/**
				 * This is the only place where pair date-url is saved into
				 * preferences
				 * */
				preferences
						.saveCurrentUrl(currDate
								.toString(DilbertPreferences.DATE_FORMATTER), s);
				/**
				 * Not using method loadImage() as it would be inefficient
				 * */
				return s;
			}
		}
		return null;
	}

	private String handleSlowNetworkParse(HttpResponse response) {
		String result = null;
		try {
			result = EntityUtils.toString(response.getEntity());
		} catch (Throwable t) {
			Log.e("GetStripUrl", "handleSlowNetwork allocation failed");
			return null;
		}
		result = result.replace("\"/dyn/str_strip",
				"\"http://www.dilbert.com/dyn/str_strip");
		for (String s : FindUrls.extractUrls(result)) {
			/**
			 * This method can only accept gif URLs with appropriate suffixes
			 * */
			if (s.endsWith(".strip.gif") || s.endsWith(".sunday.gif")
					|| s.endsWith(".strip.zoom.gif")) {
				s = s.replace(".strip.gif", ".strip.zoom.gif");
				s = s.replace(".sunday.gif", ".strip.zoom.gif");
				s = s.replace(".strip.strip", ".strip");
				/**
				 * This is the only place where pair date-url is saved into
				 * preferences
				 * */
				preferences
						.saveCurrentUrl(currDate
								.toString(DilbertPreferences.DATE_FORMATTER), s);
				/**
				 * Not using method loadImage() as it would be inefficient
				 * */
				return s;
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if (result == null) {
			if (listener != null)
				listener.imageLoadFailed(preferences.getCachedUrl(currDate),
						new FailReason(FailType.NETWORK_DENIED,
								new ParseException()));
			else
				Log.e(TAG, "Listener is NULL");
		} else {
			if (listener != null)
				listener.displayImage(result);
			else
				Log.e(TAG, "listener is NULL");
		}
	}

	/**
	 * Indicates that there is any work in progress
	 * */
	@Override
	protected void onPreExecute() {
		if (progressBar != null)
			progressBar.setVisibility(View.VISIBLE);
	}
}
