package com.dnnproject.android.dnnandroidclient.tcpclient;

import dnnUtil.dnnMessage.DnnMessage;

/**
 * Created by nitai on 01/04/17.
 */

public interface DnnMessageTransceiver {
    public void sendMessage(DnnMessage message);
    public DnnMessage getMessage();
}
