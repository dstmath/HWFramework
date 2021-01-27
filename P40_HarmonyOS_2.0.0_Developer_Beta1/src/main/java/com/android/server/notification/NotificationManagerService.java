package com.android.server.notification;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityThread;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.AutomaticZenRule;
import android.app.IActivityManager;
import android.app.INotificationManager;
import android.app.ITransientNotification;
import android.app.IUriGrantsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Person;
import android.app.RemoteInput;
import android.app.UriGrantsManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.app.backup.BackupManager;
import android.app.role.OnRoleHoldersChangedListener;
import android.app.role.RoleManager;
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
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioManagerInternal;
import android.media.IRingtonePlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.metrics.LogMaker;
import android.net.Uri;
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
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.TransactionTooLargeException;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.rms.HwSysResource;
import android.service.notification.Adjustment;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.INotificationListener;
import android.service.notification.IStatusBarNotificationHolder;
import android.service.notification.NotificationListenerService;
import android.service.notification.NotificationRankingUpdate;
import android.service.notification.NotificationStats;
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
import android.util.IntArray;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.StatsLog;
import android.util.Xml;
import android.util.proto.ProtoOutputStream;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.CollectionUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.FunctionalUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.internal.util.function.TriPredicate;
import com.android.server.DeviceIdleController;
import com.android.server.EventLogTags;
import com.android.server.HwServiceExFactory;
import com.android.server.IoThread;
import com.android.server.LocalServices;
import com.android.server.display.FoldPolicy;
import com.android.server.hdmi.HdmiCecKeycode;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.notification.GroupHelper;
import com.android.server.notification.ManagedServices;
import com.android.server.notification.NotificationManagerService;
import com.android.server.notification.NotificationRecord;
import com.android.server.notification.PreferencesHelper;
import com.android.server.notification.SnoozeHelper;
import com.android.server.notification.ZenModeHelper;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.uri.UriGrantsManagerInternal;
import com.android.server.utils.PriorityDump;
import com.android.server.wm.ActivityTaskManagerInternal;
import com.android.server.wm.WindowManagerInternal;
import com.huawei.android.os.HwVibrator;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import libcore.io.IoUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class NotificationManagerService extends AbsNotificationManager {
    private static final String ACTION_NOTIFICATION_TIMEOUT = (NotificationManagerService.class.getSimpleName() + ".TIMEOUT");
    private static final String ATTR_CHANNEL_NAME = "channelName";
    private static final String ATTR_VERSION = "version";
    static final boolean DBG = Log.isLoggable(TAG, 3);
    private static final int DB_VERSION = 1;
    static final boolean DEBUG_INTERRUPTIVENESS = SystemProperties.getBoolean("debug.notification.interruptiveness", false);
    private static final boolean DEBUG_USAGESTATS = SystemProperties.getBoolean("debug.notification.usagestats", false);
    static final String[] DEFAULT_ALLOWED_ADJUSTMENTS = {"key_contextual_actions", "key_text_replies"};
    static final float DEFAULT_MAX_NOTIFICATION_ENQUEUE_RATE = 5.0f;
    static final int DEFAULT_STREAM_TYPE = 5;
    static final long[] DEFAULT_VIBRATE_PATTERN = {0, 250, 250, 250};
    private static final long DELAY_FOR_ASSISTANT_TIME = 100;
    static final boolean ENABLE_BLOCKED_TOASTS = true;
    public static final boolean ENABLE_CHILD_NOTIFICATIONS = SystemProperties.getBoolean("debug.child_notifs", true);
    private static final int EVENTLOG_ENQUEUE_STATUS_IGNORED = 2;
    private static final int EVENTLOG_ENQUEUE_STATUS_NEW = 0;
    private static final int EVENTLOG_ENQUEUE_STATUS_UPDATE = 1;
    private static final String EXTRA_KEY = "key";
    static final int FINISH_TOKEN_TIMEOUT = 11000;
    private static final boolean HWFLOW;
    private static final String HWSYSTEMMANAGER_PKG = "com.huawei.systemmanager";
    private static final boolean ISCHINA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static final String LOCKSCREEN_ALLOW_SECURE_NOTIFICATIONS_TAG = "allow-secure-notifications-on-lockscreen";
    private static final String LOCKSCREEN_ALLOW_SECURE_NOTIFICATIONS_VALUE = "value";
    static final int LONG_DELAY = 3500;
    static final int MATCHES_CALL_FILTER_CONTACTS_TIMEOUT_MS = 3000;
    static final float MATCHES_CALL_FILTER_TIMEOUT_AFFINITY = 1.0f;
    static final int MAX_PACKAGE_NOTIFICATIONS = 50;
    static final int MESSAGE_DURATION_REACHED = 2;
    static final int MESSAGE_FINISH_TOKEN_TIMEOUT = 7;
    static final int MESSAGE_LISTENER_HINTS_CHANGED = 5;
    static final int MESSAGE_LISTENER_NOTIFICATION_FILTER_CHANGED = 6;
    static final int MESSAGE_ON_PACKAGE_CHANGED = 8;
    private static final int MESSAGE_RANKING_SORT = 1001;
    private static final int MESSAGE_RECONSIDER_RANKING = 1000;
    static final int MESSAGE_SEND_RANKING_UPDATE = 4;
    private static final long MIN_PACKAGE_OVERRATE_LOG_INTERVAL = 5000;
    private static final int MY_PID = Process.myPid();
    private static final int MY_UID = Process.myUid();
    static final String[] NON_BLOCKABLE_DEFAULT_ROLES = {"android.app.role.DIALER", "android.app.role.EMERGENCY"};
    private static final String NOTIFICATION_CENTER_ORIGIN_PKG = "hw_origin_sender_package_name";
    private static final String NOTIFICATION_CENTER_PKG = "com.huawei.android.pushagent";
    private static final long NOTIFICATION_UPDATE_REPORT_INTERVAL = 15000;
    private static final String NOTIFICATION_VIBRATE = "Notification_Vibrate";
    private static final String PUSH_TAG = "NotificationServiceForPush";
    private static final int REQUEST_CODE_TIMEOUT = 1;
    private static final String SCHEME_TIMEOUT = "timeout";
    static final int SHORT_DELAY = 2000;
    static final long SNOOZE_UNTIL_UNSPECIFIED = -1;
    static final String TAG = "NotificationService";
    private static final String TAG_NOTIFICATION_POLICY = "notification-policy";
    private static final String TELECOM_PKG = "com.android.server.telecom";
    static final int VIBRATE_PATTERN_MAXLEN = 17;
    private static final String VIBRATOR_TYPE_HAPTIC_NOTICE = "haptic.notice.";
    private static final IBinder WHITELIST_TOKEN = new Binder();
    protected IHwNotificationManagerServiceEx iHwNotificationManagerServiceEx;
    private AccessibilityManager mAccessibilityManager;
    private ActivityManager mActivityManager;
    private AlarmManager mAlarmManager;
    private TriPredicate<String, Integer, String> mAllowedManagedServicePackages;
    private IActivityManager mAm;
    private AppOpsManager mAppOps;
    private UsageStatsManagerInternal mAppUsageStats;
    private Archive mArchive;
    private NotificationAssistants mAssistants;
    Light mAttentionLight;
    AudioManager mAudioManager;
    AudioManagerInternal mAudioManagerInternal;
    private int mAutoGroupAtCount;
    @GuardedBy({"mNotificationLock"})
    final ArrayMap<Integer, ArrayMap<String, String>> mAutobundledSummaries = new ArrayMap<>();
    private Binder mCallNotificationToken = null;
    private int mCallState;
    private ICompanionDeviceManager mCompanionManager;
    private ConditionProviders mConditionProviders;
    private IDeviceIdleController mDeviceIdleController;
    private boolean mDisableNotificationEffects;
    private DevicePolicyManagerInternal mDpm;
    private List<ComponentName> mEffectsSuppressors = new ArrayList();
    @GuardedBy({"mNotificationLock"})
    final ArrayList<NotificationRecord> mEnqueuedNotifications = new ArrayList<>();
    private long[] mFallbackVibrationPattern;
    final IBinder mForegroundToken = new Binder();
    protected boolean mGameDndStatus = false;
    private GroupHelper mGroupHelper;
    private WorkerHandler mHandler;
    boolean mHasLight = true;
    protected boolean mInCall = false;
    private AudioAttributes mInCallNotificationAudioAttributes;
    private Uri mInCallNotificationUri;
    private float mInCallNotificationVolume;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.server.notification.NotificationManagerService.AnonymousClass6 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_ON")) {
                NotificationManagerService notificationManagerService = NotificationManagerService.this;
                notificationManagerService.mScreenOn = true;
                notificationManagerService.mGameDndStatus = notificationManagerService.isGameRunningForeground();
                Flog.i(400, "mIntentReceiver-ACTION_SCREEN_ON, mGameDndStatus = " + NotificationManagerService.this.mGameDndStatus);
                NotificationManagerService.this.updateNotificationPulse();
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                NotificationManagerService notificationManagerService2 = NotificationManagerService.this;
                notificationManagerService2.mScreenOn = false;
                notificationManagerService2.mGameDndStatus = false;
                Flog.i(400, "mIntentReceiver-ACTION_SCREEN_OFF");
                NotificationManagerService.this.updateNotificationPulse();
            } else if (action.equals("android.intent.action.PHONE_STATE")) {
                NotificationManagerService.this.mInCall = TelephonyManager.EXTRA_STATE_OFFHOOK.equals(intent.getStringExtra("state"));
                Flog.i(400, "mIntentReceiver-ACTION_PHONE_STATE_CHANGED");
                NotificationManagerService.this.updateNotificationPulse();
            } else if (action.equals("android.intent.action.USER_STOPPED")) {
                Flog.i(400, "mIntentReceiver-ACTION_USER_STOPPED");
                int userHandle = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userHandle >= 0) {
                    NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, null, null, 0, 0, true, userHandle, 6, null);
                }
            } else if (action.equals("android.intent.action.MANAGED_PROFILE_UNAVAILABLE")) {
                int userHandle2 = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userHandle2 >= 0) {
                    NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, null, null, 0, 0, true, userHandle2, 15, null);
                }
            } else if (action.equals("android.intent.action.USER_PRESENT")) {
                if (NotificationManagerService.this.mScreenOn) {
                    NotificationManagerService.this.mNotificationLight.turnOff();
                    StatusBarManagerInternal statusBarManagerInternal = NotificationManagerService.this.mStatusBar;
                    NotificationManagerService notificationManagerService3 = NotificationManagerService.this;
                    notificationManagerService3.mGameDndStatus = notificationManagerService3.isGameRunningForeground();
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
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                NotificationManagerService.this.mUserProfiles.updateCache(context);
                if (!NotificationManagerService.this.mUserProfiles.isManagedProfile(userId)) {
                    Flog.i(400, "mIntentReceiver-ACTION_USER_SWITCHED");
                    NotificationManagerService.this.mSettingsObserver.update(null);
                    NotificationManagerService.this.mConditionProviders.onUserSwitched(userId);
                    NotificationManagerService.this.mListeners.onUserSwitched(userId);
                    NotificationManagerService.this.mZenModeHelper.onUserSwitched(userId);
                    NotificationManagerService.this.mPreferencesHelper.onUserSwitched(userId);
                    NotificationManagerService.this.handleUserSwitchEvents(userId);
                }
                NotificationManagerService.this.mAssistants.onUserSwitched(userId);
                NotificationManagerService.this.stopPlaySound();
            } else if (action.equals("android.intent.action.USER_ADDED")) {
                int userId2 = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                if (userId2 != -10000) {
                    NotificationManagerService.this.mUserProfiles.updateCache(context);
                    if (!NotificationManagerService.this.mUserProfiles.isManagedProfile(userId2)) {
                        NotificationManagerService.this.readDefaultApprovedServices(userId2);
                    }
                }
                Flog.i(400, "mIntentReceiver-ACTION_USER_ADDED");
            } else if (action.equals("android.intent.action.USER_REMOVED")) {
                int userId3 = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                NotificationManagerService.this.mUserProfiles.updateCache(context);
                NotificationManagerService.this.mZenModeHelper.onUserRemoved(userId3);
                NotificationManagerService.this.mPreferencesHelper.onUserRemoved(userId3);
                NotificationManagerService.this.mListeners.onUserRemoved(userId3);
                NotificationManagerService.this.mConditionProviders.onUserRemoved(userId3);
                NotificationManagerService.this.mAssistants.onUserRemoved(userId3);
                NotificationManagerService.this.handleSavePolicyFile();
            } else if (action.equals("android.intent.action.USER_UNLOCKED")) {
                int userId4 = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                NotificationManagerService.this.mUserProfiles.updateCache(context);
                NotificationManagerService.this.mAssistants.onUserUnlocked(userId4);
                if (!NotificationManagerService.this.mUserProfiles.isManagedProfile(userId4)) {
                    NotificationManagerService.this.mConditionProviders.onUserUnlocked(userId4);
                    NotificationManagerService.this.mListeners.onUserUnlocked(userId4);
                    UserInfo userInfo = NotificationManagerService.this.mUm.getUserInfo(userId4);
                    if (userInfo != null && !userInfo.isClonedProfile()) {
                        NotificationManagerService.this.mZenModeHelper.onUserUnlocked(userId4);
                    }
                    NotificationManagerService.this.mPreferencesHelper.onUserUnlocked(userId4);
                }
            }
        }
    };
    private final NotificationManagerInternal mInternalService = new NotificationManagerInternal() {
        /* class com.android.server.notification.NotificationManagerService.AnonymousClass11 */

        @Override // com.android.server.notification.NotificationManagerInternal
        public NotificationChannel getNotificationChannel(String pkg, int uid, String channelId) {
            return NotificationManagerService.this.mPreferencesHelper.getNotificationChannel(pkg, uid, channelId, false);
        }

        @Override // com.android.server.notification.NotificationManagerInternal
        public void enqueueNotification(String pkg, String opPkg, int callingUid, int callingPid, String tag, int id, Notification notification, int userId) {
            NotificationManagerService.this.enqueueNotificationInternal(pkg, opPkg, callingUid, callingPid, tag, id, notification, userId);
        }

        @Override // com.android.server.notification.NotificationManagerInternal
        public void removeForegroundServiceFlagFromNotification(String pkg, int notificationId, int userId) {
            NotificationManagerService.this.checkCallerIsSystem();
            NotificationManagerService.this.mHandler.post(new Runnable(pkg, notificationId, userId) {
                /* class com.android.server.notification.$$Lambda$NotificationManagerService$11$zVdn9N0ybkMxz8xM8Qa1AXowlic */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    NotificationManagerService.AnonymousClass11.this.lambda$removeForegroundServiceFlagFromNotification$0$NotificationManagerService$11(this.f$1, this.f$2, this.f$3);
                }
            });
        }

        public /* synthetic */ void lambda$removeForegroundServiceFlagFromNotification$0$NotificationManagerService$11(String pkg, int notificationId, int userId) {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                List<NotificationRecord> enqueued = NotificationManagerService.this.findNotificationsByListLocked(NotificationManagerService.this.mEnqueuedNotifications, pkg, null, notificationId, userId);
                for (int i = 0; i < enqueued.size(); i++) {
                    removeForegroundServiceFlagLocked(enqueued.get(i));
                }
                NotificationRecord r = NotificationManagerService.this.findNotificationByListLocked(NotificationManagerService.this.mNotificationList, pkg, null, notificationId, userId);
                if (r != null) {
                    removeForegroundServiceFlagLocked(r);
                    NotificationManagerService.this.mRankingHelper.sort(NotificationManagerService.this.mNotificationList);
                    NotificationManagerService.this.mListeners.notifyPostedLocked(r, r);
                }
            }
        }

        @GuardedBy({"mNotificationLock"})
        private void removeForegroundServiceFlagLocked(NotificationRecord r) {
            if (r != null) {
                r.sbn.getNotification().flags = r.mOriginalFlags & -65;
            }
        }
    };
    private int mInterruptionFilter = 0;
    private boolean mIsAutomotive;
    private boolean mIsTelevision;
    private long mLastOverRateLogTime;
    boolean mLightEnabled;
    ArrayList<String> mLights = new ArrayList<>();
    private int mListenerHints;
    private NotificationListeners mListeners;
    private final SparseArray<ArraySet<ComponentName>> mListenersDisablingEffects = new SparseArray<>();
    protected final BroadcastReceiver mLocaleChangeReceiver = new BroadcastReceiver() {
        /* class com.android.server.notification.NotificationManagerService.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                SystemNotificationChannels.createAll(context);
                NotificationManagerService.this.mZenModeHelper.updateDefaultZenRules();
                NotificationManagerService.this.mPreferencesHelper.onLocaleChanged(context, ActivityManager.getCurrentUser());
            }
        }
    };
    private boolean mLockScreenAllowSecureNotifications = true;
    private float mMaxPackageEnqueueRate = DEFAULT_MAX_NOTIFICATION_ENQUEUE_RATE;
    private MetricsLogger mMetricsLogger;
    @VisibleForTesting
    final NotificationDelegate mNotificationDelegate = new NotificationDelegate() {
        /* class com.android.server.notification.NotificationManagerService.AnonymousClass1 */

        @Override // com.android.server.notification.NotificationDelegate
        public void onSetDisabled(int status) {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationManagerService.this.mDisableNotificationEffects = (262144 & status) != 0;
                if (NotificationManagerService.this.disableNotificationEffects(null) != null) {
                    long identity = Binder.clearCallingIdentity();
                    try {
                        if (NotificationManagerService.this.mRingtone != null) {
                            NotificationManagerService.this.mRingtone.stop();
                        }
                        IRingtonePlayer player = NotificationManagerService.this.mAudioManager.getRingtonePlayer();
                        if (player != null) {
                            player.stopAsync();
                        }
                    } catch (RemoteException e) {
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                    long identity2 = Binder.clearCallingIdentity();
                    try {
                        HwVibrator.stopHwVibrator(Process.myUid(), ActivityThread.currentPackageName(), NotificationManagerService.this.mToken, NotificationManagerService.NOTIFICATION_VIBRATE);
                    } finally {
                        Binder.restoreCallingIdentity(identity2);
                    }
                }
            }
        }

        @Override // com.android.server.notification.NotificationDelegate
        public void onClearAll(int callingUid, int callingPid, int userId) {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationManagerService.this.cancelAllLocked(callingUid, callingPid, userId, 3, null, true);
            }
        }

        @Override // com.android.server.notification.NotificationDelegate
        public void onNotificationClick(int callingUid, int callingPid, String key, NotificationVisibility nv) {
            NotificationManagerService.this.exitIdle();
            synchronized (NotificationManagerService.this.mNotificationLock) {
                Flog.i(400, "onNotificationClick called");
                NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r == null) {
                    Slog.w(NotificationManagerService.TAG, "No notification with key: " + key);
                    return;
                }
                long now = System.currentTimeMillis();
                MetricsLogger.action(r.getItemLogMaker().setType(4).addTaggedData(798, Integer.valueOf(nv.rank)).addTaggedData(1395, Integer.valueOf(nv.count)));
                EventLogTags.writeNotificationClicked(key, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now), nv.rank, nv.count);
                StatusBarNotification sbn = r.sbn;
                NotificationManagerService.this.cancelNotification(callingUid, callingPid, sbn.getPackageName(), sbn.getTag(), sbn.getId(), 16, 64, false, r.getUserId(), 1, nv.rank, nv.count, null);
                nv.recycle();
                NotificationManagerService.this.reportUserInteraction(r);
            }
        }

        @Override // com.android.server.notification.NotificationDelegate
        public void onNotificationActionClick(int callingUid, int callingPid, String key, int actionIndex, Notification.Action action, NotificationVisibility nv, boolean generatedByAssistant) {
            NotificationManagerService.this.exitIdle();
            synchronized (NotificationManagerService.this.mNotificationLock) {
                try {
                    NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(key);
                    if (r == null) {
                        Slog.w(NotificationManagerService.TAG, "No notification with key: " + key);
                        return;
                    }
                    long now = System.currentTimeMillis();
                    int i = 1;
                    LogMaker addTaggedData = r.getLogMaker(now).setCategory(129).setType(4).setSubtype(actionIndex).addTaggedData(798, Integer.valueOf(nv.rank)).addTaggedData(1395, Integer.valueOf(nv.count)).addTaggedData(1601, Integer.valueOf(action.isContextual() ? 1 : 0));
                    if (!generatedByAssistant) {
                        i = 0;
                    }
                    MetricsLogger.action(addTaggedData.addTaggedData(1600, Integer.valueOf(i)).addTaggedData(1629, Integer.valueOf(nv.location.toMetricsEventEnum())));
                    EventLogTags.writeNotificationActionClicked(key, actionIndex, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now), nv.rank, nv.count);
                    nv.recycle();
                    NotificationManagerService.this.reportUserInteraction(r);
                    NotificationManagerService.this.mAssistants.notifyAssistantActionClicked(r.sbn, actionIndex, action, generatedByAssistant);
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            }
        }

        @Override // com.android.server.notification.NotificationDelegate
        public void onNotificationClear(int callingUid, int callingPid, String pkg, String tag, int id, int userId, String key, int dismissalSurface, int dismissalSentiment, NotificationVisibility nv) {
            Throwable th;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                try {
                    try {
                        NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(key);
                        if (r != null) {
                            try {
                                r.recordDismissalSurface(dismissalSurface);
                                r.recordDismissalSentiment(dismissalSentiment);
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                        NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 66, true, userId, 2, nv.rank, nv.count, null);
                        nv.recycle();
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    throw th;
                }
            }
        }

        @Override // com.android.server.notification.NotificationDelegate
        public void onPanelRevealed(boolean clearEffects, int items) {
            MetricsLogger.visible(NotificationManagerService.this.getContext(), 127);
            MetricsLogger.histogram(NotificationManagerService.this.getContext(), "note_load", items);
            EventLogTags.writeNotificationPanelRevealed(items);
            if (clearEffects) {
                clearEffects();
            }
        }

        @Override // com.android.server.notification.NotificationDelegate
        public void onPanelHidden() {
            MetricsLogger.hidden(NotificationManagerService.this.getContext(), 127);
            EventLogTags.writeNotificationPanelHidden();
        }

        @Override // com.android.server.notification.NotificationDelegate
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

        @Override // com.android.server.notification.NotificationDelegate
        public void onNotificationError(int callingUid, int callingPid, String pkg, String tag, int id, int uid, int initialPid, String message, int userId) {
            boolean fgService;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = NotificationManagerService.this.findNotificationLocked(pkg, tag, id, userId);
                fgService = (r == null || (r.getNotification().flags & 64) == 0) ? false : true;
            }
            NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 0, false, userId, 4, null);
            if (fgService) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(uid, initialPid, pkg, tag, id, message) {
                    /* class com.android.server.notification.$$Lambda$NotificationManagerService$1$xhbVsydQBNNW5m21WjLTPrHQojA */
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ int f$2;
                    private final /* synthetic */ String f$3;
                    private final /* synthetic */ String f$4;
                    private final /* synthetic */ int f$5;
                    private final /* synthetic */ String f$6;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                        this.f$5 = r6;
                        this.f$6 = r7;
                    }

                    public final void runOrThrow() {
                        NotificationManagerService.AnonymousClass1.this.lambda$onNotificationError$0$NotificationManagerService$1(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onNotificationError$0$NotificationManagerService$1(int uid, int initialPid, String pkg, String tag, int id, String message) throws Exception {
            IActivityManager iActivityManager = NotificationManagerService.this.mAm;
            iActivityManager.crashApplication(uid, initialPid, pkg, -1, "Bad notification(tag=" + tag + ", id=" + id + ") posted from package " + pkg + ", crashing app(uid=" + uid + ", pid=" + initialPid + "): " + message, true);
        }

        @Override // com.android.server.notification.NotificationDelegate
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
                        }
                        boolean isHun = true;
                        r.setVisibility(true, nv.rank, nv.count);
                        if (nv.location != NotificationVisibility.NotificationLocation.LOCATION_FIRST_HEADS_UP) {
                            isHun = false;
                        }
                        if (isHun || r.hasBeenVisiblyExpanded()) {
                            NotificationManagerService.this.logSmartSuggestionsVisible(r, nv.location.toMetricsEventEnum());
                        }
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

        @Override // com.android.server.notification.NotificationDelegate
        public void onNotificationExpansionChanged(String key, boolean userAction, boolean expanded, int notificationLocation) {
            int i;
            Flog.i(400, "onNotificationExpansionChanged called");
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r != null) {
                    r.stats.onExpansionChanged(userAction, expanded);
                    if (r.hasBeenVisiblyExpanded()) {
                        NotificationManagerService.this.logSmartSuggestionsVisible(r, notificationLocation);
                    }
                    if (userAction) {
                        LogMaker itemLogMaker = r.getItemLogMaker();
                        if (expanded) {
                            i = 3;
                        } else {
                            i = 14;
                        }
                        MetricsLogger.action(itemLogMaker.setType(i));
                    }
                    if (expanded && userAction) {
                        r.recordExpanded();
                        NotificationManagerService.this.reportUserInteraction(r);
                    }
                    NotificationManagerService.this.mAssistants.notifyAssistantExpansionChangedLocked(r.sbn, userAction, expanded);
                }
            }
        }

        @Override // com.android.server.notification.NotificationDelegate
        public void onNotificationDirectReplied(String key) {
            NotificationManagerService.this.exitIdle();
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r != null) {
                    r.recordDirectReplied();
                    NotificationManagerService.this.mMetricsLogger.write(r.getLogMaker().setCategory(1590).setType(4));
                    NotificationManagerService.this.reportUserInteraction(r);
                    NotificationManagerService.this.mAssistants.notifyAssistantNotificationDirectReplyLocked(r.sbn);
                }
            }
        }

        @Override // com.android.server.notification.NotificationDelegate
        public void onNotificationSmartSuggestionsAdded(String key, int smartReplyCount, int smartActionCount, boolean generatedByAssistant, boolean editBeforeSending) {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r != null) {
                    r.setNumSmartRepliesAdded(smartReplyCount);
                    r.setNumSmartActionsAdded(smartActionCount);
                    r.setSuggestionsGeneratedByAssistant(generatedByAssistant);
                    r.setEditChoicesBeforeSending(editBeforeSending);
                }
            }
        }

        @Override // com.android.server.notification.NotificationDelegate
        public void onNotificationSmartReplySent(String key, int replyIndex, CharSequence reply, int notificationLocation, boolean modifiedBeforeSending) {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r != null) {
                    int i = 1;
                    LogMaker addTaggedData = r.getLogMaker().setCategory(1383).setSubtype(replyIndex).addTaggedData(1600, Integer.valueOf(r.getSuggestionsGeneratedByAssistant() ? 1 : 0)).addTaggedData(1629, Integer.valueOf(notificationLocation)).addTaggedData(1647, Integer.valueOf(r.getEditChoicesBeforeSending() ? 1 : 0));
                    if (!modifiedBeforeSending) {
                        i = 0;
                    }
                    NotificationManagerService.this.mMetricsLogger.write(addTaggedData.addTaggedData(1648, Integer.valueOf(i)));
                    NotificationManagerService.this.reportUserInteraction(r);
                    NotificationManagerService.this.mAssistants.notifyAssistantSuggestedReplySent(r.sbn, reply, r.getSuggestionsGeneratedByAssistant());
                }
            }
        }

        @Override // com.android.server.notification.NotificationDelegate
        public void onNotificationSettingsViewed(String key) {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r != null) {
                    r.recordViewedSettings();
                }
            }
        }

        @Override // com.android.server.notification.NotificationDelegate
        public void onNotificationBubbleChanged(String key, boolean isBubble) {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r != null) {
                    StatusBarNotification n = r.sbn;
                    int callingUid = n.getUid();
                    String pkg = n.getPackageName();
                    if (!isBubble || !NotificationManagerService.this.isNotificationAppropriateToBubble(r, pkg, callingUid, null)) {
                        r.getNotification().flags &= -4097;
                    } else {
                        r.getNotification().flags |= 4096;
                    }
                }
            }
        }
    };
    private boolean mNotificationEffectsEnabledForAutomotive;
    private Light mNotificationLight;
    @GuardedBy({"mNotificationLock"})
    final ArrayList<NotificationRecord> mNotificationList = new ArrayList<>();
    final Object mNotificationLock = new Object();
    boolean mNotificationPulseEnabled;
    private HwSysResource mNotificationResource;
    private final BroadcastReceiver mNotificationTimeoutReceiver = new BroadcastReceiver() {
        /* class com.android.server.notification.NotificationManagerService.AnonymousClass4 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Throwable th;
            NotificationRecord record;
            String action = intent.getAction();
            if (action != null) {
                if (NotificationManagerService.ACTION_NOTIFICATION_TIMEOUT.equals(action)) {
                    synchronized (NotificationManagerService.this.mNotificationLock) {
                        try {
                            try {
                                record = NotificationManagerService.this.findNotificationByKeyLocked(intent.getStringExtra(NotificationManagerService.EXTRA_KEY));
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            throw th;
                        }
                    }
                    if (record != null) {
                        NotificationManagerService.this.cancelNotification(record.sbn.getUid(), record.sbn.getInitialPid(), record.sbn.getPackageName(), record.sbn.getTag(), record.sbn.getId(), 0, 64, true, record.getUserId(), 19, null);
                    }
                }
            }
        }
    };
    @GuardedBy({"mNotificationLock"})
    final ArrayMap<String, NotificationRecord> mNotificationsByKey = new ArrayMap<>();
    private final BroadcastReceiver mPackageIntentReceiver = new BroadcastReceiver() {
        /* class com.android.server.notification.NotificationManagerService.AnonymousClass5 */

        /* JADX DEBUG: Multi-variable search result rejected for r25v0, resolved type: boolean */
        /* JADX DEBUG: Multi-variable search result rejected for r25v1, resolved type: boolean */
        /* JADX DEBUG: Multi-variable search result rejected for r25v2, resolved type: boolean */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            boolean packageChanged;
            boolean packageChanged2;
            boolean unhideNotifications;
            int i;
            boolean unhideNotifications2;
            boolean hideNotifications;
            int[] uidList;
            String[] pkgList;
            int changeUserId;
            boolean removingPackage;
            int changeUserId2;
            boolean removingPackage2;
            int i2;
            int i3;
            int i4;
            String pkgName;
            int i5;
            int uid;
            int[] uidList2;
            boolean cancelNotifications;
            String action = intent.getAction();
            if (action != null) {
                boolean queryRestart = false;
                boolean queryRemove = false;
                boolean packageChanged3 = false;
                boolean cancelNotifications2 = true;
                boolean hideNotifications2 = false;
                boolean unhideNotifications3 = false;
                if (!action.equals("android.intent.action.PACKAGE_ADDED")) {
                    boolean equals = action.equals("android.intent.action.PACKAGE_REMOVED");
                    queryRemove = equals;
                    if (!equals && !action.equals("android.intent.action.PACKAGE_RESTARTED")) {
                        boolean equals2 = action.equals("android.intent.action.PACKAGE_CHANGED");
                        packageChanged3 = equals2;
                        if (!equals2) {
                            boolean equals3 = action.equals("android.intent.action.QUERY_PACKAGE_RESTART");
                            queryRestart = equals3;
                            if (!equals3 && !action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE") && !action.equals("android.intent.action.PACKAGES_SUSPENDED") && !action.equals("android.intent.action.PACKAGES_UNSUSPENDED") && !action.equals("android.intent.action.DISTRACTING_PACKAGES_CHANGED")) {
                                return;
                            }
                        } else {
                            packageChanged = packageChanged3;
                            packageChanged2 = false;
                        }
                    }
                    packageChanged = packageChanged3;
                    packageChanged2 = queryRestart;
                } else {
                    packageChanged = false;
                    packageChanged2 = false;
                }
                int changeUserId3 = intent.getIntExtra("android.intent.extra.user_handle", -1);
                Flog.i(400, "mIntentReceiver-Package changed");
                boolean removingPackage3 = queryRemove && !intent.getBooleanExtra("android.intent.extra.REPLACING", false);
                StringBuilder sb = new StringBuilder();
                sb.append("action=");
                sb.append(action);
                sb.append(" removing=");
                boolean removingPackage4 = removingPackage3;
                sb.append(removingPackage4);
                Flog.i(400, sb.toString());
                if (action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                    unhideNotifications = false;
                    i = 0;
                    unhideNotifications2 = false;
                    hideNotifications = true;
                    uidList = intent.getIntArrayExtra("android.intent.extra.changed_uid_list");
                } else if (action.equals("android.intent.action.PACKAGES_SUSPENDED")) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                    unhideNotifications = false;
                    i = 0;
                    unhideNotifications2 = true;
                    hideNotifications = false;
                    uidList = intent.getIntArrayExtra("android.intent.extra.changed_uid_list");
                } else if (action.equals("android.intent.action.PACKAGES_UNSUSPENDED")) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                    unhideNotifications = true;
                    i = 0;
                    unhideNotifications2 = false;
                    hideNotifications = false;
                    uidList = intent.getIntArrayExtra("android.intent.extra.changed_uid_list");
                } else if (action.equals("android.intent.action.DISTRACTING_PACKAGES_CHANGED")) {
                    if ((intent.getIntExtra("android.intent.extra.distraction_restrictions", 0) & 2) != 0) {
                        cancelNotifications = false;
                        hideNotifications2 = true;
                        pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                        uidList2 = intent.getIntArrayExtra("android.intent.extra.changed_uid_list");
                    } else {
                        cancelNotifications = false;
                        unhideNotifications3 = true;
                        pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                        uidList2 = intent.getIntArrayExtra("android.intent.extra.changed_uid_list");
                    }
                    unhideNotifications = unhideNotifications3;
                    i = 0;
                    unhideNotifications2 = hideNotifications2;
                    hideNotifications = cancelNotifications;
                    uidList = uidList2;
                } else if (packageChanged2) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.PACKAGES");
                    unhideNotifications = false;
                    i = 0;
                    unhideNotifications2 = false;
                    hideNotifications = true;
                    uidList = new int[]{intent.getIntExtra("android.intent.extra.UID", -1)};
                } else {
                    Uri uri = intent.getData();
                    if (uri != null && (pkgName = uri.getSchemeSpecificPart()) != null) {
                        if (!((!queryRemove && !packageChanged) || (uid = intent.getIntExtra("android.intent.extra.UID", -1)) == -1 || NotificationManagerService.this.mNotificationResource == null)) {
                            NotificationManagerService.this.mNotificationResource.clear(uid, pkgName, -1);
                        }
                        if (packageChanged) {
                            try {
                                IPackageManager iPackageManager = NotificationManagerService.this.mPackageManager;
                                if (changeUserId3 != -1) {
                                    i5 = changeUserId3;
                                } else {
                                    i5 = 0;
                                }
                                int enabled = iPackageManager.getApplicationEnabledSetting(pkgName, i5);
                                if (enabled == 1 || enabled == 0) {
                                    cancelNotifications2 = false;
                                }
                            } catch (IllegalArgumentException e) {
                                Flog.i(400, "Exception trying to look up app enabled setting", e);
                            } catch (RemoteException e2) {
                            }
                        }
                        i = 0;
                        unhideNotifications = false;
                        pkgList = new String[]{pkgName};
                        unhideNotifications2 = false;
                        hideNotifications = cancelNotifications2;
                        uidList = new int[]{intent.getIntExtra("android.intent.extra.UID", -1)};
                    } else {
                        return;
                    }
                }
                if (pkgList == null || pkgList.length <= 0) {
                    removingPackage = removingPackage4;
                    changeUserId = changeUserId3;
                } else {
                    int length = pkgList.length;
                    int i6 = i;
                    while (i6 < length) {
                        String pkgName2 = pkgList[i6];
                        if (hideNotifications) {
                            i4 = i;
                            i3 = i6;
                            i2 = length;
                            removingPackage2 = removingPackage4;
                            changeUserId2 = changeUserId3;
                            NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkgName2, null, 0, 0, !packageChanged2 ? 1 : i, changeUserId2, 5, null);
                        } else {
                            removingPackage2 = removingPackage4;
                            i3 = i6;
                            i2 = length;
                            i4 = i;
                            changeUserId2 = changeUserId3;
                            if (unhideNotifications2) {
                                NotificationManagerService.this.hideNotificationsForPackages(pkgList);
                            } else if (unhideNotifications) {
                                NotificationManagerService.this.unhideNotificationsForPackages(pkgList);
                            }
                        }
                        i6 = i3 + 1;
                        i = i4;
                        length = i2;
                        removingPackage4 = removingPackage2;
                        changeUserId3 = changeUserId2;
                    }
                    removingPackage = removingPackage4;
                    changeUserId = changeUserId3;
                }
                NotificationManagerService.this.mHandler.scheduleOnPackageChanged(removingPackage, changeUserId, pkgList, uidList);
            }
        }
    };
    protected IPackageManager mPackageManager;
    private PackageManager mPackageManagerClient;
    private AtomicFile mPolicyFile;
    private PreferencesHelper mPreferencesHelper;
    private RankingHandler mRankingHandler;
    private RankingHelper mRankingHelper;
    private final HandlerThread mRankingThread = new HandlerThread("ranker", 10);
    private final BroadcastReceiver mRestoreReceiver = new BroadcastReceiver() {
        /* class com.android.server.notification.NotificationManagerService.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
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
    Ringtone mRingtone;
    private RoleObserver mRoleObserver;
    boolean mScreenOn = true;
    @VisibleForTesting
    final IBinder mService = new INotificationManager.Stub() {
        /* class com.android.server.notification.NotificationManagerService.AnonymousClass10 */

        public void enqueueToast(String pkg, ITransientNotification callback, int duration, int displayId) {
            ArrayList<ToastRecord> arrayList;
            Throwable th;
            String str;
            IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
            if (manager != null) {
                manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.NOTIFICATIONMANAGER_ENQUEUETOAST);
            }
            Flog.i(400, "enqueueToast pkg=" + pkg + " callback=" + callback + " duration=" + duration);
            if (pkg == null || callback == null) {
                Slog.e(NotificationManagerService.TAG, "Not enqueuing toast. pkg=" + pkg + " callback=" + callback);
            } else if (!NotificationManagerService.ISCHINA || NotificationManagerService.this.isAllowToShow(pkg, ((ActivityTaskManagerInternal) LocalServices.getService(ActivityTaskManagerInternal.class)).getLastResumedActivity())) {
                int callingUid = Binder.getCallingUid();
                boolean appIsForeground = false;
                boolean isSystemToast = NotificationManagerService.this.isCallerSystemOrPhone() || PackageManagerService.PLATFORM_PACKAGE_NAME.equals(pkg);
                boolean isPackageSuspended = isPackagePaused(pkg);
                boolean notificationsDisabledForPackage = !areNotificationsEnabledForPackage(pkg, callingUid);
                long callingIdentity = Binder.clearCallingIdentity();
                try {
                    if (NotificationManagerService.this.mActivityManager.getUidImportance(callingUid) == 100) {
                        appIsForeground = true;
                    }
                    if (isSystemToast || ((!notificationsDisabledForPackage || appIsForeground) && !isPackageSuspended)) {
                        Binder.restoreCallingIdentity(callingIdentity);
                        ArrayList<ToastRecord> arrayList2 = NotificationManagerService.this.mToastQueue;
                        synchronized (arrayList2) {
                            try {
                                int callingPid = Binder.getCallingPid();
                                long callingId = Binder.clearCallingIdentity();
                                try {
                                    int index = NotificationManagerService.this.indexOfToastLocked(pkg, callback);
                                    if (index >= 0) {
                                        try {
                                            NotificationManagerService.this.mToastQueue.get(index).update(duration);
                                            arrayList = arrayList2;
                                        } catch (Throwable th2) {
                                            th = th2;
                                            Binder.restoreCallingIdentity(callingId);
                                            throw th;
                                        }
                                    } else {
                                        if (!isSystemToast) {
                                            int count = 0;
                                            int N = NotificationManagerService.this.mToastQueue.size();
                                            int i = 0;
                                            while (i < N) {
                                                if (!NotificationManagerService.this.mToastQueue.get(i).pkg.equals(pkg) || (count = count + 1) < 50) {
                                                    i++;
                                                    index = index;
                                                } else {
                                                    Slog.e(NotificationManagerService.TAG, "Package has already posted " + count + " toasts. Not showing more. Package=" + pkg);
                                                    Binder.restoreCallingIdentity(callingId);
                                                }
                                            }
                                        }
                                        Binder token = new Binder();
                                        NotificationManagerService.this.mWindowManagerInternal.addWindowToken(token, 2005, displayId);
                                        arrayList = arrayList2;
                                        try {
                                            NotificationManagerService.this.mToastQueue.add(new ToastRecord(callingPid, pkg, callback, duration, token, displayId));
                                            int index2 = NotificationManagerService.this.mToastQueue.size() - 1;
                                            try {
                                                NotificationManagerService.this.keepProcessAliveIfNeededLocked(callingPid);
                                                index = index2;
                                            } catch (Throwable th3) {
                                                th = th3;
                                                Binder.restoreCallingIdentity(callingId);
                                                throw th;
                                            }
                                        } catch (Throwable th4) {
                                            th = th4;
                                            throw th;
                                        }
                                    }
                                    if (index == 0) {
                                        NotificationManagerService.this.showNextToastLocked();
                                    }
                                    Binder.restoreCallingIdentity(callingId);
                                    return;
                                } catch (Throwable th5) {
                                    th = th5;
                                    Binder.restoreCallingIdentity(callingId);
                                    throw th;
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                arrayList = arrayList2;
                                throw th;
                            }
                        }
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("Suppressing toast from package ");
                    sb.append(pkg);
                    if (isPackageSuspended) {
                        str = " due to package suspended.";
                    } else {
                        str = " by user request.";
                    }
                    sb.append(str);
                    Slog.e(NotificationManagerService.TAG, sb.toString());
                } finally {
                    Binder.restoreCallingIdentity(callingIdentity);
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
                        ToastRecord record = NotificationManagerService.this.mToastQueue.get(index);
                        NotificationManagerService.this.finishTokenLocked(record.token, record.displayId);
                    } else {
                        Slog.w(NotificationManagerService.TAG, "Toast already killed. pkg=" + pkg + " callback=" + callback);
                    }
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }

        public void enqueueNotificationWithTag(String pkg, String opPkg, String tag, int id, Notification notification, int userId) throws RemoteException {
            IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
            if (manager != null) {
                manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.NOTIFICATIONMANAGER_ENQUEUENOTIFICATIONWITHTAG);
            }
            NotificationManagerService.this.addHwExtraForNotification(notification, pkg, Binder.getCallingPid());
            NotificationManagerService notificationManagerService = NotificationManagerService.this;
            notificationManagerService.enqueueNotificationInternal(notificationManagerService.getNCTargetAppPkg(opPkg, pkg, notification), opPkg, Binder.getCallingUid(), Binder.getCallingPid(), tag, id, notification, userId);
        }

        public void cancelNotificationWithTag(String pkg, String tag, int id, int userId) {
            int mustNotHaveFlags;
            String tag2;
            String pkg2;
            IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
            if (manager != null) {
                manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.NOTIFICATIONMANAGER_CANCELNOTIFICATIONWITHTAG);
            }
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, false, "cancelNotificationWithTag", pkg);
            if (NotificationManagerService.HWFLOW) {
                Flog.i(400, "cancelNotificationWithTag pid " + Binder.getCallingPid() + ",uid = " + Binder.getCallingUid() + ",tag = " + tag + ",pkg =" + pkg + ",id =" + id);
            }
            if (NotificationManagerService.this.isCallingUidSystem()) {
                mustNotHaveFlags = 0;
            } else {
                mustNotHaveFlags = 1088;
            }
            if (NotificationManagerService.this.iHwNotificationManagerServiceEx == null || !NotificationManagerService.this.iHwNotificationManagerServiceEx.isPushSpecialRequest(pkg, tag)) {
                pkg2 = pkg;
                tag2 = tag;
            } else {
                Log.i(NotificationManagerService.PUSH_TAG, "Push agent request cancel notification, pkg=" + pkg + ",tag=" + tag);
                pkg2 = NotificationManagerService.this.iHwNotificationManagerServiceEx.getPushSpecialRequestPkg(pkg, tag);
                tag2 = NotificationManagerService.this.iHwNotificationManagerServiceEx.getPushSpecialRequestTag(tag);
                if (pkg2 == null) {
                    return;
                }
            }
            NotificationManagerService.this.cancelNotification(Binder.getCallingUid(), Binder.getCallingPid(), pkg2, tag2, id, 0, mustNotHaveFlags, false, userId2, 8, null);
        }

        public void cancelAllNotifications(String pkg, int userId) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            NotificationManagerService.this.cancelAllNotificationsInt(Binder.getCallingUid(), Binder.getCallingPid(), pkg, null, 0, 64, true, ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, false, "cancelAllNotifications", pkg), 9, null);
        }

        public void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled) {
            enforceSystemOrSystemUI("setNotificationsEnabledForPackage");
            NotificationManagerService.this.mPreferencesHelper.setEnabled(pkg, uid, enabled);
            NotificationManagerService.this.mMetricsLogger.write(new LogMaker(147).setType(4).setPackageName(pkg).setSubtype(enabled ? 1 : 0));
            if (!enabled) {
                NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkg, null, 0, 0, true, UserHandle.getUserId(uid), 7, null);
            }
            try {
                NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.APP_BLOCK_STATE_CHANGED").putExtra("android.app.extra.BLOCKED_STATE", !enabled).addFlags(268435456).setPackage(pkg), UserHandle.of(UserHandle.getUserId(uid)), null);
            } catch (SecurityException e) {
                Slog.w(NotificationManagerService.TAG, "Can't notify app about app block change", e);
            }
            NotificationManagerService.this.handleSavePolicyFile();
        }

        public void setNotificationsEnabledWithImportanceLockForPackage(String pkg, int uid, boolean enabled) {
            setNotificationsEnabledForPackage(pkg, uid, enabled);
            NotificationManagerService.this.mPreferencesHelper.setAppImportanceLocked(pkg, uid);
        }

        public boolean areNotificationsEnabled(String pkg) {
            return areNotificationsEnabledForPackage(pkg, Binder.getCallingUid());
        }

        public boolean areNotificationsEnabledForPackage(String pkg, int uid) {
            enforceSystemOrSystemUIOrSamePackage(pkg, "Caller not system or systemui or same package");
            if (UserHandle.getCallingUserId() != UserHandle.getUserId(uid)) {
                Context context = NotificationManagerService.this.getContext();
                context.enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS", "canNotifyAsPackage for uid " + uid);
            }
            return NotificationManagerService.this.mPreferencesHelper.getImportance(pkg, uid) != 0;
        }

        public boolean areBubblesAllowed(String pkg) {
            return areBubblesAllowedForPackage(pkg, Binder.getCallingUid());
        }

        public boolean areBubblesAllowedForPackage(String pkg, int uid) {
            enforceSystemOrSystemUIOrSamePackage(pkg, "Caller not system or systemui or same package");
            if (UserHandle.getCallingUserId() != UserHandle.getUserId(uid)) {
                Context context = NotificationManagerService.this.getContext();
                context.enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS", "canNotifyAsPackage for uid " + uid);
            }
            return NotificationManagerService.this.mPreferencesHelper.areBubblesAllowed(pkg, uid);
        }

        public void setBubblesAllowed(String pkg, int uid, boolean allowed) {
            enforceSystemOrSystemUI("Caller not system or systemui");
            NotificationManagerService.this.mPreferencesHelper.setBubblesAllowed(pkg, uid, allowed);
            NotificationManagerService.this.handleSavePolicyFile();
        }

        public boolean hasUserApprovedBubblesForPackage(String pkg, int uid) {
            enforceSystemOrSystemUI("Caller not system or systemui");
            return (NotificationManagerService.this.mPreferencesHelper.getAppLockedFields(pkg, uid) & 2) != 0;
        }

        public boolean shouldHideSilentStatusIcons(String callingPkg) {
            NotificationManagerService.this.checkCallerIsSameApp(callingPkg);
            if (NotificationManagerService.this.isCallerSystemOrPhone() || NotificationManagerService.this.mListeners.isListenerPackage(callingPkg)) {
                return NotificationManagerService.this.mPreferencesHelper.shouldHideSilentStatusIcons();
            }
            throw new SecurityException("Only available for notification listeners");
        }

        public void setHideSilentStatusIcons(boolean hide) {
            NotificationManagerService.this.checkCallerIsSystem();
            NotificationManagerService.this.mPreferencesHelper.setHideSilentStatusIcons(hide);
            NotificationManagerService.this.handleSavePolicyFile();
            NotificationManagerService.this.mListeners.onStatusBarIconsBehaviorChanged(hide);
        }

        public int getPackageImportance(String pkg) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            return NotificationManagerService.this.mPreferencesHelper.getImportance(pkg, Binder.getCallingUid());
        }

        public boolean canShowBadge(String pkg, int uid) {
            NotificationManagerService.this.checkCallerIsSystemOrSystemApp();
            return NotificationManagerService.this.mPreferencesHelper.canShowBadge(pkg, uid);
        }

        public void setShowBadge(String pkg, int uid, boolean showBadge) {
            NotificationManagerService.this.checkCallerIsSystem();
            NotificationManagerService.this.mPreferencesHelper.setShowBadge(pkg, uid, showBadge);
            NotificationManagerService.this.handleSavePolicyFile();
        }

        public void setNotificationDelegate(String callingPkg, String delegate) {
            NotificationManagerService.this.checkCallerIsSameApp(callingPkg);
            int callingUid = Binder.getCallingUid();
            UserHandle user = UserHandle.getUserHandleForUid(callingUid);
            if (delegate == null) {
                NotificationManagerService.this.mPreferencesHelper.revokeNotificationDelegate(callingPkg, Binder.getCallingUid());
                NotificationManagerService.this.handleSavePolicyFile();
                return;
            }
            try {
                ApplicationInfo info = NotificationManagerService.this.mPackageManager.getApplicationInfo(delegate, 786432, user.getIdentifier());
                if (info != null) {
                    NotificationManagerService.this.mPreferencesHelper.setNotificationDelegate(callingPkg, callingUid, delegate, info.uid);
                    NotificationManagerService.this.handleSavePolicyFile();
                }
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            }
        }

        public String getNotificationDelegate(String callingPkg) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(callingPkg);
            return NotificationManagerService.this.mPreferencesHelper.getNotificationDelegate(callingPkg, Binder.getCallingUid());
        }

        public boolean canNotifyAsPackage(String callingPkg, String targetPkg, int userId) {
            NotificationManagerService.this.checkCallerIsSameApp(callingPkg);
            int callingUid = Binder.getCallingUid();
            if (UserHandle.getUserHandleForUid(callingUid).getIdentifier() != userId) {
                Context context = NotificationManagerService.this.getContext();
                context.enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS", "canNotifyAsPackage for user " + userId);
            }
            if (callingPkg.equals(targetPkg)) {
                return true;
            }
            try {
                ApplicationInfo info = NotificationManagerService.this.mPackageManager.getApplicationInfo(targetPkg, 786432, userId);
                if (info != null) {
                    return NotificationManagerService.this.mPreferencesHelper.isDelegateAllowed(targetPkg, info.uid, callingPkg, callingUid);
                }
                return false;
            } catch (RemoteException e) {
                return false;
            }
        }

        public void updateNotificationChannelGroupForPackage(String pkg, int uid, NotificationChannelGroup group) throws RemoteException {
            enforceSystemOrSystemUI("Caller not system or systemui");
            NotificationManagerService.this.createNotificationChannelGroup(pkg, uid, group, false, false);
            NotificationManagerService.this.handleSavePolicyFile();
        }

        public void createNotificationChannelGroups(String pkg, ParceledListSlice channelGroupList) throws RemoteException {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            List<NotificationChannelGroup> groups = channelGroupList.getList();
            List<NotificationChannelGroup> groupsCache = new ArrayList<>(groups);
            int groupSize = groups.size();
            for (int i = 0; i < groupSize; i++) {
                NotificationManagerService.this.createNotificationChannelGroup(pkg, Binder.getCallingUid(), groupsCache.get(i), true, false);
            }
            NotificationManagerService.this.handleSavePolicyFile();
        }

        private void createNotificationChannelsImpl(String pkg, int uid, ParceledListSlice channelsList) {
            List<NotificationChannel> channels = channelsList.getList();
            int channelsSize = channels.size();
            boolean needsPolicyFileChange = false;
            for (int i = 0; i < channelsSize; i++) {
                NotificationChannel channel = channels.get(i);
                Preconditions.checkNotNull(channel, "channel in list is null");
                needsPolicyFileChange = NotificationManagerService.this.mPreferencesHelper.createNotificationChannel(pkg, uid, channel, true, NotificationManagerService.this.mConditionProviders.isPackageOrComponentAllowed(pkg, UserHandle.getUserId(uid)));
                if (needsPolicyFileChange) {
                    NotificationManagerService.this.mListeners.notifyNotificationChannelChanged(pkg, UserHandle.getUserHandleForUid(uid), NotificationManagerService.this.mPreferencesHelper.getNotificationChannel(pkg, uid, channel.getId(), false), 1);
                }
            }
            if (needsPolicyFileChange) {
                NotificationManagerService.this.handleSavePolicyFile();
            }
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

        public NotificationChannel getNotificationChannel(String callingPkg, int userId, String targetPkg, String channelId) {
            if (canNotifyAsPackage(callingPkg, targetPkg, userId) || NotificationManagerService.this.isCallingUidSystem()) {
                if (NotificationManagerService.this.iHwNotificationManagerServiceEx != null && NotificationManagerService.this.iHwNotificationManagerServiceEx.isPushSpecialRequest(targetPkg, channelId)) {
                    targetPkg = NotificationManagerService.this.iHwNotificationManagerServiceEx.getPushSpecialRequestPkg(targetPkg, channelId);
                    channelId = NotificationManagerService.this.iHwNotificationManagerServiceEx.getPushSpecialRequestChannel(channelId);
                    if (channelId == null || targetPkg == null) {
                        return null;
                    }
                }
                int targetUid = -1;
                try {
                    targetUid = NotificationManagerService.this.mPackageManagerClient.getPackageUidAsUser(targetPkg, userId);
                } catch (PackageManager.NameNotFoundException e) {
                }
                return NotificationManagerService.this.getHwNotificationChannel(NotificationManagerService.this.mPreferencesHelper.getNotificationChannel(targetPkg, targetUid, channelId, false), targetPkg, targetUid);
            }
            throw new SecurityException("Pkg " + callingPkg + " cannot read channels for " + targetPkg + " in " + userId);
        }

        public NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, boolean includeDeleted) {
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mPreferencesHelper.getNotificationChannel(pkg, uid, channelId, includeDeleted);
        }

        public void deleteNotificationChannel(String pkg, String channelId) {
            String pkg2 = pkg;
            String channelId2 = channelId;
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg2);
            int callingUid = Binder.getCallingUid();
            if (!"miscellaneous".equals(channelId2)) {
                if (NotificationManagerService.this.iHwNotificationManagerServiceEx != null && NotificationManagerService.this.iHwNotificationManagerServiceEx.isPushSpecialRequest(pkg2, channelId2)) {
                    Log.i(NotificationManagerService.PUSH_TAG, "Push agent request delete NotificationChannel, pkg=" + pkg2 + ",channelId=" + channelId2);
                    String pkg3 = NotificationManagerService.this.iHwNotificationManagerServiceEx.getPushSpecialRequestPkg(pkg2, channelId2);
                    channelId2 = NotificationManagerService.this.iHwNotificationManagerServiceEx.getPushSpecialRequestChannel(channelId2);
                    if (channelId2 != null && pkg3 != null) {
                        try {
                            callingUid = NotificationManagerService.this.mPackageManagerClient.getPackageUidAsUser(pkg3, UserHandle.getUserId(callingUid));
                            pkg2 = pkg3;
                        } catch (PackageManager.NameNotFoundException e) {
                            Slog.w(NotificationManagerService.TAG, "deleteNotificationChannel from push, get callingUid failed");
                            return;
                        }
                    } else {
                        return;
                    }
                }
                NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkg2, channelId2, 0, 0, true, UserHandle.getUserId(callingUid), 17, null);
                NotificationManagerService.this.mPreferencesHelper.deleteNotificationChannel(pkg2, callingUid, channelId2);
                NotificationManagerService.this.mListeners.notifyNotificationChannelChanged(pkg2, UserHandle.getUserHandleForUid(callingUid), NotificationManagerService.this.mPreferencesHelper.getNotificationChannel(pkg2, callingUid, channelId2, true), 3);
                NotificationManagerService.this.handleSavePolicyFile();
                return;
            }
            throw new IllegalArgumentException("Cannot delete default channel");
        }

        public NotificationChannelGroup getNotificationChannelGroup(String pkg, String groupId) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            return NotificationManagerService.this.mPreferencesHelper.getNotificationChannelGroupWithChannels(pkg, Binder.getCallingUid(), groupId, false);
        }

        public ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroups(String pkg) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            return NotificationManagerService.this.mPreferencesHelper.getNotificationChannelGroups(pkg, Binder.getCallingUid(), false, false, true);
        }

        public void deleteNotificationChannelGroup(String pkg, String groupId) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            int callingUid = Binder.getCallingUid();
            NotificationChannelGroup groupToDelete = NotificationManagerService.this.mPreferencesHelper.getNotificationChannelGroup(groupId, pkg, callingUid);
            if (groupToDelete != null) {
                int i = 0;
                for (List<NotificationChannel> deletedChannels = NotificationManagerService.this.mPreferencesHelper.deleteNotificationChannelGroup(pkg, callingUid, groupId); i < deletedChannels.size(); deletedChannels = deletedChannels) {
                    NotificationChannel deletedChannel = deletedChannels.get(i);
                    NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkg, deletedChannel.getId(), 0, 0, true, UserHandle.getUserId(Binder.getCallingUid()), 17, null);
                    NotificationManagerService.this.mListeners.notifyNotificationChannelChanged(pkg, UserHandle.getUserHandleForUid(callingUid), deletedChannel, 3);
                    i++;
                }
                NotificationManagerService.this.mListeners.notifyNotificationChannelGroupChanged(pkg, UserHandle.getUserHandleForUid(callingUid), groupToDelete, 3);
                NotificationManagerService.this.handleSavePolicyFile();
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
            return NotificationManagerService.this.mPreferencesHelper.getNotificationChannels(pkg, uid, includeDeleted);
        }

        public int getNumNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted) {
            enforceSystemOrSystemUI("getNumNotificationChannelsForPackage");
            return NotificationManagerService.this.mPreferencesHelper.getNotificationChannels(pkg, uid, includeDeleted).getList().size();
        }

        public boolean onlyHasDefaultChannel(String pkg, int uid) {
            enforceSystemOrSystemUI("onlyHasDefaultChannel");
            return NotificationManagerService.this.mPreferencesHelper.onlyHasDefaultChannel(pkg, uid);
        }

        public int getDeletedChannelCount(String pkg, int uid) {
            enforceSystemOrSystemUI("getDeletedChannelCount");
            return NotificationManagerService.this.mPreferencesHelper.getDeletedChannelCount(pkg, uid);
        }

        public int getBlockedChannelCount(String pkg, int uid) {
            enforceSystemOrSystemUI("getBlockedChannelCount");
            return NotificationManagerService.this.mPreferencesHelper.getBlockedChannelCount(pkg, uid);
        }

        public ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroupsForPackage(String pkg, int uid, boolean includeDeleted) {
            enforceSystemOrSystemUI("getNotificationChannelGroupsForPackage");
            return NotificationManagerService.this.mPreferencesHelper.getNotificationChannelGroups(pkg, uid, includeDeleted, true, false);
        }

        public NotificationChannelGroup getPopulatedNotificationChannelGroupForPackage(String pkg, int uid, String groupId, boolean includeDeleted) {
            enforceSystemOrSystemUI("getPopulatedNotificationChannelGroupForPackage");
            return NotificationManagerService.this.mPreferencesHelper.getNotificationChannelGroupWithChannels(pkg, uid, groupId, includeDeleted);
        }

        public NotificationChannelGroup getNotificationChannelGroupForPackage(String groupId, String pkg, int uid) {
            enforceSystemOrSystemUI("getNotificationChannelGroupForPackage");
            return NotificationManagerService.this.mPreferencesHelper.getNotificationChannelGroup(groupId, pkg, uid);
        }

        public ParceledListSlice<NotificationChannel> getNotificationChannels(String callingPkg, String targetPkg, int userId) {
            if (canNotifyAsPackage(callingPkg, targetPkg, userId) || NotificationManagerService.this.isCallingUidSystem()) {
                int targetUid = -1;
                try {
                    targetUid = NotificationManagerService.this.mPackageManagerClient.getPackageUidAsUser(targetPkg, userId);
                } catch (PackageManager.NameNotFoundException e) {
                }
                return NotificationManagerService.this.mPreferencesHelper.getNotificationChannels(targetPkg, targetUid, false);
            }
            throw new SecurityException("Pkg " + callingPkg + " cannot read channels for " + targetPkg + " in " + userId);
        }

        public int getBlockedAppCount(int userId) {
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mPreferencesHelper.getBlockedAppCount(userId);
        }

        public int getAppsBypassingDndCount(int userId) {
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mPreferencesHelper.getAppsBypassingDndCount(userId);
        }

        public ParceledListSlice<NotificationChannel> getNotificationChannelsBypassingDnd(String pkg, int userId) {
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mPreferencesHelper.getNotificationChannelsBypassingDnd(pkg, userId);
        }

        public boolean areChannelsBypassingDnd() {
            return NotificationManagerService.this.mPreferencesHelper.areChannelsBypassingDnd();
        }

        public void clearData(String packageName, int uid, boolean fromApp) throws RemoteException {
            NotificationManagerService.this.checkCallerIsSystem();
            NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, packageName, null, 0, 0, true, UserHandle.getUserId(Binder.getCallingUid()), 17, null);
            String[] packages = {packageName};
            int[] uids = {uid};
            NotificationManagerService.this.mListeners.onPackagesChanged(true, packages, uids);
            NotificationManagerService.this.mAssistants.onPackagesChanged(true, packages, uids);
            NotificationManagerService.this.mConditionProviders.onPackagesChanged(true, packages, uids);
            NotificationManagerService.this.mSnoozeHelper.clearData(UserHandle.getUserId(uid), packageName);
            if (!fromApp) {
                NotificationManagerService.this.mPreferencesHelper.clearData(packageName, uid);
            }
            NotificationManagerService.this.handleSavePolicyFile();
        }

        public List<String> getAllowedAssistantAdjustments(String pkg) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            if (NotificationManagerService.this.isCallerSystemOrPhone() || NotificationManagerService.this.mAssistants.isPackageAllowed(pkg, UserHandle.getCallingUserId())) {
                return NotificationManagerService.this.mAssistants.getAllowedAssistantAdjustments();
            }
            throw new SecurityException("Not currently an assistant");
        }

        public void allowAssistantAdjustment(String adjustmentType) {
            NotificationManagerService.this.checkCallerIsSystemOrSystemUiOrShell();
            NotificationManagerService.this.mAssistants.allowAdjustmentType(adjustmentType);
            NotificationManagerService.this.handleSavePolicyFile();
        }

        public void disallowAssistantAdjustment(String adjustmentType) {
            NotificationManagerService.this.checkCallerIsSystemOrSystemUiOrShell();
            NotificationManagerService.this.mAssistants.disallowAdjustmentType(adjustmentType);
            NotificationManagerService.this.handleSavePolicyFile();
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
            if (sbn.getUserId() != userId) {
                return null;
            }
            if (sbn.getPackageName().equals(pkg) || sbn.getOpPkg().equals(pkg)) {
                return new StatusBarNotification(sbn.getPackageName(), sbn.getOpPkg(), sbn.getId(), sbn.getTag(), sbn.getUid(), sbn.getInitialPid(), sbn.getNotification().clone(), sbn.getUser(), sbn.getOverrideGroupKey(), sbn.getPostTime());
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
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            long identity = Binder.clearCallingIdentity();
            try {
                Flog.i(400, "cancelNotificationsFromListener called,callingUid = " + callingUid + ",callingPid = " + callingPid);
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    ManagedServices.ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    if (keys != null) {
                        int N2 = keys.length;
                        int i2 = 0;
                        while (i2 < N2) {
                            NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(keys[i2]);
                            if (r == null) {
                                i = i2;
                                N = N2;
                            } else {
                                int userId = r.sbn.getUserId();
                                if (!(userId == info.userid || userId == -1)) {
                                    if (!NotificationManagerService.this.mUserProfiles.isCurrentProfile(userId)) {
                                        throw new SecurityException("Disallowed call from listener: " + info.service);
                                    }
                                }
                                i = i2;
                                N = N2;
                                cancelNotificationFromListenerLocked(info, callingUid, callingPid, r.sbn.getPackageName(), r.sbn.getTag(), r.sbn.getId(), userId);
                            }
                            i2 = i + 1;
                            N2 = N;
                        }
                    } else {
                        NotificationManagerService.this.cancelAllLocked(callingUid, callingPid, info.userid, 11, info, info.supportsProfiles());
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
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
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void setNotificationsShownFromListener(INotificationListener token, String[] keys) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    ManagedServices.ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    if (keys != null) {
                        ArrayList<NotificationRecord> seen = new ArrayList<>();
                        int n = keys.length;
                        for (int i = 0; i < n; i++) {
                            NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(keys[i]);
                            if (r != null) {
                                int userId = r.sbn.getUserId();
                                if (userId == info.userid || userId == -1 || NotificationManagerService.this.mUserProfiles.isCurrentProfile(userId)) {
                                    seen.add(r);
                                    if (!r.isSeen()) {
                                        Flog.i(400, "Marking notification as seen " + keys[i]);
                                        NotificationManagerService.this.reportSeen(r);
                                        r.setSeen();
                                        NotificationManagerService.this.maybeRecordInterruptionLocked(r);
                                    }
                                } else {
                                    throw new SecurityException("Disallowed call from listener: " + info.service);
                                }
                            }
                        }
                        if (!seen.isEmpty()) {
                            NotificationManagerService.this.mAssistants.onNotificationsSeenLocked(seen);
                        }
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        @GuardedBy({"mNotificationLock"})
        private void cancelNotificationFromListenerLocked(ManagedServices.ManagedServiceInfo info, int callingUid, int callingPid, String pkg, String tag, int id, int userId) {
            NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 4162, true, userId, 10, info);
        }

        public void snoozeNotificationUntilContextFromListener(INotificationListener token, String key, String snoozeCriterionId) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.snoozeNotificationInt(key, -1, snoozeCriterionId, NotificationManagerService.this.mListeners.checkServiceTokenLocked(token));
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void snoozeNotificationUntilFromListener(INotificationListener token, String key, long duration) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.snoozeNotificationInt(key, duration, null, NotificationManagerService.this.mListeners.checkServiceTokenLocked(token));
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void unsnoozeNotificationFromAssistant(INotificationListener token, String key) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.unsnoozeNotificationInt(key, NotificationManagerService.this.mAssistants.checkServiceTokenLocked(token));
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void cancelNotificationFromListener(INotificationListener token, String pkg, String tag, int id) {
            Throwable th;
            Throwable th2;
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    try {
                        try {
                            ManagedServices.ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                            if (info.supportsProfiles()) {
                                Slog.e(NotificationManagerService.TAG, "Ignoring deprecated cancelNotification(pkg, tag, id) from " + info.component + " use cancelNotification(key) instead.");
                            } else {
                                cancelNotificationFromListenerLocked(info, callingUid, callingPid, pkg, tag, id, info.userid);
                            }
                            Binder.restoreCallingIdentity(identity);
                            return;
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    } catch (Throwable th4) {
                        th2 = th4;
                    }
                }
                try {
                    throw th2;
                } catch (Throwable th5) {
                    th = th5;
                }
            } catch (Throwable th6) {
                th = th6;
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

        public void clearRequestedListenerHints(INotificationListener token) {
            if (!"com.google.android.projection.gearhead".equals(NotificationManagerService.this.getPackageNameByPid(Binder.getCallingPid()))) {
                long identity = Binder.clearCallingIdentity();
                try {
                    synchronized (NotificationManagerService.this.mNotificationLock) {
                        NotificationManagerService.this.removeDisabledHints(NotificationManagerService.this.mListeners.checkServiceTokenLocked(token));
                        NotificationManagerService.this.updateListenerHintsLocked();
                        NotificationManagerService.this.updateEffectsSuppressorLocked();
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void requestHintsFromListener(INotificationListener token, int hints) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    ManagedServices.ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    if ((hints & 7) != 0) {
                        NotificationManagerService.this.addDisabledHints(info, hints);
                    } else {
                        NotificationManagerService.this.removeDisabledHints(info, hints);
                    }
                    NotificationManagerService.this.updateListenerHintsLocked();
                    NotificationManagerService.this.updateEffectsSuppressorLocked();
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public int getHintsFromListener(INotificationListener token) {
            int i;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                i = NotificationManagerService.this.mListenerHints;
            }
            return i;
        }

        public void requestInterruptionFilterFromListener(INotificationListener token, int interruptionFilter) throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.mZenModeHelper.requestFromListener(NotificationManagerService.this.mListeners.checkServiceTokenLocked(token).component, interruptionFilter);
                    NotificationManagerService.this.updateInterruptionFilterLocked();
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public int getInterruptionFilterFromListener(INotificationListener token) throws RemoteException {
            int i;
            synchronized (NotificationManagerService.this.mNotificationLight) {
                i = NotificationManagerService.this.mInterruptionFilter;
            }
            return i;
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

        public String addAutomaticZenRule(AutomaticZenRule automaticZenRule) {
            Preconditions.checkNotNull(automaticZenRule, "automaticZenRule is null");
            Preconditions.checkNotNull(automaticZenRule.getName(), "Name is null");
            if (automaticZenRule.getOwner() == null && automaticZenRule.getConfigurationActivity() == null) {
                throw new NullPointerException("Rule must have a conditionproviderservice and/or configuration activity");
            }
            Preconditions.checkNotNull(automaticZenRule.getConditionId(), "ConditionId is null");
            if (automaticZenRule.getZenPolicy() == null || automaticZenRule.getInterruptionFilter() == 2) {
                enforcePolicyAccess(Binder.getCallingUid(), "addAutomaticZenRule");
                return NotificationManagerService.this.mZenModeHelper.addAutomaticZenRule(automaticZenRule, "addAutomaticZenRule");
            }
            throw new IllegalArgumentException("ZenPolicy is only applicable to INTERRUPTION_FILTER_PRIORITY filters");
        }

        public boolean updateAutomaticZenRule(String id, AutomaticZenRule automaticZenRule) throws RemoteException {
            Preconditions.checkNotNull(automaticZenRule, "automaticZenRule is null");
            Preconditions.checkNotNull(automaticZenRule.getName(), "Name is null");
            if (automaticZenRule.getOwner() == null && automaticZenRule.getConfigurationActivity() == null) {
                throw new NullPointerException("Rule must have a conditionproviderservice and/or configuration activity");
            }
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

        public void setAutomaticZenRuleState(String id, Condition condition) {
            Preconditions.checkNotNull(id, "id is null");
            Preconditions.checkNotNull(condition, "Condition is null");
            enforcePolicyAccess(Binder.getCallingUid(), "setAutomaticZenRuleState");
            NotificationManagerService.this.mZenModeHelper.setAutomaticZenRuleState(id, condition);
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
                /* class com.android.server.notification.NotificationManagerService.AnonymousClass10.AnonymousClass1 */

                @Override // java.lang.Runnable
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
                for (String str : NotificationManagerService.this.mPackageManagerClient.getPackagesForUid(uid)) {
                    if (NotificationManagerService.this.mConditionProviders.isPackageOrComponentAllowed(str, UserHandle.getUserId(uid))) {
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
            try {
                if (ActivityManager.checkComponentPermission("android.permission.MANAGE_NOTIFICATIONS", NotificationManagerService.this.getContext().getPackageManager().getPackageUidAsUser(pkg, UserHandle.getCallingUserId()), -1, true) == 0) {
                    return true;
                }
                if (checkPackagePolicyAccess(pkg) || NotificationManagerService.this.mListeners.isComponentEnabledForPackage(pkg) || (NotificationManagerService.this.mDpm != null && NotificationManagerService.this.mDpm.isActiveAdminWithPolicy(Binder.getCallingUid(), -1))) {
                    return true;
                }
                return false;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpAndUsageStatsPermission(NotificationManagerService.this.getContext(), NotificationManagerService.TAG, pw)) {
                DumpFilter filter = DumpFilter.parseFromArguments(args);
                long token = Binder.clearCallingIdentity();
                try {
                    if (filter.stats) {
                        NotificationManagerService.this.dumpJson(pw, filter);
                    } else if (filter.proto) {
                        NotificationManagerService.this.dumpProto(fd, filter);
                    } else if (filter.criticalPriority) {
                        NotificationManagerService.this.dumpNotificationRecords(pw, filter);
                    } else {
                        NotificationManagerService.this.dumpImpl(pw, filter);
                    }
                } finally {
                    Binder.restoreCallingIdentity(token);
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
            NotificationManagerService.this.checkCallerIsSystem();
            if (NotificationManagerService.DBG) {
                Slog.d(NotificationManagerService.TAG, "getBackupPayload u=" + user);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                NotificationManagerService.this.writePolicyXml(baos, true, user);
                return baos.toByteArray();
            } catch (IllegalArgumentException e) {
                Slog.w(NotificationManagerService.TAG, "getBackupPayload: get IllegalArgumentException.");
                return null;
            } catch (IOException e2) {
                Slog.w(NotificationManagerService.TAG, "getBackupPayload: error writing payload for user " + user, e2);
                return null;
            }
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
                return;
            }
            try {
                NotificationManagerService.this.readPolicyXml(new ByteArrayInputStream(payload), true, user);
                NotificationManagerService.this.handleSavePolicyFile();
            } catch (IOException | NumberFormatException | XmlPullParserException e) {
                Slog.w(NotificationManagerService.TAG, "applyRestore: error reading payload", e);
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
                if (NotificationManagerService.this.mAllowedManagedServicePackages.test(pkg, Integer.valueOf(userId), NotificationManagerService.this.mConditionProviders.getRequiredPermission())) {
                    NotificationManagerService.this.mConditionProviders.setPackageOrComponentEnabled(pkg, userId, true, granted);
                    NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED").setPackage(pkg).addFlags(DumpState.DUMP_HANDLE), UserHandle.of(userId), null);
                    NotificationManagerService.this.handleSavePolicyFile();
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

        public NotificationManager.Policy getConsolidatedNotificationPolicy() {
            long identity = Binder.clearCallingIdentity();
            try {
                return NotificationManagerService.this.mZenModeHelper.getConsolidatedNotificationPolicy();
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

        public ComponentName getAllowedNotificationAssistantForUser(int userId) {
            NotificationManagerService.this.checkCallerIsSystemOrSystemUiOrShell();
            List<ComponentName> allowedComponents = NotificationManagerService.this.mAssistants.getAllowedComponents(userId);
            if (allowedComponents.size() <= 1) {
                return (ComponentName) CollectionUtils.firstOrNull(allowedComponents);
            }
            throw new IllegalStateException("At most one NotificationAssistant: " + allowedComponents.size());
        }

        public ComponentName getAllowedNotificationAssistant() {
            return getAllowedNotificationAssistantForUser(getCallingUserHandle().getIdentifier());
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

        public void setNotificationAssistantAccessGranted(ComponentName assistant, boolean granted) {
            setNotificationAssistantAccessGrantedForUser(assistant, getCallingUserHandle().getIdentifier(), granted);
        }

        public void setNotificationListenerAccessGrantedForUser(ComponentName listener, int userId, boolean granted) {
            Preconditions.checkNotNull(listener);
            NotificationManagerService.this.checkCallerIsSystemOrShell();
            long identity = Binder.clearCallingIdentity();
            try {
                if (NotificationManagerService.this.mAllowedManagedServicePackages.test(listener.getPackageName(), Integer.valueOf(userId), NotificationManagerService.this.mListeners.getRequiredPermission())) {
                    NotificationManagerService.this.mConditionProviders.setPackageOrComponentEnabled(listener.flattenToString(), userId, false, granted);
                    NotificationManagerService.this.mListeners.setPackageOrComponentEnabled(listener.flattenToString(), userId, true, granted);
                    NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED").setPackage(listener.getPackageName()).addFlags(1073741824), UserHandle.of(userId), null);
                    NotificationManagerService.this.handleSavePolicyFile();
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void setNotificationAssistantAccessGrantedForUser(ComponentName assistant, int userId, boolean granted) {
            NotificationManagerService.this.checkCallerIsSystemOrSystemUiOrShell();
            for (UserInfo ui : NotificationManagerService.this.mUm.getEnabledProfiles(userId)) {
                NotificationManagerService.this.mAssistants.setUserSet(ui.id, true);
            }
            long identity = Binder.clearCallingIdentity();
            try {
                NotificationManagerService.this.setNotificationAssistantAccessGrantedForUserInternal(assistant, userId, granted);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void applyEnqueuedAdjustmentFromAssistant(INotificationListener token, Adjustment adjustment) {
            boolean foundEnqueued = false;
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
                        NotificationRecord r = NotificationManagerService.this.mEnqueuedNotifications.get(i);
                        if (Objects.equals(adjustment.getKey(), r.getKey()) && Objects.equals(Integer.valueOf(adjustment.getUser()), Integer.valueOf(r.getUserId())) && NotificationManagerService.this.mAssistants.isSameUser(token, r.getUserId())) {
                            NotificationManagerService.this.applyAdjustment(r, adjustment);
                            r.applyAdjustments();
                            r.calculateImportance();
                            foundEnqueued = true;
                            break;
                        }
                        i++;
                    }
                    if (!foundEnqueued) {
                        applyAdjustmentFromAssistant(token, adjustment);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void applyAdjustmentFromAssistant(INotificationListener token, Adjustment adjustment) {
            List<Adjustment> adjustments = new ArrayList<>();
            adjustments.add(adjustment);
            applyAdjustmentsFromAssistant(token, adjustments);
        }

        public void applyAdjustmentsFromAssistant(INotificationListener token, List<Adjustment> adjustments) {
            boolean needsSort = false;
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.mAssistants.checkServiceTokenLocked(token);
                    for (Adjustment adjustment : adjustments) {
                        NotificationRecord r = NotificationManagerService.this.mNotificationsByKey.get(adjustment.getKey());
                        if (r != null && NotificationManagerService.this.mAssistants.isSameUser(token, r.getUserId())) {
                            NotificationManagerService.this.applyAdjustment(r, adjustment);
                            if (!adjustment.getSignals().containsKey("key_importance") || adjustment.getSignals().getInt("key_importance") != 0) {
                                needsSort = true;
                            } else {
                                cancelNotificationsFromListener(token, new String[]{r.getKey()});
                            }
                        }
                    }
                }
                if (needsSort) {
                    NotificationManagerService.this.mRankingHandler.requestSort();
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void updateNotificationChannelGroupFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user, NotificationChannelGroup group) throws RemoteException {
            Preconditions.checkNotNull(user);
            verifyPrivilegedListener(token, user, false);
            NotificationManagerService.this.createNotificationChannelGroup(pkg, getUidForPackageAndUser(pkg, user), group, false, true);
            NotificationManagerService.this.handleSavePolicyFile();
        }

        public void updateNotificationChannelFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user, NotificationChannel channel) throws RemoteException {
            Preconditions.checkNotNull(channel);
            Preconditions.checkNotNull(pkg);
            Preconditions.checkNotNull(user);
            verifyPrivilegedListener(token, user, false);
            NotificationManagerService.this.updateNotificationChannelInt(pkg, getUidForPackageAndUser(pkg, user), channel, true);
        }

        public ParceledListSlice<NotificationChannel> getNotificationChannelsFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user) throws RemoteException {
            Preconditions.checkNotNull(pkg);
            Preconditions.checkNotNull(user);
            verifyPrivilegedListener(token, user, true);
            return NotificationManagerService.this.mPreferencesHelper.getNotificationChannels(pkg, getUidForPackageAndUser(pkg, user), false);
        }

        public ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroupsFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user) throws RemoteException {
            Preconditions.checkNotNull(pkg);
            Preconditions.checkNotNull(user);
            verifyPrivilegedListener(token, user, true);
            List<NotificationChannelGroup> groups = new ArrayList<>();
            groups.addAll(NotificationManagerService.this.mPreferencesHelper.getNotificationChannelGroups(pkg, getUidForPackageAndUser(pkg, user)));
            return new ParceledListSlice<>(groups);
        }

        public void setPrivateNotificationsAllowed(boolean allow) {
            if (NotificationManagerService.this.getContext().checkCallingPermission("android.permission.CONTROL_KEYGUARD_SECURE_NOTIFICATIONS") != 0) {
                throw new SecurityException("Requires CONTROL_KEYGUARD_SECURE_NOTIFICATIONS permission");
            } else if (allow != NotificationManagerService.this.mLockScreenAllowSecureNotifications) {
                NotificationManagerService.this.mLockScreenAllowSecureNotifications = allow;
                NotificationManagerService.this.handleSavePolicyFile();
            }
        }

        public boolean getPrivateNotificationsAllowed() {
            if (NotificationManagerService.this.getContext().checkCallingPermission("android.permission.CONTROL_KEYGUARD_SECURE_NOTIFICATIONS") == 0) {
                return NotificationManagerService.this.mLockScreenAllowSecureNotifications;
            }
            throw new SecurityException("Requires CONTROL_KEYGUARD_SECURE_NOTIFICATIONS permission");
        }

        public boolean isPackagePaused(String pkg) {
            Preconditions.checkNotNull(pkg);
            NotificationManagerService.this.checkCallerIsSameApp(pkg);
            return NotificationManagerService.this.isPackagePausedOrSuspended(pkg, Binder.getCallingUid());
        }

        private void verifyPrivilegedListener(INotificationListener token, UserHandle user, boolean assistantAllowed) {
            ManagedServices.ManagedServiceInfo info;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
            }
            if (!NotificationManagerService.this.hasCompanionDevice(info)) {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    if (assistantAllowed) {
                        if (NotificationManagerService.this.mAssistants.isServiceTokenValidLocked(info.service)) {
                        }
                    }
                    throw new SecurityException(info + " does not have access");
                }
            }
            if (!info.enabledAndUserMatches(user.getIdentifier())) {
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

        /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.notification.NotificationManagerService$10 */
        /* JADX WARN: Multi-variable type inference failed */
        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
            new NotificationShellCmd(NotificationManagerService.this).exec(this, in, out, err, args, callback, resultReceiver);
        }
    };
    private SettingsObserver mSettingsObserver;
    private SnoozeHelper mSnoozeHelper;
    protected String mSoundNotificationKey;
    StatusBarManagerInternal mStatusBar;
    final ArrayMap<String, NotificationRecord> mSummaryByGroupKey = new ArrayMap<>();
    boolean mSystemReady;
    final ArrayList<ToastRecord> mToastQueue = new ArrayList<>();
    protected final Binder mToken = new Binder();
    private IUriGrantsManager mUgm;
    private UriGrantsManagerInternal mUgmInternal;
    protected UserManager mUm;
    private NotificationUsageStats mUsageStats;
    private boolean mUseAttentionLight;
    private final ManagedServices.UserProfiles mUserProfiles = new ManagedServices.UserProfiles();
    private String mVibrateNotificationKey;
    Vibrator mVibrator;
    private WindowManagerInternal mWindowManagerInternal;
    protected ZenModeHelper mZenModeHelper;

    /* access modifiers changed from: private */
    public interface FlagChecker {
        boolean apply(int i);
    }

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    /* access modifiers changed from: private */
    public static class Archive {
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

    /* access modifiers changed from: protected */
    public void readDefaultApprovedServices(int userId) {
        String defaultListenerAccess = readDefaultApprovedFromWhiteList(getContext().getResources().getString(17039815));
        if (!TextUtils.isEmpty(defaultListenerAccess)) {
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
        String defaultDndAccess = getContext().getResources().getString(17039814);
        if (defaultDndAccess != null) {
            for (String whitelisted2 : defaultDndAccess.split(":")) {
                try {
                    getBinderService().setNotificationPolicyAccessGranted(whitelisted2, true);
                } catch (RemoteException e2) {
                    e2.printStackTrace();
                }
            }
        }
        setDefaultAssistantForUser(userId);
    }

    /* access modifiers changed from: protected */
    public void setDefaultAssistantForUser(int userId) {
        List<ComponentName> validAssistants = new ArrayList<>(this.mAssistants.queryPackageForServices(null, 786432, userId));
        List<String> candidateStrs = new ArrayList<>();
        candidateStrs.add(DeviceConfig.getProperty("systemui", "nas_default_service"));
        candidateStrs.add(getContext().getResources().getString(17039808));
        for (String candidateStr : candidateStrs) {
            if (!TextUtils.isEmpty(candidateStr)) {
                ComponentName candidate = ComponentName.unflattenFromString(candidateStr);
                if (candidate == null || !validAssistants.contains(candidate)) {
                    Slog.w(TAG, "Invalid default NAS config is found: " + candidateStr);
                } else {
                    setNotificationAssistantAccessGrantedForUserInternal(candidate, userId, true);
                    Slog.d(TAG, String.format("Set default NAS to be %s in %d", candidateStr, Integer.valueOf(userId)));
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readPolicyXml(InputStream stream, boolean forRestore, int userId) throws XmlPullParserException, NumberFormatException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(stream, StandardCharsets.UTF_8.name());
        XmlUtils.beginDocument(parser, TAG_NOTIFICATION_POLICY);
        boolean migratedManagedServices = false;
        boolean ineligibleForManagedServices = forRestore && this.mUm.isManagedProfile(userId);
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if ("zen".equals(parser.getName())) {
                this.mZenModeHelper.readXml(parser, forRestore, userId);
            } else if ("ranking".equals(parser.getName())) {
                this.mPreferencesHelper.readXml(parser, forRestore, userId);
            }
            if (this.mListeners.getConfig().xmlTag.equals(parser.getName())) {
                if (!ineligibleForManagedServices) {
                    this.mListeners.readXml(parser, this.mAllowedManagedServicePackages, forRestore, userId);
                    migratedManagedServices = true;
                }
            } else if (this.mAssistants.getConfig().xmlTag.equals(parser.getName())) {
                if (!ineligibleForManagedServices) {
                    this.mAssistants.readXml(parser, this.mAllowedManagedServicePackages, forRestore, userId);
                    migratedManagedServices = true;
                }
            } else if (this.mConditionProviders.getConfig().xmlTag.equals(parser.getName())) {
                if (!ineligibleForManagedServices) {
                    this.mConditionProviders.readXml(parser, this.mAllowedManagedServicePackages, forRestore, userId);
                    migratedManagedServices = true;
                }
            }
            if (LOCKSCREEN_ALLOW_SECURE_NOTIFICATIONS_TAG.equals(parser.getName()) && (!forRestore || userId == 0)) {
                this.mLockScreenAllowSecureNotifications = safeBoolean(parser.getAttributeValue(null, LOCKSCREEN_ALLOW_SECURE_NOTIFICATIONS_VALUE), true);
            }
        }
        if (!migratedManagedServices) {
            this.mListeners.migrateToXml();
            this.mAssistants.migrateToXml();
            this.mConditionProviders.migrateToXml();
            handleSavePolicyFile();
        }
        this.mAssistants.resetDefaultAssistantsIfNecessary();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void loadPolicyFile() {
        if (DBG) {
            Slog.d(TAG, "loadPolicyFile");
        }
        synchronized (this.mPolicyFile) {
            InputStream infile = null;
            try {
                infile = this.mPolicyFile.openRead();
                readPolicyXml(infile, false, -1);
            } catch (FileNotFoundException e) {
                readDefaultApprovedServices(0);
            } catch (IOException e2) {
                Log.wtf(TAG, "Unable to read notification policy", e2);
            } catch (NumberFormatException e3) {
                Log.wtf(TAG, "Unable to parse notification policy", e3);
            } catch (XmlPullParserException e4) {
                Log.wtf(TAG, "Unable to parse notification policy", e4);
            } finally {
                IoUtils.closeQuietly(infile);
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void handleSavePolicyFile() {
        IoThread.getHandler().post(new Runnable() {
            /* class com.android.server.notification.$$Lambda$NotificationManagerService$Ehw7Jxsy3ZIwTcAa2DFsHjIULas */

            @Override // java.lang.Runnable
            public final void run() {
                NotificationManagerService.this.lambda$handleSavePolicyFile$0$NotificationManagerService();
            }
        });
    }

    public /* synthetic */ void lambda$handleSavePolicyFile$0$NotificationManagerService() {
        if (DBG) {
            Slog.d(TAG, "handleSavePolicyFile");
        }
        synchronized (this.mPolicyFile) {
            try {
                FileOutputStream stream = this.mPolicyFile.startWrite();
                try {
                    writePolicyXml(stream, false, -1);
                    this.mPolicyFile.finishWrite(stream);
                } catch (IllegalArgumentException e) {
                    Slog.w(TAG, "handleSavePolicyFile: Get IllegalArgumentException.");
                } catch (IOException e2) {
                    Slog.w(TAG, "Failed to save policy file, restoring backup", e2);
                    this.mPolicyFile.failWrite(stream);
                }
            } catch (IOException e3) {
                Slog.w(TAG, "Failed to save policy file", e3);
                return;
            }
        }
        BackupManager.dataChanged(getContext().getPackageName());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writePolicyXml(OutputStream stream, boolean forBackup, int userId) throws IOException {
        XmlSerializer out = new FastXmlSerializer();
        out.setOutput(stream, StandardCharsets.UTF_8.name());
        out.startDocument(null, true);
        out.startTag(null, TAG_NOTIFICATION_POLICY);
        out.attribute(null, ATTR_VERSION, Integer.toString(1));
        this.mZenModeHelper.writeXml(out, forBackup, null, userId);
        this.mPreferencesHelper.writeXml(out, forBackup, userId);
        this.mListeners.writeXml(out, forBackup, userId);
        this.mAssistants.writeXml(out, forBackup, userId);
        this.mConditionProviders.writeXml(out, forBackup, userId);
        if (!forBackup || userId == 0) {
            writeSecureNotificationsPolicy(out);
        }
        out.endTag(null, TAG_NOTIFICATION_POLICY);
        out.endDocument();
    }

    /* access modifiers changed from: private */
    public static final class ToastRecord {
        final ITransientNotification callback;
        int displayId;
        int duration;
        final int pid;
        final String pkg;
        Binder token;

        ToastRecord(int pid2, String pkg2, ITransientNotification callback2, int duration2, Binder token2, int displayId2) {
            this.pid = pid2;
            this.pkg = pkg2;
            this.callback = callback2;
            this.duration = duration2;
            this.token = token2;
            this.displayId = displayId2;
        }

        /* access modifiers changed from: package-private */
        public void update(int duration2) {
            this.duration = duration2;
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

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void logSmartSuggestionsVisible(NotificationRecord r, int notificationLocation) {
        if ((r.getNumSmartRepliesAdded() > 0 || r.getNumSmartActionsAdded() > 0) && !r.hasSeenSmartReplies()) {
            r.setSeenSmartReplies(true);
            this.mMetricsLogger.write(r.getLogMaker().setCategory(1382).addTaggedData(1384, Integer.valueOf(r.getNumSmartRepliesAdded())).addTaggedData(1599, Integer.valueOf(r.getNumSmartActionsAdded())).addTaggedData(1600, Integer.valueOf(r.getSuggestionsGeneratedByAssistant() ? 1 : 0)).addTaggedData(1629, Integer.valueOf(notificationLocation)).addTaggedData(1647, Integer.valueOf(r.getEditChoicesBeforeSending() ? 1 : 0)));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNotificationLock"})
    private void clearSoundLocked() {
        this.mSoundNotificationKey = null;
        long identity = Binder.clearCallingIdentity();
        try {
            if (this.mRingtone != null) {
                this.mRingtone.stop();
            }
            IRingtonePlayer player = this.mAudioManager.getRingtonePlayer();
            if (player != null) {
                player.stopAsync();
            }
        } catch (RemoteException e) {
        } catch (Throwable player2) {
            Binder.restoreCallingIdentity(identity);
            throw player2;
        }
        Binder.restoreCallingIdentity(identity);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNotificationLock"})
    private void clearVibrateLocked() {
        this.mVibrateNotificationKey = null;
        long identity = Binder.clearCallingIdentity();
        try {
            HwVibrator.stopHwVibrator(Process.myUid(), ActivityThread.currentPackageName(), this.mToken, NOTIFICATION_VIBRATE);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNotificationLock"})
    private void clearLightsLocked() {
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

    /* access modifiers changed from: private */
    public final class SettingsObserver extends ContentObserver {
        private final Uri NOTIFICATION_BADGING_URI = Settings.Secure.getUriFor("notification_badging");
        private final Uri NOTIFICATION_BUBBLES_URI = Settings.Secure.getUriFor("notification_bubbles");
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
            resolver.registerContentObserver(this.NOTIFICATION_BUBBLES_URI, false, this, -1);
            update(null);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            ContentResolver resolver = NotificationManagerService.this.getContext().getContentResolver();
            if (uri == null || this.NOTIFICATION_LIGHT_PULSE_URI.equals(uri)) {
                boolean pulseEnabled = false;
                if (Settings.System.getIntForUser(resolver, "notification_light_pulse", 0, -2) != 0) {
                    pulseEnabled = true;
                }
                if (NotificationManagerService.this.mNotificationPulseEnabled != pulseEnabled) {
                    NotificationManagerService notificationManagerService = NotificationManagerService.this;
                    notificationManagerService.mNotificationPulseEnabled = pulseEnabled;
                    notificationManagerService.updateNotificationPulse();
                }
            }
            if (uri == null || this.NOTIFICATION_RATE_LIMIT_URI.equals(uri)) {
                NotificationManagerService notificationManagerService2 = NotificationManagerService.this;
                notificationManagerService2.mMaxPackageEnqueueRate = Settings.Global.getFloat(resolver, "max_notification_enqueue_rate", notificationManagerService2.mMaxPackageEnqueueRate);
            }
            if (uri == null || this.NOTIFICATION_BADGING_URI.equals(uri)) {
                NotificationManagerService.this.mPreferencesHelper.updateBadgingEnabled();
            }
            if (uri == null || this.NOTIFICATION_BUBBLES_URI.equals(uri)) {
                NotificationManagerService.this.mPreferencesHelper.updateBubblesEnabled();
            }
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

    /* JADX WARN: Type inference failed for: r0v11, types: [com.android.server.notification.NotificationManagerService$10, android.os.IBinder] */
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
    public void setHints(int hints) {
        this.mListenerHints = hints;
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
    public void setPreferencesHelper(PreferencesHelper prefHelper) {
        this.mPreferencesHelper = prefHelper;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setRankingHandler(RankingHandler rankingHandler) {
        this.mRankingHandler = rankingHandler;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setZenHelper(ZenModeHelper zenHelper) {
        this.mZenModeHelper = zenHelper;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setIsAutomotive(boolean isAutomotive) {
        this.mIsAutomotive = isAutomotive;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setNotificationEffectsEnabledForAutomotive(boolean isEnabled) {
        this.mNotificationEffectsEnabledForAutomotive = isEnabled;
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
    public void init(Looper looper, IPackageManager packageManager, PackageManager packageManagerClient, LightsManager lightsManager, NotificationListeners notificationListeners, NotificationAssistants notificationAssistants, ConditionProviders conditionProviders, ICompanionDeviceManager companionManager, SnoozeHelper snoozeHelper, NotificationUsageStats usageStats, AtomicFile policyFile, ActivityManager activityManager, GroupHelper groupHelper, IActivityManager am, UsageStatsManagerInternal appUsageStats, DevicePolicyManagerInternal dpm, IUriGrantsManager ugm, UriGrantsManagerInternal ugmInternal, AppOpsManager appOps, UserManager userManager) {
        String[] extractorNames;
        Resources resources = getContext().getResources();
        this.mMaxPackageEnqueueRate = Settings.Global.getFloat(getContext().getContentResolver(), "max_notification_enqueue_rate", DEFAULT_MAX_NOTIFICATION_ENQUEUE_RATE);
        this.mAccessibilityManager = (AccessibilityManager) getContext().getSystemService("accessibility");
        this.mAm = am;
        this.mUgm = ugm;
        this.mUgmInternal = ugmInternal;
        this.mPackageManager = packageManager;
        this.mPackageManagerClient = packageManagerClient;
        this.mAppOps = appOps;
        this.mVibrator = (Vibrator) getContext().getSystemService("vibrator");
        this.mAppUsageStats = appUsageStats;
        this.mAlarmManager = (AlarmManager) getContext().getSystemService("alarm");
        this.mCompanionManager = companionManager;
        this.mActivityManager = activityManager;
        this.mDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
        this.mDpm = dpm;
        this.mUm = userManager;
        this.mHandler = new WorkerHandler(looper);
        this.mRankingThread.start();
        try {
            extractorNames = resources.getStringArray(17236046);
        } catch (Resources.NotFoundException e) {
            extractorNames = new String[0];
        }
        this.mUsageStats = usageStats;
        this.mMetricsLogger = new MetricsLogger();
        this.mRankingHandler = new RankingHandlerWorker(this.mRankingThread.getLooper());
        this.mConditionProviders = conditionProviders;
        this.mZenModeHelper = new ZenModeHelper(getContext(), this.mHandler.getLooper(), this.mConditionProviders);
        this.mZenModeHelper.addCallback(new ZenModeHelper.Callback() {
            /* class com.android.server.notification.NotificationManagerService.AnonymousClass7 */

            @Override // com.android.server.notification.ZenModeHelper.Callback
            public void onConfigChanged() {
                NotificationManagerService.this.handleSavePolicyFile();
            }

            /* access modifiers changed from: package-private */
            @Override // com.android.server.notification.ZenModeHelper.Callback
            public void onZenModeChanged() {
                NotificationManagerService.this.sendRegisteredOnlyBroadcast("android.app.action.INTERRUPTION_FILTER_CHANGED");
                NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.INTERRUPTION_FILTER_CHANGED_INTERNAL").addFlags(DumpState.DUMP_HANDLE), UserHandle.ALL, "android.permission.MANAGE_NOTIFICATIONS");
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.updateInterruptionFilterLocked();
                }
                NotificationManagerService.this.mRankingHandler.requestSort();
            }

            /* access modifiers changed from: package-private */
            @Override // com.android.server.notification.ZenModeHelper.Callback
            public void onPolicyChanged() {
                NotificationManagerService.this.sendRegisteredOnlyBroadcast("android.app.action.NOTIFICATION_POLICY_CHANGED");
                NotificationManagerService.this.mRankingHandler.requestSort();
            }
        });
        this.mPreferencesHelper = new PreferencesHelper(getContext(), this.mPackageManagerClient, this.mRankingHandler, this.mZenModeHelper);
        this.mRankingHelper = new RankingHelper(getContext(), this.mRankingHandler, this.mPreferencesHelper, this.mZenModeHelper, this.mUsageStats, extractorNames);
        this.mSnoozeHelper = snoozeHelper;
        this.mGroupHelper = groupHelper;
        this.mListeners = notificationListeners;
        this.mAssistants = notificationAssistants;
        this.mAllowedManagedServicePackages = new TriPredicate() {
            /* class com.android.server.notification.$$Lambda$V4J7df5A6vhSIuw7Ym9xgkfahto */

            public final boolean test(Object obj, Object obj2, Object obj3) {
                return NotificationManagerService.this.canUseManagedServices((String) obj, (Integer) obj2, (String) obj3);
            }
        };
        this.mPolicyFile = policyFile;
        loadPolicyFile();
        this.mStatusBar = (StatusBarManagerInternal) getLocalService(StatusBarManagerInternal.class);
        StatusBarManagerInternal statusBarManagerInternal = this.mStatusBar;
        if (statusBarManagerInternal != null) {
            statusBarManagerInternal.setNotificationDelegate(this.mNotificationDelegate);
        }
        this.mNotificationLight = lightsManager.getLight(4);
        this.mAttentionLight = lightsManager.getLight(5);
        this.mFallbackVibrationPattern = getLongArray(resources, 17236045, 17, DEFAULT_VIBRATE_PATTERN);
        this.mInCallNotificationUri = Uri.parse("file://" + resources.getString(17039852));
        this.mInCallNotificationAudioAttributes = new AudioAttributes.Builder().setContentType(4).setUsage(2).build();
        this.mInCallNotificationVolume = resources.getFloat(17105062);
        this.mUseAttentionLight = resources.getBoolean(17891558);
        this.mHasLight = resources.getBoolean(17891471);
        boolean z = true;
        if (Settings.Global.getInt(getContext().getContentResolver(), "device_provisioned", 0) == 0) {
            this.mDisableNotificationEffects = true;
        }
        this.mZenModeHelper.initZenMode();
        this.mInterruptionFilter = this.mZenModeHelper.getZenModeListenerInterruptionFilter();
        this.mUserProfiles.updateCache(getContext());
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mArchive = new Archive(resources.getInteger(17694862));
        if (!this.mPackageManagerClient.hasSystemFeature("android.software.leanback") && !this.mPackageManagerClient.hasSystemFeature("android.hardware.type.television")) {
            z = false;
        }
        this.mIsTelevision = z;
        this.mIsAutomotive = this.mPackageManagerClient.hasSystemFeature("android.hardware.type.automotive", 0);
        this.mNotificationEffectsEnabledForAutomotive = resources.getBoolean(17891451);
        this.mPreferencesHelper.lockChannelsForOEM(getContext().getResources().getStringArray(17236044));
        this.mZenModeHelper.setPriorityOnlyDndExemptPackages(getContext().getResources().getStringArray(17236048));
        this.iHwNotificationManagerServiceEx = HwServiceExFactory.getHwNotificationManagerServiceEx();
        IHwNotificationManagerServiceEx iHwNotificationManagerServiceEx2 = this.iHwNotificationManagerServiceEx;
        if (iHwNotificationManagerServiceEx2 != null) {
            iHwNotificationManagerServiceEx2.init(getContext());
        }
    }

    @Override // com.android.server.SystemService
    public void onStart() {
        init(Looper.myLooper(), AppGlobals.getPackageManager(), getContext().getPackageManager(), (LightsManager) getLocalService(LightsManager.class), new NotificationListeners(AppGlobals.getPackageManager()), new NotificationAssistants(getContext(), this.mNotificationLock, this.mUserProfiles, AppGlobals.getPackageManager()), new ConditionProviders(getContext(), this.mUserProfiles, AppGlobals.getPackageManager()), null, new SnoozeHelper(getContext(), new SnoozeHelper.Callback() {
            /* class com.android.server.notification.NotificationManagerService.AnonymousClass8 */

            @Override // com.android.server.notification.SnoozeHelper.Callback
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
        }, this.mUserProfiles), new NotificationUsageStats(getContext()), new AtomicFile(new File(new File(Environment.getDataDirectory(), "system"), "notification_policy.xml"), TAG_NOTIFICATION_POLICY), (ActivityManager) getContext().getSystemService("activity"), getGroupHelper(), ActivityManager.getService(), (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class), (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class), UriGrantsManager.getService(), (UriGrantsManagerInternal) LocalServices.getService(UriGrantsManagerInternal.class), (AppOpsManager) getContext().getSystemService("appops"), (UserManager) getContext().getSystemService(UserManager.class));
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
        getContext().registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, filter, null, null);
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
        suspendedPkgFilter.addAction("android.intent.action.DISTRACTING_PACKAGES_CHANGED");
        getContext().registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.ALL, suspendedPkgFilter, null, null);
        getContext().registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE"), null, null);
        IntentFilter timeoutFilter = new IntentFilter(ACTION_NOTIFICATION_TIMEOUT);
        timeoutFilter.addDataScheme(SCHEME_TIMEOUT);
        getContext().registerReceiver(this.mNotificationTimeoutReceiver, timeoutFilter);
        getContext().registerReceiver(this.mRestoreReceiver, new IntentFilter("android.os.action.SETTING_RESTORED"));
        getContext().registerReceiver(this.mLocaleChangeReceiver, new IntentFilter("android.intent.action.LOCALE_CHANGED"));
        publishBinderService("notification", this.mService, false, 5);
        publishLocalService(NotificationManagerInternal.class, this.mInternalService);
    }

    private void registerDeviceConfigChange() {
        DeviceConfig.addOnPropertiesChangedListener("systemui", getContext().getMainExecutor(), new DeviceConfig.OnPropertiesChangedListener() {
            /* class com.android.server.notification.$$Lambda$NotificationManagerService$NFdAeB4Fj_ZP4GXkIVrEH_Cxj8 */

            public final void onPropertiesChanged(DeviceConfig.Properties properties) {
                NotificationManagerService.this.lambda$registerDeviceConfigChange$1$NotificationManagerService(properties);
            }
        });
    }

    public /* synthetic */ void lambda$registerDeviceConfigChange$1$NotificationManagerService(DeviceConfig.Properties properties) {
        if ("systemui".equals(properties.getNamespace()) && properties.getKeyset().contains("nas_default_service")) {
            this.mAssistants.resetDefaultAssistantsIfNecessary();
        }
    }

    private GroupHelper getGroupHelper() {
        this.mAutoGroupAtCount = getContext().getResources().getInteger(17694742);
        return new GroupHelper(this.mAutoGroupAtCount, new GroupHelper.Callback() {
            /* class com.android.server.notification.NotificationManagerService.AnonymousClass9 */

            @Override // com.android.server.notification.GroupHelper.Callback
            public void addAutoGroup(String key) {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.addAutogroupKeyLocked(key);
                }
            }

            @Override // com.android.server.notification.GroupHelper.Callback
            public void removeAutoGroup(String key) {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.removeAutogroupKeyLocked(key);
                }
            }

            @Override // com.android.server.notification.GroupHelper.Callback
            public void addAutoGroupSummary(int userId, String pkg, String triggeringKey) {
                NotificationManagerService.this.createAutoGroupSummary(userId, pkg, triggeringKey);
            }

            @Override // com.android.server.notification.GroupHelper.Callback
            public void removeAutoGroupSummary(int userId, String pkg) {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.clearAutogroupSummaryLocked(userId, pkg);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendRegisteredOnlyBroadcast(String action) {
        Intent intent = new Intent(action);
        getContext().sendBroadcastAsUser(intent.addFlags(1073741824), UserHandle.ALL, null);
        intent.setFlags(0);
        for (String pkg : this.mConditionProviders.getAllowedPackages()) {
            intent.setPackage(pkg);
            getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mSystemReady = true;
            this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
            this.mAudioManagerInternal = (AudioManagerInternal) getLocalService(AudioManagerInternal.class);
            this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            this.mZenModeHelper.onSystemReady();
            this.mRoleObserver = new RoleObserver((RoleManager) getContext().getSystemService(RoleManager.class), this.mPackageManager, getContext().getMainExecutor());
            this.mRoleObserver.init();
        } else if (phase == 600) {
            this.mSettingsObserver.observe();
            this.mListeners.onBootPhaseAppsCanStart();
            this.mAssistants.onBootPhaseAppsCanStart();
            this.mConditionProviders.onBootPhaseAppsCanStart();
            registerDeviceConfigChange();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNotificationLock"})
    private void updateListenerHintsLocked() {
        int hints = calculateHints();
        int i = this.mListenerHints;
        if (hints != i) {
            ZenLog.traceListenerHintsChanged(i, hints, this.mEffectsSuppressors.size());
            this.mListenerHints = hints;
            scheduleListenerHintsChanged(hints);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNotificationLock"})
    private void updateEffectsSuppressorLocked() {
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
    /* access modifiers changed from: public */
    private void exitIdle() {
        try {
            if (this.mDeviceIdleController != null) {
                this.mDeviceIdleController.exitIdle("notification interaction");
            }
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNotificationChannelInt(String pkg, int uid, NotificationChannel channel, boolean fromListener) {
        if (channel.getImportance() == 0) {
            cancelAllNotificationsInt(MY_UID, MY_PID, pkg, channel.getId(), 0, 0, true, UserHandle.getUserId(uid), 17, null);
            if (isUidSystemOrPhone(uid)) {
                IntArray profileIds = this.mUserProfiles.getCurrentProfileIds();
                int i = 0;
                for (int N = profileIds.size(); i < N; N = N) {
                    cancelAllNotificationsInt(MY_UID, MY_PID, pkg, channel.getId(), 0, 0, true, profileIds.get(i), 17, null);
                    i++;
                }
            }
        }
        NotificationChannel preUpdate = this.mPreferencesHelper.getNotificationChannel(pkg, uid, channel.getId(), true);
        this.mPreferencesHelper.updateNotificationChannel(pkg, uid, channel, true);
        maybeNotifyChannelOwner(pkg, uid, preUpdate, channel);
        if (!fromListener) {
            this.mListeners.notifyNotificationChannelChanged(pkg, UserHandle.getUserHandleForUid(uid), this.mPreferencesHelper.getNotificationChannel(pkg, uid, channel.getId(), false), 2);
        }
        handleSavePolicyFile();
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
    /* access modifiers changed from: public */
    private void createNotificationChannelGroup(String pkg, int uid, NotificationChannelGroup group, boolean fromApp, boolean fromListener) {
        Preconditions.checkNotNull(group);
        Preconditions.checkNotNull(pkg);
        NotificationChannelGroup preUpdate = this.mPreferencesHelper.getNotificationChannelGroup(group.getId(), pkg, uid);
        this.mPreferencesHelper.createNotificationChannelGroup(pkg, uid, group, fromApp);
        if (!fromApp) {
            maybeNotifyChannelGroupOwner(pkg, uid, preUpdate, group);
        }
        if (!fromListener) {
            this.mListeners.notifyNotificationChannelGroupChanged(pkg, UserHandle.of(UserHandle.getCallingUserId()), group, 1);
        }
    }

    private void maybeNotifyChannelGroupOwner(String pkg, int uid, NotificationChannelGroup preUpdate, NotificationChannelGroup update) {
        if (preUpdate != null) {
            try {
                if (preUpdate.isBlocked() != update.isBlocked()) {
                    getContext().sendBroadcastAsUser(new Intent("android.app.action.NOTIFICATION_CHANNEL_GROUP_BLOCK_STATE_CHANGED").putExtra("android.app.extra.NOTIFICATION_CHANNEL_GROUP_ID", update.getId()).putExtra("android.app.extra.BLOCKED_STATE", update.isBlocked()).addFlags(268435456).setPackage(pkg), UserHandle.of(UserHandle.getUserId(uid)), null);
                }
            } catch (SecurityException e) {
                Slog.w(TAG, "Can't notify app about group change", e);
            }
        }
    }

    private ArrayList<ComponentName> getSuppressors() {
        ArrayList<ComponentName> names = new ArrayList<>();
        for (int i = this.mListenersDisablingEffects.size() - 1; i >= 0; i--) {
            Iterator<ComponentName> it = this.mListenersDisablingEffects.valueAt(i).iterator();
            while (it.hasNext()) {
                names.add(it.next());
            }
        }
        return names;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean removeDisabledHints(ManagedServices.ManagedServiceInfo info) {
        return removeDisabledHints(info, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean removeDisabledHints(ManagedServices.ManagedServiceInfo info, int hints) {
        boolean removed = false;
        for (int i = this.mListenersDisablingEffects.size() - 1; i >= 0; i--) {
            int hint = this.mListenersDisablingEffects.keyAt(i);
            ArraySet<ComponentName> listeners = this.mListenersDisablingEffects.valueAt(i);
            if (hints == 0 || (hint & hints) == hint) {
                removed |= listeners.remove(info.component);
            }
        }
        return removed;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addDisabledHints(ManagedServices.ManagedServiceInfo info, int hints) {
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
            this.mListenersDisablingEffects.put(hint, new ArraySet<>());
        }
        this.mListenersDisablingEffects.get(hint).add(info.component);
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
    /* access modifiers changed from: public */
    @GuardedBy({"mNotificationLock"})
    private void updateInterruptionFilterLocked() {
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
    @GuardedBy({"mNotificationLock"})
    public void reportSeen(NotificationRecord r) {
        if (!r.isProxied()) {
            this.mAppUsageStats.reportEvent(r.sbn.getPackageName(), getRealUserId(r.sbn.getUserId()), 10);
        }
    }

    /* access modifiers changed from: protected */
    public int calculateSuppressedVisualEffects(NotificationManager.Policy incomingPolicy, NotificationManager.Policy currPolicy, int targetSdkVersion) {
        if (incomingPolicy.suppressedVisualEffects == -1) {
            return incomingPolicy.suppressedVisualEffects;
        }
        int[] effectsIntroducedInP = {4, 8, 16, 32, 64, 128, 256};
        int newSuppressedVisualEffects = incomingPolicy.suppressedVisualEffects;
        if (targetSdkVersion < 28) {
            for (int i = 0; i < effectsIntroducedInP.length; i++) {
                newSuppressedVisualEffects = (newSuppressedVisualEffects & (~effectsIntroducedInP[i])) | (currPolicy.suppressedVisualEffects & effectsIntroducedInP[i]);
            }
            if ((newSuppressedVisualEffects & 1) != 0) {
                newSuppressedVisualEffects = newSuppressedVisualEffects | 8 | 4;
            }
            if ((newSuppressedVisualEffects & 2) != 0) {
                return newSuppressedVisualEffects | 16;
            }
            return newSuppressedVisualEffects;
        }
        boolean hasNewEffects = true;
        if ((newSuppressedVisualEffects - 2) - 1 <= 0) {
            hasNewEffects = false;
        }
        if (hasNewEffects) {
            int newSuppressedVisualEffects2 = newSuppressedVisualEffects & -4;
            if ((newSuppressedVisualEffects2 & 16) != 0) {
                newSuppressedVisualEffects2 |= 2;
            }
            if ((newSuppressedVisualEffects2 & 8) == 0 || (newSuppressedVisualEffects2 & 4) == 0 || (newSuppressedVisualEffects2 & 128) == 0) {
                return newSuppressedVisualEffects2;
            }
            return newSuppressedVisualEffects2 | 1;
        }
        if ((newSuppressedVisualEffects & 1) != 0) {
            newSuppressedVisualEffects = newSuppressedVisualEffects | 8 | 4 | 128;
        }
        if ((newSuppressedVisualEffects & 2) != 0) {
            return newSuppressedVisualEffects | 16;
        }
        return newSuppressedVisualEffects;
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mNotificationLock"})
    public void maybeRecordInterruptionLocked(NotificationRecord r) {
        if (r.isInterruptive() && !r.hasRecordedInterruption()) {
            this.mAppUsageStats.reportInterruptiveNotification(r.sbn.getPackageName(), r.getChannel().getId(), getRealUserId(r.sbn.getUserId()));
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

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void setNotificationAssistantAccessGrantedForUserInternal(ComponentName assistant, int baseUserId, boolean granted) {
        List<UserInfo> users = this.mUm.getEnabledProfiles(baseUserId);
        if (users != null) {
            for (UserInfo user : users) {
                int userId = user.id;
                if (assistant == null) {
                    ComponentName allowedAssistant = (ComponentName) CollectionUtils.firstOrNull(this.mAssistants.getAllowedComponents(userId));
                    if (allowedAssistant != null) {
                        setNotificationAssistantAccessGrantedForUserInternal(allowedAssistant, userId, false);
                    }
                } else if (!granted || this.mAllowedManagedServicePackages.test(assistant.getPackageName(), Integer.valueOf(userId), this.mAssistants.getRequiredPermission())) {
                    this.mConditionProviders.setPackageOrComponentEnabled(assistant.flattenToString(), userId, false, granted);
                    this.mAssistants.setPackageOrComponentEnabled(assistant.flattenToString(), userId, true, granted);
                    getContext().sendBroadcastAsUser(new Intent("android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED").setPackage(assistant.getPackageName()).addFlags(1073741824), UserHandle.of(userId), null);
                    handleSavePolicyFile();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void applyAdjustment(NotificationRecord r, Adjustment adjustment) {
        if (!(r == null || adjustment.getSignals() == null)) {
            Bundle adjustments = adjustment.getSignals();
            Bundle.setDefusable(adjustments, true);
            List<String> toRemove = new ArrayList<>();
            for (String potentialKey : adjustments.keySet()) {
                if (!this.mAssistants.isAdjustmentAllowed(potentialKey)) {
                    toRemove.add(potentialKey);
                }
            }
            for (String removeKey : toRemove) {
                adjustments.remove(removeKey);
            }
            r.addAdjustment(adjustment);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mNotificationLock"})
    public void addAutogroupKeyLocked(String key) {
        NotificationRecord r = this.mNotificationsByKey.get(key);
        if (r != null && r.sbn.getOverrideGroupKey() == null) {
            addAutoGroupAdjustment(r, "ranker_group");
            EventLogTags.writeNotificationAutogrouped(key);
            this.mRankingHandler.requestSort();
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mNotificationLock"})
    public void removeAutogroupKeyLocked(String key) {
        NotificationRecord r = this.mNotificationsByKey.get(key);
        if (r != null && r.sbn.getOverrideGroupKey() != null) {
            addAutoGroupAdjustment(r, null);
            EventLogTags.writeNotificationUnautogrouped(key);
            this.mRankingHandler.requestSort();
        }
    }

    private void addAutoGroupAdjustment(NotificationRecord r, String overrideGroupKey) {
        Bundle signals = new Bundle();
        signals.putString("key_group_key", overrideGroupKey);
        r.addAdjustment(new Adjustment(r.sbn.getPackageName(), r.getKey(), signals, "", r.sbn.getUserId()));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNotificationLock"})
    private void clearAutogroupSummaryLocked(int userId, String pkg) {
        NotificationRecord removed;
        ArrayMap<String, String> summaries = this.mAutobundledSummaries.get(Integer.valueOf(userId));
        if (summaries != null && summaries.containsKey(pkg) && (removed = findNotificationByKeyLocked(summaries.remove(pkg))) != null) {
            cancelNotificationLocked(removed, false, 16, removeFromNotificationListsLocked(removed), null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNotificationLock"})
    private boolean hasAutoGroupSummaryLocked(StatusBarNotification sbn) {
        ArrayMap<String, String> summaries = this.mAutobundledSummaries.get(Integer.valueOf(sbn.getUserId()));
        return summaries != null && summaries.containsKey(sbn.getPackageName());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createAutoGroupSummary(int userId, String pkg, String triggeringKey) {
        Throwable th;
        int userId2;
        NotificationRecord summaryRecord;
        synchronized (this.mNotificationLock) {
            try {
                try {
                    NotificationRecord notificationRecord = this.mNotificationsByKey.get(triggeringKey);
                    if (notificationRecord == null) {
                        try {
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } else {
                        StatusBarNotification adjustedSbn = notificationRecord.sbn;
                        userId2 = adjustedSbn.getUser().getIdentifier();
                        try {
                            ArrayMap<String, String> summaries = this.mAutobundledSummaries.get(Integer.valueOf(userId2));
                            if (summaries == null) {
                                try {
                                    summaries = new ArrayMap<>();
                                } catch (Throwable th3) {
                                    th = th3;
                                    throw th;
                                }
                            }
                            this.mAutobundledSummaries.put(Integer.valueOf(userId2), summaries);
                            if (!summaries.containsKey(pkg)) {
                                Bundle extras = new Bundle();
                                extras.putParcelable("android.appInfo", (ApplicationInfo) adjustedSbn.getNotification().extras.getParcelable("android.appInfo"));
                                Notification summaryNotification = new Notification.Builder(getContext(), notificationRecord.getChannel().getId()).setSmallIcon(adjustedSbn.getNotification().getSmallIcon()).setGroupSummary(true).setGroupAlertBehavior(2).setGroup("ranker_group").setFlag(1024, true).setFlag(512, true).setColor(adjustedSbn.getNotification().color).setLocalOnly(true).build();
                                summaryNotification.extras.putAll(extras);
                                Intent appIntent = getContext().getPackageManager().getLaunchIntentForPackage(pkg);
                                if (appIntent != null) {
                                    summaryNotification.contentIntent = PendingIntent.getActivityAsUser(getContext(), 0, appIntent, 0, null, UserHandle.of(userId2));
                                }
                                StatusBarNotification summarySbn = new StatusBarNotification(adjustedSbn.getPackageName(), adjustedSbn.getOpPkg(), Integer.MAX_VALUE, "ranker_group", adjustedSbn.getUid(), adjustedSbn.getInitialPid(), summaryNotification, adjustedSbn.getUser(), "ranker_group", System.currentTimeMillis());
                                try {
                                    NotificationRecord summaryRecord2 = new NotificationRecord(getContext(), summarySbn, notificationRecord.getChannel());
                                    summaryRecord2.setIsAppImportanceLocked(notificationRecord.getIsAppImportanceLocked());
                                    summaries.put(pkg, summarySbn.getKey());
                                    summaryRecord = summaryRecord2;
                                } catch (Throwable th4) {
                                    th = th4;
                                    throw th;
                                }
                            } else {
                                summaryRecord = null;
                            }
                            try {
                            } catch (Throwable th5) {
                                th = th5;
                                throw th;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            throw th;
                        }
                    }
                } catch (Throwable th7) {
                    th = th7;
                    throw th;
                }
            } catch (Throwable th8) {
                th = th8;
                throw th;
            }
        }
        if (summaryRecord != null && checkDisqualifyingFeatures(userId2, MY_UID, summaryRecord.sbn.getId(), summaryRecord.sbn.getTag(), summaryRecord, true)) {
            this.mHandler.post(new EnqueueNotificationRunnable(userId2, summaryRecord));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String disableNotificationEffects(NotificationRecord record) {
        if (this.mDisableNotificationEffects) {
            return "booleanState";
        }
        if ((this.mListenerHints & 1) != 0) {
            return "listenerHints";
        }
        if (!(record == null || record.getAudioAttributes() == null)) {
            if ((this.mListenerHints & 2) != 0 && record.getAudioAttributes().getUsage() != 2) {
                return "listenerNoti";
            }
            if ((this.mListenerHints & 4) != 0 && record.getAudioAttributes().getUsage() == 2) {
                return "listenerCall";
            }
        }
        if (this.mCallState == 0 || this.mZenModeHelper.isCall(record)) {
            return null;
        }
        return "callState";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpJson(PrintWriter pw, DumpFilter filter) {
        JSONObject dump = new JSONObject();
        try {
            dump.put("service", "Notification Manager");
            dump.put("bans", this.mPreferencesHelper.dumpBansJson(filter));
            dump.put("ranking", this.mPreferencesHelper.dumpJson(filter));
            dump.put("stats", this.mUsageStats.dumpJson(filter));
            dump.put("channels", this.mPreferencesHelper.dumpChannelsJson(filter));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pw.println(dump);
    }

    private boolean allowNotificationsInCall(String disableEffects, NotificationRecord record) {
        if (record == null || record.sbn == null) {
            return false;
        }
        boolean isMmsEnabled = isMmsNotificationEnable(record.sbn.getPackageName());
        boolean isCallEnabled = "callState".equals(disableEffects) && "com.android.systemui".equals(record.sbn.getPackageName()) && "low_battery".equals(record.sbn.getTag());
        if (isMmsEnabled || isCallEnabled) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpProto(FileDescriptor fd, DumpFilter filter) {
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        synchronized (this.mNotificationLock) {
            int N = this.mNotificationList.size();
            for (int i = 0; i < N; i++) {
                NotificationRecord nr = this.mNotificationList.get(i);
                if (!filter.filtered || filter.matches(nr.sbn)) {
                    nr.dump(proto, 2246267895809L, filter.redact, 1);
                }
            }
            int N2 = this.mEnqueuedNotifications.size();
            for (int i2 = 0; i2 < N2; i2++) {
                NotificationRecord nr2 = this.mEnqueuedNotifications.get(i2);
                if (!filter.filtered || filter.matches(nr2.sbn)) {
                    nr2.dump(proto, 2246267895809L, filter.redact, 0);
                }
            }
            List<NotificationRecord> snoozed = this.mSnoozeHelper.getSnoozed();
            int N3 = snoozed.size();
            for (int i3 = 0; i3 < N3; i3++) {
                NotificationRecord nr3 = snoozed.get(i3);
                if (!filter.filtered || filter.matches(nr3.sbn)) {
                    nr3.dump(proto, 2246267895809L, filter.redact, 2);
                }
            }
            long zenLog = proto.start(1146756268034L);
            this.mZenModeHelper.dump(proto);
            for (ComponentName suppressor : this.mEffectsSuppressors) {
                suppressor.writeToProto(proto, 2246267895812L);
            }
            proto.end(zenLog);
            long listenersToken = proto.start(1146756268035L);
            this.mListeners.dump(proto, filter);
            proto.end(listenersToken);
            proto.write(1120986464260L, this.mListenerHints);
            int i4 = 0;
            while (i4 < this.mListenersDisablingEffects.size()) {
                long effectsToken = proto.start(2246267895813L);
                proto.write(1120986464257L, this.mListenersDisablingEffects.keyAt(i4));
                ArraySet<ComponentName> listeners = this.mListenersDisablingEffects.valueAt(i4);
                for (int j = 0; j < listeners.size(); j++) {
                    listeners.valueAt(j).writeToProto(proto, 2246267895811L);
                }
                proto.end(effectsToken);
                i4++;
                zenLog = zenLog;
            }
            long assistantsToken = proto.start(1146756268038L);
            this.mAssistants.dump(proto, filter);
            proto.end(assistantsToken);
            long conditionsToken = proto.start(1146756268039L);
            this.mConditionProviders.dump(proto, filter);
            proto.end(conditionsToken);
            long rankingToken = proto.start(1146756268040L);
            this.mRankingHelper.dump(proto, filter);
            this.mPreferencesHelper.dump(proto, filter);
            proto.end(rankingToken);
        }
        proto.flush();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpNotificationRecords(PrintWriter pw, DumpFilter filter) {
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
        int j = 0;
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
                        pw.println("  mHasLight=" + this.mHasLight);
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
                    while (true) {
                        if (!iter.hasNext()) {
                            break;
                        }
                        StatusBarNotification sbn = iter.next();
                        if (filter.matches(sbn)) {
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
                pw.println("\n Notification Preferences:");
                this.mPreferencesHelper.dump(pw, "    ", filter);
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
                    ArraySet<ComponentName> listeners = this.mListenersDisablingEffects.valueAt(i4);
                    int listenerSize = listeners.size();
                    for (int j2 = 0; j2 < listenerSize; j2++) {
                        if (j2 > 0) {
                            pw.print(',');
                        }
                        ComponentName listener = listeners.valueAt(j2);
                        if (listener != null) {
                            pw.print(listener);
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

    /* access modifiers changed from: package-private */
    public void enqueueNotificationInternal(String pkg, String opPkg, int callingUid, int callingPid, String tag, int id, Notification notification, int incomingUserId) {
        String channelId;
        boolean z;
        boolean z2;
        NotificationChannel channel;
        IHwNotificationManagerServiceEx iHwNotificationManagerServiceEx2 = this.iHwNotificationManagerServiceEx;
        if (iHwNotificationManagerServiceEx2 != null && iHwNotificationManagerServiceEx2.isBanNotification(pkg, notification)) {
            Slog.i(TAG, "enqueueNotificationInternal, we ban the notification = " + notification + ",pkg = " + pkg);
        } else if (SystemProperties.getBoolean("sys.super_power_save", false) && !isNoitficationWhiteApp(pkg)) {
            Flog.i(400, "enqueueNotificationInternal  !isNoitficationWhiteApp package=" + pkg);
        } else if (isBlockRideModeNotification(pkg)) {
            Flog.i(400, "enqueueNotificationInternal  !isBlockModeNotification package=" + pkg);
        } else if (isNotInTvWhiteListNotification(pkg)) {
            Flog.i(400, "enqueueNotificationWithTag packageName (" + pkg + ") is not in the tv whilte list");
        } else if (isNotificationDisable()) {
            Flog.i(400, "MDM policy is on , enqueueNotificationInternal  !isNotificationDisable package=" + pkg);
        } else {
            Slog.i(TAG, "enqueueNotificationInternal: pkg=" + pkg + " id=" + id + " notification=" + notification);
            IHwNotificationManagerServiceEx iHwNotificationManagerServiceEx3 = this.iHwNotificationManagerServiceEx;
            if (iHwNotificationManagerServiceEx3 != null && iHwNotificationManagerServiceEx3.isSendNotificationDisable(callingUid, pkg, notification)) {
                Flog.i(400, "MDM policy forbid (callingUid : " + callingUid + ", pkg : " + pkg + ") send notification");
            } else if (pkg == null || notification == null) {
                throw new IllegalArgumentException("null not allowed: pkg=" + pkg + " id=" + id + " notification=" + notification);
            } else {
                int userId = ActivityManager.handleIncomingUser(callingPid, callingUid, incomingUserId, true, false, "enqueueNotification", pkg);
                UserHandle user = UserHandle.of(userId);
                int notificationUid = resolveNotificationUid(opPkg, pkg, callingUid, userId);
                checkRestrictedCategories(notification);
                try {
                    fixNotification(notification, pkg, userId);
                    this.mUsageStats.registerEnqueuedByApp(pkg);
                    String channelId2 = notification.getChannelId();
                    if (!this.mIsTelevision || new Notification.TvExtender(notification).getChannelId() == null) {
                        channelId = channelId2;
                    } else {
                        channelId = new Notification.TvExtender(notification).getChannelId();
                    }
                    HashMap<String, String> extra = new HashMap<>();
                    extra.put(ATTR_CHANNEL_NAME, channelId);
                    if (isNeedForbidAppNotification(pkg, null, extra)) {
                        Flog.i(400, "enqueueNotificationInternal  isNeedForbidAppNotification");
                        return;
                    }
                    NotificationChannel channel2 = this.mPreferencesHelper.getNotificationChannel(pkg, notificationUid, channelId, false);
                    if (channel2 == null) {
                        Slog.e(TAG, "No Channel found for pkg=" + pkg + ", channelId=" + channelId + ", id=" + id + ", tag=" + tag + ", opPkg=" + opPkg + ", callingUid=" + callingUid + ", userId=" + userId + ", incomingUserId=" + incomingUserId + ", notificationUid=" + notificationUid + ", notification=" + notification);
                        if (!(this.mPreferencesHelper.getImportance(pkg, notificationUid) == 0)) {
                            Log.e(TAG, "appNotificationsOff is false ");
                            return;
                        }
                        return;
                    }
                    Slog.i(TAG, "enqueueNotificationInternal Channel Info : pkg=" + pkg + " id=" + id + " importance =" + channel2.getImportance());
                    IHwNotificationManagerServiceEx iHwNotificationManagerServiceEx4 = this.iHwNotificationManagerServiceEx;
                    if (iHwNotificationManagerServiceEx4 != null) {
                        iHwNotificationManagerServiceEx4.adjustNotificationGroupIfNeeded(notification, id);
                    }
                    NotificationRecord r = new NotificationRecord(getContext(), new StatusBarNotification(pkg, opPkg, id, tag, notificationUid, callingPid, notification, user, (String) null, System.currentTimeMillis()), channel2);
                    r.setIsAppImportanceLocked(this.mPreferencesHelper.getIsAppImportanceLocked(pkg, callingUid));
                    if ((notification.flags & 64) != 0) {
                        boolean fgServiceShown = channel2.isFgServiceShown();
                        if (((channel2.getUserLockedFields() & 4) == 0 || !fgServiceShown) && (r.getImportance() == 1 || r.getImportance() == 0)) {
                            if (TextUtils.isEmpty(channelId)) {
                                z2 = true;
                                z = false;
                            } else if ("miscellaneous".equals(channelId)) {
                                z2 = true;
                                z = false;
                            } else {
                                channel2.setImportance(2);
                                r.setSystemImportance(2);
                                if (!fgServiceShown) {
                                    channel2.unlockFields(4);
                                    z2 = true;
                                    channel2.setFgServiceShown(true);
                                } else {
                                    z2 = true;
                                }
                                z = false;
                                this.mPreferencesHelper.updateNotificationChannel(pkg, notificationUid, channel2, false);
                                r.updateNotificationChannel(channel2);
                            }
                            r.setSystemImportance(2);
                        } else if (fgServiceShown || TextUtils.isEmpty(channelId)) {
                            z2 = true;
                            z = false;
                        } else if (!"miscellaneous".equals(channelId)) {
                            channel2.setFgServiceShown(true);
                            r.updateNotificationChannel(channel2);
                            z2 = true;
                            z = false;
                        } else {
                            z2 = true;
                            z = false;
                        }
                    } else {
                        z2 = true;
                        z = false;
                    }
                    if (r.sbn.getOverrideGroupKey() != null) {
                        z = z2;
                    }
                    NotificationChannel channel3 = channel2;
                    if (checkDisqualifyingFeatures(userId, notificationUid, id, tag, r, z)) {
                        if (notification.allPendingIntents != null) {
                            int intentCount = notification.allPendingIntents.size();
                            if (intentCount > 0) {
                                ActivityManagerInternal am = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
                                long duration = ((DeviceIdleController.LocalService) LocalServices.getService(DeviceIdleController.LocalService.class)).getNotificationWhitelistDuration();
                                int i = 0;
                                while (i < intentCount) {
                                    PendingIntent pendingIntent = (PendingIntent) notification.allPendingIntents.valueAt(i);
                                    if (pendingIntent != null) {
                                        am.setPendingIntentWhitelistDuration(pendingIntent.getTarget(), WHITELIST_TOKEN, duration);
                                        channel = channel3;
                                        am.setPendingIntentAllowBgActivityStarts(pendingIntent.getTarget(), WHITELIST_TOKEN, 7);
                                    } else {
                                        channel = channel3;
                                    }
                                    i++;
                                    channel3 = channel;
                                }
                            }
                        }
                        if (this.mNotificationResource == null) {
                            if (DBG || Log.HWINFO) {
                                Log.i(TAG, " init notification resource");
                            }
                            this.mNotificationResource = HwFrameworkFactory.getHwResource(10);
                        }
                        HwSysResource hwSysResource = this.mNotificationResource;
                        if (hwSysResource == null || 2 != hwSysResource.acquire(notificationUid, pkg, -1)) {
                            this.mHandler.post(new EnqueueNotificationRunnable(userId, r));
                        } else if (DBG || Log.HWINFO) {
                            Log.i(TAG, " enqueueNotificationInternal dont acquire resource");
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Slog.e(TAG, "Cannot create a context for sending app", e);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void fixNotification(Notification notification, String pkg, int userId) throws PackageManager.NameNotFoundException {
        ApplicationInfo ai = this.mPackageManagerClient.getApplicationInfoAsUser(pkg, 268435456, userId == -1 ? 0 : userId);
        Notification.addFieldsFromContext(ai, notification);
        if (this.mPackageManagerClient.checkPermission("android.permission.USE_COLORIZED_NOTIFICATIONS", pkg) == 0) {
            notification.flags |= 2048;
        } else {
            notification.flags &= -2049;
        }
        if (notification.fullScreenIntent != null && ai.targetSdkVersion >= 29 && this.mPackageManagerClient.checkPermission("android.permission.USE_FULL_SCREEN_INTENT", pkg) != 0) {
            notification.fullScreenIntent = null;
            Slog.w(TAG, "Package " + pkg + ": Use of fullScreenIntent requires the USE_FULL_SCREEN_INTENT permission");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void flagNotificationForBubbles(NotificationRecord r, String pkg, int userId, NotificationRecord oldRecord) {
        Notification notification = r.getNotification();
        if (isNotificationAppropriateToBubble(r, pkg, userId, oldRecord)) {
            notification.flags |= 4096;
        } else {
            notification.flags &= -4097;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0062  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0064  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0069  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0072  */
    private boolean isNotificationAppropriateToBubble(NotificationRecord r, String pkg, int userId, NotificationRecord oldRecord) {
        boolean canBubble;
        ArrayList<Person> peopleList;
        Notification notification = r.getNotification();
        Notification.BubbleMetadata metadata = notification.getBubbleMetadata();
        if (metadata != null && canLaunchInActivityView(getContext(), metadata.getIntent(), pkg)) {
            if (this.mPreferencesHelper.areBubblesAllowed(pkg, userId)) {
                if (this.mPreferencesHelper.bubblesEnabled(r.sbn.getUser()) && r.getChannel().canBubble() && !this.mActivityManager.isLowRamDevice()) {
                    canBubble = true;
                    boolean appIsForeground = this.mActivityManager.getPackageImportance(pkg) != 100;
                    if (notification.extras == null) {
                        peopleList = notification.extras.getParcelableArrayList("android.people.list");
                    } else {
                        peopleList = null;
                    }
                    boolean notificationAppropriateToBubble = (!Notification.MessagingStyle.class.equals(notification.getNotificationStyle()) && hasValidRemoteInput(notification)) || (peopleList != null && !peopleList.isEmpty() && (!"call".equals(notification.category) && (notification.flags & 64) != 0));
                    boolean bubbleUpdate = oldRecord == null && (oldRecord.getNotification().flags & 4096) != 0;
                    if (canBubble || (!notificationAppropriateToBubble && !appIsForeground && !bubbleUpdate)) {
                        return false;
                    }
                    return true;
                }
            }
        }
        canBubble = false;
        if (this.mActivityManager.getPackageImportance(pkg) != 100) {
        }
        if (notification.extras == null) {
        }
        if (!Notification.MessagingStyle.class.equals(notification.getNotificationStyle())) {
        }
        if (oldRecord == null) {
        }
        if (canBubble) {
        }
        return false;
    }

    private boolean hasValidRemoteInput(Notification n) {
        Notification.Action[] actions = n.actions;
        if (actions == null) {
            return false;
        }
        for (Notification.Action action : actions) {
            RemoteInput[] inputs = action.getRemoteInputs();
            if (inputs != null && inputs.length > 0) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean canLaunchInActivityView(Context context, PendingIntent pendingIntent, String packageName) {
        ActivityInfo info;
        if (pendingIntent == null) {
            Log.w(TAG, "Unable to create bubble -- no intent");
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            Intent intent = pendingIntent.getIntent();
            if (intent != null) {
                info = intent.resolveActivityInfo(context.getPackageManager(), 0);
            } else {
                info = null;
            }
            if (info == null) {
                StatsLog.write(173, packageName, 1);
                Log.w(TAG, "Unable to send as bubble -- couldn't find activity info for intent: " + intent);
                return false;
            } else if (!ActivityInfo.isResizeableMode(info.resizeMode)) {
                StatsLog.write(173, packageName, 2);
                Log.w(TAG, "Unable to send as bubble -- activity is not resizable for intent: " + intent);
                return false;
            } else if (info.documentLaunchMode != 2) {
                StatsLog.write(173, packageName, 3);
                Log.w(TAG, "Unable to send as bubble -- activity is not documentLaunchMode=always for intent: " + intent);
                return false;
            } else if ((info.flags & Integer.MIN_VALUE) != 0) {
                return true;
            } else {
                Log.w(TAG, "Unable to send as bubble -- activity is not embeddable for intent: " + intent);
                return false;
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void doChannelWarningToast(CharSequence toastText) {
        Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(toastText) {
            /* class com.android.server.notification.$$Lambda$NotificationManagerService$8Pjq1sh4PByau66KrVt7XTs1eXA */
            private final /* synthetic */ CharSequence f$1;

            {
                this.f$1 = r2;
            }

            public final void runOrThrow() {
                NotificationManagerService.this.lambda$doChannelWarningToast$2$NotificationManagerService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$doChannelWarningToast$2$NotificationManagerService(CharSequence toastText) throws Exception {
        if (Settings.Global.getInt(getContext().getContentResolver(), "show_notification_channel_warnings", Build.IS_DEBUGGABLE ? 1 : 0) != 0) {
            Toast.makeText(getContext(), this.mHandler.getLooper(), toastText, 0).show();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int resolveNotificationUid(String callingPkg, String targetPkg, int callingUid, int userId) {
        if (userId == -1) {
            userId = 0;
        }
        if (isCallerSameApp(targetPkg, callingUid, userId) && ((TextUtils.equals(callingPkg, targetPkg) && !TELECOM_PKG.equals(targetPkg)) || isCallerSameApp(callingPkg, callingUid, userId))) {
            return callingUid;
        }
        int targetUid = -1;
        try {
            targetUid = this.mPackageManagerClient.getPackageUidAsUser(targetPkg, userId);
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (targetUid != -1 && (isCallerAndroid(callingPkg, callingUid) || this.mPreferencesHelper.isDelegateAllowed(targetPkg, targetUid, callingPkg, callingUid) || NOTIFICATION_CENTER_PKG.equals(callingPkg))) {
            return targetUid;
        }
        throw new SecurityException("Caller " + callingPkg + ":" + callingUid + " cannot post for pkg " + targetPkg + " in user " + userId);
    }

    private boolean checkDisqualifyingFeatures(int userId, int uid, int id, String tag, NotificationRecord r, boolean isAutogroup) {
        String pkg = r.sbn.getPackageName();
        boolean isSystemNotification = isUidSystemOrPhone(uid) || PackageManagerService.PLATFORM_PACKAGE_NAME.equals(pkg);
        boolean isNotificationFromListener = this.mListeners.isListenerPackage(pkg);
        if (!isSystemNotification && !isNotificationFromListener) {
            synchronized (this.mNotificationLock) {
                try {
                    int callingUid = Binder.getCallingUid();
                    if (this.mNotificationsByKey.get(r.sbn.getKey()) == null) {
                        if (isCallerInstantApp(callingUid, userId)) {
                            throw new SecurityException("Instant app " + pkg + " cannot create notifications");
                        }
                    }
                    if (this.mNotificationsByKey.get(r.sbn.getKey()) != null && !r.getNotification().hasCompletedProgress() && !isAutogroup) {
                        float appEnqueueRate = this.mUsageStats.getAppEnqueueRate(pkg);
                        if (appEnqueueRate > this.mMaxPackageEnqueueRate) {
                            this.mUsageStats.registerOverRateQuota(pkg);
                            long now = SystemClock.elapsedRealtime();
                            if (now - this.mLastOverRateLogTime > MIN_PACKAGE_OVERRATE_LOG_INTERVAL) {
                                Slog.e(TAG, "Package enqueue rate is " + appEnqueueRate + ". Shedding " + r.sbn.getKey() + ". package=" + pkg);
                                this.mLastOverRateLogTime = now;
                            }
                            return false;
                        }
                    }
                    int count = getNotificationCountLocked(pkg, userId, id, tag);
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
        if (!this.mSnoozeHelper.isSnoozed(userId, pkg, r.getKey())) {
            return !isBlocked(r, this.mUsageStats);
        }
        MetricsLogger.action(r.getLogMaker().setType(6).setCategory(831));
        if (DBG) {
            Slog.d(TAG, "Ignored enqueue for snoozed notification " + r.getKey());
        }
        this.mSnoozeHelper.update(userId, r);
        handleSavePolicyFile();
        return false;
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mNotificationLock"})
    public int getNotificationCountLocked(String pkg, int userId, int excludedId, String excludedTag) {
        int count = 0;
        int N = this.mNotificationList.size();
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
        if (!isBlocked(r)) {
            return false;
        }
        Slog.e(TAG, "Suppressing notification from package by user request.");
        usageStats.registerBlocked(r);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isBlocked(NotificationRecord r) {
        String pkg = r.sbn.getPackageName();
        int callingUid = r.sbn.getUid();
        return !isFromPinNotification(r.getNotification(), pkg) && (this.mPreferencesHelper.isGroupBlocked(pkg, callingUid, r.getChannel().getGroup()) || this.mPreferencesHelper.getImportance(pkg, callingUid) == 0 || r.getChannel().getImportance() == 0);
    }

    /* access modifiers changed from: protected */
    public class SnoozeNotificationRunnable implements Runnable {
        private final long mDuration;
        private final String mKey;
        private final String mSnoozeCriterionId;

        SnoozeNotificationRunnable(String key, long duration, String snoozeCriterionId) {
            this.mKey = key;
            this.mDuration = duration;
            this.mSnoozeCriterionId = snoozeCriterionId;
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = NotificationManagerService.this.findNotificationByKeyLocked(this.mKey);
                if (r != null) {
                    snoozeLocked(r);
                }
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mNotificationLock"})
        public void snoozeLocked(NotificationRecord r) {
            if (r.sbn.isGroup()) {
                List<NotificationRecord> groupNotifications = NotificationManagerService.this.findGroupNotificationsLocked(r.sbn.getPackageName(), r.sbn.getGroupKey(), r.sbn.getUserId());
                if (r.getNotification().isGroupSummary()) {
                    for (int i = 0; i < groupNotifications.size(); i++) {
                        snoozeNotificationLocked(groupNotifications.get(i));
                    }
                } else if (!NotificationManagerService.this.mSummaryByGroupKey.containsKey(r.sbn.getGroupKey())) {
                    snoozeNotificationLocked(r);
                } else if (groupNotifications.size() != 2) {
                    snoozeNotificationLocked(r);
                } else {
                    for (int i2 = 0; i2 < groupNotifications.size(); i2++) {
                        snoozeNotificationLocked(groupNotifications.get(i2));
                    }
                }
            } else {
                snoozeNotificationLocked(r);
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mNotificationLock"})
        public void snoozeNotificationLocked(NotificationRecord r) {
            MetricsLogger.action(r.getLogMaker().setCategory(831).setType(2).addTaggedData(1139, Long.valueOf(this.mDuration)).addTaggedData(832, Integer.valueOf(this.mSnoozeCriterionId == null ? 0 : 1)));
            NotificationManagerService.this.reportUserInteraction(r);
            NotificationManagerService.this.cancelNotificationLocked(r, false, 18, NotificationManagerService.this.removeFromNotificationListsLocked(r), null);
            NotificationManagerService.this.updateLightsLocked();
            if (this.mSnoozeCriterionId != null) {
                NotificationManagerService.this.mAssistants.notifyAssistantSnoozedLocked(r.sbn, this.mSnoozeCriterionId);
                NotificationManagerService.this.mSnoozeHelper.snooze(r);
            } else {
                NotificationManagerService.this.mSnoozeHelper.snooze(r, this.mDuration);
            }
            r.recordSnoozed();
            NotificationManagerService.this.handleSavePolicyFile();
        }
    }

    /* access modifiers changed from: protected */
    public class CancelNotificationRunnable implements Runnable {
        private final int mCallingPid;
        private final int mCallingUid;
        private final int mCount;
        private final int mId;
        private final ManagedServices.ManagedServiceInfo mListener;
        private final int mMustHaveFlags;
        private final int mMustNotHaveFlags;
        private final String mPkg;
        private final int mRank;
        private final int mReason;
        private final boolean mSendDelete;
        private final String mTag;
        private final int mUserId;

        CancelNotificationRunnable(int callingUid, int callingPid, String pkg, String tag, int id, int mustHaveFlags, int mustNotHaveFlags, boolean sendDelete, int userId, int reason, int rank, int count, ManagedServices.ManagedServiceInfo listener) {
            this.mCallingUid = callingUid;
            this.mCallingPid = callingPid;
            this.mPkg = pkg;
            this.mTag = tag;
            this.mId = id;
            this.mMustHaveFlags = mustHaveFlags;
            this.mMustNotHaveFlags = mustNotHaveFlags;
            this.mSendDelete = sendDelete;
            this.mUserId = userId;
            this.mReason = reason;
            this.mRank = rank;
            this.mCount = count;
            this.mListener = listener;
        }

        @Override // java.lang.Runnable
        public void run() {
            ManagedServices.ManagedServiceInfo managedServiceInfo = this.mListener;
            String listenerName = managedServiceInfo == null ? null : managedServiceInfo.component.toShortString();
            if (NotificationManagerService.DBG) {
                EventLogTags.writeNotificationCancel(this.mCallingUid, this.mCallingPid, this.mPkg, this.mId, this.mTag, this.mUserId, this.mMustHaveFlags, this.mMustNotHaveFlags, this.mReason, listenerName);
            }
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = NotificationManagerService.this.findNotificationLocked(this.mPkg, this.mTag, this.mId, this.mUserId);
                if (r != null) {
                    if (this.mReason == 1) {
                        NotificationManagerService.this.mUsageStats.registerClickedByUser(r);
                    }
                    if ((r.getNotification().flags & this.mMustHaveFlags) == this.mMustHaveFlags) {
                        if ((r.getNotification().flags & this.mMustNotHaveFlags) == 0) {
                            NotificationManagerService.this.cancelNotificationLocked(r, this.mSendDelete, this.mReason, this.mRank, this.mCount, NotificationManagerService.this.removeFromNotificationListsLocked(r), listenerName);
                            NotificationManagerService.this.cancelGroupChildrenLocked(r, this.mCallingUid, this.mCallingPid, listenerName, this.mSendDelete, null, this.mReason == 101);
                            NotificationManagerService.this.updateLightsLocked();
                        }
                    }
                } else if (this.mReason != 18 && NotificationManagerService.this.mSnoozeHelper.cancel(this.mUserId, this.mPkg, this.mTag, this.mId)) {
                    NotificationManagerService.this.handleSavePolicyFile();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public class EnqueueNotificationRunnable implements Runnable {
        private final NotificationRecord r;
        private final int userId;

        EnqueueNotificationRunnable(int userId2, NotificationRecord r2) {
            this.userId = userId2;
            this.r = r2;
        }

        /* JADX INFO: Multiple debug info for r5v24 android.widget.RemoteViews: [D('enqueueStatus' int), D('contentView' android.widget.RemoteViews)] */
        @Override // java.lang.Runnable
        public void run() {
            int enqueueStatus;
            int contentViewSize;
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
                NotificationManagerService.this.flagNotificationForBubbles(this.r, pkg, callingUid, old);
                NotificationManagerService.this.handleGroupedNotificationLocked(this.r, old, callingUid, callingPid);
                if (n.isGroup() && notification.isGroupChild()) {
                    NotificationManagerService.this.mSnoozeHelper.repostGroupSummary(pkg, this.r.getUserId(), n.getGroupKey());
                }
                if (!pkg.equals("com.android.providers.downloads") || Log.isLoggable("DownloadManager", 2)) {
                    if (old != null) {
                        enqueueStatus = 1;
                    } else {
                        enqueueStatus = 0;
                    }
                    RemoteViews contentView = notification.contentView;
                    RemoteViews bigContentView = notification.bigContentView;
                    if (contentView != null) {
                        contentViewSize = contentView.getCacheSize();
                    } else {
                        contentViewSize = 0;
                    }
                    if (bigContentView != null) {
                        bigContentViewSize = bigContentView.getCacheSize();
                    } else {
                        bigContentViewSize = 0;
                    }
                    EventLogTags.writeNotificationEnqueue(callingUid, callingPid, pkg, id, tag, this.userId, notification.toString() + " contentViewSize = " + contentViewSize + " bigContentViewSize = " + bigContentViewSize + " N = " + NotificationManagerService.this.mNotificationList.size(), enqueueStatus);
                }
                if (NotificationManagerService.this.mNotificationResource != null) {
                    NotificationManagerService.this.mNotificationResource.release(callingUid, pkg, -1);
                }
                if (NotificationManagerService.this.mAssistants.isEnabled()) {
                    NotificationManagerService.this.mAssistants.onNotificationEnqueuedLocked(this.r);
                    NotificationManagerService.this.mHandler.postDelayed(new PostNotificationRunnable(this.r.getKey()), NotificationManagerService.DELAY_FOR_ASSISTANT_TIME);
                } else if (!NotificationManagerService.this.doForUpdateNotification(this.r.getKey(), NotificationManagerService.this.mHandler)) {
                    NotificationManagerService.this.mHandler.post(new PostNotificationRunnable(this.r.getKey()));
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mNotificationLock"})
    public boolean isPackagePausedOrSuspended(String pkg, int uid) {
        return ((((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getDistractingPackageRestrictions(pkg, Binder.getCallingUserHandle().getIdentifier()) & 2) != 0) | isPackageSuspendedForUser(pkg, uid);
    }

    protected class PostNotificationRunnable implements Runnable {
        private final String key;

        PostNotificationRunnable(String key2) {
            this.key = key2;
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = null;
                try {
                    int N = NotificationManagerService.this.mEnqueuedNotifications.size();
                    int i = 0;
                    while (true) {
                        if (i >= N) {
                            break;
                        }
                        NotificationRecord enqueued = NotificationManagerService.this.mEnqueuedNotifications.get(i);
                        if (Objects.equals(this.key, enqueued.getKey())) {
                            r = enqueued;
                            break;
                        }
                        i++;
                    }
                    if (r == null) {
                        Slog.i(NotificationManagerService.TAG, "Cannot find enqueued record for key: " + this.key);
                        int N2 = NotificationManagerService.this.mEnqueuedNotifications.size();
                        int i2 = 0;
                        while (true) {
                            if (i2 >= N2) {
                                break;
                            } else if (Objects.equals(this.key, NotificationManagerService.this.mEnqueuedNotifications.get(i2).getKey())) {
                                NotificationManagerService.this.mEnqueuedNotifications.remove(i2);
                                break;
                            } else {
                                i2++;
                            }
                        }
                        NotificationManagerService.this.removeNotificationInUpdateQueue(this.key);
                    } else if (NotificationManagerService.this.isBlocked(r)) {
                        Slog.i(NotificationManagerService.TAG, "notification blocked by assistant request");
                        int N3 = NotificationManagerService.this.mEnqueuedNotifications.size();
                        int i3 = 0;
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
                    } else {
                        boolean isPackageSuspended = NotificationManagerService.this.isPackagePausedOrSuspended(r.sbn.getPackageName(), r.getUid());
                        r.setHidden(isPackageSuspended);
                        if (isPackageSuspended) {
                            NotificationManagerService.this.mUsageStats.registerSuspendedByAdmin(r);
                        }
                        NotificationRecord old = NotificationManagerService.this.mNotificationsByKey.get(this.key);
                        final StatusBarNotification n = r.sbn;
                        Notification notification = n.getNotification();
                        int index = NotificationManagerService.this.indexOfNotificationLocked(n.getKey());
                        StatusBarNotification oldSbn = null;
                        if (index < 0) {
                            NotificationManagerService.this.mNotificationList.add(r);
                            NotificationManagerService.this.mUsageStats.registerPostedByApp(r);
                            r.setInterruptive(NotificationManagerService.this.isVisuallyInterruptive(null, r));
                        } else {
                            old = NotificationManagerService.this.mNotificationList.get(index);
                            NotificationManagerService.this.mNotificationList.set(index, r);
                            NotificationManagerService.this.mUsageStats.registerUpdatedByApp(r, old);
                            notification.flags |= old.getNotification().flags & 64;
                            r.isUpdate = true;
                            if (!NotificationManagerService.DEBUG_USAGESTATS) {
                                r.setTextChanged(NotificationManagerService.this.isVisuallyInterruptive(old, r));
                            }
                        }
                        Flog.i(400, "enqueueNotificationInternal: n.getKey = " + n.getKey());
                        NotificationManagerService.this.mNotificationsByKey.put(n.getKey(), r);
                        if ((notification.flags & 64) != 0) {
                            notification.flags |= 34;
                        }
                        NotificationManagerService.this.mRankingHelper.extractSignals(r);
                        NotificationManagerService.this.mRankingHelper.sort(NotificationManagerService.this.mNotificationList);
                        if (!r.isHidden()) {
                            NotificationManagerService.this.buzzBeepBlinkLocked(r);
                        }
                        if (notification.getSmallIcon() != null) {
                            if (old != null) {
                                oldSbn = old.sbn;
                            }
                            NotificationManagerService.this.addNotificationFlag(n);
                            NotificationManagerService.this.mListeners.notifyPostedLocked(r, old);
                            if ((oldSbn == null || !Objects.equals(oldSbn.getGroup(), n.getGroup())) && !NotificationManagerService.this.isCritical(r)) {
                                NotificationManagerService.this.mHandler.post(new Runnable() {
                                    /* class com.android.server.notification.NotificationManagerService.PostNotificationRunnable.AnonymousClass1 */

                                    @Override // java.lang.Runnable
                                    public void run() {
                                        NotificationManagerService.this.mGroupHelper.onNotificationPosted(n, NotificationManagerService.this.hasAutoGroupSummaryLocked(n));
                                    }
                                });
                            }
                            if (oldSbn == null) {
                                LogPower.push((int) FoldPolicy.NAV_BAR_HEIGHT, n.getPackageName(), Integer.toString(n.getId()), n.getOpPkg(), new String[]{Integer.toString(r.getFlags())});
                                NotificationManagerService.this.reportToIAware(n.getPackageName(), n.getUid(), n.getId(), true);
                                r.setPushLogPowerTimeMs(System.currentTimeMillis());
                            } else {
                                long now = System.currentTimeMillis();
                                if (old.getPushLogPowerTimeMs(now) > NotificationManagerService.NOTIFICATION_UPDATE_REPORT_INTERVAL || !(oldSbn.getNotification() == null || oldSbn.getNotification().flags == r.getFlags())) {
                                    LogPower.push((int) HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_MINUS, n.getPackageName(), Integer.toString(n.getId()), n.getOpPkg(), new String[]{Integer.toString(r.getFlags())});
                                    r.setPushLogPowerTimeMs(now);
                                }
                            }
                        } else {
                            Slog.e(NotificationManagerService.TAG, "Not posting notification without small icon: " + notification);
                            if (old != null && !old.isCanceled) {
                                NotificationManagerService.this.mListeners.notifyRemovedLocked(r, 4, r.getStats());
                                NotificationManagerService.this.mHandler.post(new Runnable() {
                                    /* class com.android.server.notification.NotificationManagerService.PostNotificationRunnable.AnonymousClass2 */

                                    @Override // java.lang.Runnable
                                    public void run() {
                                        NotificationManagerService.this.mGroupHelper.onNotificationRemoved(n);
                                    }
                                });
                            }
                            Slog.e(NotificationManagerService.TAG, "WARNING: In a future release this will crash the app: " + n.getPackageName());
                        }
                        NotificationManagerService.this.maybeRecordInterruptionLocked(r);
                        int N4 = NotificationManagerService.this.mEnqueuedNotifications.size();
                        int i4 = 0;
                        while (true) {
                            if (i4 >= N4) {
                                break;
                            } else if (Objects.equals(this.key, NotificationManagerService.this.mEnqueuedNotifications.get(i4).getKey())) {
                                NotificationManagerService.this.mEnqueuedNotifications.remove(i4);
                                break;
                            } else {
                                i4++;
                            }
                        }
                        NotificationManagerService.this.removeNotificationInUpdateQueue(this.key);
                    }
                } catch (Throwable th) {
                    int N5 = NotificationManagerService.this.mEnqueuedNotifications.size();
                    int i5 = 0;
                    while (true) {
                        if (i5 < N5) {
                            if (Objects.equals(this.key, NotificationManagerService.this.mEnqueuedNotifications.get(i5).getKey())) {
                                NotificationManagerService.this.mEnqueuedNotifications.remove(i5);
                                break;
                            }
                            i5++;
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

    /* access modifiers changed from: protected */
    @GuardedBy({"mNotificationLock"})
    @VisibleForTesting
    public boolean isVisuallyInterruptive(NotificationRecord old, NotificationRecord r) {
        if (r == null) {
            if (DEBUG_INTERRUPTIVENESS) {
                Slog.v(TAG, "INTERRUPTIVENESS: " + r + " is not interruptive: null");
            }
            return false;
        } else if (r.sbn.isGroup() && r.sbn.getNotification().isGroupSummary()) {
            if (DEBUG_INTERRUPTIVENESS) {
                Slog.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is not interruptive: summary");
            }
            return false;
        } else if (old == null) {
            if (DEBUG_INTERRUPTIVENESS) {
                Slog.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is interruptive: new notification");
            }
            return true;
        } else {
            Notification oldN = old.sbn.getNotification();
            Notification newN = r.sbn.getNotification();
            if (oldN.extras == null || newN.extras == null) {
                if (DEBUG_INTERRUPTIVENESS) {
                    Slog.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is not interruptive: no extras");
                }
                return false;
            } else if ((r.sbn.getNotification().flags & 64) != 0) {
                if (DEBUG_INTERRUPTIVENESS) {
                    Slog.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is not interruptive: foreground service");
                }
                return false;
            } else {
                String oldTitle = String.valueOf(oldN.extras.get("android.title"));
                String newTitle = String.valueOf(newN.extras.get("android.title"));
                if (!Objects.equals(oldTitle, newTitle)) {
                    if (DEBUG_INTERRUPTIVENESS) {
                        Slog.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is interruptive: changed title");
                        StringBuilder sb = new StringBuilder();
                        sb.append("INTERRUPTIVENESS: ");
                        sb.append(String.format("   old title: %s (%s@0x%08x)", oldTitle, oldTitle.getClass(), Integer.valueOf(oldTitle.hashCode())));
                        Slog.v(TAG, sb.toString());
                        Slog.v(TAG, "INTERRUPTIVENESS: " + String.format("   new title: %s (%s@0x%08x)", newTitle, newTitle.getClass(), Integer.valueOf(newTitle.hashCode())));
                    }
                    return true;
                }
                String oldText = String.valueOf(oldN.extras.get("android.text"));
                String newText = String.valueOf(newN.extras.get("android.text"));
                if (!Objects.equals(oldText, newText)) {
                    if (DEBUG_INTERRUPTIVENESS) {
                        Slog.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is interruptive: changed text");
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("INTERRUPTIVENESS: ");
                        sb2.append(String.format("   old text: %s (%s@0x%08x)", oldText, oldText.getClass(), Integer.valueOf(oldText.hashCode())));
                        Slog.v(TAG, sb2.toString());
                        Slog.v(TAG, "INTERRUPTIVENESS: " + String.format("   new text: %s (%s@0x%08x)", newText, newText.getClass(), Integer.valueOf(newText.hashCode())));
                    }
                    return true;
                } else if (oldN.hasCompletedProgress() != newN.hasCompletedProgress()) {
                    if (DEBUG_INTERRUPTIVENESS) {
                        Slog.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is interruptive: completed progress");
                    }
                    return true;
                } else if (Notification.areActionsVisiblyDifferent(oldN, newN)) {
                    if (DEBUG_INTERRUPTIVENESS) {
                        Slog.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is interruptive: changed actions");
                    }
                    return true;
                } else {
                    try {
                        Notification.Builder oldB = Notification.Builder.recoverBuilder(getContext(), oldN);
                        Notification.Builder newB = Notification.Builder.recoverBuilder(getContext(), newN);
                        if (Notification.areStyledNotificationsVisiblyDifferent(oldB, newB)) {
                            if (DEBUG_INTERRUPTIVENESS) {
                                Slog.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is interruptive: styles differ");
                            }
                            return true;
                        }
                        if (Notification.areRemoteViewsChanged(oldB, newB)) {
                            if (DEBUG_INTERRUPTIVENESS) {
                                Slog.v(TAG, "INTERRUPTIVENESS: " + r.getKey() + " is interruptive: remoteviews differ");
                            }
                            return true;
                        }
                        return false;
                    } catch (Exception e) {
                        Slog.w(TAG, "error recovering builder", e);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isCritical(NotificationRecord record) {
        return record.getCriticality() < 2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNotificationLock"})
    private void handleGroupedNotificationLocked(NotificationRecord r, NotificationRecord old, int callingUid, int callingPid) {
        NotificationRecord removedSummary;
        StatusBarNotification sbn = r.sbn;
        Notification n = sbn.getNotification();
        if (n.isGroupSummary() && !sbn.isAppGroup()) {
            n.flags &= -513;
        }
        String group = sbn.getGroupKey();
        boolean isSummary = n.isGroupSummary();
        String oldGroup = null;
        Notification oldN = old != null ? old.sbn.getNotification() : null;
        if (old != null) {
            oldGroup = old.sbn.getGroupKey();
        }
        boolean oldIsSummary = old != null && oldN.isGroupSummary();
        if (oldIsSummary && (removedSummary = this.mSummaryByGroupKey.remove(oldGroup)) != old) {
            Slog.w(TAG, "Removed summary didn't match old notification: old=" + old.getKey() + ", removed=" + (removedSummary != null ? removedSummary.getKey() : "<null>"));
        }
        if (isSummary) {
            this.mSummaryByGroupKey.put(group, r);
        }
        if (!oldIsSummary) {
            return;
        }
        if (!isSummary || !oldGroup.equals(group)) {
            cancelGroupChildrenLocked(old, callingUid, callingPid, null, false, null, false);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mNotificationLock"})
    @VisibleForTesting
    public void scheduleTimeoutLocked(NotificationRecord record) {
        if (record.getNotification().getTimeoutAfter() > 0) {
            this.mAlarmManager.setExactAndAllowWhileIdle(2, SystemClock.elapsedRealtime() + record.getNotification().getTimeoutAfter(), PendingIntent.getBroadcast(getContext(), 1, new Intent(ACTION_NOTIFICATION_TIMEOUT).setData(new Uri.Builder().scheme(SCHEME_TIMEOUT).appendPath(record.getKey()).build()).addFlags(268435456).putExtra(EXTRA_KEY, record.getKey()), DumpState.DUMP_HWFEATURES));
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:189:0x0377 */
    /* JADX DEBUG: Multi-variable search result rejected for r17v2, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x0299  */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x029e  */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x02dc A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:164:0x02f3  */
    /* JADX WARNING: Removed duplicated region for block: B:167:0x02fa A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:172:0x030c  */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x033f  */
    /* JADX WARNING: Removed duplicated region for block: B:184:0x0363  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0374  */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x0376  */
    /* JADX WARNING: Removed duplicated region for block: B:190:0x037a  */
    /* JADX WARNING: Removed duplicated region for block: B:193:0x037f  */
    /* JADX WARNING: Removed duplicated region for block: B:196:0x038c  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x038e  */
    /* JADX WARNING: Removed duplicated region for block: B:199:0x0391  */
    /* JADX WARNING: Removed duplicated region for block: B:200:0x0393  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x0396  */
    /* JADX WARNING: Removed duplicated region for block: B:203:0x0398  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00df  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x00ea  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x00f2 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x011a  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x011c  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x011f A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x012a  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0148  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0152 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x0191  */
    @GuardedBy({"mNotificationLock"})
    @VisibleForTesting
    public void buzzBeepBlinkLocked(NotificationRecord record) {
        boolean aboveThreshold;
        boolean canInterrupt;
        boolean blink;
        boolean buzz;
        boolean beep;
        boolean beep2;
        int i;
        boolean hasValidVibrate;
        boolean wasShowLights;
        boolean blink2;
        boolean z;
        boolean z2;
        boolean buzz2;
        boolean hasValidVibrate2;
        boolean hasValidSound;
        boolean isHwSoundAllow;
        boolean isHwFallBackToVibration;
        long[] vibration;
        long[] vibration2;
        boolean hasValidVibrate3;
        boolean hasValidSound2;
        boolean hasValidVibrate4;
        boolean hasAudibleAlert;
        boolean hwOpPkg;
        boolean beep3;
        if (!this.mIsAutomotive || this.mNotificationEffectsEnabledForAutomotive) {
            Notification notification = record.sbn.getNotification();
            String key = record.getKey();
            if (this.mIsAutomotive) {
                aboveThreshold = record.getImportance() > 3;
            } else {
                aboveThreshold = record.getImportance() >= 3;
            }
            boolean canInterrupt2 = !isFromPinNotification(notification, record.sbn.getPackageName()) && !disallowInterrupt(notification);
            boolean wasBeep = key != null && key.equals(this.mSoundNotificationKey);
            boolean wasBuzz = key != null && key.equals(this.mVibrateNotificationKey);
            boolean hasValidSound3 = false;
            boolean sentAccessibilityEvent = false;
            if (!record.isUpdate && record.getImportance() > 1) {
                sendAccessibilityEvent(notification, record.sbn.getPackageName());
                sentAccessibilityEvent = true;
            }
            int i2 = 4;
            if (!aboveThreshold || !isNotificationForCurrentUser(record)) {
                hasValidVibrate2 = false;
                buzz2 = false;
                beep = false;
                blink = false;
                canInterrupt = canInterrupt2;
                i = 2;
                beep2 = false;
            } else if (!canInterrupt2 || !this.mSystemReady || this.mAudioManager == null) {
                hasValidVibrate2 = false;
                buzz2 = false;
                beep = false;
                blink = false;
                canInterrupt = canInterrupt2;
                i = 2;
                beep2 = false;
            } else if (!this.mGameDndStatus || !isGameDndSwitchOn()) {
                Uri soundUri = record.getSound();
                if (soundUri != null) {
                    if (!Uri.EMPTY.equals(soundUri)) {
                        hasValidSound = true;
                        boolean z3 = false;
                        beep = false;
                        isHwSoundAllow = isHwSoundAllow(record.sbn.getPackageName(), record.getChannel().getId(), record.getUserId());
                        if (isHwSoundAllow) {
                            if (this.mAudioManager.getRingerModeInternal() == 2) {
                                isHwFallBackToVibration = true;
                                vibration = record.getVibration();
                                if (vibration == null || !hasValidSound) {
                                    blink = false;
                                } else {
                                    blink = false;
                                    if ((this.mAudioManager.getRingerModeInternal() == 1 || isHwFallBackToVibration) && this.mAudioManager.getStreamVolume(AudioAttributes.toLegacyStreamType(record.getAudioAttributes())) == 0) {
                                        vibration2 = this.mFallbackVibrationPattern;
                                        hasValidVibrate3 = vibration2 == null;
                                        hasValidSound2 = !hasValidSound && isHwSoundAllow;
                                        if (!hasValidVibrate3) {
                                            if (isHwVibrateAllow(record.sbn.getPackageName(), record.getChannel().getId(), record.getUserId())) {
                                                hasValidVibrate4 = true;
                                                hasAudibleAlert = !hasValidSound2 || hasValidVibrate4;
                                                StringBuilder sb = new StringBuilder();
                                                canInterrupt = canInterrupt2;
                                                sb.append("buzzBeepBlinkLocked hasValidSound=");
                                                sb.append(hasValidSound2);
                                                sb.append(",hasValidVibrate=");
                                                sb.append(hasValidVibrate4);
                                                sb.append(",vibration=");
                                                sb.append(Arrays.toString(vibration2));
                                                sb.append(",key=");
                                                sb.append(record.getKey());
                                                Slog.i(TAG, sb.toString());
                                                if (hasAudibleAlert || shouldMuteNotificationLocked(record)) {
                                                    if ((record.getFlags() & 4) != 0) {
                                                        hasValidSound3 = false;
                                                        hasValidVibrate = hasValidVibrate4;
                                                        i = 2;
                                                        beep2 = false;
                                                        buzz = z3;
                                                    } else {
                                                        hasValidVibrate = hasValidVibrate4;
                                                        hasValidSound3 = hasValidSound2;
                                                        i = 2;
                                                        beep2 = false;
                                                        buzz = z3;
                                                    }
                                                    if (wasBeep && !hasValidSound3) {
                                                        clearSoundLocked();
                                                    }
                                                    if (wasBuzz && !hasValidVibrate) {
                                                        clearVibrateLocked();
                                                    }
                                                    wasShowLights = this.mLights.remove(key);
                                                    if (canShowLightsLocked(record, aboveThreshold) || !canInterrupt) {
                                                        if (wasShowLights) {
                                                            updateLightsLocked();
                                                        }
                                                        blink2 = blink;
                                                    } else {
                                                        this.mLights.add(key);
                                                        updateLightsLocked();
                                                        if (this.mUseAttentionLight) {
                                                            this.mAttentionLight.pulse();
                                                        }
                                                        blink2 = true;
                                                    }
                                                    if (!buzz || beep || blink2) {
                                                        if (record.sbn.isGroup() || !record.sbn.getNotification().isGroupSummary()) {
                                                            if (!DEBUG_USAGESTATS) {
                                                                if (DEBUG_INTERRUPTIVENESS) {
                                                                    Slog.v(TAG, "INTERRUPTIVENESS: " + record.getKey() + " is interruptive: alerted");
                                                                }
                                                                z = true;
                                                                record.setInterruptive(true);
                                                            } else {
                                                                z = true;
                                                            }
                                                        } else if (DEBUG_INTERRUPTIVENESS) {
                                                            Slog.v(TAG, "INTERRUPTIVENESS: " + record.getKey() + " is not interruptive: summary");
                                                            z = true;
                                                        } else {
                                                            z = true;
                                                        }
                                                        LogMaker category = record.getLogMaker().setCategory(199);
                                                        int i3 = z ? 1 : 0;
                                                        int i4 = z ? 1 : 0;
                                                        int i5 = z ? 1 : 0;
                                                        int i6 = z ? 1 : 0;
                                                        int i7 = z ? 1 : 0;
                                                        int i8 = z ? 1 : 0;
                                                        LogMaker type = category.setType(i3);
                                                        boolean z4 = buzz ? z : beep2;
                                                        if (!beep) {
                                                            i = beep2;
                                                        }
                                                        int i9 = i | (z4 ? 1 : 0);
                                                        if (!blink2) {
                                                            i2 = beep2;
                                                        }
                                                        MetricsLogger.action(type.setSubtype(i9 | i2));
                                                        boolean z5 = buzz ? z : beep2;
                                                        boolean z6 = beep ? z : beep2;
                                                        if (blink2) {
                                                            boolean z7 = z ? 1 : 0;
                                                            boolean z8 = z ? 1 : 0;
                                                            boolean z9 = z ? 1 : 0;
                                                            boolean z10 = z ? 1 : 0;
                                                            boolean z11 = z ? 1 : 0;
                                                            z2 = z7;
                                                        } else {
                                                            z2 = beep2;
                                                        }
                                                        int i10 = z5 ? 1 : 0;
                                                        int i11 = z6 ? 1 : 0;
                                                        int i12 = z2 ? 1 : 0;
                                                        int i13 = z2 ? 1 : 0;
                                                        int i14 = z2 ? 1 : 0;
                                                        int i15 = z6 ? 1 : 0;
                                                        int i16 = z6 ? 1 : 0;
                                                        int i17 = z5 ? 1 : 0;
                                                        int i18 = z5 ? 1 : 0;
                                                        EventLogTags.writeNotificationAlert(key, i10, i11, i12);
                                                    } else {
                                                        z = true;
                                                    }
                                                    if (buzz || beep) {
                                                        boolean z12 = z ? 1 : 0;
                                                        boolean z13 = z ? 1 : 0;
                                                        beep2 = z12;
                                                    }
                                                    record.setAudiblyAlerted(beep2);
                                                }
                                                if (!sentAccessibilityEvent) {
                                                    sendAccessibilityEvent(notification, record.sbn.getPackageName());
                                                }
                                                Slog.v(TAG, "Interrupting!");
                                                if (this.mRingtone == null) {
                                                    this.mRingtone = RingtoneManager.getRingtone(getContext(), soundUri);
                                                }
                                                String hwVibratorType = getHwVibratorType(RingtoneManager.getRingtone(getContext(), soundUri), record.sbn.getPackageName(), record.getChannel().getId(), record.getUserId());
                                                if (hasValidVibrate4) {
                                                    try {
                                                        VibrationEffect.createWaveform(vibration2, 0);
                                                    } catch (Exception e) {
                                                        Slog.e(TAG, "Illegal vibration : " + Arrays.toString(vibration2));
                                                        hasValidVibrate = false;
                                                    }
                                                }
                                                hasValidVibrate = hasValidVibrate4;
                                                if (!hwVibratorType.contains(VIBRATOR_TYPE_HAPTIC_NOTICE) || !hasValidVibrate || !hasValidSound2) {
                                                    if (!hasValidSound2 || !this.mInCall) {
                                                        beep3 = false;
                                                    } else {
                                                        playInCallNotification();
                                                        beep3 = true;
                                                    }
                                                    if (hasValidSound2 && !beep3) {
                                                        beep3 = playSound(record, soundUri);
                                                    }
                                                    if (beep3) {
                                                        this.mSoundNotificationKey = key;
                                                    }
                                                    if (this.mInCall || !hasValidVibrate) {
                                                        beep = beep3;
                                                        hwOpPkg = false;
                                                    } else {
                                                        playHwVibrate(record, hwVibratorType, hasValidSound2);
                                                        this.mVibrateNotificationKey = key;
                                                        beep = beep3;
                                                        hwOpPkg = true;
                                                    }
                                                } else {
                                                    IHwNotificationManagerServiceEx iHwNotificationManagerServiceEx2 = this.iHwNotificationManagerServiceEx;
                                                    String hwOpPkg2 = iHwNotificationManagerServiceEx2 != null ? iHwNotificationManagerServiceEx2.getHwOpPkg(record.sbn) : record.sbn.getOpPkg();
                                                    this.mRingtone.setUri(soundUri);
                                                    this.mRingtone.setLooping((record.getNotification().flags & 4) != 0);
                                                    this.mRingtone.setAudioAttributes(record.getAudioAttributes());
                                                    this.mRingtone.playWithVibrate(hwVibratorType, hwOpPkg2, record.sbn.getUid());
                                                    beep = true;
                                                    this.mSoundNotificationKey = key;
                                                    this.mVibrateNotificationKey = key;
                                                    hwOpPkg = true;
                                                }
                                                buzz = hwOpPkg;
                                                hasValidSound3 = hasValidSound2;
                                                i = 2;
                                                beep2 = false;
                                                clearSoundLocked();
                                                clearVibrateLocked();
                                                wasShowLights = this.mLights.remove(key);
                                                if (canShowLightsLocked(record, aboveThreshold)) {
                                                }
                                                if (wasShowLights) {
                                                }
                                                blink2 = blink;
                                                if (!buzz) {
                                                }
                                                if (record.sbn.isGroup()) {
                                                }
                                                if (!DEBUG_USAGESTATS) {
                                                }
                                                LogMaker category2 = record.getLogMaker().setCategory(199);
                                                int i32 = z ? 1 : 0;
                                                int i42 = z ? 1 : 0;
                                                int i52 = z ? 1 : 0;
                                                int i62 = z ? 1 : 0;
                                                int i72 = z ? 1 : 0;
                                                int i82 = z ? 1 : 0;
                                                LogMaker type2 = category2.setType(i32);
                                                if (buzz) {
                                                }
                                                if (!beep) {
                                                }
                                                int i92 = i | (z4 ? 1 : 0);
                                                if (!blink2) {
                                                }
                                                MetricsLogger.action(type2.setSubtype(i92 | i2));
                                                if (buzz) {
                                                }
                                                if (beep) {
                                                }
                                                if (blink2) {
                                                }
                                                int i102 = z5 ? 1 : 0;
                                                int i112 = z6 ? 1 : 0;
                                                int i122 = z2 ? 1 : 0;
                                                int i132 = z2 ? 1 : 0;
                                                int i142 = z2 ? 1 : 0;
                                                int i152 = z6 ? 1 : 0;
                                                int i162 = z6 ? 1 : 0;
                                                int i172 = z5 ? 1 : 0;
                                                int i182 = z5 ? 1 : 0;
                                                EventLogTags.writeNotificationAlert(key, i102, i112, i122);
                                                boolean z122 = z ? 1 : 0;
                                                boolean z132 = z ? 1 : 0;
                                                beep2 = z122;
                                                record.setAudiblyAlerted(beep2);
                                            }
                                        }
                                        hasValidVibrate4 = false;
                                        if (!hasValidSound2) {
                                        }
                                        StringBuilder sb2 = new StringBuilder();
                                        canInterrupt = canInterrupt2;
                                        sb2.append("buzzBeepBlinkLocked hasValidSound=");
                                        sb2.append(hasValidSound2);
                                        sb2.append(",hasValidVibrate=");
                                        sb2.append(hasValidVibrate4);
                                        sb2.append(",vibration=");
                                        sb2.append(Arrays.toString(vibration2));
                                        sb2.append(",key=");
                                        sb2.append(record.getKey());
                                        Slog.i(TAG, sb2.toString());
                                        if (hasAudibleAlert) {
                                        }
                                        if ((record.getFlags() & 4) != 0) {
                                        }
                                        clearSoundLocked();
                                        clearVibrateLocked();
                                        wasShowLights = this.mLights.remove(key);
                                        if (canShowLightsLocked(record, aboveThreshold)) {
                                        }
                                        if (wasShowLights) {
                                        }
                                        blink2 = blink;
                                        if (!buzz) {
                                        }
                                        if (record.sbn.isGroup()) {
                                        }
                                        if (!DEBUG_USAGESTATS) {
                                        }
                                        LogMaker category22 = record.getLogMaker().setCategory(199);
                                        int i322 = z ? 1 : 0;
                                        int i422 = z ? 1 : 0;
                                        int i522 = z ? 1 : 0;
                                        int i622 = z ? 1 : 0;
                                        int i722 = z ? 1 : 0;
                                        int i822 = z ? 1 : 0;
                                        LogMaker type22 = category22.setType(i322);
                                        if (buzz) {
                                        }
                                        if (!beep) {
                                        }
                                        int i922 = i | (z4 ? 1 : 0);
                                        if (!blink2) {
                                        }
                                        MetricsLogger.action(type22.setSubtype(i922 | i2));
                                        if (buzz) {
                                        }
                                        if (beep) {
                                        }
                                        if (blink2) {
                                        }
                                        int i1022 = z5 ? 1 : 0;
                                        int i1122 = z6 ? 1 : 0;
                                        int i1222 = z2 ? 1 : 0;
                                        int i1322 = z2 ? 1 : 0;
                                        int i1422 = z2 ? 1 : 0;
                                        int i1522 = z6 ? 1 : 0;
                                        int i1622 = z6 ? 1 : 0;
                                        int i1722 = z5 ? 1 : 0;
                                        int i1822 = z5 ? 1 : 0;
                                        EventLogTags.writeNotificationAlert(key, i1022, i1122, i1222);
                                        boolean z1222 = z ? 1 : 0;
                                        boolean z1322 = z ? 1 : 0;
                                        beep2 = z1222;
                                        record.setAudiblyAlerted(beep2);
                                    }
                                }
                                vibration2 = vibration;
                                if (vibration2 == null) {
                                }
                                if (!hasValidSound) {
                                }
                                if (!hasValidVibrate3) {
                                }
                                hasValidVibrate4 = false;
                                if (!hasValidSound2) {
                                }
                                StringBuilder sb22 = new StringBuilder();
                                canInterrupt = canInterrupt2;
                                sb22.append("buzzBeepBlinkLocked hasValidSound=");
                                sb22.append(hasValidSound2);
                                sb22.append(",hasValidVibrate=");
                                sb22.append(hasValidVibrate4);
                                sb22.append(",vibration=");
                                sb22.append(Arrays.toString(vibration2));
                                sb22.append(",key=");
                                sb22.append(record.getKey());
                                Slog.i(TAG, sb22.toString());
                                if (hasAudibleAlert) {
                                }
                                if ((record.getFlags() & 4) != 0) {
                                }
                                clearSoundLocked();
                                clearVibrateLocked();
                                wasShowLights = this.mLights.remove(key);
                                if (canShowLightsLocked(record, aboveThreshold)) {
                                }
                                if (wasShowLights) {
                                }
                                blink2 = blink;
                                if (!buzz) {
                                }
                                if (record.sbn.isGroup()) {
                                }
                                if (!DEBUG_USAGESTATS) {
                                }
                                LogMaker category222 = record.getLogMaker().setCategory(199);
                                int i3222 = z ? 1 : 0;
                                int i4222 = z ? 1 : 0;
                                int i5222 = z ? 1 : 0;
                                int i6222 = z ? 1 : 0;
                                int i7222 = z ? 1 : 0;
                                int i8222 = z ? 1 : 0;
                                LogMaker type222 = category222.setType(i3222);
                                if (buzz) {
                                }
                                if (!beep) {
                                }
                                int i9222 = i | (z4 ? 1 : 0);
                                if (!blink2) {
                                }
                                MetricsLogger.action(type222.setSubtype(i9222 | i2));
                                if (buzz) {
                                }
                                if (beep) {
                                }
                                if (blink2) {
                                }
                                int i10222 = z5 ? 1 : 0;
                                int i11222 = z6 ? 1 : 0;
                                int i12222 = z2 ? 1 : 0;
                                int i13222 = z2 ? 1 : 0;
                                int i14222 = z2 ? 1 : 0;
                                int i15222 = z6 ? 1 : 0;
                                int i16222 = z6 ? 1 : 0;
                                int i17222 = z5 ? 1 : 0;
                                int i18222 = z5 ? 1 : 0;
                                EventLogTags.writeNotificationAlert(key, i10222, i11222, i12222);
                                boolean z12222 = z ? 1 : 0;
                                boolean z13222 = z ? 1 : 0;
                                beep2 = z12222;
                                record.setAudiblyAlerted(beep2);
                            }
                        }
                        isHwFallBackToVibration = false;
                        vibration = record.getVibration();
                        if (vibration == null) {
                        }
                        blink = false;
                        vibration2 = vibration;
                        if (vibration2 == null) {
                        }
                        if (!hasValidSound) {
                        }
                        if (!hasValidVibrate3) {
                        }
                        hasValidVibrate4 = false;
                        if (!hasValidSound2) {
                        }
                        StringBuilder sb222 = new StringBuilder();
                        canInterrupt = canInterrupt2;
                        sb222.append("buzzBeepBlinkLocked hasValidSound=");
                        sb222.append(hasValidSound2);
                        sb222.append(",hasValidVibrate=");
                        sb222.append(hasValidVibrate4);
                        sb222.append(",vibration=");
                        sb222.append(Arrays.toString(vibration2));
                        sb222.append(",key=");
                        sb222.append(record.getKey());
                        Slog.i(TAG, sb222.toString());
                        if (hasAudibleAlert) {
                        }
                        if ((record.getFlags() & 4) != 0) {
                        }
                        clearSoundLocked();
                        clearVibrateLocked();
                        wasShowLights = this.mLights.remove(key);
                        if (canShowLightsLocked(record, aboveThreshold)) {
                        }
                        if (wasShowLights) {
                        }
                        blink2 = blink;
                        if (!buzz) {
                        }
                        if (record.sbn.isGroup()) {
                        }
                        if (!DEBUG_USAGESTATS) {
                        }
                        LogMaker category2222 = record.getLogMaker().setCategory(199);
                        int i32222 = z ? 1 : 0;
                        int i42222 = z ? 1 : 0;
                        int i52222 = z ? 1 : 0;
                        int i62222 = z ? 1 : 0;
                        int i72222 = z ? 1 : 0;
                        int i82222 = z ? 1 : 0;
                        LogMaker type2222 = category2222.setType(i32222);
                        if (buzz) {
                        }
                        if (!beep) {
                        }
                        int i92222 = i | (z4 ? 1 : 0);
                        if (!blink2) {
                        }
                        MetricsLogger.action(type2222.setSubtype(i92222 | i2));
                        if (buzz) {
                        }
                        if (beep) {
                        }
                        if (blink2) {
                        }
                        int i102222 = z5 ? 1 : 0;
                        int i112222 = z6 ? 1 : 0;
                        int i122222 = z2 ? 1 : 0;
                        int i132222 = z2 ? 1 : 0;
                        int i142222 = z2 ? 1 : 0;
                        int i152222 = z6 ? 1 : 0;
                        int i162222 = z6 ? 1 : 0;
                        int i172222 = z5 ? 1 : 0;
                        int i182222 = z5 ? 1 : 0;
                        EventLogTags.writeNotificationAlert(key, i102222, i112222, i122222);
                        boolean z122222 = z ? 1 : 0;
                        boolean z132222 = z ? 1 : 0;
                        beep2 = z122222;
                        record.setAudiblyAlerted(beep2);
                    }
                }
                hasValidSound = false;
                boolean z32 = false;
                beep = false;
                isHwSoundAllow = isHwSoundAllow(record.sbn.getPackageName(), record.getChannel().getId(), record.getUserId());
                if (isHwSoundAllow) {
                }
                isHwFallBackToVibration = false;
                vibration = record.getVibration();
                if (vibration == null) {
                }
                blink = false;
                vibration2 = vibration;
                if (vibration2 == null) {
                }
                if (!hasValidSound) {
                }
                if (!hasValidVibrate3) {
                }
                hasValidVibrate4 = false;
                if (!hasValidSound2) {
                }
                StringBuilder sb2222 = new StringBuilder();
                canInterrupt = canInterrupt2;
                sb2222.append("buzzBeepBlinkLocked hasValidSound=");
                sb2222.append(hasValidSound2);
                sb2222.append(",hasValidVibrate=");
                sb2222.append(hasValidVibrate4);
                sb2222.append(",vibration=");
                sb2222.append(Arrays.toString(vibration2));
                sb2222.append(",key=");
                sb2222.append(record.getKey());
                Slog.i(TAG, sb2222.toString());
                if (hasAudibleAlert) {
                }
                if ((record.getFlags() & 4) != 0) {
                }
                clearSoundLocked();
                clearVibrateLocked();
                wasShowLights = this.mLights.remove(key);
                if (canShowLightsLocked(record, aboveThreshold)) {
                }
                if (wasShowLights) {
                }
                blink2 = blink;
                if (!buzz) {
                }
                if (record.sbn.isGroup()) {
                }
                if (!DEBUG_USAGESTATS) {
                }
                LogMaker category22222 = record.getLogMaker().setCategory(199);
                int i322222 = z ? 1 : 0;
                int i422222 = z ? 1 : 0;
                int i522222 = z ? 1 : 0;
                int i622222 = z ? 1 : 0;
                int i722222 = z ? 1 : 0;
                int i822222 = z ? 1 : 0;
                LogMaker type22222 = category22222.setType(i322222);
                if (buzz) {
                }
                if (!beep) {
                }
                int i922222 = i | (z4 ? 1 : 0);
                if (!blink2) {
                }
                MetricsLogger.action(type22222.setSubtype(i922222 | i2));
                if (buzz) {
                }
                if (beep) {
                }
                if (blink2) {
                }
                int i1022222 = z5 ? 1 : 0;
                int i1122222 = z6 ? 1 : 0;
                int i1222222 = z2 ? 1 : 0;
                int i1322222 = z2 ? 1 : 0;
                int i1422222 = z2 ? 1 : 0;
                int i1522222 = z6 ? 1 : 0;
                int i1622222 = z6 ? 1 : 0;
                int i1722222 = z5 ? 1 : 0;
                int i1822222 = z5 ? 1 : 0;
                EventLogTags.writeNotificationAlert(key, i1022222, i1122222, i1222222);
                boolean z1222222 = z ? 1 : 0;
                boolean z1322222 = z ? 1 : 0;
                beep2 = z1222222;
                record.setAudiblyAlerted(beep2);
            } else {
                hasValidVibrate2 = false;
                buzz2 = false;
                beep = false;
                blink = false;
                canInterrupt = canInterrupt2;
                i = 2;
                beep2 = false;
            }
            hasValidVibrate = hasValidVibrate2;
            buzz = buzz2;
            clearSoundLocked();
            clearVibrateLocked();
            wasShowLights = this.mLights.remove(key);
            if (canShowLightsLocked(record, aboveThreshold)) {
            }
            if (wasShowLights) {
            }
            blink2 = blink;
            if (!buzz) {
            }
            if (record.sbn.isGroup()) {
            }
            if (!DEBUG_USAGESTATS) {
            }
            LogMaker category222222 = record.getLogMaker().setCategory(199);
            int i3222222 = z ? 1 : 0;
            int i4222222 = z ? 1 : 0;
            int i5222222 = z ? 1 : 0;
            int i6222222 = z ? 1 : 0;
            int i7222222 = z ? 1 : 0;
            int i8222222 = z ? 1 : 0;
            LogMaker type222222 = category222222.setType(i3222222);
            if (buzz) {
            }
            if (!beep) {
            }
            int i9222222 = i | (z4 ? 1 : 0);
            if (!blink2) {
            }
            MetricsLogger.action(type222222.setSubtype(i9222222 | i2));
            if (buzz) {
            }
            if (beep) {
            }
            if (blink2) {
            }
            int i10222222 = z5 ? 1 : 0;
            int i11222222 = z6 ? 1 : 0;
            int i12222222 = z2 ? 1 : 0;
            int i13222222 = z2 ? 1 : 0;
            int i14222222 = z2 ? 1 : 0;
            int i15222222 = z6 ? 1 : 0;
            int i16222222 = z6 ? 1 : 0;
            int i17222222 = z5 ? 1 : 0;
            int i18222222 = z5 ? 1 : 0;
            EventLogTags.writeNotificationAlert(key, i10222222, i11222222, i12222222);
            boolean z12222222 = z ? 1 : 0;
            boolean z13222222 = z ? 1 : 0;
            beep2 = z12222222;
            record.setAudiblyAlerted(beep2);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mNotificationLock"})
    public boolean canShowLightsLocked(NotificationRecord record, boolean aboveThreshold) {
        if (!this.mHasLight || !this.mNotificationPulseEnabled || record.getLight() == null || !aboveThreshold || (record.getSuppressedVisualEffects() & 8) != 0) {
            return false;
        }
        Notification notification = record.getNotification();
        if (record.isUpdate && (notification.flags & 8) != 0) {
            return false;
        }
        if (!record.sbn.isGroup() || !record.getNotification().suppressAlertingDueToGrouping()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mNotificationLock"})
    public boolean shouldMuteNotificationLocked(NotificationRecord record) {
        Notification notification = record.getNotification();
        if (!record.isUpdate || (notification.flags & 8) == 0) {
            String disableEffects = disableNotificationEffects(record);
            if (disableEffects != null && !allowNotificationsInCall(disableEffects, record)) {
                ZenLog.traceDisableEffects(record, disableEffects);
                Slog.i(TAG, "muting noisy :" + record.getKey() + ",disableEffects=" + disableEffects);
                return true;
            } else if (record.isIntercepted() && !inNonDisturbMode(record.sbn.getPackageName())) {
                Slog.i(TAG, "muting noisy, suppressed due to DND :" + record.getKey());
                return true;
            } else if (!record.sbn.isGroup() || !notification.suppressAlertingDueToGrouping()) {
                if (!this.mUsageStats.isAlertRateLimited(record.sbn.getPackageName())) {
                    return false;
                }
                Slog.e(TAG, "Muting recently noisy " + record.getKey());
                return true;
            } else {
                Slog.i(TAG, "muting noisy, suppressed due to isGroup :" + record.getKey());
                return true;
            }
        } else {
            Slog.i(TAG, "muting noisy, suppressed because it's a silent update :" + record.getKey());
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
                    Log.i(TAG, "Playing sound " + soundUri + " with attributes " + record.getAudioAttributes());
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
        long identity = Binder.clearCallingIdentity();
        try {
            VibrationEffect effect = VibrationEffect.createWaveform(vibration, (record.getNotification().flags & 4) != 0 ? 0 : -1);
            try {
                String hwOpPkg = this.iHwNotificationManagerServiceEx != null ? this.iHwNotificationManagerServiceEx.getHwOpPkg(record.sbn) : record.sbn.getOpPkg();
                if (delayVibForSound) {
                    new Thread(new Runnable(record, hwOpPkg, effect) {
                        /* class com.android.server.notification.$$Lambda$NotificationManagerService$NIYSPZGVx25iKHqhPwPDiGh25s */
                        private final /* synthetic */ NotificationRecord f$1;
                        private final /* synthetic */ String f$2;
                        private final /* synthetic */ VibrationEffect f$3;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            NotificationManagerService.this.lambda$playVibration$3$NotificationManagerService(this.f$1, this.f$2, this.f$3);
                        }
                    }).start();
                } else {
                    this.mVibrator.vibrate(record.sbn.getUid(), record.sbn.getPackageName(), effect, "Notification", record.getAudioAttributes());
                }
                return true;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "Error creating vibration waveform with pattern: " + Arrays.toString(vibration));
            Binder.restoreCallingIdentity(identity);
            return false;
        }
    }

    public /* synthetic */ void lambda$playVibration$3$NotificationManagerService(NotificationRecord record, String hwOpPkg, VibrationEffect effect) {
        int waitMs = this.mAudioManager.getFocusRampTimeMs(3, record.getAudioAttributes());
        if (DBG) {
            Slog.v(TAG, "Delaying vibration by " + waitMs + "ms");
        }
        try {
            Thread.sleep((long) waitMs);
        } catch (InterruptedException e) {
        }
        synchronized (this.mNotificationLock) {
            if (this.mNotificationsByKey.get(record.getKey()) != null) {
                this.mVibrator.vibrate(record.sbn.getUid(), hwOpPkg, effect, "Notification (delayed)", record.getAudioAttributes());
            } else {
                Slog.e(TAG, "No vibration for canceled notification : " + record.getKey());
            }
        }
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
        if (this.mAudioManager.getRingerModeInternal() == 2 && Settings.Secure.getInt(getContext().getContentResolver(), "in_call_notification_enabled", 1) != 0) {
            new Thread() {
                /* class com.android.server.notification.NotificationManagerService.AnonymousClass12 */

                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    long identity = Binder.clearCallingIdentity();
                    try {
                        IRingtonePlayer player = NotificationManagerService.this.mAudioManager.getRingtonePlayer();
                        if (player != null) {
                            if (NotificationManagerService.this.mCallNotificationToken != null) {
                                player.stop(NotificationManagerService.this.mCallNotificationToken);
                            }
                            NotificationManagerService.this.mCallNotificationToken = new Binder();
                            player.play(NotificationManagerService.this.mCallNotificationToken, NotificationManagerService.this.mInCallNotificationUri, NotificationManagerService.this.mInCallNotificationAudioAttributes, NotificationManagerService.this.mInCallNotificationVolume, false);
                        }
                    } catch (RemoteException e) {
                    } catch (Throwable player2) {
                        Binder.restoreCallingIdentity(identity);
                        throw player2;
                    }
                    Binder.restoreCallingIdentity(identity);
                }
            }.start();
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mToastQueue"})
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
    @GuardedBy({"mToastQueue"})
    public void cancelToastLocked(int index) {
        ToastRecord record = this.mToastQueue.get(index);
        try {
            record.callback.hide();
        } catch (RemoteException e) {
            Slog.w(TAG, "Object died trying to hide notification " + record.callback + " in package " + record.pkg);
        }
        ToastRecord lastToast = this.mToastQueue.remove(index);
        this.mWindowManagerInternal.removeWindowToken(lastToast.token, false, lastToast.displayId);
        scheduleKillTokenTimeout(lastToast);
        keepProcessAliveIfNeededLocked(record.pid);
        if (this.mToastQueue.size() > 0) {
            showNextToastLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void finishTokenLocked(IBinder t, int displayId) {
        this.mHandler.removeCallbacksAndMessages(t);
        this.mWindowManagerInternal.removeWindowToken(t, true, displayId);
    }

    @GuardedBy({"mToastQueue"})
    private void scheduleDurationReachedLocked(ToastRecord r) {
        this.mHandler.removeCallbacksAndMessages(r);
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 2, r), (long) this.mAccessibilityManager.getRecommendedTimeoutMillis(r.duration == 1 ? 3500 : 2000, 2));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDurationReached(ToastRecord record) {
        Flog.i(400, "Timeout pkg=" + record.pkg + " callback=" + record.callback);
        synchronized (this.mToastQueue) {
            int index = indexOfToastLocked(record.pkg, record.callback);
            if (index >= 0) {
                cancelToastLocked(index);
            }
        }
    }

    @GuardedBy({"mToastQueue"})
    private void scheduleKillTokenTimeout(ToastRecord r) {
        this.mHandler.removeCallbacksAndMessages(r);
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 7, r), 11000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleKillTokenTimeout(ToastRecord record) {
        if (DBG) {
            Slog.d(TAG, "Kill Token Timeout token=" + record.token);
        }
        synchronized (this.mToastQueue) {
            finishTokenLocked(record.token, record.displayId);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mToastQueue"})
    public int indexOfToastLocked(String pkg, ITransientNotification callback) {
        IBinder cbak = callback.asBinder();
        ArrayList<ToastRecord> list = this.mToastQueue;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            ToastRecord r = list.get(i);
            if (r.pkg.equals(pkg) && r.callback.asBinder() == cbak) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mToastQueue"})
    public void keepProcessAliveIfNeededLocked(int pid) {
        int toastCount = 0;
        ArrayList<ToastRecord> list = this.mToastQueue;
        int N = list.size();
        for (int i = 0; i < N; i++) {
            if (list.get(i).pid == pid) {
                toastCount++;
            }
        }
        try {
            this.mAm.setProcessImportant(this.mForegroundToken, pid, toastCount > 0, "toast");
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRankingReconsideration(Message message) {
        boolean changed;
        if (message.obj instanceof RankingReconsideration) {
            RankingReconsideration recon = (RankingReconsideration) message.obj;
            recon.run();
            synchronized (this.mNotificationLock) {
                NotificationRecord record = this.mNotificationsByKey.get(recon.getKey());
                if (record != null) {
                    int indexBefore = findNotificationRecordIndexLocked(record);
                    boolean interceptBefore = record.isIntercepted();
                    int visibilityBefore = record.getPackageVisibilityOverride();
                    recon.applyChangesLocked(record);
                    applyZenModeLocked(record);
                    this.mRankingHelper.sort(this.mNotificationList);
                    int indexAfter = findNotificationRecordIndexLocked(record);
                    boolean interceptAfter = record.isIntercepted();
                    int visibilityAfter = record.getPackageVisibilityOverride();
                    if (indexBefore == indexAfter && interceptBefore == interceptAfter) {
                        if (visibilityBefore == visibilityAfter) {
                            changed = false;
                            if (interceptBefore && !interceptAfter && record.isNewEnoughForAlerting(System.currentTimeMillis())) {
                                buzzBeepBlinkLocked(record);
                            }
                        }
                    }
                    changed = true;
                    buzzBeepBlinkLocked(record);
                } else {
                    return;
                }
            }
            if (changed) {
                this.mHandler.scheduleSendRankingUpdate();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleRankingSort() {
        Throwable th;
        NotificationManagerService notificationManagerService = this;
        if (notificationManagerService.mRankingHelper != null) {
            synchronized (notificationManagerService.mNotificationLock) {
                try {
                    int N = notificationManagerService.mNotificationList.size();
                    ArrayList<String> orderBefore = new ArrayList<>(N);
                    int[] visibilities = new int[N];
                    boolean[] showBadges = new boolean[N];
                    boolean[] allowBubbles = new boolean[N];
                    ArrayList<NotificationChannel> channelBefore = new ArrayList<>(N);
                    ArrayList<String> groupKeyBefore = new ArrayList<>(N);
                    ArrayList<ArrayList<String>> overridePeopleBefore = new ArrayList<>(N);
                    ArrayList<ArrayList<SnoozeCriterion>> snoozeCriteriaBefore = new ArrayList<>(N);
                    ArrayList<Integer> userSentimentBefore = new ArrayList<>(N);
                    ArrayList<Integer> suppressVisuallyBefore = new ArrayList<>(N);
                    ArrayList<ArrayList<Notification.Action>> systemSmartActionsBefore = new ArrayList<>(N);
                    ArrayList<ArrayList<CharSequence>> smartRepliesBefore = new ArrayList<>(N);
                    int[] importancesBefore = new int[N];
                    int i = 0;
                    while (i < N) {
                        NotificationRecord r = notificationManagerService.mNotificationList.get(i);
                        try {
                            orderBefore.add(r.getKey());
                            visibilities[i] = r.getPackageVisibilityOverride();
                            showBadges[i] = r.canShowBadge();
                            allowBubbles[i] = r.canBubble();
                            channelBefore.add(r.getChannel());
                            groupKeyBefore.add(r.getGroupKey());
                            overridePeopleBefore.add(r.getPeopleOverride());
                            snoozeCriteriaBefore.add(r.getSnoozeCriteria());
                            userSentimentBefore.add(Integer.valueOf(r.getUserSentiment()));
                            suppressVisuallyBefore.add(Integer.valueOf(r.getSuppressedVisualEffects()));
                            systemSmartActionsBefore.add(r.getSystemGeneratedSmartActions());
                            smartRepliesBefore.add(r.getSmartReplies());
                            importancesBefore[i] = r.getImportance();
                            notificationManagerService = this;
                            notificationManagerService.mRankingHelper.extractSignals(r);
                            i++;
                            N = N;
                            smartRepliesBefore = smartRepliesBefore;
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                    int N2 = N;
                    ArrayList<ArrayList<CharSequence>> smartRepliesBefore2 = smartRepliesBefore;
                    notificationManagerService.mRankingHelper.sort(notificationManagerService.mNotificationList);
                    int i2 = 0;
                    while (i2 < N2) {
                        NotificationRecord r2 = notificationManagerService.mNotificationList.get(i2);
                        N2 = N2;
                        if (orderBefore.get(i2).equals(r2.getKey()) && visibilities[i2] == r2.getPackageVisibilityOverride() && showBadges[i2] == r2.canShowBadge() && allowBubbles[i2] == r2.canBubble() && Objects.equals(channelBefore.get(i2), r2.getChannel()) && Objects.equals(groupKeyBefore.get(i2), r2.getGroupKey()) && Objects.equals(overridePeopleBefore.get(i2), r2.getPeopleOverride()) && Objects.equals(snoozeCriteriaBefore.get(i2), r2.getSnoozeCriteria()) && Objects.equals(userSentimentBefore.get(i2), Integer.valueOf(r2.getUserSentiment())) && Objects.equals(suppressVisuallyBefore.get(i2), Integer.valueOf(r2.getSuppressedVisualEffects())) && Objects.equals(systemSmartActionsBefore.get(i2), r2.getSystemGeneratedSmartActions())) {
                            ArrayList<CharSequence> arrayList = smartRepliesBefore2.get(i2);
                            smartRepliesBefore2 = smartRepliesBefore2;
                            if (Objects.equals(arrayList, r2.getSmartReplies()) && importancesBefore[i2] == r2.getImportance()) {
                                i2++;
                                orderBefore = orderBefore;
                            }
                        }
                        notificationManagerService.mHandler.scheduleSendRankingUpdate();
                        return;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
    }

    @GuardedBy({"mNotificationLock"})
    private void recordCallerLocked(NotificationRecord record) {
        if (this.mZenModeHelper.isCall(record)) {
            this.mZenModeHelper.recordCaller(record);
        }
    }

    @GuardedBy({"mNotificationLock"})
    private void applyZenModeLocked(NotificationRecord record) {
        record.setIntercepted(this.mZenModeHelper.shouldIntercept(record));
        if (record.isIntercepted()) {
            record.setSuppressedVisualEffects(this.mZenModeHelper.getConsolidatedNotificationPolicy().suppressedVisualEffects);
        } else {
            record.setSuppressedVisualEffects(0);
        }
    }

    @GuardedBy({"mNotificationLock"})
    private int findNotificationRecordIndexLocked(NotificationRecord target) {
        return this.mRankingHelper.indexOf(this.mNotificationList, target);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSendRankingUpdate() {
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
    /* access modifiers changed from: public */
    private void handleListenerHintsChanged(int hints) {
        synchronized (this.mNotificationLock) {
            this.mListeners.notifyListenerHintsChangedLocked(hints);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleListenerInterruptionFilterChanged(int interruptionFilter) {
        synchronized (this.mNotificationLock) {
            this.mListeners.notifyInterruptionFilterChanged(interruptionFilter);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOnPackageChanged(boolean removingPackage, int changeUserId, String[] pkgList, int[] uidList) {
        this.mListeners.onPackagesChanged(removingPackage, pkgList, uidList);
        this.mAssistants.onPackagesChanged(removingPackage, pkgList, uidList);
        this.mConditionProviders.onPackagesChanged(removingPackage, pkgList, uidList);
        if (removingPackage || this.mPreferencesHelper.onPackagesChanged(removingPackage, changeUserId, pkgList, uidList)) {
            handleSavePolicyFile();
        }
    }

    /* access modifiers changed from: protected */
    public class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    NotificationManagerService.this.handleDurationReached((ToastRecord) msg.obj);
                    return;
                case 3:
                default:
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
                    NotificationManagerService.this.handleKillTokenTimeout((ToastRecord) msg.obj);
                    return;
                case 8:
                    SomeArgs args = (SomeArgs) msg.obj;
                    NotificationManagerService.this.handleOnPackageChanged(((Boolean) args.arg1).booleanValue(), args.argi1, (String[]) args.arg2, (int[]) args.arg3);
                    args.recycle();
                    return;
            }
        }

        /* access modifiers changed from: protected */
        public void scheduleSendRankingUpdate() {
            if (!hasMessages(4)) {
                sendMessage(Message.obtain(this, 4));
            }
        }

        /* access modifiers changed from: protected */
        public void scheduleCancelNotification(CancelNotificationRunnable cancelRunnable) {
            if (!hasCallbacks(cancelRunnable)) {
                sendMessage(Message.obtain(this, cancelRunnable));
            }
        }

        /* access modifiers changed from: protected */
        public void scheduleOnPackageChanged(boolean removingPackage, int changeUserId, String[] pkgList, int[] uidList) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Boolean.valueOf(removingPackage);
            args.argi1 = changeUserId;
            args.arg2 = pkgList;
            args.arg3 = uidList;
            sendMessage(Message.obtain(this, 8, args));
        }
    }

    /* access modifiers changed from: private */
    public final class RankingHandlerWorker extends Handler implements RankingHandler {
        public RankingHandlerWorker(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1000) {
                NotificationManagerService.this.handleRankingReconsideration(msg);
            } else if (i == 1001) {
                NotificationManagerService.this.handleRankingSort();
            }
        }

        @Override // com.android.server.notification.RankingHandler
        public void requestSort() {
            removeMessages(1001);
            Message msg = Message.obtain();
            msg.what = 1001;
            sendMessage(msg);
        }

        @Override // com.android.server.notification.RankingHandler
        public void requestReconsideration(RankingReconsideration recon) {
            sendMessageDelayed(Message.obtain(this, 1000, recon), recon.getDelay(TimeUnit.MILLISECONDS));
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
    /* access modifiers changed from: public */
    @GuardedBy({"mNotificationLock"})
    private boolean removeFromNotificationListsLocked(NotificationRecord r) {
        boolean wasPosted = false;
        NotificationRecord recordInList = findNotificationByListLocked(this.mNotificationList, r.getKey());
        if (recordInList != null) {
            this.mNotificationList.remove(recordInList);
            this.mNotificationsByKey.remove(recordInList.sbn.getKey());
            wasPosted = true;
        }
        while (true) {
            NotificationRecord recordInList2 = findNotificationByListLocked(this.mEnqueuedNotifications, r.getKey());
            if (recordInList2 == null) {
                return wasPosted;
            }
            this.mEnqueuedNotifications.remove(recordInList2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNotificationLock"})
    private void cancelNotificationLocked(NotificationRecord r, boolean sendDelete, int reason, boolean wasPosted, String listenerName) {
        cancelNotificationLocked(r, sendDelete, reason, -1, -1, wasPosted, listenerName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x01bc  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x01d4  */
    @GuardedBy({"mNotificationLock"})
    private void cancelNotificationLocked(final NotificationRecord r, boolean sendDelete, int reason, int rank, int count, boolean wasPosted, String listenerName) {
        ArrayMap<String, String> summaries;
        PendingIntent deleteIntent;
        String canceledKey = r.getKey();
        recordCallerLocked(r);
        if (r.getStats().getDismissalSurface() == -1) {
            r.recordDismissalSurface(0);
        }
        Slog.i(TAG, "cancelNotificationLocked called,tell the app,reason = " + reason + ",sbn key = " + r.getKey());
        if (sendDelete && (deleteIntent = r.getNotification().deleteIntent) != null) {
            try {
                ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).clearPendingIntentAllowBgActivityStarts(deleteIntent.getTarget(), WHITELIST_TOKEN);
                deleteIntent.send();
            } catch (PendingIntent.CanceledException ex) {
                Slog.w(TAG, "canceled PendingIntent for " + r.sbn.getPackageName(), ex);
            }
        }
        if (wasPosted) {
            if (r.getNotification().getSmallIcon() != null) {
                if (reason != 18) {
                    r.isCanceled = true;
                }
                Flog.i(400, "cancelNotificationLocked:" + r.sbn.getKey());
                this.mListeners.notifyRemovedLocked(r, reason, r.getStats());
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.notification.NotificationManagerService.AnonymousClass13 */

                    @Override // java.lang.Runnable
                    public void run() {
                        NotificationManagerService.this.mGroupHelper.onNotificationRemoved(r.sbn);
                    }
                });
            }
            if (canceledKey.equals(this.mSoundNotificationKey)) {
                this.mSoundNotificationKey = null;
                long identity = Binder.clearCallingIdentity();
                try {
                    if (this.mRingtone != null) {
                        this.mRingtone.stop();
                    }
                    IRingtonePlayer player = this.mAudioManager.getRingtonePlayer();
                    if (player != null) {
                        player.stopAsync();
                    }
                } catch (RemoteException e) {
                } catch (Throwable player2) {
                    Binder.restoreCallingIdentity(identity);
                    throw player2;
                }
                Binder.restoreCallingIdentity(identity);
            }
            if (canceledKey.equals(this.mVibrateNotificationKey)) {
                this.mVibrateNotificationKey = null;
                long identity2 = Binder.clearCallingIdentity();
                try {
                    HwVibrator.stopHwVibrator(Process.myUid(), ActivityThread.currentPackageName(), this.mToken, NOTIFICATION_VIBRATE);
                } finally {
                    Binder.restoreCallingIdentity(identity2);
                }
            }
            this.mLights.remove(canceledKey);
        }
        if (!(reason == 2 || reason == 3)) {
            switch (reason) {
                case 8:
                case 9:
                    this.mUsageStats.registerRemovedByApp(r);
                    break;
            }
            Flog.i(400, "cancelNotificationLocked,remove =" + r.sbn.getPackageName() + ", key:" + r.getKey());
            String groupKey = r.getGroupKey();
            NotificationRecord groupSummary = this.mSummaryByGroupKey.get(groupKey);
            if (groupSummary != null && groupSummary.getKey().equals(canceledKey)) {
                this.mSummaryByGroupKey.remove(groupKey);
            }
            summaries = this.mAutobundledSummaries.get(Integer.valueOf(r.sbn.getUserId()));
            if (summaries != null && r.sbn.getKey().equals(summaries.get(r.sbn.getPackageName()))) {
                summaries.remove(r.sbn.getPackageName());
            }
            this.mArchive.record(r.sbn);
            long now = System.currentTimeMillis();
            LogMaker logMaker = r.getItemLogMaker().setType(5).setSubtype(reason);
            if (rank == -1) {
                if (count != -1) {
                    logMaker.addTaggedData(798, Integer.valueOf(rank)).addTaggedData(1395, Integer.valueOf(count));
                }
            }
            MetricsLogger.action(logMaker);
            EventLogTags.writeNotificationCanceled(canceledKey, reason, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now), rank, count, listenerName);
        }
        this.mUsageStats.registerDismissedByUser(r);
        Flog.i(400, "cancelNotificationLocked,remove =" + r.sbn.getPackageName() + ", key:" + r.getKey());
        String groupKey2 = r.getGroupKey();
        NotificationRecord groupSummary2 = this.mSummaryByGroupKey.get(groupKey2);
        this.mSummaryByGroupKey.remove(groupKey2);
        summaries = this.mAutobundledSummaries.get(Integer.valueOf(r.sbn.getUserId()));
        summaries.remove(r.sbn.getPackageName());
        this.mArchive.record(r.sbn);
        long now2 = System.currentTimeMillis();
        LogMaker logMaker2 = r.getItemLogMaker().setType(5).setSubtype(reason);
        if (rank == -1) {
        }
        MetricsLogger.action(logMaker2);
        EventLogTags.writeNotificationCanceled(canceledKey, reason, r.getLifespanMs(now2), r.getFreshnessMs(now2), r.getExposureMs(now2), rank, count, listenerName);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updateUriPermissions(NotificationRecord newRecord, NotificationRecord oldRecord, String targetPkg, int targetUserId) {
        IBinder permissionOwner;
        String key = newRecord != null ? newRecord.getKey() : oldRecord.getKey();
        if (DBG) {
            Slog.d(TAG, key + ": updating permissions");
        }
        ArraySet<Uri> newUris = newRecord != null ? newRecord.getGrantableUris() : null;
        ArraySet<Uri> oldUris = oldRecord != null ? oldRecord.getGrantableUris() : null;
        if (!(newUris == null && oldUris == null)) {
            IBinder permissionOwner2 = null;
            if (newRecord != null && 0 == 0) {
                permissionOwner2 = newRecord.permissionOwner;
            }
            if (oldRecord != null && permissionOwner2 == null) {
                permissionOwner2 = oldRecord.permissionOwner;
            }
            if (newUris != null && permissionOwner2 == null) {
                if (DBG) {
                    Slog.d(TAG, key + ": creating owner");
                }
                permissionOwner2 = this.mUgmInternal.newUriPermissionOwner("NOTIF:" + key);
            }
            if (newUris != null || permissionOwner2 == null) {
                permissionOwner = permissionOwner2;
            } else {
                long ident = Binder.clearCallingIdentity();
                try {
                    if (DBG) {
                        Slog.d(TAG, key + ": destroying owner");
                    }
                    this.mUgmInternal.revokeUriPermissionFromOwner(permissionOwner2, null, -1, UserHandle.getUserId(oldRecord.getUid()));
                    permissionOwner = null;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
            if (!(newUris == null || permissionOwner == null)) {
                for (int i = 0; i < newUris.size(); i++) {
                    Uri uri = newUris.valueAt(i);
                    if (oldUris == null || !oldUris.contains(uri)) {
                        if (DBG) {
                            Slog.d(TAG, key + ": granting " + uri);
                        }
                        grantUriPermission(permissionOwner, uri, newRecord.getUid(), targetPkg, targetUserId);
                    }
                }
            }
            if (!(oldUris == null || permissionOwner == null)) {
                for (int i2 = 0; i2 < oldUris.size(); i2++) {
                    Uri uri2 = oldUris.valueAt(i2);
                    if (newUris == null || !newUris.contains(uri2)) {
                        if (DBG) {
                            Slog.d(TAG, key + ": revoking " + uri2);
                        }
                        revokeUriPermission(permissionOwner, uri2, oldRecord.getUid());
                    }
                }
            }
            if (newRecord != null) {
                newRecord.permissionOwner = permissionOwner;
            }
        }
    }

    private void grantUriPermission(IBinder owner, Uri uri, int sourceUid, String targetPkg, int targetUserId) {
        if (uri != null && "content".equals(uri.getScheme())) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mUgm.grantUriPermissionFromOwner(owner, sourceUid, targetPkg, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(sourceUid)), targetUserId);
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
                this.mUgmInternal.revokeUriPermissionFromOwner(owner, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(sourceUid)));
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelNotification(int callingUid, int callingPid, String pkg, String tag, int id, int mustHaveFlags, int mustNotHaveFlags, boolean sendDelete, int userId, int reason, ManagedServices.ManagedServiceInfo listener) {
        cancelNotification(callingUid, callingPid, pkg, tag, id, mustHaveFlags, mustNotHaveFlags, sendDelete, userId, reason, -1, -1, listener);
    }

    /* access modifiers changed from: package-private */
    public void cancelNotification(int callingUid, int callingPid, String pkg, String tag, int id, int mustHaveFlags, int mustNotHaveFlags, boolean sendDelete, int userId, int reason, int rank, int count, ManagedServices.ManagedServiceInfo listener) {
        this.mHandler.scheduleCancelNotification(new CancelNotificationRunnable(callingUid, callingPid, pkg, tag, id, mustHaveFlags, mustNotHaveFlags, sendDelete, userId, reason, rank, count, listener));
    }

    private boolean notificationMatchesUserId(NotificationRecord r, int userId) {
        return userId == -1 || r.getUserId() == -1 || r.getUserId() == userId;
    }

    private boolean notificationMatchesCurrentProfiles(NotificationRecord r, int userId) {
        return notificationMatchesUserId(r, userId) || this.mUserProfiles.isCurrentProfile(r.getUserId());
    }

    /* access modifiers changed from: package-private */
    public void cancelAllNotificationsInt(final int callingUid, final int callingPid, final String pkg, final String channelId, final int mustHaveFlags, final int mustNotHaveFlags, final boolean doit, final int userId, final int reason, final ManagedServices.ManagedServiceInfo listener) {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.notification.NotificationManagerService.AnonymousClass14 */

            @Override // java.lang.Runnable
            public void run() {
                ManagedServices.ManagedServiceInfo managedServiceInfo = listener;
                String listenerName = managedServiceInfo == null ? null : managedServiceInfo.component.toShortString();
                EventLogTags.writeNotificationCancelAll(callingUid, callingPid, pkg, userId, mustHaveFlags, mustNotHaveFlags, reason, listenerName);
                if (doit) {
                    synchronized (NotificationManagerService.this.mNotificationLock) {
                        Flog.i(400, "cancelAllNotificationsInt called");
                        FlagChecker flagChecker = new FlagChecker(mustHaveFlags, mustNotHaveFlags) {
                            /* class com.android.server.notification.$$Lambda$NotificationManagerService$14$hWnH6mjUAxwVmpU3QRoPHh5_FyI */
                            private final /* synthetic */ int f$0;
                            private final /* synthetic */ int f$1;

                            {
                                this.f$0 = r1;
                                this.f$1 = r2;
                            }

                            @Override // com.android.server.notification.NotificationManagerService.FlagChecker
                            public final boolean apply(int i) {
                                return NotificationManagerService.AnonymousClass14.lambda$run$0(this.f$0, this.f$1, i);
                            }
                        };
                        NotificationManagerService.this.cancelAllNotificationsByListLocked(NotificationManagerService.this.mNotificationList, callingUid, callingPid, pkg, true, channelId, flagChecker, false, userId, false, reason, listenerName, true);
                        NotificationManagerService.this.cancelAllNotificationsByListLocked(NotificationManagerService.this.mEnqueuedNotifications, callingUid, callingPid, pkg, true, channelId, flagChecker, false, userId, false, reason, listenerName, false);
                        NotificationManagerService.this.mSnoozeHelper.cancel(userId, pkg);
                    }
                }
            }

            static /* synthetic */ boolean lambda$run$0(int mustHaveFlags, int mustNotHaveFlags, int flags) {
                if ((flags & mustHaveFlags) == mustHaveFlags && (flags & mustNotHaveFlags) == 0) {
                    return true;
                }
                return false;
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNotificationLock"})
    private void cancelAllNotificationsByListLocked(ArrayList<NotificationRecord> notificationList, int callingUid, int callingPid, String pkg, boolean nullPkgIndicatesUserSwitch, String channelId, FlagChecker flagChecker, boolean includeCurrentProfiles, int userId, boolean sendDelete, int reason, String listenerName, boolean wasPosted) {
        ArrayList<NotificationRecord> canceledNotifications = null;
        for (int i = notificationList.size() - 1; i >= 0; i--) {
            NotificationRecord r = notificationList.get(i);
            if (includeCurrentProfiles) {
                if (!notificationMatchesCurrentProfiles(r, userId)) {
                }
            } else if (!notificationMatchesUserId(r, userId)) {
            }
            if (!nullPkgIndicatesUserSwitch || pkg != null || r.getUserId() != -1) {
                if (flagChecker.apply(r.getFlags()) && ((pkg == null || r.sbn.getPackageName().equals(pkg)) && (channelId == null || channelId.equals(r.getChannel().getId())))) {
                    if (canceledNotifications == null) {
                        canceledNotifications = new ArrayList<>();
                    }
                    Flog.i(400, "cancelAllNotificationsInt:" + r.sbn.getKey());
                    notificationList.remove(i);
                    this.mNotificationsByKey.remove(r.getKey());
                    r.recordDismissalSentiment(1);
                    canceledNotifications.add(r);
                    cancelNotificationLocked(r, sendDelete, reason, wasPosted, listenerName);
                }
            }
        }
        if (canceledNotifications != null) {
            int M = canceledNotifications.size();
            for (int i2 = 0; i2 < M; i2++) {
                cancelGroupChildrenLocked(canceledNotifications.get(i2), callingUid, callingPid, listenerName, false, flagChecker, false);
            }
            updateLightsLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void snoozeNotificationInt(String key, long duration, String snoozeCriterionId, ManagedServices.ManagedServiceInfo listener) {
        String listenerName = listener == null ? null : listener.component.toShortString();
        if ((duration > 0 || snoozeCriterionId != null) && key != null) {
            if (DBG) {
                Slog.d(TAG, String.format("snooze event(%s, %d, %s, %s)", key, Long.valueOf(duration), snoozeCriterionId, listenerName));
            }
            this.mHandler.post(new SnoozeNotificationRunnable(key, duration, snoozeCriterionId));
        }
    }

    /* access modifiers changed from: package-private */
    public void unsnoozeNotificationInt(String key, ManagedServices.ManagedServiceInfo listener) {
        String listenerName = listener == null ? null : listener.component.toShortString();
        if (DBG) {
            Slog.d(TAG, String.format("unsnooze event(%s, %s)", key, listenerName));
        }
        this.mSnoozeHelper.repost(key);
        handleSavePolicyFile();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mNotificationLock"})
    public void cancelAllLocked(final int callingUid, final int callingPid, final int userId, final int reason, final ManagedServices.ManagedServiceInfo listener, final boolean includeCurrentProfiles) {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.notification.NotificationManagerService.AnonymousClass15 */

            @Override // java.lang.Runnable
            public void run() {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    String listenerName = listener == null ? null : listener.component.toShortString();
                    EventLogTags.writeNotificationCancelAll(callingUid, callingPid, null, userId, 0, 0, reason, listenerName);
                    Flog.i(400, "cancelAllLocked called,callingUid = " + callingUid + ",callingPid = " + callingPid);
                    FlagChecker flagChecker = new FlagChecker(reason) {
                        /* class com.android.server.notification.$$Lambda$NotificationManagerService$15$U436K_bi4RF3tuE3ATVdL4kLpsQ */
                        private final /* synthetic */ int f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // com.android.server.notification.NotificationManagerService.FlagChecker
                        public final boolean apply(int i) {
                            return NotificationManagerService.AnonymousClass15.lambda$run$0(this.f$0, i);
                        }
                    };
                    NotificationManagerService.this.cancelAllNotificationsByListLocked(NotificationManagerService.this.mNotificationList, callingUid, callingPid, null, false, null, flagChecker, includeCurrentProfiles, userId, true, reason, listenerName, true);
                    NotificationManagerService.this.cancelAllNotificationsByListLocked(NotificationManagerService.this.mEnqueuedNotifications, callingUid, callingPid, null, false, null, flagChecker, includeCurrentProfiles, userId, true, reason, listenerName, false);
                    NotificationManagerService.this.mSnoozeHelper.cancel(userId, includeCurrentProfiles);
                }
            }

            static /* synthetic */ boolean lambda$run$0(int reason, int flags) {
                int flagsToCheck = 34;
                if (11 == reason) {
                    flagsToCheck = 34 | 4096;
                }
                if ((flags & flagsToCheck) != 0) {
                    return false;
                }
                return true;
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNotificationLock"})
    private void cancelGroupChildrenLocked(NotificationRecord r, int callingUid, int callingPid, String listenerName, boolean sendDelete, FlagChecker flagChecker, boolean isForced) {
        if (r.getNotification().isGroupSummary()) {
            if (r.sbn.getPackageName() == null) {
                Slog.e(TAG, "No package for group summary: " + r.getKey());
                return;
            }
            cancelGroupChildrenByListLocked(this.mNotificationList, r, callingUid, callingPid, listenerName, sendDelete, true, flagChecker, isForced);
            cancelGroupChildrenByListLocked(this.mEnqueuedNotifications, r, callingUid, callingPid, listenerName, sendDelete, false, flagChecker, isForced);
        }
    }

    @GuardedBy({"mNotificationLock"})
    private void cancelGroupChildrenByListLocked(ArrayList<NotificationRecord> notificationList, NotificationRecord parentNotification, int callingUid, int callingPid, String listenerName, boolean sendDelete, boolean wasPosted, FlagChecker flagChecker, boolean isForced) {
        int unDeleteFlag;
        int i;
        FlagChecker flagChecker2 = flagChecker;
        String pkg = parentNotification.sbn.getPackageName();
        int userId = parentNotification.getUserId();
        if (isForced) {
            unDeleteFlag = 0;
        } else {
            unDeleteFlag = 98;
        }
        int i2 = notificationList.size() - 1;
        while (i2 >= 0) {
            NotificationRecord childR = notificationList.get(i2);
            StatusBarNotification childSbn = childR.sbn;
            if (!childSbn.isGroup() || childSbn.getNotification().isGroupSummary()) {
                i = i2;
            } else if (!childR.getGroupKey().equals(parentNotification.getGroupKey())) {
                i = i2;
            } else if ((childR.getFlags() & unDeleteFlag) != 0) {
                i = i2;
            } else if (flagChecker2 == null || flagChecker2.apply(childR.getFlags())) {
                i = i2;
                EventLogTags.writeNotificationCancel(callingUid, callingPid, pkg, childSbn.getId(), childSbn.getTag(), userId, 0, 0, 12, listenerName);
                notificationList.remove(i);
                this.mNotificationsByKey.remove(childR.getKey());
                cancelNotificationLocked(childR, sendDelete, 12, wasPosted, listenerName);
            } else {
                i = i2;
            }
            i2 = i - 1;
            flagChecker2 = flagChecker;
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mNotificationLock"})
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
            sb.append(",mNotificationPulseEnabled = ");
            sb.append(this.mNotificationPulseEnabled);
            Flog.i(400, sb.toString());
            if (ledNotification == null || this.mInCall || ((this.mScreenOn && !this.mGameDndStatus) || !this.mNotificationPulseEnabled)) {
                Flog.i(400, "updateLightsLocked,turn off notificationLight");
                this.mNotificationLight.turnOff();
                Flog.i(1100, "turn off notificationLight due to incall or screenon or notification light disabled");
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

    /* access modifiers changed from: package-private */
    @GuardedBy({"mNotificationLock"})
    public List<NotificationRecord> findGroupNotificationsLocked(String pkg, String groupKey, int userId) {
        List<NotificationRecord> records = new ArrayList<>();
        records.addAll(findGroupNotificationByListLocked(this.mNotificationList, pkg, groupKey, userId));
        records.addAll(findGroupNotificationByListLocked(this.mEnqueuedNotifications, pkg, groupKey, userId));
        return records;
    }

    @GuardedBy({"mNotificationLock"})
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
    /* access modifiers changed from: public */
    @GuardedBy({"mNotificationLock"})
    private NotificationRecord findNotificationByKeyLocked(String key) {
        NotificationRecord r = findNotificationByListLocked(this.mNotificationList, key);
        if (r != null) {
            return r;
        }
        NotificationRecord r2 = findNotificationByListLocked(this.mEnqueuedNotifications, key);
        if (r2 != null) {
            return r2;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mNotificationLock"})
    public NotificationRecord findNotificationLocked(String pkg, String tag, int id, int userId) {
        NotificationRecord r = findNotificationByListLocked(this.mNotificationList, pkg, tag, id, userId);
        if (r != null) {
            return r;
        }
        NotificationRecord r2 = findNotificationByListLocked(this.mEnqueuedNotifications, pkg, tag, id, userId);
        if (r2 != null) {
            return r2;
        }
        return null;
    }

    @GuardedBy({"mNotificationLock"})
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNotificationLock"})
    private List<NotificationRecord> findNotificationsByListLocked(ArrayList<NotificationRecord> list, String pkg, String tag, int id, int userId) {
        List<NotificationRecord> matching = new ArrayList<>();
        int len = list.size();
        for (int i = 0; i < len; i++) {
            NotificationRecord r = list.get(i);
            if (notificationMatchesUserId(r, userId) && r.sbn.getId() == id && TextUtils.equals(r.sbn.getTag(), tag) && r.sbn.getPackageName().equals(pkg)) {
                matching.add(r);
            }
        }
        return matching;
    }

    @GuardedBy({"mNotificationLock"})
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
    @GuardedBy({"mNotificationLock"})
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
    /* access modifiers changed from: public */
    private void updateNotificationPulse() {
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
    /* access modifiers changed from: public */
    private void checkCallerIsSystemOrShell() {
        if (Binder.getCallingUid() != 2000) {
            checkCallerIsSystem();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkCallerIsSystem() {
        if (!isCallerSystemOrPhone()) {
            throw new SecurityException("Disallowed call for uid " + Binder.getCallingUid());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkCallerIsSystemOrSystemUiOrShell() {
        if (Binder.getCallingUid() != 2000 && !isCallerSystemOrPhone()) {
            getContext().enforceCallingPermission("android.permission.STATUS_BAR_SERVICE", null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkCallerIsSystemOrSameApp(String pkg) {
        if (!isCallerSystemOrPhone()) {
            checkCallerIsSameApp(pkg);
        }
    }

    private boolean isCallerAndroid(String callingPkg, int uid) {
        return isUidSystemOrPhone(uid) && callingPkg != null && PackageManagerService.PLATFORM_PACKAGE_NAME.equals(callingPkg);
    }

    private void checkRestrictedCategories(Notification notification) {
        try {
            if (!this.mPackageManager.hasSystemFeature("android.hardware.type.automotive", 0)) {
                return;
            }
        } catch (RemoteException e) {
            if (DBG) {
                Slog.e(TAG, "Unable to confirm if it's safe to skip category restrictions check thus the check will be done anyway");
            }
        }
        if ("car_emergency".equals(notification.category) || "car_warning".equals(notification.category) || "car_information".equals(notification.category)) {
            checkCallerIsSystem();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isCallerInstantApp(int callingUid, int userId) {
        if (isUidSystemOrPhone(callingUid)) {
            return false;
        }
        if (userId == -1) {
            userId = 0;
        }
        try {
            String[] pkgs = this.mPackageManager.getPackagesForUid(callingUid);
            if (pkgs != null) {
                String pkg = pkgs[0];
                this.mAppOps.checkPackage(callingUid, pkg);
                ApplicationInfo ai = this.mPackageManager.getApplicationInfo(pkg, 0, userId);
                if (ai != null) {
                    return ai.isInstantApp();
                }
                throw new SecurityException("Unknown package " + pkg);
            }
            throw new SecurityException("Unknown uid " + callingUid);
        } catch (RemoteException re) {
            throw new SecurityException("Unknown uid " + callingUid, re);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkCallerIsSameApp(String pkg) {
        checkCallerIsSameApp(pkg, Binder.getCallingUid(), UserHandle.getCallingUserId());
    }

    private void checkCallerIsSameApp(String pkg, int uid, int userId) {
        try {
            ApplicationInfo ai = this.mPackageManager.getApplicationInfo(pkg, 0, userId);
            if (ai == null) {
                throw new SecurityException("Unknown package " + pkg);
            } else if (!UserHandle.isSameApp(ai.uid, uid)) {
                throw new SecurityException("Calling uid " + uid + " gave package " + pkg + " which is owned by uid " + ai.uid);
            }
        } catch (RemoteException re) {
            throw new SecurityException("Unknown package " + pkg + "\n" + re);
        }
    }

    private boolean isCallerSameApp(String pkg) {
        try {
            checkCallerIsSameApp(pkg);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    private boolean isCallerSameApp(String pkg, int uid, int userId) {
        try {
            checkCallerIsSameApp(pkg, uid, userId);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static String callStateToString(int state) {
        if (state == 0) {
            return "CALL_STATE_IDLE";
        }
        if (state == 1) {
            return "CALL_STATE_RINGING";
        }
        if (state == 2) {
            return "CALL_STATE_OFFHOOK";
        }
        return "CALL_STATE_UNKNOWN_" + state;
    }

    private void listenForCallState() {
        TelephonyManager.from(getContext()).listen(new PhoneStateListener() {
            /* class com.android.server.notification.NotificationManagerService.AnonymousClass16 */

            @Override // android.telephony.PhoneStateListener
            public void onCallStateChanged(int state, String incomingNumber) {
                if (NotificationManagerService.this.mCallState != state) {
                    if (NotificationManagerService.DBG) {
                        Slog.d(NotificationManagerService.TAG, "Call state changed: " + NotificationManagerService.callStateToString(state));
                    }
                    NotificationManagerService.this.mCallState = state;
                }
            }
        }, 32);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNotificationLock"})
    private NotificationRankingUpdate makeRankingUpdateLocked(ManagedServices.ManagedServiceInfo info) {
        NotificationManagerService notificationManagerService = this;
        int N = notificationManagerService.mNotificationList.size();
        ArrayList<NotificationListenerService.Ranking> rankings = new ArrayList<>();
        int i = 0;
        while (true) {
            boolean z = false;
            if (i >= N) {
                return new NotificationRankingUpdate((NotificationListenerService.Ranking[]) rankings.toArray(new NotificationListenerService.Ranking[0]));
            }
            NotificationRecord record = notificationManagerService.mNotificationList.get(i);
            if (notificationManagerService.isVisibleToListener(record.sbn, info)) {
                String key = record.sbn.getKey();
                NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
                int size = rankings.size();
                boolean z2 = !record.isIntercepted();
                int packageVisibilityOverride = record.getPackageVisibilityOverride();
                int suppressedVisualEffects = record.getSuppressedVisualEffects();
                int importance = record.getImportance();
                CharSequence importanceExplanation = record.getImportanceExplanation();
                String overrideGroupKey = record.sbn.getOverrideGroupKey();
                NotificationChannel channel = record.getChannel();
                ArrayList<String> peopleOverride = record.getPeopleOverride();
                ArrayList<SnoozeCriterion> snoozeCriteria = record.getSnoozeCriteria();
                boolean canShowBadge = record.canShowBadge();
                int userSentiment = record.getUserSentiment();
                boolean isHidden = record.isHidden();
                long lastAudiblyAlertedMs = record.getLastAudiblyAlertedMs();
                if (!(record.getSound() == null && record.getVibration() == null)) {
                    z = true;
                }
                ranking.populate(key, size, z2, packageVisibilityOverride, suppressedVisualEffects, importance, importanceExplanation, overrideGroupKey, channel, peopleOverride, snoozeCriteria, canShowBadge, userSentiment, isHidden, lastAudiblyAlertedMs, z, record.getSystemGeneratedSmartActions(), record.getSmartReplies(), record.canBubble());
                rankings.add(ranking);
            }
            i++;
            notificationManagerService = this;
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
    /* access modifiers changed from: public */
    private boolean isVisibleToListener(StatusBarNotification sbn, ManagedServices.ManagedServiceInfo listener) {
        if (!listener.enabledAndUserMatches(sbn.getUserId())) {
            return false;
        }
        return true;
    }

    private boolean isPackageSuspendedForUser(String pkg, int uid) {
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
    public boolean canUseManagedServices(String pkg, Integer userId, String requiredPermission) {
        boolean canUseManagedServices = !this.mActivityManager.isLowRamDevice() || this.mPackageManagerClient.hasSystemFeature("android.hardware.type.watch");
        for (String whitelisted : getContext().getResources().getStringArray(17235977)) {
            if (whitelisted.equals(pkg)) {
                canUseManagedServices = true;
            }
        }
        if (requiredPermission == null) {
            return canUseManagedServices;
        }
        try {
            if (this.mPackageManager.checkPermission(requiredPermission, pkg, userId.intValue()) != 0) {
                return false;
            }
            return canUseManagedServices;
        } catch (RemoteException e) {
            Slog.e(TAG, "can't talk to pm", e);
            return canUseManagedServices;
        }
    }

    /* access modifiers changed from: private */
    public class TrimCache {
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

    public class NotificationAssistants extends ManagedServices {
        private static final String ATT_TYPES = "types";
        private static final String ATT_USER_SET = "user_set";
        private static final String TAG_ALLOWED_ADJUSTMENT_TYPES = "q_allowed_adjustments";
        static final String TAG_ENABLED_NOTIFICATION_ASSISTANTS = "enabled_assistants";
        private Set<String> mAllowedAdjustments = new ArraySet();
        private final Object mLock = new Object();
        @GuardedBy({"mLock"})
        private ArrayMap<Integer, Boolean> mUserSetMap = new ArrayMap<>();

        public NotificationAssistants(Context context, Object lock, ManagedServices.UserProfiles up, IPackageManager pm) {
            super(context, lock, up, pm);
            for (int i = 0; i < NotificationManagerService.DEFAULT_ALLOWED_ADJUSTMENTS.length; i++) {
                this.mAllowedAdjustments.add(NotificationManagerService.DEFAULT_ALLOWED_ADJUSTMENTS[i]);
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.notification.ManagedServices
        public ManagedServices.Config getConfig() {
            ManagedServices.Config c = new ManagedServices.Config();
            c.caption = "notification assistant";
            c.serviceInterface = "android.service.notification.NotificationAssistantService";
            c.xmlTag = TAG_ENABLED_NOTIFICATION_ASSISTANTS;
            c.secureSettingName = "enabled_notification_assistant";
            c.bindPermission = "android.permission.BIND_NOTIFICATION_ASSISTANT_SERVICE";
            c.settingsAction = "android.settings.MANAGE_DEFAULT_APPS_SETTINGS";
            c.clientLabel = 17040695;
            return c;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.notification.ManagedServices
        public IInterface asInterface(IBinder binder) {
            return INotificationListener.Stub.asInterface(binder);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.notification.ManagedServices
        public boolean checkType(IInterface service) {
            return service instanceof INotificationListener;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.notification.ManagedServices
        public void onServiceAdded(ManagedServices.ManagedServiceInfo info) {
            NotificationManagerService.this.mListeners.registerGuestService(info);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.notification.ManagedServices
        @GuardedBy({"mNotificationLock"})
        public void onServiceRemovedLocked(ManagedServices.ManagedServiceInfo removed) {
            NotificationManagerService.this.mListeners.unregisterService(removed.service, removed.userid);
        }

        @Override // com.android.server.notification.ManagedServices
        public void onUserUnlocked(int user) {
            if (this.DEBUG) {
                String str = this.TAG;
                Slog.d(str, "onUserUnlocked u=" + user);
            }
            rebindServices(true, user);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.notification.ManagedServices
        public String getRequiredPermission() {
            return "android.permission.REQUEST_NOTIFICATION_ASSISTANT_SERVICE";
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.notification.ManagedServices
        public void writeExtraXmlTags(XmlSerializer out) throws IOException {
            synchronized (this.mLock) {
                out.startTag(null, TAG_ALLOWED_ADJUSTMENT_TYPES);
                out.attribute(null, ATT_TYPES, TextUtils.join(",", this.mAllowedAdjustments));
                out.endTag(null, TAG_ALLOWED_ADJUSTMENT_TYPES);
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.notification.ManagedServices
        public void readExtraTag(String tag, XmlPullParser parser) throws IOException {
            if (TAG_ALLOWED_ADJUSTMENT_TYPES.equals(tag)) {
                String types = XmlUtils.readStringAttribute(parser, ATT_TYPES);
                synchronized (this.mLock) {
                    this.mAllowedAdjustments.clear();
                    if (!TextUtils.isEmpty(types)) {
                        this.mAllowedAdjustments.addAll(Arrays.asList(types.split(",")));
                    }
                }
            }
        }

        /* access modifiers changed from: protected */
        public void allowAdjustmentType(String type) {
            synchronized (this.mLock) {
                this.mAllowedAdjustments.add(type);
            }
            for (ManagedServices.ManagedServiceInfo info : getServices()) {
                NotificationManagerService.this.mHandler.post(new Runnable(info) {
                    /* class com.android.server.notification.$$Lambda$NotificationManagerService$NotificationAssistants$FsWpf1cmSi9GG7O4rBv1eLAEE9M */
                    private final /* synthetic */ ManagedServices.ManagedServiceInfo f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        NotificationManagerService.NotificationAssistants.this.lambda$allowAdjustmentType$0$NotificationManagerService$NotificationAssistants(this.f$1);
                    }
                });
            }
        }

        /* access modifiers changed from: protected */
        public void disallowAdjustmentType(String type) {
            synchronized (this.mLock) {
                this.mAllowedAdjustments.remove(type);
            }
            for (ManagedServices.ManagedServiceInfo info : getServices()) {
                NotificationManagerService.this.mHandler.post(new Runnable(info) {
                    /* class com.android.server.notification.$$Lambda$NotificationManagerService$NotificationAssistants$6E04T6AkRfKEIjCw7jopFAFGv30 */
                    private final /* synthetic */ ManagedServices.ManagedServiceInfo f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        NotificationManagerService.NotificationAssistants.this.lambda$disallowAdjustmentType$1$NotificationManagerService$NotificationAssistants(this.f$1);
                    }
                });
            }
        }

        /* access modifiers changed from: protected */
        public List<String> getAllowedAssistantAdjustments() {
            List<String> types;
            synchronized (this.mLock) {
                types = new ArrayList<>();
                types.addAll(this.mAllowedAdjustments);
            }
            return types;
        }

        /* access modifiers changed from: protected */
        public boolean isAdjustmentAllowed(String type) {
            boolean contains;
            synchronized (this.mLock) {
                contains = this.mAllowedAdjustments.contains(type);
            }
            return contains;
        }

        /* access modifiers changed from: protected */
        public void onNotificationsSeenLocked(ArrayList<NotificationRecord> records) {
            for (ManagedServices.ManagedServiceInfo info : getServices()) {
                ArrayList<String> keys = new ArrayList<>(records.size());
                Iterator<NotificationRecord> it = records.iterator();
                while (it.hasNext()) {
                    NotificationRecord r = it.next();
                    if (NotificationManagerService.this.isVisibleToListener(r.sbn, info) && info.isSameUser(r.getUserId())) {
                        keys.add(r.getKey());
                    }
                }
                if (!keys.isEmpty()) {
                    NotificationManagerService.this.mHandler.post(new Runnable(info, keys) {
                        /* class com.android.server.notification.$$Lambda$NotificationManagerService$NotificationAssistants$hdUZ_hmwLutGkIKdq7dHKjQLP4E */
                        private final /* synthetic */ ManagedServices.ManagedServiceInfo f$1;
                        private final /* synthetic */ ArrayList f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            NotificationManagerService.NotificationAssistants.this.lambda$onNotificationsSeenLocked$2$NotificationManagerService$NotificationAssistants(this.f$1, this.f$2);
                        }
                    });
                }
            }
        }

        /* access modifiers changed from: package-private */
        public boolean hasUserSet(int userId) {
            boolean booleanValue;
            synchronized (this.mLock) {
                booleanValue = this.mUserSetMap.getOrDefault(Integer.valueOf(userId), false).booleanValue();
            }
            return booleanValue;
        }

        /* access modifiers changed from: package-private */
        public void setUserSet(int userId, boolean set) {
            synchronized (this.mLock) {
                this.mUserSetMap.put(Integer.valueOf(userId), Boolean.valueOf(set));
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.notification.ManagedServices
        public void writeExtraAttributes(XmlSerializer out, int userId) throws IOException {
            out.attribute(null, ATT_USER_SET, Boolean.toString(hasUserSet(userId)));
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.notification.ManagedServices
        public void readExtraAttributes(String tag, XmlPullParser parser, int userId) throws IOException {
            setUserSet(userId, XmlUtils.readBooleanAttribute(parser, ATT_USER_SET, false));
        }

        /* access modifiers changed from: private */
        /* renamed from: notifyCapabilitiesChanged */
        public void lambda$disallowAdjustmentType$1$NotificationManagerService$NotificationAssistants(ManagedServices.ManagedServiceInfo info) {
            INotificationListener assistant = info.service;
            try {
                assistant.onAllowedAdjustmentsChanged();
            } catch (RemoteException ex) {
                String str = this.TAG;
                Slog.e(str, "unable to notify assistant (capabilities): " + assistant, ex);
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: notifySeen */
        public void lambda$onNotificationsSeenLocked$2$NotificationManagerService$NotificationAssistants(ManagedServices.ManagedServiceInfo info, ArrayList<String> keys) {
            INotificationListener assistant = info.service;
            try {
                assistant.onNotificationsSeen(keys);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Slog.e(str, "unable to notify assistant (seen): " + assistant, ex);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        @GuardedBy({"mNotificationLock"})
        private void onNotificationEnqueuedLocked(NotificationRecord r) {
            boolean debug = isVerboseLogEnabled();
            if (debug) {
                String str = this.TAG;
                Slog.v(str, "onNotificationEnqueuedLocked() called with: r = [" + r + "]");
            }
            notifyAssistantLocked(r.sbn, true, new BiConsumer(debug, r) {
                /* class com.android.server.notification.$$Lambda$NotificationManagerService$NotificationAssistants$xFD5w0lXKCfWgU2f03eJAOPQABs */
                private final /* synthetic */ boolean f$1;
                private final /* synthetic */ NotificationRecord f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    NotificationManagerService.NotificationAssistants.this.lambda$onNotificationEnqueuedLocked$3$NotificationManagerService$NotificationAssistants(this.f$1, this.f$2, (INotificationListener) obj, (NotificationManagerService.StatusBarNotificationHolder) obj2);
                }
            });
        }

        public /* synthetic */ void lambda$onNotificationEnqueuedLocked$3$NotificationManagerService$NotificationAssistants(boolean debug, NotificationRecord r, INotificationListener assistant, StatusBarNotificationHolder sbnHolder) {
            if (debug) {
                try {
                    String str = this.TAG;
                    Slog.v(str, "calling onNotificationEnqueuedWithChannel " + sbnHolder);
                } catch (RemoteException ex) {
                    String str2 = this.TAG;
                    Slog.e(str2, "unable to notify assistant (enqueued): " + assistant, ex);
                    return;
                }
            }
            assistant.onNotificationEnqueuedWithChannel(sbnHolder, r.getChannel());
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mNotificationLock"})
        public void notifyAssistantExpansionChangedLocked(StatusBarNotification sbn, boolean isUserAction, boolean isExpanded) {
            notifyAssistantLocked(sbn, false, new BiConsumer(sbn.getKey(), isUserAction, isExpanded) {
                /* class com.android.server.notification.$$Lambda$NotificationManagerService$NotificationAssistants$h7WPxGy6WExnaTHJZiTUqSURFAU */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ boolean f$2;
                private final /* synthetic */ boolean f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    NotificationManagerService.NotificationAssistants.this.lambda$notifyAssistantExpansionChangedLocked$4$NotificationManagerService$NotificationAssistants(this.f$1, this.f$2, this.f$3, (INotificationListener) obj, (NotificationManagerService.StatusBarNotificationHolder) obj2);
                }
            });
        }

        public /* synthetic */ void lambda$notifyAssistantExpansionChangedLocked$4$NotificationManagerService$NotificationAssistants(String key, boolean isUserAction, boolean isExpanded, INotificationListener assistant, StatusBarNotificationHolder sbnHolder) {
            try {
                assistant.onNotificationExpansionChanged(key, isUserAction, isExpanded);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Slog.e(str, "unable to notify assistant (expanded): " + assistant, ex);
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mNotificationLock"})
        public void notifyAssistantNotificationDirectReplyLocked(StatusBarNotification sbn) {
            notifyAssistantLocked(sbn, false, new BiConsumer(sbn.getKey()) {
                /* class com.android.server.notification.$$Lambda$NotificationManagerService$NotificationAssistants$JF5pLiK7GJ1M0xNPiK9WMEs3Axo */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    NotificationManagerService.NotificationAssistants.this.lambda$notifyAssistantNotificationDirectReplyLocked$5$NotificationManagerService$NotificationAssistants(this.f$1, (INotificationListener) obj, (NotificationManagerService.StatusBarNotificationHolder) obj2);
                }
            });
        }

        public /* synthetic */ void lambda$notifyAssistantNotificationDirectReplyLocked$5$NotificationManagerService$NotificationAssistants(String key, INotificationListener assistant, StatusBarNotificationHolder sbnHolder) {
            try {
                assistant.onNotificationDirectReply(key);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Slog.e(str, "unable to notify assistant (expanded): " + assistant, ex);
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mNotificationLock"})
        public void notifyAssistantSuggestedReplySent(StatusBarNotification sbn, CharSequence reply, boolean generatedByAssistant) {
            notifyAssistantLocked(sbn, false, new BiConsumer(sbn.getKey(), reply, generatedByAssistant) {
                /* class com.android.server.notification.$$Lambda$NotificationManagerService$NotificationAssistants$pTtydmbKR53sVGAi5B_cGeLDo */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ CharSequence f$2;
                private final /* synthetic */ boolean f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    NotificationManagerService.NotificationAssistants.this.lambda$notifyAssistantSuggestedReplySent$6$NotificationManagerService$NotificationAssistants(this.f$1, this.f$2, this.f$3, (INotificationListener) obj, (NotificationManagerService.StatusBarNotificationHolder) obj2);
                }
            });
        }

        public /* synthetic */ void lambda$notifyAssistantSuggestedReplySent$6$NotificationManagerService$NotificationAssistants(String key, CharSequence reply, boolean generatedByAssistant, INotificationListener assistant, StatusBarNotificationHolder sbnHolder) {
            int i;
            if (generatedByAssistant) {
                i = 1;
            } else {
                i = 0;
            }
            try {
                assistant.onSuggestedReplySent(key, reply, i);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Slog.e(str, "unable to notify assistant (snoozed): " + assistant, ex);
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mNotificationLock"})
        public void notifyAssistantActionClicked(StatusBarNotification sbn, int actionIndex, Notification.Action action, boolean generatedByAssistant) {
            notifyAssistantLocked(sbn, false, new BiConsumer(sbn.getKey(), action, generatedByAssistant) {
                /* class com.android.server.notification.$$Lambda$NotificationManagerService$NotificationAssistants$Rqv2CeOOOVMkVDRSXa6GcHvi5Vc */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ Notification.Action f$2;
                private final /* synthetic */ boolean f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    NotificationManagerService.NotificationAssistants.this.lambda$notifyAssistantActionClicked$7$NotificationManagerService$NotificationAssistants(this.f$1, this.f$2, this.f$3, (INotificationListener) obj, (NotificationManagerService.StatusBarNotificationHolder) obj2);
                }
            });
        }

        public /* synthetic */ void lambda$notifyAssistantActionClicked$7$NotificationManagerService$NotificationAssistants(String key, Notification.Action action, boolean generatedByAssistant, INotificationListener assistant, StatusBarNotificationHolder sbnHolder) {
            int i;
            if (generatedByAssistant) {
                i = 1;
            } else {
                i = 0;
            }
            try {
                assistant.onActionClicked(key, action, i);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Slog.e(str, "unable to notify assistant (snoozed): " + assistant, ex);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        @GuardedBy({"mNotificationLock"})
        private void notifyAssistantSnoozedLocked(StatusBarNotification sbn, String snoozeCriterionId) {
            notifyAssistantLocked(sbn, false, new BiConsumer(snoozeCriterionId) {
                /* class com.android.server.notification.$$Lambda$NotificationManagerService$NotificationAssistants$hZf_EguDPjMH_PBC0gm7sadRDTE */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    NotificationManagerService.NotificationAssistants.this.lambda$notifyAssistantSnoozedLocked$8$NotificationManagerService$NotificationAssistants(this.f$1, (INotificationListener) obj, (NotificationManagerService.StatusBarNotificationHolder) obj2);
                }
            });
        }

        public /* synthetic */ void lambda$notifyAssistantSnoozedLocked$8$NotificationManagerService$NotificationAssistants(String snoozeCriterionId, INotificationListener assistant, StatusBarNotificationHolder sbnHolder) {
            try {
                assistant.onNotificationSnoozedUntilContext(sbnHolder, snoozeCriterionId);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Slog.e(str, "unable to notify assistant (snoozed): " + assistant, ex);
            }
        }

        @GuardedBy({"mNotificationLock"})
        private void notifyAssistantLocked(StatusBarNotification sbn, boolean sameUserOnly, BiConsumer<INotificationListener, StatusBarNotificationHolder> callback) {
            TrimCache trimCache = new TrimCache(sbn);
            boolean debug = isVerboseLogEnabled();
            if (debug) {
                String str = this.TAG;
                Slog.v(str, "notifyAssistantLocked() called with: sbn = [" + sbn + "], sameUserOnly = [" + sameUserOnly + "], callback = [" + callback + "]");
            }
            for (ManagedServices.ManagedServiceInfo info : getServices()) {
                boolean sbnVisible = NotificationManagerService.this.isVisibleToListener(sbn, info) && (!sameUserOnly || info.isSameUser(sbn.getUserId()));
                if (debug) {
                    String str2 = this.TAG;
                    Slog.v(str2, "notifyAssistantLocked info=" + info + " snbVisible=" + sbnVisible);
                }
                if (sbnVisible) {
                    NotificationManagerService.this.mHandler.post(new Runnable(callback, info.service, new StatusBarNotificationHolder(trimCache.ForListener(info))) {
                        /* class com.android.server.notification.$$Lambda$NotificationManagerService$NotificationAssistants$FrOqX0VMAS0gs6vhrmVEabwpi2k */
                        private final /* synthetic */ BiConsumer f$0;
                        private final /* synthetic */ INotificationListener f$1;
                        private final /* synthetic */ NotificationManagerService.StatusBarNotificationHolder f$2;

                        {
                            this.f$0 = r1;
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.accept(this.f$1, this.f$2);
                        }
                    });
                }
            }
        }

        public boolean isEnabled() {
            return !getServices().isEmpty();
        }

        /* access modifiers changed from: protected */
        public void resetDefaultAssistantsIfNecessary() {
            for (UserInfo userInfo : this.mUm.getUsers(true)) {
                int userId = userInfo.getUserHandle().getIdentifier();
                if (!hasUserSet(userId)) {
                    String str = this.TAG;
                    Slog.d(str, "Approving default notification assistant for user " + userId);
                    NotificationManagerService.this.setDefaultAssistantForUser(userId);
                }
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.notification.ManagedServices
        public void setPackageOrComponentEnabled(String pkgOrComponent, int userId, boolean isPrimary, boolean enabled) {
            if (enabled) {
                List<ComponentName> allowedComponents = getAllowedComponents(userId);
                if (!allowedComponents.isEmpty()) {
                    ComponentName currentComponent = (ComponentName) CollectionUtils.firstOrNull(allowedComponents);
                    if (!currentComponent.flattenToString().equals(pkgOrComponent)) {
                        NotificationManagerService.this.setNotificationAssistantAccessGrantedForUserInternal(currentComponent, userId, false);
                    } else {
                        return;
                    }
                }
            }
            super.setPackageOrComponentEnabled(pkgOrComponent, userId, isPrimary, enabled);
        }

        @Override // com.android.server.notification.ManagedServices
        public void dump(PrintWriter pw, DumpFilter filter) {
            super.dump(pw, filter);
            pw.println("    Has user set:");
            synchronized (this.mLock) {
                for (Integer num : this.mUserSetMap.keySet()) {
                    int userId = num.intValue();
                    pw.println("      userId=" + userId + " value=" + this.mUserSetMap.get(Integer.valueOf(userId)));
                }
            }
        }

        private boolean isVerboseLogEnabled() {
            return Log.isLoggable("notification_assistant", 2);
        }
    }

    public class NotificationListeners extends ManagedServices {
        static final String TAG_ENABLED_NOTIFICATION_LISTENERS = "enabled_listeners";
        private final ArraySet<ManagedServices.ManagedServiceInfo> mLightTrimListeners = new ArraySet<>();

        public NotificationListeners(IPackageManager pm) {
            super(NotificationManagerService.this.getContext(), NotificationManagerService.this.mNotificationLock, NotificationManagerService.this.mUserProfiles, pm);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.notification.ManagedServices
        public int getBindFlags() {
            return 83886337;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.notification.ManagedServices
        public ManagedServices.Config getConfig() {
            ManagedServices.Config c = new ManagedServices.Config();
            c.caption = "notification listener";
            c.serviceInterface = "android.service.notification.NotificationListenerService";
            c.xmlTag = TAG_ENABLED_NOTIFICATION_LISTENERS;
            c.secureSettingName = "enabled_notification_listeners";
            c.bindPermission = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE";
            c.settingsAction = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
            c.clientLabel = 17040693;
            return c;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.notification.ManagedServices
        public IInterface asInterface(IBinder binder) {
            return INotificationListener.Stub.asInterface(binder);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.notification.ManagedServices
        public boolean checkType(IInterface service) {
            return service instanceof INotificationListener;
        }

        @Override // com.android.server.notification.ManagedServices
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
        @Override // com.android.server.notification.ManagedServices
        @GuardedBy({"mNotificationLock"})
        public void onServiceRemovedLocked(ManagedServices.ManagedServiceInfo removed) {
            if (NotificationManagerService.this.removeDisabledHints(removed)) {
                NotificationManagerService.this.updateListenerHintsLocked();
                NotificationManagerService.this.updateEffectsSuppressorLocked();
            }
            this.mLightTrimListeners.remove(removed);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.notification.ManagedServices
        public String getRequiredPermission() {
            return null;
        }

        @GuardedBy({"mNotificationLock"})
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

        public void onStatusBarIconsBehaviorChanged(boolean hideSilentStatusIcons) {
            for (ManagedServices.ManagedServiceInfo info : getServices()) {
                NotificationManagerService.this.mHandler.post(new Runnable(info, hideSilentStatusIcons) {
                    /* class com.android.server.notification.$$Lambda$NotificationManagerService$NotificationListeners$Uven29tL9XX5tMiwAHBwNumQKc */
                    private final /* synthetic */ ManagedServices.ManagedServiceInfo f$1;
                    private final /* synthetic */ boolean f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        NotificationManagerService.NotificationListeners.this.lambda$onStatusBarIconsBehaviorChanged$0$NotificationManagerService$NotificationListeners(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onStatusBarIconsBehaviorChanged$0$NotificationManagerService$NotificationListeners(ManagedServices.ManagedServiceInfo info, boolean hideSilentStatusIcons) {
            INotificationListener listener = info.service;
            try {
                listener.onStatusBarIconsBehaviorChanged(hideSilentStatusIcons);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Slog.e(str, "unable to notify listener (hideSilentStatusIcons): " + listener, ex);
            }
        }

        @GuardedBy({"mNotificationLock"})
        public void notifyPostedLocked(NotificationRecord r, NotificationRecord old) {
            notifyPostedLocked(r, old, true);
        }

        @GuardedBy({"mNotificationLock"})
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
                            /* class com.android.server.notification.NotificationManagerService.NotificationListeners.AnonymousClass2 */

                            @Override // java.lang.Runnable
                            public void run() {
                                NotificationListeners.this.notifyPosted(info, sbnToPost, update);
                            }
                        });
                    } else {
                        final StatusBarNotification oldSbnLightClone = oldSbn.cloneLight();
                        NotificationManagerService.this.mHandler.post(new Runnable() {
                            /* class com.android.server.notification.NotificationManagerService.NotificationListeners.AnonymousClass1 */

                            @Override // java.lang.Runnable
                            public void run() {
                                NotificationListeners.this.notifyRemoved(info, oldSbnLightClone, update, null, 6);
                            }
                        });
                    }
                }
            }
        }

        @GuardedBy({"mNotificationLock"})
        public void notifyRemovedLocked(NotificationRecord r, final int reason, NotificationStats notificationStats) {
            int i = reason;
            StatusBarNotification sbn = r.sbn;
            final StatusBarNotification sbnLight = sbn.cloneLight();
            for (final ManagedServices.ManagedServiceInfo info : getServices()) {
                if (NotificationManagerService.this.isVisibleToListener(sbn, info) && ((!r.isHidden() || i == 14 || info.targetSdkVersion >= 28) && (i != 14 || info.targetSdkVersion < 28))) {
                    final NotificationStats stats = NotificationManagerService.this.mAssistants.isServiceTokenValidLocked(info.service) ? notificationStats : null;
                    final NotificationRankingUpdate update = NotificationManagerService.this.makeRankingUpdateLocked(info);
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        /* class com.android.server.notification.NotificationManagerService.NotificationListeners.AnonymousClass3 */

                        @Override // java.lang.Runnable
                        public void run() {
                            NotificationListeners.this.notifyRemoved(info, sbnLight, update, stats, reason);
                        }
                    });
                    i = reason;
                }
            }
            NotificationManagerService.this.mHandler.post(new Runnable(r) {
                /* class com.android.server.notification.$$Lambda$NotificationManagerService$NotificationListeners$3bretMyG2YyNFKU5plLQgmxuGr0 */
                private final /* synthetic */ NotificationRecord f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    NotificationManagerService.NotificationListeners.this.lambda$notifyRemovedLocked$1$NotificationManagerService$NotificationListeners(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$notifyRemovedLocked$1$NotificationManagerService$NotificationListeners(NotificationRecord r) {
            NotificationManagerService.this.updateUriPermissions(null, r, null, 0);
        }

        @GuardedBy({"mNotificationLock"})
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
                            /* class com.android.server.notification.NotificationManagerService.NotificationListeners.AnonymousClass4 */

                            @Override // java.lang.Runnable
                            public void run() {
                                NotificationListeners.this.notifyRankingUpdate(serviceInfo, update);
                            }
                        });
                    }
                }
            }
        }

        @GuardedBy({"mNotificationLock"})
        public void notifyListenerHintsChangedLocked(final int hints) {
            for (final ManagedServices.ManagedServiceInfo serviceInfo : getServices()) {
                if (serviceInfo.isEnabledForCurrentProfiles()) {
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        /* class com.android.server.notification.NotificationManagerService.NotificationListeners.AnonymousClass5 */

                        @Override // java.lang.Runnable
                        public void run() {
                            NotificationListeners.this.notifyListenerHintsChanged(serviceInfo, hints);
                        }
                    });
                }
            }
        }

        @GuardedBy({"mNotificationLock"})
        public void notifyHiddenLocked(List<NotificationRecord> changedNotifications) {
            if (!(changedNotifications == null || changedNotifications.size() == 0)) {
                notifyRankingUpdateLocked(changedNotifications);
                int numChangedNotifications = changedNotifications.size();
                for (int i = 0; i < numChangedNotifications; i++) {
                    NotificationRecord rec = changedNotifications.get(i);
                    NotificationManagerService.this.mListeners.notifyRemovedLocked(rec, 14, rec.getStats());
                }
            }
        }

        @GuardedBy({"mNotificationLock"})
        public void notifyUnhiddenLocked(List<NotificationRecord> changedNotifications) {
            if (!(changedNotifications == null || changedNotifications.size() == 0)) {
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
                        /* class com.android.server.notification.NotificationManagerService.NotificationListeners.AnonymousClass6 */

                        @Override // java.lang.Runnable
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
                        BackgroundThread.getHandler().post(new Runnable(serviceInfo, pkg, user, channel, modificationType) {
                            /* class com.android.server.notification.$$Lambda$NotificationManagerService$NotificationListeners$T5BM1IF40aMGtqZZRr6BWGjzNxA */
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

                            @Override // java.lang.Runnable
                            public final void run() {
                                NotificationManagerService.NotificationListeners.this.lambda$notifyNotificationChannelChanged$2$NotificationManagerService$NotificationListeners(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
                            }
                        });
                    }
                }
            }
        }

        public /* synthetic */ void lambda$notifyNotificationChannelChanged$2$NotificationManagerService$NotificationListeners(ManagedServices.ManagedServiceInfo serviceInfo, String pkg, UserHandle user, NotificationChannel channel, int modificationType) {
            if (NotificationManagerService.this.hasCompanionDevice(serviceInfo) || serviceInfo.component.toString().contains(NotificationManagerService.HWSYSTEMMANAGER_PKG)) {
                notifyNotificationChannelChanged(serviceInfo, pkg, user, channel, modificationType);
            }
        }

        /* access modifiers changed from: protected */
        public void notifyNotificationChannelGroupChanged(String pkg, UserHandle user, NotificationChannelGroup group, int modificationType) {
            if (group != null) {
                for (ManagedServices.ManagedServiceInfo serviceInfo : getServices()) {
                    if (serviceInfo.enabledAndUserMatches(UserHandle.getCallingUserId())) {
                        BackgroundThread.getHandler().post(new Runnable(serviceInfo, pkg, user, group, modificationType) {
                            /* class com.android.server.notification.$$Lambda$NotificationManagerService$NotificationListeners$Srt8NNqA1xJUAp_7nDU6CBZJm_0 */
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

                            @Override // java.lang.Runnable
                            public final void run() {
                                NotificationManagerService.NotificationListeners.this.lambda$notifyNotificationChannelGroupChanged$3$NotificationManagerService$NotificationListeners(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
                            }
                        });
                    }
                }
            }
        }

        public /* synthetic */ void lambda$notifyNotificationChannelGroupChanged$3$NotificationManagerService$NotificationListeners(ManagedServices.ManagedServiceInfo serviceInfo, String pkg, UserHandle user, NotificationChannelGroup group, int modificationType) {
            if (NotificationManagerService.this.hasCompanionDevice(serviceInfo)) {
                notifyNotificationChannelGroupChanged(serviceInfo, pkg, user, group, modificationType);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void notifyPosted(ManagedServices.ManagedServiceInfo info, StatusBarNotification sbn, NotificationRankingUpdate rankingUpdate) {
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
                Slog.e(str2, "unable to notify listener (posted): " + listener, ex2);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void notifyRemoved(ManagedServices.ManagedServiceInfo info, StatusBarNotification sbn, NotificationRankingUpdate rankingUpdate, NotificationStats stats, int reason) {
            if (info.enabledAndUserMatches(sbn.getUserId())) {
                INotificationListener listener = info.service;
                try {
                    listener.onNotificationRemoved(new StatusBarNotificationHolder(sbn), rankingUpdate, stats, reason);
                    LogPower.push(123, sbn.getPackageName(), Integer.toString(sbn.getId()), sbn.getOpPkg(), new String[]{Integer.toString(sbn.getNotification().flags)});
                    NotificationManagerService.this.reportToIAware(sbn.getPackageName(), sbn.getUid(), sbn.getId(), false);
                } catch (RemoteException ex) {
                    String str = this.TAG;
                    Slog.e(str, "unable to notify listener (removed): " + listener, ex);
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void notifyRankingUpdate(ManagedServices.ManagedServiceInfo info, NotificationRankingUpdate rankingUpdate) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationRankingUpdate(rankingUpdate);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Slog.e(str, "unable to notify listener (ranking update): " + listener, ex);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void notifyListenerHintsChanged(ManagedServices.ManagedServiceInfo info, int hints) {
            INotificationListener listener = info.service;
            try {
                listener.onListenerHintsChanged(hints);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Slog.e(str, "unable to notify listener (listener hints): " + listener, ex);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void notifyInterruptionFilterChanged(ManagedServices.ManagedServiceInfo info, int interruptionFilter) {
            INotificationListener listener = info.service;
            try {
                listener.onInterruptionFilterChanged(interruptionFilter);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Slog.e(str, "unable to notify listener (interruption filter): " + listener, ex);
            }
        }

        /* access modifiers changed from: package-private */
        public void notifyNotificationChannelChanged(ManagedServices.ManagedServiceInfo info, String pkg, UserHandle user, NotificationChannel channel, int modificationType) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationChannelModification(pkg, user, channel, modificationType);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Slog.e(str, "unable to notify listener (channel changed): " + listener, ex);
            }
        }

        private void notifyNotificationChannelGroupChanged(ManagedServices.ManagedServiceInfo info, String pkg, UserHandle user, NotificationChannelGroup group, int modificationType) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationChannelGroupModification(pkg, user, group, modificationType);
            } catch (RemoteException ex) {
                String str = this.TAG;
                Slog.e(str, "unable to notify listener (channel group changed): " + listener, ex);
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

    class RoleObserver implements OnRoleHoldersChangedListener {
        private final Executor mExecutor;
        private ArrayMap<String, ArrayMap<Integer, ArraySet<String>>> mNonBlockableDefaultApps;
        private final IPackageManager mPm;
        private final RoleManager mRm;

        RoleObserver(RoleManager roleManager, IPackageManager pkgMgr, Executor executor) {
            this.mRm = roleManager;
            this.mPm = pkgMgr;
            this.mExecutor = executor;
        }

        public void init() {
            List<UserInfo> users = NotificationManagerService.this.mUm.getUsers();
            this.mNonBlockableDefaultApps = new ArrayMap<>();
            for (int i = 0; i < NotificationManagerService.NON_BLOCKABLE_DEFAULT_ROLES.length; i++) {
                ArrayMap<Integer, ArraySet<String>> userToApprovedList = new ArrayMap<>();
                this.mNonBlockableDefaultApps.put(NotificationManagerService.NON_BLOCKABLE_DEFAULT_ROLES[i], userToApprovedList);
                for (int j = 0; j < users.size(); j++) {
                    Integer userId = Integer.valueOf(users.get(j).getUserHandle().getIdentifier());
                    ArraySet<String> approvedForUserId = new ArraySet<>(this.mRm.getRoleHoldersAsUser(NotificationManagerService.NON_BLOCKABLE_DEFAULT_ROLES[i], UserHandle.of(userId.intValue())));
                    ArraySet<Pair<String, Integer>> approvedAppUids = new ArraySet<>();
                    Iterator<String> it = approvedForUserId.iterator();
                    while (it.hasNext()) {
                        String pkg = it.next();
                        approvedAppUids.add(new Pair<>(pkg, Integer.valueOf(getUidForPackage(pkg, userId.intValue()))));
                    }
                    userToApprovedList.put(userId, approvedForUserId);
                    NotificationManagerService.this.mPreferencesHelper.updateDefaultApps(userId.intValue(), null, approvedAppUids);
                }
            }
            this.mRm.addOnRoleHoldersChangedListenerAsUser(this.mExecutor, this, UserHandle.ALL);
        }

        @VisibleForTesting
        public boolean isApprovedPackageForRoleForUser(String role, String pkg, int userId) {
            return this.mNonBlockableDefaultApps.get(role).get(Integer.valueOf(userId)).contains(pkg);
        }

        public void onRoleHoldersChanged(String roleName, UserHandle user) {
            boolean relevantChange = false;
            int i = 0;
            while (true) {
                if (i >= NotificationManagerService.NON_BLOCKABLE_DEFAULT_ROLES.length) {
                    break;
                } else if (NotificationManagerService.NON_BLOCKABLE_DEFAULT_ROLES[i].equals(roleName)) {
                    relevantChange = true;
                    break;
                } else {
                    i++;
                }
            }
            if (relevantChange) {
                ArraySet<String> roleHolders = new ArraySet<>(this.mRm.getRoleHoldersAsUser(roleName, user));
                ArrayMap<Integer, ArraySet<String>> prevApprovedForRole = this.mNonBlockableDefaultApps.getOrDefault(roleName, new ArrayMap<>());
                ArraySet<String> previouslyApproved = prevApprovedForRole.getOrDefault(Integer.valueOf(user.getIdentifier()), new ArraySet<>());
                ArraySet<String> toRemove = new ArraySet<>();
                ArraySet<Pair<String, Integer>> toAdd = new ArraySet<>();
                Iterator<String> it = previouslyApproved.iterator();
                while (it.hasNext()) {
                    String previous = it.next();
                    if (!roleHolders.contains(previous)) {
                        toRemove.add(previous);
                    }
                }
                Iterator<String> it2 = roleHolders.iterator();
                while (it2.hasNext()) {
                    String nowApproved = it2.next();
                    if (!previouslyApproved.contains(nowApproved)) {
                        toAdd.add(new Pair<>(nowApproved, Integer.valueOf(getUidForPackage(nowApproved, user.getIdentifier()))));
                    }
                }
                prevApprovedForRole.put(Integer.valueOf(user.getIdentifier()), roleHolders);
                this.mNonBlockableDefaultApps.put(roleName, prevApprovedForRole);
                NotificationManagerService.this.mPreferencesHelper.updateDefaultApps(user.getIdentifier(), toRemove, toAdd);
            }
        }

        private int getUidForPackage(String pkg, int userId) {
            try {
                return this.mPm.getPackageUid(pkg, (int) DumpState.DUMP_INTENT_FILTER_VERIFIERS, userId);
            } catch (RemoteException e) {
                Slog.e(NotificationManagerService.TAG, "role manager has bad default " + pkg + " " + userId);
                return -1;
            }
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

        /* JADX WARNING: Code restructure failed: missing block: B:35:0x009f, code lost:
            if (r3.equals(com.android.server.utils.PriorityDump.PRIORITY_ARG_CRITICAL) == false) goto L_0x00ac;
         */
        /* JADX WARNING: Removed duplicated region for block: B:41:0x00af  */
        /* JADX WARNING: Removed duplicated region for block: B:43:0x00b5  */
        public static DumpFilter parseFromArguments(String[] args) {
            DumpFilter filter = new DumpFilter();
            int ai = 0;
            while (ai < args.length) {
                String a = args[ai];
                if (PriorityDump.PROTO_ARG.equals(a)) {
                    filter.proto = true;
                } else {
                    boolean z = false;
                    if ("--noredact".equals(a) || "--reveal".equals(a)) {
                        filter.redact = false;
                    } else if ("p".equals(a) || "pkg".equals(a) || "--package".equals(a)) {
                        if (ai < args.length - 1) {
                            ai++;
                            filter.pkgFilter = args[ai].trim().toLowerCase();
                            if (filter.pkgFilter.isEmpty()) {
                                filter.pkgFilter = null;
                            } else {
                                filter.filtered = true;
                            }
                        }
                    } else if ("--zen".equals(a) || "zen".equals(a)) {
                        filter.filtered = true;
                        filter.zen = true;
                    } else if ("--stats".equals(a)) {
                        filter.stats = true;
                        if (ai < args.length - 1) {
                            ai++;
                            filter.since = Long.parseLong(args[ai]);
                        } else {
                            filter.since = 0;
                        }
                    } else if (PriorityDump.PRIORITY_ARG.equals(a) && ai < args.length - 1) {
                        ai++;
                        String str = args[ai];
                        int hashCode = str.hashCode();
                        if (hashCode != -1986416409) {
                            if (hashCode == -1560189025) {
                            }
                        } else if (str.equals(PriorityDump.PRIORITY_ARG_NORMAL)) {
                            z = true;
                            if (z) {
                                filter.criticalPriority = true;
                            } else if (z) {
                                filter.normalPriority = true;
                            }
                        }
                        z = true;
                        if (z) {
                        }
                    }
                }
                ai++;
            }
            return filter;
        }

        public boolean matches(StatusBarNotification sbn) {
            if (!this.filtered || this.zen) {
                return true;
            }
            if (sbn == null || (!matches(sbn.getPackageName()) && !matches(sbn.getOpPkg()))) {
                return false;
            }
            return true;
        }

        public boolean matches(ComponentName component) {
            if (!this.filtered || this.zen) {
                return true;
            }
            if (component == null || !matches(component.getPackageName())) {
                return false;
            }
            return true;
        }

        public boolean matches(String pkg) {
            if (!this.filtered || this.zen) {
                return true;
            }
            if (pkg == null || !pkg.toLowerCase().contains(this.pkgFilter)) {
                return false;
            }
            return true;
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

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void resetAssistantUserSet(int userId) {
        checkCallerIsSystemOrShell();
        this.mAssistants.setUserSet(userId, false);
        handleSavePolicyFile();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public ComponentName getApprovedAssistant(int userId) {
        checkCallerIsSystemOrShell();
        return (ComponentName) CollectionUtils.firstOrNull(this.mAssistants.getAllowedComponents(userId));
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void simulatePackageSuspendBroadcast(boolean suspend, String pkg) {
        String action;
        checkCallerIsSystemOrShell();
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

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void simulatePackageDistractionBroadcast(int flag, String[] pkgs) {
        checkCallerIsSystemOrShell();
        Bundle extras = new Bundle();
        extras.putStringArray("android.intent.extra.changed_package_list", pkgs);
        extras.putInt("android.intent.extra.distraction_restrictions", flag);
        Intent intent = new Intent("android.intent.action.DISTRACTING_PACKAGES_CHANGED");
        intent.putExtras(extras);
        this.mPackageIntentReceiver.onReceive(getContext(), intent);
    }

    /* access modifiers changed from: private */
    public static final class StatusBarNotificationHolder extends IStatusBarNotificationHolder.Stub {
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

    private boolean disallowInterrupt(Notification notification) {
        if (notification == null || notification.extras == null) {
            return false;
        }
        return notification.extras.getBoolean("hw_btw", false);
    }

    private void writeSecureNotificationsPolicy(XmlSerializer out) throws IOException {
        out.startTag(null, LOCKSCREEN_ALLOW_SECURE_NOTIFICATIONS_TAG);
        out.attribute(null, LOCKSCREEN_ALLOW_SECURE_NOTIFICATIONS_VALUE, Boolean.toString(this.mLockScreenAllowSecureNotifications));
        out.endTag(null, LOCKSCREEN_ALLOW_SECURE_NOTIFICATIONS_TAG);
    }

    private static boolean safeBoolean(String val, boolean defValue) {
        if (TextUtils.isEmpty(val)) {
            return defValue;
        }
        return Boolean.parseBoolean(val);
    }

    public void setSysMgrCfgMap(ArrayList<PreferencesHelper.NotificationSysMgrCfg> tempSysMgrCfgList) {
        PreferencesHelper preferencesHelper = this.mPreferencesHelper;
        if (preferencesHelper == null) {
            Log.w(TAG, "setSysMgrCfgMap: mPreferencesHelper is null");
        } else {
            preferencesHelper.setSysMgrCfgMap(tempSysMgrCfgList);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCustDialer(String packageName) {
        return false;
    }
}
