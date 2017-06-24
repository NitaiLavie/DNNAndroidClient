package com.dnnproject.android.dnnandroidclient;

/**
 * Created by nitai on 20/06/17.
 */

public interface Poster {
    void postMessage(String message);
    void postToLog(String message);
    void serverDisconnect();
}