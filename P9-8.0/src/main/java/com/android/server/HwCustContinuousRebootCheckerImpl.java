package com.android.server;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.util.Log;
import java.io.File;

public class HwCustContinuousRebootCheckerImpl extends HwCustContinuousRebootChecker {
    private static final String APANIC_DEV_NODE = "/dev/huawei_apanic";
    private static final boolean DEBUG = true;
    protected static boolean HWFLOW = true;
    protected static final boolean HWLOGW_E = true;
    private static final String PROP_ABNORMAL_REBOOT_TIMES = "persist.sys.hw_abnreboot_times";
    private static final String PROP_EMERGENCY_FROM_ABNREBOOT = "sys.huawei.emgc_from_abnreboot";
    private static final String PROP_EMERGENCY_MOUNTDATA = "sys.emergency.mountdata";
    private static final String PROP_FIRST_BOOT = "ro.runtime.firstboot";
    private static final String PROP_IS_ABNORMAL_REBOOT = "sys.huawei.abnormal.reboot";
    private static final int REBOOT_THRESHOLD = 5;
    private static final String TAG = "HwCustContinuousRebootCheckerImpl";
    private static final String TAG_FLOW = "HwCustContinuousRebootCheckerImpl_FLOW";
    private static final long TIME_THRESHOLD = 1800000;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private void LOG_TAG(String logs) {
        if (HWFLOW) {
            Log.e(TAG_FLOW, logs);
        }
    }

    public void checkAbnormalReboot() {
        if (isAbnormalReboot()) {
            LOG_TAG("isAbnormalReboot true");
            if (setAbnormalRebootTimes() < REBOOT_THRESHOLD) {
                cleanDataOnTimeOut();
                return;
            }
            Log.e(TAG, "Abnormal reboot happened 5 times, will start EmergencyData");
            makeEmergencyStart();
            clearData();
            return;
        }
        LOG_TAG("isAbnormalReboot false");
        clearData();
    }

    private boolean isAbnormalReboot() {
        if (isZygoteReboot() || (isEmergencyDataSettedInKernel() ^ 1) == 0 || !isHardwareResetReboot()) {
            return false;
        }
        return true;
    }

    private boolean isEmergencyDataSettedInKernel() {
        String emergencyDataProp = SystemProperties.get(PROP_EMERGENCY_MOUNTDATA);
        if ("1".equals(emergencyDataProp) || "2".equals(emergencyDataProp)) {
            return true;
        }
        return false;
    }

    private boolean isZygoteReboot() {
        if (SystemProperties.getLong(PROP_FIRST_BOOT, 0) == 0) {
            return false;
        }
        return true;
    }

    private boolean isHardwareResetReboot() {
        if (!"1".equals(SystemProperties.get(PROP_IS_ABNORMAL_REBOOT, "0"))) {
            return false;
        }
        SystemProperties.set(PROP_IS_ABNORMAL_REBOOT, "0");
        return true;
    }

    private boolean isApanicReboot() {
        if (new File(APANIC_DEV_NODE).exists()) {
            return true;
        }
        return false;
    }

    private int setAbnormalRebootTimes() {
        int rebootTimes = SystemProperties.getInt(PROP_ABNORMAL_REBOOT_TIMES, 0) + 1;
        LOG_TAG("reboot time is " + rebootTimes);
        SystemProperties.set(PROP_ABNORMAL_REBOOT_TIMES, String.valueOf(rebootTimes));
        return rebootTimes;
    }

    private void makeEmergencyStart() {
        SystemProperties.set(PROP_EMERGENCY_MOUNTDATA, "1");
        SystemProperties.set(PROP_EMERGENCY_FROM_ABNREBOOT, "true");
    }

    private void clearData() {
        SystemProperties.set(PROP_ABNORMAL_REBOOT_TIMES, "0");
    }

    private void cleanDataOnTimeOut() {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                HwCustContinuousRebootCheckerImpl.this.LOG_TAG("Call clearData in after booting 30 mins");
                HwCustContinuousRebootCheckerImpl.this.clearData();
            }
        }, TIME_THRESHOLD);
    }
}
