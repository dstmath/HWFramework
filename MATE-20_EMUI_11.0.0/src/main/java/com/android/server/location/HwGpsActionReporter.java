package com.android.server.location;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.IGnssStatusListener;
import android.location.ILocationManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import com.android.server.intellicom.common.SmartDualCardConsts;
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
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int LOCATION_ACTION_REMOVE = 1;
    private static final int LOCATION_ACTION_REQUEST = 0;
    private static final int LOCATION_CHANGE_THRESHOLD_VALUE = 5000;
    private static final long LOCATION_INTERVAL_ZEROS = 0;
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
    private static final Object LOCK = new Object();
    private static final int NO_FIX_TIMEOUT = 60000;
    private static final String TAG = "HwGpsActionReporter";
    private static final int VALID_DISTANCE_RANGE = SystemProperties.getInt("ro.config.invalid_distance", 10000);
    private static final int VALID_TIME_RANGE = SystemProperties.getInt("ro.config.invalid_time", 120000);
    private static volatile HwGpsActionReporter sSingleInstance = null;
    private int mCellId = -1;
    private ConnectivityManager mConnectivityManager = null;
    private Context mContext = null;
    private int mCurrentUserId = 0;
    private ArrayList<Location> mErrorNetworkLocations = new ArrayList<>();
    private Location mFirstLocation = null;
    private Location mGpsLastLocation = null;
    private int mGpsStatus = 0;
    private GpsStatusListenerTransport mGpsStatusListener = null;
    private ILocationManager mILocationManager;
    private Location mInjectLocation = null;
    private final BroadcastReceiver mInjectedLocationReceiver = new BroadcastReceiver() {
        /* class com.android.server.location.HwGpsActionReporter.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                LBSLog.e(HwGpsActionReporter.TAG, false, "intent is null", new Object[0]);
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                LBSLog.e(HwGpsActionReporter.TAG, false, "action is null", new Object[0]);
            } else if ("action_inject_location".equals(action)) {
                if (HwGpsActionReporter.HWFLOW) {
                    LBSLog.i(HwGpsActionReporter.TAG, false, "receive inject location broadcast", new Object[0]);
                }
                HwGpsActionReporter.this.processLocInjectInfo((Location) intent.getParcelableExtra("key_location"));
            } else if (action.equals(SmartDualCardConsts.SYSTEM_STATE_ACTION_USER_SWITCHED)) {
                if (HwGpsActionReporter.HWFLOW) {
                    LBSLog.i(HwGpsActionReporter.TAG, false, "receive switch user broadcast", new Object[0]);
                }
                HwGpsActionReporter.this.switchUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
            } else if (HwGpsActionReporter.HWFLOW) {
                LBSLog.i(HwGpsActionReporter.TAG, false, "no match broadcast", new Object[0]);
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
    private String mTableId = null;
    private TelephonyManager mTelephonyManager = null;
    private ArrayList<Location> mUncheckdNetworkLocations = new ArrayList<>();

    public static HwGpsActionReporter getInstance(Context context, ILocationManager iLocationManager) {
        if (sSingleInstance == null) {
            synchronized (LOCK) {
                if (sSingleInstance == null) {
                    sSingleInstance = new HwGpsActionReporter(context, iLocationManager);
                }
            }
        }
        return sSingleInstance;
    }

    private HwGpsActionReporter(Context context, ILocationManager locationManager) {
        this.mContext = context;
        this.mILocationManager = locationManager;
        this.mReportTool = HwReportTool.getInstance(context);
        this.mGpsStatusListener = new GpsStatusListenerTransport();
        this.mPassiveLocationListener = new PassiveLocationListener();
        this.mLastGpsSwitchStatus = isGpsProviderSettingOn(this.mCurrentUserId) ? 1 : 0;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action_inject_location");
        intentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_ACTION_USER_SWITCHED);
        this.mContext.registerReceiver(this.mInjectedLocationReceiver, intentFilter, GPS_INJECT_LOCATION_PERMISSION, null);
        try {
            if (!this.mILocationManager.registerGnssStatusCallback(this.mGpsStatusListener, this.mContext.getPackageName())) {
                LBSLog.e(TAG, false, "addGpsStatusListener failed", new Object[0]);
            }
        } catch (RemoteException e) {
            LBSLog.e(TAG, false, "addGpsStatusListener catch RemoteException", new Object[0]);
        }
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        this.mLocationManager.requestLocationUpdates("passive", 0, 0.0f, this.mPassiveLocationListener);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("location_providers_allowed"), true, new ContentObserver(null) {
            /* class com.android.server.location.HwGpsActionReporter.AnonymousClass2 */

            /* JADX WARN: Type inference failed for: r1v2, types: [boolean, int] */
            /* JADX WARNING: Unknown variable types count: 1 */
            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                HwGpsActionReporter hwGpsActionReporter = HwGpsActionReporter.this;
                String providersSetting = hwGpsActionReporter.getProvidersSettingAllowed(hwGpsActionReporter.mCurrentUserId);
                HwGpsActionReporter hwGpsActionReporter2 = HwGpsActionReporter.this;
                ?? isGpsProviderSettingOn = hwGpsActionReporter2.isGpsProviderSettingOn(hwGpsActionReporter2.mCurrentUserId);
                if (isGpsProviderSettingOn != HwGpsActionReporter.this.mLastGpsSwitchStatus) {
                    HwGpsActionReporter.this.reportGpsSwitch(isGpsProviderSettingOn == true ? 1 : 0);
                    HwGpsActionReporter.this.mLastGpsSwitchStatus = isGpsProviderSettingOn;
                }
                HwGpsActionReporter.this.reportProvidersSetting(providersSetting);
            }
        }, -1);
    }

    private class GpsStatusListenerTransport extends IGnssStatusListener.Stub {
        private double mDeltaDis;
        private int mSvCount;
        private int mTimeToFirstFix;

        private GpsStatusListenerTransport() {
            this.mTimeToFirstFix = 0;
            this.mSvCount = 0;
            this.mDeltaDis = -1.0d;
        }

        public void onGnssStarted() {
            int positionMode;
            if (Settings.Global.getInt(HwGpsActionReporter.this.mContext.getContentResolver(), "assisted_gps_enabled", 1) != 0) {
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
                    LBSLog.i(HwGpsActionReporter.TAG, false, "mInjectLocation is null.", new Object[0]);
                }
            } else if (HwGpsActionReporter.this.mFirstLocation != null) {
                this.mDeltaDis = (double) HwGpsActionReporter.this.mInjectLocation.distanceTo(HwGpsActionReporter.this.mFirstLocation);
            } else {
                this.mDeltaDis = -1.0d;
                if (HwGpsActionReporter.HWFLOW) {
                    LBSLog.i(HwGpsActionReporter.TAG, false, "mFirstLocation is null.", new Object[0]);
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
        private PassiveLocationListener() {
        }

        @Override // android.location.LocationListener
        public void onLocationChanged(Location location) {
            if (location == null) {
                LBSLog.e(HwGpsActionReporter.TAG, false, "location is null", new Object[0]);
                return;
            }
            boolean isGpsProvider = "gps".equals(location.getProvider());
            boolean isNetworkProvider = "network".equals(location.getProvider());
            boolean isFusedProvider = "fused".equals(location.getProvider());
            if (HwGpsActionReporter.HWFLOW) {
                LBSLog.i(HwGpsActionReporter.TAG, false, "provider:%{public}s ,acc is %{public}s", location.getProvider(), Float.valueOf(location.getAccuracy()));
            }
            if (isGpsProvider) {
                if (HwGpsActionReporter.this.mFirstLocation == null) {
                    HwGpsActionReporter.this.mFirstLocation = location;
                }
                HwGpsActionReporter.this.mGpsLastLocation = location;
                if (HwGpsActionReporter.HWFLOW) {
                    LBSLog.i(HwGpsActionReporter.TAG, false, "refresh mGpsLastLocation", new Object[0]);
                }
            }
            if (isNetworkProvider) {
                HwGpsActionReporter.this.mUncheckdNetworkLocations.add(location);
                if (HwGpsActionReporter.HWFLOW) {
                    LBSLog.i(HwGpsActionReporter.TAG, false, "add: %{public}s", Integer.toHexString(System.identityHashCode(location)));
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

        @Override // android.location.LocationListener
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override // android.location.LocationListener
        public void onProviderEnabled(String provider) {
        }

        @Override // android.location.LocationListener
        public void onProviderDisabled(String provider) {
        }
    }

    public boolean uploadLocationAction(int actionType, String actionMsg) {
        StringBuilder strBuilder = new StringBuilder("");
        if (HWFLOW) {
            LBSLog.i(TAG, false, "uploadLocationAction:actionType:%{public}d actionMsg: %{public}s", Integer.valueOf(actionType), actionMsg);
        }
        if (actionMsg == null) {
            return false;
        }
        if (actionType == 0) {
            strBuilder.append("{RT:0");
            strBuilder.append(actionMsg);
            strBuilder.append("}");
            return report(LOCATION_REPORT_LOCATION_ACTION, strBuilder.toString());
        } else if (actionType != 1) {
            LBSLog.e(TAG, false, "unexpected action type: %{public}d", Integer.valueOf(actionType));
            return false;
        } else {
            strBuilder.append("{RT:1");
            strBuilder.append(actionMsg);
            strBuilder.append("}");
            return report(LOCATION_REPORT_LOCATION_ACTION, strBuilder.toString());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportGpsSwitch(int gpsSwitchStatus) {
        boolean isCheckResult = isGpsProviderSettingOn(this.mCurrentUserId) == gpsSwitchStatus;
        String reportString = "{ACT:" + gpsSwitchStatus + ",RT:" + isCheckResult + "}";
        if (HWFLOW) {
            LBSLog.i(TAG, false, "reportGpsSwitch: { ACT:%{public}d,RT:%{public}b }", Integer.valueOf(gpsSwitchStatus), Boolean.valueOf(isCheckResult));
        }
        report(LOCATION_REPORT_GPSSWITCH, reportString);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportProvidersSetting(String itemValue) {
        String reportString = "{Mod:[" + itemValue + "]}";
        if (HWFLOW) {
            LBSLog.i(TAG, false, "reportProvidersSetting: {Mod:[%{public}s]}", itemValue);
        }
        report(LOCATION_REPORT_GPSSETTING, reportString);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportGpsStarted(int positionMode) {
        String reportString = "{GA:1,PAR1:" + positionMode + ",PAR2:60000}";
        if (HWFLOW) {
            LBSLog.i(TAG, false, "reportGpsStarted: {GA:%{public}d,PAR1:%{public}d,PAR2:%{public}d}", 1, Integer.valueOf(positionMode), 60000);
        }
        report(LOCATION_REPORT_GPS_ACTION, reportString);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportGpsStopped(int ttff, int svCount, double deltaDistance) {
        String reportString = "{GA:0,PAR1:" + ttff + ",PAR2:" + svCount + ",PAR3:" + this.mProvider + ",PAR4:" + this.mTableId + ",PAR5:" + deltaDistance + "}";
        if (HWFLOW) {
            LBSLog.i(TAG, false, "reportGpsStopped: {GA:%{public}d, %{public}d, %{public}d, %{public}s, %{public}s, %{public}b }", 0, Integer.valueOf(ttff), Integer.valueOf(svCount), this.mProvider, this.mTableId, Double.valueOf(deltaDistance));
        }
        report(LOCATION_REPORT_GPS_ACTION, reportString);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportLocationChange(Location location) {
        if (location != null && isLocationChange(location)) {
            refreshNetworkStatus();
            refreshCellStatus();
            String reportString = "{CID:" + this.mCellId + ",MCCMNC:" + this.mMccMnc + ",LAT:" + location.getLatitude() + ",LON:" + location.getLongitude() + ",NA:" + this.mNetworkAvailable + ",NT:" + this.mNetworkType + "}";
            if (HWFLOW) {
                LBSLog.i(TAG, false, "reportLocationChange: %{public}s", location);
            }
            report(LOCATION_REPORT_LOCATION_CHANGE, reportString);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportErrorNetworkLocations() {
        if (!this.mErrorNetworkLocations.isEmpty()) {
            refreshNetworkStatus();
            refreshCellStatus();
            if (HWFLOW) {
                LBSLog.i(TAG, false, "mErrorNetworkLocations's size is: %{public}d", Integer.valueOf(this.mErrorNetworkLocations.size()));
            }
            Iterator<Location> unReportedLocations = this.mErrorNetworkLocations.iterator();
            while (unReportedLocations.hasNext()) {
                Location location = unReportedLocations.next();
                this.mNetworkLocErrCnt++;
                int i = this.mNetworkLocCorrCnt;
                float corrRate = ((float) i) / ((float) (this.mNetworkLocErrCnt + i));
                String reportString = "{CID:" + this.mCellId + ",MCCMNC:" + this.mMccMnc + ",LAT:" + location.getLatitude() + ",LON:" + location.getLongitude() + ",NA:" + this.mNetworkAvailable + ",NT:" + this.mNetworkType + ",RATE:" + corrRate + "}";
                if (HWFLOW) {
                    LBSLog.i(TAG, false, "corrRate:%{public}f", Float.valueOf(corrRate));
                }
                report(LOCATION_REPORT_LOCATION_CHANGE, reportString);
                unReportedLocations.remove();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkErrorNetworkLocations() {
        if (!this.mUncheckdNetworkLocations.isEmpty() && isReferenceLocationAvailable()) {
            if (HWFLOW) {
                LBSLog.i(TAG, false, "checkErrorNetworkLocations mUncheckdNetworkLocations's size is: %{public}d", Integer.valueOf(this.mUncheckdNetworkLocations.size()));
            }
            Iterator<Location> uncheckedLocations = this.mUncheckdNetworkLocations.iterator();
            while (uncheckedLocations.hasNext()) {
                Location networkLocation = uncheckedLocations.next();
                if (networkLocation == null) {
                    LBSLog.e(TAG, false, "removeExpiredNetworkLocations networkLocation is null", new Object[0]);
                } else {
                    String locationHashCode = Integer.toHexString(System.identityHashCode(networkLocation));
                    if (this.mGpsLastLocation.getAccuracy() - networkLocation.getAccuracy() <= 0.0f) {
                        float distance = this.mGpsLastLocation.distanceTo(networkLocation);
                        if (distance > ((float) VALID_DISTANCE_RANGE)) {
                            this.mErrorNetworkLocations.add(networkLocation);
                        } else {
                            this.mNetworkLocCorrCnt++;
                        }
                        uncheckedLocations.remove();
                        if (HWFLOW) {
                            LBSLog.i(TAG, false, "remove: %{public}s, reason: checked: %{public}f meters", locationHashCode, Float.valueOf(distance));
                        }
                    } else if (HWFLOW) {
                        LBSLog.i(TAG, false, "input gpsLocation is less accuracy, %{public}s", locationHashCode);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeExpiredNetworkLocations() {
        if (!this.mUncheckdNetworkLocations.isEmpty()) {
            long referenceTime = System.currentTimeMillis();
            if (HWFLOW) {
                LBSLog.i(TAG, false, "removeExpiredNetworkLocations mUncheckdNetworkLocations's size is:%{public}d", Integer.valueOf(this.mUncheckdNetworkLocations.size()));
            }
            Iterator<Location> uncheckedLocations = this.mUncheckdNetworkLocations.iterator();
            while (uncheckedLocations.hasNext()) {
                Location location = uncheckedLocations.next();
                if (location == null) {
                    LBSLog.e(TAG, false, "removeExpiredNetworkLocations network location is null", new Object[0]);
                } else {
                    String locationHashCode = Integer.toHexString(System.identityHashCode(location));
                    long diffTime = referenceTime - location.getTime();
                    if (diffTime > ((long) VALID_TIME_RANGE)) {
                        if (HWFLOW) {
                            LBSLog.i(TAG, false, "remove: %{public}s, reason: expired: %d ms", locationHashCode, Long.valueOf(diffTime));
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
                LBSLog.i(TAG, false, "mGpsLastLocation is null", new Object[0]);
            }
            return false;
        }
        long diffTime = System.currentTimeMillis() - this.mGpsLastLocation.getTime();
        if (diffTime <= ((long) VALID_TIME_RANGE)) {
            return true;
        }
        if (HWFLOW) {
            LBSLog.i(TAG, false, "gpslocation is expired:%{public}d ms", Long.valueOf(diffTime));
        }
        return false;
    }

    private boolean isLocationChange(Location location) {
        boolean isChange = false;
        if (location == null) {
            LBSLog.e(TAG, false, "location is null", new Object[0]);
            return false;
        }
        Location location2 = this.mLastLocation;
        if (location2 == null) {
            this.mLastLocation = location;
            return true;
        }
        float dis1 = location.distanceTo(location2);
        float dis2 = location.getAccuracy();
        if ((dis1 > dis2 ? dis1 : dis2) >= 5000.0f) {
            isChange = true;
        }
        if (isChange) {
            this.mLastLocation = location;
        }
        return isChange;
    }

    @SuppressLint({"NewApi"})
    private void refreshCellStatus() {
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        }
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager != null) {
            this.mMccMnc = telephonyManager.getSimOperator();
            int type = this.mTelephonyManager.getCurrentPhoneType();
            CellLocation location = this.mTelephonyManager.getCellLocation();
            if (type != 1) {
                if (type != 2) {
                    this.mCellId = -1;
                    LBSLog.e(TAG, false, "unexpected phone type: %{public}d", Integer.valueOf(type));
                } else if (location instanceof CdmaCellLocation) {
                    try {
                        this.mCellId = ((CdmaCellLocation) location).getBaseStationId();
                    } catch (Exception e) {
                        LBSLog.e(TAG, false, "CdmaCellLocation Type Cast Exception", new Object[0]);
                    }
                }
            } else if (location instanceof GsmCellLocation) {
                try {
                    this.mCellId = ((GsmCellLocation) location).getCid();
                } catch (Exception e2) {
                    LBSLog.e(TAG, false, "GsmCellLocation Type Cast Exception", new Object[0]);
                }
            }
        } else {
            LBSLog.e(TAG, false, "mTelephonyManager is null", new Object[0]);
        }
    }

    private void refreshNetworkStatus() {
        if (this.mConnectivityManager == null) {
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        NetworkInfo networkInfo = null;
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        if (networkInfo != null) {
            this.mNetworkAvailable = networkInfo.isAvailable();
            this.mNetworkType = networkInfo.getSubtype();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processLocInjectInfo(Location location) {
        if (location == null) {
            if (HWFLOW) {
                LBSLog.i(TAG, false, "processLocInjectInfo: location is null", new Object[0]);
            }
        } else if (this.mGpsStatus == 0) {
            if (HWFLOW) {
                LBSLog.i(TAG, false, "processLocInjectInfo: gps does not start", new Object[0]);
            }
        } else if (this.mFirstLocation == null) {
            this.mInjectLocation = location;
            Bundle bundle = location.getExtras();
            if (bundle != null) {
                this.mProvider = bundle.getString(HwLocalLocationProvider.KEY_LOC_SOURCE);
                this.mTableId = bundle.getString(HwLocalLocationProvider.KEY_LOC_TABLEID);
            }
        } else if (HWFLOW) {
            LBSLog.i(TAG, "processLocInjectInfo: gps has already fixed");
        }
    }

    private boolean report(int actionID, String actionMsg) {
        HwReportTool hwReportTool = this.mReportTool;
        if (hwReportTool != null) {
            return hwReportTool.report(actionID, actionMsg);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void setGpsStatus(int gpsStatus) {
        if (gpsStatus == 1 || gpsStatus == 0) {
            this.mGpsStatus = gpsStatus;
        } else if (HWFLOW) {
            LBSLog.i(TAG, false, "unexpceted gpsStatus:%{public}d", Integer.valueOf(gpsStatus));
        }
    }

    /* access modifiers changed from: package-private */
    public void clearStateInfo() {
        this.mInjectLocation = null;
        this.mFirstLocation = null;
        this.mTableId = null;
        this.mProvider = null;
    }

    /* access modifiers changed from: package-private */
    public void switchUser(int userId) {
        if (this.mCurrentUserId != userId) {
            this.mCurrentUserId = userId;
            this.mLastGpsSwitchStatus = isGpsProviderSettingOn(userId) ? 1 : 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isGpsProviderSettingOn(int userId) {
        try {
            int locationMode = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "location_mode", 0, userId);
            if (locationMode == 3 || locationMode == 1) {
                return true;
            }
            return false;
        } catch (IllegalArgumentException e) {
            LBSLog.e(TAG, false, "illegal userId for isLocationProviderEnabledForUser", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isNetworkProviderSettingOn(int userId) {
        try {
            int locationMode = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "location_mode", 0, userId);
            if (locationMode == 3 || locationMode == 2) {
                return true;
            }
            return false;
        } catch (IllegalArgumentException e) {
            LBSLog.e(TAG, false, "illegal userId for isLocationProviderEnabledForUser", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public String getProvidersSettingAllowed(int userId) {
        String allowedProviders = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "location_providers_allowed", userId);
        if (allowedProviders != null) {
            return allowedProviders.replace("0,", "").replace("0", "");
        }
        return "";
    }
}
