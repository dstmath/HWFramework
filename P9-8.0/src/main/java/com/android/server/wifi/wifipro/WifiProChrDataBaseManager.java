package com.android.server.wifi.wifipro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.android.server.wifi.HwWifiCHRConstImpl;

public class WifiProChrDataBaseManager {
    private static final int DBG_LOG_LEVEL = 1;
    private static final int ERROR_LOG_LEVEL = 3;
    private static final int INFO_LOG_LEVEL = 2;
    private static final String TAG = "WifiProChrDataBaseManager";
    private static WifiProChrDataBaseManager mChrDataBaseManager;
    private static int printLogLevel = 1;
    private Object mChrLock = new Object();
    private SQLiteDatabase mDatabase;
    private WifiProChrDataBaseHelper mHelper;

    public WifiProChrDataBaseManager(Context context) {
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

    /* JADX WARNING: Missing block: B:9:0x0012, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void closeDB() {
        synchronized (this.mChrLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
            } else {
                logd("closeDB()");
                this.mDatabase.close();
            }
        }
    }

    private boolean checkIfRcdExist() {
        int recCnt = 0;
        logi("checkIfRcdExist enter.");
        try {
            Cursor c = this.mDatabase.rawQuery("SELECT * FROM CHRStatTable where _id like ?", new String[]{"1"});
            String lastStatUploadTime = "";
            while (c.moveToNext()) {
                recCnt++;
                if (recCnt > 1) {
                    break;
                }
                logd("checkIfRcdExist read record succ, lastStatUploadTime:" + c.getString(c.getColumnIndex("mLastStatUploadTime")));
            }
            c.close();
            if (recCnt > 1) {
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
        int rowChg = 0;
        try {
            rowChg = this.mDatabase.update(WifiProChrDataBaseHelper.CHR_STAT_TABLE_NAME, values, "_id like ?", new String[]{"1"});
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
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                loge("insertChrStatRcd database error.");
                return false;
            } else if (dbr == null) {
                loge("insertChrStatRcd null error.");
                return false;
            } else {
                this.mDatabase.beginTransaction();
                try {
                    Object[] objArr = new Object[HwWifiCHRConstImpl.WIFI_PORTAL_SAMPLES_COLLECTE];
                    objArr[0] = Short.valueOf((short) 1);
                    objArr[1] = dbr.mLastStatUploadTime;
                    objArr[2] = Short.valueOf(dbr.mLastWifiproState);
                    objArr[3] = dbr.mLastWifiproStateUpdateTime;
                    objArr[4] = Integer.valueOf(dbr.mEnableTotTime);
                    objArr[5] = Short.valueOf(dbr.mNoInetHandoverCount);
                    objArr[6] = Short.valueOf(dbr.mPortalUnauthCount);
                    objArr[7] = Short.valueOf(dbr.mWifiScoCount);
                    objArr[8] = Short.valueOf(dbr.mPortalCodeParseCount);
                    objArr[9] = Short.valueOf(dbr.mRcvSMS_Count);
                    objArr[10] = Short.valueOf(dbr.mPortalAutoLoginCount);
                    objArr[11] = Short.valueOf(dbr.mCellAutoOpenCount);
                    objArr[12] = Short.valueOf(dbr.mCellAutoCloseCount);
                    objArr[13] = Short.valueOf(dbr.mTotalBQE_BadROC);
                    objArr[14] = Short.valueOf(dbr.mManualBackROC);
                    objArr[15] = Short.valueOf(dbr.mRSSI_RO_Tot);
                    objArr[16] = Short.valueOf(dbr.mRSSI_ErrRO_Tot);
                    objArr[17] = Short.valueOf(dbr.mOTA_RO_Tot);
                    objArr[18] = Short.valueOf(dbr.mOTA_ErrRO_Tot);
                    objArr[19] = Short.valueOf(dbr.mTCP_RO_Tot);
                    objArr[20] = Short.valueOf(dbr.mTCP_ErrRO_Tot);
                    objArr[21] = Integer.valueOf(dbr.mManualRI_TotTime);
                    objArr[22] = Integer.valueOf(dbr.mAutoRI_TotTime);
                    objArr[23] = Short.valueOf(dbr.mAutoRI_TotCount);
                    objArr[24] = Short.valueOf(dbr.mRSSI_RestoreRI_Count);
                    objArr[25] = Short.valueOf(dbr.mRSSI_BetterRI_Count);
                    objArr[26] = Short.valueOf(dbr.mTimerRI_Count);
                    objArr[27] = Short.valueOf(dbr.mHisScoRI_Count);
                    objArr[28] = Short.valueOf(dbr.mUserCancelROC);
                    objArr[29] = Short.valueOf(dbr.mWifiToWifiSuccCount);
                    objArr[30] = Short.valueOf(dbr.mNoInetAlarmCount);
                    objArr[31] = Short.valueOf(dbr.mWifiOobInitState);
                    objArr[32] = Short.valueOf(dbr.mNotAutoConnPortalCnt);
                    objArr[33] = Short.valueOf(dbr.mHighDataRateStopROC);
                    objArr[34] = Short.valueOf(dbr.mSelectNotInetAPCount);
                    objArr[35] = Short.valueOf(dbr.mUserUseBgScanAPCount);
                    objArr[36] = Short.valueOf(dbr.mPingPongCount);
                    objArr[37] = Short.valueOf(dbr.mBQE_BadSettingCancel);
                    objArr[38] = Short.valueOf(dbr.mNotInetSettingCancel);
                    objArr[39] = Short.valueOf(dbr.mNotInetUserCancel);
                    objArr[40] = Short.valueOf(dbr.mNotInetRestoreRI);
                    objArr[41] = Short.valueOf(dbr.mNotInetUserManualRI);
                    objArr[42] = Short.valueOf(dbr.mNotInetWifiToWifiCount);
                    objArr[43] = Short.valueOf(dbr.mReopenWifiRICount);
                    objArr[44] = Short.valueOf(dbr.mSelCSPShowDiglogCount);
                    objArr[45] = Short.valueOf(dbr.mSelCSPAutoSwCount);
                    objArr[46] = Short.valueOf(dbr.mSelCSPNotSwCount);
                    objArr[47] = Short.valueOf(dbr.mTotBtnRICount);
                    objArr[48] = Short.valueOf(dbr.mBMD_TenMNotifyCount);
                    objArr[49] = Short.valueOf(dbr.mBMD_TenM_RI_Count);
                    objArr[50] = Short.valueOf(dbr.mBMD_FiftyMNotifyCount);
                    objArr[51] = Short.valueOf(dbr.mBMD_FiftyM_RI_Count);
                    objArr[52] = Short.valueOf(dbr.mBMD_UserDelNotifyCount);
                    objArr[53] = Integer.valueOf(dbr.mRO_TotMobileData);
                    objArr[54] = Short.valueOf(dbr.mAF_PhoneNumSuccCnt);
                    objArr[55] = Short.valueOf(dbr.mAF_PhoneNumFailCnt);
                    objArr[56] = Short.valueOf(dbr.mAF_PasswordSuccCnt);
                    objArr[57] = Short.valueOf(dbr.mAF_PasswordFailCnt);
                    objArr[58] = Short.valueOf(dbr.mAF_AutoLoginSuccCnt);
                    objArr[59] = Short.valueOf(dbr.mAF_AutoLoginFailCnt);
                    objArr[60] = Short.valueOf(dbr.mBG_BgRunCnt);
                    objArr[61] = Short.valueOf(dbr.mBG_SettingRunCnt);
                    objArr[62] = Short.valueOf(dbr.mBG_FreeInetOkApCnt);
                    objArr[63] = Short.valueOf(dbr.mBG_FishingApCnt);
                    objArr[64] = Short.valueOf(dbr.mBG_FreeNotInetApCnt);
                    objArr[65] = Short.valueOf(dbr.mBG_PortalApCnt);
                    objArr[66] = Short.valueOf(dbr.mBG_FailedCnt);
                    objArr[67] = Short.valueOf(dbr.mBG_InetNotOkActiveOk);
                    objArr[68] = Short.valueOf(dbr.mBG_InetOkActiveNotOk);
                    objArr[69] = Short.valueOf(dbr.mBG_UserSelApFishingCnt);
                    objArr[70] = Short.valueOf(dbr.mBG_ConntTimeoutCnt);
                    objArr[71] = Short.valueOf(dbr.mBG_DNSFailCnt);
                    objArr[72] = Short.valueOf(dbr.mBG_DHCPFailCnt);
                    objArr[73] = Short.valueOf(dbr.mBG_AUTH_FailCnt);
                    objArr[74] = Short.valueOf(dbr.mBG_AssocRejectCnt);
                    objArr[75] = Short.valueOf(dbr.mBG_UserSelFreeInetOkCnt);
                    objArr[76] = Short.valueOf(dbr.mBG_UserSelNoInetCnt);
                    objArr[77] = Short.valueOf(dbr.mBG_UserSelPortalCnt);
                    objArr[78] = Short.valueOf(dbr.mBG_FoundTwoMoreApCnt);
                    objArr[79] = Short.valueOf(dbr.mAF_FPNSuccNotMsmCnt);
                    objArr[80] = Short.valueOf(dbr.mBSG_RsGoodCnt);
                    objArr[81] = Short.valueOf(dbr.mBSG_RsMidCnt);
                    objArr[82] = Short.valueOf(dbr.mBSG_RsBadCnt);
                    objArr[83] = Short.valueOf(dbr.mBSG_EndIn4sCnt);
                    objArr[84] = Short.valueOf(dbr.mBSG_EndIn4s7sCnt);
                    objArr[85] = Short.valueOf(dbr.mBSG_NotEndIn7sCnt);
                    objArr[86] = Short.valueOf(dbr.mBG_NCByConnectFail);
                    objArr[87] = Short.valueOf(dbr.mBG_NCByCheckFail);
                    objArr[88] = Short.valueOf(dbr.mBG_NCByStateErr);
                    objArr[89] = Short.valueOf(dbr.mBG_NCByUnknown);
                    objArr[90] = Short.valueOf(dbr.mBQE_CNUrl1FailCount);
                    objArr[91] = Short.valueOf(dbr.mBQE_CNUrl2FailCount);
                    objArr[92] = Short.valueOf(dbr.mBQE_CNUrl3FailCount);
                    objArr[93] = Short.valueOf(dbr.mBQE_NCNUrl1FailCount);
                    objArr[94] = Short.valueOf(dbr.mBQE_NCNUrl2FailCount);
                    objArr[95] = Short.valueOf(dbr.mBQE_NCNUrl3FailCount);
                    objArr[96] = Short.valueOf(dbr.mBQE_ScoreUnknownCount);
                    objArr[97] = Short.valueOf(dbr.mBQE_BindWlanFailCount);
                    objArr[98] = Short.valueOf(dbr.mBQE_StopBqeFailCount);
                    objArr[99] = Integer.valueOf(dbr.mQOE_AutoRI_TotData);
                    objArr[100] = Integer.valueOf(dbr.mNotInet_AutoRI_TotData);
                    objArr[101] = Short.valueOf(dbr.mQOE_RO_DISCONNECT_Cnt);
                    objArr[102] = Integer.valueOf(dbr.mQOE_RO_DISCONNECT_TotData);
                    objArr[103] = Short.valueOf(dbr.mNotInetRO_DISCONNECT_Cnt);
                    objArr[104] = Integer.valueOf(dbr.mNotInetRO_DISCONNECT_TotData);
                    objArr[HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO] = Integer.valueOf(dbr.mTotWifiConnectTime);
                    objArr[HwQoEUtils.QOE_MSG_EVALUATE_OTA_INFO] = Short.valueOf(dbr.mActiveCheckRS_Diff);
                    objArr[HwQoEUtils.QOE_MSG_WIFI_ENABLED] = Short.valueOf(dbr.mNoInetAlarmOnConnCnt);
                    objArr[HwQoEUtils.QOE_MSG_WIFI_DISABLE] = Short.valueOf(dbr.mPortalNoAutoConnCnt);
                    objArr[HwQoEUtils.QOE_MSG_WIFI_CONNECTED] = Short.valueOf(dbr.mHomeAPAddRoPeriodCnt);
                    objArr[110] = Short.valueOf(dbr.mHomeAPQoeBadCnt);
                    objArr[HwQoEUtils.QOE_MSG_WIFI_INTERNET] = Integer.valueOf(dbr.mHistoryTotWifiConnHour);
                    objArr[HwQoEUtils.QOE_MSG_WIFI_CHECK_TIMEOUT] = Short.valueOf(dbr.mBigRTT_RO_Tot);
                    objArr[HwQoEUtils.QOE_MSG_WIFI_START_EVALUATE] = Short.valueOf(dbr.mBigRTT_ErrRO_Tot);
                    objArr[HwQoEUtils.QOE_MSG_WIFI_EVALUATE_TIMEOUT] = Short.valueOf(dbr.mTotalPortalConnCount);
                    objArr[HwQoEUtils.QOE_MSG_WIFI_ROAMING] = Short.valueOf(dbr.mTotalPortalAuthSuccCount);
                    objArr[HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT] = Short.valueOf(dbr.mManualConnBlockPortalCount);
                    objArr[HwQoEUtils.QOE_MSG_WIFI_RSSI_CHANGED] = Short.valueOf(dbr.mWifiproOpenCount);
                    objArr[118] = Short.valueOf(dbr.mWifiproCloseCount);
                    objArr[119] = Short.valueOf(dbr.mActiveCheckRS_Same);
                    this.mDatabase.execSQL("INSERT INTO CHRStatTable VALUES(?,   ?, ?, ?, ?, ?,   ?, ?, ?, ?, ?, ?,   ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?,    ?, ?,  ?, ?, ?, ?, ?, ?, ?, ?,   ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,     ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?,    ?,?,?,?,?,?)", objArr);
                    this.mDatabase.setTransactionSuccessful();
                    logi("insertChrStatRcd update or add a record succ");
                    this.mDatabase.endTransaction();
                } catch (SQLException e) {
                    loge("insertChrStatRcd error:" + e);
                    this.mDatabase.endTransaction();
                    return true;
                } catch (Throwable th) {
                    this.mDatabase.endTransaction();
                }
            }
        }
    }

    public boolean addOrUpdateChrStatRcd(WifiProStatisticsRecord dbr) {
        synchronized (this.mChrLock) {
            boolean updateChrStatRcd;
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || dbr == null) {
                loge("insertChrStatRcd error.");
                return false;
            } else if (checkIfRcdExist()) {
                updateChrStatRcd = updateChrStatRcd(dbr);
                return updateChrStatRcd;
            } else {
                updateChrStatRcd = insertChrStatRcd(dbr);
                return updateChrStatRcd;
            }
        }
    }

    /* JADX WARNING: Missing block: B:25:0x0054, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean queryChrStatRcd(WifiProStatisticsRecord dbr) {
        int recCnt = 0;
        logd("queryChrStatRcd enter.");
        synchronized (this.mChrLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                loge("queryChrStatRcd database error.");
                return false;
            } else if (dbr == null) {
                loge("queryChrStatRcd null error.");
                return false;
            } else {
                try {
                    Cursor c = this.mDatabase.rawQuery("SELECT * FROM CHRStatTable where _id like ?", new String[]{"1"});
                    while (c.moveToNext()) {
                        recCnt++;
                        if (recCnt > 1) {
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
                        logi("read record succ, LastStatUploadTime:" + dbr.mLastStatUploadTime);
                    }
                    c.close();
                    if (recCnt > 1) {
                        loge("more than one record error. use first record.");
                    } else if (recCnt == 0) {
                        logi("queryChrStatRcd not CHR statistics record.");
                    }
                } catch (SQLException e) {
                    loge("queryChrStatRcd error:" + e);
                    return false;
                }
            }
        }
    }

    private void logd(String msg) {
        if (printLogLevel <= 1) {
            Log.d(TAG, msg);
        }
    }

    private void logi(String msg) {
        if (printLogLevel <= 2) {
            Log.i(TAG, msg);
        }
    }

    private void loge(String msg) {
        if (printLogLevel <= 3) {
            Log.e(TAG, msg);
        }
    }
}
