package com.huawei.server.security.permissionmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.huawei.android.util.SlogEx;
import com.huawei.server.security.permissionmanager.util.CursorHelper;
import com.huawei.server.security.permissionmanager.util.PermConst;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class HwSmsBlockDbAdapter {
    private static final int DEFAULT_LENGTH = 16;
    private static final int FIRST_ITEM_IN_ARRAY = 0;
    private static final int ILLEGAL_ROW = -1;
    private static final int IS_PRE_DEFINED_SMS = 1;
    private static final String KEY_CALLER = "caller";
    private static final String KEY_OPERATION = "operation";
    private static final String KEY_PACKAGE_NAME = "pkgName";
    private static final String KEY_PERM_TYPE = "permType";
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "HwSmsBlockDbAdapter";
    private static final int NOT_PRE_DEFINED_SMS = 0;
    private static final int NO_USER_DISTINGUISH = -1;
    private static final long SQLITE_INVALID_ROW_ID = -1;
    private static volatile HwSmsBlockDbAdapter sUniqueInstance = null;
    private HwPermDbHelper mHwPermDbHelper;

    private HwSmsBlockDbAdapter(Context context) {
        SlogEx.v(LOG_TAG, "create HwSmsBlockDbAdapter");
        this.mHwPermDbHelper = HwPermDbHelper.getInstance(context);
        new HandlerThread(LOG_TAG).start();
    }

    public static HwSmsBlockDbAdapter getInstance(Context context) {
        HwSmsBlockDbAdapter hwSmsBlockDbAdapter;
        synchronized (LOCK) {
            if (sUniqueInstance == null) {
                sUniqueInstance = new HwSmsBlockDbAdapter(context);
            }
            hwSmsBlockDbAdapter = sUniqueInstance;
        }
        return hwSmsBlockDbAdapter;
    }

    public static String getSmsBlockTableName(int userId) {
        return PermConst.SMS_BLOCK_TABLE_NAME;
    }

    public void initSmsBlockData(int userId, @NonNull ArrayList<ContentValues> contentValuesList) {
        String tableName = getSmsBlockTableName(userId);
        if (contentValuesList != null) {
            try {
                if (!contentValuesList.isEmpty()) {
                    deletePreDefinedSmsApps(userId);
                    int listSize = contentValuesList.size();
                    for (int i = 0; i < listSize; i++) {
                        ContentValues values = contentValuesList.get(i);
                        values.put(PermConst.PRE_DEFINED_SMS_CONFIG, (Integer) 1);
                        this.mHwPermDbHelper.insert(values, tableName);
                    }
                    SlogEx.v(LOG_TAG, "initSmsBlockData complete");
                    return;
                }
            } catch (SQLiteException e) {
                SlogEx.e(LOG_TAG, "initSmsBlockData SQLiteException for " + tableName);
                return;
            }
        }
        SlogEx.e(LOG_TAG, "initSmsBlockData contentValuesList is empty!");
    }

    public void hotaUpdateSmsBlockData(int userId, @NonNull ArrayList<ContentValues> contentValuesList) {
        String tableName = getSmsBlockTableName(userId);
        if (contentValuesList != null) {
            try {
                if (!contentValuesList.isEmpty()) {
                    List<String> preDefinedSmsApps = getPreDefinedSmsApps();
                    int listSize = contentValuesList.size();
                    for (int i = 0; i < listSize; i++) {
                        ContentValues values = contentValuesList.get(i);
                        Set<String> nameSet = values.keySet();
                        if (nameSet != null) {
                            if (!nameSet.isEmpty()) {
                                String pkgName = ((String[]) nameSet.toArray(new String[0]))[0];
                                if (preDefinedSmsApps.contains(pkgName)) {
                                    SlogEx.v(LOG_TAG, pkgName + " is preDefined");
                                } else {
                                    values.put(PermConst.PRE_DEFINED_SMS_CONFIG, (Integer) 0);
                                    this.mHwPermDbHelper.replace(values, tableName);
                                }
                            }
                        }
                        SlogEx.e(LOG_TAG, "hotaUpdateSmsBlockData name set is empty");
                    }
                    SlogEx.v(LOG_TAG, "hotaUpdateSmsBlockData complete");
                    return;
                }
            } catch (SQLiteException e) {
                SlogEx.e(LOG_TAG, "hotaUpdateSmsBlockData SQLiteException for " + tableName);
                return;
            }
        }
        SlogEx.e(LOG_TAG, "hotaUpdateSmsBlockData contentValuesList is empty!");
    }

    public void replaceSmsBlockAuthResult(@NonNull String pkgName, int result, int userId) {
        Cursor cursor = null;
        SlogEx.v(LOG_TAG, "replaceSmsBlockAuthResult:" + pkgName + ", " + result + " for user " + userId);
        if (!validParameterForSmsAuth(pkgName, result)) {
            SlogEx.e(LOG_TAG, "replaceSmsBlockAuthResult parameter invalid");
            return;
        }
        try {
            cursor = this.mHwPermDbHelper.query(getSmsBlockTableName(userId), "packageName= ?", new String[]{pkgName});
            if (!CursorHelper.checkCursorValid(cursor) || !cursor.moveToFirst() || cursor.getInt(cursor.getColumnIndex(PermConst.PRE_DEFINED_SMS_CONFIG)) != 1) {
                ContentValues values = new ContentValues();
                values.put(PermConst.PACKAGE_NAME, pkgName);
                values.put(PermConst.SUB_PERMISSION, Integer.valueOf(result));
                this.mHwPermDbHelper.replace(values, getSmsBlockTableName(userId));
                CursorHelper.closeCursor(cursor);
                return;
            }
            SlogEx.v(LOG_TAG, pkgName + " is predefined SMS, can't replace");
            CursorHelper.closeCursor(cursor);
        } catch (SQLiteException e) {
            SlogEx.e(LOG_TAG, "replaceSmsBlockAuthResult SQLiteException");
        } catch (Throwable th) {
            CursorHelper.closeCursor(null);
            throw th;
        }
    }

    public int checkSmsBlockAuthResult(@NonNull String pkgName, int userId) {
        Cursor cursor = getCursorOfSmsBlockDb(pkgName, userId);
        if (cursor != null) {
            try {
                if (cursor.getCount() != 0) {
                    if (cursor.moveToFirst()) {
                        int smsPermission = cursor.getInt(cursor.getColumnIndex(PermConst.SUB_PERMISSION));
                        SlogEx.v(LOG_TAG, "checkSmsBlockAuthResult, pkgName: " + pkgName + ", smsPermission:" + smsPermission);
                        return smsPermission;
                    }
                    SlogEx.e(LOG_TAG, "pkg is null, no sms block value, pkgName:" + pkgName);
                    CursorHelper.closeCursor(cursor);
                    return 2;
                }
            } catch (SQLiteException e) {
                SlogEx.e(LOG_TAG, "checkHwPerm SQLiteException");
                return 2;
            } finally {
                CursorHelper.closeCursor(cursor);
            }
        }
        SlogEx.e(LOG_TAG, "cursor is null, no sms block value, pkgName:" + pkgName);
        CursorHelper.closeCursor(cursor);
        return 2;
    }

    public void addSmsBlockAuthResult(@NonNull String pkgName, int result, int userId) {
        SlogEx.v(LOG_TAG, "addSmsBlockAuthResult:" + pkgName + ", " + result + " for user " + userId);
        if (validParameterForSmsAuth(pkgName, result)) {
            try {
                ContentValues values = new ContentValues();
                values.put(PermConst.PACKAGE_NAME, pkgName);
                values.put(PermConst.SUB_PERMISSION, Integer.valueOf(result));
                this.mHwPermDbHelper.insert(values, getSmsBlockTableName(userId));
                SlogEx.v(LOG_TAG, "addSmsBlockAuthResult complete");
            } catch (SQLiteException e) {
                SlogEx.e(LOG_TAG, "addSmsBlockAuthResult SQLiteException");
            }
        }
    }

    public void deleteSmsBlockAuthResult(@NonNull String pkgName, int userId) {
        SlogEx.v(LOG_TAG, "deleteSmsBlockAuthResult:" + pkgName);
        try {
            Cursor cursor = this.mHwPermDbHelper.query(getSmsBlockTableName(userId), "packageName= ?", new String[]{pkgName});
            if (CursorHelper.checkCursorValid(cursor) && cursor.moveToFirst() && cursor.getInt(cursor.getColumnIndex(PermConst.PRE_DEFINED_SMS_CONFIG)) == 1) {
                SlogEx.v(LOG_TAG, pkgName + " is predefined SMS, can't delete");
            }
            this.mHwPermDbHelper.delete(getSmsBlockTableName(userId), "packageName= ?", new String[]{pkgName});
            CursorHelper.closeCursor(cursor);
        } catch (SQLiteException e) {
            SlogEx.e(LOG_TAG, "addSmsBlockAuthResult SQLiteException");
        }
    }

    public List<String> getPreDefinedSmsApps() {
        List<String> preDefinedSmsApps = new ArrayList<>(16);
        Cursor cursor = getCursorOfDefaultSmsConfig();
        if (cursor != null) {
            try {
                if (cursor.getCount() != 0) {
                    int packageNameIndex = cursor.getColumnIndex(PermConst.PACKAGE_NAME);
                    while (cursor.moveToNext()) {
                        String pkgName = cursor.getString(packageNameIndex);
                        SlogEx.v(LOG_TAG, "preDefinedSmsApp, pkgName: " + pkgName);
                        preDefinedSmsApps.add(pkgName);
                    }
                    return preDefinedSmsApps;
                }
            } catch (SQLiteException e) {
                SlogEx.e(LOG_TAG, "getPreDefinedSmsApps SQLiteException");
                return preDefinedSmsApps;
            } finally {
                CursorHelper.closeCursor(cursor);
            }
        }
        SlogEx.v(LOG_TAG, "cursor is null, can't get pre defined sms apps");
        CursorHelper.closeCursor(cursor);
        return preDefinedSmsApps;
    }

    public List<String> getUserDefinedSmsApps() {
        List<String> userDefinedSmsApps = new ArrayList<>(16);
        Cursor cursor = getCursorOfUserDefinedSmsConfig();
        if (cursor != null) {
            try {
                if (cursor.getCount() != 0) {
                    int packageNameIndex = cursor.getColumnIndex(PermConst.PACKAGE_NAME);
                    while (cursor.moveToNext()) {
                        String pkgName = cursor.getString(packageNameIndex);
                        SlogEx.v(LOG_TAG, "preDefinedSmsApp, pkgName: " + pkgName);
                        userDefinedSmsApps.add(pkgName);
                    }
                    return userDefinedSmsApps;
                }
            } catch (SQLiteException e) {
                SlogEx.e(LOG_TAG, "getPreDefinedSmsApps SQLiteException");
                return userDefinedSmsApps;
            } finally {
                CursorHelper.closeCursor(cursor);
            }
        }
        SlogEx.v(LOG_TAG, "cursor is null, can't get user defined sms apps");
        CursorHelper.closeCursor(cursor);
        return userDefinedSmsApps;
    }

    public HashMap<String, Integer> getAllSmsBlockSetting(int userId) {
        HashMap<String, Integer> smsPermissionSetting = new HashMap<>(16);
        Cursor cursor = this.mHwPermDbHelper.query(getSmsBlockTableName(userId), null, null);
        try {
            if (CursorHelper.checkCursorValid(cursor)) {
                int packageNameIndex = cursor.getColumnIndex(PermConst.PACKAGE_NAME);
                int smsPermissionIndex = cursor.getColumnIndex(PermConst.SUB_PERMISSION);
                while (cursor.moveToNext()) {
                    smsPermissionSetting.put(cursor.getString(packageNameIndex), Integer.valueOf(cursor.getInt(smsPermissionIndex)));
                }
                CursorHelper.closeCursor(cursor);
                return smsPermissionSetting;
            }
        } catch (IllegalArgumentException e) {
            SlogEx.e(LOG_TAG, "getAllAppsPermInfo error");
        } catch (Throwable th) {
            CursorHelper.closeCursor(cursor);
            throw th;
        }
        CursorHelper.closeCursor(cursor);
        return smsPermissionSetting;
    }

    public Cursor getCursorOfDefaultSmsConfig() {
        return this.mHwPermDbHelper.query(getSmsBlockTableName(-1), "preDefinedSmsConfig = ?", new String[]{Integer.toString(1)});
    }

    public Cursor getCursorOfUserDefinedSmsConfig() {
        return this.mHwPermDbHelper.query(getSmsBlockTableName(-1), "preDefinedSmsConfig = ?", new String[]{Integer.toString(0)});
    }

    private Cursor getCursorOfSmsBlockDb(String pkgName, int userId) {
        return this.mHwPermDbHelper.query(getSmsBlockTableName(userId), "packageName = ?", new String[]{pkgName});
    }

    private boolean validParameterForSmsAuth(String pkgName, int result) {
        boolean isAllow = result == 1;
        boolean isDisallow = result == 3;
        if (!TextUtils.isEmpty(pkgName)) {
            return isAllow || isDisallow;
        }
        return false;
    }

    private void deletePreDefinedSmsApps(int userId) {
        Cursor cursor = getCursorOfDefaultSmsConfig();
        if (cursor != null) {
            try {
                if (cursor.getCount() != 0) {
                    int packageNameIndex = cursor.getColumnIndex(PermConst.PACKAGE_NAME);
                    while (cursor.moveToNext()) {
                        String pkgName = cursor.getString(packageNameIndex);
                        SlogEx.v(LOG_TAG, "deletePreDefinedSmsApps, pkgName: " + pkgName);
                        this.mHwPermDbHelper.delete(getSmsBlockTableName(userId), "packageName= ?", new String[]{pkgName});
                    }
                    return;
                }
            } catch (SQLiteException e) {
                SlogEx.e(LOG_TAG, "deletePreDefinedSmsApps SQLiteException");
                return;
            } finally {
                CursorHelper.closeCursor(cursor);
            }
        }
        SlogEx.v(LOG_TAG, "deletePreDefinedSmsApps cursor is null");
        CursorHelper.closeCursor(cursor);
    }
}
