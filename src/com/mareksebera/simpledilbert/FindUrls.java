package com.mareksebera.simpledilbert;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import android.util.Log;

public final class FindUrls {
	private FindUrls() {
	}

	private static final Pattern url_match_pattern = Pattern
			.compile("/dyn/str_strip(.*).gif");
	
	public static List<String> extractUrls(String input) {
		List<String> result = new ArrayList<String>();

		Pattern pattern = Pattern
				.compile("\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)|www.)"
						+ "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov"
						+ "|mil|biz|info|mobi|name|aero|jobs|museum"
						+ "|travel|[a-z]{2}))(:[\\d]{1,5})?"
						+ "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?"
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
			Scanner scan = null;
			Header contentEncoding = response
					.getFirstHeader("Content-Encoding");
			if (contentEncoding != null
					&& contentEncoding.getValue().equalsIgnoreCase("gzip")) {
				scan = new Scanner(new GZIPInputStream(response.getEntity()
						.getContent()));
			} else {
				scan = new Scanner(response.getEntity().getContent());
			}

			String match = null;
			while ((match = scan.findWithinHorizon(url_match_pattern, 0)) != null) {
				found.add(match.replace("/dyn/str_strip",
						"http://www.dilbert.com/dyn/str_strip"));
			}
			scan.close();
			response.getEntity().consumeContent();
		} catch (Throwable t) {
			Log.e("FindUrls", "Error Occured", t);
		}
		return found;
	}
}