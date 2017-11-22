package com.tunaemre.remotecontroller.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tunaemre.remotecontroller.ControllerActivity;
import com.tunaemre.remotecontroller.MainActivity;
import com.tunaemre.remotecontroller.R;
import com.tunaemre.remotecontroller.model.ConnectionModel;

import java.util.regex.Pattern;

public class MainIPFragment extends Fragment {

    private static String IP_ADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    private RelativeLayout layout = null;

    public MainIPFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = (RelativeLayout) inflater.inflate(R.layout.layout_main_ip, null);

        prepareFragment(layout);
        return layout;
    }

    private void prepareFragment(RelativeLayout layout) {
        final EditText editIP = (EditText) layout.findViewById(R.id.editIP);
        final EditText editPIN = (EditText) layout.findViewById(R.id.editPIN);
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(R.id.floatingActionButton);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Pattern.compile(IP_ADDRESS_PATTERN).matcher(editIP.getText().toString()).matches()) {
                    Toast.makeText(getContext(), "Invalid IP address.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(editPIN.getText().toString())) {
                    Toast.makeText(getContext(), "Enter PIN code.", Toast.LENGTH_LONG).show();
                    return;
                }

                ConnectionModel model = new ConnectionModel();
                model.ip = editIP.getText().toString();
                model.pin = editPIN.getText().toString();
                model.port = 13000;

                Intent intent = new Intent(getActivity(), ControllerActivity.class);
                intent.putExtra("model", model);
                getActivity().startActivityForResult(intent, MainActivity.CONNECTION_REQUEST_CODE);
            }
        });
    }
}
