package android.net.dhcp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.LruCache;

public class DhcpResultsInfoDBManager {
    private static final int DHCP_RESULTS_DB_SIZE = 50;
    private static final String TAG = "DhcpResultsInfoDBManager";
    private static DhcpResultsInfoDBManager mDataBaseManager;
    private SQLiteDatabase mDatabase;
    private DhcpResultsInfoDBHelper mHelper;
    private Object mdbLock = new Object();

    public DhcpResultsInfoDBManager(Context context) {
        this.mHelper = new DhcpResultsInfoDBHelper(context);
        this.mDatabase = this.mHelper.getWritableDatabase();
    }

    public static DhcpResultsInfoDBManager getInstance(Context context) {
        if (mDataBaseManager == null) {
            mDataBaseManager = new DhcpResultsInfoDBManager(context);
        }
        return mDataBaseManager;
    }

    private boolean deleteHistoryRecord(String dbTableName, String apssid) {
        synchronized (this.mdbLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    if (apssid == null) {
                        Log.e(TAG, "deleteHistoryRecord null error.");
                        return false;
                    }
                    try {
                        this.mDatabase.delete(dbTableName, "apSSID like ?", new String[]{apssid});
                        return true;
                    } catch (SQLException e) {
                        Log.e(TAG, "deleteHistoryRecord error:" + e);
                        return false;
                    }
                }
            }
            Log.e(TAG, "deleteHistoryRecord database error.");
            return false;
        }
    }

    public boolean deleteDhcpResultsInfoRecord(String apssid) {
        Log.d(TAG, "deleteApInfoRecord delete record of ssid:" + apssid);
        return deleteHistoryRecord(DhcpResultsInfoDBHelper.DHCP_RESULTS_INFO_DB_NAME, apssid);
    }

    private boolean checkHistoryRecordExist(String dbTableName, String apssid) {
        boolean ret = false;
        Cursor c = null;
        try {
            SQLiteDatabase sQLiteDatabase = this.mDatabase;
            Cursor c2 = sQLiteDatabase.rawQuery("SELECT * FROM " + dbTableName + " where apSSID like ?", new String[]{apssid});
            int rcdCount = c2.getCount();
            if (rcdCount > 0) {
                ret = true;
            }
            Log.d(TAG, "checkHistoryRecordExist read from:" + dbTableName + ", get record: " + rcdCount);
            if (c2 != null) {
                c2.close();
            }
            return ret;
        } catch (SQLException e) {
            Log.e(TAG, "checkHistoryRecordExist error:" + e);
            if (c != null) {
                c.close();
            }
            return false;
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    private boolean updateDhcpResultsInfoRecord(DhcpResultsInfoRecord dbr) {
        ContentValues values = new ContentValues();
        values.put("apSSID", dbr.apSSID);
        values.put("IP", dbr.staIP);
        values.put("DHCPServer", dbr.apDhcpServer);
        values.put("EX1", "ex1");
        values.put("EX2", "ex2");
        try {
            int rowChg = this.mDatabase.update(DhcpResultsInfoDBHelper.DHCP_RESULTS_INFO_DB_NAME, values, "apSSID like ?", new String[]{dbr.apSSID});
            if (rowChg == 0) {
                Log.e(TAG, "updateDhcpResultsInfoRecord update failed.");
                return false;
            }
            Log.d(TAG, "updateDhcpResultsInfoRecord update succ, rowChg=" + rowChg);
            return true;
        } catch (SQLException e) {
            Log.e(TAG, "updateDhcpResultsInfoRecord error:" + e);
            return false;
        }
    }

    private boolean insertDhcpResultsInfoRecord(DhcpResultsInfoRecord dbr) {
        try {
            this.mDatabase.execSQL("INSERT INTO DhcpResults VALUES(?, ?, ?, ?, ?)", new Object[]{dbr.apSSID, dbr.staIP, dbr.apDhcpServer, "ex1", "ex2"});
            Log.i(TAG, "insertDhcpResultsInfoRecord add a record succ.");
            return true;
        } catch (SQLException e) {
            Log.e(TAG, "insertDhcpResultsInfoRecord error:" + e);
            return false;
        }
    }

    public boolean addOrUpdateDhcpResultsInfoRecord(DhcpResultsInfoRecord dbr) {
        synchronized (this.mdbLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (dbr != null) {
                    if (dbr.apSSID == null) {
                        Log.e(TAG, "addOrUpdateApInfoRecord null error.");
                        return false;
                    } else if (checkHistoryRecordExist(DhcpResultsInfoDBHelper.DHCP_RESULTS_INFO_DB_NAME, dbr.apSSID)) {
                        boolean updateDhcpResultsInfoRecord = updateDhcpResultsInfoRecord(dbr);
                        return updateDhcpResultsInfoRecord;
                    } else {
                        boolean insertDhcpResultsInfoRecord = insertDhcpResultsInfoRecord(dbr);
                        return insertDhcpResultsInfoRecord;
                    }
                }
            }
            Log.e(TAG, "addOrUpdateApInfoRecord error.");
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0034, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0064, code lost:
        if (r2 != null) goto L_0x0066;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0084, code lost:
        if (r2 == null) goto L_0x0087;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0088, code lost:
        return r1;
     */
    public LruCache<String, DhcpResultsInfoRecord> getAllDhcpResultsInfo() {
        synchronized (this.mdbLock) {
            LruCache<String, DhcpResultsInfoRecord> dhcpResultsInfo = new LruCache<>(50);
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                Log.e(TAG, "getAllDhcpResultsInfo database error.");
                return null;
            }
            Cursor c = null;
            try {
                c = this.mDatabase.rawQuery("SELECT * FROM DhcpResults", null);
                if (c == null) {
                    Log.e(TAG, "query database error.");
                    if (c != null) {
                        c.close();
                    }
                } else {
                    while (c.moveToNext()) {
                        DhcpResultsInfoRecord dbr = new DhcpResultsInfoRecord(c.getString(c.getColumnIndex("apSSID")), c.getString(c.getColumnIndex("IP")), c.getString(c.getColumnIndex("DHCPServer")));
                        dhcpResultsInfo.put(dbr.apSSID, dbr);
                    }
                }
            } catch (Exception e) {
                try {
                    Log.e(TAG, "queryDhcpResultsInfo error:" + e);
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                    throw th;
                }
            }
        }
    }
}
