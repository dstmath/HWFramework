package android.app;

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
import android.service.notification.StatusBarNotification;
import android.service.notification.ZenModeConfig;
import android.util.Log;
import android.util.proto.ProtoOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NotificationManager {
    public static final String ACTION_APP_BLOCK_STATE_CHANGED = "android.app.action.APP_BLOCK_STATE_CHANGED";
    public static final String ACTION_EFFECTS_SUPPRESSOR_CHANGED = "android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED";
    public static final String ACTION_INTERRUPTION_FILTER_CHANGED = "android.app.action.INTERRUPTION_FILTER_CHANGED";
    public static final String ACTION_INTERRUPTION_FILTER_CHANGED_INTERNAL = "android.app.action.INTERRUPTION_FILTER_CHANGED_INTERNAL";
    public static final String ACTION_NOTIFICATION_CHANNEL_BLOCK_STATE_CHANGED = "android.app.action.NOTIFICATION_CHANNEL_BLOCK_STATE_CHANGED";
    public static final String ACTION_NOTIFICATION_CHANNEL_GROUP_BLOCK_STATE_CHANGED = "android.app.action.NOTIFICATION_CHANNEL_GROUP_BLOCK_STATE_CHANGED";
    public static final String ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED = "android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED";
    public static final String ACTION_NOTIFICATION_POLICY_CHANGED = "android.app.action.NOTIFICATION_POLICY_CHANGED";
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
    private static String TAG = "NotificationManager";
    public static final int VISIBILITY_NO_OVERRIDE = -1000;
    private static boolean localLOGV = false;
    private static INotificationManager sService;
    private Context mContext;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Importance {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface InterruptionFilter {
    }

    public static class Policy implements Parcelable {
        public static final int[] ALL_PRIORITY_CATEGORIES = {32, 64, 128, 1, 2, 4, 8, 16};
        private static final int[] ALL_SUPPRESSED_EFFECTS = {1, 2, 4, 8, 16, 64, 128};
        public static final Parcelable.Creator<Policy> CREATOR = new Parcelable.Creator<Policy>() {
            public Policy createFromParcel(Parcel in) {
                return new Policy(in);
            }

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

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.priorityCategories);
            dest.writeInt(this.priorityCallSenders);
            dest.writeInt(this.priorityMessageSenders);
            dest.writeInt(this.suppressedVisualEffects);
            dest.writeInt(this.state);
        }

        public int describeContents() {
            return 0;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(this.priorityCategories), Integer.valueOf(this.priorityCallSenders), Integer.valueOf(this.priorityMessageSenders), Integer.valueOf(this.suppressedVisualEffects)});
        }

        public boolean equals(Object o) {
            if (!(o instanceof Policy)) {
                return false;
            }
            boolean z = true;
            if (o == this) {
                return true;
            }
            Policy other = (Policy) o;
            if (!(other.priorityCategories == this.priorityCategories && other.priorityCallSenders == this.priorityCallSenders && other.priorityMessageSenders == this.priorityMessageSenders && other.suppressedVisualEffects == this.suppressedVisualEffects)) {
                z = false;
            }
            return z;
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
            proto.write(PolicyProto.PRIORITY_MESSAGE_SENDER, this.priorityMessageSenders);
            bitwiseToProtoEnum(proto, PolicyProto.SUPPRESSED_VISUAL_EFFECTS, this.suppressedVisualEffects);
            proto.end(pToken);
        }

        private static void bitwiseToProtoEnum(ProtoOutputStream proto, long fieldId, int data) {
            int i = 1;
            for (int data2 = data; data2 > 0; data2 >>>= 1) {
                if ((data2 & 1) == 1) {
                    proto.write(fieldId, i);
                }
                i++;
            }
        }

        public static int getAllSuppressedVisualEffects() {
            int effects = 0;
            for (int i : ALL_SUPPRESSED_EFFECTS) {
                effects |= i;
            }
            return effects;
        }

        public static boolean areAllVisualEffectsSuppressed(int effects) {
            for (int effect : ALL_SUPPRESSED_EFFECTS) {
                if ((effects & effect) == 0) {
                    return false;
                }
            }
            return true;
        }

        public static boolean areAnyScreenOffEffectsSuppressed(int effects) {
            for (int effect : SCREEN_OFF_SUPPRESSED_EFFECTS) {
                if ((effects & effect) != 0) {
                    return true;
                }
            }
            return false;
        }

        public static boolean areAnyScreenOnEffectsSuppressed(int effects) {
            for (int effect : SCREEN_ON_SUPPRESSED_EFFECTS) {
                if ((effects & effect) != 0) {
                    return true;
                }
            }
            return false;
        }

        public static int toggleScreenOffEffectsSuppressed(int currentEffects, boolean suppress) {
            return toggleEffects(currentEffects, SCREEN_OFF_SUPPRESSED_EFFECTS, suppress);
        }

        public static int toggleScreenOnEffectsSuppressed(int currentEffects, boolean suppress) {
            return toggleEffects(currentEffects, SCREEN_ON_SUPPRESSED_EFFECTS, suppress);
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
            for (int effect : ALL_SUPPRESSED_EFFECTS) {
                if ((effects & effect) != 0) {
                    if (sb.length() > 0) {
                        sb.append(',');
                    }
                    sb.append(effectToString(effect));
                }
                effects &= ~effect;
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
            for (int priorityCategory : ALL_PRIORITY_CATEGORIES) {
                if ((priorityCategories2 & priorityCategory) != 0) {
                    if (sb.length() > 0) {
                        sb.append(',');
                    }
                    sb.append(priorityCategoryToString(priorityCategory));
                }
                priorityCategories2 &= ~priorityCategory;
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
            switch (effect) {
                case 1:
                    return "SUPPRESSED_EFFECT_SCREEN_OFF";
                case 2:
                    return "SUPPRESSED_EFFECT_SCREEN_ON";
                default:
                    return "UNKNOWN_" + effect;
            }
        }

        private static String priorityCategoryToString(int priorityCategory) {
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
            switch (priorityCategory) {
                case 1:
                    return "PRIORITY_CATEGORY_REMINDERS";
                case 2:
                    return "PRIORITY_CATEGORY_EVENTS";
                default:
                    return "PRIORITY_CATEGORY_UNKNOWN_" + priorityCategory;
            }
        }

        public static String prioritySendersToString(int prioritySenders) {
            switch (prioritySenders) {
                case 0:
                    return "PRIORITY_SENDERS_ANY";
                case 1:
                    return "PRIORITY_SENDERS_CONTACTS";
                case 2:
                    return "PRIORITY_SENDERS_STARRED";
                default:
                    return "PRIORITY_SENDERS_UNKNOWN_" + prioritySenders;
            }
        }
    }

    public static INotificationManager getService() {
        if (sService != null) {
            return sService;
        }
        sService = INotificationManager.Stub.asInterface(ServiceManager.getService(Context.NOTIFICATION_SERVICE));
        return sService;
    }

    NotificationManager(Context context, Handler handler) {
        this.mContext = context;
    }

    public static NotificationManager from(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void notify(int id, Notification notification) {
        notify(null, id, notification);
    }

    public void notify(String tag, int id, Notification notification) {
        notifyAsUser(tag, id, notification, this.mContext.getUser());
    }

    public void notifyAsUser(String tag, int id, Notification notification, UserHandle user) {
        String pkg = this.mContext.getPackageName();
        INotificationManager service = getService();
        Notification.addFieldsFromContext(this.mContext, notification);
        if (notification.sound != null) {
            notification.sound = notification.sound.getCanonicalUri();
            if (StrictMode.vmFileUriExposureEnabled()) {
                notification.sound.checkFileUriExposed("Notification.sound");
            }
        }
        fixLegacySmallIcon(notification, pkg);
        if (this.mContext.getApplicationInfo().targetSdkVersion <= 22 || notification.getSmallIcon() != null) {
            if (localLOGV) {
                String str = TAG;
                Log.v(str, pkg + ": notify(" + id + ", " + notification + ")");
            }
            notification.reduceImageSizes(this.mContext);
            try {
                service.enqueueNotificationWithTag(pkg, this.mContext.getOpPackageName(), tag, id, Notification.Builder.maybeCloneStrippedForDelivery(notification, ((ActivityManager) this.mContext.getSystemService(Context.ACTIVITY_SERVICE)).isLowRamDevice(), this.mContext), user.getIdentifier());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("Invalid notification (no valid small icon): " + notification);
        }
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

    public void cancelAsUser(String tag, int id, UserHandle user) {
        INotificationManager service = getService();
        String pkg = this.mContext.getPackageName();
        if (localLOGV) {
            String str = TAG;
            Log.v(str, pkg + ": cancel(" + id + ")");
        }
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

    public void createNotificationChannelGroup(NotificationChannelGroup group) {
        createNotificationChannelGroups(Arrays.asList(new NotificationChannelGroup[]{group}));
    }

    public void createNotificationChannelGroups(List<NotificationChannelGroup> groups) {
        try {
            getService().createNotificationChannelGroups(this.mContext.getPackageName(), new ParceledListSlice(groups));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void createNotificationChannel(NotificationChannel channel) {
        createNotificationChannels(Arrays.asList(new NotificationChannel[]{channel}));
    }

    public void createNotificationChannels(List<NotificationChannel> channels) {
        try {
            getService().createNotificationChannels(this.mContext.getPackageName(), new ParceledListSlice(channels));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public NotificationChannel getNotificationChannel(String channelId) {
        try {
            return getService().getNotificationChannel(this.mContext.getPackageName(), channelId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<NotificationChannel> getNotificationChannels() {
        try {
            return getService().getNotificationChannels(this.mContext.getPackageName()).getList();
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
            return getService().getNotificationChannelGroups(this.mContext.getPackageName()).getList();
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

    public ZenModeConfig getZenModeConfig() {
        try {
            return getService().getZenModeConfig();
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
        try {
            List<ZenModeConfig.ZenRule> rules = getService().getZenRules();
            Map<String, AutomaticZenRule> ruleMap = new HashMap<>();
            for (ZenModeConfig.ZenRule rule : rules) {
                String str = rule.id;
                AutomaticZenRule automaticZenRule = new AutomaticZenRule(rule.name, rule.component, rule.conditionId, zenModeToInterruptionFilter(rule.zenMode), rule.enabled, rule.creationTime);
                ruleMap.put(str, automaticZenRule);
            }
            return ruleMap;
        } catch (RemoteException e) {
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

    public boolean isNotificationAssistantAccessGranted(ComponentName assistant) {
        try {
            return getService().isNotificationAssistantAccessGranted(assistant);
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

    public List<ComponentName> getEnabledNotificationListeners(int userId) {
        try {
            return getService().getEnabledNotificationListeners(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private static void checkRequired(String name, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required");
        }
    }

    public StatusBarNotification[] getActiveNotifications() {
        try {
            List<StatusBarNotification> list = getService().getAppActiveNotifications(this.mContext.getPackageName(), this.mContext.getUserId()).getList();
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
        switch (zen) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 4;
            default:
                return 0;
        }
    }

    public static int zenModeFromInterruptionFilter(int interruptionFilter, int defValue) {
        switch (interruptionFilter) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            default:
                return defValue;
        }
    }
}
