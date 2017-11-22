package com.tunaemre.remotecontroller;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.tunaemre.remotecontroller.cache.Cache;
import com.tunaemre.remotecontroller.model.ConnectionModel;
import com.tunaemre.remotecontroller.view.ExtendedAppCombatActivity;
import com.tunaemre.remotecontroller.view.IExtendedAppCombatActivity;

@IExtendedAppCombatActivity(theme = IExtendedAppCombatActivity.ActivityTheme.LIGHT, customToolBar = R.id.toolbar, titleRes = R.string.title_quickconnect)
public class QuickConnectActivity extends ExtendedAppCombatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24px);

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
        final ConnectionModel connectionModel = Cache.getInstance(this).getLastConnection();
        if (connectionModel != null)
        {
            ((EditText) findViewById(R.id.editIP)).setText(connectionModel.ip);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(QuickConnectActivity.this, ControllerActivity.class);
                    intent.putExtra("model", connectionModel);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
}