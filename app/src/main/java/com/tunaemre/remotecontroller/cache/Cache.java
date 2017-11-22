package com.tunaemre.remotecontroller.cache;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.tunaemre.remotecontroller.model.ConnectionModel;

public class Cache {

    private final String PREF_NAME = "PREF_REMOTECONTOLLER";

    private SharedPreferences mPreference;
    private SharedPreferences.Editor mEditor;

    public Cache(Context context) {
        this.mPreference = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private volatile static Cache instance = null;

    public static Cache getInstance(Context context) {
        if (instance == null) {
            synchronized (Cache.class) {
                if (instance == null)
                    instance = new Cache(context);
            }
        }

        return instance;
    }

    static String CALIBRATION = "TOUCHCALIBRATION";
    static String CONNECTION = "LASTCONNECTION";

    public void setTouchCalibration(float pressure) {
        mEditor = mPreference.edit();
        mEditor.putFloat(CALIBRATION, pressure);
        mEditor.commit();
    }

    public float getTouchCalibration() {
        return mPreference.getFloat(CALIBRATION, -1);
    }

    public void setLastConnection(ConnectionModel model) {
        String modelData = new Gson().toJson(model);
        mEditor = mPreference.edit();
        mEditor.putString(CONNECTION, modelData);
        mEditor.commit();
    }

    public ConnectionModel getLastConnection() {
        String modelData = mPreference.getString(CONNECTION, null);
        if (modelData == null)
            return null;

        ConnectionModel model = new Gson().fromJson(modelData, ConnectionModel.class);
        return model;
    }
}
