package com.android.server.wifi.MSS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class HwMSSBlackListManager implements IHwMSSBlacklistMgr {
    private static final int DATA_BASE_MAX_NUM = 500;
    private static final boolean DEBUG = false;
    private static final String TAG = "HwMSSBlackListManager";
    private static final long TIME_EXPIRED = 86400000;
    private static HwMSSBlackListManager mHwMSSBlackListManager = null;
    private SQLiteDatabase mDatabase;
    private HwMSSBlackListHelper mHelper;
    private Object mLock = new Object();

    class HwMSSBlackListHelper extends SQLiteOpenHelper {
        public static final String BLACKLIST_TABLE_NAME = "BlackListTable";
        private static final String DATABASE_NAME = "/data/system/HwMSSBlackList.db";
        private static final int DATABASE_VERSION = 1;

        public HwMSSBlackListHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public HwMSSBlackListHelper(Context context) {
            super(context, DATABASE_NAME, null, 1);
        }

        public void onCreate(SQLiteDatabase db) {
            StringBuffer sBuffer = new StringBuffer();
            sBuffer.append("CREATE TABLE [BlackListTable] (");
            sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
            sBuffer.append("[ssid] TEXT,");
            sBuffer.append("[bssid] TEXT,");
            sBuffer.append("[reason_code] INTEGER,");
            sBuffer.append("[direction] INTEGER,");
            sBuffer.append("[update_time] LONG,");
            sBuffer.append("[reserved] INTEGER)");
            db.execSQL(sBuffer.toString());
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS BlackListTable");
            onCreate(db);
        }
    }

    private HwMSSBlackListManager(Context context) {
        try {
            this.mHelper = new HwMSSBlackListHelper(context);
            this.mDatabase = this.mHelper.getWritableDatabase();
        } catch (SQLiteCantOpenDatabaseException e) {
            this.mDatabase = null;
            Log.e(TAG, "SQLiteDatabase is null, error!");
        }
    }

    public static synchronized IHwMSSBlacklistMgr getInstance(Context context) {
        HwMSSBlackListManager hwMSSBlackListManager;
        synchronized (HwMSSBlackListManager.class) {
            if (mHwMSSBlackListManager == null) {
                mHwMSSBlackListManager = new HwMSSBlackListManager(context);
            }
            hwMSSBlackListManager = mHwMSSBlackListManager;
        }
        return hwMSSBlackListManager;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0018, code lost:
        return;
     */
    public void closeDB() {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    this.mDatabase.close();
                }
            }
        }
    }

    public boolean addToBlacklist(String ssid, String bssid, int reasoncode) {
        dbg("Try to add to blacklist. ssid:" + ssid + "; bssid:" + hideBssid(bssid) + "; reasoncode: " + reasoncode);
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (ssid != null) {
                    String bssid2 = hideBssid(bssid);
                    if (!inlineIsRecordInDatabase(ssid)) {
                        inlineCheckIfDatabaseFull();
                        boolean inlineAddBlacklist = inlineAddBlacklist(ssid, bssid2, reasoncode);
                        return inlineAddBlacklist;
                    }
                    dbg(ssid + " is already in blacklist, update it.");
                    boolean inlineUpdateBlacklist = inlineUpdateBlacklist(ssid, bssid2, reasoncode);
                    return inlineUpdateBlacklist;
                }
            }
            dbg("Failed to add to blacklist. ssid: " + ssid);
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005d, code lost:
        if (r2 != null) goto L_0x005f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006d, code lost:
        if (r2 == null) goto L_0x0070;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0071, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0079, code lost:
        return false;
     */
    public boolean isInBlacklist(String ssid) {
        boolean find = false;
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen() && ssid != null) {
                Cursor c = null;
                try {
                    c = this.mDatabase.rawQuery("SELECT * FROM BlackListTable where ssid like ?", new String[]{ssid});
                    if (c != null && c.moveToNext()) {
                        find = true;
                    }
                    if (find) {
                        long time = c.getLong(c.getColumnIndex("update_time"));
                        if (System.currentTimeMillis() - time > TIME_EXPIRED) {
                            dbg(ssid + " is expired, last update time: " + time);
                            find = false;
                        }
                    }
                } catch (SQLException e) {
                    try {
                        Log.w(TAG, "Failed to query the blacklist");
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

    public boolean addToBlacklist(HwMSSDatabaseItem item) {
        return addToBlacklist(item.ssid, item.bssid, item.reasoncode);
    }

    public boolean isInBlacklistByBssid(String bssid) {
        return false;
    }

    public List<HwMSSDatabaseItem> getBlacklist(boolean noexpired) {
        return new ArrayList();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0019, code lost:
        return;
     */
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
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001f, code lost:
        return;
     */
    public void delBlacklistAll() {
        dbg("delete all blacklist");
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    this.mDatabase.execSQL("DELETE FROM BlackListTable");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0082, code lost:
        if (r2 != null) goto L_0x0084;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0092, code lost:
        if (r2 == null) goto L_0x0095;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0096, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x009e, code lost:
        return;
     */
    public void dumpBlacklist() {
        dbg("dump blacklist:");
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                Cursor c = null;
                try {
                    c = this.mDatabase.rawQuery("SELECT * FROM BlackListTable", null);
                    while (c != null && c.moveToNext()) {
                        String ssid = c.getString(c.getColumnIndex("ssid"));
                        String bssid = c.getString(c.getColumnIndex("bssid"));
                        int reason = c.getInt(c.getColumnIndex("reason_code"));
                        long time = c.getLong(c.getColumnIndex("update_time"));
                        dbg("ssid:" + ssid + ";bssid:" + hideBssid(bssid) + ";reason_code:" + reason + ";time:" + time);
                    }
                } catch (SQLException e) {
                    try {
                        Log.w(TAG, "Failed to dump the blacklist");
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

    private void inlineDeleteBlacklistBySsid(String ssid) {
        if (ssid != null) {
            this.mDatabase.delete("BlackListTable", "ssid like ?", new String[]{ssid});
        }
    }

    private boolean inlineAddBlacklist(String ssid, String bssid, int reasoncode) {
        try {
            this.mDatabase.execSQL("INSERT INTO BlackListTable VALUES(null, ?, ?, ?, ?, ?, ?)", new Object[]{ssid, bssid, Integer.valueOf(reasoncode), -1, Long.valueOf(System.currentTimeMillis()), 0});
            return true;
        } catch (SQLException e) {
            Log.w(TAG, "Failed to add the blacklist");
            return false;
        }
    }

    private boolean inlineUpdateBlacklist(String ssid, String bssid, int reasoncode) {
        ContentValues values = new ContentValues();
        values.put("bssid", bssid);
        values.put("ssid", ssid);
        values.put("reason_code", Integer.valueOf(reasoncode));
        values.put("update_time", Long.valueOf(System.currentTimeMillis()));
        this.mDatabase.update("BlackListTable", values, "ssid like ?", new String[]{ssid});
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0037, code lost:
        if (r5 != null) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0039, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0047, code lost:
        if (r5 == null) goto L_0x004a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004c, code lost:
        if (r3 < 500) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004e, code lost:
        if (r2 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0050, code lost:
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
        Cursor c = null;
        try {
            c = this.mDatabase.rawQuery("SELECT * FROM BlackListTable", null);
            while (c != null && c.moveToNext()) {
                dataNum++;
                long time = c.getLong(c.getColumnIndex("update_time"));
                String ssid = c.getString(c.getColumnIndex("ssid"));
                if (time < earliestTime) {
                    earliestTime = time;
                    delSsid = ssid;
                }
            }
        } catch (SQLException e) {
            Log.w(TAG, "Failed to check the blacklist");
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002a, code lost:
        if (r1 == null) goto L_0x002d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002d, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x001a, code lost:
        if (r1 != null) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001c, code lost:
        r1.close();
     */
    private boolean inlineIsRecordInDatabase(String ssid) {
        boolean find = false;
        Cursor c = null;
        try {
            c = this.mDatabase.rawQuery("SELECT * FROM BlackListTable where ssid like ?", new String[]{ssid});
            if (c != null && c.moveToNext()) {
                find = true;
            }
        } catch (SQLException e) {
            Log.w(TAG, "Failed to query the blacklist when check records");
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    private String hideBssid(String bssid) {
        if (bssid == null || !bssid.matches("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}")) {
            return "unknown";
        }
        return bssid.substring(0, 6) + "xx:xx" + bssid.substring(11);
    }

    private void dbg(String msg) {
    }
}
