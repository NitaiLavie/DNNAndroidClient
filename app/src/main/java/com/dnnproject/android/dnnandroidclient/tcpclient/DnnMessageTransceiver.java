package com.dnnproject.android.dnnandroidclient.tcpclient;

import dnnUtil.dnnMessage.DnnMessage;

/**
 * Created by nitai on 01/04/17.
 */

public interface DnnMessageTransceiver {
    /**
     * these methods are potentially blocking and therfore should be run from within a thread
     * that takes this into account
     */
    public void sendMessage(DnnMessage message) throws InterruptedException;
    public DnnMessage getMessage() throws InterruptedException;
}
