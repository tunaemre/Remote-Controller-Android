package com.tunaemre.remotecontroller.view;

import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.tunaemre.remotecontroller.R;

public abstract class ExtendedAppCombatActivity extends AppCompatActivity {

    public interface onSetContentViewListener {
        void onSet();
    }

    public interface onWindowFocusChangedListener {
        void onChange(boolean hasFocus);
    }

    static onSetContentViewListener mOnSetContentViewListener = null;
    static onWindowFocusChangedListener mOnWindowFocusChangedListener = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apply(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        apply(this);
    }

    public static void apply(AppCompatActivity activity) {
        IExtendedAppCombatActivity interf = activity.getClass().getAnnotation(IExtendedAppCombatActivity.class);
        if (interf != null)
            apply(activity, interf);
    }


    private static void apply(final AppCompatActivity activity, final IExtendedAppCombatActivity interf) {

        //Theme
        if (interf.theme() == IExtendedAppCombatActivity.ActivityTheme.DARK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.setStatusBarColor(activity.getResources().getColor(R.color.colorPrimaryDark));
            }
        }
        else if (interf.theme() == IExtendedAppCombatActivity.ActivityTheme.LIGHT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.setStatusBarColor(activity.getResources().getColor(R.color.colorLightStatusBar));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Window window = activity.getWindow();
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        else if (interf.theme() == IExtendedAppCombatActivity.ActivityTheme.TRANSPARENT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                mOnWindowFocusChangedListener = new onWindowFocusChangedListener() {
                    @Override
                    public void onChange(boolean hasFocus) {
                        Window window = activity.getWindow();
                        if (hasFocus)
                            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    }
                };
            }
        }

        if (interf.customToolBar() != -1 || !TextUtils.isEmpty(interf.title()) || interf.titleRes() != -1) {
            mOnSetContentViewListener = new onSetContentViewListener() {
                @Override
                public void onSet() {

                    //Custom Toolbar
                    if (interf.customToolBar() != -1)
                        activity.setSupportActionBar((Toolbar) activity.findViewById(interf.customToolBar()));

                    //Title
                    if (!TextUtils.isEmpty(interf.title()))
                        activity.getSupportActionBar().setTitle(interf.title());
                    else if (interf.titleRes() != -1)
                        activity.getSupportActionBar().setTitle(interf.titleRes());
                }
            };

        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (mOnWindowFocusChangedListener != null)
            mOnWindowFocusChangedListener.onChange(hasFocus);

        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        if (mOnSetContentViewListener != null)
            mOnSetContentViewListener.onSet();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        if (mOnSetContentViewListener != null)
            mOnSetContentViewListener.onSet();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        if (mOnSetContentViewListener != null)
            mOnSetContentViewListener.onSet();
    }

    protected abstract void prepareActivity();
}
