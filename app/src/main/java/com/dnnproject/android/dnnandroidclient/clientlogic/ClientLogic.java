package com.dnnproject.android.dnnandroidclient.clientlogic;

import android.util.Log;

import com.dnnproject.android.dnnandroidclient.tcpclient.DnnMessageTransceiver;

import dnnUtil.dnnMessage.DnnHelloMessage;
import dnnUtil.dnnMessage.DnnMessage;
import dnnUtil.dnnMessage.DnnReadyMessage;
import dnnUtil.dnnMessage.DnnWeightsMessage;
import dnnUtil.dnnModel.DnnModel;
import dnnUtil.dnnModel.DnnModelDescriptor;
import dnnUtil.dnnModel.DnnTrainingData;
import dnnUtil.dnnModel.DnnTrainingPackage;
import dnnUtil.dnnModel.DnnWeightsData;
import dnnUtil.dnnTimer.DnnTimer;

/**
 * Created by nitai on 09/05/17.
 */

public class ClientLogic {
    private static final String TAG = "ClientLogic";

    private final DnnMessageTransceiver mMessageTransceiver;
    private final Thread mThread;
    private final String mAndroidId;
    private boolean mRun;
    DnnModel mModel;

    public ClientLogic(Thread thread, DnnMessageTransceiver messageTransceiver, String androidId){
        mThread = thread;
        mMessageTransceiver = messageTransceiver;
        mAndroidId = androidId;
        mRun = false;
    }

    public void run() {
        // this is where all client runnign and messaging logic runs

        DnnTimer timer = new DnnTimer(); //for timing

        mRun = true;
        try {
            // sending first hello message to server
            timer.start();
            mMessageTransceiver.sendMessage(new DnnHelloMessage(mAndroidId, "Hello"));
            timer.stop();
            Log.i(TAG, "sent hello message ("+timer+")");

            while(mRun){
                if (mThread.isInterrupted()) {
                    throw new InterruptedException();
                }

                // waiting to get a new DnnModel from server and set up our model
                DnnMessage inMessage = mMessageTransceiver.getMessage();

                DnnMessage.MessageType messageType = inMessage.getMessageType();

                switch(messageType){
                    case MODEL:
                        Log.i(TAG, "Received a new dnn model");
                        Log.i(TAG, "Creating the model");
                        timer.start();
                        mModel = new DnnModel((DnnModelDescriptor)inMessage.getContent());
                        timer.stop();
                        Log.i(TAG, "Model ready! ("+timer+")");
                        timer.start();
                        mMessageTransceiver.sendMessage(new DnnReadyMessage(mAndroidId,"Ready"));
                        timer.stop();
                        Log.i(TAG, "sent ready message ("+timer+")");
                        break;

                    case TRAININGDATA:
                        Log.i(TAG, "Received new training data");
                        Log.i(TAG, "Setting received training data to created DnnModel");
                        timer.start();
                        mModel.setTrainingData((DnnTrainingData)inMessage.getContent());
                        timer.stop();
                        Log.i(TAG, "Training data is set ("+timer+")");
                        Log.i(TAG, "Training the model");
                        timer.start();
                        mModel.trainModel();
                        timer.stop();
                        Log.i(TAG, "Finished training! ("+timer+")");
                        timer.start();
                        mMessageTransceiver.sendMessage(new DnnWeightsMessage(mAndroidId, mModel.getWeightsData()));
                        timer.stop();
                        Log.i(TAG, "sent new weights to the server ("+timer+")");

                        break;

                    case WEIGHTS:
                        Log.i(TAG, "Received new model weights");
                        Log.i(TAG, "Setting new weights to new model");
                        timer.start();
                        mModel.setWeightsData((DnnWeightsData)inMessage.getContent());
                        timer.stop();
                        mMessageTransceiver.sendMessage(new DnnReadyMessage(mAndroidId,"Ready"));
                        Log.i(TAG, "sent ready message ("+timer+")");
                        break;

                    default:
                }
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Thread interupted!");
            e.printStackTrace();
        }
    }
}
