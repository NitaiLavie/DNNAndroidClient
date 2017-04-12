package com.dnnproject.android.dnnandroidclient;

import com.dnnproject.android.dnnandroidclient.tcpclient.TcpClient;

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
        for(int i = 0; i<20; i++) {
            tcpClient.sendMessage(new DnnTestMessage("Rickster Rick", "And Thaaaaat's - the way the news gos!"));
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
