package com.huawei.hwwifiproservice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.wifi.HwHiLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataBaseManager {
    private static final String TAG = "DataBaseManager";
    private SQLiteDatabase mDatabase;
    private DataBaseHelper mHelper;
    private final Object mLock = new Object();
    private WifiManager mWifiManager;

    public DataBaseManager(Context context) {
        HwHiLog.i(MessageUtil.TAG, false, "DataBaseManager()", new Object[0]);
        this.mHelper = new DataBaseHelper(context);
        try {
            this.mDatabase = this.mHelper.getWritableDatabase();
        } catch (SQLiteCantOpenDatabaseException e) {
            HwHiLog.e(MessageUtil.TAG, false, "DataBaseManager(), can't open database!", new Object[0]);
        }
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x008a, code lost:
        if (0 == 0) goto L_0x008d;
     */
    public List<APInfoData> getAllApInfos() {
        synchronized (this.mLock) {
            ArrayList<APInfoData> infos = new ArrayList<>();
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                return infos;
            }
            Cursor cursor = null;
            try {
                cursor = this.mDatabase.rawQuery("SELECT * FROM BSSIDTable", null);
                while (cursor.moveToNext()) {
                    infos.add(new APInfoData(cursor.getString(cursor.getColumnIndex("bssid")), cursor.getString(cursor.getColumnIndex("ssid")), cursor.getInt(cursor.getColumnIndex("inbacklist")), cursor.getInt(cursor.getColumnIndex(HwDualBandMessageUtil.MSG_KEY_AUTHTYPE)), cursor.getLong(cursor.getColumnIndex(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_TIME)), cursor.getInt(cursor.getColumnIndex("isHome")), cursor.getInt(cursor.getColumnIndex(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_FREQUENCY))));
                }
            } catch (IllegalArgumentException e) {
                HwHiLog.e(MessageUtil.TAG, false, "Exception happened in getAllApInfos()", new Object[0]);
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
            cursor.close();
            HwHiLog.i(MessageUtil.TAG, false, "getAllApInfos infos.size()=%{public}d", new Object[]{Integer.valueOf(infos.size())});
            if (infos.size() > 0) {
                Iterator<APInfoData> it = infos.iterator();
                while (it.hasNext()) {
                    APInfoData info = it.next();
                    List<CellInfoData> cellInfos = queryCellInfoByBssid(info.getBssid());
                    if (cellInfos.size() != 0) {
                        info.setCellInfo(cellInfos);
                    }
                    List<String> nearbyApInfosList = getNearbyApInfo(info.getBssid());
                    if (nearbyApInfosList.size() != 0) {
                        info.setNearbyAPInfos(nearbyApInfosList);
                    }
                }
            }
            return infos;
        }
    }

    public List<Integer> queryAvailable5gChannelListByCellId(String cellid) {
        synchronized (this.mLock) {
            HwHiLog.i(TAG, false, "queryAvailable5gChannelListByCellId enter", new Object[0]);
            if (cellid == null) {
                HwHiLog.w(TAG, false, "invalid cellid, ignore queryAvailable5gChannelListByCellId", new Object[0]);
                return new ArrayList();
            }
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    List<String> savedBssidLists = queryBssidByCellid(cellid);
                    if (savedBssidLists != null) {
                        if (!savedBssidLists.isEmpty()) {
                            List<Integer> avaliable5gLists = new ArrayList<>();
                            int count = savedBssidLists.size();
                            Cursor cursor = null;
                            for (int i = 0; i < count; i++) {
                                try {
                                    cursor = this.mDatabase.rawQuery("SELECT * FROM BSSIDTable where bssid like ?", new String[]{savedBssidLists.get(i)});
                                    while (cursor.moveToNext()) {
                                        int frequency = cursor.getInt(cursor.getColumnIndex(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_FREQUENCY));
                                        if (ScanResult.is5GHz(frequency)) {
                                            avaliable5gLists.add(Integer.valueOf(frequency));
                                        }
                                    }
                                } catch (IllegalArgumentException e) {
                                    HwHiLog.e(MessageUtil.TAG, false, "Exception happened in queryAvailable5gChannelListByCellId()", new Object[0]);
                                    if (cursor == null) {
                                    }
                                } catch (Throwable th) {
                                    if (cursor != null) {
                                        cursor.close();
                                    }
                                    throw th;
                                }
                                cursor.close();
                            }
                            return avaliable5gLists;
                        }
                    }
                    return new ArrayList();
                }
            }
            HwHiLog.w(TAG, false, "Database is not opend!, ignore queryAvailable5gChannelListByCellId", new Object[0]);
            return new ArrayList();
        }
    }

    public void addApInfos(String bssid, String ssid, String cellid, int authtype, int frequency) {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    inlineAddBssidIdInfo(bssid, ssid, authtype, frequency);
                    inlineAddCellInfo(bssid, cellid);
                    inlineAddNearbyApInfo(bssid);
                }
            }
        }
    }

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

    public void closeDB() {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    HwHiLog.i(MessageUtil.TAG, false, "closeDB()", new Object[0]);
                    this.mDatabase.close();
                }
            }
        }
    }

    public void addCellInfo(String bssid, String cellid) {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    inlineAddCellInfo(bssid, cellid);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004e, code lost:
        if (0 == 0) goto L_0x0051;
     */
    public List<String> getNearbyApInfo(String bssid) {
        synchronized (this.mLock) {
            List<String> datas = new ArrayList<>();
            if (this.mDatabase == null || !this.mDatabase.isOpen() || bssid == null) {
                return datas;
            }
            Cursor c = null;
            try {
                c = this.mDatabase.rawQuery("SELECT * FROM APInfoTable where bssid like ?", new String[]{bssid});
                while (c.moveToNext()) {
                    String nearbyBssid = c.getString(c.getColumnIndex("nearbybssid"));
                    if (nearbyBssid != null) {
                        datas.add(nearbyBssid);
                    }
                }
            } catch (IllegalArgumentException e) {
                HwHiLog.e(MessageUtil.TAG, false, "getNearbyApInfo Exception", new Object[0]);
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
            c.close();
            return datas;
        }
    }

    public void addNearbyApInfo(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    inlineAddNearbyApInfo(bssid);
                }
            }
        }
    }

    public void updateBssidTimer(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (bssid != null) {
                    long time = System.currentTimeMillis();
                    HwHiLog.i(MessageUtil.TAG, false, "updateBssidTimer time = %{public}s", new Object[]{String.valueOf(time)});
                    ContentValues values = new ContentValues();
                    values.put(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_TIME, Long.valueOf(time));
                    try {
                        this.mDatabase.update(DataBaseHelper.BSSID_TABLE_NAME, values, "bssid like ?", new String[]{bssid});
                    } catch (SQLiteException e) {
                        HwHiLog.e(MessageUtil.TAG, false, "Exception happened in updateBssidTimer()", new Object[0]);
                    }
                }
            }
        }
    }

    public void updateBssidIsInBlackList(String bssid, int inblacklist) {
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (bssid != null) {
                    ContentValues values = new ContentValues();
                    values.put("inbacklist", Integer.valueOf(inblacklist));
                    try {
                        this.mDatabase.update(DataBaseHelper.BSSID_TABLE_NAME, values, "bssid like ?", new String[]{bssid});
                    } catch (SQLiteException e) {
                        HwHiLog.e(MessageUtil.TAG, false, "Exception happened in updateBssidIsInBlackList()", new Object[0]);
                    }
                }
            }
        }
    }

    public void delNearbyApInfo(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen() && bssid != null) {
                try {
                    this.mDatabase.delete(DataBaseHelper.APINFO_TABLE_NAME, "bssid like ?", new String[]{bssid});
                } catch (SQLiteException e) {
                    HwHiLog.e(MessageUtil.TAG, false, "Exception happened in delNearbyApInfo()", new Object[0]);
                }
            }
        }
    }

    public void updateChannel(String bssid, int frequency) {
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (bssid != null) {
                    ContentValues values = new ContentValues();
                    values.put(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_FREQUENCY, Integer.valueOf(frequency));
                    try {
                        this.mDatabase.update(DataBaseHelper.BSSID_TABLE_NAME, values, "bssid like ?", new String[]{bssid});
                    } catch (SQLiteException e) {
                        HwHiLog.e(MessageUtil.TAG, false, "Exception happened in updateChannel()", new Object[0]);
                    }
                }
            }
        }
    }

    public void updateSsid(String bssid, String ssid) {
        synchronized (this.mLock) {
            if (!(this.mDatabase == null || !this.mDatabase.isOpen() || bssid == null)) {
                if (ssid != null) {
                    ContentValues values = new ContentValues();
                    values.put("ssid", ssid);
                    try {
                        this.mDatabase.update(DataBaseHelper.BSSID_TABLE_NAME, values, "bssid like ?", new String[]{bssid});
                    } catch (SQLiteException e) {
                        HwHiLog.e(MessageUtil.TAG, false, "Exception happened in updateSsid()", new Object[0]);
                    }
                }
            }
        }
    }

    public void updateAuthType(String bssid, int authtype) {
        synchronized (this.mLock) {
            if (this.mDatabase != null && this.mDatabase.isOpen()) {
                if (bssid != null) {
                    ContentValues values = new ContentValues();
                    values.put(HwDualBandMessageUtil.MSG_KEY_AUTHTYPE, Integer.valueOf(authtype));
                    try {
                        this.mDatabase.update(DataBaseHelper.BSSID_TABLE_NAME, values, "bssid like ?", new String[]{bssid});
                    } catch (SQLiteException e) {
                        HwHiLog.e(MessageUtil.TAG, false, "Exception happened in updateAuthType()", new Object[0]);
                    }
                }
            }
        }
    }

    private void inlineAddBssidIdInfo(String bssid, String ssid, int authtype, int frequency) {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase != null && sQLiteDatabase.isOpen() && bssid != null && ssid != null) {
            try {
                this.mDatabase.execSQL("INSERT INTO BSSIDTable VALUES(null, ?,?,?,?,?,?,?)", new Object[]{bssid, ssid, 0, Integer.valueOf(authtype), Long.valueOf(System.currentTimeMillis()), 0, Integer.valueOf(frequency)});
            } catch (SQLiteException e) {
                HwHiLog.e(MessageUtil.TAG, false, "Exception happened in inlineAddBssidIdInfo()", new Object[0]);
            }
        }
    }

    private void delBssidInfo(String bssid) {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase != null && sQLiteDatabase.isOpen() && bssid != null) {
            try {
                this.mDatabase.delete(DataBaseHelper.BSSID_TABLE_NAME, "bssid like ?", new String[]{bssid});
            } catch (SQLiteException e) {
                HwHiLog.e(MessageUtil.TAG, false, "Exception happened in delBssidInfo()", new Object[0]);
            }
        }
    }

    private void inlineAddCellInfo(String bssid, String cellid) {
        int rssi = CellStateMonitor.getCellRssi();
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase != null && sQLiteDatabase.isOpen() && bssid != null && cellid != null) {
            try {
                this.mDatabase.execSQL("INSERT INTO CELLIDTable VALUES(null, ?, ?, ?)", new Object[]{bssid, cellid, Integer.valueOf(rssi)});
            } catch (SQLiteException e) {
                HwHiLog.e(MessageUtil.TAG, false, "Exception happened in inlineAddCellInfo()", new Object[0]);
            }
        }
    }

    private void delCellidInfoByBssid(String bssid) {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase != null && sQLiteDatabase.isOpen() && bssid != null) {
            try {
                this.mDatabase.delete(DataBaseHelper.CELLID_TABLE_NAME, "bssid like ?", new String[]{bssid});
            } catch (SQLiteException e) {
                HwHiLog.e(MessageUtil.TAG, false, "Exception happened in delCellidInfoByBssid()", new Object[0]);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0054, code lost:
        if (0 == 0) goto L_0x0057;
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
            } catch (IllegalArgumentException e) {
                HwHiLog.e(MessageUtil.TAG, false, "Exception happened in queryCellInfoByBssid()", new Object[0]);
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
            c.close();
            return datas;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0062, code lost:
        if (r1 != null) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0064, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0074, code lost:
        if (0 == 0) goto L_0x0077;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0078, code lost:
        return r2;
     */
    private List<String> queryBssidByCellid(String cellid) {
        synchronized (this.mLock) {
            HwHiLog.i(TAG, false, "queryBssidByCellid enter", new Object[0]);
            if (cellid == null) {
                HwHiLog.w(TAG, false, "invalid cellid, ignore queryBssidByCellid", new Object[0]);
                return new ArrayList();
            }
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    Cursor cursor = null;
                    List<String> savedBssidLists = new ArrayList<>();
                    try {
                        SQLiteDatabase sQLiteDatabase = this.mDatabase;
                        cursor = sQLiteDatabase.rawQuery("SELECT * FROM CELLIDTable WHERE cellid = " + cellid, null);
                        while (cursor != null && cursor.moveToNext()) {
                            savedBssidLists.add(cursor.getString(cursor.getColumnIndex("bssid")));
                        }
                    } catch (SQLiteException e) {
                        HwHiLog.e(TAG, false, "Exception happened in queryBssidByCellid()", new Object[0]);
                    } catch (Throwable th) {
                        if (0 != 0) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            }
            HwHiLog.w(TAG, false, "Database is not opend!, ignore queryBssidByCellid", new Object[0]);
            return new ArrayList();
        }
    }

    private void inlineAddNearbyApInfo(String bssid) {
        int num = 0;
        List<ScanResult> lists = WifiproUtils.getScanResultsFromWsm();
        if (lists != null) {
            HwHiLog.i(MessageUtil.TAG, false, "addNearbyApInfo lists.size = %{public}d", new Object[]{Integer.valueOf(lists.size())});
            for (ScanResult result : lists) {
                if (num < 20) {
                    addNearbyApInfo(bssid, result.BSSID);
                    num++;
                }
            }
        }
    }

    private void addNearbyApInfo(String bssid, String nearbyBssid) {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        boolean isDbAvailable = sQLiteDatabase == null || !sQLiteDatabase.isOpen();
        boolean isBssidAvailable = bssid == null || nearbyBssid == null;
        if (!isDbAvailable && !isBssidAvailable) {
            try {
                this.mDatabase.execSQL("INSERT INTO APInfoTable VALUES(null, ?, ?)", new Object[]{bssid, nearbyBssid});
            } catch (SQLiteException e) {
                HwHiLog.e(MessageUtil.TAG, false, "Exception happened in addNearbyApInfo()", new Object[0]);
            }
        }
    }
}
