package com.android.server.hidata.appqoe;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HwAPPQoEDataBase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "HwAPPQoEDataBase.db";
    public static final int DATABASE_VERSION = 5;
    public static final String TABLE_USER_ACTION = "APPQoEUserAction";

    public HwAPPQoEDataBase(Context context) {
        super(context, DATABASE_NAME, null, 5);
    }

    public void onCreate(SQLiteDatabase db) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [APPQoEUserAction] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[appId] INTEGER,");
        sBuffer.append("[wifiSSID] TEXT,");
        sBuffer.append("[cardInfo] TEXT,");
        sBuffer.append("[commonCnt] INTEGER,");
        sBuffer.append("[radicalCnt] INTEGER)");
        db.execSQL(sBuffer.toString());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS APPQoEUserAction");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS APPQoEUserAction");
        onCreate(db);
    }
}
