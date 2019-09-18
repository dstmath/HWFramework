package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.INetworkPolicyManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.IMaintenanceActivityListener;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.KeyValueListParser;
import android.util.Log;
import android.util.MutableLong;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.TimeUtils;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.AtomicFile;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.AnyMotionDetector;
import com.android.server.UiModeManagerService;
import com.android.server.am.BatteryStatsService;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.job.controllers.JobStatus;
import com.android.server.net.NetworkPolicyManagerInternal;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.usage.AppStandbyController;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class DeviceIdleController extends SystemService implements AnyMotionDetector.DeviceIdleCallback {
    private static final boolean COMPRESS_TIME = false;
    /* access modifiers changed from: private */
    public static boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int EVENT_BUFFER_SIZE = 100;
    private static final int EVENT_DEEP_IDLE = 4;
    private static final int EVENT_DEEP_MAINTENANCE = 5;
    private static final int EVENT_LIGHT_IDLE = 2;
    private static final int EVENT_LIGHT_MAINTENANCE = 3;
    private static final int EVENT_NORMAL = 1;
    private static final int EVENT_NULL = 0;
    private static final int LIGHT_STATE_ACTIVE = 0;
    private static final int LIGHT_STATE_IDLE = 4;
    private static final int LIGHT_STATE_IDLE_MAINTENANCE = 6;
    private static final int LIGHT_STATE_INACTIVE = 1;
    private static final int LIGHT_STATE_OVERRIDE = 7;
    private static final int LIGHT_STATE_PRE_IDLE = 3;
    private static final int LIGHT_STATE_WAITING_FOR_NETWORK = 5;
    private static final int MSG_FINISH_IDLE_OP = 8;
    private static final int MSG_REPORT_ACTIVE = 5;
    private static final int MSG_REPORT_IDLE_OFF = 4;
    private static final int MSG_REPORT_IDLE_ON = 2;
    private static final int MSG_REPORT_IDLE_ON_LIGHT = 3;
    private static final int MSG_REPORT_MAINTENANCE_ACTIVITY = 7;
    private static final int MSG_REPORT_TEMP_APP_WHITELIST_CHANGED = 9;
    private static final int MSG_TEMP_APP_WHITELIST_TIMEOUT = 6;
    private static final int MSG_WRITE_CONFIG = 1;
    private static final int READ_DB_DELAY_TIME = 10000;
    private static final int STATE_ACTIVE = 0;
    private static final int STATE_IDLE = 5;
    private static final int STATE_IDLE_MAINTENANCE = 6;
    private static final int STATE_IDLE_PENDING = 2;
    private static final int STATE_INACTIVE = 1;
    private static final int STATE_LOCATING = 4;
    private static final int STATE_SENSING = 3;
    private static final String TAG = "DeviceIdleController";
    private int mActiveIdleOpCount;
    private PowerManager.WakeLock mActiveIdleWakeLock;
    private AlarmManager mAlarmManager;
    private boolean mAlarmsActive;
    private AnyMotionDetector mAnyMotionDetector;
    private final AppStateTracker mAppStateTracker;
    /* access modifiers changed from: private */
    public IBatteryStats mBatteryStats;
    BinderService mBinderService;
    private boolean mCharging;
    public final AtomicFile mConfigFile = new AtomicFile(new File(getSystemDir(), "deviceidle.xml"));
    private ConnectivityService mConnectivityService;
    /* access modifiers changed from: private */
    public Constants mConstants;
    private long mCurIdleBudget;
    private final AlarmManager.OnAlarmListener mDeepAlarmListener = new AlarmManager.OnAlarmListener() {
        public void onAlarm() {
            synchronized (DeviceIdleController.this) {
                DeviceIdleController.this.stepIdleStateLocked("s:alarm");
            }
        }
    };
    private boolean mDeepEnabled;
    private final int[] mEventCmds = new int[100];
    private final String[] mEventReasons = new String[100];
    private final long[] mEventTimes = new long[100];
    private boolean mForceIdle;
    private final LocationListener mGenericLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            synchronized (DeviceIdleController.this) {
                DeviceIdleController.this.receivedGenericLocationLocked(location);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };
    /* access modifiers changed from: private */
    public PowerManager.WakeLock mGoingIdleWakeLock;
    private final LocationListener mGpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            synchronized (DeviceIdleController.this) {
                DeviceIdleController.this.receivedGpsLocationLocked(location);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };
    final MyHandler mHandler = new MyHandler(BackgroundThread.getHandler().getLooper());
    private boolean mHasGps;
    private boolean mHasNetworkLocation;
    /* access modifiers changed from: private */
    public Intent mIdleIntent;
    /* access modifiers changed from: private */
    public final BroadcastReceiver mIdleStartedDoneReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(intent.getAction())) {
                DeviceIdleController.this.mHandler.sendEmptyMessageDelayed(8, DeviceIdleController.this.mConstants.MIN_DEEP_MAINTENANCE_TIME);
            } else {
                DeviceIdleController.this.mHandler.sendEmptyMessageDelayed(8, DeviceIdleController.this.mConstants.MIN_LIGHT_MAINTENANCE_TIME);
            }
        }
    };
    private long mInactiveTimeout;
    private final BroadcastReceiver mInteractivityReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            synchronized (DeviceIdleController.this) {
                DeviceIdleController.this.updateInteractivityLocked();
            }
        }
    };
    private boolean mJobsActive;
    private Location mLastGenericLocation;
    private Location mLastGpsLocation;
    private final AlarmManager.OnAlarmListener mLightAlarmListener = new AlarmManager.OnAlarmListener() {
        public void onAlarm() {
            synchronized (DeviceIdleController.this) {
                DeviceIdleController.this.stepLightIdleStateLocked("s:alarm");
            }
        }
    };
    private boolean mLightEnabled;
    /* access modifiers changed from: private */
    public Intent mLightIdleIntent;
    /* access modifiers changed from: private */
    public int mLightState;
    private ActivityManagerInternal mLocalActivityManager;
    /* access modifiers changed from: private */
    public PowerManagerInternal mLocalPowerManager;
    private boolean mLocated;
    private boolean mLocating;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    /* access modifiers changed from: private */
    public final RemoteCallbackList<IMaintenanceActivityListener> mMaintenanceActivityListeners = new RemoteCallbackList<>();
    private long mMaintenanceStartTime;
    /* access modifiers changed from: private */
    public final MotionListener mMotionListener = new MotionListener();
    /* access modifiers changed from: private */
    public Sensor mMotionSensor;
    private boolean mNetworkConnected;
    /* access modifiers changed from: private */
    public INetworkPolicyManager mNetworkPolicyManager;
    /* access modifiers changed from: private */
    public NetworkPolicyManagerInternal mNetworkPolicyManagerInternal;
    private long mNextAlarmTime;
    private long mNextIdleDelay;
    private long mNextIdlePendingDelay;
    private long mNextLightAlarmTime;
    private long mNextLightIdleDelay;
    private long mNextSensingTimeoutAlarmTime;
    private boolean mNotMoving;
    private PowerManager mPowerManager;
    private int[] mPowerSaveWhitelistAllAppIdArray = new int[0];
    private final SparseBooleanArray mPowerSaveWhitelistAllAppIds = new SparseBooleanArray();
    private final ArrayMap<String, Integer> mPowerSaveWhitelistApps = new ArrayMap<>();
    private final ArrayMap<String, Integer> mPowerSaveWhitelistAppsExceptIdle = new ArrayMap<>();
    private int[] mPowerSaveWhitelistExceptIdleAppIdArray = new int[0];
    private final SparseBooleanArray mPowerSaveWhitelistExceptIdleAppIds = new SparseBooleanArray();
    private final SparseBooleanArray mPowerSaveWhitelistSystemAppIds = new SparseBooleanArray();
    private final SparseBooleanArray mPowerSaveWhitelistSystemAppIdsExceptIdle = new SparseBooleanArray();
    private int[] mPowerSaveWhitelistUserAppIdArray = new int[0];
    private final SparseBooleanArray mPowerSaveWhitelistUserAppIds = new SparseBooleanArray();
    private final ArrayMap<String, Integer> mPowerSaveWhitelistUserApps = new ArrayMap<>();
    private final ArraySet<String> mPowerSaveWhitelistUserAppsExceptIdle = new ArraySet<>();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Removed duplicated region for block: B:17:0x003d  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0058  */
        /* JADX WARNING: Removed duplicated region for block: B:38:0x0070  */
        /* JADX WARNING: Removed duplicated region for block: B:45:? A[RETURN, SYNTHETIC] */
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            int hashCode = action.hashCode();
            boolean z = true;
            if (hashCode == -1538406691) {
                if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                    c = 1;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
            } else if (hashCode == -1172645946) {
                if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    c = 0;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
            } else if (hashCode == 525384130 && action.equals("android.intent.action.PACKAGE_REMOVED")) {
                c = 2;
                switch (c) {
                    case 0:
                        DeviceIdleController.this.updateConnectivityState(intent);
                        return;
                    case 1:
                        synchronized (DeviceIdleController.this) {
                            int plugged = intent.getIntExtra("plugged", 0);
                            DeviceIdleController deviceIdleController = DeviceIdleController.this;
                            if (plugged == 0) {
                                z = false;
                            }
                            deviceIdleController.updateChargingLocked(z);
                        }
                        return;
                    case 2:
                        if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                            Uri data = intent.getData();
                            if (data != null) {
                                String schemeSpecificPart = data.getSchemeSpecificPart();
                                String ssp = schemeSpecificPart;
                                if (schemeSpecificPart != null) {
                                    DeviceIdleController.this.removePowerSaveWhitelistAppInternal(ssp);
                                    return;
                                }
                                return;
                            }
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
        }
    };
    private ArrayMap<String, Integer> mRemovedFromSystemWhitelistApps = new ArrayMap<>();
    private boolean mReportedMaintenanceActivity;
    private boolean mScreenLocked;
    private ActivityManagerInternal.ScreenObserver mScreenObserver = new ActivityManagerInternal.ScreenObserver() {
        public void onAwakeStateChanged(boolean isAwake) {
        }

        public void onKeyguardStateChanged(boolean isShowing) {
            synchronized (DeviceIdleController.this) {
                DeviceIdleController.this.keyguardShowingLocked(isShowing);
            }
        }
    };
    private boolean mScreenOn;
    private final AlarmManager.OnAlarmListener mSensingTimeoutAlarmListener = new AlarmManager.OnAlarmListener() {
        public void onAlarm() {
            if (DeviceIdleController.this.mState == 3) {
                synchronized (DeviceIdleController.this) {
                    DeviceIdleController.this.becomeInactiveIfAppropriateLocked();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public SensorManager mSensorManager;
    /* access modifiers changed from: private */
    public int mState;
    private int[] mTempWhitelistAppIdArray = new int[0];
    private final SparseArray<Pair<MutableLong, String>> mTempWhitelistAppIdEndTimes = new SparseArray<>();

    private final class BinderService extends IDeviceIdleController.Stub {
        private BinderService() {
        }

        public void addPowerSaveWhitelistApp(String name) {
            if (DeviceIdleController.DEBUG) {
                Slog.i(DeviceIdleController.TAG, "addPowerSaveWhitelistApp(name = " + name + ")");
            }
            DeviceIdleController.this.getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                DeviceIdleController.this.addPowerSaveWhitelistAppInternal(name);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void removePowerSaveWhitelistApp(String name) {
            if (DeviceIdleController.DEBUG) {
                Slog.i(DeviceIdleController.TAG, "removePowerSaveWhitelistApp(name = " + name + ")");
            }
            DeviceIdleController.this.getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                DeviceIdleController.this.removePowerSaveWhitelistAppInternal(name);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void removeSystemPowerWhitelistApp(String name) {
            if (DeviceIdleController.DEBUG) {
                Slog.d(DeviceIdleController.TAG, "removeAppFromSystemWhitelist(name = " + name + ")");
            }
            DeviceIdleController.this.getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                DeviceIdleController.this.removeSystemPowerWhitelistAppInternal(name);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void restoreSystemPowerWhitelistApp(String name) {
            if (DeviceIdleController.DEBUG) {
                Slog.d(DeviceIdleController.TAG, "restoreAppToSystemWhitelist(name = " + name + ")");
            }
            DeviceIdleController.this.getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                DeviceIdleController.this.restoreSystemPowerWhitelistAppInternal(name);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public String[] getRemovedSystemPowerWhitelistApps() {
            return DeviceIdleController.this.getRemovedSystemPowerWhitelistAppsInternal();
        }

        public String[] getSystemPowerWhitelistExceptIdle() {
            return DeviceIdleController.this.getSystemPowerWhitelistExceptIdleInternal();
        }

        public String[] getSystemPowerWhitelist() {
            return DeviceIdleController.this.getSystemPowerWhitelistInternal();
        }

        public String[] getUserPowerWhitelist() {
            return DeviceIdleController.this.getUserPowerWhitelistInternal();
        }

        public String[] getFullPowerWhitelistExceptIdle() {
            return DeviceIdleController.this.getFullPowerWhitelistExceptIdleInternal();
        }

        public String[] getFullPowerWhitelist() {
            return DeviceIdleController.this.getFullPowerWhitelistInternal();
        }

        public int[] getAppIdWhitelistExceptIdle() {
            return DeviceIdleController.this.getAppIdWhitelistExceptIdleInternal();
        }

        public int[] getAppIdWhitelist() {
            return DeviceIdleController.this.getAppIdWhitelistInternal();
        }

        public int[] getAppIdUserWhitelist() {
            return DeviceIdleController.this.getAppIdUserWhitelistInternal();
        }

        public int[] getAppIdTempWhitelist() {
            return DeviceIdleController.this.getAppIdTempWhitelistInternal();
        }

        public boolean isPowerSaveWhitelistExceptIdleApp(String name) {
            return DeviceIdleController.this.isPowerSaveWhitelistExceptIdleAppInternal(name);
        }

        public int getIdleStateDetailed() {
            DeviceIdleController.this.getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            return DeviceIdleController.this.mState;
        }

        public int getLightIdleStateDetailed() {
            DeviceIdleController.this.getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            return DeviceIdleController.this.mLightState;
        }

        public boolean isPowerSaveWhitelistApp(String name) {
            return DeviceIdleController.this.isPowerSaveWhitelistAppInternal(name);
        }

        public void addPowerSaveTempWhitelistApp(String packageName, long duration, int userId, String reason) throws RemoteException {
            DeviceIdleController.this.addPowerSaveTempWhitelistAppChecked(packageName, duration, userId, reason);
        }

        public long addPowerSaveTempWhitelistAppForMms(String packageName, int userId, String reason) throws RemoteException {
            long duration = DeviceIdleController.this.mConstants.MMS_TEMP_APP_WHITELIST_DURATION;
            DeviceIdleController.this.addPowerSaveTempWhitelistAppChecked(packageName, duration, userId, reason);
            return duration;
        }

        public long addPowerSaveTempWhitelistAppForSms(String packageName, int userId, String reason) throws RemoteException {
            long duration = DeviceIdleController.this.mConstants.SMS_TEMP_APP_WHITELIST_DURATION;
            DeviceIdleController.this.addPowerSaveTempWhitelistAppChecked(packageName, duration, userId, reason);
            return duration;
        }

        public void exitIdle(String reason) {
            DeviceIdleController.this.getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                DeviceIdleController.this.exitIdleInternal(reason);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean registerMaintenanceActivityListener(IMaintenanceActivityListener listener) {
            return DeviceIdleController.this.registerMaintenanceActivityListener(listener);
        }

        public void unregisterMaintenanceActivityListener(IMaintenanceActivityListener listener) {
            DeviceIdleController.this.unregisterMaintenanceActivityListener(listener);
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            DeviceIdleController.this.dump(fd, pw, args);
        }

        /* JADX WARNING: type inference failed for: r1v1, types: [android.os.Binder] */
        /* JADX WARNING: Multi-variable type inference failed */
        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new Shell().exec(this, in, out, err, args, callback, resultReceiver);
        }

        public int forceIdle() {
            if (1000 == Binder.getCallingUid()) {
                return DeviceIdleController.this.forceIdleInternal();
            }
            Slog.e(DeviceIdleController.TAG, " forceIdle error , permission not allowed. uid = " + Binder.getCallingUid());
            return -1;
        }
    }

    private final class Constants extends ContentObserver {
        private static final String KEY_IDLE_AFTER_INACTIVE_TIMEOUT = "idle_after_inactive_to";
        private static final String KEY_IDLE_FACTOR = "idle_factor";
        private static final String KEY_IDLE_PENDING_FACTOR = "idle_pending_factor";
        private static final String KEY_IDLE_PENDING_TIMEOUT = "idle_pending_to";
        private static final String KEY_IDLE_TIMEOUT = "idle_to";
        private static final String KEY_INACTIVE_TIMEOUT = "inactive_to";
        private static final String KEY_LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT = "light_after_inactive_to";
        private static final String KEY_LIGHT_IDLE_FACTOR = "light_idle_factor";
        private static final String KEY_LIGHT_IDLE_MAINTENANCE_MAX_BUDGET = "light_idle_maintenance_max_budget";
        private static final String KEY_LIGHT_IDLE_MAINTENANCE_MIN_BUDGET = "light_idle_maintenance_min_budget";
        private static final String KEY_LIGHT_IDLE_TIMEOUT = "light_idle_to";
        private static final String KEY_LIGHT_MAX_IDLE_TIMEOUT = "light_max_idle_to";
        private static final String KEY_LIGHT_PRE_IDLE_TIMEOUT = "light_pre_idle_to";
        private static final String KEY_LOCATING_TIMEOUT = "locating_to";
        private static final String KEY_LOCATION_ACCURACY = "location_accuracy";
        private static final String KEY_MAX_IDLE_PENDING_TIMEOUT = "max_idle_pending_to";
        private static final String KEY_MAX_IDLE_TIMEOUT = "max_idle_to";
        private static final String KEY_MAX_TEMP_APP_WHITELIST_DURATION = "max_temp_app_whitelist_duration";
        private static final String KEY_MIN_DEEP_MAINTENANCE_TIME = "min_deep_maintenance_time";
        private static final String KEY_MIN_LIGHT_MAINTENANCE_TIME = "min_light_maintenance_time";
        private static final String KEY_MIN_TIME_TO_ALARM = "min_time_to_alarm";
        private static final String KEY_MMS_TEMP_APP_WHITELIST_DURATION = "mms_temp_app_whitelist_duration";
        private static final String KEY_MOTION_INACTIVE_TIMEOUT = "motion_inactive_to";
        private static final String KEY_NOTIFICATION_WHITELIST_DURATION = "notification_whitelist_duration";
        private static final String KEY_SENSING_TIMEOUT = "sensing_to";
        private static final String KEY_SMS_TEMP_APP_WHITELIST_DURATION = "sms_temp_app_whitelist_duration";
        private static final String KEY_WAIT_FOR_UNLOCK = "wait_for_unlock";
        public long IDLE_AFTER_INACTIVE_TIMEOUT;
        public float IDLE_FACTOR;
        public float IDLE_PENDING_FACTOR;
        public long IDLE_PENDING_TIMEOUT;
        public long IDLE_TIMEOUT;
        public long INACTIVE_TIMEOUT;
        public long LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT;
        public float LIGHT_IDLE_FACTOR;
        public long LIGHT_IDLE_MAINTENANCE_MAX_BUDGET;
        public long LIGHT_IDLE_MAINTENANCE_MIN_BUDGET;
        public long LIGHT_IDLE_TIMEOUT;
        public long LIGHT_MAX_IDLE_TIMEOUT;
        public long LIGHT_PRE_IDLE_TIMEOUT;
        public long LOCATING_TIMEOUT;
        public float LOCATION_ACCURACY;
        public long MAX_IDLE_PENDING_TIMEOUT;
        public long MAX_IDLE_TIMEOUT;
        public long MAX_TEMP_APP_WHITELIST_DURATION;
        public long MIN_DEEP_MAINTENANCE_TIME;
        public long MIN_LIGHT_MAINTENANCE_TIME;
        public long MIN_TIME_TO_ALARM;
        public long MMS_TEMP_APP_WHITELIST_DURATION;
        public long MOTION_INACTIVE_TIMEOUT;
        public long NOTIFICATION_WHITELIST_DURATION;
        public long SENSING_TIMEOUT;
        public long SMS_TEMP_APP_WHITELIST_DURATION;
        public boolean WAIT_FOR_UNLOCK;
        private final KeyValueListParser mParser = new KeyValueListParser(',');
        private final ContentResolver mResolver;
        private final boolean mSmallBatteryDevice;

        public Constants(Handler handler, ContentResolver resolver) {
            super(handler);
            this.mResolver = resolver;
            this.mSmallBatteryDevice = ActivityManager.isSmallBatteryDevice();
            this.mResolver.registerContentObserver(Settings.Global.getUriFor("device_idle_constants"), false, this);
            updateConstants();
        }

        public void onChange(boolean selfChange, Uri uri) {
            updateConstants();
        }

        private void updateConstants() {
            long j;
            long j2;
            synchronized (DeviceIdleController.this) {
                try {
                    this.mParser.setString(Settings.Global.getString(this.mResolver, "device_idle_constants"));
                } catch (IllegalArgumentException e) {
                    Slog.e(DeviceIdleController.TAG, "Bad device idle settings", e);
                }
                this.LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT = this.mParser.getDurationMillis(KEY_LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT, 180000);
                this.LIGHT_PRE_IDLE_TIMEOUT = this.mParser.getDurationMillis(KEY_LIGHT_PRE_IDLE_TIMEOUT, 180000);
                this.LIGHT_IDLE_TIMEOUT = this.mParser.getDurationMillis(KEY_LIGHT_IDLE_TIMEOUT, BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS);
                this.LIGHT_IDLE_FACTOR = this.mParser.getFloat(KEY_LIGHT_IDLE_FACTOR, 2.0f);
                this.LIGHT_MAX_IDLE_TIMEOUT = this.mParser.getDurationMillis(KEY_LIGHT_MAX_IDLE_TIMEOUT, 900000);
                this.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET = this.mParser.getDurationMillis(KEY_LIGHT_IDLE_MAINTENANCE_MIN_BUDGET, 60000);
                this.LIGHT_IDLE_MAINTENANCE_MAX_BUDGET = this.mParser.getDurationMillis(KEY_LIGHT_IDLE_MAINTENANCE_MAX_BUDGET, BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS);
                this.MIN_LIGHT_MAINTENANCE_TIME = this.mParser.getDurationMillis(KEY_MIN_LIGHT_MAINTENANCE_TIME, 5000);
                this.MIN_DEEP_MAINTENANCE_TIME = this.mParser.getDurationMillis(KEY_MIN_DEEP_MAINTENANCE_TIME, 30000);
                long inactiveTimeoutDefault = ((long) ((this.mSmallBatteryDevice ? 15 : 30) * 60)) * 1000;
                this.INACTIVE_TIMEOUT = this.mParser.getDurationMillis(KEY_INACTIVE_TIMEOUT, inactiveTimeoutDefault);
                KeyValueListParser keyValueListParser = this.mParser;
                if (!DeviceIdleController.DEBUG) {
                    j = 240000;
                } else {
                    j = 60000;
                }
                this.SENSING_TIMEOUT = keyValueListParser.getDurationMillis(KEY_SENSING_TIMEOUT, j);
                KeyValueListParser keyValueListParser2 = this.mParser;
                if (!DeviceIdleController.DEBUG) {
                    j2 = 30000;
                } else {
                    j2 = 15000;
                }
                this.LOCATING_TIMEOUT = keyValueListParser2.getDurationMillis(KEY_LOCATING_TIMEOUT, j2);
                this.LOCATION_ACCURACY = this.mParser.getFloat(KEY_LOCATION_ACCURACY, 20.0f);
                long j3 = inactiveTimeoutDefault;
                this.MOTION_INACTIVE_TIMEOUT = this.mParser.getDurationMillis(KEY_MOTION_INACTIVE_TIMEOUT, 600000);
                this.IDLE_AFTER_INACTIVE_TIMEOUT = this.mParser.getDurationMillis(KEY_IDLE_AFTER_INACTIVE_TIMEOUT, ((long) ((this.mSmallBatteryDevice ? 15 : 30) * 60)) * 1000);
                this.IDLE_PENDING_TIMEOUT = this.mParser.getDurationMillis(KEY_IDLE_PENDING_TIMEOUT, BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS);
                this.MAX_IDLE_PENDING_TIMEOUT = this.mParser.getDurationMillis(KEY_MAX_IDLE_PENDING_TIMEOUT, 600000);
                this.IDLE_PENDING_FACTOR = this.mParser.getFloat(KEY_IDLE_PENDING_FACTOR, 2.0f);
                this.IDLE_TIMEOUT = this.mParser.getDurationMillis(KEY_IDLE_TIMEOUT, AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT);
                this.MAX_IDLE_TIMEOUT = this.mParser.getDurationMillis(KEY_MAX_IDLE_TIMEOUT, 21600000);
                this.IDLE_FACTOR = this.mParser.getFloat(KEY_IDLE_FACTOR, 2.0f);
                this.MIN_TIME_TO_ALARM = this.mParser.getDurationMillis(KEY_MIN_TIME_TO_ALARM, AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT);
                this.MAX_TEMP_APP_WHITELIST_DURATION = this.mParser.getDurationMillis(KEY_MAX_TEMP_APP_WHITELIST_DURATION, BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS);
                this.MMS_TEMP_APP_WHITELIST_DURATION = this.mParser.getDurationMillis(KEY_MMS_TEMP_APP_WHITELIST_DURATION, 60000);
                this.SMS_TEMP_APP_WHITELIST_DURATION = this.mParser.getDurationMillis(KEY_SMS_TEMP_APP_WHITELIST_DURATION, 20000);
                this.NOTIFICATION_WHITELIST_DURATION = this.mParser.getDurationMillis(KEY_NOTIFICATION_WHITELIST_DURATION, 30000);
                this.WAIT_FOR_UNLOCK = this.mParser.getBoolean(KEY_WAIT_FOR_UNLOCK, false);
            }
        }

        /* access modifiers changed from: package-private */
        public void dump(PrintWriter pw) {
            pw.println("  Settings:");
            pw.print("    ");
            pw.print(KEY_LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_LIGHT_PRE_IDLE_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.LIGHT_PRE_IDLE_TIMEOUT, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_LIGHT_IDLE_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.LIGHT_IDLE_TIMEOUT, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_LIGHT_IDLE_FACTOR);
            pw.print("=");
            pw.print(this.LIGHT_IDLE_FACTOR);
            pw.println();
            pw.print("    ");
            pw.print(KEY_LIGHT_MAX_IDLE_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.LIGHT_MAX_IDLE_TIMEOUT, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_LIGHT_IDLE_MAINTENANCE_MIN_BUDGET);
            pw.print("=");
            TimeUtils.formatDuration(this.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_LIGHT_IDLE_MAINTENANCE_MAX_BUDGET);
            pw.print("=");
            TimeUtils.formatDuration(this.LIGHT_IDLE_MAINTENANCE_MAX_BUDGET, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_LIGHT_MAINTENANCE_TIME);
            pw.print("=");
            TimeUtils.formatDuration(this.MIN_LIGHT_MAINTENANCE_TIME, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_DEEP_MAINTENANCE_TIME);
            pw.print("=");
            TimeUtils.formatDuration(this.MIN_DEEP_MAINTENANCE_TIME, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_INACTIVE_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.INACTIVE_TIMEOUT, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_SENSING_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.SENSING_TIMEOUT, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_LOCATING_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.LOCATING_TIMEOUT, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_LOCATION_ACCURACY);
            pw.print("=");
            pw.print(this.LOCATION_ACCURACY);
            pw.print("m");
            pw.println();
            pw.print("    ");
            pw.print(KEY_MOTION_INACTIVE_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.MOTION_INACTIVE_TIMEOUT, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_IDLE_AFTER_INACTIVE_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.IDLE_AFTER_INACTIVE_TIMEOUT, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_IDLE_PENDING_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.IDLE_PENDING_TIMEOUT, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MAX_IDLE_PENDING_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.MAX_IDLE_PENDING_TIMEOUT, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_IDLE_PENDING_FACTOR);
            pw.print("=");
            pw.println(this.IDLE_PENDING_FACTOR);
            pw.print("    ");
            pw.print(KEY_IDLE_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.IDLE_TIMEOUT, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MAX_IDLE_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.MAX_IDLE_TIMEOUT, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_IDLE_FACTOR);
            pw.print("=");
            pw.println(this.IDLE_FACTOR);
            pw.print("    ");
            pw.print(KEY_MIN_TIME_TO_ALARM);
            pw.print("=");
            TimeUtils.formatDuration(this.MIN_TIME_TO_ALARM, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MAX_TEMP_APP_WHITELIST_DURATION);
            pw.print("=");
            TimeUtils.formatDuration(this.MAX_TEMP_APP_WHITELIST_DURATION, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MMS_TEMP_APP_WHITELIST_DURATION);
            pw.print("=");
            TimeUtils.formatDuration(this.MMS_TEMP_APP_WHITELIST_DURATION, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_SMS_TEMP_APP_WHITELIST_DURATION);
            pw.print("=");
            TimeUtils.formatDuration(this.SMS_TEMP_APP_WHITELIST_DURATION, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_NOTIFICATION_WHITELIST_DURATION);
            pw.print("=");
            TimeUtils.formatDuration(this.NOTIFICATION_WHITELIST_DURATION, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_WAIT_FOR_UNLOCK);
            pw.print("=");
            pw.println(this.WAIT_FOR_UNLOCK);
        }
    }

    public class LocalService {
        public LocalService() {
        }

        public void addPowerSaveTempWhitelistApp(int callingUid, String packageName, long duration, int userId, boolean sync, String reason) {
            DeviceIdleController.this.addPowerSaveTempWhitelistAppInternal(callingUid, packageName, duration, userId, sync, reason);
        }

        public void addPowerSaveTempWhitelistAppDirect(int appId, long duration, boolean sync, String reason) {
            DeviceIdleController.this.addPowerSaveTempWhitelistAppDirectInternal(0, appId, duration, sync, reason);
        }

        public long getNotificationWhitelistDuration() {
            return DeviceIdleController.this.mConstants.NOTIFICATION_WHITELIST_DURATION;
        }

        public void setJobsActive(boolean active) {
            DeviceIdleController.this.setJobsActive(active);
        }

        public void setAlarmsActive(boolean active) {
            DeviceIdleController.this.setAlarmsActive(active);
        }

        public boolean isAppOnWhitelist(int appid) {
            return DeviceIdleController.this.isAppOnWhitelistInternal(appid);
        }

        public int[] getPowerSaveWhitelistUserAppIds() {
            return DeviceIdleController.this.getPowerSaveWhitelistUserAppIds();
        }

        public int[] getPowerSaveTempWhitelistAppIds() {
            return DeviceIdleController.this.getAppIdTempWhitelistInternal();
        }
    }

    private final class MotionListener extends TriggerEventListener implements SensorEventListener {
        boolean active;

        private MotionListener() {
            this.active = false;
        }

        public void onTrigger(TriggerEvent event) {
            synchronized (DeviceIdleController.this) {
                this.active = false;
                DeviceIdleController.this.motionLocked();
            }
        }

        public void onSensorChanged(SensorEvent event) {
            synchronized (DeviceIdleController.this) {
                DeviceIdleController.this.mSensorManager.unregisterListener(this, DeviceIdleController.this.mMotionSensor);
                this.active = false;
                DeviceIdleController.this.motionLocked();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public boolean registerLocked() {
            boolean success;
            if (DeviceIdleController.this.mMotionSensor.getReportingMode() == 2) {
                success = DeviceIdleController.this.mSensorManager.requestTriggerSensor(DeviceIdleController.this.mMotionListener, DeviceIdleController.this.mMotionSensor);
            } else {
                success = DeviceIdleController.this.mSensorManager.registerListener(DeviceIdleController.this.mMotionListener, DeviceIdleController.this.mMotionSensor, 3);
            }
            if (success) {
                this.active = true;
            } else {
                Slog.e(DeviceIdleController.TAG, "Unable to register for " + DeviceIdleController.this.mMotionSensor);
            }
            return success;
        }

        public void unregisterLocked() {
            if (DeviceIdleController.this.mMotionSensor.getReportingMode() == 2) {
                DeviceIdleController.this.mSensorManager.cancelTriggerSensor(DeviceIdleController.this.mMotionListener, DeviceIdleController.this.mMotionSensor);
            } else {
                DeviceIdleController.this.mSensorManager.unregisterListener(DeviceIdleController.this.mMotionListener);
            }
            this.active = false;
        }
    }

    final class MyHandler extends Handler {
        MyHandler(Looper looper) {
            super(looper);
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v0, resolved type: int} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: int} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v6, resolved type: int} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v16, resolved type: int} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v19, resolved type: boolean} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v21, resolved type: int} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v22, resolved type: int} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v24, resolved type: boolean} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v25, resolved type: int} */
        /* JADX WARNING: Multi-variable type inference failed */
        public void handleMessage(Message msg) {
            boolean lightChanged;
            boolean deepChanged;
            String str;
            if (DeviceIdleController.DEBUG) {
                Slog.d(DeviceIdleController.TAG, "handleMessage(" + msg.what + ")");
            }
            int added = 1;
            int i = 0;
            switch (msg.what) {
                case 1:
                    DeviceIdleController.this.handleWriteConfigFile();
                    return;
                case 2:
                case 3:
                    EventLogTags.writeDeviceIdleOnStart();
                    if (msg.what == 2) {
                        deepChanged = DeviceIdleController.this.mLocalPowerManager.setDeviceIdleMode(true);
                        lightChanged = DeviceIdleController.this.mLocalPowerManager.setLightDeviceIdleMode(false);
                    } else {
                        deepChanged = DeviceIdleController.this.mLocalPowerManager.setDeviceIdleMode(false);
                        lightChanged = DeviceIdleController.this.mLocalPowerManager.setLightDeviceIdleMode(true);
                    }
                    try {
                        DeviceIdleController.this.mNetworkPolicyManager.setDeviceIdleMode(true);
                        IBatteryStats access$900 = DeviceIdleController.this.mBatteryStats;
                        if (msg.what == 2) {
                            added = 2;
                        }
                        access$900.noteDeviceIdleMode(added, null, Process.myUid());
                    } catch (RemoteException e) {
                    }
                    if (deepChanged) {
                        DeviceIdleController.this.getContext().sendBroadcastAsUser(DeviceIdleController.this.mIdleIntent, UserHandle.ALL);
                    }
                    if (lightChanged) {
                        DeviceIdleController.this.getContext().sendBroadcastAsUser(DeviceIdleController.this.mLightIdleIntent, UserHandle.ALL);
                    }
                    EventLogTags.writeDeviceIdleOnComplete();
                    DeviceIdleController.this.mGoingIdleWakeLock.release();
                    return;
                case 4:
                    EventLogTags.writeDeviceIdleOffStart(UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
                    boolean deepChanged2 = DeviceIdleController.this.mLocalPowerManager.setDeviceIdleMode(false);
                    boolean lightChanged2 = DeviceIdleController.this.mLocalPowerManager.setLightDeviceIdleMode(false);
                    try {
                        DeviceIdleController.this.mNetworkPolicyManager.setDeviceIdleMode(false);
                        DeviceIdleController.this.mBatteryStats.noteDeviceIdleMode(0, null, Process.myUid());
                    } catch (RemoteException e2) {
                    }
                    if (deepChanged2) {
                        DeviceIdleController.this.incActiveIdleOps();
                        DeviceIdleController.this.getContext().sendOrderedBroadcastAsUser(DeviceIdleController.this.mIdleIntent, UserHandle.ALL, null, DeviceIdleController.this.mIdleStartedDoneReceiver, null, 0, null, null);
                    }
                    if (lightChanged2) {
                        DeviceIdleController.this.incActiveIdleOps();
                        DeviceIdleController.this.getContext().sendOrderedBroadcastAsUser(DeviceIdleController.this.mLightIdleIntent, UserHandle.ALL, null, DeviceIdleController.this.mIdleStartedDoneReceiver, null, 0, null, null);
                    }
                    DeviceIdleController.this.decActiveIdleOps();
                    EventLogTags.writeDeviceIdleOffComplete();
                    return;
                case 5:
                    String activeReason = (String) msg.obj;
                    int activeUid = msg.arg1;
                    if (activeReason != null) {
                        str = activeReason;
                    } else {
                        str = UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
                    }
                    EventLogTags.writeDeviceIdleOffStart(str);
                    boolean deepChanged3 = DeviceIdleController.this.mLocalPowerManager.setDeviceIdleMode(false);
                    boolean lightChanged3 = DeviceIdleController.this.mLocalPowerManager.setLightDeviceIdleMode(false);
                    try {
                        DeviceIdleController.this.mNetworkPolicyManager.setDeviceIdleMode(false);
                        DeviceIdleController.this.mBatteryStats.noteDeviceIdleMode(0, activeReason, activeUid);
                    } catch (RemoteException e3) {
                    }
                    if (deepChanged3) {
                        DeviceIdleController.this.getContext().sendBroadcastAsUser(DeviceIdleController.this.mIdleIntent, UserHandle.ALL);
                    }
                    if (lightChanged3) {
                        DeviceIdleController.this.getContext().sendBroadcastAsUser(DeviceIdleController.this.mLightIdleIntent, UserHandle.ALL);
                    }
                    EventLogTags.writeDeviceIdleOffComplete();
                    return;
                case 6:
                    DeviceIdleController.this.checkTempAppWhitelistTimeout(msg.arg1);
                    return;
                case 7:
                    if (msg.arg1 != 1) {
                        added = 0;
                    }
                    boolean active = added;
                    int size = DeviceIdleController.this.mMaintenanceActivityListeners.beginBroadcast();
                    while (true) {
                        int i2 = i;
                        if (i2 < size) {
                            try {
                                DeviceIdleController.this.mMaintenanceActivityListeners.getBroadcastItem(i2).onMaintenanceActivityChanged(active);
                            } catch (RemoteException e4) {
                            } catch (Throwable th) {
                                DeviceIdleController.this.mMaintenanceActivityListeners.finishBroadcast();
                                throw th;
                            }
                            i = i2 + 1;
                        } else {
                            DeviceIdleController.this.mMaintenanceActivityListeners.finishBroadcast();
                            return;
                        }
                    }
                case 8:
                    DeviceIdleController.this.decActiveIdleOps();
                    return;
                case 9:
                    int appId = msg.arg1;
                    if (msg.arg2 != 1) {
                        added = 0;
                    }
                    DeviceIdleController.this.mNetworkPolicyManagerInternal.onTempPowerSaveWhitelistChange(appId, added);
                    return;
                default:
                    return;
            }
        }
    }

    class Shell extends ShellCommand {
        int userId = 0;

        Shell() {
        }

        public int onCommand(String cmd) {
            return DeviceIdleController.this.onShellCommand(this, cmd);
        }

        public void onHelp() {
            DeviceIdleController.dumpHelp(getOutPrintWriter());
        }
    }

    private static String stateToString(int state) {
        switch (state) {
            case 0:
                return "ACTIVE";
            case 1:
                return "INACTIVE";
            case 2:
                return "IDLE_PENDING";
            case 3:
                return "SENSING";
            case 4:
                return "LOCATING";
            case 5:
                return "IDLE";
            case 6:
                return "IDLE_MAINTENANCE";
            default:
                return Integer.toString(state);
        }
    }

    private static String lightStateToString(int state) {
        switch (state) {
            case 0:
                return "ACTIVE";
            case 1:
                return "INACTIVE";
            case 3:
                return "PRE_IDLE";
            case 4:
                return "IDLE";
            case 5:
                return "WAITING_FOR_NETWORK";
            case 6:
                return "IDLE_MAINTENANCE";
            case 7:
                return "OVERRIDE";
            default:
                return Integer.toString(state);
        }
    }

    private void addEvent(int cmd, String reason) {
        if (this.mEventCmds[0] != cmd) {
            System.arraycopy(this.mEventCmds, 0, this.mEventCmds, 1, 99);
            System.arraycopy(this.mEventTimes, 0, this.mEventTimes, 1, 99);
            System.arraycopy(this.mEventReasons, 0, this.mEventReasons, 1, 99);
            this.mEventCmds[0] = cmd;
            this.mEventTimes[0] = SystemClock.elapsedRealtime();
            this.mEventReasons[0] = reason;
        }
    }

    public void onAnyMotionResult(int result) {
        if (DEBUG) {
            Slog.d(TAG, "onAnyMotionResult(" + result + ")");
        }
        if (result != -1) {
            synchronized (this) {
                cancelSensingTimeoutAlarmLocked();
            }
        }
        if (result == 1 || result == -1) {
            synchronized (this) {
                handleMotionDetectedLocked(this.mConstants.INACTIVE_TIMEOUT, "non_stationary");
            }
        } else if (result != 0) {
        } else {
            if (this.mState == 3) {
                synchronized (this) {
                    this.mNotMoving = true;
                    stepIdleStateLocked("s:stationary");
                }
            } else if (this.mState == 4) {
                synchronized (this) {
                    this.mNotMoving = true;
                    if (this.mLocated) {
                        stepIdleStateLocked("s:stationary");
                    }
                }
            }
        }
    }

    public DeviceIdleController(Context context) {
        super(context);
        this.mAppStateTracker = new AppStateTracker(context, FgThread.get().getLooper());
        LocalServices.addService(AppStateTracker.class, this.mAppStateTracker);
    }

    /* access modifiers changed from: package-private */
    public boolean isAppOnWhitelistInternal(int appid) {
        boolean z;
        synchronized (this) {
            z = Arrays.binarySearch(this.mPowerSaveWhitelistAllAppIdArray, appid) >= 0;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public int[] getPowerSaveWhitelistUserAppIds() {
        int[] iArr;
        synchronized (this) {
            iArr = this.mPowerSaveWhitelistUserAppIdArray;
        }
        return iArr;
    }

    private static File getSystemDir() {
        return new File(Environment.getDataDirectory(), "system");
    }

    /* JADX WARNING: type inference failed for: r2v3, types: [com.android.server.DeviceIdleController$BinderService, android.os.IBinder] */
    public void onStart() {
        PackageManager pm = getContext().getPackageManager();
        synchronized (this) {
            boolean z = getContext().getResources().getBoolean(17956949);
            this.mDeepEnabled = z;
            this.mLightEnabled = z;
            SystemConfig sysConfig = SystemConfig.getInstance();
            ArraySet<String> allowPowerExceptIdle = sysConfig.getAllowInPowerSaveExceptIdle();
            for (int i = 0; i < allowPowerExceptIdle.size(); i++) {
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(allowPowerExceptIdle.valueAt(i), DumpState.DUMP_DEXOPT);
                    int appid = UserHandle.getAppId(ai.uid);
                    this.mPowerSaveWhitelistAppsExceptIdle.put(ai.packageName, Integer.valueOf(appid));
                    this.mPowerSaveWhitelistSystemAppIdsExceptIdle.put(appid, true);
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
            ArraySet<String> allowPower = sysConfig.getAllowInPowerSave();
            for (int i2 = 0; i2 < allowPower.size(); i2++) {
                try {
                    ApplicationInfo ai2 = pm.getApplicationInfo(allowPower.valueAt(i2), DumpState.DUMP_DEXOPT);
                    int appid2 = UserHandle.getAppId(ai2.uid);
                    this.mPowerSaveWhitelistAppsExceptIdle.put(ai2.packageName, Integer.valueOf(appid2));
                    this.mPowerSaveWhitelistSystemAppIdsExceptIdle.put(appid2, true);
                    this.mPowerSaveWhitelistApps.put(ai2.packageName, Integer.valueOf(appid2));
                    this.mPowerSaveWhitelistSystemAppIds.put(appid2, true);
                } catch (PackageManager.NameNotFoundException e2) {
                }
            }
            this.mConstants = new Constants(this.mHandler, getContext().getContentResolver());
            readConfigFileLocked();
            updateWhitelistAppIdsLocked();
            this.mNetworkConnected = true;
            this.mScreenOn = true;
            this.mScreenLocked = false;
            this.mCharging = true;
            this.mState = 0;
            this.mLightState = 0;
            this.mInactiveTimeout = this.mConstants.INACTIVE_TIMEOUT;
        }
        this.mBinderService = new BinderService();
        publishBinderService("deviceidle", this.mBinderService);
        publishLocalService(LocalService.class, new LocalService());
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            synchronized (this) {
                this.mAlarmManager = (AlarmManager) getContext().getSystemService("alarm");
                this.mBatteryStats = BatteryStatsService.getService();
                this.mLocalActivityManager = (ActivityManagerInternal) getLocalService(ActivityManagerInternal.class);
                this.mLocalPowerManager = (PowerManagerInternal) getLocalService(PowerManagerInternal.class);
                this.mPowerManager = (PowerManager) getContext().getSystemService(PowerManager.class);
                this.mActiveIdleWakeLock = this.mPowerManager.newWakeLock(1, "deviceidle_maint");
                this.mActiveIdleWakeLock.setReferenceCounted(false);
                this.mGoingIdleWakeLock = this.mPowerManager.newWakeLock(1, "deviceidle_going_idle");
                this.mGoingIdleWakeLock.setReferenceCounted(true);
                this.mConnectivityService = (ConnectivityService) ServiceManager.getService("connectivity");
                this.mNetworkPolicyManager = INetworkPolicyManager.Stub.asInterface(ServiceManager.getService("netpolicy"));
                this.mNetworkPolicyManagerInternal = (NetworkPolicyManagerInternal) getLocalService(NetworkPolicyManagerInternal.class);
                this.mSensorManager = (SensorManager) getContext().getSystemService("sensor");
                int sigMotionSensorId = getContext().getResources().getInteger(17694736);
                if (sigMotionSensorId > 0) {
                    this.mMotionSensor = this.mSensorManager.getDefaultSensor(sigMotionSensorId, true);
                }
                if (this.mMotionSensor == null && getContext().getResources().getBoolean(17956892)) {
                    this.mMotionSensor = this.mSensorManager.getDefaultSensor(26, true);
                }
                if (this.mMotionSensor == null) {
                    this.mMotionSensor = this.mSensorManager.getDefaultSensor(17, true);
                }
                if (getContext().getResources().getBoolean(17956893)) {
                    this.mLocationManager = (LocationManager) getContext().getSystemService("location");
                    this.mLocationRequest = new LocationRequest().setQuality(100).setInterval(0).setFastestInterval(0).setNumUpdates(1);
                }
                AnyMotionDetector anyMotionDetector = new AnyMotionDetector((PowerManager) getContext().getSystemService("power"), this.mHandler, this.mSensorManager, this, ((float) getContext().getResources().getInteger(17694737)) / 100.0f);
                this.mAnyMotionDetector = anyMotionDetector;
                this.mAppStateTracker.onSystemServicesReady();
                this.mIdleIntent = new Intent("android.os.action.DEVICE_IDLE_MODE_CHANGED");
                this.mIdleIntent.addFlags(1342177280);
                this.mLightIdleIntent = new Intent("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED");
                this.mLightIdleIntent.addFlags(1342177280);
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.BATTERY_CHANGED");
                getContext().registerReceiver(this.mReceiver, filter);
                IntentFilter filter2 = new IntentFilter();
                filter2.addAction("android.intent.action.PACKAGE_REMOVED");
                filter2.addDataScheme("package");
                getContext().registerReceiver(this.mReceiver, filter2);
                IntentFilter filter3 = new IntentFilter();
                filter3.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                getContext().registerReceiver(this.mReceiver, filter3);
                IntentFilter filter4 = new IntentFilter();
                filter4.addAction("android.intent.action.SCREEN_OFF");
                filter4.addAction("android.intent.action.SCREEN_ON");
                getContext().registerReceiver(this.mInteractivityReceiver, filter4);
                this.mLocalActivityManager.setDeviceIdleWhitelist(this.mPowerSaveWhitelistAllAppIdArray, this.mPowerSaveWhitelistExceptIdleAppIdArray);
                this.mLocalPowerManager.setDeviceIdleWhitelist(this.mPowerSaveWhitelistAllAppIdArray);
                this.mLocalActivityManager.registerScreenObserver(this.mScreenObserver);
                passWhiteListsToForceAppStandbyTrackerLocked();
                updateInteractivityLocked();
            }
            updateConnectivityState(null);
        } else if (phase == 1000) {
            Slog.d(TAG, "PHASE_BOOT_COMPLETED");
            this.mHandler.postDelayed(new Runnable() {
                private static final int MAX_TRY_TIMES = 3;
                private int count = 0;

                public void run() {
                    boolean ignoreDbNotExist = true;
                    this.count++;
                    if (this.count < 3) {
                        ignoreDbNotExist = false;
                    }
                    if (DeviceIdleController.this.updateWhitelistFromDB(ignoreDbNotExist)) {
                        synchronized (this) {
                            DeviceIdleController.this.writeConfigFileLocked();
                        }
                    } else if (this.count < 3) {
                        DeviceIdleController.this.mHandler.postDelayed(this, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                    }
                }
            }, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        }
    }

    public boolean addPowerSaveWhitelistAppInternal(String name) {
        synchronized (this) {
            try {
                if (this.mPowerSaveWhitelistUserApps.put(name, Integer.valueOf(UserHandle.getAppId(getContext().getPackageManager().getApplicationInfo(name, DumpState.DUMP_CHANGES).uid))) == null) {
                    reportPowerSaveWhitelistChangedLocked();
                    updateWhitelistAppIdsLocked();
                    writeConfigFileLocked();
                }
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return true;
    }

    public boolean removePowerSaveWhitelistAppInternal(String name) {
        synchronized (this) {
            if (this.mPowerSaveWhitelistUserApps.remove(name) == null) {
                return false;
            }
            reportPowerSaveWhitelistChangedLocked();
            updateWhitelistAppIdsLocked();
            writeConfigFileLocked();
            return true;
        }
    }

    public boolean getPowerSaveWhitelistAppInternal(String name) {
        boolean containsKey;
        synchronized (this) {
            containsKey = this.mPowerSaveWhitelistUserApps.containsKey(name);
        }
        return containsKey;
    }

    /* access modifiers changed from: package-private */
    public void resetSystemPowerWhitelistInternal() {
        synchronized (this) {
            this.mPowerSaveWhitelistApps.putAll(this.mRemovedFromSystemWhitelistApps);
            this.mRemovedFromSystemWhitelistApps.clear();
            reportPowerSaveWhitelistChangedLocked();
            updateWhitelistAppIdsLocked();
            writeConfigFileLocked();
        }
    }

    public boolean restoreSystemPowerWhitelistAppInternal(String name) {
        synchronized (this) {
            if (!this.mRemovedFromSystemWhitelistApps.containsKey(name)) {
                return false;
            }
            this.mPowerSaveWhitelistApps.put(name, this.mRemovedFromSystemWhitelistApps.remove(name));
            reportPowerSaveWhitelistChangedLocked();
            updateWhitelistAppIdsLocked();
            writeConfigFileLocked();
            return true;
        }
    }

    public boolean removeSystemPowerWhitelistAppInternal(String name) {
        synchronized (this) {
            if (!this.mPowerSaveWhitelistApps.containsKey(name)) {
                return false;
            }
            this.mRemovedFromSystemWhitelistApps.put(name, this.mPowerSaveWhitelistApps.remove(name));
            reportPowerSaveWhitelistChangedLocked();
            updateWhitelistAppIdsLocked();
            writeConfigFileLocked();
            return true;
        }
    }

    public boolean addPowerSaveWhitelistExceptIdleInternal(String name) {
        synchronized (this) {
            try {
                if (this.mPowerSaveWhitelistAppsExceptIdle.put(name, Integer.valueOf(UserHandle.getAppId(getContext().getPackageManager().getApplicationInfo(name, DumpState.DUMP_CHANGES).uid))) == null) {
                    this.mPowerSaveWhitelistUserAppsExceptIdle.add(name);
                    reportPowerSaveWhitelistChangedLocked();
                    this.mPowerSaveWhitelistExceptIdleAppIdArray = buildAppIdArray(this.mPowerSaveWhitelistAppsExceptIdle, this.mPowerSaveWhitelistUserApps, this.mPowerSaveWhitelistExceptIdleAppIds);
                    passWhiteListsToForceAppStandbyTrackerLocked();
                }
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return true;
    }

    public void resetPowerSaveWhitelistExceptIdleInternal() {
        synchronized (this) {
            if (this.mPowerSaveWhitelistAppsExceptIdle.removeAll(this.mPowerSaveWhitelistUserAppsExceptIdle)) {
                reportPowerSaveWhitelistChangedLocked();
                this.mPowerSaveWhitelistExceptIdleAppIdArray = buildAppIdArray(this.mPowerSaveWhitelistAppsExceptIdle, this.mPowerSaveWhitelistUserApps, this.mPowerSaveWhitelistExceptIdleAppIds);
                this.mPowerSaveWhitelistUserAppsExceptIdle.clear();
                passWhiteListsToForceAppStandbyTrackerLocked();
            }
        }
    }

    public boolean getPowerSaveWhitelistExceptIdleInternal(String name) {
        boolean containsKey;
        synchronized (this) {
            containsKey = this.mPowerSaveWhitelistAppsExceptIdle.containsKey(name);
        }
        return containsKey;
    }

    public String[] getSystemPowerWhitelistExceptIdleInternal() {
        String[] apps;
        synchronized (this) {
            int size = this.mPowerSaveWhitelistAppsExceptIdle.size();
            apps = new String[size];
            for (int i = 0; i < size; i++) {
                apps[i] = this.mPowerSaveWhitelistAppsExceptIdle.keyAt(i);
            }
        }
        return apps;
    }

    public String[] getSystemPowerWhitelistInternal() {
        String[] apps;
        synchronized (this) {
            int size = this.mPowerSaveWhitelistApps.size();
            apps = new String[size];
            for (int i = 0; i < size; i++) {
                apps[i] = this.mPowerSaveWhitelistApps.keyAt(i);
            }
        }
        return apps;
    }

    public String[] getRemovedSystemPowerWhitelistAppsInternal() {
        String[] apps;
        synchronized (this) {
            int size = this.mRemovedFromSystemWhitelistApps.size();
            apps = new String[size];
            for (int i = 0; i < size; i++) {
                apps[i] = this.mRemovedFromSystemWhitelistApps.keyAt(i);
            }
        }
        return apps;
    }

    public String[] getUserPowerWhitelistInternal() {
        String[] apps;
        synchronized (this) {
            apps = new String[this.mPowerSaveWhitelistUserApps.size()];
            for (int i = 0; i < this.mPowerSaveWhitelistUserApps.size(); i++) {
                apps[i] = this.mPowerSaveWhitelistUserApps.keyAt(i);
            }
        }
        return apps;
    }

    public String[] getFullPowerWhitelistExceptIdleInternal() {
        String[] apps;
        synchronized (this) {
            apps = new String[(this.mPowerSaveWhitelistAppsExceptIdle.size() + this.mPowerSaveWhitelistUserApps.size())];
            int i = 0;
            int cur = 0;
            for (int i2 = 0; i2 < this.mPowerSaveWhitelistAppsExceptIdle.size(); i2++) {
                apps[cur] = this.mPowerSaveWhitelistAppsExceptIdle.keyAt(i2);
                cur++;
            }
            while (true) {
                int i3 = i;
                if (i3 < this.mPowerSaveWhitelistUserApps.size()) {
                    apps[cur] = this.mPowerSaveWhitelistUserApps.keyAt(i3);
                    cur++;
                    i = i3 + 1;
                }
            }
        }
        return apps;
    }

    public String[] getFullPowerWhitelistInternal() {
        String[] apps;
        synchronized (this) {
            apps = new String[(this.mPowerSaveWhitelistApps.size() + this.mPowerSaveWhitelistUserApps.size())];
            int i = 0;
            int cur = 0;
            for (int i2 = 0; i2 < this.mPowerSaveWhitelistApps.size(); i2++) {
                apps[cur] = this.mPowerSaveWhitelistApps.keyAt(i2);
                cur++;
            }
            while (true) {
                int i3 = i;
                if (i3 < this.mPowerSaveWhitelistUserApps.size()) {
                    apps[cur] = this.mPowerSaveWhitelistUserApps.keyAt(i3);
                    cur++;
                    i = i3 + 1;
                }
            }
        }
        return apps;
    }

    public boolean isPowerSaveWhitelistExceptIdleAppInternal(String packageName) {
        boolean z;
        synchronized (this) {
            if (!this.mPowerSaveWhitelistAppsExceptIdle.containsKey(packageName)) {
                if (!this.mPowerSaveWhitelistUserApps.containsKey(packageName)) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    public boolean isPowerSaveWhitelistAppInternal(String packageName) {
        boolean z;
        synchronized (this) {
            if (!this.mPowerSaveWhitelistApps.containsKey(packageName)) {
                if (!this.mPowerSaveWhitelistUserApps.containsKey(packageName)) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    public int[] getAppIdWhitelistExceptIdleInternal() {
        int[] iArr;
        synchronized (this) {
            iArr = this.mPowerSaveWhitelistExceptIdleAppIdArray;
        }
        return iArr;
    }

    public int[] getAppIdWhitelistInternal() {
        int[] iArr;
        synchronized (this) {
            iArr = this.mPowerSaveWhitelistAllAppIdArray;
        }
        return iArr;
    }

    public int[] getAppIdUserWhitelistInternal() {
        int[] iArr;
        synchronized (this) {
            iArr = this.mPowerSaveWhitelistUserAppIdArray;
        }
        return iArr;
    }

    public int[] getAppIdTempWhitelistInternal() {
        int[] iArr;
        synchronized (this) {
            iArr = this.mTempWhitelistAppIdArray;
        }
        return iArr;
    }

    /* access modifiers changed from: package-private */
    public void addPowerSaveTempWhitelistAppChecked(String packageName, long duration, int userId, String reason) throws RemoteException {
        getContext().enforceCallingPermission("android.permission.CHANGE_DEVICE_IDLE_TEMP_WHITELIST", "No permission to change device idle whitelist");
        int callingUid = Binder.getCallingUid();
        int userId2 = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), callingUid, userId, false, false, "addPowerSaveTempWhitelistApp", null);
        long token = Binder.clearCallingIdentity();
        try {
            addPowerSaveTempWhitelistAppInternal(callingUid, packageName, duration, userId2, true, reason);
            Binder.restoreCallingIdentity(token);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void removePowerSaveTempWhitelistAppChecked(String packageName, int userId) throws RemoteException {
        getContext().enforceCallingPermission("android.permission.CHANGE_DEVICE_IDLE_TEMP_WHITELIST", "No permission to change device idle whitelist");
        int userId2 = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, "removePowerSaveTempWhitelistApp", null);
        long token = Binder.clearCallingIdentity();
        try {
            removePowerSaveTempWhitelistAppInternal(packageName, userId2);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* access modifiers changed from: package-private */
    public void addPowerSaveTempWhitelistAppInternal(int callingUid, String packageName, long duration, int userId, boolean sync, String reason) {
        try {
            addPowerSaveTempWhitelistAppDirectInternal(callingUid, UserHandle.getAppId(getContext().getPackageManager().getPackageUidAsUser(packageName, userId)), duration, sync, reason);
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00c0, code lost:
        if (r6 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00c2, code lost:
        r1.mNetworkPolicyManagerInternal.onTempPowerSaveWhitelistChange(r2, true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:?, code lost:
        return;
     */
    public void addPowerSaveTempWhitelistAppDirectInternal(int callingUid, int appId, long duration, boolean sync, String reason) {
        long j;
        int i = appId;
        String str = reason;
        long timeNow = SystemClock.elapsedRealtime();
        boolean informWhitelistChanged = false;
        synchronized (this) {
            try {
                int callingAppId = UserHandle.getAppId(callingUid);
                if (callingAppId >= 10000) {
                    try {
                        if (!this.mPowerSaveWhitelistSystemAppIds.get(callingAppId)) {
                            throw new SecurityException("Calling app " + UserHandle.formatUid(callingUid) + " is not on whitelist");
                        }
                    } catch (Throwable th) {
                        th = th;
                        long j2 = duration;
                        throw th;
                    }
                }
                j = duration;
                try {
                    long duration2 = Math.min(j, this.mConstants.MAX_TEMP_APP_WHITELIST_DURATION);
                    Pair<MutableLong, String> entry = this.mTempWhitelistAppIdEndTimes.get(i);
                    boolean newEntry = entry == null;
                    if (newEntry) {
                        entry = new Pair<>(new MutableLong(0), str);
                        this.mTempWhitelistAppIdEndTimes.put(i, entry);
                    }
                    ((MutableLong) entry.first).value = timeNow + duration2;
                    if (DEBUG) {
                        Slog.d(TAG, "Adding AppId " + i + " to temp whitelist. New entry: " + newEntry);
                    }
                    if (newEntry) {
                        try {
                            this.mBatteryStats.noteEvent(32785, str, i);
                        } catch (RemoteException e) {
                        }
                        try {
                            postTempActiveTimeoutMessage(i, duration2);
                            updateTempWhitelistAppIdsLocked(i, true);
                            if (sync) {
                                informWhitelistChanged = true;
                            } else {
                                this.mHandler.obtainMessage(9, i, 1).sendToTarget();
                            }
                            reportTempWhitelistChangedLocked();
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    long duration3 = j;
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                j = duration;
                long duration32 = j;
                throw th;
            }
        }
    }

    private void removePowerSaveTempWhitelistAppInternal(String packageName, int userId) {
        try {
            removePowerSaveTempWhitelistAppDirectInternal(UserHandle.getAppId(getContext().getPackageManager().getPackageUidAsUser(packageName, userId)));
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    private void removePowerSaveTempWhitelistAppDirectInternal(int appId) {
        synchronized (this) {
            int idx = this.mTempWhitelistAppIdEndTimes.indexOfKey(appId);
            if (idx >= 0) {
                this.mTempWhitelistAppIdEndTimes.removeAt(idx);
                onAppRemovedFromTempWhitelistLocked(appId, (String) this.mTempWhitelistAppIdEndTimes.valueAt(idx).second);
            }
        }
    }

    private void postTempActiveTimeoutMessage(int uid, long delay) {
        if (DEBUG) {
            Slog.d(TAG, "postTempActiveTimeoutMessage: uid=" + uid + ", delay=" + delay);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6, uid, 0), delay);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x007d, code lost:
        return;
     */
    public void checkTempAppWhitelistTimeout(int uid) {
        long timeNow = SystemClock.elapsedRealtime();
        if (DEBUG) {
            Slog.d(TAG, "checkTempAppWhitelistTimeout: uid=" + uid + ", timeNow=" + timeNow);
        }
        synchronized (this) {
            Pair<MutableLong, String> entry = this.mTempWhitelistAppIdEndTimes.get(uid);
            if (entry != null) {
                if (timeNow >= ((MutableLong) entry.first).value) {
                    this.mTempWhitelistAppIdEndTimes.delete(uid);
                    onAppRemovedFromTempWhitelistLocked(uid, (String) entry.second);
                } else {
                    if (DEBUG) {
                        Slog.d(TAG, "Time to remove UID " + uid + ": " + ((MutableLong) entry.first).value);
                    }
                    postTempActiveTimeoutMessage(uid, ((MutableLong) entry.first).value - timeNow);
                }
            }
        }
    }

    @GuardedBy("this")
    private void onAppRemovedFromTempWhitelistLocked(int appId, String reason) {
        if (DEBUG) {
            Slog.d(TAG, "Removing appId " + appId + " from temp whitelist");
        }
        updateTempWhitelistAppIdsLocked(appId, false);
        this.mHandler.obtainMessage(9, appId, 0).sendToTarget();
        reportTempWhitelistChangedLocked();
        try {
            this.mBatteryStats.noteEvent(16401, reason, appId);
        } catch (RemoteException e) {
        }
    }

    public void exitIdleInternal(String reason) {
        synchronized (this) {
            becomeActiveLocked(reason, Binder.getCallingUid());
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0046, code lost:
        return;
     */
    public void updateConnectivityState(Intent connIntent) {
        ConnectivityService cm;
        boolean conn;
        synchronized (this) {
            cm = this.mConnectivityService;
        }
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            synchronized (this) {
                if (ni == null) {
                    conn = false;
                } else if (connIntent == null) {
                    conn = ni.isConnected();
                } else {
                    if (ni.getType() == connIntent.getIntExtra("networkType", -1)) {
                        conn = !connIntent.getBooleanExtra("noConnectivity", false);
                    } else {
                        return;
                    }
                }
                if (conn != this.mNetworkConnected) {
                    this.mNetworkConnected = conn;
                    if (conn && this.mLightState == 5) {
                        stepLightIdleStateLocked("network");
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateInteractivityLocked() {
        boolean screenOn = this.mPowerManager.isInteractive();
        if (DEBUG) {
            Slog.d(TAG, "updateInteractivityLocked: screenOn=" + screenOn);
        }
        if (!screenOn && this.mScreenOn) {
            this.mScreenOn = false;
            if (!this.mForceIdle) {
                becomeInactiveIfAppropriateLocked();
            }
        } else if (screenOn) {
            this.mScreenOn = true;
            if (this.mForceIdle) {
                return;
            }
            if (!this.mScreenLocked || !this.mConstants.WAIT_FOR_UNLOCK) {
                becomeActiveLocked("screen", Process.myUid());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateChargingLocked(boolean charging) {
        if (DEBUG) {
            Slog.i(TAG, "updateChargingLocked: charging=" + charging);
        }
        if (!charging && this.mCharging) {
            this.mCharging = false;
            if (!this.mForceIdle) {
                becomeInactiveIfAppropriateLocked();
            }
        } else if (charging) {
            this.mCharging = charging;
            if (!this.mForceIdle) {
                becomeActiveLocked("charging", Process.myUid());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void keyguardShowingLocked(boolean showing) {
        if (DEBUG) {
            Slog.i(TAG, "keyguardShowing=" + showing);
        }
        if (this.mScreenLocked != showing) {
            this.mScreenLocked = showing;
            if (this.mScreenOn && !this.mForceIdle && !this.mScreenLocked) {
                becomeActiveLocked("unlocked", Process.myUid());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleReportActiveLocked(String activeReason, int activeUid) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(5, activeUid, 0, activeReason));
    }

    /* access modifiers changed from: package-private */
    public void becomeActiveLocked(String activeReason, int activeUid) {
        if (DEBUG) {
            Slog.i(TAG, "becomeActiveLocked, reason = " + activeReason);
        }
        if (this.mState != 0 || this.mLightState != 0) {
            EventLogTags.writeDeviceIdle(0, activeReason);
            EventLogTags.writeDeviceIdleLight(0, activeReason);
            scheduleReportActiveLocked(activeReason, activeUid);
            this.mState = 0;
            this.mLightState = 0;
            this.mInactiveTimeout = this.mConstants.INACTIVE_TIMEOUT;
            this.mCurIdleBudget = 0;
            this.mMaintenanceStartTime = 0;
            resetIdleManagementLocked();
            resetLightIdleManagementLocked();
            addEvent(1, activeReason);
        }
    }

    /* access modifiers changed from: package-private */
    public void becomeInactiveIfAppropriateLocked() {
        if (DEBUG) {
            Slog.d(TAG, "becomeInactiveIfAppropriateLocked()");
        }
        if ((!this.mScreenOn && !this.mCharging) || this.mForceIdle) {
            if (this.mState == 0 && this.mDeepEnabled) {
                this.mState = 1;
                if (DEBUG) {
                    Slog.d(TAG, "Moved from STATE_ACTIVE to STATE_INACTIVE");
                }
                resetIdleManagementLocked();
                scheduleAlarmLocked(this.mInactiveTimeout, false);
                EventLogTags.writeDeviceIdle(this.mState, "no activity");
            }
            if (this.mLightState == 0 && this.mLightEnabled) {
                this.mLightState = 1;
                if (DEBUG) {
                    Slog.d(TAG, "Moved from LIGHT_STATE_ACTIVE to LIGHT_STATE_INACTIVE");
                }
                resetLightIdleManagementLocked();
                scheduleLightAlarmLocked(this.mConstants.LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT);
                EventLogTags.writeDeviceIdleLight(this.mLightState, "no activity");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void resetIdleManagementLocked() {
        this.mNextIdlePendingDelay = 0;
        this.mNextIdleDelay = 0;
        if (this.mLightState == 0) {
            this.mNextLightIdleDelay = 0;
        }
        cancelAlarmLocked();
        cancelSensingTimeoutAlarmLocked();
        cancelLocatingLocked();
        stopMonitoringMotionLocked();
        this.mAnyMotionDetector.stop();
    }

    /* access modifiers changed from: package-private */
    public void resetLightIdleManagementLocked() {
        cancelLightAlarmLocked();
    }

    /* access modifiers changed from: package-private */
    public void exitForceIdleLocked() {
        if (this.mForceIdle) {
            this.mForceIdle = false;
            if (this.mScreenOn || this.mCharging) {
                becomeActiveLocked("exit-force", Process.myUid());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void stepLightIdleStateLocked(String reason) {
        if (this.mLightState != 7) {
            if (DEBUG) {
                Slog.d(TAG, "stepLightIdleStateLocked: mLightState=" + this.mLightState);
            }
            EventLogTags.writeDeviceIdleLightStep();
            int i = this.mLightState;
            if (i != 1) {
                switch (i) {
                    case 3:
                    case 6:
                        break;
                    case 4:
                    case 5:
                        if (!this.mNetworkConnected && this.mLightState != 5) {
                            scheduleLightAlarmLocked(this.mNextLightIdleDelay);
                            if (DEBUG) {
                                Slog.d(TAG, "Moved to LIGHT_WAITING_FOR_NETWORK.");
                            }
                            this.mLightState = 5;
                            EventLogTags.writeDeviceIdleLight(this.mLightState, reason);
                            break;
                        } else {
                            this.mActiveIdleOpCount = 1;
                            this.mActiveIdleWakeLock.acquire();
                            this.mMaintenanceStartTime = SystemClock.elapsedRealtime();
                            if (this.mCurIdleBudget < this.mConstants.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET) {
                                this.mCurIdleBudget = this.mConstants.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET;
                            } else if (this.mCurIdleBudget > this.mConstants.LIGHT_IDLE_MAINTENANCE_MAX_BUDGET) {
                                this.mCurIdleBudget = this.mConstants.LIGHT_IDLE_MAINTENANCE_MAX_BUDGET;
                            }
                            scheduleLightAlarmLocked(this.mCurIdleBudget);
                            if (DEBUG) {
                                Slog.d(TAG, "Moved from LIGHT_STATE_IDLE to LIGHT_STATE_IDLE_MAINTENANCE.");
                            }
                            this.mLightState = 6;
                            EventLogTags.writeDeviceIdleLight(this.mLightState, reason);
                            addEvent(3, null);
                            this.mHandler.sendEmptyMessage(4);
                            break;
                        }
                }
            } else {
                this.mCurIdleBudget = this.mConstants.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET;
                this.mNextLightIdleDelay = this.mConstants.LIGHT_IDLE_TIMEOUT;
                this.mMaintenanceStartTime = 0;
                if (!isOpsInactiveLocked()) {
                    this.mLightState = 3;
                    EventLogTags.writeDeviceIdleLight(this.mLightState, reason);
                    scheduleLightAlarmLocked(this.mConstants.LIGHT_PRE_IDLE_TIMEOUT);
                }
            }
            if (this.mMaintenanceStartTime != 0) {
                long duration = SystemClock.elapsedRealtime() - this.mMaintenanceStartTime;
                if (duration < this.mConstants.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET) {
                    this.mCurIdleBudget += this.mConstants.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET - duration;
                } else {
                    this.mCurIdleBudget -= duration - this.mConstants.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET;
                }
            }
            this.mMaintenanceStartTime = 0;
            scheduleLightAlarmLocked(this.mNextLightIdleDelay);
            this.mNextLightIdleDelay = Math.min(this.mConstants.LIGHT_MAX_IDLE_TIMEOUT, (long) (((float) this.mNextLightIdleDelay) * this.mConstants.LIGHT_IDLE_FACTOR));
            if (this.mNextLightIdleDelay < this.mConstants.LIGHT_IDLE_TIMEOUT) {
                this.mNextLightIdleDelay = this.mConstants.LIGHT_IDLE_TIMEOUT;
            }
            if (DEBUG) {
                Slog.d(TAG, "Moved to LIGHT_STATE_IDLE.");
            }
            this.mLightState = 4;
            EventLogTags.writeDeviceIdleLight(this.mLightState, reason);
            addEvent(2, null);
            this.mGoingIdleWakeLock.acquire();
            this.mHandler.sendEmptyMessage(3);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0139, code lost:
        if (r0.mLocating != false) goto L_0x0228;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x013d, code lost:
        cancelAlarmLocked();
        cancelLocatingLocked();
        r0.mAnyMotionDetector.stop();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0148, code lost:
        scheduleAlarmLocked(r0.mNextIdleDelay, true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x014f, code lost:
        if (DEBUG == false) goto L_0x016e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0151, code lost:
        android.util.Slog.d(TAG, "Moved to STATE_IDLE. Next alarm in " + r0.mNextIdleDelay + " ms.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x016e, code lost:
        r0.mNextIdleDelay = (long) (((float) r0.mNextIdleDelay) * r0.mConstants.IDLE_FACTOR);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x017b, code lost:
        if (DEBUG == false) goto L_0x0195;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x017d, code lost:
        android.util.Slog.d(TAG, "Setting mNextIdleDelay = " + r0.mNextIdleDelay);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0195, code lost:
        r0.mNextIdleDelay = java.lang.Math.min(r0.mNextIdleDelay, r0.mConstants.MAX_IDLE_TIMEOUT);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x01a9, code lost:
        if (r0.mNextIdleDelay >= r0.mConstants.IDLE_TIMEOUT) goto L_0x01b1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x01ab, code lost:
        r0.mNextIdleDelay = r0.mConstants.IDLE_TIMEOUT;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x01b1, code lost:
        r0.mState = 5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x01b6, code lost:
        if (r0.mLightState == 7) goto L_0x01bd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x01b8, code lost:
        r0.mLightState = 7;
        cancelLightAlarmLocked();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x01bd, code lost:
        com.android.server.EventLogTags.writeDeviceIdle(r0.mState, r1);
        addEvent(4, null);
        r0.mGoingIdleWakeLock.acquire();
        r0.mHandler.sendEmptyMessage(2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0228, code lost:
        return;
     */
    public void stepIdleStateLocked(String reason) {
        String str = reason;
        if (DEBUG) {
            Slog.d(TAG, "stepIdleStateLocked: mState=" + this.mState);
        }
        EventLogTags.writeDeviceIdleStep();
        if (this.mConstants.MIN_TIME_TO_ALARM + SystemClock.elapsedRealtime() <= this.mAlarmManager.getNextWakeFromIdleTime()) {
            switch (this.mState) {
                case 1:
                    startMonitoringMotionLocked();
                    scheduleAlarmLocked(this.mConstants.IDLE_AFTER_INACTIVE_TIMEOUT, false);
                    this.mNextIdlePendingDelay = this.mConstants.IDLE_PENDING_TIMEOUT;
                    this.mNextIdleDelay = this.mConstants.IDLE_TIMEOUT;
                    this.mState = 2;
                    if (DEBUG) {
                        Slog.d(TAG, "Moved from STATE_INACTIVE to STATE_IDLE_PENDING.");
                    }
                    EventLogTags.writeDeviceIdle(this.mState, str);
                    break;
                case 2:
                    this.mState = 3;
                    if (DEBUG) {
                        Slog.d(TAG, "Moved from STATE_IDLE_PENDING to STATE_SENSING.");
                    }
                    EventLogTags.writeDeviceIdle(this.mState, str);
                    scheduleSensingTimeoutAlarmLocked(this.mConstants.SENSING_TIMEOUT);
                    cancelLocatingLocked();
                    this.mNotMoving = false;
                    this.mLocated = false;
                    this.mLastGenericLocation = null;
                    this.mLastGpsLocation = null;
                    if (!this.mForceIdle) {
                        this.mAnyMotionDetector.checkForAnyMotion();
                        break;
                    }
                    break;
                case 3:
                    cancelSensingTimeoutAlarmLocked();
                    this.mState = 4;
                    if (DEBUG) {
                        Slog.d(TAG, "Moved from STATE_SENSING to STATE_LOCATING.");
                    }
                    EventLogTags.writeDeviceIdle(this.mState, str);
                    scheduleAlarmLocked(this.mConstants.LOCATING_TIMEOUT, false);
                    if (this.mForceIdle) {
                        if (DEBUG) {
                            Slog.d(TAG, "forceidle, not check locating");
                            break;
                        }
                    } else {
                        if (this.mLocationManager == null || this.mLocationManager.getProvider("network") == null) {
                            this.mHasNetworkLocation = false;
                        } else {
                            this.mLocationManager.requestLocationUpdates(this.mLocationRequest, this.mGenericLocationListener, this.mHandler.getLooper());
                            this.mLocating = true;
                        }
                        if (this.mLocationManager == null || this.mLocationManager.getProvider("gps") == null) {
                            this.mHasGps = false;
                        } else {
                            this.mHasGps = true;
                            this.mLocationManager.requestLocationUpdates("gps", 1000, 5.0f, this.mGpsLocationListener, this.mHandler.getLooper());
                            this.mLocating = true;
                        }
                        break;
                    }
                    break;
                case 4:
                    break;
                case 5:
                    this.mActiveIdleOpCount = 1;
                    this.mActiveIdleWakeLock.acquire();
                    scheduleAlarmLocked(this.mNextIdlePendingDelay, false);
                    if (DEBUG) {
                        Slog.d(TAG, "Moved from STATE_IDLE to STATE_IDLE_MAINTENANCE. Next alarm in " + this.mNextIdlePendingDelay + " ms.");
                    }
                    this.mMaintenanceStartTime = SystemClock.elapsedRealtime();
                    this.mNextIdlePendingDelay = Math.min(this.mConstants.MAX_IDLE_PENDING_TIMEOUT, (long) (((float) this.mNextIdlePendingDelay) * this.mConstants.IDLE_PENDING_FACTOR));
                    if (this.mNextIdlePendingDelay < this.mConstants.IDLE_PENDING_TIMEOUT) {
                        this.mNextIdlePendingDelay = this.mConstants.IDLE_PENDING_TIMEOUT;
                    }
                    this.mState = 6;
                    EventLogTags.writeDeviceIdle(this.mState, str);
                    addEvent(5, null);
                    this.mHandler.sendEmptyMessage(4);
                    break;
                case 6:
                    break;
            }
        } else {
            if (this.mState != 0) {
                becomeActiveLocked("alarm", Process.myUid());
                becomeInactiveIfAppropriateLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void incActiveIdleOps() {
        synchronized (this) {
            this.mActiveIdleOpCount++;
        }
    }

    /* access modifiers changed from: package-private */
    public void decActiveIdleOps() {
        synchronized (this) {
            this.mActiveIdleOpCount--;
            if (this.mActiveIdleOpCount <= 0) {
                exitMaintenanceEarlyIfNeededLocked();
                this.mActiveIdleWakeLock.release();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setJobsActive(boolean active) {
        synchronized (this) {
            this.mJobsActive = active;
            reportMaintenanceActivityIfNeededLocked();
            if (!active) {
                exitMaintenanceEarlyIfNeededLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setAlarmsActive(boolean active) {
        synchronized (this) {
            this.mAlarmsActive = active;
            if (!active) {
                exitMaintenanceEarlyIfNeededLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean registerMaintenanceActivityListener(IMaintenanceActivityListener listener) {
        boolean z;
        synchronized (this) {
            this.mMaintenanceActivityListeners.register(listener);
            z = this.mReportedMaintenanceActivity;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void unregisterMaintenanceActivityListener(IMaintenanceActivityListener listener) {
        synchronized (this) {
            this.mMaintenanceActivityListeners.unregister(listener);
        }
    }

    /* access modifiers changed from: package-private */
    public void reportMaintenanceActivityIfNeededLocked() {
        boolean active = this.mJobsActive;
        if (active != this.mReportedMaintenanceActivity) {
            this.mReportedMaintenanceActivity = active;
            this.mHandler.sendMessage(this.mHandler.obtainMessage(7, this.mReportedMaintenanceActivity ? 1 : 0, 0));
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isOpsInactiveLocked() {
        return this.mActiveIdleOpCount <= 0 && !this.mJobsActive && !this.mAlarmsActive;
    }

    /* access modifiers changed from: package-private */
    public void exitMaintenanceEarlyIfNeededLocked() {
        if ((this.mState == 6 || this.mLightState == 6 || this.mLightState == 3) && isOpsInactiveLocked()) {
            long now = SystemClock.elapsedRealtime();
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("Exit: start=");
                TimeUtils.formatDuration(this.mMaintenanceStartTime, sb);
                sb.append(" now=");
                TimeUtils.formatDuration(now, sb);
                Slog.d(TAG, sb.toString());
            }
            if (this.mState == 6) {
                stepIdleStateLocked("s:early");
            } else if (this.mLightState == 3) {
                stepLightIdleStateLocked("s:predone");
            } else {
                stepLightIdleStateLocked("s:early");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void motionLocked() {
        if (DEBUG) {
            Slog.d(TAG, "motionLocked()");
        }
        handleMotionDetectedLocked(this.mConstants.MOTION_INACTIVE_TIMEOUT, "motion");
    }

    /* access modifiers changed from: package-private */
    public void handleMotionDetectedLocked(long timeout, String type) {
        boolean becomeInactive = false;
        if (this.mState != 0) {
            if (!(this.mLightState == 4 || this.mLightState == 5 || this.mLightState == 6)) {
                scheduleReportActiveLocked(type, Process.myUid());
                addEvent(1, type);
            }
            this.mState = 0;
            this.mInactiveTimeout = timeout;
            this.mCurIdleBudget = 0;
            this.mMaintenanceStartTime = 0;
            EventLogTags.writeDeviceIdle(this.mState, type);
            becomeInactive = true;
        }
        if (this.mLightState == 7) {
            this.mLightState = 0;
            EventLogTags.writeDeviceIdleLight(this.mLightState, type);
            becomeInactive = true;
        }
        if (becomeInactive) {
            becomeInactiveIfAppropriateLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void receivedGenericLocationLocked(Location location) {
        if (this.mState != 4) {
            cancelLocatingLocked();
            return;
        }
        if (DEBUG) {
            Slog.d(TAG, "Generic location: ");
        }
        this.mLastGenericLocation = new Location(location);
        if (location.getAccuracy() <= this.mConstants.LOCATION_ACCURACY || !this.mHasGps) {
            this.mLocated = true;
            if (this.mNotMoving) {
                stepIdleStateLocked("s:location");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void receivedGpsLocationLocked(Location location) {
        if (this.mState != 4) {
            cancelLocatingLocked();
            return;
        }
        if (DEBUG) {
            Slog.d(TAG, "GPS location: ");
        }
        this.mLastGpsLocation = new Location(location);
        if (location.getAccuracy() <= this.mConstants.LOCATION_ACCURACY) {
            this.mLocated = true;
            if (this.mNotMoving) {
                stepIdleStateLocked("s:gps");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void startMonitoringMotionLocked() {
        if (DEBUG) {
            Slog.d(TAG, "startMonitoringMotionLocked()");
        }
        if (this.mMotionSensor != null && !this.mMotionListener.active) {
            this.mMotionListener.registerLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void stopMonitoringMotionLocked() {
        if (DEBUG) {
            Slog.d(TAG, "stopMonitoringMotionLocked()");
        }
        if (this.mMotionSensor != null && this.mMotionListener.active) {
            this.mMotionListener.unregisterLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelAlarmLocked() {
        if (this.mNextAlarmTime != 0) {
            this.mNextAlarmTime = 0;
            this.mAlarmManager.cancel(this.mDeepAlarmListener);
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelLightAlarmLocked() {
        if (this.mNextLightAlarmTime != 0) {
            this.mNextLightAlarmTime = 0;
            this.mAlarmManager.cancel(this.mLightAlarmListener);
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelLocatingLocked() {
        if (this.mLocating) {
            this.mLocationManager.removeUpdates(this.mGenericLocationListener);
            this.mLocationManager.removeUpdates(this.mGpsLocationListener);
            this.mLocating = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelSensingTimeoutAlarmLocked() {
        if (this.mNextSensingTimeoutAlarmTime != 0) {
            this.mNextSensingTimeoutAlarmTime = 0;
            this.mAlarmManager.cancel(this.mSensingTimeoutAlarmListener);
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleAlarmLocked(long delay, boolean idleUntil) {
        long j = delay;
        boolean z = idleUntil;
        if (DEBUG) {
            Slog.d(TAG, "scheduleAlarmLocked(" + j + ", " + z + ")");
        }
        if (this.mMotionSensor != null) {
            this.mNextAlarmTime = SystemClock.elapsedRealtime() + j;
            if (z) {
                this.mAlarmManager.setIdleUntil(2, this.mNextAlarmTime, "DeviceIdleController.deep", this.mDeepAlarmListener, this.mHandler);
            } else {
                this.mAlarmManager.set(2, this.mNextAlarmTime, "DeviceIdleController.deep", this.mDeepAlarmListener, this.mHandler);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleLightAlarmLocked(long delay) {
        if (DEBUG) {
            Slog.d(TAG, "scheduleLightAlarmLocked(" + delay + ")");
        }
        this.mNextLightAlarmTime = SystemClock.elapsedRealtime() + delay;
        this.mAlarmManager.set(2, this.mNextLightAlarmTime, "DeviceIdleController.light", this.mLightAlarmListener, this.mHandler);
    }

    /* access modifiers changed from: package-private */
    public void scheduleSensingTimeoutAlarmLocked(long delay) {
        if (DEBUG) {
            Slog.d(TAG, "scheduleSensingAlarmLocked(" + delay + ")");
        }
        this.mNextSensingTimeoutAlarmTime = SystemClock.elapsedRealtime() + delay;
        this.mAlarmManager.set(2, this.mNextSensingTimeoutAlarmTime, "DeviceIdleController.sensing", this.mSensingTimeoutAlarmListener, this.mHandler);
    }

    private static int[] buildAppIdArray(ArrayMap<String, Integer> systemApps, ArrayMap<String, Integer> userApps, SparseBooleanArray outAppIds) {
        outAppIds.clear();
        if (systemApps != null) {
            for (int i = 0; i < systemApps.size(); i++) {
                outAppIds.put(systemApps.valueAt(i).intValue(), true);
            }
        }
        if (userApps != null) {
            for (int i2 = 0; i2 < userApps.size(); i2++) {
                outAppIds.put(userApps.valueAt(i2).intValue(), true);
            }
        }
        int size = outAppIds.size();
        int[] appids = new int[size];
        for (int i3 = 0; i3 < size; i3++) {
            appids[i3] = outAppIds.keyAt(i3);
        }
        return appids;
    }

    private void updateWhitelistAppIdsLocked() {
        this.mPowerSaveWhitelistExceptIdleAppIdArray = buildAppIdArray(this.mPowerSaveWhitelistAppsExceptIdle, this.mPowerSaveWhitelistUserApps, this.mPowerSaveWhitelistExceptIdleAppIds);
        this.mPowerSaveWhitelistAllAppIdArray = buildAppIdArray(this.mPowerSaveWhitelistApps, this.mPowerSaveWhitelistUserApps, this.mPowerSaveWhitelistAllAppIds);
        this.mPowerSaveWhitelistUserAppIdArray = buildAppIdArray(null, this.mPowerSaveWhitelistUserApps, this.mPowerSaveWhitelistUserAppIds);
        if (this.mLocalActivityManager != null) {
            this.mLocalActivityManager.setDeviceIdleWhitelist(this.mPowerSaveWhitelistAllAppIdArray, this.mPowerSaveWhitelistExceptIdleAppIdArray);
        }
        if (this.mLocalPowerManager != null) {
            if (DEBUG) {
                Slog.d(TAG, "Setting wakelock whitelist to " + Arrays.toString(this.mPowerSaveWhitelistAllAppIdArray));
            }
            this.mLocalPowerManager.setDeviceIdleWhitelist(this.mPowerSaveWhitelistAllAppIdArray);
        }
        passWhiteListsToForceAppStandbyTrackerLocked();
    }

    private void updateTempWhitelistAppIdsLocked(int appId, boolean adding) {
        int size = this.mTempWhitelistAppIdEndTimes.size();
        if (this.mTempWhitelistAppIdArray.length != size) {
            this.mTempWhitelistAppIdArray = new int[size];
        }
        for (int i = 0; i < size; i++) {
            this.mTempWhitelistAppIdArray[i] = this.mTempWhitelistAppIdEndTimes.keyAt(i);
        }
        if (this.mLocalActivityManager != null) {
            if (DEBUG) {
                Slog.d(TAG, "Setting activity manager temp whitelist to " + Arrays.toString(this.mTempWhitelistAppIdArray));
            }
            this.mLocalActivityManager.updateDeviceIdleTempWhitelist(this.mTempWhitelistAppIdArray, appId, adding);
        }
        if (this.mLocalPowerManager != null) {
            if (DEBUG) {
                Slog.d(TAG, "Setting wakelock temp whitelist to " + Arrays.toString(this.mTempWhitelistAppIdArray));
            }
            this.mLocalPowerManager.setDeviceIdleTempWhitelist(this.mTempWhitelistAppIdArray);
        }
        passWhiteListsToForceAppStandbyTrackerLocked();
    }

    private void reportPowerSaveWhitelistChangedLocked() {
        Intent intent = new Intent("android.os.action.POWER_SAVE_WHITELIST_CHANGED");
        intent.addFlags(1073741824);
        getContext().sendBroadcastAsUser(intent, UserHandle.SYSTEM);
    }

    private void reportTempWhitelistChangedLocked() {
        Intent intent = new Intent("android.os.action.POWER_SAVE_TEMP_WHITELIST_CHANGED");
        intent.addFlags(1073741824);
        getContext().sendBroadcastAsUser(intent, UserHandle.SYSTEM);
    }

    private void passWhiteListsToForceAppStandbyTrackerLocked() {
        this.mAppStateTracker.setPowerSaveWhitelistAppIds(this.mPowerSaveWhitelistExceptIdleAppIdArray, this.mPowerSaveWhitelistUserAppIdArray, this.mTempWhitelistAppIdArray);
    }

    /* access modifiers changed from: package-private */
    public void readConfigFileLocked() {
        if (DEBUG) {
            Slog.d(TAG, "Reading config from " + this.mConfigFile.getBaseFile());
        }
        this.mPowerSaveWhitelistUserApps.clear();
        try {
            FileInputStream stream = this.mConfigFile.openRead();
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, StandardCharsets.UTF_8.name());
                readConfigFileLocked(parser);
                try {
                    stream.close();
                } catch (IOException e) {
                }
            } catch (XmlPullParserException e2) {
                stream.close();
            } catch (Throwable th) {
                try {
                    stream.close();
                } catch (IOException e3) {
                }
                throw th;
            }
        } catch (FileNotFoundException e4) {
        }
    }

    /* access modifiers changed from: private */
    public boolean updateWhitelistFromDB(boolean ignoreDbNotExist) {
        PackageManager pm = getContext().getPackageManager();
        Bundle bundle = null;
        ArrayList<String> protectlist = null;
        try {
            Slog.d(TAG, "begin to read protectlist from DB");
            bundle = getContext().getContentResolver().call(Uri.parse("content://com.huawei.android.smartpowerprovider"), "hsm_get_freeze_list", "protect", null);
        } catch (Exception e) {
            Slog.d(TAG, "read protectlist fail:" + e);
            if (!ignoreDbNotExist) {
                return false;
            }
        }
        if (bundle != null) {
            protectlist = bundle.getStringArrayList("frz_protect");
            Slog.d(TAG, "protect list: " + protectlist);
        } else {
            Slog.d(TAG, "read protectlist wrong , Bundle is null");
        }
        HashSet<String> protectPkgsExt = new HashSet<String>() {
            {
                add(PackageManagerService.PLATFORM_PACKAGE_NAME);
                add("com.android.phone");
                add("org.simalliance.openmobileapi.service");
                add("com.android.cellbroadcastreceiver");
                add("com.android.providers.media");
                add("com.android.exchange");
                add("com.android.providers.downloads");
                add("com.facebook.services");
                add("com.google.android.tetheringentitlement");
                add("com.google.android.ims");
            }
        };
        List<PackageInfo> packages = pm.getInstalledPackages(8192);
        synchronized (this) {
            for (int i = 0; i < packages.size(); i++) {
                String pkgName = packages.get(i).packageName;
                if (!this.mPowerSaveWhitelistUserApps.containsKey(pkgName)) {
                    try {
                        ApplicationInfo ai = pm.getApplicationInfo(pkgName, 8192);
                        if ((protectlist != null && protectlist.contains(pkgName)) || protectPkgsExt.contains(pkgName)) {
                            this.mPowerSaveWhitelistUserApps.put(ai.packageName, Integer.valueOf(UserHandle.getAppId(ai.uid)));
                        }
                    } catch (PackageManager.NameNotFoundException e2) {
                        Slog.d(TAG, "NameNotFound: " + pkgName);
                    }
                }
            }
            updateWhitelistAppIdsLocked();
            reportPowerSaveWhitelistChangedLocked();
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x00bc A[Catch:{ IllegalStateException -> 0x013e, NullPointerException -> 0x0126, NumberFormatException -> 0x010e, XmlPullParserException -> 0x00f6, IOException -> 0x00de, IndexOutOfBoundsException -> 0x00c5 }] */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x0016 A[Catch:{ IllegalStateException -> 0x013e, NullPointerException -> 0x0126, NumberFormatException -> 0x010e, XmlPullParserException -> 0x00f6, IOException -> 0x00de, IndexOutOfBoundsException -> 0x00c5 }] */
    private void readConfigFileLocked(XmlPullParser parser) {
        int type;
        PackageManager pm = getContext().getPackageManager();
        while (true) {
            try {
                int next = parser.next();
                type = next;
                if (next == 2 || type == 1) {
                    if (type != 2) {
                        int outerDepth = parser.getDepth();
                        while (true) {
                            int next2 = parser.next();
                            int type2 = next2;
                            if (next2 == 1) {
                                return;
                            }
                            if (type2 != 3 || parser.getDepth() > outerDepth) {
                                if (type2 != 3) {
                                    if (type2 != 4) {
                                        String tagName = parser.getName();
                                        char c = 65535;
                                        int hashCode = tagName.hashCode();
                                        if (hashCode != 3797) {
                                            if (hashCode == 111376009) {
                                                if (tagName.equals("un-wl")) {
                                                    c = 1;
                                                }
                                            }
                                        } else if (tagName.equals("wl")) {
                                            c = 0;
                                        }
                                        switch (c) {
                                            case 0:
                                                String name = parser.getAttributeValue(null, "n");
                                                if (name != null) {
                                                    try {
                                                        ApplicationInfo ai = pm.getApplicationInfo(name, DumpState.DUMP_CHANGES);
                                                        this.mPowerSaveWhitelistUserApps.put(ai.packageName, Integer.valueOf(UserHandle.getAppId(ai.uid)));
                                                        break;
                                                    } catch (PackageManager.NameNotFoundException e) {
                                                        break;
                                                    }
                                                }
                                                break;
                                            case 1:
                                                String packageName = parser.getAttributeValue(null, "n");
                                                if (this.mPowerSaveWhitelistApps.containsKey(packageName)) {
                                                    this.mRemovedFromSystemWhitelistApps.put(packageName, this.mPowerSaveWhitelistApps.remove(packageName));
                                                    break;
                                                }
                                                break;
                                            default:
                                                Slog.w(TAG, "Unknown element under <config>: " + parser.getName());
                                                XmlUtils.skipCurrentTag(parser);
                                                break;
                                        }
                                    }
                                }
                            } else {
                                return;
                            }
                        }
                    } else {
                        throw new IllegalStateException("no start tag found");
                    }
                }
            } catch (IllegalStateException e2) {
                Slog.w(TAG, "Failed parsing config " + e2);
                return;
            } catch (NullPointerException e3) {
                Slog.w(TAG, "Failed parsing config " + e3);
                return;
            } catch (NumberFormatException e4) {
                Slog.w(TAG, "Failed parsing config " + e4);
                return;
            } catch (XmlPullParserException e5) {
                Slog.w(TAG, "Failed parsing config " + e5);
                return;
            } catch (IOException e6) {
                Slog.w(TAG, "Failed parsing config " + e6);
                return;
            } catch (IndexOutOfBoundsException e7) {
                Slog.w(TAG, "Failed parsing config " + e7);
                return;
            }
        }
        if (type != 2) {
        }
    }

    /* access modifiers changed from: package-private */
    public void writeConfigFileLocked() {
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 5000);
    }

    /* access modifiers changed from: package-private */
    public void handleWriteConfigFile() {
        ByteArrayOutputStream memStream = new ByteArrayOutputStream();
        try {
            synchronized (this) {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(memStream, StandardCharsets.UTF_8.name());
                writeConfigFileLocked(out);
            }
        } catch (IOException e) {
        }
        synchronized (this.mConfigFile) {
            FileOutputStream stream = null;
            try {
                stream = this.mConfigFile.startWrite();
                memStream.writeTo(stream);
                stream.flush();
                FileUtils.sync(stream);
                stream.close();
                this.mConfigFile.finishWrite(stream);
            } catch (IOException e2) {
                Slog.w(TAG, "Error writing config file", e2);
                this.mConfigFile.failWrite(stream);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeConfigFileLocked(XmlSerializer out) throws IOException {
        out.startDocument(null, true);
        out.startTag(null, "config");
        for (int i = 0; i < this.mPowerSaveWhitelistUserApps.size(); i++) {
            out.startTag(null, "wl");
            out.attribute(null, "n", this.mPowerSaveWhitelistUserApps.keyAt(i));
            out.endTag(null, "wl");
        }
        for (int i2 = 0; i2 < this.mRemovedFromSystemWhitelistApps.size(); i2++) {
            out.startTag(null, "un-wl");
            out.attribute(null, "n", this.mRemovedFromSystemWhitelistApps.keyAt(i2));
            out.endTag(null, "un-wl");
        }
        out.endTag(null, "config");
        out.endDocument();
    }

    static void dumpHelp(PrintWriter pw) {
        pw.println("Device idle controller (deviceidle) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("  step [light|deep]");
        pw.println("    Immediately step to next state, without waiting for alarm.");
        pw.println("  force-idle [light|deep]");
        pw.println("    Force directly into idle mode, regardless of other device state.");
        pw.println("  force-inactive");
        pw.println("    Force to be inactive, ready to freely step idle states.");
        pw.println("  unforce");
        pw.println("    Resume normal functioning after force-idle or force-inactive.");
        pw.println("  get [light|deep|force|screen|charging|network]");
        pw.println("    Retrieve the current given state.");
        pw.println("  disable [light|deep|all]");
        pw.println("    Completely disable device idle mode.");
        pw.println("  enable [light|deep|all]");
        pw.println("    Re-enable device idle mode after it had previously been disabled.");
        pw.println("  enabled [light|deep|all]");
        pw.println("    Print 1 if device idle mode is currently enabled, else 0.");
        pw.println("  whitelist");
        pw.println("    Print currently whitelisted apps.");
        pw.println("  whitelist [package ...]");
        pw.println("    Add (prefix with +) or remove (prefix with -) packages.");
        pw.println("  sys-whitelist [package ...|reset]");
        pw.println("    Prefix the package with '-' to remove it from the system whitelist or '+' to put it back in the system whitelist.");
        pw.println("    Note that only packages that were earlier removed from the system whitelist can be added back.");
        pw.println("    reset will reset the whitelist to the original state");
        pw.println("    Prints the system whitelist if no arguments are specified");
        pw.println("  except-idle-whitelist [package ...|reset]");
        pw.println("    Prefix the package with '+' to add it to whitelist or '=' to check if it is already whitelisted");
        pw.println("    [reset] will reset the whitelist to it's original state");
        pw.println("    Note that unlike <whitelist> cmd, changes made using this won't be persisted across boots");
        pw.println("  tempwhitelist");
        pw.println("    Print packages that are temporarily whitelisted.");
        pw.println("  tempwhitelist [-u USER] [-d DURATION] [-r] [package]");
        pw.println("    Temporarily place package in whitelist for DURATION milliseconds.");
        pw.println("    If no DURATION is specified, 10 seconds is used");
        pw.println("    If [-r] option is used, then the package is removed from temp whitelist and any [-d] is ignored");
        pw.println("  motion");
        pw.println("    Simulate a motion event to bring the device out of deep doze");
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x024c, code lost:
        r0 = 65535;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x024d, code lost:
        switch(r0) {
            case 0: goto L_0x0275;
            case 1: goto L_0x026b;
            case 2: goto L_0x0265;
            case 3: goto L_0x025f;
            case 4: goto L_0x0259;
            case 5: goto L_0x0253;
            default: goto L_0x0250;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:159:0x0253, code lost:
        r10.println(r7.mNetworkConnected);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:160:0x0259, code lost:
        r10.println(r7.mCharging);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:161:0x025f, code lost:
        r10.println(r7.mScreenOn);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x0265, code lost:
        r10.println(r7.mForceIdle);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x026b, code lost:
        r10.println(stateToString(r7.mState));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x0275, code lost:
        r10.println(lightStateToString(r7.mLightState));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x027f, code lost:
        r10.println("Unknown get option: " + r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:?, code lost:
        android.os.Binder.restoreCallingIdentity(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:332:?, code lost:
        r10.println("Package must be prefixed with +, -, or =: " + r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:333:0x0508, code lost:
        android.os.Binder.restoreCallingIdentity(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:334:0x050c, code lost:
        return -1;
     */
    /* JADX WARNING: Removed duplicated region for block: B:204:0x02fe A[Catch:{ all -> 0x02d7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:206:0x0308 A[Catch:{ all -> 0x02d7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:212:0x0328 A[Catch:{ all -> 0x02d7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:249:0x03a2 A[Catch:{ all -> 0x037b }] */
    /* JADX WARNING: Removed duplicated region for block: B:251:0x03ac A[Catch:{ all -> 0x037b }] */
    /* JADX WARNING: Removed duplicated region for block: B:253:0x03b1 A[Catch:{ all -> 0x037b }] */
    public int onShellCommand(Shell shell, String cmd) {
        String nextArg;
        String nextArg2;
        char c;
        Shell shell2 = shell;
        String str = cmd;
        PrintWriter pw = shell.getOutPrintWriter();
        if ("step".equals(str)) {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            synchronized (this) {
                long token = Binder.clearCallingIdentity();
                String arg = shell.getNextArg();
                if (arg != null) {
                    try {
                        if (!"deep".equals(arg)) {
                            if ("light".equals(arg)) {
                                stepLightIdleStateLocked("s:shell");
                                pw.print("Stepped to light: ");
                                pw.println(lightStateToString(this.mLightState));
                            } else {
                                pw.println("Unknown idle mode: " + arg);
                            }
                            Binder.restoreCallingIdentity(token);
                        }
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(token);
                        throw th;
                    }
                }
                stepIdleStateLocked("s:shell");
                pw.print("Stepped to deep: ");
                pw.println(stateToString(this.mState));
                Binder.restoreCallingIdentity(token);
            }
        } else if ("force-idle".equals(str)) {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            synchronized (this) {
                long token2 = Binder.clearCallingIdentity();
                String arg2 = shell.getNextArg();
                if (arg2 != null) {
                    try {
                        if (!"deep".equals(arg2)) {
                            if ("light".equals(arg2)) {
                                this.mForceIdle = true;
                                becomeInactiveIfAppropriateLocked();
                                for (int curLightState = this.mLightState; curLightState != 4; curLightState = this.mLightState) {
                                    stepLightIdleStateLocked("s:shell");
                                    if (curLightState == this.mLightState) {
                                        pw.print("Unable to go light idle; stopped at ");
                                        pw.println(lightStateToString(this.mLightState));
                                        exitForceIdleLocked();
                                        Binder.restoreCallingIdentity(token2);
                                        return -1;
                                    }
                                }
                                pw.println("Now forced in to light idle mode");
                            } else {
                                pw.println("Unknown idle mode: " + arg2);
                            }
                            Binder.restoreCallingIdentity(token2);
                        }
                    } catch (Throwable th2) {
                        Binder.restoreCallingIdentity(token2);
                        throw th2;
                    }
                }
                if (!this.mDeepEnabled) {
                    pw.println("Unable to go deep idle; not enabled");
                    Binder.restoreCallingIdentity(token2);
                    return -1;
                }
                this.mForceIdle = true;
                becomeInactiveIfAppropriateLocked();
                for (int curState = this.mState; curState != 5; curState = this.mState) {
                    stepIdleStateLocked("s:shell");
                    if (curState == this.mState) {
                        pw.print("Unable to go deep idle; stopped at ");
                        pw.println(stateToString(this.mState));
                        exitForceIdleLocked();
                        Binder.restoreCallingIdentity(token2);
                        return -1;
                    }
                }
                pw.println("Now forced in to deep idle mode");
                Binder.restoreCallingIdentity(token2);
            }
        } else if ("force-inactive".equals(str)) {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            synchronized (this) {
                long token3 = Binder.clearCallingIdentity();
                try {
                    this.mForceIdle = true;
                    becomeInactiveIfAppropriateLocked();
                    pw.print("Light state: ");
                    pw.print(lightStateToString(this.mLightState));
                    pw.print(", deep state: ");
                    pw.println(stateToString(this.mState));
                } finally {
                    Binder.restoreCallingIdentity(token3);
                }
            }
        } else if ("unforce".equals(str)) {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            synchronized (this) {
                long token4 = Binder.clearCallingIdentity();
                try {
                    exitForceIdleLocked();
                    pw.print("Light state: ");
                    pw.print(lightStateToString(this.mLightState));
                    pw.print(", deep state: ");
                    pw.println(stateToString(this.mState));
                } finally {
                    Binder.restoreCallingIdentity(token4);
                }
            }
        } else if ("get".equals(str)) {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            synchronized (this) {
                String arg3 = shell.getNextArg();
                if (arg3 != null) {
                    long token5 = Binder.clearCallingIdentity();
                    try {
                        switch (arg3.hashCode()) {
                            case -907689876:
                                if (arg3.equals("screen")) {
                                    c = 3;
                                    break;
                                }
                            case 3079404:
                                if (arg3.equals("deep")) {
                                    c = 1;
                                    break;
                                }
                            case 97618667:
                                if (arg3.equals("force")) {
                                    c = 2;
                                    break;
                                }
                            case 102970646:
                                if (arg3.equals("light")) {
                                    c = 0;
                                    break;
                                }
                            case 1436115569:
                                if (arg3.equals("charging")) {
                                    c = 4;
                                    break;
                                }
                            case 1843485230:
                                if (arg3.equals("network")) {
                                    c = 5;
                                    break;
                                }
                        }
                    } catch (Throwable th3) {
                        Binder.restoreCallingIdentity(token5);
                        throw th3;
                    }
                } else {
                    pw.println("Argument required");
                }
            }
        } else if ("disable".equals(str)) {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            synchronized (this) {
                long token6 = Binder.clearCallingIdentity();
                String arg4 = shell.getNextArg();
                boolean becomeActive = false;
                boolean valid = false;
                if (arg4 != null) {
                    try {
                        if (!"deep".equals(arg4)) {
                            if ("all".equals(arg4)) {
                            }
                            if (arg4 == null || "light".equals(arg4) || "all".equals(arg4)) {
                                valid = true;
                                if (this.mLightEnabled) {
                                    this.mLightEnabled = false;
                                    becomeActive = true;
                                    pw.println("Light idle mode disabled");
                                }
                            }
                            if (becomeActive) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(arg4 == null ? "all" : arg4);
                                sb.append("-disabled");
                                becomeActiveLocked(sb.toString(), Process.myUid());
                            }
                            if (!valid) {
                                pw.println("Unknown idle mode: " + arg4);
                            }
                            Binder.restoreCallingIdentity(token6);
                        }
                    } catch (Throwable th4) {
                        Binder.restoreCallingIdentity(token6);
                        throw th4;
                    }
                }
                valid = true;
                if (this.mDeepEnabled) {
                    this.mDeepEnabled = false;
                    becomeActive = true;
                    pw.println("Deep idle mode disabled");
                }
                valid = true;
                if (this.mLightEnabled) {
                }
                if (becomeActive) {
                }
                if (!valid) {
                }
                Binder.restoreCallingIdentity(token6);
            }
        } else if ("enable".equals(str)) {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            synchronized (this) {
                long token7 = Binder.clearCallingIdentity();
                String arg5 = shell.getNextArg();
                boolean becomeInactive = false;
                boolean valid2 = false;
                if (arg5 != null) {
                    try {
                        if (!"deep".equals(arg5)) {
                            if ("all".equals(arg5)) {
                            }
                            if (arg5 == null || "light".equals(arg5) || "all".equals(arg5)) {
                                valid2 = true;
                                if (!this.mLightEnabled) {
                                    this.mLightEnabled = true;
                                    becomeInactive = true;
                                    pw.println("Light idle mode enable");
                                }
                            }
                            if (becomeInactive) {
                                becomeInactiveIfAppropriateLocked();
                            }
                            if (!valid2) {
                                pw.println("Unknown idle mode: " + arg5);
                            }
                            Binder.restoreCallingIdentity(token7);
                        }
                    } catch (Throwable th5) {
                        Binder.restoreCallingIdentity(token7);
                        throw th5;
                    }
                }
                valid2 = true;
                if (!this.mDeepEnabled) {
                    this.mDeepEnabled = true;
                    becomeInactive = true;
                    pw.println("Deep idle mode enabled");
                }
                valid2 = true;
                if (!this.mLightEnabled) {
                }
                if (becomeInactive) {
                }
                if (!valid2) {
                }
                Binder.restoreCallingIdentity(token7);
            }
        } else if ("enabled".equals(str)) {
            synchronized (this) {
                String arg6 = shell.getNextArg();
                if (arg6 != null) {
                    if (!"all".equals(arg6)) {
                        if ("deep".equals(arg6)) {
                            pw.println(this.mDeepEnabled ? "1" : 0);
                        } else if ("light".equals(arg6)) {
                            pw.println(this.mLightEnabled ? "1" : 0);
                        } else {
                            pw.println("Unknown idle mode: " + arg6);
                        }
                    }
                }
                pw.println((!this.mDeepEnabled || !this.mLightEnabled) ? 0 : "1");
            }
        } else {
            char c2 = '=';
            if ("whitelist".equals(str)) {
                String arg7 = shell.getNextArg();
                if (arg7 != null) {
                    getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                    long token8 = Binder.clearCallingIdentity();
                    while (true) {
                        long token9 = token8;
                        try {
                            if (arg7.length() >= 1) {
                                if (arg7.charAt(0) != '-' && arg7.charAt(0) != '+' && arg7.charAt(0) != c2) {
                                    break;
                                }
                                char op = arg7.charAt(0);
                                String pkg = arg7.substring(1);
                                if (op == '+') {
                                    if (addPowerSaveWhitelistAppInternal(pkg)) {
                                        pw.println("Added: " + pkg);
                                    } else {
                                        pw.println("Unknown package: " + pkg);
                                    }
                                } else if (op != '-') {
                                    pw.println(getPowerSaveWhitelistAppInternal(pkg));
                                } else if (removePowerSaveWhitelistAppInternal(pkg)) {
                                    pw.println("Removed: " + pkg);
                                }
                                String nextArg3 = shell.getNextArg();
                                arg7 = nextArg3;
                                if (nextArg3 == null) {
                                    break;
                                }
                                token8 = token9;
                                c2 = '=';
                            }
                        } finally {
                            Binder.restoreCallingIdentity(token9);
                        }
                    }
                } else {
                    synchronized (this) {
                        for (int j = 0; j < this.mPowerSaveWhitelistAppsExceptIdle.size(); j++) {
                            pw.print("system-excidle,");
                            pw.print(this.mPowerSaveWhitelistAppsExceptIdle.keyAt(j));
                            pw.print(",");
                            pw.println(this.mPowerSaveWhitelistAppsExceptIdle.valueAt(j));
                        }
                        for (int j2 = 0; j2 < this.mPowerSaveWhitelistApps.size(); j2++) {
                            pw.print("system,");
                            pw.print(this.mPowerSaveWhitelistApps.keyAt(j2));
                            pw.print(",");
                            pw.println(this.mPowerSaveWhitelistApps.valueAt(j2));
                        }
                        for (int j3 = 0; j3 < this.mPowerSaveWhitelistUserApps.size(); j3++) {
                            pw.print("user,");
                            pw.print(this.mPowerSaveWhitelistUserApps.keyAt(j3));
                            pw.print(",");
                            pw.println(this.mPowerSaveWhitelistUserApps.valueAt(j3));
                        }
                    }
                }
            } else if ("tempwhitelist".equals(str)) {
                long duration = 10000;
                boolean removePkg = false;
                while (true) {
                    boolean removePkg2 = removePkg;
                    String nextOption = shell.getNextOption();
                    String opt = nextOption;
                    if (nextOption != null) {
                        if ("-u".equals(opt)) {
                            String opt2 = shell.getNextArg();
                            if (opt2 == null) {
                                pw.println("-u requires a user number");
                                return -1;
                            }
                            shell2.userId = Integer.parseInt(opt2);
                        } else if ("-d".equals(opt)) {
                            String opt3 = shell.getNextArg();
                            if (opt3 == null) {
                                pw.println("-d requires a duration");
                                return -1;
                            }
                            duration = Long.parseLong(opt3);
                        } else if ("-r".equals(opt)) {
                            removePkg = true;
                        }
                        removePkg = removePkg2;
                    } else {
                        String arg8 = shell.getNextArg();
                        if (arg8 == null) {
                            String str2 = opt;
                            if (removePkg2) {
                                pw.println("[-r] requires a package name");
                                return -1;
                            }
                            dumpTempWhitelistSchedule(pw, false);
                        } else if (removePkg2) {
                            try {
                                removePowerSaveTempWhitelistAppChecked(arg8, shell2.userId);
                                String str3 = arg8;
                                String str4 = opt;
                            } catch (Exception e) {
                                e = e;
                                String str5 = arg8;
                                String str6 = opt;
                                pw.println("Failed: " + e);
                                return -1;
                            }
                        } else {
                            try {
                                String str7 = arg8;
                                String str8 = opt;
                                try {
                                    addPowerSaveTempWhitelistAppChecked(arg8, duration, shell2.userId, "shell");
                                } catch (Exception e2) {
                                    e = e2;
                                }
                            } catch (Exception e3) {
                                e = e3;
                                String str9 = arg8;
                                String str10 = opt;
                                pw.println("Failed: " + e);
                                return -1;
                            }
                        }
                    }
                }
            } else if ("except-idle-whitelist".equals(str)) {
                getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                long token10 = Binder.clearCallingIdentity();
                try {
                    String arg9 = shell.getNextArg();
                    if (arg9 == null) {
                        pw.println("No arguments given");
                        return -1;
                    }
                    if ("reset".equals(arg9)) {
                        resetPowerSaveWhitelistExceptIdleInternal();
                    } else {
                        do {
                            if (arg9.length() >= 1) {
                                if (arg9.charAt(0) == '-' || arg9.charAt(0) == '+' || arg9.charAt(0) == '=') {
                                    char op2 = arg9.charAt(0);
                                    String pkg2 = arg9.substring(1);
                                    if (op2 == '+') {
                                        if (addPowerSaveWhitelistExceptIdleInternal(pkg2)) {
                                            pw.println("Added: " + pkg2);
                                        } else {
                                            pw.println("Unknown package: " + pkg2);
                                        }
                                    } else if (op2 == '=') {
                                        pw.println(getPowerSaveWhitelistExceptIdleInternal(pkg2));
                                    } else {
                                        pw.println("Unknown argument: " + arg9);
                                        Binder.restoreCallingIdentity(token10);
                                        return -1;
                                    }
                                    nextArg2 = shell.getNextArg();
                                    arg9 = nextArg2;
                                }
                            }
                            pw.println("Package must be prefixed with +, -, or =: " + arg9);
                            Binder.restoreCallingIdentity(token10);
                            return -1;
                        } while (nextArg2 != null);
                    }
                    Binder.restoreCallingIdentity(token10);
                } finally {
                    Binder.restoreCallingIdentity(token10);
                }
            } else if ("sys-whitelist".equals(str)) {
                String arg10 = shell.getNextArg();
                if (arg10 != null) {
                    getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                    long token11 = Binder.clearCallingIdentity();
                    try {
                        if ("reset".equals(arg10)) {
                            resetSystemPowerWhitelistInternal();
                        } else {
                            do {
                                if (arg10.length() >= 1) {
                                    if (arg10.charAt(0) == '-' || arg10.charAt(0) == '+') {
                                        char op3 = arg10.charAt(0);
                                        String pkg3 = arg10.substring(1);
                                        if (op3 != '+') {
                                            if (op3 == '-') {
                                                if (removeSystemPowerWhitelistAppInternal(pkg3)) {
                                                    pw.println("Removed " + pkg3);
                                                }
                                            }
                                        } else if (restoreSystemPowerWhitelistAppInternal(pkg3)) {
                                            pw.println("Restored " + pkg3);
                                        }
                                        nextArg = shell.getNextArg();
                                        arg10 = nextArg;
                                    }
                                }
                                pw.println("Package must be prefixed with + or - " + arg10);
                                Binder.restoreCallingIdentity(token11);
                                return -1;
                            } while (nextArg != null);
                        }
                    } finally {
                        Binder.restoreCallingIdentity(token11);
                    }
                } else {
                    synchronized (this) {
                        for (int j4 = 0; j4 < this.mPowerSaveWhitelistApps.size(); j4++) {
                            pw.print(this.mPowerSaveWhitelistApps.keyAt(j4));
                            pw.print(",");
                            pw.println(this.mPowerSaveWhitelistApps.valueAt(j4));
                        }
                    }
                }
            } else if (!"motion".equals(str)) {
                return shell.handleDefaultCommands(cmd);
            } else {
                getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                synchronized (this) {
                    long token12 = Binder.clearCallingIdentity();
                    try {
                        motionLocked();
                        pw.print("Light state: ");
                        pw.print(lightStateToString(this.mLightState));
                        pw.print(", deep state: ");
                        pw.println(stateToString(this.mState));
                    } finally {
                        Binder.restoreCallingIdentity(token12);
                    }
                }
            }
        }
        return 0;
    }

    /* JADX WARNING: type inference failed for: r9v9, types: [android.os.Binder, com.android.server.DeviceIdleController$BinderService] */
    /* access modifiers changed from: package-private */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        String label;
        PrintWriter printWriter = pw;
        String[] strArr = args;
        if (DumpUtils.checkDumpPermission(getContext(), TAG, printWriter)) {
            if (strArr != null) {
                int userId = 0;
                int i = 0;
                while (i < strArr.length) {
                    String arg = strArr[i];
                    if ("-h".equals(arg)) {
                        dumpHelp(pw);
                        return;
                    }
                    if ("-u".equals(arg)) {
                        i++;
                        if (i < strArr.length) {
                            userId = Integer.parseInt(strArr[i]);
                        }
                    } else if (!"-a".equals(arg)) {
                        if (arg.length() <= 0 || arg.charAt(0) != '-') {
                            Shell shell = new Shell();
                            shell.userId = userId;
                            String[] newArgs = new String[(strArr.length - i)];
                            System.arraycopy(strArr, i, newArgs, 0, strArr.length - i);
                            String[] strArr2 = newArgs;
                            shell.exec(this.mBinderService, null, fd, null, newArgs, null, new ResultReceiver(null));
                            return;
                        }
                        printWriter.println("Unknown option: " + arg);
                        return;
                    }
                    i++;
                }
            }
            synchronized (this) {
                this.mConstants.dump(printWriter);
                if (this.mEventCmds[0] != 0) {
                    printWriter.println("  Idling history:");
                    long now = SystemClock.elapsedRealtime();
                    for (int i2 = 99; i2 >= 0; i2--) {
                        if (this.mEventCmds[i2] != 0) {
                            switch (this.mEventCmds[i2]) {
                                case 1:
                                    label = "     normal";
                                    break;
                                case 2:
                                    label = " light-idle";
                                    break;
                                case 3:
                                    label = "light-maint";
                                    break;
                                case 4:
                                    label = "  deep-idle";
                                    break;
                                case 5:
                                    label = " deep-maint";
                                    break;
                                default:
                                    label = "         ??";
                                    break;
                            }
                            printWriter.print("    ");
                            printWriter.print(label);
                            printWriter.print(": ");
                            TimeUtils.formatDuration(this.mEventTimes[i2], now, printWriter);
                            if (this.mEventReasons[i2] != null) {
                                printWriter.print(" (");
                                printWriter.print(this.mEventReasons[i2]);
                                printWriter.print(")");
                            }
                            pw.println();
                        }
                    }
                }
                int size = this.mPowerSaveWhitelistAppsExceptIdle.size();
                if (size > 0) {
                    printWriter.println("  Whitelist (except idle) system apps:");
                    for (int i3 = 0; i3 < size; i3++) {
                        printWriter.print("    ");
                        printWriter.println(this.mPowerSaveWhitelistAppsExceptIdle.keyAt(i3));
                    }
                }
                int size2 = this.mPowerSaveWhitelistApps.size();
                if (size2 > 0) {
                    printWriter.println("  Whitelist system apps:");
                    for (int i4 = 0; i4 < size2; i4++) {
                        printWriter.print("    ");
                        printWriter.println(this.mPowerSaveWhitelistApps.keyAt(i4));
                    }
                }
                int size3 = this.mRemovedFromSystemWhitelistApps.size();
                if (size3 > 0) {
                    printWriter.println("  Removed from whitelist system apps:");
                    for (int i5 = 0; i5 < size3; i5++) {
                        printWriter.print("    ");
                        printWriter.println(this.mRemovedFromSystemWhitelistApps.keyAt(i5));
                    }
                }
                int size4 = this.mPowerSaveWhitelistUserApps.size();
                if (size4 > 0) {
                    printWriter.println("  Whitelist user apps:");
                    for (int i6 = 0; i6 < size4; i6++) {
                        printWriter.print("    ");
                        printWriter.println(this.mPowerSaveWhitelistUserApps.keyAt(i6));
                    }
                }
                int size5 = this.mPowerSaveWhitelistExceptIdleAppIds.size();
                if (size5 > 0) {
                    printWriter.println("  Whitelist (except idle) all app ids:");
                    for (int i7 = 0; i7 < size5; i7++) {
                        printWriter.print("    ");
                        printWriter.print(this.mPowerSaveWhitelistExceptIdleAppIds.keyAt(i7));
                        pw.println();
                    }
                }
                int size6 = this.mPowerSaveWhitelistUserAppIds.size();
                if (size6 > 0) {
                    printWriter.println("  Whitelist user app ids:");
                    for (int i8 = 0; i8 < size6; i8++) {
                        printWriter.print("    ");
                        printWriter.print(this.mPowerSaveWhitelistUserAppIds.keyAt(i8));
                        pw.println();
                    }
                }
                int size7 = this.mPowerSaveWhitelistAllAppIds.size();
                if (size7 > 0) {
                    printWriter.println("  Whitelist all app ids:");
                    for (int i9 = 0; i9 < size7; i9++) {
                        printWriter.print("    ");
                        printWriter.print(this.mPowerSaveWhitelistAllAppIds.keyAt(i9));
                        pw.println();
                    }
                }
                dumpTempWhitelistSchedule(printWriter, true);
                int size8 = this.mTempWhitelistAppIdArray != null ? this.mTempWhitelistAppIdArray.length : 0;
                if (size8 > 0) {
                    printWriter.println("  Temp whitelist app ids:");
                    for (int i10 = 0; i10 < size8; i10++) {
                        printWriter.print("    ");
                        printWriter.print(this.mTempWhitelistAppIdArray[i10]);
                        pw.println();
                    }
                }
                printWriter.print("  mLightEnabled=");
                printWriter.print(this.mLightEnabled);
                printWriter.print("  mDeepEnabled=");
                printWriter.println(this.mDeepEnabled);
                printWriter.print("  mForceIdle=");
                printWriter.println(this.mForceIdle);
                printWriter.print("  mMotionSensor=");
                printWriter.println(this.mMotionSensor);
                printWriter.print("  mScreenOn=");
                printWriter.println(this.mScreenOn);
                printWriter.print("  mScreenLocked=");
                printWriter.println(this.mScreenLocked);
                printWriter.print("  mNetworkConnected=");
                printWriter.println(this.mNetworkConnected);
                printWriter.print("  mCharging=");
                printWriter.println(this.mCharging);
                printWriter.print("  mMotionActive=");
                printWriter.println(this.mMotionListener.active);
                printWriter.print("  mNotMoving=");
                printWriter.println(this.mNotMoving);
                printWriter.print("  mLocating=");
                printWriter.print(this.mLocating);
                printWriter.print(" mHasGps=");
                printWriter.print(this.mHasGps);
                printWriter.print(" mHasNetwork=");
                printWriter.print(this.mHasNetworkLocation);
                printWriter.print(" mLocated=");
                printWriter.println(this.mLocated);
                if (this.mLastGenericLocation != null) {
                    printWriter.print("  mLastGenericLocation=");
                    printWriter.println(this.mLastGenericLocation);
                }
                if (this.mLastGpsLocation != null) {
                    printWriter.print("  mLastGpsLocation=");
                    printWriter.println(this.mLastGpsLocation);
                }
                printWriter.print("  mState=");
                printWriter.print(stateToString(this.mState));
                printWriter.print(" mLightState=");
                printWriter.println(lightStateToString(this.mLightState));
                printWriter.print("  mInactiveTimeout=");
                TimeUtils.formatDuration(this.mInactiveTimeout, printWriter);
                pw.println();
                if (this.mActiveIdleOpCount != 0) {
                    printWriter.print("  mActiveIdleOpCount=");
                    printWriter.println(this.mActiveIdleOpCount);
                }
                if (this.mNextAlarmTime != 0) {
                    printWriter.print("  mNextAlarmTime=");
                    TimeUtils.formatDuration(this.mNextAlarmTime, SystemClock.elapsedRealtime(), printWriter);
                    pw.println();
                }
                if (this.mNextIdlePendingDelay != 0) {
                    printWriter.print("  mNextIdlePendingDelay=");
                    TimeUtils.formatDuration(this.mNextIdlePendingDelay, printWriter);
                    pw.println();
                }
                if (this.mNextIdleDelay != 0) {
                    printWriter.print("  mNextIdleDelay=");
                    TimeUtils.formatDuration(this.mNextIdleDelay, printWriter);
                    pw.println();
                }
                if (this.mNextLightIdleDelay != 0) {
                    printWriter.print("  mNextIdleDelay=");
                    TimeUtils.formatDuration(this.mNextLightIdleDelay, printWriter);
                    pw.println();
                }
                if (this.mNextLightAlarmTime != 0) {
                    printWriter.print("  mNextLightAlarmTime=");
                    TimeUtils.formatDuration(this.mNextLightAlarmTime, SystemClock.elapsedRealtime(), printWriter);
                    pw.println();
                }
                if (this.mCurIdleBudget != 0) {
                    printWriter.print("  mCurIdleBudget=");
                    TimeUtils.formatDuration(this.mCurIdleBudget, printWriter);
                    pw.println();
                }
                if (this.mMaintenanceStartTime != 0) {
                    printWriter.print("  mMaintenanceStartTime=");
                    TimeUtils.formatDuration(this.mMaintenanceStartTime, SystemClock.elapsedRealtime(), printWriter);
                    pw.println();
                }
                if (this.mJobsActive) {
                    printWriter.print("  mJobsActive=");
                    printWriter.println(this.mJobsActive);
                }
                if (this.mAlarmsActive) {
                    printWriter.print("  mAlarmsActive=");
                    printWriter.println(this.mAlarmsActive);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpTempWhitelistSchedule(PrintWriter pw, boolean printTitle) {
        int size = this.mTempWhitelistAppIdEndTimes.size();
        if (size > 0) {
            String prefix = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            if (printTitle) {
                pw.println("  Temp whitelist schedule:");
                prefix = "    ";
            }
            long timeNow = SystemClock.elapsedRealtime();
            for (int i = 0; i < size; i++) {
                pw.print(prefix);
                pw.print("UID=");
                pw.print(this.mTempWhitelistAppIdEndTimes.keyAt(i));
                pw.print(": ");
                Pair<MutableLong, String> entry = this.mTempWhitelistAppIdEndTimes.valueAt(i);
                TimeUtils.formatDuration(((MutableLong) entry.first).value, timeNow, pw);
                pw.print(" - ");
                pw.println((String) entry.second);
            }
        }
    }

    public int forceIdleInternal() {
        synchronized (this) {
            if (!this.mDeepEnabled) {
                Slog.d(TAG, "Unable to go idle; not enabled");
                return -1;
            } else if (this.mForceIdle) {
                Slog.d(TAG, "now it is in ForceIdle by dump");
                return 0;
            } else {
                this.mForceIdle = true;
                becomeInactiveIfAppropriateLocked();
                int curState = this.mState;
                while (curState != 5) {
                    stepIdleStateLocked("s:shell");
                    if (curState == this.mState) {
                        Slog.d(TAG, "Unable to go idle; stopped at " + stateToString(this.mState));
                        exitForceIdleLocked();
                        return -1;
                    }
                    curState = this.mState;
                }
                this.mForceIdle = false;
                Slog.d(TAG, "Now forced in to idle mode");
                return 0;
            }
        }
    }
}
