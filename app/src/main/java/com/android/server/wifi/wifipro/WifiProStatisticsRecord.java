package com.android.server.wifi.wifipro;

public class WifiProStatisticsRecord {
    private static final String TAG = "WifiProStatisticsRecord";
    public static final String WIFIPRO_DEAULT_STR = "DEAULT_STR";
    public static final short WIFIPRO_STATE_DISABLE = (short) 2;
    public static final short WIFIPRO_STATE_ENABLE = (short) 1;
    public static final short WIFIPRO_STATE_UNKNOW = (short) 0;
    public short mAF_AutoLoginFailCnt;
    public short mAF_AutoLoginSuccCnt;
    public short mAF_FPNSuccNotMsmCnt;
    public short mAF_PasswordFailCnt;
    public short mAF_PasswordSuccCnt;
    public short mAF_PhoneNumFailCnt;
    public short mAF_PhoneNumSuccCnt;
    public short mActiveCheckRS_Diff;
    public short mActiveCheckRS_Same;
    public short mAutoRI_TotCount;
    public int mAutoRI_TotTime;
    public short mBG_AUTH_FailCnt;
    public short mBG_AssocRejectCnt;
    public short mBG_BgRunCnt;
    public short mBG_ConntTimeoutCnt;
    public short mBG_DHCPFailCnt;
    public short mBG_DNSFailCnt;
    public short mBG_FailedCnt;
    public short mBG_FishingApCnt;
    public short mBG_FoundTwoMoreApCnt;
    public short mBG_FreeInetOkApCnt;
    public short mBG_FreeNotInetApCnt;
    public short mBG_InetNotOkActiveOk;
    public short mBG_InetOkActiveNotOk;
    public short mBG_NCByCheckFail;
    public short mBG_NCByConnectFail;
    public short mBG_NCByStateErr;
    public short mBG_NCByUnknown;
    public short mBG_PortalApCnt;
    public short mBG_SettingRunCnt;
    public short mBG_UserSelApFishingCnt;
    public short mBG_UserSelFreeInetOkCnt;
    public short mBG_UserSelNoInetCnt;
    public short mBG_UserSelPortalCnt;
    public short mBMD_FiftyMNotifyCount;
    public short mBMD_FiftyM_RI_Count;
    public short mBMD_TenMNotifyCount;
    public short mBMD_TenM_RI_Count;
    public short mBMD_UserDelNotifyCount;
    public short mBQE_BadSettingCancel;
    public short mBQE_BindWlanFailCount;
    public short mBQE_CNUrl1FailCount;
    public short mBQE_CNUrl2FailCount;
    public short mBQE_CNUrl3FailCount;
    public short mBQE_NCNUrl1FailCount;
    public short mBQE_NCNUrl2FailCount;
    public short mBQE_NCNUrl3FailCount;
    public short mBQE_ScoreUnknownCount;
    public short mBQE_StopBqeFailCount;
    public short mBSG_EndIn4s7sCnt;
    public short mBSG_EndIn4sCnt;
    public short mBSG_NotEndIn7sCnt;
    public short mBSG_RsBadCnt;
    public short mBSG_RsGoodCnt;
    public short mBSG_RsMidCnt;
    public short mBigRTT_ErrRO_Tot;
    public short mBigRTT_RO_Tot;
    public short mCellAutoCloseCount;
    public short mCellAutoOpenCount;
    public short mCustomizedScan_FailCount;
    public short mCustomizedScan_SuccCount;
    public int mEnableTotTime;
    public short mHandoverPingpongCount;
    public short mHandoverToBad5GCount;
    public short mHandoverToNotInet5GCount;
    public short mHandoverTooSlowCount;
    public short mHighDataRateStopROC;
    public short mHisScoRI_Count;
    public int mHistoryTotWifiConnHour;
    public short mHomeAPAddRoPeriodCnt;
    public short mHomeAPQoeBadCnt;
    public String mLastStatUploadTime;
    public short mLastWifiproState;
    public String mLastWifiproStateUpdateTime;
    public short mManualBackROC;
    public short mManualConnBlockPortalCount;
    public int mManualRI_TotTime;
    public short mMixedAP_DisapperCount;
    public short mMixedAP_HandoverFailCount;
    public short mMixedAP_HandoverSucCount;
    public short mMixedAP_HighFreqScan5GCount;
    public short mMixedAP_InblacklistCount;
    public short mMixedAP_LearnedCount;
    public short mMixedAP_LowFreqScan5GCount;
    public short mMixedAP_MidFreqScan5GCount;
    public short mMixedAP_MonitorCount;
    public short mMixedAP_NearbyCount;
    public short mMixedAP_SatisfiedCount;
    public short mMixedAP_ScoreNotSatisfyCount;
    public short mNoInetAlarmCount;
    public short mNoInetAlarmOnConnCnt;
    public short mNoInetHandoverCount;
    public short mNotAutoConnPortalCnt;
    public short mNotInetRO_DISCONNECT_Cnt;
    public int mNotInetRO_DISCONNECT_TotData;
    public short mNotInetRestoreRI;
    public short mNotInetSettingCancel;
    public short mNotInetUserCancel;
    public short mNotInetUserManualRI;
    public short mNotInetWifiToWifiCount;
    public int mNotInet_AutoRI_TotData;
    public short mOTA_ErrRO_Tot;
    public short mOTA_RO_Tot;
    public short mPingPongCount;
    public short mPortalAutoLoginCount;
    public short mPortalCodeParseCount;
    public short mPortalNoAutoConnCnt;
    public short mPortalUnauthCount;
    public int mQOE_AutoRI_TotData;
    public short mQOE_RO_DISCONNECT_Cnt;
    public int mQOE_RO_DISCONNECT_TotData;
    public int mRO_TotMobileData;
    public short mRSSI_BetterRI_Count;
    public short mRSSI_ErrRO_Tot;
    public short mRSSI_RO_Tot;
    public short mRSSI_RestoreRI_Count;
    public short mRcvSMS_Count;
    public short mReopenWifiRICount;
    public short mSelCSPAutoSwCount;
    public short mSelCSPNotSwCount;
    public short mSelCSPShowDiglogCount;
    public short mSelectNotInetAPCount;
    public short mSingleAP_DisapperCount;
    public short mSingleAP_HandoverFailCount;
    public short mSingleAP_HandoverSucCount;
    public short mSingleAP_HighFreqScan5GCount;
    public short mSingleAP_InblacklistCount;
    public short mSingleAP_LearnedCount;
    public short mSingleAP_LowFreqScan5GCount;
    public short mSingleAP_MidFreqScan5GCount;
    public short mSingleAP_MonitorCount;
    public short mSingleAP_NearbyCount;
    public short mSingleAP_SatisfiedCount;
    public short mSingleAP_ScoreNotSatisfyCount;
    public short mTCP_ErrRO_Tot;
    public short mTCP_RO_Tot;
    public short mTimerRI_Count;
    public short mTotAPRecordCnt;
    public short mTotBtnRICount;
    public short mTotHomeAPCnt;
    public int mTotWifiConnectTime;
    public short mTotalBQE_BadROC;
    public short mTotalPortalAuthSuccCount;
    public short mTotalPortalConnCount;
    public short mUserCancelROC;
    public short mUserRejectHandoverCount;
    public short mUserUseBgScanAPCount;
    public short mWifiOobInitState;
    public short mWifiScoCount;
    public short mWifiToWifiSuccCount;
    public short mWifiproCloseCount;
    public short mWifiproOpenCount;
    public short mWifiproStateAtReportTime;

    public void dumpAllChrStatRecord() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.WifiProStatisticsRecord.dumpAllChrStatRecord():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.WifiProStatisticsRecord.dumpAllChrStatRecord():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.WifiProStatisticsRecord.dumpAllChrStatRecord():void");
    }

    public WifiProStatisticsRecord() {
        resetRecord();
    }

    public void resetRecord() {
        this.mLastStatUploadTime = WIFIPRO_DEAULT_STR;
        this.mLastWifiproState = (short) 0;
        this.mLastWifiproStateUpdateTime = WIFIPRO_DEAULT_STR;
        this.mEnableTotTime = 0;
        this.mNoInetHandoverCount = (short) 0;
        this.mPortalUnauthCount = (short) 0;
        this.mWifiScoCount = (short) 0;
        this.mPortalCodeParseCount = (short) 0;
        this.mRcvSMS_Count = (short) 0;
        this.mPortalAutoLoginCount = (short) 0;
        this.mCellAutoOpenCount = (short) 0;
        this.mCellAutoCloseCount = (short) 0;
        this.mTotalBQE_BadROC = (short) 0;
        this.mManualBackROC = (short) 0;
        this.mRSSI_RO_Tot = (short) 0;
        this.mRSSI_ErrRO_Tot = (short) 0;
        this.mOTA_RO_Tot = (short) 0;
        this.mOTA_ErrRO_Tot = (short) 0;
        this.mTCP_RO_Tot = (short) 0;
        this.mTCP_ErrRO_Tot = (short) 0;
        this.mManualRI_TotTime = 0;
        this.mAutoRI_TotTime = 0;
        this.mAutoRI_TotCount = (short) 0;
        this.mRSSI_RestoreRI_Count = (short) 0;
        this.mRSSI_BetterRI_Count = (short) 0;
        this.mTimerRI_Count = (short) 0;
        this.mHisScoRI_Count = (short) 0;
        this.mUserCancelROC = (short) 0;
        this.mWifiToWifiSuccCount = (short) 0;
        this.mNoInetAlarmCount = (short) 0;
        this.mWifiOobInitState = (short) 0;
        this.mNotAutoConnPortalCnt = (short) 0;
        this.mHighDataRateStopROC = (short) 0;
        this.mSelectNotInetAPCount = (short) 0;
        this.mUserUseBgScanAPCount = (short) 0;
        this.mPingPongCount = (short) 0;
        this.mBQE_BadSettingCancel = (short) 0;
        this.mNotInetSettingCancel = (short) 0;
        this.mNotInetUserCancel = (short) 0;
        this.mNotInetRestoreRI = (short) 0;
        this.mNotInetUserManualRI = (short) 0;
        this.mNotInetWifiToWifiCount = (short) 0;
        this.mReopenWifiRICount = (short) 0;
        this.mSelCSPShowDiglogCount = (short) 0;
        this.mSelCSPAutoSwCount = (short) 0;
        this.mSelCSPNotSwCount = (short) 0;
        this.mTotBtnRICount = (short) 0;
        this.mBMD_TenMNotifyCount = (short) 0;
        this.mBMD_TenM_RI_Count = (short) 0;
        this.mBMD_FiftyMNotifyCount = (short) 0;
        this.mBMD_FiftyM_RI_Count = (short) 0;
        this.mBMD_UserDelNotifyCount = (short) 0;
        this.mRO_TotMobileData = 0;
        this.mAF_PhoneNumSuccCnt = (short) 0;
        this.mAF_PhoneNumFailCnt = (short) 0;
        this.mAF_PasswordSuccCnt = (short) 0;
        this.mAF_PasswordFailCnt = (short) 0;
        this.mAF_AutoLoginSuccCnt = (short) 0;
        this.mAF_AutoLoginFailCnt = (short) 0;
        this.mBG_BgRunCnt = (short) 0;
        this.mBG_SettingRunCnt = (short) 0;
        this.mBG_FreeInetOkApCnt = (short) 0;
        this.mBG_FishingApCnt = (short) 0;
        this.mBG_FreeNotInetApCnt = (short) 0;
        this.mBG_PortalApCnt = (short) 0;
        this.mBG_FailedCnt = (short) 0;
        this.mBG_InetNotOkActiveOk = (short) 0;
        this.mBG_InetOkActiveNotOk = (short) 0;
        this.mBG_UserSelApFishingCnt = (short) 0;
        this.mBG_ConntTimeoutCnt = (short) 0;
        this.mBG_DNSFailCnt = (short) 0;
        this.mBG_DHCPFailCnt = (short) 0;
        this.mBG_AUTH_FailCnt = (short) 0;
        this.mBG_AssocRejectCnt = (short) 0;
        this.mBG_UserSelFreeInetOkCnt = (short) 0;
        this.mBG_UserSelNoInetCnt = (short) 0;
        this.mBG_UserSelPortalCnt = (short) 0;
        this.mBG_FoundTwoMoreApCnt = (short) 0;
        this.mAF_FPNSuccNotMsmCnt = (short) 0;
        this.mBSG_RsGoodCnt = (short) 0;
        this.mBSG_RsMidCnt = (short) 0;
        this.mBSG_RsBadCnt = (short) 0;
        this.mBSG_EndIn4sCnt = (short) 0;
        this.mBSG_EndIn4s7sCnt = (short) 0;
        this.mBSG_NotEndIn7sCnt = (short) 0;
        this.mBG_NCByConnectFail = (short) 0;
        this.mBG_NCByCheckFail = (short) 0;
        this.mBG_NCByStateErr = (short) 0;
        this.mBG_NCByUnknown = (short) 0;
        this.mBQE_CNUrl1FailCount = (short) 0;
        this.mBQE_CNUrl2FailCount = (short) 0;
        this.mBQE_CNUrl3FailCount = (short) 0;
        this.mBQE_NCNUrl1FailCount = (short) 0;
        this.mBQE_NCNUrl2FailCount = (short) 0;
        this.mBQE_NCNUrl3FailCount = (short) 0;
        this.mBQE_ScoreUnknownCount = (short) 0;
        this.mBQE_BindWlanFailCount = (short) 0;
        this.mBQE_StopBqeFailCount = (short) 0;
        this.mQOE_AutoRI_TotData = 0;
        this.mNotInet_AutoRI_TotData = 0;
        this.mQOE_RO_DISCONNECT_Cnt = (short) 0;
        this.mQOE_RO_DISCONNECT_TotData = 0;
        this.mNotInetRO_DISCONNECT_Cnt = (short) 0;
        this.mNotInetRO_DISCONNECT_TotData = 0;
        this.mTotWifiConnectTime = 0;
        this.mActiveCheckRS_Diff = (short) 0;
        this.mNoInetAlarmOnConnCnt = (short) 0;
        this.mPortalNoAutoConnCnt = (short) 0;
        this.mHomeAPAddRoPeriodCnt = (short) 0;
        this.mHomeAPQoeBadCnt = (short) 0;
        this.mHistoryTotWifiConnHour = 0;
        this.mBigRTT_RO_Tot = (short) 0;
        this.mBigRTT_ErrRO_Tot = (short) 0;
        this.mTotAPRecordCnt = (short) 0;
        this.mTotHomeAPCnt = (short) 0;
        this.mTotalPortalConnCount = (short) 0;
        this.mTotalPortalAuthSuccCount = (short) 0;
        this.mManualConnBlockPortalCount = (short) 0;
        this.mWifiproStateAtReportTime = (short) 0;
        this.mWifiproOpenCount = (short) 0;
        this.mWifiproCloseCount = (short) 0;
        this.mActiveCheckRS_Same = (short) 0;
        this.mSingleAP_LearnedCount = (short) 0;
        this.mSingleAP_NearbyCount = (short) 0;
        this.mSingleAP_MonitorCount = (short) 0;
        this.mSingleAP_SatisfiedCount = (short) 0;
        this.mSingleAP_DisapperCount = (short) 0;
        this.mSingleAP_InblacklistCount = (short) 0;
        this.mSingleAP_ScoreNotSatisfyCount = (short) 0;
        this.mSingleAP_HandoverSucCount = (short) 0;
        this.mSingleAP_HandoverFailCount = (short) 0;
        this.mSingleAP_LowFreqScan5GCount = (short) 0;
        this.mSingleAP_MidFreqScan5GCount = (short) 0;
        this.mSingleAP_HighFreqScan5GCount = (short) 0;
        this.mMixedAP_LearnedCount = (short) 0;
        this.mMixedAP_NearbyCount = (short) 0;
        this.mMixedAP_MonitorCount = (short) 0;
        this.mMixedAP_SatisfiedCount = (short) 0;
        this.mMixedAP_DisapperCount = (short) 0;
        this.mMixedAP_InblacklistCount = (short) 0;
        this.mMixedAP_ScoreNotSatisfyCount = (short) 0;
        this.mMixedAP_HandoverSucCount = (short) 0;
        this.mMixedAP_HandoverFailCount = (short) 0;
        this.mMixedAP_LowFreqScan5GCount = (short) 0;
        this.mMixedAP_MidFreqScan5GCount = (short) 0;
        this.mMixedAP_HighFreqScan5GCount = (short) 0;
        this.mCustomizedScan_SuccCount = (short) 0;
        this.mCustomizedScan_FailCount = (short) 0;
        this.mHandoverToNotInet5GCount = (short) 0;
        this.mHandoverTooSlowCount = (short) 0;
        this.mHandoverToBad5GCount = (short) 0;
        this.mUserRejectHandoverCount = (short) 0;
        this.mHandoverPingpongCount = (short) 0;
    }
}
