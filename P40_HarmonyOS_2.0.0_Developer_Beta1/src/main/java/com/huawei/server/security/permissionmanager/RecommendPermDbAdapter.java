package com.huawei.server.security.permissionmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.huawei.android.util.SlogEx;
import com.huawei.securitycenter.permission.ui.model.DbPermissionItem;
import com.huawei.server.security.permissionmanager.util.PermConst;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;

public class RecommendPermDbAdapter {
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "RecommendPermDbAdapter";
    private static final long RECOMMEND_ILLEGAL_INFO = 0;
    private static volatile RecommendPermDbAdapter sInstance;
    private volatile Context mContext;
    private volatile HwPermDbHelper mHwPermDbHelper;

    private RecommendPermDbAdapter(Context context) {
        SlogEx.v(LOG_TAG, "create RecommendPermDbAdapter");
        this.mContext = context.getApplicationContext();
        this.mHwPermDbHelper = HwPermDbHelper.getInstance(context);
    }

    public static RecommendPermDbAdapter getInstance(Context context) {
        if (context != null) {
            if (sInstance == null) {
                synchronized (LOCK) {
                    if (sInstance == null) {
                        sInstance = new RecommendPermDbAdapter(context);
                    }
                }
            }
            return sInstance;
        }
        throw new IllegalArgumentException("Input value is null!");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00b1, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00b2, code lost:
        if (r1 != null) goto L_0x00b4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00b8, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00b9, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00bc, code lost:
        throw r4;
     */
    @NonNull
    public long[] getRecommendPermValue(String pkgName) {
        if (this.mContext == null) {
            return new long[]{0, 0};
        }
        try {
            Cursor cursor = this.mHwPermDbHelper.query(PermConst.RECOMMEND_PERMISSION_TABLE_NAME, "packageName = ?", new String[]{pkgName});
            if (cursor != null) {
                if (cursor.getCount() != 0) {
                    if (cursor.moveToFirst()) {
                        int permissionCodeIndex = cursor.getColumnIndex(PermConst.PERMISSION_CODE);
                        int permissionCfgIndex = cursor.getColumnIndex(PermConst.PERMISSION_CFG);
                        long permissionCode = cursor.getLong(permissionCodeIndex);
                        long permissionCfg = cursor.getLong(permissionCfgIndex);
                        SlogEx.i(LOG_TAG, "getRecommendPermValue, pkgName: " + pkgName + ", code:" + permissionCode + ", Cfg :" + permissionCfg);
                        long[] jArr = {permissionCode, permissionCfg};
                        cursor.close();
                        return jArr;
                    }
                    SlogEx.i(LOG_TAG, "pkg is null, recommend close, pkgName:" + pkgName);
                    long[] jArr2 = {0, 0};
                    cursor.close();
                    return jArr2;
                }
            }
            SlogEx.i(LOG_TAG, "cursor is null, recommend close, pkgName:" + pkgName);
            long[] jArr3 = {0, 0};
            if (cursor != null) {
                cursor.close();
            }
            return jArr3;
        } catch (SQLiteException e) {
            SlogEx.i(LOG_TAG, "Exception occurs while execute getRecommendPermValue operation!");
            return new long[]{0, 0};
        }
    }

    public void setRecommendPermission(Bundle param) {
        if (param == null) {
            SlogEx.e(LOG_TAG, "get null bundle");
            return;
        }
        ArrayList<DbPermissionItem> recommendPermList = null;
        try {
            recommendPermList = param.getParcelableArrayList(PermConst.APP_LIST_KEY);
        } catch (IndexOutOfBoundsException e) {
            SlogEx.e(LOG_TAG, "setRecommendPermission ArrayIndexOutOfBoundsException: " + e.getMessage());
        } catch (Exception e2) {
            SlogEx.e(LOG_TAG, "setRecommendPermission get unexpected exception");
        }
        if (recommendPermList != null && !recommendPermList.isEmpty()) {
            Thread parseThread = new Thread(new ParseConfigRunnable(recommendPermList), "SetRecommendPermissionThread");
            parseThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                /* class com.huawei.server.security.permissionmanager.RecommendPermDbAdapter.AnonymousClass1 */

                @Override // java.lang.Thread.UncaughtExceptionHandler
                public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
                    SlogEx.e(RecommendPermDbAdapter.LOG_TAG, "Uncaught exception found");
                }
            });
            parseThread.start();
        }
    }

    /* access modifiers changed from: private */
    public class ParseConfigRunnable implements Runnable {
        List<DbPermissionItem> mRecommendPermItems;

        ParseConfigRunnable(List<DbPermissionItem> appList) {
            this.mRecommendPermItems = appList;
        }

        @Override // java.lang.Runnable
        public void run() {
            List<ContentValues> valuesList = new ArrayList<>(this.mRecommendPermItems.size());
            for (DbPermissionItem item : this.mRecommendPermItems) {
                ContentValues values = new ContentValues();
                values.put(PermConst.PACKAGE_NAME, item.getPackageName());
                values.put(PermConst.PERMISSION_CODE, Long.valueOf(item.getPermissionCode()));
                values.put(PermConst.PERMISSION_CFG, Long.valueOf(item.getPermissionCfg()));
                valuesList.add(values);
            }
            RecommendPermDbAdapter.this.mHwPermDbHelper.removeAndUpdateAll(PermConst.RECOMMEND_PERMISSION_TABLE_NAME, PermConst.RECOMMEND_PERM_TYPE, valuesList);
        }
    }
}
