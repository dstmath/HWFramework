package android.location;

import android.common.HwFrameworkFactory;

public class LocationManagerEx {
    public static final int POWER_ERROR = -1;
    public static final int POWER_HIGH = 2;
    public static final int POWER_LOW = 1;
    public static final int POWER_NONE = 0;

    public static int getPowerTypeByPackageName(String packageName) {
        IHwInnerLocationManager locationManager = HwFrameworkFactory.getHwInnerLocationManager();
        if (locationManager != null) {
            return locationManager.getPowerTypeByPackageName(packageName);
        }
        return -1;
    }

    public static int logEvent(int type, int event, String parameter) {
        IHwInnerLocationManager locationManager = HwFrameworkFactory.getHwInnerLocationManager();
        if (locationManager != null) {
            return locationManager.logEvent(type, event, parameter);
        }
        return -1;
    }
}
