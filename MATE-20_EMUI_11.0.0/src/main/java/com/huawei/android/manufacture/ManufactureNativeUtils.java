package com.huawei.android.manufacture;

import android.os.Binder;
import android.os.UserHandle;
import android.util.Log;

public class ManufactureNativeUtils {
    private static final String NOT_ALLOW = "disallowed call for uid ";
    private static final String NOT_SUPPORT = "not support!";
    private static final String TAG = "ManufactureNativeUtils";
    private static final int VERSION_CODE = 100000;
    private static final String VERSION_NAME = "1.000.00";
    private static ManufactureNativeUtils sSingleton;

    private native String native_getApparatusModel(int i);

    private native int native_getBackgroundDebugMode();

    private native String native_getBoundPLMNInfo(int i);

    private native String native_getDeviceInfo(int i);

    private native int native_getFuseState();

    private native int native_getNVBackupResult();

    private native String native_getSIMLockDetail();

    private native String native_getTestResult(int i);

    private native String native_getVersionAndTime(int i);

    private native int native_setBackgroundDebugMode(int i, String str);

    private native int native_setPowerState(String str, String str2);

    private native int native_verifySecbootKey(String str);

    private ManufactureNativeUtils() {
        Log.i(TAG, "ManufactureNativeUtils, VERSION_NAME: 1.000.00 VERSION_CODE: 100000");
    }

    private static ManufactureNativeUtils getInstance() {
        synchronized (ManufactureNativeUtils.class) {
            if (sSingleton == null) {
                try {
                    Log.d(TAG, "load loadLibrary");
                    System.loadLibrary("manufacture_jni");
                    sSingleton = new ManufactureNativeUtils();
                } catch (UnsatisfiedLinkError e) {
                    sSingleton = null;
                    Log.e(TAG, "getInstance, load loadLibrary fail!");
                }
            }
        }
        return sSingleton;
    }

    private static boolean isUidSystem() {
        return UserHandle.getAppId(Binder.getCallingUid()) == 1000 || Binder.getCallingUid() == 0;
    }

    public static int setPowerState(String testScene, String param) throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance = getInstance();
            if (instance != null) {
                int ret = instance.native_setPowerState(testScene, param);
                Log.d(TAG, "setPowerState, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "setPowerState, not support!");
            throw new Exception(NOT_SUPPORT);
        }
        throw new SecurityException(NOT_ALLOW + Binder.getCallingUid());
    }

    public static String getDeviceInfo(int id) throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance = getInstance();
            if (instance != null) {
                return instance.native_getDeviceInfo(id);
            }
            Log.e(TAG, "getDeviceInfo, not support!");
            throw new Exception(NOT_SUPPORT);
        }
        throw new SecurityException(NOT_ALLOW + Binder.getCallingUid());
    }

    public static String getTestResult(int testId) throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance = getInstance();
            if (instance != null) {
                String ret = instance.native_getTestResult(testId);
                Log.d(TAG, "getTestResult, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "getTestResult, not support!");
            throw new Exception(NOT_SUPPORT);
        }
        throw new SecurityException(NOT_ALLOW + Binder.getCallingUid());
    }

    public static String getApparatusModel(int apparatusId) throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance = getInstance();
            if (instance != null) {
                String ret = instance.native_getApparatusModel(apparatusId);
                Log.d(TAG, "getApparatusModel, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "getApparatusModel, not support!");
            throw new Exception(NOT_SUPPORT);
        }
        throw new SecurityException(NOT_ALLOW + Binder.getCallingUid());
    }

    public static String getVersionInfo(int id) throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance = getInstance();
            if (instance != null) {
                String ret = instance.native_getVersionAndTime(id);
                Log.d(TAG, "getVersionInfo, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "getVersionInfo, not support!");
            throw new Exception(NOT_SUPPORT);
        }
        throw new SecurityException(NOT_ALLOW + Binder.getCallingUid());
    }

    public static int getNVBackupResult() throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance = getInstance();
            if (instance != null) {
                int ret = instance.native_getNVBackupResult();
                Log.d(TAG, "getNVBackupResult, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "getNVBackupResult, not support!");
            throw new Exception(NOT_SUPPORT);
        }
        throw new SecurityException(NOT_ALLOW + Binder.getCallingUid());
    }

    public static int getBackgroundDebugMode() throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance = getInstance();
            if (instance != null) {
                int ret = instance.native_getBackgroundDebugMode();
                Log.d(TAG, "getBackgroundDebugMode, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "getBackgroundDebugMode, not support!");
            throw new Exception(NOT_SUPPORT);
        }
        throw new SecurityException(NOT_ALLOW + Binder.getCallingUid());
    }

    public static int setBackgroundDebugMode(int mode, String password) throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance = getInstance();
            if (instance != null) {
                int ret = instance.native_setBackgroundDebugMode(mode, password);
                Log.d(TAG, "setBackgroundDebugMode, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "setBackgroundDebugMode, not support!");
            throw new Exception(NOT_SUPPORT);
        }
        throw new SecurityException(NOT_ALLOW + Binder.getCallingUid());
    }

    public static int getFuseState() throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance = getInstance();
            if (instance != null) {
                int ret = instance.native_getFuseState();
                Log.d(TAG, "getFuseState, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "getFuseState, not support!");
            throw new Exception(NOT_SUPPORT);
        }
        throw new SecurityException(NOT_ALLOW + Binder.getCallingUid());
    }

    public static int verifySecbootKey(String key) throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance = getInstance();
            if (instance != null) {
                int ret = instance.native_verifySecbootKey(key);
                Log.d(TAG, "verifySecbootKey, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "verifySecbootKey, not support!");
            throw new Exception(NOT_SUPPORT);
        }
        throw new SecurityException(NOT_ALLOW + Binder.getCallingUid());
    }

    public static String getSIMLockInfo() throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance = getInstance();
            if (instance != null) {
                String ret = instance.native_getSIMLockDetail();
                Log.d(TAG, "getSIMLockInfo, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "getSIMLockInfo, not support!");
            throw new Exception(NOT_SUPPORT);
        }
        throw new SecurityException(NOT_ALLOW + Binder.getCallingUid());
    }

    public static String getPLMNInfo(int id) throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance = getInstance();
            if (instance != null) {
                String ret = instance.native_getBoundPLMNInfo(id);
                Log.d(TAG, "getPLMNInfo, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "getPLMNInfo, not support!");
            throw new Exception(NOT_SUPPORT);
        }
        throw new SecurityException(NOT_ALLOW + Binder.getCallingUid());
    }
}
