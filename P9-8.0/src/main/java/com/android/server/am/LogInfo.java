package com.android.server.am;

import java.util.ArrayList;
import java.util.List;

/* compiled from: HwPowerInfoService */
class LogInfo {
    String mAlarmName = null;
    int mBTState = 0;
    int mBatteryLevel = 0;
    String mBattery_temp = null;
    String mBoardTemp = null;
    int mBrightness = 0;
    String mCPU0Freq = null;
    String mCPU0Freq_Max = null;
    String mCPU0Temp = null;
    String mCPU1Temp = null;
    String mCPU4Freq = null;
    String mCPU4Freq_Max = null;
    String mCPUOnLine = null;
    int mCameraState = 0;
    int mChargeStatus = 0;
    int mConnectionStatus = 0;
    List<CpuTopLoad> mCpuTopLoads = new ArrayList();
    int mCpuTotalLoad = 0;
    String mCurrent = null;
    String mCurrentLimit = null;
    int mDataConnection = 0;
    int mGPSStatus = 0;
    int mHeadSet = 0;
    int mNFCOn = 0;
    String mPA_temp = null;
    String mSOC_rm = null;
    int mSignalStrength = 0;
    String mTime = null;
    String mTopAppName = null;
    int mTopAppUID = 0;
    List<WakeLock> mWakeLocks = new ArrayList();
    String mWakeupReason = null;
    List<WakeupSource> mWakeupSources = new ArrayList();
    int mWifiStatus = 0;

    LogInfo() {
    }
}
