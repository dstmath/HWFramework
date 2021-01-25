package com.android.server.notification;

import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.util.Slog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class GroupHelper {
    protected static final int AUTOGROUP_AT_COUNT = 2;
    protected static final String AUTOGROUP_KEY = "ranker_group";
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "GroupHelper";
    private final int mAutoGroupAtCount;
    private final Callback mCallback;
    Map<Integer, Map<String, LinkedHashSet<String>>> mUngroupedNotifications = new HashMap();

    /* access modifiers changed from: protected */
    public interface Callback {
        void addAutoGroup(String str);

        void addAutoGroupSummary(int i, String str, String str2);

        void removeAutoGroup(String str);

        void removeAutoGroupSummary(int i, String str);
    }

    public GroupHelper(int autoGroupAtCount, Callback callback) {
        this.mAutoGroupAtCount = autoGroupAtCount;
        this.mCallback = callback;
    }

    public void onNotificationPosted(StatusBarNotification sbn, boolean autogroupSummaryExists) {
        if (DEBUG) {
            Log.i(TAG, "POSTED " + sbn.getKey());
        }
        try {
            List<String> notificationsToGroup = new ArrayList<>();
            if (!sbn.isAppGroup()) {
                synchronized (this.mUngroupedNotifications) {
                    Map<String, LinkedHashSet<String>> ungroupedNotificationsByUser = this.mUngroupedNotifications.get(Integer.valueOf(sbn.getUserId()));
                    if (ungroupedNotificationsByUser == null) {
                        ungroupedNotificationsByUser = new HashMap();
                    }
                    this.mUngroupedNotifications.put(Integer.valueOf(sbn.getUserId()), ungroupedNotificationsByUser);
                    LinkedHashSet<String> notificationsForPackage = ungroupedNotificationsByUser.get(sbn.getPackageName());
                    if (notificationsForPackage == null) {
                        notificationsForPackage = new LinkedHashSet<>();
                    }
                    notificationsForPackage.add(sbn.getKey());
                    ungroupedNotificationsByUser.put(sbn.getPackageName(), notificationsForPackage);
                    if (notificationsForPackage.size() >= 2 || autogroupSummaryExists) {
                        notificationsToGroup.addAll(notificationsForPackage);
                    }
                }
                if (notificationsToGroup.size() > 0) {
                    adjustAutogroupingSummary(sbn.getUserId(), sbn.getPackageName(), notificationsToGroup.get(0), true);
                    adjustNotificationBundling(notificationsToGroup, true);
                    return;
                }
                return;
            }
            maybeUngroup(sbn, false, sbn.getUserId());
        } catch (Exception e) {
            Slog.e(TAG, "Failure processing new notification", e);
        }
    }

    public void onNotificationRemoved(StatusBarNotification sbn) {
        try {
            maybeUngroup(sbn, true, sbn.getUserId());
        } catch (Exception e) {
            Slog.e(TAG, "Error processing canceled notification", e);
        }
    }

    private void maybeUngroup(StatusBarNotification sbn, boolean notificationGone, int userId) {
        List<String> notificationsToUnAutogroup = new ArrayList<>();
        boolean removeSummary = false;
        synchronized (this.mUngroupedNotifications) {
            Map<String, LinkedHashSet<String>> ungroupedNotificationsByUser = this.mUngroupedNotifications.get(Integer.valueOf(sbn.getUserId()));
            if (ungroupedNotificationsByUser != null) {
                if (ungroupedNotificationsByUser.size() != 0) {
                    LinkedHashSet<String> notificationsForPackage = ungroupedNotificationsByUser.get(sbn.getPackageName());
                    if (notificationsForPackage != null) {
                        if (notificationsForPackage.size() != 0) {
                            if (notificationsForPackage.remove(sbn.getKey()) && !notificationGone) {
                                notificationsToUnAutogroup.add(sbn.getKey());
                            }
                            if (notificationsForPackage.size() == 0) {
                                ungroupedNotificationsByUser.remove(sbn.getPackageName());
                                removeSummary = true;
                            }
                        }
                    }
                    return;
                }
            }
            return;
        }
        if (removeSummary) {
            adjustAutogroupingSummary(userId, sbn.getPackageName(), null, false);
        }
        if (notificationsToUnAutogroup.size() > 0) {
            adjustNotificationBundling(notificationsToUnAutogroup, false);
        }
    }

    private void adjustAutogroupingSummary(int userId, String packageName, String triggeringKey, boolean summaryNeeded) {
        if (summaryNeeded) {
            this.mCallback.addAutoGroupSummary(userId, packageName, triggeringKey);
        } else {
            this.mCallback.removeAutoGroupSummary(userId, packageName);
        }
    }

    private void adjustNotificationBundling(List<String> keys, boolean group) {
        for (String key : keys) {
            if (DEBUG) {
                Log.i(TAG, "Sending grouping adjustment for: " + key + " group? " + group);
            }
            if (group) {
                this.mCallback.addAutoGroup(key);
            } else {
                this.mCallback.removeAutoGroup(key);
            }
        }
    }
}
