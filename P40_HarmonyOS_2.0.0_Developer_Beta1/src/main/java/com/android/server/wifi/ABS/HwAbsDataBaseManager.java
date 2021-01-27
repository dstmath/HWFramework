package com.android.server.wifi.ABS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HwAbsDataBaseManager {
    private static final int DATA_BASE_MAX_NUM = 500;
    private static final String TAG = "DataBaseManager";
    private static HwAbsDataBaseManager sHwAbsDataBaseManager = null;
    private SQLiteDatabase mDatabase;
    private HwAbsDataBaseHelper mHelper;
    private final Object mLock = new Object();

    private HwAbsDataBaseManager(Context context) {
        HwAbsUtils.logD(false, "HwAbsDataBaseManager()", new Object[0]);
        try {
            this.mHelper = new HwAbsDataBaseHelper(context);
            this.mDatabase = this.mHelper.getWritableDatabase();
        } catch (SQLiteCantOpenDatabaseException e) {
            this.mDatabase = null;
        }
    }

    public static HwAbsDataBaseManager getInstance(Context context) {
        if (sHwAbsDataBaseManager == null) {
            sHwAbsDataBaseManager = new HwAbsDataBaseManager(context);
        }
        return sHwAbsDataBaseManager;
    }

    public void closeDb() {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    HwAbsUtils.logD(false, "HwAbsDataBaseManager closeDb()", new Object[0]);
                    this.mDatabase.close();
                }
            }
        }
    }

    public void addOrUpdateApInfos(HwAbsApInfoData data) {
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (data != null) {
                    if (getApInfoByBssid(data.mBssid) == null) {
                        HwAbsUtils.logD(false, "addOrUpdateApInfos inlineAddApInfo", new Object[0]);
                        checkIfAllCaseNumSatisfy();
                        inlineAddApInfo(data);
                    } else {
                        HwAbsUtils.logD(false, "addOrUpdateApInfos", new Object[0]);
                        inlineUpdateApInfo(data);
                    }
                }
            }
        }
    }

    public List<HwAbsApInfoData> getApInfoBySsid(String ssid) {
        synchronized (this.mLock) {
            List<HwAbsApInfoData> lists = new ArrayList<>();
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                return lists;
            }
            Cursor c = null;
            try {
                Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM MIMOApInfoTable where ssid like ?", new String[]{ssid});
                while (c2.moveToNext()) {
                    lists.add(new HwAbsApInfoData(c2.getString(c2.getColumnIndex("bssid")), c2.getString(c2.getColumnIndex("ssid")), c2.getInt(c2.getColumnIndex("switch_mimo_type")), c2.getInt(c2.getColumnIndex("switch_siso_type")), c2.getInt(c2.getColumnIndex("auth_type")), c2.getInt(c2.getColumnIndex("in_black_list")), c2.getInt(c2.getColumnIndex("reassociate_times")), c2.getInt(c2.getColumnIndex("failed_times")), c2.getInt(c2.getColumnIndex("continuous_failure_times")), c2.getLong(c2.getColumnIndex("last_connect_time"))));
                }
                c2.close();
                return lists;
            } catch (SQLException e) {
                HwAbsUtils.logE(false, "getApInfoBySsid Exception happen!", new Object[0]);
                List<HwAbsApInfoData> emptyList = Collections.emptyList();
                if (0 != 0) {
                    c.close();
                }
                return emptyList;
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
        }
    }

    public HwAbsApInfoData getApInfoByBssid(String bssid) {
        synchronized (this.mLock) {
            HwAbsApInfoData data = null;
            if (bssid == null) {
                return null;
            }
            Cursor c = null;
            try {
                Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM MIMOApInfoTable where bssid like ?", new String[]{bssid});
                if (c2.moveToNext()) {
                    data = new HwAbsApInfoData(c2.getString(c2.getColumnIndex("bssid")), c2.getString(c2.getColumnIndex("ssid")), c2.getInt(c2.getColumnIndex("switch_mimo_type")), c2.getInt(c2.getColumnIndex("switch_siso_type")), c2.getInt(c2.getColumnIndex("auth_type")), c2.getInt(c2.getColumnIndex("in_black_list")), c2.getInt(c2.getColumnIndex("reassociate_times")), c2.getInt(c2.getColumnIndex("failed_times")), c2.getInt(c2.getColumnIndex("continuous_failure_times")), c2.getLong(c2.getColumnIndex("last_connect_time")));
                }
                c2.close();
                return data;
            } catch (SQLException e) {
                HwAbsUtils.logE(false, "getApInfoByBssid error.", new Object[0]);
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
    }

    public List<HwAbsApInfoData> getApInfoInBlackList() {
        synchronized (this.mLock) {
            List<HwAbsApInfoData> lists = new ArrayList<>();
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                return lists;
            }
            Cursor c = null;
            try {
                Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM MIMOApInfoTable where in_black_list like ?", new String[]{"1"});
                while (c2.moveToNext()) {
                    lists.add(new HwAbsApInfoData(c2.getString(c2.getColumnIndex("bssid")), c2.getString(c2.getColumnIndex("ssid")), c2.getInt(c2.getColumnIndex("switch_mimo_type")), c2.getInt(c2.getColumnIndex("switch_siso_type")), c2.getInt(c2.getColumnIndex("auth_type")), c2.getInt(c2.getColumnIndex("in_black_list")), c2.getInt(c2.getColumnIndex("reassociate_times")), c2.getInt(c2.getColumnIndex("failed_times")), c2.getInt(c2.getColumnIndex("continuous_failure_times")), c2.getLong(c2.getColumnIndex("last_connect_time"))));
                }
                c2.close();
                return lists;
            } catch (SQLException e) {
                HwAbsUtils.logE(false, "getApInfoByBssid error.", new Object[0]);
                if (0 != 0) {
                    c.close();
                }
                return lists;
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
        }
    }

    public void deleteApInfosByBssid(HwAbsApInfoData data) {
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (data != null) {
                    inlineDeleteApInfoByBssid(data.mBssid);
                }
            }
        }
    }

    public void deleteApInfosBySsid(HwAbsApInfoData data) {
        synchronized (this.mLock) {
            if (data != null) {
                inlineDeleteApInfoBySsid(data.mSsid);
            }
        }
    }

    private void inlineDeleteApInfoBySsid(String ssid) {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase != null && sQLiteDatabase.isOpen() && ssid != null) {
            this.mDatabase.delete(HwAbsDataBaseHelper.MIMO_AP_TABLE_NAME, "ssid like ?", new String[]{ssid});
        }
    }

    private void inlineDeleteApInfoByBssid(String bssid) {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase != null && sQLiteDatabase.isOpen() && bssid != null) {
            this.mDatabase.delete(HwAbsDataBaseHelper.MIMO_AP_TABLE_NAME, "bssid like ?", new String[]{bssid});
        }
    }

    private void inlineAddApInfo(HwAbsApInfoData data) {
        if (data.mBssid != null) {
            this.mDatabase.execSQL("INSERT INTO MIMOApInfoTable VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{data.mBssid, data.mSsid, Integer.valueOf(data.mSwitchMimoType), Integer.valueOf(data.mSwitchSisoType), Integer.valueOf(data.mAuthType), Integer.valueOf(data.mInBlackList), 0, Integer.valueOf(data.mReassociateTimes), Integer.valueOf(data.mFailedTimes), Integer.valueOf(data.mContinuousFailureTimes), Long.valueOf(data.mLastConnectTime), 0});
        }
    }

    private void inlineUpdateApInfo(HwAbsApInfoData data) {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase != null && sQLiteDatabase.isOpen() && data.mBssid != null) {
            HwAbsUtils.logD(false, "inlineUpdateApInfo ssid = %{public}s", StringUtilEx.safeDisplaySsid(data.mSsid));
            ContentValues values = new ContentValues();
            values.put("bssid", data.mBssid);
            values.put("ssid", data.mSsid);
            values.put("switch_mimo_type", Integer.valueOf(data.mSwitchMimoType));
            values.put("switch_siso_type", Integer.valueOf(data.mSwitchSisoType));
            values.put("auth_type", Integer.valueOf(data.mAuthType));
            values.put("in_black_list", Integer.valueOf(data.mInBlackList));
            values.put("in_vowifi_black_list", (Integer) 0);
            values.put("reassociate_times", Integer.valueOf(data.mReassociateTimes));
            values.put("failed_times", Integer.valueOf(data.mFailedTimes));
            values.put("continuous_failure_times", Integer.valueOf(data.mContinuousFailureTimes));
            values.put("last_connect_time", Long.valueOf(data.mLastConnectTime));
            this.mDatabase.update(HwAbsDataBaseHelper.MIMO_AP_TABLE_NAME, values, "bssid like ?", new String[]{data.mBssid});
        }
    }

    private void checkIfAllCaseNumSatisfy() {
        List<HwAbsApInfoData> lists = getAllApInfo();
        long lastConnectTime = 0;
        String bssid = null;
        boolean isDeleteRecord = false;
        HwAbsUtils.logD(false, "checkIfAllCaseNumSatisfy lists.size() = %{public}d", Integer.valueOf(lists.size()));
        if (lists.size() >= DATA_BASE_MAX_NUM) {
            isDeleteRecord = true;
            for (HwAbsApInfoData data : lists) {
                long currentConnectTime = data.mLastConnectTime;
                if (lastConnectTime == 0 || lastConnectTime > currentConnectTime) {
                    lastConnectTime = currentConnectTime;
                    bssid = data.mBssid;
                }
            }
        }
        if (isDeleteRecord) {
            synchronized (this.mLock) {
                inlineDeleteApInfoByBssid(bssid);
            }
        }
    }

    public List<HwAbsApInfoData> getAllApInfo() {
        synchronized (this.mLock) {
            Cursor c = null;
            List<HwAbsApInfoData> lists = new ArrayList<>();
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                return lists;
            }
            try {
                Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM MIMOApInfoTable", null);
                while (c2.moveToNext()) {
                    lists.add(new HwAbsApInfoData(c2.getString(c2.getColumnIndex("bssid")), c2.getString(c2.getColumnIndex("ssid")), c2.getInt(c2.getColumnIndex("switch_mimo_type")), c2.getInt(c2.getColumnIndex("switch_siso_type")), c2.getInt(c2.getColumnIndex("auth_type")), c2.getInt(c2.getColumnIndex("in_black_list")), c2.getInt(c2.getColumnIndex("reassociate_times")), c2.getInt(c2.getColumnIndex("failed_times")), c2.getInt(c2.getColumnIndex("continuous_failure_times")), c2.getLong(c2.getColumnIndex("last_connect_time"))));
                }
                c2.close();
                return lists;
            } catch (SQLException e) {
                HwAbsUtils.logE(false, "getAllApInfo Exception happen!", new Object[0]);
                List<HwAbsApInfoData> emptyList = Collections.emptyList();
                if (0 != 0) {
                    c.close();
                }
                return emptyList;
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
        }
    }

    public HwAbsChrStatistics getChrStatistics() {
        synchronized (this.mLock) {
            HwAbsChrStatistics statistics = null;
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                return null;
            }
            Cursor c = null;
            try {
                Cursor c2 = this.mDatabase.rawQuery("SELECT * FROM StatisticsTable", null);
                if (c2.moveToNext()) {
                    statistics = new HwAbsChrStatistics();
                    statistics.longConnectEvent = c2.getInt(c2.getColumnIndex("longConnectEvent"));
                    statistics.shortConnectEvent = c2.getInt(c2.getColumnIndex("short_connect_event"));
                    statistics.searchEvent = c2.getInt(c2.getColumnIndex("search_event"));
                    statistics.antennaPreemptedScreenOnEvent = c2.getInt(c2.getColumnIndex("antenna_preempted_screen_on_event"));
                    statistics.antennaPreemptedScreenOffEvent = c2.getInt(c2.getColumnIndex("antenna_preempted_screen_off_event"));
                    statistics.moMtCallEvent = c2.getInt(c2.getColumnIndex("mo_mt_call_event"));
                    statistics.sisoToMimoEvent = c2.getInt(c2.getColumnIndex("siso_to_mimo_event"));
                    statistics.pingPongTimes = c2.getInt(c2.getColumnIndex("ping_pong_times"));
                    statistics.maxPingPongTimes = c2.getInt(c2.getColumnIndex("max_ping_pong_times"));
                    statistics.sisoTime = (long) c2.getInt(c2.getColumnIndex("siso_time"));
                    statistics.mimoTime = (long) c2.getInt(c2.getColumnIndex("mimo_time"));
                    statistics.mimoScreenOnTime = (long) c2.getInt(c2.getColumnIndex("mimo_screen_on_time"));
                    statistics.sisoScreenOnTime = (long) c2.getInt(c2.getColumnIndex("siso_screen_on_time"));
                    statistics.lastUploadTime = c2.getLong(c2.getColumnIndex("last_upload_time"));
                    statistics.rssiL0 = c2.getInt(c2.getColumnIndex("rssiL0"));
                    statistics.rssiL1 = c2.getInt(c2.getColumnIndex("rssiL1"));
                    statistics.rssiL2 = c2.getInt(c2.getColumnIndex("rssiL2"));
                    statistics.rssiL3 = c2.getInt(c2.getColumnIndex("rssiL3"));
                    statistics.rssiL4 = c2.getInt(c2.getColumnIndex("rssiL4"));
                }
                c2.close();
                return statistics;
            } catch (SQLException e) {
                HwAbsUtils.logE(false, "getChrStatistics Exception hapen!", new Object[0]);
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
    }

    public void inlineAddChrInfo(HwAbsChrStatistics data) {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    this.mDatabase.execSQL("INSERT INTO StatisticsTable VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{Integer.valueOf(data.longConnectEvent), Integer.valueOf(data.shortConnectEvent), Integer.valueOf(data.searchEvent), Integer.valueOf(data.antennaPreemptedScreenOnEvent), Integer.valueOf(data.antennaPreemptedScreenOffEvent), Integer.valueOf(data.moMtCallEvent), Integer.valueOf(data.sisoToMimoEvent), Integer.valueOf(data.pingPongTimes), Integer.valueOf(data.maxPingPongTimes), Long.valueOf(data.mimoTime), Long.valueOf(data.sisoTime), Long.valueOf(data.mimoScreenOnTime), Long.valueOf(data.sisoScreenOnTime), Long.valueOf(data.lastUploadTime), Integer.valueOf(data.rssiL0), Integer.valueOf(data.rssiL1), Integer.valueOf(data.rssiL2), Integer.valueOf(data.rssiL3), Integer.valueOf(data.rssiL4), 0});
                }
            }
        }
    }

    public void inlineUpdateChrInfo(HwAbsChrStatistics data) {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    HwAbsUtils.logD(false, "inlineUpdateCHRInfo ", new Object[0]);
                    ContentValues values = new ContentValues();
                    values.put("long_connect_event", Integer.valueOf(data.longConnectEvent));
                    values.put("short_connect_event", Integer.valueOf(data.shortConnectEvent));
                    values.put("search_event", Integer.valueOf(data.searchEvent));
                    values.put("antenna_preempted_screen_on_event", Integer.valueOf(data.antennaPreemptedScreenOnEvent));
                    values.put("antenna_preempted_screen_off_event", Integer.valueOf(data.antennaPreemptedScreenOffEvent));
                    values.put("mo_mt_call_event", Integer.valueOf(data.moMtCallEvent));
                    values.put("siso_to_mimo_event", Integer.valueOf(data.sisoToMimoEvent));
                    values.put("ping_pong_times", Integer.valueOf(data.pingPongTimes));
                    values.put("max_ping_pong_times", Integer.valueOf(data.maxPingPongTimes));
                    values.put("mimo_time", Long.valueOf(data.mimoTime));
                    values.put("siso_time", Long.valueOf(data.sisoTime));
                    values.put("mimo_screen_on_time", Long.valueOf(data.mimoScreenOnTime));
                    values.put("siso_screen_on_time", Long.valueOf(data.sisoScreenOnTime));
                    values.put("last_upload_time", Long.valueOf(data.lastUploadTime));
                    values.put("rssiL0", Integer.valueOf(data.rssiL0));
                    values.put("rssiL1", Integer.valueOf(data.rssiL1));
                    values.put("rssiL2", Integer.valueOf(data.rssiL2));
                    values.put("rssiL3", Integer.valueOf(data.rssiL3));
                    values.put("rssiL4", Integer.valueOf(data.rssiL4));
                    this.mDatabase.update(HwAbsDataBaseHelper.STATISTICS_TABLE_NAME, values, "_id like ?", new String[]{"1"});
                }
            }
        }
    }
}
