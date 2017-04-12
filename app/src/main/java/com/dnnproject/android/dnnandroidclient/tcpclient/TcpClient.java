package com.dnnproject.android.dnnandroidclient.tcpclient;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import dnnUtil.dnnMessage.DnnMessage;


public class TcpClient implements DnnMessageTransceiver{

    // server default access port
    public static final int SERVER_PORT = 2828;

    // server ip
    public final String mServerIP;
    // while this is true, the client will continue running
    private boolean mRun = false;

    // the connection socket
    private Socket mSocket;

    // input and output streams
    private ObjectInputStream mInputStream;
    private ObjectOutputStream mOutputStream;

    // implementing some shared resource safety:
    private final Lock mInputLock = new ReentrantLock(true);
    private final Lock mOutputLock = new ReentrantLock(true);
    private final Semaphore mInputSemaphore = new Semaphore(0, true);
    private final Semaphore mOutputSemaphore = new Semaphore(0, true);

    // creating messages queues
    private final Queue<DnnMessage> mInputMessageQueue = new LinkedBlockingDeque<>();
    private final Queue<DnnMessage> mOutputMessageQueue = new LinkedBlockingDeque<>();

    // creating the input and output runables
    private final Thread mInputListener = new Thread() {
        /** this Runnable is for reading DnnMessage objects from the input stream
         * If the object is not a DnnMessage and exeption should be thrown
         */
        @Override
        public void run() {
            while(mRun == true){
                try {

                    Object message = mInputStream.readObject();
                    if (!(message instanceof DnnMessage)) {
                        // TODO: create this kind of exception!
                        throw new NotDnnMessageException("TcpClient: the received message is not a DnnMessage");
                    } else {
                        mInputLock.lock();
                            mInputMessageQueue.add((DnnMessage) message);
                        mInputLock.unlock();
                        mInputSemaphore.release();
                    }

                } catch (NotDnnMessageException e) {
                    //Todo: do something usefull here
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
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
                    mOutputSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mOutputLock.lock();
                    DnnMessage message = mOutputMessageQueue.remove();
                mOutputLock.unlock();

                try {
                    mOutputStream.writeObject(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    };

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(String serverIP) {
        mServerIP = serverIP;
    }

    @Override
    public void sendMessage(DnnMessage message){
        mOutputLock.lock();
            mOutputMessageQueue.add(message);
        mOutputLock.unlock();
        mOutputSemaphore.release();
    }

    @Override
    public DnnMessage getMessage(){
        try {
            mInputSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mInputLock.lock();
            DnnMessage message = mInputMessageQueue.remove();
        mInputLock.unlock();

        return message;
    }

    public void start() {

        mRun = true;
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(mServerIP);
            //create a socket to make the connection with the server
            mSocket = new Socket(serverAddr, SERVER_PORT);
            // initiate output and input streams
            mInputStream = new ObjectInputStream(mSocket.getInputStream());
            mOutputStream = new ObjectOutputStream(mSocket.getOutputStream());

            // starting input and output listeners threads
            mInputListener.start();
            mOutputListener.start();

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

    }

    /**
     * Close the connection and release the members
     */
    public void stop() throws IOException {

        // send mesage that we are closing the connection
        //sendMessage(null/* stop connection message */);

        // set mRun to false so that the thread's running loop will finish
        mRun = false;

        // close all connection buffers and streams
        mOutputStream.close();
        mInputStream.close();
        mSocket.close();
    }

    // declaring NotDnnMessageException for TcpClient's use:
    class NotDnnMessageException extends Exception{
        public NotDnnMessageException() { super(); }
        public NotDnnMessageException(String message) { super(message); }
        public NotDnnMessageException(String message, Throwable cause) { super(message, cause); }
        public NotDnnMessageException(Throwable cause) { super(cause); }
    }
}
