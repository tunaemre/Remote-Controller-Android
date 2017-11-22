package com.tunaemre.remotecontroller.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.tunaemre.remotecontroller.ControllerActivity;
import com.tunaemre.remotecontroller.R;
import com.tunaemre.remotecontroller.network.AsyncSocketConnection;

import org.json.JSONObject;

public class ControllerMediaControlFragment extends Fragment {

    private ControllerActivity activity = null;

    private RelativeLayout layout = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (ControllerActivity) getActivity();
        layout = (RelativeLayout) inflater.inflate(R.layout.layout_controller_media, null);

        prepareFragment(layout);
        return layout;
    }

    private void prepareFragment(RelativeLayout layout) {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId())
                {
                    case R.id.btnVolumeDown:
                        sendMediaCommandAction("VolumeDown");
                        break;
                    case R.id.btnVolumeUp:
                        sendMediaCommandAction("VolumeUp");
                        break;
                    case R.id.btnVolumeMute:
                        sendMediaCommandAction("VolumeMute");
                        break;
                    case R.id.btnMediaPrev:
                        sendMediaCommandAction("MediaPrevTrack");
                        break;
                    case R.id.btnMediaNext:
                        sendMediaCommandAction("MediaNextTrack");
                        break;
                    case R.id.btnMediaPlayPause:
                        sendMediaCommandAction("MediaPlayPause");
                        break;
                    case R.id.btnMediaStop:
                        sendMediaCommandAction("MediaStop");
                        break;
                    case R.id.btnMediaRewind:
                        sendMediaCommandAction("MediaFastRewind");
                        break;
                    case R.id.btnMediaForward:
                        sendMediaCommandAction("MediaFastForward");
                        break;
                }
            }
        };

        layout.findViewById(R.id.btnVolumeDown).setOnClickListener(onClickListener);
        layout.findViewById(R.id.btnVolumeUp).setOnClickListener(onClickListener);
        layout.findViewById(R.id.btnVolumeMute).setOnClickListener(onClickListener);
        layout.findViewById(R.id.btnMediaPrev).setOnClickListener(onClickListener);
        layout.findViewById(R.id.btnMediaNext).setOnClickListener(onClickListener);
        layout.findViewById(R.id.btnMediaPlayPause).setOnClickListener(onClickListener);
        layout.findViewById(R.id.btnMediaStop).setOnClickListener(onClickListener);
        layout.findViewById(R.id.btnMediaRewind).setOnClickListener(onClickListener);
        layout.findViewById(R.id.btnMediaForward).setOnClickListener(onClickListener);
    }

    private void sendMediaCommandAction(String action)
    {
        if (!activity.isConnected)
            return;

        try {
            JSONObject object = new JSONObject();
            object.put("Action", action);

            AsyncSocketConnection.getInstance().runSocketConnection(activity.connectionModel.ip, activity.connectionModel.port, object.toString(), new AsyncSocketConnection.ResultListener() {
                @Override
                public void onStart() {
                    activity.showCommandIndicator();
                }

                @Override
                public void onResult(AsyncSocketConnection.SocketConnectionResult result) {
                    activity.hideCommandIndicator();
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}