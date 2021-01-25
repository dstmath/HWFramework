package com.huawei.server.security.permissionmanager;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.huawei.android.util.SlogEx;
import com.huawei.server.security.permissionmanager.util.CursorHelper;
import com.huawei.server.security.permissionmanager.util.PermConst;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HwPermDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DB_NAME = "hw_permission.db";
    private static final int DEFAULT_CAPACITY_ARRAY = 10;
    private static final int DEFAULT_ROW = 0;
    private static final String GET_TABLE_NAME = "select name from sqlite_master where type='table' order by name";
    private static final int ILLEGAL_ROW = -1;
    private static final int ILLEGAL_USER_ID = -1;
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "HwPermDbHelper";
    private static final int OWNER_USER_ID = 0;
    private static volatile HwPermDbHelper sUniqueInstance = null;
    private Context mContext = null;
    private SQLiteDatabase mDatabase;
    private ExecutorService mPermissionSingleExecutor = null;
    private BroadcastReceiver mUserChangeReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.security.permissionmanager.HwPermDbHelper.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null) {
                String action = intent.getAction();
                if (TextUtils.isEmpty(action)) {
                    SlogEx.e(HwPermDbHelper.LOG_TAG, "onReceive action is null");
                    return;
                }
                SlogEx.v(HwPermDbHelper.LOG_TAG, "mUserChangeReceiver action = " + action);
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if ("android.intent.action.USER_REMOVED".equals(action)) {
                    HwPermDbHelper.this.dropPermTable(HwPermDbAdapter.getPermCfgTableName(userId));
                    HwPermDbAdapter.getInstance(HwPermDbHelper.this.mContext).removeHwPermissionCache(null, userId);
                } else if ("android.intent.action.USER_ADDED".equals(action)) {
                    HwPermDbHelper.this.createPermissionCfgTableAsUser(userId);
                } else {
                    SlogEx.e(HwPermDbHelper.LOG_TAG, "error Intent");
                }
            }
        }
    };

    private HwPermDbHelper(@NonNull Context context) {
        super(context, DB_NAME, (SQLiteDatabase.CursorFactory) null, 2);
        this.mContext = context;
        openDatabase();
        registerUserChange();
        if (this.mPermissionSingleExecutor == null) {
            this.mPermissionSingleExecutor = Executors.newSingleThreadExecutor();
        }
        SlogEx.v(LOG_TAG, "create HwPermDbHelper:");
    }

    public static HwPermDbHelper getInstance(@NonNull Context context) {
        if (sUniqueInstance == null) {
            synchronized (LOCK) {
                if (sUniqueInstance == null) {
                    sUniqueInstance = new HwPermDbHelper(context);
                }
            }
        }
        return sUniqueInstance;
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(final SQLiteDatabase sqLiteDatabase) {
        SlogEx.i(LOG_TAG, "onCreate HwPermDbHelper");
        this.mPermissionSingleExecutor = Executors.newSingleThreadExecutor();
        this.mPermissionSingleExecutor.submit(new Runnable() {
            /* class com.huawei.server.security.permissionmanager.HwPermDbHelper.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                HwPermDbHelper.this.createPermissionCfgTable(sqLiteDatabase, 0);
            }
        });
        this.mPermissionSingleExecutor.submit(new Runnable() {
            /* class com.huawei.server.security.permissionmanager.HwPermDbHelper.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                HwPermDbHelper.this.createSmsBlockTable(sqLiteDatabase, 0);
            }
        });
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(final SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        SlogEx.i(LOG_TAG, "onUpgrade oldVersion:" + oldVersion + " newVersion " + newVersion);
        if (this.mPermissionSingleExecutor == null) {
            this.mPermissionSingleExecutor = Executors.newSingleThreadExecutor();
        }
        this.mPermissionSingleExecutor.submit(new Runnable() {
            /* class com.huawei.server.security.permissionmanager.HwPermDbHelper.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                HwPermDbHelper.this.createSmsBlockTable(sqLiteDatabase, 0);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createPermissionCfgTable(@NonNull SQLiteDatabase db, int userId) {
        String tableName = HwPermDbAdapter.getPermCfgTableName(userId);
        db.execSQL("create table if not exists " + tableName + " ( " + PermConst.PACKAGE_NAME + " text primary key, uid int DEFAULT (-1), " + PermConst.PERMISSION_CODE + " bigint DEFAULT (0), " + PermConst.PERMISSION_CFG + " bigint DEFAULT (0));");
        StringBuilder sb = new StringBuilder();
        sb.append("has created table: ");
        sb.append(tableName);
        SlogEx.i(LOG_TAG, sb.toString());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createSmsBlockTable(@NonNull SQLiteDatabase db, int userId) {
        String tableName = HwSmsBlockDbAdapter.getSmsBlockTableName(userId);
        db.execSQL("create table if not exists " + tableName + " ( " + PermConst.PACKAGE_NAME + " text primary key, " + PermConst.SUB_PERMISSION + " int DEFAULT (0), " + PermConst.PRE_DEFINED_SMS_CONFIG + " int DEFAULT (0));");
        StringBuilder sb = new StringBuilder();
        sb.append("has created table: ");
        sb.append(tableName);
        SlogEx.i(LOG_TAG, sb.toString());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void openDatabase() {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase == null) {
            this.mDatabase = getWritableDatabase();
            SlogEx.i(LOG_TAG, "HwPermDbHelper open Database:" + this.mDatabase);
        } else if (!new File(sQLiteDatabase.getPath()).exists()) {
            SlogEx.i(LOG_TAG, " db file is not exist, close db ");
            closeDatabase();
        }
    }

    private void closeDatabase() {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase != null) {
            sQLiteDatabase.close();
            this.mDatabase = null;
        }
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SlogEx.i(LOG_TAG, "onDowngrade oldVersion:" + oldVersion + " newVersion " + newVersion);
    }

    /* access modifiers changed from: package-private */
    public Cursor query(@NonNull String table, @Nullable String selection, @Nullable String[] selectionArgs) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db != null) {
                return db.query(table, null, selection, selectionArgs, null, null, null);
            }
            SlogEx.e(LOG_TAG, "query db is null");
            return null;
        } catch (SQLException e) {
            SlogEx.e(LOG_TAG, "query SQLException");
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void delete(@NonNull final String tableName, @Nullable final String whereClause, @Nullable final String[] whereArgs) {
        this.mPermissionSingleExecutor.submit(new Runnable() {
            /* class com.huawei.server.security.permissionmanager.HwPermDbHelper.AnonymousClass5 */

            @Override // java.lang.Runnable
            public void run() {
                HwPermDbHelper.this.deleteRunnable(tableName, whereClause, whereArgs);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deleteRunnable(String tableName, String whereClause, String[] whereArgs) {
        openDatabase();
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase == null) {
            SlogEx.e(LOG_TAG, "mDatabase can't be null");
            return;
        }
        try {
            int result = sQLiteDatabase.delete(tableName, whereClause, whereArgs);
            SlogEx.v(LOG_TAG, "delete result: " + result);
        } catch (SQLException e) {
            SlogEx.e(LOG_TAG, "db delete Exception");
        }
    }

    /* access modifiers changed from: package-private */
    public void update(@NonNull final String tableName, @NonNull final ContentValues values, @Nullable final String whereClause, @Nullable final String[] whereArgs) {
        this.mPermissionSingleExecutor.submit(new Runnable() {
            /* class com.huawei.server.security.permissionmanager.HwPermDbHelper.AnonymousClass6 */

            @Override // java.lang.Runnable
            public void run() {
                HwPermDbHelper.this.updateRunnable(tableName, values, whereClause, whereArgs);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRunnable(String tableName, ContentValues values, String whereClause, String[] args) {
        openDatabase();
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase == null) {
            SlogEx.e(LOG_TAG, "mDatabase can't be null");
            return;
        }
        try {
            int result = sQLiteDatabase.update(tableName, values, whereClause, args);
            SlogEx.v(LOG_TAG, "update result: " + result);
        } catch (SQLException e) {
            SlogEx.e(LOG_TAG, "db delete Exception");
        }
    }

    /* access modifiers changed from: package-private */
    public void insert(@NonNull final ContentValues values, @NonNull final String tableName) {
        this.mPermissionSingleExecutor.submit(new Runnable() {
            /* class com.huawei.server.security.permissionmanager.HwPermDbHelper.AnonymousClass7 */

            @Override // java.lang.Runnable
            public void run() {
                HwPermDbHelper.this.insertRunnable(values, tableName);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void insertRunnable(ContentValues values, String tableName) {
        openDatabase();
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase == null) {
            SlogEx.e(LOG_TAG, "mDatabase can't be null");
            return;
        }
        long result = sQLiteDatabase.insert(tableName, null, values);
        SlogEx.v(LOG_TAG, "insert result: " + result);
    }

    /* access modifiers changed from: package-private */
    public void replace(@NonNull final ContentValues value, @NonNull final String tableName) {
        this.mPermissionSingleExecutor.submit(new Runnable() {
            /* class com.huawei.server.security.permissionmanager.HwPermDbHelper.AnonymousClass8 */

            @Override // java.lang.Runnable
            public void run() {
                HwPermDbHelper.this.replaceRunnable(value, tableName);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void replaceRunnable(ContentValues value, String tableName) {
        openDatabase();
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase == null) {
            SlogEx.e(LOG_TAG, "mDatabase can't be null");
            return;
        }
        long result = sQLiteDatabase.replace(tableName, null, value);
        SlogEx.v(LOG_TAG, "replace result: " + result);
    }

    private void registerUserChange() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_ADDED");
        filter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(this.mUserChangeReceiver, filter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dropPermTable(@NonNull final String tableName) {
        this.mPermissionSingleExecutor.submit(new Runnable() {
            /* class com.huawei.server.security.permissionmanager.HwPermDbHelper.AnonymousClass9 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    HwPermDbHelper.this.openDatabase();
                    if (HwPermDbHelper.this.mDatabase == null) {
                        SlogEx.e(HwPermDbHelper.LOG_TAG, "mDatabase can't be null");
                        return;
                    }
                    SQLiteDatabase sQLiteDatabase = HwPermDbHelper.this.mDatabase;
                    sQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + tableName);
                } catch (SQLiteException e) {
                    SlogEx.e(HwPermDbHelper.LOG_TAG, "dropPermTable SQLiteException");
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createPermissionCfgTableAsUser(final int userId) {
        this.mPermissionSingleExecutor.submit(new Runnable() {
            /* class com.huawei.server.security.permissionmanager.HwPermDbHelper.AnonymousClass10 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    HwPermDbHelper.this.openDatabase();
                    if (HwPermDbHelper.this.mDatabase == null) {
                        SlogEx.e(HwPermDbHelper.LOG_TAG, "mDatabase can't be null");
                    } else {
                        HwPermDbHelper.this.createPermissionCfgTable(HwPermDbHelper.this.mDatabase, userId);
                    }
                } catch (SQLiteException e) {
                    SlogEx.e(HwPermDbHelper.LOG_TAG, "dropPermTable SQLiteException");
                }
            }
        });
    }

    public ArrayList<String> getTableNames() {
        ArrayList<String> list = new ArrayList<>(10);
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db == null) {
                CursorHelper.closeCursor(null);
                return list;
            }
            cursor = db.rawQuery(GET_TABLE_NAME, null);
            if (CursorHelper.checkCursorValid(cursor)) {
                while (cursor.moveToNext()) {
                    list.add(cursor.getString(0));
                }
            }
            CursorHelper.closeCursor(cursor);
            return list;
        } catch (SQLiteException e) {
            SlogEx.e(LOG_TAG, "getAllAppsPermInfo error");
        } catch (Throwable th) {
            CursorHelper.closeCursor(null);
            throw th;
        }
    }

    public void removeAndUpdateAllForOneTimePerm(final String infoType, final List<ContentValues> valueList) {
        if (TextUtils.isEmpty(infoType) || valueList == null) {
            SlogEx.w(LOG_TAG, "get invalid parameter");
        } else {
            this.mPermissionSingleExecutor.submit(new Runnable() {
                /* class com.huawei.server.security.permissionmanager.HwPermDbHelper.AnonymousClass11 */

                @Override // java.lang.Runnable
                public void run() {
                    String columnName;
                    try {
                        HwPermDbHelper.this.openDatabase();
                        if (HwPermDbHelper.this.mDatabase == null) {
                            SlogEx.e(HwPermDbHelper.LOG_TAG, "mDatabase can't be null");
                            if (HwPermDbHelper.this.mDatabase != null) {
                                HwPermDbHelper.this.mDatabase.endTransaction();
                                return;
                            }
                            return;
                        }
                        HwPermDbHelper.this.mDatabase.beginTransaction();
                        SQLiteDatabase sQLiteDatabase = HwPermDbHelper.this.mDatabase;
                        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PermConst.ONE_TIME_PERM_TABLE_NAME);
                        if (PermConst.ONE_TIME_PERM_TYPE_SUPPORT.equals(infoType)) {
                            columnName = PermConst.COLUMN_SUPPORTED_PACKAGE_NAME;
                        } else if (PermConst.ONE_TIME_PERM_TYPE_BLOCK.equals(infoType)) {
                            columnName = PermConst.COLUMN_BLOCKED_PACKAGE_NAME;
                        } else {
                            SlogEx.e(HwPermDbHelper.LOG_TAG, "get invalid type, update table fail.");
                            if (HwPermDbHelper.this.mDatabase != null) {
                                HwPermDbHelper.this.mDatabase.endTransaction();
                                return;
                            }
                            return;
                        }
                        SQLiteDatabase sQLiteDatabase2 = HwPermDbHelper.this.mDatabase;
                        sQLiteDatabase2.execSQL("create table if not exists " + PermConst.ONE_TIME_PERM_TABLE_NAME + " ( " + columnName + " text primary key);");
                        for (ContentValues values : valueList) {
                            HwPermDbHelper.this.mDatabase.insert(PermConst.ONE_TIME_PERM_TABLE_NAME, null, values);
                        }
                        HwPermDbHelper.this.mDatabase.setTransactionSuccessful();
                        SlogEx.i(HwPermDbHelper.LOG_TAG, "update all finish.");
                        if (HwPermDbHelper.this.mDatabase == null) {
                            return;
                        }
                        HwPermDbHelper.this.mDatabase.endTransaction();
                    } catch (SQLiteException e) {
                        SlogEx.e(HwPermDbHelper.LOG_TAG, "removeAndUpdateAllForOneTimePerm error.");
                        if (HwPermDbHelper.this.mDatabase == null) {
                        }
                    } catch (Exception e2) {
                        SlogEx.e(HwPermDbHelper.LOG_TAG, "removeAndUpdateAllForOneTimePerm get unexpected exception.");
                        if (HwPermDbHelper.this.mDatabase == null) {
                        }
                    } catch (Throwable th) {
                        if (HwPermDbHelper.this.mDatabase != null) {
                            HwPermDbHelper.this.mDatabase.endTransaction();
                        }
                        throw th;
                    }
                }
            });
        }
    }
}
