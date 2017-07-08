package com.android.server.wifi.wifipro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WifiProChrDataBaseManager {
    private static final int DBG_LOG_LEVEL = 1;
    private static final int ERROR_LOG_LEVEL = 3;
    private static final int INFO_LOG_LEVEL = 2;
    private static final String TAG = "WifiProChrDataBaseManager";
    private static WifiProChrDataBaseManager mChrDataBaseManager;
    private static int printLogLevel;
    private Object mChrLock;
    private SQLiteDatabase mDatabase;
    private WifiProChrDataBaseHelper mHelper;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.WifiProChrDataBaseManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.WifiProChrDataBaseManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.WifiProChrDataBaseManager.<clinit>():void");
    }

    public WifiProChrDataBaseManager(Context context) {
        this.mChrLock = new Object();
        logd("WifiProChrDataBaseManager()");
        this.mHelper = new WifiProChrDataBaseHelper(context);
        this.mDatabase = this.mHelper.getWritableDatabase();
    }

    public static WifiProChrDataBaseManager getInstance(Context context) {
        if (mChrDataBaseManager == null) {
            mChrDataBaseManager = new WifiProChrDataBaseManager(context);
        }
        return mChrDataBaseManager;
    }

    public void closeDB() {
        synchronized (this.mChrLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                return;
            }
            logd("closeDB()");
            this.mDatabase.close();
        }
    }

    private boolean checkIfRcdExist() {
        int recCnt = 0;
        logi("checkIfRcdExist enter.");
        try {
            String[] strArr = new String[DBG_LOG_LEVEL];
            strArr[0] = "1";
            Cursor c = this.mDatabase.rawQuery("SELECT * FROM CHRStatTable where _id like ?", strArr);
            String lastStatUploadTime = "";
            while (c.moveToNext()) {
                recCnt += DBG_LOG_LEVEL;
                if (recCnt > DBG_LOG_LEVEL) {
                    break;
                }
                logd("checkIfRcdExist read record succ, lastStatUploadTime:" + c.getString(c.getColumnIndex("mLastStatUploadTime")));
            }
            c.close();
            if (recCnt > DBG_LOG_LEVEL) {
                loge("more than one record error. ");
            } else if (recCnt == 0) {
                return false;
            }
            return true;
        } catch (SQLException e) {
            loge("checkIfRcdExist error:" + e);
            return false;
        }
    }

    private boolean updateChrStatRcd(WifiProStatisticsRecord dbr) {
        ContentValues values = new ContentValues();
        values.put("mLastStatUploadTime", dbr.mLastStatUploadTime);
        values.put("mLastWifiproState", Short.valueOf(dbr.mLastWifiproState));
        values.put("mLastWifiproStateUpdateTime", dbr.mLastWifiproStateUpdateTime);
        values.put("mEnableTotTime", Integer.valueOf(dbr.mEnableTotTime));
        values.put("mNoInetHandoverCount", Short.valueOf(dbr.mNoInetHandoverCount));
        values.put("mPortalUnauthCount", Short.valueOf(dbr.mPortalUnauthCount));
        values.put("mWifiScoCount", Short.valueOf(dbr.mWifiScoCount));
        values.put("mPortalCodeParseCount", Short.valueOf(dbr.mPortalCodeParseCount));
        values.put("mRcvSMS_Count", Short.valueOf(dbr.mRcvSMS_Count));
        values.put("mPortalAutoLoginCount", Short.valueOf(dbr.mPortalAutoLoginCount));
        values.put("mCellAutoOpenCount", Short.valueOf(dbr.mCellAutoOpenCount));
        values.put("mCellAutoCloseCount", Short.valueOf(dbr.mCellAutoCloseCount));
        values.put("mTotalBQE_BadROC", Short.valueOf(dbr.mTotalBQE_BadROC));
        values.put("mManualBackROC", Short.valueOf(dbr.mManualBackROC));
        values.put("mRSSI_RO_Tot", Short.valueOf(dbr.mRSSI_RO_Tot));
        values.put("mRSSI_ErrRO_Tot", Short.valueOf(dbr.mRSSI_ErrRO_Tot));
        values.put("mOTA_RO_Tot", Short.valueOf(dbr.mOTA_RO_Tot));
        values.put("mOTA_ErrRO_Tot", Short.valueOf(dbr.mOTA_ErrRO_Tot));
        values.put("mTCP_RO_Tot", Short.valueOf(dbr.mTCP_RO_Tot));
        values.put("mTCP_ErrRO_Tot", Short.valueOf(dbr.mTCP_ErrRO_Tot));
        values.put("mManualRI_TotTime", Integer.valueOf(dbr.mManualRI_TotTime));
        values.put("mAutoRI_TotTime", Integer.valueOf(dbr.mAutoRI_TotTime));
        values.put("mAutoRI_TotCount", Short.valueOf(dbr.mAutoRI_TotCount));
        values.put("mRSSI_RestoreRI_Count", Short.valueOf(dbr.mRSSI_RestoreRI_Count));
        values.put("mRSSI_BetterRI_Count", Short.valueOf(dbr.mRSSI_BetterRI_Count));
        values.put("mTimerRI_Count", Short.valueOf(dbr.mTimerRI_Count));
        values.put("mHisScoRI_Count", Short.valueOf(dbr.mHisScoRI_Count));
        values.put("mUserCancelROC", Short.valueOf(dbr.mUserCancelROC));
        values.put("mWifiToWifiSuccCount", Short.valueOf(dbr.mWifiToWifiSuccCount));
        values.put("mNoInetAlarmCount", Short.valueOf(dbr.mNoInetAlarmCount));
        values.put("mWifiOobInitState", Short.valueOf(dbr.mWifiOobInitState));
        values.put("mNotAutoConnPortalCnt", Short.valueOf(dbr.mNotAutoConnPortalCnt));
        values.put("mHighDataRateStopROC", Short.valueOf(dbr.mHighDataRateStopROC));
        values.put("mSelectNotInetAPCount", Short.valueOf(dbr.mSelectNotInetAPCount));
        values.put("mUserUseBgScanAPCount", Short.valueOf(dbr.mUserUseBgScanAPCount));
        values.put("mPingPongCount", Short.valueOf(dbr.mPingPongCount));
        values.put("mBQE_BadSettingCancel", Short.valueOf(dbr.mBQE_BadSettingCancel));
        values.put("mNotInetSettingCancel", Short.valueOf(dbr.mNotInetSettingCancel));
        values.put("mNotInetUserCancel", Short.valueOf(dbr.mNotInetUserCancel));
        values.put("mNotInetRestoreRI", Short.valueOf(dbr.mNotInetRestoreRI));
        values.put("mNotInetUserManualRI", Short.valueOf(dbr.mNotInetUserManualRI));
        values.put("mNotInetWifiToWifiCount", Short.valueOf(dbr.mNotInetWifiToWifiCount));
        values.put("mReopenWifiRICount", Short.valueOf(dbr.mReopenWifiRICount));
        values.put("mSelCSPShowDiglogCount", Short.valueOf(dbr.mSelCSPShowDiglogCount));
        values.put("mSelCSPAutoSwCount", Short.valueOf(dbr.mSelCSPAutoSwCount));
        values.put("mSelCSPNotSwCount", Short.valueOf(dbr.mSelCSPNotSwCount));
        values.put("mTotBtnRICount", Short.valueOf(dbr.mTotBtnRICount));
        values.put("mBMD_TenMNotifyCount", Short.valueOf(dbr.mBMD_TenMNotifyCount));
        values.put("mBMD_TenM_RI_Count", Short.valueOf(dbr.mBMD_TenM_RI_Count));
        values.put("mBMD_FiftyMNotifyCount", Short.valueOf(dbr.mBMD_FiftyMNotifyCount));
        values.put("mBMD_FiftyM_RI_Count", Short.valueOf(dbr.mBMD_FiftyM_RI_Count));
        values.put("mBMD_UserDelNotifyCount", Short.valueOf(dbr.mBMD_UserDelNotifyCount));
        values.put("mRO_TotMobileData", Integer.valueOf(dbr.mRO_TotMobileData));
        values.put("mAF_PhoneNumSuccCnt", Short.valueOf(dbr.mAF_PhoneNumSuccCnt));
        values.put("mAF_PhoneNumFailCnt", Short.valueOf(dbr.mAF_PhoneNumFailCnt));
        values.put("mAF_PasswordSuccCnt", Short.valueOf(dbr.mAF_PasswordSuccCnt));
        values.put("mAF_PasswordFailCnt", Short.valueOf(dbr.mAF_PasswordFailCnt));
        values.put("mAF_AutoLoginSuccCnt", Short.valueOf(dbr.mAF_AutoLoginSuccCnt));
        values.put("mAF_AutoLoginFailCnt", Short.valueOf(dbr.mAF_AutoLoginFailCnt));
        values.put("mBG_BgRunCnt", Short.valueOf(dbr.mBG_BgRunCnt));
        values.put("mBG_SettingRunCnt", Short.valueOf(dbr.mBG_SettingRunCnt));
        values.put("mBG_FreeInetOkApCnt", Short.valueOf(dbr.mBG_FreeInetOkApCnt));
        values.put("mBG_FishingApCnt", Short.valueOf(dbr.mBG_FishingApCnt));
        values.put("mBG_FreeNotInetApCnt", Short.valueOf(dbr.mBG_FreeNotInetApCnt));
        values.put("mBG_PortalApCnt", Short.valueOf(dbr.mBG_PortalApCnt));
        values.put("mBG_FailedCnt", Short.valueOf(dbr.mBG_FailedCnt));
        values.put("mBG_InetNotOkActiveOk", Short.valueOf(dbr.mBG_InetNotOkActiveOk));
        values.put("mBG_InetOkActiveNotOk", Short.valueOf(dbr.mBG_InetOkActiveNotOk));
        values.put("mBG_UserSelApFishingCnt", Short.valueOf(dbr.mBG_UserSelApFishingCnt));
        values.put("mBG_ConntTimeoutCnt", Short.valueOf(dbr.mBG_ConntTimeoutCnt));
        values.put("mBG_DNSFailCnt", Short.valueOf(dbr.mBG_DNSFailCnt));
        values.put("mBG_DHCPFailCnt", Short.valueOf(dbr.mBG_DHCPFailCnt));
        values.put("mBG_AUTH_FailCnt", Short.valueOf(dbr.mBG_AUTH_FailCnt));
        values.put("mBG_AssocRejectCnt", Short.valueOf(dbr.mBG_AssocRejectCnt));
        values.put("mBG_UserSelFreeInetOkCnt", Short.valueOf(dbr.mBG_UserSelFreeInetOkCnt));
        values.put("mBG_UserSelNoInetCnt", Short.valueOf(dbr.mBG_UserSelNoInetCnt));
        values.put("mBG_UserSelPortalCnt", Short.valueOf(dbr.mBG_UserSelPortalCnt));
        values.put("mBG_FoundTwoMoreApCnt", Short.valueOf(dbr.mBG_FoundTwoMoreApCnt));
        values.put("mAF_FPNSuccNotMsmCnt", Short.valueOf(dbr.mAF_FPNSuccNotMsmCnt));
        values.put("mBSG_RsGoodCnt", Short.valueOf(dbr.mBSG_RsGoodCnt));
        values.put("mBSG_RsMidCnt", Short.valueOf(dbr.mBSG_RsMidCnt));
        values.put("mBSG_RsBadCnt", Short.valueOf(dbr.mBSG_RsBadCnt));
        values.put("mBSG_EndIn4sCnt", Short.valueOf(dbr.mBSG_EndIn4sCnt));
        values.put("mBSG_EndIn4s7sCnt", Short.valueOf(dbr.mBSG_EndIn4s7sCnt));
        values.put("mBSG_NotEndIn7sCnt", Short.valueOf(dbr.mBSG_NotEndIn7sCnt));
        values.put("mBG_NCByConnectFail", Short.valueOf(dbr.mBG_NCByConnectFail));
        values.put("mBG_NCByCheckFail", Short.valueOf(dbr.mBG_NCByCheckFail));
        values.put("mBG_NCByStateErr", Short.valueOf(dbr.mBG_NCByStateErr));
        values.put("mBG_NCByUnknown", Short.valueOf(dbr.mBG_NCByUnknown));
        values.put("mBQE_CNUrl1FailCount", Short.valueOf(dbr.mBQE_CNUrl1FailCount));
        values.put("mBQE_CNUrl2FailCount", Short.valueOf(dbr.mBQE_CNUrl2FailCount));
        values.put("mBQE_CNUrl3FailCount", Short.valueOf(dbr.mBQE_CNUrl3FailCount));
        values.put("mBQE_NCNUrl1FailCount", Short.valueOf(dbr.mBQE_NCNUrl1FailCount));
        values.put("mBQE_NCNUrl2FailCount", Short.valueOf(dbr.mBQE_NCNUrl2FailCount));
        values.put("mBQE_NCNUrl3FailCount", Short.valueOf(dbr.mBQE_NCNUrl3FailCount));
        values.put("mBQE_ScoreUnknownCount", Short.valueOf(dbr.mBQE_ScoreUnknownCount));
        values.put("mBQE_BindWlanFailCount", Short.valueOf(dbr.mBQE_BindWlanFailCount));
        values.put("mBQE_StopBqeFailCount", Short.valueOf(dbr.mBQE_StopBqeFailCount));
        values.put("mQOE_AutoRI_TotData", Integer.valueOf(dbr.mQOE_AutoRI_TotData));
        values.put("mNotInet_AutoRI_TotData", Integer.valueOf(dbr.mNotInet_AutoRI_TotData));
        values.put("mQOE_RO_DISCONNECT_Cnt", Short.valueOf(dbr.mQOE_RO_DISCONNECT_Cnt));
        values.put("mQOE_RO_DISCONNECT_TotData", Integer.valueOf(dbr.mQOE_RO_DISCONNECT_TotData));
        values.put("mNotInetRO_DISCONNECT_Cnt", Short.valueOf(dbr.mNotInetRO_DISCONNECT_Cnt));
        values.put("mNotInetRO_DISCONNECT_TotData", Integer.valueOf(dbr.mNotInetRO_DISCONNECT_TotData));
        values.put("mTotWifiConnectTime", Integer.valueOf(dbr.mTotWifiConnectTime));
        values.put("mActiveCheckRS_Diff", Short.valueOf(dbr.mActiveCheckRS_Diff));
        values.put("mNoInetAlarmOnConnCnt", Short.valueOf(dbr.mNoInetAlarmOnConnCnt));
        values.put("mPortalNoAutoConnCnt", Short.valueOf(dbr.mPortalNoAutoConnCnt));
        values.put("mHomeAPAddRoPeriodCnt", Short.valueOf(dbr.mHomeAPAddRoPeriodCnt));
        values.put("mHomeAPQoeBadCnt", Short.valueOf(dbr.mHomeAPQoeBadCnt));
        values.put("mHistoryTotWifiConnHour", Integer.valueOf(dbr.mHistoryTotWifiConnHour));
        values.put("mBigRTT_RO_Tot", Short.valueOf(dbr.mBigRTT_RO_Tot));
        values.put("mBigRTT_ErrRO_Tot", Short.valueOf(dbr.mBigRTT_ErrRO_Tot));
        values.put("mTotalPortalConnCount", Short.valueOf(dbr.mTotalPortalConnCount));
        values.put("mTotalPortalAuthSuccCount", Short.valueOf(dbr.mTotalPortalAuthSuccCount));
        values.put("mManualConnBlockPortalCount", Short.valueOf(dbr.mManualConnBlockPortalCount));
        values.put("mWifiproOpenCount", Short.valueOf(dbr.mWifiproOpenCount));
        values.put("mWifiproCloseCount", Short.valueOf(dbr.mWifiproCloseCount));
        values.put("mActiveCheckRS_Same", Short.valueOf(dbr.mActiveCheckRS_Same));
        values.put("mSingleAP_LearnedCount", Short.valueOf(dbr.mSingleAP_LearnedCount));
        values.put("mSingleAP_NearbyCount", Short.valueOf(dbr.mSingleAP_NearbyCount));
        values.put("mSingleAP_MonitorCount", Short.valueOf(dbr.mSingleAP_MonitorCount));
        values.put("mSingleAP_SatisfiedCount", Short.valueOf(dbr.mSingleAP_SatisfiedCount));
        values.put("mSingleAP_DisapperCount", Short.valueOf(dbr.mSingleAP_DisapperCount));
        values.put("mSingleAP_InblacklistCount", Short.valueOf(dbr.mSingleAP_InblacklistCount));
        values.put("mSingleAP_ScoreNotSatisfyCount", Short.valueOf(dbr.mSingleAP_ScoreNotSatisfyCount));
        values.put("mSingleAP_HandoverSucCount", Short.valueOf(dbr.mSingleAP_HandoverSucCount));
        values.put("mSingleAP_HandoverFailCount", Short.valueOf(dbr.mSingleAP_HandoverFailCount));
        values.put("mSingleAP_LowFreqScan5GCount", Short.valueOf(dbr.mSingleAP_LowFreqScan5GCount));
        values.put("mSingleAP_MidFreqScan5GCount", Short.valueOf(dbr.mSingleAP_MidFreqScan5GCount));
        values.put("mSingleAP_HighFreqScan5GCount", Short.valueOf(dbr.mSingleAP_HighFreqScan5GCount));
        values.put("mMixedAP_LearnedCount", Short.valueOf(dbr.mMixedAP_LearnedCount));
        values.put("mMixedAP_NearbyCount", Short.valueOf(dbr.mMixedAP_NearbyCount));
        values.put("mMixedAP_MonitorCount", Short.valueOf(dbr.mMixedAP_MonitorCount));
        values.put("mMixedAP_SatisfiedCount", Short.valueOf(dbr.mMixedAP_SatisfiedCount));
        values.put("mMixedAP_DisapperCount", Short.valueOf(dbr.mMixedAP_DisapperCount));
        values.put("mMixedAP_InblacklistCount", Short.valueOf(dbr.mMixedAP_InblacklistCount));
        values.put("mMixedAP_ScoreNotSatisfyCount", Short.valueOf(dbr.mMixedAP_ScoreNotSatisfyCount));
        values.put("mMixedAP_HandoverSucCount", Short.valueOf(dbr.mMixedAP_HandoverSucCount));
        values.put("mMixedAP_HandoverFailCount", Short.valueOf(dbr.mMixedAP_HandoverFailCount));
        values.put("mMixedAP_LowFreqScan5GCount", Short.valueOf(dbr.mMixedAP_LowFreqScan5GCount));
        values.put("mMixedAP_MidFreqScan5GCount", Short.valueOf(dbr.mMixedAP_MidFreqScan5GCount));
        values.put("mMixedAP_HighFreqScan5GCount", Short.valueOf(dbr.mMixedAP_HighFreqScan5GCount));
        values.put("mCustomizedScan_SuccCount", Short.valueOf(dbr.mCustomizedScan_SuccCount));
        values.put("mCustomizedScan_FailCount", Short.valueOf(dbr.mCustomizedScan_FailCount));
        values.put("mHandoverToNotInet5GCount", Short.valueOf(dbr.mHandoverToNotInet5GCount));
        values.put("mHandoverTooSlowCount", Short.valueOf(dbr.mHandoverTooSlowCount));
        values.put("mHandoverToBad5GCount", Short.valueOf(dbr.mHandoverToBad5GCount));
        values.put("mUserRejectHandoverCount", Short.valueOf(dbr.mUserRejectHandoverCount));
        values.put("mHandoverPingpongCount", Short.valueOf(dbr.mHandoverPingpongCount));
        int rowChg = 0;
        try {
            String[] strArr = new String[DBG_LOG_LEVEL];
            strArr[0] = "1";
            rowChg = this.mDatabase.update(WifiProChrDataBaseHelper.CHR_STAT_TABLE_NAME, values, "_id like ?", strArr);
        } catch (SQLException e) {
            loge("update error:" + e);
        }
        if (rowChg == 0) {
            loge("updateChrStatRcd update failed.");
            return false;
        }
        logd("updateChrStatRcd update succ, rowChg=" + rowChg);
        return true;
    }

    private boolean insertChrStatRcd(WifiProStatisticsRecord dbr) {
        logd("insertChrStatRcd enter.");
        synchronized (this.mChrLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("insertChrStatRcd database error.");
                return false;
            } else if (dbr == null) {
                loge("insertChrStatRcd null error.");
                return false;
            } else {
                this.mDatabase.beginTransaction();
                try {
                    this.mDatabase.execSQL("INSERT INTO CHRStatTable VALUES(?,   ?, ?, ?, ?, ?,   ?, ?, ?, ?, ?, ?,   ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?,    ?, ?,  ?, ?, ?, ?, ?, ?, ?, ?,   ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,     ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?,    ?,?,?,?,?,?, ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?,  ?)", new Object[]{Short.valueOf((short) 1), dbr.mLastStatUploadTime, Short.valueOf(dbr.mLastWifiproState), dbr.mLastWifiproStateUpdateTime, Integer.valueOf(dbr.mEnableTotTime), Short.valueOf(dbr.mNoInetHandoverCount), Short.valueOf(dbr.mPortalUnauthCount), Short.valueOf(dbr.mWifiScoCount), Short.valueOf(dbr.mPortalCodeParseCount), Short.valueOf(dbr.mRcvSMS_Count), Short.valueOf(dbr.mPortalAutoLoginCount), Short.valueOf(dbr.mCellAutoOpenCount), Short.valueOf(dbr.mCellAutoCloseCount), Short.valueOf(dbr.mTotalBQE_BadROC), Short.valueOf(dbr.mManualBackROC), Short.valueOf(dbr.mRSSI_RO_Tot), Short.valueOf(dbr.mRSSI_ErrRO_Tot), Short.valueOf(dbr.mOTA_RO_Tot), Short.valueOf(dbr.mOTA_ErrRO_Tot), Short.valueOf(dbr.mTCP_RO_Tot), Short.valueOf(dbr.mTCP_ErrRO_Tot), Integer.valueOf(dbr.mManualRI_TotTime), Integer.valueOf(dbr.mAutoRI_TotTime), Short.valueOf(dbr.mAutoRI_TotCount), Short.valueOf(dbr.mRSSI_RestoreRI_Count), Short.valueOf(dbr.mRSSI_BetterRI_Count), Short.valueOf(dbr.mTimerRI_Count), Short.valueOf(dbr.mHisScoRI_Count), Short.valueOf(dbr.mUserCancelROC), Short.valueOf(dbr.mWifiToWifiSuccCount), Short.valueOf(dbr.mNoInetAlarmCount), Short.valueOf(dbr.mWifiOobInitState), Short.valueOf(dbr.mNotAutoConnPortalCnt), Short.valueOf(dbr.mHighDataRateStopROC), Short.valueOf(dbr.mSelectNotInetAPCount), Short.valueOf(dbr.mUserUseBgScanAPCount), Short.valueOf(dbr.mPingPongCount), Short.valueOf(dbr.mBQE_BadSettingCancel), Short.valueOf(dbr.mNotInetSettingCancel), Short.valueOf(dbr.mNotInetUserCancel), Short.valueOf(dbr.mNotInetRestoreRI), Short.valueOf(dbr.mNotInetUserManualRI), Short.valueOf(dbr.mNotInetWifiToWifiCount), Short.valueOf(dbr.mReopenWifiRICount), Short.valueOf(dbr.mSelCSPShowDiglogCount), Short.valueOf(dbr.mSelCSPAutoSwCount), Short.valueOf(dbr.mSelCSPNotSwCount), Short.valueOf(dbr.mTotBtnRICount), Short.valueOf(dbr.mBMD_TenMNotifyCount), Short.valueOf(dbr.mBMD_TenM_RI_Count), Short.valueOf(dbr.mBMD_FiftyMNotifyCount), Short.valueOf(dbr.mBMD_FiftyM_RI_Count), Short.valueOf(dbr.mBMD_UserDelNotifyCount), Integer.valueOf(dbr.mRO_TotMobileData), Short.valueOf(dbr.mAF_PhoneNumSuccCnt), Short.valueOf(dbr.mAF_PhoneNumFailCnt), Short.valueOf(dbr.mAF_PasswordSuccCnt), Short.valueOf(dbr.mAF_PasswordFailCnt), Short.valueOf(dbr.mAF_AutoLoginSuccCnt), Short.valueOf(dbr.mAF_AutoLoginFailCnt), Short.valueOf(dbr.mBG_BgRunCnt), Short.valueOf(dbr.mBG_SettingRunCnt), Short.valueOf(dbr.mBG_FreeInetOkApCnt), Short.valueOf(dbr.mBG_FishingApCnt), Short.valueOf(dbr.mBG_FreeNotInetApCnt), Short.valueOf(dbr.mBG_PortalApCnt), Short.valueOf(dbr.mBG_FailedCnt), Short.valueOf(dbr.mBG_InetNotOkActiveOk), Short.valueOf(dbr.mBG_InetOkActiveNotOk), Short.valueOf(dbr.mBG_UserSelApFishingCnt), Short.valueOf(dbr.mBG_ConntTimeoutCnt), Short.valueOf(dbr.mBG_DNSFailCnt), Short.valueOf(dbr.mBG_DHCPFailCnt), Short.valueOf(dbr.mBG_AUTH_FailCnt), Short.valueOf(dbr.mBG_AssocRejectCnt), Short.valueOf(dbr.mBG_UserSelFreeInetOkCnt), Short.valueOf(dbr.mBG_UserSelNoInetCnt), Short.valueOf(dbr.mBG_UserSelPortalCnt), Short.valueOf(dbr.mBG_FoundTwoMoreApCnt), Short.valueOf(dbr.mAF_FPNSuccNotMsmCnt), Short.valueOf(dbr.mBSG_RsGoodCnt), Short.valueOf(dbr.mBSG_RsMidCnt), Short.valueOf(dbr.mBSG_RsBadCnt), Short.valueOf(dbr.mBSG_EndIn4sCnt), Short.valueOf(dbr.mBSG_EndIn4s7sCnt), Short.valueOf(dbr.mBSG_NotEndIn7sCnt), Short.valueOf(dbr.mBG_NCByConnectFail), Short.valueOf(dbr.mBG_NCByCheckFail), Short.valueOf(dbr.mBG_NCByStateErr), Short.valueOf(dbr.mBG_NCByUnknown), Short.valueOf(dbr.mBQE_CNUrl1FailCount), Short.valueOf(dbr.mBQE_CNUrl2FailCount), Short.valueOf(dbr.mBQE_CNUrl3FailCount), Short.valueOf(dbr.mBQE_NCNUrl1FailCount), Short.valueOf(dbr.mBQE_NCNUrl2FailCount), Short.valueOf(dbr.mBQE_NCNUrl3FailCount), Short.valueOf(dbr.mBQE_ScoreUnknownCount), Short.valueOf(dbr.mBQE_BindWlanFailCount), Short.valueOf(dbr.mBQE_StopBqeFailCount), Integer.valueOf(dbr.mQOE_AutoRI_TotData), Integer.valueOf(dbr.mNotInet_AutoRI_TotData), Short.valueOf(dbr.mQOE_RO_DISCONNECT_Cnt), Integer.valueOf(dbr.mQOE_RO_DISCONNECT_TotData), Short.valueOf(dbr.mNotInetRO_DISCONNECT_Cnt), Integer.valueOf(dbr.mNotInetRO_DISCONNECT_TotData), Integer.valueOf(dbr.mTotWifiConnectTime), Short.valueOf(dbr.mActiveCheckRS_Diff), Short.valueOf(dbr.mNoInetAlarmOnConnCnt), Short.valueOf(dbr.mPortalNoAutoConnCnt), Short.valueOf(dbr.mHomeAPAddRoPeriodCnt), Short.valueOf(dbr.mHomeAPQoeBadCnt), Integer.valueOf(dbr.mHistoryTotWifiConnHour), Short.valueOf(dbr.mBigRTT_RO_Tot), Short.valueOf(dbr.mBigRTT_ErrRO_Tot), Short.valueOf(dbr.mTotalPortalConnCount), Short.valueOf(dbr.mTotalPortalAuthSuccCount), Short.valueOf(dbr.mManualConnBlockPortalCount), Short.valueOf(dbr.mWifiproOpenCount), Short.valueOf(dbr.mWifiproCloseCount), Short.valueOf(dbr.mActiveCheckRS_Same), Short.valueOf(dbr.mSingleAP_LearnedCount), Short.valueOf(dbr.mSingleAP_NearbyCount), Short.valueOf(dbr.mSingleAP_MonitorCount), Short.valueOf(dbr.mSingleAP_SatisfiedCount), Short.valueOf(dbr.mSingleAP_DisapperCount), Short.valueOf(dbr.mSingleAP_InblacklistCount), Short.valueOf(dbr.mSingleAP_ScoreNotSatisfyCount), Short.valueOf(dbr.mSingleAP_HandoverSucCount), Short.valueOf(dbr.mSingleAP_HandoverFailCount), Short.valueOf(dbr.mSingleAP_LowFreqScan5GCount), Short.valueOf(dbr.mSingleAP_MidFreqScan5GCount), Short.valueOf(dbr.mSingleAP_HighFreqScan5GCount), Short.valueOf(dbr.mMixedAP_LearnedCount), Short.valueOf(dbr.mMixedAP_NearbyCount), Short.valueOf(dbr.mMixedAP_MonitorCount), Short.valueOf(dbr.mMixedAP_SatisfiedCount), Short.valueOf(dbr.mMixedAP_DisapperCount), Short.valueOf(dbr.mMixedAP_InblacklistCount), Short.valueOf(dbr.mMixedAP_ScoreNotSatisfyCount), Short.valueOf(dbr.mMixedAP_HandoverSucCount), Short.valueOf(dbr.mMixedAP_HandoverFailCount), Short.valueOf(dbr.mMixedAP_LowFreqScan5GCount), Short.valueOf(dbr.mMixedAP_MidFreqScan5GCount), Short.valueOf(dbr.mMixedAP_HighFreqScan5GCount), Short.valueOf(dbr.mCustomizedScan_SuccCount), Short.valueOf(dbr.mCustomizedScan_FailCount), Short.valueOf(dbr.mHandoverToNotInet5GCount), Short.valueOf(dbr.mHandoverTooSlowCount), Short.valueOf(dbr.mHandoverToBad5GCount), Short.valueOf(dbr.mUserRejectHandoverCount), Short.valueOf(dbr.mHandoverPingpongCount)});
                    this.mDatabase.setTransactionSuccessful();
                    logi("insertChrStatRcd update or add a record succ");
                    this.mDatabase.endTransaction();
                } catch (SQLException e) {
                    loge("insertChrStatRcd error:" + e);
                    this.mDatabase.endTransaction();
                } catch (Throwable th) {
                    this.mDatabase.endTransaction();
                }
                return true;
            }
        }
    }

    public boolean addOrUpdateChrStatRcd(WifiProStatisticsRecord dbr) {
        synchronized (this.mChrLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || dbr == null) {
                loge("insertChrStatRcd error.");
                return false;
            } else if (checkIfRcdExist()) {
                r1 = updateChrStatRcd(dbr);
                return r1;
            } else {
                r1 = insertChrStatRcd(dbr);
                return r1;
            }
        }
    }

    public boolean queryChrStatRcd(WifiProStatisticsRecord dbr) {
        int recCnt = 0;
        logd("queryChrStatRcd enter.");
        synchronized (this.mChrLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("queryChrStatRcd database error.");
                return false;
            } else if (dbr == null) {
                loge("queryChrStatRcd null error.");
                return false;
            } else {
                try {
                    String[] strArr = new String[DBG_LOG_LEVEL];
                    strArr[0] = "1";
                    Cursor c = this.mDatabase.rawQuery("SELECT * FROM CHRStatTable where _id like ?", strArr);
                    while (c.moveToNext()) {
                        recCnt += DBG_LOG_LEVEL;
                        if (recCnt > DBG_LOG_LEVEL) {
                            break;
                        }
                        dbr.mLastStatUploadTime = c.getString(c.getColumnIndex("mLastStatUploadTime"));
                        dbr.mLastWifiproState = c.getShort(c.getColumnIndex("mLastWifiproState"));
                        dbr.mLastWifiproStateUpdateTime = c.getString(c.getColumnIndex("mLastWifiproStateUpdateTime"));
                        dbr.mEnableTotTime = c.getInt(c.getColumnIndex("mEnableTotTime"));
                        dbr.mNoInetHandoverCount = c.getShort(c.getColumnIndex("mNoInetHandoverCount"));
                        dbr.mPortalUnauthCount = c.getShort(c.getColumnIndex("mPortalUnauthCount"));
                        dbr.mWifiScoCount = c.getShort(c.getColumnIndex("mWifiScoCount"));
                        dbr.mPortalCodeParseCount = c.getShort(c.getColumnIndex("mPortalCodeParseCount"));
                        dbr.mRcvSMS_Count = c.getShort(c.getColumnIndex("mRcvSMS_Count"));
                        dbr.mPortalAutoLoginCount = c.getShort(c.getColumnIndex("mPortalAutoLoginCount"));
                        dbr.mCellAutoOpenCount = c.getShort(c.getColumnIndex("mCellAutoOpenCount"));
                        dbr.mCellAutoCloseCount = c.getShort(c.getColumnIndex("mCellAutoCloseCount"));
                        dbr.mTotalBQE_BadROC = c.getShort(c.getColumnIndex("mTotalBQE_BadROC"));
                        dbr.mManualBackROC = c.getShort(c.getColumnIndex("mManualBackROC"));
                        dbr.mRSSI_RO_Tot = c.getShort(c.getColumnIndex("mRSSI_RO_Tot"));
                        dbr.mRSSI_ErrRO_Tot = c.getShort(c.getColumnIndex("mRSSI_ErrRO_Tot"));
                        dbr.mOTA_RO_Tot = c.getShort(c.getColumnIndex("mOTA_RO_Tot"));
                        dbr.mOTA_ErrRO_Tot = c.getShort(c.getColumnIndex("mOTA_ErrRO_Tot"));
                        dbr.mTCP_RO_Tot = c.getShort(c.getColumnIndex("mTCP_RO_Tot"));
                        dbr.mTCP_ErrRO_Tot = c.getShort(c.getColumnIndex("mTCP_ErrRO_Tot"));
                        dbr.mManualRI_TotTime = c.getInt(c.getColumnIndex("mManualRI_TotTime"));
                        dbr.mAutoRI_TotTime = c.getInt(c.getColumnIndex("mAutoRI_TotTime"));
                        dbr.mAutoRI_TotCount = c.getShort(c.getColumnIndex("mAutoRI_TotCount"));
                        dbr.mRSSI_RestoreRI_Count = c.getShort(c.getColumnIndex("mRSSI_RestoreRI_Count"));
                        dbr.mRSSI_BetterRI_Count = c.getShort(c.getColumnIndex("mRSSI_BetterRI_Count"));
                        dbr.mTimerRI_Count = c.getShort(c.getColumnIndex("mTimerRI_Count"));
                        dbr.mHisScoRI_Count = c.getShort(c.getColumnIndex("mHisScoRI_Count"));
                        dbr.mUserCancelROC = c.getShort(c.getColumnIndex("mUserCancelROC"));
                        dbr.mWifiToWifiSuccCount = c.getShort(c.getColumnIndex("mWifiToWifiSuccCount"));
                        dbr.mNoInetAlarmCount = c.getShort(c.getColumnIndex("mNoInetAlarmCount"));
                        dbr.mWifiOobInitState = c.getShort(c.getColumnIndex("mWifiOobInitState"));
                        dbr.mNotAutoConnPortalCnt = c.getShort(c.getColumnIndex("mNotAutoConnPortalCnt"));
                        dbr.mHighDataRateStopROC = c.getShort(c.getColumnIndex("mHighDataRateStopROC"));
                        dbr.mSelectNotInetAPCount = c.getShort(c.getColumnIndex("mSelectNotInetAPCount"));
                        dbr.mUserUseBgScanAPCount = c.getShort(c.getColumnIndex("mUserUseBgScanAPCount"));
                        dbr.mPingPongCount = c.getShort(c.getColumnIndex("mPingPongCount"));
                        dbr.mBQE_BadSettingCancel = c.getShort(c.getColumnIndex("mBQE_BadSettingCancel"));
                        dbr.mNotInetSettingCancel = c.getShort(c.getColumnIndex("mNotInetSettingCancel"));
                        dbr.mNotInetUserCancel = c.getShort(c.getColumnIndex("mNotInetUserCancel"));
                        dbr.mNotInetRestoreRI = c.getShort(c.getColumnIndex("mNotInetRestoreRI"));
                        dbr.mNotInetUserManualRI = c.getShort(c.getColumnIndex("mNotInetUserManualRI"));
                        dbr.mNotInetWifiToWifiCount = c.getShort(c.getColumnIndex("mNotInetWifiToWifiCount"));
                        dbr.mReopenWifiRICount = c.getShort(c.getColumnIndex("mReopenWifiRICount"));
                        dbr.mSelCSPShowDiglogCount = c.getShort(c.getColumnIndex("mSelCSPShowDiglogCount"));
                        dbr.mSelCSPAutoSwCount = c.getShort(c.getColumnIndex("mSelCSPAutoSwCount"));
                        dbr.mSelCSPNotSwCount = c.getShort(c.getColumnIndex("mSelCSPNotSwCount"));
                        dbr.mTotBtnRICount = c.getShort(c.getColumnIndex("mTotBtnRICount"));
                        dbr.mBMD_TenMNotifyCount = c.getShort(c.getColumnIndex("mBMD_TenMNotifyCount"));
                        dbr.mBMD_TenM_RI_Count = c.getShort(c.getColumnIndex("mBMD_TenM_RI_Count"));
                        dbr.mBMD_FiftyMNotifyCount = c.getShort(c.getColumnIndex("mBMD_FiftyMNotifyCount"));
                        dbr.mBMD_FiftyM_RI_Count = c.getShort(c.getColumnIndex("mBMD_FiftyM_RI_Count"));
                        dbr.mBMD_UserDelNotifyCount = c.getShort(c.getColumnIndex("mBMD_UserDelNotifyCount"));
                        dbr.mRO_TotMobileData = c.getInt(c.getColumnIndex("mRO_TotMobileData"));
                        dbr.mAF_PhoneNumSuccCnt = c.getShort(c.getColumnIndex("mAF_PhoneNumSuccCnt"));
                        dbr.mAF_PhoneNumFailCnt = c.getShort(c.getColumnIndex("mAF_PhoneNumFailCnt"));
                        dbr.mAF_PasswordSuccCnt = c.getShort(c.getColumnIndex("mAF_PasswordSuccCnt"));
                        dbr.mAF_PasswordFailCnt = c.getShort(c.getColumnIndex("mAF_PasswordFailCnt"));
                        dbr.mAF_AutoLoginSuccCnt = c.getShort(c.getColumnIndex("mAF_AutoLoginSuccCnt"));
                        dbr.mAF_AutoLoginFailCnt = c.getShort(c.getColumnIndex("mAF_AutoLoginFailCnt"));
                        dbr.mBG_BgRunCnt = c.getShort(c.getColumnIndex("mBG_BgRunCnt"));
                        dbr.mBG_SettingRunCnt = c.getShort(c.getColumnIndex("mBG_SettingRunCnt"));
                        dbr.mBG_FreeInetOkApCnt = c.getShort(c.getColumnIndex("mBG_FreeInetOkApCnt"));
                        dbr.mBG_FishingApCnt = c.getShort(c.getColumnIndex("mBG_FishingApCnt"));
                        dbr.mBG_FreeNotInetApCnt = c.getShort(c.getColumnIndex("mBG_FreeNotInetApCnt"));
                        dbr.mBG_PortalApCnt = c.getShort(c.getColumnIndex("mBG_PortalApCnt"));
                        dbr.mBG_FailedCnt = c.getShort(c.getColumnIndex("mBG_FailedCnt"));
                        dbr.mBG_InetNotOkActiveOk = c.getShort(c.getColumnIndex("mBG_InetNotOkActiveOk"));
                        dbr.mBG_InetOkActiveNotOk = c.getShort(c.getColumnIndex("mBG_InetOkActiveNotOk"));
                        dbr.mBG_UserSelApFishingCnt = c.getShort(c.getColumnIndex("mBG_UserSelApFishingCnt"));
                        dbr.mBG_ConntTimeoutCnt = c.getShort(c.getColumnIndex("mBG_ConntTimeoutCnt"));
                        dbr.mBG_DNSFailCnt = c.getShort(c.getColumnIndex("mBG_DNSFailCnt"));
                        dbr.mBG_DHCPFailCnt = c.getShort(c.getColumnIndex("mBG_DHCPFailCnt"));
                        dbr.mBG_AUTH_FailCnt = c.getShort(c.getColumnIndex("mBG_AUTH_FailCnt"));
                        dbr.mBG_AssocRejectCnt = c.getShort(c.getColumnIndex("mBG_AssocRejectCnt"));
                        dbr.mBG_UserSelFreeInetOkCnt = c.getShort(c.getColumnIndex("mBG_UserSelFreeInetOkCnt"));
                        dbr.mBG_UserSelNoInetCnt = c.getShort(c.getColumnIndex("mBG_UserSelNoInetCnt"));
                        dbr.mBG_UserSelPortalCnt = c.getShort(c.getColumnIndex("mBG_UserSelPortalCnt"));
                        dbr.mBG_FoundTwoMoreApCnt = c.getShort(c.getColumnIndex("mBG_FoundTwoMoreApCnt"));
                        dbr.mAF_FPNSuccNotMsmCnt = c.getShort(c.getColumnIndex("mAF_FPNSuccNotMsmCnt"));
                        dbr.mBSG_RsGoodCnt = c.getShort(c.getColumnIndex("mBSG_RsGoodCnt"));
                        dbr.mBSG_RsMidCnt = c.getShort(c.getColumnIndex("mBSG_RsMidCnt"));
                        dbr.mBSG_RsBadCnt = c.getShort(c.getColumnIndex("mBSG_RsBadCnt"));
                        dbr.mBSG_EndIn4sCnt = c.getShort(c.getColumnIndex("mBSG_EndIn4sCnt"));
                        dbr.mBSG_EndIn4s7sCnt = c.getShort(c.getColumnIndex("mBSG_EndIn4s7sCnt"));
                        dbr.mBSG_NotEndIn7sCnt = c.getShort(c.getColumnIndex("mBSG_NotEndIn7sCnt"));
                        dbr.mBG_NCByConnectFail = c.getShort(c.getColumnIndex("mBG_NCByConnectFail"));
                        dbr.mBG_NCByCheckFail = c.getShort(c.getColumnIndex("mBG_NCByCheckFail"));
                        dbr.mBG_NCByStateErr = c.getShort(c.getColumnIndex("mBG_NCByStateErr"));
                        dbr.mBG_NCByUnknown = c.getShort(c.getColumnIndex("mBG_NCByUnknown"));
                        dbr.mBQE_CNUrl1FailCount = c.getShort(c.getColumnIndex("mBQE_CNUrl1FailCount"));
                        dbr.mBQE_CNUrl2FailCount = c.getShort(c.getColumnIndex("mBQE_CNUrl2FailCount"));
                        dbr.mBQE_CNUrl3FailCount = c.getShort(c.getColumnIndex("mBQE_CNUrl3FailCount"));
                        dbr.mBQE_NCNUrl1FailCount = c.getShort(c.getColumnIndex("mBQE_NCNUrl1FailCount"));
                        dbr.mBQE_NCNUrl2FailCount = c.getShort(c.getColumnIndex("mBQE_NCNUrl2FailCount"));
                        dbr.mBQE_NCNUrl3FailCount = c.getShort(c.getColumnIndex("mBQE_NCNUrl3FailCount"));
                        dbr.mBQE_ScoreUnknownCount = c.getShort(c.getColumnIndex("mBQE_ScoreUnknownCount"));
                        dbr.mBQE_BindWlanFailCount = c.getShort(c.getColumnIndex("mBQE_BindWlanFailCount"));
                        dbr.mBQE_StopBqeFailCount = c.getShort(c.getColumnIndex("mBQE_StopBqeFailCount"));
                        dbr.mQOE_AutoRI_TotData = c.getInt(c.getColumnIndex("mQOE_AutoRI_TotData"));
                        dbr.mNotInet_AutoRI_TotData = c.getInt(c.getColumnIndex("mNotInet_AutoRI_TotData"));
                        dbr.mQOE_RO_DISCONNECT_Cnt = c.getShort(c.getColumnIndex("mQOE_RO_DISCONNECT_Cnt"));
                        dbr.mQOE_RO_DISCONNECT_TotData = c.getInt(c.getColumnIndex("mQOE_RO_DISCONNECT_TotData"));
                        dbr.mNotInetRO_DISCONNECT_Cnt = c.getShort(c.getColumnIndex("mNotInetRO_DISCONNECT_Cnt"));
                        dbr.mNotInetRO_DISCONNECT_TotData = c.getInt(c.getColumnIndex("mNotInetRO_DISCONNECT_TotData"));
                        dbr.mTotWifiConnectTime = c.getInt(c.getColumnIndex("mTotWifiConnectTime"));
                        dbr.mActiveCheckRS_Diff = c.getShort(c.getColumnIndex("mActiveCheckRS_Diff"));
                        dbr.mNoInetAlarmOnConnCnt = c.getShort(c.getColumnIndex("mNoInetAlarmOnConnCnt"));
                        dbr.mPortalNoAutoConnCnt = c.getShort(c.getColumnIndex("mPortalNoAutoConnCnt"));
                        dbr.mHomeAPAddRoPeriodCnt = c.getShort(c.getColumnIndex("mHomeAPAddRoPeriodCnt"));
                        dbr.mHomeAPQoeBadCnt = c.getShort(c.getColumnIndex("mHomeAPQoeBadCnt"));
                        dbr.mHistoryTotWifiConnHour = c.getInt(c.getColumnIndex("mHistoryTotWifiConnHour"));
                        dbr.mBigRTT_RO_Tot = c.getShort(c.getColumnIndex("mBigRTT_RO_Tot"));
                        dbr.mBigRTT_ErrRO_Tot = c.getShort(c.getColumnIndex("mBigRTT_ErrRO_Tot"));
                        dbr.mTotalPortalConnCount = c.getShort(c.getColumnIndex("mTotalPortalConnCount"));
                        dbr.mTotalPortalAuthSuccCount = c.getShort(c.getColumnIndex("mTotalPortalAuthSuccCount"));
                        dbr.mManualConnBlockPortalCount = c.getShort(c.getColumnIndex("mManualConnBlockPortalCount"));
                        dbr.mWifiproOpenCount = c.getShort(c.getColumnIndex("mWifiproOpenCount"));
                        dbr.mWifiproCloseCount = c.getShort(c.getColumnIndex("mWifiproCloseCount"));
                        dbr.mActiveCheckRS_Same = c.getShort(c.getColumnIndex("mActiveCheckRS_Same"));
                        dbr.mSingleAP_LearnedCount = c.getShort(c.getColumnIndex("mSingleAP_LearnedCount"));
                        dbr.mSingleAP_NearbyCount = c.getShort(c.getColumnIndex("mSingleAP_NearbyCount"));
                        dbr.mSingleAP_MonitorCount = c.getShort(c.getColumnIndex("mSingleAP_MonitorCount"));
                        dbr.mSingleAP_SatisfiedCount = c.getShort(c.getColumnIndex("mSingleAP_SatisfiedCount"));
                        dbr.mSingleAP_DisapperCount = c.getShort(c.getColumnIndex("mSingleAP_DisapperCount"));
                        dbr.mSingleAP_InblacklistCount = c.getShort(c.getColumnIndex("mSingleAP_InblacklistCount"));
                        dbr.mSingleAP_ScoreNotSatisfyCount = c.getShort(c.getColumnIndex("mSingleAP_ScoreNotSatisfyCount"));
                        dbr.mSingleAP_HandoverSucCount = c.getShort(c.getColumnIndex("mSingleAP_HandoverSucCount"));
                        dbr.mSingleAP_HandoverFailCount = c.getShort(c.getColumnIndex("mSingleAP_HandoverFailCount"));
                        dbr.mSingleAP_LowFreqScan5GCount = c.getShort(c.getColumnIndex("mSingleAP_LowFreqScan5GCount"));
                        dbr.mSingleAP_MidFreqScan5GCount = c.getShort(c.getColumnIndex("mSingleAP_MidFreqScan5GCount"));
                        dbr.mSingleAP_HighFreqScan5GCount = c.getShort(c.getColumnIndex("mSingleAP_HighFreqScan5GCount"));
                        dbr.mMixedAP_LearnedCount = c.getShort(c.getColumnIndex("mMixedAP_LearnedCount"));
                        dbr.mMixedAP_NearbyCount = c.getShort(c.getColumnIndex("mMixedAP_NearbyCount"));
                        dbr.mMixedAP_MonitorCount = c.getShort(c.getColumnIndex("mMixedAP_MonitorCount"));
                        dbr.mMixedAP_SatisfiedCount = c.getShort(c.getColumnIndex("mMixedAP_SatisfiedCount"));
                        dbr.mMixedAP_DisapperCount = c.getShort(c.getColumnIndex("mMixedAP_DisapperCount"));
                        dbr.mMixedAP_InblacklistCount = c.getShort(c.getColumnIndex("mMixedAP_InblacklistCount"));
                        dbr.mMixedAP_ScoreNotSatisfyCount = c.getShort(c.getColumnIndex("mMixedAP_ScoreNotSatisfyCount"));
                        dbr.mMixedAP_HandoverSucCount = c.getShort(c.getColumnIndex("mMixedAP_HandoverSucCount"));
                        dbr.mMixedAP_HandoverFailCount = c.getShort(c.getColumnIndex("mMixedAP_HandoverFailCount"));
                        dbr.mMixedAP_LowFreqScan5GCount = c.getShort(c.getColumnIndex("mMixedAP_LowFreqScan5GCount"));
                        dbr.mMixedAP_MidFreqScan5GCount = c.getShort(c.getColumnIndex("mMixedAP_MidFreqScan5GCount"));
                        dbr.mMixedAP_HighFreqScan5GCount = c.getShort(c.getColumnIndex("mMixedAP_HighFreqScan5GCount"));
                        dbr.mCustomizedScan_SuccCount = c.getShort(c.getColumnIndex("mCustomizedScan_SuccCount"));
                        dbr.mCustomizedScan_FailCount = c.getShort(c.getColumnIndex("mCustomizedScan_FailCount"));
                        dbr.mHandoverToNotInet5GCount = c.getShort(c.getColumnIndex("mHandoverToNotInet5GCount"));
                        dbr.mHandoverTooSlowCount = c.getShort(c.getColumnIndex("mHandoverTooSlowCount"));
                        dbr.mHandoverToBad5GCount = c.getShort(c.getColumnIndex("mHandoverToBad5GCount"));
                        dbr.mUserRejectHandoverCount = c.getShort(c.getColumnIndex("mUserRejectHandoverCount"));
                        dbr.mHandoverPingpongCount = c.getShort(c.getColumnIndex("mHandoverPingpongCount"));
                        logi("read record succ, LastStatUploadTime:" + dbr.mLastStatUploadTime);
                    }
                    c.close();
                    if (recCnt > DBG_LOG_LEVEL) {
                        loge("more than one record error. use first record.");
                    } else if (recCnt == 0) {
                        logi("queryChrStatRcd not CHR statistics record.");
                    }
                    return true;
                } catch (SQLException e) {
                    loge("queryChrStatRcd error:" + e);
                    return false;
                }
            }
        }
    }

    private void logd(String msg) {
        if (printLogLevel <= DBG_LOG_LEVEL) {
            Log.d(TAG, msg);
        }
    }

    private void logi(String msg) {
        if (printLogLevel <= INFO_LOG_LEVEL) {
            Log.i(TAG, msg);
        }
    }

    private void loge(String msg) {
        if (printLogLevel <= ERROR_LOG_LEVEL) {
            Log.e(TAG, msg);
        }
    }
}
