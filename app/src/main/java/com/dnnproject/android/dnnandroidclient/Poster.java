package com.dnnproject.android.dnnandroidclient;

/**
 * Created by nitai on 20/06/17.
 */

public interface Poster {
    void postMessage(String message);
    void postToLog(String message);
    void serverDisconnect();

    //Todo: this doesn't belong here but for now it will do
    boolean isBatteryCharging();
    float getBatteryLevel();
}