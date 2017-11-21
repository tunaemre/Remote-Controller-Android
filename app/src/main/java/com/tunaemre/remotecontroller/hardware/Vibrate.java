package com.tunaemre.remotecontroller.hardware;

import android.content.Context;
import android.os.Vibrator;

import com.tunaemre.remotecontroller.cache.Cache;

public class Vibrate {

    private Vibrator mVibrator;

    public Vibrate(Context context) {
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private volatile static Vibrate instance = null;

    public static Vibrate getInstance(Context context) {
        if (instance == null) {
            synchronized (Vibrate.class) {
                if (instance == null)
                    instance = new Vibrate(context);
            }
        }

        return instance;
    }

    public void makeSingleHapticFeedback() {
        mVibrator.vibrate(new long[] {0, 25}, -1);
    }

    public void makeDoubleHapticFeedback() {
        mVibrator.vibrate(new long[] {0, 25, 50, 25}, -1);
    }
}
