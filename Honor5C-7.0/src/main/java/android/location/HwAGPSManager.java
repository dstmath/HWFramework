package android.location;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.Log;

public class HwAGPSManager {
    public static final int AGPS_ENABLE_VALUE = 1;
    public static final int AGPS_MODE_DEFAULT_VALUE = 1;
    public static final int AGPS_ROAMING_DEFAULT_VALUE = 0;
    public static final int AGPS_ROAMING_ENABLED = 1;
    public static final int AGPS_ROAMING_UNENABLED = 0;
    public static final String AGPS_SERVICE_ADDRESS_DEFAULT_VALUE = "supl.google.com";
    public static final String AGPS_SERVICE_PORT_DEFAULT_VALUE = "7275";
    public static final int AGPS_SETTINGS_DEFAULT_VALUE = 1;
    public static final int AGPS_UNENABLE_VALUE = 0;
    private static final boolean DBG = false;
    public static final int GPSNI_REQUEST_DEFAULT_VALUE = 1;
    public static final int GPSNI_REQUEST_ENABLED = 1;
    public static final int GPSNI_REQUEST_UNENABLED = 0;
    public static final int GPSTIME_SYNC_DEFAULT_VALUE = 0;
    public static final int GPSTIME_SYNC_ENABLED = 1;
    public static final int GPSTIME_SYNC_UNENABLED = 0;
    private static final int GPS_COLD_START_AIDINGDATA = 13;
    public static final int GPS_COLD_START_MODE = 2;
    private static final int GPS_DELETE_ALL = 65535;
    private static final int GPS_DELETE_ALMANAC = 2;
    private static final int GPS_DELETE_CELLDB_INFO = 32768;
    private static final int GPS_DELETE_EPHEMERIS = 1;
    private static final int GPS_DELETE_HEALTH = 64;
    private static final int GPS_DELETE_IONO = 16;
    private static final int GPS_DELETE_POSITION = 4;
    private static final int GPS_DELETE_RTI = 1024;
    private static final int GPS_DELETE_SADATA = 512;
    private static final int GPS_DELETE_SVDIR = 128;
    private static final int GPS_DELETE_SVSTEER = 256;
    private static final int GPS_DELETE_TIME = 8;
    private static final int GPS_DELETE_UTC = 32;
    public static final int GPS_HOT_START_MODE = 0;
    public static final int GPS_POSITION_MODE_MS_ASSISTED = 2;
    public static final int GPS_POSITION_MODE_MS_BASED = 1;
    public static final int GPS_POSITION_MODE_STANDALONE = 0;
    public static final int GPS_START_MODE_DEFAULT_VALUE = 0;
    private static final int GPS_WARM_START_AIDINGDATA = 1;
    public static final int GPS_WARM_START_MODE = 1;
    protected static final boolean HWFLOW = false;
    private static final String KEY_AGPSMODE_SETTINGS = "assisted_gps_mode";
    private static final String KEY_AGPS_ROAMING_USED = "assisted_gps_roaming_enabled";
    private static final String KEY_AGPS_SERVICE_ADDRESS = "assisted_gps_service_IP";
    private static final String KEY_AGPS_SERVICE_PORT = "assisted_gps_service_port";
    private static final String KEY_AGPS_SWITCH_SETTINGS = "assisted_gps_enabled";
    private static final String KEY_GPS_START_MODE = "gps_start_mode";
    private static final String KEY_GPS_TIME_SYNC = "time_synchronization";
    private static final String KEY_NI_REQUEST = "gps_ni_request";
    private static final String KEY_QUICK_GPS = "quick_gps_switch";
    public static final int QUICKGPS_DEFAULT_VALUE = 1;
    public static final int QUICKGPS_ENABLED = 1;
    public static final int QUICKGPS_UNENABLED = 0;
    private static final String TAG = "HwAGPSManager";
    private Context mContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.location.HwAGPSManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.location.HwAGPSManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.location.HwAGPSManager.<clinit>():void");
    }

    public HwAGPSManager(Context context) {
        this.mContext = context;
    }

    public int getAGPSSwitchSettings() {
        return Global.getInt(this.mContext.getContentResolver(), KEY_AGPS_SWITCH_SETTINGS, QUICKGPS_ENABLED);
    }

    public void setAGPSSwitchSettings(int AGPSSettings) {
        Global.putInt(this.mContext.getContentResolver(), KEY_AGPS_SWITCH_SETTINGS, AGPSSettings);
    }

    public boolean isAgpsEnable() {
        return getAGPSSwitchSettings() != 0 ? true : HWFLOW;
    }

    public int getAGPSModeSettings() {
        return getSecureDataBaseInt(KEY_AGPSMODE_SETTINGS, QUICKGPS_ENABLED);
    }

    private int getSecureDataBaseInt(String key, int defaultValue) {
        int dataValue = System.getInt(this.mContext.getContentResolver(), key, defaultValue);
        if (DBG) {
            Log.d(TAG, " key=" + key + ", datavalue=" + dataValue + ", defaultValue" + defaultValue);
        }
        return dataValue;
    }

    public void setAGPSModeSettings(int AGPSModeSettings) {
        setSecureDataBaseInt(KEY_AGPSMODE_SETTINGS, AGPSModeSettings);
    }

    private void setSecureDataBaseInt(String key, int value) {
        if (DBG) {
            Log.d(TAG, " key=" + key + ", value=" + value);
        }
        System.putInt(this.mContext.getContentResolver(), key, value);
    }

    public int getAGPSRoamingEnable() {
        return getSecureDataBaseInt(KEY_AGPS_ROAMING_USED, QUICKGPS_UNENABLED);
    }

    public void setAGPSRoamingEnable(int AGPSRoamingEnable) {
        setSecureDataBaseInt(KEY_AGPS_ROAMING_USED, AGPSRoamingEnable);
    }

    public boolean isAgpsRoamingEnable() {
        return getAGPSRoamingEnable() == QUICKGPS_ENABLED ? true : HWFLOW;
    }

    public String getAGPSServiceAddress() {
        return getSecureDataBaseString(KEY_AGPS_SERVICE_ADDRESS, AGPS_SERVICE_ADDRESS_DEFAULT_VALUE);
    }

    private String getSecureDataBaseString(String key, String defaultValue) {
        String dataValue = System.getString(this.mContext.getContentResolver(), key);
        if (DBG) {
            Log.d(TAG, " key=" + key + ", datavalue=" + dataValue + ", defaultValue" + defaultValue);
        }
        if (dataValue != null) {
            return dataValue;
        }
        return defaultValue;
    }

    public void setAGPSServiceAddress(String AGPSServiceAddress) {
        setSecureDataBaseString(KEY_AGPS_SERVICE_ADDRESS, AGPSServiceAddress);
    }

    private void setSecureDataBaseString(String key, String value) {
        System.putString(this.mContext.getContentResolver(), key, value);
    }

    public String getAGPSServicePort() {
        return getSecureDataBaseString(KEY_AGPS_SERVICE_PORT, AGPS_SERVICE_PORT_DEFAULT_VALUE);
    }

    public void setAGPSServicePort(String AGPSServicePort) {
        setSecureDataBaseString(KEY_AGPS_SERVICE_PORT, AGPSServicePort);
    }

    public int getQuickGpsSettings() {
        return getDataBaseInt(KEY_QUICK_GPS, QUICKGPS_ENABLED);
    }

    public void setQuickGpsSettings(int QuickGpsSettings) {
        setDataBaseInt(KEY_QUICK_GPS, QuickGpsSettings);
    }

    public boolean isQuickGpsEnable() {
        return getDataBaseInt(KEY_QUICK_GPS, QUICKGPS_ENABLED) != 0 ? true : HWFLOW;
    }

    public int getGpsTimeSyncSettings() {
        return getSecureDataBaseInt(KEY_GPS_TIME_SYNC, QUICKGPS_UNENABLED);
    }

    public void setGpsTimeSyncSettings(int GpsTimeSyncSettings) {
        setSecureDataBaseInt(KEY_GPS_TIME_SYNC, GpsTimeSyncSettings);
    }

    public boolean isGpsTimeSyncEnable() {
        return getGpsTimeSyncSettings() == QUICKGPS_ENABLED ? true : HWFLOW;
    }

    public int getNiRequestSettings() {
        return getDataBaseInt(KEY_NI_REQUEST, QUICKGPS_ENABLED);
    }

    public void setNiRequestSettings(int NiRequestSettings) {
        setDataBaseInt(KEY_NI_REQUEST, NiRequestSettings);
    }

    public boolean isNiRequestEnable() {
        return getDataBaseInt(KEY_NI_REQUEST, QUICKGPS_ENABLED) != 0 ? true : HWFLOW;
    }

    public int getGpsStartModeSettings() {
        return getSecureDataBaseInt(KEY_GPS_START_MODE, QUICKGPS_UNENABLED);
    }

    public void setGpsStartModeSettings(int GpsStartSettings) {
        setSecureDataBaseInt(KEY_GPS_START_MODE, GpsStartSettings);
    }

    public int getAidingDataByStartMode() {
        int flag = QUICKGPS_UNENABLED;
        switch (getGpsStartModeSettings()) {
            case QUICKGPS_ENABLED /*1*/:
                flag = QUICKGPS_ENABLED;
                break;
            case GPS_POSITION_MODE_MS_ASSISTED /*2*/:
                flag = GPS_COLD_START_AIDINGDATA;
                break;
        }
        if (DBG) {
            Log.d(TAG, " aidingdata = " + flag);
        }
        return flag;
    }

    private void setDataBaseInt(String key, int value) {
        if (DBG) {
            Log.d(TAG, " key=" + key + ", value=" + value);
        }
        Global.putInt(this.mContext.getContentResolver(), key, value);
    }

    private int getDataBaseInt(String key, int defaultValue) {
        int dataValue = Global.getInt(this.mContext.getContentResolver(), key, defaultValue);
        if (DBG) {
            Log.d(TAG, " key=" + key + ", datavalue=" + dataValue + ", defaultValue" + defaultValue);
        }
        return dataValue;
    }

    private void setDataBaseString(String key, String value) {
        Global.putString(this.mContext.getContentResolver(), key, value);
    }

    private String getDataBaseString(String key, String defaultValue) {
        String dataValue = Global.getString(this.mContext.getContentResolver(), key);
        if (DBG) {
            Log.d(TAG, " key=" + key + ", datavalue=" + dataValue + ", defaultValue" + defaultValue);
        }
        if (dataValue != null) {
            return dataValue;
        }
        return defaultValue;
    }

    public void bootCompleteInit() {
    }

    public void setAgpsServer(Intent intent) {
    }
}
