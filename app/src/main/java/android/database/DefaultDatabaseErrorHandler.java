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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onCorruption(SQLiteDatabase dbObj) {
        Log.e(TAG, "Corruption reported by sqlite on database: " + dbObj.getPath());
        if (dbObj.isOpen()) {
            Iterable iterable = null;
            try {
                iterable = dbObj.getAttachedDbs();
                addLogListenter(r0, dbObj);
                dbObj.close();
            } catch (SQLiteException e) {
            } catch (Throwable th) {
                Throwable th2 = th;
                if (r0 == null) {
                    deleteDatabaseFile(dbObj.getPath());
                } else {
                    for (Pair<String, String> p : r0) {
                        deleteDatabaseFile((String) p.second);
                    }
                }
            }
            if (r0 != null) {
                for (Pair<String, String> p2 : r0) {
                    deleteDatabaseFile((String) p2.second);
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
