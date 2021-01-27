package huawei.android.hwgallerycache;

import android.util.Log;

public class Utils {
    private static final String TAG = "Utils";

    public static boolean versionInRange(int checkedVersion, String versionRanage) {
        if (versionRanage == null) {
            return false;
        }
        int versionIndex = versionRanage.indexOf(";");
        for (String str : (versionIndex >= 0 ? versionRanage.substring(0, versionIndex) : versionRanage).split(",")) {
            String[] versionStartAndEnd = str.split("-");
            if (versionStartAndEnd.length >= 2) {
                try {
                    int checkedVersionStart = Integer.parseInt(versionStartAndEnd[0]);
                    int checkedVersionEnd = Integer.parseInt(versionStartAndEnd[1]);
                    if (checkedVersion >= checkedVersionStart && checkedVersion <= checkedVersionEnd) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "version number format error");
                    return false;
                }
            }
        }
        if (versionIndex < 0) {
            return false;
        }
        for (String str2 : versionRanage.substring(versionIndex + 1).split(",")) {
            try {
                if (checkedVersion == Integer.parseInt(str2)) {
                    return true;
                }
            } catch (NumberFormatException e2) {
                Log.e(TAG, "version number format error");
                return false;
            }
        }
        return false;
    }
}
