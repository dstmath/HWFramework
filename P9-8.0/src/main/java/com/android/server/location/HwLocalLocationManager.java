package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.ILocationManager;
import android.location.Location;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.wifipro.PortalDbHelper;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwLocalLocationManager implements IHwLocalLocationManager {
    private static final int AP_INVALID = 1;
    private static final int AP_VALID = 0;
    private static final String[] BSSID_COLUMNS = new String[]{COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_ACCURACY, COLUMN_AP_VALID, COLUMN_LOC_SOURC};
    private static final String BSSID_SELECTION = "id_hash=?";
    public static final String BSSID_TABLE = "bssID";
    private static final byte[] C2 = new byte[]{(byte) -89, (byte) 82, (byte) 3, (byte) 85, (byte) -88, (byte) -104, (byte) 57, (byte) -10, (byte) -103, (byte) 108, (byte) -88, (byte) 122, (byte) -38, (byte) -12, (byte) -55, (byte) -2};
    private static final String[] CELLID_COLUMNS = new String[]{COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_ACCURACY, COLUMN_LOC_SOURC};
    public static final String CELLID_TABLE = "cellID";
    private static final String CELL_RECENT_QUERY_SELECTION = "SELECT * FROM cell_fix_info ORDER BY time DESC LIMIT 1";
    private static final String CELL_SELECTION = "id_hash=? and areacode=?";
    private static final String COLUMN_ACCURACY = "accuracy";
    private static final String COLUMN_AP_VALID = "ap_valid";
    private static final String COLUMN_AREACODE = "areacode";
    private static final String COLUMN_BSSID_INFO = "wifi_bssid";
    private static final String COLUMN_CELL_INFO = "cellid";
    private static final String COLUMN_ID_HASH = "id_hash";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LOC_SOURC = "loc_source";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_PRESET = "perset";
    private static final String COLUMN_TIME = "time";
    public static final String CREATE_BSSID_TABLE = "CREATE TABLE IF NOT EXISTS  bssid_fix_info(wifi_bssid VARCHAR(128) , id_hash VARCHAR(128) PRIMARY KEY,ap_valid INT DEFAULT (0) , latitude VARCHAR(128), longitude VARCHAR(128), accuracy FLOAT, loc_source VARCHAR(32) ,perset INT DEFAULT (0) , time INT (64) )";
    public static final String CREATE_CELLID_TABLE = "CREATE TABLE IF NOT EXISTS  cell_fix_info(cellid VARCHAR(128) , id_hash VARCHAR(128) PRIMARY KEY,areacode INT (64) , latitude VARCHAR(128), longitude VARCHAR(128), accuracy FLOAT, loc_source VARCHAR(32) ,perset INT DEFAULT (0) , time INT (64) )";
    private static final int DATA_NO_PRESET = 0;
    private static final int DATA_PRESET = 1;
    public static final boolean DUG = true;
    private static final String EXTRA_KEY_LOC_SOURCE = "key_loc_source";
    private static final String EXTRA_KEY_LOC_TABLEID = "key_loc_tableID";
    private static final int FIX_TIME_OUT = 2000;
    private static final String INVAILD_BSSID = "00:00:00:00:00:00";
    private static final int INVAILD_CELLID = -1;
    public static final String LOCAL_PROVIDER = "local_database";
    public static final String LOCATION_DB_NAME = "local_location.db";
    private static final String MASTER_PASSWORD = getKey(HwLocalLocationProvider.C1, C2, HwLocalLocationDBHelper.C3);
    private static final int MAX_COLUMN = 10000;
    private static final int MAX_INTERVALTIME = 600000;
    public static final int MSG_REPORT_LOCATION = 1;
    private static final String PERMISSION_INJECT_LOCATION = "com.huawei.android.permission.INJECT_LOCATION";
    public static final String TABLE_BSSID_NAME = "bssid_fix_info";
    public static final String TABLE_CELLID_NAME = "cell_fix_info";
    public static final String TAG = "HwLocalLocationProvider";
    private static final int TWO_MINUTES = 120000;
    public static final int VERSION = 2;
    private static final String XML_PATH = "app_name";
    private static final String XML_ROOT_NAME = "time_priority";
    private static final String XML_TAG_NAME = "app_name";
    private BroadcastReceiver bootCompleteReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                HwLocalLocationManager.this.mTelephonyManager.listen(HwLocalLocationManager.this.mCellIdChangedListener, 16);
            }
        }
    };
    private volatile boolean isLocating;
    private volatile boolean isRegister;
    private CellIdChangedListener mCellIdChangedListener;
    private Context mContext;
    private volatile int mCurrentAreaCode;
    private Location mCurrentBestLocation;
    private volatile String mCurrentBssId;
    private volatile int mCurrentCellId;
    private volatile Handler mHandler;
    private Intent mInjectIntent;
    private IntentFilter mIntentFilter;
    private int mLastAreaCode;
    private String mLastBssId;
    private int mLastCellid;
    private HwLocalLocationDBHelper mLocalLocationDB;
    private volatile Message mMessage;
    private volatile LocalFixTask mQueryLocationTask;
    private Timer mQueryLocationTimer;
    private TelephonyManager mTelephonyManager;
    private List<String> mTimePriorityAPPNames;
    private WifiManager mWifiManager;
    private BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                NetworkInfo netinfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (netinfo != null && netinfo.isConnected()) {
                    HwLocalLocationManager.this.mCurrentBssId = intent.getStringExtra(PortalDbHelper.ITEM_BSSID);
                } else if (HwLocalLocationManager.this.mWifiManager.isWifiEnabled()) {
                    List<ScanResult> mlist = HwLocalLocationManager.this.mWifiManager.getScanResults();
                    if (mlist == null || (mlist.isEmpty() ^ 1) == 0 || mlist.get(0) == null) {
                        HwLocalLocationManager.this.mCurrentBssId = null;
                    } else {
                        HwLocalLocationManager.this.mCurrentBssId = ((ScanResult) mlist.get(0)).BSSID;
                    }
                } else {
                    HwLocalLocationManager.this.mCurrentBssId = null;
                }
            }
        }
    };

    private class CellIdChangedListener extends PhoneStateListener {
        /* synthetic */ CellIdChangedListener(HwLocalLocationManager this$0, CellIdChangedListener -this1) {
            this();
        }

        private CellIdChangedListener() {
        }

        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);
            if (HwLocalLocationManager.this.mTelephonyManager != null) {
                switch (HwLocalLocationManager.this.mTelephonyManager.getCurrentPhoneType()) {
                    case 1:
                        if (location instanceof GsmCellLocation) {
                            try {
                                GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
                                HwLocalLocationManager.this.mCurrentCellId = gsmCellLocation.getCid();
                                HwLocalLocationManager.this.mCurrentAreaCode = gsmCellLocation.getLac();
                                return;
                            } catch (Exception e) {
                                Log.e(HwLocalLocationManager.TAG, "GsmCellLocation Type Cast Exception :" + e.getMessage());
                                return;
                            }
                        }
                        return;
                    case 2:
                        if (location instanceof CdmaCellLocation) {
                            try {
                                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) location;
                                HwLocalLocationManager.this.mCurrentCellId = cdmaCellLocation.getBaseStationId();
                                HwLocalLocationManager.this.mCurrentAreaCode = cdmaCellLocation.getNetworkId();
                                return;
                            } catch (Exception e2) {
                                Log.e(HwLocalLocationManager.TAG, "CdmaCellLocation Type Cast Exception :" + e2.getMessage());
                                return;
                            }
                        }
                        return;
                    default:
                        HwLocalLocationManager.this.mCurrentCellId = -1;
                        HwLocalLocationManager.this.mCurrentAreaCode = -1;
                        return;
                }
            }
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
            if (serviceState.getState() != 0) {
                HwLocalLocationManager.this.mCurrentCellId = -1;
                HwLocalLocationManager.this.mCurrentAreaCode = -1;
            }
        }
    }

    class LocalFixTask extends AsyncTask<Void, Void, Location> {
        LocalFixTask() {
        }

        protected Location doInBackground(Void... params) {
            return HwLocalLocationManager.this.queryLocFormDB();
        }

        protected void onPostExecute(Location result) {
            super.onPostExecute(result);
            synchronized (HwLocalLocationManager.this) {
                if (HwLocalLocationManager.this.mQueryLocationTimer != null) {
                    HwLocalLocationManager.this.mQueryLocationTimer.cancel();
                    HwLocalLocationManager.this.mQueryLocationTimer = null;
                }
                if (result != null) {
                    Slog.d(HwLocalLocationManager.TAG, "has query Loc Form DB and send msg to Hander");
                    HwLocalLocationManager.this.mMessage = HwLocalLocationManager.this.mHandler.obtainMessage();
                    HwLocalLocationManager.this.mMessage.what = 1;
                    HwLocalLocationManager.this.mMessage.obj = result;
                    HwLocalLocationManager.this.mHandler.sendMessage(HwLocalLocationManager.this.mMessage);
                }
                HwLocalLocationManager.this.isLocating = false;
            }
            cancel(false);
        }
    }

    private final class LocalLocationHandler extends Handler {
        /* synthetic */ LocalLocationHandler(HwLocalLocationManager this$0, LocalLocationHandler -this1) {
            this();
        }

        private LocalLocationHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Location loc = msg.obj;
                    if (loc != null) {
                        HwLocalLocationManager.this.mInjectIntent = new Intent("action_inject_location");
                        HwLocalLocationManager.this.mInjectIntent.putExtra("key_location", loc);
                        HwLocalLocationManager.this.mContext.sendBroadcast(HwLocalLocationManager.this.mInjectIntent, HwLocalLocationManager.PERMISSION_INJECT_LOCATION);
                        Slog.d(HwLocalLocationManager.TAG, "sendBroadcast ACTION_INJECT_LOCATION");
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    class LocationTimerTask extends TimerTask {
        LocationTimerTask() {
        }

        public void run() {
            synchronized (HwLocalLocationManager.this) {
                if (HwLocalLocationManager.this.mQueryLocationTimer != null) {
                    HwLocalLocationManager.this.mQueryLocationTimer.cancel();
                    HwLocalLocationManager.this.mQueryLocationTimer = null;
                }
                if (!(HwLocalLocationManager.this.mQueryLocationTask == null || (HwLocalLocationManager.this.mQueryLocationTask.isCancelled() ^ 1) == 0)) {
                    HwLocalLocationManager.this.mQueryLocationTask.cancel(true);
                    HwLocalLocationManager.this.mQueryLocationTask = null;
                }
                HwLocalLocationManager.this.isLocating = false;
            }
        }
    }

    class RefreshBssIDDBTask extends AsyncTask<Void, Void, Location> {
        int ap_valid;
        String bssid;
        Location loc;

        public RefreshBssIDDBTask(Location loc, int ap_valid, String bssid) {
            this.loc = loc;
            this.bssid = bssid;
            this.ap_valid = ap_valid;
        }

        protected Location doInBackground(Void... params) {
            HwLocalLocationManager.this.refreshLocToBssTable(this.loc, this.ap_valid, this.bssid);
            return null;
        }

        protected void onPostExecute(Location result) {
            super.onPostExecute(result);
            cancel(false);
        }
    }

    class RefreshCellInfoDBTask extends AsyncTask<Void, Void, Location> {
        int cellid;
        Location loc;

        public RefreshCellInfoDBTask(Location loc, int cellid) {
            this.loc = loc;
            this.cellid = cellid;
        }

        protected Location doInBackground(Void... params) {
            HwLocalLocationManager.this.refreshLocToCellTable(this.loc, this.cellid);
            return null;
        }

        protected void onPostExecute(Location result) {
            super.onPostExecute(result);
            cancel(false);
        }
    }

    public HwLocalLocationManager(Context context, ILocationManager ilocationManager) {
        this.mContext = context;
        initialize();
    }

    public synchronized void initialize() {
        this.mCurrentCellId = -1;
        this.mCurrentAreaCode = -1;
        this.mCurrentBssId = null;
        this.mLastBssId = null;
        this.mLastCellid = -1;
        this.mLastAreaCode = -1;
        this.mHandler = new LocalLocationHandler(this, null);
        this.mTimePriorityAPPNames = new ArrayList();
        this.mLocalLocationDB = new HwLocalLocationDBHelper(this.mContext);
        initTimePriorityAPPNames();
        insertPersetData();
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mCellIdChangedListener = new CellIdChangedListener(this, null);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mIntentFilter = new IntentFilter("android.net.wifi.STATE_CHANGE");
        registerListen();
    }

    public synchronized void registerListen() {
        if (!this.isRegister) {
            this.mTelephonyManager.listen(this.mCellIdChangedListener, 16);
            this.mContext.registerReceiver(this.bootCompleteReceiver, new IntentFilter("android.intent.action.BOOT_COMPLETED"));
            this.mContext.registerReceiver(this.wifiBroadcastReceiver, this.mIntentFilter);
            this.isRegister = true;
        }
    }

    public synchronized void unregisterListen() {
        if (this.isRegister) {
            this.mTelephonyManager.listen(this.mCellIdChangedListener, 0);
            this.mContext.unregisterReceiver(this.bootCompleteReceiver);
            this.mContext.unregisterReceiver(this.wifiBroadcastReceiver);
            this.isRegister = false;
        }
    }

    public synchronized void requestLocation() {
        Slog.d(TAG, "HwLocalLocationManager requestLocation");
        if (this.isLocating) {
            Slog.d(TAG, "isLocating ,return");
        } else if (-1 == getCellID()) {
            Slog.d(TAG, "cellid is null,return");
        } else {
            this.isLocating = true;
            this.mQueryLocationTimer = new Timer();
            this.mQueryLocationTimer.schedule(new LocationTimerTask(), 2000);
            this.mQueryLocationTask = new LocalFixTask();
            this.mQueryLocationTask.execute(new Void[0]);
        }
    }

    public void closedb() {
        this.mLocalLocationDB.closedb();
    }

    public void updataLocationDB(Location loc) {
        if (loc == null || (loc.isComplete() ^ 1) != 0) {
            Slog.w(TAG, "loc is null or not complete, can not updata to DB");
        } else if (isValidLocation(loc) && !LOCAL_PROVIDER.equals(loc.getProvider())) {
            if (isBetterLocation(loc, this.mCurrentBestLocation)) {
                Slog.d(TAG, "this loc is Better than Last Location");
                if (this.mCurrentBestLocation == null) {
                    this.mCurrentBestLocation = new Location(loc);
                } else {
                    this.mCurrentBestLocation.set(loc);
                }
                this.mCurrentCellId = getCellID();
                if (-1 != this.mCurrentCellId) {
                    new RefreshCellInfoDBTask(loc, this.mCurrentCellId).execute(new Void[0]);
                }
                this.mCurrentBssId = getBSSID();
                String tempBssId = this.mCurrentBssId;
                if (!(tempBssId == null || (tempBssId.isEmpty() ^ 1) == 0)) {
                    new RefreshBssIDDBTask(loc, 0, tempBssId).execute(new Void[0]);
                }
            } else {
                if (isCellInfoChange()) {
                    Slog.d(TAG, "cellid has changed,RefreshCellInfoDB");
                    new RefreshCellInfoDBTask(loc, this.mCurrentCellId).execute(new Void[0]);
                }
                if (isBssIdChange()) {
                    Slog.d(TAG, "bssid has changed,RefreshBssIDDB");
                    new RefreshBssIDDBTask(loc, 0, this.mCurrentBssId).execute(new Void[0]);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x000f, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized boolean isCellInfoChange() {
        getCellID();
        if (-1 != this.mCurrentAreaCode && -1 != this.mLastAreaCode) {
            if (-1 != this.mLastCellid && -1 != this.mLastAreaCode && this.mLastCellid == this.mCurrentCellId && this.mLastAreaCode == this.mCurrentAreaCode) {
                return false;
            }
            this.mLastAreaCode = this.mCurrentAreaCode;
            this.mLastCellid = this.mCurrentCellId;
            return true;
        }
    }

    private boolean isBssIdChange() {
        this.mCurrentBssId = getBSSID();
        String tempBssId = this.mCurrentBssId;
        if (tempBssId == null || tempBssId.isEmpty()) {
            return false;
        }
        if (this.mLastBssId != null && this.mLastBssId.equals(tempBssId)) {
            return false;
        }
        this.mLastBssId = tempBssId;
        return true;
    }

    public boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > 120000;
        boolean isSignificantlyOlder = timeDelta < -120000;
        boolean isNewer = timeDelta > 0;
        if (isSignificantlyNewer) {
            return true;
        }
        if (isSignificantlyOlder) {
            return false;
        }
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 100;
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());
        if (isMoreAccurate) {
            return true;
        }
        if (isNewer && (isLessAccurate ^ 1) != 0) {
            return true;
        }
        if (isNewer && (isSignificantlyLessAccurate ^ 1) != 0 && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 != null) {
            return provider1.equals(provider2);
        }
        return provider2 == null;
    }

    private int getCellID() {
        return this.mCurrentCellId;
    }

    private synchronized String getBSSID() {
        if (!this.mWifiManager.isWifiEnabled()) {
            this.mCurrentBssId = null;
        } else if (this.mCurrentBssId == null) {
            WifiInfo info = this.mWifiManager.getConnectionInfo();
            if (info != null) {
                this.mCurrentBssId = info.getBSSID();
            } else {
                List<ScanResult> mlist = this.mWifiManager.getScanResults();
                if (mlist == null || (mlist.isEmpty() ^ 1) == 0 || mlist.get(0) == null) {
                    this.mCurrentBssId = null;
                } else {
                    this.mCurrentBssId = ((ScanResult) mlist.get(0)).BSSID;
                }
            }
        }
        return this.mCurrentBssId;
    }

    public boolean isValidLocation(Location loc) {
        return true;
    }

    private int getindex(String[] columns, String column) {
        if (columns != null && columns.length > 0) {
            for (int i = columns.length - 1; i >= 0; i--) {
                if (columns[i].equals(column)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void refreshLocToCellTable(Location loc, int cellid) {
        if (-1 != cellid) {
            if (isCellIdExist(cellid)) {
                Slog.d(TAG, "cellid is Exist, update  cellinfo to DB");
                updatetLocToCellIdTable(loc, cellid);
            } else {
                Slog.d(TAG, "cellid is not Exist, insert cellinfo to DB");
                if (10000 < getDBNumber(TABLE_CELLID_NAME)) {
                    Slog.e(TAG, "DB Number > 10000, first delete Top data");
                    deleteTopData(TABLE_CELLID_NAME);
                }
                insertLocToDB(TABLE_CELLID_NAME, loc, cellid + "", false);
            }
        }
    }

    private void refreshLocToBssTable(Location loc, int ap_valid, String id) {
        if (id != null && !id.isEmpty() && !id.equals(INVAILD_BSSID)) {
            if (ap_valid == 1) {
                updatetLocToBssIdTable(loc, id, ap_valid);
            }
            if (isBssidExist(id)) {
                updatetLocToBssIdTable(loc, id, 0);
            } else {
                if (10000 < getDBNumber(TABLE_BSSID_NAME)) {
                    deleteTopData(TABLE_BSSID_NAME);
                }
                insertLocToDB(TABLE_BSSID_NAME, loc, id, false);
            }
        }
    }

    private Location queryLocFormDB() {
        Slog.d(TAG, "start query Loc Form DB ");
        Location locCellID = queryLocFormCellIdTable(getCellID());
        if (locCellID != null) {
            Slog.i(TAG, "query Loc Form cellid");
            Location locBssID = queryLocFormBssIdTable(getBSSID());
            if (locBssID == null) {
                Slog.w(TAG, "query Loc Form bssid , locBssID = null.  return locCellID");
                return locCellID;
            } else if (locCellID.distanceTo(locBssID) < 5000.0f) {
                Slog.w(TAG, "location disrance < 5000 M == " + locCellID.distanceTo(locBssID) + " m");
                return locBssID;
            } else {
                Slog.w(TAG, "location disrance >5000 M == " + locCellID.distanceTo(locBssID) + " m");
                new RefreshBssIDDBTask(locBssID, 1, this.mCurrentBssId).execute(new Void[0]);
                return locCellID;
            }
        }
        Slog.w(TAG, "queryLoc cellid is  null");
        return null;
    }

    private int getDBNumber(String tablename) {
        Cursor cursor = null;
        try {
            cursor = this.mLocalLocationDB.rawQuery("SELECT COUNT (*) FROM " + tablename, null);
            if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return 0;
            }
            int count = cursor.getInt(0) + 1;
            Slog.i(TAG, tablename + " count is " + count);
            if (cursor != null) {
                cursor.close();
            }
            return count;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private boolean isCellIdExist(int id) {
        if (-1 == id || -1 == this.mCurrentAreaCode) {
            return false;
        }
        Cursor cursor = null;
        try {
            cursor = this.mLocalLocationDB.query(TABLE_CELLID_NAME, CELLID_COLUMNS, CELL_SELECTION, new String[]{Sha256Encrypt.Encrypt(id + "", ""), this.mCurrentAreaCode + ""});
            if (cursor == null || cursor.getCount() <= 0) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            if (cursor != null) {
                cursor.close();
            }
            return true;
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public Location queryLocFormRecentCell() {
        Slog.i(TAG, "query loc Form Recent Cell");
        Cursor cursor = null;
        try {
            cursor = this.mLocalLocationDB.rawQuery(CELL_RECENT_QUERY_SELECTION, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                String[] columnNames = cursor.getColumnNames();
                long intervalTime = System.currentTimeMillis() - cursor.getLong(getindex(columnNames, COLUMN_TIME));
                if (intervalTime > 0 && intervalTime < AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME) {
                    Location location = new Location(LOCAL_PROVIDER);
                    String encryptedLat = cursor.getString(getindex(BSSID_COLUMNS, COLUMN_LATITUDE));
                    String encryptedLong = cursor.getString(getindex(BSSID_COLUMNS, COLUMN_LONGITUDE));
                    String decryptedLat = AESLocalDbCrypto.decrypt(MASTER_PASSWORD, encryptedLat);
                    String decryptedLong = AESLocalDbCrypto.decrypt(MASTER_PASSWORD, encryptedLong);
                    double lat = Double.parseDouble(decryptedLat);
                    double longi = Double.parseDouble(decryptedLong);
                    location.setLatitude(lat);
                    location.setLongitude(longi);
                    location.setAccuracy(cursor.getFloat(getindex(columnNames, COLUMN_ACCURACY)));
                    location.setTime(System.currentTimeMillis());
                    location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                    Bundle b = new Bundle();
                    b.putString("key_loc_source", cursor.getString(getindex(columnNames, COLUMN_LOC_SOURC)));
                    b.putString("key_loc_tableID", CELLID_TABLE);
                    location.setExtras(b);
                    Slog.i(TAG, "queryLocFormRecentCell loc  == " + location.toString());
                    if (cursor != null) {
                        cursor.close();
                    }
                    return location;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public Location queryLocFormCellIdTable(int id) {
        Slog.i(TAG, "query loc Form Cell Table");
        if (-1 == id) {
            return queryLocFormRecentCell();
        }
        String cellidStrSHA256 = null;
        try {
            cellidStrSHA256 = Sha256Encrypt.Encrypt(id + "", "");
        } catch (Exception e) {
            Log.e(TAG, "query Exception :" + e.getMessage());
        }
        String[] selectionArgs = new String[]{cellidStrSHA256, this.mCurrentAreaCode + ""};
        Cursor cursor = null;
        try {
            cursor = this.mLocalLocationDB.query(TABLE_CELLID_NAME, CELLID_COLUMNS, CELL_SELECTION, selectionArgs);
            if (cursor == null || cursor.getCount() <= 0) {
                Location queryLocFormRecentCell = queryLocFormRecentCell();
                if (cursor != null) {
                    cursor.close();
                }
                return queryLocFormRecentCell;
            } else if (cursor.moveToFirst()) {
                Location loc = new Location(LOCAL_PROVIDER);
                String encryptedLat = cursor.getString(getindex(BSSID_COLUMNS, COLUMN_LATITUDE));
                String encryptedLong = cursor.getString(getindex(BSSID_COLUMNS, COLUMN_LONGITUDE));
                String decryptedLat = AESLocalDbCrypto.decrypt(MASTER_PASSWORD, encryptedLat);
                String decryptedLong = AESLocalDbCrypto.decrypt(MASTER_PASSWORD, encryptedLong);
                double lat = Double.parseDouble(decryptedLat);
                double longi = Double.parseDouble(decryptedLong);
                loc.setLatitude(lat);
                loc.setLongitude(longi);
                loc.setAccuracy(cursor.getFloat(getindex(CELLID_COLUMNS, COLUMN_ACCURACY)));
                loc.setTime(System.currentTimeMillis());
                loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                Bundle b = new Bundle();
                b.putString("key_loc_source", cursor.getString(getindex(CELLID_COLUMNS, COLUMN_LOC_SOURC)));
                b.putString("key_loc_tableID", CELLID_TABLE);
                loc.setExtras(b);
                Slog.d(TAG, "queryLocFormCellIdTable success");
                if (cursor != null) {
                    cursor.close();
                }
                return loc;
            } else {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
        } catch (Exception e2) {
            Log.e(TAG, "query Exception :" + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void insertLocToDB(String table, Location loc, String id, boolean isperset) {
        if (loc == null || (loc.isComplete() ^ 1) != 0) {
            Slog.w(TAG, "loc is null or not complete, can not insert to DB");
            return;
        }
        ContentValues values = new ContentValues();
        try {
            values.put(COLUMN_ID_HASH, Sha256Encrypt.Encrypt(id, ""));
            String defaultID = "";
            String encryptedLong = AESLocalDbCrypto.encrypt(MASTER_PASSWORD, loc.getLongitude() + "");
            String encryptedLat = AESLocalDbCrypto.encrypt(MASTER_PASSWORD, loc.getLatitude() + "");
            if (TABLE_CELLID_NAME.equals(table)) {
                values.put(COLUMN_CELL_INFO, defaultID);
                values.put(COLUMN_AREACODE, Integer.valueOf(this.mCurrentAreaCode));
            } else if (TABLE_BSSID_NAME.equals(table)) {
                values.put(COLUMN_BSSID_INFO, defaultID);
                values.put(COLUMN_AP_VALID, Integer.valueOf(0));
            } else {
                return;
            }
            if (isperset) {
                values.put(COLUMN_PRESET, Integer.valueOf(1));
            }
            values.put(COLUMN_ACCURACY, Float.valueOf(loc.getAccuracy()));
            values.put(COLUMN_LATITUDE, encryptedLat);
            values.put(COLUMN_LONGITUDE, encryptedLong);
            values.put(COLUMN_TIME, Long.valueOf(System.currentTimeMillis()));
            values.put(COLUMN_LOC_SOURC, loc.getProvider());
            int i = (this.mLocalLocationDB.insert(table, values) > 0 ? 1 : (this.mLocalLocationDB.insert(table, values) == 0 ? 0 : -1));
            Slog.d(TAG, "insertLocToDB success");
        } catch (Exception e) {
            Log.e(TAG, "query Exception :" + e.getMessage());
        }
    }

    private void deleteTopData(String table) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM ");
        sql.append(table);
        sql.append(" WHERE ");
        sql.append(COLUMN_TIME);
        sql.append(" = (SELECT ");
        sql.append(COLUMN_TIME);
        sql.append(" FROM ");
        sql.append(table);
        sql.append(" WHERE ");
        sql.append(COLUMN_PRESET);
        sql.append(" = ");
        sql.append(0);
        sql.append(" ORDER BY ");
        sql.append(COLUMN_TIME);
        sql.append(" ASC LIMIT 1 ) ");
        Slog.d(TAG, "deleteTopData sql = " + sql.toString());
        this.mLocalLocationDB.execSQL(sql.toString());
    }

    private void updatetLocToCellIdTable(Location loc, int id) {
        Slog.d(TAG, "update loc to cell database");
        try {
            String encryptedLong = AESLocalDbCrypto.encrypt(MASTER_PASSWORD, loc.getLongitude() + "");
            String encryptedLat = AESLocalDbCrypto.encrypt(MASTER_PASSWORD, loc.getLatitude() + "");
            String idStrSHA256 = Sha256Encrypt.Encrypt(id + "", "");
            ContentValues values = new ContentValues(3);
            values.put(COLUMN_ACCURACY, Float.valueOf(loc.getAccuracy()));
            values.put(COLUMN_LATITUDE, encryptedLat);
            values.put(COLUMN_LONGITUDE, encryptedLong);
            values.put(COLUMN_LOC_SOURC, loc.getProvider());
            values.put(COLUMN_TIME, Long.valueOf(System.currentTimeMillis()));
            this.mLocalLocationDB.update(TABLE_CELLID_NAME, values, CELL_SELECTION, new String[]{idStrSHA256 + "", this.mCurrentAreaCode + ""});
        } catch (Exception e) {
            Log.e(TAG, "query Exception :" + e.getMessage());
        }
    }

    private void updatetLocToBssIdTable(Location loc, String id, int ap_valid) {
        Slog.d(TAG, "update loc to bssid database");
        try {
            String encryptedLong = AESLocalDbCrypto.encrypt(MASTER_PASSWORD, loc.getLongitude() + "");
            String encryptedLat = AESLocalDbCrypto.encrypt(MASTER_PASSWORD, loc.getLatitude() + "");
            String idStrSHA256 = Sha256Encrypt.Encrypt(id, "");
            ContentValues values = new ContentValues();
            if (ap_valid == 1) {
                values.put(COLUMN_AP_VALID, Integer.valueOf(ap_valid));
            } else {
                values.put(COLUMN_ACCURACY, Float.valueOf(loc.getAccuracy()));
                values.put(COLUMN_LATITUDE, encryptedLat);
                values.put(COLUMN_LONGITUDE, encryptedLong);
            }
            values.put(COLUMN_LOC_SOURC, loc.getProvider());
            values.put(COLUMN_TIME, Long.valueOf(System.currentTimeMillis()));
            this.mLocalLocationDB.update(TABLE_BSSID_NAME, values, BSSID_SELECTION, new String[]{idStrSHA256});
        } catch (Exception e) {
            Log.e(TAG, "query Exception :" + e.getMessage());
        }
    }

    private boolean isBssidExist(String bssid) {
        if (bssid == null || bssid.isEmpty()) {
            Slog.w(TAG, "bssid is null,isBssidExist is false");
            return false;
        }
        String bssidStrSHA256 = Sha256Encrypt.Encrypt(bssid, "");
        Cursor cursor = null;
        try {
            cursor = this.mLocalLocationDB.query(TABLE_BSSID_NAME, BSSID_COLUMNS, BSSID_SELECTION, new String[]{bssidStrSHA256});
            if (cursor == null || cursor.getCount() <= 0) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            if (cursor != null) {
                cursor.close();
            }
            return true;
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private Location queryLocFormBssIdTable(String bssid) {
        if (bssid == null || bssid.isEmpty()) {
            Slog.w(TAG, "bssid is null,not query");
            return null;
        }
        Cursor cursor = null;
        try {
            String bssidStrSHA256 = Sha256Encrypt.Encrypt(bssid, "");
            cursor = this.mLocalLocationDB.query(TABLE_BSSID_NAME, BSSID_COLUMNS, BSSID_SELECTION, new String[]{bssidStrSHA256});
            if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            if (1.0f == cursor.getFloat(getindex(BSSID_COLUMNS, COLUMN_AP_VALID))) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            Location loc = new Location(LOCAL_PROVIDER);
            String encryptedLat = cursor.getString(getindex(BSSID_COLUMNS, COLUMN_LATITUDE));
            String encryptedLong = cursor.getString(getindex(BSSID_COLUMNS, COLUMN_LONGITUDE));
            String decryptedLat = AESLocalDbCrypto.decrypt(MASTER_PASSWORD, encryptedLat);
            String decryptedLong = AESLocalDbCrypto.decrypt(MASTER_PASSWORD, encryptedLong);
            double lat = Double.parseDouble(decryptedLat);
            double longi = Double.parseDouble(decryptedLong);
            loc.setLatitude(lat);
            loc.setLongitude(longi);
            loc.setAccuracy(cursor.getFloat(getindex(BSSID_COLUMNS, COLUMN_ACCURACY)));
            loc.setTime(System.currentTimeMillis());
            loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            Bundle b = new Bundle();
            b.putString("key_loc_source", cursor.getString(getindex(BSSID_COLUMNS, COLUMN_LOC_SOURC)));
            b.putString("key_loc_tableID", BSSID_TABLE);
            loc.setExtras(b);
            Slog.d(TAG, "queryLocFormBssIdTable success");
            if (cursor != null) {
                cursor.close();
            }
            return loc;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private List parserXML(String filepath) {
        if (filepath == null || filepath.isEmpty()) {
            return null;
        }
        List<String> list = new ArrayList();
        InputStream inputStream = null;
        try {
            inputStream = this.mContext.openFileInput(filepath);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, "utf-8");
            for (int type = parser.getEventType(); type != 1; type = parser.next()) {
                switch (type) {
                    case 2:
                        if (!"app_name".equals(parser.getName())) {
                            break;
                        }
                        type = parser.next();
                        list.add(parser.getText());
                        break;
                    default:
                        break;
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "FileNotFoundException");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    Log.e(TAG, e3.getMessage());
                }
            }
        } catch (XmlPullParserException e4) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e32) {
                    Log.e(TAG, e32.getMessage());
                }
            }
        } catch (IOException e5) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e322) {
                    Log.e(TAG, e322.getMessage());
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3222) {
                    Log.e(TAG, e3222.getMessage());
                }
            }
        }
        return list;
    }

    public byte getLocationRequestType(String appname) {
        if (appname == null || appname.isEmpty()) {
            return (byte) 0;
        }
        for (String str : this.mTimePriorityAPPNames) {
            if (appname.contains(str)) {
                Slog.w(TAG, appname + "location request is LOCATION_TYPE_TIME_PRIORITY");
                return (byte) 1;
            }
        }
        return (byte) -1;
    }

    private void insertPersetData() {
        this.mLocalLocationDB.beginTransaction();
        this.mLocalLocationDB.setTransactionSuccessful();
        this.mLocalLocationDB.endTransaction();
    }

    private void initTimePriorityAPPNames() {
        this.mTimePriorityAPPNames.add("Map");
        this.mTimePriorityAPPNames.add("map");
        this.mTimePriorityAPPNames.add("Weather");
        this.mTimePriorityAPPNames.add("weather");
        this.mTimePriorityAPPNames.add("camera");
        this.mTimePriorityAPPNames.add("Camera");
        this.mTimePriorityAPPNames.add("tencent");
        this.mTimePriorityAPPNames.add("mall");
        this.mTimePriorityAPPNames.add("UC");
        this.mTimePriorityAPPNames.add("weibo");
        this.mTimePriorityAPPNames.add("mall");
        this.mTimePriorityAPPNames.add("gallery");
        this.mTimePriorityAPPNames.add("Gallery");
        this.mTimePriorityAPPNames.add("location");
        this.mTimePriorityAPPNames.add("Location");
        this.mTimePriorityAPPNames.add(NetworkCheckerThread.SERVER_BAIDU);
        this.mTimePriorityAPPNames.add("gps");
        this.mTimePriorityAPPNames.add("navi");
        this.mTimePriorityAPPNames.add("Navi");
        this.mTimePriorityAPPNames.add("didi");
        this.mTimePriorityAPPNames.add("funcity");
        this.mTimePriorityAPPNames.add("news");
        this.mTimePriorityAPPNames.add("News");
    }

    private static String getKey(byte[] c1, byte[] c2, byte[] c3) {
        return new String(right(XOR(c1, left(XOR(c3, left(c2, 2)), 6)), 4), Charset.defaultCharset());
    }

    private static byte[] right(byte[] source, int count) {
        byte[] temp = (byte[]) source.clone();
        for (int i = 0; i < count; i++) {
            byte m = temp[temp.length - 1];
            for (int j = temp.length - 1; j > 0; j--) {
                temp[j] = temp[j - 1];
            }
            temp[0] = m;
        }
        return temp;
    }

    private static byte[] left(byte[] source, int count) {
        byte[] temp = (byte[]) source.clone();
        for (int i = 0; i < count; i++) {
            byte m = temp[0];
            for (int j = 0; j < temp.length - 1; j++) {
                temp[j] = temp[j + 1];
            }
            temp[temp.length - 1] = m;
        }
        return temp;
    }

    private static byte[] XOR(byte[] m, byte[] n) {
        byte[] temp = new byte[m.length];
        for (int i = 0; i < m.length; i++) {
            temp[i] = (byte) (m[i] ^ n[i]);
        }
        return temp;
    }
}
