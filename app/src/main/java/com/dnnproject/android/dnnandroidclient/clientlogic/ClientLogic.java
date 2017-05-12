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

/**
 * Created by nitai on 09/05/17.
 */

public class ClientLogic {

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
        mRun = true;
        try {
            // sending first hello message to server
            mMessageTransceiver.sendMessage(new DnnHelloMessage(mAndroidId, "Hello"));
            Log.i("DnnServiceThread.java", "sent hello message");

            while(mRun){
                if (mThread.isInterrupted()) {
                    throw new InterruptedException();
                }

                // waiting to get a new DnnModel from server and set up our model
                DnnMessage inMessage = mMessageTransceiver.getMessage();

                DnnMessage.MessageType messageType = inMessage.getMessageType();

                switch(messageType){
                    case MODEL:
                        Log.i("DnnServiceThread.java", "Received a new dnn model");
                        Log.i("DnnServiceThread.java", "Creating the model");
                        mModel = new DnnModel((DnnModelDescriptor)inMessage.getContent());

                        Log.i("DnnServiceThread.java", "Model ready! sending ready message to server");
                        mMessageTransceiver.sendMessage(new DnnReadyMessage(mAndroidId,"Ready"));

                    case TRAININGDATA:
                        Log.i("DnnServiceThread.java", "Received new training data");
                        Log.i("DnnServiceThread.java", "Setting received training data to created DnnModel");
                        mModel.setTrainingData((DnnTrainingData)inMessage.getContent());
                        Log.i("DnnServiceThread.java", "Training the model");
                        mModel.trainModel();

                        Log.i("DnnServiceThread.java", "Finished training! sending new weights to server");
                        mMessageTransceiver.sendMessage(new DnnWeightsMessage(mAndroidId, mModel.getWeightsData()));

                    case WEIGHTS:
                        Log.i("DnnServiceThread.java", "Received new model weights");
                        Log.i("DnnServiceThread.java", "Setting new weights to new model");
                        mModel.setWeightsData((DnnWeightsData)inMessage.getContent());

                        Log.i("DnnServiceThread.java", "Model ready! sending ready message to server");
                        mMessageTransceiver.sendMessage(new DnnReadyMessage(mAndroidId,"Ready"));

                    default:
                }
            }
        } catch (InterruptedException e) {
            Log.e("DnnServiceThread.java", "Thread interupted!");
            e.printStackTrace();
        }
    }
}
