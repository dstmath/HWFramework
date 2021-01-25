package com.android.server.hidata.hinetwork;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.wifi.HwHiLog;

public class HwHiNetworkDataBase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "HiNetwork.db";
    public static final int DATABASE_VERSION = 4;
    public static final String TABLE_USER_ACTION = "HiNetworkWhiteListTable";

    public HwHiNetworkDataBase(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 4);
        HwHiLog.e("HwHiNetworkDataBase", false, "create db file", new Object[0]);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase db) {
        HwHiLog.e("HwHiNetworkDataBase", false, "create db table", new Object[0]);
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [HiNetworkWhiteListTable] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[PACKAGENAME] TEXT, ");
        sBuffer.append("[ISCANACCE] INTEGER)     ");
        try {
            db.execSQL(sBuffer.toString());
        } catch (SQLException e) {
            HwHiLog.e("HwHiNetworkDataBase", false, "createTrafficTable error:%{public}s", new Object[]{e.getMessage()});
        }
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS HiNetworkWhiteListTable");
        onCreate(db);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS HiNetworkWhiteListTable");
        onCreate(db);
    }
}
