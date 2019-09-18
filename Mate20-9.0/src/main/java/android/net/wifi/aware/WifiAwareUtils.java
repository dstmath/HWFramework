package android.net.wifi.aware;

import android.content.Context;
import android.content.pm.PackageManager;

public class WifiAwareUtils {
    public static void validateServiceName(byte[] serviceNameData) throws IllegalArgumentException {
        if (serviceNameData == null) {
            throw new IllegalArgumentException("Invalid service name - null");
        } else if (serviceNameData.length < 1 || serviceNameData.length > 255) {
            throw new IllegalArgumentException("Invalid service name length - must be between 1 and 255 bytes (UTF-8 encoding)");
        } else {
            int index = 0;
            while (index < serviceNameData.length) {
                byte b = serviceNameData[index];
                if ((b & 128) != 0 || ((b >= 48 && b <= 57) || ((b >= 97 && b <= 122) || ((b >= 65 && b <= 90) || b == 45 || b == 46)))) {
                    index++;
                } else {
                    throw new IllegalArgumentException("Invalid service name - illegal characters, allowed = (0-9, a-z,A-Z, -, .)");
                }
            }
        }
    }

    public static boolean validatePassphrase(String passphrase) {
        if (passphrase == null || passphrase.length() < 8 || passphrase.length() > 63) {
            return false;
        }
        return true;
    }

    public static boolean validatePmk(byte[] pmk) {
        if (pmk == null || pmk.length != 32) {
            return false;
        }
        return true;
    }

    public static boolean isLegacyVersion(Context context, int minVersion) {
        try {
            if (context.getPackageManager().getApplicationInfo(context.getOpPackageName(), 0).targetSdkVersion < minVersion) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }
}
