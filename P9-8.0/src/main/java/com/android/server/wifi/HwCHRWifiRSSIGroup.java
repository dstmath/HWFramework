package com.android.server.wifi;

import android.util.Log;
import com.huawei.device.connectivitychrlog.CSubRSSIGROUP_EVENT;
import com.huawei.device.connectivitychrlog.CSubRSSIGROUP_EVENT_EX;

public class HwCHRWifiRSSIGroup {
    public static final int RSSI_1 = 1;
    public static final int RSSI_2 = 2;
    public static final int RSSI_3 = 3;
    public static final int RSSI_4 = 4;
    private int adjacent_area_frequency = 0;
    private int rssi_index;
    private int rtt_lan_duration = 0;
    private int rtt_lan_failures = 0;
    private int rtt_lan_max = 0;
    private int rtt_lan_min = Integer.MAX_VALUE;
    private int rtt_lan_succ = 0;
    private int rtt_tcp_duration = 0;
    private int rtt_tcp_packets = 0;
    private int same_area_frequency = 0;

    public HwCHRWifiRSSIGroup(int index) {
        this.rssi_index = index;
    }

    private static int get_rssi_index(int rssi) {
        if (rssi <= -83) {
            return 1;
        }
        if (rssi <= -76 && rssi >= -82) {
            return 2;
        }
        if (rssi > -66 || rssi < -75) {
            return 4;
        }
        return 3;
    }

    public boolean isMatchByRSSI(int rssi) {
        return get_rssi_index(rssi) == this.rssi_index;
    }

    public void add_rtt_tcp(int rtt_tcp_packets, int rtt_tcp_duration) {
        this.rtt_tcp_packets += rtt_tcp_packets;
        this.rtt_tcp_duration += rtt_tcp_duration;
    }

    public void add_rtt_lan_succ(int rtt_lan_duration) {
        this.rtt_lan_succ++;
        this.rtt_lan_duration += rtt_lan_duration;
        if (rtt_lan_duration > this.rtt_lan_max) {
            this.rtt_lan_max = rtt_lan_duration;
        }
        if (rtt_lan_duration < this.rtt_lan_min) {
            this.rtt_lan_min = rtt_lan_duration;
        }
    }

    public void add_area_freq(boolean isSameArea) {
        if (isSameArea) {
            this.same_area_frequency++;
        } else {
            this.adjacent_area_frequency++;
        }
    }

    public void add_rtt_lan_failures() {
        this.rtt_lan_failures++;
    }

    public void reset() {
        this.rtt_tcp_packets = 0;
        this.rtt_tcp_duration = 0;
        this.rtt_lan_duration = 0;
        this.rtt_lan_failures = 0;
        this.rtt_lan_succ = 0;
        this.rtt_lan_max = 0;
        this.rtt_lan_min = Integer.MAX_VALUE;
        this.adjacent_area_frequency = 0;
        this.same_area_frequency = 0;
    }

    public HwCHRWifiRSSIGroup copy() {
        HwCHRWifiRSSIGroup result = new HwCHRWifiRSSIGroup(this.rssi_index);
        result.rtt_tcp_packets = this.rtt_tcp_packets;
        result.rtt_tcp_duration = this.rtt_tcp_duration;
        result.rtt_lan_duration = this.rtt_lan_duration;
        result.rtt_lan_failures = this.rtt_lan_failures;
        result.rtt_lan_succ = this.rtt_lan_succ;
        result.rtt_lan_max = this.rtt_lan_max;
        result.rtt_lan_min = this.rtt_lan_min;
        result.adjacent_area_frequency = this.adjacent_area_frequency;
        result.same_area_frequency = this.same_area_frequency;
        return result;
    }

    public String toString() {
        return "HwCHRWifiRSSIGroup [rssi_index=" + this.rssi_index + ", rtt_tcp_packets=" + this.rtt_tcp_packets + ", rtt_tcp_duration=" + this.rtt_tcp_duration + ", rtt_lan_duration=" + this.rtt_lan_duration + ", rtt_lan_failures=" + this.rtt_lan_failures + ", rtt_lan_succ=" + this.rtt_lan_succ + ", rtt_lan_max=" + this.rtt_lan_max + ", rtt_lan_min=" + this.rtt_lan_min + ",same_area_frequency=" + this.same_area_frequency + ",adjacent_area_frequency=" + this.adjacent_area_frequency + "]";
    }

    public CSubRSSIGROUP_EVENT getRSSIGroupEventCHR() {
        CSubRSSIGROUP_EVENT event = new CSubRSSIGROUP_EVENT();
        event.ucRSSIGrpIndex.setValue(this.rssi_index);
        event.iSAME_FREQ_APS.setValue(this.same_area_frequency);
        event.iADJACENT_FREQ_APS.setValue(this.adjacent_area_frequency);
        Log.e("HwCHRWifiRSSIGroup", toString());
        return event;
    }

    public CSubRSSIGROUP_EVENT_EX getRSSIGroupEXEventCHR() {
        CSubRSSIGROUP_EVENT_EX event = new CSubRSSIGROUP_EVENT_EX();
        event.ucRSSIGrpIndex.setValue(this.rssi_index);
        event.iSAME_FREQ_APS.setValue(this.same_area_frequency);
        event.iADJACENT_FREQ_APS.setValue(this.adjacent_area_frequency);
        event.iTcp_RTT_Duration.setValue(this.rtt_tcp_duration);
        event.iTcp_RTT_Packets.setValue(this.rtt_tcp_packets);
        event.iLAN_RTT_Duration.setValue(this.rtt_lan_duration);
        event.iLAN_RTT_Packets.setValue(this.rtt_lan_succ);
        event.iLAN_RTT_Failues.setValue(this.rtt_lan_failures);
        event.iLAN_RTT_MAX.setValue(this.rtt_lan_max);
        event.iLAN_RTT_MIN.setValue(this.rtt_lan_min);
        Log.e("HwCHRWifiRSSIGroupEX", toString());
        return event;
    }
}
