package com.huawei.indexsearch;

import android.database.Cursor;
import android.os.SystemProperties;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class IndexSearchParser implements IIndexSearchParser {
    private static final String HWINDEXSEARCHSERVICE_APK_NAME = "com.huawei.nb.service";
    private static final boolean IS_SUPPORT_FULL_TEXT_SEARCH = SystemProperties.get("ro.config.hw_globalSearch", "true").equals("true");
    private static final String TAG = "IndexSearchParser";
    private static volatile IndexSearchParser mInstance = null;
    private String mPkgName;
    private String[] mTables;

    public static synchronized void createIndexSearchParser(String pkgName, String[] tables) {
        synchronized (IndexSearchParser.class) {
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
            Log.i(TAG, "table=" + table);
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
        Log.i(TAG, "notifyIndexSearchService end");
    }

    public boolean isValidTable(String table) {
        for (String equals : this.mTables) {
            if (equals.equals(table)) {
                updateTable(table);
                return true;
            }
        }
        return false;
    }

    private void updateTable(String table) {
        if ("pdu".equals(table)) {
            this.mPkgName = "com.android.providers.telephony";
        } else if ("sms".equals(table) || "fav_sms".equals(table)) {
            this.mPkgName = "com.android.mms";
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0027, code lost:
        if (0 != 0) goto L_0x002a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002b, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0012, code lost:
        if (android.app.ActivityThread.getPackageManager().getPackageInfo(HWINDEXSEARCHSERVICE_APK_NAME, 0, 0) == null) goto L_0x0014;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0014, code lost:
        android.util.Log.e(TAG, "IndexSearchService not exist");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001b, code lost:
        return false;
     */
    public static boolean isHwIndexSearchServiceExist() {
        if (!IS_SUPPORT_FULL_TEXT_SEARCH) {
            return false;
        }
        try {
        } catch (Exception e) {
            Log.e(TAG, "IndexSearchService packageInfo is null");
        } catch (Throwable th) {
            if (0 != 0) {
                throw th;
            }
        }
    }

    public static void destroy() {
        if (mInstance != null) {
            mInstance = null;
        }
    }
}
