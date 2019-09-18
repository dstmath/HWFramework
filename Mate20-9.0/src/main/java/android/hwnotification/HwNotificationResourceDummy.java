package android.hwnotification;

import android.hwnotification.HwNotificationResource;
import android.os.Bundle;

public class HwNotificationResourceDummy implements HwNotificationResource.IHwNotificationResource {
    public Bundle getNotificationThemeData(Bundle bundle, int contIconId, int repIconId, int bgIndex, int repLocation) {
        return null;
    }
}
