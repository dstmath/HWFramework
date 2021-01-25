package android.app;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.INotificationManager;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.UserHandle;
import android.service.notification.Condition;
import android.service.notification.StatusBarNotification;
import android.service.notification.ZenModeConfig;
import android.util.Log;
import android.util.proto.ProtoOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NotificationManager {
    public static final String ACTION_APP_BLOCK_STATE_CHANGED = "android.app.action.APP_BLOCK_STATE_CHANGED";
    public static final String ACTION_AUTOMATIC_ZEN_RULE = "android.app.action.AUTOMATIC_ZEN_RULE";
    public static final String ACTION_EFFECTS_SUPPRESSOR_CHANGED = "android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED";
    public static final String ACTION_INTERRUPTION_FILTER_CHANGED = "android.app.action.INTERRUPTION_FILTER_CHANGED";
    public static final String ACTION_INTERRUPTION_FILTER_CHANGED_INTERNAL = "android.app.action.INTERRUPTION_FILTER_CHANGED_INTERNAL";
    public static final String ACTION_NOTIFICATION_CHANNEL_BLOCK_STATE_CHANGED = "android.app.action.NOTIFICATION_CHANNEL_BLOCK_STATE_CHANGED";
    public static final String ACTION_NOTIFICATION_CHANNEL_GROUP_BLOCK_STATE_CHANGED = "android.app.action.NOTIFICATION_CHANNEL_GROUP_BLOCK_STATE_CHANGED";
    public static final String ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED = "android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED";
    public static final String ACTION_NOTIFICATION_POLICY_CHANGED = "android.app.action.NOTIFICATION_POLICY_CHANGED";
    public static final String EXTRA_AUTOMATIC_RULE_ID = "android.app.extra.AUTOMATIC_RULE_ID";
    public static final String EXTRA_BLOCKED_STATE = "android.app.extra.BLOCKED_STATE";
    public static final String EXTRA_NOTIFICATION_CHANNEL_GROUP_ID = "android.app.extra.NOTIFICATION_CHANNEL_GROUP_ID";
    public static final String EXTRA_NOTIFICATION_CHANNEL_ID = "android.app.extra.NOTIFICATION_CHANNEL_ID";
    public static final int IMPORTANCE_DEFAULT = 3;
    public static final int IMPORTANCE_HIGH = 4;
    public static final int IMPORTANCE_LOW = 2;
    public static final int IMPORTANCE_MAX = 5;
    public static final int IMPORTANCE_MIN = 1;
    public static final int IMPORTANCE_NONE = 0;
    public static final int IMPORTANCE_UNSPECIFIED = -1000;
    public static final int INTERRUPTION_FILTER_ALARMS = 4;
    public static final int INTERRUPTION_FILTER_ALL = 1;
    public static final int INTERRUPTION_FILTER_NONE = 3;
    public static final int INTERRUPTION_FILTER_PRIORITY = 2;
    public static final int INTERRUPTION_FILTER_UNKNOWN = 0;
    public static final String META_DATA_AUTOMATIC_RULE_TYPE = "android.service.zen.automatic.ruleType";
    public static final String META_DATA_RULE_INSTANCE_LIMIT = "android.service.zen.automatic.ruleInstanceLimit";
    private static String TAG = "NotificationManager";
    public static final int VISIBILITY_NO_OVERRIDE = -1000;
    private static boolean localLOGV = false;
    @UnsupportedAppUsage
    private static INotificationManager sService;
    private Context mContext;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Importance {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface InterruptionFilter {
    }

    @UnsupportedAppUsage
    public static INotificationManager getService() {
        INotificationManager iNotificationManager = sService;
        if (iNotificationManager != null) {
            return iNotificationManager;
        }
        sService = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        return sService;
    }

    @UnsupportedAppUsage
    NotificationManager(Context context, Handler handler) {
        this.mContext = context;
    }

    @UnsupportedAppUsage
    public static NotificationManager from(Context context) {
        return (NotificationManager) context.getSystemService("notification");
    }

    public void notify(int id, Notification notification) {
        notify(null, id, notification);
    }

    public void notify(String tag, int id, Notification notification) {
        notifyAsUser(tag, id, notification, this.mContext.getUser());
    }

    public void notifyAsPackage(String targetPackage, String tag, int id, Notification notification) {
        INotificationManager service = getService();
        String sender = this.mContext.getPackageName();
        try {
            if (localLOGV) {
                String str = TAG;
                Log.v(str, sender + ": notify(" + id + ", " + notification + ")");
            }
            service.enqueueNotificationWithTag(targetPackage, sender, tag, id, fixNotification(notification), this.mContext.getUser().getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public void notifyAsUser(String tag, int id, Notification notification, UserHandle user) {
        INotificationManager service = getService();
        String pkg = this.mContext.getPackageName();
        try {
            if (localLOGV) {
                String str = TAG;
                Log.v(str, pkg + ": notify(" + id + ", " + notification + ")");
            }
            service.enqueueNotificationWithTag(pkg, this.mContext.getOpPackageName(), tag, id, fixNotification(notification), user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private Notification fixNotification(Notification notification) {
        String pkg = this.mContext.getPackageName();
        Notification.addFieldsFromContext(this.mContext, notification);
        if (notification.sound != null) {
            notification.sound = notification.sound.getCanonicalUri();
            if (StrictMode.vmFileUriExposureEnabled()) {
                notification.sound.checkFileUriExposed("Notification.sound");
            }
        }
        fixLegacySmallIcon(notification, pkg);
        if (this.mContext.getApplicationInfo().targetSdkVersion <= 22 || notification.getSmallIcon() != null) {
            notification.reduceImageSizes(this.mContext);
            return Notification.Builder.maybeCloneStrippedForDelivery(notification, ((ActivityManager) this.mContext.getSystemService(Context.ACTIVITY_SERVICE)).isLowRamDevice(), this.mContext);
        }
        throw new IllegalArgumentException("Invalid notification (no valid small icon): " + notification);
    }

    private void fixLegacySmallIcon(Notification n, String pkg) {
        if (n.getSmallIcon() == null && n.icon != 0) {
            n.setSmallIcon(Icon.createWithResource(pkg, n.icon));
        }
    }

    public void cancel(int id) {
        cancel(null, id);
    }

    public void cancel(String tag, int id) {
        cancelAsUser(tag, id, this.mContext.getUser());
    }

    @UnsupportedAppUsage
    public void cancelAsUser(String tag, int id, UserHandle user) {
        INotificationManager service = getService();
        String pkg = this.mContext.getPackageName();
        String str = TAG;
        Log.i(str, pkg + ": cancel(" + id + ")");
        try {
            service.cancelNotificationWithTag(pkg, tag, id, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void cancelAll() {
        INotificationManager service = getService();
        String pkg = this.mContext.getPackageName();
        if (localLOGV) {
            String str = TAG;
            Log.v(str, pkg + ": cancelAll()");
        }
        try {
            service.cancelAllNotifications(pkg, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setNotificationDelegate(String delegate) {
        INotificationManager service = getService();
        String pkg = this.mContext.getPackageName();
        if (localLOGV) {
            String str = TAG;
            Log.v(str, pkg + ": cancelAll()");
        }
        try {
            service.setNotificationDelegate(pkg, delegate);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getNotificationDelegate() {
        try {
            return getService().getNotificationDelegate(this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean canNotifyAsPackage(String pkg) {
        try {
            return getService().canNotifyAsPackage(this.mContext.getPackageName(), pkg, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void createNotificationChannelGroup(NotificationChannelGroup group) {
        createNotificationChannelGroups(Arrays.asList(group));
    }

    public void createNotificationChannelGroups(List<NotificationChannelGroup> groups) {
        try {
            getService().createNotificationChannelGroups(this.mContext.getPackageName(), new ParceledListSlice(groups));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void createNotificationChannel(NotificationChannel channel) {
        createNotificationChannels(Arrays.asList(channel));
    }

    public void createNotificationChannels(List<NotificationChannel> channels) {
        INotificationManager service = getService();
        if (service != null) {
            try {
                service.createNotificationChannels(this.mContext.getPackageName(), new ParceledListSlice(channels));
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "HwNotificationManagerService is null");
        }
    }

    public NotificationChannel getNotificationChannel(String channelId) {
        try {
            return getService().getNotificationChannel(this.mContext.getOpPackageName(), this.mContext.getUserId(), this.mContext.getPackageName(), channelId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<NotificationChannel> getNotificationChannels() {
        try {
            return getService().getNotificationChannels(this.mContext.getOpPackageName(), this.mContext.getPackageName(), this.mContext.getUserId()).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void deleteNotificationChannel(String channelId) {
        try {
            getService().deleteNotificationChannel(this.mContext.getPackageName(), channelId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public NotificationChannelGroup getNotificationChannelGroup(String channelGroupId) {
        try {
            return getService().getNotificationChannelGroup(this.mContext.getPackageName(), channelGroupId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<NotificationChannelGroup> getNotificationChannelGroups() {
        try {
            ParceledListSlice<NotificationChannelGroup> parceledList = getService().getNotificationChannelGroups(this.mContext.getPackageName());
            if (parceledList != null) {
                return parceledList.getList();
            }
            return new ArrayList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void deleteNotificationChannelGroup(String groupId) {
        try {
            getService().deleteNotificationChannelGroup(this.mContext.getPackageName(), groupId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ComponentName getEffectsSuppressor() {
        try {
            return getService().getEffectsSuppressor();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean matchesCallFilter(Bundle extras) {
        try {
            return getService().matchesCallFilter(extras);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSystemConditionProviderEnabled(String path) {
        try {
            return getService().isSystemConditionProviderEnabled(path);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public void setZenMode(int mode, Uri conditionId, String reason) {
        try {
            getService().setZenMode(mode, conditionId, reason);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getZenMode() {
        try {
            return getService().getZenMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public ZenModeConfig getZenModeConfig() {
        try {
            return getService().getZenModeConfig();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Policy getConsolidatedNotificationPolicy() {
        try {
            return getService().getConsolidatedNotificationPolicy();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getRuleInstanceCount(ComponentName owner) {
        try {
            return getService().getRuleInstanceCount(owner);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Map<String, AutomaticZenRule> getAutomaticZenRules() {
        RemoteException e;
        INotificationManager service = getService();
        try {
            List<ZenModeConfig.ZenRule> rules = service.getZenRules();
            Map<String, AutomaticZenRule> ruleMap = new HashMap<>();
            if (rules == null) {
                try {
                    Log.w(TAG, "getAutomaticZenRules null == rules");
                    return ruleMap;
                } catch (RemoteException e2) {
                    e = e2;
                    throw e.rethrowFromSystemServer();
                }
            } else {
                for (Iterator<ZenModeConfig.ZenRule> it = rules.iterator(); it.hasNext(); it = it) {
                    ZenModeConfig.ZenRule rule = it.next();
                    try {
                        ruleMap.put(rule.id, new AutomaticZenRule(rule.name, rule.component, rule.configurationActivity, rule.conditionId, rule.zenPolicy, zenModeToInterruptionFilter(rule.zenMode), rule.enabled, rule.creationTime));
                        service = service;
                        rules = rules;
                    } catch (RemoteException e3) {
                        e = e3;
                        throw e.rethrowFromSystemServer();
                    }
                }
                return ruleMap;
            }
        } catch (RemoteException e4) {
            e = e4;
            throw e.rethrowFromSystemServer();
        }
    }

    public AutomaticZenRule getAutomaticZenRule(String id) {
        try {
            return getService().getAutomaticZenRule(id);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String addAutomaticZenRule(AutomaticZenRule automaticZenRule) {
        try {
            return getService().addAutomaticZenRule(automaticZenRule);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean updateAutomaticZenRule(String id, AutomaticZenRule automaticZenRule) {
        try {
            return getService().updateAutomaticZenRule(id, automaticZenRule);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setAutomaticZenRuleState(String id, Condition condition) {
        try {
            getService().setAutomaticZenRuleState(id, condition);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean removeAutomaticZenRule(String id) {
        try {
            return getService().removeAutomaticZenRule(id);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean removeAutomaticZenRules(String packageName) {
        try {
            return getService().removeAutomaticZenRules(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getImportance() {
        try {
            return getService().getPackageImportance(this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean areNotificationsEnabled() {
        try {
            return getService().areNotificationsEnabled(this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean areBubblesAllowed() {
        try {
            return getService().areBubblesAllowed(this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean areNotificationsPaused() {
        try {
            return getService().isPackagePaused(this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isNotificationPolicyAccessGranted() {
        try {
            return getService().isNotificationPolicyAccessGranted(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isNotificationListenerAccessGranted(ComponentName listener) {
        try {
            return getService().isNotificationListenerAccessGranted(listener);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean isNotificationAssistantAccessGranted(ComponentName assistant) {
        try {
            return getService().isNotificationAssistantAccessGranted(assistant);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean shouldHideSilentStatusBarIcons() {
        try {
            return getService().shouldHideSilentStatusIcons(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public List<String> getAllowedAssistantAdjustments() {
        try {
            return getService().getAllowedAssistantAdjustments(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void allowAssistantAdjustment(String capability) {
        try {
            getService().allowAssistantAdjustment(capability);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void disallowAssistantAdjustment(String capability) {
        try {
            getService().disallowAssistantAdjustment(capability);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isNotificationPolicyAccessGrantedForPackage(String pkg) {
        try {
            return getService().isNotificationPolicyAccessGrantedForPackage(pkg);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getEnabledNotificationListenerPackages() {
        try {
            return getService().getEnabledNotificationListenerPackages();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Policy getNotificationPolicy() {
        try {
            return getService().getNotificationPolicy(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setNotificationPolicy(Policy policy) {
        checkRequired("policy", policy);
        try {
            getService().setNotificationPolicy(this.mContext.getOpPackageName(), policy);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setNotificationPolicyAccessGranted(String pkg, boolean granted) {
        try {
            getService().setNotificationPolicyAccessGranted(pkg, granted);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setNotificationListenerAccessGranted(ComponentName listener, boolean granted) {
        try {
            getService().setNotificationListenerAccessGranted(listener, granted);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setNotificationListenerAccessGrantedForUser(ComponentName listener, int userId, boolean granted) {
        try {
            getService().setNotificationListenerAccessGrantedForUser(listener, userId, granted);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void setNotificationAssistantAccessGranted(ComponentName assistant, boolean granted) {
        try {
            getService().setNotificationAssistantAccessGranted(assistant, granted);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ComponentName> getEnabledNotificationListeners(int userId) {
        try {
            return getService().getEnabledNotificationListeners(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public ComponentName getAllowedNotificationAssistant() {
        try {
            return getService().getAllowedNotificationAssistant();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private static void checkRequired(String name, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required");
        }
    }

    public static class Policy implements Parcelable {
        public static final int[] ALL_PRIORITY_CATEGORIES = {32, 64, 128, 1, 2, 4, 8, 16};
        private static final int[] ALL_SUPPRESSED_EFFECTS = {1, 2, 4, 8, 16, 64, 128};
        public static final Parcelable.Creator<Policy> CREATOR = new Parcelable.Creator<Policy>() {
            /* class android.app.NotificationManager.Policy.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Policy createFromParcel(Parcel in) {
                return new Policy(in);
            }

            @Override // android.os.Parcelable.Creator
            public Policy[] newArray(int size) {
                return new Policy[size];
            }
        };
        public static final int PRIORITY_CATEGORY_ALARMS = 32;
        public static final int PRIORITY_CATEGORY_CALLS = 8;
        public static final int PRIORITY_CATEGORY_EVENTS = 2;
        public static final int PRIORITY_CATEGORY_MEDIA = 64;
        public static final int PRIORITY_CATEGORY_MESSAGES = 4;
        public static final int PRIORITY_CATEGORY_REMINDERS = 1;
        public static final int PRIORITY_CATEGORY_REPEAT_CALLERS = 16;
        public static final int PRIORITY_CATEGORY_SYSTEM = 128;
        public static final int PRIORITY_SENDERS_ANY = 0;
        public static final int PRIORITY_SENDERS_CONTACTS = 1;
        public static final int PRIORITY_SENDERS_STARRED = 2;
        private static final int[] SCREEN_OFF_SUPPRESSED_EFFECTS = {1, 4, 8, 128};
        private static final int[] SCREEN_ON_SUPPRESSED_EFFECTS = {2, 16, 64};
        public static final int STATE_CHANNELS_BYPASSING_DND = 1;
        public static final int STATE_UNSET = -1;
        public static final int SUPPRESSED_EFFECTS_UNSET = -1;
        public static final int SUPPRESSED_EFFECT_AMBIENT = 128;
        public static final int SUPPRESSED_EFFECT_BADGE = 64;
        public static final int SUPPRESSED_EFFECT_FULL_SCREEN_INTENT = 4;
        public static final int SUPPRESSED_EFFECT_LIGHTS = 8;
        public static final int SUPPRESSED_EFFECT_NOTIFICATION_LIST = 256;
        public static final int SUPPRESSED_EFFECT_PEEK = 16;
        @Deprecated
        public static final int SUPPRESSED_EFFECT_SCREEN_OFF = 1;
        @Deprecated
        public static final int SUPPRESSED_EFFECT_SCREEN_ON = 2;
        public static final int SUPPRESSED_EFFECT_STATUS_BAR = 32;
        public final int priorityCallSenders;
        public final int priorityCategories;
        public final int priorityMessageSenders;
        public final int state;
        public final int suppressedVisualEffects;

        public Policy(int priorityCategories2, int priorityCallSenders2, int priorityMessageSenders2) {
            this(priorityCategories2, priorityCallSenders2, priorityMessageSenders2, -1, -1);
        }

        public Policy(int priorityCategories2, int priorityCallSenders2, int priorityMessageSenders2, int suppressedVisualEffects2) {
            this.priorityCategories = priorityCategories2;
            this.priorityCallSenders = priorityCallSenders2;
            this.priorityMessageSenders = priorityMessageSenders2;
            this.suppressedVisualEffects = suppressedVisualEffects2;
            this.state = -1;
        }

        public Policy(int priorityCategories2, int priorityCallSenders2, int priorityMessageSenders2, int suppressedVisualEffects2, int state2) {
            this.priorityCategories = priorityCategories2;
            this.priorityCallSenders = priorityCallSenders2;
            this.priorityMessageSenders = priorityMessageSenders2;
            this.suppressedVisualEffects = suppressedVisualEffects2;
            this.state = state2;
        }

        public Policy(Parcel source) {
            this(source.readInt(), source.readInt(), source.readInt(), source.readInt(), source.readInt());
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.priorityCategories);
            dest.writeInt(this.priorityCallSenders);
            dest.writeInt(this.priorityMessageSenders);
            dest.writeInt(this.suppressedVisualEffects);
            dest.writeInt(this.state);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public int hashCode() {
            return Objects.hash(Integer.valueOf(this.priorityCategories), Integer.valueOf(this.priorityCallSenders), Integer.valueOf(this.priorityMessageSenders), Integer.valueOf(this.suppressedVisualEffects));
        }

        public boolean equals(Object o) {
            if (!(o instanceof Policy)) {
                return false;
            }
            if (o == this) {
                return true;
            }
            Policy other = (Policy) o;
            if (other.priorityCategories == this.priorityCategories && other.priorityCallSenders == this.priorityCallSenders && other.priorityMessageSenders == this.priorityMessageSenders && suppressedVisualEffectsEqual(this.suppressedVisualEffects, other.suppressedVisualEffects)) {
                return true;
            }
            return false;
        }

        private boolean suppressedVisualEffectsEqual(int suppressedEffects, int otherSuppressedVisualEffects) {
            if (suppressedEffects == otherSuppressedVisualEffects) {
                return true;
            }
            if ((suppressedEffects & 2) != 0) {
                suppressedEffects |= 16;
            }
            if ((suppressedEffects & 1) != 0) {
                suppressedEffects = suppressedEffects | 4 | 8 | 128;
            }
            if ((otherSuppressedVisualEffects & 2) != 0) {
                otherSuppressedVisualEffects |= 16;
            }
            if ((otherSuppressedVisualEffects & 1) != 0) {
                otherSuppressedVisualEffects = otherSuppressedVisualEffects | 4 | 8 | 128;
            }
            if ((suppressedEffects & 2) != (otherSuppressedVisualEffects & 2)) {
                if ((((suppressedEffects & 2) != 0 ? otherSuppressedVisualEffects : suppressedEffects) & 16) == 0) {
                    return false;
                }
            }
            if ((suppressedEffects & 1) != (otherSuppressedVisualEffects & 1)) {
                int currSuppressedEffects = (suppressedEffects & 1) != 0 ? otherSuppressedVisualEffects : suppressedEffects;
                if ((currSuppressedEffects & 4) == 0 || (currSuppressedEffects & 8) == 0 || (currSuppressedEffects & 128) == 0) {
                    return false;
                }
            }
            if ((suppressedEffects & -3 & -2) == (otherSuppressedVisualEffects & -3 & -2)) {
                return true;
            }
            return false;
        }

        public String toString() {
            String str;
            StringBuilder sb = new StringBuilder();
            sb.append("NotificationManager.Policy[priorityCategories=");
            sb.append(priorityCategoriesToString(this.priorityCategories));
            sb.append(",priorityCallSenders=");
            sb.append(prioritySendersToString(this.priorityCallSenders));
            sb.append(",priorityMessageSenders=");
            sb.append(prioritySendersToString(this.priorityMessageSenders));
            sb.append(",suppressedVisualEffects=");
            sb.append(suppressedEffectsToString(this.suppressedVisualEffects));
            sb.append(",areChannelsBypassingDnd=");
            if ((this.state & 1) != 0) {
                str = "true";
            } else {
                str = "false";
            }
            sb.append(str);
            sb.append("]");
            return sb.toString();
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            long pToken = proto.start(fieldId);
            bitwiseToProtoEnum(proto, 2259152797697L, this.priorityCategories);
            proto.write(1159641169922L, this.priorityCallSenders);
            proto.write(1159641169923L, this.priorityMessageSenders);
            bitwiseToProtoEnum(proto, 2259152797700L, this.suppressedVisualEffects);
            proto.end(pToken);
        }

        private static void bitwiseToProtoEnum(ProtoOutputStream proto, long fieldId, int data) {
            int i = 1;
            while (data > 0) {
                if ((data & 1) == 1) {
                    proto.write(fieldId, i);
                }
                i++;
                data >>>= 1;
            }
        }

        public static int getAllSuppressedVisualEffects() {
            int effects = 0;
            int i = 0;
            while (true) {
                int[] iArr = ALL_SUPPRESSED_EFFECTS;
                if (i >= iArr.length) {
                    return effects;
                }
                effects |= iArr[i];
                i++;
            }
        }

        public static boolean areAllVisualEffectsSuppressed(int effects) {
            int i = 0;
            while (true) {
                int[] iArr = ALL_SUPPRESSED_EFFECTS;
                if (i >= iArr.length) {
                    return true;
                }
                if ((effects & iArr[i]) == 0) {
                    return false;
                }
                i++;
            }
        }

        private static int toggleEffects(int currentEffects, int[] effects, boolean suppress) {
            for (int effect : effects) {
                if (suppress) {
                    currentEffects |= effect;
                } else {
                    currentEffects &= ~effect;
                }
            }
            return currentEffects;
        }

        public static String suppressedEffectsToString(int effects) {
            if (effects <= 0) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (true) {
                int[] iArr = ALL_SUPPRESSED_EFFECTS;
                if (i >= iArr.length) {
                    break;
                }
                int effect = iArr[i];
                if ((effects & effect) != 0) {
                    if (sb.length() > 0) {
                        sb.append(',');
                    }
                    sb.append(effectToString(effect));
                }
                effects &= ~effect;
                i++;
            }
            if (effects != 0) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append("UNKNOWN_");
                sb.append(effects);
            }
            return sb.toString();
        }

        public static String priorityCategoriesToString(int priorityCategories2) {
            if (priorityCategories2 == 0) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (true) {
                int[] iArr = ALL_PRIORITY_CATEGORIES;
                if (i >= iArr.length) {
                    break;
                }
                int priorityCategory = iArr[i];
                if ((priorityCategories2 & priorityCategory) != 0) {
                    if (sb.length() > 0) {
                        sb.append(',');
                    }
                    sb.append(priorityCategoryToString(priorityCategory));
                }
                priorityCategories2 &= ~priorityCategory;
                i++;
            }
            if (priorityCategories2 != 0) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append("PRIORITY_CATEGORY_UNKNOWN_");
                sb.append(priorityCategories2);
            }
            return sb.toString();
        }

        private static String effectToString(int effect) {
            if (effect == -1) {
                return "SUPPRESSED_EFFECTS_UNSET";
            }
            if (effect == 4) {
                return "SUPPRESSED_EFFECT_FULL_SCREEN_INTENT";
            }
            if (effect == 8) {
                return "SUPPRESSED_EFFECT_LIGHTS";
            }
            if (effect == 16) {
                return "SUPPRESSED_EFFECT_PEEK";
            }
            if (effect == 32) {
                return "SUPPRESSED_EFFECT_STATUS_BAR";
            }
            if (effect == 64) {
                return "SUPPRESSED_EFFECT_BADGE";
            }
            if (effect == 128) {
                return "SUPPRESSED_EFFECT_AMBIENT";
            }
            if (effect == 256) {
                return "SUPPRESSED_EFFECT_NOTIFICATION_LIST";
            }
            if (effect == 1) {
                return "SUPPRESSED_EFFECT_SCREEN_OFF";
            }
            if (effect == 2) {
                return "SUPPRESSED_EFFECT_SCREEN_ON";
            }
            return "UNKNOWN_" + effect;
        }

        private static String priorityCategoryToString(int priorityCategory) {
            if (priorityCategory == 1) {
                return "PRIORITY_CATEGORY_REMINDERS";
            }
            if (priorityCategory == 2) {
                return "PRIORITY_CATEGORY_EVENTS";
            }
            if (priorityCategory == 4) {
                return "PRIORITY_CATEGORY_MESSAGES";
            }
            if (priorityCategory == 8) {
                return "PRIORITY_CATEGORY_CALLS";
            }
            if (priorityCategory == 16) {
                return "PRIORITY_CATEGORY_REPEAT_CALLERS";
            }
            if (priorityCategory == 32) {
                return "PRIORITY_CATEGORY_ALARMS";
            }
            if (priorityCategory == 64) {
                return "PRIORITY_CATEGORY_MEDIA";
            }
            if (priorityCategory == 128) {
                return "PRIORITY_CATEGORY_SYSTEM";
            }
            return "PRIORITY_CATEGORY_UNKNOWN_" + priorityCategory;
        }

        public static String prioritySendersToString(int prioritySenders) {
            if (prioritySenders == 0) {
                return "PRIORITY_SENDERS_ANY";
            }
            if (prioritySenders == 1) {
                return "PRIORITY_SENDERS_CONTACTS";
            }
            if (prioritySenders == 2) {
                return "PRIORITY_SENDERS_STARRED";
            }
            return "PRIORITY_SENDERS_UNKNOWN_" + prioritySenders;
        }

        public boolean allowAlarms() {
            return (this.priorityCategories & 32) != 0;
        }

        public boolean allowMedia() {
            return (this.priorityCategories & 64) != 0;
        }

        public boolean allowSystem() {
            return (this.priorityCategories & 128) != 0;
        }

        public boolean allowRepeatCallers() {
            return (this.priorityCategories & 16) != 0;
        }

        public boolean allowCalls() {
            return (this.priorityCategories & 8) != 0;
        }

        public boolean allowMessages() {
            return (this.priorityCategories & 4) != 0;
        }

        public boolean allowEvents() {
            return (this.priorityCategories & 2) != 0;
        }

        public boolean allowReminders() {
            return (this.priorityCategories & 1) != 0;
        }

        public int allowCallsFrom() {
            return this.priorityCallSenders;
        }

        public int allowMessagesFrom() {
            return this.priorityMessageSenders;
        }

        public boolean showFullScreenIntents() {
            return (this.suppressedVisualEffects & 4) == 0;
        }

        public boolean showLights() {
            return (this.suppressedVisualEffects & 8) == 0;
        }

        public boolean showPeeking() {
            return (this.suppressedVisualEffects & 16) == 0;
        }

        public boolean showStatusBarIcons() {
            return (this.suppressedVisualEffects & 32) == 0;
        }

        public boolean showAmbient() {
            return (this.suppressedVisualEffects & 128) == 0;
        }

        public boolean showBadges() {
            return (this.suppressedVisualEffects & 64) == 0;
        }

        public boolean showInNotificationList() {
            return (this.suppressedVisualEffects & 256) == 0;
        }

        public Policy copy() {
            Parcel parcel = Parcel.obtain();
            try {
                writeToParcel(parcel, 0);
                parcel.setDataPosition(0);
                return new Policy(parcel);
            } finally {
                parcel.recycle();
            }
        }
    }

    public StatusBarNotification[] getActiveNotifications() {
        try {
            ParceledListSlice<StatusBarNotification> parceledList = getService().getAppActiveNotifications(this.mContext.getPackageName(), this.mContext.getUserId());
            if (parceledList == null) {
                return new StatusBarNotification[0];
            }
            List<StatusBarNotification> list = parceledList.getList();
            return (StatusBarNotification[]) list.toArray(new StatusBarNotification[list.size()]);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public final int getCurrentInterruptionFilter() {
        try {
            return zenModeToInterruptionFilter(getService().getZenMode());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public final void setInterruptionFilter(int interruptionFilter) {
        try {
            getService().setInterruptionFilter(this.mContext.getOpPackageName(), interruptionFilter);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int zenModeToInterruptionFilter(int zen) {
        if (zen == 0) {
            return 1;
        }
        if (zen == 1) {
            return 2;
        }
        if (zen == 2) {
            return 3;
        }
        if (zen != 3) {
            return 0;
        }
        return 4;
    }

    public static int zenModeFromInterruptionFilter(int interruptionFilter, int defValue) {
        if (interruptionFilter == 1) {
            return 0;
        }
        if (interruptionFilter == 2) {
            return 1;
        }
        if (interruptionFilter == 3) {
            return 2;
        }
        if (interruptionFilter != 4) {
            return defValue;
        }
        return 3;
    }
}
