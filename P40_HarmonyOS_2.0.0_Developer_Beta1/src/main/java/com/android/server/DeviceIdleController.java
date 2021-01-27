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
import android.os.PowerSaveState;
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
import com.android.internal.annotations.VisibleForTesting;
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
import com.android.server.deviceidle.ConstraintController;
import com.android.server.deviceidle.DeviceIdleConstraintTracker;
import com.android.server.deviceidle.IDeviceIdleConstraint;
import com.android.server.deviceidle.TvConstraintController;
import com.android.server.job.controllers.JobStatus;
import com.android.server.net.NetworkPolicyManagerInternal;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.usage.AppStandbyController;
import com.android.server.wm.ActivityTaskManagerInternal;
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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class DeviceIdleController extends SystemService implements AnyMotionDetector.DeviceIdleCallback {
    private static final int ACTIVE_REASON_ALARM = 7;
    private static final int ACTIVE_REASON_CHARGING = 3;
    private static final int ACTIVE_REASON_FORCED = 6;
    private static final int ACTIVE_REASON_FROM_BINDER_CALL = 5;
    private static final int ACTIVE_REASON_MOTION = 1;
    private static final int ACTIVE_REASON_SCREEN = 2;
    private static final int ACTIVE_REASON_UNKNOWN = 0;
    private static final int ACTIVE_REASON_UNLOCKED = 4;
    private static final boolean COMPRESS_TIME = false;
    private static boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int EVENT_BUFFER_SIZE = 100;
    private static final int EVENT_DEEP_IDLE = 4;
    private static final int EVENT_DEEP_MAINTENANCE = 5;
    private static final int EVENT_LIGHT_IDLE = 2;
    private static final int EVENT_LIGHT_MAINTENANCE = 3;
    private static final int EVENT_NORMAL = 1;
    private static final int EVENT_NULL = 0;
    @VisibleForTesting
    static final int LIGHT_STATE_ACTIVE = 0;
    @VisibleForTesting
    static final int LIGHT_STATE_IDLE = 4;
    @VisibleForTesting
    static final int LIGHT_STATE_IDLE_MAINTENANCE = 6;
    @VisibleForTesting
    static final int LIGHT_STATE_INACTIVE = 1;
    @VisibleForTesting
    static final int LIGHT_STATE_OVERRIDE = 7;
    @VisibleForTesting
    static final int LIGHT_STATE_PRE_IDLE = 3;
    @VisibleForTesting
    static final int LIGHT_STATE_WAITING_FOR_NETWORK = 5;
    @VisibleForTesting
    static final float MIN_PRE_IDLE_FACTOR_CHANGE = 0.05f;
    @VisibleForTesting
    static final long MIN_STATE_STEP_ALARM_CHANGE = 60000;
    private static final int MSG_FINISH_IDLE_OP = 8;
    private static final int MSG_REPORT_ACTIVE = 5;
    private static final int MSG_REPORT_IDLE_OFF = 4;
    private static final int MSG_REPORT_IDLE_ON = 2;
    private static final int MSG_REPORT_IDLE_ON_LIGHT = 3;
    private static final int MSG_REPORT_MAINTENANCE_ACTIVITY = 7;
    private static final int MSG_REPORT_TEMP_APP_WHITELIST_CHANGED = 9;
    private static final int MSG_RESET_PRE_IDLE_TIMEOUT_FACTOR = 12;
    private static final int MSG_SEND_CONSTRAINT_MONITORING = 10;
    private static final int MSG_TEMP_APP_WHITELIST_TIMEOUT = 6;
    private static final int MSG_UPDATE_PRE_IDLE_TIMEOUT_FACTOR = 11;
    private static final int MSG_WRITE_CONFIG = 1;
    private static final int READ_DB_DELAY_TIME = 10000;
    @VisibleForTesting
    static final int SET_IDLE_FACTOR_RESULT_IGNORED = 0;
    @VisibleForTesting
    static final int SET_IDLE_FACTOR_RESULT_INVALID = 3;
    @VisibleForTesting
    static final int SET_IDLE_FACTOR_RESULT_NOT_SUPPORT = 2;
    @VisibleForTesting
    static final int SET_IDLE_FACTOR_RESULT_OK = 1;
    @VisibleForTesting
    static final int SET_IDLE_FACTOR_RESULT_UNINIT = -1;
    @VisibleForTesting
    static final int STATE_ACTIVE = 0;
    @VisibleForTesting
    static final int STATE_IDLE = 5;
    @VisibleForTesting
    static final int STATE_IDLE_MAINTENANCE = 6;
    @VisibleForTesting
    static final int STATE_IDLE_PENDING = 2;
    @VisibleForTesting
    static final int STATE_INACTIVE = 1;
    @VisibleForTesting
    static final int STATE_LOCATING = 4;
    @VisibleForTesting
    static final int STATE_QUICK_DOZE_DELAY = 7;
    @VisibleForTesting
    static final int STATE_SENSING = 3;
    private static final String TAG = "DeviceIdleController";
    private int mActiveIdleOpCount;
    private PowerManager.WakeLock mActiveIdleWakeLock;
    private int mActiveReason;
    private AlarmManager mAlarmManager;
    private boolean mAlarmsActive;
    private AnyMotionDetector mAnyMotionDetector;
    private final AppStateTracker mAppStateTracker;
    private IBatteryStats mBatteryStats;
    BinderService mBinderService;
    private boolean mCharging;
    public final AtomicFile mConfigFile;
    private Constants mConstants;
    private ConstraintController mConstraintController;
    private final ArrayMap<IDeviceIdleConstraint, DeviceIdleConstraintTracker> mConstraints;
    private long mCurIdleBudget;
    @VisibleForTesting
    final AlarmManager.OnAlarmListener mDeepAlarmListener;
    private boolean mDeepEnabled;
    private final int[] mEventCmds;
    private final String[] mEventReasons;
    private final long[] mEventTimes;
    private boolean mForceIdle;
    private final LocationListener mGenericLocationListener;
    private PowerManager.WakeLock mGoingIdleWakeLock;
    private final LocationListener mGpsLocationListener;
    final MyHandler mHandler;
    private boolean mHasGps;
    private boolean mHasNetworkLocation;
    private Intent mIdleIntent;
    private long mIdleStartTime;
    private final BroadcastReceiver mIdleStartedDoneReceiver;
    private long mInactiveTimeout;
    private final Injector mInjector;
    private final BroadcastReceiver mInteractivityReceiver;
    private boolean mJobsActive;
    private Location mLastGenericLocation;
    private Location mLastGpsLocation;
    private float mLastPreIdleFactor;
    private final AlarmManager.OnAlarmListener mLightAlarmListener;
    private boolean mLightEnabled;
    private Intent mLightIdleIntent;
    private int mLightState;
    private ActivityManagerInternal mLocalActivityManager;
    private ActivityTaskManagerInternal mLocalActivityTaskManager;
    private AlarmManagerInternal mLocalAlarmManager;
    private PowerManagerInternal mLocalPowerManager;
    private boolean mLocated;
    private boolean mLocating;
    private LocationRequest mLocationRequest;
    private final RemoteCallbackList<IMaintenanceActivityListener> mMaintenanceActivityListeners;
    private long mMaintenanceStartTime;
    @VisibleForTesting
    final MotionListener mMotionListener;
    private Sensor mMotionSensor;
    private boolean mNetworkConnected;
    private INetworkPolicyManager mNetworkPolicyManager;
    private NetworkPolicyManagerInternal mNetworkPolicyManagerInternal;
    private long mNextAlarmTime;
    private long mNextIdleDelay;
    private long mNextIdlePendingDelay;
    private long mNextLightAlarmTime;
    private long mNextLightIdleDelay;
    private long mNextSensingTimeoutAlarmTime;
    private boolean mNotMoving;
    private int mNumBlockingConstraints;
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
    private final ArraySet<String> mPowerSaveWhitelistUserAppsExceptIdle;
    private float mPreIdleFactor;
    private boolean mQuickDozeActivated;
    private final BroadcastReceiver mReceiver;
    private ArrayMap<String, Integer> mRemovedFromSystemWhitelistApps;
    private boolean mReportedMaintenanceActivity;
    private boolean mScreenLocked;
    private ActivityTaskManagerInternal.ScreenObserver mScreenObserver;
    private boolean mScreenOn;
    private final AlarmManager.OnAlarmListener mSensingTimeoutAlarmListener;
    private SensorManager mSensorManager;
    private int mState;
    private int[] mTempWhitelistAppIdArray;
    private final SparseArray<Pair<MutableLong, String>> mTempWhitelistAppIdEndTimes;
    private final boolean mUseMotionSensor;

    @VisibleForTesting
    static String stateToString(int state) {
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
            case 7:
                return "QUICK_DOZE_DELAY";
            default:
                return Integer.toString(state);
        }
    }

    @VisibleForTesting
    static String lightStateToString(int state) {
        if (state == 0) {
            return "ACTIVE";
        }
        if (state == 1) {
            return "INACTIVE";
        }
        if (state == 3) {
            return "PRE_IDLE";
        }
        if (state == 4) {
            return "IDLE";
        }
        if (state == 5) {
            return "WAITING_FOR_NETWORK";
        }
        if (state == 6) {
            return "IDLE_MAINTENANCE";
        }
        if (state != 7) {
            return Integer.toString(state);
        }
        return "OVERRIDE";
    }

    private void addEvent(int cmd, String reason) {
        int[] iArr = this.mEventCmds;
        if (iArr[0] != cmd) {
            System.arraycopy(iArr, 0, iArr, 1, 99);
            long[] jArr = this.mEventTimes;
            System.arraycopy(jArr, 0, jArr, 1, 99);
            String[] strArr = this.mEventReasons;
            System.arraycopy(strArr, 0, strArr, 1, 99);
            this.mEventCmds[0] = cmd;
            this.mEventTimes[0] = SystemClock.elapsedRealtime();
            this.mEventReasons[0] = reason;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public final class MotionListener extends TriggerEventListener implements SensorEventListener {
        boolean active = false;

        MotionListener() {
        }

        public boolean isActive() {
            return this.active;
        }

        @Override // android.hardware.TriggerEventListener
        public void onTrigger(TriggerEvent event) {
            synchronized (DeviceIdleController.this) {
                this.active = false;
                DeviceIdleController.this.motionLocked();
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            synchronized (DeviceIdleController.this) {
                DeviceIdleController.this.mSensorManager.unregisterListener(this, DeviceIdleController.this.mMotionSensor);
                this.active = false;
                DeviceIdleController.this.motionLocked();
            }
        }

        @Override // android.hardware.SensorEventListener
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

    public final class Constants extends ContentObserver {
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
        private static final String KEY_PRE_IDLE_FACTOR_LONG = "pre_idle_factor_long";
        private static final String KEY_PRE_IDLE_FACTOR_SHORT = "pre_idle_factor_short";
        private static final String KEY_QUICK_DOZE_DELAY_TIMEOUT = "quick_doze_delay_to";
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
        public float PRE_IDLE_FACTOR_LONG;
        public float PRE_IDLE_FACTOR_SHORT;
        public long QUICK_DOZE_DELAY_TIMEOUT;
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

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            updateConstants();
        }

        private void updateConstants() {
            synchronized (DeviceIdleController.this) {
                try {
                    this.mParser.setString(Settings.Global.getString(this.mResolver, "device_idle_constants"));
                } catch (IllegalArgumentException e) {
                    Slog.e(DeviceIdleController.TAG, "Bad device idle settings", e);
                }
                this.LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT = this.mParser.getDurationMillis(KEY_LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT, 180000);
                this.LIGHT_PRE_IDLE_TIMEOUT = this.mParser.getDurationMillis(KEY_LIGHT_PRE_IDLE_TIMEOUT, 180000);
                this.LIGHT_IDLE_TIMEOUT = this.mParser.getDurationMillis(KEY_LIGHT_IDLE_TIMEOUT, (long) BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS);
                this.LIGHT_IDLE_FACTOR = this.mParser.getFloat(KEY_LIGHT_IDLE_FACTOR, 2.0f);
                this.LIGHT_MAX_IDLE_TIMEOUT = this.mParser.getDurationMillis(KEY_LIGHT_MAX_IDLE_TIMEOUT, 900000);
                this.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET = this.mParser.getDurationMillis(KEY_LIGHT_IDLE_MAINTENANCE_MIN_BUDGET, 60000);
                this.LIGHT_IDLE_MAINTENANCE_MAX_BUDGET = this.mParser.getDurationMillis(KEY_LIGHT_IDLE_MAINTENANCE_MAX_BUDGET, (long) BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS);
                this.MIN_LIGHT_MAINTENANCE_TIME = this.mParser.getDurationMillis(KEY_MIN_LIGHT_MAINTENANCE_TIME, 5000);
                this.MIN_DEEP_MAINTENANCE_TIME = this.mParser.getDurationMillis(KEY_MIN_DEEP_MAINTENANCE_TIME, 30000);
                this.INACTIVE_TIMEOUT = this.mParser.getDurationMillis(KEY_INACTIVE_TIMEOUT, ((long) ((this.mSmallBatteryDevice ? 15 : 30) * 60)) * 1000);
                this.SENSING_TIMEOUT = this.mParser.getDurationMillis(KEY_SENSING_TIMEOUT, 240000);
                this.LOCATING_TIMEOUT = this.mParser.getDurationMillis(KEY_LOCATING_TIMEOUT, 30000);
                this.LOCATION_ACCURACY = this.mParser.getFloat(KEY_LOCATION_ACCURACY, 20.0f);
                this.MOTION_INACTIVE_TIMEOUT = this.mParser.getDurationMillis(KEY_MOTION_INACTIVE_TIMEOUT, 600000);
                this.IDLE_AFTER_INACTIVE_TIMEOUT = this.mParser.getDurationMillis(KEY_IDLE_AFTER_INACTIVE_TIMEOUT, ((long) ((this.mSmallBatteryDevice ? 15 : 30) * 60)) * 1000);
                this.IDLE_PENDING_TIMEOUT = this.mParser.getDurationMillis(KEY_IDLE_PENDING_TIMEOUT, (long) BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS);
                this.MAX_IDLE_PENDING_TIMEOUT = this.mParser.getDurationMillis(KEY_MAX_IDLE_PENDING_TIMEOUT, 600000);
                this.IDLE_PENDING_FACTOR = this.mParser.getFloat(KEY_IDLE_PENDING_FACTOR, 2.0f);
                this.QUICK_DOZE_DELAY_TIMEOUT = this.mParser.getDurationMillis(KEY_QUICK_DOZE_DELAY_TIMEOUT, 60000);
                this.IDLE_TIMEOUT = this.mParser.getDurationMillis(KEY_IDLE_TIMEOUT, (long) AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT);
                this.MAX_IDLE_TIMEOUT = this.mParser.getDurationMillis(KEY_MAX_IDLE_TIMEOUT, 21600000);
                this.IDLE_FACTOR = this.mParser.getFloat(KEY_IDLE_FACTOR, 2.0f);
                this.MIN_TIME_TO_ALARM = this.mParser.getDurationMillis(KEY_MIN_TIME_TO_ALARM, (long) AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT);
                this.MAX_TEMP_APP_WHITELIST_DURATION = this.mParser.getDurationMillis(KEY_MAX_TEMP_APP_WHITELIST_DURATION, (long) BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS);
                this.MMS_TEMP_APP_WHITELIST_DURATION = this.mParser.getDurationMillis(KEY_MMS_TEMP_APP_WHITELIST_DURATION, 60000);
                this.SMS_TEMP_APP_WHITELIST_DURATION = this.mParser.getDurationMillis(KEY_SMS_TEMP_APP_WHITELIST_DURATION, 20000);
                this.NOTIFICATION_WHITELIST_DURATION = this.mParser.getDurationMillis(KEY_NOTIFICATION_WHITELIST_DURATION, 30000);
                this.WAIT_FOR_UNLOCK = this.mParser.getBoolean(KEY_WAIT_FOR_UNLOCK, true);
                this.PRE_IDLE_FACTOR_LONG = this.mParser.getFloat(KEY_PRE_IDLE_FACTOR_LONG, 1.67f);
                this.PRE_IDLE_FACTOR_SHORT = this.mParser.getFloat(KEY_PRE_IDLE_FACTOR_SHORT, 0.33f);
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
            pw.print(KEY_QUICK_DOZE_DELAY_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.QUICK_DOZE_DELAY_TIMEOUT, pw);
            pw.println();
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
            pw.print("    ");
            pw.print(KEY_PRE_IDLE_FACTOR_LONG);
            pw.print("=");
            pw.println(this.PRE_IDLE_FACTOR_LONG);
            pw.print("    ");
            pw.print(KEY_PRE_IDLE_FACTOR_SHORT);
            pw.print("=");
            pw.println(this.PRE_IDLE_FACTOR_SHORT);
        }
    }

    @Override // com.android.server.AnyMotionDetector.DeviceIdleCallback
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
        } else if (result == 0) {
            int i = this.mState;
            if (i == 3) {
                synchronized (this) {
                    this.mNotMoving = true;
                    stepIdleStateLocked("s:stationary");
                }
            } else if (i == 4) {
                synchronized (this) {
                    this.mNotMoving = true;
                    if (this.mLocated) {
                        stepIdleStateLocked("s:stationary");
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class MyHandler extends Handler {
        MyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            boolean lightChanged;
            boolean deepChanged;
            if (DeviceIdleController.DEBUG) {
                Slog.d(DeviceIdleController.TAG, "handleMessage(" + msg.what + ")");
            }
            int i = msg.what;
            String str = UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
            boolean monitoring = true;
            switch (i) {
                case 1:
                    DeviceIdleController.this.handleWriteConfigFile();
                    return;
                case 2:
                case 3:
                    EventLogTags.writeDeviceIdleOnStart();
                    int i2 = 2;
                    if (msg.what == 2) {
                        deepChanged = DeviceIdleController.this.mLocalPowerManager.setDeviceIdleMode(true);
                        lightChanged = DeviceIdleController.this.mLocalPowerManager.setLightDeviceIdleMode(false);
                    } else {
                        deepChanged = DeviceIdleController.this.mLocalPowerManager.setDeviceIdleMode(false);
                        lightChanged = DeviceIdleController.this.mLocalPowerManager.setLightDeviceIdleMode(true);
                    }
                    try {
                        DeviceIdleController.this.mNetworkPolicyManager.setDeviceIdleMode(true);
                        IBatteryStats iBatteryStats = DeviceIdleController.this.mBatteryStats;
                        if (msg.what != 2) {
                            i2 = 1;
                        }
                        iBatteryStats.noteDeviceIdleMode(i2, (String) null, Process.myUid());
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
                    EventLogTags.writeDeviceIdleOffStart(str);
                    boolean deepChanged2 = DeviceIdleController.this.mLocalPowerManager.setDeviceIdleMode(false);
                    boolean lightChanged2 = DeviceIdleController.this.mLocalPowerManager.setLightDeviceIdleMode(false);
                    try {
                        DeviceIdleController.this.mNetworkPolicyManager.setDeviceIdleMode(false);
                        DeviceIdleController.this.mBatteryStats.noteDeviceIdleMode(0, (String) null, Process.myUid());
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
                        monitoring = false;
                    }
                    int size = DeviceIdleController.this.mMaintenanceActivityListeners.beginBroadcast();
                    for (int i3 = 0; i3 < size; i3++) {
                        try {
                            DeviceIdleController.this.mMaintenanceActivityListeners.getBroadcastItem(i3).onMaintenanceActivityChanged(monitoring);
                        } catch (RemoteException e4) {
                        } catch (Throwable th) {
                            DeviceIdleController.this.mMaintenanceActivityListeners.finishBroadcast();
                            throw th;
                        }
                    }
                    DeviceIdleController.this.mMaintenanceActivityListeners.finishBroadcast();
                    return;
                case 8:
                    DeviceIdleController.this.decActiveIdleOps();
                    return;
                case 9:
                    int appId = msg.arg1;
                    if (msg.arg2 != 1) {
                        monitoring = false;
                    }
                    DeviceIdleController.this.mNetworkPolicyManagerInternal.onTempPowerSaveWhitelistChange(appId, monitoring);
                    return;
                case 10:
                    IDeviceIdleConstraint constraint = (IDeviceIdleConstraint) msg.obj;
                    if (msg.arg1 != 1) {
                        monitoring = false;
                    }
                    if (monitoring) {
                        constraint.startMonitoring();
                        return;
                    } else {
                        constraint.stopMonitoring();
                        return;
                    }
                case 11:
                    DeviceIdleController.this.updatePreIdleFactor();
                    return;
                case 12:
                    DeviceIdleController.this.updatePreIdleFactor();
                    DeviceIdleController.this.maybeDoImmediateMaintenance();
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public final class BinderService extends IDeviceIdleController.Stub {
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

        public int setPreIdleTimeoutMode(int mode) {
            DeviceIdleController.this.getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                return DeviceIdleController.this.setPreIdleTimeoutMode(mode);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void resetPreIdleTimeoutMode() {
            DeviceIdleController.this.getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                DeviceIdleController.this.resetPreIdleTimeoutMode();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            DeviceIdleController.this.dump(fd, pw, args);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.DeviceIdleController$BinderService */
        /* JADX WARN: Multi-variable type inference failed */
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

    public class LocalService {
        public LocalService() {
        }

        public void onConstraintStateChanged(IDeviceIdleConstraint constraint, boolean active) {
            synchronized (DeviceIdleController.this) {
                DeviceIdleController.this.onConstraintStateChangedLocked(constraint, active);
            }
        }

        public void registerDeviceIdleConstraint(IDeviceIdleConstraint constraint, String name, int minState) {
            DeviceIdleController.this.registerDeviceIdleConstraintInternal(constraint, name, minState);
        }

        public void unregisterDeviceIdleConstraint(IDeviceIdleConstraint constraint) {
            DeviceIdleController.this.unregisterDeviceIdleConstraintInternal(constraint);
        }

        public void exitIdle(String reason) {
            DeviceIdleController.this.exitIdleInternal(reason);
        }

        public void addPowerSaveTempWhitelistApp(int callingUid, String packageName, long duration, int userId, boolean sync, String reason) {
            DeviceIdleController.this.addPowerSaveTempWhitelistAppInternal(callingUid, packageName, duration, userId, sync, reason);
        }

        public void addPowerSaveTempWhitelistAppDirect(int uid, long duration, boolean sync, String reason) {
            DeviceIdleController.this.addPowerSaveTempWhitelistAppDirectInternal(0, uid, duration, sync, reason);
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

        public void enableQuickDoze(boolean enable) {
            synchronized (DeviceIdleController.this) {
                DeviceIdleController.this.updateQuickDozeFlagLocked(enable);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class Injector {
        private ConnectivityService mConnectivityService;
        private Constants mConstants;
        private final Context mContext;
        private LocationManager mLocationManager;

        Injector(Context ctx) {
            this.mContext = ctx;
        }

        /* access modifiers changed from: package-private */
        public AlarmManager getAlarmManager() {
            return (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        }

        /* access modifiers changed from: package-private */
        public AnyMotionDetector getAnyMotionDetector(Handler handler, SensorManager sm, AnyMotionDetector.DeviceIdleCallback callback, float angleThreshold) {
            return new AnyMotionDetector(getPowerManager(), handler, sm, callback, angleThreshold);
        }

        /* access modifiers changed from: package-private */
        public AppStateTracker getAppStateTracker(Context ctx, Looper looper) {
            return new AppStateTracker(ctx, looper);
        }

        /* access modifiers changed from: package-private */
        public ConnectivityService getConnectivityService() {
            if (this.mConnectivityService == null) {
                this.mConnectivityService = (ConnectivityService) ServiceManager.getService("connectivity");
            }
            return this.mConnectivityService;
        }

        /* access modifiers changed from: package-private */
        public Constants getConstants(DeviceIdleController controller, Handler handler, ContentResolver resolver) {
            if (this.mConstants == null) {
                Objects.requireNonNull(controller);
                this.mConstants = new Constants(handler, resolver);
            }
            return this.mConstants;
        }

        /* access modifiers changed from: package-private */
        public LocationManager getLocationManager() {
            if (this.mLocationManager == null) {
                this.mLocationManager = (LocationManager) this.mContext.getSystemService(LocationManager.class);
            }
            return this.mLocationManager;
        }

        /* access modifiers changed from: package-private */
        public MyHandler getHandler(DeviceIdleController controller) {
            Objects.requireNonNull(controller);
            return new MyHandler(BackgroundThread.getHandler().getLooper());
        }

        /* access modifiers changed from: package-private */
        public PowerManager getPowerManager() {
            return (PowerManager) this.mContext.getSystemService(PowerManager.class);
        }

        /* access modifiers changed from: package-private */
        public SensorManager getSensorManager() {
            return (SensorManager) this.mContext.getSystemService(SensorManager.class);
        }

        /* access modifiers changed from: package-private */
        public ConstraintController getConstraintController(Handler handler, LocalService localService) {
            if (this.mContext.getPackageManager().hasSystemFeature("android.software.leanback_only")) {
                return new TvConstraintController(this.mContext, handler);
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public boolean useMotionSensor() {
            return this.mContext.getResources().getBoolean(17891366);
        }
    }

    @VisibleForTesting
    DeviceIdleController(Context context, Injector injector) {
        super(context);
        this.mNumBlockingConstraints = 0;
        this.mConstraints = new ArrayMap<>();
        this.mMaintenanceActivityListeners = new RemoteCallbackList<>();
        this.mPowerSaveWhitelistAppsExceptIdle = new ArrayMap<>();
        this.mPowerSaveWhitelistUserAppsExceptIdle = new ArraySet<>();
        this.mPowerSaveWhitelistApps = new ArrayMap<>();
        this.mPowerSaveWhitelistUserApps = new ArrayMap<>();
        this.mPowerSaveWhitelistSystemAppIdsExceptIdle = new SparseBooleanArray();
        this.mPowerSaveWhitelistSystemAppIds = new SparseBooleanArray();
        this.mPowerSaveWhitelistExceptIdleAppIds = new SparseBooleanArray();
        this.mPowerSaveWhitelistExceptIdleAppIdArray = new int[0];
        this.mPowerSaveWhitelistAllAppIds = new SparseBooleanArray();
        this.mPowerSaveWhitelistAllAppIdArray = new int[0];
        this.mPowerSaveWhitelistUserAppIds = new SparseBooleanArray();
        this.mPowerSaveWhitelistUserAppIdArray = new int[0];
        this.mTempWhitelistAppIdEndTimes = new SparseArray<>();
        this.mTempWhitelistAppIdArray = new int[0];
        this.mRemovedFromSystemWhitelistApps = new ArrayMap<>();
        this.mEventCmds = new int[100];
        this.mEventTimes = new long[100];
        this.mEventReasons = new String[100];
        this.mReceiver = new BroadcastReceiver() {
            /* class com.android.server.DeviceIdleController.AnonymousClass1 */

            /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
            /* JADX WARNING: Removed duplicated region for block: B:43:0x0081  */
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                char c;
                Uri data;
                String ssp;
                String action = intent.getAction();
                int hashCode = action.hashCode();
                boolean z = false;
                if (hashCode != -1538406691) {
                    if (hashCode != -1172645946) {
                        if (hashCode == 525384130 && action.equals("android.intent.action.PACKAGE_REMOVED")) {
                            c = 2;
                            if (c != 0) {
                                DeviceIdleController.this.updateConnectivityState(intent);
                                return;
                            } else if (c == 1) {
                                boolean present = intent.getBooleanExtra("present", true);
                                boolean plugged = intent.getIntExtra("plugged", 0) != 0;
                                synchronized (DeviceIdleController.this) {
                                    DeviceIdleController deviceIdleController = DeviceIdleController.this;
                                    if (present && plugged) {
                                        z = true;
                                    }
                                    deviceIdleController.updateChargingLocked(z);
                                }
                                return;
                            } else if (c == 2 && !intent.getBooleanExtra("android.intent.extra.REPLACING", false) && (data = intent.getData()) != null && (ssp = data.getSchemeSpecificPart()) != null) {
                                DeviceIdleController.this.removePowerSaveWhitelistAppInternal(ssp);
                                return;
                            } else {
                                return;
                            }
                        }
                    } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                        c = 0;
                        if (c != 0) {
                        }
                    }
                } else if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                    c = 1;
                    if (c != 0) {
                    }
                }
                c = 65535;
                if (c != 0) {
                }
            }
        };
        this.mLightAlarmListener = new AlarmManager.OnAlarmListener() {
            /* class com.android.server.DeviceIdleController.AnonymousClass2 */

            @Override // android.app.AlarmManager.OnAlarmListener
            public void onAlarm() {
                synchronized (DeviceIdleController.this) {
                    DeviceIdleController.this.stepLightIdleStateLocked("s:alarm");
                }
            }
        };
        this.mSensingTimeoutAlarmListener = new AlarmManager.OnAlarmListener() {
            /* class com.android.server.DeviceIdleController.AnonymousClass3 */

            @Override // android.app.AlarmManager.OnAlarmListener
            public void onAlarm() {
                if (DeviceIdleController.this.mState == 3) {
                    synchronized (DeviceIdleController.this) {
                        DeviceIdleController.this.becomeInactiveIfAppropriateLocked();
                    }
                }
            }
        };
        this.mDeepAlarmListener = new AlarmManager.OnAlarmListener() {
            /* class com.android.server.DeviceIdleController.AnonymousClass4 */

            @Override // android.app.AlarmManager.OnAlarmListener
            public void onAlarm() {
                synchronized (DeviceIdleController.this) {
                    DeviceIdleController.this.stepIdleStateLocked("s:alarm");
                }
            }
        };
        this.mIdleStartedDoneReceiver = new BroadcastReceiver() {
            /* class com.android.server.DeviceIdleController.AnonymousClass5 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(intent.getAction())) {
                    DeviceIdleController.this.mHandler.sendEmptyMessageDelayed(8, DeviceIdleController.this.mConstants.MIN_DEEP_MAINTENANCE_TIME);
                } else {
                    DeviceIdleController.this.mHandler.sendEmptyMessageDelayed(8, DeviceIdleController.this.mConstants.MIN_LIGHT_MAINTENANCE_TIME);
                }
            }
        };
        this.mInteractivityReceiver = new BroadcastReceiver() {
            /* class com.android.server.DeviceIdleController.AnonymousClass6 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                synchronized (DeviceIdleController.this) {
                    DeviceIdleController.this.updateInteractivityLocked();
                }
            }
        };
        this.mMotionListener = new MotionListener();
        this.mGenericLocationListener = new LocationListener() {
            /* class com.android.server.DeviceIdleController.AnonymousClass7 */

            @Override // android.location.LocationListener
            public void onLocationChanged(Location location) {
                synchronized (DeviceIdleController.this) {
                    DeviceIdleController.this.receivedGenericLocationLocked(location);
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
        };
        this.mGpsLocationListener = new LocationListener() {
            /* class com.android.server.DeviceIdleController.AnonymousClass8 */

            @Override // android.location.LocationListener
            public void onLocationChanged(Location location) {
                synchronized (DeviceIdleController.this) {
                    DeviceIdleController.this.receivedGpsLocationLocked(location);
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
        };
        this.mScreenObserver = new ActivityTaskManagerInternal.ScreenObserver() {
            /* class com.android.server.DeviceIdleController.AnonymousClass9 */

            public void onAwakeStateChanged(boolean isAwake) {
            }

            public void onKeyguardStateChanged(boolean isShowing) {
                synchronized (DeviceIdleController.this) {
                    DeviceIdleController.this.keyguardShowingLocked(isShowing);
                }
            }
        };
        this.mInjector = injector;
        this.mConfigFile = new AtomicFile(new File(getSystemDir(), "deviceidle.xml"));
        this.mHandler = this.mInjector.getHandler(this);
        this.mAppStateTracker = this.mInjector.getAppStateTracker(context, FgThread.get().getLooper());
        LocalServices.addService(AppStateTracker.class, this.mAppStateTracker);
        this.mUseMotionSensor = this.mInjector.useMotionSensor();
    }

    public DeviceIdleController(Context context) {
        this(context, new Injector(context));
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

    /* JADX DEBUG: Multi-variable search result rejected for r14v0, resolved type: com.android.server.DeviceIdleController */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v6, types: [com.android.server.DeviceIdleController$BinderService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        PackageManager pm = getContext().getPackageManager();
        synchronized (this) {
            boolean z = getContext().getResources().getBoolean(17891434);
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
            this.mConstants = this.mInjector.getConstants(this, this.mHandler, getContext().getContentResolver());
            readConfigFileLocked();
            updateWhitelistAppIdsLocked();
            this.mNetworkConnected = true;
            this.mScreenOn = true;
            this.mScreenLocked = false;
            this.mCharging = true;
            this.mActiveReason = 0;
            this.mState = 0;
            this.mLightState = 0;
            this.mInactiveTimeout = this.mConstants.INACTIVE_TIMEOUT;
            this.mPreIdleFactor = 1.0f;
            this.mLastPreIdleFactor = 1.0f;
        }
        this.mBinderService = new BinderService();
        publishBinderService("deviceidle", this.mBinderService);
        publishLocalService(LocalService.class, new LocalService());
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 500) {
            synchronized (this) {
                this.mAlarmManager = this.mInjector.getAlarmManager();
                this.mLocalAlarmManager = (AlarmManagerInternal) getLocalService(AlarmManagerInternal.class);
                this.mBatteryStats = BatteryStatsService.getService();
                this.mLocalActivityManager = (ActivityManagerInternal) getLocalService(ActivityManagerInternal.class);
                this.mLocalActivityTaskManager = (ActivityTaskManagerInternal) getLocalService(ActivityTaskManagerInternal.class);
                this.mLocalPowerManager = (PowerManagerInternal) getLocalService(PowerManagerInternal.class);
                this.mPowerManager = this.mInjector.getPowerManager();
                this.mActiveIdleWakeLock = this.mPowerManager.newWakeLock(1, "deviceidle_maint");
                this.mActiveIdleWakeLock.setReferenceCounted(false);
                this.mGoingIdleWakeLock = this.mPowerManager.newWakeLock(1, "deviceidle_going_idle");
                this.mGoingIdleWakeLock.setReferenceCounted(true);
                this.mNetworkPolicyManager = INetworkPolicyManager.Stub.asInterface(ServiceManager.getService("netpolicy"));
                this.mNetworkPolicyManagerInternal = (NetworkPolicyManagerInternal) getLocalService(NetworkPolicyManagerInternal.class);
                this.mSensorManager = this.mInjector.getSensorManager();
                if (this.mUseMotionSensor) {
                    int sigMotionSensorId = getContext().getResources().getInteger(17694743);
                    if (sigMotionSensorId > 0) {
                        this.mMotionSensor = this.mSensorManager.getDefaultSensor(sigMotionSensorId, true);
                    }
                    if (this.mMotionSensor == null && getContext().getResources().getBoolean(17891364)) {
                        this.mMotionSensor = this.mSensorManager.getDefaultSensor(26, true);
                    }
                    if (this.mMotionSensor == null) {
                        this.mMotionSensor = this.mSensorManager.getDefaultSensor(17, true);
                    }
                }
                if (getContext().getResources().getBoolean(17891365)) {
                    this.mLocationRequest = new LocationRequest().setQuality(100).setInterval(0).setFastestInterval(0).setNumUpdates(1);
                }
                this.mConstraintController = this.mInjector.getConstraintController(this.mHandler, (LocalService) getLocalService(LocalService.class));
                if (this.mConstraintController != null) {
                    this.mConstraintController.start();
                }
                this.mAnyMotionDetector = this.mInjector.getAnyMotionDetector(this.mHandler, this.mSensorManager, this, ((float) getContext().getResources().getInteger(17694744)) / 100.0f);
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
                this.mLocalPowerManager.registerLowPowerModeObserver(15, new Consumer() {
                    /* class com.android.server.$$Lambda$DeviceIdleController$Lhneg3gOUCFQWz6Y5S75AWemY8 */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        DeviceIdleController.this.lambda$onBootPhase$0$DeviceIdleController((PowerSaveState) obj);
                    }
                });
                updateQuickDozeFlagLocked(this.mLocalPowerManager.getLowPowerState(15).batterySaverEnabled);
                this.mLocalActivityTaskManager.registerScreenObserver(this.mScreenObserver);
                passWhiteListsToForceAppStandbyTrackerLocked();
                updateInteractivityLocked();
            }
            updateConnectivityState(null);
        } else if (phase == 1000) {
            Slog.d(TAG, "PHASE_BOOT_COMPLETED");
            this.mHandler.postDelayed(new Runnable() {
                /* class com.android.server.DeviceIdleController.AnonymousClass10 */
                private static final int MAX_TRY_TIMES = 3;
                private int count = 0;

                @Override // java.lang.Runnable
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

    public /* synthetic */ void lambda$onBootPhase$0$DeviceIdleController(PowerSaveState state) {
        synchronized (this) {
            updateQuickDozeFlagLocked(state.batterySaverEnabled);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean hasMotionSensor() {
        return this.mUseMotionSensor && this.mMotionSensor != null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerDeviceIdleConstraintInternal(IDeviceIdleConstraint constraint, String name, int type) {
        int minState;
        if (type == 0) {
            minState = 0;
        } else if (type != 1) {
            Slog.wtf(TAG, "Registering device-idle constraint with invalid type: " + type);
            return;
        } else {
            minState = 3;
        }
        synchronized (this) {
            if (this.mConstraints.containsKey(constraint)) {
                Slog.e(TAG, "Re-registering device-idle constraint: " + constraint + ".");
                return;
            }
            this.mConstraints.put(constraint, new DeviceIdleConstraintTracker(name, minState));
            updateActiveConstraintsLocked();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterDeviceIdleConstraintInternal(IDeviceIdleConstraint constraint) {
        synchronized (this) {
            onConstraintStateChangedLocked(constraint, false);
            setConstraintMonitoringLocked(constraint, false);
            this.mConstraints.remove(constraint);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"this"})
    private void onConstraintStateChangedLocked(IDeviceIdleConstraint constraint, boolean active) {
        DeviceIdleConstraintTracker tracker = this.mConstraints.get(constraint);
        if (tracker == null) {
            Slog.e(TAG, "device-idle constraint " + constraint + " has not been registered.");
        } else if (active != tracker.active && tracker.monitoring) {
            tracker.active = active;
            this.mNumBlockingConstraints += tracker.active ? 1 : -1;
            if (this.mNumBlockingConstraints != 0) {
                return;
            }
            if (this.mState == 0) {
                becomeInactiveIfAppropriateLocked();
                return;
            }
            long j = this.mNextAlarmTime;
            if (j == 0 || j < SystemClock.elapsedRealtime()) {
                stepIdleStateLocked("s:" + tracker.name);
            }
        }
    }

    @GuardedBy({"this"})
    private void setConstraintMonitoringLocked(IDeviceIdleConstraint constraint, boolean monitor) {
        DeviceIdleConstraintTracker tracker = this.mConstraints.get(constraint);
        if (tracker.monitoring != monitor) {
            tracker.monitoring = monitor;
            updateActiveConstraintsLocked();
            this.mHandler.obtainMessage(10, monitor ? 1 : 0, -1, constraint).sendToTarget();
        }
    }

    @GuardedBy({"this"})
    private void updateActiveConstraintsLocked() {
        this.mNumBlockingConstraints = 0;
        for (int i = 0; i < this.mConstraints.size(); i++) {
            IDeviceIdleConstraint constraint = this.mConstraints.keyAt(i);
            DeviceIdleConstraintTracker tracker = this.mConstraints.valueAt(i);
            boolean monitoring = tracker.minState == this.mState;
            if (monitoring != tracker.monitoring) {
                setConstraintMonitoringLocked(constraint, monitoring);
                tracker.active = monitoring;
            }
            if (tracker.monitoring && tracker.active) {
                this.mNumBlockingConstraints++;
            }
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
            this.mPowerSaveWhitelistApps.putAll((ArrayMap<? extends String, ? extends Integer>) this.mRemovedFromSystemWhitelistApps);
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
            int cur = 0;
            for (int i = 0; i < this.mPowerSaveWhitelistAppsExceptIdle.size(); i++) {
                apps[cur] = this.mPowerSaveWhitelistAppsExceptIdle.keyAt(i);
                cur++;
            }
            for (int i2 = 0; i2 < this.mPowerSaveWhitelistUserApps.size(); i2++) {
                apps[cur] = this.mPowerSaveWhitelistUserApps.keyAt(i2);
                cur++;
            }
        }
        return apps;
    }

    public String[] getFullPowerWhitelistInternal() {
        String[] apps;
        synchronized (this) {
            apps = new String[(this.mPowerSaveWhitelistApps.size() + this.mPowerSaveWhitelistUserApps.size())];
            int cur = 0;
            for (int i = 0; i < this.mPowerSaveWhitelistApps.size(); i++) {
                apps[cur] = this.mPowerSaveWhitelistApps.keyAt(i);
                cur++;
            }
            for (int i2 = 0; i2 < this.mPowerSaveWhitelistUserApps.size(); i2++) {
                apps[cur] = this.mPowerSaveWhitelistUserApps.keyAt(i2);
                cur++;
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
        int userId2 = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), callingUid, userId, false, false, "addPowerSaveTempWhitelistApp", (String) null);
        long token = Binder.clearCallingIdentity();
        try {
            addPowerSaveTempWhitelistAppInternal(callingUid, packageName, duration, userId2, true, reason);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* access modifiers changed from: package-private */
    public void removePowerSaveTempWhitelistAppChecked(String packageName, int userId) throws RemoteException {
        getContext().enforceCallingPermission("android.permission.CHANGE_DEVICE_IDLE_TEMP_WHITELIST", "No permission to change device idle whitelist");
        int userId2 = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, "removePowerSaveTempWhitelistApp", (String) null);
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
            addPowerSaveTempWhitelistAppDirectInternal(callingUid, getContext().getPackageManager().getPackageUidAsUser(packageName, userId), duration, sync, reason);
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void addPowerSaveTempWhitelistAppDirectInternal(int callingUid, int uid, long duration, boolean sync, String reason) {
        Throwable th;
        long j;
        long duration2;
        Pair<MutableLong, String> entry;
        long timeNow = SystemClock.elapsedRealtime();
        boolean informWhitelistChanged = false;
        int appId = UserHandle.getAppId(uid);
        synchronized (this) {
            try {
                int callingAppId = UserHandle.getAppId(callingUid);
                if (callingAppId >= 10000) {
                    try {
                        if (!this.mPowerSaveWhitelistSystemAppIds.get(callingAppId)) {
                            throw new SecurityException("Calling app " + UserHandle.formatUid(callingUid) + " is not on whitelist");
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
                j = duration;
                try {
                    duration2 = Math.min(j, this.mConstants.MAX_TEMP_APP_WHITELIST_DURATION);
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
                try {
                    Pair<MutableLong, String> entry2 = this.mTempWhitelistAppIdEndTimes.get(appId);
                    boolean newEntry = entry2 == null;
                    if (newEntry) {
                        Pair<MutableLong, String> entry3 = new Pair<>(new MutableLong(0), reason);
                        this.mTempWhitelistAppIdEndTimes.put(appId, entry3);
                        entry = entry3;
                    } else {
                        entry = entry2;
                    }
                    ((MutableLong) entry.first).value = timeNow + duration2;
                    if (DEBUG) {
                        Slog.d(TAG, "Adding AppId " + appId + " to temp whitelist. New entry: " + newEntry);
                    }
                    if (newEntry) {
                        try {
                            try {
                                this.mBatteryStats.noteEvent(32785, reason, uid);
                            } catch (RemoteException e) {
                            }
                        } catch (RemoteException e2) {
                        }
                        try {
                            postTempActiveTimeoutMessage(appId, duration2);
                            updateTempWhitelistAppIdsLocked(appId, true);
                            if (sync) {
                                informWhitelistChanged = true;
                            } else {
                                this.mHandler.obtainMessage(9, appId, 1).sendToTarget();
                            }
                            reportTempWhitelistChangedLocked();
                        } catch (Throwable th4) {
                            th = th4;
                            throw th;
                        }
                    }
                } catch (Throwable th5) {
                    th = th5;
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                j = duration;
                throw th;
            }
        }
        if (informWhitelistChanged) {
            this.mNetworkPolicyManagerInternal.onTempPowerSaveWhitelistChange(appId, true);
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

    private void postTempActiveTimeoutMessage(int appId, long delay) {
        if (DEBUG) {
            Slog.d(TAG, "postTempActiveTimeoutMessage: appId=" + appId + ", delay=" + delay);
        }
        MyHandler myHandler = this.mHandler;
        myHandler.sendMessageDelayed(myHandler.obtainMessage(6, appId, 0), delay);
    }

    /* access modifiers changed from: package-private */
    public void checkTempAppWhitelistTimeout(int appId) {
        long timeNow = SystemClock.elapsedRealtime();
        if (DEBUG) {
            Slog.d(TAG, "checkTempAppWhitelistTimeout: appId=" + appId + ", timeNow=" + timeNow);
        }
        synchronized (this) {
            Pair<MutableLong, String> entry = this.mTempWhitelistAppIdEndTimes.get(appId);
            if (entry != null) {
                if (timeNow >= ((MutableLong) entry.first).value) {
                    this.mTempWhitelistAppIdEndTimes.delete(appId);
                    onAppRemovedFromTempWhitelistLocked(appId, (String) entry.second);
                } else {
                    if (DEBUG) {
                        Slog.d(TAG, "Time to remove AppId " + appId + ": " + ((MutableLong) entry.first).value);
                    }
                    postTempActiveTimeoutMessage(appId, ((MutableLong) entry.first).value - timeNow);
                }
            }
        }
    }

    @GuardedBy({"this"})
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
            this.mActiveReason = 5;
            becomeActiveLocked(reason, Binder.getCallingUid());
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isNetworkConnected() {
        boolean z;
        synchronized (this) {
            z = this.mNetworkConnected;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void updateConnectivityState(Intent connIntent) {
        ConnectivityService cm;
        boolean conn;
        synchronized (this) {
            cm = this.mInjector.getConnectivityService();
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
    @VisibleForTesting
    public boolean isScreenOn() {
        boolean z;
        synchronized (this) {
            z = this.mScreenOn;
        }
        return z;
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
                this.mActiveReason = 2;
                becomeActiveLocked("screen", Process.myUid());
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isCharging() {
        boolean z;
        synchronized (this) {
            z = this.mCharging;
        }
        return z;
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
                this.mActiveReason = 3;
                becomeActiveLocked("charging", Process.myUid());
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isQuickDozeEnabled() {
        boolean z;
        synchronized (this) {
            z = this.mQuickDozeActivated;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updateQuickDozeFlagLocked(boolean enabled) {
        if (DEBUG) {
            Slog.i(TAG, "updateQuickDozeFlagLocked: enabled=" + enabled);
        }
        this.mQuickDozeActivated = enabled;
        if (enabled) {
            becomeInactiveIfAppropriateLocked();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isKeyguardShowing() {
        boolean z;
        synchronized (this) {
            z = this.mScreenLocked;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void keyguardShowingLocked(boolean showing) {
        if (DEBUG) {
            Slog.i(TAG, "keyguardShowing=" + showing);
        }
        if (this.mScreenLocked != showing) {
            this.mScreenLocked = showing;
            if (this.mScreenOn && !this.mForceIdle && !this.mScreenLocked) {
                this.mActiveReason = 4;
                becomeActiveLocked("unlocked", Process.myUid());
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void scheduleReportActiveLocked(String activeReason, int activeUid) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(5, activeUid, 0, activeReason));
    }

    /* access modifiers changed from: package-private */
    public void becomeActiveLocked(String activeReason, int activeUid) {
        becomeActiveLocked(activeReason, activeUid, this.mConstants.INACTIVE_TIMEOUT, true);
    }

    private void becomeActiveLocked(String activeReason, int activeUid, long newInactiveTimeout, boolean changeLightIdle) {
        if (DEBUG) {
            Slog.i(TAG, "becomeActiveLocked, reason=" + activeReason + ", changeLightIdle=" + changeLightIdle);
        }
        if (this.mState != 0 || this.mLightState != 0) {
            EventLogTags.writeDeviceIdle(0, activeReason);
            this.mState = 0;
            this.mInactiveTimeout = newInactiveTimeout;
            this.mCurIdleBudget = 0;
            this.mMaintenanceStartTime = 0;
            resetIdleManagementLocked();
            if (changeLightIdle) {
                EventLogTags.writeDeviceIdleLight(0, activeReason);
                this.mLightState = 0;
                resetLightIdleManagementLocked();
                scheduleReportActiveLocked(activeReason, activeUid);
                addEvent(1, activeReason);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setDeepEnabledForTest(boolean enabled) {
        synchronized (this) {
            this.mDeepEnabled = enabled;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setLightEnabledForTest(boolean enabled) {
        synchronized (this) {
            this.mLightEnabled = enabled;
        }
    }

    private void verifyAlarmStateLocked() {
        if (this.mState == 0 && this.mNextAlarmTime != 0) {
            Slog.wtf(TAG, "mState=ACTIVE but mNextAlarmTime=" + this.mNextAlarmTime);
        }
        if (this.mState != 5 && this.mLocalAlarmManager.isIdling()) {
            Slog.wtf(TAG, "mState=" + stateToString(this.mState) + " but AlarmManager is idling");
        }
        if (this.mState == 5 && !this.mLocalAlarmManager.isIdling()) {
            Slog.wtf(TAG, "mState=IDLE but AlarmManager is not idling");
        }
        if (this.mLightState == 0 && this.mNextLightAlarmTime != 0) {
            Slog.wtf(TAG, "mLightState=ACTIVE but mNextLightAlarmTime is " + TimeUtils.formatDuration(this.mNextLightAlarmTime - SystemClock.elapsedRealtime()) + " from now");
        }
    }

    /* access modifiers changed from: package-private */
    public void becomeInactiveIfAppropriateLocked() {
        verifyAlarmStateLocked();
        boolean isScreenBlockingInactive = this.mScreenOn && (!this.mConstants.WAIT_FOR_UNLOCK || !this.mScreenLocked);
        if (DEBUG) {
            Slog.i(TAG, "becomeInactiveIfAppropriateLocked(): isScreenBlockingInactive=" + isScreenBlockingInactive + " (mScreenOn=" + this.mScreenOn + ", WAIT_FOR_UNLOCK=" + this.mConstants.WAIT_FOR_UNLOCK + ", mScreenLocked=" + this.mScreenLocked + ") mCharging=" + this.mCharging + " mForceIdle=" + this.mForceIdle);
        }
        if (this.mForceIdle || (!this.mCharging && !isScreenBlockingInactive)) {
            if (this.mDeepEnabled) {
                if (this.mQuickDozeActivated) {
                    int i = this.mState;
                    if (i != 7 && i != 5 && i != 6) {
                        if (DEBUG) {
                            Slog.i(TAG, "Moved from " + stateToString(this.mState) + " to STATE_QUICK_DOZE_DELAY");
                        }
                        this.mState = 7;
                        resetIdleManagementLocked();
                        if (this.mActiveReason == 7) {
                            scheduleNoWakeupAlarmLockedForQuickDoze(this.mConstants.QUICK_DOZE_DELAY_TIMEOUT);
                        } else {
                            scheduleAlarmLocked(this.mConstants.QUICK_DOZE_DELAY_TIMEOUT, false);
                        }
                        EventLogTags.writeDeviceIdle(this.mState, "no activity");
                    } else {
                        return;
                    }
                } else if (this.mState == 0) {
                    this.mState = 1;
                    if (DEBUG) {
                        Slog.i(TAG, "Moved from STATE_ACTIVE to STATE_INACTIVE");
                    }
                    resetIdleManagementLocked();
                    long delay = this.mInactiveTimeout;
                    if (shouldUseIdleTimeoutFactorLocked()) {
                        delay = (long) (this.mPreIdleFactor * ((float) delay));
                    }
                    scheduleAlarmLocked(delay, false);
                    EventLogTags.writeDeviceIdle(this.mState, "no activity");
                }
            }
            if (this.mLightState == 0 && this.mLightEnabled) {
                this.mLightState = 1;
                if (DEBUG) {
                    Slog.i(TAG, "Moved from LIGHT_STATE_ACTIVE to LIGHT_STATE_INACTIVE");
                }
                resetLightIdleManagementLocked();
                scheduleLightAlarmLocked(this.mConstants.LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT);
                EventLogTags.writeDeviceIdleLight(this.mLightState, "no activity");
            }
        }
    }

    private void resetIdleManagementLocked() {
        this.mNextIdlePendingDelay = 0;
        this.mNextIdleDelay = 0;
        if (this.mLightState == 0) {
            this.mNextLightIdleDelay = 0;
        }
        this.mIdleStartTime = 0;
        cancelAlarmLocked();
        cancelSensingTimeoutAlarmLocked();
        cancelLocatingLocked();
        stopMonitoringMotionLocked();
        this.mAnyMotionDetector.stop();
        updateActiveConstraintsLocked();
    }

    private void resetLightIdleManagementLocked() {
        cancelLightAlarmLocked();
    }

    /* access modifiers changed from: package-private */
    public void exitForceIdleLocked() {
        if (this.mForceIdle) {
            this.mForceIdle = false;
            if (this.mScreenOn || this.mCharging) {
                this.mActiveReason = 6;
                becomeActiveLocked("exit-force", Process.myUid());
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setLightStateForTest(int lightState) {
        synchronized (this) {
            this.mLightState = lightState;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getLightState() {
        return this.mLightState;
    }

    /* access modifiers changed from: package-private */
    public void stepLightIdleStateLocked(String reason) {
        if (this.mLightState != 7) {
            if (DEBUG) {
                Slog.i(TAG, "stepLightIdleStateLocked: mLightState=" + this.mLightState);
            }
            EventLogTags.writeDeviceIdleLightStep();
            int i = this.mLightState;
            if (i == 1) {
                this.mCurIdleBudget = this.mConstants.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET;
                this.mNextLightIdleDelay = this.mConstants.LIGHT_IDLE_TIMEOUT;
                this.mMaintenanceStartTime = 0;
                if (!isOpsInactiveLocked()) {
                    this.mLightState = 3;
                    EventLogTags.writeDeviceIdleLight(this.mLightState, reason);
                    scheduleLightAlarmLocked(this.mConstants.LIGHT_PRE_IDLE_TIMEOUT);
                    return;
                }
            } else if (i != 3) {
                if (i == 4 || i == 5) {
                    if (this.mNetworkConnected || this.mLightState == 5) {
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
                            Slog.i(TAG, "Moved from LIGHT_STATE_IDLE to LIGHT_STATE_IDLE_MAINTENANCE.");
                        }
                        this.mLightState = 6;
                        EventLogTags.writeDeviceIdleLight(this.mLightState, reason);
                        addEvent(3, null);
                        this.mHandler.sendEmptyMessage(4);
                        return;
                    }
                    scheduleLightAlarmLocked(this.mNextLightIdleDelay);
                    if (DEBUG) {
                        Slog.i(TAG, "Moved to LIGHT_WAITING_FOR_NETWORK.");
                    }
                    this.mLightState = 5;
                    EventLogTags.writeDeviceIdleLight(this.mLightState, reason);
                    return;
                } else if (i != 6) {
                    return;
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
                Slog.i(TAG, "Moved to LIGHT_STATE_IDLE.");
            }
            this.mLightState = 4;
            EventLogTags.writeDeviceIdleLight(this.mLightState, reason);
            addEvent(2, null);
            this.mGoingIdleWakeLock.acquire();
            this.mHandler.sendEmptyMessage(3);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getState() {
        return this.mState;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x014e  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x01c1  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x01e9  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x021b  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0228  */
    @VisibleForTesting
    public void stepIdleStateLocked(String reason) {
        if (DEBUG) {
            Slog.i(TAG, "stepIdleStateLocked: mState=" + this.mState);
        }
        EventLogTags.writeDeviceIdleStep();
        if (this.mConstants.MIN_TIME_TO_ALARM + SystemClock.elapsedRealtime() > this.mAlarmManager.getNextWakeFromIdleTime()) {
            if (this.mState != 0) {
                this.mActiveReason = 7;
                becomeActiveLocked("alarm", Process.myUid());
                becomeInactiveIfAppropriateLocked();
            }
        } else if (this.mNumBlockingConstraints == 0 || this.mForceIdle) {
            switch (this.mState) {
                case 1:
                    startMonitoringMotionLocked();
                    long delay = this.mConstants.IDLE_AFTER_INACTIVE_TIMEOUT;
                    if (shouldUseIdleTimeoutFactorLocked()) {
                        delay = (long) (this.mPreIdleFactor * ((float) delay));
                    }
                    scheduleAlarmLocked(delay, false);
                    moveToStateLocked(2, reason);
                    return;
                case 2:
                    moveToStateLocked(3, reason);
                    cancelLocatingLocked();
                    this.mLocated = false;
                    this.mLastGenericLocation = null;
                    this.mLastGpsLocation = null;
                    updateActiveConstraintsLocked();
                    if (this.mUseMotionSensor && this.mAnyMotionDetector.hasSensor()) {
                        scheduleSensingTimeoutAlarmLocked(this.mConstants.SENSING_TIMEOUT);
                        this.mNotMoving = false;
                        if (!this.mForceIdle) {
                            this.mAnyMotionDetector.checkForAnyMotion();
                            return;
                        }
                        return;
                    } else if (this.mNumBlockingConstraints != 0) {
                        cancelAlarmLocked();
                        return;
                    } else {
                        this.mNotMoving = true;
                        cancelSensingTimeoutAlarmLocked();
                        moveToStateLocked(4, reason);
                        scheduleAlarmLocked(this.mConstants.LOCATING_TIMEOUT, false);
                        LocationManager locationManager = this.mInjector.getLocationManager();
                        if (this.mForceIdle) {
                            if (locationManager == null || locationManager.getProvider("network") == null) {
                                this.mHasNetworkLocation = false;
                            } else {
                                locationManager.requestLocationUpdates(this.mLocationRequest, this.mGenericLocationListener, this.mHandler.getLooper());
                                this.mLocating = true;
                            }
                            if (locationManager == null || locationManager.getProvider("gps") == null) {
                                this.mHasGps = false;
                            } else {
                                this.mHasGps = true;
                                locationManager.requestLocationUpdates("gps", 1000, 5.0f, this.mGpsLocationListener, this.mHandler.getLooper());
                                this.mLocating = true;
                            }
                            if (this.mLocating) {
                                return;
                            }
                            cancelAlarmLocked();
                            cancelLocatingLocked();
                            this.mAnyMotionDetector.stop();
                            this.mNextIdlePendingDelay = this.mConstants.IDLE_PENDING_TIMEOUT;
                            this.mNextIdleDelay = this.mConstants.IDLE_TIMEOUT;
                            scheduleAlarmLocked(this.mNextIdleDelay, true);
                            if (DEBUG) {
                                Slog.i(TAG, "Moved to STATE_IDLE. Next alarm in " + this.mNextIdleDelay + " ms.");
                            }
                            this.mNextIdleDelay = (long) (((float) this.mNextIdleDelay) * this.mConstants.IDLE_FACTOR);
                            if (DEBUG) {
                                Slog.i(TAG, "Setting mNextIdleDelay = " + this.mNextIdleDelay);
                            }
                            this.mIdleStartTime = SystemClock.elapsedRealtime();
                            this.mNextIdleDelay = Math.min(this.mNextIdleDelay, this.mConstants.MAX_IDLE_TIMEOUT);
                            if (this.mNextIdleDelay < this.mConstants.IDLE_TIMEOUT) {
                                this.mNextIdleDelay = this.mConstants.IDLE_TIMEOUT;
                            }
                            moveToStateLocked(5, reason);
                            if (this.mLightState != 7) {
                                this.mLightState = 7;
                                cancelLightAlarmLocked();
                            }
                            addEvent(4, null);
                            this.mGoingIdleWakeLock.acquire(this.mConstants.LIGHT_IDLE_TIMEOUT);
                            this.mHandler.sendEmptyMessage(2);
                            return;
                        } else if (DEBUG) {
                            Slog.i(TAG, "forceidle, not check locating");
                            return;
                        } else {
                            return;
                        }
                    }
                case 3:
                    cancelSensingTimeoutAlarmLocked();
                    moveToStateLocked(4, reason);
                    scheduleAlarmLocked(this.mConstants.LOCATING_TIMEOUT, false);
                    LocationManager locationManager2 = this.mInjector.getLocationManager();
                    if (this.mForceIdle) {
                    }
                    break;
                case 4:
                    cancelAlarmLocked();
                    cancelLocatingLocked();
                    this.mAnyMotionDetector.stop();
                    this.mNextIdlePendingDelay = this.mConstants.IDLE_PENDING_TIMEOUT;
                    this.mNextIdleDelay = this.mConstants.IDLE_TIMEOUT;
                    scheduleAlarmLocked(this.mNextIdleDelay, true);
                    if (DEBUG) {
                    }
                    this.mNextIdleDelay = (long) (((float) this.mNextIdleDelay) * this.mConstants.IDLE_FACTOR);
                    if (DEBUG) {
                    }
                    this.mIdleStartTime = SystemClock.elapsedRealtime();
                    this.mNextIdleDelay = Math.min(this.mNextIdleDelay, this.mConstants.MAX_IDLE_TIMEOUT);
                    if (this.mNextIdleDelay < this.mConstants.IDLE_TIMEOUT) {
                    }
                    moveToStateLocked(5, reason);
                    if (this.mLightState != 7) {
                    }
                    addEvent(4, null);
                    this.mGoingIdleWakeLock.acquire(this.mConstants.LIGHT_IDLE_TIMEOUT);
                    this.mHandler.sendEmptyMessage(2);
                    return;
                case 5:
                    this.mActiveIdleOpCount = 1;
                    this.mActiveIdleWakeLock.acquire();
                    scheduleAlarmLocked(this.mNextIdlePendingDelay, false);
                    if (DEBUG) {
                        Slog.i(TAG, "Moved from STATE_IDLE to STATE_IDLE_MAINTENANCE. Next alarm in " + this.mNextIdlePendingDelay + " ms.");
                    }
                    this.mMaintenanceStartTime = SystemClock.elapsedRealtime();
                    this.mNextIdlePendingDelay = Math.min(this.mConstants.MAX_IDLE_PENDING_TIMEOUT, (long) (((float) this.mNextIdlePendingDelay) * this.mConstants.IDLE_PENDING_FACTOR));
                    if (this.mNextIdlePendingDelay < this.mConstants.IDLE_PENDING_TIMEOUT) {
                        this.mNextIdlePendingDelay = this.mConstants.IDLE_PENDING_TIMEOUT;
                    }
                    moveToStateLocked(6, reason);
                    addEvent(5, null);
                    this.mHandler.sendEmptyMessage(4);
                    return;
                case 6:
                    scheduleAlarmLocked(this.mNextIdleDelay, true);
                    if (DEBUG) {
                    }
                    this.mNextIdleDelay = (long) (((float) this.mNextIdleDelay) * this.mConstants.IDLE_FACTOR);
                    if (DEBUG) {
                    }
                    this.mIdleStartTime = SystemClock.elapsedRealtime();
                    this.mNextIdleDelay = Math.min(this.mNextIdleDelay, this.mConstants.MAX_IDLE_TIMEOUT);
                    if (this.mNextIdleDelay < this.mConstants.IDLE_TIMEOUT) {
                    }
                    moveToStateLocked(5, reason);
                    if (this.mLightState != 7) {
                    }
                    addEvent(4, null);
                    this.mGoingIdleWakeLock.acquire(this.mConstants.LIGHT_IDLE_TIMEOUT);
                    this.mHandler.sendEmptyMessage(2);
                    return;
                case 7:
                    this.mNextIdlePendingDelay = this.mConstants.IDLE_PENDING_TIMEOUT;
                    this.mNextIdleDelay = this.mConstants.IDLE_TIMEOUT;
                    scheduleAlarmLocked(this.mNextIdleDelay, true);
                    if (DEBUG) {
                    }
                    this.mNextIdleDelay = (long) (((float) this.mNextIdleDelay) * this.mConstants.IDLE_FACTOR);
                    if (DEBUG) {
                    }
                    this.mIdleStartTime = SystemClock.elapsedRealtime();
                    this.mNextIdleDelay = Math.min(this.mNextIdleDelay, this.mConstants.MAX_IDLE_TIMEOUT);
                    if (this.mNextIdleDelay < this.mConstants.IDLE_TIMEOUT) {
                    }
                    moveToStateLocked(5, reason);
                    if (this.mLightState != 7) {
                    }
                    addEvent(4, null);
                    this.mGoingIdleWakeLock.acquire(this.mConstants.LIGHT_IDLE_TIMEOUT);
                    this.mHandler.sendEmptyMessage(2);
                    return;
                default:
                    return;
            }
        } else if (DEBUG) {
            Slog.i(TAG, "Cannot step idle state. Blocked by: " + ((String) this.mConstraints.values().stream().filter($$Lambda$DeviceIdleController$XXLrlAvMnQRbDjUCgRxYn4rjwzM.INSTANCE).map($$Lambda$DeviceIdleController$TvIYx9dFl_39KgsbmlgCmJyl8.INSTANCE).collect(Collectors.joining(","))));
        }
    }

    private void moveToStateLocked(int state, String reason) {
        int oldState = this.mState;
        this.mState = state;
        if (DEBUG) {
            Slog.i(TAG, String.format("Moved from STATE_%s to STATE_%s.", stateToString(oldState), stateToString(this.mState)));
        }
        EventLogTags.writeDeviceIdle(this.mState, reason);
        updateActiveConstraintsLocked();
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
    @VisibleForTesting
    public void setActiveIdleOpsForTest(int count) {
        synchronized (this) {
            this.mActiveIdleOpCount = count;
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
    @VisibleForTesting
    public int setPreIdleTimeoutMode(int mode) {
        return setPreIdleTimeoutFactor(getPreIdleTimeoutByMode(mode));
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public float getPreIdleTimeoutByMode(int mode) {
        if (mode == 0) {
            return 1.0f;
        }
        if (mode == 1) {
            return this.mConstants.PRE_IDLE_FACTOR_LONG;
        }
        if (mode == 2) {
            return this.mConstants.PRE_IDLE_FACTOR_SHORT;
        }
        Slog.w(TAG, "Invalid time out factor mode: " + mode);
        return 1.0f;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public float getPreIdleTimeoutFactor() {
        return this.mPreIdleFactor;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int setPreIdleTimeoutFactor(float ratio) {
        if (!this.mDeepEnabled) {
            if (!DEBUG) {
                return 2;
            }
            Slog.d(TAG, "setPreIdleTimeoutFactor: Deep Idle disable");
            return 2;
        } else if (ratio <= MIN_PRE_IDLE_FACTOR_CHANGE) {
            if (!DEBUG) {
                return 3;
            }
            Slog.d(TAG, "setPreIdleTimeoutFactor: Invalid input");
            return 3;
        } else if (Math.abs(ratio - this.mPreIdleFactor) >= MIN_PRE_IDLE_FACTOR_CHANGE) {
            synchronized (this) {
                this.mLastPreIdleFactor = this.mPreIdleFactor;
                this.mPreIdleFactor = ratio;
            }
            if (DEBUG) {
                Slog.d(TAG, "setPreIdleTimeoutFactor: " + ratio);
            }
            postUpdatePreIdleFactor();
            return 1;
        } else if (!DEBUG) {
            return 0;
        } else {
            Slog.d(TAG, "setPreIdleTimeoutFactor: New factor same as previous factor");
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void resetPreIdleTimeoutMode() {
        synchronized (this) {
            this.mLastPreIdleFactor = this.mPreIdleFactor;
            this.mPreIdleFactor = 1.0f;
        }
        if (DEBUG) {
            Slog.d(TAG, "resetPreIdleTimeoutMode to 1.0");
        }
        postResetPreIdleTimeoutFactor();
    }

    private void postUpdatePreIdleFactor() {
        this.mHandler.sendEmptyMessage(11);
    }

    private void postResetPreIdleTimeoutFactor() {
        this.mHandler.sendEmptyMessage(12);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updatePreIdleFactor() {
        synchronized (this) {
            if (shouldUseIdleTimeoutFactorLocked()) {
                if (this.mState == 1 || this.mState == 2) {
                    if (this.mNextAlarmTime != 0) {
                        long delay = this.mNextAlarmTime - SystemClock.elapsedRealtime();
                        if (delay >= 60000) {
                            long newDelay = (long) ((((float) delay) / this.mLastPreIdleFactor) * this.mPreIdleFactor);
                            if (Math.abs(delay - newDelay) >= 60000) {
                                scheduleAlarmLocked(newDelay, false);
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void maybeDoImmediateMaintenance() {
        synchronized (this) {
            if (this.mState == 5 && SystemClock.elapsedRealtime() - this.mIdleStartTime > this.mConstants.IDLE_TIMEOUT) {
                scheduleAlarmLocked(0, false);
            }
        }
    }

    private boolean shouldUseIdleTimeoutFactorLocked() {
        if (this.mActiveReason == 1) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setIdleStartTimeForTest(long idleStartTime) {
        synchronized (this) {
            this.mIdleStartTime = idleStartTime;
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
    @VisibleForTesting
    public long getNextAlarmTime() {
        return this.mNextAlarmTime;
    }

    /* access modifiers changed from: package-private */
    public boolean isOpsInactiveLocked() {
        return this.mActiveIdleOpCount <= 0 && !this.mJobsActive && !this.mAlarmsActive;
    }

    /* access modifiers changed from: package-private */
    public void exitMaintenanceEarlyIfNeededLocked() {
        int i;
        if ((this.mState == 6 || (i = this.mLightState) == 6 || i == 3) && isOpsInactiveLocked()) {
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
            Slog.i(TAG, "motionLocked()");
        }
        handleMotionDetectedLocked(this.mConstants.MOTION_INACTIVE_TIMEOUT, "motion");
    }

    /* access modifiers changed from: package-private */
    public void handleMotionDetectedLocked(long timeout, String type) {
        boolean becomeInactive = this.mState != 0 || this.mLightState == 7;
        becomeActiveLocked(type, Process.myUid(), timeout, this.mLightState == 7);
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
            Slog.i(TAG, "Generic location: " + location);
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
            Slog.i(TAG, "GPS location: " + location);
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
            Slog.i(TAG, "startMonitoringMotionLocked()");
        }
        if (this.mMotionSensor != null && !this.mMotionListener.active) {
            this.mMotionListener.registerLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void stopMonitoringMotionLocked() {
        if (DEBUG) {
            Slog.i(TAG, "stopMonitoringMotionLocked()");
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
            LocationManager locationManager = this.mInjector.getLocationManager();
            locationManager.removeUpdates(this.mGenericLocationListener);
            locationManager.removeUpdates(this.mGpsLocationListener);
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
    public void scheduleNoWakeupAlarmLockedForQuickDoze(long delay) {
        int i;
        if (DEBUG) {
            Slog.i(TAG, "scheduleNoWakeupAlarmLockedForQuickDoze(" + delay + ", false)");
        }
        if (!this.mUseMotionSensor || this.mMotionSensor != null || (i = this.mState) == 7 || i == 5 || i == 6) {
            this.mNextAlarmTime = SystemClock.elapsedRealtime() + delay;
            this.mAlarmManager.set(3, this.mNextAlarmTime, "DeviceIdleController.deep", this.mDeepAlarmListener, this.mHandler);
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleAlarmLocked(long delay, boolean idleUntil) {
        int i;
        if (DEBUG) {
            Slog.i(TAG, "scheduleAlarmLocked(" + delay + ", " + idleUntil + ")");
        }
        if (!this.mUseMotionSensor || this.mMotionSensor != null || (i = this.mState) == 7 || i == 5 || i == 6) {
            this.mNextAlarmTime = SystemClock.elapsedRealtime() + delay;
            if (idleUntil) {
                this.mAlarmManager.setIdleUntil(2, this.mNextAlarmTime, "DeviceIdleController.deep", this.mDeepAlarmListener, this.mHandler);
            } else {
                this.mAlarmManager.set(2, this.mNextAlarmTime, "DeviceIdleController.deep", this.mDeepAlarmListener, this.mHandler);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleLightAlarmLocked(long delay) {
        if (DEBUG) {
            Slog.i(TAG, "scheduleLightAlarmLocked(" + delay + ")");
        }
        this.mNextLightAlarmTime = SystemClock.elapsedRealtime() + delay;
        this.mAlarmManager.set(2, this.mNextLightAlarmTime, "DeviceIdleController.light", this.mLightAlarmListener, this.mHandler);
    }

    /* access modifiers changed from: package-private */
    public void scheduleSensingTimeoutAlarmLocked(long delay) {
        if (DEBUG) {
            Slog.i(TAG, "scheduleSensingAlarmLocked(" + delay + ")");
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
        ActivityManagerInternal activityManagerInternal = this.mLocalActivityManager;
        if (activityManagerInternal != null) {
            activityManagerInternal.setDeviceIdleWhitelist(this.mPowerSaveWhitelistAllAppIdArray, this.mPowerSaveWhitelistExceptIdleAppIdArray);
        }
        if (this.mLocalPowerManager != null) {
            if (DEBUG) {
                Slog.i(TAG, "Setting wakelock whitelist to " + Arrays.toString(this.mPowerSaveWhitelistAllAppIdArray));
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
                Slog.i(TAG, "Setting activity manager temp whitelist to " + Arrays.toString(this.mTempWhitelistAppIdArray));
            }
            this.mLocalActivityManager.updateDeviceIdleTempWhitelist(this.mTempWhitelistAppIdArray, appId, adding);
        }
        if (this.mLocalPowerManager != null) {
            if (DEBUG) {
                Slog.i(TAG, "Setting wakelock temp whitelist to " + Arrays.toString(this.mTempWhitelistAppIdArray));
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
            Slog.i(TAG, "Reading config from " + this.mConfigFile.getBaseFile());
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
    /* access modifiers changed from: public */
    private boolean updateWhitelistFromDB(boolean ignoreDbNotExist) {
        PackageManager pm = getContext().getPackageManager();
        Bundle bundle = null;
        ArrayList<String> protectlist = null;
        try {
            Slog.d(TAG, "begin to read protectlist from DB");
            bundle = getContext().getContentResolver().call(Uri.parse("content://com.huawei.android.smartpowerprovider"), "hsm_get_freeze_list", "protect", (Bundle) null);
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
            /* class com.android.server.DeviceIdleController.AnonymousClass11 */

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

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0062  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x009a  */
    private void readConfigFileLocked(XmlPullParser parser) {
        int type;
        PackageManager pm = getContext().getPackageManager();
        while (true) {
            try {
                type = parser.next();
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
            if (type == 2 || type == 1) {
                break;
            }
        }
        if (type == 2) {
            int outerDepth = parser.getDepth();
            while (true) {
                int type2 = parser.next();
                if (type2 == 1) {
                    return;
                }
                if (type2 == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (type2 != 3) {
                    if (type2 != 4) {
                        String tagName = parser.getName();
                        char c = 65535;
                        int hashCode = tagName.hashCode();
                        if (hashCode != 3797) {
                            if (hashCode == 111376009 && tagName.equals("un-wl")) {
                                c = 1;
                                if (c == 0) {
                                    String name = parser.getAttributeValue(null, "n");
                                    if (name != null) {
                                        try {
                                            ApplicationInfo ai = pm.getApplicationInfo(name, DumpState.DUMP_CHANGES);
                                            this.mPowerSaveWhitelistUserApps.put(ai.packageName, Integer.valueOf(UserHandle.getAppId(ai.uid)));
                                        } catch (PackageManager.NameNotFoundException e7) {
                                        }
                                    }
                                } else if (c != 1) {
                                    Slog.w(TAG, "Unknown element under <config>: " + parser.getName());
                                    XmlUtils.skipCurrentTag(parser);
                                } else {
                                    String packageName = parser.getAttributeValue(null, "n");
                                    if (this.mPowerSaveWhitelistApps.containsKey(packageName)) {
                                        this.mRemovedFromSystemWhitelistApps.put(packageName, this.mPowerSaveWhitelistApps.remove(packageName));
                                    }
                                }
                            }
                        } else if (tagName.equals("wl")) {
                            c = 0;
                            if (c == 0) {
                            }
                        }
                        if (c == 0) {
                        }
                    }
                }
            }
        } else {
            throw new IllegalStateException("no start tag found");
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
        pw.println("  pre-idle-factor [0|1|2]");
        pw.println("    Set a new factor to idle time before step to idle(inactive_to and idle_after_inactive_to)");
        pw.println("  reset-pre-idle-factor");
        pw.println("    Reset factor to idle time to default");
    }

    /* access modifiers changed from: package-private */
    public class Shell extends ShellCommand {
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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:175:0x02e9, code lost:
        if ("all".equals(r3) == false) goto L_0x02f8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:217:0x038d, code lost:
        if ("all".equals(r3) == false) goto L_0x039c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:448:0x07c6, code lost:
        r10.println("Package must be prefixed with + or - " + r1);
        android.os.Binder.restoreCallingIdentity(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:449:0x07de, code lost:
        return -1;
     */
    public int onShellCommand(Shell shell, String cmd) {
        Throwable th;
        Exception e;
        PrintWriter pw = shell.getOutPrintWriter();
        int i = 0;
        if ("step".equals(cmd)) {
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
                    } catch (Throwable th2) {
                        Binder.restoreCallingIdentity(token);
                        throw th2;
                    }
                }
                stepIdleStateLocked("s:shell");
                pw.print("Stepped to deep: ");
                pw.println(stateToString(this.mState));
                Binder.restoreCallingIdentity(token);
            }
        } else {
            char c = 4;
            if ("force-idle".equals(cmd)) {
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
                                    int curLightState = this.mLightState;
                                    while (curLightState != 4) {
                                        stepLightIdleStateLocked("s:shell");
                                        if (curLightState == this.mLightState) {
                                            pw.print("Unable to go light idle; stopped at ");
                                            pw.println(lightStateToString(this.mLightState));
                                            exitForceIdleLocked();
                                            return -1;
                                        }
                                        curLightState = this.mLightState;
                                    }
                                    pw.println("Now forced in to light idle mode");
                                } else {
                                    pw.println("Unknown idle mode: " + arg2);
                                }
                                Binder.restoreCallingIdentity(token2);
                            }
                        } finally {
                            Binder.restoreCallingIdentity(token2);
                        }
                    }
                    if (!this.mDeepEnabled) {
                        pw.println("Unable to go deep idle; not enabled");
                        Binder.restoreCallingIdentity(token2);
                        return -1;
                    }
                    this.mForceIdle = true;
                    becomeInactiveIfAppropriateLocked();
                    int curState = this.mState;
                    while (curState != 5) {
                        stepIdleStateLocked("s:shell");
                        if (curState == this.mState) {
                            pw.print("Unable to go deep idle; stopped at ");
                            pw.println(stateToString(this.mState));
                            exitForceIdleLocked();
                            Binder.restoreCallingIdentity(token2);
                            return -1;
                        }
                        curState = this.mState;
                    }
                    pw.println("Now forced in to deep idle mode");
                    Binder.restoreCallingIdentity(token2);
                }
            } else if ("force-inactive".equals(cmd)) {
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
            } else if ("unforce".equals(cmd)) {
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
            } else if ("get".equals(cmd)) {
                getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                synchronized (this) {
                    String arg3 = shell.getNextArg();
                    if (arg3 != null) {
                        long token5 = Binder.clearCallingIdentity();
                        try {
                            switch (arg3.hashCode()) {
                                case -907689876:
                                    if (arg3.equals("screen")) {
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 3079404:
                                    if (arg3.equals("deep")) {
                                        c = 1;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 97618667:
                                    if (arg3.equals("force")) {
                                        c = 2;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 102970646:
                                    if (arg3.equals("light")) {
                                        c = 0;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 107947501:
                                    if (arg3.equals("quick")) {
                                        c = 3;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 1436115569:
                                    if (arg3.equals("charging")) {
                                        c = 5;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 1843485230:
                                    if (arg3.equals("network")) {
                                        c = 6;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                default:
                                    c = 65535;
                                    break;
                            }
                            switch (c) {
                                case 0:
                                    pw.println(lightStateToString(this.mLightState));
                                    break;
                                case 1:
                                    pw.println(stateToString(this.mState));
                                    break;
                                case 2:
                                    pw.println(this.mForceIdle);
                                    break;
                                case 3:
                                    pw.println(this.mQuickDozeActivated);
                                    break;
                                case 4:
                                    pw.println(this.mScreenOn);
                                    break;
                                case 5:
                                    pw.println(this.mCharging);
                                    break;
                                case 6:
                                    pw.println(this.mNetworkConnected);
                                    break;
                                default:
                                    pw.println("Unknown get option: " + arg3);
                                    break;
                            }
                        } finally {
                            Binder.restoreCallingIdentity(token5);
                        }
                    } else {
                        pw.println("Argument required");
                    }
                }
            } else if ("disable".equals(cmd)) {
                getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                synchronized (this) {
                    long token6 = Binder.clearCallingIdentity();
                    String arg4 = shell.getNextArg();
                    boolean becomeActive = false;
                    boolean valid = false;
                    if (arg4 != null) {
                        try {
                            if (!"deep".equals(arg4)) {
                            }
                        } catch (Throwable th3) {
                            Binder.restoreCallingIdentity(token6);
                            throw th3;
                        }
                    }
                    valid = true;
                    if (this.mDeepEnabled) {
                        this.mDeepEnabled = false;
                        becomeActive = true;
                        pw.println("Deep idle mode disabled");
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
                        this.mActiveReason = 6;
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
            } else if ("enable".equals(cmd)) {
                getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                synchronized (this) {
                    long token7 = Binder.clearCallingIdentity();
                    String arg5 = shell.getNextArg();
                    boolean becomeInactive = false;
                    boolean valid2 = false;
                    if (arg5 != null) {
                        try {
                            if (!"deep".equals(arg5)) {
                            }
                        } catch (Throwable th4) {
                            Binder.restoreCallingIdentity(token7);
                            throw th4;
                        }
                    }
                    valid2 = true;
                    if (!this.mDeepEnabled) {
                        this.mDeepEnabled = true;
                        becomeInactive = true;
                        pw.println("Deep idle mode enabled");
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
            } else if ("enabled".equals(cmd)) {
                synchronized (this) {
                    String arg6 = shell.getNextArg();
                    if (arg6 != null) {
                        if (!"all".equals(arg6)) {
                            if ("deep".equals(arg6)) {
                                if (this.mDeepEnabled) {
                                    i = "1";
                                }
                                pw.println(i);
                            } else if ("light".equals(arg6)) {
                                if (this.mLightEnabled) {
                                    i = "1";
                                }
                                pw.println(i);
                            } else {
                                pw.println("Unknown idle mode: " + arg6);
                            }
                        }
                    }
                    if (this.mDeepEnabled && this.mLightEnabled) {
                        i = "1";
                    }
                    pw.println(i);
                }
            } else {
                char c2 = '=';
                char c3 = '-';
                char c4 = '+';
                if ("whitelist".equals(cmd)) {
                    String arg7 = shell.getNextArg();
                    if (arg7 != null) {
                        getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                        long token8 = Binder.clearCallingIdentity();
                        while (true) {
                            try {
                                if (arg7.length() < 1) {
                                    break;
                                }
                                if (!(arg7.charAt(0) == '-' || arg7.charAt(0) == c4 || arg7.charAt(0) == c2)) {
                                    break;
                                }
                                char op = arg7.charAt(0);
                                String pkg = arg7.substring(1);
                                if (op == c4) {
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
                                String nextArg = shell.getNextArg();
                                arg7 = nextArg;
                                if (nextArg == null) {
                                    break;
                                }
                                c2 = '=';
                                c4 = '+';
                            } finally {
                                Binder.restoreCallingIdentity(token8);
                            }
                        }
                        pw.println("Package must be prefixed with +, -, or =: " + arg7);
                        Binder.restoreCallingIdentity(token8);
                        return -1;
                    }
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
                } else if ("tempwhitelist".equals(cmd)) {
                    long duration = 10000;
                    boolean removePkg = false;
                    while (true) {
                        String opt = shell.getNextOption();
                        if (opt == null) {
                            String arg8 = shell.getNextArg();
                            if (arg8 != null) {
                                if (removePkg) {
                                    try {
                                        removePowerSaveTempWhitelistAppChecked(arg8, shell.userId);
                                    } catch (Exception e2) {
                                        e = e2;
                                        pw.println("Failed: " + e);
                                        return -1;
                                    }
                                } else {
                                    try {
                                        try {
                                            addPowerSaveTempWhitelistAppChecked(arg8, duration, shell.userId, "shell");
                                        } catch (Exception e3) {
                                            e = e3;
                                        }
                                    } catch (Exception e4) {
                                        e = e4;
                                        pw.println("Failed: " + e);
                                        return -1;
                                    }
                                }
                            } else if (removePkg) {
                                pw.println("[-r] requires a package name");
                                return -1;
                            } else {
                                dumpTempWhitelistSchedule(pw, false);
                            }
                        } else if ("-u".equals(opt)) {
                            String opt2 = shell.getNextArg();
                            if (opt2 == null) {
                                pw.println("-u requires a user number");
                                return -1;
                            }
                            shell.userId = Integer.parseInt(opt2);
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
                    }
                } else if ("except-idle-whitelist".equals(cmd)) {
                    getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                    long token9 = Binder.clearCallingIdentity();
                    try {
                        String arg9 = shell.getNextArg();
                        if (arg9 == null) {
                            pw.println("No arguments given");
                            return -1;
                        }
                        if ("reset".equals(arg9)) {
                            resetPowerSaveWhitelistExceptIdleInternal();
                        } else {
                            while (arg9.length() >= 1 && (arg9.charAt(0) == '-' || arg9.charAt(0) == '+' || arg9.charAt(0) == '=')) {
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
                                    Binder.restoreCallingIdentity(token9);
                                    return -1;
                                }
                                String nextArg2 = shell.getNextArg();
                                arg9 = nextArg2;
                                if (nextArg2 == null) {
                                }
                            }
                            pw.println("Package must be prefixed with +, -, or =: " + arg9);
                            Binder.restoreCallingIdentity(token9);
                            return -1;
                        }
                        Binder.restoreCallingIdentity(token9);
                    } finally {
                        Binder.restoreCallingIdentity(token9);
                    }
                } else if ("sys-whitelist".equals(cmd)) {
                    String arg10 = shell.getNextArg();
                    if (arg10 != null) {
                        getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                        long token10 = Binder.clearCallingIdentity();
                        try {
                            if (!"reset".equals(arg10)) {
                                while (true) {
                                    if (arg10.length() < 1) {
                                        break;
                                    }
                                    if (!(arg10.charAt(0) == c3 || arg10.charAt(0) == '+')) {
                                        break;
                                    }
                                    char op3 = arg10.charAt(0);
                                    String pkg3 = arg10.substring(1);
                                    if (op3 != '+') {
                                        if (op3 == c3) {
                                            if (removeSystemPowerWhitelistAppInternal(pkg3)) {
                                                pw.println("Removed " + pkg3);
                                            }
                                        }
                                    } else if (restoreSystemPowerWhitelistAppInternal(pkg3)) {
                                        pw.println("Restored " + pkg3);
                                    }
                                    String nextArg3 = shell.getNextArg();
                                    arg10 = nextArg3;
                                    if (nextArg3 == null) {
                                        break;
                                    }
                                    c3 = '-';
                                }
                            } else {
                                resetSystemPowerWhitelistInternal();
                            }
                        } finally {
                            Binder.restoreCallingIdentity(token10);
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
                } else if ("motion".equals(cmd)) {
                    getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                    synchronized (this) {
                        long token11 = Binder.clearCallingIdentity();
                        try {
                            motionLocked();
                            pw.print("Light state: ");
                            pw.print(lightStateToString(this.mLightState));
                            pw.print(", deep state: ");
                            pw.println(stateToString(this.mState));
                        } finally {
                            Binder.restoreCallingIdentity(token11);
                        }
                    }
                } else if ("pre-idle-factor".equals(cmd)) {
                    getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                    synchronized (this) {
                        long token12 = Binder.clearCallingIdentity();
                        int ret = -1;
                        try {
                            String arg11 = shell.getNextArg();
                            boolean valid3 = false;
                            if (arg11 != null) {
                                int mode = Integer.parseInt(arg11);
                                ret = setPreIdleTimeoutMode(mode);
                                if (ret == 1) {
                                    pw.println("pre-idle-factor: " + mode);
                                    valid3 = true;
                                } else if (ret == 2) {
                                    valid3 = true;
                                    pw.println("Deep idle not supported");
                                } else if (ret == 0) {
                                    valid3 = true;
                                    pw.println("Idle timeout factor not changed");
                                }
                            }
                            if (!valid3) {
                                pw.println("Unknown idle timeout factor: " + arg11 + ",(error code: " + ret + ")");
                            }
                            Binder.restoreCallingIdentity(token12);
                        } catch (NumberFormatException e5) {
                            pw.println("Unknown idle timeout factor,(error code: -1)");
                            Binder.restoreCallingIdentity(token12);
                        } catch (Throwable th5) {
                            th = th5;
                            Binder.restoreCallingIdentity(token12);
                            throw th;
                        }
                    }
                } else if (!"reset-pre-idle-factor".equals(cmd)) {
                    return shell.handleDefaultCommands(cmd);
                } else {
                    getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                    synchronized (this) {
                        long token13 = Binder.clearCallingIdentity();
                        try {
                            resetPreIdleTimeoutMode();
                        } finally {
                            Binder.restoreCallingIdentity(token13);
                        }
                    }
                }
            }
        }
        return 0;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v8, resolved type: com.android.server.DeviceIdleController$Shell */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r9v11, types: [android.os.Binder, com.android.server.DeviceIdleController$BinderService] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        String label;
        if (DumpUtils.checkDumpPermission(getContext(), TAG, pw)) {
            if (args != null) {
                int userId = 0;
                int i = 0;
                while (i < args.length) {
                    String arg = args[i];
                    if ("-h".equals(arg)) {
                        dumpHelp(pw);
                        return;
                    }
                    if ("-u".equals(arg)) {
                        i++;
                        if (i < args.length) {
                            userId = Integer.parseInt(args[i]);
                        }
                    } else if (!"-a".equals(arg)) {
                        if (arg.length() <= 0 || arg.charAt(0) != '-') {
                            Shell shell = new Shell();
                            shell.userId = userId;
                            String[] newArgs = new String[(args.length - i)];
                            System.arraycopy(args, i, newArgs, 0, args.length - i);
                            shell.exec(this.mBinderService, null, fd, null, newArgs, null, new ResultReceiver(null));
                            return;
                        }
                        pw.println("Unknown option: " + arg);
                        return;
                    }
                    i++;
                }
            }
            synchronized (this) {
                this.mConstants.dump(pw);
                if (this.mEventCmds[0] != 0) {
                    pw.println("  Idling history:");
                    long now = SystemClock.elapsedRealtime();
                    for (int i2 = 99; i2 >= 0; i2--) {
                        if (this.mEventCmds[i2] != 0) {
                            int i3 = this.mEventCmds[i2];
                            if (i3 == 1) {
                                label = "     normal";
                            } else if (i3 == 2) {
                                label = " light-idle";
                            } else if (i3 == 3) {
                                label = "light-maint";
                            } else if (i3 == 4) {
                                label = "  deep-idle";
                            } else if (i3 != 5) {
                                label = "         ??";
                            } else {
                                label = " deep-maint";
                            }
                            pw.print("    ");
                            pw.print(label);
                            pw.print(": ");
                            TimeUtils.formatDuration(this.mEventTimes[i2], now, pw);
                            if (this.mEventReasons[i2] != null) {
                                pw.print(" (");
                                pw.print(this.mEventReasons[i2]);
                                pw.print(")");
                            }
                            pw.println();
                        }
                    }
                }
                int size = this.mPowerSaveWhitelistAppsExceptIdle.size();
                if (size > 0) {
                    pw.println("  Whitelist (except idle) system apps:");
                    for (int i4 = 0; i4 < size; i4++) {
                        pw.print("    ");
                        pw.println(this.mPowerSaveWhitelistAppsExceptIdle.keyAt(i4));
                    }
                }
                int size2 = this.mPowerSaveWhitelistApps.size();
                if (size2 > 0) {
                    pw.println("  Whitelist system apps:");
                    for (int i5 = 0; i5 < size2; i5++) {
                        pw.print("    ");
                        pw.println(this.mPowerSaveWhitelistApps.keyAt(i5));
                    }
                }
                int size3 = this.mRemovedFromSystemWhitelistApps.size();
                if (size3 > 0) {
                    pw.println("  Removed from whitelist system apps:");
                    for (int i6 = 0; i6 < size3; i6++) {
                        pw.print("    ");
                        pw.println(this.mRemovedFromSystemWhitelistApps.keyAt(i6));
                    }
                }
                int size4 = this.mPowerSaveWhitelistUserApps.size();
                if (size4 > 0) {
                    pw.println("  Whitelist user apps:");
                    for (int i7 = 0; i7 < size4; i7++) {
                        pw.print("    ");
                        pw.println(this.mPowerSaveWhitelistUserApps.keyAt(i7));
                    }
                }
                int size5 = this.mPowerSaveWhitelistExceptIdleAppIds.size();
                if (size5 > 0) {
                    pw.println("  Whitelist (except idle) all app ids:");
                    for (int i8 = 0; i8 < size5; i8++) {
                        pw.print("    ");
                        pw.print(this.mPowerSaveWhitelistExceptIdleAppIds.keyAt(i8));
                        pw.println();
                    }
                }
                int size6 = this.mPowerSaveWhitelistUserAppIds.size();
                if (size6 > 0) {
                    pw.println("  Whitelist user app ids:");
                    for (int i9 = 0; i9 < size6; i9++) {
                        pw.print("    ");
                        pw.print(this.mPowerSaveWhitelistUserAppIds.keyAt(i9));
                        pw.println();
                    }
                }
                int size7 = this.mPowerSaveWhitelistAllAppIds.size();
                if (size7 > 0) {
                    pw.println("  Whitelist all app ids:");
                    for (int i10 = 0; i10 < size7; i10++) {
                        pw.print("    ");
                        pw.print(this.mPowerSaveWhitelistAllAppIds.keyAt(i10));
                        pw.println();
                    }
                }
                dumpTempWhitelistSchedule(pw, true);
                int size8 = this.mTempWhitelistAppIdArray != null ? this.mTempWhitelistAppIdArray.length : 0;
                if (size8 > 0) {
                    pw.println("  Temp whitelist app ids:");
                    for (int i11 = 0; i11 < size8; i11++) {
                        pw.print("    ");
                        pw.print(this.mTempWhitelistAppIdArray[i11]);
                        pw.println();
                    }
                }
                pw.print("  mLightEnabled=");
                pw.print(this.mLightEnabled);
                pw.print("  mDeepEnabled=");
                pw.println(this.mDeepEnabled);
                pw.print("  mForceIdle=");
                pw.println(this.mForceIdle);
                pw.print("  mUseMotionSensor=");
                pw.print(this.mUseMotionSensor);
                if (this.mUseMotionSensor) {
                    pw.print(" mMotionSensor=");
                    pw.println(this.mMotionSensor);
                } else {
                    pw.println();
                }
                pw.print("  mScreenOn=");
                pw.println(this.mScreenOn);
                pw.print("  mScreenLocked=");
                pw.println(this.mScreenLocked);
                pw.print("  mNetworkConnected=");
                pw.println(this.mNetworkConnected);
                pw.print("  mCharging=");
                pw.println(this.mCharging);
                if (this.mConstraints.size() != 0) {
                    pw.println("  mConstraints={");
                    for (int i12 = 0; i12 < this.mConstraints.size(); i12++) {
                        DeviceIdleConstraintTracker tracker = this.mConstraints.valueAt(i12);
                        pw.print("    \"");
                        pw.print(tracker.name);
                        pw.print("\"=");
                        if (tracker.minState == this.mState) {
                            pw.println(tracker.active);
                        } else {
                            pw.print("ignored <mMinState=");
                            pw.print(stateToString(tracker.minState));
                            pw.println(">");
                        }
                    }
                    pw.println("  }");
                }
                if (this.mUseMotionSensor) {
                    pw.print("  mMotionActive=");
                    pw.println(this.mMotionListener.active);
                    pw.print("  mNotMoving=");
                    pw.println(this.mNotMoving);
                }
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
                if (Math.abs(this.mPreIdleFactor - 1.0f) > MIN_PRE_IDLE_FACTOR_CHANGE) {
                    pw.print("  mPreIdleFactor=");
                    pw.println(this.mPreIdleFactor);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpTempWhitelistSchedule(PrintWriter pw, boolean printTitle) {
        int size = this.mTempWhitelistAppIdEndTimes.size();
        if (size > 0) {
            String prefix = "";
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
                Slog.i(TAG, "Unable to go idle; not enabled");
                return -1;
            } else if (this.mForceIdle) {
                Slog.i(TAG, "now it is in ForceIdle by dump");
                return 0;
            } else {
                this.mForceIdle = true;
                becomeInactiveIfAppropriateLocked();
                int curState = this.mState;
                while (curState != 5) {
                    stepIdleStateLocked("s:shell");
                    if (curState == this.mState) {
                        Slog.i(TAG, "Unable to go idle; stopped at " + stateToString(this.mState));
                        exitForceIdleLocked();
                        return -1;
                    }
                    curState = this.mState;
                }
                this.mForceIdle = false;
                Slog.i(TAG, "Now forced in to idle mode");
                return 0;
            }
        }
    }
}
