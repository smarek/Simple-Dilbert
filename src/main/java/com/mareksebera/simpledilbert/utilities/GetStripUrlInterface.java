package com.mareksebera.simpledilbert.utilities;

import com.nostra13.universalimageloader.core.assist.FailReason;

public interface GetStripUrlInterface {

    void displayImage(String url);

    void imageLoadFailed(String url, FailReason reason);

}
