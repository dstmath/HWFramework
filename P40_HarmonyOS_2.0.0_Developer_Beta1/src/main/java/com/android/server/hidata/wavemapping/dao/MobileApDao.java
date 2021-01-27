package com.android.server.hidata.wavemapping.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.entity.ApInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.util.ArrayList;
import java.util.List;

public class MobileApDao {
    private static final int DEFAULT_CAPACITY = 10;
    private static final String DELETE_PREFIX = "DELETE FROM ";
    private static final String JUDGE_SRC_TYPE_SUFFIX = " WHERE SRCTYPE = ? ";
    private static final String JUDGE_SSID_AND_MAC_SUFFIX = " WHERE SSID = ? AND MAC = ? ";
    private static final String KEY_MAC = "MAC";
    private static final String KEY_SRC_TYPE = "SRCTYPE";
    private static final String KEY_SSID = "SSID";
    private static final String KEY_UPDATE_TIME = "UPTIME";
    private static final String SELECT_ALL_AP_PREFIX = "SELECT SSID,MAC,UPTIME FROM ";
    private static final String SELECT_ALL_AP_SUFFIX = " WHERE 1 = 1";
    private static final String SELECT_ALL_COUNT_PREFIX = "SELECT count(1) FROM ";
    private static final String TAG = ("WMapping." + MobileApDao.class.getSimpleName());
    private static final String UPDATE_PREFIX = "UPDATE ";
    private static final String UPDATE_SUFFIX = " SET UPTIME = ? WHERE SSID = ? AND MAC = ? ";
    private SQLiteDatabase db = DatabaseSingleton.getInstance();

    public boolean insert(ApInfo apInfo) {
        if (apInfo == null || TextUtils.isEmpty(apInfo.getSsid()) || TextUtils.isEmpty(apInfo.getMac())) {
            return false;
        }
        if (findBySsid(apInfo.getSsid(), apInfo.getMac()) != null) {
            return true;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SSID, apInfo.getSsid());
        contentValues.put(KEY_MAC, apInfo.getMac());
        contentValues.put(KEY_UPDATE_TIME, apInfo.getUptime());
        contentValues.put(KEY_SRC_TYPE, Integer.valueOf(apInfo.getSrcType()));
        try {
            this.db.insert(Constant.MOBILE_AP_TABLE_NAME, null, contentValues);
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "insert failed by Exception", new Object[0]);
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0066, code lost:
        if (0 == 0) goto L_0x0069;
     */
    public List<ApInfo> findAllAps() {
        List<ApInfo> apInfos = new ArrayList<>(10);
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("SELECT SSID,MAC,UPTIME FROM MOBILE_AP WHERE 1 = 1", null);
            while (cursor.moveToNext()) {
                apInfos.add(new ApInfo(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SSID)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_MAC)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_UPDATE_TIME))));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.d(false, "SELECT SSID,MAC,UPTIME FROM MOBILE_AP WHERE 1 = 1", new Object[0]);
            LogUtil.e(false, "findAllAps IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "findAllAps failed by Exceptions", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return apInfos;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0046, code lost:
        if (0 == 0) goto L_0x0049;
     */
    public int findAllCountBySrcType(int srcType) {
        int cnt = 0;
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("SELECT count(1) FROM MOBILE_AP WHERE SRCTYPE = ? ", new String[]{String.valueOf(srcType)});
            while (cursor.moveToNext()) {
                cnt = cursor.getInt(0);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.d(false, "SELECT count(1) FROM MOBILE_AP WHERE SRCTYPE = ? ", new Object[0]);
            LogUtil.e(false, "findAllCountBySrcType IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "findAllCountBySrcType failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return cnt;
    }

    public boolean update(ApInfo apInfo) {
        if (apInfo == null) {
            LogUtil.d(false, "update failure, place == null", new Object[0]);
            return false;
        }
        Object[] args = {apInfo.getUptime(), apInfo.getSsid(), apInfo.getMac()};
        LogUtil.i(false, "update begin:%{public}s apInfo:%{private}s", "UPDATE MOBILE_AP SET UPTIME = ? WHERE SSID = ? AND MAC = ? ", apInfo.toString());
        try {
            this.db.execSQL("UPDATE MOBILE_AP SET UPTIME = ? WHERE SSID = ? AND MAC = ? ", args);
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "UPDATE exception: %{public}s", e.getMessage());
            return false;
        }
    }

    public boolean remove(String ssid, String mac) {
        if (ssid == null || "".equals(ssid) || mac == null || "".equals(mac)) {
            return false;
        }
        try {
            this.db.execSQL("DELETE FROM MOBILE_AP WHERE SSID = ? AND MAC = ? ", new Object[]{ssid, mac});
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "remove exception: %{public}s", e.getMessage());
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0076, code lost:
        if (0 == 0) goto L_0x0079;
     */
    public ApInfo findBySsid(String ssid, String mac) {
        ApInfo apInfo = null;
        if (ssid == null || "".equals(ssid)) {
            LogUtil.d(false, "findBySsid ssid=null or ssid=", new Object[0]);
            return null;
        } else if (mac == null || "".equals(mac)) {
            LogUtil.d(false, "findBySsid mac=null or mac=", new Object[0]);
            return null;
        } else {
            Cursor cursor = null;
            SQLiteDatabase sQLiteDatabase = this.db;
            if (sQLiteDatabase == null) {
                return null;
            }
            try {
                cursor = sQLiteDatabase.rawQuery("SELECT SSID,MAC,UPTIME FROM MOBILE_AP WHERE SSID = ? AND MAC = ? ", new String[]{ssid, mac});
                if (cursor.moveToNext()) {
                    apInfo = new ApInfo(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SSID)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_MAC)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_UPDATE_TIME)));
                }
            } catch (IllegalArgumentException e) {
                LogUtil.e(false, "findBySsid IllegalArgumentException: %{public}s", e.getMessage());
            } catch (SQLException e2) {
                LogUtil.e(false, "findBySsid failed by Exception", new Object[0]);
                if (0 != 0) {
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
            cursor.close();
            return apInfo;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0077, code lost:
        if (0 == 0) goto L_0x007a;
     */
    public ApInfo findBySsidForUpdateTime(String ssid, String bssid) {
        ApInfo apInfo = null;
        if (ssid == null || "".equals(ssid)) {
            LogUtil.d(false, "findBySsidForUpdateTime ssid=null or ssid =", new Object[0]);
            return null;
        } else if (bssid == null || "".equals(bssid)) {
            LogUtil.d(false, "findBySsidForUpdateTime bssid=null or bssid =", new Object[0]);
            return null;
        } else {
            Cursor cursor = null;
            SQLiteDatabase sQLiteDatabase = this.db;
            if (sQLiteDatabase == null) {
                return null;
            }
            try {
                cursor = sQLiteDatabase.rawQuery("SELECT SSID,MAC,UPTIME FROM MOBILE_AP WHERE SSID = ? AND MAC = ? ", new String[]{ssid, bssid});
                if (cursor.moveToNext()) {
                    apInfo = new ApInfo(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SSID)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_MAC)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_UPDATE_TIME)));
                }
            } catch (IllegalArgumentException e) {
                LogUtil.e(false, "findBySsidForUpdateTime IllegalArgumentException: %{public}s", e.getMessage());
            } catch (SQLException e2) {
                LogUtil.e(false, "findBySsidForUpdateTime failed by Exception", new Object[0]);
                if (0 != 0) {
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
            cursor.close();
            if (apInfo != null) {
                apInfo.setUptime(TimeUtil.getTime());
                if (!update(apInfo)) {
                    LogUtil.d(false, "findBySsidForUpdateTime update failure", new Object[0]);
                    LogUtil.i(false, "apinfo: %{private}s", apInfo.toString());
                }
            }
            return apInfo;
        }
    }
}
