package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
        this.mDatabase = this.mHelper.getWritableDatabase();
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
    }

    /* JADX WARNING: Missing block: B:9:0x0017, code:
            return r12;
     */
    /* JADX WARNING: Missing block: B:46:0x00f1, code:
            return r12;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<APInfoData> getAllApInfos() {
        synchronized (this.mLock) {
            ArrayList<APInfoData> infos = new ArrayList();
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
            } else {
                Cursor cursor = null;
                try {
                    cursor = this.mDatabase.rawQuery("SELECT * FROM BSSIDTable", null);
                    while (cursor.moveToNext()) {
                        infos.add(new APInfoData(cursor.getString(cursor.getColumnIndex("bssid")), cursor.getString(cursor.getColumnIndex("ssid")), cursor.getInt(cursor.getColumnIndex("inbacklist")), cursor.getInt(cursor.getColumnIndex(HwDualBandMessageUtil.MSG_KEY_AUTHTYPE)), cursor.getLong(cursor.getColumnIndex(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_TIME))));
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    Log.e(MessageUtil.TAG, "getAllApInfos:" + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                Log.e(MessageUtil.TAG, "getAllApInfos infos.size()=" + infos.size());
                if (infos.size() > 0) {
                    for (APInfoData info : infos) {
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
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0012, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addApInfos(String bssid, String ssid, String cellid, int authtype) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
            } else {
                inlineAddBssidIdInfo(bssid, ssid, authtype);
                inlineAddCellInfo(bssid, cellid);
                inlineAddNearbyApInfo(bssid);
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0012, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void delAPInfos(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
            } else {
                delBssidInfo(bssid);
                delCellidInfoByBssid(bssid);
                delNearbyApInfo(bssid);
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0012, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void closeDB() {
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
            } else {
                Log.e(MessageUtil.TAG, "closeDB()");
                this.mDatabase.close();
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0012, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addCellInfo(String bssid, String cellid) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
            } else {
                inlineAddCellInfo(bssid, cellid);
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0019, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<String> getNearbyApInfo(String bssid) {
        synchronized (this.mLock) {
            List<String> datas = new ArrayList();
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || bssid == null) {
            } else {
                Cursor cursor = null;
                try {
                    cursor = this.mDatabase.rawQuery("SELECT * FROM APInfoTable where bssid like ?", new String[]{bssid});
                    while (cursor.moveToNext()) {
                        String nearbyBssid = cursor.getString(cursor.getColumnIndex("nearbybssid"));
                        if (nearbyBssid != null) {
                            datas.add(nearbyBssid);
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    Log.e(MessageUtil.TAG, "getNearbyApInfo Exception:" + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return datas;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0012, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addNearbyApInfo(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
            } else {
                inlineAddNearbyApInfo(bssid);
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0014, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateBssidTimer(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || bssid == null) {
            } else {
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

    /* JADX WARNING: Missing block: B:10:0x0014, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateBssidIsInBlackList(String bssid, int inblacklist) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || bssid == null) {
            } else {
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

    /* JADX WARNING: Missing block: B:10:0x0014, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void delNearbyApInfo(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || bssid == null) {
            } else {
                try {
                    this.mDatabase.delete(DataBaseHelper.APINFO_TABLE_NAME, "bssid like ?", new String[]{bssid});
                } catch (SQLiteException e) {
                    Log.w(MessageUtil.TAG, "delNearbyApInfo, delete, SQLiteException");
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0014, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateSsid(String bssid, String ssid) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || bssid == null || ssid == null) {
            } else {
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

    /* JADX WARNING: Missing block: B:10:0x0014, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateAuthType(String bssid, int authtype) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || bssid == null) {
            } else {
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

    private void inlineAddBssidIdInfo(String bssid, String ssid, int authtype) {
        if (this.mDatabase != null && (this.mDatabase.isOpen() ^ 1) == 0 && bssid != null && ssid != null) {
            long time = System.currentTimeMillis();
            try {
                this.mDatabase.execSQL("INSERT INTO BSSIDTable VALUES(null, ?,?,?,?,?)", new Object[]{bssid, ssid, Integer.valueOf(0), Integer.valueOf(authtype), Long.valueOf(time)});
            } catch (SQLiteException e) {
                Log.w(MessageUtil.TAG, "inlineAddBssidIdInfo, execSQL, SQLiteException");
            }
        }
    }

    private void delBssidInfo(String bssid) {
        if (this.mDatabase != null && (this.mDatabase.isOpen() ^ 1) == 0 && bssid != null) {
            try {
                this.mDatabase.delete(DataBaseHelper.BSSID_TABLE_NAME, "bssid like ?", new String[]{bssid});
            } catch (SQLiteException e) {
                Log.w(MessageUtil.TAG, "delBssidInfo, delete, SQLiteException");
            }
        }
    }

    private void inlineAddCellInfo(String bssid, String cellid) {
        int rssi = CellStateMonitor.getCellRssi();
        if (this.mDatabase != null && (this.mDatabase.isOpen() ^ 1) == 0 && bssid != null && cellid != null) {
            try {
                this.mDatabase.execSQL("INSERT INTO CELLIDTable VALUES(null, ?, ?, ?)", new Object[]{bssid, cellid, Integer.valueOf(rssi)});
            } catch (SQLiteException e) {
                Log.w(MessageUtil.TAG, "inlineAddCellInfo, execSQL, SQLiteException");
            }
        }
    }

    private void delCellidInfoByBssid(String bssid) {
        if (this.mDatabase != null && (this.mDatabase.isOpen() ^ 1) == 0 && bssid != null) {
            try {
                this.mDatabase.delete(DataBaseHelper.CELLID_TABLE_NAME, "bssid like ?", new String[]{bssid});
            } catch (SQLiteException e) {
                Log.w(MessageUtil.TAG, "delCellidInfoByBssid, delete, SQLiteException");
            }
        }
    }

    public List<CellInfoData> queryCellInfoByBssid(String bssid) {
        synchronized (this.mLock) {
            List<CellInfoData> datas = new ArrayList();
            if (bssid == null) {
                return datas;
            }
            Cursor cursor = null;
            try {
                cursor = this.mDatabase.rawQuery("SELECT * FROM CELLIDTable where bssid like ?", new String[]{bssid});
                while (cursor.moveToNext()) {
                    String cellid = cursor.getString(cursor.getColumnIndex(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_CELLID));
                    int rssi = cursor.getInt(cursor.getColumnIndex(HwDualBandMessageUtil.MSG_KEY_RSSI));
                    if (!(cellid == null || rssi == 0)) {
                        datas.add(new CellInfoData(cellid, rssi));
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                Log.e(MessageUtil.TAG, "queryCellInfoByBssid:" + e);
                if (cursor != null) {
                    cursor.close();
                }
                return datas;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
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
        if (this.mDatabase != null && (this.mDatabase.isOpen() ^ 1) == 0 && bssid != null && nearbyBssid != null) {
            try {
                this.mDatabase.execSQL("INSERT INTO APInfoTable VALUES(null, ?, ?)", new Object[]{bssid, nearbyBssid});
            } catch (SQLiteException e) {
                Log.w(MessageUtil.TAG, "addNearbyApInfo, execSQL, SQLiteException");
            }
        }
    }
}
