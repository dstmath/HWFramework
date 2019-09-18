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
        if (LSS_DB_BACKUP) {
            Map<String, ?> map = getAllData();
            if (map != null && map.size() > 0) {
                String userIdString = String.valueOf(userId);
                SharedPreferences.Editor edit = this.mSpImpl.edit();
                for (String key : map.keySet()) {
                    String[] result = splitKey(key);
                    if (result == null || result.length != 2) {
                        edit.remove(key);
                    } else if (userIdString.equals(result[0])) {
                        edit.remove(key);
                    }
                }
                edit.commit();
            }
        }
    }

    public void writeBackItemData(String key, String value, int userId) {
        if (LSS_DB_BACKUP) {
            this.mSpImpl.edit().putString(getUserKey(userId, key), value).apply();
        }
    }

    public void restoreDataFromXml(SQLiteDatabase db) {
        if (LSS_DB_BACKUP && db != null) {
            Map<String, ?> map = getAllData();
            if (map != null && map.size() > 0) {
                Slog.w(TAG, "restore lss xml data to lss db");
                db.beginTransaction();
                try {
                    for (String key : map.keySet()) {
                        String[] result = splitKey(key);
                        if (result != null) {
                            if (result.length == 2) {
                                String userId = result[0];
                                String name = result[1];
                                String value = map.get(key).toString();
                                ContentValues cv = new ContentValues();
                                cv.put("name", name);
                                cv.put(COLUMN_USERID, userId);
                                cv.put("value", value);
                                db.delete(TABLE, "name=? AND user=?", new String[]{name, userId});
                                db.insert(TABLE, null, cv);
                            }
                        }
                        db.endTransaction();
                        return;
                    }
                    db.setTransactionSuccessful();
                } catch (SQLiteException | OperationCanceledException | SecurityException ex) {
                    Slog.w(TAG, "restore data from xml ex = " + ex.toString());
                } catch (Throwable th) {
                    db.endTransaction();
                    throw th;
                }
                db.endTransaction();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005c, code lost:
        if (r1 != null) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005e, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0080, code lost:
        if (r1 == null) goto L_0x0083;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0083, code lost:
        return;
     */
    public void syncDataToXmlFile(SQLiteDatabase db) {
        if (LSS_DB_BACKUP && db != null) {
            Map<String, ?> map = getAllData();
            if (map != null && map.size() <= 0) {
                Cursor cursor = null;
                try {
                    cursor = db.query(TABLE, null, null, null, null, null, null);
                    if (cursor != null) {
                        SharedPreferences.Editor edit = this.mSpImpl.edit();
                        while (cursor.moveToNext()) {
                            int userId = Integer.parseInt(getString(cursor, COLUMN_USERID, "1"));
                            String key = getString(cursor, "name", "");
                            edit.putString(getUserKey(userId, key), getString(cursor, "value", ""));
                        }
                        edit.commit();
                    }
                } catch (SQLiteException | OperationCanceledException | SecurityException ex) {
                    Slog.w(TAG, "sync data to xml ex = " + ex.toString());
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
        }
    }

    private Map<String, ?> getAllData() {
        if (this.mSpImpl != null) {
            return this.mSpImpl.getAll();
        }
        return null;
    }

    private String getUserKey(int userId, String key) {
        return "U" + String.valueOf(userId) + "_" + key;
    }

    private String[] splitKey(String key) {
        if (key == null || key.isEmpty() || key.charAt(0) != 'U') {
            return null;
        }
        int index = key.indexOf("_", 0);
        if (index < 0) {
            return null;
        }
        return new String[]{key.substring(1, index), key.substring(index + 1, key.length())};
    }

    private String getString(Cursor cursor, String column, String defValue) {
        String rt = defValue;
        if (cursor != null) {
            int index = cursor.getColumnIndex(column);
            if (index > -1) {
                return cursor.getString(index);
            }
            Slog.w(TAG, "column = " + column + ", index = " + index);
            return rt;
        }
        Slog.w(TAG, "column = " + column + ", cursor is null");
        return rt;
    }
}
