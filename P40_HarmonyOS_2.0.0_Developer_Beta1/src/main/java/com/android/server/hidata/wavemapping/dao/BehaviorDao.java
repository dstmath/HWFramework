package com.android.server.hidata.wavemapping.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BehaviorDao {
    private static final String BATCH_KEY = "BATCH";
    private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss.SSS";
    private static final String SELECT_PERFIX = "SELECT * FROM ";
    private static final String TAG = ("WMapping." + BehaviorDao.class.getSimpleName());
    private static final String UPDATE_TIME_KEY = "UPDATETIME";
    private SQLiteDatabase db = DatabaseSingleton.getInstance();

    public boolean insert(int batch) {
        if (getBatch() > 0) {
            update(batch);
            return true;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(UPDATE_TIME_KEY, new SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(new Date(System.currentTimeMillis())));
        contentValues.put(BATCH_KEY, Integer.valueOf(batch));
        try {
            this.db.beginTransaction();
            this.db.insert(Constant.BEHAVIOR_TABLE_NAME, null, contentValues);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "insert failed by Exception", new Object[0]);
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean update(int batch) {
        if (batch == 0) {
            LogUtil.d(false, "update failure,batch == 0", new Object[0]);
            return false;
        }
        Object[] args = {new SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(new Date(System.currentTimeMillis())), Integer.valueOf(batch)};
        LogUtil.i(false, "update begin:%{public}s", "UPDATE BEHAVIOR_MAINTAIN SET UPDATETIME = ?,BATCH = ?");
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE BEHAVIOR_MAINTAIN SET UPDATETIME = ?,BATCH = ?", args);
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
            this.db.execSQL("DELETE FROM BEHAVIOR_MAINTAIN", null);
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "remove exception: %{public}s", e.getMessage());
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0042, code lost:
        if (0 == 0) goto L_0x0045;
     */
    public int getBatch() {
        int batch = 0;
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return 0;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT * FROM BEHAVIOR_MAINTAIN", null);
            if (cursor.moveToNext()) {
                batch = cursor.getInt(cursor.getColumnIndex(BATCH_KEY));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "getBatch IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "getBatch failed by SQLException", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return batch;
    }
}
