package android.app;

import android.app.INotificationManager.Stub;
import android.app.Notification.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.ZenRule;
import android.util.ArraySet;
import android.util.Log;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NotificationManager {
    public static final String ACTION_EFFECTS_SUPPRESSOR_CHANGED = "android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED";
    public static final String ACTION_INTERRUPTION_FILTER_CHANGED = "android.app.action.INTERRUPTION_FILTER_CHANGED";
    public static final String ACTION_INTERRUPTION_FILTER_CHANGED_INTERNAL = "android.app.action.INTERRUPTION_FILTER_CHANGED_INTERNAL";
    public static final String ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED = "android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED";
    public static final String ACTION_NOTIFICATION_POLICY_CHANGED = "android.app.action.NOTIFICATION_POLICY_CHANGED";
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
    public static final int MAX_HEIGHT_FOR_NOTIFICATION_ICON = 160;
    public static final int MAX_WIDTH_FOR_NOTIFICATION_ICON = 160;
    private static String TAG = null;
    public static final int VISIBILITY_NO_OVERRIDE = -1000;
    private static boolean localLOGV;
    private static INotificationManager sService;
    private Context mContext;

    public static class Policy implements Parcelable {
        private static final int[] ALL_PRIORITY_CATEGORIES = null;
        private static final int[] ALL_SUPPRESSED_EFFECTS = null;
        public static final Creator<Policy> CREATOR = null;
        public static final int PRIORITY_CATEGORY_CALLS = 8;
        public static final int PRIORITY_CATEGORY_EVENTS = 2;
        public static final int PRIORITY_CATEGORY_MESSAGES = 4;
        public static final int PRIORITY_CATEGORY_REMINDERS = 1;
        public static final int PRIORITY_CATEGORY_REPEAT_CALLERS = 16;
        public static final int PRIORITY_SENDERS_ANY = 0;
        public static final int PRIORITY_SENDERS_CONTACTS = 1;
        public static final int PRIORITY_SENDERS_STARRED = 2;
        public static final int SUPPRESSED_EFFECTS_UNSET = -1;
        public static final int SUPPRESSED_EFFECT_SCREEN_OFF = 1;
        public static final int SUPPRESSED_EFFECT_SCREEN_ON = 2;
        public final int priorityCallSenders;
        public final int priorityCategories;
        public final int priorityMessageSenders;
        public final int suppressedVisualEffects;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.NotificationManager.Policy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.NotificationManager.Policy.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.NotificationManager.Policy.<clinit>():void");
        }

        public static java.lang.String priorityCategoriesToString(int r1) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.NotificationManager.Policy.priorityCategoriesToString(int):java.lang.String
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.NotificationManager.Policy.priorityCategoriesToString(int):java.lang.String");
        }

        public static java.lang.String suppressedEffectsToString(int r1) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.NotificationManager.Policy.suppressedEffectsToString(int):java.lang.String
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.NotificationManager.Policy.suppressedEffectsToString(int):java.lang.String");
        }

        public Policy(int priorityCategories, int priorityCallSenders, int priorityMessageSenders) {
            this(priorityCategories, priorityCallSenders, priorityMessageSenders, SUPPRESSED_EFFECTS_UNSET);
        }

        public Policy(int priorityCategories, int priorityCallSenders, int priorityMessageSenders, int suppressedVisualEffects) {
            this.priorityCategories = priorityCategories;
            this.priorityCallSenders = priorityCallSenders;
            this.priorityMessageSenders = priorityMessageSenders;
            this.suppressedVisualEffects = suppressedVisualEffects;
        }

        public Policy(Parcel source) {
            this(source.readInt(), source.readInt(), source.readInt(), source.readInt());
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.priorityCategories);
            dest.writeInt(this.priorityCallSenders);
            dest.writeInt(this.priorityMessageSenders);
            dest.writeInt(this.suppressedVisualEffects);
        }

        public int describeContents() {
            return PRIORITY_SENDERS_ANY;
        }

        public int hashCode() {
            Object[] objArr = new Object[PRIORITY_CATEGORY_MESSAGES];
            objArr[PRIORITY_SENDERS_ANY] = Integer.valueOf(this.priorityCategories);
            objArr[SUPPRESSED_EFFECT_SCREEN_OFF] = Integer.valueOf(this.priorityCallSenders);
            objArr[SUPPRESSED_EFFECT_SCREEN_ON] = Integer.valueOf(this.priorityMessageSenders);
            objArr[NotificationManager.INTERRUPTION_FILTER_NONE] = Integer.valueOf(this.suppressedVisualEffects);
            return Objects.hash(objArr);
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (!(o instanceof Policy)) {
                return false;
            }
            if (o == this) {
                return true;
            }
            Policy other = (Policy) o;
            if (other.priorityCategories != this.priorityCategories || other.priorityCallSenders != this.priorityCallSenders || other.priorityMessageSenders != this.priorityMessageSenders) {
                z = false;
            } else if (other.suppressedVisualEffects != this.suppressedVisualEffects) {
                z = false;
            }
            return z;
        }

        public String toString() {
            return "NotificationManager.Policy[priorityCategories=" + priorityCategoriesToString(this.priorityCategories) + ",priorityCallSenders=" + prioritySendersToString(this.priorityCallSenders) + ",priorityMessageSenders=" + prioritySendersToString(this.priorityMessageSenders) + ",suppressedVisualEffects=" + suppressedEffectsToString(this.suppressedVisualEffects) + "]";
        }

        private static String effectToString(int effect) {
            switch (effect) {
                case SUPPRESSED_EFFECTS_UNSET /*-1*/:
                    return "SUPPRESSED_EFFECTS_UNSET";
                case SUPPRESSED_EFFECT_SCREEN_OFF /*1*/:
                    return "SUPPRESSED_EFFECT_SCREEN_OFF";
                case SUPPRESSED_EFFECT_SCREEN_ON /*2*/:
                    return "SUPPRESSED_EFFECT_SCREEN_ON";
                default:
                    return "UNKNOWN_" + effect;
            }
        }

        private static String priorityCategoryToString(int priorityCategory) {
            switch (priorityCategory) {
                case SUPPRESSED_EFFECT_SCREEN_OFF /*1*/:
                    return "PRIORITY_CATEGORY_REMINDERS";
                case SUPPRESSED_EFFECT_SCREEN_ON /*2*/:
                    return "PRIORITY_CATEGORY_EVENTS";
                case PRIORITY_CATEGORY_MESSAGES /*4*/:
                    return "PRIORITY_CATEGORY_MESSAGES";
                case PRIORITY_CATEGORY_CALLS /*8*/:
                    return "PRIORITY_CATEGORY_CALLS";
                case PRIORITY_CATEGORY_REPEAT_CALLERS /*16*/:
                    return "PRIORITY_CATEGORY_REPEAT_CALLERS";
                default:
                    return "PRIORITY_CATEGORY_UNKNOWN_" + priorityCategory;
            }
        }

        public static String prioritySendersToString(int prioritySenders) {
            switch (prioritySenders) {
                case PRIORITY_SENDERS_ANY /*0*/:
                    return "PRIORITY_SENDERS_ANY";
                case SUPPRESSED_EFFECT_SCREEN_OFF /*1*/:
                    return "PRIORITY_SENDERS_CONTACTS";
                case SUPPRESSED_EFFECT_SCREEN_ON /*2*/:
                    return "PRIORITY_SENDERS_STARRED";
                default:
                    return "PRIORITY_SENDERS_UNKNOWN_" + prioritySenders;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.NotificationManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.NotificationManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.NotificationManager.<clinit>():void");
    }

    public static INotificationManager getService() {
        if (sService != null) {
            return sService;
        }
        sService = Stub.asInterface(ServiceManager.getService(Context.NOTIFICATION_SERVICE));
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
        notifyAsUser(tag, id, notification, new UserHandle(UserHandle.myUserId()));
    }

    public void notifyAsUser(String tag, int id, Notification notification, UserHandle user) {
        int[] idOut = new int[INTERRUPTION_FILTER_ALL];
        INotificationManager service = getService();
        String pkg = this.mContext.getPackageName();
        Notification.addFieldsFromContext(this.mContext, notification);
        if (notification.sound != null) {
            notification.sound = notification.sound.getCanonicalUri();
            if (StrictMode.vmFileUriExposureEnabled()) {
                notification.sound.checkFileUriExposed("Notification.sound");
            }
        }
        fixLegacySmallIcon(notification, pkg);
        fixLegacyLargeIcon(notification, pkg);
        if (this.mContext.getApplicationInfo().targetSdkVersion <= 22 || notification.getSmallIcon() != null) {
            if (localLOGV) {
                Log.v(TAG, pkg + ": notify(" + id + ", " + notification + ")");
            }
            try {
                service.enqueueNotificationWithTag(pkg, this.mContext.getOpPackageName(), tag, id, Builder.maybeCloneStrippedForDelivery(notification), idOut, user.getIdentifier());
                if (id != idOut[INTERRUPTION_FILTER_UNKNOWN]) {
                    Log.w(TAG, "notify: id corrupted: sent " + id + ", got back " + idOut[INTERRUPTION_FILTER_UNKNOWN]);
                    return;
                }
                return;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        throw new IllegalArgumentException("Invalid notification (no valid small icon): " + notification);
    }

    private void fixLegacySmallIcon(Notification n, String pkg) {
        if (n.getSmallIcon() == null && n.icon != 0) {
            n.setSmallIcon(Icon.createWithResource(pkg, n.icon));
        }
    }

    private void fixLegacyLargeIcon(Notification n, String pkg) {
        Icon large = n.getLargeIcon();
        if (large != null && large.getType() == INTERRUPTION_FILTER_ALL) {
            Bitmap src = large.getBitmap();
            if (src.getWidth() > MAX_WIDTH_FOR_NOTIFICATION_ICON) {
                Bitmap dest = Bitmap.createScaledBitmap(src, MAX_WIDTH_FOR_NOTIFICATION_ICON, MAX_WIDTH_FOR_NOTIFICATION_ICON, true);
                if (dest != null) {
                    try {
                        n.setLargeIcon(Icon.createWithBitmap(dest));
                    } catch (Exception e) {
                        Log.w(TAG, "adjust notification icon fail");
                    }
                }
            }
        }
    }

    public void cancel(int id) {
        cancel(null, id);
    }

    public void cancel(String tag, int id) {
        cancelAsUser(tag, id, new UserHandle(UserHandle.myUserId()));
    }

    public void cancelAsUser(String tag, int id, UserHandle user) {
        INotificationManager service = getService();
        String pkg = this.mContext.getPackageName();
        if (localLOGV) {
            Log.v(TAG, pkg + ": cancel(" + id + ")");
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
            Log.v(TAG, pkg + ": cancelAll()");
        }
        try {
            service.cancelAllNotifications(pkg, UserHandle.myUserId());
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
            List<ZenRule> rules = getService().getZenRules();
            Map<String, AutomaticZenRule> ruleMap = new HashMap();
            for (ZenRule rule : rules) {
                ruleMap.put(rule.id, new AutomaticZenRule(rule.name, rule.component, rule.conditionId, zenModeToInterruptionFilter(rule.zenMode), rule.enabled, rule.creationTime));
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

    public boolean isNotificationPolicyAccessGrantedForPackage(String pkg) {
        try {
            return getService().isNotificationPolicyAccessGrantedForPackage(pkg);
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

    public ArraySet<String> getPackagesRequestingNotificationPolicyAccess() {
        try {
            String[] pkgs = getService().getPackagesRequestingNotificationPolicyAccess();
            if (pkgs == null || pkgs.length <= 0) {
                return new ArraySet();
            }
            ArraySet<String> rt = new ArraySet(pkgs.length);
            for (int i = INTERRUPTION_FILTER_UNKNOWN; i < pkgs.length; i += INTERRUPTION_FILTER_ALL) {
                rt.add(pkgs[i]);
            }
            return rt;
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
            List<StatusBarNotification> list = getService().getAppActiveNotifications(this.mContext.getPackageName(), UserHandle.myUserId()).getList();
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
            case INTERRUPTION_FILTER_UNKNOWN /*0*/:
                return INTERRUPTION_FILTER_ALL;
            case INTERRUPTION_FILTER_ALL /*1*/:
                return INTERRUPTION_FILTER_PRIORITY;
            case INTERRUPTION_FILTER_PRIORITY /*2*/:
                return INTERRUPTION_FILTER_NONE;
            case INTERRUPTION_FILTER_NONE /*3*/:
                return INTERRUPTION_FILTER_ALARMS;
            default:
                return INTERRUPTION_FILTER_UNKNOWN;
        }
    }

    public static int zenModeFromInterruptionFilter(int interruptionFilter, int defValue) {
        switch (interruptionFilter) {
            case INTERRUPTION_FILTER_ALL /*1*/:
                return INTERRUPTION_FILTER_UNKNOWN;
            case INTERRUPTION_FILTER_PRIORITY /*2*/:
                return INTERRUPTION_FILTER_ALL;
            case INTERRUPTION_FILTER_NONE /*3*/:
                return INTERRUPTION_FILTER_PRIORITY;
            case INTERRUPTION_FILTER_ALARMS /*4*/:
                return INTERRUPTION_FILTER_NONE;
            default:
                return defValue;
        }
    }
}
