package com.tunaemre.remotecontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;


public class ClipboardActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String data = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (ControllerActivity.isConnected) {
            ControllerActivity.sendClipboardMessage(data);
            startActivity(new Intent(this, ControllerActivity.class));
        }
        else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("data", data);
            startActivity(intent);
        }

        finish();
    }
}
