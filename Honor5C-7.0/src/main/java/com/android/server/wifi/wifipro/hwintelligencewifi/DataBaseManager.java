package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.android.server.wifi.wifipro.HwDualBandMessageUtil;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieDataBaseImpl;
import java.util.ArrayList;
import java.util.List;

public class DataBaseManager {
    private static final String TAG = "DataBaseManager";
    private SQLiteDatabase mDatabase;
    private DataBaseHelper mHelper;
    private Object mLock;
    private WifiManager mWifiManager;

    public DataBaseManager(Context context) {
        this.mLock = new Object();
        Log.e(MessageUtil.TAG, "DataBaseManager()");
        this.mHelper = new DataBaseHelper(context);
        this.mDatabase = this.mHelper.getWritableDatabase();
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
    }

    public List<APInfoData> getAllApInfos() {
        synchronized (this.mLock) {
            Cursor cursor;
            ArrayList<APInfoData> infos = new ArrayList();
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                return infos;
            }
            cursor = null;
            try {
                cursor = this.mDatabase.rawQuery("SELECT * FROM BSSIDTable", null);
                while (cursor.moveToNext()) {
                    infos.add(new APInfoData(cursor.getString(cursor.getColumnIndex(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_BSSID)), cursor.getString(cursor.getColumnIndex(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_SSID)), cursor.getInt(cursor.getColumnIndex("inbacklist")), cursor.getInt(cursor.getColumnIndex(HwDualBandMessageUtil.MSG_KEY_AUTHTYPE)), cursor.getLong(cursor.getColumnIndex(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_TIME))));
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
            return infos;
        }
    }

    public void addApInfos(String bssid, String ssid, String cellid, int authtype) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                return;
            }
            inlineAddBssidIdInfo(bssid, ssid, authtype);
            inlineAddCellInfo(bssid, cellid);
            inlineAddNearbyApInfo(bssid);
        }
    }

    public void delAPInfos(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                return;
            }
            delBssidInfo(bssid);
            delCellidInfoByBssid(bssid);
            delNearbyApInfo(bssid);
        }
    }

    public void closeDB() {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                return;
            }
            Log.e(MessageUtil.TAG, "closeDB()");
            this.mDatabase.close();
        }
    }

    public void addCellInfo(String bssid, String cellid) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                return;
            }
            inlineAddCellInfo(bssid, cellid);
        }
    }

    public List<String> getNearbyApInfo(String bssid) {
        synchronized (this.mLock) {
            Cursor cursor;
            List<String> datas = new ArrayList();
            if (this.mDatabase == null || !this.mDatabase.isOpen() || bssid == null) {
                return datas;
            }
            cursor = null;
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
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return datas;
        }
    }

    public void addNearbyApInfo(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                return;
            }
            inlineAddNearbyApInfo(bssid);
        }
    }

    public void updateBssidTimer(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || bssid == null) {
                return;
            }
            long time = System.currentTimeMillis();
            Log.w(MessageUtil.TAG, "updateBssidTimer time = " + time);
            ContentValues values = new ContentValues();
            values.put(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_TIME, Long.valueOf(time));
            this.mDatabase.update(DataBaseHelper.BSSID_TABLE_NAME, values, "bssid like ?", new String[]{bssid});
        }
    }

    public void updateBssidIsInBlackList(String bssid, int inblacklist) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || bssid == null) {
                return;
            }
            ContentValues values = new ContentValues();
            values.put("inbacklist", Integer.valueOf(inblacklist));
            this.mDatabase.update(DataBaseHelper.BSSID_TABLE_NAME, values, "bssid like ?", new String[]{bssid});
        }
    }

    public void delNearbyApInfo(String bssid) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || bssid == null) {
                return;
            }
            this.mDatabase.delete(DataBaseHelper.APINFO_TABLE_NAME, "bssid like ?", new String[]{bssid});
        }
    }

    public void updateSsid(String bssid, String ssid) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || bssid == null || ssid == null) {
                return;
            }
            ContentValues values = new ContentValues();
            values.put(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_SSID, ssid);
            this.mDatabase.update(DataBaseHelper.BSSID_TABLE_NAME, values, "bssid like ?", new String[]{bssid});
        }
    }

    public void updateAuthType(String bssid, int authtype) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || bssid == null) {
                return;
            }
            ContentValues values = new ContentValues();
            values.put(HwDualBandMessageUtil.MSG_KEY_AUTHTYPE, Integer.valueOf(authtype));
            this.mDatabase.update(DataBaseHelper.BSSID_TABLE_NAME, values, "bssid like ?", new String[]{bssid});
        }
    }

    private void inlineAddBssidIdInfo(String bssid, String ssid, int authtype) {
        if (this.mDatabase != null && this.mDatabase.isOpen() && bssid != null && ssid != null) {
            long time = System.currentTimeMillis();
            this.mDatabase.execSQL("INSERT INTO BSSIDTable VALUES(null, ?,?,?,?,?)", new Object[]{bssid, ssid, Integer.valueOf(0), Integer.valueOf(authtype), Long.valueOf(time)});
        }
    }

    private void delBssidInfo(String bssid) {
        if (this.mDatabase != null && this.mDatabase.isOpen() && bssid != null) {
            this.mDatabase.delete(DataBaseHelper.BSSID_TABLE_NAME, "bssid like ?", new String[]{bssid});
        }
    }

    private void inlineAddCellInfo(String bssid, String cellid) {
        int rssi = CellStateMonitor.getCellRssi();
        if (this.mDatabase != null && this.mDatabase.isOpen() && bssid != null && cellid != null) {
            this.mDatabase.execSQL("INSERT INTO CELLIDTable VALUES(null, ?, ?, ?)", new Object[]{bssid, cellid, Integer.valueOf(rssi)});
        }
    }

    private void delCellidInfoByBssid(String bssid) {
        if (this.mDatabase != null && this.mDatabase.isOpen() && bssid != null) {
            this.mDatabase.delete(DataBaseHelper.CELLID_TABLE_NAME, "bssid like ?", new String[]{bssid});
        }
    }

    public List<CellInfoData> queryCellInfoByBssid(String bssid) {
        synchronized (this.mLock) {
            Cursor cursor;
            List<CellInfoData> datas = new ArrayList();
            if (bssid == null) {
                return datas;
            }
            cursor = null;
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
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return datas;
        }
    }

    private void inlineAddNearbyApInfo(String bssid) {
        int num = 0;
        List<ScanResult> lists = this.mWifiManager.getScanResults();
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
            this.mDatabase.execSQL("INSERT INTO APInfoTable VALUES(null, ?, ?)", new Object[]{bssid, nearbyBssid});
        }
    }
}
