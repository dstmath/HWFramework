package com.android.server.location;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.IGnssStatusListener.Stub;
import android.location.ILocationManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import java.util.ArrayList;
import java.util.Iterator;

public class HwGpsActionReporter implements IHwGpsActionReporter {
    private static final int ACTION_LOC_REMOVE = 0;
    private static final int ACTION_LOC_REQUEST = 1;
    private static final String GPS_INJECT_LOCATION_PERMISSION = "com.huawei.android.permission.INJECT_LOCATION";
    private static final int GPS_POSITION_MODE_MS_BASED = 1;
    private static final int GPS_POSITION_MODE_STANDALONE = 0;
    private static final int GPS_START = 1;
    private static final int GPS_STOP = 0;
    private static final int GPS_SWITCH_OFF = 0;
    private static final int GPS_SWITCH_ON = 1;
    protected static final boolean HWFLOW;
    protected static final boolean HWLOGW_E = true;
    private static final int LOCATION_ACTION_REMOVE = 1;
    private static final int LOCATION_ACTION_REQUEST = 0;
    private static final int LOCATION_CHANGE_THRESHOLD_VALUE = 5000;
    private static final long LOCATION_INTERVAL = 0;
    private static final float LOCATION_MIN_DISTANCE = 0.0f;
    private static final int LOCATION_REPORT_GPSSETTING = 65002;
    private static final int LOCATION_REPORT_GPSSWITCH = 65001;
    private static final int LOCATION_REPORT_GPS_ACTION = 65004;
    private static final int LOCATION_REPORT_GPS_DISABLE = 0;
    private static final int LOCATION_REPORT_GPS_ENABLE = 1;
    private static final int LOCATION_REPORT_GPS_START = 1;
    private static final int LOCATION_REPORT_GPS_STOP = 0;
    private static final int LOCATION_REPORT_LOCATION_ACTION = 65003;
    private static final int LOCATION_REPORT_LOCATION_CHANGE = 65005;
    private static final int NO_FIX_TIMEOUT = 60000;
    private static final String TAG = "HwGpsActionReporter";
    private static final int VALID_DISTANCE_RANGE = SystemProperties.getInt("ro.config.invalid_distance", 10000);
    private static final int VALID_TIME_RANGE = SystemProperties.getInt("ro.config.invalid_time", AwareAppAssociate.ASSOC_DECAY_MIN_TIME);
    private static final Object mLock = new Object();
    private static volatile HwGpsActionReporter mSingleInstance = null;
    private int mCellID = -1;
    private ConnectivityManager mConnectivityManager = null;
    private Context mContext = null;
    private int mCurrentUserId = 0;
    private ArrayList<Location> mErrorNetworkLocations = new ArrayList();
    private Location mFirstLocation = null;
    private Location mGpsLastLocation = null;
    private int mGpsStatus = 0;
    private GpsStatusListenerTransport mGpsStatusListener = null;
    private ILocationManager mILocationManager;
    private Location mInjectLocation = null;
    private final BroadcastReceiver mInjectedLocationReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(HwGpsActionReporter.TAG, "intent is null");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                Log.e(HwGpsActionReporter.TAG, "action is null");
                return;
            }
            if (action.equals("action_inject_location")) {
                if (HwGpsActionReporter.HWFLOW) {
                    Log.i(HwGpsActionReporter.TAG, "receive inject location broadcast");
                }
                HwGpsActionReporter.this.processLocInjectInfo((Location) intent.getParcelableExtra("key_location"));
            } else if (action.equals("android.intent.action.USER_SWITCHED")) {
                if (HwGpsActionReporter.HWFLOW) {
                    Log.i(HwGpsActionReporter.TAG, "receive switch user broadcast");
                }
                HwGpsActionReporter.this.switchUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
            }
        }
    };
    private int mLastGpsSwitchStatus;
    private Location mLastLocation = null;
    private LocationManager mLocationManager;
    private String mMccMnc = "";
    private boolean mNetworkAvailable = false;
    private int mNetworkLocCorrCnt = 0;
    private int mNetworkLocErrCnt = 0;
    private int mNetworkType = 0;
    private PassiveLocationListener mPassiveLocationListener = null;
    private String mProvider = null;
    private HwReportTool mReportTool = null;
    private String mTableID = null;
    private TelephonyManager mTelephonyManager = null;
    private ArrayList<Location> mUncheckdNetworkLocations = new ArrayList();

    private class GpsStatusListenerTransport extends Stub {
        private double mDeltaDis;
        private int mSvCount;
        private int mTimeToFirstFix;

        /* synthetic */ GpsStatusListenerTransport(HwGpsActionReporter this$0, GpsStatusListenerTransport -this1) {
            this();
        }

        private GpsStatusListenerTransport() {
            this.mTimeToFirstFix = 0;
            this.mSvCount = 0;
            this.mDeltaDis = -1.0d;
        }

        public void onGnssStarted() {
            int positionMode;
            if (Global.getInt(HwGpsActionReporter.this.mContext.getContentResolver(), "assisted_gps_enabled", 1) != 0) {
                positionMode = 1;
            } else {
                positionMode = 0;
            }
            HwGpsActionReporter.this.reportGpsStarted(positionMode);
            HwGpsActionReporter.this.setGpsStatus(1);
        }

        public void onGnssStopped() {
            if (HwGpsActionReporter.this.mInjectLocation == null) {
                this.mDeltaDis = -1.0d;
                if (HwGpsActionReporter.HWFLOW) {
                    Log.i(HwGpsActionReporter.TAG, "mInjectLocation is null.");
                }
            } else if (HwGpsActionReporter.this.mFirstLocation != null) {
                this.mDeltaDis = (double) HwGpsActionReporter.this.mInjectLocation.distanceTo(HwGpsActionReporter.this.mFirstLocation);
            } else {
                this.mDeltaDis = -1.0d;
                if (HwGpsActionReporter.HWFLOW) {
                    Log.i(HwGpsActionReporter.TAG, "mFirstLocation is null.");
                }
            }
            HwGpsActionReporter.this.reportGpsStopped(this.mTimeToFirstFix, this.mSvCount, this.mDeltaDis);
            this.mTimeToFirstFix = 0;
            this.mSvCount = 0;
            HwGpsActionReporter.this.clearStateInfo();
            HwGpsActionReporter.this.setGpsStatus(0);
        }

        public void onFirstFix(int ttff) {
            this.mTimeToFirstFix = ttff;
        }

        public void onSvStatusChanged(int svCount, int[] prnWithFlags, float[] cn0s, float[] elevations, float[] azimuths, float[] carrierFreqs) {
            this.mSvCount = svCount;
        }

        public void onNmeaReceived(long timestamp, String nmea) {
        }
    }

    private class PassiveLocationListener implements LocationListener {
        /* synthetic */ PassiveLocationListener(HwGpsActionReporter this$0, PassiveLocationListener -this1) {
            this();
        }

        private PassiveLocationListener() {
        }

        public void onLocationChanged(Location location) {
            if (location == null) {
                Log.e(HwGpsActionReporter.TAG, "location is null");
                return;
            }
            boolean isGpsProvider = "gps".equals(location.getProvider());
            boolean isNetworkProvider = "network".equals(location.getProvider());
            boolean isFusedProvider = "fused".equals(location.getProvider());
            if (HwGpsActionReporter.HWFLOW) {
                Log.i(HwGpsActionReporter.TAG, "provider:" + location.getProvider() + ",acc is " + location.getAccuracy());
            }
            if (isGpsProvider) {
                if (HwGpsActionReporter.this.mFirstLocation == null) {
                    HwGpsActionReporter.this.mFirstLocation = location;
                }
                HwGpsActionReporter.this.mGpsLastLocation = location;
                if (HwGpsActionReporter.HWFLOW) {
                    Log.i(HwGpsActionReporter.TAG, "refresh mGpsLastLocation");
                }
            }
            if (isNetworkProvider) {
                HwGpsActionReporter.this.mUncheckdNetworkLocations.add(location);
                if (HwGpsActionReporter.HWFLOW) {
                    Log.i(HwGpsActionReporter.TAG, "add:" + Integer.toHexString(System.identityHashCode(location)));
                }
            }
            if (isGpsProvider || isNetworkProvider) {
                HwGpsActionReporter.this.removeExpiredNetworkLocations();
                HwGpsActionReporter.this.checkErrorNetworkLocations();
                HwGpsActionReporter.this.reportErrorNetworkLocations();
            }
            if (isGpsProvider || isNetworkProvider || isFusedProvider) {
                HwGpsActionReporter.this.reportLocationChange(location);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public static HwGpsActionReporter getInstance(Context context, ILocationManager iLocationManager) {
        if (mSingleInstance == null) {
            synchronized (mLock) {
                if (mSingleInstance == null) {
                    mSingleInstance = new HwGpsActionReporter(context, iLocationManager);
                }
            }
        }
        return mSingleInstance;
    }

    private HwGpsActionReporter(Context context, ILocationManager iLocationManager) {
        int i = 0;
        this.mContext = context;
        this.mILocationManager = iLocationManager;
        this.mReportTool = HwReportTool.getInstance(context);
        this.mGpsStatusListener = new GpsStatusListenerTransport(this, null);
        this.mPassiveLocationListener = new PassiveLocationListener(this, null);
        if (isGpsProviderSettingOn(this.mCurrentUserId)) {
            i = 1;
        }
        this.mLastGpsSwitchStatus = i;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action_inject_location");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(this.mInjectedLocationReceiver, intentFilter, GPS_INJECT_LOCATION_PERMISSION, null);
        try {
            if (!this.mILocationManager.registerGnssStatusCallback(this.mGpsStatusListener, this.mContext.getPackageName())) {
                Log.e(TAG, "addGpsStatusListener failed");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "addGpsStatusListener catch RemoteException", e);
        }
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        this.mLocationManager.requestLocationUpdates("passive", 0, LOCATION_MIN_DISTANCE, this.mPassiveLocationListener);
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("location_providers_allowed"), true, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                String providersSetting = HwGpsActionReporter.this.getProvidersSettingAllowed(HwGpsActionReporter.this.mCurrentUserId);
                int gpsSwitchStatus = HwGpsActionReporter.this.isGpsProviderSettingOn(HwGpsActionReporter.this.mCurrentUserId) ? 1 : 0;
                if (gpsSwitchStatus != HwGpsActionReporter.this.mLastGpsSwitchStatus) {
                    HwGpsActionReporter.this.reportGpsSwitch(gpsSwitchStatus);
                    HwGpsActionReporter.this.mLastGpsSwitchStatus = gpsSwitchStatus;
                }
                HwGpsActionReporter.this.reportProvidersSetting(providersSetting);
            }
        }, -1);
    }

    public boolean uploadLocationAction(int actionType, String actionMsg) {
        StringBuilder strBuilder = new StringBuilder("");
        if (HWFLOW) {
            Log.i(TAG, "uploadLocationAction:actionType:" + actionType + "actionMsg:" + actionMsg);
        }
        if (actionMsg == null) {
            return false;
        }
        boolean reportRst;
        switch (actionType) {
            case 0:
                reportRst = report(LOCATION_REPORT_LOCATION_ACTION, strBuilder.append("{RT:0").append(actionMsg).append("}").toString());
                break;
            case 1:
                reportRst = report(LOCATION_REPORT_LOCATION_ACTION, strBuilder.append("{RT:1").append(actionMsg).append("}").toString());
                break;
            default:
                Log.e(TAG, "unexpected action type:" + actionType);
                return false;
        }
        return reportRst;
    }

    private void reportGpsSwitch(int gpsSwitchStatus) {
        String reportString = "" + "{ACT:" + gpsSwitchStatus + ",RT:" + ((isGpsProviderSettingOn(this.mCurrentUserId) ? 1 : 0) == gpsSwitchStatus) + "}";
        if (HWFLOW) {
            Log.i(TAG, "reportGpsSwitch: " + reportString);
        }
        report(LOCATION_REPORT_GPSSWITCH, reportString);
    }

    private void reportProvidersSetting(String itemValue) {
        String reportString = "" + "{Mod:[" + itemValue + "]}";
        if (HWFLOW) {
            Log.i(TAG, "reportProvidersSetting: " + reportString);
        }
        report(LOCATION_REPORT_GPSSETTING, reportString);
    }

    private void reportGpsStarted(int positionMode) {
        String reportString = "" + "{GA:" + 1 + ",PAR1:" + positionMode + ",PAR2:" + 60000 + "}";
        if (HWFLOW) {
            Log.i(TAG, "reportGpsStarted: " + reportString);
        }
        report(LOCATION_REPORT_GPS_ACTION, reportString);
    }

    private void reportGpsStopped(int ttff, int svCount, double deltaDistance) {
        String reportString = "" + "{GA:" + 0 + ",PAR1:" + ttff + ",PAR2:" + svCount + ",PAR3:" + this.mProvider + ",PAR4:" + this.mTableID + ",PAR5:" + deltaDistance + "}";
        if (HWFLOW) {
            Log.i(TAG, "reportGpsStopped: " + reportString);
        }
        report(LOCATION_REPORT_GPS_ACTION, reportString);
    }

    private void reportLocationChange(Location location) {
        if (location != null && isLocationChange(location)) {
            refreshNetworkStatus();
            refreshCellStatus();
            String reportString = "" + "{CID:" + this.mCellID + ",MCCMNC:" + this.mMccMnc + ",LAT:" + location.getLatitude() + ",LON:" + location.getLongitude() + ",NA:" + this.mNetworkAvailable + ",NT:" + this.mNetworkType + "}";
            if (HWFLOW) {
                Log.i(TAG, "reportLocationChange:" + location);
            }
            report(LOCATION_REPORT_LOCATION_CHANGE, reportString);
        }
    }

    private void reportErrorNetworkLocations() {
        if (!this.mErrorNetworkLocations.isEmpty()) {
            refreshNetworkStatus();
            refreshCellStatus();
            if (HWFLOW) {
                Log.i(TAG, "mErrorNetworkLocations's size is: " + this.mErrorNetworkLocations.size());
            }
            Iterator<Location> unReportedLocations = this.mErrorNetworkLocations.iterator();
            while (unReportedLocations.hasNext()) {
                Location location = (Location) unReportedLocations.next();
                this.mNetworkLocErrCnt++;
                float corrRate = ((float) this.mNetworkLocCorrCnt) / ((float) (this.mNetworkLocErrCnt + this.mNetworkLocCorrCnt));
                String reportString = "" + "{CID:" + this.mCellID + ",MCCMNC:" + this.mMccMnc + ",LAT:" + location.getLatitude() + ",LON:" + location.getLongitude() + ",NA:" + this.mNetworkAvailable + ",NT:" + this.mNetworkType + ",RATE:" + corrRate + "}";
                if (HWFLOW) {
                    Log.i(TAG, "corrRate:" + corrRate);
                }
                report(LOCATION_REPORT_LOCATION_CHANGE, reportString);
                unReportedLocations.remove();
            }
        }
    }

    private void checkErrorNetworkLocations() {
        if (!this.mUncheckdNetworkLocations.isEmpty() && isReferenceLocationAvailable()) {
            if (HWFLOW) {
                Log.i(TAG, "checkErrorNetworkLocations mUncheckdNetworkLocations's size is: " + this.mUncheckdNetworkLocations.size());
            }
            Iterator<Location> uncheckedLocations = this.mUncheckdNetworkLocations.iterator();
            while (uncheckedLocations.hasNext()) {
                Location networkLocation = (Location) uncheckedLocations.next();
                if (networkLocation == null) {
                    Log.e(TAG, "removeExpiredNetworkLocations networkLocation is null");
                } else if (this.mGpsLastLocation.getAccuracy() - networkLocation.getAccuracy() <= LOCATION_MIN_DISTANCE) {
                    float distance = this.mGpsLastLocation.distanceTo(networkLocation);
                    if (distance > ((float) VALID_DISTANCE_RANGE)) {
                        this.mErrorNetworkLocations.add(networkLocation);
                    } else {
                        this.mNetworkLocCorrCnt++;
                    }
                    uncheckedLocations.remove();
                    if (HWFLOW) {
                        Log.i(TAG, "remove: " + Integer.toHexString(System.identityHashCode(networkLocation)) + ", reason: checked: " + distance + " meters");
                    }
                } else if (HWFLOW) {
                    Log.i(TAG, "input gpsLocation is less accuracy," + Integer.toHexString(System.identityHashCode(networkLocation)));
                }
            }
        }
    }

    private void removeExpiredNetworkLocations() {
        if (!this.mUncheckdNetworkLocations.isEmpty()) {
            long referenceTime = System.currentTimeMillis();
            if (HWFLOW) {
                Log.i(TAG, "removeExpiredNetworkLocations mUncheckdNetworkLocations's size is: " + this.mUncheckdNetworkLocations.size());
            }
            Iterator<Location> uncheckedLocations = this.mUncheckdNetworkLocations.iterator();
            while (uncheckedLocations.hasNext()) {
                Location location = (Location) uncheckedLocations.next();
                if (location == null) {
                    Log.e(TAG, "removeExpiredNetworkLocations network location is null");
                } else {
                    long diffTime = referenceTime - location.getTime();
                    if (diffTime > ((long) VALID_TIME_RANGE)) {
                        if (HWFLOW) {
                            Log.i(TAG, "remove: " + Integer.toHexString(System.identityHashCode(location)) + ", reason: expired: " + diffTime + "ms");
                        }
                        uncheckedLocations.remove();
                    }
                }
            }
        }
    }

    private boolean isReferenceLocationAvailable() {
        if (this.mGpsLastLocation == null) {
            if (HWFLOW) {
                Log.i(TAG, "mGpsLastLocation is null");
            }
            return false;
        }
        long diffTime = System.currentTimeMillis() - this.mGpsLastLocation.getTime();
        if (diffTime <= ((long) VALID_TIME_RANGE)) {
            return true;
        }
        if (HWFLOW) {
            Log.i(TAG, "gpslocation is expired:" + diffTime + "ms");
        }
        return false;
    }

    private boolean isLocationChange(Location location) {
        if (location == null) {
            Log.e(TAG, "location is null");
            return false;
        } else if (this.mLastLocation == null) {
            this.mLastLocation = location;
            return true;
        } else {
            float dis1 = location.distanceTo(this.mLastLocation);
            float dis2 = location.getAccuracy();
            boolean isChange = 5000.0f <= ((dis1 > dis2 ? 1 : (dis1 == dis2 ? 0 : -1)) > 0 ? dis1 : dis2);
            if (isChange) {
                this.mLastLocation = location;
            }
            return isChange;
        }
    }

    @SuppressLint({"NewApi"})
    private void refreshCellStatus() {
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        }
        if (this.mTelephonyManager != null) {
            this.mMccMnc = this.mTelephonyManager.getSimOperator();
            int type = this.mTelephonyManager.getCurrentPhoneType();
            CellLocation location = this.mTelephonyManager.getCellLocation();
            switch (type) {
                case 1:
                    if (location instanceof GsmCellLocation) {
                        try {
                            this.mCellID = ((GsmCellLocation) location).getCid();
                            return;
                        } catch (Exception e) {
                            Log.e(TAG, "GsmCellLocation Type Cast Exception :" + e.getMessage());
                            return;
                        }
                    }
                    return;
                case 2:
                    if (location instanceof CdmaCellLocation) {
                        try {
                            this.mCellID = ((CdmaCellLocation) location).getBaseStationId();
                            return;
                        } catch (Exception e2) {
                            Log.e(TAG, "CdmaCellLocation Type Cast Exception :" + e2.getMessage());
                            return;
                        }
                    }
                    return;
                default:
                    this.mCellID = -1;
                    Log.e(TAG, "unexpected phone type:" + type);
                    return;
            }
        }
        Log.e(TAG, "mTelephonyManager is null");
    }

    private void refreshNetworkStatus() {
        if (this.mConnectivityManager == null) {
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        NetworkInfo networkInfo = null;
        if (this.mConnectivityManager != null) {
            networkInfo = this.mConnectivityManager.getActiveNetworkInfo();
        }
        if (networkInfo != null) {
            this.mNetworkAvailable = networkInfo.isAvailable();
            this.mNetworkType = networkInfo.getSubtype();
        }
    }

    private void processLocInjectInfo(Location location) {
        if (location == null) {
            if (HWFLOW) {
                Log.i(TAG, "processLocInjectInfo: location is null");
            }
        } else if (this.mGpsStatus == 0) {
            if (HWFLOW) {
                Log.i(TAG, "processLocInjectInfo: gps does not start");
            }
        } else if (this.mFirstLocation != null) {
            if (HWFLOW) {
                Log.i(TAG, "processLocInjectInfo: gps has already fixed");
            }
        } else {
            this.mInjectLocation = location;
            Bundle b = location.getExtras();
            if (b != null) {
                this.mProvider = b.getString(HwLocalLocationProvider.KEY_LOC_SOURCE);
                this.mTableID = b.getString(HwLocalLocationProvider.KEY_LOC_TABLEID);
            }
        }
    }

    private boolean report(int actionID, String actionMsg) {
        if (this.mReportTool != null) {
            return this.mReportTool.report(actionID, actionMsg);
        }
        return false;
    }

    void setGpsStatus(int gpsStatus) {
        if (1 == gpsStatus || gpsStatus == 0) {
            this.mGpsStatus = gpsStatus;
        } else if (HWFLOW) {
            Log.i(TAG, "unexpceted gpsStatus:" + gpsStatus);
        }
    }

    void clearStateInfo() {
        this.mInjectLocation = null;
        this.mFirstLocation = null;
        this.mTableID = null;
        this.mProvider = null;
    }

    void switchUser(int userId) {
        if (this.mCurrentUserId != userId) {
            this.mCurrentUserId = userId;
            this.mLastGpsSwitchStatus = isGpsProviderSettingOn(userId) ? 1 : 0;
        }
    }

    boolean isGpsProviderSettingOn(int userId) {
        boolean isGpsProviderOn = false;
        try {
            return Secure.isLocationProviderEnabledForUser(this.mContext.getContentResolver(), "gps", userId);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "illegal userId for isLocationProviderEnabledForUser:" + userId);
            return isGpsProviderOn;
        }
    }

    boolean isNetworkProviderSettingOn(int userId) {
        boolean isNetworkProviderOn = false;
        try {
            return Secure.isLocationProviderEnabledForUser(this.mContext.getContentResolver(), "network", userId);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "illegal userId for isLocationProviderEnabledForUser:" + userId);
            return isNetworkProviderOn;
        }
    }

    String getProvidersSettingAllowed(int userId) {
        return Secure.getStringForUser(this.mContext.getContentResolver(), "location_providers_allowed", userId).replace("0,", "").replace("0", "");
    }
}
