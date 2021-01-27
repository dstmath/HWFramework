package com.huawei.server.security.permissionmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.huawei.android.util.SlogEx;
import com.huawei.server.security.permissionmanager.util.CursorHelper;
import com.huawei.server.security.permissionmanager.util.HwPermUtils;
import com.huawei.server.security.permissionmanager.util.PermConst;
import java.util.ArrayList;
import java.util.List;

public class OneTimePermAdapter {
    private static final boolean IS_CHINA_AREA = HwPermUtils.IS_CHINA_AREA;
    private static final boolean IS_DEBUG = HwPermUtils.IS_DEBUG;
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "OneTimePermAdapter";
    private static final int MAX_PACKAGE_NAMES = 10000;
    private static volatile OneTimePermAdapter sInstance;
    private volatile boolean isInitialized = false;
    private Context mContext;
    private HwPermDbHelper mHwPermDbHelper;
    private volatile String mInfoType = PermConst.ONE_TIME_PERM_TYPE_SUPPORT;
    private volatile ArrayList<String> mRecordedPackageNames = new ArrayList<>(128);

    private OneTimePermAdapter(Context context) {
        SlogEx.v(LOG_TAG, "create OneTimePermAdapter");
        this.mContext = context.getApplicationContext();
        this.mHwPermDbHelper = HwPermDbHelper.getInstance(context);
    }

    public static OneTimePermAdapter getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (context != null) {
                    try {
                        if (sInstance == null) {
                            sInstance = new OneTimePermAdapter(context);
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    throw new IllegalArgumentException("Input value is null!");
                }
            }
        }
        return sInstance;
    }

    @NonNull
    public Bundle getOneTimePermissionInfo() {
        Bundle info = new Bundle();
        if (!IS_CHINA_AREA) {
            info.putString(PermConst.INFO_TYPE_KEY, PermConst.ONE_TIME_PERM_TYPE_SUPPORT);
            info.putStringArrayList(PermConst.PACKAEGE_NAME_KEY, new ArrayList<>(0));
            SlogEx.d(LOG_TAG, "not china area, return default.");
            return info;
        }
        long startTime = System.currentTimeMillis();
        synchronized (LOCK) {
            if (!this.isInitialized) {
                SlogEx.w(LOG_TAG, "query before initialize or update success, use old data.");
            }
            info.putString(PermConst.INFO_TYPE_KEY, this.mInfoType);
            info.putStringArrayList(PermConst.PACKAEGE_NAME_KEY, this.mRecordedPackageNames);
        }
        SlogEx.i(LOG_TAG, "getOneTimePermissionInfo: type :" + this.mInfoType + ",packages size:" + this.mRecordedPackageNames.size());
        if (IS_DEBUG) {
            long endTime = System.currentTimeMillis();
            SlogEx.d(LOG_TAG, "get one time permission consume time:" + (endTime - startTime));
        }
        return info;
    }

    public void updateInfoAll(Bundle params) {
        if (params == null) {
            SlogEx.w(LOG_TAG, "invalid params.");
            return;
        }
        long startTime = System.currentTimeMillis();
        try {
            String infoType = params.getString(PermConst.INFO_TYPE_KEY);
            List<String> updatePackageNames = params.getStringArrayList(PermConst.PACKAEGE_NAME_KEY);
            if (!TextUtils.isEmpty(infoType) && updatePackageNames != null) {
                if (updatePackageNames.size() <= 10000) {
                    List<String> installedPackageNames = HwPermUtils.getInstalledPackages(this.mContext);
                    List<String> activePackageNames = new ArrayList<>(128);
                    synchronized (LOCK) {
                        if (isInfoTypeValid(infoType)) {
                            this.mInfoType = infoType;
                            String columnPackageName = getPackageColumnName(this.mInfoType);
                            this.mRecordedPackageNames.clear();
                            this.mRecordedPackageNames.addAll(updatePackageNames);
                            this.mRecordedPackageNames.retainAll(installedPackageNames);
                            if (!this.isInitialized) {
                                this.isInitialized = true;
                                SlogEx.i(LOG_TAG, "initialize success during update process.");
                            }
                            activePackageNames.addAll(this.mRecordedPackageNames);
                            if (IS_DEBUG) {
                                long memoryEndTime = System.currentTimeMillis();
                                SlogEx.d(LOG_TAG, "update info all memory consume time: " + (memoryEndTime - startTime));
                            }
                            updateAllInDb(PermConst.ACTIVE_ONE_TIME_PERM_TABLE_NAME, infoType, columnPackageName, activePackageNames);
                            updateAllInDb(PermConst.ONE_TIME_PERM_TABLE_NAME, infoType, columnPackageName, updatePackageNames);
                            SlogEx.i(LOG_TAG, "update local db.");
                            return;
                        }
                        SlogEx.w(LOG_TAG, "update all info fail with invalid info type");
                        return;
                    }
                }
            }
            SlogEx.w(LOG_TAG, "invalid parameter value.");
        } catch (IndexOutOfBoundsException e) {
            SlogEx.e(LOG_TAG, "invalid parameter value get exception");
        } catch (Exception e2) {
            SlogEx.e(LOG_TAG, "invalid parameter value get unexpected exception.");
        }
    }

    public void initInfoForInstalledPkg() {
        long startTime = System.currentTimeMillis();
        List<String> installedPackageNames = HwPermUtils.getInstalledPackages(this.mContext);
        synchronized (LOCK) {
            if (this.isInitialized) {
                SlogEx.i(LOG_TAG, "get initialized already, give up this initialize.");
                return;
            }
            Cursor cursor = null;
            try {
                cursor = this.mHwPermDbHelper.query(PermConst.ONE_TIME_PERM_TABLE_NAME, null, null);
                if (cursor != null) {
                    if (cursor.getCount() >= 0) {
                        String infoType = getInfoTypeFromColumnName(cursor.getColumnNames());
                        if (!isInfoTypeValid(infoType)) {
                            SlogEx.w(LOG_TAG, "initialize memory fail for nothing updated before");
                            CursorHelper.closeCursor(cursor);
                            return;
                        }
                        this.mInfoType = infoType;
                        this.mRecordedPackageNames.clear();
                        String columnPackageName = getPackageColumnName(this.mInfoType);
                        int packageNameColumnIndex = cursor.getColumnIndex(columnPackageName);
                        while (cursor.moveToNext()) {
                            this.mRecordedPackageNames.add(cursor.getString(packageNameColumnIndex));
                        }
                        this.mRecordedPackageNames.retainAll(installedPackageNames);
                        this.isInitialized = true;
                        SlogEx.i(LOG_TAG, "initialize success");
                        updateAllInDb(PermConst.ACTIVE_ONE_TIME_PERM_TABLE_NAME, this.mInfoType, columnPackageName, this.mRecordedPackageNames);
                        if (IS_DEBUG) {
                            long endTime = System.currentTimeMillis();
                            SlogEx.d(LOG_TAG, "init memory consume time:" + (endTime - startTime));
                        }
                        CursorHelper.closeCursor(cursor);
                        return;
                    }
                }
                SlogEx.i(LOG_TAG, "initialize fail with invalid database");
            } catch (IllegalArgumentException e) {
                SlogEx.e(LOG_TAG, "initialize error.");
            } finally {
                CursorHelper.closeCursor(cursor);
            }
        }
    }

    public void initFromLastActiveData() {
        synchronized (LOCK) {
            if (this.isInitialized) {
                SlogEx.i(LOG_TAG, "already initialized for installed package, give up this initialize");
                return;
            }
            Cursor cursor = null;
            try {
                cursor = this.mHwPermDbHelper.query(PermConst.ACTIVE_ONE_TIME_PERM_TABLE_NAME, null, null);
                if (cursor != null) {
                    if (cursor.getCount() >= 0) {
                        String infoType = getInfoTypeFromColumnName(cursor.getColumnNames());
                        if (!isInfoTypeValid(infoType)) {
                            SlogEx.w(LOG_TAG, "initialize memory fail for no active data before");
                            CursorHelper.closeCursor(cursor);
                            return;
                        }
                        this.mInfoType = infoType;
                        int packageNameColumnIndex = cursor.getColumnIndex(getPackageColumnName(this.mInfoType));
                        while (cursor.moveToNext()) {
                            this.mRecordedPackageNames.add(cursor.getString(packageNameColumnIndex));
                        }
                        CursorHelper.closeCursor(cursor);
                        return;
                    }
                }
                SlogEx.i(LOG_TAG, "initialize fail with invalid database");
            } catch (Exception e) {
                SlogEx.e(LOG_TAG, "initialize from old data error.");
            } finally {
                CursorHelper.closeCursor(cursor);
            }
        }
    }

    public void updateInfoForPkgAdd(@NonNull String packageName) {
        Cursor cursor;
        long startTime = System.currentTimeMillis();
        try {
            synchronized (LOCK) {
                if (!this.isInitialized) {
                    SlogEx.w(LOG_TAG, "update fail for initialize fail.");
                    return;
                } else if (this.mRecordedPackageNames.contains(packageName)) {
                    SlogEx.i(LOG_TAG, "current package already recorded.");
                    CursorHelper.closeCursor(null);
                    return;
                } else {
                    String columnName = getPackageColumnName(this.mInfoType);
                    HwPermDbHelper hwPermDbHelper = this.mHwPermDbHelper;
                    cursor = hwPermDbHelper.query(PermConst.ONE_TIME_PERM_TABLE_NAME, columnName + "= ?", new String[]{packageName});
                    if (CursorHelper.checkCursorValid(cursor) && cursor.moveToFirst()) {
                        this.mRecordedPackageNames.add(packageName);
                        SlogEx.d(LOG_TAG, "add recorded package success");
                        ContentValues value = new ContentValues();
                        value.put(columnName, packageName);
                        this.mHwPermDbHelper.insert(value, PermConst.ACTIVE_ONE_TIME_PERM_TABLE_NAME);
                    }
                }
            }
            CursorHelper.closeCursor(cursor);
            if (IS_DEBUG) {
                long endTime = System.currentTimeMillis();
                SlogEx.d(LOG_TAG, "update add package consume time:" + (endTime - startTime));
            }
        } catch (Exception e) {
            SlogEx.e(LOG_TAG, "update info for package add get unexpected exception.");
        } finally {
            CursorHelper.closeCursor(null);
        }
    }

    private boolean isInfoTypeValid(String infoType) {
        return PermConst.ONE_TIME_PERM_TYPE_SUPPORT.equals(infoType) || PermConst.ONE_TIME_PERM_TYPE_BLOCK.equals(infoType);
    }

    @Nullable
    private String getInfoTypeFromColumnName(String[] columnNames) {
        if (columnNames == null || columnNames.length < 0) {
            return null;
        }
        for (String columnName : columnNames) {
            if (PermConst.COLUMN_SUPPORTED_PACKAGE_NAME.equals(columnName)) {
                return PermConst.ONE_TIME_PERM_TYPE_SUPPORT;
            }
            if (PermConst.COLUMN_BLOCKED_PACKAGE_NAME.equals(columnName)) {
                return PermConst.ONE_TIME_PERM_TYPE_BLOCK;
            }
        }
        return null;
    }

    @NonNull
    private String getPackageColumnName(String infoType) {
        return PermConst.ONE_TIME_PERM_TYPE_SUPPORT.equals(infoType) ? PermConst.COLUMN_SUPPORTED_PACKAGE_NAME : PermConst.COLUMN_BLOCKED_PACKAGE_NAME;
    }

    private void updateAllInDb(String tableName, String infoType, String columnPackageName, List<String> updatePackageNames) {
        List<ContentValues> contentValuesList = new ArrayList<>(updatePackageNames.size());
        for (String packageName : updatePackageNames) {
            ContentValues values = new ContentValues();
            values.put(columnPackageName, packageName);
            contentValuesList.add(values);
        }
        this.mHwPermDbHelper.removeAndUpdateAll(tableName, infoType, contentValuesList);
    }
}
