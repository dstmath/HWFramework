package com.android.server.notification;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.AutomaticZenRule;
import android.app.IActivityManager;
import android.app.INotificationManager;
import android.app.ITransientNotification;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManagerInternal;
import android.app.backup.BackupManager;
import android.app.usage.UsageStatsManagerInternal;
import android.common.HwFrameworkFactory;
import android.companion.ICompanionDeviceManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hdm.HwDeviceManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioManagerInternal;
import android.media.IRingtonePlayer;
import android.metrics.LogMaker;
import android.net.Uri;
import android.net.util.NetworkConstants;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.TransactionTooLargeException;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.rms.HwSysResource;
import android.service.notification.Adjustment;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.INotificationListener;
import android.service.notification.IStatusBarNotificationHolder;
import android.service.notification.NotificationRankingUpdate;
import android.service.notification.NotificationStats;
import android.service.notification.NotifyingApp;
import android.service.notification.SnoozeCriterion;
import android.service.notification.StatusBarNotification;
import android.service.notification.ZenModeConfig;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SplitNotificationUtils;
import android.util.Xml;
import android.util.proto.ProtoOutputStream;
import android.view.IWindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.BackgroundThread;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.AbsLocationManagerService;
import com.android.server.DeviceIdleController;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.audio.AudioService;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.notification.GroupHelper;
import com.android.server.notification.ManagedServices;
import com.android.server.notification.NotificationManagerService;
import com.android.server.notification.NotificationRecord;
import com.android.server.notification.RankingHelper;
import com.android.server.notification.SnoozeHelper;
import com.android.server.notification.ZenModeHelper;
import com.android.server.os.HwBootFail;
import com.android.server.pm.PackageManagerService;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.utils.PriorityDump;
import com.android.server.wm.WindowManagerInternal;
import com.huawei.pgmng.log.LogPower;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import libcore.io.IoUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class NotificationManagerService extends AbsNotificationManager {
    private static final String ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE = "huawei.intent.action.HWSYSTEMMANAGER_CHANGE_POWERMODE";
    private static final String ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE = "huawei.intent.action.HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE";
    /* access modifiers changed from: private */
    public static final String ACTION_NOTIFICATION_TIMEOUT = (NotificationManagerService.class.getSimpleName() + ".TIMEOUT");
    private static final String ATTR_VERSION = "version";
    private static final int CLOSE_SAVE_POWER = 0;
    static final boolean DBG = Log.isLoggable(TAG, 3);
    private static final int DB_VERSION = 1;
    static final boolean DEBUG_INTERRUPTIVENESS = SystemProperties.getBoolean("debug.notification.interruptiveness", false);
    static final float DEFAULT_MAX_NOTIFICATION_ENQUEUE_RATE = 5.0f;
    static final int DEFAULT_STREAM_TYPE = 5;
    static final long[] DEFAULT_VIBRATE_PATTERN = {0, 250, 250, 250};
    private static final long DELAY_FOR_ASSISTANT_TIME = 100;
    /* access modifiers changed from: private */
    public static final boolean DISABLE_MULTIWIN = SystemProperties.getBoolean("ro.huawei.disable_multiwindow", false);
    static final boolean ENABLE_BLOCKED_TOASTS = true;
    public static final boolean ENABLE_CHILD_NOTIFICATIONS = SystemProperties.getBoolean("debug.child_notifs", true);
    private static final int EVENTLOG_ENQUEUE_STATUS_IGNORED = 2;
    private static final int EVENTLOG_ENQUEUE_STATUS_NEW = 0;
    private static final int EVENTLOG_ENQUEUE_STATUS_UPDATE = 1;
    public static final List<String> EXPANDEDNTF_PKGS = new ArrayList();
    private static final String EXTRA_KEY = "key";
    static final int FINISH_TOKEN_TIMEOUT = 11000;
    /* access modifiers changed from: private */
    public static final boolean HWFLOW;
    private static final String HWSYSTEMMANAGER_PKG = "com.huawei.systemmanager";
    private static final int IS_TOP_FULL_SCREEN_TOKEN = 206;
    static final int LONG_DELAY = 3500;
    static final int MATCHES_CALL_FILTER_CONTACTS_TIMEOUT_MS = 3000;
    static final float MATCHES_CALL_FILTER_TIMEOUT_AFFINITY = 1.0f;
    static final int MAX_PACKAGE_NOTIFICATIONS = 50;
    static final int MESSAGE_DURATION_REACHED = 2;
    static final int MESSAGE_FINISH_TOKEN_TIMEOUT = 7;
    static final int MESSAGE_LISTENER_HINTS_CHANGED = 5;
    static final int MESSAGE_LISTENER_NOTIFICATION_FILTER_CHANGED = 6;
    private static final int MESSAGE_RANKING_SORT = 1001;
    private static final int MESSAGE_RECONSIDER_RANKING = 1000;
    static final int MESSAGE_SAVE_POLICY_FILE = 3;
    static final int MESSAGE_SEND_RANKING_UPDATE = 4;
    private static final long MIN_PACKAGE_OVERRATE_LOG_INTERVAL = 5000;
    /* access modifiers changed from: private */
    public static final int MY_PID = Process.myPid();
    /* access modifiers changed from: private */
    public static final int MY_UID = Process.myUid();
    private static final String NOTIFICATION_CENTER_ORIGIN_PKG = "hw_origin_sender_package_name";
    private static final String NOTIFICATION_CENTER_PKG = "com.huawei.android.pushagent";
    private static final long NOTIFICATION_UPDATE_REPORT_INTERVAL = 15000;
    private static final int N_BTW = 0;
    private static final int N_MUSIC = 2;
    private static final int N_NORMAL = 1;
    private static final int OPEN_SAVE_POWER = 3;
    private static final String PERMISSION = "com.huawei.android.launcher.permission.CHANGE_POWERMODE";
    private static final String POWER_MODE = "power_mode";
    private static final String POWER_SAVER_NOTIFICATION_WHITELIST = "super_power_save_notification_whitelist";
    private static final int REQUEST_CODE_TIMEOUT = 1;
    private static final String SCHEME_TIMEOUT = "timeout";
    static final int SHORT_DELAY = 2000;
    private static final String SHUTDOWN_LIMIT_POWERMODE = "shutdomn_limit_powermode";
    static final long SNOOZE_UNTIL_UNSPECIFIED = -1;
    static final String TAG = "NotificationService";
    private static final String TAG_NOTIFICATION_POLICY = "notification-policy";
    private static final String TELECOM_PKG = "com.android.server.telecom";
    static final int VIBRATE_PATTERN_MAXLEN = 17;
    private static final IBinder WHITELIST_TOKEN = new Binder();
    /* access modifiers changed from: private */
    public static final boolean mIsChina = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS));
    private AccessibilityManager mAccessibilityManager;
    private ActivityManager mActivityManager;
    private AlarmManager mAlarmManager;
    /* access modifiers changed from: private */
    public Predicate<String> mAllowedManagedServicePackages;
    private IActivityManager mAm;
    /* access modifiers changed from: private */
    public AppOpsManager mAppOps;
    private UsageStatsManagerInternal mAppUsageStats;
    /* access modifiers changed from: private */
    public Archive mArchive;
    /* access modifiers changed from: private */
    public NotificationAssistants mAssistants;
    Light mAttentionLight;
    AudioManager mAudioManager;
    AudioManagerInternal mAudioManagerInternal;
    @GuardedBy("mNotificationLock")
    final ArrayMap<Integer, ArrayMap<String, String>> mAutobundledSummaries = new ArrayMap<>();
    /* access modifiers changed from: private */
    public int mCallState;
    private ICompanionDeviceManager mCompanionManager;
    /* access modifiers changed from: private */
    public ConditionProviders mConditionProviders;
    private IDeviceIdleController mDeviceIdleController;
    /* access modifiers changed from: private */
    public boolean mDisableNotificationEffects;
    /* access modifiers changed from: private */
    public DevicePolicyManagerInternal mDpm;
    /* access modifiers changed from: private */
    public List<ComponentName> mEffectsSuppressors = new ArrayList();
    @GuardedBy("mNotificationLock")
    final ArrayList<NotificationRecord> mEnqueuedNotifications = new ArrayList<>();
    private long[] mFallbackVibrationPattern;
    final IBinder mForegroundToken = new Binder();
    protected boolean mGameDndStatus = false;
    /* access modifiers changed from: private */
    public GroupHelper mGroupHelper;
    /* access modifiers changed from: private */
    public WorkerHandler mHandler;
    protected boolean mInCall = false;
    /* access modifiers changed from: private */
    public final Binder mInCallBinder = new Binder();
    /* access modifiers changed from: private */
    public AudioAttributes mInCallNotificationAudioAttributes;
    /* access modifiers changed from: private */
    public Uri mInCallNotificationUri;
    /* access modifiers changed from: private */
    public float mInCallNotificationVolume;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Context context2 = context;
            Intent intent2 = intent;
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_ON")) {
                boolean unused = NotificationManagerService.this.mScreenOn = true;
                NotificationManagerService.this.mGameDndStatus = NotificationManagerService.this.isGameRunningForeground();
                Flog.i(400, "mIntentReceiver-ACTION_SCREEN_ON, mGameDndStatus = " + NotificationManagerService.this.mGameDndStatus);
                NotificationManagerService.this.updateNotificationPulse();
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                boolean unused2 = NotificationManagerService.this.mScreenOn = false;
                NotificationManagerService.this.mGameDndStatus = false;
                Flog.i(400, "mIntentReceiver-ACTION_SCREEN_OFF");
                NotificationManagerService.this.updateNotificationPulse();
            } else if (action.equals("android.intent.action.PHONE_STATE")) {
                NotificationManagerService.this.mInCall = TelephonyManager.EXTRA_STATE_OFFHOOK.equals(intent2.getStringExtra(AudioService.CONNECT_INTENT_KEY_STATE));
                Flog.i(400, "mIntentReceiver-ACTION_PHONE_STATE_CHANGED");
                NotificationManagerService.this.updateNotificationPulse();
            } else if (action.equals("android.intent.action.USER_STOPPED")) {
                Flog.i(400, "mIntentReceiver-ACTION_USER_STOPPED");
                int userHandle = intent2.getIntExtra("android.intent.extra.user_handle", -1);
                if (userHandle >= 0) {
                    NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, null, null, 0, 0, true, userHandle, 6, null);
                }
            } else if (action.equals("android.intent.action.MANAGED_PROFILE_UNAVAILABLE")) {
                int userHandle2 = intent2.getIntExtra("android.intent.extra.user_handle", -1);
                if (userHandle2 >= 0) {
                    NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, null, null, 0, 0, true, userHandle2, 15, null);
                }
            } else if (action.equals("android.intent.action.USER_PRESENT")) {
                if (NotificationManagerService.this.mScreenOn) {
                    NotificationManagerService.this.mNotificationLight.turnOff();
                    StatusBarManagerInternal statusBarManagerInternal = NotificationManagerService.this.mStatusBar;
                    NotificationManagerService.this.mGameDndStatus = NotificationManagerService.this.isGameRunningForeground();
                    Flog.i(1100, "turn off notificationLight due to Receiver-ACTION_USER_PRESENT, mGameDndStatus = " + NotificationManagerService.this.mGameDndStatus);
                    NotificationManagerService.this.updateLight(false, 0, 0);
                }
            } else if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                if (!NotificationManagerService.this.mScreenOn || NotificationManagerService.this.mGameDndStatus) {
                    NotificationManagerService.this.mNotificationLight.turnOff();
                    NotificationManagerService.this.updateNotificationPulse();
                    Flog.i(1100, "turn off notificationLight due to Receiver-ACTION_POWER_DISCONNECTED,mScreenOn= " + NotificationManagerService.this.mScreenOn + ",mGameDndStatus= " + NotificationManagerService.this.mGameDndStatus);
                    NotificationManagerService.this.updateLight(false, 0, 0);
                }
            } else if (action.equals("android.intent.action.USER_SWITCHED")) {
                int user = intent2.getIntExtra("android.intent.extra.user_handle", -10000);
                Flog.i(400, "mIntentReceiver-ACTION_USER_SWITCHED");
                NotificationManagerService.this.mSettingsObserver.update(null);
                NotificationManagerService.this.mUserProfiles.updateCache(context2);
                NotificationManagerService.this.mConditionProviders.onUserSwitched(user);
                NotificationManagerService.this.mListeners.onUserSwitched(user);
                NotificationManagerService.this.mAssistants.onUserSwitched(user);
                NotificationManagerService.this.mZenModeHelper.onUserSwitched(user);
                NotificationManagerService.this.handleUserSwitchEvents(user);
                NotificationManagerService.this.stopPlaySound();
            } else if (action.equals("android.intent.action.USER_ADDED")) {
                int userId = intent2.getIntExtra("android.intent.extra.user_handle", -10000);
                if (userId != -10000) {
                    NotificationManagerService.this.mUserProfiles.updateCache(context2);
                    if (!NotificationManagerService.this.mUserProfiles.isManagedProfile(userId)) {
                        NotificationManagerService.this.readDefaultApprovedServices(userId);
                    }
                }
                Flog.i(400, "mIntentReceiver-ACTION_USER_ADDED");
            } else if (action.equals("android.intent.action.USER_REMOVED")) {
                int user2 = intent2.getIntExtra("android.intent.extra.user_handle", -10000);
                NotificationManagerService.this.mUserProfiles.updateCache(context2);
                NotificationManagerService.this.mZenModeHelper.onUserRemoved(user2);
                NotificationManagerService.this.mRankingHelper.onUserRemoved(user2);
                NotificationManagerService.this.mListeners.onUserRemoved(user2);
                NotificationManagerService.this.mConditionProviders.onUserRemoved(user2);
                NotificationManagerService.this.mAssistants.onUserRemoved(user2);
                NotificationManagerService.this.savePolicyFile();
            } else if (action.equals("android.intent.action.USER_UNLOCKED")) {
                int user3 = intent2.getIntExtra("android.intent.extra.user_handle", -10000);
                NotificationManagerService.this.mConditionProviders.onUserUnlocked(user3);
                NotificationManagerService.this.mListeners.onUserUnlocked(user3);
                NotificationManagerService.this.mAssistants.onUserUnlocked(user3);
                NotificationManagerService.this.mZenModeHelper.onUserUnlocked(user3);
            }
        }
    };
    private final NotificationManagerInternal mInternalService = new NotificationManagerInternal() {
        public NotificationChannel getNotificationChannel(String pkg, int uid, String channelId) {
            return NotificationManagerService.this.mRankingHelper.getNotificationChannel(pkg, uid, channelId, false);
        }

        public void enqueueNotification(String pkg, String opPkg, int callingUid, int callingPid, String tag, int id, Notification notification, int userId) {
            NotificationManagerService.this.enqueueNotificationInternal(pkg, opPkg, callingUid, callingPid, tag, id, notification, userId);
        }

        public void removeForegroundServiceFlagFromNotification(final String pkg, final int notificationId, final int userId) {
            NotificationManagerService.this.checkCallerIsSystem();
            NotificationManagerService.this.mHandler.post(new Runnable() {
                public void run() {
                    synchronized (NotificationManagerService.this.mNotificationLock) {
                        AnonymousClass12.this.removeForegroundServiceFlagByListLocked(NotificationManagerService.this.mEnqueuedNotifications, pkg, notificationId, userId);
                        AnonymousClass12.this.removeForegroundServiceFlagByListLocked(NotificationManagerService.this.mNotificationList, pkg, notificationId, userId);
                    }
                }
            });
        }

        /* access modifiers changed from: private */
        @GuardedBy("mNotificationLock")
        public void removeForegroundServiceFlagByListLocked(ArrayList<NotificationRecord> notificationList, String pkg, int notificationId, int userId) {
            NotificationRecord r = NotificationManagerService.this.findNotificationByListLocked(notificationList, pkg, null, notificationId, userId);
            if (r != null) {
                r.sbn.getNotification().flags = r.mOriginalFlags & -65;
                NotificationManagerService.this.mRankingHelper.sort(NotificationManagerService.this.mNotificationList);
                NotificationManagerService.this.mListeners.notifyPostedLocked(r, r);
            }
        }
    };
    /* access modifiers changed from: private */
    public int mInterruptionFilter = 0;
    private boolean mIsTelevision;
    private long mLastOverRateLogTime;
    ArrayList<String> mLights = new ArrayList<>();
    /* access modifiers changed from: private */
    public int mListenerHints;
    /* access modifiers changed from: private */
    public NotificationListeners mListeners;
    private final SparseArray<ArraySet<ManagedServices.ManagedServiceInfo>> mListenersDisablingEffects = new SparseArray<>();
    protected final BroadcastReceiver mLocaleChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                SystemNotificationChannels.createAll(context);
                NotificationManagerService.this.mZenModeHelper.updateDefaultZenRules();
                NotificationManagerService.this.mRankingHelper.onLocaleChanged(context, ActivityManager.getCurrentUser());
            }
        }
    };
    /* access modifiers changed from: private */
    public float mMaxPackageEnqueueRate = 5.0f;
    /* access modifiers changed from: private */
    public MetricsLogger mMetricsLogger;
    @VisibleForTesting
    final NotificationDelegate mNotificationDelegate = new NotificationDelegate() {
        public void onSetDisabled(int status) {
            long identity;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                boolean unused = NotificationManagerService.this.mDisableNotificationEffects = (262144 & status) != 0;
                if (NotificationManagerService.this.disableNotificationEffects(null) != null) {
                    long identity2 = Binder.clearCallingIdentity();
                    try {
                        IRingtonePlayer player = NotificationManagerService.this.mAudioManager.getRingtonePlayer();
                        if (player != null) {
                            player.stopAsync();
                        }
                        Binder.restoreCallingIdentity(identity2);
                    } catch (RemoteException e) {
                        Binder.restoreCallingIdentity(identity2);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(identity2);
                        throw th;
                    }
                    identity = Binder.clearCallingIdentity();
                    NotificationManagerService.this.mVibrator.cancel();
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void onClearAll(int callingUid, int callingPid, int userId) {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationManagerService.this.cancelAllLocked(callingUid, callingPid, userId, 3, null, true);
            }
        }

        public void onNotificationClick(int callingUid, int callingPid, String key, NotificationVisibility nv) {
            String str = key;
            NotificationVisibility notificationVisibility = nv;
            NotificationManagerService.this.exitIdle();
            synchronized (NotificationManagerService.this.mNotificationLock) {
                Flog.i(400, "onNotificationClick called");
                NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(str);
                if (r == null) {
                    Log.w(NotificationManagerService.TAG, "No notification with key: " + str);
                    return;
                }
                long now = System.currentTimeMillis();
                MetricsLogger.action(r.getLogMaker(now).setCategory(128).setType(4).addTaggedData(798, Integer.valueOf(notificationVisibility.rank)).addTaggedData(1395, Integer.valueOf(notificationVisibility.count)));
                EventLogTags.writeNotificationClicked(str, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now), notificationVisibility.rank, notificationVisibility.count);
                StatusBarNotification sbn = r.sbn;
                NotificationManagerService.this.cancelNotification(callingUid, callingPid, sbn.getPackageName(), sbn.getTag(), sbn.getId(), 16, 64, false, r.getUserId(), 1, notificationVisibility.rank, notificationVisibility.count, null);
                nv.recycle();
                NotificationManagerService.this.reportUserInteraction(r);
            }
        }

        public void onNotificationActionClick(int callingUid, int callingPid, String key, int actionIndex, NotificationVisibility nv) {
            String str = key;
            NotificationVisibility notificationVisibility = nv;
            NotificationManagerService.this.exitIdle();
            synchronized (NotificationManagerService.this.mNotificationLock) {
                try {
                    NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(str);
                    if (r == null) {
                        Log.w(NotificationManagerService.TAG, "No notification with key: " + str);
                        return;
                    }
                    long now = System.currentTimeMillis();
                    int i = actionIndex;
                    MetricsLogger.action(r.getLogMaker(now).setCategory(NetworkConstants.ICMPV6_ECHO_REPLY_TYPE).setType(4).setSubtype(i).addTaggedData(798, Integer.valueOf(notificationVisibility.rank)).addTaggedData(1395, Integer.valueOf(notificationVisibility.count)));
                    EventLogTags.writeNotificationActionClicked(str, i, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now), notificationVisibility.rank, notificationVisibility.count);
                    nv.recycle();
                    NotificationManagerService.this.reportUserInteraction(r);
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0020, code lost:
            r1.this$0.cancelNotification(r21, r22, r23, r24, r25, 0, 66, true, r26, 2, r2.rank, r2.count, null);
            r29.recycle();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0044, code lost:
            return;
         */
        public void onNotificationClear(int callingUid, int callingPid, String pkg, String tag, int id, int userId, String key, int dismissalSurface, NotificationVisibility nv) {
            NotificationVisibility notificationVisibility = nv;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                try {
                    try {
                        NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(key);
                        if (r != null) {
                            try {
                                r.recordDismissalSurface(dismissalSurface);
                            } catch (Throwable th) {
                                th = th;
                                throw th;
                            }
                        } else {
                            int i = dismissalSurface;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        int i2 = dismissalSurface;
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    String str = key;
                    int i22 = dismissalSurface;
                    throw th;
                }
            }
        }

        public void onPanelRevealed(boolean clearEffects, int items) {
            MetricsLogger.visible(NotificationManagerService.this.getContext(), 127);
            MetricsLogger.histogram(NotificationManagerService.this.getContext(), "note_load", items);
            EventLogTags.writeNotificationPanelRevealed(items);
            if (clearEffects) {
                clearEffects();
            }
        }

        public void onPanelHidden() {
            MetricsLogger.hidden(NotificationManagerService.this.getContext(), 127);
            EventLogTags.writeNotificationPanelHidden();
        }

        public void clearEffects() {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                if (NotificationManagerService.DBG) {
                    Slog.d(NotificationManagerService.TAG, "clearEffects");
                }
                NotificationManagerService.this.clearSoundLocked();
                NotificationManagerService.this.clearVibrateLocked();
                NotificationManagerService.this.clearLightsLocked();
            }
        }

        public void onNotificationError(int callingUid, int callingPid, String pkg, String tag, int id, int uid, int initialPid, String message, int userId) {
            NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 0, false, userId, 4, null);
        }

        public void onNotificationVisibilityChanged(NotificationVisibility[] newlyVisibleKeys, NotificationVisibility[] noLongerVisibleKeys) {
            Flog.i(400, "onNotificationVisibilityChanged called");
            synchronized (NotificationManagerService.this.mNotificationLock) {
                for (NotificationVisibility nv : newlyVisibleKeys) {
                    NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(nv.key);
                    if (r != null) {
                        if (!r.isSeen()) {
                            if (NotificationManagerService.DBG) {
                                Slog.d(NotificationManagerService.TAG, "Marking notification as visible " + nv.key);
                            }
                            NotificationManagerService.this.reportSeen(r);
                            if (r.getNumSmartRepliesAdded() > 0 && !r.hasSeenSmartReplies()) {
                                r.setSeenSmartReplies(true);
                                NotificationManagerService.this.mMetricsLogger.write(r.getLogMaker().setCategory(1382).addTaggedData(1384, Integer.valueOf(r.getNumSmartRepliesAdded())));
                            }
                        }
                        r.setVisibility(true, nv.rank, nv.count);
                        NotificationManagerService.this.maybeRecordInterruptionLocked(r);
                        nv.recycle();
                    }
                }
                for (NotificationVisibility nv2 : noLongerVisibleKeys) {
                    NotificationRecord r2 = NotificationManagerService.this.mNotificationsByKey.get(nv2.key);
                    if (r2 != null) {
                        r2.setVisibility(false, nv2.rank, nv2.count);
                        nv2.recycle();
                    }
                }
            }
        }

        public void onNotificationExpansionChanged(String key, boolean userAction, boolean expanded) {
            int i;
            Flog.i(400, "onNotificationExpansionChanged called");
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r != null) {
                    r.stats.onExpansionChanged(userAction, expanded);
                    long now = System.currentTimeMillis();
                    if (userAction) {
                        LogMaker category = r.getLogMaker(now).setCategory(128);
                        if (expanded) {
                            i = 3;
                        } else {
                            i = 14;
                        }
                        MetricsLogger.action(category.setType(i));
                    }
                    if (expanded && userAction) {
                        r.recordExpanded();
                    }
                    EventLogTags.writeNotificationExpansion(key, userAction ? 1 : 0, expanded ? 1 : 0, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now));
                }
            }
        }

        public void onNotificationDirectReplied(String key) {
            NotificationManagerService.this.exitIdle();
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r != null) {
                    r.recordDirectReplied();
                    NotificationManagerService.this.reportUserInteraction(r);
                }
            }
        }

        public void onNotificationSmartRepliesAdded(String key, int replyCount) {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r != null) {
                    r.setNumSmartRepliesAdded(replyCount);
                }
            }
        }

        public void onNotificationSmartReplySent(String key, int replyIndex) {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r != null) {
                    NotificationManagerService.this.mMetricsLogger.write(r.getLogMaker().setCategory(1383).setSubtype(replyIndex));
                    NotificationManagerService.this.reportUserInteraction(r);
                }
            }
        }

        public void onNotificationSettingsViewed(String key) {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r != null) {
                    r.recordViewedSettings();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Light mNotificationLight;
    @GuardedBy("mNotificationLock")
    final ArrayList<NotificationRecord> mNotificationList = new ArrayList<>();
    final Object mNotificationLock = new Object();
    /* access modifiers changed from: private */
    public boolean mNotificationPulseEnabled;
    /* access modifiers changed from: private */
    public HwSysResource mNotificationResource;
    private final BroadcastReceiver mNotificationTimeoutReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0028, code lost:
            if (r0 == null) goto L_0x0063;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x002a, code lost:
            r1.this$0.cancelNotification(r0.sbn.getUid(), r0.sbn.getInitialPid(), r0.sbn.getPackageName(), r0.sbn.getTag(), r0.sbn.getId(), 0, 64, true, r0.getUserId(), 19, null);
         */
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (NotificationManagerService.ACTION_NOTIFICATION_TIMEOUT.equals(action)) {
                    synchronized (NotificationManagerService.this.mNotificationLock) {
                        try {
                            try {
                                NotificationRecord record = NotificationManagerService.this.findNotificationByKeyLocked(intent.getStringExtra(NotificationManagerService.EXTRA_KEY));
                            } catch (Throwable th) {
                                th = th;
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            Intent intent2 = intent;
                            throw th;
                        }
                    }
                } else {
                    Intent intent3 = intent;
                }
            }
        }
    };
    @GuardedBy("mNotificationLock")
    final ArrayMap<String, NotificationRecord> mNotificationsByKey = new ArrayMap<>();
    private final BroadcastReceiver mPackageIntentReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Removed duplicated region for block: B:73:0x016b  */
        public void onReceive(Context context, Intent intent) {
            boolean z;
            int[] uidList;
            String[] pkgList;
            int length;
            int i;
            int changeUserId;
            boolean removingPackage;
            int i2;
            boolean z2;
            int i3;
            Intent intent2 = intent;
            String action = intent.getAction();
            if (action != null) {
                boolean queryRestart = false;
                boolean queryRemove = false;
                boolean packageChanged = false;
                boolean cancelNotifications = true;
                boolean hideNotifications = false;
                boolean unhideNotifications = false;
                if (!action.equals("android.intent.action.PACKAGE_ADDED")) {
                    boolean equals = action.equals("android.intent.action.PACKAGE_REMOVED");
                    queryRemove = equals;
                    if (!equals && !action.equals("android.intent.action.PACKAGE_RESTARTED")) {
                        boolean equals2 = action.equals("android.intent.action.PACKAGE_CHANGED");
                        packageChanged = equals2;
                        if (!equals2) {
                            boolean equals3 = action.equals("android.intent.action.QUERY_PACKAGE_RESTART");
                            queryRestart = equals3;
                            if (!equals3 && !action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE") && !action.equals("android.intent.action.PACKAGES_SUSPENDED") && !action.equals("android.intent.action.PACKAGES_UNSUSPENDED")) {
                                String str = action;
                                boolean z3 = packageChanged;
                            }
                        }
                    }
                }
                boolean packageChanged2 = packageChanged;
                boolean queryRestart2 = queryRestart;
                int changeUserId2 = intent2.getIntExtra("android.intent.extra.user_handle", -1);
                Flog.i(400, "mIntentReceiver-Package changed");
                int[] uidList2 = null;
                boolean removingPackage2 = queryRemove && !intent2.getBooleanExtra("android.intent.extra.REPLACING", false);
                StringBuilder sb = new StringBuilder();
                sb.append("action=");
                sb.append(action);
                sb.append(" removing=");
                boolean removingPackage3 = removingPackage2;
                sb.append(removingPackage3);
                Flog.i(400, sb.toString());
                if (action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")) {
                    pkgList = intent2.getStringArrayExtra("android.intent.extra.changed_package_list");
                    uidList2 = intent2.getIntArrayExtra("android.intent.extra.changed_uid_list");
                } else if (action.equals("android.intent.action.PACKAGES_SUSPENDED")) {
                    pkgList = intent2.getStringArrayExtra("android.intent.extra.changed_package_list");
                    cancelNotifications = false;
                    hideNotifications = true;
                } else if (action.equals("android.intent.action.PACKAGES_UNSUSPENDED")) {
                    pkgList = intent2.getStringArrayExtra("android.intent.extra.changed_package_list");
                    cancelNotifications = false;
                    unhideNotifications = true;
                } else if (queryRestart2) {
                    pkgList = intent2.getStringArrayExtra("android.intent.extra.PACKAGES");
                    uidList2 = new int[]{intent2.getIntExtra("android.intent.extra.UID", -1)};
                } else {
                    Uri uri = intent.getData();
                    if (uri != null) {
                        String pkgName = uri.getSchemeSpecificPart();
                        if (pkgName != null) {
                            if (queryRemove || packageChanged2) {
                                int uid = intent2.getIntExtra("android.intent.extra.UID", -1);
                                if (uid == -1 || NotificationManagerService.this.mNotificationResource == null) {
                                } else {
                                    String str2 = action;
                                    NotificationManagerService.this.mNotificationResource.clear(uid, pkgName, -1);
                                }
                            } else {
                                String str3 = action;
                            }
                            if (packageChanged2) {
                                try {
                                    int enabled = NotificationManagerService.this.mPackageManager.getApplicationEnabledSetting(pkgName, changeUserId2 != -1 ? changeUserId2 : 0);
                                    if (enabled == 1 || enabled == 0) {
                                        cancelNotifications = false;
                                    }
                                } catch (IllegalArgumentException e) {
                                    Flog.i(400, "Exception trying to look up app enabled setting", e);
                                } catch (RemoteException e2) {
                                }
                            }
                            z = false;
                            pkgList = new String[]{pkgName};
                            uidList = new int[]{intent2.getIntExtra("android.intent.extra.UID", -1)};
                            if (pkgList != null && pkgList.length > 0) {
                                length = pkgList.length;
                                i = z;
                                while (i < length) {
                                    String pkgName2 = pkgList[i];
                                    if (cancelNotifications) {
                                        i3 = i;
                                        removingPackage = removingPackage3;
                                        z2 = z;
                                        i2 = length;
                                        changeUserId = changeUserId2;
                                        NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkgName2, null, 0, 0, !queryRestart2 ? true : z, changeUserId, 5, null);
                                    } else {
                                        i3 = i;
                                        removingPackage = removingPackage3;
                                        i2 = length;
                                        changeUserId = changeUserId2;
                                        z2 = z;
                                        if (hideNotifications) {
                                            NotificationManagerService.this.hideNotificationsForPackages(pkgList);
                                        } else if (unhideNotifications) {
                                            NotificationManagerService.this.unhideNotificationsForPackages(pkgList);
                                        }
                                    }
                                    i = i3 + 1;
                                    z = z2;
                                    length = i2;
                                    removingPackage3 = removingPackage;
                                    changeUserId2 = changeUserId;
                                }
                            }
                            boolean removingPackage4 = removingPackage3;
                            NotificationManagerService.this.mListeners.onPackagesChanged(removingPackage4, pkgList, uidList);
                            NotificationManagerService.this.mAssistants.onPackagesChanged(removingPackage4, pkgList, uidList);
                            NotificationManagerService.this.mConditionProviders.onPackagesChanged(removingPackage4, pkgList, uidList);
                            NotificationManagerService.this.mRankingHelper.onPackagesChanged(removingPackage4, changeUserId2, pkgList, uidList);
                            NotificationManagerService.this.savePolicyFile();
                            boolean z4 = queryRestart2;
                        }
                        return;
                    }
                    return;
                }
                uidList = uidList2;
                z = false;
                length = pkgList.length;
                i = z;
                while (i < length) {
                }
                boolean removingPackage42 = removingPackage3;
                NotificationManagerService.this.mListeners.onPackagesChanged(removingPackage42, pkgList, uidList);
                NotificationManagerService.this.mAssistants.onPackagesChanged(removingPackage42, pkgList, uidList);
                NotificationManagerService.this.mConditionProviders.onPackagesChanged(removingPackage42, pkgList, uidList);
                NotificationManagerService.this.mRankingHelper.onPackagesChanged(removingPackage42, changeUserId2, pkgList, uidList);
                NotificationManagerService.this.savePolicyFile();
                boolean z42 = queryRestart2;
            }
        }
    };
    /* access modifiers changed from: private */
    public IPackageManager mPackageManager;
    private PackageManager mPackageManagerClient;
    /* access modifiers changed from: private */
    public AtomicFile mPolicyFile;
    /* access modifiers changed from: private */
    public PowerSaverObserver mPowerSaverObserver;
    /* access modifiers changed from: private */
    public RankingHandler mRankingHandler;
    /* access modifiers changed from: private */
    public RankingHelper mRankingHelper;
    private final HandlerThread mRankingThread = new HandlerThread("ranker", 10);
    final ArrayMap<Integer, ArrayList<NotifyingApp>> mRecentApps = new ArrayMap<>();
    private final BroadcastReceiver mRestoreReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.os.action.SETTING_RESTORED".equals(intent.getAction())) {
                try {
                    String element = intent.getStringExtra("setting_name");
                    String newValue = intent.getStringExtra("new_value");
                    int restoredFromSdkInt = intent.getIntExtra("restored_from_sdk_int", 0);
                    NotificationManagerService.this.mListeners.onSettingRestored(element, newValue, restoredFromSdkInt, getSendingUserId());
                    NotificationManagerService.this.mConditionProviders.onSettingRestored(element, newValue, restoredFromSdkInt, getSendingUserId());
                } catch (Exception e) {
                    Slog.wtf(NotificationManagerService.TAG, "Cannot restore managed services from settings", e);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mScreenOn = true;
    private final IBinder mService = new INotificationManager.Stub() {
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (1599293262 == code) {
                try {
                    int event = data.readInt();
                    Flog.i(400, "NotificationManagerService.onTransact: got ST_GET_NOTIFICATIONS event " + event);
                    if (event == 1) {
                        NotificationManagerService.this.handleGetNotifications(data, reply);
                        return true;
                    }
                } catch (Exception e) {
                    Flog.i(400, "NotificationManagerService.onTransact: catch exception " + e.toString());
                    return false;
                }
            }
            return NotificationManagerService.super.onTransact(code, data, reply, flags);
        }

        public void enqueueToast(String pkg, ITransientNotification callback, int duration) {
            enqueueToastEx(pkg, callback, duration, 0);
        }

        /* JADX WARNING: type inference failed for: r17v0 */
        /* JADX WARNING: type inference failed for: r2v19, types: [com.android.server.notification.NotificationManagerService$ToastRecordEx] */
        /* JADX WARNING: type inference failed for: r2v20, types: [com.android.server.notification.NotificationManagerService$ToastRecord] */
        /* JADX WARNING: Multi-variable type inference failed */
        public void enqueueToastEx(String pkg, ITransientNotification callback, int duration, int displayId) {
            ArrayList<ToastRecord> arrayList;
            long callingId;
            int index;
            int callingPid;
            long callingId2;
            ? record;
            ToastRecord toastRecord;
            String str;
            String str2 = pkg;
            ITransientNotification iTransientNotification = callback;
            int i = duration;
            int i2 = displayId;
            IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
            if (manager != null) {
                manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.NOTIFICATIONMANAGER_ENQUEUETOAST);
            }
            Flog.i(400, "enqueueToast pkg=" + str2 + " callback=" + iTransientNotification + " duration=" + i);
            if (str2 == null || iTransientNotification == null) {
                Slog.e(NotificationManagerService.TAG, "Not doing toast. pkg=" + str2 + " callback=" + iTransientNotification);
            } else if (!NotificationManagerService.mIsChina || NotificationManagerService.this.isAllowToShow(str2, ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).getLastResumedActivity())) {
                boolean isSystemToast = NotificationManagerService.this.isCallerSystemOrPhone() || PackageManagerService.PLATFORM_PACKAGE_NAME.equals(str2);
                boolean isPackageSuspended = NotificationManagerService.this.isPackageSuspendedForUser(str2, Binder.getCallingUid());
                if (isSystemToast || (areNotificationsEnabledForPackage(str2, Binder.getCallingUid()) && !isPackageSuspended)) {
                    ArrayList<ToastRecord> arrayList2 = NotificationManagerService.this.mToastQueue;
                    synchronized (arrayList2) {
                        try {
                            int callingPid2 = Binder.getCallingPid();
                            long callingId3 = Binder.clearCallingIdentity();
                            if (!isSystemToast) {
                                try {
                                    index = NotificationManagerService.this.indexOfToastPackageLocked(str2);
                                } catch (Throwable th) {
                                    th = th;
                                    callingId = callingId3;
                                    int i3 = callingPid2;
                                    ArrayList<ToastRecord> arrayList3 = arrayList2;
                                    Binder.restoreCallingIdentity(callingId);
                                    throw th;
                                }
                            } else {
                                try {
                                    index = NotificationManagerService.this.indexOfToastLocked(str2, iTransientNotification);
                                } catch (Throwable th2) {
                                    th = th2;
                                    callingId = callingId3;
                                    int i4 = callingPid2;
                                    ArrayList<ToastRecord> arrayList4 = arrayList2;
                                    Binder.restoreCallingIdentity(callingId);
                                    throw th;
                                }
                            }
                            int index2 = index;
                            if (index2 >= 0) {
                                ToastRecord record2 = NotificationManagerService.this.mToastQueue.get(index2);
                                record2.update(i);
                                try {
                                    record2.callback.hide();
                                } catch (RemoteException e) {
                                }
                                record2.update(iTransientNotification);
                                callingId2 = callingId3;
                                callingPid = callingPid2;
                                arrayList = arrayList2;
                            } else {
                                Binder token = new Binder();
                                NotificationManagerService.this.mWindowManagerInternal.addWindowToken(token, 2005, i2);
                                if (i2 == 0) {
                                    try {
                                        toastRecord = toastRecord;
                                        int i5 = index2;
                                        callingId2 = callingId3;
                                        callingPid = callingPid2;
                                    } catch (Throwable th3) {
                                        th = th3;
                                        callingId = callingId3;
                                        int i6 = callingPid2;
                                        ArrayList<ToastRecord> arrayList5 = arrayList2;
                                        Binder.restoreCallingIdentity(callingId);
                                        throw th;
                                    }
                                    try {
                                        toastRecord = new ToastRecord(callingPid2, str2, iTransientNotification, i, token);
                                        ToastRecord toastRecord2 = toastRecord;
                                        arrayList = arrayList2;
                                        record = toastRecord;
                                    } catch (Throwable th4) {
                                        th = th4;
                                        ArrayList<ToastRecord> arrayList6 = arrayList2;
                                        callingId = callingId2;
                                        int i7 = callingPid;
                                        Binder.restoreCallingIdentity(callingId);
                                        throw th;
                                    }
                                } else {
                                    int i8 = index2;
                                    callingId2 = callingId3;
                                    callingPid = callingPid2;
                                    try {
                                        r2 = r2;
                                        arrayList = arrayList2;
                                        try {
                                            ToastRecordEx toastRecordEx = new ToastRecordEx(callingPid, str2, iTransientNotification, i, token, i2);
                                            record = toastRecordEx;
                                        } catch (Throwable th5) {
                                            th = th5;
                                            callingId = callingId2;
                                            int i9 = callingPid;
                                            Binder.restoreCallingIdentity(callingId);
                                            throw th;
                                        }
                                    } catch (Throwable th6) {
                                        th = th6;
                                        ArrayList<ToastRecord> arrayList7 = arrayList2;
                                        callingId = callingId2;
                                        int i92 = callingPid;
                                        Binder.restoreCallingIdentity(callingId);
                                        throw th;
                                    }
                                }
                                NotificationManagerService.this.mToastQueue.add(record);
                                index2 = NotificationManagerService.this.mToastQueue.size() - 1;
                            }
                            try {
                                NotificationManagerService.this.keepProcessAliveIfNeededLocked(callingPid);
                                if (index2 == 0) {
                                    try {
                                        NotificationManagerService.this.showNextToastLocked();
                                    } catch (Throwable th7) {
                                        th = th7;
                                        callingId = callingId2;
                                    }
                                }
                                Binder.restoreCallingIdentity(callingId2);
                            } catch (Throwable th8) {
                                th = th8;
                                throw th;
                            }
                        } catch (Throwable th9) {
                            th = th9;
                            arrayList = arrayList2;
                            throw th;
                        }
                    }
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Suppressing toast from package ");
                    sb.append(str2);
                    if (isPackageSuspended) {
                        str = " due to package suspended by administrator.";
                    } else {
                        str = " by user request.";
                    }
                    sb.append(str);
                    Slog.e(NotificationManagerService.TAG, sb.toString());
                }
            }
        }

        public void cancelToast(String pkg, ITransientNotification callback) {
            Slog.i(NotificationManagerService.TAG, "cancelToast pkg=" + pkg + " callback=" + callback);
            if (pkg == null || callback == null) {
                Slog.e(NotificationManagerService.TAG, "Not cancelling notification. pkg=" + pkg + " callback=" + callback);
                return;
            }
            synchronized (NotificationManagerService.this.mToastQueue) {
                long callingId = Binder.clearCallingIdentity();
                try {
                    int index = NotificationManagerService.this.indexOfToastLocked(pkg, callback);
                    if (index >= 0) {
                        NotificationManagerService.this.cancelToastLocked(index);
                    } else {
                        Slog.w(NotificationManagerService.TAG, "Toast already cancelled. pkg=" + pkg + " callback=" + callback);
                    }
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }

        public void finishToken(String pkg, ITransientNotification callback) {
            synchronized (NotificationManagerService.this.mToastQueue) {
                long callingId = Binder.clearCallingIdentity();
                try {
                    int index = NotificationManagerService.this.indexOfToastLocked(pkg, callback);
                    if (index >= 0) {
                        NotificationManagerService.this.finishTokenLocked(NotificationManagerService.this.mToastQueue.get(index).token);
                    } else {
                        Slog.w(NotificationManagerService.TAG, "Toast already killed. pkg=" + pkg + " callback=" + callback);
                    }
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }

        public void enqueueNotificationWithTag(String pkg, String opPkg, String tag, int id, Notification notification, int userId) throws RemoteException {
            String str = pkg;
            Notification notification2 = notification;
            IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
            if (manager != null) {
                manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.NOTIFICATIONMANAGER_ENQUEUENOTIFICATIONWITHTAG);
            }
            NotificationManagerService.this.addHwExtraForNotification(notification2, str, Binder.getCallingPid());
            String str2 = opPkg;
            NotificationManagerService.this.enqueueNotificationInternal(NotificationManagerService.this.getNCTargetAppPkg(str2, str, notification2), str2, Binder.getCallingUid(), Binder.getCallingPid(), tag, id, notification2, userId);
        }

        public void cancelNotificationWithTag(String pkg, String tag, int id, int userId) {
            String str;
            int i;
            int mustNotHaveFlags;
            String str2 = pkg;
            IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
            if (manager != null) {
                manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.NOTIFICATIONMANAGER_CANCELNOTIFICATIONWITHTAG);
            }
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(str2);
            int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, false, "cancelNotificationWithTag", str2);
            if (NotificationManagerService.HWFLOW) {
                StringBuilder sb = new StringBuilder();
                sb.append("cancelNotificationWithTag pid ");
                sb.append(Binder.getCallingPid());
                sb.append(",uid = ");
                sb.append(Binder.getCallingUid());
                sb.append(",tag = ");
                str = tag;
                sb.append(str);
                sb.append(",pkg =");
                sb.append(str2);
                sb.append(",id =");
                i = id;
                sb.append(i);
                Flog.i(400, sb.toString());
            } else {
                str = tag;
                i = id;
            }
            if (NotificationManagerService.this.isCallingUidSystem()) {
                mustNotHaveFlags = 0;
            } else {
                mustNotHaveFlags = 1088;
            }
            NotificationManagerService.this.cancelNotification(Binder.getCallingUid(), Binder.getCallingPid(), str2, str, i, 0, mustNotHaveFlags, false, userId2, 8, null);
        }

        public void cancelAllNotifications(String pkg, int userId) {
            String str = pkg;
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(str);
            NotificationManagerService.this.cancelAllNotificationsInt(Binder.getCallingUid(), Binder.getCallingPid(), str, null, 0, 64, true, ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, false, "cancelAllNotifications", str), 9, null);
        }

        public void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled) {
            enforceSystemOrSystemUI("setNotificationsEnabledForPackage");
            NotificationManagerService.this.mRankingHelper.setEnabled(pkg, uid, enabled);
            if (!enabled) {
                NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkg, null, 0, 0, true, UserHandle.getUserId(uid), 7, null);
            }
            try {
                NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.APP_BLOCK_STATE_CHANGED").putExtra("android.app.extra.BLOCKED_STATE", !enabled).addFlags(268435456).setPackage(pkg), UserHandle.of(UserHandle.getUserId(uid)), null);
            } catch (SecurityException e) {
                Slog.w(NotificationManagerService.TAG, "Can't notify app about app block change", e);
            }
            NotificationManagerService.this.savePolicyFile();
        }

        public void setNotificationsEnabledWithImportanceLockForPackage(String pkg, int uid, boolean enabled) {
            setNotificationsEnabledForPackage(pkg, uid, enabled);
            NotificationManagerService.this.mRankingHelper.setAppImportanceLocked(pkg, uid);
        }

        public boolean areNotificationsEnabled(String pkg) {
            return areNotificationsEnabledForPackage(pkg, Binder.getCallingUid());
        }

        public boolean areNotificationsEnabledForPackage(String pkg, int uid) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            if (UserHandle.getCallingUserId() != UserHandle.getUserId(uid)) {
                Context context = NotificationManagerService.this.getContext();
                context.enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS", "canNotifyAsPackage for uid " + uid);
            }
            return NotificationManagerService.this.mRankingHelper.getImportance(pkg, uid) != 0;
        }

        public int getPackageImportance(String pkg) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            return NotificationManagerService.this.mRankingHelper.getImportance(pkg, Binder.getCallingUid());
        }

        public boolean canShowBadge(String pkg, int uid) {
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mRankingHelper.canShowBadge(pkg, uid);
        }

        public void setShowBadge(String pkg, int uid, boolean showBadge) {
            NotificationManagerService.this.checkCallerIsSystem();
            NotificationManagerService.this.mRankingHelper.setShowBadge(pkg, uid, showBadge);
            NotificationManagerService.this.savePolicyFile();
        }

        public void updateNotificationChannelGroupForPackage(String pkg, int uid, NotificationChannelGroup group) throws RemoteException {
            enforceSystemOrSystemUI("Caller not system or systemui");
            NotificationManagerService.this.createNotificationChannelGroup(pkg, uid, group, false, false);
            NotificationManagerService.this.savePolicyFile();
        }

        public void createNotificationChannelGroups(String pkg, ParceledListSlice channelGroupList) throws RemoteException {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            List<NotificationChannelGroup> groups = channelGroupList.getList();
            List<NotificationChannelGroup> groupsCache = new ArrayList<>(groups);
            int groupSize = groups.size();
            for (int i = 0; i < groupSize; i++) {
                NotificationManagerService.this.createNotificationChannelGroup(pkg, Binder.getCallingUid(), groupsCache.get(i), true, false);
            }
            NotificationManagerService.this.savePolicyFile();
        }

        private void createNotificationChannelsImpl(String pkg, int uid, ParceledListSlice channelsList) {
            List<NotificationChannel> channels = channelsList.getList();
            int channelsSize = channels.size();
            for (int i = 0; i < channelsSize; i++) {
                NotificationChannel channel = channels.get(i);
                Preconditions.checkNotNull(channel, "channel in list is null");
                NotificationManagerService.this.mRankingHelper.createNotificationChannel(pkg, uid, channel, true, NotificationManagerService.this.mConditionProviders.isPackageOrComponentAllowed(pkg, UserHandle.getUserId(uid)));
                NotificationManagerService.this.mListeners.notifyNotificationChannelChanged(pkg, UserHandle.getUserHandleForUid(uid), NotificationManagerService.this.mRankingHelper.getNotificationChannel(pkg, uid, channel.getId(), false), 1);
            }
            NotificationManagerService.this.savePolicyFile();
        }

        public void createNotificationChannels(String pkg, ParceledListSlice channelsList) throws RemoteException {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            createNotificationChannelsImpl(pkg, Binder.getCallingUid(), channelsList);
        }

        public void createNotificationChannelsForPackage(String pkg, int uid, ParceledListSlice channelsList) throws RemoteException {
            int callingUid = Binder.getCallingUid();
            String callingPkgName = null;
            PackageManagerInternal packageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
            if (packageManagerInternal != null) {
                callingPkgName = packageManagerInternal.getNameForUid(callingUid);
            }
            if (!NotificationManagerService.NOTIFICATION_CENTER_PKG.equals(callingPkgName)) {
                NotificationManagerService.this.checkCallerIsSystem();
            }
            createNotificationChannelsImpl(pkg, uid, channelsList);
        }

        public NotificationChannel getNotificationChannel(String pkg, String channelId) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            return NotificationManagerService.this.mRankingHelper.getNotificationChannel(pkg, Binder.getCallingUid(), channelId, false);
        }

        public NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, boolean includeDeleted) {
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mRankingHelper.getNotificationChannel(pkg, uid, channelId, includeDeleted);
        }

        public void deleteNotificationChannel(String pkg, String channelId) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            int callingUid = Binder.getCallingUid();
            if (!"miscellaneous".equals(channelId)) {
                NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkg, channelId, 0, 0, true, UserHandle.getUserId(callingUid), 17, null);
                NotificationManagerService.this.mRankingHelper.deleteNotificationChannel(pkg, callingUid, channelId);
                NotificationManagerService.this.mListeners.notifyNotificationChannelChanged(pkg, UserHandle.getUserHandleForUid(callingUid), NotificationManagerService.this.mRankingHelper.getNotificationChannel(pkg, callingUid, channelId, true), 3);
                NotificationManagerService.this.savePolicyFile();
                return;
            }
            throw new IllegalArgumentException("Cannot delete default channel");
        }

        public NotificationChannelGroup getNotificationChannelGroup(String pkg, String groupId) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            return NotificationManagerService.this.mRankingHelper.getNotificationChannelGroupWithChannels(pkg, Binder.getCallingUid(), groupId, false);
        }

        public ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroups(String pkg) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            return NotificationManagerService.this.mRankingHelper.getNotificationChannelGroups(pkg, Binder.getCallingUid(), false, false, true);
        }

        public void deleteNotificationChannelGroup(String pkg, String groupId) {
            String str = pkg;
            String str2 = groupId;
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(str);
            int callingUid = Binder.getCallingUid();
            NotificationChannelGroup groupToDelete = NotificationManagerService.this.mRankingHelper.getNotificationChannelGroup(str2, str, callingUid);
            if (groupToDelete != null) {
                List<NotificationChannel> deletedChannels = NotificationManagerService.this.mRankingHelper.deleteNotificationChannelGroup(str, callingUid, str2);
                int i = 0;
                while (true) {
                    int i2 = i;
                    if (i2 < deletedChannels.size()) {
                        NotificationChannel deletedChannel = deletedChannels.get(i2);
                        NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, str, deletedChannel.getId(), 0, 0, true, UserHandle.getUserId(Binder.getCallingUid()), 17, null);
                        NotificationManagerService.this.mListeners.notifyNotificationChannelChanged(str, UserHandle.getUserHandleForUid(callingUid), deletedChannel, 3);
                        i = i2 + 1;
                        deletedChannels = deletedChannels;
                    } else {
                        List<NotificationChannel> list = deletedChannels;
                        NotificationManagerService.this.mListeners.notifyNotificationChannelGroupChanged(str, UserHandle.getUserHandleForUid(callingUid), groupToDelete, 3);
                        NotificationManagerService.this.savePolicyFile();
                        return;
                    }
                }
            }
        }

        public void updateNotificationChannelForPackage(String pkg, int uid, NotificationChannel channel) {
            enforceSystemOrSystemUI("Caller not system or systemui");
            Preconditions.checkNotNull(channel);
            Slog.i(NotificationManagerService.TAG, "updateNotificationChannelForPackage, channel: " + channel);
            NotificationManagerService.this.updateNotificationChannelInt(pkg, uid, channel, false);
        }

        public ParceledListSlice<NotificationChannel> getNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted) {
            enforceSystemOrSystemUI("getNotificationChannelsForPackage");
            return NotificationManagerService.this.mRankingHelper.getNotificationChannels(pkg, uid, includeDeleted);
        }

        public int getNumNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted) {
            enforceSystemOrSystemUI("getNumNotificationChannelsForPackage");
            return NotificationManagerService.this.mRankingHelper.getNotificationChannels(pkg, uid, includeDeleted).getList().size();
        }

        public boolean onlyHasDefaultChannel(String pkg, int uid) {
            enforceSystemOrSystemUI("onlyHasDefaultChannel");
            return NotificationManagerService.this.mRankingHelper.onlyHasDefaultChannel(pkg, uid);
        }

        public int getDeletedChannelCount(String pkg, int uid) {
            enforceSystemOrSystemUI("getDeletedChannelCount");
            return NotificationManagerService.this.mRankingHelper.getDeletedChannelCount(pkg, uid);
        }

        public int getBlockedChannelCount(String pkg, int uid) {
            enforceSystemOrSystemUI("getBlockedChannelCount");
            return NotificationManagerService.this.mRankingHelper.getBlockedChannelCount(pkg, uid);
        }

        public ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroupsForPackage(String pkg, int uid, boolean includeDeleted) {
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mRankingHelper.getNotificationChannelGroups(pkg, uid, includeDeleted, true, false);
        }

        public NotificationChannelGroup getPopulatedNotificationChannelGroupForPackage(String pkg, int uid, String groupId, boolean includeDeleted) {
            enforceSystemOrSystemUI("getPopulatedNotificationChannelGroupForPackage");
            return NotificationManagerService.this.mRankingHelper.getNotificationChannelGroupWithChannels(pkg, uid, groupId, includeDeleted);
        }

        public NotificationChannelGroup getNotificationChannelGroupForPackage(String groupId, String pkg, int uid) {
            enforceSystemOrSystemUI("getNotificationChannelGroupForPackage");
            return NotificationManagerService.this.mRankingHelper.getNotificationChannelGroup(groupId, pkg, uid);
        }

        public ParceledListSlice<NotificationChannel> getNotificationChannels(String pkg) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            return NotificationManagerService.this.mRankingHelper.getNotificationChannels(pkg, Binder.getCallingUid(), false);
        }

        public ParceledListSlice<NotifyingApp> getRecentNotifyingAppsForUser(int userId) {
            ParceledListSlice<NotifyingApp> parceledListSlice;
            NotificationManagerService.this.checkCallerIsSystem();
            synchronized (NotificationManagerService.this.mNotificationLock) {
                parceledListSlice = new ParceledListSlice<>(new ArrayList<>((Collection) NotificationManagerService.this.mRecentApps.getOrDefault(Integer.valueOf(userId), new ArrayList())));
            }
            return parceledListSlice;
        }

        public int getBlockedAppCount(int userId) {
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mRankingHelper.getBlockedAppCount(userId);
        }

        public boolean areChannelsBypassingDnd() {
            return NotificationManagerService.this.mRankingHelper.areChannelsBypassingDnd();
        }

        public void clearData(String packageName, int uid, boolean fromApp) throws RemoteException {
            NotificationManagerService.this.checkCallerIsSystem();
            NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, packageName, null, 0, 0, true, UserHandle.getUserId(Binder.getCallingUid()), 17, null);
            String[] packages = {packageName};
            int[] uids = {uid};
            NotificationManagerService.this.mListeners.onPackagesChanged(true, packages, uids);
            NotificationManagerService.this.mAssistants.onPackagesChanged(true, packages, uids);
            NotificationManagerService.this.mConditionProviders.onPackagesChanged(true, packages, uids);
            if (!fromApp) {
                NotificationManagerService.this.mRankingHelper.onPackagesChanged(true, UserHandle.getCallingUserId(), packages, uids);
            }
            NotificationManagerService.this.savePolicyFile();
        }

        public StatusBarNotification[] getActiveNotifications(String callingPkg) {
            NotificationManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.ACCESS_NOTIFICATIONS", "NotificationManagerService.getActiveNotifications");
            StatusBarNotification[] tmp = null;
            if (NotificationManagerService.this.mAppOps.noteOpNoThrow(25, Binder.getCallingUid(), callingPkg) == 0) {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    tmp = new StatusBarNotification[NotificationManagerService.this.mNotificationList.size()];
                    int N = NotificationManagerService.this.mNotificationList.size();
                    for (int i = 0; i < N; i++) {
                        tmp[i] = NotificationManagerService.this.mNotificationList.get(i).sbn;
                    }
                }
            }
            return tmp;
        }

        public ParceledListSlice<StatusBarNotification> getAppActiveNotifications(String pkg, int incomingUserId) {
            ParceledListSlice<StatusBarNotification> parceledListSlice;
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            int userId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), incomingUserId, true, false, "getAppActiveNotifications", pkg);
            synchronized (NotificationManagerService.this.mNotificationLock) {
                ArrayMap<String, StatusBarNotification> map = new ArrayMap<>(NotificationManagerService.this.mNotificationList.size() + NotificationManagerService.this.mEnqueuedNotifications.size());
                int N = NotificationManagerService.this.mNotificationList.size();
                for (int i = 0; i < N; i++) {
                    StatusBarNotification sbn = sanitizeSbn(pkg, userId, NotificationManagerService.this.mNotificationList.get(i).sbn);
                    if (sbn != null) {
                        map.put(sbn.getKey(), sbn);
                    }
                }
                for (NotificationRecord snoozed : NotificationManagerService.this.mSnoozeHelper.getSnoozed(userId, pkg)) {
                    StatusBarNotification sbn2 = sanitizeSbn(pkg, userId, snoozed.sbn);
                    if (sbn2 != null) {
                        map.put(sbn2.getKey(), sbn2);
                    }
                }
                int M = NotificationManagerService.this.mEnqueuedNotifications.size();
                for (int i2 = 0; i2 < M; i2++) {
                    StatusBarNotification sbn3 = sanitizeSbn(pkg, userId, NotificationManagerService.this.mEnqueuedNotifications.get(i2).sbn);
                    if (sbn3 != null) {
                        map.put(sbn3.getKey(), sbn3);
                    }
                }
                ArrayList<StatusBarNotification> list = new ArrayList<>(map.size());
                list.addAll(map.values());
                parceledListSlice = new ParceledListSlice<>(list);
            }
            return parceledListSlice;
        }

        private StatusBarNotification sanitizeSbn(String pkg, int userId, StatusBarNotification sbn) {
            if (!sbn.getPackageName().equals(pkg)) {
                int i = userId;
            } else if (sbn.getUserId() == userId) {
                StatusBarNotification statusBarNotification = new StatusBarNotification(sbn.getPackageName(), sbn.getOpPkg(), sbn.getId(), sbn.getTag(), sbn.getUid(), sbn.getInitialPid(), sbn.getNotification().clone(), sbn.getUser(), sbn.getOverrideGroupKey(), sbn.getPostTime());
                return statusBarNotification;
            }
            return null;
        }

        public StatusBarNotification[] getHistoricalNotifications(String callingPkg, int count) {
            NotificationManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.ACCESS_NOTIFICATIONS", "NotificationManagerService.getHistoricalNotifications");
            StatusBarNotification[] tmp = null;
            if (NotificationManagerService.this.mAppOps.noteOpNoThrow(25, Binder.getCallingUid(), callingPkg) == 0) {
                synchronized (NotificationManagerService.this.mArchive) {
                    tmp = NotificationManagerService.this.mArchive.getArray(count);
                }
            }
            return tmp;
        }

        public void registerListener(INotificationListener listener, ComponentName component, int userid) {
            enforceSystemOrSystemUI("INotificationManager.registerListener");
            NotificationManagerService.this.mListeners.registerService(listener, component, userid);
        }

        public void unregisterListener(INotificationListener token, int userid) {
            NotificationManagerService.this.mListeners.unregisterService((IInterface) token, userid);
        }

        public void cancelNotificationsFromListener(INotificationListener token, String[] keys) {
            int N;
            int i;
            String[] strArr = keys;
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            long identity = Binder.clearCallingIdentity();
            try {
                Flog.i(400, "cancelNotificationsFromListener called,callingUid = " + callingUid + ",callingPid = " + callingPid);
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    ManagedServices.ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    if (strArr != null) {
                        int N2 = strArr.length;
                        int i2 = 0;
                        while (true) {
                            int i3 = i2;
                            if (i3 >= N2) {
                                break;
                            }
                            NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(strArr[i3]);
                            if (r == null) {
                                i = i3;
                                N = N2;
                            } else {
                                int userId = r.sbn.getUserId();
                                if (!(userId == info.userid || userId == -1)) {
                                    if (!NotificationManagerService.this.mUserProfiles.isCurrentProfile(userId)) {
                                        throw new SecurityException("Disallowed call from listener: " + info.service);
                                    }
                                }
                                String packageName = r.sbn.getPackageName();
                                String tag = r.sbn.getTag();
                                NotificationRecord notificationRecord = r;
                                String str = packageName;
                                i = i3;
                                String str2 = tag;
                                N = N2;
                                cancelNotificationFromListenerLocked(info, callingUid, callingPid, str, str2, r.sbn.getId(), userId);
                            }
                            i2 = i + 1;
                            INotificationListener iNotificationListener = token;
                            N2 = N;
                        }
                    } else {
                        NotificationManagerService.this.cancelAllLocked(callingUid, callingPid, info.userid, 11, info, info.supportsProfiles());
                    }
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        public void requestBindListener(ComponentName component) {
            ManagedServices manager;
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(component.getPackageName());
            long identity = Binder.clearCallingIdentity();
            try {
                if (NotificationManagerService.this.mAssistants.isComponentEnabledForCurrentProfiles(component)) {
                    manager = NotificationManagerService.this.mAssistants;
                } else {
                    manager = NotificationManagerService.this.mListeners;
                }
                manager.setComponentState(component, true);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void requestUnbindListener(INotificationListener token) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    ManagedServices.ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    info.getOwner().setComponentState(info.component, false);
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        public void setNotificationsShownFromListener(INotificationListener token, String[] keys) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    ManagedServices.ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    if (keys != null) {
                        for (String str : keys) {
                            NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(str);
                            if (r != null) {
                                int userId = r.sbn.getUserId();
                                if (!(userId == info.userid || userId == -1)) {
                                    if (!NotificationManagerService.this.mUserProfiles.isCurrentProfile(userId)) {
                                        throw new SecurityException("Disallowed call from listener: " + info.service);
                                    }
                                }
                                if (!r.isSeen()) {
                                    Flog.i(400, "Marking notification as seen " + keys[i]);
                                    NotificationManagerService.this.reportSeen(r);
                                    r.setSeen();
                                    NotificationManagerService.this.maybeRecordInterruptionLocked(r);
                                }
                            }
                        }
                    }
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        @GuardedBy("mNotificationLock")
        private void cancelNotificationFromListenerLocked(ManagedServices.ManagedServiceInfo info, int callingUid, int callingPid, String pkg, String tag, int id, int userId) {
            NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 66, true, userId, 10, info);
        }

        public void snoozeNotificationUntilContextFromListener(INotificationListener token, String key, String snoozeCriterionId) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.snoozeNotificationInt(key, -1, snoozeCriterionId, NotificationManagerService.this.mListeners.checkServiceTokenLocked(token));
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        public void snoozeNotificationUntilFromListener(INotificationListener token, String key, long duration) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.snoozeNotificationInt(key, duration, null, NotificationManagerService.this.mListeners.checkServiceTokenLocked(token));
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        public void unsnoozeNotificationFromAssistant(INotificationListener token, String key) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.unsnoozeNotificationInt(key, NotificationManagerService.this.mAssistants.checkServiceTokenLocked(token));
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0054, code lost:
            android.os.Binder.restoreCallingIdentity(r12);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0058, code lost:
            return;
         */
        public void cancelNotificationFromListener(INotificationListener token, String pkg, String tag, int id) {
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    try {
                        try {
                            ManagedServices.ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                            if (info.supportsProfiles()) {
                                Log.e(NotificationManagerService.TAG, "Ignoring deprecated cancelNotification(pkg, tag, id) from " + info.component + " use cancelNotification(key) instead.");
                            } else {
                                cancelNotificationFromListenerLocked(info, callingUid, callingPid, pkg, tag, id, info.userid);
                            }
                        } catch (Throwable th) {
                            th = th;
                            try {
                                throw th;
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        INotificationListener iNotificationListener = token;
                        throw th;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                INotificationListener iNotificationListener2 = token;
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        public ParceledListSlice<StatusBarNotification> getActiveNotificationsFromListener(INotificationListener token, String[] keys, int trim) {
            ParceledListSlice<StatusBarNotification> parceledListSlice;
            NotificationRecord r;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                ManagedServices.ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                boolean getKeys = keys != null;
                int N = getKeys ? keys.length : NotificationManagerService.this.mNotificationList.size();
                ArrayList<StatusBarNotification> list = new ArrayList<>(N);
                for (int i = 0; i < N; i++) {
                    if (getKeys) {
                        r = NotificationManagerService.this.mNotificationsByKey.get(keys[i]);
                    } else {
                        r = NotificationManagerService.this.mNotificationList.get(i);
                    }
                    if (r != null) {
                        StatusBarNotification sbn = r.sbn;
                        if (NotificationManagerService.this.isVisibleToListener(sbn, info)) {
                            list.add(trim == 0 ? sbn : sbn.cloneLight());
                        }
                    }
                }
                parceledListSlice = new ParceledListSlice<>(list);
            }
            return parceledListSlice;
        }

        public ParceledListSlice<StatusBarNotification> getSnoozedNotificationsFromListener(INotificationListener token, int trim) {
            ParceledListSlice<StatusBarNotification> parceledListSlice;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                ManagedServices.ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                List<NotificationRecord> snoozedRecords = NotificationManagerService.this.mSnoozeHelper.getSnoozed();
                int N = snoozedRecords.size();
                ArrayList<StatusBarNotification> list = new ArrayList<>(N);
                for (int i = 0; i < N; i++) {
                    NotificationRecord r = snoozedRecords.get(i);
                    if (r != null) {
                        StatusBarNotification sbn = r.sbn;
                        if (NotificationManagerService.this.isVisibleToListener(sbn, info)) {
                            list.add(trim == 0 ? sbn : sbn.cloneLight());
                        }
                    }
                }
                parceledListSlice = new ParceledListSlice<>(list);
            }
            return parceledListSlice;
        }

        public void requestHintsFromListener(INotificationListener token, int hints) {
            if (!"com.google.android.projection.gearhead".equals(NotificationManagerService.this.getPackageNameByPid(Binder.getCallingPid()))) {
                long identity = Binder.clearCallingIdentity();
                try {
                    synchronized (NotificationManagerService.this.mNotificationLock) {
                        ManagedServices.ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                        if ((hints & 7) != 0) {
                            NotificationManagerService.this.addDisabledHints(info, hints);
                        } else {
                            boolean unused = NotificationManagerService.this.removeDisabledHints(info, hints);
                        }
                        NotificationManagerService.this.updateListenerHintsLocked();
                        NotificationManagerService.this.updateEffectsSuppressorLocked();
                    }
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            }
        }

        public int getHintsFromListener(INotificationListener token) {
            int access$4700;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                access$4700 = NotificationManagerService.this.mListenerHints;
            }
            return access$4700;
        }

        public void requestInterruptionFilterFromListener(INotificationListener token, int interruptionFilter) throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.mZenModeHelper.requestFromListener(NotificationManagerService.this.mListeners.checkServiceTokenLocked(token).component, interruptionFilter);
                    NotificationManagerService.this.updateInterruptionFilterLocked();
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        public int getInterruptionFilterFromListener(INotificationListener token) throws RemoteException {
            int access$4800;
            synchronized (NotificationManagerService.this.mNotificationLight) {
                access$4800 = NotificationManagerService.this.mInterruptionFilter;
            }
            return access$4800;
        }

        public void setOnNotificationPostedTrimFromListener(INotificationListener token, int trim) throws RemoteException {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                ManagedServices.ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                if (info != null) {
                    NotificationManagerService.this.mListeners.setOnNotificationPostedTrimLocked(info, trim);
                }
            }
        }

        public int getZenMode() {
            return NotificationManagerService.this.mZenModeHelper.getZenMode();
        }

        public ZenModeConfig getZenModeConfig() {
            enforceSystemOrSystemUI("INotificationManager.getZenModeConfig");
            return NotificationManagerService.this.mZenModeHelper.getConfig();
        }

        public void setZenMode(int mode, Uri conditionId, String reason) throws RemoteException {
            enforceSystemOrSystemUI("INotificationManager.setZenMode");
            long identity = Binder.clearCallingIdentity();
            try {
                NotificationManagerService.this.mZenModeHelper.setManualZenMode(mode, conditionId, null, reason);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public List<ZenModeConfig.ZenRule> getZenRules() throws RemoteException {
            enforcePolicyAccess(Binder.getCallingUid(), "getAutomaticZenRules");
            return NotificationManagerService.this.mZenModeHelper.getZenRules();
        }

        public AutomaticZenRule getAutomaticZenRule(String id) throws RemoteException {
            Preconditions.checkNotNull(id, "Id is null");
            enforcePolicyAccess(Binder.getCallingUid(), "getAutomaticZenRule");
            return NotificationManagerService.this.mZenModeHelper.getAutomaticZenRule(id);
        }

        public String addAutomaticZenRule(AutomaticZenRule automaticZenRule) throws RemoteException {
            Preconditions.checkNotNull(automaticZenRule, "automaticZenRule is null");
            Preconditions.checkNotNull(automaticZenRule.getName(), "Name is null");
            Preconditions.checkNotNull(automaticZenRule.getOwner(), "Owner is null");
            Preconditions.checkNotNull(automaticZenRule.getConditionId(), "ConditionId is null");
            enforcePolicyAccess(Binder.getCallingUid(), "addAutomaticZenRule");
            return NotificationManagerService.this.mZenModeHelper.addAutomaticZenRule(automaticZenRule, "addAutomaticZenRule");
        }

        public boolean updateAutomaticZenRule(String id, AutomaticZenRule automaticZenRule) throws RemoteException {
            Preconditions.checkNotNull(automaticZenRule, "automaticZenRule is null");
            Preconditions.checkNotNull(automaticZenRule.getName(), "Name is null");
            Preconditions.checkNotNull(automaticZenRule.getOwner(), "Owner is null");
            Preconditions.checkNotNull(automaticZenRule.getConditionId(), "ConditionId is null");
            enforcePolicyAccess(Binder.getCallingUid(), "updateAutomaticZenRule");
            return NotificationManagerService.this.mZenModeHelper.updateAutomaticZenRule(id, automaticZenRule, "updateAutomaticZenRule");
        }

        public boolean removeAutomaticZenRule(String id) throws RemoteException {
            Preconditions.checkNotNull(id, "Id is null");
            enforcePolicyAccess(Binder.getCallingUid(), "removeAutomaticZenRule");
            return NotificationManagerService.this.mZenModeHelper.removeAutomaticZenRule(id, "removeAutomaticZenRule");
        }

        public boolean removeAutomaticZenRules(String packageName) throws RemoteException {
            Preconditions.checkNotNull(packageName, "Package name is null");
            enforceSystemOrSystemUI("removeAutomaticZenRules");
            return NotificationManagerService.this.mZenModeHelper.removeAutomaticZenRules(packageName, "removeAutomaticZenRules");
        }

        public int getRuleInstanceCount(ComponentName owner) throws RemoteException {
            Preconditions.checkNotNull(owner, "Owner is null");
            enforceSystemOrSystemUI("getRuleInstanceCount");
            return NotificationManagerService.this.mZenModeHelper.getCurrentInstanceCount(owner);
        }

        public void setInterruptionFilter(String pkg, int filter) throws RemoteException {
            enforcePolicyAccess(pkg, "setInterruptionFilter");
            int zen = NotificationManager.zenModeFromInterruptionFilter(filter, -1);
            if (zen != -1) {
                long identity = Binder.clearCallingIdentity();
                try {
                    NotificationManagerService.this.mZenModeHelper.setManualZenMode(zen, null, pkg, "setInterruptionFilter");
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            } else {
                throw new IllegalArgumentException("Invalid filter: " + filter);
            }
        }

        public void notifyConditions(final String pkg, IConditionProvider provider, final Condition[] conditions) {
            final ManagedServices.ManagedServiceInfo info = NotificationManagerService.this.mConditionProviders.checkServiceToken(provider);
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            NotificationManagerService.this.mHandler.post(new Runnable() {
                public void run() {
                    NotificationManagerService.this.mConditionProviders.notifyConditions(pkg, info, conditions);
                }
            });
        }

        public void requestUnbindProvider(IConditionProvider provider) {
            long identity = Binder.clearCallingIdentity();
            try {
                ManagedServices.ManagedServiceInfo info = NotificationManagerService.this.mConditionProviders.checkServiceToken(provider);
                info.getOwner().setComponentState(info.component, false);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void requestBindProvider(ComponentName component) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(component.getPackageName());
            long identity = Binder.clearCallingIdentity();
            try {
                NotificationManagerService.this.mConditionProviders.setComponentState(component, true);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        private void enforceSystemOrSystemUI(String message) {
            if (!NotificationManagerService.this.isCallerSystemOrPhone()) {
                NotificationManagerService.this.getContext().enforceCallingPermission("android.permission.STATUS_BAR_SERVICE", message);
            }
        }

        private void enforceSystemOrSystemUIOrSamePackage(String pkg, String message) {
            try {
                NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            } catch (SecurityException e) {
                NotificationManagerService.this.getContext().enforceCallingPermission("android.permission.STATUS_BAR_SERVICE", message);
            }
        }

        private void enforcePolicyAccess(int uid, String method) {
            if (NotificationManagerService.this.getContext().checkCallingPermission("android.permission.MANAGE_NOTIFICATIONS") != 0) {
                boolean accessAllowed = false;
                for (String isPackageOrComponentAllowed : NotificationManagerService.this.getContext().getPackageManager().getPackagesForUid(uid)) {
                    if (NotificationManagerService.this.mConditionProviders.isPackageOrComponentAllowed(isPackageOrComponentAllowed, UserHandle.getUserId(uid))) {
                        accessAllowed = true;
                    }
                }
                if (!accessAllowed) {
                    Slog.w(NotificationManagerService.TAG, "Notification policy access denied calling " + method);
                    throw new SecurityException("Notification policy access denied");
                }
            }
        }

        private void enforcePolicyAccess(String pkg, String method) {
            if (NotificationManagerService.this.getContext().checkCallingPermission("android.permission.MANAGE_NOTIFICATIONS") != 0) {
                NotificationManagerService.this.checkCallerIsSameApp(pkg);
                if (!checkPolicyAccess(pkg)) {
                    Slog.w(NotificationManagerService.TAG, "Notification policy access denied calling " + method);
                    throw new SecurityException("Notification policy access denied");
                }
            }
        }

        private boolean checkPackagePolicyAccess(String pkg) {
            return NotificationManagerService.this.mConditionProviders.isPackageOrComponentAllowed(pkg, getCallingUserHandle().getIdentifier());
        }

        private boolean checkPolicyAccess(String pkg) {
            boolean z = false;
            try {
                if (ActivityManager.checkComponentPermission("android.permission.MANAGE_NOTIFICATIONS", NotificationManagerService.this.getContext().getPackageManager().getPackageUidAsUser(pkg, UserHandle.getCallingUserId()), -1, true) == 0) {
                    return true;
                }
                if (checkPackagePolicyAccess(pkg) || NotificationManagerService.this.mListeners.isComponentEnabledForPackage(pkg) || (NotificationManagerService.this.mDpm != null && NotificationManagerService.this.mDpm.isActiveAdminWithPolicy(Binder.getCallingUid(), -1))) {
                    z = true;
                }
                return z;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpAndUsageStatsPermission(NotificationManagerService.this.getContext(), NotificationManagerService.TAG, pw)) {
                DumpFilter filter = DumpFilter.parseFromArguments(args);
                if (filter.stats) {
                    NotificationManagerService.this.dumpJson(pw, filter);
                } else if (filter.proto) {
                    NotificationManagerService.this.dumpProto(fd, filter);
                } else if (filter.criticalPriority) {
                    NotificationManagerService.this.dumpNotificationRecords(pw, filter);
                } else {
                    NotificationManagerService.this.dumpImpl(pw, filter);
                }
            }
        }

        public ComponentName getEffectsSuppressor() {
            if (!NotificationManagerService.this.mEffectsSuppressors.isEmpty()) {
                return (ComponentName) NotificationManagerService.this.mEffectsSuppressors.get(0);
            }
            return null;
        }

        public boolean matchesCallFilter(Bundle extras) {
            UserHandle userHandle;
            enforceSystemOrSystemUI("INotificationManager.matchesCallFilter");
            int userId = -10000;
            if (extras != null) {
                userId = extras.getInt("userId", -10000);
            }
            if (userId == -10000) {
                userHandle = Binder.getCallingUserHandle();
            } else {
                userHandle = UserHandle.of(userId);
            }
            return NotificationManagerService.this.mZenModeHelper.matchesCallFilter(userHandle, extras, (ValidateNotificationPeople) NotificationManagerService.this.mRankingHelper.findExtractor(ValidateNotificationPeople.class), NotificationManagerService.MATCHES_CALL_FILTER_CONTACTS_TIMEOUT_MS, 1.0f);
        }

        public boolean isSystemConditionProviderEnabled(String path) {
            enforceSystemOrSystemUI("INotificationManager.isSystemConditionProviderEnabled");
            return NotificationManagerService.this.mConditionProviders.isSystemProviderEnabled(path);
        }

        public byte[] getBackupPayload(int user) {
            byte[] byteArray;
            NotificationManagerService.this.checkCallerIsSystem();
            if (NotificationManagerService.DBG) {
                Slog.d(NotificationManagerService.TAG, "getBackupPayload u=" + user);
            }
            if (user != 0) {
                Slog.w(NotificationManagerService.TAG, "getBackupPayload: cannot backup policy for user " + user);
                return null;
            }
            synchronized (NotificationManagerService.this.mPolicyFile) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    NotificationManagerService.this.writePolicyXml(baos, true);
                    byteArray = baos.toByteArray();
                } catch (IOException e) {
                    Slog.w(NotificationManagerService.TAG, "getBackupPayload: error writing payload for user " + user, e);
                    return null;
                }
            }
            return byteArray;
        }

        public void applyRestore(byte[] payload, int user) {
            NotificationManagerService.this.checkCallerIsSystem();
            if (NotificationManagerService.DBG) {
                StringBuilder sb = new StringBuilder();
                sb.append("applyRestore u=");
                sb.append(user);
                sb.append(" payload=");
                sb.append(payload != null ? new String(payload, StandardCharsets.UTF_8) : null);
                Slog.d(NotificationManagerService.TAG, sb.toString());
            }
            if (payload == null) {
                Slog.w(NotificationManagerService.TAG, "applyRestore: no payload to restore for user " + user);
            } else if (user != 0) {
                Slog.w(NotificationManagerService.TAG, "applyRestore: cannot restore policy for user " + user);
            } else {
                synchronized (NotificationManagerService.this.mPolicyFile) {
                    try {
                        NotificationManagerService.this.readPolicyXml(new ByteArrayInputStream(payload), true);
                        NotificationManagerService.this.savePolicyFile();
                    } catch (IOException | NumberFormatException | XmlPullParserException e) {
                        Slog.w(NotificationManagerService.TAG, "applyRestore: error reading payload", e);
                    }
                }
            }
        }

        public boolean isNotificationPolicyAccessGranted(String pkg) {
            return checkPolicyAccess(pkg);
        }

        public boolean isNotificationPolicyAccessGrantedForPackage(String pkg) {
            enforceSystemOrSystemUIOrSamePackage(pkg, "request policy access status for another package");
            return checkPolicyAccess(pkg);
        }

        public void setNotificationPolicyAccessGranted(String pkg, boolean granted) throws RemoteException {
            setNotificationPolicyAccessGrantedForUser(pkg, getCallingUserHandle().getIdentifier(), granted);
        }

        public void setNotificationPolicyAccessGrantedForUser(String pkg, int userId, boolean granted) {
            NotificationManagerService.this.checkCallerIsSystemOrShell();
            long identity = Binder.clearCallingIdentity();
            try {
                if (NotificationManagerService.this.mAllowedManagedServicePackages.test(pkg)) {
                    NotificationManagerService.this.mConditionProviders.setPackageOrComponentEnabled(pkg, userId, true, granted);
                    NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED").setPackage(pkg).addFlags(1073741824), UserHandle.of(userId), null);
                    NotificationManagerService.this.savePolicyFile();
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public NotificationManager.Policy getNotificationPolicy(String pkg) {
            long identity = Binder.clearCallingIdentity();
            try {
                return NotificationManagerService.this.mZenModeHelper.getNotificationPolicy();
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void setNotificationPolicy(String pkg, NotificationManager.Policy policy) {
            enforcePolicyAccess(pkg, "setNotificationPolicy");
            long identity = Binder.clearCallingIdentity();
            try {
                ApplicationInfo applicationInfo = NotificationManagerService.this.mPackageManager.getApplicationInfo(pkg, 0, UserHandle.getUserId(NotificationManagerService.MY_UID));
                NotificationManager.Policy currPolicy = NotificationManagerService.this.mZenModeHelper.getNotificationPolicy();
                if (applicationInfo.targetSdkVersion < 28) {
                    policy = new NotificationManager.Policy((policy.priorityCategories & -33 & -65 & -129) | (currPolicy.priorityCategories & 32) | (currPolicy.priorityCategories & 64) | (currPolicy.priorityCategories & 128), policy.priorityCallSenders, policy.priorityMessageSenders, policy.suppressedVisualEffects);
                }
                NotificationManager.Policy policy2 = new NotificationManager.Policy(policy.priorityCategories, policy.priorityCallSenders, policy.priorityMessageSenders, NotificationManagerService.this.calculateSuppressedVisualEffects(policy, currPolicy, applicationInfo.targetSdkVersion));
                ZenLog.traceSetNotificationPolicy(pkg, applicationInfo.targetSdkVersion, policy2);
                NotificationManagerService.this.mZenModeHelper.setNotificationPolicy(policy2);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
        }

        public List<String> getEnabledNotificationListenerPackages() {
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mListeners.getAllowedPackages(getCallingUserHandle().getIdentifier());
        }

        public List<ComponentName> getEnabledNotificationListeners(int userId) {
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mListeners.getAllowedComponents(userId);
        }

        public boolean isNotificationListenerAccessGranted(ComponentName listener) {
            Preconditions.checkNotNull(listener);
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(listener.getPackageName());
            return NotificationManagerService.this.mListeners.isPackageOrComponentAllowed(listener.flattenToString(), getCallingUserHandle().getIdentifier());
        }

        public boolean isNotificationListenerAccessGrantedForUser(ComponentName listener, int userId) {
            Preconditions.checkNotNull(listener);
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mListeners.isPackageOrComponentAllowed(listener.flattenToString(), userId);
        }

        public boolean isNotificationAssistantAccessGranted(ComponentName assistant) {
            Preconditions.checkNotNull(assistant);
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(assistant.getPackageName());
            return NotificationManagerService.this.mAssistants.isPackageOrComponentAllowed(assistant.flattenToString(), getCallingUserHandle().getIdentifier());
        }

        public void setNotificationListenerAccessGranted(ComponentName listener, boolean granted) throws RemoteException {
            setNotificationListenerAccessGrantedForUser(listener, getCallingUserHandle().getIdentifier(), granted);
        }

        public void setNotificationAssistantAccessGranted(ComponentName assistant, boolean granted) throws RemoteException {
            setNotificationAssistantAccessGrantedForUser(assistant, getCallingUserHandle().getIdentifier(), granted);
        }

        public void setNotificationListenerAccessGrantedForUser(ComponentName listener, int userId, boolean granted) throws RemoteException {
            Preconditions.checkNotNull(listener);
            NotificationManagerService.this.checkCallerIsSystemOrShell();
            long identity = Binder.clearCallingIdentity();
            try {
                if (NotificationManagerService.this.mAllowedManagedServicePackages.test(listener.getPackageName())) {
                    NotificationManagerService.this.mConditionProviders.setPackageOrComponentEnabled(listener.flattenToString(), userId, false, granted);
                    NotificationManagerService.this.mListeners.setPackageOrComponentEnabled(listener.flattenToString(), userId, true, granted);
                    NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED").setPackage(listener.getPackageName()).addFlags(1073741824), UserHandle.of(userId), null);
                    NotificationManagerService.this.savePolicyFile();
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void setNotificationAssistantAccessGrantedForUser(ComponentName assistant, int userId, boolean granted) throws RemoteException {
            Preconditions.checkNotNull(assistant);
            NotificationManagerService.this.checkCallerIsSystemOrShell();
            long identity = Binder.clearCallingIdentity();
            try {
                if (NotificationManagerService.this.mAllowedManagedServicePackages.test(assistant.getPackageName())) {
                    NotificationManagerService.this.mConditionProviders.setPackageOrComponentEnabled(assistant.flattenToString(), userId, false, granted);
                    NotificationManagerService.this.mAssistants.setPackageOrComponentEnabled(assistant.flattenToString(), userId, true, granted);
                    NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED").setPackage(assistant.getPackageName()).addFlags(1073741824), UserHandle.of(userId), null);
                    NotificationManagerService.this.savePolicyFile();
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void applyEnqueuedAdjustmentFromAssistant(INotificationListener token, Adjustment adjustment) throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.mAssistants.checkServiceTokenLocked(token);
                    int N = NotificationManagerService.this.mEnqueuedNotifications.size();
                    int i = 0;
                    while (true) {
                        if (i >= N) {
                            break;
                        }
                        NotificationRecord n = NotificationManagerService.this.mEnqueuedNotifications.get(i);
                        if (Objects.equals(adjustment.getKey(), n.getKey()) && Objects.equals(Integer.valueOf(adjustment.getUser()), Integer.valueOf(n.getUserId()))) {
                            NotificationManagerService.this.applyAdjustment(n, adjustment);
                            break;
                        }
                        i++;
                    }
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        public void applyAdjustmentFromAssistant(INotificationListener token, Adjustment adjustment) throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.mAssistants.checkServiceTokenLocked(token);
                    NotificationManagerService.this.applyAdjustment(NotificationManagerService.this.mNotificationsByKey.get(adjustment.getKey()), adjustment);
                }
                NotificationManagerService.this.mRankingHandler.requestSort();
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        public void applyAdjustmentsFromAssistant(INotificationListener token, List<Adjustment> adjustments) throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.mAssistants.checkServiceTokenLocked(token);
                    for (Adjustment adjustment : adjustments) {
                        NotificationManagerService.this.applyAdjustment(NotificationManagerService.this.mNotificationsByKey.get(adjustment.getKey()), adjustment);
                    }
                }
                NotificationManagerService.this.mRankingHandler.requestSort();
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        public void updateNotificationChannelGroupFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user, NotificationChannelGroup group) throws RemoteException {
            Preconditions.checkNotNull(user);
            verifyPrivilegedListener(token, user);
            NotificationManagerService.this.createNotificationChannelGroup(pkg, getUidForPackageAndUser(pkg, user), group, false, true);
            NotificationManagerService.this.savePolicyFile();
        }

        public void updateNotificationChannelFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user, NotificationChannel channel) throws RemoteException {
            Preconditions.checkNotNull(channel);
            Preconditions.checkNotNull(pkg);
            Preconditions.checkNotNull(user);
            verifyPrivilegedListener(token, user);
            NotificationManagerService.this.updateNotificationChannelInt(pkg, getUidForPackageAndUser(pkg, user), channel, true);
        }

        public ParceledListSlice<NotificationChannel> getNotificationChannelsFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user) throws RemoteException {
            Preconditions.checkNotNull(pkg);
            Preconditions.checkNotNull(user);
            verifyPrivilegedListener(token, user);
            return NotificationManagerService.this.mRankingHelper.getNotificationChannels(pkg, getUidForPackageAndUser(pkg, user), false);
        }

        public ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroupsFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user) throws RemoteException {
            Preconditions.checkNotNull(pkg);
            Preconditions.checkNotNull(user);
            verifyPrivilegedListener(token, user);
            List<NotificationChannelGroup> groups = new ArrayList<>();
            groups.addAll(NotificationManagerService.this.mRankingHelper.getNotificationChannelGroups(pkg, getUidForPackageAndUser(pkg, user)));
            return new ParceledListSlice<>(groups);
        }

        private void verifyPrivilegedListener(INotificationListener token, UserHandle user) {
            ManagedServices.ManagedServiceInfo info;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
            }
            if (!NotificationManagerService.this.hasCompanionDevice(info)) {
                throw new SecurityException(info + " does not have access");
            } else if (!info.enabledAndUserMatches(user.getIdentifier())) {
                throw new SecurityException(info + " does not have access");
            }
        }

        private int getUidForPackageAndUser(String pkg, UserHandle user) throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                return NotificationManagerService.this.mPackageManager.getPackageUid(pkg, 0, user.getIdentifier());
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void doBindRecSys() {
            Slog.w(NotificationManagerService.TAG, "do bind recsys service");
            NotificationManagerService.this.checkCallerIsSystem();
            NotificationManagerService.this.bindRecSys();
        }

        /* JADX WARNING: type inference failed for: r1v1, types: [android.os.Binder] */
        /* JADX WARNING: Multi-variable type inference failed */
        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
            new ShellCmd().exec(this, in, out, err, args, callback, resultReceiver);
        }
    };
    /* access modifiers changed from: private */
    public SettingsObserver mSettingsObserver;
    /* access modifiers changed from: private */
    public SnoozeHelper mSnoozeHelper;
    protected String mSoundNotificationKey;
    StatusBarManagerInternal mStatusBar;
    final ArrayMap<String, NotificationRecord> mSummaryByGroupKey = new ArrayMap<>();
    boolean mSystemReady;
    final ArrayList<ToastRecord> mToastQueue = new ArrayList<>();
    /* access modifiers changed from: private */
    public NotificationUsageStats mUsageStats;
    private boolean mUseAttentionLight;
    /* access modifiers changed from: private */
    public final ManagedServices.UserProfiles mUserProfiles = new ManagedServices.UserProfiles();
    private String mVibrateNotificationKey;
    Vibrator mVibrator;
    /* access modifiers changed from: private */
    public WindowManagerInternal mWindowManagerInternal;
    protected ZenModeHelper mZenModeHelper;
    private String[] noitfication_white_list = {"com.huawei.message", "com.android.mms", "com.android.contacts", "com.android.phone", "com.android.deskclock", "com.android.calendar", "com.android.systemui", PackageManagerService.PLATFORM_PACKAGE_NAME, "com.android.incallui", "com.android.phone.recorder", "com.android.cellbroadcastreceiver", TELECOM_PKG};
    private String[] plus_notification_white_list = {"com.android.bluetooth"};
    private final BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (NotificationManagerService.ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE.equals(action)) {
                    if (intent.getIntExtra(NotificationManagerService.POWER_MODE, 0) == 3) {
                        if (NotificationManagerService.this.mPowerSaverObserver == null) {
                            PowerSaverObserver unused = NotificationManagerService.this.mPowerSaverObserver = new PowerSaverObserver(NotificationManagerService.this.mHandler);
                        }
                        NotificationManagerService.this.mPowerSaverObserver.observe();
                        Log.i(NotificationManagerService.TAG, "super power save 2.0 recevier brodcast register sqlite listener");
                    }
                } else if (NotificationManagerService.ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE.equals(action) && intent.getIntExtra(NotificationManagerService.SHUTDOWN_LIMIT_POWERMODE, 0) == 0 && NotificationManagerService.this.mPowerSaverObserver != null) {
                    NotificationManagerService.this.mPowerSaverObserver.unObserve();
                    PowerSaverObserver unused2 = NotificationManagerService.this.mPowerSaverObserver = null;
                    Log.i(NotificationManagerService.TAG, "super power save 2.0 recevier brodcast unregister sqlite listener");
                }
            }
        }
    };
    private HashSet<String> power_save_whiteSet = new HashSet<>();

    private static class Archive {
        final ArrayDeque<StatusBarNotification> mBuffer = new ArrayDeque<>(this.mBufferSize);
        final int mBufferSize;

        public Archive(int size) {
            this.mBufferSize = size;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            int N = this.mBuffer.size();
            sb.append("Archive (");
            sb.append(N);
            sb.append(" notification");
            sb.append(N == 1 ? ")" : "s)");
            return sb.toString();
        }

        public void record(StatusBarNotification nr) {
            if (this.mBuffer.size() == this.mBufferSize) {
                this.mBuffer.removeFirst();
            }
            this.mBuffer.addLast(nr.cloneLight());
        }

        public Iterator<StatusBarNotification> descendingIterator() {
            return this.mBuffer.descendingIterator();
        }

        public StatusBarNotification[] getArray(int count) {
            if (count == 0) {
                count = this.mBufferSize;
            }
            StatusBarNotification[] a = new StatusBarNotification[Math.min(count, this.mBuffer.size())];
            Iterator<StatusBarNotification> iter = descendingIterator();
            int i = 0;
            while (iter.hasNext() && i < count) {
                a[i] = iter.next();
                i++;
            }
            return a;
        }
    }

    public static final class DumpFilter {
        public boolean criticalPriority = false;
        public boolean filtered = false;
        public boolean normalPriority = false;
        public String pkgFilter;
        public boolean proto = false;
        public boolean redact = true;
        public long since;
        public boolean stats;
        public boolean zen;

        public static DumpFilter parseFromArguments(String[] args) {
            DumpFilter filter = new DumpFilter();
            int ai = 0;
            while (ai < args.length) {
                String a = args[ai];
                if (!PriorityDump.PROTO_ARG.equals(a)) {
                    if (!"--noredact".equals(a) && !"--reveal".equals(a)) {
                        if (!"p".equals(a) && !AbsLocationManagerService.DEL_PKG.equals(a) && !"--package".equals(a)) {
                            if (!"--zen".equals(a) && !"zen".equals(a)) {
                                if (!"--stats".equals(a)) {
                                    if (PriorityDump.PRIORITY_ARG.equals(a) && ai < args.length - 1) {
                                        ai++;
                                        String str = args[ai];
                                        char c = 65535;
                                        int hashCode = str.hashCode();
                                        if (hashCode != -1986416409) {
                                            if (hashCode == -1560189025 && str.equals(PriorityDump.PRIORITY_ARG_CRITICAL)) {
                                                c = 0;
                                            }
                                        } else if (str.equals(PriorityDump.PRIORITY_ARG_NORMAL)) {
                                            c = 1;
                                        }
                                        switch (c) {
                                            case 0:
                                                filter.criticalPriority = true;
                                                break;
                                            case 1:
                                                filter.normalPriority = true;
                                                break;
                                        }
                                    }
                                } else {
                                    filter.stats = true;
                                    if (ai < args.length - 1) {
                                        ai++;
                                        filter.since = Long.parseLong(args[ai]);
                                    } else {
                                        filter.since = 0;
                                    }
                                }
                            } else {
                                filter.filtered = true;
                                filter.zen = true;
                            }
                        } else if (ai < args.length - 1) {
                            ai++;
                            filter.pkgFilter = args[ai].trim().toLowerCase();
                            if (filter.pkgFilter.isEmpty()) {
                                filter.pkgFilter = null;
                            } else {
                                filter.filtered = true;
                            }
                        }
                    } else {
                        filter.redact = false;
                    }
                } else {
                    filter.proto = true;
                }
                ai++;
            }
            return filter;
        }

        public boolean matches(StatusBarNotification sbn) {
            boolean z = true;
            if (!this.filtered) {
                return true;
            }
            if (!this.zen && (sbn == null || (!matches(sbn.getPackageName()) && !matches(sbn.getOpPkg())))) {
                z = false;
            }
            return z;
        }

        public boolean matches(ComponentName component) {
            boolean z = true;
            if (!this.filtered) {
                return true;
            }
            if (!this.zen && (component == null || !matches(component.getPackageName()))) {
                z = false;
            }
            return z;
        }

        public boolean matches(String pkg) {
            boolean z = true;
            if (!this.filtered) {
                return true;
            }
            if (!this.zen && (pkg == null || !pkg.toLowerCase().contains(this.pkgFilter))) {
                z = false;
            }
            return z;
        }

        public String toString() {
            if (this.stats) {
                return "stats";
            }
            if (this.zen) {
                return "zen";
            }
            return '\'' + this.pkgFilter + '\'';
        }
    }

    protected class EnqueueNotificationRunnable implements Runnable {
        private final NotificationRecord r;
        private final int userId;

        EnqueueNotificationRunnable(int userId2, NotificationRecord r2) {
            this.userId = userId2;
            this.r = r2;
        }

        public void run() {
            int bigContentViewSize;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationManagerService.this.mEnqueuedNotifications.add(this.r);
                NotificationManagerService.this.scheduleTimeoutLocked(this.r);
                StatusBarNotification n = this.r.sbn;
                if (NotificationManagerService.DBG) {
                    Slog.d(NotificationManagerService.TAG, "EnqueueNotificationRunnable.run for: " + n.getKey());
                }
                NotificationRecord old = NotificationManagerService.this.mNotificationsByKey.get(n.getKey());
                if (old != null) {
                    this.r.copyRankingInformation(old);
                }
                int callingUid = n.getUid();
                int callingPid = n.getInitialPid();
                Notification notification = n.getNotification();
                String pkg = n.getPackageName();
                int id = n.getId();
                String tag = n.getTag();
                NotificationManagerService.this.handleGroupedNotificationLocked(this.r, old, callingUid, callingPid);
                if (n.isGroup() && notification.isGroupChild()) {
                    NotificationManagerService.this.mSnoozeHelper.repostGroupSummary(pkg, this.r.getUserId(), n.getGroupKey());
                }
                if (!pkg.equals("com.android.providers.downloads") || Log.isLoggable("DownloadManager", 2)) {
                    int enqueueStatus = 0;
                    if (old != null) {
                        enqueueStatus = 1;
                    }
                    int enqueueStatus2 = enqueueStatus;
                    RemoteViews contentView = notification.contentView;
                    RemoteViews bigContentView = notification.bigContentView;
                    int contentViewSize = 0;
                    if (contentView != null) {
                        contentViewSize = contentView.getCacheSize();
                    }
                    int contentViewSize2 = contentViewSize;
                    if (bigContentView != null) {
                        bigContentViewSize = bigContentView.getCacheSize();
                    } else {
                        bigContentViewSize = 0;
                    }
                    int i = bigContentViewSize;
                    int i2 = contentViewSize2;
                    RemoteViews remoteViews = bigContentView;
                    int i3 = this.userId;
                    RemoteViews remoteViews2 = contentView;
                    EventLogTags.writeNotificationEnqueue(callingUid, callingPid, pkg, id, tag, i3, notification.toString() + " contentViewSize = " + contentViewSize2 + " bigContentViewSize = " + bigContentViewSize + " N = " + NotificationManagerService.this.mNotificationList.size(), enqueueStatus2);
                }
                NotificationManagerService.this.mRankingHelper.extractSignals(this.r);
                if (NotificationManagerService.this.mNotificationResource != null) {
                    NotificationManagerService.this.mNotificationResource.release(callingUid, pkg, -1);
                }
                if (NotificationManagerService.this.mAssistants.isEnabled()) {
                    NotificationManagerService.this.mAssistants.onNotificationEnqueued(this.r);
                    NotificationManagerService.this.mHandler.postDelayed(new PostNotificationRunnable(this.r.getKey()), NotificationManagerService.DELAY_FOR_ASSISTANT_TIME);
                } else if (!NotificationManagerService.this.doForUpdateNotification(this.r.getKey(), NotificationManagerService.this.mHandler)) {
                    NotificationManagerService.this.mHandler.post(new PostNotificationRunnable(this.r.getKey()));
                }
            }
        }
    }

    private interface FlagChecker {
        boolean apply(int i);
    }

    public class NotificationAssistants extends ManagedServices {
        static final String TAG_ENABLED_NOTIFICATION_ASSISTANTS = "enabled_assistants";

        public NotificationAssistants(Context context, Object lock, ManagedServices.UserProfiles up, IPackageManager pm) {
            super(context, lock, up, pm);
        }

        /* access modifiers changed from: protected */
        public ManagedServices.Config getConfig() {
            ManagedServices.Config c = new ManagedServices.Config();
            c.caption = "notification assistant";
            c.serviceInterface = "android.service.notification.NotificationAssistantService";
            c.xmlTag = TAG_ENABLED_NOTIFICATION_ASSISTANTS;
            c.secureSettingName = "enabled_notification_assistant";
            c.bindPermission = "android.permission.BIND_NOTIFICATION_ASSISTANT_SERVICE";
            c.settingsAction = "android.settings.MANAGE_DEFAULT_APPS_SETTINGS";
            c.clientLabel = 17040606;
            return c;
        }

        /* access modifiers changed from: protected */
        public IInterface asInterface(IBinder binder) {
            return INotificationListener.Stub.asInterface(binder);
        }

        /* access modifiers changed from: protected */
        public boolean checkType(IInterface service) {
            return service instanceof INotificationListener;
        }

        /* access modifiers changed from: protected */
        public void onServiceAdded(ManagedServices.ManagedServiceInfo info) {
            NotificationManagerService.this.mListeners.registerGuestService(info);
        }

        /* access modifiers changed from: protected */
        @GuardedBy("mNotificationLock")
        public void onServiceRemovedLocked(ManagedServices.ManagedServiceInfo removed) {
            NotificationManagerService.this.mListeners.unregisterService(removed.service, removed.userid);
        }

        public void onUserUnlocked(int user) {
            if (this.DEBUG) {
                String str = this.TAG;
                Slog.d(str, "onUserUnlocked u=" + user);
            }
            rebindServices(true);
        }

        public void onNotificationEnqueued(NotificationRecord r) {
            StatusBarNotification sbn = r.sbn;
            TrimCache trimCache = new TrimCache(sbn);
            for (final ManagedServices.ManagedServiceInfo info : getServices()) {
                if (NotificationManagerService.this.isVisibleToListener(sbn, info)) {
                    final StatusBarNotification sbnToPost = trimCache.ForListener(info);
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationAssistants.this.notifyEnqueued(info, sbnToPost);
                        }
                    });
                }
            }
        }

        /* access modifiers changed from: private */
        public void notifyEnqueued(ManagedServices.ManagedServiceInfo info, StatusBarNotification sbn) {
            INotificationListener assistant = info.service;
            try {
                assistant.onNotificationEnqueued(new StatusBarNotificationHolder(sbn));
            } catch (RemoteException ex) {
                String str = this.TAG;
                Log.e(str, "unable to notify assistant (enqueued): " + assistant, ex);
            }
        }

        @GuardedBy("mNotificationLock")
        public void notifyAssistantSnoozedLocked(StatusBarNotification sbn, final String snoozeCriterionId) {
            TrimCache trimCache = new TrimCache(sbn);
            for (final ManagedServices.ManagedServiceInfo info : getServices()) {
                if (NotificationManagerService.this.isVisibleToListener(sbn, info)) {
                    final StatusBarNotification sbnToPost = trimCache.ForListener(info);
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            INotificationListener assistant = info.service;
                            try {
                                assistant.onNotificationSnoozedUntilContext(new StatusBarNotificationHolder(sbnToPost), snoozeCriterionId);
                            } catch (RemoteException ex) {
                                String str = NotificationAssistants.this.TAG;
                                Log.e(str, "unable to notify assistant (snoozed): " + assistant, ex);
                            }
                        }
                    });
                }
            }
        }

        public boolean isEnabled() {
            return !getServices().isEmpty();
        }

        /* access modifiers changed from: protected */
        public void ensureAssistant() {
            for (UserInfo userInfo : this.mUm.getUsers(true)) {
                int userId = userInfo.getUserHandle().getIdentifier();
                if (getAllowedPackages(userId).isEmpty()) {
                    String str = this.TAG;
                    Slog.d(str, "Approving default notification assistant for user " + userId);
                    NotificationManagerService.this.readDefaultAssistant(userId);
                }
            }
        }
    }

    public class NotificationListeners extends ManagedServices {
        static final String TAG_ENABLED_NOTIFICATION_LISTENERS = "enabled_listeners";
        private final ArraySet<ManagedServices.ManagedServiceInfo> mLightTrimListeners = new ArraySet<>();

        public NotificationListeners(IPackageManager pm) {
            super(NotificationManagerService.this.getContext(), NotificationManagerService.this.mNotificationLock, NotificationManagerService.this.mUserProfiles, pm);
        }

        /* access modifiers changed from: protected */
        public ManagedServices.Config getConfig() {
            ManagedServices.Config c = new ManagedServices.Config();
            c.caption = "notification listener";
            c.serviceInterface = "android.service.notification.NotificationListenerService";
            c.xmlTag = TAG_ENABLED_NOTIFICATION_LISTENERS;
            c.secureSettingName = "enabled_notification_listeners";
            c.bindPermission = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE";
            c.settingsAction = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
            c.clientLabel = 17040604;
            return c;
        }

        /* access modifiers changed from: protected */
        public IInterface asInterface(IBinder binder) {
            return INotificationListener.Stub.asInterface(binder);
        }

        /* access modifiers changed from: protected */
        public boolean checkType(IInterface service) {
            return service instanceof INotificationListener;
        }

        public void onServiceAdded(ManagedServices.ManagedServiceInfo info) {
            NotificationRankingUpdate update;
            INotificationListener listener = info.service;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                update = NotificationManagerService.this.makeRankingUpdateLocked(info);
            }
            try {
                listener.onListenerConnected(update);
            } catch (RemoteException e) {
            }
        }

        /* access modifiers changed from: protected */
        @GuardedBy("mNotificationLock")
        public void onServiceRemovedLocked(ManagedServices.ManagedServiceInfo removed) {
            if (NotificationManagerService.this.removeDisabledHints(removed)) {
                NotificationManagerService.this.updateListenerHintsLocked();
                NotificationManagerService.this.updateEffectsSuppressorLocked();
            }
            this.mLightTrimListeners.remove(removed);
        }

        @GuardedBy("mNotificationLock")
        public void setOnNotificationPostedTrimLocked(ManagedServices.ManagedServiceInfo info, int trim) {
            if (trim == 1) {
                this.mLightTrimListeners.add(info);
            } else {
                this.mLightTrimListeners.remove(info);
            }
        }

        public int getOnNotificationPostedTrim(ManagedServices.ManagedServiceInfo info) {
            return this.mLightTrimListeners.contains(info) ? 1 : 0;
        }

        @GuardedBy("mNotificationLock")
        public void notifyPostedLocked(NotificationRecord r, NotificationRecord old) {
            notifyPostedLocked(r, old, true);
        }

        @GuardedBy("mNotificationLock")
        private void notifyPostedLocked(NotificationRecord r, NotificationRecord old, boolean notifyAllListeners) {
            StatusBarNotification sbn = r.sbn;
            StatusBarNotification oldSbn = old != null ? old.sbn : null;
            TrimCache trimCache = new TrimCache(sbn);
            for (final ManagedServices.ManagedServiceInfo info : getServices()) {
                boolean sbnVisible = NotificationManagerService.this.isVisibleToListener(sbn, info);
                int targetUserId = 0;
                boolean oldSbnVisible = oldSbn != null ? NotificationManagerService.this.isVisibleToListener(oldSbn, info) : false;
                if ((oldSbnVisible || sbnVisible) && ((!r.isHidden() || info.targetSdkVersion >= 28) && (notifyAllListeners || info.targetSdkVersion < 28))) {
                    final NotificationRankingUpdate update = NotificationManagerService.this.makeRankingUpdateLocked(info);
                    if (!oldSbnVisible || sbnVisible) {
                        if (info.userid != -1) {
                            targetUserId = info.userid;
                        }
                        NotificationManagerService.this.updateUriPermissions(r, old, info.component.getPackageName(), targetUserId);
                        final StatusBarNotification sbnToPost = trimCache.ForListener(info);
                        NotificationManagerService.this.mHandler.post(new Runnable() {
                            public void run() {
                                NotificationListeners.this.notifyPosted(info, sbnToPost, update);
                            }
                        });
                    } else {
                        final StatusBarNotification oldSbnLightClone = oldSbn.cloneLight();
                        NotificationManagerService.this.mHandler.post(new Runnable() {
                            public void run() {
                                NotificationListeners.this.notifyRemoved(info, oldSbnLightClone, update, null, 6);
                            }
                        });
                    }
                }
            }
        }

        @GuardedBy("mNotificationLock")
        public void notifyRemovedLocked(NotificationRecord r, int reason, NotificationStats notificationStats) {
            NotificationRecord notificationRecord = r;
            int i = reason;
            StatusBarNotification sbn = notificationRecord.sbn;
            StatusBarNotification sbnLight = sbn.cloneLight();
            for (ManagedServices.ManagedServiceInfo info : getServices()) {
                if (NotificationManagerService.this.isVisibleToListener(sbn, info) && ((!r.isHidden() || i == 14 || info.targetSdkVersion >= 28) && (i != 14 || info.targetSdkVersion < 28))) {
                    final NotificationStats stats = NotificationManagerService.this.mAssistants.isServiceTokenValidLocked(info.service) ? notificationStats : null;
                    NotificationRankingUpdate update = NotificationManagerService.this.makeRankingUpdateLocked(info);
                    WorkerHandler access$2300 = NotificationManagerService.this.mHandler;
                    final ManagedServices.ManagedServiceInfo managedServiceInfo = info;
                    final StatusBarNotification statusBarNotification = sbnLight;
                    final NotificationRankingUpdate notificationRankingUpdate = update;
                    StatusBarNotification sbn2 = sbn;
                    AnonymousClass3 r10 = r0;
                    final int i2 = i;
                    AnonymousClass3 r0 = new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyRemoved(managedServiceInfo, statusBarNotification, notificationRankingUpdate, stats, i2);
                        }
                    };
                    access$2300.post(r10);
                    sbn = sbn2;
                }
            }
            NotificationManagerService.this.mHandler.post(new Runnable(notificationRecord) {
                private final /* synthetic */ NotificationRecord f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    NotificationManagerService.this.updateUriPermissions(null, this.f$1, null, 0);
                }
            });
        }

        @GuardedBy("mNotificationLock")
        public void notifyRankingUpdateLocked(List<NotificationRecord> changedHiddenNotifications) {
            boolean isHiddenRankingUpdate = changedHiddenNotifications != null && changedHiddenNotifications.size() > 0;
            for (final ManagedServices.ManagedServiceInfo serviceInfo : getServices()) {
                if (serviceInfo.isEnabledForCurrentProfiles()) {
                    boolean notifyThisListener = false;
                    if (isHiddenRankingUpdate && serviceInfo.targetSdkVersion >= 28) {
                        Iterator<NotificationRecord> it = changedHiddenNotifications.iterator();
                        while (true) {
                            if (it.hasNext()) {
                                if (NotificationManagerService.this.isVisibleToListener(it.next().sbn, serviceInfo)) {
                                    notifyThisListener = true;
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                    if (notifyThisListener || !isHiddenRankingUpdate) {
                        final NotificationRankingUpdate update = NotificationManagerService.this.makeRankingUpdateLocked(serviceInfo);
                        NotificationManagerService.this.mHandler.post(new Runnable() {
                            public void run() {
                                NotificationListeners.this.notifyRankingUpdate(serviceInfo, update);
                            }
                        });
                    }
                }
            }
        }

        @GuardedBy("mNotificationLock")
        public void notifyListenerHintsChangedLocked(final int hints) {
            for (final ManagedServices.ManagedServiceInfo serviceInfo : getServices()) {
                if (serviceInfo.isEnabledForCurrentProfiles()) {
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyListenerHintsChanged(serviceInfo, hints);
                        }
                    });
                }
            }
        }

        @GuardedBy("mNotificationLock")
        public void notifyHiddenLocked(List<NotificationRecord> changedNotifications) {
            if (changedNotifications != null && changedNotifications.size() != 0) {
                notifyRankingUpdateLocked(changedNotifications);
                int numChangedNotifications = changedNotifications.size();
                for (int i = 0; i < numChangedNotifications; i++) {
                    NotificationRecord rec = changedNotifications.get(i);
                    NotificationManagerService.this.mListeners.notifyRemovedLocked(rec, 14, rec.getStats());
                }
            }
        }

        @GuardedBy("mNotificationLock")
        public void notifyUnhiddenLocked(List<NotificationRecord> changedNotifications) {
            if (changedNotifications != null && changedNotifications.size() != 0) {
                notifyRankingUpdateLocked(changedNotifications);
                int numChangedNotifications = changedNotifications.size();
                for (int i = 0; i < numChangedNotifications; i++) {
                    NotificationRecord rec = changedNotifications.get(i);
                    NotificationManagerService.this.mListeners.notifyPostedLocked(rec, rec, false);
                }
            }
        }

        public void notifyInterruptionFilterChanged(final int interruptionFilter) {
            for (final ManagedServices.ManagedServiceInfo serviceInfo : getServices()) {
                if (serviceInfo.isEnabledForCurrentProfiles()) {
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyInterruptionFilterChanged(serviceInfo, interruptionFilter);
                        }
                    });
                }
            }
        }

        /* access modifiers changed from: protected */
        public void notifyNotificationChannelChanged(String pkg, UserHandle user, NotificationChannel channel, int modificationType) {
            if (channel != null) {
                for (ManagedServices.ManagedServiceInfo serviceInfo : getServices()) {
                    if (serviceInfo.enabledAndUserMatches(UserHandle.getCallingUserId())) {
                        Handler handler = BackgroundThread.getHandler();
                        $$Lambda$NotificationManagerService$NotificationListeners$E8qsFPrFYYUtUGked50pRub20 r2 = new Runnable(serviceInfo, pkg, user, channel, modificationType) {
                            private final /* synthetic */ ManagedServices.ManagedServiceInfo f$1;
                            private final /* synthetic */ String f$2;
                            private final /* synthetic */ UserHandle f$3;
                            private final /* synthetic */ NotificationChannel f$4;
                            private final /* synthetic */ int f$5;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                                this.f$4 = r5;
                                this.f$5 = r6;
                            }

                            public final void run() {
                                NotificationManagerService.NotificationListeners.lambda$notifyNotificationChannelChanged$1(NotificationManagerService.NotificationListeners.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
                            }
                        };
                        handler.post(r2);
                    }
                }
            }
        }

        public static /* synthetic */ void lambda$notifyNotificationChannelChanged$1(NotificationListeners notificationListeners, ManagedServices.ManagedServiceInfo serviceInfo, String pkg, UserHandle user, NotificationChannel channel, int modificationType) {
            if (NotificationManagerService.this.hasCompanionDevice(serviceInfo) || serviceInfo.component.toString().contains(NotificationManagerService.HWSYSTEMMANAGER_PKG)) {
                notificationListeners.notifyNotificationChannelChanged(serviceInfo, pkg, user, channel, modificationType);
            }
        }

        /* access modifiers changed from: protected */
        public void notifyNotificationChannelGroupChanged(String pkg, UserHandle user, NotificationChannelGroup group, int modificationType) {
            if (group != null) {
                for (ManagedServices.ManagedServiceInfo serviceInfo : getServices()) {
                    if (serviceInfo.enabledAndUserMatches(UserHandle.getCallingUserId())) {
                        Handler handler = BackgroundThread.getHandler();
                        $$Lambda$NotificationManagerService$NotificationListeners$ZpwYxOiDD13VBHvGZVH3p7iGkFI r2 = new Runnable(serviceInfo, pkg, user, group, modificationType) {
                            private final /* synthetic */ ManagedServices.ManagedServiceInfo f$1;
                            private final /* synthetic */ String f$2;
                            private final /* synthetic */ UserHandle f$3;
                            private final /* synthetic */ NotificationChannelGroup f$4;
                            private final /* synthetic */ int f$5;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                                this.f$4 = r5;
                                this.f$5 = r6;
                            }

                            public final void run() {
                                NotificationManagerService.NotificationListeners.lambda$notifyNotificationChannelGroupChanged$2(NotificationManagerService.NotificationListeners.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
                            }
                        };
                        handler.post(r2);
                    }
                }
            }
        }

        public static /* synthetic */ void lambda$notifyNotificationChannelGroupChanged$2(NotificationListeners notificationListeners, ManagedServices.ManagedServiceInfo serviceInfo, String pkg, UserHandle user, NotificationChannelGroup group, int modificationType) {
            if (NotificationManagerService.this.hasCompanionDevice(serviceInfo)) {
                notificationListeners.notifyNotificationChannelGroupChanged(serviceInfo, pkg, user, group, modificationType);
            }
        }

        /* access modifiers changed from: private */
        public void notifyPosted(ManagedServices.ManagedServiceInfo info, StatusBarNotification sbn, NotificationRankingUpdate rankingUpdate) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationPosted(new StatusBarNotificationHolder(sbn), rankingUpdate);
            } catch (TransactionTooLargeException ex) {
                int callingUid = Binder.getCallingUid();
                int callingPid = Binder.getCallingPid();
                int userId = sbn.getUserId();
                String str = this.TAG;
                Log.e(str, "unable to notify listener (posted): " + listener + ",callingUid=" + callingUid + ",callingPid=" + callingPid + ",userId=" + userId, ex);
                NotificationManagerService.this.mNotificationDelegate.onClearAll(callingUid, callingPid, userId);
            } catch (RemoteException ex2) {
                String str2 = this.TAG;
                Log.e(str2, "unable to notify listener (posted): " + listener, ex2);
            }
        }

        /* access modifiers changed from: private */
        public void notifyRemoved(ManagedServices.ManagedServiceInfo info, StatusBarNotification sbn, NotificationRankingUpdate rankingUpdate, NotificationStats stats, int reason) {
            if (info.enabledAndUserMatches(sbn.getUserId())) {
                INotificationListener listener = info.service;
                try {
                    listener.onNotificationRemoved(new StatusBarNotificationHolder(sbn), rankingUpdate, stats, reason);
                    LogPower.push(123, sbn.getPackageName(), Integer.toString(sbn.getId()), sbn.getOpPkg(), new String[]{Integer.toString(sbn.getNotification().flags)});
                    NotificationManagerService.this.reportToIAware(sbn.getPackageName(), sbn.getUid(), sbn.getId(), false);
                } catch (RemoteException ex) {
                    String str = this.TAG;
                    Log.e(str, "unable to notify listener (removed): " + listener, ex);
                }
            }
        }

        /* access modifiers changed from: private */
        public void notifyRankingUpdate(ManagedServices.ManagedServiceInfo info, NotificationRankingUpdate rankingUpdate) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationRankingUpdate(rankingUpdate);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Log.e(str, "unable to notify listener (ranking update): " + listener, ex);
            }
        }

        /* access modifiers changed from: private */
        public void notifyListenerHintsChanged(ManagedServices.ManagedServiceInfo info, int hints) {
            INotificationListener listener = info.service;
            try {
                listener.onListenerHintsChanged(hints);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Log.e(str, "unable to notify listener (listener hints): " + listener, ex);
            }
        }

        /* access modifiers changed from: private */
        public void notifyInterruptionFilterChanged(ManagedServices.ManagedServiceInfo info, int interruptionFilter) {
            INotificationListener listener = info.service;
            try {
                listener.onInterruptionFilterChanged(interruptionFilter);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Log.e(str, "unable to notify listener (interruption filter): " + listener, ex);
            }
        }

        /* access modifiers changed from: package-private */
        public void notifyNotificationChannelChanged(ManagedServices.ManagedServiceInfo info, String pkg, UserHandle user, NotificationChannel channel, int modificationType) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationChannelModification(pkg, user, channel, modificationType);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Log.e(str, "unable to notify listener (channel changed): " + listener, ex);
            }
        }

        private void notifyNotificationChannelGroupChanged(ManagedServices.ManagedServiceInfo info, String pkg, UserHandle user, NotificationChannelGroup group, int modificationType) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationChannelGroupModification(pkg, user, group, modificationType);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Log.e(str, "unable to notify listener (channel group changed): " + listener, ex);
            }
        }

        public boolean isListenerPackage(String packageName) {
            if (packageName == null) {
                return false;
            }
            synchronized (NotificationManagerService.this.mNotificationLock) {
                for (ManagedServices.ManagedServiceInfo serviceInfo : getServices()) {
                    if (packageName.equals(serviceInfo.component.getPackageName())) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    protected class PostNotificationRunnable implements Runnable {
        private final String key;

        PostNotificationRunnable(String key2) {
            this.key = key2;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:74:0x023c, code lost:
            if (java.util.Objects.equals(r9.getGroup(), r6.getGroup()) == false) goto L_0x023e;
         */
        /* JADX WARNING: Removed duplicated region for block: B:136:0x03ee A[Catch:{ IllegalArgumentException -> 0x02e1, all -> 0x03dc }] */
        /* JADX WARNING: Removed duplicated region for block: B:154:0x040f A[EDGE_INSN: B:154:0x040f->B:140:0x040f ?: BREAK  , SYNTHETIC] */
        public void run() {
            int i;
            int N;
            int i2;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = null;
                int i3 = 0;
                try {
                    int N2 = NotificationManagerService.this.mEnqueuedNotifications.size();
                    int i4 = 0;
                    while (true) {
                        if (i4 >= N2) {
                            break;
                        }
                        NotificationRecord enqueued = NotificationManagerService.this.mEnqueuedNotifications.get(i4);
                        if (Objects.equals(this.key, enqueued.getKey())) {
                            r = enqueued;
                            break;
                        }
                        i4++;
                    }
                    NotificationRecord r2 = r;
                    if (r2 == null) {
                        Slog.i(NotificationManagerService.TAG, "Cannot find enqueued record for key: " + this.key);
                        int N3 = NotificationManagerService.this.mEnqueuedNotifications.size();
                        while (true) {
                            if (i3 >= N3) {
                                break;
                            } else if (Objects.equals(this.key, NotificationManagerService.this.mEnqueuedNotifications.get(i3).getKey())) {
                                NotificationManagerService.this.mEnqueuedNotifications.remove(i3);
                                break;
                            } else {
                                i3++;
                            }
                        }
                        NotificationManagerService.this.removeNotificationInUpdateQueue(this.key);
                        return;
                    }
                    r2.setHidden(NotificationManagerService.this.isPackageSuspendedLocked(r2));
                    NotificationRecord old = NotificationManagerService.this.mNotificationsByKey.get(this.key);
                    final StatusBarNotification n = r2.sbn;
                    Notification notification = n.getNotification();
                    int index = NotificationManagerService.this.indexOfNotificationLocked(n.getKey());
                    StatusBarNotification oldSbn = null;
                    if (index < 0) {
                        NotificationManagerService.this.mNotificationList.add(r2);
                        NotificationManagerService.this.mUsageStats.registerPostedByApp(r2);
                        r2.setInterruptive(NotificationManagerService.this.isVisuallyInterruptive(null, r2));
                    } else {
                        old = NotificationManagerService.this.mNotificationList.get(index);
                        NotificationManagerService.this.mNotificationList.set(index, r2);
                        NotificationManagerService.this.mUsageStats.registerUpdatedByApp(r2, old);
                        notification.flags |= old.getNotification().flags & 64;
                        r2.isUpdate = true;
                        r2.setTextChanged(NotificationManagerService.this.isVisuallyInterruptive(old, r2));
                    }
                    NotificationRecord old2 = old;
                    Flog.i(400, "enqueueNotificationInternal: n.getKey = " + n.getKey());
                    NotificationManagerService.this.mNotificationsByKey.put(n.getKey(), r2);
                    if ((notification.flags & 64) != 0) {
                        notification.flags |= 34;
                    }
                    NotificationManagerService.this.applyZenModeLocked(r2);
                    NotificationManagerService.this.mRankingHelper.sort(NotificationManagerService.this.mNotificationList);
                    final int notifyType = NotificationManagerService.this.calculateNotifyType(r2);
                    if (notification.getSmallIcon() != null) {
                        if (old2 != null) {
                            oldSbn = old2.sbn;
                        }
                        if (n.getNotification().extras != null && !NotificationManagerService.DISABLE_MULTIWIN) {
                            n.getNotification().extras.putString("specialType", SplitNotificationUtils.getInstance(NotificationManagerService.this.getContext()).getNotificationType(n.getPackageName(), 1));
                            n.getNotification().extras.putBoolean("topFullscreen", !NotificationManagerService.this.isExpandedNtfPkg(n.getPackageName()) && (NotificationManagerService.this.isTopFullscreen() || NotificationManagerService.this.isPackageRequestNarrowNotification()));
                        }
                        if (notification.extras != null) {
                            boolean isGameDndSwitchOn = false;
                            try {
                                if (NotificationManagerService.this.mGameDndStatus) {
                                    isGameDndSwitchOn = NotificationManagerService.this.isGameDndSwitchOn();
                                    notification.extras.putBoolean("gameDndSwitchOn", isGameDndSwitchOn);
                                }
                                Log.d(NotificationManagerService.TAG, "mGameDndStatus is:" + NotificationManagerService.this.mGameDndStatus + " ,isGameDndSwitchOn is:" + isGameDndSwitchOn);
                                notification.extras.putBoolean("gameDndOn", NotificationManagerService.this.mGameDndStatus);
                            } catch (ConcurrentModificationException e) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("notification.extras:");
                                sb.append(e.toString());
                                Log.e(NotificationManagerService.TAG, sb.toString());
                            } catch (Throwable th) {
                                th = th;
                                i = 0;
                                N = NotificationManagerService.this.mEnqueuedNotifications.size();
                                while (true) {
                                    i2 = i;
                                    if (i2 < N) {
                                    }
                                    i = i2 + 1;
                                }
                                NotificationManagerService.this.removeNotificationInUpdateQueue(this.key);
                                throw th;
                            }
                        }
                        try {
                            NotificationManagerService.this.mListeners.notifyPostedLocked(r2, old2);
                            if (oldSbn != null) {
                            }
                            NotificationManagerService.this.mHandler.post(new Runnable() {
                                public void run() {
                                    NotificationManagerService.this.mGroupHelper.onNotificationPosted(n, NotificationManagerService.this.hasAutoGroupSummaryLocked(n), notifyType);
                                }
                            });
                            if (oldSbn == null) {
                                LogPower.push(122, n.getPackageName(), Integer.toString(n.getId()), n.getOpPkg(), new String[]{Integer.toString(r2.getFlags())});
                                NotificationManagerService.this.reportToIAware(n.getPackageName(), n.getUid(), n.getId(), true);
                                r2.setPushLogPowerTimeMs(System.currentTimeMillis());
                            } else {
                                long now = System.currentTimeMillis();
                                if (old2.getPushLogPowerTimeMs(now) <= NotificationManagerService.NOTIFICATION_UPDATE_REPORT_INTERVAL) {
                                    if (!(oldSbn.getNotification() == null || oldSbn.getNotification().flags == r2.getFlags())) {
                                    }
                                }
                                String packageName = n.getPackageName();
                                String num = Integer.toString(n.getId());
                                String opPkg = n.getOpPkg();
                                int i5 = N2;
                                String[] strArr = new String[1];
                                i = 0;
                                strArr[0] = Integer.toString(r2.getFlags());
                                LogPower.push(HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_MINUS, packageName, num, opPkg, strArr);
                                r2.setPushLogPowerTimeMs(now);
                            }
                            int i6 = N2;
                            i = 0;
                        } catch (IllegalArgumentException e2) {
                            int i7 = N2;
                            int i8 = 0;
                            Log.e(NotificationManagerService.TAG, "notification:" + n + "extras:" + n.getNotification().extras);
                            int N4 = NotificationManagerService.this.mEnqueuedNotifications.size();
                            while (true) {
                                int i9 = i8;
                                if (i9 >= N4) {
                                    break;
                                } else if (Objects.equals(this.key, NotificationManagerService.this.mEnqueuedNotifications.get(i9).getKey())) {
                                    NotificationManagerService.this.mEnqueuedNotifications.remove(i9);
                                    break;
                                } else {
                                    i8 = i9 + 1;
                                }
                            }
                            NotificationManagerService.this.removeNotificationInUpdateQueue(this.key);
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            N = NotificationManagerService.this.mEnqueuedNotifications.size();
                            while (true) {
                                i2 = i;
                                if (i2 < N) {
                                }
                                i = i2 + 1;
                            }
                            NotificationManagerService.this.removeNotificationInUpdateQueue(this.key);
                            throw th;
                        }
                    } else {
                        i = 0;
                        int i10 = N2;
                        Slog.e(NotificationManagerService.TAG, "Not posting notification without small icon: " + notification);
                        if (old2 != null && !old2.isCanceled) {
                            NotificationManagerService.this.mListeners.notifyRemovedLocked(r2, 4, null);
                            NotificationManagerService.this.mHandler.post(new Runnable() {
                                public void run() {
                                    NotificationManagerService.this.mGroupHelper.onNotificationRemoved(n, notifyType);
                                }
                            });
                        }
                        Slog.e(NotificationManagerService.TAG, "WARNING: In a future release this will crash the app: " + n.getPackageName());
                    }
                    if (!r2.isHidden()) {
                        NotificationManagerService.this.buzzBeepBlinkLocked(r2);
                    }
                    NotificationManagerService.this.maybeRecordInterruptionLocked(r2);
                    int N5 = NotificationManagerService.this.mEnqueuedNotifications.size();
                    while (true) {
                        int i11 = i;
                        if (i11 >= N5) {
                            break;
                        } else if (Objects.equals(this.key, NotificationManagerService.this.mEnqueuedNotifications.get(i11).getKey())) {
                            NotificationManagerService.this.mEnqueuedNotifications.remove(i11);
                            break;
                        } else {
                            i = i11 + 1;
                        }
                    }
                    NotificationManagerService.this.removeNotificationInUpdateQueue(this.key);
                } catch (ConcurrentModificationException e3) {
                    Log.e(NotificationManagerService.TAG, "ConcurrentModificationException is happen, notification.extras.putBoolean:" + e3.toString());
                } catch (Throwable th3) {
                    th = th3;
                    i = 0;
                    N = NotificationManagerService.this.mEnqueuedNotifications.size();
                    while (true) {
                        i2 = i;
                        if (i2 < N) {
                            if (Objects.equals(this.key, NotificationManagerService.this.mEnqueuedNotifications.get(i2).getKey())) {
                                NotificationManagerService.this.mEnqueuedNotifications.remove(i2);
                                break;
                            }
                            i = i2 + 1;
                        } else {
                            break;
                        }
                    }
                    NotificationManagerService.this.removeNotificationInUpdateQueue(this.key);
                    throw th;
                }
            }
        }
    }

    private final class PowerSaverObserver extends ContentObserver {
        private final Uri SUPER_POWER_SAVE_NOTIFICATION_URI = Settings.Secure.getUriFor(NotificationManagerService.POWER_SAVER_NOTIFICATION_WHITELIST);
        private boolean initObserver = false;

        PowerSaverObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            if (!this.initObserver) {
                this.initObserver = true;
                NotificationManagerService.this.getContext().getContentResolver().registerContentObserver(this.SUPER_POWER_SAVE_NOTIFICATION_URI, false, this, -1);
                update(null);
            }
        }

        /* access modifiers changed from: package-private */
        public void unObserve() {
            this.initObserver = false;
            NotificationManagerService.this.getContext().getContentResolver().unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            if (uri == null || this.SUPER_POWER_SAVE_NOTIFICATION_URI.equals(uri)) {
                NotificationManagerService.this.setNotificationWhiteList();
            }
        }
    }

    private final class RankingHandlerWorker extends Handler implements RankingHandler {
        public RankingHandlerWorker(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    NotificationManagerService.this.handleRankingReconsideration(msg);
                    return;
                case 1001:
                    NotificationManagerService.this.handleRankingSort();
                    return;
                default:
                    return;
            }
        }

        public void requestSort() {
            removeMessages(1001);
            Message msg = Message.obtain();
            msg.what = 1001;
            sendMessage(msg);
        }

        public void requestReconsideration(RankingReconsideration recon) {
            sendMessageDelayed(Message.obtain(this, 1000, recon), recon.getDelay(TimeUnit.MILLISECONDS));
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri NOTIFICATION_BADGING_URI = Settings.Secure.getUriFor("notification_badging");
        private final Uri NOTIFICATION_LIGHT_PULSE_URI = Settings.System.getUriFor("notification_light_pulse");
        private final Uri NOTIFICATION_RATE_LIMIT_URI = Settings.Global.getUriFor("max_notification_enqueue_rate");

        SettingsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            ContentResolver resolver = NotificationManagerService.this.getContext().getContentResolver();
            resolver.registerContentObserver(this.NOTIFICATION_BADGING_URI, false, this, -1);
            resolver.registerContentObserver(this.NOTIFICATION_LIGHT_PULSE_URI, false, this, -1);
            resolver.registerContentObserver(this.NOTIFICATION_RATE_LIMIT_URI, false, this, -1);
            update(null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            ContentResolver resolver = NotificationManagerService.this.getContext().getContentResolver();
            if (uri == null || this.NOTIFICATION_LIGHT_PULSE_URI.equals(uri)) {
                boolean z = false;
                if (Settings.System.getIntForUser(resolver, "notification_light_pulse", 0, -2) != 0) {
                    z = true;
                }
                boolean pulseEnabled = z;
                if (NotificationManagerService.this.mNotificationPulseEnabled != pulseEnabled) {
                    boolean unused = NotificationManagerService.this.mNotificationPulseEnabled = pulseEnabled;
                    NotificationManagerService.this.updateNotificationPulse();
                }
            }
            if (uri == null || this.NOTIFICATION_RATE_LIMIT_URI.equals(uri)) {
                float unused2 = NotificationManagerService.this.mMaxPackageEnqueueRate = Settings.Global.getFloat(resolver, "max_notification_enqueue_rate", NotificationManagerService.this.mMaxPackageEnqueueRate);
            }
            if (uri == null || this.NOTIFICATION_BADGING_URI.equals(uri)) {
                NotificationManagerService.this.mRankingHelper.updateBadgingEnabled();
            }
        }
    }

    private class ShellCmd extends ShellCommand {
        public static final String USAGE = "help\nallow_listener COMPONENT [user_id]\ndisallow_listener COMPONENT [user_id]\nallow_assistant COMPONENT\nremove_assistant COMPONENT\nallow_dnd PACKAGE\ndisallow_dnd PACKAGE\nsuspend_package PACKAGE\nunsuspend_package PACKAGE";

        private ShellCmd() {
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x0068, code lost:
            r2 = 65535;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x0069, code lost:
            switch(r2) {
                case 0: goto L_0x0128;
                case 1: goto L_0x011a;
                case 2: goto L_0x00ec;
                case 3: goto L_0x00be;
                case 4: goto L_0x00a3;
                case 5: goto L_0x0088;
                case 6: goto L_0x007d;
                case 7: goto L_0x0072;
                default: goto L_0x006c;
            };
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x0072, code lost:
            r7.this$0.simulatePackageSuspendBroadcast(false, getNextArgRequired());
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x007d, code lost:
            r7.this$0.simulatePackageSuspendBroadcast(true, getNextArgRequired());
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x0088, code lost:
            r2 = android.content.ComponentName.unflattenFromString(getNextArgRequired());
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x0090, code lost:
            if (r2 != null) goto L_0x0098;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x0092, code lost:
            r0.println("Invalid assistant - must be a ComponentName");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x0097, code lost:
            return -1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:0x0098, code lost:
            r7.this$0.getBinderService().setNotificationAssistantAccessGranted(r2, false);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x00a3, code lost:
            r2 = android.content.ComponentName.unflattenFromString(getNextArgRequired());
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x00ab, code lost:
            if (r2 != null) goto L_0x00b3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:0x00ad, code lost:
            r0.println("Invalid assistant - must be a ComponentName");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:45:0x00b2, code lost:
            return -1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:0x00b3, code lost:
            r7.this$0.getBinderService().setNotificationAssistantAccessGranted(r2, true);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:47:0x00be, code lost:
            r2 = android.content.ComponentName.unflattenFromString(getNextArgRequired());
         */
        /* JADX WARNING: Code restructure failed: missing block: B:48:0x00c6, code lost:
            if (r2 != null) goto L_0x00ce;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:49:0x00c8, code lost:
            r0.println("Invalid listener - must be a ComponentName");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:50:0x00cd, code lost:
            return -1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:51:0x00ce, code lost:
            r3 = getNextArg();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:52:0x00d2, code lost:
            if (r3 != null) goto L_0x00de;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:53:0x00d4, code lost:
            r7.this$0.getBinderService().setNotificationListenerAccessGranted(r2, false);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:54:0x00de, code lost:
            r7.this$0.getBinderService().setNotificationListenerAccessGrantedForUser(r2, java.lang.Integer.parseInt(r3), false);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:56:0x00ec, code lost:
            r2 = android.content.ComponentName.unflattenFromString(getNextArgRequired());
         */
        /* JADX WARNING: Code restructure failed: missing block: B:57:0x00f4, code lost:
            if (r2 != null) goto L_0x00fc;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:58:0x00f6, code lost:
            r0.println("Invalid listener - must be a ComponentName");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:59:0x00fb, code lost:
            return -1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:60:0x00fc, code lost:
            r3 = getNextArg();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:61:0x0100, code lost:
            if (r3 != null) goto L_0x010c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:62:0x0102, code lost:
            r7.this$0.getBinderService().setNotificationListenerAccessGranted(r2, true);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:63:0x010c, code lost:
            r7.this$0.getBinderService().setNotificationListenerAccessGrantedForUser(r2, java.lang.Integer.parseInt(r3), true);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:65:0x011a, code lost:
            r7.this$0.getBinderService().setNotificationPolicyAccessGranted(getNextArgRequired(), false);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:66:0x0128, code lost:
            r7.this$0.getBinderService().setNotificationPolicyAccessGranted(getNextArgRequired(), true);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:67:0x0137, code lost:
            return handleDefaultCommands(r8);
         */
        public int onCommand(String cmd) {
            char c;
            if (cmd == null) {
                return handleDefaultCommands(cmd);
            }
            PrintWriter pw = getOutPrintWriter();
            try {
                switch (cmd.hashCode()) {
                    case -1325770982:
                        if (cmd.equals("disallow_assistant")) {
                            c = 5;
                            break;
                        }
                    case -506770550:
                        if (cmd.equals("unsuspend_package")) {
                            c = 7;
                            break;
                        }
                    case -432999190:
                        if (cmd.equals("allow_listener")) {
                            c = 2;
                            break;
                        }
                    case -429832618:
                        if (cmd.equals("disallow_dnd")) {
                            c = 1;
                            break;
                        }
                    case 372345636:
                        if (cmd.equals("allow_dnd")) {
                            c = 0;
                            break;
                        }
                    case 393969475:
                        if (cmd.equals("suspend_package")) {
                            c = 6;
                            break;
                        }
                    case 1257269496:
                        if (cmd.equals("disallow_listener")) {
                            c = 3;
                            break;
                        }
                    case 2110474600:
                        if (cmd.equals("allow_assistant")) {
                            c = 4;
                            break;
                        }
                }
            } catch (Exception e) {
                pw.println("Error occurred. Check logcat for details. " + e.getMessage());
                Slog.e(NotificationManagerService.TAG, "Error running shell command", e);
            }
            return 0;
        }

        public void onHelp() {
            getOutPrintWriter().println(USAGE);
        }
    }

    protected class SnoozeNotificationRunnable implements Runnable {
        private final long mDuration;
        private final String mKey;
        private final String mSnoozeCriterionId;

        SnoozeNotificationRunnable(String key, long duration, String snoozeCriterionId) {
            this.mKey = key;
            this.mDuration = duration;
            this.mSnoozeCriterionId = snoozeCriterionId;
        }

        public void run() {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = NotificationManagerService.this.findNotificationByKeyLocked(this.mKey);
                if (r != null) {
                    snoozeLocked(r);
                }
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy("mNotificationLock")
        public void snoozeLocked(NotificationRecord r) {
            if (r.sbn.isGroup()) {
                List<NotificationRecord> groupNotifications = NotificationManagerService.this.findGroupNotificationsLocked(r.sbn.getPackageName(), r.sbn.getGroupKey(), r.sbn.getUserId());
                int i = 0;
                if (r.getNotification().isGroupSummary()) {
                    while (true) {
                        int i2 = i;
                        if (i2 < groupNotifications.size()) {
                            snoozeNotificationLocked(groupNotifications.get(i2));
                            i = i2 + 1;
                        } else {
                            return;
                        }
                    }
                } else if (!NotificationManagerService.this.mSummaryByGroupKey.containsKey(r.sbn.getGroupKey())) {
                    snoozeNotificationLocked(r);
                } else if (groupNotifications.size() != 2) {
                    snoozeNotificationLocked(r);
                } else {
                    while (true) {
                        int i3 = i;
                        if (i3 < groupNotifications.size()) {
                            snoozeNotificationLocked(groupNotifications.get(i3));
                            i = i3 + 1;
                        } else {
                            return;
                        }
                    }
                }
            } else {
                snoozeNotificationLocked(r);
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy("mNotificationLock")
        public void snoozeNotificationLocked(NotificationRecord r) {
            MetricsLogger.action(r.getLogMaker().setCategory(831).setType(2).addTaggedData(1139, Long.valueOf(this.mDuration)).addTaggedData(832, Integer.valueOf(this.mSnoozeCriterionId == null ? 0 : 1)));
            NotificationManagerService.this.cancelNotificationLocked(r, false, 18, NotificationManagerService.this.removeFromNotificationListsLocked(r), null);
            NotificationManagerService.this.updateLightsLocked();
            if (this.mSnoozeCriterionId != null) {
                NotificationManagerService.this.mAssistants.notifyAssistantSnoozedLocked(r.sbn, this.mSnoozeCriterionId);
                NotificationManagerService.this.mSnoozeHelper.snooze(r);
            } else {
                NotificationManagerService.this.mSnoozeHelper.snooze(r, this.mDuration);
            }
            r.recordSnoozed();
            NotificationManagerService.this.savePolicyFile();
        }
    }

    private static final class StatusBarNotificationHolder extends IStatusBarNotificationHolder.Stub {
        private StatusBarNotification mValue;

        public StatusBarNotificationHolder(StatusBarNotification value) {
            this.mValue = value;
        }

        public StatusBarNotification get() {
            StatusBarNotification value = this.mValue;
            this.mValue = null;
            return value;
        }
    }

    private static class ToastRecord {
        ITransientNotification callback;
        int duration;
        final int pid;
        final String pkg;
        Binder token;

        ToastRecord(int pid2, String pkg2, ITransientNotification callback2, int duration2, Binder token2) {
            this.pid = pid2;
            this.pkg = pkg2;
            this.callback = callback2;
            this.duration = duration2;
            this.token = token2;
        }

        /* access modifiers changed from: package-private */
        public void update(int duration2) {
            this.duration = duration2;
        }

        /* access modifiers changed from: package-private */
        public void update(ITransientNotification callback2) {
            this.callback = callback2;
        }

        /* access modifiers changed from: package-private */
        public void dump(PrintWriter pw, String prefix, DumpFilter filter) {
            if (filter == null || filter.matches(this.pkg)) {
                pw.println(prefix + this);
            }
        }

        public final String toString() {
            return "ToastRecord{" + Integer.toHexString(System.identityHashCode(this)) + " pkg=" + this.pkg + " callback=" + this.callback + " duration=" + this.duration;
        }
    }

    private static class ToastRecordEx extends ToastRecord {
        int displayId;

        ToastRecordEx(int pid, String pkg, ITransientNotification callback, int duration, Binder token, int displayId2) {
            super(pid, pkg, callback, duration, token);
            this.displayId = displayId2;
        }
    }

    private class TrimCache {
        StatusBarNotification heavy;
        StatusBarNotification sbnClone;
        StatusBarNotification sbnCloneLight;

        TrimCache(StatusBarNotification sbn) {
            this.heavy = sbn;
        }

        /* access modifiers changed from: package-private */
        public StatusBarNotification ForListener(ManagedServices.ManagedServiceInfo info) {
            if (NotificationManagerService.this.mListeners.getOnNotificationPostedTrim(info) == 1) {
                if (this.sbnCloneLight == null) {
                    this.sbnCloneLight = this.heavy.cloneLight();
                }
                return this.sbnCloneLight;
            }
            if (this.sbnClone == null) {
                this.sbnClone = this.heavy.clone();
            }
            return this.sbnClone;
        }
    }

    protected class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    NotificationManagerService.this.handleDurationReached((ToastRecord) msg.obj);
                    return;
                case 3:
                    NotificationManagerService.this.handleSavePolicyFile();
                    return;
                case 4:
                    NotificationManagerService.this.handleSendRankingUpdate();
                    return;
                case 5:
                    NotificationManagerService.this.handleListenerHintsChanged(msg.arg1);
                    return;
                case 6:
                    NotificationManagerService.this.handleListenerInterruptionFilterChanged(msg.arg1);
                    return;
                case 7:
                    NotificationManagerService.this.handleKillTokenTimeout((IBinder) msg.obj);
                    return;
                default:
                    return;
            }
        }

        /* access modifiers changed from: protected */
        public void scheduleSendRankingUpdate() {
            if (!hasMessages(4)) {
                sendMessage(Message.obtain(this, 4));
            }
        }
    }

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
        EXPANDEDNTF_PKGS.add("com.android.incallui");
        EXPANDEDNTF_PKGS.add("com.android.deskclock");
    }

    /* access modifiers changed from: protected */
    public void readDefaultApprovedServices(int userId) {
        String defaultListenerAccess = readDefaultApprovedFromWhiteList(getContext().getResources().getString(17039787));
        if (defaultListenerAccess != null) {
            for (String whitelisted : defaultListenerAccess.split(":")) {
                for (ComponentName cn : this.mListeners.queryPackageForServices(whitelisted, 786432, userId)) {
                    try {
                        getBinderService().setNotificationListenerAccessGrantedForUser(cn, userId, true);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        String defaultDndAccess = getContext().getResources().getString(17039786);
        if (defaultListenerAccess != null) {
            for (String whitelisted2 : defaultDndAccess.split(":")) {
                try {
                    getBinderService().setNotificationPolicyAccessGranted(whitelisted2, true);
                } catch (RemoteException e2) {
                    e2.printStackTrace();
                }
            }
        }
        readDefaultAssistant(userId);
    }

    /* access modifiers changed from: protected */
    public void readDefaultAssistant(int userId) {
        String defaultAssistantAccess = getContext().getResources().getString(17039782);
        if (defaultAssistantAccess != null) {
            for (ComponentName cn : this.mAssistants.queryPackageForServices(defaultAssistantAccess, 786432, userId)) {
                try {
                    getBinderService().setNotificationAssistantAccessGrantedForUser(cn, userId, true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readPolicyXml(InputStream stream, boolean forRestore) throws XmlPullParserException, NumberFormatException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(stream, StandardCharsets.UTF_8.name());
        XmlUtils.beginDocument(parser, TAG_NOTIFICATION_POLICY);
        boolean migratedManagedServices = false;
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if ("zen".equals(parser.getName())) {
                this.mZenModeHelper.readXml(parser, forRestore);
            } else if ("ranking".equals(parser.getName())) {
                this.mRankingHelper.readXml(parser, forRestore);
            }
            if (this.mListeners.getConfig().xmlTag.equals(parser.getName())) {
                this.mListeners.readXml(parser, this.mAllowedManagedServicePackages);
                migratedManagedServices = true;
            } else if (this.mAssistants.getConfig().xmlTag.equals(parser.getName())) {
                this.mAssistants.readXml(parser, this.mAllowedManagedServicePackages);
                migratedManagedServices = true;
            } else if (this.mConditionProviders.getConfig().xmlTag.equals(parser.getName())) {
                this.mConditionProviders.readXml(parser, this.mAllowedManagedServicePackages);
                migratedManagedServices = true;
            }
        }
        if (!migratedManagedServices) {
            this.mListeners.migrateToXml();
            this.mAssistants.migrateToXml();
            this.mConditionProviders.migrateToXml();
            savePolicyFile();
        }
        this.mAssistants.ensureAssistant();
    }

    private void loadPolicyFile() {
        if (DBG) {
            Slog.d(TAG, "loadPolicyFile");
        }
        synchronized (this.mPolicyFile) {
            InputStream infile = null;
            try {
                infile = this.mPolicyFile.openRead();
                readPolicyXml(infile, false);
            } catch (FileNotFoundException e) {
                readDefaultApprovedServices(0);
            } catch (IOException e2) {
                Log.wtf(TAG, "Unable to read notification policy", e2);
                IoUtils.closeQuietly(infile);
            } catch (NumberFormatException e3) {
                Log.wtf(TAG, "Unable to parse notification policy", e3);
                IoUtils.closeQuietly(infile);
            } catch (XmlPullParserException e4) {
                try {
                    Log.wtf(TAG, "Unable to parse notification policy", e4);
                    IoUtils.closeQuietly(infile);
                } catch (Throwable th) {
                    IoUtils.closeQuietly(infile);
                    throw th;
                }
            }
            IoUtils.closeQuietly(infile);
        }
        return;
    }

    public void savePolicyFile() {
        this.mHandler.removeMessages(3);
        this.mHandler.sendEmptyMessage(3);
    }

    /* access modifiers changed from: private */
    public void handleSavePolicyFile() {
        if (DBG) {
            Slog.d(TAG, "handleSavePolicyFile");
        }
        synchronized (this.mPolicyFile) {
            try {
                FileOutputStream stream = this.mPolicyFile.startWrite();
                try {
                    writePolicyXml(stream, false);
                    this.mPolicyFile.finishWrite(stream);
                } catch (IOException e) {
                    Slog.w(TAG, "Failed to save policy file, restoring backup", e);
                    this.mPolicyFile.failWrite(stream);
                } catch (ArrayIndexOutOfBoundsException e2) {
                    Slog.e(TAG, "handleSavePolicyFile has Exception : ArrayIndexOutOfBoundsException");
                } catch (NullPointerException e3) {
                    Slog.e(TAG, "writePolicyXml has Exception : NullPointerException");
                }
            } catch (IOException e4) {
                Slog.w(TAG, "Failed to save policy file", e4);
                return;
            }
        }
        BackupManager.dataChanged(getContext().getPackageName());
    }

    /* access modifiers changed from: private */
    public void writePolicyXml(OutputStream stream, boolean forBackup) throws IOException {
        XmlSerializer out = new FastXmlSerializer();
        out.setOutput(stream, StandardCharsets.UTF_8.name());
        out.startDocument(null, true);
        out.startTag(null, TAG_NOTIFICATION_POLICY);
        out.attribute(null, ATTR_VERSION, Integer.toString(1));
        this.mZenModeHelper.writeXml(out, forBackup, null);
        this.mRankingHelper.writeXml(out, forBackup);
        this.mListeners.writeXml(out, forBackup);
        this.mAssistants.writeXml(out, forBackup);
        this.mConditionProviders.writeXml(out, forBackup);
        out.endTag(null, TAG_NOTIFICATION_POLICY);
        out.endDocument();
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public void clearSoundLocked() {
        this.mSoundNotificationKey = null;
        long identity = Binder.clearCallingIdentity();
        try {
            IRingtonePlayer player = this.mAudioManager.getRingtonePlayer();
            if (player != null) {
                player.stopAsync();
            }
        } catch (RemoteException e) {
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public void clearVibrateLocked() {
        this.mVibrateNotificationKey = null;
        long identity = Binder.clearCallingIdentity();
        try {
            this.mVibrator.cancel();
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public void clearLightsLocked() {
        long token = Binder.clearCallingIdentity();
        try {
            int currentUser = ActivityManager.getCurrentUser();
            Binder.restoreCallingIdentity(token);
            int i = this.mLights.size();
            while (i > 0) {
                i--;
                String owner = this.mLights.get(i);
                NotificationRecord ledNotification = this.mNotificationsByKey.get(owner);
                Slog.d(TAG, "clearEffects :" + owner);
                if (ledNotification == null) {
                    Slog.wtfStack(TAG, "LED Notification does not exist: " + owner);
                    this.mLights.remove(owner);
                } else if (ledNotification.getUser().getIdentifier() == currentUser || ledNotification.getUser().getIdentifier() == -1 || isAFWUserId(ledNotification.getUser().getIdentifier())) {
                    Slog.d(TAG, "clearEffects CurrentUser AFWuser or AllUser :" + owner);
                    this.mLights.remove(owner);
                }
            }
            updateLightsLocked();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    static long[] getLongArray(Resources r, int resid, int maxlen, long[] def) {
        int[] ar = r.getIntArray(resid);
        if (ar == null) {
            return def;
        }
        int len = ar.length > maxlen ? maxlen : ar.length;
        long[] out = new long[len];
        for (int i = 0; i < len; i++) {
            out[i] = (long) ar[i];
        }
        return out;
    }

    /* JADX WARNING: type inference failed for: r0v12, types: [com.android.server.notification.NotificationManagerService$11, android.os.IBinder] */
    public NotificationManagerService(Context context) {
        super(context);
        Notification.processWhitelistToken = WHITELIST_TOKEN;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setAudioManager(AudioManager audioMananger) {
        this.mAudioManager = audioMananger;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setVibrator(Vibrator vibrator) {
        this.mVibrator = vibrator;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setLights(Light light) {
        this.mNotificationLight = light;
        this.mAttentionLight = light;
        this.mNotificationPulseEnabled = true;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setScreenOn(boolean on) {
        this.mScreenOn = on;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getNotificationRecordCount() {
        int count;
        synchronized (this.mNotificationLock) {
            count = this.mNotificationList.size() + this.mNotificationsByKey.size() + this.mSummaryByGroupKey.size() + this.mEnqueuedNotifications.size();
            Iterator<NotificationRecord> it = this.mNotificationList.iterator();
            while (it.hasNext()) {
                NotificationRecord posted = it.next();
                if (this.mNotificationsByKey.containsKey(posted.getKey())) {
                    count--;
                }
                if (posted.sbn.isGroup() && posted.getNotification().isGroupSummary()) {
                    count--;
                }
            }
        }
        return count;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void clearNotifications() {
        this.mEnqueuedNotifications.clear();
        this.mNotificationList.clear();
        this.mNotificationsByKey.clear();
        this.mSummaryByGroupKey.clear();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void addNotification(NotificationRecord r) {
        this.mNotificationList.add(r);
        this.mNotificationsByKey.put(r.sbn.getKey(), r);
        if (r.sbn.isGroup()) {
            this.mSummaryByGroupKey.put(r.getGroupKey(), r);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void addEnqueuedNotification(NotificationRecord r) {
        this.mEnqueuedNotifications.add(r);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public NotificationRecord getNotificationRecord(String key) {
        return this.mNotificationsByKey.get(key);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setSystemReady(boolean systemReady) {
        this.mSystemReady = systemReady;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setHandler(WorkerHandler handler) {
        this.mHandler = handler;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setFallbackVibrationPattern(long[] vibrationPattern) {
        this.mFallbackVibrationPattern = vibrationPattern;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setPackageManager(IPackageManager packageManager) {
        this.mPackageManager = packageManager;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setRankingHelper(RankingHelper rankingHelper) {
        this.mRankingHelper = rankingHelper;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setRankingHandler(RankingHandler rankingHandler) {
        this.mRankingHandler = rankingHandler;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setIsTelevision(boolean isTelevision) {
        this.mIsTelevision = isTelevision;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setUsageStats(NotificationUsageStats us) {
        this.mUsageStats = us;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setAccessibilityManager(AccessibilityManager am) {
        this.mAccessibilityManager = am;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void init(Looper looper, IPackageManager packageManager, PackageManager packageManagerClient, LightsManager lightsManager, NotificationListeners notificationListeners, NotificationAssistants notificationAssistants, ConditionProviders conditionProviders, ICompanionDeviceManager companionManager, SnoozeHelper snoozeHelper, NotificationUsageStats usageStats, AtomicFile policyFile, ActivityManager activityManager, GroupHelper groupHelper, IActivityManager am, UsageStatsManagerInternal appUsageStats, DevicePolicyManagerInternal dpm) {
        String[] extractorNames;
        LightsManager lightsManager2 = lightsManager;
        Resources resources = getContext().getResources();
        this.mMaxPackageEnqueueRate = Settings.Global.getFloat(getContext().getContentResolver(), "max_notification_enqueue_rate", 5.0f);
        this.mAccessibilityManager = (AccessibilityManager) getContext().getSystemService("accessibility");
        this.mAm = am;
        this.mPackageManager = packageManager;
        this.mPackageManagerClient = packageManagerClient;
        this.mAppOps = (AppOpsManager) getContext().getSystemService("appops");
        this.mVibrator = (Vibrator) getContext().getSystemService("vibrator");
        this.mAppUsageStats = appUsageStats;
        this.mAlarmManager = (AlarmManager) getContext().getSystemService("alarm");
        this.mCompanionManager = companionManager;
        this.mActivityManager = activityManager;
        this.mDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
        this.mDpm = dpm;
        this.mHandler = new WorkerHandler(looper);
        this.mRankingThread.start();
        try {
            extractorNames = resources.getStringArray(17236026);
        } catch (Resources.NotFoundException e) {
            Resources.NotFoundException notFoundException = e;
            extractorNames = new String[0];
        }
        String[] extractorNames2 = extractorNames;
        this.mUsageStats = usageStats;
        this.mMetricsLogger = new MetricsLogger();
        this.mRankingHandler = new RankingHandlerWorker(this.mRankingThread.getLooper());
        this.mConditionProviders = conditionProviders;
        this.mZenModeHelper = new ZenModeHelper(getContext(), this.mHandler.getLooper(), this.mConditionProviders);
        this.mZenModeHelper.addCallback(new ZenModeHelper.Callback() {
            public void onConfigChanged() {
                NotificationManagerService.this.savePolicyFile();
            }

            /* access modifiers changed from: package-private */
            public void onZenModeChanged() {
                NotificationManagerService.this.sendRegisteredOnlyBroadcast("android.app.action.INTERRUPTION_FILTER_CHANGED");
                NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.INTERRUPTION_FILTER_CHANGED_INTERNAL").addFlags(67108864), UserHandle.ALL, "android.permission.MANAGE_NOTIFICATIONS");
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.updateInterruptionFilterLocked();
                }
                NotificationManagerService.this.mRankingHandler.requestSort();
            }

            /* access modifiers changed from: package-private */
            public void onPolicyChanged() {
                NotificationManagerService.this.sendRegisteredOnlyBroadcast("android.app.action.NOTIFICATION_POLICY_CHANGED");
                NotificationManagerService.this.mRankingHandler.requestSort();
            }
        });
        Context context = getContext();
        PackageManager packageManager2 = this.mPackageManagerClient;
        Context context2 = context;
        PackageManager packageManager3 = packageManager2;
        RankingHelper rankingHelper = new RankingHelper(context2, packageManager3, this.mRankingHandler, this.mZenModeHelper, this.mUsageStats, extractorNames2);
        this.mRankingHelper = rankingHelper;
        this.mSnoozeHelper = snoozeHelper;
        this.mGroupHelper = groupHelper;
        this.mListeners = notificationListeners;
        this.mAssistants = notificationAssistants;
        this.mAllowedManagedServicePackages = new Predicate() {
            public final boolean test(Object obj) {
                return NotificationManagerService.this.canUseManagedServices((String) obj);
            }
        };
        this.mPolicyFile = policyFile;
        loadPolicyFile();
        this.mStatusBar = (StatusBarManagerInternal) getLocalService(StatusBarManagerInternal.class);
        if (this.mStatusBar != null) {
            this.mStatusBar.setNotificationDelegate(this.mNotificationDelegate);
        }
        this.mNotificationLight = lightsManager2.getLight(4);
        this.mAttentionLight = lightsManager2.getLight(5);
        this.mFallbackVibrationPattern = getLongArray(resources, 17236025, 17, DEFAULT_VIBRATE_PATTERN);
        this.mInCallNotificationUri = Uri.parse("file://" + resources.getString(17039823));
        this.mInCallNotificationAudioAttributes = new AudioAttributes.Builder().setContentType(4).setUsage(2).build();
        this.mInCallNotificationVolume = resources.getFloat(17104962);
        this.mUseAttentionLight = resources.getBoolean(17957057);
        boolean z = false;
        if (Settings.Global.getInt(getContext().getContentResolver(), "device_provisioned", 0) == 0) {
            this.mDisableNotificationEffects = true;
        }
        this.mZenModeHelper.initZenMode();
        this.mInterruptionFilter = this.mZenModeHelper.getZenModeListenerInterruptionFilter();
        this.mUserProfiles.updateCache(getContext());
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mArchive = new Archive(resources.getInteger(17694836));
        if (this.mPackageManagerClient.hasSystemFeature("android.software.leanback") || this.mPackageManagerClient.hasSystemFeature("android.hardware.type.television")) {
            z = true;
        }
        this.mIsTelevision = z;
    }

    public void onStart() {
        SnoozeHelper snoozeHelper = new SnoozeHelper(getContext(), new SnoozeHelper.Callback() {
            public void repost(int userId, NotificationRecord r) {
                try {
                    if (NotificationManagerService.DBG) {
                        Slog.d(NotificationManagerService.TAG, "Reposting " + r.getKey());
                    }
                    NotificationManagerService.this.enqueueNotificationInternal(r.sbn.getPackageName(), r.sbn.getOpPkg(), r.sbn.getUid(), r.sbn.getInitialPid(), r.sbn.getTag(), r.sbn.getId(), r.sbn.getNotification(), userId);
                } catch (Exception e) {
                    Slog.e(NotificationManagerService.TAG, "Cannot un-snooze notification", e);
                }
            }
        }, this.mUserProfiles);
        File systemDir = new File(Environment.getDataDirectory(), "system");
        NotificationListeners notificationListeners = new NotificationListeners(AppGlobals.getPackageManager());
        NotificationAssistants notificationAssistants = new NotificationAssistants(getContext(), this.mNotificationLock, this.mUserProfiles, AppGlobals.getPackageManager());
        ConditionProviders conditionProviders = new ConditionProviders(getContext(), this.mUserProfiles, AppGlobals.getPackageManager());
        NotificationUsageStats notificationUsageStats = new NotificationUsageStats(getContext());
        AtomicFile atomicFile = new AtomicFile(new File(systemDir, "notification_policy.xml"), TAG_NOTIFICATION_POLICY);
        File file = systemDir;
        ConditionProviders conditionProviders2 = conditionProviders;
        init(Looper.myLooper(), AppGlobals.getPackageManager(), getContext().getPackageManager(), (LightsManager) getLocalService(LightsManager.class), notificationListeners, notificationAssistants, conditionProviders2, null, snoozeHelper, notificationUsageStats, atomicFile, (ActivityManager) getContext().getSystemService("activity"), getGroupHelper(), ActivityManager.getService(), (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class), (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        filter.addAction("android.intent.action.USER_PRESENT");
        filter.addAction("android.intent.action.USER_STOPPED");
        filter.addAction("android.intent.action.USER_SWITCHED");
        filter.addAction("android.intent.action.USER_ADDED");
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.USER_UNLOCKED");
        filter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        getContext().registerReceiver(this.mIntentReceiver, filter);
        IntentFilter powerFilter = new IntentFilter();
        powerFilter.addAction(ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE);
        powerFilter.addAction(ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE);
        getContext().registerReceiver(this.powerReceiver, powerFilter, PERMISSION, null);
        IntentFilter pkgFilter = new IntentFilter();
        pkgFilter.addAction("android.intent.action.PACKAGE_ADDED");
        pkgFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        pkgFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        pkgFilter.addAction("android.intent.action.PACKAGE_RESTARTED");
        pkgFilter.addAction("android.intent.action.QUERY_PACKAGE_RESTART");
        pkgFilter.addDataScheme("package");
        getContext().registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.ALL, pkgFilter, null, null);
        IntentFilter suspendedPkgFilter = new IntentFilter();
        suspendedPkgFilter.addAction("android.intent.action.PACKAGES_SUSPENDED");
        suspendedPkgFilter.addAction("android.intent.action.PACKAGES_UNSUSPENDED");
        getContext().registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.ALL, suspendedPkgFilter, null, null);
        IntentFilter sdFilter = new IntentFilter("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
        getContext().registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.ALL, sdFilter, null, null);
        IntentFilter timeoutFilter = new IntentFilter(ACTION_NOTIFICATION_TIMEOUT);
        timeoutFilter.addDataScheme(SCHEME_TIMEOUT);
        getContext().registerReceiver(this.mNotificationTimeoutReceiver, timeoutFilter);
        getContext().registerReceiver(this.mRestoreReceiver, new IntentFilter("android.os.action.SETTING_RESTORED"));
        getContext().registerReceiver(this.mLocaleChangeReceiver, new IntentFilter("android.intent.action.LOCALE_CHANGED"));
        publishBinderService("notification", this.mService, false, 5);
        publishLocalService(NotificationManagerInternal.class, this.mInternalService);
    }

    private GroupHelper getGroupHelper() {
        return new GroupHelper(new GroupHelper.Callback() {
            public void addAutoGroup(String key) {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.addAutogroupKeyLocked(key);
                }
            }

            public void removeAutoGroup(String key) {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.removeAutogroupKeyLocked(key);
                }
            }

            public void addAutoGroupSummary(int userId, String pkg, String triggeringKey) {
                NotificationManagerService.this.createAutoGroupSummary(userId, pkg, triggeringKey);
            }

            public void removeAutoGroupSummary(int userId, String pkg) {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.clearAutogroupSummaryLocked(userId, pkg, 1);
                }
            }

            public void removeAutoGroupSummary(int userId, String pkg, int notifyType) {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.clearAutogroupSummaryLocked(userId, pkg, notifyType);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void sendRegisteredOnlyBroadcast(String action) {
        getContext().sendBroadcastAsUser(new Intent(action).addFlags(1073741824), UserHandle.ALL, null);
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mSystemReady = true;
            this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
            this.mAudioManagerInternal = (AudioManagerInternal) getLocalService(AudioManagerInternal.class);
            this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            this.mZenModeHelper.onSystemReady();
        } else if (phase == 600) {
            this.mSettingsObserver.observe();
            this.mListeners.onBootPhaseAppsCanStart();
            this.mAssistants.onBootPhaseAppsCanStart();
            this.mConditionProviders.onBootPhaseAppsCanStart();
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public void updateListenerHintsLocked() {
        int hints = calculateHints();
        if (hints != this.mListenerHints) {
            ZenLog.traceListenerHintsChanged(this.mListenerHints, hints, this.mEffectsSuppressors.size());
            this.mListenerHints = hints;
            scheduleListenerHintsChanged(hints);
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public void updateEffectsSuppressorLocked() {
        long updatedSuppressedEffects = calculateSuppressedEffects();
        if (updatedSuppressedEffects != this.mZenModeHelper.getSuppressedEffects()) {
            List<ComponentName> suppressors = getSuppressors();
            ZenLog.traceEffectsSuppressorChanged(this.mEffectsSuppressors, suppressors, updatedSuppressedEffects);
            this.mEffectsSuppressors = suppressors;
            this.mZenModeHelper.setSuppressedEffects(updatedSuppressedEffects);
            sendRegisteredOnlyBroadcast("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED");
        }
    }

    /* access modifiers changed from: private */
    public void exitIdle() {
        try {
            if (this.mDeviceIdleController != null) {
                this.mDeviceIdleController.exitIdle("notification interaction");
            }
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: private */
    public void updateNotificationChannelInt(String pkg, int uid, NotificationChannel channel, boolean fromListener) {
        String str = pkg;
        int i = uid;
        NotificationChannel notificationChannel = channel;
        if (channel.getImportance() == 0) {
            cancelAllNotificationsInt(MY_UID, MY_PID, str, channel.getId(), 0, 0, true, UserHandle.getUserId(uid), 17, null);
            if (isUidSystemOrPhone(i)) {
                int[] profileIds = this.mUserProfiles.getCurrentProfileIds();
                int N = profileIds.length;
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    if (i3 >= N) {
                        break;
                    }
                    cancelAllNotificationsInt(MY_UID, MY_PID, str, channel.getId(), 0, 0, true, profileIds[i3], 17, null);
                    i2 = i3 + 1;
                    profileIds = profileIds;
                    N = N;
                }
            }
        }
        NotificationChannel preUpdate = this.mRankingHelper.getNotificationChannel(str, i, channel.getId(), true);
        this.mRankingHelper.updateNotificationChannel(str, i, notificationChannel, true);
        maybeNotifyChannelOwner(str, i, preUpdate, notificationChannel);
        if (!fromListener) {
            this.mListeners.notifyNotificationChannelChanged(str, UserHandle.getUserHandleForUid(uid), this.mRankingHelper.getNotificationChannel(str, i, channel.getId(), false), 2);
        }
        savePolicyFile();
    }

    private void maybeNotifyChannelOwner(String pkg, int uid, NotificationChannel preUpdate, NotificationChannel update) {
        try {
            if ((preUpdate.getImportance() == 0 && update.getImportance() != 0) || (preUpdate.getImportance() != 0 && update.getImportance() == 0)) {
                getContext().sendBroadcastAsUser(new Intent("android.app.action.NOTIFICATION_CHANNEL_BLOCK_STATE_CHANGED").putExtra("android.app.extra.NOTIFICATION_CHANNEL_ID", update.getId()).putExtra("android.app.extra.BLOCKED_STATE", update.getImportance() == 0).addFlags(268435456).setPackage(pkg), UserHandle.of(UserHandle.getUserId(uid)), null);
            }
        } catch (SecurityException e) {
            Slog.w(TAG, "Can't notify app about channel change", e);
        }
    }

    /* access modifiers changed from: private */
    public void createNotificationChannelGroup(String pkg, int uid, NotificationChannelGroup group, boolean fromApp, boolean fromListener) {
        Preconditions.checkNotNull(group);
        Preconditions.checkNotNull(pkg);
        NotificationChannelGroup preUpdate = this.mRankingHelper.getNotificationChannelGroup(group.getId(), pkg, uid);
        this.mRankingHelper.createNotificationChannelGroup(pkg, uid, group, fromApp);
        if (!fromApp) {
            maybeNotifyChannelGroupOwner(pkg, uid, preUpdate, group);
        }
        if (!fromListener) {
            this.mListeners.notifyNotificationChannelGroupChanged(pkg, UserHandle.of(UserHandle.getCallingUserId()), group, 1);
        }
    }

    private void maybeNotifyChannelGroupOwner(String pkg, int uid, NotificationChannelGroup preUpdate, NotificationChannelGroup update) {
        try {
            if (preUpdate.isBlocked() != update.isBlocked()) {
                getContext().sendBroadcastAsUser(new Intent("android.app.action.NOTIFICATION_CHANNEL_GROUP_BLOCK_STATE_CHANGED").putExtra("android.app.extra.NOTIFICATION_CHANNEL_GROUP_ID", update.getId()).putExtra("android.app.extra.BLOCKED_STATE", update.isBlocked()).addFlags(268435456).setPackage(pkg), UserHandle.of(UserHandle.getUserId(uid)), null);
            }
        } catch (SecurityException e) {
            Slog.w(TAG, "Can't notify app about group change", e);
        }
    }

    private ArrayList<ComponentName> getSuppressors() {
        ArrayList<ComponentName> names = new ArrayList<>();
        for (int i = this.mListenersDisablingEffects.size() - 1; i >= 0; i--) {
            Iterator<ManagedServices.ManagedServiceInfo> it = this.mListenersDisablingEffects.valueAt(i).iterator();
            while (it.hasNext()) {
                names.add(it.next().component);
            }
        }
        return names;
    }

    /* access modifiers changed from: private */
    public boolean removeDisabledHints(ManagedServices.ManagedServiceInfo info) {
        return removeDisabledHints(info, 0);
    }

    /* access modifiers changed from: private */
    public boolean removeDisabledHints(ManagedServices.ManagedServiceInfo info, int hints) {
        boolean removed = false;
        for (int i = this.mListenersDisablingEffects.size() - 1; i >= 0; i--) {
            int hint = this.mListenersDisablingEffects.keyAt(i);
            ArraySet<ManagedServices.ManagedServiceInfo> listeners = this.mListenersDisablingEffects.valueAt(i);
            if (hints == 0 || (hint & hints) == hint) {
                removed = removed || listeners.remove(info);
            }
        }
        return removed;
    }

    /* access modifiers changed from: private */
    public void addDisabledHints(ManagedServices.ManagedServiceInfo info, int hints) {
        if ((hints & 1) != 0) {
            addDisabledHint(info, 1);
        }
        if ((hints & 2) != 0) {
            addDisabledHint(info, 2);
        }
        if ((hints & 4) != 0) {
            addDisabledHint(info, 4);
        }
    }

    private void addDisabledHint(ManagedServices.ManagedServiceInfo info, int hint) {
        if (this.mListenersDisablingEffects.indexOfKey(hint) < 0) {
            this.mListenersDisablingEffects.put(hint, new ArraySet());
        }
        this.mListenersDisablingEffects.get(hint).add(info);
    }

    private int calculateHints() {
        int hints = 0;
        for (int i = this.mListenersDisablingEffects.size() - 1; i >= 0; i--) {
            int hint = this.mListenersDisablingEffects.keyAt(i);
            if (!this.mListenersDisablingEffects.valueAt(i).isEmpty()) {
                hints |= hint;
            }
        }
        return hints;
    }

    private long calculateSuppressedEffects() {
        int hints = calculateHints();
        long suppressedEffects = 0;
        if ((hints & 1) != 0) {
            suppressedEffects = 0 | 3;
        }
        if ((hints & 2) != 0) {
            suppressedEffects |= 1;
        }
        if ((hints & 4) != 0) {
            return suppressedEffects | 2;
        }
        return suppressedEffects;
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public void updateInterruptionFilterLocked() {
        int interruptionFilter = this.mZenModeHelper.getZenModeListenerInterruptionFilter();
        if (interruptionFilter != this.mInterruptionFilter) {
            this.mInterruptionFilter = interruptionFilter;
            scheduleInterruptionFilterChanged(interruptionFilter);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public INotificationManager getBinderService() {
        return INotificationManager.Stub.asInterface(this.mService);
    }

    /* access modifiers changed from: protected */
    @GuardedBy("mNotificationLock")
    public void reportSeen(NotificationRecord r) {
        this.mAppUsageStats.reportEvent(r.sbn.getPackageName(), getRealUserId(r.sbn.getUserId()), 10);
    }

    /* access modifiers changed from: protected */
    public int calculateSuppressedVisualEffects(NotificationManager.Policy incomingPolicy, NotificationManager.Policy currPolicy, int targetSdkVersion) {
        int newSuppressedVisualEffects;
        if (incomingPolicy.suppressedVisualEffects == -1) {
            return incomingPolicy.suppressedVisualEffects;
        }
        int[] effectsIntroducedInP = {4, 8, 16, 32, 64, 128, 256};
        int newSuppressedVisualEffects2 = incomingPolicy.suppressedVisualEffects;
        int i = 0;
        if (targetSdkVersion < 28) {
            while (true) {
                int i2 = i;
                if (i2 >= effectsIntroducedInP.length) {
                    break;
                }
                newSuppressedVisualEffects2 = (newSuppressedVisualEffects2 & (~effectsIntroducedInP[i2])) | (currPolicy.suppressedVisualEffects & effectsIntroducedInP[i2]);
                i = i2 + 1;
            }
            if ((newSuppressedVisualEffects2 & 1) != 0) {
                newSuppressedVisualEffects2 = newSuppressedVisualEffects2 | 8 | 4;
            }
            if ((newSuppressedVisualEffects & 2) != 0) {
                newSuppressedVisualEffects |= 16;
            }
        } else {
            if ((newSuppressedVisualEffects2 - 2) - 1 > 0) {
                i = 1;
            }
            if (i != 0) {
                newSuppressedVisualEffects = newSuppressedVisualEffects2 & -4;
                if ((newSuppressedVisualEffects & 16) != 0) {
                    newSuppressedVisualEffects |= 2;
                }
                if (!((newSuppressedVisualEffects & 8) == 0 || (newSuppressedVisualEffects & 4) == 0 || (newSuppressedVisualEffects & 128) == 0)) {
                    newSuppressedVisualEffects |= 1;
                }
            } else {
                if ((newSuppressedVisualEffects2 & 1) != 0) {
                    newSuppressedVisualEffects2 = newSuppressedVisualEffects2 | 8 | 4 | 128;
                }
                if ((newSuppressedVisualEffects & 2) != 0) {
                    newSuppressedVisualEffects |= 16;
                }
            }
        }
        return newSuppressedVisualEffects;
    }

    /* access modifiers changed from: protected */
    @GuardedBy("mNotificationLock")
    public void maybeRecordInterruptionLocked(NotificationRecord r) {
        if (r.isInterruptive() && !r.hasRecordedInterruption()) {
            this.mAppUsageStats.reportInterruptiveNotification(r.sbn.getPackageName(), r.getChannel().getId(), getRealUserId(r.sbn.getUserId()));
            logRecentLocked(r);
            r.setRecordedInterruption(true);
        }
    }

    /* access modifiers changed from: protected */
    public void reportUserInteraction(NotificationRecord r) {
        this.mAppUsageStats.reportEvent(r.sbn.getPackageName(), getRealUserId(r.sbn.getUserId()), 7);
    }

    private int getRealUserId(int userId) {
        if (userId == -1) {
            return 0;
        }
        return userId;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public NotificationManagerInternal getInternalService() {
        return this.mInternalService;
    }

    /* access modifiers changed from: private */
    public void applyAdjustment(NotificationRecord r, Adjustment adjustment) {
        if (!(r == null || adjustment.getSignals() == null)) {
            Bundle.setDefusable(adjustment.getSignals(), true);
            r.addAdjustment(adjustment);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mNotificationLock")
    public void addAutogroupKeyLocked(String key) {
        NotificationRecord r = this.mNotificationsByKey.get(key);
        if (r != null) {
            String groupKey = this.mGroupHelper.getAutoGroupKey(calculateNotifyType(r), r.sbn.getId());
            if (r.sbn.getOverrideGroupKey() == null) {
                addAutoGroupAdjustment(r, groupKey);
                EventLogTags.writeNotificationAutogrouped(key);
                this.mRankingHandler.requestSort();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mNotificationLock")
    public void removeAutogroupKeyLocked(String key) {
        NotificationRecord r = this.mNotificationsByKey.get(key);
        if (!(r == null || r.sbn.getOverrideGroupKey() == null)) {
            addAutoGroupAdjustment(r, null);
            EventLogTags.writeNotificationUnautogrouped(key);
            this.mRankingHandler.requestSort();
        }
    }

    private void addAutoGroupAdjustment(NotificationRecord r, String overrideGroupKey) {
        Bundle signals = new Bundle();
        signals.putString("key_group_key", overrideGroupKey);
        Adjustment adjustment = new Adjustment(r.sbn.getPackageName(), r.getKey(), signals, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, r.sbn.getUserId());
        r.addAdjustment(adjustment);
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public void clearAutogroupSummaryLocked(int userId, String pkg, int notifyType) {
        ArrayMap<String, String> summaries = this.mAutobundledSummaries.get(Integer.valueOf(userId));
        String notifyKey = this.mGroupHelper.getUnGroupKey(pkg, notifyType);
        if (summaries != null && summaries.containsKey(notifyKey)) {
            NotificationRecord removed = findNotificationByKeyLocked(summaries.remove(notifyKey));
            if (removed != null) {
                cancelNotificationLocked(removed, false, 16, removeFromNotificationListsLocked(removed), null);
            }
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public boolean hasAutoGroupSummaryLocked(StatusBarNotification sbn) {
        ArrayMap<String, String> summaries = this.mAutobundledSummaries.get(Integer.valueOf(sbn.getUserId()));
        return summaries != null && summaries.containsKey(sbn.getPackageName());
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x01c3, code lost:
        if (r9 == null) goto L_0x01e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x01db, code lost:
        if (checkDisqualifyingFeatures(r11, MY_UID, r9.sbn.getId(), r9.sbn.getTag(), r9, true) == false) goto L_0x01e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x01dd, code lost:
        r8.mHandler.post(new com.android.server.notification.NotificationManagerService.EnqueueNotificationRunnable(r8, r11, r9));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x01e7, code lost:
        return;
     */
    public void createAutoGroupSummary(int userId, String pkg, String triggeringKey) {
        NotificationRecord summaryRecord;
        boolean isBtw;
        String str = pkg;
        synchronized (this.mNotificationLock) {
            try {
                try {
                    NotificationRecord notificationRecord = this.mNotificationsByKey.get(triggeringKey);
                    if (notificationRecord == null) {
                        try {
                        } catch (Throwable th) {
                            th = th;
                            int i = userId;
                            throw th;
                        }
                    } else {
                        int notifyType = calculateNotifyType(notificationRecord);
                        String notifyKey = this.mGroupHelper.getUnGroupKey(str, notifyType);
                        StatusBarNotification adjustedSbn = notificationRecord.sbn;
                        int userId2 = adjustedSbn.getUser().getIdentifier();
                        ArrayMap<String, String> summaries = this.mAutobundledSummaries.get(Integer.valueOf(userId2));
                        if (summaries == null) {
                            try {
                                summaries = new ArrayMap<>();
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                        try {
                            this.mAutobundledSummaries.put(Integer.valueOf(userId2), summaries);
                            if (notificationRecord.getNotification().getGroup() != null) {
                                if (notificationRecord.getNotification().getGroup().contains("ranker_group")) {
                                    return;
                                }
                            }
                            if (!summaries.containsKey(notifyKey)) {
                                ApplicationInfo appInfo = (ApplicationInfo) adjustedSbn.getNotification().extras.getParcelable("android.appInfo");
                                Bundle extras = new Bundle();
                                extras.putParcelable("android.appInfo", appInfo);
                                String channelId = notificationRecord.getChannel().getId();
                                String groupKey = this.mGroupHelper.getAutoGroupKey(notifyType, notificationRecord.sbn.getId());
                                try {
                                    int i2 = notifyType;
                                    Notification summaryNotification = new Notification.Builder(getContext(), channelId).setSmallIcon(adjustedSbn.getNotification().getSmallIcon()).setGroupSummary(true).setGroupAlertBehavior(2).setGroup(groupKey).setFlag(1024, true).setFlag(512, true).setColor(adjustedSbn.getNotification().color).setLocalOnly(true).build();
                                    summaryNotification.extras.putAll(extras);
                                    Intent appIntent = getContext().getPackageManager().getLaunchIntentForPackage(str);
                                    if (appIntent != null) {
                                        summaryNotification.contentIntent = PendingIntent.getActivityAsUser(getContext(), 0, appIntent, 0, null, UserHandle.of(userId2));
                                    }
                                    StatusBarNotification statusBarNotification = new StatusBarNotification(adjustedSbn.getPackageName(), adjustedSbn.getOpPkg(), HwBootFail.STAGE_BOOT_SUCCESS, groupKey, adjustedSbn.getUid(), adjustedSbn.getInitialPid(), summaryNotification, adjustedSbn.getUser(), groupKey, System.currentTimeMillis());
                                    StatusBarNotification summarySbn = statusBarNotification;
                                    Notification notification = summaryNotification;
                                    StatusBarNotification statusBarNotification2 = adjustedSbn;
                                    NotificationRecord summaryRecord2 = new NotificationRecord(getContext(), summarySbn, notificationRecord.getChannel());
                                    if (notificationRecord.getNotification().extras == null || !notificationRecord.getNotification().extras.containsKey("hw_btw")) {
                                        if (notificationRecord.getImportance() < 2) {
                                            summaryRecord2.setImportance(1, "for user");
                                        }
                                    } else {
                                        summaryRecord2.setImportance(1, "for user");
                                        Bundle summaryExtras = summaryRecord2.getNotification().extras;
                                        ApplicationInfo applicationInfo = appInfo;
                                        Flog.i(400, "Autogroup summary Notification is btw : " + isBtw);
                                        if (summaryExtras != null) {
                                            summaryExtras.putBoolean("hw_btw", isBtw);
                                        }
                                    }
                                    summaryRecord2.setIsAppImportanceLocked(notificationRecord.getIsAppImportanceLocked());
                                    summaries.put(notifyKey, summarySbn.getKey());
                                    summaryRecord = summaryRecord2;
                                } catch (Throwable th3) {
                                    th = th3;
                                    throw th;
                                }
                            } else {
                                summaryRecord = null;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            throw th;
                        }
                        try {
                        } catch (Throwable th5) {
                            th = th5;
                            NotificationRecord notificationRecord2 = summaryRecord;
                            throw th;
                        }
                    }
                } catch (Throwable th6) {
                    th = th6;
                    throw th;
                }
            } catch (Throwable th7) {
                th = th7;
                String str2 = triggeringKey;
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public String disableNotificationEffects(NotificationRecord record) {
        if (this.mDisableNotificationEffects) {
            return "booleanState";
        }
        if ((this.mListenerHints & 1) != 0) {
            return "listenerHints";
        }
        if (this.mCallState == 0 || this.mZenModeHelper.isCall(record)) {
            return null;
        }
        return "callState";
    }

    /* access modifiers changed from: private */
    public void dumpJson(PrintWriter pw, DumpFilter filter) {
        JSONObject dump = new JSONObject();
        try {
            dump.put("service", "Notification Manager");
            dump.put("bans", this.mRankingHelper.dumpBansJson(filter));
            dump.put("ranking", this.mRankingHelper.dumpJson(filter));
            dump.put("stats", this.mUsageStats.dumpJson(filter));
            dump.put("channels", this.mRankingHelper.dumpChannelsJson(filter));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pw.println(dump);
    }

    private boolean allowNotificationsInCall(String disableEffects, NotificationRecord record) {
        boolean z = false;
        if (record == null || record.sbn == null) {
            return false;
        }
        boolean isMmsEnabled = isMmsNotificationEnable(record.sbn.getPackageName());
        boolean isCallEnabled = "callState".equals(disableEffects) && "com.android.systemui".equals(record.sbn.getPackageName()) && "low_battery".equals(record.sbn.getTag());
        if (isMmsEnabled || isCallEnabled) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void dumpProto(FileDescriptor fd, DumpFilter filter) {
        DumpFilter dumpFilter = filter;
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        synchronized (this.mNotificationLock) {
            int N = this.mNotificationList.size();
            int i = 0;
            while (true) {
                int i2 = i;
                if (i2 >= N) {
                    break;
                }
                NotificationRecord nr = this.mNotificationList.get(i2);
                if (!dumpFilter.filtered || dumpFilter.matches(nr.sbn)) {
                    nr.dump(proto, 2246267895809L, dumpFilter.redact, 1);
                }
                i = i2 + 1;
            }
            int N2 = this.mEnqueuedNotifications.size();
            int i3 = 0;
            while (true) {
                int i4 = i3;
                if (i4 >= N2) {
                    break;
                }
                NotificationRecord nr2 = this.mEnqueuedNotifications.get(i4);
                if (!dumpFilter.filtered || dumpFilter.matches(nr2.sbn)) {
                    nr2.dump(proto, 2246267895809L, dumpFilter.redact, 0);
                }
                i3 = i4 + 1;
            }
            List<NotificationRecord> snoozed = this.mSnoozeHelper.getSnoozed();
            int N3 = snoozed.size();
            int i5 = 0;
            while (true) {
                int i6 = i5;
                if (i6 >= N3) {
                    break;
                }
                NotificationRecord nr3 = snoozed.get(i6);
                if (!dumpFilter.filtered || dumpFilter.matches(nr3.sbn)) {
                    nr3.dump(proto, 2246267895809L, dumpFilter.redact, 2);
                }
                i5 = i6 + 1;
            }
            long zenLog = proto.start(1146756268034L);
            this.mZenModeHelper.dump(proto);
            for (ComponentName suppressor : this.mEffectsSuppressors) {
                suppressor.writeToProto(proto, 2246267895812L);
            }
            proto.end(zenLog);
            long listenersToken = proto.start(1146756268035L);
            this.mListeners.dump(proto, dumpFilter);
            proto.end(listenersToken);
            proto.write(1120986464260L, this.mListenerHints);
            int i7 = 0;
            while (i7 < this.mListenersDisablingEffects.size()) {
                long effectsToken = proto.start(2246267895813L);
                List<NotificationRecord> snoozed2 = snoozed;
                proto.write(1120986464257L, this.mListenersDisablingEffects.keyAt(i7));
                ArraySet<ManagedServices.ManagedServiceInfo> listeners = this.mListenersDisablingEffects.valueAt(i7);
                int j = 0;
                while (j < listeners.size()) {
                    listeners.valueAt(i7).writeToProto(proto, 2246267895810L, null);
                    j++;
                    zenLog = zenLog;
                    FileDescriptor fileDescriptor = fd;
                }
                proto.end(effectsToken);
                i7++;
                snoozed = snoozed2;
                zenLog = zenLog;
                FileDescriptor fileDescriptor2 = fd;
            }
            List<NotificationRecord> list = snoozed;
            long assistantsToken = proto.start(1146756268038L);
            this.mAssistants.dump(proto, dumpFilter);
            proto.end(assistantsToken);
            long conditionsToken = proto.start(1146756268039L);
            this.mConditionProviders.dump(proto, dumpFilter);
            proto.end(conditionsToken);
            long rankingToken = proto.start(1146756268040L);
            this.mRankingHelper.dump(proto, dumpFilter);
            proto.end(rankingToken);
        }
        proto.flush();
    }

    /* access modifiers changed from: private */
    public void dumpNotificationRecords(PrintWriter pw, DumpFilter filter) {
        synchronized (this.mNotificationLock) {
            int N = this.mNotificationList.size();
            if (N > 0) {
                pw.println("  Notification List:");
                for (int i = 0; i < N; i++) {
                    NotificationRecord nr = this.mNotificationList.get(i);
                    if (!filter.filtered || filter.matches(nr.sbn)) {
                        nr.dump(pw, "    ", getContext(), filter.redact);
                    }
                }
                pw.println("  ");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpImpl(PrintWriter pw, DumpFilter filter) {
        pw.print("Current Notification Manager state");
        if (filter.filtered) {
            pw.print(" (filtered to ");
            pw.print(filter);
            pw.print(")");
        }
        pw.println(':');
        boolean zenOnly = filter.filtered && filter.zen;
        if (!zenOnly) {
            synchronized (this.mToastQueue) {
                int N = this.mToastQueue.size();
                if (N > 0) {
                    pw.println("  Toast Queue:");
                    for (int i = 0; i < N; i++) {
                        this.mToastQueue.get(i).dump(pw, "    ", filter);
                    }
                    pw.println("  ");
                }
            }
        }
        synchronized (this.mNotificationLock) {
            if (!zenOnly) {
                try {
                    if (!filter.normalPriority) {
                        dumpNotificationRecords(pw, filter);
                    }
                    if (!filter.filtered) {
                        int N2 = this.mLights.size();
                        if (N2 > 0) {
                            pw.println("  Lights List:");
                            for (int i2 = 0; i2 < N2; i2++) {
                                if (i2 == N2 - 1) {
                                    pw.print("  > ");
                                } else {
                                    pw.print("    ");
                                }
                                pw.println(this.mLights.get(i2));
                            }
                            pw.println("  ");
                        }
                        pw.println("  mUseAttentionLight=" + this.mUseAttentionLight);
                        pw.println("  mNotificationPulseEnabled=" + this.mNotificationPulseEnabled);
                        pw.println("  mSoundNotificationKey=" + this.mSoundNotificationKey);
                        pw.println("  mVibrateNotificationKey=" + this.mVibrateNotificationKey);
                        pw.println("  mDisableNotificationEffects=" + this.mDisableNotificationEffects);
                        pw.println("  mCallState=" + callStateToString(this.mCallState));
                        pw.println("  mSystemReady=" + this.mSystemReady);
                        pw.println("  mMaxPackageEnqueueRate=" + this.mMaxPackageEnqueueRate);
                    }
                    pw.println("  mArchive=" + this.mArchive.toString());
                    Iterator<StatusBarNotification> iter = this.mArchive.descendingIterator();
                    int j = 0;
                    while (true) {
                        if (!iter.hasNext()) {
                            break;
                        }
                        StatusBarNotification sbn = iter.next();
                        if (filter == null || filter.matches(sbn)) {
                            pw.println("    " + sbn);
                            j++;
                            if (j >= 5) {
                                if (iter.hasNext()) {
                                    pw.println("    ...");
                                }
                            }
                        }
                    }
                    if (!zenOnly) {
                        int N3 = this.mEnqueuedNotifications.size();
                        if (N3 > 0) {
                            pw.println("  Enqueued Notification List:");
                            for (int i3 = 0; i3 < N3; i3++) {
                                NotificationRecord nr = this.mEnqueuedNotifications.get(i3);
                                if (!filter.filtered || filter.matches(nr.sbn)) {
                                    nr.dump(pw, "    ", getContext(), filter.redact);
                                }
                            }
                            pw.println("  ");
                        }
                        this.mSnoozeHelper.dump(pw, filter);
                    }
                } finally {
                }
            }
            if (!zenOnly) {
                pw.println("\n  Ranking Config:");
                this.mRankingHelper.dump(pw, "    ", filter);
                pw.println("\n  Notification listeners:");
                this.mListeners.dump(pw, filter);
                pw.print("    mListenerHints: ");
                pw.println(this.mListenerHints);
                pw.print("    mListenersDisablingEffects: (");
                int N4 = this.mListenersDisablingEffects.size();
                for (int i4 = 0; i4 < N4; i4++) {
                    int hint = this.mListenersDisablingEffects.keyAt(i4);
                    if (i4 > 0) {
                        pw.print(';');
                    }
                    pw.print("hint[" + hint + "]:");
                    ArraySet<ManagedServices.ManagedServiceInfo> listeners = this.mListenersDisablingEffects.valueAt(i4);
                    int listenerSize = listeners.size();
                    for (int j2 = 0; j2 < listenerSize; j2++) {
                        if (i4 > 0) {
                            pw.print(',');
                        }
                        ManagedServices.ManagedServiceInfo listener = listeners.valueAt(i4);
                        if (listener != null) {
                            pw.print(listener.component);
                        }
                    }
                }
                pw.println(')');
                pw.println("\n  Notification assistant services:");
                this.mAssistants.dump(pw, filter);
            }
            if (!filter.filtered || zenOnly) {
                pw.println("\n  Zen Mode:");
                pw.print("    mInterruptionFilter=");
                pw.println(this.mInterruptionFilter);
                this.mZenModeHelper.dump(pw, "    ");
                pw.println("\n  Zen Log:");
                ZenLog.dump(pw, "    ");
            }
            pw.println("\n  Condition providers:");
            this.mConditionProviders.dump(pw, filter);
            pw.println("\n  Group summaries:");
            for (Map.Entry<String, NotificationRecord> entry : this.mSummaryByGroupKey.entrySet()) {
                NotificationRecord r = entry.getValue();
                pw.println("    " + entry.getKey() + " -> " + r.getKey());
                if (this.mNotificationsByKey.get(r.getKey()) != r) {
                    pw.println("!!!!!!LEAK: Record not found in mNotificationsByKey.");
                    r.dump(pw, "      ", getContext(), filter.redact);
                }
            }
            if (!zenOnly) {
                pw.println("\n  Usage Stats:");
                this.mUsageStats.dump(pw, "    ", filter);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setNotificationWhiteList() {
        String apps_plus = Settings.Secure.getString(getContext().getContentResolver(), POWER_SAVER_NOTIFICATION_WHITELIST);
        Log.i(TAG, "getNotificationWhiteList from db: " + apps_plus);
        this.power_save_whiteSet.clear();
        for (String s : this.plus_notification_white_list) {
            this.power_save_whiteSet.add(s);
        }
        for (String s2 : this.noitfication_white_list) {
            this.power_save_whiteSet.add(s2);
        }
        if (!TextUtils.isEmpty(apps_plus)) {
            for (String s3 : apps_plus.split(";")) {
                this.power_save_whiteSet.add(s3);
            }
        }
    }

    private boolean isNoitficationWhiteApp(String pkg) {
        return this.power_save_whiteSet.contains(pkg);
    }

    /* access modifiers changed from: package-private */
    public void enqueueNotificationInternal(String pkg, String opPkg, int callingUid, int callingPid, String tag, int id, Notification notification, int incomingUserId) {
        String channelId;
        boolean z;
        String str = pkg;
        String str2 = opPkg;
        int userId = callingUid;
        int i = id;
        Notification notification2 = notification;
        if (SystemProperties.getBoolean("sys.super_power_save", false) && !isNoitficationWhiteApp(pkg)) {
            Flog.i(400, "enqueueNotificationInternal  !isNoitficationWhiteApp package=" + str);
        } else if (isBlockRideModeNotification(pkg)) {
            Flog.i(400, "enqueueNotificationInternal  !isBlockModeNotification package=" + str);
        } else if (isNotificationDisable()) {
            Flog.i(400, "MDM policy is on , enqueueNotificationInternal  !isNotificationDisable package=" + str);
        } else {
            Slog.i(TAG, "enqueueNotificationInternal: pkg=" + str + " id=" + i + " notification=" + notification2);
            int targetUid = getNCTargetAppUid(str2, str, userId, notification2);
            if (targetUid == userId) {
                checkCallerIsSystemOrSameApp(pkg);
            } else {
                Slog.i(TAG, "NC " + userId + " calling " + targetUid);
            }
            if ((isUidSystemOrPhone(targetUid) || PackageManagerService.PLATFORM_PACKAGE_NAME.equals(str) || isCustDialer(pkg)) || !HwDeviceManager.disallowOp(33)) {
                int targetUid2 = targetUid;
                int targetUid3 = ActivityManager.handleIncomingUser(callingPid, userId, incomingUserId, true, false, "enqueueNotification", str);
                UserHandle user = new UserHandle(targetUid3);
                if (str == null || notification2 == null) {
                    int userId2 = targetUid3;
                    int i2 = targetUid2;
                    throw new IllegalArgumentException("null not allowed: pkg=" + str + " id=" + id + " notification=" + notification2);
                }
                int userId3 = targetUid3;
                recognize(tag, i, notification2, user, str, userId, callingPid);
                if (notification2.extras != null && notification2.extras.containsKey("hw_btw")) {
                    if (notification.isGroupSummary()) {
                        CharSequence title = notification2.extras.getCharSequence("android.title");
                        CharSequence content = notification2.extras.getCharSequence("android.text");
                        RemoteViews remoteV = notification2.contentView;
                        RemoteViews bigContentView = notification2.bigContentView;
                        RemoteViews headsUpContentView = notification2.headsUpContentView;
                        if (remoteV == null && bigContentView == null && headsUpContentView == null && title == null && content == null) {
                            Flog.i(400, "epmty GroupSummary");
                            return;
                        }
                        notification2.flags ^= 512;
                    }
                    notification2.setGroup(null);
                }
                int notificationUid = resolveNotificationUid(str2, targetUid2, userId3);
                try {
                    Notification.addFieldsFromContext(this.mPackageManagerClient.getApplicationInfoAsUser(str, 268435456, userId3 == -1 ? 0 : userId3), notification2);
                    if (this.mPackageManagerClient.checkPermission("android.permission.USE_COLORIZED_NOTIFICATIONS", str) == 0) {
                        try {
                            notification2.flags |= 2048;
                        } catch (PackageManager.NameNotFoundException e) {
                            e = e;
                            int i3 = userId3;
                            String str3 = str;
                            int i4 = targetUid2;
                        }
                    } else {
                        notification2.flags &= -2049;
                    }
                    this.mUsageStats.registerEnqueuedByApp(str);
                    String channelId2 = notification.getChannelId();
                    if (this.mIsTelevision && new Notification.TvExtender(notification2).getChannelId() != null) {
                        channelId2 = new Notification.TvExtender(notification2).getChannelId();
                    }
                    NotificationChannel channel = this.mRankingHelper.getNotificationChannel(str, notificationUid, channelId2, false);
                    if (channel == null) {
                        String noChannelStr = "No Channel found for pkg=" + str + ", channelId=" + channelId2 + ", id=" + id + ", tag=" + tag + ", opPkg=" + str2 + ", callingUid=" + userId + ", userId=" + userId3 + ", incomingUserId=" + incomingUserId + ", notificationUid=" + notificationUid + ", notification=" + notification2;
                        Log.e(TAG, noChannelStr);
                        boolean appNotificationsOff = this.mRankingHelper.getImportance(str, notificationUid) == 0;
                        if (!appNotificationsOff) {
                            String str4 = noChannelStr;
                            boolean z2 = appNotificationsOff;
                            Log.e(TAG, "appNotificationsOff is false ");
                        } else {
                            boolean z3 = appNotificationsOff;
                        }
                        return;
                    }
                    int i5 = incomingUserId;
                    int userId4 = userId3;
                    Slog.i(TAG, "enqueueNotificationInternal Channel Info : pkg=" + str + " id=" + userId + " importance =" + channel.getImportance());
                    int targetUid4 = targetUid2;
                    StatusBarNotification statusBarNotification = new StatusBarNotification(str, str2, id, tag, notificationUid, callingPid, notification, user, null, System.currentTimeMillis());
                    NotificationRecord r = new NotificationRecord(getContext(), statusBarNotification, channel);
                    r.setIsAppImportanceLocked(this.mRankingHelper.getIsAppImportanceLocked(str, userId));
                    Notification notification3 = notification;
                    StatusBarNotification statusBarNotification2 = statusBarNotification;
                    if ((notification3.flags & 64) != 0) {
                        boolean fgServiceShown = channel.isFgServiceShown();
                        if ((channel.getUserLockedFields() & 4) == 0 || !fgServiceShown) {
                            int i6 = targetUid4;
                            z = true;
                            if (r.getImportance() == 1 || r.getImportance() == 0) {
                                if (TextUtils.isEmpty(channelId2) || "miscellaneous".equals(channelId2)) {
                                    r.setImportance(2, "Bumped for foreground service");
                                } else {
                                    channel.setImportance(2);
                                    if (!fgServiceShown) {
                                        channel.unlockFields(4);
                                        channel.setFgServiceShown(true);
                                    }
                                    this.mRankingHelper.updateNotificationChannel(str, notificationUid, channel, false);
                                    r.updateNotificationChannel(channel);
                                }
                            }
                        } else {
                            int i7 = targetUid4;
                            z = true;
                        }
                        if (!fgServiceShown && !TextUtils.isEmpty(channelId2) && !"miscellaneous".equals(channelId2)) {
                            channel.setFgServiceShown(z);
                            r.updateNotificationChannel(channel);
                        }
                    }
                    int userId5 = userId4;
                    String str5 = str;
                    if (checkDisqualifyingFeatures(userId4, notificationUid, id, tag, r, r.sbn.getOverrideGroupKey() != null)) {
                        if (notification3.allPendingIntents != null) {
                            int intentCount = notification3.allPendingIntents.size();
                            if (intentCount > 0) {
                                ActivityManagerInternal am = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
                                long duration = ((DeviceIdleController.LocalService) LocalServices.getService(DeviceIdleController.LocalService.class)).getNotificationWhitelistDuration();
                                int i8 = 0;
                                while (true) {
                                    int i9 = i8;
                                    if (i9 >= intentCount) {
                                        break;
                                    }
                                    PendingIntent pendingIntent = (PendingIntent) notification3.allPendingIntents.valueAt(i9);
                                    if (pendingIntent != null) {
                                        channelId = channelId2;
                                        am.setPendingIntentWhitelistDuration(pendingIntent.getTarget(), WHITELIST_TOKEN, duration);
                                    } else {
                                        channelId = channelId2;
                                    }
                                    i8 = i9 + 1;
                                    channelId2 = channelId;
                                }
                            }
                        }
                        if (this.mNotificationResource == null) {
                            if (DBG || Log.HWINFO) {
                                Log.i(TAG, " init notification resource");
                            }
                            this.mNotificationResource = HwFrameworkFactory.getHwResource(10);
                        }
                        if (this.mNotificationResource == null || 2 != this.mNotificationResource.acquire(notificationUid, str5, -1)) {
                            this.mHandler.post(new EnqueueNotificationRunnable(userId5, r));
                            return;
                        }
                        if (DBG || Log.HWINFO) {
                            Log.i(TAG, " enqueueNotificationInternal dont acquire resource");
                        }
                    }
                } catch (PackageManager.NameNotFoundException e2) {
                    e = e2;
                    int i10 = userId3;
                    String str6 = str;
                    int i11 = targetUid2;
                    Slog.e(TAG, "Cannot create a context for sending app", e);
                }
            } else {
                Flog.i(400, "MDM policy forbid (targetUid : " + targetUid + ", pkg : " + str + ") send notification");
            }
        }
    }

    private void doChannelWarningToast(CharSequence toastText) {
        if (Settings.Global.getInt(getContext().getContentResolver(), "show_notification_channel_warnings", (int) Build.IS_DEBUGGABLE) != 0) {
            Toast.makeText(getContext(), this.mHandler.getLooper(), toastText, 0).show();
        }
    }

    private int resolveNotificationUid(String opPackageName, int callingUid, int userId) {
        if (isCallerSystemOrPhone() && opPackageName != null && !PackageManagerService.PLATFORM_PACKAGE_NAME.equals(opPackageName) && !TELECOM_PKG.equals(opPackageName)) {
            try {
                return getContext().getPackageManager().getPackageUidAsUser(opPackageName, userId);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        return callingUid;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00dc, code lost:
        return false;
     */
    private boolean checkDisqualifyingFeatures(int userId, int callingUid, int id, String tag, NotificationRecord r, boolean isAutogroup) {
        int i = userId;
        NotificationRecord notificationRecord = r;
        String pkg = notificationRecord.sbn.getPackageName();
        boolean isSystemNotification = isUidSystemOrPhone(callingUid) || PackageManagerService.PLATFORM_PACKAGE_NAME.equals(pkg);
        boolean isNotificationFromListener = this.mListeners.isListenerPackage(pkg);
        if (isSystemNotification || isNotificationFromListener) {
            int i2 = id;
            String str = tag;
        } else {
            synchronized (this.mNotificationLock) {
                try {
                    if (this.mNotificationsByKey.get(notificationRecord.sbn.getKey()) == null && !isPushNotification(notificationRecord.sbn.getOpPkg(), pkg, notificationRecord.sbn.getNotification())) {
                        if (isCallerInstantApp(pkg)) {
                            throw new SecurityException("Instant app " + pkg + " cannot create notifications");
                        }
                    }
                    if (this.mNotificationsByKey.get(notificationRecord.sbn.getKey()) != null && !r.getNotification().hasCompletedProgress() && !isAutogroup) {
                        float appEnqueueRate = this.mUsageStats.getAppEnqueueRate(pkg);
                        if (appEnqueueRate > this.mMaxPackageEnqueueRate) {
                            this.mUsageStats.registerOverRateQuota(pkg);
                            long now = SystemClock.elapsedRealtime();
                            if (now - this.mLastOverRateLogTime > MIN_PACKAGE_OVERRATE_LOG_INTERVAL) {
                                Slog.e(TAG, "Package enqueue rate is " + appEnqueueRate + ". Shedding " + notificationRecord.sbn.getKey() + ". package=" + pkg);
                                this.mLastOverRateLogTime = now;
                            }
                        }
                    }
                    int count = getNotificationCountLocked(pkg, i, id, tag);
                    if (count >= 50) {
                        this.mUsageStats.registerOverCountQuota(pkg);
                        Slog.e(TAG, "Package has already posted or enqueued " + count + " notifications.  Not showing more.  package=" + pkg);
                        return false;
                    }
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            }
        }
        if (!this.mSnoozeHelper.isSnoozed(i, pkg, r.getKey())) {
            return !isBlocked(notificationRecord, this.mUsageStats);
        }
        MetricsLogger.action(r.getLogMaker().setType(6).setCategory(831));
        if (DBG) {
            Slog.d(TAG, "Ignored enqueue for snoozed notification " + r.getKey());
        }
        this.mSnoozeHelper.update(i, notificationRecord);
        savePolicyFile();
        return false;
    }

    /* access modifiers changed from: protected */
    @GuardedBy("mNotificationLock")
    public int getNotificationCountLocked(String pkg, int userId, int excludedId, String excludedTag) {
        int N = this.mNotificationList.size();
        int count = 0;
        for (int i = 0; i < N; i++) {
            NotificationRecord existing = this.mNotificationList.get(i);
            if (existing.sbn.getPackageName().equals(pkg) && existing.sbn.getUserId() == userId && (existing.sbn.getId() != excludedId || !TextUtils.equals(existing.sbn.getTag(), excludedTag))) {
                count++;
            }
        }
        int M = this.mEnqueuedNotifications.size();
        for (int i2 = 0; i2 < M; i2++) {
            NotificationRecord existing2 = this.mEnqueuedNotifications.get(i2);
            if (existing2.sbn.getPackageName().equals(pkg) && existing2.sbn.getUserId() == userId) {
                count++;
            }
        }
        return count;
    }

    /* access modifiers changed from: protected */
    public boolean isBlocked(NotificationRecord r, NotificationUsageStats usageStats) {
        String pkg = r.sbn.getPackageName();
        int callingUid = r.sbn.getUid();
        boolean isPackageSuspended = isPackageSuspendedForUser(pkg, callingUid);
        if (isPackageSuspended) {
            Slog.e(TAG, "Suppressing notification from package due to package suspended by administrator.");
            usageStats.registerSuspendedByAdmin(r);
            return isPackageSuspended;
        }
        boolean isFromPinNf = isFromPinNotification(r.getNotification(), pkg);
        boolean isRankGroupBlocked = this.mRankingHelper.isGroupBlocked(pkg, callingUid, r.getChannel().getGroup());
        int rankImportance = this.mRankingHelper.getImportance(pkg, callingUid);
        int channelImportance = r.getChannel().getImportance();
        boolean isBlocked = !isFromPinNf && (isRankGroupBlocked || rankImportance == 0 || channelImportance == 0);
        if (isBlocked) {
            Slog.e(TAG, "Suppressing notification from package by user request. isFromPinNotification = " + isFromPinNf + ", isGroupBlocked = " + isRankGroupBlocked + ", RankImportance = " + rankImportance + ", ChannelImportance = " + channelImportance);
            usageStats.registerBlocked(r);
        }
        return isBlocked;
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public boolean isPackageSuspendedLocked(NotificationRecord r) {
        return isPackageSuspendedForUser(r.sbn.getPackageName(), r.sbn.getUid());
    }

    /* access modifiers changed from: private */
    public boolean isExpandedNtfPkg(String pkgName) {
        return EXPANDEDNTF_PKGS.contains(pkgName);
    }

    /* access modifiers changed from: private */
    public boolean isTopFullscreen() {
        boolean z = false;
        int ret = 0;
        try {
            IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
            if (wm == null) {
                return false;
            }
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.view.IWindowManager");
            wm.asBinder().transact(IS_TOP_FULL_SCREEN_TOKEN, data, reply, 0);
            ret = reply.readInt();
            if (ret > 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.e(TAG, "isTopIsFullscreen", e);
        }
    }

    /* access modifiers changed from: protected */
    @GuardedBy("mNotificationLock")
    @VisibleForTesting
    public boolean isVisuallyInterruptive(NotificationRecord old, NotificationRecord r) {
        NotificationRecord notificationRecord = old;
        NotificationRecord notificationRecord2 = r;
        if (notificationRecord == null) {
            if (DEBUG_INTERRUPTIVENESS) {
                Log.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is interruptive: new notification");
            }
            return true;
        } else if (notificationRecord2 == null) {
            if (DEBUG_INTERRUPTIVENESS) {
                Log.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is not interruptive: null");
            }
            return false;
        } else {
            Notification oldN = notificationRecord.sbn.getNotification();
            Notification newN = notificationRecord2.sbn.getNotification();
            if (oldN.extras == null || newN.extras == null) {
                if (DEBUG_INTERRUPTIVENESS) {
                    Log.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is not interruptive: no extras");
                }
                return false;
            } else if ((notificationRecord2.sbn.getNotification().flags & 64) != 0) {
                if (DEBUG_INTERRUPTIVENESS) {
                    Log.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is not interruptive: foreground service");
                }
                return false;
            } else if (!notificationRecord2.sbn.isGroup() || !notificationRecord2.sbn.getNotification().isGroupSummary()) {
                String oldTitle = String.valueOf(oldN.extras.get("android.title"));
                String newTitle = String.valueOf(newN.extras.get("android.title"));
                if (!Objects.equals(oldTitle, newTitle)) {
                    if (DEBUG_INTERRUPTIVENESS) {
                        Log.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is interruptive: changed title");
                        StringBuilder sb = new StringBuilder();
                        sb.append("INTERRUPTIVENESS: ");
                        sb.append(String.format("   old title: %s (%s@0x%08x)", new Object[]{oldTitle, oldTitle.getClass(), Integer.valueOf(oldTitle.hashCode())}));
                        Log.v(TAG, sb.toString());
                        Log.v(TAG, "INTERRUPTIVENESS: " + String.format("   new title: %s (%s@0x%08x)", new Object[]{newTitle, newTitle.getClass(), Integer.valueOf(newTitle.hashCode())}));
                    }
                    return true;
                }
                String oldText = String.valueOf(oldN.extras.get("android.text"));
                String newText = String.valueOf(newN.extras.get("android.text"));
                if (!Objects.equals(oldText, newText)) {
                    if (DEBUG_INTERRUPTIVENESS) {
                        Log.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is interruptive: changed text");
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("INTERRUPTIVENESS: ");
                        sb2.append(String.format("   old text: %s (%s@0x%08x)", new Object[]{oldText, oldText.getClass(), Integer.valueOf(oldText.hashCode())}));
                        Log.v(TAG, sb2.toString());
                        Log.v(TAG, "INTERRUPTIVENESS: " + String.format("   new text: %s (%s@0x%08x)", new Object[]{newText, newText.getClass(), Integer.valueOf(newText.hashCode())}));
                    }
                    return true;
                } else if (oldN.hasCompletedProgress() != newN.hasCompletedProgress()) {
                    if (DEBUG_INTERRUPTIVENESS) {
                        Log.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is interruptive: completed progress");
                    }
                    return true;
                } else if (Notification.areActionsVisiblyDifferent(oldN, newN)) {
                    if (DEBUG_INTERRUPTIVENESS) {
                        Log.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is interruptive: changed actions");
                    }
                    return true;
                } else {
                    try {
                        Notification.Builder oldB = Notification.Builder.recoverBuilder(getContext(), oldN);
                        Notification.Builder newB = Notification.Builder.recoverBuilder(getContext(), newN);
                        if (Notification.areStyledNotificationsVisiblyDifferent(oldB, newB)) {
                            if (DEBUG_INTERRUPTIVENESS) {
                                Log.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is interruptive: styles differ");
                            }
                            return true;
                        }
                        if (Notification.areRemoteViewsChanged(oldB, newB)) {
                            if (DEBUG_INTERRUPTIVENESS) {
                                Log.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is interruptive: remoteviews differ");
                            }
                            return true;
                        }
                        return false;
                    } catch (Exception e) {
                        Slog.w(TAG, "error recovering builder", e);
                    }
                }
            } else {
                if (DEBUG_INTERRUPTIVENESS) {
                    Log.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is not interruptive: summary");
                }
                return false;
            }
        }
    }

    /* access modifiers changed from: protected */
    @GuardedBy("mNotificationLock")
    @VisibleForTesting
    public void logRecentLocked(NotificationRecord r) {
        if (!r.isUpdate) {
            ArrayList<NotifyingApp> recentAppsForUser = (ArrayList) this.mRecentApps.getOrDefault(Integer.valueOf(r.getUser().getIdentifier()), new ArrayList(6));
            NotifyingApp na = new NotifyingApp().setPackage(r.sbn.getPackageName()).setUid(r.sbn.getUid()).setLastNotified(r.sbn.getPostTime());
            int i = recentAppsForUser.size() - 1;
            while (true) {
                if (i < 0) {
                    break;
                }
                NotifyingApp naExisting = recentAppsForUser.get(i);
                if (na.getPackage().equals(naExisting.getPackage()) && na.getUid() == naExisting.getUid()) {
                    recentAppsForUser.remove(i);
                    break;
                }
                i--;
            }
            recentAppsForUser.add(0, na);
            if (recentAppsForUser.size() > 5) {
                recentAppsForUser.remove(recentAppsForUser.size() - 1);
            }
            this.mRecentApps.put(Integer.valueOf(r.getUser().getIdentifier()), recentAppsForUser);
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public void handleGroupedNotificationLocked(NotificationRecord r, NotificationRecord old, int callingUid, int callingPid) {
        NotificationRecord notificationRecord = r;
        NotificationRecord notificationRecord2 = old;
        StatusBarNotification sbn = notificationRecord.sbn;
        Notification n = sbn.getNotification();
        if (n.isGroupSummary() && !sbn.isAppGroup()) {
            n.flags &= -513;
        }
        String group = sbn.getGroupKey();
        boolean isSummary = n.isGroupSummary();
        String str = null;
        Notification oldN = notificationRecord2 != null ? notificationRecord2.sbn.getNotification() : null;
        if (notificationRecord2 != null) {
            str = notificationRecord2.sbn.getGroupKey();
        }
        String oldGroup = str;
        boolean oldIsSummary = notificationRecord2 != null && oldN.isGroupSummary();
        if (oldIsSummary) {
            NotificationRecord removedSummary = this.mSummaryByGroupKey.remove(oldGroup);
            if (removedSummary != notificationRecord2) {
                String removedKey = removedSummary != null ? removedSummary.getKey() : "<null>";
                Slog.w(TAG, "Removed summary didn't match old notification: old=" + old.getKey() + ", removed=" + removedKey);
            }
        }
        if (isSummary) {
            this.mSummaryByGroupKey.put(group, notificationRecord);
        }
        if (!oldIsSummary) {
            return;
        }
        if (!isSummary || !oldGroup.equals(group)) {
            cancelGroupChildrenLocked(notificationRecord2, callingUid, callingPid, null, false, null);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mNotificationLock")
    @VisibleForTesting
    public void scheduleTimeoutLocked(NotificationRecord record) {
        if (record.getNotification().getTimeoutAfter() > 0) {
            this.mAlarmManager.setExactAndAllowWhileIdle(2, SystemClock.elapsedRealtime() + record.getNotification().getTimeoutAfter(), PendingIntent.getBroadcast(getContext(), 1, new Intent(ACTION_NOTIFICATION_TIMEOUT).setData(new Uri.Builder().scheme(SCHEME_TIMEOUT).appendPath(record.getKey()).build()).addFlags(268435456).putExtra(EXTRA_KEY, record.getKey()), 134217728));
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:134:0x022c A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x024f  */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x0254 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x026c  */
    /* JADX WARNING: Removed duplicated region for block: B:150:0x026e  */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x0271  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x0274  */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x027a  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x027d  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x028a  */
    /* JADX WARNING: Removed duplicated region for block: B:161:0x028c  */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x028f  */
    /* JADX WARNING: Removed duplicated region for block: B:164:0x0291  */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x0295  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0132  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0134  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0138 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x013f  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x015d  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0165 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x016d  */
    @GuardedBy("mNotificationLock")
    @VisibleForTesting
    public void buzzBeepBlinkLocked(NotificationRecord record) {
        boolean blink;
        boolean buzz;
        boolean beep;
        boolean wasShowLights;
        boolean beep2;
        boolean blink2;
        boolean isHwFallBackToVibration;
        long[] vibration;
        long[] vibration2;
        boolean hasValidVibrate;
        boolean isHwSoundAllow;
        boolean hasAudibleAlert;
        boolean buzz2;
        NotificationRecord notificationRecord = record;
        Notification notification = notificationRecord.sbn.getNotification();
        String key = record.getKey();
        boolean aboveThreshold = record.getImportance() >= 3;
        boolean canInterrupt = !isFromPinNotification(notification, notificationRecord.sbn.getPackageName()) && !disallowInterrupt(notification);
        if (DBG || record.isIntercepted()) {
            Slog.v(TAG, "pkg=" + notificationRecord.sbn.getPackageName() + " canInterrupt=" + canInterrupt + " record.getImportance()=" + record.getImportance());
        }
        String disableEffects = disableNotificationEffects(record);
        boolean wasBeep = key != null && key.equals(this.mSoundNotificationKey);
        boolean wasBuzz = key != null && key.equals(this.mVibrateNotificationKey);
        boolean hasValidVibrate2 = false;
        boolean hasValidSound = false;
        boolean sentAccessibilityEvent = false;
        if (!notificationRecord.isUpdate && record.getImportance() > 1) {
            sendAccessibilityEvent(notification, notificationRecord.sbn.getPackageName());
            sentAccessibilityEvent = true;
        }
        if (!aboveThreshold || !isNotificationForCurrentUser(record)) {
            buzz = false;
            beep2 = false;
            blink2 = false;
            if (DBG || Log.HWINFO) {
                StringBuilder sb = new StringBuilder();
                sb.append("disableEffects=");
                sb.append(disableEffects);
                sb.append(" canInterrupt=");
                sb.append(canInterrupt);
                sb.append(" once update: ");
                sb.append(notificationRecord.isUpdate && (notification.flags & 8) != 0);
                Slog.v(TAG, sb.toString());
            }
        } else if (!canInterrupt || !this.mSystemReady || this.mAudioManager == null) {
            buzz = false;
            beep2 = false;
            blink2 = false;
        } else if (!this.mGameDndStatus || !isGameDndSwitchOn()) {
            Uri soundUri = record.getSound();
            boolean hasValidSound2 = soundUri != null && !Uri.EMPTY.equals(soundUri);
            boolean isHwSoundAllow2 = isHwSoundAllow(notificationRecord.sbn.getPackageName(), record.getChannel().getId(), record.getUserId());
            if (!isHwSoundAllow2) {
                if (this.mAudioManager.getRingerModeInternal() == 2) {
                    isHwFallBackToVibration = true;
                    vibration = record.getVibration();
                    if (vibration == null || !hasValidSound2) {
                        blink = false;
                    } else {
                        blink = false;
                        if ((this.mAudioManager.getRingerModeInternal() == 1 || isHwFallBackToVibration) && this.mAudioManager.getStreamVolume(AudioAttributes.toLegacyStreamType(record.getAudioAttributes())) == 0) {
                            vibration2 = this.mFallbackVibrationPattern;
                            hasValidVibrate = vibration2 != null;
                            hasValidSound = !hasValidSound2 && isHwSoundAllow2;
                            if (hasValidVibrate) {
                                boolean z = isHwSoundAllow2;
                                boolean z2 = isHwFallBackToVibration;
                                if (isHwVibrateAllow(notificationRecord.sbn.getPackageName(), record.getChannel().getId(), record.getUserId())) {
                                    isHwSoundAllow = true;
                                    hasValidVibrate2 = isHwSoundAllow;
                                    hasAudibleAlert = !hasValidSound || hasValidVibrate2;
                                    if (hasAudibleAlert || shouldMuteNotificationLocked(record)) {
                                        buzz2 = false;
                                        beep = false;
                                    } else {
                                        if (!sentAccessibilityEvent) {
                                            sendAccessibilityEvent(notification, notificationRecord.sbn.getPackageName());
                                        }
                                        if (DBG) {
                                            Slog.v(TAG, "Interrupting!");
                                        }
                                        if (hasValidSound) {
                                            this.mSoundNotificationKey = key;
                                            if (this.mInCall) {
                                                playInCallNotification();
                                                beep = true;
                                            } else {
                                                beep = playSound(notificationRecord, soundUri);
                                            }
                                        } else {
                                            beep = false;
                                        }
                                        boolean ringerModeSilent = this.mAudioManager.getRingerModeInternal() == 0;
                                        boolean z3 = hasAudibleAlert;
                                        if (this.mInCall || !hasValidVibrate2 || ringerModeSilent) {
                                            buzz2 = false;
                                        } else {
                                            this.mVibrateNotificationKey = key;
                                            buzz2 = playVibration(notificationRecord, vibration2, hasValidSound);
                                        }
                                    }
                                    buzz = buzz2;
                                    if (wasBeep && !hasValidSound) {
                                        clearSoundLocked();
                                    }
                                    if (wasBuzz && !hasValidVibrate2) {
                                        clearVibrateLocked();
                                    }
                                    wasShowLights = this.mLights.remove(key);
                                    if (record.getLight() == null && aboveThreshold && canInterrupt && (record.getSuppressedVisualEffects() & 8) == 0) {
                                        this.mLights.add(key);
                                        updateLightsLocked();
                                        if (this.mUseAttentionLight) {
                                            this.mAttentionLight.pulse();
                                        }
                                        blink = true;
                                    } else if (wasShowLights) {
                                        updateLightsLocked();
                                    }
                                    if (!buzz || beep || blink) {
                                        int i = 1;
                                        notificationRecord.setInterruptive(true);
                                        MetricsLogger.action(record.getLogMaker().setCategory(199).setType(1).setSubtype((buzz ? 1 : 0) | (beep ? 2 : 0) | (blink ? 4 : 0)));
                                        int i2 = buzz ? 1 : 0;
                                        int i3 = beep ? 1 : 0;
                                        if (!blink) {
                                            i = 0;
                                        }
                                        EventLogTags.writeNotificationAlert(key, i2, i3, i);
                                    }
                                    return;
                                }
                            } else {
                                boolean z4 = isHwFallBackToVibration;
                            }
                            isHwSoundAllow = false;
                            hasValidVibrate2 = isHwSoundAllow;
                            if (!hasValidSound) {
                            }
                            if (hasAudibleAlert) {
                            }
                            buzz2 = false;
                            beep = false;
                            buzz = buzz2;
                            clearSoundLocked();
                            clearVibrateLocked();
                            wasShowLights = this.mLights.remove(key);
                            if (record.getLight() == null) {
                            }
                            if (wasShowLights) {
                            }
                            if (!buzz) {
                            }
                            int i4 = 1;
                            notificationRecord.setInterruptive(true);
                            MetricsLogger.action(record.getLogMaker().setCategory(199).setType(1).setSubtype((buzz ? 1 : 0) | (beep ? 2 : 0) | (blink ? 4 : 0)));
                            if (buzz) {
                            }
                            if (beep) {
                            }
                            if (!blink) {
                            }
                            EventLogTags.writeNotificationAlert(key, i2, i3, i4);
                        }
                    }
                    vibration2 = vibration;
                    hasValidVibrate = vibration2 != null;
                    if (!hasValidSound2) {
                    }
                    if (hasValidVibrate) {
                    }
                    isHwSoundAllow = false;
                    hasValidVibrate2 = isHwSoundAllow;
                    if (!hasValidSound) {
                    }
                    if (hasAudibleAlert) {
                    }
                    buzz2 = false;
                    beep = false;
                    buzz = buzz2;
                    clearSoundLocked();
                    clearVibrateLocked();
                    wasShowLights = this.mLights.remove(key);
                    if (record.getLight() == null) {
                    }
                    if (wasShowLights) {
                    }
                    if (!buzz) {
                    }
                    int i42 = 1;
                    notificationRecord.setInterruptive(true);
                    MetricsLogger.action(record.getLogMaker().setCategory(199).setType(1).setSubtype((buzz ? 1 : 0) | (beep ? 2 : 0) | (blink ? 4 : 0)));
                    if (buzz) {
                    }
                    if (beep) {
                    }
                    if (!blink) {
                    }
                    EventLogTags.writeNotificationAlert(key, i2, i3, i42);
                }
            }
            isHwFallBackToVibration = false;
            vibration = record.getVibration();
            if (vibration == null) {
            }
            blink = false;
            vibration2 = vibration;
            hasValidVibrate = vibration2 != null;
            if (!hasValidSound2) {
            }
            if (hasValidVibrate) {
            }
            isHwSoundAllow = false;
            hasValidVibrate2 = isHwSoundAllow;
            if (!hasValidSound) {
            }
            if (hasAudibleAlert) {
            }
            buzz2 = false;
            beep = false;
            buzz = buzz2;
            clearSoundLocked();
            clearVibrateLocked();
            wasShowLights = this.mLights.remove(key);
            if (record.getLight() == null) {
            }
            if (wasShowLights) {
            }
            if (!buzz) {
            }
            int i422 = 1;
            notificationRecord.setInterruptive(true);
            MetricsLogger.action(record.getLogMaker().setCategory(199).setType(1).setSubtype((buzz ? 1 : 0) | (beep ? 2 : 0) | (blink ? 4 : 0)));
            if (buzz) {
            }
            if (beep) {
            }
            if (!blink) {
            }
            EventLogTags.writeNotificationAlert(key, i2, i3, i422);
        } else {
            buzz = false;
            beep2 = false;
            blink2 = false;
        }
        beep = beep2;
        clearSoundLocked();
        clearVibrateLocked();
        wasShowLights = this.mLights.remove(key);
        if (record.getLight() == null) {
        }
        if (wasShowLights) {
        }
        if (!buzz) {
        }
        int i4222 = 1;
        notificationRecord.setInterruptive(true);
        MetricsLogger.action(record.getLogMaker().setCategory(199).setType(1).setSubtype((buzz ? 1 : 0) | (beep ? 2 : 0) | (blink ? 4 : 0)));
        if (buzz) {
        }
        if (beep) {
        }
        if (!blink) {
        }
        EventLogTags.writeNotificationAlert(key, i2, i3, i4222);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mNotificationLock")
    public boolean shouldMuteNotificationLocked(NotificationRecord record) {
        Notification notification = record.getNotification();
        if (record.isUpdate && (notification.flags & 8) != 0) {
            return true;
        }
        String disableEffects = disableNotificationEffects(record);
        if (disableEffects != null && !allowNotificationsInCall(disableEffects, record)) {
            ZenLog.traceDisableEffects(record, disableEffects);
            return true;
        } else if (record.isIntercepted() && !inNonDisturbMode(record.sbn.getPackageName())) {
            return true;
        } else {
            if (record.sbn.isGroup() && notification.suppressAlertingDueToGrouping()) {
                return true;
            }
            if (!this.mUsageStats.isAlertRateLimited(record.sbn.getPackageName())) {
                return false;
            }
            Slog.e(TAG, "Muting recently noisy " + record.getKey());
            return true;
        }
    }

    private boolean playSound(NotificationRecord record, Uri soundUri) {
        boolean looping = (record.getNotification().flags & 4) != 0;
        if (!this.mAudioManager.isAudioFocusExclusive() && this.mAudioManager.getStreamVolume(AudioAttributes.toLegacyStreamType(record.getAudioAttributes())) != 0) {
            long identity = Binder.clearCallingIdentity();
            try {
                IRingtonePlayer player = this.mAudioManager.getRingtonePlayer();
                if (player != null) {
                    Slog.v(TAG, "Playing sound " + soundUri + " with attributes " + record.getAudioAttributes());
                    player.playAsync(soundUri, record.sbn.getUser(), looping, record.getAudioAttributes());
                    Binder.restoreCallingIdentity(identity);
                    return true;
                }
            } catch (RemoteException e) {
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
        }
        return false;
    }

    private boolean playVibration(NotificationRecord record, long[] vibration, boolean delayVibForSound) {
        int i;
        long identity = Binder.clearCallingIdentity();
        try {
            if ((record.getNotification().flags & 4) != 0) {
                i = 0;
            } else {
                i = -1;
            }
            VibrationEffect effect = VibrationEffect.createWaveform(vibration, i);
            if (delayVibForSound) {
                new Thread(new Runnable(record, effect) {
                    private final /* synthetic */ NotificationRecord f$1;
                    private final /* synthetic */ VibrationEffect f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        NotificationManagerService.lambda$playVibration$0(NotificationManagerService.this, this.f$1, this.f$2);
                    }
                }).start();
            } else {
                this.mVibrator.vibrate(record.sbn.getUid(), record.sbn.getOpPkg(), effect, record.getAudioAttributes());
            }
            return true;
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "Error creating vibration waveform with pattern: " + Arrays.toString(vibration));
            return false;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public static /* synthetic */ void lambda$playVibration$0(NotificationManagerService notificationManagerService, NotificationRecord record, VibrationEffect effect) {
        int waitMs = notificationManagerService.mAudioManager.getFocusRampTimeMs(3, record.getAudioAttributes());
        if (DBG) {
            Slog.v(TAG, "Delaying vibration by " + waitMs + "ms");
        }
        try {
            Thread.sleep((long) waitMs);
        } catch (InterruptedException e) {
        }
        notificationManagerService.mVibrator.vibrate(record.sbn.getUid(), record.sbn.getOpPkg(), effect, record.getAudioAttributes());
    }

    /* JADX INFO: finally extract failed */
    private boolean isNotificationForCurrentUser(NotificationRecord record) {
        long token = Binder.clearCallingIdentity();
        try {
            int currentUser = ActivityManager.getCurrentUser();
            Binder.restoreCallingIdentity(token);
            return record.getUserId() == -1 || record.getUserId() == currentUser || this.mUserProfiles.isCurrentProfile(record.getUserId());
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void playInCallNotification() {
        new Thread() {
            public void run() {
                long identity = Binder.clearCallingIdentity();
                try {
                    IRingtonePlayer player = NotificationManagerService.this.mAudioManager.getRingtonePlayer();
                    if (player != null) {
                        Slog.v(NotificationManagerService.TAG, "playInCallNotification sound " + NotificationManagerService.this.mInCallNotificationUri + " with attributes " + NotificationManagerService.this.mInCallNotificationAudioAttributes + "mInCallBinder : " + NotificationManagerService.this.mInCallBinder);
                        player.play(NotificationManagerService.this.mInCallBinder, NotificationManagerService.this.mInCallNotificationUri, NotificationManagerService.this.mInCallNotificationAudioAttributes, NotificationManagerService.this.mInCallNotificationVolume, false);
                    }
                } catch (RemoteException e) {
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
                Binder.restoreCallingIdentity(identity);
            }
        }.start();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mToastQueue")
    public void showNextToastLocked() {
        ToastRecord record = this.mToastQueue.get(0);
        while (record != null) {
            Flog.i(400, "Show pkg=" + record.pkg + " callback=" + record.callback);
            try {
                record.callback.show(record.token);
                scheduleDurationReachedLocked(record);
                return;
            } catch (RemoteException e) {
                Slog.w(TAG, "Object died trying to show notification " + record.callback + " in package " + record.pkg);
                int index = this.mToastQueue.indexOf(record);
                if (index >= 0) {
                    this.mToastQueue.remove(index);
                }
                keepProcessAliveIfNeededLocked(record.pid);
                if (this.mToastQueue.size() > 0) {
                    record = this.mToastQueue.get(0);
                } else {
                    record = null;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mToastQueue")
    public void cancelToastLocked(int index) {
        ToastRecord record = this.mToastQueue.get(index);
        try {
            record.callback.hide();
        } catch (RemoteException e) {
            Slog.w(TAG, "Object died trying to hide notification " + record.callback + " in package " + record.pkg);
        }
        ToastRecord lastToast = this.mToastQueue.remove(index);
        if (!HwPCUtils.isPcCastModeInServer() || !(lastToast instanceof ToastRecordEx)) {
            this.mWindowManagerInternal.removeWindowToken(lastToast.token, false, 0);
            scheduleKillTokenTimeout(lastToast.token);
        } else {
            this.mWindowManagerInternal.removeWindowToken(lastToast.token, true, ((ToastRecordEx) lastToast).displayId);
        }
        keepProcessAliveIfNeededLocked(record.pid);
        if (this.mToastQueue.size() > 0) {
            showNextToastLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void finishTokenLocked(IBinder t) {
        this.mHandler.removeCallbacksAndMessages(t);
        this.mWindowManagerInternal.removeWindowToken(t, true, 0);
    }

    @GuardedBy("mToastQueue")
    private void scheduleDurationReachedLocked(ToastRecord r) {
        this.mHandler.removeCallbacksAndMessages(r);
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 2, r), r.duration == 1 ? 3500 : 2000);
    }

    /* access modifiers changed from: private */
    public void handleDurationReached(ToastRecord record) {
        Flog.i(400, "Timeout pkg=" + record.pkg + " callback=" + record.callback);
        synchronized (this.mToastQueue) {
            int index = indexOfToastLocked(record.pkg, record.callback);
            if (index >= 0) {
                cancelToastLocked(index);
            }
        }
    }

    @GuardedBy("mToastQueue")
    private void scheduleKillTokenTimeout(IBinder token) {
        this.mHandler.removeCallbacksAndMessages(token);
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 7, token), 11000);
    }

    /* access modifiers changed from: private */
    public void handleKillTokenTimeout(IBinder token) {
        if (DBG) {
            Slog.d(TAG, "Kill Token Timeout token=" + token);
        }
        synchronized (this.mToastQueue) {
            finishTokenLocked(token);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mToastQueue")
    public int indexOfToastLocked(String pkg, ITransientNotification callback) {
        IBinder cbak = callback.asBinder();
        ArrayList<ToastRecord> list = this.mToastQueue;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            ToastRecord r = list.get(i);
            if (r.pkg.equals(pkg) && r.callback.asBinder().equals(cbak)) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mToastQueue")
    public int indexOfToastPackageLocked(String pkg) {
        ArrayList<ToastRecord> list = this.mToastQueue;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            if (list.get(i).pkg.equals(pkg)) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mToastQueue")
    public void keepProcessAliveIfNeededLocked(int pid) {
        ArrayList<ToastRecord> list = this.mToastQueue;
        int N = list.size();
        boolean z = false;
        int toastCount = 0;
        for (int i = 0; i < N; i++) {
            if (list.get(i).pid == pid) {
                toastCount++;
            }
        }
        try {
            IActivityManager iActivityManager = this.mAm;
            IBinder iBinder = this.mForegroundToken;
            if (toastCount > 0) {
                z = true;
            }
            iActivityManager.setProcessImportant(iBinder, pid, z, "toast");
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0067, code lost:
        if (r11 == false) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0069, code lost:
        r13.mHandler.scheduleSendRankingUpdate();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x006e, code lost:
        return;
     */
    public void handleRankingReconsideration(Message message) {
        boolean changed;
        if (message.obj instanceof RankingReconsideration) {
            RankingReconsideration recon = (RankingReconsideration) message.obj;
            recon.run();
            synchronized (this.mNotificationLock) {
                NotificationRecord record = this.mNotificationsByKey.get(recon.getKey());
                if (record != null) {
                    int indexBefore = findNotificationRecordIndexLocked(record);
                    boolean interceptBefore = record.isIntercepted();
                    float contactAffinityBefore = record.getContactAffinity();
                    int visibilityBefore = record.getPackageVisibilityOverride();
                    recon.applyChangesLocked(record);
                    applyZenModeLocked(record);
                    this.mRankingHelper.sort(this.mNotificationList);
                    int indexAfter = findNotificationRecordIndexLocked(record);
                    boolean interceptAfter = record.isIntercepted();
                    float contactAffinityAfter = record.getContactAffinity();
                    int visibilityAfter = record.getPackageVisibilityOverride();
                    if (indexBefore == indexAfter && interceptBefore == interceptAfter) {
                        if (visibilityBefore == visibilityAfter) {
                            changed = false;
                            if (interceptBefore && !interceptAfter && Float.compare(contactAffinityBefore, contactAffinityAfter) != 0) {
                                buzzBeepBlinkLocked(record);
                            }
                        }
                    }
                    changed = true;
                    buzzBeepBlinkLocked(record);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleRankingSort() {
        if (this.mRankingHelper != null) {
            synchronized (this.mNotificationLock) {
                int N = this.mNotificationList.size();
                ArrayList<String> orderBefore = new ArrayList<>(N);
                int[] visibilities = new int[N];
                boolean[] showBadges = new boolean[N];
                ArrayList<NotificationChannel> channelBefore = new ArrayList<>(N);
                ArrayList<String> groupKeyBefore = new ArrayList<>(N);
                ArrayList<ArrayList<String>> overridePeopleBefore = new ArrayList<>(N);
                ArrayList<ArrayList<SnoozeCriterion>> snoozeCriteriaBefore = new ArrayList<>(N);
                ArrayList<Integer> userSentimentBefore = new ArrayList<>(N);
                ArrayList<Integer> suppressVisuallyBefore = new ArrayList<>(N);
                int i = 0;
                for (int i2 = 0; i2 < N; i2++) {
                    NotificationRecord r = this.mNotificationList.get(i2);
                    orderBefore.add(r.getKey());
                    visibilities[i2] = r.getPackageVisibilityOverride();
                    showBadges[i2] = r.canShowBadge();
                    channelBefore.add(r.getChannel());
                    groupKeyBefore.add(r.getGroupKey());
                    overridePeopleBefore.add(r.getPeopleOverride());
                    snoozeCriteriaBefore.add(r.getSnoozeCriteria());
                    userSentimentBefore.add(Integer.valueOf(r.getUserSentiment()));
                    suppressVisuallyBefore.add(Integer.valueOf(r.getSuppressedVisualEffects()));
                    this.mRankingHelper.extractSignals(r);
                }
                this.mRankingHelper.sort(this.mNotificationList);
                while (i < N) {
                    NotificationRecord r2 = this.mNotificationList.get(i);
                    if (orderBefore.get(i).equals(r2.getKey()) && visibilities[i] == r2.getPackageVisibilityOverride() && showBadges[i] == r2.canShowBadge() && Objects.equals(channelBefore.get(i), r2.getChannel()) && Objects.equals(groupKeyBefore.get(i), r2.getGroupKey()) && Objects.equals(overridePeopleBefore.get(i), r2.getPeopleOverride()) && Objects.equals(snoozeCriteriaBefore.get(i), r2.getSnoozeCriteria()) && Objects.equals(userSentimentBefore.get(i), Integer.valueOf(r2.getUserSentiment()))) {
                        if (Objects.equals(suppressVisuallyBefore.get(i), Integer.valueOf(r2.getSuppressedVisualEffects()))) {
                            i++;
                        }
                    }
                    this.mHandler.scheduleSendRankingUpdate();
                    return;
                }
            }
        }
    }

    @GuardedBy("mNotificationLock")
    private void recordCallerLocked(NotificationRecord record) {
        if (this.mZenModeHelper.isCall(record)) {
            this.mZenModeHelper.recordCaller(record);
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public void applyZenModeLocked(NotificationRecord record) {
        record.setIntercepted(this.mZenModeHelper.shouldIntercept(record));
        if (record.isIntercepted()) {
            record.setSuppressedVisualEffects(this.mZenModeHelper.getNotificationPolicy().suppressedVisualEffects);
        } else {
            record.setSuppressedVisualEffects(0);
        }
    }

    @GuardedBy("mNotificationLock")
    private int findNotificationRecordIndexLocked(NotificationRecord target) {
        return this.mRankingHelper.indexOf(this.mNotificationList, target);
    }

    /* access modifiers changed from: private */
    public void handleSendRankingUpdate() {
        synchronized (this.mNotificationLock) {
            this.mListeners.notifyRankingUpdateLocked(null);
        }
    }

    private void scheduleListenerHintsChanged(int state) {
        this.mHandler.removeMessages(5);
        this.mHandler.obtainMessage(5, state, 0).sendToTarget();
    }

    private void scheduleInterruptionFilterChanged(int listenerInterruptionFilter) {
        this.mHandler.removeMessages(6);
        this.mHandler.obtainMessage(6, listenerInterruptionFilter, 0).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void handleListenerHintsChanged(int hints) {
        synchronized (this.mNotificationLock) {
            this.mListeners.notifyListenerHintsChangedLocked(hints);
        }
    }

    /* access modifiers changed from: private */
    public void handleListenerInterruptionFilterChanged(int interruptionFilter) {
        synchronized (this.mNotificationLock) {
            this.mListeners.notifyInterruptionFilterChanged(interruptionFilter);
        }
    }

    static int clamp(int x, int low, int high) {
        if (x < low) {
            return low;
        }
        return x > high ? high : x;
    }

    /* access modifiers changed from: package-private */
    public void sendAccessibilityEvent(Notification notification, CharSequence packageName) {
        if (this.mAccessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(64);
            event.setPackageName(packageName);
            event.setClassName(Notification.class.getName());
            event.setParcelableData(notification);
            CharSequence tickerText = notification.tickerText;
            if (!TextUtils.isEmpty(tickerText)) {
                event.getText().add(tickerText);
            }
            this.mAccessibilityManager.sendAccessibilityEvent(event);
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public boolean removeFromNotificationListsLocked(NotificationRecord r) {
        boolean wasPosted = false;
        NotificationRecord findNotificationByListLocked = findNotificationByListLocked(this.mNotificationList, r.getKey());
        NotificationRecord recordInList = findNotificationByListLocked;
        if (findNotificationByListLocked != null) {
            this.mNotificationList.remove(recordInList);
            this.mNotificationsByKey.remove(recordInList.sbn.getKey());
            wasPosted = true;
        }
        while (true) {
            NotificationRecord findNotificationByListLocked2 = findNotificationByListLocked(this.mEnqueuedNotifications, r.getKey());
            NotificationRecord recordInList2 = findNotificationByListLocked2;
            if (findNotificationByListLocked2 == null) {
                return wasPosted;
            }
            this.mEnqueuedNotifications.remove(recordInList2);
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public void cancelNotificationLocked(NotificationRecord r, boolean sendDelete, int reason, boolean wasPosted, String listenerName) {
        cancelNotificationLocked(r, sendDelete, reason, -1, -1, wasPosted, listenerName);
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public void cancelNotificationLocked(NotificationRecord r, boolean sendDelete, int reason, int rank, int count, boolean wasPosted, String listenerName) {
        int i;
        final NotificationRecord notificationRecord = r;
        int i2 = reason;
        String canceledKey = r.getKey();
        recordCallerLocked(r);
        if (r.getStats().getDismissalSurface() == -1) {
            notificationRecord.recordDismissalSurface(0);
        }
        Slog.i(TAG, "cancelNotificationLocked called,tell the app,reason = " + i2);
        if (sendDelete && r.getNotification().deleteIntent != null) {
            try {
                r.getNotification().deleteIntent.send();
            } catch (PendingIntent.CanceledException ex) {
                Slog.w(TAG, "canceled PendingIntent for " + notificationRecord.sbn.getPackageName(), ex);
            }
        }
        final int notifyType = calculateNotifyType(r);
        if (wasPosted) {
            if (r.getNotification().getSmallIcon() != null) {
                if (i2 != 18) {
                    notificationRecord.isCanceled = true;
                }
                Flog.i(400, "cancelNotificationLocked:" + notificationRecord.sbn.getKey());
                this.mListeners.notifyRemovedLocked(notificationRecord, i2, r.getStats());
                this.mHandler.post(new Runnable() {
                    public void run() {
                        NotificationManagerService.this.mGroupHelper.onNotificationRemoved(notificationRecord.sbn, notifyType);
                    }
                });
            }
            if (canceledKey.equals(this.mSoundNotificationKey)) {
                this.mSoundNotificationKey = null;
                long identity = Binder.clearCallingIdentity();
                try {
                    IRingtonePlayer player = this.mAudioManager.getRingtonePlayer();
                    if (player != null) {
                        player.stopAsync();
                    }
                } catch (RemoteException e) {
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
                Binder.restoreCallingIdentity(identity);
            }
            if (canceledKey.equals(this.mVibrateNotificationKey)) {
                this.mVibrateNotificationKey = null;
                long identity2 = Binder.clearCallingIdentity();
                try {
                    this.mVibrator.cancel();
                } finally {
                    Binder.restoreCallingIdentity(identity2);
                }
            }
            this.mLights.remove(canceledKey);
        }
        switch (i2) {
            case 2:
            case 3:
                this.mUsageStats.registerDismissedByUser(notificationRecord);
                break;
            default:
                switch (i2) {
                    case 8:
                    case 9:
                        this.mUsageStats.registerRemovedByApp(notificationRecord);
                        break;
                    case 10:
                    case 11:
                        break;
                }
                this.mUsageStats.registerDismissedByUser(notificationRecord);
                break;
        }
        Flog.i(400, "cancelNotificationLocked,remove =" + notificationRecord.sbn.getPackageName() + ", key:" + r.getKey());
        String groupKey = r.getGroupKey();
        NotificationRecord groupSummary = this.mSummaryByGroupKey.get(groupKey);
        if (groupSummary != null && groupSummary.getKey().equals(canceledKey)) {
            this.mSummaryByGroupKey.remove(groupKey);
        }
        ArrayMap<String, String> summaries = this.mAutobundledSummaries.get(Integer.valueOf(notificationRecord.sbn.getUserId()));
        String notifyKey = this.mGroupHelper.getUnGroupKey(notificationRecord.sbn.getPackageName(), notifyType);
        if (summaries != null && notificationRecord.sbn.getKey().equals(summaries.get(notifyKey))) {
            summaries.remove(notifyKey);
        }
        this.mArchive.record(notificationRecord.sbn);
        long now = System.currentTimeMillis();
        LogMaker logMaker = notificationRecord.getLogMaker(now).setCategory(128).setType(5).setSubtype(i2);
        if (rank != -1) {
            i = count;
            if (i != -1) {
                String str = groupKey;
                logMaker.addTaggedData(798, Integer.valueOf(rank)).addTaggedData(1395, Integer.valueOf(count));
                MetricsLogger.action(logMaker);
                LogMaker logMaker2 = logMaker;
                long j = now;
                String str2 = notifyKey;
                ArrayMap<String, String> arrayMap = summaries;
                NotificationRecord notificationRecord2 = groupSummary;
                EventLogTags.writeNotificationCanceled(canceledKey, i2, notificationRecord.getLifespanMs(now), notificationRecord.getFreshnessMs(now), notificationRecord.getExposureMs(now), rank, i, listenerName);
            }
        } else {
            i = count;
        }
        String str3 = groupKey;
        MetricsLogger.action(logMaker);
        LogMaker logMaker22 = logMaker;
        long j2 = now;
        String str22 = notifyKey;
        ArrayMap<String, String> arrayMap2 = summaries;
        NotificationRecord notificationRecord22 = groupSummary;
        EventLogTags.writeNotificationCanceled(canceledKey, i2, notificationRecord.getLifespanMs(now), notificationRecord.getFreshnessMs(now), notificationRecord.getExposureMs(now), rank, i, listenerName);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00d7  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0120  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0157  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0114 A[EDGE_INSN: B:82:0x0114->B:66:0x0114 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0155 A[EDGE_INSN: B:85:0x0155->B:79:0x0155 ?: BREAK  , SYNTHETIC] */
    @VisibleForTesting
    public void updateUriPermissions(NotificationRecord newRecord, NotificationRecord oldRecord, String targetPkg, int targetUserId) {
        IBinder permissionOwner;
        int i;
        int i2;
        int i3;
        int i4;
        NotificationRecord notificationRecord = newRecord;
        NotificationRecord notificationRecord2 = oldRecord;
        String key = notificationRecord != null ? newRecord.getKey() : oldRecord.getKey();
        if (DBG) {
            Slog.d(TAG, key + ": updating permissions");
        }
        ArraySet<Uri> newUris = notificationRecord != null ? newRecord.getGrantableUris() : null;
        ArraySet<Uri> oldUris = notificationRecord2 != null ? oldRecord.getGrantableUris() : null;
        if (newUris != null || oldUris != null) {
            IBinder permissionOwner2 = null;
            if (notificationRecord != null && 0 == 0) {
                permissionOwner2 = notificationRecord.permissionOwner;
            }
            if (notificationRecord2 != null && permissionOwner2 == null) {
                permissionOwner2 = notificationRecord2.permissionOwner;
            }
            IBinder permissionOwner3 = permissionOwner2;
            if (newUris != null && permissionOwner3 == null) {
                try {
                    if (DBG) {
                        Slog.d(TAG, key + ": creating owner");
                    }
                    permissionOwner3 = this.mAm.newUriPermissionOwner("NOTIF:" + key);
                } catch (RemoteException e) {
                }
            }
            if (newUris == null && permissionOwner3 != null) {
                long ident = Binder.clearCallingIdentity();
                try {
                    if (DBG) {
                        Slog.d(TAG, key + ": destroying owner");
                    }
                    this.mAm.revokeUriPermissionFromOwner(permissionOwner3, null, -1, UserHandle.getUserId(oldRecord.getUid()));
                    permissionOwner = null;
                } catch (RemoteException e2) {
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
                i = 0;
                if (newUris != null && permissionOwner != null) {
                    i3 = 0;
                    while (true) {
                        i4 = i3;
                        if (i4 < newUris.size()) {
                            break;
                        }
                        Uri uri = newUris.valueAt(i4);
                        if (oldUris == null || !oldUris.contains(uri)) {
                            if (DBG) {
                                Slog.d(TAG, key + ": granting " + uri);
                            }
                            grantUriPermission(permissionOwner, uri, newRecord.getUid(), targetPkg, targetUserId);
                        }
                        i3 = i4 + 1;
                    }
                }
                if (oldUris != null && permissionOwner != null) {
                    while (true) {
                        i2 = i;
                        if (i2 < oldUris.size()) {
                            break;
                        }
                        Uri uri2 = oldUris.valueAt(i2);
                        if (newUris == null || !newUris.contains(uri2)) {
                            if (DBG) {
                                Slog.d(TAG, key + ": revoking " + uri2);
                            }
                            revokeUriPermission(permissionOwner, uri2, oldRecord.getUid());
                        }
                        i = i2 + 1;
                    }
                }
                if (notificationRecord != null) {
                    notificationRecord.permissionOwner = permissionOwner;
                }
            }
            permissionOwner = permissionOwner3;
            i = 0;
            i3 = 0;
            while (true) {
                i4 = i3;
                if (i4 < newUris.size()) {
                }
                i3 = i4 + 1;
            }
            while (true) {
                i2 = i;
                if (i2 < oldUris.size()) {
                }
                i = i2 + 1;
            }
            if (notificationRecord != null) {
            }
        }
    }

    private void grantUriPermission(IBinder owner, Uri uri, int sourceUid, String targetPkg, int targetUserId) {
        if (uri != null && "content".equals(uri.getScheme())) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mAm.grantUriPermissionFromOwner(owner, sourceUid, targetPkg, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(sourceUid)), targetUserId);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void revokeUriPermission(IBinder owner, Uri uri, int sourceUid) {
        if (uri != null && "content".equals(uri.getScheme())) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mAm.revokeUriPermissionFromOwner(owner, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(sourceUid)));
            } catch (RemoteException e) {
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelNotification(int callingUid, int callingPid, String pkg, String tag, int id, int mustHaveFlags, int mustNotHaveFlags, boolean sendDelete, int userId, int reason, ManagedServices.ManagedServiceInfo listener) {
        cancelNotification(callingUid, callingPid, pkg, tag, id, mustHaveFlags, mustNotHaveFlags, sendDelete, userId, reason, -1, -1, listener);
    }

    /* access modifiers changed from: package-private */
    public void cancelNotification(int callingUid, int callingPid, String pkg, String tag, int id, int mustHaveFlags, int mustNotHaveFlags, boolean sendDelete, int userId, int reason, int rank, int count, ManagedServices.ManagedServiceInfo listener) {
        final ManagedServices.ManagedServiceInfo managedServiceInfo = listener;
        final int i = callingUid;
        final int i2 = callingPid;
        final String str = pkg;
        final int i3 = id;
        final String str2 = tag;
        final int i4 = userId;
        final int i5 = mustHaveFlags;
        final int i6 = mustNotHaveFlags;
        final int i7 = reason;
        final boolean z = sendDelete;
        final int i8 = rank;
        AnonymousClass15 r16 = r0;
        WorkerHandler workerHandler = this.mHandler;
        final int i9 = count;
        AnonymousClass15 r0 = new Runnable() {
            /* JADX WARNING: Code restructure failed: missing block: B:28:0x00da, code lost:
                return;
             */
            public void run() {
                String listenerName = managedServiceInfo == null ? null : managedServiceInfo.component.toShortString();
                EventLogTags.writeNotificationCancel(i, i2, str, i3, str2, i4, i5, i6, i7, listenerName);
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationRecord r = NotificationManagerService.this.findNotificationLocked(str, str2, i3, i4);
                    if (r != null) {
                        if (i7 == 1) {
                            NotificationManagerService.this.mUsageStats.registerClickedByUser(r);
                        }
                        if ((r.getNotification().flags & i5) == i5) {
                            if ((r.getNotification().flags & i6) == 0) {
                                Flog.i(400, "cancelNotification,cancelNotificationLocked,callingUid = " + i + ",callingPid = " + i2);
                                NotificationManagerService.this.cancelNotificationLocked(r, z, i7, i8, i9, NotificationManagerService.this.removeFromNotificationListsLocked(r), listenerName);
                                NotificationManagerService.this.cancelGroupChildrenLocked(r, i, i2, listenerName, z, null);
                                NotificationManagerService.this.updateLightsLocked();
                            }
                        }
                    } else {
                        Flog.i(400, "cancelNotification,r: null");
                        if (i7 != 18 && NotificationManagerService.this.mSnoozeHelper.cancel(i4, str, str2, i3)) {
                            NotificationManagerService.this.savePolicyFile();
                        }
                    }
                }
            }
        };
        workerHandler.post(r16);
    }

    private boolean notificationMatchesUserId(NotificationRecord r, int userId) {
        return userId == -1 || r.getUserId() == -1 || r.getUserId() == userId;
    }

    private boolean notificationMatchesCurrentProfiles(NotificationRecord r, int userId) {
        return notificationMatchesUserId(r, userId) || this.mUserProfiles.isCurrentProfile(r.getUserId());
    }

    /* access modifiers changed from: package-private */
    public void cancelAllNotificationsInt(int callingUid, int callingPid, String pkg, String channelId, int mustHaveFlags, int mustNotHaveFlags, boolean doit, int userId, int reason, ManagedServices.ManagedServiceInfo listener) {
        WorkerHandler workerHandler = this.mHandler;
        final ManagedServices.ManagedServiceInfo managedServiceInfo = listener;
        final int i = callingUid;
        final int i2 = callingPid;
        final String str = pkg;
        final int i3 = userId;
        final int i4 = mustHaveFlags;
        final int i5 = mustNotHaveFlags;
        final int i6 = reason;
        final boolean z = doit;
        final String str2 = channelId;
        AnonymousClass16 r0 = new Runnable() {
            public void run() {
                String listenerName = managedServiceInfo == null ? null : managedServiceInfo.component.toShortString();
                EventLogTags.writeNotificationCancelAll(i, i2, str, i3, i4, i5, i6, listenerName);
                if (z) {
                    synchronized (NotificationManagerService.this.mNotificationLock) {
                        Flog.i(400, "cancelAllNotificationsInt called");
                        FlagChecker flagChecker = new FlagChecker(i4, i5) {
                            private final /* synthetic */ int f$0;
                            private final /* synthetic */ int f$1;

                            {
                                this.f$0 = r1;
                                this.f$1 = r2;
                            }

                            public final boolean apply(int i) {
                                return NotificationManagerService.AnonymousClass16.lambda$run$0(this.f$0, this.f$1, i);
                            }
                        };
                        NotificationManagerService.this.cancelAllNotificationsByListLocked(NotificationManagerService.this.mNotificationList, i, i2, str, true, str2, flagChecker, false, i3, false, i6, listenerName, true);
                        NotificationManagerService.this.cancelAllNotificationsByListLocked(NotificationManagerService.this.mEnqueuedNotifications, i, i2, str, true, str2, flagChecker, false, i3, false, i6, listenerName, false);
                        NotificationManagerService.this.mSnoozeHelper.cancel(i3, str);
                    }
                }
            }

            static /* synthetic */ boolean lambda$run$0(int mustHaveFlags, int mustNotHaveFlags, int flags) {
                if ((flags & mustHaveFlags) == mustHaveFlags && (flags & mustNotHaveFlags) == 0) {
                    return true;
                }
                return false;
            }
        };
        workerHandler.post(r0);
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public void cancelAllNotificationsByListLocked(ArrayList<NotificationRecord> notificationList, int callingUid, int callingPid, String pkg, boolean nullPkgIndicatesUserSwitch, String channelId, FlagChecker flagChecker, boolean includeCurrentProfiles, int userId, boolean sendDelete, int reason, String listenerName, boolean wasPosted) {
        ArrayList<NotificationRecord> arrayList = notificationList;
        String str = pkg;
        String str2 = channelId;
        int i = userId;
        int i2 = notificationList.size() - 1;
        ArrayList<NotificationRecord> canceledNotifications = null;
        while (true) {
            int i3 = i2;
            if (i3 < 0) {
                break;
            }
            NotificationRecord r = arrayList.get(i3);
            if (!includeCurrentProfiles ? notificationMatchesUserId(r, i) : notificationMatchesCurrentProfiles(r, i)) {
                if (!(nullPkgIndicatesUserSwitch && str == null && r.getUserId() == -1)) {
                    if (flagChecker.apply(r.getFlags()) && ((str == null || r.sbn.getPackageName().equals(str)) && (str2 == null || str2.equals(r.getChannel().getId())))) {
                        if (canceledNotifications == null) {
                            canceledNotifications = new ArrayList<>();
                        }
                        Flog.i(400, "cancelAllNotificationsInt:" + r.sbn.getKey());
                        arrayList.remove(i3);
                        this.mNotificationsByKey.remove(r.getKey());
                        canceledNotifications.add(r);
                        cancelNotificationLocked(r, sendDelete, reason, wasPosted, listenerName);
                    }
                    i2 = i3 - 1;
                }
            }
            FlagChecker flagChecker2 = flagChecker;
            i2 = i3 - 1;
        }
        if (canceledNotifications != null) {
            int M = canceledNotifications.size();
            int i4 = 0;
            while (true) {
                int i5 = i4;
                if (i5 < M) {
                    cancelGroupChildrenLocked(canceledNotifications.get(i5), callingUid, callingPid, listenerName, false, flagChecker);
                    i4 = i5 + 1;
                } else {
                    updateLightsLocked();
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void snoozeNotificationInt(String key, long duration, String snoozeCriterionId, ManagedServices.ManagedServiceInfo listener) {
        String listenerName = listener == null ? null : listener.component.toShortString();
        if ((duration > 0 || snoozeCriterionId != null) && key != null) {
            if (DBG) {
                Slog.d(TAG, String.format("snooze event(%s, %d, %s, %s)", new Object[]{key, Long.valueOf(duration), snoozeCriterionId, listenerName}));
            }
            WorkerHandler workerHandler = this.mHandler;
            SnoozeNotificationRunnable snoozeNotificationRunnable = new SnoozeNotificationRunnable(key, duration, snoozeCriterionId);
            workerHandler.post(snoozeNotificationRunnable);
        }
    }

    /* access modifiers changed from: package-private */
    public void unsnoozeNotificationInt(String key, ManagedServices.ManagedServiceInfo listener) {
        String listenerName = listener == null ? null : listener.component.toShortString();
        if (DBG) {
            Slog.d(TAG, String.format("unsnooze event(%s, %s)", new Object[]{key, listenerName}));
        }
        this.mSnoozeHelper.repost(key);
        savePolicyFile();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mNotificationLock")
    public void cancelAllLocked(int callingUid, int callingPid, int userId, int reason, ManagedServices.ManagedServiceInfo listener, boolean includeCurrentProfiles) {
        WorkerHandler workerHandler = this.mHandler;
        final ManagedServices.ManagedServiceInfo managedServiceInfo = listener;
        final int i = callingUid;
        final int i2 = callingPid;
        final int i3 = userId;
        final int i4 = reason;
        final boolean z = includeCurrentProfiles;
        AnonymousClass17 r0 = new Runnable() {
            public void run() {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    String listenerName = managedServiceInfo == null ? null : managedServiceInfo.component.toShortString();
                    EventLogTags.writeNotificationCancelAll(i, i2, null, i3, 0, 0, i4, listenerName);
                    Flog.i(400, "cancelAllLocked called,callingUid = " + i + ",callingPid = " + i2);
                    $$Lambda$NotificationManagerService$17$Hl56UaJa0DLooMsM68orQNF1Y r18 = $$Lambda$NotificationManagerService$17$Hl56UaJa0DLooMsM68orQNF1Y.INSTANCE;
                    NotificationManagerService.this.cancelAllNotificationsByListLocked(NotificationManagerService.this.mNotificationList, i, i2, null, false, null, r18, z, i3, true, i4, listenerName, true);
                    NotificationManagerService.this.cancelAllNotificationsByListLocked(NotificationManagerService.this.mEnqueuedNotifications, i, i2, null, false, null, r18, z, i3, true, i4, listenerName, false);
                    NotificationManagerService.this.mSnoozeHelper.cancel(i3, z);
                }
            }

            static /* synthetic */ boolean lambda$run$0(int flags) {
                if ((flags & 34) != 0) {
                    return false;
                }
                return true;
            }
        };
        workerHandler.post(r0);
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public void cancelGroupChildrenLocked(NotificationRecord r, int callingUid, int callingPid, String listenerName, boolean sendDelete, FlagChecker flagChecker) {
        if (r.getNotification().isGroupSummary()) {
            NotificationRecord notificationRecord = r;
            if (notificationRecord.sbn.getPackageName() == null) {
                Flog.e(400, "No package for group summary: " + notificationRecord.getKey());
                return;
            }
            NotificationRecord notificationRecord2 = notificationRecord;
            int i = callingUid;
            int i2 = callingPid;
            String str = listenerName;
            boolean z = sendDelete;
            FlagChecker flagChecker2 = flagChecker;
            cancelGroupChildrenByListLocked(this.mNotificationList, notificationRecord2, i, i2, str, z, true, flagChecker2);
            cancelGroupChildrenByListLocked(this.mEnqueuedNotifications, notificationRecord2, i, i2, str, z, false, flagChecker2);
        }
    }

    @GuardedBy("mNotificationLock")
    private void cancelGroupChildrenByListLocked(ArrayList<NotificationRecord> notificationList, NotificationRecord parentNotification, int callingUid, int callingPid, String listenerName, boolean sendDelete, boolean wasPosted, FlagChecker flagChecker) {
        int i;
        ArrayList<NotificationRecord> arrayList = notificationList;
        FlagChecker flagChecker2 = flagChecker;
        String pkg = parentNotification.sbn.getPackageName();
        int userId = parentNotification.getUserId();
        int i2 = notificationList.size() - 1;
        while (true) {
            int i3 = i2;
            if (i3 >= 0) {
                NotificationRecord childR = arrayList.get(i3);
                StatusBarNotification childSbn = childR.sbn;
                if (!childSbn.isGroup() || childSbn.getNotification().isGroupSummary() || !childR.getGroupKey().equals(parentNotification.getGroupKey()) || (childR.getFlags() & 98) != 0) {
                    i = i3;
                } else if (flagChecker2 == null || flagChecker2.apply(childR.getFlags())) {
                    StatusBarNotification statusBarNotification = childSbn;
                    i = i3;
                    EventLogTags.writeNotificationCancel(callingUid, callingPid, pkg, childSbn.getId(), childSbn.getTag(), userId, 0, 0, 12, listenerName);
                    arrayList.remove(i);
                    NotificationRecord childR2 = childR;
                    this.mNotificationsByKey.remove(childR2.getKey());
                    cancelNotificationLocked(childR2, sendDelete, 12, wasPosted, listenerName);
                } else {
                    i = i3;
                }
                i2 = i - 1;
                flagChecker2 = flagChecker;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mNotificationLock")
    public void updateLightsLocked() {
        NotificationRecord ledNotification = null;
        int i = this.mLights.size();
        long token = Binder.clearCallingIdentity();
        try {
            int currentUser = ActivityManager.getCurrentUser();
            while (ledNotification == null && i > 0) {
                i--;
                String owner = this.mLights.get(i);
                ledNotification = this.mNotificationsByKey.get(owner);
                if (ledNotification == null) {
                    Slog.wtfStack(TAG, "LED Notification does not exist: " + owner);
                    this.mLights.remove(owner);
                } else if (!(ledNotification.getUser().getIdentifier() == currentUser || ledNotification.getUser().getIdentifier() == -1 || isAFWUserId(ledNotification.getUser().getIdentifier()))) {
                    Slog.d(TAG, "ledNotification is not CurrentUser,AFWuser and AllUser:" + owner);
                    ledNotification = null;
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("updateLightsLocked,mInCall =");
            sb.append(this.mInCall);
            sb.append(",mScreenOn = ");
            sb.append(this.mScreenOn);
            sb.append(",mGameDndStatus = ");
            sb.append(this.mGameDndStatus);
            sb.append(",ledNotification == null?");
            sb.append(ledNotification == null);
            Flog.i(400, sb.toString());
            if (ledNotification == null || this.mInCall || (this.mScreenOn && !this.mGameDndStatus)) {
                Flog.i(400, "updateLightsLocked,turn off notificationLight");
                this.mNotificationLight.turnOff();
                Flog.i(1100, "turn off notificationLight due to incall or screenon");
                updateLight(false, 0, 0);
                return;
            }
            NotificationRecord.Light light = ledNotification.getLight();
            if (light != null && this.mNotificationPulseEnabled) {
                this.mNotificationLight.setFlashing(light.color, 1, light.onMs, light.offMs);
                Flog.i(1100, "set flash led color=0x" + Integer.toHexString(light.color) + ", ledOn:" + light.onMs + ", ledOff:" + light.offMs);
                updateLight(true, light.onMs, light.offMs);
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* access modifiers changed from: protected */
    public int indexOfNotificationLocked(String pkg, String tag, int id, int userId) {
        ArrayList<NotificationRecord> list = this.mNotificationList;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            NotificationRecord r = list.get(i);
            if (notificationMatchesUserId(r, userId) && r.sbn.getId() == id && TextUtils.equals(r.sbn.getTag(), tag) && r.sbn.getPackageName().equals(pkg)) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mNotificationLock")
    public List<NotificationRecord> findGroupNotificationsLocked(String pkg, String groupKey, int userId) {
        List<NotificationRecord> records = new ArrayList<>();
        records.addAll(findGroupNotificationByListLocked(this.mNotificationList, pkg, groupKey, userId));
        records.addAll(findGroupNotificationByListLocked(this.mEnqueuedNotifications, pkg, groupKey, userId));
        return records;
    }

    @GuardedBy("mNotificationLock")
    private List<NotificationRecord> findGroupNotificationByListLocked(ArrayList<NotificationRecord> list, String pkg, String groupKey, int userId) {
        List<NotificationRecord> records = new ArrayList<>();
        int len = list.size();
        for (int i = 0; i < len; i++) {
            NotificationRecord r = list.get(i);
            if (notificationMatchesUserId(r, userId) && r.getGroupKey().equals(groupKey) && r.sbn.getPackageName().equals(pkg)) {
                records.add(r);
            }
        }
        return records;
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public NotificationRecord findNotificationByKeyLocked(String key) {
        NotificationRecord findNotificationByListLocked = findNotificationByListLocked(this.mNotificationList, key);
        NotificationRecord r = findNotificationByListLocked;
        if (findNotificationByListLocked != null) {
            return r;
        }
        NotificationRecord findNotificationByListLocked2 = findNotificationByListLocked(this.mEnqueuedNotifications, key);
        NotificationRecord r2 = findNotificationByListLocked2;
        if (findNotificationByListLocked2 != null) {
            return r2;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mNotificationLock")
    public NotificationRecord findNotificationLocked(String pkg, String tag, int id, int userId) {
        NotificationRecord findNotificationByListLocked = findNotificationByListLocked(this.mNotificationList, pkg, tag, id, userId);
        NotificationRecord r = findNotificationByListLocked;
        if (findNotificationByListLocked != null) {
            return r;
        }
        NotificationRecord findNotificationByListLocked2 = findNotificationByListLocked(this.mEnqueuedNotifications, pkg, tag, id, userId);
        NotificationRecord r2 = findNotificationByListLocked2;
        if (findNotificationByListLocked2 != null) {
            return r2;
        }
        return null;
    }

    @GuardedBy("mNotificationLock")
    public NotificationRecord findNotificationByListLocked(ArrayList<NotificationRecord> list, String pkg, String tag, int id, int userId) {
        int len = list.size();
        for (int i = 0; i < len; i++) {
            NotificationRecord r = list.get(i);
            if (notificationMatchesUserId(r, userId) && r.sbn.getId() == id && TextUtils.equals(r.sbn.getTag(), tag) && r.sbn.getPackageName().equals(pkg)) {
                return r;
            }
        }
        return null;
    }

    @GuardedBy("mNotificationLock")
    public NotificationRecord findNotificationByListLocked(ArrayList<NotificationRecord> list, String key) {
        int N = list.size();
        for (int i = 0; i < N; i++) {
            if (key.equals(list.get(i).getKey())) {
                return list.get(i);
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mNotificationLock")
    public int indexOfNotificationLocked(String key) {
        int N = this.mNotificationList.size();
        for (int i = 0; i < N; i++) {
            if (key.equals(this.mNotificationList.get(i).getKey())) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void hideNotificationsForPackages(String[] pkgs) {
        synchronized (this.mNotificationLock) {
            List<String> pkgList = Arrays.asList(pkgs);
            List<NotificationRecord> changedNotifications = new ArrayList<>();
            int numNotifications = this.mNotificationList.size();
            for (int i = 0; i < numNotifications; i++) {
                NotificationRecord rec = this.mNotificationList.get(i);
                if (pkgList.contains(rec.sbn.getPackageName())) {
                    rec.setHidden(true);
                    changedNotifications.add(rec);
                }
            }
            this.mListeners.notifyHiddenLocked(changedNotifications);
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void unhideNotificationsForPackages(String[] pkgs) {
        synchronized (this.mNotificationLock) {
            List<String> pkgList = Arrays.asList(pkgs);
            List<NotificationRecord> changedNotifications = new ArrayList<>();
            int numNotifications = this.mNotificationList.size();
            for (int i = 0; i < numNotifications; i++) {
                NotificationRecord rec = this.mNotificationList.get(i);
                if (pkgList.contains(rec.sbn.getPackageName())) {
                    rec.setHidden(false);
                    changedNotifications.add(rec);
                }
            }
            this.mListeners.notifyUnhiddenLocked(changedNotifications);
        }
    }

    /* access modifiers changed from: private */
    public void updateNotificationPulse() {
        synchronized (this.mNotificationLock) {
            updateLightsLocked();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCallingUidSystem() {
        return Binder.getCallingUid() == 1000;
    }

    /* access modifiers changed from: protected */
    public boolean isUidSystemOrPhone(int uid) {
        int appid = UserHandle.getAppId(uid);
        return appid == 1000 || appid == 1001 || uid == 0;
    }

    /* access modifiers changed from: protected */
    public boolean isCallerSystemOrPhone() {
        return isUidSystemOrPhone(Binder.getCallingUid());
    }

    /* access modifiers changed from: private */
    public void checkCallerIsSystemOrShell() {
        if (Binder.getCallingUid() != 2000) {
            checkCallerIsSystem();
        }
    }

    /* access modifiers changed from: private */
    public void checkCallerIsSystem() {
        if (!isCallerSystemOrPhone()) {
            throw new SecurityException("Disallowed call for uid " + Binder.getCallingUid());
        }
    }

    /* access modifiers changed from: private */
    public void checkCallerIsSystemOrSameApp(String pkg) {
        if (!isCallerSystemOrPhone()) {
            checkCallerIsSameApp(pkg);
        }
    }

    private boolean isCallerInstantApp(String pkg) {
        if (isCallerSystemOrPhone()) {
            return false;
        }
        this.mAppOps.checkPackage(Binder.getCallingUid(), pkg);
        try {
            ApplicationInfo ai = this.mPackageManager.getApplicationInfo(pkg, 0, UserHandle.getCallingUserId());
            if (ai != null) {
                return ai.isInstantApp();
            }
            throw new SecurityException("Unknown package " + pkg);
        } catch (RemoteException re) {
            throw new SecurityException("Unknown package " + pkg, re);
        }
    }

    /* access modifiers changed from: private */
    public void checkCallerIsSameApp(String pkg) {
        int uid = Binder.getCallingUid();
        try {
            ApplicationInfo ai = this.mPackageManager.getApplicationInfo(pkg, 0, UserHandle.getCallingUserId());
            if (ai == null) {
                throw new SecurityException("Unknown package " + pkg);
            } else if (!UserHandle.isSameApp(ai.uid, uid)) {
                throw new SecurityException("Calling uid " + uid + " gave package " + pkg + " which is owned by uid " + ai.uid);
            }
        } catch (RemoteException re) {
            throw new SecurityException("Unknown package " + pkg + "\n" + re);
        }
    }

    /* access modifiers changed from: private */
    public static String callStateToString(int state) {
        switch (state) {
            case 0:
                return "CALL_STATE_IDLE";
            case 1:
                return "CALL_STATE_RINGING";
            case 2:
                return "CALL_STATE_OFFHOOK";
            default:
                return "CALL_STATE_UNKNOWN_" + state;
        }
    }

    private void listenForCallState() {
        TelephonyManager.from(getContext()).listen(new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                if (NotificationManagerService.this.mCallState != state) {
                    if (NotificationManagerService.DBG) {
                        Slog.d(NotificationManagerService.TAG, "Call state changed: " + NotificationManagerService.callStateToString(state));
                    }
                    int unused = NotificationManagerService.this.mCallState = state;
                }
            }
        }, 32);
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNotificationLock")
    public NotificationRankingUpdate makeRankingUpdateLocked(ManagedServices.ManagedServiceInfo info) {
        NotificationManagerService notificationManagerService = this;
        int N = notificationManagerService.mNotificationList.size();
        ArrayList<String> keys = new ArrayList<>(N);
        ArrayList<String> interceptedKeys = new ArrayList<>(N);
        ArrayList<Integer> importance = new ArrayList<>(N);
        Bundle overrideGroupKeys = new Bundle();
        Bundle visibilityOverrides = new Bundle();
        Bundle suppressedVisualEffects = new Bundle();
        Bundle explanation = new Bundle();
        Bundle channels = new Bundle();
        Bundle overridePeople = new Bundle();
        Bundle snoozeCriteria = new Bundle();
        Bundle showBadge = new Bundle();
        Bundle userSentiment = new Bundle();
        Bundle hidden = new Bundle();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= N) {
                break;
            }
            int N2 = N;
            NotificationRecord record = notificationManagerService.mNotificationList.get(i2);
            int i3 = i2;
            Bundle hidden2 = hidden;
            if (!notificationManagerService.isVisibleToListener(record.sbn, info)) {
                hidden = hidden2;
            } else {
                String key = record.sbn.getKey();
                keys.add(key);
                importance.add(Integer.valueOf(record.getImportance()));
                if (record.getImportanceExplanation() != null) {
                    explanation.putCharSequence(key, record.getImportanceExplanation());
                }
                if (record.isIntercepted()) {
                    interceptedKeys.add(key);
                }
                suppressedVisualEffects.putInt(key, record.getSuppressedVisualEffects());
                if (record.getPackageVisibilityOverride() != -1000) {
                    visibilityOverrides.putInt(key, record.getPackageVisibilityOverride());
                }
                overrideGroupKeys.putString(key, record.sbn.getOverrideGroupKey());
                channels.putParcelable(key, record.getChannel());
                overridePeople.putStringArrayList(key, record.getPeopleOverride());
                snoozeCriteria.putParcelableArrayList(key, record.getSnoozeCriteria());
                showBadge.putBoolean(key, record.canShowBadge());
                userSentiment.putInt(key, record.getUserSentiment());
                hidden = hidden2;
                hidden.putBoolean(key, record.isHidden());
            }
            i = i3 + 1;
            N = N2;
            notificationManagerService = this;
        }
        int M = keys.size();
        String[] keysAr = (String[]) keys.toArray(new String[M]);
        String[] interceptedKeysAr = (String[]) interceptedKeys.toArray(new String[interceptedKeys.size()]);
        int[] importanceAr = new int[M];
        int i4 = 0;
        while (true) {
            ArrayList<String> keys2 = keys;
            int i5 = i4;
            if (i5 < M) {
                importanceAr[i5] = importance.get(i5).intValue();
                i4 = i5 + 1;
                keys = keys2;
                M = M;
            } else {
                Bundle userSentiment2 = userSentiment;
                Bundle showBadge2 = showBadge;
                Bundle snoozeCriteria2 = snoozeCriteria;
                Bundle overridePeople2 = overridePeople;
                Bundle channels2 = channels;
                Bundle bundle = explanation;
                Bundle bundle2 = suppressedVisualEffects;
                Bundle bundle3 = visibilityOverrides;
                NotificationRankingUpdate notificationRankingUpdate = new NotificationRankingUpdate(keysAr, interceptedKeysAr, visibilityOverrides, suppressedVisualEffects, importanceAr, explanation, overrideGroupKeys, channels2, overridePeople2, snoozeCriteria2, showBadge2, userSentiment2, hidden);
                return notificationRankingUpdate;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasCompanionDevice(ManagedServices.ManagedServiceInfo info) {
        if (this.mCompanionManager == null) {
            this.mCompanionManager = getCompanionManager();
        }
        if (this.mCompanionManager == null) {
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            if (!ArrayUtils.isEmpty(this.mCompanionManager.getAssociations(info.component.getPackageName(), info.userid))) {
                Binder.restoreCallingIdentity(identity);
                return true;
            }
        } catch (SecurityException e) {
        } catch (RemoteException re) {
            Slog.e(TAG, "Cannot reach companion device service", re);
        } catch (Exception e2) {
            Slog.e(TAG, "Cannot verify listener " + info, e2);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return false;
    }

    /* access modifiers changed from: protected */
    public ICompanionDeviceManager getCompanionManager() {
        return ICompanionDeviceManager.Stub.asInterface(ServiceManager.getService("companiondevice"));
    }

    /* access modifiers changed from: private */
    public boolean isVisibleToListener(StatusBarNotification sbn, ManagedServices.ManagedServiceInfo listener) {
        if (!listener.enabledAndUserMatches(sbn.getUserId())) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean isPackageSuspendedForUser(String pkg, int uid) {
        long identity = Binder.clearCallingIdentity();
        try {
            boolean isPackageSuspendedForUser = this.mPackageManager.isPackageSuspendedForUser(pkg, UserHandle.getUserId(uid));
            Binder.restoreCallingIdentity(identity);
            return isPackageSuspendedForUser;
        } catch (RemoteException e) {
            throw new SecurityException("Could not talk to package manager service");
        } catch (IllegalArgumentException e2) {
            Binder.restoreCallingIdentity(identity);
            return false;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean canUseManagedServices(String pkg) {
        boolean canUseManagedServices = !this.mActivityManager.isLowRamDevice() || this.mPackageManagerClient.hasSystemFeature("android.hardware.type.watch");
        for (String whitelisted : getContext().getResources().getStringArray(17235977)) {
            if (whitelisted.equals(pkg)) {
                canUseManagedServices = true;
            }
        }
        return canUseManagedServices;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void simulatePackageSuspendBroadcast(boolean suspend, String pkg) {
        String action;
        Bundle extras = new Bundle();
        extras.putStringArray("android.intent.extra.changed_package_list", new String[]{pkg});
        if (suspend) {
            action = "android.intent.action.PACKAGES_SUSPENDED";
        } else {
            action = "android.intent.action.PACKAGES_UNSUSPENDED";
        }
        Intent intent = new Intent(action);
        intent.putExtras(extras);
        this.mPackageIntentReceiver.onReceive(getContext(), intent);
    }

    private boolean isPushNotification(String opPkg, String pkg, Notification notification) {
        if (notification != null && NOTIFICATION_CENTER_PKG.equals(opPkg)) {
            Bundle bundle = notification.extras;
            if (bundle != null) {
                String targetPkg = bundle.getString(NOTIFICATION_CENTER_ORIGIN_PKG);
                if (targetPkg != null && targetPkg.equals(pkg)) {
                    try {
                        if (AppGlobals.getPackageManager().getApplicationInfo(targetPkg, 0, UserHandle.getCallingUserId()) != null) {
                            return true;
                        }
                    } catch (Exception e) {
                        Slog.w(TAG, "Unknown package pkg:" + targetPkg);
                    }
                }
            }
        }
        return false;
    }

    private boolean disallowInterrupt(Notification notification) {
        if (notification == null || notification.extras == null) {
            return false;
        }
        return notification.extras.getBoolean("hw_btw", false);
    }

    /* access modifiers changed from: private */
    public int calculateNotifyType(NotificationRecord r) {
        int notifyType = r.getImportance() < 2 ? 0 : 1;
        if (r.getNotification().extras == null) {
            return (int) notifyType;
        }
        if (r.getNotification().extras.containsKey("hw_btw")) {
            notifyType = 1 ^ disallowInterrupt(r.getNotification());
        }
        String hwType = r.getNotification().extras.getString("hw_type");
        if (hwType == null) {
            return (int) notifyType;
        }
        if (hwType.equals("type_music")) {
            return 2;
        }
        return notifyType;
    }

    public void setSysMgrCfgMap(ArrayList<RankingHelper.NotificationSysMgrCfg> tempSysMgrCfgList) {
        if (this.mRankingHelper == null) {
            Log.w(TAG, "setSysMgrCfgMap: mRankingHelper is null");
        } else {
            this.mRankingHelper.setSysMgrCfgMap(tempSysMgrCfgList);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCustDialer(String packageName) {
        return false;
    }

    /* access modifiers changed from: protected */
    public String getPackageNameByPid(int pid) {
        if (pid <= 0) {
            return null;
        }
        ActivityManager activityManager = (ActivityManager) getContext().getSystemService("activity");
        if (activityManager == null) {
            return null;
        }
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return null;
        }
        String packageName = null;
        Iterator<ActivityManager.RunningAppProcessInfo> it = appProcesses.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ActivityManager.RunningAppProcessInfo appProcess = it.next();
            if (appProcess.pid == pid) {
                packageName = appProcess.processName;
                break;
            }
        }
        int indexProcessFlag = -1;
        if (packageName != null) {
            indexProcessFlag = packageName.indexOf(58);
        }
        return indexProcessFlag > 0 ? packageName.substring(0, indexProcessFlag) : packageName;
    }
}
