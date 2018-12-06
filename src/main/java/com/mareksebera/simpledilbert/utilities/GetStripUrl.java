package com.mareksebera.simpledilbert.utilities;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mareksebera.simpledilbert.preferences.DilbertPreferences;

import org.joda.time.LocalDate;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class GetStripUrl extends AsyncTask<Void, Void, String[]> {

    private static final String TAG = "GetStripUrl";
    private final DilbertPreferences preferences;
    private WeakReference<ProgressBar> progressBar;
    private final LocalDate currDate;
    private final GetStripUrlInterface listener;
    private String[] handledResponse;
    private RequestQueue volleyRequestQueue;
    private final Response.Listener<String> getSuccess = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {

        }
    };
    private final Response.ErrorListener getError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }
    };

    public GetStripUrl(Context ctx, GetStripUrlInterface listener,
                       DilbertPreferences preferences, LocalDate currDate) {
        this(ctx, listener, preferences, currDate, null);
    }

    public GetStripUrl(Context ctx, GetStripUrlInterface listener,
                       DilbertPreferences preferences, LocalDate currDate,
                       ProgressBar progressBar) {
        this.preferences = preferences;
        this.progressBar = new WeakReference<>(progressBar);
        this.currDate = currDate;
        this.listener = listener;
        this.volleyRequestQueue = Volley.newRequestQueue(ctx);
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

        RequestFuture<String> future = RequestFuture.newFuture();

        volleyRequestQueue.add(
                new StringRequest(Request.Method.GET, "https://dilbert.com/strip/"
                        + currDate.toString(DilbertPreferences.DATE_FORMATTER) + "/", future, future)
        );

        try {
            handledResponse = handleParse(future.get(15, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        return handledResponse;
    }

    private String[] handleParse(String responseString) {
        String[] found = FindUrls.extractUrlAndTitle(responseString);
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
