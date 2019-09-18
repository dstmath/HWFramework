package android.telephony;

import android.os.SystemProperties;

public class HwCustUtil {
    public static final boolean isVZW = ("389".equals(SystemProperties.get("ro.config.hw_opta")) && "840".equals(SystemProperties.get("ro.config.hw_optb")));
    public static final boolean isVoLteOn = SystemProperties.getBoolean("ro.config.hw_volte_on", false);
    public static final boolean isVoWiFi = SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false);
}
