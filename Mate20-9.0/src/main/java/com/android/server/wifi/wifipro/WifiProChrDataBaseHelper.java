package com.android.server.wifi.wifipro;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WifiProChrDataBaseHelper extends SQLiteOpenHelper {
    public static final String CHR_STAT_TABLE_NAME = "CHRStatTable";
    public static final String DATABASE_NAME = "wifiproChrStatistics.db";
    public static final int DATABASE_VERSION = 9;

    public WifiProChrDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 9);
    }

    public void onCreate(SQLiteDatabase db) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [CHRStatTable] (");
        sBuffer.append("[_id] SMALLINT, ");
        sBuffer.append("[mLastStatUploadTime] TEXT,");
        sBuffer.append("[mLastWifiproState] SMALLINT,");
        sBuffer.append("[mLastWifiproStateUpdateTime] TEXT,");
        sBuffer.append("[mEnableTotTime] INTEGER,");
        sBuffer.append("[mNoInetHandoverCount] SMALLINT,");
        sBuffer.append("[mPortalUnauthCount] SMALLINT,");
        sBuffer.append("[mWifiScoCount] SMALLINT,");
        sBuffer.append("[mPortalCodeParseCount] SMALLINT,");
        sBuffer.append("[mRcvSMS_Count] SMALLINT,");
        sBuffer.append("[mPortalAutoLoginCount] SMALLINT,");
        sBuffer.append("[mCellAutoOpenCount] SMALLINT,");
        sBuffer.append("[mCellAutoCloseCount] SMALLINT,");
        sBuffer.append("[mTotalBQE_BadROC] SMALLINT,");
        sBuffer.append("[mManualBackROC] SMALLINT,");
        sBuffer.append("[mRSSI_RO_Tot] SMALLINT,");
        sBuffer.append("[mRSSI_ErrRO_Tot] SMALLINT,");
        sBuffer.append("[mOTA_RO_Tot] SMALLINT,");
        sBuffer.append("[mOTA_ErrRO_Tot] SMALLINT,");
        sBuffer.append("[mTCP_RO_Tot] SMALLINT,");
        sBuffer.append("[mTCP_ErrRO_Tot] SMALLINT,");
        sBuffer.append("[mManualRI_TotTime] INTEGER,");
        sBuffer.append("[mAutoRI_TotTime] INTEGER,");
        sBuffer.append("[mAutoRI_TotCount] SMALLINT,");
        sBuffer.append("[mRSSI_RestoreRI_Count] SMALLINT,");
        sBuffer.append("[mRSSI_BetterRI_Count] SMALLINT,");
        sBuffer.append("[mTimerRI_Count] SMALLINT,");
        sBuffer.append("[mHisScoRI_Count] SMALLINT,");
        sBuffer.append("[mUserCancelROC] SMALLINT,");
        sBuffer.append("[mWifiToWifiSuccCount] SMALLINT,");
        sBuffer.append("[mNoInetAlarmCount] SMALLINT,");
        sBuffer.append("[mWifiOobInitState] SMALLINT,");
        sBuffer.append("[mNotAutoConnPortalCnt] SMALLINT,");
        sBuffer.append("[mHighDataRateStopROC] SMALLINT,");
        sBuffer.append("[mSelectNotInetAPCount] SMALLINT,");
        sBuffer.append("[mUserUseBgScanAPCount] SMALLINT,");
        sBuffer.append("[mPingPongCount] SMALLINT,");
        sBuffer.append("[mBQE_BadSettingCancel] SMALLINT,");
        sBuffer.append("[mNotInetSettingCancel] SMALLINT,");
        sBuffer.append("[mNotInetUserCancel] SMALLINT,");
        sBuffer.append("[mNotInetRestoreRI] SMALLINT,");
        sBuffer.append("[mNotInetUserManualRI] SMALLINT,");
        sBuffer.append("[mNotInetWifiToWifiCount] SMALLINT,");
        sBuffer.append("[mReopenWifiRICount] SMALLINT,");
        sBuffer.append("[mSelCSPShowDiglogCount] SMALLINT,");
        sBuffer.append("[mSelCSPAutoSwCount] SMALLINT,");
        sBuffer.append("[mSelCSPNotSwCount] SMALLINT,");
        sBuffer.append("[mTotBtnRICount] SMALLINT,");
        sBuffer.append("[mBMD_TenMNotifyCount] SMALLINT,");
        sBuffer.append("[mBMD_TenM_RI_Count] SMALLINT,");
        sBuffer.append("[mBMD_FiftyMNotifyCount] SMALLINT,");
        sBuffer.append("[mBMD_FiftyM_RI_Count] SMALLINT,");
        sBuffer.append("[mBMD_UserDelNotifyCount] SMALLINT,");
        sBuffer.append("[mRO_TotMobileData] INTEGER,");
        sBuffer.append("[mAF_PhoneNumSuccCnt] SMALLINT,");
        sBuffer.append("[mAF_PhoneNumFailCnt] SMALLINT,");
        sBuffer.append("[mAF_PasswordSuccCnt] SMALLINT,");
        sBuffer.append("[mAF_PasswordFailCnt] SMALLINT,");
        sBuffer.append("[mAF_AutoLoginSuccCnt] SMALLINT,");
        sBuffer.append("[mAF_AutoLoginFailCnt] SMALLINT,");
        sBuffer.append("[mBG_BgRunCnt] SMALLINT,");
        sBuffer.append("[mBG_SettingRunCnt] SMALLINT,");
        sBuffer.append("[mBG_FreeInetOkApCnt] SMALLINT,");
        sBuffer.append("[mBG_FishingApCnt] SMALLINT,");
        sBuffer.append("[mBG_FreeNotInetApCnt] SMALLINT,");
        sBuffer.append("[mBG_PortalApCnt] SMALLINT,");
        sBuffer.append("[mBG_FailedCnt] SMALLINT,");
        sBuffer.append("[mBG_InetNotOkActiveOk] SMALLINT,");
        sBuffer.append("[mBG_InetOkActiveNotOk] SMALLINT,");
        sBuffer.append("[mBG_UserSelApFishingCnt] SMALLINT,");
        sBuffer.append("[mBG_ConntTimeoutCnt] SMALLINT,");
        sBuffer.append("[mBG_DNSFailCnt] SMALLINT,");
        sBuffer.append("[mBG_DHCPFailCnt] SMALLINT,");
        sBuffer.append("[mBG_AUTH_FailCnt] SMALLINT,");
        sBuffer.append("[mBG_AssocRejectCnt] SMALLINT,");
        sBuffer.append("[mBG_UserSelFreeInetOkCnt] SMALLINT,");
        sBuffer.append("[mBG_UserSelNoInetCnt] SMALLINT,");
        sBuffer.append("[mBG_UserSelPortalCnt] SMALLINT,");
        sBuffer.append("[mBG_FoundTwoMoreApCnt] SMALLINT,");
        sBuffer.append("[mAF_FPNSuccNotMsmCnt] SMALLINT,");
        sBuffer.append("[mBSG_RsGoodCnt] SMALLINT,");
        sBuffer.append("[mBSG_RsMidCnt] SMALLINT,");
        sBuffer.append("[mBSG_RsBadCnt] SMALLINT,");
        sBuffer.append("[mBSG_EndIn4sCnt] SMALLINT,");
        sBuffer.append("[mBSG_EndIn4s7sCnt] SMALLINT,");
        sBuffer.append("[mBSG_NotEndIn7sCnt] SMALLINT,");
        sBuffer.append("[mBG_NCByConnectFail] SMALLINT,");
        sBuffer.append("[mBG_NCByCheckFail] SMALLINT,");
        sBuffer.append("[mBG_NCByStateErr] SMALLINT,");
        sBuffer.append("[mBG_NCByUnknown] SMALLINT,");
        sBuffer.append("[mBQE_CNUrl1FailCount] SMALLINT,");
        sBuffer.append("[mBQE_CNUrl2FailCount] SMALLINT,");
        sBuffer.append("[mBQE_CNUrl3FailCount] SMALLINT,");
        sBuffer.append("[mBQE_NCNUrl1FailCount] SMALLINT,");
        sBuffer.append("[mBQE_NCNUrl2FailCount] SMALLINT,");
        sBuffer.append("[mBQE_NCNUrl3FailCount] SMALLINT,");
        sBuffer.append("[mBQE_ScoreUnknownCount] SMALLINT,");
        sBuffer.append("[mBQE_BindWlanFailCount] SMALLINT,");
        sBuffer.append("[mBQE_StopBqeFailCount] SMALLINT,");
        sBuffer.append("[mQOE_AutoRI_TotData] INTEGER,");
        sBuffer.append("[mNotInet_AutoRI_TotData] INTEGER,");
        sBuffer.append("[mQOE_RO_DISCONNECT_Cnt] SMALLINT,");
        sBuffer.append("[mQOE_RO_DISCONNECT_TotData] INTEGER,");
        sBuffer.append("[mNotInetRO_DISCONNECT_Cnt] SMALLINT,");
        sBuffer.append("[mNotInetRO_DISCONNECT_TotData] INTEGER,");
        sBuffer.append("[mTotWifiConnectTime] INTEGER,");
        sBuffer.append("[mActiveCheckRS_Diff] SMALLINT,");
        sBuffer.append("[mNoInetAlarmOnConnCnt] SMALLINT,");
        sBuffer.append("[mPortalNoAutoConnCnt] SMALLINT,");
        sBuffer.append("[mHomeAPAddRoPeriodCnt] SMALLINT,");
        sBuffer.append("[mHomeAPQoeBadCnt] SMALLINT,");
        sBuffer.append("[mHistoryTotWifiConnHour] INTEGER,");
        sBuffer.append("[mBigRTT_RO_Tot] SMALLINT,");
        sBuffer.append("[mBigRTT_ErrRO_Tot] SMALLINT,");
        sBuffer.append("[mTotalPortalConnCount] SMALLINT,");
        sBuffer.append("[mTotalPortalAuthSuccCount] SMALLINT,");
        sBuffer.append("[mManualConnBlockPortalCount] SMALLINT,");
        sBuffer.append("[mWifiproOpenCount] SMALLINT,");
        sBuffer.append("[mWifiproCloseCount] SMALLINT,");
        sBuffer.append("[mActiveCheckRS_Same] SMALLINT)");
        db.execSQL(sBuffer.toString());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS CHRStatTable");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS CHRStatTable");
        onCreate(db);
    }
}
