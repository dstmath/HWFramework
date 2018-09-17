package com.android.server.wifi.wifipro.wifiscangenie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
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
    private Object mLock;

    public static class ScanRecord {
        String bssid;
        int cellid;
        int frequency;
        int priority;
        String ssid;

        public ScanRecord(String bssid, String ssid, int frequency, int cellid) {
            this.bssid = bssid;
            this.ssid = ssid;
            this.frequency = frequency;
            this.cellid = cellid;
            this.priority = 0;
        }

        public ScanRecord(String bssid, String ssid, int priority) {
            this.bssid = bssid;
            this.ssid = ssid;
            this.priority = priority;
            this.cellid = -1;
            this.frequency = -1;
        }

        public ScanRecord(String bssid, String ssid, int frequency, int cellid, int priority) {
            this.bssid = bssid;
            this.ssid = ssid;
            this.frequency = frequency;
            this.cellid = cellid;
            this.priority = priority;
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

    public WifiScanGenieDataBaseImpl(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.mLock = new Object();
    }

    public WifiScanGenieDataBaseImpl(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mLock = new Object();
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

    public void closeDB() {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                return;
            }
            Log.e(TAG, " closeDB()");
            this.mDatabase.close();
        }
    }

    public void openDB() {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                this.mDatabase = getWritableDatabase();
            }
        }
    }

    private boolean isValidChannel(int frequency) {
        return frequency > 0;
    }

    public void addNewChannelRecord(String bssid, String ssid, int frequency, int cellids, int priority) {
        if (TextUtils.isEmpty(bssid) || TextUtils.isEmpty(ssid) || !isValidChannel(frequency)) {
            Log.w(TAG, "New Channel Record is illegal ! ignor add");
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                Log.w(TAG, "Database isnot opend!, ignor add");
                return;
            }
            ContentValues values = new ContentValues();
            values.put(CHANNEL_TABLE_BSSID, bssid);
            values.put(CHANNEL_TABLE_SSID, ssid);
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

    public void deleteLastRecords(String tableName) {
        if (TextUtils.isEmpty(tableName)) {
            Log.w(TAG, "tableName Record is illegal ! ignor delete");
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                Log.w(TAG, "Database isnot opend!, ignor delete");
                return;
            }
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

    public void deleteBssidRecord(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            Log.w(TAG, "bssid Record is illegal ! ignor delete");
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                Log.w(TAG, "Database isnot opend!, ignor delete");
                return;
            }
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

    public void deleteSsidRecord(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            Log.w(TAG, "ssid Record is illegal ! ignor delete");
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                Log.w(TAG, "Database isnot opend!, ignor delete");
                return;
            }
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

    public void updateBssidChannelRecord(String bssid, String ssid, int frequency, int priority) {
        if (TextUtils.isEmpty(bssid) || !isValidChannel(frequency)) {
            Log.w(TAG, "update Record is illegal ! ignor update");
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                Log.w(TAG, "Database isnot opend!, ignor update");
                return;
            }
            ContentValues values = new ContentValues();
            values.put(CHANNEL_TABLE_SSID, ssid);
            values.put(CHANNEL_TABLE_FREQUENCY, Integer.valueOf(frequency));
            values.put(CHANNEL_TABLE_PRIORITY, Integer.valueOf(priority));
            values.put(CHANNEL_TABLE_TIME, Long.valueOf(System.currentTimeMillis()));
            if (this.mDatabase.update(CHANNEL_TABLE_NAME, values, " bssid like ?", new String[]{bssid}) > 0) {
                Log.i(TAG, "update succeed");
            }
        }
    }

    public void updateBssidPriorityRecord(String ssid, int priority) {
        if (TextUtils.isEmpty(ssid)) {
            Log.w(TAG, "update Record is illegal ! ignor update");
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                Log.w(TAG, "Database isnot opend!, ignor update");
                return;
            }
            ContentValues values = new ContentValues();
            values.put(CHANNEL_TABLE_PRIORITY, Integer.valueOf(priority));
            values.put(CHANNEL_TABLE_TIME, Long.valueOf(System.currentTimeMillis()));
            if (this.mDatabase.update(CHANNEL_TABLE_NAME, values, " ssid like ?", new String[]{ssid}) > 0) {
                Log.i(TAG, "update succeed");
            }
        }
    }

    public int queryTableSize(String tableName) {
        Cursor cursor;
        if (TextUtils.isEmpty(tableName)) {
            return 0;
        }
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                Log.w(TAG, "Database isnot opend!, ignor queryScanRecordsByCellid");
                return 0;
            }
            cursor = null;
            try {
                cursor = this.mDatabase.rawQuery("SELECT COUNT (_id) FROM " + tableName, null);
                if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return 0;
                }
                int count = cursor.getInt(0) + 1;
                Log.i(TAG, tableName + " count is : " + count);
                if (cursor != null) {
                    cursor.close();
                }
                return count;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public List<ScanRecord> queryScanRecordsByCellid(int cellid) {
        synchronized (this.mLock) {
            Cursor cursor;
            Log.i(TAG, "queryScanRecordsByCellid, cellid:" + cellid);
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                Log.w(TAG, "Database isnot opend!, ignor queryScanRecordsByCellid");
                return null;
            }
            List<ScanRecord> scanRecords = new ArrayList();
            cursor = null;
            if (cellid > 0) {
                try {
                    cursor = this.mDatabase.rawQuery("SELECT * FROM bssid_channel_tables WHERE cellid = " + cellid + " GROUP BY frequency ORDER BY priority DESC ", null);
                } catch (Exception e) {
                    Log.e(TAG, "queryScanRecordsByCellid:" + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } else {
                cursor = this.mDatabase.rawQuery("SELECT * FROM bssid_channel_tables GROUP BY frequency ORDER BY priority DESC LIMIT 5", null);
            }
            while (cursor != null && cursor.moveToNext()) {
                String bssid = cursor.getString(cursor.getColumnIndex(CHANNEL_TABLE_BSSID));
                String ssid = cursor.getString(cursor.getColumnIndex(CHANNEL_TABLE_SSID));
                int frequency = cursor.getInt(cursor.getColumnIndex(CHANNEL_TABLE_FREQUENCY));
                int priority = cursor.getInt(cursor.getColumnIndex(CHANNEL_TABLE_PRIORITY));
                if (TextUtils.isEmpty(bssid) || !isValidChannel(frequency)) {
                    Log.w(TAG, "queryScanRecordsByCellid Record is illegal ! ignor query");
                    if (cursor != null) {
                        cursor.close();
                    }
                    return null;
                }
                scanRecords.add(new ScanRecord(bssid, ssid, frequency, cellid, priority));
            }
            if (cursor != null) {
                cursor.close();
            }
            return scanRecords;
        }
    }

    public List<ScanRecord> queryScanRecordsByBssid(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                Log.w(TAG, "Database isnot opend!, ignor queryScanRecordsByCellid");
                return null;
            } else if (TextUtils.isEmpty(bssid)) {
                return null;
            } else {
                List<ScanRecord> scanRecords = new ArrayList();
                Cursor cursor = null;
                try {
                    cursor = this.mDatabase.rawQuery("SELECT * FROM bssid_channel_tables WHERE bssid LIKE ?", new String[]{bssid});
                    while (cursor.moveToNext()) {
                        String ssid = cursor.getString(cursor.getColumnIndex(CHANNEL_TABLE_SSID));
                        int frequency = cursor.getInt(cursor.getColumnIndex(CHANNEL_TABLE_FREQUENCY));
                        int cellid = cursor.getInt(cursor.getColumnIndex(CHANNEL_TABLE_CELLID));
                        int priority = cursor.getInt(cursor.getColumnIndex(CHANNEL_TABLE_PRIORITY));
                        if (isValidChannel(frequency)) {
                            scanRecords.add(new ScanRecord(bssid, ssid, frequency, cellid, priority));
                        } else {
                            Log.w(TAG, "queryScanRecordsByBssid Record is illegal ! ignor query");
                            if (cursor != null) {
                                cursor.close();
                            }
                            return null;
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "queryScanRecordsByBssid:" + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                return scanRecords;
            }
        }
    }
}
