package com.android.server.wifi.ABS;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HwABSDataBaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "/data/system/HwABSDataBase.db";
    public static final int DATABASE_VERSION = 6;
    public static final String MIMO_AP_TABLE_NAME = "MIMOApInfoTable";
    public static final String STATISTICS_TABLE_NAME = "StatisticsTable";

    public HwABSDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public HwABSDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 6);
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
        sBuffer.append("[in_black_list] INTEGER,");
        sBuffer.append("[in_vowifi_black_list] INTEGER,");
        sBuffer.append("[reassociate_times] INTEGER,");
        sBuffer.append("[failed_times] INTEGER,");
        sBuffer.append("[continuous_failure_times] INTEGER,");
        sBuffer.append("[last_connect_time] LONG,");
        sBuffer.append("[Reserved] INTEGER)");
        db.execSQL(sBuffer.toString());
        StringBuffer sBuffer2 = new StringBuffer();
        sBuffer2.append("CREATE TABLE [StatisticsTable] (");
        sBuffer2.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer2.append("[long_connect_event] INTEGER,");
        sBuffer2.append("[short_connect_event] INTEGER,");
        sBuffer2.append("[search_event] INTEGER,");
        sBuffer2.append("[antenna_preempted_screen_on_event] INTEGER,");
        sBuffer2.append("[antenna_preempted_screen_off_event] INTEGER,");
        sBuffer2.append("[mo_mt_call_event] INTEGER,");
        sBuffer2.append("[siso_to_mimo_event] INTEGER,");
        sBuffer2.append("[ping_pong_times] INTEGER,");
        sBuffer2.append("[max_ping_pong_times] INTEGER,");
        sBuffer2.append("[mimo_time] LONG,");
        sBuffer2.append("[siso_time] LONG,");
        sBuffer2.append("[mimo_screen_on_time] LONG,");
        sBuffer2.append("[siso_screen_on_time] LONG,");
        sBuffer2.append("[last_upload_time] LONG,");
        sBuffer2.append("[rssiL0] INTEGER,");
        sBuffer2.append("[rssiL1] INTEGER,");
        sBuffer2.append("[rssiL2] INTEGER,");
        sBuffer2.append("[rssiL3] INTEGER,");
        sBuffer2.append("[rssiL4] INTEGER,");
        sBuffer2.append("[Reserved] INTEGER)");
        db.execSQL(sBuffer2.toString());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS MIMOApInfoTable");
        db.execSQL("DROP TABLE IF EXISTS StatisticsTable");
        onCreate(db);
    }
}
