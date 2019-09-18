package com.android.server.wifi.wifipro.wifiscangenie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class WifiScanGenieDataBaseImpl extends SQLiteOpenHelper {
    public static final String CHANNEL_TABLE_BSSID = "bssid";
    public static final String CHANNEL_TABLE_CELLID = "cellid";
    public static final String CHANNEL_TABLE_FREQUENCY = "frequency";
    public static final String CHANNEL_TABLE_NAME = "bssid_channel_tables";
    public static final String CHANNEL_TABLE_PRIORITY = "priority";
    public static final String CHANNEL_TABLE_SSID = "ssid";
    public static final String CHANNEL_TABLE_TIME = "time";
    public static final String CHANNEL_TABLE_TYPE = "ap_type";
    public static final String DATABASE_NAME = "/data/system/wifisangenie.db";
    public static final int DATABASE_VERSION = 2;
    public static final int SCAN_GENIE_MAX_RECORD = 2000;
    private static final String TAG = "WifiScanGenie_DataBaseImpl";
    private SQLiteDatabase mDatabase;
    private Object mLock = new Object();

    public static class ScanRecord {
        String bssid;
        int cellid;
        int frequency;
        int priority;
        String ssid;

        public ScanRecord(String bssid2, String ssid2, int frequency2, int cellid2) {
            this.bssid = bssid2;
            this.ssid = ssid2;
            this.frequency = frequency2;
            this.cellid = cellid2;
            this.priority = 0;
        }

        public ScanRecord(String bssid2, String ssid2, int priority2) {
            this.bssid = bssid2;
            this.ssid = ssid2;
            this.priority = priority2;
            this.cellid = -1;
            this.frequency = -1;
        }

        public ScanRecord(String bssid2, String ssid2, int frequency2, int cellid2, int priority2) {
            this.bssid = bssid2;
            this.ssid = ssid2;
            this.frequency = frequency2;
            this.cellid = cellid2;
            this.priority = priority2;
        }

        public String getBssid() {
            return this.bssid;
        }

        public String getSsid() {
            return this.ssid;
        }

        public int getCurrentFrequency() {
            return this.frequency;
        }

        public int getCellid() {
            return this.cellid;
        }

        public int gerPriority() {
            return this.priority;
        }
    }

    public WifiScanGenieDataBaseImpl(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public WifiScanGenieDataBaseImpl(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    public void onCreate(SQLiteDatabase db) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE [bssid_channel_tables] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[bssid] VARCHAR (64),");
        sBuffer.append("[ssid] VARCHAR (64),");
        sBuffer.append("[frequency] INT (32),");
        sBuffer.append("[priority] INT (32),");
        sBuffer.append("[cellid] INT (32),");
        sBuffer.append("[time] INT (64),");
        sBuffer.append("[ap_type] INT DEFAULT (0) )");
        db.execSQL(sBuffer.toString());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS bssid_channel_tables");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS bssid_channel_tables");
        onCreate(db);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001f, code lost:
        return;
     */
    public void closeDB() {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    Log.e(TAG, " closeDB()");
                    this.mDatabase.close();
                }
            }
        }
    }

    public void openDB() {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                try {
                    this.mDatabase = getWritableDatabase();
                } catch (SQLiteCantOpenDatabaseException e) {
                    Log.w(TAG, "openDB(), can't open database!");
                }
            }
        }
    }

    private boolean isValidChannel(int frequency) {
        return frequency > 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x009f, code lost:
        return;
     */
    public void addNewChannelRecord(String bssid, String ssid, int frequency, int cellids, int priority) {
        if (frequency == -100 || (!TextUtils.isEmpty(bssid) && !TextUtils.isEmpty(ssid) && isValidChannel(frequency))) {
            synchronized (this.mLock) {
                if (this.mDatabase != null) {
                    if (this.mDatabase.isOpen()) {
                        ContentValues values = new ContentValues();
                        values.put("bssid", bssid);
                        values.put("ssid", ssid);
                        values.put(CHANNEL_TABLE_FREQUENCY, Integer.valueOf(frequency));
                        values.put(CHANNEL_TABLE_CELLID, Integer.valueOf(cellids));
                        values.put(CHANNEL_TABLE_PRIORITY, Integer.valueOf(priority));
                        values.put(CHANNEL_TABLE_TIME, Long.valueOf(System.currentTimeMillis()));
                        Log.i(TAG, "try insert " + ssid + " , frequency:" + frequency + " to db");
                        if (this.mDatabase.insert(CHANNEL_TABLE_NAME, null, values) > 0) {
                            Log.i(TAG, "insert succeed");
                        }
                    }
                }
                Log.w(TAG, "Database isnot opend!, ignor add");
                return;
            }
        }
        Log.w(TAG, "New Channel Record is illegal ! ignor add");
    }

    public void deleteLastRecords(String tableName) {
        if (TextUtils.isEmpty(tableName)) {
            Log.w(TAG, "tableName Record is illegal ! ignor delete");
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    Log.i(TAG, "try delete " + tableName + " LastRecords form db");
                    try {
                        if (this.mDatabase.delete(CHANNEL_TABLE_NAME, " time = (select time from bssid_channel_tables order by time LIMIT 1) ", null) > 0) {
                            Log.i(TAG, "delete succeed");
                        }
                    } catch (SQLiteException e) {
                        Log.i(TAG, "delete error SQLite: " + e);
                    }
                }
            }
            Log.w(TAG, "Database isnot opend!, ignor delete");
        }
    }

    public void deleteBssidRecord(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            Log.w(TAG, "bssid Record is illegal ! ignor delete");
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    Log.i(TAG, "try delete " + bssid + " form db");
                    try {
                        if (this.mDatabase.delete(CHANNEL_TABLE_NAME, " bssid like ?", new String[]{bssid}) > 0) {
                            Log.i(TAG, "delete succeed");
                        }
                    } catch (SQLiteException e) {
                        Log.i(TAG, "delete error SQLite: " + e);
                    }
                }
            }
            Log.w(TAG, "Database isnot opend!, ignor delete");
        }
    }

    public void deleteCellIdRecord(int cellId) {
        if (cellId <= 0) {
            Log.w(TAG, "cellId Record is illegal ! ignore delete");
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                Log.w(TAG, "Database isnot opend!, ignore delete");
                return;
            }
            try {
                int ret = this.mDatabase.delete(CHANNEL_TABLE_NAME, " cellid = ?", new String[]{Integer.toString(cellId)});
                if (ret >= 0) {
                    Log.i(TAG, "deleteCellIdRecord, delete succeed, ret = " + ret);
                }
            } catch (SQLiteException e) {
                Log.i(TAG, "deleteCellIdRecord, delete error SQLite.");
            }
        }
    }

    public void deleteSsidRecord(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            Log.w(TAG, "ssid Record is illegal ! ignor delete");
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    Log.i(TAG, "try delete " + ssid + " form db");
                    try {
                        if (this.mDatabase.delete(CHANNEL_TABLE_NAME, " ssid like ?", new String[]{ssid}) > 0) {
                            Log.i(TAG, "delete succeed");
                        }
                    } catch (SQLiteException e) {
                        Log.i(TAG, "delete error SQLite: " + e);
                    }
                }
            }
            Log.w(TAG, "Database isnot opend!, ignor delete");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0060, code lost:
        return;
     */
    public void updateBssidChannelRecord(String bssid, String ssid, int frequency, int priority) {
        if (TextUtils.isEmpty(bssid) || !isValidChannel(frequency)) {
            Log.w(TAG, "update Record is illegal ! ignor update");
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    ContentValues values = new ContentValues();
                    values.put("ssid", ssid);
                    values.put(CHANNEL_TABLE_FREQUENCY, Integer.valueOf(frequency));
                    values.put(CHANNEL_TABLE_PRIORITY, Integer.valueOf(priority));
                    values.put(CHANNEL_TABLE_TIME, Long.valueOf(System.currentTimeMillis()));
                    if (this.mDatabase.update(CHANNEL_TABLE_NAME, values, " bssid like ?", new String[]{bssid}) > 0) {
                        Log.i(TAG, "update succeed");
                    }
                }
            }
            Log.w(TAG, "Database isnot opend!, ignor update");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0053, code lost:
        return;
     */
    public void updateBssidPriorityRecord(String ssid, int priority) {
        if (TextUtils.isEmpty(ssid)) {
            Log.w(TAG, "update Record is illegal ! ignor update");
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    ContentValues values = new ContentValues();
                    values.put(CHANNEL_TABLE_PRIORITY, Integer.valueOf(priority));
                    values.put(CHANNEL_TABLE_TIME, Long.valueOf(System.currentTimeMillis()));
                    if (this.mDatabase.update(CHANNEL_TABLE_NAME, values, " ssid like ?", new String[]{ssid}) > 0) {
                        Log.i(TAG, "update succeed");
                    }
                }
            }
            Log.w(TAG, "Database isnot opend!, ignor update");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0067, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0068, code lost:
        if (r3 != null) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006a, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007a, code lost:
        if (r3 == null) goto L_0x007d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x007e, code lost:
        return 0;
     */
    public int queryTableSize(String tableName) {
        int count;
        if (TextUtils.isEmpty(tableName)) {
            return 0;
        }
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                Log.w(TAG, "Database isnot opend!, ignor queryScanRecordsByCellid");
                return 0;
            }
            Cursor cursor = null;
            try {
                cursor = this.mDatabase.rawQuery("SELECT COUNT (_id) FROM " + tableName, null);
                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                    Log.i(TAG, tableName + " count is : " + count);
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } catch (Exception e) {
                try {
                    Log.e(TAG, e.getMessage());
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x009d, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00ac, code lost:
        if (r3 == null) goto L_0x00cc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00c9, code lost:
        if (r3 == null) goto L_0x00cc;
     */
    public List<ScanRecord> queryScanRecordsByCellid(int cellid) {
        String bssid;
        int frequency;
        synchronized (this.mLock) {
            Log.i(TAG, "queryScanRecordsByCellid enter");
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    List<ScanRecord> scanRecords = new ArrayList<>();
                    Cursor cursor = null;
                    if (cellid > 0) {
                        try {
                            cursor = this.mDatabase.rawQuery("SELECT * FROM bssid_channel_tables WHERE cellid = " + cellid + " GROUP BY frequency ORDER BY time DESC ", null);
                            while (cursor != null && cursor.moveToNext()) {
                                bssid = cursor.getString(cursor.getColumnIndex("bssid"));
                                String ssid = cursor.getString(cursor.getColumnIndex("ssid"));
                                frequency = cursor.getInt(cursor.getColumnIndex(CHANNEL_TABLE_FREQUENCY));
                                int priority = cursor.getInt(cursor.getColumnIndex(CHANNEL_TABLE_PRIORITY));
                                if (frequency != -100 || (!TextUtils.isEmpty(bssid) && isValidChannel(frequency))) {
                                    ScanRecord scanRecord = new ScanRecord(bssid, ssid, frequency, cellid, priority);
                                    scanRecords.add(scanRecord);
                                } else {
                                    Log.w(TAG, "queryScanRecordsByCellid Record is illegal ! ignor query");
                                    if (cursor != null) {
                                        cursor.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            try {
                                Log.e(TAG, "queryScanRecordsByCellid:" + e);
                            } catch (Throwable th) {
                                if (cursor != null) {
                                    cursor.close();
                                }
                                throw th;
                            }
                        }
                    } else {
                        cursor = this.mDatabase.rawQuery("SELECT * FROM bssid_channel_tables GROUP BY frequency ORDER BY time DESC LIMIT 5", null);
                        while (cursor != null) {
                            bssid = cursor.getString(cursor.getColumnIndex("bssid"));
                            String ssid2 = cursor.getString(cursor.getColumnIndex("ssid"));
                            frequency = cursor.getInt(cursor.getColumnIndex(CHANNEL_TABLE_FREQUENCY));
                            int priority2 = cursor.getInt(cursor.getColumnIndex(CHANNEL_TABLE_PRIORITY));
                            if (frequency != -100) {
                            }
                            ScanRecord scanRecord2 = new ScanRecord(bssid, ssid2, frequency, cellid, priority2);
                            scanRecords.add(scanRecord2);
                        }
                    }
                    cursor.close();
                    return scanRecords;
                }
            }
            Log.w(TAG, "Database isnot opend!, ignor queryScanRecordsByCellid");
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0071, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x007f, code lost:
        if (r3 != null) goto L_0x0081;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x009e, code lost:
        if (r3 == null) goto L_0x00a1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a2, code lost:
        return r1;
     */
    public List<ScanRecord> queryScanRecordsByBssid(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (TextUtils.isEmpty(bssid)) {
                        return null;
                    }
                    List<ScanRecord> scanRecords = new ArrayList<>();
                    Cursor cursor = null;
                    try {
                        cursor = this.mDatabase.rawQuery("SELECT * FROM bssid_channel_tables WHERE bssid LIKE ?", new String[]{bssid});
                        while (cursor.moveToNext()) {
                            String ssid = cursor.getString(cursor.getColumnIndex("ssid"));
                            int frequency = cursor.getInt(cursor.getColumnIndex(CHANNEL_TABLE_FREQUENCY));
                            int cellid = cursor.getInt(cursor.getColumnIndex(CHANNEL_TABLE_CELLID));
                            int priority = cursor.getInt(cursor.getColumnIndex(CHANNEL_TABLE_PRIORITY));
                            if (!isValidChannel(frequency)) {
                                Log.w(TAG, "queryScanRecordsByBssid Record is illegal ! ignor query");
                                if (cursor != null) {
                                    cursor.close();
                                }
                            } else {
                                ScanRecord scanRecord = new ScanRecord(bssid, ssid, frequency, cellid, priority);
                                scanRecords.add(scanRecord);
                            }
                        }
                    } catch (Exception e) {
                        try {
                            Log.e(TAG, "queryScanRecordsByBssid:" + e);
                        } catch (Throwable th) {
                            if (cursor != null) {
                                cursor.close();
                            }
                            throw th;
                        }
                    }
                }
            }
            Log.w(TAG, "Database isnot opend!, ignor queryScanRecordsByCellid");
            return null;
        }
    }
}
