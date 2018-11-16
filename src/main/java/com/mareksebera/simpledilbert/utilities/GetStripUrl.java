package com.mareksebera.simpledilbert.utilities;

import android.accounts.NetworkErrorException;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.mareksebera.simpledilbert.preferences.DilbertPreferences;

import org.joda.time.LocalDate;

import java.io.IOException;
import java.lang.ref.WeakReference;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;

public final class GetStripUrl extends AsyncTask<Void, Void, String[]> {

    private static final String TAG = "GetStripUrl";
    private final DilbertPreferences preferences;
    private WeakReference<ProgressBar> progressBar;
    private final LocalDate currDate;
    private final GetStripUrlInterface listener;

    public GetStripUrl(GetStripUrlInterface listener,
                       DilbertPreferences preferences, LocalDate currDate) {
        this(listener, preferences, currDate, null);
    }

    public GetStripUrl(GetStripUrlInterface listener,
                       DilbertPreferences preferences, LocalDate currDate,
                       ProgressBar progressBar) {
        this.preferences = preferences;
        this.progressBar = new WeakReference<>(progressBar);
        this.currDate = currDate;
        this.listener = listener;
    }

    @Override
    protected String[] doInBackground(Void... params) {
        if (this.currDate == null) {
            Log.e(TAG, "Cannot load for null date");
            return null;
        }
        String cached = this.preferences.getCachedUrl(this.currDate);
        if (cached != null) {
            return new String[]{cached, this.preferences.getCachedTitle(this.currDate)};
        }
        HttpGet get = new HttpGet("https://dilbert.com/strip/"
                + currDate.toString(DilbertPreferences.DATE_FORMATTER) + "/");
        HttpResponse response = null;
        CloseableHttpClient client = null;
        try {
            client = HttpClients.createSystem();
            response = client.execute(get);
        } catch (Exception e) {
            Log.e(TAG, "HttpGet failed", e);
        }
        if (response == null) {
            return null;
        }
        String[] rtn = handleParse(response);

        try {
            client.close();
        } catch (IOException e) {
            Log.e(TAG, "Closing HttpClient failed", e);
        }

        return rtn;
    }

    private String[] handleParse(HttpResponse response) {
        String[] found = FindUrls.extractUrlAndTitle(response);
        if (found.length == 2 && found[0] != null && found[1] != null) {
            preferences
                    .saveCurrentUrl(currDate
                            .toString(DilbertPreferences.DATE_FORMATTER), found[0]);
            preferences
                    .saveCurrentTitle(currDate
                            .toString(DilbertPreferences.DATE_FORMATTER), found[1]);
        }
        return found;
    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result == null) {
            if (listener != null) {
                listener.imageLoadFailed(preferences.getCachedUrl(currDate),
                        new NetworkErrorException("Network Denied"));
            } else {
                Log.e(TAG, "Listener is NULL");
            }
        } else {
            if (listener != null) {
                listener.displayImage(result[0], result[1]);
            } else {
                Log.e(TAG, "listener is NULL");
            }
        }
    }

    /**
     * Indicates that there is any work in progress
     */
    @Override
    protected void onPreExecute() {
        if (progressBar.get() != null) {
            progressBar.get().setVisibility(View.VISIBLE);
        }
    }
}
