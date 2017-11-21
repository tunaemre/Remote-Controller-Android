package com.tunaemre.remotecontroller.view;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
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
    int titleRes() default -1;

    int customToolBar() default -1;
}
