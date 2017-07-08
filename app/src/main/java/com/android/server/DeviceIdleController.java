package com.android.server;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
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
import android.os.IDeviceIdleController.Stub;
import android.os.IMaintenanceActivityListener;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.KeyValueListParser;
import android.util.MutableLong;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.TimeUtils;
import android.util.Xml;
import android.view.Display;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.AtomicFile;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.am.BatteryStatsService;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.am.ProcessList;
import com.android.server.job.controllers.JobStatus;
import com.android.server.location.LocationFudger;
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
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class DeviceIdleController extends SystemService implements DeviceIdleCallback {
    private static final boolean COMPRESS_TIME = false;
    private static boolean DEBUG = false;
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
    static final int MSG_FINISH_IDLE_OP = 8;
    static final int MSG_REPORT_ACTIVE = 5;
    static final int MSG_REPORT_IDLE_OFF = 4;
    static final int MSG_REPORT_IDLE_ON = 2;
    static final int MSG_REPORT_IDLE_ON_LIGHT = 3;
    static final int MSG_REPORT_MAINTENANCE_ACTIVITY = 7;
    static final int MSG_TEMP_APP_WHITELIST_TIMEOUT = 6;
    static final int MSG_WRITE_CONFIG = 1;
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
    private WakeLock mActiveIdleWakeLock;
    private AlarmManager mAlarmManager;
    private boolean mAlarmsActive;
    private AnyMotionDetector mAnyMotionDetector;
    private IBatteryStats mBatteryStats;
    BinderService mBinderService;
    private boolean mCharging;
    public final AtomicFile mConfigFile;
    private ConnectivityService mConnectivityService;
    private Constants mConstants;
    private Display mCurDisplay;
    private long mCurIdleBudget;
    private final OnAlarmListener mDeepAlarmListener;
    private boolean mDeepEnabled;
    private final DisplayListener mDisplayListener;
    private DisplayManager mDisplayManager;
    private final int[] mEventCmds;
    private final long[] mEventTimes;
    private boolean mForceIdle;
    private final LocationListener mGenericLocationListener;
    private final LocationListener mGpsLocationListener;
    final MyHandler mHandler;
    private boolean mHasGps;
    private boolean mHasNetworkLocation;
    private Intent mIdleIntent;
    private final BroadcastReceiver mIdleStartedDoneReceiver;
    private long mInactiveTimeout;
    private boolean mJobsActive;
    private Location mLastGenericLocation;
    private Location mLastGpsLocation;
    private final OnAlarmListener mLightAlarmListener;
    private boolean mLightEnabled;
    private Intent mLightIdleIntent;
    private int mLightState;
    private com.android.server.AlarmManagerService.LocalService mLocalAlarmManager;
    private PowerManagerInternal mLocalPowerManager;
    private boolean mLocated;
    private boolean mLocating;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private final RemoteCallbackList<IMaintenanceActivityListener> mMaintenanceActivityListeners;
    private long mMaintenanceStartTime;
    private final MotionListener mMotionListener;
    private Sensor mMotionSensor;
    private boolean mNetworkConnected;
    private INetworkPolicyManager mNetworkPolicyManager;
    Runnable mNetworkPolicyTempWhitelistCallback;
    private long mNextAlarmTime;
    private long mNextIdleDelay;
    private long mNextIdlePendingDelay;
    private long mNextLightAlarmTime;
    private long mNextLightIdleDelay;
    private long mNextSensingTimeoutAlarmTime;
    private boolean mNotMoving;
    private PowerManager mPowerManager;
    private int[] mPowerSaveWhitelistAllAppIdArray;
    private final SparseBooleanArray mPowerSaveWhitelistAllAppIds;
    private final ArrayMap<String, Integer> mPowerSaveWhitelistApps;
    private final ArrayMap<String, Integer> mPowerSaveWhitelistAppsExceptIdle;
    private int[] mPowerSaveWhitelistExceptIdleAppIdArray;
    private final SparseBooleanArray mPowerSaveWhitelistExceptIdleAppIds;
    private final SparseBooleanArray mPowerSaveWhitelistSystemAppIds;
    private final SparseBooleanArray mPowerSaveWhitelistSystemAppIdsExceptIdle;
    private int[] mPowerSaveWhitelistUserAppIdArray;
    private final SparseBooleanArray mPowerSaveWhitelistUserAppIds;
    private final ArrayMap<String, Integer> mPowerSaveWhitelistUserApps;
    private final BroadcastReceiver mReceiver;
    private boolean mReportedMaintenanceActivity;
    private boolean mScreenOn;
    private final OnAlarmListener mSensingTimeoutAlarmListener;
    private SensorManager mSensorManager;
    private int mState;
    private int[] mTempWhitelistAppIdArray;
    private final SparseArray<Pair<MutableLong, String>> mTempWhitelistAppIdEndTimes;

    private final class BinderService extends Stub {
        private BinderService() {
        }

        public void addPowerSaveWhitelistApp(String name) {
            DeviceIdleController.this.getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                DeviceIdleController.this.addPowerSaveWhitelistAppInternal(name);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void removePowerSaveWhitelistApp(String name) {
            DeviceIdleController.this.getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                DeviceIdleController.this.removePowerSaveWhitelistAppInternal(name);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
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

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            DeviceIdleController.this.dump(fd, pw, args);
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) {
            new Shell().exec(this, in, out, err, args, resultReceiver);
        }

        public int forceIdle() {
            if (ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE == Binder.getCallingUid()) {
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
        private final boolean mHasWatch;
        private final KeyValueListParser mParser;
        private final ContentResolver mResolver;

        void dump(java.io.PrintWriter r1) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.DeviceIdleController.Constants.dump(java.io.PrintWriter):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.DeviceIdleController.Constants.dump(java.io.PrintWriter):void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.DeviceIdleController.Constants.dump(java.io.PrintWriter):void");
        }

        public Constants(Handler handler, ContentResolver resolver) {
            String str;
            super(handler);
            this.mParser = new KeyValueListParser(',');
            this.mResolver = resolver;
            this.mHasWatch = DeviceIdleController.this.getContext().getPackageManager().hasSystemFeature("android.hardware.type.watch");
            ContentResolver contentResolver = this.mResolver;
            if (this.mHasWatch) {
                str = "device_idle_constants_watch";
            } else {
                str = "device_idle_constants";
            }
            contentResolver.registerContentObserver(Global.getUriFor(str), DeviceIdleController.COMPRESS_TIME, this);
            updateConstants();
        }

        public void onChange(boolean selfChange, Uri uri) {
            updateConstants();
        }

        private void updateConstants() {
            synchronized (DeviceIdleController.this) {
                try {
                    String str;
                    KeyValueListParser keyValueListParser = this.mParser;
                    ContentResolver contentResolver = this.mResolver;
                    if (this.mHasWatch) {
                        str = "device_idle_constants_watch";
                    } else {
                        str = "device_idle_constants";
                    }
                    keyValueListParser.setString(Global.getString(contentResolver, str));
                } catch (IllegalArgumentException e) {
                    Slog.e(DeviceIdleController.TAG, "Bad device idle settings", e);
                }
                this.LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT = this.mParser.getLong(KEY_LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT, 300000);
                this.LIGHT_PRE_IDLE_TIMEOUT = this.mParser.getLong(KEY_LIGHT_PRE_IDLE_TIMEOUT, LocationFudger.FASTEST_INTERVAL_MS);
                this.LIGHT_IDLE_TIMEOUT = this.mParser.getLong(KEY_LIGHT_IDLE_TIMEOUT, 300000);
                this.LIGHT_IDLE_FACTOR = this.mParser.getFloat(KEY_LIGHT_IDLE_FACTOR, 2.0f);
                this.LIGHT_MAX_IDLE_TIMEOUT = this.mParser.getLong(KEY_LIGHT_MAX_IDLE_TIMEOUT, 900000);
                this.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET = this.mParser.getLong(KEY_LIGHT_IDLE_MAINTENANCE_MIN_BUDGET, 60000);
                this.LIGHT_IDLE_MAINTENANCE_MAX_BUDGET = this.mParser.getLong(KEY_LIGHT_IDLE_MAINTENANCE_MAX_BUDGET, 300000);
                this.MIN_LIGHT_MAINTENANCE_TIME = this.mParser.getLong(KEY_MIN_LIGHT_MAINTENANCE_TIME, 5000);
                this.MIN_DEEP_MAINTENANCE_TIME = this.mParser.getLong(KEY_MIN_DEEP_MAINTENANCE_TIME, 30000);
                this.INACTIVE_TIMEOUT = this.mParser.getLong(KEY_INACTIVE_TIMEOUT, ((long) ((this.mHasWatch ? 15 : 30) * 60)) * 1000);
                this.SENSING_TIMEOUT = this.mParser.getLong(KEY_SENSING_TIMEOUT, !DeviceIdleController.DEBUG ? 240000 : 60000);
                this.LOCATING_TIMEOUT = this.mParser.getLong(KEY_LOCATING_TIMEOUT, !DeviceIdleController.DEBUG ? 30000 : 15000);
                this.LOCATION_ACCURACY = this.mParser.getFloat(KEY_LOCATION_ACCURACY, 20.0f);
                this.MOTION_INACTIVE_TIMEOUT = this.mParser.getLong(KEY_MOTION_INACTIVE_TIMEOUT, LocationFudger.FASTEST_INTERVAL_MS);
                this.IDLE_AFTER_INACTIVE_TIMEOUT = this.mParser.getLong(KEY_IDLE_AFTER_INACTIVE_TIMEOUT, ((long) ((this.mHasWatch ? 15 : 30) * 60)) * 1000);
                this.IDLE_PENDING_TIMEOUT = this.mParser.getLong(KEY_IDLE_PENDING_TIMEOUT, 300000);
                this.MAX_IDLE_PENDING_TIMEOUT = this.mParser.getLong(KEY_MAX_IDLE_PENDING_TIMEOUT, LocationFudger.FASTEST_INTERVAL_MS);
                this.IDLE_PENDING_FACTOR = this.mParser.getFloat(KEY_IDLE_PENDING_FACTOR, 2.0f);
                this.IDLE_TIMEOUT = this.mParser.getLong(KEY_IDLE_TIMEOUT, 3600000);
                this.MAX_IDLE_TIMEOUT = this.mParser.getLong(KEY_MAX_IDLE_TIMEOUT, 21600000);
                this.IDLE_FACTOR = this.mParser.getFloat(KEY_IDLE_FACTOR, 2.0f);
                this.MIN_TIME_TO_ALARM = this.mParser.getLong(KEY_MIN_TIME_TO_ALARM, 3600000);
                this.MAX_TEMP_APP_WHITELIST_DURATION = this.mParser.getLong(KEY_MAX_TEMP_APP_WHITELIST_DURATION, 300000);
                this.MMS_TEMP_APP_WHITELIST_DURATION = this.mParser.getLong(KEY_MMS_TEMP_APP_WHITELIST_DURATION, 60000);
                this.SMS_TEMP_APP_WHITELIST_DURATION = this.mParser.getLong(KEY_SMS_TEMP_APP_WHITELIST_DURATION, 20000);
                this.NOTIFICATION_WHITELIST_DURATION = this.mParser.getLong(KEY_NOTIFICATION_WHITELIST_DURATION, 30000);
            }
        }
    }

    public final class LocalService {
        public void addPowerSaveTempWhitelistAppDirect(int appId, long duration, boolean sync, String reason) {
            DeviceIdleController.this.addPowerSaveTempWhitelistAppDirectInternal(DeviceIdleController.STATE_ACTIVE, appId, duration, sync, reason);
        }

        public long getNotificationWhitelistDuration() {
            return DeviceIdleController.this.mConstants.NOTIFICATION_WHITELIST_DURATION;
        }

        public void setNetworkPolicyTempWhitelistCallback(Runnable callback) {
            DeviceIdleController.this.setNetworkPolicyTempWhitelistCallbackInternal(callback);
        }

        public void setJobsActive(boolean active) {
            DeviceIdleController.this.setJobsActive(active);
        }

        public void setAlarmsActive(boolean active) {
            DeviceIdleController.this.setAlarmsActive(active);
        }

        public int[] getPowerSaveWhitelistUserAppIds() {
            return DeviceIdleController.this.getPowerSaveWhitelistUserAppIds();
        }
    }

    private final class MotionListener extends TriggerEventListener implements SensorEventListener {
        boolean active;

        private MotionListener() {
            this.active = DeviceIdleController.COMPRESS_TIME;
        }

        public void onTrigger(TriggerEvent event) {
            synchronized (DeviceIdleController.this) {
                this.active = DeviceIdleController.COMPRESS_TIME;
                DeviceIdleController.this.motionLocked();
            }
        }

        public void onSensorChanged(SensorEvent event) {
            synchronized (DeviceIdleController.this) {
                DeviceIdleController.this.mSensorManager.unregisterListener(this, DeviceIdleController.this.mMotionSensor);
                this.active = DeviceIdleController.COMPRESS_TIME;
                DeviceIdleController.this.motionLocked();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public boolean registerLocked() {
            boolean success;
            if (DeviceIdleController.this.mMotionSensor.getReportingMode() == DeviceIdleController.STATE_IDLE_PENDING) {
                success = DeviceIdleController.this.mSensorManager.requestTriggerSensor(DeviceIdleController.this.mMotionListener, DeviceIdleController.this.mMotionSensor);
            } else {
                success = DeviceIdleController.this.mSensorManager.registerListener(DeviceIdleController.this.mMotionListener, DeviceIdleController.this.mMotionSensor, DeviceIdleController.STATE_SENSING);
            }
            if (success) {
                this.active = true;
            } else {
                Slog.e(DeviceIdleController.TAG, "Unable to register for " + DeviceIdleController.this.mMotionSensor);
            }
            return success;
        }

        public void unregisterLocked() {
            if (DeviceIdleController.this.mMotionSensor.getReportingMode() == DeviceIdleController.STATE_IDLE_PENDING) {
                DeviceIdleController.this.mSensorManager.cancelTriggerSensor(DeviceIdleController.this.mMotionListener, DeviceIdleController.this.mMotionSensor);
            } else {
                DeviceIdleController.this.mSensorManager.unregisterListener(DeviceIdleController.this.mMotionListener);
            }
            this.active = DeviceIdleController.COMPRESS_TIME;
        }
    }

    final class MyHandler extends Handler {
        MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (DeviceIdleController.DEBUG) {
                Slog.d(DeviceIdleController.TAG, "handleMessage(" + msg.what + ")");
            }
            boolean deepChanged;
            boolean lightChanged;
            switch (msg.what) {
                case DeviceIdleController.STATE_INACTIVE /*1*/:
                    DeviceIdleController.this.handleWriteConfigFile();
                case DeviceIdleController.STATE_IDLE_PENDING /*2*/:
                case DeviceIdleController.STATE_SENSING /*3*/:
                    EventLogTags.writeDeviceIdleOnStart();
                    if (msg.what == DeviceIdleController.STATE_IDLE_PENDING) {
                        deepChanged = DeviceIdleController.this.mLocalPowerManager.setDeviceIdleMode(true);
                        lightChanged = DeviceIdleController.this.mLocalPowerManager.setLightDeviceIdleMode(DeviceIdleController.COMPRESS_TIME);
                    } else {
                        deepChanged = DeviceIdleController.this.mLocalPowerManager.setDeviceIdleMode(DeviceIdleController.COMPRESS_TIME);
                        lightChanged = DeviceIdleController.this.mLocalPowerManager.setLightDeviceIdleMode(true);
                    }
                    try {
                        int i;
                        DeviceIdleController.this.mNetworkPolicyManager.setDeviceIdleMode(true);
                        IBatteryStats -get1 = DeviceIdleController.this.mBatteryStats;
                        if (msg.what == DeviceIdleController.STATE_IDLE_PENDING) {
                            i = DeviceIdleController.STATE_IDLE_PENDING;
                        } else {
                            i = DeviceIdleController.STATE_INACTIVE;
                        }
                        -get1.noteDeviceIdleMode(i, null, Process.myUid());
                    } catch (RemoteException e) {
                    }
                    if (deepChanged) {
                        DeviceIdleController.this.getContext().sendBroadcastAsUser(DeviceIdleController.this.mIdleIntent, UserHandle.ALL);
                    }
                    if (lightChanged) {
                        DeviceIdleController.this.getContext().sendBroadcastAsUser(DeviceIdleController.this.mLightIdleIntent, UserHandle.ALL);
                    }
                    EventLogTags.writeDeviceIdleOnComplete();
                case DeviceIdleController.STATE_LOCATING /*4*/:
                    EventLogTags.writeDeviceIdleOffStart("unknown");
                    deepChanged = DeviceIdleController.this.mLocalPowerManager.setDeviceIdleMode(DeviceIdleController.COMPRESS_TIME);
                    lightChanged = DeviceIdleController.this.mLocalPowerManager.setLightDeviceIdleMode(DeviceIdleController.COMPRESS_TIME);
                    try {
                        DeviceIdleController.this.mNetworkPolicyManager.setDeviceIdleMode(DeviceIdleController.COMPRESS_TIME);
                        DeviceIdleController.this.mBatteryStats.noteDeviceIdleMode(DeviceIdleController.STATE_ACTIVE, null, Process.myUid());
                    } catch (RemoteException e2) {
                    }
                    if (deepChanged) {
                        DeviceIdleController.this.incActiveIdleOps();
                        DeviceIdleController.this.getContext().sendOrderedBroadcastAsUser(DeviceIdleController.this.mIdleIntent, UserHandle.ALL, null, DeviceIdleController.this.mIdleStartedDoneReceiver, null, DeviceIdleController.STATE_ACTIVE, null, null);
                    }
                    if (lightChanged) {
                        DeviceIdleController.this.incActiveIdleOps();
                        DeviceIdleController.this.getContext().sendOrderedBroadcastAsUser(DeviceIdleController.this.mLightIdleIntent, UserHandle.ALL, null, DeviceIdleController.this.mIdleStartedDoneReceiver, null, DeviceIdleController.STATE_ACTIVE, null, null);
                    }
                    DeviceIdleController.this.decActiveIdleOps();
                    EventLogTags.writeDeviceIdleOffComplete();
                case DeviceIdleController.STATE_IDLE /*5*/:
                    String activeReason = msg.obj;
                    int activeUid = msg.arg1;
                    EventLogTags.writeDeviceIdleOffStart(activeReason != null ? activeReason : "unknown");
                    deepChanged = DeviceIdleController.this.mLocalPowerManager.setDeviceIdleMode(DeviceIdleController.COMPRESS_TIME);
                    lightChanged = DeviceIdleController.this.mLocalPowerManager.setLightDeviceIdleMode(DeviceIdleController.COMPRESS_TIME);
                    try {
                        DeviceIdleController.this.mNetworkPolicyManager.setDeviceIdleMode(DeviceIdleController.COMPRESS_TIME);
                        DeviceIdleController.this.mBatteryStats.noteDeviceIdleMode(DeviceIdleController.STATE_ACTIVE, activeReason, activeUid);
                    } catch (RemoteException e3) {
                    }
                    if (deepChanged) {
                        DeviceIdleController.this.getContext().sendBroadcastAsUser(DeviceIdleController.this.mIdleIntent, UserHandle.ALL);
                    }
                    if (lightChanged) {
                        DeviceIdleController.this.getContext().sendBroadcastAsUser(DeviceIdleController.this.mLightIdleIntent, UserHandle.ALL);
                    }
                    EventLogTags.writeDeviceIdleOffComplete();
                case DeviceIdleController.STATE_IDLE_MAINTENANCE /*6*/:
                    DeviceIdleController.this.checkTempAppWhitelistTimeout(msg.arg1);
                case DeviceIdleController.MSG_REPORT_MAINTENANCE_ACTIVITY /*7*/:
                    boolean active = msg.arg1 == DeviceIdleController.STATE_INACTIVE ? true : DeviceIdleController.COMPRESS_TIME;
                    int size = DeviceIdleController.this.mMaintenanceActivityListeners.beginBroadcast();
                    for (int i2 = DeviceIdleController.STATE_ACTIVE; i2 < size; i2 += DeviceIdleController.STATE_INACTIVE) {
                        try {
                            ((IMaintenanceActivityListener) DeviceIdleController.this.mMaintenanceActivityListeners.getBroadcastItem(i2)).onMaintenanceActivityChanged(active);
                        } catch (RemoteException e4) {
                        } catch (Throwable th) {
                            DeviceIdleController.this.mMaintenanceActivityListeners.finishBroadcast();
                        }
                    }
                    DeviceIdleController.this.mMaintenanceActivityListeners.finishBroadcast();
                case DeviceIdleController.MSG_FINISH_IDLE_OP /*8*/:
                    DeviceIdleController.this.decActiveIdleOps();
                default:
            }
        }
    }

    class Shell extends ShellCommand {
        int userId;

        Shell() {
            this.userId = DeviceIdleController.STATE_ACTIVE;
        }

        public int onCommand(String cmd) {
            return DeviceIdleController.this.onShellCommand(this, cmd);
        }

        public void onHelp() {
            DeviceIdleController.dumpHelp(getOutPrintWriter());
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.DeviceIdleController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.DeviceIdleController.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.DeviceIdleController.<clinit>():void");
    }

    int onShellCommand(com.android.server.DeviceIdleController.Shell r22, java.lang.String r23) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:84:0x017b
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.modifyBlocksTree(BlockProcessor.java:248)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:52)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r21 = this;
        r16 = r22.getOutPrintWriter();
        r2 = "step";
        r0 = r23;
        r2 = r2.equals(r0);
        if (r2 == 0) goto L_0x009a;
    L_0x000f:
        r2 = r21.getContext();
        r4 = "android.permission.DEVICE_POWER";
        r5 = 0;
        r2.enforceCallingOrSelfPermission(r4, r5);
        monitor-enter(r21);
        r18 = android.os.Binder.clearCallingIdentity();	 Catch:{ all -> 0x0078 }
        r3 = r22.getNextArg();	 Catch:{ all -> 0x0078 }
        if (r3 == 0) goto L_0x002e;
    L_0x0025:
        r2 = "deep";	 Catch:{ all -> 0x0078 }
        r2 = r2.equals(r3);	 Catch:{ all -> 0x0078 }
        if (r2 == 0) goto L_0x0051;	 Catch:{ all -> 0x0078 }
    L_0x002e:
        r2 = "s:shell";	 Catch:{ all -> 0x0078 }
        r0 = r21;	 Catch:{ all -> 0x0078 }
        r0.stepIdleStateLocked(r2);	 Catch:{ all -> 0x0078 }
        r2 = "Stepped to deep: ";	 Catch:{ all -> 0x0078 }
        r0 = r16;	 Catch:{ all -> 0x0078 }
        r0.print(r2);	 Catch:{ all -> 0x0078 }
        r0 = r21;	 Catch:{ all -> 0x0078 }
        r2 = r0.mState;	 Catch:{ all -> 0x0078 }
        r2 = stateToString(r2);	 Catch:{ all -> 0x0078 }
        r0 = r16;	 Catch:{ all -> 0x0078 }
        r0.println(r2);	 Catch:{ all -> 0x0078 }
    L_0x004b:
        android.os.Binder.restoreCallingIdentity(r18);	 Catch:{ all -> 0x0078 }
    L_0x004e:
        monitor-exit(r21);
    L_0x004f:
        r2 = 0;
        return r2;
    L_0x0051:
        r2 = "light";	 Catch:{ all -> 0x0078 }
        r2 = r2.equals(r3);	 Catch:{ all -> 0x0078 }
        if (r2 == 0) goto L_0x0080;	 Catch:{ all -> 0x0078 }
    L_0x005a:
        r2 = "s:shell";	 Catch:{ all -> 0x0078 }
        r0 = r21;	 Catch:{ all -> 0x0078 }
        r0.stepLightIdleStateLocked(r2);	 Catch:{ all -> 0x0078 }
        r2 = "Stepped to light: ";	 Catch:{ all -> 0x0078 }
        r0 = r16;	 Catch:{ all -> 0x0078 }
        r0.print(r2);	 Catch:{ all -> 0x0078 }
        r0 = r21;	 Catch:{ all -> 0x0078 }
        r2 = r0.mLightState;	 Catch:{ all -> 0x0078 }
        r2 = lightStateToString(r2);	 Catch:{ all -> 0x0078 }
        r0 = r16;	 Catch:{ all -> 0x0078 }
        r0.println(r2);	 Catch:{ all -> 0x0078 }
        goto L_0x004b;
    L_0x0078:
        r2 = move-exception;
        android.os.Binder.restoreCallingIdentity(r18);	 Catch:{ all -> 0x0078 }
        throw r2;	 Catch:{ all -> 0x0078 }
    L_0x007d:
        r2 = move-exception;
        monitor-exit(r21);
        throw r2;
    L_0x0080:
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0078 }
        r2.<init>();	 Catch:{ all -> 0x0078 }
        r4 = "Unknown idle mode: ";	 Catch:{ all -> 0x0078 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0078 }
        r2 = r2.append(r3);	 Catch:{ all -> 0x0078 }
        r2 = r2.toString();	 Catch:{ all -> 0x0078 }
        r0 = r16;	 Catch:{ all -> 0x0078 }
        r0.println(r2);	 Catch:{ all -> 0x0078 }
        goto L_0x004b;
    L_0x009a:
        r2 = "force-idle";
        r0 = r23;
        r2 = r2.equals(r0);
        if (r2 == 0) goto L_0x0199;
    L_0x00a5:
        r2 = r21.getContext();
        r4 = "android.permission.DEVICE_POWER";
        r5 = 0;
        r2.enforceCallingOrSelfPermission(r4, r5);
        monitor-enter(r21);
        r18 = android.os.Binder.clearCallingIdentity();	 Catch:{ all -> 0x017a }
        r3 = r22.getNextArg();	 Catch:{ all -> 0x017a }
        if (r3 == 0) goto L_0x00c4;
    L_0x00bb:
        r2 = "deep";	 Catch:{ all -> 0x017a }
        r2 = r2.equals(r3);	 Catch:{ all -> 0x017a }
        if (r2 == 0) goto L_0x0128;	 Catch:{ all -> 0x017a }
    L_0x00c4:
        r0 = r21;	 Catch:{ all -> 0x017a }
        r2 = r0.mDeepEnabled;	 Catch:{ all -> 0x017a }
        if (r2 != 0) goto L_0x00d8;	 Catch:{ all -> 0x017a }
    L_0x00ca:
        r2 = "Unable to go deep idle; not enabled";	 Catch:{ all -> 0x017a }
        r0 = r16;	 Catch:{ all -> 0x017a }
        r0.println(r2);	 Catch:{ all -> 0x017a }
        android.os.Binder.restoreCallingIdentity(r18);	 Catch:{ all -> 0x017a }
        r2 = -1;
        monitor-exit(r21);
        return r2;
    L_0x00d8:
        r2 = 1;
        r0 = r21;	 Catch:{ all -> 0x017a }
        r0.mForceIdle = r2;	 Catch:{ all -> 0x017a }
        r21.becomeInactiveIfAppropriateLocked();	 Catch:{ all -> 0x017a }
        r0 = r21;	 Catch:{ all -> 0x017a }
        r11 = r0.mState;	 Catch:{ all -> 0x017a }
    L_0x00e4:
        r2 = 5;	 Catch:{ all -> 0x017a }
        if (r11 == r2) goto L_0x0118;	 Catch:{ all -> 0x017a }
    L_0x00e7:
        r2 = "s:shell";	 Catch:{ all -> 0x017a }
        r0 = r21;	 Catch:{ all -> 0x017a }
        r0.stepIdleStateLocked(r2);	 Catch:{ all -> 0x017a }
        r0 = r21;	 Catch:{ all -> 0x017a }
        r2 = r0.mState;	 Catch:{ all -> 0x017a }
        if (r11 != r2) goto L_0x0113;	 Catch:{ all -> 0x017a }
    L_0x00f5:
        r2 = "Unable to go deep idle; stopped at ";	 Catch:{ all -> 0x017a }
        r0 = r16;	 Catch:{ all -> 0x017a }
        r0.print(r2);	 Catch:{ all -> 0x017a }
        r0 = r21;	 Catch:{ all -> 0x017a }
        r2 = r0.mState;	 Catch:{ all -> 0x017a }
        r2 = stateToString(r2);	 Catch:{ all -> 0x017a }
        r0 = r16;	 Catch:{ all -> 0x017a }
        r0.println(r2);	 Catch:{ all -> 0x017a }
        r21.exitForceIdleLocked();	 Catch:{ all -> 0x017a }
        android.os.Binder.restoreCallingIdentity(r18);	 Catch:{ all -> 0x017a }
        r2 = -1;
        monitor-exit(r21);
        return r2;
    L_0x0113:
        r0 = r21;	 Catch:{ all -> 0x017a }
        r11 = r0.mState;	 Catch:{ all -> 0x017a }
        goto L_0x00e4;	 Catch:{ all -> 0x017a }
    L_0x0118:
        r2 = "Now forced in to deep idle mode";	 Catch:{ all -> 0x017a }
        r0 = r16;	 Catch:{ all -> 0x017a }
        r0.println(r2);	 Catch:{ all -> 0x017a }
    L_0x0120:
        android.os.Binder.restoreCallingIdentity(r18);	 Catch:{ all -> 0x017a }
        goto L_0x004e;
    L_0x0125:
        r2 = move-exception;
        monitor-exit(r21);
        throw r2;
    L_0x0128:
        r2 = "light";	 Catch:{ all -> 0x017a }
        r2 = r2.equals(r3);	 Catch:{ all -> 0x017a }
        if (r2 == 0) goto L_0x017f;	 Catch:{ all -> 0x017a }
    L_0x0131:
        r2 = 1;	 Catch:{ all -> 0x017a }
        r0 = r21;	 Catch:{ all -> 0x017a }
        r0.mForceIdle = r2;	 Catch:{ all -> 0x017a }
        r21.becomeInactiveIfAppropriateLocked();	 Catch:{ all -> 0x017a }
        r0 = r21;	 Catch:{ all -> 0x017a }
        r10 = r0.mLightState;	 Catch:{ all -> 0x017a }
    L_0x013d:
        r2 = 4;	 Catch:{ all -> 0x017a }
        if (r10 == r2) goto L_0x0171;	 Catch:{ all -> 0x017a }
    L_0x0140:
        r2 = "s:shell";	 Catch:{ all -> 0x017a }
        r0 = r21;	 Catch:{ all -> 0x017a }
        r0.stepIdleStateLocked(r2);	 Catch:{ all -> 0x017a }
        r0 = r21;	 Catch:{ all -> 0x017a }
        r2 = r0.mLightState;	 Catch:{ all -> 0x017a }
        if (r10 != r2) goto L_0x016c;	 Catch:{ all -> 0x017a }
    L_0x014e:
        r2 = "Unable to go light idle; stopped at ";	 Catch:{ all -> 0x017a }
        r0 = r16;	 Catch:{ all -> 0x017a }
        r0.print(r2);	 Catch:{ all -> 0x017a }
        r0 = r21;	 Catch:{ all -> 0x017a }
        r2 = r0.mLightState;	 Catch:{ all -> 0x017a }
        r2 = lightStateToString(r2);	 Catch:{ all -> 0x017a }
        r0 = r16;	 Catch:{ all -> 0x017a }
        r0.println(r2);	 Catch:{ all -> 0x017a }
        r21.exitForceIdleLocked();	 Catch:{ all -> 0x017a }
        android.os.Binder.restoreCallingIdentity(r18);
        r2 = -1;
        monitor-exit(r21);
        return r2;
    L_0x016c:
        r0 = r21;	 Catch:{ all -> 0x017a }
        r10 = r0.mLightState;	 Catch:{ all -> 0x017a }
        goto L_0x013d;	 Catch:{ all -> 0x017a }
    L_0x0171:
        r2 = "Now forced in to light idle mode";	 Catch:{ all -> 0x017a }
        r0 = r16;	 Catch:{ all -> 0x017a }
        r0.println(r2);	 Catch:{ all -> 0x017a }
        goto L_0x0120;
    L_0x017a:
        r2 = move-exception;
        android.os.Binder.restoreCallingIdentity(r18);	 Catch:{ all -> 0x017a }
        throw r2;	 Catch:{ all -> 0x017a }
    L_0x017f:
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x017a }
        r2.<init>();	 Catch:{ all -> 0x017a }
        r4 = "Unknown idle mode: ";	 Catch:{ all -> 0x017a }
        r2 = r2.append(r4);	 Catch:{ all -> 0x017a }
        r2 = r2.append(r3);	 Catch:{ all -> 0x017a }
        r2 = r2.toString();	 Catch:{ all -> 0x017a }
        r0 = r16;	 Catch:{ all -> 0x017a }
        r0.println(r2);	 Catch:{ all -> 0x017a }
        goto L_0x0120;
    L_0x0199:
        r2 = "force-inactive";
        r0 = r23;
        r2 = r2.equals(r0);
        if (r2 == 0) goto L_0x01f3;
    L_0x01a4:
        r2 = r21.getContext();
        r4 = "android.permission.DEVICE_POWER";
        r5 = 0;
        r2.enforceCallingOrSelfPermission(r4, r5);
        monitor-enter(r21);
        r18 = android.os.Binder.clearCallingIdentity();	 Catch:{ all -> 0x01ee }
        r2 = 1;
        r0 = r21;	 Catch:{ all -> 0x01ee }
        r0.mForceIdle = r2;	 Catch:{ all -> 0x01ee }
        r21.becomeInactiveIfAppropriateLocked();	 Catch:{ all -> 0x01ee }
        r2 = "Light state: ";	 Catch:{ all -> 0x01ee }
        r0 = r16;	 Catch:{ all -> 0x01ee }
        r0.print(r2);	 Catch:{ all -> 0x01ee }
        r0 = r21;	 Catch:{ all -> 0x01ee }
        r2 = r0.mLightState;	 Catch:{ all -> 0x01ee }
        r2 = lightStateToString(r2);	 Catch:{ all -> 0x01ee }
        r0 = r16;	 Catch:{ all -> 0x01ee }
        r0.print(r2);	 Catch:{ all -> 0x01ee }
        r2 = ", deep state: ";	 Catch:{ all -> 0x01ee }
        r0 = r16;	 Catch:{ all -> 0x01ee }
        r0.print(r2);	 Catch:{ all -> 0x01ee }
        r0 = r21;	 Catch:{ all -> 0x01ee }
        r2 = r0.mState;	 Catch:{ all -> 0x01ee }
        r2 = stateToString(r2);	 Catch:{ all -> 0x01ee }
        r0 = r16;	 Catch:{ all -> 0x01ee }
        r0.println(r2);	 Catch:{ all -> 0x01ee }
        android.os.Binder.restoreCallingIdentity(r18);
        goto L_0x004e;
    L_0x01eb:
        r2 = move-exception;
        monitor-exit(r21);
        throw r2;
    L_0x01ee:
        r2 = move-exception;
        android.os.Binder.restoreCallingIdentity(r18);	 Catch:{ all -> 0x01ee }
        throw r2;	 Catch:{ all -> 0x01ee }
    L_0x01f3:
        r2 = "unforce";
        r0 = r23;
        r2 = r2.equals(r0);
        if (r2 == 0) goto L_0x0248;
    L_0x01fe:
        r2 = r21.getContext();
        r4 = "android.permission.DEVICE_POWER";
        r5 = 0;
        r2.enforceCallingOrSelfPermission(r4, r5);
        monitor-enter(r21);
        r18 = android.os.Binder.clearCallingIdentity();	 Catch:{ all -> 0x0243 }
        r21.exitForceIdleLocked();	 Catch:{ all -> 0x0243 }
        r2 = "Light state: ";	 Catch:{ all -> 0x0243 }
        r0 = r16;	 Catch:{ all -> 0x0243 }
        r0.print(r2);	 Catch:{ all -> 0x0243 }
        r0 = r21;	 Catch:{ all -> 0x0243 }
        r2 = r0.mLightState;	 Catch:{ all -> 0x0243 }
        r2 = lightStateToString(r2);	 Catch:{ all -> 0x0243 }
        r0 = r16;	 Catch:{ all -> 0x0243 }
        r0.print(r2);	 Catch:{ all -> 0x0243 }
        r2 = ", deep state: ";	 Catch:{ all -> 0x0243 }
        r0 = r16;	 Catch:{ all -> 0x0243 }
        r0.print(r2);	 Catch:{ all -> 0x0243 }
        r0 = r21;	 Catch:{ all -> 0x0243 }
        r2 = r0.mState;	 Catch:{ all -> 0x0243 }
        r2 = stateToString(r2);	 Catch:{ all -> 0x0243 }
        r0 = r16;	 Catch:{ all -> 0x0243 }
        r0.println(r2);	 Catch:{ all -> 0x0243 }
        android.os.Binder.restoreCallingIdentity(r18);
        goto L_0x004e;
    L_0x0240:
        r2 = move-exception;
        monitor-exit(r21);
        throw r2;
    L_0x0243:
        r2 = move-exception;
        android.os.Binder.restoreCallingIdentity(r18);	 Catch:{ all -> 0x0243 }
        throw r2;	 Catch:{ all -> 0x0243 }
    L_0x0248:
        r2 = "get";
        r0 = r23;
        r2 = r2.equals(r0);
        if (r2 == 0) goto L_0x0314;
    L_0x0253:
        r2 = r21.getContext();
        r4 = "android.permission.DEVICE_POWER";
        r5 = 0;
        r2.enforceCallingOrSelfPermission(r4, r5);
        monitor-enter(r21);
        r3 = r22.getNextArg();	 Catch:{ all -> 0x029e }
        if (r3 == 0) goto L_0x030a;	 Catch:{ all -> 0x029e }
    L_0x0265:
        r18 = android.os.Binder.clearCallingIdentity();	 Catch:{ all -> 0x029e }
        r2 = "light";	 Catch:{ all -> 0x029e }
        r2 = r3.equals(r2);	 Catch:{ all -> 0x029e }
        if (r2 == 0) goto L_0x0287;	 Catch:{ all -> 0x029e }
    L_0x0272:
        r0 = r21;	 Catch:{ all -> 0x029e }
        r2 = r0.mLightState;	 Catch:{ all -> 0x029e }
        r2 = lightStateToString(r2);	 Catch:{ all -> 0x029e }
        r0 = r16;	 Catch:{ all -> 0x029e }
        r0.println(r2);	 Catch:{ all -> 0x029e }
    L_0x027f:
        android.os.Binder.restoreCallingIdentity(r18);
        goto L_0x004e;
    L_0x0284:
        r2 = move-exception;
        monitor-exit(r21);
        throw r2;
    L_0x0287:
        r2 = "deep";	 Catch:{ all -> 0x029e }
        r2 = r3.equals(r2);	 Catch:{ all -> 0x029e }
        if (r2 == 0) goto L_0x02a3;	 Catch:{ all -> 0x029e }
    L_0x0290:
        r0 = r21;	 Catch:{ all -> 0x029e }
        r2 = r0.mState;	 Catch:{ all -> 0x029e }
        r2 = stateToString(r2);	 Catch:{ all -> 0x029e }
        r0 = r16;	 Catch:{ all -> 0x029e }
        r0.println(r2);	 Catch:{ all -> 0x029e }
        goto L_0x027f;
    L_0x029e:
        r2 = move-exception;
        android.os.Binder.restoreCallingIdentity(r18);	 Catch:{ all -> 0x029e }
        throw r2;	 Catch:{ all -> 0x029e }
    L_0x02a3:
        r2 = "force";	 Catch:{ all -> 0x029e }
        r2 = r3.equals(r2);	 Catch:{ all -> 0x029e }
        if (r2 == 0) goto L_0x02b6;	 Catch:{ all -> 0x029e }
    L_0x02ac:
        r0 = r21;	 Catch:{ all -> 0x029e }
        r2 = r0.mForceIdle;	 Catch:{ all -> 0x029e }
        r0 = r16;	 Catch:{ all -> 0x029e }
        r0.println(r2);	 Catch:{ all -> 0x029e }
        goto L_0x027f;	 Catch:{ all -> 0x029e }
    L_0x02b6:
        r2 = "screen";	 Catch:{ all -> 0x029e }
        r2 = r3.equals(r2);	 Catch:{ all -> 0x029e }
        if (r2 == 0) goto L_0x02c9;	 Catch:{ all -> 0x029e }
    L_0x02bf:
        r0 = r21;	 Catch:{ all -> 0x029e }
        r2 = r0.mScreenOn;	 Catch:{ all -> 0x029e }
        r0 = r16;	 Catch:{ all -> 0x029e }
        r0.println(r2);	 Catch:{ all -> 0x029e }
        goto L_0x027f;	 Catch:{ all -> 0x029e }
    L_0x02c9:
        r2 = "charging";	 Catch:{ all -> 0x029e }
        r2 = r3.equals(r2);	 Catch:{ all -> 0x029e }
        if (r2 == 0) goto L_0x02dc;	 Catch:{ all -> 0x029e }
    L_0x02d2:
        r0 = r21;	 Catch:{ all -> 0x029e }
        r2 = r0.mCharging;	 Catch:{ all -> 0x029e }
        r0 = r16;	 Catch:{ all -> 0x029e }
        r0.println(r2);	 Catch:{ all -> 0x029e }
        goto L_0x027f;	 Catch:{ all -> 0x029e }
    L_0x02dc:
        r2 = "network";	 Catch:{ all -> 0x029e }
        r2 = r3.equals(r2);	 Catch:{ all -> 0x029e }
        if (r2 == 0) goto L_0x02ef;	 Catch:{ all -> 0x029e }
    L_0x02e5:
        r0 = r21;	 Catch:{ all -> 0x029e }
        r2 = r0.mNetworkConnected;	 Catch:{ all -> 0x029e }
        r0 = r16;	 Catch:{ all -> 0x029e }
        r0.println(r2);	 Catch:{ all -> 0x029e }
        goto L_0x027f;	 Catch:{ all -> 0x029e }
    L_0x02ef:
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x029e }
        r2.<init>();	 Catch:{ all -> 0x029e }
        r4 = "Unknown get option: ";	 Catch:{ all -> 0x029e }
        r2 = r2.append(r4);	 Catch:{ all -> 0x029e }
        r2 = r2.append(r3);	 Catch:{ all -> 0x029e }
        r2 = r2.toString();	 Catch:{ all -> 0x029e }
        r0 = r16;	 Catch:{ all -> 0x029e }
        r0.println(r2);	 Catch:{ all -> 0x029e }
        goto L_0x027f;
    L_0x030a:
        r2 = "Argument required";	 Catch:{ all -> 0x029e }
        r0 = r16;	 Catch:{ all -> 0x029e }
        r0.println(r2);	 Catch:{ all -> 0x029e }
        goto L_0x004e;
    L_0x0314:
        r2 = "disable";
        r0 = r23;
        r2 = r2.equals(r0);
        if (r2 == 0) goto L_0x03d8;
    L_0x031f:
        r2 = r21.getContext();
        r4 = "android.permission.DEVICE_POWER";
        r5 = 0;
        r2.enforceCallingOrSelfPermission(r4, r5);
        monitor-enter(r21);
        r18 = android.os.Binder.clearCallingIdentity();	 Catch:{ all -> 0x03d3 }
        r3 = r22.getNextArg();	 Catch:{ all -> 0x03d3 }
        r8 = 0;
        r20 = 0;
        if (r3 == 0) goto L_0x034a;
    L_0x0338:
        r2 = "deep";	 Catch:{ all -> 0x03d3 }
        r2 = r2.equals(r3);	 Catch:{ all -> 0x03d3 }
        if (r2 != 0) goto L_0x034a;	 Catch:{ all -> 0x03d3 }
    L_0x0341:
        r2 = "all";	 Catch:{ all -> 0x03d3 }
        r2 = r2.equals(r3);	 Catch:{ all -> 0x03d3 }
        if (r2 == 0) goto L_0x0360;	 Catch:{ all -> 0x03d3 }
    L_0x034a:
        r20 = 1;	 Catch:{ all -> 0x03d3 }
        r0 = r21;	 Catch:{ all -> 0x03d3 }
        r2 = r0.mDeepEnabled;	 Catch:{ all -> 0x03d3 }
        if (r2 == 0) goto L_0x0360;	 Catch:{ all -> 0x03d3 }
    L_0x0352:
        r2 = 0;	 Catch:{ all -> 0x03d3 }
        r0 = r21;	 Catch:{ all -> 0x03d3 }
        r0.mDeepEnabled = r2;	 Catch:{ all -> 0x03d3 }
        r8 = 1;	 Catch:{ all -> 0x03d3 }
        r2 = "Deep idle mode disabled";	 Catch:{ all -> 0x03d3 }
        r0 = r16;	 Catch:{ all -> 0x03d3 }
        r0.println(r2);	 Catch:{ all -> 0x03d3 }
    L_0x0360:
        if (r3 == 0) goto L_0x0374;	 Catch:{ all -> 0x03d3 }
    L_0x0362:
        r2 = "light";	 Catch:{ all -> 0x03d3 }
        r2 = r2.equals(r3);	 Catch:{ all -> 0x03d3 }
        if (r2 != 0) goto L_0x0374;	 Catch:{ all -> 0x03d3 }
    L_0x036b:
        r2 = "all";	 Catch:{ all -> 0x03d3 }
        r2 = r2.equals(r3);	 Catch:{ all -> 0x03d3 }
        if (r2 == 0) goto L_0x038a;	 Catch:{ all -> 0x03d3 }
    L_0x0374:
        r20 = 1;	 Catch:{ all -> 0x03d3 }
        r0 = r21;	 Catch:{ all -> 0x03d3 }
        r2 = r0.mLightEnabled;	 Catch:{ all -> 0x03d3 }
        if (r2 == 0) goto L_0x038a;	 Catch:{ all -> 0x03d3 }
    L_0x037c:
        r2 = 0;	 Catch:{ all -> 0x03d3 }
        r0 = r21;	 Catch:{ all -> 0x03d3 }
        r0.mLightEnabled = r2;	 Catch:{ all -> 0x03d3 }
        r8 = 1;	 Catch:{ all -> 0x03d3 }
        r2 = "Light idle mode disabled";	 Catch:{ all -> 0x03d3 }
        r0 = r16;	 Catch:{ all -> 0x03d3 }
        r0.println(r2);	 Catch:{ all -> 0x03d3 }
    L_0x038a:
        if (r8 == 0) goto L_0x03ae;	 Catch:{ all -> 0x03d3 }
    L_0x038c:
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x03d3 }
        r4.<init>();	 Catch:{ all -> 0x03d3 }
        if (r3 != 0) goto L_0x03d1;	 Catch:{ all -> 0x03d3 }
    L_0x0393:
        r2 = "all";	 Catch:{ all -> 0x03d3 }
    L_0x0396:
        r2 = r4.append(r2);	 Catch:{ all -> 0x03d3 }
        r4 = "-disabled";	 Catch:{ all -> 0x03d3 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x03d3 }
        r2 = r2.toString();	 Catch:{ all -> 0x03d3 }
        r4 = android.os.Process.myUid();	 Catch:{ all -> 0x03d3 }
        r0 = r21;	 Catch:{ all -> 0x03d3 }
        r0.becomeActiveLocked(r2, r4);	 Catch:{ all -> 0x03d3 }
    L_0x03ae:
        if (r20 != 0) goto L_0x03c9;	 Catch:{ all -> 0x03d3 }
    L_0x03b0:
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x03d3 }
        r2.<init>();	 Catch:{ all -> 0x03d3 }
        r4 = "Unknown idle mode: ";	 Catch:{ all -> 0x03d3 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x03d3 }
        r2 = r2.append(r3);	 Catch:{ all -> 0x03d3 }
        r2 = r2.toString();	 Catch:{ all -> 0x03d3 }
        r0 = r16;	 Catch:{ all -> 0x03d3 }
        r0.println(r2);	 Catch:{ all -> 0x03d3 }
    L_0x03c9:
        android.os.Binder.restoreCallingIdentity(r18);	 Catch:{ all -> 0x03d3 }
        goto L_0x004e;
    L_0x03ce:
        r2 = move-exception;
        monitor-exit(r21);
        throw r2;
    L_0x03d1:
        r2 = r3;
        goto L_0x0396;
    L_0x03d3:
        r2 = move-exception;
        android.os.Binder.restoreCallingIdentity(r18);	 Catch:{ all -> 0x03d3 }
        throw r2;	 Catch:{ all -> 0x03d3 }
    L_0x03d8:
        r2 = "enable";
        r0 = r23;
        r2 = r2.equals(r0);
        if (r2 == 0) goto L_0x047b;
    L_0x03e3:
        r2 = r21.getContext();
        r4 = "android.permission.DEVICE_POWER";
        r5 = 0;
        r2.enforceCallingOrSelfPermission(r4, r5);
        monitor-enter(r21);
        r18 = android.os.Binder.clearCallingIdentity();	 Catch:{ all -> 0x0476 }
        r3 = r22.getNextArg();	 Catch:{ all -> 0x0476 }
        r9 = 0;
        r20 = 0;
        if (r3 == 0) goto L_0x040e;
    L_0x03fc:
        r2 = "deep";	 Catch:{ all -> 0x0476 }
        r2 = r2.equals(r3);	 Catch:{ all -> 0x0476 }
        if (r2 != 0) goto L_0x040e;	 Catch:{ all -> 0x0476 }
    L_0x0405:
        r2 = "all";	 Catch:{ all -> 0x0476 }
        r2 = r2.equals(r3);	 Catch:{ all -> 0x0476 }
        if (r2 == 0) goto L_0x0424;	 Catch:{ all -> 0x0476 }
    L_0x040e:
        r20 = 1;	 Catch:{ all -> 0x0476 }
        r0 = r21;	 Catch:{ all -> 0x0476 }
        r2 = r0.mDeepEnabled;	 Catch:{ all -> 0x0476 }
        if (r2 != 0) goto L_0x0424;	 Catch:{ all -> 0x0476 }
    L_0x0416:
        r2 = 1;	 Catch:{ all -> 0x0476 }
        r0 = r21;	 Catch:{ all -> 0x0476 }
        r0.mDeepEnabled = r2;	 Catch:{ all -> 0x0476 }
        r9 = 1;	 Catch:{ all -> 0x0476 }
        r2 = "Deep idle mode enabled";	 Catch:{ all -> 0x0476 }
        r0 = r16;	 Catch:{ all -> 0x0476 }
        r0.println(r2);	 Catch:{ all -> 0x0476 }
    L_0x0424:
        if (r3 == 0) goto L_0x0438;	 Catch:{ all -> 0x0476 }
    L_0x0426:
        r2 = "light";	 Catch:{ all -> 0x0476 }
        r2 = r2.equals(r3);	 Catch:{ all -> 0x0476 }
        if (r2 != 0) goto L_0x0438;	 Catch:{ all -> 0x0476 }
    L_0x042f:
        r2 = "all";	 Catch:{ all -> 0x0476 }
        r2 = r2.equals(r3);	 Catch:{ all -> 0x0476 }
        if (r2 == 0) goto L_0x044e;	 Catch:{ all -> 0x0476 }
    L_0x0438:
        r20 = 1;	 Catch:{ all -> 0x0476 }
        r0 = r21;	 Catch:{ all -> 0x0476 }
        r2 = r0.mLightEnabled;	 Catch:{ all -> 0x0476 }
        if (r2 != 0) goto L_0x044e;	 Catch:{ all -> 0x0476 }
    L_0x0440:
        r2 = 1;	 Catch:{ all -> 0x0476 }
        r0 = r21;	 Catch:{ all -> 0x0476 }
        r0.mLightEnabled = r2;	 Catch:{ all -> 0x0476 }
        r9 = 1;	 Catch:{ all -> 0x0476 }
        r2 = "Light idle mode enable";	 Catch:{ all -> 0x0476 }
        r0 = r16;	 Catch:{ all -> 0x0476 }
        r0.println(r2);	 Catch:{ all -> 0x0476 }
    L_0x044e:
        if (r9 == 0) goto L_0x0453;	 Catch:{ all -> 0x0476 }
    L_0x0450:
        r21.becomeInactiveIfAppropriateLocked();	 Catch:{ all -> 0x0476 }
    L_0x0453:
        if (r20 != 0) goto L_0x046e;	 Catch:{ all -> 0x0476 }
    L_0x0455:
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0476 }
        r2.<init>();	 Catch:{ all -> 0x0476 }
        r4 = "Unknown idle mode: ";	 Catch:{ all -> 0x0476 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0476 }
        r2 = r2.append(r3);	 Catch:{ all -> 0x0476 }
        r2 = r2.toString();	 Catch:{ all -> 0x0476 }
        r0 = r16;	 Catch:{ all -> 0x0476 }
        r0.println(r2);	 Catch:{ all -> 0x0476 }
    L_0x046e:
        android.os.Binder.restoreCallingIdentity(r18);	 Catch:{ all -> 0x0476 }
        goto L_0x004e;
    L_0x0473:
        r2 = move-exception;
        monitor-exit(r21);
        throw r2;
    L_0x0476:
        r2 = move-exception;
        android.os.Binder.restoreCallingIdentity(r18);	 Catch:{ all -> 0x0476 }
        throw r2;	 Catch:{ all -> 0x0476 }
    L_0x047b:
        r2 = "enabled";
        r0 = r23;
        r2 = r2.equals(r0);
        if (r2 == 0) goto L_0x050e;
    L_0x0486:
        monitor-enter(r21);
        r3 = r22.getNextArg();
        if (r3 == 0) goto L_0x0496;
    L_0x048d:
        r2 = "all";
        r2 = r2.equals(r3);
        if (r2 == 0) goto L_0x04b5;
    L_0x0496:
        r0 = r21;
        r2 = r0.mDeepEnabled;
        if (r2 == 0) goto L_0x04af;
    L_0x049c:
        r0 = r21;
        r2 = r0.mLightEnabled;
        if (r2 == 0) goto L_0x04af;
    L_0x04a2:
        r2 = "1";
    L_0x04a5:
        r0 = r16;
        r0.println(r2);
        goto L_0x004e;
    L_0x04ac:
        r2 = move-exception;
        monitor-exit(r21);
        throw r2;
    L_0x04af:
        r2 = 0;
        r2 = java.lang.Integer.valueOf(r2);
        goto L_0x04a5;
    L_0x04b5:
        r2 = "deep";
        r2 = r2.equals(r3);
        if (r2 == 0) goto L_0x04d4;
    L_0x04be:
        r0 = r21;
        r2 = r0.mDeepEnabled;
        if (r2 == 0) goto L_0x04ce;
    L_0x04c4:
        r2 = "1";
    L_0x04c7:
        r0 = r16;
        r0.println(r2);
        goto L_0x004e;
    L_0x04ce:
        r2 = 0;
        r2 = java.lang.Integer.valueOf(r2);
        goto L_0x04c7;
    L_0x04d4:
        r2 = "light";
        r2 = r2.equals(r3);
        if (r2 == 0) goto L_0x04f3;
    L_0x04dd:
        r0 = r21;
        r2 = r0.mLightEnabled;
        if (r2 == 0) goto L_0x04ed;
    L_0x04e3:
        r2 = "1";
    L_0x04e6:
        r0 = r16;
        r0.println(r2);
        goto L_0x004e;
    L_0x04ed:
        r2 = 0;
        r2 = java.lang.Integer.valueOf(r2);
        goto L_0x04e6;
    L_0x04f3:
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r4 = "Unknown idle mode: ";
        r2 = r2.append(r4);
        r2 = r2.append(r3);
        r2 = r2.toString();
        r0 = r16;
        r0.println(r2);
        goto L_0x004e;
    L_0x050e:
        r2 = "whitelist";
        r0 = r23;
        r2 = r2.equals(r0);
        if (r2 == 0) goto L_0x06b9;
    L_0x0519:
        r3 = r22.getNextArg();
        if (r3 == 0) goto L_0x05f9;
    L_0x051f:
        r2 = r21.getContext();
        r4 = "android.permission.DEVICE_POWER";
        r5 = 0;
        r2.enforceCallingOrSelfPermission(r4, r5);
        r18 = android.os.Binder.clearCallingIdentity();
    L_0x052e:
        r2 = r3.length();	 Catch:{ all -> 0x05c2 }
        r4 = 1;	 Catch:{ all -> 0x05c2 }
        if (r2 < r4) goto L_0x0550;	 Catch:{ all -> 0x05c2 }
    L_0x0535:
        r2 = 0;	 Catch:{ all -> 0x05c2 }
        r2 = r3.charAt(r2);	 Catch:{ all -> 0x05c2 }
        r4 = 45;	 Catch:{ all -> 0x05c2 }
        if (r2 == r4) goto L_0x056e;	 Catch:{ all -> 0x05c2 }
    L_0x053e:
        r2 = 0;	 Catch:{ all -> 0x05c2 }
        r2 = r3.charAt(r2);	 Catch:{ all -> 0x05c2 }
        r4 = 43;	 Catch:{ all -> 0x05c2 }
        if (r2 == r4) goto L_0x056e;	 Catch:{ all -> 0x05c2 }
    L_0x0547:
        r2 = 0;	 Catch:{ all -> 0x05c2 }
        r2 = r3.charAt(r2);	 Catch:{ all -> 0x05c2 }
        r4 = 61;	 Catch:{ all -> 0x05c2 }
        if (r2 == r4) goto L_0x056e;	 Catch:{ all -> 0x05c2 }
    L_0x0550:
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x05c2 }
        r2.<init>();	 Catch:{ all -> 0x05c2 }
        r4 = "Package must be prefixed with +, -, or =: ";	 Catch:{ all -> 0x05c2 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x05c2 }
        r2 = r2.append(r3);	 Catch:{ all -> 0x05c2 }
        r2 = r2.toString();	 Catch:{ all -> 0x05c2 }
        r0 = r16;	 Catch:{ all -> 0x05c2 }
        r0.println(r2);	 Catch:{ all -> 0x05c2 }
        r2 = -1;
        android.os.Binder.restoreCallingIdentity(r18);
        return r2;
    L_0x056e:
        r2 = 0;
        r13 = r3.charAt(r2);	 Catch:{ all -> 0x05c2 }
        r2 = 1;	 Catch:{ all -> 0x05c2 }
        r15 = r3.substring(r2);	 Catch:{ all -> 0x05c2 }
        r2 = 43;	 Catch:{ all -> 0x05c2 }
        if (r13 != r2) goto L_0x05c7;	 Catch:{ all -> 0x05c2 }
    L_0x057c:
        r0 = r21;	 Catch:{ all -> 0x05c2 }
        r2 = r0.addPowerSaveWhitelistAppInternal(r15);	 Catch:{ all -> 0x05c2 }
        if (r2 == 0) goto L_0x05a8;	 Catch:{ all -> 0x05c2 }
    L_0x0584:
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x05c2 }
        r2.<init>();	 Catch:{ all -> 0x05c2 }
        r4 = "Added: ";	 Catch:{ all -> 0x05c2 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x05c2 }
        r2 = r2.append(r15);	 Catch:{ all -> 0x05c2 }
        r2 = r2.toString();	 Catch:{ all -> 0x05c2 }
        r0 = r16;	 Catch:{ all -> 0x05c2 }
        r0.println(r2);	 Catch:{ all -> 0x05c2 }
    L_0x059d:
        r3 = r22.getNextArg();	 Catch:{ all -> 0x05c2 }
        if (r3 != 0) goto L_0x052e;
    L_0x05a3:
        android.os.Binder.restoreCallingIdentity(r18);
        goto L_0x004f;
    L_0x05a8:
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x05c2 }
        r2.<init>();	 Catch:{ all -> 0x05c2 }
        r4 = "Unknown package: ";	 Catch:{ all -> 0x05c2 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x05c2 }
        r2 = r2.append(r15);	 Catch:{ all -> 0x05c2 }
        r2 = r2.toString();	 Catch:{ all -> 0x05c2 }
        r0 = r16;	 Catch:{ all -> 0x05c2 }
        r0.println(r2);	 Catch:{ all -> 0x05c2 }
        goto L_0x059d;
    L_0x05c2:
        r2 = move-exception;
        android.os.Binder.restoreCallingIdentity(r18);
        throw r2;
    L_0x05c7:
        r2 = 45;
        if (r13 != r2) goto L_0x05ed;
    L_0x05cb:
        r0 = r21;	 Catch:{ all -> 0x05c2 }
        r2 = r0.removePowerSaveWhitelistAppInternal(r15);	 Catch:{ all -> 0x05c2 }
        if (r2 == 0) goto L_0x059d;	 Catch:{ all -> 0x05c2 }
    L_0x05d3:
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x05c2 }
        r2.<init>();	 Catch:{ all -> 0x05c2 }
        r4 = "Removed: ";	 Catch:{ all -> 0x05c2 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x05c2 }
        r2 = r2.append(r15);	 Catch:{ all -> 0x05c2 }
        r2 = r2.toString();	 Catch:{ all -> 0x05c2 }
        r0 = r16;	 Catch:{ all -> 0x05c2 }
        r0.println(r2);	 Catch:{ all -> 0x05c2 }
        goto L_0x059d;	 Catch:{ all -> 0x05c2 }
    L_0x05ed:
        r0 = r21;	 Catch:{ all -> 0x05c2 }
        r2 = r0.getPowerSaveWhitelistAppInternal(r15);	 Catch:{ all -> 0x05c2 }
        r0 = r16;	 Catch:{ all -> 0x05c2 }
        r0.println(r2);	 Catch:{ all -> 0x05c2 }
        goto L_0x059d;
    L_0x05f9:
        monitor-enter(r21);
        r18 = android.os.Binder.clearCallingIdentity();	 Catch:{ all -> 0x06b4 }
        r12 = 0;
    L_0x05ff:
        r0 = r21;	 Catch:{ all -> 0x06b4 }
        r2 = r0.mPowerSaveWhitelistAppsExceptIdle;	 Catch:{ all -> 0x06b4 }
        r2 = r2.size();	 Catch:{ all -> 0x06b4 }
        if (r12 >= r2) goto L_0x0638;	 Catch:{ all -> 0x06b4 }
    L_0x0609:
        r2 = "system-excidle,";	 Catch:{ all -> 0x06b4 }
        r0 = r16;	 Catch:{ all -> 0x06b4 }
        r0.print(r2);	 Catch:{ all -> 0x06b4 }
        r0 = r21;	 Catch:{ all -> 0x06b4 }
        r2 = r0.mPowerSaveWhitelistAppsExceptIdle;	 Catch:{ all -> 0x06b4 }
        r2 = r2.keyAt(r12);	 Catch:{ all -> 0x06b4 }
        r2 = (java.lang.String) r2;	 Catch:{ all -> 0x06b4 }
        r0 = r16;	 Catch:{ all -> 0x06b4 }
        r0.print(r2);	 Catch:{ all -> 0x06b4 }
        r2 = ",";	 Catch:{ all -> 0x06b4 }
        r0 = r16;	 Catch:{ all -> 0x06b4 }
        r0.print(r2);	 Catch:{ all -> 0x06b4 }
        r0 = r21;	 Catch:{ all -> 0x06b4 }
        r2 = r0.mPowerSaveWhitelistAppsExceptIdle;	 Catch:{ all -> 0x06b4 }
        r2 = r2.valueAt(r12);	 Catch:{ all -> 0x06b4 }
        r0 = r16;	 Catch:{ all -> 0x06b4 }
        r0.println(r2);	 Catch:{ all -> 0x06b4 }
        r12 = r12 + 1;	 Catch:{ all -> 0x06b4 }
        goto L_0x05ff;	 Catch:{ all -> 0x06b4 }
    L_0x0638:
        r12 = 0;	 Catch:{ all -> 0x06b4 }
    L_0x0639:
        r0 = r21;	 Catch:{ all -> 0x06b4 }
        r2 = r0.mPowerSaveWhitelistApps;	 Catch:{ all -> 0x06b4 }
        r2 = r2.size();	 Catch:{ all -> 0x06b4 }
        if (r12 >= r2) goto L_0x0672;	 Catch:{ all -> 0x06b4 }
    L_0x0643:
        r2 = "system,";	 Catch:{ all -> 0x06b4 }
        r0 = r16;	 Catch:{ all -> 0x06b4 }
        r0.print(r2);	 Catch:{ all -> 0x06b4 }
        r0 = r21;	 Catch:{ all -> 0x06b4 }
        r2 = r0.mPowerSaveWhitelistApps;	 Catch:{ all -> 0x06b4 }
        r2 = r2.keyAt(r12);	 Catch:{ all -> 0x06b4 }
        r2 = (java.lang.String) r2;	 Catch:{ all -> 0x06b4 }
        r0 = r16;	 Catch:{ all -> 0x06b4 }
        r0.print(r2);	 Catch:{ all -> 0x06b4 }
        r2 = ",";	 Catch:{ all -> 0x06b4 }
        r0 = r16;	 Catch:{ all -> 0x06b4 }
        r0.print(r2);	 Catch:{ all -> 0x06b4 }
        r0 = r21;	 Catch:{ all -> 0x06b4 }
        r2 = r0.mPowerSaveWhitelistApps;	 Catch:{ all -> 0x06b4 }
        r2 = r2.valueAt(r12);	 Catch:{ all -> 0x06b4 }
        r0 = r16;	 Catch:{ all -> 0x06b4 }
        r0.println(r2);	 Catch:{ all -> 0x06b4 }
        r12 = r12 + 1;	 Catch:{ all -> 0x06b4 }
        goto L_0x0639;	 Catch:{ all -> 0x06b4 }
    L_0x0672:
        r12 = 0;	 Catch:{ all -> 0x06b4 }
    L_0x0673:
        r0 = r21;	 Catch:{ all -> 0x06b4 }
        r2 = r0.mPowerSaveWhitelistUserApps;	 Catch:{ all -> 0x06b4 }
        r2 = r2.size();	 Catch:{ all -> 0x06b4 }
        if (r12 >= r2) goto L_0x06ac;	 Catch:{ all -> 0x06b4 }
    L_0x067d:
        r2 = "user,";	 Catch:{ all -> 0x06b4 }
        r0 = r16;	 Catch:{ all -> 0x06b4 }
        r0.print(r2);	 Catch:{ all -> 0x06b4 }
        r0 = r21;	 Catch:{ all -> 0x06b4 }
        r2 = r0.mPowerSaveWhitelistUserApps;	 Catch:{ all -> 0x06b4 }
        r2 = r2.keyAt(r12);	 Catch:{ all -> 0x06b4 }
        r2 = (java.lang.String) r2;	 Catch:{ all -> 0x06b4 }
        r0 = r16;	 Catch:{ all -> 0x06b4 }
        r0.print(r2);	 Catch:{ all -> 0x06b4 }
        r2 = ",";	 Catch:{ all -> 0x06b4 }
        r0 = r16;	 Catch:{ all -> 0x06b4 }
        r0.print(r2);	 Catch:{ all -> 0x06b4 }
        r0 = r21;	 Catch:{ all -> 0x06b4 }
        r2 = r0.mPowerSaveWhitelistUserApps;	 Catch:{ all -> 0x06b4 }
        r2 = r2.valueAt(r12);	 Catch:{ all -> 0x06b4 }
        r0 = r16;	 Catch:{ all -> 0x06b4 }
        r0.println(r2);	 Catch:{ all -> 0x06b4 }
        r12 = r12 + 1;
        goto L_0x0673;
    L_0x06ac:
        android.os.Binder.restoreCallingIdentity(r18);
        goto L_0x004e;
    L_0x06b1:
        r2 = move-exception;
        monitor-exit(r21);
        throw r2;
    L_0x06b4:
        r2 = move-exception;
        android.os.Binder.restoreCallingIdentity(r18);	 Catch:{ all -> 0x06b4 }
        throw r2;	 Catch:{ all -> 0x06b4 }
    L_0x06b9:
        r2 = "tempwhitelist";
        r0 = r23;
        r2 = r2.equals(r0);
        if (r2 == 0) goto L_0x072a;
    L_0x06c4:
        r14 = r22.getNextOption();
        if (r14 == 0) goto L_0x06ec;
    L_0x06ca:
        r2 = "-u";
        r2 = r2.equals(r14);
        if (r2 == 0) goto L_0x06c4;
    L_0x06d3:
        r14 = r22.getNextArg();
        if (r14 != 0) goto L_0x06e3;
    L_0x06d9:
        r2 = "-u requires a user number";
        r0 = r16;
        r0.println(r2);
        r2 = -1;
        return r2;
    L_0x06e3:
        r2 = java.lang.Integer.parseInt(r14);
        r0 = r22;
        r0.userId = r2;
        goto L_0x06c4;
    L_0x06ec:
        r3 = r22.getNextArg();
        if (r3 == 0) goto L_0x0720;
    L_0x06f2:
        r0 = r22;	 Catch:{ RemoteException -> 0x0702 }
        r6 = r0.userId;	 Catch:{ RemoteException -> 0x0702 }
        r7 = "shell";	 Catch:{ RemoteException -> 0x0702 }
        r4 = 10000; // 0x2710 float:1.4013E-41 double:4.9407E-320;	 Catch:{ RemoteException -> 0x0702 }
        r2 = r21;	 Catch:{ RemoteException -> 0x0702 }
        r2.addPowerSaveTempWhitelistAppChecked(r3, r4, r6, r7);	 Catch:{ RemoteException -> 0x0702 }
        goto L_0x004f;
    L_0x0702:
        r17 = move-exception;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r4 = "Failed: ";
        r2 = r2.append(r4);
        r0 = r17;
        r2 = r2.append(r0);
        r2 = r2.toString();
        r0 = r16;
        r0.println(r2);
        goto L_0x004f;
    L_0x0720:
        r2 = 0;
        r0 = r21;
        r1 = r16;
        r0.dumpTempWhitelistSchedule(r1, r2);
        goto L_0x004f;
    L_0x072a:
        r2 = r22.handleDefaultCommands(r23);
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.DeviceIdleController.onShellCommand(com.android.server.DeviceIdleController$Shell, java.lang.String):int");
    }

    private static String stateToString(int state) {
        switch (state) {
            case STATE_ACTIVE /*0*/:
                return "ACTIVE";
            case STATE_INACTIVE /*1*/:
                return "INACTIVE";
            case STATE_IDLE_PENDING /*2*/:
                return "IDLE_PENDING";
            case STATE_SENSING /*3*/:
                return "SENSING";
            case STATE_LOCATING /*4*/:
                return "LOCATING";
            case STATE_IDLE /*5*/:
                return "IDLE";
            case STATE_IDLE_MAINTENANCE /*6*/:
                return "IDLE_MAINTENANCE";
            default:
                return Integer.toString(state);
        }
    }

    private static String lightStateToString(int state) {
        switch (state) {
            case STATE_ACTIVE /*0*/:
                return "ACTIVE";
            case STATE_INACTIVE /*1*/:
                return "INACTIVE";
            case STATE_SENSING /*3*/:
                return "PRE_IDLE";
            case STATE_LOCATING /*4*/:
                return "IDLE";
            case STATE_IDLE /*5*/:
                return "WAITING_FOR_NETWORK";
            case STATE_IDLE_MAINTENANCE /*6*/:
                return "IDLE_MAINTENANCE";
            case MSG_REPORT_MAINTENANCE_ACTIVITY /*7*/:
                return "OVERRIDE";
            default:
                return Integer.toString(state);
        }
    }

    private void addEvent(int cmd) {
        if (this.mEventCmds[STATE_ACTIVE] != cmd) {
            System.arraycopy(this.mEventCmds, STATE_ACTIVE, this.mEventCmds, STATE_INACTIVE, 99);
            System.arraycopy(this.mEventTimes, STATE_ACTIVE, this.mEventTimes, STATE_INACTIVE, 99);
            this.mEventCmds[STATE_ACTIVE] = cmd;
            this.mEventTimes[STATE_ACTIVE] = SystemClock.elapsedRealtime();
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
        if (result == STATE_INACTIVE) {
            if (DEBUG) {
                Slog.d(TAG, "RESULT_MOVED received.");
            }
            synchronized (this) {
                handleMotionDetectedLocked(this.mConstants.INACTIVE_TIMEOUT, "sense_motion");
            }
        } else if (result == 0) {
            if (DEBUG) {
                Slog.d(TAG, "RESULT_STATIONARY received.");
            }
            if (this.mState == STATE_SENSING) {
                synchronized (this) {
                    this.mNotMoving = true;
                    stepIdleStateLocked("s:stationary");
                }
            } else if (this.mState == STATE_LOCATING) {
                synchronized (this) {
                    this.mNotMoving = true;
                    if (this.mLocated) {
                        stepIdleStateLocked("s:stationary");
                    }
                }
            } else {
                return;
            }
        } else {
            return;
        }
    }

    public DeviceIdleController(Context context) {
        super(context);
        this.mMaintenanceActivityListeners = new RemoteCallbackList();
        this.mPowerSaveWhitelistAppsExceptIdle = new ArrayMap();
        this.mPowerSaveWhitelistApps = new ArrayMap();
        this.mPowerSaveWhitelistUserApps = new ArrayMap();
        this.mPowerSaveWhitelistSystemAppIdsExceptIdle = new SparseBooleanArray();
        this.mPowerSaveWhitelistSystemAppIds = new SparseBooleanArray();
        this.mPowerSaveWhitelistExceptIdleAppIds = new SparseBooleanArray();
        this.mPowerSaveWhitelistExceptIdleAppIdArray = new int[STATE_ACTIVE];
        this.mPowerSaveWhitelistAllAppIds = new SparseBooleanArray();
        this.mPowerSaveWhitelistAllAppIdArray = new int[STATE_ACTIVE];
        this.mPowerSaveWhitelistUserAppIds = new SparseBooleanArray();
        this.mPowerSaveWhitelistUserAppIdArray = new int[STATE_ACTIVE];
        this.mTempWhitelistAppIdEndTimes = new SparseArray();
        this.mTempWhitelistAppIdArray = new int[STATE_ACTIVE];
        this.mEventCmds = new int[EVENT_BUFFER_SIZE];
        this.mEventTimes = new long[EVENT_BUFFER_SIZE];
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean z = DeviceIdleController.COMPRESS_TIME;
                String action = intent.getAction();
                if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    DeviceIdleController.this.updateConnectivityState(intent);
                } else if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                    synchronized (DeviceIdleController.this) {
                        int plugged = intent.getIntExtra("plugged", DeviceIdleController.STATE_ACTIVE);
                        DeviceIdleController deviceIdleController = DeviceIdleController.this;
                        if (plugged != 0) {
                            z = true;
                        }
                        deviceIdleController.updateChargingLocked(z);
                    }
                } else if (action.equals("android.intent.action.PACKAGE_REMOVED") && !intent.getBooleanExtra("android.intent.extra.REPLACING", DeviceIdleController.COMPRESS_TIME)) {
                    Uri data = intent.getData();
                    if (data != null) {
                        String ssp = data.getSchemeSpecificPart();
                        if (ssp != null) {
                            DeviceIdleController.this.removePowerSaveWhitelistAppInternal(ssp);
                        }
                    }
                }
            }
        };
        this.mLightAlarmListener = new OnAlarmListener() {
            public void onAlarm() {
                synchronized (DeviceIdleController.this) {
                    DeviceIdleController.this.stepLightIdleStateLocked("s:alarm");
                }
            }
        };
        this.mSensingTimeoutAlarmListener = new OnAlarmListener() {
            public void onAlarm() {
                if (DeviceIdleController.this.mState == DeviceIdleController.STATE_SENSING) {
                    synchronized (DeviceIdleController.this) {
                        DeviceIdleController.this.becomeInactiveIfAppropriateLocked();
                    }
                }
            }
        };
        this.mDeepAlarmListener = new OnAlarmListener() {
            public void onAlarm() {
                synchronized (DeviceIdleController.this) {
                    DeviceIdleController.this.stepIdleStateLocked("s:alarm");
                }
            }
        };
        this.mIdleStartedDoneReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(intent.getAction())) {
                    DeviceIdleController.this.mHandler.sendEmptyMessageDelayed(DeviceIdleController.MSG_FINISH_IDLE_OP, DeviceIdleController.this.mConstants.MIN_DEEP_MAINTENANCE_TIME);
                } else {
                    DeviceIdleController.this.mHandler.sendEmptyMessageDelayed(DeviceIdleController.MSG_FINISH_IDLE_OP, DeviceIdleController.this.mConstants.MIN_LIGHT_MAINTENANCE_TIME);
                }
            }
        };
        this.mDisplayListener = new DisplayListener() {
            public void onDisplayAdded(int displayId) {
            }

            public void onDisplayRemoved(int displayId) {
            }

            public void onDisplayChanged(int displayId) {
                if (displayId == 0) {
                    synchronized (DeviceIdleController.this) {
                        DeviceIdleController.this.updateDisplayLocked();
                    }
                }
            }
        };
        this.mMotionListener = new MotionListener();
        this.mGenericLocationListener = new LocationListener() {
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
        this.mGpsLocationListener = new LocationListener() {
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
        this.mConfigFile = new AtomicFile(new File(getSystemDir(), "deviceidle.xml"));
        this.mHandler = new MyHandler(BackgroundThread.getHandler().getLooper());
    }

    int[] getPowerSaveWhitelistUserAppIds() {
        int[] iArr;
        synchronized (this) {
            iArr = this.mPowerSaveWhitelistUserAppIdArray;
        }
        return iArr;
    }

    private static File getSystemDir() {
        return new File(Environment.getDataDirectory(), "system");
    }

    public void onStart() {
        PackageManager pm = getContext().getPackageManager();
        synchronized (this) {
            int i;
            boolean z = getContext().getResources().getBoolean(17956883);
            this.mDeepEnabled = z;
            this.mLightEnabled = z;
            SystemConfig sysConfig = SystemConfig.getInstance();
            ArraySet<String> allowPowerExceptIdle = sysConfig.getAllowInPowerSaveExceptIdle();
            for (i = STATE_ACTIVE; i < allowPowerExceptIdle.size(); i += STATE_INACTIVE) {
                try {
                    ApplicationInfo ai = pm.getApplicationInfo((String) allowPowerExceptIdle.valueAt(i), DumpState.DUMP_DEXOPT);
                    int appid = UserHandle.getAppId(ai.uid);
                    this.mPowerSaveWhitelistAppsExceptIdle.put(ai.packageName, Integer.valueOf(appid));
                    this.mPowerSaveWhitelistSystemAppIdsExceptIdle.put(appid, true);
                } catch (NameNotFoundException e) {
                }
            }
            ArraySet<String> allowPower = sysConfig.getAllowInPowerSave();
            for (i = STATE_ACTIVE; i < allowPower.size(); i += STATE_INACTIVE) {
                try {
                    ai = pm.getApplicationInfo((String) allowPower.valueAt(i), DumpState.DUMP_DEXOPT);
                    appid = UserHandle.getAppId(ai.uid);
                    this.mPowerSaveWhitelistAppsExceptIdle.put(ai.packageName, Integer.valueOf(appid));
                    this.mPowerSaveWhitelistSystemAppIdsExceptIdle.put(appid, true);
                    this.mPowerSaveWhitelistApps.put(ai.packageName, Integer.valueOf(appid));
                    this.mPowerSaveWhitelistSystemAppIds.put(appid, true);
                } catch (NameNotFoundException e2) {
                }
            }
            this.mConstants = new Constants(this.mHandler, getContext().getContentResolver());
            this.mNetworkConnected = true;
            this.mScreenOn = true;
            this.mCharging = true;
            this.mState = STATE_ACTIVE;
            this.mLightState = STATE_ACTIVE;
            this.mInactiveTimeout = this.mConstants.INACTIVE_TIMEOUT;
        }
        this.mBinderService = new BinderService();
        publishBinderService("deviceidle", this.mBinderService);
        publishLocalService(LocalService.class, new LocalService());
    }

    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_SYSTEM_SERVICES_READY) {
            synchronized (this) {
                this.mAlarmManager = (AlarmManager) getContext().getSystemService("alarm");
                this.mBatteryStats = BatteryStatsService.getService();
                this.mLocalPowerManager = (PowerManagerInternal) getLocalService(PowerManagerInternal.class);
                this.mPowerManager = (PowerManager) getContext().getSystemService(PowerManager.class);
                this.mActiveIdleWakeLock = this.mPowerManager.newWakeLock(STATE_INACTIVE, "deviceidle_maint");
                this.mActiveIdleWakeLock.setReferenceCounted(COMPRESS_TIME);
                this.mConnectivityService = (ConnectivityService) ServiceManager.getService("connectivity");
                this.mLocalAlarmManager = (com.android.server.AlarmManagerService.LocalService) getLocalService(com.android.server.AlarmManagerService.LocalService.class);
                this.mNetworkPolicyManager = INetworkPolicyManager.Stub.asInterface(ServiceManager.getService("netpolicy"));
                this.mDisplayManager = (DisplayManager) getContext().getSystemService("display");
                this.mSensorManager = (SensorManager) getContext().getSystemService("sensor");
                int sigMotionSensorId = getContext().getResources().getInteger(17694733);
                if (sigMotionSensorId > 0) {
                    this.mMotionSensor = this.mSensorManager.getDefaultSensor(sigMotionSensorId, true);
                }
                if (this.mMotionSensor == null && getContext().getResources().getBoolean(17956884)) {
                    this.mMotionSensor = this.mSensorManager.getDefaultSensor(26, true);
                }
                if (this.mMotionSensor == null) {
                    this.mMotionSensor = this.mSensorManager.getDefaultSensor(17, true);
                }
                if (getContext().getResources().getBoolean(17956885)) {
                    this.mLocationManager = (LocationManager) getContext().getSystemService("location");
                    this.mLocationRequest = new LocationRequest().setQuality(EVENT_BUFFER_SIZE).setInterval(0).setFastestInterval(0).setNumUpdates(STATE_INACTIVE);
                }
                this.mAnyMotionDetector = new AnyMotionDetector((PowerManager) getContext().getSystemService("power"), this.mHandler, this.mSensorManager, this, ((float) getContext().getResources().getInteger(17694732)) / 100.0f);
                this.mIdleIntent = new Intent("android.os.action.DEVICE_IDLE_MODE_CHANGED");
                this.mIdleIntent.addFlags(1342177280);
                this.mLightIdleIntent = new Intent("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED");
                this.mLightIdleIntent.addFlags(1342177280);
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.BATTERY_CHANGED");
                getContext().registerReceiver(this.mReceiver, filter);
                filter = new IntentFilter();
                filter.addAction("android.intent.action.PACKAGE_REMOVED");
                filter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
                getContext().registerReceiver(this.mReceiver, filter);
                filter = new IntentFilter();
                filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                getContext().registerReceiver(this.mReceiver, filter);
                this.mLocalPowerManager.setDeviceIdleWhitelist(this.mPowerSaveWhitelistAllAppIdArray);
                this.mLocalAlarmManager.setDeviceIdleUserWhitelist(this.mPowerSaveWhitelistUserAppIdArray);
                this.mDisplayManager.registerDisplayListener(this.mDisplayListener, null);
                updateDisplayLocked();
            }
            updateConnectivityState(null);
        } else if (phase == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            Slog.d(TAG, "PHASE_BOOT_COMPLETED");
            this.mHandler.postDelayed(new Runnable() {
                private static final int MAX_TRY_TIMES = 3;
                private int count;

                {
                    this.count = DeviceIdleController.STATE_ACTIVE;
                }

                public void run() {
                    this.count += DeviceIdleController.STATE_INACTIVE;
                    if (!DeviceIdleController.this.updateWhitelistFromDB(this.count >= MAX_TRY_TIMES ? true : DeviceIdleController.COMPRESS_TIME) && this.count < MAX_TRY_TIMES) {
                        DeviceIdleController.this.mHandler.postDelayed(this, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                    }
                }
            }, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        }
    }

    public boolean addPowerSaveWhitelistAppInternal(String name) {
        synchronized (this) {
            try {
                if (this.mPowerSaveWhitelistUserApps.put(name, Integer.valueOf(UserHandle.getAppId(getContext().getPackageManager().getApplicationInfo(name, DumpState.DUMP_PREFERRED_XML).uid))) == null) {
                    reportPowerSaveWhitelistChangedLocked();
                    updateWhitelistAppIdsLocked();
                    writeConfigFileLocked();
                }
            } catch (NameNotFoundException e) {
                return COMPRESS_TIME;
            }
        }
        return true;
    }

    public boolean removePowerSaveWhitelistAppInternal(String name) {
        synchronized (this) {
            if (this.mPowerSaveWhitelistUserApps.remove(name) != null) {
                reportPowerSaveWhitelistChangedLocked();
                updateWhitelistAppIdsLocked();
                writeConfigFileLocked();
                return true;
            }
            return COMPRESS_TIME;
        }
    }

    public boolean getPowerSaveWhitelistAppInternal(String name) {
        boolean containsKey;
        synchronized (this) {
            containsKey = this.mPowerSaveWhitelistUserApps.containsKey(name);
        }
        return containsKey;
    }

    public String[] getSystemPowerWhitelistExceptIdleInternal() {
        String[] apps;
        synchronized (this) {
            int size = this.mPowerSaveWhitelistAppsExceptIdle.size();
            apps = new String[size];
            for (int i = STATE_ACTIVE; i < size; i += STATE_INACTIVE) {
                apps[i] = (String) this.mPowerSaveWhitelistAppsExceptIdle.keyAt(i);
            }
        }
        return apps;
    }

    public String[] getSystemPowerWhitelistInternal() {
        String[] apps;
        synchronized (this) {
            int size = this.mPowerSaveWhitelistApps.size();
            apps = new String[size];
            for (int i = STATE_ACTIVE; i < size; i += STATE_INACTIVE) {
                apps[i] = (String) this.mPowerSaveWhitelistApps.keyAt(i);
            }
        }
        return apps;
    }

    public String[] getUserPowerWhitelistInternal() {
        String[] apps;
        synchronized (this) {
            apps = new String[this.mPowerSaveWhitelistUserApps.size()];
            for (int i = STATE_ACTIVE; i < this.mPowerSaveWhitelistUserApps.size(); i += STATE_INACTIVE) {
                apps[i] = (String) this.mPowerSaveWhitelistUserApps.keyAt(i);
            }
        }
        return apps;
    }

    public String[] getFullPowerWhitelistExceptIdleInternal() {
        String[] apps;
        synchronized (this) {
            int i;
            apps = new String[(this.mPowerSaveWhitelistAppsExceptIdle.size() + this.mPowerSaveWhitelistUserApps.size())];
            int cur = STATE_ACTIVE;
            for (i = STATE_ACTIVE; i < this.mPowerSaveWhitelistAppsExceptIdle.size(); i += STATE_INACTIVE) {
                apps[cur] = (String) this.mPowerSaveWhitelistAppsExceptIdle.keyAt(i);
                cur += STATE_INACTIVE;
            }
            for (i = STATE_ACTIVE; i < this.mPowerSaveWhitelistUserApps.size(); i += STATE_INACTIVE) {
                apps[cur] = (String) this.mPowerSaveWhitelistUserApps.keyAt(i);
                cur += STATE_INACTIVE;
            }
        }
        return apps;
    }

    public String[] getFullPowerWhitelistInternal() {
        String[] apps;
        synchronized (this) {
            int i;
            apps = new String[(this.mPowerSaveWhitelistApps.size() + this.mPowerSaveWhitelistUserApps.size())];
            int cur = STATE_ACTIVE;
            for (i = STATE_ACTIVE; i < this.mPowerSaveWhitelistApps.size(); i += STATE_INACTIVE) {
                apps[cur] = (String) this.mPowerSaveWhitelistApps.keyAt(i);
                cur += STATE_INACTIVE;
            }
            for (i = STATE_ACTIVE; i < this.mPowerSaveWhitelistUserApps.size(); i += STATE_INACTIVE) {
                apps[cur] = (String) this.mPowerSaveWhitelistUserApps.keyAt(i);
                cur += STATE_INACTIVE;
            }
        }
        return apps;
    }

    public boolean isPowerSaveWhitelistExceptIdleAppInternal(String packageName) {
        boolean z;
        synchronized (this) {
            if (this.mPowerSaveWhitelistAppsExceptIdle.containsKey(packageName)) {
                z = true;
            } else {
                z = this.mPowerSaveWhitelistUserApps.containsKey(packageName);
            }
        }
        return z;
    }

    public boolean isPowerSaveWhitelistAppInternal(String packageName) {
        boolean z;
        synchronized (this) {
            if (this.mPowerSaveWhitelistApps.containsKey(packageName)) {
                z = true;
            } else {
                z = this.mPowerSaveWhitelistUserApps.containsKey(packageName);
            }
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

    void addPowerSaveTempWhitelistAppChecked(String packageName, long duration, int userId, String reason) throws RemoteException {
        getContext().enforceCallingPermission("android.permission.CHANGE_DEVICE_IDLE_TEMP_WHITELIST", "No permission to change device idle whitelist");
        int callingUid = Binder.getCallingUid();
        userId = ActivityManagerNative.getDefault().handleIncomingUser(Binder.getCallingPid(), callingUid, userId, COMPRESS_TIME, COMPRESS_TIME, "addPowerSaveTempWhitelistApp", null);
        long token = Binder.clearCallingIdentity();
        try {
            addPowerSaveTempWhitelistAppInternal(callingUid, packageName, duration, userId, true, reason);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    void addPowerSaveTempWhitelistAppInternal(int callingUid, String packageName, long duration, int userId, boolean sync, String reason) {
        try {
            addPowerSaveTempWhitelistAppDirectInternal(callingUid, UserHandle.getAppId(getContext().getPackageManager().getPackageUidAsUser(packageName, userId)), duration, sync, reason);
        } catch (NameNotFoundException e) {
        }
    }

    void addPowerSaveTempWhitelistAppDirectInternal(int callingUid, int appId, long duration, boolean sync, String reason) {
        long timeNow = SystemClock.elapsedRealtime();
        Runnable runnable = null;
        synchronized (this) {
            int callingAppId = UserHandle.getAppId(callingUid);
            if (callingAppId < READ_DB_DELAY_TIME || this.mPowerSaveWhitelistSystemAppIds.get(callingAppId)) {
                duration = Math.min(duration, this.mConstants.MAX_TEMP_APP_WHITELIST_DURATION);
                Pair<MutableLong, String> entry = (Pair) this.mTempWhitelistAppIdEndTimes.get(appId);
                boolean newEntry = entry == null ? true : COMPRESS_TIME;
                if (newEntry) {
                    entry = new Pair(new MutableLong(0), reason);
                    this.mTempWhitelistAppIdEndTimes.put(appId, entry);
                }
                ((MutableLong) entry.first).value = timeNow + duration;
                if (DEBUG) {
                    Slog.d(TAG, "Adding AppId " + appId + " to temp whitelist. New entry: " + newEntry);
                }
                if (newEntry) {
                    try {
                        this.mBatteryStats.noteEvent(32785, reason, appId);
                    } catch (RemoteException e) {
                    }
                    postTempActiveTimeoutMessage(appId, duration);
                    updateTempWhitelistAppIdsLocked();
                    if (this.mNetworkPolicyTempWhitelistCallback != null) {
                        if (sync) {
                            runnable = this.mNetworkPolicyTempWhitelistCallback;
                        } else {
                            this.mHandler.post(this.mNetworkPolicyTempWhitelistCallback);
                        }
                    }
                    reportTempWhitelistChangedLocked();
                }
            } else {
                throw new SecurityException("Calling app " + UserHandle.formatUid(callingUid) + " is not on whitelist");
            }
        }
        if (runnable != null) {
            runnable.run();
        }
    }

    public void setNetworkPolicyTempWhitelistCallbackInternal(Runnable callback) {
        synchronized (this) {
            this.mNetworkPolicyTempWhitelistCallback = callback;
        }
    }

    private void postTempActiveTimeoutMessage(int uid, long delay) {
        if (DEBUG) {
            Slog.d(TAG, "postTempActiveTimeoutMessage: uid=" + uid + ", delay=" + delay);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(STATE_IDLE_MAINTENANCE, uid, STATE_ACTIVE), delay);
    }

    void checkTempAppWhitelistTimeout(int uid) {
        long timeNow = SystemClock.elapsedRealtime();
        if (DEBUG) {
            Slog.d(TAG, "checkTempAppWhitelistTimeout: uid=" + uid + ", timeNow=" + timeNow);
        }
        synchronized (this) {
            Pair<MutableLong, String> entry = (Pair) this.mTempWhitelistAppIdEndTimes.get(uid);
            if (entry == null) {
                return;
            }
            if (timeNow >= ((MutableLong) entry.first).value) {
                this.mTempWhitelistAppIdEndTimes.delete(uid);
                if (DEBUG) {
                    Slog.d(TAG, "Removing UID " + uid + " from temp whitelist");
                }
                updateTempWhitelistAppIdsLocked();
                if (this.mNetworkPolicyTempWhitelistCallback != null) {
                    this.mHandler.post(this.mNetworkPolicyTempWhitelistCallback);
                }
                reportTempWhitelistChangedLocked();
                try {
                    this.mBatteryStats.noteEvent(16401, (String) entry.second, uid);
                } catch (RemoteException e) {
                }
            } else {
                if (DEBUG) {
                    Slog.d(TAG, "Time to remove UID " + uid + ": " + ((MutableLong) entry.first).value);
                }
                postTempActiveTimeoutMessage(uid, ((MutableLong) entry.first).value - timeNow);
            }
        }
    }

    public void exitIdleInternal(String reason) {
        synchronized (this) {
            becomeActiveLocked(reason, Binder.getCallingUid());
        }
    }

    void updateConnectivityState(Intent connIntent) {
        synchronized (this) {
            ConnectivityService cm = this.mConnectivityService;
        }
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            synchronized (this) {
                boolean z;
                if (ni == null) {
                    z = COMPRESS_TIME;
                } else if (connIntent == null) {
                    z = ni.isConnected();
                } else {
                    if (ni.getType() != connIntent.getIntExtra("networkType", -1)) {
                        return;
                    }
                    z = connIntent.getBooleanExtra("noConnectivity", COMPRESS_TIME) ? COMPRESS_TIME : true;
                }
                if (z != this.mNetworkConnected) {
                    this.mNetworkConnected = z;
                    if (z && this.mLightState == STATE_IDLE) {
                        stepLightIdleStateLocked("network");
                    }
                }
            }
        }
    }

    void updateDisplayLocked() {
        this.mCurDisplay = this.mDisplayManager.getDisplay(STATE_ACTIVE);
        boolean screenOn = this.mCurDisplay.getState() == STATE_IDLE_PENDING ? true : COMPRESS_TIME;
        if (DEBUG) {
            Slog.d(TAG, "updateDisplayLocked: screenOn=" + screenOn);
        }
        if (!screenOn && this.mScreenOn) {
            this.mScreenOn = COMPRESS_TIME;
            if (!this.mForceIdle) {
                becomeInactiveIfAppropriateLocked();
            }
        } else if (screenOn) {
            this.mScreenOn = true;
            if (!this.mForceIdle) {
                becomeActiveLocked("screen", Process.myUid());
            }
        }
    }

    void updateChargingLocked(boolean charging) {
        if (DEBUG) {
            Slog.i(TAG, "updateChargingLocked: charging=" + charging);
        }
        if (!charging && this.mCharging) {
            this.mCharging = COMPRESS_TIME;
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

    void scheduleReportActiveLocked(String activeReason, int activeUid) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(STATE_IDLE, activeUid, STATE_ACTIVE, activeReason));
    }

    void becomeActiveLocked(String activeReason, int activeUid) {
        if (DEBUG) {
            Slog.i(TAG, "becomeActiveLocked, reason = " + activeReason);
        }
        if (this.mState != 0 || this.mLightState != 0) {
            EventLogTags.writeDeviceIdle(STATE_ACTIVE, activeReason);
            EventLogTags.writeDeviceIdleLight(STATE_ACTIVE, activeReason);
            scheduleReportActiveLocked(activeReason, activeUid);
            this.mState = STATE_ACTIVE;
            this.mLightState = STATE_ACTIVE;
            this.mInactiveTimeout = this.mConstants.INACTIVE_TIMEOUT;
            this.mCurIdleBudget = 0;
            this.mMaintenanceStartTime = 0;
            resetIdleManagementLocked();
            resetLightIdleManagementLocked();
            addEvent(STATE_INACTIVE);
        }
    }

    void becomeInactiveIfAppropriateLocked() {
        if (DEBUG) {
            Slog.d(TAG, "becomeInactiveIfAppropriateLocked()");
        }
        if ((!this.mScreenOn && !this.mCharging) || this.mForceIdle) {
            if (this.mState == 0 && this.mDeepEnabled) {
                this.mState = STATE_INACTIVE;
                if (DEBUG) {
                    Slog.d(TAG, "Moved from STATE_ACTIVE to STATE_INACTIVE");
                }
                resetIdleManagementLocked();
                scheduleAlarmLocked(this.mInactiveTimeout, COMPRESS_TIME);
                EventLogTags.writeDeviceIdle(this.mState, "no activity");
            }
            if (this.mLightState == 0 && this.mLightEnabled) {
                this.mLightState = STATE_INACTIVE;
                if (DEBUG) {
                    Slog.d(TAG, "Moved from LIGHT_STATE_ACTIVE to LIGHT_STATE_INACTIVE");
                }
                resetLightIdleManagementLocked();
                scheduleLightAlarmLocked(this.mConstants.LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT);
                EventLogTags.writeDeviceIdleLight(this.mLightState, "no activity");
            }
        }
    }

    void resetIdleManagementLocked() {
        this.mNextIdlePendingDelay = 0;
        this.mNextIdleDelay = 0;
        this.mNextLightIdleDelay = 0;
        cancelAlarmLocked();
        cancelSensingTimeoutAlarmLocked();
        cancelLocatingLocked();
        stopMonitoringMotionLocked();
        this.mAnyMotionDetector.stop();
    }

    void resetLightIdleManagementLocked() {
        cancelLightAlarmLocked();
    }

    void exitForceIdleLocked() {
        if (this.mForceIdle) {
            this.mForceIdle = COMPRESS_TIME;
            if (this.mScreenOn || this.mCharging) {
                becomeActiveLocked("exit-force", Process.myUid());
            }
        }
    }

    void stepLightIdleStateLocked(String reason) {
        if (this.mLightState != MSG_REPORT_MAINTENANCE_ACTIVITY) {
            if (DEBUG) {
                Slog.d(TAG, "stepLightIdleStateLocked: mLightState=" + this.mLightState);
            }
            EventLogTags.writeDeviceIdleLightStep();
            switch (this.mLightState) {
                case STATE_INACTIVE /*1*/:
                    this.mCurIdleBudget = this.mConstants.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET;
                    this.mNextLightIdleDelay = this.mConstants.LIGHT_IDLE_TIMEOUT;
                    this.mMaintenanceStartTime = 0;
                    if (!isOpsInactiveLocked()) {
                        this.mLightState = STATE_SENSING;
                        EventLogTags.writeDeviceIdleLight(this.mLightState, reason);
                        scheduleLightAlarmLocked(this.mConstants.LIGHT_PRE_IDLE_TIMEOUT);
                        break;
                    }
                case STATE_SENSING /*3*/:
                case STATE_IDLE_MAINTENANCE /*6*/:
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
                    this.mLightState = STATE_LOCATING;
                    EventLogTags.writeDeviceIdleLight(this.mLightState, reason);
                    addEvent(STATE_IDLE_PENDING);
                    this.mHandler.sendEmptyMessage(STATE_SENSING);
                    break;
                case STATE_LOCATING /*4*/:
                case STATE_IDLE /*5*/:
                    if (!this.mNetworkConnected && this.mLightState != STATE_IDLE) {
                        scheduleLightAlarmLocked(this.mNextLightIdleDelay);
                        if (DEBUG) {
                            Slog.d(TAG, "Moved to LIGHT_WAITING_FOR_NETWORK.");
                        }
                        this.mLightState = STATE_IDLE;
                        EventLogTags.writeDeviceIdleLight(this.mLightState, reason);
                        break;
                    }
                    this.mActiveIdleOpCount = STATE_INACTIVE;
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
                    this.mLightState = STATE_IDLE_MAINTENANCE;
                    EventLogTags.writeDeviceIdleLight(this.mLightState, reason);
                    addEvent(STATE_SENSING);
                    this.mHandler.sendEmptyMessage(STATE_LOCATING);
                    break;
                    break;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void stepIdleStateLocked(String reason) {
        if (DEBUG) {
            Slog.d(TAG, "stepIdleStateLocked: mState=" + this.mState);
        }
        EventLogTags.writeDeviceIdleStep();
        if (this.mConstants.MIN_TIME_TO_ALARM + SystemClock.elapsedRealtime() <= this.mAlarmManager.getNextWakeFromIdleTime()) {
            switch (this.mState) {
                case STATE_INACTIVE /*1*/:
                    startMonitoringMotionLocked();
                    scheduleAlarmLocked(this.mConstants.IDLE_AFTER_INACTIVE_TIMEOUT, COMPRESS_TIME);
                    this.mNextIdlePendingDelay = this.mConstants.IDLE_PENDING_TIMEOUT;
                    this.mNextIdleDelay = this.mConstants.IDLE_TIMEOUT;
                    this.mState = STATE_IDLE_PENDING;
                    if (DEBUG) {
                        Slog.d(TAG, "Moved from STATE_INACTIVE to STATE_IDLE_PENDING.");
                    }
                    EventLogTags.writeDeviceIdle(this.mState, reason);
                    break;
                case STATE_IDLE_PENDING /*2*/:
                    this.mState = STATE_SENSING;
                    if (DEBUG) {
                        Slog.d(TAG, "Moved from STATE_IDLE_PENDING to STATE_SENSING.");
                    }
                    EventLogTags.writeDeviceIdle(this.mState, reason);
                    scheduleSensingTimeoutAlarmLocked(this.mConstants.SENSING_TIMEOUT);
                    cancelLocatingLocked();
                    this.mNotMoving = COMPRESS_TIME;
                    this.mLocated = COMPRESS_TIME;
                    this.mLastGenericLocation = null;
                    this.mLastGpsLocation = null;
                    if (!this.mForceIdle) {
                        this.mAnyMotionDetector.checkForAnyMotion();
                        break;
                    }
                    break;
                case STATE_SENSING /*3*/:
                    cancelSensingTimeoutAlarmLocked();
                    this.mState = STATE_LOCATING;
                    if (DEBUG) {
                        Slog.d(TAG, "Moved from STATE_SENSING to STATE_LOCATING.");
                    }
                    EventLogTags.writeDeviceIdle(this.mState, reason);
                    scheduleAlarmLocked(this.mConstants.LOCATING_TIMEOUT, COMPRESS_TIME);
                    if (this.mForceIdle) {
                        if (DEBUG) {
                            Slog.d(TAG, "forceidle, not check locating");
                            break;
                        }
                    }
                    if (this.mLocationManager == null || this.mLocationManager.getProvider("network") == null) {
                        this.mHasNetworkLocation = COMPRESS_TIME;
                    } else {
                        this.mLocationManager.requestLocationUpdates(this.mLocationRequest, this.mGenericLocationListener, this.mHandler.getLooper());
                        this.mLocating = true;
                    }
                    if (this.mLocationManager == null || this.mLocationManager.getProvider("gps") == null) {
                        this.mHasGps = COMPRESS_TIME;
                    } else {
                        this.mHasGps = true;
                        this.mLocationManager.requestLocationUpdates("gps", 1000, 5.0f, this.mGpsLocationListener, this.mHandler.getLooper());
                        this.mLocating = true;
                    }
                    break;
                    break;
                case STATE_LOCATING /*4*/:
                    break;
                case STATE_IDLE /*5*/:
                    this.mActiveIdleOpCount = STATE_INACTIVE;
                    this.mActiveIdleWakeLock.acquire();
                    scheduleAlarmLocked(this.mNextIdlePendingDelay, COMPRESS_TIME);
                    if (DEBUG) {
                        Slog.d(TAG, "Moved from STATE_IDLE to STATE_IDLE_MAINTENANCE. Next alarm in " + this.mNextIdlePendingDelay + " ms.");
                    }
                    this.mMaintenanceStartTime = SystemClock.elapsedRealtime();
                    this.mNextIdlePendingDelay = Math.min(this.mConstants.MAX_IDLE_PENDING_TIMEOUT, (long) (((float) this.mNextIdlePendingDelay) * this.mConstants.IDLE_PENDING_FACTOR));
                    if (this.mNextIdlePendingDelay < this.mConstants.IDLE_PENDING_TIMEOUT) {
                        this.mNextIdlePendingDelay = this.mConstants.IDLE_PENDING_TIMEOUT;
                    }
                    this.mState = STATE_IDLE_MAINTENANCE;
                    EventLogTags.writeDeviceIdle(this.mState, reason);
                    addEvent(STATE_IDLE);
                    this.mHandler.sendEmptyMessage(STATE_LOCATING);
                    break;
                case STATE_IDLE_MAINTENANCE /*6*/:
                    break;
            }
        }
        if (this.mState != 0) {
            becomeActiveLocked("alarm", Process.myUid());
            becomeInactiveIfAppropriateLocked();
        }
    }

    void incActiveIdleOps() {
        synchronized (this) {
            this.mActiveIdleOpCount += STATE_INACTIVE;
        }
    }

    void decActiveIdleOps() {
        synchronized (this) {
            this.mActiveIdleOpCount--;
            if (this.mActiveIdleOpCount <= 0) {
                exitMaintenanceEarlyIfNeededLocked();
                this.mActiveIdleWakeLock.release();
            }
        }
    }

    void setJobsActive(boolean active) {
        synchronized (this) {
            this.mJobsActive = active;
            reportMaintenanceActivityIfNeededLocked();
            if (!active) {
                exitMaintenanceEarlyIfNeededLocked();
            }
        }
    }

    void setAlarmsActive(boolean active) {
        synchronized (this) {
            this.mAlarmsActive = active;
            if (!active) {
                exitMaintenanceEarlyIfNeededLocked();
            }
        }
    }

    boolean registerMaintenanceActivityListener(IMaintenanceActivityListener listener) {
        boolean z;
        synchronized (this) {
            this.mMaintenanceActivityListeners.register(listener);
            z = this.mReportedMaintenanceActivity;
        }
        return z;
    }

    void unregisterMaintenanceActivityListener(IMaintenanceActivityListener listener) {
        synchronized (this) {
            this.mMaintenanceActivityListeners.unregister(listener);
        }
    }

    void reportMaintenanceActivityIfNeededLocked() {
        boolean active = this.mJobsActive;
        if (active != this.mReportedMaintenanceActivity) {
            int i;
            this.mReportedMaintenanceActivity = active;
            MyHandler myHandler = this.mHandler;
            if (this.mReportedMaintenanceActivity) {
                i = STATE_INACTIVE;
            } else {
                i = STATE_ACTIVE;
            }
            this.mHandler.sendMessage(myHandler.obtainMessage(MSG_REPORT_MAINTENANCE_ACTIVITY, i, STATE_ACTIVE));
        }
    }

    boolean isOpsInactiveLocked() {
        return (this.mActiveIdleOpCount > 0 || this.mJobsActive || this.mAlarmsActive) ? COMPRESS_TIME : true;
    }

    void exitMaintenanceEarlyIfNeededLocked() {
        if (!(this.mState == STATE_IDLE_MAINTENANCE || this.mLightState == STATE_IDLE_MAINTENANCE)) {
            if (this.mLightState != STATE_SENSING) {
                return;
            }
        }
        if (isOpsInactiveLocked()) {
            long now = SystemClock.elapsedRealtime();
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("Exit: start=");
                TimeUtils.formatDuration(this.mMaintenanceStartTime, sb);
                sb.append(" now=");
                TimeUtils.formatDuration(now, sb);
                Slog.d(TAG, sb.toString());
            }
            if (this.mState == STATE_IDLE_MAINTENANCE) {
                stepIdleStateLocked("s:early");
            } else if (this.mLightState == STATE_SENSING) {
                stepLightIdleStateLocked("s:predone");
            } else {
                stepLightIdleStateLocked("s:early");
            }
        }
    }

    void motionLocked() {
        if (DEBUG) {
            Slog.d(TAG, "motionLocked()");
        }
        handleMotionDetectedLocked(this.mConstants.MOTION_INACTIVE_TIMEOUT, "motion");
    }

    void handleMotionDetectedLocked(long timeout, String type) {
        boolean becomeInactive = COMPRESS_TIME;
        if (this.mState != 0) {
            scheduleReportActiveLocked(type, Process.myUid());
            this.mState = STATE_ACTIVE;
            this.mInactiveTimeout = timeout;
            this.mCurIdleBudget = 0;
            this.mMaintenanceStartTime = 0;
            EventLogTags.writeDeviceIdle(this.mState, type);
            addEvent(STATE_INACTIVE);
            becomeInactive = true;
        }
        if (this.mLightState == MSG_REPORT_MAINTENANCE_ACTIVITY) {
            this.mLightState = STATE_ACTIVE;
            EventLogTags.writeDeviceIdleLight(this.mLightState, type);
            becomeInactive = true;
        }
        if (becomeInactive) {
            becomeInactiveIfAppropriateLocked();
        }
    }

    void receivedGenericLocationLocked(Location location) {
        if (this.mState != STATE_LOCATING) {
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

    void receivedGpsLocationLocked(Location location) {
        if (this.mState != STATE_LOCATING) {
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

    void startMonitoringMotionLocked() {
        if (DEBUG) {
            Slog.d(TAG, "startMonitoringMotionLocked()");
        }
        if (this.mMotionSensor != null && !this.mMotionListener.active) {
            this.mMotionListener.registerLocked();
        }
    }

    void stopMonitoringMotionLocked() {
        if (DEBUG) {
            Slog.d(TAG, "stopMonitoringMotionLocked()");
        }
        if (this.mMotionSensor != null && this.mMotionListener.active) {
            this.mMotionListener.unregisterLocked();
        }
    }

    void cancelAlarmLocked() {
        if (this.mNextAlarmTime != 0) {
            this.mNextAlarmTime = 0;
            this.mAlarmManager.cancel(this.mDeepAlarmListener);
        }
    }

    void cancelLightAlarmLocked() {
        if (this.mNextLightAlarmTime != 0) {
            this.mNextLightAlarmTime = 0;
            this.mAlarmManager.cancel(this.mLightAlarmListener);
        }
    }

    void cancelLocatingLocked() {
        if (this.mLocating) {
            this.mLocationManager.removeUpdates(this.mGenericLocationListener);
            this.mLocationManager.removeUpdates(this.mGpsLocationListener);
            this.mLocating = COMPRESS_TIME;
        }
    }

    void cancelSensingTimeoutAlarmLocked() {
        if (this.mNextSensingTimeoutAlarmTime != 0) {
            this.mNextSensingTimeoutAlarmTime = 0;
            this.mAlarmManager.cancel(this.mSensingTimeoutAlarmListener);
        }
    }

    void scheduleAlarmLocked(long delay, boolean idleUntil) {
        if (DEBUG) {
            Slog.d(TAG, "scheduleAlarmLocked(" + delay + ", " + idleUntil + ")");
        }
        if (this.mMotionSensor != null) {
            this.mNextAlarmTime = SystemClock.elapsedRealtime() + delay;
            if (idleUntil) {
                this.mAlarmManager.setIdleUntil(STATE_IDLE_PENDING, this.mNextAlarmTime, "DeviceIdleController.deep", this.mDeepAlarmListener, this.mHandler);
            } else {
                this.mAlarmManager.set(STATE_IDLE_PENDING, this.mNextAlarmTime, "DeviceIdleController.deep", this.mDeepAlarmListener, this.mHandler);
            }
        }
    }

    void scheduleLightAlarmLocked(long delay) {
        if (DEBUG) {
            Slog.d(TAG, "scheduleLightAlarmLocked(" + delay + ")");
        }
        this.mNextLightAlarmTime = SystemClock.elapsedRealtime() + delay;
        this.mAlarmManager.set(STATE_IDLE_PENDING, this.mNextLightAlarmTime, "DeviceIdleController.light", this.mLightAlarmListener, this.mHandler);
    }

    void scheduleSensingTimeoutAlarmLocked(long delay) {
        if (DEBUG) {
            Slog.d(TAG, "scheduleSensingAlarmLocked(" + delay + ")");
        }
        this.mNextSensingTimeoutAlarmTime = SystemClock.elapsedRealtime() + delay;
        this.mAlarmManager.set(STATE_IDLE_PENDING, this.mNextSensingTimeoutAlarmTime, "DeviceIdleController.sensing", this.mSensingTimeoutAlarmListener, this.mHandler);
    }

    private static int[] buildAppIdArray(ArrayMap<String, Integer> systemApps, ArrayMap<String, Integer> userApps, SparseBooleanArray outAppIds) {
        int i;
        outAppIds.clear();
        if (systemApps != null) {
            for (i = STATE_ACTIVE; i < systemApps.size(); i += STATE_INACTIVE) {
                outAppIds.put(((Integer) systemApps.valueAt(i)).intValue(), true);
            }
        }
        if (userApps != null) {
            for (i = STATE_ACTIVE; i < userApps.size(); i += STATE_INACTIVE) {
                outAppIds.put(((Integer) userApps.valueAt(i)).intValue(), true);
            }
        }
        int size = outAppIds.size();
        int[] appids = new int[size];
        for (i = STATE_ACTIVE; i < size; i += STATE_INACTIVE) {
            appids[i] = outAppIds.keyAt(i);
        }
        return appids;
    }

    private void updateWhitelistAppIdsLocked() {
        this.mPowerSaveWhitelistExceptIdleAppIdArray = buildAppIdArray(this.mPowerSaveWhitelistAppsExceptIdle, this.mPowerSaveWhitelistUserApps, this.mPowerSaveWhitelistExceptIdleAppIds);
        this.mPowerSaveWhitelistAllAppIdArray = buildAppIdArray(this.mPowerSaveWhitelistApps, this.mPowerSaveWhitelistUserApps, this.mPowerSaveWhitelistAllAppIds);
        this.mPowerSaveWhitelistUserAppIdArray = buildAppIdArray(null, this.mPowerSaveWhitelistUserApps, this.mPowerSaveWhitelistUserAppIds);
        if (this.mLocalPowerManager != null) {
            if (DEBUG) {
                Slog.d(TAG, "Setting wakelock whitelist to " + Arrays.toString(this.mPowerSaveWhitelistAllAppIdArray));
            }
            this.mLocalPowerManager.setDeviceIdleWhitelist(this.mPowerSaveWhitelistAllAppIdArray);
        }
        if (this.mLocalAlarmManager != null) {
            if (DEBUG) {
                Slog.d(TAG, "Setting alarm whitelist to " + Arrays.toString(this.mPowerSaveWhitelistUserAppIdArray));
            }
            this.mLocalAlarmManager.setDeviceIdleUserWhitelist(this.mPowerSaveWhitelistUserAppIdArray);
        }
    }

    private void updateTempWhitelistAppIdsLocked() {
        int size = this.mTempWhitelistAppIdEndTimes.size();
        if (this.mTempWhitelistAppIdArray.length != size) {
            this.mTempWhitelistAppIdArray = new int[size];
        }
        for (int i = STATE_ACTIVE; i < size; i += STATE_INACTIVE) {
            this.mTempWhitelistAppIdArray[i] = this.mTempWhitelistAppIdEndTimes.keyAt(i);
        }
        if (this.mLocalPowerManager != null) {
            if (DEBUG) {
                Slog.d(TAG, "Setting wakelock temp whitelist to " + Arrays.toString(this.mTempWhitelistAppIdArray));
            }
            this.mLocalPowerManager.setDeviceIdleTempWhitelist(this.mTempWhitelistAppIdArray);
        }
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void readConfigFileLocked() {
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
            } catch (Throwable th) {
                try {
                    stream.close();
                } catch (IOException e3) {
                }
            }
        } catch (FileNotFoundException e4) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean updateWhitelistFromDB(boolean ignoreDbNotExist) {
        PackageManager pm = getContext().getPackageManager();
        Bundle bundle = null;
        ArrayList arrayList = null;
        try {
            Slog.d(TAG, "begin to read unprotectlist from DB");
            bundle = getContext().getContentResolver().call(Uri.parse("content://com.huawei.android.smartpowerprovider"), "hsm_get_freeze_list", "unprotect", null);
        } catch (Exception e) {
            Slog.d(TAG, "read unprotectlist fail:" + e);
            if (!ignoreDbNotExist) {
                return COMPRESS_TIME;
            }
        }
        if (bundle != null) {
            arrayList = bundle.getStringArrayList("frz_unprotect");
            Slog.d(TAG, "unprotect list: " + arrayList);
        } else {
            Slog.d(TAG, "read unprotectlist wrong , Bundle is null");
        }
        List<PackageInfo> packages = pm.getInstalledPackages(DumpState.DUMP_PREFERRED_XML);
        synchronized (this) {
            int i = STATE_ACTIVE;
            while (true) {
                if (i < packages.size()) {
                    String pkgName = ((PackageInfo) packages.get(i)).packageName;
                    if (!this.mPowerSaveWhitelistUserApps.containsKey(pkgName)) {
                        try {
                            ApplicationInfo ai = pm.getApplicationInfo(pkgName, DumpState.DUMP_PREFERRED_XML);
                            if (arrayList == null || !arrayList.contains(pkgName)) {
                                this.mPowerSaveWhitelistUserApps.put(ai.packageName, Integer.valueOf(UserHandle.getAppId(ai.uid)));
                            }
                        } catch (NameNotFoundException e2) {
                            Slog.d(TAG, "NameNotFound: " + pkgName);
                        }
                    }
                    i += STATE_INACTIVE;
                } else {
                    updateWhitelistAppIdsLocked();
                    reportPowerSaveWhitelistChangedLocked();
                }
            }
        }
        return true;
    }

    private void readConfigFileLocked(XmlPullParser parser) {
        int type;
        PackageManager pm = getContext().getPackageManager();
        do {
            try {
                type = parser.next();
                if (type == STATE_IDLE_PENDING) {
                    break;
                }
            } catch (IllegalStateException e) {
                Slog.w(TAG, "Failed parsing config " + e);
                return;
            } catch (NullPointerException e2) {
                Slog.w(TAG, "Failed parsing config " + e2);
                return;
            } catch (NumberFormatException e3) {
                Slog.w(TAG, "Failed parsing config " + e3);
                return;
            } catch (XmlPullParserException e4) {
                Slog.w(TAG, "Failed parsing config " + e4);
                return;
            } catch (IOException e5) {
                Slog.w(TAG, "Failed parsing config " + e5);
                return;
            } catch (IndexOutOfBoundsException e6) {
                Slog.w(TAG, "Failed parsing config " + e6);
                return;
            }
        } while (type != STATE_INACTIVE);
        if (type != STATE_IDLE_PENDING) {
            throw new IllegalStateException("no start tag found");
        }
        int outerDepth = parser.getDepth();
        while (true) {
            type = parser.next();
            if (type == STATE_INACTIVE) {
                return;
            }
            if (type == STATE_SENSING && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == STATE_SENSING || type == STATE_LOCATING)) {
                if (parser.getName().equals("wl")) {
                    String name = parser.getAttributeValue(null, "n");
                    if (name != null) {
                        try {
                            ApplicationInfo ai = pm.getApplicationInfo(name, DumpState.DUMP_PREFERRED_XML);
                            this.mPowerSaveWhitelistUserApps.put(ai.packageName, Integer.valueOf(UserHandle.getAppId(ai.uid)));
                        } catch (NameNotFoundException e7) {
                        }
                    }
                } else {
                    Slog.w(TAG, "Unknown element under <config>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    void writeConfigFileLocked() {
        this.mHandler.removeMessages(STATE_INACTIVE);
        this.mHandler.sendEmptyMessageDelayed(STATE_INACTIVE, 5000);
    }

    void handleWriteConfigFile() {
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
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = this.mConfigFile.startWrite();
                memStream.writeTo(fileOutputStream);
                fileOutputStream.flush();
                FileUtils.sync(fileOutputStream);
                fileOutputStream.close();
                this.mConfigFile.finishWrite(fileOutputStream);
            } catch (IOException e2) {
                Slog.w(TAG, "Error writing config file", e2);
                this.mConfigFile.failWrite(fileOutputStream);
            }
        }
    }

    void writeConfigFileLocked(XmlSerializer out) throws IOException {
        out.startDocument(null, Boolean.valueOf(true));
        out.startTag(null, "config");
        for (int i = STATE_ACTIVE; i < this.mPowerSaveWhitelistUserApps.size(); i += STATE_INACTIVE) {
            String name = (String) this.mPowerSaveWhitelistUserApps.keyAt(i);
            out.startTag(null, "wl");
            out.attribute(null, "n", name);
            out.endTag(null, "wl");
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
        pw.println("  tempwhitelist");
        pw.println("    Print packages that are temporarily whitelisted.");
        pw.println("  tempwhitelist [-u] [package ..]");
        pw.println("    Temporarily place packages in whitelist for 10 seconds.");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump DeviceIdleController from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            return;
        }
        int i;
        if (args != null) {
            int userId = STATE_ACTIVE;
            i = STATE_ACTIVE;
            while (i < args.length) {
                String arg = args[i];
                if ("-h".equals(arg)) {
                    dumpHelp(pw);
                    return;
                }
                if ("-u".equals(arg)) {
                    i += STATE_INACTIVE;
                    if (i < args.length) {
                        userId = Integer.parseInt(args[i]);
                    }
                } else if (!"-a".equals(arg)) {
                    if (arg.length() <= 0 || arg.charAt(STATE_ACTIVE) != '-') {
                        Shell shell = new Shell();
                        shell.userId = userId;
                        String[] newArgs = new String[(args.length - i)];
                        System.arraycopy(args, i, newArgs, STATE_ACTIVE, args.length - i);
                        shell.exec(this.mBinderService, null, fd, null, newArgs, new ResultReceiver(null));
                        return;
                    }
                    pw.println("Unknown option: " + arg);
                    return;
                }
                i += STATE_INACTIVE;
            }
        }
        synchronized (this) {
            this.mConstants.dump(pw);
            if (this.mEventCmds[STATE_ACTIVE] != 0) {
                pw.println("  Idling history:");
                long now = SystemClock.elapsedRealtime();
                i = 99;
                while (i >= 0) {
                    if (this.mEventCmds[i] != 0) {
                        String label;
                        switch (this.mEventCmds[i]) {
                            case STATE_INACTIVE /*1*/:
                                label = "     normal";
                                break;
                            case STATE_IDLE_PENDING /*2*/:
                                label = " light-idle";
                                break;
                            case STATE_SENSING /*3*/:
                                label = "light-maint";
                                break;
                            case STATE_LOCATING /*4*/:
                                label = "  deep-idle";
                                break;
                            case STATE_IDLE /*5*/:
                                label = " deep-maint";
                                break;
                            default:
                                label = "         ??";
                                break;
                        }
                    }
                    i--;
                }
            }
            int size = this.mPowerSaveWhitelistAppsExceptIdle.size();
            if (size > 0) {
                pw.println("  Whitelist (except idle) system apps:");
                for (i = STATE_ACTIVE; i < size; i += STATE_INACTIVE) {
                    pw.print("    ");
                    pw.println((String) this.mPowerSaveWhitelistAppsExceptIdle.keyAt(i));
                }
            }
            size = this.mPowerSaveWhitelistApps.size();
            if (size > 0) {
                pw.println("  Whitelist system apps:");
                for (i = STATE_ACTIVE; i < size; i += STATE_INACTIVE) {
                    pw.print("    ");
                    pw.println((String) this.mPowerSaveWhitelistApps.keyAt(i));
                }
            }
            size = this.mPowerSaveWhitelistUserApps.size();
            if (size > 0) {
                pw.println("  Whitelist user apps:");
                for (i = STATE_ACTIVE; i < size; i += STATE_INACTIVE) {
                    pw.print("    ");
                    pw.println((String) this.mPowerSaveWhitelistUserApps.keyAt(i));
                }
            }
            size = this.mPowerSaveWhitelistExceptIdleAppIds.size();
            if (size > 0) {
                pw.println("  Whitelist (except idle) all app ids:");
                for (i = STATE_ACTIVE; i < size; i += STATE_INACTIVE) {
                    pw.print("    ");
                    pw.print(this.mPowerSaveWhitelistExceptIdleAppIds.keyAt(i));
                    pw.println();
                }
            }
            size = this.mPowerSaveWhitelistUserAppIds.size();
            if (size > 0) {
                pw.println("  Whitelist user app ids:");
                for (i = STATE_ACTIVE; i < size; i += STATE_INACTIVE) {
                    pw.print("    ");
                    pw.print(this.mPowerSaveWhitelistUserAppIds.keyAt(i));
                    pw.println();
                }
            }
            size = this.mPowerSaveWhitelistAllAppIds.size();
            if (size > 0) {
                pw.println("  Whitelist all app ids:");
                for (i = STATE_ACTIVE; i < size; i += STATE_INACTIVE) {
                    pw.print("    ");
                    pw.print(this.mPowerSaveWhitelistAllAppIds.keyAt(i));
                    pw.println();
                }
            }
            dumpTempWhitelistSchedule(pw, true);
            size = this.mTempWhitelistAppIdArray != null ? this.mTempWhitelistAppIdArray.length : STATE_ACTIVE;
            if (size > 0) {
                pw.println("  Temp whitelist app ids:");
                for (i = STATE_ACTIVE; i < size; i += STATE_INACTIVE) {
                    pw.print("    ");
                    pw.print(this.mTempWhitelistAppIdArray[i]);
                    pw.println();
                }
            }
            pw.print("  mLightEnabled=");
            pw.print(this.mLightEnabled);
            pw.print("  mDeepEnabled=");
            pw.println(this.mDeepEnabled);
            pw.print("  mForceIdle=");
            pw.println(this.mForceIdle);
            pw.print("  mMotionSensor=");
            pw.println(this.mMotionSensor);
            pw.print("  mCurDisplay=");
            pw.println(this.mCurDisplay);
            pw.print("  mScreenOn=");
            pw.println(this.mScreenOn);
            pw.print("  mNetworkConnected=");
            pw.println(this.mNetworkConnected);
            pw.print("  mCharging=");
            pw.println(this.mCharging);
            pw.print("  mMotionActive=");
            pw.println(this.mMotionListener.active);
            pw.print("  mNotMoving=");
            pw.println(this.mNotMoving);
            pw.print("  mLocating=");
            pw.print(this.mLocating);
            pw.print(" mHasGps=");
            pw.print(this.mHasGps);
            pw.print(" mHasNetwork=");
            pw.print(this.mHasNetworkLocation);
            pw.print(" mLocated=");
            pw.println(this.mLocated);
            if (this.mLastGenericLocation != null) {
                pw.print("  mLastGenericLocation=");
                pw.println(this.mLastGenericLocation);
            }
            if (this.mLastGpsLocation != null) {
                pw.print("  mLastGpsLocation=");
                pw.println(this.mLastGpsLocation);
            }
            pw.print("  mState=");
            pw.print(stateToString(this.mState));
            pw.print(" mLightState=");
            pw.println(lightStateToString(this.mLightState));
            pw.print("  mInactiveTimeout=");
            TimeUtils.formatDuration(this.mInactiveTimeout, pw);
            pw.println();
            if (this.mActiveIdleOpCount != 0) {
                pw.print("  mActiveIdleOpCount=");
                pw.println(this.mActiveIdleOpCount);
            }
            if (this.mNextAlarmTime != 0) {
                pw.print("  mNextAlarmTime=");
                TimeUtils.formatDuration(this.mNextAlarmTime, SystemClock.elapsedRealtime(), pw);
                pw.println();
            }
            if (this.mNextIdlePendingDelay != 0) {
                pw.print("  mNextIdlePendingDelay=");
                TimeUtils.formatDuration(this.mNextIdlePendingDelay, pw);
                pw.println();
            }
            if (this.mNextIdleDelay != 0) {
                pw.print("  mNextIdleDelay=");
                TimeUtils.formatDuration(this.mNextIdleDelay, pw);
                pw.println();
            }
            if (this.mNextLightIdleDelay != 0) {
                pw.print("  mNextIdleDelay=");
                TimeUtils.formatDuration(this.mNextLightIdleDelay, pw);
                pw.println();
            }
            if (this.mNextLightAlarmTime != 0) {
                pw.print("  mNextLightAlarmTime=");
                TimeUtils.formatDuration(this.mNextLightAlarmTime, SystemClock.elapsedRealtime(), pw);
                pw.println();
            }
            if (this.mCurIdleBudget != 0) {
                pw.print("  mCurIdleBudget=");
                TimeUtils.formatDuration(this.mCurIdleBudget, pw);
                pw.println();
            }
            if (this.mMaintenanceStartTime != 0) {
                pw.print("  mMaintenanceStartTime=");
                TimeUtils.formatDuration(this.mMaintenanceStartTime, SystemClock.elapsedRealtime(), pw);
                pw.println();
            }
            if (this.mJobsActive) {
                pw.print("  mJobsActive=");
                pw.println(this.mJobsActive);
            }
            if (this.mAlarmsActive) {
                pw.print("  mAlarmsActive=");
                pw.println(this.mAlarmsActive);
            }
        }
    }

    void dumpTempWhitelistSchedule(PrintWriter pw, boolean printTitle) {
        int size = this.mTempWhitelistAppIdEndTimes.size();
        if (size > 0) {
            String prefix = "";
            if (printTitle) {
                pw.println("  Temp whitelist schedule:");
                prefix = "    ";
            }
            long timeNow = SystemClock.elapsedRealtime();
            for (int i = STATE_ACTIVE; i < size; i += STATE_INACTIVE) {
                pw.print(prefix);
                pw.print("UID=");
                pw.print(this.mTempWhitelistAppIdEndTimes.keyAt(i));
                pw.print(": ");
                Pair<MutableLong, String> entry = (Pair) this.mTempWhitelistAppIdEndTimes.valueAt(i);
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
                return STATE_ACTIVE;
            } else {
                this.mForceIdle = true;
                becomeInactiveIfAppropriateLocked();
                int curState = this.mState;
                while (curState != STATE_IDLE) {
                    stepIdleStateLocked("s:shell");
                    if (curState == this.mState) {
                        Slog.d(TAG, "Unable to go idle; stopped at " + stateToString(this.mState));
                        exitForceIdleLocked();
                        return -1;
                    }
                    curState = this.mState;
                }
                this.mForceIdle = COMPRESS_TIME;
                Slog.d(TAG, "Now forced in to idle mode");
                return STATE_ACTIVE;
            }
        }
    }
}
