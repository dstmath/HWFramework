package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_WIFIPRO_STATISTICS_EVENT extends ChrLogBaseEventModel {
    public ENCEventId enEventId;
    public ENCWifiUserType enWifiUserType;
    public LogInt iAutoRI_TotTime;
    public LogInt iEnableTotTime;
    public LogInt iHistoryTotWifiConnHour;
    public LogInt iManualRI_TotTime;
    public LogInt iNotInetRO_DISCONNECT_TotData;
    public LogInt iNotInet_AutoRI_TotData;
    public LogInt iQOE_AutoRI_TotData;
    public LogInt iQOE_RO_DISCONNECT_TotData;
    public LogInt iRO_TotMobileData;
    public LogInt iStatIntervalTime;
    public LogInt iTotWifiConnectTime;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogByte ucManualConnBlockPortalCount;
    public LogByte ucWifiproCloseCount;
    public LogByte ucWifiproOpenCount;
    public LogByte ucWifiproStateAtReportTime;
    public LogShort usAF_AutoLoginFailCnt;
    public LogShort usAF_AutoLoginSuccCnt;
    public LogShort usAF_FPNSuccNotMsmCnt;
    public LogShort usAF_PasswordFailCnt;
    public LogShort usAF_PasswordSuccCnt;
    public LogShort usAF_PhoneNumFailCnt;
    public LogShort usAF_PhoneNumSuccCnt;
    public LogShort usActiveCheckRS_Diff;
    public LogShort usActiveCheckRS_Same;
    public LogShort usAutoRI_TotCount;
    public LogShort usBG_AUTH_FailCnt;
    public LogShort usBG_AssocRejectCnt;
    public LogShort usBG_BgRunCnt;
    public LogShort usBG_ConntTimeoutCnt;
    public LogShort usBG_DHCPFailCnt;
    public LogShort usBG_DNSFailCnt;
    public LogShort usBG_FailedCnt;
    public LogShort usBG_FishingApCnt;
    public LogShort usBG_FoundTwoMoreApCnt;
    public LogShort usBG_FreeInetOkApCnt;
    public LogShort usBG_FreeNotInetApCnt;
    public LogShort usBG_InetNotOkActiveOk;
    public LogShort usBG_InetOkActiveNotOk;
    public LogShort usBG_NCByCheckFail;
    public LogShort usBG_NCByConnectFail;
    public LogShort usBG_NCByStateErr;
    public LogShort usBG_NCByUnknown;
    public LogShort usBG_PortalApCnt;
    public LogShort usBG_SettingRunCnt;
    public LogShort usBG_UserSelApFishingCnt;
    public LogShort usBG_UserSelFreeInetOkCnt;
    public LogShort usBG_UserSelNoInetCnt;
    public LogShort usBG_UserSelPortalCnt;
    public LogShort usBMD_FiftyMNotifyCount;
    public LogShort usBMD_FiftyM_RI_Count;
    public LogShort usBMD_TenMNotifyCount;
    public LogShort usBMD_TenM_RI_Count;
    public LogShort usBMD_UserDelNotifyCount;
    public LogShort usBQE_BadSettingCancel;
    public LogShort usBQE_BindWlanFailCount;
    public LogShort usBQE_CNUrl1FailCount;
    public LogShort usBQE_CNUrl2FailCount;
    public LogShort usBQE_CNUrl3FailCount;
    public LogShort usBQE_NCNUrl1FailCount;
    public LogShort usBQE_NCNUrl2FailCount;
    public LogShort usBQE_NCNUrl3FailCount;
    public LogShort usBQE_ScoreUnknownCount;
    public LogShort usBQE_StopBqeFailCount;
    public LogShort usBSG_EndIn4s7sCnt;
    public LogShort usBSG_EndIn4sCnt;
    public LogShort usBSG_NotEndIn7sCnt;
    public LogShort usBSG_RsBadCnt;
    public LogShort usBSG_RsGoodCnt;
    public LogShort usBSG_RsMidCnt;
    public LogShort usBigRTT_ErrRO_Tot;
    public LogShort usBigRTT_RO_Tot;
    public LogShort usCellAutoCloseCount;
    public LogShort usCellAutoOpenCount;
    public LogShort usCustomizedScan_FailCount;
    public LogShort usCustomizedScan_SuccCount;
    public LogShort usHandoverPingpongCount;
    public LogShort usHandoverToBad5GCount;
    public LogShort usHandoverToNotInet5GCount;
    public LogShort usHandoverTooSlowCount;
    public LogShort usHighDataRateStopROC;
    public LogShort usHisScoRI_Count;
    public LogShort usHomeAPAddRoPeriodCnt;
    public LogShort usHomeAPQoeBadCnt;
    public LogShort usLen;
    public LogShort usManualBackROC;
    public LogShort usMixedAP_DisapperCount;
    public LogShort usMixedAP_HandoverFailCount;
    public LogShort usMixedAP_HandoverSucCount;
    public LogShort usMixedAP_HighFreqScan5GCount;
    public LogShort usMixedAP_InblacklistCount;
    public LogShort usMixedAP_LearnedCount;
    public LogShort usMixedAP_LowFreqScan5GCount;
    public LogShort usMixedAP_MidFreqScan5GCount;
    public LogShort usMixedAP_MonitorCount;
    public LogShort usMixedAP_NearbyCount;
    public LogShort usMixedAP_SatisfiedCount;
    public LogShort usMixedAP_ScoreNotSatisfyCount;
    public LogShort usNoInetAlarmCount;
    public LogShort usNoInetAlarmOnConnCnt;
    public LogShort usNoInetHandoverCount;
    public LogShort usNotAutoConnPortalCnt;
    public LogShort usNotInetRO_DISCONNECT_Cnt;
    public LogShort usNotInetRestoreRI;
    public LogShort usNotInetSettingCancel;
    public LogShort usNotInetUserCancel;
    public LogShort usNotInetUserManualRI;
    public LogShort usNotInetWifiToWifiCount;
    public LogShort usOTA_ErrRO_Tot;
    public LogShort usOTA_RO_Tot;
    public LogShort usPingPongCount;
    public LogShort usPortalAutoLoginCount;
    public LogShort usPortalCodeParseCount;
    public LogShort usPortalNoAutoConnCnt;
    public LogShort usPortalUnauthCount;
    public LogShort usQOE_RO_DISCONNECT_Cnt;
    public LogShort usRSSI_BetterRI_Count;
    public LogShort usRSSI_ErrRO_Tot;
    public LogShort usRSSI_RO_Tot;
    public LogShort usRSSI_RestoreRI_Count;
    public LogShort usRcvSMS_Count;
    public LogShort usReopenWifiRICount;
    public LogShort usSelCSPAutoSwCount;
    public LogShort usSelCSPNotSwCount;
    public LogShort usSelCSPShowDiglogCount;
    public LogShort usSelectNotInetAPCount;
    public LogShort usSingleAP_DisapperCount;
    public LogShort usSingleAP_HandoverFailCount;
    public LogShort usSingleAP_HandoverSucCount;
    public LogShort usSingleAP_HighFreqScan5GCount;
    public LogShort usSingleAP_InblacklistCount;
    public LogShort usSingleAP_LearnedCount;
    public LogShort usSingleAP_LowFreqScan5GCount;
    public LogShort usSingleAP_MidFreqScan5GCount;
    public LogShort usSingleAP_MonitorCount;
    public LogShort usSingleAP_NearbyCount;
    public LogShort usSingleAP_SatisfiedCount;
    public LogShort usSingleAP_ScoreNotSatisfyCount;
    public LogShort usTCP_ErrRO_Tot;
    public LogShort usTCP_RO_Tot;
    public LogShort usTimerRI_Count;
    public LogShort usTotAPRecordCnt;
    public LogShort usTotBtnRICount;
    public LogShort usTotHomeAPCnt;
    public LogShort usTotalBQE_BadROC;
    public LogShort usTotalPortalAuthSuccCount;
    public LogShort usTotalPortalConnCount;
    public LogShort usUserCancelROC;
    public LogShort usUserRejectHandoverCount;
    public LogShort usUserUseBgScanAPCount;
    public LogShort usWifiOobInitState;
    public LogShort usWifiScoCount;
    public LogShort usWifiToWifiSuccCount;

    public CSegEVENT_WIFI_WIFIPRO_STATISTICS_EVENT() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_WIFIPRO_STATISTICS_EVENT.<init>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_WIFIPRO_STATISTICS_EVENT.<init>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_WIFIPRO_STATISTICS_EVENT.<init>():void");
    }
}
