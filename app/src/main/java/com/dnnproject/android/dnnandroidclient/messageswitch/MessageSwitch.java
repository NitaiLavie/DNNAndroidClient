package com.dnnproject.android.dnnandroidclient.messageswitch;

import com.dnnproject.android.dnnandroidclient.tcpclient.MessageReceiver;

import java.util.ArrayList;
import java.util.List;

import dnn.message.DnnMessage;

/**
 * Created by nitai on 01/04/17.
 */



public class MessageSwitch implements MessageReceiver, InOutMessageQueue {


    // creating messages queues
    private List<DnnMessage> mInputMessageQueue;
    private List<DnnMessage> mOutputMessageQueue;

    public MessageSwitch() {

        mInputMessageQueue = new ArrayList<>();
        mOutputMessageQueue = new ArrayList<>();
    }

    @Override
    public void receiveMessage(DnnMessage message) {

    }

    @Override
    public void pushMessage(DnnMessage message) {

    }

    @Override
    public void pullMessage(DnnMessage message) {

    }
}
