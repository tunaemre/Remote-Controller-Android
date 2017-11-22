package com.tunaemre.remotecontroller;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tunaemre.remotecontroller.cache.Cache;
import com.tunaemre.remotecontroller.hardware.Vibrate;
import com.tunaemre.remotecontroller.view.ExtendedAppCombatActivity;
import com.tunaemre.remotecontroller.view.IExtendedAppCombatActivity;

import java.util.List;
import java.util.Vector;

@IExtendedAppCombatActivity(theme = IExtendedAppCombatActivity.ActivityTheme.LIGHT, customToolBar = R.id.toolbar, titleRes = R.string.title_calibration)
public class CalibrationActivity extends ExtendedAppCombatActivity {

    private CoordinatorLayout coordinatorLayout;
    private ProgressBar progressBar;
    private TextView txtCalibration;

    private ValueAnimator progressBarAnimator;

    private List<Float> hardPressList = new Vector(),
            softPressList = new Vector();

    private int currentStep = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24px);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        txtCalibration = (TextView) findViewById(R.id.txtCalibration);

        prepareActivity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void prepareActivity() {

        View.OnTouchListener touchListener = new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        startProgressBar();
                        return false;
                    case MotionEvent.ACTION_UP:
                        stopProgressBar();
                        return false;
                    default:
                        addPressure(currentStep == 0, event.getSize());
                }
                return false;
            }
        };

        txtCalibration.setText("Please hard press to touch area and keep touching for 3 seconds.");
        findViewById(R.id.touchpadLayout).setOnTouchListener(touchListener);
    }

    private void refreshActivity() {
        Vibrate.getInstance(this).makeDoubleHapticFeedback();
        stopProgressBar();
        currentStep++;

        if (currentStep == 1) {
            txtCalibration.setText("Please soft press to touch area and keep touching for 3 seconds.");
        }
        else if (currentStep == 2) {
            findViewById(R.id.touchpadLayout).setOnTouchListener(null);
            txtCalibration.setText("Completed.");

            float sumOfSoftPress = 0;
            for (float pressure : softPressList)
                sumOfSoftPress += pressure;

            float avgSoftPress = sumOfSoftPress / softPressList.size();

            float maxHardPress = 0;
            for (float pressure : hardPressList)
                if (pressure > maxHardPress) maxHardPress = pressure;

            float calcHardPress = ((maxHardPress - avgSoftPress) * 3 / 4) + avgSoftPress;

            Log.e("Calibration", "avgSoftPress:" + avgSoftPress);
            Log.e("Calibration", "maxHardPress:" + maxHardPress);
            Log.e("Calibration", "calcHardPress:" + calcHardPress);

            Cache.getInstance(CalibrationActivity.this).setTouchCalibration(calcHardPress);

            Snackbar.make(coordinatorLayout, "Calibration completed.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            }).show();
        }
    }

    private void addPressure(boolean isHardPress, float pressure) {
        if (isHardPress)
            hardPressList.add(pressure);
        else
            softPressList.add(pressure);
    }

    private void startProgressBar() {
        Vibrate.getInstance(this).makeSingleHapticFeedback();
        progressBar.setVisibility(View.VISIBLE);
        progressBarAnimator = ValueAnimator.ofInt(0, 100);
        progressBarAnimator.setDuration(3000);
        progressBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int)animation.getAnimatedValue();
                progressBar.setProgress(value);

                if (value == 100)
                    refreshActivity();
            }
        });
        progressBarAnimator.start();
    }

    private void stopProgressBar() {
        if (progressBar.isShown())
            progressBar.setVisibility(View.INVISIBLE);
        if (progressBarAnimator.isRunning())
            progressBarAnimator.cancel();
    }
}