package com.huawei.device.connectivitychrlog;

import java.util.List;

public class CSegEVENT_WIFI_ACCESS_WEB_SLOWLY extends ChrLogBaseEventModel {
    private CSubApRoaming cApRoaming;
    private CSubBTStatus cBTStatus;
    private CSubCPUInfo cCPUInfo;
    private CSubCellID cCellID;
    private CSubDNS cDNS;
    private CSubMemInfo cMemInfo;
    private CSubNET_CFG cNET_CFG;
    private CSubPacketCount cPacketCount;
    private List<CSubRSSIGROUP_EVENT_EX> cRSSIGROUP_EVENT_EXList;
    private CSubTCP_STATIST cTCP_STATIST;
    private CSubTRAFFIC_GROUND cTRAFFIC_GROUND;
    private CSubWL_COUNTERS cWL_COUNTERS;
    public ENCEventId enEventId;
    public ENCWIFI_REPEATER_STATUS enWIFI_REPEATER_STATUS;
    public ENCWifiAccessWebSlowlyReason enWifiAccessWebSlowlyReason;
    public ENCucHwStatus enucHwStatus;
    public LogInt iAP_RSSI;
    public LogInt iRepeterFreq;
    public LogInt iTime_NetNormalTime;
    public LogInt iTime_NetSlowlyTime;
    public LogInt idetect_RTT_arp;
    public LogInt idetect_RTT_baidu;
    public LogInt ilost_beacon_amount;
    public LogInt imonitor_interval;
    public LogInt irx_beacon_from_assoc_ap;
    public LogInt irx_byte_amount;
    public LogInt irx_frame_amount;
    public LogInt isdio_info_ksoclrreq;
    public LogInt isdio_info_ksoclrretry;
    public LogInt isdio_info_ksosetreq;
    public LogInt isdio_info_ksosetretry;
    public LogInt isdio_info_readb;
    public LogInt isdio_info_readbreq;
    public LogInt isdio_info_readw;
    public LogInt isdio_info_readwreq;
    public LogInt isdio_info_writeb;
    public LogInt isdio_info_writebreq;
    public LogInt isdio_info_writew;
    public LogInt isdio_info_writewreq;
    public LogInt itx_byte_amount;
    public LogInt itx_data_frame_err_amount;
    public LogInt itx_frame_amount;
    public LogInt itx_retrans_amount;
    public LogString strAP_MAC;
    public LogString strAP_SSID;
    public LogString strAP_auth_alg;
    public LogString strAP_eap;
    public LogString strAP_group;
    public LogString strAP_key_mgmt;
    public LogString strAP_pairwise;
    public LogString strAP_proto;
    public LogString strCountryCode;
    public LogString strDNS_ADDRESS;
    public LogString strIP_LEASETIME;
    public LogString strIP_public;
    public LogString strInfo;
    public LogString strRoutes;
    public LogString strSTA_MAC;
    public LogString strUIDInfo;
    public LogString strWIFI_GATE;
    public LogString strWIFI_IP;
    public LogString strapVendorInfo;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogByte ucFailReason;
    public LogByte ucGWCount;
    public LogByte ucIsMobleAP;
    public LogByte ucIsOnScreen;
    public LogByte ucPortalStatus;
    public LogByte ucPublicEss;
    public LogByte ucScanAlwaysAvailble;
    public LogByte ucWIFIAlwaysNotifation;
    public LogByte ucWIFISleepPolicy;
    public LogByte ucWifiProStatus;
    public LogByte ucWifiToPDP;
    public LogByte ucap_distance;
    public LogByte ucdisturbing_degree;
    public LogByte ucisPortal;
    public LogByte uctraffic_aftersuspend;
    public LogShort usAP_channel;
    public LogShort usAP_link_speed;
    public LogShort usLen;
    public LogShort usSubErrorCode;

    public CSegEVENT_WIFI_ACCESS_WEB_SLOWLY() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_ACCESS_WEB_SLOWLY.<init>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_ACCESS_WEB_SLOWLY.<init>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_ACCESS_WEB_SLOWLY.<init>():void");
    }

    public void setCSubBTStatus(CSubBTStatus pBTStatus) {
        if (pBTStatus != null) {
            this.cBTStatus = pBTStatus;
            this.lengthMap.put("cBTStatus", Integer.valueOf(this.cBTStatus.getTotalBytes()));
            this.fieldMap.put("cBTStatus", this.cBTStatus);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubCellID(CSubCellID pCellID) {
        if (pCellID != null) {
            this.cCellID = pCellID;
            this.lengthMap.put("cCellID", Integer.valueOf(this.cCellID.getTotalBytes()));
            this.fieldMap.put("cCellID", this.cCellID);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubNET_CFG(CSubNET_CFG pNET_CFG) {
        if (pNET_CFG != null) {
            this.cNET_CFG = pNET_CFG;
            this.lengthMap.put("cNET_CFG", Integer.valueOf(this.cNET_CFG.getTotalBytes()));
            this.fieldMap.put("cNET_CFG", this.cNET_CFG);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubCPUInfo(CSubCPUInfo pCPUInfo) {
        if (pCPUInfo != null) {
            this.cCPUInfo = pCPUInfo;
            this.lengthMap.put("cCPUInfo", Integer.valueOf(this.cCPUInfo.getTotalBytes()));
            this.fieldMap.put("cCPUInfo", this.cCPUInfo);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubMemInfo(CSubMemInfo pMemInfo) {
        if (pMemInfo != null) {
            this.cMemInfo = pMemInfo;
            this.lengthMap.put("cMemInfo", Integer.valueOf(this.cMemInfo.getTotalBytes()));
            this.fieldMap.put("cMemInfo", this.cMemInfo);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubTRAFFIC_GROUND(CSubTRAFFIC_GROUND pTRAFFIC_GROUND) {
        if (pTRAFFIC_GROUND != null) {
            this.cTRAFFIC_GROUND = pTRAFFIC_GROUND;
            this.lengthMap.put("cTRAFFIC_GROUND", Integer.valueOf(this.cTRAFFIC_GROUND.getTotalBytes()));
            this.fieldMap.put("cTRAFFIC_GROUND", this.cTRAFFIC_GROUND);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubDNS(CSubDNS pDNS) {
        if (pDNS != null) {
            this.cDNS = pDNS;
            this.lengthMap.put("cDNS", Integer.valueOf(this.cDNS.getTotalBytes()));
            this.fieldMap.put("cDNS", this.cDNS);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubPacketCount(CSubPacketCount pPacketCount) {
        if (pPacketCount != null) {
            this.cPacketCount = pPacketCount;
            this.lengthMap.put("cPacketCount", Integer.valueOf(this.cPacketCount.getTotalBytes()));
            this.fieldMap.put("cPacketCount", this.cPacketCount);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubRSSIGROUP_EVENT_EXList(CSubRSSIGROUP_EVENT_EX pRSSIGROUP_EVENT_EX) {
        if (pRSSIGROUP_EVENT_EX != null) {
            this.cRSSIGROUP_EVENT_EXList.add(pRSSIGROUP_EVENT_EX);
            this.lengthMap.put("cRSSIGROUP_EVENT_EXList", Integer.valueOf((((ChrLogBaseModel) this.cRSSIGROUP_EVENT_EXList.get(0)).getTotalBytes() * this.cRSSIGROUP_EVENT_EXList.size()) + 2));
            this.fieldMap.put("cRSSIGROUP_EVENT_EXList", this.cRSSIGROUP_EVENT_EXList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubWL_COUNTERS(CSubWL_COUNTERS pWL_COUNTERS) {
        if (pWL_COUNTERS != null) {
            this.cWL_COUNTERS = pWL_COUNTERS;
            this.lengthMap.put("cWL_COUNTERS", Integer.valueOf(this.cWL_COUNTERS.getTotalBytes()));
            this.fieldMap.put("cWL_COUNTERS", this.cWL_COUNTERS);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubTCP_STATIST(CSubTCP_STATIST pTCP_STATIST) {
        if (pTCP_STATIST != null) {
            this.cTCP_STATIST = pTCP_STATIST;
            this.lengthMap.put("cTCP_STATIST", Integer.valueOf(this.cTCP_STATIST.getTotalBytes()));
            this.fieldMap.put("cTCP_STATIST", this.cTCP_STATIST);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubApRoaming(CSubApRoaming pApRoaming) {
        if (pApRoaming != null) {
            this.cApRoaming = pApRoaming;
            this.lengthMap.put("cApRoaming", Integer.valueOf(this.cApRoaming.getTotalBytes()));
            this.fieldMap.put("cApRoaming", this.cApRoaming);
            this.usLen.setValue(getTotalLen());
        }
    }
}
