package android.location;

import android.content.ContentResolver;
import java.util.ArrayList;

public class DefaultHwInnerLocationManager implements IHwInnerLocationManager {
    private static volatile IHwInnerLocationManager sInstance = new DefaultHwInnerLocationManager();

    public static IHwInnerLocationManager getDefault() {
        return sInstance;
    }

    @Override // android.location.IHwInnerLocationManager
    public int getPowerTypeByPackageName(String packageName) {
        return 0;
    }

    @Override // android.location.IHwInnerLocationManager
    public int logEvent(int type, int event, String parameter) {
        return 0;
    }

    @Override // android.location.IHwInnerLocationManager
    public int checkLocationSettings(ContentResolver resolver, String name, String value, int userHandle) {
        return 0;
    }

    @Override // android.location.IHwInnerLocationManager
    public ArrayList<String> gnssDetect(String packageName) {
        return null;
    }
}
