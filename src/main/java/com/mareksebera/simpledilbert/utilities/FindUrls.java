package com.mareksebera.simpledilbert.utilities;

import android.net.Uri;
import android.util.Log;

import com.mareksebera.simpledilbert.preferences.DilbertPreferences;

import org.joda.time.LocalDate;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

final public class FindUrls {
    private static final String LOG_TAG = "FindUrls";

    private static final Pattern url_match_pattern = Pattern
            .compile(".*data-image=\"([^\"]+)\".*");
    private static final Pattern date_match_pattern = Pattern
            .compile(".*([\\d]{4}-[\\d]{2}-[\\d]{2}).*");
    private static final Pattern title_match_pattern = Pattern
            .compile(".*\"comic-title-name\">([^<]+)<.*");
    private static final Pattern twitter_title_match_pattern = Pattern
            .compile(".*content=\"(.*)\".*");

    private FindUrls() {
    }

    @NonNull
    static String[] extractUrlAndTitle(String httpResponse) {
        String foundUrl = null;
        String foundTitle = null;
        boolean hasFoundUrl = false;
        boolean hasFoundTitle = false;
        try {
            Scanner scan;
            scan = new Scanner(httpResponse);

            while (!hasFoundUrl || !hasFoundTitle) {
                if (!scan.hasNextLine()) break;

                String line = scan.nextLine();

                if (line.contains("data-image")) {
                    Matcher m = url_match_pattern.matcher(line);
                    if (m.matches()) {
                        foundUrl = FindUrls.correctUrl(m.group(1));
                        hasFoundUrl = true;
                    }
                } else if (line.contains("comic-title-name")) {
                    Matcher m = title_match_pattern.matcher(line);
                    if (m.matches()) {
                        foundTitle = m.group(1);
                        hasFoundTitle = true;
                    }
                } else if (!hasFoundTitle && line.contains("twitter:title")) {
                    Matcher m = twitter_title_match_pattern.matcher(line);
                    if(m.matches()) {
                        foundTitle = m.group(1);
                        hasFoundTitle = true;
                    }
                }
            }

            scan.close();
        } catch (Throwable t) {
            Log.e(LOG_TAG, "Error Occurred", t);
        }
        return new String[]{foundUrl, foundTitle};
    }

    /**
     * Will correct urls not starting with http://, https:// and those starting with "//"
     *
     * @param foundUrl String url in raw form parsed out of html
     * @return String corrected URL
     */
    private static String correctUrl(String foundUrl) {
        if (!foundUrl.startsWith("https://") && !foundUrl.startsWith("http://")) {
            if (foundUrl.startsWith("//")) {
                foundUrl = "https:" + foundUrl;
            } else {
                foundUrl = "https://" + foundUrl;
            }
        }
        return foundUrl;
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