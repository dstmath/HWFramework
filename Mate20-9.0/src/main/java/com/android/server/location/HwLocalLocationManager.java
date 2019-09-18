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
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.location.HwCryptoUtility;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
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
    private static final String[] BSSID_COLUMNS = {COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_ACCURACY, COLUMN_AP_VALID, COLUMN_LOC_SOURC};
    private static final String BSSID_SELECTION = "id_hash=?";
    public static final String BSSID_TABLE = "bssID";
    private static final byte[] C2 = {-89, 82, 3, 85, -88, -104, 57, -10, -103, 108, -88, 122, -38, -12, -55, -2};
    private static final String[] CELLID_COLUMNS = {COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_ACCURACY, COLUMN_LOC_SOURC};
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
    public static final String MASTER_PASSWORD = getKey(HwLocalLocationProvider.C1, C2, HwLocalLocationDBHelper.C3);
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
    /* access modifiers changed from: private */
    public volatile boolean isLocating;
    private volatile boolean isRegister;
    /* access modifiers changed from: private */
    public CellIdChangedListener mCellIdChangedListener;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public volatile int mCurrentAreaCode;
    private Location mCurrentBestLocation;
    /* access modifiers changed from: private */
    public volatile String mCurrentBssId;
    /* access modifiers changed from: private */
    public volatile int mCurrentCellId;
    /* access modifiers changed from: private */
    public volatile Handler mHandler;
    /* access modifiers changed from: private */
    public Intent mInjectIntent;
    private IntentFilter mIntentFilter;
    private int mLastAreaCode;
    private String mLastBssId;
    private int mLastCellid;
    private HwLocalLocationDBHelper mLocalLocationDB;
    /* access modifiers changed from: private */
    public volatile Message mMessage;
    /* access modifiers changed from: private */
    public volatile LocalFixTask mQueryLocationTask;
    /* access modifiers changed from: private */
    public Timer mQueryLocationTimer;
    /* access modifiers changed from: private */
    public TelephonyManager mTelephonyManager;
    private List<String> mTimePriorityAPPNames;
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;
    private BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                NetworkInfo netinfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (netinfo != null && netinfo.isConnected()) {
                    String unused = HwLocalLocationManager.this.mCurrentBssId = intent.getStringExtra(PortalDbHelper.ITEM_BSSID);
                } else if (!HwLocalLocationManager.this.mWifiManager.isWifiEnabled()) {
                    String unused2 = HwLocalLocationManager.this.mCurrentBssId = null;
                } else {
                    List<ScanResult> mlist = HwLocalLocationManager.this.mWifiManager.getScanResults();
                    if (mlist == null || mlist.isEmpty() || mlist.get(0) == null) {
                        String unused3 = HwLocalLocationManager.this.mCurrentBssId = null;
                    } else {
                        String unused4 = HwLocalLocationManager.this.mCurrentBssId = mlist.get(0).BSSID;
                    }
                }
            }
        }
    };

    private class CellIdChangedListener extends PhoneStateListener {
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
                                int unused = HwLocalLocationManager.this.mCurrentCellId = gsmCellLocation.getCid();
                                int unused2 = HwLocalLocationManager.this.mCurrentAreaCode = gsmCellLocation.getLac();
                                return;
                            } catch (Exception e) {
                                Log.e(HwLocalLocationManager.TAG, "GsmCellLocation Type Cast Exception :" + e.getMessage());
                                return;
                            }
                        } else {
                            return;
                        }
                    case 2:
                        if (location instanceof CdmaCellLocation) {
                            try {
                                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) location;
                                int unused3 = HwLocalLocationManager.this.mCurrentCellId = cdmaCellLocation.getBaseStationId();
                                int unused4 = HwLocalLocationManager.this.mCurrentAreaCode = cdmaCellLocation.getNetworkId();
                                return;
                            } catch (Exception e2) {
                                Log.e(HwLocalLocationManager.TAG, "CdmaCellLocation Type Cast Exception :" + e2.getMessage());
                                return;
                            }
                        } else {
                            return;
                        }
                    default:
                        int unused5 = HwLocalLocationManager.this.mCurrentCellId = -1;
                        int unused6 = HwLocalLocationManager.this.mCurrentAreaCode = -1;
                        return;
                }
            }
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
            if (serviceState.getState() != 0) {
                int unused = HwLocalLocationManager.this.mCurrentCellId = -1;
                int unused2 = HwLocalLocationManager.this.mCurrentAreaCode = -1;
            }
        }
    }

    class LocalFixTask extends AsyncTask<Void, Void, Location> {
        LocalFixTask() {
        }

        /* access modifiers changed from: protected */
        public Location doInBackground(Void... params) {
            return HwLocalLocationManager.this.queryLocFormDB();
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Location result) {
            super.onPostExecute(result);
            synchronized (HwLocalLocationManager.this) {
                if (HwLocalLocationManager.this.mQueryLocationTimer != null) {
                    HwLocalLocationManager.this.mQueryLocationTimer.cancel();
                    Timer unused = HwLocalLocationManager.this.mQueryLocationTimer = null;
                }
                if (result != null) {
                    Slog.d(HwLocalLocationManager.TAG, "has query Loc Form DB and send msg to Hander");
                    Message unused2 = HwLocalLocationManager.this.mMessage = HwLocalLocationManager.this.mHandler.obtainMessage();
                    HwLocalLocationManager.this.mMessage.what = 1;
                    HwLocalLocationManager.this.mMessage.obj = result;
                    HwLocalLocationManager.this.mHandler.sendMessage(HwLocalLocationManager.this.mMessage);
                }
                boolean unused3 = HwLocalLocationManager.this.isLocating = false;
            }
            cancel(false);
        }
    }

    private final class LocalLocationHandler extends Handler {
        private LocalLocationHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Location loc = (Location) msg.obj;
                if (loc != null) {
                    Intent unused = HwLocalLocationManager.this.mInjectIntent = new Intent("action_inject_location");
                    HwLocalLocationManager.this.mInjectIntent.putExtra("key_location", loc);
                    HwLocalLocationManager.this.mContext.sendBroadcast(HwLocalLocationManager.this.mInjectIntent, HwLocalLocationManager.PERMISSION_INJECT_LOCATION);
                    Slog.d(HwLocalLocationManager.TAG, "sendBroadcast ACTION_INJECT_LOCATION");
                }
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
                    Timer unused = HwLocalLocationManager.this.mQueryLocationTimer = null;
                }
                if (HwLocalLocationManager.this.mQueryLocationTask != null && !HwLocalLocationManager.this.mQueryLocationTask.isCancelled()) {
                    HwLocalLocationManager.this.mQueryLocationTask.cancel(true);
                    LocalFixTask unused2 = HwLocalLocationManager.this.mQueryLocationTask = null;
                }
                boolean unused3 = HwLocalLocationManager.this.isLocating = false;
            }
        }
    }

    class RefreshBssIDDBTask extends AsyncTask<Void, Void, Location> {
        int apValid;
        String bssId;
        Location loc;

        public RefreshBssIDDBTask(Location loc2, int apValid2, String bssId2) {
            this.loc = loc2;
            this.bssId = bssId2;
            this.apValid = apValid2;
        }

        /* access modifiers changed from: protected */
        public Location doInBackground(Void... params) {
            HwLocalLocationManager.this.refreshLocToBssTable(this.loc, this.apValid, this.bssId);
            return null;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Location result) {
            super.onPostExecute(result);
            cancel(false);
        }
    }

    class RefreshCellInfoDBTask extends AsyncTask<Void, Void, Location> {
        int cellId;
        Location loc;

        public RefreshCellInfoDBTask(Location loc2, int cellId2) {
            this.loc = loc2;
            this.cellId = cellId2;
        }

        /* access modifiers changed from: protected */
        public Location doInBackground(Void... params) {
            HwLocalLocationManager.this.refreshLocToCellTable(this.loc, this.cellId);
            return null;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Location result) {
            super.onPostExecute(result);
            cancel(false);
        }
    }

    public HwLocalLocationManager(Context context, ILocationManager iLocationManager) {
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
        this.mHandler = new LocalLocationHandler();
        this.mTimePriorityAPPNames = new ArrayList();
        this.mLocalLocationDB = new HwLocalLocationDBHelper(this.mContext);
        initTimePriorityAPPNames();
        insertPersetData();
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mCellIdChangedListener = new CellIdChangedListener();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
        this.mIntentFilter = new IntentFilter("android.net.wifi.STATE_CHANGE");
        this.mIntentFilter.addCategory("android.net.wifi.STATE_CHANGE@hwBrExpand@WifiNetStatus=WIFICON|WifiNetStatus=WIFIDSCON");
        registerListen();
    }

    public synchronized void registerListen() {
        Log.d(TAG, "registerListen, isRegister=" + this.isRegister);
        if (!this.isRegister) {
            this.mTelephonyManager.listen(this.mCellIdChangedListener, 16);
            this.mContext.registerReceiver(this.bootCompleteReceiver, new IntentFilter("android.intent.action.BOOT_COMPLETED"));
            this.mContext.registerReceiver(this.wifiBroadcastReceiver, this.mIntentFilter);
            this.isRegister = true;
        }
    }

    public synchronized void unregisterListen() {
        Log.d(TAG, "unregisterListen, isRegister=" + this.isRegister);
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
        if (loc == null || !loc.isComplete()) {
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
                if (tempBssId != null && !tempBssId.isEmpty()) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0031, code lost:
        return false;
     */
    private synchronized boolean isCellInfoChange() {
        getCellID();
        if (-1 != this.mCurrentAreaCode) {
            if (-1 != this.mLastAreaCode) {
                if (-1 != this.mLastCellid && -1 != this.mLastAreaCode && this.mLastCellid == this.mCurrentCellId && this.mLastAreaCode == this.mCurrentAreaCode) {
                    return false;
                }
                this.mLastAreaCode = this.mCurrentAreaCode;
                this.mLastCellid = this.mCurrentCellId;
                return true;
            }
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
        if (isNewer && !isLessAccurate) {
            return true;
        }
        if (!isNewer || isSignificantlyLessAccurate || !isFromSameProvider) {
            return false;
        }
        return true;
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
                if (mlist == null || mlist.isEmpty() || mlist.get(0) == null) {
                    this.mCurrentBssId = null;
                } else {
                    this.mCurrentBssId = mlist.get(0).BSSID;
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

    /* access modifiers changed from: private */
    public void refreshLocToCellTable(Location loc, int cellid) {
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

    /* access modifiers changed from: private */
    public void refreshLocToBssTable(Location loc, int apValid, String id) {
        if (id != null && !id.isEmpty() && !id.equals(INVAILD_BSSID)) {
            if (apValid == 1) {
                updatetLocToBssIdTable(loc, id, apValid);
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

    /* access modifiers changed from: private */
    public Location queryLocFormDB() {
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
        } else {
            Slog.w(TAG, "queryLoc cellid is  null");
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x004f, code lost:
        if (r1 != null) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0051, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0061, code lost:
        if (r1 == null) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0064, code lost:
        return 0;
     */
    private int getDBNumber(String tablename) {
        int count;
        Cursor cursor = null;
        try {
            cursor = this.mLocalLocationDB.rawQuery("SELECT COUNT (*) FROM " + tablename, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                Slog.i(TAG, tablename + " count is " + count);
                if (cursor != null) {
                    cursor.close();
                }
                return count;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0058, code lost:
        if (r3 != null) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005a, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0069, code lost:
        if (r3 == null) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x006c, code lost:
        return false;
     */
    private boolean isCellIdExist(int id) {
        if (-1 == id || -1 == this.mCurrentAreaCode) {
            return false;
        }
        String[] selectionArgs = {HwCryptoUtility.Sha256Encrypt.encrypt(id + "", ""), this.mCurrentAreaCode + ""};
        Cursor cursor = null;
        try {
            cursor = this.mLocalLocationDB.query(TABLE_CELLID_NAME, CELLID_COLUMNS, CELL_SELECTION, selectionArgs);
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor != null) {
                    cursor.close();
                }
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "query db error in isCellIdExist");
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:45:0x0110  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0118  */
    public Location queryLocFormRecentCell() {
        Cursor cursor;
        Slog.i(TAG, "query loc Form Recent Cell");
        Cursor cursor2 = null;
        try {
            cursor2 = this.mLocalLocationDB.rawQuery(CELL_RECENT_QUERY_SELECTION, null);
            if (cursor2 != null) {
                try {
                    if (cursor2.getCount() > 0 && cursor2.moveToFirst()) {
                        String[] columnNames = cursor2.getColumnNames();
                        long fixTime = cursor2.getLong(getindex(columnNames, COLUMN_TIME));
                        long intervalTime = System.currentTimeMillis() - fixTime;
                        if (intervalTime > 0 && intervalTime < AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME) {
                            Location loc = new Location(LOCAL_PROVIDER);
                            String encryptedLat = cursor2.getString(getindex(BSSID_COLUMNS, COLUMN_LATITUDE));
                            String encryptedLong = cursor2.getString(getindex(BSSID_COLUMNS, COLUMN_LONGITUDE));
                            String decryptedLat = HwCryptoUtility.AESLocalDbCrypto.decrypt(MASTER_PASSWORD, encryptedLat);
                            String decryptedLong = HwCryptoUtility.AESLocalDbCrypto.decrypt(MASTER_PASSWORD, encryptedLong);
                            double lat = Double.parseDouble(decryptedLat);
                            double longi = Double.parseDouble(decryptedLong);
                            loc.setLatitude(lat);
                            Cursor cursor3 = cursor2;
                            double longi2 = longi;
                            try {
                                loc.setLongitude(longi2);
                                double d = longi2;
                                cursor = cursor3;
                                try {
                                    loc.setAccuracy(cursor.getFloat(getindex(columnNames, COLUMN_ACCURACY)));
                                    long j = fixTime;
                                    loc.setTime(System.currentTimeMillis());
                                    loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                                    Bundle b = new Bundle();
                                    b.putString("key_loc_source", cursor.getString(getindex(columnNames, COLUMN_LOC_SOURC)));
                                    b.putString("key_loc_tableID", CELLID_TABLE);
                                    loc.setExtras(b);
                                    StringBuilder sb = new StringBuilder();
                                    String[] strArr = columnNames;
                                    sb.append("queryLocFormRecentCell loc  == ");
                                    sb.append(loc.toString());
                                    Slog.i(TAG, sb.toString());
                                    if (cursor != null) {
                                        cursor.close();
                                    }
                                    return loc;
                                } catch (Exception e) {
                                    e = e;
                                    cursor2 = cursor;
                                } catch (Throwable th) {
                                    th = th;
                                    if (cursor != null) {
                                    }
                                    throw th;
                                }
                            } catch (Exception e2) {
                                e = e2;
                                cursor2 = cursor3;
                                try {
                                    Log.e(TAG, e.getMessage());
                                    if (cursor2 != null) {
                                    }
                                    Cursor cursor4 = cursor2;
                                    return null;
                                } catch (Throwable th2) {
                                    th = th2;
                                    cursor = cursor2;
                                    if (cursor != null) {
                                        cursor.close();
                                    }
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                cursor = cursor3;
                                if (cursor != null) {
                                }
                                throw th;
                            }
                        }
                    }
                } catch (Exception e3) {
                    e = e3;
                    Cursor cursor5 = cursor2;
                    Log.e(TAG, e.getMessage());
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                    Cursor cursor42 = cursor2;
                    return null;
                } catch (Throwable th4) {
                    th = th4;
                    cursor = cursor2;
                    if (cursor != null) {
                    }
                    throw th;
                }
            }
            Cursor cursor6 = cursor2;
            if (cursor6 != null) {
                cursor6.close();
            }
        } catch (Exception e4) {
            e = e4;
            Log.e(TAG, e.getMessage());
            if (cursor2 != null) {
            }
            Cursor cursor422 = cursor2;
            return null;
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x010b, code lost:
        if (r6 != null) goto L_0x010d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x010d, code lost:
        r6.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0139, code lost:
        if (r6 == null) goto L_0x013c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x013d, code lost:
        return null;
     */
    public Location queryLocFormCellIdTable(int id) {
        int i = id;
        Slog.i(TAG, "query loc Form Cell Table");
        if (-1 == i) {
            return queryLocFormRecentCell();
        }
        String cellidStrSHA256 = null;
        try {
            cellidStrSHA256 = HwCryptoUtility.Sha256Encrypt.encrypt(i + "", "");
        } catch (Exception e) {
            Log.e(TAG, "query Exception :" + e.getMessage());
        }
        Cursor cursor = null;
        try {
            cursor = this.mLocalLocationDB.query(TABLE_CELLID_NAME, CELLID_COLUMNS, CELL_SELECTION, new String[]{cellidStrSHA256, this.mCurrentAreaCode + ""});
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
                String decryptedLat = HwCryptoUtility.AESLocalDbCrypto.decrypt(MASTER_PASSWORD, encryptedLat);
                String decryptedLong = HwCryptoUtility.AESLocalDbCrypto.decrypt(MASTER_PASSWORD, encryptedLong);
                double lat = Double.parseDouble(decryptedLat);
                double longi = Double.parseDouble(decryptedLong);
                loc.setLatitude(lat);
                loc.setLongitude(longi);
                loc.setAccuracy(cursor.getFloat(getindex(CELLID_COLUMNS, COLUMN_ACCURACY)));
                loc.setTime(System.currentTimeMillis());
                loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                Bundle b = new Bundle();
                String source = cursor.getString(getindex(CELLID_COLUMNS, COLUMN_LOC_SOURC));
                b.putString("key_loc_source", source);
                String str = source;
                b.putString("key_loc_tableID", CELLID_TABLE);
                loc.setExtras(b);
                Slog.d(TAG, "queryLocFormCellIdTable success");
                if (cursor != null) {
                    cursor.close();
                }
                return loc;
            }
        } catch (Exception e2) {
            Log.e(TAG, "query Exception :" + e2.getMessage());
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private void insertLocToDB(String table, Location loc, String id, boolean isperset) {
        if (loc == null || !loc.isComplete()) {
            Slog.w(TAG, "loc is null or not complete, can not insert to DB");
            return;
        }
        ContentValues values = new ContentValues();
        try {
            values.put(COLUMN_ID_HASH, HwCryptoUtility.Sha256Encrypt.encrypt(id, ""));
            String str = MASTER_PASSWORD;
            String encryptedLong = HwCryptoUtility.AESLocalDbCrypto.encrypt(str, loc.getLongitude() + "");
            String str2 = MASTER_PASSWORD;
            String encryptedLat = HwCryptoUtility.AESLocalDbCrypto.encrypt(str2, loc.getLatitude() + "");
            if (TABLE_CELLID_NAME.equals(table)) {
                values.put(COLUMN_CELL_INFO, "");
                values.put(COLUMN_AREACODE, Integer.valueOf(this.mCurrentAreaCode));
            } else if (TABLE_BSSID_NAME.equals(table)) {
                values.put(COLUMN_BSSID_INFO, "");
                values.put(COLUMN_AP_VALID, 0);
            } else {
                return;
            }
            if (isperset) {
                values.put(COLUMN_PRESET, 1);
            }
            values.put(COLUMN_ACCURACY, Float.valueOf(loc.getAccuracy()));
            values.put(COLUMN_LATITUDE, encryptedLat);
            values.put(COLUMN_LONGITUDE, encryptedLong);
            values.put(COLUMN_TIME, Long.valueOf(System.currentTimeMillis()));
            values.put(COLUMN_LOC_SOURC, loc.getProvider());
            this.mLocalLocationDB.insert(table, values);
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
            String str = MASTER_PASSWORD;
            String encryptedLong = HwCryptoUtility.AESLocalDbCrypto.encrypt(str, loc.getLongitude() + "");
            String str2 = MASTER_PASSWORD;
            String encryptedLat = HwCryptoUtility.AESLocalDbCrypto.encrypt(str2, loc.getLatitude() + "");
            String idStrSHA256 = HwCryptoUtility.Sha256Encrypt.encrypt(id + "", "");
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

    private void updatetLocToBssIdTable(Location loc, String id, int apValid) {
        Slog.d(TAG, "update loc to bssid database");
        try {
            String str = MASTER_PASSWORD;
            String encryptedLong = HwCryptoUtility.AESLocalDbCrypto.encrypt(str, loc.getLongitude() + "");
            String str2 = MASTER_PASSWORD;
            String encryptedLat = HwCryptoUtility.AESLocalDbCrypto.encrypt(str2, loc.getLatitude() + "");
            String idStrSHA256 = HwCryptoUtility.Sha256Encrypt.encrypt(id, "");
            ContentValues values = new ContentValues();
            if (apValid == 1) {
                values.put(COLUMN_AP_VALID, Integer.valueOf(apValid));
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

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0032, code lost:
        if (r2 != null) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0034, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0043, code lost:
        if (r2 == null) goto L_0x0046;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0046, code lost:
        return false;
     */
    private boolean isBssidExist(String bssid) {
        if (bssid == null || bssid.isEmpty()) {
            Slog.w(TAG, "bssid is null,isBssidExist is false");
            return false;
        }
        String bssidStrSHA256 = HwCryptoUtility.Sha256Encrypt.encrypt(bssid, "");
        Cursor cursor = null;
        try {
            cursor = this.mLocalLocationDB.query(TABLE_BSSID_NAME, BSSID_COLUMNS, BSSID_SELECTION, new String[]{bssidStrSHA256});
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor != null) {
                    cursor.close();
                }
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "query db error in isBssidExist");
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00d7, code lost:
        if (r4 != null) goto L_0x00d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00d9, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00e9, code lost:
        if (r4 == null) goto L_0x00ec;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00ed, code lost:
        return null;
     */
    private Location queryLocFormBssIdTable(String bssid) {
        String str = bssid;
        if (str == null || bssid.isEmpty()) {
            Slog.w(TAG, "bssid is null,not query");
            return null;
        }
        Cursor cursor = null;
        try {
            String bssidStrSHA256 = HwCryptoUtility.Sha256Encrypt.encrypt(str, "");
            cursor = this.mLocalLocationDB.query(TABLE_BSSID_NAME, BSSID_COLUMNS, BSSID_SELECTION, new String[]{bssidStrSHA256});
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                if (1.0f == cursor.getFloat(getindex(BSSID_COLUMNS, COLUMN_AP_VALID))) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return null;
                }
                Location loc = new Location(LOCAL_PROVIDER);
                String encryptedLat = cursor.getString(getindex(BSSID_COLUMNS, COLUMN_LATITUDE));
                String encryptedLong = cursor.getString(getindex(BSSID_COLUMNS, COLUMN_LONGITUDE));
                String decryptedLat = HwCryptoUtility.AESLocalDbCrypto.decrypt(MASTER_PASSWORD, encryptedLat);
                String decryptedLong = HwCryptoUtility.AESLocalDbCrypto.decrypt(MASTER_PASSWORD, encryptedLong);
                double lat = Double.parseDouble(decryptedLat);
                double longi = Double.parseDouble(decryptedLong);
                loc.setLatitude(lat);
                loc.setLongitude(longi);
                loc.setAccuracy(cursor.getFloat(getindex(BSSID_COLUMNS, COLUMN_ACCURACY)));
                loc.setTime(System.currentTimeMillis());
                loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                Bundle b = new Bundle();
                b.putString("key_loc_source", cursor.getString(getindex(BSSID_COLUMNS, COLUMN_LOC_SOURC)));
                String str2 = bssidStrSHA256;
                b.putString("key_loc_tableID", BSSID_TABLE);
                loc.setExtras(b);
                Slog.d(TAG, "queryLocFormBssIdTable success");
                if (cursor != null) {
                    cursor.close();
                }
                return loc;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [java.util.List, java.io.InputStream] */
    private List parserXML(String filepath) {
        ? r0 = 0;
        if (filepath == null || filepath.isEmpty()) {
            return r0;
        }
        List<String> list = new ArrayList<>();
        try {
            InputStream inputStream = this.mContext.openFileInput(filepath);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, "utf-8");
            for (int type = parser.getEventType(); type != 1; type = parser.next()) {
                if (type != 0) {
                    switch (type) {
                        case 2:
                            if ("app_name".equals(parser.getName())) {
                                int type2 = parser.next();
                                list.add(parser.getText());
                                break;
                            }
                            break;
                        case 3:
                            break;
                    }
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
            Log.e(TAG, "FileNotFoundException in parserXML");
            if (r0 != 0) {
                r0.close();
            }
        } catch (XmlPullParserException e3) {
            Log.e(TAG, "XmlPullParserException in parserXML");
            if (r0 != 0) {
                r0.close();
            }
        } catch (IOException e4) {
            Log.e(TAG, "FileNotFoundException in parserXML");
            if (r0 != 0) {
                r0.close();
            }
        } catch (Throwable th) {
            if (r0 != 0) {
                try {
                    r0.close();
                } catch (IOException e5) {
                    Log.e(TAG, e5.getMessage());
                }
            }
            throw th;
        }
        return list;
    }

    public byte getLocationRequestType(String appName) {
        if (appName == null || appName.isEmpty()) {
            return 0;
        }
        for (String str : this.mTimePriorityAPPNames) {
            if (appName.contains(str)) {
                Slog.w(TAG, appName + "location request is LOCATION_TYPE_TIME_PRIORITY");
                return 1;
            }
        }
        return -1;
    }

    private void insertPersetData() {
        try {
            this.mLocalLocationDB.beginTransaction();
            this.mLocalLocationDB.setTransactionSuccessful();
            this.mLocalLocationDB.endTransaction();
        } catch (Exception e) {
            Slog.e(TAG, "local_location.db abnormal, can't open or write!");
        }
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
        this.mTimePriorityAPPNames.add(HwNetworkPropertyChecker.NetworkCheckerThread.SERVER_BAIDU);
        this.mTimePriorityAPPNames.add("gps");
        this.mTimePriorityAPPNames.add("navi");
        this.mTimePriorityAPPNames.add("Navi");
        this.mTimePriorityAPPNames.add("didi");
        this.mTimePriorityAPPNames.add("funcity");
        this.mTimePriorityAPPNames.add("news");
        this.mTimePriorityAPPNames.add("News");
    }

    private static String getKey(byte[] c1, byte[] c2, byte[] c3) {
        return new String(right(xor(c1, left(xor(c3, left(c2, 2)), 6)), 4), Charset.defaultCharset());
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

    private static byte[] xor(byte[] m, byte[] n) {
        byte[] temp = new byte[m.length];
        for (int i = 0; i < m.length; i++) {
            temp[i] = (byte) (m[i] ^ n[i]);
        }
        return temp;
    }
}
