package com.huawei.android.manufacture;

import android.os.Binder;
import android.os.UserHandle;
import android.util.Log;

public class ManufactureNativeUtils {
    private static final String TAG = "ManufactureNativeUtils";
    private static final int VERSION_CODE = 100000;
    private static final String VERSION_NAME = "1.000.00";
    private static ManufactureNativeUtils instance;

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
            if (instance == null) {
                try {
                    Log.d(TAG, "load loadLibrary");
                    System.loadLibrary("manufacture_jni");
                    instance = new ManufactureNativeUtils();
                } catch (UnsatisfiedLinkError e) {
                    instance = null;
                    Log.e(TAG, "getInstance, load loadLibrary fail!");
                }
            }
        }
        return instance;
    }

    private static boolean isUidSystem() {
        return UserHandle.getAppId(Binder.getCallingUid()) == 1000 || Binder.getCallingUid() == 0;
    }

    public static int setPowerState(String testScene, String para) throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance2 = getInstance();
            if (instance2 != null) {
                int ret = instance2.native_setPowerState(testScene, para);
                Log.d(TAG, "setPowerState, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "setPowerState, instance is null");
            throw new Exception("not support!");
        }
        throw new SecurityException("disallowed call for uid " + Binder.getCallingUid());
    }

    public static String getDeviceInfo(int id) throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance2 = getInstance();
            if (instance2 != null) {
                return instance2.native_getDeviceInfo(id);
            }
            Log.e(TAG, "getDeviceInfo, instance is null");
            throw new Exception("not support!");
        }
        throw new SecurityException("disallowed call for uid " + Binder.getCallingUid());
    }

    public static String getTestResult(int testid) throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance2 = getInstance();
            if (instance2 != null) {
                String ret = instance2.native_getTestResult(testid);
                Log.d(TAG, "getTestResult, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "getTestResult, instance is null");
            throw new Exception("not support!");
        }
        throw new SecurityException("disallowed call for uid " + Binder.getCallingUid());
    }

    public static String getApparatusModel(int apparatusId) throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance2 = getInstance();
            if (instance2 != null) {
                String ret = instance2.native_getApparatusModel(apparatusId);
                Log.d(TAG, "getApparatusModel, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "getApparatusModel, instance is null");
            throw new Exception("not support!");
        }
        throw new SecurityException("disallowed call for uid " + Binder.getCallingUid());
    }

    public static String getVersionInfo(int id) throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance2 = getInstance();
            if (instance2 != null) {
                String ret = instance2.native_getVersionAndTime(id);
                Log.d(TAG, "getVersionInfo, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "getVersionInfo, instance is null");
            throw new Exception("not support!");
        }
        throw new SecurityException("disallowed call for uid " + Binder.getCallingUid());
    }

    public static int getNVBackupResult() throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance2 = getInstance();
            if (instance2 != null) {
                int ret = instance2.native_getNVBackupResult();
                Log.d(TAG, "getNVBackupResult, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "getNVBackupResult, instance is null");
            throw new Exception("not support!");
        }
        throw new SecurityException("disallowed call for uid " + Binder.getCallingUid());
    }

    public static int getBackgroundDebugMode() throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance2 = getInstance();
            if (instance2 != null) {
                int ret = instance2.native_getBackgroundDebugMode();
                Log.d(TAG, "getBackgroundDebugMode, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "getBackgroundDebugMode, instance is null");
            throw new Exception("not support!");
        }
        throw new SecurityException("disallowed call for uid " + Binder.getCallingUid());
    }

    public static int setBackgroundDebugMode(int mode, String password) throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance2 = getInstance();
            if (instance2 != null) {
                int ret = instance2.native_setBackgroundDebugMode(mode, password);
                Log.d(TAG, "setBackgroundDebugMode, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "setBackgroundDebugMode, instance is null");
            throw new Exception("not support!");
        }
        throw new SecurityException("disallowed call for uid " + Binder.getCallingUid());
    }

    public static int getFuseState() throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance2 = getInstance();
            if (instance2 != null) {
                int ret = instance2.native_getFuseState();
                Log.d(TAG, "getFuseState, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "getFuseState, instance is null");
            throw new Exception("not support!");
        }
        throw new SecurityException("disallowed call for uid " + Binder.getCallingUid());
    }

    public static int verifySecbootKey(String key) throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance2 = getInstance();
            if (instance2 != null) {
                int ret = instance2.native_verifySecbootKey(key);
                Log.d(TAG, "verifySecbootKey, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "verifySecbootKey, instance is null");
            throw new Exception("not support!");
        }
        throw new SecurityException("disallowed call for uid " + Binder.getCallingUid());
    }

    public static String getSIMLockInfo() throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance2 = getInstance();
            if (instance2 != null) {
                String ret = instance2.native_getSIMLockDetail();
                Log.d(TAG, "getSIMLockInfo, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "getSIMLockInfo, instance is null");
            throw new Exception("not support!");
        }
        throw new SecurityException("disallowed call for uid " + Binder.getCallingUid());
    }

    public static String getPLMNInfo(int id) throws Exception {
        if (isUidSystem()) {
            ManufactureNativeUtils instance2 = getInstance();
            if (instance2 != null) {
                String ret = instance2.native_getBoundPLMNInfo(id);
                Log.d(TAG, "getPLMNInfo, ret = " + ret);
                return ret;
            }
            Log.e(TAG, "getPLMNInfo, instance is null");
            throw new Exception("not support!");
        }
        throw new SecurityException("disallowed call for uid " + Binder.getCallingUid());
    }
}
