package com.android.server.hidata.wavemapping.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.Arrays;

public class LocationDAO {
    private static final String TAG = ("WMapping." + LocationDAO.class.getSimpleName());
    private SQLiteDatabase db = DatabaseSingleton.getInstance();

    public boolean insert(String frequentlocation) {
        if (frequentlocation == null) {
            LogUtil.d("insert failed ,frequentlocation == null");
            return false;
        } else if (0 < getOOBTime()) {
            return updateFrequentLocation(frequentlocation);
        } else {
            ContentValues cValue = new ContentValues();
            long now = System.currentTimeMillis();
            cValue.put("UPDATETIME", Long.valueOf(now));
            cValue.put("OOBTIME", Long.valueOf(now));
            cValue.put("FREQUENTLOCATION", frequentlocation);
            try {
                this.db.beginTransaction();
                this.db.insert(Constant.LOCATION_TABLE_NAME, null, cValue);
                this.db.setTransactionSuccessful();
                this.db.endTransaction();
                return true;
            } catch (Exception e) {
                LogUtil.e("insert exception: " + e.getMessage());
                this.db.endTransaction();
                return false;
            } catch (Throwable th) {
                this.db.endTransaction();
                throw th;
            }
        }
    }

    public boolean insertOOBTime(long OOBTime) {
        if (0 < getOOBTime()) {
            return false;
        }
        ContentValues cValue = new ContentValues();
        cValue.put("OOBTIME", Long.valueOf(OOBTime));
        try {
            this.db.beginTransaction();
            this.db.insert(Constant.LOCATION_TABLE_NAME, null, cValue);
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
            return true;
        } catch (Exception e) {
            LogUtil.e("insert exception: " + e.getMessage());
            this.db.endTransaction();
            return false;
        } catch (Throwable th) {
            this.db.endTransaction();
            throw th;
        }
    }

    public boolean updateFrequentLocation(String frequentlocation) {
        if (frequentlocation == null) {
            LogUtil.d("update failure,frequentlocation == null");
            return false;
        }
        Object[] args = {Long.valueOf(System.currentTimeMillis()), frequentlocation};
        LogUtil.i("update begin: frequentlocation = " + frequentlocation);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE FREQUENT_LOCATION SET UPDATETIME = ?,FREQUENTLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("UPDATE exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean updateBenefitCHRTime(long now) {
        if (0 == getOOBTime()) {
            LogUtil.i("updateBenefitCHRTime: OOBTime = 0");
            return false;
        }
        Object[] args = {Long.valueOf(now)};
        LogUtil.i("update begin: CHRBENEFITUPLOADTIME = " + now);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE FREQUENT_LOCATION SET CHRBENEFITUPLOADTIME = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("UPDATE exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean updateSpaceUserCHRTime(long now) {
        if (0 == getOOBTime()) {
            LogUtil.i("updateSpaceUserCHRTime: OOBTime = 0");
            return false;
        }
        Object[] args = {Long.valueOf(now)};
        LogUtil.i("update begin: CHRSPACEUSERUPLOADTIME = " + now);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE FREQUENT_LOCATION SET CHRSPACEUSERUPLOADTIME = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("UPDATE exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean remove() {
        try {
            this.db.execSQL("DELETE FROM FREQUENT_LOCATION", null);
            return true;
        } catch (SQLException e) {
            LogUtil.e("remove exception: " + e.getMessage());
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005f, code lost:
        if (r1 == null) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0022, code lost:
        if (r1 != null) goto L_0x0024;
     */
    public String getFrequentLocation() {
        String frequentlocation = null;
        Cursor cursor = null;
        if (this.db == null) {
            return null;
        }
        try {
            cursor = this.db.rawQuery("SELECT * FROM FREQUENT_LOCATION", null);
            if (cursor.moveToNext()) {
                frequentlocation = cursor.getString(cursor.getColumnIndexOrThrow("FREQUENTLOCATION"));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("getFrequentLocation IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("getFrequentLocation Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            if (frequentlocation != null) {
                LogUtil.i("getFrequentLocation, frequentlocation:" + frequentlocation);
            } else {
                LogUtil.d("getFrequentLocation, NO DATA");
            }
            return frequentlocation;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0060, code lost:
        if (r2 == null) goto L_0x0063;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0023, code lost:
        if (r2 != null) goto L_0x0025;
     */
    public long getBenefitCHRTime() {
        long time = 0;
        Cursor cursor = null;
        if (this.db == null) {
            return 0;
        }
        try {
            cursor = this.db.rawQuery("SELECT * FROM FREQUENT_LOCATION", null);
            if (cursor.moveToNext()) {
                time = cursor.getLong(cursor.getColumnIndexOrThrow("CHRBENEFITUPLOADTIME"));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("getBenefitCHRTime IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("getBenefitCHRTime Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            LogUtil.i("getBenefitCHRTime, time:" + time);
            return time;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0060, code lost:
        if (r2 == null) goto L_0x0063;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0023, code lost:
        if (r2 != null) goto L_0x0025;
     */
    public long getSpaceUserCHRTime() {
        long time = 0;
        Cursor cursor = null;
        if (this.db == null) {
            return 0;
        }
        try {
            cursor = this.db.rawQuery("SELECT * FROM FREQUENT_LOCATION", null);
            if (cursor.moveToNext()) {
                time = cursor.getLong(cursor.getColumnIndexOrThrow("CHRSPACEUSERUPLOADTIME"));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("getSpaceUserCHRTime IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("getSpaceUserCHRTime Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            LogUtil.i("getSpaceUserCHRTime, time:" + time);
            return time;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0060, code lost:
        if (r2 == null) goto L_0x0063;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0023, code lost:
        if (r2 != null) goto L_0x0025;
     */
    public long getlastUpdateTime() {
        long time = 0;
        Cursor cursor = null;
        if (this.db == null) {
            return 0;
        }
        try {
            cursor = this.db.rawQuery("SELECT * FROM FREQUENT_LOCATION", null);
            if (cursor.moveToNext()) {
                time = cursor.getLong(cursor.getColumnIndexOrThrow("UPDATETIME"));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("getlastUpdateTime IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("getlastUpdateTime Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            LogUtil.i("getlastUpdateTime, time:" + time);
            return time;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0060, code lost:
        if (r2 == null) goto L_0x0063;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0023, code lost:
        if (r2 != null) goto L_0x0025;
     */
    public long getOOBTime() {
        long oob_time = 0;
        Cursor cursor = null;
        if (this.db == null) {
            return 0;
        }
        try {
            cursor = this.db.rawQuery("SELECT * FROM FREQUENT_LOCATION", null);
            if (cursor.moveToNext()) {
                oob_time = cursor.getLong(cursor.getColumnIndexOrThrow("OOBTIME"));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("getOOBTime IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("getOOBTime Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            LogUtil.d("getOOBTime, OOBTime:" + oob_time);
            return oob_time;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public boolean insertCHR(String freqlocation) {
        if (findCHRbyFreqLoc(freqlocation).containsKey("FREQLOCATION")) {
            return false;
        }
        ContentValues cValue = new ContentValues();
        cValue.put("FREQLOCATION", freqlocation);
        long oobtime = getOOBTime();
        long now = System.currentTimeMillis();
        int firstreport = Math.round(((float) (now - oobtime)) / 3600000.0f);
        cValue.put("FIRSTREPORT", Integer.valueOf(firstreport));
        LogUtil.d("insertCHR, OOBTime:" + oobtime + " now: " + now + " first report:" + firstreport);
        try {
            this.db.beginTransaction();
            this.db.insert(Constant.CHR_LOCATION_TABLE_NAME, null, cValue);
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
            return true;
        } catch (Exception e) {
            LogUtil.e("insertCHR exception: " + e.getMessage());
            this.db.endTransaction();
            return false;
        } catch (Throwable th) {
            this.db.endTransaction();
            throw th;
        }
    }

    public boolean accCHREnterybyFreqLoc(String location) {
        if (location == null) {
            LogUtil.d("accCHREnterybyFreqLoc failure,frequent location == null");
            return false;
        }
        insertCHR(location);
        Object[] args = {location};
        LogUtil.i("update begin: entry  " + location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET ENTERY = ENTERY + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("accCHREnterybyFreqLoc exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accCHRLeavebyFreqLoc(String location) {
        if (location == null) {
            LogUtil.d("accCHRLeavebyFreqLoc failure,frequent location == null");
            return false;
        }
        insertCHR(location);
        Object[] args = {location};
        LogUtil.i("update begin: leave  " + location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET LEAVE = LEAVE + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("accCHRLeavebyFreqLoc exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accCHRSpaceChangebyFreqLoc(String location) {
        if (location == null) {
            LogUtil.d("accCHRSpaceChangebyFreqLoc failure,frequent location == null");
            return false;
        }
        insertCHR(location);
        Object[] args = {location};
        LogUtil.i("update begin: SPACECHANGE  " + location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET SPACECHANGE = SPACECHANGE + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("accCHRSpaceChangebyFreqLoc exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accCHRSpaceLeavebyFreqLoc(String location) {
        if (location == null) {
            LogUtil.d("accCHRSpaceChangebyFreqLoc failure,frequent location == null");
            return false;
        }
        insertCHR(location);
        Object[] args = {location};
        LogUtil.i("update begin: SPACELEAVE  " + location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET SPACELEAVE = SPACELEAVE + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("accCHRSpaceLeavebyFreqLoc exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean addCHRDurationbyFreqLoc(int duration, String location) {
        if (location == null) {
            LogUtil.d("addCHRDurationbyFreqLoc failure,frequent location == null");
            return false;
        }
        insertCHR(location);
        Object[] args = {Integer.valueOf(duration), location};
        LogUtil.i("update begin: add duration  " + duration + " location:" + location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET DURATION = DURATION + ? WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("addCHRDurationbyFreqLoc exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accCHRUserPrefNoSwitchFailbyFreqLoc(String location) {
        if (location == null) {
            LogUtil.d("accCHRUserPrefNoSwitchFailbyFreqLoc failure,frequent location == null");
            return false;
        }
        insertCHR(location);
        Object[] args = {location};
        LogUtil.i("update begin: UPNOSWITCHFAIL at " + location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPNOSWITCHFAIL = UPNOSWITCHFAIL + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("accCHRUserPrefNoSwitchFailbyFreqLoc exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accCHRUserPrefAutoFailbyFreqLoc(String location) {
        if (location == null) {
            LogUtil.d("accCHRUserPrefAutoFailbyFreqLoc failure,frequent location == null");
            return false;
        }
        insertCHR(location);
        Object[] args = {location};
        LogUtil.i("update begin: UPAUTOFAIL at " + location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPAUTOFAIL = UPAUTOFAIL + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("accCHRUserPrefAutoFailbyFreqLoc exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accCHRUserPrefAutoSuccbyFreqLoc(String location) {
        if (location == null) {
            LogUtil.d("accCHRUserPrefAutoSuccbyFreqLoc failure,frequent location == null");
            return false;
        }
        insertCHR(location);
        Object[] args = {location};
        LogUtil.i("update begin: UPAUTOSUCC at " + location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPAUTOSUCC = UPAUTOSUCC + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("accCHRUserPrefAutoSuccbyFreqLoc exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accCHRUserPrefManualSuccbyFreqLoc(String location) {
        if (location == null) {
            LogUtil.d("accCHRUserPrefManualSuccbyFreqLoc failure,frequent location == null");
            return false;
        }
        insertCHR(location);
        Object[] args = {location};
        LogUtil.i("update begin: UPMANUALSUCC at " + location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPMANUALSUCC = UPMANUALSUCC + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("accCHRUserPrefManualSuccbyFreqLoc exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accCHRUserPrefTotalSwitchbyFreqLoc(String location) {
        if (location == null) {
            LogUtil.d("accCHRUserPrefTotalSwitchbyFreqLoc failure,frequent location == null");
            return false;
        }
        insertCHR(location);
        Object[] args = {location};
        LogUtil.i("update begin: UPTOTALSWITCH at " + location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPTOTALSWITCH = UPTOTALSWITCH + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("accCHRUserPrefTotalSwitchbyFreqLoc exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accCHRUserPrefQueryCntbyFreqLoc(String location) {
        if (location == null) {
            LogUtil.d("accCHRUserPrefQueryCntbyFreqLoc failure,frequent location == null");
            return false;
        }
        insertCHR(location);
        Object[] args = {location};
        LogUtil.i("update begin: UPQRYCNT at " + location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPQRYCNT = UPQRYCNT + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("accCHRUserPrefQueryCntbyFreqLoc exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accCHRUserPrefResCntbyFreqLoc(String location) {
        if (location == null) {
            LogUtil.d("accCHRUserPrefResCntbyFreqLoc failure,frequent location == null");
            return false;
        }
        insertCHR(location);
        Object[] args = {location};
        LogUtil.i("update begin: UPRESCNT at " + location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPRESCNT = UPRESCNT + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("accCHRUserPrefResCntbyFreqLoc exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accCHRUserPrefUnknownDBbyFreqLoc(String location) {
        if (location == null) {
            LogUtil.d("accCHRUserPrefUnknownDBbyFreqLoc failure,frequent location == null");
            return false;
        }
        insertCHR(location);
        Object[] args = {location};
        LogUtil.i("update begin: UPUNKNOWNDB at " + location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPUNKNOWNDB = UPUNKNOWNDB + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("accCHRUserPrefUnknownDBbyFreqLoc exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accCHRUserPrefUnknownSpacebyFreqLoc(String location) {
        if (location == null) {
            LogUtil.d("accCHRUserPrefUnknownSpacebyFreqLoc failure,frequent location == null");
            return false;
        }
        insertCHR(location);
        Object[] args = {location};
        LogUtil.i("update begin: UPUNKNOWNSPACE at " + location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPUNKNOWNSPACE = UPUNKNOWNSPACE + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("accCHRUserPrefUnknownSpacebyFreqLoc exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean resetCHRbyFreqLoc(String location) {
        if (location == null) {
            LogUtil.d("resetCHRbyFreqLoc failure,frequent location == null");
            return false;
        } else if (!findCHRbyFreqLoc(location).containsKey("FREQLOCATION")) {
            return false;
        } else {
            Object[] args = {location};
            LogUtil.i("update begin: RESET CHR at " + location);
            try {
                this.db.beginTransaction();
                this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET ENTERY = 0,LEAVE = 0,DURATION = 0,SPACECHANGE = 0,SPACELEAVE = 0,UPTOTALSWITCH = 0,UPAUTOSUCC = 0,UPMANUALSUCC = 0,UPAUTOFAIL = 0,UPNOSWITCHFAIL = 0,UPQRYCNT = 0,UPRESCNT = 0,UPUNKNOWNDB = 0,UPUNKNOWNSPACE = 0,LPTOTALSWITCH = 0,LPDATARX = 0,LPDATATX = 0,LPDURATION = 0,LPOFFSET = 0,LPALREADYBEST = 0,LPNOTREACH = 0,LPBACK = 0,LPUNKNOWNDB = 0,LPUNKNOWNSPACE = 0 WHERE FREQLOCATION = ?", args);
                this.db.setTransactionSuccessful();
                return true;
            } catch (SQLException e) {
                LogUtil.e("resetCHRbyFreqLoc exception: " + e.getMessage());
                return false;
            } finally {
                this.db.endTransaction();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0414, code lost:
        if (r4 == null) goto L_0x043c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0416, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0439, code lost:
        if (r4 == null) goto L_0x043c;
     */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0440  */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:78:0x0421=Splitter:B:78:0x0421, B:72:0x03fc=Splitter:B:72:0x03fc} */
    public Bundle findCHRbyFreqLoc(String freqlocation) {
        Bundle results;
        Bundle results2;
        Cursor cursor;
        int firstreport;
        int entery;
        int leave;
        int duration;
        int spacechange;
        int spaceleave;
        int uptotalswitch;
        int upautosucc;
        int upmanualsucc;
        int upautofail;
        int upnoswitchfail;
        String sql;
        int upqrycnt;
        String[] args;
        int uprescnt;
        int upunknowndb;
        int upunknownspace;
        int lptotalswitch;
        int lpdatarx;
        int lpdatatx;
        int lpduration;
        int lpoffset;
        int lpalreadybest;
        int lpnotreach;
        int lpback;
        int lpunknowndb;
        int lpunknownspace;
        Cursor cursor2;
        String str = freqlocation;
        Bundle results3 = new Bundle();
        Cursor cursor3 = null;
        if (this.db == null) {
            results = results3;
        } else if (str == null) {
            results = results3;
        } else {
            String sql2 = "SELECT * FROM CHR_FREQUENT_LOCATION WHERE FREQLOCATION = ?";
            String[] args2 = {str};
            LogUtil.i("findCHRbyFreqLocation: sql=" + sql2 + " ,args=" + Arrays.toString(args2));
            try {
                cursor3 = this.db.rawQuery(sql2, args2);
                while (cursor3.moveToNext()) {
                    try {
                        try {
                            firstreport = cursor3.getInt(cursor3.getColumnIndexOrThrow("FIRSTREPORT"));
                            entery = cursor3.getInt(cursor3.getColumnIndexOrThrow("ENTERY"));
                            leave = cursor3.getInt(cursor3.getColumnIndexOrThrow("LEAVE"));
                            duration = cursor3.getInt(cursor3.getColumnIndexOrThrow(Constant.USERDB_APP_NAME_DURATION));
                            spacechange = cursor3.getInt(cursor3.getColumnIndexOrThrow("SPACECHANGE"));
                            spaceleave = cursor3.getInt(cursor3.getColumnIndexOrThrow("SPACELEAVE"));
                            uptotalswitch = cursor3.getInt(cursor3.getColumnIndexOrThrow("UPTOTALSWITCH"));
                            upautosucc = cursor3.getInt(cursor3.getColumnIndexOrThrow("UPAUTOSUCC"));
                            upmanualsucc = cursor3.getInt(cursor3.getColumnIndexOrThrow("UPMANUALSUCC"));
                            upautofail = cursor3.getInt(cursor3.getColumnIndexOrThrow("UPAUTOFAIL"));
                            upnoswitchfail = cursor3.getInt(cursor3.getColumnIndexOrThrow("UPNOSWITCHFAIL"));
                            sql = sql2;
                        } catch (IllegalArgumentException e) {
                            e = e;
                            results2 = results3;
                            Cursor cursor4 = cursor3;
                            String str2 = sql2;
                            String[] strArr = args2;
                            LogUtil.e("findCHRbyFreqLocation IllegalArgumentException: " + e.getMessage());
                        } catch (Exception e2) {
                            e = e2;
                            results2 = results3;
                            Cursor cursor5 = cursor3;
                            String str3 = sql2;
                            String[] strArr2 = args2;
                            try {
                                LogUtil.e("findCHRbyFreqLocation Exception: " + e.getMessage());
                            } catch (Throwable th) {
                                th = th;
                                if (cursor3 != null) {
                                }
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            Bundle bundle = results3;
                            Cursor cursor6 = cursor3;
                            String str4 = sql2;
                            String[] strArr3 = args2;
                            if (cursor3 != null) {
                            }
                            throw th;
                        }
                        try {
                            upqrycnt = cursor3.getInt(cursor3.getColumnIndexOrThrow("UPQRYCNT"));
                            args = args2;
                        } catch (IllegalArgumentException e3) {
                            e = e3;
                            results2 = results3;
                            Cursor cursor7 = cursor3;
                            String[] strArr4 = args2;
                            LogUtil.e("findCHRbyFreqLocation IllegalArgumentException: " + e.getMessage());
                        } catch (Exception e4) {
                            e = e4;
                            results2 = results3;
                            Cursor cursor8 = cursor3;
                            String[] strArr5 = args2;
                            LogUtil.e("findCHRbyFreqLocation Exception: " + e.getMessage());
                        } catch (Throwable th3) {
                            th = th3;
                            Bundle bundle2 = results3;
                            Cursor cursor9 = cursor3;
                            String[] strArr6 = args2;
                            if (cursor3 != null) {
                            }
                            throw th;
                        }
                        try {
                            uprescnt = cursor3.getInt(cursor3.getColumnIndexOrThrow("UPRESCNT"));
                            upunknowndb = cursor3.getInt(cursor3.getColumnIndexOrThrow("UPUNKNOWNDB"));
                            upunknownspace = cursor3.getInt(cursor3.getColumnIndexOrThrow("UPUNKNOWNSPACE"));
                            lptotalswitch = cursor3.getInt(cursor3.getColumnIndexOrThrow("LPTOTALSWITCH"));
                            lpdatarx = cursor3.getInt(cursor3.getColumnIndexOrThrow("LPDATARX"));
                            lpdatatx = cursor3.getInt(cursor3.getColumnIndexOrThrow("LPDATATX"));
                            lpduration = cursor3.getInt(cursor3.getColumnIndexOrThrow("LPDURATION"));
                            lpoffset = cursor3.getInt(cursor3.getColumnIndexOrThrow("LPOFFSET"));
                            lpalreadybest = cursor3.getInt(cursor3.getColumnIndexOrThrow("LPALREADYBEST"));
                            lpnotreach = cursor3.getInt(cursor3.getColumnIndexOrThrow("LPNOTREACH"));
                            lpback = cursor3.getInt(cursor3.getColumnIndexOrThrow("LPBACK"));
                            lpunknowndb = cursor3.getInt(cursor3.getColumnIndexOrThrow("LPUNKNOWNDB"));
                            lpunknownspace = cursor3.getInt(cursor3.getColumnIndexOrThrow("LPUNKNOWNSPACE"));
                            cursor2 = cursor3;
                        } catch (IllegalArgumentException e5) {
                            e = e5;
                            results2 = results3;
                            Cursor cursor10 = cursor3;
                            LogUtil.e("findCHRbyFreqLocation IllegalArgumentException: " + e.getMessage());
                        } catch (Exception e6) {
                            e = e6;
                            results2 = results3;
                            Cursor cursor11 = cursor3;
                            LogUtil.e("findCHRbyFreqLocation Exception: " + e.getMessage());
                        } catch (Throwable th4) {
                            th = th4;
                            Bundle bundle3 = results3;
                            Cursor cursor12 = cursor3;
                            if (cursor3 != null) {
                            }
                            throw th;
                        }
                    } catch (IllegalArgumentException e7) {
                        e = e7;
                        results2 = results3;
                        Cursor cursor13 = cursor3;
                        String str5 = sql2;
                        String[] strArr7 = args2;
                        LogUtil.e("findCHRbyFreqLocation IllegalArgumentException: " + e.getMessage());
                    } catch (Exception e8) {
                        e = e8;
                        results2 = results3;
                        Cursor cursor14 = cursor3;
                        String str6 = sql2;
                        String[] strArr8 = args2;
                        LogUtil.e("findCHRbyFreqLocation Exception: " + e.getMessage());
                    } catch (Throwable th5) {
                        th = th5;
                        Bundle bundle4 = results3;
                        Cursor cursor15 = cursor3;
                        String str7 = sql2;
                        String[] strArr9 = args2;
                        if (cursor3 != null) {
                        }
                        throw th;
                    }
                    try {
                        results3.putString("FREQLOCATION", str);
                        results3.putInt("FIRSTREPORT", firstreport);
                        results3.putInt("ENTERY", entery);
                        results3.putInt("LEAVE", leave);
                        results3.putInt(Constant.USERDB_APP_NAME_DURATION, duration);
                        results3.putInt("SPACECHANGE", spacechange);
                        results3.putInt("SPACELEAVE", spaceleave);
                        results3.putInt("UPTOTALSWITCH", uptotalswitch);
                        results3.putInt("UPAUTOSUCC", upautosucc);
                        results3.putInt("UPMANUALSUCC", upmanualsucc);
                        results3.putInt("UPAUTOFAIL", upautofail);
                        results3.putInt("UPNOSWITCHFAIL", upnoswitchfail);
                        results3.putInt("UPQRYCNT", upqrycnt);
                        int upqrycnt2 = upqrycnt;
                        int uprescnt2 = uprescnt;
                        results3.putInt("UPRESCNT", uprescnt2);
                        int uprescnt3 = uprescnt2;
                        int uprescnt4 = upunknowndb;
                        results3.putInt("UPUNKNOWNDB", uprescnt4);
                        int upunknowndb2 = uprescnt4;
                        int upunknownspace2 = upunknownspace;
                        results3.putInt("UPUNKNOWNSPACE", upunknownspace2);
                        int upunknownspace3 = upunknownspace2;
                        int upunknownspace4 = lptotalswitch;
                        results3.putInt("LPTOTALSWITCH", upunknownspace4);
                        int lptotalswitch2 = upunknownspace4;
                        int lpdatarx2 = lpdatarx;
                        results3.putInt("LPDATARX", lpdatarx2);
                        int lpdatarx3 = lpdatarx2;
                        int lpdatatx2 = lpdatatx;
                        results3.putInt("LPDATATX", lpdatatx2);
                        int lpdatatx3 = lpdatatx2;
                        int lpduration2 = lpduration;
                        results3.putInt("LPDURATION", lpduration2);
                        int lpduration3 = lpduration2;
                        int lpduration4 = lpoffset;
                        results3.putInt("LPOFFSET", lpduration4);
                        int lpoffset2 = lpduration4;
                        int lpalreadybest2 = lpalreadybest;
                        results3.putInt("LPALREADYBEST", lpalreadybest2);
                        int lpalreadybest3 = lpalreadybest2;
                        int lpalreadybest4 = lpnotreach;
                        results3.putInt("LPNOTREACH", lpalreadybest4);
                        int lpnotreach2 = lpalreadybest4;
                        int lpback2 = lpback;
                        results3.putInt("LPBACK", lpback2);
                        int lpback3 = lpback2;
                        int lpunknowndb2 = lpunknowndb;
                        results3.putInt("LPUNKNOWNDB", lpunknowndb2);
                        results3.putInt("LPUNKNOWNSPACE", lpunknownspace);
                        StringBuilder sb = new StringBuilder();
                        results2 = results3;
                        try {
                            sb.append(" freqlocation:");
                            sb.append(str);
                            sb.append(",first report:");
                            sb.append(firstreport);
                            sb.append(",entery:");
                            sb.append(entery);
                            sb.append(",leave:");
                            sb.append(leave);
                            sb.append(",duration:");
                            sb.append(duration);
                            sb.append(",space change:");
                            sb.append(spacechange);
                            sb.append(",space leave:");
                            sb.append(spaceleave);
                            LogUtil.i(sb.toString());
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append(" uptotalswitch:");
                            sb2.append(uptotalswitch);
                            sb2.append(",upautosucc:");
                            sb2.append(upautosucc);
                            sb2.append(",upmanualsucc:");
                            sb2.append(upmanualsucc);
                            sb2.append(",upautofail:");
                            sb2.append(upautofail);
                            sb2.append(",upnoswitchfail:");
                            sb2.append(upnoswitchfail);
                            sb2.append(",upqrycnt:");
                            sb2.append(upqrycnt2);
                            int i = firstreport;
                            sb2.append(",uprescnt:");
                            int uprescnt5 = uprescnt3;
                            sb2.append(uprescnt5);
                            int i2 = uprescnt5;
                            sb2.append(" upunknowndb:");
                            int upunknowndb3 = upunknowndb2;
                            sb2.append(upunknowndb3);
                            int i3 = upunknowndb3;
                            sb2.append(" upunknownspace:");
                            int upunknownspace5 = upunknownspace3;
                            sb2.append(upunknownspace5);
                            LogUtil.i(sb2.toString());
                            StringBuilder sb3 = new StringBuilder();
                            int i4 = upunknownspace5;
                            sb3.append(" lptotalswitch:");
                            int lptotalswitch3 = lptotalswitch2;
                            sb3.append(lptotalswitch3);
                            int i5 = lptotalswitch3;
                            sb3.append(",lpdatarx:");
                            int lpdatarx4 = lpdatarx3;
                            sb3.append(lpdatarx4);
                            int i6 = lpdatarx4;
                            sb3.append(",lpdatatx:");
                            int lpdatatx4 = lpdatatx3;
                            sb3.append(lpdatatx4);
                            int i7 = lpdatatx4;
                            sb3.append(",lpduration:");
                            int lpduration5 = lpduration3;
                            sb3.append(lpduration5);
                            int i8 = lpduration5;
                            sb3.append(",lpoffset:");
                            int lpoffset3 = lpoffset2;
                            sb3.append(lpoffset3);
                            int i9 = lpoffset3;
                            sb3.append(",lpalreadybest:");
                            int lpalreadybest5 = lpalreadybest3;
                            sb3.append(lpalreadybest5);
                            int i10 = lpalreadybest5;
                            sb3.append(",lpnotreach:");
                            int lpnotreach3 = lpnotreach2;
                            sb3.append(lpnotreach3);
                            int i11 = lpnotreach3;
                            sb3.append(" lpback:");
                            int lpback4 = lpback3;
                            sb3.append(lpback4);
                            int i12 = lpback4;
                            sb3.append(" lpunknowndb:");
                            sb3.append(lpunknowndb2);
                            sb3.append(" lpunknownspace:");
                            sb3.append(lpunknownspace);
                            LogUtil.i(sb3.toString());
                            sql2 = sql;
                            args2 = args;
                            cursor3 = cursor2;
                            results3 = results2;
                        } catch (IllegalArgumentException e9) {
                            e = e9;
                            cursor3 = cursor2;
                            LogUtil.e("findCHRbyFreqLocation IllegalArgumentException: " + e.getMessage());
                        } catch (Exception e10) {
                            e = e10;
                            cursor3 = cursor2;
                            LogUtil.e("findCHRbyFreqLocation Exception: " + e.getMessage());
                        } catch (Throwable th6) {
                            th = th6;
                            cursor3 = cursor2;
                            if (cursor3 != null) {
                            }
                            throw th;
                        }
                    } catch (IllegalArgumentException e11) {
                        e = e11;
                        results2 = results3;
                        cursor3 = cursor2;
                        LogUtil.e("findCHRbyFreqLocation IllegalArgumentException: " + e.getMessage());
                    } catch (Exception e12) {
                        e = e12;
                        results2 = results3;
                        cursor3 = cursor2;
                        LogUtil.e("findCHRbyFreqLocation Exception: " + e.getMessage());
                    } catch (Throwable th7) {
                        th = th7;
                        Bundle bundle5 = results3;
                        cursor3 = cursor2;
                        if (cursor3 != null) {
                        }
                        throw th;
                    }
                }
                results2 = results3;
                Cursor cursor16 = cursor3;
                String str8 = sql2;
                String[] strArr10 = args2;
                if (cursor16 != null) {
                    cursor = cursor16;
                    cursor.close();
                } else {
                    cursor = cursor16;
                }
                Cursor cursor17 = cursor;
            } catch (IllegalArgumentException e13) {
                e = e13;
                results2 = results3;
                String str9 = sql2;
                String[] strArr11 = args2;
                LogUtil.e("findCHRbyFreqLocation IllegalArgumentException: " + e.getMessage());
            } catch (Exception e14) {
                e = e14;
                results2 = results3;
                String str10 = sql2;
                String[] strArr12 = args2;
                LogUtil.e("findCHRbyFreqLocation Exception: " + e.getMessage());
            } catch (Throwable th8) {
                th = th8;
                Bundle bundle6 = results3;
                String str11 = sql2;
                String[] strArr13 = args2;
                if (cursor3 != null) {
                    cursor3.close();
                }
                throw th;
            }
            return results2;
        }
        return results;
    }
}
