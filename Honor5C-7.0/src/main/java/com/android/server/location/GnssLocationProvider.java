package com.android.server.location;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.hardware.location.GeofenceHardwareImpl;
import android.location.FusedBatchOptions.SourceTechnologies;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.IGnssStatusListener;
import android.location.IGnssStatusProvider;
import android.location.IGnssStatusProvider.Stub;
import android.location.IGpsGeofenceHardware;
import android.location.ILocationManager;
import android.location.INetInitiatedListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest.Builder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Telephony.Carriers;
import android.provider.Telephony.Sms.Intents;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.HwLog;
import android.util.Log;
import android.util.NtpTrustedTime;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.internal.location.GpsNetInitiatedHandler;
import com.android.internal.location.GpsNetInitiatedHandler.GpsNiNotification;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.server.HwServiceFactory;
import com.android.server.am.IHwPowerInfoService;
import com.android.server.am.ProcessList;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import com.huawei.cust.HwCustUtils;
import huawei.cust.HwCfgFilePolicy;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import libcore.io.IoUtils;

public class GnssLocationProvider implements LocationProviderInterface {
    private static final int ADD_LISTENER = 8;
    private static final int AGPS_APP_STARTED = 1;
    private static final int AGPS_APP_STOPPED = 0;
    private static final int AGPS_DATA_CONNECTION_CLOSED = 0;
    private static final int AGPS_DATA_CONNECTION_OPEN = 2;
    private static final int AGPS_DATA_CONNECTION_OPENING = 1;
    private static final int AGPS_REF_LOCATION_TYPE_GSM_CELLID = 1;
    private static final int AGPS_REF_LOCATION_TYPE_UMTS_CELLID = 2;
    private static final int AGPS_REG_LOCATION_TYPE_MAC = 3;
    private static final int AGPS_RIL_REQUEST_REFLOC_CELLID = 1;
    private static final int AGPS_RIL_REQUEST_REFLOC_MAC = 2;
    private static final int AGPS_RIL_REQUEST_SETID_IMSI = 1;
    private static final int AGPS_RIL_REQUEST_SETID_MSISDN = 2;
    private static final int AGPS_SETID_TYPE_IMSI = 1;
    private static final int AGPS_SETID_TYPE_MSISDN = 2;
    private static final int AGPS_SETID_TYPE_NONE = 0;
    private static final int AGPS_SUPL_MODE_MSA = 2;
    private static final int AGPS_SUPL_MODE_MSB = 1;
    private static final int AGPS_TYPE_C2K = 2;
    private static final int AGPS_TYPE_SUPL = 1;
    private static final String ALARM_TIMEOUT = "com.android.internal.location.ALARM_TIMEOUT";
    private static final String ALARM_WAKEUP = "com.android.internal.location.ALARM_WAKEUP";
    private static final int APN_INVALID = 0;
    private static final int APN_IPV4 = 1;
    private static final int APN_IPV4V6 = 3;
    private static final int APN_IPV6 = 2;
    private static final String BATTERY_SAVER_GPS_MODE = "batterySaverGpsMode";
    private static final int BATTERY_SAVER_MODE_DISABLED_WHEN_SCREEN_OFF = 1;
    private static final int BATTERY_SAVER_MODE_NO_CHANGE = 0;
    private static final int CHECK_LOCATION = 1;
    private static final boolean DEBUG = false;
    private static final String DEFAULT_PROPERTIES_FILE = "gps.conf";
    private static final int DOWNLOAD_XTRA_DATA = 6;
    private static final int DOWNLOAD_XTRA_DATA_FINISHED = 11;
    private static final int ENABLE = 2;
    private static final int GPS_AGPS_DATA_CONNECTED = 3;
    private static final int GPS_AGPS_DATA_CONN_DONE = 4;
    private static final int GPS_AGPS_DATA_CONN_FAILED = 5;
    private static final int GPS_CAPABILITY_GEOFENCING = 32;
    private static final int GPS_CAPABILITY_MEASUREMENTS = 64;
    private static final int GPS_CAPABILITY_MSA = 4;
    private static final int GPS_CAPABILITY_MSB = 2;
    private static final int GPS_CAPABILITY_NAV_MESSAGES = 128;
    private static final int GPS_CAPABILITY_ON_DEMAND_TIME = 16;
    private static final int GPS_CAPABILITY_SCHEDULING = 1;
    private static final int GPS_CAPABILITY_SINGLE_SHOT = 8;
    private static final int GPS_DATA_MAX_SIZE = 300;
    private static final int GPS_DATA_TIMEOUT = 3000;
    private static final int GPS_DELETE_ALL = 65535;
    private static final int GPS_DELETE_ALMANAC = 2;
    private static final int GPS_DELETE_CELLDB_INFO = 32768;
    private static final int GPS_DELETE_EPHEMERIS = 1;
    private static final int GPS_DELETE_HEALTH = 64;
    private static final int GPS_DELETE_IONO = 16;
    private static final int GPS_DELETE_POSITION = 4;
    private static final int GPS_DELETE_RTI = 1024;
    private static final int GPS_DELETE_SADATA = 512;
    private static final int GPS_DELETE_SVDIR = 128;
    private static final int GPS_DELETE_SVSTEER = 256;
    private static final int GPS_DELETE_TIME = 8;
    private static final int GPS_DELETE_UTC = 32;
    private static final int GPS_GEOFENCE_AVAILABLE = 2;
    private static final int GPS_GEOFENCE_ERROR_GENERIC = -149;
    private static final int GPS_GEOFENCE_ERROR_ID_EXISTS = -101;
    private static final int GPS_GEOFENCE_ERROR_ID_UNKNOWN = -102;
    private static final int GPS_GEOFENCE_ERROR_INVALID_TRANSITION = -103;
    private static final int GPS_GEOFENCE_ERROR_TOO_MANY_GEOFENCES = 100;
    private static final int GPS_GEOFENCE_OPERATION_SUCCESS = 0;
    private static final int GPS_GEOFENCE_UNAVAILABLE = 1;
    private static final String GPS_INJECT_LOCATION_PERMISSION = "com.huawei.android.permission.INJECT_LOCATION";
    private static final int GPS_POLLING_THRESHOLD_INTERVAL = 10000;
    private static final int GPS_POSITION_MODE_MS_ASSISTED = 2;
    private static final int GPS_POSITION_MODE_MS_BASED = 1;
    private static final int GPS_POSITION_MODE_STANDALONE = 0;
    private static final int GPS_POSITION_RECURRENCE_PERIODIC = 0;
    private static final int GPS_POSITION_RECURRENCE_SINGLE = 1;
    private static final int GPS_RELEASE_AGPS_DATA_CONN = 2;
    private static final int GPS_REQUEST_AGPS_DATA_CONN = 1;
    private static final int GPS_STATUS_ENGINE_OFF = 4;
    private static final int GPS_STATUS_ENGINE_ON = 3;
    private static final int GPS_STATUS_NONE = 0;
    private static final int GPS_STATUS_SESSION_BEGIN = 1;
    private static final int GPS_STATUS_SESSION_END = 2;
    private static final int INITIALIZE_HANDLER = 13;
    private static final int INJECT_NTP_TIME = 5;
    private static final int INJECT_NTP_TIME_FINISHED = 10;
    private static final String KEY_AGPS_APP_STARTED_NAVIGATION = "agps_app_started_navigation";
    private static final int LOCATION_HAS_ACCURACY = 16;
    private static final int LOCATION_HAS_ALTITUDE = 2;
    private static final int LOCATION_HAS_BEARING = 8;
    private static final int LOCATION_HAS_LAT_LONG = 1;
    private static final int LOCATION_HAS_SPEED = 4;
    private static final int LOCATION_INVALID = 0;
    private static final long MAX_RETRY_INTERVAL = 14400000;
    private static final int MAX_SVS = 64;
    private static final int NO_FIX_TIMEOUT = 60000;
    private static final long NTP_INTERVAL = 86400000;
    private static final ProviderProperties PROPERTIES = null;
    private static final String PROPERTIES_FILE_PREFIX = "gps";
    private static final String PROPERTIES_FILE_SUFFIX = ".conf";
    private static final long RECENT_FIX_TIMEOUT = 10000;
    private static final int RELEASE_SUPL_CONNECTION = 15;
    private static final int REMOVE_LISTENER = 9;
    private static final int REQUEST_SUPL_CONNECTION = 14;
    private static final long RETRY_INTERVAL = 300000;
    private static final int SET_REQUEST = 3;
    private static final String SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private static final int STATE_DOWNLOADING = 1;
    private static final int STATE_IDLE = 2;
    private static final int STATE_PENDING_NETWORK = 0;
    private static final int SUBSCRIPTION_OR_SIM_CHANGED = 12;
    private static final String TAG = "GnssLocationProvider";
    private static final int TCP_MAX_PORT = 65535;
    private static final int TCP_MIN_PORT = 0;
    private static final int UPDATE_LOCATION = 7;
    private static final int UPDATE_NETWORK_STATE = 4;
    private static final String WAKELOCK_KEY = "GnssLocationProvider";
    private static long mLastAddNmeaTimestamp;
    private static boolean mWcdmaVpEnabled;
    private InetAddress mAGpsDataConnectionIpAddr;
    private int mAGpsDataConnectionState;
    private AgpsConnectProvider mAgpsConnectProvider;
    private final AlarmManager mAlarmManager;
    private final IAppOpsService mAppOpsService;
    private final IBatteryStats mBatteryStats;
    private final BroadcastReceiver mBroadcastReceiver;
    private String mC2KServerHost;
    private int mC2KServerPort;
    private WorkSource mClientSource;
    private float[] mCn0s;
    private final ConnectivityManager mConnMgr;
    private final Context mContext;
    HwCustGpsLocationProvider mCust;
    private String mDefaultApn;
    private ContentObserver mDefaultApnObserver;
    private boolean mDisableGps;
    private int mDownloadXtraDataPending;
    private boolean mEnabled;
    private int mEngineCapabilities;
    private boolean mEngineOn;
    private int mFixInterval;
    private long mFixRequestTime;
    private GeofenceHardwareImpl mGeofenceHardwareImpl;
    private final GnssMeasurementsProvider mGnssMeasurementsProvider;
    private final GnssNavigationMessageProvider mGnssNavigationMessageProvider;
    private final IGnssStatusProvider mGnssStatusProvider;
    private Thread mGpsDataThread;
    private LinkedBlockingQueue<GpsData> mGpsDatas;
    private IGpsGeofenceHardware mGpsGeofenceBinder;
    private final BroadcastReceiver mGpsLocalLocationReceiver;
    IHwGpsXtraDownloadReceiver mGpsXtraReceiver;
    private Handler mHandler;
    private IHwCmccGpsFeature mHwCmccGpsFeature;
    private IHwGpsLocationCustFeature mHwGpsLocationCustFeature;
    IHwGpsLocationManager mHwGpsLocationManager;
    private IHwGpsLogServices mHwGpsLogServices;
    private IHwPowerInfoService mHwPowerInfoService;
    private final ILocationManager mILocationManager;
    private int mInjectNtpTimePending;
    private long mLastFixTime;
    private final GnssStatusListenerHelper mListenerHelper;
    private Location mLocation;
    private Bundle mLocationExtras;
    private int mLocationFlags;
    private Object mLock;
    private final GpsNetInitiatedHandler mNIHandler;
    private boolean mNavigating;
    private final INetInitiatedListener mNetInitiatedListener;
    private final NetworkCallback mNetworkConnectivityCallback;
    private byte[] mNmeaBuffer;
    private BackOff mNtpBackOff;
    private final NtpTrustedTime mNtpTime;
    private boolean mOnDemandTimeInjection;
    private final OnSubscriptionsChangedListener mOnSubscriptionsChangedListener;
    private final PackageManager mPackageManager;
    private int mPositionMode;
    private final PowerManager mPowerManager;
    private Properties mProperties;
    private ProviderRequest mProviderRequest;
    private boolean mRequesetUtcTime;
    private boolean mSingleShot;
    private boolean mStarted;
    private int mStatus;
    private long mStatusUpdateTime;
    private final NetworkCallback mSuplConnectivityCallback;
    private boolean mSuplEsEnabled;
    private String mSuplServerHost;
    private int mSuplServerPort;
    private boolean mSupportsXtra;
    private float[] mSvAzimuths;
    private int mSvCount;
    private float[] mSvElevations;
    private int[] mSvidWithFlags;
    private int mTimeToFirstFix;
    private final PendingIntent mTimeoutIntent;
    private final WakeLock mWakeLock;
    private final PendingIntent mWakeupIntent;
    private WorkSource mWorkSource;
    private BackOff mXtraBackOff;
    private int mYearOfHardware;
    private boolean misBetaUser;

    /* renamed from: com.android.server.location.GnssLocationProvider.10 */
    class AnonymousClass10 extends GnssMeasurementsProvider {
        AnonymousClass10(Handler $anonymous0) {
            super($anonymous0);
        }

        public boolean isAvailableInPlatform() {
            return GnssLocationProvider.native_is_measurement_supported();
        }

        protected boolean registerWithService() {
            return GnssLocationProvider.this.native_start_measurement_collection();
        }

        protected void unregisterFromService() {
            GnssLocationProvider.this.native_stop_measurement_collection();
        }

        protected boolean isGpsEnabled() {
            return GnssLocationProvider.this.isEnabled();
        }
    }

    /* renamed from: com.android.server.location.GnssLocationProvider.11 */
    class AnonymousClass11 extends GnssNavigationMessageProvider {
        AnonymousClass11(Handler $anonymous0) {
            super($anonymous0);
        }

        protected boolean isAvailableInPlatform() {
            return GnssLocationProvider.native_is_navigation_message_supported();
        }

        protected boolean registerWithService() {
            return GnssLocationProvider.this.native_start_navigation_message_collection();
        }

        protected void unregisterFromService() {
            GnssLocationProvider.this.native_stop_navigation_message_collection();
        }

        protected boolean isGpsEnabled() {
            return GnssLocationProvider.this.isEnabled();
        }
    }

    /* renamed from: com.android.server.location.GnssLocationProvider.15 */
    class AnonymousClass15 extends Thread {
        AnonymousClass15(String $anonymous0) {
            super($anonymous0);
        }

        public void run() {
            Log.i(GnssLocationProvider.WAKELOCK_KEY, "mGpsDataThread begin");
            while (true) {
                if (GnssLocationProvider.this.mGpsDatas.size() > GnssLocationProvider.GPS_DATA_MAX_SIZE) {
                    Log.e(GnssLocationProvider.WAKELOCK_KEY, "mGpsDatas too large :" + GnssLocationProvider.this.mGpsDatas.size());
                    GnssLocationProvider.this.mGpsDatas.clear();
                }
                try {
                    ((GpsData) GnssLocationProvider.this.mGpsDatas.take()).send();
                } catch (InterruptedException e) {
                    Log.e(GnssLocationProvider.WAKELOCK_KEY, "mGpsDatas send error");
                }
            }
        }
    }

    public interface GnssSystemInfoProvider {
        int getGnssYearOfHardware();
    }

    /* renamed from: com.android.server.location.GnssLocationProvider.9 */
    class AnonymousClass9 extends GnssStatusListenerHelper {
        AnonymousClass9(Handler $anonymous0) {
            super($anonymous0);
        }

        protected boolean isAvailableInPlatform() {
            return GnssLocationProvider.isSupported();
        }

        protected boolean isGpsEnabled() {
            return GnssLocationProvider.this.isEnabled();
        }
    }

    private static final class BackOff {
        private static final int MULTIPLIER = 2;
        private long mCurrentIntervalMillis;
        private final long mInitIntervalMillis;
        private final long mMaxIntervalMillis;

        public BackOff(long initIntervalMillis, long maxIntervalMillis) {
            this.mInitIntervalMillis = initIntervalMillis;
            this.mMaxIntervalMillis = maxIntervalMillis;
            this.mCurrentIntervalMillis = this.mInitIntervalMillis / 2;
        }

        public long nextBackoffMillis() {
            if (this.mCurrentIntervalMillis > this.mMaxIntervalMillis) {
                return this.mMaxIntervalMillis;
            }
            this.mCurrentIntervalMillis *= 2;
            return this.mCurrentIntervalMillis;
        }

        public void reset() {
            this.mCurrentIntervalMillis = this.mInitIntervalMillis / 2;
        }
    }

    protected interface GpsData {
        void send();
    }

    private static class GpsRequest {
        public ProviderRequest request;
        public WorkSource source;

        public GpsRequest(ProviderRequest request, WorkSource source) {
            this.request = request;
            this.source = source;
        }
    }

    private final class NetworkLocationListener implements LocationListener {
        private NetworkLocationListener() {
        }

        public void onLocationChanged(Location location) {
            if ("network".equals(location.getProvider())) {
                GnssLocationProvider.this.handleUpdateLocation(location);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    }

    private class NmeaData implements GpsData {
        final String mNmea;
        final long mTimestamp;

        NmeaData(long timestamp, String nmea) {
            this.mTimestamp = timestamp;
            this.mNmea = nmea;
            if (GnssLocationProvider.mLastAddNmeaTimestamp != timestamp) {
                GnssLocationProvider.mLastAddNmeaTimestamp = timestamp;
            }
        }

        public void send() {
            if (GnssLocationProvider.mLastAddNmeaTimestamp - this.mTimestamp >= 3000) {
                Log.e(GnssLocationProvider.WAKELOCK_KEY, "mGpsDatas skip next old nmea data " + this.mTimestamp);
            } else {
                GnssLocationProvider.this.mListenerHelper.onNmeaReceived(this.mTimestamp, this.mNmea);
            }
        }
    }

    private final class ProviderHandler extends Handler {
        public ProviderHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int message = msg.what;
            switch (message) {
                case GnssLocationProvider.STATE_IDLE /*2*/:
                    if (msg.arg1 != GnssLocationProvider.STATE_DOWNLOADING) {
                        GnssLocationProvider.this.handleDisable();
                        break;
                    } else {
                        GnssLocationProvider.this.handleEnable();
                        break;
                    }
                case GnssLocationProvider.SET_REQUEST /*3*/:
                    GpsRequest gpsRequest = msg.obj;
                    GnssLocationProvider.this.handleSetRequest(gpsRequest.request, gpsRequest.source);
                    break;
                case GnssLocationProvider.UPDATE_NETWORK_STATE /*4*/:
                    GnssLocationProvider.this.handleUpdateNetworkState((Network) msg.obj);
                    break;
                case GnssLocationProvider.INJECT_NTP_TIME /*5*/:
                    GnssLocationProvider.this.handleInjectNtpTime();
                    break;
                case GnssLocationProvider.DOWNLOAD_XTRA_DATA /*6*/:
                    if (GnssLocationProvider.this.mSupportsXtra) {
                        GnssLocationProvider.this.handleDownloadXtraData();
                        break;
                    }
                    break;
                case GnssLocationProvider.UPDATE_LOCATION /*7*/:
                    GnssLocationProvider.this.handleUpdateLocation((Location) msg.obj);
                    break;
                case GnssLocationProvider.INJECT_NTP_TIME_FINISHED /*10*/:
                    GnssLocationProvider.this.mInjectNtpTimePending = GnssLocationProvider.STATE_IDLE;
                    break;
                case GnssLocationProvider.DOWNLOAD_XTRA_DATA_FINISHED /*11*/:
                    GnssLocationProvider.this.mDownloadXtraDataPending = GnssLocationProvider.STATE_IDLE;
                    break;
                case GnssLocationProvider.SUBSCRIPTION_OR_SIM_CHANGED /*12*/:
                    GnssLocationProvider.this.subscriptionOrSimChanged(GnssLocationProvider.this.mContext);
                    break;
                case GnssLocationProvider.INITIALIZE_HANDLER /*13*/:
                    handleInitialize();
                    break;
                case GnssLocationProvider.REQUEST_SUPL_CONNECTION /*14*/:
                    GnssLocationProvider.this.handleRequestSuplConnection((InetAddress) msg.obj);
                    break;
                case GnssLocationProvider.RELEASE_SUPL_CONNECTION /*15*/:
                    GnssLocationProvider.this.handleReleaseSuplConnection(msg.arg1);
                    break;
            }
            if (msg.arg2 == GnssLocationProvider.STATE_DOWNLOADING) {
                GnssLocationProvider.this.mWakeLock.release();
                Log.i(GnssLocationProvider.WAKELOCK_KEY, "WakeLock released by handleMessage(" + message + ", " + msg.arg1 + ", " + msg.obj + ")");
            }
        }

        private void handleInitialize() {
            IntentFilter intentFilter;
            GnssLocationProvider.this.reloadGpsProperties(GnssLocationProvider.this.mContext, GnssLocationProvider.this.mProperties);
            SubscriptionManager.from(GnssLocationProvider.this.mContext).addOnSubscriptionsChangedListener(GnssLocationProvider.this.mOnSubscriptionsChangedListener);
            if (GnssLocationProvider.native_is_agps_ril_supported()) {
                intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.DATA_SMS_RECEIVED");
                intentFilter.addDataScheme("sms");
                intentFilter.addDataAuthority("localhost", "7275");
                GnssLocationProvider.this.mContext.registerReceiver(GnssLocationProvider.this.mBroadcastReceiver, intentFilter, null, this);
                intentFilter = new IntentFilter();
                intentFilter.addAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
                try {
                    intentFilter.addDataType("application/vnd.omaloc-supl-init");
                } catch (MalformedMimeTypeException e) {
                    Log.w(GnssLocationProvider.WAKELOCK_KEY, "Malformed SUPL init mime type");
                }
                GnssLocationProvider.this.mContext.registerReceiver(GnssLocationProvider.this.mBroadcastReceiver, intentFilter, null, this);
            } else {
                Log.i(GnssLocationProvider.WAKELOCK_KEY, "Skipped registration for SMS/WAP-PUSH messages because AGPS Ril in GPS HAL is not supported");
            }
            intentFilter = new IntentFilter();
            intentFilter.addAction(GnssLocationProvider.ALARM_WAKEUP);
            intentFilter.addAction(GnssLocationProvider.ALARM_TIMEOUT);
            intentFilter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
            intentFilter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction(GnssLocationProvider.SIM_STATE_CHANGED);
            GnssLocationProvider.this.mContext.registerReceiver(GnssLocationProvider.this.mBroadcastReceiver, intentFilter, null, this);
            intentFilter = new IntentFilter();
            intentFilter.addAction(IHwLocalLocationManager.ACTION_INJECT_LOCATION);
            GnssLocationProvider.this.mContext.registerReceiver(GnssLocationProvider.this.mGpsLocalLocationReceiver, intentFilter, GnssLocationProvider.GPS_INJECT_LOCATION_PERMISSION, this);
            GnssLocationProvider.this.initDefaultApnObserver(GnssLocationProvider.this.mHandler);
            GnssLocationProvider.this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://telephony/carriers/preferapn"), GnssLocationProvider.DEBUG, GnssLocationProvider.this.mDefaultApnObserver);
            Builder networkRequestBuilder = new Builder();
            networkRequestBuilder.addTransportType(GnssLocationProvider.TCP_MIN_PORT);
            networkRequestBuilder.addTransportType(GnssLocationProvider.STATE_DOWNLOADING);
            GnssLocationProvider.this.mConnMgr.registerNetworkCallback(networkRequestBuilder.build(), GnssLocationProvider.this.mNetworkConnectivityCallback);
            LocationManager locManager = (LocationManager) GnssLocationProvider.this.mContext.getSystemService("location");
            LocationRequest request = LocationRequest.createFromDeprecatedProvider("passive", 0, 0.0f, GnssLocationProvider.DEBUG);
            request.setHideFromAppOps(true);
            locManager.requestLocationUpdates(request, new NetworkLocationListener(null), getLooper());
        }
    }

    private class SvData implements GpsData {
        final float[] mAzimuths;
        final float[] mCn0s;
        final float[] mElevations;
        final int[] mPrnWithFlags;
        final int mSvCount;

        SvData(int svCount, int[] prnWithFlags, float[] cn0s, float[] elevations, float[] azimuths) {
            this.mSvCount = svCount;
            this.mPrnWithFlags = prnWithFlags;
            this.mCn0s = cn0s;
            this.mElevations = elevations;
            this.mAzimuths = azimuths;
        }

        public void send() {
            GnssLocationProvider.this.mListenerHelper.onSvStatusChanged(this.mSvCount, this.mPrnWithFlags, this.mCn0s, this.mElevations, this.mAzimuths);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.GnssLocationProvider.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.GnssLocationProvider.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.GnssLocationProvider.<clinit>():void");
    }

    private static native void class_init_native();

    private static native boolean native_add_geofence(int i, double d, double d2, double d3, int i2, int i3, int i4, int i5);

    private native void native_agps_data_conn_closed();

    private native void native_agps_data_conn_failed();

    private native void native_agps_data_conn_open(String str, int i);

    private native void native_agps_ni_message(byte[] bArr, int i);

    private native void native_agps_set_id(int i, String str);

    private native void native_agps_set_ref_location_cellid(int i, int i2, int i3, int i4, int i5);

    private native void native_cleanup();

    private static native void native_configuration_update(String str);

    private native void native_delete_aiding_data(int i);

    private native String native_get_internal_state();

    private native boolean native_init();

    private native void native_inject_location(double d, double d2, float f);

    private native void native_inject_time(long j, long j2, int i);

    private native void native_inject_xtra_data(byte[] bArr, int i);

    private static native boolean native_is_agps_ril_supported();

    private static native boolean native_is_geofence_supported();

    private static native boolean native_is_gnss_configuration_supported();

    private static native boolean native_is_measurement_supported();

    private static native boolean native_is_navigation_message_supported();

    private static native boolean native_is_supported();

    private static native boolean native_pause_geofence(int i);

    private native int native_read_nmea(byte[] bArr, int i);

    private native int native_read_sv_status(int[] iArr, float[] fArr, float[] fArr2, float[] fArr3);

    private static native boolean native_remove_geofence(int i);

    private static native boolean native_resume_geofence(int i, int i2);

    private native void native_send_ni_response(int i, int i2);

    private native void native_set_agps_server(int i, String str, int i2);

    private native boolean native_set_position_mode(int i, int i2, int i3, int i4, int i5);

    private native boolean native_start();

    private native boolean native_start_measurement_collection();

    private native boolean native_start_navigation_message_collection();

    private native boolean native_stop();

    private native boolean native_stop_measurement_collection();

    private native boolean native_stop_navigation_message_collection();

    private native boolean native_supports_xtra();

    private native void native_update_network_state(boolean z, int i, boolean z2, boolean z3, String str, String str2);

    private synchronized int getPositionMode() {
        return this.mPositionMode;
    }

    public IGnssStatusProvider getGnssStatusProvider() {
        return this.mGnssStatusProvider;
    }

    public IGpsGeofenceHardware getGpsGeofenceProxy() {
        return this.mGpsGeofenceBinder;
    }

    public GnssMeasurementsProvider getGnssMeasurementsProvider() {
        return this.mGnssMeasurementsProvider;
    }

    public GnssNavigationMessageProvider getGnssNavigationMessageProvider() {
        return this.mGnssNavigationMessageProvider;
    }

    private void subscriptionOrSimChanged(Context context) {
        if (DEBUG) {
            Log.d(WAKELOCK_KEY, "received SIM related action: ");
        }
        String mccMnc = ((TelephonyManager) this.mContext.getSystemService("phone")).getSimOperator();
        if (!TextUtils.isEmpty(mccMnc)) {
            if (DEBUG) {
                Log.d(WAKELOCK_KEY, "SIM MCC/MNC is available: " + mccMnc);
            }
            synchronized (this.mLock) {
                if (!SystemProperties.getBoolean("ro.config.hw_agps_adpt_sim", DEBUG)) {
                    reloadGpsProperties(context, this.mProperties);
                }
                this.mNIHandler.setSuplEsEnabled(this.mSuplEsEnabled);
            }
        } else if (DEBUG) {
            Log.d(WAKELOCK_KEY, "SIM MCC/MNC is still not available");
        }
    }

    private void checkSmsSuplInit(Intent intent) {
        if (!this.mHwCmccGpsFeature.checkSuplInit()) {
            SmsMessage[] messages = Intents.getMessagesFromIntent(intent);
            Object[] pdus = (Object[]) intent.getExtra("pdus");
            if (pdus == null) {
                Log.i(WAKELOCK_KEY, "pdus is null");
                return;
            }
            byte[] supl_init = SmsMessage.createFromPdu((byte[]) pdus[TCP_MIN_PORT]).getUserData();
            Log.i(WAKELOCK_KEY, "[NI][checkSmsSuplInit]:++");
            native_agps_ni_message(supl_init, supl_init.length);
        }
    }

    private void checkWapSuplInit(Intent intent) {
        if (!this.mHwCmccGpsFeature.checkSuplInit()) {
            byte[] suplInit = intent.getByteArrayExtra(SoundModelContract.KEY_DATA);
            if (suplInit != null) {
                native_agps_ni_message(suplInit, suplInit.length);
            }
        }
    }

    private void updateLowPowerMode() {
        int i = TCP_MIN_PORT;
        boolean disableGps = this.mPowerManager.isDeviceIdleMode();
        switch (Secure.getInt(this.mContext.getContentResolver(), BATTERY_SAVER_GPS_MODE, STATE_DOWNLOADING)) {
            case STATE_DOWNLOADING /*1*/:
                Log.i(WAKELOCK_KEY, "updateLowPowerMode,isPowerSaveMode:" + this.mPowerManager.isPowerSaveMode() + "isInteractive:" + this.mPowerManager.isInteractive());
                if (this.mPowerManager.isPowerSaveMode() && !this.mPowerManager.isInteractive()) {
                    i = STATE_DOWNLOADING;
                }
                disableGps |= i;
                break;
        }
        Log.i(WAKELOCK_KEY, "disableGps:" + disableGps + "  isEnabled()=" + isEnabled());
        if (disableGps != this.mDisableGps) {
            this.mDisableGps = disableGps;
            updateRequirements();
        }
    }

    public static boolean isSupported() {
        return native_is_supported();
    }

    private void reloadGpsProperties(Context context, Properties properties) {
        File cfg;
        if (DEBUG) {
            Log.d(WAKELOCK_KEY, "Reset GPS properties, previous size = " + properties.size());
        }
        loadPropertiesFromResource(context, properties);
        boolean isPropertiesLoadedFromFile = DEBUG;
        String gpsHardware = SystemProperties.get("ro.hardware.gps");
        if (!TextUtils.isEmpty(gpsHardware)) {
            cfg = HwCfgFilePolicy.getCfgFile("gps." + gpsHardware + PROPERTIES_FILE_SUFFIX, TCP_MIN_PORT);
            if (cfg != null) {
                if (DEBUG) {
                    Log.d(WAKELOCK_KEY, "gpsHardware config filepath = " + cfg.getAbsolutePath());
                }
                isPropertiesLoadedFromFile = loadPropertiesFromFile(cfg.getAbsolutePath(), properties);
            }
        }
        if (!isPropertiesLoadedFromFile) {
            cfg = HwCfgFilePolicy.getCfgFile(DEFAULT_PROPERTIES_FILE, TCP_MIN_PORT);
            if (cfg != null) {
                if (DEBUG) {
                    Log.d(WAKELOCK_KEY, "gpsHardware config filepath = " + cfg.getAbsolutePath());
                }
                loadPropertiesFromFile(cfg.getAbsolutePath(), properties);
            }
        }
        if (DEBUG) {
            Log.d(WAKELOCK_KEY, "GPS properties reloaded, size = " + properties.size());
        }
        setSuplHostPort(properties.getProperty("SUPL_HOST"), properties.getProperty("SUPL_PORT"));
        this.mC2KServerHost = properties.getProperty("C2K_HOST");
        String portString = properties.getProperty("C2K_PORT");
        if (!(this.mC2KServerHost == null || portString == null)) {
            try {
                this.mC2KServerPort = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                Log.e(WAKELOCK_KEY, "unable to parse C2K_PORT: " + portString);
            }
        }
        if (native_is_gnss_configuration_supported()) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(DumpState.DUMP_PREFERRED);
                properties.store(baos, null);
                native_configuration_update(baos.toString());
                if (DEBUG) {
                    Log.d(WAKELOCK_KEY, "final config = " + baos.toString());
                }
            } catch (IOException e2) {
                Log.e(WAKELOCK_KEY, "failed to dump properties contents");
            }
        } else {
            Log.i(WAKELOCK_KEY, "Skipped configuration update because GNSS configuration in GPS HAL is not supported");
        }
        String suplESProperty = this.mProperties.getProperty("SUPL_ES");
        if (suplESProperty != null) {
            try {
                boolean z;
                if (Integer.parseInt(suplESProperty) == STATE_DOWNLOADING) {
                    z = true;
                } else {
                    z = DEBUG;
                }
                this.mSuplEsEnabled = z;
            } catch (NumberFormatException e3) {
                Log.e(WAKELOCK_KEY, "unable to parse SUPL_ES: " + suplESProperty);
            }
        }
    }

    private void loadPropertiesFromResource(Context context, Properties properties) {
        String[] configValues = context.getResources().getStringArray(17236034);
        int length = configValues.length;
        for (int i = TCP_MIN_PORT; i < length; i += STATE_DOWNLOADING) {
            String item = configValues[i];
            if (DEBUG) {
                Log.d(WAKELOCK_KEY, "GpsParamsResource: " + item);
            }
            String[] split = item.split("=");
            if (split.length == STATE_IDLE) {
                properties.setProperty(split[TCP_MIN_PORT].trim().toUpperCase(), split[STATE_DOWNLOADING]);
            } else {
                Log.w(WAKELOCK_KEY, "malformed contents: " + item);
            }
        }
    }

    private boolean loadPropertiesFromFile(String filename, Properties properties) {
        Throwable th;
        try {
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(new File(filename));
                try {
                    properties.load(stream2);
                    IoUtils.closeQuietly(stream2);
                    return true;
                } catch (Throwable th2) {
                    th = th2;
                    stream = stream2;
                    IoUtils.closeQuietly(stream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                IoUtils.closeQuietly(stream);
                throw th;
            }
        } catch (IOException e) {
            Log.w(WAKELOCK_KEY, "Could not open GPS configuration file " + filename);
            return DEBUG;
        }
    }

    public GnssLocationProvider(Context context, ILocationManager ilocationManager, Looper looper) {
        this.mLock = new Object();
        this.mLocationFlags = TCP_MIN_PORT;
        this.mStatus = STATE_DOWNLOADING;
        this.mStatusUpdateTime = SystemClock.elapsedRealtime();
        this.mNtpBackOff = new BackOff(RETRY_INTERVAL, MAX_RETRY_INTERVAL);
        this.mXtraBackOff = new BackOff(RETRY_INTERVAL, MAX_RETRY_INTERVAL);
        this.mRequesetUtcTime = DEBUG;
        this.mInjectNtpTimePending = TCP_MIN_PORT;
        this.mDownloadXtraDataPending = TCP_MIN_PORT;
        this.mFixInterval = ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE;
        this.mFixRequestTime = 0;
        this.mTimeToFirstFix = TCP_MIN_PORT;
        this.mProviderRequest = null;
        this.mWorkSource = null;
        this.mDisableGps = DEBUG;
        this.mSuplServerPort = TCP_MIN_PORT;
        this.mSuplEsEnabled = DEBUG;
        this.mLocation = new Location(PROPERTIES_FILE_PREFIX);
        this.mLocationExtras = new Bundle();
        this.mClientSource = new WorkSource();
        Object[] objArr = new Object[STATE_DOWNLOADING];
        objArr[TCP_MIN_PORT] = this;
        this.mCust = (HwCustGpsLocationProvider) HwCustUtils.createObj(HwCustGpsLocationProvider.class, objArr);
        this.misBetaUser = DEBUG;
        this.mYearOfHardware = TCP_MIN_PORT;
        this.mGnssStatusProvider = new Stub() {
            public void registerGnssStatusCallback(IGnssStatusListener callback) {
                GnssLocationProvider.this.mListenerHelper.addListener(callback, GnssLocationProvider.this.mPackageManager.getPackagesForUid(Binder.getCallingUid())[GnssLocationProvider.TCP_MIN_PORT]);
            }

            public void unregisterGnssStatusCallback(IGnssStatusListener callback) {
                GnssLocationProvider.this.mListenerHelper.removeListener(callback);
            }
        };
        this.mNetworkConnectivityCallback = new NetworkCallback() {
            public void onAvailable(Network network) {
                NetworkInfo info = GnssLocationProvider.this.mConnMgr.getNetworkInfo(network);
                GnssLocationProvider.this.requestUtcTime();
                if (info != null) {
                    if (GnssLocationProvider.this.mGpsXtraReceiver != null) {
                        boolean z;
                        IHwGpsXtraDownloadReceiver iHwGpsXtraDownloadReceiver = GnssLocationProvider.this.mGpsXtraReceiver;
                        if (GnssLocationProvider.this.mDownloadXtraDataPending == 0) {
                            z = true;
                        } else {
                            z = GnssLocationProvider.DEBUG;
                        }
                        if (iHwGpsXtraDownloadReceiver.handleUpdateNetworkState(info, z)) {
                            GnssLocationProvider.this.sendMessage(GnssLocationProvider.DOWNLOAD_XTRA_DATA, GnssLocationProvider.TCP_MIN_PORT, null);
                        }
                    }
                    GnssLocationProvider.this.mHwGpsLogServices.updateNetworkState(info);
                }
            }
        };
        this.mSuplConnectivityCallback = new NetworkCallback() {
            public void onAvailable(Network network) {
                GnssLocationProvider.this.sendMessage(GnssLocationProvider.UPDATE_NETWORK_STATE, GnssLocationProvider.TCP_MIN_PORT, network);
            }

            public void onLost(Network network) {
                GnssLocationProvider.this.releaseSuplConnection(GnssLocationProvider.STATE_IDLE);
            }

            public void onUnavailable() {
                GnssLocationProvider.this.releaseSuplConnection(GnssLocationProvider.INJECT_NTP_TIME);
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(GnssLocationProvider.WAKELOCK_KEY, "receive broadcast intent, action: " + action);
                if (action != null) {
                    if (action.equals(GnssLocationProvider.ALARM_WAKEUP)) {
                        GnssLocationProvider.this.startNavigating(GnssLocationProvider.DEBUG);
                    } else if (action.equals(GnssLocationProvider.ALARM_TIMEOUT)) {
                        GnssLocationProvider.this.hibernate();
                    } else if (action.equals("android.intent.action.DATA_SMS_RECEIVED")) {
                        GnssLocationProvider.this.checkSmsSuplInit(intent);
                    } else if (action.equals("android.provider.Telephony.WAP_PUSH_RECEIVED")) {
                        GnssLocationProvider.this.checkWapSuplInit(intent);
                    } else if ("android.os.action.POWER_SAVE_MODE_CHANGED".equals(action) || "android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(action) || "android.intent.action.SCREEN_OFF".equals(action) || "android.intent.action.SCREEN_ON".equals(action)) {
                        GnssLocationProvider.this.updateLowPowerMode();
                    } else if (action.equals(GnssLocationProvider.SIM_STATE_CHANGED)) {
                        GnssLocationProvider.this.subscriptionOrSimChanged(context);
                    }
                }
            }
        };
        this.mOnSubscriptionsChangedListener = new OnSubscriptionsChangedListener() {
            public void onSubscriptionsChanged() {
                GnssLocationProvider.this.sendMessage(GnssLocationProvider.SUBSCRIPTION_OR_SIM_CHANGED, GnssLocationProvider.TCP_MIN_PORT, null);
            }
        };
        this.mGpsGeofenceBinder = new IGpsGeofenceHardware.Stub() {
            public boolean isHardwareGeofenceSupported() {
                return GnssLocationProvider.native_is_geofence_supported();
            }

            public boolean addCircularHardwareGeofence(int geofenceId, double latitude, double longitude, double radius, int lastTransition, int monitorTransitions, int notificationResponsiveness, int unknownTimer) {
                return GnssLocationProvider.native_add_geofence(geofenceId, latitude, longitude, radius, lastTransition, monitorTransitions, notificationResponsiveness, unknownTimer);
            }

            public boolean removeHardwareGeofence(int geofenceId) {
                return GnssLocationProvider.native_remove_geofence(geofenceId);
            }

            public boolean pauseHardwareGeofence(int geofenceId) {
                return GnssLocationProvider.native_pause_geofence(geofenceId);
            }

            public boolean resumeHardwareGeofence(int geofenceId, int monitorTransition) {
                return GnssLocationProvider.native_resume_geofence(geofenceId, monitorTransition);
            }
        };
        this.mGpsDatas = new LinkedBlockingQueue();
        this.mNetInitiatedListener = new INetInitiatedListener.Stub() {
            public boolean sendNiResponse(int notificationId, int userResponse) {
                Log.i(GnssLocationProvider.WAKELOCK_KEY, "sendNiResponse, notifId: " + notificationId + ", response: " + userResponse);
                GnssLocationProvider.this.native_send_ni_response(notificationId, userResponse);
                return true;
            }
        };
        this.mGpsLocalLocationReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i(GnssLocationProvider.WAKELOCK_KEY, "receive broadcast intent, action: " + action);
                if (action != null && action.equals(IHwLocalLocationManager.ACTION_INJECT_LOCATION) && GnssLocationProvider.this.mEngineOn && GnssLocationProvider.this.mStarted) {
                    Log.e(GnssLocationProvider.WAKELOCK_KEY, "mEngineOn && mStarted ,get location from DB, inject to GPS  Module");
                    GnssLocationProvider.this.sendMessage(GnssLocationProvider.UPDATE_LOCATION, GnssLocationProvider.TCP_MIN_PORT, intent.getParcelableExtra(IHwLocalLocationManager.EXTRA_KEY_LOCATION_CHANGED));
                }
            }
        };
        this.mSvidWithFlags = new int[MAX_SVS];
        this.mCn0s = new float[MAX_SVS];
        this.mSvElevations = new float[MAX_SVS];
        this.mSvAzimuths = new float[MAX_SVS];
        this.mNmeaBuffer = new byte[120];
        this.mContext = context;
        this.mNtpTime = NtpTrustedTime.getInstance(context);
        this.mILocationManager = ilocationManager;
        this.mLocation.setExtras(this.mLocationExtras);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWakeLock = this.mPowerManager.newWakeLock(STATE_DOWNLOADING, WAKELOCK_KEY);
        this.mWakeLock.setReferenceCounted(true);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mWakeupIntent = PendingIntent.getBroadcast(this.mContext, TCP_MIN_PORT, new Intent(ALARM_WAKEUP), TCP_MIN_PORT);
        this.mTimeoutIntent = PendingIntent.getBroadcast(this.mContext, TCP_MIN_PORT, new Intent(ALARM_TIMEOUT), TCP_MIN_PORT);
        this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
        this.mAppOpsService = IAppOpsService.Stub.asInterface(ServiceManager.getService("appops"));
        this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mHandler = new ProviderHandler(looper);
        this.mProperties = new Properties();
        sendMessage(INITIALIZE_HANDLER, TCP_MIN_PORT, null);
        this.mNIHandler = new GpsNetInitiatedHandler(context, this.mNetInitiatedListener, this.mSuplEsEnabled);
        this.mListenerHelper = new AnonymousClass9(this.mHandler);
        this.mGnssMeasurementsProvider = new AnonymousClass10(this.mHandler);
        this.mGnssNavigationMessageProvider = new AnonymousClass11(this.mHandler);
        this.mHwCmccGpsFeature = HwServiceFactory.getHwCmccGpsFeature(this.mContext, this);
        this.mHwGpsLocationCustFeature = HwServiceFactory.getHwGpsLocationCustFeature();
        this.mHwGpsLogServices = HwServiceFactory.getHwGpsLogServices(this.mContext);
        this.mGpsXtraReceiver = HwServiceFactory.getHwGpsXtraDownloadReceiver();
        if (this.mGpsXtraReceiver != null) {
            this.mGpsXtraReceiver.init(this.mContext, this);
            this.mSupportsXtra = native_supports_xtra();
        }
        this.mHwGpsLocationManager = HwServiceFactory.getHwGpsLocationManager(this.mContext);
        this.mPackageManager = this.mContext.getPackageManager();
        GpsFreezeProc.getInstance().registerFreezeListener(new GpsFreezeListener() {
            public void onFreezeProChange() {
                synchronized (GnssLocationProvider.this.mLock) {
                    if (GnssLocationProvider.this.mEnabled) {
                        GnssLocationProvider.this.updateRequirements();
                        return;
                    }
                }
            }
        });
        if ("cdma".equals(SystemProperties.get("ro.config.gpstype", ""))) {
            this.mAgpsConnectProvider = AgpsConnectProvider.createAgpsConnectProvider(context);
        }
        if (SystemProperties.getBoolean("ro.control.sleeplog", DEBUG) && SET_REQUEST == SystemProperties.getInt("ro.logsystem.usertype", TCP_MIN_PORT)) {
            this.misBetaUser = true;
        }
        if (this.misBetaUser) {
            this.mHwPowerInfoService = HwServiceFactory.getHwPowerInfoService(this.mContext, DEBUG);
        }
    }

    public String getName() {
        return PROPERTIES_FILE_PREFIX;
    }

    public ProviderProperties getProperties() {
        return PROPERTIES;
    }

    private void handleUpdateNetworkState(Network network) {
        NetworkInfo info = this.mConnMgr.getNetworkInfo(network);
        if (info != null) {
            boolean isConnected = info.isConnected();
            Object[] objArr = new Object[UPDATE_NETWORK_STATE];
            objArr[TCP_MIN_PORT] = agpsDataConnStateAsString();
            objArr[STATE_DOWNLOADING] = Boolean.valueOf(isConnected);
            objArr[STATE_IDLE] = info;
            objArr[SET_REQUEST] = this.mConnMgr.getNetworkCapabilities(network);
            Log.d(WAKELOCK_KEY, String.format("UpdateNetworkState, state=%s, connected=%s, info=%s, capabilities=%S", objArr));
            if (native_is_agps_ril_supported()) {
                boolean dataEnabled = info.isAvailable() ? TelephonyManager.getDefault().getDataEnabled() : DEBUG;
                if (this.mDefaultApn == null) {
                    this.mDefaultApn = getDefaultApn();
                }
                this.mHwCmccGpsFeature.setRoaming(info.isRoaming());
                if (this.mCust != null) {
                    this.mCust.setRoaming(info.isRoaming());
                }
                native_update_network_state(isConnected, info.getType(), info.isRoaming(), dataEnabled, info.getExtraInfo(), this.mDefaultApn);
            } else {
                Log.i(WAKELOCK_KEY, "Skipped network state update because GPS HAL AGPS-RIL is not  supported");
            }
            if (this.mAGpsDataConnectionState == STATE_DOWNLOADING) {
                if (isConnected) {
                    String apnName = info.getExtraInfo();
                    if (apnName == null) {
                        apnName = "dummy-apn";
                    }
                    int apnIpType = getApnIpType(apnName);
                    setRouting();
                    objArr = new Object[STATE_IDLE];
                    objArr[TCP_MIN_PORT] = apnName;
                    objArr[STATE_DOWNLOADING] = Integer.valueOf(apnIpType);
                    Log.d(WAKELOCK_KEY, String.format("native_agps_data_conn_open: mAgpsApn=%s, mApnIpType=%s", objArr));
                    native_agps_data_conn_open(apnName, apnIpType);
                    this.mAGpsDataConnectionState = STATE_IDLE;
                } else {
                    handleReleaseSuplConnection(INJECT_NTP_TIME);
                }
            }
        }
    }

    private void handleRequestSuplConnection(InetAddress address) {
        if (DEBUG) {
            Object[] objArr = new Object[STATE_IDLE];
            objArr[TCP_MIN_PORT] = agpsDataConnStateAsString();
            objArr[STATE_DOWNLOADING] = address;
            Log.d(WAKELOCK_KEY, String.format("requestSuplConnection, state=%s, address=%s", objArr));
        }
        if (this.mAGpsDataConnectionState == 0) {
            this.mAGpsDataConnectionIpAddr = address;
            this.mAGpsDataConnectionState = STATE_DOWNLOADING;
            Builder requestBuilder = new Builder();
            requestBuilder.addTransportType(TCP_MIN_PORT);
            requestBuilder.addCapability(STATE_DOWNLOADING);
            this.mConnMgr.requestNetwork(requestBuilder.build(), this.mSuplConnectivityCallback, 6000000);
        }
    }

    private void handleReleaseSuplConnection(int agpsDataConnStatus) {
        if (DEBUG) {
            Object[] objArr = new Object[STATE_IDLE];
            objArr[TCP_MIN_PORT] = agpsDataConnStateAsString();
            objArr[STATE_DOWNLOADING] = agpsDataConnStatusAsString(agpsDataConnStatus);
            Log.d(WAKELOCK_KEY, String.format("releaseSuplConnection, state=%s, status=%s", objArr));
        }
        if (this.mAGpsDataConnectionState != 0) {
            this.mAGpsDataConnectionState = TCP_MIN_PORT;
            this.mConnMgr.unregisterNetworkCallback(this.mSuplConnectivityCallback);
            switch (agpsDataConnStatus) {
                case STATE_IDLE /*2*/:
                    native_agps_data_conn_closed();
                    break;
                case INJECT_NTP_TIME /*5*/:
                    native_agps_data_conn_failed();
                    break;
                default:
                    Log.e(WAKELOCK_KEY, "Invalid status to release SUPL connection: " + agpsDataConnStatus);
                    break;
            }
        }
    }

    protected boolean isHttpReachable() {
        return true;
    }

    private void handleInjectNtpTime() {
        if (this.mInjectNtpTimePending != STATE_DOWNLOADING) {
            if (isDataNetworkConnected()) {
                this.mInjectNtpTimePending = STATE_DOWNLOADING;
                this.mWakeLock.acquire();
                Log.i(WAKELOCK_KEY, "WakeLock acquired by handleInjectNtpTime()");
                AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                    public void run() {
                        NetworkInfo activeNetworkInfo = GnssLocationProvider.this.mConnMgr.getActiveNetworkInfo();
                        InjectTimeRecord injectTimeRecord;
                        if (activeNetworkInfo == null || (GnssLocationProvider.STATE_DOWNLOADING == activeNetworkInfo.getType() && !GnssLocationProvider.this.isHttpReachable())) {
                            GnssLocationProvider.this.mInjectNtpTimePending = GnssLocationProvider.TCP_MIN_PORT;
                            injectTimeRecord = GnssLocationProvider.this.mHwGpsLocationManager.getInjectTime(0);
                            if (0 != injectTimeRecord.getInjectTime()) {
                                GnssLocationProvider.this.native_inject_time(injectTimeRecord.getInjectTime(), SystemClock.elapsedRealtime(), injectTimeRecord.getUncertainty());
                            }
                            GnssLocationProvider.this.mWakeLock.release();
                            Log.i(GnssLocationProvider.WAKELOCK_KEY, "WakeLock released by handleInjectNtpTime() for http unreachable");
                            return;
                        }
                        long delay;
                        Log.i(GnssLocationProvider.WAKELOCK_KEY, "Execute handleInjectNtpTime()");
                        boolean z = true;
                        if (GnssLocationProvider.this.mNtpTime.getCacheAge() >= GnssLocationProvider.NTP_INTERVAL) {
                            z = GnssLocationProvider.this.mNtpTime.forceRefresh();
                            GnssLocationProvider.this.mHwGpsLogServices.updateNtpDloadStatus(z);
                            if (z) {
                                GnssLocationProvider.this.mHwGpsLogServices.updateNtpServerInfo(GnssLocationProvider.this.mNtpTime.getCachedNtpIpAddress());
                            }
                        }
                        if (GnssLocationProvider.this.mNtpTime.getCacheAge() < GnssLocationProvider.NTP_INTERVAL) {
                            long time = GnssLocationProvider.this.mNtpTime.getCachedNtpTime();
                            long timeReference = GnssLocationProvider.this.mNtpTime.getCachedNtpTimeReference();
                            long certainty = GnssLocationProvider.this.mNtpTime.getCacheCertainty();
                            long now = System.currentTimeMillis();
                            if (GnssLocationProvider.DEBUG) {
                                Log.d(GnssLocationProvider.WAKELOCK_KEY, "NTP server returned: " + time + " (" + new Date(time) + ") reference: " + timeReference + " certainty: " + certainty + " system time offset: " + (GnssLocationProvider.this.mNtpTime.currentTimeMillis() - now));
                            }
                            long currentNtpTime = 0;
                            if (GnssLocationProvider.this.mHwGpsLocationManager.checkNtpTime(time, timeReference)) {
                                if (GnssLocationProvider.this.mGpsXtraReceiver != null) {
                                    GnssLocationProvider.this.mGpsXtraReceiver.setNtpTime(time, timeReference);
                                }
                                currentNtpTime = (SystemClock.elapsedRealtime() + time) - timeReference;
                            }
                            injectTimeRecord = GnssLocationProvider.this.mHwGpsLocationManager.getInjectTime(currentNtpTime);
                            if (0 != injectTimeRecord.getInjectTime()) {
                                GnssLocationProvider.this.native_inject_time(injectTimeRecord.getInjectTime(), SystemClock.elapsedRealtime(), injectTimeRecord.getUncertainty());
                            }
                            delay = GnssLocationProvider.NTP_INTERVAL;
                            GnssLocationProvider.this.mNtpBackOff.reset();
                        } else {
                            Log.i(GnssLocationProvider.WAKELOCK_KEY, "requestTime failed");
                            delay = GnssLocationProvider.this.mNtpBackOff.nextBackoffMillis();
                        }
                        GnssLocationProvider.this.sendMessage(GnssLocationProvider.INJECT_NTP_TIME_FINISHED, GnssLocationProvider.TCP_MIN_PORT, null);
                        if (GnssLocationProvider.DEBUG) {
                            Object[] objArr = new Object[GnssLocationProvider.SET_REQUEST];
                            objArr[GnssLocationProvider.TCP_MIN_PORT] = Boolean.valueOf(GnssLocationProvider.this.mOnDemandTimeInjection);
                            objArr[GnssLocationProvider.STATE_DOWNLOADING] = Boolean.valueOf(z);
                            objArr[GnssLocationProvider.STATE_IDLE] = Long.valueOf(delay);
                            Log.d(GnssLocationProvider.WAKELOCK_KEY, String.format("onDemandTimeInjection=%s, refreshSuccess=%s, delay=%s", objArr));
                        }
                        if ((GnssLocationProvider.this.mOnDemandTimeInjection || !z) && !GnssLocationProvider.this.mHandler.hasMessages(GnssLocationProvider.INJECT_NTP_TIME)) {
                            GnssLocationProvider.this.mHandler.sendEmptyMessageDelayed(GnssLocationProvider.INJECT_NTP_TIME, delay);
                        }
                        GnssLocationProvider.this.mWakeLock.release();
                        Log.i(GnssLocationProvider.WAKELOCK_KEY, "WakeLock released by handleInjectNtpTime()");
                    }
                });
                return;
            }
            this.mInjectNtpTimePending = TCP_MIN_PORT;
            InjectTimeRecord injectTimeRecord = this.mHwGpsLocationManager.getInjectTime(0);
            if (0 != injectTimeRecord.getInjectTime()) {
                native_inject_time(injectTimeRecord.getInjectTime(), SystemClock.elapsedRealtime(), injectTimeRecord.getUncertainty());
            }
        }
    }

    private void handleDownloadXtraData() {
        if (this.mDownloadXtraDataPending != STATE_DOWNLOADING) {
            if (isDataNetworkConnected()) {
                this.mDownloadXtraDataPending = STATE_DOWNLOADING;
                this.mWakeLock.acquire();
                Log.i(WAKELOCK_KEY, "WakeLock acquired by handleDownloadXtraData()");
                AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                    public void run() {
                        NetworkInfo activeNetworkInfo = GnssLocationProvider.this.mConnMgr.getActiveNetworkInfo();
                        if (activeNetworkInfo == null || (GnssLocationProvider.STATE_DOWNLOADING == activeNetworkInfo.getType() && !GnssLocationProvider.this.isHttpReachable())) {
                            GnssLocationProvider.this.mDownloadXtraDataPending = GnssLocationProvider.TCP_MIN_PORT;
                            GnssLocationProvider.this.mWakeLock.release();
                            Log.i(GnssLocationProvider.WAKELOCK_KEY, "WakeLock released by handleDownloadXtraData() for http unreachable");
                            return;
                        }
                        Log.i(GnssLocationProvider.WAKELOCK_KEY, "Execute handleDownloadXtraData()");
                        byte[] data = new GpsXtraDownloader(GnssLocationProvider.this.mProperties).downloadXtraData();
                        if (data != null) {
                            Log.i(GnssLocationProvider.WAKELOCK_KEY, "calling native_inject_xtra_data");
                            GnssLocationProvider.this.native_inject_xtra_data(data, data.length);
                            GnssLocationProvider.this.mXtraBackOff.reset();
                            if (GnssLocationProvider.this.mGpsXtraReceiver != null) {
                                GnssLocationProvider.this.mGpsXtraReceiver.sendXtraDownloadComplete();
                            }
                        }
                        GnssLocationProvider.this.sendMessage(GnssLocationProvider.DOWNLOAD_XTRA_DATA_FINISHED, GnssLocationProvider.TCP_MIN_PORT, null);
                        if (data == null && !GnssLocationProvider.this.mHandler.hasMessages(GnssLocationProvider.DOWNLOAD_XTRA_DATA)) {
                            GnssLocationProvider.this.mHandler.sendEmptyMessageDelayed(GnssLocationProvider.DOWNLOAD_XTRA_DATA, GnssLocationProvider.this.mXtraBackOff.nextBackoffMillis());
                        }
                        GnssLocationProvider.this.mWakeLock.release();
                        Log.i(GnssLocationProvider.WAKELOCK_KEY, "WakeLock released by handleDownloadXtraData()");
                    }
                });
                return;
            }
            this.mDownloadXtraDataPending = TCP_MIN_PORT;
        }
    }

    private void handleUpdateLocation(Location location) {
        if (location.hasAccuracy()) {
            native_inject_location(location.getLatitude(), location.getLongitude(), location.getAccuracy());
        }
    }

    public void enable() {
        synchronized (this.mLock) {
            if (this.mEnabled) {
                return;
            }
            this.mEnabled = true;
            sendMessage(STATE_IDLE, STATE_DOWNLOADING, null);
        }
    }

    private void setSuplHostPort(String hostString, String portString) {
        if (!(hostString == null || "".equals(hostString))) {
            this.mSuplServerHost = hostString;
        }
        if (!(portString == null || "".equals(portString))) {
            try {
                this.mSuplServerPort = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                Log.e(WAKELOCK_KEY, "unable to parse SUPL_PORT: " + portString);
            }
        }
        if (this.mSuplServerHost != null && this.mSuplServerPort > 0 && this.mSuplServerPort <= TCP_MAX_PORT) {
            native_set_agps_server(STATE_DOWNLOADING, this.mSuplServerHost, this.mSuplServerPort);
        }
    }

    private int getSuplMode(Properties properties, boolean agpsEnabled, boolean singleShot) {
        if (agpsEnabled) {
            String modeString = properties.getProperty("SUPL_MODE");
            int suplMode = TCP_MIN_PORT;
            if (!TextUtils.isEmpty(modeString)) {
                try {
                    suplMode = Integer.parseInt(modeString);
                } catch (NumberFormatException e) {
                    Log.e(WAKELOCK_KEY, "unable to parse SUPL_MODE: " + modeString);
                    return TCP_MIN_PORT;
                }
            }
            if (!hasCapability(STATE_IDLE) || (suplMode & STATE_DOWNLOADING) == 0) {
                return (singleShot && hasCapability(UPDATE_NETWORK_STATE) && (suplMode & STATE_IDLE) != 0) ? STATE_IDLE : TCP_MIN_PORT;
            } else {
                return STATE_DOWNLOADING;
            }
        }
    }

    private void handleEnable() {
        Log.i(WAKELOCK_KEY, "handleEnable");
        boolean enabled = native_init();
        this.mHwGpsLogServices.initGps(enabled, (byte) this.mEngineCapabilities);
        if (enabled) {
            this.mSupportsXtra = native_supports_xtra();
            if (this.mSuplServerHost != null) {
                native_set_agps_server(STATE_DOWNLOADING, this.mSuplServerHost, this.mSuplServerPort);
            }
            if (this.mC2KServerHost != null) {
                native_set_agps_server(STATE_IDLE, this.mC2KServerHost, this.mC2KServerPort);
            }
            this.mGnssMeasurementsProvider.onGpsEnabledChanged();
            this.mGnssNavigationMessageProvider.onGpsEnabledChanged();
            return;
        }
        synchronized (this.mLock) {
            this.mEnabled = DEBUG;
        }
        Log.w(WAKELOCK_KEY, "Failed to enable location provider");
    }

    public void disable() {
        synchronized (this.mLock) {
            if (this.mEnabled) {
                this.mEnabled = DEBUG;
                sendMessage(STATE_IDLE, TCP_MIN_PORT, null);
                return;
            }
        }
    }

    private void handleDisable() {
        Log.i(WAKELOCK_KEY, "handleDisable");
        this.mProviderRequest = null;
        this.mWorkSource = null;
        updateClientUids(new WorkSource());
        stopNavigating();
        this.mAlarmManager.cancel(this.mWakeupIntent);
        this.mAlarmManager.cancel(this.mTimeoutIntent);
        native_cleanup();
        this.mGnssMeasurementsProvider.onGpsEnabledChanged();
        this.mGnssNavigationMessageProvider.onGpsEnabledChanged();
    }

    public boolean isEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mEnabled;
        }
        return z;
    }

    public int getStatus(Bundle extras) {
        if (extras != null) {
            extras.putInt("satellites", this.mSvCount);
        }
        return this.mStatus;
    }

    private void updateStatus(int status, int svCount) {
        if (status != this.mStatus || svCount != this.mSvCount) {
            this.mStatus = status;
            this.mSvCount = svCount;
            synchronized (this.mLocationExtras) {
                this.mLocationExtras.putInt("satellites", svCount);
            }
            this.mStatusUpdateTime = SystemClock.elapsedRealtime();
        }
    }

    public long getStatusUpdateTime() {
        return this.mStatusUpdateTime;
    }

    public void setRequest(ProviderRequest request, WorkSource source) {
        sendMessage(SET_REQUEST, TCP_MIN_PORT, new GpsRequest(request, source));
    }

    private void handleSetRequest(ProviderRequest request, WorkSource source) {
        this.mProviderRequest = request;
        this.mWorkSource = source;
        updateRequirements();
    }

    private void updateRequirements() {
        if (this.mProviderRequest != null && this.mWorkSource != null) {
            boolean singleShot = DEBUG;
            if (this.mProviderRequest.locationRequests != null && this.mProviderRequest.locationRequests.size() > 0) {
                singleShot = true;
                for (LocationRequest lr : this.mProviderRequest.locationRequests) {
                    if (lr.getNumUpdates() != STATE_DOWNLOADING) {
                        singleShot = DEBUG;
                    }
                }
            }
            Log.i(WAKELOCK_KEY, "setRequest " + this.mProviderRequest);
            if (!this.mProviderRequest.reportLocation || this.mDisableGps || GpsFreezeProc.getInstance().shouldFreeze(this.mWorkSource) || !isEnabled()) {
                updateClientUids(new WorkSource());
                stopNavigating();
                this.mAlarmManager.cancel(this.mWakeupIntent);
                this.mAlarmManager.cancel(this.mTimeoutIntent);
            } else {
                updateClientUids(this.mWorkSource);
                handleGnssRequirementsChange(STATE_DOWNLOADING);
                this.mFixInterval = (int) this.mProviderRequest.interval;
                if (((long) this.mFixInterval) != this.mProviderRequest.interval) {
                    Log.w(WAKELOCK_KEY, "interval overflow: " + this.mProviderRequest.interval);
                    this.mFixInterval = Integer.MAX_VALUE;
                }
                if (this.mStarted) {
                    if (shouldReStartNavi()) {
                        stopNavigating();
                        startNavigating(singleShot);
                    }
                    if (hasCapability(STATE_DOWNLOADING) && !native_set_position_mode(getPositionMode(), TCP_MIN_PORT, this.mFixInterval, getPreferred_accuracy(), TCP_MIN_PORT)) {
                        Log.e(WAKELOCK_KEY, "set_position_mode failed in setMinTime()");
                    }
                } else if (isEnabled()) {
                    startNavigating(singleShot);
                }
            }
        }
    }

    private void updateClientUids(WorkSource source) {
        WorkSource[] changes = this.mClientSource.setReturningDiffs(source);
        if (changes != null) {
            int lastuid;
            int i;
            int uid;
            WorkSource newWork = changes[TCP_MIN_PORT];
            WorkSource goneWork = changes[STATE_DOWNLOADING];
            if (newWork != null) {
                lastuid = -1;
                for (i = TCP_MIN_PORT; i < newWork.size(); i += STATE_DOWNLOADING) {
                    try {
                        uid = newWork.get(i);
                        this.mAppOpsService.startOperation(AppOpsManager.getToken(this.mAppOpsService), STATE_IDLE, uid, newWork.getName(i));
                        if (uid != lastuid) {
                            lastuid = uid;
                            HwLog.bdate("BDAT_TAG_GPS_START", "uid=" + uid + " name=" + newWork.getName(i));
                            this.mBatteryStats.noteStartGps(uid);
                        }
                    } catch (RemoteException e) {
                        Log.w(WAKELOCK_KEY, "RemoteException", e);
                    }
                }
            }
            if (goneWork != null) {
                lastuid = -1;
                for (i = TCP_MIN_PORT; i < goneWork.size(); i += STATE_DOWNLOADING) {
                    try {
                        uid = goneWork.get(i);
                        this.mAppOpsService.finishOperation(AppOpsManager.getToken(this.mAppOpsService), STATE_IDLE, uid, goneWork.getName(i));
                        if (uid != lastuid) {
                            lastuid = uid;
                            HwLog.bdate("BDAT_TAG_GPS_STOP", "uid=" + uid + " name=" + goneWork.getName(i));
                            this.mBatteryStats.noteStopGps(uid);
                        }
                    } catch (RemoteException e2) {
                        Log.w(WAKELOCK_KEY, "RemoteException", e2);
                    }
                }
            }
        }
    }

    public boolean sendExtraCommand(String command, Bundle extras) {
        long identity = Binder.clearCallingIdentity();
        boolean result = DEBUG;
        if ("delete_aiding_data".equals(command)) {
            result = deleteAidingData(extras);
        } else if ("force_time_injection".equals(command)) {
            requestUtcTime();
            result = true;
        } else if (!"force_xtra_injection".equals(command)) {
            Log.w(WAKELOCK_KEY, "sendExtraCommand: unknown command " + command);
        } else if (this.mSupportsXtra) {
            xtraDownloadRequest();
            result = true;
        }
        if (this.mCust != null) {
            result = this.mCust.sendPostionModeCommand(result, command);
            synchronized (this) {
                this.mPositionMode = this.mCust.setPostionMode(this.mPositionMode);
            }
        }
        Binder.restoreCallingIdentity(identity);
        return result;
    }

    private boolean deleteAidingData(Bundle extras) {
        int flags;
        if (extras == null) {
            flags = TCP_MAX_PORT;
        } else {
            flags = TCP_MIN_PORT;
            if (extras.getBoolean("ephemeris")) {
                flags = STATE_DOWNLOADING;
            }
            if (extras.getBoolean("almanac")) {
                flags |= STATE_IDLE;
            }
            if (extras.getBoolean("position")) {
                flags |= UPDATE_NETWORK_STATE;
            }
            if (extras.getBoolean("time")) {
                flags |= LOCATION_HAS_BEARING;
            }
            if (extras.getBoolean("iono")) {
                flags |= LOCATION_HAS_ACCURACY;
            }
            if (extras.getBoolean("utc")) {
                flags |= GPS_DELETE_UTC;
            }
            if (extras.getBoolean("health")) {
                flags |= MAX_SVS;
            }
            if (extras.getBoolean("svdir")) {
                flags |= GPS_DELETE_SVDIR;
            }
            if (extras.getBoolean("svsteer")) {
                flags |= GPS_DELETE_SVSTEER;
            }
            if (extras.getBoolean("sadata")) {
                flags |= GPS_DELETE_SADATA;
            }
            if (extras.getBoolean("rti")) {
                flags |= GPS_DELETE_RTI;
            }
            if (extras.getBoolean("celldb-info")) {
                flags |= GPS_DELETE_CELLDB_INFO;
            }
            if (extras.getBoolean("all")) {
                flags |= TCP_MAX_PORT;
            }
        }
        if (flags == 0) {
            return DEBUG;
        }
        native_delete_aiding_data(flags);
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void startNavigating(boolean singleShot) {
        if (!this.mStarted) {
            String mode;
            int interval;
            Log.i(WAKELOCK_KEY, "startNavigating, singleShot is " + singleShot);
            this.mTimeToFirstFix = TCP_MIN_PORT;
            this.mLastFixTime = 0;
            this.mStarted = true;
            this.mSingleShot = singleShot;
            this.mPositionMode = TCP_MIN_PORT;
            this.mRequesetUtcTime = DEBUG;
            boolean agpsEnabled = Global.getInt(this.mContext.getContentResolver(), "assisted_gps_enabled", STATE_DOWNLOADING) != 0 ? true : DEBUG;
            this.mPositionMode = getSuplMode(this.mProperties, agpsEnabled, singleShot);
            this.mPositionMode = this.mHwGpsLocationCustFeature.setPostionMode(this.mPositionMode, agpsEnabled);
            this.mPositionMode = this.mHwCmccGpsFeature.setPostionModeAndAgpsServer(this.mPositionMode, agpsEnabled);
            if (this.mCust != null) {
                this.mPositionMode = this.mCust.setPostionMode(this.mPositionMode);
            }
            if ("cdma".equals(SystemProperties.get("ro.config.gpstype", ""))) {
                this.mPositionMode = AgpsConnectProvider.setPostionMode(this.mPositionMode, this.mContext);
            }
            switch (this.mPositionMode) {
                case TCP_MIN_PORT /*0*/:
                    mode = "standalone";
                    break;
                case STATE_DOWNLOADING /*1*/:
                    mode = "MS_BASED";
                    Log.i(WAKELOCK_KEY, "setting position_mode to " + mode);
                    interval = hasCapability(STATE_DOWNLOADING) ? this.mFixInterval : ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE;
                    if (mWcdmaVpEnabled && (this.mPositionMode == STATE_DOWNLOADING || this.mPositionMode == STATE_IDLE)) {
                        Global.putInt(this.mContext.getContentResolver(), KEY_AGPS_APP_STARTED_NAVIGATION, STATE_DOWNLOADING);
                    }
                    if (native_set_position_mode(this.mPositionMode, TCP_MIN_PORT, interval, getPreferred_accuracy(), TCP_MIN_PORT)) {
                        this.mHwCmccGpsFeature.setDelAidData();
                        if (native_start()) {
                            updateStatus(STATE_DOWNLOADING, TCP_MIN_PORT);
                            this.mFixRequestTime = System.currentTimeMillis();
                            this.mHwGpsLogServices.startGps(this.mStarted, this.mPositionMode);
                            if (!hasCapability(STATE_DOWNLOADING) && this.mFixInterval >= NO_FIX_TIMEOUT) {
                                Log.d(WAKELOCK_KEY, "GnssLocationProvider startNavigating SET_WAKE_ALARM60000");
                                this.mAlarmManager.set(STATE_IDLE, SystemClock.elapsedRealtime() + 60000, this.mTimeoutIntent);
                            }
                            startCacheThread();
                            handleGnssRequirementsChange(STATE_DOWNLOADING);
                            handleGnssNavigatingStateChange(true);
                            break;
                        }
                        this.mStarted = DEBUG;
                        this.mHwGpsLogServices.startGps(this.mStarted, this.mPositionMode);
                        Log.e(WAKELOCK_KEY, "native_start failed in startNavigating()");
                        return;
                    }
                    this.mStarted = DEBUG;
                    Log.e(WAKELOCK_KEY, "set_position_mode failed in startNavigating()");
                    this.mHwGpsLogServices.updateSetPosMode(DEBUG);
                    return;
                case STATE_IDLE /*2*/:
                    mode = "MS_ASSISTED";
                    Log.i(WAKELOCK_KEY, "setting position_mode to " + mode);
                    if (hasCapability(STATE_DOWNLOADING)) {
                    }
                    Global.putInt(this.mContext.getContentResolver(), KEY_AGPS_APP_STARTED_NAVIGATION, STATE_DOWNLOADING);
                    if (native_set_position_mode(this.mPositionMode, TCP_MIN_PORT, interval, getPreferred_accuracy(), TCP_MIN_PORT)) {
                        this.mHwCmccGpsFeature.setDelAidData();
                        if (native_start()) {
                            updateStatus(STATE_DOWNLOADING, TCP_MIN_PORT);
                            this.mFixRequestTime = System.currentTimeMillis();
                            this.mHwGpsLogServices.startGps(this.mStarted, this.mPositionMode);
                            Log.d(WAKELOCK_KEY, "GnssLocationProvider startNavigating SET_WAKE_ALARM60000");
                            this.mAlarmManager.set(STATE_IDLE, SystemClock.elapsedRealtime() + 60000, this.mTimeoutIntent);
                            startCacheThread();
                            handleGnssRequirementsChange(STATE_DOWNLOADING);
                            handleGnssNavigatingStateChange(true);
                            break;
                        }
                        this.mStarted = DEBUG;
                        this.mHwGpsLogServices.startGps(this.mStarted, this.mPositionMode);
                        Log.e(WAKELOCK_KEY, "native_start failed in startNavigating()");
                        return;
                    }
                    this.mStarted = DEBUG;
                    Log.e(WAKELOCK_KEY, "set_position_mode failed in startNavigating()");
                    this.mHwGpsLogServices.updateSetPosMode(DEBUG);
                    return;
                default:
                    mode = "unknown";
            }
            Log.i(WAKELOCK_KEY, "setting position_mode to " + mode);
            if (hasCapability(STATE_DOWNLOADING)) {
            }
            Global.putInt(this.mContext.getContentResolver(), KEY_AGPS_APP_STARTED_NAVIGATION, STATE_DOWNLOADING);
            if (native_set_position_mode(this.mPositionMode, TCP_MIN_PORT, interval, getPreferred_accuracy(), TCP_MIN_PORT)) {
                this.mStarted = DEBUG;
                Log.e(WAKELOCK_KEY, "set_position_mode failed in startNavigating()");
                this.mHwGpsLogServices.updateSetPosMode(DEBUG);
                return;
            }
            this.mHwCmccGpsFeature.setDelAidData();
            if (native_start()) {
                this.mStarted = DEBUG;
                this.mHwGpsLogServices.startGps(this.mStarted, this.mPositionMode);
                Log.e(WAKELOCK_KEY, "native_start failed in startNavigating()");
                return;
            }
            updateStatus(STATE_DOWNLOADING, TCP_MIN_PORT);
            this.mFixRequestTime = System.currentTimeMillis();
            this.mHwGpsLogServices.startGps(this.mStarted, this.mPositionMode);
            Log.d(WAKELOCK_KEY, "GnssLocationProvider startNavigating SET_WAKE_ALARM60000");
            this.mAlarmManager.set(STATE_IDLE, SystemClock.elapsedRealtime() + 60000, this.mTimeoutIntent);
            startCacheThread();
            handleGnssRequirementsChange(STATE_DOWNLOADING);
            handleGnssNavigatingStateChange(true);
        }
    }

    private synchronized void stopNavigating() {
        Log.i(WAKELOCK_KEY, "stopNavigating, mStarted=" + this.mStarted);
        if (this.mStarted) {
            this.mStarted = DEBUG;
            this.mSingleShot = DEBUG;
            this.mHwGpsLogServices.stopGps(native_stop());
            this.mTimeToFirstFix = TCP_MIN_PORT;
            this.mLastFixTime = 0;
            this.mLocationFlags = TCP_MIN_PORT;
            updateStatus(STATE_DOWNLOADING, TCP_MIN_PORT);
            if (mWcdmaVpEnabled) {
                Global.putInt(this.mContext.getContentResolver(), KEY_AGPS_APP_STARTED_NAVIGATION, TCP_MIN_PORT);
            }
            handleGnssRequirementsChange(STATE_IDLE);
            handleGnssNavigatingStateChange(DEBUG);
        }
    }

    private void hibernate() {
        stopNavigating();
        this.mAlarmManager.cancel(this.mTimeoutIntent);
        this.mAlarmManager.cancel(this.mWakeupIntent);
        long now = SystemClock.elapsedRealtime();
        Log.d(WAKELOCK_KEY, "GnssLocationProvider hibernate SET_WAKE_ALARM" + this.mFixInterval);
        this.mAlarmManager.set(STATE_IDLE, ((long) this.mFixInterval) + now, this.mWakeupIntent);
    }

    private boolean hasCapability(int capability) {
        return (this.mEngineCapabilities & capability) != 0 ? true : DEBUG;
    }

    private void reportLocation(int flags, double latitude, double longitude, double altitude, float speed, float bearing, float accuracy, long timestamp) {
        Log.i(WAKELOCK_KEY, "reportLocation lat: XXXXXX long: XXXXXX timestamp: " + timestamp + "speed:" + speed + ", bearing:" + bearing);
        synchronized (this.mLocation) {
            this.mLocationFlags = flags;
            if ((flags & STATE_DOWNLOADING) == STATE_DOWNLOADING) {
                this.mLocation.setLatitude(latitude);
                this.mLocation.setLongitude(longitude);
                this.mLocation.setTime(timestamp);
                this.mLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }
            if ((flags & STATE_IDLE) == STATE_IDLE) {
                this.mLocation.setAltitude(altitude);
            } else {
                this.mLocation.removeAltitude();
            }
            if ((flags & UPDATE_NETWORK_STATE) != UPDATE_NETWORK_STATE || speed < 0.0f) {
                this.mLocation.removeSpeed();
            } else {
                this.mLocation.setSpeed(speed);
            }
            if ((flags & LOCATION_HAS_BEARING) != LOCATION_HAS_BEARING || bearing == 1000.0f) {
                this.mLocation.removeBearing();
            } else {
                this.mLocation.setBearing(bearing);
            }
            if ((flags & LOCATION_HAS_ACCURACY) == LOCATION_HAS_ACCURACY) {
                this.mLocation.setAccuracy(accuracy);
            } else {
                this.mLocation.removeAccuracy();
            }
            synchronized (this.mLocationExtras) {
                this.mLocation.setExtras(this.mLocationExtras);
            }
            try {
                this.mILocationManager.reportLocation(this.mLocation, DEBUG);
            } catch (RemoteException e) {
                Log.e(WAKELOCK_KEY, "RemoteException calling reportLocation");
            }
        }
        this.mLastFixTime = System.currentTimeMillis();
        if (this.mTimeToFirstFix == 0 && (flags & STATE_DOWNLOADING) == STATE_DOWNLOADING) {
            this.mTimeToFirstFix = (int) (this.mLastFixTime - this.mFixRequestTime);
            Log.i(WAKELOCK_KEY, "TTFF: " + this.mTimeToFirstFix);
            this.mListenerHelper.onFirstFix(this.mTimeToFirstFix);
            synchronized (this.mLocation) {
                this.mHwGpsLocationManager.setGpsTime(timestamp, this.mLocation.getElapsedRealtimeNanos());
            }
        }
        synchronized (this) {
            if (this.mSingleShot) {
                stopNavigating();
            }
        }
        if (this.mStarted && this.mStatus != STATE_IDLE) {
            if (!hasCapability(STATE_DOWNLOADING) && this.mFixInterval < NO_FIX_TIMEOUT) {
                this.mAlarmManager.cancel(this.mTimeoutIntent);
            }
            Intent intent = new Intent("android.location.GPS_FIX_CHANGE");
            intent.putExtra("enabled", true);
            intent.putExtra("isFrameworkBroadcast", "true");
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            updateStatus(STATE_IDLE, this.mSvCount);
        }
        if (!hasCapability(STATE_DOWNLOADING) && this.mStarted && this.mFixInterval > GPS_POLLING_THRESHOLD_INTERVAL) {
            Log.i(WAKELOCK_KEY, "got fix, hibernating");
            hibernate();
        }
        this.mHwCmccGpsFeature.syncTime(timestamp);
    }

    private void reportStatus(int status) {
        Log.i(WAKELOCK_KEY, "reportStatus status: " + status);
        this.mHwGpsLogServices.updateGpsRunState(status);
        boolean wasNavigating = this.mNavigating;
        switch (status) {
            case STATE_DOWNLOADING /*1*/:
                this.mNavigating = true;
                this.mEngineOn = true;
                break;
            case STATE_IDLE /*2*/:
                this.mNavigating = DEBUG;
                break;
            case SET_REQUEST /*3*/:
                this.mEngineOn = true;
                break;
            case UPDATE_NETWORK_STATE /*4*/:
                this.mEngineOn = DEBUG;
                this.mNavigating = DEBUG;
                break;
        }
        if (this.misBetaUser) {
            synchronized (this.mHwPowerInfoService) {
                this.mHwPowerInfoService.notePowerInfoGPSStatus(status);
            }
        }
        if (wasNavigating != this.mNavigating) {
            this.mListenerHelper.onStatusChanged(this.mNavigating);
            Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
            intent.putExtra("enabled", this.mNavigating);
            intent.putExtra("isFrameworkBroadcast", "true");
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private void reportSvStatus() {
        int svCount = native_read_sv_status(this.mSvidWithFlags, this.mCn0s, this.mSvElevations, this.mSvAzimuths);
        this.mHwGpsLogServices.updateSvStatus(svCount, this.mSvidWithFlags, this.mCn0s, this.mSvElevations, this.mSvAzimuths);
        updateSvDate(svCount, this.mSvidWithFlags, this.mCn0s, this.mSvElevations, this.mSvAzimuths);
        Log.v(WAKELOCK_KEY, "SV count: " + svCount);
        int usedInFixCount = TCP_MIN_PORT;
        for (int i = TCP_MIN_PORT; i < svCount; i += STATE_DOWNLOADING) {
            if ((this.mSvidWithFlags[i] & UPDATE_NETWORK_STATE) != 0) {
                usedInFixCount += STATE_DOWNLOADING;
            }
            if (DEBUG) {
                Log.d(WAKELOCK_KEY, "svid: " + (this.mSvidWithFlags[i] >> UPDATE_LOCATION) + " cn0: " + (this.mCn0s[i] / 10.0f) + " elev: " + this.mSvElevations[i] + " azimuth: " + this.mSvAzimuths[i] + ((this.mSvidWithFlags[i] & STATE_DOWNLOADING) == 0 ? "  " : " E") + ((this.mSvidWithFlags[i] & STATE_IDLE) == 0 ? "  " : " A") + ((this.mSvidWithFlags[i] & UPDATE_NETWORK_STATE) == 0 ? "" : "U"));
            }
        }
        updateStatus(this.mStatus, usedInFixCount);
        if (this.mNavigating && this.mStatus == STATE_IDLE && this.mLastFixTime > 0 && System.currentTimeMillis() - this.mLastFixTime > RECENT_FIX_TIMEOUT) {
            Intent intent = new Intent("android.location.GPS_FIX_CHANGE");
            intent.putExtra("enabled", DEBUG);
            intent.putExtra("isFrameworkBroadcast", "true");
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            updateStatus(STATE_DOWNLOADING, this.mSvCount);
        }
    }

    private void reportAGpsStatus(int type, int status, byte[] ipaddr) {
        this.mHwGpsLogServices.updateAgpsState(type, status);
        switch (status) {
            case STATE_DOWNLOADING /*1*/:
                Log.i(WAKELOCK_KEY, "GPS_REQUEST_AGPS_DATA_CONN");
                Log.v(WAKELOCK_KEY, "Received SUPL IP addr[]: " + Arrays.toString(ipaddr));
                Object obj = null;
                if (ipaddr != null) {
                    try {
                        obj = InetAddress.getByAddress(ipaddr);
                        if (DEBUG) {
                            Log.d(WAKELOCK_KEY, "IP address converted to: " + obj);
                        }
                    } catch (UnknownHostException e) {
                        Log.e(WAKELOCK_KEY, "Bad IP Address: " + ipaddr, e);
                    }
                }
                sendMessage(REQUEST_SUPL_CONNECTION, TCP_MIN_PORT, obj);
            case STATE_IDLE /*2*/:
                Log.i(WAKELOCK_KEY, "GPS_RELEASE_AGPS_DATA_CONN");
                releaseSuplConnection(STATE_IDLE);
            case SET_REQUEST /*3*/:
                Log.i(WAKELOCK_KEY, "GPS_AGPS_DATA_CONNECTED");
            case UPDATE_NETWORK_STATE /*4*/:
                Log.i(WAKELOCK_KEY, "GPS_AGPS_DATA_CONN_DONE");
            case INJECT_NTP_TIME /*5*/:
                Log.i(WAKELOCK_KEY, "GPS_AGPS_DATA_CONN_FAILED");
            default:
                Log.i(WAKELOCK_KEY, "Received Unknown AGPS status: " + status);
        }
    }

    private void releaseSuplConnection(int connStatus) {
        sendMessage(RELEASE_SUPL_CONNECTION, connStatus, null);
    }

    private void reportNmea(long timestamp) {
        updateNmeaDate(timestamp, new String(this.mNmeaBuffer, TCP_MIN_PORT, native_read_nmea(this.mNmeaBuffer, this.mNmeaBuffer.length)));
    }

    private void updateSvDate(int svCount, int[] prnWithFlags, float[] cn0s, float[] elevations, float[] azimuths) {
        try {
            this.mGpsDatas.put(new SvData(svCount, prnWithFlags, cn0s, elevations, azimuths));
        } catch (InterruptedException e) {
            Log.e(WAKELOCK_KEY, "mGpsDatas put sv error");
        }
    }

    private void updateNmeaDate(long timestamp, String nmea) {
        try {
            this.mGpsDatas.put(new NmeaData(timestamp, nmea));
        } catch (InterruptedException e) {
            Log.e(WAKELOCK_KEY, "mGpsDatas put nmea error");
        }
    }

    private void startCacheThread() {
        if (this.mGpsDataThread == null || !this.mGpsDataThread.isAlive()) {
            this.mGpsDataThread = new AnonymousClass15("GpsDataThread");
            this.mGpsDataThread.start();
            return;
        }
        Log.i(WAKELOCK_KEY, "GpsDataThread alive");
    }

    private void reportMeasurementData(GnssMeasurementsEvent event) {
        this.mGnssMeasurementsProvider.onMeasurementsAvailable(event);
    }

    private void reportNavigationMessage(GnssNavigationMessage event) {
        this.mGnssNavigationMessageProvider.onNavigationMessageAvailable(event);
    }

    private void setEngineCapabilities(int capabilities) {
        boolean z;
        boolean z2 = true;
        this.mEngineCapabilities = capabilities;
        if (hasCapability(LOCATION_HAS_ACCURACY)) {
            this.mOnDemandTimeInjection = true;
            requestUtcTime();
        }
        GnssMeasurementsProvider gnssMeasurementsProvider = this.mGnssMeasurementsProvider;
        if ((capabilities & MAX_SVS) == MAX_SVS) {
            z = true;
        } else {
            z = DEBUG;
        }
        gnssMeasurementsProvider.onCapabilitiesUpdated(z);
        GnssNavigationMessageProvider gnssNavigationMessageProvider = this.mGnssNavigationMessageProvider;
        if ((capabilities & GPS_DELETE_SVDIR) != GPS_DELETE_SVDIR) {
            z2 = DEBUG;
        }
        gnssNavigationMessageProvider.onCapabilitiesUpdated(z2);
    }

    private void setGnssYearOfHardware(int yearOfHardware) {
        if (DEBUG) {
            Log.d(WAKELOCK_KEY, "setGnssYearOfHardware called with " + yearOfHardware);
        }
        this.mYearOfHardware = yearOfHardware;
    }

    public GnssSystemInfoProvider getGnssSystemInfoProvider() {
        return new GnssSystemInfoProvider() {
            public int getGnssYearOfHardware() {
                return GnssLocationProvider.this.mYearOfHardware;
            }
        };
    }

    private void xtraDownloadRequest() {
        Log.i(WAKELOCK_KEY, "xtraDownloadRequest");
        sendMessage(DOWNLOAD_XTRA_DATA, TCP_MIN_PORT, null);
    }

    private Location buildLocation(int flags, double latitude, double longitude, double altitude, float speed, float bearing, float accuracy, long timestamp) {
        Location location = new Location(PROPERTIES_FILE_PREFIX);
        if ((flags & STATE_DOWNLOADING) == STATE_DOWNLOADING) {
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setTime(timestamp);
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        if ((flags & STATE_IDLE) == STATE_IDLE) {
            location.setAltitude(altitude);
        }
        if ((flags & UPDATE_NETWORK_STATE) == UPDATE_NETWORK_STATE) {
            location.setSpeed(speed);
        }
        if ((flags & LOCATION_HAS_BEARING) == LOCATION_HAS_BEARING) {
            location.setBearing(bearing);
        }
        if ((flags & LOCATION_HAS_ACCURACY) == LOCATION_HAS_ACCURACY) {
            location.setAccuracy(accuracy);
        }
        return location;
    }

    private int getGeofenceStatus(int status) {
        switch (status) {
            case GPS_GEOFENCE_ERROR_GENERIC /*-149*/:
                return INJECT_NTP_TIME;
            case GPS_GEOFENCE_ERROR_INVALID_TRANSITION /*-103*/:
                return UPDATE_NETWORK_STATE;
            case GPS_GEOFENCE_ERROR_ID_UNKNOWN /*-102*/:
                return SET_REQUEST;
            case GPS_GEOFENCE_ERROR_ID_EXISTS /*-101*/:
                return STATE_IDLE;
            case TCP_MIN_PORT /*0*/:
                return TCP_MIN_PORT;
            case GPS_GEOFENCE_ERROR_TOO_MANY_GEOFENCES /*100*/:
                return STATE_DOWNLOADING;
            default:
                return -1;
        }
    }

    private void reportGeofenceTransition(int geofenceId, int flags, double latitude, double longitude, double altitude, float speed, float bearing, float accuracy, long timestamp, int transition, long transitionTimestamp) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        int i = geofenceId;
        this.mGeofenceHardwareImpl.reportGeofenceTransition(i, buildLocation(flags, latitude, longitude, altitude, speed, bearing, accuracy, timestamp), transition, transitionTimestamp, TCP_MIN_PORT, SourceTechnologies.GNSS);
    }

    private void reportGeofenceStatus(int status, int flags, double latitude, double longitude, double altitude, float speed, float bearing, float accuracy, long timestamp) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        Location location = buildLocation(flags, latitude, longitude, altitude, speed, bearing, accuracy, timestamp);
        int monitorStatus = STATE_DOWNLOADING;
        if (status == STATE_IDLE) {
            monitorStatus = TCP_MIN_PORT;
        }
        this.mGeofenceHardwareImpl.reportGeofenceMonitorStatus(TCP_MIN_PORT, monitorStatus, location, SourceTechnologies.GNSS);
    }

    private void reportGeofenceAddStatus(int geofenceId, int status) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        this.mGeofenceHardwareImpl.reportGeofenceAddStatus(geofenceId, getGeofenceStatus(status));
    }

    private void reportGeofenceRemoveStatus(int geofenceId, int status) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        this.mGeofenceHardwareImpl.reportGeofenceRemoveStatus(geofenceId, getGeofenceStatus(status));
    }

    private void reportGeofencePauseStatus(int geofenceId, int status) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        this.mGeofenceHardwareImpl.reportGeofencePauseStatus(geofenceId, getGeofenceStatus(status));
    }

    private void reportGeofenceResumeStatus(int geofenceId, int status) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        this.mGeofenceHardwareImpl.reportGeofenceResumeStatus(geofenceId, getGeofenceStatus(status));
    }

    public INetInitiatedListener getNetInitiatedListener() {
        return this.mNetInitiatedListener;
    }

    public void reportNiNotification(int notificationId, int niType, int notifyFlags, int timeout, int defaultResponse, String requestorId, String text, int requestorIdEncoding, int textEncoding, String extras) {
        Log.i(WAKELOCK_KEY, "reportNiNotification: entered");
        Log.i(WAKELOCK_KEY, "notificationId: " + notificationId + ", niType: " + niType + ", notifyFlags: " + notifyFlags + ", timeout: " + timeout + ", defaultResponse: " + defaultResponse);
        Log.i(WAKELOCK_KEY, "requestorId: " + requestorId + ", text: " + text + ", requestorIdEncoding: " + requestorIdEncoding + ", textEncoding: " + textEncoding);
        GpsNiNotification notification = new GpsNiNotification();
        notification.notificationId = notificationId;
        notification.niType = niType;
        notification.needNotify = (notifyFlags & STATE_DOWNLOADING) != 0 ? true : DEBUG;
        notification.needVerify = (notifyFlags & STATE_IDLE) != 0 ? true : DEBUG;
        notification.privacyOverride = (notifyFlags & UPDATE_NETWORK_STATE) != 0 ? true : DEBUG;
        notification.timeout = timeout;
        notification.defaultResponse = defaultResponse;
        notification.requestorId = requestorId;
        notification.text = text;
        notification.requestorIdEncoding = requestorIdEncoding;
        notification.textEncoding = textEncoding;
        Bundle bundle = new Bundle();
        if (extras == null) {
            extras = "";
        }
        Properties extraProp = new Properties();
        try {
            extraProp.load(new StringReader(extras));
        } catch (IOException e) {
            Log.e(WAKELOCK_KEY, "reportNiNotification cannot parse extras data: " + extras);
        }
        for (Entry<Object, Object> ent : extraProp.entrySet()) {
            bundle.putString((String) ent.getKey(), (String) ent.getValue());
        }
        notification.extras = bundle;
        this.mNIHandler.handleNiNotification(notification);
    }

    private void requestSetID(int flags) {
        TelephonyManager phone = (TelephonyManager) this.mContext.getSystemService("phone");
        int type = TCP_MIN_PORT;
        String data = "";
        String data_temp;
        if ((flags & STATE_DOWNLOADING) == STATE_DOWNLOADING) {
            data_temp = phone.getSubscriberId();
            if (data_temp != null) {
                data = data_temp;
                type = STATE_DOWNLOADING;
            }
        } else if ((flags & STATE_IDLE) == STATE_IDLE) {
            data_temp = phone.getLine1Number();
            if (data_temp != null) {
                data = data_temp;
                type = STATE_IDLE;
            }
        }
        native_agps_set_id(type, data);
    }

    private void requestUtcTime() {
        if (this.mRequesetUtcTime) {
            Log.d(WAKELOCK_KEY, "reject utc time request");
            return;
        }
        this.mRequesetUtcTime = true;
        sendMessage(INJECT_NTP_TIME, TCP_MIN_PORT, null);
    }

    private void requestRefLocation(int flags) {
        TelephonyManager phone = (TelephonyManager) this.mContext.getSystemService("phone");
        int phoneType = phone.getPhoneType();
        if (phoneType == STATE_DOWNLOADING) {
            GsmCellLocation gsm_cell = (GsmCellLocation) phone.getCellLocation();
            if (gsm_cell == null || phone.getNetworkOperator() == null || phone.getNetworkOperator().length() <= SET_REQUEST) {
                Log.e(WAKELOCK_KEY, "Error getting cell location info.");
                return;
            }
            int type;
            int mcc = Integer.parseInt(phone.getNetworkOperator().substring(TCP_MIN_PORT, SET_REQUEST));
            int mnc = Integer.parseInt(phone.getNetworkOperator().substring(SET_REQUEST));
            int networkType = phone.getNetworkType();
            if (networkType == SET_REQUEST || networkType == LOCATION_HAS_BEARING || networkType == REMOVE_LISTENER || networkType == INJECT_NTP_TIME_FINISHED || networkType == RELEASE_SUPL_CONNECTION) {
                type = STATE_IDLE;
            } else {
                type = STATE_DOWNLOADING;
            }
            native_agps_set_ref_location_cellid(type, mcc, mnc, gsm_cell.getLac(), gsm_cell.getCid());
        } else if (phoneType == STATE_IDLE) {
            Log.e(WAKELOCK_KEY, "CDMA not supported.");
        }
    }

    private void sendMessage(int message, int arg, Object obj) {
        this.mWakeLock.acquire();
        Log.i(WAKELOCK_KEY, "WakeLock acquired by sendMessage(" + message + ", " + arg + ", " + obj + ")");
        this.mHandler.obtainMessage(message, arg, STATE_DOWNLOADING, obj).sendToTarget();
    }

    private String getDefaultApn() {
        Uri uri = Uri.parse("content://telephony/carriers/preferapn");
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            String[] strArr = new String[STATE_DOWNLOADING];
            strArr[TCP_MIN_PORT] = "apn";
            cursor = contentResolver.query(uri, strArr, null, null, "name ASC");
            if (cursor == null || !cursor.moveToFirst()) {
                Log.e(WAKELOCK_KEY, "No APN found to select.");
                if (cursor != null) {
                    cursor.close();
                }
                return "dummy-apn";
            }
            String string = cursor.getString(TCP_MIN_PORT);
            if (cursor != null) {
                cursor.close();
            }
            return string;
        } catch (Exception e) {
            Log.e(WAKELOCK_KEY, "Error encountered on selecting the APN.", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int getApnIpType(String apn) {
        ensureInHandlerThread();
        if (apn == null) {
            return TCP_MIN_PORT;
        }
        Object[] objArr = new Object[STATE_DOWNLOADING];
        objArr[TCP_MIN_PORT] = apn;
        String selection = String.format("current = 1 and apn = '%s' and carrier_enabled = 1", objArr);
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri uri = Carriers.CONTENT_URI;
            String[] strArr = new String[STATE_DOWNLOADING];
            strArr[TCP_MIN_PORT] = "protocol";
            cursor = contentResolver.query(uri, strArr, selection, null, "name ASC");
            if (cursor == null || !cursor.moveToFirst()) {
                Log.e(WAKELOCK_KEY, "No entry found in query for APN: " + apn);
                if (cursor != null) {
                    cursor.close();
                }
                return TCP_MIN_PORT;
            }
            int translateToApnIpType = translateToApnIpType(cursor.getString(TCP_MIN_PORT), apn);
            if (cursor != null) {
                cursor.close();
            }
            return translateToApnIpType;
        } catch (Exception e) {
            Log.e(WAKELOCK_KEY, "Error encountered on APN query for: " + apn, e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int translateToApnIpType(String ipProtocol, String apn) {
        if ("IP".equals(ipProtocol)) {
            return STATE_DOWNLOADING;
        }
        if ("IPV6".equals(ipProtocol)) {
            return STATE_IDLE;
        }
        if ("IPV4V6".equals(ipProtocol)) {
            return SET_REQUEST;
        }
        Object[] objArr = new Object[STATE_IDLE];
        objArr[TCP_MIN_PORT] = ipProtocol;
        objArr[STATE_DOWNLOADING] = apn;
        Log.e(WAKELOCK_KEY, String.format("Unknown IP Protocol: %s, for APN: %s", objArr));
        return TCP_MIN_PORT;
    }

    private void setRouting() {
        if (this.mAGpsDataConnectionIpAddr != null) {
            if (this.mConnMgr.requestRouteToHostAddress(SET_REQUEST, this.mAGpsDataConnectionIpAddr)) {
                Log.d(WAKELOCK_KEY, "Successfully requested route to host: " + this.mAGpsDataConnectionIpAddr);
            } else {
                Log.e(WAKELOCK_KEY, "Error requesting route to host: " + this.mAGpsDataConnectionIpAddr);
            }
        }
    }

    private boolean isDataNetworkConnected() {
        NetworkInfo activeNetworkInfo = this.mConnMgr.getActiveNetworkInfo();
        return activeNetworkInfo != null ? activeNetworkInfo.isConnected() : DEBUG;
    }

    private void ensureInHandlerThread() {
        if (this.mHandler == null || Looper.myLooper() != this.mHandler.getLooper()) {
            throw new RuntimeException("This method must run on the Handler thread.");
        }
    }

    private String agpsDataConnStateAsString() {
        switch (this.mAGpsDataConnectionState) {
            case TCP_MIN_PORT /*0*/:
                return "CLOSED";
            case STATE_DOWNLOADING /*1*/:
                return "OPENING";
            case STATE_IDLE /*2*/:
                return "OPEN";
            default:
                return "<Unknown>";
        }
    }

    private String agpsDataConnStatusAsString(int agpsDataConnStatus) {
        switch (agpsDataConnStatus) {
            case STATE_DOWNLOADING /*1*/:
                return "REQUEST";
            case STATE_IDLE /*2*/:
                return "RELEASE";
            case SET_REQUEST /*3*/:
                return "CONNECTED";
            case UPDATE_NETWORK_STATE /*4*/:
                return "DONE";
            case INJECT_NTP_TIME /*5*/:
                return "FAILED";
            default:
                return "<Unknown>";
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        StringBuilder s = new StringBuilder();
        s.append("  mFixInterval=").append(this.mFixInterval).append('\n');
        s.append("  mDisableGps (battery saver mode)=").append(this.mDisableGps).append('\n');
        s.append("  mEngineCapabilities=0x").append(Integer.toHexString(this.mEngineCapabilities));
        s.append(" ( ");
        if (hasCapability(STATE_DOWNLOADING)) {
            s.append("SCHEDULING ");
        }
        if (hasCapability(STATE_IDLE)) {
            s.append("MSB ");
        }
        if (hasCapability(UPDATE_NETWORK_STATE)) {
            s.append("MSA ");
        }
        if (hasCapability(LOCATION_HAS_BEARING)) {
            s.append("SINGLE_SHOT ");
        }
        if (hasCapability(LOCATION_HAS_ACCURACY)) {
            s.append("ON_DEMAND_TIME ");
        }
        if (hasCapability(GPS_DELETE_UTC)) {
            s.append("GEOFENCING ");
        }
        if (hasCapability(MAX_SVS)) {
            s.append("MEASUREMENTS ");
        }
        if (hasCapability(GPS_DELETE_SVDIR)) {
            s.append("NAV_MESSAGES ");
        }
        s.append(")\n");
        s.append(native_get_internal_state());
        pw.append(s);
    }

    public boolean isLocalDBEnabled() {
        return DEBUG;
    }

    public void initDefaultApnObserver(Handler handler) {
    }

    public void reportContextStatus(int status) {
    }

    public int getPreferred_accuracy() {
        return TCP_MIN_PORT;
    }

    public boolean shouldReStartNavi() {
        return DEBUG;
    }

    public WorkSource getWorkSource() {
        return this.mWorkSource;
    }

    public void handleGnssRequirementsChange(int changereson) {
    }

    public void handleGnssNavigatingStateChange(boolean start) {
    }
}
