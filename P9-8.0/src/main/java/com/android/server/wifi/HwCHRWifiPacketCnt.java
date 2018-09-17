package com.android.server.wifi;

import com.android.server.wifi.WifiNative.TxPacketCounters;
import com.huawei.device.connectivitychrlog.CSubPacketCount;

public class HwCHRWifiPacketCnt {
    private int mRXGood = 0;
    private int mRxgood_Last = 0;
    private int mTXGood = 0;
    private int mTXbad = 0;
    private int mTxbad_Last = 0;
    private int mTxgood_Last = 0;
    private WifiNative mWifiNative;

    public HwCHRWifiPacketCnt(WifiNative wifiNative) {
        this.mWifiNative = wifiNative;
    }

    public void fetchPktcntNative() {
        if (this.mWifiNative != null) {
            int tx_Good = 0;
            int tx_bad = 0;
            TxPacketCounters counters = this.mWifiNative.getTxPacketCounters();
            if (counters != null) {
                tx_Good = counters.txSucceeded;
                tx_bad = counters.txFailed;
            }
            this.mTXGood = tx_Good - this.mTxgood_Last;
            this.mTxgood_Last = tx_Good;
            this.mRXGood = 0 - this.mRxgood_Last;
            this.mRxgood_Last = 0;
            this.mTXbad = tx_bad - this.mTxbad_Last;
            this.mTxbad_Last = tx_bad;
        }
    }

    public int getTxTotal() {
        return this.mTXGood + this.mTXbad;
    }

    public int getTxBad() {
        return this.mTXbad;
    }

    public CSubPacketCount getPacketCntCHR() {
        CSubPacketCount result = new CSubPacketCount();
        result.iRX_GOOD.setValue(this.mRXGood);
        result.iTX_GOOD.setValue(this.mTXGood);
        result.iTX_BAD.setValue(this.mTXbad);
        return result;
    }

    public String toString() {
        return "HwCHRWifiPacketCnt [mTXGood=" + this.mTXGood + ", mTXbad=" + this.mTXbad + ", mRXGood=" + this.mRXGood + "]";
    }
}
