package com.huawei.hwwifiproservice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
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
    public static final String DATABASE_NAME = "wifisangenie.db";
    public static final int DATABASE_VERSION = 2;
    public static final int SCAN_GENIE_MAX_RECORD = 2000;
    private static final String TAG = "WifiScanGenie_DataBaseImpl";
    private SQLiteDatabase mDatabase;
    private final Object mLock = new Object();

    public WifiScanGenieDataBaseImpl(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public WifiScanGenieDataBaseImpl(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 2);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase db) {
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append("CREATE TABLE [bssid_channel_tables] (");
        strBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        strBuffer.append("[bssid] VARCHAR (64),");
        strBuffer.append("[ssid] VARCHAR (64),");
        strBuffer.append("[frequency] INT (32),");
        strBuffer.append("[priority] INT (32),");
        strBuffer.append("[cellid] INT (32),");
        strBuffer.append("[time] INT (64),");
        strBuffer.append("[ap_type] INT DEFAULT (0) )");
        db.execSQL(strBuffer.toString());
        HwHiLog.i(TAG, false, "wifisangenie.db : onCreate()", new Object[0]);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS bssid_channel_tables");
        onCreate(db);
        HwHiLog.i(TAG, false, "wifisangenie.db : onUpgrade()", new Object[0]);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS bssid_channel_tables");
        onCreate(db);
        HwHiLog.i(TAG, false, "wifisangenie.db : onDowngrade()", new Object[0]);
    }

    public void closeDB() {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    HwHiLog.i(TAG, false, " closeDB()", new Object[0]);
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
                    HwHiLog.e(TAG, false, "openDB(), can't open database!", new Object[0]);
                }
            }
        }
    }

    private boolean isValidChannel(int frequency) {
        return frequency > 0;
    }

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
                        HwHiLog.i(TAG, false, "try insert %{public}s , frequency:%{public}d to db", new Object[]{StringUtilEx.safeDisplaySsid(ssid), Integer.valueOf(frequency)});
                        if (this.mDatabase.insert(CHANNEL_TABLE_NAME, null, values) > 0) {
                            HwHiLog.i(TAG, false, "insert succeed", new Object[0]);
                        }
                        return;
                    }
                }
                HwHiLog.w(TAG, false, "Database isnot opend!, ignore add", new Object[0]);
                return;
            }
        }
        HwHiLog.w(TAG, false, "New Channel Record is illegal ! ignore add", new Object[0]);
    }

    public void deleteLastRecords(String tableName) {
        if (TextUtils.isEmpty(tableName)) {
            HwHiLog.w(TAG, false, "tableName Record is illegal ! ignore delete", new Object[0]);
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    HwHiLog.i(TAG, false, "try delete %{public}s LastRecords form db", new Object[]{tableName});
                    try {
                        if (this.mDatabase.delete(CHANNEL_TABLE_NAME, " time = (select time from bssid_channel_tables order by time LIMIT 1) ", null) > 0) {
                            HwHiLog.i(TAG, false, "delete succeed", new Object[0]);
                        }
                    } catch (SQLiteException e) {
                        HwHiLog.e(TAG, false, "delete error happened in SQLite", new Object[0]);
                    }
                    return;
                }
            }
            HwHiLog.w(TAG, false, "Database isnot opend!, ignore delete", new Object[0]);
        }
    }

    public void deleteBssidRecord(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            HwHiLog.w(TAG, false, "bssid Record is illegal ! ignore delete", new Object[0]);
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    HwHiLog.i(TAG, false, "try delete bssid form db", new Object[0]);
                    try {
                        if (this.mDatabase.delete(CHANNEL_TABLE_NAME, " bssid like ?", new String[]{bssid}) > 0) {
                            HwHiLog.i(TAG, false, "delete succeed", new Object[0]);
                        }
                    } catch (SQLiteException e) {
                        HwHiLog.e(TAG, false, "delete error happened in SQLite", new Object[0]);
                    }
                    return;
                }
            }
            HwHiLog.w(TAG, false, "Database isnot opend!, ignore delete", new Object[0]);
        }
    }

    public void deleteCellIdRecord(int cellId) {
        if (cellId <= 0) {
            HwHiLog.w(TAG, false, "cellId Record is illegal ! ignore delete", new Object[0]);
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                HwHiLog.w(TAG, false, "Database isnot opend!, ignore delete", new Object[0]);
                return;
            }
            try {
                int ret = this.mDatabase.delete(CHANNEL_TABLE_NAME, " cellid = ?", new String[]{Integer.toString(cellId)});
                if (ret >= 0) {
                    HwHiLog.i(TAG, false, "deleteCellIdRecord, delete succeed, ret = %{public}d", new Object[]{Integer.valueOf(ret)});
                }
            } catch (SQLiteException e) {
                HwHiLog.e(TAG, false, "deleteCellIdRecord, delete error SQLite.", new Object[0]);
            }
        }
    }

    public void deleteSsidRecord(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            HwHiLog.w(TAG, false, "ssid Record is illegal ! ignore delete", new Object[0]);
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    HwHiLog.i(TAG, false, "try delete %{public}s form db", new Object[]{StringUtilEx.safeDisplaySsid(ssid)});
                    try {
                        if (this.mDatabase.delete(CHANNEL_TABLE_NAME, " ssid like ?", new String[]{ssid}) > 0) {
                            HwHiLog.i(TAG, false, "delete succeed", new Object[0]);
                        }
                    } catch (SQLiteException e) {
                        HwHiLog.e(TAG, false, "delete error happened in SQLite", new Object[0]);
                    }
                    return;
                }
            }
            HwHiLog.w(TAG, false, "Database isnot opend!, ignore delete", new Object[0]);
        }
    }

    public void updateBssidChannelRecord(String bssid, String ssid, int frequency, int priority) {
        if (TextUtils.isEmpty(bssid) || !isValidChannel(frequency)) {
            HwHiLog.w(TAG, false, "update Record is illegal ! ignore update", new Object[0]);
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
                        HwHiLog.i(TAG, false, "update succeed", new Object[0]);
                    }
                    return;
                }
            }
            HwHiLog.w(TAG, false, "Database isnot opend!, ignore update", new Object[0]);
        }
    }

    public void updateBssidPriorityRecord(String ssid, int priority) {
        if (TextUtils.isEmpty(ssid)) {
            HwHiLog.w(TAG, false, "update Record is illegal ! ignore update", new Object[0]);
            return;
        }
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    ContentValues values = new ContentValues();
                    values.put(CHANNEL_TABLE_PRIORITY, Integer.valueOf(priority));
                    values.put(CHANNEL_TABLE_TIME, Long.valueOf(System.currentTimeMillis()));
                    if (this.mDatabase.update(CHANNEL_TABLE_NAME, values, " ssid like ?", new String[]{ssid}) > 0) {
                        HwHiLog.i(TAG, false, "update succeed", new Object[0]);
                    }
                    return;
                }
            }
            HwHiLog.w(TAG, false, "Database isnot opend!, ignore update", new Object[0]);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005f, code lost:
        if (r2 != null) goto L_0x0061;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0061, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0071, code lost:
        if (0 == 0) goto L_0x0074;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0075, code lost:
        return 0;
     */
    public int queryTableSize(String tableName) {
        if (TextUtils.isEmpty(tableName)) {
            return 0;
        }
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                HwHiLog.w(TAG, false, "Database isnot opend!, ignore queryScanRecordsByCellid", new Object[0]);
                return 0;
            }
            Cursor cursor = null;
            try {
                SQLiteDatabase sQLiteDatabase = this.mDatabase;
                cursor = sQLiteDatabase.rawQuery("SELECT COUNT (_id) FROM " + tableName, null);
                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                    int count = cursor.getInt(0) + 1;
                    HwHiLog.i(TAG, false, "%{public}s count is : %{public}d", new Object[]{tableName, Integer.valueOf(count)});
                    cursor.close();
                    return count;
                }
            } catch (SQLiteException e) {
                HwHiLog.e(TAG, false, "Exception happened in queryTableSize()", new Object[0]);
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00ad, code lost:
        if (r2 == null) goto L_0x00c2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00bf, code lost:
        if (0 == 0) goto L_0x00c2;
     */
    public List<ScanRecord> queryScanRecordsByCellid(int cellid) {
        String bssid;
        int frequency;
        synchronized (this.mLock) {
            HwHiLog.i(TAG, false, "queryScanRecordsByCellid enter", new Object[0]);
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    List<ScanRecord> scanRecords = new ArrayList<>();
                    Cursor cursor = null;
                    if (cellid > 0) {
                        try {
                            SQLiteDatabase sQLiteDatabase = this.mDatabase;
                            cursor = sQLiteDatabase.rawQuery("SELECT * FROM bssid_channel_tables WHERE cellid = " + cellid + " GROUP BY frequency ORDER BY time DESC ", null);
                            while (cursor != null && cursor.moveToNext()) {
                                bssid = cursor.getString(cursor.getColumnIndex("bssid"));
                                String ssid = cursor.getString(cursor.getColumnIndex("ssid"));
                                frequency = cursor.getInt(cursor.getColumnIndex(CHANNEL_TABLE_FREQUENCY));
                                int priority = cursor.getInt(cursor.getColumnIndex(CHANNEL_TABLE_PRIORITY));
                                if (frequency != -100 || (!TextUtils.isEmpty(bssid) && isValidChannel(frequency))) {
                                    scanRecords.add(new ScanRecord(bssid, ssid, frequency, cellid, priority));
                                } else {
                                    HwHiLog.w(TAG, false, "queryScanRecordsByCellid Record is illegal ! ignore query", new Object[0]);
                                    ArrayList arrayList = new ArrayList();
                                    cursor.close();
                                    return arrayList;
                                }
                            }
                        } catch (SQLiteException e) {
                            HwHiLog.e(TAG, false, "Exception happened in queryScanRecordsByCellid()", new Object[0]);
                        } catch (Throwable th) {
                            if (0 != 0) {
                                cursor.close();
                            }
                            throw th;
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
                            scanRecords.add(new ScanRecord(bssid, ssid2, frequency, cellid, priority2));
                        }
                    }
                    cursor.close();
                    return scanRecords;
                }
            }
            HwHiLog.w(TAG, false, "Database isnot opend!, ignore queryScanRecordsByCellid", new Object[0]);
            return new ArrayList();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x009a, code lost:
        if (0 == 0) goto L_0x009d;
     */
    public List<ScanRecord> queryScanRecordsByBssid(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (TextUtils.isEmpty(bssid)) {
                        return new ArrayList();
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
                                HwHiLog.w(TAG, false, "queryScanRecordsByBssid Record is illegal ! ignore query", new Object[0]);
                                ArrayList arrayList = new ArrayList();
                                cursor.close();
                                return arrayList;
                            }
                            scanRecords.add(new ScanRecord(bssid, ssid, frequency, cellid, priority));
                        }
                    } catch (SQLiteException e) {
                        HwHiLog.e(TAG, false, "Exception happened in queryScanRecordsByBssid()", new Object[0]);
                    } catch (Throwable th) {
                        if (0 != 0) {
                            cursor.close();
                        }
                        throw th;
                    }
                    cursor.close();
                    return scanRecords;
                }
            }
            HwHiLog.w(TAG, false, "Database isnot opend!, ignore queryScanRecordsByCellid", new Object[0]);
            return new ArrayList();
        }
    }

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
}
