package com.android.server.pm;

import android.os.SystemProperties;
import com.android.server.UiModeManagerService;
import dalvik.system.DexFile;

public class PackageManagerServiceCompilerMapping {
    static final int REASON_SHARED_INDEX = 6;
    public static final String[] REASON_STRINGS = {"first-boot", "boot", "install", "bg-dexopt", "ab-ota", "inactive", "shared", "bg-speed-dexopt"};

    static {
        if (8 != REASON_STRINGS.length) {
            throw new IllegalStateException("REASON_STRINGS not correct");
        } else if (!"shared".equals(REASON_STRINGS[6])) {
            throw new IllegalStateException("REASON_STRINGS not correct because of shared index");
        }
    }

    private static String getSystemPropertyName(int reason) {
        if (reason < 0 || reason >= REASON_STRINGS.length) {
            throw new IllegalArgumentException("reason " + reason + " invalid");
        }
        return "pm.dexopt." + REASON_STRINGS[reason];
    }

    private static String getAndCheckValidity(int reason) {
        String sysPropValue = SystemProperties.get(getSystemPropertyName(reason));
        if (7 == reason && (sysPropValue == null || sysPropValue.isEmpty())) {
            sysPropValue = "speed";
        }
        if (sysPropValue == null || sysPropValue.isEmpty() || !DexFile.isValidCompilerFilter(sysPropValue)) {
            throw new IllegalStateException("Value \"" + sysPropValue + "\" not valid (reason " + REASON_STRINGS[reason] + ")");
        } else if (isFilterAllowedForReason(reason, sysPropValue)) {
            return sysPropValue;
        } else {
            throw new IllegalStateException("Value \"" + sysPropValue + "\" not allowed (reason " + REASON_STRINGS[reason] + ")");
        }
    }

    private static boolean isFilterAllowedForReason(int reason, String filter) {
        return reason != 6 || !DexFile.isProfileGuidedCompilerFilter(filter);
    }

    static void checkProperties() {
        RuntimeException toThrow = null;
        int reason = 0;
        while (reason <= 7) {
            try {
                String sysPropName = getSystemPropertyName(reason);
                if (sysPropName == null || sysPropName.isEmpty()) {
                    throw new IllegalStateException("Reason system property name \"" + sysPropName + "\" for reason " + REASON_STRINGS[reason]);
                }
                getAndCheckValidity(reason);
                reason++;
            } catch (Exception exc) {
                if (toThrow == null) {
                    toThrow = new IllegalStateException("PMS compiler filter settings are bad.");
                }
                toThrow.addSuppressed(exc);
            }
        }
        if (toThrow != null) {
            throw toThrow;
        }
    }

    public static String getCompilerFilterForReason(int reason) {
        return getAndCheckValidity(reason);
    }

    public static String getDefaultCompilerFilter() {
        String value = SystemProperties.get("dalvik.vm.dex2oat-filter");
        if (value == null || value.isEmpty()) {
            return "speed";
        }
        if (!DexFile.isValidCompilerFilter(value) || DexFile.isProfileGuidedCompilerFilter(value)) {
            return "speed";
        }
        return value;
    }

    public static String getReasonName(int reason) {
        if (reason == -1) {
            return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
        }
        if (reason >= 0 && reason < REASON_STRINGS.length) {
            return REASON_STRINGS[reason];
        }
        throw new IllegalArgumentException("reason " + reason + " invalid");
    }
}
