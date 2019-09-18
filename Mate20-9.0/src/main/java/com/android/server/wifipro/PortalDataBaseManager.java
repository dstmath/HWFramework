package com.android.server.wifipro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import com.android.server.HwNetworkPropertyChecker;

public class PortalDataBaseManager {
    public static final boolean DBG = false;
    private static final String DB_NAME = "wifipro_portal_page_info.db";
    private static final int DB_VERSION = 9;
    public static final int MSG_INSERT_UPLOADED_TABLE = 101;
    private static final long TABLE5_MAX_SIZE = 5000;
    private static final String TAG = "PortalDataBaseManager";
    private static PortalDataBaseManager portalDataBaseManager = null;
    private SQLiteDatabase database = null;
    private PortalDbHelper dbHelper = null;

    public PortalDataBaseManager(Context context) {
        this.dbHelper = new PortalDbHelper(context, DB_NAME, null, 9);
        try {
            LOGD("PortalDataBaseManager, getWritableDatabase begin.");
            this.database = this.dbHelper.getWritableDatabase();
            LOGD("PortalDataBaseManager, getWritableDatabase end.");
        } catch (SQLiteCantOpenDatabaseException e) {
            LOGD("PortalDataBaseManager(), can't open database!");
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
        if (this.database == null || !this.database.isOpen() || dbName == null || dbName.length() <= 0) {
            return -1;
        }
        try {
            count = this.database.compileStatement("SELECT COUNT(*) FROM " + dbName).simpleQueryForLong();
            LOGD("getCurrentRowNumber, dbName = " + dbName + ", row num = " + count);
            return count;
        } catch (SQLException e) {
            LOGW("getCurrentRowNumber, SQLException");
            return count;
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00d1, code lost:
        if (r1 != null) goto L_0x00d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00e2, code lost:
        if (r1 == null) goto L_0x00ee;
     */
    public synchronized void updateStandardPortalTable(HwNetworkPropertyChecker.StarndardPortalInfo portalInfo) {
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
                        if (portalInfo.currentSsid.equals(cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_SSID)))) {
                            found = true;
                            break;
                        }
                    }
                    ContentValues cv = new ContentValues();
                    if (found) {
                        cv.put(PortalDbHelper.ITEM_CHECK_TIMESTAMP, String.valueOf(portalInfo.timestamp));
                        cv.put(PortalDbHelper.ITEM_CHECK_LAC, Integer.valueOf(portalInfo.lac));
                        if (((long) this.database.update(PortalDbHelper.TABLE_STANDARD_PORTAL_302, cv, "ssid=?", new String[]{portalInfo.currentSsid})) > 0) {
                            Log.d(TAG, "updateStandardPortalTable, update ret = " + ret0);
                        }
                    } else {
                        cv.put(PortalDbHelper.ITEM_SSID, portalInfo.currentSsid);
                        cv.put(PortalDbHelper.ITEM_CHECK_TIMESTAMP, String.valueOf(portalInfo.timestamp));
                        cv.put(PortalDbHelper.ITEM_CHECK_LAC, Integer.valueOf(portalInfo.lac));
                        if (this.database.insert(PortalDbHelper.TABLE_STANDARD_PORTAL_302, null, cv) > 0) {
                            Log.d(TAG, "updateStandardPortalTable, insert ret = " + ret);
                        }
                    }
                } catch (SQLiteException e) {
                    try {
                        Log.w(TAG, "updateStandardPortalTable, SQLiteException happened");
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 6 */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007d, code lost:
        if (r0 != null) goto L_0x007f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x009a, code lost:
        if (r0 == null) goto L_0x00a6;
     */
    public synchronized void syncQueryStarndardPortalNetwork(HwNetworkPropertyChecker.StarndardPortalInfo portalInfo) {
        Cursor cursor = null;
        if (portalInfo != null) {
            if (!(portalInfo.currentSsid == null || this.database == null || !this.database.isOpen())) {
                try {
                    cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE4, null);
                    while (true) {
                        if (!cursor.moveToNext()) {
                            break;
                        }
                        if (portalInfo.currentSsid.equals(cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_SSID)))) {
                            String ts = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_CHECK_TIMESTAMP));
                            int lac = cursor.getInt(cursor.getColumnIndex(PortalDbHelper.ITEM_CHECK_LAC));
                            LOGD("syncQueryStarndardPortalNetwork, matched, currentSsid = " + portalInfo.currentSsid);
                            if (ts != null && ts.length() > 0) {
                                portalInfo.timestamp = Long.parseLong(ts);
                            }
                            if (lac > Integer.MIN_VALUE && lac < Integer.MAX_VALUE) {
                                portalInfo.lac = lac;
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    Log.w(TAG, "syncQueryStarndardPortalNetwork, NumberFormatException happened");
                } catch (SQLiteException e2) {
                    try {
                        Log.w(TAG, "syncQueryStarndardPortalNetwork, SQLiteException happened");
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 11 */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0025, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00bc, code lost:
        if (r1 != null) goto L_0x00be;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00cb, code lost:
        if (r1 == null) goto L_0x00d7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00d8, code lost:
        return;
     */
    public synchronized void updateDhcpResultsByBssid(String currBssid, String dhcpResults) {
        if (!(currBssid == null || dhcpResults == null)) {
            if (this.database != null && this.database.isOpen()) {
                Cursor cursor = null;
                try {
                    long currRows = getCurrentRowNumber(PortalDbHelper.TABLE_DHCP_RESULTS_INTERNET_OK);
                    if (currRows != -1) {
                        boolean found = false;
                        cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE5, null);
                        while (true) {
                            if (!cursor.moveToNext()) {
                                break;
                            } else if (currBssid.equals(cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_BSSID)))) {
                                found = true;
                                break;
                            }
                        }
                        ContentValues cv = new ContentValues();
                        if (found) {
                            cv.put(PortalDbHelper.ITEM_DHCP_RESULTS, String.valueOf(dhcpResults));
                            if (this.database.update(PortalDbHelper.TABLE_DHCP_RESULTS_INTERNET_OK, cv, "bssid=?", new String[]{currBssid}) > 0) {
                                Log.d(TAG, "updateDhcpResultsByBssid, update ret = " + ret0);
                            }
                        } else if (currRows < TABLE5_MAX_SIZE) {
                            cv.put(PortalDbHelper.ITEM_BSSID, currBssid);
                            cv.put(PortalDbHelper.ITEM_DHCP_RESULTS, String.valueOf(dhcpResults));
                            if (this.database.insert(PortalDbHelper.TABLE_DHCP_RESULTS_INTERNET_OK, null, cv) > 0) {
                                Log.d(TAG, "updateDhcpResultsByBssid, insert ret = " + ret1);
                            }
                        }
                    } else if (cursor != null) {
                        cursor.close();
                    }
                } catch (SQLiteException e) {
                    try {
                        LOGW("updateDhcpResultsByBssid, SQLiteException happend!");
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0046, code lost:
        if (r2 != null) goto L_0x0048;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0055, code lost:
        if (r2 == null) goto L_0x0061;
     */
    public synchronized String syncQueryDhcpResultsByBssid(String currentBssid) {
        String dhcpResults;
        dhcpResults = null;
        if (currentBssid != null) {
            if (this.database != null && this.database.isOpen()) {
                Cursor cursor = null;
                try {
                    cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE5, null);
                    while (true) {
                        if (!cursor.moveToNext()) {
                            break;
                        } else if (currentBssid.equals(cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_BSSID)))) {
                            String matchedDhcp = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_DHCP_RESULTS));
                            if (matchedDhcp != null && matchedDhcp.length() > 0) {
                                dhcpResults = matchedDhcp;
                            }
                        }
                    }
                } catch (SQLiteException e) {
                    try {
                        LOGW("syncQueryDhcpResultsByBssid, SQLiteException msg happend.");
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            }
        }
        return dhcpResults;
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    public void LOGW(String msg) {
        Log.w(TAG, msg);
    }
}
