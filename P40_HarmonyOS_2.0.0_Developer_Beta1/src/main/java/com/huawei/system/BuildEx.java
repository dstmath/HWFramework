package com.huawei.system;

import android.content.res.Resources;
import android.text.TextUtils;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.utils.HwDeviceFingerprint;

public class BuildEx {
    public static final String DISPLAY = HwDeviceFingerprint.getString("ro.huawei.build.display.id");
    public static final String FINGERPRINT = HwDeviceFingerprint.deriveFingerprint();
    public static final String HOST = HwDeviceFingerprint.getString("ro.huawei.build.host");
    public static final String INCREMENTAL = HwDeviceFingerprint.getString("ro.huawei.build.version.incremental");
    public static final String SECURITY_PATCH = SystemPropertiesEx.get("ro.huawei.build.version.security_patch", "");
    public static final Long TIME = Long.valueOf(getLong("ro.huawei.build.date.utc") * 1000);

    public static final class OsBrand {
        public static final String EMUI = "emui";
        public static final String HARMONY = "harmony";
    }

    private BuildEx() {
    }

    public static String getRadioVersion() {
        String propVal = SystemPropertiesEx.get("gsm.version.baseband", "");
        if (TextUtils.isEmpty(propVal)) {
            return null;
        }
        return propVal;
    }

    private static long getLong(String property) {
        try {
            return Long.parseLong(SystemPropertiesEx.get(property, ""));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static String getOsBrand() {
        return Resources.getSystem().getString(17039873);
    }
}
