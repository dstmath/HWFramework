package com.android.server.hidata.wavemapping.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.entity.RegularPlaceInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.util.HashMap;

public class RegularPlaceDao {
    private static final int DEFAULT_CAPACITY = 16;
    private static final String DELETE_PREFIX = "DELETE FROM ";
    private static final String JUDGE_LOCATION_SUFFIX = " WHERE 1 = 1 ";
    private static final String JUDGE_SUFFIX = " WHERE SSID = ? and ISMAINAP = ? ";
    private static final String KEY_BATCH = "BATCH";
    private static final String KEY_BEGIN_TIME = "BEGINTIME";
    private static final String KEY_DIST_NUM = "DISNUM";
    private static final String KEY_FINGER_NUM = "FINGERNUM";
    private static final String KEY_IDENTIFY_NUM = "IDENTIFYNUM";
    private static final String KEY_MAIN_AP = "ISMAINAP";
    private static final String KEY_MODEL_NAME = "MODELNAME";
    private static final String KEY_NO_OCCUR_SSID = "NO_OCURBSSIDS";
    private static final String KEY_SSID = "SSID";
    private static final String KEY_STATE = "STATE";
    private static final String KEY_TEST_DATA_NUM = "TEST_DAT_NUM";
    private static final String MAIN_AP_FLAG = "1";
    private static final String SELECT_ALL_PREFIX = "SELECT SSID,STATE,BATCH,FINGERNUM,TEST_DAT_NUM,DISNUM,IDENTIFYNUM,NO_OCURBSSIDS,ISMAINAP,MODELNAME,BEGINTIME FROM ";
    private static final String SELECT_LOCATION_PREFIX = "SELECT SSID,STATE,BATCH,FINGERNUM,TEST_DAT_NUM,DISNUM,IDENTIFYNUM,NO_OCURBSSIDS,ISMAINAP,MODELNAME FROM ";
    private static final String TAG = ("WMapping." + RegularPlaceDao.class.getSimpleName());
    private static final String UNDER_LINE = "_";
    private static final String UPDATE_PARAM = " SET STATE = ?,BATCH = ?,FINGERNUM= ? ,UPTIME = ?,TEST_DAT_NUM = ?,DISNUM = ?,IDENTIFYNUM = ?,NO_OCURBSSIDS = ?,MODELNAME = ? WHERE ISMAINAP = ? AND SSID = ? ";
    private static final String UPDATE_PREFIX = "UPDATE ";
    private SQLiteDatabase db = DatabaseSingleton.getInstance();

    public boolean insert(RegularPlaceInfo location) {
        if (findBySsid(location.getPlace(), location.isMainAp()) != null) {
            return true;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SSID, location.getPlace());
        contentValues.put(KEY_STATE, Integer.valueOf(location.getState()));
        contentValues.put(KEY_BATCH, Integer.valueOf(location.getBatch()));
        contentValues.put(KEY_FINGER_NUM, Integer.valueOf(location.getFingerNum()));
        contentValues.put(KEY_TEST_DATA_NUM, Integer.valueOf(location.getTestDataNum()));
        contentValues.put(KEY_DIST_NUM, Integer.valueOf(location.getDisNum()));
        contentValues.put(KEY_IDENTIFY_NUM, Integer.valueOf(location.getIdentifyNum()));
        contentValues.put(KEY_NO_OCCUR_SSID, location.getNoOcurBssids());
        contentValues.put(KEY_MODEL_NAME, Integer.valueOf(location.getModelName()));
        contentValues.put(KEY_BEGIN_TIME, Integer.valueOf(new TimeUtil().time2IntDate(TimeUtil.getTime())));
        contentValues.put(KEY_MAIN_AP, String.valueOf(location.isMainAp() ? 1 : 0));
        try {
            this.db.insert(Constant.REGULAR_PLACESTATE_TABLE_NAME, null, contentValues);
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "insert failed by Exception", new Object[0]);
            return false;
        }
    }

    public boolean update(RegularPlaceInfo location) {
        if (location == null) {
            LogUtil.d(false, "update failure, location == null", new Object[0]);
            return false;
        }
        Object[] args = {Integer.valueOf(location.getState()), Integer.valueOf(location.getBatch()), Integer.valueOf(location.getFingerNum()), TimeUtil.getTime(), Integer.valueOf(location.getTestDataNum()), Integer.valueOf(location.getDisNum()), Integer.valueOf(location.getIdentifyNum()), location.getNoOcurBssids(), Integer.valueOf(location.getModelName()), String.valueOf(location.isMainAp() ? 1 : 0), location.getPlace()};
        LogUtil.d(false, "update begin, sql=%{public}s", "UPDATE RGL_PLACESTATE SET STATE = ?,BATCH = ?,FINGERNUM= ? ,UPTIME = ?,TEST_DAT_NUM = ?,DISNUM = ?,IDENTIFYNUM = ?,NO_OCURBSSIDS = ?,MODELNAME = ? WHERE ISMAINAP = ? AND SSID = ? ");
        LogUtil.i(false, "            , location=%{private}s", location.toString());
        try {
            this.db.execSQL("UPDATE RGL_PLACESTATE SET STATE = ?,BATCH = ?,FINGERNUM= ? ,UPTIME = ?,TEST_DAT_NUM = ?,DISNUM = ?,IDENTIFYNUM = ?,NO_OCURBSSIDS = ?,MODELNAME = ? WHERE ISMAINAP = ? AND SSID = ? ", args);
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "UPDATE exception: %{public}s", e.getMessage());
            return false;
        }
    }

    public RegularPlaceInfo addRegularLocation(String place, boolean isMainAp) {
        RegularPlaceInfo regularPlaceInfo = null;
        if (place == null) {
            return null;
        }
        try {
            regularPlaceInfo = new RegularPlaceInfo(place, 3, 1, 0, 0, 0, 0, "", isMainAp);
            insert(regularPlaceInfo);
            return regularPlaceInfo;
        } catch (SQLException e) {
            LogUtil.e(false, "addRegularLocation failed by Exception", new Object[0]);
            return regularPlaceInfo;
        }
    }

    public boolean remove(String place, boolean isMainAp) {
        try {
            this.db.execSQL("DELETE FROM RGL_PLACESTATE WHERE SSID = ? and ISMAINAP = ? ", new Object[]{place, String.valueOf(isMainAp ? 1 : 0)});
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "remove exception: %{public}s", e.getMessage());
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00d9, code lost:
        if (0 == 0) goto L_0x00dc;
     */
    public RegularPlaceInfo findAllBySsid(String place, boolean isMainAp) {
        RegularPlaceInfo placeInfo = null;
        if (TextUtils.isEmpty(place)) {
            LogUtil.d(false, "findBySsid place=null or place=", new Object[0]);
            return null;
        }
        Cursor cursor = null;
        if (this.db == null) {
            return null;
        }
        try {
            cursor = this.db.rawQuery("SELECT SSID,STATE,BATCH,FINGERNUM,TEST_DAT_NUM,DISNUM,IDENTIFYNUM,NO_OCURBSSIDS,ISMAINAP,MODELNAME,BEGINTIME FROM RGL_PLACESTATE WHERE SSID = ? and ISMAINAP = ? ", new String[]{place, String.valueOf(isMainAp ? 1 : 0)});
            if (cursor.moveToNext()) {
                placeInfo = new RegularPlaceInfo(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SSID)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STATE)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BATCH)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_FINGER_NUM)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TEST_DATA_NUM)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DIST_NUM)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IDENTIFY_NUM)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_NO_OCCUR_SSID)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_MAIN_AP)).equals("1"));
                placeInfo.setModelName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MODEL_NAME)));
                placeInfo.setBeginTime(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BEGIN_TIME)));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "findBySsid IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "findAllBySsid failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return placeInfo;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00cc, code lost:
        if (0 == 0) goto L_0x00cf;
     */
    public RegularPlaceInfo findBySsid(String place, boolean isMainAp) {
        RegularPlaceInfo placeInfo = null;
        if (TextUtils.isEmpty(place)) {
            LogUtil.d(false, "findBySsid place=null or place=", new Object[0]);
            return null;
        }
        Cursor cursor = null;
        if (this.db == null) {
            return null;
        }
        try {
            cursor = this.db.rawQuery("SELECT SSID,STATE,BATCH,FINGERNUM,TEST_DAT_NUM,DISNUM,IDENTIFYNUM,NO_OCURBSSIDS,ISMAINAP,MODELNAME FROM RGL_PLACESTATE WHERE SSID = ? and ISMAINAP = ? ", new String[]{place, String.valueOf(isMainAp ? 1 : 0)});
            if (cursor.moveToNext()) {
                placeInfo = new RegularPlaceInfo(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SSID)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STATE)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BATCH)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_FINGER_NUM)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TEST_DATA_NUM)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DIST_NUM)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IDENTIFY_NUM)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_NO_OCCUR_SSID)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_MAIN_AP)).equals("1"));
                placeInfo.setModelName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MODEL_NAME)));
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
        return placeInfo;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x00e3, code lost:
        if (0 == 0) goto L_0x00e6;
     */
    public HashMap<String, RegularPlaceInfo> findAllLocations() {
        Cursor cursor = null;
        HashMap<String, RegularPlaceInfo> placeInfoHashMap = new HashMap<>(16);
        try {
            cursor = this.db.rawQuery("SELECT SSID,STATE,BATCH,FINGERNUM,TEST_DAT_NUM,DISNUM,IDENTIFYNUM,NO_OCURBSSIDS,ISMAINAP,MODELNAME FROM RGL_PLACESTATE WHERE 1 = 1 ", null);
            while (cursor.moveToNext()) {
                RegularPlaceInfo placeInfo = new RegularPlaceInfo(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SSID)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STATE)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BATCH)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_FINGER_NUM)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TEST_DATA_NUM)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DIST_NUM)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IDENTIFY_NUM)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_NO_OCCUR_SSID)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_MAIN_AP)).equals("1"));
                placeInfo.setModelName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MODEL_NAME)));
                LogUtil.d(false, " findAllLocations:place:%{private}s", placeInfo.toString());
                placeInfoHashMap.put(placeInfo.getPlace() + "_" + cursor.getString(cursor.getColumnIndexOrThrow(KEY_MAIN_AP)), placeInfo);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "findAllLocations IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "findAllLocations failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return placeInfoHashMap;
    }
}
