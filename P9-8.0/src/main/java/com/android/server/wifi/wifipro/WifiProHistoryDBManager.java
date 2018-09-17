package com.android.server.wifi.wifipro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
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
    private static final short MAX_AP_INFO_RECORD_NUM = (short) 1;
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
        this.mDatabase = this.mHelper.getWritableDatabase();
    }

    public static WifiProHistoryDBManager getInstance(Context context) {
        if (mBQEDataBaseManager == null) {
            mBQEDataBaseManager = new WifiProHistoryDBManager(context);
        }
        return mBQEDataBaseManager;
    }

    /* JADX WARNING: Missing block: B:9:0x0012, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void closeDB() {
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
            } else {
                Log.w(TAG, "closeDB()");
                this.mDatabase.close();
            }
        }
    }

    private boolean deleteHistoryRecord(String dbTableName, String apBssid) {
        logi("deleteHistoryRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                loge("deleteHistoryRecord database error.");
                return false;
            } else if (apBssid == null) {
                loge("deleteHistoryRecord null error.");
                return false;
            } else {
                try {
                    this.mDatabase.delete(dbTableName, "apBSSID like ?", new String[]{apBssid});
                    return true;
                } catch (SQLException e) {
                    loge("deleteHistoryRecord error:" + e);
                    return false;
                }
            }
        }
    }

    public boolean deleteApInfoRecord(String apBssid) {
        return deleteHistoryRecord(WifiProHistoryDBHelper.WP_AP_INFO_TB_NAME, apBssid);
    }

    private boolean checkHistoryRecordExist(String dbTableName, String apBssid) {
        boolean ret = false;
        Cursor cursor = null;
        try {
            cursor = this.mDatabase.rawQuery("SELECT * FROM " + dbTableName + " where apBSSID like ?", new String[]{apBssid});
            int rcdCount = cursor.getCount();
            if (rcdCount > 0) {
                ret = true;
            }
            logd("checkHistoryRecordExist read from:" + dbTableName + ", get record: " + rcdCount);
            if (cursor != null) {
                cursor.close();
            }
            return ret;
        } catch (SQLException e) {
            loge("checkHistoryRecordExist error:" + e);
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
            boolean updateApInfoRecord;
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || dbr == null) {
                loge("addOrUpdateApInfoRecord error.");
                return false;
            } else if (dbr.apBSSID == null) {
                loge("addOrUpdateApInfoRecord null error.");
                return false;
            } else if (checkHistoryRecordExist(WifiProHistoryDBHelper.WP_AP_INFO_TB_NAME, dbr.apBSSID)) {
                updateApInfoRecord = updateApInfoRecord(dbr);
                return updateApInfoRecord;
            } else {
                updateApInfoRecord = insertApInfoRecord(dbr);
                return updateApInfoRecord;
            }
        }
    }

    /* JADX WARNING: Missing block: B:29:0x0056, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean queryApInfoRecord(String apBssid, WifiProApInfoRecord dbr) {
        int recCnt = 0;
        logd("queryApInfoRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                loge("queryApInfoRecord database error.");
                return false;
            } else if (apBssid == null || dbr == null) {
                loge("queryApInfoRecord null error.");
                return false;
            } else {
                Cursor c = null;
                try {
                    c = this.mDatabase.rawQuery("SELECT * FROM WifiProApInfoRecodTable where apBSSID like ?", new String[]{apBssid});
                    while (c.moveToNext()) {
                        recCnt++;
                        if (recCnt > 1) {
                            break;
                        }
                        logd("read record id = " + c.getInt(c.getColumnIndex("_id")));
                        if (recCnt == 1) {
                            dbr.apBSSID = apBssid;
                            dbr.apSSID = c.getString(c.getColumnIndex("apSSID"));
                            dbr.apSecurityType = c.getInt(c.getColumnIndex("apSecurityType"));
                            dbr.firstConnectTime = c.getLong(c.getColumnIndex("firstConnectTime"));
                            dbr.lastConnectTime = c.getLong(c.getColumnIndex("lastConnectTime"));
                            dbr.lanDataSize = c.getInt(c.getColumnIndex("lanDataSize"));
                            dbr.highSpdFreq = c.getInt(c.getColumnIndex("highSpdFreq"));
                            dbr.totalUseTime = c.getInt(c.getColumnIndex("totalUseTime"));
                            dbr.totalUseTimeAtNight = c.getInt(c.getColumnIndex("totalUseTimeAtNight"));
                            dbr.totalUseTimeAtWeekend = c.getInt(c.getColumnIndex("totalUseTimeAtWeekend"));
                            dbr.judgeHomeAPTime = c.getLong(c.getColumnIndex("judgeHomeAPTime"));
                            logi("read record succ, LastConnectTime:" + dbr.lastConnectTime);
                        }
                    }
                    if (c != null) {
                        c.close();
                    }
                    if (recCnt > 1) {
                        loge("more than one record error. use first record.");
                    } else if (recCnt == 0) {
                        logi("queryApInfoRecord not record.");
                    }
                } catch (SQLException e) {
                    loge("queryApInfoRecord error:" + e);
                    if (c != null) {
                        c.close();
                    }
                    return false;
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:23:0x0078, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int querySameSSIDApCount(String apBSSID, String apSsid, int secType) {
        int recCnt = 0;
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
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
                    loge("querySameSSIDApCount error:" + e);
                    if (c != null) {
                        c.close();
                    }
                    return recCnt;
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }

    public boolean removeTooOldApInfoRecord() {
        Vector<String> delRecordsVentor = new Vector();
        long currDateMsTime = new Date().getTime();
        logd("removeTooOldApInfoRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                loge("removeTooOldApInfoRecord database error.");
                return false;
            }
            delRecordsVentor.clear();
            Cursor c = null;
            try {
                c = this.mDatabase.rawQuery("SELECT * FROM WifiProApInfoRecodTable", null);
                logd("all record count=" + c.getCount());
                while (c.moveToNext()) {
                    long lastConnDateMsTime = c.getLong(c.getColumnIndex("lastConnectTime"));
                    long totalConnectTime = (long) c.getInt(c.getColumnIndex("totalUseTime"));
                    long pastDays = (currDateMsTime - lastConnDateMsTime) / MS_OF_ONE_DAY;
                    if (pastDays > TOO_OLD_VALID_DAY && totalConnectTime - (AGING_MS_OF_EACH_DAY * pastDays) < 0) {
                        logi("check result: need delete.");
                        int id = c.getInt(c.getColumnIndex("_id"));
                        String ssid = c.getString(c.getColumnIndex("apSSID"));
                        String bssid = c.getString(c.getColumnIndex("apBSSID"));
                        logi("check record: ssid:" + ssid + ", id:" + id + ", pass time:" + (currDateMsTime - lastConnDateMsTime));
                        delRecordsVentor.add(bssid);
                    }
                }
                int delSize = delRecordsVentor.size();
                logi("start delete " + delSize + " records.");
                int i = 0;
                while (i < delSize && ((String) delRecordsVentor.get(i)) != null) {
                    this.mDatabase.delete(WifiProHistoryDBHelper.WP_AP_INFO_TB_NAME, "apBSSID like ?", new String[]{delBSSID});
                    i++;
                }
                if (c != null) {
                    c.close();
                }
                return true;
            } catch (SQLException e) {
                loge("removeTooOldApInfoRecord error:" + e);
                if (c != null) {
                    c.close();
                }
                return false;
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    public boolean statisticApInfoRecord() {
        logd("statisticApInfoRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                loge("statisticApInfoRecord database error.");
                return false;
            }
            this.mHomeApRecordCount = 0;
            this.mApRecordCount = 0;
            Cursor cursor = null;
            try {
                cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProApInfoRecodTable", null);
                this.mApRecordCount = cursor.getCount();
                logi("all record count=" + this.mApRecordCount);
                while (cursor.moveToNext()) {
                    if (cursor.getLong(cursor.getColumnIndex("judgeHomeAPTime")) > 0) {
                        String ssid = cursor.getString(cursor.getColumnIndex("apSSID"));
                        this.mHomeApRecordCount++;
                        logi("check record: Home ap ssid:" + ssid + ", total:" + this.mHomeApRecordCount);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                return true;
            } catch (SQLException e) {
                loge("removeTooOldApInfoRecord error:" + e);
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
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
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || apSSID == null) {
                loge("addOrUpdateEnterpriseApRecord error.");
                return false;
            } else if (queryEnterpriseApRecord(apSSID, secType)) {
                logi("already exist the record ssid:" + apSSID);
                return true;
            } else {
                logi("add record here ssid:" + apSSID);
                boolean insertEnterpriseApRecord = insertEnterpriseApRecord(apSSID, secType);
                return insertEnterpriseApRecord;
            }
        }
    }

    public boolean queryEnterpriseApRecord(String apSSID, int secType) {
        logd("queryEnterpriseApRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                loge("queryEnterpriseApRecord database error.");
                return false;
            } else if (apSSID == null) {
                loge("queryEnterpriseApRecord null error.");
                return false;
            } else {
                Cursor c = null;
                try {
                    c = this.mDatabase.rawQuery("SELECT * FROM WifiProEnterpriseAPTable where (apSSID like ?) and (apSecurityType = ?)", new String[]{apSSID, String.valueOf(secType)});
                    int recCnt = c.getCount();
                    if (c != null) {
                        c.close();
                    }
                    if (recCnt > 0) {
                        logi("SSID:" + apSSID + ", security: " + secType + " is in Enterprise Ap table. count:" + recCnt);
                        return true;
                    }
                    logi("SSID:" + apSSID + ", security: " + secType + " is not in Enterprise Ap table. count:" + recCnt);
                    return false;
                } catch (SQLException e) {
                    loge("queryEnterpriseApRecord error:" + e);
                    if (c != null) {
                        c.close();
                    }
                    return false;
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }

    public boolean deleteEnterpriseApRecord(String tableName, String ssid, int secType) {
        if (tableName == null || ssid == null) {
            loge("deleteHistoryRecord null error.");
            return false;
        }
        logi("delete record of same (SSID:" + ssid + ", secType:" + secType + ") from " + tableName);
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                loge("deleteHistoryRecord database error.");
                return false;
            }
            try {
                logd("delete record count=" + this.mDatabase.delete(tableName, "(apSSID like ?) and (apSecurityType = ?)", new String[]{ssid, String.valueOf(secType)}));
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
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
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
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                loge("deleteRelateApRcd database error.");
                return false;
            } else if (relatedBSSID == null) {
                loge("deleteRelateApRcd null error.");
                return false;
            } else {
                try {
                    this.mDatabase.delete(WifiProHistoryDBHelper.WP_RELATE_AP_TB_NAME, "(RelatedBSSID like ?)", new String[]{relatedBSSID});
                    return true;
                } catch (SQLException e) {
                    loge("deleteRelateApRcd error:" + e);
                    return false;
                }
            }
        }
    }

    private boolean deleteAllDualBandAPRcd(String apBssid) {
        if (deleteDualBandApInfoRcd(apBssid) && deleteApQualityRcd(apBssid) && deleteRelateApRcd(apBssid)) {
            return deleteRelate5GAPRcd(apBssid);
        }
        return false;
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
            boolean updateApQualityRcd;
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || dbr == null) {
                loge("addOrUpdateApQualityRcd error.");
                return false;
            } else if (dbr.apBSSID == null) {
                loge("addOrUpdateApQualityRcd null error.");
                return false;
            } else if (checkHistoryRecordExist(WifiProHistoryDBHelper.WP_QUALITY_TB_NAME, dbr.apBSSID)) {
                updateApQualityRcd = updateApQualityRcd(dbr);
                return updateApQualityRcd;
            } else {
                updateApQualityRcd = insertApQualityRcd(dbr);
                return updateApQualityRcd;
            }
        }
    }

    /* JADX WARNING: Missing block: B:29:0x0056, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean queryApQualityRcd(String apBssid, WifiProApQualityRcd dbr) {
        int recCnt = 0;
        logd("queryApQualityRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                loge("queryApQualityRcd database error.");
                return false;
            } else if (apBssid == null || dbr == null) {
                loge("queryApQualityRcd null error.");
                return false;
            } else {
                Cursor c = null;
                try {
                    c = this.mDatabase.rawQuery("SELECT * FROM WifiProApQualityTable where apBSSID like ?", new String[]{apBssid});
                    while (c.moveToNext()) {
                        recCnt++;
                        if (recCnt > 1) {
                            break;
                        } else if (recCnt == 1) {
                            dbr.apBSSID = apBssid;
                            dbr.mRTT_Product = c.getBlob(c.getColumnIndex("RTT_Product"));
                            dbr.mRTT_PacketVolume = c.getBlob(c.getColumnIndex("RTT_PacketVolume"));
                            dbr.mHistoryAvgRtt = c.getBlob(c.getColumnIndex("HistoryAvgRtt"));
                            dbr.mOTA_LostRateValue = c.getBlob(c.getColumnIndex("OTA_LostRateValue"));
                            dbr.mOTA_PktVolume = c.getBlob(c.getColumnIndex("OTA_PktVolume"));
                            dbr.mOTA_BadPktProduct = c.getBlob(c.getColumnIndex("OTA_BadPktProduct"));
                            logi("read record succ");
                        }
                    }
                    if (c != null) {
                        c.close();
                    }
                    if (recCnt > 1) {
                        loge("more than one record error. use first record.");
                    } else if (recCnt == 0) {
                        logi("queryApQualityRcd not record.");
                        return false;
                    }
                } catch (SQLException e) {
                    loge("queryApQualityRcd error:" + e);
                    if (c != null) {
                        c.close();
                    }
                    return false;
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
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
        Cursor cursor = null;
        try {
            cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProRelateApTable where (apBSSID like ?) and (RelatedBSSID like ?)", new String[]{apBssid, relatedBSSID});
            int rcdCount = cursor.getCount();
            if (rcdCount > 0) {
                ret = true;
            }
            logd("checkRelateApRcdExist get record: " + rcdCount);
            if (cursor != null) {
                cursor.close();
            }
            return ret;
        } catch (SQLException e) {
            loge("checkRelateApRcdExist error:" + e);
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

    public boolean addOrUpdateRelateApRcd(WifiProRelateApRcd dbr) {
        logd("addOrUpdateRelateApRcd enter.");
        synchronized (this.mBqeLock) {
            boolean updateRelateApRcd;
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || dbr == null) {
                loge("addOrUpdateRelateApRcd error.");
                return false;
            } else if (dbr.apBSSID == null || dbr.mRelatedBSSID == null) {
                loge("addOrUpdateRelateApRcd null error.");
                return false;
            } else if (checkRelateApRcdExist(dbr.apBSSID, dbr.mRelatedBSSID)) {
                updateRelateApRcd = updateRelateApRcd(dbr);
                return updateRelateApRcd;
            } else {
                updateRelateApRcd = insertRelateApRcd(dbr);
                return updateRelateApRcd;
            }
        }
    }

    public boolean queryRelateApRcd(String apBssid, List<WifiProRelateApRcd> relateApList) {
        int recCnt = 0;
        logd("queryRelateApRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                loge("queryRelateApRcd database error.");
                return false;
            } else if (apBssid == null || relateApList == null) {
                loge("queryRelateApRcd null error.");
                return false;
            } else {
                relateApList.clear();
                Cursor c = null;
                try {
                    c = this.mDatabase.rawQuery("SELECT * FROM WifiProRelateApTable where apBSSID like ?", new String[]{apBssid});
                    while (c.moveToNext()) {
                        recCnt++;
                        if (recCnt > 20) {
                            break;
                        }
                        WifiProRelateApRcd dbr = new WifiProRelateApRcd(apBssid);
                        dbr.mRelatedBSSID = c.getString(c.getColumnIndex("RelatedBSSID"));
                        dbr.mRelateType = c.getShort(c.getColumnIndex("RelateType"));
                        dbr.mMaxCurrentRSSI = c.getInt(c.getColumnIndex("MaxCurrentRSSI"));
                        dbr.mMaxRelatedRSSI = c.getInt(c.getColumnIndex("MaxRelatedRSSI"));
                        dbr.mMinCurrentRSSI = c.getInt(c.getColumnIndex("MinCurrentRSSI"));
                        dbr.mMinRelatedRSSI = c.getInt(c.getColumnIndex("MinRelatedRSSI"));
                        relateApList.add(dbr);
                    }
                    if (c != null) {
                        c.close();
                    }
                    if (recCnt == 0) {
                        logi("queryRelateApRcd not record.");
                        return false;
                    }
                    return true;
                } catch (SQLException e) {
                    loge("queryRelateApRcd error:" + e);
                    if (c != null) {
                        c.close();
                    }
                    return false;
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                }
            }
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
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || dbr == null) {
                loge("addOrUpdateDualBandApInfoRcd error.");
                return false;
            } else if (dbr.apBSSID == null) {
                loge("addOrUpdateDualBandApInfoRcd null error.");
                return false;
            } else {
                dbr.mUpdateTime = System.currentTimeMillis();
                boolean updateDualBandApInfoRcd;
                if (checkHistoryRecordExist(WifiProHistoryDBHelper.WP_DUAL_BAND_AP_INFO_TB_NAME, dbr.apBSSID)) {
                    updateDualBandApInfoRcd = updateDualBandApInfoRcd(dbr);
                    return updateDualBandApInfoRcd;
                }
                updateDualBandApInfoRcd = insertDualBandApInfoRcd(dbr);
                return updateDualBandApInfoRcd;
            }
        }
    }

    /* JADX WARNING: Missing block: B:29:0x0056, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean queryDualBandApInfoRcd(String apBssid, WifiProDualBandApInfoRcd dbr) {
        int recCnt = 0;
        logd("queryDualBandApInfoRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                loge("queryDualBandApInfoRcd database error.");
                return false;
            } else if (apBssid == null || dbr == null) {
                loge("queryDualBandApInfoRcd null error.");
                return false;
            } else {
                Cursor c = null;
                try {
                    c = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable where apBSSID like ?", new String[]{apBssid});
                    while (c.moveToNext()) {
                        recCnt++;
                        if (recCnt > 1) {
                            break;
                        } else if (recCnt == 1) {
                            dbr.apBSSID = apBssid;
                            dbr.mApSSID = c.getString(c.getColumnIndex("apSSID"));
                            dbr.mInetCapability = Short.valueOf(c.getShort(c.getColumnIndex("InetCapability")));
                            dbr.mServingBand = Short.valueOf(c.getShort(c.getColumnIndex("ServingBand")));
                            dbr.mApAuthType = Short.valueOf(c.getShort(c.getColumnIndex("ApAuthType")));
                            dbr.mChannelFrequency = c.getInt(c.getColumnIndex("ChannelFrequency"));
                            dbr.mDisappearCount = c.getShort(c.getColumnIndex("DisappearCount"));
                            dbr.isInBlackList = c.getShort(c.getColumnIndex("isInBlackList"));
                            dbr.mUpdateTime = c.getLong(c.getColumnIndex("UpdateTime"));
                            logi("read record succ");
                        }
                    }
                    if (c != null) {
                        c.close();
                    }
                    if (recCnt > 1) {
                        loge("more than one record error. use first record.");
                    } else if (recCnt == 0) {
                        logi("queryDualBandApInfoRcd not record.");
                        return false;
                    }
                } catch (SQLException e) {
                    loge("queryDualBandApInfoRcd error:" + e);
                    if (c != null) {
                        c.close();
                    }
                    return false;
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:37:0x00fd, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<WifiProDualBandApInfoRcd> queryDualBandApInfoRcdBySsid(String ssid) {
        List<WifiProDualBandApInfoRcd> mRecList = new ArrayList();
        logd("queryDualBandApInfoRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                loge("queryDualBandApInfoRcdBySsid database error.");
                return null;
            } else if (ssid == null) {
                loge("queryDualBandApInfoRcdBySsid null error.");
                return null;
            } else {
                Cursor cursor = null;
                try {
                    cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable where apSSID like ?", new String[]{ssid});
                    while (cursor.moveToNext()) {
                        WifiProDualBandApInfoRcd dbr = new WifiProDualBandApInfoRcd(null);
                        dbr.apBSSID = cursor.getString(cursor.getColumnIndex("apBSSID"));
                        dbr.mApSSID = ssid;
                        dbr.mInetCapability = Short.valueOf(cursor.getShort(cursor.getColumnIndex("InetCapability")));
                        dbr.mServingBand = Short.valueOf(cursor.getShort(cursor.getColumnIndex("ServingBand")));
                        dbr.mApAuthType = Short.valueOf(cursor.getShort(cursor.getColumnIndex("ApAuthType")));
                        dbr.mChannelFrequency = cursor.getInt(cursor.getColumnIndex("ChannelFrequency"));
                        dbr.mDisappearCount = cursor.getShort(cursor.getColumnIndex("DisappearCount"));
                        dbr.isInBlackList = cursor.getShort(cursor.getColumnIndex("isInBlackList"));
                        dbr.mUpdateTime = cursor.getLong(cursor.getColumnIndex("UpdateTime"));
                        logi("read record succ");
                        mRecList.add(dbr);
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (mRecList.size() == 0) {
                        logi("queryDualBandApInfoRcdBySsid not record.");
                    }
                } catch (SQLException e) {
                    loge("queryDualBandApInfoRcdBySsid error:" + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return null;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    public List<WifiProDualBandApInfoRcd> getAllDualBandApInfo() {
        logd("getAllDualBandApInfo enter.");
        synchronized (this.mBqeLock) {
            ArrayList<WifiProDualBandApInfoRcd> apInfoList = new ArrayList();
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                loge("queryDualBandApInfoRcd database error.");
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
                    dbr.isInBlackList = cursor.getShort(cursor.getColumnIndex("isInBlackList"));
                    dbr.mUpdateTime = cursor.getLong(cursor.getColumnIndex("UpdateTime"));
                    apInfoList.add(dbr);
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLException e) {
                loge("queryDualBandApInfoRcd error:" + e);
                if (cursor != null) {
                    cursor.close();
                }
                return apInfoList;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
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
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || apBSSID == null || rssiThreshold == null) {
                loge("addOrUpdateApRSSIThreshold error.");
                return false;
            }
            if (!checkHistoryRecordExist(WifiProHistoryDBHelper.WP_DUAL_BAND_AP_INFO_TB_NAME, apBSSID)) {
                insertDualBandApInfoRcd(new WifiProDualBandApInfoRcd(apBSSID));
            }
            boolean updateApRSSIThreshold = updateApRSSIThreshold(apBSSID, rssiThreshold);
            return updateApRSSIThreshold;
        }
    }

    public String queryApRSSIThreshold(String apBssid) {
        int recCnt = 0;
        String result = null;
        logd("queryApRSSIThreshold enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || apBssid == null) {
                loge("queryApRSSIThreshold database error.");
                return null;
            }
            Cursor c = null;
            try {
                c = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable where apBSSID like ?", new String[]{apBssid});
                while (c.moveToNext()) {
                    recCnt++;
                    if (recCnt > 1) {
                        break;
                    } else if (recCnt == 1) {
                        result = c.getString(c.getColumnIndex("RSSIThreshold"));
                        logi("read record succ, RSSIThreshold = " + result);
                    }
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLException e) {
                loge("queryApRSSIThreshold error:" + e);
                if (c != null) {
                    c.close();
                }
                return result;
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    public int getDualBandApInfoSize() {
        logd("getDualBandApInfoSize enter.");
        synchronized (this.mBqeLock) {
            int result = -1;
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                loge("getDualBandApInfoSize database error.");
                return -1;
            }
            Cursor c = null;
            try {
                c = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable", null);
                result = c.getCount();
                if (c != null) {
                    c.close();
                }
                logd("getDualBandApInfoSize: " + result);
                return result;
            } catch (SQLException e) {
                loge("getDualBandApInfoSize error:" + e);
                if (c != null) {
                    c.close();
                }
                logd("getDualBandApInfoSize: " + result);
                return -1;
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
                logd("getDualBandApInfoSize: " + result);
                return -1;
            }
        }
    }

    private boolean deleteOldestDualBandApInfo() {
        List<WifiProDualBandApInfoRcd> allApInfos = getAllDualBandApInfo();
        if (allApInfos.size() <= 0) {
            return false;
        }
        WifiProDualBandApInfoRcd oldestApInfo = (WifiProDualBandApInfoRcd) allApInfos.get(0);
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
