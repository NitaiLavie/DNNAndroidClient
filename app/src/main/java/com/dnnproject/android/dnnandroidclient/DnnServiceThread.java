package com.dnnproject.android.dnnandroidclient;

import android.util.Log;

import com.dnnproject.android.dnnandroidclient.tcpclient.DnnMessageTransceiver;
import com.dnnproject.android.dnnandroidclient.tcpclient.TcpClient;

import java.io.IOException;

import dnnUtil.dnnMessage.*;
import dnnUtil.dnnModel.*;

/**
 * Created by nitai on 01/04/17.
 */

public class DnnServiceThread extends Thread {

    private final String mDnnServerIP;

    public DnnServiceThread(String dnnServerIP){
        super();
        mDnnServerIP = dnnServerIP;
    }

    @Override
    public void run(){
        super.run();

        //TODO: add functionality
//        DnnModel myDnnModel = new DnnModel(new DnnModelParameters());
//        Log.i("DnnServiceThread.java", "Created my own DnnModel!!!");


        // creating the tcp client
        TcpClient tcpClient = new TcpClient(mDnnServerIP);
        try {
            tcpClient.start();

            try {
                if (this.isInterrupted()) {
                    throw new InterruptedException();
                }
                tcpClient.sendMessage(new DnnHelloMessage("Rickster Rick", "And that's the waaaaaay - the news gos!"));
                Log.i("DnnServiceThread.java", "sent hello message");

                DnnMessage inMessage = tcpClient.getMessage();
                Log.i("DnnServiceThread.java", "Received a DnnMessage");
                if(inMessage instanceof DnnTrainingPackageMessage){
                    Log.i("DnnServiceThread.java", "Received a DnnTrainingPackageMessage (!!!)");
                    DnnTrainingPackage receivedTrainingPackage = (DnnTrainingPackage) inMessage.getContent();
                    Log.i("DnnServiceThread.java", "Successfully extracted DnnTrainingPackage from DnnTrainingPackageMessage(!!!!)");
                    DnnTrainingData receivedTrainingData = receivedTrainingPackage.getTrainingData();
                    Log.i("DnnServiceThread.java", "Successfully extracted DnnTrainingData from Training Package(!!!!!)");
                    DnnModelDescriptor receivedModelDescriptor = receivedTrainingPackage.getModelDescriptor();
                    Log.i("DnnServiceThread.java", "Successfully extracted DnnModelDescriptor from Training Package(!!!!!!)");
                    DnnModel model = new DnnModel(receivedModelDescriptor);
                    Log.i("DnnServiceThread.java", "created a new DnnModel instance(!!!!!!!)");
                    model.setTrainingData(receivedTrainingData);
                    Log.i("DnnServiceThread.java", "set received training data to created DnnModel(!!!!!!!!)");
                    Log.i("DnnServiceThread.java", "training the created DnnModel with the given training data");
                    model.trainModel();
                    Log.i("DnnServiceThread.java", "the DnnModel finished training successfully!!! :-) :-) :-) :-)");
                    DnnWeightsData newWeights = model.getWeightsData();
                    Log.i("DnnServiceThread.java", "extracted weights Data");

                    tcpClient.sendMessage(new DnnWeightsMessage("Rickster Rick", newWeights));
                    Log.i("DnnServiceThread.java", "new weights sent to server!");





                } else {
                    Log.i("DnnServiceThread.java", "Received DnnMessage is not a DnnTrainingPackageMessage :-(");
                }

            } catch (InterruptedException e) {
                Log.e("DnnServiceThread.java", "Thread interupted!");
                e.printStackTrace();
            }

        } catch (IOException e){
            Log.e("DnnServiceThread.java", e.getMessage());
        }

        try {
            tcpClient.stop();
        } catch (IOException e) {
            Log.e("DnnServiceThread.java", "Could not stop tcp client!");
            e.printStackTrace();
        }
    }

}
