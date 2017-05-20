package com.dnnproject.android.dnnandroidclient;

import android.os.PowerManager;
import android.util.Log;

import com.dnnproject.android.dnnandroidclient.clientlogic.ClientLogic;
import com.dnnproject.android.dnnandroidclient.tcpclient.TcpClient;

import java.io.IOException;

import dnnUtil.dnnModel.DnnModel;
import dnnUtil.dnnModel.DnnModelParameters;
import dnnUtil.dnnModel.DnnTrainingData;
import dnnUtil.dnnModel.DnnTrainingDescriptor;

/**
 * Created by nitai on 01/04/17.
 */

public class DnnServiceThread extends Thread {
    private static final String TAG = "DnnServiceThread";

    private final String mDnnServerIP;
    private final String mAndroidId;
    private final PowerManager.WakeLock mWakeLock;

    public DnnServiceThread(String dnnServerIP, PowerManager.WakeLock wakeLock, String androidId){
        super();
        mDnnServerIP = dnnServerIP;
        mAndroidId = androidId;
        mWakeLock = wakeLock;
    }

    @Override
    public void run(){
        super.run();

        // locking the partial wake lock
        mWakeLock.acquire();

        // creating the tcp client
        TcpClient tcpClient = new TcpClient(mDnnServerIP);
        ClientLogic clientLogic = new ClientLogic(this, tcpClient, mAndroidId);

        try {
            tcpClient.start();
            clientLogic.run();

        } catch (IOException e){
            if(e.getMessage() != null){
                Log.e(TAG, e.getMessage());
            } else {
                Log.e(TAG, e.getClass().getSimpleName() + " Occured!" );
            }

        }

        try {
            tcpClient.stop();
        } catch (IOException e) {
            Log.e(TAG, "Could not stop tcp client!");
            e.printStackTrace();
        }

        // unlocking the partial wake lock
        mWakeLock.release();
    }

}
