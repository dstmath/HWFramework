package com.android.server.wifi.dc;

import android.os.Bundle;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiServiceFactory;

public class DcChr {
    private static final String DC_ABNORMAL_DISCONNECT_COUNT = "DcAbnormalDisCnt";
    private static final String DC_CONNECT_DURA = "DcConnectDura";
    private static final String DC_CONNECT_SUCC_COUNT = "DcConnectSuccCnt";
    private static final String DC_CONNECT_TOTAL_COUNT = "DcConnectTotalCnt";
    private static final String DC_CONNECT_WIFI_DISCONNECT_COUNT = "DcConnectWifiDisCnt";
    private static final int DC_DEFAULT_COUNT = 1;
    private static final String DC_GET_CONFIG_FAIL_COUNT = "DcHiGetCfgFailCnt";
    private static final String DC_HILINK_CONNECT_FAIL_COUNT = "DcHiConnectFailCnt";
    private static final String DC_P2P_CONNECT_DURA = "DcP2pConnectDura";
    private static final String DC_P2P_CONNECT_FAIL_COUNT = "DcP2pConnectFailCnt";
    private static final String DC_STATE = "DcState";
    private static final String TAG = "DcChr";
    private static DcChr sDcChr = null;
    private long mDcConnectStartTime = 0;
    private long mDcP2pConnectStartTime = 0;
    private HwWifiCHRService mHwWifiChrService = HwWifiServiceFactory.getHwWifiCHRService();

    private DcChr() {
    }

    public static synchronized DcChr getInstance() {
        DcChr dcChr;
        synchronized (DcChr.class) {
            if (sDcChr == null) {
                sDcChr = new DcChr();
            }
            dcChr = sDcChr;
        }
        return dcChr;
    }

    private void uploadDcStatistics(String key, int value) {
        int dcConnectDura;
        String str;
        int dcP2pConnectDura;
        int dcConnectWifiDisconnectCount;
        int dcConnectDura2;
        int dcP2pConnectDura2;
        int dcAbnormalDisconnectCount;
        int dcConnectTotalCount;
        int dcConnectSuccCount;
        int dcAbnormalDisconnectCount2;
        if (this.mHwWifiChrService == null) {
            HwHiLog.e(TAG, false, "mHwWifiChrService is null", new Object[0]);
            return;
        }
        if (DC_CONNECT_TOTAL_COUNT.equals(key)) {
            dcConnectTotalCount = value;
            dcAbnormalDisconnectCount2 = 0;
            dcConnectDura = 0;
            str = DC_CONNECT_DURA;
            dcConnectSuccCount = 0;
            dcAbnormalDisconnectCount = 0;
            dcConnectDura2 = 0;
            dcConnectWifiDisconnectCount = 0;
            dcP2pConnectDura = 0;
            dcP2pConnectDura2 = 0;
        } else if (DC_CONNECT_SUCC_COUNT.equals(key)) {
            dcConnectSuccCount = value;
            dcAbnormalDisconnectCount2 = 0;
            dcConnectDura = 0;
            str = DC_CONNECT_DURA;
            dcConnectTotalCount = 0;
            dcAbnormalDisconnectCount = 0;
            dcConnectDura2 = 0;
            dcConnectWifiDisconnectCount = 0;
            dcP2pConnectDura = 0;
            dcP2pConnectDura2 = 0;
        } else if (DC_GET_CONFIG_FAIL_COUNT.equals(key)) {
            dcAbnormalDisconnectCount2 = 0;
            dcConnectDura = 0;
            str = DC_CONNECT_DURA;
            dcConnectSuccCount = 0;
            dcConnectDura2 = 0;
            dcConnectWifiDisconnectCount = 0;
            dcAbnormalDisconnectCount = value;
            dcP2pConnectDura = 0;
            dcConnectTotalCount = 0;
            dcP2pConnectDura2 = 0;
        } else if (DC_HILINK_CONNECT_FAIL_COUNT.equals(key)) {
            dcAbnormalDisconnectCount2 = 0;
            dcConnectDura = 0;
            str = DC_CONNECT_DURA;
            dcConnectSuccCount = 0;
            dcAbnormalDisconnectCount = 0;
            dcConnectDura2 = 0;
            dcConnectWifiDisconnectCount = 0;
            dcP2pConnectDura = 0;
            dcP2pConnectDura2 = value;
            dcConnectTotalCount = 0;
        } else if (DC_P2P_CONNECT_FAIL_COUNT.equals(key)) {
            dcAbnormalDisconnectCount2 = 0;
            dcConnectDura = 0;
            str = DC_CONNECT_DURA;
            dcConnectSuccCount = 0;
            dcAbnormalDisconnectCount = 0;
            dcConnectWifiDisconnectCount = 0;
            dcConnectDura2 = value;
            dcP2pConnectDura = 0;
            dcConnectTotalCount = 0;
            dcP2pConnectDura2 = 0;
        } else if (DC_CONNECT_WIFI_DISCONNECT_COUNT.equals(key)) {
            dcConnectWifiDisconnectCount = value;
            dcAbnormalDisconnectCount2 = 0;
            dcConnectDura = 0;
            str = DC_CONNECT_DURA;
            dcConnectTotalCount = 0;
            dcConnectSuccCount = 0;
            dcAbnormalDisconnectCount = 0;
            dcConnectDura2 = 0;
            dcP2pConnectDura = 0;
            dcP2pConnectDura2 = 0;
        } else if (DC_ABNORMAL_DISCONNECT_COUNT.equals(key)) {
            dcAbnormalDisconnectCount2 = value;
            dcConnectDura = 0;
            str = DC_CONNECT_DURA;
            dcConnectTotalCount = 0;
            dcConnectSuccCount = 0;
            dcAbnormalDisconnectCount = 0;
            dcConnectDura2 = 0;
            dcConnectWifiDisconnectCount = 0;
            dcP2pConnectDura = 0;
            dcP2pConnectDura2 = 0;
        } else if (DC_P2P_CONNECT_DURA.equals(key)) {
            dcAbnormalDisconnectCount2 = 0;
            dcConnectDura = 0;
            str = DC_CONNECT_DURA;
            dcConnectTotalCount = 0;
            dcConnectSuccCount = 0;
            dcAbnormalDisconnectCount = 0;
            dcConnectDura2 = 0;
            dcConnectWifiDisconnectCount = 0;
            dcP2pConnectDura = value;
            dcP2pConnectDura2 = 0;
        } else if (DC_CONNECT_DURA.equals(key)) {
            dcAbnormalDisconnectCount2 = 0;
            dcConnectDura = value;
            str = DC_CONNECT_DURA;
            dcConnectTotalCount = 0;
            dcConnectSuccCount = 0;
            dcAbnormalDisconnectCount = 0;
            dcConnectDura2 = 0;
            dcConnectWifiDisconnectCount = 0;
            dcP2pConnectDura = 0;
            dcP2pConnectDura2 = 0;
        } else {
            HwHiLog.d(TAG, false, "uploadDcCount other info", new Object[0]);
            dcConnectDura = 0;
            dcAbnormalDisconnectCount2 = 0;
            dcConnectTotalCount = 0;
            dcConnectSuccCount = 0;
            dcAbnormalDisconnectCount = 0;
            dcConnectDura2 = 0;
            dcConnectWifiDisconnectCount = 0;
            str = DC_CONNECT_DURA;
            dcP2pConnectDura = 0;
            dcP2pConnectDura2 = 0;
        }
        Bundle data = new Bundle();
        data.putInt(DC_CONNECT_TOTAL_COUNT, dcConnectTotalCount);
        data.putInt(DC_CONNECT_SUCC_COUNT, dcConnectSuccCount);
        data.putInt(DC_GET_CONFIG_FAIL_COUNT, dcAbnormalDisconnectCount);
        data.putInt(DC_HILINK_CONNECT_FAIL_COUNT, dcP2pConnectDura2);
        data.putInt(DC_P2P_CONNECT_FAIL_COUNT, dcConnectDura2);
        data.putInt(DC_CONNECT_WIFI_DISCONNECT_COUNT, dcConnectWifiDisconnectCount);
        data.putInt(DC_ABNORMAL_DISCONNECT_COUNT, dcAbnormalDisconnectCount2);
        data.putInt(DC_P2P_CONNECT_DURA, dcP2pConnectDura);
        data.putInt(str, dcConnectDura);
        this.mHwWifiChrService.uploadDFTEvent(16, data);
    }

    public void uploadDcConnectTotalCount() {
        uploadDcStatistics(DC_CONNECT_TOTAL_COUNT, 1);
    }

    public void uploadDcConnectSuccCount() {
        uploadDcStatistics(DC_CONNECT_SUCC_COUNT, 1);
    }

    public void uploadDcGetConfigFailCount() {
        uploadDcStatistics(DC_GET_CONFIG_FAIL_COUNT, 1);
    }

    public void uploadDcHilinkConnectFailCount() {
        uploadDcStatistics(DC_HILINK_CONNECT_FAIL_COUNT, 1);
    }

    public void uploadDcP2pConnectFailCount() {
        uploadDcStatistics(DC_P2P_CONNECT_FAIL_COUNT, 1);
    }

    public void uploadDcConnectWifiDisconnectCount() {
        uploadDcStatistics(DC_CONNECT_WIFI_DISCONNECT_COUNT, 1);
    }

    public void uploadDcAbnormalDisconnectCount() {
        uploadDcStatistics(DC_ABNORMAL_DISCONNECT_COUNT, 1);
    }

    public void setDcP2pConnectStartTime(long time) {
        this.mDcP2pConnectStartTime = time;
    }

    public void uploadDcP2pConnectDura(long dcP2pConnectEndTime) {
        this.mDcP2pConnectStartTime = 0;
        uploadDcStatistics(DC_P2P_CONNECT_DURA, (int) (dcP2pConnectEndTime - this.mDcP2pConnectStartTime));
    }

    public void setDcConnectStartTime(long time) {
        this.mDcConnectStartTime = time;
    }

    public void uploadDcConnectDura(long dcConnectEndTime) {
        this.mDcConnectStartTime = 0;
        uploadDcStatistics(DC_CONNECT_DURA, (int) (dcConnectEndTime - this.mDcConnectStartTime));
    }

    public void uploadDcState(int dcState) {
        if (this.mHwWifiChrService == null) {
            HwHiLog.e(TAG, false, "mHwWifiCHRService is null", new Object[0]);
            return;
        }
        Bundle data = new Bundle();
        data.putInt(DC_STATE, dcState);
        this.mHwWifiChrService.uploadDFTEvent(16, data);
    }
}
