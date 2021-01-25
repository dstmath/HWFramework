package com.huawei.hwwifiproservice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindowAllocationException;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class WifiProHistoryDBManager {
    private static long AGING_MS_OF_EACH_DAY = 1800000;
    private static final int DBG_LOG_LEVEL = 1;
    private static final int ERROR_LOG_LEVEL = 3;
    private static final int INFO_LOG_LEVEL = 2;
    private static final short MAX_AP_INFO_RECORD_NUM = 1;
    private static final String TAG = "WifiProHistoryDBManager";
    private static long msOfOneDay = 86400000;
    private static int printLogLevel = 1;
    private static WifiProHistoryDBManager sBqeDataBaseManager;
    private static long tooOldValidDay = 10;
    private int mApRecordCount = 0;
    private final Object mBqeLock = new Object();
    private SQLiteDatabase mDatabase;
    private WifiProHistoryDBHelper mHelper;
    private int mHomeApRecordCount = 0;
    private boolean mNeedDelOldDualBandApInfo;

    public WifiProHistoryDBManager(Context context) {
        HwHiLog.w(TAG, false, "WifiProHistoryDBManager()", new Object[0]);
        this.mHelper = new WifiProHistoryDBHelper(context);
        try {
            this.mDatabase = this.mHelper.getWritableDatabase();
        } catch (SQLiteCantOpenDatabaseException e) {
            HwHiLog.e(TAG, false, "WifiProHistoryDBManager(), can't open database!", new Object[0]);
        }
    }

    public static WifiProHistoryDBManager getInstance(Context context) {
        if (sBqeDataBaseManager == null) {
            sBqeDataBaseManager = new WifiProHistoryDBManager(context);
        }
        return sBqeDataBaseManager;
    }

    public void closeDB() {
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    HwHiLog.w(TAG, false, "closeDB()", new Object[0]);
                    this.mDatabase.close();
                }
            }
        }
    }

    private boolean deleteHistoryRecord(String dbTableName, String apBssid) {
        HwHiLog.i(TAG, false, "deleteHistoryRecord enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (apBssid == null) {
                        HwHiLog.e(TAG, false, "deleteHistoryRecord null error.", new Object[0]);
                        return false;
                    }
                    try {
                        this.mDatabase.delete(dbTableName, "apBSSID like ?", new String[]{apBssid});
                        return true;
                    } catch (SQLException e) {
                        HwHiLog.e(TAG, false, "deleteHistoryRecord error", new Object[0]);
                        return false;
                    }
                }
            }
            HwHiLog.e(TAG, false, "deleteHistoryRecord database error.", new Object[0]);
            return false;
        }
    }

    public boolean deleteApInfoRecord(String apBssid) {
        return deleteHistoryRecord(WifiProHistoryDBHelper.WP_AP_INFO_TB_NAME, apBssid);
    }

    private boolean checkHistoryRecordExist(String dbTableName, String apBssid) throws CheckHistoryRecordException {
        boolean ret = false;
        Cursor cursor = null;
        try {
            SQLiteDatabase sQLiteDatabase = this.mDatabase;
            Cursor cursor2 = sQLiteDatabase.rawQuery("SELECT * FROM " + dbTableName + " where apBSSID like ?", new String[]{apBssid});
            int rcdCount = cursor2.getCount();
            if (rcdCount > 0) {
                ret = true;
            }
            HwHiLog.d(TAG, false, "checkHistoryRecordExist read from:%{public}s, get record: %{public}d", new Object[]{dbTableName, Integer.valueOf(rcdCount)});
            cursor2.close();
            return ret;
        } catch (CursorWindowAllocationException | SQLException e) {
            HwHiLog.e(TAG, false, "checkHistoryRecordExist error", new Object[0]);
            throw new CheckHistoryRecordException();
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    private boolean updateApInfoRecord(WifiProApInfoRecord dbr) {
        ContentValues values = new ContentValues();
        values.put("apSSID", dbr.apSSID);
        values.put("apSecurityType", Integer.valueOf(dbr.apSecurityType));
        values.put("firstConnectTime", Long.valueOf(dbr.firstConnectTime));
        values.put("lastConnectTime", Long.valueOf(dbr.lastConnectTime));
        values.put("lanDataSize", Integer.valueOf(dbr.lanDataSize));
        values.put("highSpdFreq", Integer.valueOf(dbr.highSpdFreq));
        values.put("totalUseTime", Integer.valueOf(dbr.totalUseTime));
        values.put("totalUseTimeAtNight", Integer.valueOf(dbr.totalUseTimeAtNight));
        values.put("totalUseTimeAtWeekend", Integer.valueOf(dbr.totalUseTimeAtWeekend));
        values.put("judgeHomeAPTime", Long.valueOf(dbr.judgeHomeAPTime));
        try {
            int rowChg = this.mDatabase.update(WifiProHistoryDBHelper.WP_AP_INFO_TB_NAME, values, "apBSSID like ?", new String[]{dbr.apBSSID});
            if (rowChg == 0) {
                HwHiLog.e(TAG, false, "updateApInfoRecord update failed.", new Object[0]);
                return false;
            }
            HwHiLog.d(TAG, false, "updateApInfoRecord update succ, rowChg=%{public}d", new Object[]{Integer.valueOf(rowChg)});
            return true;
        } catch (SQLException e) {
            HwHiLog.e(TAG, false, "updateApInfoRecord error", new Object[0]);
            return false;
        }
    }

    private boolean insertApInfoRecord(WifiProApInfoRecord dbr) {
        try {
            this.mDatabase.execSQL("INSERT INTO WifiProApInfoRecodTable VALUES(null,  ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,?)", new Object[]{dbr.apBSSID, dbr.apSSID, Integer.valueOf(dbr.apSecurityType), Long.valueOf(dbr.firstConnectTime), Long.valueOf(dbr.lastConnectTime), Integer.valueOf(dbr.lanDataSize), Integer.valueOf(dbr.highSpdFreq), Integer.valueOf(dbr.totalUseTime), Integer.valueOf(dbr.totalUseTimeAtNight), Integer.valueOf(dbr.totalUseTimeAtWeekend), Long.valueOf(dbr.judgeHomeAPTime)});
            HwHiLog.i(TAG, false, "insertApInfoRecord add a record succ.", new Object[0]);
            return true;
        } catch (SQLException e) {
            HwHiLog.e(TAG, false, "insertApInfoRecord error", new Object[0]);
            return false;
        }
    }

    public boolean addOrUpdateApInfoRecord(WifiProApInfoRecord dbr) {
        HwHiLog.d(TAG, false, "addOrUpdateApInfoRecord enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (dbr != null) {
                    if (dbr.apBSSID == null) {
                        HwHiLog.e(TAG, false, "addOrUpdateApInfoRecord null error.", new Object[0]);
                        return false;
                    }
                    try {
                        if (checkHistoryRecordExist(WifiProHistoryDBHelper.WP_AP_INFO_TB_NAME, dbr.apBSSID)) {
                            return updateApInfoRecord(dbr);
                        }
                        return insertApInfoRecord(dbr);
                    } catch (CheckHistoryRecordException e) {
                        HwHiLog.e(TAG, false, "Exceptions happened in addOrUpdateApInfoRecord()", new Object[0]);
                        return false;
                    }
                }
            }
            HwHiLog.e(TAG, false, "addOrUpdateApInfoRecord error.", new Object[0]);
            return false;
        }
    }

    public boolean queryApInfoRecord(String apBssid, WifiProApInfoRecord dbr) {
        HwHiLog.d(TAG, false, "queryApInfoRecord enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            if (!isWifiProHistoryDatabaseOpen() || apBssid == null || dbr == null) {
                return false;
            }
            Cursor cursor = null;
            int recCnt = 0;
            try {
                Cursor cursor2 = this.mDatabase.rawQuery("SELECT * FROM WifiProApInfoRecodTable where apBSSID like ?", new String[]{apBssid});
                while (true) {
                    if (!cursor2.moveToNext()) {
                        break;
                    }
                    recCnt++;
                    if (recCnt > 1) {
                        break;
                    } else if (recCnt == 1) {
                        dbr.apBSSID = apBssid;
                        dbr.apSSID = cursor2.getString(cursor2.getColumnIndex("apSSID"));
                        dbr.apSecurityType = cursor2.getInt(cursor2.getColumnIndex("apSecurityType"));
                        dbr.firstConnectTime = cursor2.getLong(cursor2.getColumnIndex("firstConnectTime"));
                        dbr.lastConnectTime = cursor2.getLong(cursor2.getColumnIndex("lastConnectTime"));
                        dbr.lanDataSize = cursor2.getInt(cursor2.getColumnIndex("lanDataSize"));
                        dbr.highSpdFreq = cursor2.getInt(cursor2.getColumnIndex("highSpdFreq"));
                        dbr.totalUseTime = cursor2.getInt(cursor2.getColumnIndex("totalUseTime"));
                        dbr.totalUseTimeAtNight = cursor2.getInt(cursor2.getColumnIndex("totalUseTimeAtNight"));
                        dbr.totalUseTimeAtWeekend = cursor2.getInt(cursor2.getColumnIndex("totalUseTimeAtWeekend"));
                        dbr.judgeHomeAPTime = cursor2.getLong(cursor2.getColumnIndex("judgeHomeAPTime"));
                        HwHiLog.i(TAG, false, "read record succ, LastConnectTime:%{public}s", new Object[]{String.valueOf(dbr.lastConnectTime)});
                    }
                }
                cursor2.close();
                if (recCnt > 1) {
                    HwHiLog.e(TAG, false, "more than one record error. use first record.", new Object[0]);
                } else if (recCnt == 0) {
                    HwHiLog.i(TAG, false, "queryApInfoRecord not record.", new Object[0]);
                }
                return true;
            } catch (SQLException e) {
                HwHiLog.e(TAG, false, "queryApInfoRecord error", new Object[0]);
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

    public int querySameSsidApCount(String apBssid, String apSsid, int secType) {
        int recCnt = 0;
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                HwHiLog.e(TAG, false, "querySameSsidApCount database error.", new Object[0]);
                return 0;
            } else if (apBssid == null || apSsid == null) {
                HwHiLog.e(TAG, false, "querySameSsidApCount null error.", new Object[0]);
                return 0;
            } else {
                Cursor cursor = null;
                try {
                    cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProApInfoRecodTable where (apSSID like ?) and (apSecurityType = ?) and (apBSSID != ?)", new String[]{apSsid, String.valueOf(secType), apBssid});
                    recCnt = cursor.getCount();
                    HwHiLog.i(TAG, false, "querySameSsidApCount read same (SSID:%{public}s, secType:%{public}d) and different BSSID record count=%{public}d", new Object[]{StringUtilEx.safeDisplaySsid(apSsid), Integer.valueOf(secType), Integer.valueOf(recCnt)});
                    cursor.close();
                    return recCnt;
                } catch (SQLException e) {
                    HwHiLog.e(TAG, false, "querySameSsidApCount error", new Object[0]);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return recCnt;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
        }
    }

    private String getOldApBssid(Cursor cursor, Date currentDate) {
        long lastConnDateMsTime = cursor.getLong(cursor.getColumnIndex("lastConnectTime"));
        long totalConnectTime = (long) cursor.getInt(cursor.getColumnIndex("totalUseTime"));
        long currDateMsTime = currentDate.getTime();
        long pastDays = (currDateMsTime - lastConnDateMsTime) / msOfOneDay;
        if (pastDays <= tooOldValidDay) {
            return null;
        }
        if (totalConnectTime - (AGING_MS_OF_EACH_DAY * pastDays) >= 0) {
            return null;
        }
        HwHiLog.i(TAG, false, "check result: need delete.", new Object[0]);
        int id = cursor.getInt(cursor.getColumnIndex("_id"));
        String ssid = cursor.getString(cursor.getColumnIndex("apSSID"));
        String bssid = cursor.getString(cursor.getColumnIndex("apBSSID"));
        HwHiLog.i(TAG, false, "check record: ssid:%{public}s, id:%{public}d, pass time:%{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid), Integer.valueOf(id), String.valueOf(currDateMsTime - lastConnDateMsTime)});
        return bssid;
    }

    public boolean removeTooOldApInfoRecord() {
        Vector<String> delRecordsVector = new Vector<>();
        HwHiLog.d(TAG, false, "removeTooOldApInfoRecord enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            if (!isWifiProHistoryDatabaseOpen()) {
                return false;
            }
            delRecordsVector.clear();
            Cursor cursor = null;
            try {
                Cursor cursor2 = this.mDatabase.rawQuery("SELECT * FROM WifiProApInfoRecodTable", null);
                HwHiLog.d(TAG, false, "all record count=%{public}d", new Object[]{Integer.valueOf(cursor2.getCount())});
                Date currDate = new Date();
                while (cursor2.moveToNext()) {
                    String bssid = getOldApBssid(cursor2, currDate);
                    if (bssid != null) {
                        delRecordsVector.add(bssid);
                    }
                }
                int delSize = delRecordsVector.size();
                HwHiLog.i(TAG, false, "start delete %{public}d records.", new Object[]{Integer.valueOf(delSize)});
                int i = 0;
                while (true) {
                    if (i >= delSize) {
                        break;
                    }
                    String delBssid = delRecordsVector.get(i);
                    if (delBssid == null) {
                        break;
                    }
                    this.mDatabase.delete(WifiProHistoryDBHelper.WP_AP_INFO_TB_NAME, "apBSSID like ?", new String[]{delBssid});
                    i++;
                }
                cursor2.close();
                return true;
            } catch (SQLException e) {
                HwHiLog.e(TAG, false, "removeTooOldApInfoRecord error", new Object[0]);
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

    public boolean statisticApInfoRecord() {
        HwHiLog.d(TAG, false, "statisticApInfoRecord enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    this.mHomeApRecordCount = 0;
                    this.mApRecordCount = 0;
                    Cursor cursor = null;
                    try {
                        Cursor cursor2 = this.mDatabase.rawQuery("SELECT * FROM WifiProApInfoRecodTable", null);
                        this.mApRecordCount = cursor2.getCount();
                        HwHiLog.i(TAG, false, "all record count=%{public}d", new Object[]{Integer.valueOf(this.mApRecordCount)});
                        while (cursor2.moveToNext()) {
                            if (cursor2.getLong(cursor2.getColumnIndex("judgeHomeAPTime")) > 0) {
                                String ssid = cursor2.getString(cursor2.getColumnIndex("apSSID"));
                                this.mHomeApRecordCount++;
                                HwHiLog.i(TAG, false, "check record: Home ap ssid:%{public}s, total:%{public}d", new Object[]{StringUtilEx.safeDisplaySsid(ssid), Integer.valueOf(this.mHomeApRecordCount)});
                            }
                        }
                        cursor2.close();
                        return true;
                    } catch (SQLException e) {
                        HwHiLog.e(TAG, false, "removeTooOldApInfoRecord error", new Object[0]);
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
            HwHiLog.e(TAG, false, "statisticApInfoRecord database error.", new Object[0]);
            return false;
        }
    }

    public int getTotRecordCount() {
        int i;
        synchronized (this.mBqeLock) {
            i = this.mApRecordCount;
        }
        return i;
    }

    public int getHomeApRecordCount() {
        int i;
        synchronized (this.mBqeLock) {
            i = this.mHomeApRecordCount;
        }
        return i;
    }

    private boolean insertEnterpriseApRecord(String apSSID, int secType) {
        try {
            this.mDatabase.execSQL("INSERT INTO WifiProEnterpriseAPTable VALUES(null,  ?, ?)", new Object[]{apSSID, Integer.valueOf(secType)});
            HwHiLog.i(TAG, false, "insertEnterpriseApRecord add a record succ.", new Object[0]);
            return true;
        } catch (SQLException e) {
            HwHiLog.e(TAG, false, "insertEnterpriseApRecord error", new Object[0]);
            return false;
        }
    }

    public boolean addOrUpdateEnterpriseApRecord(String apSsid, int secType) {
        HwHiLog.d(TAG, false, "addOrUpdateEnterpriseApRecord enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (apSsid != null) {
                    if (!queryEnterpriseApRecord(apSsid, secType)) {
                        HwHiLog.i(TAG, false, "add record here ssid:%{public}s", new Object[]{StringUtilEx.safeDisplaySsid(apSsid)});
                        return insertEnterpriseApRecord(apSsid, secType);
                    }
                    HwHiLog.i(TAG, false, "already exist the record ssid:%{public}s", new Object[]{StringUtilEx.safeDisplaySsid(apSsid)});
                    return true;
                }
            }
            HwHiLog.e(TAG, false, "addOrUpdateEnterpriseApRecord error.", new Object[0]);
            return false;
        }
    }

    public boolean queryEnterpriseApRecord(String apSsid, int secType) {
        HwHiLog.d(TAG, false, "queryEnterpriseApRecord enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (apSsid == null) {
                        HwHiLog.e(TAG, false, "queryEnterpriseApRecord null error.", new Object[0]);
                        return false;
                    }
                    Cursor cursor = null;
                    try {
                        cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProEnterpriseAPTable where (apSSID like ?) and (apSecurityType = ?)", new String[]{apSsid, String.valueOf(secType)});
                        int recCnt = cursor.getCount();
                        cursor.close();
                        if (recCnt > 0) {
                            HwHiLog.i(TAG, false, "SSID:%{public}s, security: %{public}d is in Enterprise Ap table. count:%{public}d", new Object[]{StringUtilEx.safeDisplaySsid(apSsid), Integer.valueOf(secType), Integer.valueOf(recCnt)});
                            return true;
                        }
                        HwHiLog.i(TAG, false, "SSID:%{public}s, security: %{public}d is not in Enterprise Ap table. count:%{public}d", new Object[]{StringUtilEx.safeDisplaySsid(apSsid), Integer.valueOf(secType), Integer.valueOf(recCnt)});
                        return false;
                    } catch (SQLException e) {
                        HwHiLog.e(TAG, false, "queryEnterpriseApRecord error", new Object[0]);
                        if (cursor != null) {
                            cursor.close();
                        }
                        return false;
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            }
            HwHiLog.e(TAG, false, "queryEnterpriseApRecord database error.", new Object[0]);
            return false;
        }
    }

    public boolean deleteEnterpriseApRecord(String tableName, String ssid, int secType) {
        if (tableName == null || ssid == null) {
            HwHiLog.e(TAG, false, "deleteHistoryRecord null error.", new Object[0]);
            return false;
        }
        HwHiLog.i(TAG, false, "delete record of same (SSID:%{public}s, secType:%{public}d) from %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid), Integer.valueOf(secType), tableName});
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                HwHiLog.e(TAG, false, "deleteHistoryRecord database error.", new Object[0]);
                return false;
            }
            try {
                HwHiLog.d(TAG, false, "delete record count=%{public}d", new Object[]{Integer.valueOf(this.mDatabase.delete(tableName, "(apSSID like ?) and (apSecurityType = ?)", new String[]{ssid, String.valueOf(secType)}))});
                return true;
            } catch (SQLException e) {
                HwHiLog.e(TAG, false, "deleteHistoryRecord error", new Object[0]);
                return false;
            }
        }
    }

    public boolean deleteRelateApRcd(String apBssid) {
        return deleteHistoryRecord(WifiProHistoryDBHelper.WP_RELATE_AP_TB_NAME, apBssid);
    }

    public boolean deleteApQualityRcd(String apBssid) {
        return deleteHistoryRecord(WifiProHistoryDBHelper.WP_QUALITY_TB_NAME, apBssid);
    }

    public boolean deleteDualBandApInfoRcd(String apBssid) {
        return deleteHistoryRecord(WifiProHistoryDBHelper.WP_DUAL_BAND_AP_INFO_TB_NAME, apBssid);
    }

    public boolean deleteRelateApRcd(String apBssid, String relatedBSSID) {
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                HwHiLog.e(TAG, false, "deleteRelateApRcd database error.", new Object[0]);
                return false;
            } else if (apBssid == null || relatedBSSID == null) {
                HwHiLog.e(TAG, false, "deleteRelateApRcd null error.", new Object[0]);
                return false;
            } else {
                try {
                    this.mDatabase.delete(WifiProHistoryDBHelper.WP_RELATE_AP_TB_NAME, "(apBSSID like ?) and (RelatedBSSID like ?)", new String[]{apBssid, relatedBSSID});
                    return true;
                } catch (SQLException e) {
                    HwHiLog.e(TAG, false, "deleteRelateApRcd error", new Object[0]);
                    return false;
                }
            }
        }
    }

    public boolean deleteRelate5GAPRcd(String relatedBssid) {
        HwHiLog.i(TAG, false, "deleteRelateApRcd enter", new Object[0]);
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (relatedBssid == null) {
                        HwHiLog.e(TAG, false, "deleteRelateApRcd null error.", new Object[0]);
                        return false;
                    }
                    try {
                        this.mDatabase.delete(WifiProHistoryDBHelper.WP_RELATE_AP_TB_NAME, "(RelatedBSSID like ?)", new String[]{relatedBssid});
                        return true;
                    } catch (SQLException e) {
                        HwHiLog.e(TAG, false, "deleteRelateApRcd error", new Object[0]);
                        return false;
                    }
                }
            }
            HwHiLog.e(TAG, false, "deleteRelateApRcd database error.", new Object[0]);
            return false;
        }
    }

    private boolean deleteAllDualBandAPRcd(String apBssid) {
        return deleteDualBandApInfoRcd(apBssid) && deleteApQualityRcd(apBssid) && deleteRelateApRcd(apBssid) && deleteRelate5GAPRcd(apBssid);
    }

    private boolean updateApQualityRcd(WifiProApQualityRcd dbr) {
        ContentValues values = new ContentValues();
        values.put("RTT_Product", dbr.getRttProduct());
        values.put("RTT_PacketVolume", dbr.getRttPacketVolume());
        values.put("HistoryAvgRtt", dbr.getHistoryAvgRtt());
        values.put("OTA_LostRateValue", dbr.getOtaLostRateValue());
        values.put("OTA_PktVolume", dbr.getOtaPktVolume());
        values.put("OTA_BadPktProduct", dbr.getOtaBadPktProduct());
        try {
            int rowChg = this.mDatabase.update(WifiProHistoryDBHelper.WP_QUALITY_TB_NAME, values, "apBSSID like ?", new String[]{dbr.getApBssid()});
            if (rowChg == 0) {
                HwHiLog.e(TAG, false, "updateApQualityRcd update failed.", new Object[0]);
                return false;
            }
            HwHiLog.d(TAG, false, "updateApQualityRcd update succ, rowChg=%{public}d", new Object[]{Integer.valueOf(rowChg)});
            return true;
        } catch (SQLException e) {
            HwHiLog.e(TAG, false, "updateApQualityRcd error", new Object[0]);
            return false;
        }
    }

    private boolean insertApQualityRcd(WifiProApQualityRcd dbr) {
        try {
            this.mDatabase.execSQL("INSERT INTO WifiProApQualityTable VALUES(null,  ?, ?, ?, ?, ?,   ?, ?)", new Object[]{dbr.getApBssid(), dbr.getRttProduct(), dbr.getRttPacketVolume(), dbr.getHistoryAvgRtt(), dbr.getOtaLostRateValue(), dbr.getOtaPktVolume(), dbr.getOtaBadPktProduct()});
            HwHiLog.i(TAG, false, "insertApQualityRcd add a record succ.", new Object[0]);
            return true;
        } catch (SQLException e) {
            HwHiLog.e(TAG, false, "insertApQualityRcd error", new Object[0]);
            return false;
        }
    }

    public boolean addOrUpdateApQualityRcd(WifiProApQualityRcd dbr) {
        HwHiLog.d(TAG, false, "addOrUpdateApQualityRcd enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (dbr != null) {
                    if (dbr.getApBssid() == null) {
                        HwHiLog.e(TAG, false, "addOrUpdateApQualityRcd null error.", new Object[0]);
                        return false;
                    }
                    try {
                        if (checkHistoryRecordExist(WifiProHistoryDBHelper.WP_QUALITY_TB_NAME, dbr.getApBssid())) {
                            return updateApQualityRcd(dbr);
                        }
                        return insertApQualityRcd(dbr);
                    } catch (CheckHistoryRecordException e) {
                        HwHiLog.e(TAG, false, "Exceptions happened in addOrUpdateApQualityRcd()", new Object[0]);
                        return false;
                    }
                }
            }
            HwHiLog.e(TAG, false, "addOrUpdateApQualityRcd error.", new Object[0]);
            return false;
        }
    }

    public boolean queryApQualityRcd(String apBssid, WifiProApQualityRcd dbr) {
        HwHiLog.d(TAG, false, "queryApQualityRcd enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            if (!isWifiProHistoryDatabaseOpen() || apBssid == null || dbr == null) {
                return false;
            }
            Cursor cursor = null;
            int recCnt = 0;
            try {
                Cursor cursor2 = this.mDatabase.rawQuery("SELECT * FROM WifiProApQualityTable where apBSSID like ?", new String[]{apBssid});
                while (true) {
                    if (!cursor2.moveToNext()) {
                        break;
                    }
                    recCnt++;
                    if (recCnt > 1) {
                        break;
                    } else if (recCnt == 1) {
                        dbr.setApBssid(apBssid);
                        dbr.setRttProduct(cursor2.getBlob(cursor2.getColumnIndex("RTT_Product")));
                        dbr.setRttPacketVolume(cursor2.getBlob(cursor2.getColumnIndex("RTT_PacketVolume")));
                        dbr.setHistoryAvgRtt(cursor2.getBlob(cursor2.getColumnIndex("HistoryAvgRtt")));
                        dbr.setOtaLostRateValue(cursor2.getBlob(cursor2.getColumnIndex("OTA_LostRateValue")));
                        dbr.setOtaPktVolume(cursor2.getBlob(cursor2.getColumnIndex("OTA_PktVolume")));
                        dbr.setOtaBadPktProduct(cursor2.getBlob(cursor2.getColumnIndex("OTA_BadPktProduct")));
                        HwHiLog.i(TAG, false, "read record succ", new Object[0]);
                    }
                }
                cursor2.close();
                if (recCnt > 1) {
                    HwHiLog.e(TAG, false, "more than one record error. use first record.", new Object[0]);
                } else if (recCnt == 0) {
                    HwHiLog.i(TAG, false, "queryApQualityRcd not record.", new Object[0]);
                    return false;
                }
                return true;
            } catch (SQLException e) {
                HwHiLog.e(TAG, false, "queryApQualityRcd error", new Object[0]);
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

    private boolean updateRelateApRcd(WifiProRelateApRcd dbr) {
        ContentValues values = new ContentValues();
        values.put("RelateType", Integer.valueOf(dbr.mRelateType));
        values.put("MaxCurrentRSSI", Integer.valueOf(dbr.mMaxCurrentRSSI));
        values.put("MaxRelatedRSSI", Integer.valueOf(dbr.mMaxRelatedRSSI));
        values.put("MinCurrentRSSI", Integer.valueOf(dbr.mMinCurrentRSSI));
        values.put("MinRelatedRSSI", Integer.valueOf(dbr.mMinRelatedRSSI));
        try {
            int rowChg = this.mDatabase.update(WifiProHistoryDBHelper.WP_RELATE_AP_TB_NAME, values, "(apBSSID like ?) and (RelatedBSSID like ?)", new String[]{dbr.mApBSSID, dbr.mRelatedBSSID});
            if (rowChg == 0) {
                HwHiLog.e(TAG, false, "updateRelateApRcd update failed.", new Object[0]);
                return false;
            }
            HwHiLog.d(TAG, false, "updateRelateApRcd update succ, rowChg=%{public}d", new Object[]{Integer.valueOf(rowChg)});
            return true;
        } catch (SQLException e) {
            HwHiLog.e(TAG, false, "updateRelateApRcd error", new Object[0]);
            return false;
        }
    }

    private boolean insertRelateApRcd(WifiProRelateApRcd dbr) {
        try {
            this.mDatabase.execSQL("INSERT INTO WifiProRelateApTable VALUES(null,  ?, ?, ?, ?, ?, ?, ?)", new Object[]{dbr.mApBSSID, dbr.mRelatedBSSID, Integer.valueOf(dbr.mRelateType), Integer.valueOf(dbr.mMaxCurrentRSSI), Integer.valueOf(dbr.mMaxRelatedRSSI), Integer.valueOf(dbr.mMinCurrentRSSI), Integer.valueOf(dbr.mMinRelatedRSSI)});
            HwHiLog.i(TAG, false, "insertRelateApRcd add a record succ.", new Object[0]);
            return true;
        } catch (SQLException e) {
            HwHiLog.e(TAG, false, "insertRelateApRcd error", new Object[0]);
            return false;
        }
    }

    private boolean checkRelateApRcdExist(String apBssid, String relatedBSSID) {
        boolean ret = false;
        Cursor cursor = null;
        try {
            Cursor cursor2 = this.mDatabase.rawQuery("SELECT * FROM WifiProRelateApTable where (apBSSID like ?) and (RelatedBSSID like ?)", new String[]{apBssid, relatedBSSID});
            int rcdCount = cursor2.getCount();
            if (rcdCount > 0) {
                ret = true;
            }
            HwHiLog.d(TAG, false, "checkRelateApRcdExist get record: %{public}d", new Object[]{Integer.valueOf(rcdCount)});
            cursor2.close();
            return ret;
        } catch (SQLException e) {
            HwHiLog.e(TAG, false, "checkRelateApRcdExist error", new Object[0]);
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

    public boolean addOrUpdateRelateApRcd(WifiProRelateApRcd dbr) {
        HwHiLog.d(TAG, false, "addOrUpdateRelateApRcd enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (dbr != null) {
                    if (dbr.mApBSSID != null) {
                        if (dbr.mRelatedBSSID != null) {
                            if (checkRelateApRcdExist(dbr.mApBSSID, dbr.mRelatedBSSID)) {
                                return updateRelateApRcd(dbr);
                            }
                            return insertRelateApRcd(dbr);
                        }
                    }
                    HwHiLog.e(TAG, false, "addOrUpdateRelateApRcd null error.", new Object[0]);
                    return false;
                }
            }
            HwHiLog.e(TAG, false, "addOrUpdateRelateApRcd error.", new Object[0]);
            return false;
        }
    }

    public boolean queryRelateApRcd(String apBssid, List<WifiProRelateApRcd> relateApList) {
        HwHiLog.d(TAG, false, "queryRelateApRcd enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (apBssid != null) {
                        if (relateApList != null) {
                            relateApList.clear();
                            Cursor cursor = null;
                            int recCnt = 0;
                            try {
                                Cursor cursor2 = this.mDatabase.rawQuery("SELECT * FROM WifiProRelateApTable where apBSSID like ?", new String[]{apBssid});
                                while (true) {
                                    if (!cursor2.moveToNext()) {
                                        break;
                                    }
                                    recCnt++;
                                    if (recCnt > 20) {
                                        break;
                                    }
                                    WifiProRelateApRcd dbr = new WifiProRelateApRcd(apBssid);
                                    dbr.mRelatedBSSID = cursor2.getString(cursor2.getColumnIndex("RelatedBSSID"));
                                    dbr.mRelateType = cursor2.getShort(cursor2.getColumnIndex("RelateType"));
                                    dbr.mMaxCurrentRSSI = cursor2.getInt(cursor2.getColumnIndex("MaxCurrentRSSI"));
                                    dbr.mMaxRelatedRSSI = cursor2.getInt(cursor2.getColumnIndex("MaxRelatedRSSI"));
                                    dbr.mMinCurrentRSSI = cursor2.getInt(cursor2.getColumnIndex("MinCurrentRSSI"));
                                    dbr.mMinRelatedRSSI = cursor2.getInt(cursor2.getColumnIndex("MinRelatedRSSI"));
                                    relateApList.add(dbr);
                                }
                                cursor2.close();
                                if (recCnt != 0) {
                                    return true;
                                }
                                HwHiLog.i(TAG, false, "queryRelateApRcd not record.", new Object[0]);
                                return false;
                            } catch (SQLException e) {
                                HwHiLog.e(TAG, false, "queryRelateApRcd error", new Object[0]);
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
                    HwHiLog.e(TAG, false, "queryRelateApRcd null error.", new Object[0]);
                    return false;
                }
            }
            HwHiLog.e(TAG, false, "queryRelateApRcd database error.", new Object[0]);
            return false;
        }
    }

    private boolean updateDualBandApInfoRcd(WifiProDualBandApInfoRcd dbr) {
        ContentValues values = new ContentValues();
        values.put("apSSID", dbr.mApSSID);
        values.put("InetCapability", dbr.mInetCapability);
        values.put("ServingBand", dbr.mServingBand);
        values.put("ApAuthType", dbr.mApAuthType);
        values.put("ChannelFrequency", Integer.valueOf(dbr.mChannelFrequency));
        values.put("DisappearCount", Integer.valueOf(dbr.mDisappearCount));
        values.put("isInBlackList", Integer.valueOf(dbr.mInBlackList));
        values.put("UpdateTime", Long.valueOf(dbr.mUpdateTime));
        try {
            int rowChg = this.mDatabase.update(WifiProHistoryDBHelper.WP_DUAL_BAND_AP_INFO_TB_NAME, values, "apBSSID like ?", new String[]{dbr.mApBSSID});
            if (rowChg == 0) {
                HwHiLog.e(TAG, false, "updateDualBandApInfoRcd update failed.", new Object[0]);
                return false;
            }
            HwHiLog.d(TAG, false, "updateDualBandApInfoRcd update succ, rowChg=%{public}d", new Object[]{Integer.valueOf(rowChg)});
            return true;
        } catch (SQLException e) {
            HwHiLog.e(TAG, false, "updateDualBandApInfoRcd error", new Object[0]);
            return false;
        }
    }

    private boolean insertDualBandApInfoRcd(WifiProDualBandApInfoRcd dbr) {
        if (this.mNeedDelOldDualBandApInfo || getDualBandApInfoSize() >= 500) {
            if (!this.mNeedDelOldDualBandApInfo) {
                this.mNeedDelOldDualBandApInfo = true;
            }
            if (!deleteOldestDualBandApInfo()) {
                return false;
            }
        }
        try {
            this.mDatabase.execSQL("INSERT INTO WifiProDualBandApInfoRcdTable VALUES(null,  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{dbr.mApBSSID, dbr.mApSSID, dbr.mInetCapability, dbr.mServingBand, dbr.mApAuthType, Integer.valueOf(dbr.mChannelFrequency), Integer.valueOf(dbr.mDisappearCount), Integer.valueOf(dbr.mInBlackList), Long.valueOf(dbr.mUpdateTime)});
            HwHiLog.i(TAG, false, "insertDualBandApInfoRcd add a record succ.", new Object[0]);
            return true;
        } catch (SQLException e) {
            HwHiLog.e(TAG, false, "insertDualBandApInfoRcd error", new Object[0]);
            return false;
        }
    }

    public boolean addOrUpdateDualBandApInfoRcd(WifiProDualBandApInfoRcd dbr) {
        HwHiLog.d(TAG, false, "addOrUpdateDualBandApInfoRcd enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (dbr != null) {
                    if (dbr.mApBSSID == null) {
                        HwHiLog.e(TAG, false, "addOrUpdateDualBandApInfoRcd null error.", new Object[0]);
                        return false;
                    }
                    dbr.mUpdateTime = System.currentTimeMillis();
                    try {
                        if (checkHistoryRecordExist(WifiProHistoryDBHelper.WP_DUAL_BAND_AP_INFO_TB_NAME, dbr.mApBSSID)) {
                            return updateDualBandApInfoRcd(dbr);
                        }
                        return insertDualBandApInfoRcd(dbr);
                    } catch (CheckHistoryRecordException e) {
                        HwHiLog.e(TAG, false, "Exceptions happened in addOrUpdateDualBandApInfoRcd()", new Object[0]);
                        return false;
                    }
                }
            }
            HwHiLog.e(TAG, false, "addOrUpdateDualBandApInfoRcd error.", new Object[0]);
            return false;
        }
    }

    private boolean isWifiProHistoryDatabaseOpen() {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase != null && sQLiteDatabase.isOpen()) {
            return true;
        }
        HwHiLog.e(TAG, false, "isWifiProHistoryDatabaseOpen database error.", new Object[0]);
        return false;
    }

    public boolean queryDualBandApInfoRcd(String apBssid, WifiProDualBandApInfoRcd dbr) {
        HwHiLog.d(TAG, false, "queryDualBandApInfoRcd enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            if (!isWifiProHistoryDatabaseOpen() || apBssid == null || dbr == null) {
                return false;
            }
            Cursor cursor = null;
            int recCnt = 0;
            try {
                Cursor cursor2 = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable where apBSSID like ?", new String[]{apBssid});
                while (true) {
                    if (!cursor2.moveToNext()) {
                        break;
                    }
                    recCnt++;
                    if (recCnt > 1) {
                        break;
                    } else if (recCnt == 1) {
                        dbr.mApBSSID = apBssid;
                        dbr.mApSSID = cursor2.getString(cursor2.getColumnIndex("apSSID"));
                        dbr.mInetCapability = Short.valueOf(cursor2.getShort(cursor2.getColumnIndex("InetCapability")));
                        dbr.mServingBand = Short.valueOf(cursor2.getShort(cursor2.getColumnIndex("ServingBand")));
                        dbr.mApAuthType = Short.valueOf(cursor2.getShort(cursor2.getColumnIndex("ApAuthType")));
                        dbr.mChannelFrequency = cursor2.getInt(cursor2.getColumnIndex("ChannelFrequency"));
                        dbr.mDisappearCount = cursor2.getShort(cursor2.getColumnIndex("DisappearCount"));
                        dbr.mInBlackList = cursor2.getShort(cursor2.getColumnIndex("isInBlackList"));
                        dbr.mUpdateTime = cursor2.getLong(cursor2.getColumnIndex("UpdateTime"));
                        HwHiLog.i(TAG, false, "read record succ", new Object[0]);
                    }
                }
                cursor2.close();
                if (recCnt > 1) {
                    HwHiLog.e(TAG, false, "more than one record error. use first record.", new Object[0]);
                } else if (recCnt == 0) {
                    HwHiLog.i(TAG, false, "queryDualBandApInfoRcd not record.", new Object[0]);
                    return false;
                }
                return true;
            } catch (SQLException e) {
                HwHiLog.e(TAG, false, "queryDualBandApInfoRcd error", new Object[0]);
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

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00de, code lost:
        if (0 == 0) goto L_0x00e1;
     */
    public List<WifiProDualBandApInfoRcd> queryDualBandApInfoRcdBySsid(String ssid) {
        List<WifiProDualBandApInfoRcd> apInfoRcdList = new ArrayList<>();
        HwHiLog.d(TAG, false, "queryDualBandApInfoRcd enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (ssid == null) {
                        HwHiLog.e(TAG, false, "queryDualBandApInfoRcdBySsid null error.", new Object[0]);
                        return Collections.emptyList();
                    }
                    Cursor cursor = null;
                    try {
                        cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable where apSSID like ?", new String[]{ssid});
                        while (cursor.moveToNext()) {
                            WifiProDualBandApInfoRcd dbr = new WifiProDualBandApInfoRcd(null);
                            dbr.mApBSSID = cursor.getString(cursor.getColumnIndex("apBSSID"));
                            dbr.mApSSID = ssid;
                            dbr.mInetCapability = Short.valueOf(cursor.getShort(cursor.getColumnIndex("InetCapability")));
                            dbr.mServingBand = Short.valueOf(cursor.getShort(cursor.getColumnIndex("ServingBand")));
                            dbr.mApAuthType = Short.valueOf(cursor.getShort(cursor.getColumnIndex("ApAuthType")));
                            dbr.mChannelFrequency = cursor.getInt(cursor.getColumnIndex("ChannelFrequency"));
                            dbr.mDisappearCount = cursor.getShort(cursor.getColumnIndex("DisappearCount"));
                            dbr.mInBlackList = cursor.getShort(cursor.getColumnIndex("isInBlackList"));
                            dbr.mUpdateTime = cursor.getLong(cursor.getColumnIndex("UpdateTime"));
                            HwHiLog.i(TAG, false, "read record succ", new Object[0]);
                            apInfoRcdList.add(dbr);
                        }
                    } catch (SQLException e) {
                        HwHiLog.e(TAG, false, "queryDualBandApInfoRcdBySsid error", new Object[0]);
                        Collections.emptyList();
                    } catch (Throwable th) {
                        if (0 != 0) {
                            cursor.close();
                        }
                        throw th;
                    }
                    cursor.close();
                    if (apInfoRcdList.size() == 0) {
                        HwHiLog.i(TAG, false, "queryDualBandApInfoRcdBySsid not record.", new Object[0]);
                    }
                    return apInfoRcdList;
                }
            }
            HwHiLog.e(TAG, false, "queryDualBandApInfoRcdBySsid database error.", new Object[0]);
            return Collections.emptyList();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00c3, code lost:
        if (0 == 0) goto L_0x00c6;
     */
    public List<WifiProDualBandApInfoRcd> getAllDualBandApInfo() {
        HwHiLog.d(TAG, false, "getAllDualBandApInfo enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            ArrayList<WifiProDualBandApInfoRcd> apInfoList = new ArrayList<>();
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                HwHiLog.e(TAG, false, "queryDualBandApInfoRcd database error.", new Object[0]);
                return apInfoList;
            }
            Cursor cursor = null;
            try {
                cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable", null);
                while (cursor.moveToNext()) {
                    WifiProDualBandApInfoRcd dbr = new WifiProDualBandApInfoRcd(cursor.getString(cursor.getColumnIndex("apBSSID")));
                    dbr.mApSSID = cursor.getString(cursor.getColumnIndex("apSSID"));
                    dbr.mInetCapability = Short.valueOf(cursor.getShort(cursor.getColumnIndex("InetCapability")));
                    dbr.mServingBand = Short.valueOf(cursor.getShort(cursor.getColumnIndex("ServingBand")));
                    dbr.mApAuthType = Short.valueOf(cursor.getShort(cursor.getColumnIndex("ApAuthType")));
                    dbr.mChannelFrequency = cursor.getInt(cursor.getColumnIndex("ChannelFrequency"));
                    dbr.mDisappearCount = cursor.getShort(cursor.getColumnIndex("DisappearCount"));
                    dbr.mInBlackList = cursor.getShort(cursor.getColumnIndex("isInBlackList"));
                    dbr.mUpdateTime = cursor.getLong(cursor.getColumnIndex("UpdateTime"));
                    apInfoList.add(dbr);
                }
            } catch (SQLException e) {
                HwHiLog.e(TAG, false, "queryDualBandApInfoRcd error", new Object[0]);
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
            cursor.close();
            return apInfoList;
        }
    }

    private boolean updateApRSSIThreshold(String apBSSID, String rssiThreshold) {
        ContentValues values = new ContentValues();
        values.put("RSSIThreshold", rssiThreshold);
        try {
            int rowChg = this.mDatabase.update(WifiProHistoryDBHelper.WP_DUAL_BAND_AP_INFO_TB_NAME, values, "apBSSID like ?", new String[]{apBSSID});
            if (rowChg == 0) {
                HwHiLog.e(TAG, false, "updateApRSSIThreshold update failed.", new Object[0]);
                return false;
            }
            HwHiLog.d(TAG, false, "updateApRSSIThreshold update succ, rowChg=%{public}d", new Object[]{Integer.valueOf(rowChg)});
            return true;
        } catch (SQLException e) {
            HwHiLog.e(TAG, false, "updateApRSSIThreshold error", new Object[0]);
            return false;
        }
    }

    public boolean addOrUpdateApRssiThreshold(String apBssid, String rssiThreshold) {
        HwHiLog.d(TAG, false, "addOrUpdateApRssiThreshold enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || apBssid == null || rssiThreshold == null || rssiThreshold.isEmpty()) {
                HwHiLog.e(TAG, false, "addOrUpdateApRssiThreshold error.", new Object[0]);
                return false;
            }
            try {
                if (!checkHistoryRecordExist(WifiProHistoryDBHelper.WP_DUAL_BAND_AP_INFO_TB_NAME, apBssid)) {
                    insertDualBandApInfoRcd(new WifiProDualBandApInfoRcd(apBssid));
                }
                return updateApRSSIThreshold(apBssid, rssiThreshold);
            } catch (CheckHistoryRecordException e) {
                HwHiLog.e(TAG, false, "Exceptions happened in addOrUpdateApRssiThreshold", new Object[0]);
                return false;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0062, code lost:
        if (0 == 0) goto L_0x0065;
     */
    public String queryApRssiThreshold(String apBssid) {
        String result = null;
        HwHiLog.d(TAG, false, "queryApRssiThreshold enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || apBssid == null) {
                HwHiLog.e(TAG, false, "queryApRssiThreshold database error.", new Object[0]);
                return null;
            }
            Cursor cursor = null;
            int recCnt = 0;
            try {
                cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable where apBSSID like ?", new String[]{apBssid});
                while (true) {
                    if (!cursor.moveToNext()) {
                        break;
                    }
                    recCnt++;
                    if (recCnt > 1) {
                        break;
                    } else if (recCnt == 1) {
                        result = cursor.getString(cursor.getColumnIndex("RSSIThreshold"));
                        HwHiLog.i(TAG, false, "read record succ, RSSIThreshold = %{public}s", new Object[]{result});
                    }
                }
            } catch (SQLException e) {
                HwHiLog.e(TAG, false, "queryApRssiThreshold error", new Object[0]);
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
            cursor.close();
            return result;
        }
    }

    public int getDualBandApInfoSize() {
        String str;
        String str2;
        Object[] objArr;
        HwHiLog.d(TAG, false, "getDualBandApInfoSize enter.", new Object[0]);
        synchronized (this.mBqeLock) {
            int result = -1;
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                HwHiLog.e(TAG, false, "getDualBandApInfoSize database error.", new Object[0]);
                return -1;
            }
            Cursor cursor = null;
            try {
                cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable", null);
                result = cursor.getCount();
                cursor.close();
                str = TAG;
                str2 = "getDualBandApInfoSize: %{public}d";
                objArr = new Object[]{Integer.valueOf(result)};
            } catch (SQLException e) {
                HwHiLog.e(TAG, false, "getDualBandApInfoSize error", new Object[0]);
                if (cursor != null) {
                    cursor.close();
                }
                str = TAG;
                str2 = "getDualBandApInfoSize: %{public}d";
                objArr = new Object[]{-1};
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                HwHiLog.d(TAG, false, "getDualBandApInfoSize: %{public}d", new Object[]{-1});
                throw th;
            }
            HwHiLog.d(str, false, str2, objArr);
            return result;
        }
    }

    private boolean deleteOldestDualBandApInfo() {
        List<WifiProDualBandApInfoRcd> allApInfos = getAllDualBandApInfo();
        if (allApInfos.size() <= 0) {
            return false;
        }
        WifiProDualBandApInfoRcd oldestApInfo = allApInfos.get(0);
        for (WifiProDualBandApInfoRcd apInfo : allApInfos) {
            if (apInfo.mUpdateTime < oldestApInfo.mUpdateTime) {
                oldestApInfo = apInfo;
            }
        }
        return deleteAllDualBandAPRcd(oldestApInfo.mApBSSID);
    }

    /* access modifiers changed from: private */
    public static class CheckHistoryRecordException extends Exception {
        CheckHistoryRecordException() {
        }
    }
}
