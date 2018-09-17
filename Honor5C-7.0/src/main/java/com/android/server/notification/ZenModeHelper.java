package com.android.server.notification;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AppOpsManager;
import android.app.AutomaticZenRule;
import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.media.AudioManagerInternal;
import android.media.VolumePolicy;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.EventInfo;
import android.service.notification.ZenModeConfig.Migration;
import android.service.notification.ZenModeConfig.ScheduleInfo;
import android.service.notification.ZenModeConfig.XmlV1;
import android.service.notification.ZenModeConfig.ZenRule;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.logging.MetricsLogger;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IHwZenModeFiltering;
import com.android.server.LocalServices;
import com.android.server.am.ProcessList;
import com.android.server.notification.ManagedServices.Config;
import com.android.server.wm.WindowState;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ZenModeHelper {
    static final boolean DEBUG = false;
    private static final String DefaultEventsName = "zen_mode_default_events_name";
    private static final String DefaultWeekendsName = "zen_mode_default_weekends_name";
    private static final String DefaultWeeknightsName = "zen_mode_default_weeknights_name";
    private static final int RULE_INSTANCE_GRACE_PERIOD = 259200000;
    public static final long SUPPRESSED_EFFECT_ALL = 3;
    public static final long SUPPRESSED_EFFECT_CALLS = 2;
    public static final long SUPPRESSED_EFFECT_NOTIFICATIONS = 1;
    static final String TAG = "ZenModeHelper";
    private final AppOpsManager mAppOps;
    private AudioManagerInternal mAudioManager;
    private final ArrayList<Callback> mCallbacks;
    private final ZenModeConditions mConditions;
    private ZenModeConfig mConfig;
    private final Migration mConfigMigration;
    private final SparseArray<ZenModeConfig> mConfigs;
    private final Context mContext;
    private final ZenModeConfig mDefaultConfig;
    private final ZenModeFiltering mFiltering;
    private final H mHandler;
    private final Metrics mMetrics;
    private PackageManager mPm;
    private final RingerModeDelegate mRingerModeDelegate;
    private final Config mServiceConfig;
    private final SettingsObserver mSettingsObserver;
    private long mSuppressedEffects;
    private int mUser;
    private int mZenMode;

    public static class Callback {
        void onConfigChanged() {
        }

        void onZenModeChanged() {
        }

        void onPolicyChanged() {
        }
    }

    private final class H extends Handler {
        private static final long METRICS_PERIOD_MS = 21600000;
        private static final int MSG_APPLY_CONFIG = 4;
        private static final int MSG_DISPATCH = 1;
        private static final int MSG_METRICS = 2;
        private static final int MSG_SET_CONFIG = 3;

        private final class ConfigMessageData {
            public final ZenModeConfig config;
            public final String reason;
            public final boolean setRingerMode;

            ConfigMessageData(ZenModeConfig config, String reason) {
                this.config = config;
                this.reason = reason;
                this.setRingerMode = ZenModeHelper.DEBUG;
            }

            ConfigMessageData(ZenModeConfig config, String reason, boolean setRingerMode) {
                this.config = config;
                this.reason = reason;
                this.setRingerMode = setRingerMode;
            }
        }

        private H(Looper looper) {
            super(looper);
        }

        private void postDispatchOnZenModeChanged() {
            removeMessages(MSG_DISPATCH);
            sendEmptyMessage(MSG_DISPATCH);
        }

        private void postMetricsTimer() {
            removeMessages(MSG_METRICS);
            sendEmptyMessageDelayed(MSG_METRICS, METRICS_PERIOD_MS);
        }

        private void postSetConfig(ZenModeConfig config, String reason) {
            sendMessage(obtainMessage(MSG_SET_CONFIG, new ConfigMessageData(config, reason)));
        }

        private void postApplyConfig(ZenModeConfig config, String reason, boolean setRingerMode) {
            sendMessage(obtainMessage(MSG_APPLY_CONFIG, new ConfigMessageData(config, reason, setRingerMode)));
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DISPATCH /*1*/:
                    ZenModeHelper.this.dispatchOnZenModeChanged();
                case MSG_METRICS /*2*/:
                    ZenModeHelper.this.mMetrics.emit();
                case MSG_SET_CONFIG /*3*/:
                    ConfigMessageData configData = msg.obj;
                    synchronized (ZenModeHelper.this.mConfig) {
                        ZenModeHelper.this.setConfigLocked(configData.config, configData.reason);
                        break;
                    }
                case MSG_APPLY_CONFIG /*4*/:
                    ConfigMessageData applyConfigData = msg.obj;
                    ZenModeHelper.this.applyConfig(applyConfigData.config, applyConfigData.reason, applyConfigData.setRingerMode);
                default:
            }
        }
    }

    private final class Metrics extends Callback {
        private static final String COUNTER_PREFIX = "dnd_mode_";
        private static final long MINIMUM_LOG_PERIOD_MS = 60000;
        private long mBeginningMs;
        private int mPreviousZenMode;

        private Metrics() {
            this.mPreviousZenMode = -1;
            this.mBeginningMs = 0;
        }

        void onZenModeChanged() {
            emit();
        }

        private void emit() {
            ZenModeHelper.this.mHandler.postMetricsTimer();
            long now = SystemClock.elapsedRealtime();
            long since = now - this.mBeginningMs;
            if (this.mPreviousZenMode != ZenModeHelper.this.mZenMode || since > MINIMUM_LOG_PERIOD_MS) {
                if (this.mPreviousZenMode != -1) {
                    MetricsLogger.count(ZenModeHelper.this.mContext, COUNTER_PREFIX + this.mPreviousZenMode, (int) since);
                }
                this.mPreviousZenMode = ZenModeHelper.this.mZenMode;
                this.mBeginningMs = now;
            }
        }
    }

    private final class RingerModeDelegate implements android.media.AudioManagerInternal.RingerModeDelegate {
        private RingerModeDelegate() {
        }

        public String toString() {
            return ZenModeHelper.TAG;
        }

        public int onSetRingerModeInternal(int ringerModeOld, int ringerModeNew, String caller, int ringerModeExternal, VolumePolicy policy) {
            boolean isChange = ringerModeOld != ringerModeNew ? true : ZenModeHelper.DEBUG;
            int ringerModeExternalOut = ringerModeNew;
            int newZen = -1;
            switch (ringerModeNew) {
                case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                    if (isChange && policy.doNotDisturbWhenSilent) {
                        if (!(ZenModeHelper.this.mZenMode == 2 || ZenModeHelper.this.mZenMode == 3)) {
                            newZen = 3;
                        }
                        ZenModeHelper.this.setPreviousRingerModeSetting(Integer.valueOf(ringerModeOld));
                        break;
                    }
                case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                    if (!isChange || ringerModeOld != 0 || (ZenModeHelper.this.mZenMode != 2 && ZenModeHelper.this.mZenMode != 3)) {
                        if (ZenModeHelper.this.mZenMode != 0) {
                            break;
                        }
                    }
                    newZen = 0;
                    break;
                    break;
            }
            if (!isChange && newZen == -1) {
                if (ringerModeExternal != ringerModeNew) {
                }
                return ringerModeNew;
            }
            ZenLog.traceSetRingerModeInternal(ringerModeOld, ringerModeNew, caller, ringerModeExternal, ringerModeNew);
            return ringerModeNew;
        }

        public int onSetRingerModeExternal(int ringerModeOld, int ringerModeNew, String caller, int ringerModeInternal, VolumePolicy policy) {
            int ringerModeInternalOut = ringerModeNew;
            boolean isChange = ringerModeOld != ringerModeNew ? true : ZenModeHelper.DEBUG;
            if (ringerModeInternal == 1) {
            }
            switch (ringerModeNew) {
                case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                    if (isChange) {
                        if (ZenModeHelper.this.mZenMode == 0) {
                            break;
                        }
                    }
                    ringerModeInternalOut = ringerModeInternal;
                    break;
                    break;
                case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                    if (ZenModeHelper.this.mZenMode != 0) {
                        break;
                    }
                    break;
            }
            ZenLog.traceSetRingerModeExternal(ringerModeOld, ringerModeNew, caller, ringerModeInternal, ringerModeInternalOut);
            return ringerModeInternalOut;
        }

        public boolean canVolumeDownEnterSilent() {
            return ZenModeHelper.this.mZenMode == 0 ? true : ZenModeHelper.DEBUG;
        }

        public int getRingerModeAffectedStreams(int streams) {
            streams |= 38;
            if (ZenModeHelper.this.mZenMode == 2) {
                return streams | 8;
            }
            return streams & -9;
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri ZEN_MODE;

        public SettingsObserver(Handler handler) {
            super(handler);
            this.ZEN_MODE = Global.getUriFor("zen_mode");
        }

        public void observe() {
            ZenModeHelper.this.mContext.getContentResolver().registerContentObserver(this.ZEN_MODE, ZenModeHelper.DEBUG, this);
            update(null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            if (this.ZEN_MODE.equals(uri) && ZenModeHelper.this.mZenMode != ZenModeHelper.this.getZenModeSetting()) {
                if (ZenModeHelper.DEBUG) {
                    Log.d(ZenModeHelper.TAG, "Fixing zen mode setting");
                }
                ZenModeHelper.this.setZenModeSetting(ZenModeHelper.this.mZenMode);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.notification.ZenModeHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.notification.ZenModeHelper.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.ZenModeHelper.<clinit>():void");
    }

    public ZenModeHelper(Context context, Looper looper, ConditionProviders conditionProviders) {
        this.mCallbacks = new ArrayList();
        this.mRingerModeDelegate = new RingerModeDelegate();
        this.mConfigs = new SparseArray();
        this.mMetrics = new Metrics();
        this.mUser = 0;
        this.mConfigMigration = new Migration() {
            public ZenModeConfig migrate(XmlV1 v1) {
                int i = 1;
                if (v1 == null) {
                    return null;
                }
                ZenModeConfig rt = new ZenModeConfig();
                rt.allowCalls = v1.allowCalls;
                rt.allowEvents = v1.allowEvents;
                rt.allowCallsFrom = v1.allowFrom;
                rt.allowMessages = v1.allowMessages;
                rt.allowMessagesFrom = v1.allowFrom;
                rt.allowReminders = v1.allowReminders;
                int[] days = XmlV1.tryParseDays(v1.sleepMode);
                if (days == null || days.length <= 0) {
                    Log.i(ZenModeHelper.TAG, "No existing V1 downtime found, generating default schedules");
                    ZenModeHelper.this.appendDefaultScheduleRules(rt);
                } else {
                    Log.i(ZenModeHelper.TAG, "Migrating existing V1 downtime to single schedule");
                    ScheduleInfo schedule = new ScheduleInfo();
                    schedule.days = days;
                    schedule.startHour = v1.sleepStartHour;
                    schedule.startMinute = v1.sleepStartMinute;
                    schedule.endHour = v1.sleepEndHour;
                    schedule.endMinute = v1.sleepEndMinute;
                    ZenRule rule = new ZenRule();
                    rule.enabled = true;
                    rule.name = ZenModeHelper.this.mContext.getResources().getString(17040811);
                    rule.conditionId = ZenModeConfig.toScheduleConditionId(schedule);
                    if (v1.sleepNone) {
                        i = 2;
                    }
                    rule.zenMode = i;
                    rule.component = ScheduleConditionProvider.COMPONENT;
                    rt.automaticRules.put(ZenModeConfig.newRuleId(), rule);
                }
                ZenModeHelper.this.appendDefaultEventRules(rt);
                return rt;
            }
        };
        this.mContext = context;
        this.mHandler = new H(looper, null);
        addCallback(this.mMetrics);
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mDefaultConfig = readDefaultConfig(context.getResources());
        appendDefaultScheduleRules(this.mDefaultConfig);
        appendDefaultEventRules(this.mDefaultConfig);
        this.mConfig = this.mDefaultConfig;
        this.mConfigs.put(0, this.mConfig);
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mSettingsObserver.observe();
        this.mFiltering = new ZenModeFiltering(this.mContext);
        this.mConditions = new ZenModeConditions(this, conditionProviders);
        this.mServiceConfig = conditionProviders.getConfig();
    }

    public Looper getLooper() {
        return this.mHandler.getLooper();
    }

    public String toString() {
        return TAG;
    }

    public boolean matchesCallFilter(UserHandle userHandle, Bundle extras, ValidateNotificationPeople validator, int contactsTimeoutMs, float timeoutAffinity) {
        synchronized (this.mConfig) {
            IHwZenModeFiltering filter = HwServiceFactory.getHwZenModeFiltering();
            if (filter != null) {
                boolean matchesCallFilter = filter.matchesCallFilter(this.mContext, this.mZenMode, this.mConfig, userHandle, extras, validator, contactsTimeoutMs, timeoutAffinity);
                return matchesCallFilter;
            }
            matchesCallFilter = ZenModeFiltering.matchesCallFilter(this.mContext, this.mZenMode, this.mConfig, userHandle, extras, validator, contactsTimeoutMs, timeoutAffinity);
            return matchesCallFilter;
        }
    }

    public boolean isCall(NotificationRecord record) {
        return this.mFiltering.isCall(record);
    }

    public boolean shouldIntercept(NotificationRecord record) {
        boolean shouldIntercept;
        synchronized (this.mConfig) {
            shouldIntercept = this.mFiltering.shouldIntercept(this.mZenMode, this.mConfig, record);
        }
        return shouldIntercept;
    }

    public boolean shouldSuppressWhenScreenOff() {
        boolean z;
        synchronized (this.mConfig) {
            z = this.mConfig.allowWhenScreenOff ? DEBUG : true;
        }
        return z;
    }

    public boolean shouldSuppressWhenScreenOn() {
        boolean z;
        synchronized (this.mConfig) {
            z = this.mConfig.allowWhenScreenOn ? DEBUG : true;
        }
        return z;
    }

    public void addCallback(Callback callback) {
        this.mCallbacks.add(callback);
    }

    public void removeCallback(Callback callback) {
        this.mCallbacks.remove(callback);
    }

    public void initZenMode() {
        if (DEBUG) {
            Log.d(TAG, "initZenMode");
        }
        evaluateZenMode("init", true);
    }

    public void onSystemReady() {
        if (DEBUG) {
            Log.d(TAG, "onSystemReady");
        }
        this.mAudioManager = (AudioManagerInternal) LocalServices.getService(AudioManagerInternal.class);
        if (this.mAudioManager != null) {
            this.mAudioManager.setRingerModeDelegate(this.mRingerModeDelegate);
        }
        this.mPm = this.mContext.getPackageManager();
        this.mHandler.postMetricsTimer();
        cleanUpZenRules();
        evaluateZenMode("onSystemReady", true);
    }

    public void onUserSwitched(int user) {
        loadConfigForUser(user, "onUserSwitched");
    }

    public void onUserRemoved(int user) {
        if (user >= 0) {
            if (DEBUG) {
                Log.d(TAG, "onUserRemoved u=" + user);
            }
            this.mConfigs.remove(user);
        }
    }

    public void onUserUnlocked(int user) {
        loadConfigForUser(user, "onUserUnlocked");
    }

    private void loadConfigForUser(int user, String reason) {
        if (this.mUser != user && user >= 0) {
            this.mUser = user;
            if (DEBUG) {
                Log.d(TAG, reason + " u=" + user);
            }
            ZenModeConfig config = (ZenModeConfig) this.mConfigs.get(user);
            if (config == null) {
                if (DEBUG) {
                    Log.d(TAG, reason + " generating default config for user " + user);
                }
                config = this.mDefaultConfig.copy();
                config.user = user;
            }
            if (user != 0 && "onUserSwitched".equals(reason)) {
                ZenModeConfig config_owner = (ZenModeConfig) this.mConfigs.get(0);
                config = config_owner.copy();
                config.user = user;
                Log.d(TAG, "current user config.user:" + config.user + ",config_owner:" + config_owner);
            }
            synchronized (this.mConfig) {
                setConfigLocked(config, reason);
            }
            cleanUpZenRules();
        }
    }

    public int getZenModeListenerInterruptionFilter() {
        return NotificationManager.zenModeToInterruptionFilter(this.mZenMode);
    }

    public void requestFromListener(ComponentName name, int filter) {
        int newZen = NotificationManager.zenModeFromInterruptionFilter(filter, -1);
        if (newZen != -1) {
            String flattenToShortString;
            StringBuilder append = new StringBuilder().append("listener:");
            if (name != null) {
                flattenToShortString = name.flattenToShortString();
            } else {
                flattenToShortString = null;
            }
            setManualZenMode(newZen, null, append.append(flattenToShortString).toString());
        }
    }

    public void setSuppressedEffects(long suppressedEffects) {
        if (this.mSuppressedEffects != suppressedEffects) {
            this.mSuppressedEffects = suppressedEffects;
            applyRestrictions();
        }
    }

    public long getSuppressedEffects() {
        return this.mSuppressedEffects;
    }

    public int getZenMode() {
        return this.mZenMode;
    }

    public List<ZenRule> getZenRules() {
        List<ZenRule> rules = new ArrayList();
        synchronized (this.mConfig) {
            if (this.mConfig == null) {
                return rules;
            }
            for (ZenRule rule : this.mConfig.automaticRules.values()) {
                if (canManageAutomaticZenRule(rule)) {
                    rules.add(rule);
                }
            }
            return rules;
        }
    }

    public AutomaticZenRule getAutomaticZenRule(String id) {
        synchronized (this.mConfig) {
            if (this.mConfig == null) {
                return null;
            }
            ZenRule rule = (ZenRule) this.mConfig.automaticRules.get(id);
            if (rule != null && canManageAutomaticZenRule(rule)) {
                return createAutomaticZenRule(rule);
            }
            return null;
        }
    }

    public String addAutomaticZenRule(AutomaticZenRule automaticZenRule, String reason) {
        String str;
        if (!isSystemRule(automaticZenRule)) {
            ServiceInfo owner = getServiceInfo(automaticZenRule.getOwner());
            if (owner == null) {
                throw new IllegalArgumentException("Owner is not a condition provider service");
            }
            int ruleInstanceLimit = -1;
            if (owner.metaData != null) {
                ruleInstanceLimit = owner.metaData.getInt("android.service.zen.automatic.ruleInstanceLimit", -1);
            }
            if (ruleInstanceLimit > 0 && ruleInstanceLimit < getCurrentInstanceCount(automaticZenRule.getOwner()) + 1) {
                throw new IllegalArgumentException("Rule instance limit exceeded");
            }
        }
        synchronized (this.mConfig) {
            if (this.mConfig == null) {
                throw new AndroidRuntimeException("Could not create rule");
            }
            if (DEBUG) {
                Log.d(TAG, "addAutomaticZenRule rule= " + automaticZenRule + " reason=" + reason);
            }
            ZenModeConfig newConfig = this.mConfig.copy();
            ZenRule rule = new ZenRule();
            populateZenRule(automaticZenRule, rule, true);
            newConfig.automaticRules.put(rule.id, rule);
            if (setConfigLocked(newConfig, reason, true)) {
                str = rule.id;
            } else {
                throw new AndroidRuntimeException("Could not create rule");
            }
        }
        return str;
    }

    public boolean updateAutomaticZenRule(String ruleId, AutomaticZenRule automaticZenRule, String reason) {
        synchronized (this.mConfig) {
            if (this.mConfig == null) {
                return DEBUG;
            }
            if (DEBUG) {
                Log.d(TAG, "updateAutomaticZenRule zenRule=" + automaticZenRule + " reason=" + reason);
            }
            ZenModeConfig newConfig = this.mConfig.copy();
            if (ruleId == null) {
                throw new IllegalArgumentException("Rule doesn't exist");
            }
            ZenRule rule = (ZenRule) newConfig.automaticRules.get(ruleId);
            if (rule == null || !canManageAutomaticZenRule(rule)) {
                throw new SecurityException("Cannot update rules not owned by your condition provider");
            }
            populateZenRule(automaticZenRule, rule, DEBUG);
            newConfig.automaticRules.put(ruleId, rule);
            boolean configLocked = setConfigLocked(newConfig, reason, true);
            return configLocked;
        }
    }

    public boolean removeAutomaticZenRule(String id, String reason) {
        synchronized (this.mConfig) {
            if (this.mConfig == null) {
                return DEBUG;
            }
            ZenModeConfig newConfig = this.mConfig.copy();
            ZenRule rule = (ZenRule) newConfig.automaticRules.get(id);
            if (rule == null) {
                return DEBUG;
            } else if (canManageAutomaticZenRule(rule)) {
                newConfig.automaticRules.remove(id);
                if (DEBUG) {
                    Log.d(TAG, "removeZenRule zenRule=" + id + " reason=" + reason);
                }
                boolean configLocked = setConfigLocked(newConfig, reason, true);
                return configLocked;
            } else {
                throw new SecurityException("Cannot delete rules not owned by your condition provider");
            }
        }
    }

    public boolean removeAutomaticZenRules(String packageName, String reason) {
        synchronized (this.mConfig) {
            if (this.mConfig == null) {
                return DEBUG;
            }
            ZenModeConfig newConfig = this.mConfig.copy();
            for (int i = newConfig.automaticRules.size() - 1; i >= 0; i--) {
                ZenRule rule = (ZenRule) newConfig.automaticRules.get(newConfig.automaticRules.keyAt(i));
                if (rule.component.getPackageName().equals(packageName) && canManageAutomaticZenRule(rule)) {
                    newConfig.automaticRules.removeAt(i);
                }
            }
            boolean configLocked = setConfigLocked(newConfig, reason, true);
            return configLocked;
        }
    }

    public int getCurrentInstanceCount(ComponentName owner) {
        int count = 0;
        synchronized (this.mConfig) {
            for (ZenRule rule : this.mConfig.automaticRules.values()) {
                if (rule.component != null && rule.component.equals(owner)) {
                    count++;
                }
            }
        }
        return count;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean canManageAutomaticZenRule(ZenRule rule) {
        int callingUid = Binder.getCallingUid();
        if (callingUid == 0 || callingUid == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || this.mContext.checkCallingPermission("android.permission.MANAGE_NOTIFICATIONS") == 0) {
            return true;
        }
        String[] packages = this.mPm.getPackagesForUid(Binder.getCallingUid());
        if (packages != null) {
            for (String equals : packages) {
                if (equals.equals(rule.component.getPackageName())) {
                    return true;
                }
            }
        }
        return DEBUG;
    }

    private boolean isSystemRule(AutomaticZenRule rule) {
        return "android".equals(rule.getOwner().getPackageName());
    }

    private ServiceInfo getServiceInfo(ComponentName owner) {
        Intent queryIntent = new Intent();
        queryIntent.setComponent(owner);
        List<ResolveInfo> installedServices = this.mPm.queryIntentServicesAsUser(queryIntent, 132, UserHandle.getCallingUserId());
        if (installedServices != null) {
            int count = installedServices.size();
            for (int i = 0; i < count; i++) {
                ServiceInfo info = ((ResolveInfo) installedServices.get(i)).serviceInfo;
                if (this.mServiceConfig.bindPermission.equals(info.permission)) {
                    return info;
                }
            }
        }
        return null;
    }

    private void populateZenRule(AutomaticZenRule automaticZenRule, ZenRule rule, boolean isNew) {
        if (isNew) {
            rule.id = ZenModeConfig.newRuleId();
            rule.creationTime = System.currentTimeMillis();
            rule.component = automaticZenRule.getOwner();
        }
        if (rule.enabled != automaticZenRule.isEnabled()) {
            rule.snoozing = DEBUG;
        }
        rule.name = automaticZenRule.getName();
        rule.condition = null;
        rule.conditionId = automaticZenRule.getConditionId();
        rule.enabled = automaticZenRule.isEnabled();
        rule.zenMode = NotificationManager.zenModeFromInterruptionFilter(automaticZenRule.getInterruptionFilter(), 0);
    }

    private AutomaticZenRule createAutomaticZenRule(ZenRule rule) {
        return new AutomaticZenRule(rule.name, rule.component, rule.conditionId, NotificationManager.zenModeToInterruptionFilter(rule.zenMode), rule.enabled, rule.creationTime);
    }

    public void setManualZenMode(int zenMode, Uri conditionId, String reason) {
        setManualZenMode(zenMode, conditionId, reason, true);
    }

    private void setManualZenMode(int zenMode, Uri conditionId, String reason, boolean setRingerMode) {
        synchronized (this.mConfig) {
            if (this.mConfig == null) {
            } else if (Global.isValidZenMode(zenMode)) {
                if (DEBUG) {
                    Log.d(TAG, "setManualZenMode " + Global.zenModeToString(zenMode) + " conditionId=" + conditionId + " reason=" + reason + " setRingerMode=" + setRingerMode);
                }
                ZenModeConfig newConfig = this.mConfig.copy();
                if (zenMode == 0) {
                    newConfig.manualRule = null;
                    for (ZenRule automaticRule : newConfig.automaticRules.values()) {
                        if (automaticRule.isAutomaticActive()) {
                            automaticRule.snoozing = true;
                        }
                    }
                } else {
                    ZenRule newRule = new ZenRule();
                    newRule.enabled = true;
                    newRule.zenMode = zenMode;
                    newRule.conditionId = conditionId;
                    newConfig.manualRule = newRule;
                }
                if ("restore_default".equals(reason)) {
                    setConfigLocked(this.mDefaultConfig, reason, true);
                } else {
                    setConfigLocked(newConfig, reason, setRingerMode);
                }
            }
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mZenMode=");
        pw.println(Global.zenModeToString(this.mZenMode));
        dump(pw, prefix, "mDefaultConfig", this.mDefaultConfig);
        int N = this.mConfigs.size();
        for (int i = 0; i < N; i++) {
            dump(pw, prefix, "mConfigs[u=" + this.mConfigs.keyAt(i) + "]", (ZenModeConfig) this.mConfigs.valueAt(i));
        }
        pw.print(prefix);
        pw.print("mUser=");
        pw.println(this.mUser);
        synchronized (this.mConfig) {
            dump(pw, prefix, "mConfig", this.mConfig);
        }
        pw.print(prefix);
        pw.print("mSuppressedEffects=");
        pw.println(this.mSuppressedEffects);
        this.mFiltering.dump(pw, prefix);
        this.mConditions.dump(pw, prefix);
    }

    private static void dump(PrintWriter pw, String prefix, String var, ZenModeConfig config) {
        pw.print(prefix);
        pw.print(var);
        pw.print('=');
        if (config == null) {
            pw.println(config);
            return;
        }
        pw.printf("allow(calls=%s,callsFrom=%s,repeatCallers=%s,messages=%s,messagesFrom=%s,events=%s,reminders=%s,whenScreenOff,whenScreenOn=%s)\n", new Object[]{Boolean.valueOf(config.allowCalls), ZenModeConfig.sourceToString(config.allowCallsFrom), Boolean.valueOf(config.allowRepeatCallers), Boolean.valueOf(config.allowMessages), ZenModeConfig.sourceToString(config.allowMessagesFrom), Boolean.valueOf(config.allowEvents), Boolean.valueOf(config.allowReminders), Boolean.valueOf(config.allowWhenScreenOff), Boolean.valueOf(config.allowWhenScreenOn)});
        pw.print(prefix);
        pw.print("  manualRule=");
        pw.println(config.manualRule);
        if (!config.automaticRules.isEmpty()) {
            int N = config.automaticRules.size();
            int i = 0;
            while (i < N) {
                pw.print(prefix);
                pw.print(i == 0 ? "  automaticRules=" : "                 ");
                pw.println(config.automaticRules.valueAt(i));
                i++;
            }
        }
    }

    public void readXml(XmlPullParser parser, boolean forRestore) throws XmlPullParserException, IOException {
        ZenModeConfig config = ZenModeConfig.readXml(parser, this.mConfigMigration);
        if (config != null) {
            if (forRestore) {
                if (config.user == 0) {
                    config.manualRule = null;
                    long time = System.currentTimeMillis();
                    if (config.automaticRules != null) {
                        for (ZenRule automaticRule : config.automaticRules.values()) {
                            automaticRule.snoozing = DEBUG;
                            automaticRule.condition = null;
                            automaticRule.creationTime = time;
                        }
                    }
                } else {
                    return;
                }
            }
            if (DEBUG) {
                Log.d(TAG, "readXml");
            }
            synchronized (this.mConfig) {
                setConfigLocked(config, "readXml");
            }
        }
    }

    public void writeXml(XmlSerializer out, boolean forBackup) throws IOException {
        int N = this.mConfigs.size();
        int i = 0;
        while (i < N) {
            if (!forBackup || this.mConfigs.keyAt(i) == 0) {
                ((ZenModeConfig) this.mConfigs.valueAt(i)).writeXml(out);
            }
            i++;
        }
    }

    public Policy getNotificationPolicy() {
        return getNotificationPolicy(this.mConfig);
    }

    private static Policy getNotificationPolicy(ZenModeConfig config) {
        return config == null ? null : config.toNotificationPolicy();
    }

    public void setNotificationPolicy(Policy policy) {
        if (policy != null && this.mConfig != null) {
            synchronized (this.mConfig) {
                ZenModeConfig newConfig = this.mConfig.copy();
                newConfig.applyNotificationPolicy(policy);
                setConfigLocked(newConfig, "setNotificationPolicy");
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void cleanUpZenRules() {
        long currentTime = System.currentTimeMillis();
        synchronized (this.mConfig) {
            ZenModeConfig newConfig = this.mConfig.copy();
            if (newConfig.automaticRules != null) {
                for (int i = newConfig.automaticRules.size() - 1; i >= 0; i--) {
                    ZenRule rule = (ZenRule) newConfig.automaticRules.get(newConfig.automaticRules.keyAt(i));
                    if (259200000 < currentTime - rule.creationTime) {
                        try {
                            this.mPm.getPackageInfo(rule.component.getPackageName(), DumpState.DUMP_PREFERRED_XML);
                        } catch (NameNotFoundException e) {
                            newConfig.automaticRules.removeAt(i);
                        }
                    }
                }
            }
            setConfigLocked(newConfig, "cleanUpZenRules");
        }
    }

    public ZenModeConfig getConfig() {
        ZenModeConfig copy;
        synchronized (this.mConfig) {
            copy = this.mConfig.copy();
        }
        return copy;
    }

    public boolean setConfigLocked(ZenModeConfig config, String reason) {
        if ("restore_default".equals(reason)) {
            config = this.mDefaultConfig;
        }
        return setConfigLocked(config, reason, true);
    }

    private boolean isVDriverMode() {
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (activityManager == null) {
            return DEBUG;
        }
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return DEBUG;
        }
        boolean isVDriveModeFlay = DEBUG;
        String packageName = "com.huawei.vdrive";
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (packageName.equals(appProcess.processName)) {
                isVDriveModeFlay = true;
                Log.d(TAG, "HwVDrive is running .");
                break;
            }
        }
        Log.d(TAG, "isVDriverMode return value isVDriveModeFlay:" + isVDriveModeFlay);
        return isVDriveModeFlay;
    }

    public void setConfigAsync(ZenModeConfig config, String reason) {
        this.mHandler.postSetConfig(config, reason);
    }

    private boolean setConfigLocked(ZenModeConfig config, String reason, boolean setRingerMode) {
        long identity = Binder.clearCallingIdentity();
        if (config != null) {
            try {
                if (config.isValid()) {
                    if (config.user != this.mUser) {
                        this.mConfigs.put(config.user, config);
                        if (DEBUG) {
                            Log.d(TAG, "setConfigLocked: store config for user " + config.user);
                        }
                        Binder.restoreCallingIdentity(identity);
                        return true;
                    }
                    this.mConditions.evaluateConfig(config, DEBUG);
                    this.mConfigs.put(config.user, config);
                    if (DEBUG) {
                        Log.d(TAG, "setConfigLocked reason=" + reason, new Throwable());
                    }
                    ZenLog.traceConfig(reason, this.mConfig, config);
                    boolean policyChanged = Objects.equals(getNotificationPolicy(this.mConfig), getNotificationPolicy(config)) ? DEBUG : true;
                    if (!config.equals(this.mConfig)) {
                        dispatchOnConfigChanged();
                    }
                    if (policyChanged) {
                        dispatchOnPolicyChanged();
                    }
                    this.mConfig = config;
                    String val = Integer.toString(config.hashCode());
                    boolean isVDriverMode = "conditionChanged".equals(reason) ? isVDriverMode() : DEBUG;
                    Log.e(TAG, "ZenModeHelper isVDriverMode:" + isVDriverMode + ",timecondition:" + "conditionChanged".equals(reason));
                    if (!isVDriverMode) {
                        Global.putString(this.mContext.getContentResolver(), "zen_mode_config_etag", val);
                        if (!evaluateZenMode(reason, setRingerMode)) {
                            applyRestrictions();
                        }
                    }
                    this.mConditions.evaluateConfig(config, true);
                    this.mHandler.postApplyConfig(config, reason, setRingerMode);
                    Binder.restoreCallingIdentity(identity);
                    return true;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }
        Log.w(TAG, "Invalid config in setConfigLocked; " + config);
        Binder.restoreCallingIdentity(identity);
        return DEBUG;
    }

    private void applyConfig(ZenModeConfig config, String reason, boolean setRingerMode) {
        Global.putString(this.mContext.getContentResolver(), "zen_mode_config_etag", Integer.toString(config.hashCode()));
        if (!evaluateZenMode(reason, setRingerMode)) {
            applyRestrictions();
        }
        this.mConditions.evaluateConfig(config, true);
    }

    private int getZenModeSetting() {
        return Global.getInt(this.mContext.getContentResolver(), "zen_mode", 0);
    }

    private void setZenModeSetting(int zen) {
        Global.putInt(this.mContext.getContentResolver(), "zen_mode", zen);
    }

    private int getPreviousRingerModeSetting() {
        return Global.getInt(this.mContext.getContentResolver(), "zen_mode_ringer_level", 2);
    }

    private void setPreviousRingerModeSetting(Integer previousRingerLevel) {
        String str = null;
        ContentResolver contentResolver = this.mContext.getContentResolver();
        String str2 = "zen_mode_ringer_level";
        if (previousRingerLevel != null) {
            str = Integer.toString(previousRingerLevel.intValue());
        }
        Global.putString(contentResolver, str2, str);
    }

    private boolean evaluateZenMode(String reason, boolean setRingerMode) {
        if (DEBUG) {
            Log.d(TAG, "evaluateZenMode");
        }
        int zenBefore = this.mZenMode;
        int zen = computeZenMode();
        ZenLog.traceSetZenMode(zen, reason);
        this.mZenMode = zen;
        updateRingerModeAffectedStreams();
        if (this.mZenMode == 2 && "setInterruptionFilter".equals(reason)) {
            Global.putInt(this.mContext.getContentResolver(), "total_silence_mode", 1);
        } else {
            Global.putInt(this.mContext.getContentResolver(), "total_silence_mode", 0);
        }
        setZenModeSetting(this.mZenMode);
        if (setRingerMode) {
            applyZenToRingerMode();
        }
        applyRestrictions();
        if (zen != zenBefore) {
            this.mHandler.postDispatchOnZenModeChanged();
        }
        return true;
    }

    private void updateRingerModeAffectedStreams() {
        if (this.mAudioManager != null) {
            this.mAudioManager.updateRingerModeAffectedStreamsInternal();
        }
    }

    private int computeZenMode() {
        synchronized (this.mConfig) {
            if (this.mConfig == null) {
                return 0;
            } else if (this.mConfig.manualRule != null) {
                int i = this.mConfig.manualRule.zenMode;
                return i;
            } else {
                int zen = 0;
                for (ZenRule automaticRule : this.mConfig.automaticRules.values()) {
                    if (automaticRule.isAutomaticActive() && zenSeverity(automaticRule.zenMode) > zenSeverity(zen)) {
                        zen = automaticRule.zenMode;
                    }
                }
                return zen;
            }
        }
    }

    private void applyRestrictions() {
        boolean zen = this.mZenMode != 0 ? true : DEBUG;
        boolean muteNotifications = (this.mSuppressedEffects & SUPPRESSED_EFFECT_NOTIFICATIONS) != 0 ? true : DEBUG;
        boolean muteCalls = (!zen || this.mConfig.allowCalls || this.mConfig.allowRepeatCallers || Secure.getInt(this.mContext.getContentResolver(), "zen_call_white_list_enabled", 0) == 1) ? (this.mSuppressedEffects & SUPPRESSED_EFFECT_CALLS) != 0 ? true : DEBUG : true;
        boolean muteAlarms = this.mZenMode == 2 ? true : DEBUG;
        applyRestrictions(muteAlarms, 4);
        boolean muteEverything = this.mZenMode == 2 ? Global.getInt(this.mContext.getContentResolver(), "total_silence_mode", 0) == 1 ? true : DEBUG : DEBUG;
        for (int i = 0; i <= 15; i++) {
            if (i == 5) {
                applyRestrictions(!muteNotifications ? muteEverything : true, i);
            } else if (i == 6) {
                applyRestrictions(!muteCalls ? muteEverything : true, i);
            } else if (i == 4) {
                applyRestrictions(muteAlarms | muteEverything, i);
            } else {
                applyRestrictions(muteEverything, i);
            }
        }
    }

    private void applyRestrictions(boolean mute, int usage) {
        int i;
        int i2 = 1;
        String[] exceptionPackages = getExceptionPackages(usage);
        AppOpsManager appOpsManager = this.mAppOps;
        if (mute) {
            i = 1;
        } else {
            i = 0;
        }
        appOpsManager.setRestriction(3, usage, i, exceptionPackages);
        AppOpsManager appOpsManager2 = this.mAppOps;
        if (!mute) {
            i2 = 0;
        }
        appOpsManager2.setRestriction(28, usage, i2, exceptionPackages);
    }

    private String[] getExceptionPackages(int usage) {
        if (4 != usage || this.mZenMode == 2) {
            return null;
        }
        return new String[]{"com.android.deskclock"};
    }

    private void applyZenToRingerMode() {
        if (this.mAudioManager != null) {
            int ringerModeInternal = this.mAudioManager.getRingerModeInternal();
            int newRingerModeInternal = ringerModeInternal;
            switch (this.mZenMode) {
                case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                    if (ringerModeInternal == 0) {
                        newRingerModeInternal = getPreviousRingerModeSetting();
                        setPreviousRingerModeSetting(null);
                        break;
                    }
                    break;
                case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                case com.android.server.wm.WindowManagerService.H.REPORT_LOSING_FOCUS /*3*/:
                    if (ringerModeInternal != 0) {
                        setPreviousRingerModeSetting(Integer.valueOf(ringerModeInternal));
                        break;
                    }
                    break;
            }
        }
    }

    private void dispatchOnConfigChanged() {
        for (Callback callback : this.mCallbacks) {
            callback.onConfigChanged();
        }
    }

    private void dispatchOnPolicyChanged() {
        for (Callback callback : this.mCallbacks) {
            callback.onPolicyChanged();
        }
    }

    private void dispatchOnZenModeChanged() {
        for (Callback callback : this.mCallbacks) {
            callback.onZenModeChanged();
        }
    }

    private ZenModeConfig readDefaultConfig(Resources resources) {
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = resources.getXml(17891333);
            while (autoCloseable.next() != 1) {
                ZenModeConfig config = ZenModeConfig.readXml(autoCloseable, this.mConfigMigration);
                if (config != null) {
                    return config;
                }
            }
            IoUtils.closeQuietly(autoCloseable);
        } catch (Exception e) {
            Log.w(TAG, "Error reading default zen mode config from resource", e);
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
        return new ZenModeConfig();
    }

    private void appendDefaultScheduleRules(ZenModeConfig config) {
        if (config != null) {
            ScheduleInfo weekends = new ScheduleInfo();
            weekends.days = ZenModeConfig.WEEKEND_DAYS;
            weekends.startHour = 22;
            weekends.endHour = 7;
            ZenRule rule2 = new ZenRule();
            rule2.enabled = DEBUG;
            rule2.name = DefaultWeekendsName;
            rule2.conditionId = ZenModeConfig.toScheduleConditionId(weekends);
            rule2.zenMode = 3;
            rule2.component = ScheduleConditionProvider.COMPONENT;
            rule2.id = ZenModeConfig.newRuleId();
            rule2.creationTime = System.currentTimeMillis();
            config.automaticRules.put(rule2.id, rule2);
        }
    }

    private void appendDefaultEventRules(ZenModeConfig config) {
        if (config != null) {
            EventInfo events = new EventInfo();
            events.calendar = null;
            events.reply = 1;
            ZenRule rule = new ZenRule();
            rule.enabled = DEBUG;
            rule.name = DefaultEventsName;
            rule.conditionId = ZenModeConfig.toEventConditionId(events);
            rule.zenMode = 3;
            rule.component = EventConditionProvider.COMPONENT;
            rule.id = ZenModeConfig.newRuleId();
            rule.creationTime = System.currentTimeMillis();
            config.automaticRules.put(rule.id, rule);
        }
    }

    private static int zenSeverity(int zen) {
        switch (zen) {
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                return 1;
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                return 3;
            case com.android.server.wm.WindowManagerService.H.REPORT_LOSING_FOCUS /*3*/:
                return 2;
            default:
                return 0;
        }
    }
}
