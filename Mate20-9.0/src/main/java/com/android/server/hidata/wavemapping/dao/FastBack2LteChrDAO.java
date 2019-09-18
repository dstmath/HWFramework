package com.android.server.hidata.wavemapping.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.Arrays;

public class FastBack2LteChrDAO {
    private static final int SESSION3GEND = 0;
    private static final int SESSION3GSTART = 1;
    private static final int SESSION3GWAIT = 2;
    private static final String TAG = ("WMapping." + FastBack2LteChrDAO.class.getSimpleName());
    private int cells4G = 0;
    private SQLiteDatabase db = DatabaseSingleton.getInstance();
    private int fastBack = 0;
    private int inLteCnt = 0;
    private String location = "UNKNOWN";
    private int lowRatCnt = 0;
    private int outLteCnt = 0;
    private int refCnt = 0;
    private int session3gState = 0;
    private int sessionSpace_all = -1;
    private int sessionSpace_main = -1;
    private int successBack = 0;
    private int sumcells4G = 0;
    private int sumfastBack = 0;
    private int suminLteCnt = 0;
    private int sumlowRatCnt = 0;
    private int sumoutLteCnt = 0;
    private int sumrefCnt = 0;
    private int sumsuccessBack = 0;
    private int sumunknownDB = 0;
    private int sumunknownSpace = 0;
    private int unknownDB = 0;
    private int unknownSpace = 0;

    public void resetFastBack2LteSumCount() {
        this.sumlowRatCnt = 0;
        this.suminLteCnt = 0;
        this.sumoutLteCnt = 0;
        this.sumfastBack = 0;
        this.sumsuccessBack = 0;
        this.sumcells4G = 0;
        this.sumrefCnt = 0;
        this.sumunknownDB = 0;
        this.sumunknownSpace = 0;
    }

    public void resetFastBack2LteCount() {
        this.lowRatCnt = 0;
        this.inLteCnt = 0;
        this.outLteCnt = 0;
        this.fastBack = 0;
        this.successBack = 0;
        this.cells4G = 0;
        this.refCnt = 0;
        this.unknownDB = 0;
        this.unknownSpace = 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00e3, code lost:
        if (r2 == null) goto L_0x00e6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x00a5, code lost:
        if (r2 != null) goto L_0x00a7;
     */
    public boolean getTotalCounters() {
        boolean found = false;
        Cursor cursor = null;
        if (this.db == null) {
            return false;
        }
        try {
            resetFastBack2LteSumCount();
            cursor = this.db.rawQuery("SELECT LOWRATCNT, INLTECNT, OUTLTECNT, FASTBACK, SUCCESSBACK, CELLS4G, REFCNT, UNKNOWNDB, UNKNOWNSPACE FROM CHR_FASTBACK2LTE", null);
            while (cursor.moveToNext()) {
                found = true;
                this.sumlowRatCnt += cursor.getInt(cursor.getColumnIndexOrThrow("LOWRATCNT"));
                this.suminLteCnt += cursor.getInt(cursor.getColumnIndexOrThrow("INLTECNT"));
                this.sumoutLteCnt += cursor.getInt(cursor.getColumnIndexOrThrow("OUTLTECNT"));
                this.sumfastBack += cursor.getInt(cursor.getColumnIndexOrThrow("FASTBACK"));
                this.sumsuccessBack += cursor.getInt(cursor.getColumnIndexOrThrow("SUCCESSBACK"));
                this.sumcells4G += cursor.getInt(cursor.getColumnIndexOrThrow("CELLS4G"));
                this.sumrefCnt += cursor.getInt(cursor.getColumnIndexOrThrow("REFCNT"));
                this.sumunknownDB += cursor.getInt(cursor.getColumnIndexOrThrow("UNKNOWNDB"));
                this.sumunknownSpace += cursor.getInt(cursor.getColumnIndexOrThrow("UNKNOWNSPACE"));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("Argument getTotalCounters in Back2LteChrTable IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("exception getTotalCounters in Back2LteChrTable Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            LogUtil.d("getTotalCounters in Back2LteChrTable found:" + found + ":" + this.sumlowRatCnt + ":" + this.suminLteCnt + ":" + this.sumoutLteCnt + ":" + this.sumfastBack + ":" + this.sumsuccessBack + ":" + this.sumcells4G + ":" + this.sumrefCnt + ":" + this.sumunknownDB + ":" + this.sumunknownSpace);
            return found;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x00ca, code lost:
        if (r4 == null) goto L_0x00cd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x008c, code lost:
        if (r4 != null) goto L_0x008e;
     */
    public boolean getCountersByLocation(String loc) {
        String[] args = {loc};
        boolean found = false;
        Cursor cursor = null;
        if (this.db == null) {
            return false;
        }
        try {
            resetFastBack2LteCount();
            cursor = this.db.rawQuery("SELECT LOWRATCNT, INLTECNT, OUTLTECNT, FASTBACK, SUCCESSBACK, CELLS4G, REFCNT, UNKNOWNDB, UNKNOWNSPACE FROM CHR_FASTBACK2LTE WHERE FREQLOCNAME = ?", args);
            if (cursor.moveToNext()) {
                found = true;
                this.lowRatCnt = cursor.getInt(cursor.getColumnIndexOrThrow("LOWRATCNT"));
                this.inLteCnt = cursor.getInt(cursor.getColumnIndexOrThrow("INLTECNT"));
                this.outLteCnt = cursor.getInt(cursor.getColumnIndexOrThrow("OUTLTECNT"));
                this.fastBack = cursor.getInt(cursor.getColumnIndexOrThrow("FASTBACK"));
                this.successBack = cursor.getInt(cursor.getColumnIndexOrThrow("SUCCESSBACK"));
                this.cells4G = cursor.getInt(cursor.getColumnIndexOrThrow("CELLS4G"));
                this.refCnt = cursor.getInt(cursor.getColumnIndexOrThrow("REFCNT"));
                this.unknownDB = cursor.getInt(cursor.getColumnIndexOrThrow("UNKNOWNDB"));
                this.unknownSpace = cursor.getInt(cursor.getColumnIndexOrThrow("UNKNOWNSPACE"));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("Argument getCountersByLocation in Back2LteChrTable IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("exception getCountersByLocation in Back2LteChrTable Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            LogUtil.d("getCountersByLocation in Back2LteChrTable found:" + found + " location:" + this.location + ":" + this.lowRatCnt + ":" + this.inLteCnt + ":" + this.outLteCnt + ":" + this.fastBack + ":" + this.successBack + ":" + this.cells4G + ":" + this.refCnt + ":" + this.unknownDB + ":" + this.unknownSpace);
            return found;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x00c9, code lost:
        if (r4 == null) goto L_0x00cc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x008b, code lost:
        if (r4 != null) goto L_0x008d;
     */
    public boolean getCountersByLocation() {
        String[] args = {this.location};
        boolean found = false;
        Cursor cursor = null;
        if (this.db == null) {
            return false;
        }
        try {
            cursor = this.db.rawQuery("SELECT LOWRATCNT, INLTECNT, OUTLTECNT, FASTBACK, SUCCESSBACK, CELLS4G, REFCNT, UNKNOWNDB, UNKNOWNSPACE FROM CHR_FASTBACK2LTE WHERE FREQLOCNAME = ?", args);
            if (cursor.moveToNext()) {
                found = true;
                this.lowRatCnt = cursor.getInt(cursor.getColumnIndexOrThrow("LOWRATCNT"));
                this.inLteCnt = cursor.getInt(cursor.getColumnIndexOrThrow("INLTECNT"));
                this.outLteCnt = cursor.getInt(cursor.getColumnIndexOrThrow("OUTLTECNT"));
                this.fastBack = cursor.getInt(cursor.getColumnIndexOrThrow("FASTBACK"));
                this.successBack = cursor.getInt(cursor.getColumnIndexOrThrow("SUCCESSBACK"));
                this.cells4G = cursor.getInt(cursor.getColumnIndexOrThrow("CELLS4G"));
                this.refCnt = cursor.getInt(cursor.getColumnIndexOrThrow("REFCNT"));
                this.unknownDB = cursor.getInt(cursor.getColumnIndexOrThrow("UNKNOWNDB"));
                this.unknownSpace = cursor.getInt(cursor.getColumnIndexOrThrow("UNKNOWNSPACE"));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("Argument getCountersByLocation in Back2LteChrTable IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("exception getCountersByLocation in Back2LteChrTable Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            LogUtil.d("getCountersByLocation in Back2LteChrTable found:" + found + " location:" + this.location + ":" + this.lowRatCnt + ":" + this.inLteCnt + ":" + this.outLteCnt + ":" + this.fastBack + ":" + this.successBack + ":" + this.cells4G + ":" + this.refCnt + ":" + this.unknownDB + ":" + this.unknownSpace);
            return found;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005c, code lost:
        if (r4 == null) goto L_0x005f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x001f, code lost:
        if (r4 != null) goto L_0x0021;
     */
    public boolean getRecordByLoc() {
        String[] args = {this.location};
        boolean found = false;
        Cursor cursor = null;
        if (this.db == null) {
            return false;
        }
        try {
            cursor = this.db.rawQuery("SELECT LOWRATCNT, INLTECNT, OUTLTECNT, FASTBACK, SUCCESSBACK, CELLS4G, REFCNT, UNKNOWNDB, UNKNOWNSPACE FROM CHR_FASTBACK2LTE WHERE FREQLOCNAME = ?", args);
            if (cursor.moveToNext()) {
                found = true;
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("Argument getRecordByLoc in Back2LteChrTable IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("exception getRecordByLoc in Back2LteChrTable Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            LogUtil.i("getRecordByLoc in Back2LteChrTable found:" + found);
            return found;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public boolean updateRecordByLoc() {
        Object[] args = {Integer.valueOf(this.lowRatCnt), Integer.valueOf(this.inLteCnt), Integer.valueOf(this.outLteCnt), Integer.valueOf(this.fastBack), Integer.valueOf(this.successBack), Integer.valueOf(this.cells4G), Integer.valueOf(this.refCnt), Integer.valueOf(this.unknownDB), Integer.valueOf(this.unknownSpace), this.location};
        LogUtil.i("updateRecordByLoc: " + Arrays.toString(Arrays.copyOfRange(args, 1, args.length)));
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FASTBACK2LTE SET LOWRATCNT = ?, INLTECNT = ?, OUTLTECNT = ?, FASTBACK = ?, SUCCESSBACK = ?, CELLS4G = ?, REFCNT = ?, UNKNOWNDB = ?, UNKNOWNSPACE  = ? WHERE FREQLOCNAME = ?", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("updateRecordByLoc exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean insertRecordByLoc() {
        if (getRecordByLoc()) {
            return updateRecordByLoc();
        }
        ContentValues cValueBase = new ContentValues();
        cValueBase.put("FREQLOCNAME", this.location);
        cValueBase.put("LOWRATCNT", Integer.valueOf(this.lowRatCnt));
        cValueBase.put("INLTECNT", Integer.valueOf(this.inLteCnt));
        cValueBase.put("OUTLTECNT", Integer.valueOf(this.outLteCnt));
        cValueBase.put("FASTBACK", Integer.valueOf(this.fastBack));
        cValueBase.put("SUCCESSBACK", Integer.valueOf(this.successBack));
        cValueBase.put("CELLS4G", Integer.valueOf(this.cells4G));
        cValueBase.put("REFCNT", Integer.valueOf(this.refCnt));
        cValueBase.put("UNKNOWNDB", Integer.valueOf(this.unknownDB));
        cValueBase.put("UNKNOWNSPACE", Integer.valueOf(this.unknownSpace));
        LogUtil.i("insertRecordByLoc in Back2LteChrTable  location:" + this.location + ":" + this.lowRatCnt + ":" + this.inLteCnt + ":" + this.outLteCnt + ":" + this.fastBack + ":" + this.successBack + ":" + this.cells4G + ":" + this.refCnt + ":" + this.unknownDB + ":" + this.unknownSpace);
        try {
            this.db.beginTransaction();
            this.db.insert(Constant.FASTBACK2LTECHR_NAME, null, cValueBase);
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
            return true;
        } catch (Exception e) {
            LogUtil.e("insertRecordByLoc exception: " + e.getMessage());
            this.db.endTransaction();
            return false;
        } catch (Throwable th) {
            this.db.endTransaction();
            throw th;
        }
    }

    public boolean resetRecord(String loc) {
        String[] args = {loc};
        if (!getCountersByLocation(loc)) {
            return false;
        }
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_FASTBACK2LTE SET LOWRATCNT = 0, INLTECNT = 0, OUTLTECNT = 0, FASTBACK = 0, SUCCESSBACK = 0, CELLS4G = 0, REFCNT = 0, UNKNOWNDB = 0, UNKNOWNSPACE  = 0 WHERE FREQLOCNAME = ? ", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("resetRecord by loc of STA_BACK2LTE exception: " + e.getMessage());
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
            this.db.execSQL("UPDATE CHR_FASTBACK2LTE SET LOWRATCNT = 0, INLTECNT = 0, OUTLTECNT = 0, FASTBACK = 0, SUCCESSBACK = 0, CELLS4G = 0, REFCNT = 0, UNKNOWNDB = 0, UNKNOWNSPACE  = 0", null);
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
            resetFastBack2LteCount();
            resetFastBack2LteSumCount();
            return true;
        } catch (SQLException e) {
            LogUtil.e("resetRecord of STA_BACK2LTE exception: " + e.getMessage());
            this.db.endTransaction();
            resetFastBack2LteCount();
            resetFastBack2LteSumCount();
            return false;
        } catch (Throwable th) {
            this.db.endTransaction();
            resetFastBack2LteCount();
            resetFastBack2LteSumCount();
            throw th;
        }
    }

    public boolean delRecord() {
        try {
            this.db.beginTransaction();
            this.db.execSQL("DELETE FROM CHR_FASTBACK2LTE", null);
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
            return true;
        } catch (SQLException e) {
            LogUtil.e("delRecord of STA_BACK2LTE exception: " + e.getMessage());
            this.db.endTransaction();
            return false;
        } catch (Throwable th) {
            this.db.endTransaction();
            throw th;
        }
    }

    public void addlowRatCnt() {
        this.lowRatCnt++;
    }

    public int getlowRatCnt() {
        return this.lowRatCnt;
    }

    public void addinLteCnt() {
        if (1 == this.session3gState) {
            this.inLteCnt++;
        }
    }

    public int getinLteCnt() {
        return this.inLteCnt;
    }

    public void addoutLteCnt() {
        if (1 == this.session3gState) {
            this.outLteCnt++;
        }
    }

    public int getoutLteCnt() {
        return this.outLteCnt;
    }

    public void addfastBack() {
        if (1 == this.session3gState) {
            this.fastBack++;
        }
    }

    public int getfastBack() {
        return this.fastBack;
    }

    public void addsuccessBack() {
        if (2 == this.session3gState) {
            this.successBack++;
        }
    }

    public int getsuccessBack() {
        return this.successBack;
    }

    public void setcells4G(int num) {
        if (1 == this.session3gState) {
            this.cells4G += num;
        }
    }

    public int getcells4G() {
        return this.cells4G;
    }

    public void addQueryCnt() {
        this.refCnt++;
    }

    public int getrefCnt() {
        return this.refCnt;
    }

    public void addunknownDB() {
        if (1 == this.session3gState) {
            this.unknownDB++;
        }
    }

    public int getUnknown2DB() {
        return this.unknownDB;
    }

    public void addunknownSpace() {
        if (1 == this.session3gState) {
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

    public int getSumlowRatCnt() {
        return this.sumlowRatCnt;
    }

    public int getSuminLteCnt() {
        return this.suminLteCnt;
    }

    public int getSumoutLteCnt() {
        return this.sumoutLteCnt;
    }

    public int getSumfastBack() {
        return this.sumfastBack;
    }

    public int getSumsuccessBack() {
        return this.sumsuccessBack;
    }

    public int getSumcells4G() {
        return this.sumcells4G;
    }

    public int getSumrefCnt() {
        return this.sumrefCnt;
    }

    public int getSumunknownDB() {
        return this.sumunknownDB;
    }

    public int getSumunknownSpace() {
        return this.sumunknownSpace;
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
        if (1 == this.session3gState) {
            this.refCnt--;
        }
        this.session3gState = 0;
    }

    public boolean sessionSpace(int allAp, int mainAp) {
        boolean diffSpace = false;
        if (allAp != this.sessionSpace_all) {
            diffSpace = true;
        }
        if (mainAp != this.sessionSpace_main) {
            diffSpace = true;
        }
        this.sessionSpace_all = allAp;
        this.sessionSpace_main = mainAp;
        return diffSpace;
    }
}
