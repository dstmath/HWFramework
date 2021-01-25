package com.huawei.hwwifiproservice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.huawei.hwwifiproservice.HwNetworkPropertyChecker;

public class PortalDataBaseManager {
    private static final boolean DBG = false;
    public static final String DB_NAME = "wifipro_portal_page_info.db";
    private static final int DB_VERSION = 9;
    private static final int MSG_INSERT_UPLOADED_TABLE = 101;
    private static final long TABLE5_MAX_SIZE = 5000;
    private static final String TAG = "PortalDataBaseManager";
    private static PortalDataBaseManager portalDataBaseManager = null;
    private SQLiteDatabase database = null;
    private PortalDbHelper dbHelper = null;

    private PortalDataBaseManager(Context context) {
        this.dbHelper = new PortalDbHelper(context, DB_NAME, null, 9);
        try {
            HwHiLog.d(TAG, false, "PortalDataBaseManager, getWritableDatabase begin.", new Object[0]);
            this.database = this.dbHelper.getWritableDatabase();
            HwHiLog.d(TAG, false, "PortalDataBaseManager, getWritableDatabase end.", new Object[0]);
        } catch (SQLiteCantOpenDatabaseException e) {
            HwHiLog.e(TAG, false, "PortalDataBaseManager(), can't open database!", new Object[0]);
        }
    }

    public static synchronized PortalDataBaseManager getInstance(Context context) {
        PortalDataBaseManager portalDataBaseManager2;
        synchronized (PortalDataBaseManager.class) {
            if (portalDataBaseManager == null) {
                portalDataBaseManager = new PortalDataBaseManager(context);
            }
            portalDataBaseManager2 = portalDataBaseManager;
        }
        return portalDataBaseManager2;
    }

    private long getCurrentRowNumber(String dbName) {
        long count = -1;
        SQLiteDatabase sQLiteDatabase = this.database;
        if (sQLiteDatabase == null || !sQLiteDatabase.isOpen() || dbName == null || dbName.length() <= 0) {
            return -1;
        }
        try {
            count = this.database.compileStatement("SELECT COUNT(*) FROM " + dbName).simpleQueryForLong();
            HwHiLog.d(TAG, false, "getCurrentRowNumber, dbName = %{public}s, row num = %{public}s", new Object[]{dbName, String.valueOf(count)});
            return count;
        } catch (SQLException e) {
            HwHiLog.e(TAG, false, "Exception happened in getCurrentRowNumber()", new Object[0]);
            return count;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x00d7  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00dd  */
    public synchronized void updateStandardPortalTable(HwNetworkPropertyChecker.StarndardPortalInfo portalInfo) {
        Throwable th;
        if (portalInfo != null) {
            if (portalInfo.currentSsid != null && portalInfo.timestamp > 0 && this.database != null && this.database.isOpen()) {
                Cursor cursor = null;
                boolean found = false;
                try {
                    cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE4, null);
                    while (true) {
                        if (!cursor.moveToNext()) {
                            break;
                        }
                        try {
                            if (portalInfo.currentSsid.equals(cursor.getString(cursor.getColumnIndex("ssid")))) {
                                found = true;
                                break;
                            }
                        } catch (SQLiteException e) {
                            try {
                                HwHiLog.e(TAG, false, "Exception happened in updateStandardPortalTable()", new Object[0]);
                                if (cursor != null) {
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (cursor != null) {
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            if (cursor != null) {
                                cursor.close();
                            }
                            throw th;
                        }
                    }
                    ContentValues cv = new ContentValues();
                    if (found) {
                        cv.put(PortalDbHelper.ITEM_CHECK_TIMESTAMP, String.valueOf(portalInfo.timestamp));
                        cv.put(PortalDbHelper.ITEM_CHECK_LAC, Integer.valueOf(portalInfo.lac));
                        long ret0 = (long) this.database.update(PortalDbHelper.TABLE_STANDARD_PORTAL_302, cv, "ssid=?", new String[]{portalInfo.currentSsid});
                        if (ret0 > 0) {
                            HwHiLog.d(TAG, false, "updateStandardPortalTable, update ret = %{public}s", new Object[]{String.valueOf(ret0)});
                        }
                    } else {
                        cv.put("ssid", portalInfo.currentSsid);
                        cv.put(PortalDbHelper.ITEM_CHECK_TIMESTAMP, String.valueOf(portalInfo.timestamp));
                        cv.put(PortalDbHelper.ITEM_CHECK_LAC, Integer.valueOf(portalInfo.lac));
                        long ret = this.database.insert(PortalDbHelper.TABLE_STANDARD_PORTAL_302, null, cv);
                        if (ret > 0) {
                            HwHiLog.d(TAG, false, "updateStandardPortalTable, insert ret = %{public}s", new Object[]{String.valueOf(ret)});
                        }
                    }
                    cursor.close();
                } catch (SQLiteException e2) {
                    HwHiLog.e(TAG, false, "Exception happened in updateStandardPortalTable()", new Object[0]);
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x008f, code lost:
        if (r0 != null) goto L_0x0091;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0091, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x009f, code lost:
        if (r0 != null) goto L_0x0091;
     */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00a4  */
    public synchronized void syncQueryStarndardPortalNetwork(HwNetworkPropertyChecker.StarndardPortalInfo portalInfo) {
        Throwable th;
        Cursor cursor = null;
        if (portalInfo != null) {
            if (!(portalInfo.currentSsid == null || this.database == null || !this.database.isOpen())) {
                try {
                    cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE4, null);
                    while (true) {
                        if (!cursor.moveToNext()) {
                            break;
                        }
                        try {
                            if (portalInfo.currentSsid.equals(cursor.getString(cursor.getColumnIndex("ssid")))) {
                                String ts = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_CHECK_TIMESTAMP));
                                int lac = cursor.getInt(cursor.getColumnIndex(PortalDbHelper.ITEM_CHECK_LAC));
                                HwHiLog.d(TAG, false, "syncQueryStarndardPortalNetwork, matched, currentSsid = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(portalInfo.currentSsid)});
                                if (ts != null && ts.length() > 0) {
                                    portalInfo.timestamp = Long.parseLong(ts);
                                }
                                if (lac > Integer.MIN_VALUE && lac < Integer.MAX_VALUE) {
                                    portalInfo.lac = lac;
                                }
                            }
                        } catch (NumberFormatException e) {
                            HwHiLog.e(TAG, false, "Exception happened in syncQueryStarndardPortalNetwork(), format error", new Object[0]);
                        } catch (SQLiteException e2) {
                            try {
                                HwHiLog.e(TAG, false, "Exception happened in syncQueryStarndardPortalNetwork(), database error", new Object[0]);
                            } catch (Throwable th2) {
                                th = th2;
                                if (cursor != null) {
                                    cursor.close();
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            if (cursor != null) {
                            }
                            throw th;
                        }
                    }
                    cursor.close();
                } catch (NumberFormatException e3) {
                    HwHiLog.e(TAG, false, "Exception happened in syncQueryStarndardPortalNetwork(), format error", new Object[0]);
                } catch (SQLiteException e4) {
                    HwHiLog.e(TAG, false, "Exception happened in syncQueryStarndardPortalNetwork(), database error", new Object[0]);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x00c4  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00ca  */
    public synchronized void updateDhcpResultsByBssid(String currBssid, String dhcpResults) {
        Throwable th;
        if (!(currBssid == null || dhcpResults == null)) {
            if (this.database != null && this.database.isOpen()) {
                Cursor cursor = null;
                try {
                    long currRows = getCurrentRowNumber(PortalDbHelper.TABLE_DHCP_RESULTS_INTERNET_OK);
                    if (currRows == -1) {
                        if (0 != 0) {
                            cursor.close();
                        }
                        return;
                    }
                    boolean found = false;
                    cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE5, null);
                    while (true) {
                        if (!cursor.moveToNext()) {
                            break;
                        }
                        try {
                            if (currBssid.equals(cursor.getString(cursor.getColumnIndex("bssid")))) {
                                found = true;
                                break;
                            }
                        } catch (SQLiteException e) {
                            try {
                                HwHiLog.e(TAG, false, "updateDhcpResultsByBssid, Exception happend!", new Object[0]);
                                if (cursor != null) {
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (cursor != null) {
                                    cursor.close();
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            if (cursor != null) {
                            }
                            throw th;
                        }
                    }
                    ContentValues cv = new ContentValues();
                    if (found) {
                        cv.put(PortalDbHelper.ITEM_DHCP_RESULTS, String.valueOf(dhcpResults));
                        int ret0 = this.database.update(PortalDbHelper.TABLE_DHCP_RESULTS_INTERNET_OK, cv, "bssid=?", new String[]{currBssid});
                        if (ret0 > 0) {
                            HwHiLog.d(TAG, false, "updateDhcpResultsByBssid, update ret = %{public}d", new Object[]{Integer.valueOf(ret0)});
                        }
                    } else if (currRows < TABLE5_MAX_SIZE) {
                        cv.put("bssid", currBssid);
                        cv.put(PortalDbHelper.ITEM_DHCP_RESULTS, String.valueOf(dhcpResults));
                        long ret1 = this.database.insert(PortalDbHelper.TABLE_DHCP_RESULTS_INTERNET_OK, null, cv);
                        if (ret1 > 0) {
                            HwHiLog.d(TAG, false, "updateDhcpResultsByBssid, insert ret = %{public}s", new Object[]{String.valueOf(ret1)});
                        }
                    }
                    cursor.close();
                } catch (SQLiteException e2) {
                    HwHiLog.e(TAG, false, "updateDhcpResultsByBssid, Exception happend!", new Object[0]);
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x005e  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0064  */
    public synchronized String syncQueryDhcpResultsByBssid(String currentBssid) {
        String dhcpResults;
        Throwable th;
        dhcpResults = null;
        if (currentBssid != null) {
            if (this.database != null && this.database.isOpen()) {
                Cursor cursor = null;
                try {
                    cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE5, null);
                    while (true) {
                        if (!cursor.moveToNext()) {
                            break;
                        }
                        try {
                            if (currentBssid.equals(cursor.getString(cursor.getColumnIndex("bssid")))) {
                                String matchedDhcp = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_DHCP_RESULTS));
                                if (matchedDhcp != null && matchedDhcp.length() > 0) {
                                    dhcpResults = matchedDhcp;
                                }
                            }
                        } catch (SQLiteException e) {
                            try {
                                HwHiLog.e(TAG, false, "syncQueryDhcpResultsByBssid, Exception happened.", new Object[0]);
                                if (cursor != null) {
                                    cursor.close();
                                }
                                return dhcpResults;
                            } catch (Throwable th2) {
                                th = th2;
                                if (cursor != null) {
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            if (cursor != null) {
                                cursor.close();
                            }
                            throw th;
                        }
                    }
                    cursor.close();
                } catch (SQLiteException e2) {
                    HwHiLog.e(TAG, false, "syncQueryDhcpResultsByBssid, Exception happened.", new Object[0]);
                    if (cursor != null) {
                    }
                    return dhcpResults;
                }
            }
        }
        return dhcpResults;
    }
}
