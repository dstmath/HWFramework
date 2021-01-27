package com.huawei.displayengine;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DisplayEngineDbHelper extends SQLiteOpenHelper {
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
    private static final String TAG = "DE J DisplayEngineDbHelper";

    public DisplayEngineDbHelper(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 10);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase db) {
        createDragInformationTable(db);
        createUserPreferencesTable(db);
        createBrightnessCurveTable(db, "BrightnessCurveLow");
        createBrightnessCurveTable(db, "BrightnessCurveMiddle");
        createBrightnessCurveTable(db, "BrightnessCurveHigh");
        createBrightnessCurveTable(db, "BrightnessCurveDefault");
        createAlgorithmEscwTable(db);
        createDataCleanerTable(db);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 9) {
            db.execSQL("DROP TABLE IF EXISTS UserDragInformation");
            createDragInformationTable(db);
            return;
        }
        cleanTable(db);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        cleanTable(db);
    }

    private void cleanTable(SQLiteDatabase db) {
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
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE if not exists [UserDragInformation] (");
        buffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        buffer.append("[TIMESTAMP] INTEGER UNIQUE, ");
        buffer.append("[PRIORITY] INTEGER, ");
        buffer.append("[STARTPOINT] REAL, ");
        buffer.append("[STOPPOINT] REAL, ");
        buffer.append("[AL] INTEGER, ");
        buffer.append("[PROXIMITYPOSITIVE] INTEGER, ");
        buffer.append("[USERID] INTEGER, ");
        buffer.append("[APPTYPE] INTEGER, ");
        buffer.append("[GAMESTATE] INTEGER, ");
        buffer.append("[PACKAGE] TEXT)");
        try {
            db.execSQL(buffer.toString());
            DeLog.i(TAG, "createDragInformationTable succss." + buffer.toString());
        } catch (SQLException e) {
            DeLog.e(TAG, "createDragInformationTable error:" + e.getMessage());
        }
    }

    private static void createUserPreferencesTable(SQLiteDatabase db) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE if not exists [UserPreferences] (");
        buffer.append("[_id] INTEGER NOT NULL PRIMARY KEY, ");
        buffer.append("[USERID] INTEGER, ");
        buffer.append("[APPTYPE] INTEGER, ");
        buffer.append("[AL] INTEGER, ");
        buffer.append("[DELTA] INTEGER)");
        try {
            db.execSQL(buffer.toString());
            DeLog.i(TAG, "createUserPreferencesTable succss." + buffer.toString());
        } catch (SQLException e) {
            DeLog.e(TAG, "createUserPreferencesTable error:" + e.getMessage());
        }
    }

    private static void createBrightnessCurveTable(SQLiteDatabase db, String table) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE if not exists [" + table + "] (");
        buffer.append("[_id] INTEGER NOT NULL PRIMARY KEY, ");
        buffer.append("[USERID] INTEGER, ");
        buffer.append("[AL] REAL, ");
        buffer.append("[BL] REAL)");
        try {
            db.execSQL(buffer.toString());
            DeLog.i(TAG, "createBrightnessCurveTable succss." + buffer.toString());
        } catch (SQLException e) {
            DeLog.e(TAG, "createBrightnessCurveTable error:" + e.getMessage());
        }
    }

    private static void createAlgorithmEscwTable(SQLiteDatabase db) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE if not exists [AlgorithmESCW] (");
        buffer.append("[_id] INTEGER NOT NULL PRIMARY KEY, ");
        buffer.append("[USERID] INTEGER, ");
        buffer.append("[ESCW] REAL)");
        try {
            db.execSQL(buffer.toString());
            DeLog.i(TAG, "createAlgorithmEscwTable succss." + buffer.toString());
        } catch (SQLException e) {
            DeLog.e(TAG, "createAlgorithmEscwTable error:" + e.getMessage());
        }
    }

    private static void createDataCleanerTable(SQLiteDatabase db) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE if not exists [DataCleaner] (");
        buffer.append("[_id] INTEGER NOT NULL PRIMARY KEY, ");
        buffer.append("[RANGEFLAG] INTEGER, ");
        buffer.append("[TIMESTAMP] INTEGER)");
        try {
            db.execSQL(buffer.toString());
            DeLog.i(TAG, "createDataCleanerTable succss." + buffer.toString());
        } catch (SQLException e) {
            DeLog.e(TAG, "createDataCleanerTable error:" + e.getMessage());
        }
    }
}
