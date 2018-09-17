package com.android.server.wifi.MSS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class HwMSSBlackListManager {
    private static final int DATA_BASE_MAX_NUM = 500;
    private static final boolean DEBUG = false;
    private static final String TAG = "HwMSSBlackListManager";
    private static final long TIME_EXPIRED = 86400000;
    private static HwMSSBlackListManager mHwMSSBlackListManager = null;
    private final SQLiteDatabase mDatabase;
    private final HwMSSBlackListHelper mHelper;
    private Object mLock = new Object();

    class HwMSSBlackListHelper extends SQLiteOpenHelper {
        public static final String BLACKLIST_TABLE_NAME = "BlackListTable";
        private static final String DATABASE_NAME = "/data/system/HwMSSBlackList.db";
        private static final int DATABASE_VERSION = 1;

        public HwMSSBlackListHelper(Context context, String name, CursorFactory factory, int version) {
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
        this.mHelper = new HwMSSBlackListHelper(context);
        this.mDatabase = this.mHelper.getWritableDatabase();
    }

    public static synchronized HwMSSBlackListManager getInstance(Context context) {
        HwMSSBlackListManager hwMSSBlackListManager;
        synchronized (HwMSSBlackListManager.class) {
            if (mHwMSSBlackListManager == null) {
                mHwMSSBlackListManager = new HwMSSBlackListManager(context);
            }
            hwMSSBlackListManager = mHwMSSBlackListManager;
        }
        return hwMSSBlackListManager;
    }

    /* JADX WARNING: Missing block: B:9:0x0012, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void closeDB() {
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
            } else {
                this.mDatabase.close();
            }
        }
    }

    public boolean addToBlacklist(String ssid, String bssid, int reasoncode) {
        dbg("Try to add to blacklist. ssid:" + ssid + "; bssid:" + hideBssid(bssid) + "; reasoncode: " + reasoncode);
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || ssid == null) {
                dbg("Failed to add to blacklist. ssid: " + ssid);
                return false;
            }
            bssid = hideBssid(bssid);
            boolean inlineUpdateBlacklist;
            if (inlineIsRecordInDatabase(ssid)) {
                dbg(ssid + " is already in blacklist, update it.");
                inlineUpdateBlacklist = inlineUpdateBlacklist(ssid, bssid, reasoncode);
                return inlineUpdateBlacklist;
            }
            inlineCheckIfDatabaseFull();
            inlineUpdateBlacklist = inlineAddBlacklist(ssid, bssid, reasoncode);
            return inlineUpdateBlacklist;
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0016, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isInBlacklist(String ssid) {
        boolean find = false;
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || ssid == null) {
            } else {
                Cursor cursor = null;
                try {
                    cursor = this.mDatabase.rawQuery("SELECT * FROM BlackListTable where ssid like ?", new String[]{ssid});
                    if (cursor != null && cursor.moveToNext()) {
                        find = true;
                    }
                    if (find) {
                        long time = cursor.getLong(cursor.getColumnIndex("update_time"));
                        if (System.currentTimeMillis() - time > TIME_EXPIRED) {
                            dbg(ssid + " is expired, last update time: " + time);
                            find = false;
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (SQLException e) {
                    Log.w(TAG, "Failed to query the blacklist");
                    if (cursor != null) {
                        cursor.close();
                    }
                    return find;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0015, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void delBlacklistBySsid(String ssid) {
        if (ssid != null) {
            synchronized (this.mLock) {
                if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                } else {
                    inlineDeleteBlacklistBySsid(ssid);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0018, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void delBlacklistAll() {
        dbg("delete all blacklist");
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
            } else {
                this.mDatabase.execSQL("DELETE FROM BlackListTable");
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0018, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void dumpBlacklist() {
        dbg("dump blacklist:");
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
            } else {
                Cursor cursor = null;
                try {
                    cursor = this.mDatabase.rawQuery("SELECT * FROM BlackListTable", null);
                    while (cursor != null && cursor.moveToNext()) {
                        String ssid = cursor.getString(cursor.getColumnIndex("ssid"));
                        String bssid = cursor.getString(cursor.getColumnIndex("bssid"));
                        int reason = cursor.getInt(cursor.getColumnIndex("reason_code"));
                        dbg("ssid:" + ssid + ";bssid:" + hideBssid(bssid) + ";reason_code:" + reason + ";time:" + cursor.getLong(cursor.getColumnIndex("update_time")));
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (SQLException e) {
                    Log.w(TAG, "Failed to dump the blacklist");
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
    }

    private void inlineDeleteBlacklistBySsid(String ssid) {
        if (ssid != null) {
            this.mDatabase.delete(HwMSSBlackListHelper.BLACKLIST_TABLE_NAME, "ssid like ?", new String[]{ssid});
        }
    }

    private boolean inlineAddBlacklist(String ssid, String bssid, int reasoncode) {
        try {
            this.mDatabase.execSQL("INSERT INTO BlackListTable VALUES(null, ?, ?, ?, ?, ?, ?)", new Object[]{ssid, bssid, Integer.valueOf(reasoncode), Integer.valueOf(-1), Long.valueOf(System.currentTimeMillis()), Integer.valueOf(0)});
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
        this.mDatabase.update(HwMSSBlackListHelper.BLACKLIST_TABLE_NAME, values, "ssid like ?", new String[]{ssid});
        return true;
    }

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
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            Log.w(TAG, "Failed to check the blacklist");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (dataNum >= 500 && delSsid != null) {
            inlineDeleteBlacklistBySsid(delSsid);
        }
    }

    private boolean inlineIsRecordInDatabase(String ssid) {
        boolean find = false;
        Cursor cursor = null;
        try {
            cursor = this.mDatabase.rawQuery("SELECT * FROM BlackListTable where ssid like ?", new String[]{ssid});
            if (cursor != null && cursor.moveToNext()) {
                find = true;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            Log.w(TAG, "Failed to query the blacklist when check records");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return find;
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
