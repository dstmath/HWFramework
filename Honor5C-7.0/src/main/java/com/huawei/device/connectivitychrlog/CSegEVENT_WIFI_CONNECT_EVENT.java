package com.huawei.device.connectivitychrlog;

import java.util.List;

public class CSegEVENT_WIFI_CONNECT_EVENT extends ChrLogBaseEventModel {
    public List<CSubAssoc_Chr_Event> cAssoc_Chr_EventList;
    private List<CSubAuth_Chr_Event> cAuth_Chr_EventList;
    private List<CSubDHCP_Chr_Event> cDHCP_Chr_EventList;
    private List<CSubRSSIGROUP_EVENT> cRSSIGROUP_EVENTList;
    public ENCEventId enEventId;
    public ENCTriggerReason enTriggerReason;
    public ENCconnect_type enconnect_type;
    public ENCucHwStatus enucHwStatus;
    public LogInt iAP_RSSI;
    public LogInt iconnect_success_time;
    public LogLong lTimeStamp1;
    public LogLong lTimeStamp2;
    public LogLong lTimeStamp3;
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
    public LogString strProxySettingInfo;
    public LogString strRoutes;
    public LogString strSTA_MAC;
    public LogString strThreadNameConnectAP;
    public LogString strThreadNameDisableAP;
    public LogString strWIFI_GATE;
    public LogString strWIFI_IP;
    public LogString strapVendorInfo;
    public LogDate tmTimeStamp;
    public LogByte ucBTConnState;
    public LogByte ucBTState;
    public LogByte ucCardIndex;
    public LogByte ucIsMobleAP;
    public LogByte ucIsOnScreen;
    public LogByte ucProxySettings;
    public LogByte ucPublicEss;
    public LogByte ucScanAlwaysAvailble;
    public LogByte ucWIFIAlwaysNotifation;
    public LogByte ucWIFISleepPolicy;
    public LogByte ucWifiProStatus;
    public LogByte ucWifiToPDP;
    public LogShort usAP_channel;
    public LogShort usAP_link_speed;
    public LogShort usLen;

    public CSegEVENT_WIFI_CONNECT_EVENT() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_CONNECT_EVENT.<init>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_CONNECT_EVENT.<init>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_CONNECT_EVENT.<init>():void");
    }

    public void setCSubAssoc_Chr_EventList(CSubAssoc_Chr_Event pAssoc_Chr_Event) {
        if (pAssoc_Chr_Event != null) {
            this.cAssoc_Chr_EventList.add(pAssoc_Chr_Event);
            this.lengthMap.put("cAssoc_Chr_EventList", Integer.valueOf((((ChrLogBaseModel) this.cAssoc_Chr_EventList.get(0)).getTotalBytes() * this.cAssoc_Chr_EventList.size()) + 2));
            this.fieldMap.put("cAssoc_Chr_EventList", this.cAssoc_Chr_EventList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubAuth_Chr_EventList(CSubAuth_Chr_Event pAuth_Chr_Event) {
        if (pAuth_Chr_Event != null) {
            this.cAuth_Chr_EventList.add(pAuth_Chr_Event);
            this.lengthMap.put("cAuth_Chr_EventList", Integer.valueOf((((ChrLogBaseModel) this.cAuth_Chr_EventList.get(0)).getTotalBytes() * this.cAuth_Chr_EventList.size()) + 2));
            this.fieldMap.put("cAuth_Chr_EventList", this.cAuth_Chr_EventList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubDHCP_Chr_EventList(CSubDHCP_Chr_Event pDHCP_Chr_Event) {
        if (pDHCP_Chr_Event != null) {
            this.cDHCP_Chr_EventList.add(pDHCP_Chr_Event);
            this.lengthMap.put("cDHCP_Chr_EventList", Integer.valueOf((((ChrLogBaseModel) this.cDHCP_Chr_EventList.get(0)).getTotalBytes() * this.cDHCP_Chr_EventList.size()) + 2));
            this.fieldMap.put("cDHCP_Chr_EventList", this.cDHCP_Chr_EventList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubRSSIGROUP_EVENTList(CSubRSSIGROUP_EVENT pRSSIGROUP_EVENT) {
        if (pRSSIGROUP_EVENT != null) {
            this.cRSSIGROUP_EVENTList.add(pRSSIGROUP_EVENT);
            this.lengthMap.put("cRSSIGROUP_EVENTList", Integer.valueOf((((ChrLogBaseModel) this.cRSSIGROUP_EVENTList.get(0)).getTotalBytes() * this.cRSSIGROUP_EVENTList.size()) + 2));
            this.fieldMap.put("cRSSIGROUP_EVENTList", this.cRSSIGROUP_EVENTList);
            this.usLen.setValue(getTotalLen());
        }
    }
}
