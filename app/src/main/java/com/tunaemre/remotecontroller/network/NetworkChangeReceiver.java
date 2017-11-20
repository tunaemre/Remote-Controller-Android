package com.tunaemre.remotecontroller.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;

public class NetworkChangeReceiver extends BroadcastReceiver {

    public interface NetworkListener {
        void onChange(boolean isWifiConnected);
    }

    private static NetworkListener mListener = null;

    public static void setNetworkListener(NetworkListener listener) {
        mListener = listener;
    }

    public static void removeNetworkListener() {
        mListener = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mListener != null)
            mListener.onChange(checkWifiConnection(context));
    }

    public static boolean checkWifiConnection(Context context) {
        try {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            return mWifi.isConnected();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
