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

public class DNNService extends Service {

    private final IBinder mBinder = new DNNServiceBinder();

    // for foreground service test:
    public static final int ONGOING_NOTIFICATION_ID = 666;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // setting up a notification for the forground service:

        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.mipmap.ic_launcher_round)
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
        startForeground(ONGOING_NOTIFICATION_ID, notificationBuilder.build());

        ////////

        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    public class DNNServiceBinder extends Binder {

        boolean isRunning(){
            return false;
        }
        DNNService getService() {
            return DNNService.this;
        }
    }
}
