package com.android.server.hidata.wavemapping.dao;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.ArrayList;
import java.util.List;

public class DatabaseDao {
    private static final int DEFAULT_CAPACITY = 10;
    private static final String DROP_TABLE_PREFIX = "DROP TABLE IF EXISTS ";
    private static final String SELECT_NAME_PREFIX = "select name from sqlite_master where type='table' and name like '%";
    private static final String SELECT_SUFFIX = "%';";
    private static final String TAG = ("WMapping." + DatabaseDao.class.getSimpleName());
    private SQLiteDatabase db = DatabaseSingleton.getInstance();
    private String tempTableName = "CREATE TABLE IF NOT EXISTS _TEMP_STD (LOCATION VARCHAR(200), BATCH VARCHAR(20),DATAS TEXT)";

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0066, code lost:
        if (0 == 0) goto L_0x0069;
     */
    public List<String> findTableName(String key) {
        String sql = SELECT_NAME_PREFIX + key + SELECT_SUFFIX;
        LogUtil.i(false, " findTableName :sql=%{public}s", sql);
        Cursor cursor = null;
        List<String> tables = new ArrayList<>(10);
        try {
            cursor = this.db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "findTableName IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "findTableName failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return tables;
    }

    public boolean createTempStdDataTable(String location) {
        try {
            String str = this.tempTableName;
            String sql = str.replace(Constant.TEMP_STD_DATA_TABLE_NAME, location + Constant.TEMP_STD_DATA_TABLE_NAME);
            LogUtil.i(false, "createTempStdDataTable:%{public}s", sql);
            this.db.execSQL(sql);
        } catch (SQLException e) {
            LogUtil.e(false, "createTempStdDataTable,e %{public}s", e.getMessage());
        }
        return true;
    }

    public boolean dropTempStdDataTable(String location) {
        try {
            SQLiteDatabase sQLiteDatabase = this.db;
            sQLiteDatabase.execSQL(DROP_TABLE_PREFIX + location + Constant.TEMP_STD_DATA_TABLE_NAME);
        } catch (SQLException e) {
            LogUtil.e(false, "dropTempStdDataTable:%{public}s", e.getMessage());
        }
        return true;
    }
}
