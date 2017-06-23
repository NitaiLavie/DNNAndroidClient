package com.dnnproject.android.dnnandroidclient;

/**
 * Created by nitai on 17/06/17.
 */

public interface DnnServiceCallbacks {

    void printMessage(final String message);
    void printToLog(final String message);

    void serverDisconnect();
}
