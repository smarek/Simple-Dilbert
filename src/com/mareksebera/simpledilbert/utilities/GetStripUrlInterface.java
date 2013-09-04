package com.mareksebera.simpledilbert.utilities;

import com.nostra13.universalimageloader.core.assist.FailReason;

public interface GetStripUrlInterface {

    public void displayImage(String url);

    public void imageLoadFailed(String url, FailReason reason);

}
