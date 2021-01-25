package com.huawei.hwwifiproservice;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.wifi.HwHiLog;

public class WifiProHistoryDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "WifiproHistoryRecord.db";
    public static final int DATABASE_VERSION = 4;
    private static final String TAG = "WifiProHistory_DBHelper";
    public static final String WP_AP_INFO_TB_NAME = "WifiProApInfoRecodTable";
    public static final String WP_DUAL_BAND_AP_INFO_TB_NAME = "WifiProDualBandApInfoRcdTable";
    public static final String WP_ENTERPRISE_AP_TB_NAME = "WifiProEnterpriseAPTable";
    public static final String WP_QUALITY_TB_NAME = "WifiProApQualityTable";
    public static final String WP_RELATE_AP_TB_NAME = "WifiProRelateApTable";

    public WifiProHistoryDBHelper(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 4);
    }

    private void createWifiProApInfoRecodTable(SQLiteDatabase db) {
        if (db == null) {
            HwHiLog.i(TAG, false, "createWifiProApInfoRecodTable db is null", new Object[0]);
            return;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("CREATE TABLE if not exists [WifiProApInfoRecodTable] (");
        stringBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        stringBuffer.append("[apBSSID] TEXT, ");
        stringBuffer.append("[apSSID] TEXT, ");
        stringBuffer.append("[apSecurityType] INTEGER, ");
        stringBuffer.append("[firstConnectTime] INT8, ");
        stringBuffer.append("[lastConnectTime] INT8, ");
        stringBuffer.append("[lanDataSize] INTEGER, ");
        stringBuffer.append("[highSpdFreq] INTEGER, ");
        stringBuffer.append("[totalUseTime] INTEGER, ");
        stringBuffer.append("[totalUseTimeAtNight] INTEGER, ");
        stringBuffer.append("[totalUseTimeAtWeekend] INTEGER, ");
        stringBuffer.append("[judgeHomeAPTime] INT8)");
        db.execSQL(stringBuffer.toString());
    }

    private void createWifiProEnterpriseApTable(SQLiteDatabase db) {
        if (db == null) {
            HwHiLog.i(TAG, false, "createWifiProEnterpriseApTable db is null", new Object[0]);
            return;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("CREATE TABLE if not exists [WifiProEnterpriseAPTable] (");
        stringBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        stringBuffer.append("[apSSID] TEXT, ");
        stringBuffer.append("[apSecurityType] INTEGER)");
        db.execSQL(stringBuffer.toString());
    }

    private void createWifiProRelateApTable(SQLiteDatabase db) {
        if (db == null) {
            HwHiLog.i(TAG, false, "createWifiProRelateApTable db is null", new Object[0]);
            return;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("CREATE TABLE if not exists [WifiProRelateApTable] (");
        stringBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        stringBuffer.append("[apBSSID] TEXT, ");
        stringBuffer.append("[RelatedBSSID] TEXT, ");
        stringBuffer.append("[RelateType] SMALLINT, ");
        stringBuffer.append("[MaxCurrentRSSI] INTEGER, ");
        stringBuffer.append("[MaxRelatedRSSI] INTEGER, ");
        stringBuffer.append("[MinCurrentRSSI] INTEGER, ");
        stringBuffer.append("[MinRelatedRSSI] INTEGER)");
        db.execSQL(stringBuffer.toString());
    }

    private void createWifiProApQualityTable(SQLiteDatabase db) {
        if (db == null) {
            HwHiLog.i(TAG, false, "createWifiProApQualityTable db is null", new Object[0]);
            return;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("CREATE TABLE if not exists [WifiProApQualityTable] (");
        stringBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        stringBuffer.append("[apBSSID] TEXT, ");
        stringBuffer.append("[RTT_Product] BLOB, ");
        stringBuffer.append("[RTT_PacketVolume] BLOB, ");
        stringBuffer.append("[HistoryAvgRtt] BLOB, ");
        stringBuffer.append("[OTA_LostRateValue] BLOB, ");
        stringBuffer.append("[OTA_PktVolume] BLOB, ");
        stringBuffer.append("[OTA_BadPktProduct] BLOB)");
        db.execSQL(stringBuffer.toString());
    }

    private void createWifiProDualBandApInfoRcdTable(SQLiteDatabase db) {
        if (db == null) {
            HwHiLog.i(TAG, false, "createWifiProDualBandApInfoRcdTable db is null", new Object[0]);
            return;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("CREATE TABLE if not exists [WifiProDualBandApInfoRcdTable] (");
        stringBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        stringBuffer.append("[apBSSID] TEXT, ");
        stringBuffer.append("[apSSID] TEXT, ");
        stringBuffer.append("[InetCapability] SMALLINT, ");
        stringBuffer.append("[ServingBand] SMALLINT, ");
        stringBuffer.append("[ApAuthType] SMALLINT, ");
        stringBuffer.append("[ChannelFrequency] INTEGER, ");
        stringBuffer.append("[DisappearCount] INTEGER, ");
        stringBuffer.append("[isInBlackList] INTEGER, ");
        stringBuffer.append("[UpdateTime] LONG, ");
        stringBuffer.append("[RSSIThreshold] TEXT)");
        db.execSQL(stringBuffer.toString());
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase db) {
        createWifiProApInfoRecodTable(db);
        createWifiProEnterpriseApTable(db);
        createWifiProRelateApTable(db);
        createWifiProApQualityTable(db);
        createWifiProDualBandApInfoRcdTable(db);
        HwHiLog.i(TAG, false, "WifiproHistoryRecord.db : onCreate()", new Object[0]);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS WifiProApInfoRecodTable");
        db.execSQL("DROP TABLE IF EXISTS WifiProEnterpriseAPTable");
        db.execSQL("DROP TABLE IF EXISTS WifiProApQualityTable");
        db.execSQL("DROP TABLE IF EXISTS WifiProRelateApTable");
        db.execSQL("DROP TABLE IF EXISTS WifiProDualBandApInfoRcdTable");
        onCreate(db);
        HwHiLog.i(TAG, false, "WifiproHistoryRecord.db : onUpgrade()", new Object[0]);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS WifiProApInfoRecodTable");
        db.execSQL("DROP TABLE IF EXISTS WifiProEnterpriseAPTable");
        db.execSQL("DROP TABLE IF EXISTS WifiProApQualityTable");
        db.execSQL("DROP TABLE IF EXISTS WifiProRelateApTable");
        db.execSQL("DROP TABLE IF EXISTS WifiProDualBandApInfoRcdTable");
        onCreate(db);
        HwHiLog.i(TAG, false, "WifiproHistoryRecord.db : onDowngrade()", new Object[0]);
    }
}
