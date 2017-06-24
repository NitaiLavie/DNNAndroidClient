package com.dnnproject.android.dnnandroidclient;

import android.app.Application;

import java.util.Calendar;

/**
 * Created by nitai on 24/06/17.
 */

public class DnnApplication extends Application {

    private boolean dnnServiceStarted = false;
    private boolean mServiceBound = false;

    private boolean mServerDisconnected = false;

    private String mServiceMessage = "";
    private String mLog = "";

    public boolean isServerDisconnected() {
        return mServerDisconnected;
    }

    public void setServerDisconnected(boolean mServerDisconnected) {
        this.mServerDisconnected = mServerDisconnected;
    }

    public String getServiceMessage() {
        return mServiceMessage;
    }

    public void setServiceMessage(String serviceMessage) {
        this.mServiceMessage = serviceMessage;
    }

    public String getLog() {
        return mLog;
    }

    public void logLog(String log) {
        String logEntry = ">>" + Calendar.getInstance().getTime() + ":" +
                "\n" + log + "\n";
        this.mLog = mLog.concat(logEntry);
    }

    public boolean isDnnServiceStarted() {
        return dnnServiceStarted;
    }

    public boolean isServiceBound() {
        return mServiceBound;
    }

    public void setDnnServiceStarted(boolean dnnServiceStarted) {
        this.dnnServiceStarted = dnnServiceStarted;
    }

    public void setServiceBound(boolean serviceBound) {
        this.mServiceBound = serviceBound;
    }
}
