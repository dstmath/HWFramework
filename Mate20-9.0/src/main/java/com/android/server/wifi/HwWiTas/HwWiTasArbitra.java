package com.android.server.wifi.HwWiTas;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.android.server.wifi.HwSelfCureUtils;
import com.android.server.wifi.WifiNative;

public class HwWiTasArbitra {
    private static String mIfname = "wlan0";
    private static HwWiTasArbitra mWiTasArbitra = null;
    private int mCurAntIndex = 0;
    private int mFreezeTime = 0;
    private int mGameDelay = 0;
    private int mGameId = 0;
    private boolean mIsInTestMode = false;
    private boolean mIsInWhiteList = false;
    private int mMeasureState = 0;
    private HwWiTasChr mWiTasChr;
    private HwWiTasTest mWiTasTest;
    private WifiManager mWifiManager;
    private WifiNative mWifiNative;

    protected static synchronized HwWiTasArbitra getInstance(WifiManager wifiManager, WifiNative wifiNative) {
        HwWiTasArbitra hwWiTasArbitra;
        synchronized (HwWiTasArbitra.class) {
            if (mWiTasArbitra == null) {
                mWiTasArbitra = new HwWiTasArbitra(wifiManager, wifiNative);
            }
            hwWiTasArbitra = mWiTasArbitra;
        }
        return hwWiTasArbitra;
    }

    /* access modifiers changed from: protected */
    public void checkAndSetTestMode() {
        this.mIsInTestMode = this.mWiTasTest.getTestModeCfg();
    }

    /* access modifiers changed from: protected */
    public boolean isTestMode() {
        return this.mIsInTestMode;
    }

    /* access modifiers changed from: protected */
    public int getTimeoutThr() {
        if (this.mIsInTestMode) {
            return this.mWiTasTest.getTimeoutThreshold();
        }
        return HwSelfCureUtils.SELFCURE_WIFI_ON_TIMEOUT;
    }

    /* access modifiers changed from: protected */
    public void setGameId(int id) {
        this.mGameId = id;
    }

    /* access modifiers changed from: protected */
    public int getGameId() {
        return this.mGameId;
    }

    /* access modifiers changed from: protected */
    public void setGameDelay(int delay) {
        this.mGameDelay = delay;
    }

    /* access modifiers changed from: protected */
    public int getGameDelay() {
        return this.mGameDelay;
    }

    /* access modifiers changed from: protected */
    public void updateGameWhiteListInfo(boolean inWhiteList) {
        this.mIsInWhiteList = inWhiteList;
    }

    /* access modifiers changed from: protected */
    public boolean isGameInWhiteList() {
        return this.mIsInWhiteList;
    }

    /* access modifiers changed from: protected */
    public boolean isGameLag(int delay) {
        int gameDelayThreshold;
        if (this.mIsInTestMode) {
            gameDelayThreshold = this.mWiTasTest.getGameThreshold();
        } else {
            gameDelayThreshold = 200;
        }
        if (delay >= gameDelayThreshold) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void setFreezeTime(int time) {
        this.mFreezeTime = time;
    }

    /* access modifiers changed from: protected */
    public int getFreezeTime() {
        return this.mFreezeTime;
    }

    /* access modifiers changed from: protected */
    public void setMeasureState(int state) {
        this.mMeasureState = state;
    }

    /* access modifiers changed from: protected */
    public int getMeasureState() {
        return this.mMeasureState;
    }

    /* access modifiers changed from: protected */
    public int getCurAntIndex() {
        return this.mCurAntIndex;
    }

    /* access modifiers changed from: protected */
    public int getCurRssiInfo() {
        int rssi = 0;
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            rssi = wifiInfo.getRssi();
        }
        if (this.mIsInTestMode) {
            Log.d(HwWiTasUtils.TAG, "current rssi: " + rssi);
        }
        return rssi;
    }

    /* access modifiers changed from: protected */
    public boolean isRssiBad(int rssi) {
        int rssiThreshold;
        if (this.mIsInTestMode) {
            rssiThreshold = this.mWiTasTest.getRssiThreshold();
        } else {
            rssiThreshold = -65;
        }
        if (rssi < rssiThreshold) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isRssiUnSteady(int currentRssi, int recordRssi) {
        int rssiMaxFluctuation;
        if (this.mIsInTestMode) {
            rssiMaxFluctuation = this.mWiTasTest.getRssiMaxFluctuation();
        } else {
            rssiMaxFluctuation = 6;
        }
        if (currentRssi == 0 || recordRssi == 0 || !isRssiBad(currentRssi) || recordRssi - currentRssi <= rssiMaxFluctuation) {
            return false;
        }
        Log.d(HwWiTasUtils.TAG, "rssi not steady, currentRssi: " + currentRssi + ", recordRssi: " + recordRssi);
        return true;
    }

    /* access modifiers changed from: protected */
    public int switchAntenna(int antIndex) {
        byte[] buff = new byte[1];
        Log.d(HwWiTasUtils.TAG, "switch antenna, from index: " + this.mCurAntIndex + " to index: " + antIndex);
        if (checkAntenna(this.mCurAntIndex) == 0 && this.mCurAntIndex == antIndex) {
            Log.e(HwWiTasUtils.TAG, "do not need do swtich operation");
            return 0;
        }
        this.mWiTasChr.increaseSwitchCnt();
        if (antIndex == 0) {
            int ret = adjustTxPower(antIndex);
            if (ret != 0) {
                return ret;
            }
        }
        buff[0] = (byte) antIndex;
        if (this.mWifiNative.sendCmdToDriver(mIfname, 104, buff) < 0) {
            Log.e(HwWiTasUtils.TAG, "switch rssi command error");
            this.mWiTasChr.recordErrCode(4);
            return -1;
        } else if (checkAntenna(antIndex) != 0) {
            Log.e(HwWiTasUtils.TAG, "the ant index is not match current ant index");
            this.mWiTasChr.recordErrCode(5);
            return -1;
        } else {
            this.mCurAntIndex = antIndex;
            if (antIndex == 1) {
                int ret2 = adjustTxPower(antIndex);
                if (ret2 != 0) {
                    return ret2;
                }
            }
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public int measureAntenna() {
        Log.d(HwWiTasUtils.TAG, "measure antenna, index: " + this.mCurAntIndex);
        if (this.mWifiNative.sendCmdToDriver(mIfname, 102, new byte[]{0}) >= 0) {
            return 0;
        }
        Log.e(HwWiTasUtils.TAG, "measure rssi command error");
        this.mWiTasChr.recordErrCode(1);
        return -1;
    }

    /* access modifiers changed from: protected */
    public int chooseAntenna(int srcAntRssi, int dstAntRssi) {
        int longFreezeTime;
        int shortFreezeTime;
        int highRssiThreshold;
        int lowRssiThreshold;
        int srcAntIndex = 1 - this.mCurAntIndex;
        int dstAntIndex = this.mCurAntIndex;
        Log.d(HwWiTasUtils.TAG, "srcAntIndex: " + srcAntIndex + ", dstAntIndex: " + dstAntIndex);
        Log.d(HwWiTasUtils.TAG, "srcAntRssi: " + srcAntRssi + ", dstAntRssi: " + dstAntRssi);
        if (this.mIsInTestMode) {
            lowRssiThreshold = this.mWiTasTest.getRssiLow();
            highRssiThreshold = this.mWiTasTest.getRssiHigh();
            shortFreezeTime = this.mWiTasTest.getFreezeShort();
            longFreezeTime = this.mWiTasTest.getFreezeLong();
        } else {
            lowRssiThreshold = 1;
            highRssiThreshold = 4;
            shortFreezeTime = 15000;
            longFreezeTime = 30000;
        }
        int dValue = Math.abs(dstAntRssi - srcAntRssi);
        if (dValue < lowRssiThreshold) {
            this.mFreezeTime = shortFreezeTime;
        } else if (dValue < lowRssiThreshold || dValue >= highRssiThreshold) {
            this.mFreezeTime = longFreezeTime;
        } else {
            this.mFreezeTime = shortFreezeTime;
        }
        if (srcAntRssi <= dstAntRssi || dValue < lowRssiThreshold) {
            if (dstAntIndex == 0) {
                this.mWiTasChr.increaseDefaultAntCnt();
            } else {
                this.mWiTasChr.increaseTasAntCnt();
            }
            Log.d(HwWiTasUtils.TAG, "choose dst ant: " + dstAntIndex);
            this.mWiTasChr.recordState(4);
            return 0;
        }
        Log.d(HwWiTasUtils.TAG, "choose src ant: " + srcAntIndex);
        this.mWiTasChr.recordState(3);
        if (srcAntIndex == 0) {
            this.mWiTasChr.increaseDefaultAntCnt();
        } else {
            this.mWiTasChr.increaseTasAntCnt();
        }
        return switchAntenna(srcAntIndex);
    }

    private HwWiTasArbitra(WifiManager wifiManager, WifiNative wifiNative) {
        Log.d(HwWiTasUtils.TAG, "init HwWiTasArbitra");
        this.mWifiManager = wifiManager;
        this.mWifiNative = wifiNative;
        this.mWiTasTest = HwWiTasTest.getInstance();
        this.mWiTasChr = HwWiTasChr.getInstance(this.mWifiManager, wifiNative);
    }

    private int checkAntenna(int antIndex) {
        int index = this.mWifiNative.sendCmdToDriver(mIfname, HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO, new byte[1]);
        Log.d(HwWiTasUtils.TAG, "check antenna, current index: " + antIndex + ", check index: " + index);
        if (index != antIndex) {
            return -1;
        }
        return 0;
    }

    private int adjustTxPower(int antIndex) {
        byte increaseTxPower = 0;
        byte[] buff = new byte[2];
        Log.d(HwWiTasUtils.TAG, "adjust tx power, index: " + antIndex);
        if (antIndex == 1) {
            increaseTxPower = 1;
        }
        buff[0] = 0;
        buff[1] = increaseTxPower;
        if (this.mWifiNative.sendCmdToDriver(mIfname, 103, buff) >= 0) {
            return 0;
        }
        Log.e(HwWiTasUtils.TAG, "adjust txpower command error");
        this.mWiTasChr.recordErrCode(6);
        return -1;
    }
}
