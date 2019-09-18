package android.location;

import android.content.ContentResolver;
import java.util.ArrayList;

public interface IHwInnerLocationManager {
    int checkLocationSettings(ContentResolver contentResolver, String str, String str2, int i);

    int getPowerTypeByPackageName(String str);

    ArrayList<String> gnssDetect(String str);

    int logEvent(int i, int i2, String str);
}
