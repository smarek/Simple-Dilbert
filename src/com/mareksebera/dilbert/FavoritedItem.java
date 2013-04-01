package com.mareksebera.dilbert;

import org.joda.time.DateMidnight;

public class FavoritedItem {

	public FavoritedItem(DateMidnight d, String u) {
		assert(d != null);
		assert(u != null);
		this.date = d;
		this.url = u;
	}

	public DateMidnight date;
	public String url;

}
