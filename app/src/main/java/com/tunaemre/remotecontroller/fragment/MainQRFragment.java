package com.tunaemre.remotecontroller.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.samples.vision.barcodereader.BarcodeCapture;
import com.google.android.gms.samples.vision.barcodereader.BarcodeGraphic;
import com.google.android.gms.samples.vision.barcodereader.BarcodeRetriever;
import com.google.android.gms.vision.barcode.Barcode;
import com.tunaemre.remotecontroller.ControllerActivity;
import com.tunaemre.remotecontroller.R;
import com.tunaemre.remotecontroller.operator.PermissionOperator;

import java.util.List;

public class MainQRFragment extends Fragment {

    private RelativeLayout layout = null;

    public  MainQRFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = (RelativeLayout) inflater.inflate(R.layout.layout_main_qr, null);

        prepareFragment(layout);
        return layout;
    }

    private void prepareFragment(RelativeLayout layout) {
        final BarcodeCapture barcodeCapture = (BarcodeCapture)getChildFragmentManager().findFragmentById(R.id.barcode_reader);

        if (barcodeCapture != null) {
            barcodeCapture.setRetrieval(new BarcodeRetriever() {
                @Override
                public void onRetrieved(Barcode barcode)
                {
                    String barcodeData = barcode.displayValue;

                    Intent intent = new Intent(getActivity(), ControllerActivity.class);
                    intent.putExtra("ip", "1.1.1.1");
                    startActivity(intent);
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
