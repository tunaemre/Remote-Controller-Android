package com.tunaemre.remotecontroller.view;

public @interface IExtendedAppCombatActivity {
    enum ActivityTheme {
        DARK,
        LIGHT,
        TRANSPARENT;

        ActivityTheme() {
        }
    }

    ActivityTheme theme() default ActivityTheme.DARK;

    String title() default "";

    int customToolBar() default -1;
}
