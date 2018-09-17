package com.huawei.indexsearch;

import android.app.ActivityThread;
import android.database.Cursor;
import android.os.SystemProperties;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class IndexSearchParser {
    private static final String FULL_TEXT_SEARCH_CONFIG = "ro.config.hw_globalSearch";
    private static final String HWINDEXSEARCHSERVICE_APK_NAME = "com.huawei.indexsearch";
    private static final boolean IS_SUPPORT_FULL_TEXT_SEARCH = SystemProperties.get(FULL_TEXT_SEARCH_CONFIG, "true").equals("true");
    private static final String TAG = "IndexSearchParser";
    private static IndexSearchObserverManager mIndexSearchObserverManager = null;
    private static IndexSearchParser mInstance = null;
    private static String mPkgName;
    private static String[] mTables;
    private final String[] FILE_TYPE = new String[]{"txt", "html", "htm", "doc", "docx", "xls", "xlsx", "ppt", "pptx"};
    private final String[] VALID_MIME_TYPE = new String[]{"text/plain", "text/html", "text/htm", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/mspowerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation"};

    public static synchronized void createIndexSearchParser(String pkgName, String[] tables) {
        synchronized (IndexSearchParser.class) {
            if (isHwIndexSearchServiceExist() && mInstance == null) {
                mInstance = new IndexSearchParser();
                mPkgName = pkgName;
                mTables = tables;
                mIndexSearchObserverManager = IndexSearchObserverManager.getInstance();
            }
        }
    }

    public static synchronized IndexSearchParser getInstance() {
        IndexSearchParser indexSearchParser;
        synchronized (IndexSearchParser.class) {
            indexSearchParser = mInstance;
        }
        return indexSearchParser;
    }

    private IndexSearchParser() {
    }

    public void notifyIndexSearchService(Cursor c, int operator) {
        if (c == null) {
            Log.i(TAG, "notifyIndexSearchService(Cursor c, int operator) : cursor is null, return.");
            return;
        }
        List idList = new ArrayList();
        while (c.moveToNext()) {
            idList.add(Long.toString(c.getLong(0)));
        }
        if (mIndexSearchObserverManager != null && idList.size() > 0) {
            IndexSearchObserverManager indexSearchObserverManager = mIndexSearchObserverManager;
            IndexSearchObserverManager.getInstance().buildIndex(mPkgName, idList, operator);
        }
    }

    public void notifyIndexSearchService(long id, int operator) {
        if (mIndexSearchObserverManager != null && mPkgName != null) {
            IndexSearchObserverManager indexSearchObserverManager = mIndexSearchObserverManager;
            IndexSearchObserverManager.getInstance().buildIndex(mPkgName, Long.toString(id), operator);
        }
    }

    public void notifyIndexSearchService(List<Long> list, int operator) {
        List idList = new ArrayList();
        for (Long id : list) {
            idList.add(Long.toString(id.longValue()));
        }
        if (mIndexSearchObserverManager != null && mPkgName != null) {
            IndexSearchObserverManager indexSearchObserverManager = mIndexSearchObserverManager;
            IndexSearchObserverManager.getInstance().buildIndex(mPkgName, idList, operator);
        }
    }

    public static boolean isValidTable(String table) {
        for (String equals : mTables) {
            if (equals.equals(table)) {
                updateTable(table);
                return true;
            }
        }
        return false;
    }

    private static void updateTable(String table) {
        if ("pdu".equals(table)) {
            mPkgName = "com.android.providers.telephony";
        } else if ("sms".equals(table)) {
            mPkgName = "com.android.mms";
        }
    }

    public static boolean isHwIndexSearchServiceExist() {
        if (!IS_SUPPORT_FULL_TEXT_SEARCH) {
            return false;
        }
        try {
            if (ActivityThread.getPackageManager().getPackageInfo("com.huawei.indexsearch", 0, 0) != null) {
                return true;
            }
            Log.e(TAG, "IndexSearchService not exist");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "IndexSearchService packageInfo is null");
            Log.e(TAG, "IndexSearchService not exist");
            return false;
        } catch (Throwable th) {
            if (null == null) {
                Log.e(TAG, "IndexSearchService not exist");
                return false;
            }
        }
    }
}
