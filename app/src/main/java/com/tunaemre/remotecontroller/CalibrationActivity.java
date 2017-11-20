package com.tunaemre.remotecontroller;

import com.tunaemre.remotecontroller.view.ExtendedAppCombatActivity;
import com.tunaemre.remotecontroller.view.IExtendedAppCombatActivity;

@IExtendedAppCombatActivity(theme = IExtendedAppCombatActivity.ActivityTheme.LIGHT, customToolBar = R.id.toolbar)
public class CalibrationActivity extends ExtendedAppCombatActivity {

    @Override
    protected void prepareActivity() {

    }
}