package com.loopj.android.http;

import org.apache.http.HttpResponse;

public interface AsyncHttpResponseHandlerInterface {

    public void sendResponseMessage(HttpResponse response);

}
