package com.huawei.hwwifiproservice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WifiProChrDataBaseManager {
    private static final int DBG_LOG_LEVEL = 1;
    private static final int ERROR_LOG_LEVEL = 3;
    private static final int INFO_LOG_LEVEL = 2;
    private static final String TAG = "WifiProChrDataBaseManager";
    private static int printLogLevel = 1;
    private static WifiProChrDataBaseManager sChrDataBaseManager;
    private final Object mChrLock = new Object();
    private SQLiteDatabase mDatabase;
    private WifiProChrDataBaseHelper mHelper;

    public WifiProChrDataBaseManager(Context context) {
        logI("WifiProChrDataBaseManager()");
        if (context != null) {
            this.mHelper = new WifiProChrDataBaseHelper(context);
            try {
                this.mDatabase = this.mHelper.getWritableDatabase();
            } catch (SQLiteCantOpenDatabaseException e) {
                logE("WifiProChrDataBaseManager(), can't open database!");
            }
        }
    }

    public static WifiProChrDataBaseManager getInstance(Context context) {
        if (sChrDataBaseManager == null) {
            sChrDataBaseManager = new WifiProChrDataBaseManager(context);
        }
        return sChrDataBaseManager;
    }

    public void closeDB() {
        synchronized (this.mChrLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    logI("closeDB()");
                    this.mDatabase.close();
                }
            }
        }
    }

    private boolean checkIfRcdExist() {
        int recCnt = 0;
        logI("checkIfRcdExist enter.");
        Cursor cursor = null;
        try {
            Cursor cursor2 = this.mDatabase.rawQuery("SELECT * FROM CHRStatTable where _id like ?", new String[]{"1"});
            while (true) {
                if (!cursor2.moveToNext()) {
                    break;
                }
                recCnt++;
                if (recCnt > 1) {
                    break;
                }
                String lastStatUploadTime = cursor2.getString(cursor2.getColumnIndex("mLastStatUploadTime"));
                logI("checkIfRcdExist read record succ, lastStatUploadTime:" + lastStatUploadTime);
            }
            if (recCnt > 1) {
                logE("more than one record error. ");
            } else if (recCnt == 0) {
                cursor2.close();
                return false;
            }
            cursor2.close();
            return true;
        } catch (SQLException e) {
            logE("checkIfRcdExist error:" + e);
            if (0 != 0) {
                cursor.close();
            }
            return false;
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    private boolean updateChrStatRcd(WifiProStatisticsRecord dbr) {
        ContentValues values = new ContentValues();
        putV1Values(dbr, values);
        putV2Param(dbr, values);
        putBackGroundParam(dbr, values);
        putV2ChrStatRcd(dbr, values);
        values.put("mActiveCheckRS_Diff", Short.valueOf(dbr.mActiveCheckRsDiff));
        values.put("mNoInetAlarmOnConnCnt", Short.valueOf(dbr.mNoInetAlarmOnConnCnt));
        values.put("mPortalNoAutoConnCnt", Short.valueOf(dbr.mPortalNoAutoConnCnt));
        values.put("mHomeAPAddRoPeriodCnt", Short.valueOf(dbr.mHomeAPAddRoPeriodCnt));
        values.put("mHomeAPQoeBadCnt", Short.valueOf(dbr.mHomeAPQoeBadCnt));
        values.put("mHistoryTotWifiConnHour", Integer.valueOf(dbr.mHistoryTotWifiConnHour));
        values.put("mBigRTT_RO_Tot", Short.valueOf(dbr.mBigRttRoTot));
        values.put("mBigRTT_ErrRO_Tot", Short.valueOf(dbr.mBigRttErrRoTot));
        values.put("mTotalPortalConnCount", Short.valueOf(dbr.mTotalPortalConnCount));
        values.put("mTotalPortalAuthSuccCount", Short.valueOf(dbr.mTotalPortalAuthSuccCount));
        values.put("mManualConnBlockPortalCount", Short.valueOf(dbr.mManualConnBlockPortalCount));
        values.put("mWifiproOpenCount", Short.valueOf(dbr.mWifiproOpenCount));
        values.put("mWifiproCloseCount", Short.valueOf(dbr.mWifiproCloseCount));
        values.put("mActiveCheckRS_Same", Short.valueOf(dbr.mActiveCheckRsSame));
        int rowChg = 0;
        try {
            rowChg = this.mDatabase.update(WifiProChrDataBaseHelper.CHR_STAT_TABLE_NAME, values, "_id like ?", new String[]{"1"});
        } catch (SQLException e) {
            logE("update error:" + e);
        }
        if (rowChg == 0) {
            logE("updateChrStatRcd update failed.");
            return false;
        }
        logI("updateChrStatRcd update succ, rowChg=" + rowChg);
        return true;
    }

    private void putV2ChrStatRcd(WifiProStatisticsRecord dbr, ContentValues values) {
        values.put("mBG_NCByConnectFail", Short.valueOf(dbr.mBgNcbyConnectFail));
        values.put("mBG_NCByCheckFail", Short.valueOf(dbr.mBgNcbyCheckFail));
        values.put("mBG_NCByStateErr", Short.valueOf(dbr.mBgNcbyStateErr));
        values.put("mBG_NCByUnknown", Short.valueOf(dbr.mBgNcbyUnknown));
        values.put("mBQE_CNUrl1FailCount", Short.valueOf(dbr.mBqeCnUrl1FailCount));
        values.put("mBQE_CNUrl2FailCount", Short.valueOf(dbr.mBqeCnUrl2FailCount));
        values.put("mBQE_CNUrl3FailCount", Short.valueOf(dbr.mBqeCnUrl3FailCount));
        values.put("mBQE_NCNUrl1FailCount", Short.valueOf(dbr.mBqenCnUrl1FailCount));
        values.put("mBQE_NCNUrl2FailCount", Short.valueOf(dbr.mBqenCnUrl2FailCount));
        values.put("mBQE_NCNUrl3FailCount", Short.valueOf(dbr.mBqenCnUrl3FailCount));
        values.put("mBQE_ScoreUnknownCount", Short.valueOf(dbr.mBqeScoreUnknownCount));
        values.put("mBQE_BindWlanFailCount", Short.valueOf(dbr.mBqeBindWlanFailCount));
        values.put("mBQE_StopBqeFailCount", Short.valueOf(dbr.mBqeStopBqeFailCount));
        values.put("mQOE_AutoRI_TotData", Integer.valueOf(dbr.mQoeAutoRiTotData));
        values.put("mNotInet_AutoRI_TotData", Integer.valueOf(dbr.mNotInetAutoRiTotData));
        values.put("mQOE_RO_DISCONNECT_Cnt", Short.valueOf(dbr.mQoeRoDisconnectCnt));
        values.put("mQOE_RO_DISCONNECT_TotData", Integer.valueOf(dbr.mQoeRoDisconnectTotData));
        values.put("mNotInetRO_DISCONNECT_Cnt", Short.valueOf(dbr.mNotInetRoDisconnectCnt));
        values.put("mNotInetRO_DISCONNECT_TotData", Integer.valueOf(dbr.mNotInetRoDisconnectTotData));
        values.put("mTotWifiConnectTime", Integer.valueOf(dbr.mTotWifiConnectTime));
    }

    private void putBackGroundParam(WifiProStatisticsRecord dbr, ContentValues values) {
        values.put("mBG_BgRunCnt", Short.valueOf(dbr.mBgBgRunCnt));
        values.put("mBG_SettingRunCnt", Short.valueOf(dbr.mBgSettingRunCnt));
        values.put("mBG_FreeInetOkApCnt", Short.valueOf(dbr.mBgFreeInetOkApCnt));
        values.put("mBG_FishingApCnt", Short.valueOf(dbr.mBgFishingApCnt));
        values.put("mBG_FreeNotInetApCnt", Short.valueOf(dbr.mBgFreeNotInetApCnt));
        values.put("mBG_PortalApCnt", Short.valueOf(dbr.mBgPortalApCnt));
        values.put("mBG_FailedCnt", Short.valueOf(dbr.mBgFailedCnt));
        values.put("mBG_InetNotOkActiveOk", Short.valueOf(dbr.mBgInetNotOkActiveOk));
        values.put("mBG_InetOkActiveNotOk", Short.valueOf(dbr.mBgInetOkActiveNotOk));
        values.put("mBG_UserSelApFishingCnt", Short.valueOf(dbr.mBgUserSelApFishingCnt));
        values.put("mBG_ConntTimeoutCnt", Short.valueOf(dbr.mBgConntTimeoutCnt));
        values.put("mBG_DNSFailCnt", Short.valueOf(dbr.mBgDnsFailCnt));
        values.put("mBG_DHCPFailCnt", Short.valueOf(dbr.mBgDhcpFailCnt));
        values.put("mBG_AUTH_FailCnt", Short.valueOf(dbr.mBgAuthFailCnt));
        values.put("mBG_AssocRejectCnt", Short.valueOf(dbr.mBgAssocRejectCnt));
        values.put("mBG_UserSelFreeInetOkCnt", Short.valueOf(dbr.mBgUserSelFreeInetOkCnt));
        values.put("mBG_UserSelNoInetCnt", Short.valueOf(dbr.mBgUserSelNoInetCnt));
        values.put("mBG_UserSelPortalCnt", Short.valueOf(dbr.mBgUserSelPortalCnt));
        values.put("mBG_FoundTwoMoreApCnt", Short.valueOf(dbr.mBgFoundTwoMoreApCnt));
        values.put("mAF_FPNSuccNotMsmCnt", Short.valueOf(dbr.mAfFpnsSuccNotMsmCnt));
        values.put("mBSG_RsGoodCnt", Short.valueOf(dbr.mBsgRsGoodCnt));
        values.put("mBSG_RsMidCnt", Short.valueOf(dbr.mBsgRsMidCnt));
        values.put("mBSG_RsBadCnt", Short.valueOf(dbr.mBsgRsBadCnt));
        values.put("mBSG_EndIn4sCnt", Short.valueOf(dbr.mBsgEndIn4sCnt));
        values.put("mBSG_EndIn4s7sCnt", Short.valueOf(dbr.mBsgEndIn4s7sCnt));
        values.put("mBSG_NotEndIn7sCnt", Short.valueOf(dbr.mBsgNotEndIn7sCnt));
    }

    private void putV2Param(WifiProStatisticsRecord dbr, ContentValues values) {
        values.put("mNotInetWifiToWifiCount", Short.valueOf(dbr.mNotInetWifiToWifiCount));
        values.put("mReopenWifiRICount", Short.valueOf(dbr.mReopenWifiRICount));
        values.put("mSelCSPShowDiglogCount", Short.valueOf(dbr.mSelCSPShowDiglogCount));
        values.put("mSelCSPAutoSwCount", Short.valueOf(dbr.mSelCSPAutoSwCount));
        values.put("mSelCSPNotSwCount", Short.valueOf(dbr.mSelCSPNotSwCount));
        values.put("mTotBtnRICount", Short.valueOf(dbr.mTotBtnRICount));
        values.put("mBMD_TenMNotifyCount", Short.valueOf(dbr.mBmdTenmNotifyCount));
        values.put("mBMD_TenM_RI_Count", Short.valueOf(dbr.mBmdTenmRiCount));
        values.put("mBMD_FiftyMNotifyCount", Short.valueOf(dbr.mBmdFiftymNotifyCount));
        values.put("mBMD_FiftyM_RI_Count", Short.valueOf(dbr.mBmdFiftymRiCount));
        values.put("mBMD_UserDelNotifyCount", Short.valueOf(dbr.mBmdUserDelNotifyCount));
        values.put("mRO_TotMobileData", Integer.valueOf(dbr.mRoTotMobileData));
        values.put("mAF_PhoneNumSuccCnt", Short.valueOf(dbr.mAfPhoneNumSuccCnt));
        values.put("mAF_PhoneNumFailCnt", Short.valueOf(dbr.mAfPhoneNumFailCnt));
        values.put("mAF_PasswordSuccCnt", Short.valueOf(dbr.mAfPasswordSuccCnt));
        values.put("mAF_PasswordFailCnt", Short.valueOf(dbr.mAfPasswordFailCnt));
        values.put("mAF_AutoLoginSuccCnt", Short.valueOf(dbr.mAfAutoLoginSuccCnt));
        values.put("mAF_AutoLoginFailCnt", Short.valueOf(dbr.mAfAutoLoginFailCnt));
    }

    private void putV1Values(WifiProStatisticsRecord dbr, ContentValues values) {
        values.put("mLastStatUploadTime", dbr.mLastStatUploadTime);
        values.put("mLastWifiproState", Short.valueOf(dbr.mLastWifiproState));
        values.put("mLastWifiproStateUpdateTime", dbr.mLastWifiproStateUpdateTime);
        values.put("mEnableTotTime", Integer.valueOf(dbr.mEnableTotTime));
        values.put("mNoInetHandoverCount", Short.valueOf(dbr.mNoInetHandoverCount));
        values.put("mPortalUnauthCount", Short.valueOf(dbr.mPortalUnauthCount));
        values.put("mWifiScoCount", Short.valueOf(dbr.mWifiScoCount));
        values.put("mPortalCodeParseCount", Short.valueOf(dbr.mPortalCodeParseCount));
        values.put("mRcvSMS_Count", Short.valueOf(dbr.mRcvSmsCount));
        values.put("mPortalAutoLoginCount", Short.valueOf(dbr.mPortalAutoLoginCount));
        values.put("mCellAutoOpenCount", Short.valueOf(dbr.mCellAutoOpenCount));
        values.put("mCellAutoCloseCount", Short.valueOf(dbr.mCellAutoCloseCount));
        values.put("mTotalBQE_BadROC", Short.valueOf(dbr.mTotalBqeBadRoc));
        values.put("mManualBackROC", Short.valueOf(dbr.mManualBackROC));
        values.put("mRSSI_RO_Tot", Short.valueOf(dbr.mRssiRoTot));
        values.put("mRSSI_ErrRO_Tot", Short.valueOf(dbr.mRssiErrRoTot));
        values.put("mOTA_RO_Tot", Short.valueOf(dbr.mOtaRoTot));
        values.put("mOTA_ErrRO_Tot", Short.valueOf(dbr.mOtaErrRoTot));
        values.put("mTCP_RO_Tot", Short.valueOf(dbr.mTcpRoTot));
        values.put("mTCP_ErrRO_Tot", Short.valueOf(dbr.mTcpErrRoTot));
        values.put("mManualRI_TotTime", Integer.valueOf(dbr.mManualRiTotTime));
        values.put("mAutoRI_TotTime", Integer.valueOf(dbr.mAutoRiTotTime));
        values.put("mAutoRI_TotCount", Short.valueOf(dbr.mAutoRiTotCount));
        values.put("mRSSI_RestoreRI_Count", Short.valueOf(dbr.mRssiRestoreRiCount));
        values.put("mRSSI_BetterRI_Count", Short.valueOf(dbr.mRssiBetterRiCount));
        values.put("mTimerRI_Count", Short.valueOf(dbr.mTimerRiCount));
        values.put("mHisScoRI_Count", Short.valueOf(dbr.mHisScoRiCount));
        values.put("mUserCancelROC", Short.valueOf(dbr.mUserCancelROC));
        values.put("mWifiToWifiSuccCount", Short.valueOf(dbr.mWifiToWifiSuccCount));
        values.put("mNoInetAlarmCount", Short.valueOf(dbr.mNoInetAlarmCount));
        values.put("mWifiOobInitState", Short.valueOf(dbr.mWifiOobInitState));
        values.put("mNotAutoConnPortalCnt", Short.valueOf(dbr.mNotAutoConnPortalCnt));
        values.put("mHighDataRateStopROC", Short.valueOf(dbr.mHighDataRateStopROC));
        values.put("mSelectNotInetAPCount", Short.valueOf(dbr.mSelectNotInetAPCount));
        values.put("mUserUseBgScanAPCount", Short.valueOf(dbr.mUserUseBgScanAPCount));
        values.put("mPingPongCount", Short.valueOf(dbr.mPingPongCount));
        values.put("mBQE_BadSettingCancel", Short.valueOf(dbr.mBqeBadSettingCancel));
        values.put("mNotInetSettingCancel", Short.valueOf(dbr.mNotInetSettingCancel));
        values.put("mNotInetUserCancel", Short.valueOf(dbr.mNotInetUserCancel));
        values.put("mNotInetRestoreRI", Short.valueOf(dbr.mNotInetRestoreRI));
        values.put("mNotInetUserManualRI", Short.valueOf(dbr.mNotInetUserManualRI));
    }

    private boolean insertChrStatRcd(WifiProStatisticsRecord dbr) {
        SQLiteDatabase sQLiteDatabase;
        logI("insertChrStatRcd enter.");
        synchronized (this.mChrLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (dbr == null) {
                        logE("insertChrStatRcd null error.");
                        return false;
                    }
                    this.mDatabase.beginTransaction();
                    try {
                        this.mDatabase.execSQL("INSERT INTO CHRStatTable VALUES(?,   ?, ?, ?, ?, ?,   ?, ?, ?, ?, ?, ?,   ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,   ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,     ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?,    ?,?,?,?,?,?)", getObjects(dbr, 1));
                        this.mDatabase.setTransactionSuccessful();
                        logI("insertChrStatRcd update or add a record succ");
                        sQLiteDatabase = this.mDatabase;
                    } catch (SQLException e) {
                        logE("insertChrStatRcd error:" + e);
                        sQLiteDatabase = this.mDatabase;
                    } catch (Throwable th) {
                        this.mDatabase.endTransaction();
                        throw th;
                    }
                    sQLiteDatabase.endTransaction();
                    return true;
                }
            }
            logE("insertChrStatRcd database error.");
            return false;
        }
    }

    private Object[] getObjects(WifiProStatisticsRecord dbr, short idValue) {
        return new Object[]{Short.valueOf(idValue), dbr.mLastStatUploadTime, Short.valueOf(dbr.mLastWifiproState), dbr.mLastWifiproStateUpdateTime, Integer.valueOf(dbr.mEnableTotTime), Short.valueOf(dbr.mNoInetHandoverCount), Short.valueOf(dbr.mPortalUnauthCount), Short.valueOf(dbr.mWifiScoCount), Short.valueOf(dbr.mPortalCodeParseCount), Short.valueOf(dbr.mRcvSmsCount), Short.valueOf(dbr.mPortalAutoLoginCount), Short.valueOf(dbr.mCellAutoOpenCount), Short.valueOf(dbr.mCellAutoCloseCount), Short.valueOf(dbr.mTotalBqeBadRoc), Short.valueOf(dbr.mManualBackROC), Short.valueOf(dbr.mRssiRoTot), Short.valueOf(dbr.mRssiErrRoTot), Short.valueOf(dbr.mOtaRoTot), Short.valueOf(dbr.mOtaErrRoTot), Short.valueOf(dbr.mTcpRoTot), Short.valueOf(dbr.mTcpErrRoTot), Integer.valueOf(dbr.mManualRiTotTime), Integer.valueOf(dbr.mAutoRiTotTime), Short.valueOf(dbr.mAutoRiTotCount), Short.valueOf(dbr.mRssiRestoreRiCount), Short.valueOf(dbr.mRssiBetterRiCount), Short.valueOf(dbr.mTimerRiCount), Short.valueOf(dbr.mHisScoRiCount), Short.valueOf(dbr.mUserCancelROC), Short.valueOf(dbr.mWifiToWifiSuccCount), Short.valueOf(dbr.mNoInetAlarmCount), Short.valueOf(dbr.mWifiOobInitState), Short.valueOf(dbr.mNotAutoConnPortalCnt), Short.valueOf(dbr.mHighDataRateStopROC), Short.valueOf(dbr.mSelectNotInetAPCount), Short.valueOf(dbr.mUserUseBgScanAPCount), Short.valueOf(dbr.mPingPongCount), Short.valueOf(dbr.mBqeBadSettingCancel), Short.valueOf(dbr.mNotInetSettingCancel), Short.valueOf(dbr.mNotInetUserCancel), Short.valueOf(dbr.mNotInetRestoreRI), Short.valueOf(dbr.mNotInetUserManualRI), Short.valueOf(dbr.mNotInetWifiToWifiCount), Short.valueOf(dbr.mReopenWifiRICount), Short.valueOf(dbr.mSelCSPShowDiglogCount), Short.valueOf(dbr.mSelCSPAutoSwCount), Short.valueOf(dbr.mSelCSPNotSwCount), Short.valueOf(dbr.mTotBtnRICount), Short.valueOf(dbr.mBmdTenmNotifyCount), Short.valueOf(dbr.mBmdTenmRiCount), Short.valueOf(dbr.mBmdFiftymNotifyCount), Short.valueOf(dbr.mBmdFiftymRiCount), Short.valueOf(dbr.mBmdUserDelNotifyCount), Integer.valueOf(dbr.mRoTotMobileData), Short.valueOf(dbr.mAfPhoneNumSuccCnt), Short.valueOf(dbr.mAfPhoneNumFailCnt), Short.valueOf(dbr.mAfPasswordSuccCnt), Short.valueOf(dbr.mAfPasswordFailCnt), Short.valueOf(dbr.mAfAutoLoginSuccCnt), Short.valueOf(dbr.mAfAutoLoginFailCnt), Short.valueOf(dbr.mBgBgRunCnt), Short.valueOf(dbr.mBgSettingRunCnt), Short.valueOf(dbr.mBgFreeInetOkApCnt), Short.valueOf(dbr.mBgFishingApCnt), Short.valueOf(dbr.mBgFreeNotInetApCnt), Short.valueOf(dbr.mBgPortalApCnt), Short.valueOf(dbr.mBgFailedCnt), Short.valueOf(dbr.mBgInetNotOkActiveOk), Short.valueOf(dbr.mBgInetOkActiveNotOk), Short.valueOf(dbr.mBgUserSelApFishingCnt), Short.valueOf(dbr.mBgConntTimeoutCnt), Short.valueOf(dbr.mBgDnsFailCnt), Short.valueOf(dbr.mBgDhcpFailCnt), Short.valueOf(dbr.mBgAuthFailCnt), Short.valueOf(dbr.mBgAssocRejectCnt), Short.valueOf(dbr.mBgUserSelFreeInetOkCnt), Short.valueOf(dbr.mBgUserSelNoInetCnt), Short.valueOf(dbr.mBgUserSelPortalCnt), Short.valueOf(dbr.mBgFoundTwoMoreApCnt), Short.valueOf(dbr.mAfFpnsSuccNotMsmCnt), Short.valueOf(dbr.mBsgRsGoodCnt), Short.valueOf(dbr.mBsgRsMidCnt), Short.valueOf(dbr.mBsgRsBadCnt), Short.valueOf(dbr.mBsgEndIn4sCnt), Short.valueOf(dbr.mBsgEndIn4s7sCnt), Short.valueOf(dbr.mBsgNotEndIn7sCnt), Short.valueOf(dbr.mBgNcbyConnectFail), Short.valueOf(dbr.mBgNcbyCheckFail), Short.valueOf(dbr.mBgNcbyStateErr), Short.valueOf(dbr.mBgNcbyUnknown), Short.valueOf(dbr.mBqeCnUrl1FailCount), Short.valueOf(dbr.mBqeCnUrl2FailCount), Short.valueOf(dbr.mBqeCnUrl3FailCount), Short.valueOf(dbr.mBqenCnUrl1FailCount), Short.valueOf(dbr.mBqenCnUrl2FailCount), Short.valueOf(dbr.mBqenCnUrl3FailCount), Short.valueOf(dbr.mBqeScoreUnknownCount), Short.valueOf(dbr.mBqeBindWlanFailCount), Short.valueOf(dbr.mBqeStopBqeFailCount), Integer.valueOf(dbr.mQoeAutoRiTotData), Integer.valueOf(dbr.mNotInetAutoRiTotData), Short.valueOf(dbr.mQoeRoDisconnectCnt), Integer.valueOf(dbr.mQoeRoDisconnectTotData), Short.valueOf(dbr.mNotInetRoDisconnectCnt), Integer.valueOf(dbr.mNotInetRoDisconnectTotData), Integer.valueOf(dbr.mTotWifiConnectTime), Short.valueOf(dbr.mActiveCheckRsDiff), Short.valueOf(dbr.mNoInetAlarmOnConnCnt), Short.valueOf(dbr.mPortalNoAutoConnCnt), Short.valueOf(dbr.mHomeAPAddRoPeriodCnt), Short.valueOf(dbr.mHomeAPQoeBadCnt), Integer.valueOf(dbr.mHistoryTotWifiConnHour), Short.valueOf(dbr.mBigRttRoTot), Short.valueOf(dbr.mBigRttErrRoTot), Short.valueOf(dbr.mTotalPortalConnCount), Short.valueOf(dbr.mTotalPortalAuthSuccCount), Short.valueOf(dbr.mManualConnBlockPortalCount), Short.valueOf(dbr.mWifiproOpenCount), Short.valueOf(dbr.mWifiproCloseCount), Short.valueOf(dbr.mActiveCheckRsSame)};
    }

    public boolean addOrUpdateChrStatRcd(WifiProStatisticsRecord dbr) {
        synchronized (this.mChrLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (dbr != null) {
                    if (checkIfRcdExist()) {
                        return updateChrStatRcd(dbr);
                    }
                    return insertChrStatRcd(dbr);
                }
            }
            logE("insertChrStatRcd error.");
            return false;
        }
    }

    public boolean queryChrStatRcd(WifiProStatisticsRecord dbr) {
        logD("queryChrStatRcd enter.");
        synchronized (this.mChrLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (dbr == null) {
                        logE("queryChrStatRcd null error.");
                        return false;
                    }
                    Cursor cursor = null;
                    try {
                        Cursor cursor2 = this.mDatabase.rawQuery("SELECT * FROM CHRStatTable where _id like ?", new String[]{"1"});
                        int recCnt = 0;
                        while (true) {
                            if (!cursor2.moveToNext()) {
                                break;
                            }
                            recCnt++;
                            if (recCnt > 1) {
                                break;
                            }
                            queryV1Param(dbr, cursor2);
                            queryV2Param(dbr, cursor2);
                            queryBackGroundParam(dbr, cursor2);
                            queryV2Chr(dbr, cursor2);
                            queryV3AndV4Param(dbr, cursor2);
                            logI("read record succ, LastStatUploadTime:" + dbr.mLastStatUploadTime);
                        }
                        if (recCnt > 1) {
                            logE("more than one record error. use first record.");
                        } else if (recCnt == 0) {
                            logI("queryChrStatRcd not CHR statistics record.");
                        }
                        cursor2.close();
                        return true;
                    } catch (SQLException e) {
                        logE("queryChrStatRcd error:" + e);
                        if (0 != 0) {
                            cursor.close();
                        }
                        return false;
                    } catch (Throwable th) {
                        if (0 != 0) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            }
            logE("queryChrStatRcd database error.");
            return false;
        }
    }

    private void queryV3AndV4Param(WifiProStatisticsRecord dbr, Cursor cursor) {
        dbr.mActiveCheckRsDiff = cursor.getShort(cursor.getColumnIndex("mActiveCheckRS_Diff"));
        dbr.mNoInetAlarmOnConnCnt = cursor.getShort(cursor.getColumnIndex("mNoInetAlarmOnConnCnt"));
        dbr.mPortalNoAutoConnCnt = cursor.getShort(cursor.getColumnIndex("mPortalNoAutoConnCnt"));
        dbr.mHomeAPAddRoPeriodCnt = cursor.getShort(cursor.getColumnIndex("mHomeAPAddRoPeriodCnt"));
        dbr.mHomeAPQoeBadCnt = cursor.getShort(cursor.getColumnIndex("mHomeAPQoeBadCnt"));
        dbr.mHistoryTotWifiConnHour = cursor.getInt(cursor.getColumnIndex("mHistoryTotWifiConnHour"));
        dbr.mBigRttRoTot = cursor.getShort(cursor.getColumnIndex("mBigRTT_RO_Tot"));
        dbr.mBigRttErrRoTot = cursor.getShort(cursor.getColumnIndex("mBigRTT_ErrRO_Tot"));
        dbr.mTotalPortalConnCount = cursor.getShort(cursor.getColumnIndex("mTotalPortalConnCount"));
        dbr.mTotalPortalAuthSuccCount = cursor.getShort(cursor.getColumnIndex("mTotalPortalAuthSuccCount"));
        dbr.mManualConnBlockPortalCount = cursor.getShort(cursor.getColumnIndex("mManualConnBlockPortalCount"));
        dbr.mWifiproOpenCount = cursor.getShort(cursor.getColumnIndex("mWifiproOpenCount"));
        dbr.mWifiproCloseCount = cursor.getShort(cursor.getColumnIndex("mWifiproCloseCount"));
        dbr.mActiveCheckRsSame = cursor.getShort(cursor.getColumnIndex("mActiveCheckRS_Same"));
    }

    private void queryV2Chr(WifiProStatisticsRecord dbr, Cursor cursor) {
        dbr.mBgNcbyConnectFail = cursor.getShort(cursor.getColumnIndex("mBG_NCByConnectFail"));
        dbr.mBgNcbyCheckFail = cursor.getShort(cursor.getColumnIndex("mBG_NCByCheckFail"));
        dbr.mBgNcbyStateErr = cursor.getShort(cursor.getColumnIndex("mBG_NCByStateErr"));
        dbr.mBgNcbyUnknown = cursor.getShort(cursor.getColumnIndex("mBG_NCByUnknown"));
        dbr.mBqeCnUrl1FailCount = cursor.getShort(cursor.getColumnIndex("mBQE_CNUrl1FailCount"));
        dbr.mBqeCnUrl2FailCount = cursor.getShort(cursor.getColumnIndex("mBQE_CNUrl2FailCount"));
        dbr.mBqeCnUrl3FailCount = cursor.getShort(cursor.getColumnIndex("mBQE_CNUrl3FailCount"));
        dbr.mBqenCnUrl1FailCount = cursor.getShort(cursor.getColumnIndex("mBQE_NCNUrl1FailCount"));
        dbr.mBqenCnUrl2FailCount = cursor.getShort(cursor.getColumnIndex("mBQE_NCNUrl2FailCount"));
        dbr.mBqenCnUrl3FailCount = cursor.getShort(cursor.getColumnIndex("mBQE_NCNUrl3FailCount"));
        dbr.mBqeScoreUnknownCount = cursor.getShort(cursor.getColumnIndex("mBQE_ScoreUnknownCount"));
        dbr.mBqeBindWlanFailCount = cursor.getShort(cursor.getColumnIndex("mBQE_BindWlanFailCount"));
        dbr.mBqeStopBqeFailCount = cursor.getShort(cursor.getColumnIndex("mBQE_StopBqeFailCount"));
        dbr.mQoeAutoRiTotData = cursor.getInt(cursor.getColumnIndex("mQOE_AutoRI_TotData"));
        dbr.mNotInetAutoRiTotData = cursor.getInt(cursor.getColumnIndex("mNotInet_AutoRI_TotData"));
        dbr.mQoeRoDisconnectCnt = cursor.getShort(cursor.getColumnIndex("mQOE_RO_DISCONNECT_Cnt"));
        dbr.mQoeRoDisconnectTotData = cursor.getInt(cursor.getColumnIndex("mQOE_RO_DISCONNECT_TotData"));
        dbr.mNotInetRoDisconnectCnt = cursor.getShort(cursor.getColumnIndex("mNotInetRO_DISCONNECT_Cnt"));
        dbr.mNotInetRoDisconnectTotData = cursor.getInt(cursor.getColumnIndex("mNotInetRO_DISCONNECT_TotData"));
        dbr.mTotWifiConnectTime = cursor.getInt(cursor.getColumnIndex("mTotWifiConnectTime"));
    }

    private void queryBackGroundParam(WifiProStatisticsRecord dbr, Cursor cursor) {
        dbr.mBgBgRunCnt = cursor.getShort(cursor.getColumnIndex("mBG_BgRunCnt"));
        dbr.mBgSettingRunCnt = cursor.getShort(cursor.getColumnIndex("mBG_SettingRunCnt"));
        dbr.mBgFreeInetOkApCnt = cursor.getShort(cursor.getColumnIndex("mBG_FreeInetOkApCnt"));
        dbr.mBgFishingApCnt = cursor.getShort(cursor.getColumnIndex("mBG_FishingApCnt"));
        dbr.mBgFreeNotInetApCnt = cursor.getShort(cursor.getColumnIndex("mBG_FreeNotInetApCnt"));
        dbr.mBgPortalApCnt = cursor.getShort(cursor.getColumnIndex("mBG_PortalApCnt"));
        dbr.mBgFailedCnt = cursor.getShort(cursor.getColumnIndex("mBG_FailedCnt"));
        dbr.mBgInetNotOkActiveOk = cursor.getShort(cursor.getColumnIndex("mBG_InetNotOkActiveOk"));
        dbr.mBgInetOkActiveNotOk = cursor.getShort(cursor.getColumnIndex("mBG_InetOkActiveNotOk"));
        dbr.mBgUserSelApFishingCnt = cursor.getShort(cursor.getColumnIndex("mBG_UserSelApFishingCnt"));
        dbr.mBgConntTimeoutCnt = cursor.getShort(cursor.getColumnIndex("mBG_ConntTimeoutCnt"));
        dbr.mBgDnsFailCnt = cursor.getShort(cursor.getColumnIndex("mBG_DNSFailCnt"));
        dbr.mBgDhcpFailCnt = cursor.getShort(cursor.getColumnIndex("mBG_DHCPFailCnt"));
        dbr.mBgAuthFailCnt = cursor.getShort(cursor.getColumnIndex("mBG_AUTH_FailCnt"));
        dbr.mBgAssocRejectCnt = cursor.getShort(cursor.getColumnIndex("mBG_AssocRejectCnt"));
        dbr.mBgUserSelFreeInetOkCnt = cursor.getShort(cursor.getColumnIndex("mBG_UserSelFreeInetOkCnt"));
        dbr.mBgUserSelNoInetCnt = cursor.getShort(cursor.getColumnIndex("mBG_UserSelNoInetCnt"));
        dbr.mBgUserSelPortalCnt = cursor.getShort(cursor.getColumnIndex("mBG_UserSelPortalCnt"));
        dbr.mBgFoundTwoMoreApCnt = cursor.getShort(cursor.getColumnIndex("mBG_FoundTwoMoreApCnt"));
        dbr.mAfFpnsSuccNotMsmCnt = cursor.getShort(cursor.getColumnIndex("mAF_FPNSuccNotMsmCnt"));
        dbr.mBsgRsGoodCnt = cursor.getShort(cursor.getColumnIndex("mBSG_RsGoodCnt"));
        dbr.mBsgRsMidCnt = cursor.getShort(cursor.getColumnIndex("mBSG_RsMidCnt"));
        dbr.mBsgRsBadCnt = cursor.getShort(cursor.getColumnIndex("mBSG_RsBadCnt"));
        dbr.mBsgEndIn4sCnt = cursor.getShort(cursor.getColumnIndex("mBSG_EndIn4sCnt"));
        dbr.mBsgEndIn4s7sCnt = cursor.getShort(cursor.getColumnIndex("mBSG_EndIn4s7sCnt"));
        dbr.mBsgNotEndIn7sCnt = cursor.getShort(cursor.getColumnIndex("mBSG_NotEndIn7sCnt"));
    }

    private void queryV2Param(WifiProStatisticsRecord dbr, Cursor cursor) {
        dbr.mNotInetWifiToWifiCount = cursor.getShort(cursor.getColumnIndex("mNotInetWifiToWifiCount"));
        dbr.mReopenWifiRICount = cursor.getShort(cursor.getColumnIndex("mReopenWifiRICount"));
        dbr.mSelCSPShowDiglogCount = cursor.getShort(cursor.getColumnIndex("mSelCSPShowDiglogCount"));
        dbr.mSelCSPAutoSwCount = cursor.getShort(cursor.getColumnIndex("mSelCSPAutoSwCount"));
        dbr.mSelCSPNotSwCount = cursor.getShort(cursor.getColumnIndex("mSelCSPNotSwCount"));
        dbr.mTotBtnRICount = cursor.getShort(cursor.getColumnIndex("mTotBtnRICount"));
        dbr.mBmdTenmNotifyCount = cursor.getShort(cursor.getColumnIndex("mBMD_TenMNotifyCount"));
        dbr.mBmdTenmRiCount = cursor.getShort(cursor.getColumnIndex("mBMD_TenM_RI_Count"));
        dbr.mBmdFiftymNotifyCount = cursor.getShort(cursor.getColumnIndex("mBMD_FiftyMNotifyCount"));
        dbr.mBmdFiftymRiCount = cursor.getShort(cursor.getColumnIndex("mBMD_FiftyM_RI_Count"));
        dbr.mBmdUserDelNotifyCount = cursor.getShort(cursor.getColumnIndex("mBMD_UserDelNotifyCount"));
        dbr.mRoTotMobileData = cursor.getInt(cursor.getColumnIndex("mRO_TotMobileData"));
        dbr.mAfPhoneNumSuccCnt = cursor.getShort(cursor.getColumnIndex("mAF_PhoneNumSuccCnt"));
        dbr.mAfPhoneNumFailCnt = cursor.getShort(cursor.getColumnIndex("mAF_PhoneNumFailCnt"));
        dbr.mAfPasswordSuccCnt = cursor.getShort(cursor.getColumnIndex("mAF_PasswordSuccCnt"));
        dbr.mAfPasswordFailCnt = cursor.getShort(cursor.getColumnIndex("mAF_PasswordFailCnt"));
        dbr.mAfAutoLoginSuccCnt = cursor.getShort(cursor.getColumnIndex("mAF_AutoLoginSuccCnt"));
        dbr.mAfAutoLoginFailCnt = cursor.getShort(cursor.getColumnIndex("mAF_AutoLoginFailCnt"));
    }

    private void queryV1Param(WifiProStatisticsRecord dbr, Cursor cursor) {
        dbr.mLastStatUploadTime = cursor.getString(cursor.getColumnIndex("mLastStatUploadTime"));
        dbr.mLastWifiproState = cursor.getShort(cursor.getColumnIndex("mLastWifiproState"));
        dbr.mLastWifiproStateUpdateTime = cursor.getString(cursor.getColumnIndex("mLastWifiproStateUpdateTime"));
        dbr.mEnableTotTime = cursor.getInt(cursor.getColumnIndex("mEnableTotTime"));
        dbr.mNoInetHandoverCount = cursor.getShort(cursor.getColumnIndex("mNoInetHandoverCount"));
        dbr.mPortalUnauthCount = cursor.getShort(cursor.getColumnIndex("mPortalUnauthCount"));
        dbr.mWifiScoCount = cursor.getShort(cursor.getColumnIndex("mWifiScoCount"));
        dbr.mPortalCodeParseCount = cursor.getShort(cursor.getColumnIndex("mPortalCodeParseCount"));
        dbr.mRcvSmsCount = cursor.getShort(cursor.getColumnIndex("mRcvSMS_Count"));
        dbr.mPortalAutoLoginCount = cursor.getShort(cursor.getColumnIndex("mPortalAutoLoginCount"));
        dbr.mCellAutoOpenCount = cursor.getShort(cursor.getColumnIndex("mCellAutoOpenCount"));
        dbr.mCellAutoCloseCount = cursor.getShort(cursor.getColumnIndex("mCellAutoCloseCount"));
        dbr.mTotalBqeBadRoc = cursor.getShort(cursor.getColumnIndex("mTotalBQE_BadROC"));
        dbr.mManualBackROC = cursor.getShort(cursor.getColumnIndex("mManualBackROC"));
        dbr.mRssiRoTot = cursor.getShort(cursor.getColumnIndex("mRSSI_RO_Tot"));
        dbr.mRssiErrRoTot = cursor.getShort(cursor.getColumnIndex("mRSSI_ErrRO_Tot"));
        dbr.mOtaRoTot = cursor.getShort(cursor.getColumnIndex("mOTA_RO_Tot"));
        dbr.mOtaErrRoTot = cursor.getShort(cursor.getColumnIndex("mOTA_ErrRO_Tot"));
        dbr.mTcpRoTot = cursor.getShort(cursor.getColumnIndex("mTCP_RO_Tot"));
        dbr.mTcpErrRoTot = cursor.getShort(cursor.getColumnIndex("mTCP_ErrRO_Tot"));
        dbr.mManualRiTotTime = cursor.getInt(cursor.getColumnIndex("mManualRI_TotTime"));
        dbr.mAutoRiTotTime = cursor.getInt(cursor.getColumnIndex("mAutoRI_TotTime"));
        dbr.mAutoRiTotCount = cursor.getShort(cursor.getColumnIndex("mAutoRI_TotCount"));
        dbr.mRssiRestoreRiCount = cursor.getShort(cursor.getColumnIndex("mRSSI_RestoreRI_Count"));
        dbr.mRssiBetterRiCount = cursor.getShort(cursor.getColumnIndex("mRSSI_BetterRI_Count"));
        dbr.mTimerRiCount = cursor.getShort(cursor.getColumnIndex("mTimerRI_Count"));
        dbr.mHisScoRiCount = cursor.getShort(cursor.getColumnIndex("mHisScoRI_Count"));
        dbr.mUserCancelROC = cursor.getShort(cursor.getColumnIndex("mUserCancelROC"));
        dbr.mWifiToWifiSuccCount = cursor.getShort(cursor.getColumnIndex("mWifiToWifiSuccCount"));
        dbr.mNoInetAlarmCount = cursor.getShort(cursor.getColumnIndex("mNoInetAlarmCount"));
        dbr.mWifiOobInitState = cursor.getShort(cursor.getColumnIndex("mWifiOobInitState"));
        dbr.mNotAutoConnPortalCnt = cursor.getShort(cursor.getColumnIndex("mNotAutoConnPortalCnt"));
        dbr.mHighDataRateStopROC = cursor.getShort(cursor.getColumnIndex("mHighDataRateStopROC"));
        dbr.mSelectNotInetAPCount = cursor.getShort(cursor.getColumnIndex("mSelectNotInetAPCount"));
        dbr.mUserUseBgScanAPCount = cursor.getShort(cursor.getColumnIndex("mUserUseBgScanAPCount"));
        dbr.mPingPongCount = cursor.getShort(cursor.getColumnIndex("mPingPongCount"));
        dbr.mBqeBadSettingCancel = cursor.getShort(cursor.getColumnIndex("mBQE_BadSettingCancel"));
        dbr.mNotInetSettingCancel = cursor.getShort(cursor.getColumnIndex("mNotInetSettingCancel"));
        dbr.mNotInetUserCancel = cursor.getShort(cursor.getColumnIndex("mNotInetUserCancel"));
        dbr.mNotInetRestoreRI = cursor.getShort(cursor.getColumnIndex("mNotInetRestoreRI"));
        dbr.mNotInetUserManualRI = cursor.getShort(cursor.getColumnIndex("mNotInetUserManualRI"));
    }

    private void logD(String msg) {
        if (printLogLevel <= 1) {
            Log.d(TAG, msg);
        }
    }

    private void logI(String msg) {
        if (printLogLevel <= 2) {
            Log.i(TAG, msg);
        }
    }

    private void logE(String msg) {
        if (printLogLevel <= 3) {
            Log.e(TAG, msg);
        }
    }
}
