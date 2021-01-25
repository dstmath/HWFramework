package com.android.server.wifi.HwWiTas;

import android.os.SystemProperties;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.HwQoE.HwQoEService;

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
        HwHiLog.d(HwWiTasUtils.TAG, false, "test mode is open", new Object[0]);
        return true;
    }

    /* access modifiers changed from: protected */
    public int getGameThreshold() {
        int propCfg = SystemProperties.getInt("runtime.witas.game.thr", 0);
        if (propCfg == 0) {
            return HwQoEService.KOG_LATENCY_TIME_THRESHOLD;
        }
        HwHiLog.d(HwWiTasUtils.TAG, false, "test mode, game threshold: %{public}d", new Object[]{Integer.valueOf(propCfg)});
        return propCfg;
    }

    /* access modifiers changed from: protected */
    public int getRssiThreshold() {
        int propCfg = SystemProperties.getInt("runtime.witas.rssi.thr", 0);
        if (propCfg == 0) {
            return -65;
        }
        HwHiLog.d(HwWiTasUtils.TAG, false, "test mode, rssi threshold: %{public}d", new Object[]{Integer.valueOf(propCfg)});
        return propCfg;
    }

    /* access modifiers changed from: protected */
    public int getRssiMaxFluctuation() {
        int propCfg = SystemProperties.getInt("runtime.witas.rssi.fluctuation", 0);
        if (propCfg == 0) {
            return 6;
        }
        HwHiLog.d(HwWiTasUtils.TAG, false, "test mode, rssi allowed max fluctuation: %{public}d", new Object[]{Integer.valueOf(propCfg)});
        return propCfg;
    }

    /* access modifiers changed from: protected */
    public int getTimeoutThreshold() {
        int propCfg = SystemProperties.getInt("runtime.witas.timeout.thr", 0);
        if (propCfg == 0) {
            return 3000;
        }
        HwHiLog.d(HwWiTasUtils.TAG, false, "test mode, timeout threshold: %{public}d", new Object[]{Integer.valueOf(propCfg)});
        return propCfg;
    }

    /* access modifiers changed from: protected */
    public int getSrcRssi(int rssi) {
        int propCfg = SystemProperties.getInt("runtime.witas.rssi.src", 0);
        if (propCfg == 0) {
            return rssi;
        }
        HwHiLog.d(HwWiTasUtils.TAG, false, "test mode, src rssi: %{public}d", new Object[]{Integer.valueOf(propCfg)});
        return propCfg;
    }

    /* access modifiers changed from: protected */
    public int getDstRssi(int rssi) {
        int propCfg = SystemProperties.getInt("runtime.witas.rssi.dst", 0);
        if (propCfg == 0) {
            return rssi;
        }
        HwHiLog.d(HwWiTasUtils.TAG, false, "test mode, dst rssi: %{public}d", new Object[]{Integer.valueOf(propCfg)});
        return propCfg;
    }

    /* access modifiers changed from: protected */
    public int getGameDelay(int delay) {
        int propCfg = SystemProperties.getInt("runtime.witas.game.delay", 0);
        if (propCfg == 0) {
            return delay;
        }
        HwHiLog.d(HwWiTasUtils.TAG, false, "test mode, game delay: %{public}d", new Object[]{Integer.valueOf(propCfg)});
        return propCfg;
    }

    /* access modifiers changed from: protected */
    public int getRssiLow() {
        int propCfg = SystemProperties.getInt("runtime.witas.rssi.low", 0);
        if (propCfg == 0) {
            return 1;
        }
        HwHiLog.d(HwWiTasUtils.TAG, false, "test mode, rssi low: %{public}d", new Object[]{Integer.valueOf(propCfg)});
        return propCfg;
    }

    /* access modifiers changed from: protected */
    public int getRssiHigh() {
        int propCfg = SystemProperties.getInt("runtime.witas.rssi.high", 0);
        if (propCfg == 0) {
            return 4;
        }
        HwHiLog.d(HwWiTasUtils.TAG, false, "test mode, rssi high: %{public}d", new Object[]{Integer.valueOf(propCfg)});
        return propCfg;
    }

    /* access modifiers changed from: protected */
    public int getFreezeShort() {
        int propCfg = SystemProperties.getInt("runtime.witas.freeze.short", 0);
        if (propCfg == 0) {
            return 15000;
        }
        HwHiLog.d(HwWiTasUtils.TAG, false, "test mode, freeze short: %{public}d", new Object[]{Integer.valueOf(propCfg)});
        return propCfg;
    }

    /* access modifiers changed from: protected */
    public int getFreezeLong() {
        int propCfg = SystemProperties.getInt("runtime.witas.freeze.long", 0);
        if (propCfg == 0) {
            return 30000;
        }
        HwHiLog.d(HwWiTasUtils.TAG, false, "test mode, freeze long: %{public}d", new Object[]{Integer.valueOf(propCfg)});
        return propCfg;
    }

    private HwWiTasTest() {
        HwHiLog.d(HwWiTasUtils.TAG, false, "init HwWiTasTest", new Object[0]);
    }
}
