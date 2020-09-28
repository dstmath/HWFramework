package com.huawei.android.os;

import android.os.IDeviceIdentifiersPolicyService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.AndroidRuntimeException;
import android.util.Log;

public class BuildEx {
    public static final String EMUI_VERSION = SystemProperties.get("ro.build.version.emui", "");
    private static final String TAG = "BuildEx";
    private static final String UDID_EXCEPTION = "AndroidRuntimeException";

    public static class VERSION_CODES {
        public static final int CUR_DEVELOPMENT = 10000;
        public static final int EMUI_10_0 = 21;
        public static final int EMUI_10_0_1 = 22;
        public static final int EMUI_10_1 = 23;
        public static final int EMUI_10_1_1 = 24;
        public static final int EMUI_1_0 = 1;
        public static final int EMUI_1_5 = 2;
        public static final int EMUI_1_6 = 3;
        public static final int EMUI_2_0_JB = 4;
        public static final int EMUI_2_0_KK = 5;
        public static final int EMUI_2_3 = 6;
        public static final int EMUI_3_0 = 7;
        public static final int EMUI_3_0_5 = 8;
        public static final int EMUI_3_1 = 8;
        public static final int EMUI_4_0 = 9;
        public static final int EMUI_4_1 = 10;
        public static final int EMUI_5_0 = 11;
        public static final int EMUI_5_1 = 12;
        public static final int EMUI_5_1_b10x = 13;
        public static final int EMUI_5_1_b200 = 13;
        public static final int EMUI_6_0 = 14;
        public static final int EMUI_8_0_1 = 15;
        public static final int EMUI_8_2 = 16;
        public static final int EMUI_9_0 = 17;
        public static final int EMUI_9_0_1 = 18;
        public static final int EMUI_9_1 = 19;
        public static final int EMUI_9_1_1 = 20;
        public static final int EMUI_SUB_SDK_VERSION = 2;
        public static final int UNKNOWN_EMUI = 0;
    }

    private BuildEx() {
    }

    public static String getUDID() {
        try {
            String udid = IDeviceIdentifiersPolicyService.Stub.asInterface(ServiceManager.getService("device_identifiers")).getUDID();
            if (!UDID_EXCEPTION.equals(udid)) {
                return udid;
            }
            throw new AndroidRuntimeException("read udid failed!");
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException happens in getUDID! " + e.getMessage());
            return null;
        }
    }

    public static String getSecondaryUDID() {
        try {
            String udid = IDeviceIdentifiersPolicyService.Stub.asInterface(ServiceManager.getService("device_identifiers")).getSecondaryUDID();
            if (!UDID_EXCEPTION.equals(udid)) {
                return udid;
            }
            throw new AndroidRuntimeException("read udid failed!");
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException happens in getUDID! " + e.getMessage());
            return null;
        }
    }

    public static class VERSION {
        public static final int EMUI_SDK_INT = SystemProperties.getInt("ro.build.hw_emui_api_level", 0);
        public static final int EMUI_SUB_SDK_INT = getEmuiSubSdkVerison();

        private static int getEmuiSubSdkVerison() {
            return 2;
        }
    }
}
