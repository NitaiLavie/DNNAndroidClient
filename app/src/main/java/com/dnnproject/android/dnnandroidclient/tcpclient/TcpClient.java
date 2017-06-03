package com.dnnproject.android.dnnandroidclient.tcpclient;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import dnnUtil.dnnMessage.DnnMessage;


public class TcpClient implements DnnMessageTransceiver{
    private static final String TAG = "TcpClient";

    // server default access port
    public static final int SERVER_PORT = 2828;

    // server ip
    public final String mServerIP;
    // while this is true, the client will continue running
    private boolean mRun = false;

    // the connection socket
    private Socket mSocket;

    // input and output streams
    private BufferedInputStream mInputBuf;
    private BufferedOutputStream mOutputBuf;
    private ObjectInputStream mInputStream;
    private ObjectOutputStream mOutputStream;

    // creating messages blocking queues - they are thread safe!
    private final BlockingQueue<DnnMessage> mInputMessageQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<DnnMessage> mOutputMessageQueue = new LinkedBlockingQueue<>();

    // creating the input and output runables
    private final Thread mInputListener = new Thread() {
        /** this Runnable is for reading DnnMessage objects from the input stream
         * If the object is not a DnnMessage and exeption should be thrown
         */
        @Override
        public void run() {
            //Todo: handle case when Server is disconnected and receiving EOF
            while(mRun == true){
                try {

                    Object message = mInputStream.readObject();
                    if (!(message instanceof DnnMessage)) {
                        throw new NotDnnMessageException("Received an Object that is not extending DnnMessage");
                    } else {

                        try {
                            mInputMessageQueue.put((DnnMessage) message);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "mInputListener: input queue put interupt - DnnMessage lost!");
                            e.printStackTrace();
                        }

                    }

                } catch (NotDnnMessageException e) {
                    String msg = e.getMessage();
                    if(msg != null) Log.e(TAG, msg);
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    String msg = e.getMessage();
                    if(msg != null) Log.e(TAG, msg);
                    e.printStackTrace();
                } catch (IOException e) {
                    String msg = e.getMessage();
                    if(msg != null) Log.e(TAG, msg);
                    e.printStackTrace();
                    //Todo: this is temporary! need to pass this exception on (probably server disconnected)
                    mRun = false;
                }
            }
        }
    };

    private final Thread mOutputListener = new Thread() {
        /** this Runnable is for writing DnnMessage objects to the output stream
         * it waits for output messages on the output queue and sends them
         */
        @Override
        public void run() {
            while(mRun == true){
                try {
                    DnnMessage message = mOutputMessageQueue.take();
                    mOutputStream.writeObject(message);
                    mOutputStream.reset();
                    mOutputStream.flush();
                } catch (InterruptedException e) {
                    Log.e(TAG, "mOutputListener: output queue take interupt");
                    e.printStackTrace();
                } catch (IOException e) {
                    String msg = e.getMessage();
                    if(msg != null) Log.e(TAG, msg);
                    e.printStackTrace();
                    //Todo: this is temporary! need to pass this exception on (probably server disconnected)
                    mRun = false;
                }
            }

        }
    };

    /**
     * Constructor of the class.
     */
    public TcpClient(String serverIP) {
        mServerIP = serverIP;
    }

    @Override
    public void sendMessage(DnnMessage message) throws InterruptedException {
        mOutputMessageQueue.put(message);
    }

    @Override
    public DnnMessage getMessage() throws InterruptedException {
        return mInputMessageQueue.take();
    }

    public void start() throws IOException {

        mRun = true;

        //here you must put your computer's IP address.
        InetAddress serverAddr = InetAddress.getByName(mServerIP);
        //create a socket to make the connection with the server
        mSocket = new Socket(serverAddr, SERVER_PORT);
        if(mSocket.isConnected()) {
            // initiate output and input streams
            mOutputBuf = new BufferedOutputStream(mSocket.getOutputStream());
            mOutputStream = new ObjectOutputStream(mOutputBuf);
            mOutputStream.flush();
            mInputBuf = new BufferedInputStream(mSocket.getInputStream());
            mInputStream = new ObjectInputStream(mInputBuf);


            // starting input and output listeners threads
            mInputListener.start();
            mOutputListener.start();
        } else {
            throw new IOException("Could not connect to the server");
        }


    }

    /**
     * Close the connection and release the members
     */
    public void stop() throws IOException {

        // send message that we are closing the connection
        //sendMessage(null/* stop connection message */);

        // set mRun to false so that the thread's running loop will finish
        mRun = false;

        // close all connection buffers and streams
        if(mOutputStream != null) mOutputStream.close();
        if(mInputStream != null) mInputStream.close();
        if(mOutputBuf != null) mOutputBuf.close();
        if(mInputBuf != null) mInputBuf.close();
        if(mSocket != null) mSocket.close();
    }

    // declaring NotDnnMessageException for TcpClient's use:
    class NotDnnMessageException extends Exception{
        public NotDnnMessageException() { super(); }
        public NotDnnMessageException(String message) { super(message); }
        public NotDnnMessageException(String message, Throwable cause) { super(message, cause); }
        public NotDnnMessageException(Throwable cause) { super(cause); }
    }
}
