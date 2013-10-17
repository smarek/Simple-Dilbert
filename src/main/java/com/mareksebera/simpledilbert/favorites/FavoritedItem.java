package com.mareksebera.simpledilbert.favorites;

import org.joda.time.LocalDate;

public final class FavoritedItem {

    private LocalDate date;

    public FavoritedItem(LocalDate d) {
        assert (d != null);
        date = d;
    }

    public LocalDate getDate() {
        return date;
    }

}
