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

public class BehaviorDAO {
    private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss.SSS";
    private static final String TAG = ("WMapping." + BehaviorDAO.class.getSimpleName());
    private SQLiteDatabase db = DatabaseSingleton.getInstance();

    public boolean insert(int batch) {
        if (getBatch() > 0) {
            update(batch);
            return true;
        }
        ContentValues cValue = new ContentValues();
        cValue.put("UPDATETIME", new SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(new Date(System.currentTimeMillis())));
        cValue.put("BATCH", Integer.valueOf(batch));
        try {
            this.db.beginTransaction();
            this.db.insert(Constant.BEHAVIOR_TABLE_NAME, null, cValue);
            this.db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            LogUtil.e("insert exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean update(int batch) {
        if (batch == 0) {
            LogUtil.d("update failure,batch == 0");
            return false;
        }
        Object[] args = {new SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(new Date(System.currentTimeMillis())), Integer.valueOf(batch)};
        LogUtil.i("update begin:" + "UPDATE BEHAVIOR_MAINTAIN SET UPDATETIME = ?,BATCH = ?");
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE BEHAVIOR_MAINTAIN SET UPDATETIME = ?,BATCH = ?", args);
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
            this.db.execSQL("DELETE FROM BEHAVIOR_MAINTAIN", null);
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
    public int getBatch() {
        int batch = 0;
        Cursor cursor = null;
        if (this.db == null) {
            return 0;
        }
        try {
            cursor = this.db.rawQuery("SELECT * FROM BEHAVIOR_MAINTAIN", null);
            if (cursor.moveToNext()) {
                batch = cursor.getInt(cursor.getColumnIndex("BATCH"));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("getBatch IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("getBatch Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            return batch;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }
}
