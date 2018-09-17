package huawei.android.hwnotification;

import android.hwnotification.HwNotificationResource.IHwNotificationResource;
import android.os.Bundle;

public class HwNotificationResourceImpl implements IHwNotificationResource {
    public Bundle getNotificationThemeData(Bundle bundle, int contIconId, int repIconId, int bgIndex, int repLocation) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        if (contIconId > 0) {
            bundle.putInt("huawei.notification.contentIcon", contIconId);
        }
        if (repIconId > 0) {
            bundle.putInt("huawei.notification.replace.iconId", repIconId);
        }
        if (bgIndex >= 0) {
            bundle.putInt("huawei.notification.backgroundIndex", bgIndex);
        }
        if (repLocation > 0) {
            bundle.putInt("huawei.notification.replace.location", repLocation);
        }
        return bundle;
    }
}
