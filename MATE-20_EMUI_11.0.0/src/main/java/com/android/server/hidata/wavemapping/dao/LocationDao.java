package com.android.server.hidata.wavemapping.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.Arrays;

public class LocationDao {
    private static final String KEY_DURATION = "DURATION";
    private static final String KEY_ENTRY = "ENTERY";
    private static final String KEY_FIRST_REPORT = "FIRSTREPORT";
    private static final String KEY_FREQUENT_LOCATION = "FREQUENTLOCATION";
    private static final String KEY_FREQ_LOCATION = "FREQLOCATION";
    private static final String KEY_LEAVE = "LEAVE";
    private static final String KEY_LP_ALREADY_BEST = "LPALREADYBEST";
    private static final String KEY_LP_BACK = "LPBACK";
    private static final String KEY_LP_DATA_RX = "LPDATARX";
    private static final String KEY_LP_DATA_TX = "LPDATATX";
    private static final String KEY_LP_DURATION = "LPDURATION";
    private static final String KEY_LP_NOT_REACH = "LPNOTREACH";
    private static final String KEY_LP_OFFSET = "LPOFFSET";
    private static final String KEY_LP_TOTAL_SWITCH = "LPTOTALSWITCH";
    private static final String KEY_LP_UNKNOWN_DB = "LPUNKNOWNDB";
    private static final String KEY_LP_UNKNOWN_SPACE = "LPUNKNOWNSPACE";
    private static final String KEY_OOB_TIME = "OOBTIME";
    private static final String KEY_SPACE_CHANGE = "SPACECHANGE";
    private static final String KEY_SPACE_LEAVE = "SPACELEAVE";
    private static final String KEY_UPDATE_TIME = "UPDATETIME";
    private static final String KEY_UP_AUTO_FAIL = "UPAUTOFAIL";
    private static final String KEY_UP_AUTO_SUCC = "UPAUTOSUCC";
    private static final String KEY_UP_MANUAL_SUCC = "UPMANUALSUCC";
    private static final String KEY_UP_NO_SWITCH_FAIL = "UPNOSWITCHFAIL";
    private static final String KEY_UP_QUERY_CNT = "UPQRYCNT";
    private static final String KEY_UP_RESULT_CNT = "UPRESCNT";
    private static final String KEY_UP_TOTAL_SWITCH = "UPTOTALSWITCH";
    private static final String KEY_UP_UNKNOWN_DB = "UPUNKNOWNDB";
    private static final String KEY_UP_UNKNOWN_SPACE = "UPUNKNOWNSPACE";
    private static final String TAG = ("WMapping." + LocationDao.class.getSimpleName());
    private static final String UPDATE_PREFIX = "UPDATE ";
    private SQLiteDatabase db = DatabaseSingleton.getInstance();

    public boolean insert(String frequentLocation) {
        if (frequentLocation == null) {
            LogUtil.d(false, "insert failed ,frequentlocation == null", new Object[0]);
            return false;
        } else if (getOobTime() > 0) {
            return updateFrequentLocation(frequentLocation);
        } else {
            ContentValues contentValues = new ContentValues();
            long now = System.currentTimeMillis();
            contentValues.put(KEY_UPDATE_TIME, Long.valueOf(now));
            contentValues.put(KEY_OOB_TIME, Long.valueOf(now));
            contentValues.put(KEY_FREQUENT_LOCATION, frequentLocation);
            try {
                this.db.beginTransaction();
                this.db.insert(Constant.LOCATION_TABLE_NAME, null, contentValues);
                this.db.setTransactionSuccessful();
                this.db.endTransaction();
                return true;
            } catch (SQLException e) {
                LogUtil.e(false, "insert failed by Exception", new Object[0]);
                this.db.endTransaction();
                return false;
            } catch (Throwable th) {
                this.db.endTransaction();
                throw th;
            }
        }
    }

    public boolean insertOobTime(long oobTime) {
        if (getOobTime() > 0) {
            return false;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_OOB_TIME, Long.valueOf(oobTime));
        try {
            this.db.beginTransaction();
            this.db.insert(Constant.LOCATION_TABLE_NAME, null, contentValues);
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "insertOobTime failed by Exception", new Object[0]);
            this.db.endTransaction();
            return false;
        } catch (Throwable th) {
            this.db.endTransaction();
            throw th;
        }
    }

    public boolean updateFrequentLocation(String frequentLocation) {
        if (frequentLocation == null) {
            LogUtil.d(false, "update failure,frequentlocation == null", new Object[0]);
            return false;
        }
        Object[] args = {Long.valueOf(System.currentTimeMillis()), frequentLocation};
        LogUtil.i(false, "update begin: frequentlocation = %{private}s", frequentLocation);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE FREQUENT_LOCATION SET UPDATETIME = ?,FREQUENTLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "UPDATE exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean updateBenefitChrTime(long now) {
        if (getOobTime() == 0) {
            LogUtil.i(false, "updateBenefitChrTime: OOBTime = 0", new Object[0]);
            return false;
        }
        Object[] args = {Long.valueOf(now)};
        LogUtil.i(false, "update begin: CHRBENEFITUPLOADTIME = %{public}s", String.valueOf(now));
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE FREQUENT_LOCATION SET CHRBENEFITUPLOADTIME = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "UPDATE exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean updateSpaceUserChrTime(long now) {
        if (0 == getOobTime()) {
            LogUtil.i(false, "updateSpaceUserChrTime: OOBTime = 0", new Object[0]);
            return false;
        }
        Object[] args = {Long.valueOf(now)};
        LogUtil.i(false, "update begin: CHRSPACEUSERUPLOADTIME = %{public}s", String.valueOf(now));
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE FREQUENT_LOCATION SET CHRSPACEUSERUPLOADTIME = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "UPDATE exception: %{public}s", e.getMessage());
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
            LogUtil.e(false, "remove exception: %{public}s", e.getMessage());
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0042, code lost:
        if (0 == 0) goto L_0x0045;
     */
    public String getFrequentLocation() {
        String frequentLocation = null;
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return null;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT * FROM FREQUENT_LOCATION", null);
            if (cursor.moveToNext()) {
                frequentLocation = cursor.getString(cursor.getColumnIndexOrThrow(KEY_FREQUENT_LOCATION));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "getFrequentLocation IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "getFrequentLocation failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        if (frequentLocation != null) {
            LogUtil.i(false, "getFrequentLocation, frequentlocation:%{private}s", frequentLocation);
        } else {
            LogUtil.d(false, "getFrequentLocation, NO DATA", new Object[0]);
        }
        return frequentLocation;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0043, code lost:
        if (0 == 0) goto L_0x0046;
     */
    public long getBenefitChrTime() {
        long time = 0;
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return 0;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT * FROM FREQUENT_LOCATION", null);
            if (cursor.moveToNext()) {
                time = cursor.getLong(cursor.getColumnIndexOrThrow("CHRBENEFITUPLOADTIME"));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "getBenefitChrTime IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "getBenefitChrTime failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        LogUtil.i(false, "getBenefitChrTime, time:%{public}s", String.valueOf(time));
        return time;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0043, code lost:
        if (0 == 0) goto L_0x0046;
     */
    public long getSpaceUserChrTime() {
        long time = 0;
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return 0;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT * FROM FREQUENT_LOCATION", null);
            if (cursor.moveToNext()) {
                time = cursor.getLong(cursor.getColumnIndexOrThrow("CHRSPACEUSERUPLOADTIME"));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "getSpaceUserChrTime IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "getSpaceUserChrTime failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        LogUtil.i(false, "getSpaceUserChrTime, time:%{public}s", String.valueOf(time));
        return time;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0043, code lost:
        if (0 == 0) goto L_0x0046;
     */
    public long getLastUpdateTime() {
        long time = 0;
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return 0;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT * FROM FREQUENT_LOCATION", null);
            if (cursor.moveToNext()) {
                time = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_UPDATE_TIME));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "getLastUpdateTime IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "getLastUpdateTime failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        LogUtil.i(false, "getLastUpdateTime, time:%{public}s", String.valueOf(time));
        return time;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004e, code lost:
        if (0 == 0) goto L_0x0051;
     */
    public long getOobTime() {
        long oobTime = 0;
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return 0;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT * FROM FREQUENT_LOCATION", null);
            if (cursor.moveToNext()) {
                oobTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_OOB_TIME));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "getOobTime IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "getOobTime failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (IllegalStateException e3) {
            LogUtil.e(false, "getOobTime failed by IllegalStateException", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        LogUtil.d(false, "getOobTime, OOBTime:%{public}s", String.valueOf(oobTime));
        return oobTime;
    }

    public boolean insertChr(String freqLocation) {
        if (findChrByFreqLoc(freqLocation).containsKey(KEY_FREQ_LOCATION)) {
            return false;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_FREQ_LOCATION, freqLocation);
        long oobtime = getOobTime();
        long now = System.currentTimeMillis();
        int firstreport = Math.round(((float) (now - oobtime)) / 3600000.0f);
        contentValues.put(KEY_FIRST_REPORT, Integer.valueOf(firstreport));
        LogUtil.d(false, "insertChr, OOBTime:%{public}s now: %{public}s first report:%{public}d", String.valueOf(oobtime), String.valueOf(now), Integer.valueOf(firstreport));
        try {
            this.db.beginTransaction();
            this.db.insert(Constant.CHR_LOCATION_TABLE_NAME, null, contentValues);
            this.db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            LogUtil.e(false, "insertChr failed by Exception", new Object[0]);
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accChrEnterByFreqLoc(String location) {
        if (location == null) {
            LogUtil.d(false, "accChrEnterByFreqLoc failure,frequent location == null", new Object[0]);
            return false;
        }
        insertChr(location);
        Object[] args = {location};
        LogUtil.i(false, "update begin: entry  %{private}s", location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET ENTERY = ENTERY + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "accChrEnterByFreqLoc exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accChrLeaveByFreqLoc(String location) {
        if (location == null) {
            LogUtil.d(false, "accChrLeaveByFreqLoc failure,frequent location == null", new Object[0]);
            return false;
        }
        insertChr(location);
        Object[] args = {location};
        LogUtil.i(false, "update begin: leave  %{private}s", location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET LEAVE = LEAVE + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "accChrLeaveByFreqLoc exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accChrSpaceChangeByFreqLoc(String location) {
        if (location == null) {
            LogUtil.d(false, "accChrSpaceChangeByFreqLoc failure,frequent location == null", new Object[0]);
            return false;
        }
        insertChr(location);
        Object[] args = {location};
        LogUtil.i(false, "update begin: SPACECHANGE  %{private}s", location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET SPACECHANGE = SPACECHANGE + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "accChrSpaceChangeByFreqLoc exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accChrSpaceLeaveByFreqLoc(String location) {
        if (location == null) {
            LogUtil.d(false, "accChrSpaceChangeByFreqLoc failure,frequent location == null", new Object[0]);
            return false;
        }
        insertChr(location);
        Object[] args = {location};
        LogUtil.i(false, "update begin: SPACELEAVE  %{private}s", location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET SPACELEAVE = SPACELEAVE + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "accChrSpaceLeaveByFreqLoc exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean addChrDurationByFreqLoc(int duration, String location) {
        if (location == null) {
            LogUtil.d(false, "addChrDurationByFreqLoc failure,frequent location == null", new Object[0]);
            return false;
        }
        insertChr(location);
        Object[] args = {Integer.valueOf(duration), location};
        LogUtil.i(false, "update begin: add duration  %{public}d location:%{private}s", Integer.valueOf(duration), location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET DURATION = DURATION + ? WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "addChrDurationByFreqLoc exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accChrUserPrefNoSwitchFailByFreqLoc(String location) {
        if (location == null) {
            LogUtil.d(false, "accChrUserPrefNoSwitchFailByFreqLoc failure,frequent location == null", new Object[0]);
            return false;
        }
        insertChr(location);
        Object[] args = {location};
        LogUtil.i(false, "update begin: UPNOSWITCHFAIL at %{private}s", location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPNOSWITCHFAIL = UPNOSWITCHFAIL + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "accChrUserPrefNoSwitchFailByFreqLoc exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accChrUserPrefAutoFailByFreqLoc(String location) {
        if (location == null) {
            LogUtil.d(false, "accChrUserPrefAutoFailByFreqLoc failure,frequent location == null", new Object[0]);
            return false;
        }
        insertChr(location);
        Object[] args = {location};
        LogUtil.i(false, "update begin: UPAUTOFAIL at %{private}s", location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPAUTOFAIL = UPAUTOFAIL + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "accChrUserPrefAutoFailByFreqLoc exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accChrUserPrefAutoSuccByFreqLoc(String location) {
        if (location == null) {
            LogUtil.d(false, "accChrUserPrefAutoSuccByFreqLoc failure,frequent location == null", new Object[0]);
            return false;
        }
        insertChr(location);
        Object[] args = {location};
        LogUtil.i(false, "update begin: UPAUTOSUCC at %{private}s", location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPAUTOSUCC = UPAUTOSUCC + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "accChrUserPrefAutoSuccByFreqLoc exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accChrUserPrefManualSuccByFreqLoc(String location) {
        if (location == null) {
            LogUtil.d(false, "accChrUserPrefManualSuccByFreqLoc failure,frequent location == null", new Object[0]);
            return false;
        }
        insertChr(location);
        Object[] args = {location};
        LogUtil.i(false, "update begin: UPMANUALSUCC at %{private}s", location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPMANUALSUCC = UPMANUALSUCC + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "accChrUserPrefManualSuccByFreqLoc exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accChrUserPrefTotalSwitchByFreqLoc(String location) {
        if (location == null) {
            LogUtil.d(false, "accChrUserPrefTotalSwitchByFreqLoc failure,frequent location == null", new Object[0]);
            return false;
        }
        insertChr(location);
        Object[] args = {location};
        LogUtil.i(false, "update begin: UPTOTALSWITCH at %{private}s", location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPTOTALSWITCH = UPTOTALSWITCH + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "accChrUserPrefTotalSwitchByFreqLoc exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accChrUserPrefQueryCntByFreqLoc(String location) {
        if (location == null) {
            LogUtil.d(false, "accChrUserPrefQueryCntByFreqLoc failure,frequent location == null", new Object[0]);
            return false;
        }
        insertChr(location);
        Object[] args = {location};
        LogUtil.i(false, "update begin: UPQRYCNT at %{private}s", location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPQRYCNT = UPQRYCNT + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "accChrUserPrefQueryCntByFreqLoc exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accChrUserPrefResCntByFreqLoc(String location) {
        if (location == null) {
            LogUtil.d(false, "accChrUserPrefResCntByFreqLoc failure,frequent location == null", new Object[0]);
            return false;
        }
        insertChr(location);
        Object[] args = {location};
        LogUtil.i(false, "update begin: UPRESCNT at %{private}s", location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPRESCNT = UPRESCNT + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "accChrUserPrefResCntByFreqLoc exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accChrUserPrefUnknownDbByFreqLoc(String location) {
        if (location == null) {
            LogUtil.d(false, "accChrUserPrefUnknownDbByFreqLoc failure,frequent location == null", new Object[0]);
            return false;
        }
        insertChr(location);
        Object[] args = {location};
        LogUtil.i(false, "update begin: UPUNKNOWNDB at %{private}s", location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPUNKNOWNDB = UPUNKNOWNDB + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "accChrUserPrefUnknownDbByFreqLoc exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean accChrUserPrefUnknownSpaceByFreqLoc(String location) {
        if (location == null) {
            LogUtil.d(false, "accChrUserPrefUnknownSpaceByFreqLoc failure,frequent location == null", new Object[0]);
            return false;
        }
        insertChr(location);
        Object[] args = {location};
        LogUtil.i(false, "update begin: UPUNKNOWNSPACE at %{private}s", location);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET UPUNKNOWNSPACE = UPUNKNOWNSPACE + 1 WHERE FREQLOCATION = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "accChrUserPrefUnknownSpaceByFreqLoc exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean resetChrByFreqLoc(String location) {
        if (location == null) {
            LogUtil.d(false, "resetChrByFreqLoc failure,frequent location == null", new Object[0]);
            return false;
        } else if (!findChrByFreqLoc(location).containsKey(KEY_FREQ_LOCATION)) {
            return false;
        } else {
            Object[] args = {location};
            LogUtil.i(false, "update begin: RESET CHR at %{private}s", location);
            try {
                this.db.beginTransaction();
                this.db.execSQL("UPDATE CHR_FREQUENT_LOCATION SET ENTERY = 0,LEAVE = 0,DURATION = 0,SPACECHANGE = 0,SPACELEAVE = 0,UPTOTALSWITCH = 0,UPAUTOSUCC = 0,UPMANUALSUCC = 0,UPAUTOFAIL = 0,UPNOSWITCHFAIL = 0,UPQRYCNT = 0,UPRESCNT = 0,UPUNKNOWNDB = 0,UPUNKNOWNSPACE = 0,LPTOTALSWITCH = 0,LPDATARX = 0,LPDATATX = 0,LPDURATION = 0,LPOFFSET = 0,LPALREADYBEST = 0,LPNOTREACH = 0,LPBACK = 0,LPUNKNOWNDB = 0,LPUNKNOWNSPACE = 0 WHERE FREQLOCATION = ?", args);
                this.db.setTransactionSuccessful();
                return true;
            } catch (SQLException e) {
                LogUtil.e(false, "resetChrByFreqLoc exception: %{public}s", e.getMessage());
                return false;
            } finally {
                this.db.endTransaction();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:62:0x03f3, code lost:
        if (r15 != null) goto L_0x03f5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x03f5, code lost:
        r15.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0411, code lost:
        if (r15 != null) goto L_0x03f5;
     */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x041a  */
    public Bundle findChrByFreqLoc(String freqLocation) {
        Cursor cursor;
        Throwable th;
        Cursor cursor2;
        IllegalArgumentException e;
        int firstReport;
        int entery;
        int leave;
        int duration;
        int spaceChange;
        int spaceLeave;
        int upTotalSwitch;
        int upAutoSucc;
        int upManualSucc;
        int upAutoFail;
        int upNoSwitchFail;
        int upQueryCnt;
        int upResultCnt;
        int upUnknownDb;
        int upUnknownSpace;
        int lpTotalSwitch;
        int lpDataRx;
        int lpDataTx;
        int lpDuration;
        int lpOffset;
        int lpAlreadyBest;
        int lpNotReach;
        int lpBack;
        int lpUnknownDb;
        int lpUnknownSpace;
        String str = KEY_UP_AUTO_FAIL;
        String str2 = KEY_UP_MANUAL_SUCC;
        String str3 = KEY_UP_AUTO_SUCC;
        String str4 = KEY_UP_TOTAL_SWITCH;
        String str5 = KEY_SPACE_LEAVE;
        String str6 = KEY_SPACE_CHANGE;
        String str7 = "DURATION";
        String str8 = KEY_LEAVE;
        String str9 = KEY_ENTRY;
        String str10 = KEY_FIRST_REPORT;
        Bundle results = new Bundle();
        if (this.db == null) {
            return results;
        }
        if (freqLocation == null) {
            return results;
        }
        String sql = "SELECT * FROM CHR_FREQUENT_LOCATION WHERE FREQLOCATION = ?";
        Bundle results2 = results;
        String str11 = KEY_LP_UNKNOWN_SPACE;
        String[] args = {freqLocation};
        String str12 = KEY_UP_NO_SWITCH_FAIL;
        LogUtil.i(false, "findCHRbyFreqLocation: sql=%{public}s ,args=%{public}s", sql, Arrays.toString(args));
        try {
            Cursor cursor3 = this.db.rawQuery(sql, args);
            while (cursor3.moveToNext()) {
                try {
                    firstReport = cursor3.getInt(cursor3.getColumnIndexOrThrow(str10));
                    entery = cursor3.getInt(cursor3.getColumnIndexOrThrow(str9));
                    leave = cursor3.getInt(cursor3.getColumnIndexOrThrow(str8));
                    try {
                        duration = cursor3.getInt(cursor3.getColumnIndexOrThrow(str7));
                    } catch (IllegalArgumentException e2) {
                        e = e2;
                        cursor2 = cursor3;
                        LogUtil.e(false, "findCHRbyFreqLocation IllegalArgumentException: %{public}s", e.getMessage());
                    } catch (SQLException e3) {
                        cursor2 = cursor3;
                        try {
                            LogUtil.e(false, "findCHRbyFreqLocation failed by Exception", new Object[0]);
                        } catch (Throwable th2) {
                            th = th2;
                            cursor = cursor2;
                            if (cursor != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        cursor = cursor3;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                } catch (IllegalArgumentException e4) {
                    e = e4;
                    cursor2 = cursor3;
                    LogUtil.e(false, "findCHRbyFreqLocation IllegalArgumentException: %{public}s", e.getMessage());
                } catch (SQLException e5) {
                    cursor2 = cursor3;
                    LogUtil.e(false, "findCHRbyFreqLocation failed by Exception", new Object[0]);
                } catch (Throwable th4) {
                    th = th4;
                    cursor = cursor3;
                    if (cursor != null) {
                    }
                    throw th;
                }
                try {
                    spaceChange = cursor3.getInt(cursor3.getColumnIndexOrThrow(str6));
                    spaceLeave = cursor3.getInt(cursor3.getColumnIndexOrThrow(str5));
                    upTotalSwitch = cursor3.getInt(cursor3.getColumnIndexOrThrow(str4));
                    upAutoSucc = cursor3.getInt(cursor3.getColumnIndexOrThrow(str3));
                    upManualSucc = cursor3.getInt(cursor3.getColumnIndexOrThrow(str2));
                    upAutoFail = cursor3.getInt(cursor3.getColumnIndexOrThrow(str));
                    upNoSwitchFail = cursor3.getInt(cursor3.getColumnIndexOrThrow(str12));
                    upQueryCnt = cursor3.getInt(cursor3.getColumnIndexOrThrow(KEY_UP_QUERY_CNT));
                    upResultCnt = cursor3.getInt(cursor3.getColumnIndexOrThrow(KEY_UP_RESULT_CNT));
                    upUnknownDb = cursor3.getInt(cursor3.getColumnIndexOrThrow(KEY_UP_UNKNOWN_DB));
                    upUnknownSpace = cursor3.getInt(cursor3.getColumnIndexOrThrow(str11));
                    lpTotalSwitch = cursor3.getInt(cursor3.getColumnIndexOrThrow(KEY_LP_TOTAL_SWITCH));
                    lpDataRx = cursor3.getInt(cursor3.getColumnIndexOrThrow(KEY_LP_DATA_RX));
                    lpDataTx = cursor3.getInt(cursor3.getColumnIndexOrThrow(KEY_LP_DATA_TX));
                    lpDuration = cursor3.getInt(cursor3.getColumnIndexOrThrow(KEY_LP_DURATION));
                    lpOffset = cursor3.getInt(cursor3.getColumnIndexOrThrow(KEY_LP_OFFSET));
                    lpAlreadyBest = cursor3.getInt(cursor3.getColumnIndexOrThrow(KEY_LP_ALREADY_BEST));
                    lpNotReach = cursor3.getInt(cursor3.getColumnIndexOrThrow(KEY_LP_NOT_REACH));
                    lpBack = cursor3.getInt(cursor3.getColumnIndexOrThrow(KEY_LP_BACK));
                    lpUnknownDb = cursor3.getInt(cursor3.getColumnIndexOrThrow(KEY_LP_UNKNOWN_DB));
                    lpUnknownSpace = cursor3.getInt(cursor3.getColumnIndexOrThrow(str11));
                    cursor = cursor3;
                } catch (IllegalArgumentException e6) {
                    e = e6;
                    cursor2 = cursor3;
                    LogUtil.e(false, "findCHRbyFreqLocation IllegalArgumentException: %{public}s", e.getMessage());
                } catch (SQLException e7) {
                    cursor2 = cursor3;
                    LogUtil.e(false, "findCHRbyFreqLocation failed by Exception", new Object[0]);
                } catch (Throwable th5) {
                    th = th5;
                    cursor = cursor3;
                    if (cursor != null) {
                    }
                    throw th;
                }
                try {
                    results2.putString(KEY_FREQ_LOCATION, freqLocation);
                    results2.putInt(str10, firstReport);
                    results2.putInt(str9, entery);
                    results2.putInt(str8, leave);
                    results2.putInt(str7, duration);
                    results2.putInt(str6, spaceChange);
                    results2.putInt(str5, spaceLeave);
                    results2.putInt(str4, upTotalSwitch);
                    results2.putInt(str3, upAutoSucc);
                    results2.putInt(str2, upManualSucc);
                    results2.putInt(str, upAutoFail);
                    results2.putInt(str12, upNoSwitchFail);
                    str12 = str12;
                    results2.putInt(KEY_UP_QUERY_CNT, upQueryCnt);
                    results2.putInt(KEY_UP_RESULT_CNT, upResultCnt);
                    results2.putInt(KEY_UP_UNKNOWN_DB, upUnknownDb);
                    results2.putInt(KEY_UP_UNKNOWN_SPACE, upUnknownSpace);
                    results2.putInt(KEY_LP_TOTAL_SWITCH, lpTotalSwitch);
                    results2.putInt(KEY_LP_DATA_RX, lpDataRx);
                    results2.putInt(KEY_LP_DATA_TX, lpDataTx);
                    results2.putInt(KEY_LP_DURATION, lpDuration);
                    results2.putInt(KEY_LP_OFFSET, lpOffset);
                    results2.putInt(KEY_LP_ALREADY_BEST, lpAlreadyBest);
                    results2.putInt(KEY_LP_NOT_REACH, lpNotReach);
                    results2.putInt(KEY_LP_BACK, lpBack);
                    results2.putInt(KEY_LP_UNKNOWN_DB, lpUnknownDb);
                    results2.putInt(str11, lpUnknownSpace);
                    results2 = results2;
                    try {
                        LogUtil.i(false, " freqLocation:%{private}s, first report:%{public}d, entery:%{public}d, leave:%{public}d, duration:%{public}d, space change:%{public}d, space leave:%{public}d, uptotalswitch:%{public}d, upAutoSucc:%{public}d, upManualSucc:%{public}d, upAutoFail:%{public}d, upNoSwitchFail:%{public}d, upQueryCnt:%{public}d, upResultCnt:%{public}d upUnknownDb:%{public}d, upUnknownSpace:%{public}d, lpTotalSwitch:%{public}d, lpDataRx:%{public}d, lpDataTx:%{public}d, lpDuration:%{public}d, lpOffset:%{public}d, lpAlreadyBest:%{public}d, lpNotReach:%{public}d, lpBack:%{public}d lpUnknownDb:%{public}d, lpUnknownSpace:%{public}d", freqLocation, Integer.valueOf(firstReport), Integer.valueOf(entery), Integer.valueOf(leave), Integer.valueOf(duration), Integer.valueOf(spaceChange), Integer.valueOf(spaceLeave), Integer.valueOf(upTotalSwitch), Integer.valueOf(upAutoSucc), Integer.valueOf(upManualSucc), Integer.valueOf(upAutoFail), Integer.valueOf(upNoSwitchFail), Integer.valueOf(upQueryCnt), Integer.valueOf(upResultCnt), Integer.valueOf(upUnknownDb), Integer.valueOf(upUnknownSpace), Integer.valueOf(lpTotalSwitch), Integer.valueOf(lpDataRx), Integer.valueOf(lpDataTx), Integer.valueOf(lpDuration), Integer.valueOf(lpOffset), Integer.valueOf(lpAlreadyBest), Integer.valueOf(lpNotReach), Integer.valueOf(lpBack), Integer.valueOf(lpUnknownDb), Integer.valueOf(lpUnknownSpace));
                        str = str;
                        str8 = str8;
                        args = args;
                        sql = sql;
                        str6 = str6;
                        str5 = str5;
                        str4 = str4;
                        str3 = str3;
                        str2 = str2;
                        str7 = str7;
                        str10 = str10;
                        cursor3 = cursor;
                        str11 = str11;
                        str9 = str9;
                    } catch (IllegalArgumentException e8) {
                        e = e8;
                        cursor2 = cursor;
                        LogUtil.e(false, "findCHRbyFreqLocation IllegalArgumentException: %{public}s", e.getMessage());
                    } catch (SQLException e9) {
                        cursor2 = cursor;
                        LogUtil.e(false, "findCHRbyFreqLocation failed by Exception", new Object[0]);
                    } catch (Throwable th6) {
                        th = th6;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                } catch (IllegalArgumentException e10) {
                    e = e10;
                    results2 = results2;
                    cursor2 = cursor;
                    LogUtil.e(false, "findCHRbyFreqLocation IllegalArgumentException: %{public}s", e.getMessage());
                } catch (SQLException e11) {
                    results2 = results2;
                    cursor2 = cursor;
                    LogUtil.e(false, "findCHRbyFreqLocation failed by Exception", new Object[0]);
                } catch (Throwable th7) {
                    th = th7;
                    if (cursor != null) {
                    }
                    throw th;
                }
            }
            cursor3.close();
        } catch (IllegalArgumentException e12) {
            e = e12;
            cursor2 = null;
            LogUtil.e(false, "findCHRbyFreqLocation IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e13) {
            cursor2 = null;
            LogUtil.e(false, "findCHRbyFreqLocation failed by Exception", new Object[0]);
        } catch (Throwable th8) {
            th = th8;
            cursor = null;
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        return results2;
    }
}
