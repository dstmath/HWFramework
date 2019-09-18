package com.android.server.wifi.HwQoE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.android.server.wifi.HwQoE.HiDataTracfficInfo;
import com.android.server.wifi.HwWifiStateMachine;
import java.util.ArrayList;
import java.util.List;

public class HwQoEQualityManager {
    private static HwQoEQualityManager mHwQoEQualityManager = null;
    private SQLiteDatabase mDatabase;
    private HwQoEQualityDataBase mHelper;
    private Object mSQLLock = new Object();

    private HwQoEQualityManager(Context context) {
        this.mHelper = new HwQoEQualityDataBase(context);
        this.mDatabase = this.mHelper.getWritableDatabase();
    }

    public static HwQoEQualityManager getInstance(Context context) {
        if (mHwQoEQualityManager == null) {
            mHwQoEQualityManager = new HwQoEQualityManager(context);
        }
        return mHwQoEQualityManager;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0018, code lost:
        return;
     */
    public void closeDB() {
        synchronized (this.mSQLLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    this.mDatabase.close();
                }
            }
        }
    }

    private boolean updateAppQualityRcd(HwQoEQualityInfo dbr) {
        ContentValues values = new ContentValues();
        values.put(HwWifiStateMachine.BSSID_KEY, dbr.mBSSID);
        values.put("RSSI", Integer.valueOf(dbr.mRSSI));
        values.put("APPType", Integer.valueOf(dbr.mAPPType));
        values.put("Thoughtput", Long.valueOf(dbr.mThoughtput));
        try {
            if (this.mDatabase.update(HwQoEQualityDataBase.HW_QOE_QUALITY_NAME, values, "(BSSID like ?) and (RSSI = ?) and (APPType = ?)", new String[]{dbr.mBSSID, String.valueOf(dbr.mRSSI), String.valueOf(dbr.mAPPType)}) != 0) {
                return true;
            }
            HwQoEUtils.logE("updateAppQualityRcd update failed.");
            return false;
        } catch (SQLException e) {
            HwQoEUtils.logE("updateAppQualityRcd error:" + e);
            return false;
        }
    }

    private boolean insertAppQualityRcd(HwQoEQualityInfo dbr) {
        try {
            this.mDatabase.execSQL("INSERT INTO HwQoEQualityRecordTable VALUES(null,  ?, ?, ?, ?)", new Object[]{dbr.mBSSID, Integer.valueOf(dbr.mRSSI), Integer.valueOf(dbr.mAPPType), Long.valueOf(dbr.mThoughtput)});
            return true;
        } catch (SQLException e) {
            HwQoEUtils.logE("insertAppQualityRcd error:" + e);
            return false;
        }
    }

    private boolean checkHistoryRecordExist(HwQoEQualityInfo dbr) {
        boolean ret = false;
        Cursor c = null;
        try {
            Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM HwQoEQualityRecordTable where (BSSID like ?) and (RSSI = ?) and (APPType = ?)", new String[]{dbr.mBSSID, String.valueOf(dbr.mRSSI), String.valueOf(dbr.mAPPType)});
            if (c2.getCount() > 0) {
                ret = true;
            }
            if (c2 != null) {
                c2.close();
            }
            return ret;
        } catch (SQLException e) {
            HwQoEUtils.logE("checkHistoryRecordExist error:" + e);
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

    public boolean addOrUpdateAppQualityRcd(HwQoEQualityInfo dbr) {
        synchronized (this.mSQLLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (dbr != null) {
                    if (dbr.mBSSID == null) {
                        HwQoEUtils.logE("addOrUpdateAppQualityRcd null error.");
                        return false;
                    } else if (checkHistoryRecordExist(dbr)) {
                        boolean updateAppQualityRcd = updateAppQualityRcd(dbr);
                        return updateAppQualityRcd;
                    } else {
                        boolean insertAppQualityRcd = insertAppQualityRcd(dbr);
                        return insertAppQualityRcd;
                    }
                }
            }
            HwQoEUtils.logE("addOrUpdateAppQualityRcd error.");
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0068, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x006f, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0077, code lost:
        return null;
     */
    public HwQoEQualityInfo queryAppQualityRcd(String apBssid, int apRssi, int appType) {
        HwQoEUtils.logD("queryAppQualityRcd enter.");
        synchronized (this.mSQLLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (apBssid == null) {
                        HwQoEUtils.logE("queryAppQualityRcd null error.");
                        return null;
                    }
                    Cursor c = null;
                    try {
                        Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM HwQoEQualityRecordTable where (BSSID like ?) and (RSSI = ?) and (APPType = ?)", new String[]{apBssid, String.valueOf(apRssi), String.valueOf(appType)});
                        if (c2.getCount() > 0) {
                            if (c2.moveToNext()) {
                                HwQoEQualityInfo record = new HwQoEQualityInfo();
                                record.mBSSID = apBssid;
                                record.mAPPType = appType;
                                record.mRSSI = apRssi;
                                record.mThoughtput = c2.getLong(c2.getColumnIndex("Thoughtput"));
                                if (c2 != null) {
                                    c2.close();
                                }
                            } else if (c2 != null) {
                                c2.close();
                            }
                        } else if (c2 != null) {
                            c2.close();
                        }
                    } catch (SQLException e) {
                        try {
                            HwQoEUtils.logE("queryAppQualityRcd error:" + e);
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
            HwQoEUtils.logE("queryAppQualityRcd database error.");
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006e, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0076, code lost:
        return r0;
     */
    public List<HwQoEQualityInfo> getAppQualityAllRcd(String apBssid, int appType) {
        List<HwQoEQualityInfo> mRecordList = new ArrayList<>();
        synchronized (this.mSQLLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (apBssid == null) {
                        HwQoEUtils.logE("getAppQualityAllRcd null error.");
                        return mRecordList;
                    }
                    Cursor c = null;
                    try {
                        Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM HwQoEQualityRecordTable where (BSSID like ?) and (APPType = ?)", new String[]{apBssid, String.valueOf(appType)});
                        if (c2.getCount() > 0) {
                            while (c2.moveToNext()) {
                                HwQoEQualityInfo record = new HwQoEQualityInfo();
                                record.mBSSID = apBssid;
                                record.mAPPType = appType;
                                record.mRSSI = c2.getInt(c2.getColumnIndex("RSSI"));
                                record.mThoughtput = c2.getLong(c2.getColumnIndex("Thoughtput"));
                                mRecordList.add(record);
                            }
                            if (c2 != null) {
                                c2.close();
                            }
                        } else if (c2 != null) {
                            c2.close();
                        }
                    } catch (SQLException e) {
                        try {
                            HwQoEUtils.logE("getAppQualityAllRcd error:" + e);
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
            HwQoEUtils.logE("getAppQualityAllRcd database error.");
            return mRecordList;
        }
    }

    public boolean deleteAppQualityRcd(String apBssid) {
        synchronized (this.mSQLLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (apBssid == null) {
                        HwQoEUtils.logE("deleteAppQualityRcd null error.");
                        return false;
                    }
                    try {
                        this.mDatabase.delete(HwQoEQualityDataBase.HW_QOE_QUALITY_NAME, "BSSID like ?", new String[]{apBssid});
                        return true;
                    } catch (SQLException e) {
                        HwQoEUtils.logE("deleteAppQualityRcd error:" + e);
                        return false;
                    }
                }
            }
            HwQoEUtils.logE("deleteAppQualityRcd database error.");
            return false;
        }
    }

    public boolean addAPPTracfficData(HiDataTracfficInfo data) {
        if (this.mDatabase == null || !this.mDatabase.isOpen()) {
            HwQoEUtils.logE("addAPPTracfficData database error.");
            return false;
        }
        try {
            this.mDatabase.execSQL("INSERT INTO HwQoEWeChatRecordTable VALUES(null,  ?, ?, ?, ?, ?)", new Object[]{data.mIMSI, String.valueOf(data.mAPPType), String.valueOf(data.mThoughtput), String.valueOf(data.mDuration), String.valueOf(data.mTimestamp)});
            return true;
        } catch (SQLException e) {
            HwQoEUtils.logE("addAPPTracfficData error:" + e);
            return false;
        }
    }

    public boolean deleteAPPTracfficData(HiDataTracfficInfo data) {
        if (this.mDatabase == null || !this.mDatabase.isOpen()) {
            HwQoEUtils.logE("addAPPTracfficData database error.");
            return false;
        }
        try {
            this.mDatabase.delete(HwQoEQualityDataBase.HW_QOE_WECHAT_NAME, "(IMSI = ?) and (APPType = ?) and (Timestamp = ?)", new String[]{String.valueOf(data.mAPPType), String.valueOf(data.mTimestamp)});
            return true;
        } catch (SQLException e) {
            HwQoEUtils.logE("deleteAPPTracfficData error:" + e);
            return false;
        }
    }

    public List<HiDataTracfficInfo> queryTracfficData(String imsi, int appType, long startTime, long endTime) {
        List<HiDataTracfficInfo> mRecordList = new ArrayList<>();
        if (this.mDatabase == null || !this.mDatabase.isOpen()) {
            HwQoEUtils.logE("queryTracfficData database error.");
            return mRecordList;
        }
        Cursor c = null;
        try {
            Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM HwQoEWeChatRecordTable where (IMSI = ?) and (APPType = ?)", new String[]{imsi, String.valueOf(appType)});
            if (c2.getCount() > 0) {
                while (c2.moveToNext()) {
                    boolean isResult = false;
                    HiDataTracfficInfo record = new HiDataTracfficInfo();
                    record.mIMSI = imsi;
                    record.mAPPType = appType;
                    record.mThoughtput = c2.getLong(c2.getColumnIndex("Thoughtput"));
                    record.mDuration = c2.getLong(c2.getColumnIndex("Duration"));
                    record.mTimestamp = c2.getLong(c2.getColumnIndex("Timestamp"));
                    if (startTime == 0 || endTime == 0) {
                        if (startTime != 0) {
                            if (record.mTimestamp >= startTime) {
                                isResult = true;
                            }
                        } else if (endTime == 0) {
                            isResult = true;
                        } else if (record.mTimestamp <= endTime) {
                            isResult = true;
                        }
                    } else if (record.mTimestamp >= startTime && record.mTimestamp <= endTime) {
                        isResult = true;
                    }
                    if (isResult) {
                        mRecordList.add(record);
                    }
                }
                if (c2 != null) {
                    c2.close();
                }
                return mRecordList;
            }
            if (c2 != null) {
                c2.close();
            }
            return mRecordList;
        } catch (SQLException e) {
            HwQoEUtils.logE("queryTracfficData error:" + e);
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

    private boolean checkAPHistoryRecordExist(String ssid, int authType, int appType) {
        boolean ret = false;
        Cursor c = null;
        try {
            Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM HwQoEWeChatAPRecordTable where (SSID like ?) and (authType = ?) and (appType = ?)", new String[]{ssid, String.valueOf(authType), String.valueOf(appType)});
            if (c2.getCount() > 0) {
                ret = true;
            }
            if (c2 != null) {
                c2.close();
            }
            return ret;
        } catch (SQLException e) {
            HwQoEUtils.logE("checkAPHistoryRecordExist error:" + e);
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

    private boolean updateAPRcd(String ssid, int authType, int apType, int appType, int blackCount) {
        ContentValues values = new ContentValues();
        values.put("SSID", ssid);
        values.put("authType", Integer.valueOf(authType));
        values.put("apType", Integer.valueOf(apType));
        values.put("appType", Integer.valueOf(appType));
        values.put("blackCount", Integer.valueOf(blackCount));
        try {
            if (this.mDatabase.update(HwQoEQualityDataBase.HW_QOE_WECHAT_AP_NAME, values, "(SSID like ?) and (authType = ?) and (appType = ?)", new String[]{ssid, String.valueOf(authType), String.valueOf(appType)}) != 0) {
                return true;
            }
            HwQoEUtils.logE("updateAPRcd update failed.");
            return false;
        } catch (SQLException e) {
            HwQoEUtils.logE("updateAPRcd error:" + e);
            return false;
        }
    }

    private boolean insertAPRcd(String ssid, int authType, int apType, int appType, int blackCount) {
        try {
            this.mDatabase.execSQL("INSERT INTO HwQoEWeChatAPRecordTable VALUES(null,  ?, ?, ?, ?, ?)", new Object[]{ssid, Integer.valueOf(authType), Integer.valueOf(apType), Integer.valueOf(appType), Integer.valueOf(blackCount)});
            return true;
        } catch (SQLException e) {
            HwQoEUtils.logE("insertAPRcd error:" + e);
            return false;
        }
    }

    public boolean addOrUpdateAPRcd(HiDataTracfficInfo.HiDataApInfo info) {
        if (this.mDatabase == null || !this.mDatabase.isOpen()) {
            HwQoEUtils.logE("addOrUpdateAPRcd error.");
            return false;
        } else if (info.mSsid == null) {
            HwQoEUtils.logE("addOrUpdateAPRcd null error.");
            return false;
        } else if (checkAPHistoryRecordExist(info.mSsid, info.mAuthType, info.mAppType)) {
            return updateAPRcd(info.mSsid, info.mAuthType, info.mApType, info.mAppType, info.mBlackCount);
        } else {
            return insertAPRcd(info.mSsid, info.mAuthType, info.mApType, info.mAppType, info.mBlackCount);
        }
    }

    public HiDataTracfficInfo.HiDataApInfo queryAPUseType(String ssid, int authType, int appType) {
        HiDataTracfficInfo.HiDataApInfo hiDataApInfo = new HiDataTracfficInfo.HiDataApInfo();
        if (this.mDatabase == null || !this.mDatabase.isOpen()) {
            HwQoEUtils.logE("queryAPUseType database error.");
            return hiDataApInfo;
        }
        Cursor c = null;
        try {
            Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM HwQoEWeChatAPRecordTable where (SSID like ?) and (authType = ?) and (appType = ?)", new String[]{ssid, String.valueOf(authType), String.valueOf(appType)});
            if (c2.getCount() > 0) {
                while (c2.moveToNext()) {
                    hiDataApInfo.mSsid = ssid;
                    hiDataApInfo.mAuthType = authType;
                    hiDataApInfo.mAppType = appType;
                    hiDataApInfo.mApType = c2.getInt(c2.getColumnIndex("apType"));
                    hiDataApInfo.mBlackCount = c2.getInt(c2.getColumnIndex("blackCount"));
                }
            }
            if (c2 != null) {
                c2.close();
            }
            return hiDataApInfo;
        } catch (SQLException e) {
            HwQoEUtils.logE("queryAPUseType error:" + e);
            if (c != null) {
                c.close();
            }
            return hiDataApInfo;
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }
}
