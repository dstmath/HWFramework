package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String APINFO_TABLE_NAME = "APInfoTable";
    public static final String BSSID_TABLE_NAME = "BSSIDTable";
    public static final String CELLID_TABLE_NAME = "CELLIDTable";
    public static final String DATABASE_NAME = "/data/system/wifipro.db";
    public static final int DATABASE_VERSION = 6;

    public DataBaseHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 6);
    }

    public void onCreate(SQLiteDatabase db) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE [BSSIDTable] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[bssid] TEXT,");
        sBuffer.append("[ssid] TEXT,");
        sBuffer.append("[inbacklist] INTEGER,");
        sBuffer.append("[authtype] INTEGER,");
        sBuffer.append("[time] LONG)");
        db.execSQL(sBuffer.toString());
        sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE [APInfoTable] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[bssid] TEXT,");
        sBuffer.append("[nearbybssid] TEXT)");
        db.execSQL(sBuffer.toString());
        sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE [CELLIDTable] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[bssid] TEXT,");
        sBuffer.append("[cellid] TEXT,");
        sBuffer.append("[rssi] INTEGER)");
        db.execSQL(sBuffer.toString());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS BSSIDTable");
        db.execSQL("DROP TABLE IF EXISTS APInfoTable");
        db.execSQL("DROP TABLE IF EXISTS CELLIDTable");
        onCreate(db);
    }
}
