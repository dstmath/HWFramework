package huawei.android.hwgallerycache;

import android.util.Log;

public class Utils {
    private static final String TAG = "Utils";

    public static boolean versionInRange(int checkedVersion, String versionRanage) {
        String versionPreRange;
        if (versionRanage == null) {
            return false;
        }
        int versionIndex = versionRanage.indexOf(";");
        if (versionIndex >= 0) {
            versionPreRange = versionRanage.substring(0, versionIndex);
        } else {
            versionPreRange = versionRanage;
        }
        for (String str : versionPreRange.split(",")) {
            String[] VersionStartAndEnd = str.split("-");
            if (VersionStartAndEnd.length >= 2) {
                try {
                    int checkedVersionStart = Integer.parseInt(VersionStartAndEnd[0]);
                    int checkedVersionEnd = Integer.parseInt(VersionStartAndEnd[1]);
                    if (checkedVersion >= checkedVersionStart && checkedVersion <= checkedVersionEnd) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "version number format error");
                    return false;
                }
            }
        }
        if (versionIndex >= 0) {
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
        }
        return false;
    }
}
