package com.dnnproject.android.dnnandroidclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.io.File;

/**
 * Created by nitai on 31/03/17.
 */

public class DnnService extends Service {
    private static final String TAG = "DnnService";

    // for foreground service test:
    public static final int ONGOING_NOTIFICATION_ID = 666;
    public static final String IP = "ip";
    public static final String USERNAME = "username";

    private static final String DefaultIP = "79.181.131.28";
    private static final String DefaultUsername = "foobarbaz";

    private final IBinder mBinder = new DNNServiceBinder();
    private DnnServiceCallbacks mServiceCallbacks;
    private DnnServiceThread mMainThread;
    private String mDnnServerIP;
    private String mClientUsername;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            // getting extras from intent
            mDnnServerIP = intent.getStringExtra(IP);
            if(mDnnServerIP.equals("")){
                mDnnServerIP = DefaultIP;
            }

            mClientUsername = intent.getStringExtra(USERNAME);
            if(mClientUsername.equals("")){
                mClientUsername = DefaultUsername;
            }

            // setting up a notification for the forground service:
            Notification.Builder notificationBuilder = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.hamster_cogwheel)
                    .setContentTitle(getText(R.string.notification_title))
                    .setContentText(getText(R.string.notification_message))
                    .setTicker(getText(R.string.ticker_text));

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            Intent notificationIntent = new Intent(this, MainActivity.class);
            stackBuilder.addNextIntent(notificationIntent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            notificationBuilder.setContentIntent(pendingIntent);
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // sending this service to the foreground
            startForeground(ONGOING_NOTIFICATION_ID, notificationBuilder.build());

        } else {
            Log.i(TAG,"Relunched DnnService with 'null' Intent");
        }
        return Service.START_STICKY;
    }

    @Override

    public void onDestroy(){
        // removing this service from the foreground
        stopForeground(true);
        mMainThread.interrupt();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    public class DNNServiceBinder extends Binder {

        boolean isRunning(){
            return false;
        }
        DnnService getService() {
            return DnnService.this;
        }
    }

    public void setCallbacks(DnnServiceCallbacks callbacks) {
        mServiceCallbacks = callbacks;
        if(mMainThread != null) {
            mMainThread.setServiceCallbacks(mServiceCallbacks);
        }
    }
    public void unsetCallbacks(){
        mServiceCallbacks = null;
        if(mMainThread != null) {
            mMainThread.unsetServiceCallbacks();
        }
    }

    public void startMainThread(){
        if(mMainThread == null) {
            // getting a uniqe device_id
            String androidId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            File filesDir = getApplicationContext().getFilesDir();
//            //DEBUG
//            File filesDir = Environment.getExternalStorageDirectory();
//            //DEBUG
            // getting a PartialWakeLock so this service threads will run even when the device is locked
            PowerManager mgr = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DnnWakeLock");
            // creating the main service thread
            mMainThread = new DnnServiceThread((DnnApplication)getApplication(), mServiceCallbacks, mDnnServerIP, wakeLock, androidId + "." + mClientUsername, filesDir);
        }
        if(!mMainThread.isAlive()){
            // Starting main service thread:
            mMainThread.start();
        }
    }
}
