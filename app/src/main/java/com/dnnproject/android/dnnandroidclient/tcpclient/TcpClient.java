package com.dnnproject.android.dnnandroidclient.tcpclient;

import android.util.Log;

import com.dnnproject.android.dnnandroidclient.messageswitch.MessageSender;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import dnn.message.DnnMessage;
import dnn.message.DnnTestMessage;


public class TcpClient extends Thread implements MessageSender{

    // server default access port
    public static final int SERVER_PORT = 2828;

    // server ip
    public final String mServerIP;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the client will continue running
    private boolean mRun = false;

    // the connection socket
    private Socket mSocket;

    // input and output streams
    private ObjectInputStream mInputStream;
    private ObjectOutputStream mOutputStream;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(String serverIP, OnMessageReceived listener) {
        mServerIP = serverIP;
        mMessageListener = listener;
    }

    @Override
    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(mServerIP);

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVER_PORT);

            // initiate output and input streams
            mInputStream = new ObjectInputStream(socket.getInputStream());
            mOutputStream = new ObjectOutputStream(socket.getOutputStream());

            try {

                // send login name
                //sendMessage(null);

                //in this while the client listens for the messages sent by the server
                while (mRun) {

                    // TEST with server
                    mOutputStream.writeObject(new DnnTestMessage("GoGo","llll"));
                    Thread.sleep(10000);
                    ///

//                    mServerMessage = mBufferIn.readLine();
//
//                    if (mServerMessage != null && mMessageListener != null) {
//                        //call the method messageReceived from MyActivity class
//                        mMessageListener.messageReceived(mServerMessage);
//                    }

                }
            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() throws IOException {

        // send mesage that we are closing the connection
        //sendMessage(null/* stop connection message */);

        // set mRun to false so that the thread's running loop will finish
        mRun = false;

        // close all connection buffers and streams
        mOutputStream.close();
        mInputStream.close();
        mSocket.close();
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message DNNMessage object
     * @throws IOException
     */
    @Override
    public void sendMessage(DnnMessage message) throws IOException {
        if(mOutputStream != null) {
            mOutputStream.writeObject(message);
        } else {
            throw new IOException("TCP client: No TCP connection available");
        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
