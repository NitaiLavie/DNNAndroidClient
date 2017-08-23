package com.dnnproject.android.dnnandroidclient;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

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
    private int mLogSize = 0;
    private final int mLogLimit = 50;

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
        if(mLogSize < mLogLimit) {
            this.mLog = mLog.concat(logEntry);
            mLogSize++;
        } else {
            this.mLog = (this.mLog.split("\n", 3))[2].concat(logEntry);
        }
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


    public boolean isBatteyCharging(){
        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        return isCharging;
    }
    public float getBatteryLevel() {
        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f;
    }
}
