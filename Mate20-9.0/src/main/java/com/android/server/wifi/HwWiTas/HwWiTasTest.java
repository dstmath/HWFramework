package com.android.server.wifi.HwWiTas;

import android.os.SystemProperties;
import android.util.Log;
import com.android.server.wifi.HwSelfCureUtils;

public class HwWiTasTest {
    private static HwWiTasTest mWiTasTest = null;

    protected static synchronized HwWiTasTest getInstance() {
        HwWiTasTest hwWiTasTest;
        synchronized (HwWiTasTest.class) {
            if (mWiTasTest == null) {
                mWiTasTest = new HwWiTasTest();
            }
            hwWiTasTest = mWiTasTest;
        }
        return hwWiTasTest;
    }

    /* access modifiers changed from: protected */
    public boolean getTestModeCfg() {
        if (SystemProperties.getInt("runtime.witas.test.mode", 0) == 0) {
            return false;
        }
        Log.d(HwWiTasUtils.TAG, "test mode is open");
        return true;
    }

    /* access modifiers changed from: protected */
    public int getGameThreshold() {
        int propCfg = SystemProperties.getInt("runtime.witas.game.thr", 0);
        if (propCfg == 0) {
            return 200;
        }
        int value = propCfg;
        Log.d(HwWiTasUtils.TAG, "test mode, game threshold: " + value);
        return value;
    }

    /* access modifiers changed from: protected */
    public int getRssiThreshold() {
        int propCfg = SystemProperties.getInt("runtime.witas.rssi.thr", 0);
        if (propCfg == 0) {
            return -65;
        }
        int value = propCfg;
        Log.d(HwWiTasUtils.TAG, "test mode, rssi threshold: " + value);
        return value;
    }

    /* access modifiers changed from: protected */
    public int getRssiMaxFluctuation() {
        int propCfg = SystemProperties.getInt("runtime.witas.rssi.fluctuation", 0);
        if (propCfg == 0) {
            return 6;
        }
        int value = propCfg;
        Log.d(HwWiTasUtils.TAG, "test mode, rssi allowed max fluctuation: " + value);
        return value;
    }

    /* access modifiers changed from: protected */
    public int getTimeoutThreshold() {
        int propCfg = SystemProperties.getInt("runtime.witas.timeout.thr", 0);
        if (propCfg == 0) {
            return HwSelfCureUtils.SELFCURE_WIFI_ON_TIMEOUT;
        }
        int value = propCfg;
        Log.d(HwWiTasUtils.TAG, "test mode, timeout threshold: " + value);
        return value;
    }

    /* access modifiers changed from: protected */
    public int getSrcRssi(int rssi) {
        int propCfg = SystemProperties.getInt("runtime.witas.rssi.src", 0);
        if (propCfg == 0) {
            return rssi;
        }
        int value = propCfg;
        Log.d(HwWiTasUtils.TAG, "test mode, src rssi: " + value);
        return value;
    }

    /* access modifiers changed from: protected */
    public int getDstRssi(int rssi) {
        int propCfg = SystemProperties.getInt("runtime.witas.rssi.dst", 0);
        if (propCfg == 0) {
            return rssi;
        }
        int value = propCfg;
        Log.d(HwWiTasUtils.TAG, "test mode, dst rssi: " + value);
        return value;
    }

    /* access modifiers changed from: protected */
    public int getGameDelay(int delay) {
        int propCfg = SystemProperties.getInt("runtime.witas.game.delay", 0);
        if (propCfg == 0) {
            return delay;
        }
        int value = propCfg;
        Log.d(HwWiTasUtils.TAG, "test mode, game delay: " + value);
        return value;
    }

    /* access modifiers changed from: protected */
    public int getRssiLow() {
        int propCfg = SystemProperties.getInt("runtime.witas.rssi.low", 0);
        if (propCfg == 0) {
            return 1;
        }
        int value = propCfg;
        Log.d(HwWiTasUtils.TAG, "test mode, rssi low: " + value);
        return value;
    }

    /* access modifiers changed from: protected */
    public int getRssiHigh() {
        int propCfg = SystemProperties.getInt("runtime.witas.rssi.high", 0);
        if (propCfg == 0) {
            return 4;
        }
        int value = propCfg;
        Log.d(HwWiTasUtils.TAG, "test mode, rssi high: " + value);
        return value;
    }

    /* access modifiers changed from: protected */
    public int getFreezeShort() {
        int propCfg = SystemProperties.getInt("runtime.witas.freeze.short", 0);
        if (propCfg == 0) {
            return 15000;
        }
        int value = propCfg;
        Log.d(HwWiTasUtils.TAG, "test mode, freeze short: " + value);
        return value;
    }

    /* access modifiers changed from: protected */
    public int getFreezeLong() {
        int propCfg = SystemProperties.getInt("runtime.witas.freeze.long", 0);
        if (propCfg == 0) {
            return 30000;
        }
        int value = propCfg;
        Log.d(HwWiTasUtils.TAG, "test mode, freeze long: " + value);
        return value;
    }

    private HwWiTasTest() {
        Log.d(HwWiTasUtils.TAG, "init HwWiTasTest");
    }
}
