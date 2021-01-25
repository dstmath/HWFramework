package com.android.server.hidata.histream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.android.server.hidata.wavemapping.cons.Constant;

public class HwHiStreamDataBaseManager {
    private static HwHiStreamDataBaseManager mHwHiStreamDataBaseManager = null;
    private SQLiteDatabase mDatabase;
    private HwHiStreamDataBase mHelper;
    private final Object mSQLLock = new Object();

    private HwHiStreamDataBaseManager(Context context) {
        this.mHelper = new HwHiStreamDataBase(context);
        this.mDatabase = this.mHelper.getWritableDatabase();
    }

    public static HwHiStreamDataBaseManager getInstance(Context context) {
        if (mHwHiStreamDataBaseManager == null) {
            mHwHiStreamDataBaseManager = new HwHiStreamDataBaseManager(context);
        }
        return mHwHiStreamDataBaseManager;
    }

    public void closeHiStreamDB() {
        synchronized (this.mSQLLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    this.mDatabase.close();
                }
            }
        }
    }

    public void dropTrafficInfoTable() {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase == null) {
            HwHiStreamUtils.logE(false, "dropTrafficInfoTable mDatabase null:", new Object[0]);
            return;
        }
        try {
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS HiStreamTrafficTable");
        } catch (SQLException e) {
            HwHiStreamUtils.logE(false, "updateHistreamDataBase error:%{public}s", e.getMessage());
        }
        HwHiStreamUtils.logE(false, "dropTrafficInfoTable success", new Object[0]);
    }

    private boolean updateApRecordInfo(HiStreamAPInfo dbr) {
        if (dbr == null) {
            return false;
        }
        HwHiStreamUtils.logD(false, "updateApRecordInfo:APUSRType=%{public}d,scenceId=%{public}d", Integer.valueOf(dbr.APUsrType), Integer.valueOf(dbr.mScenceId));
        ContentValues values = new ContentValues();
        values.put("SSID", dbr.mSSID);
        values.put("mScenario", Integer.valueOf(dbr.mScenceId));
        values.put("APUSRType", Integer.valueOf(dbr.APUsrType));
        try {
            if (this.mDatabase.update(HwHiStreamDataBase.HISTERAM_WECHAT_AP_NAME, values, "(SSID like ?) and (mScenario = ?)", new String[]{dbr.mSSID, String.valueOf(dbr.mScenceId)}) != 0) {
                return true;
            }
            HwHiStreamUtils.logE(false, "updateApInfo update failed.", new Object[0]);
            return false;
        } catch (SQLException e) {
            HwHiStreamUtils.logE(false, "updateApInfo error:%{public}s", e.getMessage());
            return false;
        }
    }

    private boolean insertApRecordInfo(HiStreamAPInfo dbr) {
        if (dbr == null) {
            return false;
        }
        HwHiStreamUtils.logD(false, "insertApRecordInfo:APUSRType=%{public}d,mScenceId=%{public}d", Integer.valueOf(dbr.APUsrType), Integer.valueOf(dbr.mScenceId));
        try {
            this.mDatabase.execSQL("INSERT INTO HiStreamAPRecordTable VALUES(null,  ?, ?, ?)", new Object[]{dbr.mSSID, Integer.valueOf(dbr.mScenceId), Integer.valueOf(dbr.APUsrType)});
            return true;
        } catch (SQLException e) {
            HwHiStreamUtils.logE(false, "insertApInfo error:%{public}s", e.getMessage());
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0028, code lost:
        if (r2 != null) goto L_0x002a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002a, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003f, code lost:
        if (0 == 0) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0042, code lost:
        return r1;
     */
    private boolean checkAPRecordExist(HiStreamAPInfo dbr) {
        if (dbr == null) {
            return false;
        }
        boolean ret = false;
        Cursor c = null;
        try {
            c = this.mDatabase.rawQuery("SELECT * FROM HiStreamAPRecordTable where (SSID like ?) and (mScenario = ?)", new String[]{dbr.mSSID, String.valueOf(dbr.mScenceId)});
            if (c != null && c.getCount() > 0) {
                ret = true;
            }
        } catch (SQLException e) {
            HwHiStreamUtils.logE(false, "checkAPRecordExist error:%{public}s", e.getMessage());
            ret = false;
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    public boolean addOrUpdateApRecordInfo(HiStreamAPInfo dbr) {
        synchronized (this.mSQLLock) {
            if (!(this.mDatabase == null || !this.mDatabase.isOpen() || dbr == null)) {
                if (dbr.mSSID != null) {
                    if (checkAPRecordExist(dbr)) {
                        return updateApRecordInfo(dbr);
                    }
                    return insertApRecordInfo(dbr);
                }
            }
            HwHiStreamUtils.logE(false, "addOrUpdateApRecordInfo error.", new Object[0]);
            return false;
        }
    }

    public HiStreamAPInfo queryApRecordInfo(String ssid, int scenceId) {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase == null || !sQLiteDatabase.isOpen() || ssid == null) {
            HwHiStreamUtils.logE(false, "queryApRecordInfo null error.", new Object[0]);
            return null;
        }
        Cursor c = null;
        try {
            Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM HiStreamAPRecordTable where (SSID like ?) and (mScenario = ?)", new String[]{ssid, String.valueOf(scenceId)});
            if (c2 == null || c2.getCount() <= 0) {
                if (c2 != null) {
                    c2.close();
                }
                return null;
            } else if (c2.moveToNext()) {
                HiStreamAPInfo record = new HiStreamAPInfo(ssid, scenceId, c2.getInt(c2.getColumnIndex("APUSRType")));
                c2.close();
                return record;
            } else {
                c2.close();
                return null;
            }
        } catch (SQLException e) {
            HwHiStreamUtils.logE(false, "queryApRecordInfo error:%{public}s", e.getMessage());
            if (0 != 0) {
                c.close();
            }
            return null;
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    private boolean checkTrafficInfoExist(String subId, int callType, int netType, long currDay) {
        if (subId == null) {
            return false;
        }
        Cursor c = null;
        if (800 == netType) {
            try {
                c = this.mDatabase.rawQuery("SELECT * FROM HiStreamTrafficTable where (CALLTYPE = ?) and (NETTYPE = ?) and (CURRDAY = ?)", new String[]{String.valueOf(callType), String.valueOf(netType), String.valueOf(currDay)});
            } catch (SQLException e) {
                HwHiStreamUtils.logE(false, "checkTrafficInfoExist error:%{public}s", e.getMessage());
                if (0 != 0) {
                    c.close();
                }
                return false;
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
        } else if (801 == netType) {
            c = this.mDatabase.rawQuery("SELECT * FROM HiStreamTrafficTable where (SUBID = ?) and (CALLTYPE = ?) and (NETTYPE = ?) and (CURRDAY = ?)", new String[]{subId, String.valueOf(callType), String.valueOf(netType), String.valueOf(currDay)});
        }
        if (c == null || c.getCount() <= 0) {
            if (c != null) {
                c.close();
            }
            return false;
        }
        c.close();
        return true;
    }

    public HwHiStreamTrafficInfo queryTrafficInfo(String subId, int callType, int netType, long currDay) {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase == null || !sQLiteDatabase.isOpen() || subId == null) {
            HwHiStreamUtils.logE(false, "queryTrafficInfo database error.", new Object[0]);
            return null;
        }
        Cursor c = null;
        if (800 == netType) {
            try {
                c = this.mDatabase.rawQuery("SELECT * FROM HiStreamTrafficTable where (CALLTYPE = ?) and (NETTYPE = ?) and (CURRDAY = ?)", new String[]{String.valueOf(callType), String.valueOf(netType), String.valueOf(currDay)});
            } catch (SQLException e) {
                HwHiStreamUtils.logE(false, "queryTrafficInfo error:%{public}s", e.getMessage());
                if (0 != 0) {
                    c.close();
                }
                return null;
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
        } else if (801 == netType) {
            c = this.mDatabase.rawQuery("SELECT * FROM HiStreamTrafficTable where (SUBID = ?) and (CALLTYPE = ?) and (NETTYPE = ?) and (CURRDAY = ?)", new String[]{subId, String.valueOf(callType), String.valueOf(netType), String.valueOf(currDay)});
        }
        if (c == null || c.getCount() <= 0) {
            if (c != null) {
                c.close();
            }
            return null;
        } else if (c.moveToNext()) {
            HwHiStreamTrafficInfo record = new HwHiStreamTrafficInfo();
            record.mSubId = subId;
            record.mCallType = callType;
            record.mNetType = netType;
            record.mCurrDay = c.getLong(c.getColumnIndex("CURRDAY"));
            record.mTraffic = c.getLong(c.getColumnIndex("TRAFFIC"));
            c.close();
            return record;
        } else {
            c.close();
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00b7, code lost:
        if (r3 != null) goto L_0x00b9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00b9, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00ca, code lost:
        if (0 == 0) goto L_0x00cd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00cd, code lost:
        return r7;
     */
    public long getRecentWeekTotalTraffic(String subId, int callType, int netType, long currDay) {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase == null || !sQLiteDatabase.isOpen() || subId == null) {
            HwHiStreamUtils.logE(false, "getRecentWeekTotalTraffic database error.", new Object[0]);
            return 0;
        }
        Cursor c = null;
        long queryStartTime = currDay - Constant.MILLISEC_SEVEN_DAYS;
        long totalTraffic = 0;
        if (800 == netType) {
            try {
                c = this.mDatabase.rawQuery("SELECT * FROM HiStreamTrafficTable where (CALLTYPE = ?) and (NETTYPE = ?) and (CURRDAY >= ?) and (CURRDAY < ?)", new String[]{String.valueOf(callType), String.valueOf(netType), String.valueOf(queryStartTime), String.valueOf(currDay)});
            } catch (SQLException e) {
                HwHiStreamUtils.logE(false, "getRecentWeekTotalTraffic error:%{public}s", e.getMessage());
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
        } else if (801 == netType) {
            c = this.mDatabase.rawQuery("SELECT * FROM HiStreamTrafficTable where (CALLTYPE = ?) and (NETTYPE = ?) and (CURRDAY >= ?) and (CURRDAY < ?)", new String[]{String.valueOf(callType), String.valueOf(netType), String.valueOf(queryStartTime), String.valueOf(currDay)});
        }
        if (c == null || c.getCount() <= 0) {
            HwHiStreamUtils.logE(false, "getRecentWeekTotalTraffic no records.", new Object[0]);
        } else {
            while (c.moveToNext()) {
                totalTraffic += c.getLong(c.getColumnIndex("TRAFFIC"));
                HwHiStreamUtils.logD(false, "getRecentWeekTotalTraffic TIME = :%{public}s, TAFFIC = %{public}s", String.valueOf(c.getLong(c.getColumnIndex("CURRDAY"))), String.valueOf(c.getLong(c.getColumnIndex("TRAFFIC"))));
            }
        }
    }

    private int getTrafficRecordCounts(String subId, int callType, int netType) {
        if (subId == null) {
            return 0;
        }
        Cursor c = null;
        if (800 == netType) {
            try {
                c = this.mDatabase.rawQuery("SELECT * FROM HiStreamTrafficTable where (CALLTYPE = ?) and (NETTYPE = ?)", new String[]{String.valueOf(callType), String.valueOf(netType)});
            } catch (SQLException e) {
                HwHiStreamUtils.logE(false, "checkTrafficInfoExist error:%{public}s", e.getMessage());
                if (0 != 0) {
                    c.close();
                }
                return 0;
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
        } else if (801 == netType) {
            c = this.mDatabase.rawQuery("SELECT * FROM HiStreamTrafficTable where (SUBID = ?) and (CALLTYPE = ?) and (NETTYPE = ?)", new String[]{subId, String.valueOf(callType), String.valueOf(netType)});
        }
        if (c == null || c.getCount() <= 0) {
            if (c != null) {
                c.close();
            }
            return 0;
        }
        HwHiStreamUtils.logD(false, "getTrafficRecordCounts count = %{public}d", Integer.valueOf(c.getCount()));
        int count = c.getCount();
        c.close();
        return count;
    }

    public long getEarlierTrafficRecordTime(String subId, int callType, int netType) {
        Cursor c = null;
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase == null || !sQLiteDatabase.isOpen() || subId == null) {
            HwHiStreamUtils.logE(false, "getRecentWeekTotalTraffic error.", new Object[0]);
            return 0;
        }
        if (800 == netType) {
            try {
                c = this.mDatabase.rawQuery("SELECT * FROM HiStreamTrafficTable where (CALLTYPE = ?) and (NETTYPE = ?)ORDER BY CURRDAY ASC", new String[]{String.valueOf(callType), String.valueOf(netType)});
            } catch (SQLException e) {
                HwHiStreamUtils.logE(false, "getEarlierTrafficRecordTime error:%{public}s", e.getMessage());
                if (0 != 0) {
                    c.close();
                }
                return 0;
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
        } else if (801 == netType) {
            c = this.mDatabase.rawQuery("SELECT * FROM HiStreamTrafficTable where (SUBID = ?) and (CALLTYPE = ?) and (NETTYPE = ?)ORDER BY CURRDAY ASC", new String[]{subId, String.valueOf(callType), String.valueOf(netType)});
        }
        if (c == null || c.getCount() <= 0) {
            HwHiStreamUtils.logE(false, "getEarlierTrafficRecordTime cannot find the Earliest record:", new Object[0]);
            if (c != null) {
                c.close();
            }
            return 0;
        } else if (c.moveToNext()) {
            long j = c.getLong(c.getColumnIndex("CURRDAY"));
            c.close();
            return j;
        } else {
            c.close();
            return 0;
        }
    }

    private boolean deleteEarliestTrafficRecord(String subId, int callType, int netType) {
        HwHiStreamUtils.logE(false, "deleteEarliestTrafficRecord enter", new Object[0]);
        if (subId == null) {
            return false;
        }
        Cursor c = null;
        int recordId = -1;
        if (800 == netType) {
            try {
                c = this.mDatabase.rawQuery("SELECT * FROM HiStreamTrafficTable where (CALLTYPE = ?) and (NETTYPE = ?)ORDER BY CURRDAY ASC", new String[]{String.valueOf(callType), String.valueOf(netType)});
            } catch (SQLException e) {
                HwHiStreamUtils.logE(false, "deleteEarliestTrafficRecord error:%{public}s", e.getMessage());
                if (0 != 0) {
                    c.close();
                }
                return false;
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
        } else if (801 == netType) {
            c = this.mDatabase.rawQuery("SELECT * FROM HiStreamTrafficTable where (SUBID = ?) and (CALLTYPE = ?) and (NETTYPE = ?)ORDER BY CURRDAY ASC", new String[]{subId, String.valueOf(callType), String.valueOf(netType)});
        }
        if (c == null || c.getCount() <= 0) {
            HwHiStreamUtils.logE(false, "deleteEarliestTrafficRecord cannot find the Earliest record:", new Object[0]);
            if (c != null) {
                c.close();
            }
            return false;
        }
        while (c.moveToNext()) {
            recordId = c.getInt(c.getColumnIndex("_id"));
        }
        this.mDatabase.execSQL("DELETE FROM HiStreamTrafficTable where (_id = ?)", new Object[]{Integer.valueOf(recordId)});
        HwHiStreamUtils.logE(false, "deleteEarliestTrafficRecord delete the record which id = %{public}d", Integer.valueOf(recordId));
        c.close();
        return true;
    }

    private boolean insertTrafficInfo(String subId, int callType, int netType, long currDay, long mCurrTraffic) {
        HwHiStreamUtils.logE(false, "insertTrafficInfo enter", new Object[0]);
        if (7 <= getTrafficRecordCounts(subId, callType, netType)) {
            deleteEarliestTrafficRecord(subId, callType, netType);
        }
        try {
            this.mDatabase.execSQL("INSERT INTO HiStreamTrafficTable VALUES(null,  ?, ?, ?, ?, ?)", new Object[]{subId, Integer.valueOf(callType), Integer.valueOf(netType), Long.valueOf(currDay), Long.valueOf(mCurrTraffic)});
            return true;
        } catch (SQLException e) {
            HwHiStreamUtils.logE(false, "insertTrafficInfo error:%{public}s", e.getMessage());
            return false;
        }
    }

    private boolean updateTrafficInfo(HwHiStreamTrafficInfo dbr) {
        if (dbr == null) {
            return false;
        }
        int rowChg = 0;
        ContentValues values = new ContentValues();
        values.put("SUBID", dbr.mSubId);
        values.put("CALLTYPE", Integer.valueOf(dbr.mCallType));
        values.put("NETTYPE", Integer.valueOf(dbr.mNetType));
        values.put("CURRDAY", Long.valueOf(dbr.mCurrDay));
        values.put("TRAFFIC", Long.valueOf(dbr.mTraffic));
        try {
            if (800 == dbr.mNetType) {
                rowChg = this.mDatabase.update(HwHiStreamDataBase.HISTREAM_WECHAT_TRAFFIC_NAME, values, "(CALLTYPE = ?) and (NETTYPE = ?) and (CURRDAY = ?)", new String[]{String.valueOf(dbr.mCallType), String.valueOf(dbr.mNetType), String.valueOf(dbr.mCurrDay)});
            } else if (801 == dbr.mNetType) {
                rowChg = this.mDatabase.update(HwHiStreamDataBase.HISTREAM_WECHAT_TRAFFIC_NAME, values, "(SUBID = ?) and (CALLTYPE = ?) and (NETTYPE = ?) and (CURRDAY = ?)", new String[]{String.valueOf(dbr.mSubId), String.valueOf(dbr.mCallType), String.valueOf(dbr.mNetType), String.valueOf(dbr.mCurrDay)});
            }
            if (rowChg == 0) {
                HwHiStreamUtils.logE(false, "updateApInfo update failed.", new Object[0]);
                return false;
            }
            printTrafficInfo(dbr);
            return true;
        } catch (SQLException e) {
            HwHiStreamUtils.logE(false, "UpdateTrafficInfo error:%{public}s", e.getMessage());
            return false;
        }
    }

    private boolean updateTrafficInfo(String subId, int callType, int netType, long currDay, long mCurrTraffic) {
        HwHiStreamTrafficInfo record = queryTrafficInfo(subId, callType, netType, currDay);
        HwHiStreamUtils.logD(false, "UpdateTrafficInfo enter", new Object[0]);
        if (record == null) {
            HwHiStreamUtils.logE(false, "UpdateTrafficInfo,record is null .", new Object[0]);
            return false;
        }
        record.mTraffic += mCurrTraffic;
        return updateTrafficInfo(record);
    }

    public boolean addOrUpdateTrafficInfo(String subId, int callType, int netType, long currDay, long mCurrTraffic) {
        synchronized (this.mSQLLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (subId != null) {
                    HwHiStreamUtils.logD(false, "addOrUpdateTrafficInfo calltype=%{public}d, netType=%{public}d,currDay=%{public}s, mCurrTraffic=%{public}s", Integer.valueOf(callType), Integer.valueOf(netType), String.valueOf(currDay), String.valueOf(mCurrTraffic));
                    if (checkTrafficInfoExist(subId, callType, netType, currDay)) {
                        return updateTrafficInfo(subId, callType, netType, currDay, mCurrTraffic);
                    }
                    return insertTrafficInfo(subId, callType, netType, currDay, mCurrTraffic);
                }
            }
            HwHiStreamUtils.logE(false, "addOrUpdateTrafficInfo error.", new Object[0]);
            return false;
        }
    }

    public void printTrafficInfo(HwHiStreamTrafficInfo trafficInfo) {
        if (trafficInfo == null) {
            HwHiStreamUtils.logD(false, "printTrafficInfo ERROR ", new Object[0]);
        } else {
            HwHiStreamUtils.logD(false, "trafficInfo,mCallType = %{public}d, mNetType = %{public}d, mCurrDay = %{public}s ,mTraffic = %{public}s", Integer.valueOf(trafficInfo.mCallType), Integer.valueOf(trafficInfo.mNetType), String.valueOf(trafficInfo.mCurrDay), String.valueOf(trafficInfo.mTraffic));
        }
    }

    public void insertAppStatistics(HwHistreamCHRStatisticsInfo chrInfo) {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase != null && sQLiteDatabase.isOpen() && chrInfo != null) {
            try {
                this.mDatabase.execSQL("INSERT INTO HiStreamAppStatisticsTable VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{Integer.valueOf(chrInfo.mScenario), Integer.valueOf(chrInfo.mNum), Integer.valueOf(chrInfo.mStartInWiFiCnt), Integer.valueOf(chrInfo.mStartInCellularCnt), Integer.valueOf(chrInfo.mCallInCellularDur), Integer.valueOf(chrInfo.mCallInWiFiDur), Integer.valueOf(chrInfo.mCellLv1Cnt), Integer.valueOf(chrInfo.mCellLv2Cnt), Integer.valueOf(chrInfo.mCellLv3Cnt), Integer.valueOf(chrInfo.mWiFiLv1Cnt), Integer.valueOf(chrInfo.mWiFiLv2Cnt), Integer.valueOf(chrInfo.mWiFiLv3Cnt), Integer.valueOf(chrInfo.mTrfficCell), Integer.valueOf(chrInfo.mVipSwitchCnt), Integer.valueOf(chrInfo.mStallSwitchCnt), Integer.valueOf(chrInfo.mStallSwitch0Cnt), Integer.valueOf(chrInfo.mStallSwitch1Cnt), Integer.valueOf(chrInfo.mStallSwitchAbove1Cnt), Integer.valueOf(chrInfo.mSwitch2CellCnt), Integer.valueOf(chrInfo.mSwitch2WifiCnt), Integer.valueOf(chrInfo.mMplinkDur), Integer.valueOf(chrInfo.mMplinkEnCnt), Integer.valueOf(chrInfo.mMplinkDisStallCnt), Integer.valueOf(chrInfo.mMplinkDisWifiGoodCnt), Integer.valueOf(chrInfo.mMplinkEnFailCnt), Integer.valueOf(chrInfo.mMplinkDisFailCnt), Integer.valueOf(chrInfo.mMplinkEnTraf), Integer.valueOf(chrInfo.mMplinkEnFailEnvironCnt), Integer.valueOf(chrInfo.mMplinkEnFailCoexistCnt), Integer.valueOf(chrInfo.mMplinkEnFailPingPongCnt), Integer.valueOf(chrInfo.mMplinkEnFailHistoryQoeCnt), Integer.valueOf(chrInfo.mMplinkEnFailChQoeCnt), Integer.valueOf(chrInfo.mHicureEnCnt), Integer.valueOf(chrInfo.mHicureSucCnt), Long.valueOf(chrInfo.mLastUploadTime)});
            } catch (SQLException e) {
                HwHiStreamUtils.logE(false, "insertAppStatistics error:%{public}s", e.getMessage());
            }
        }
    }

    public void updateAppStatistics(HwHistreamCHRStatisticsInfo chrInfo) {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase != null && sQLiteDatabase.isOpen() && chrInfo != null) {
            ContentValues values = new ContentValues();
            values.put("mScenario", Integer.valueOf(chrInfo.mScenario));
            values.put("mNum", Integer.valueOf(chrInfo.mNum));
            values.put("mStartInWiFiCnt", Integer.valueOf(chrInfo.mStartInWiFiCnt));
            values.put("mStartInCellularCnt", Integer.valueOf(chrInfo.mStartInCellularCnt));
            values.put("mCallInCellularDur", Integer.valueOf(chrInfo.mCallInCellularDur));
            values.put("mCallInWiFiDur", Integer.valueOf(chrInfo.mCallInWiFiDur));
            values.put("mCellLv1Cnt", Integer.valueOf(chrInfo.mCellLv1Cnt));
            values.put("mCellLv2Cnt", Integer.valueOf(chrInfo.mCellLv2Cnt));
            values.put("mCellLv3Cnt", Integer.valueOf(chrInfo.mCellLv3Cnt));
            values.put("mWiFiLv1Cnt", Integer.valueOf(chrInfo.mWiFiLv1Cnt));
            values.put("mWiFiLv2Cnt", Integer.valueOf(chrInfo.mWiFiLv2Cnt));
            values.put("mWiFiLv3Cnt", Integer.valueOf(chrInfo.mWiFiLv3Cnt));
            values.put("mTrfficCell", Integer.valueOf(chrInfo.mTrfficCell));
            values.put("mVipSwitchCnt", Integer.valueOf(chrInfo.mVipSwitchCnt));
            values.put("mStallSwitchCnt", Integer.valueOf(chrInfo.mStallSwitchCnt));
            values.put("mStallSwitch0Cnt", Integer.valueOf(chrInfo.mStallSwitch0Cnt));
            values.put("mStallSwitch1Cnt", Integer.valueOf(chrInfo.mStallSwitch1Cnt));
            values.put("mStallSwitchAbove1Cnt", Integer.valueOf(chrInfo.mStallSwitchAbove1Cnt));
            values.put("mSwitch2CellCnt", Integer.valueOf(chrInfo.mSwitch2CellCnt));
            values.put("mSwitch2WifiCnt", Integer.valueOf(chrInfo.mSwitch2WifiCnt));
            values.put("mMplinkDur", Integer.valueOf(chrInfo.mMplinkDur));
            values.put("mMplinkEnCnt", Integer.valueOf(chrInfo.mMplinkEnCnt));
            values.put("mMplinkDisStallCnt", Integer.valueOf(chrInfo.mMplinkDisStallCnt));
            values.put("mMplinkDisWifiGoodCnt", Integer.valueOf(chrInfo.mMplinkDisWifiGoodCnt));
            values.put("mMplinkEnFailCnt", Integer.valueOf(chrInfo.mMplinkEnFailCnt));
            values.put("mMplinkDisFailCnt", Integer.valueOf(chrInfo.mMplinkDisFailCnt));
            values.put("mMplinkEnTraf", Integer.valueOf(chrInfo.mMplinkEnTraf));
            values.put("MplinkEnFailEnvironCnt", Integer.valueOf(chrInfo.mMplinkEnFailEnvironCnt));
            values.put("mMplinkEnFailCoexistCnt", Integer.valueOf(chrInfo.mMplinkEnFailCoexistCnt));
            values.put("mMplinkEnFailPingPongCnt", Integer.valueOf(chrInfo.mMplinkEnFailPingPongCnt));
            values.put("mMplinkEnFailHistoryQoeCnt", Integer.valueOf(chrInfo.mMplinkEnFailHistoryQoeCnt));
            values.put("mMplinkEnFailChQoeCnt", Integer.valueOf(chrInfo.mMplinkEnFailChQoeCnt));
            values.put("mHicureEnCnt", Integer.valueOf(chrInfo.mHicureEnCnt));
            values.put("mHicureSucCnt", Integer.valueOf(chrInfo.mHicureSucCnt));
            values.put("mLastUploadTime", Long.valueOf(chrInfo.mLastUploadTime));
            try {
                this.mDatabase.update(HwHiStreamDataBase.HISTREAM_APP_STATISTICS, values, "mScenario = ?", new String[]{String.valueOf(chrInfo.mScenario)});
            } catch (SQLException e) {
                HwHiStreamUtils.logE(false, "updateAppStatistics error:%{public}s", e.getMessage());
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x01d7, code lost:
        if (r1 != null) goto L_0x01d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x01d9, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x01ed, code lost:
        if (0 == 0) goto L_0x01f0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x01f0, code lost:
        return r3;
     */
    public HwHistreamCHRStatisticsInfo getAppStatistics(int scenario) {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase == null || !sQLiteDatabase.isOpen()) {
            HwHiStreamUtils.logE(false, "getAppStatistics database error.", new Object[0]);
            return null;
        }
        Cursor c = null;
        HwHistreamCHRStatisticsInfo statistics = null;
        try {
            c = this.mDatabase.rawQuery("SELECT * FROM HiStreamAppStatisticsTable where (mScenario = ?)", new String[]{String.valueOf(scenario)});
            if (c != null && c.getCount() > 0 && c.moveToNext()) {
                statistics = new HwHistreamCHRStatisticsInfo(scenario);
                statistics.mScenario = c.getInt(c.getColumnIndex("mScenario"));
                statistics.mNum = c.getInt(c.getColumnIndex("mNum"));
                statistics.mStartInWiFiCnt = c.getInt(c.getColumnIndex("mStartInWiFiCnt"));
                statistics.mStartInCellularCnt = c.getInt(c.getColumnIndex("mStartInCellularCnt"));
                statistics.mCallInCellularDur = c.getInt(c.getColumnIndex("mCallInCellularDur"));
                statistics.mCallInWiFiDur = c.getInt(c.getColumnIndex("mCallInWiFiDur"));
                statistics.mCellLv1Cnt = c.getInt(c.getColumnIndex("mCellLv1Cnt"));
                statistics.mCellLv2Cnt = c.getInt(c.getColumnIndex("mCellLv2Cnt"));
                statistics.mCellLv2Cnt = c.getInt(c.getColumnIndex("mCellLv2Cnt"));
                statistics.mWiFiLv1Cnt = c.getInt(c.getColumnIndex("mWiFiLv1Cnt"));
                statistics.mWiFiLv2Cnt = c.getInt(c.getColumnIndex("mWiFiLv2Cnt"));
                statistics.mWiFiLv3Cnt = c.getInt(c.getColumnIndex("mWiFiLv3Cnt"));
                statistics.mTrfficCell = c.getInt(c.getColumnIndex("mTrfficCell"));
                statistics.mVipSwitchCnt = c.getInt(c.getColumnIndex("mVipSwitchCnt"));
                statistics.mStallSwitchCnt = c.getInt(c.getColumnIndex("mStallSwitchCnt"));
                statistics.mStallSwitch0Cnt = c.getInt(c.getColumnIndex("mStallSwitch0Cnt"));
                statistics.mStallSwitch1Cnt = c.getInt(c.getColumnIndex("mStallSwitch1Cnt"));
                statistics.mStallSwitchAbove1Cnt = c.getInt(c.getColumnIndex("mStallSwitchAbove1Cnt"));
                statistics.mSwitch2CellCnt = c.getInt(c.getColumnIndex("mSwitch2CellCnt"));
                statistics.mSwitch2WifiCnt = c.getInt(c.getColumnIndex("mSwitch2WifiCnt"));
                statistics.mLastUploadTime = c.getLong(c.getColumnIndex("mLastUploadTime"));
                statistics.mMplinkDur = c.getInt(c.getColumnIndex("mMplinkDur"));
                statistics.mMplinkEnCnt = c.getInt(c.getColumnIndex("mMplinkEnCnt"));
                statistics.mMplinkDisStallCnt = c.getInt(c.getColumnIndex("mMplinkDisStallCnt"));
                statistics.mMplinkDisWifiGoodCnt = c.getInt(c.getColumnIndex("mMplinkDisWifiGoodCnt"));
                statistics.mMplinkEnFailCnt = c.getInt(c.getColumnIndex("mMplinkEnFailCnt"));
                statistics.mMplinkDisFailCnt = c.getInt(c.getColumnIndex("mMplinkDisFailCnt"));
                statistics.mMplinkEnTraf = c.getInt(c.getColumnIndex("mMplinkEnTraf"));
                statistics.mMplinkEnFailEnvironCnt = c.getInt(c.getColumnIndex("MplinkEnFailEnvironCnt"));
                statistics.mMplinkEnFailCoexistCnt = c.getInt(c.getColumnIndex("mMplinkEnFailCoexistCnt"));
                statistics.mMplinkEnFailPingPongCnt = c.getInt(c.getColumnIndex("mMplinkEnFailPingPongCnt"));
                statistics.mMplinkEnFailHistoryQoeCnt = c.getInt(c.getColumnIndex("mMplinkEnFailHistoryQoeCnt"));
                statistics.mMplinkEnFailChQoeCnt = c.getInt(c.getColumnIndex("mMplinkEnFailChQoeCnt"));
                statistics.mHicureEnCnt = c.getInt(c.getColumnIndex("mHicureEnCnt"));
                statistics.mHicureSucCnt = c.getInt(c.getColumnIndex("mHicureSucCnt"));
            }
        } catch (SQLException e) {
            HwHiStreamUtils.logE(false, "getAppStatistics error:%{public}s", e.getMessage());
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }
}
