package com.huawei.device.connectivitychrlog;

import java.util.List;

public class CSegEVENT_WIFI_ABNORMAL_DISCONNECT_EX extends ChrLogBaseEventModel {
    public LogByteArray aucExt_info;
    public List<CSubRSSIGROUP_EVENT_EX> cRSSIGROUP_EVENTList;
    public ENCABNORMAL_DISCONNECT enABNORMAL_DISCONNECT;
    public ENCEventId enEventId;
    public ENCucHwStatus enucHwStatus;
    public LogInt iAP_RSSI;
    public LogString strAP_MAC;
    public LogString strAP_SSID;
    public LogString strAP_auth_alg;
    public LogString strAP_eap;
    public LogString strAP_group;
    public LogString strAP_key_mgmt;
    public LogString strAP_pairwise;
    public LogString strAP_proto;
    public LogString strDNS_ADDRESS;
    public LogString strIP_LEASETIME;
    public LogString strRoutes;
    public LogString strSTA_MAC;
    public LogString strWIFI_GATE;
    public LogString strWIFI_IP;
    public LogString strapVendorInfo;
    public LogDate tmTimeStamp;
    public LogByte ucAntCurWork;
    public LogByte ucBTConnState;
    public LogByte ucBTState;
    public LogByte ucCardIndex;
    public LogByte ucIsAntSWCauseBreak;
    public LogByte ucIsMobleAP;
    public LogByte ucIsOnScreen;
    public LogByte ucPublicEss;
    public LogByte ucScanAlwaysAvailble;
    public LogByte ucWIFIAlwaysNotifation;
    public LogByte ucWIFISleepPolicy;
    public LogByte ucWifiProStatus;
    public LogByte ucWifiToPDP;
    public LogShort usAP_channel;
    public LogShort usAP_link_speed;
    public LogShort usLen;
    public LogShort usSubErrorCode;

    public CSegEVENT_WIFI_ABNORMAL_DISCONNECT_EX() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_ABNORMAL_DISCONNECT_EX.<init>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_ABNORMAL_DISCONNECT_EX.<init>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_ABNORMAL_DISCONNECT_EX.<init>():void");
    }

    public void setCSubRSSIGROUP_EVENTList(CSubRSSIGROUP_EVENT_EX pRSSIGROUP_EVENT) {
        if (pRSSIGROUP_EVENT != null) {
            this.cRSSIGROUP_EVENTList.add(pRSSIGROUP_EVENT);
            this.lengthMap.put("cRSSIGROUP_EVENTList", Integer.valueOf((((ChrLogBaseModel) this.cRSSIGROUP_EVENTList.get(0)).getTotalBytes() * this.cRSSIGROUP_EVENTList.size()) + 2));
            this.fieldMap.put("cRSSIGROUP_EVENTList", this.cRSSIGROUP_EVENTList);
            this.usLen.setValue(getTotalLen());
        }
    }
}
