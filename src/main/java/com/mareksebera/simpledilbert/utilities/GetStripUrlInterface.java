package com.mareksebera.simpledilbert.utilities;

public interface GetStripUrlInterface {

    void displayImage(String url, String title);

    void imageLoadFailed(String url, Throwable reason);

}
