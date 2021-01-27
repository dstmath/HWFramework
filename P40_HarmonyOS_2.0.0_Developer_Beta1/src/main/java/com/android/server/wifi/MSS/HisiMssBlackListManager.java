package com.android.server.wifi.MSS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HisiMssBlackListManager implements IHwMssBlacklistMgr {
    private static final int DATA_BASE_MAX_NUM = 64;
    private static final boolean DEBUG = false;
    private static final String TAG = "HisiMssBlackListManager";
    private static final long TIME_EXPIRED = 86400000;
    private static final int TYPE_QUERY_BY_BSSID = 1;
    private static final int TYPE_QUERY_BY_SSID = 0;
    private static HisiMssBlackListManager sHisiMssBlackListManager = null;
    private SQLiteDatabase mDatabase;
    private HisiMssBlackListHelper mHelper;
    private final Object mLock = new Object();

    private HisiMssBlackListManager(Context context) {
        try {
            this.mHelper = new HisiMssBlackListHelper(context);
            this.mDatabase = this.mHelper.getWritableDatabase();
        } catch (SQLiteCantOpenDatabaseException e) {
            this.mDatabase = null;
            HwHiLog.e(TAG, false, "SQLiteDatabase is null, error!", new Object[0]);
        }
    }

    public static synchronized IHwMssBlacklistMgr getInstance(Context context) {
        HisiMssBlackListManager hisiMssBlackListManager;
        synchronized (HisiMssBlackListManager.class) {
            if (sHisiMssBlackListManager == null) {
                sHisiMssBlackListManager = new HisiMssBlackListManager(context);
            }
            hisiMssBlackListManager = sHisiMssBlackListManager;
        }
        return hisiMssBlackListManager;
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

    @Override // com.android.server.wifi.MSS.IHwMssBlacklistMgr
    public boolean addToBlacklist(HwMssDatabaseItem item) {
        if (item != null) {
            return addToBlacklist(item.ssid, item.bssid, item.reasonCode);
        }
        return false;
    }

    @Override // com.android.server.wifi.MSS.IHwMssBlacklistMgr
    public boolean addToBlacklist(String ssid, String bssid, int actiontype) {
        HwMssUtils.logD(TAG, false, "Try to add to blacklist. ssid:%{public}s; bssid:%{private}s; actiontype: %{public}d", StringUtilEx.safeDisplaySsid(ssid), StringUtilEx.safeDisplayBssid(bssid), Integer.valueOf(actiontype));
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (bssid != null) {
                    if (!innerIsRecordInDatabase(1, bssid, false)) {
                        innerCheckIfDatabaseFull();
                        return innerAddBlacklist(ssid, bssid, actiontype);
                    }
                    dbg(false, "%{private}s is already in blacklist, update it.", StringUtilEx.safeDisplayBssid(bssid));
                    return innerUpdateBlacklist(ssid, bssid, actiontype);
                }
            }
            dbg(false, "Failed to add to blacklist. bssid: %{private}s", StringUtilEx.safeDisplayBssid(bssid));
            return false;
        }
    }

    @Override // com.android.server.wifi.MSS.IHwMssBlacklistMgr
    public boolean isInBlacklist(String ssid) {
        return innerIsRecordInDatabase(0, ssid, true);
    }

    @Override // com.android.server.wifi.MSS.IHwMssBlacklistMgr
    public boolean isInBlacklistByBssid(String bssid) {
        return innerIsRecordInDatabase(1, bssid, true);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0092, code lost:
        if (r4 != null) goto L_0x0094;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0094, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00a4, code lost:
        if (0 == 0) goto L_0x00a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00a8, code lost:
        return r0;
     */
    @Override // com.android.server.wifi.MSS.IHwMssBlacklistMgr
    public List<HwMssDatabaseItem> getBlacklist(boolean isValid) {
        ArrayList<HwMssDatabaseItem> lists = new ArrayList<>();
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    Cursor cursor = null;
                    long curr = System.currentTimeMillis();
                    try {
                        cursor = this.mDatabase.rawQuery("SELECT * FROM BlackListTable", null);
                        while (cursor != null && cursor.moveToNext()) {
                            String ssid = cursor.getString(cursor.getColumnIndex("ssid"));
                            String bssid = cursor.getString(cursor.getColumnIndex("bssid"));
                            int actiontype = cursor.getInt(cursor.getColumnIndex("action_type"));
                            long time = cursor.getLong(cursor.getColumnIndex("update_time"));
                            dbg(false, "ssid:%{public}s;bssid:%{private}s;action_type:%{public}d;time:%{public}s", StringUtilEx.safeDisplaySsid(ssid), StringUtilEx.safeDisplayBssid(bssid), Integer.valueOf(actiontype), String.valueOf(time));
                            if (!isValid || curr - time < TIME_EXPIRED) {
                                HwMssDatabaseItem item = new HwMssDatabaseItem(ssid, bssid, actiontype);
                                item.updateTime = time;
                                lists.add(item);
                            }
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
            return lists;
        }
    }

    /* access modifiers changed from: protected */
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
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0078, code lost:
        if (r2 != null) goto L_0x007a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x007a, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x008a, code lost:
        if (0 == 0) goto L_0x008d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x008e, code lost:
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
                        dbg(false, "ssid:%{public}s;bssid:%{private}s;action_type:%{public}d;time:%{public}s", StringUtilEx.safeDisplaySsid(cursor.getString(cursor.getColumnIndex("ssid"))), StringUtilEx.safeDisplayBssid(cursor.getString(cursor.getColumnIndex("bssid"))), Integer.valueOf(cursor.getInt(cursor.getColumnIndex("action_type"))), String.valueOf(cursor.getLong(cursor.getColumnIndex("update_time"))));
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

    /* JADX WARNING: Removed duplicated region for block: B:32:0x0067  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x006e  */
    private boolean innerIsRecordInDatabase(int type, String value, boolean isValid) {
        Throwable th;
        boolean isFind = false;
        String strQuery = String.format(Locale.ENGLISH, "SELECT * FROM %s where %s like ?", "BlackListTable", type == 0 ? "ssid" : "bssid");
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (value != null) {
                    Cursor cursor = null;
                    long curr = System.currentTimeMillis();
                    try {
                        Cursor cursor2 = this.mDatabase.rawQuery(strQuery, new String[]{value});
                        try {
                            isFind = isFindRecord(value, isValid, false, cursor2, curr);
                            if (cursor2 != null) {
                                cursor2.close();
                            }
                        } catch (SQLException e) {
                            cursor = cursor2;
                            try {
                                HwHiLog.w(TAG, false, "Failed to query the blacklist", new Object[0]);
                                if (cursor != null) {
                                    cursor.close();
                                }
                                return isFind;
                            } catch (Throwable th2) {
                                th = th2;
                                if (cursor != null) {
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            cursor = cursor2;
                            if (cursor != null) {
                                cursor.close();
                            }
                            throw th;
                        }
                    } catch (SQLException e2) {
                        HwHiLog.w(TAG, false, "Failed to query the blacklist", new Object[0]);
                        if (cursor != null) {
                        }
                        return isFind;
                    }
                    return isFind;
                }
            }
            return false;
        }
    }

    private boolean isFindRecord(String value, boolean isValid, boolean isFind, Cursor cursor, long curr) {
        while (cursor != null && cursor.moveToNext()) {
            if (isValid) {
                long time = cursor.getLong(cursor.getColumnIndex("update_time"));
                if (curr - time > TIME_EXPIRED) {
                    dbg(false, value + " is expired, last update time: %{public}s", String.valueOf(time));
                }
            }
            return true;
        }
        return isFind;
    }

    private void innerDeleteBlacklistByBssid(String bssid) {
        if (!TextUtils.isEmpty(bssid)) {
            this.mDatabase.delete("BlackListTable", "bssid like ?", new String[]{bssid});
        }
    }

    private boolean innerAddBlacklist(String ssid, String bssid, int actiontype) {
        try {
            this.mDatabase.execSQL(String.format(Locale.ENGLISH, "INSERT INTO %s VALUES(null, ?, ?, ?, ?, ?, ?)", "BlackListTable"), new Object[]{ssid, bssid, Integer.valueOf(actiontype), -1, Long.valueOf(System.currentTimeMillis()), 0});
            return true;
        } catch (SQLException e) {
            HwHiLog.w(TAG, false, "Failed to add the blacklist", new Object[0]);
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
        if (r4 != null) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0039, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004a, code lost:
        if (0 == 0) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004f, code lost:
        if (r3 < 64) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0051, code lost:
        if (r2 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0053, code lost:
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
        Cursor cursor = null;
        try {
            cursor = this.mDatabase.rawQuery("SELECT * FROM BlackListTable", null);
            while (cursor != null && cursor.moveToNext()) {
                dataNum++;
                long time = cursor.getLong(cursor.getColumnIndex("update_time"));
                String bssid = cursor.getString(cursor.getColumnIndex("bssid"));
                if (time < earliestTime) {
                    earliestTime = time;
                    delBssid = bssid;
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

    private void dbg(boolean isFmtStrPrivate, String msg, Object... args) {
    }

    static class HisiMssBlackListHelper extends SQLiteOpenHelper {
        public static final String BLACKLIST_TABLE_NAME = "BlackListTable";
        private static final String DATABASE_NAME = "/data/system/HisiMSSBlackList.db";
        private static final int DATABASE_VERSION = 1;

        public HisiMssBlackListHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public HisiMssBlackListHelper(Context context) {
            super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 1);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase db) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("CREATE TABLE [");
            buffer.append("BlackListTable");
            buffer.append("] (");
            buffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
            buffer.append("[ssid] TEXT,");
            buffer.append("[bssid] TEXT,");
            buffer.append("[action_type] INTEGER,");
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
