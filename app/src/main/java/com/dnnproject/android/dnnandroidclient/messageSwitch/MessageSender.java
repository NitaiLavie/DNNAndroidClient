package com.dnnproject.android.dnnandroidclient.messageSwitch;

import java.io.IOException;

import dnnUtil.dnnMessage.DnnMessage;

/**
 * Created by nitai on 01/04/17.
 */

public interface MessageSender {
    public void sendMessage(DnnMessage message) throws IOException;
}
