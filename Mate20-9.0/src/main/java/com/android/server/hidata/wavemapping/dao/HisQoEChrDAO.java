package com.android.server.hidata.wavemapping.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.util.LogUtil;

public class HisQoEChrDAO {
    private static final String TAG = ("WMapping." + HisQoEChrDAO.class.getSimpleName());
    private SQLiteDatabase db = DatabaseSingleton.getInstance();
    private String freqLocation = "UNKNOWN";
    private int hQoeGoodCnt = 0;
    private int hQoePoorCnt = 0;
    private int hQoeQueryCnt = 0;
    private int hQoeUnknownDB = 0;
    private int hQoeUnknownSpace = 0;

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0099, code lost:
        if (r4 == null) goto L_0x009c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x005b, code lost:
        if (r4 != null) goto L_0x005d;
     */
    public boolean getCountersByLocation() {
        String[] args = {this.freqLocation};
        boolean found = false;
        Cursor cursor = null;
        if (this.db == null) {
            return false;
        }
        try {
            cursor = this.db.rawQuery("SELECT QUERYCNT, GOODCNT, POORCNT, UNKNOWNDB, UNKNOWNSPACE FROM CHR_HISTQOERPT WHERE FREQLOCNAME = ?", args);
            if (cursor.moveToNext()) {
                found = true;
                this.hQoeQueryCnt = cursor.getInt(cursor.getColumnIndexOrThrow("QUERYCNT"));
                this.hQoeGoodCnt = cursor.getInt(cursor.getColumnIndexOrThrow("GOODCNT"));
                this.hQoePoorCnt = cursor.getInt(cursor.getColumnIndexOrThrow("POORCNT"));
                this.hQoeUnknownDB = cursor.getInt(cursor.getColumnIndexOrThrow("UNKNOWNDB"));
                this.hQoeUnknownSpace = cursor.getInt(cursor.getColumnIndexOrThrow("UNKNOWNSPACE"));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("Argument getCntNumByLoc in HistQoeChrTable IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("exception getCntNumByLoc in HistQoeChrTable Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            LogUtil.i("getCntNumByLoc in HistQoeChrTable found:" + found + " location:" + this.freqLocation + ":" + this.hQoeQueryCnt + ":" + this.hQoeGoodCnt + ":" + this.hQoePoorCnt + ":" + this.hQoeUnknownDB + ":" + this.hQoeUnknownSpace);
            return found;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x009a, code lost:
        if (r4 == null) goto L_0x009d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x005c, code lost:
        if (r4 != null) goto L_0x005e;
     */
    public boolean getCountersByLocation(String location) {
        String[] args = {location};
        boolean found = false;
        Cursor cursor = null;
        if (this.db == null || location == null) {
            return false;
        }
        try {
            cursor = this.db.rawQuery("SELECT QUERYCNT, GOODCNT, POORCNT, UNKNOWNDB, UNKNOWNSPACE FROM CHR_HISTQOERPT WHERE FREQLOCNAME = ?", args);
            if (cursor.moveToNext()) {
                found = true;
                this.hQoeQueryCnt = cursor.getInt(cursor.getColumnIndexOrThrow("QUERYCNT"));
                this.hQoeGoodCnt = cursor.getInt(cursor.getColumnIndexOrThrow("GOODCNT"));
                this.hQoePoorCnt = cursor.getInt(cursor.getColumnIndexOrThrow("POORCNT"));
                this.hQoeUnknownDB = cursor.getInt(cursor.getColumnIndexOrThrow("UNKNOWNDB"));
                this.hQoeUnknownSpace = cursor.getInt(cursor.getColumnIndexOrThrow("UNKNOWNSPACE"));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("getCountersByLocation IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("getCountersByLocation Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            LogUtil.i("getCountersByLocation in HistQoeChrTable found:" + found + " location:" + location + ":" + this.hQoeQueryCnt + ":" + this.hQoeGoodCnt + ":" + this.hQoePoorCnt + ":" + this.hQoeUnknownDB + ":" + this.hQoeUnknownSpace);
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
        String[] args = {this.freqLocation};
        boolean found = false;
        Cursor cursor = null;
        if (this.db == null) {
            return false;
        }
        try {
            cursor = this.db.rawQuery("SELECT * FROM CHR_HISTQOERPT WHERE FREQLOCNAME = ?", args);
            if (cursor.moveToNext()) {
                found = true;
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("Argument getRecordByLoc in HistQoeChrTable IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("exception getRecordByLoc in HistQoeChrTable Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            LogUtil.i("getRecordByLoc in HistQoeChrTable found:" + found);
            return found;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public boolean updateRecordByLoc() {
        Object[] args = {Integer.valueOf(this.hQoeQueryCnt), Integer.valueOf(this.hQoeGoodCnt), Integer.valueOf(this.hQoePoorCnt), Integer.valueOf(this.hQoeUnknownDB), Integer.valueOf(this.hQoeUnknownSpace), this.freqLocation};
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE CHR_HISTQOERPT SET QUERYCNT = ?, GOODCNT = ?, POORCNT = ?, UNKNOWNDB = ?, UNKNOWNSPACE  = ? WHERE FREQLOCNAME = ?", args);
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
        cValueBase.put("FREQLOCNAME", this.freqLocation);
        cValueBase.put("QUERYCNT", Integer.valueOf(this.hQoeQueryCnt));
        cValueBase.put("GOODCNT", Integer.valueOf(this.hQoeGoodCnt));
        cValueBase.put("POORCNT", Integer.valueOf(this.hQoePoorCnt));
        cValueBase.put("UNKNOWNDB", Integer.valueOf(this.hQoeUnknownDB));
        cValueBase.put("UNKNOWNSPACE", Integer.valueOf(this.hQoeUnknownSpace));
        LogUtil.i("insertRecordByLoc in HistQoeChrTable  location:" + this.freqLocation + ":" + this.hQoeQueryCnt + ":" + this.hQoeGoodCnt + ":" + this.hQoePoorCnt + ":" + this.hQoeUnknownDB + ":" + this.hQoeUnknownSpace);
        try {
            this.db.beginTransaction();
            this.db.insert(Constant.CHR_HISTQOERPT, null, cValueBase);
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
            this.db.execSQL("UPDATE CHR_HISTQOERPT SET QUERYCNT = 0, GOODCNT = 0, POORCNT = 0, DATARX = 0, DATATX = 0, UNKNOWNDB = 0, UNKNOWNSPACE = 0 WHERE FREQLOCNAME = ? ", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("resetRecord by loc of CHR_HISTQOERPT exception: " + e.getMessage());
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
            this.db.endTransaction();
            return true;
        } catch (SQLException e) {
            LogUtil.e("delRecord of delRecord exception: " + e.getMessage());
            this.db.endTransaction();
            return false;
        } catch (Throwable th) {
            this.db.endTransaction();
            throw th;
        }
    }

    public void resetChrCnt() {
        this.hQoeQueryCnt = 0;
        this.hQoeGoodCnt = 0;
        this.hQoePoorCnt = 0;
        this.hQoeUnknownDB = 0;
        this.hQoeUnknownSpace = 0;
    }

    public void accQueryCnt() {
        this.hQoeQueryCnt++;
    }

    public int getQueryCnt() {
        return this.hQoeQueryCnt;
    }

    public void accGoodCnt() {
        this.hQoeGoodCnt++;
    }

    public int getGoodCnt() {
        return this.hQoeGoodCnt;
    }

    public void accPoorCnt() {
        this.hQoePoorCnt++;
    }

    public int getPoorCnt() {
        return this.hQoePoorCnt;
    }

    public void accUnknownDB() {
        this.hQoeUnknownDB++;
    }

    public int getUnknownDB() {
        return this.hQoeUnknownDB;
    }

    public void accUnknownSpace() {
        this.hQoeUnknownSpace++;
    }

    public int getUnknownSpace() {
        return this.hQoeUnknownSpace;
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
