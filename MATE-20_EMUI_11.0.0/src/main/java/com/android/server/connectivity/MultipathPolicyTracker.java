package com.android.server.connectivity;

import android.app.usage.NetworkStatsManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkIdentity;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkRequest;
import android.net.NetworkTemplate;
import android.net.Uri;
import android.os.BestClock;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DebugUtils;
import android.util.Range;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.LocalServices;
import com.android.server.connectivity.MultipathPolicyTracker;
import com.android.server.job.controllers.JobStatus;
import com.android.server.net.NetworkPolicyManagerInternal;
import com.android.server.net.NetworkStatsManagerInternal;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MultipathPolicyTracker {
    private static final boolean DBG;
    private static final int DOMESTIC_BETA_VERSION = 3;
    private static final int OPQUOTA_USER_SETTING_DIVIDER = 20;
    private static String TAG = MultipathPolicyTracker.class.getSimpleName();
    private ConnectivityManager mCM;
    private final Clock mClock;
    private final ConfigChangeReceiver mConfigChangeReceiver;
    private final Context mContext;
    private final Dependencies mDeps;
    private final Handler mHandler;
    private ConnectivityManager.NetworkCallback mMobileNetworkCallback;
    private final ConcurrentHashMap<Network, MultipathTracker> mMultipathTrackers;
    private NetworkPolicyManager mNPM;
    private NetworkPolicyManager.Listener mPolicyListener;
    private final ContentResolver mResolver;
    @VisibleForTesting
    final ContentObserver mSettingsObserver;
    private NetworkStatsManager mStatsManager;

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.logsystem.usertype", 0) == 3) {
            z = true;
        }
        DBG = z;
    }

    public static class Dependencies {
        public Clock getClock() {
            return new BestClock(ZoneOffset.UTC, new Clock[]{SystemClock.currentNetworkTimeClock(), Clock.systemUTC()});
        }
    }

    public MultipathPolicyTracker(Context ctx, Handler handler) {
        this(ctx, handler, new Dependencies());
    }

    public MultipathPolicyTracker(Context ctx, Handler handler, Dependencies deps) {
        this.mMultipathTrackers = new ConcurrentHashMap<>();
        this.mContext = ctx;
        this.mHandler = handler;
        this.mClock = deps.getClock();
        this.mDeps = deps;
        this.mResolver = this.mContext.getContentResolver();
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mConfigChangeReceiver = new ConfigChangeReceiver();
    }

    public void start() {
        this.mCM = (ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class);
        this.mNPM = (NetworkPolicyManager) this.mContext.getSystemService(NetworkPolicyManager.class);
        this.mStatsManager = (NetworkStatsManager) this.mContext.getSystemService(NetworkStatsManager.class);
        registerTrackMobileCallback();
        registerNetworkPolicyListener();
        this.mResolver.registerContentObserver(Settings.Global.getUriFor("network_default_daily_multipath_quota_bytes"), false, this.mSettingsObserver);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mContext.registerReceiverAsUser(this.mConfigChangeReceiver, UserHandle.ALL, intentFilter, null, this.mHandler);
    }

    public void shutdown() {
        maybeUnregisterTrackMobileCallback();
        unregisterNetworkPolicyListener();
        for (MultipathTracker t : this.mMultipathTrackers.values()) {
            t.shutdown();
        }
        this.mMultipathTrackers.clear();
        this.mResolver.unregisterContentObserver(this.mSettingsObserver);
        this.mContext.unregisterReceiver(this.mConfigChangeReceiver);
    }

    public Integer getMultipathPreference(Network network) {
        MultipathTracker t;
        if (network == null || (t = this.mMultipathTrackers.get(network)) == null) {
            return null;
        }
        return Integer.valueOf(t.getMultipathPreference());
    }

    /* access modifiers changed from: package-private */
    public class MultipathTracker {
        private long mMultipathBudget;
        private NetworkCapabilities mNetworkCapabilities;
        private final NetworkTemplate mNetworkTemplate;
        private long mQuota;
        private final NetworkStatsManager.UsageCallback mUsageCallback;
        final Network network;
        final int subId;
        final String subscriberId;

        public MultipathTracker(final Network network2, NetworkCapabilities nc) {
            this.network = network2;
            this.mNetworkCapabilities = new NetworkCapabilities(nc);
            try {
                this.subId = Integer.parseInt(nc.getNetworkSpecifier().toString());
                TelephonyManager tele = (TelephonyManager) MultipathPolicyTracker.this.mContext.getSystemService(TelephonyManager.class);
                if (tele != null) {
                    TelephonyManager tele2 = tele.createForSubscriptionId(this.subId);
                    if (tele2 != null) {
                        this.subscriberId = tele2.getSubscriberId();
                        String str = this.subscriberId;
                        this.mNetworkTemplate = new NetworkTemplate(1, str, new String[]{str}, (String) null, -1, -1, 0);
                        this.mUsageCallback = new NetworkStatsManager.UsageCallback(MultipathPolicyTracker.this) {
                            /* class com.android.server.connectivity.MultipathPolicyTracker.MultipathTracker.AnonymousClass1 */

                            @Override // android.app.usage.NetworkStatsManager.UsageCallback
                            public void onThresholdReached(int networkType, String subscriberId) {
                                if (MultipathPolicyTracker.DBG) {
                                    String str = MultipathPolicyTracker.TAG;
                                    Slog.d(str, "onThresholdReached for network " + network2);
                                }
                                MultipathTracker.this.updateMultipathBudget();
                            }
                        };
                        if (MultipathPolicyTracker.DBG) {
                            Slog.d(MultipathPolicyTracker.TAG, "MultipathTracker instance created");
                        }
                        updateMultipathBudget();
                        return;
                    }
                    throw new IllegalStateException(String.format("Can't get TelephonyManager for subId %d", Integer.valueOf(this.subId)));
                }
                throw new IllegalStateException(String.format("Missing TelephonyManager", new Object[0]));
            } catch (ClassCastException | NullPointerException | NumberFormatException e) {
                throw new IllegalStateException(String.format("Can't get subId from mobile network %s (%s): %s", network2, nc, e.getMessage()));
            }
        }

        public void setNetworkCapabilities(NetworkCapabilities nc) {
            this.mNetworkCapabilities = new NetworkCapabilities(nc);
        }

        private long getDailyNonDefaultDataUsage() {
            ZonedDateTime end = ZonedDateTime.ofInstant(MultipathPolicyTracker.this.mClock.instant(), ZoneId.systemDefault());
            long bytes = getNetworkTotalBytes(end.truncatedTo(ChronoUnit.DAYS).toInstant().toEpochMilli(), end.toInstant().toEpochMilli());
            if (MultipathPolicyTracker.DBG) {
                String str = MultipathPolicyTracker.TAG;
                Slog.d(str, "Non-default data usage: " + bytes);
            }
            return bytes;
        }

        private long getNetworkTotalBytes(long start, long end) {
            try {
                return ((NetworkStatsManagerInternal) LocalServices.getService(NetworkStatsManagerInternal.class)).getNetworkTotalBytes(this.mNetworkTemplate, start, end);
            } catch (RuntimeException e) {
                Slog.w(MultipathPolicyTracker.TAG, "Failed to get data usage");
                return -1;
            }
        }

        private NetworkIdentity getTemplateMatchingNetworkIdentity(NetworkCapabilities nc) {
            return new NetworkIdentity(0, 0, this.subscriberId, (String) null, !nc.hasCapability(18), !nc.hasCapability(11), false);
        }

        private long getRemainingDailyBudget(long limitBytes, Range<ZonedDateTime> cycle) {
            long start = cycle.getLower().toInstant().toEpochMilli();
            long end = cycle.getUpper().toInstant().toEpochMilli();
            long totalBytes = getNetworkTotalBytes(start, end);
            long remainingBytes = 0;
            if (totalBytes != -1) {
                remainingBytes = Math.max(0L, limitBytes - totalBytes);
            }
            return remainingBytes / Math.max(1L, (((end - MultipathPolicyTracker.this.mClock.millis()) - 1) / TimeUnit.DAYS.toMillis(1)) + 1);
        }

        private long getUserPolicyOpportunisticQuotaBytes() {
            long policyBytes;
            long minQuota = JobStatus.NO_LATEST_RUNTIME;
            NetworkIdentity identity = getTemplateMatchingNetworkIdentity(this.mNetworkCapabilities);
            NetworkPolicy[] policies = MultipathPolicyTracker.this.mNPM.getNetworkPolicies();
            for (NetworkPolicy policy : policies) {
                if (policy.hasCycle() && policy.template.matches(identity)) {
                    long cycleStart = ((ZonedDateTime) ((Range) policy.cycleIterator().next()).getLower()).toInstant().toEpochMilli();
                    long activeWarning = MultipathPolicyTracker.getActiveWarning(policy, cycleStart);
                    if (activeWarning == -1) {
                        policyBytes = MultipathPolicyTracker.getActiveLimit(policy, cycleStart);
                    } else {
                        policyBytes = activeWarning;
                    }
                    if (!(policyBytes == -1 || policyBytes == -1)) {
                        minQuota = Math.min(minQuota, getRemainingDailyBudget(policyBytes, (Range) policy.cycleIterator().next()));
                    }
                }
            }
            if (minQuota == JobStatus.NO_LATEST_RUNTIME) {
                return -1;
            }
            return minQuota / 20;
        }

        /* access modifiers changed from: package-private */
        public void updateMultipathBudget() {
            long quota = ((NetworkPolicyManagerInternal) LocalServices.getService(NetworkPolicyManagerInternal.class)).getSubscriptionOpportunisticQuota(this.network, 2);
            if (MultipathPolicyTracker.DBG) {
                String str = MultipathPolicyTracker.TAG;
                Slog.d(str, "Opportunistic quota from data plan: " + quota + " bytes");
            }
            if (quota == -1) {
                quota = getUserPolicyOpportunisticQuotaBytes();
                if (MultipathPolicyTracker.DBG) {
                    String str2 = MultipathPolicyTracker.TAG;
                    Slog.d(str2, "Opportunistic quota from user policy: " + quota + " bytes");
                }
            }
            if (quota == -1) {
                quota = MultipathPolicyTracker.this.getDefaultDailyMultipathQuotaBytes();
                if (MultipathPolicyTracker.DBG) {
                    String str3 = MultipathPolicyTracker.TAG;
                    Slog.d(str3, "Setting quota: " + quota + " bytes");
                }
            }
            if (!haveMultipathBudget() || quota != this.mQuota) {
                this.mQuota = quota;
                long usage = getDailyNonDefaultDataUsage();
                long budget = 0;
                if (usage != -1) {
                    budget = Math.max(0L, quota - usage);
                }
                if (budget > NetworkStatsManager.MIN_THRESHOLD_BYTES) {
                    if (MultipathPolicyTracker.DBG) {
                        String str4 = MultipathPolicyTracker.TAG;
                        Slog.d(str4, "Setting callback for " + budget + " bytes on network " + this.network);
                    }
                    registerUsageCallback(budget);
                    return;
                }
                maybeUnregisterUsageCallback();
            } else if (MultipathPolicyTracker.DBG) {
                String str5 = MultipathPolicyTracker.TAG;
                Slog.d(str5, "Quota still " + quota + ", not updating.");
            }
        }

        public int getMultipathPreference() {
            if (haveMultipathBudget()) {
                return 3;
            }
            return 0;
        }

        public long getQuota() {
            return this.mQuota;
        }

        public long getMultipathBudget() {
            return this.mMultipathBudget;
        }

        private boolean haveMultipathBudget() {
            return this.mMultipathBudget > 0;
        }

        private void registerUsageCallback(long budget) {
            maybeUnregisterUsageCallback();
            String str = MultipathPolicyTracker.TAG;
            Slog.i(str, "registerUsageCallback, budget is " + budget);
            MultipathPolicyTracker.this.mStatsManager.registerUsageCallback(this.mNetworkTemplate, 0, budget, this.mUsageCallback, MultipathPolicyTracker.this.mHandler);
            this.mMultipathBudget = budget;
        }

        private void maybeUnregisterUsageCallback() {
            if (haveMultipathBudget()) {
                String str = MultipathPolicyTracker.TAG;
                Slog.i(str, "Unregistering callback, budget was " + this.mMultipathBudget);
                MultipathPolicyTracker.this.mStatsManager.unregisterUsageCallback(this.mUsageCallback);
                this.mMultipathBudget = 0;
            }
        }

        /* access modifiers changed from: package-private */
        public void shutdown() {
            maybeUnregisterUsageCallback();
        }
    }

    /* access modifiers changed from: private */
    public static long getActiveWarning(NetworkPolicy policy, long cycleStart) {
        if (policy.lastWarningSnooze < cycleStart) {
            return policy.warningBytes;
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public static long getActiveLimit(NetworkPolicy policy, long cycleStart) {
        if (policy.lastLimitSnooze < cycleStart) {
            return policy.limitBytes;
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getDefaultDailyMultipathQuotaBytes() {
        String setting = Settings.Global.getString(this.mContext.getContentResolver(), "network_default_daily_multipath_quota_bytes");
        if (setting != null) {
            try {
                return Long.parseLong(setting);
            } catch (NumberFormatException e) {
            }
        }
        return (long) this.mContext.getResources().getInteger(17694852);
    }

    private void registerTrackMobileCallback() {
        NetworkRequest request = new NetworkRequest.Builder().addCapability(12).addTransportType(0).build();
        this.mMobileNetworkCallback = new ConnectivityManager.NetworkCallback() {
            /* class com.android.server.connectivity.MultipathPolicyTracker.AnonymousClass1 */

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onCapabilitiesChanged(Network network, NetworkCapabilities nc) {
                MultipathTracker existing = (MultipathTracker) MultipathPolicyTracker.this.mMultipathTrackers.get(network);
                if (existing != null) {
                    if (MultipathPolicyTracker.DBG) {
                        String str = MultipathPolicyTracker.TAG;
                        Slog.d(str, "onCapabilitiesChanged tracking mobile network " + network);
                    }
                    existing.setNetworkCapabilities(nc);
                    existing.updateMultipathBudget();
                    return;
                }
                try {
                    MultipathPolicyTracker.this.mMultipathTrackers.put(network, new MultipathTracker(network, nc));
                } catch (IllegalStateException e) {
                    String str2 = MultipathPolicyTracker.TAG;
                    Slog.e(str2, "Can't track mobile network " + network);
                }
                if (MultipathPolicyTracker.DBG) {
                    String str3 = MultipathPolicyTracker.TAG;
                    Slog.d(str3, "Tracking mobile network " + network);
                }
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLost(Network network) {
                MultipathTracker existing = (MultipathTracker) MultipathPolicyTracker.this.mMultipathTrackers.get(network);
                if (existing != null) {
                    existing.shutdown();
                    MultipathPolicyTracker.this.mMultipathTrackers.remove(network);
                }
                if (MultipathPolicyTracker.DBG) {
                    String str = MultipathPolicyTracker.TAG;
                    Slog.d(str, "No longer tracking mobile network " + network);
                }
            }
        };
        this.mCM.registerNetworkCallback(request, this.mMobileNetworkCallback, this.mHandler);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAllMultipathBudgets() {
        if (DBG) {
            Slog.d(TAG, "updateAllMultipathBudgets.");
        }
        for (MultipathTracker t : this.mMultipathTrackers.values()) {
            t.updateMultipathBudget();
        }
    }

    private void maybeUnregisterTrackMobileCallback() {
        ConnectivityManager.NetworkCallback networkCallback = this.mMobileNetworkCallback;
        if (networkCallback != null) {
            this.mCM.unregisterNetworkCallback(networkCallback);
        }
        this.mMobileNetworkCallback = null;
    }

    private void registerNetworkPolicyListener() {
        this.mPolicyListener = new NetworkPolicyManager.Listener() {
            /* class com.android.server.connectivity.MultipathPolicyTracker.AnonymousClass2 */

            public /* synthetic */ void lambda$onMeteredIfacesChanged$0$MultipathPolicyTracker$2() {
                MultipathPolicyTracker.this.updateAllMultipathBudgets();
            }

            public void onMeteredIfacesChanged(String[] meteredIfaces) {
                MultipathPolicyTracker.this.mHandler.post(new Runnable() {
                    /* class com.android.server.connectivity.$$Lambda$MultipathPolicyTracker$2$dvyDLfu9d6g2XoEdL3QMHx7ut6k */

                    @Override // java.lang.Runnable
                    public final void run() {
                        MultipathPolicyTracker.AnonymousClass2.this.lambda$onMeteredIfacesChanged$0$MultipathPolicyTracker$2();
                    }
                });
            }
        };
        this.mNPM.registerListener(this.mPolicyListener);
    }

    private void unregisterNetworkPolicyListener() {
        this.mNPM.unregisterListener(this.mPolicyListener);
    }

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            Slog.wtf(MultipathPolicyTracker.TAG, "Should never be reached.");
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            if (!Settings.Global.getUriFor("network_default_daily_multipath_quota_bytes").equals(uri)) {
                String str = MultipathPolicyTracker.TAG;
                Slog.wtf(str, "Unexpected settings observation: " + uri);
            }
            if (MultipathPolicyTracker.DBG) {
                Slog.d(MultipathPolicyTracker.TAG, "Settings change: updating budgets.");
            }
            MultipathPolicyTracker.this.updateAllMultipathBudgets();
        }
    }

    /* access modifiers changed from: private */
    public final class ConfigChangeReceiver extends BroadcastReceiver {
        private ConfigChangeReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (MultipathPolicyTracker.DBG) {
                Slog.d(MultipathPolicyTracker.TAG, "Configuration change: updating budgets.");
            }
            MultipathPolicyTracker.this.updateAllMultipathBudgets();
        }
    }

    public void dump(IndentingPrintWriter pw) {
        pw.println("MultipathPolicyTracker:");
        pw.increaseIndent();
        for (MultipathTracker t : this.mMultipathTrackers.values()) {
            pw.println(String.format("Network %s: quota %d, budget %d. Preference: %s", t.network, Long.valueOf(t.getQuota()), Long.valueOf(t.getMultipathBudget()), DebugUtils.flagsToString(ConnectivityManager.class, "MULTIPATH_PREFERENCE_", t.getMultipathPreference())));
        }
        pw.decreaseIndent();
    }
}
