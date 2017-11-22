package com.tunaemre.remotecontroller.network;

import android.os.AsyncTask;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class AsyncSocketConnection {

    public enum SocketConnectionResult {
        Success,
        AuthError,
        SocketInitError,
        DataTransmitError,
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

    public interface InputStreamListener {
        void onReceive(String data) throws AuthenticationException, JSONException;
    }

    public void runSocketConnection(String ipAddress, int portNumber, String data) {
        new MakeSocketConnection(ipAddress, portNumber, data).execute();
    }

    public void runSocketConnection(String ipAddress, int portNumber, String data, ResultListener listener) {
        new MakeSocketConnection(ipAddress, portNumber, data).addResultListener(listener).execute();
    }

    public void runSocketConnection(String ipAddress, int portNumber, String data, InputStreamListener listener) {
        new MakeSocketConnection(ipAddress, portNumber, data).addInputStreamListener(listener).execute();
    }

    public void runSocketConnection(String ipAddress, int portNumber, String data, InputStreamListener inputStreamListener, ResultListener resultListener) {
        new MakeSocketConnection(ipAddress, portNumber, data).addInputStreamListener(inputStreamListener).addResultListener(resultListener).execute();
    }

    private class MakeSocketConnection {
        ResultListener mResultListener;
        InputStreamListener mInputStreamListener;

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

        private MakeSocketConnection addResultListener(ResultListener listener) {
            this.mResultListener = listener;
            return this;
        }

        private MakeSocketConnection addInputStreamListener(InputStreamListener listener) {
            this.mInputStreamListener = listener;
            return this;
        }

        private void execute() {
            new tcpConnectionTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        private class tcpConnectionTask extends AsyncTask<Void, Void, SocketConnectionResult> {
            @Override
            protected void onPreExecute() {
                if (mResultListener != null)
                    mResultListener.onStart();
                mSocket = new Socket();
            }

            protected SocketConnectionResult doInBackground(Void... params) {
                if (mData == null)
                    return SocketConnectionResult.GeneralError;

                OutputStream outputStream = null;
                InputStream inputStream = null;

                try {
                    mSocket.bind(null);
                    mSocket.connect(new InetSocketAddress(mIPAddress, mPortNumber), 3000);
                    outputStream = mSocket.getOutputStream();

                    if (mInputStreamListener != null)
                        inputStream = mSocket.getInputStream();
                } catch (Exception e) {
                    e.printStackTrace();
                    return SocketConnectionResult.SocketInitError;
                }

                try {
                    outputStream.write(mData.getBytes());
                    outputStream.flush();

                    if (mInputStreamListener != null && inputStream != null)
                    {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();

                        int charsRead = 0;
                        char[] buffer = new char[512];
                        while ((charsRead = bufferedReader.read(buffer)) != -1) {
                            response.append(new String(buffer).substring(0, charsRead));
                        }

                        mInputStreamListener.onReceive(response.toString());

                        inputStream.close();
                    }

                    outputStream.close();
                } catch (AuthenticationException e) {
                    e.printStackTrace();
                    return SocketConnectionResult.AuthError;
                } catch (Exception e) {
                    e.printStackTrace();
                    return SocketConnectionResult.DataTransmitError;
                }

                return SocketConnectionResult.Success;
            }

            protected void onPostExecute(SocketConnectionResult result) {
                if (mSocket != null && !mSocket.isClosed()) {
                    safeCloseSocket(mSocket);
                    mSocket = null;
                }
                if (mResultListener != null)
                    mResultListener.onResult(result);
            }
        }
    }
}