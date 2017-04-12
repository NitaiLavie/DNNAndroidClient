package com.dnnproject.android.dnnandroidclient.messageswitch;

import com.dnnproject.android.dnnandroidclient.tcpclient.DnnMessageTransceiver;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;

import dnn.dnnMessage.DnnMessage;

/**
 * Created by nitai on 01/04/17.
 */



public class MessageSwitch implements DnnMessageTransceiver, InOutMessageQueue {

    // implementing some shared resource safety:
    private Lock mInputLock;
    private Lock mOutputLock;

    // creating messages queues
    private Queue<DnnMessage> mInputMessageQueue;
    private Queue<DnnMessage> mOutputMessageQueue;

    // a member messageSender
    private MessageSender mMessageSender;

    public MessageSwitch(MessageSender messageSender) {

        mMessageSender = messageSender;
        mInputMessageQueue = new LinkedBlockingDeque<>();
        mOutputMessageQueue = new LinkedBlockingDeque<>();
    }


    public void receiveMessage(DnnMessage message) {
        mInputLock.lock();
        mInputMessageQueue.add(message);
        mInputLock.unlock();

    }

    @Override
    public void pushMessage(DnnMessage message) {
        mOutputLock.lock();
        mOutputMessageQueue.add(message);
        mOutputLock.unlock();

    }

    @Override
    public void pullMessage(DnnMessage message) {
        mInputLock.lock();
        mInputMessageQueue.remove();
        mInputLock.unlock();

    }

    @Override
    public void sendMessage(DnnMessage message) {

    }

    @Override
    public DnnMessage getMessage() {
        return null;
    }
}
