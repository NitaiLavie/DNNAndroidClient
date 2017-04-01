package com.dnnproject.android.dnnandroidclient.tcpclient;

import dnn.message.DnnMessage;

/**
 * Created by nitai on 01/04/17.
 */

public interface MessageReceiver {
    public void receiveMessage(DnnMessage message);
}
