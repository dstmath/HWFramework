package android.rms.iaware.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AppTypeRecoManager.AppTypeCacheInfo;
import android.rms.iaware.AwareConstant.Database;
import android.rms.iaware.AwareLog;
import android.text.TextUtils;
import java.util.List;
import java.util.Map;

public class AppTypeRecoUtils {
    private static final int APPLY_BATCH_COUNT = 100;
    private static final long ONE_DAY = 86400000;
    public static final int RECOG_SOURCE_CLOUD = 3;
    private static final int RECOG_SOURCE_CLOUD_DURATION = 30;
    public static final int RECOG_SOURCE_CUST_UPGRADE = 4;
    public static final int RECOG_SOURCE_GAME_DYNAMIC = 6;
    public static final int RECOG_SOURCE_GAME_ENEGINE = 5;
    public static final int RECOG_SOURCE_PG = 2;
    public static final int RECOG_SOURCE_PRERECOG = 0;
    public static final int RECOG_SOURCE_RECOG = 1;
    private static final String TAG = "AppTypeRecoUtils";
    private static final String WHERECLAUSE = "appPkgName =? ";

    public static void insertAppTypeInfo(ContentResolver resolver, String pkgName, int version, int type, int source, int attr) {
        if (resolver != null) {
            ContentValues cvs = new ContentValues();
            cvs.put("appPkgName", pkgName);
            cvs.put("recogVersion", Integer.valueOf(version));
            cvs.put(AppTypeRecoManager.APP_TYPE, Integer.valueOf(type));
            cvs.put("source", Integer.valueOf(source));
            cvs.put("typeAttri", Integer.valueOf(attr));
            if (source == 3) {
                cvs.put("recogTime", Long.valueOf(System.currentTimeMillis()));
            }
            try {
                resolver.insert(Database.APPTYPE_URI, cvs);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: insert AppType ");
            }
        }
    }

    public static void bulkInsertAppTypeInfo(ContentResolver resolver, List<ContentValues> listContentValues) {
        if (resolver != null && listContentValues != null) {
            int i;
            int repeatCount = listContentValues.size() / 100;
            for (i = 0; i < repeatCount; i++) {
                ContentValues[] transValues = new ContentValues[100];
                for (int j = 0; j < 100; j++) {
                    transValues[j] = (ContentValues) listContentValues.get((i * 100) + j);
                }
                try {
                    resolver.bulkInsert(Database.APPTYPE_URI, transValues);
                } catch (SQLiteException e) {
                    AwareLog.e(TAG, "Error: bulkInsert AppType ");
                }
            }
            int resetCount = listContentValues.size() % 100;
            ContentValues[] resetTransValues = new ContentValues[resetCount];
            for (i = 0; i < resetCount; i++) {
                resetTransValues[i] = (ContentValues) listContentValues.get((repeatCount * 100) + i);
            }
            try {
                resolver.bulkInsert(Database.APPTYPE_URI, resetTransValues);
            } catch (SQLiteException e2) {
                AwareLog.e(TAG, "Error: bulkInsert AppType ");
            }
        }
    }

    public static void deleteAppTypeInfo(ContentResolver resolver, String pkgName) {
        if (resolver != null && pkgName != null) {
            try {
                resolver.delete(Database.APPTYPE_URI, WHERECLAUSE, new String[]{pkgName});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteAppTypeInfo ");
            }
        }
    }

    public static void updateAppTypeInfo(ContentResolver resolver, String pkgName, int version, int type, int source) {
        if (resolver != null) {
            ContentValues cvs = new ContentValues();
            cvs.put("recogVersion", Integer.valueOf(version));
            cvs.put(AppTypeRecoManager.APP_TYPE, Integer.valueOf(type));
            cvs.put("source", Integer.valueOf(source));
            if (source == 3) {
                cvs.put("recogTime", Long.valueOf(System.currentTimeMillis()));
            }
            try {
                resolver.update(Database.APPTYPE_URI, cvs, WHERECLAUSE, new String[]{pkgName});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: update AppType ");
            }
        }
    }

    public static void updateAppTypeInfo(ContentResolver resolver, String pkgName, ContentValues cvs) {
        if (resolver != null && cvs != null) {
            try {
                resolver.update(Database.APPTYPE_URI, cvs, WHERECLAUSE, new String[]{pkgName});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: update appType");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: updateAppTypeInfo IllegalStateException");
            }
        }
    }

    public static void loadAppType(ContentResolver resolver, Map<String, AppTypeCacheInfo> typeMap) {
        if (resolver != null && typeMap != null) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(Database.APPTYPE_URI, new String[]{"appPkgName", "typeAttri", AppTypeRecoManager.APP_TYPE, "source"}, null, null, null);
                if (cursor == null) {
                    AwareLog.e(TAG, "loadAppType cursor is null.");
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                }
                while (cursor.moveToNext()) {
                    String pkgName = cursor.getString(0);
                    int attri = cursor.getInt(1);
                    int apptype = cursor.getInt(2);
                    int source = cursor.getInt(3);
                    if (!TextUtils.isEmpty(pkgName)) {
                        typeMap.put(pkgName, new AppTypeCacheInfo(apptype, attri, source));
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadAppType SQLiteException");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadAppType IllegalStateException");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public static void loadAppTypeForCloudRecog(ContentResolver resolver, Map<String, Integer> map, int curCloudRecogVersion) {
        if (resolver != null && map != null) {
            Cursor cursor = null;
            try {
                ContentResolver contentResolver = resolver;
                cursor = contentResolver.query(Database.APPTYPE_URI, new String[]{"appPkgName", AppTypeRecoManager.APP_TYPE, "recogVersion", "recogTime", "source"}, "source != 0 AND source != 4 AND source != 1", null, null);
                if (cursor == null) {
                    AwareLog.e(TAG, "loadAppTypeForCloudRecog cursor is null.");
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                }
                while (cursor.moveToNext()) {
                    String pkgName = cursor.getString(0);
                    if (!TextUtils.isEmpty(pkgName)) {
                        int type = cursor.getInt(1);
                        int lastRecogVersion = cursor.getInt(2);
                        long lastRecogTime = cursor.getLong(3);
                        int source = cursor.getInt(4);
                        long diffDays = 0;
                        if (lastRecogTime != 0) {
                            diffDays = Math.abs(System.currentTimeMillis() - lastRecogTime) / 86400000;
                        }
                        if ((source == 3 || lastRecogTime == 0) && (source != 3 || lastRecogVersion < curCloudRecogVersion || diffDays > 30)) {
                            map.put(pkgName, Integer.valueOf(type));
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadAppTypeForCloudRecog SQLiteException");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadAppTypeForCloudRecog IllegalStateException");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public static void loadReRecogApp(ContentResolver resolver, List<String> list, int curHabitVersion) {
        if (resolver != null && list != null) {
            Cursor cursor = null;
            String whereClause = "source=1 or source=2";
            try {
                cursor = resolver.query(Database.APPTYPE_URI, new String[]{"appPkgName", "recogVersion"}, whereClause, null, null);
                if (cursor == null) {
                    AwareLog.e(TAG, "loadReRecogApp cursor is null.");
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                }
                while (cursor.moveToNext()) {
                    String pkgName = cursor.getString(0);
                    int version = cursor.getInt(1);
                    if (!TextUtils.isEmpty(pkgName) && version < curHabitVersion) {
                        list.add(pkgName);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadReRecogApp SQLiteException");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadReRecogApp IllegalStateException");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public static void deleteAppTypeInfo(ContentResolver resolver, int version) {
        if (resolver != null) {
            String whereClause = "source!=0 and recogVersion != ?";
            try {
                resolver.delete(Database.APPTYPE_URI, whereClause, new String[]{String.valueOf(version)});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteAppTypeInfo ");
            }
        }
    }
}
