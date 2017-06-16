package com.dnnproject.android.dnnandroidclient.downloader;

import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by nitai on 31/05/17.
 */

public class DnnDataDownloader {
    private static final String TAG = "DnnDataDownloader";

    private final String mBaseURL = "https://raw.githubusercontent.com/NitaiLavie/DnnDataSets/master/";
    private final File mFilesDir;

    public DnnDataDownloader(File filesDir){
        mFilesDir = filesDir;
    }

    public String[] download(String dataSet, String dataType, Integer dataSize, Integer dataIndex) throws Exception{
        URL dataURL = null;
        URL labelsURL = null;
        File dataPath = null;
        File labelsPath = null;
        try{
            if (dataSet.equals("mnist")){
                dataURL      = new URL(mnistDataURL(dataSet, dataType, dataSize, dataIndex));
                labelsURL    = new URL(mnistLabelsURL(dataSet, dataType, dataSize, dataIndex));
                dataPath     = new File(mnistDataPath(dataSet, dataType, dataSize, dataIndex));
                labelsPath   = new File(mnistLabelsPath(dataSet, dataType, dataSize, dataIndex));
            } else if(dataSet.equals("cifar10")){
                dataURL      = new URL(cifar10DataURL(dataSet, dataType, dataSize, dataIndex));
                dataPath     = new File(cifar10DataPath(dataSet, dataType, dataSize, dataIndex));
            }
            else {
                //Todo: ERROR
                throw new Exception("illegal dataSet");
            }

            if(dataPath != null && !dataPath.exists()) {
                FileUtils.copyURLToFile(dataURL, dataPath);
            } else {
                Log.i(TAG, "download: file "+dataPath+" already exists. no need to download");
            }

            if(labelsPath != null && !labelsPath.exists()) {
                FileUtils.copyURLToFile(labelsURL, labelsPath);
            } else {
                Log.i(TAG, "download: file "+labelsPath+" already exists. no need to download");
            }

        }catch(Exception e){
            Log.e(TAG, "download: "+ e.getMessage());
            e.printStackTrace();
            throw e;
        }
        if (dataSet.equals("mnist")){
            return new String[]{dataPath.getPath(), labelsPath.getPath()};
        } else if(dataSet.equals("cifar10")){
            return new String[]{dataPath.getPath(),"no_labels"};
        } else {
            return null;
        }
    }

    private String mnistDataURL(String dataSet, String dataType, Integer dataSize, Integer dataIndex){
        return mBaseURL+dataSet+"/"+dataType+"-labels.idx1-ubyte."+dataSize+
                "/"+dataType+"-labels.idx1-ubyte."+dataSize+"."+String.format("%02d",dataIndex);
    }

    private String mnistLabelsURL(String dataSet, String dataType, Integer dataSize, Integer dataIndex){
        return mBaseURL+dataSet+"/"+dataType+"-labels.idx1-ubyte."+dataSize+
                "/"+dataType+"-labels.idx1-ubyte."+dataSize+"."+String.format("%02d",dataIndex);
    }

    private String cifar10DataURL(String dataSet, String dataType, Integer dataSize, Integer dataIndex) {
        return mBaseURL+dataSet+"/"+dataType+"_batch.bin."+dataSize+
                "/"+dataType+"_batch.bin."+dataSize+"."+String.format("%02d",dataIndex);
    }

    private String mnistDataPath(String dataSet, String dataType, Integer dataSize, Integer dataIndex){
        return mFilesDir+"/"+dataType+"-images.idx3-ubyte."+dataSize+"."+String.format("%02d",dataIndex);
    }

    private String mnistLabelsPath(String dataSet, String dataType, Integer dataSize, Integer dataIndex){
        return mFilesDir+"/"+dataType+"-labels.idx1-ubyte."+dataSize+"."+String.format("%02d",dataIndex);
    }

    private String cifar10DataPath(String dataSet, String dataType, Integer dataSize, Integer dataIndex) {
        return mFilesDir+"/"+dataType+"_batch.bin."+dataSize+"."+String.format("%02d",dataIndex);
    }

}
