package com.android.server.notification;

import java.util.Comparator;

public class NotificationComparator implements Comparator<NotificationRecord> {
    public int compare(NotificationRecord left, NotificationRecord right) {
        int leftImportance = left.getImportance();
        int rightImportance = right.getImportance();
        if (leftImportance != rightImportance) {
            return Integer.compare(leftImportance, rightImportance) * -1;
        }
        int leftPackagePriority = left.getPackagePriority();
        int rightPackagePriority = right.getPackagePriority();
        if (leftPackagePriority != rightPackagePriority) {
            return Integer.compare(leftPackagePriority, rightPackagePriority) * -1;
        }
        int leftPriority = left.sbn.getNotification().priority;
        int rightPriority = right.sbn.getNotification().priority;
        if (leftPriority != rightPriority) {
            return Integer.compare(leftPriority, rightPriority) * -1;
        }
        float leftPeople = left.getContactAffinity();
        float rightPeople = right.getContactAffinity();
        if (leftPeople != rightPeople) {
            return Float.compare(leftPeople, rightPeople) * -1;
        }
        return Long.compare(left.getRankingTimeMs(), right.getRankingTimeMs()) * -1;
    }
}
