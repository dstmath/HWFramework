package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String APINFO_TABLE_NAME = "APInfoTable";
    public static final String BSSID_TABLE_NAME = "BSSIDTable";
    public static final String CELLID_TABLE_NAME = "CELLIDTable";
    public static final String DATABASE_NAME = "/data/system/wifipro.db";
    public static final int DATABASE_VERSION = 7;

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 7);
    }

    public void onCreate(SQLiteDatabase db) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE [BSSIDTable] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[bssid] TEXT,");
        sBuffer.append("[ssid] TEXT,");
        sBuffer.append("[inbacklist] INTEGER,");
        sBuffer.append("[authtype] INTEGER,");
        sBuffer.append("[time] LONG,");
        sBuffer.append("[isHome] INTEGER)");
        db.execSQL(sBuffer.toString());
        StringBuffer sBuffer2 = new StringBuffer();
        sBuffer2.append("CREATE TABLE [APInfoTable] (");
        sBuffer2.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer2.append("[bssid] TEXT,");
        sBuffer2.append("[nearbybssid] TEXT)");
        db.execSQL(sBuffer2.toString());
        StringBuffer sBuffer3 = new StringBuffer();
        sBuffer3.append("CREATE TABLE [CELLIDTable] (");
        sBuffer3.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer3.append("[bssid] TEXT,");
        sBuffer3.append("[cellid] TEXT,");
        sBuffer3.append("[rssi] INTEGER)");
        db.execSQL(sBuffer3.toString());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS BSSIDTable");
        db.execSQL("DROP TABLE IF EXISTS APInfoTable");
        db.execSQL("DROP TABLE IF EXISTS CELLIDTable");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS BSSIDTable");
        db.execSQL("DROP TABLE IF EXISTS APInfoTable");
        db.execSQL("DROP TABLE IF EXISTS CELLIDTable");
        onCreate(db);
    }
}
