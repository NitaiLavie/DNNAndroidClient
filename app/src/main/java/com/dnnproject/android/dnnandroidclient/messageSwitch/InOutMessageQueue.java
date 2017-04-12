package com.dnnproject.android.dnnandroidclient.messageSwitch;

import dnnUtil.dnnMessage.DnnMessage;

/**
 * Created by nitai on 01/04/17.
 */

public interface InOutMessageQueue {
    public void pushMessage(DnnMessage message);
    public void pullMessage(DnnMessage message);
}
