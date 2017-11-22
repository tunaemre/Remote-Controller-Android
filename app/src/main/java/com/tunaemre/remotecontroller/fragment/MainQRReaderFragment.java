package com.tunaemre.remotecontroller.fragment;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.samples.vision.barcodereader.BarcodeCapture;
import com.google.android.gms.samples.vision.barcodereader.BarcodeGraphic;
import com.google.android.gms.samples.vision.barcodereader.BarcodeRetriever;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.tunaemre.remotecontroller.ControllerActivity;
import com.tunaemre.remotecontroller.MainActivity;
import com.tunaemre.remotecontroller.R;
import com.tunaemre.remotecontroller.model.ConnectionModel;
import com.tunaemre.remotecontroller.operator.PermissionOperator;

import org.json.JSONObject;

import java.util.List;

public class MainQRReaderFragment extends Fragment {

    private static PermissionOperator permissionOperator = new PermissionOperator();

    private RelativeLayout layout = null;

    public MainQRReaderFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = (RelativeLayout) inflater.inflate(R.layout.layout_main_qrreader, null);

        prepareFragment();
        return layout;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionOperator.REQUEST_CAMERA_PERMISSION)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                prepareFragment();
            else
                Snackbar.make(getActivity().findViewById(R.id.coordinator), "Camera permission should be granted.", Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prepareFragment();
                    }
                }).show();
        }
    }

    public void prepareFragment() {
        if (!permissionOperator.isCameraPermissionGranded(getContext()))
        {
            permissionOperator.requestCameraPermission(getActivity());
            return;
        }
        final BarcodeCapture barcodeCapture = (BarcodeCapture)getChildFragmentManager().findFragmentById(R.id.barcode_reader);

        if (barcodeCapture != null) {
            barcodeCapture.setRetrieval(new BarcodeRetriever() {
                @Override
                public void onRetrieved(Barcode barcode)
                {
                    String barcodeData = barcode.displayValue;

                    try
                    {
                        JSONObject barcodeObj = new JSONObject(barcodeData);

                        Intent intent = new Intent(getActivity(), ControllerActivity.class);
                        intent.putExtra("model", new ConnectionModel(barcodeObj));
                        getActivity().startActivityForResult(intent, MainActivity.CONNECTION_REQUEST_CODE);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "An error occurred.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                @Override
                public void onRetrievedMultiple(Barcode closetToClick, List<BarcodeGraphic> barcode) {
                }

                @Override
                public void onBitmapScanned(SparseArray<Barcode> sparseArray) {
                }

                @Override
                public void onRetrievedFailed(String reason) {
                }
            });
        }
    }
}
