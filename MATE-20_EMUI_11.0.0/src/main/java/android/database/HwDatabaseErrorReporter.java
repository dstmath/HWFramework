package android.database;

import android.util.Log;

public class HwDatabaseErrorReporter implements IHwDatabaseErrorReporter {
    private static final int CORRUPT_ERROR_CODE = 1;
    private static final Object LOCK = new Object();
    private static final String TAG = "HwDatabaseErrorReporter";
    private static HwDatabaseErrorReporter sInstance = null;

    public static HwDatabaseErrorReporter getInstance() {
        HwDatabaseErrorReporter hwDatabaseErrorReporter;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new HwDatabaseErrorReporter();
            }
            hwDatabaseErrorReporter = sInstance;
        }
        return hwDatabaseErrorReporter;
    }

    public void report(int errorCode, String fileName) {
        if (errorCode == 1) {
            Log.w(TAG, "reportCorruption is triggered.");
            DatabaseCorruptAudit.report(fileName);
        }
    }
}
