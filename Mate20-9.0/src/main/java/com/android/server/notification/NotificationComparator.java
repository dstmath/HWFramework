package com.android.server.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telecom.TelecomManager;
import com.android.internal.util.NotificationMessagingUtil;
import java.util.Comparator;
import java.util.Objects;

public class NotificationComparator implements Comparator<NotificationRecord> {
    private final Context mContext;
    /* access modifiers changed from: private */
    public String mDefaultPhoneApp;
    private final NotificationMessagingUtil mMessagingUtil;
    private final BroadcastReceiver mPhoneAppBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String unused = NotificationComparator.this.mDefaultPhoneApp = intent.getStringExtra("android.telecom.extra.CHANGE_DEFAULT_DIALER_PACKAGE_NAME");
        }
    };

    public NotificationComparator(Context context) {
        this.mContext = context;
        this.mContext.registerReceiver(this.mPhoneAppBroadcastReceiver, new IntentFilter("android.telecom.action.DEFAULT_DIALER_CHANGED"));
        this.mMessagingUtil = new NotificationMessagingUtil(this.mContext);
    }

    public int compare(NotificationRecord left, NotificationRecord right) {
        NotificationRecord notificationRecord = right;
        boolean leftImportantColorized = isImportantColorized(left);
        boolean rightImportantColorized = isImportantColorized(notificationRecord);
        if (leftImportantColorized != rightImportantColorized) {
            return -1 * Boolean.compare(leftImportantColorized, rightImportantColorized);
        }
        boolean leftImportantOngoing = isImportantOngoing(left);
        boolean rightImportantOngoing = isImportantOngoing(notificationRecord);
        if (leftImportantOngoing != rightImportantOngoing) {
            return -1 * Boolean.compare(leftImportantOngoing, rightImportantOngoing);
        }
        boolean leftMessaging = isImportantMessaging(left);
        boolean rightMessaging = isImportantMessaging(notificationRecord);
        if (leftMessaging != rightMessaging) {
            return -1 * Boolean.compare(leftMessaging, rightMessaging);
        }
        boolean leftPeople = isImportantPeople(left);
        boolean rightPeople = isImportantPeople(notificationRecord);
        int contactAffinityComparison = Float.compare(left.getContactAffinity(), right.getContactAffinity());
        if (!leftPeople || !rightPeople) {
            if (leftPeople != rightPeople) {
                return -1 * Boolean.compare(leftPeople, rightPeople);
            }
        } else if (contactAffinityComparison != 0) {
            return -1 * contactAffinityComparison;
        }
        int leftImportance = left.getImportance();
        int rightImportance = right.getImportance();
        if (leftImportance != rightImportance) {
            return -1 * Integer.compare(leftImportance, rightImportance);
        }
        if (contactAffinityComparison != 0) {
            return -1 * contactAffinityComparison;
        }
        int leftPackagePriority = left.getPackagePriority();
        int rightPackagePriority = right.getPackagePriority();
        if (leftPackagePriority != rightPackagePriority) {
            return -1 * Integer.compare(leftPackagePriority, rightPackagePriority);
        }
        int leftPriority = left.sbn.getNotification().priority;
        boolean z = leftImportantColorized;
        int rightPriority = notificationRecord.sbn.getNotification().priority;
        if (leftPriority != rightPriority) {
            return Integer.compare(leftPriority, rightPriority) * -1;
        }
        int i = rightPriority;
        boolean z2 = rightImportantColorized;
        boolean z3 = leftImportantOngoing;
        return -1 * Long.compare(left.getRankingTimeMs(), right.getRankingTimeMs());
    }

    private boolean isImportantColorized(NotificationRecord record) {
        if (record.getImportance() < 2) {
            return false;
        }
        return record.getNotification().isColorized();
    }

    private boolean isImportantOngoing(NotificationRecord record) {
        boolean z = false;
        if (!isOngoing(record) || record.getImportance() < 2) {
            return false;
        }
        if (isCall(record) || isMediaNotification(record)) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public boolean isImportantPeople(NotificationRecord record) {
        if (record.getImportance() >= 2 && record.getContactAffinity() > 0.0f) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isImportantMessaging(NotificationRecord record) {
        return this.mMessagingUtil.isImportantMessaging(record.sbn, record.getImportance());
    }

    private boolean isOngoing(NotificationRecord record) {
        return (record.getNotification().flags & 64) != 0;
    }

    private boolean isMediaNotification(NotificationRecord record) {
        return record.getNotification().hasMediaSession();
    }

    private boolean isCall(NotificationRecord record) {
        return record.isCategory("call") && isDefaultPhoneApp(record.sbn.getPackageName());
    }

    private boolean isDefaultPhoneApp(String pkg) {
        if (this.mDefaultPhoneApp == null) {
            TelecomManager telecomm = (TelecomManager) this.mContext.getSystemService("telecom");
            this.mDefaultPhoneApp = telecomm != null ? telecomm.getDefaultDialerPackage() : null;
        }
        return Objects.equals(pkg, this.mDefaultPhoneApp);
    }
}
