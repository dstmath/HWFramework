package com.android.server.wifi.MSS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.util.ArrayList;
import java.util.List;

public class HwMssBlackListManager implements IHwMssBlacklistMgr {
    private static final int DATA_BASE_MAX_NUM = 500;
    private static final boolean DEBUG = false;
    private static final String TAG = "HwMssBlackListManager";
    private static final long TIME_EXPIRED = 86400000;
    private static HwMssBlackListManager sHwMssBlackListManager = null;
    private SQLiteDatabase mDatabase;
    private HwMssBlackListHelper mHelper;
    private final Object mLock = new Object();

    private HwMssBlackListManager(Context context) {
        try {
            this.mHelper = new HwMssBlackListHelper(context);
            this.mDatabase = this.mHelper.getWritableDatabase();
        } catch (SQLiteCantOpenDatabaseException e) {
            this.mDatabase = null;
            HwHiLog.e(TAG, false, "SQLiteDatabase is null, error!", new Object[0]);
        }
    }

    public static synchronized IHwMssBlacklistMgr getInstance(Context context) {
        HwMssBlackListManager hwMssBlackListManager;
        synchronized (HwMssBlackListManager.class) {
            if (sHwMssBlackListManager == null) {
                sHwMssBlackListManager = new HwMssBlackListManager(context);
            }
            hwMssBlackListManager = sHwMssBlackListManager;
        }
        return hwMssBlackListManager;
    }

    @Override // com.android.server.wifi.MSS.IHwMssBlacklistMgr
    public void closeDb() {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    this.mDatabase.close();
                }
            }
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x000d: APUT  (r0v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r1v0 java.lang.String) */
    @Override // com.android.server.wifi.MSS.IHwMssBlacklistMgr
    public boolean addToBlacklist(String ssid, String bssid, int reasonCode) {
        Object[] objArr = new Object[3];
        objArr[0] = ssid == null ? "null" : StringUtilEx.safeDisplaySsid(ssid);
        objArr[1] = StringUtilEx.safeDisplayBssid(bssid);
        objArr[2] = Integer.valueOf(reasonCode);
        dbg(false, "Try to add to blacklist. ssid:%{public}s; bssid:%{private}s; reasonCode: %{public}d", objArr);
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (ssid != null) {
                    String safeBssid = StringUtilEx.safeDisplayBssid(bssid);
                    if (!inlineIsRecordInDatabase(ssid)) {
                        inlineCheckIfDatabaseFull();
                        return inlineAddBlacklist(ssid, safeBssid, reasonCode);
                    }
                    dbg(false, "%{public}s is already in blacklist, update it.", ssid);
                    return inlineUpdateBlacklist(ssid, safeBssid, reasonCode);
                }
            }
            dbg(false, "Failed to add to blacklist. ssid: %{public}s", StringUtilEx.safeDisplaySsid(ssid));
            return false;
        }
    }

    @Override // com.android.server.wifi.MSS.IHwMssBlacklistMgr
    public boolean addToBlacklist(HwMssDatabaseItem item) {
        return addToBlacklist(item.ssid, item.bssid, item.reasonCode);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0056, code lost:
        if (r2 != null) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0058, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0068, code lost:
        if (0 == 0) goto L_0x006b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x006c, code lost:
        return r0;
     */
    @Override // com.android.server.wifi.MSS.IHwMssBlacklistMgr
    public boolean isInBlacklist(String ssid) {
        boolean isFind = false;
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || ssid == null) {
                return false;
            }
            Cursor cursor = null;
            try {
                cursor = this.mDatabase.rawQuery("SELECT * FROM BlackListTable where ssid like ?", new String[]{ssid});
                if (cursor != null && cursor.moveToNext()) {
                    isFind = true;
                }
                if (isFind) {
                    long time = cursor.getLong(cursor.getColumnIndex("update_time"));
                    if (System.currentTimeMillis() - time > TIME_EXPIRED) {
                        dbg(false, "%{public}s is expired, last update time: %{public}s", ssid, String.valueOf(time));
                        isFind = false;
                    }
                }
            } catch (SQLException e) {
                HwHiLog.w(TAG, false, "Failed to query the blacklist", new Object[0]);
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
        }
    }

    @Override // com.android.server.wifi.MSS.IHwMssBlacklistMgr
    public boolean isInBlacklistByBssid(String bssid) {
        return false;
    }

    @Override // com.android.server.wifi.MSS.IHwMssBlacklistMgr
    public List<HwMssDatabaseItem> getBlacklist(boolean isValid) {
        return new ArrayList();
    }

    /* access modifiers changed from: protected */
    public void delBlacklistBySsid(String ssid) {
        if (ssid != null) {
            synchronized (this.mLock) {
                if (this.mDatabase != null) {
                    if (this.mDatabase.isOpen()) {
                        inlineDeleteBlacklistBySsid(ssid);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void delBlacklistAll() {
        dbg(false, "delete all blacklist", new Object[0]);
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    this.mDatabase.execSQL("DELETE FROM BlackListTable");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0074, code lost:
        if (r2 != null) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0076, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0086, code lost:
        if (0 == 0) goto L_0x0089;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x008a, code lost:
        return;
     */
    public void dumpBlacklist() {
        dbg(false, "dump blacklist:", new Object[0]);
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                Cursor cursor = null;
                try {
                    cursor = this.mDatabase.rawQuery("SELECT * FROM BlackListTable", null);
                    while (cursor != null && cursor.moveToNext()) {
                        dbg(false, "ssid:%{public}s;bssid:%{private}s;reason_code:%{public}d;time:%{public}s", cursor.getString(cursor.getColumnIndex("ssid")), StringUtilEx.safeDisplayBssid(cursor.getString(cursor.getColumnIndex("bssid"))), Integer.valueOf(cursor.getInt(cursor.getColumnIndex("reason_code"))), String.valueOf(cursor.getLong(cursor.getColumnIndex("update_time"))));
                    }
                } catch (SQLException e) {
                    HwHiLog.w(TAG, false, "Failed to dump the blacklist", new Object[0]);
                } catch (Throwable th) {
                    if (0 != 0) {
                        cursor.close();
                    }
                    throw th;
                }
            }
        }
    }

    private void inlineDeleteBlacklistBySsid(String ssid) {
        if (ssid != null) {
            this.mDatabase.delete("BlackListTable", "ssid like ?", new String[]{ssid});
        }
    }

    private boolean inlineAddBlacklist(String ssid, String bssid, int reasonCode) {
        try {
            this.mDatabase.execSQL("INSERT INTO BlackListTable VALUES(null, ?, ?, ?, ?, ?, ?)", new Object[]{ssid, bssid, Integer.valueOf(reasonCode), -1, Long.valueOf(System.currentTimeMillis()), 0});
            return true;
        } catch (SQLException e) {
            HwHiLog.w(TAG, false, "Failed to add the blacklist", new Object[0]);
            return false;
        }
    }

    private boolean inlineUpdateBlacklist(String ssid, String bssid, int reasonCode) {
        ContentValues values = new ContentValues();
        values.put("bssid", bssid);
        values.put("ssid", ssid);
        values.put("reason_code", Integer.valueOf(reasonCode));
        values.put("update_time", Long.valueOf(System.currentTimeMillis()));
        this.mDatabase.update("BlackListTable", values, "ssid like ?", new String[]{ssid});
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0037, code lost:
        if (r4 != null) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0039, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004a, code lost:
        if (0 == 0) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004f, code lost:
        if (r3 < com.android.server.wifi.MSS.HwMssBlackListManager.DATA_BASE_MAX_NUM) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0051, code lost:
        if (r2 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0053, code lost:
        inlineDeleteBlacklistBySsid(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return;
     */
    private void inlineCheckIfDatabaseFull() {
        long earliestTime = Long.MAX_VALUE;
        String delSsid = null;
        int dataNum = 0;
        Cursor cursor = null;
        try {
            cursor = this.mDatabase.rawQuery("SELECT * FROM BlackListTable", null);
            while (cursor != null && cursor.moveToNext()) {
                dataNum++;
                long time = cursor.getLong(cursor.getColumnIndex("update_time"));
                String ssid = cursor.getString(cursor.getColumnIndex("ssid"));
                if (time < earliestTime) {
                    earliestTime = time;
                    delSsid = ssid;
                }
            }
        } catch (SQLException e) {
            HwHiLog.w(TAG, false, "Failed to check the blacklist", new Object[0]);
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002c, code lost:
        if (0 == 0) goto L_0x002f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002f, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x001a, code lost:
        if (r1 != null) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001c, code lost:
        r1.close();
     */
    private boolean inlineIsRecordInDatabase(String ssid) {
        boolean isFind = false;
        Cursor cursor = null;
        try {
            cursor = this.mDatabase.rawQuery("SELECT * FROM BlackListTable where ssid like ?", new String[]{ssid});
            if (cursor != null && cursor.moveToNext()) {
                isFind = true;
            }
        } catch (SQLException e) {
            HwHiLog.w(TAG, false, "Failed to query the blacklist when check records", new Object[0]);
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    private void dbg(boolean isFmtStrPrivate, String msg, Object... args) {
    }

    class HwMssBlackListHelper extends SQLiteOpenHelper {
        public static final String BLACKLIST_TABLE_NAME = "BlackListTable";
        private static final String DATABASE_NAME = "/data/system/HwMSSBlackList.db";
        private static final int DATABASE_VERSION = 1;

        HwMssBlackListHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        HwMssBlackListHelper(Context context) {
            super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 1);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase db) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("CREATE TABLE [BlackListTable] (");
            buffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
            buffer.append("[ssid] TEXT,");
            buffer.append("[bssid] TEXT,");
            buffer.append("[reason_code] INTEGER,");
            buffer.append("[direction] INTEGER,");
            buffer.append("[update_time] LONG,");
            buffer.append("[reserved] INTEGER)");
            db.execSQL(buffer.toString());
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS BlackListTable");
            onCreate(db);
        }
    }
}
