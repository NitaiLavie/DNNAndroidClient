package com.dnnproject.android.dnnandroidclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.TaskStackBuilder;

/**
 * Created by nitai on 31/03/17.
 */

public class DnnService extends Service {

    // for foreground service test:
    public static final int ONGOING_NOTIFICATION_ID = 666;
    public static final String IP = "ip";

    private final IBinder mBinder = new DNNServiceBinder();
    private DnnServiceThread mMainThread;
    private String mDnnServerIP;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // getting extras from intent
        mDnnServerIP = intent.getStringExtra(IP);

        // creating the main service thread
        mMainThread = new DnnServiceThread(mDnnServerIP);

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

        ////////

        // Starting main service thread:
        mMainThread.start();

        ////////

        return Service.START_STICKY;
    }

    @Override

    public void onDestroy(){
        // removing this service from the foreground
        stopForeground(true);
        mMainThread.interrupt();

        //mMainThread.stop(); /* deprecated!!*/

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
}
