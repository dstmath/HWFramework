package huawei.android.hwgallerycache;

import android.util.Log;

public class Utils {
    private static final String TAG = "Utils";

    public static boolean versionInRange(int checkedVersion, String versionRanage) {
        if (versionRanage == null) {
            return false;
        }
        int i;
        int versionIndex = versionRanage.indexOf(";");
        String versionPreRange;
        if (versionIndex >= 0) {
            versionPreRange = versionRanage.substring(0, versionIndex);
        } else {
            versionPreRange = versionRanage;
        }
        for (String split : versionPreRange.split(",")) {
            String[] VersionStartAndEnd = split.split("-");
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
            String[] versionPostArray = versionRanage.substring(versionIndex + 1).split(",");
            int versionPostArrayLen = versionPostArray.length;
            i = 0;
            while (i < versionPostArrayLen) {
                try {
                    if (checkedVersion == Integer.parseInt(versionPostArray[i])) {
                        return true;
                    }
                    i++;
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "version number format error");
                    return false;
                }
            }
        }
        return false;
    }
}
