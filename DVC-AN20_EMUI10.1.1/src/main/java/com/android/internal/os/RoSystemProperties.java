package com.android.internal.os;

import android.os.SystemProperties;
import android.sysprop.CryptoProperties;

public class RoSystemProperties {
    public static final boolean CEC_AUDIO_DEVICE_FORWARD_VOLUME_KEYS_SYSTEM_AUDIO_MODE_OFF = SystemProperties.getBoolean("ro.hdmi.cec_audio_device_forward_volume_keys_system_audio_mode_off", false);
    public static final boolean CONFIG_AVOID_GFX_ACCEL = SystemProperties.getBoolean("ro.config.avoid_gfx_accel", false);
    public static final boolean CONFIG_LOW_RAM = SystemProperties.getBoolean("ro.config.low_ram", false);
    public static final boolean CONFIG_SMALL_BATTERY = SystemProperties.getBoolean("ro.config.small_battery", false);
    public static final String CONTROL_PRIVAPP_PERMISSIONS = SystemProperties.get("ro.control_privapp_permissions");
    public static final boolean CONTROL_PRIVAPP_PERMISSIONS_DISABLE;
    public static final boolean CONTROL_PRIVAPP_PERMISSIONS_ENFORCE = "enforce".equalsIgnoreCase(CONTROL_PRIVAPP_PERMISSIONS);
    public static final boolean CONTROL_PRIVAPP_PERMISSIONS_LOG = "log".equalsIgnoreCase(CONTROL_PRIVAPP_PERMISSIONS);
    public static final boolean CRYPTO_BLOCK_ENCRYPTED = (CRYPTO_TYPE == CryptoProperties.type_values.BLOCK);
    public static final boolean CRYPTO_ENCRYPTABLE = (CRYPTO_STATE != CryptoProperties.state_values.UNSUPPORTED);
    public static final boolean CRYPTO_ENCRYPTED = (CRYPTO_STATE == CryptoProperties.state_values.ENCRYPTED);
    public static final boolean CRYPTO_FILE_ENCRYPTED = (CRYPTO_TYPE == CryptoProperties.type_values.FILE);
    public static final CryptoProperties.state_values CRYPTO_STATE = CryptoProperties.state().orElse(CryptoProperties.state_values.UNSUPPORTED);
    public static final CryptoProperties.type_values CRYPTO_TYPE = CryptoProperties.type().orElse(CryptoProperties.type_values.NONE);
    public static final boolean DEBUGGABLE = (SystemProperties.getInt("ro.debuggable", 0) == 1);
    public static final int FACTORYTEST = SystemProperties.getInt("ro.factorytest", 0);
    public static final boolean FW_SYSTEM_USER_SPLIT = SystemProperties.getBoolean("ro.fw.system_user_split", false);
    public static final boolean MULTIUSER_HEADLESS_SYSTEM_USER = SystemProperties.getBoolean("ro.fw.mu.headless_system_user", false);
    public static final String PROPERTY_HDMI_IS_DEVICE_HDMI_CEC_SWITCH = "ro.hdmi.property_is_device_hdmi_cec_switch";

    static {
        boolean z = false;
        if (!CONTROL_PRIVAPP_PERMISSIONS_LOG && !CONTROL_PRIVAPP_PERMISSIONS_ENFORCE) {
            z = true;
        }
        CONTROL_PRIVAPP_PERMISSIONS_DISABLE = z;
    }
}
