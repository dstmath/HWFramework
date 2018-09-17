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
import android.media.AudioAttributes;
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
import android.service.notification.ZenModeConfig.ScheduleInfo;
import android.service.notification.ZenModeConfig.ZenRule;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.util.SparseArray;
import android.util.proto.ProtoOutputStream;
import com.android.internal.logging.MetricsLogger;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IHwZenModeFiltering;
import com.android.server.LocalServices;
import com.android.server.notification.ManagedServices.Config;
import huawei.cust.HwCustUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ZenModeHelper {
    static final boolean DEBUG = Log.isLoggable(TAG, 3);
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
    private final ArrayList<Callback> mCallbacks = new ArrayList();
    private final ZenModeConditions mConditions;
    private ZenModeConfig mConfig;
    private final SparseArray<ZenModeConfig> mConfigs = new SparseArray();
    private final Context mContext;
    private HwCustZenModeHelper mCust = ((HwCustZenModeHelper) HwCustUtils.createObj(HwCustZenModeHelper.class, new Object[0]));
    private final ZenModeConfig mDefaultConfig;
    private final ZenModeFiltering mFiltering;
    private final H mHandler;
    private final Metrics mMetrics = new Metrics(this, null);
    private PackageManager mPm;
    private final RingerModeDelegate mRingerModeDelegate = new RingerModeDelegate(this, null);
    private final Config mServiceConfig;
    private final SettingsObserver mSettingsObserver;
    private long mSuppressedEffects;
    private int mUser = 0;
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

        private final class ConfigMessageData {
            public final ZenModeConfig config;
            public final String reason;
            public final boolean setRingerMode;

            ConfigMessageData(ZenModeConfig config, String reason, boolean setRingerMode) {
                this.config = config;
                this.reason = reason;
                this.setRingerMode = setRingerMode;
            }
        }

        /* synthetic */ H(ZenModeHelper this$0, Looper looper, H -this2) {
            this(looper);
        }

        private H(Looper looper) {
            super(looper);
        }

        private void postDispatchOnZenModeChanged() {
            removeMessages(1);
            sendEmptyMessage(1);
        }

        private void postMetricsTimer() {
            removeMessages(2);
            sendEmptyMessageDelayed(2, METRICS_PERIOD_MS);
        }

        private void postApplyConfig(ZenModeConfig config, String reason, boolean setRingerMode) {
            sendMessage(obtainMessage(4, new ConfigMessageData(config, reason, setRingerMode)));
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ZenModeHelper.this.dispatchOnZenModeChanged();
                    return;
                case 2:
                    ZenModeHelper.this.mMetrics.emit();
                    return;
                case 4:
                    ConfigMessageData applyConfigData = msg.obj;
                    ZenModeHelper.this.applyConfig(applyConfigData.config, applyConfigData.reason, applyConfigData.setRingerMode);
                    return;
                default:
                    return;
            }
        }
    }

    private final class Metrics extends Callback {
        private static final String COUNTER_PREFIX = "dnd_mode_";
        private static final long MINIMUM_LOG_PERIOD_MS = 60000;
        private long mBeginningMs;
        private int mPreviousZenMode;

        /* synthetic */ Metrics(ZenModeHelper this$0, Metrics -this1) {
            this();
        }

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
            if (this.mPreviousZenMode != ZenModeHelper.this.mZenMode || since > 60000) {
                if (this.mPreviousZenMode != -1) {
                    MetricsLogger.count(ZenModeHelper.this.mContext, COUNTER_PREFIX + this.mPreviousZenMode, (int) since);
                }
                this.mPreviousZenMode = ZenModeHelper.this.mZenMode;
                this.mBeginningMs = now;
            }
        }
    }

    private final class RingerModeDelegate implements android.media.AudioManagerInternal.RingerModeDelegate {
        /* synthetic */ RingerModeDelegate(ZenModeHelper this$0, RingerModeDelegate -this1) {
            this();
        }

        private RingerModeDelegate() {
        }

        public String toString() {
            return ZenModeHelper.TAG;
        }

        public int onSetRingerModeInternal(int ringerModeOld, int ringerModeNew, String caller, int ringerModeExternal, VolumePolicy policy) {
            boolean isChange = ringerModeOld != ringerModeNew;
            int ringerModeExternalOut = ringerModeNew;
            int newZen = -1;
            switch (ringerModeNew) {
                case 0:
                    if (isChange && policy.doNotDisturbWhenSilent) {
                        if (!(ZenModeHelper.this.mZenMode == 2 || ZenModeHelper.this.mZenMode == 3)) {
                            newZen = 3;
                        }
                        ZenModeHelper.this.setPreviousRingerModeSetting(Integer.valueOf(ringerModeOld));
                        break;
                    }
                case 1:
                case 2:
                    if (!isChange || ringerModeOld != 0 || (ZenModeHelper.this.mZenMode != 2 && ZenModeHelper.this.mZenMode != 3)) {
                        int -get3 = ZenModeHelper.this.mZenMode;
                        break;
                    }
                    newZen = 0;
                    break;
                    break;
            }
            if (!(!isChange && newZen == -1 && ringerModeExternal == ringerModeNew)) {
                ZenLog.traceSetRingerModeInternal(ringerModeOld, ringerModeNew, caller, ringerModeExternal, ringerModeNew);
            }
            return ringerModeNew;
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
                    }
                    ringerModeInternalOut = ringerModeInternal;
                    break;
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
            streams |= 38;
            if (ZenModeHelper.this.mZenMode == 2) {
                return streams | 8;
            }
            return streams & -9;
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri ZEN_MODE = Global.getUriFor("zen_mode");

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
        this.mHandler = new H(this, looper, null);
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
            boolean matchesCallFilter;
            if (filter != null) {
                matchesCallFilter = filter.matchesCallFilter(this.mContext, this.mZenMode, this.mConfig, userHandle, extras, validator, contactsTimeoutMs, timeoutAffinity);
                return matchesCallFilter;
            }
            matchesCallFilter = ZenModeFiltering.matchesCallFilter(this.mContext, this.mZenMode, this.mConfig, userHandle, extras, validator, contactsTimeoutMs, timeoutAffinity);
            return matchesCallFilter;
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

    public boolean shouldSuppressWhenScreenOff() {
        boolean z;
        synchronized (this.mConfig) {
            z = this.mConfig.allowWhenScreenOff ^ 1;
        }
        return z;
    }

    public boolean shouldSuppressWhenScreenOn() {
        boolean z;
        synchronized (this.mConfig) {
            z = this.mConfig.allowWhenScreenOn ^ 1;
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
            String packageName;
            String flattenToShortString;
            if (name != null) {
                packageName = name.getPackageName();
            } else {
                packageName = null;
            }
            StringBuilder append = new StringBuilder().append("listener:");
            if (name != null) {
                flattenToShortString = name.flattenToShortString();
            } else {
                flattenToShortString = null;
            }
            setManualZenMode(newZen, null, packageName, append.append(flattenToShortString).toString());
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

    /* JADX WARNING: Missing block: B:10:0x0015, code:
            if (r0 != null) goto L_0x001b;
     */
    /* JADX WARNING: Missing block: B:11:0x0017, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:16:0x001f, code:
            if (canManageAutomaticZenRule(r0) == false) goto L_0x0026;
     */
    /* JADX WARNING: Missing block: B:18:0x0025, code:
            return createAutomaticZenRule(r0);
     */
    /* JADX WARNING: Missing block: B:19:0x0026, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public AutomaticZenRule getAutomaticZenRule(String id) {
        synchronized (this.mConfig) {
            if (this.mConfig == null) {
                return null;
            }
            ZenRule rule = (ZenRule) this.mConfig.automaticRules.get(id);
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
                return false;
            }
            if (DEBUG) {
                Log.d(TAG, "updateAutomaticZenRule zenRule=" + automaticZenRule + " reason=" + reason);
            }
            ZenModeConfig newConfig = this.mConfig.copy();
            if (ruleId == null) {
                throw new IllegalArgumentException("Rule doesn't exist");
            }
            ZenRule rule = (ZenRule) newConfig.automaticRules.get(ruleId);
            if (rule == null || (canManageAutomaticZenRule(rule) ^ 1) != 0) {
                throw new SecurityException("Cannot update rules not owned by your condition provider");
            }
            populateZenRule(automaticZenRule, rule, false);
            newConfig.automaticRules.put(ruleId, rule);
            boolean configLocked = setConfigLocked(newConfig, reason, true);
            return configLocked;
        }
    }

    public boolean removeAutomaticZenRule(String id, String reason) {
        synchronized (this.mConfig) {
            if (this.mConfig == null) {
                return false;
            }
            ZenModeConfig newConfig = this.mConfig.copy();
            ZenRule rule = (ZenRule) newConfig.automaticRules.get(id);
            if (rule == null) {
                return false;
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
                return false;
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

    /* JADX WARNING: Missing block: B:4:0x000c, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean canManageAutomaticZenRule(ZenRule rule) {
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
            rule.snoozing = false;
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

    public void setManualZenMode(int zenMode, Uri conditionId, String caller, String reason) {
        setManualZenMode(zenMode, conditionId, reason, caller, true);
    }

    /* JADX WARNING: Missing block: B:34:0x00a2, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setManualZenMode(int zenMode, Uri conditionId, String reason, String caller, boolean setRingerMode) {
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
                    newRule.enabler = caller;
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

    void dump(ProtoOutputStream proto) {
        proto.write(1168231104513L, this.mZenMode);
        synchronized (this.mConfig) {
            if (this.mConfig.manualRule != null) {
                proto.write(2259152797698L, this.mConfig.manualRule.toString());
            }
            for (ZenRule rule : this.mConfig.automaticRules.values()) {
                if (rule.enabled && rule.condition.state == 1 && (rule.snoozing ^ 1) != 0) {
                    proto.write(2259152797698L, rule.toString());
                }
            }
            proto.write(1159641169925L, this.mConfig.toNotificationPolicy().toString());
            proto.write(1112396529667L, this.mSuppressedEffects);
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mZenMode=");
        pw.println(Global.zenModeToString(this.mZenMode));
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
        pw.printf("allow(calls=%b,callsFrom=%s,repeatCallers=%b,messages=%b,messagesFrom=%s,events=%b,reminders=%b,whenScreenOff=%b,whenScreenOn=%b)\n", new Object[]{Boolean.valueOf(config.allowCalls), ZenModeConfig.sourceToString(config.allowCallsFrom), Boolean.valueOf(config.allowRepeatCallers), Boolean.valueOf(config.allowMessages), ZenModeConfig.sourceToString(config.allowMessagesFrom), Boolean.valueOf(config.allowEvents), Boolean.valueOf(config.allowReminders), Boolean.valueOf(config.allowWhenScreenOff), Boolean.valueOf(config.allowWhenScreenOn)});
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
        ZenModeConfig config = ZenModeConfig.readXml(parser);
        if (config != null) {
            if (forRestore) {
                if (config.user == 0) {
                    config.manualRule = null;
                    long time = System.currentTimeMillis();
                    if (config.automaticRules != null) {
                        for (ZenRule automaticRule : config.automaticRules.values()) {
                            automaticRule.snoozing = false;
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

    private void cleanUpZenRules() {
        long currentTime = System.currentTimeMillis();
        synchronized (this.mConfig) {
            ZenModeConfig newConfig = this.mConfig.copy();
            if (newConfig.automaticRules != null) {
                for (int i = newConfig.automaticRules.size() - 1; i >= 0; i--) {
                    ZenRule rule = (ZenRule) newConfig.automaticRules.get(newConfig.automaticRules.keyAt(i));
                    if (259200000 < currentTime - rule.creationTime) {
                        try {
                            this.mPm.getPackageInfo(rule.component.getPackageName(), DumpState.DUMP_CHANGES);
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
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        boolean isVDriveModeFlay = false;
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

    private boolean setConfigLocked(ZenModeConfig config, String reason, boolean setRingerMode) {
        long identity = Binder.clearCallingIdentity();
        if (config != null) {
            try {
                if ((config.isValid() ^ 1) == 0) {
                    if (config.user != this.mUser) {
                        this.mConfigs.put(config.user, config);
                        if (DEBUG) {
                            Log.d(TAG, "setConfigLocked: store config for user " + config.user);
                        }
                        Binder.restoreCallingIdentity(identity);
                        return true;
                    }
                    this.mConditions.evaluateConfig(config, false);
                    this.mConfigs.put(config.user, config);
                    if (DEBUG) {
                        Log.d(TAG, "setConfigLocked reason=" + reason, new Throwable());
                    }
                    ZenLog.traceConfig(reason, this.mConfig, config);
                    boolean policyChanged = Objects.equals(getNotificationPolicy(this.mConfig), getNotificationPolicy(config)) ^ 1;
                    if (!config.equals(this.mConfig)) {
                        dispatchOnConfigChanged();
                    }
                    if (policyChanged) {
                        dispatchOnPolicyChanged();
                    }
                    this.mConfig = config;
                    String val = Integer.toString(config.hashCode());
                    boolean isVDriverMode = "conditionChanged".equals(reason) ? isVDriverMode() : false;
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
        return false;
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
        boolean zen = this.mZenMode != 0;
        boolean muteNotifications = (this.mSuppressedEffects & 1) != 0 || this.mZenMode == 3 || this.mZenMode == 2;
        boolean muteCalls = (!zen || (this.mConfig.allowCalls ^ 1) == 0 || (this.mConfig.allowRepeatCallers ^ 1) == 0 || Secure.getInt(this.mContext.getContentResolver(), "zen_call_white_list_enabled", 0) == 1) ? (this.mSuppressedEffects & 2) != 0 : true;
        boolean muteAlarms = this.mZenMode == 2;
        boolean muteEverything = this.mZenMode == 2 ? Global.getInt(this.mContext.getContentResolver(), "total_silence_mode", 0) == 1 : false;
        for (int usage : AudioAttributes.SDK_USAGES) {
            int suppressionBehavior = AudioAttributes.SUPPRESSIBLE_USAGES.get(usage);
            if (suppressionBehavior == 3) {
                applyRestrictions(false, usage);
            } else if (suppressionBehavior == 1) {
                applyRestrictions(!muteNotifications ? muteEverything : true, usage);
            } else if (suppressionBehavior == 2) {
                applyRestrictions(!muteCalls ? muteEverything : true, usage);
            } else if (usage == 4) {
                applyRestrictions(!muteAlarms ? muteEverything : true, usage);
            } else {
                applyRestrictions(muteEverything, usage);
            }
        }
    }

    private void applyRestrictions(boolean mute, int usage) {
        int i = 1;
        String[] exceptionPackages = getExceptionPackages(usage);
        this.mAppOps.setRestriction(3, usage, mute ? 1 : 0, exceptionPackages);
        AppOpsManager appOpsManager = this.mAppOps;
        if (!mute) {
            i = 0;
        }
        appOpsManager.setRestriction(28, usage, i, exceptionPackages);
    }

    private String[] getExceptionPackages(int usage) {
        List<String> listPkgs = new ArrayList();
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

    private void applyZenToRingerMode() {
        if (this.mAudioManager != null) {
            int ringerModeInternal = this.mAudioManager.getRingerModeInternal();
            int newRingerModeInternal = ringerModeInternal;
            switch (this.mZenMode) {
                case 0:
                case 1:
                    if (ringerModeInternal == 0) {
                        newRingerModeInternal = getPreviousRingerModeSetting();
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
            autoCloseable = resources.getXml(18284549);
            while (autoCloseable.next() != 1) {
                ZenModeConfig config = ZenModeConfig.readXml(autoCloseable);
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
            rule2.enabled = false;
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
            rule.enabled = false;
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
}
