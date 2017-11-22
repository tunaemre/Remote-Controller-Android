package com.tunaemre.remotecontroller;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.tunaemre.remotecontroller.cache.Cache;
import com.tunaemre.remotecontroller.fragment.ControllerGyroMouseFragment;
import com.tunaemre.remotecontroller.fragment.ControllerMediaControlFragment;
import com.tunaemre.remotecontroller.fragment.ControllerMousePadFragment;
import com.tunaemre.remotecontroller.model.AuthJSONObject;
import com.tunaemre.remotecontroller.model.ConnectionModel;
import com.tunaemre.remotecontroller.network.AsyncSocketConnection;
import com.tunaemre.remotecontroller.network.AuthenticationException;
import com.tunaemre.remotecontroller.network.NetworkChangeReceiver;
import com.tunaemre.remotecontroller.view.CircularRevealActivity;
import com.tunaemre.remotecontroller.view.IExtendedAppCombatActivity;

import org.json.JSONException;
import org.json.JSONObject;

@IExtendedAppCombatActivity(theme = IExtendedAppCombatActivity.ActivityTheme.LIGHT, customToolBar = R.id.toolbar)
public class ControllerActivity extends CircularRevealActivity {

    private boolean onBackPressedHook = false;

    private CoordinatorLayout coordinatorLayout;
    private View progressLayout;

    private AlertDialog networkDialog = null;

    private NetworkChangeReceiver.NetworkListener networkListener = new NetworkChangeReceiver.NetworkListener() {
        @Override
        public void onChange(boolean isWifiConnected) {
            if (!isWifiConnected) {
                showWifiNotConnectedDialog();
                return;
            }

            if (isWifiConnected && !isConnected)
                sendHelloMessage();

            if (isWifiConnected && networkDialog != null && networkDialog.isShowing())
            {
                networkDialog.dismiss();
                networkDialog = null;
            }
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_mousepad:
                    prepareMousePadFragment();
                    return true;
                case R.id.navigation_mousegyro:
                    prepareGyroMouseFragment();
                    return true;
                case R.id.navigation_media:
                    prepareMediaControlFragment();
                    return true;
            }
            return false;
        }
    };

    public static ConnectionModel connectionModel = null;

    public static boolean isConnected = false;

    private View imgCommandIndicator = null;
    private int commandIndicatorVisibility = 0;

    public void showCommandIndicator() {
        commandIndicatorVisibility++;
        if (imgCommandIndicator.getVisibility() != View.VISIBLE)
            imgCommandIndicator.setVisibility(View.VISIBLE);
    }

    public void hideCommandIndicator() {
        commandIndicatorVisibility--;
        if (commandIndicatorVisibility < 0)
            commandIndicatorVisibility = 0;
        if (commandIndicatorVisibility == 0 && imgCommandIndicator.getVisibility() == View.VISIBLE)
            imgCommandIndicator.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NetworkChangeReceiver.setNetworkListener(networkListener);

        connectionModel = (ConnectionModel) getIntent().getExtras().getSerializable("model");

        setContentView(R.layout.activity_controller);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);
        progressLayout =  findViewById(R.id.layoutProgress);
        imgCommandIndicator = findViewById(R.id.imgRemoteCommand);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        prepareActivity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        if (!onBackPressedHook) {
            Toast.makeText(this, "Press back again to exit.", Toast.LENGTH_SHORT).show();
            onBackPressedHook = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onBackPressedHook = false;
                }
            }, 1000);
        } else
        {
            if (isConnected)
                setResult(Activity.RESULT_OK);
            else
                setResult(Activity.RESULT_CANCELED);

            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!NetworkChangeReceiver.checkWifiConnection(getApplicationContext()))
            showWifiNotConnectedDialog();
    }

    @Override
    protected void onDestroy() {
        NetworkChangeReceiver.removeNetworkListener();
        if (isConnected)
            sendGoodbyeMessage();
        super.onDestroy();
    }

    @Override
    protected void prepareActivity() {
        prepareMousePadFragment();

        if (NetworkChangeReceiver.checkWifiConnection(getApplicationContext()))
            sendHelloMessage();
    }

    private void prepareMousePadFragment() {
        getSupportActionBar().setTitle("Mouse Pad");
        ControllerMousePadFragment mMousePadFragment = new ControllerMousePadFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, mMousePadFragment).commitAllowingStateLoss();
    }

    private void prepareGyroMouseFragment() {
        getSupportActionBar().setTitle("Gyro Mouse");
        ControllerGyroMouseFragment mGyroMouseFragment = new ControllerGyroMouseFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, mGyroMouseFragment).commitAllowingStateLoss();
    }

    private void prepareMediaControlFragment() {
        getSupportActionBar().setTitle("Media Control");
        ControllerMediaControlFragment mMediaControlFragment = new ControllerMediaControlFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, mMediaControlFragment).commitAllowingStateLoss();
    }

    private void showWifiNotConnectedDialog() {
        if (networkDialog != null && networkDialog.isShowing())
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this).setCancelable(false).setMessage("Wifi connection not available.")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        networkDialog = builder.create();
        networkDialog.show();
    }

    private void sendHelloMessage() {
        try {
            JSONObject object = new JSONObject();
            object.put("Action", "Hello");
            object.put("PIN", connectionModel.pin);
            AsyncSocketConnection.getInstance().runSocketConnection(connectionModel.ip, connectionModel.port, object.toString(), new AsyncSocketConnection.InputStreamListener() {
                @Override
                public void onReceive(String data) throws AuthenticationException, JSONException {
                    String token = null;
                    JSONObject object = new JSONObject(data);
                    if (object.getBoolean("Result")) {
                        token = object.getString("Token");
                        connectionModel.setToken(token);
                    }
                    else
                        throw new AuthenticationException();
                }
            }, new AsyncSocketConnection.ResultListener() {

                @Override
                public void onStart() {
                    progressLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onResult(AsyncSocketConnection.SocketConnectionResult result) {
                    progressLayout.clearAnimation();
                    progressLayout.setAnimation(AnimationUtils.loadAnimation(ControllerActivity.this, android.R.anim.fade_out));
                    progressLayout.setVisibility(View.GONE);

                    isConnected = result == AsyncSocketConnection.SocketConnectionResult.Success;
                    if (isConnected) {
                        Cache.getInstance(getBaseContext()).setLastConnection(connectionModel);
                        if (MainActivity.isPendingDataToSend())
                            sendClipboardMessage(MainActivity.getPendingDataToSend());
                    } else {
                        if (result == AsyncSocketConnection.SocketConnectionResult.AuthError)
                        {
                            final Snackbar snackbar = Snackbar.make(coordinatorLayout, "Authentication error.", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("Close", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }
                        else
                        {
                            final Snackbar snackbar = Snackbar.make(coordinatorLayout, "Cannot connect.", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                    sendHelloMessage();
                                }
                            });
                            snackbar.show();
                        }
                    }
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
            progressLayout.clearAnimation();
            progressLayout.setAnimation(AnimationUtils.loadAnimation(ControllerActivity.this, android.R.anim.fade_out));
            progressLayout.setVisibility(View.GONE);

            final Snackbar snackbar = Snackbar.make(coordinatorLayout, "An error occured.", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Retry", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                    sendHelloMessage();
                }
            });
            snackbar.show();
        }
    }

    private void sendGoodbyeMessage() {
        try {
            JSONObject object = new JSONObject();
            object.put("Action", "Goodbye");
            AsyncSocketConnection.getInstance().runSocketConnection(connectionModel.ip, connectionModel.port, object.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            isConnected = false;
        }
    }

    public static void sendClipboardMessage(String data) {
        try {
            AuthJSONObject object = new AuthJSONObject(connectionModel.getToken());
            object.put("Action", "Clipboard");
            object.put("Data", data);
            AsyncSocketConnection.getInstance().runSocketConnection(connectionModel.ip, connectionModel.port, object.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
