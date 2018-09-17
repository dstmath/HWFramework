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

    /* JADX WARNING: Removed duplicated region for block: B:17:0x005b A:{ExcHandler: all (r4_7 'th' java.lang.Throwable), PHI: r0 , Splitter: B:5:0x002d} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:17:0x005b, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:18:0x005c, code:
            r5 = r4;
     */
    /* JADX WARNING: Missing block: B:19:0x005d, code:
            if (r0 != null) goto L_0x005f;
     */
    /* JADX WARNING: Missing block: B:20:0x005f, code:
            r3 = r0.iterator();
     */
    /* JADX WARNING: Missing block: B:22:0x0067, code:
            if (r3.hasNext() != false) goto L_0x0069;
     */
    /* JADX WARNING: Missing block: B:23:0x0069, code:
            deleteDatabaseFile((java.lang.String) ((android.util.Pair) r3.next()).second);
     */
    /* JADX WARNING: Missing block: B:24:0x0077, code:
            deleteDatabaseFile(r8.getPath());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onCorruption(SQLiteDatabase dbObj) {
        Log.e(TAG, "Corruption reported by sqlite on database: " + dbObj.getPath());
        if (dbObj.isOpen()) {
            Iterable attachedDbs = null;
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
            return;
        }
        deleteDatabaseFile(dbObj.getPath());
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
        FileUtils.copyFile(new File(fileName), new File(fileName + "-corrupted"));
        FileUtils.copyFile(new File(fileName + "-journal"), new File(fileName + "-journalcorrupted"));
        FileUtils.copyFile(new File(fileName + "-wal"), new File(fileName + "-walcorrupted"));
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
