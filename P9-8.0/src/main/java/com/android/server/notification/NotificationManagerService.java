package com.android.server.notification;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.AutomaticZenRule;
import android.app.IActivityManager;
import android.app.INotificationManager;
import android.app.INotificationManager.Stub;
import android.app.ITransientNotification;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.Notification.TvExtender;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.backup.BackupManager;
import android.app.usage.UsageStatsManagerInternal;
import android.common.HwFrameworkFactory;
import android.companion.ICompanionDeviceManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.hdm.HwDeviceManager;
import android.media.AudioManager;
import android.media.AudioManagerInternal;
import android.media.IRingtonePlayer;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.rms.HwSysResource;
import android.service.notification.Adjustment;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.INotificationListener;
import android.service.notification.IStatusBarNotificationHolder;
import android.service.notification.NotificationRankingUpdate;
import android.service.notification.SnoozeCriterion;
import android.service.notification.StatusBarNotification;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.ZenRule;
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
import android.view.WindowManagerInternal;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.server.AbsLocationManagerService;
import com.android.server.DeviceIdleController.LocalService;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.audio.AudioService;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.notification.ManagedServices.Config;
import com.android.server.notification.ManagedServices.ManagedServiceInfo;
import com.android.server.notification.ManagedServices.UserProfiles;
import com.android.server.notification.RankingHelper.NotificationSysMgrCfg;
import com.android.server.notification.ZenModeHelper.Callback;
import com.android.server.os.HwBootFail;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.huawei.pgmng.log.LogPower;
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
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import libcore.io.IoUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class NotificationManagerService extends AbsNotificationManager {
    private static final String ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE = "huawei.intent.action.HWSYSTEMMANAGER_CHANGE_POWERMODE";
    private static final String ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE = "huawei.intent.action.HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE";
    private static final String ACTION_NOTIFICATION_TIMEOUT = (NotificationManagerService.class.getSimpleName() + ".TIMEOUT");
    private static final String ATTR_VERSION = "version";
    private static final int CLOSE_SAVE_POWER = 0;
    static final boolean DBG = Log.isLoggable(TAG, 3);
    private static final int DB_VERSION = 1;
    static final float DEFAULT_MAX_NOTIFICATION_ENQUEUE_RATE = 10.0f;
    static final int DEFAULT_STREAM_TYPE = 5;
    static final long[] DEFAULT_VIBRATE_PATTERN = new long[]{0, 250, 250, 250};
    private static final long DELAY_FOR_ASSISTANT_TIME = 100;
    static final boolean ENABLE_BLOCKED_TOASTS = true;
    public static final boolean ENABLE_CHILD_NOTIFICATIONS = SystemProperties.getBoolean("debug.child_notifs", true);
    private static final int EVENTLOG_ENQUEUE_STATUS_IGNORED = 2;
    private static final int EVENTLOG_ENQUEUE_STATUS_NEW = 0;
    private static final int EVENTLOG_ENQUEUE_STATUS_UPDATE = 1;
    private static final String EXTRA_KEY = "key";
    private static final boolean HWFLOW;
    private static final String HWSYSTEMMANAGER_PKG = "com.huawei.systemmanager";
    static final int LONG_DELAY = 3500;
    static final int MATCHES_CALL_FILTER_CONTACTS_TIMEOUT_MS = 3000;
    static final float MATCHES_CALL_FILTER_TIMEOUT_AFFINITY = 1.0f;
    static final int MAX_PACKAGE_NOTIFICATIONS = 50;
    static final int MESSAGE_LISTENER_HINTS_CHANGED = 5;
    static final int MESSAGE_LISTENER_NOTIFICATION_FILTER_CHANGED = 6;
    private static final int MESSAGE_RANKING_SORT = 1001;
    private static final int MESSAGE_RECONSIDER_RANKING = 1000;
    static final int MESSAGE_SAVE_POLICY_FILE = 3;
    static final int MESSAGE_SEND_RANKING_UPDATE = 4;
    static final int MESSAGE_TIMEOUT = 2;
    private static final long MIN_PACKAGE_OVERRATE_LOG_INTERVAL = 5000;
    private static final int MY_PID = Process.myPid();
    private static final int MY_UID = Process.myUid();
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
    private AlarmManager mAlarmManager;
    private IActivityManager mAm;
    private AppOpsManager mAppOps;
    private UsageStatsManagerInternal mAppUsageStats;
    private Archive mArchive;
    Light mAttentionLight;
    AudioManager mAudioManager;
    AudioManagerInternal mAudioManagerInternal;
    @GuardedBy("mNotificationLock")
    final ArrayMap<Integer, ArrayMap<String, String>> mAutobundledSummaries = new ArrayMap();
    private int mCallState;
    private ICompanionDeviceManager mCompanionManager;
    private ConditionProviders mConditionProviders;
    private boolean mDisableNotificationEffects;
    private List<ComponentName> mEffectsSuppressors = new ArrayList();
    @GuardedBy("mNotificationLock")
    final ArrayList<NotificationRecord> mEnqueuedNotifications = new ArrayList();
    private long[] mFallbackVibrationPattern;
    final IBinder mForegroundToken = new Binder();
    protected boolean mGameDndStatus = false;
    private GroupHelper mGroupHelper;
    private Handler mHandler;
    private boolean mInCall = false;
    private ToneGenerator mInCallToneGenerator;
    private final Object mInCallToneGeneratorLock = new Object();
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int userHandle;
            int user;
            if (action.equals("android.intent.action.SCREEN_ON")) {
                NotificationManagerService.this.mScreenOn = true;
                Flog.i(400, "mIntentReceiver-ACTION_SCREEN_ON");
                NotificationManagerService.this.updateNotificationPulse();
                return;
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                NotificationManagerService.this.mScreenOn = false;
                NotificationManagerService.this.mGameDndStatus = false;
                Flog.i(400, "mIntentReceiver-ACTION_SCREEN_OFF");
                NotificationManagerService.this.updateNotificationPulse();
                return;
            } else if (action.equals("android.intent.action.PHONE_STATE")) {
                NotificationManagerService.this.mInCall = TelephonyManager.EXTRA_STATE_OFFHOOK.equals(intent.getStringExtra(AudioService.CONNECT_INTENT_KEY_STATE));
                Flog.i(400, "mIntentReceiver-ACTION_PHONE_STATE_CHANGED");
                NotificationManagerService.this.updateNotificationPulse();
                synchronized (NotificationManagerService.this.mInCallToneGeneratorLock) {
                    if (NotificationManagerService.this.mInCall) {
                        if (NotificationManagerService.this.mInCallToneGenerator == null) {
                            int relativeToneVolume = NotificationManagerService.this.getContext().getResources().getInteger(17694791);
                            if (relativeToneVolume < 0 || relativeToneVolume > 100) {
                                relativeToneVolume = 100;
                            }
                            try {
                                NotificationManagerService.this.mInCallToneGenerator = new ToneGenerator(0, relativeToneVolume);
                            } catch (RuntimeException e) {
                                Log.e(NotificationManagerService.TAG, "Error creating local tone generator: " + e);
                                NotificationManagerService.this.mInCallToneGenerator = null;
                            }
                        }
                    } else if (NotificationManagerService.this.mInCallToneGenerator != null) {
                        NotificationManagerService.this.mInCallToneGenerator.release();
                        NotificationManagerService.this.mInCallToneGenerator = null;
                    }
                }
                return;
            } else if (action.equals("android.intent.action.USER_STOPPED")) {
                Flog.i(400, "mIntentReceiver-ACTION_USER_STOPPED");
                userHandle = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userHandle >= 0) {
                    NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, null, null, 0, 0, true, userHandle, 6, null);
                    return;
                }
                return;
            } else if (action.equals("android.intent.action.MANAGED_PROFILE_UNAVAILABLE")) {
                userHandle = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userHandle >= 0) {
                    NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, null, null, 0, 0, true, userHandle, 15, null);
                    return;
                }
                return;
            } else if (action.equals("android.intent.action.USER_PRESENT")) {
                if (NotificationManagerService.this.mScreenOn) {
                    NotificationManagerService.this.mNotificationLight.turnOff();
                    StatusBarManagerInternal statusBarManagerInternal = NotificationManagerService.this.mStatusBar;
                    Flog.i(1100, "turn off notificationLight due to Receiver-ACTION_USER_PRESENT");
                    NotificationManagerService.this.mGameDndStatus = NotificationManagerService.this.isGameRunningForeground();
                    NotificationManagerService.this.updateLight(false, 0, 0);
                    return;
                }
                return;
            } else if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                if (!NotificationManagerService.this.mScreenOn || NotificationManagerService.this.mGameDndStatus) {
                    NotificationManagerService.this.mNotificationLight.turnOff();
                    NotificationManagerService.this.updateNotificationPulse();
                    Flog.i(1100, "turn off notificationLight due to Receiver-ACTION_POWER_DISCONNECTED,mScreenOn= " + NotificationManagerService.this.mScreenOn + ",mGameDndStatus= " + NotificationManagerService.this.mGameDndStatus);
                    NotificationManagerService.this.updateLight(false, 0, 0);
                    return;
                }
                return;
            } else if (action.equals("android.intent.action.USER_SWITCHED")) {
                user = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                Flog.i(400, "mIntentReceiver-ACTION_USER_SWITCHED");
                NotificationManagerService.this.mSettingsObserver.update(null);
                NotificationManagerService.this.mUserProfiles.updateCache(context);
                NotificationManagerService.this.mConditionProviders.onUserSwitched(user);
                NotificationManagerService.this.mListeners.onUserSwitched(user);
                NotificationManagerService.this.mNotificationAssistants.onUserSwitched(user);
                NotificationManagerService.this.mZenModeHelper.onUserSwitched(user);
                NotificationManagerService.this.handleUserSwitchEvents(user);
                NotificationManagerService.this.stopPlaySound();
                return;
            } else if (action.equals("android.intent.action.USER_ADDED")) {
                Flog.i(400, "mIntentReceiver-ACTION_USER_ADDED");
                NotificationManagerService.this.mUserProfiles.updateCache(context);
                return;
            } else if (action.equals("android.intent.action.USER_REMOVED")) {
                user = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                NotificationManagerService.this.mZenModeHelper.onUserRemoved(user);
                NotificationManagerService.this.mRankingHelper.onUserRemoved(user);
                NotificationManagerService.this.savePolicyFile();
                return;
            } else if (action.equals("android.intent.action.USER_UNLOCKED")) {
                user = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                NotificationManagerService.this.mConditionProviders.onUserUnlocked(user);
                NotificationManagerService.this.mListeners.onUserUnlocked(user);
                NotificationManagerService.this.mNotificationAssistants.onUserUnlocked(user);
                NotificationManagerService.this.mZenModeHelper.onUserUnlocked(user);
                return;
            } else {
                return;
            }
        }
    };
    private final NotificationManagerInternal mInternalService = new NotificationManagerInternal() {
        public void enqueueNotification(String pkg, String opPkg, int callingUid, int callingPid, String tag, int id, Notification notification, int userId) {
            NotificationManagerService.this.enqueueNotificationInternal(pkg, opPkg, callingUid, callingPid, tag, id, notification, userId);
        }

        public void removeForegroundServiceFlagFromNotification(final String pkg, final int notificationId, final int userId) {
            NotificationManagerService.this.checkCallerIsSystem();
            NotificationManagerService.this.mHandler.post(new Runnable() {
                public void run() {
                    synchronized (NotificationManagerService.this.mNotificationLock) {
                        AnonymousClass7.this.removeForegroundServiceFlagByListLocked(NotificationManagerService.this.mEnqueuedNotifications, pkg, notificationId, userId);
                        AnonymousClass7.this.removeForegroundServiceFlagByListLocked(NotificationManagerService.this.mNotificationList, pkg, notificationId, userId);
                    }
                }
            });
        }

        @GuardedBy("mNotificationLock")
        private void removeForegroundServiceFlagByListLocked(ArrayList<NotificationRecord> notificationList, String pkg, int notificationId, int userId) {
            NotificationRecord r = NotificationManagerService.this.findNotificationByListLocked(notificationList, pkg, null, notificationId, userId);
            if (r != null) {
                StatusBarNotification sbn = r.sbn;
                sbn.getNotification().flags = r.mOriginalFlags & -65;
                NotificationManagerService.this.mRankingHelper.sort(NotificationManagerService.this.mNotificationList);
                NotificationManagerService.this.mListeners.notifyPostedLocked(sbn, sbn);
                int notifyType = r.getImportance() < 2 ? 0 : 1;
                if (r.getNotification().extras.containsKey("hw_btw")) {
                    notifyType = NotificationManagerService.this.disallowInterrupt(r.getNotification()) ? 0 : 1;
                }
                NotificationManagerService.this.mGroupHelper.onNotificationPosted(sbn, notifyType);
            }
        }
    };
    private int mInterruptionFilter = 0;
    private boolean mIsTelevision;
    private long mLastOverRateLogTime;
    ArrayList<String> mLights = new ArrayList();
    private int mListenerHints;
    private NotificationListeners mListeners;
    private final SparseArray<ArraySet<ManagedServiceInfo>> mListenersDisablingEffects = new SparseArray();
    private float mMaxPackageEnqueueRate = DEFAULT_MAX_NOTIFICATION_ENQUEUE_RATE;
    private NotificationAssistants mNotificationAssistants;
    final NotificationDelegate mNotificationDelegate = new NotificationDelegate() {
        public void onSetDisabled(int status) {
            boolean z = false;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationManagerService notificationManagerService = NotificationManagerService.this;
                if ((DumpState.DUMP_DOMAIN_PREFERRED & status) != 0) {
                    z = true;
                }
                notificationManagerService.mDisableNotificationEffects = z;
                if (NotificationManagerService.this.disableNotificationEffects(null) != null) {
                    long identity = Binder.clearCallingIdentity();
                    try {
                        IRingtonePlayer player = NotificationManagerService.this.mAudioManager.getRingtonePlayer();
                        if (player != null) {
                            player.stopAsync();
                        }
                    } catch (RemoteException e) {
                    } catch (Throwable th) {
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                    identity = Binder.clearCallingIdentity();
                    NotificationManagerService.this.mVibrator.cancel();
                }
            }
        }

        public void onClearAll(int callingUid, int callingPid, int userId) {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationManagerService.this.cancelAllLocked(callingUid, callingPid, userId, 3, null, true);
            }
        }

        public void onNotificationClick(int callingUid, int callingPid, String key) {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                Flog.i(400, "onNotificationClick called");
                NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r == null) {
                    Log.w(NotificationManagerService.TAG, "No notification with key: " + key);
                    return;
                }
                long now = System.currentTimeMillis();
                MetricsLogger.action(r.getLogMaker(now).setCategory(128).setType(4));
                EventLogTags.writeNotificationClicked(key, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now));
                StatusBarNotification sbn = r.sbn;
                NotificationManagerService.this.cancelNotification(callingUid, callingPid, sbn.getPackageName(), sbn.getTag(), sbn.getId(), 16, 64, false, r.getUserId(), 1, null);
            }
        }

        public void onNotificationActionClick(int callingUid, int callingPid, String key, int actionIndex) {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r == null) {
                    Log.w(NotificationManagerService.TAG, "No notification with key: " + key);
                    return;
                }
                long now = System.currentTimeMillis();
                MetricsLogger.action(r.getLogMaker(now).setCategory(129).setType(4).setSubtype(actionIndex));
                EventLogTags.writeNotificationActionClicked(key, actionIndex, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now));
            }
        }

        public void onNotificationClear(int callingUid, int callingPid, String pkg, String tag, int id, int userId) {
            NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 66, true, userId, 2, null);
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
            Slog.d(NotificationManagerService.TAG, "onNotification error pkg=" + pkg + " tag=" + tag + " id=" + id + "; will crashApplication(uid=" + uid + ", pid=" + initialPid + ")");
            NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 0, false, userId, 4, null);
            long ident = Binder.clearCallingIdentity();
            try {
                ActivityManager.getService().crashApplication(uid, initialPid, pkg, -1, "Bad notification posted from package " + pkg + ": " + message);
            } catch (RemoteException e) {
            }
            Binder.restoreCallingIdentity(ident);
        }

        public void onNotificationVisibilityChanged(NotificationVisibility[] newlyVisibleKeys, NotificationVisibility[] noLongerVisibleKeys) {
            int i = 0;
            Flog.i(400, "onNotificationVisibilityChanged called");
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r;
                NotificationVisibility nv;
                for (NotificationVisibility nv2 : newlyVisibleKeys) {
                    r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(nv2.key);
                    if (r != null) {
                        r.setVisibility(true, nv2.rank);
                        nv2.recycle();
                    }
                }
                int length = noLongerVisibleKeys.length;
                while (i < length) {
                    nv2 = noLongerVisibleKeys[i];
                    r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(nv2.key);
                    if (r != null) {
                        r.setVisibility(false, nv2.rank);
                        nv2.recycle();
                    }
                    i++;
                }
            }
        }

        public void onNotificationExpansionChanged(String key, boolean userAction, boolean expanded) {
            int i = 1;
            Flog.i(400, "onNotificationExpansionChanged called");
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r != null) {
                    int i2;
                    r.stats.onExpansionChanged(userAction, expanded);
                    long now = System.currentTimeMillis();
                    MetricsLogger.action(r.getLogMaker(now).setCategory(128).setType(3));
                    if (userAction) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    if (!expanded) {
                        i = 0;
                    }
                    EventLogTags.writeNotificationExpansion(key, i2, i, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now));
                }
            }
        }
    };
    private Light mNotificationLight;
    @GuardedBy("mNotificationLock")
    protected final ArrayList<NotificationRecord> mNotificationList = new ArrayList();
    final Object mNotificationLock = new Object();
    private boolean mNotificationPulseEnabled;
    private HwSysResource mNotificationResource;
    private final BroadcastReceiver mNotificationTimeoutReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && NotificationManagerService.ACTION_NOTIFICATION_TIMEOUT.equals(action)) {
                NotificationRecord record;
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    record = NotificationManagerService.this.findNotificationByKeyLocked(intent.getStringExtra(NotificationManagerService.EXTRA_KEY));
                }
                if (record != null) {
                    NotificationManagerService.this.cancelNotification(record.sbn.getUid(), record.sbn.getInitialPid(), record.sbn.getPackageName(), record.sbn.getTag(), record.sbn.getId(), 0, 64, true, record.getUserId(), 19, null);
                }
            }
        }
    };
    @GuardedBy("mNotificationLock")
    final ArrayMap<String, NotificationRecord> mNotificationsByKey = new ArrayMap();
    private final BroadcastReceiver mPackageIntentReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Missing block: B:16:0x004c, code:
            if (r14.equals("android.intent.action.PACKAGES_SUSPENDED") == false) goto L_0x01de;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                boolean removingPackage;
                String[] pkgList;
                String pkgName;
                int i;
                int queryRestart = 0;
                boolean queryRemove = false;
                boolean packageChanged = false;
                boolean cancelNotifications = true;
                int reason = 5;
                if (!action.equals("android.intent.action.PACKAGE_ADDED")) {
                    queryRemove = action.equals("android.intent.action.PACKAGE_REMOVED");
                    if (!(queryRemove || action.equals("android.intent.action.PACKAGE_RESTARTED"))) {
                        packageChanged = action.equals("android.intent.action.PACKAGE_CHANGED");
                        if (!packageChanged) {
                            queryRestart = action.equals("android.intent.action.QUERY_PACKAGE_RESTART");
                            if (queryRestart == 0) {
                                if (!action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")) {
                                }
                            }
                        }
                    }
                }
                int changeUserId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                Flog.i(400, "mIntentReceiver-Package changed");
                int[] uidList = null;
                if (queryRemove) {
                    removingPackage = intent.getBooleanExtra("android.intent.extra.REPLACING", false) ^ 1;
                } else {
                    removingPackage = false;
                }
                Flog.i(400, "action=" + action + " removing=" + removingPackage);
                if (action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                    uidList = intent.getIntArrayExtra("android.intent.extra.changed_uid_list");
                } else if (action.equals("android.intent.action.PACKAGES_SUSPENDED")) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                    reason = 14;
                } else if (queryRestart != 0) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.PACKAGES");
                    uidList = new int[1];
                    uidList[0] = intent.getIntExtra("android.intent.extra.UID", -1);
                } else {
                    Uri uri = intent.getData();
                    if (uri != null) {
                        pkgName = uri.getSchemeSpecificPart();
                        if (pkgName != null) {
                            if (queryRemove || packageChanged) {
                                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                                if (!(uid == -1 || NotificationManagerService.this.mNotificationResource == null)) {
                                    NotificationManagerService.this.mNotificationResource.clear(uid, pkgName, -1);
                                }
                            }
                            if (packageChanged) {
                                try {
                                    IPackageManager -get23 = NotificationManagerService.this.mPackageManager;
                                    if (changeUserId != -1) {
                                        i = changeUserId;
                                    } else {
                                        i = 0;
                                    }
                                    int enabled = -get23.getApplicationEnabledSetting(pkgName, i);
                                    if (enabled == 1 || enabled == 0) {
                                        cancelNotifications = false;
                                    }
                                } catch (Throwable e) {
                                    Flog.i(400, "Exception trying to look up app enabled setting", e);
                                } catch (RemoteException e2) {
                                }
                            }
                            pkgList = new String[]{pkgName};
                            uidList = new int[1];
                            uidList[0] = intent.getIntExtra("android.intent.extra.UID", -1);
                        } else {
                            return;
                        }
                    }
                    return;
                }
                if (pkgList != null && pkgList.length > 0) {
                    i = 0;
                    int length = pkgList.length;
                    while (true) {
                        int i2 = i;
                        if (i2 >= length) {
                            break;
                        }
                        pkgName = pkgList[i2];
                        if (cancelNotifications) {
                            NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkgName, null, 0, 0, queryRestart ^ 1, changeUserId, reason, null);
                        }
                        i = i2 + 1;
                    }
                }
                NotificationManagerService.this.mListeners.onPackagesChanged(removingPackage, pkgList);
                NotificationManagerService.this.mNotificationAssistants.onPackagesChanged(removingPackage, pkgList);
                NotificationManagerService.this.mConditionProviders.onPackagesChanged(removingPackage, pkgList);
                NotificationManagerService.this.mRankingHelper.onPackagesChanged(removingPackage, changeUserId, pkgList, uidList);
                NotificationManagerService.this.savePolicyFile();
            }
        }
    };
    private IPackageManager mPackageManager;
    private PackageManager mPackageManagerClient;
    final PolicyAccess mPolicyAccess = new PolicyAccess(this, null);
    private AtomicFile mPolicyFile;
    private PowerSaverObserver mPowerSaverObserver;
    private RankingHandler mRankingHandler;
    private RankingHelper mRankingHelper;
    private final HandlerThread mRankingThread = new HandlerThread("ranker", 10);
    private boolean mScreenOn = true;
    private final IBinder mService = new Stub() {
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (1599293262 == code) {
                try {
                    int event = data.readInt();
                    Flog.i(400, "NotificationManagerService.onTransact: got ST_GET_NOTIFICATIONS event " + event);
                    switch (event) {
                        case 1:
                            NotificationManagerService.this.handleGetNotifications(data, reply);
                            return true;
                    }
                } catch (Exception e) {
                    Flog.i(400, "NotificationManagerService.onTransact: catch exception " + e.toString());
                    return false;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        public void enqueueToast(String pkg, ITransientNotification callback, int duration) {
            enqueueToastEx(pkg, callback, duration, 0);
        }

        public void enqueueToastEx(String pkg, ITransientNotification callback, int duration, int displayId) {
            Flog.i(400, "enqueueToast pkg=" + pkg + " callback=" + callback + " duration=" + duration);
            if (pkg == null || callback == null) {
                Slog.e(NotificationManagerService.TAG, "Not doing toast. pkg=" + pkg + " callback=" + callback);
                return;
            }
            int isSystemToast = !NotificationManagerService.this.isCallerSystemOrPhone() ? "android".equals(pkg) : 1;
            boolean isPackageSuspended = NotificationManagerService.this.isPackageSuspendedForUser(pkg, Binder.getCallingUid());
            if ((isSystemToast ^ 1) != 0) {
                if (!areNotificationsEnabledForPackage(pkg, Binder.getCallingUid()) || isPackageSuspended) {
                    String str;
                    String str2 = NotificationManagerService.TAG;
                    StringBuilder append = new StringBuilder().append("Suppressing toast from package ").append(pkg);
                    if (isPackageSuspended) {
                        str = " due to package suspended by administrator.";
                    } else {
                        str = " by user request.";
                    }
                    Slog.e(str2, append.append(str).toString());
                    return;
                }
            }
            synchronized (NotificationManagerService.this.mToastQueue) {
                int callingPid = Binder.getCallingPid();
                long callingId = Binder.clearCallingIdentity();
                try {
                    int index = NotificationManagerService.this.indexOfToastLocked(pkg, callback);
                    if (index >= 0) {
                        ((ToastRecord) NotificationManagerService.this.mToastQueue.get(index)).update(duration);
                    } else {
                        ToastRecord record;
                        if (isSystemToast == 0) {
                            int count = 0;
                            int N = NotificationManagerService.this.mToastQueue.size();
                            for (int i = 0; i < N; i++) {
                                if (((ToastRecord) NotificationManagerService.this.mToastQueue.get(i)).pkg.equals(pkg)) {
                                    count++;
                                    if (count >= 50) {
                                        Slog.e(NotificationManagerService.TAG, "Package has already posted " + count + " toasts. Not showing more. Package=" + pkg);
                                        Binder.restoreCallingIdentity(callingId);
                                        return;
                                    }
                                }
                            }
                        }
                        Binder token = new Binder();
                        NotificationManagerService.this.mWindowManagerInternal.addWindowToken(token, 2005, displayId);
                        if (displayId == 0) {
                            record = new ToastRecord(callingPid, pkg, callback, duration, token);
                        } else {
                            record = new ToastRecordEx(callingPid, pkg, callback, duration, token, displayId);
                        }
                        NotificationManagerService.this.mToastQueue.add(record);
                        index = NotificationManagerService.this.mToastQueue.size() - 1;
                        NotificationManagerService.this.keepProcessAliveIfNeededLocked(callingPid);
                    }
                    if (index == 0) {
                        NotificationManagerService.this.showNextToastLocked();
                    }
                    Binder.restoreCallingIdentity(callingId);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
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
                    Binder.restoreCallingIdentity(callingId);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }

        public void enqueueNotificationWithTag(String pkg, String opPkg, String tag, int id, Notification notification, int userId) throws RemoteException {
            NotificationManagerService.this.addHwExtraForNotification(notification, pkg, Binder.getCallingPid());
            NotificationManagerService.this.enqueueNotificationInternal(NotificationManagerService.this.getNCTargetAppPkg(opPkg, pkg, notification), opPkg, Binder.getCallingUid(), Binder.getCallingPid(), tag, id, notification, userId);
        }

        public void cancelNotificationWithTag(String pkg, String tag, int id, int userId) {
            int mustNotHaveFlags;
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            userId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, false, "cancelNotificationWithTag", pkg);
            if (NotificationManagerService.HWFLOW) {
                Flog.i(400, "cancelNotificationWithTag pid " + Binder.getCallingPid() + ",uid = " + Binder.getCallingUid() + ",tag = " + tag + ",pkg =" + pkg + ",id =" + id);
            }
            if (NotificationManagerService.this.isCallingUidSystem()) {
                mustNotHaveFlags = 0;
            } else {
                mustNotHaveFlags = 1088;
            }
            NotificationManagerService.this.cancelNotification(Binder.getCallingUid(), Binder.getCallingPid(), pkg, tag, id, 0, mustNotHaveFlags, false, userId, 8, null);
        }

        public void cancelAllNotifications(String pkg, int userId) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            Flog.i(400, "cancelAllNotifications pid " + Binder.getCallingPid() + ",uid = " + Binder.getCallingUid());
            NotificationManagerService.this.cancelAllNotificationsInt(Binder.getCallingUid(), Binder.getCallingPid(), pkg, null, 0, 64, true, ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, false, "cancelAllNotifications", pkg), 9, null);
        }

        public void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled) {
            NotificationManagerService.this.checkCallerIsSystem();
            NotificationManagerService.this.mRankingHelper.setEnabled(pkg, uid, enabled);
            if (!enabled) {
                NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkg, null, 0, 0, true, UserHandle.getUserId(uid), 7, null);
            }
            NotificationManagerService.this.savePolicyFile();
        }

        public boolean areNotificationsEnabled(String pkg) {
            return areNotificationsEnabledForPackage(pkg, Binder.getCallingUid());
        }

        public boolean areNotificationsEnabledForPackage(String pkg, int uid) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            if (NotificationManagerService.this.mRankingHelper.getImportance(pkg, uid) != 0) {
                return true;
            }
            return false;
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

        public void createNotificationChannelGroups(String pkg, ParceledListSlice channelGroupList) throws RemoteException {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            List<NotificationChannelGroup> groups = channelGroupList.getList();
            List<NotificationChannelGroup> groupsCache = new ArrayList(groups);
            int groupSize = groups.size();
            for (int i = 0; i < groupSize; i++) {
                NotificationChannelGroup group = (NotificationChannelGroup) groupsCache.get(i);
                Preconditions.checkNotNull(group, "group in list is null");
                NotificationManagerService.this.mRankingHelper.createNotificationChannelGroup(pkg, Binder.getCallingUid(), group, true);
                NotificationManagerService.this.mListeners.notifyNotificationChannelGroupChanged(pkg, UserHandle.of(UserHandle.getCallingUserId()), group, 1);
            }
            NotificationManagerService.this.savePolicyFile();
        }

        private void createNotificationChannelsImpl(String pkg, int uid, ParceledListSlice channelsList) {
            List<NotificationChannel> channels = channelsList.getList();
            int channelsSize = channels.size();
            for (int i = 0; i < channelsSize; i++) {
                NotificationChannel channel = (NotificationChannel) channels.get(i);
                Preconditions.checkNotNull(channel, "channel in list is null");
                NotificationManagerService.this.mRankingHelper.createNotificationChannel(pkg, uid, channel, true);
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
            Object callingPkgName = null;
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
            if ("miscellaneous".equals(channelId)) {
                throw new IllegalArgumentException("Cannot delete default channel");
            }
            NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkg, channelId, 0, 0, true, UserHandle.getUserId(callingUid), 17, null);
            NotificationManagerService.this.mRankingHelper.deleteNotificationChannel(pkg, callingUid, channelId);
            NotificationManagerService.this.mListeners.notifyNotificationChannelChanged(pkg, UserHandle.getUserHandleForUid(callingUid), NotificationManagerService.this.mRankingHelper.getNotificationChannel(pkg, callingUid, channelId, true), 3);
            NotificationManagerService.this.savePolicyFile();
        }

        public ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroups(String pkg) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            return new ParceledListSlice(new ArrayList(NotificationManagerService.this.mRankingHelper.getNotificationChannelGroups(pkg, Binder.getCallingUid())));
        }

        public void deleteNotificationChannelGroup(String pkg, String groupId) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            int callingUid = Binder.getCallingUid();
            NotificationChannelGroup groupToDelete = NotificationManagerService.this.mRankingHelper.getNotificationChannelGroup(groupId, pkg, callingUid);
            if (groupToDelete != null) {
                List<NotificationChannel> deletedChannels = NotificationManagerService.this.mRankingHelper.deleteNotificationChannelGroup(pkg, callingUid, groupId);
                for (int i = 0; i < deletedChannels.size(); i++) {
                    NotificationChannel deletedChannel = (NotificationChannel) deletedChannels.get(i);
                    NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkg, deletedChannel.getId(), 0, 0, true, UserHandle.getUserId(Binder.getCallingUid()), 17, null);
                    NotificationManagerService.this.mListeners.notifyNotificationChannelChanged(pkg, UserHandle.getUserHandleForUid(callingUid), deletedChannel, 3);
                }
                NotificationManagerService.this.mListeners.notifyNotificationChannelGroupChanged(pkg, UserHandle.getUserHandleForUid(callingUid), groupToDelete, 3);
                NotificationManagerService.this.savePolicyFile();
            }
        }

        public void updateNotificationChannelForPackage(String pkg, int uid, NotificationChannel channel) {
            enforceSystemOrSystemUI("Caller not system or systemui");
            Preconditions.checkNotNull(channel);
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

        public ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroupsForPackage(String pkg, int uid, boolean includeDeleted) {
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mRankingHelper.getNotificationChannelGroups(pkg, uid, includeDeleted);
        }

        public NotificationChannelGroup getNotificationChannelGroupForPackage(String groupId, String pkg, int uid) {
            enforceSystemOrSystemUI("getNotificationChannelGroupForPackage");
            return NotificationManagerService.this.mRankingHelper.getNotificationChannelGroup(groupId, pkg, uid);
        }

        public ParceledListSlice<NotificationChannel> getNotificationChannels(String pkg) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            return NotificationManagerService.this.mRankingHelper.getNotificationChannels(pkg, Binder.getCallingUid(), false);
        }

        public void clearData(String packageName, int uid, boolean fromApp) throws RemoteException {
            NotificationManagerService.this.checkCallerIsSystem();
            NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, packageName, null, 0, 0, true, UserHandle.getUserId(Binder.getCallingUid()), 17, null);
            NotificationManagerService.this.mListeners.onPackagesChanged(true, new String[]{packageName});
            NotificationManagerService.this.mNotificationAssistants.onPackagesChanged(true, new String[]{packageName});
            NotificationManagerService.this.mConditionProviders.onPackagesChanged(true, new String[]{packageName});
            if (!fromApp) {
                NotificationManagerService.this.mRankingHelper.onPackagesChanged(true, UserHandle.getCallingUserId(), new String[]{packageName}, new int[]{uid});
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
                        tmp[i] = ((NotificationRecord) NotificationManagerService.this.mNotificationList.get(i)).sbn;
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
                int i;
                StatusBarNotification sbn;
                ArrayMap<String, StatusBarNotification> map = new ArrayMap(NotificationManagerService.this.mNotificationList.size() + NotificationManagerService.this.mEnqueuedNotifications.size());
                int N = NotificationManagerService.this.mNotificationList.size();
                for (i = 0; i < N; i++) {
                    sbn = sanitizeSbn(pkg, userId, ((NotificationRecord) NotificationManagerService.this.mNotificationList.get(i)).sbn);
                    if (sbn != null) {
                        map.put(sbn.getKey(), sbn);
                    }
                }
                for (NotificationRecord snoozed : NotificationManagerService.this.mSnoozeHelper.getSnoozed(userId, pkg)) {
                    sbn = sanitizeSbn(pkg, userId, snoozed.sbn);
                    if (sbn != null) {
                        map.put(sbn.getKey(), sbn);
                    }
                }
                int M = NotificationManagerService.this.mEnqueuedNotifications.size();
                for (i = 0; i < M; i++) {
                    sbn = sanitizeSbn(pkg, userId, ((NotificationRecord) NotificationManagerService.this.mEnqueuedNotifications.get(i)).sbn);
                    if (sbn != null) {
                        map.put(sbn.getKey(), sbn);
                    }
                }
                ArrayList<StatusBarNotification> list = new ArrayList(map.size());
                list.addAll(map.values());
                parceledListSlice = new ParceledListSlice(list);
            }
            return parceledListSlice;
        }

        private StatusBarNotification sanitizeSbn(String pkg, int userId, StatusBarNotification sbn) {
            if (sbn.getPackageName().equals(pkg) && sbn.getUserId() == userId && (sbn.getNotification().flags & 1024) == 0) {
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
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            long identity = Binder.clearCallingIdentity();
            try {
                Flog.i(400, "cancelNotificationsFromListener called,callingUid = " + callingUid + ",callingPid = " + callingPid);
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    if (keys != null) {
                        for (Object obj : keys) {
                            NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(obj);
                            if (r != null) {
                                int userId = r.sbn.getUserId();
                                if (userId == info.userid || userId == -1 || (NotificationManagerService.this.mUserProfiles.isCurrentProfile(userId) ^ 1) == 0) {
                                    cancelNotificationFromListenerLocked(info, callingUid, callingPid, r.sbn.getPackageName(), r.sbn.getTag(), r.sbn.getId(), userId);
                                } else {
                                    throw new SecurityException("Disallowed call from listener: " + info.service);
                                }
                            }
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
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(component.getPackageName());
            long identity = Binder.clearCallingIdentity();
            try {
                ManagedServices manager;
                if (NotificationManagerService.this.mNotificationAssistants.isComponentEnabledForCurrentProfiles(component)) {
                    manager = NotificationManagerService.this.mNotificationAssistants;
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
                    ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
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
                    ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    if (keys != null) {
                        int N = keys.length;
                        for (int i = 0; i < N; i++) {
                            NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(keys[i]);
                            if (r != null) {
                                int userId = r.sbn.getUserId();
                                if (userId != info.userid && userId != -1 && (NotificationManagerService.this.mUserProfiles.isCurrentProfile(userId) ^ 1) != 0) {
                                    throw new SecurityException("Disallowed call from listener: " + info.service);
                                } else if (!r.isSeen()) {
                                    Flog.i(400, "Marking notification as seen " + keys[i]);
                                    UsageStatsManagerInternal -get5 = NotificationManagerService.this.mAppUsageStats;
                                    String packageName = r.sbn.getPackageName();
                                    if (userId == -1) {
                                        userId = 0;
                                    }
                                    -get5.reportEvent(packageName, userId, 7);
                                    r.setSeen();
                                }
                            }
                        }
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        @GuardedBy("mNotificationLock")
        private void cancelNotificationFromListenerLocked(ManagedServiceInfo info, int callingUid, int callingPid, String pkg, String tag, int id, int userId) {
            NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 66, true, userId, 10, info);
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
                    NotificationManagerService.this.unsnoozeNotificationInt(key, NotificationManagerService.this.mNotificationAssistants.checkServiceTokenLocked(token));
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void cancelNotificationFromListener(INotificationListener token, String pkg, String tag, int id) {
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    if (info.supportsProfiles()) {
                        Log.e(NotificationManagerService.TAG, "Ignoring deprecated cancelNotification(pkg, tag, id) from " + info.component + " use cancelNotification(key) instead.");
                    } else {
                        cancelNotificationFromListenerLocked(info, callingUid, callingPid, pkg, tag, id, info.userid);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public ParceledListSlice<StatusBarNotification> getActiveNotificationsFromListener(INotificationListener token, String[] keys, int trim) {
            ParceledListSlice<StatusBarNotification> parceledListSlice;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                boolean getKeys = keys != null;
                int N = getKeys ? keys.length : NotificationManagerService.this.mNotificationList.size();
                ArrayList<StatusBarNotification> list = new ArrayList(N);
                for (int i = 0; i < N; i++) {
                    NotificationRecord r;
                    if (getKeys) {
                        r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(keys[i]);
                    } else {
                        r = (NotificationRecord) NotificationManagerService.this.mNotificationList.get(i);
                    }
                    if (r != null) {
                        StatusBarNotification sbn = r.sbn;
                        if (NotificationManagerService.this.isVisibleToListener(sbn, info)) {
                            list.add(trim == 0 ? sbn : sbn.cloneLight());
                        } else {
                            continue;
                        }
                    }
                }
                parceledListSlice = new ParceledListSlice(list);
            }
            return parceledListSlice;
        }

        public ParceledListSlice<StatusBarNotification> getSnoozedNotificationsFromListener(INotificationListener token, int trim) {
            ParceledListSlice<StatusBarNotification> parceledListSlice;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                List<NotificationRecord> snoozedRecords = NotificationManagerService.this.mSnoozeHelper.getSnoozed();
                int N = snoozedRecords.size();
                ArrayList<StatusBarNotification> list = new ArrayList(N);
                for (int i = 0; i < N; i++) {
                    NotificationRecord r = (NotificationRecord) snoozedRecords.get(i);
                    if (r != null) {
                        StatusBarNotification sbn = r.sbn;
                        if (NotificationManagerService.this.isVisibleToListener(sbn, info)) {
                            list.add(trim == 0 ? sbn : sbn.cloneLight());
                        } else {
                            continue;
                        }
                    }
                }
                parceledListSlice = new ParceledListSlice(list);
            }
            return parceledListSlice;
        }

        public void requestHintsFromListener(INotificationListener token, int hints) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
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
            int -get16;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                -get16 = NotificationManagerService.this.mListenerHints;
            }
            return -get16;
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
            int -get15;
            synchronized (NotificationManagerService.this.mNotificationLight) {
                -get15 = NotificationManagerService.this.mInterruptionFilter;
            }
            return -get15;
        }

        public void setOnNotificationPostedTrimFromListener(INotificationListener token, int trim) throws RemoteException {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                if (info == null) {
                    return;
                }
                NotificationManagerService.this.mListeners.setOnNotificationPostedTrimLocked(info, trim);
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

        public List<ZenRule> getZenRules() throws RemoteException {
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
            if (zen == -1) {
                throw new IllegalArgumentException("Invalid filter: " + filter);
            }
            long identity = Binder.clearCallingIdentity();
            try {
                NotificationManagerService.this.mZenModeHelper.setManualZenMode(zen, null, pkg, "setInterruptionFilter");
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void notifyConditions(final String pkg, IConditionProvider provider, final Condition[] conditions) {
            final ManagedServiceInfo info = NotificationManagerService.this.mConditionProviders.checkServiceToken(provider);
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
                ManagedServiceInfo info = NotificationManagerService.this.mConditionProviders.checkServiceToken(provider);
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
                for (String checkPolicyAccess : NotificationManagerService.this.getContext().getPackageManager().getPackagesForUid(uid)) {
                    if (checkPolicyAccess(checkPolicyAccess)) {
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
            return NotificationManagerService.this.mPolicyAccess.isPackageGranted(pkg);
        }

        private boolean checkPolicyAccess(String pkg) {
            boolean z = true;
            try {
                if (ActivityManager.checkComponentPermission("android.permission.MANAGE_NOTIFICATIONS", NotificationManagerService.this.getContext().getPackageManager().getPackageUidAsUser(pkg, UserHandle.getCallingUserId()), -1, true) == 0) {
                    return true;
                }
                if (!checkPackagePolicyAccess(pkg)) {
                    z = NotificationManagerService.this.mListeners.isComponentEnabledForPackage(pkg);
                }
                return z;
            } catch (NameNotFoundException e) {
                return false;
            }
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpAndUsageStatsPermission(NotificationManagerService.this.getContext(), NotificationManagerService.TAG, pw)) {
                DumpFilter filter = DumpFilter.parseFromArguments(args);
                if (filter != null && filter.stats) {
                    NotificationManagerService.this.dumpJson(pw, filter);
                } else if (filter == null || !filter.proto) {
                    NotificationManagerService.this.dumpImpl(pw, filter);
                } else {
                    NotificationManagerService.this.dumpProto(fd, filter);
                }
            }
        }

        public ComponentName getEffectsSuppressor() {
            return !NotificationManagerService.this.mEffectsSuppressors.isEmpty() ? (ComponentName) NotificationManagerService.this.mEffectsSuppressors.get(0) : null;
        }

        public boolean matchesCallFilter(Bundle extras) {
            enforceSystemOrSystemUI("INotificationManager.matchesCallFilter");
            return NotificationManagerService.this.mZenModeHelper.matchesCallFilter(Binder.getCallingUserHandle(), extras, (ValidateNotificationPeople) NotificationManagerService.this.mRankingHelper.findExtractor(ValidateNotificationPeople.class), NotificationManagerService.MATCHES_CALL_FILTER_CONTACTS_TIMEOUT_MS, 1.0f);
        }

        public boolean isSystemConditionProviderEnabled(String path) {
            enforceSystemOrSystemUI("INotificationManager.isSystemConditionProviderEnabled");
            return NotificationManagerService.this.mConditionProviders.isSystemProviderEnabled(path);
        }

        public byte[] getBackupPayload(int user) {
            if (NotificationManagerService.DBG) {
                Slog.d(NotificationManagerService.TAG, "getBackupPayload u=" + user);
            }
            if (user != 0) {
                Slog.w(NotificationManagerService.TAG, "getBackupPayload: cannot backup policy for user " + user);
                return null;
            }
            byte[] toByteArray;
            synchronized (NotificationManagerService.this.mPolicyFile) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    NotificationManagerService.this.writePolicyXml(baos, true);
                    toByteArray = baos.toByteArray();
                } catch (IOException e) {
                    Slog.w(NotificationManagerService.TAG, "getBackupPayload: error writing payload for user " + user, e);
                    return null;
                }
            }
            return toByteArray;
        }

        /* JADX WARNING: Removed duplicated region for block: B:20:0x0086 A:{Splitter: B:16:0x0079, ExcHandler: java.lang.NumberFormatException (r1_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x0086 A:{Splitter: B:16:0x0079, ExcHandler: java.lang.NumberFormatException (r1_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:20:0x0086, code:
            r1 = move-exception;
     */
        /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.w(com.android.server.notification.NotificationManagerService.TAG, "applyRestore: error reading payload", r1);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void applyRestore(byte[] payload, int user) {
            String str = null;
            if (NotificationManagerService.DBG) {
                String str2 = NotificationManagerService.TAG;
                StringBuilder append = new StringBuilder().append("applyRestore u=").append(user).append(" payload=");
                if (payload != null) {
                    str = new String(payload, StandardCharsets.UTF_8);
                }
                Slog.d(str2, append.append(str).toString());
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
                    } catch (Exception e) {
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

        public String[] getPackagesRequestingNotificationPolicyAccess() throws RemoteException {
            enforceSystemOrSystemUI("request policy access packages");
            long identity = Binder.clearCallingIdentity();
            try {
                String[] requestingPackages = NotificationManagerService.this.mPolicyAccess.getRequestingPackages();
                return requestingPackages;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void setNotificationPolicyAccessGranted(String pkg, boolean granted) throws RemoteException {
            enforceSystemOrSystemUI("grant notification policy access");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.mPolicyAccess.put(pkg, granted);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public Policy getNotificationPolicy(String pkg) {
            enforcePolicyAccess(pkg, "getNotificationPolicy");
            long identity = Binder.clearCallingIdentity();
            try {
                Policy notificationPolicy = NotificationManagerService.this.mZenModeHelper.getNotificationPolicy();
                return notificationPolicy;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void setNotificationPolicy(String pkg, Policy policy) {
            enforcePolicyAccess(pkg, "setNotificationPolicy");
            long identity = Binder.clearCallingIdentity();
            try {
                NotificationManagerService.this.mZenModeHelper.setNotificationPolicy(policy);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void applyEnqueuedAdjustmentFromAssistant(INotificationListener token, Adjustment adjustment) throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.mNotificationAssistants.checkServiceTokenLocked(token);
                    int N = NotificationManagerService.this.mEnqueuedNotifications.size();
                    for (int i = 0; i < N; i++) {
                        NotificationRecord n = (NotificationRecord) NotificationManagerService.this.mEnqueuedNotifications.get(i);
                        if (Objects.equals(adjustment.getKey(), n.getKey()) && Objects.equals(Integer.valueOf(adjustment.getUser()), Integer.valueOf(n.getUserId()))) {
                            NotificationManagerService.this.applyAdjustment(n, adjustment);
                            break;
                        }
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void applyAdjustmentFromAssistant(INotificationListener token, Adjustment adjustment) throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.mNotificationAssistants.checkServiceTokenLocked(token);
                    NotificationManagerService.this.applyAdjustment((NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(adjustment.getKey()), adjustment);
                }
                NotificationManagerService.this.mRankingHandler.requestSort(true);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void applyAdjustmentsFromAssistant(INotificationListener token, List<Adjustment> adjustments) throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.mNotificationAssistants.checkServiceTokenLocked(token);
                    for (Adjustment adjustment : adjustments) {
                        NotificationManagerService.this.applyAdjustment((NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(adjustment.getKey()), adjustment);
                    }
                }
                NotificationManagerService.this.mRankingHandler.requestSort(true);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
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
            List<NotificationChannelGroup> groups = new ArrayList();
            groups.addAll(NotificationManagerService.this.mRankingHelper.getNotificationChannelGroups(pkg, getUidForPackageAndUser(pkg, user)));
            return new ParceledListSlice(groups);
        }

        private void verifyPrivilegedListener(INotificationListener token, UserHandle user) {
            ManagedServiceInfo info;
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
            int uid = 0;
            long identity = Binder.clearCallingIdentity();
            try {
                uid = NotificationManagerService.this.mPackageManager.getPackageUid(pkg, 0, user.getIdentifier());
                return uid;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void doBindRecSys() {
            Slog.w(NotificationManagerService.TAG, "do bind recsys service");
            NotificationManagerService.this.checkCallerIsSystem();
            NotificationManagerService.this.bindRecSys();
        }
    };
    private SettingsObserver mSettingsObserver;
    private SnoozeHelper mSnoozeHelper;
    protected String mSoundNotificationKey;
    StatusBarManagerInternal mStatusBar;
    final ArrayMap<String, NotificationRecord> mSummaryByGroupKey = new ArrayMap();
    boolean mSystemReady;
    final ArrayList<ToastRecord> mToastQueue = new ArrayList();
    private NotificationUsageStats mUsageStats;
    private boolean mUseAttentionLight;
    private final UserProfiles mUserProfiles = new UserProfiles();
    private String mVibrateNotificationKey;
    Vibrator mVibrator;
    private WindowManagerInternal mWindowManagerInternal;
    private ZenModeHelper mZenModeHelper;
    private String[] noitfication_white_list = new String[]{"com.huawei.message", "com.android.mms", "com.android.contacts", "com.android.phone", "com.android.deskclock", "com.android.calendar", "com.android.systemui", "android", "com.android.incallui", "com.android.phone.recorder", "com.android.cellbroadcastreceiver", TELECOM_PKG};
    private String[] plus_notification_white_list = new String[]{"com.android.bluetooth"};
    private final BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (NotificationManagerService.ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE.equals(action)) {
                    if (intent.getIntExtra(NotificationManagerService.POWER_MODE, 0) == 3) {
                        if (NotificationManagerService.this.mPowerSaverObserver == null) {
                            NotificationManagerService.this.mPowerSaverObserver = new PowerSaverObserver(NotificationManagerService.this.mHandler);
                        }
                        NotificationManagerService.this.mPowerSaverObserver.observe();
                        Log.i(NotificationManagerService.TAG, "super power save 2.0 recevier brodcast register sqlite listener");
                    }
                } else if (NotificationManagerService.ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE.equals(action) && intent.getIntExtra(NotificationManagerService.SHUTDOWN_LIMIT_POWERMODE, 0) == 0 && NotificationManagerService.this.mPowerSaverObserver != null) {
                    NotificationManagerService.this.mPowerSaverObserver.unObserve();
                    NotificationManagerService.this.mPowerSaverObserver = null;
                    Log.i(NotificationManagerService.TAG, "super power save 2.0 recevier brodcast unregister sqlite listener");
                }
            }
        }
    };
    private HashSet<String> power_save_whiteSet = new HashSet();

    private interface FlagChecker {
        boolean apply(int i);
    }

    private static class Archive {
        final ArrayDeque<StatusBarNotification> mBuffer = new ArrayDeque(this.mBufferSize);
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
                int i2 = i + 1;
                a[i] = (StatusBarNotification) iter.next();
                i = i2;
            }
            return a;
        }
    }

    public static final class DumpFilter {
        public boolean filtered = false;
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
                if ("--proto".equals(args[0])) {
                    filter.proto = true;
                }
                if ("--noredact".equals(a) || "--reveal".equals(a)) {
                    filter.redact = false;
                } else if ("p".equals(a) || AbsLocationManagerService.DEL_PKG.equals(a) || "--package".equals(a)) {
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
            if (!this.zen) {
                if (sbn == null) {
                    z = false;
                } else if (!matches(sbn.getPackageName())) {
                    z = matches(sbn.getOpPkg());
                }
            }
            return z;
        }

        public boolean matches(ComponentName component) {
            boolean z = true;
            if (!this.filtered) {
                return true;
            }
            if (!this.zen) {
                z = component != null ? matches(component.getPackageName()) : false;
            }
            return z;
        }

        public boolean matches(String pkg) {
            boolean z = true;
            if (!this.filtered) {
                return true;
            }
            if (!this.zen) {
                z = pkg != null ? pkg.toLowerCase().contains(this.pkgFilter) : false;
            }
            return z;
        }

        public String toString() {
            if (this.stats) {
                return "stats";
            }
            return this.zen ? "zen" : '\'' + this.pkgFilter + '\'';
        }
    }

    protected class EnqueueNotificationRunnable implements Runnable {
        private final NotificationRecord r;
        private final int userId;

        EnqueueNotificationRunnable(int userId, NotificationRecord r) {
            this.userId = userId;
            this.r = r;
        }

        public void run() {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationManagerService.this.mEnqueuedNotifications.add(this.r);
                NotificationManagerService.this.scheduleTimeoutLocked(this.r);
                StatusBarNotification n = this.r.sbn;
                if (NotificationManagerService.DBG) {
                    Slog.d(NotificationManagerService.TAG, "EnqueueNotificationRunnable.run for: " + n.getKey());
                }
                NotificationRecord old = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(n.getKey());
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
                    RemoteViews contentView = notification.contentView;
                    RemoteViews bigContentView = notification.bigContentView;
                    int contentViewSize = 0;
                    if (contentView != null) {
                        contentViewSize = contentView.getCacheSize();
                    }
                    int bigContentViewSize = 0;
                    if (bigContentView != null) {
                        bigContentViewSize = bigContentView.getCacheSize();
                    }
                    EventLogTags.writeNotificationEnqueue(callingUid, callingPid, pkg, id, tag, this.userId, notification.toString() + " contentViewSize = " + contentViewSize + " bigContentViewSize = " + bigContentViewSize + " N = " + NotificationManagerService.this.mNotificationList.size(), enqueueStatus);
                }
                NotificationManagerService.this.mRankingHelper.extractSignals(this.r);
                if (NotificationManagerService.this.mNotificationResource != null) {
                    NotificationManagerService.this.mNotificationResource.release(callingUid, pkg, -1);
                }
                if (NotificationManagerService.this.mNotificationAssistants.isEnabled()) {
                    NotificationManagerService.this.mNotificationAssistants.onNotificationEnqueued(this.r);
                    NotificationManagerService.this.mHandler.postDelayed(new PostNotificationRunnable(this.r.getKey()), NotificationManagerService.DELAY_FOR_ASSISTANT_TIME);
                } else {
                    NotificationManagerService.this.mHandler.post(new PostNotificationRunnable(this.r.getKey()));
                }
            }
        }
    }

    public class NotificationAssistants extends ManagedServices {
        public NotificationAssistants() {
            super(NotificationManagerService.this.getContext(), NotificationManagerService.this.mHandler, NotificationManagerService.this.mNotificationLock, NotificationManagerService.this.mUserProfiles);
        }

        protected Config getConfig() {
            Config c = new Config();
            c.caption = "notification assistant service";
            c.serviceInterface = "android.service.notification.NotificationAssistantService";
            c.secureSettingName = "enabled_notification_assistant";
            c.bindPermission = "android.permission.BIND_NOTIFICATION_ASSISTANT_SERVICE";
            c.settingsAction = "android.settings.MANAGE_DEFAULT_APPS_SETTINGS";
            c.clientLabel = 17040513;
            return c;
        }

        protected IInterface asInterface(IBinder binder) {
            return INotificationListener.Stub.asInterface(binder);
        }

        protected boolean checkType(IInterface service) {
            return service instanceof INotificationListener;
        }

        protected void onServiceAdded(ManagedServiceInfo info) {
            NotificationManagerService.this.mListeners.registerGuestService(info);
        }

        @GuardedBy("mNotificationLock")
        protected void onServiceRemovedLocked(ManagedServiceInfo removed) {
            NotificationManagerService.this.mListeners.unregisterService(removed.service, removed.userid);
        }

        public void onNotificationEnqueued(NotificationRecord r) {
            StatusBarNotification sbn = r.sbn;
            TrimCache trimCache = new TrimCache(sbn);
            for (final ManagedServiceInfo info : getServices()) {
                if (NotificationManagerService.this.isVisibleToListener(sbn, info)) {
                    int importance = r.getImportance();
                    boolean fromUser = r.isImportanceFromUser();
                    final StatusBarNotification sbnToPost = trimCache.ForListener(info);
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationAssistants.this.notifyEnqueued(info, sbnToPost);
                        }
                    });
                }
            }
        }

        private void notifyEnqueued(ManagedServiceInfo info, StatusBarNotification sbn) {
            INotificationListener assistant = info.service;
            try {
                assistant.onNotificationEnqueued(new StatusBarNotificationHolder(sbn));
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify assistant (enqueued): " + assistant, ex);
            }
        }

        @GuardedBy("mNotificationLock")
        public void notifyAssistantSnoozedLocked(StatusBarNotification sbn, final String snoozeCriterionId) {
            TrimCache trimCache = new TrimCache(sbn);
            for (final ManagedServiceInfo info : getServices()) {
                final StatusBarNotification sbnToPost = trimCache.ForListener(info);
                NotificationManagerService.this.mHandler.post(new Runnable() {
                    public void run() {
                        INotificationListener assistant = info.service;
                        try {
                            assistant.onNotificationSnoozedUntilContext(new StatusBarNotificationHolder(sbnToPost), snoozeCriterionId);
                        } catch (RemoteException ex) {
                            Log.e(NotificationAssistants.this.TAG, "unable to notify assistant (snoozed): " + assistant, ex);
                        }
                    }
                });
            }
        }

        public boolean isEnabled() {
            return getServices().isEmpty() ^ 1;
        }
    }

    public class NotificationListeners extends ManagedServices {
        private final ArraySet<ManagedServiceInfo> mLightTrimListeners = new ArraySet();

        public NotificationListeners() {
            super(NotificationManagerService.this.getContext(), NotificationManagerService.this.mHandler, NotificationManagerService.this.mNotificationLock, NotificationManagerService.this.mUserProfiles);
        }

        protected Config getConfig() {
            Config c = new Config();
            c.caption = "notification listener";
            c.serviceInterface = "android.service.notification.NotificationListenerService";
            c.secureSettingName = "enabled_notification_listeners";
            c.bindPermission = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE";
            c.settingsAction = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
            c.clientLabel = 17040511;
            return c;
        }

        protected IInterface asInterface(IBinder binder) {
            return INotificationListener.Stub.asInterface(binder);
        }

        protected boolean checkType(IInterface service) {
            return service instanceof INotificationListener;
        }

        public void onServiceAdded(ManagedServiceInfo info) {
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

        @GuardedBy("mNotificationLock")
        protected void onServiceRemovedLocked(ManagedServiceInfo removed) {
            if (NotificationManagerService.this.removeDisabledHints(removed)) {
                NotificationManagerService.this.updateListenerHintsLocked();
                NotificationManagerService.this.updateEffectsSuppressorLocked();
            }
            this.mLightTrimListeners.remove(removed);
        }

        @GuardedBy("mNotificationLock")
        public void setOnNotificationPostedTrimLocked(ManagedServiceInfo info, int trim) {
            if (trim == 1) {
                this.mLightTrimListeners.add(info);
            } else {
                this.mLightTrimListeners.remove(info);
            }
        }

        public int getOnNotificationPostedTrim(ManagedServiceInfo info) {
            return this.mLightTrimListeners.contains(info) ? 1 : 0;
        }

        @GuardedBy("mNotificationLock")
        public void notifyPostedLocked(StatusBarNotification sbn, StatusBarNotification oldSbn) {
            TrimCache trimCache = new TrimCache(sbn);
            for (final ManagedServiceInfo info : getServices()) {
                boolean sbnVisible = NotificationManagerService.this.isVisibleToListener(sbn, info);
                boolean oldSbnVisible = oldSbn != null ? NotificationManagerService.this.isVisibleToListener(oldSbn, info) : false;
                if (oldSbnVisible || (sbnVisible ^ 1) == 0) {
                    final NotificationRankingUpdate update = NotificationManagerService.this.makeRankingUpdateLocked(info);
                    if (!oldSbnVisible || (sbnVisible ^ 1) == 0) {
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
                                NotificationListeners.this.notifyRemoved(info, oldSbnLightClone, update, 6);
                            }
                        });
                    }
                }
            }
        }

        @GuardedBy("mNotificationLock")
        public void notifyRemovedLocked(StatusBarNotification sbn, int reason) {
            final StatusBarNotification sbnLight = sbn.cloneLight();
            for (final ManagedServiceInfo info : getServices()) {
                if (NotificationManagerService.this.isVisibleToListener(sbn, info)) {
                    final NotificationRankingUpdate update = NotificationManagerService.this.makeRankingUpdateLocked(info);
                    final int i = reason;
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyRemoved(info, sbnLight, update, i);
                        }
                    });
                }
            }
        }

        @GuardedBy("mNotificationLock")
        public void notifyRankingUpdateLocked() {
            for (final ManagedServiceInfo serviceInfo : getServices()) {
                if (serviceInfo.isEnabledForCurrentProfiles()) {
                    final NotificationRankingUpdate update = NotificationManagerService.this.makeRankingUpdateLocked(serviceInfo);
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyRankingUpdate(serviceInfo, update);
                        }
                    });
                }
            }
        }

        @GuardedBy("mNotificationLock")
        public void notifyListenerHintsChangedLocked(final int hints) {
            for (final ManagedServiceInfo serviceInfo : getServices()) {
                if (serviceInfo.isEnabledForCurrentProfiles()) {
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyListenerHintsChanged(serviceInfo, hints);
                        }
                    });
                }
            }
        }

        public void notifyInterruptionFilterChanged(final int interruptionFilter) {
            for (final ManagedServiceInfo serviceInfo : getServices()) {
                if (serviceInfo.isEnabledForCurrentProfiles()) {
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyInterruptionFilterChanged(serviceInfo, interruptionFilter);
                        }
                    });
                }
            }
        }

        protected void notifyNotificationChannelChanged(String pkg, UserHandle user, NotificationChannel channel, int modificationType) {
            if (channel != null) {
                for (final ManagedServiceInfo serviceInfo : getServices()) {
                    if (serviceInfo.enabledAndUserMatches(UserHandle.getCallingUserId())) {
                        final String str = pkg;
                        final UserHandle userHandle = user;
                        final NotificationChannel notificationChannel = channel;
                        final int i = modificationType;
                        NotificationManagerService.this.mHandler.post(new Runnable() {
                            public void run() {
                                if (NotificationManagerService.this.hasCompanionDevice(serviceInfo) || serviceInfo.component.toString().contains(NotificationManagerService.HWSYSTEMMANAGER_PKG)) {
                                    NotificationListeners.this.notifyNotificationChannelChanged(serviceInfo, str, userHandle, notificationChannel, i);
                                }
                            }
                        });
                    }
                }
            }
        }

        protected void notifyNotificationChannelGroupChanged(String pkg, UserHandle user, NotificationChannelGroup group, int modificationType) {
            if (group != null) {
                for (final ManagedServiceInfo serviceInfo : getServices()) {
                    if (serviceInfo.enabledAndUserMatches(UserHandle.getCallingUserId())) {
                        final String str = pkg;
                        final UserHandle userHandle = user;
                        final NotificationChannelGroup notificationChannelGroup = group;
                        final int i = modificationType;
                        NotificationManagerService.this.mHandler.post(new Runnable() {
                            public void run() {
                                if (NotificationManagerService.this.hasCompanionDevice(serviceInfo)) {
                                    NotificationListeners.this.notifyNotificationChannelGroupChanged(serviceInfo, str, userHandle, notificationChannelGroup, i);
                                }
                            }
                        });
                    }
                }
            }
        }

        private void notifyPosted(ManagedServiceInfo info, StatusBarNotification sbn, NotificationRankingUpdate rankingUpdate) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationPosted(new StatusBarNotificationHolder(sbn), rankingUpdate);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify listener (posted): " + listener, ex);
            }
        }

        private void notifyRemoved(ManagedServiceInfo info, StatusBarNotification sbn, NotificationRankingUpdate rankingUpdate, int reason) {
            if (info.enabledAndUserMatches(sbn.getUserId())) {
                INotificationListener listener = info.service;
                try {
                    listener.onNotificationRemoved(new StatusBarNotificationHolder(sbn), rankingUpdate, reason);
                    LogPower.push(123, sbn.getPackageName(), Integer.toString(sbn.getId()), sbn.getOpPkg(), new String[]{Integer.toString(sbn.getNotification().flags)});
                    NotificationManagerService.this.reportToIAware(sbn.getPackageName(), sbn.getUid(), sbn.getId(), false);
                } catch (RemoteException ex) {
                    Log.e(this.TAG, "unable to notify listener (removed): " + listener, ex);
                }
            }
        }

        private void notifyRankingUpdate(ManagedServiceInfo info, NotificationRankingUpdate rankingUpdate) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationRankingUpdate(rankingUpdate);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify listener (ranking update): " + listener, ex);
            }
        }

        private void notifyListenerHintsChanged(ManagedServiceInfo info, int hints) {
            INotificationListener listener = info.service;
            try {
                listener.onListenerHintsChanged(hints);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify listener (listener hints): " + listener, ex);
            }
        }

        private void notifyInterruptionFilterChanged(ManagedServiceInfo info, int interruptionFilter) {
            INotificationListener listener = info.service;
            try {
                listener.onInterruptionFilterChanged(interruptionFilter);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify listener (interruption filter): " + listener, ex);
            }
        }

        void notifyNotificationChannelChanged(ManagedServiceInfo info, String pkg, UserHandle user, NotificationChannel channel, int modificationType) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationChannelModification(pkg, user, channel, modificationType);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify listener (channel changed): " + listener, ex);
            }
        }

        private void notifyNotificationChannelGroupChanged(ManagedServiceInfo info, String pkg, UserHandle user, NotificationChannelGroup group, int modificationType) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationChannelGroupModification(pkg, user, group, modificationType);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify listener (channel group changed): " + listener, ex);
            }
        }

        public boolean isListenerPackage(String packageName) {
            if (packageName == null) {
                return false;
            }
            synchronized (NotificationManagerService.this.mNotificationLock) {
                for (ManagedServiceInfo serviceInfo : getServices()) {
                    if (packageName.equals(serviceInfo.component.getPackageName())) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    private final class PolicyAccess {
        private static final String SEPARATOR = ":";
        private final String[] PERM;

        /* synthetic */ PolicyAccess(NotificationManagerService this$0, PolicyAccess -this1) {
            this();
        }

        private PolicyAccess() {
            this.PERM = new String[]{"android.permission.ACCESS_NOTIFICATION_POLICY"};
        }

        public boolean isPackageGranted(String pkg) {
            return pkg != null ? getGrantedPackages().contains(pkg) : false;
        }

        public void put(String pkg, boolean granted) {
            if (pkg != null) {
                boolean changed;
                ArraySet<String> pkgs = getGrantedPackages();
                if (granted) {
                    changed = pkgs.add(pkg);
                } else {
                    changed = pkgs.remove(pkg);
                }
                if (changed) {
                    String setting = TextUtils.join(SEPARATOR, pkgs);
                    int currentUser = ActivityManager.getCurrentUser();
                    Secure.putStringForUser(NotificationManagerService.this.getContext().getContentResolver(), "enabled_notification_policy_access_packages", setting, currentUser);
                    NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED").setPackage(pkg).addFlags(1073741824), new UserHandle(currentUser), null);
                }
            }
        }

        public ArraySet<String> getGrantedPackages() {
            ArraySet<String> pkgs = new ArraySet();
            long identity = Binder.clearCallingIdentity();
            try {
                String setting = Secure.getStringForUser(NotificationManagerService.this.getContext().getContentResolver(), "enabled_notification_policy_access_packages", ActivityManager.getCurrentUser());
                if (setting != null) {
                    String[] tokens = setting.split(SEPARATOR);
                    for (String token : tokens) {
                        String token2;
                        if (token2 != null) {
                            token2 = token2.trim();
                        }
                        if (!TextUtils.isEmpty(token2)) {
                            pkgs.add(token2);
                        }
                    }
                }
                Binder.restoreCallingIdentity(identity);
                return pkgs;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public String[] getRequestingPackages() throws RemoteException {
            List<PackageInfo> pkgs = NotificationManagerService.this.mPackageManager.getPackagesHoldingPermissions(this.PERM, 0, ActivityManager.getCurrentUser()).getList();
            if (pkgs == null || pkgs.isEmpty()) {
                return new String[0];
            }
            int N = pkgs.size();
            String[] rt = new String[N];
            for (int i = 0; i < N; i++) {
                rt[i] = ((PackageInfo) pkgs.get(i)).packageName;
            }
            return rt;
        }
    }

    protected class PostNotificationRunnable implements Runnable {
        private final String key;

        PostNotificationRunnable(String key) {
            this.key = key;
        }

        /* JADX WARNING: Missing block: B:18:0x00bf, code:
            return;
     */
        /* JADX WARNING: Missing block: B:57:0x034d, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = null;
                int N;
                int i;
                try {
                    N = NotificationManagerService.this.mEnqueuedNotifications.size();
                    for (i = 0; i < N; i++) {
                        NotificationRecord enqueued = (NotificationRecord) NotificationManagerService.this.mEnqueuedNotifications.get(i);
                        if (Objects.equals(this.key, enqueued.getKey())) {
                            r = enqueued;
                            break;
                        }
                    }
                    if (r == null) {
                        Slog.i(NotificationManagerService.TAG, "Cannot find enqueued record for key: " + this.key);
                        N = NotificationManagerService.this.mEnqueuedNotifications.size();
                        for (i = 0; i < N; i++) {
                            if (Objects.equals(this.key, ((NotificationRecord) NotificationManagerService.this.mEnqueuedNotifications.get(i)).getKey())) {
                                NotificationManagerService.this.mEnqueuedNotifications.remove(i);
                                break;
                            }
                        }
                    } else {
                        NotificationRecord old = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(this.key);
                        final StatusBarNotification n = r.sbn;
                        Notification notification = n.getNotification();
                        int index = NotificationManagerService.this.indexOfNotificationLocked(n.getKey());
                        if (index < 0) {
                            NotificationManagerService.this.mNotificationList.add(r);
                            NotificationManagerService.this.mUsageStats.registerPostedByApp(r);
                        } else {
                            old = (NotificationRecord) NotificationManagerService.this.mNotificationList.get(index);
                            NotificationManagerService.this.mNotificationList.set(index, r);
                            NotificationManagerService.this.mUsageStats.registerUpdatedByApp(r, old);
                            notification.flags |= old.getNotification().flags & 64;
                            r.isUpdate = true;
                        }
                        Flog.i(400, "enqueueNotificationInternal: n.getKey = " + n.getKey());
                        NotificationManagerService.this.mNotificationsByKey.put(n.getKey(), r);
                        if ((notification.flags & 64) != 0) {
                            notification.flags |= 34;
                        }
                        NotificationManagerService.this.applyZenModeLocked(r);
                        NotificationManagerService.this.mRankingHelper.sort(NotificationManagerService.this.mNotificationList);
                        final int notifyType = NotificationManagerService.this.calculateNotifyType(r);
                        if (notification.getSmallIcon() != null) {
                            StatusBarNotification oldSbn = old != null ? old.sbn : null;
                            if (n.getNotification().extras != null) {
                                n.getNotification().extras.putBoolean("toSingleLine", SplitNotificationUtils.isNotificationAddSplitButton(n.getPackageName()));
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
                                    Log.e(NotificationManagerService.TAG, "notification.extras:" + e.toString());
                                }
                            }
                            try {
                                NotificationManagerService.this.mListeners.notifyPostedLocked(n, oldSbn);
                                NotificationManagerService.this.mHandler.post(new Runnable() {
                                    public void run() {
                                        NotificationManagerService.this.mGroupHelper.onNotificationPosted(n, notifyType);
                                    }
                                });
                                if (oldSbn == null) {
                                    LogPower.push(122, n.getPackageName(), Integer.toString(n.getId()), n.getOpPkg(), new String[]{Integer.toString(r.getFlags())});
                                    NotificationManagerService.this.reportToIAware(n.getPackageName(), n.getUid(), n.getId(), true);
                                    r.setPushLogPowerTimeMs(System.currentTimeMillis());
                                } else {
                                    long now = System.currentTimeMillis();
                                    if (old.getPushLogPowerTimeMs(now) > NotificationManagerService.NOTIFICATION_UPDATE_REPORT_INTERVAL) {
                                        LogPower.push(HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_MINUS, n.getPackageName(), Integer.toString(n.getId()), n.getOpPkg(), new String[]{Integer.toString(r.getFlags())});
                                        r.setPushLogPowerTimeMs(now);
                                    }
                                }
                            } catch (IllegalArgumentException e2) {
                                Log.e(NotificationManagerService.TAG, "notification:" + n + "extras:" + n.getNotification().extras);
                                N = NotificationManagerService.this.mEnqueuedNotifications.size();
                                for (i = 0; i < N; i++) {
                                    if (Objects.equals(this.key, ((NotificationRecord) NotificationManagerService.this.mEnqueuedNotifications.get(i)).getKey())) {
                                        NotificationManagerService.this.mEnqueuedNotifications.remove(i);
                                        break;
                                    }
                                }
                                return;
                            }
                        }
                        Slog.e(NotificationManagerService.TAG, "Not posting notification without small icon: " + notification);
                        if (!(old == null || (old.isCanceled ^ 1) == 0)) {
                            NotificationManagerService.this.mListeners.notifyRemovedLocked(n, 4);
                            NotificationManagerService.this.mHandler.post(new Runnable() {
                                public void run() {
                                    NotificationManagerService.this.mGroupHelper.onNotificationRemoved(n, notifyType);
                                }
                            });
                        }
                        Slog.e(NotificationManagerService.TAG, "WARNING: In a future release this will crash the app: " + n.getPackageName());
                        NotificationManagerService.this.buzzBeepBlinkLocked(r);
                        N = NotificationManagerService.this.mEnqueuedNotifications.size();
                        for (i = 0; i < N; i++) {
                            if (Objects.equals(this.key, ((NotificationRecord) NotificationManagerService.this.mEnqueuedNotifications.get(i)).getKey())) {
                                NotificationManagerService.this.mEnqueuedNotifications.remove(i);
                                break;
                            }
                        }
                    }
                } catch (Throwable th) {
                    N = NotificationManagerService.this.mEnqueuedNotifications.size();
                    for (i = 0; i < N; i++) {
                        if (Objects.equals(this.key, ((NotificationRecord) NotificationManagerService.this.mEnqueuedNotifications.get(i)).getKey())) {
                            NotificationManagerService.this.mEnqueuedNotifications.remove(i);
                            break;
                        }
                    }
                }
            }
        }
    }

    private final class PowerSaverObserver extends ContentObserver {
        private final Uri SUPER_POWER_SAVE_NOTIFICATION_URI = Secure.getUriFor(NotificationManagerService.POWER_SAVER_NOTIFICATION_WHITELIST);
        private boolean initObserver = false;

        PowerSaverObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            if (!this.initObserver) {
                this.initObserver = true;
                NotificationManagerService.this.getContext().getContentResolver().registerContentObserver(this.SUPER_POWER_SAVE_NOTIFICATION_URI, false, this, -1);
                update(null);
            }
        }

        void unObserve() {
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
                    NotificationManagerService.this.handleRankingSort(msg);
                    return;
                default:
                    return;
            }
        }

        public void requestSort(boolean forceUpdate) {
            removeMessages(1001);
            Message msg = Message.obtain();
            msg.what = 1001;
            msg.obj = Boolean.valueOf(forceUpdate);
            sendMessage(msg);
        }

        public void requestReconsideration(RankingReconsideration recon) {
            sendMessageDelayed(Message.obtain(this, 1000, recon), recon.getDelay(TimeUnit.MILLISECONDS));
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri NOTIFICATION_BADGING_URI = Secure.getUriFor("notification_badging");
        private final Uri NOTIFICATION_LIGHT_PULSE_URI = System.getUriFor("notification_light_pulse");
        private final Uri NOTIFICATION_RATE_LIMIT_URI = Global.getUriFor("max_notification_enqueue_rate");

        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
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
                boolean pulseEnabled = System.getIntForUser(resolver, "notification_light_pulse", 0, -2) != 0;
                if (NotificationManagerService.this.mNotificationPulseEnabled != pulseEnabled) {
                    NotificationManagerService.this.mNotificationPulseEnabled = pulseEnabled;
                    NotificationManagerService.this.updateNotificationPulse();
                }
            }
            if (uri == null || this.NOTIFICATION_RATE_LIMIT_URI.equals(uri)) {
                NotificationManagerService.this.mMaxPackageEnqueueRate = Global.getFloat(resolver, "max_notification_enqueue_rate", NotificationManagerService.this.mMaxPackageEnqueueRate);
            }
            if (uri == null || this.NOTIFICATION_BADGING_URI.equals(uri)) {
                NotificationManagerService.this.mRankingHelper.updateBadgingEnabled();
            }
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

        @GuardedBy("mNotificationLock")
        void snoozeLocked(NotificationRecord r) {
            if (r.sbn.isGroup()) {
                List<NotificationRecord> groupNotifications = NotificationManagerService.this.findGroupNotificationsLocked(r.sbn.getPackageName(), r.sbn.getGroupKey(), r.sbn.getUserId());
                int i;
                if (r.getNotification().isGroupSummary()) {
                    for (i = 0; i < groupNotifications.size(); i++) {
                        snoozeNotificationLocked((NotificationRecord) groupNotifications.get(i));
                    }
                    return;
                } else if (!NotificationManagerService.this.mSummaryByGroupKey.containsKey(r.sbn.getGroupKey())) {
                    snoozeNotificationLocked(r);
                    return;
                } else if (groupNotifications.size() != 2) {
                    snoozeNotificationLocked(r);
                    return;
                } else {
                    for (i = 0; i < groupNotifications.size(); i++) {
                        snoozeNotificationLocked((NotificationRecord) groupNotifications.get(i));
                    }
                    return;
                }
            }
            snoozeNotificationLocked(r);
        }

        @GuardedBy("mNotificationLock")
        void snoozeNotificationLocked(NotificationRecord r) {
            MetricsLogger.action(r.getLogMaker().setCategory(831).setType(2).addTaggedData(832, Integer.valueOf(this.mSnoozeCriterionId == null ? 0 : 1)));
            NotificationManagerService.this.cancelNotificationLocked(r, false, 18, NotificationManagerService.this.removeFromNotificationListsLocked(r));
            NotificationManagerService.this.updateLightsLocked();
            if (this.mSnoozeCriterionId != null) {
                NotificationManagerService.this.mNotificationAssistants.notifyAssistantSnoozedLocked(r.sbn, this.mSnoozeCriterionId);
                NotificationManagerService.this.mSnoozeHelper.snooze(r);
            } else {
                NotificationManagerService.this.mSnoozeHelper.snooze(r, this.mDuration);
            }
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
        final ITransientNotification callback;
        int duration;
        final int pid;
        final String pkg;
        Binder token;

        ToastRecord(int pid, String pkg, ITransientNotification callback, int duration, Binder token) {
            this.pid = pid;
            this.pkg = pkg;
            this.callback = callback;
            this.duration = duration;
            this.token = token;
        }

        void update(int duration) {
            this.duration = duration;
        }

        void dump(PrintWriter pw, String prefix, DumpFilter filter) {
            if (filter == null || (filter.matches(this.pkg) ^ 1) == 0) {
                pw.println(prefix + this);
            }
        }

        public final String toString() {
            return "ToastRecord{" + Integer.toHexString(System.identityHashCode(this)) + " pkg=" + this.pkg + " callback=" + this.callback + " duration=" + this.duration;
        }
    }

    private static class ToastRecordEx extends ToastRecord {
        int displayId;

        ToastRecordEx(int pid, String pkg, ITransientNotification callback, int duration, Binder token, int displayId) {
            super(pid, pkg, callback, duration, token);
            this.displayId = displayId;
        }
    }

    private class TrimCache {
        StatusBarNotification heavy;
        StatusBarNotification sbnClone;
        StatusBarNotification sbnCloneLight;

        TrimCache(StatusBarNotification sbn) {
            this.heavy = sbn;
        }

        StatusBarNotification ForListener(ManagedServiceInfo info) {
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

    private final class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    NotificationManagerService.this.handleTimeout((ToastRecord) msg.obj);
                    return;
                case 3:
                    new Thread() {
                        public void run() {
                            NotificationManagerService.this.handleSavePolicyFile();
                        }
                    }.start();
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
                default:
                    return;
            }
        }
    }

    static {
        boolean z = true;
        if (!Log.HWINFO) {
            if (Log.HWModuleLog) {
                z = Log.isLoggable(TAG, 4);
            } else {
                z = false;
            }
        }
        HWFLOW = z;
    }

    private void readPolicyXml(InputStream stream, boolean forRestore) throws XmlPullParserException, NumberFormatException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(stream, StandardCharsets.UTF_8.name());
        while (parser.next() != 1) {
            this.mZenModeHelper.readXml(parser, forRestore);
            this.mRankingHelper.readXml(parser, forRestore);
        }
    }

    private void loadPolicyFile() {
        if (DBG) {
            Slog.d(TAG, "loadPolicyFile");
        }
        synchronized (this.mPolicyFile) {
            AutoCloseable autoCloseable = null;
            try {
                autoCloseable = this.mPolicyFile.openRead();
                readPolicyXml(autoCloseable, false);
            } catch (FileNotFoundException e) {
            } catch (IOException e2) {
                Log.wtf(TAG, "Unable to read notification policy", e2);
            } catch (NumberFormatException e3) {
                Log.wtf(TAG, "Unable to parse notification policy", e3);
            } catch (XmlPullParserException e4) {
                Log.wtf(TAG, "Unable to parse notification policy", e4);
            } finally {
                IoUtils.closeQuietly(autoCloseable);
            }
        }
    }

    public void savePolicyFile() {
        this.mHandler.removeMessages(3);
        this.mHandler.sendEmptyMessage(3);
    }

    private void handleSavePolicyFile() {
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
                }
            } catch (IOException e3) {
                Slog.w(TAG, "Failed to save policy file", e3);
                return;
            }
        }
        BackupManager.dataChanged(getContext().getPackageName());
        return;
    }

    private void writePolicyXml(OutputStream stream, boolean forBackup) throws IOException {
        XmlSerializer out = new FastXmlSerializer();
        out.setOutput(stream, StandardCharsets.UTF_8.name());
        out.startDocument(null, Boolean.valueOf(true));
        out.startTag(null, TAG_NOTIFICATION_POLICY);
        out.attribute(null, ATTR_VERSION, Integer.toString(1));
        this.mZenModeHelper.writeXml(out, forBackup);
        this.mRankingHelper.writeXml(out, forBackup);
        out.endTag(null, TAG_NOTIFICATION_POLICY);
        out.endDocument();
    }

    private boolean checkNotificationOp(String pkg, int uid) {
        if (this.mAppOps.checkOp(11, uid, pkg) == 0) {
            return isPackageSuspendedForUser(pkg, uid) ^ 1;
        }
        return false;
    }

    @GuardedBy("mNotificationLock")
    private void clearSoundLocked() {
        this.mSoundNotificationKey = null;
        long identity = Binder.clearCallingIdentity();
        try {
            IRingtonePlayer player = this.mAudioManager.getRingtonePlayer();
            if (player != null) {
                player.stopAsync();
            }
            Binder.restoreCallingIdentity(identity);
        } catch (RemoteException e) {
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    @GuardedBy("mNotificationLock")
    private void clearVibrateLocked() {
        this.mVibrateNotificationKey = null;
        long identity = Binder.clearCallingIdentity();
        try {
            this.mVibrator.cancel();
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    @GuardedBy("mNotificationLock")
    private void clearLightsLocked() {
        long token = Binder.clearCallingIdentity();
        try {
            int currentUser = ActivityManager.getCurrentUser();
            int i = this.mLights.size();
            while (i > 0) {
                i--;
                String owner = (String) this.mLights.get(i);
                NotificationRecord ledNotification = (NotificationRecord) this.mNotificationsByKey.get(owner);
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
        } finally {
            Binder.restoreCallingIdentity(token);
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

    public NotificationManagerService(Context context) {
        super(context);
        Notification.processWhitelistToken = WHITELIST_TOKEN;
    }

    void setAudioManager(AudioManager audioMananger) {
        this.mAudioManager = audioMananger;
    }

    void setVibrator(Vibrator vibrator) {
        this.mVibrator = vibrator;
    }

    void setLights(Light light) {
        this.mNotificationLight = light;
        this.mAttentionLight = light;
        this.mNotificationPulseEnabled = true;
    }

    void setScreenOn(boolean on) {
        this.mScreenOn = on;
    }

    int getNotificationRecordCount() {
        int count;
        synchronized (this.mNotificationLock) {
            count = ((this.mNotificationList.size() + this.mNotificationsByKey.size()) + this.mSummaryByGroupKey.size()) + this.mEnqueuedNotifications.size();
            for (NotificationRecord posted : this.mNotificationList) {
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

    void addNotification(NotificationRecord r) {
        this.mNotificationList.add(r);
        this.mNotificationsByKey.put(r.sbn.getKey(), r);
        if (r.sbn.isGroup()) {
            this.mSummaryByGroupKey.put(r.getGroupKey(), r);
        }
    }

    void addEnqueuedNotification(NotificationRecord r) {
        this.mEnqueuedNotifications.add(r);
    }

    NotificationRecord getNotificationRecord(String key) {
        return (NotificationRecord) this.mNotificationsByKey.get(key);
    }

    void setSystemReady(boolean systemReady) {
        this.mSystemReady = systemReady;
    }

    void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    void setFallbackVibrationPattern(long[] vibrationPattern) {
        this.mFallbackVibrationPattern = vibrationPattern;
    }

    void setPackageManager(IPackageManager packageManager) {
        this.mPackageManager = packageManager;
    }

    void setRankingHelper(RankingHelper rankingHelper) {
        this.mRankingHelper = rankingHelper;
    }

    void setIsTelevision(boolean isTelevision) {
        this.mIsTelevision = isTelevision;
    }

    void init(Looper looper, IPackageManager packageManager, PackageManager packageManagerClient, LightsManager lightsManager, NotificationListeners notificationListeners, ICompanionDeviceManager companionManager, SnoozeHelper snoozeHelper, NotificationUsageStats usageStats) {
        String[] extractorNames;
        boolean z;
        Resources resources = getContext().getResources();
        this.mMaxPackageEnqueueRate = Global.getFloat(getContext().getContentResolver(), "max_notification_enqueue_rate", DEFAULT_MAX_NOTIFICATION_ENQUEUE_RATE);
        this.mAm = ActivityManager.getService();
        this.mPackageManager = packageManager;
        this.mPackageManagerClient = packageManagerClient;
        this.mAppOps = (AppOpsManager) getContext().getSystemService("appops");
        this.mVibrator = (Vibrator) getContext().getSystemService("vibrator");
        this.mAppUsageStats = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
        this.mAlarmManager = (AlarmManager) getContext().getSystemService("alarm");
        this.mCompanionManager = companionManager;
        this.mHandler = new WorkerHandler(looper);
        this.mRankingThread.start();
        try {
            extractorNames = resources.getStringArray(17236022);
        } catch (NotFoundException e) {
            extractorNames = new String[0];
        }
        this.mUsageStats = usageStats;
        this.mRankingHandler = new RankingHandlerWorker(this.mRankingThread.getLooper());
        this.mRankingHelper = new RankingHelper(getContext(), this.mPackageManagerClient, this.mRankingHandler, this.mUsageStats, extractorNames);
        this.mConditionProviders = new ConditionProviders(getContext(), this.mHandler, this.mUserProfiles);
        this.mZenModeHelper = new ZenModeHelper(getContext(), this.mHandler.getLooper(), this.mConditionProviders);
        this.mZenModeHelper.addCallback(new Callback() {
            public void onConfigChanged() {
                NotificationManagerService.this.savePolicyFile();
            }

            void onZenModeChanged() {
                NotificationManagerService.this.sendRegisteredOnlyBroadcast("android.app.action.INTERRUPTION_FILTER_CHANGED");
                NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.INTERRUPTION_FILTER_CHANGED_INTERNAL").addFlags(67108864), UserHandle.ALL, "android.permission.MANAGE_NOTIFICATIONS");
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.updateInterruptionFilterLocked();
                }
            }

            void onPolicyChanged() {
                NotificationManagerService.this.sendRegisteredOnlyBroadcast("android.app.action.NOTIFICATION_POLICY_CHANGED");
            }
        });
        this.mSnoozeHelper = snoozeHelper;
        this.mGroupHelper = new GroupHelper(new Callback() {
            public void addAutoGroup(String key) {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.addAutogroupKeyLocked(key);
                }
                NotificationManagerService.this.mRankingHandler.requestSort(false);
            }

            public void removeAutoGroup(String key) {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.removeAutogroupKeyLocked(key);
                }
                NotificationManagerService.this.mRankingHandler.requestSort(false);
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
        this.mPolicyFile = new AtomicFile(new File(new File(Environment.getDataDirectory(), "system"), "notification_policy.xml"));
        loadPolicyFile();
        this.mListeners = notificationListeners;
        this.mNotificationAssistants = new NotificationAssistants();
        this.mStatusBar = (StatusBarManagerInternal) -wrap1(StatusBarManagerInternal.class);
        if (this.mStatusBar != null) {
            this.mStatusBar.setNotificationDelegate(this.mNotificationDelegate);
        }
        this.mNotificationLight = lightsManager.getLight(4);
        this.mAttentionLight = lightsManager.getLight(5);
        this.mFallbackVibrationPattern = getLongArray(resources, 17236021, 17, DEFAULT_VIBRATE_PATTERN);
        this.mUseAttentionLight = resources.getBoolean(17957036);
        if (Global.getInt(getContext().getContentResolver(), "device_provisioned", 0) == 0) {
            this.mDisableNotificationEffects = true;
        }
        this.mZenModeHelper.initZenMode();
        this.mInterruptionFilter = this.mZenModeHelper.getZenModeListenerInterruptionFilter();
        this.mUserProfiles.updateCache(getContext());
        listenForCallState();
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
        pkgFilter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
        getContext().registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.ALL, pkgFilter, null, null);
        IntentFilter suspendedPkgFilter = new IntentFilter();
        suspendedPkgFilter.addAction("android.intent.action.PACKAGES_SUSPENDED");
        getContext().registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.ALL, suspendedPkgFilter, null, null);
        getContext().registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE"), null, null);
        IntentFilter intentFilter = new IntentFilter(ACTION_NOTIFICATION_TIMEOUT);
        intentFilter.addDataScheme(SCHEME_TIMEOUT);
        getContext().registerReceiver(this.mNotificationTimeoutReceiver, intentFilter);
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mArchive = new Archive(resources.getInteger(17694825));
        if (this.mPackageManagerClient.hasSystemFeature("android.software.leanback")) {
            z = true;
        } else {
            z = this.mPackageManagerClient.hasSystemFeature("android.hardware.type.television");
        }
        this.mIsTelevision = z;
    }

    public void onStart() {
        SnoozeHelper snoozeHelper = new SnoozeHelper(getContext(), new Callback() {
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
        init(Looper.myLooper(), AppGlobals.getPackageManager(), getContext().getPackageManager(), (LightsManager) -wrap1(LightsManager.class), new NotificationListeners(), null, snoozeHelper, new NotificationUsageStats(getContext()));
        publishBinderService("notification", this.mService);
        publishLocalService(NotificationManagerInternal.class, this.mInternalService);
    }

    private void sendRegisteredOnlyBroadcast(String action) {
        getContext().sendBroadcastAsUser(new Intent(action).addFlags(1073741824), UserHandle.ALL, null);
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mSystemReady = true;
            this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
            this.mAudioManagerInternal = (AudioManagerInternal) -wrap1(AudioManagerInternal.class);
            this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            this.mZenModeHelper.onSystemReady();
        } else if (phase == 600) {
            this.mSettingsObserver.observe();
            this.mListeners.onBootPhaseAppsCanStart();
            this.mNotificationAssistants.onBootPhaseAppsCanStart();
            this.mConditionProviders.onBootPhaseAppsCanStart();
        }
    }

    @GuardedBy("mNotificationLock")
    private void updateListenerHintsLocked() {
        int hints = calculateHints();
        if (hints != this.mListenerHints) {
            ZenLog.traceListenerHintsChanged(this.mListenerHints, hints, this.mEffectsSuppressors.size());
            this.mListenerHints = hints;
            scheduleListenerHintsChanged(hints);
        }
    }

    @GuardedBy("mNotificationLock")
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

    private void updateNotificationChannelInt(String pkg, int uid, NotificationChannel channel, boolean fromListener) {
        if (channel.getImportance() == 0) {
            cancelAllNotificationsInt(MY_UID, MY_PID, pkg, channel.getId(), 0, 0, true, UserHandle.getUserId(Binder.getCallingUid()), 17, null);
        }
        this.mRankingHelper.updateNotificationChannel(pkg, uid, channel, true);
        NotificationChannel modifiedChannel = this.mRankingHelper.getNotificationChannel(pkg, uid, channel.getId(), false);
        if (!fromListener) {
            this.mListeners.notifyNotificationChannelChanged(pkg, UserHandle.getUserHandleForUid(uid), modifiedChannel, 2);
        }
        synchronized (this.mNotificationLock) {
            for (int i = this.mNotificationList.size() - 1; i >= 0; i--) {
                NotificationRecord r = (NotificationRecord) this.mNotificationList.get(i);
                if (r.sbn.getPackageName().equals(pkg) && r.sbn.getUid() == uid && channel.getId() != null && channel.getId().equals(r.getChannel().getId())) {
                    r.updateNotificationChannel(modifiedChannel);
                }
            }
        }
        this.mRankingHandler.requestSort(true);
        savePolicyFile();
    }

    private ArrayList<ComponentName> getSuppressors() {
        ArrayList<ComponentName> names = new ArrayList();
        for (int i = this.mListenersDisablingEffects.size() - 1; i >= 0; i--) {
            for (ManagedServiceInfo info : (ArraySet) this.mListenersDisablingEffects.valueAt(i)) {
                names.add(info.component);
            }
        }
        return names;
    }

    private boolean removeDisabledHints(ManagedServiceInfo info) {
        return removeDisabledHints(info, 0);
    }

    private boolean removeDisabledHints(ManagedServiceInfo info, int hints) {
        boolean removed = false;
        for (int i = this.mListenersDisablingEffects.size() - 1; i >= 0; i--) {
            int hint = this.mListenersDisablingEffects.keyAt(i);
            ArraySet<ManagedServiceInfo> listeners = (ArraySet) this.mListenersDisablingEffects.valueAt(i);
            if (hints == 0 || (hint & hints) == hint) {
                if (removed) {
                    removed = true;
                } else {
                    removed = listeners.remove(info);
                }
            }
        }
        return removed;
    }

    private void addDisabledHints(ManagedServiceInfo info, int hints) {
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

    private void addDisabledHint(ManagedServiceInfo info, int hint) {
        if (this.mListenersDisablingEffects.indexOfKey(hint) < 0) {
            this.mListenersDisablingEffects.put(hint, new ArraySet());
        }
        ((ArraySet) this.mListenersDisablingEffects.get(hint)).add(info);
    }

    private int calculateHints() {
        int hints = 0;
        for (int i = this.mListenersDisablingEffects.size() - 1; i >= 0; i--) {
            int hint = this.mListenersDisablingEffects.keyAt(i);
            if (!((ArraySet) this.mListenersDisablingEffects.valueAt(i)).isEmpty()) {
                hints |= hint;
            }
        }
        return hints;
    }

    private long calculateSuppressedEffects() {
        int hints = calculateHints();
        long suppressedEffects = 0;
        if ((hints & 1) != 0) {
            suppressedEffects = 3;
        }
        if ((hints & 2) != 0) {
            suppressedEffects |= 1;
        }
        if ((hints & 4) != 0) {
            return suppressedEffects | 2;
        }
        return suppressedEffects;
    }

    @GuardedBy("mNotificationLock")
    private void updateInterruptionFilterLocked() {
        int interruptionFilter = this.mZenModeHelper.getZenModeListenerInterruptionFilter();
        if (interruptionFilter != this.mInterruptionFilter) {
            this.mInterruptionFilter = interruptionFilter;
            scheduleInterruptionFilterChanged(interruptionFilter);
        }
    }

    INotificationManager getBinderService() {
        return Stub.asInterface(this.mService);
    }

    NotificationManagerInternal getInternalService() {
        return this.mInternalService;
    }

    private void applyAdjustment(NotificationRecord n, Adjustment adjustment) {
        if (!(n == null || adjustment.getSignals() == null)) {
            Bundle.setDefusable(adjustment.getSignals(), true);
            ArrayList<String> people = adjustment.getSignals().getStringArrayList("key_people");
            ArrayList<SnoozeCriterion> snoozeCriterionList = adjustment.getSignals().getParcelableArrayList("key_snooze_criteria");
            n.setPeopleOverride(people);
            n.setSnoozeCriteria(snoozeCriterionList);
        }
    }

    @GuardedBy("mNotificationLock")
    private void addAutogroupKeyLocked(String key) {
        NotificationRecord n = (NotificationRecord) this.mNotificationsByKey.get(key);
        if (n != null) {
            String groupKey = this.mGroupHelper.getAutoGroupKey(calculateNotifyType(n), n.sbn.getId());
            if (n.sbn.getOverrideGroupKey() == null) {
                n.sbn.setOverrideGroupKey(groupKey);
                EventLogTags.writeNotificationAutogrouped(key);
            }
        }
    }

    @GuardedBy("mNotificationLock")
    private void removeAutogroupKeyLocked(String key) {
        NotificationRecord n = (NotificationRecord) this.mNotificationsByKey.get(key);
        if (!(n == null || n.sbn.getOverrideGroupKey() == null)) {
            n.sbn.setOverrideGroupKey(null);
            EventLogTags.writeNotificationUnautogrouped(key);
        }
    }

    @GuardedBy("mNotificationLock")
    private void clearAutogroupSummaryLocked(int userId, String pkg, int notifyType) {
        ArrayMap<String, String> summaries = (ArrayMap) this.mAutobundledSummaries.get(Integer.valueOf(userId));
        String notifyKey = this.mGroupHelper.getUnGroupKey(pkg, notifyType);
        if (summaries != null && summaries.containsKey(notifyKey)) {
            NotificationRecord removed = findNotificationByKeyLocked((String) summaries.remove(notifyKey));
            if (removed != null) {
                cancelNotificationLocked(removed, false, 16, removeFromNotificationListsLocked(removed));
            }
        }
    }

    /* JADX WARNING: Missing block: B:35:0x01c5, code:
            if (r21 == null) goto L_0x01f5;
     */
    /* JADX WARNING: Missing block: B:37:0x01e1, code:
            if (checkDisqualifyingFeatures(r34, MY_UID, r21.sbn.getId(), r21.sbn.getTag(), r21) == false) goto L_0x01f5;
     */
    /* JADX WARNING: Missing block: B:38:0x01e3, code:
            r33.mHandler.post(new com.android.server.notification.NotificationManagerService.EnqueueNotificationRunnable(r33, r34, r21));
     */
    /* JADX WARNING: Missing block: B:39:0x01f5, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void createAutoGroupSummary(int userId, String pkg, String triggeringKey) {
        Throwable th;
        NotificationRecord notificationRecord = null;
        synchronized (this.mNotificationLock) {
            try {
                NotificationRecord notificationRecord2 = (NotificationRecord) this.mNotificationsByKey.get(triggeringKey);
                if (notificationRecord2 == null) {
                    return;
                }
                int notifyType = calculateNotifyType(notificationRecord2);
                String notifyKey = this.mGroupHelper.getUnGroupKey(pkg, notifyType);
                StatusBarNotification adjustedSbn = notificationRecord2.sbn;
                userId = adjustedSbn.getUser().getIdentifier();
                ArrayMap<String, String> summaries = (ArrayMap) this.mAutobundledSummaries.get(Integer.valueOf(userId));
                if (summaries == null) {
                    summaries = new ArrayMap();
                }
                this.mAutobundledSummaries.put(Integer.valueOf(userId), summaries);
                if (notificationRecord2.getNotification().getGroup() != null && notificationRecord2.getNotification().getGroup().contains("ranker_group")) {
                } else if (!summaries.containsKey(notifyKey)) {
                    ApplicationInfo appInfo = (ApplicationInfo) adjustedSbn.getNotification().extras.getParcelable("android.appInfo");
                    Bundle extras = new Bundle();
                    extras.putParcelable("android.appInfo", appInfo);
                    String channelId = notificationRecord2.getChannel().getId();
                    String groupKey = this.mGroupHelper.getAutoGroupKey(notifyType, notificationRecord2.sbn.getId());
                    Notification summaryNotification = new Builder(getContext(), channelId).setSmallIcon(adjustedSbn.getNotification().getSmallIcon()).setGroupSummary(true).setGroupAlertBehavior(2).setGroup(groupKey).setFlag(1024, true).setFlag(512, true).setColor(adjustedSbn.getNotification().color).setLocalOnly(true).build();
                    summaryNotification.extras.putAll(extras);
                    Intent appIntent = getContext().getPackageManager().getLaunchIntentForPackage(pkg);
                    if (appIntent != null) {
                        summaryNotification.contentIntent = PendingIntent.getActivityAsUser(getContext(), 0, appIntent, 0, null, UserHandle.of(userId));
                    }
                    StatusBarNotification summarySbn = new StatusBarNotification(adjustedSbn.getPackageName(), adjustedSbn.getOpPkg(), HwBootFail.STAGE_BOOT_SUCCESS, groupKey, adjustedSbn.getUid(), adjustedSbn.getInitialPid(), summaryNotification, adjustedSbn.getUser(), groupKey, System.currentTimeMillis());
                    NotificationRecord notificationRecord3 = new NotificationRecord(getContext(), summarySbn, notificationRecord2.getChannel());
                    try {
                        if (notificationRecord2.getNotification().extras != null && notificationRecord2.getNotification().extras.containsKey("hw_btw")) {
                            notificationRecord3.setImportance(1, "for user");
                            Bundle summaryExtras = notificationRecord3.getNotification().extras;
                            boolean isBtw = notificationRecord2.getNotification().extras.getBoolean("hw_btw");
                            Flog.i(400, "Autogroup summary Notification is btw : " + isBtw);
                            if (summaryExtras != null) {
                                summaryExtras.putBoolean("hw_btw", isBtw);
                            }
                        } else if (notificationRecord2.getImportance() < 2) {
                            notificationRecord3.setImportance(1, "for user");
                        }
                        summaries.put(notifyKey, summarySbn.getKey());
                        notificationRecord = notificationRecord3;
                    } catch (Throwable th2) {
                        th = th2;
                        notificationRecord = notificationRecord3;
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    private String disableNotificationEffects(NotificationRecord record) {
        if (this.mDisableNotificationEffects) {
            return "booleanState";
        }
        if ((this.mListenerHints & 1) != 0) {
            return "listenerHints";
        }
        if (this.mCallState == 0 || (this.mZenModeHelper.isCall(record) ^ 1) == 0) {
            return null;
        }
        return "callState";
    }

    private void dumpJson(PrintWriter pw, DumpFilter filter) {
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
        if (record == null || record.sbn == null) {
            return false;
        }
        boolean isCallEnabled;
        boolean isMmsEnabled = isMmsNotificationEnable(record.sbn.getPackageName());
        if ("callState".equals(disableEffects) && "com.android.systemui".equals(record.sbn.getPackageName())) {
            isCallEnabled = "low_battery".equals(record.sbn.getTag());
        } else {
            isCallEnabled = false;
        }
        if (isMmsEnabled) {
            isCallEnabled = true;
        }
        return isCallEnabled;
    }

    private void dumpProto(FileDescriptor fd, DumpFilter filter) {
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        synchronized (this.mNotificationLock) {
            int i;
            NotificationRecord nr;
            long records = proto.start(2272037699585L);
            int N = this.mNotificationList.size();
            if (N > 0) {
                for (i = 0; i < N; i++) {
                    nr = (NotificationRecord) this.mNotificationList.get(i);
                    if (filter.filtered) {
                        if ((filter.matches(nr.sbn) ^ 1) != 0) {
                        }
                    }
                    nr.dump(proto, filter.redact);
                    proto.write(1168231104514L, 1);
                }
            }
            N = this.mEnqueuedNotifications.size();
            if (N > 0) {
                for (i = 0; i < N; i++) {
                    nr = (NotificationRecord) this.mEnqueuedNotifications.get(i);
                    if (filter.filtered) {
                        if ((filter.matches(nr.sbn) ^ 1) != 0) {
                        }
                    }
                    nr.dump(proto, filter.redact);
                    proto.write(1168231104514L, 0);
                }
            }
            List<NotificationRecord> snoozed = this.mSnoozeHelper.getSnoozed();
            N = snoozed.size();
            if (N > 0) {
                for (i = 0; i < N; i++) {
                    nr = (NotificationRecord) snoozed.get(i);
                    if (filter.filtered) {
                        if ((filter.matches(nr.sbn) ^ 1) != 0) {
                        }
                    }
                    nr.dump(proto, filter.redact);
                    proto.write(1168231104514L, 2);
                }
            }
            proto.end(records);
        }
        long zenLog = proto.start(1172526071810L);
        this.mZenModeHelper.dump(proto);
        for (ComponentName suppressor : this.mEffectsSuppressors) {
            proto.write(2259152797700L, suppressor.toString());
        }
        proto.end(zenLog);
        proto.flush();
    }

    /* JADX WARNING: Removed duplicated region for block: B:68:0x0340  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void dumpImpl(PrintWriter pw, DumpFilter filter) {
        int N;
        int i;
        pw.print("Current Notification Manager state");
        if (filter.filtered) {
            pw.print(" (filtered to ");
            pw.print(filter);
            pw.print(")");
        }
        pw.println(':');
        boolean zenOnly = filter.filtered ? filter.zen : false;
        if (!zenOnly) {
            synchronized (this.mToastQueue) {
                N = this.mToastQueue.size();
                if (N > 0) {
                    pw.println("  Toast Queue:");
                    for (i = 0; i < N; i++) {
                        ((ToastRecord) this.mToastQueue.get(i)).dump(pw, "    ", filter);
                    }
                    pw.println("  ");
                }
            }
        }
        synchronized (this.mNotificationLock) {
            int j;
            if (!zenOnly) {
                NotificationRecord nr;
                N = this.mNotificationList.size();
                if (N > 0) {
                    pw.println("  Notification List:");
                    for (i = 0; i < N; i++) {
                        nr = (NotificationRecord) this.mNotificationList.get(i);
                        if (filter.filtered) {
                            if ((filter.matches(nr.sbn) ^ 1) != 0) {
                                continue;
                            }
                        }
                        nr.dump(pw, "    ", getContext(), filter.redact);
                    }
                    pw.println("  ");
                }
                if (!filter.filtered) {
                    N = this.mLights.size();
                    if (N > 0) {
                        pw.println("  Lights List:");
                        for (i = 0; i < N; i++) {
                            if (i == N - 1) {
                                pw.print("  > ");
                            } else {
                                pw.print("    ");
                            }
                            pw.println((String) this.mLights.get(i));
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
                j = 0;
                while (iter.hasNext()) {
                    StatusBarNotification sbn = (StatusBarNotification) iter.next();
                    if (filter == null || (filter.matches(sbn) ^ 1) == 0) {
                        pw.println("    " + sbn);
                        j++;
                        if (j >= 5) {
                            if (iter.hasNext()) {
                                pw.println("    ...");
                            }
                            if (!zenOnly) {
                                N = this.mEnqueuedNotifications.size();
                                if (N > 0) {
                                    pw.println("  Enqueued Notification List:");
                                    for (i = 0; i < N; i++) {
                                        nr = (NotificationRecord) this.mEnqueuedNotifications.get(i);
                                        if (filter.filtered) {
                                            if ((filter.matches(nr.sbn) ^ 1) != 0) {
                                            }
                                        }
                                        nr.dump(pw, "    ", getContext(), filter.redact);
                                    }
                                    pw.println("  ");
                                }
                                this.mSnoozeHelper.dump(pw, filter);
                            }
                        }
                    }
                }
                if (zenOnly) {
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
                N = this.mListenersDisablingEffects.size();
                for (i = 0; i < N; i++) {
                    int hint = this.mListenersDisablingEffects.keyAt(i);
                    if (i > 0) {
                        pw.print(';');
                    }
                    pw.print("hint[" + hint + "]:");
                    ArraySet<ManagedServiceInfo> listeners = (ArraySet) this.mListenersDisablingEffects.valueAt(i);
                    int listenerSize = listeners.size();
                    for (j = 0; j < listenerSize; j++) {
                        if (i > 0) {
                            pw.print(',');
                        }
                        pw.print(((ManagedServiceInfo) listeners.valueAt(i)).component);
                    }
                }
                pw.println(')');
                pw.println("\n  Notification assistant services:");
                this.mNotificationAssistants.dump(pw, filter);
            }
            if (!filter.filtered || zenOnly) {
                pw.println("\n  Zen Mode:");
                pw.print("    mInterruptionFilter=");
                pw.println(this.mInterruptionFilter);
                this.mZenModeHelper.dump(pw, "    ");
                pw.println("\n  Zen Log:");
                ZenLog.dump(pw, "    ");
            }
            pw.println("\n  Policy access:");
            pw.print("    mPolicyAccess: ");
            pw.println(this.mPolicyAccess);
            pw.println("\n  Condition providers:");
            this.mConditionProviders.dump(pw, filter);
            pw.println("\n  Group summaries:");
            for (Entry<String, NotificationRecord> entry : this.mSummaryByGroupKey.entrySet()) {
                NotificationRecord r = (NotificationRecord) entry.getValue();
                pw.println("    " + ((String) entry.getKey()) + " -> " + r.getKey());
                if (this.mNotificationsByKey.get(r.getKey()) != r) {
                    pw.println("!!!!!!LEAK: Record not found in mNotificationsByKey.");
                    r.dump(pw, "      ", getContext(), filter.redact);
                }
            }
            if (!zenOnly) {
                pw.println("\n  Usage Stats:");
                this.mUsageStats.dump(pw, "    ", filter);
            }
            try {
                pw.println("\n  Banned Packages:");
                ArrayMap<Integer, ArrayList<String>> packageBans = getPackageBans(filter);
                for (Integer userId : packageBans.keySet()) {
                    for (String packageName : (ArrayList) packageBans.get(userId)) {
                        pw.println("    " + userId + ": " + packageName);
                    }
                }
            } catch (NameNotFoundException e) {
            }
        }
    }

    private void setNotificationWhiteList() {
        int i = 0;
        String apps_plus = Secure.getString(getContext().getContentResolver(), POWER_SAVER_NOTIFICATION_WHITELIST);
        Log.i(TAG, "getNotificationWhiteList from db: " + apps_plus);
        this.power_save_whiteSet.clear();
        for (String s : this.plus_notification_white_list) {
            this.power_save_whiteSet.add(s);
        }
        for (String s2 : this.noitfication_white_list) {
            this.power_save_whiteSet.add(s2);
        }
        if (!TextUtils.isEmpty(apps_plus)) {
            String[] split = apps_plus.split(";");
            int length = split.length;
            while (i < length) {
                this.power_save_whiteSet.add(split[i]);
                i++;
            }
        }
    }

    private boolean isNoitficationWhiteApp(String pkg) {
        return this.power_save_whiteSet.contains(pkg);
    }

    private ArrayMap<Integer, ArrayList<String>> getPackageBans(DumpFilter filter) throws NameNotFoundException {
        ArrayMap<Integer, ArrayList<String>> packageBans = new ArrayMap();
        ArrayList<String> packageNames = new ArrayList();
        for (UserInfo user : UserManager.get(getContext()).getUsers()) {
            int userId = user.getUserHandle().getIdentifier();
            PackageManager packageManager = getContext().getPackageManager();
            List<PackageInfo> packages = packageManager.getInstalledPackagesAsUser(0, userId);
            int packageCount = packages.size();
            for (int p = 0; p < packageCount; p++) {
                String packageName = ((PackageInfo) packages.get(p)).packageName;
                if ((filter == null || filter.matches(packageName)) && !checkNotificationOp(packageName, packageManager.getPackageUidAsUser(packageName, userId))) {
                    packageNames.add(packageName);
                }
            }
            if (!packageNames.isEmpty()) {
                packageBans.put(Integer.valueOf(userId), packageNames);
                packageNames = new ArrayList();
            }
        }
        return packageBans;
    }

    void enqueueNotificationInternal(String pkg, String opPkg, int callingUid, int callingPid, String tag, int id, Notification notification, int incomingUserId) {
        if (SystemProperties.getBoolean("sys.super_power_save", false) && (isNoitficationWhiteApp(pkg) ^ 1) != 0) {
            Flog.i(400, "enqueueNotificationInternal  !isNoitficationWhiteApp package=" + pkg);
        } else if (isBlockRideModeNotification(pkg)) {
            Flog.i(400, "enqueueNotificationInternal  !isBlockModeNotification package=" + pkg);
        } else if (isNotificationDisable()) {
            Flog.i(400, "MDM policy is on , enqueueNotificationInternal  !isNotificationDisable package=" + pkg);
        } else {
            Flog.i(400, "enqueueNotificationInternal: pkg=" + pkg + " id=" + id + " notification=" + notification);
            int targetUid = getNCTargetAppUid(opPkg, pkg, callingUid, notification);
            if (targetUid == callingUid) {
                checkCallerIsSystemOrSameApp(pkg);
            } else {
                Slog.i(TAG, "NC " + callingUid + " calling " + targetUid);
            }
            if ((!isUidSystemOrPhone(targetUid) ? "android".equals(pkg) : true) || !HwDeviceManager.disallowOp(33)) {
                int userId = ActivityManager.handleIncomingUser(callingPid, callingUid, incomingUserId, true, false, "enqueueNotification", pkg);
                UserHandle user = new UserHandle(userId);
                if (pkg == null || notification == null) {
                    throw new IllegalArgumentException("null not allowed: pkg=" + pkg + " id=" + id + " notification=" + notification);
                }
                recognize(tag, id, notification, user, pkg, callingUid, callingPid);
                if (notification.extras != null && notification.extras.containsKey("hw_btw")) {
                    if (notification.isGroupSummary()) {
                        CharSequence title = notification.extras.getCharSequence("android.title");
                        CharSequence content = notification.extras.getCharSequence("android.text");
                        RemoteViews remoteV = notification.contentView;
                        RemoteViews bigContentView = notification.bigContentView;
                        RemoteViews headsUpContentView = notification.headsUpContentView;
                        if (remoteV == null && bigContentView == null && headsUpContentView == null && title == null && content == null) {
                            Flog.i(400, "epmty GroupSummary");
                            return;
                        }
                        notification.flags ^= 512;
                    }
                    notification.setGroup(null);
                }
                int notificationUid = resolveNotificationUid(opPkg, targetUid, userId);
                try {
                    int i;
                    PackageManager packageManager = this.mPackageManagerClient;
                    if (userId == -1) {
                        i = 0;
                    } else {
                        i = userId;
                    }
                    Notification.addFieldsFromContext(packageManager.getApplicationInfoAsUser(pkg, 268435456, i), notification);
                    this.mUsageStats.registerEnqueuedByApp(pkg);
                    String channelId = notification.getChannelId();
                    if (this.mIsTelevision && new TvExtender(notification).getChannelId() != null) {
                        channelId = new TvExtender(notification).getChannelId();
                    }
                    NotificationChannel channel = this.mRankingHelper.getNotificationChannel(pkg, notificationUid, channelId, false);
                    if (channel == null) {
                        Log.e(TAG, "No Channel found for pkg=" + pkg + ", channelId=" + channelId + ", id=" + id + ", tag=" + tag + ", opPkg=" + opPkg + ", callingUid=" + callingUid + ", userId=" + userId + ", incomingUserId=" + incomingUserId + ", notificationUid=" + notificationUid + ", notification=" + notification);
                        doChannelWarningToast("Developer warning for package \"" + pkg + "\"\n" + "Failed to post notification on channel \"" + channelId + "\"\n" + "See log for more details");
                        return;
                    }
                    NotificationRecord notificationRecord = new NotificationRecord(getContext(), new StatusBarNotification(pkg, opPkg, id, tag, notificationUid, callingPid, notification, user, null, System.currentTimeMillis()), channel);
                    if ((notification.flags & 64) != 0 && (channel.getUserLockedFields() & 4) == 0 && (notificationRecord.getImportance() == 1 || notificationRecord.getImportance() == 0)) {
                        if (TextUtils.isEmpty(channelId) || "miscellaneous".equals(channelId)) {
                            notificationRecord.setImportance(2, "Bumped for foreground service");
                        } else {
                            channel.setImportance(2);
                            this.mRankingHelper.updateNotificationChannel(pkg, notificationUid, channel, false);
                            notificationRecord.updateNotificationChannel(channel);
                        }
                    }
                    if (checkDisqualifyingFeatures(userId, notificationUid, id, tag, notificationRecord)) {
                        if (notification.allPendingIntents != null) {
                            int intentCount = notification.allPendingIntents.size();
                            if (intentCount > 0) {
                                ActivityManagerInternal am = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
                                long duration = ((LocalService) LocalServices.getService(LocalService.class)).getNotificationWhitelistDuration();
                                for (int i2 = 0; i2 < intentCount; i2++) {
                                    PendingIntent pendingIntent = (PendingIntent) notification.allPendingIntents.valueAt(i2);
                                    if (pendingIntent != null) {
                                        am.setPendingIntentWhitelistDuration(pendingIntent.getTarget(), WHITELIST_TOKEN, duration);
                                    }
                                }
                            }
                        }
                        if (this.mNotificationResource == null) {
                            if (DBG || Log.HWINFO) {
                                Log.i(TAG, " init notification resource");
                            }
                            this.mNotificationResource = HwFrameworkFactory.getHwResource(10);
                        }
                        if (this.mNotificationResource == null || 2 != this.mNotificationResource.acquire(notificationUid, pkg, -1)) {
                            this.mHandler.post(new EnqueueNotificationRunnable(userId, notificationRecord));
                            return;
                        }
                        if (DBG || Log.HWINFO) {
                            Log.i(TAG, " enqueueNotificationInternal dont acquire resource");
                        }
                        return;
                    }
                    return;
                } catch (Throwable e) {
                    Slog.e(TAG, "Cannot create a context for sending app", e);
                    return;
                }
            }
            Flog.i(400, "MDM policy forbid (targetUid : " + targetUid + ", pkg : " + pkg + ") send notification");
        }
    }

    private void doChannelWarningToast(CharSequence toastText) {
        if (Global.getInt(getContext().getContentResolver(), "show_notification_channel_warnings", Build.IS_DEBUGGABLE ? 1 : 0) != 0) {
            Toast.makeText(getContext(), this.mHandler.getLooper(), toastText, 0).show();
        }
    }

    private int resolveNotificationUid(String opPackageName, int callingUid, int userId) {
        if (!(!isCallerSystemOrPhone() || opPackageName == null || ("android".equals(opPackageName) ^ 1) == 0 || (TELECOM_PKG.equals(opPackageName) ^ 1) == 0)) {
            try {
                return getContext().getPackageManager().getPackageUidAsUser(opPackageName, userId);
            } catch (NameNotFoundException e) {
            }
        }
        return callingUid;
    }

    private boolean checkDisqualifyingFeatures(int userId, int callingUid, int id, String tag, NotificationRecord r) {
        String pkg = r.sbn.getPackageName();
        boolean isSystemNotification = !isUidSystemOrPhone(callingUid) ? "android".equals(pkg) : true;
        boolean isNotificationFromListener = this.mListeners.isListenerPackage(pkg);
        if (!(isSystemNotification || (isNotificationFromListener ^ 1) == 0)) {
            synchronized (this.mNotificationLock) {
                if (this.mNotificationsByKey.get(r.sbn.getKey()) != null) {
                    float appEnqueueRate = this.mUsageStats.getAppEnqueueRate(pkg);
                    if (appEnqueueRate > this.mMaxPackageEnqueueRate) {
                        this.mUsageStats.registerOverRateQuota(pkg);
                        long now = SystemClock.elapsedRealtime();
                        if (now - this.mLastOverRateLogTime > MIN_PACKAGE_OVERRATE_LOG_INTERVAL) {
                            Slog.e(TAG, "Package enqueue rate is " + appEnqueueRate + ". Shedding events. package=" + pkg);
                            this.mLastOverRateLogTime = now;
                        }
                        return false;
                    }
                }
                if (!isPushNotification(r.sbn.getOpPkg(), pkg, r.sbn.getNotification()) && isCallerInstantApp(pkg)) {
                    throw new SecurityException("Instant app " + pkg + " cannot create notifications");
                }
                int count = 0;
                int N = this.mNotificationList.size();
                for (int i = 0; i < N; i++) {
                    NotificationRecord existing = (NotificationRecord) this.mNotificationList.get(i);
                    if (existing.sbn.getPackageName().equals(pkg) && existing.sbn.getUserId() == userId) {
                        if (existing.sbn.getId() == id && TextUtils.equals(existing.sbn.getTag(), tag)) {
                            break;
                        }
                        count++;
                        if (count >= 50) {
                            this.mUsageStats.registerOverCountQuota(pkg);
                            Slog.e(TAG, "Package has already posted " + count + " notifications.  Not showing more.  package=" + pkg);
                            return false;
                        }
                    }
                }
            }
        }
        if (this.mSnoozeHelper.isSnoozed(userId, pkg, r.getKey())) {
            MetricsLogger.action(r.getLogMaker().setType(6).setCategory(831));
            if (DBG) {
                Slog.d(TAG, "Ignored enqueue for snoozed notification " + r.getKey());
            }
            this.mSnoozeHelper.update(userId, r);
            savePolicyFile();
            return false;
        }
        if (isBlocked(r, this.mUsageStats)) {
            return false;
        }
        return true;
    }

    protected boolean isBlocked(NotificationRecord r, NotificationUsageStats usageStats) {
        String pkg = r.sbn.getPackageName();
        int callingUid = r.sbn.getUid();
        boolean isPackageSuspended = isPackageSuspendedForUser(pkg, callingUid);
        if (isPackageSuspended) {
            Slog.e(TAG, "Suppressing notification from package due to package suspended by administrator.");
            usageStats.registerSuspendedByAdmin(r);
            return isPackageSuspended;
        }
        boolean isBlocked = !isFromPinNotification(r.getNotification(), pkg) ? this.mRankingHelper.getImportance(pkg, callingUid) != 0 ? r.getChannel().getImportance() == 0 : true : false;
        if (isBlocked) {
            Slog.e(TAG, "Suppressing notification from package by user request.");
            usageStats.registerBlocked(r);
        }
        return isBlocked;
    }

    @GuardedBy("mNotificationLock")
    private void handleGroupedNotificationLocked(NotificationRecord r, NotificationRecord old, int callingUid, int callingPid) {
        StatusBarNotification sbn = r.sbn;
        Notification n = sbn.getNotification();
        if (n.isGroupSummary() && (sbn.isAppGroup() ^ 1) != 0) {
            n.flags &= -513;
        }
        String group = sbn.getGroupKey();
        boolean isSummary = n.isGroupSummary();
        Notification oldN = old != null ? old.sbn.getNotification() : null;
        String oldGroup = old != null ? old.sbn.getGroupKey() : null;
        boolean oldIsSummary = old != null ? oldN.isGroupSummary() : false;
        if (oldIsSummary) {
            NotificationRecord removedSummary = (NotificationRecord) this.mSummaryByGroupKey.remove(oldGroup);
            if (removedSummary != old) {
                Slog.w(TAG, "Removed summary didn't match old notification: old=" + old.getKey() + ", removed=" + (removedSummary != null ? removedSummary.getKey() : "<null>"));
            }
        }
        if (isSummary) {
            this.mSummaryByGroupKey.put(group, r);
        }
        if (!oldIsSummary) {
            return;
        }
        if (!isSummary || (oldGroup.equals(group) ^ 1) != 0) {
            cancelGroupChildrenLocked(old, callingUid, callingPid, null, false);
        }
    }

    @GuardedBy("mNotificationLock")
    void scheduleTimeoutLocked(NotificationRecord record) {
        if (record.getNotification().getTimeoutAfter() > 0) {
            this.mAlarmManager.setExactAndAllowWhileIdle(2, SystemClock.elapsedRealtime() + record.getNotification().getTimeoutAfter(), PendingIntent.getBroadcast(getContext(), 1, new Intent(ACTION_NOTIFICATION_TIMEOUT).setData(new Uri.Builder().scheme(SCHEME_TIMEOUT).appendPath(record.getKey()).build()).addFlags(268435456).putExtra(EXTRA_KEY, record.getKey()), 134217728));
        }
    }

    @GuardedBy("mNotificationLock")
    void buzzBeepBlinkLocked(NotificationRecord record) {
        boolean buzz = false;
        boolean beep = false;
        boolean blink = false;
        Notification notification = record.sbn.getNotification();
        String key = record.getKey();
        boolean canInterrupt = !isFromPinNotification(notification, record.sbn.getPackageName()) ? (!(record.getImportance() >= 3) || (record.isIntercepted() ^ 1) == 0 || (disallowInterrupt(notification) ^ 1) == 0) ? inNonDisturbMode(record.sbn.getPackageName()) : true : false;
        if (DBG || record.isIntercepted()) {
            Slog.v(TAG, "pkg=" + record.sbn.getPackageName() + " canInterrupt=" + canInterrupt + " intercept=" + record.isIntercepted());
        }
        long token = Binder.clearCallingIdentity();
        try {
            int currentUser = ActivityManager.getCurrentUser();
            String disableEffects = disableNotificationEffects(record);
            if (disableEffects != null) {
                ZenLog.traceDisableEffects(record, disableEffects);
            }
            boolean wasBeep = key != null ? key.equals(this.mSoundNotificationKey) : false;
            boolean wasBuzz = key != null ? key.equals(this.mVibrateNotificationKey) : false;
            int hasValidVibrate = 0;
            int i = 0;
            if (isNotificationForCurrentUser(record)) {
                if (!record.isUpdate && record.getImportance() > 1) {
                    sendAccessibilityEvent(notification, record.sbn.getPackageName());
                }
                if ((disableEffects == null || allowNotificationsInCall(disableEffects, record)) && ((record.getUserId() == -1 || record.getUserId() == currentUser || this.mUserProfiles.isCurrentProfile(record.getUserId())) && canInterrupt && (this.mGameDndStatus ^ 1) != 0 && this.mSystemReady && this.mAudioManager != null)) {
                    if (DBG) {
                        Slog.v(TAG, "Interrupting!");
                    }
                    Uri soundUri = record.getSound();
                    boolean hasValidSound = soundUri != null ? Uri.EMPTY.equals(soundUri) ^ 1 : false;
                    boolean isHwSoundAllow = isHwSoundAllow(record.sbn.getPackageName(), record.getChannel().getId(), record.getUserId());
                    boolean isHwFallBackToVibration = !isHwSoundAllow && this.mAudioManager.getRingerModeInternal() == 2;
                    long[] vibration = record.getVibration();
                    if (vibration == null && hasValidSound && (this.mAudioManager.getRingerModeInternal() == 1 || isHwFallBackToVibration)) {
                        vibration = this.mFallbackVibrationPattern;
                    }
                    boolean hasValidVibrate2 = vibration != null;
                    i = hasValidSound ? isHwSoundAllow : 0;
                    hasValidVibrate = hasValidVibrate2 ? isHwVibrateAllow(record.sbn.getPackageName(), record.getChannel().getId(), record.getUserId()) : 0;
                    if (!shouldMuteNotificationLocked(record)) {
                        sendAccessibilityEvent(notification, record.sbn.getPackageName());
                        if (i != 0) {
                            this.mSoundNotificationKey = key;
                            if (this.mInCall) {
                                playInCallNotification();
                                beep = true;
                            } else {
                                beep = playSound(record, soundUri);
                            }
                        }
                        boolean ringerModeSilent = this.mAudioManager.getRingerModeInternal() == 0;
                        if (!(this.mInCall || hasValidVibrate == 0 || (ringerModeSilent ^ 1) == 0)) {
                            this.mVibrateNotificationKey = key;
                            buzz = playVibration(record, vibration, i);
                        }
                    }
                }
            } else if (DBG || Log.HWINFO) {
                String str = TAG;
                StringBuilder append = new StringBuilder().append("disableEffects=").append(disableEffects).append(" canInterrupt=").append(canInterrupt).append(" once update: ");
                boolean z = record.isUpdate && (notification.flags & 8) != 0;
                Slog.v(str, append.append(z).toString());
            }
            if (wasBeep && (i ^ 1) != 0) {
                clearSoundLocked();
            }
            if (wasBuzz && (hasValidVibrate ^ 1) != 0) {
                clearVibrateLocked();
            }
            boolean wasShowLights = this.mLights.remove(key);
            if (record.getLight() != null && canInterrupt && (record.getSuppressedVisualEffects() & 1) == 0) {
                this.mLights.add(key);
                updateLightsLocked();
                if (this.mUseAttentionLight) {
                    this.mAttentionLight.pulse();
                }
                blink = true;
            } else if (wasShowLights) {
                updateLightsLocked();
            }
            if (buzz || beep || blink) {
                MetricsLogger.action(record.getLogMaker().setCategory(199).setType(1).setSubtype((blink ? 4 : 0) | ((buzz ? 1 : 0) | (beep ? 2 : 0))));
                EventLogTags.writeNotificationAlert(key, buzz ? 1 : 0, beep ? 1 : 0, blink ? 1 : 0);
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @GuardedBy("mNotificationLock")
    boolean shouldMuteNotificationLocked(NotificationRecord record) {
        Notification notification = record.getNotification();
        if (record.isUpdate && (notification.flags & 8) != 0) {
            return true;
        }
        if (record.sbn.isGroup()) {
            return notification.suppressAlertingDueToGrouping();
        }
        return false;
    }

    private boolean playSound(NotificationRecord record, Uri soundUri) {
        boolean looping = (record.getNotification().flags & 4) != 0;
        if (!(this.mAudioManager.isAudioFocusExclusive() || this.mAudioManager.getRingerModeInternal() == 1)) {
            long identity = Binder.clearCallingIdentity();
            try {
                IRingtonePlayer player = this.mAudioManager.getRingtonePlayer();
                if (player != null) {
                    if (DBG) {
                        Slog.v(TAG, "Playing sound " + soundUri + " with attributes " + record.getAudioAttributes());
                    }
                    player.playAsync(soundUri, record.sbn.getUser(), looping, record.getAudioAttributes());
                    return true;
                }
                Binder.restoreCallingIdentity(identity);
            } catch (RemoteException e) {
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
        return false;
    }

    private boolean playVibration(NotificationRecord record, long[] vibration, boolean delayVibForSound) {
        long identity = Binder.clearCallingIdentity();
        try {
            VibrationEffect effect = VibrationEffect.createWaveform(vibration, (record.getNotification().flags & 4) != 0 ? 0 : -1);
            if (delayVibForSound) {
                new Thread(new com.android.server.notification.-$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA.AnonymousClass1(this, record, effect)).start();
            } else {
                this.mVibrator.vibrate(record.sbn.getUid(), record.sbn.getOpPkg(), effect, record.getAudioAttributes());
            }
            Binder.restoreCallingIdentity(identity);
            return true;
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "Error creating vibration waveform with pattern: " + Arrays.toString(vibration));
            Binder.restoreCallingIdentity(identity);
            return false;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    /* synthetic */ void lambda$-com_android_server_notification_NotificationManagerService_218944(NotificationRecord record, VibrationEffect effect) {
        int waitMs = this.mAudioManager.getFocusRampTimeMs(3, record.getAudioAttributes());
        if (DBG) {
            Slog.v(TAG, "Delaying vibration by " + waitMs + "ms");
        }
        try {
            Thread.sleep((long) waitMs);
        } catch (InterruptedException e) {
        }
        this.mVibrator.vibrate(record.sbn.getUid(), record.sbn.getOpPkg(), effect, record.getAudioAttributes());
    }

    private boolean isNotificationForCurrentUser(NotificationRecord record) {
        long token = Binder.clearCallingIdentity();
        try {
            int currentUser = ActivityManager.getCurrentUser();
            if (record.getUserId() == -1 || record.getUserId() == currentUser) {
                return true;
            }
            return this.mUserProfiles.isCurrentProfile(record.getUserId());
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void playInCallNotification() {
        new Thread() {
            public void run() {
                try {
                    synchronized (NotificationManagerService.this.mInCallToneGeneratorLock) {
                        if (NotificationManagerService.this.mInCallToneGenerator != null) {
                            NotificationManagerService.this.mInCallToneGenerator.startTone(28, 1000);
                        }
                    }
                } catch (RuntimeException e) {
                    Log.w(NotificationManagerService.TAG, "Exception from ToneGenerator: " + e);
                }
            }
        }.start();
    }

    @GuardedBy("mToastQueue")
    void showNextToastLocked() {
        ToastRecord record = (ToastRecord) this.mToastQueue.get(0);
        while (record != null) {
            Flog.i(400, "Show pkg=" + record.pkg + " callback=" + record.callback);
            try {
                record.callback.show(record.token);
                scheduleTimeoutLocked(record);
                return;
            } catch (RemoteException e) {
                Slog.w(TAG, "Object died trying to show notification " + record.callback + " in package " + record.pkg);
                int index = this.mToastQueue.indexOf(record);
                if (index >= 0) {
                    this.mToastQueue.remove(index);
                }
                keepProcessAliveIfNeededLocked(record.pid);
                if (this.mToastQueue.size() > 0) {
                    record = (ToastRecord) this.mToastQueue.get(0);
                } else {
                    record = null;
                }
            }
        }
    }

    @GuardedBy("mToastQueue")
    void cancelToastLocked(int index) {
        ToastRecord record = (ToastRecord) this.mToastQueue.get(index);
        try {
            record.callback.hide();
        } catch (RemoteException e) {
            Slog.w(TAG, "Object died trying to hide notification " + record.callback + " in package " + record.pkg);
        }
        ToastRecord lastToast = (ToastRecord) this.mToastQueue.remove(index);
        if (HwPCUtils.isPcCastModeInServer() && (lastToast instanceof ToastRecordEx)) {
            this.mWindowManagerInternal.removeWindowToken(lastToast.token, true, ((ToastRecordEx) lastToast).displayId);
        } else {
            this.mWindowManagerInternal.removeWindowToken(lastToast.token, true, 0);
        }
        keepProcessAliveIfNeededLocked(record.pid);
        if (this.mToastQueue.size() > 0) {
            showNextToastLocked();
        }
    }

    @GuardedBy("mToastQueue")
    private void scheduleTimeoutLocked(ToastRecord r) {
        this.mHandler.removeCallbacksAndMessages(r);
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 2, r), (long) (r.duration == 1 ? 3500 : 2000));
    }

    private void handleTimeout(ToastRecord record) {
        Flog.i(400, "Timeout pkg=" + record.pkg + " callback=" + record.callback);
        synchronized (this.mToastQueue) {
            int index = indexOfToastLocked(record.pkg, record.callback);
            if (index >= 0) {
                cancelToastLocked(index);
            }
        }
    }

    @GuardedBy("mToastQueue")
    int indexOfToastLocked(String pkg, ITransientNotification callback) {
        IBinder cbak = callback.asBinder();
        ArrayList<ToastRecord> list = this.mToastQueue;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            ToastRecord r = (ToastRecord) list.get(i);
            if (r.pkg.equals(pkg) && r.callback.asBinder() == cbak) {
                return i;
            }
        }
        return -1;
    }

    @GuardedBy("mToastQueue")
    void keepProcessAliveIfNeededLocked(int pid) {
        boolean z = false;
        int toastCount = 0;
        ArrayList<ToastRecord> list = this.mToastQueue;
        int N = list.size();
        for (int i = 0; i < N; i++) {
            if (((ToastRecord) list.get(i)).pid == pid) {
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

    /* JADX WARNING: Missing block: B:20:0x0055, code:
            if (r0 == false) goto L_0x005a;
     */
    /* JADX WARNING: Missing block: B:21:0x0057, code:
            scheduleSendRankingUpdate();
     */
    /* JADX WARNING: Missing block: B:22:0x005a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleRankingReconsideration(Message message) {
        if (message.obj instanceof RankingReconsideration) {
            RankingReconsideration recon = message.obj;
            recon.run();
            synchronized (this.mNotificationLock) {
                NotificationRecord record = (NotificationRecord) this.mNotificationsByKey.get(recon.getKey());
                if (record == null) {
                    return;
                }
                int indexBefore = findNotificationRecordIndexLocked(record);
                boolean interceptBefore = record.isIntercepted();
                int visibilityBefore = record.getPackageVisibilityOverride();
                recon.applyChangesLocked(record);
                applyZenModeLocked(record);
                this.mRankingHelper.sort(this.mNotificationList);
                int indexAfter = findNotificationRecordIndexLocked(record);
                boolean interceptAfter = record.isIntercepted();
                boolean changed = (indexBefore == indexAfter && interceptBefore == interceptAfter) ? visibilityBefore != record.getPackageVisibilityOverride() : true;
                if (interceptBefore && (interceptAfter ^ 1) != 0) {
                    buzzBeepBlinkLocked(record);
                }
            }
        }
    }

    private void handleRankingSort(Message msg) {
        if ((msg.obj instanceof Boolean) && this.mRankingHelper != null) {
            boolean forceUpdate = ((Boolean) msg.obj) == null ? false : ((Boolean) msg.obj).booleanValue();
            synchronized (this.mNotificationLock) {
                int i;
                NotificationRecord r;
                int N = this.mNotificationList.size();
                ArrayList<String> orderBefore = new ArrayList(N);
                ArrayList<String> groupOverrideBefore = new ArrayList(N);
                int[] visibilities = new int[N];
                boolean[] showBadges = new boolean[N];
                for (i = 0; i < N; i++) {
                    r = (NotificationRecord) this.mNotificationList.get(i);
                    orderBefore.add(r.getKey());
                    groupOverrideBefore.add(r.sbn.getGroupKey());
                    visibilities[i] = r.getPackageVisibilityOverride();
                    showBadges[i] = r.canShowBadge();
                    this.mRankingHelper.extractSignals(r);
                }
                this.mRankingHelper.sort(this.mNotificationList);
                i = 0;
                while (i < N) {
                    r = (NotificationRecord) this.mNotificationList.get(i);
                    if (!forceUpdate && (((String) orderBefore.get(i)).equals(r.getKey()) ^ 1) == 0 && visibilities[i] == r.getPackageVisibilityOverride()) {
                        if ((((String) groupOverrideBefore.get(i)).equals(r.sbn.getGroupKey()) ^ 1) == 0 && showBadges[i] == r.canShowBadge()) {
                            i++;
                        }
                    }
                    scheduleSendRankingUpdate();
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

    @GuardedBy("mNotificationLock")
    private void applyZenModeLocked(NotificationRecord record) {
        int i = 0;
        record.setIntercepted(this.mZenModeHelper.shouldIntercept(record));
        if (record.isIntercepted()) {
            int i2 = this.mZenModeHelper.shouldSuppressWhenScreenOff() ? 1 : 0;
            if (this.mZenModeHelper.shouldSuppressWhenScreenOn()) {
                i = 2;
            }
            record.setSuppressedVisualEffects(i2 | i);
            return;
        }
        record.setSuppressedVisualEffects(0);
    }

    @GuardedBy("mNotificationLock")
    private int findNotificationRecordIndexLocked(NotificationRecord target) {
        return this.mRankingHelper.indexOf(this.mNotificationList, target);
    }

    private void scheduleSendRankingUpdate() {
        if (!this.mHandler.hasMessages(4)) {
            this.mHandler.sendMessage(Message.obtain(this.mHandler, 4));
        }
    }

    private void handleSendRankingUpdate() {
        synchronized (this.mNotificationLock) {
            this.mListeners.notifyRankingUpdateLocked();
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

    private void handleListenerHintsChanged(int hints) {
        synchronized (this.mNotificationLock) {
            this.mListeners.notifyListenerHintsChangedLocked(hints);
        }
    }

    private void handleListenerInterruptionFilterChanged(int interruptionFilter) {
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

    void sendAccessibilityEvent(Notification notification, CharSequence packageName) {
        AccessibilityManager manager = AccessibilityManager.getInstance(getContext());
        if (manager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(64);
            event.setPackageName(packageName);
            event.setClassName(Notification.class.getName());
            event.setParcelableData(notification);
            CharSequence tickerText = notification.tickerText;
            if (!TextUtils.isEmpty(tickerText)) {
                event.getText().add(tickerText);
            }
            manager.sendAccessibilityEvent(event);
        }
    }

    @GuardedBy("mNotificationLock")
    private boolean removeFromNotificationListsLocked(NotificationRecord r) {
        boolean wasPosted = false;
        NotificationRecord recordInList = findNotificationByListLocked(this.mNotificationList, r.getKey());
        if (recordInList != null) {
            this.mNotificationList.remove(recordInList);
            this.mNotificationsByKey.remove(recordInList.sbn.getKey());
            wasPosted = true;
        }
        while (true) {
            recordInList = findNotificationByListLocked(this.mEnqueuedNotifications, r.getKey());
            if (recordInList == null) {
                return wasPosted;
            }
            this.mEnqueuedNotifications.remove(recordInList);
        }
    }

    @GuardedBy("mNotificationLock")
    private void cancelNotificationLocked(NotificationRecord r, boolean sendDelete, int reason, boolean wasPosted) {
        String canceledKey = r.getKey();
        recordCallerLocked(r);
        Flog.i(400, "cancelNotificationLocked called,tell the app,reason = " + reason);
        if (sendDelete && r.getNotification().deleteIntent != null) {
            try {
                r.getNotification().deleteIntent.send();
            } catch (CanceledException ex) {
                Slog.w(TAG, "canceled PendingIntent for " + r.sbn.getPackageName(), ex);
            }
        }
        final int notifyType = calculateNotifyType(r);
        if (wasPosted) {
            long identity;
            if (r.getNotification().getSmallIcon() != null) {
                if (reason != 18) {
                    r.isCanceled = true;
                }
                Flog.i(400, "cancelNotificationLocked:" + r.sbn.getKey());
                this.mListeners.notifyRemovedLocked(r.sbn, reason);
                final NotificationRecord notificationRecord = r;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        NotificationManagerService.this.mGroupHelper.onNotificationRemoved(notificationRecord.sbn, notifyType);
                    }
                });
            }
            if (canceledKey.equals(this.mSoundNotificationKey)) {
                this.mSoundNotificationKey = null;
                identity = Binder.clearCallingIdentity();
                try {
                    IRingtonePlayer player = this.mAudioManager.getRingtonePlayer();
                    if (player != null) {
                        player.stopAsync();
                    }
                    Binder.restoreCallingIdentity(identity);
                } catch (RemoteException e) {
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            }
            if (canceledKey.equals(this.mVibrateNotificationKey)) {
                this.mVibrateNotificationKey = null;
                identity = Binder.clearCallingIdentity();
                try {
                    this.mVibrator.cancel();
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th2) {
                    Binder.restoreCallingIdentity(identity);
                    throw th2;
                }
            }
            this.mLights.remove(canceledKey);
        }
        switch (reason) {
            case 2:
            case 3:
            case 10:
            case 11:
                this.mUsageStats.registerDismissedByUser(r);
                break;
            case 8:
            case 9:
                this.mUsageStats.registerRemovedByApp(r);
                break;
        }
        Flog.i(400, "cancelNotificationLocked,remove =" + r.sbn.getPackageName() + ", key:" + r.getKey());
        String groupKey = r.getGroupKey();
        NotificationRecord groupSummary = (NotificationRecord) this.mSummaryByGroupKey.get(groupKey);
        if (groupSummary != null && groupSummary.getKey().equals(canceledKey)) {
            this.mSummaryByGroupKey.remove(groupKey);
        }
        ArrayMap<String, String> summaries = (ArrayMap) this.mAutobundledSummaries.get(Integer.valueOf(r.sbn.getUserId()));
        String notifyKey = this.mGroupHelper.getUnGroupKey(r.sbn.getPackageName(), notifyType);
        if (summaries != null && r.sbn.getKey().equals(summaries.get(notifyKey))) {
            summaries.remove(notifyKey);
        }
        this.mArchive.record(r.sbn);
        long now = System.currentTimeMillis();
        MetricsLogger.action(r.getLogMaker(now).setCategory(128).setType(5).setSubtype(reason));
        EventLogTags.writeNotificationCanceled(canceledKey, reason, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now));
    }

    void cancelNotification(int callingUid, int callingPid, String pkg, String tag, int id, int mustHaveFlags, int mustNotHaveFlags, boolean sendDelete, int userId, int reason, ManagedServiceInfo listener) {
        final ManagedServiceInfo managedServiceInfo = listener;
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
        this.mHandler.post(new Runnable() {
            /* JADX WARNING: Missing block: B:25:0x00aa, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                String listenerName = managedServiceInfo == null ? null : managedServiceInfo.component.toShortString();
                EventLogTags.writeNotificationCancel(i, i2, str, i3, str2, i4, i5, i6, i7, listenerName);
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationRecord r = NotificationManagerService.this.findNotificationLocked(str, str2, i3, i4);
                    if (r != null) {
                        if (i7 == 1) {
                            NotificationManagerService.this.mUsageStats.registerClickedByUser(r);
                        }
                        if ((r.getNotification().flags & i5) != i5) {
                        } else if ((r.getNotification().flags & i6) != 0) {
                        } else {
                            Flog.i(400, "cancelNotification,cancelNotificationLocked,callingUid = " + i + ",callingPid = " + i2);
                            NotificationManagerService.this.cancelNotificationLocked(r, z, i7, NotificationManagerService.this.removeFromNotificationListsLocked(r));
                            NotificationManagerService.this.cancelGroupChildrenLocked(r, i, i2, listenerName, z);
                            NotificationManagerService.this.updateLightsLocked();
                        }
                    } else {
                        Flog.i(400, "cancelNotification,r: null");
                        if (i7 != 18 && NotificationManagerService.this.mSnoozeHelper.cancel(i4, str, str2, i3)) {
                            NotificationManagerService.this.savePolicyFile();
                        }
                    }
                }
            }
        });
    }

    private boolean notificationMatchesUserId(NotificationRecord r, int userId) {
        if (userId == -1 || r.getUserId() == -1 || r.getUserId() == userId) {
            return true;
        }
        return false;
    }

    private boolean notificationMatchesCurrentProfiles(NotificationRecord r, int userId) {
        if (notificationMatchesUserId(r, userId)) {
            return true;
        }
        return this.mUserProfiles.isCurrentProfile(r.getUserId());
    }

    void cancelAllNotificationsInt(int callingUid, int callingPid, String pkg, String channelId, int mustHaveFlags, int mustNotHaveFlags, boolean doit, int userId, int reason, ManagedServiceInfo listener) {
        final ManagedServiceInfo managedServiceInfo = listener;
        final int i = callingUid;
        final int i2 = callingPid;
        final String str = pkg;
        final int i3 = userId;
        final int i4 = mustHaveFlags;
        final int i5 = mustNotHaveFlags;
        final int i6 = reason;
        final boolean z = doit;
        final String str2 = channelId;
        this.mHandler.post(new Runnable() {
            public void run() {
                String listenerName = managedServiceInfo == null ? null : managedServiceInfo.component.toShortString();
                EventLogTags.writeNotificationCancelAll(i, i2, str, i3, i4, i5, i6, listenerName);
                if (z) {
                    synchronized (NotificationManagerService.this.mNotificationLock) {
                        Flog.i(400, "cancelAllNotificationsInt called");
                        FlagChecker anonymousClass2 = new com.android.server.notification.-$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA.AnonymousClass2(i4, i5);
                        NotificationManagerService.this.cancelAllNotificationsByListLocked(NotificationManagerService.this.mNotificationList, i, i2, str, true, str2, anonymousClass2, false, i3, false, i6, listenerName, true);
                        NotificationManagerService.this.cancelAllNotificationsByListLocked(NotificationManagerService.this.mEnqueuedNotifications, i, i2, str, true, str2, anonymousClass2, false, i3, false, i6, listenerName, false);
                        NotificationManagerService.this.mSnoozeHelper.cancel(i3, str);
                    }
                }
            }

            static /* synthetic */ boolean lambda$-com_android_server_notification_NotificationManagerService$14_247583(int mustHaveFlags, int mustNotHaveFlags, int flags) {
                if ((flags & mustHaveFlags) == mustHaveFlags && (flags & mustNotHaveFlags) == 0) {
                    return true;
                }
                return false;
            }
        });
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x004f  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0063  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @GuardedBy("mNotificationLock")
    private void cancelAllNotificationsByListLocked(ArrayList<NotificationRecord> notificationList, int callingUid, int callingPid, String pkg, boolean nullPkgIndicatesUserSwitch, String channelId, FlagChecker flagChecker, boolean includeCurrentProfiles, int userId, boolean sendDelete, int reason, String listenerName, boolean wasPosted) {
        int i;
        ArrayList canceledNotifications = null;
        for (i = notificationList.size() - 1; i >= 0; i--) {
            NotificationRecord r = (NotificationRecord) notificationList.get(i);
            if (includeCurrentProfiles) {
                if (!notificationMatchesCurrentProfiles(r, userId)) {
                }
                if (nullPkgIndicatesUserSwitch || pkg != null || r.getUserId() != -1) {
                    if (flagChecker.apply(r.getFlags()) && (pkg == null || (r.sbn.getPackageName().equals(pkg) ^ 1) == 0)) {
                        if (channelId != null) {
                            if ((channelId.equals(r.getChannel().getId()) ^ 1) != 0) {
                            }
                        }
                        if (canceledNotifications == null) {
                            canceledNotifications = new ArrayList();
                        }
                        Flog.i(400, "cancelAllNotificationsInt:" + r.sbn.getKey());
                        notificationList.remove(i);
                        this.mNotificationsByKey.remove(r.getKey());
                        canceledNotifications.add(r);
                        cancelNotificationLocked(r, sendDelete, reason, wasPosted);
                    }
                }
            } else {
                if (!notificationMatchesUserId(r, userId)) {
                }
                if (nullPkgIndicatesUserSwitch) {
                }
                if (channelId != null) {
                }
                if (canceledNotifications == null) {
                }
                Flog.i(400, "cancelAllNotificationsInt:" + r.sbn.getKey());
                notificationList.remove(i);
                this.mNotificationsByKey.remove(r.getKey());
                canceledNotifications.add(r);
                cancelNotificationLocked(r, sendDelete, reason, wasPosted);
            }
        }
        if (canceledNotifications != null) {
            int M = canceledNotifications.size();
            for (i = 0; i < M; i++) {
                cancelGroupChildrenLocked((NotificationRecord) canceledNotifications.get(i), callingUid, callingPid, listenerName, false);
            }
            updateLightsLocked();
        }
    }

    void snoozeNotificationInt(String key, long duration, String snoozeCriterionId, ManagedServiceInfo listener) {
        String listenerName = listener == null ? null : listener.component.toShortString();
        if ((duration > 0 || snoozeCriterionId != null) && key != null) {
            if (DBG) {
                Slog.d(TAG, String.format("snooze event(%s, %d, %s, %s)", new Object[]{key, Long.valueOf(duration), snoozeCriterionId, listenerName}));
            }
            this.mHandler.post(new SnoozeNotificationRunnable(key, duration, snoozeCriterionId));
        }
    }

    void unsnoozeNotificationInt(String key, ManagedServiceInfo listener) {
        String listenerName = listener == null ? null : listener.component.toShortString();
        if (DBG) {
            Slog.d(TAG, String.format("unsnooze event(%s, %s)", new Object[]{key, listenerName}));
        }
        this.mSnoozeHelper.repost(key);
        savePolicyFile();
    }

    @GuardedBy("mNotificationLock")
    void cancelAllLocked(int callingUid, int callingPid, int userId, int reason, ManagedServiceInfo listener, boolean includeCurrentProfiles) {
        final ManagedServiceInfo managedServiceInfo = listener;
        final int i = callingUid;
        final int i2 = callingPid;
        final int i3 = userId;
        final int i4 = reason;
        final boolean z = includeCurrentProfiles;
        this.mHandler.post(new Runnable() {
            public void run() {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    String listenerName = managedServiceInfo == null ? null : managedServiceInfo.component.toShortString();
                    EventLogTags.writeNotificationCancelAll(i, i2, null, i3, 0, 0, i4, listenerName);
                    Flog.i(400, "cancelAllLocked called,callingUid = " + i + ",callingPid = " + i2);
                    FlagChecker flagChecker = new -$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA();
                    NotificationManagerService.this.cancelAllNotificationsByListLocked(NotificationManagerService.this.mNotificationList, i, i2, null, false, null, flagChecker, z, i3, true, i4, listenerName, true);
                    NotificationManagerService.this.cancelAllNotificationsByListLocked(NotificationManagerService.this.mEnqueuedNotifications, i, i2, null, false, null, flagChecker, z, i3, true, i4, listenerName, false);
                    NotificationManagerService.this.mSnoozeHelper.cancel(i3, z);
                }
            }

            static /* synthetic */ boolean lambda$-com_android_server_notification_NotificationManagerService$15_253356(int flags) {
                if ((flags & 34) != 0) {
                    return false;
                }
                return true;
            }
        });
    }

    @GuardedBy("mNotificationLock")
    private void cancelGroupChildrenLocked(NotificationRecord r, int callingUid, int callingPid, String listenerName, boolean sendDelete) {
        if (!r.getNotification().isGroupSummary()) {
            return;
        }
        if (r.sbn.getPackageName() == null) {
            Flog.e(400, "No package for group summary: " + r.getKey());
            return;
        }
        cancelGroupChildrenByListLocked(this.mNotificationList, r, callingUid, callingPid, listenerName, sendDelete, true);
        cancelGroupChildrenByListLocked(this.mEnqueuedNotifications, r, callingUid, callingPid, listenerName, sendDelete, false);
    }

    @GuardedBy("mNotificationLock")
    private void cancelGroupChildrenByListLocked(ArrayList<NotificationRecord> notificationList, NotificationRecord parentNotification, int callingUid, int callingPid, String listenerName, boolean sendDelete, boolean wasPosted) {
        String pkg = parentNotification.sbn.getPackageName();
        int userId = parentNotification.getUserId();
        for (int i = notificationList.size() - 1; i >= 0; i--) {
            NotificationRecord childR = (NotificationRecord) notificationList.get(i);
            StatusBarNotification childSbn = childR.sbn;
            if (childSbn.isGroup() && (childSbn.getNotification().isGroupSummary() ^ 1) != 0 && childR.getGroupKey().equals(parentNotification.getGroupKey()) && (childR.getFlags() & 98) == 0) {
                EventLogTags.writeNotificationCancel(callingUid, callingPid, pkg, childSbn.getId(), childSbn.getTag(), userId, 0, 0, 12, listenerName);
                notificationList.remove(i);
                this.mNotificationsByKey.remove(childR.getKey());
                cancelNotificationLocked(childR, sendDelete, 12, wasPosted);
            }
        }
    }

    @GuardedBy("mNotificationLock")
    void updateLightsLocked() {
        NotificationRecord ledNotification = null;
        int i = this.mLights.size();
        long token = Binder.clearCallingIdentity();
        try {
            int currentUser = ActivityManager.getCurrentUser();
            while (ledNotification == null && i > 0) {
                i--;
                String owner = (String) this.mLights.get(i);
                ledNotification = (NotificationRecord) this.mNotificationsByKey.get(owner);
                if (ledNotification == null) {
                    Slog.wtfStack(TAG, "LED Notification does not exist: " + owner);
                    this.mLights.remove(owner);
                } else if (!(ledNotification.getUser().getIdentifier() == currentUser || ledNotification.getUser().getIdentifier() == -1 || (isAFWUserId(ledNotification.getUser().getIdentifier()) ^ 1) == 0)) {
                    Slog.d(TAG, "ledNotification is not CurrentUser,AFWuser and AllUser:" + owner);
                    ledNotification = null;
                }
            }
            Flog.i(400, "updateLightsLocked,mInCall =" + this.mInCall + ",mScreenOn = " + this.mScreenOn + ",mGameDndStatus = " + this.mGameDndStatus + ",ledNotification == null?" + (ledNotification == null));
            if (ledNotification == null || this.mInCall || (this.mScreenOn && (this.mGameDndStatus ^ 1) != 0)) {
                Flog.i(400, "updateLightsLocked,turn off notificationLight");
                this.mNotificationLight.turnOff();
                Flog.i(1100, "turn off notificationLight due to incall or screenon");
                updateLight(false, 0, 0);
                return;
            }
            Light light = ledNotification.getLight();
            if (light != null && this.mNotificationPulseEnabled) {
                this.mNotificationLight.setFlashing(light.color, 1, light.onMs, light.offMs);
                Flog.i(1100, "set flash led color=0x" + Integer.toHexString(light.color) + ", ledOn:" + light.onMs + ", ledOff:" + light.offMs);
                updateLight(true, light.onMs, light.offMs);
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    protected int indexOfNotificationLocked(String pkg, String tag, int id, int userId) {
        ArrayList<NotificationRecord> list = this.mNotificationList;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            NotificationRecord r = (NotificationRecord) list.get(i);
            if (notificationMatchesUserId(r, userId) && r.sbn.getId() == id && TextUtils.equals(r.sbn.getTag(), tag) && r.sbn.getPackageName().equals(pkg)) {
                return i;
            }
        }
        return -1;
    }

    @GuardedBy("mNotificationLock")
    List<NotificationRecord> findGroupNotificationsLocked(String pkg, String groupKey, int userId) {
        List<NotificationRecord> records = new ArrayList();
        records.addAll(findGroupNotificationByListLocked(this.mNotificationList, pkg, groupKey, userId));
        records.addAll(findGroupNotificationByListLocked(this.mEnqueuedNotifications, pkg, groupKey, userId));
        return records;
    }

    @GuardedBy("mNotificationLock")
    private List<NotificationRecord> findGroupNotificationByListLocked(ArrayList<NotificationRecord> list, String pkg, String groupKey, int userId) {
        List<NotificationRecord> records = new ArrayList();
        int len = list.size();
        for (int i = 0; i < len; i++) {
            NotificationRecord r = (NotificationRecord) list.get(i);
            if (notificationMatchesUserId(r, userId) && r.getGroupKey().equals(groupKey) && r.sbn.getPackageName().equals(pkg)) {
                records.add(r);
            }
        }
        return records;
    }

    @GuardedBy("mNotificationLock")
    private NotificationRecord findNotificationByKeyLocked(String key) {
        NotificationRecord r = findNotificationByListLocked(this.mNotificationList, key);
        if (r != null) {
            return r;
        }
        r = findNotificationByListLocked(this.mEnqueuedNotifications, key);
        if (r != null) {
            return r;
        }
        return null;
    }

    @GuardedBy("mNotificationLock")
    NotificationRecord findNotificationLocked(String pkg, String tag, int id, int userId) {
        NotificationRecord r = findNotificationByListLocked(this.mNotificationList, pkg, tag, id, userId);
        if (r != null) {
            return r;
        }
        r = findNotificationByListLocked(this.mEnqueuedNotifications, pkg, tag, id, userId);
        if (r != null) {
            return r;
        }
        return null;
    }

    @GuardedBy("mNotificationLock")
    public NotificationRecord findNotificationByListLocked(ArrayList<NotificationRecord> list, String pkg, String tag, int id, int userId) {
        int len = list.size();
        for (int i = 0; i < len; i++) {
            NotificationRecord r = (NotificationRecord) list.get(i);
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
            if (key.equals(((NotificationRecord) list.get(i)).getKey())) {
                return (NotificationRecord) list.get(i);
            }
        }
        return null;
    }

    @GuardedBy("mNotificationLock")
    int indexOfNotificationLocked(String key) {
        int N = this.mNotificationList.size();
        for (int i = 0; i < N; i++) {
            if (key.equals(((NotificationRecord) this.mNotificationList.get(i)).getKey())) {
                return i;
            }
        }
        return -1;
    }

    private void updateNotificationPulse() {
        synchronized (this.mNotificationLock) {
            updateLightsLocked();
        }
    }

    protected boolean isCallingUidSystem() {
        return Binder.getCallingUid() == 1000;
    }

    protected boolean isUidSystemOrPhone(int uid) {
        int appid = UserHandle.getAppId(uid);
        if (appid == 1000 || appid == 1001 || uid == 0) {
            return true;
        }
        return false;
    }

    protected boolean isCallerSystemOrPhone() {
        return isUidSystemOrPhone(Binder.getCallingUid());
    }

    private void checkCallerIsSystem() {
        if (!isCallerSystemOrPhone()) {
            throw new SecurityException("Disallowed call for uid " + Binder.getCallingUid());
        }
    }

    private void checkCallerIsSystemOrSameApp(String pkg) {
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

    private void checkCallerIsSameApp(String pkg) {
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

    private static String callStateToString(int state) {
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
                    NotificationManagerService.this.mCallState = state;
                }
            }
        }, 32);
    }

    @GuardedBy("mNotificationLock")
    private NotificationRankingUpdate makeRankingUpdateLocked(ManagedServiceInfo info) {
        int i;
        int N = this.mNotificationList.size();
        ArrayList<String> arrayList = new ArrayList(N);
        arrayList = new ArrayList(N);
        ArrayList<Integer> arrayList2 = new ArrayList(N);
        Bundle overrideGroupKeys = new Bundle();
        Bundle visibilityOverrides = new Bundle();
        Bundle suppressedVisualEffects = new Bundle();
        Bundle explanation = new Bundle();
        Bundle channels = new Bundle();
        Bundle overridePeople = new Bundle();
        Bundle snoozeCriteria = new Bundle();
        Bundle showBadge = new Bundle();
        for (i = 0; i < N; i++) {
            NotificationRecord record = (NotificationRecord) this.mNotificationList.get(i);
            if (isVisibleToListener(record.sbn, info)) {
                String key = record.sbn.getKey();
                arrayList.add(key);
                arrayList2.add(Integer.valueOf(record.getImportance()));
                if (record.getImportanceExplanation() != null) {
                    explanation.putCharSequence(key, record.getImportanceExplanation());
                }
                if (record.isIntercepted()) {
                    arrayList.add(key);
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
            }
        }
        int M = arrayList.size();
        String[] keysAr = (String[]) arrayList.toArray(new String[M]);
        String[] interceptedKeysAr = (String[]) arrayList.toArray(new String[arrayList.size()]);
        int[] importanceAr = new int[M];
        for (i = 0; i < M; i++) {
            importanceAr[i] = ((Integer) arrayList2.get(i)).intValue();
        }
        return new NotificationRankingUpdate(keysAr, interceptedKeysAr, visibilityOverrides, suppressedVisualEffects, importanceAr, explanation, overrideGroupKeys, channels, overridePeople, snoozeCriteria, showBadge);
    }

    boolean hasCompanionDevice(ManagedServiceInfo info) {
        if (this.mCompanionManager == null) {
            this.mCompanionManager = getCompanionManager();
        }
        boolean z = this.mCompanionManager;
        if (!z) {
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            if (ArrayUtils.isEmpty(this.mCompanionManager.getAssociations(info.component.getPackageName(), info.userid))) {
                Binder.restoreCallingIdentity(identity);
                return false;
            }
            z = true;
            return z;
        } catch (SecurityException e) {
        } catch (RemoteException re) {
            z = TAG;
            Slog.e(z, "Cannot reach companion device service", re);
        } catch (Exception e2) {
            z = TAG;
            Slog.e(z, "Cannot verify listener " + info, e2);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    protected ICompanionDeviceManager getCompanionManager() {
        return ICompanionDeviceManager.Stub.asInterface(ServiceManager.getService("companiondevice"));
    }

    private boolean isVisibleToListener(StatusBarNotification sbn, ManagedServiceInfo listener) {
        if (listener.enabledAndUserMatches(sbn.getUserId())) {
            return true;
        }
        return false;
    }

    private boolean isPackageSuspendedForUser(String pkg, int uid) {
        try {
            return this.mPackageManager.isPackageSuspendedForUser(pkg, UserHandle.getUserId(uid));
        } catch (RemoteException e) {
            throw new SecurityException("Could not talk to package manager service");
        } catch (IllegalArgumentException e2) {
            return false;
        }
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

    private int calculateNotifyType(NotificationRecord r) {
        int notifyType = r.getImportance() < 2 ? 0 : 1;
        if (r.getNotification().extras == null) {
            return notifyType;
        }
        if (r.getNotification().extras.containsKey("hw_btw")) {
            notifyType = disallowInterrupt(r.getNotification()) ? 0 : 1;
        }
        String hwType = r.getNotification().extras.getString("hw_type");
        if (hwType == null || !hwType.equals("type_music")) {
            return notifyType;
        }
        return 2;
    }

    public void setSysMgrCfgMap(ArrayList<NotificationSysMgrCfg> tempSysMgrCfgList) {
        if (this.mRankingHelper == null) {
            Log.w(TAG, "setSysMgrCfgMap: mRankingHelper is null");
        } else {
            this.mRankingHelper.setSysMgrCfgMap(tempSysMgrCfgList);
        }
    }
}
