package ohos.sysappcomponents.settings;

import ohos.app.Context;

public class AppSettings {
    private AppSettings() {
    }

    public static boolean canShowFloating(Context context) {
        return SystemSettingsProxy.canShowFloating(context);
    }

    public static boolean checkSetPermission(Context context) {
        return SystemSettingsProxy.checkSetPermission(context);
    }
}
