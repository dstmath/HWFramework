package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.android.server.wifi.wifipro.HwDualBandMessageUtil;
import com.android.server.wifi.wifipro.WifiproUtils;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieDataBaseImpl;
import java.util.ArrayList;
import java.util.List;

public class DataBaseManager {
    private static final String TAG = "DataBaseManager";
    private SQLiteDatabase mDatabase;
    private DataBaseHelper mHelper;
    private Object mLock = new Object();
    private WifiManager mWifiManager;

    public DataBaseManager(Context context) {
        Log.e(MessageUtil.TAG, "DataBaseManager()");
        this.mHelper = new DataBaseHelper(context);
        try {
            this.mDatabase = this.mHelper.getWritableDatabase();
        } catch (SQLiteCantOpenDatabaseException e) {
            Log.e(MessageUtil.TAG, "DataBaseManager(), can't open database!");
        }
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x006d, code lost:
        if (r3 != null) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x008c, code lost:
        if (r3 == null) goto L_0x008f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        android.util.Log.e(com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil.TAG, "getAllApInfos infos.size()=" + r1.size());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00ad, code lost:
        if (r1.size() <= 0) goto L_0x00e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00af, code lost:
        r2 = r1.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00b7, code lost:
        if (r2.hasNext() == false) goto L_0x00e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00b9, code lost:
        r4 = r2.next();
        r5 = queryCellInfoByBssid(r4.getBssid());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00cb, code lost:
        if (r5.size() == 0) goto L_0x00d0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00cd, code lost:
        r4.setCellInfo(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00d0, code lost:
        r6 = getNearbyApInfo(r4.getBssid());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00dc, code lost:
        if (r6.size() == 0) goto L_0x00e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00de, code lost:
        r4.setNearbyAPInfos(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00e3, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00eb, code lost:
        return r1;
     */
    public List<APInfoData> getAllApInfos() {
        synchronized (this.mLock) {
            ArrayList<APInfoData> infos = new ArrayList<>();
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                Cursor c = null;
                try {
                    c = this.mDatabase.rawQuery("SELECT * FROM BSSIDTable", null);
                    while (c.moveToNext()) {
                        APInfoData aPInfoData = new APInfoData(c.getString(c.getColumnIndex("bssid")), c.getString(c.getColumnIndex("ssid")), c.getInt(c.getColumnIndex("inbacklist")), c.getInt(c.getColumnIndex(HwDualBandMessageUtil.MSG_KEY_AUTHTYPE)), c.getLong(c.getColumnIndex(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_TIME)), c.getInt(c.getColumnIndex("isHome")));
                        infos.add(aPInfoData);
                    }
                } catch (Exception e) {
                    try {
                        Log.e(MessageUtil.TAG, "getAllApInfos:" + e);
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

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        return;
     */
    public void addApInfos(String bssid, String ssid, String cellid, int authtype) {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    inlineAddBssidIdInfo(bssid, ssid, authtype);
                    inlineAddCellInfo(bssid, cellid);
                    inlineAddNearbyApInfo(bssid);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        return;
     */
    public void delAPInfos(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    delBssidInfo(bssid);
                    delCellidInfoByBssid(bssid);
                    delNearbyApInfo(bssid);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001f, code lost:
        return;
     */
    public void closeDB() {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    Log.e(MessageUtil.TAG, "closeDB()");
                    this.mDatabase.close();
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0016, code lost:
        return;
     */
    public void addCellInfo(String bssid, String cellid) {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    inlineAddCellInfo(bssid, cellid);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003d, code lost:
        if (r2 != null) goto L_0x003f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005c, code lost:
        if (r2 == null) goto L_0x005f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0060, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0068, code lost:
        return r1;
     */
    public List<String> getNearbyApInfo(String bssid) {
        synchronized (this.mLock) {
            List<String> datas = new ArrayList<>();
            if (this.mDatabase != null && this.mDatabase.isOpen() && bssid != null) {
                Cursor c = null;
                try {
                    c = this.mDatabase.rawQuery("SELECT * FROM APInfoTable where bssid like ?", new String[]{bssid});
                    while (c.moveToNext()) {
                        String nearbyBssid = c.getString(c.getColumnIndex("nearbybssid"));
                        if (nearbyBssid != null) {
                            datas.add(nearbyBssid);
                        }
                    }
                } catch (Exception e) {
                    try {
                        Log.e(MessageUtil.TAG, "getNearbyApInfo Exception:" + e);
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

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0016, code lost:
        return;
     */
    public void addNearbyApInfo(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    inlineAddNearbyApInfo(bssid);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0055, code lost:
        return;
     */
    public void updateBssidTimer(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (bssid != null) {
                    long time = System.currentTimeMillis();
                    Log.w(MessageUtil.TAG, "updateBssidTimer time = " + time);
                    ContentValues values = new ContentValues();
                    values.put(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_TIME, Long.valueOf(time));
                    try {
                        this.mDatabase.update(DataBaseHelper.BSSID_TABLE_NAME, values, "bssid like ?", new String[]{bssid});
                    } catch (SQLiteException e) {
                        Log.w(MessageUtil.TAG, "updateBssidTimer, update, SQLiteException");
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003b, code lost:
        return;
     */
    public void updateBssidIsInBlackList(String bssid, int inblacklist) {
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (bssid != null) {
                    ContentValues values = new ContentValues();
                    values.put("inbacklist", Integer.valueOf(inblacklist));
                    try {
                        this.mDatabase.update(DataBaseHelper.BSSID_TABLE_NAME, values, "bssid like ?", new String[]{bssid});
                    } catch (SQLiteException e) {
                        Log.w(MessageUtil.TAG, "updateBssidIsInBlackList, update, SQLiteException");
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003b, code lost:
        return;
     */
    public void updateBssidIsHome(String bssid, int isHome) {
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (bssid != null) {
                    ContentValues values = new ContentValues();
                    values.put("isHome", Integer.valueOf(isHome));
                    try {
                        this.mDatabase.update(DataBaseHelper.BSSID_TABLE_NAME, values, "bssid like ?", new String[]{bssid});
                    } catch (SQLiteException e) {
                        Log.w(MessageUtil.TAG, "updateBssidIsHome, update, SQLiteException");
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002d, code lost:
        return;
     */
    public void delNearbyApInfo(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen() && bssid != null) {
                try {
                    this.mDatabase.delete(DataBaseHelper.APINFO_TABLE_NAME, "bssid like ?", new String[]{bssid});
                } catch (SQLiteException e) {
                    Log.w(MessageUtil.TAG, "delNearbyApInfo, delete, SQLiteException");
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0039, code lost:
        return;
     */
    public void updateSsid(String bssid, String ssid) {
        synchronized (this.mLock) {
            if (!(this.mDatabase == null || !this.mDatabase.isOpen() || bssid == null)) {
                if (ssid != null) {
                    ContentValues values = new ContentValues();
                    values.put("ssid", ssid);
                    try {
                        this.mDatabase.update(DataBaseHelper.BSSID_TABLE_NAME, values, "bssid like ?", new String[]{bssid});
                    } catch (SQLiteException e) {
                        Log.w(MessageUtil.TAG, "updateSsid, update, SQLiteException");
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003b, code lost:
        return;
     */
    public void updateAuthType(String bssid, int authtype) {
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (bssid != null) {
                    ContentValues values = new ContentValues();
                    values.put(HwDualBandMessageUtil.MSG_KEY_AUTHTYPE, Integer.valueOf(authtype));
                    try {
                        this.mDatabase.update(DataBaseHelper.BSSID_TABLE_NAME, values, "bssid like ?", new String[]{bssid});
                    } catch (SQLiteException e) {
                        Log.w(MessageUtil.TAG, "updateAuthType, update, SQLiteException");
                    }
                }
            }
        }
    }

    private void inlineAddBssidIdInfo(String bssid, String ssid, int authtype) {
        if (this.mDatabase != null && this.mDatabase.isOpen() && bssid != null && ssid != null) {
            long time = System.currentTimeMillis();
            try {
                this.mDatabase.execSQL("INSERT INTO BSSIDTable VALUES(null, ?,?,?,?,?,?)", new Object[]{bssid, ssid, 0, Integer.valueOf(authtype), Long.valueOf(time), 0});
            } catch (SQLiteException e) {
                Log.w(MessageUtil.TAG, "inlineAddBssidIdInfo, execSQL, SQLiteException");
            }
        }
    }

    private void delBssidInfo(String bssid) {
        if (this.mDatabase != null && this.mDatabase.isOpen() && bssid != null) {
            try {
                this.mDatabase.delete(DataBaseHelper.BSSID_TABLE_NAME, "bssid like ?", new String[]{bssid});
            } catch (SQLiteException e) {
                Log.w(MessageUtil.TAG, "delBssidInfo, delete, SQLiteException");
            }
        }
    }

    private void inlineAddCellInfo(String bssid, String cellid) {
        int rssi = CellStateMonitor.getCellRssi();
        if (this.mDatabase != null && this.mDatabase.isOpen() && bssid != null && cellid != null) {
            try {
                this.mDatabase.execSQL("INSERT INTO CELLIDTable VALUES(null, ?, ?, ?)", new Object[]{bssid, cellid, Integer.valueOf(rssi)});
            } catch (SQLiteException e) {
                Log.w(MessageUtil.TAG, "inlineAddCellInfo, execSQL, SQLiteException");
            }
        }
    }

    private void delCellidInfoByBssid(String bssid) {
        if (this.mDatabase != null && this.mDatabase.isOpen() && bssid != null) {
            try {
                this.mDatabase.delete(DataBaseHelper.CELLID_TABLE_NAME, "bssid like ?", new String[]{bssid});
            } catch (SQLiteException e) {
                Log.w(MessageUtil.TAG, "delCellidInfoByBssid, delete, SQLiteException");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0043, code lost:
        if (r2 != null) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0062, code lost:
        if (r2 == null) goto L_0x0065;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0066, code lost:
        return r1;
     */
    public List<CellInfoData> queryCellInfoByBssid(String bssid) {
        synchronized (this.mLock) {
            List<CellInfoData> datas = new ArrayList<>();
            if (bssid == null) {
                return datas;
            }
            Cursor c = null;
            try {
                c = this.mDatabase.rawQuery("SELECT * FROM CELLIDTable where bssid like ?", new String[]{bssid});
                while (c.moveToNext()) {
                    String cellid = c.getString(c.getColumnIndex(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_CELLID));
                    int rssi = c.getInt(c.getColumnIndex(HwDualBandMessageUtil.MSG_KEY_RSSI));
                    if (!(cellid == null || rssi == 0)) {
                        datas.add(new CellInfoData(cellid, rssi));
                    }
                }
            } catch (Exception e) {
                try {
                    Log.e(MessageUtil.TAG, "queryCellInfoByBssid:" + e);
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                    throw th;
                }
            }
        }
    }

    private void inlineAddNearbyApInfo(String bssid) {
        int num = 0;
        List<ScanResult> lists = WifiproUtils.getScanResultsFromWsm();
        if (lists != null) {
            Log.w(MessageUtil.TAG, "addNearbyApInfo lists.size = " + lists.size());
            for (ScanResult result : lists) {
                if (num < 20) {
                    addNearbyApInfo(bssid, result.BSSID);
                    num++;
                }
            }
        }
    }

    private void addNearbyApInfo(String bssid, String nearbyBssid) {
        if (this.mDatabase != null && this.mDatabase.isOpen() && bssid != null && nearbyBssid != null) {
            try {
                this.mDatabase.execSQL("INSERT INTO APInfoTable VALUES(null, ?, ?)", new Object[]{bssid, nearbyBssid});
            } catch (SQLiteException e) {
                Log.w(MessageUtil.TAG, "addNearbyApInfo, execSQL, SQLiteException");
            }
        }
    }
}
