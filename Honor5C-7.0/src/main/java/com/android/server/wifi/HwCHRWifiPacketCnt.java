package com.android.server.wifi;

import com.huawei.device.connectivitychrlog.CSubPacketCount;

public class HwCHRWifiPacketCnt {
    private int mRXGood;
    private int mRxgood_Last;
    private int mTXGood;
    private int mTXbad;
    private int mTxbad_Last;
    private int mTxgood_Last;
    private WifiNative mWifiNative;

    public HwCHRWifiPacketCnt(WifiNative wifiNative) {
        this.mTXGood = 0;
        this.mTXbad = 0;
        this.mRXGood = 0;
        this.mTxgood_Last = 0;
        this.mTxbad_Last = 0;
        this.mRxgood_Last = 0;
        this.mWifiNative = wifiNative;
    }

    public void fetchPktcntNative() {
        if (this.mWifiNative != null) {
            int tx_Good = 0;
            int tx_bad = 0;
            int rx_Good = 0;
            String pktcntPoll = this.mWifiNative.pktcntPoll();
            if (pktcntPoll != null) {
                for (String line : pktcntPoll.split("\n")) {
                    String[] prop = line.split("=");
                    if (prop.length >= 2) {
                        try {
                            if (prop[0].equals("TXGOOD")) {
                                tx_Good = Integer.parseInt(prop[1]);
                            } else if (prop[0].equals("TXBAD")) {
                                tx_bad = Integer.parseInt(prop[1]);
                            } else if (prop[0].equals("RXGOOD")) {
                                rx_Good = Integer.parseInt(prop[1]);
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
            this.mTXGood = tx_Good - this.mTxgood_Last;
            this.mTxgood_Last = tx_Good;
            this.mRXGood = rx_Good - this.mRxgood_Last;
            this.mRxgood_Last = rx_Good;
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
