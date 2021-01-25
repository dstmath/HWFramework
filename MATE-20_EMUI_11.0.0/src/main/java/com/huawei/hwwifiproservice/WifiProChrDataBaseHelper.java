package com.huawei.hwwifiproservice;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WifiProChrDataBaseHelper extends SQLiteOpenHelper {
    public static final String CHR_STAT_TABLE_NAME = "CHRStatTable";
    public static final String DATABASE_NAME = "wifiproChrStatistics.db";
    public static final int DATABASE_VERSION = 9;
    private static final String TAG = "WifiProChr_DataBaseHelper";

    public WifiProChrDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 9);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase db) {
        StringBuffer stringBuffer = new StringBuffer();
        appendV1Param(stringBuffer);
        appendV2Param(stringBuffer);
        appendBackGroundParam(stringBuffer);
        stringBuffer.append("[mBG_NCByConnectFail] SMALLINT,");
        stringBuffer.append("[mBG_NCByCheckFail] SMALLINT,");
        stringBuffer.append("[mBG_NCByStateErr] SMALLINT,");
        stringBuffer.append("[mBG_NCByUnknown] SMALLINT,");
        stringBuffer.append("[mBQE_CNUrl1FailCount] SMALLINT,");
        stringBuffer.append("[mBQE_CNUrl2FailCount] SMALLINT,");
        stringBuffer.append("[mBQE_CNUrl3FailCount] SMALLINT,");
        stringBuffer.append("[mBQE_NCNUrl1FailCount] SMALLINT,");
        stringBuffer.append("[mBQE_NCNUrl2FailCount] SMALLINT,");
        stringBuffer.append("[mBQE_NCNUrl3FailCount] SMALLINT,");
        stringBuffer.append("[mBQE_ScoreUnknownCount] SMALLINT,");
        stringBuffer.append("[mBQE_BindWlanFailCount] SMALLINT,");
        stringBuffer.append("[mBQE_StopBqeFailCount] SMALLINT,");
        stringBuffer.append("[mQOE_AutoRI_TotData] INTEGER,");
        stringBuffer.append("[mNotInet_AutoRI_TotData] INTEGER,");
        stringBuffer.append("[mQOE_RO_DISCONNECT_Cnt] SMALLINT,");
        stringBuffer.append("[mQOE_RO_DISCONNECT_TotData] INTEGER,");
        stringBuffer.append("[mNotInetRO_DISCONNECT_Cnt] SMALLINT,");
        stringBuffer.append("[mNotInetRO_DISCONNECT_TotData] INTEGER,");
        stringBuffer.append("[mTotWifiConnectTime] INTEGER,");
        stringBuffer.append("[mActiveCheckRS_Diff] SMALLINT,");
        stringBuffer.append("[mNoInetAlarmOnConnCnt] SMALLINT,");
        stringBuffer.append("[mPortalNoAutoConnCnt] SMALLINT,");
        stringBuffer.append("[mHomeAPAddRoPeriodCnt] SMALLINT,");
        stringBuffer.append("[mHomeAPQoeBadCnt] SMALLINT,");
        stringBuffer.append("[mHistoryTotWifiConnHour] INTEGER,");
        stringBuffer.append("[mBigRTT_RO_Tot] SMALLINT,");
        stringBuffer.append("[mBigRTT_ErrRO_Tot] SMALLINT,");
        stringBuffer.append("[mTotalPortalConnCount] SMALLINT,");
        stringBuffer.append("[mTotalPortalAuthSuccCount] SMALLINT,");
        stringBuffer.append("[mManualConnBlockPortalCount] SMALLINT,");
        stringBuffer.append("[mWifiproOpenCount] SMALLINT,");
        stringBuffer.append("[mWifiproCloseCount] SMALLINT,");
        stringBuffer.append("[mActiveCheckRS_Same] SMALLINT)");
        db.execSQL(stringBuffer.toString());
        Log.i(TAG, "wifiproChrStatistics.db : onCreate()");
    }

    private void appendBackGroundParam(StringBuffer stringBuffer) {
        stringBuffer.append("[mBG_BgRunCnt] SMALLINT,");
        stringBuffer.append("[mBG_SettingRunCnt] SMALLINT,");
        stringBuffer.append("[mBG_FreeInetOkApCnt] SMALLINT,");
        stringBuffer.append("[mBG_FishingApCnt] SMALLINT,");
        stringBuffer.append("[mBG_FreeNotInetApCnt] SMALLINT,");
        stringBuffer.append("[mBG_PortalApCnt] SMALLINT,");
        stringBuffer.append("[mBG_FailedCnt] SMALLINT,");
        stringBuffer.append("[mBG_InetNotOkActiveOk] SMALLINT,");
        stringBuffer.append("[mBG_InetOkActiveNotOk] SMALLINT,");
        stringBuffer.append("[mBG_UserSelApFishingCnt] SMALLINT,");
        stringBuffer.append("[mBG_ConntTimeoutCnt] SMALLINT,");
        stringBuffer.append("[mBG_DNSFailCnt] SMALLINT,");
        stringBuffer.append("[mBG_DHCPFailCnt] SMALLINT,");
        stringBuffer.append("[mBG_AUTH_FailCnt] SMALLINT,");
        stringBuffer.append("[mBG_AssocRejectCnt] SMALLINT,");
        stringBuffer.append("[mBG_UserSelFreeInetOkCnt] SMALLINT,");
        stringBuffer.append("[mBG_UserSelNoInetCnt] SMALLINT,");
        stringBuffer.append("[mBG_UserSelPortalCnt] SMALLINT,");
        stringBuffer.append("[mBG_FoundTwoMoreApCnt] SMALLINT,");
        stringBuffer.append("[mAF_FPNSuccNotMsmCnt] SMALLINT,");
        stringBuffer.append("[mBSG_RsGoodCnt] SMALLINT,");
        stringBuffer.append("[mBSG_RsMidCnt] SMALLINT,");
        stringBuffer.append("[mBSG_RsBadCnt] SMALLINT,");
        stringBuffer.append("[mBSG_EndIn4sCnt] SMALLINT,");
        stringBuffer.append("[mBSG_EndIn4s7sCnt] SMALLINT,");
        stringBuffer.append("[mBSG_NotEndIn7sCnt] SMALLINT,");
    }

    private void appendV2Param(StringBuffer stringBuffer) {
        stringBuffer.append("[mNotInetWifiToWifiCount] SMALLINT,");
        stringBuffer.append("[mReopenWifiRICount] SMALLINT,");
        stringBuffer.append("[mSelCSPShowDiglogCount] SMALLINT,");
        stringBuffer.append("[mSelCSPAutoSwCount] SMALLINT,");
        stringBuffer.append("[mSelCSPNotSwCount] SMALLINT,");
        stringBuffer.append("[mTotBtnRICount] SMALLINT,");
        stringBuffer.append("[mBMD_TenMNotifyCount] SMALLINT,");
        stringBuffer.append("[mBMD_TenM_RI_Count] SMALLINT,");
        stringBuffer.append("[mBMD_FiftyMNotifyCount] SMALLINT,");
        stringBuffer.append("[mBMD_FiftyM_RI_Count] SMALLINT,");
        stringBuffer.append("[mBMD_UserDelNotifyCount] SMALLINT,");
        stringBuffer.append("[mRO_TotMobileData] INTEGER,");
        stringBuffer.append("[mAF_PhoneNumSuccCnt] SMALLINT,");
        stringBuffer.append("[mAF_PhoneNumFailCnt] SMALLINT,");
        stringBuffer.append("[mAF_PasswordSuccCnt] SMALLINT,");
        stringBuffer.append("[mAF_PasswordFailCnt] SMALLINT,");
        stringBuffer.append("[mAF_AutoLoginSuccCnt] SMALLINT,");
        stringBuffer.append("[mAF_AutoLoginFailCnt] SMALLINT,");
    }

    private void appendV1Param(StringBuffer stringBuffer) {
        stringBuffer.append("CREATE TABLE if not exists [CHRStatTable] (");
        stringBuffer.append("[_id] SMALLINT, ");
        stringBuffer.append("[mLastStatUploadTime] TEXT,");
        stringBuffer.append("[mLastWifiproState] SMALLINT,");
        stringBuffer.append("[mLastWifiproStateUpdateTime] TEXT,");
        stringBuffer.append("[mEnableTotTime] INTEGER,");
        stringBuffer.append("[mNoInetHandoverCount] SMALLINT,");
        stringBuffer.append("[mPortalUnauthCount] SMALLINT,");
        stringBuffer.append("[mWifiScoCount] SMALLINT,");
        stringBuffer.append("[mPortalCodeParseCount] SMALLINT,");
        stringBuffer.append("[mRcvSMS_Count] SMALLINT,");
        stringBuffer.append("[mPortalAutoLoginCount] SMALLINT,");
        stringBuffer.append("[mCellAutoOpenCount] SMALLINT,");
        stringBuffer.append("[mCellAutoCloseCount] SMALLINT,");
        stringBuffer.append("[mTotalBQE_BadROC] SMALLINT,");
        stringBuffer.append("[mManualBackROC] SMALLINT,");
        stringBuffer.append("[mRSSI_RO_Tot] SMALLINT,");
        stringBuffer.append("[mRSSI_ErrRO_Tot] SMALLINT,");
        stringBuffer.append("[mOTA_RO_Tot] SMALLINT,");
        stringBuffer.append("[mOTA_ErrRO_Tot] SMALLINT,");
        stringBuffer.append("[mTCP_RO_Tot] SMALLINT,");
        stringBuffer.append("[mTCP_ErrRO_Tot] SMALLINT,");
        stringBuffer.append("[mManualRI_TotTime] INTEGER,");
        stringBuffer.append("[mAutoRI_TotTime] INTEGER,");
        stringBuffer.append("[mAutoRI_TotCount] SMALLINT,");
        stringBuffer.append("[mRSSI_RestoreRI_Count] SMALLINT,");
        stringBuffer.append("[mRSSI_BetterRI_Count] SMALLINT,");
        stringBuffer.append("[mTimerRI_Count] SMALLINT,");
        stringBuffer.append("[mHisScoRI_Count] SMALLINT,");
        stringBuffer.append("[mUserCancelROC] SMALLINT,");
        stringBuffer.append("[mWifiToWifiSuccCount] SMALLINT,");
        stringBuffer.append("[mNoInetAlarmCount] SMALLINT,");
        stringBuffer.append("[mWifiOobInitState] SMALLINT,");
        stringBuffer.append("[mNotAutoConnPortalCnt] SMALLINT,");
        stringBuffer.append("[mHighDataRateStopROC] SMALLINT,");
        stringBuffer.append("[mSelectNotInetAPCount] SMALLINT,");
        stringBuffer.append("[mUserUseBgScanAPCount] SMALLINT,");
        stringBuffer.append("[mPingPongCount] SMALLINT,");
        stringBuffer.append("[mBQE_BadSettingCancel] SMALLINT,");
        stringBuffer.append("[mNotInetSettingCancel] SMALLINT,");
        stringBuffer.append("[mNotInetUserCancel] SMALLINT,");
        stringBuffer.append("[mNotInetRestoreRI] SMALLINT,");
        stringBuffer.append("[mNotInetUserManualRI] SMALLINT,");
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS CHRStatTable");
        onCreate(db);
        Log.i(TAG, "wifiproChrStatistics.db : onUpgrade()");
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS CHRStatTable");
        onCreate(db);
        Log.i(TAG, "wifiproChrStatistics.db : onDowngrade()");
    }
}
