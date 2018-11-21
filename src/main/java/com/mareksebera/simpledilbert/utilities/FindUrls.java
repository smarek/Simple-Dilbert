package com.mareksebera.simpledilbert.utilities;

import android.net.Uri;
import android.util.Log;

import com.mareksebera.simpledilbert.preferences.DilbertPreferences;

import org.joda.time.LocalDate;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import androidx.annotation.NonNull;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.util.EntityUtils;

final public class FindUrls {
    private static final String LOG_TAG = "FindUrls";

    private static final Pattern url_match_pattern = Pattern
            .compile(".*data-image=\"([^\"]+)\".*");
    private static final Pattern date_match_pattern = Pattern
            .compile(".*([\\d]{4}-[\\d]{2}-[\\d]{2}).*");
    private static final Pattern title_match_pattern = Pattern
            .compile(".*\"comic-title-name\">([^<]+)<.*");

    private FindUrls() {
    }

    @NonNull
    static String[] extractUrlAndTitle(HttpResponse response) {
        String foundUrl = null;
        String foundTitle = null;
        boolean hasFoundUrl = false;
        boolean hasFoundTitle = false;
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

            while (!hasFoundUrl || !hasFoundTitle) {
                if (!scan.hasNextLine()) break;

                String line = scan.nextLine();

                if (line.contains("data-image")) {
                    Matcher m = url_match_pattern.matcher(line);
                    if (m.matches()) {
                        foundUrl = "http:" + m.group(1);
                        hasFoundUrl = true;
                    }
                } else if (line.contains("comic-title-name")) {
                    Matcher m = title_match_pattern.matcher(line);
                    if (m.matches()) {
                        foundTitle = m.group(1);
                        hasFoundTitle = true;
                    }
                }
            }

            scan.close();
            EntityUtils.consume(response.getEntity());
        } catch (Throwable t) {
            Log.e(LOG_TAG, "Error Occurred", t);
        }
        return new String[]{foundUrl, foundTitle};
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