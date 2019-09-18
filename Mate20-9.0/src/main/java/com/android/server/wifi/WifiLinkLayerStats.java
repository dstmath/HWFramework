package com.android.server.wifi;

import java.util.Arrays;

public class WifiLinkLayerStats {
    public int beacon_rx;
    public long lostmpdu_be;
    public long lostmpdu_bk;
    public long lostmpdu_vi;
    public long lostmpdu_vo;
    public int on_time;
    public int on_time_scan;
    public long retries_be;
    public long retries_bk;
    public long retries_vi;
    public long retries_vo;
    public int rssi_mgmt;
    public int rx_time;
    public long rxmpdu_be;
    public long rxmpdu_bk;
    public long rxmpdu_vi;
    public long rxmpdu_vo;
    public long timeStampInMs;
    public int tx_time;
    public int[] tx_time_per_level;
    public long txmpdu_be;
    public long txmpdu_bk;
    public long txmpdu_vi;
    public long txmpdu_vo;

    public String toString() {
        return " WifiLinkLayerStats: " + 10 + " my bss beacon rx: " + Integer.toString(this.beacon_rx) + 10 + " RSSI mgmt: " + Integer.toString(this.rssi_mgmt) + 10 + " BE : " + " rx=" + Long.toString(this.rxmpdu_be) + " tx=" + Long.toString(this.txmpdu_be) + " lost=" + Long.toString(this.lostmpdu_be) + " retries=" + Long.toString(this.retries_be) + 10 + " BK : " + " rx=" + Long.toString(this.rxmpdu_bk) + " tx=" + Long.toString(this.txmpdu_bk) + " lost=" + Long.toString(this.lostmpdu_bk) + " retries=" + Long.toString(this.retries_bk) + 10 + " VI : " + " rx=" + Long.toString(this.rxmpdu_vi) + " tx=" + Long.toString(this.txmpdu_vi) + " lost=" + Long.toString(this.lostmpdu_vi) + " retries=" + Long.toString(this.retries_vi) + 10 + " VO : " + " rx=" + Long.toString(this.rxmpdu_vo) + " tx=" + Long.toString(this.txmpdu_vo) + " lost=" + Long.toString(this.lostmpdu_vo) + " retries=" + Long.toString(this.retries_vo) + 10 + " on_time : " + Integer.toString(this.on_time) + " rx_time=" + Integer.toString(this.rx_time) + " scan_time=" + Integer.toString(this.on_time_scan) + 10 + " tx_time=" + Integer.toString(this.tx_time) + (" tx_time_per_level=" + Arrays.toString(this.tx_time_per_level)) + (" ts=" + this.timeStampInMs);
    }
}
