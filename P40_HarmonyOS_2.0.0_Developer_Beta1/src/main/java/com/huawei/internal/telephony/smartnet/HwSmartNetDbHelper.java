package com.huawei.internal.telephony.smartnet;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.huawei.android.telephony.RlogEx;

public class HwSmartNetDbHelper extends SQLiteOpenHelper {
    public static final String CELLSENSOR_CDMA_LEVELHW = "CdmaLevelHw";
    public static final String CELLSENSOR_DATA_OPERATOR_NUMERIC = "DataOperatorNumeric";
    public static final String CELLSENSOR_DATA_RADIOTECH = "DataRadioTech";
    public static final String CELLSENSOR_DATA_REGSTATE = "DataRegState";
    public static final String CELLSENSOR_GSM_LEVELHW = "GsmLevelHw";
    public static final String CELLSENSOR_ICCID = "iccid";
    public static final String CELLSENSOR_ID = "id";
    public static final String CELLSENSOR_LTE_LEVELHW = "LteLevelHw";
    public static final String CELLSENSOR_MAIN_CELLID = "MainCellId";
    public static final String CELLSENSOR_MAIN_PCI = "MainPci";
    public static final String CELLSENSOR_MAIN_TAC = "MainTac";
    public static final String CELLSENSOR_NEIGHBORING_CELLID = "NeighboringCellId";
    public static final String CELLSENSOR_NEIGHBORING_PCI = "NeighboringPci";
    public static final String CELLSENSOR_NEIGHBORING_TAC = "NeighboringTac";
    public static final String CELLSENSOR_NRSTATE = "NrState";
    public static final String CELLSENSOR_NR_LEVELHW = "NrLevelHw";
    public static final String CELLSENSOR_NSASTATE = "NsaState";
    public static final String CELLSENSOR_SLOTID = "slotId";
    public static final String CELLSENSOR_TIME = "samplingTime";
    public static final String CELLSENSOR_VOICE_OPERATOR_NUMERIC = "VoiceOperatorNumeric";
    public static final String CELLSENSOR_VOICE_RADIOTECH = "VoiceRadioTech";
    public static final String CELLSENSOR_VOICE_REGSTATE = "VoiceRegState";
    public static final String CELLSENSOR_WCDMA_LEVELHW = "WcdmaLevelHw";
    public static final String CELL_INFO_POINT_ARRIVE_TIME = "ArriveTime";
    public static final String CELL_INFO_POINT_BLACK_TYPE = "BlackType";
    public static final String CELL_INFO_POINT_CELL_IDENTITY = "CellIdentity";
    public static final String CELL_INFO_POINT_CONVERTTOEXCEPTION_DURATION = "ConvertToExceptionDuration";
    public static final String CELL_INFO_POINT_EXCEPTION_COUNTS = "ExceptionCounts";
    public static final String CELL_INFO_POINT_EXCEPTION_PROBABILITY = "ExceptionProbability";
    public static final String CELL_INFO_POINT_NORMAL_COUNTS = "NormalCounts";
    public static final String CELL_INFO_POINT_TOTAL_COUNTS = "TotalCounts";
    private static final String DATABASE_NAME = "hw_smartnet_database.db";
    private static final int DATABASE_VERSION = 1;
    public static final String ICCID_HASH = "IccIdHash";
    private static final int NEI_CELL_INFO_COUNT = 7;
    public static final String ROUTE_ID = "RouteId";
    private static final String SQL_DROP_CELLINFOPOINTS = "DROP TABLE IF EXISTS CellInfoPoints";
    private static final String SQL_DROP_CELLSENSOR = "DROP TABLE IF EXISTS CellSensor";
    private static final String SQL_DROP_STUDYINDEX = "DROP TABLE IF EXISTS StudyIndex";
    private static int STDUY_MAX_NUM = 20;
    public static final String STUDY_INDEX_MAX_COUNT = "StudyMaxCount";
    public static final String TABLE_CELLINFOPOINTS = "CellInfoPoints";
    public static final String TABLE_CELLSENSOR = "CellSensor";
    public static final String TABLE_STUDYINDEX = "StudyIndex";
    private static final String TAG = "HwSmartNetDbHelper";
    private static final int TARGET_POINT_LIMIT = 5;

    public HwSmartNetDbHelper(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 1);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase db) {
        creatCellSensorTable(db);
        createCellInfoPointTable(db);
        createStudyIndexTable(db);
        logi("create HwSmartNetDbHelper");
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_CELLSENSOR);
        db.execSQL(SQL_DROP_CELLINFOPOINTS);
        db.execSQL(SQL_DROP_STUDYINDEX);
        onCreate(db);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_CELLSENSOR);
        db.execSQL(SQL_DROP_CELLINFOPOINTS);
        db.execSQL(SQL_DROP_STUDYINDEX);
        onCreate(db);
    }

    private void creatCellSensorTable(SQLiteDatabase db) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE IF NOT EXISTS CellSensor (");
        buffer.append("id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,");
        buffer.append("slotId INTEGER,");
        buffer.append("iccid TEXT,");
        buffer.append("samplingTime INTEGER,");
        buffer.append("MainCellId INTEGER,");
        buffer.append("MainPci INTEGER,");
        buffer.append("MainTac INTEGER,");
        buffer.append("NeighboringCellId INTEGER,");
        buffer.append("NeighboringPci INTEGER,");
        buffer.append("NeighboringTac INTEGER,");
        buffer.append("VoiceRegState INTEGER,");
        buffer.append("DataRegState INTEGER,");
        buffer.append("VoiceOperatorNumeric TEXT,");
        buffer.append("DataOperatorNumeric TEXT,");
        buffer.append("VoiceRadioTech INTEGER,");
        buffer.append("DataRadioTech INTEGER,");
        buffer.append("NrState INTEGER,");
        buffer.append("NsaState INTEGER,");
        buffer.append("CdmaLevelHw INTEGER,");
        buffer.append("GsmLevelHw INTEGER,");
        buffer.append("WcdmaLevelHw INTEGER,");
        buffer.append("LteLevelHw INTEGER,");
        buffer.append("NrLevelHw INTEGER");
        buffer.append(")");
        try {
            db.execSQL(buffer.toString());
        } catch (SQLException e) {
            logi("create cell sensor fail.");
        }
    }

    private void createCellInfoPointTable(SQLiteDatabase db) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE if not exists CellInfoPoints (");
        buffer.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,");
        buffer.append("RouteId INTEGER,");
        buffer.append("BlackType INTEGER,");
        buffer.append("CellIdentity INTEGER,");
        buffer.append("NormalCounts INTEGER,");
        buffer.append("ExceptionCounts INTEGER,");
        buffer.append("TotalCounts INTEGER,");
        buffer.append("ArriveTime INTEGER,");
        buffer.append("ConvertToExceptionDuration INTEGER,");
        buffer.append("ExceptionProbability REAL,");
        buffer.append("IccIdHash TEXT)");
        try {
            db.execSQL(buffer.toString());
        } catch (SQLException e) {
            logi("create cell info points fail.");
        }
    }

    private void createStudyIndexTable(SQLiteDatabase db) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE if not exists StudyIndex (");
        buffer.append("RouteId INTEGER,");
        buffer.append("IccIdHash TEXT,");
        buffer.append("StudyMaxCount INTEGER)");
        try {
            db.execSQL(buffer.toString());
        } catch (SQLException e) {
            logi("create StudyIndexTable fail.");
        }
    }

    private void logi(String msg) {
        RlogEx.i(TAG, msg);
    }
}
