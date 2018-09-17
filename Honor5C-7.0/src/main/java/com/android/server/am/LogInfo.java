package com.android.server.am;

import java.util.ArrayList;
import java.util.List;

/* compiled from: HwPowerInfoService */
class LogInfo {
    String mAlarmName;
    int mBTState;
    int mBatteryLevel;
    String mBattery_temp;
    String mBoardTemp;
    int mBrightness;
    String mCPU0Freq;
    String mCPU0Freq_Max;
    String mCPU0Temp;
    String mCPU1Temp;
    String mCPU4Freq;
    String mCPU4Freq_Max;
    String mCPUOnLine;
    int mCameraState;
    int mChargeStatus;
    int mConnectionStatus;
    List<CpuTopLoad> mCpuTopLoads;
    int mCpuTotalLoad;
    String mCurrent;
    String mCurrentLimit;
    int mDataConnection;
    int mGPSStatus;
    int mHeadSet;
    int mNFCOn;
    String mPA_temp;
    String mSOC_rm;
    int mSignalStrength;
    String mTime;
    String mTopAppName;
    int mTopAppUID;
    List<WakeLock> mWakeLocks;
    String mWakeupReason;
    List<WakeupSource> mWakeupSources;
    int mWifiStatus;

    LogInfo() {
        this.mCurrent = null;
        this.mCPU0Freq = null;
        this.mCPU4Freq = null;
        this.mCPU0Temp = null;
        this.mCPU1Temp = null;
        this.mGPSStatus = 0;
        this.mTopAppUID = 0;
        this.mBoardTemp = null;
        this.mWifiStatus = 0;
        this.mBrightness = 0;
        this.mChargeStatus = 0;
        this.mBatteryLevel = 0;
        this.mDataConnection = 0;
        this.mSignalStrength = 0;
        this.mConnectionStatus = 0;
        this.mHeadSet = 0;
        this.mNFCOn = 0;
        this.mBTState = 0;
        this.mCameraState = 0;
        this.mCpuTotalLoad = 0;
        this.mTime = null;
        this.mAlarmName = null;
        this.mCPUOnLine = null;
        this.mTopAppName = null;
        this.mWakeupReason = null;
        this.mWakeLocks = new ArrayList();
        this.mWakeupSources = new ArrayList();
        this.mCpuTopLoads = new ArrayList();
        this.mCPU0Freq_Max = null;
        this.mCPU4Freq_Max = null;
        this.mCurrentLimit = null;
        this.mSOC_rm = null;
        this.mPA_temp = null;
        this.mBattery_temp = null;
    }
}
