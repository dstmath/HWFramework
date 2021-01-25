package com.android.server.wifi.HwWiTas;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.android.server.wifi.WifiInjector;
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
        return 3000;
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
            gameDelayThreshold = HwQoEService.KOG_LATENCY_TIME_THRESHOLD;
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
            HwHiLog.d(HwWiTasUtils.TAG, false, "current rssi: %{public}d", new Object[]{Integer.valueOf(rssi)});
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
        HwHiLog.d(HwWiTasUtils.TAG, false, "rssi not steady, currentRssi: %{public}d, recordRssi: %{public}d", new Object[]{Integer.valueOf(currentRssi), Integer.valueOf(recordRssi)});
        return true;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0043: APUT  
      (r3v0 'buff' byte[] A[D('buff' byte[]), IMMUTABLE_TYPE])
      (0 ??[int, short, byte, char])
      (wrap: byte : 0x0042: CAST (r4v6 byte A[IMMUTABLE_TYPE]) = (byte) (r9v0 'antIndex' int A[D('antIndex' int)]))
     */
    /* access modifiers changed from: protected */
    public int switchAntenna(int antIndex) {
        int ret;
        int ret2;
        byte[] buff = new byte[1];
        HwHiLog.d(HwWiTasUtils.TAG, false, "switch antenna, from index: %{public}d to index: %{public}d", new Object[]{Integer.valueOf(this.mCurAntIndex), Integer.valueOf(antIndex)});
        if (checkAntenna(this.mCurAntIndex) == 0 && this.mCurAntIndex == antIndex) {
            HwHiLog.e(HwWiTasUtils.TAG, false, "do not need do swtich operation", new Object[0]);
            return 0;
        }
        this.mWiTasChr.increaseSwitchCnt();
        if (antIndex == HwWiTasUtils.mDefaultAntIndex && (ret2 = adjustTxPower(antIndex)) != 0) {
            return ret2;
        }
        buff[0] = (byte) antIndex;
        if (WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.sendCmdToDriver(mIfname, (int) HwQoEUtils.QOE_MSG_MONITOR_NO_INTERNET, buff) < 0) {
            HwHiLog.e(HwWiTasUtils.TAG, false, "switch antenna command error", new Object[0]);
            this.mWiTasChr.recordErrCode(4);
            return -1;
        } else if (checkAntenna(antIndex) != 0) {
            HwHiLog.e(HwWiTasUtils.TAG, false, "the ant index is not match current ant index", new Object[0]);
            this.mWiTasChr.recordErrCode(5);
            return -1;
        } else {
            this.mCurAntIndex = antIndex;
            if (antIndex != HwWiTasUtils.mWitasAntIndex || (ret = adjustTxPower(antIndex)) == 0) {
                return 0;
            }
            return ret;
        }
    }

    /* access modifiers changed from: protected */
    public int measureAntenna() {
        HwHiLog.d(HwWiTasUtils.TAG, false, "measure antenna, index: %{public}d", new Object[]{Integer.valueOf(this.mCurAntIndex)});
        if (WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.sendCmdToDriver(mIfname, 102, new byte[]{(byte) HwWiTasUtils.mCoreAntIndex}) >= 0) {
            return 0;
        }
        HwHiLog.e(HwWiTasUtils.TAG, false, "measure rssi command error", new Object[0]);
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
        HwHiLog.d(HwWiTasUtils.TAG, false, "srcAntIndex: %{public}d, dstAntIndex: %{public}d", new Object[]{Integer.valueOf(srcAntIndex), Integer.valueOf(dstAntIndex)});
        HwHiLog.d(HwWiTasUtils.TAG, false, "srcAntRssi: %{public}d, dstAntRssi: %{public}d", new Object[]{Integer.valueOf(srcAntRssi), Integer.valueOf(dstAntRssi)});
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
            if (dstAntIndex == HwWiTasUtils.mDefaultAntIndex) {
                this.mWiTasChr.increaseDefaultAntCnt();
            } else {
                this.mWiTasChr.increaseTasAntCnt();
            }
            HwHiLog.d(HwWiTasUtils.TAG, false, "choose dst ant: %{public}d", new Object[]{Integer.valueOf(dstAntIndex)});
            this.mWiTasChr.recordState(4);
            return 0;
        }
        HwHiLog.d(HwWiTasUtils.TAG, false, "choose src ant: %{public}d", new Object[]{Integer.valueOf(srcAntIndex)});
        this.mWiTasChr.recordState(3);
        if (srcAntIndex == HwWiTasUtils.mDefaultAntIndex) {
            this.mWiTasChr.increaseDefaultAntCnt();
        } else {
            this.mWiTasChr.increaseTasAntCnt();
        }
        return switchAntenna(srcAntIndex);
    }

    private HwWiTasArbitra(WifiManager wifiManager, WifiNative wifiNative) {
        HwHiLog.d(HwWiTasUtils.TAG, false, "init HwWiTasArbitra", new Object[0]);
        this.mWifiManager = wifiManager;
        this.mWifiNative = wifiNative;
        this.mWiTasTest = HwWiTasTest.getInstance();
        this.mWiTasChr = HwWiTasChr.getInstance(this.mWifiManager, wifiNative);
    }

    private int checkAntenna(int antIndex) {
        int index = WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.sendCmdToDriver(mIfname, (int) HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO, new byte[1]);
        HwHiLog.d(HwWiTasUtils.TAG, false, "check antenna, current index: %{public}d, check index: %{public}d", new Object[]{Integer.valueOf(antIndex), Integer.valueOf(index)});
        if (index != antIndex) {
            return -1;
        }
        return 0;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x001e: APUT  
      (r2v1 'buff' byte[] A[D('buff' byte[]), IMMUTABLE_TYPE])
      (0 ??[int, short, byte, char])
      (wrap: byte : 0x001d: CAST (r4v3 byte A[IMMUTABLE_TYPE]) = (byte) (wrap: int : 0x001b: SGET  (r4v2 int) =  com.android.server.wifi.HwWiTas.HwWiTasUtils.mCoreAntIndex int))
     */
    private int adjustTxPower(int antIndex) {
        byte increaseTxPower = 0;
        byte[] buff = new byte[2];
        HwHiLog.d(HwWiTasUtils.TAG, false, "adjust tx power, index: %{public}d", new Object[]{Integer.valueOf(antIndex)});
        if (antIndex == HwWiTasUtils.mWitasAntIndex) {
            increaseTxPower = 1;
        }
        buff[0] = (byte) HwWiTasUtils.mCoreAntIndex;
        buff[1] = increaseTxPower;
        if (WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.sendCmdToDriver(mIfname, 103, buff) >= 0) {
            return 0;
        }
        HwHiLog.e(HwWiTasUtils.TAG, false, "adjust txpower command error", new Object[0]);
        this.mWiTasChr.recordErrCode(6);
        return -1;
    }
}
