package com.android.server.hidata.wavemapping.dao;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ContextManager;
import com.android.server.hidata.wavemapping.service.HwHistoryQoeResourceBuilder;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.Iterator;
import java.util.Map;

public class DatabaseSingleton extends SQLiteOpenHelper {
    private static final String CREATE_APP_TABLE_FRONT = "CREATE TABLE IF NOT EXISTS ";
    private static final String CREATE_APP_TABLE_TITLE = " (SCRBID TEXT, UPDATE_DATE DATE DEFAULT (date('now', 'localtime')), FREQLOCNAME TEXT, SPACEID TEXT, SPACEIDMAIN TEXT, NETWORKNAME TEXT, NETWORKID TEXT, NETWORKFREQ TEXT, NW_TYPE INTEGER, DURATION INTEGER, POORCOUNT INTEGER, GOODCOUNT INTEGER, MODEL_VER_ALLAP TEXT, MODEL_VER_MAINAP TEXT)";
    private static final String CREATE_BEHAVIOR_TABLE = "CREATE TABLE IF NOT EXISTS BEHAVIOR_MAINTAIN (UPDATETIME TEXT, BATCH INTEGER)";
    private static final String CREATE_CHR_HISTQOERPT = "CREATE TABLE IF NOT EXISTS CHR_HISTQOERPT(FREQLOCNAME TEXT, QUERYCNT INTEGER, GOODCNT INTEGER, POORCNT INTEGER, DATARX INTEGER, DATATX INTEGER, UNKNOWNDB INTEGER, UNKNOWNSPACE INTEGER)";
    private static final String CREATE_CHR_LOCATION_TABLE = "CREATE TABLE IF NOT EXISTS CHR_FREQUENT_LOCATION (FREQLOCATION TEXT, FIRSTREPORT INTEGER DEFAULT 0, ENTERY INTEGER DEFAULT 0, LEAVE INTEGER DEFAULT 0, DURATION INTEGER DEFAULT 0, SPACECHANGE INTEGER DEFAULT 0, SPACELEAVE INTEGER DEFAULT 0, UPTOTALSWITCH INTEGER DEFAULT 0, UPAUTOSUCC INTEGER DEFAULT 0, UPMANUALSUCC INTEGER DEFAULT 0, UPAUTOFAIL INTEGER DEFAULT 0, UPNOSWITCHFAIL INTEGER DEFAULT 0,UPQRYCNT INTEGER DEFAULT 0, UPRESCNT INTEGER DEFAULT 0,UPUNKNOWNDB INTEGER DEFAULT 0,UPUNKNOWNSPACE INTEGER DEFAULT 0, LPTOTALSWITCH INTEGER DEFAULT 0, LPDATARX INTEGER DEFAULT 0, LPDATATX INTEGER DEFAULT 0, LPDURATION INTEGER DEFAULT 0, LPOFFSET INTEGER DEFAULT 0, LPALREADYBEST INTEGER DEFAULT 0, LPNOTREACH INTEGER DEFAULT 0, LPBACK INTEGER DEFAULT 0, LPUNKNOWNDB INTEGER DEFAULT 0, LPUNKNOWNSPACE INTEGER DEFAULT 0)";
    private static final String CREATE_ENTERPRISE_AP_TABLE = "CREATE TABLE IF NOT EXISTS ENTERPRISE_AP (SSID TEXT,MAC TEXT,UPTIME TEXT)";
    private static final String CREATE_IDENTIFY_RESULT_TABLE = "CREATE TABLE IF NOT EXISTS IDENTIFY_RESULT (SSID TEXT,PRELABLE INTEGER,SERVERMAC TEXT,UPTIME TEXT,ISMAINAP BOOLEAN,MODELNAME TEXT)";
    private static final String CREATE_LOCATION_TABLE = "CREATE TABLE IF NOT EXISTS FREQUENT_LOCATION (OOBTIME INTEGER DEFAULT 0, UPDATETIME INTEGER DEFAULT 0, CHRBENEFITUPLOADTIME INTEGER DEFAULT 0, CHRSPACEUSERUPLOADTIME INTEGER DEFAULT 0, FREQUENTLOCATION TEXT)";
    private static final String CREATE_MOBILE_AP_TABLE = "CREATE TABLE IF NOT EXISTS MOBILE_AP (SSID TEXT,MAC TEXT, UPTIME TEXT,SRCTYPE INTEGER)";
    private static final String CREATE_REGULAR_PLACESTATE_TABLE = "CREATE TABLE IF NOT EXISTS RGL_PLACESTATE (SSID TEXT, STATE INTEGER,BATCH INTEGER,FINGERNUM INTEGER,TEST_DAT_NUM INTEGER,DISNUM INTEGER, UPTIME TEXT,IDENTIFYNUM INTEGER,NO_OCURBSSIDS TEXT,ISMAINAP BOOLEAN,MODELNAME TEXT,BEGINTIME INTEGER)";
    private static final String CREATE_SPACE_TABLE_NEW = "CREATE TABLE IF NOT EXISTS SPACEUSER_BASE (SCRBID TEXT, UPDATE_DATE DATE DEFAULT (date('now', 'localtime')), FREQLOCNAME TEXT, SPACEID TEXT, SPACEIDMAIN TEXT, NETWORKNAME TEXT, NETWORKID TEXT, NETWORKFREQ TEXT, NW_TYPE INTEGER, USER_PREF_OPT_IN INTEGER, USER_PREF_OPT_OUT INTEGER, USER_PREF_STAY INTEGER, USER_PREF_TOTAL_COUNT INTEGER, POWER_CONSUMPTION INTEGER, DATA_RX INTEGER, DATA_TX INTEGER, SIGNAL_VALUE INTEGER, DURATION_CONNECTED INTEGER, MODEL_VER_ALLAP TEXT, MODEL_VER_MAINAP TEXT)";
    private static final String CREATE_STA_BACK2LTE = "CREATE TABLE IF NOT EXISTS CHR_FASTBACK2LTE(FREQLOCNAME TEXT, LOWRATCNT INTEGER, INLTECNT INTEGER, OUTLTECNT INTEGER, FASTBACK INTEGER, SUCCESSBACK INTEGER, CELLS4G INTEGER, REFCNT INTEGER, UNKNOWNDB INTEGER, UNKNOWNSPACE INTEGER)";
    public static final int DATABASE_VERSION = 2;
    private static final String DROP_APP_TABLE_FRONT = "DROP TABLE IF EXISTS ";
    private static final String TAG = ("WMapping." + DatabaseSingleton.class.getSimpleName());
    private static DatabaseSingleton mDataBaseOpenHelper = null;
    private static HwHistoryQoeResourceBuilder mQoeAppBuilder = null;

    private DatabaseSingleton(Context context) {
        super(context, Constant.getDbPath(), (SQLiteDatabase.CursorFactory) null, 2);
        mQoeAppBuilder = HwHistoryQoeResourceBuilder.getInstance();
    }

    public static synchronized SQLiteDatabase getInstance() {
        SQLiteDatabase writableDatabase;
        synchronized (DatabaseSingleton.class) {
            try {
                LogUtil.i(false, "SQLiteDatabase getInstance begin.", new Object[0]);
                if (mDataBaseOpenHelper == null) {
                    LogUtil.d(false, "SQLiteDatabase getInstance ,mDataBaseOpenHelper == null", new Object[0]);
                    Context context = ContextManager.getInstance().getContext();
                    if (context == null) {
                        LogUtil.d(false, " context is null ", new Object[0]);
                    }
                    Constant.checkPath(context);
                    mDataBaseOpenHelper = new DatabaseSingleton(context);
                    LogUtil.i(false, " DatabaseSingleton init db success", new Object[0]);
                }
                writableDatabase = mDataBaseOpenHelper.getWritableDatabase();
            } catch (SQLException e) {
                LogUtil.e(false, "SQLiteDatabase getInstance failed by Exceptions", new Object[0]);
                return null;
            }
        }
        return writableDatabase;
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            LogUtil.i(false, "onCreate,sql01=%{public}s", CREATE_REGULAR_PLACESTATE_TABLE);
            db.execSQL(CREATE_REGULAR_PLACESTATE_TABLE);
            db.execSQL(CREATE_ENTERPRISE_AP_TABLE);
            db.execSQL(CREATE_MOBILE_AP_TABLE);
            db.execSQL(CREATE_IDENTIFY_RESULT_TABLE);
            LogUtil.i(false, "onCreate,sql02=%{public}s", CREATE_IDENTIFY_RESULT_TABLE);
            LogUtil.i(false, "onCreate,sql04=%{public}s", CREATE_BEHAVIOR_TABLE);
            db.execSQL(CREATE_BEHAVIOR_TABLE);
            LogUtil.i(false, "onCreate,sql05=%{public}s", CREATE_SPACE_TABLE_NEW);
            db.execSQL(CREATE_SPACE_TABLE_NEW);
            LogUtil.i(false, "onCreate,sql06=%{public}s", CREATE_LOCATION_TABLE);
            db.execSQL(CREATE_LOCATION_TABLE);
            LogUtil.i(false, "onCreate,sql07=%{public}s", CREATE_STA_BACK2LTE);
            db.execSQL(CREATE_STA_BACK2LTE);
            LogUtil.i(false, "onCreate,sql08=%{public}s", CREATE_CHR_LOCATION_TABLE);
            db.execSQL(CREATE_CHR_LOCATION_TABLE);
            LogUtil.i(false, "onCreate,sql09=%{public}s", CREATE_CHR_HISTQOERPT);
            db.execSQL(CREATE_CHR_HISTQOERPT);
            Iterator<Map.Entry<Integer, Float>> it = mQoeAppBuilder.getQoeAppList().entrySet().iterator();
            while (it.hasNext()) {
                String sql = CREATE_APP_TABLE_FRONT + (Constant.USERDB_APP_NAME_PREFIX + it.next().getKey()) + CREATE_APP_TABLE_TITLE;
                LogUtil.i(false, "onCreate,app_sql=%{public}s", sql);
                db.execSQL(sql);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            LogUtil.e(false, "onCreate failed by Exception", new Object[0]);
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.i(false, "onUpgrade: %{public}d to %{public}d", Integer.valueOf(oldVersion), Integer.valueOf(newVersion));
        try {
            db.execSQL("DROP TABLE IF EXISTS RGL_PLACESTATE");
            db.execSQL("DROP TABLE IF EXISTS ENTERPRISE_AP");
            db.execSQL("DROP TABLE IF EXISTS MOBILE_AP");
            db.execSQL("DROP TABLE IF EXISTS IDENTIFY_RESULT");
            db.execSQL("DROP TABLE IF EXISTS SPACEUSER_BASE");
            db.execSQL("DROP TABLE IF EXISTS BEHAVIOR_MAINTAIN");
            if (oldVersion < 2) {
                db.execSQL("DROP TABLE IF EXISTS FREQUENT_LOCATION");
                db.execSQL("DROP TABLE IF EXISTS CHR_FREQUENT_LOCATION");
                db.execSQL("DROP TABLE IF EXISTS CHR_HISTQOERPT");
                db.execSQL("DROP TABLE IF EXISTS CHR_FASTBACK2LTE");
            }
            onCreate(db);
        } catch (SQLException e) {
            LogUtil.e(false, "onUpgrade failed by Exception", new Object[0]);
        }
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.i(false, "onUpgrade: %{public}d to %{public}d", Integer.valueOf(oldVersion), Integer.valueOf(newVersion));
        try {
            db.execSQL("DROP TABLE IF EXISTS RGL_PLACESTATE");
            db.execSQL("DROP TABLE IF EXISTS ENTERPRISE_AP");
            db.execSQL("DROP TABLE IF EXISTS MOBILE_AP");
            db.execSQL("DROP TABLE IF EXISTS IDENTIFY_RESULT");
            db.execSQL("DROP TABLE IF EXISTS SPACEUSER_BASE");
            db.execSQL("DROP TABLE IF EXISTS BEHAVIOR_MAINTAIN");
            onCreate(db);
        } catch (SQLException e) {
            LogUtil.e(false, "onDowngrade failed by Exception", new Object[0]);
        }
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public synchronized void onOpen(SQLiteDatabase db) {
        Throwable th;
        LogUtil.i(false, "onOpen", new Object[0]);
        try {
            if (!db.isReadOnly()) {
                try {
                    db.beginTransaction();
                    Iterator<Map.Entry<Integer, Float>> it = mQoeAppBuilder.getQoeAppList().entrySet().iterator();
                    while (it.hasNext()) {
                        String sql = CREATE_APP_TABLE_FRONT + (Constant.USERDB_APP_NAME_PREFIX + it.next().getKey()) + CREATE_APP_TABLE_TITLE;
                        LogUtil.i(false, "onOpen,app_sql=%{public}s", sql);
                        db.execSQL(sql);
                    }
                    db.setTransactionSuccessful();
                } catch (SQLException e) {
                    try {
                        LogUtil.e(false, "onOpen failed by Exception", new Object[0]);
                        db.endTransaction();
                        super.onOpen(db);
                    } catch (Throwable th2) {
                        th = th2;
                        db.endTransaction();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    db.endTransaction();
                    throw th;
                }
            }
            db.endTransaction();
        } catch (SQLException e2) {
            LogUtil.e(false, "onOpen failed by Exception", new Object[0]);
            db.endTransaction();
            super.onOpen(db);
        }
        super.onOpen(db);
    }
}
