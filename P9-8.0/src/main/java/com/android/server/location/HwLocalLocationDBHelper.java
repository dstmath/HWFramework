package com.android.server.location;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class HwLocalLocationDBHelper extends SQLiteOpenHelper {
    static final byte[] C3 = new byte[]{(byte) -9, (byte) -86, (byte) 60, (byte) -113, (byte) 122, (byte) -7, (byte) -55, (byte) 69, (byte) 23, (byte) 119, (byte) 87, (byte) -83, (byte) 89, (byte) -1, (byte) -113, (byte) 29};
    private static final String TAG = "HwLocalLocationProvider";
    private SQLiteDatabase mSqLiteDatabase;

    public HwLocalLocationDBHelper(Context context) {
        super(context, HwLocalLocationManager.LOCATION_DB_NAME, null, 2);
    }

    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(HwLocalLocationManager.CREATE_CELLID_TABLE);
            db.execSQL(HwLocalLocationManager.CREATE_BSSID_TABLE);
        } catch (SQLException e) {
            Log.e("HwLocalLocationProvider", e.getMessage());
        }
        Log.i("HwLocalLocationProvider", "Path = " + db.getPath());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS cell_fix_info");
            db.execSQL("DROP TABLE IF EXISTS bssid_fix_info");
        } catch (SQLException e) {
            Log.e("HwLocalLocationProvider", e.getMessage());
        }
        onCreate(db);
    }

    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    public long insert(String table, ContentValues values) {
        this.mSqLiteDatabase = getWritableDatabase();
        return this.mSqLiteDatabase.insert(table, null, values);
    }

    public int delete(String table, String whereClause, String[] whereArgs) {
        this.mSqLiteDatabase = getWritableDatabase();
        return this.mSqLiteDatabase.delete(table, whereClause, whereArgs);
    }

    public void execSQL(String sql) {
        this.mSqLiteDatabase = getWritableDatabase();
        try {
            this.mSqLiteDatabase.execSQL(sql);
        } catch (SQLException e) {
            Log.e("HwLocalLocationProvider", e.getMessage());
        }
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        this.mSqLiteDatabase = getWritableDatabase();
        return this.mSqLiteDatabase.update(table, values, whereClause, whereArgs);
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        this.mSqLiteDatabase = getReadableDatabase();
        return this.mSqLiteDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs) {
        this.mSqLiteDatabase = getReadableDatabase();
        return this.mSqLiteDatabase.query(table, columns, selection, selectionArgs, null, null, null, null);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        this.mSqLiteDatabase = getReadableDatabase();
        return this.mSqLiteDatabase.rawQuery(sql, selectionArgs);
    }

    public void closedb() {
        if (this.mSqLiteDatabase != null && this.mSqLiteDatabase.isOpen()) {
            this.mSqLiteDatabase.close();
        }
    }

    public void beginTransaction() {
        this.mSqLiteDatabase = getWritableDatabase();
        this.mSqLiteDatabase.beginTransaction();
    }

    public void setTransactionSuccessful() {
        this.mSqLiteDatabase = getWritableDatabase();
        this.mSqLiteDatabase.setTransactionSuccessful();
    }

    public void endTransaction() {
        this.mSqLiteDatabase = getWritableDatabase();
        this.mSqLiteDatabase.endTransaction();
    }
}
