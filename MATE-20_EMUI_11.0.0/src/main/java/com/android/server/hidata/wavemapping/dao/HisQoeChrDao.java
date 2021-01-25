package com.android.server.hidata.wavemapping.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.util.LogUtil;

public class HisQoeChrDao {
    private static final String DELETE_PREFIX = "DELETE FROM ";
    private static final String FREQ_LOC_KEY = "FREQLOCNAME";
    private static final String GOOD_COUNT_KEY = "GOODCNT";
    private static final String JUDGE_SUFFIX = " WHERE FREQLOCNAME = ?";
    private static final String POOR_COUNT_KEY = "POORCNT";
    private static final String QUERY_COUNT_KEY = "QUERYCNT";
    private static final String RESET_RECORD_BY_LOC_SUFFIX = " SET QUERYCNT = 0, GOODCNT = 0, POORCNT = 0, DATARX = 0, DATATX = 0, UNKNOWNDB = 0, UNKNOWNSPACE = 0 WHERE FREQLOCNAME = ?";
    private static final String SELECT_COUNT_BY_LOC_PREFIX = "SELECT QUERYCNT, GOODCNT, POORCNT, UNKNOWNDB, UNKNOWNSPACE FROM ";
    private static final String SELECT_RECORD_BY_LOC_PREFIX = "SELECT * FROM ";
    private static final String TAG = ("WMapping." + HisQoeChrDao.class.getSimpleName());
    private static final String UNKNOWN_DB_KEY = "UNKNOWNDB";
    private static final String UNKNOWN_SPACE_KEY = "UNKNOWNSPACE";
    private static final String UPDATE_RECORD_BY_LOC_PREFIX = "UPDATE ";
    private static final String UPDATE_RECORD_BY_LOC_SUFFIX = " SET QUERYCNT = ?, GOODCNT = ?, POORCNT = ?, UNKNOWNDB = ?, UNKNOWNSPACE = ? WHERE FREQLOCNAME = ?";
    private SQLiteDatabase db = DatabaseSingleton.getInstance();
    private String freqLocation = "UNKNOWN";
    private int hisQoeGoodCnt = 0;
    private int hisQoePoorCnt = 0;
    private int hisQoeQueryCnt = 0;
    private int hisQoeUnknownDb = 0;
    private int hisQoeUnknownSpace = 0;

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0079, code lost:
        if (0 == 0) goto L_0x007c;
     */
    public boolean getCountersByLocation() {
        String[] args = {this.freqLocation};
        boolean isFounded = false;
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return false;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT QUERYCNT, GOODCNT, POORCNT, UNKNOWNDB, UNKNOWNSPACE FROM CHR_HISTQOERPT WHERE FREQLOCNAME = ?", args);
            if (cursor.moveToNext()) {
                isFounded = true;
                this.hisQoeQueryCnt = cursor.getInt(cursor.getColumnIndexOrThrow(QUERY_COUNT_KEY));
                this.hisQoeGoodCnt = cursor.getInt(cursor.getColumnIndexOrThrow(GOOD_COUNT_KEY));
                this.hisQoePoorCnt = cursor.getInt(cursor.getColumnIndexOrThrow(POOR_COUNT_KEY));
                this.hisQoeUnknownDb = cursor.getInt(cursor.getColumnIndexOrThrow(UNKNOWN_DB_KEY));
                this.hisQoeUnknownSpace = cursor.getInt(cursor.getColumnIndexOrThrow(UNKNOWN_SPACE_KEY));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "Argument getCntNumByLoc in HistQoeChrTable IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "getCntNumByLoc in HistQoeChrTable failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        LogUtil.i(false, "getCntNumByLoc in HistQoeChrTable found:%{public}s location:%{private}s:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d", String.valueOf(isFounded), this.freqLocation, Integer.valueOf(this.hisQoeQueryCnt), Integer.valueOf(this.hisQoeGoodCnt), Integer.valueOf(this.hisQoePoorCnt), Integer.valueOf(this.hisQoeUnknownDb), Integer.valueOf(this.hisQoeUnknownSpace));
        return isFounded;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x007a, code lost:
        if (0 == 0) goto L_0x007d;
     */
    public boolean getCountersByLocation(String location) {
        String[] args = {location};
        boolean isFounded = false;
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null || location == null) {
            return false;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT QUERYCNT, GOODCNT, POORCNT, UNKNOWNDB, UNKNOWNSPACE FROM CHR_HISTQOERPT WHERE FREQLOCNAME = ?", args);
            if (cursor.moveToNext()) {
                isFounded = true;
                this.hisQoeQueryCnt = cursor.getInt(cursor.getColumnIndexOrThrow(QUERY_COUNT_KEY));
                this.hisQoeGoodCnt = cursor.getInt(cursor.getColumnIndexOrThrow(GOOD_COUNT_KEY));
                this.hisQoePoorCnt = cursor.getInt(cursor.getColumnIndexOrThrow(POOR_COUNT_KEY));
                this.hisQoeUnknownDb = cursor.getInt(cursor.getColumnIndexOrThrow(UNKNOWN_DB_KEY));
                this.hisQoeUnknownSpace = cursor.getInt(cursor.getColumnIndexOrThrow(UNKNOWN_SPACE_KEY));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "getCountersByLocation IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "getCountersByLocation failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        LogUtil.i(false, "getCountersByLocation in HistQoeChrTable found:%{public}s location:%{private}s:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d", String.valueOf(isFounded), location, Integer.valueOf(this.hisQoeQueryCnt), Integer.valueOf(this.hisQoeGoodCnt), Integer.valueOf(this.hisQoePoorCnt), Integer.valueOf(this.hisQoeUnknownDb), Integer.valueOf(this.hisQoeUnknownSpace));
        return isFounded;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003d, code lost:
        if (r5 == null) goto L_0x0040;
     */
    public boolean getRecordByLoc() {
        String[] args = {this.freqLocation};
        boolean isFounded = false;
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return false;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT * FROM CHR_HISTQOERPT WHERE FREQLOCNAME = ?", args);
            if (cursor.moveToNext()) {
                isFounded = true;
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "Argument getRecordByLoc in HistQoeChrTable IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "getRecordByLoc in HistQoeChrTable failed by Exception", new Object[0]);
            if (cursor != null) {
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        LogUtil.i(false, "getRecordByLoc in HistQoeChrTable found:%{public}s", String.valueOf(isFounded));
        return isFounded;
    }

    public boolean updateRecordByLoc() {
        Object[] args = {Integer.valueOf(this.hisQoeQueryCnt), Integer.valueOf(this.hisQoeGoodCnt), Integer.valueOf(this.hisQoePoorCnt), Integer.valueOf(this.hisQoeUnknownDb), Integer.valueOf(this.hisQoeUnknownSpace), this.freqLocation};
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_HISTQOERPT SET QUERYCNT = ?, GOODCNT = ?, POORCNT = ?, UNKNOWNDB = ?, UNKNOWNSPACE = ? WHERE FREQLOCNAME = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "updateRecordByLoc exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean insertRecordByLoc() {
        if (getRecordByLoc()) {
            return updateRecordByLoc();
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(FREQ_LOC_KEY, this.freqLocation);
        contentValues.put(QUERY_COUNT_KEY, Integer.valueOf(this.hisQoeQueryCnt));
        contentValues.put(GOOD_COUNT_KEY, Integer.valueOf(this.hisQoeGoodCnt));
        contentValues.put(POOR_COUNT_KEY, Integer.valueOf(this.hisQoePoorCnt));
        contentValues.put(UNKNOWN_DB_KEY, Integer.valueOf(this.hisQoeUnknownDb));
        contentValues.put(UNKNOWN_SPACE_KEY, Integer.valueOf(this.hisQoeUnknownSpace));
        LogUtil.i(false, "insertRecordByLoc in HistQoeChrTable location:%{public}s:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d", this.freqLocation, Integer.valueOf(this.hisQoeQueryCnt), Integer.valueOf(this.hisQoeGoodCnt), Integer.valueOf(this.hisQoePoorCnt), Integer.valueOf(this.hisQoeUnknownDb), Integer.valueOf(this.hisQoeUnknownSpace));
        try {
            this.db.beginTransaction();
            this.db.insert(Constant.CHR_HISTQOERPT, null, contentValues);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "insertRecordByLoc failed by Exception", new Object[0]);
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean resetRecord(String loc) {
        String[] args = {loc};
        if (!getCountersByLocation(loc)) {
            return false;
        }
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_HISTQOERPT SET QUERYCNT = 0, GOODCNT = 0, POORCNT = 0, DATARX = 0, DATATX = 0, UNKNOWNDB = 0, UNKNOWNSPACE = 0 WHERE FREQLOCNAME = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "resetRecord by loc of CHR_HISTQOERPT exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
            resetChrCnt();
        }
    }

    public boolean delRecord() {
        try {
            this.db.beginTransaction();
            this.db.execSQL("DELETE FROM CHR_HISTQOERPT", null);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "delRecord of delRecord exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public void resetChrCnt() {
        this.hisQoeQueryCnt = 0;
        this.hisQoeGoodCnt = 0;
        this.hisQoePoorCnt = 0;
        this.hisQoeUnknownDb = 0;
        this.hisQoeUnknownSpace = 0;
    }

    public void accQueryCnt() {
        this.hisQoeQueryCnt++;
    }

    public int getQueryCnt() {
        return this.hisQoeQueryCnt;
    }

    public void accGoodCnt() {
        this.hisQoeGoodCnt++;
    }

    public int getGoodCnt() {
        return this.hisQoeGoodCnt;
    }

    public void accPoorCnt() {
        this.hisQoePoorCnt++;
    }

    public int getPoorCnt() {
        return this.hisQoePoorCnt;
    }

    public void accUnknownDb() {
        this.hisQoeUnknownDb++;
    }

    public int getUnknownDb() {
        return this.hisQoeUnknownDb;
    }

    public void accUnknownSpace() {
        this.hisQoeUnknownSpace++;
    }

    public int getUnknownSpace() {
        return this.hisQoeUnknownSpace;
    }

    public void setLocation(String location) {
        if (location != null) {
            this.freqLocation = location;
            resetChrCnt();
        }
    }

    public String getLocation() {
        return this.freqLocation;
    }
}
