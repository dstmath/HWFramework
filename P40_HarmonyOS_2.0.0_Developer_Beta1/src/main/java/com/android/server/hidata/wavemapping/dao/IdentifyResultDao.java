package com.android.server.hidata.wavemapping.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.wifi.HwHiLog;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.entity.IdentifyResult;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.util.ArrayList;
import java.util.List;

public class IdentifyResultDao {
    private static final int DEFAULT_CAPACITY = 10;
    private static final String DELECT_PREFIX = "DELETE FROM ";
    private static final String JUDGE_SUFFIX = " WHERE SSID = ? AND ISMAINAP = ?";
    private static final String KEY_ALL_COUNT = "CNT";
    private static final String KEY_MAIN_AP = "ISMAINAP";
    private static final String KEY_MODEL_NAME = "MODELNAME";
    private static final String KEY_PRE_LABEL = "PRELABLE";
    private static final String KEY_SERVE_MAC = "SERVERMAC";
    private static final String KEY_SSID = "SSID";
    private static final String KEY_UPDATE_TIME = "UPTIME";
    private static final String SELECT_ALL_COUNT_PREFIX = "SELECT count(1) as CNT FROM ";
    private static final String SELECT_ALL_RESULT_PREFIX = "SELECT PRELABLE,SSID,SERVERMAC,UPTIME,MODELNAME FROM ";
    private static final String SELECT_RESULT_BY_SSID_PREFIX = "SELECT SSID,PRELABLE,SERVERMAC,UPTIME,MODELNAME FROM ";
    private static final String TAG = ("WMapping." + IdentifyResultDao.class.getSimpleName());
    private SQLiteDatabase db = DatabaseSingleton.getInstance();

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0084, code lost:
        if (0 == 0) goto L_0x0087;
     */
    public List<IdentifyResult> findAll() {
        List<IdentifyResult> identifyResultList = new ArrayList<>(10);
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("SELECT PRELABLE,SSID,SERVERMAC,UPTIME,MODELNAME FROM IDENTIFY_RESULT", null);
            while (cursor.moveToNext()) {
                IdentifyResult tempIdentifyResult = new IdentifyResult();
                tempIdentifyResult.setPreLabel(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PRE_LABEL)));
                tempIdentifyResult.setSsid(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SSID)));
                tempIdentifyResult.setServeMac(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SERVE_MAC)));
                tempIdentifyResult.setModelName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MODEL_NAME)));
                identifyResultList.add(tempIdentifyResult);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.d(false, "SELECT PRELABLE,SSID,SERVERMAC,UPTIME,MODELNAME FROM IDENTIFY_RESULT", new Object[0]);
            HwHiLog.e(TAG, false, "findAll IllegalArgumentException: %{public}s", new Object[]{e.getMessage()});
        } catch (SQLException e2) {
            HwHiLog.e(TAG, false, "findAll failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return identifyResultList;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004d, code lost:
        if (0 == 0) goto L_0x0050;
     */
    public int findAllCount() {
        Cursor cursor = null;
        int allCnt = 0;
        try {
            cursor = this.db.rawQuery("SELECT count(1) as CNT FROM IDENTIFY_RESULT", null);
            while (cursor.moveToNext()) {
                allCnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ALL_COUNT));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.d(false, "%{public}s", "SELECT count(1) as CNT FROM IDENTIFY_RESULT");
            HwHiLog.e(TAG, false, "findAll IllegalArgumentException: %{public}s", new Object[]{e.getMessage()});
        } catch (SQLException e2) {
            HwHiLog.e(TAG, false, "findAllCount failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return allCnt;
    }

    public boolean remove(String ssid, boolean isMainAp) {
        try {
            this.db.execSQL("DELETE FROM IDENTIFY_RESULT WHERE SSID = ? AND ISMAINAP = ?", new Object[]{ssid, String.valueOf(isMainAp ? 1 : 0)});
            return true;
        } catch (SQLException e) {
            HwHiLog.e(TAG, false, "remove exception: %{public}s", new Object[]{e.getMessage()});
            return false;
        }
    }

    public boolean insert(IdentifyResult identifyResult, boolean isMainAp) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_SSID, identifyResult.getSsid());
            contentValues.put(KEY_PRE_LABEL, Integer.valueOf(identifyResult.getPreLabel()));
            contentValues.put(KEY_SERVE_MAC, identifyResult.getServeMac());
            contentValues.put(KEY_MODEL_NAME, identifyResult.getModelName());
            contentValues.put(KEY_UPDATE_TIME, TimeUtil.getTime());
            contentValues.put(KEY_MAIN_AP, String.valueOf(isMainAp ? 1 : 0));
            this.db.insert(Constant.IDENTIFY_RESULT_TABLE_NAME, null, contentValues);
            return true;
        } catch (SQLException e) {
            HwHiLog.e(TAG, false, "insert failed by Exception", new Object[0]);
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x009f, code lost:
        if (0 == 0) goto L_0x00a2;
     */
    public List<IdentifyResult> findBySsid(String ssid, boolean isMainAp) {
        List<IdentifyResult> identifyResultList = new ArrayList<>(10);
        if (ssid == null || "".equals(ssid)) {
            LogUtil.d(false, "findBySsid ssid=null or ssid=", new Object[0]);
            return identifyResultList;
        } else if (this.db == null) {
            return identifyResultList;
        } else {
            Cursor cursor = null;
            try {
                cursor = this.db.rawQuery("SELECT SSID,PRELABLE,SERVERMAC,UPTIME,MODELNAME FROM IDENTIFY_RESULT WHERE SSID = ? AND ISMAINAP = ?", new String[]{ssid, String.valueOf(isMainAp ? 1 : 0)});
                while (cursor.moveToNext()) {
                    IdentifyResult tempIdentifyResult = new IdentifyResult();
                    tempIdentifyResult.setPreLabel(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PRE_LABEL)));
                    tempIdentifyResult.setSsid(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SSID)));
                    tempIdentifyResult.setServeMac(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SERVE_MAC)));
                    tempIdentifyResult.setModelName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MODEL_NAME)));
                    identifyResultList.add(tempIdentifyResult);
                }
            } catch (IllegalArgumentException e) {
                HwHiLog.e(TAG, false, "findBySsid IllegalArgumentException: %{public}s", new Object[]{e.getMessage()});
            } catch (SQLException e2) {
                HwHiLog.e(TAG, false, "findBySsid failed by Exception", new Object[0]);
                if (0 != 0) {
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
            cursor.close();
            return identifyResultList;
        }
    }
}
