package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_STABILITY_SSIDSTAT extends ChrLogBaseEventModel {
    public ENCEventId enEventId;
    public LogInt iAbnormalDisconnCount;
    public LogInt iAccessWebCnt;
    public LogInt iAccessWebFailedPortal;
    public LogInt iAccessWebReDHCPFailedPortal;
    public LogInt iAccessWebRoamingFailedPortal;
    public LogInt iAccessWebSlowlyCnt;
    public LogInt iAccessWebSuccCnt;
    public LogInt iAppDisabledAbnromalCnt;
    public LogInt iAppDisabledScSuccCnt;
    public LogInt iArpReassocOkCnt;
    public LogInt iArpUnreachableCnt;
    public LogInt iAssocByABSCnt;
    public LogInt iAssocCount;
    public LogInt iAssocDuration;
    public LogInt iAssocRejectAccessFullCnt;
    public LogInt iAssocRejectedAbnormalCnt;
    public LogInt iAssocRejectedScSuccCnt;
    public LogInt iAssocSuccCount;
    public LogInt iAuthCount;
    public LogInt iAuthDuration;
    public LogInt iAuthFailedAbnormalCnt;
    public LogInt iAuthFailedScSuccCnt;
    public LogInt iAuthSuccCount;
    public LogInt iCHRConnectingDuration;
    public LogInt iConnectTotalCount;
    public LogInt iConnectedCount;
    public LogInt iConnectedDuration;
    public LogInt iDHCPStaticAccessCount;
    public LogInt iDhcpCount;
    public LogInt iDhcpDuration;
    public LogInt iDhcpFailedAbnormalCnt;
    public LogInt iDhcpFailedScSuccCnt;
    public LogInt iDhcpStaticCount;
    public LogInt iDhcpSuccCount;
    public LogInt iDisconnectCnt;
    public LogInt iDnsAbnormalCnt;
    public LogInt iDnsParseFailCnt;
    public LogInt iDnsScSuccCnt;
    public LogInt iFirstConnInternetFailCnt;
    public LogInt iFirstConnInternetFailDuration;
    public LogInt iGatewayAbnormalCnt;
    public LogInt iGoodReConnectCnt;
    public LogInt iGoodReConnectSuccCnt;
    public LogInt iNoUserProcRunCnt;
    public LogInt iOnSceenConnectedDuration;
    public LogInt iOnSceenReConnectedCnt;
    public LogInt iOnScreenAbDisconnectCnt;
    public LogInt iOnScreenConnectCnt;
    public LogInt iOnScreenConnectedCnt;
    public LogInt iOnScreenDisconnectCnt;
    public LogInt iOnlyTheTxNoRxCnt;
    public LogInt iReDHCPAccessWebSuccCnt;
    public LogInt iReDHCPCnt;
    public LogInt iReDHCPDuration;
    public LogInt iReDHCPSuccCnt;
    public LogInt iReDhcpScSuccCnt;
    public LogInt iReKeyCnt;
    public LogInt iReKeyDuration;
    public LogInt iReKeySuccCnt;
    public LogInt iReassocScSuccCnt;
    public LogInt iResetScSuccCnt;
    public LogInt iRoamingAbnormalCnt;
    public LogInt iRoamingAccessWebSuccCnt;
    public LogInt iRoamingCnt;
    public LogInt iRoamingDuration;
    public LogInt iRoamingSuccCnt;
    public LogInt iStaticIpScSuccCnt;
    public LogInt iTcpRxAbnormalCnt;
    public LogInt iUserEnableStaticIpCnt;
    public LogInt iWeakReConnectCnt;
    public LogInt iWeakReConnectSuccCnt;
    public LogString strAP_auth_alg;
    public LogString strAP_eap;
    public LogString strAP_group;
    public LogString strAP_key_mgmt;
    public LogString strAP_pairwise;
    public LogString strAP_proto;
    public LogString strBSSID;
    public LogString strProxySettingInfo;
    public LogString strSSID;
    public LogString strapVendorInfo;
    public LogDate tmTimeLastUpdateStamp;
    public LogDate tmTimeStamp;
    public LogDate tmTimeStartedStamp;
    public LogByte ucCardIndex;
    public LogByte ucMultiGWCount;
    public LogByte ucProxySettings;
    public LogShort usLen;

    public CSegEVENT_WIFI_STABILITY_SSIDSTAT() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_STABILITY_SSIDSTAT.<init>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_STABILITY_SSIDSTAT.<init>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_STABILITY_SSIDSTAT.<init>():void");
    }
}
