package com.android.server.wifi.ABS;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class HwABSDataBaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "/data/system/HwABSDataBase.db";
    public static final int DATABASE_VERSION = 5;
    public static final String MIMO_AP_TABLE_NAME = "MIMOApInfoTable";
    public static final String STATISTICS_TABLE_NAME = "StatisticsTable";

    public HwABSDataBaseHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public HwABSDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 5);
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
        sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE [StatisticsTable] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[long_connect_event] INTEGER,");
        sBuffer.append("[short_connect_event] INTEGER,");
        sBuffer.append("[search_event] INTEGER,");
        sBuffer.append("[antenna_preempted_screen_on_event] INTEGER,");
        sBuffer.append("[antenna_preempted_screen_off_event] INTEGER,");
        sBuffer.append("[mo_mt_call_event] INTEGER,");
        sBuffer.append("[siso_to_mimo_event] INTEGER,");
        sBuffer.append("[ping_pong_times] INTEGER,");
        sBuffer.append("[max_ping_pong_times] INTEGER,");
        sBuffer.append("[mimo_time] LONG,");
        sBuffer.append("[siso_time] LONG,");
        sBuffer.append("[mimo_screen_on_time] LONG,");
        sBuffer.append("[siso_screen_on_time] LONG,");
        sBuffer.append("[last_upload_time] LONG,");
        sBuffer.append("[Reserved] INTEGER)");
        db.execSQL(sBuffer.toString());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS MIMOApInfoTable");
        db.execSQL("DROP TABLE IF EXISTS StatisticsTable");
        onCreate(db);
    }
}
