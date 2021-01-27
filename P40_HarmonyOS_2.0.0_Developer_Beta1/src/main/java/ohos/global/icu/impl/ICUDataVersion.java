package ohos.global.icu.impl;

import java.util.MissingResourceException;
import ohos.global.icu.util.UResourceBundle;
import ohos.global.icu.util.VersionInfo;

public final class ICUDataVersion {
    private static final String U_ICU_DATA_KEY = "DataVersion";
    private static final String U_ICU_VERSION_BUNDLE = "icuver";

    public static VersionInfo getDataVersion() {
        try {
            return VersionInfo.getInstance(UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, U_ICU_VERSION_BUNDLE, ICUResourceBundle.ICU_DATA_CLASS_LOADER).get(U_ICU_DATA_KEY).getString());
        } catch (MissingResourceException unused) {
            return null;
        }
    }
}
