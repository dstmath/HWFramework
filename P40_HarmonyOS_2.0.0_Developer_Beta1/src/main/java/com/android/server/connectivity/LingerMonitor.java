package com.android.server.connectivity;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkCapabilities;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.MessageUtils;
import com.android.server.connectivity.NetworkNotificationManager;
import java.util.HashMap;

public class LingerMonitor {
    @VisibleForTesting
    public static final Intent CELLULAR_SETTINGS = new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
    private static final boolean DBG = true;
    public static final int DEFAULT_NOTIFICATION_DAILY_LIMIT = 3;
    public static final long DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS = 60000;
    @VisibleForTesting
    public static final int NOTIFY_TYPE_NONE = 0;
    public static final int NOTIFY_TYPE_NOTIFICATION = 1;
    public static final int NOTIFY_TYPE_TOAST = 2;
    private static final String TAG = LingerMonitor.class.getSimpleName();
    private static final HashMap<String, Integer> TRANSPORT_NAMES = makeTransportToNameMap();
    private static final boolean VDBG = false;
    private static SparseArray<String> sNotifyTypeNames = MessageUtils.findMessageNames(new Class[]{LingerMonitor.class}, new String[]{"NOTIFY_TYPE_"});
    private final Context mContext;
    private final int mDailyLimit;
    private final SparseBooleanArray mEverNotified = new SparseBooleanArray();
    private long mFirstNotificationMillis;
    private long mLastNotificationMillis;
    private int mNotificationCounter;
    private final SparseIntArray mNotifications = new SparseIntArray();
    private final NetworkNotificationManager mNotifier;
    private final long mRateLimitMillis;

    public LingerMonitor(Context context, NetworkNotificationManager notifier, int dailyLimit, long rateLimitMillis) {
        this.mContext = context;
        this.mNotifier = notifier;
        this.mDailyLimit = dailyLimit;
        this.mRateLimitMillis = rateLimitMillis;
        this.mLastNotificationMillis = -rateLimitMillis;
    }

    private static HashMap<String, Integer> makeTransportToNameMap() {
        SparseArray<String> numberToName = MessageUtils.findMessageNames(new Class[]{NetworkCapabilities.class}, new String[]{"TRANSPORT_"});
        HashMap<String, Integer> nameToNumber = new HashMap<>();
        for (int i = 0; i < numberToName.size(); i++) {
            nameToNumber.put(numberToName.valueAt(i), Integer.valueOf(numberToName.keyAt(i)));
        }
        return nameToNumber;
    }

    private static boolean hasTransport(NetworkAgentInfo nai, int transport) {
        return nai.networkCapabilities.hasTransport(transport);
    }

    private int getNotificationSource(NetworkAgentInfo toNai) {
        for (int i = 0; i < this.mNotifications.size(); i++) {
            if (this.mNotifications.valueAt(i) == toNai.network.netId) {
                return this.mNotifications.keyAt(i);
            }
        }
        return 0;
    }

    private boolean everNotified(NetworkAgentInfo nai) {
        return this.mEverNotified.get(nai.network.netId, false);
    }

    @VisibleForTesting
    public boolean isNotificationEnabled(NetworkAgentInfo fromNai, NetworkAgentInfo toNai) {
        String[] notifySwitches = this.mContext.getResources().getStringArray(17236040);
        for (String notifySwitch : notifySwitches) {
            if (!TextUtils.isEmpty(notifySwitch)) {
                String[] transports = notifySwitch.split("-", 2);
                if (transports.length != 2) {
                    Log.e(TAG, "Invalid network switch notification configuration: " + notifySwitch);
                } else {
                    int fromTransport = TRANSPORT_NAMES.get("TRANSPORT_" + transports[0]).intValue();
                    int toTransport = TRANSPORT_NAMES.get("TRANSPORT_" + transports[1]).intValue();
                    if (hasTransport(fromNai, fromTransport) && hasTransport(toNai, toTransport)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void showNotification(NetworkAgentInfo fromNai, NetworkAgentInfo toNai) {
        this.mNotifier.showNotification(fromNai.network.netId, NetworkNotificationManager.NotificationType.NETWORK_SWITCH, fromNai, toNai, createNotificationIntent(), true);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public PendingIntent createNotificationIntent() {
        return PendingIntent.getActivityAsUser(this.mContext, 0, CELLULAR_SETTINGS, 268435456, null, UserHandle.CURRENT);
    }

    private void maybeStopNotifying(NetworkAgentInfo nai) {
        int fromNetId = getNotificationSource(nai);
        if (fromNetId != 0) {
            this.mNotifications.delete(fromNetId);
            this.mNotifier.clearNotification(fromNetId);
        }
    }

    private void notify(NetworkAgentInfo fromNai, NetworkAgentInfo toNai, boolean forceToast) {
        int notifyType = this.mContext.getResources().getInteger(17694854);
        if (notifyType == 1 && forceToast) {
            notifyType = 2;
        }
        if (notifyType != 0) {
            if (notifyType == 1) {
                showNotification(fromNai, toNai);
            } else if (notifyType != 2) {
                String str = TAG;
                Log.e(str, "Unknown notify type " + notifyType);
                return;
            } else {
                this.mNotifier.showToast(fromNai, toNai);
            }
            String str2 = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Notifying switch from=");
            sb.append(fromNai.name());
            sb.append(" to=");
            sb.append(toNai.name());
            sb.append(" type=");
            SparseArray<String> sparseArray = sNotifyTypeNames;
            sb.append(sparseArray.get(notifyType, "unknown(" + notifyType + ")"));
            Log.d(str2, sb.toString());
            this.mNotifications.put(fromNai.network.netId, toNai.network.netId);
            this.mEverNotified.put(fromNai.network.netId, true);
        }
    }

    public void noteLingerDefaultNetwork(NetworkAgentInfo fromNai, NetworkAgentInfo toNai) {
        maybeStopNotifying(fromNai);
        if (fromNai.everValidated) {
            boolean forceToast = fromNai.networkCapabilities.hasCapability(17);
            if (!everNotified(fromNai) && !fromNai.lastValidated && isNotificationEnabled(fromNai, toNai)) {
                long now = SystemClock.elapsedRealtime();
                if (!isRateLimited(now) && !isAboveDailyLimit(now)) {
                    notify(fromNai, toNai, forceToast);
                }
            }
        }
    }

    public void noteDisconnect(NetworkAgentInfo nai) {
        this.mNotifications.delete(nai.network.netId);
        this.mEverNotified.delete(nai.network.netId);
        maybeStopNotifying(nai);
    }

    private boolean isRateLimited(long now) {
        if (now - this.mLastNotificationMillis < this.mRateLimitMillis) {
            return true;
        }
        this.mLastNotificationMillis = now;
        return false;
    }

    private boolean isAboveDailyLimit(long now) {
        if (this.mFirstNotificationMillis == 0) {
            this.mFirstNotificationMillis = now;
        }
        if (now - this.mFirstNotificationMillis > 86400000) {
            this.mNotificationCounter = 0;
            this.mFirstNotificationMillis = 0;
        }
        int i = this.mNotificationCounter;
        if (i >= this.mDailyLimit) {
            return true;
        }
        this.mNotificationCounter = i + 1;
        return false;
    }
}
