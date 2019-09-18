package com.huawei.displayengine;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DisplayEngineDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "DisplayEngine.db";
    public static final int DATABASE_VERSION = 10;
    public static final String TABLE_NAME_ALGORITHM_ESCW = "AlgorithmESCW";
    public static final String TABLE_NAME_BRIGHTNESS_CURVE_DEFAULT = "BrightnessCurveDefault";
    public static final String TABLE_NAME_BRIGHTNESS_CURVE_HIGH = "BrightnessCurveHigh";
    public static final String TABLE_NAME_BRIGHTNESS_CURVE_LOW = "BrightnessCurveLow";
    public static final String TABLE_NAME_BRIGHTNESS_CURVE_MIDDLE = "BrightnessCurveMiddle";
    public static final String TABLE_NAME_DATA_CLEANER = "DataCleaner";
    public static final String TABLE_NAME_DRAG_INFORMATION = "UserDragInformation";
    public static final String TABLE_NAME_USER_PREFERENCES = "UserPreferences";
    private static final String TAG = "DE J DisplayEngineDBHelper";

    public DisplayEngineDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 10);
    }

    public void onCreate(SQLiteDatabase db) {
        createDragInformationTable(db);
        createUserPreferencesTable(db);
        createBrightnessCurveTable(db, "BrightnessCurveLow");
        createBrightnessCurveTable(db, "BrightnessCurveMiddle");
        createBrightnessCurveTable(db, "BrightnessCurveHigh");
        createBrightnessCurveTable(db, "BrightnessCurveDefault");
        createAlgorithmESCWTable(db);
        createDataCleanerTable(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 9) {
            db.execSQL("DROP TABLE IF EXISTS UserDragInformation");
            createDragInformationTable(db);
            return;
        }
        db.execSQL("DROP TABLE IF EXISTS UserDragInformation");
        db.execSQL("DROP TABLE IF EXISTS UserPreferences");
        db.execSQL("DROP TABLE IF EXISTS BrightnessCurveLow");
        db.execSQL("DROP TABLE IF EXISTS BrightnessCurveMiddle");
        db.execSQL("DROP TABLE IF EXISTS BrightnessCurveHigh");
        db.execSQL("DROP TABLE IF EXISTS BrightnessCurveDefault");
        db.execSQL("DROP TABLE IF EXISTS AlgorithmESCW");
        db.execSQL("DROP TABLE IF EXISTS DataCleaner");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS UserDragInformation");
        db.execSQL("DROP TABLE IF EXISTS UserPreferences");
        db.execSQL("DROP TABLE IF EXISTS BrightnessCurveLow");
        db.execSQL("DROP TABLE IF EXISTS BrightnessCurveMiddle");
        db.execSQL("DROP TABLE IF EXISTS BrightnessCurveHigh");
        db.execSQL("DROP TABLE IF EXISTS BrightnessCurveDefault");
        db.execSQL("DROP TABLE IF EXISTS AlgorithmESCW");
        db.execSQL("DROP TABLE IF EXISTS DataCleaner");
        onCreate(db);
    }

    private static void createDragInformationTable(SQLiteDatabase db) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [UserDragInformation] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[TIMESTAMP] INTEGER UNIQUE, ");
        sBuffer.append("[PRIORITY] INTEGER, ");
        sBuffer.append("[STARTPOINT] REAL, ");
        sBuffer.append("[STOPPOINT] REAL, ");
        sBuffer.append("[AL] INTEGER, ");
        sBuffer.append("[PROXIMITYPOSITIVE] INTEGER, ");
        sBuffer.append("[USERID] INTEGER, ");
        sBuffer.append("[APPTYPE] INTEGER, ");
        sBuffer.append("[GAMESTATE] INTEGER, ");
        sBuffer.append("[PACKAGE] TEXT)");
        try {
            db.execSQL(sBuffer.toString());
            DElog.i(TAG, "createDragInformationTable succss." + sBuffer.toString());
        } catch (SQLException e) {
            DElog.e(TAG, "createDragInformationTable error:" + e.getMessage());
        }
    }

    private static void createUserPreferencesTable(SQLiteDatabase db) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [UserPreferences] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY, ");
        sBuffer.append("[USERID] INTEGER, ");
        sBuffer.append("[APPTYPE] INTEGER, ");
        sBuffer.append("[AL] INTEGER, ");
        sBuffer.append("[DELTA] INTEGER)");
        try {
            db.execSQL(sBuffer.toString());
            DElog.i(TAG, "createUserPreferencesTable succss." + sBuffer.toString());
        } catch (SQLException e) {
            DElog.e(TAG, "createUserPreferencesTable error:" + e.getMessage());
        }
    }

    private static void createBrightnessCurveTable(SQLiteDatabase db, String table) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [" + table + "] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY, ");
        sBuffer.append("[USERID] INTEGER, ");
        sBuffer.append("[AL] REAL, ");
        sBuffer.append("[BL] REAL)");
        try {
            db.execSQL(sBuffer.toString());
            DElog.i(TAG, "createBrightnessCurveTable succss." + sBuffer.toString());
        } catch (SQLException e) {
            DElog.e(TAG, "createBrightnessCurveTable error:" + e.getMessage());
        }
    }

    private static void createAlgorithmESCWTable(SQLiteDatabase db) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [AlgorithmESCW] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY, ");
        sBuffer.append("[USERID] INTEGER, ");
        sBuffer.append("[ESCW] REAL)");
        try {
            db.execSQL(sBuffer.toString());
            DElog.i(TAG, "createAlgorithmESCWTable succss." + sBuffer.toString());
        } catch (SQLException e) {
            DElog.e(TAG, "createAlgorithmESCWTable error:" + e.getMessage());
        }
    }

    private static void createDataCleanerTable(SQLiteDatabase db) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [DataCleaner] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY, ");
        sBuffer.append("[RANGEFLAG] INTEGER, ");
        sBuffer.append("[TIMESTAMP] INTEGER)");
        try {
            db.execSQL(sBuffer.toString());
            DElog.i(TAG, "createDataCleanerTable succss." + sBuffer.toString());
        } catch (SQLException e) {
            DElog.e(TAG, "createDataCleanerTable error:" + e.getMessage());
        }
    }
}
