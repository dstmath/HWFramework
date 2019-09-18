package com.android.server.location;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.hardware.location.GeofenceHardwareImpl;
import android.location.FusedBatchOptions;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.IGnssStatusListener;
import android.location.IGnssStatusProvider;
import android.location.IGpsGeofenceHardware;
import android.location.ILocationManager;
import android.location.INetInitiatedListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.util.NetworkConstants;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerSaveState;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CarrierConfigManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.HwLog;
import android.util.Log;
import com.android.internal.app.IBatteryStats;
import com.android.internal.location.GpsNetInitiatedHandler;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.internal.location.gnssmetrics.GnssMetrics;
import com.android.server.HwServiceFactory;
import com.android.server.UiModeManagerService;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.GnssSatelliteBlacklistHelper;
import com.android.server.location.NtpTimeHelper;
import com.android.server.os.HwBootFail;
import com.android.server.usage.AppStandbyController;
import com.huawei.pgmng.log.LogPower;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import libcore.io.IoUtils;

public class GnssLocationProvider implements LocationProviderInterface, NtpTimeHelper.InjectNtpTimeCallback, GnssSatelliteBlacklistHelper.GnssSatelliteBlacklistCallback {
    private static final int ADD_LISTENER = 8;
    private static final int AGPS_APP_STARTED = 1;
    private static final int AGPS_APP_STOPPED = 0;
    private static final int AGPS_DATA_CONNECTION_CLOSED = 0;
    private static final int AGPS_DATA_CONNECTION_OPEN = 2;
    private static final int AGPS_DATA_CONNECTION_OPENING = 1;
    private static final int AGPS_REF_LOCATION_TYPE_GSM_CELLID = 1;
    private static final int AGPS_REF_LOCATION_TYPE_UMTS_CELLID = 2;
    private static final int AGPS_RIL_REQUEST_SETID_IMSI = 1;
    private static final int AGPS_RIL_REQUEST_SETID_MSISDN = 2;
    private static final int AGPS_SETID_TYPE_IMSI = 1;
    private static final int AGPS_SETID_TYPE_MSISDN = 2;
    private static final int AGPS_SETID_TYPE_NONE = 0;
    private static final int AGPS_SUPL_MODE_MSA = 2;
    private static final int AGPS_SUPL_MODE_MSB = 1;
    private static final int AGPS_TYPE_C2K = 2;
    private static final int AGPS_TYPE_SUPL = 1;
    private static final String ALARM_SUPLNI_TIMEOUT = "com.android.internal.location.ALARM_SUPLNI_TIMEOUT";
    private static final String ALARM_TIMEOUT = "com.android.internal.location.ALARM_TIMEOUT";
    private static final String ALARM_WAKEUP = "com.android.internal.location.ALARM_WAKEUP";
    private static final int APN_INVALID = 0;
    private static final int APN_IPV4 = 1;
    private static final int APN_IPV4V6 = 3;
    private static final int APN_IPV6 = 2;
    private static final int CALL_HIBERNATE = 53;
    private static final int CHECK_LOCATION = 1;
    /* access modifiers changed from: private */
    public static final boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable("GnssLocationProvider", 4)));
    private static final String DEBUG_PROPERTIES_FILE = "/etc/gps.conf";
    private static final String DOWNLOAD_EXTRA_WAKELOCK_KEY = "GnssLocationProviderXtraDownload";
    private static final int DOWNLOAD_XTRA_DATA = 6;
    private static final int DOWNLOAD_XTRA_DATA_FINISHED = 11;
    private static final long DOWNLOAD_XTRA_DATA_TIMEOUT_MS = 60000;
    private static final int EMS_SUPL_DNS_REQUEST = 55;
    private static final int ENABLE = 2;
    private static final boolean FORBINDDEN_MSA_SWITCH = SystemProperties.getBoolean("ro.config.forbidden_msa_switch", false);
    private static final int GNSS_REQ_CHANGE_START = 1;
    private static final int GNSS_REQ_CHANGE_STOP = 2;
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
    private static final String HW_XTRA_DOWNLOAD_INTERVAL = "HW_XTRA_DOWNLOAD_INTERVAL";
    private static final float ILLEGAL_BEARING = 1000.0f;
    private static final int INITIALIZE_HANDLER = 13;
    private static final int INJECT_NTP_TIME = 5;
    private static final float ITAR_SPEED_LIMIT_METERS_PER_SECOND = 400.0f;
    private static final String KEY_AGPS_APP_STARTED_NAVIGATION = "agps_app_started_navigation";
    private static final String LAST_SUCCESS_XTRA_DATA_SIZE = "LAST_SUCCESS_XTRA_DATA_SIZE";
    private static final String LAST_XTRA_DOWNLOAD_TIME = "LAST_XTRA_DOWNLOAD_TIME";
    private static final int LISTEN_UDP_PORT = 56;
    private static final int LOCATION_HAS_ALTITUDE = 2;
    private static final int LOCATION_HAS_BEARING = 8;
    private static final int LOCATION_HAS_BEARING_ACCURACY = 128;
    private static final int LOCATION_HAS_HORIZONTAL_ACCURACY = 16;
    private static final int LOCATION_HAS_LAT_LONG = 1;
    private static final int LOCATION_HAS_SPEED = 4;
    private static final int LOCATION_HAS_SPEED_ACCURACY = 64;
    private static final int LOCATION_HAS_VERTICAL_ACCURACY = 32;
    private static final int LOCATION_INVALID = 0;
    private static final long LOCATION_UPDATE_DURATION_MILLIS = 0;
    private static final long LOCATION_UPDATE_MIN_TIME_INTERVAL_MILLIS = 1000;
    private static final String LPP_PROFILE = "persist.sys.gps.lpp";
    private static final long MAX_RETRY_INTERVAL = 14400000;
    private static final long MAX_SUPL_NI_TIMEOUT = 120000;
    private static final float MIX_SPEED = 0.0f;
    protected static final int MSG_REPORT_IDLE = 57;
    private static final int NO_FIX_TIMEOUT = 60000;
    private static final int NTP_TIME_INJECT_CLOSE = 0;
    private static final String NTP_TIME_INJECT_FLAG = "hw_ntp_time_inject";
    private static final int PORT_NO = 7275;
    private static final ProviderProperties PROPERTIES;
    private static final String PROPERTIES_FILE_PREFIX = "gps";
    private static final int QUICK_START_COMMOND = 50;
    private static final int QUICK_STOP_COMMOND = 51;
    private static final int QUICK_TTFF = 128;
    private static final long RECENT_FIX_TIMEOUT = 10000;
    private static final int RELEASE_SUPL_CONNECTION = 15;
    private static final int REMOVE_LISTENER = 9;
    private static final int REPORT_LOCATION = 17;
    private static final int REPORT_SV_STATUS = 18;
    private static final int REQUEST_LOCATION = 16;
    private static final int REQUEST_NETWORK_TIMEOUT = 8000;
    private static final int REQUEST_SUPL_CONNECTION = 14;
    private static final long RETRY_INTERVAL = 300000;
    private static final int SET_REQUEST = 3;
    private static final String SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private static final int START_NAVIGATING = 52;
    private static final int STATE_DOWNLOADING = 1;
    private static final int STATE_IDLE = 2;
    private static final int STATE_PENDING_NETWORK = 0;
    private static final int SUBSCRIPTION_OR_SIM_CHANGED = 12;
    /* access modifiers changed from: private */
    public static final boolean SUPL_ES_PDN_SWITCH = SystemProperties.getBoolean("ro.config.supl_es_pdn_switch", false);
    /* access modifiers changed from: private */
    public static final boolean SUPL_KDDI_SWITCH = SystemProperties.getBoolean("ro.config.supl_kddi_switch", false);
    private static final int SUPL_MAX_DATA_LEN = 2000;
    private static final boolean SUPL_UDP_SWITCH = SystemProperties.getBoolean("ro.config.supl_udp_switch", false);
    private static final String TAG = "GnssLocationProvider";
    private static final int TCP_MAX_PORT = 65535;
    private static final int TCP_MIN_PORT = 0;
    private static final int UPDATE_LOCATION = 7;
    private static final int UPDATE_LOWPOWER_MODE = 54;
    private static final int UPDATE_NETWORK_STATE = 4;
    private static final String WAKELOCK_KEY = "GnssLocationProvider";
    private static boolean mWcdmaVpEnabled = SystemProperties.getBoolean("ro.hwpp.wcdma_voice_preference", false);
    /* access modifiers changed from: private */
    public boolean isRealQuick = false;
    private InetAddress mAGpsDataConnectionIpAddr;
    private int mAGpsDataConnectionState;
    private Object mAlarmLock = new Object();
    private final AlarmManager mAlarmManager;
    private final AppOpsManager mAppOps;
    private final IBatteryStats mBatteryStats;
    /* access modifiers changed from: private */
    public final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("GnssLocationProvider", "receive broadcast intent, action: " + action);
            if (action != null) {
                if (action.equals(GnssLocationProvider.ALARM_WAKEUP) && GnssLocationProvider.this.isEnabled()) {
                    GnssLocationProvider.this.sendMessage(52, 0, null);
                } else if (action.equals(GnssLocationProvider.ALARM_TIMEOUT)) {
                    GnssLocationProvider.this.sendMessage(53, 0, null);
                } else if ("android.os.action.POWER_SAVE_MODE_CHANGED".equals(action) || "android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(action) || "android.intent.action.SCREEN_OFF".equals(action) || "android.intent.action.SCREEN_ON".equals(action)) {
                    GnssLocationProvider.this.sendMessage(54, 0, null);
                } else if (action.equals(GnssLocationProvider.SIM_STATE_CHANGED)) {
                    GnssLocationProvider.this.subscriptionOrSimChanged(context);
                } else if (action.equals("android.intent.action.DATA_SMS_RECEIVED")) {
                    GnssLocationProvider.this.checkSmsSuplInit(intent);
                } else if (action.equals("android.provider.Telephony.WAP_PUSH_RECEIVED")) {
                    GnssLocationProvider.this.checkWapSuplInit(intent);
                } else if (action.equals(GnssLocationProvider.ALARM_SUPLNI_TIMEOUT) && GnssLocationProvider.SUPL_ES_PDN_SWITCH) {
                    if (!GnssLocationProvider.this.isEnabled() && GnssLocationProvider.this.isEnabledBackGround()) {
                        GnssLocationProvider.this.native_cleanup();
                    }
                    synchronized (GnssLocationProvider.this.mLock) {
                        boolean unused = GnssLocationProvider.this.mEnabledBackGround = false;
                    }
                }
            }
        }
    };
    private String mC2KServerHost;
    private int mC2KServerPort;
    private final PendingIntent mClearupIntent;
    private WorkSource mClientSource = new WorkSource();
    /* access modifiers changed from: private */
    public final ConnectivityManager mConnMgr;
    /* access modifiers changed from: private */
    public final Context mContext;
    private String mDefaultApn;
    /* access modifiers changed from: private */
    public ContentObserver mDefaultApnObserver;
    private boolean mDisableGps = false;
    /* access modifiers changed from: private */
    public int mDownloadXtraDataPending = 0;
    /* access modifiers changed from: private */
    public final PowerManager.WakeLock mDownloadXtraWakeLock;
    private boolean mEnabled;
    /* access modifiers changed from: private */
    public boolean mEnabledBackGround = false;
    /* access modifiers changed from: private */
    public int mEngineCapabilities;
    /* access modifiers changed from: private */
    public boolean mEngineOn;
    /* access modifiers changed from: private */
    public InetAddress mEsuplIpAddress = null;
    private int mFixInterval = 1000;
    private long mFixRequestTime = 0;
    private final LocationChangeListener mFusedLocationListener = new FusedLocationListener();
    private GeofenceHardwareImpl mGeofenceHardwareImpl;
    private final GnssBatchingProvider mGnssBatchingProvider;
    private final GnssGeofenceProvider mGnssGeofenceProvider;
    /* access modifiers changed from: private */
    public final GnssMeasurementsProvider mGnssMeasurementsProvider;
    /* access modifiers changed from: private */
    public GnssMetrics mGnssMetrics;
    /* access modifiers changed from: private */
    public final GnssNavigationMessageProvider mGnssNavigationMessageProvider;
    private final GnssSatelliteBlacklistHelper mGnssSatelliteBlacklistHelper;
    private final IGnssStatusProvider mGnssStatusProvider = new IGnssStatusProvider.Stub() {
        public void registerGnssStatusCallback(IGnssStatusListener callback) {
            GnssLocationProvider.this.mListenerHelper.addListener(callback, GnssLocationProvider.this.mPackageManager.getPackagesForUid(Binder.getCallingUid())[0]);
        }

        public void unregisterGnssStatusCallback(IGnssStatusListener callback) {
            GnssLocationProvider.this.mListenerHelper.removeListener(callback);
        }
    };
    /* access modifiers changed from: private */
    public final BroadcastReceiver mGpsLocalLocationReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("GnssLocationProvider", "receive broadcast intent, action: " + action);
            if (action != null && action.equals(IHwLocalLocationManager.ACTION_INJECT_LOCATION) && GnssLocationProvider.this.mEngineOn && GnssLocationProvider.this.mStarted) {
                Log.e("GnssLocationProvider", "mEngineOn && mStarted ,get location from DB, inject to GPS  Module");
                GnssLocationProvider.this.sendMessage(7, 0, intent.getParcelableExtra(IHwLocalLocationManager.EXTRA_KEY_LOCATION_CHANGED));
            }
        }
    };
    IHwGpsXtraDownloadReceiver mGpsXtraReceiver;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public volatile String mHardwareModelName;
    /* access modifiers changed from: private */
    public volatile int mHardwareYear = 0;
    private IHwCmccGpsFeature mHwCmccGpsFeature;
    private IHwGpsLocationCustFeature mHwGpsLocationCustFeature;
    IHwGpsLocationManager mHwGpsLocationManager;
    /* access modifiers changed from: private */
    public IHwGpsLogServices mHwGpsLogServices;
    private final ILocationManager mILocationManager;
    private volatile boolean mItarSpeedLimitExceeded = false;
    private long mLastFixTime;
    /* access modifiers changed from: private */
    public final GnssStatusListenerHelper mListenerHelper;
    private final LocationExtras mLocationExtras = new LocationExtras();
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private boolean mLowPowerMode = false;
    private final GpsNetInitiatedHandler mNIHandler;
    private boolean mNavigating;
    boolean mNeedEmergencyApn = false;
    private final INetInitiatedListener mNetInitiatedListener = new INetInitiatedListener.Stub() {
        public boolean sendNiResponse(int notificationId, int userResponse) {
            Log.i("GnssLocationProvider", "sendNiResponse, notifId: " + notificationId + ", response: " + userResponse);
            GnssLocationProvider.this.native_send_ni_response(notificationId, userResponse);
            return true;
        }
    };
    /* access modifiers changed from: private */
    public final ConnectivityManager.NetworkCallback mNetworkConnectivityCallback = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            NetworkInfo info = GnssLocationProvider.this.mConnMgr.getNetworkInfo(network);
            GnssLocationProvider.this.mNtpTimeHelper.onNetworkAvailable();
            if (info != null) {
                if (GnssLocationProvider.this.mGpsXtraReceiver != null) {
                    if (GnssLocationProvider.this.mGpsXtraReceiver.handleUpdateNetworkState(info, GnssLocationProvider.this.mDownloadXtraDataPending == 0) && GnssLocationProvider.this.mSupportsXtra) {
                        GnssLocationProvider.this.sendMessage(6, 0, null);
                    }
                }
                GnssLocationProvider.this.mHwGpsLogServices.updateNetworkState(info);
            }
            GnssLocationProvider.this.sendMessage(4, 0, network);
        }

        public void onLost(Network network) {
            GnssLocationProvider.this.sendMessage(4, 0, network);
        }
    };
    /* access modifiers changed from: private */
    public Network mNetworkEs = null;
    private final LocationChangeListener mNetworkLocationListener = new NetworkLocationListener();
    private byte[] mNmeaBuffer = new byte[120];
    /* access modifiers changed from: private */
    public final NtpTimeHelper mNtpTimeHelper;
    /* access modifiers changed from: private */
    public final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangedListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        public void onSubscriptionsChanged() {
            GnssLocationProvider.this.sendMessage(12, 0, null);
        }
    };
    /* access modifiers changed from: private */
    public final PackageManager mPackageManager;
    private volatile int mPositionMode;
    private final PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public Properties mProperties;
    private ProviderRequest mProviderRequest = null;
    private Object mQuickLock = new Object();
    /* access modifiers changed from: private */
    public boolean mRealStoped = true;
    private boolean mRequesetUtcTime = false;
    private boolean mSingleShot;
    /* access modifiers changed from: private */
    public boolean mStarted;
    private int mStatus = 1;
    private long mStatusUpdateTime = SystemClock.elapsedRealtime();
    private final ConnectivityManager.NetworkCallback mSuplConnectivityCallback = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            GnssLocationProvider.this.sendMessage(4, 0, network);
        }

        public void onLost(Network network) {
            GnssLocationProvider.this.releaseSuplConnection(2);
        }
    };
    /* access modifiers changed from: private */
    public final ConnectivityManager.NetworkCallback mSuplConnectivityCallbackEs = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            boolean result;
            NetworkInfo info = GnssLocationProvider.this.mConnMgr.getNetworkInfo(network);
            if (info != null) {
                Log.d("GnssLocationProvider", "Network onAvailable NetworkInfo: " + info);
                boolean isConnected = info.isConnected();
                Network unused = GnssLocationProvider.this.mNetworkEs = network;
                if (isConnected) {
                    try {
                        InetAddress agpsDataConnectionIpAddr = network.getAllByName(GnssLocationProvider.this.mSuplServerHostES)[0];
                        Log.v("GnssLocationProvider", "DNS query(use network.getAllByName) success: " + agpsDataConnectionIpAddr);
                        if (GnssLocationProvider.this.mNeedEmergencyApn) {
                            result = GnssLocationProvider.this.mConnMgr.requestRouteToHostAddress(15, agpsDataConnectionIpAddr);
                            Log.e("GnssLocationProvider", "mSuplConnectivityCallbackEs-EMERGENCY");
                            InetAddress unused2 = GnssLocationProvider.this.mEsuplIpAddress = agpsDataConnectionIpAddr;
                        } else {
                            result = GnssLocationProvider.this.mConnMgr.requestRouteToHostAddress(3, agpsDataConnectionIpAddr);
                            Log.e("GnssLocationProvider", "mSuplConnectivityCallbackEs-SUPL");
                        }
                        if (!result) {
                            Log.e("GnssLocationProvider", "Error requesting route to host: " + agpsDataConnectionIpAddr);
                        } else {
                            Log.d("GnssLocationProvider", "Successfully requested route to host: " + agpsDataConnectionIpAddr);
                        }
                        if (agpsDataConnectionIpAddr != null) {
                            if (GnssLocationProvider.SUPL_KDDI_SWITCH) {
                                GnssLocationProvider.this.native_agps_ni_message(GnssLocationProvider.this.mSuplinit, GnssLocationProvider.this.mSuplinitlength);
                                Arrays.fill(GnssLocationProvider.this.mSuplinit, (byte) 0);
                            } else {
                                Log.v("GnssLocationProvider", "agpsDataConnectionIpAddr getHostAddress" + agpsDataConnectionIpAddr.getHostAddress());
                                GnssLocationProvider.native_set_supl_host_ip(agpsDataConnectionIpAddr.getHostAddress());
                            }
                            boolean unused3 = GnssLocationProvider.this.mSuplEsConnected = true;
                        }
                    } catch (UnknownHostException e) {
                        Log.e("GnssLocationProvider", "DNS query(use network.getAllByName) fail: " + GnssLocationProvider.this.mSuplServerHostES, e);
                        try {
                            GnssLocationProvider.this.mConnMgr.unregisterNetworkCallback(GnssLocationProvider.this.mSuplConnectivityCallbackEs);
                        } catch (IllegalArgumentException e2) {
                            Log.e("GnssLocationProvider", "DNS query fail,duplicate unregisterNetworkCallbackES");
                        }
                        Network unused4 = GnssLocationProvider.this.mNetworkEs = null;
                    }
                }
            }
        }

        public void onLost(Network network) {
            Log.e("GnssLocationProvider", "SUPL ES Network Lost: " + network);
            GnssLocationProvider.this.releaseSuplConnection(2);
        }
    };
    /* access modifiers changed from: private */
    public boolean mSuplEsConnected = false;
    private boolean mSuplEsEnabled = false;
    private String mSuplServerHost;
    /* access modifiers changed from: private */
    public String mSuplServerHostES;
    private int mSuplServerPort = 0;
    /* access modifiers changed from: private */
    public byte[] mSuplinit = new byte[2000];
    /* access modifiers changed from: private */
    public int mSuplinitlength = 0;
    /* access modifiers changed from: private */
    public boolean mSupportsXtra;
    private int mTimeToFirstFix = 0;
    private final PendingIntent mTimeoutIntent;
    /* access modifiers changed from: private */
    public InetAddress mUdpIpAddress;
    private final PowerManager.WakeLock mWakeLock;
    private final PendingIntent mWakeupIntent;
    private WorkSource mWorkSource = null;
    /* access modifiers changed from: private */
    public final ExponentialBackOff mXtraBackOff = new ExponentialBackOff(300000, 14400000);
    private HwQuickTTFFMonitor nHwQuickTTFFMonitor;
    private int ntpSwitch;
    private UDPServerThread udpServerThread = null;

    private final class FusedLocationListener extends LocationChangeListener {
        private FusedLocationListener() {
            super();
        }

        public void onLocationChanged(Location location) {
            if ("fused".equals(location.getProvider())) {
                GnssLocationProvider.this.injectBestLocation(location);
            }
        }
    }

    public interface GnssMetricsProvider {
        String getGnssMetricsAsProtoString();
    }

    public interface GnssSystemInfoProvider {
        String getGnssHardwareModelName();

        int getGnssYearOfHardware();
    }

    private static class GpsRequest {
        public ProviderRequest request;
        public WorkSource source;

        public GpsRequest(ProviderRequest request2, WorkSource source2) {
            this.request = request2;
            this.source = source2;
        }
    }

    private abstract class LocationChangeListener implements LocationListener {
        int numLocationUpdateRequest;

        private LocationChangeListener() {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    }

    private static class LocationExtras {
        private final Bundle mBundle = new Bundle();
        private int mMaxCn0;
        private int mMeanCn0;
        private int mSvCount;

        public void set(int svCount, int meanCn0, int maxCn0) {
            synchronized (this) {
                this.mSvCount = svCount;
                this.mMeanCn0 = meanCn0;
                this.mMaxCn0 = maxCn0;
            }
            setBundle(this.mBundle);
        }

        public void reset() {
            set(0, 0, 0);
        }

        public void setBundleExtra(String key, int value) {
            synchronized (this) {
                this.mBundle.putInt(key, value);
            }
        }

        public void setBundle(Bundle extras) {
            if (extras != null) {
                synchronized (this) {
                    extras.putInt("satellites", this.mSvCount);
                    extras.putInt("meanCn0", this.mMeanCn0);
                    extras.putInt("maxCn0", this.mMaxCn0);
                }
            }
        }

        public Bundle getBundle() {
            Bundle bundle;
            synchronized (this) {
                bundle = new Bundle(this.mBundle);
            }
            return bundle;
        }
    }

    private final class NetworkLocationListener extends LocationChangeListener {
        private NetworkLocationListener() {
            super();
        }

        public void onLocationChanged(Location location) {
            if ("network".equals(location.getProvider())) {
                GnssLocationProvider.this.handleUpdateLocation(location);
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
                case 2:
                    if (msg.arg1 != 1) {
                        GnssLocationProvider.this.handleDisable();
                        break;
                    } else {
                        GnssLocationProvider.this.handleEnable();
                        break;
                    }
                case 3:
                    GpsRequest gpsRequest = (GpsRequest) msg.obj;
                    GnssLocationProvider.this.handleSetRequest(gpsRequest.request, gpsRequest.source);
                    break;
                case 4:
                    GnssLocationProvider.this.handleUpdateNetworkState((Network) msg.obj);
                    break;
                case 5:
                    GnssLocationProvider.this.mNtpTimeHelper.retrieveAndInjectNtpTime();
                    break;
                case 6:
                    GnssLocationProvider.this.handleDownloadXtraData();
                    break;
                case 7:
                    GnssLocationProvider.this.handleUpdateLocation((Location) msg.obj);
                    break;
                default:
                    boolean isInteractive = false;
                    switch (message) {
                        case 11:
                            int unused = GnssLocationProvider.this.mDownloadXtraDataPending = 2;
                            break;
                        case 12:
                            GnssLocationProvider.this.subscriptionOrSimChanged(GnssLocationProvider.this.mContext);
                            break;
                        case 13:
                            handleInitialize();
                            break;
                        case 14:
                            GnssLocationProvider.this.handleRequestSuplConnection((InetAddress) msg.obj);
                            break;
                        case 15:
                            GnssLocationProvider.this.handleReleaseSuplConnection(msg.arg1);
                            break;
                        case 16:
                            GnssLocationProvider.this.handleRequestLocation(((Boolean) msg.obj).booleanValue());
                            break;
                        case 17:
                            GnssLocationProvider gnssLocationProvider = GnssLocationProvider.this;
                            if (msg.arg1 == 1) {
                                isInteractive = true;
                            }
                            gnssLocationProvider.handleReportLocation(isInteractive, (Location) msg.obj);
                            break;
                        case 18:
                            GnssLocationProvider.this.handleReportSvStatus((SvStatusInfo) msg.obj);
                            break;
                        default:
                            switch (message) {
                                case 50:
                                    if (GnssLocationProvider.this.isRealQuick) {
                                        GnssLocationProvider.this.native_start_quick_ttff();
                                        break;
                                    }
                                    break;
                                case 51:
                                    GnssLocationProvider.this.native_stop_quick_ttff();
                                    break;
                                case 52:
                                    if (!GnssLocationProvider.this.mRealStoped) {
                                        GnssLocationProvider.this.startNavigating(false);
                                        break;
                                    }
                                    break;
                                case 53:
                                    GnssLocationProvider.this.hibernate();
                                    break;
                                case 54:
                                    if (GnssLocationProvider.this.hwCheckLowPowerMode()) {
                                        GnssLocationProvider.this.updateLowPowerMode();
                                        break;
                                    }
                                    break;
                                case 55:
                                    GnssLocationProvider.this.handleRequestSuplConnectionES();
                                    break;
                                case 56:
                                    GnssLocationProvider.this.startUDPServer();
                                    break;
                                case 57:
                                    boolean isGnssStarted = msg.arg1 == 1;
                                    if (msg.arg2 == 1) {
                                        isInteractive = true;
                                    }
                                    GnssLocationProvider.this.handleReportIdle(isGnssStarted, isInteractive);
                                    break;
                            }
                    }
            }
            if (msg.arg2 == 1) {
                GnssLocationProvider.this.wakelockRelease();
                if (Log.isLoggable("GnssLocationProvider", 4)) {
                    Log.i("GnssLocationProvider", "WakeLock released by handleMessage(" + GnssLocationProvider.this.messageIdAsString(message) + ", " + msg.arg1 + ", " + msg.obj + ")");
                }
            }
        }

        private void handleInitialize() {
            GnssLocationProvider.native_init_once();
            if ("factory".equalsIgnoreCase(SystemProperties.get("ro.runmode", UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN))) {
                Log.d("GnssLocationProvider", "not initialize on factory version");
            } else if (!GnssLocationProvider.this.native_init()) {
                Log.w("GnssLocationProvider", "Native initialization failed at bootup");
            } else {
                GnssLocationProvider.this.native_cleanup();
            }
            GnssLocationProvider.this.reloadGpsProperties(GnssLocationProvider.this.mContext, GnssLocationProvider.this.mProperties);
            SubscriptionManager.from(GnssLocationProvider.this.mContext).addOnSubscriptionsChangedListener(GnssLocationProvider.this.mOnSubscriptionsChangedListener);
            if (GnssLocationProvider.native_is_agps_ril_supported()) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.DATA_SMS_RECEIVED");
                intentFilter.addDataScheme("sms");
                intentFilter.addDataAuthority("localhost", "7275");
                GnssLocationProvider.this.mContext.registerReceiver(GnssLocationProvider.this.mBroadcastReceiver, intentFilter, null, this);
                IntentFilter intentFilter2 = new IntentFilter();
                intentFilter2.addAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
                try {
                    intentFilter2.addDataType("application/vnd.omaloc-supl-init");
                } catch (IntentFilter.MalformedMimeTypeException e) {
                    Log.w("GnssLocationProvider", "Malformed SUPL init mime type");
                }
                GnssLocationProvider.this.mContext.registerReceiver(GnssLocationProvider.this.mBroadcastReceiver, intentFilter2, null, this);
            } else {
                Log.i("GnssLocationProvider", "Skipped registration for SMS/WAP-PUSH messages because AGPS Ril in GPS HAL is not supported");
            }
            IntentFilter intentFilter3 = new IntentFilter();
            intentFilter3.addAction(GnssLocationProvider.ALARM_WAKEUP);
            intentFilter3.addAction(GnssLocationProvider.ALARM_TIMEOUT);
            intentFilter3.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
            intentFilter3.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
            intentFilter3.addAction("android.intent.action.SCREEN_OFF");
            intentFilter3.addAction("android.intent.action.SCREEN_ON");
            intentFilter3.addAction(GnssLocationProvider.SIM_STATE_CHANGED);
            GnssLocationProvider.this.mContext.registerReceiver(GnssLocationProvider.this.mBroadcastReceiver, intentFilter3, null, this);
            IntentFilter intentFilter4 = new IntentFilter();
            intentFilter4.addAction(IHwLocalLocationManager.ACTION_INJECT_LOCATION);
            GnssLocationProvider.this.mContext.registerReceiver(GnssLocationProvider.this.mGpsLocalLocationReceiver, intentFilter4, GnssLocationProvider.GPS_INJECT_LOCATION_PERMISSION, this);
            GnssLocationProvider.this.initDefaultApnObserver(GnssLocationProvider.this.mHandler);
            GnssLocationProvider.this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://telephony/carriers/preferapn"), false, GnssLocationProvider.this.mDefaultApnObserver);
            NetworkRequest.Builder networkRequestBuilder = new NetworkRequest.Builder();
            networkRequestBuilder.addCapability(12);
            networkRequestBuilder.addCapability(16);
            networkRequestBuilder.removeCapability(15);
            GnssLocationProvider.this.mConnMgr.registerNetworkCallback(networkRequestBuilder.build(), GnssLocationProvider.this.mNetworkConnectivityCallback);
            LocationRequest request = LocationRequest.createFromDeprecatedProvider("passive", 0, GnssLocationProvider.MIX_SPEED, false);
            request.setHideFromAppOps(true);
            ((LocationManager) GnssLocationProvider.this.mContext.getSystemService("location")).requestLocationUpdates(request, new NetworkLocationListener(), getLooper());
        }
    }

    interface SetCarrierProperty {
        boolean set(int i);
    }

    private static class SvStatusInfo {
        public float[] mCn0s;
        public float[] mSvAzimuths;
        public float[] mSvCarrierFreqs;
        public int mSvCount;
        public float[] mSvElevations;
        public int[] mSvidWithFlags;

        private SvStatusInfo() {
        }
    }

    private class UDPServerThread extends Thread {
        private boolean isStop;
        private byte[] receiveData;
        private DatagramSocket serverSocket;

        public UDPServerThread(String threadName) {
            super(threadName);
            this.receiveData = null;
            this.isStop = false;
            this.serverSocket = null;
            this.receiveData = new byte[4096];
        }

        public void run() {
            try {
                Log.i("GnssLocationProvider", " UDP server thread execution");
                this.serverSocket = new DatagramSocket(GnssLocationProvider.PORT_NO);
                DatagramPacket receivePacket = new DatagramPacket(this.receiveData, this.receiveData.length);
                while (!this.isStop) {
                    try {
                        Log.i("GnssLocationProvider", " Datagram ServerThread start receive");
                        this.serverSocket.receive(receivePacket);
                        Log.i("GnssLocationProvider", " Datagram ServerThread after receive");
                        if (receivePacket != null) {
                            GnssLocationProvider.this.SetEnabledBackGround();
                            int iOffset = receivePacket.getOffset();
                            int iLength = receivePacket.getLength();
                            Log.i("GnssLocationProvider", " Receive Offset = " + iOffset + ", Length = " + iLength);
                            InetAddress unused = GnssLocationProvider.this.mUdpIpAddress = receivePacket.getAddress();
                            int port = receivePacket.getPort();
                            if (GnssLocationProvider.this.mUdpIpAddress != null) {
                                Log.i("GnssLocationProvider", " RECEIVED IP Address: " + GnssLocationProvider.this.mUdpIpAddress + " port = " + port);
                            }
                            byte[] bData = new byte[iLength];
                            System.arraycopy(receivePacket.getData(), iOffset, bData, 0, iLength);
                            if (GnssLocationProvider.SUPL_KDDI_SWITCH) {
                                Log.i("GnssLocationProvider", "[NI][checkUDPSuplinit]:handleforEPDN");
                                GnssLocationProvider.this.handleforKDDI(bData);
                            } else {
                                GnssLocationProvider.this.native_agps_ni_message(bData, bData.length);
                            }
                        }
                    } catch (SocketException e1) {
                        if (this.isStop) {
                            Log.i("GnssLocationProvider", " DatagramSocket closed");
                        } else {
                            Log.e("GnssLocationProvider", " DatagramSocket exception ", e1);
                        }
                    } catch (IOException e) {
                        if (this.isStop) {
                            Log.i("GnssLocationProvider", " DatagramSocket closed");
                        } else {
                            Log.e("GnssLocationProvider", " DatagramSocket exception ", e);
                        }
                    }
                }
            } catch (SocketException e2) {
                Log.e("GnssLocationProvider", " DatagramSocket create exception ", e2);
            }
        }

        /* access modifiers changed from: private */
        public void stopThread() {
            interrupt();
            this.isStop = true;
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
        }
    }

    private static native void class_init_native();

    private native void native_agps_data_conn_closed();

    private native void native_agps_data_conn_failed();

    private native void native_agps_data_conn_open(String str, int i);

    /* access modifiers changed from: private */
    public native void native_agps_ni_message(byte[] bArr, int i);

    private native void native_agps_set_id(int i, String str);

    private native void native_agps_set_ref_location_cellid(int i, int i2, int i3, int i4, int i5);

    /* access modifiers changed from: private */
    public native void native_cleanup();

    private static native void native_cleanup_batching();

    public static native String native_decode_suplinit(byte[] bArr, int i);

    private native void native_delete_aiding_data(int i);

    private static native void native_flush_batch();

    private static native int native_get_batch_size();

    private native String native_get_internal_state();

    /* access modifiers changed from: private */
    public native boolean native_init();

    private static native boolean native_init_batching();

    /* access modifiers changed from: private */
    public static native void native_init_once();

    private native void native_inject_best_location(int i, double d, double d2, double d3, float f, float f2, float f3, float f4, float f5, float f6, long j);

    private native void native_inject_location(double d, double d2, float f);

    private native void native_inject_time(long j, long j2, int i);

    /* access modifiers changed from: private */
    public native void native_inject_xtra_data(byte[] bArr, int i);

    /* access modifiers changed from: private */
    public static native boolean native_is_agps_ril_supported();

    private static native boolean native_is_gnss_configuration_supported();

    private static native boolean native_is_supported();

    private native int native_read_nmea(byte[] bArr, int i);

    /* access modifiers changed from: private */
    public native void native_send_ni_response(int i, int i2);

    private native void native_set_agps_server(int i, String str, int i2);

    /* access modifiers changed from: private */
    public static native boolean native_set_emergency_supl_pdn(int i);

    /* access modifiers changed from: private */
    public static native boolean native_set_gnss_pos_protocol_select(int i);

    /* access modifiers changed from: private */
    public static native boolean native_set_gps_lock(int i);

    /* access modifiers changed from: private */
    public static native boolean native_set_lpp_profile(int i);

    private native boolean native_set_position_mode(int i, int i2, int i3, int i4, int i5, boolean z);

    /* access modifiers changed from: private */
    public static native boolean native_set_satellite_blacklist(int[] iArr, int[] iArr2);

    /* access modifiers changed from: private */
    public static native boolean native_set_supl_es(int i);

    /* access modifiers changed from: private */
    public static native void native_set_supl_host_ip(String str);

    /* access modifiers changed from: private */
    public static native boolean native_set_supl_mode(int i);

    /* access modifiers changed from: private */
    public static native boolean native_set_supl_version(int i);

    private native boolean native_start();

    private static native boolean native_start_batch(long j, boolean z);

    /* access modifiers changed from: private */
    public native void native_start_quick_ttff();

    private native boolean native_stop();

    private static native boolean native_stop_batch();

    /* access modifiers changed from: private */
    public native void native_stop_quick_ttff();

    private native boolean native_supports_xtra();

    private native void native_update_network_state(boolean z, int i, boolean z2, boolean z3, String str, String str2);

    static {
        ProviderProperties providerProperties = new ProviderProperties(true, true, false, false, true, true, true, 3, 1);
        PROPERTIES = providerProperties;
        class_init_native();
    }

    private synchronized int getPositionMode() {
        return this.mPositionMode;
    }

    public IGnssStatusProvider getGnssStatusProvider() {
        return this.mGnssStatusProvider;
    }

    public IGpsGeofenceHardware getGpsGeofenceProxy() {
        return this.mGnssGeofenceProvider;
    }

    public GnssMeasurementsProvider getGnssMeasurementsProvider() {
        return this.mGnssMeasurementsProvider;
    }

    public GnssNavigationMessageProvider getGnssNavigationMessageProvider() {
        return this.mGnssNavigationMessageProvider;
    }

    /* access modifiers changed from: private */
    public void checkSmsSuplInit(Intent intent) {
        if (!this.mHwCmccGpsFeature.checkSuplInit()) {
            Object[] pdus = (Object[]) intent.getExtra("pdus");
            if (pdus == null) {
                Log.i("GnssLocationProvider", "pdus is null");
                return;
            }
            byte[] supl_init = SmsMessage.createFromPdu((byte[]) pdus[0]).getUserData();
            if (supl_init == null || supl_init.length == 0) {
                Log.i("GnssLocationProvider", "Message is null");
                return;
            }
            Log.i("GnssLocationProvider", "[NI][checkSmsSuplInit]:++");
            if (SUPL_KDDI_SWITCH) {
                Log.i("GnssLocationProvider", "[NI][checkSmsSuplInit]:handleforEPDN");
                handleforKDDI(supl_init);
            } else {
                native_agps_ni_message(supl_init, supl_init.length);
            }
        }
    }

    /* access modifiers changed from: private */
    public void checkWapSuplInit(Intent intent) {
        if (!this.mHwCmccGpsFeature.checkSuplInit()) {
            byte[] suplInit = intent.getByteArrayExtra("data");
            if (suplInit != null) {
                String applicationId = intent.getStringExtra("x-application-id-field");
                if (applicationId != null) {
                    Log.d("GnssLocationProvider", "applicationId: " + applicationId);
                    if (!"16".equals(applicationId) && !"x-oma-application:ulp.ua".equals(applicationId)) {
                        return;
                    }
                }
                if (SUPL_ES_PDN_SWITCH) {
                    SetEnabledBackGround();
                }
                Log.i("GnssLocationProvider", "[NI][checkWapSuplInit]:++");
                if (SUPL_KDDI_SWITCH) {
                    Log.i("GnssLocationProvider", "[NI][checkWapSuplInit]:handleforEPDN");
                    handleforKDDI(suplInit);
                } else {
                    native_agps_ni_message(suplInit, suplInit.length);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void startUDPServer() {
        Log.i("GnssLocationProvider", "startUDPServer invoked");
        this.udpServerThread = new UDPServerThread("UDP Server thread");
        this.udpServerThread.start();
    }

    private void stopUDPServer() {
        if (this.udpServerThread != null) {
            Log.i("GnssLocationProvider", "stopUDPServer invoked");
            this.udpServerThread.stopThread();
        }
    }

    /* access modifiers changed from: private */
    public void handleforKDDI(byte[] bData) {
        String fqdn = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        Arrays.fill(this.mSuplinit, (byte) 0);
        System.arraycopy(bData, 0, this.mSuplinit, 0, bData.length);
        this.mSuplinitlength = bData.length;
        try {
            fqdn = native_decode_suplinit(bData, bData.length);
        } catch (Exception e) {
            Log.e("GnssLocationProvider", "Exception happened:" + e);
            e.printStackTrace();
        }
        checkIfNeedEmergencyApn(fqdn);
        if (!this.mNeedEmergencyApn) {
            native_agps_ni_message(bData, bData.length);
            return;
        }
        this.mSuplServerHostES = fqdn;
        handleRequestSuplConnectionES();
    }

    public void onUpdateSatelliteBlacklist(int[] constellations, int[] svids) {
        this.mHandler.post(new Runnable(constellations, svids) {
            private final /* synthetic */ int[] f$0;
            private final /* synthetic */ int[] f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run() {
                GnssLocationProvider.native_set_satellite_blacklist(this.f$0, this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    public void subscriptionOrSimChanged(Context context) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "received SIM related action: ");
        }
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        String mccMnc = ((TelephonyManager) this.mContext.getSystemService("phone")).getSimOperator();
        boolean isKeepLppProfile = false;
        if (!TextUtils.isEmpty(mccMnc)) {
            if (DEBUG) {
                Log.d("GnssLocationProvider", "SIM MCC/MNC is available: " + mccMnc);
            }
            synchronized (this.mLock) {
                if (configManager != null) {
                    try {
                        PersistableBundle b = configManager.getConfig();
                        if (b != null) {
                            isKeepLppProfile = b.getBoolean("persist_lpp_mode_bool");
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                if (!isKeepLppProfile || SystemProperties.getBoolean("ro.config.hw_agps_adpt_sim", true)) {
                    SystemProperties.set(LPP_PROFILE, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                } else {
                    loadPropertiesFromResource(context, this.mProperties);
                    String lpp_profile = this.mProperties.getProperty("LPP_PROFILE");
                    if (lpp_profile != null) {
                        SystemProperties.set(LPP_PROFILE, lpp_profile);
                    }
                }
                hwLoadPropertiesFromResource(context, this.mProperties);
                this.mNIHandler.setSuplEsEnabled(this.mSuplEsEnabled);
            }
        } else if (DEBUG) {
            Log.d("GnssLocationProvider", "SIM MCC/MNC is still not available");
        }
    }

    /* access modifiers changed from: private */
    public void updateLowPowerMode() {
        boolean disableGps = this.mPowerManager.isDeviceIdleMode();
        boolean z = true;
        PowerSaveState result = this.mPowerManager.getPowerSaveState(1);
        if (result.gpsMode == 1) {
            Log.i("GnssLocationProvider", "updateLowPowerMode,batterySaverEnabled:" + result.batterySaverEnabled + "isInteractive:" + this.mPowerManager.isInteractive());
            if (!result.batterySaverEnabled || this.mPowerManager.isInteractive()) {
                z = false;
            }
            disableGps |= z;
        }
        Log.i("GnssLocationProvider", "disableGps:" + disableGps + "  isEnabled()=" + isEnabled());
        if (disableGps != this.mDisableGps) {
            this.mDisableGps = disableGps;
            updateRequirements();
        }
    }

    public static boolean isSupported() {
        return native_is_supported();
    }

    /* access modifiers changed from: private */
    public void reloadGpsProperties(Context context, Properties properties) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "Reset GPS properties, previous size = " + properties.size());
        }
        loadPropertiesFromResource(context, properties);
        String lpp_prof = SystemProperties.get(LPP_PROFILE);
        if (!TextUtils.isEmpty(lpp_prof)) {
            properties.setProperty("LPP_PROFILE", lpp_prof);
        }
        loadPropertiesFromFile(DEBUG_PROPERTIES_FILE, properties);
        setSuplHostPort(properties.getProperty("SUPL_HOST"), properties.getProperty("SUPL_PORT"));
        this.mC2KServerHost = properties.getProperty("C2K_HOST");
        String portString = properties.getProperty("C2K_PORT");
        if (!(this.mC2KServerHost == null || portString == null)) {
            try {
                this.mC2KServerPort = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                Log.e("GnssLocationProvider", "unable to parse C2K_PORT: " + portString);
            }
        }
        if (native_is_gnss_configuration_supported()) {
            for (Map.Entry<String, SetCarrierProperty> entry : new HashMap<String, SetCarrierProperty>() {
                {
                    put("SUPL_VER", $$Lambda$GnssLocationProvider$6$d34_RfOwt4eW2WTSkMsS8UoXSqY.INSTANCE);
                    put("SUPL_MODE", $$Lambda$GnssLocationProvider$6$7ITcPSS3RLwdJLvqPT1qDZbuYgU.INSTANCE);
                    put("SUPL_ES", $$Lambda$GnssLocationProvider$6$pJxRP_yDkUU0ylFw431I8fN70.INSTANCE);
                    put("LPP_PROFILE", $$Lambda$GnssLocationProvider$6$vt8zMIL_RIFwKcgd1rz4Y33NVyk.INSTANCE);
                    put("A_GLONASS_POS_PROTOCOL_SELECT", $$Lambda$GnssLocationProvider$6$fIEuYdSEFZVtEQQ5H4ObTmjLE.INSTANCE);
                    put("USE_EMERGENCY_PDN_FOR_EMERGENCY_SUPL", $$Lambda$GnssLocationProvider$6$M4Zfb6dp_EFsOdGGju4tOPslc4.INSTANCE);
                    put("GPS_LOCK", $$Lambda$GnssLocationProvider$6$0TBIDASC8cGFJxhCk2blveu19LI.INSTANCE);
                }
            }.entrySet()) {
                String propertyName = entry.getKey();
                String propertyValueString = properties.getProperty(propertyName);
                if (propertyValueString != null) {
                    try {
                        if (!entry.getValue().set(Integer.decode(propertyValueString).intValue())) {
                            Log.e("GnssLocationProvider", "Unable to set " + propertyName);
                        }
                    } catch (NumberFormatException e2) {
                        Log.e("GnssLocationProvider", "unable to parse propertyName: " + propertyValueString);
                    }
                }
            }
        } else {
            Log.i("GnssLocationProvider", "Skipped configuration update because GNSS configuration in GPS HAL is not supported");
        }
        String suplESProperty = this.mProperties.getProperty("SUPL_ES");
        if (suplESProperty != null) {
            try {
                boolean z = true;
                if (Integer.parseInt(suplESProperty) != 1) {
                    z = false;
                }
                this.mSuplEsEnabled = z;
            } catch (NumberFormatException e3) {
                Log.e("GnssLocationProvider", "unable to parse SUPL_ES: " + suplESProperty);
            }
        }
        String emergencyExtensionSecondsString = properties.getProperty("ES_EXTENSION_SEC", "0");
        try {
            this.mNIHandler.setEmergencyExtensionSeconds(Integer.parseInt(emergencyExtensionSecondsString));
        } catch (NumberFormatException e4) {
            Log.e("GnssLocationProvider", "unable to parse ES_EXTENSION_SEC: " + emergencyExtensionSecondsString);
        }
    }

    private void loadPropertiesFromResource(Context context, Properties properties) {
        for (String item : context.getResources().getStringArray(17236011)) {
            if (DEBUG) {
                Log.d("GnssLocationProvider", "GpsParamsResource: " + item);
            }
            int index = item.indexOf("=");
            if (index <= 0 || index + 1 >= item.length()) {
                Log.w("GnssLocationProvider", "malformed contents: " + item);
            } else {
                properties.setProperty(item.substring(0, index).trim().toUpperCase(), item.substring(index + 1));
            }
        }
    }

    private boolean loadPropertiesFromFile(String filename, Properties properties) {
        FileInputStream stream;
        try {
            stream = null;
            stream = new FileInputStream(new File(filename));
            properties.load(stream);
            IoUtils.closeQuietly(stream);
            return true;
        } catch (IOException e) {
            if (DEBUG) {
                Log.d("GnssLocationProvider", "Could not open GPS configuration file " + filename);
            }
            return false;
        } catch (Throwable th) {
            IoUtils.closeQuietly(stream);
            throw th;
        }
    }

    public GnssLocationProvider(Context context, ILocationManager ilocationManager, Looper looper) {
        this.mContext = context;
        this.mILocationManager = ilocationManager;
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWakeLock = this.mPowerManager.newWakeLock(1, "GnssLocationProvider");
        this.mWakeLock.setReferenceCounted(true);
        this.mDownloadXtraWakeLock = this.mPowerManager.newWakeLock(1, DOWNLOAD_EXTRA_WAKELOCK_KEY);
        this.mDownloadXtraWakeLock.setReferenceCounted(true);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mWakeupIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ALARM_WAKEUP), 0);
        this.mTimeoutIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ALARM_TIMEOUT), 0);
        this.mClearupIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ALARM_SUPLNI_TIMEOUT), 0);
        this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mHandler = new ProviderHandler(looper);
        this.mProperties = new Properties();
        this.mNIHandler = new GpsNetInitiatedHandler(context, this.mNetInitiatedListener, this.mSuplEsEnabled);
        sendMessage(13, 0, null);
        this.mListenerHelper = new GnssStatusListenerHelper(this.mHandler) {
            /* access modifiers changed from: protected */
            public boolean isAvailableInPlatform() {
                return GnssLocationProvider.isSupported();
            }

            /* access modifiers changed from: protected */
            public boolean isGpsEnabled() {
                return GnssLocationProvider.this.isEnabled();
            }
        };
        this.mGnssMeasurementsProvider = new GnssMeasurementsProvider(this.mContext, this.mHandler) {
            /* access modifiers changed from: protected */
            public boolean isGpsEnabled() {
                return GnssLocationProvider.this.isEnabled();
            }
        };
        this.mGnssNavigationMessageProvider = new GnssNavigationMessageProvider(this.mHandler) {
            /* access modifiers changed from: protected */
            public boolean isGpsEnabled() {
                return GnssLocationProvider.this.isEnabled();
            }
        };
        this.mGnssMetrics = new GnssMetrics(this.mBatteryStats);
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
        this.mProperties.setProperty(LAST_XTRA_DOWNLOAD_TIME, "0");
        this.mProperties.setProperty(LAST_SUCCESS_XTRA_DATA_SIZE, "0");
        this.mProperties.setProperty(HW_XTRA_DOWNLOAD_INTERVAL, "0");
        if (SUPL_UDP_SWITCH) {
            sendMessage(56, 0, null);
        }
        this.mNtpTimeHelper = new NtpTimeHelper(this.mContext, looper, this);
        this.mGnssSatelliteBlacklistHelper = new GnssSatelliteBlacklistHelper(this.mContext, looper, this);
        Handler handler = this.mHandler;
        GnssSatelliteBlacklistHelper gnssSatelliteBlacklistHelper = this.mGnssSatelliteBlacklistHelper;
        Objects.requireNonNull(gnssSatelliteBlacklistHelper);
        handler.post(new Runnable() {
            public final void run() {
                GnssSatelliteBlacklistHelper.this.updateSatelliteBlacklist();
            }
        });
        this.mGnssBatchingProvider = new GnssBatchingProvider();
        this.mGnssGeofenceProvider = new GnssGeofenceProvider(looper);
        this.ntpSwitch = Settings.Secure.getInt(this.mContext.getContentResolver(), NTP_TIME_INJECT_FLAG, 0);
    }

    public String getName() {
        return PROPERTIES_FILE_PREFIX;
    }

    public ProviderProperties getProperties() {
        return PROPERTIES;
    }

    public void injectTime(long time, long timeReference, int uncertainty) {
        native_inject_time(time, timeReference, uncertainty);
    }

    public void gpsXtra(long time, long timeReference) {
        if (this.mGpsXtraReceiver != null) {
            this.mGpsXtraReceiver.setNtpTime(time, timeReference);
        }
    }

    public void handleUpdateNetworkStateHook(NetworkInfo info) {
    }

    /* access modifiers changed from: private */
    public void handleUpdateNetworkState(Network network) {
        int i;
        Network network2 = network;
        NetworkInfo info = this.mConnMgr.getNetworkInfo(network2);
        boolean networkAvailable = false;
        boolean isConnected = false;
        int type = -1;
        boolean isRoaming = false;
        String apnName = null;
        if (info != null) {
            networkAvailable = info.isAvailable() && TelephonyManager.getDefault().getDataEnabled();
            isConnected = info.isConnected();
            type = info.getType();
            isRoaming = info.isRoaming();
            apnName = info.getExtraInfo();
        }
        boolean networkAvailable2 = networkAvailable;
        boolean isConnected2 = isConnected;
        int type2 = type;
        boolean isRoaming2 = isRoaming;
        String apnName2 = apnName;
        Log.d("GnssLocationProvider", String.format("UpdateNetworkState, state=%s, connected=%s, info=%s, capabilities=%S", new Object[]{agpsDataConnStateAsString(), Boolean.valueOf(isConnected2), info, this.mConnMgr.getNetworkCapabilities(network2)}));
        handleUpdateNetworkStateHook(info);
        if (native_is_agps_ril_supported()) {
            String defaultApn = getSelectedApn();
            if (defaultApn == null) {
                defaultApn = "dummy-apn";
            }
            this.mHwCmccGpsFeature.setRoaming(isRoaming2);
            i = 2;
            native_update_network_state(isConnected2, type2, isRoaming2, networkAvailable2, apnName2, defaultApn);
        } else {
            i = 2;
            Log.d("GnssLocationProvider", "Skipped network state update because GPS HAL AGPS-RIL is not  supported");
        }
        if (this.mAGpsDataConnectionState != 1) {
            return;
        }
        if (isConnected2) {
            if (apnName2 == null) {
                apnName2 = "dummy-apn";
            }
            String apnName3 = apnName2;
            int apnIpType = getApnIpType(apnName3);
            setRouting();
            Object[] objArr = new Object[i];
            objArr[0] = apnName3;
            objArr[1] = Integer.valueOf(apnIpType);
            Log.d("GnssLocationProvider", String.format("native_agps_data_conn_open: mAgpsApn=%s, mApnIpType=%s", objArr));
            native_agps_data_conn_open(apnName3, apnIpType);
            this.mAGpsDataConnectionState = i;
            String str = apnName3;
            return;
        }
        handleReleaseSuplConnection(5);
    }

    /* access modifiers changed from: private */
    public void handleRequestSuplConnection(InetAddress address) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", String.format("requestSuplConnection, state=%s, address=%s, mSuplEsConnected = %s", new Object[]{agpsDataConnStateAsString(), address, Boolean.valueOf(this.mSuplEsConnected)}));
        }
        if (this.mAGpsDataConnectionState != 0) {
            if (SUPL_ES_PDN_SWITCH && address != null && address.equals(this.mEsuplIpAddress)) {
                Log.d("GnssLocationProvider", "handle for Emergency SUPL");
            } else {
                return;
            }
        }
        this.mAGpsDataConnectionState = 1;
        if (!SUPL_ES_PDN_SWITCH || !this.mSuplEsConnected) {
            this.mAGpsDataConnectionIpAddr = address;
            NetworkRequest.Builder requestBuilder = new NetworkRequest.Builder();
            requestBuilder.addTransportType(0);
            requestBuilder.addCapability(1);
            this.mConnMgr.requestNetwork(requestBuilder.build(), this.mSuplConnectivityCallback);
            return;
        }
        Log.d("GnssLocationProvider", "During SUPL ES Session, only return network:" + this.mNetworkEs);
        sendMessage(4, 0, this.mNetworkEs);
    }

    /* access modifiers changed from: private */
    public void handleRequestSuplConnectionES() {
        NetworkRequest.Builder requestBuilder = new NetworkRequest.Builder();
        requestBuilder.addTransportType(0);
        if (this.mNeedEmergencyApn) {
            requestBuilder.addCapability(10);
            Log.e("GnssLocationProvider", "handleRequestSuplConnectionES-EIMS");
        } else {
            requestBuilder.addCapability(1);
            Log.e("GnssLocationProvider", "handleRequestSuplConnectionES-SUPL");
        }
        NetworkRequest request = requestBuilder.build();
        try {
            Log.v("GnssLocationProvider", "handleRequestSuplConnectionES requestNetwork");
            this.mConnMgr.requestNetwork(request, this.mSuplConnectivityCallbackEs, 8000);
        } catch (ConnectivityManager.TooManyRequestsException e) {
            Log.e("GnssLocationProvider", "request network more than 100 times");
            try {
                this.mConnMgr.unregisterNetworkCallback(this.mSuplConnectivityCallbackEs);
            } catch (IllegalArgumentException e2) {
                Log.e("GnssLocationProvider", "handleRequestSuplConnectionES,duplicate unregisterNetworkCallbackES");
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleReleaseSuplConnection(int agpsDataConnStatus) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", String.format("releaseSuplConnection, state=%s, status=%s", new Object[]{agpsDataConnStateAsString(), agpsDataConnStatusAsString(agpsDataConnStatus)}));
        }
        if (SUPL_UDP_SWITCH) {
            this.mUdpIpAddress = null;
        }
        if (this.mAGpsDataConnectionState != 0) {
            this.mAGpsDataConnectionState = 0;
            if (!SUPL_ES_PDN_SWITCH || !this.mSuplEsConnected) {
                try {
                    this.mConnMgr.unregisterNetworkCallback(this.mSuplConnectivityCallback);
                } catch (IllegalArgumentException e) {
                    Log.d("GnssLocationProvider", "handleReleaseSuplConnection,duplicate unregisterNetworkCallback");
                }
            } else if (this.mNetworkEs != null) {
                try {
                    this.mConnMgr.unregisterNetworkCallback(this.mSuplConnectivityCallbackEs);
                } catch (IllegalArgumentException e2) {
                    Log.e("GnssLocationProvider", "handleReleaseSuplConnection,duplicate unregisterNetworkCallbackES");
                }
                this.mSuplEsConnected = false;
                this.mNetworkEs = null;
            }
            if (agpsDataConnStatus == 2) {
                native_agps_data_conn_closed();
            } else if (agpsDataConnStatus != 5) {
                Log.e("GnssLocationProvider", "Invalid status to release SUPL connection: " + agpsDataConnStatus);
            } else {
                native_agps_data_conn_failed();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isHttpReachable() {
        return true;
    }

    /* access modifiers changed from: private */
    public void handleRequestLocation(boolean independentFromGnss) {
        LocationChangeListener locationListener;
        String provider;
        if (isRequestLocationRateLimited()) {
            if (DEBUG) {
                Log.d("GnssLocationProvider", "RequestLocation is denied due to too frequent requests.");
            }
            return;
        }
        long durationMillis = Settings.Global.getLong(this.mContext.getContentResolver(), "gnss_hal_location_request_duration_millis", 0);
        if (durationMillis == 0) {
            Log.i("GnssLocationProvider", "GNSS HAL location request is disabled by Settings.");
            return;
        }
        LocationManager locationManager = (LocationManager) this.mContext.getSystemService("location");
        if (independentFromGnss) {
            provider = "network";
            locationListener = this.mNetworkLocationListener;
        } else {
            provider = "fused";
            locationListener = this.mFusedLocationListener;
        }
        Log.i("GnssLocationProvider", String.format("GNSS HAL Requesting location updates from %s provider for %d millis.", new Object[]{provider, Long.valueOf(durationMillis)}));
        try {
            locationManager.requestLocationUpdates(provider, 1000, MIX_SPEED, locationListener, this.mHandler.getLooper());
            locationListener.numLocationUpdateRequest++;
            this.mHandler.postDelayed(new Runnable(provider, locationManager) {
                private final /* synthetic */ String f$1;
                private final /* synthetic */ LocationManager f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    GnssLocationProvider.lambda$handleRequestLocation$1(GnssLocationProvider.LocationChangeListener.this, this.f$1, this.f$2);
                }
            }, durationMillis);
        } catch (IllegalArgumentException e) {
            Log.w("GnssLocationProvider", "Unable to request location.", e);
        }
    }

    static /* synthetic */ void lambda$handleRequestLocation$1(LocationChangeListener locationListener, String provider, LocationManager locationManager) {
        int i = locationListener.numLocationUpdateRequest - 1;
        locationListener.numLocationUpdateRequest = i;
        if (i == 0) {
            Log.i("GnssLocationProvider", String.format("Removing location updates from %s provider.", new Object[]{provider}));
            locationManager.removeUpdates(locationListener);
        }
    }

    /* access modifiers changed from: private */
    public void injectBestLocation(Location location) {
        int i = 0;
        int i2 = (location.hasAltitude() ? 2 : 0) | 1 | (location.hasSpeed() ? 4 : 0) | (location.hasBearing() ? 8 : 0) | (location.hasAccuracy() ? 16 : 0) | (location.hasVerticalAccuracy() ? 32 : 0) | (location.hasSpeedAccuracy() ? 64 : 0);
        if (location.hasBearingAccuracy()) {
            i = 128;
        }
        native_inject_best_location(i2 | i, location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getSpeed(), location.getBearing(), location.getAccuracy(), location.getVerticalAccuracyMeters(), location.getSpeedAccuracyMetersPerSecond(), location.getBearingAccuracyDegrees(), location.getTime());
    }

    private boolean isRequestLocationRateLimited() {
        return false;
    }

    /* access modifiers changed from: private */
    public void handleDownloadXtraData() {
        if (!this.mSupportsXtra) {
            Log.d("GnssLocationProvider", "handleDownloadXtraData() called when Xtra not supported");
        } else if (this.mDownloadXtraDataPending != 1) {
            if (!isDataNetworkConnected()) {
                this.mDownloadXtraDataPending = 0;
                return;
            }
            this.mDownloadXtraDataPending = 1;
            this.mDownloadXtraWakeLock.acquire(60000);
            Log.i("GnssLocationProvider", "WakeLock acquired by handleDownloadXtraData()");
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                public void run() {
                    NetworkInfo activeNetworkInfo = GnssLocationProvider.this.mConnMgr.getActiveNetworkInfo();
                    if (activeNetworkInfo == null || (1 == activeNetworkInfo.getType() && !GnssLocationProvider.this.isHttpReachable())) {
                        int unused = GnssLocationProvider.this.mDownloadXtraDataPending = 0;
                        synchronized (GnssLocationProvider.this.mLock) {
                            if (GnssLocationProvider.this.mDownloadXtraWakeLock.isHeld()) {
                                GnssLocationProvider.this.mDownloadXtraWakeLock.release();
                                Log.i("GnssLocationProvider", "WakeLock released by handleDownloadXtraData() for http unreachable");
                            } else {
                                Log.e("GnssLocationProvider", "WakeLock expired before release in handleDownloadXtraData() for http unreachable");
                            }
                        }
                        return;
                    }
                    Log.i("GnssLocationProvider", "Execute handleDownloadXtraData()");
                    byte[] data = new GpsXtraDownloader(GnssLocationProvider.this.mProperties).downloadXtraData();
                    if (data != null) {
                        Log.i("GnssLocationProvider", "calling native_inject_xtra_data");
                        GnssLocationProvider.this.native_inject_xtra_data(data, data.length);
                        GnssLocationProvider.this.mXtraBackOff.reset();
                        if (GnssLocationProvider.this.mGpsXtraReceiver != null) {
                            GnssLocationProvider.this.mGpsXtraReceiver.sendXtraDownloadComplete();
                        }
                    }
                    GnssLocationProvider.this.sendMessage(11, 0, null);
                    if (data == null && !GnssLocationProvider.this.mHandler.hasMessages(6)) {
                        GnssLocationProvider.this.mHandler.sendEmptyMessageDelayed(6, GnssLocationProvider.this.mXtraBackOff.nextBackoffMillis());
                    }
                    synchronized (GnssLocationProvider.this.mLock) {
                        if (GnssLocationProvider.this.mDownloadXtraWakeLock.isHeld()) {
                            try {
                                GnssLocationProvider.this.mDownloadXtraWakeLock.release();
                                if (GnssLocationProvider.DEBUG) {
                                    Log.d("GnssLocationProvider", "WakeLock released by handleDownloadXtraData()");
                                }
                            } catch (Exception e) {
                                Log.i("GnssLocationProvider", "Wakelock timeout & release race exception in handleDownloadXtraData()", e);
                            }
                        } else {
                            Log.e("GnssLocationProvider", "WakeLock expired before release in handleDownloadXtraData()");
                        }
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void handleUpdateLocation(Location location) {
        if (location.hasAccuracy()) {
            native_inject_location(location.getLatitude(), location.getLongitude(), location.getAccuracy());
            this.mHwGpsLogServices.injectExtraParam("ref_location");
        }
    }

    public void enable() {
        synchronized (this.mLock) {
            if (!this.mEnabled) {
                this.mEnabled = true;
                sendMessage(2, 1, null);
            }
        }
    }

    private void setSuplHostPort(String hostString, String portString) {
        if (hostString != null && !BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS.equals(hostString)) {
            this.mSuplServerHost = hostString;
        }
        if (portString != null && !BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS.equals(portString)) {
            try {
                this.mSuplServerPort = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                Log.e("GnssLocationProvider", "unable to parse SUPL_PORT: " + portString);
            }
        }
        if (this.mSuplServerHost != null && this.mSuplServerPort > 0 && this.mSuplServerPort <= 65535) {
            native_set_agps_server(1, this.mSuplServerHost, this.mSuplServerPort);
        }
    }

    private int getSuplMode(Properties properties, boolean agpsEnabled, boolean singleShot) {
        if (agpsEnabled) {
            String modeString = properties.getProperty("SUPL_MODE");
            int suplMode = 0;
            if (!TextUtils.isEmpty(modeString)) {
                try {
                    suplMode = Integer.parseInt(modeString);
                } catch (NumberFormatException e) {
                    Log.e("GnssLocationProvider", "unable to parse SUPL_MODE: " + modeString);
                    return 0;
                }
            }
            if (singleShot && hasCapability(4) && (suplMode & 2) != 0 && !FORBINDDEN_MSA_SWITCH) {
                return 2;
            }
            if (hasCapability(2) && (suplMode & 1) != 0) {
                return 1;
            }
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public void handleEnable() {
        boolean enabled;
        Log.i("GnssLocationProvider", "handleEnable");
        if (!isEnabledBackGround() || !SUPL_ES_PDN_SWITCH) {
            enabled = native_init();
        } else {
            enabled = true;
        }
        this.mHwGpsLogServices.initGps(enabled, (byte) this.mEngineCapabilities);
        if (enabled) {
            this.mSupportsXtra = native_supports_xtra();
            if (this.mSuplServerHost != null) {
                native_set_agps_server(1, this.mSuplServerHost, this.mSuplServerPort);
            }
            if (this.mC2KServerHost != null) {
                native_set_agps_server(2, this.mC2KServerHost, this.mC2KServerPort);
            }
            this.mGnssMeasurementsProvider.onGpsEnabledChanged();
            this.mGnssNavigationMessageProvider.onGpsEnabledChanged();
            this.mGnssBatchingProvider.enable();
            return;
        }
        synchronized (this.mLock) {
            this.mEnabled = false;
        }
        Log.w("GnssLocationProvider", "Failed to enable location provider");
    }

    public void disable() {
        synchronized (this.mLock) {
            if (this.mEnabled) {
                this.mEnabled = false;
                sendMessage(2, 0, null);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleDisable() {
        Log.i("GnssLocationProvider", "handleDisable");
        this.mProviderRequest = null;
        this.mWorkSource = null;
        updateClientUids(new WorkSource());
        stopNavigating();
        this.mAlarmManager.cancel(this.mWakeupIntent);
        this.mAlarmManager.cancel(this.mTimeoutIntent);
        this.mGnssBatchingProvider.disable();
        if (!isEnabledBackGround() || !SUPL_ES_PDN_SWITCH) {
            native_cleanup();
        } else {
            Log.e("GnssLocationProvider", "isEnabledBackGround:TRUE, not call native_cleanup");
        }
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

    /* access modifiers changed from: private */
    public boolean isEnabledBackGround() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mEnabledBackGround;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void SetEnabledBackGround() {
        if (isEnabledBackGround()) {
            this.mAlarmManager.cancel(this.mClearupIntent);
        }
        if (!isEnabled()) {
            boolean enable = native_init();
            synchronized (this.mLock) {
                if (enable) {
                    try {
                        this.mEnabledBackGround = true;
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    this.mEnabledBackGround = false;
                    Log.w("GnssLocationProvider", "Failed to enable location provider for NI failed");
                }
            }
        } else {
            synchronized (this.mLock) {
                this.mEnabledBackGround = true;
            }
        }
        Log.d("GnssLocationProvider", "GnssLocationProvider checkWapSuplInit SET_CLEARUP_ALARM120000");
        this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + 120000, this.mClearupIntent);
    }

    public int getStatus(Bundle extras) {
        this.mLocationExtras.setBundle(extras);
        return this.mStatus;
    }

    private void updateStatus(int status) {
        if (status != this.mStatus) {
            this.mStatus = status;
            this.mStatusUpdateTime = SystemClock.elapsedRealtime();
        }
    }

    public long getStatusUpdateTime() {
        return this.mStatusUpdateTime;
    }

    public void setRequest(ProviderRequest request, WorkSource source) {
        sendMessage(3, 0, new GpsRequest(request, source));
    }

    /* access modifiers changed from: private */
    public void handleSetRequest(ProviderRequest request, WorkSource source) {
        this.mProviderRequest = request;
        this.mWorkSource = source;
        updateRequirements();
    }

    private void updateRequirements() {
        if (this.mProviderRequest != null && this.mWorkSource != null) {
            boolean singleShot = false;
            if (this.mProviderRequest.locationRequests != null && this.mProviderRequest.locationRequests.size() > 0) {
                singleShot = true;
                for (LocationRequest lr : this.mProviderRequest.locationRequests) {
                    if (lr.getNumUpdates() != 1) {
                        singleShot = false;
                    }
                }
            }
            synchronized (this) {
                this.mSingleShot = singleShot;
            }
            if (this.ntpSwitch > 0 && this.mRequesetUtcTime && !this.mProviderRequest.reportLocation) {
                this.mRequesetUtcTime = false;
            }
            Log.i("GnssLocationProvider", "setRequest " + this.mProviderRequest + ", mDisableGps:" + this.mDisableGps + ", mEnabled:" + isEnabled());
            if (!this.mProviderRequest.reportLocation || this.mDisableGps || !isEnabled()) {
                updateClientUids(new WorkSource());
                stopNavigating();
                synchronized (this.mAlarmLock) {
                    this.mRealStoped = true;
                    this.mAlarmManager.cancel(this.mWakeupIntent);
                    this.mAlarmManager.cancel(this.mTimeoutIntent);
                }
            } else {
                synchronized (this.mAlarmLock) {
                    this.mRealStoped = false;
                }
                updateClientUids(this.mWorkSource);
                handleGnssRequirementsChange(1);
                this.mFixInterval = (int) this.mProviderRequest.interval;
                this.mLowPowerMode = this.mProviderRequest.lowPowerMode;
                if (((long) this.mFixInterval) != this.mProviderRequest.interval) {
                    Log.w("GnssLocationProvider", "interval overflow: " + this.mProviderRequest.interval);
                    this.mFixInterval = HwBootFail.STAGE_BOOT_SUCCESS;
                }
                if (this.mStarted) {
                    if (shouldReStartNavi()) {
                        stopNavigating();
                        startNavigating(singleShot);
                    }
                    if (hasCapability(1)) {
                        if (!native_set_position_mode(getPositionMode(), 0, this.mFixInterval, getPreferred_accuracy(), 0, this.mLowPowerMode)) {
                            Log.e("GnssLocationProvider", "set_position_mode failed in setMinTime()");
                            this.mHwGpsLogServices.updateSetPosMode(false, this.mFixInterval / 1000);
                        } else {
                            this.mHwGpsLogServices.updateSetPosMode(true, this.mFixInterval / 1000);
                        }
                    } else {
                        this.mAlarmManager.cancel(this.mTimeoutIntent);
                        if (this.mFixInterval >= NO_FIX_TIMEOUT) {
                            this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + 60000, this.mTimeoutIntent);
                        }
                    }
                } else if (isEnabled()) {
                    startNavigating(singleShot);
                }
            }
        }
    }

    private void updateClientUids(WorkSource source) {
        if (!source.equals(this.mClientSource)) {
            try {
                this.mBatteryStats.noteGpsChanged(this.mClientSource, source);
            } catch (RemoteException e) {
                Log.w("GnssLocationProvider", "RemoteException", e);
            }
            List<WorkSource.WorkChain>[] diffs = WorkSource.diffChains(this.mClientSource, source);
            if (diffs != null) {
                List<WorkSource.WorkChain> newChains = diffs[0];
                List<WorkSource.WorkChain> goneChains = diffs[1];
                if (newChains != null) {
                    for (int i = 0; i < newChains.size(); i++) {
                        WorkSource.WorkChain newChain = newChains.get(i);
                        this.mAppOps.startOpNoThrow(2, newChain.getAttributionUid(), newChain.getAttributionTag());
                    }
                }
                if (goneChains != null) {
                    for (int i2 = 0; i2 < goneChains.size(); i2++) {
                        WorkSource.WorkChain goneChain = goneChains.get(i2);
                        this.mAppOps.finishOp(2, goneChain.getAttributionUid(), goneChain.getAttributionTag());
                    }
                }
                this.mClientSource.transferWorkChains(source);
            }
            WorkSource[] changes = this.mClientSource.setReturningDiffs(source);
            if (changes != null) {
                WorkSource newWork = changes[0];
                WorkSource goneWork = changes[1];
                if (newWork != null) {
                    for (int i3 = 0; i3 < newWork.size(); i3++) {
                        this.mAppOps.startOpNoThrow(2, newWork.get(i3), newWork.getName(i3));
                        HwLog.dubaie("DUBAI_TAG_GNSS_START", "name=" + newWork.getName(i3));
                        LogPower.push(156, Integer.toString(newWork.get(i3)), newWork.getName(i3));
                    }
                }
                if (goneWork != null) {
                    for (int i4 = 0; i4 < goneWork.size(); i4++) {
                        this.mAppOps.finishOp(2, goneWork.get(i4), goneWork.getName(i4));
                        HwLog.dubaie("DUBAI_TAG_GNSS_STOP", "name=" + goneWork.getName(i4));
                        LogPower.push(157, Integer.toString(goneWork.get(i4)), goneWork.getName(i4));
                    }
                }
            }
        }
    }

    public boolean sendExtraCommandHook(String command, boolean result) {
        return result;
    }

    public boolean sendExtraCommand(String command, Bundle extras) {
        long identity = Binder.clearCallingIdentity();
        boolean result = false;
        try {
            if ("delete_aiding_data".equals(command)) {
                result = deleteAidingData(extras);
            } else if ("force_time_injection".equals(command)) {
                requestUtcTime();
                result = true;
            } else if ("force_xtra_injection".equals(command)) {
                if (this.mSupportsXtra) {
                    long currenttime = SystemClock.elapsedRealtime();
                    long lastdownloadtime = Long.parseLong(this.mProperties.getProperty(LAST_XTRA_DOWNLOAD_TIME));
                    Log.i("GnssLocationProvider", "force_xtra_injection, last xtra download time:" + lastdownloadtime + " current time:" + currenttime);
                    if (currenttime - lastdownloadtime > AppStandbyController.SettingsObserver.DEFAULT_NOTIFICATION_TIMEOUT || lastdownloadtime == 0) {
                        xtraDownloadRequest();
                    }
                    result = true;
                }
            } else if ("request_quick_ttff".equals(command)) {
                synchronized (this.mQuickLock) {
                    Log.w("GnssLocationProvider", "native_start_quick_ttff ");
                    this.isRealQuick = true;
                    sendMessage(50, 0, null);
                }
            } else if ("stop_quick_ttff".equals(command)) {
                synchronized (this.mQuickLock) {
                    Log.w("GnssLocationProvider", "native_stop_quick_ttff ");
                    this.isRealQuick = false;
                    sendMessage(51, 0, null);
                }
            } else {
                Log.w("GnssLocationProvider", "sendExtraCommand: unknown command " + command);
            }
            boolean result2 = sendExtraCommandHook(command, result);
            Binder.restoreCallingIdentity(identity);
            return result2;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    private boolean deleteAidingData(Bundle extras) {
        int flags;
        if (extras == null) {
            flags = NetworkConstants.ARP_HWTYPE_RESERVED_HI;
        } else {
            flags = 0;
            if (extras.getBoolean("ephemeris")) {
                flags = 0 | 1;
            }
            if (extras.getBoolean("almanac")) {
                flags |= 2;
            }
            if (extras.getBoolean("position")) {
                flags |= 4;
            }
            if (extras.getBoolean("time")) {
                flags |= 8;
            }
            if (extras.getBoolean("iono")) {
                flags |= 16;
            }
            if (extras.getBoolean("utc")) {
                flags |= 32;
            }
            if (extras.getBoolean("health")) {
                flags |= 64;
            }
            if (extras.getBoolean("svdir")) {
                flags |= 128;
            }
            if (extras.getBoolean("svsteer")) {
                flags |= 256;
            }
            if (extras.getBoolean("sadata")) {
                flags |= 512;
            }
            if (extras.getBoolean("rti")) {
                flags |= 1024;
            }
            if (extras.getBoolean("celldb-info")) {
                flags |= 32768;
            }
            if (extras.getBoolean("all")) {
                flags |= NetworkConstants.ARP_HWTYPE_RESERVED_HI;
            }
        }
        if (flags == 0) {
            return false;
        }
        native_delete_aiding_data(flags);
        return true;
    }

    public void startNavigatingPreparedHook() {
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0165, code lost:
        return;
     */
    public synchronized void startNavigating(boolean singleShot) {
        String mode;
        if (!this.mStarted) {
            Log.i("GnssLocationProvider", "startNavigating, singleShot is " + singleShot);
            this.mTimeToFirstFix = 0;
            this.mLastFixTime = 0;
            this.mStarted = true;
            this.mSingleShot = singleShot;
            this.mPositionMode = 0;
            if (this.mItarSpeedLimitExceeded) {
                Log.i("GnssLocationProvider", "startNavigating with ITAR limit in place. Output limited  until slow enough speed reported.");
            }
            if (this.ntpSwitch <= 0) {
                this.mRequesetUtcTime = false;
            }
            boolean agpsEnabled = Settings.Global.getInt(this.mContext.getContentResolver(), "assisted_gps_enabled", 1) != 0;
            this.mPositionMode = getSuplMode(this.mProperties, agpsEnabled, singleShot);
            this.mPositionMode = this.mHwGpsLocationCustFeature.setPostionMode(this.mContext, this.mPositionMode, agpsEnabled);
            this.mPositionMode = this.mHwCmccGpsFeature.setPostionModeAndAgpsServer(this.mPositionMode, agpsEnabled);
            startNavigatingPreparedHook();
            switch (this.mPositionMode) {
                case 0:
                    mode = "standalone";
                    break;
                case 1:
                    mode = "MS_BASED";
                    break;
                case 2:
                    mode = "MS_ASSISTED";
                    break;
                default:
                    mode = UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
                    break;
            }
            Log.i("GnssLocationProvider", "setting position_mode to " + mode);
            int interval = hasCapability(1) ? this.mFixInterval : 1000;
            this.mLowPowerMode = this.mProviderRequest.lowPowerMode;
            if (mWcdmaVpEnabled && (this.mPositionMode == 1 || this.mPositionMode == 2)) {
                Settings.Global.putInt(this.mContext.getContentResolver(), KEY_AGPS_APP_STARTED_NAVIGATION, 1);
            }
            if (!native_set_position_mode(this.mPositionMode, 0, interval, getPreferred_accuracy(), 0, this.mLowPowerMode)) {
                this.mStarted = false;
                Log.e("GnssLocationProvider", "set_position_mode failed in startNavigating()");
                this.mHwGpsLogServices.updateSetPosMode(false, interval / 1000);
                return;
            }
            this.mHwGpsLogServices.updateSetPosMode(true, interval / 1000);
            this.mHwCmccGpsFeature.setDelAidData();
            if (!native_start()) {
                this.mStarted = false;
                this.mHwGpsLogServices.startGps(this.mStarted, this.mPositionMode);
                Log.e("GnssLocationProvider", "native_start failed in startNavigating()");
                return;
            }
            this.nHwQuickTTFFMonitor = HwQuickTTFFMonitor.getMonitor();
            if (this.nHwQuickTTFFMonitor != null) {
                this.nHwQuickTTFFMonitor.sendStartCommand();
                this.nHwQuickTTFFMonitor.setNavigating(this.mStarted);
            }
            updateStatus(1);
            this.mLocationExtras.reset();
            this.mFixRequestTime = SystemClock.elapsedRealtime();
            this.mHwGpsLogServices.startGps(this.mStarted, this.mPositionMode);
            if (!hasCapability(1) && this.mFixInterval >= NO_FIX_TIMEOUT) {
                Log.d("GnssLocationProvider", "GnssLocationProvider startNavigating SET_WAKE_ALARM60000");
                this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + 60000, this.mTimeoutIntent);
            }
            handleGnssRequirementsChange(1);
            handleGnssNavigatingStateChange(true);
        }
    }

    private synchronized void stopNavigating() {
        Log.i("GnssLocationProvider", "stopNavigating, mStarted=" + this.mStarted);
        if (this.mStarted) {
            this.mStarted = false;
            this.mSingleShot = false;
            boolean enabled = native_stop();
            this.nHwQuickTTFFMonitor = HwQuickTTFFMonitor.getMonitor();
            if (this.nHwQuickTTFFMonitor != null) {
                if (this.nHwQuickTTFFMonitor.isRunning()) {
                    this.nHwQuickTTFFMonitor.sendStopCommand();
                }
                this.nHwQuickTTFFMonitor.setNavigating(this.mStarted);
            }
            this.mHwGpsLogServices.stopGps(enabled);
            this.mLastFixTime = 0;
            updateStatus(1);
            this.mLocationExtras.reset();
            if (mWcdmaVpEnabled) {
                Settings.Global.putInt(this.mContext.getContentResolver(), KEY_AGPS_APP_STARTED_NAVIGATION, 0);
            }
            handleGnssRequirementsChange(2);
            handleGnssNavigatingStateChange(false);
        }
    }

    /* access modifiers changed from: private */
    public void hibernate() {
        stopNavigating();
        this.mAlarmManager.cancel(this.mTimeoutIntent);
        this.mAlarmManager.cancel(this.mWakeupIntent);
        synchronized (this.mAlarmLock) {
            if (!this.mRealStoped) {
                long now = SystemClock.elapsedRealtime();
                Log.d("GnssLocationProvider", "GnssLocationProvider hibernate SET_WAKE_ALARM" + this.mFixInterval);
                this.mAlarmManager.set(2, ((long) this.mFixInterval) + now, this.mWakeupIntent);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean hasCapability(int capability) {
        return (this.mEngineCapabilities & capability) != 0;
    }

    private void reportLocation(boolean hasLatLong, Location location) {
        int mSourceType = 0;
        if (location.getProvider() != null && !location.getProvider().equals(PROPERTIES_FILE_PREFIX)) {
            try {
                mSourceType = Integer.parseInt(location.getProvider());
            } catch (NumberFormatException e) {
                Log.e("GnssLocationProvider", "unable to parse  mSourceType" + location.getProvider());
            }
            if ((mSourceType & 128) == 128) {
                reportMeasurementData(HwQuickTTFFMonitor.getGnssMeasurements());
                reportMeasurementData(HwQuickTTFFMonitor.getGnssMeasurements());
                HwQuickTTFFMonitor.pauseTask();
                location.setProvider("quickgps");
            } else {
                location.setProvider(PROPERTIES_FILE_PREFIX);
            }
        }
        this.mLocationExtras.setBundleExtra("SourceType", mSourceType);
        location.setExtras(this.mLocationExtras.getBundle());
        Log.v("GnssLocationProvider", "mSourceType in mLocationExtras " + location.getExtras().getInt("SourceType"));
        sendMessage(17, hasLatLong, location);
    }

    /* access modifiers changed from: private */
    public void handleReportLocation(boolean hasLatLong, Location location) {
        if (location.hasSpeed()) {
            this.mItarSpeedLimitExceeded = location.getSpeed() > ITAR_SPEED_LIMIT_METERS_PER_SECOND;
        }
        if (this.mItarSpeedLimitExceeded) {
            Log.i("GnssLocationProvider", "Hal reported a speed in excess of ITAR limit.  GPS/GNSS Navigation output blocked.");
            if (this.mStarted) {
                this.mGnssMetrics.logReceivedLocationStatus(false);
            }
            return;
        }
        Log.v("GnssLocationProvider", "reportLocation " + location.toString() + " isquickgps " + location.getProvider());
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        hwHandleReportLocation(hasLatLong, location);
        try {
            this.mILocationManager.reportLocation(location, false);
        } catch (RemoteException e) {
            Log.e("GnssLocationProvider", "RemoteException calling reportLocation");
        }
        if (this.mStarted) {
            this.mGnssMetrics.logReceivedLocationStatus(hasLatLong);
            if (hasLatLong) {
                if (location.hasAccuracy()) {
                    this.mGnssMetrics.logPositionAccuracyMeters(location.getAccuracy());
                }
                if (this.mTimeToFirstFix > 0) {
                    this.mGnssMetrics.logMissedReports(this.mFixInterval, (int) (SystemClock.elapsedRealtime() - this.mLastFixTime));
                }
            }
        }
        this.mLastFixTime = SystemClock.elapsedRealtime();
        if (this.mTimeToFirstFix == 0 && hasLatLong) {
            this.mTimeToFirstFix = (int) (this.mLastFixTime - this.mFixRequestTime);
            Log.d("GnssLocationProvider", "TTFF: " + this.mTimeToFirstFix);
            if (this.mStarted) {
                this.mGnssMetrics.logTimeToFirstFixMilliSecs(this.mTimeToFirstFix);
            }
            this.mListenerHelper.onFirstFix(this.mTimeToFirstFix);
        }
        synchronized (this) {
            if (this.mSingleShot) {
                stopNavigating();
            }
        }
        if (this.mStarted && this.mStatus != 2) {
            if (!hasCapability(1) && this.mFixInterval < NO_FIX_TIMEOUT) {
                this.mAlarmManager.cancel(this.mTimeoutIntent);
            }
            Intent intent = new Intent("android.location.GPS_FIX_CHANGE");
            intent.putExtra("enabled", true);
            intent.putExtra("isFrameworkBroadcast", "true");
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            updateStatus(2);
        }
        if (!location.getProvider().equals("quickgps") && !hasCapability(1) && this.mStarted && this.mFixInterval > 10000) {
            Log.i("GnssLocationProvider", "got fix, hibernating");
            hibernate();
        }
        if (!location.getProvider().equals("quickgps")) {
            this.mHwCmccGpsFeature.syncTime(location.getTime());
        }
    }

    private void reportStatus(int status) {
        Log.i("GnssLocationProvider", "reportStatus status: " + status);
        this.mHwGpsLogServices.updateGpsRunState(status);
        boolean wasNavigating = this.mNavigating;
        switch (status) {
            case 1:
                this.mNavigating = true;
                this.mEngineOn = true;
                break;
            case 2:
                this.mNavigating = false;
                break;
            case 3:
                this.mEngineOn = true;
                break;
            case 4:
                this.mEngineOn = false;
                this.mNavigating = false;
                break;
        }
        if (wasNavigating != this.mNavigating) {
            this.mListenerHelper.onStatusChanged(this.mNavigating);
            Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
            intent.putExtra("enabled", this.mNavigating);
            intent.putExtra("isFrameworkBroadcast", "true");
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private void reportSvStatus(int svCount, int[] svidWithFlags, float[] cn0s, float[] svElevations, float[] svAzimuths, float[] svCarrierFreqs) {
        SvStatusInfo svStatusInfo = new SvStatusInfo();
        svStatusInfo.mSvCount = svCount;
        svStatusInfo.mSvidWithFlags = svidWithFlags;
        svStatusInfo.mCn0s = cn0s;
        svStatusInfo.mSvElevations = svElevations;
        svStatusInfo.mSvAzimuths = svAzimuths;
        svStatusInfo.mSvCarrierFreqs = svCarrierFreqs;
        this.mHwGpsLogServices.updateSvStatus(svCount, svidWithFlags, cn0s, svElevations, svAzimuths);
        if (!GnssStatus.checkGnssData(svCount, svidWithFlags, cn0s, svElevations, svAzimuths)) {
            Log.e("GnssLocationProvider", "onSvStatusChanged GnssStatus has invalid data");
        }
        sendMessage(18, 0, svStatusInfo);
    }

    /* access modifiers changed from: private */
    public void handleReportSvStatus(SvStatusInfo info) {
        String str;
        this.mListenerHelper.onSvStatusChanged(info.mSvCount, info.mSvidWithFlags, info.mCn0s, info.mSvElevations, info.mSvAzimuths, info.mSvCarrierFreqs);
        this.mGnssMetrics.logCn0(info.mCn0s, info.mSvCount);
        Log.v("GnssLocationProvider", "SV count: " + info.mSvCount);
        if (!GnssStatus.checkGnssData(info.mSvCount, info.mSvidWithFlags, info.mCn0s, info.mSvElevations, info.mSvAzimuths)) {
            Log.e("GnssLocationProvider", "onSvStatusChanged GnssStatus has invalid data");
        }
        int meanCn0 = 0;
        int maxCn0 = 0;
        int usedInFixCount = 0;
        for (int i = 0; i < info.mSvCount; i++) {
            if ((info.mSvidWithFlags[i] & 4) != 0) {
                usedInFixCount++;
                if (info.mCn0s[i] > ((float) maxCn0)) {
                    maxCn0 = (int) info.mCn0s[i];
                }
                meanCn0 = (int) (((float) meanCn0) + info.mCn0s[i]);
            }
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append(getSvType(info.mSvidWithFlags[i]));
                sb.append(" svid: ");
                sb.append(info.mSvidWithFlags[i] >> 8);
                sb.append(" cn0: ");
                sb.append(info.mCn0s[i]);
                sb.append(" elev: ");
                sb.append(info.mSvElevations[i]);
                sb.append(" azimuth: ");
                sb.append(info.mSvAzimuths[i]);
                sb.append(" carrier frequency: ");
                sb.append(info.mSvCarrierFreqs[i]);
                sb.append((1 & info.mSvidWithFlags[i]) == 0 ? "  " : " E");
                sb.append((2 & info.mSvidWithFlags[i]) == 0 ? "  " : " A");
                sb.append((info.mSvidWithFlags[i] & 4) == 0 ? BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS : "U");
                if ((info.mSvidWithFlags[i] & 8) == 0) {
                    str = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                } else {
                    str = "F";
                }
                sb.append(str);
                Log.v("GnssLocationProvider", sb.toString());
            }
        }
        if (usedInFixCount > 0) {
            meanCn0 /= usedInFixCount;
        }
        this.mLocationExtras.set(usedInFixCount, meanCn0, maxCn0);
        if (this.mNavigating && this.mStatus == 2 && this.mLastFixTime > 0 && SystemClock.elapsedRealtime() - this.mLastFixTime > 10000) {
            Intent intent = new Intent("android.location.GPS_FIX_CHANGE");
            intent.putExtra("enabled", false);
            intent.putExtra("isFrameworkBroadcast", "true");
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            updateStatus(1);
        }
    }

    private void reportAGpsStatus(int type, int status, byte[] ipaddr) {
        this.mHwGpsLogServices.updateAgpsState(type, status);
        switch (status) {
            case 1:
                Log.i("GnssLocationProvider", "GPS_REQUEST_AGPS_DATA_CONN");
                Log.v("GnssLocationProvider", "Received SUPL IP addr[]: " + Arrays.toString(ipaddr));
                InetAddress connectionIpAddress = null;
                if (ipaddr != null) {
                    try {
                        connectionIpAddress = InetAddress.getByAddress(ipaddr);
                        if (DEBUG) {
                            Log.d("GnssLocationProvider", "IP address converted to: " + connectionIpAddress);
                        }
                    } catch (UnknownHostException e) {
                        Log.e("GnssLocationProvider", "Bad IP Address: " + ipaddr, e);
                    }
                }
                sendMessage(14, 0, connectionIpAddress);
                return;
            case 2:
                Log.i("GnssLocationProvider", "GPS_RELEASE_AGPS_DATA_CONN");
                releaseSuplConnection(2);
                return;
            case 3:
                Log.i("GnssLocationProvider", "GPS_AGPS_DATA_CONNECTED");
                return;
            case 4:
                Log.i("GnssLocationProvider", "GPS_AGPS_DATA_CONN_DONE");
                return;
            case 5:
                Log.i("GnssLocationProvider", "GPS_AGPS_DATA_CONN_FAILED");
                return;
            default:
                Log.i("GnssLocationProvider", "Received Unknown AGPS status: " + status);
                return;
        }
    }

    /* access modifiers changed from: private */
    public void releaseSuplConnection(int connStatus) {
        sendMessage(15, connStatus, null);
    }

    private void reportNmea(long timestamp) {
        if (!this.mItarSpeedLimitExceeded) {
            this.mListenerHelper.onNmeaReceived(timestamp, new String(this.mNmeaBuffer, 0, native_read_nmea(this.mNmeaBuffer, this.mNmeaBuffer.length)));
        }
    }

    private void reportMeasurementData(final GnssMeasurementsEvent event) {
        if (!this.mItarSpeedLimitExceeded) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    GnssLocationProvider.this.mGnssMeasurementsProvider.onMeasurementsAvailable(event);
                }
            });
        }
    }

    private void reportNavigationMessage(final GnssNavigationMessage event) {
        if (!this.mItarSpeedLimitExceeded) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    GnssLocationProvider.this.mGnssNavigationMessageProvider.onNavigationMessageAvailable(event);
                }
            });
        }
    }

    private void setEngineCapabilities(final int capabilities) {
        this.mHandler.post(new Runnable() {
            public void run() {
                int unused = GnssLocationProvider.this.mEngineCapabilities = capabilities;
                if (GnssLocationProvider.this.hasCapability(16)) {
                    GnssLocationProvider.this.mNtpTimeHelper.enablePeriodicTimeInjection();
                    GnssLocationProvider.this.requestUtcTime();
                }
                GnssLocationProvider.this.mGnssMeasurementsProvider.onCapabilitiesUpdated(GnssLocationProvider.this.hasCapability(64));
                GnssLocationProvider.this.mGnssNavigationMessageProvider.onCapabilitiesUpdated(GnssLocationProvider.this.hasCapability(128));
                GnssLocationProvider.this.restartRequests();
            }
        });
    }

    /* access modifiers changed from: private */
    public void restartRequests() {
        Log.i("GnssLocationProvider", "restartRequests");
        restartLocationRequest();
        this.mGnssMeasurementsProvider.resumeIfStarted();
        this.mGnssNavigationMessageProvider.resumeIfStarted();
        this.mGnssBatchingProvider.resumeIfStarted();
        this.mGnssGeofenceProvider.resumeIfStarted();
    }

    private void restartLocationRequest() {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "restartLocationRequest");
        }
        this.mStarted = false;
        updateRequirements();
    }

    private void setGnssYearOfHardware(int yearOfHardware) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "setGnssYearOfHardware called with " + yearOfHardware);
        }
        this.mHardwareYear = yearOfHardware;
    }

    private void checkIfNeedEmergencyApn(String serverName) {
        Log.d("GnssLocationProvider", "checkIfNeedEmergencyApn");
        for (String s : new String[]{"e-slp.e911.kddi.ne.jp"}) {
            if (s.equals(serverName)) {
                Log.d("GnssLocationProvider", "eslp match, need to open emergency apn.");
                this.mNeedEmergencyApn = true;
                return;
            }
        }
        Log.d("GnssLocationProvider", "eslp  not match, don't need to open emergency apn.");
        this.mNeedEmergencyApn = false;
    }

    private void requestSuplDns(String fqdn) {
        Log.d("GnssLocationProvider", "JNI Call requestSuplDns");
        if (SUPL_ES_PDN_SWITCH) {
            Log.d("GnssLocationProvider", "JNI Call requestSuplDns(SUPL_ES_PDN_SWITCH is Enable)");
            this.mSuplServerHostES = fqdn;
            checkIfNeedEmergencyApn(fqdn);
            sendMessage(55, 0, fqdn);
        }
    }

    private void setGnssHardwareModelName(String modelName) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "setGnssModelName called with " + modelName);
        }
        this.mHardwareModelName = modelName;
    }

    private void reportGnssServiceDied() {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "reportGnssServiceDied");
        }
        this.mHandler.post(new Runnable() {
            public final void run() {
                GnssLocationProvider.lambda$reportGnssServiceDied$2(GnssLocationProvider.this);
            }
        });
    }

    public static /* synthetic */ void lambda$reportGnssServiceDied$2(GnssLocationProvider gnssLocationProvider) {
        class_init_native();
        native_init_once();
        if (gnssLocationProvider.isEnabled()) {
            gnssLocationProvider.handleEnable();
            gnssLocationProvider.reloadGpsProperties(gnssLocationProvider.mContext, gnssLocationProvider.mProperties);
        }
    }

    public GnssSystemInfoProvider getGnssSystemInfoProvider() {
        return new GnssSystemInfoProvider() {
            public int getGnssYearOfHardware() {
                return GnssLocationProvider.this.mHardwareYear;
            }

            public String getGnssHardwareModelName() {
                return GnssLocationProvider.this.mHardwareModelName;
            }
        };
    }

    public GnssBatchingProvider getGnssBatchingProvider() {
        return this.mGnssBatchingProvider;
    }

    public GnssMetricsProvider getGnssMetricsProvider() {
        return new GnssMetricsProvider() {
            public String getGnssMetricsAsProtoString() {
                return GnssLocationProvider.this.mGnssMetrics.dumpGnssMetricsAsProtoString();
            }
        };
    }

    private void reportLocationBatch(Location[] locationArray) {
        List<Location> locations = new ArrayList<>(Arrays.asList(locationArray));
        if (DEBUG) {
            Log.d("GnssLocationProvider", "Location batch of size " + locationArray.length + " reported");
        }
        try {
            this.mILocationManager.reportLocationBatch(locations);
        } catch (RemoteException e) {
            Log.e("GnssLocationProvider", "RemoteException calling reportLocationBatch");
        }
    }

    private void xtraDownloadRequest() {
        Log.i("GnssLocationProvider", "xtraDownloadRequest");
        sendMessage(6, 0, null);
    }

    private int getGeofenceStatus(int status) {
        if (status == GPS_GEOFENCE_ERROR_GENERIC) {
            return 5;
        }
        if (status == 0) {
            return 0;
        }
        if (status == 100) {
            return 1;
        }
        switch (status) {
            case GPS_GEOFENCE_ERROR_INVALID_TRANSITION /*-103*/:
                return 4;
            case GPS_GEOFENCE_ERROR_ID_UNKNOWN /*-102*/:
                return 3;
            case GPS_GEOFENCE_ERROR_ID_EXISTS /*-101*/:
                return 2;
            default:
                return -1;
        }
    }

    private void reportGeofenceTransition(int geofenceId, Location location, int transition, long transitionTimestamp) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        this.mGeofenceHardwareImpl.reportGeofenceTransition(geofenceId, location, transition, transitionTimestamp, 0, FusedBatchOptions.SourceTechnologies.GNSS);
    }

    private void reportGeofenceStatus(int status, Location location) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        int monitorStatus = 1;
        if (status == 2) {
            monitorStatus = 0;
        }
        this.mGeofenceHardwareImpl.reportGeofenceMonitorStatus(0, monitorStatus, location, FusedBatchOptions.SourceTechnologies.GNSS);
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

    public void reportNiNotification(int notificationId, int niType, int notifyFlags, int timeout, int defaultResponse, String requestorId, String text, int requestorIdEncoding, int textEncoding) {
        Log.i("GnssLocationProvider", "reportNiNotification: entered");
        Log.i("GnssLocationProvider", "notificationId: " + notificationId + ", niType: " + niType + ", notifyFlags: " + notifyFlags + ", timeout: " + timeout + ", defaultResponse: " + defaultResponse);
        StringBuilder sb = new StringBuilder();
        sb.append("requestorId: ");
        sb.append(requestorId);
        sb.append(", text: ");
        sb.append(text);
        sb.append(", requestorIdEncoding: ");
        sb.append(requestorIdEncoding);
        sb.append(", textEncoding: ");
        sb.append(textEncoding);
        Log.i("GnssLocationProvider", sb.toString());
        GpsNetInitiatedHandler.GpsNiNotification notification = new GpsNetInitiatedHandler.GpsNiNotification();
        notification.notificationId = notificationId;
        notification.niType = niType;
        boolean z = false;
        notification.needNotify = (notifyFlags & 1) != 0;
        notification.needVerify = (notifyFlags & 2) != 0;
        if ((notifyFlags & 4) != 0) {
            z = true;
        }
        notification.privacyOverride = z;
        notification.timeout = timeout;
        notification.defaultResponse = defaultResponse;
        notification.requestorId = requestorId;
        notification.text = text;
        notification.requestorIdEncoding = requestorIdEncoding;
        notification.textEncoding = textEncoding;
        this.mNIHandler.handleNiNotification(notification);
    }

    private void requestSetID(int flags) {
        TelephonyManager phone = (TelephonyManager) this.mContext.getSystemService("phone");
        int type = 0;
        String data = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        if ((flags & 1) == 1) {
            String data_temp = phone.getSubscriberId();
            if (data_temp != null) {
                data = data_temp;
                type = 1;
            }
        } else if ((flags & 2) == 2) {
            String data_temp2 = phone.getLine1Number();
            if (data_temp2 != null) {
                data = data_temp2;
                type = 2;
            }
        }
        native_agps_set_id(type, data);
    }

    private void requestLocation(boolean independentFromGnss) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "requestLocation. independentFromGnss: " + independentFromGnss);
        }
        sendMessage(16, 0, Boolean.valueOf(independentFromGnss));
    }

    /* access modifiers changed from: private */
    public void requestUtcTime() {
        if (this.mRequesetUtcTime) {
            Log.d("GnssLocationProvider", "reject utc time request");
            return;
        }
        this.mRequesetUtcTime = true;
        sendMessage(5, 0, null);
    }

    private void requestRefLocation() {
        TelephonyManager phone = (TelephonyManager) this.mContext.getSystemService("phone");
        int phoneType = phone.getPhoneType();
        int type = 1;
        if (phoneType == 1) {
            GsmCellLocation gsm_cell = (GsmCellLocation) phone.getCellLocation();
            if (gsm_cell == null || phone.getNetworkOperator() == null || phone.getNetworkOperator().length() <= 3) {
                Log.e("GnssLocationProvider", "Error getting cell location info.");
                return;
            }
            int mcc = Integer.parseInt(phone.getNetworkOperator().substring(0, 3));
            int mnc = Integer.parseInt(phone.getNetworkOperator().substring(3));
            int networkType = phone.getNetworkType();
            if (networkType == 3 || networkType == 8 || networkType == 9 || networkType == 10 || networkType == 15) {
                type = 2;
            }
            native_agps_set_ref_location_cellid(type, mcc, mnc, gsm_cell.getLac(), gsm_cell.getCid());
        } else if (phoneType == 2) {
            Log.e("GnssLocationProvider", "CDMA not supported.");
        }
    }

    /* access modifiers changed from: private */
    public void wakelockRelease() {
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        } else {
            Log.e("GnssLocationProvider", "WakeLock release error");
        }
    }

    /* access modifiers changed from: private */
    public void sendMessage(int message, int arg, Object obj) {
        this.mWakeLock.acquire();
        if (Log.isLoggable("GnssLocationProvider", 4)) {
            Log.i("GnssLocationProvider", "WakeLock acquired by sendMessage(" + messageIdAsString(message) + ", " + arg + ", " + obj + ")");
        }
        this.mHandler.obtainMessage(message, arg, 1, obj).sendToTarget();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0039, code lost:
        if (r8 != null) goto L_0x003b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003b, code lost:
        r8.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0049, code lost:
        if (r8 == null) goto L_0x004c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004c, code lost:
        return null;
     */
    private String getSelectedApn() {
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(Uri.parse("content://telephony/carriers/preferapn"), new String[]{"apn"}, null, null, "name ASC");
            if (cursor == null || !cursor.moveToFirst()) {
                Log.e("GnssLocationProvider", "No APN found to select.");
            } else {
                String string = cursor.getString(0);
                if (cursor != null) {
                    cursor.close();
                }
                return string;
            }
        } catch (Exception e) {
            Log.e("GnssLocationProvider", "Error encountered on selecting the APN.", e);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0058, code lost:
        if (r2 != null) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005a, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0077, code lost:
        if (r2 == null) goto L_0x007a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007a, code lost:
        return 0;
     */
    private int getApnIpType(String apn) {
        ensureInHandlerThread();
        if (apn == null) {
            return 0;
        }
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(Telephony.Carriers.CONTENT_URI, new String[]{"protocol"}, String.format("current = 1 and apn = '%s' and carrier_enabled = 1", new Object[]{apn}), null, "name ASC");
            if (cursor == null || !cursor.moveToFirst()) {
                Log.e("GnssLocationProvider", "No entry found in query for APN: " + apn);
            } else {
                int translateToApnIpType = translateToApnIpType(cursor.getString(0), apn);
                if (cursor != null) {
                    cursor.close();
                }
                return translateToApnIpType;
            }
        } catch (Exception e) {
            Log.e("GnssLocationProvider", "Error encountered on APN query for: " + apn, e);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private int translateToApnIpType(String ipProtocol, String apn) {
        if ("IP".equals(ipProtocol)) {
            return 1;
        }
        if ("IPV6".equals(ipProtocol)) {
            return 2;
        }
        if ("IPV4V6".equals(ipProtocol)) {
            return 3;
        }
        Log.e("GnssLocationProvider", String.format("Unknown IP Protocol: %s, for APN: %s", new Object[]{ipProtocol, apn}));
        return 0;
    }

    private void setRouting() {
        if (this.mAGpsDataConnectionIpAddr != null) {
            if (!this.mConnMgr.requestRouteToHostAddress(3, this.mAGpsDataConnectionIpAddr)) {
                Log.e("GnssLocationProvider", "Error requesting route to host: " + this.mAGpsDataConnectionIpAddr);
            } else {
                Log.d("GnssLocationProvider", "Successfully requested route to host: " + this.mAGpsDataConnectionIpAddr);
            }
        }
    }

    private boolean isDataNetworkConnected() {
        NetworkInfo activeNetworkInfo = this.mConnMgr.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void ensureInHandlerThread() {
        if (this.mHandler == null || Looper.myLooper() != this.mHandler.getLooper()) {
            throw new RuntimeException("This method must run on the Handler thread.");
        }
    }

    private String agpsDataConnStateAsString() {
        switch (this.mAGpsDataConnectionState) {
            case 0:
                return "CLOSED";
            case 1:
                return "OPENING";
            case 2:
                return "OPEN";
            default:
                return "<Unknown>";
        }
    }

    private String agpsDataConnStatusAsString(int agpsDataConnStatus) {
        switch (agpsDataConnStatus) {
            case 1:
                return "REQUEST";
            case 2:
                return "RELEASE";
            case 3:
                return "CONNECTED";
            case 4:
                return "DONE";
            case 5:
                return "FAILED";
            default:
                return "<Unknown>";
        }
    }

    /* access modifiers changed from: private */
    public String messageIdAsString(int message) {
        switch (message) {
            case 2:
                return "ENABLE";
            case 3:
                return "SET_REQUEST";
            case 4:
                return "UPDATE_NETWORK_STATE";
            case 5:
                return "INJECT_NTP_TIME";
            case 6:
                return "DOWNLOAD_XTRA_DATA";
            case 7:
                return "UPDATE_LOCATION";
            case 11:
                return "DOWNLOAD_XTRA_DATA_FINISHED";
            case 12:
                return "SUBSCRIPTION_OR_SIM_CHANGED";
            case 13:
                return "INITIALIZE_HANDLER";
            case 14:
                return "REQUEST_SUPL_CONNECTION";
            case 15:
                return "RELEASE_SUPL_CONNECTION";
            case 16:
                return "REQUEST_LOCATION";
            case 17:
                return "REPORT_LOCATION";
            case 18:
                return "REPORT_SV_STATUS";
            default:
                return "<Unknown>";
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        StringBuilder s = new StringBuilder();
        s.append("  mStarted=");
        s.append(this.mStarted);
        s.append(10);
        s.append("  mFixInterval=");
        s.append(this.mFixInterval);
        s.append(10);
        s.append("  mLowPowerMode=");
        s.append(this.mLowPowerMode);
        s.append(10);
        s.append("  mGnssMeasurementsProvider.isRegistered()=");
        s.append(this.mGnssMeasurementsProvider.isRegistered());
        s.append(10);
        s.append("  mGnssNavigationMessageProvider.isRegistered()=");
        s.append(this.mGnssNavigationMessageProvider.isRegistered());
        s.append(10);
        s.append("  mDisableGps (battery saver mode)=");
        s.append(this.mDisableGps);
        s.append(10);
        s.append("  mEngineCapabilities=0x");
        s.append(Integer.toHexString(this.mEngineCapabilities));
        s.append(" ( ");
        if (hasCapability(1)) {
            s.append("SCHEDULING ");
        }
        if (hasCapability(2)) {
            s.append("MSB ");
        }
        if (hasCapability(4)) {
            s.append("MSA ");
        }
        if (hasCapability(8)) {
            s.append("SINGLE_SHOT ");
        }
        if (hasCapability(16)) {
            s.append("ON_DEMAND_TIME ");
        }
        if (hasCapability(32)) {
            s.append("GEOFENCING ");
        }
        if (hasCapability(64)) {
            s.append("MEASUREMENTS ");
        }
        if (hasCapability(128)) {
            s.append("NAV_MESSAGES ");
        }
        s.append(")\n");
        s.append(this.mGnssMetrics.dumpGnssMetricsAsText());
        s.append("  native internal state: ");
        s.append(native_get_internal_state());
        s.append("\n");
        pw.append(s);
    }

    public boolean isLocalDBEnabled() {
        return false;
    }

    public void initDefaultApnObserver(Handler handler) {
    }

    public void reportContextStatus(int status) {
    }

    public int getPreferred_accuracy() {
        return 0;
    }

    public boolean shouldReStartNavi() {
        return false;
    }

    public WorkSource getWorkSource() {
        return this.mWorkSource;
    }

    public void handleGnssRequirementsChange(int changereson) {
    }

    public void handleGnssNavigatingStateChange(boolean start) {
    }

    /* access modifiers changed from: protected */
    public String getSvType(int svidWithFlag) {
        return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
    }

    /* access modifiers changed from: protected */
    public boolean hwCheckLowPowerMode() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void handleReportIdle(boolean isGnssStarted, boolean isInteractive) {
    }

    /* access modifiers changed from: protected */
    public void hwLoadPropertiesFromResource(Context context, Properties properties) {
    }

    /* access modifiers changed from: protected */
    public void hwHandleReportLocation(boolean hasLatLong, Location location) {
    }
}
