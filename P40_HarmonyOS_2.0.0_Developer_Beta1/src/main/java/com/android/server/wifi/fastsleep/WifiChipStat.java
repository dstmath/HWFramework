package com.android.server.wifi.fastsleep;

import android.os.Bundle;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.WifiNative;

public class WifiChipStat {
    private static final String BANDWIDTH_TIME = "bwTime";
    private static final String CHIP_TIME = "chipTime";
    private static final int CMD_GET_BW_TIME_20M_2G = 208;
    private static final int CMD_GET_TX_RATE_CNT = 214;
    private static final int CMD_GET_TX_TOTAL_CNT = 201;
    private static final int CMD_GET_WORK_TIME = 203;
    private static final String IFACE = "wlan0";
    private static final String RATE_PKTS_CNT = "ratePkts";
    private static final String TAG = "WifiChipStat";
    private static final String TOTAL_PKTS_CNT = "totalPkts";
    private static final byte[] WIFI_CHIP_DATA = {89};
    private static WifiChipStat sWifiChipStat = null;
    private int[] mBandwidthTime;
    private HwWifiCHRService mHwWifiChrService;
    private int[] mTotalPktsCnt;
    private WifiNative mWifiNative;
    private int[] mWorkTime;

    /* access modifiers changed from: private */
    public enum HiviewBwIdx {
        BW_2G_20M,
        BW_2G_40M,
        BW_2G_80M,
        BW_2G_160M,
        BW_2G_80M_PLUS_80M,
        BW_5G_20M,
        BW_5G_40M,
        BW_5G_80M,
        BW_5G_160M,
        BW_5G_80M_PLUS_80M
    }

    /* access modifiers changed from: private */
    public enum WifiChipBwIdx {
        BW_2G_20M,
        BW_2G_40M,
        BW_5G_20M,
        BW_5G_40M,
        BW_5G_80M
    }

    private WifiChipStat(WifiNative wifiNative) {
        this.mHwWifiChrService = null;
        this.mTotalPktsCnt = new int[]{0, 0};
        this.mWorkTime = new int[]{0, 0, 0};
        this.mBandwidthTime = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        this.mHwWifiChrService = HwWifiServiceFactory.getHwWifiCHRService();
        this.mWifiNative = wifiNative;
    }

    public static synchronized WifiChipStat createWifiChipStat(WifiNative wifiNative) {
        WifiChipStat wifiChipStat;
        synchronized (WifiChipStat.class) {
            if (sWifiChipStat == null) {
                sWifiChipStat = new WifiChipStat(wifiNative);
            }
            wifiChipStat = sWifiChipStat;
        }
        return wifiChipStat;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.wifi.fastsleep.WifiChipStat$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$wifi$fastsleep$WifiChipStat$WifiChipBwIdx = new int[WifiChipBwIdx.values().length];

        static {
            try {
                $SwitchMap$com$android$server$wifi$fastsleep$WifiChipStat$WifiChipBwIdx[WifiChipBwIdx.BW_2G_20M.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$wifi$fastsleep$WifiChipStat$WifiChipBwIdx[WifiChipBwIdx.BW_2G_40M.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$wifi$fastsleep$WifiChipStat$WifiChipBwIdx[WifiChipBwIdx.BW_5G_20M.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$server$wifi$fastsleep$WifiChipStat$WifiChipBwIdx[WifiChipBwIdx.BW_5G_40M.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$server$wifi$fastsleep$WifiChipStat$WifiChipBwIdx[WifiChipBwIdx.BW_5G_80M.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    private static int wifiBwIndexToHiviewIdx(WifiChipBwIdx bwIdx) {
        int i = AnonymousClass1.$SwitchMap$com$android$server$wifi$fastsleep$WifiChipStat$WifiChipBwIdx[bwIdx.ordinal()];
        if (i == 1) {
            return HiviewBwIdx.BW_2G_20M.ordinal();
        }
        if (i == 2) {
            return HiviewBwIdx.BW_2G_40M.ordinal();
        }
        if (i == 3) {
            return HiviewBwIdx.BW_5G_20M.ordinal();
        }
        if (i == 4) {
            return HiviewBwIdx.BW_5G_40M.ordinal();
        }
        if (i != 5) {
            return HiviewBwIdx.BW_5G_80M_PLUS_80M.ordinal();
        }
        return HiviewBwIdx.BW_5G_80M.ordinal();
    }

    public void getWifiChipData() {
        int i = 0;
        while (true) {
            int[] iArr = this.mTotalPktsCnt;
            if (i >= iArr.length) {
                break;
            }
            iArr[i] = this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, i + CMD_GET_TX_TOTAL_CNT, WIFI_CHIP_DATA);
            i++;
        }
        int i2 = 0;
        while (true) {
            int[] iArr2 = this.mWorkTime;
            if (i2 >= iArr2.length) {
                break;
            }
            iArr2[i2] = this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, i2 + CMD_GET_WORK_TIME, WIFI_CHIP_DATA);
            i2++;
        }
        WifiChipBwIdx[] values = WifiChipBwIdx.values();
        for (WifiChipBwIdx bwIdx : values) {
            this.mBandwidthTime[wifiBwIndexToHiviewIdx(bwIdx)] = this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, bwIdx.ordinal() + CMD_GET_BW_TIME_20M_2G, WIFI_CHIP_DATA);
        }
    }

    public void reportWifiChipData() {
        if (this.mHwWifiChrService != null) {
            Bundle chrData = new Bundle();
            chrData.putIntArray(TOTAL_PKTS_CNT, this.mTotalPktsCnt);
            chrData.putIntArray(CHIP_TIME, this.mWorkTime);
            chrData.putIntArray(BANDWIDTH_TIME, this.mBandwidthTime);
            this.mHwWifiChrService.uploadDFTEvent(34, chrData);
        }
    }
}
