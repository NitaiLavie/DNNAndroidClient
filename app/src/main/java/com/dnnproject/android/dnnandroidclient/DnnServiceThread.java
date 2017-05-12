package com.dnnproject.android.dnnandroidclient;

import android.util.Log;

import com.dnnproject.android.dnnandroidclient.clientlogic.ClientLogic;
import com.dnnproject.android.dnnandroidclient.tcpclient.TcpClient;

import java.io.IOException;

/**
 * Created by nitai on 01/04/17.
 */

public class DnnServiceThread extends Thread {

    private final String mDnnServerIP;
    private final String mAndroidId;

    public DnnServiceThread(String dnnServerIP, String androidId){
        super();
        mDnnServerIP = dnnServerIP;
        mAndroidId = androidId;
    }

    @Override
    public void run(){
        super.run();

        //TODO: add functionality

        // creating the tcp client
        TcpClient tcpClient = new TcpClient(mDnnServerIP);
        ClientLogic clientLogic = new ClientLogic(this, tcpClient, mAndroidId);

        try {
            tcpClient.start();
            clientLogic.run();

        } catch (IOException e){
            Log.e("DnnServiceThread.java", e.getMessage());
        }

        try {
            tcpClient.stop();
        } catch (IOException e) {
            Log.e("DnnServiceThread.java", "Could not stop tcp client!");
            e.printStackTrace();
        }
    }

}
