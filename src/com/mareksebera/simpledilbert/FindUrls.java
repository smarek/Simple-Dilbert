package com.mareksebera.simpledilbert;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;

import android.util.Log;

public final class FindUrls {
	private FindUrls() {
	}

	private static final Pattern url_match_pattern = Pattern
			.compile("/dyn/str_strip(.*).gif");

	public static List<String> extractUrls(HttpResponse response) {
		List<String> found = new ArrayList<String>();
		try {
			Scanner scan = new Scanner(response.getEntity().getContent());
			String match = null;
			while ((match = scan.findWithinHorizon(url_match_pattern, 0)) != null) {
				found.add(match.replace("/dyn/str_strip",
						"http://www.dilbert.com/dyn/str_strip"));
			}
		} catch (Throwable t) {
			Log.e("FindUrls", "Error Occured", t);
		}
		return found;
	}
}