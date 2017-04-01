package com.dnnproject.android.dnnandroidclient;

import com.dnnproject.android.dnnandroidclient.tcpclient.TcpClient;

import java.io.IOException;

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
        TcpClient tcpClient = new TcpClient(mDnnServerIP, new TcpClient.OnMessageReceived() {
            @Override
            public void messageReceived(String message) {

            }
        });

        try {
            tcpClient.start();
            Thread.sleep(120000);
            tcpClient.stopClient();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
