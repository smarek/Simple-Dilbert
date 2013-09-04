package com.mareksebera.simpledilbert.favorites;

import org.joda.time.LocalDate;

public final class FavoritedItem {

	private LocalDate date;

	private String url;

	public FavoritedItem(LocalDate d, String u) {
		assert (d != null);
		assert (u != null);
		date = d;
		url = u;
	}

	public LocalDate getDate() {
		return date;
	}

	public String getUrl() {
		return url;
	}

    public void setUrl(String url) {
		this.url = url;
	}

}
