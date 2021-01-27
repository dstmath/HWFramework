package com.android.server.hidata.wavemapping.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.Arrays;

public class FastBack2LteChrDao {
    private static final String JUDGE_SUFFIX = " WHERE FREQLOCNAME = ?";
    private static final String KEY_CELL_IN_4G = "CELLS4G";
    private static final String KEY_FAST_BACK = "FASTBACK";
    private static final String KEY_FREQ_LOC_NAME = "FREQLOCNAME";
    private static final String KEY_IN_LTE_CNT = "INLTECNT";
    private static final String KEY_LOW_RAT_CNT = "LOWRATCNT";
    private static final String KEY_OUT_LTE_CNT = "OUTLTECNT";
    private static final String KEY_REF_CNT = "REFCNT";
    private static final String KEY_SUCCESS_BACK = "SUCCESSBACK";
    private static final String KEY_UNKNOWN_DB = "UNKNOWNDB";
    private static final String KEY_UNKNOWN_SPACE = "UNKNOWNSPACE";
    private static final String RESET_RECORD_BY_FREQ_LOC_SUFFIX = " SET LOWRATCNT = 0, INLTECNT = 0, OUTLTECNT = 0, FASTBACK = 0, SUCCESSBACK = 0, CELLS4G = 0, REFCNT = 0, UNKNOWNDB = 0, UNKNOWNSPACE = 0 WHERE FREQLOCNAME = ?";
    private static final String RESET_SUFFIX = " SET LOWRATCNT = 0, INLTECNT = 0, OUTLTECNT = 0, FASTBACK = 0, SUCCESSBACK = 0, CELLS4G = 0, REFCNT = 0, UNKNOWNDB = 0, UNKNOWNSPACE = 0";
    private static final String SELECT_TOTAL_COUNT_PREFIX = "SELECT LOWRATCNT, INLTECNT, OUTLTECNT, FASTBACK, SUCCESSBACK, CELLS4G, REFCNT, UNKNOWNDB, UNKNOWNSPACE FROM ";
    private static final int SESSION_3G_END = 0;
    private static final int SESSION_3G_START = 1;
    private static final int SESSION_3G_WAIT = 2;
    private static final String TAG = ("WMapping." + FastBack2LteChrDao.class.getSimpleName());
    private static final String UPDATE_PREFIX = "UPDATE ";
    private static final String UPDATE_RECORD_BY_FREQ_LOC_SUFFIX = " SET LOWRATCNT = ?, INLTECNT = ?, OUTLTECNT = ?, FASTBACK = ?, SUCCESSBACK = ?, CELLS4G = ?, REFCNT = ?, UNKNOWNDB = ?, UNKNOWNSPACE = ? WHERE FREQLOCNAME = ?";
    private static final String UPDATE_SUFFIX = " SET LOWRATCNT = ?, INLTECNT = ?, OUTLTECNT = ?, FASTBACK = ?, SUCCESSBACK = ?, CELLS4G = ?, REFCNT = ?, UNKNOWNDB = ?, UNKNOWNSPACE = ?";
    private int cells4G = 0;
    private SQLiteDatabase db = DatabaseSingleton.getInstance();
    private int fastBack = 0;
    private int inLteCnt = 0;
    private String location = "UNKNOWN";
    private int lowRatCnt = 0;
    private int outLteCnt = 0;
    private int refCnt = 0;
    private int session3gState = 0;
    private int sessionSpaceAll = -1;
    private int sessionSpaceMain = -1;
    private int successBack = 0;
    private int sumCells4G = 0;
    private int sumFastBack = 0;
    private int sumInLteCnt = 0;
    private int sumLowRatCnt = 0;
    private int sumOutLteCnt = 0;
    private int sumRefCnt = 0;
    private int sumSuccessBack = 0;
    private int sumUnknownDb = 0;
    private int sumUnknownSpace = 0;
    private int unknownDb = 0;
    private int unknownSpace = 0;

    public void resetFastBack2LteSumCount() {
        this.sumLowRatCnt = 0;
        this.sumInLteCnt = 0;
        this.sumOutLteCnt = 0;
        this.sumFastBack = 0;
        this.sumSuccessBack = 0;
        this.sumCells4G = 0;
        this.sumRefCnt = 0;
        this.sumUnknownDb = 0;
        this.sumUnknownSpace = 0;
    }

    public void resetFastBack2LteCount() {
        this.lowRatCnt = 0;
        this.inLteCnt = 0;
        this.outLteCnt = 0;
        this.fastBack = 0;
        this.successBack = 0;
        this.cells4G = 0;
        this.refCnt = 0;
        this.unknownDb = 0;
        this.unknownSpace = 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00c7, code lost:
        if (0 == 0) goto L_0x00ca;
     */
    public boolean getTotalCounters() {
        boolean isFounded = false;
        Cursor cursor = null;
        if (this.db == null) {
            return false;
        }
        try {
            resetFastBack2LteSumCount();
            cursor = this.db.rawQuery("SELECT LOWRATCNT, INLTECNT, OUTLTECNT, FASTBACK, SUCCESSBACK, CELLS4G, REFCNT, UNKNOWNDB, UNKNOWNSPACE FROM CHR_FASTBACK2LTE", null);
            while (cursor.moveToNext()) {
                isFounded = true;
                this.sumLowRatCnt += cursor.getInt(cursor.getColumnIndexOrThrow(KEY_LOW_RAT_CNT));
                this.sumInLteCnt += cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_LTE_CNT));
                this.sumOutLteCnt += cursor.getInt(cursor.getColumnIndexOrThrow(KEY_OUT_LTE_CNT));
                this.sumFastBack += cursor.getInt(cursor.getColumnIndexOrThrow(KEY_FAST_BACK));
                this.sumSuccessBack += cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUCCESS_BACK));
                this.sumCells4G += cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CELL_IN_4G));
                this.sumRefCnt += cursor.getInt(cursor.getColumnIndexOrThrow(KEY_REF_CNT));
                this.sumUnknownDb += cursor.getInt(cursor.getColumnIndexOrThrow(KEY_UNKNOWN_DB));
                this.sumUnknownSpace += cursor.getInt(cursor.getColumnIndexOrThrow(KEY_UNKNOWN_SPACE));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "Argument getTotalCounters in Back2LteChrTable IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "getTotalCounters in Back2LteChrTable failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        LogUtil.d(false, "getTotalCounters in Back2LteChrTable found:%{public}s:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d", String.valueOf(isFounded), Integer.valueOf(this.sumLowRatCnt), Integer.valueOf(this.sumInLteCnt), Integer.valueOf(this.sumOutLteCnt), Integer.valueOf(this.sumFastBack), Integer.valueOf(this.sumSuccessBack), Integer.valueOf(this.sumCells4G), Integer.valueOf(this.sumRefCnt), Integer.valueOf(this.sumUnknownDb), Integer.valueOf(this.sumUnknownSpace));
        return isFounded;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x00ad, code lost:
        if (0 == 0) goto L_0x00b0;
     */
    public boolean getCountersByLocation(String loc) {
        String[] args = {loc};
        boolean isFounded = false;
        Cursor cursor = null;
        if (this.db == null) {
            return false;
        }
        try {
            resetFastBack2LteCount();
            cursor = this.db.rawQuery("SELECT LOWRATCNT, INLTECNT, OUTLTECNT, FASTBACK, SUCCESSBACK, CELLS4G, REFCNT, UNKNOWNDB, UNKNOWNSPACE FROM CHR_FASTBACK2LTE WHERE FREQLOCNAME = ?", args);
            if (cursor.moveToNext()) {
                isFounded = true;
                this.lowRatCnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_LOW_RAT_CNT));
                this.inLteCnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_LTE_CNT));
                this.outLteCnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_OUT_LTE_CNT));
                this.fastBack = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_FAST_BACK));
                this.successBack = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUCCESS_BACK));
                this.cells4G = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CELL_IN_4G));
                this.refCnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_REF_CNT));
                this.unknownDb = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_UNKNOWN_DB));
                this.unknownSpace = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_UNKNOWN_SPACE));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "Argument getCountersByLocation in Back2LteChrTable IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "getCountersByLocation with string in Back2LteChrTable failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        LogUtil.d(false, "getCountersByLocation in Back2LteChrTable found:%{public}s location:%{public}s:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d", String.valueOf(isFounded), this.location, Integer.valueOf(this.lowRatCnt), Integer.valueOf(this.inLteCnt), Integer.valueOf(this.outLteCnt), Integer.valueOf(this.fastBack), Integer.valueOf(this.successBack), Integer.valueOf(this.cells4G), Integer.valueOf(this.refCnt), Integer.valueOf(this.unknownDb), Integer.valueOf(this.unknownSpace));
        return isFounded;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x00aa, code lost:
        if (0 == 0) goto L_0x00ad;
     */
    public boolean getCountersByLocation() {
        String[] args = {this.location};
        boolean isFounded = false;
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return false;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT LOWRATCNT, INLTECNT, OUTLTECNT, FASTBACK, SUCCESSBACK, CELLS4G, REFCNT, UNKNOWNDB, UNKNOWNSPACE FROM CHR_FASTBACK2LTE WHERE FREQLOCNAME = ?", args);
            if (cursor.moveToNext()) {
                isFounded = true;
                this.lowRatCnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_LOW_RAT_CNT));
                this.inLteCnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_LTE_CNT));
                this.outLteCnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_OUT_LTE_CNT));
                this.fastBack = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_FAST_BACK));
                this.successBack = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUCCESS_BACK));
                this.cells4G = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CELL_IN_4G));
                this.refCnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_REF_CNT));
                this.unknownDb = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_UNKNOWN_DB));
                this.unknownSpace = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_UNKNOWN_SPACE));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "Argument getCountersByLocation in Back2LteChrTable IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "getCountersByLocation in Back2LteChrTable failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        LogUtil.d(false, "getCountersByLocation in Back2LteChrTable found:%{public}s location:%{public}s:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d", String.valueOf(isFounded), this.location, Integer.valueOf(this.lowRatCnt), Integer.valueOf(this.inLteCnt), Integer.valueOf(this.outLteCnt), Integer.valueOf(this.fastBack), Integer.valueOf(this.successBack), Integer.valueOf(this.cells4G), Integer.valueOf(this.refCnt), Integer.valueOf(this.unknownDb), Integer.valueOf(this.unknownSpace));
        return isFounded;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003d, code lost:
        if (r5 == null) goto L_0x0040;
     */
    public boolean getRecordByLoc() {
        String[] args = {this.location};
        boolean isFounded = false;
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return false;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT LOWRATCNT, INLTECNT, OUTLTECNT, FASTBACK, SUCCESSBACK, CELLS4G, REFCNT, UNKNOWNDB, UNKNOWNSPACE FROM CHR_FASTBACK2LTE WHERE FREQLOCNAME = ?", args);
            if (cursor.moveToNext()) {
                isFounded = true;
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "Argument getRecordByLoc in Back2LteChrTable IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "getRecordByLoc in Back2LteChrTable failed by Exception", new Object[0]);
            if (cursor != null) {
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        LogUtil.i(false, "getRecordByLoc in Back2LteChrTable found:%{public}s", String.valueOf(isFounded));
        return isFounded;
    }

    public boolean updateRecordByLoc() {
        Object[] args = {Integer.valueOf(this.lowRatCnt), Integer.valueOf(this.inLteCnt), Integer.valueOf(this.outLteCnt), Integer.valueOf(this.fastBack), Integer.valueOf(this.successBack), Integer.valueOf(this.cells4G), Integer.valueOf(this.refCnt), Integer.valueOf(this.unknownDb), Integer.valueOf(this.unknownSpace), this.location};
        LogUtil.i(false, "updateRecordByLoc: %{public}s", Arrays.toString(Arrays.copyOfRange(args, 1, args.length)));
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FASTBACK2LTE SET LOWRATCNT = ?, INLTECNT = ?, OUTLTECNT = ?, FASTBACK = ?, SUCCESSBACK = ?, CELLS4G = ?, REFCNT = ?, UNKNOWNDB = ?, UNKNOWNSPACE = ? WHERE FREQLOCNAME = ?", args);
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
        contentValues.put(KEY_FREQ_LOC_NAME, this.location);
        contentValues.put(KEY_LOW_RAT_CNT, Integer.valueOf(this.lowRatCnt));
        contentValues.put(KEY_IN_LTE_CNT, Integer.valueOf(this.inLteCnt));
        contentValues.put(KEY_OUT_LTE_CNT, Integer.valueOf(this.outLteCnt));
        contentValues.put(KEY_FAST_BACK, Integer.valueOf(this.fastBack));
        contentValues.put(KEY_SUCCESS_BACK, Integer.valueOf(this.successBack));
        contentValues.put(KEY_CELL_IN_4G, Integer.valueOf(this.cells4G));
        contentValues.put(KEY_REF_CNT, Integer.valueOf(this.refCnt));
        contentValues.put(KEY_UNKNOWN_DB, Integer.valueOf(this.unknownDb));
        contentValues.put(KEY_UNKNOWN_SPACE, Integer.valueOf(this.unknownSpace));
        LogUtil.i(false, "insertRecordByLoc in Back2LteChrTable location:%{private}s:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d:%{public}d", this.location, Integer.valueOf(this.lowRatCnt), Integer.valueOf(this.inLteCnt), Integer.valueOf(this.outLteCnt), Integer.valueOf(this.fastBack), Integer.valueOf(this.successBack), Integer.valueOf(this.cells4G), Integer.valueOf(this.refCnt), Integer.valueOf(this.unknownDb), Integer.valueOf(this.unknownSpace));
        try {
            this.db.beginTransaction();
            this.db.insert(Constant.FASTBACK2LTECHR_NAME, null, contentValues);
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
            this.db.execSQL("UPDATE CHR_FASTBACK2LTE SET LOWRATCNT = 0, INLTECNT = 0, OUTLTECNT = 0, FASTBACK = 0, SUCCESSBACK = 0, CELLS4G = 0, REFCNT = 0, UNKNOWNDB = 0, UNKNOWNSPACE = 0 WHERE FREQLOCNAME = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "resetRecord by loc of STA_BACK2LTE exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
            resetFastBack2LteCount();
            resetFastBack2LteSumCount();
        }
    }

    public boolean resetRecord() {
        if (!getTotalCounters()) {
            return false;
        }
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FASTBACK2LTE SET LOWRATCNT = 0, INLTECNT = 0, OUTLTECNT = 0, FASTBACK = 0, SUCCESSBACK = 0, CELLS4G = 0, REFCNT = 0, UNKNOWNDB = 0, UNKNOWNSPACE = 0", null);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "resetRecord of STA_BACK2LTE exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
            resetFastBack2LteCount();
            resetFastBack2LteSumCount();
        }
    }

    public boolean delRecord() {
        try {
            this.db.beginTransaction();
            this.db.execSQL("DELETE FROM CHR_FASTBACK2LTE", null);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "delRecord of STA_BACK2LTE exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public void addLowRatCnt() {
        this.lowRatCnt++;
    }

    public int getLowRatCnt() {
        return this.lowRatCnt;
    }

    public void addInLteCnt() {
        if (this.session3gState == 1) {
            this.inLteCnt++;
        }
    }

    public int getInLteCnt() {
        return this.inLteCnt;
    }

    public void addOutLteCnt() {
        if (this.session3gState == 1) {
            this.outLteCnt++;
        }
    }

    public int getOutLteCnt() {
        return this.outLteCnt;
    }

    public void addFastBack() {
        if (this.session3gState == 1) {
            this.fastBack++;
        }
    }

    public int getFastBack() {
        return this.fastBack;
    }

    public void addSuccessBack() {
        if (this.session3gState == 2) {
            this.successBack++;
        }
    }

    public int getSuccessBack() {
        return this.successBack;
    }

    public void setCells4G(int num) {
        if (this.session3gState == 1) {
            this.cells4G += num;
        }
    }

    public int getCells4G() {
        return this.cells4G;
    }

    public void addQueryCnt() {
        this.refCnt++;
    }

    public int getRefCnt() {
        return this.refCnt;
    }

    public void addUnknownDb() {
        if (this.session3gState == 1) {
            this.unknownDb++;
        }
    }

    public int getUnknown2Db() {
        return this.unknownDb;
    }

    public void addUnknownSpace() {
        if (this.session3gState == 1) {
            this.unknownSpace++;
        }
    }

    public int getUnknown2Space() {
        return this.unknownSpace;
    }

    public void setLocation(String location2) {
        if (location2 != null) {
            this.location = location2;
            resetFastBack2LteCount();
        }
    }

    public String getLocation() {
        return this.location;
    }

    public int getSumLowRatCnt() {
        return this.sumLowRatCnt;
    }

    public int getSumInLteCnt() {
        return this.sumInLteCnt;
    }

    public int getSumOutLteCnt() {
        return this.sumOutLteCnt;
    }

    public int getSumFastBack() {
        return this.sumFastBack;
    }

    public int getSumSuccessBack() {
        return this.sumSuccessBack;
    }

    public int getSumCells4G() {
        return this.sumCells4G;
    }

    public int getSumRefCnt() {
        return this.sumRefCnt;
    }

    public int getSumUnknownDb() {
        return this.sumUnknownDb;
    }

    public int getSumUnknownSpace() {
        return this.sumUnknownSpace;
    }

    public void startSession() {
        this.session3gState = 1;
    }

    public void endSession() {
        this.session3gState = 0;
    }

    public void waitSession() {
        this.session3gState = 2;
    }

    public void resetSession() {
        if (this.session3gState == 1) {
            this.refCnt--;
        }
        this.session3gState = 0;
    }

    public boolean sessionSpace(int allAp, int mainAp) {
        boolean isDiffSpace = false;
        if (allAp != this.sessionSpaceAll) {
            isDiffSpace = true;
        }
        if (mainAp != this.sessionSpaceMain) {
            isDiffSpace = true;
        }
        this.sessionSpaceAll = allAp;
        this.sessionSpaceMain = mainAp;
        return isDiffSpace;
    }
}
