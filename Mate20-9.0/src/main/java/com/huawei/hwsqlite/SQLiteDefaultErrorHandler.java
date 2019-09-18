package com.huawei.hwsqlite;

import android.util.Log;
import android.util.Pair;
import java.io.File;
import java.util.List;

public final class SQLiteDefaultErrorHandler implements SQLiteErrorHandler {
    private static final String TAG = "SQLiteDefErrHandler";

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003f, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0046, code lost:
        if (r0 != null) goto L_0x0048;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0048, code lost:
        r2 = r0.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0050, code lost:
        if (r2.hasNext() != false) goto L_0x0052;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0052, code lost:
        deleteDatabaseFile((java.lang.String) r2.next().second);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0060, code lost:
        deleteDatabaseFile(r6.getPath());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0067, code lost:
        throw r1;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x003f A[ExcHandler: all (r1v6 'th' java.lang.Throwable A[CUSTOM_DECLARE]), PHI: r0 
      PHI: (r0v5 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>) = (r0v3 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>), (r0v4 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>), (r0v4 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>) binds: [B:11:0x0039, B:16:0x0042, B:17:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:11:0x0039] */
    public void onCorruption(SQLiteDatabase dbObj) {
        Log.e(TAG, "Corruption reported by sqlite on database: " + dbObj.getPath());
        if (!dbObj.isOpen()) {
            if (dbObj.reopenOnOpenCorruption()) {
                deleteDatabaseFile(dbObj.getPath());
            }
        } else if (!dbObj.reopenOnOpenCorruption()) {
            dbObj.close();
        } else {
            List<Pair<String, String>> attachedDbs = null;
            try {
                attachedDbs = dbObj.getAttachedDbs();
                dbObj.close();
            } catch (SQLiteException e) {
            } catch (Throwable th) {
            }
            if (attachedDbs != null) {
                for (Pair<String, String> p : attachedDbs) {
                    deleteDatabaseFile((String) p.second);
                }
            } else {
                deleteDatabaseFile(dbObj.getPath());
            }
        }
    }

    private void deleteDatabaseFile(String fileName) {
        if (!fileName.equalsIgnoreCase(SQLiteDatabaseConfiguration.MEMORY_DB_PATH) && fileName.trim().length() != 0) {
            Log.e(TAG, "deleting the database file: " + fileName);
            try {
                SQLiteDatabase.deleteDatabase(new File(fileName));
            } catch (Exception e) {
                Log.w(TAG, "delete failed: " + e.getMessage());
            }
        }
    }
}
