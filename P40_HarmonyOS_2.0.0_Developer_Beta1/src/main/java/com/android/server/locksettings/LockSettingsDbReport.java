package com.android.server.locksettings;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.OperationCanceledException;
import android.os.SystemProperties;
import android.util.Slog;
import java.io.File;
import java.util.Map;

public class LockSettingsDbReport {
    private static final String COLUMN_KEY = "name";
    private static final String COLUMN_USERID = "user";
    private static final String COLUMN_VALUE = "value";
    private static final String DB_FILE_PREFIX = "lock_settings_db";
    private static final String DELIMETER = "_";
    private static String FILE_DB_DIRECTORY = (File.separator + "data" + File.separator + "system_de" + File.separator + "lss_data");
    private static final String FILE_DB_PATH;
    private static final boolean LSS_DB_BACKUP = SystemProperties.getBoolean("ro.config.lss_db_backup", true);
    private static final int SIZE = 2;
    private static final String TABLE = "locksettings";
    private static final String TAG = "LockSettingsDbReport";
    private Context mContext;
    private SharedPreferences mSpImpl;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append(FILE_DB_DIRECTORY);
        sb.append(File.separator);
        sb.append(DB_FILE_PREFIX);
        sb.append(".xml");
        FILE_DB_PATH = sb.toString();
    }

    public LockSettingsDbReport(Context context) {
        this.mContext = context;
        File pFile = new File(FILE_DB_DIRECTORY);
        if (!pFile.exists() && !pFile.mkdirs()) {
            Slog.i(TAG, "create xml directory fail:" + FILE_DB_DIRECTORY);
        }
        if (LSS_DB_BACKUP) {
            this.mSpImpl = context.createDeviceProtectedStorageContext().getSharedPreferences(new File(FILE_DB_PATH), 0);
        }
    }

    public void removeUserInfo(int userId) {
        Map<String, ?> map;
        if (LSS_DB_BACKUP && (map = getAllData()) != null && map.size() > 0) {
            String userIdString = String.valueOf(userId);
            SharedPreferences.Editor edit = this.mSpImpl.edit();
            for (String key : map.keySet()) {
                String[] result = splitKey(key);
                if (result.length != 2) {
                    edit.remove(key);
                } else if (userIdString.equals(result[0])) {
                    edit.remove(key);
                }
            }
            edit.commit();
        }
    }

    public void writeBackItemData(String key, String value, int userId) {
        if (LSS_DB_BACKUP) {
            this.mSpImpl.edit().putString(getUserKey(userId, key), value).apply();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0081, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        android.util.Slog.w(com.android.server.locksettings.LockSettingsDbReport.TAG, "restore data from xml ex = " + r0.toString());
     */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0081 A[ExcHandler: SQLiteException | OperationCanceledException | SecurityException (r0v1 'ex' java.lang.RuntimeException A[CUSTOM_DECLARE]), Splitter:B:15:0x0043] */
    public void restoreDataFromXml(SQLiteDatabase db) {
        Map<String, ?> map;
        if (LSS_DB_BACKUP && db != null && (map = getAllData()) != null && map.size() > 0) {
            Slog.w(TAG, "restore lss xml data to lss db");
            db.beginTransaction();
            for (String key : map.keySet()) {
                String[] result = splitKey(key);
                if (result.length != 2) {
                    db.endTransaction();
                    return;
                }
                try {
                    String userId = result[0];
                    String name = result[1];
                    String value = map.get(key).toString();
                    ContentValues cv = new ContentValues();
                    cv.put("name", name);
                    cv.put(COLUMN_USERID, userId);
                    cv.put("value", value);
                    db.delete(TABLE, "name=? AND user=?", new String[]{name, userId});
                    db.insert(TABLE, null, cv);
                } catch (SQLiteException | OperationCanceledException | SecurityException ex) {
                } catch (Throwable th) {
                    db.endTransaction();
                    throw th;
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public void syncDataToXmlFile(SQLiteDatabase db) {
        Map<String, ?> map;
        if (LSS_DB_BACKUP && db != null && (map = getAllData()) != null && map.size() <= 0) {
            Cursor cursor = null;
            try {
                cursor = db.query(TABLE, null, null, null, null, null, null);
                if (cursor != null) {
                    SharedPreferences.Editor edit = this.mSpImpl.edit();
                    while (cursor.moveToNext()) {
                        edit.putString(getUserKey(Integer.parseInt(getString(cursor, COLUMN_USERID, "1")), getString(cursor, "name", "")), getString(cursor, "value", ""));
                    }
                    edit.commit();
                }
                if (cursor == null) {
                    return;
                }
            } catch (SQLiteException | OperationCanceledException | SecurityException ex) {
                Slog.w(TAG, "sync data to xml ex = " + ex.toString());
                if (0 == 0) {
                    return;
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
            cursor.close();
        }
    }

    private Map<String, ?> getAllData() {
        SharedPreferences sharedPreferences = this.mSpImpl;
        if (sharedPreferences != null) {
            return sharedPreferences.getAll();
        }
        return null;
    }

    private String getUserKey(int userId, String key) {
        return "U" + String.valueOf(userId) + "_" + key;
    }

    private String[] splitKey(String key) {
        if (key == null || key.isEmpty() || key.charAt(0) != 'U') {
            return new String[0];
        }
        int index = key.indexOf("_", 0);
        return index < 0 ? new String[0] : new String[]{key.substring(1, index), key.substring(index + 1, key.length())};
    }

    private String getString(Cursor cursor, String column, String defValue) {
        if (cursor != null) {
            int index = cursor.getColumnIndex(column);
            if (index > -1) {
                return cursor.getString(index);
            }
            Slog.w(TAG, "column = " + column + ", index = " + index);
            return defValue;
        }
        Slog.w(TAG, "column = " + column + ", cursor is null.");
        return defValue;
    }
}
