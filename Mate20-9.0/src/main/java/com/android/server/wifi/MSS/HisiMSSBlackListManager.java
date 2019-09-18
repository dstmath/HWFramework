package com.android.server.wifi.MSS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HisiMSSBlackListManager implements IHwMSSBlacklistMgr {
    private static final int DATA_BASE_MAX_NUM = 64;
    private static final boolean DEBUG = false;
    private static final String TAG = "HisiMSSBlackListManager";
    private static final long TIME_EXPIRED = 86400000;
    private static final int TYPE_QUERY_BY_BSSID = 1;
    private static final int TYPE_QUERY_BY_SSID = 0;
    private static HisiMSSBlackListManager mHisiMSSBlackListManager = null;
    private SQLiteDatabase mDatabase;
    private HisiMSSBlackListHelper mHelper;
    private Object mLock = new Object();

    static class HisiMSSBlackListHelper extends SQLiteOpenHelper {
        public static final String BLACKLIST_TABLE_NAME = "BlackListTable";
        private static final String DATABASE_NAME = "/data/system/HisiMSSBlackList.db";
        private static final int DATABASE_VERSION = 1;

        public HisiMSSBlackListHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public HisiMSSBlackListHelper(Context context) {
            super(context, DATABASE_NAME, null, 1);
        }

        public void onCreate(SQLiteDatabase db) {
            StringBuffer sBuffer = new StringBuffer();
            sBuffer.append("CREATE TABLE [");
            sBuffer.append("BlackListTable");
            sBuffer.append("] (");
            sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
            sBuffer.append("[ssid] TEXT,");
            sBuffer.append("[bssid] TEXT,");
            sBuffer.append("[action_type] INTEGER,");
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

    private HisiMSSBlackListManager(Context context) {
        try {
            this.mHelper = new HisiMSSBlackListHelper(context);
            this.mDatabase = this.mHelper.getWritableDatabase();
        } catch (SQLiteCantOpenDatabaseException e) {
            this.mDatabase = null;
            Log.e(TAG, "SQLiteDatabase is null, error!");
        }
    }

    public static synchronized IHwMSSBlacklistMgr getInstance(Context context) {
        HisiMSSBlackListManager hisiMSSBlackListManager;
        synchronized (HisiMSSBlackListManager.class) {
            if (mHisiMSSBlackListManager == null) {
                mHisiMSSBlackListManager = new HisiMSSBlackListManager(context);
            }
            hisiMSSBlackListManager = mHisiMSSBlackListManager;
        }
        return hisiMSSBlackListManager;
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

    public boolean addToBlacklist(HwMSSDatabaseItem item) {
        if (item != null) {
            return addToBlacklist(item.ssid, item.bssid, item.reasoncode);
        }
        return false;
    }

    public boolean addToBlacklist(String ssid, String bssid, int actiontype) {
        HwMSSUtils.logd(TAG, "Try to add to blacklist. ssid:" + ssid + "; bssid:" + hideBssid(bssid) + "; actiontype: " + actiontype);
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (bssid != null) {
                    if (!innerIsRecordInDatabase(1, bssid, false)) {
                        innerCheckIfDatabaseFull();
                        boolean innerAddBlacklist = innerAddBlacklist(ssid, bssid, actiontype);
                        return innerAddBlacklist;
                    }
                    dbg(bssid + " is already in blacklist, update it.");
                    boolean innerUpdateBlacklist = innerUpdateBlacklist(ssid, bssid, actiontype);
                    return innerUpdateBlacklist;
                }
            }
            dbg("Failed to add to blacklist. bssid: " + bssid);
            return false;
        }
    }

    public boolean isInBlacklist(String ssid) {
        return innerIsRecordInDatabase(0, ssid, true);
    }

    public boolean isInBlacklistByBssid(String bssid) {
        return innerIsRecordInDatabase(1, bssid, true);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x009b, code lost:
        if (r2 != null) goto L_0x009d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00ab, code lost:
        if (r2 == null) goto L_0x00ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00af, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b7, code lost:
        return r0;
     */
    public List<HwMSSDatabaseItem> getBlacklist(boolean noexpired) {
        ArrayList<HwMSSDatabaseItem> lists = new ArrayList<>();
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    Cursor c = null;
                    long curr = System.currentTimeMillis();
                    try {
                        c = this.mDatabase.rawQuery("SELECT * FROM BlackListTable", null);
                        while (c != null && c.moveToNext()) {
                            String ssid = c.getString(c.getColumnIndex("ssid"));
                            String bssid = c.getString(c.getColumnIndex("bssid"));
                            int actiontype = c.getInt(c.getColumnIndex("action_type"));
                            long time = c.getLong(c.getColumnIndex("update_time"));
                            dbg("ssid:" + ssid + ";bssid:" + hideBssid(bssid) + ";action_type:" + actiontype + ";time:" + time);
                            if (!noexpired || curr - time < TIME_EXPIRED) {
                                HwMSSDatabaseItem item = new HwMSSDatabaseItem(ssid, bssid, actiontype);
                                item.updatetime = time;
                                lists.add(item);
                            }
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
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0016, code lost:
        return;
     */
    public void delBlacklistByBssid(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    innerDeleteBlacklistByBssid(bssid);
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
                        int actiontype = c.getInt(c.getColumnIndex("action_type"));
                        long time = c.getLong(c.getColumnIndex("update_time"));
                        dbg("ssid:" + ssid + ";bssid:" + hideBssid(bssid) + ";action_type:" + actiontype + ";time:" + time);
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

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007c, code lost:
        if (r7 != null) goto L_0x007e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x008c, code lost:
        if (r7 == null) goto L_0x008f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0090, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0098, code lost:
        return false;
     */
    private boolean innerIsRecordInDatabase(int type, String value, boolean noexpired) {
        String str = value;
        boolean find = false;
        String strQuery = String.format(Locale.US, "SELECT * FROM %s where %s like ?", new Object[]{"BlackListTable", type == 0 ? "ssid" : "bssid"});
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (str != null) {
                    Cursor c = null;
                    long curr = System.currentTimeMillis();
                    try {
                        c = this.mDatabase.rawQuery(strQuery, new String[]{str});
                        while (true) {
                            if (c != null && c.moveToNext()) {
                                if (!noexpired) {
                                    break;
                                }
                                if (curr - c.getLong(c.getColumnIndex("update_time")) <= TIME_EXPIRED) {
                                    break;
                                }
                                dbg(str + " is expired, last update time: " + time);
                            } else {
                                break;
                            }
                        }
                        find = true;
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
    }

    private void innerDeleteBlacklistByBssid(String bssid) {
        if (!TextUtils.isEmpty(bssid)) {
            this.mDatabase.delete("BlackListTable", "bssid like ?", new String[]{bssid});
        }
    }

    private boolean innerAddBlacklist(String ssid, String bssid, int actiontype) {
        String strcmd = String.format(Locale.US, "INSERT INTO %s VALUES(null, ?, ?, ?, ?, ?, ?)", new Object[]{"BlackListTable"});
        try {
            this.mDatabase.execSQL(strcmd, new Object[]{ssid, bssid, Integer.valueOf(actiontype), -1, Long.valueOf(System.currentTimeMillis()), 0});
            return true;
        } catch (SQLException e) {
            Log.w(TAG, "Failed to add the blacklist");
            return false;
        }
    }

    private boolean innerUpdateBlacklist(String ssid, String bssid, int actiontype) {
        ContentValues values = new ContentValues();
        values.put("bssid", bssid);
        values.put("ssid", ssid);
        values.put("action_type", Integer.valueOf(actiontype));
        values.put("update_time", Long.valueOf(System.currentTimeMillis()));
        this.mDatabase.update("BlackListTable", values, "bssid like ?", new String[]{bssid});
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
        if (r3 < DATA_BASE_MAX_NUM) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004e, code lost:
        if (r2 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0050, code lost:
        innerDeleteBlacklistByBssid(r2);
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
    private void innerCheckIfDatabaseFull() {
        long earliestTime = Long.MAX_VALUE;
        String delBssid = null;
        int dataNum = 0;
        Cursor c = null;
        try {
            c = this.mDatabase.rawQuery("SELECT * FROM BlackListTable", null);
            while (c != null && c.moveToNext()) {
                dataNum++;
                long time = c.getLong(c.getColumnIndex("update_time"));
                String bssid = c.getString(c.getColumnIndex("bssid"));
                if (time < earliestTime) {
                    earliestTime = time;
                    delBssid = bssid;
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

    private String hideBssid(String bssid) {
        if (bssid == null || !bssid.matches("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}")) {
            return "unknown";
        }
        return bssid.substring(0, 6) + "xx:xx" + bssid.substring(11);
    }

    private void dbg(String msg) {
    }
}
