package com.mareksebera.simpledilbert.utilities;

import android.net.Uri;
import android.util.Log;

import com.mareksebera.simpledilbert.preferences.DilbertPreferences;

import org.joda.time.LocalDate;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.util.EntityUtils;

final public class FindUrls {
    private static final String LOG_TAG = "FindUrls";

    private FindUrls() {
    }

    private static final Pattern url_match_pattern = Pattern
            .compile("<img.*img-comic.*src=\"([a-zA-Z0-9:/\\.]*)\"\\s+");

    private static final Pattern date_match_pattern = Pattern
            .compile(".*([\\d]{4}-[\\d]{2}-[\\d]{2}).*");

    public static String extractUrls(HttpResponse response) {
        String found = null;
        try {
            Scanner scan;
            Header contentEncoding = response
                    .getFirstHeader("Content-Encoding");
            if (contentEncoding != null
                    && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                scan = new Scanner(new GZIPInputStream(response.getEntity()
                        .getContent()));
            } else {
                scan = new Scanner(response.getEntity().getContent());
            }

            found = scan.findWithinHorizon(url_match_pattern, 0);
            if (null != found) {
                Matcher m = url_match_pattern.matcher(found);
                if (m.matches())
                    found = m.group(1);
            }
            scan.close();
            EntityUtils.consume(response.getEntity());
        } catch (Throwable t) {
            Log.e(LOG_TAG, "Error Occurred", t);
        }
        return found;
    }

    public static LocalDate extractCurrentDateFromIntentUrl(Uri path) {
        try {
            Matcher m = date_match_pattern.matcher(path.toString());
            if (m.matches()) {
                return LocalDate.parse(m.group(1), DilbertPreferences.DATE_FORMATTER);
            }
        } catch (Throwable t) {
            Log.e(LOG_TAG, "extractCurrentDateFromIntentUrl failed", t);
        }
        return null;
    }
}