package com.android.server.wifipro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.HwNetworkPropertyChecker.StarndardPortalInfo;
import com.android.server.emcom.SmartcareConstants;
import java.util.Map;

public class PortalDataBaseManager {
    public static final boolean DBG = false;
    private static final String DB_NAME = "wifipro_portal_page_info.db";
    private static final int DB_VERSION = 8;
    private static final long MAX_ROW_SIZE = 500;
    public static final int MSG_INSERT_UPLOADED_TABLE = 101;
    private static final long TABLE5_MAX_SIZE = 5000;
    private static final String TAG = "_PortalDataBaseManager";
    private static PortalDataBaseManager portalDataBaseManager = null;
    private SQLiteDatabase database = null;
    private PortalDbHelper dbHelper = null;
    private boolean mAutoFillSupported;
    private Handler mHandler = null;
    private PortalAttributesInfo mPortalAttributesInfo = new PortalAttributesInfo();

    static class PortalAttributesInfo {
        private String mLoginBtnId;
        private String mLoginBtnName;
        private String mLoginBtnValue;
        private String mLoginNodeType;
        private String mPhoneNumInputId;
        private String mPhoneNumInputName;
        private String mSmsPwInputId;
        private String mSmsPwInputName;
        private String mSmsPwInputValue;

        PortalAttributesInfo() {
        }

        public void copyAttributesInfo(PortalAttributesInfo source) {
            if (source != null) {
                this.mPhoneNumInputId = source.mPhoneNumInputId;
                this.mPhoneNumInputName = source.mPhoneNumInputName;
                this.mSmsPwInputId = source.mSmsPwInputId;
                this.mSmsPwInputName = source.mSmsPwInputName;
                this.mSmsPwInputValue = source.mSmsPwInputValue;
                this.mLoginBtnId = source.mLoginBtnId;
                this.mLoginBtnName = source.mLoginBtnName;
                this.mLoginBtnValue = source.mLoginBtnValue;
                this.mLoginNodeType = source.mLoginNodeType;
            }
        }

        public void release() {
            this.mPhoneNumInputId = null;
            this.mPhoneNumInputName = null;
            this.mSmsPwInputId = null;
            this.mSmsPwInputName = null;
            this.mSmsPwInputValue = null;
            this.mLoginBtnId = null;
            this.mLoginBtnName = null;
            this.mLoginBtnValue = null;
            this.mLoginNodeType = null;
        }
    }

    public PortalDataBaseManager(Context context) {
        this.dbHelper = new PortalDbHelper(context, DB_NAME, null, 8);
        this.database = this.dbHelper.getWritableDatabase();
        this.mAutoFillSupported = false;
        init();
    }

    public static synchronized PortalDataBaseManager getInstance(Context context) {
        PortalDataBaseManager portalDataBaseManager;
        synchronized (PortalDataBaseManager.class) {
            if (portalDataBaseManager == null) {
                portalDataBaseManager = new PortalDataBaseManager(context);
            }
            portalDataBaseManager = portalDataBaseManager;
        }
        return portalDataBaseManager;
    }

    private void init() {
        synchronized (this) {
            HandlerThread handlerThread = new HandlerThread("wifipro_portal_db_handler_thread");
            handlerThread.start();
            this.mHandler = new Handler(handlerThread.getLooper()) {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 101:
                            PortalDataBaseManager.this.insertTable((PortalWebPageInfo) msg.obj, PortalDbHelper.TABLE_UPLOADED_PORTAL_WEB_PAGE_INFO);
                            break;
                    }
                    super.handleMessage(msg);
                }
            };
        }
    }

    public boolean isDatabaseEmpty(String database) {
        return getCurrentRowNumber(database) == 0;
    }

    public boolean removeDbRecords(String dbName) {
        if (!(this.database == null || dbName == null)) {
            try {
                this.database.delete(dbName, null, null);
                return true;
            } catch (SQLException e) {
                LOGW("removeDbRecords, SQLException");
            }
        }
        return false;
    }

    public long getCurrentRowNumber(String dbName) {
        long count = -1;
        if (dbName == null || dbName.length() <= 0) {
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

    /* JADX WARNING: Missing block: B:37:0x00c0, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean needUploadChr(PortalWebPageInfo info) {
        if (info != null) {
            if (!(info.ssid == null || info.url == null)) {
                LOGD("needUploadChr, query ssid = " + info.ssid + ", url = " + info.url);
                try {
                    if (getCurrentRowNumber(PortalDbHelper.TABLE_UPLOADED_PORTAL_WEB_PAGE_INFO) >= 500) {
                        LOGW("needUploadChr, database is out of size, remove all rows.");
                        this.database.delete(PortalDbHelper.TABLE_UPLOADED_PORTAL_WEB_PAGE_INFO, null, null);
                    }
                    Cursor cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE1, null);
                    while (cursor.moveToNext()) {
                        String ssid = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_SSID));
                        String url = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_URL));
                        if (info.ssid.equals(ssid) && info.url.contains(url)) {
                            LOGD("needUploadChr, ignore it, info = " + info);
                            cursor.close();
                            return false;
                        }
                    }
                    cursor.close();
                } catch (SQLException e) {
                    LOGW("needUploadChr, SQLException");
                }
                this.mHandler.sendMessage(Message.obtain(this.mHandler, 101, info));
                return true;
            }
        }
    }

    public static PortalWebPageInfo createPortalInfo(Map<String, String> items) {
        return PortalWebPageInfo.createPortalInfo(items);
    }

    public synchronized void updateTable(PortalWebPageInfo info, String dbName) {
        if (info != null) {
            ContentValues cv = new ContentValues();
            cv.put(PortalDbHelper.ITEM_PHONE, info.phoneNumInputId);
            cv.put(PortalDbHelper.ITEM_PHONE_NAME, info.phoneNumInputName);
            cv.put(PortalDbHelper.ITEM_PW, info.smsPwInputId);
            cv.put(PortalDbHelper.ITEM_PW_NAME, info.smsPwInputName);
            cv.put(PortalDbHelper.ITEM_PW_VALUE, info.smsPwInputValue);
            cv.put(PortalDbHelper.ITEM_SND_BTN, info.sndBtnId);
            cv.put(PortalDbHelper.ITEM_LG_BTN, info.loginBtnId);
            cv.put(PortalDbHelper.ITEM_LG_BTN_NAME, info.loginBtnName);
            cv.put(PortalDbHelper.ITEM_LG_BTN_VALUE, info.loginBtnValue);
            cv.put(PortalDbHelper.ITEM_LG_NODE_TYPE, info.loginNodeType);
            try {
                this.database.update(dbName, cv, "ssid=? AND url=?", new String[]{info.ssid, info.url});
            } catch (SQLiteException e) {
                LOGW("updateTable, SQLiteException");
            }
        }
        return;
    }

    public synchronized void insertTable(PortalWebPageInfo info, String dbName) {
        if (info != null) {
            if (WifiProCommonUtils.parseHostByUrlLocation(info.url) != null) {
                ContentValues cv = new ContentValues();
                cv.put(PortalDbHelper.ITEM_SSID, info.ssid);
                cv.put(PortalDbHelper.ITEM_URL, info.url);
                cv.put(PortalDbHelper.ITEM_PHONE, info.phoneNumInputId);
                cv.put(PortalDbHelper.ITEM_PW, info.smsPwInputId);
                cv.put(PortalDbHelper.ITEM_SND_BTN, info.sndBtnId);
                cv.put(PortalDbHelper.ITEM_LG_BTN, info.loginBtnId);
                if (PortalDbHelper.TABLE_COLLECTED_PORTAL_WEB_PAGE_INFO.equals(dbName)) {
                    cv.put(PortalDbHelper.ITEM_PHONE_NAME, info.phoneNumInputName);
                    cv.put(PortalDbHelper.ITEM_PW_NAME, info.smsPwInputName);
                    cv.put(PortalDbHelper.ITEM_PW_VALUE, info.smsPwInputValue);
                    cv.put(PortalDbHelper.ITEM_LG_BTN_NAME, info.loginBtnName);
                    cv.put(PortalDbHelper.ITEM_LG_BTN_VALUE, info.loginBtnValue);
                    cv.put(PortalDbHelper.ITEM_LG_NODE_TYPE, info.loginNodeType);
                }
                if (PortalDbHelper.TABLE_UPLOADED_PORTAL_WEB_PAGE_INFO.equals(dbName)) {
                    cv.put(PortalDbHelper.ITEM_BSSID, info.bssid);
                    cv.put(PortalDbHelper.ITEM_CELLID, info.cellid);
                    cv.put(PortalDbHelper.ITEM_BTN_NUM, info.htmlBtnNumber);
                }
                try {
                    this.database.insert(dbName, null, cv);
                } catch (SQLiteException e) {
                    LOGW("insertTable, SQLiteException");
                }
            }
        }
        return;
    }

    private void getPortalAttributesFromDb(Cursor cursor, PortalAttributesInfo info) {
        if (cursor != null && (cursor.isClosed() ^ 1) != 0 && info != null) {
            info.mPhoneNumInputId = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_PHONE));
            info.mPhoneNumInputName = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_PHONE_NAME));
            info.mSmsPwInputId = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_PW));
            info.mSmsPwInputName = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_PW_NAME));
            info.mSmsPwInputValue = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_PW_VALUE));
            info.mLoginBtnId = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_LG_BTN));
            info.mLoginBtnName = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_LG_BTN_NAME));
            info.mLoginBtnValue = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_LG_BTN_VALUE));
            info.mLoginNodeType = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_LG_NODE_TYPE));
        }
    }

    private boolean matchedByFullHostOfUrl(String requestUrl) {
        boolean matched = false;
        if (requestUrl != null) {
            Cursor cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE2, null);
            while (cursor.moveToNext()) {
                String url = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_URL));
                if (!TextUtils.isEmpty(url) && requestUrl.startsWith(url)) {
                    matched = true;
                    this.mAutoFillSupported = true;
                    getPortalAttributesFromDb(cursor, this.mPortalAttributesInfo);
                    LOGD("matchedByFullHostOfUrl, matched, valid url = " + url);
                    break;
                }
            }
            cursor.close();
        }
        return matched;
    }

    private boolean matchedByHostOfUrl(String requestUrl) {
        String requestHost = WifiProCommonUtils.parseHostByUrlLocation(requestUrl);
        boolean matched = false;
        if (requestHost != null) {
            Cursor cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE2, null);
            while (cursor.moveToNext()) {
                String url = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_URL));
                if (requestHost.equals(WifiProCommonUtils.parseHostByUrlLocation(url))) {
                    matched = true;
                    this.mAutoFillSupported = true;
                    getPortalAttributesFromDb(cursor, this.mPortalAttributesInfo);
                    LOGD("matchedByHostOfUrl, matched, valid url = " + url);
                    break;
                }
            }
            cursor.close();
        }
        return matched;
    }

    public synchronized boolean isMatchedByCollectionTable(String currentSsid, String requestUrl) {
        LOGD("isMatchedByCollectionTable, query currentSsid = " + currentSsid + ", requestUrl = " + requestUrl);
        if (!(currentSsid == null || requestUrl == null)) {
            try {
                int matchedRowNumber = getMatchedRowNumBySsid(currentSsid);
                String requestHost = WifiProCommonUtils.parseHostByUrlLocation(requestUrl);
                Cursor cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE2, null);
                boolean matched = false;
                boolean ssidMatched = false;
                PortalAttributesInfo hostMatchedPortalAttributesInfo = null;
                PortalAttributesInfo ssidMatchedPortalAttributesInfo = null;
                while (cursor.moveToNext()) {
                    String ssid = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_SSID));
                    if (currentSsid.equals(ssid)) {
                        ssidMatched = true;
                        String url = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_URL));
                        LOGD("isMatchedByCollectionTable, query database, ssid = " + ssid + ", url = " + url);
                        if (matchedRowNumber == 1) {
                            matched = true;
                            this.mAutoFillSupported = true;
                            getPortalAttributesFromDb(cursor, this.mPortalAttributesInfo);
                            break;
                        }
                        String host = WifiProCommonUtils.parseHostByUrlLocation(url);
                        if (requestUrl.startsWith(url)) {
                            matched = true;
                            this.mAutoFillSupported = true;
                            getPortalAttributesFromDb(cursor, this.mPortalAttributesInfo);
                            break;
                        }
                        if (requestHost != null) {
                            if (requestHost.equals(host)) {
                                if (hostMatchedPortalAttributesInfo == null) {
                                    hostMatchedPortalAttributesInfo = new PortalAttributesInfo();
                                    getPortalAttributesFromDb(cursor, hostMatchedPortalAttributesInfo);
                                }
                            }
                        }
                        if (ssidMatchedPortalAttributesInfo == null) {
                            ssidMatchedPortalAttributesInfo = new PortalAttributesInfo();
                            getPortalAttributesFromDb(cursor, ssidMatchedPortalAttributesInfo);
                        }
                    }
                }
                cursor.close();
                if (!matched && hostMatchedPortalAttributesInfo != null) {
                    matched = true;
                    this.mAutoFillSupported = true;
                    this.mPortalAttributesInfo.copyAttributesInfo(hostMatchedPortalAttributesInfo);
                    LOGW("isMatchedByCollectionTable, using the host matched backup data to try it");
                } else if (!matched && ssidMatchedPortalAttributesInfo != null) {
                    matched = true;
                    this.mAutoFillSupported = true;
                    this.mPortalAttributesInfo.copyAttributesInfo(ssidMatchedPortalAttributesInfo);
                    LOGW("isMatchedByCollectionTable, using the ssid matched backup data to try it");
                } else if (!ssidMatched) {
                    matched = matchedByFullHostOfUrl(requestUrl);
                    if (!matched) {
                        matched = matchedByHostOfUrl(requestUrl);
                    }
                }
                LOGD("isMatchedByCollectionTable, matched = " + matched + ", phoneId = " + this.mPortalAttributesInfo.mPhoneNumInputId + ", pwId = " + this.mPortalAttributesInfo.mSmsPwInputId + ", loginId = " + this.mPortalAttributesInfo.mLoginBtnId + ", phoneName = " + this.mPortalAttributesInfo.mPhoneNumInputName + ", passwordName = " + this.mPortalAttributesInfo.mSmsPwInputName);
                return matched;
            } catch (SQLiteException e) {
                LOGW("isMatchedByCollectionTable, SQLiteException");
            }
        }
        return false;
    }

    private int getMatchedRowNumBySsid(String currentSsid) {
        int matchedNum = -1;
        try {
            String[] whereArgs = new String[]{currentSsid};
            Cursor cursor = this.database.rawQuery("SELECT * FROM COLLECTED_PORTAL_WEB_PAGE_INFO WHERE " + "ssid=?", whereArgs);
            matchedNum = cursor.getCount();
            cursor.close();
            LOGD("getMatchedRowNumBySsid, currentSsid = " + currentSsid + ", matchedNum = " + matchedNum);
            return matchedNum;
        } catch (SQLiteException e) {
            LOGW("getMatchedRowNumBySsid, SQLiteException");
            return matchedNum;
        }
    }

    /* JADX WARNING: Missing block: B:17:0x005d, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean syncQueryPortalNetwork(String currentSsid) {
        boolean z = true;
        synchronized (this) {
            if (currentSsid != null) {
                if (this.database != null) {
                    try {
                        String whereClause = "ssid=?";
                        String[] whereArgs = new String[]{currentSsid};
                        Cursor cursor = this.database.rawQuery("SELECT * FROM COLLECTED_PORTAL_WEB_PAGE_INFO WHERE " + whereClause, whereArgs);
                        int matchedNum = cursor.getCount();
                        cursor.close();
                        if (matchedNum >= 1) {
                            return true;
                        }
                        cursor = this.database.rawQuery("SELECT * FROM UPLOADED_PORTAL_WEB_PAGE_INFO WHERE " + whereClause, whereArgs);
                        matchedNum = cursor.getCount();
                        cursor.close();
                        if (matchedNum < 1) {
                            z = false;
                        }
                    } catch (SQLiteException e) {
                        LOGW("syncQueryPortalNetwork, SQLiteException");
                        return false;
                    }
                }
            }
        }
    }

    public String queryLastInputPhoneNumBySsid(String currentSsid) {
        LOGD("queryLastInputPhoneNumBySsid, currentSsid = " + currentSsid);
        String phoneNum = null;
        if (currentSsid != null) {
            try {
                Cursor cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE3, null);
                while (cursor.moveToNext()) {
                    String ssid = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_SSID));
                    if (ssid != null && ssid.equals(currentSsid)) {
                        phoneNum = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_PHONE_NUM));
                        LOGD("queryLastInputPhoneNumBySsid, matched, currentSsid = " + currentSsid + ", num = " + (phoneNum != null));
                        cursor.close();
                    }
                }
                cursor.close();
            } catch (SQLiteException e) {
                LOGW("queryLastInputPhoneNumBySsid, SQLiteException");
            }
        }
        return phoneNum;
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x005e A:{Catch:{ SQLiteException -> 0x0074 }} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0046 A:{Catch:{ SQLiteException -> 0x0074 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateLastInputPhoneNumBySsid(String currentSsid, String newPhoneNumber) {
        if (!(currentSsid == null || newPhoneNumber == null)) {
            boolean found = false;
            try {
                ContentValues cv;
                Cursor cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE3, null);
                while (cursor.moveToNext()) {
                    if (currentSsid.equals(cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_SSID)))) {
                        if (newPhoneNumber.equals(cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_PHONE_NUM)))) {
                            cursor.close();
                            return;
                        }
                        found = true;
                        cursor.close();
                        cv = new ContentValues();
                        if (found) {
                            cv.put(PortalDbHelper.ITEM_SSID, currentSsid);
                            cv.put(PortalDbHelper.ITEM_PHONE_NUM, newPhoneNumber);
                            this.database.insert(PortalDbHelper.TABLE_LAST_INPUT_PHONE_NUM_ON_SSID, null, cv);
                        } else {
                            cv.put(PortalDbHelper.ITEM_PHONE_NUM, newPhoneNumber);
                            String[] whereArgs = new String[]{currentSsid};
                            this.database.update(PortalDbHelper.TABLE_LAST_INPUT_PHONE_NUM_ON_SSID, cv, "ssid=?", whereArgs);
                        }
                    }
                }
                cursor.close();
                cv = new ContentValues();
                if (found) {
                }
            } catch (SQLiteException e) {
                LOGW("updateLastInputPhoneNumBySsid, SQLiteException");
            }
        }
    }

    public synchronized boolean isAutoFillSupported() {
        return this.mAutoFillSupported;
    }

    public synchronized String getPhoneNumInputId() {
        return this.mPortalAttributesInfo.mPhoneNumInputId;
    }

    public synchronized String getPhoneNumInputName() {
        return this.mPortalAttributesInfo.mPhoneNumInputName;
    }

    public synchronized String getPasswordInputId() {
        return this.mPortalAttributesInfo.mSmsPwInputId;
    }

    public synchronized String getPasswordInputName() {
        return this.mPortalAttributesInfo.mSmsPwInputName;
    }

    public synchronized String getPasswordInputValue() {
        return this.mPortalAttributesInfo.mSmsPwInputValue;
    }

    public synchronized String getLoginBtnId() {
        return this.mPortalAttributesInfo.mLoginBtnId;
    }

    public synchronized String getLoginBtnName() {
        return this.mPortalAttributesInfo.mLoginBtnName;
    }

    public synchronized String getLoginBtnValue() {
        return this.mPortalAttributesInfo.mLoginBtnValue;
    }

    public synchronized String getLoginBtnNodeType() {
        return this.mPortalAttributesInfo.mLoginNodeType;
    }

    public synchronized void handlePortalDisconnected() {
        this.mAutoFillSupported = false;
        this.mPortalAttributesInfo.release();
    }

    public synchronized void updateStandardPortalTable(StarndardPortalInfo portalInfo) {
        if (portalInfo != null) {
            if (!(portalInfo.currentSsid == null || portalInfo.timestamp <= 0 || this.database == null)) {
                boolean found = false;
                try {
                    Cursor cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE4, null);
                    while (cursor.moveToNext()) {
                        if (portalInfo.currentSsid.equals(cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_SSID)))) {
                            found = true;
                            break;
                        }
                    }
                    cursor.close();
                    ContentValues cv = new ContentValues();
                    if (found) {
                        cv.put(PortalDbHelper.ITEM_CHECK_TIMESTAMP, String.valueOf(portalInfo.timestamp));
                        cv.put(PortalDbHelper.ITEM_CHECK_LAC, Integer.valueOf(portalInfo.lac));
                        String[] whereArgs = new String[]{portalInfo.currentSsid};
                        this.database.update(PortalDbHelper.TABLE_STANDARD_PORTAL_302, cv, "ssid=?", whereArgs);
                    } else {
                        cv.put(PortalDbHelper.ITEM_SSID, portalInfo.currentSsid);
                        cv.put(PortalDbHelper.ITEM_CHECK_TIMESTAMP, String.valueOf(portalInfo.timestamp));
                        cv.put(PortalDbHelper.ITEM_CHECK_LAC, Integer.valueOf(portalInfo.lac));
                        this.database.insert(PortalDbHelper.TABLE_STANDARD_PORTAL_302, null, cv);
                    }
                } catch (SQLiteException e) {
                    Log.w(TAG, "updateStandardPortalTable, SQLiteException happened");
                }
            }
        }
        return;
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0079 A:{SYNTHETIC, Splitter: B:25:0x0079} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void syncQueryStarndardPortalNetwork(StarndardPortalInfo portalInfo) {
        Cursor cursor = null;
        if (portalInfo != null) {
            if (!(portalInfo.currentSsid == null || this.database == null)) {
                try {
                    cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE4, null);
                    while (cursor.moveToNext()) {
                        if (portalInfo.currentSsid.equals(cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_SSID)))) {
                            String ts = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_CHECK_TIMESTAMP));
                            int lac = cursor.getInt(cursor.getColumnIndex(PortalDbHelper.ITEM_CHECK_LAC));
                            LOGD("syncQueryStarndardPortalNetwork, matched, currentSsid = " + portalInfo.currentSsid);
                            if (ts != null && ts.length() > 0) {
                                portalInfo.timestamp = Long.parseLong(ts);
                            }
                            if (lac > Integer.MIN_VALUE && lac < SmartcareConstants.INVALID) {
                                portalInfo.lac = lac;
                            }
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    }
                    if (cursor != null) {
                    }
                } catch (NumberFormatException e) {
                    Log.w(TAG, "syncQueryStarndardPortalNetwork, NumberFormatException happened");
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (SQLiteException e2) {
                    Log.w(TAG, "syncQueryStarndardPortalNetwork, SQLiteException happened");
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

    public synchronized void updateDhcpResultsByBssid(String currBssid, String dhcpResults) {
        if (!(currBssid == null || dhcpResults == null)) {
            if (this.database != null) {
                try {
                    long currRows = getCurrentRowNumber(PortalDbHelper.QUERY_TABLE5);
                    if (currRows != -1) {
                        boolean found = false;
                        Cursor cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE5, null);
                        while (cursor.moveToNext()) {
                            if (currBssid.equals(cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_BSSID)))) {
                                found = true;
                                break;
                            }
                        }
                        cursor.close();
                        ContentValues cv = new ContentValues();
                        if (found) {
                            cv.put(PortalDbHelper.ITEM_DHCP_RESULTS, String.valueOf(dhcpResults));
                            String[] whereArgs = new String[]{currBssid};
                            this.database.update(PortalDbHelper.TABLE_DHCP_RESULTS_INTERNET_OK, cv, "ssid=?", whereArgs);
                        } else if (currRows < TABLE5_MAX_SIZE) {
                            cv.put(PortalDbHelper.ITEM_BSSID, currBssid);
                            cv.put(PortalDbHelper.ITEM_DHCP_RESULTS, String.valueOf(dhcpResults));
                            this.database.insert(PortalDbHelper.TABLE_DHCP_RESULTS_INTERNET_OK, null, cv);
                        }
                    } else {
                        return;
                    }
                } catch (SQLiteException e) {
                    LOGW("updateDhcpResultsByBssid, SQLiteException msg happend.");
                }
            }
        }
        return;
    }

    public synchronized String syncQueryDhcpResultsByBssid(String currentBssid) {
        String dhcpResults;
        dhcpResults = null;
        if (currentBssid != null) {
            if (this.database != null) {
                try {
                    Cursor cursor = this.database.rawQuery(PortalDbHelper.QUERY_TABLE5, null);
                    while (cursor.moveToNext()) {
                        if (currentBssid.equals(cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_BSSID)))) {
                            String matchedDhcp = cursor.getString(cursor.getColumnIndex(PortalDbHelper.ITEM_DHCP_RESULTS));
                            if (matchedDhcp != null && matchedDhcp.length() > 0) {
                                dhcpResults = matchedDhcp;
                            }
                            cursor.close();
                        }
                    }
                    cursor.close();
                } catch (SQLiteException e) {
                    LOGW("syncQueryDhcpResultsByBssid, SQLiteException msg happend.");
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
