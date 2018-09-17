package com.android.server.pm;

import android.os.SystemProperties;
import dalvik.system.DexFile;

public class PackageManagerServiceCompilerMapping {
    static final String[] REASON_STRINGS = new String[]{"first-boot", "boot", "install", "bg-dexopt", "ab-ota", "bg-speed-dexopt"};

    static {
        if (6 != REASON_STRINGS.length) {
            throw new IllegalStateException("REASON_STRINGS not correct");
        }
    }

    private static String getSystemPropertyName(int reason) {
        if (reason >= 0 && reason < REASON_STRINGS.length) {
            return "pm.dexopt." + REASON_STRINGS[reason];
        }
        throw new IllegalArgumentException("reason " + reason + " invalid");
    }

    private static String getAndCheckValidity(int reason) {
        String sysPropValue = SystemProperties.get(getSystemPropertyName(reason));
        if (5 == reason && (sysPropValue == null || sysPropValue.isEmpty())) {
            sysPropValue = "speed";
        }
        if (sysPropValue != null && !sysPropValue.isEmpty() && (DexFile.isValidCompilerFilter(sysPropValue) ^ 1) == 0) {
            return sysPropValue;
        }
        throw new IllegalStateException("Value \"" + sysPropValue + "\" not valid " + "(reason " + REASON_STRINGS[reason] + ")");
    }

    static void checkProperties() {
        RuntimeException toThrow = null;
        int reason = 0;
        while (reason <= 5) {
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
}
