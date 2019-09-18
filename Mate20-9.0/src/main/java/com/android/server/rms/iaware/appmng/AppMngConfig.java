package com.android.server.rms.iaware.appmng;

import com.android.internal.util.MemInfoReader;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;

public final class AppMngConfig {
    private static final String APPMNG = "AppManagement";
    private static final int DOMESTIC = -1;
    private static final String HABIT_FILTER_LIST = "HabitFilterList";
    private static final int OVER_SEA = 1;
    private static final String TAG = "AppMngConfig";
    private static boolean sAbroadFlag = false;
    private static int sAdjCustTopN = 0;
    private static boolean sAlarmChk = false;
    private static long sBgDecay = 0;
    private static int sImCnt = 10000;
    private static long sKeySysDecay = HwArbitrationDEFS.DelayTimeMillisB;
    private static boolean sKillMore = false;
    private static long sMemMB = 0;
    private static boolean sPgProtect = false;
    private static boolean sRestartFlag = true;
    private static int sScreenChanged = 30;
    private static int sSmartCleanInterval = 600;
    private static long sSysDecay = AppHibernateCst.DELAY_ONE_MINS;
    private static int sTopN = 6;

    public static int getTopN() {
        return sTopN;
    }

    public static int getImCnt() {
        return sImCnt;
    }

    public static long getSysDecay() {
        return sSysDecay;
    }

    public static long getKeySysDecay() {
        return sKeySysDecay;
    }

    public static boolean getRestartFlag() {
        return sRestartFlag;
    }

    public static int getAdjCustTopN() {
        return sAdjCustTopN;
    }

    public static long getBgDecay() {
        return sBgDecay;
    }

    public static boolean getPgProtectFlag() {
        return sPgProtect;
    }

    public static boolean getAlarmCheckFlag() {
        return sAlarmChk;
    }

    public static boolean getAbroadFlag() {
        return sAbroadFlag;
    }

    public static void setTopN(int value) {
        sTopN = value;
    }

    public static void setImCnt(int value) {
        sImCnt = value;
    }

    public static void setSysDecay(long value) {
        sSysDecay = value;
    }

    public static void setKeySysDecay(long value) {
        sKeySysDecay = value;
    }

    public static long getMemorySize() {
        return sMemMB;
    }

    public static void setRestartFlag(boolean restartFlag) {
        sRestartFlag = restartFlag;
    }

    public static void setAdjCustTopN(int topN) {
        sAdjCustTopN = topN;
    }

    public static void setBgDecay(long decay) {
        sBgDecay = decay;
    }

    public static void setPgProtectFlag(boolean flag) {
        sPgProtect = flag;
    }

    public static void setAlarmChkFlag(boolean flag) {
        sAlarmChk = flag;
    }

    public static void setAbroadFlag(boolean flag) {
        sAbroadFlag = flag;
    }

    public static void setKillMoreFlag(boolean killMore) {
        sKillMore = killMore;
    }

    public static boolean getKillMoreFlag() {
        return sKillMore;
    }

    public static void setScreenChangedThreshold(int changedThreshold) {
        if (changedThreshold >= 0) {
            sScreenChanged = changedThreshold;
        }
    }

    public static int getScreenChangedThreshold() {
        return sScreenChanged;
    }

    public static void setSmartCleanInterval(int interval) {
        if (interval >= 0) {
            sSmartCleanInterval = interval;
        }
    }

    public static int getSmartCleanInterval() {
        return sSmartCleanInterval * 1000;
    }

    public static void init() {
        MemInfoReader minfo = new MemInfoReader();
        minfo.readMemInfo();
        sMemMB = minfo.getTotalSize() / MemoryConstant.MB_SIZE;
    }

    public static int getRegionCode() {
        return sAbroadFlag ? 1 : -1;
    }
}
