package com.mareksebera.simpledilbert;

import org.joda.time.DateMidnight;

public class FavoritedItem {

	private DateMidnight date;

	private String url;

	public FavoritedItem(DateMidnight d, String u) {
		assert (d != null);
		assert (u != null);
		date = d;
		url = u;
	}

	public DateMidnight getDate() {
		return date;
	}

	public String getUrl() {
		return url;
	}

	public void setDate(DateMidnight date) {
		this.date = date;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
