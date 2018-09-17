package com.huawei.indexsearch;

import android.app.ActivityThread;
import android.database.Cursor;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class IndexSearchParser {
    private static final String FULL_TEXT_SEARCH_CONFIG = "ro.config.hw_globalSearch";
    private static final String HWINDEXSEARCHSERVICE_APK_NAME = "com.huawei.indexsearch";
    private static final boolean IS_SUPPORT_FULL_TEXT_SEARCH = false;
    private static final String TAG = "IndexSearchParser";
    private static IndexSearchObserverManager mIndexSearchObserverManager;
    private static IndexSearchParser mInstance;
    private static String mPkgName;
    private static String[] mTables;
    private final String[] FILE_TYPE;
    private final String[] VALID_MIME_TYPE;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.indexsearch.IndexSearchParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.indexsearch.IndexSearchParser.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.indexsearch.IndexSearchParser.<clinit>():void");
    }

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
        this.VALID_MIME_TYPE = new String[]{"text/plain", "text/html", "text/htm", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/mspowerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation"};
        this.FILE_TYPE = new String[]{"txt", "html", "htm", "doc", "docx", "xls", "xlsx", "ppt", "pptx"};
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
        return IS_SUPPORT_FULL_TEXT_SEARCH;
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
            return IS_SUPPORT_FULL_TEXT_SEARCH;
        }
        try {
            if (ActivityThread.getPackageManager().getPackageInfo(HWINDEXSEARCHSERVICE_APK_NAME, 0, 0) != null) {
                return true;
            }
            Log.e(TAG, "IndexSearchService not exist");
            return IS_SUPPORT_FULL_TEXT_SEARCH;
        } catch (Exception e) {
            Log.e(TAG, "IndexSearchService packageInfo is null");
            Log.e(TAG, "IndexSearchService not exist");
            return IS_SUPPORT_FULL_TEXT_SEARCH;
        } catch (Throwable th) {
            if (null == null) {
                Log.e(TAG, "IndexSearchService not exist");
                return IS_SUPPORT_FULL_TEXT_SEARCH;
            }
        }
    }
}
