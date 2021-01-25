package com.android.server.hidata.histream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HwHiStreamDataBase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "HiStream.db";
    public static final int DATABASE_VERSION = 5;
    public static final String HISTERAM_WECHAT_AP_NAME = "HiStreamAPRecordTable";
    public static final String HISTREAM_APP_STATISTICS = "HiStreamAppStatisticsTable";
    public static final String HISTREAM_WECHAT_TRAFFIC_NAME = "HiStreamTrafficTable";

    public HwHiStreamDataBase(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 5);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase db) {
        createTrafficTable(db);
        createApInfoTable(db);
        createAppStatisticsTable(db);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS HiStreamTrafficTable");
        db.execSQL("DROP TABLE IF EXISTS HiStreamAPRecordTable");
        db.execSQL("DROP TABLE IF EXISTS HiStreamAppStatisticsTable");
        onCreate(db);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS HiStreamTrafficTable");
        db.execSQL("DROP TABLE IF EXISTS HiStreamAPRecordTable");
        db.execSQL("DROP TABLE IF EXISTS HiStreamAppStatisticsTable");
        onCreate(db);
    }

    public static void createTrafficTable(SQLiteDatabase db) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [HiStreamTrafficTable] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[SUBID] TEXT, ");
        sBuffer.append("[CALLTYPE] INTEGER, ");
        sBuffer.append("[NETTYPE] INTEGER, ");
        sBuffer.append("[CURRDAY] LONG, ");
        sBuffer.append("[TRAFFIC] LONG)");
        try {
            db.execSQL(sBuffer.toString());
        } catch (SQLException e) {
            HwHiStreamUtils.logE(false, "createTrafficTable error:%{public}s", e.getMessage());
        }
    }

    public static void createApInfoTable(SQLiteDatabase db) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [HiStreamAPRecordTable] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[SSID] TEXT, ");
        sBuffer.append("[mScenario] INTEGER, ");
        sBuffer.append("[APUSRType] INTEGER)");
        try {
            db.execSQL(sBuffer.toString());
        } catch (SQLException e) {
            HwHiStreamUtils.logE(false, "createApInfoTable error:%{public}s", e.getMessage());
        }
    }

    public static void createAppStatisticsTable(SQLiteDatabase db) {
        HwHiStreamUtils.logE(false, "createApInfoTable ENTER", new Object[0]);
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [HiStreamAppStatisticsTable] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[mScenario] INTEGER, ");
        sBuffer.append("[mNum] INTEGER, ");
        sBuffer.append("[mStartInWiFiCnt] INTEGER, ");
        sBuffer.append("[mStartInCellularCnt] INTEGER, ");
        sBuffer.append("[mCallInCellularDur] INTEGER, ");
        sBuffer.append("[mCallInWiFiDur] INTEGER, ");
        sBuffer.append("[mCellLv1Cnt] INTEGER, ");
        sBuffer.append("[mCellLv2Cnt] INTEGER, ");
        sBuffer.append("[mCellLv3Cnt] INTEGER, ");
        sBuffer.append("[mWiFiLv1Cnt] INTEGER, ");
        sBuffer.append("[mWiFiLv2Cnt] INTEGER, ");
        sBuffer.append("[mWiFiLv3Cnt] INTEGER, ");
        sBuffer.append("[mTrfficCell] INTEGER, ");
        sBuffer.append("[mVipSwitchCnt] INTEGER, ");
        sBuffer.append("[mStallSwitchCnt] INTEGER, ");
        sBuffer.append("[mStallSwitch0Cnt] INTEGER, ");
        sBuffer.append("[mStallSwitch1Cnt] INTEGER, ");
        sBuffer.append("[mStallSwitchAbove1Cnt] INTEGER, ");
        sBuffer.append("[mSwitch2CellCnt] INTEGER, ");
        sBuffer.append("[mSwitch2WifiCnt] INTEGER, ");
        sBuffer.append("[mMplinkDur] INTEGER, ");
        sBuffer.append("[mMplinkEnCnt] INTEGER, ");
        sBuffer.append("[mMplinkDisStallCnt] INTEGER, ");
        sBuffer.append("[mMplinkDisWifiGoodCnt] INTEGER, ");
        sBuffer.append("[mMplinkEnFailCnt] INTEGER, ");
        sBuffer.append("[mMplinkDisFailCnt] INTEGER, ");
        sBuffer.append("[mMplinkEnTraf] INTEGER, ");
        sBuffer.append("[MplinkEnFailEnvironCnt] INTEGER, ");
        sBuffer.append("[mMplinkEnFailCoexistCnt] INTEGER, ");
        sBuffer.append("[mMplinkEnFailPingPongCnt] INTEGER, ");
        sBuffer.append("[mMplinkEnFailHistoryQoeCnt] INTEGER, ");
        sBuffer.append("[mMplinkEnFailChQoeCnt] INTEGER, ");
        sBuffer.append("[mHicureEnCnt] INTEGER, ");
        sBuffer.append("[mHicureSucCnt] INTEGER, ");
        sBuffer.append("[mLastUploadTime] LONG)");
        try {
            db.execSQL(sBuffer.toString());
        } catch (SQLException e) {
            HwHiStreamUtils.logE(false, "createAppStatisticsTable error:%{public}s", e.getMessage());
        }
    }
}
