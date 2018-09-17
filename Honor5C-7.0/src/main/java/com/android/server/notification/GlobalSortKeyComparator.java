package com.android.server.notification;

import java.util.Comparator;

public class GlobalSortKeyComparator implements Comparator<NotificationRecord> {
    public int compare(NotificationRecord left, NotificationRecord right) {
        if (left.getGlobalSortKey() == null) {
            throw new IllegalStateException("Missing left global sort key: " + left);
        } else if (right.getGlobalSortKey() != null) {
            return left.getGlobalSortKey().compareTo(right.getGlobalSortKey());
        } else {
            throw new IllegalStateException("Missing right global sort key: " + right);
        }
    }
}
