package com.mareksebera.simpledilbert.utilities;

import android.net.Uri;
import android.util.Log;

import com.mareksebera.simpledilbert.preferences.DilbertPreferences;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

final public class FindUrls {
    private static final String LOG_TAG = "FindUrls";

    private FindUrls() {
    }

    private static final Pattern url_match_pattern = Pattern
            .compile("/dyn/str_strip(.*).gif");

    private static final Pattern date_match_pattern = Pattern
            .compile(".*([\\d]{4}-[\\d]{2}-[\\d]{2}).*");

    public static List<String> extractUrls(String input) {
        List<String> result = new ArrayList<String>();

        Pattern pattern = Pattern
                .compile("\\b(((ht|f)tp(s?)://|~/|/)|www.)"
                        + "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|[a-z]{2}))(:[\\d]{1,5})?"
                        + "(((/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|/)+|\\?|#)?"
                        + "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?"
                        + "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)"
                        + "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?"
                        + "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*"
                        + "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b");

        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            result.add(matcher.group());
        }

        return result;
    }

    public static List<String> extractUrls(HttpResponse response) {
        List<String> found = new ArrayList<String>();
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

            String match;
            while ((match = scan.findWithinHorizon(url_match_pattern, 0)) != null) {
                found.add(match.replace("/dyn/str_strip",
                        "http://www.dilbert.com/dyn/str_strip"));
            }
            scan.close();
            response.getEntity().consumeContent();
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