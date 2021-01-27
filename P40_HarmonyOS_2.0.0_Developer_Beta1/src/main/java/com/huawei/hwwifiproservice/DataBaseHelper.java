package com.huawei.hwwifiproservice;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.wifi.HwHiLog;

public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String APINFO_TABLE_NAME = "APInfoTable";
    public static final String BSSID_TABLE_NAME = "BSSIDTable";
    public static final String CELLID_TABLE_NAME = "CELLIDTable";
    public static final String DATABASE_NAME = "wifipro.db";
    public static final int DATABASE_VERSION = 8;
    private static final String TAG = "DataBaseHelper";

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 8);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase db) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE [BSSIDTable] (");
        buffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        buffer.append("[bssid] TEXT,");
        buffer.append("[ssid] TEXT,");
        buffer.append("[inbacklist] INTEGER,");
        buffer.append("[authtype] INTEGER,");
        buffer.append("[time] LONG,");
        buffer.append("[isHome] INTEGER,");
        buffer.append("[frequency] INTEGER)");
        db.execSQL(buffer.toString());
        StringBuffer buffer2 = new StringBuffer();
        buffer2.append("CREATE TABLE [APInfoTable] (");
        buffer2.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        buffer2.append("[bssid] TEXT,");
        buffer2.append("[nearbybssid] TEXT)");
        db.execSQL(buffer2.toString());
        StringBuffer buffer3 = new StringBuffer();
        buffer3.append("CREATE TABLE [CELLIDTable] (");
        buffer3.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        buffer3.append("[bssid] TEXT,");
        buffer3.append("[cellid] TEXT,");
        buffer3.append("[rssi] INTEGER)");
        db.execSQL(buffer3.toString());
        HwHiLog.i(TAG, false, "wifipro.db : onCreate()", new Object[0]);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS BSSIDTable");
        db.execSQL("DROP TABLE IF EXISTS APInfoTable");
        db.execSQL("DROP TABLE IF EXISTS CELLIDTable");
        onCreate(db);
        HwHiLog.i(TAG, false, "wifipro.db : onUpgrade()", new Object[0]);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS BSSIDTable");
        db.execSQL("DROP TABLE IF EXISTS APInfoTable");
        db.execSQL("DROP TABLE IF EXISTS CELLIDTable");
        onCreate(db);
        HwHiLog.i(TAG, false, "wifipro.db : onDowngrade()", new Object[0]);
    }
}
