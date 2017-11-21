package com.tunaemre.remotecontroller.cache;

import android.content.Context;
import android.content.SharedPreferences;

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

    String CALIBRATION = "TOUCHCALIBRATION";

    public void setTouchCalibration(float pressure)
    {
        mEditor = mPreference.edit();
        mEditor.putFloat(CALIBRATION, pressure);
        mEditor.commit();
    }

    public float getTouchCalibration()
    {
        return mPreference.getFloat(CALIBRATION, -1);
    }
}
