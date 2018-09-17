package com.android.server.wifi;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

class HwCHRWifiSpeedBaseChecker {
    private static final int ACCESS_OK = 0;
    private static final int ACCESS_SLOWLY_ONLY_RESEND = 2;
    private static final int ACCESS_SLOWLY_RESEND_RATE = 3;
    public static final int ACCESS_SLOWLY_RSSI_RTT = 4;
    private static final int ACCESS_SLOWLY_RTT = 1;
    public static final int ACCESS_SLOWLY_WIFI_PACKET_COUNT = 5;
    protected static final int AGE_INIT_VALUE = 2;
    public static final double D_ACKS_RATE = 0.05d;
    public static final String LAN_DUP_ACKS = "SNDDUPACKS";
    public static final String LAN_ERRSEGS = "ERRSEGS";
    public static final String LAN_ESTRSTS = "ESTABLISRST";
    public static final String LAN_OUTRSTS = "OUTRSTS";
    public static final String LAN_RECVSEGS = "RECVSEGS";
    public static final String LAN_RESENDSEGS = "RESENDSEGS";
    public static final String LAN_RTT_DURATION = "RTTDURATION";
    public static final String LAN_RTT_PACKETS = "RTTSEGS";
    public static final String LAN_SENDSEGS = "SENDSEGS";
    public static final int OUTRST_THRESHOLD = 10;
    public static final double RCV_SND_RATE = 0.5d;
    public static final int RECOVERAGE_THRESHOLD = 3;
    public static final int RESEND_COUNT = 2;
    public static final double RESEND_RATE = 0.4d;
    public static final int RTT_PACKET_COUNT_3 = 3;
    public static final int RTT_PACKET_COUNT_5 = 5;
    public static final int RTT_THRESHOLD_100 = 100;
    public static final int RTT_THRESHOLD_200 = 200;
    public static final int RTT_THRESHOLD_300 = 300;
    public static final int RTT_THRESHOLD_400 = 400;
    public static final int SEND_PACKETS_THRESHOLD = 5;
    public static final int SUCT_TIME_COUNT = 3;
    private static final String TAG = "HwWifiSpeedBaseCheck";
    public static final String WEB_DUP_ACKS = "WEBSNDDUPACKS";
    public static final String WEB_ERRSEGS = "WEBERRSEGS";
    public static final String WEB_ESTRSTS = "WEBESTABLISRST";
    public static final String WEB_OUTRSTS = "WEBOUTRSTS";
    public static final String WEB_RECVSEGS = "WEBRECVSEGS";
    public static final String WEB_RESENDSEGS = "WEBRESENDSEGS";
    public static final String WEB_RTT_DURATION = "WEBRTTDURATION";
    public static final String WEB_RTT_PACKETS = "WEBRTTSEGS";
    public static final String WEB_SENDSEGS = "WEBSENDSEGS";
    public static final String WEB_SRTT = "WEBSRTT";
    protected int age;
    protected List<HwCHRWifiCounterInfo> counters;
    private int mFailReason;
    protected boolean mNeedChecked;
    protected int recovage_times;
    protected int suckTimes;

    public HwCHRWifiSpeedBaseChecker() {
        this.mNeedChecked = false;
        this.recovage_times = 0;
        this.suckTimes = 0;
        this.age = 2;
        this.counters = null;
        this.mFailReason = 0;
        this.counters = new ArrayList();
    }

    public int getSuckTimes() {
        return this.suckTimes;
    }

    public int old() {
        this.age--;
        return this.age;
    }

    public int getOld() {
        return this.age;
    }

    public long getCounterDetaByTab(String tag) {
        int listSize = this.counters.size();
        for (int i = 0; i < listSize; i++) {
            if (((HwCHRWifiCounterInfo) this.counters.get(i)).isSameTag(tag)) {
                return ((HwCHRWifiCounterInfo) this.counters.get(i)).getDelta();
            }
        }
        return 0;
    }

    protected void checkSuckStatus() {
        if (this.mNeedChecked) {
            this.suckTimes = 0;
            this.mFailReason = 0;
            long rtt_duration = getCounterDetaByTab(WEB_RTT_DURATION);
            long rtt_packets = getCounterDetaByTab(WEB_RTT_PACKETS);
            int rtt = 0;
            if (rtt_packets > 0) {
                rtt = (int) (rtt_duration / rtt_packets);
            }
            if (rtt_packets < 3 || rtt < RTT_THRESHOLD_400) {
                long send_packet = getCounterDetaByTab(WEB_SENDSEGS);
                long resend_packets = getCounterDetaByTab(WEB_RESENDSEGS);
                if (rtt_packets < 3 && rtt > RTT_THRESHOLD_400 && send_packet >= 5 && ((double) resend_packets) / ((double) send_packet) > 0.4d) {
                    this.suckTimes++;
                    this.recovage_times = 0;
                    this.mFailReason = 3;
                    loge("resend_packets/send_packet=" + (((double) resend_packets) / ((double) send_packet)));
                    return;
                } else if (rtt_packets >= 3 || rtt <= RTT_THRESHOLD_400 || send_packet >= 5 || resend_packets <= 2) {
                    this.suckTimes = 0;
                    return;
                } else {
                    this.suckTimes++;
                    this.recovage_times = 0;
                    this.mFailReason = 2;
                    loge("resend_packets=" + resend_packets);
                    return;
                }
            }
            this.recovage_times = 0;
            this.suckTimes++;
            this.mFailReason = 1;
            loge("rtt_duration /rtt_packets=" + (rtt_duration / rtt_packets));
            return;
        }
        this.mNeedChecked = true;
    }

    public String getSpeedInfo() {
        StringBuffer buffer = new StringBuffer();
        int listSize = this.counters.size();
        for (int i = 0; i < listSize; i++) {
            buffer.append(((HwCHRWifiCounterInfo) this.counters.get(i)).toString() + HwCHRWifiCPUUsage.COL_SEP);
        }
        buffer.append("suckTimes=" + this.suckTimes);
        buffer.append(" recovage_times=" + this.recovage_times);
        return buffer.toString();
    }

    private void loge(String str) {
        Log.e(TAG, str);
    }

    public int getFailReason() {
        return this.mFailReason;
    }

    public String getCountersInfo() {
        StringBuffer buffer = new StringBuffer();
        int listSize = this.counters.size();
        for (int i = 0; i < listSize; i++) {
            buffer.append(((HwCHRWifiCounterInfo) this.counters.get(i)).toString() + HwCHRWifiCPUUsage.COL_SEP);
        }
        return buffer.toString();
    }
}
