package com.android.server.wifi.ABS;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HwAbsDataBaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "/data/system/HwABSDataBase.db";
    public static final int DATABASE_VERSION = 6;
    public static final String MIMO_AP_TABLE_NAME = "MIMOApInfoTable";
    public static final String STATISTICS_TABLE_NAME = "StatisticsTable";

    public HwAbsDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public HwAbsDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 6);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase db) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE [MIMOApInfoTable] (");
        buffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        buffer.append("[bssid] TEXT,");
        buffer.append("[ssid] TEXT,");
        buffer.append("[switch_mimo_type] INTEGER,");
        buffer.append("[switch_siso_type] INTEGER,");
        buffer.append("[auth_type] INTEGER,");
        buffer.append("[in_black_list] INTEGER,");
        buffer.append("[in_vowifi_black_list] INTEGER,");
        buffer.append("[reassociate_times] INTEGER,");
        buffer.append("[failed_times] INTEGER,");
        buffer.append("[continuous_failure_times] INTEGER,");
        buffer.append("[last_connect_time] LONG,");
        buffer.append("[Reserved] INTEGER)");
        db.execSQL(buffer.toString());
        StringBuffer buffer2 = new StringBuffer();
        buffer2.append("CREATE TABLE [StatisticsTable] (");
        buffer2.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        buffer2.append("[long_connect_event] INTEGER,");
        buffer2.append("[short_connect_event] INTEGER,");
        buffer2.append("[search_event] INTEGER,");
        buffer2.append("[antenna_preempted_screen_on_event] INTEGER,");
        buffer2.append("[antenna_preempted_screen_off_event] INTEGER,");
        buffer2.append("[mo_mt_call_event] INTEGER,");
        buffer2.append("[siso_to_mimo_event] INTEGER,");
        buffer2.append("[ping_pong_times] INTEGER,");
        buffer2.append("[max_ping_pong_times] INTEGER,");
        buffer2.append("[mimo_time] LONG,");
        buffer2.append("[siso_time] LONG,");
        buffer2.append("[mimo_screen_on_time] LONG,");
        buffer2.append("[siso_screen_on_time] LONG,");
        buffer2.append("[last_upload_time] LONG,");
        buffer2.append("[rssiL0] INTEGER,");
        buffer2.append("[rssiL1] INTEGER,");
        buffer2.append("[rssiL2] INTEGER,");
        buffer2.append("[rssiL3] INTEGER,");
        buffer2.append("[rssiL4] INTEGER,");
        buffer2.append("[Reserved] INTEGER)");
        db.execSQL(buffer2.toString());
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS MIMOApInfoTable");
        db.execSQL("DROP TABLE IF EXISTS StatisticsTable");
        onCreate(db);
    }
}
