package com.android.server.notification;

import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.util.Slog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class GroupHelper {
    protected static final int AUTOGROUP_AT_COUNT = 2;
    protected static final String AUTOGROUP_KEY = "ranker_group";
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int N_MUSIC = 2;
    private static final int N_NORMAL = 1;
    private static final String TAG = "GroupHelper";
    private final Callback mCallback;
    Map<Integer, Map<String, LinkedHashSet<String>>> mUngroupedNotifications = new HashMap();

    protected interface Callback {
        void addAutoGroup(String str);

        void addAutoGroupSummary(int i, String str, String str2);

        void removeAutoGroup(String str);

        void removeAutoGroupSummary(int i, String str);

        void removeAutoGroupSummary(int i, String str, int i2);
    }

    public GroupHelper(Callback callback) {
        this.mCallback = callback;
    }

    public void onNotificationPosted(StatusBarNotification sbn) {
        onNotificationPosted(sbn, 1);
    }

    public void onNotificationPosted(StatusBarNotification sbn, int notifyType) {
        if (DEBUG) {
            Log.i(TAG, "POSTED " + sbn.getKey());
        }
        try {
            List<String> notificationsToGroup = new ArrayList();
            if (sbn.getNotification().extras.containsKey("hw_btw") || !sbn.isAppGroup()) {
                synchronized (this.mUngroupedNotifications) {
                    Map<String, LinkedHashSet<String>> ungroupedNotificationsByUser = (Map) this.mUngroupedNotifications.get(Integer.valueOf(sbn.getUserId()));
                    if (ungroupedNotificationsByUser == null) {
                        ungroupedNotificationsByUser = new HashMap();
                    }
                    this.mUngroupedNotifications.put(Integer.valueOf(sbn.getUserId()), ungroupedNotificationsByUser);
                    String notifyKey = getUnGroupKey(sbn.getPackageName(), notifyType);
                    LinkedHashSet<String> notificationsForPackage = (LinkedHashSet) ungroupedNotificationsByUser.get(notifyKey);
                    if (notificationsForPackage == null) {
                        notificationsForPackage = new LinkedHashSet();
                    }
                    notificationsForPackage.add(sbn.getKey());
                    ungroupedNotificationsByUser.put(notifyKey, notificationsForPackage);
                    if (notificationsForPackage.size() >= 2) {
                        notificationsToGroup.addAll(notificationsForPackage);
                    }
                }
                if (notificationsToGroup.size() > 0) {
                    adjustAutogroupingSummary(sbn.getUserId(), sbn.getPackageName(), (String) notificationsToGroup.get(0), true, notifyType);
                    adjustNotificationBundling(notificationsToGroup, true);
                }
                return;
            }
            maybeUngroup(sbn, false, sbn.getUserId(), notifyType);
            Log.i(TAG, "onNotificationPosted " + notifyType);
        } catch (Exception e) {
            Slog.e(TAG, "Failure processing new notification", e);
        }
    }

    public void onNotificationRemoved(StatusBarNotification sbn) {
        onNotificationRemoved(sbn, 1);
    }

    public void onNotificationRemoved(StatusBarNotification sbn, int notifyType) {
        try {
            maybeUngroup(sbn, true, sbn.getUserId(), notifyType);
        } catch (Exception e) {
            Slog.e(TAG, "Error processing canceled notification", e);
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0022, code:
            return;
     */
    /* JADX WARNING: Missing block: B:15:0x003c, code:
            return;
     */
    /* JADX WARNING: Missing block: B:33:0x007a, code:
            if (r11 == false) goto L_0x008a;
     */
    /* JADX WARNING: Missing block: B:34:0x007c, code:
            adjustAutogroupingSummary(r16, r14.getPackageName(), null, false, r17);
     */
    /* JADX WARNING: Missing block: B:36:0x008e, code:
            if (r9.size() <= 0) goto L_0x0094;
     */
    /* JADX WARNING: Missing block: B:37:0x0090, code:
            adjustNotificationBundling(r9, false);
     */
    /* JADX WARNING: Missing block: B:38:0x0094, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void maybeUngroup(StatusBarNotification sbn, boolean notificationGone, int userId, int notifyType) {
        List<String> notificationsToUnAutogroup = new ArrayList();
        boolean removeSummary = false;
        synchronized (this.mUngroupedNotifications) {
            Map<String, LinkedHashSet<String>> ungroupedNotificationsByUser = (Map) this.mUngroupedNotifications.get(Integer.valueOf(sbn.getUserId()));
            if (ungroupedNotificationsByUser == null || ungroupedNotificationsByUser.size() == 0) {
            } else {
                LinkedHashSet<String> notificationsForPackage = (LinkedHashSet) ungroupedNotificationsByUser.get(getUnGroupKey(sbn.getPackageName(), notifyType));
                if (notificationsForPackage == null || notificationsForPackage.size() == 0) {
                } else {
                    if (notificationsForPackage.remove(sbn.getKey()) && !notificationGone) {
                        notificationsToUnAutogroup.add(sbn.getKey());
                    }
                    if (notificationsForPackage.size() == 0) {
                        removeSummary = true;
                    }
                    if (notificationsForPackage.size() == 1) {
                        Iterator<String> linkedSet = notificationsForPackage.iterator();
                        while (linkedSet.hasNext()) {
                            if (((String) linkedSet.next()).contains(AUTOGROUP_KEY)) {
                                removeSummary = true;
                            }
                        }
                    }
                }
            }
        }
    }

    private void adjustAutogroupingSummary(int userId, String packageName, String triggeringKey, boolean summaryNeeded, int notifyType) {
        if (summaryNeeded) {
            this.mCallback.addAutoGroupSummary(userId, packageName, triggeringKey);
        } else {
            this.mCallback.removeAutoGroupSummary(userId, packageName, notifyType);
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

    public String getAutoGroupKey(int notificationType, int id) {
        if (notificationType == 2) {
            return AUTOGROUP_KEY + notificationType + id;
        }
        return AUTOGROUP_KEY + notificationType;
    }

    public String getUnGroupKey(String pkgName, int notificationType) {
        return pkgName + notificationType;
    }
}
