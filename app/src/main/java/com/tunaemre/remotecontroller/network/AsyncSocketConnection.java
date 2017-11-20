package com.tunaemre.remotecontroller.network;

import android.content.Context;
import android.os.AsyncTask;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class AsyncSocketConnection {
    public enum SocketConnectionResult {
        Success,
        SocketInitError,
        DataSendError,
        GeneralError
    }

    private volatile static AsyncSocketConnection instance = null;

    public static AsyncSocketConnection getInstance() {
        if (instance == null) {
            synchronized (AsyncSocketConnection.class) {
                if (instance == null)
                    instance = new AsyncSocketConnection();
            }
        }

        return instance;
    }

    public interface ResultListener {
        void onStart();
        void onResult(SocketConnectionResult result);
    }

    public void runSocketConnection(String ipAddress, int portNumber, String data) {
        new MakeSocketConnection(ipAddress, portNumber, data).execute();
    }

    public void runSocketConnection(String ipAddress, int portNumber, String data, ResultListener listener) {
        new MakeSocketConnection(ipAddress, portNumber, data).addListener(listener).execute();
    }

    private class MakeSocketConnection {
        ResultListener mListener;

        String mIPAddress;
        int mPortNumber;
        String mData;

        Socket mSocket = null;

        private void safeCloseSocket(Socket socket) {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private MakeSocketConnection(String ipAddress, int portNumber, String data) {
            this.mIPAddress = ipAddress;
            this.mPortNumber = portNumber;
            this.mData = data;
        }

        private MakeSocketConnection addListener(ResultListener listener) {
            this.mListener = listener;
            return this;
        }

        private void execute() {
            new tcpConnectionTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        private class tcpConnectionTask extends AsyncTask<Void, Void, SocketConnectionResult> {
            @Override
            protected void onPreExecute() {
                if (mListener != null)
                    mListener.onStart();
                mSocket = new Socket();
            }

            protected SocketConnectionResult doInBackground(Void... params) {
                if (mData == null)
                    return SocketConnectionResult.GeneralError;

                OutputStream outputStream = null;

                try {
                    mSocket.bind(null);
                    mSocket.connect(new InetSocketAddress(mIPAddress, mPortNumber), 20000);
                    outputStream = mSocket.getOutputStream();
                } catch (Exception e) {
                    e.printStackTrace();
                    return SocketConnectionResult.SocketInitError;
                }

                try {
                    outputStream.write(mData.getBytes());
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    return SocketConnectionResult.DataSendError;
                }

                return SocketConnectionResult.Success;
            }

            protected void onPostExecute(SocketConnectionResult result) {
                if (mSocket != null && !mSocket.isClosed()) {
                    safeCloseSocket(mSocket);
                    mSocket = null;
                }
                if (mListener != null)
                    mListener.onResult(result);
            }
        }
    }
}