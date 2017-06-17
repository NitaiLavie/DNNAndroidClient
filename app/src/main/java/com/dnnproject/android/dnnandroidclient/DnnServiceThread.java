package com.dnnproject.android.dnnandroidclient;

import android.os.PowerManager;
import android.util.Log;

import com.dnnproject.android.dnnandroidclient.clientlogic.ClientLogic;
import com.dnnproject.android.dnnandroidclient.downloader.DnnDataDownloader;
import com.dnnproject.android.dnnandroidclient.tcpclient.TcpClient;

import java.io.File;
import java.io.IOException;

import dnnUtil.dnnModel.DnnModel;
import dnnUtil.dnnModel.DnnModelDescriptor;
import dnnUtil.dnnModel.DnnModelParameters;

/**
 * Created by nitai on 01/04/17.
 */

public class DnnServiceThread extends Thread {
    private static final String TAG = "DnnServiceThread";

    private final String mDnnServerIP;
    private final String mAndroidId;
    private final PowerManager.WakeLock mWakeLock;
    private final File mFilesDir;
    private final DnnServiceCallbacks mServiceCallbacks;

    public DnnServiceThread(DnnServiceCallbacks serviceCallbacks, String dnnServerIP, PowerManager.WakeLock wakeLock, String androidId, File filesDir){
        super();
        mDnnServerIP = dnnServerIP;
        mAndroidId = androidId;
        mWakeLock = wakeLock;
        mFilesDir = filesDir;
        mServiceCallbacks = serviceCallbacks;
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
        TcpClient tcpClient = new TcpClient(mDnnServerIP);
        DnnDataDownloader dataDownloader = new DnnDataDownloader(mFilesDir);
        ClientLogic clientLogic = new ClientLogic(this, tcpClient, dataDownloader, mAndroidId);

        try {
            mServiceCallbacks.printMessage("Connecting to Dnn server...");
            tcpClient.start();
            mServiceCallbacks.printMessage("Connected Successfully!\n"+
                "Dnn client is Running!");
            clientLogic.run();

        } catch (IOException e){
            if(e.getMessage() != null){
                Log.e(TAG, e.getMessage());
            } else {
                Log.e(TAG, e.getClass().getSimpleName() + " Occured!" );
            }
        } finally {
            mServiceCallbacks.printMessage("Sorry, faild to connect to Dnn server! "+
                "Please check address and try again...");
        }

        try {
            tcpClient.stop();
        } catch (IOException e) {
            Log.e(TAG, "Could not stop tcp client!");
            e.printStackTrace();
        }

        // unlocking the partial wake lock
        mWakeLock.release();
    }

}
