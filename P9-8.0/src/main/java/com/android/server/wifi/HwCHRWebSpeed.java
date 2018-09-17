package com.android.server.wifi;

import com.huawei.device.connectivitychrlog.CSubTCP_STATIST;
import java.util.List;

public class HwCHRWebSpeed extends HwCHRWifiSpeedBaseChecker {
    private static final String TAG = "WebSpeed";
    private static HwCHRWebSpeed instance = null;

    private static class VerticalCounterInfo extends HwCHRWifiCounterInfo {
        public VerticalCounterInfo(String info) {
            super(info);
        }

        public void parserValue(String Line, String cols) {
            try {
                this.mDelta = Long.parseLong(Line.replace(this.mTag + this.mOperate, ""));
            } catch (NumberFormatException e) {
            }
        }
    }

    public HwCHRWebSpeed() {
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.WEB_SENDSEGS));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.WEB_RESENDSEGS));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.WEB_RECVSEGS));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.WEB_ERRSEGS));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.WEB_OUTRSTS));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.WEB_ESTRSTS));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.WEB_RTT_DURATION));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.WEB_RTT_PACKETS));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.WEB_SRTT));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.WEB_DUP_ACKS));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.LAN_SENDSEGS));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.LAN_RESENDSEGS));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.LAN_RECVSEGS));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.LAN_ERRSEGS));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.LAN_OUTRSTS));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.LAN_ESTRSTS));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.LAN_RTT_DURATION));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.LAN_RTT_PACKETS));
        this.counters.add(new VerticalCounterInfo(HwCHRWifiSpeedBaseChecker.LAN_DUP_ACKS));
    }

    public void parser_file(List<String> line, int startLine) {
        int lineSize = line.size();
        int counterSize = this.counters.size();
        for (int i = startLine; i < lineSize; i++) {
            String item = (String) line.get(i);
            for (int j = 0; j < counterSize; j++) {
                if (((HwCHRWifiCounterInfo) this.counters.get(j)).match(item)) {
                    ((HwCHRWifiCounterInfo) this.counters.get(j)).parserValue(item, "");
                }
            }
        }
    }

    public void parse_line(List<String> lines, int startLine) {
        parser_file(lines, startLine);
        checkSuckStatus();
        this.mNeedChecked = true;
    }

    public void renew() {
        this.age = 2;
    }

    public String toString() {
        return "WebSpeed [" + getSpeedInfo() + "]";
    }

    public static synchronized HwCHRWebSpeed getDefault() {
        HwCHRWebSpeed hwCHRWebSpeed;
        synchronized (HwCHRWebSpeed.class) {
            if (instance == null) {
                instance = new HwCHRWebSpeed();
            }
            hwCHRWebSpeed = instance;
        }
        return hwCHRWebSpeed;
    }

    public CSubTCP_STATIST getTcpStatistCHR() {
        CSubTCP_STATIST tcp = new CSubTCP_STATIST();
        tcp.isend_packets.setValue((int) getCounterDetaByTab(HwCHRWifiSpeedBaseChecker.WEB_SENDSEGS));
        tcp.iresend_packets.setValue((int) getCounterDetaByTab(HwCHRWifiSpeedBaseChecker.WEB_RESENDSEGS));
        tcp.irecv_packets.setValue((int) getCounterDetaByTab(HwCHRWifiSpeedBaseChecker.WEB_RECVSEGS));
        tcp.irecv_err_packets.setValue((int) getCounterDetaByTab(HwCHRWifiSpeedBaseChecker.WEB_ERRSEGS));
        tcp.irtt_duration.setValue((int) getCounterDetaByTab(HwCHRWifiSpeedBaseChecker.WEB_RTT_DURATION));
        tcp.irtt_packets.setValue((int) getCounterDetaByTab(HwCHRWifiSpeedBaseChecker.WEB_RTT_PACKETS));
        tcp.idup_ack.setValue((int) getCounterDetaByTab(HwCHRWifiSpeedBaseChecker.WEB_DUP_ACKS));
        tcp.iout_rst.setValue((int) getCounterDetaByTab(HwCHRWifiSpeedBaseChecker.WEB_OUTRSTS));
        tcp.iest_rst.setValue((int) getCounterDetaByTab(HwCHRWifiSpeedBaseChecker.WEB_ESTRSTS));
        return tcp;
    }
}
