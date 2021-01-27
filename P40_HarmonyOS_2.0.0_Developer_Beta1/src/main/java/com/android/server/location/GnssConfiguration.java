package com.android.server.location;

import android.content.Context;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.StatsLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import libcore.io.IoUtils;

/* access modifiers changed from: package-private */
public class GnssConfiguration {
    private static final String CONFIG_A_GLONASS_POS_PROTOCOL_SELECT = "A_GLONASS_POS_PROTOCOL_SELECT";
    private static final String CONFIG_C2K_HOST = "C2K_HOST";
    private static final String CONFIG_C2K_PORT = "C2K_PORT";
    private static final String CONFIG_ES_EXTENSION_SEC = "ES_EXTENSION_SEC";
    private static final String CONFIG_GPS_LOCK = "GPS_LOCK";
    private static final String CONFIG_LPP_PROFILE = "LPP_PROFILE";
    public static final String CONFIG_NFW_PROXY_APPS = "NFW_PROXY_APPS";
    private static final String CONFIG_SUPL_ES = "SUPL_ES";
    private static final String CONFIG_SUPL_HOST = "SUPL_HOST";
    private static final String CONFIG_SUPL_MODE = "SUPL_MODE";
    private static final String CONFIG_SUPL_PORT = "SUPL_PORT";
    private static final String CONFIG_SUPL_VER = "SUPL_VER";
    private static final String CONFIG_USE_EMERGENCY_PDN_FOR_EMERGENCY_SUPL = "USE_EMERGENCY_PDN_FOR_EMERGENCY_SUPL";
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String DEBUG_PROPERTIES_FILE = "/etc/gps_debug.conf";
    static final String LPP_PROFILE = "persist.sys.gps.lpp";
    private static final int MAX_EMERGENCY_MODE_EXTENSION_SECONDS = 300;
    private static final String TAG = "GnssConfiguration";
    private final Context mContext;
    private int mEsExtensionSec = 0;
    private Properties mProperties;

    /* access modifiers changed from: package-private */
    public interface SetCarrierProperty {
        boolean set(int i);
    }

    private static native HalInterfaceVersion native_get_gnss_configuration_version();

    /* access modifiers changed from: private */
    public static native boolean native_set_emergency_supl_pdn(int i);

    private static native boolean native_set_es_extension_sec(int i);

    /* access modifiers changed from: private */
    public static native boolean native_set_gnss_pos_protocol_select(int i);

    /* access modifiers changed from: private */
    public static native boolean native_set_gps_lock(int i);

    /* access modifiers changed from: private */
    public static native boolean native_set_lpp_profile(int i);

    private static native boolean native_set_satellite_blacklist(int[] iArr, int[] iArr2);

    /* access modifiers changed from: private */
    public static native boolean native_set_supl_es(int i);

    /* access modifiers changed from: private */
    public static native boolean native_set_supl_mode(int i);

    /* access modifiers changed from: private */
    public static native boolean native_set_supl_version(int i);

    /* access modifiers changed from: private */
    public static class HalInterfaceVersion {
        final int mMajor;
        final int mMinor;

        HalInterfaceVersion(int major, int minor) {
            this.mMajor = major;
            this.mMinor = minor;
        }
    }

    GnssConfiguration(Context context) {
        this.mContext = context;
        this.mProperties = new Properties();
    }

    /* access modifiers changed from: package-private */
    public Properties getProperties() {
        return this.mProperties;
    }

    /* access modifiers changed from: package-private */
    public int getEsExtensionSec() {
        return this.mEsExtensionSec;
    }

    /* access modifiers changed from: package-private */
    public String getSuplHost() {
        return this.mProperties.getProperty(CONFIG_SUPL_HOST);
    }

    /* access modifiers changed from: package-private */
    public int getSuplPort(int defaultPort) {
        return getIntConfig(CONFIG_SUPL_PORT, defaultPort);
    }

    /* access modifiers changed from: package-private */
    public String getC2KHost() {
        return this.mProperties.getProperty(CONFIG_C2K_HOST);
    }

    /* access modifiers changed from: package-private */
    public int getC2KPort(int defaultPort) {
        return getIntConfig(CONFIG_C2K_PORT, defaultPort);
    }

    /* access modifiers changed from: package-private */
    public int getSuplMode(int defaultMode) {
        return getIntConfig(CONFIG_SUPL_MODE, defaultMode);
    }

    /* access modifiers changed from: package-private */
    public int getSuplEs(int defaulSuplEs) {
        return getIntConfig(CONFIG_SUPL_ES, defaulSuplEs);
    }

    /* access modifiers changed from: package-private */
    public String getLppProfile() {
        return this.mProperties.getProperty(CONFIG_LPP_PROFILE);
    }

    /* access modifiers changed from: package-private */
    public List<String> getProxyApps() {
        String proxyAppsStr = this.mProperties.getProperty(CONFIG_NFW_PROXY_APPS);
        if (TextUtils.isEmpty(proxyAppsStr)) {
            return Collections.EMPTY_LIST;
        }
        String[] proxyAppsArray = proxyAppsStr.trim().split("\\s+");
        if (proxyAppsArray.length == 0) {
            return Collections.EMPTY_LIST;
        }
        ArrayList proxyApps = new ArrayList(proxyAppsArray.length);
        for (String proxyApp : proxyAppsArray) {
            proxyApps.add(proxyApp);
        }
        return proxyApps;
    }

    /* access modifiers changed from: package-private */
    public void setSatelliteBlacklist(int[] constellations, int[] svids) {
        native_set_satellite_blacklist(constellations, svids);
    }

    /* access modifiers changed from: package-private */
    public void reloadGpsProperties() {
        if (DEBUG) {
            Log.d(TAG, "Reset GPS properties, previous size = " + this.mProperties.size());
        }
        loadPropertiesFromCarrierConfig();
        String lpp_prof = SystemProperties.get(LPP_PROFILE);
        if (!TextUtils.isEmpty(lpp_prof)) {
            this.mProperties.setProperty(CONFIG_LPP_PROFILE, lpp_prof);
        }
        loadPropertiesFromGpsDebugConfig(this.mProperties);
        this.mEsExtensionSec = getRangeCheckedConfigEsExtensionSec();
        logConfigurations();
        final HalInterfaceVersion gnssConfigurationIfaceVersion = native_get_gnss_configuration_version();
        if (gnssConfigurationIfaceVersion != null) {
            if (isConfigEsExtensionSecSupported(gnssConfigurationIfaceVersion) && !native_set_es_extension_sec(this.mEsExtensionSec)) {
                Log.e(TAG, "Unable to set ES_EXTENSION_SEC: " + this.mEsExtensionSec);
            }
            for (Map.Entry<String, SetCarrierProperty> entry : new HashMap<String, SetCarrierProperty>() {
                /* class com.android.server.location.GnssConfiguration.AnonymousClass1 */

                {
                    put(GnssConfiguration.CONFIG_SUPL_VER, $$Lambda$GnssConfiguration$1$9cfNUAWKKutp5KSqhvHSGJNe0ao.INSTANCE);
                    put(GnssConfiguration.CONFIG_SUPL_MODE, $$Lambda$GnssConfiguration$1$384RrX20Mx6OJsRiqsQcSxYdcZc.INSTANCE);
                    if (GnssConfiguration.isConfigSuplEsSupported(gnssConfigurationIfaceVersion)) {
                        put(GnssConfiguration.CONFIG_SUPL_ES, $$Lambda$GnssConfiguration$1$sKzdHBM7V7DxdhcWx1u8hipJYFo.INSTANCE);
                    }
                    put(GnssConfiguration.CONFIG_LPP_PROFILE, $$Lambda$GnssConfiguration$1$5tBf0Ru8L994vqKbXOeOBj2ACA.INSTANCE);
                    put(GnssConfiguration.CONFIG_A_GLONASS_POS_PROTOCOL_SELECT, $$Lambda$GnssConfiguration$1$aaV8BigB_1Oil1H82EHUb0zvWPo.INSTANCE);
                    put(GnssConfiguration.CONFIG_USE_EMERGENCY_PDN_FOR_EMERGENCY_SUPL, $$Lambda$GnssConfiguration$1$8lp2ukEzg_Agf73p3kadqhWUpE.INSTANCE);
                    if (GnssConfiguration.isConfigGpsLockSupported(gnssConfigurationIfaceVersion)) {
                        put(GnssConfiguration.CONFIG_GPS_LOCK, $$Lambda$GnssConfiguration$1$rRu0NBMB8DgPt3DY5__6u_WNl7A.INSTANCE);
                    }
                }
            }.entrySet()) {
                String propertyName = entry.getKey();
                String propertyValueString = this.mProperties.getProperty(propertyName);
                if (propertyValueString != null) {
                    try {
                        if (!entry.getValue().set(Integer.decode(propertyValueString).intValue())) {
                            Log.e(TAG, "Unable to set " + propertyName);
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Unable to parse propertyName: " + propertyValueString);
                    }
                }
            }
        } else if (DEBUG) {
            Log.d(TAG, "Skipped configuration update because GNSS configuration in GPS HAL is not supported");
        }
    }

    private void logConfigurations() {
        StatsLog.write(132, getSuplHost(), getSuplPort(0), getC2KHost(), getC2KPort(0), getIntConfig(CONFIG_SUPL_VER, 0), getSuplMode(0), getSuplEs(0) == 1, getIntConfig(CONFIG_LPP_PROFILE, 0), getIntConfig(CONFIG_A_GLONASS_POS_PROTOCOL_SELECT, 0), getIntConfig(CONFIG_USE_EMERGENCY_PDN_FOR_EMERGENCY_SUPL, 0) == 1, getIntConfig(CONFIG_GPS_LOCK, 0), getEsExtensionSec(), this.mProperties.getProperty(CONFIG_NFW_PROXY_APPS));
    }

    /* access modifiers changed from: package-private */
    public void loadPropertiesFromCarrierConfig() {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configManager != null) {
            int ddSubId = SubscriptionManager.getDefaultDataSubscriptionId();
            PersistableBundle configs = SubscriptionManager.isValidSubscriptionId(ddSubId) ? configManager.getConfigForSubId(ddSubId) : null;
            if (configs == null) {
                if (DEBUG) {
                    Log.d(TAG, "SIM not ready, use default carrier config.");
                }
                configs = CarrierConfigManager.getDefaultConfig();
            }
            for (String configKey : configs.keySet()) {
                if (configKey.startsWith("gps.")) {
                    String key = configKey.substring("gps.".length()).toUpperCase();
                    Object value = configs.get(configKey);
                    if (DEBUG) {
                        Log.d(TAG, "Gps config: " + key + " = " + value);
                    }
                    if (value instanceof String) {
                        this.mProperties.setProperty(key, (String) value);
                    } else if (value != null) {
                        this.mProperties.setProperty(key, value.toString());
                    }
                }
            }
        }
    }

    private void loadPropertiesFromGpsDebugConfig(Properties properties) {
        try {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(new File(DEBUG_PROPERTIES_FILE));
                properties.load(stream);
            } finally {
                IoUtils.closeQuietly(stream);
            }
        } catch (IOException e) {
            if (DEBUG) {
                Log.d(TAG, "Could not open GPS configuration file /etc/gps_debug.conf");
            }
        }
    }

    private int getRangeCheckedConfigEsExtensionSec() {
        int emergencyExtensionSeconds = getIntConfig(CONFIG_ES_EXTENSION_SEC, 0);
        if (emergencyExtensionSeconds > 300) {
            Log.w(TAG, "ES_EXTENSION_SEC: " + emergencyExtensionSeconds + " too high, reset to 300");
            return 300;
        } else if (emergencyExtensionSeconds >= 0) {
            return emergencyExtensionSeconds;
        } else {
            Log.w(TAG, "ES_EXTENSION_SEC: " + emergencyExtensionSeconds + " is negative, reset to zero.");
            return 0;
        }
    }

    private int getIntConfig(String configParameter, int defaultValue) {
        String valueString = this.mProperties.getProperty(configParameter);
        if (TextUtils.isEmpty(valueString)) {
            return defaultValue;
        }
        try {
            return Integer.decode(valueString).intValue();
        } catch (NumberFormatException e) {
            Log.e(TAG, "Unable to parse config parameter " + configParameter + " value: " + valueString + ". Using default value: " + defaultValue);
            return defaultValue;
        }
    }

    private static boolean isConfigEsExtensionSecSupported(HalInterfaceVersion gnssConfiguartionIfaceVersion) {
        return gnssConfiguartionIfaceVersion.mMajor >= 2;
    }

    /* access modifiers changed from: private */
    public static boolean isConfigSuplEsSupported(HalInterfaceVersion gnssConfiguartionIfaceVersion) {
        return gnssConfiguartionIfaceVersion.mMajor < 2;
    }

    /* access modifiers changed from: private */
    public static boolean isConfigGpsLockSupported(HalInterfaceVersion gnssConfiguartionIfaceVersion) {
        return gnssConfiguartionIfaceVersion.mMajor < 2;
    }
}
