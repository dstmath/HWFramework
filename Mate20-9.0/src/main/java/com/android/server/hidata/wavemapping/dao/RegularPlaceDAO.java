package com.android.server.hidata.wavemapping.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.entity.RegularPlaceInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.util.HashMap;

public class RegularPlaceDAO {
    private static final String TAG = ("WMapping." + RegularPlaceDAO.class.getSimpleName());
    private SQLiteDatabase db = DatabaseSingleton.getInstance();

    public boolean insert(RegularPlaceInfo location) {
        if (findBySsid(location.getPlace(), location.isMainAp()) != null) {
            return true;
        }
        ContentValues cValue = new ContentValues();
        cValue.put("SSID", location.getPlace());
        cValue.put("STATE", Integer.valueOf(location.getState()));
        cValue.put("BATCH", Integer.valueOf(location.getBatch()));
        cValue.put("FINGERNUM", Integer.valueOf(location.getFingerNum()));
        cValue.put("TEST_DAT_NUM", Integer.valueOf(location.getTestDataNum()));
        cValue.put("DISNUM", Integer.valueOf(location.getDisNum()));
        cValue.put("IDENTIFYNUM", Integer.valueOf(location.getIdentifyNum()));
        cValue.put("NO_OCURBSSIDS", location.getNoOcurBssids());
        cValue.put("MODELNAME", Integer.valueOf(location.getModelName()));
        cValue.put("BEGINTIME", Integer.valueOf(new TimeUtil().time2IntDate(TimeUtil.getTime())));
        cValue.put("ISMAINAP", String.valueOf(location.isMainAp() ? 1 : 0));
        try {
            this.db.insert(Constant.REGULAR_PLACESTATE_TABLE_NAME, null, cValue);
            return true;
        } catch (Exception e) {
            LogUtil.e("insert exception: " + e.getMessage());
            return false;
        }
    }

    public boolean update(RegularPlaceInfo location) {
        if (location == null) {
            LogUtil.d("update failure,null == location");
            return false;
        }
        Object[] args = {Integer.valueOf(location.getState()), Integer.valueOf(location.getBatch()), Integer.valueOf(location.getFingerNum()), TimeUtil.getTime(), Integer.valueOf(location.getTestDataNum()), Integer.valueOf(location.getDisNum()), Integer.valueOf(location.getIdentifyNum()), location.getNoOcurBssids(), Integer.valueOf(location.getModelName()), String.valueOf(location.isMainAp() ? 1 : 0), location.getPlace()};
        LogUtil.d("update begin, sql=" + "UPDATE RGL_PLACESTATE SET STATE = ?,BATCH = ?,FINGERNUM= ? ,UPTIME = ?,TEST_DAT_NUM = ?,DISNUM = ?,IDENTIFYNUM = ?,NO_OCURBSSIDS = ?,MODELNAME = ? WHERE ISMAINAP = ? AND SSID = ? ");
        LogUtil.i("            , location=" + location.toString());
        try {
            this.db.execSQL("UPDATE RGL_PLACESTATE SET STATE = ?,BATCH = ?,FINGERNUM= ? ,UPTIME = ?,TEST_DAT_NUM = ?,DISNUM = ?,IDENTIFYNUM = ?,NO_OCURBSSIDS = ?,MODELNAME = ? WHERE ISMAINAP = ? AND SSID = ? ", args);
            return true;
        } catch (SQLException e) {
            LogUtil.e("UPDATE exception: " + e.getMessage());
            return false;
        }
    }

    public RegularPlaceInfo addRegularLocation(String place, boolean isMainAp) {
        RegularPlaceInfo regularPlaceInfo = null;
        if (place == null) {
            return null;
        }
        try {
            RegularPlaceInfo regularPlaceInfo2 = new RegularPlaceInfo(place, 3, 1, 0, 0, 0, 0, "", isMainAp);
            regularPlaceInfo = regularPlaceInfo2;
            insert(regularPlaceInfo);
            return regularPlaceInfo;
        } catch (Exception e) {
            LogUtil.e("addRegularLocation,e" + e.getMessage());
            return regularPlaceInfo;
        }
    }

    public boolean remove(String place, boolean isMainAp) {
        try {
            this.db.execSQL("DELETE FROM RGL_PLACESTATE  WHERE SSID = ? and ISMAINAP = ? ", new Object[]{place, String.valueOf(isMainAp ? 1 : 0)});
            return true;
        } catch (SQLException e) {
            LogUtil.e("remove exception: " + e.getMessage());
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x00bb, code lost:
        if (r5 != null) goto L_0x00bd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00f8, code lost:
        if (r5 == null) goto L_0x00fb;
     */
    public RegularPlaceInfo findAllBySsid(String place, boolean isMainAp) {
        String str = place;
        RegularPlaceInfo placeInfo = null;
        if (str == null || str.equals("")) {
            boolean z = isMainAp;
            LogUtil.d("findBySsid place=null or place=");
            return null;
        }
        Cursor cursor = null;
        if (this.db == null) {
            return null;
        }
        try {
            cursor = this.db.rawQuery("SELECT SSID,STATE,BATCH,FINGERNUM,TEST_DAT_NUM,DISNUM,IDENTIFYNUM,NO_OCURBSSIDS,ISMAINAP,MODELNAME,BEGINTIME FROM RGL_PLACESTATE WHERE SSID = ? and ISMAINAP = ? ", new String[]{str, String.valueOf(isMainAp ? 1 : 0)});
            if (cursor.moveToNext()) {
                RegularPlaceInfo regularPlaceInfo = new RegularPlaceInfo(cursor.getString(cursor.getColumnIndexOrThrow("SSID")), cursor.getInt(cursor.getColumnIndexOrThrow("STATE")), cursor.getInt(cursor.getColumnIndexOrThrow("BATCH")), cursor.getInt(cursor.getColumnIndexOrThrow("FINGERNUM")), cursor.getInt(cursor.getColumnIndexOrThrow("TEST_DAT_NUM")), cursor.getInt(cursor.getColumnIndexOrThrow("DISNUM")), cursor.getInt(cursor.getColumnIndexOrThrow("IDENTIFYNUM")), cursor.getString(cursor.getColumnIndexOrThrow("NO_OCURBSSIDS")), cursor.getString(cursor.getColumnIndexOrThrow("ISMAINAP")).equals("1"));
                placeInfo = regularPlaceInfo;
                placeInfo.setModelName(cursor.getString(cursor.getColumnIndexOrThrow("MODELNAME")));
                placeInfo.setBeginTime(cursor.getInt(cursor.getColumnIndexOrThrow("BEGINTIME")));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("findBySsid IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("findBySsid Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            return placeInfo;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x00ae, code lost:
        if (r5 != null) goto L_0x00b0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00eb, code lost:
        if (r5 == null) goto L_0x00ee;
     */
    public RegularPlaceInfo findBySsid(String place, boolean isMainAp) {
        String str = place;
        RegularPlaceInfo placeInfo = null;
        if (str == null || str.equals("")) {
            boolean z = isMainAp;
            LogUtil.d("findBySsid place=null or place=");
            return null;
        }
        Cursor cursor = null;
        if (this.db == null) {
            return null;
        }
        try {
            cursor = this.db.rawQuery("SELECT SSID,STATE,BATCH,FINGERNUM,TEST_DAT_NUM,DISNUM,IDENTIFYNUM,NO_OCURBSSIDS,ISMAINAP,MODELNAME FROM RGL_PLACESTATE WHERE SSID = ? and ISMAINAP = ? ", new String[]{str, String.valueOf(isMainAp ? 1 : 0)});
            if (cursor.moveToNext()) {
                RegularPlaceInfo regularPlaceInfo = new RegularPlaceInfo(cursor.getString(cursor.getColumnIndexOrThrow("SSID")), cursor.getInt(cursor.getColumnIndexOrThrow("STATE")), cursor.getInt(cursor.getColumnIndexOrThrow("BATCH")), cursor.getInt(cursor.getColumnIndexOrThrow("FINGERNUM")), cursor.getInt(cursor.getColumnIndexOrThrow("TEST_DAT_NUM")), cursor.getInt(cursor.getColumnIndexOrThrow("DISNUM")), cursor.getInt(cursor.getColumnIndexOrThrow("IDENTIFYNUM")), cursor.getString(cursor.getColumnIndexOrThrow("NO_OCURBSSIDS")), cursor.getString(cursor.getColumnIndexOrThrow("ISMAINAP")).equals("1"));
                placeInfo = regularPlaceInfo;
                placeInfo.setModelName(cursor.getString(cursor.getColumnIndexOrThrow("MODELNAME")));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("findBySsid IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("findBySsid Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            return placeInfo;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x010a, code lost:
        if (r3 == null) goto L_0x010d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x00cd, code lost:
        if (r3 != null) goto L_0x00cf;
     */
    public HashMap<String, RegularPlaceInfo> findAllLocations() {
        Cursor cursor = null;
        HashMap<String, RegularPlaceInfo> placeInfoHashMap = new HashMap<>();
        try {
            cursor = this.db.rawQuery("SELECT SSID,STATE,BATCH,FINGERNUM,TEST_DAT_NUM,IDENTIFYNUM,DISNUM,NO_OCURBSSIDS,ISMAINAP,MODELNAME FROM RGL_PLACESTATE WHERE 1 = 1 ", null);
            while (cursor.moveToNext()) {
                RegularPlaceInfo regularPlaceInfo = new RegularPlaceInfo(cursor.getString(cursor.getColumnIndexOrThrow("SSID")), cursor.getInt(cursor.getColumnIndexOrThrow("STATE")), cursor.getInt(cursor.getColumnIndexOrThrow("BATCH")), cursor.getInt(cursor.getColumnIndexOrThrow("FINGERNUM")), cursor.getInt(cursor.getColumnIndexOrThrow("TEST_DAT_NUM")), cursor.getInt(cursor.getColumnIndexOrThrow("DISNUM")), cursor.getInt(cursor.getColumnIndexOrThrow("IDENTIFYNUM")), cursor.getString(cursor.getColumnIndexOrThrow("NO_OCURBSSIDS")), cursor.getString(cursor.getColumnIndexOrThrow("ISMAINAP")).equals("1"));
                RegularPlaceInfo placeInfo = regularPlaceInfo;
                placeInfo.setModelName(cursor.getString(cursor.getColumnIndexOrThrow("MODELNAME")));
                LogUtil.d(" findAllLocations:place:" + placeInfo.toString());
                placeInfoHashMap.put(placeInfo.getPlace() + "_" + cursor.getString(cursor.getColumnIndexOrThrow("ISMAINAP")), placeInfo);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("findAllLocations IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("findAllLocations Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            return placeInfoHashMap;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }
}
