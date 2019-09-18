package com.android.server.wifi.wifipro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class WifiProHistoryDBManager {
    private static long AGING_MS_OF_EACH_DAY = 1800000;
    private static final int DBG_LOG_LEVEL = 1;
    private static final int ERROR_LOG_LEVEL = 3;
    private static final int INFO_LOG_LEVEL = 2;
    private static final short MAX_AP_INFO_RECORD_NUM = 1;
    private static long MS_OF_ONE_DAY = 86400000;
    private static final String TAG = "WifiProHistoryDBManager";
    private static long TOO_OLD_VALID_DAY = 10;
    private static WifiProHistoryDBManager mBQEDataBaseManager;
    private static int printLogLevel = 1;
    private int mApRecordCount = 0;
    private Object mBqeLock = new Object();
    private SQLiteDatabase mDatabase;
    private WifiProHistoryDBHelper mHelper;
    private int mHomeApRecordCount = 0;
    private boolean mNeedDelOldDualBandApInfo;

    public WifiProHistoryDBManager(Context context) {
        Log.w(TAG, "WifiProHistoryDBManager()");
        this.mHelper = new WifiProHistoryDBHelper(context);
        try {
            this.mDatabase = this.mHelper.getWritableDatabase();
        } catch (SQLiteCantOpenDatabaseException e) {
            loge("WifiProHistoryDBManager(), can't open database!");
        }
    }

    public static WifiProHistoryDBManager getInstance(Context context) {
        if (mBQEDataBaseManager == null) {
            mBQEDataBaseManager = new WifiProHistoryDBManager(context);
        }
        return mBQEDataBaseManager;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001f, code lost:
        return;
     */
    public void closeDB() {
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    Log.w(TAG, "closeDB()");
                    this.mDatabase.close();
                }
            }
        }
    }

    private boolean deleteHistoryRecord(String dbTableName, String apBssid) {
        logi("deleteHistoryRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (apBssid == null) {
                        loge("deleteHistoryRecord null error.");
                        return false;
                    }
                    try {
                        this.mDatabase.delete(dbTableName, "apBSSID like ?", new String[]{apBssid});
                        return true;
                    } catch (SQLException e) {
                        loge("deleteHistoryRecord error:" + e);
                        return false;
                    }
                }
            }
            loge("deleteHistoryRecord database error.");
            return false;
        }
    }

    public boolean deleteApInfoRecord(String apBssid) {
        return deleteHistoryRecord(WifiProHistoryDBHelper.WP_AP_INFO_TB_NAME, apBssid);
    }

    private boolean checkHistoryRecordExist(String dbTableName, String apBssid) {
        boolean ret = false;
        Cursor c = null;
        try {
            SQLiteDatabase sQLiteDatabase = this.mDatabase;
            Cursor c2 = sQLiteDatabase.rawQuery("SELECT * FROM " + dbTableName + " where apBSSID like ?", new String[]{apBssid});
            int rcdCount = c2.getCount();
            if (rcdCount > 0) {
                ret = true;
            }
            logd("checkHistoryRecordExist read from:" + dbTableName + ", get record: " + rcdCount);
            if (c2 != null) {
                c2.close();
            }
            return ret;
        } catch (SQLException e) {
            loge("checkHistoryRecordExist error:" + e);
            if (c != null) {
                c.close();
            }
            return false;
        } catch (Throwable th) {
            if (c != null) {
                c.close();
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
                loge("updateApInfoRecord update failed.");
                return false;
            }
            logd("updateApInfoRecord update succ, rowChg=" + rowChg);
            return true;
        } catch (SQLException e) {
            loge("updateApInfoRecord error:" + e);
            return false;
        }
    }

    private boolean insertApInfoRecord(WifiProApInfoRecord dbr) {
        try {
            this.mDatabase.execSQL("INSERT INTO WifiProApInfoRecodTable VALUES(null,  ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,?)", new Object[]{dbr.apBSSID, dbr.apSSID, Integer.valueOf(dbr.apSecurityType), Long.valueOf(dbr.firstConnectTime), Long.valueOf(dbr.lastConnectTime), Integer.valueOf(dbr.lanDataSize), Integer.valueOf(dbr.highSpdFreq), Integer.valueOf(dbr.totalUseTime), Integer.valueOf(dbr.totalUseTimeAtNight), Integer.valueOf(dbr.totalUseTimeAtWeekend), Long.valueOf(dbr.judgeHomeAPTime)});
            logi("insertApInfoRecord add a record succ.");
            return true;
        } catch (SQLException e) {
            loge("insertApInfoRecord error:" + e);
            return false;
        }
    }

    public boolean addOrUpdateApInfoRecord(WifiProApInfoRecord dbr) {
        logd("addOrUpdateApInfoRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (dbr != null) {
                    if (dbr.apBSSID == null) {
                        loge("addOrUpdateApInfoRecord null error.");
                        return false;
                    } else if (checkHistoryRecordExist(WifiProHistoryDBHelper.WP_AP_INFO_TB_NAME, dbr.apBSSID)) {
                        boolean updateApInfoRecord = updateApInfoRecord(dbr);
                        return updateApInfoRecord;
                    } else {
                        boolean insertApInfoRecord = insertApInfoRecord(dbr);
                        return insertApInfoRecord;
                    }
                }
            }
            loge("addOrUpdateApInfoRecord error.");
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0100, code lost:
        return true;
     */
    public boolean queryApInfoRecord(String apBssid, WifiProApInfoRecord dbr) {
        int recCnt = 0;
        logd("queryApInfoRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("queryApInfoRecord database error.");
                return false;
            } else if (apBssid == null || dbr == null) {
                loge("queryApInfoRecord null error.");
                return false;
            } else {
                Cursor c = null;
                try {
                    Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM WifiProApInfoRecodTable where apBSSID like ?", new String[]{apBssid});
                    while (true) {
                        if (!c2.moveToNext()) {
                            break;
                        }
                        recCnt++;
                        if (recCnt > 1) {
                            break;
                        }
                        logd("read record id = " + c2.getInt(c2.getColumnIndex("_id")));
                        if (recCnt == 1) {
                            dbr.apBSSID = apBssid;
                            dbr.apSSID = c2.getString(c2.getColumnIndex("apSSID"));
                            dbr.apSecurityType = c2.getInt(c2.getColumnIndex("apSecurityType"));
                            dbr.firstConnectTime = c2.getLong(c2.getColumnIndex("firstConnectTime"));
                            dbr.lastConnectTime = c2.getLong(c2.getColumnIndex("lastConnectTime"));
                            dbr.lanDataSize = c2.getInt(c2.getColumnIndex("lanDataSize"));
                            dbr.highSpdFreq = c2.getInt(c2.getColumnIndex("highSpdFreq"));
                            dbr.totalUseTime = c2.getInt(c2.getColumnIndex("totalUseTime"));
                            dbr.totalUseTimeAtNight = c2.getInt(c2.getColumnIndex("totalUseTimeAtNight"));
                            dbr.totalUseTimeAtWeekend = c2.getInt(c2.getColumnIndex("totalUseTimeAtWeekend"));
                            dbr.judgeHomeAPTime = c2.getLong(c2.getColumnIndex("judgeHomeAPTime"));
                            logi("read record succ, LastConnectTime:" + dbr.lastConnectTime);
                        }
                    }
                    if (c2 != null) {
                        c2.close();
                    }
                    if (recCnt > 1) {
                        loge("more than one record error. use first record.");
                    } else if (recCnt == 0) {
                        logi("queryApInfoRecord not record.");
                    }
                } catch (SQLException e) {
                    try {
                        loge("queryApInfoRecord error:" + e);
                        if (c != null) {
                            c.close();
                        }
                        return false;
                    } catch (Throwable th) {
                        if (c != null) {
                            c.close();
                        }
                        throw th;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0060, code lost:
        return r0;
     */
    public int querySameSSIDApCount(String apBSSID, String apSsid, int secType) {
        int recCnt = 0;
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("querySameSSIDApCount database error.");
                return 0;
            } else if (apBSSID == null || apSsid == null) {
                loge("querySameSSIDApCount null error.");
                return 0;
            } else {
                Cursor c = null;
                try {
                    c = this.mDatabase.rawQuery("SELECT * FROM WifiProApInfoRecodTable where (apSSID like ?) and (apSecurityType = ?) and (apBSSID != ?)", new String[]{apSsid, String.valueOf(secType), apBSSID});
                    recCnt = c.getCount();
                    logi("querySameSSIDApCount read same (SSID:" + apSsid + ", secType:" + secType + ") and different BSSID record count=" + recCnt);
                    if (c != null) {
                        c.close();
                    }
                } catch (SQLException e) {
                    try {
                        loge("querySameSSIDApCount error:" + e);
                        if (c != null) {
                            c.close();
                        }
                        return recCnt;
                    } catch (Throwable th) {
                        if (c != null) {
                            c.close();
                        }
                        throw th;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:63:0x016a, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0195, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x01af, code lost:
        r0 = th;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:59:0x0165, B:69:0x0178] */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x018f A[SYNTHETIC, Splitter:B:72:0x018f] */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0198 A[Catch:{ all -> 0x0195, all -> 0x01af }] */
    public boolean removeTooOldApInfoRecord() {
        long totalConnectTime;
        Date currDate;
        int recCnt;
        String bssid;
        int recCnt2 = false;
        Vector vector = new Vector();
        Date currDate2 = new Date();
        long currDateMsTime = currDate2.getTime();
        logd("removeTooOldApInfoRecord enter.");
        synchronized (this.mBqeLock) {
            try {
                if (this.mDatabase == null) {
                    Date date = currDate2;
                } else if (!this.mDatabase.isOpen()) {
                    Date date2 = currDate2;
                } else {
                    vector.clear();
                    Cursor c = null;
                    try {
                        c = this.mDatabase.rawQuery("SELECT * FROM WifiProApInfoRecodTable", null);
                        logd("all record count=" + c.getCount());
                        while (c.moveToNext()) {
                            long lastConnDateMsTime = c.getLong(c.getColumnIndex("lastConnectTime"));
                            long totalConnectTime2 = (long) c.getInt(c.getColumnIndex("totalUseTime"));
                            try {
                                long pastDays = (currDateMsTime - lastConnDateMsTime) / MS_OF_ONE_DAY;
                                if (pastDays <= TOO_OLD_VALID_DAY || totalConnectTime2 - (AGING_MS_OF_EACH_DAY * pastDays) >= 0) {
                                    recCnt = recCnt2;
                                    currDate = currDate2;
                                    totalConnectTime = totalConnectTime2;
                                } else {
                                    logi("check result: need delete.");
                                    int id = c.getInt(c.getColumnIndex("_id"));
                                    String ssid = c.getString(c.getColumnIndex("apSSID"));
                                    recCnt = recCnt2;
                                    try {
                                        bssid = c.getString(c.getColumnIndex("apBSSID"));
                                        currDate = currDate2;
                                    } catch (SQLException e) {
                                        e = e;
                                        Date date3 = currDate2;
                                        long j = totalConnectTime2;
                                        loge("removeTooOldApInfoRecord error:" + e);
                                        if (c != null) {
                                        }
                                        return false;
                                    } catch (Throwable th) {
                                        e = th;
                                        Date date4 = currDate2;
                                        long j2 = totalConnectTime2;
                                        if (c != null) {
                                        }
                                        throw e;
                                    }
                                    try {
                                        StringBuilder sb = new StringBuilder();
                                        totalConnectTime = totalConnectTime2;
                                        try {
                                            sb.append("check record: ssid:");
                                            sb.append(ssid);
                                            sb.append(", id:");
                                            sb.append(id);
                                            sb.append(", pass time:");
                                            sb.append(currDateMsTime - lastConnDateMsTime);
                                            logi(sb.toString());
                                            vector.add(bssid);
                                        } catch (SQLException e2) {
                                            e = e2;
                                            long j3 = totalConnectTime;
                                        } catch (Throwable th2) {
                                            e = th2;
                                            long j4 = totalConnectTime;
                                            if (c != null) {
                                            }
                                            throw e;
                                        }
                                    } catch (SQLException e3) {
                                        e = e3;
                                        long j5 = totalConnectTime2;
                                        loge("removeTooOldApInfoRecord error:" + e);
                                        if (c != null) {
                                        }
                                        return false;
                                    } catch (Throwable th3) {
                                        e = th3;
                                        long j6 = totalConnectTime2;
                                        if (c != null) {
                                        }
                                        throw e;
                                    }
                                }
                                recCnt2 = recCnt;
                                currDate2 = currDate;
                                long j7 = totalConnectTime;
                            } catch (SQLException e4) {
                                e = e4;
                                int i = recCnt2;
                                Date date5 = currDate2;
                                long j8 = totalConnectTime2;
                                loge("removeTooOldApInfoRecord error:" + e);
                                if (c != null) {
                                }
                                return false;
                            } catch (Throwable th4) {
                                e = th4;
                                int i2 = recCnt2;
                                Date date6 = currDate2;
                                long j9 = totalConnectTime2;
                                if (c != null) {
                                }
                                throw e;
                            }
                        }
                        Date date7 = currDate2;
                        try {
                            int delSize = vector.size();
                            logi("start delete " + delSize + " records.");
                            int i3 = 0;
                            while (true) {
                                if (i3 >= delSize) {
                                    break;
                                }
                                String delBSSID = (String) vector.get(i3);
                                if (delBSSID == null) {
                                    break;
                                }
                                int delSize2 = delSize;
                                this.mDatabase.delete(WifiProHistoryDBHelper.WP_AP_INFO_TB_NAME, "apBSSID like ?", new String[]{delBSSID});
                                i3++;
                                delSize = delSize2;
                            }
                            if (c != null) {
                                c.close();
                            }
                        } catch (SQLException e5) {
                            e = e5;
                            loge("removeTooOldApInfoRecord error:" + e);
                            if (c != null) {
                            }
                            return false;
                        }
                    } catch (SQLException e6) {
                        e = e6;
                        Date date8 = currDate2;
                        loge("removeTooOldApInfoRecord error:" + e);
                        if (c != null) {
                            c.close();
                        }
                        return false;
                    } catch (Throwable th5) {
                        e = th5;
                        Date date9 = currDate2;
                        if (c != null) {
                            c.close();
                        }
                        throw e;
                    }
                }
                loge("removeTooOldApInfoRecord database error.");
                return false;
            } catch (Throwable th6) {
                th = th6;
                Date date10 = currDate2;
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x008d, code lost:
        return true;
     */
    public boolean statisticApInfoRecord() {
        logd("statisticApInfoRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    this.mHomeApRecordCount = 0;
                    this.mApRecordCount = 0;
                    Cursor c = null;
                    try {
                        Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM WifiProApInfoRecodTable", null);
                        this.mApRecordCount = c2.getCount();
                        logi("all record count=" + this.mApRecordCount);
                        while (c2.moveToNext()) {
                            if (c2.getLong(c2.getColumnIndex("judgeHomeAPTime")) > 0) {
                                String ssid = c2.getString(c2.getColumnIndex("apSSID"));
                                this.mHomeApRecordCount++;
                                logi("check record: Home ap ssid:" + ssid + ", total:" + this.mHomeApRecordCount);
                            }
                        }
                        if (c2 != null) {
                            c2.close();
                        }
                    } catch (SQLException e) {
                        try {
                            loge("removeTooOldApInfoRecord error:" + e);
                            if (c != null) {
                                c.close();
                            }
                            return false;
                        } catch (Throwable th) {
                            if (c != null) {
                                c.close();
                            }
                            throw th;
                        }
                    }
                }
            }
            loge("statisticApInfoRecord database error.");
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
            logi("insertEnterpriseApRecord add a record succ.");
            return true;
        } catch (SQLException e) {
            loge("insertEnterpriseApRecord error:" + e);
            return false;
        }
    }

    public boolean addOrUpdateEnterpriseApRecord(String apSSID, int secType) {
        logd("addOrUpdateEnterpriseApRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (apSSID != null) {
                    if (!queryEnterpriseApRecord(apSSID, secType)) {
                        logi("add record here ssid:" + apSSID);
                        boolean insertEnterpriseApRecord = insertEnterpriseApRecord(apSSID, secType);
                        return insertEnterpriseApRecord;
                    }
                    logi("already exist the record ssid:" + apSSID);
                    return true;
                }
            }
            loge("addOrUpdateEnterpriseApRecord error.");
            return false;
        }
    }

    public boolean queryEnterpriseApRecord(String apSSID, int secType) {
        logd("queryEnterpriseApRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (apSSID == null) {
                        loge("queryEnterpriseApRecord null error.");
                        return false;
                    }
                    Cursor c = null;
                    try {
                        Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM WifiProEnterpriseAPTable where (apSSID like ?) and (apSecurityType = ?)", new String[]{apSSID, String.valueOf(secType)});
                        int recCnt = c2.getCount();
                        if (c2 != null) {
                            c2.close();
                        }
                        if (recCnt > 0) {
                            logi("SSID:" + apSSID + ", security: " + secType + " is in Enterprise Ap table. count:" + recCnt);
                            return true;
                        }
                        logi("SSID:" + apSSID + ", security: " + secType + " is not in Enterprise Ap table. count:" + recCnt);
                        return false;
                    } catch (SQLException e) {
                        try {
                            loge("queryEnterpriseApRecord error:" + e);
                            if (c != null) {
                                c.close();
                            }
                            return false;
                        } catch (Throwable th) {
                            if (c != null) {
                                c.close();
                            }
                            throw th;
                        }
                    }
                }
            }
            loge("queryEnterpriseApRecord database error.");
            return false;
        }
    }

    public boolean deleteEnterpriseApRecord(String tableName, String ssid, int secType) {
        if (tableName == null || ssid == null) {
            loge("deleteHistoryRecord null error.");
            return false;
        }
        logi("delete record of same (SSID:" + ssid + ", secType:" + secType + ") from " + tableName);
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("deleteHistoryRecord database error.");
                return false;
            }
            try {
                int delCount = this.mDatabase.delete(tableName, "(apSSID like ?) and (apSecurityType = ?)", new String[]{ssid, String.valueOf(secType)});
                logd("delete record count=" + delCount);
                return true;
            } catch (SQLException e) {
                loge("deleteHistoryRecord error:" + e);
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
                loge("deleteRelateApRcd database error.");
                return false;
            } else if (apBssid == null || relatedBSSID == null) {
                loge("deleteRelateApRcd null error.");
                return false;
            } else {
                try {
                    this.mDatabase.delete(WifiProHistoryDBHelper.WP_RELATE_AP_TB_NAME, "(apBSSID like ?) and (RelatedBSSID like ?)", new String[]{apBssid, relatedBSSID});
                    return true;
                } catch (SQLException e) {
                    loge("deleteRelateApRcd error:" + e);
                    return false;
                }
            }
        }
    }

    public boolean deleteRelate5GAPRcd(String relatedBSSID) {
        logi("deleteRelateApRcd enter");
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (relatedBSSID == null) {
                        loge("deleteRelateApRcd null error.");
                        return false;
                    }
                    try {
                        this.mDatabase.delete(WifiProHistoryDBHelper.WP_RELATE_AP_TB_NAME, "(RelatedBSSID like ?)", new String[]{relatedBSSID});
                        return true;
                    } catch (SQLException e) {
                        loge("deleteRelateApRcd error:" + e);
                        return false;
                    }
                }
            }
            loge("deleteRelateApRcd database error.");
            return false;
        }
    }

    private boolean deleteAllDualBandAPRcd(String apBssid) {
        return deleteDualBandApInfoRcd(apBssid) && deleteApQualityRcd(apBssid) && deleteRelateApRcd(apBssid) && deleteRelate5GAPRcd(apBssid);
    }

    private boolean updateApQualityRcd(WifiProApQualityRcd dbr) {
        ContentValues values = new ContentValues();
        values.put("RTT_Product", dbr.mRTT_Product);
        values.put("RTT_PacketVolume", dbr.mRTT_PacketVolume);
        values.put("HistoryAvgRtt", dbr.mHistoryAvgRtt);
        values.put("OTA_LostRateValue", dbr.mOTA_LostRateValue);
        values.put("OTA_PktVolume", dbr.mOTA_PktVolume);
        values.put("OTA_BadPktProduct", dbr.mOTA_BadPktProduct);
        try {
            int rowChg = this.mDatabase.update(WifiProHistoryDBHelper.WP_QUALITY_TB_NAME, values, "apBSSID like ?", new String[]{dbr.apBSSID});
            if (rowChg == 0) {
                loge("updateApQualityRcd update failed.");
                return false;
            }
            logd("updateApQualityRcd update succ, rowChg=" + rowChg);
            return true;
        } catch (SQLException e) {
            loge("updateApQualityRcd error:" + e);
            return false;
        }
    }

    private boolean insertApQualityRcd(WifiProApQualityRcd dbr) {
        try {
            this.mDatabase.execSQL("INSERT INTO WifiProApQualityTable VALUES(null,  ?, ?, ?, ?, ?,   ?, ?)", new Object[]{dbr.apBSSID, dbr.mRTT_Product, dbr.mRTT_PacketVolume, dbr.mHistoryAvgRtt, dbr.mOTA_LostRateValue, dbr.mOTA_PktVolume, dbr.mOTA_BadPktProduct});
            logi("insertApQualityRcd add a record succ.");
            return true;
        } catch (SQLException e) {
            loge("insertApQualityRcd error:" + e);
            return false;
        }
    }

    public boolean addOrUpdateApQualityRcd(WifiProApQualityRcd dbr) {
        logd("addOrUpdateApQualityRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (dbr != null) {
                    if (dbr.apBSSID == null) {
                        loge("addOrUpdateApQualityRcd null error.");
                        return false;
                    } else if (checkHistoryRecordExist(WifiProHistoryDBHelper.WP_QUALITY_TB_NAME, dbr.apBSSID)) {
                        boolean updateApQualityRcd = updateApQualityRcd(dbr);
                        return updateApQualityRcd;
                    } else {
                        boolean insertApQualityRcd = insertApQualityRcd(dbr);
                        return insertApQualityRcd;
                    }
                }
            }
            loge("addOrUpdateApQualityRcd error.");
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00a1, code lost:
        return true;
     */
    public boolean queryApQualityRcd(String apBssid, WifiProApQualityRcd dbr) {
        int recCnt = 0;
        logd("queryApQualityRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("queryApQualityRcd database error.");
                return false;
            } else if (apBssid == null || dbr == null) {
                loge("queryApQualityRcd null error.");
                return false;
            } else {
                Cursor c = null;
                try {
                    Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM WifiProApQualityTable where apBSSID like ?", new String[]{apBssid});
                    while (true) {
                        if (!c2.moveToNext()) {
                            break;
                        }
                        recCnt++;
                        if (recCnt > 1) {
                            break;
                        } else if (recCnt == 1) {
                            dbr.apBSSID = apBssid;
                            dbr.mRTT_Product = c2.getBlob(c2.getColumnIndex("RTT_Product"));
                            dbr.mRTT_PacketVolume = c2.getBlob(c2.getColumnIndex("RTT_PacketVolume"));
                            dbr.mHistoryAvgRtt = c2.getBlob(c2.getColumnIndex("HistoryAvgRtt"));
                            dbr.mOTA_LostRateValue = c2.getBlob(c2.getColumnIndex("OTA_LostRateValue"));
                            dbr.mOTA_PktVolume = c2.getBlob(c2.getColumnIndex("OTA_PktVolume"));
                            dbr.mOTA_BadPktProduct = c2.getBlob(c2.getColumnIndex("OTA_BadPktProduct"));
                            logi("read record succ");
                        }
                    }
                    if (c2 != null) {
                        c2.close();
                    }
                    if (recCnt > 1) {
                        loge("more than one record error. use first record.");
                    } else if (recCnt == 0) {
                        logi("queryApQualityRcd not record.");
                        return false;
                    }
                } catch (SQLException e) {
                    try {
                        loge("queryApQualityRcd error:" + e);
                        if (c != null) {
                            c.close();
                        }
                        return false;
                    } catch (Throwable th) {
                        if (c != null) {
                            c.close();
                        }
                        throw th;
                    }
                }
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
            int rowChg = this.mDatabase.update(WifiProHistoryDBHelper.WP_RELATE_AP_TB_NAME, values, "(apBSSID like ?) and (RelatedBSSID like ?)", new String[]{dbr.apBSSID, dbr.mRelatedBSSID});
            if (rowChg == 0) {
                loge("updateRelateApRcd update failed.");
                return false;
            }
            logd("updateRelateApRcd update succ, rowChg=" + rowChg);
            return true;
        } catch (SQLException e) {
            loge("updateRelateApRcd error:" + e);
            return false;
        }
    }

    private boolean insertRelateApRcd(WifiProRelateApRcd dbr) {
        try {
            this.mDatabase.execSQL("INSERT INTO WifiProRelateApTable VALUES(null,  ?, ?, ?, ?, ?, ?, ?)", new Object[]{dbr.apBSSID, dbr.mRelatedBSSID, Integer.valueOf(dbr.mRelateType), Integer.valueOf(dbr.mMaxCurrentRSSI), Integer.valueOf(dbr.mMaxRelatedRSSI), Integer.valueOf(dbr.mMinCurrentRSSI), Integer.valueOf(dbr.mMinRelatedRSSI)});
            logi("insertRelateApRcd add a record succ.");
            return true;
        } catch (SQLException e) {
            loge("insertRelateApRcd error:" + e);
            return false;
        }
    }

    private boolean checkRelateApRcdExist(String apBssid, String relatedBSSID) {
        boolean ret = false;
        Cursor c = null;
        try {
            Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM WifiProRelateApTable where (apBSSID like ?) and (RelatedBSSID like ?)", new String[]{apBssid, relatedBSSID});
            int rcdCount = c2.getCount();
            if (rcdCount > 0) {
                ret = true;
            }
            logd("checkRelateApRcdExist get record: " + rcdCount);
            if (c2 != null) {
                c2.close();
            }
            return ret;
        } catch (SQLException e) {
            loge("checkRelateApRcdExist error:" + e);
            if (c != null) {
                c.close();
            }
            return false;
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    public boolean addOrUpdateRelateApRcd(WifiProRelateApRcd dbr) {
        logd("addOrUpdateRelateApRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (dbr != null) {
                    if (dbr.apBSSID != null) {
                        if (dbr.mRelatedBSSID != null) {
                            if (checkRelateApRcdExist(dbr.apBSSID, dbr.mRelatedBSSID)) {
                                boolean updateRelateApRcd = updateRelateApRcd(dbr);
                                return updateRelateApRcd;
                            }
                            boolean insertRelateApRcd = insertRelateApRcd(dbr);
                            return insertRelateApRcd;
                        }
                    }
                    loge("addOrUpdateRelateApRcd null error.");
                    return false;
                }
            }
            loge("addOrUpdateRelateApRcd error.");
            return false;
        }
    }

    public boolean queryRelateApRcd(String apBssid, List<WifiProRelateApRcd> relateApList) {
        int recCnt = 0;
        logd("queryRelateApRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (apBssid != null) {
                        if (relateApList != null) {
                            relateApList.clear();
                            Cursor c = null;
                            try {
                                Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM WifiProRelateApTable where apBSSID like ?", new String[]{apBssid});
                                while (true) {
                                    if (!c2.moveToNext()) {
                                        break;
                                    }
                                    recCnt++;
                                    if (recCnt > 20) {
                                        break;
                                    }
                                    WifiProRelateApRcd dbr = new WifiProRelateApRcd(apBssid);
                                    dbr.mRelatedBSSID = c2.getString(c2.getColumnIndex("RelatedBSSID"));
                                    dbr.mRelateType = c2.getShort(c2.getColumnIndex("RelateType"));
                                    dbr.mMaxCurrentRSSI = c2.getInt(c2.getColumnIndex("MaxCurrentRSSI"));
                                    dbr.mMaxRelatedRSSI = c2.getInt(c2.getColumnIndex("MaxRelatedRSSI"));
                                    dbr.mMinCurrentRSSI = c2.getInt(c2.getColumnIndex("MinCurrentRSSI"));
                                    dbr.mMinRelatedRSSI = c2.getInt(c2.getColumnIndex("MinRelatedRSSI"));
                                    relateApList.add(dbr);
                                }
                                if (c2 != null) {
                                    c2.close();
                                }
                                if (recCnt != 0) {
                                    return true;
                                }
                                logi("queryRelateApRcd not record.");
                                return false;
                            } catch (SQLException e) {
                                try {
                                    loge("queryRelateApRcd error:" + e);
                                    if (c != null) {
                                        c.close();
                                    }
                                    return false;
                                } catch (Throwable th) {
                                    if (c != null) {
                                        c.close();
                                    }
                                    throw th;
                                }
                            }
                        }
                    }
                    loge("queryRelateApRcd null error.");
                    return false;
                }
            }
            loge("queryRelateApRcd database error.");
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
        values.put("isInBlackList", Integer.valueOf(dbr.isInBlackList));
        values.put("UpdateTime", Long.valueOf(dbr.mUpdateTime));
        try {
            int rowChg = this.mDatabase.update(WifiProHistoryDBHelper.WP_DUAL_BAND_AP_INFO_TB_NAME, values, "apBSSID like ?", new String[]{dbr.apBSSID});
            if (rowChg == 0) {
                loge("updateDualBandApInfoRcd update failed.");
                return false;
            }
            logd("updateDualBandApInfoRcd update succ, rowChg=" + rowChg);
            return true;
        } catch (SQLException e) {
            loge("updateDualBandApInfoRcd error:" + e);
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
            this.mDatabase.execSQL("INSERT INTO WifiProDualBandApInfoRcdTable VALUES(null,  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{dbr.apBSSID, dbr.mApSSID, dbr.mInetCapability, dbr.mServingBand, dbr.mApAuthType, Integer.valueOf(dbr.mChannelFrequency), Integer.valueOf(dbr.mDisappearCount), Integer.valueOf(dbr.isInBlackList), Long.valueOf(dbr.mUpdateTime)});
            logi("insertDualBandApInfoRcd add a record succ.");
            return true;
        } catch (SQLException e) {
            loge("insertDualBandApInfoRcd error:" + e);
            return false;
        }
    }

    public boolean addOrUpdateDualBandApInfoRcd(WifiProDualBandApInfoRcd dbr) {
        logd("addOrUpdateDualBandApInfoRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (dbr != null) {
                    if (dbr.apBSSID == null) {
                        loge("addOrUpdateDualBandApInfoRcd null error.");
                        return false;
                    }
                    dbr.mUpdateTime = System.currentTimeMillis();
                    if (checkHistoryRecordExist(WifiProHistoryDBHelper.WP_DUAL_BAND_AP_INFO_TB_NAME, dbr.apBSSID)) {
                        boolean updateDualBandApInfoRcd = updateDualBandApInfoRcd(dbr);
                        return updateDualBandApInfoRcd;
                    }
                    boolean insertDualBandApInfoRcd = insertDualBandApInfoRcd(dbr);
                    return insertDualBandApInfoRcd;
                }
            }
            loge("addOrUpdateDualBandApInfoRcd error.");
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00c6, code lost:
        return true;
     */
    public boolean queryDualBandApInfoRcd(String apBssid, WifiProDualBandApInfoRcd dbr) {
        int recCnt = 0;
        logd("queryDualBandApInfoRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("queryDualBandApInfoRcd database error.");
                return false;
            } else if (apBssid == null || dbr == null) {
                loge("queryDualBandApInfoRcd null error.");
                return false;
            } else {
                Cursor c = null;
                try {
                    Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable where apBSSID like ?", new String[]{apBssid});
                    while (true) {
                        if (!c2.moveToNext()) {
                            break;
                        }
                        recCnt++;
                        if (recCnt > 1) {
                            break;
                        } else if (recCnt == 1) {
                            dbr.apBSSID = apBssid;
                            dbr.mApSSID = c2.getString(c2.getColumnIndex("apSSID"));
                            dbr.mInetCapability = Short.valueOf(c2.getShort(c2.getColumnIndex("InetCapability")));
                            dbr.mServingBand = Short.valueOf(c2.getShort(c2.getColumnIndex("ServingBand")));
                            dbr.mApAuthType = Short.valueOf(c2.getShort(c2.getColumnIndex("ApAuthType")));
                            dbr.mChannelFrequency = c2.getInt(c2.getColumnIndex("ChannelFrequency"));
                            dbr.mDisappearCount = c2.getShort(c2.getColumnIndex("DisappearCount"));
                            dbr.isInBlackList = c2.getShort(c2.getColumnIndex("isInBlackList"));
                            dbr.mUpdateTime = c2.getLong(c2.getColumnIndex("UpdateTime"));
                            logi("read record succ");
                        }
                    }
                    if (c2 != null) {
                        c2.close();
                    }
                    if (recCnt > 1) {
                        loge("more than one record error. use first record.");
                    } else if (recCnt == 0) {
                        logi("queryDualBandApInfoRcd not record.");
                        return false;
                    }
                } catch (SQLException e) {
                    try {
                        loge("queryDualBandApInfoRcd error:" + e);
                        if (c != null) {
                            c.close();
                        }
                        return false;
                    } catch (Throwable th) {
                        if (c != null) {
                            c.close();
                        }
                        throw th;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00c9, code lost:
        return r0;
     */
    public List<WifiProDualBandApInfoRcd> queryDualBandApInfoRcdBySsid(String ssid) {
        List<WifiProDualBandApInfoRcd> mRecList = new ArrayList<>();
        logd("queryDualBandApInfoRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (ssid == null) {
                        loge("queryDualBandApInfoRcdBySsid null error.");
                        return null;
                    }
                    Cursor c = null;
                    try {
                        Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable where apSSID like ?", new String[]{ssid});
                        while (c2.moveToNext()) {
                            WifiProDualBandApInfoRcd dbr = new WifiProDualBandApInfoRcd(null);
                            dbr.apBSSID = c2.getString(c2.getColumnIndex("apBSSID"));
                            dbr.mApSSID = ssid;
                            dbr.mInetCapability = Short.valueOf(c2.getShort(c2.getColumnIndex("InetCapability")));
                            dbr.mServingBand = Short.valueOf(c2.getShort(c2.getColumnIndex("ServingBand")));
                            dbr.mApAuthType = Short.valueOf(c2.getShort(c2.getColumnIndex("ApAuthType")));
                            dbr.mChannelFrequency = c2.getInt(c2.getColumnIndex("ChannelFrequency"));
                            dbr.mDisappearCount = c2.getShort(c2.getColumnIndex("DisappearCount"));
                            dbr.isInBlackList = c2.getShort(c2.getColumnIndex("isInBlackList"));
                            dbr.mUpdateTime = c2.getLong(c2.getColumnIndex("UpdateTime"));
                            logi("read record succ");
                            mRecList.add(dbr);
                        }
                        if (c2 != null) {
                            c2.close();
                        }
                        if (mRecList.size() == 0) {
                            logi("queryDualBandApInfoRcdBySsid not record.");
                        }
                    } catch (SQLException e) {
                        try {
                            loge("queryDualBandApInfoRcdBySsid error:" + e);
                            if (c != null) {
                                c.close();
                            }
                            return null;
                        } catch (Throwable th) {
                            if (c != null) {
                                c.close();
                            }
                            throw th;
                        }
                    }
                }
            }
            loge("queryDualBandApInfoRcdBySsid database error.");
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x00ac, code lost:
        if (r3 != null) goto L_0x00ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00c9, code lost:
        if (r3 == null) goto L_0x00cc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00cd, code lost:
        return r1;
     */
    public List<WifiProDualBandApInfoRcd> getAllDualBandApInfo() {
        logd("getAllDualBandApInfo enter.");
        synchronized (this.mBqeLock) {
            ArrayList<WifiProDualBandApInfoRcd> apInfoList = new ArrayList<>();
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("queryDualBandApInfoRcd database error.");
                return apInfoList;
            }
            Cursor c = null;
            try {
                c = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable", null);
                while (c.moveToNext()) {
                    WifiProDualBandApInfoRcd dbr = new WifiProDualBandApInfoRcd(c.getString(c.getColumnIndex("apBSSID")));
                    dbr.mApSSID = c.getString(c.getColumnIndex("apSSID"));
                    dbr.mInetCapability = Short.valueOf(c.getShort(c.getColumnIndex("InetCapability")));
                    dbr.mServingBand = Short.valueOf(c.getShort(c.getColumnIndex("ServingBand")));
                    dbr.mApAuthType = Short.valueOf(c.getShort(c.getColumnIndex("ApAuthType")));
                    dbr.mChannelFrequency = c.getInt(c.getColumnIndex("ChannelFrequency"));
                    dbr.mDisappearCount = c.getShort(c.getColumnIndex("DisappearCount"));
                    dbr.isInBlackList = c.getShort(c.getColumnIndex("isInBlackList"));
                    dbr.mUpdateTime = c.getLong(c.getColumnIndex("UpdateTime"));
                    apInfoList.add(dbr);
                }
            } catch (SQLException e) {
                try {
                    loge("queryDualBandApInfoRcd error:" + e);
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                    throw th;
                }
            }
        }
    }

    private boolean updateApRSSIThreshold(String apBSSID, String rssiThreshold) {
        ContentValues values = new ContentValues();
        values.put("RSSIThreshold", rssiThreshold);
        try {
            int rowChg = this.mDatabase.update(WifiProHistoryDBHelper.WP_DUAL_BAND_AP_INFO_TB_NAME, values, "apBSSID like ?", new String[]{apBSSID});
            if (rowChg == 0) {
                loge("updateApRSSIThreshold update failed.");
                return false;
            }
            logd("updateApRSSIThreshold update succ, rowChg=" + rowChg);
            return true;
        } catch (SQLException e) {
            loge("updateApRSSIThreshold error:" + e);
            return false;
        }
    }

    public boolean addOrUpdateApRSSIThreshold(String apBSSID, String rssiThreshold) {
        logd("addOrUpdateApRSSIThreshold enter.");
        synchronized (this.mBqeLock) {
            if (!(this.mDatabase == null || !this.mDatabase.isOpen() || apBSSID == null)) {
                if (rssiThreshold != null) {
                    if (!checkHistoryRecordExist(WifiProHistoryDBHelper.WP_DUAL_BAND_AP_INFO_TB_NAME, apBSSID)) {
                        insertDualBandApInfoRcd(new WifiProDualBandApInfoRcd(apBSSID));
                    }
                    boolean updateApRSSIThreshold = updateApRSSIThreshold(apBSSID, rssiThreshold);
                    return updateApRSSIThreshold;
                }
            }
            loge("addOrUpdateApRSSIThreshold error.");
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0057, code lost:
        if (r3 != null) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0074, code lost:
        if (r3 == null) goto L_0x0077;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0078, code lost:
        return r1;
     */
    public String queryApRSSIThreshold(String apBssid) {
        int recCnt = 0;
        String result = null;
        logd("queryApRSSIThreshold enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || apBssid == null) {
                loge("queryApRSSIThreshold database error.");
                return null;
            }
            Cursor c = null;
            try {
                c = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable where apBSSID like ?", new String[]{apBssid});
                while (true) {
                    if (!c.moveToNext()) {
                        break;
                    }
                    recCnt++;
                    if (recCnt > 1) {
                        break;
                    } else if (recCnt == 1) {
                        result = c.getString(c.getColumnIndex("RSSIThreshold"));
                        logi("read record succ, RSSIThreshold = " + result);
                    }
                }
            } catch (SQLException e) {
                try {
                    loge("queryApRSSIThreshold error:" + e);
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                    throw th;
                }
            }
        }
    }

    public int getDualBandApInfoSize() {
        String str;
        logd("getDualBandApInfoSize enter.");
        synchronized (this.mBqeLock) {
            int result = -1;
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("getDualBandApInfoSize database error.");
                return -1;
            }
            Cursor c = null;
            try {
                Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable", null);
                result = c2.getCount();
                if (c2 != null) {
                    c2.close();
                }
                str = "getDualBandApInfoSize: " + result;
            } catch (SQLException e) {
                try {
                    loge("getDualBandApInfoSize error:" + e);
                    if (c != null) {
                        c.close();
                    }
                    str = "getDualBandApInfoSize: " + -1;
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                    logd("getDualBandApInfoSize: " + -1);
                    throw th;
                }
            }
            logd(str);
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
        return deleteAllDualBandAPRcd(oldestApInfo.apBSSID);
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
