package com.android.server.wifi;

import com.huawei.device.connectivitychrlog.CSubTCP_STATIST;
import java.util.List;

public class HwCHRWebSpeed extends HwCHRWifiSpeedBaseChecker {
    private static final String TAG = "WebSpeed";
    private static HwCHRWebSpeed instance;

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.HwCHRWebSpeed.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.HwCHRWebSpeed.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.HwCHRWebSpeed.<clinit>():void");
    }

    public /* bridge */ /* synthetic */ long getCounterDetaByTab(String tag) {
        return super.getCounterDetaByTab(tag);
    }

    public /* bridge */ /* synthetic */ String getCountersInfo() {
        return super.getCountersInfo();
    }

    public /* bridge */ /* synthetic */ int getFailReason() {
        return super.getFailReason();
    }

    public /* bridge */ /* synthetic */ int getOld() {
        return super.getOld();
    }

    public /* bridge */ /* synthetic */ String getSpeedInfo() {
        return super.getSpeedInfo();
    }

    public /* bridge */ /* synthetic */ int getSuckTimes() {
        return super.getSuckTimes();
    }

    public /* bridge */ /* synthetic */ int old() {
        return super.old();
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
        for (int i = startLine; i < line.size(); i++) {
            String item = (String) line.get(i);
            for (int j = 0; j < this.counters.size(); j++) {
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
