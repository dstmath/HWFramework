package com.android.server.notification;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.AutomaticZenRule;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioManagerInternal;
import android.media.VolumePolicy;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.notification.ZenModeConfig;
import android.util.AndroidRuntimeException;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.notification.ManagedServices;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import huawei.cust.HwCustUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ZenModeHelper {
    static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int RULE_INSTANCE_GRACE_PERIOD = 259200000;
    public static final long SUPPRESSED_EFFECT_ALL = 3;
    public static final long SUPPRESSED_EFFECT_CALLS = 2;
    public static final long SUPPRESSED_EFFECT_NOTIFICATIONS = 1;
    static final String TAG = "ZenModeHelper";
    @VisibleForTesting
    protected final AppOpsManager mAppOps;
    @VisibleForTesting
    protected AudioManagerInternal mAudioManager;
    private final ArrayList<Callback> mCallbacks = new ArrayList<>();
    private final ZenModeConditions mConditions;
    @VisibleForTesting
    protected ZenModeConfig mConfig;
    private final SparseArray<ZenModeConfig> mConfigs = new SparseArray<>();
    /* access modifiers changed from: private */
    public final Context mContext;
    private HwCustZenModeHelper mCust = ((HwCustZenModeHelper) HwCustUtils.createObj(HwCustZenModeHelper.class, new Object[0]));
    protected ZenModeConfig mDefaultConfig;
    protected String mDefaultRuleEventsName;
    protected String mDefaultRuleEveryNightName;
    private final ZenModeFiltering mFiltering;
    /* access modifiers changed from: private */
    public final H mHandler;
    @VisibleForTesting
    protected boolean mIsBootComplete;
    /* access modifiers changed from: private */
    public final Metrics mMetrics = new Metrics();
    @VisibleForTesting
    protected final NotificationManager mNotificationManager;
    protected PackageManager mPm;
    protected final RingerModeDelegate mRingerModeDelegate = new RingerModeDelegate();
    private final ManagedServices.Config mServiceConfig;
    private final SettingsObserver mSettingsObserver;
    private long mSuppressedEffects;
    private int mUser = 0;
    @VisibleForTesting
    protected int mZenMode;

    public static class Callback {
        /* access modifiers changed from: package-private */
        public void onConfigChanged() {
        }

        /* access modifiers changed from: package-private */
        public void onZenModeChanged() {
        }

        /* access modifiers changed from: package-private */
        public void onPolicyChanged() {
        }
    }

    private final class H extends Handler {
        private static final long METRICS_PERIOD_MS = 21600000;
        private static final int MSG_APPLY_CONFIG = 4;
        private static final int MSG_DISPATCH = 1;
        private static final int MSG_METRICS = 2;

        private final class ConfigMessageData {
            public final ZenModeConfig config;
            public final String reason;
            public final boolean setRingerMode;

            ConfigMessageData(ZenModeConfig config2, String reason2, boolean setRingerMode2) {
                this.config = config2;
                this.reason = reason2;
                this.setRingerMode = setRingerMode2;
            }
        }

        private H(Looper looper) {
            super(looper);
        }

        /* access modifiers changed from: private */
        public void postDispatchOnZenModeChanged() {
            removeMessages(1);
            sendEmptyMessage(1);
        }

        /* access modifiers changed from: private */
        public void postMetricsTimer() {
            removeMessages(2);
            sendEmptyMessageDelayed(2, METRICS_PERIOD_MS);
        }

        /* access modifiers changed from: private */
        public void postApplyConfig(ZenModeConfig config, String reason, boolean setRingerMode) {
            sendMessage(obtainMessage(4, new ConfigMessageData(config, reason, setRingerMode)));
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 4) {
                switch (i) {
                    case 1:
                        ZenModeHelper.this.dispatchOnZenModeChanged();
                        return;
                    case 2:
                        ZenModeHelper.this.mMetrics.emit();
                        return;
                    default:
                        return;
                }
            } else {
                ConfigMessageData applyConfigData = (ConfigMessageData) msg.obj;
                ZenModeHelper.this.applyConfig(applyConfigData.config, applyConfigData.reason, applyConfigData.setRingerMode);
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

        /* access modifiers changed from: package-private */
        public void onZenModeChanged() {
            emit();
        }

        /* access modifiers changed from: private */
        public void emit() {
            ZenModeHelper.this.mHandler.postMetricsTimer();
            long now = SystemClock.elapsedRealtime();
            long since = now - this.mBeginningMs;
            if (this.mPreviousZenMode != ZenModeHelper.this.mZenMode || since > 60000) {
                if (this.mPreviousZenMode != -1) {
                    Context access$600 = ZenModeHelper.this.mContext;
                    MetricsLogger.count(access$600, COUNTER_PREFIX + this.mPreviousZenMode, (int) since);
                }
                this.mPreviousZenMode = ZenModeHelper.this.mZenMode;
                this.mBeginningMs = now;
            }
        }
    }

    @VisibleForTesting
    protected final class RingerModeDelegate implements AudioManagerInternal.RingerModeDelegate {
        protected RingerModeDelegate() {
        }

        public String toString() {
            return ZenModeHelper.TAG;
        }

        public int onSetRingerModeInternal(int ringerModeOld, int ringerModeNew, String caller, int ringerModeExternal, VolumePolicy policy) {
            boolean isChange = ringerModeOld != ringerModeNew;
            int ringerModeExternalOut = ringerModeNew;
            if (ZenModeHelper.this.mZenMode == 0 || (ZenModeHelper.this.mZenMode == 1 && !ZenModeConfig.areAllPriorityOnlyNotificationZenSoundsMuted(ZenModeHelper.this.mConfig))) {
                ZenModeHelper.this.setPreviousRingerModeSetting(Integer.valueOf(ringerModeNew));
            }
            int newZen = -1;
            switch (ringerModeNew) {
                case 0:
                    if (isChange && policy.doNotDisturbWhenSilent) {
                        if (ZenModeHelper.this.mZenMode == 0) {
                            newZen = 1;
                        }
                        ZenModeHelper.this.setPreviousRingerModeSetting(Integer.valueOf(ringerModeOld));
                        break;
                    }
                case 1:
                case 2:
                    if (!isChange || ringerModeOld != 0 || (ZenModeHelper.this.mZenMode != 2 && ZenModeHelper.this.mZenMode != 3 && (ZenModeHelper.this.mZenMode != 1 || !ZenModeConfig.areAllPriorityOnlyNotificationZenSoundsMuted(ZenModeHelper.this.mConfig)))) {
                        int i = ZenModeHelper.this.mZenMode;
                        break;
                    } else {
                        newZen = 0;
                        break;
                    }
            }
            if (!(!isChange && newZen == -1 && ringerModeExternal == ringerModeExternalOut)) {
                ZenLog.traceSetRingerModeInternal(ringerModeOld, ringerModeNew, caller, ringerModeExternal, ringerModeExternalOut);
            }
            return ringerModeExternalOut;
        }

        public int onSetRingerModeExternal(int ringerModeOld, int ringerModeNew, String caller, int ringerModeInternal, VolumePolicy policy) {
            int ringerModeInternalOut = ringerModeNew;
            boolean isChange = ringerModeOld != ringerModeNew;
            if (ringerModeInternal == 1) {
            }
            switch (ringerModeNew) {
                case 0:
                    if (isChange) {
                        if (ZenModeHelper.this.mZenMode == 0) {
                            break;
                        }
                    } else {
                        ringerModeInternalOut = ringerModeInternal;
                        break;
                    }
                    break;
                case 1:
                case 2:
                    if (ZenModeHelper.this.mZenMode != 0) {
                        break;
                    }
                    break;
            }
            ZenLog.traceSetRingerModeExternal(ringerModeOld, ringerModeNew, caller, ringerModeInternal, ringerModeInternalOut);
            return ringerModeInternalOut;
        }

        public boolean canVolumeDownEnterSilent() {
            return ZenModeHelper.this.mZenMode == 0;
        }

        public int getRingerModeAffectedStreams(int streams) {
            int streams2;
            int streams3 = streams | 38;
            if (ZenModeHelper.this.mZenMode == 2) {
                streams2 = streams3 | 8;
            } else {
                streams2 = streams3 & -9;
            }
            if (ZenModeHelper.this.mZenMode != 1 || !ZenModeConfig.areAllPriorityOnlyNotificationZenSoundsMuted(ZenModeHelper.this.mConfig)) {
                return streams2 | 2;
            }
            return streams2 & -3;
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri ZEN_MODE = Settings.Global.getUriFor("zen_mode");

        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void observe() {
            ZenModeHelper.this.mContext.getContentResolver().registerContentObserver(this.ZEN_MODE, false, this);
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

    public ZenModeHelper(Context context, Looper looper, ConditionProviders conditionProviders) {
        this.mContext = context;
        this.mHandler = new H(looper);
        addCallback(this.mMetrics);
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mNotificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
        this.mDefaultConfig = new ZenModeConfig();
        setDefaultZenRules(this.mContext);
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
            HwServiceFactory.IHwZenModeFiltering filter = HwServiceFactory.getHwZenModeFiltering();
            if (filter != null) {
                boolean matchesCallFilter = filter.matchesCallFilter(this.mContext, this.mZenMode, this.mConfig, userHandle, extras, validator, contactsTimeoutMs, timeoutAffinity);
                return matchesCallFilter;
            }
            boolean matchesCallFilter2 = ZenModeFiltering.matchesCallFilter(this.mContext, this.mZenMode, this.mConfig, userHandle, extras, validator, contactsTimeoutMs, timeoutAffinity);
            return matchesCallFilter2;
        }
    }

    public boolean isCall(NotificationRecord record) {
        return this.mFiltering.isCall(record);
    }

    public void recordCaller(NotificationRecord record) {
        this.mFiltering.recordCall(record);
    }

    public boolean shouldIntercept(NotificationRecord record) {
        boolean shouldIntercept;
        synchronized (this.mConfig) {
            shouldIntercept = this.mFiltering.shouldIntercept(this.mZenMode, this.mConfig, record);
        }
        return shouldIntercept;
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
        this.mIsBootComplete = true;
        showZenUpgradeNotification(this.mZenMode);
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
            ZenModeConfig config = this.mConfigs.get(user);
            if (config == null) {
                if (DEBUG) {
                    Log.d(TAG, reason + " generating default config for user " + user);
                }
                config = this.mDefaultConfig.copy();
                config.user = user;
            }
            if (user != 0 && "onUserSwitched".equals(reason)) {
                ZenModeConfig config_owner = this.mConfigs.get(0);
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
            String packageName = name != null ? name.getPackageName() : null;
            StringBuilder sb = new StringBuilder();
            sb.append("listener:");
            sb.append(name != null ? name.flattenToShortString() : null);
            setManualZenMode(newZen, null, packageName, sb.toString());
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

    public List<ZenModeConfig.ZenRule> getZenRules() {
        List<ZenModeConfig.ZenRule> rules = new ArrayList<>();
        synchronized (this.mConfig) {
            if (this.mConfig == null) {
                return rules;
            }
            for (ZenModeConfig.ZenRule rule : this.mConfig.automaticRules.values()) {
                if (canManageAutomaticZenRule(rule)) {
                    rules.add(rule);
                }
            }
            return rules;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0017, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        if (canManageAutomaticZenRule(r1) == false) goto L_0x0023;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0022, code lost:
        return createAutomaticZenRule(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0023, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0015, code lost:
        if (r1 != null) goto L_0x0018;
     */
    public AutomaticZenRule getAutomaticZenRule(String id) {
        synchronized (this.mConfig) {
            if (this.mConfig == null) {
                return null;
            }
            ZenModeConfig.ZenRule rule = (ZenModeConfig.ZenRule) this.mConfig.automaticRules.get(id);
        }
    }

    public String addAutomaticZenRule(AutomaticZenRule automaticZenRule, String reason) {
        String str;
        if (!isSystemRule(automaticZenRule)) {
            ServiceInfo owner = getServiceInfo(automaticZenRule.getOwner());
            if (owner != null) {
                int ruleInstanceLimit = -1;
                if (owner.metaData != null) {
                    ruleInstanceLimit = owner.metaData.getInt("android.service.zen.automatic.ruleInstanceLimit", -1);
                }
                if (ruleInstanceLimit > 0 && ruleInstanceLimit < getCurrentInstanceCount(automaticZenRule.getOwner()) + 1) {
                    throw new IllegalArgumentException("Rule instance limit exceeded");
                }
            } else {
                throw new IllegalArgumentException("Owner is not a condition provider service");
            }
        }
        synchronized (this.mConfig) {
            if (this.mConfig != null) {
                if (DEBUG) {
                    Log.d(TAG, "addAutomaticZenRule rule= " + automaticZenRule + " reason=" + reason);
                }
                ZenModeConfig newConfig = this.mConfig.copy();
                ZenModeConfig.ZenRule rule = new ZenModeConfig.ZenRule();
                populateZenRule(automaticZenRule, rule, true);
                newConfig.automaticRules.put(rule.id, rule);
                if (setConfigLocked(newConfig, reason, true)) {
                    str = rule.id;
                } else {
                    throw new AndroidRuntimeException("Could not create rule");
                }
            } else {
                throw new AndroidRuntimeException("Could not create rule");
            }
        }
        return str;
    }

    public boolean updateAutomaticZenRule(String ruleId, AutomaticZenRule automaticZenRule, String reason) {
        synchronized (this.mConfig) {
            if (this.mConfig == null) {
                return false;
            }
            if (DEBUG) {
                Log.d(TAG, "updateAutomaticZenRule zenRule=" + automaticZenRule + " reason=" + reason);
            }
            ZenModeConfig newConfig = this.mConfig.copy();
            if (ruleId != null) {
                ZenModeConfig.ZenRule rule = (ZenModeConfig.ZenRule) newConfig.automaticRules.get(ruleId);
                if (rule == null || !canManageAutomaticZenRule(rule)) {
                    throw new SecurityException("Cannot update rules not owned by your condition provider");
                }
                populateZenRule(automaticZenRule, rule, false);
                newConfig.automaticRules.put(ruleId, rule);
                boolean configLocked = setConfigLocked(newConfig, reason, true);
                return configLocked;
            }
            throw new IllegalArgumentException("Rule doesn't exist");
        }
    }

    public boolean removeAutomaticZenRule(String id, String reason) {
        synchronized (this.mConfig) {
            if (this.mConfig == null) {
                return false;
            }
            ZenModeConfig newConfig = this.mConfig.copy();
            ZenModeConfig.ZenRule rule = (ZenModeConfig.ZenRule) newConfig.automaticRules.get(id);
            if (rule == null) {
                return false;
            }
            if (canManageAutomaticZenRule(rule)) {
                newConfig.automaticRules.remove(id);
                if (DEBUG) {
                    Log.d(TAG, "removeZenRule zenRule=" + id + " reason=" + reason);
                }
                boolean configLocked = setConfigLocked(newConfig, reason, true);
                return configLocked;
            }
            throw new SecurityException("Cannot delete rules not owned by your condition provider");
        }
    }

    public boolean removeAutomaticZenRules(String packageName, String reason) {
        synchronized (this.mConfig) {
            if (this.mConfig == null) {
                return false;
            }
            ZenModeConfig newConfig = this.mConfig.copy();
            for (int i = newConfig.automaticRules.size() - 1; i >= 0; i--) {
                ZenModeConfig.ZenRule rule = (ZenModeConfig.ZenRule) newConfig.automaticRules.get(newConfig.automaticRules.keyAt(i));
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
            for (ZenModeConfig.ZenRule rule : this.mConfig.automaticRules.values()) {
                if (rule.component != null && rule.component.equals(owner)) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean canManageAutomaticZenRule(ZenModeConfig.ZenRule rule) {
        int callingUid = Binder.getCallingUid();
        if (callingUid == 0 || callingUid == 1000 || this.mContext.checkCallingPermission("android.permission.MANAGE_NOTIFICATIONS") == 0) {
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
        return false;
    }

    public void setDefaultZenRules(Context context) {
        this.mDefaultConfig = readDefaultConfig(context.getResources());
        appendDefaultRules(this.mDefaultConfig);
    }

    private void appendDefaultRules(ZenModeConfig config) {
        getDefaultRuleNames();
        appendDefaultEveryNightRule(config);
        appendDefaultEventRules(config);
    }

    private boolean ruleValuesEqual(AutomaticZenRule rule, ZenModeConfig.ZenRule defaultRule) {
        boolean z = false;
        if (rule == null || defaultRule == null) {
            return false;
        }
        if (rule.getInterruptionFilter() == NotificationManager.zenModeToInterruptionFilter(defaultRule.zenMode) && rule.getConditionId().equals(defaultRule.conditionId) && rule.getOwner().equals(defaultRule.component)) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void updateDefaultZenRules() {
        ZenModeConfig configDefaultRules = new ZenModeConfig();
        appendDefaultRules(configDefaultRules);
        for (String ruleId : ZenModeConfig.DEFAULT_RULE_IDS) {
            AutomaticZenRule currRule = getAutomaticZenRule(ruleId);
            ZenModeConfig.ZenRule defaultRule = (ZenModeConfig.ZenRule) configDefaultRules.automaticRules.get(ruleId);
            if (ruleValuesEqual(currRule, defaultRule) && !defaultRule.name.equals(currRule.getName()) && canManageAutomaticZenRule(defaultRule)) {
                if (DEBUG) {
                    Slog.d(TAG, "Locale change - updating default zen rule name from " + currRule.getName() + " to " + defaultRule.name);
                }
                AutomaticZenRule defaultAutoRule = createAutomaticZenRule(defaultRule);
                defaultAutoRule.setEnabled(currRule.isEnabled());
                updateAutomaticZenRule(ruleId, defaultAutoRule, "locale changed");
            }
        }
    }

    private boolean isSystemRule(AutomaticZenRule rule) {
        return PackageManagerService.PLATFORM_PACKAGE_NAME.equals(rule.getOwner().getPackageName());
    }

    private ServiceInfo getServiceInfo(ComponentName owner) {
        Intent queryIntent = new Intent();
        queryIntent.setComponent(owner);
        List<ResolveInfo> installedServices = this.mPm.queryIntentServicesAsUser(queryIntent, 132, UserHandle.getCallingUserId());
        if (installedServices != null) {
            int count = installedServices.size();
            for (int i = 0; i < count; i++) {
                ServiceInfo info = installedServices.get(i).serviceInfo;
                if (this.mServiceConfig.bindPermission.equals(info.permission)) {
                    return info;
                }
            }
        }
        return null;
    }

    private void populateZenRule(AutomaticZenRule automaticZenRule, ZenModeConfig.ZenRule rule, boolean isNew) {
        if (isNew) {
            rule.id = ZenModeConfig.newRuleId();
            rule.creationTime = System.currentTimeMillis();
            rule.component = automaticZenRule.getOwner();
        }
        if (rule.enabled != automaticZenRule.isEnabled()) {
            rule.snoozing = false;
        }
        rule.name = automaticZenRule.getName();
        rule.condition = null;
        rule.conditionId = automaticZenRule.getConditionId();
        rule.enabled = automaticZenRule.isEnabled();
        rule.zenMode = NotificationManager.zenModeFromInterruptionFilter(automaticZenRule.getInterruptionFilter(), 0);
    }

    /* access modifiers changed from: protected */
    public AutomaticZenRule createAutomaticZenRule(ZenModeConfig.ZenRule rule) {
        AutomaticZenRule automaticZenRule = new AutomaticZenRule(rule.name, rule.component, rule.conditionId, NotificationManager.zenModeToInterruptionFilter(rule.zenMode), rule.enabled, rule.creationTime);
        return automaticZenRule;
    }

    public void setManualZenMode(int zenMode, Uri conditionId, String caller, String reason) {
        setManualZenMode(zenMode, conditionId, reason, caller, true);
        Settings.Global.putInt(this.mContext.getContentResolver(), "show_zen_settings_suggestion", 0);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0096, code lost:
        r0 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0097, code lost:
        return;
     */
    private void setManualZenMode(int zenMode, Uri conditionId, String reason, String caller, boolean setRingerMode) {
        synchronized (this.mConfig) {
            if (this.mConfig != null) {
                if (Settings.Global.isValidZenMode(zenMode)) {
                    if (DEBUG) {
                        Log.d(TAG, "setManualZenMode " + Settings.Global.zenModeToString(zenMode) + " conditionId=" + conditionId + " reason=" + reason + " setRingerMode=" + setRingerMode);
                    }
                    ZenModeConfig newConfig = this.mConfig.copy();
                    if (zenMode == 0) {
                        newConfig.manualRule = null;
                        for (ZenModeConfig.ZenRule automaticRule : newConfig.automaticRules.values()) {
                            if (automaticRule.isAutomaticActive()) {
                                automaticRule.snoozing = true;
                            }
                        }
                    } else {
                        ZenModeConfig.ZenRule newRule = new ZenModeConfig.ZenRule();
                        newRule.enabled = true;
                        newRule.zenMode = zenMode;
                        newRule.conditionId = conditionId;
                        newRule.enabler = caller;
                        newConfig.manualRule = newRule;
                    }
                    if ("restore_default".equals(reason)) {
                        newConfig = this.mDefaultConfig;
                        setConfigLocked(newConfig, reason, true);
                    } else {
                        setConfigLocked(newConfig, reason, setRingerMode);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(ProtoOutputStream proto) {
        proto.write(1159641169921L, this.mZenMode);
        synchronized (this.mConfig) {
            if (this.mConfig.manualRule != null) {
                this.mConfig.manualRule.writeToProto(proto, 2246267895810L);
            }
            for (ZenModeConfig.ZenRule rule : this.mConfig.automaticRules.values()) {
                if (rule.enabled && rule.condition.state == 1 && !rule.snoozing) {
                    rule.writeToProto(proto, 2246267895810L);
                }
            }
            this.mConfig.toNotificationPolicy().writeToProto(proto, 1146756268037L);
            proto.write(1120986464259L, this.mSuppressedEffects);
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mZenMode=");
        pw.println(Settings.Global.zenModeToString(this.mZenMode));
        int N = this.mConfigs.size();
        for (int i = 0; i < N; i++) {
            dump(pw, prefix, "mConfigs[u=" + this.mConfigs.keyAt(i) + "]", this.mConfigs.valueAt(i));
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
        int i = 0;
        pw.printf("allow(alarms=%b,media=%b,system=%b,calls=%b,callsFrom=%s,repeatCallers=%b,messages=%b,messagesFrom=%s,events=%b,reminders=%b)\n", new Object[]{Boolean.valueOf(config.allowAlarms), Boolean.valueOf(config.allowMedia), Boolean.valueOf(config.allowSystem), Boolean.valueOf(config.allowCalls), ZenModeConfig.sourceToString(config.allowCallsFrom), Boolean.valueOf(config.allowRepeatCallers), Boolean.valueOf(config.allowMessages), ZenModeConfig.sourceToString(config.allowMessagesFrom), Boolean.valueOf(config.allowEvents), Boolean.valueOf(config.allowReminders)});
        pw.printf(" disallow(visualEffects=%s)\n", new Object[]{Integer.valueOf(config.suppressedVisualEffects)});
        pw.print(prefix);
        pw.print("  manualRule=");
        pw.println(config.manualRule);
        if (!config.automaticRules.isEmpty()) {
            int N = config.automaticRules.size();
            while (true) {
                int i2 = i;
                if (i2 < N) {
                    pw.print(prefix);
                    pw.print(i2 == 0 ? "  automaticRules=" : "                 ");
                    pw.println(config.automaticRules.valueAt(i2));
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        }
    }

    public void readXml(XmlPullParser parser, boolean forRestore) throws XmlPullParserException, IOException {
        int zenCallWhiteListEnable;
        int zenMsgWhiteListEnable;
        ZenModeConfig config = ZenModeConfig.readXml(parser);
        String reason = "readXml";
        if (config != null) {
            if (forRestore) {
                if (config.user == 0) {
                    config.manualRule = null;
                } else {
                    return;
                }
            }
            boolean resetToDefaultRules = true;
            long time = System.currentTimeMillis();
            if (config.automaticRules != null && config.automaticRules.size() > 0) {
                for (ZenModeConfig.ZenRule automaticRule : config.automaticRules.values()) {
                    if (forRestore) {
                        automaticRule.snoozing = false;
                        automaticRule.condition = null;
                        automaticRule.creationTime = time;
                    }
                    resetToDefaultRules &= !automaticRule.enabled;
                }
            }
            if (config.version < 8) {
                Log.i(TAG, "readXml: zenCallWhiteListEnable=" + zenCallWhiteListEnable + ";zenMsgWhiteListEnable=" + zenMsgWhiteListEnable);
                if (1 == zenCallWhiteListEnable) {
                    config.allowCalls = true;
                    config.allowCallsFrom = 0;
                }
                if (1 == zenMsgWhiteListEnable) {
                    config.allowMessages = true;
                    config.allowMessagesFrom = 0;
                }
            }
            if (config.version < 8 || forRestore) {
                Settings.Global.putInt(this.mContext.getContentResolver(), "show_zen_upgrade_notification", 1);
                if (resetToDefaultRules) {
                    config.automaticRules = new ArrayMap();
                    appendDefaultRules(config);
                    reason = reason + ", reset to default rules";
                }
            } else {
                Settings.Global.putInt(this.mContext.getContentResolver(), "zen_settings_updated", 1);
            }
            String reason2 = reason;
            if (DEBUG) {
                Log.d(TAG, reason2);
            }
            synchronized (this.mConfig) {
                setConfigLocked(config, reason2);
            }
            String str = reason2;
        }
    }

    public void writeXml(XmlSerializer out, boolean forBackup, Integer version) throws IOException {
        int N = this.mConfigs.size();
        for (int i = 0; i < N; i++) {
            if (!forBackup || this.mConfigs.keyAt(i) == 0) {
                this.mConfigs.valueAt(i).writeXml(out, version);
            }
        }
    }

    public NotificationManager.Policy getNotificationPolicy() {
        return getNotificationPolicy(this.mConfig);
    }

    private static NotificationManager.Policy getNotificationPolicy(ZenModeConfig config) {
        if (config == null) {
            return null;
        }
        return config.toNotificationPolicy();
    }

    public void setNotificationPolicy(NotificationManager.Policy policy) {
        if (policy != null && this.mConfig != null) {
            synchronized (this.mConfig) {
                ZenModeConfig newConfig = this.mConfig.copy();
                newConfig.applyNotificationPolicy(policy);
                setConfigLocked(newConfig, "setNotificationPolicy");
            }
        }
    }

    private void cleanUpZenRules() {
        long currentTime = System.currentTimeMillis();
        synchronized (this.mConfig) {
            ZenModeConfig newConfig = this.mConfig.copy();
            if (newConfig.automaticRules != null) {
                for (int i = newConfig.automaticRules.size() - 1; i >= 0; i--) {
                    ZenModeConfig.ZenRule rule = (ZenModeConfig.ZenRule) newConfig.automaticRules.get(newConfig.automaticRules.keyAt(i));
                    if (259200000 < currentTime - rule.creationTime) {
                        try {
                            this.mPm.getPackageInfo(rule.component.getPackageName(), DumpState.DUMP_CHANGES);
                        } catch (PackageManager.NameNotFoundException e) {
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

    public void setConfig(ZenModeConfig config, String reason) {
        synchronized (this.mConfig) {
            setConfigLocked(config, reason);
        }
    }

    private boolean isVDriverMode() {
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (activityManager == null) {
            return false;
        }
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        boolean isVDriveModeFlay = false;
        Iterator<ActivityManager.RunningAppProcessInfo> it = appProcesses.iterator();
        while (true) {
            if (it.hasNext()) {
                if ("com.huawei.vdrive".equals(it.next().processName)) {
                    isVDriveModeFlay = true;
                    Log.d(TAG, "HwVDrive is running .");
                    break;
                }
            } else {
                break;
            }
        }
        Log.d(TAG, "isVDriverMode return value isVDriveModeFlay:" + isVDriveModeFlay);
        return isVDriveModeFlay;
    }

    private boolean setConfigLocked(ZenModeConfig config, String reason, boolean setRingerMode) {
        boolean isVDriverMode;
        long identity = Binder.clearCallingIdentity();
        if (config != null) {
            try {
                if (config.isValid()) {
                    if (config.user != this.mUser) {
                        this.mConfigs.put(config.user, config);
                        if (DEBUG) {
                            Log.d(TAG, "setConfigLocked: store config for user " + config.user);
                        }
                        return true;
                    }
                    this.mConditions.evaluateConfig(config, false);
                    this.mConfigs.put(config.user, config);
                    if (DEBUG) {
                        Log.d(TAG, "setConfigLocked reason=" + reason, new Throwable());
                    }
                    ZenLog.traceConfig(reason, this.mConfig, config);
                    boolean policyChanged = !Objects.equals(getNotificationPolicy(this.mConfig), getNotificationPolicy(config));
                    if (!config.equals(this.mConfig)) {
                        dispatchOnConfigChanged();
                    }
                    if (policyChanged) {
                        dispatchOnPolicyChanged();
                    }
                    this.mConfig = config;
                    String val = Integer.toString(config.hashCode());
                    Log.e(TAG, "ZenModeHelper isVDriverMode:" + isVDriverMode + ",timecondition:" + "conditionChanged".equals(reason));
                    if (!isVDriverMode) {
                        Settings.Global.putString(this.mContext.getContentResolver(), "zen_mode_config_etag", val);
                        if (!evaluateZenMode(reason, setRingerMode)) {
                            applyRestrictions();
                        }
                    }
                    this.mConditions.evaluateConfig(config, true);
                    this.mHandler.postApplyConfig(config, reason, setRingerMode);
                    Binder.restoreCallingIdentity(identity);
                    return true;
                }
            } catch (SecurityException e) {
                Log.wtf(TAG, "Invalid rule in config", e);
                return false;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
        Log.w(TAG, "Invalid config in setConfigLocked; " + config);
        Binder.restoreCallingIdentity(identity);
        return false;
    }

    /* access modifiers changed from: private */
    public void applyConfig(ZenModeConfig config, String reason, boolean setRingerMode) {
        Settings.Global.putString(this.mContext.getContentResolver(), "zen_mode_config_etag", Integer.toString(config.hashCode()));
        if (!evaluateZenMode(reason, setRingerMode)) {
            applyRestrictions();
        }
        this.mConditions.evaluateConfig(config, true);
    }

    /* access modifiers changed from: private */
    public int getZenModeSetting() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "zen_mode", 0);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void setZenModeSetting(int zen) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "zen_mode", zen);
        showZenUpgradeNotification(zen);
        SystemProperties.set("persist.sys.zen_mode", Integer.toString(zen));
    }

    private int getPreviousRingerModeSetting() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "zen_mode_ringer_level", 2);
    }

    /* access modifiers changed from: private */
    public void setPreviousRingerModeSetting(Integer previousRingerLevel) {
        Settings.Global.putString(this.mContext.getContentResolver(), "zen_mode_ringer_level", previousRingerLevel == null ? null : Integer.toString(previousRingerLevel.intValue()));
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean evaluateZenMode(String reason, boolean setRingerMode) {
        if (DEBUG) {
            Log.d(TAG, "evaluateZenMode");
        }
        int zenBefore = this.mZenMode;
        int zen = computeZenMode();
        ZenLog.traceSetZenMode(zen, reason);
        this.mZenMode = zen;
        if ((this.mZenMode == 2 || this.mZenMode == 1) && "setInterruptionFilter".equals(reason)) {
            Settings.Global.putInt(this.mContext.getContentResolver(), "total_silence_mode", 1);
        } else {
            Settings.Global.putInt(this.mContext.getContentResolver(), "total_silence_mode", 0);
        }
        setZenModeSetting(this.mZenMode);
        updateRingerModeAffectedStreams();
        if (setRingerMode && zen != zenBefore) {
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
        if (this.mConfig == null) {
            return 0;
        }
        synchronized (this.mConfig) {
            if (this.mConfig.manualRule != null) {
                int i = this.mConfig.manualRule.zenMode;
                return i;
            }
            int zen = 0;
            for (ZenModeConfig.ZenRule automaticRule : this.mConfig.automaticRules.values()) {
                if (automaticRule.isAutomaticActive() && zenSeverity(automaticRule.zenMode) > zenSeverity(zen)) {
                    if (Settings.Global.getInt(this.mContext.getContentResolver(), "zen_settings_suggestion_viewed", 1) == 0) {
                        Settings.Global.putInt(this.mContext.getContentResolver(), "show_zen_settings_suggestion", 1);
                    }
                    zen = automaticRule.zenMode;
                }
            }
            return zen;
        }
    }

    private void getDefaultRuleNames() {
        this.mDefaultRuleEveryNightName = this.mContext.getResources().getString(17041437);
        this.mDefaultRuleEventsName = this.mContext.getResources().getString(17041436);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void applyRestrictions() {
        boolean zenSilence;
        char c;
        boolean zenPriorityOnly = this.mZenMode == 1;
        boolean zenSilence2 = this.mZenMode == 2;
        boolean zenAlarmsOnly = this.mZenMode == 3;
        boolean muteNotifications = (this.mSuppressedEffects & 1) != 0 || this.mZenMode == 3 || this.mZenMode == 2;
        boolean muteCalls = zenAlarmsOnly || (zenPriorityOnly && !this.mConfig.allowCalls && !this.mConfig.allowRepeatCallers && Settings.Secure.getInt(this.mContext.getContentResolver(), "zen_call_white_list_enabled", 0) != 1) || (this.mSuppressedEffects & 2) != 0;
        boolean muteAlarms = zenPriorityOnly && !this.mConfig.allowAlarms;
        boolean muteMedia = zenPriorityOnly && !this.mConfig.allowMedia;
        boolean muteSystem = zenAlarmsOnly || (zenPriorityOnly && !this.mConfig.allowSystem);
        boolean muteEverything = zenSilence2 || (zenPriorityOnly && ZenModeConfig.areAllZenBehaviorSoundsMuted(this.mConfig));
        boolean realMuteEverything = Settings.Global.getInt(this.mContext.getContentResolver(), "total_silence_mode", 0) == 1;
        int[] iArr = AudioAttributes.SDK_USAGES;
        int length = iArr.length;
        int i = 0;
        while (i < length) {
            int usage = iArr[i];
            int suppressionBehavior = AudioAttributes.SUPPRESSIBLE_USAGES.get(usage);
            boolean zenPriorityOnly2 = zenPriorityOnly;
            if (suppressionBehavior == 3) {
                applyRestrictions(false, usage);
            } else {
                boolean z = true;
                if (suppressionBehavior == 1) {
                    if (!muteNotifications && !muteEverything) {
                        z = false;
                    }
                    applyRestrictions(z, usage);
                } else if (suppressionBehavior == 2) {
                    applyRestrictions(muteCalls || muteEverything, usage);
                } else if (usage == 4) {
                    applyRestrictions(muteAlarms || muteEverything, usage);
                } else if (suppressionBehavior == 5) {
                    applyRestrictions(muteMedia || (muteEverything && realMuteEverything), usage);
                } else {
                    if (suppressionBehavior != 6) {
                        zenSilence = zenSilence2;
                        c = 3;
                        applyRestrictions(muteEverything, usage);
                    } else if (usage == 13) {
                        zenSilence = zenSilence2;
                        applyRestrictions(muteSystem || muteEverything, usage, 28);
                        c = 3;
                        applyRestrictions(false, usage, 3);
                    } else {
                        zenSilence = zenSilence2;
                        boolean z2 = false;
                        c = 3;
                        if (muteSystem || muteEverything) {
                            z2 = true;
                        }
                        applyRestrictions(z2, usage);
                    }
                    i++;
                    char c2 = c;
                    zenPriorityOnly = zenPriorityOnly2;
                    zenSilence2 = zenSilence;
                }
            }
            zenSilence = zenSilence2;
            c = 3;
            i++;
            char c22 = c;
            zenPriorityOnly = zenPriorityOnly2;
            zenSilence2 = zenSilence;
        }
        boolean z3 = zenSilence2;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void applyRestrictions(boolean mute, int usage, int code) {
        String[] exceptionPackages = getExceptionPackages(usage);
        if (Process.myUid() == 1000) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mAppOps.setRestriction(code, usage, mute, exceptionPackages);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void applyRestrictions(boolean mute, int usage) {
        applyRestrictions(mute, usage, 3);
        applyRestrictions(mute, usage, 28);
    }

    private String[] getExceptionPackages(int usage) {
        List<String> listPkgs = new ArrayList<>();
        if (!(this.mCust == null || this.mCust.getWhiteApps(this.mContext) == null)) {
            listPkgs.addAll(Arrays.asList(this.mCust.getWhiteApps(this.mContext)));
        }
        if (4 == usage && this.mZenMode != 2) {
            listPkgs.add("com.android.deskclock");
        }
        if (listPkgs.size() == 0) {
            return null;
        }
        String[] whiteApps = new String[listPkgs.size()];
        listPkgs.toArray(whiteApps);
        return whiteApps;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void applyZenToRingerMode() {
        if (this.mAudioManager != null) {
            int ringerModeInternal = this.mAudioManager.getRingerModeInternal();
            int i = ringerModeInternal;
            switch (this.mZenMode) {
                case 0:
                    if (ringerModeInternal == 0) {
                        int newRingerModeInternal = getPreviousRingerModeSetting();
                        setPreviousRingerModeSetting(null);
                        break;
                    }
                    break;
                case 2:
                case 3:
                    if (ringerModeInternal != 0) {
                        setPreviousRingerModeSetting(Integer.valueOf(ringerModeInternal));
                        break;
                    }
                    break;
            }
        }
    }

    private void dispatchOnConfigChanged() {
        Iterator<Callback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onConfigChanged();
        }
    }

    private void dispatchOnPolicyChanged() {
        Iterator<Callback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onPolicyChanged();
        }
    }

    /* access modifiers changed from: private */
    public void dispatchOnZenModeChanged() {
        Iterator<Callback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onZenModeChanged();
        }
    }

    private ZenModeConfig readDefaultConfig(Resources resources) {
        XmlResourceParser parser = null;
        try {
            parser = resources.getXml(18284551);
            while (parser.next() != 1) {
                ZenModeConfig config = ZenModeConfig.readXml(parser);
                if (config != null) {
                    IoUtils.closeQuietly(parser);
                    return config;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error reading default zen mode config from resource", e);
        } catch (Throwable th) {
            IoUtils.closeQuietly(null);
            throw th;
        }
        IoUtils.closeQuietly(parser);
        return new ZenModeConfig();
    }

    private void appendDefaultEveryNightRule(ZenModeConfig config) {
        if (config != null) {
            ZenModeConfig.ScheduleInfo weeknights = new ZenModeConfig.ScheduleInfo();
            weeknights.days = ZenModeConfig.WEEKEND_DAYS;
            weeknights.startHour = 22;
            weeknights.endHour = 7;
            weeknights.exitAtAlarm = false;
            ZenModeConfig.ZenRule rule = new ZenModeConfig.ZenRule();
            rule.enabled = false;
            rule.name = this.mDefaultRuleEveryNightName;
            rule.conditionId = ZenModeConfig.toScheduleConditionId(weeknights);
            rule.zenMode = 1;
            rule.component = ScheduleConditionProvider.COMPONENT;
            rule.id = "EVERY_NIGHT_DEFAULT_RULE";
            rule.creationTime = System.currentTimeMillis();
            config.automaticRules.put(rule.id, rule);
        }
    }

    private void appendDefaultEventRules(ZenModeConfig config) {
        if (config != null) {
            ZenModeConfig.EventInfo events = new ZenModeConfig.EventInfo();
            events.calendar = null;
            events.reply = 1;
            ZenModeConfig.ZenRule rule = new ZenModeConfig.ZenRule();
            rule.enabled = false;
            rule.name = this.mDefaultRuleEventsName;
            rule.conditionId = ZenModeConfig.toEventConditionId(events);
            rule.zenMode = 1;
            rule.component = EventConditionProvider.COMPONENT;
            rule.id = "EVENTS_DEFAULT_RULE";
            rule.creationTime = System.currentTimeMillis();
            config.automaticRules.put(rule.id, rule);
        }
    }

    private static int zenSeverity(int zen) {
        switch (zen) {
            case 1:
                return 1;
            case 2:
                return 3;
            case 3:
                return 2;
            default:
                return 0;
        }
    }

    private void showZenUpgradeNotification(int zen) {
        if ((!this.mIsBootComplete || zen == 0 || Settings.Global.getInt(this.mContext.getContentResolver(), "show_zen_upgrade_notification", 0) == 0) ? false : true) {
            Settings.Global.putInt(this.mContext.getContentResolver(), "show_zen_upgrade_notification", 0);
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public Notification createZenUpgradeNotification() {
        Bundle extras = new Bundle();
        extras.putString("android.substName", this.mContext.getResources().getString(17040132));
        int title = 17041447;
        int content = 17041446;
        int drawable = 17302797;
        if (NotificationManager.Policy.areAllVisualEffectsSuppressed(getNotificationPolicy().suppressedVisualEffects)) {
            title = 17041449;
            content = 17041448;
            drawable = 17302356;
        }
        Intent onboardingIntent = new Intent("android.settings.ZEN_MODE_ONBOARDING");
        onboardingIntent.addFlags(268468224);
        return new Notification.Builder(this.mContext, SystemNotificationChannels.DO_NOT_DISTURB).setAutoCancel(true).setSmallIcon(17302750).setLargeIcon(Icon.createWithResource(this.mContext, drawable)).setContentTitle(this.mContext.getResources().getString(title)).setContentText(this.mContext.getResources().getString(content)).setContentIntent(PendingIntent.getActivity(this.mContext, 0, onboardingIntent, 134217728)).setAutoCancel(true).setLocalOnly(true).addExtras(extras).setStyle(new Notification.BigTextStyle()).build();
    }
}
