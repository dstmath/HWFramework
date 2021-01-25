package com.huawei.indexsearch;

import android.app.ActivityThread;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class IndexSearchParser {
    private static final String HWINDEXSEARCHSERVICE_APK_NAME = "com.huawei.searchservice";
    private static final boolean IS_SUPPORT_FULL_TEXT_SEARCH = SystemProperties.get("ro.config.hw_globalSearch", "true").equals("true");
    private static final Object LOCK = new Object();
    private static final String TAG = "IndexSearchParser";
    private static IndexSearchParser mInstance = null;
    private String mPkgName;
    private String[] mTables;

    public static void createIndexSearchParser(String pkgName, String[] tables) {
        if (TextUtils.isEmpty(pkgName) || tables == null) {
            Log.e(TAG, "Null parameter will not support Global Search");
            return;
        }
        synchronized (LOCK) {
            Log.i(TAG, "createIndexSearchParser pkgName " + pkgName);
            if (isHwIndexSearchServiceExist() && mInstance == null) {
                mInstance = new IndexSearchParser(pkgName, tables);
            }
        }
    }

    public static IndexSearchParser getInstance() {
        return mInstance;
    }

    private IndexSearchParser(String pkgName, String[] tables) {
        this.mPkgName = pkgName;
        this.mTables = tables;
        for (String table : tables) {
            Log.i(TAG, "table = " + table);
        }
    }

    public void notifyIndexSearchService(Cursor c, int operator) {
        if (c == null) {
            Log.i(TAG, "notifyIndexSearchService(Cursor c, int operator) : cursor is null, return.");
            return;
        }
        ArrayList<String> idList = new ArrayList<>();
        while (c.moveToNext()) {
            idList.add(Long.toString(c.getLong(0)));
        }
        if (idList.size() > 0) {
            IndexSearchObserverManager.getInstance().buildIndex(this.mPkgName, idList, operator);
        }
    }

    public void notifyIndexSearchService(long id, int operator) {
        List<Long> idList = new ArrayList<>();
        idList.add(Long.valueOf(id));
        notifyIndexSearchService(idList, operator);
    }

    public void notifyIndexSearchService(List<Long> list, int operator) {
        Log.i(TAG, "notifyIndexSearchService begin operator: " + operator);
        List<String> idList = new ArrayList<>();
        for (Long id : list) {
            idList.add(Long.toString(id.longValue()));
        }
        if (this.mPkgName != null) {
            IndexSearchObserverManager.getInstance().buildIndex(this.mPkgName, idList, operator);
        }
        Log.i(TAG, "notifyIndexSearchService end.");
    }

    public boolean isValidTable(String table) {
        int i = 0;
        while (true) {
            String[] strArr = this.mTables;
            if (i >= strArr.length) {
                return false;
            }
            if (strArr[i].equals(table)) {
                updateTable(table);
                return true;
            }
            i++;
        }
    }

    private void updateTable(String table) {
        if ("pdu".equals(table)) {
            this.mPkgName = "com.android.providers.telephony";
        } else if ("sms".equals(table) || "fav_sms".equals(table)) {
            this.mPkgName = "com.android.mms";
        }
    }

    public static boolean isHwIndexSearchServiceExist() {
        PackageInfo packageInfo;
        if (!IS_SUPPORT_FULL_TEXT_SEARCH) {
            return false;
        }
        try {
            packageInfo = ActivityThread.getPackageManager().getPackageInfo(HWINDEXSEARCHSERVICE_APK_NAME, 0, 0);
        } catch (Exception e) {
            packageInfo = null;
            Log.e(TAG, "HwNaturalBase packageInfo is null.");
        }
        if (packageInfo != null) {
            return true;
        }
        Log.e(TAG, "HwNaturalBase not exist.");
        return false;
    }

    public static void destroy() {
        if (mInstance != null) {
            mInstance = null;
        }
    }
}
