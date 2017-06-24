package com.dnnproject.android.dnnandroidclient;

import android.os.PowerManager;
import android.util.Log;

import com.dnnproject.android.dnnandroidclient.clientlogic.ClientLogic;
import com.dnnproject.android.dnnandroidclient.downloader.DnnDataDownloader;
import com.dnnproject.android.dnnandroidclient.tcpclient.TcpClient;

import java.io.File;
import java.io.IOException;

/**
 * Created by nitai on 01/04/17.
 */

public class DnnServiceThread extends Thread implements Poster {
    private static final String TAG = "DnnServiceThread";

    private final String mDnnServerIP;
    private final String mAndroidId;
    private final PowerManager.WakeLock mWakeLock;
    private final File mFilesDir;
    private TcpClient mTcpClient;
    private ClientLogic mClientLogic;
    private DnnServiceCallbacks mServiceCallbacks;
    private final DnnApplication mApp;

    public DnnServiceThread(DnnApplication app, DnnServiceCallbacks serviceCallbacks, String dnnServerIP, PowerManager.WakeLock wakeLock, String androidId, File filesDir){
        super();
        mDnnServerIP = dnnServerIP;
        mAndroidId = androidId;
        mWakeLock = wakeLock;
        mFilesDir = filesDir;
        mServiceCallbacks = serviceCallbacks;
        mApp = app;
    }

    @Override
    public void run(){
        super.run();
        // locking the partial wake lock
        mWakeLock.acquire();
        //DEBUG
//        DnnDataDownloader d = new DnnDataDownloader(mFilesDir);
//        DnnModel model = new DnnModel(new DnnModelParameters());
//        String[] paths;
//        for(int i = 1; i<10; i++){
//            try{
//                paths = d.download("mnist", "train", 1000, i);
//                model.loadTrainingData(paths[0], paths[1], "mnist");
//                Log.d(TAG, "run: training batch number "+i);
//                model.trainModel();
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//        }
//        Float accuracy = new Float(0);
//        try{
//            paths = d.download("mnist", "validate", 10000, 1);
//            model.loadTrainingData(paths[0], paths[1], "mnist");
//            Log.d(TAG, "run: validating the model!");
//            accuracy = model.validateModel().getAccuracy();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        Log.d(TAG, "run: accuracy after epoch = "+accuracy);
//        mWakeLock.release();
//        if(true) return;
        //DEBUG
        // creating the tcp client
        try {
            mTcpClient = new TcpClient(mDnnServerIP);
            DnnDataDownloader dataDownloader = new DnnDataDownloader(mFilesDir);
            mClientLogic = new ClientLogic(this, mTcpClient, dataDownloader, mAndroidId, this);

            try {
                postMessage("Connecting to Dnn server...");
                mTcpClient.start();
                if(this.isInterrupted()) throw new InterruptedException("Thread interrupted!");
                postMessage("Connected Successfully!\n" +
                        "Dnn client is Running!");
                mClientLogic.run();

            } catch (IOException e) {
                if (e.getMessage() != null) {
                    Log.e(TAG, e.getMessage());
                } else {
                    Log.e(TAG, e.getClass().getSimpleName() + " Occured!");
                }
                if(this.isInterrupted()) throw new InterruptedException("Thread interrupted!");

                postMessage("Sorry, failed to connect to Dnn server! " +
                        "Please check address and try again...");
                serverDisconnect();
            }

            try {
                mTcpClient.stop();
            } catch (IOException e) {
                Log.e(TAG, "Could not stop tcp client!");
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            if (e.getMessage() != null) {
                Log.e(TAG, e.getMessage());
            } else {
                Log.e(TAG, e.getClass().getSimpleName() + " Occured!");
            }
            try {
                mTcpClient.stop();
            } catch (IOException ioe) {
                Log.e(TAG, "Could not stop tcp client!");
                ioe.printStackTrace();
            }
            serverDisconnect();

        } finally {
            // unlocking the partial wake lock
            mWakeLock.release();
        }
    }

    void setServiceCallbacks(DnnServiceCallbacks serviceCallbacks){
        mServiceCallbacks = serviceCallbacks;
    }
    void unsetServiceCallbacks(){
        mServiceCallbacks = null;
    }

    @Override
    public void postMessage(String message) {
        mApp.setServiceMessage(message);
        if(mServiceCallbacks != null) mServiceCallbacks.printMessage(message);
    }

    @Override
    public void postToLog(String message) {
        mApp.logLog(message);
        if(mServiceCallbacks != null) mServiceCallbacks.printToLog(mApp.getLog());

    }

    @Override
    public void serverDisconnect() {
        if(mServiceCallbacks != null) mServiceCallbacks.serverDisconnect();
        else mApp.setServerDisconnected(true);
    }
}
