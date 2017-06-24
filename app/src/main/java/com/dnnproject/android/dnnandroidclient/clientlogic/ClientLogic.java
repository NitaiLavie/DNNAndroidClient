package com.dnnproject.android.dnnandroidclient.clientlogic;

import android.util.Log;

import com.dnnproject.android.dnnandroidclient.Poster;
import com.dnnproject.android.dnnandroidclient.downloader.DnnDataDownloader;
import com.dnnproject.android.dnnandroidclient.tcpclient.DnnMessageTransceiver;

import dnnUtil.dnnMessage.DnnCloseMessage;
import dnnUtil.dnnMessage.DnnDeltaMessage;
import dnnUtil.dnnMessage.DnnHelloMessage;
import dnnUtil.dnnMessage.DnnMessage;
import dnnUtil.dnnMessage.DnnReadyMessage;
import dnnUtil.dnnMessage.DnnStatisticsMessage;
import dnnUtil.dnnMessage.DnnValidationResultMessage;
import dnnUtil.dnnModel.DnnBundle;
import dnnUtil.dnnModel.DnnIndex;
import dnnUtil.dnnModel.DnnModel;
import dnnUtil.dnnModel.DnnModelDescriptor;
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
    private final Poster mPoster;
    private boolean mRun;
    private DnnModel mModel;
    private DnnStatistics mStats;

    public ClientLogic(Thread thread, DnnMessageTransceiver messageTransceiver, DnnDataDownloader dataDownloader, String androidId, Poster poster){
        mThread = thread;
        mMessageTransceiver = messageTransceiver;
        mDataDownloader = dataDownloader;
        mAndroidId = androidId;
        mPoster = poster;
        mRun = false;
        mStats = new DnnStatistics();
        mStats.setClientName(mAndroidId);
        mStats.setDeviceName(android.os.Build.MODEL);
    }

    public void run() throws InterruptedException {
        // this is where all client runnign and messaging logic runs

        DnnTimer timer = new DnnTimer(); //for timing
        mRun = true;

        // sending first hello message to server
        timer.start();
        mMessageTransceiver.sendMessage(new DnnHelloMessage(mAndroidId, "Hello"));
        timer.stop();
        postToLog("sent hello message ("+timer+")");
        try {
            while (mRun) {
                if (mThread.isInterrupted()) {
                    throw new InterruptedException("Thread interrupted!");
                }

                // waiting to get a new DnnModel from server and set up our model
                postMessage("Dnn client is Idle");
                DnnMessage inMessage = mMessageTransceiver.getMessage();

                DnnMessage.MessageType messageType = inMessage.getMessageType();

                switch (messageType) {
                    case MODEL: {
                        postToLog("Received a new dnn model");
                        postToLog("Creating the model");
                        timer.start();
                        DnnModelDescriptor receivedDescriptor = (DnnModelDescriptor) inMessage.getContent();
                        mModel = new DnnModel(receivedDescriptor);
                        timer.stop();
                        mStats.setModelNumber(mModel.getModelVersion());
                        postToLog("Model ready! (" + timer + ")");
                        postToLog("Model number: " + mModel.getModelVersion());
                        timer.start();
                        mMessageTransceiver.sendMessage(new DnnReadyMessage(mAndroidId, mModel.getModelVersion()));
                        timer.stop();
                        postToLog("sent ready message (" + timer + ")");
                        break;
                    }
                    case TRAIN: {
                        postToLog("Received new train message");
                        DnnBundle bundle = (DnnBundle) inMessage.getContent();
                        DnnModelDescriptor modelDescriptor = bundle.getModelDescriptor();
                        postToLog("Creating the model");
                        timer.start();
                        mModel = new DnnModel(modelDescriptor);
                        timer.stop();
                        mStats.setModelNumber(mModel.getModelVersion());
                        postToLog("Model ready! (" + timer + ")");
                        postToLog("Model number: " + mModel.getModelVersion());

                        postToLog("Downloading the relevant trainig data from github");
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
                        postToLog("finishd downloading training data from github (" + timer + ")");
                        postToLog("loading downloaded training data to created DnnModel");
                        postMessage("Training model no. " + mModel.getModelVersion());
                        mStats.setStartTrainingTime(System.currentTimeMillis());
                        mStats.setNumberOfTrainedEpochs(1);
                        timer.start();
                        mModel.loadTrainingData(paths[0], paths[1], index.getDataSet());
                        timer.stop();
                        postToLog("Training data is loaded (" + timer + ")");
                        postToLog("Training the model");
                        timer.start();
                        mModel.trainModel();
                        timer.stop();
                        mStats.setFinishTrainingTime(System.currentTimeMillis());
                        postToLog("Finished training! (" + timer + ")");
                        timer.start();
                        mMessageTransceiver.sendMessage(new DnnDeltaMessage(mAndroidId, mModel.getDeltaData()));
                        timer.stop();
                        postToLog("sent new delta to the server (" + timer + ")");
                        timer.start();
                        mMessageTransceiver.sendMessage(new DnnStatisticsMessage(mAndroidId, mStats));
                        timer.stop();
                        postToLog("send latest statistics to the server (" + timer + ")");
                        break;
                    }
                    case VALIDATE: {
                        postToLog("Received new validate message");
                        DnnBundle bundle = (DnnBundle) inMessage.getContent();
                        DnnModelDescriptor modelDescriptor = bundle.getModelDescriptor();
                        postToLog("Creating the model");
                        timer.start();
                        mModel = new DnnModel(modelDescriptor);
                        timer.stop();
                        mStats.setModelNumber(mModel.getModelVersion());
                        postToLog("Model ready! (" + timer + ")");
                        postToLog("Model number: " + mModel.getModelVersion());

                        postToLog("Downloading the relevant validation data from github");
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
                        postToLog("finished downloading validation data from github (" + timer + ")");
                        postToLog("loading downloaded validation data to created DnnModel");
                        postMessage("Validating model no. " + mModel.getModelVersion());
                        mStats.setStartTrainingTime(System.currentTimeMillis());
                        mStats.setNumberOfTrainedEpochs(1);
                        timer.start();
                        mModel.loadTrainingData(paths[0], paths[1], index.getDataSet());
                        timer.stop();
                        postToLog("validation data is loaded (" + timer + ")");
                        postToLog("validating the model");
                        timer.start();
                        DnnValidationResult validationResult = mModel.validateModel();
                        timer.stop();
                        mStats.setFinishTrainingTime(System.currentTimeMillis());
                        postToLog("Finished validating! (" + timer + ")");
                        postToLog("Model Accuracy = " + validationResult.getAccuracy() + "%");
                        timer.start();
                        mMessageTransceiver.sendMessage(new DnnValidationResultMessage(mAndroidId, validationResult));
                        timer.stop();
                        postToLog("sent validation results to the server (" + timer + ")");
                        timer.start();
                        mMessageTransceiver.sendMessage(new DnnStatisticsMessage(mAndroidId, mStats));
                        timer.stop();
                        postToLog("send latest statistics to the server (" + timer + ")");
                        break;
                    }
                    case WEIGHTS: {
                        postToLog("Received new model weights");
                        postToLog("Setting new weights to new model");
                        timer.start();
                        mModel.setWeightsData((DnnWeightsData) inMessage.getContent());
                        timer.stop();
                        postToLog("set new weights (" + timer + ")");
                        timer.start();
                        mMessageTransceiver.sendMessage(new DnnReadyMessage(mAndroidId, mModel.getModelVersion()));
                        timer.stop();
                        postToLog("sent ready message (" + timer + ")");
                        break;
                    }
                    case CLOSE: {
                        postToLog("Received close message");
                        postMessage("Server Closed Connection,\n" +
                                "Thank You for you participation!");
                        throw new InterruptedException("Server closed connection");
                    }
                    default: {
                    }
                }
            }
        } catch (InterruptedException e) {
            timer.start();
            mMessageTransceiver.sendMessage(new DnnCloseMessage(mAndroidId, "close"));
            timer.stop();
            postToLog("sent close message (" + timer + ")");
            throw e;
        }
    }

    private void postMessage(String message){
        mPoster.postMessage(message);
    }
    private void postToLog(String message){
        Log.i(TAG, message);
        mPoster.postToLog(message);
    }
}
