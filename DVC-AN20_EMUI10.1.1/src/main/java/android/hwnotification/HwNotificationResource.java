package android.hwnotification;

import android.common.HwFrameworkFactory;
import android.os.Bundle;

public class HwNotificationResource {
    public static final int BACKGROUND_INDEX_0 = 0;
    public static final int BACKGROUND_INDEX_1 = 1;
    public static final int BACKGROUND_INDEX_2 = 2;
    public static final int BACKGROUND_INDEX_3 = 3;
    public static final int BACKGROUND_INDEX_4 = 4;
    public static final int BACKGROUND_INDEX_5 = 5;
    public static final int BACKGROUND_INDEX_6 = 6;
    public static final int BACKGROUND_INDEX_7 = 7;
    public static final String HW_NOTIFICATION_BACKGROUND_INDEX = "huawei.notification.backgroundIndex";
    public static final String HW_NOTIFICATION_CONTENT_ICON = "huawei.notification.contentIcon";
    public static final String HW_NOTIFICATION_REPLACE_ICONID = "huawei.notification.replace.iconId";
    public static final String HW_NOTIFICATION_REPLACE_LOCATION = "huawei.notification.replace.location";
    public static final int REPLACE_LOCATION_BIGCONTENT = 4;
    public static final int REPLACE_LOCATION_CONTENT = 2;
    public static final int REPLACE_LOCATION_HEADSUP = 8;
    public static final int REPLACE_LOCATION_LARGEICON = 1;
    private static IHwNotificationResource sInstance = null;

    public interface IHwNotificationResource {
        Bundle getNotificationThemeData(Bundle bundle, int i, int i2, int i3, int i4);
    }

    private static synchronized IHwNotificationResource getImplObject() {
        synchronized (HwNotificationResource.class) {
            if (sInstance != null) {
                return sInstance;
            }
            IHwNotificationResource instance = HwFrameworkFactory.getHwNotificationResource();
            if (instance == null) {
                instance = new HwNotificationResourceDummy();
            }
            sInstance = instance;
            return sInstance;
        }
    }

    public static Bundle getNotificationThemeData(int contIconId, int repIconId, int bgIndex, int repLocation) {
        return getImplObject().getNotificationThemeData(null, contIconId, repIconId, bgIndex, repLocation);
    }

    public static Bundle getNotificationThemeData(Bundle bundle, int contIconId, int repIconId, int bgIndex, int repLocation) {
        return getImplObject().getNotificationThemeData(bundle, contIconId, repIconId, bgIndex, repLocation);
    }
}
