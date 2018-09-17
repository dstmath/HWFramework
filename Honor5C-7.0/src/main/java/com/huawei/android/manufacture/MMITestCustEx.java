package com.huawei.android.manufacture;

import android.content.Context;
import com.huawei.android.util.NoExtAPIException;

public class MMITestCustEx {
    public static int getCapacitanceTestResult(Context context) {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getCompassTestResult() {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getSensorTestResult(int sensorType) {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getFingerprintTestResult(int testType) {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getGyroscopeTestResult() {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getSensorCalibrateResult(int sensorType) {
        throw new NoExtAPIException("method not supported.");
    }

    public static String getTestErrorInfo(String testItem) {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean setMMITestResult(int testType, boolean success) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void setMMITestState(boolean start) {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getDisplayDeviceTestResult(int testScene) {
        throw new NoExtAPIException("method not supported.");
    }
}
