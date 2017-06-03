package com.dnnproject.android.dnnandroidclient.clientlogic;

import android.util.Log;

import com.dnnproject.android.dnnandroidclient.downloader.DnnDataDownloader;
import com.dnnproject.android.dnnandroidclient.tcpclient.DnnMessageTransceiver;

import dnnUtil.dnnMessage.DnnDeltaMessage;
import dnnUtil.dnnMessage.DnnHelloMessage;
import dnnUtil.dnnMessage.DnnMessage;
import dnnUtil.dnnMessage.DnnReadyMessage;
import dnnUtil.dnnMessage.DnnStatisticsMessage;
import dnnUtil.dnnMessage.DnnValidationMessage;
import dnnUtil.dnnMessage.DnnValidationResultMessage;
import dnnUtil.dnnMessage.DnnWeightsMessage;
import dnnUtil.dnnModel.DnnBundle;
import dnnUtil.dnnModel.DnnIndex;
import dnnUtil.dnnModel.DnnModel;
import dnnUtil.dnnModel.DnnModelDescriptor;
import dnnUtil.dnnModel.DnnModelParameters;
import dnnUtil.dnnModel.DnnTrainingData;
import dnnUtil.dnnModel.DnnWeightsData;
import dnnUtil.dnnStatistics.DnnStatistics;
import dnnUtil.dnnStatistics.DnnValidationResult;
import dnnUtil.dnnTimer.DnnTimer;

/**
 * Created by nitai on 09/05/17.
 */

public class ClientLogic {
    private static final String TAG = "ClientLogic";

    private final DnnMessageTransceiver mMessageTransceiver;
    private final DnnDataDownloader mDataDownloader;
    private final Thread mThread;
    private final String mAndroidId;
    private boolean mRun;
    DnnModel mModel;
    DnnStatistics mStats;

    public ClientLogic(Thread thread, DnnMessageTransceiver messageTransceiver, DnnDataDownloader dataDownloader, String androidId){
        mThread = thread;
        mMessageTransceiver = messageTransceiver;
        mDataDownloader = dataDownloader;
        mAndroidId = androidId;
        mRun = false;
        mStats = new DnnStatistics();
        mStats.setClientName(mAndroidId);
        mStats.setDeviceName(android.os.Build.MODEL);
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
                    case MODEL: {
                        Log.i(TAG, "Received a new dnn model");
                        Log.i(TAG, "Creating the model");
                        timer.start();
                        DnnModelDescriptor receivedDescriptor = (DnnModelDescriptor) inMessage.getContent();
                        mModel = new DnnModel(receivedDescriptor);
                        timer.stop();
                        mStats.setModelNumber(mModel.getModelVersion());
                        Log.i(TAG, "Model ready! (" + timer + ")");
                        Log.i(TAG, "Model number: " + mModel.getModelVersion());
                        timer.start();
                        mMessageTransceiver.sendMessage(new DnnReadyMessage(mAndroidId, mModel.getModelVersion()));
                        timer.stop();
                        Log.i(TAG, "sent ready message (" + timer + ")");
                        break;
                    }
                    case TRAIN: {
                        Log.i(TAG, "Received new train message");
                        DnnBundle bundle = (DnnBundle) inMessage.getContent();
                        DnnModelDescriptor modelDescriptor = bundle.getModelDescriptor();
                        Log.i(TAG, "Creating the model");
                        timer.start();
                        mModel = new DnnModel(modelDescriptor);
                        timer.stop();
                        mStats.setModelNumber(mModel.getModelVersion());
                        Log.i(TAG, "Model ready! (" + timer + ")");
                        Log.i(TAG, "Model number: " + mModel.getModelVersion());

                        Log.i(TAG, "Downloading the relevant trainig data from github");
                        DnnIndex index = bundle.getIndexData();
                        String[] paths;
                        timer.start();
                        try {
                            paths = mDataDownloader.download(index.getDataSet(), index.getDataType(), index.getDataSize(), index.getDataIndex());
                        } catch (Exception e) {
                            Log.e(TAG, "run: faild to download training data: " + e.getMessage());
                            e.printStackTrace();
                            //Todo: maybe send an error message
                            break;
                        }
                        timer.stop();
                        Log.i(TAG, "finishd downloading training data from github (" + timer + ")");
                        Log.i(TAG, "loading downloaded training data to created DnnModel");
                        mStats.setStartTrainingTime(System.currentTimeMillis());
                        mStats.setNumberOfTrainedEpochs(1);
                        timer.start();
                        mModel.loadTrainingData(paths[0], paths[1], index.getDataSet());
                        timer.stop();
                        Log.i(TAG, "Training data is loaded (" + timer + ")");
                        Log.i(TAG, "Training the model");
                        timer.start();
                        mModel.trainModel();
                        timer.stop();
                        mStats.setFinishTrainingTime(System.currentTimeMillis());
                        Log.i(TAG, "Finished training! (" + timer + ")");
                        timer.start();
                        mMessageTransceiver.sendMessage(new DnnDeltaMessage(mAndroidId, mModel.getDeltaData()));
                        timer.stop();
                        Log.i(TAG, "sent new delta to the server (" + timer + ")");
                        timer.start();
                        mMessageTransceiver.sendMessage(new DnnStatisticsMessage(mAndroidId, mStats));
                        timer.stop();
                        Log.i(TAG, "send latest statistics to the server (" + timer + ")");
                        break;
                    }
                    case VALIDATE: {
                        Log.i(TAG, "Received new validate message");
                        DnnBundle bundle = (DnnBundle) inMessage.getContent();
                        DnnModelDescriptor modelDescriptor = bundle.getModelDescriptor();
                        Log.i(TAG, "Creating the model");
                        timer.start();
                        mModel = new DnnModel(modelDescriptor);
                        timer.stop();
                        mStats.setModelNumber(mModel.getModelVersion());
                        Log.i(TAG, "Model ready! (" + timer + ")");
                        Log.i(TAG, "Model number: " + mModel.getModelVersion());

                        Log.i(TAG, "Downloading the relevant validation data from github");
                        DnnIndex index = bundle.getIndexData();
                        String[] paths;
                        timer.start();
                        try {
                            paths = mDataDownloader.download(index.getDataSet(), index.getDataType(), index.getDataSize(), index.getDataIndex());
                        } catch (Exception e) {
                            Log.e(TAG, "run: faild to download validation data: " + e.getMessage());
                            e.printStackTrace();
                            //Todo: maybe send an error message
                            break;
                        }
                        timer.stop();
                        Log.i(TAG, "finished downloading validation data from github (" + timer + ")");
                        Log.i(TAG, "loading downloaded validation data to created DnnModel");
                        mStats.setStartTrainingTime(System.currentTimeMillis());
                        mStats.setNumberOfTrainedEpochs(1);
                        timer.start();
                        mModel.loadTrainingData(paths[0], paths[1], index.getDataSet());
                        timer.stop();
                        Log.i(TAG, "validation data is loaded (" + timer + ")");
                        Log.i(TAG, "validating the model");
                        timer.start();
                        DnnValidationResult validationResult = mModel.validateModel();
                        timer.stop();
                        mStats.setFinishTrainingTime(System.currentTimeMillis());
                        Log.i(TAG, "Finished validating! (" + timer + ")");
                        Log.i(TAG, "Model Accuracy = "+validationResult.getAccuracy() + "%");
                        timer.start();
                        mMessageTransceiver.sendMessage(new DnnValidationResultMessage(mAndroidId,validationResult));
                        timer.stop();
                        Log.i(TAG, "sent validation results to the server (" + timer + ")");
                        timer.start();
                        mMessageTransceiver.sendMessage(new DnnStatisticsMessage(mAndroidId, mStats));
                        timer.stop();
                        Log.i(TAG, "send latest statistics to the server (" + timer + ")");
                        break;
                    }
                    case WEIGHTS: {
                        Log.i(TAG, "Received new model weights");
                        Log.i(TAG, "Setting new weights to new model");
                        timer.start();
                        mModel.setWeightsData((DnnWeightsData) inMessage.getContent());
                        timer.stop();
                        Log.i(TAG, "set new weights (" + timer + ")");
                        timer.start();
                        mMessageTransceiver.sendMessage(new DnnReadyMessage(mAndroidId, mModel.getModelVersion()));
                        timer.stop();
                        Log.i(TAG, "sent ready message (" + timer + ")");
                        break;
                    }
                    default:{}
                }
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Thread interupted!");
            e.printStackTrace();
        }
    }
}
