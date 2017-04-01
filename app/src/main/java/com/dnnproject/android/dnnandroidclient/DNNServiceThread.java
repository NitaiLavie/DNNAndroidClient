package com.dnnproject.android.dnnandroidclient;

import android.util.Log;

/**
 * Created by nitai on 01/04/17.
 */

public class DNNServiceThread extends Thread {

    @Override
    public void run(){
        super.run();

        //TODO: add functionality
        try {
            sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
