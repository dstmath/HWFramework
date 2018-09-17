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
    private Object mdbLock;

    private boolean checkHistoryRecordExist(java.lang.String r10, java.lang.String r11) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x007c in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r9 = this;
        r8 = 0;
        r3 = 0;
        r0 = 0;
        r4 = r9.mDatabase;	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5.<init>();	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r6 = "SELECT * FROM ";	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5 = r5.append(r6);	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5 = r5.append(r10);	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r6 = " where apSSID like ?";	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5 = r5.append(r6);	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5 = r5.toString();	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r6 = 1;	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r6 = new java.lang.String[r6];	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r7 = 0;	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r6[r7] = r11;	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r0 = r4.rawQuery(r5, r6);	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r2 = r0.getCount();	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        if (r2 <= 0) goto L_0x0031;	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
    L_0x0030:
        r3 = 1;	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
    L_0x0031:
        r4 = "DhcpResultsInfoDBManager";	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5.<init>();	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r6 = "checkHistoryRecordExist read from:";	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5 = r5.append(r6);	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5 = r5.append(r10);	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r6 = ", get record: ";	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5 = r5.append(r6);	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5 = r5.append(r2);	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5 = r5.toString();	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        android.util.Log.d(r4, r5);	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        if (r0 == 0) goto L_0x005b;
    L_0x0058:
        r0.close();
    L_0x005b:
        return r3;
    L_0x005c:
        r1 = move-exception;
        r4 = "DhcpResultsInfoDBManager";	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5.<init>();	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r6 = "checkHistoryRecordExist error:";	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5 = r5.append(r6);	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5 = r5.append(r1);	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        r5 = r5.toString();	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        android.util.Log.e(r4, r5);	 Catch:{ SQLException -> 0x005c, all -> 0x007d }
        if (r0 == 0) goto L_0x007c;
    L_0x0079:
        r0.close();
    L_0x007c:
        return r8;
    L_0x007d:
        r4 = move-exception;
        if (r0 == 0) goto L_0x0083;
    L_0x0080:
        r0.close();
    L_0x0083:
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.dhcp.DhcpResultsInfoDBManager.checkHistoryRecordExist(java.lang.String, java.lang.String):boolean");
    }

    public DhcpResultsInfoDBManager(Context context) {
        this.mdbLock = new Object();
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
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                Log.e(TAG, "deleteHistoryRecord database error.");
                return false;
            } else if (apssid == null) {
                Log.e(TAG, "deleteHistoryRecord null error.");
                return false;
            } else {
                try {
                    this.mDatabase.delete(dbTableName, "apSSID like ?", new String[]{apssid});
                    return true;
                } catch (SQLException e) {
                    Log.e(TAG, "deleteHistoryRecord error:" + e);
                    return false;
                }
            }
        }
    }

    public boolean deleteDhcpResultsInfoRecord(String apssid) {
        Log.d(TAG, "deleteApInfoRecord delete record of ssid:" + apssid);
        return deleteHistoryRecord(DhcpResultsInfoDBHelper.DHCP_RESULTS_INFO_DB_NAME, apssid);
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
            if (this.mDatabase == null || !this.mDatabase.isOpen() || dbr == null) {
                Log.e(TAG, "addOrUpdateApInfoRecord error.");
                return false;
            } else if (dbr.apSSID == null) {
                Log.e(TAG, "addOrUpdateApInfoRecord null error.");
                return false;
            } else if (checkHistoryRecordExist(DhcpResultsInfoDBHelper.DHCP_RESULTS_INFO_DB_NAME, dbr.apSSID)) {
                r1 = updateDhcpResultsInfoRecord(dbr);
                return r1;
            } else {
                r1 = insertDhcpResultsInfoRecord(dbr);
                return r1;
            }
        }
    }

    public LruCache<String, DhcpResultsInfoRecord> getAllDhcpResultsInfo() {
        synchronized (this.mdbLock) {
            LruCache<String, DhcpResultsInfoRecord> dhcpResultsInfo = new LruCache(DHCP_RESULTS_DB_SIZE);
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                Log.e(TAG, "getAllDhcpResultsInfo database error.");
                return null;
            }
            r0 = null;
            try {
                r0 = this.mDatabase.rawQuery("SELECT * FROM DhcpResults", null);
                if (r0 == null) {
                    Log.e(TAG, "query database error.");
                    if (r0 != null) {
                        r0.close();
                    }
                    return null;
                }
                while (r0.moveToNext()) {
                    DhcpResultsInfoRecord dbr = new DhcpResultsInfoRecord(r0.getString(r0.getColumnIndex("apSSID")), r0.getString(r0.getColumnIndex("IP")), r0.getString(r0.getColumnIndex("DHCPServer")));
                    dhcpResultsInfo.put(dbr.apSSID, dbr);
                }
                if (r0 != null) {
                    r0.close();
                }
                return dhcpResultsInfo;
            } catch (Exception e) {
                Log.e(TAG, "queryDhcpResultsInfo error:" + e);
                if (r0 != null) {
                    r0.close();
                }
            } catch (Throwable th) {
                Cursor cursor;
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }
}
