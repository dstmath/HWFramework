package android.app;

import android.os.SystemProperties;

public class HwCustNotificationImpl extends HwCustNotification {
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");

    public boolean calculateEmphazisedMode(boolean fullScreenIntent) {
        if (IS_DOCOMO) {
            return fullScreenIntent;
        }
        return false;
    }
}
