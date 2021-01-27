package android.telephony;

import com.huawei.android.os.SystemPropertiesEx;

public class HwCustUtil {
    public static final boolean isVZW = ("389".equals(SystemPropertiesEx.get("ro.config.hw_opta")) && "840".equals(SystemPropertiesEx.get("ro.config.hw_optb")));
    public static final boolean isVoLteOn = SystemPropertiesEx.getBoolean("ro.config.hw_volte_on", false);
    public static final boolean isVoWiFi = SystemPropertiesEx.getBoolean("ro.vendor.config.hw_vowifi", false);
}
