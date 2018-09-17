package com.android.server.wifi.wifipro;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WifiProHistoryDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "WifiproHistoryRecord.db";
    public static final int DATABASE_VERSION = 4;
    public static final String WP_AP_INFO_TB_NAME = "WifiProApInfoRecodTable";
    public static final String WP_DUAL_BAND_AP_INFO_TB_NAME = "WifiProDualBandApInfoRcdTable";
    public static final String WP_ENTERPRISE_AP_TB_NAME = "WifiProEnterpriseAPTable";
    public static final String WP_QUALITY_TB_NAME = "WifiProApQualityTable";
    public static final String WP_RELATE_AP_TB_NAME = "WifiProRelateApTable";

    public WifiProHistoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 4);
    }

    public void onCreate(SQLiteDatabase db) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [WifiProApInfoRecodTable] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[apBSSID] TEXT, ");
        sBuffer.append("[apSSID] TEXT, ");
        sBuffer.append("[apSecurityType] INTEGER, ");
        sBuffer.append("[firstConnectTime] INT8, ");
        sBuffer.append("[lastConnectTime] INT8, ");
        sBuffer.append("[lanDataSize] INTEGER, ");
        sBuffer.append("[highSpdFreq] INTEGER, ");
        sBuffer.append("[totalUseTime] INTEGER, ");
        sBuffer.append("[totalUseTimeAtNight] INTEGER, ");
        sBuffer.append("[totalUseTimeAtWeekend] INTEGER, ");
        sBuffer.append("[judgeHomeAPTime] INT8)");
        db.execSQL(sBuffer.toString());
        sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [WifiProEnterpriseAPTable] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[apSSID] TEXT, ");
        sBuffer.append("[apSecurityType] INTEGER)");
        db.execSQL(sBuffer.toString());
        sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [WifiProRelateApTable] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[apBSSID] TEXT, ");
        sBuffer.append("[RelatedBSSID] TEXT, ");
        sBuffer.append("[RelateType] SMALLINT, ");
        sBuffer.append("[MaxCurrentRSSI] INTEGER, ");
        sBuffer.append("[MaxRelatedRSSI] INTEGER, ");
        sBuffer.append("[MinCurrentRSSI] INTEGER, ");
        sBuffer.append("[MinRelatedRSSI] INTEGER)");
        db.execSQL(sBuffer.toString());
        sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [WifiProApQualityTable] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[apBSSID] TEXT, ");
        sBuffer.append("[RTT_Product] BLOB, ");
        sBuffer.append("[RTT_PacketVolume] BLOB, ");
        sBuffer.append("[HistoryAvgRtt] BLOB, ");
        sBuffer.append("[OTA_LostRateValue] BLOB, ");
        sBuffer.append("[OTA_PktVolume] BLOB, ");
        sBuffer.append("[OTA_BadPktProduct] BLOB)");
        db.execSQL(sBuffer.toString());
        sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [WifiProDualBandApInfoRcdTable] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[apBSSID] TEXT, ");
        sBuffer.append("[apSSID] TEXT, ");
        sBuffer.append("[InetCapability] SMALLINT, ");
        sBuffer.append("[ServingBand] SMALLINT, ");
        sBuffer.append("[ApAuthType] SMALLINT, ");
        sBuffer.append("[ChannelFrequency] INTEGER, ");
        sBuffer.append("[DisappearCount] INTEGER, ");
        sBuffer.append("[isInBlackList] INTEGER, ");
        sBuffer.append("[UpdateTime] LONG, ");
        sBuffer.append("[RSSIThreshold] TEXT)");
        db.execSQL(sBuffer.toString());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS WifiProApInfoRecodTable");
        db.execSQL("DROP TABLE IF EXISTS WifiProEnterpriseAPTable");
        db.execSQL("DROP TABLE IF EXISTS WifiProApQualityTable");
        db.execSQL("DROP TABLE IF EXISTS WifiProRelateApTable");
        db.execSQL("DROP TABLE IF EXISTS WifiProDualBandApInfoRcdTable");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS WifiProApInfoRecodTable");
        db.execSQL("DROP TABLE IF EXISTS WifiProEnterpriseAPTable");
        db.execSQL("DROP TABLE IF EXISTS WifiProApQualityTable");
        db.execSQL("DROP TABLE IF EXISTS WifiProRelateApTable");
        db.execSQL("DROP TABLE IF EXISTS WifiProDualBandApInfoRcdTable");
        onCreate(db);
    }
}
