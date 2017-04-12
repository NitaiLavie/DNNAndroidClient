package com.dnnproject.android.dnnandroidclient;

import android.util.Log;

import com.dnnproject.android.dnnandroidclient.tcpclient.TcpClient;

import java.io.IOException;

import dnnUtil.dnnMessage.DnnTestMessage;

/**
 * Created by nitai on 01/04/17.
 */

public class DnnServiceThread extends Thread {

    private final String mDnnServerIP;

    public DnnServiceThread(String dnnServerIP){
        super();
        mDnnServerIP = dnnServerIP;
    }

    @Override
    public void run(){
        super.run();

        //TODO: add functionality

        // creating the tcp client
        TcpClient tcpClient = new TcpClient(mDnnServerIP);
        try {
            tcpClient.start();
            for (int i = 0; i < 20; i++) {
                try {
                    if (this.isInterrupted()) {
                        throw new InterruptedException();
                    }
                    tcpClient.sendMessage(new DnnTestMessage("Rickster Rick", "And that's the waaaaaay - the news gos!"));
                    sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("DnnServiceThread.java", "Thread interupted!");
                    e.printStackTrace();
                    break;
                }
            }
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
