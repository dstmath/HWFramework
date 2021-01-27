package com.android.server.wifi;

import android.content.Context;

public class WifiDataStall {
    public static final long MAX_MS_DELTA_FOR_DATA_STALL = 60000;
    public static final int MIN_TX_BAD_DEFAULT = 1;
    public static final int MIN_TX_SUCCESS_WITHOUT_RX_DEFAULT = 50;
    private final Context mContext;
    private final FrameworkFacade mFacade;
    private int mMinTxBad;
    private int mMinTxSuccessWithoutRx;
    private final WifiMetrics mWifiMetrics;

    public WifiDataStall(Context context, FrameworkFacade facade, WifiMetrics wifiMetrics) {
        this.mContext = context;
        this.mFacade = facade;
        this.mWifiMetrics = wifiMetrics;
        loadSettings();
    }

    public void loadSettings() {
        this.mMinTxBad = this.mFacade.getIntegerSetting(this.mContext, "wifi_data_stall_min_tx_bad", 1);
        this.mMinTxSuccessWithoutRx = this.mFacade.getIntegerSetting(this.mContext, "wifi_data_stall_min_tx_success_without_rx", 50);
        this.mWifiMetrics.setWifiDataStallMinTxBad(this.mMinTxBad);
        this.mWifiMetrics.setWifiDataStallMinRxWithoutTx(this.mMinTxSuccessWithoutRx);
    }

    public int checkForDataStall(WifiLinkLayerStats oldStats, WifiLinkLayerStats newStats) {
        if (oldStats == null || newStats == null) {
            this.mWifiMetrics.resetWifiIsUnusableLinkLayerStats();
            return 0;
        }
        long txSuccessDelta = (((newStats.txmpdu_be + newStats.txmpdu_bk) + newStats.txmpdu_vi) + newStats.txmpdu_vo) - (((oldStats.txmpdu_be + oldStats.txmpdu_bk) + oldStats.txmpdu_vi) + oldStats.txmpdu_vo);
        long txRetriesDelta = (((newStats.retries_be + newStats.retries_bk) + newStats.retries_vi) + newStats.retries_vo) - (((oldStats.retries_be + oldStats.retries_bk) + oldStats.retries_vi) + oldStats.retries_vo);
        long txBadDelta = (((newStats.lostmpdu_be + newStats.lostmpdu_bk) + newStats.lostmpdu_vi) + newStats.lostmpdu_vo) - (((oldStats.lostmpdu_be + oldStats.lostmpdu_bk) + oldStats.lostmpdu_vi) + oldStats.lostmpdu_vo);
        long rxSuccessDelta = (((newStats.rxmpdu_be + newStats.rxmpdu_bk) + newStats.rxmpdu_vi) + newStats.rxmpdu_vo) - (((oldStats.rxmpdu_be + oldStats.rxmpdu_bk) + oldStats.rxmpdu_vi) + oldStats.rxmpdu_vo);
        long timeMsDelta = newStats.timeStampInMs - oldStats.timeStampInMs;
        if (timeMsDelta < 0 || txSuccessDelta < 0 || txRetriesDelta < 0 || txBadDelta < 0 || rxSuccessDelta < 0) {
            this.mWifiMetrics.resetWifiIsUnusableLinkLayerStats();
            return 0;
        }
        this.mWifiMetrics.updateWifiIsUnusableLinkLayerStats(txSuccessDelta, txRetriesDelta, txBadDelta, rxSuccessDelta, timeMsDelta);
        if (timeMsDelta < 60000) {
            boolean dataStallBadTx = txBadDelta >= ((long) this.mMinTxBad);
            boolean dataStallTxSuccessWithoutRx = rxSuccessDelta == 0 && txSuccessDelta >= ((long) this.mMinTxSuccessWithoutRx);
            if (dataStallBadTx && dataStallTxSuccessWithoutRx) {
                this.mWifiMetrics.logWifiIsUnusableEvent(3);
                return 3;
            } else if (dataStallBadTx) {
                this.mWifiMetrics.logWifiIsUnusableEvent(1);
                return 1;
            } else if (dataStallTxSuccessWithoutRx) {
                this.mWifiMetrics.logWifiIsUnusableEvent(2);
                return 2;
            }
        }
        return 0;
    }
}
