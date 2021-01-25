package com.android.server.hidata.wavemapping.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.entity.ApInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnterpriseApDao {
    private static final int DEFAULT_CAPACITY_LIST = 10;
    private static final int DEFAULT_CAPACITY_SET = 16;
    private static final String DELETE_PREFIX = "DELETE FROM ";
    private static final String JUDGE_SSID_SUFFIX = " WHERE SSID = ? ";
    private static final String KEY_SSID = "SSID";
    private static final String KEY_UPDATE_TIME = "UPTIME";
    private static final String SELECT_COUNT_PREFIX = "SELECT count(1) FROM ";
    private static final String SELECT_SSID_MAC_UPTIME_PREFIX = "SELECT SSID,MAC,UPTIME FROM ";
    private static final String SELECT_SSID_PREFIX = "SELECT SSID FROM ";
    private static final String SELECT_SSID_UPTIME_PREFIX = "SELECT SSID,UPTIME FROM ";
    private static final String SELECT_SUFFIX = " WHERE 1 = 1";
    private static final String TAG = ("WMapping." + EnterpriseApDao.class.getSimpleName());
    private static final String UPDATE_PREFIX = "UPDATE ";
    private static final String UPDATE_SUFFIX = " SET UPTIME = ? WHERE SSID = ? ";
    private SQLiteDatabase db = DatabaseSingleton.getInstance();

    public boolean insert(ApInfo apInfo) {
        if (findBySsid(apInfo.getSsid()) != null) {
            return true;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SSID, apInfo.getSsid());
        contentValues.put(KEY_UPDATE_TIME, apInfo.getUptime());
        try {
            this.db.insert(Constant.ENTERPRISE_AP_TABLE_NAME, null, contentValues);
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "insert failed by Exception", new Object[0]);
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003f, code lost:
        if (0 == 0) goto L_0x0042;
     */
    public int findAllCount() {
        int cnt = 0;
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("SELECT count(1) FROM ENTERPRISE_AP WHERE 1 = 1", null);
            while (cursor.moveToNext()) {
                cnt = cursor.getInt(0);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.d(false, "SELECT count(1) FROM ENTERPRISE_AP WHERE 1 = 1", new Object[0]);
            LogUtil.e(false, "findAll IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "findAllCount failed by Exception", new Object[0]);
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

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004d, code lost:
        if (0 == 0) goto L_0x0050;
     */
    public Set<String> findAll() {
        Set<String> aps = new HashSet<>(16);
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("SELECT SSID FROM ENTERPRISE_AP WHERE 1 = 1", null);
            while (cursor.moveToNext()) {
                aps.add(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SSID)));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.d(false, "SELECT SSID FROM ENTERPRISE_AP WHERE 1 = 1", new Object[0]);
            LogUtil.e(false, "findAll IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "findAll failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return aps;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005c, code lost:
        if (0 == 0) goto L_0x005f;
     */
    public List<ApInfo> findAllAps() {
        List<ApInfo> apInfos = new ArrayList<>(10);
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("SELECT SSID,MAC,UPTIME FROM ENTERPRISE_AP WHERE 1 = 1", null);
            while (cursor.moveToNext()) {
                apInfos.add(new ApInfo(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SSID)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_UPDATE_TIME))));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.d(false, "SELECT SSID,MAC,UPTIME FROM ENTERPRISE_AP WHERE 1 = 1", new Object[0]);
            LogUtil.e(false, "findAllAps IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "findAllAps failed by Exception", new Object[0]);
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

    public boolean update(ApInfo apInfo) {
        if (apInfo == null) {
            LogUtil.d(false, "update failure,place == null", new Object[0]);
            return false;
        }
        try {
            this.db.execSQL("UPDATE ENTERPRISE_AP SET UPTIME = ? WHERE SSID = ? ", new Object[]{apInfo.getUptime(), apInfo.getSsid()});
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "UPDATE exception: %{public}s", e.getMessage());
            return false;
        }
    }

    public boolean remove(String ssid) {
        if (ssid == null || "".equals(ssid)) {
            return false;
        }
        try {
            this.db.execSQL("DELETE FROM ENTERPRISE_AP WHERE SSID = ? ", new Object[]{ssid});
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "remove exception: %{public}s", e.getMessage());
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005f, code lost:
        if (0 == 0) goto L_0x0062;
     */
    public ApInfo findBySsid(String ssid) {
        ApInfo apInfo = null;
        if (ssid == null || "".equals(ssid)) {
            LogUtil.d(false, "findBySsid ssid = null or ssid = ''", new Object[0]);
            return null;
        }
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return null;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT SSID,UPTIME FROM ENTERPRISE_AP WHERE SSID = ? ", new String[]{ssid});
            if (cursor.moveToNext()) {
                apInfo = new ApInfo(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SSID)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_UPDATE_TIME)));
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

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0066, code lost:
        if (0 == 0) goto L_0x0069;
     */
    public ApInfo findBySsidForUpdateTime(String ssid) {
        ApInfo apInfo = null;
        if (ssid == null || "".equals(ssid)) {
            LogUtil.d(false, "findBySsidForUpdateTime ssid=null or ssid=", new Object[0]);
            return null;
        }
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return null;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT SSID,UPTIME FROM ENTERPRISE_AP WHERE SSID = ? ", new String[]{ssid});
            if (cursor.moveToNext()) {
                apInfo = new ApInfo(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SSID)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_UPDATE_TIME)));
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
            cursor.close();
            return null;
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
    }
}
