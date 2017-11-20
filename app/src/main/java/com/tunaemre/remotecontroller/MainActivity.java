package com.tunaemre.remotecontroller;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;

import com.tunaemre.remotecontroller.fragment.MainIPFragment;
import com.tunaemre.remotecontroller.fragment.MainQRFragment;
import com.tunaemre.remotecontroller.network.NetworkChangeReceiver;
import com.tunaemre.remotecontroller.operator.PermissionOperator;
import com.tunaemre.remotecontroller.view.ExtendedAppCombatActivity;
import com.tunaemre.remotecontroller.view.IExtendedAppCombatActivity;

@IExtendedAppCombatActivity(theme = IExtendedAppCombatActivity.ActivityTheme.LIGHT, customToolBar = R.id.toolbar)
public class MainActivity extends ExtendedAppCombatActivity {

    private static PermissionOperator permissionOperator = new PermissionOperator();

    private CoordinatorLayout coordinatorLayout;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_ip:
                    prepareIPFragment();
                    return true;
                case R.id.navigation_qr:
                    prepareQRFragment();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        prepareActivity();
    }

    @Override
    protected void prepareActivity() {
        prepareIPFragment();

        if (!NetworkChangeReceiver.checkWifiConnection(getApplicationContext())) {
            final Snackbar snackbar = Snackbar.make(coordinatorLayout, "Please check your wifi connection.", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionOperator.REQUEST_CAMERA_PERMISSION)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                prepareQRFragment();
            else
                Snackbar.make(coordinatorLayout, "Camera permission should be granted.", Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prepareQRFragment();
                    }
                }).show();
        }
    }

    private void prepareIPFragment() {
        MainIPFragment mIPFragment = new MainIPFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, mIPFragment).commitAllowingStateLoss();
    }

    private void prepareQRFragment() {
        if (permissionOperator.isCameraPermissionGranded(this)) {
            MainQRFragment mQRFragment = new MainQRFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, mQRFragment).commitAllowingStateLoss();
        }
        else {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, new Fragment()).commitAllowingStateLoss();
            permissionOperator.requestCameraPermission(this);
        }

    }
}
