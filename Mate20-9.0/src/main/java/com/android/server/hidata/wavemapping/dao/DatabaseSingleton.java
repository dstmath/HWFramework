package com.android.server.hidata.wavemapping.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ContextManager;
import com.android.server.hidata.wavemapping.service.HwHistoryQoEResourceBuilder;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.Iterator;
import java.util.Map;

public class DatabaseSingleton extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 3;
    private static final String TAG = ("WMapping." + DatabaseSingleton.class.getSimpleName());
    private static DatabaseSingleton mDataBaseOpenHelper = null;
    private static HwHistoryQoEResourceBuilder mQoeAppBuilder = null;
    private String CREATE_APP_TABLE_FRONT = "CREATE TABLE IF NOT EXISTS ";
    private String CREATE_APP_TABLE_TITLE = " (SCRBID TEXT, UPDATE_DATE DATE DEFAULT (date('now', 'localtime')), FREQLOCNAME TEXT, SPACEID TEXT, SPACEIDMAIN TEXT, NETWORKNAME TEXT, NETWORKID TEXT, NETWORKFREQ TEXT, NW_TYPE INTEGER, DURATION INTEGER, POORCOUNT INTEGER, GOODCOUNT INTEGER, MODEL_VER_ALLAP TEXT, MODEL_VER_MAINAP TEXT)";
    private String CREATE_BEHAVIOR_TABLE = "CREATE TABLE IF NOT EXISTS BEHAVIOR_MAINTAIN (UPDATETIME TEXT, BATCH INTEGER)";
    private String CREATE_CHR_HISTQOERPT = "CREATE TABLE IF NOT EXISTS CHR_HISTQOERPT(FREQLOCNAME TEXT, QUERYCNT INTEGER, GOODCNT INTEGER, POORCNT INTEGER, DATARX INTEGER, DATATX INTEGER, UNKNOWNDB INTEGER, UNKNOWNSPACE INTEGER)";
    private String CREATE_CHR_LOCATION_TABLE = "CREATE TABLE IF NOT EXISTS CHR_FREQUENT_LOCATION (FREQLOCATION TEXT, FIRSTREPORT INTEGER DEFAULT 0, ENTERY INTEGER DEFAULT 0, LEAVE INTEGER DEFAULT 0, DURATION INTEGER DEFAULT 0, SPACECHANGE INTEGER DEFAULT 0, SPACELEAVE INTEGER DEFAULT 0, UPTOTALSWITCH INTEGER DEFAULT 0, UPAUTOSUCC INTEGER DEFAULT 0, UPMANUALSUCC INTEGER DEFAULT 0, UPAUTOFAIL INTEGER DEFAULT 0, UPNOSWITCHFAIL INTEGER DEFAULT 0,UPQRYCNT INTEGER DEFAULT 0, UPRESCNT INTEGER DEFAULT 0,UPUNKNOWNDB INTEGER DEFAULT 0,UPUNKNOWNSPACE INTEGER DEFAULT 0, LPTOTALSWITCH INTEGER DEFAULT 0, LPDATARX INTEGER DEFAULT 0, LPDATATX INTEGER DEFAULT 0, LPDURATION INTEGER DEFAULT 0, LPOFFSET INTEGER DEFAULT 0, LPALREADYBEST INTEGER DEFAULT 0, LPNOTREACH INTEGER DEFAULT 0, LPBACK INTEGER DEFAULT 0, LPUNKNOWNDB INTEGER DEFAULT 0, LPUNKNOWNSPACE INTEGER DEFAULT 0)";
    private String CREATE_ENTERPRISE_AP_TABLE = "CREATE TABLE IF NOT EXISTS ENTERPRISE_AP (SSID TEXT,MAC TEXT,UPTIME TEXT)";
    private String CREATE_IDENTIFY_RESULT_TABLE = "CREATE TABLE IF NOT EXISTS IDENTIFY_RESULT (SSID TEXT,PRELABLE INTEGER,SERVERMAC TEXT,UPTIME TEXT,ISMAINAP BOOLEAN,MODELNAME TEXT)";
    private String CREATE_LOCATION_TABLE = "CREATE TABLE IF NOT EXISTS FREQUENT_LOCATION (OOBTIME INTEGER DEFAULT 0, UPDATETIME INTEGER DEFAULT 0, CHRBENEFITUPLOADTIME INTEGER DEFAULT 0, CHRSPACEUSERUPLOADTIME INTEGER DEFAULT 0, FREQUENTLOCATION TEXT)";
    private String CREATE_MOBILE_AP_TABLE = "CREATE TABLE IF NOT EXISTS MOBILE_AP (SSID TEXT,MAC TEXT, UPTIME TEXT,SRCTYPE INTEGER)";
    private String CREATE_REGULAR_PLACESTATE_TABLE = "CREATE TABLE IF NOT EXISTS RGL_PLACESTATE (SSID TEXT, STATE INTEGER,BATCH INTEGER,FINGERNUM INTEGER,TEST_DAT_NUM INTEGER,DISNUM INTEGER, UPTIME TEXT,IDENTIFYNUM INTEGER,NO_OCURBSSIDS TEXT,ISMAINAP BOOLEAN,MODELNAME TEXT,BEGINTIME INTEGER)";
    private String CREATE_SPACE_TABLE_NEW = "CREATE TABLE IF NOT EXISTS SPACEUSER_BASE (SCRBID TEXT, UPDATE_DATE DATE DEFAULT (date('now', 'localtime')), FREQLOCNAME TEXT, SPACEID TEXT, SPACEIDMAIN TEXT, NETWORKNAME TEXT, NETWORKID TEXT, NETWORKFREQ TEXT, NW_TYPE INTEGER, USER_PREF_OPT_IN INTEGER, USER_PREF_OPT_OUT INTEGER, USER_PREF_STAY INTEGER, USER_PREF_TOTAL_COUNT INTEGER, POWER_CONSUMPTION INTEGER, DATA_RX INTEGER, DATA_TX INTEGER, SIGNAL_VALUE INTEGER, DURATION_CONNECTED INTEGER, MODEL_VER_ALLAP TEXT, MODEL_VER_MAINAP TEXT, DUBAI_SCREENOFF_TX INTEGER, DUBAI_SCREENOFF_RX INTEGER, DUBAI_SCREENOFF_POWER INTEGER, DUBAI_SCREENON_TX INTEGER, DUBAI_SCREENON_RX INTEGER, DUBAI_SCREENON_POWER INTEGER, DUBAI_IDLE_DURATION INTEGER, DUBAI_IDLE_POWER INTEGER)";
    private String CREATE_STA_BACK2LTE = "CREATE TABLE IF NOT EXISTS CHR_FASTBACK2LTE(FREQLOCNAME TEXT, LOWRATCNT INTEGER, INLTECNT INTEGER, OUTLTECNT INTEGER, FASTBACK INTEGER, SUCCESSBACK INTEGER, CELLS4G INTEGER, REFCNT INTEGER, UNKNOWNDB INTEGER, UNKNOWNSPACE INTEGER)";

    private DatabaseSingleton(Context context) {
        super(context, Constant.getDbPath(), null, 3);
        mQoeAppBuilder = HwHistoryQoEResourceBuilder.getInstance();
    }

    public static synchronized SQLiteDatabase getInstance() {
        SQLiteDatabase writableDatabase;
        synchronized (DatabaseSingleton.class) {
            try {
                LogUtil.i("SQLiteDatabase getInstance begin.");
                if (mDataBaseOpenHelper == null) {
                    LogUtil.d("SQLiteDatabase getInstance ,mDataBaseOpenHelper == null");
                    Context context = ContextManager.getInstance().getContext();
                    if (context == null) {
                        LogUtil.d(" context is null ");
                    }
                    Constant.checkPath(context);
                    mDataBaseOpenHelper = new DatabaseSingleton(context);
                    LogUtil.i(" DatabaseSingleton init db sucess");
                }
                writableDatabase = mDataBaseOpenHelper.getWritableDatabase();
            } catch (Exception e) {
                LogUtil.e(" Exception SQLiteDatabase getInstance: " + e + " cons.DATABASE_FILE_PATH :" + Constant.getDbPath());
                return null;
            }
        }
        return writableDatabase;
    }

    public void onCreate(SQLiteDatabase db) {
        String sql;
        String sql2;
        String sql3;
        String sql4;
        String sql5;
        String sql6;
        String sql7;
        try {
            db.beginTransaction();
            LogUtil.i("onCreate,sql01=" + sql);
            db.execSQL(sql);
            db.execSQL(this.CREATE_ENTERPRISE_AP_TABLE);
            db.execSQL(this.CREATE_MOBILE_AP_TABLE);
            db.execSQL(this.CREATE_IDENTIFY_RESULT_TABLE);
            LogUtil.i("onCreate,sql02=" + sql);
            LogUtil.i("onCreate,sql04=" + sql2);
            db.execSQL(sql2);
            LogUtil.i("onCreate,sql05=" + sql3);
            db.execSQL(sql3);
            LogUtil.i("onCreate,sql06=" + sql4);
            db.execSQL(sql4);
            LogUtil.i("onCreate,sql07=" + sql5);
            db.execSQL(sql5);
            LogUtil.i("onCreate,sql08=" + sql6);
            db.execSQL(sql6);
            LogUtil.i("onCreate,sql09=" + sql7);
            db.execSQL(sql7);
            Iterator<Map.Entry<Integer, Float>> it = mQoeAppBuilder.getQoEAppList().entrySet().iterator();
            while (it.hasNext()) {
                String tableName = Constant.USERDB_APP_NAME_PREFIX + it.next().getKey();
                LogUtil.i("onCreate,app_sql=" + sql);
                db.execSQL(this.CREATE_APP_TABLE_FRONT + tableName + this.CREATE_APP_TABLE_TITLE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.e(" Exception onCreate: " + e);
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.i("onUpgrade: " + oldVersion + " to " + newVersion);
        switch (oldVersion) {
            case 1:
                db.execSQL("DROP TABLE IF EXISTS RGL_PLACESTATE");
                db.execSQL("DROP TABLE IF EXISTS ENTERPRISE_AP");
                db.execSQL("DROP TABLE IF EXISTS MOBILE_AP");
                db.execSQL("DROP TABLE IF EXISTS IDENTIFY_RESULT");
                db.execSQL("DROP TABLE IF EXISTS SPACEUSER_BASE");
                db.execSQL("DROP TABLE IF EXISTS BEHAVIOR_MAINTAIN");
                db.execSQL("DROP TABLE IF EXISTS FREQUENT_LOCATION");
                db.execSQL("DROP TABLE IF EXISTS CHR_FREQUENT_LOCATION");
                db.execSQL("DROP TABLE IF EXISTS CHR_HISTQOERPT");
                db.execSQL("DROP TABLE IF EXISTS CHR_FASTBACK2LTE");
                break;
            case 2:
                try {
                    db.execSQL("ALTER TABLE SPACEUSER_BASE ADD COLUMN DUBAI_SCREENOFF_TX INTEGER");
                    db.execSQL("ALTER TABLE SPACEUSER_BASE ADD COLUMN DUBAI_SCREENOFF_RX INTEGER");
                    db.execSQL("ALTER TABLE SPACEUSER_BASE ADD COLUMN DUBAI_SCREENOFF_POWER INTEGER");
                    db.execSQL("ALTER TABLE SPACEUSER_BASE ADD COLUMN DUBAI_SCREENON_TX INTEGER");
                    db.execSQL("ALTER TABLE SPACEUSER_BASE ADD COLUMN DUBAI_SCREENON_RX INTEGER");
                    db.execSQL("ALTER TABLE SPACEUSER_BASE ADD COLUMN DUBAI_SCREENON_POWER INTEGER");
                    db.execSQL("ALTER TABLE SPACEUSER_BASE ADD COLUMN DUBAI_IDLE_DURATION INTEGER");
                    db.execSQL("ALTER TABLE SPACEUSER_BASE ADD COLUMN DUBAI_IDLE_POWER INTEGER");
                    break;
                } catch (Exception e) {
                    LogUtil.e(" Exception onUpgrade: " + e);
                    return;
                }
        }
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.i("onUpgrade: " + oldVersion + " to " + newVersion);
        try {
            db.execSQL("DROP TABLE IF EXISTS RGL_PLACESTATE");
            db.execSQL("DROP TABLE IF EXISTS ENTERPRISE_AP");
            db.execSQL("DROP TABLE IF EXISTS MOBILE_AP");
            db.execSQL("DROP TABLE IF EXISTS IDENTIFY_RESULT");
            db.execSQL("DROP TABLE IF EXISTS SPACEUSER_BASE");
            db.execSQL("DROP TABLE IF EXISTS BEHAVIOR_MAINTAIN");
            onCreate(db);
        } catch (Exception e) {
            LogUtil.e(" Exception onDowngrade: " + e);
        }
    }

    public synchronized void onOpen(SQLiteDatabase db) {
        LogUtil.i("onOpen");
        try {
            if (!db.isReadOnly()) {
                db.beginTransaction();
                Iterator<Map.Entry<Integer, Float>> it = mQoeAppBuilder.getQoEAppList().entrySet().iterator();
                while (it.hasNext()) {
                    String tableName = Constant.USERDB_APP_NAME_PREFIX + it.next().getKey();
                    LogUtil.i("onOpen,app_sql=" + sql);
                    db.execSQL(this.CREATE_APP_TABLE_FRONT + tableName + this.CREATE_APP_TABLE_TITLE);
                }
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            try {
                LogUtil.e(" Exception onOpen: " + e);
            } catch (Throwable th) {
                db.endTransaction();
                throw th;
            }
        }
        db.endTransaction();
        super.onOpen(db);
    }
}
