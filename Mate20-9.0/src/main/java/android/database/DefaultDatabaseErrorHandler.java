package android.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseConfiguration;
import android.database.sqlite.SQLiteException;
import android.os.FileUtils;
import android.util.Log;
import android.util.Pair;
import java.io.File;
import java.util.List;

public final class DefaultDatabaseErrorHandler implements DatabaseErrorHandler {
    private static final String TAG = "DefaultDatabaseErrorHandler";

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0039, code lost:
        if (r0 != null) goto L_0x003b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003b, code lost:
        r2 = r0.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0043, code lost:
        if (r2.hasNext() != false) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0045, code lost:
        deleteDatabaseFile((java.lang.String) r2.next().second);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0053, code lost:
        deleteDatabaseFile(r6.getPath());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005a, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x002f, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x002f A[ExcHandler: all (r1v6 'th' java.lang.Throwable A[CUSTOM_DECLARE]), PHI: r0 
      PHI: (r0v4 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>) = (r0v2 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>), (r0v3 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>), (r0v3 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>) binds: [B:5:0x0029, B:10:0x0032, B:11:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:5:0x0029] */
    public void onCorruption(SQLiteDatabase dbObj) {
        Log.e(TAG, "Corruption reported by sqlite on database: " + dbObj.getPath());
        if (!dbObj.isOpen()) {
            deleteDatabaseFile(dbObj.getPath());
            return;
        }
        List<Pair<String, String>> attachedDbs = null;
        try {
            attachedDbs = dbObj.getAttachedDbs();
            addLogListenter(attachedDbs, dbObj);
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

    private void addLogListenter(List<Pair<String, String>> attachedDbs, SQLiteDatabase dbObj) {
        if (attachedDbs != null) {
            for (Pair<String, String> p : attachedDbs) {
                logPrint((String) p.second);
            }
            return;
        }
        logPrint(dbObj.getPath());
    }

    private void logPrint(String fileName) {
        File file = new File(fileName);
        FileUtils.copyFile(file, new File(fileName + "-corrupted"));
        File file2 = new File(fileName + "-journal");
        FileUtils.copyFile(file2, new File(fileName + "-journalcorrupted"));
        File file3 = new File(fileName + "-wal");
        FileUtils.copyFile(file3, new File(fileName + "-walcorrupted"));
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
