package android.service.notification;

import android.app.INotificationManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.security.keystore.KeyProperties;
import android.service.notification.INotificationListener.Stub;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.view.Surface;
import android.widget.RemoteViews;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.SomeArgs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class NotificationListenerService extends Service {
    public static final int HINT_HOST_DISABLE_CALL_EFFECTS = 4;
    public static final int HINT_HOST_DISABLE_EFFECTS = 1;
    public static final int HINT_HOST_DISABLE_NOTIFICATION_EFFECTS = 2;
    public static final int INTERRUPTION_FILTER_ALARMS = 4;
    public static final int INTERRUPTION_FILTER_ALL = 1;
    public static final int INTERRUPTION_FILTER_NONE = 3;
    public static final int INTERRUPTION_FILTER_PRIORITY = 2;
    public static final int INTERRUPTION_FILTER_UNKNOWN = 0;
    public static final int NOTIFICATION_CHANNEL_OR_GROUP_ADDED = 1;
    public static final int NOTIFICATION_CHANNEL_OR_GROUP_DELETED = 3;
    public static final int NOTIFICATION_CHANNEL_OR_GROUP_UPDATED = 2;
    public static final int REASON_APP_CANCEL = 8;
    public static final int REASON_APP_CANCEL_ALL = 9;
    public static final int REASON_CANCEL = 2;
    public static final int REASON_CANCEL_ALL = 3;
    public static final int REASON_CHANNEL_BANNED = 17;
    public static final int REASON_CLICK = 1;
    public static final int REASON_ERROR = 4;
    public static final int REASON_GROUP_OPTIMIZATION = 13;
    public static final int REASON_GROUP_SUMMARY_CANCELED = 12;
    public static final int REASON_LISTENER_CANCEL = 10;
    public static final int REASON_LISTENER_CANCEL_ALL = 11;
    public static final int REASON_PACKAGE_BANNED = 7;
    public static final int REASON_PACKAGE_CHANGED = 5;
    public static final int REASON_PACKAGE_SUSPENDED = 14;
    public static final int REASON_PROFILE_TURNED_OFF = 15;
    public static final int REASON_SNOOZED = 18;
    public static final int REASON_TIMEOUT = 19;
    public static final int REASON_UNAUTOBUNDLED = 16;
    public static final int REASON_USER_STOPPED = 6;
    public static final String SERVICE_INTERFACE = "android.service.notification.NotificationListenerService";
    public static final int SUPPRESSED_EFFECT_SCREEN_OFF = 1;
    public static final int SUPPRESSED_EFFECT_SCREEN_ON = 2;
    public static final int TRIM_FULL = 0;
    public static final int TRIM_LIGHT = 1;
    private final String TAG = getClass().getSimpleName();
    private boolean isConnected = false;
    protected int mCurrentUser;
    private Handler mHandler;
    private final Object mLock = new Object();
    private INotificationManager mNoMan;
    @GuardedBy("mLock")
    private RankingMap mRankingMap;
    protected Context mSystemContext;
    protected NotificationListenerWrapper mWrapper = null;

    protected class NotificationListenerWrapper extends Stub {
        protected NotificationListenerWrapper() {
        }

        public void onNotificationPosted(IStatusBarNotificationHolder sbnHolder, NotificationRankingUpdate update) {
            try {
                Object sbn = sbnHolder.get();
                try {
                    NotificationListenerService.this.createLegacyIconExtras(sbn.getNotification());
                    NotificationListenerService.this.maybePopulateRemoteViews(sbn.getNotification());
                } catch (IllegalArgumentException e) {
                    Log.w(NotificationListenerService.this.TAG, "onNotificationPosted: can't rebuild notification from " + sbn.getPackageName());
                    sbn = null;
                }
                synchronized (NotificationListenerService.this.mLock) {
                    NotificationListenerService.this.applyUpdateLocked(update);
                    if (sbn != null) {
                        SomeArgs args = SomeArgs.obtain();
                        args.arg1 = sbn;
                        args.arg2 = NotificationListenerService.this.mRankingMap;
                        NotificationListenerService.this.mHandler.obtainMessage(1, args).sendToTarget();
                    } else {
                        NotificationListenerService.this.mHandler.obtainMessage(4, NotificationListenerService.this.mRankingMap).sendToTarget();
                    }
                }
            } catch (RemoteException e2) {
                Log.w(NotificationListenerService.this.TAG, "onNotificationPosted: Error receiving StatusBarNotification", e2);
            }
        }

        public void onNotificationRemoved(IStatusBarNotificationHolder sbnHolder, NotificationRankingUpdate update, int reason) {
            try {
                StatusBarNotification sbn = sbnHolder.get();
                synchronized (NotificationListenerService.this.mLock) {
                    NotificationListenerService.this.applyUpdateLocked(update);
                    SomeArgs args = SomeArgs.obtain();
                    args.arg1 = sbn;
                    args.arg2 = NotificationListenerService.this.mRankingMap;
                    args.arg3 = Integer.valueOf(reason);
                    NotificationListenerService.this.mHandler.obtainMessage(2, args).sendToTarget();
                }
            } catch (RemoteException e) {
                Log.w(NotificationListenerService.this.TAG, "onNotificationRemoved: Error receiving StatusBarNotification", e);
            }
        }

        public void onListenerConnected(NotificationRankingUpdate update) {
            synchronized (NotificationListenerService.this.mLock) {
                NotificationListenerService.this.applyUpdateLocked(update);
            }
            NotificationListenerService.this.isConnected = true;
            NotificationListenerService.this.mHandler.obtainMessage(3).sendToTarget();
        }

        public void onNotificationRankingUpdate(NotificationRankingUpdate update) throws RemoteException {
            synchronized (NotificationListenerService.this.mLock) {
                NotificationListenerService.this.applyUpdateLocked(update);
                NotificationListenerService.this.mHandler.obtainMessage(4, NotificationListenerService.this.mRankingMap).sendToTarget();
            }
        }

        public void onListenerHintsChanged(int hints) throws RemoteException {
            NotificationListenerService.this.mHandler.obtainMessage(5, hints, 0).sendToTarget();
        }

        public void onInterruptionFilterChanged(int interruptionFilter) throws RemoteException {
            NotificationListenerService.this.mHandler.obtainMessage(6, interruptionFilter, 0).sendToTarget();
        }

        public void onNotificationEnqueued(IStatusBarNotificationHolder notificationHolder) throws RemoteException {
        }

        public void onNotificationSnoozedUntilContext(IStatusBarNotificationHolder notificationHolder, String snoozeCriterionId) throws RemoteException {
        }

        public void onNotificationChannelModification(String pkgName, UserHandle user, NotificationChannel channel, int modificationType) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = pkgName;
            args.arg2 = user;
            args.arg3 = channel;
            args.arg4 = Integer.valueOf(modificationType);
            NotificationListenerService.this.mHandler.obtainMessage(7, args).sendToTarget();
        }

        public void onNotificationChannelGroupModification(String pkgName, UserHandle user, NotificationChannelGroup group, int modificationType) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = pkgName;
            args.arg2 = user;
            args.arg3 = group;
            args.arg4 = Integer.valueOf(modificationType);
            NotificationListenerService.this.mHandler.obtainMessage(8, args).sendToTarget();
        }
    }

    private final class MyHandler extends Handler {
        public static final int MSG_ON_INTERRUPTION_FILTER_CHANGED = 6;
        public static final int MSG_ON_LISTENER_CONNECTED = 3;
        public static final int MSG_ON_LISTENER_HINTS_CHANGED = 5;
        public static final int MSG_ON_NOTIFICATION_CHANNEL_GROUP_MODIFIED = 8;
        public static final int MSG_ON_NOTIFICATION_CHANNEL_MODIFIED = 7;
        public static final int MSG_ON_NOTIFICATION_POSTED = 1;
        public static final int MSG_ON_NOTIFICATION_RANKING_UPDATE = 4;
        public static final int MSG_ON_NOTIFICATION_REMOVED = 2;

        public MyHandler(Looper looper) {
            super(looper, null, false);
        }

        public void handleMessage(Message msg) {
            if (NotificationListenerService.this.isConnected) {
                SomeArgs args;
                StatusBarNotification sbn;
                RankingMap rankingMap;
                switch (msg.what) {
                    case 1:
                        args = msg.obj;
                        sbn = args.arg1;
                        rankingMap = args.arg2;
                        args.recycle();
                        NotificationListenerService.this.onNotificationPosted(sbn, rankingMap);
                        break;
                    case 2:
                        args = (SomeArgs) msg.obj;
                        sbn = (StatusBarNotification) args.arg1;
                        rankingMap = (RankingMap) args.arg2;
                        int reason = ((Integer) args.arg3).intValue();
                        args.recycle();
                        NotificationListenerService.this.onNotificationRemoved(sbn, rankingMap, reason);
                        break;
                    case 3:
                        NotificationListenerService.this.onListenerConnected();
                        break;
                    case 4:
                        NotificationListenerService.this.onNotificationRankingUpdate((RankingMap) msg.obj);
                        break;
                    case 5:
                        NotificationListenerService.this.onListenerHintsChanged(msg.arg1);
                        break;
                    case 6:
                        NotificationListenerService.this.onInterruptionFilterChanged(msg.arg1);
                        break;
                    case 7:
                        args = (SomeArgs) msg.obj;
                        NotificationListenerService.this.onNotificationChannelModified(args.arg1, args.arg2, args.arg3, ((Integer) args.arg4).intValue());
                        break;
                    case 8:
                        args = (SomeArgs) msg.obj;
                        NotificationListenerService.this.onNotificationChannelGroupModified((String) args.arg1, (UserHandle) args.arg2, args.arg3, ((Integer) args.arg4).intValue());
                        break;
                }
            }
        }
    }

    public static class Ranking {
        public static final int VISIBILITY_NO_OVERRIDE = -1000;
        private NotificationChannel mChannel;
        private int mImportance;
        private CharSequence mImportanceExplanation;
        private boolean mIsAmbient;
        private String mKey;
        private boolean mMatchesInterruptionFilter;
        private String mOverrideGroupKey;
        private ArrayList<String> mOverridePeople;
        private int mRank = -1;
        private boolean mShowBadge;
        private ArrayList<SnoozeCriterion> mSnoozeCriteria;
        private int mSuppressedVisualEffects;
        private int mVisibilityOverride;

        public String getKey() {
            return this.mKey;
        }

        public int getRank() {
            return this.mRank;
        }

        public boolean isAmbient() {
            return this.mIsAmbient;
        }

        public int getVisibilityOverride() {
            return this.mVisibilityOverride;
        }

        public int getSuppressedVisualEffects() {
            return this.mSuppressedVisualEffects;
        }

        public boolean matchesInterruptionFilter() {
            return this.mMatchesInterruptionFilter;
        }

        public int getImportance() {
            return this.mImportance;
        }

        public CharSequence getImportanceExplanation() {
            return this.mImportanceExplanation;
        }

        public String getOverrideGroupKey() {
            return this.mOverrideGroupKey;
        }

        public NotificationChannel getChannel() {
            return this.mChannel;
        }

        public List<String> getAdditionalPeople() {
            return this.mOverridePeople;
        }

        public List<SnoozeCriterion> getSnoozeCriteria() {
            return this.mSnoozeCriteria;
        }

        public boolean canShowBadge() {
            return this.mShowBadge;
        }

        private void populate(String key, int rank, boolean matchesInterruptionFilter, int visibilityOverride, int suppressedVisualEffects, int importance, CharSequence explanation, String overrideGroupKey, NotificationChannel channel, ArrayList<String> overridePeople, ArrayList<SnoozeCriterion> snoozeCriteria, boolean showBadge) {
            this.mKey = key;
            this.mRank = rank;
            this.mIsAmbient = importance < 2;
            this.mMatchesInterruptionFilter = matchesInterruptionFilter;
            this.mVisibilityOverride = visibilityOverride;
            this.mSuppressedVisualEffects = suppressedVisualEffects;
            this.mImportance = importance;
            this.mImportanceExplanation = explanation;
            this.mOverrideGroupKey = overrideGroupKey;
            this.mChannel = channel;
            this.mOverridePeople = overridePeople;
            this.mSnoozeCriteria = snoozeCriteria;
            this.mShowBadge = showBadge;
        }

        public static String importanceToString(int importance) {
            switch (importance) {
                case -1000:
                    return "UNSPECIFIED";
                case 0:
                    return KeyProperties.DIGEST_NONE;
                case 1:
                    return "MIN";
                case 2:
                    return "LOW";
                case 3:
                    return "DEFAULT";
                case 4:
                case 5:
                    return "HIGH";
                default:
                    return "UNKNOWN(" + String.valueOf(importance) + ")";
            }
        }
    }

    public static class RankingMap implements Parcelable {
        public static final Creator<RankingMap> CREATOR = new Creator<RankingMap>() {
            public RankingMap createFromParcel(Parcel source) {
                return new RankingMap((NotificationRankingUpdate) source.readParcelable(null), null);
            }

            public RankingMap[] newArray(int size) {
                return new RankingMap[size];
            }
        };
        private ArrayMap<String, NotificationChannel> mChannels;
        private ArrayMap<String, Integer> mImportance;
        private ArrayMap<String, String> mImportanceExplanation;
        private ArraySet<Object> mIntercepted;
        private ArrayMap<String, String> mOverrideGroupKeys;
        private ArrayMap<String, ArrayList<String>> mOverridePeople;
        private final NotificationRankingUpdate mRankingUpdate;
        private ArrayMap<String, Integer> mRanks;
        private ArrayMap<String, Boolean> mShowBadge;
        private ArrayMap<String, ArrayList<SnoozeCriterion>> mSnoozeCriteria;
        private ArrayMap<String, Integer> mSuppressedVisualEffects;
        private ArrayMap<String, Integer> mVisibilityOverrides;

        /* synthetic */ RankingMap(NotificationRankingUpdate rankingUpdate, RankingMap -this1) {
            this(rankingUpdate);
        }

        private RankingMap(NotificationRankingUpdate rankingUpdate) {
            this.mRankingUpdate = rankingUpdate;
        }

        public String[] getOrderedKeys() {
            return this.mRankingUpdate.getOrderedKeys();
        }

        public boolean getRanking(String key, Ranking outRanking) {
            int rank = getRank(key);
            outRanking.populate(key, rank, isIntercepted(key) ^ 1, getVisibilityOverride(key), getSuppressedVisualEffects(key), getImportance(key), getImportanceExplanation(key), getOverrideGroupKey(key), getChannel(key), getOverridePeople(key), getSnoozeCriteria(key), getShowBadge(key));
            return rank >= 0;
        }

        private int getRank(String key) {
            synchronized (this) {
                if (this.mRanks == null) {
                    buildRanksLocked();
                }
            }
            Integer rank = (Integer) this.mRanks.get(key);
            if (rank != null) {
                return rank.intValue();
            }
            return -1;
        }

        private boolean isIntercepted(String key) {
            synchronized (this) {
                if (this.mIntercepted == null) {
                    buildInterceptedSetLocked();
                }
            }
            return this.mIntercepted.contains(key);
        }

        private int getVisibilityOverride(String key) {
            synchronized (this) {
                if (this.mVisibilityOverrides == null) {
                    buildVisibilityOverridesLocked();
                }
            }
            Integer override = (Integer) this.mVisibilityOverrides.get(key);
            if (override == null) {
                return -1000;
            }
            return override.intValue();
        }

        private int getSuppressedVisualEffects(String key) {
            synchronized (this) {
                if (this.mSuppressedVisualEffects == null) {
                    buildSuppressedVisualEffectsLocked();
                }
            }
            Integer suppressed = (Integer) this.mSuppressedVisualEffects.get(key);
            if (suppressed == null) {
                return 0;
            }
            return suppressed.intValue();
        }

        private int getImportance(String key) {
            synchronized (this) {
                if (this.mImportance == null) {
                    buildImportanceLocked();
                }
            }
            Integer importance = (Integer) this.mImportance.get(key);
            if (importance == null) {
                return 3;
            }
            return importance.intValue();
        }

        private String getImportanceExplanation(String key) {
            synchronized (this) {
                if (this.mImportanceExplanation == null) {
                    buildImportanceExplanationLocked();
                }
            }
            return (String) this.mImportanceExplanation.get(key);
        }

        private String getOverrideGroupKey(String key) {
            synchronized (this) {
                if (this.mOverrideGroupKeys == null) {
                    buildOverrideGroupKeys();
                }
            }
            return (String) this.mOverrideGroupKeys.get(key);
        }

        private NotificationChannel getChannel(String key) {
            synchronized (this) {
                if (this.mChannels == null) {
                    buildChannelsLocked();
                }
            }
            return (NotificationChannel) this.mChannels.get(key);
        }

        private ArrayList<String> getOverridePeople(String key) {
            synchronized (this) {
                if (this.mOverridePeople == null) {
                    buildOverridePeopleLocked();
                }
            }
            return (ArrayList) this.mOverridePeople.get(key);
        }

        private ArrayList<SnoozeCriterion> getSnoozeCriteria(String key) {
            synchronized (this) {
                if (this.mSnoozeCriteria == null) {
                    buildSnoozeCriteriaLocked();
                }
            }
            return (ArrayList) this.mSnoozeCriteria.get(key);
        }

        private boolean getShowBadge(String key) {
            synchronized (this) {
                if (this.mShowBadge == null) {
                    buildShowBadgeLocked();
                }
            }
            Boolean showBadge = (Boolean) this.mShowBadge.get(key);
            if (showBadge == null) {
                return false;
            }
            return showBadge.booleanValue();
        }

        private void buildRanksLocked() {
            String[] orderedKeys = this.mRankingUpdate.getOrderedKeys();
            this.mRanks = new ArrayMap(orderedKeys.length);
            for (int i = 0; i < orderedKeys.length; i++) {
                this.mRanks.put(orderedKeys[i], Integer.valueOf(i));
            }
        }

        private void buildInterceptedSetLocked() {
            String[] dndInterceptedKeys = this.mRankingUpdate.getInterceptedKeys();
            this.mIntercepted = new ArraySet(dndInterceptedKeys.length);
            Collections.addAll(this.mIntercepted, dndInterceptedKeys);
        }

        private void buildVisibilityOverridesLocked() {
            Bundle visibilityBundle = this.mRankingUpdate.getVisibilityOverrides();
            this.mVisibilityOverrides = new ArrayMap(visibilityBundle.size());
            for (String key : visibilityBundle.keySet()) {
                this.mVisibilityOverrides.put(key, Integer.valueOf(visibilityBundle.getInt(key)));
            }
        }

        private void buildSuppressedVisualEffectsLocked() {
            Bundle suppressedBundle = this.mRankingUpdate.getSuppressedVisualEffects();
            this.mSuppressedVisualEffects = new ArrayMap(suppressedBundle.size());
            for (String key : suppressedBundle.keySet()) {
                this.mSuppressedVisualEffects.put(key, Integer.valueOf(suppressedBundle.getInt(key)));
            }
        }

        private void buildImportanceLocked() {
            String[] orderedKeys = this.mRankingUpdate.getOrderedKeys();
            int[] importance = this.mRankingUpdate.getImportance();
            this.mImportance = new ArrayMap(orderedKeys.length);
            for (int i = 0; i < orderedKeys.length; i++) {
                this.mImportance.put(orderedKeys[i], Integer.valueOf(importance[i]));
            }
        }

        private void buildImportanceExplanationLocked() {
            Bundle explanationBundle = this.mRankingUpdate.getImportanceExplanation();
            this.mImportanceExplanation = new ArrayMap(explanationBundle.size());
            for (String key : explanationBundle.keySet()) {
                this.mImportanceExplanation.put(key, explanationBundle.getString(key));
            }
        }

        private void buildOverrideGroupKeys() {
            Bundle overrideGroupKeys = this.mRankingUpdate.getOverrideGroupKeys();
            this.mOverrideGroupKeys = new ArrayMap(overrideGroupKeys.size());
            for (String key : overrideGroupKeys.keySet()) {
                this.mOverrideGroupKeys.put(key, overrideGroupKeys.getString(key));
            }
        }

        private void buildChannelsLocked() {
            Bundle channels = this.mRankingUpdate.getChannels();
            this.mChannels = new ArrayMap(channels.size());
            for (String key : channels.keySet()) {
                this.mChannels.put(key, (NotificationChannel) channels.getParcelable(key));
            }
        }

        private void buildOverridePeopleLocked() {
            Bundle overridePeople = this.mRankingUpdate.getOverridePeople();
            this.mOverridePeople = new ArrayMap(overridePeople.size());
            for (String key : overridePeople.keySet()) {
                this.mOverridePeople.put(key, overridePeople.getStringArrayList(key));
            }
        }

        private void buildSnoozeCriteriaLocked() {
            Bundle snoozeCriteria = this.mRankingUpdate.getSnoozeCriteria();
            this.mSnoozeCriteria = new ArrayMap(snoozeCriteria.size());
            for (String key : snoozeCriteria.keySet()) {
                this.mSnoozeCriteria.put(key, snoozeCriteria.getParcelableArrayList(key));
            }
        }

        private void buildShowBadgeLocked() {
            Bundle showBadge = this.mRankingUpdate.getShowBadge();
            this.mShowBadge = new ArrayMap(showBadge.size());
            for (String key : showBadge.keySet()) {
                this.mShowBadge.put(key, Boolean.valueOf(showBadge.getBoolean(key)));
            }
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.mRankingUpdate, flags);
        }
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        this.mHandler = new MyHandler(getMainLooper());
    }

    public void onNotificationPosted(StatusBarNotification sbn) {
    }

    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        onNotificationPosted(sbn);
    }

    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap) {
        onNotificationRemoved(sbn);
    }

    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap, int reason) {
        onNotificationRemoved(sbn, rankingMap);
    }

    public void onListenerConnected() {
    }

    public void onListenerDisconnected() {
    }

    public void onNotificationRankingUpdate(RankingMap rankingMap) {
    }

    public void onListenerHintsChanged(int hints) {
    }

    public void onNotificationChannelModified(String pkg, UserHandle user, NotificationChannel channel, int modificationType) {
    }

    public void onNotificationChannelGroupModified(String pkg, UserHandle user, NotificationChannelGroup group, int modificationType) {
    }

    public void onInterruptionFilterChanged(int interruptionFilter) {
    }

    protected final INotificationManager getNotificationInterface() {
        if (this.mNoMan == null) {
            this.mNoMan = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        }
        return this.mNoMan;
    }

    @Deprecated
    public final void cancelNotification(String pkg, String tag, int id) {
        if (isBound()) {
            try {
                getNotificationInterface().cancelNotificationFromListener(this.mWrapper, pkg, tag, id);
            } catch (RemoteException ex) {
                Log.v(this.TAG, "Unable to contact notification manager", ex);
            }
        }
    }

    public final void cancelNotification(String key) {
        if (isBound()) {
            try {
                getNotificationInterface().cancelNotificationsFromListener(this.mWrapper, new String[]{key});
            } catch (RemoteException ex) {
                Log.v(this.TAG, "Unable to contact notification manager", ex);
            }
        }
    }

    public final void cancelAllNotifications() {
        cancelNotifications(null);
    }

    public final void cancelNotifications(String[] keys) {
        if (isBound()) {
            try {
                getNotificationInterface().cancelNotificationsFromListener(this.mWrapper, keys);
            } catch (RemoteException ex) {
                Log.v(this.TAG, "Unable to contact notification manager", ex);
            }
        }
    }

    public final void snoozeNotification(String key, String snoozeCriterionId) {
        if (isBound()) {
            try {
                getNotificationInterface().snoozeNotificationUntilContextFromListener(this.mWrapper, key, snoozeCriterionId);
            } catch (RemoteException ex) {
                Log.v(this.TAG, "Unable to contact notification manager", ex);
            }
        }
    }

    public final void snoozeNotification(String key, long durationMs) {
        if (isBound()) {
            try {
                getNotificationInterface().snoozeNotificationUntilFromListener(this.mWrapper, key, durationMs);
            } catch (RemoteException ex) {
                Log.v(this.TAG, "Unable to contact notification manager", ex);
            }
        }
    }

    public final void setNotificationsShown(String[] keys) {
        if (isBound()) {
            try {
                getNotificationInterface().setNotificationsShownFromListener(this.mWrapper, keys);
            } catch (RemoteException ex) {
                Log.v(this.TAG, "Unable to contact notification manager", ex);
            }
        }
    }

    public final void updateNotificationChannel(String pkg, UserHandle user, NotificationChannel channel) {
        if (isBound()) {
            try {
                getNotificationInterface().updateNotificationChannelFromPrivilegedListener(this.mWrapper, pkg, user, channel);
            } catch (RemoteException e) {
                Log.v(this.TAG, "Unable to contact notification manager", e);
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public final List<NotificationChannel> getNotificationChannels(String pkg, UserHandle user) {
        if (!isBound()) {
            return null;
        }
        try {
            return getNotificationInterface().getNotificationChannelsFromPrivilegedListener(this.mWrapper, pkg, user).getList();
        } catch (RemoteException e) {
            Log.v(this.TAG, "Unable to contact notification manager", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public final List<NotificationChannelGroup> getNotificationChannelGroups(String pkg, UserHandle user) {
        if (!isBound()) {
            return null;
        }
        try {
            return getNotificationInterface().getNotificationChannelGroupsFromPrivilegedListener(this.mWrapper, pkg, user).getList();
        } catch (RemoteException e) {
            Log.v(this.TAG, "Unable to contact notification manager", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public final void setOnNotificationPostedTrim(int trim) {
        if (isBound()) {
            try {
                getNotificationInterface().setOnNotificationPostedTrimFromListener(this.mWrapper, trim);
            } catch (RemoteException ex) {
                Log.v(this.TAG, "Unable to contact notification manager", ex);
            }
        }
    }

    public StatusBarNotification[] getActiveNotifications() {
        return getActiveNotifications(null, 0);
    }

    public final StatusBarNotification[] getSnoozedNotifications() {
        try {
            return cleanUpNotificationList(getNotificationInterface().getSnoozedNotificationsFromListener(this.mWrapper, 0));
        } catch (RemoteException ex) {
            Log.v(this.TAG, "Unable to contact notification manager", ex);
            return null;
        }
    }

    public StatusBarNotification[] getActiveNotifications(int trim) {
        return getActiveNotifications(null, trim);
    }

    public StatusBarNotification[] getActiveNotifications(String[] keys) {
        return getActiveNotifications(keys, 0);
    }

    public StatusBarNotification[] getActiveNotifications(String[] keys, int trim) {
        if (!isBound()) {
            return null;
        }
        try {
            ParceledListSlice<StatusBarNotification> parceledList = getNotificationInterface().getActiveNotificationsFromListener(this.mWrapper, keys, trim);
            if (parceledList != null) {
                return cleanUpNotificationList(parceledList);
            }
        } catch (RemoteException ex) {
            Log.v(this.TAG, "Unable to contact notification manager", ex);
        }
        return null;
    }

    private StatusBarNotification[] cleanUpNotificationList(ParceledListSlice<StatusBarNotification> parceledList) {
        List<StatusBarNotification> list = parceledList.getList();
        ArrayList corruptNotifications = null;
        int N = list.size();
        for (int i = 0; i < N; i++) {
            StatusBarNotification sbn = (StatusBarNotification) list.get(i);
            Notification notification = sbn.getNotification();
            try {
                createLegacyIconExtras(notification);
                maybePopulateRemoteViews(notification);
            } catch (IllegalArgumentException e) {
                if (corruptNotifications == null) {
                    corruptNotifications = new ArrayList(N);
                }
                corruptNotifications.add(sbn);
                Log.w(this.TAG, "get(Active/Snoozed)Notifications: can't rebuild notification from " + sbn.getPackageName());
            }
        }
        if (corruptNotifications != null) {
            list.removeAll(corruptNotifications);
        }
        return (StatusBarNotification[]) list.toArray(new StatusBarNotification[list.size()]);
    }

    public final int getCurrentListenerHints() {
        if (!isBound()) {
            return 0;
        }
        try {
            return getNotificationInterface().getHintsFromListener(this.mWrapper);
        } catch (RemoteException ex) {
            Log.v(this.TAG, "Unable to contact notification manager", ex);
            return 0;
        }
    }

    public final int getCurrentInterruptionFilter() {
        if (!isBound()) {
            return 0;
        }
        try {
            return getNotificationInterface().getInterruptionFilterFromListener(this.mWrapper);
        } catch (RemoteException ex) {
            Log.v(this.TAG, "Unable to contact notification manager", ex);
            return 0;
        }
    }

    public final void requestListenerHints(int hints) {
        if (isBound()) {
            try {
                getNotificationInterface().requestHintsFromListener(this.mWrapper, hints);
            } catch (RemoteException ex) {
                Log.v(this.TAG, "Unable to contact notification manager", ex);
            }
        }
    }

    public final void requestInterruptionFilter(int interruptionFilter) {
        if (isBound()) {
            try {
                getNotificationInterface().requestInterruptionFilterFromListener(this.mWrapper, interruptionFilter);
            } catch (RemoteException ex) {
                Log.v(this.TAG, "Unable to contact notification manager", ex);
            }
        }
    }

    public RankingMap getCurrentRanking() {
        RankingMap rankingMap;
        synchronized (this.mLock) {
            rankingMap = this.mRankingMap;
        }
        return rankingMap;
    }

    public IBinder onBind(Intent intent) {
        if (this.mWrapper == null) {
            this.mWrapper = new NotificationListenerWrapper();
        }
        return this.mWrapper;
    }

    protected boolean isBound() {
        if (this.mWrapper != null) {
            return true;
        }
        Log.w(this.TAG, "Notification listener service not yet bound.");
        return false;
    }

    public void onDestroy() {
        onListenerDisconnected();
        super.onDestroy();
    }

    public void registerAsSystemService(Context context, ComponentName componentName, int currentUser) throws RemoteException {
        if (this.mWrapper == null) {
            this.mWrapper = new NotificationListenerWrapper();
        }
        this.mSystemContext = context;
        INotificationManager noMan = getNotificationInterface();
        this.mHandler = new MyHandler(context.getMainLooper());
        this.mCurrentUser = currentUser;
        noMan.registerListener(this.mWrapper, componentName, currentUser);
    }

    public void unregisterAsSystemService() throws RemoteException {
        if (this.mWrapper != null) {
            getNotificationInterface().unregisterListener(this.mWrapper, this.mCurrentUser);
        }
    }

    public static void requestRebind(ComponentName componentName) {
        try {
            INotificationManager.Stub.asInterface(ServiceManager.getService("notification")).requestBindListener(componentName);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public final void requestUnbind() {
        if (this.mWrapper != null) {
            try {
                getNotificationInterface().requestUnbindListener(this.mWrapper);
                this.isConnected = false;
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
    }

    private void createLegacyIconExtras(Notification n) {
        Icon smallIcon = n.getSmallIcon();
        Icon largeIcon = n.getLargeIcon();
        if (smallIcon != null && smallIcon.getType() == 2) {
            n.extras.putInt("android.icon", smallIcon.getResId());
            n.icon = smallIcon.getResId();
        }
        if (largeIcon != null) {
            Drawable d = largeIcon.loadDrawable(getContext());
            if (d != null && (d instanceof BitmapDrawable)) {
                Bitmap largeIconBits = ((BitmapDrawable) d).getBitmap();
                n.extras.putParcelable("android.largeIcon", largeIconBits);
                n.largeIcon = largeIconBits;
            }
        }
    }

    private void maybePopulateRemoteViews(Notification notification) {
        if (!Surface.APP_LAUNCHER.equals(getContext().getPackageName()) && getContext().getApplicationInfo().targetSdkVersion < 24) {
            Builder builder = Builder.recoverBuilder(getContext(), notification);
            RemoteViews content = builder.createContentView();
            RemoteViews big = builder.createBigContentView();
            RemoteViews headsUp = builder.createHeadsUpContentView();
            notification.contentView = content;
            notification.bigContentView = big;
            notification.headsUpContentView = headsUp;
        }
    }

    public final void applyUpdateLocked(NotificationRankingUpdate update) {
        this.mRankingMap = new RankingMap(update, null);
    }

    protected Context getContext() {
        if (this.mSystemContext != null) {
            return this.mSystemContext;
        }
        return this;
    }
}
