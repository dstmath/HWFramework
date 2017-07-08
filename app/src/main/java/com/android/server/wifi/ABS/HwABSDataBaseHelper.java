package com.android.server.wifi.ABS;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class HwABSDataBaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "/data/system/HwABSDataBase.db";
    public static final int DATABASE_VERSION = 1;
    public static final String MIMO_AP_TABLE_NAME = "MIMOApInfoTable";

    public HwABSDataBaseHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public HwABSDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE [MIMOApInfoTable] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[bssid] TEXT,");
        sBuffer.append("[ssid] TEXT,");
        sBuffer.append("[switch_mimo_type] INTEGER,");
        sBuffer.append("[switch_siso_type] INTEGER,");
        sBuffer.append("[auth_type] INTEGER,");
        sBuffer.append("[in_back_list] INTEGER,");
        sBuffer.append("[mimo_time] LONG,");
        sBuffer.append("[siso_time] LONG,");
        sBuffer.append("[total_time] LONG,");
        sBuffer.append("[last_connect_time] LONG,");
        sBuffer.append("[Reserved] INTEGER)");
        db.execSQL(sBuffer.toString());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS MIMOApInfoTable");
        onCreate(db);
    }
}
