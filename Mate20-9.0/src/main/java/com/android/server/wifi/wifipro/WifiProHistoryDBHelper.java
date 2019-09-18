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
        StringBuffer sBuffer2 = new StringBuffer();
        sBuffer2.append("CREATE TABLE if not exists [WifiProEnterpriseAPTable] (");
        sBuffer2.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer2.append("[apSSID] TEXT, ");
        sBuffer2.append("[apSecurityType] INTEGER)");
        db.execSQL(sBuffer2.toString());
        StringBuffer sBuffer3 = new StringBuffer();
        sBuffer3.append("CREATE TABLE if not exists [WifiProRelateApTable] (");
        sBuffer3.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer3.append("[apBSSID] TEXT, ");
        sBuffer3.append("[RelatedBSSID] TEXT, ");
        sBuffer3.append("[RelateType] SMALLINT, ");
        sBuffer3.append("[MaxCurrentRSSI] INTEGER, ");
        sBuffer3.append("[MaxRelatedRSSI] INTEGER, ");
        sBuffer3.append("[MinCurrentRSSI] INTEGER, ");
        sBuffer3.append("[MinRelatedRSSI] INTEGER)");
        db.execSQL(sBuffer3.toString());
        StringBuffer sBuffer4 = new StringBuffer();
        sBuffer4.append("CREATE TABLE if not exists [WifiProApQualityTable] (");
        sBuffer4.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer4.append("[apBSSID] TEXT, ");
        sBuffer4.append("[RTT_Product] BLOB, ");
        sBuffer4.append("[RTT_PacketVolume] BLOB, ");
        sBuffer4.append("[HistoryAvgRtt] BLOB, ");
        sBuffer4.append("[OTA_LostRateValue] BLOB, ");
        sBuffer4.append("[OTA_PktVolume] BLOB, ");
        sBuffer4.append("[OTA_BadPktProduct] BLOB)");
        db.execSQL(sBuffer4.toString());
        StringBuffer sBuffer5 = new StringBuffer();
        sBuffer5.append("CREATE TABLE if not exists [WifiProDualBandApInfoRcdTable] (");
        sBuffer5.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer5.append("[apBSSID] TEXT, ");
        sBuffer5.append("[apSSID] TEXT, ");
        sBuffer5.append("[InetCapability] SMALLINT, ");
        sBuffer5.append("[ServingBand] SMALLINT, ");
        sBuffer5.append("[ApAuthType] SMALLINT, ");
        sBuffer5.append("[ChannelFrequency] INTEGER, ");
        sBuffer5.append("[DisappearCount] INTEGER, ");
        sBuffer5.append("[isInBlackList] INTEGER, ");
        sBuffer5.append("[UpdateTime] LONG, ");
        sBuffer5.append("[RSSIThreshold] TEXT)");
        db.execSQL(sBuffer5.toString());
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
