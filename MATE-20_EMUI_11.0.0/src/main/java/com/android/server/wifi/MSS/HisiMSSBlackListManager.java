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
    private final Object mLock = new Object();

    private HisiMSSBlackListManager(Context context) {
        try {
            this.mHelper = new HisiMSSBlackListHelper(context);
            this.mDatabase = this.mHelper.getWritableDatabase();
        } catch (SQLiteCantOpenDatabaseException e) {
            this.mDatabase = null;
            HwHiLog.e(TAG, false, "SQLiteDatabase is null, error!", new Object[0]);
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

    @Override // com.android.server.wifi.MSS.IHwMSSBlacklistMgr
    public void closeDB() {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    this.mDatabase.close();
                }
            }
        }
    }

    @Override // com.android.server.wifi.MSS.IHwMSSBlacklistMgr
    public boolean addToBlacklist(HwMSSDatabaseItem item) {
        if (item != null) {
            return addToBlacklist(item.ssid, item.bssid, item.reasoncode);
        }
        return false;
    }

    @Override // com.android.server.wifi.MSS.IHwMSSBlacklistMgr
    public boolean addToBlacklist(String ssid, String bssid, int actiontype) {
        HwMSSUtils.logd(TAG, false, "Try to add to blacklist. ssid:%{public}s; bssid:%{private}s; actiontype: %{public}d", StringUtilEx.safeDisplaySsid(ssid), StringUtilEx.safeDisplayBssid(bssid), Integer.valueOf(actiontype));
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

    @Override // com.android.server.wifi.MSS.IHwMSSBlacklistMgr
    public boolean isInBlacklist(String ssid) {
        return innerIsRecordInDatabase(0, ssid, true);
    }

    @Override // com.android.server.wifi.MSS.IHwMSSBlacklistMgr
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
    @Override // com.android.server.wifi.MSS.IHwMSSBlacklistMgr
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
                            dbg(false, "ssid:%{public}s;bssid:%{private}s;action_type:%{public}d;time:%{public}s", StringUtilEx.safeDisplaySsid(ssid), StringUtilEx.safeDisplayBssid(bssid), Integer.valueOf(actiontype), String.valueOf(time));
                            if (!noexpired || curr - time < TIME_EXPIRED) {
                                HwMSSDatabaseItem item = new HwMSSDatabaseItem(ssid, bssid, actiontype);
                                item.updatetime = time;
                                lists.add(item);
                            }
                        }
                    } catch (SQLException e) {
                        HwHiLog.w(TAG, false, "Failed to dump the blacklist", new Object[0]);
                    } catch (Throwable th) {
                        if (0 != 0) {
                            c.close();
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
                Cursor c = null;
                try {
                    c = this.mDatabase.rawQuery("SELECT * FROM BlackListTable", null);
                    while (c != null && c.moveToNext()) {
                        dbg(false, "ssid:%{public}s;bssid:%{private}s;action_type:%{public}d;time:%{public}s", StringUtilEx.safeDisplaySsid(c.getString(c.getColumnIndex("ssid"))), StringUtilEx.safeDisplayBssid(c.getString(c.getColumnIndex("bssid"))), Integer.valueOf(c.getInt(c.getColumnIndex("action_type"))), String.valueOf(c.getLong(c.getColumnIndex("update_time"))));
                    }
                } catch (SQLException e) {
                    HwHiLog.w(TAG, false, "Failed to dump the blacklist", new Object[0]);
                } catch (Throwable th) {
                    if (0 != 0) {
                        c.close();
                    }
                    throw th;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0081, code lost:
        if (r9 != null) goto L_0x0083;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0083, code lost:
        r9.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0093, code lost:
        if (0 == 0) goto L_0x0096;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0097, code lost:
        return r3;
     */
    private boolean innerIsRecordInDatabase(int type, String value, boolean noexpired) {
        boolean find = false;
        String strQuery = String.format(Locale.US, "SELECT * FROM %s where %s like ?", "BlackListTable", type == 0 ? "ssid" : "bssid");
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (value != null) {
                    Cursor c = null;
                    long curr = System.currentTimeMillis();
                    try {
                        c = this.mDatabase.rawQuery(strQuery, new String[]{value});
                        while (true) {
                            if (c != null && c.moveToNext()) {
                                if (!noexpired) {
                                    break;
                                }
                                long time = c.getLong(c.getColumnIndex("update_time"));
                                if (curr - time <= TIME_EXPIRED) {
                                    break;
                                }
                                dbg(false, value + " is expired, last update time: %{public}s", String.valueOf(time));
                            } else {
                                break;
                            }
                        }
                        find = true;
                    } catch (SQLException e) {
                        HwHiLog.w(TAG, false, "Failed to query the blacklist", new Object[0]);
                    } catch (Throwable th) {
                        if (0 != 0) {
                            c.close();
                        }
                        throw th;
                    }
                }
            }
            return false;
        }
    }

    private void innerDeleteBlacklistByBssid(String bssid) {
        if (!TextUtils.isEmpty(bssid)) {
            this.mDatabase.delete("BlackListTable", "bssid like ?", new String[]{bssid});
        }
    }

    private boolean innerAddBlacklist(String ssid, String bssid, int actiontype) {
        try {
            this.mDatabase.execSQL(String.format(Locale.US, "INSERT INTO %s VALUES(null, ?, ?, ?, ?, ?, ?)", "BlackListTable"), new Object[]{ssid, bssid, Integer.valueOf(actiontype), -1, Long.valueOf(System.currentTimeMillis()), 0});
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
            HwHiLog.w(TAG, false, "Failed to check the blacklist", new Object[0]);
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    private void dbg(boolean isFmtStrPrivate, String msg, Object... args) {
    }

    static class HisiMSSBlackListHelper extends SQLiteOpenHelper {
        public static final String BLACKLIST_TABLE_NAME = "BlackListTable";
        private static final String DATABASE_NAME = "/data/system/HisiMSSBlackList.db";
        private static final int DATABASE_VERSION = 1;

        public HisiMSSBlackListHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public HisiMSSBlackListHelper(Context context) {
            super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 1);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
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

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS BlackListTable");
            onCreate(db);
        }
    }
}
