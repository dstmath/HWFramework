package android.service.notification;

import android.app.INotificationManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.security.keystore.KeyProperties;
import android.service.notification.INotificationListener.Stub;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
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
    public static final String SERVICE_INTERFACE = "android.service.notification.NotificationListenerService";
    public static final int SUPPRESSED_EFFECT_SCREEN_OFF = 1;
    public static final int SUPPRESSED_EFFECT_SCREEN_ON = 2;
    public static final int TRIM_FULL = 0;
    public static final int TRIM_LIGHT = 1;
    private final String TAG;
    private boolean isConnected;
    protected int mCurrentUser;
    private Handler mHandler;
    private final Object mLock;
    private INotificationManager mNoMan;
    @GuardedBy("mLock")
    private RankingMap mRankingMap;
    protected Context mSystemContext;
    protected NotificationListenerWrapper mWrapper;

    private final class MyHandler extends Handler {
        public static final int MSG_ON_INTERRUPTION_FILTER_CHANGED = 6;
        public static final int MSG_ON_LISTENER_CONNECTED = 3;
        public static final int MSG_ON_LISTENER_HINTS_CHANGED = 5;
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
                    case MSG_ON_NOTIFICATION_POSTED /*1*/:
                        args = msg.obj;
                        sbn = args.arg1;
                        rankingMap = args.arg2;
                        args.recycle();
                        NotificationListenerService.this.onNotificationPosted(sbn, rankingMap);
                        break;
                    case MSG_ON_NOTIFICATION_REMOVED /*2*/:
                        args = (SomeArgs) msg.obj;
                        sbn = (StatusBarNotification) args.arg1;
                        rankingMap = (RankingMap) args.arg2;
                        args.recycle();
                        NotificationListenerService.this.onNotificationRemoved(sbn, rankingMap);
                        break;
                    case MSG_ON_LISTENER_CONNECTED /*3*/:
                        NotificationListenerService.this.onListenerConnected();
                        break;
                    case MSG_ON_NOTIFICATION_RANKING_UPDATE /*4*/:
                        NotificationListenerService.this.onNotificationRankingUpdate((RankingMap) msg.obj);
                        break;
                    case MSG_ON_LISTENER_HINTS_CHANGED /*5*/:
                        NotificationListenerService.this.onListenerHintsChanged(msg.arg1);
                        break;
                    case MSG_ON_INTERRUPTION_FILTER_CHANGED /*6*/:
                        NotificationListenerService.this.onInterruptionFilterChanged(msg.arg1);
                        break;
                }
            }
        }
    }

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
                        NotificationListenerService.this.mHandler.obtainMessage(NotificationListenerService.TRIM_LIGHT, args).sendToTarget();
                    } else {
                        NotificationListenerService.this.mHandler.obtainMessage(NotificationListenerService.INTERRUPTION_FILTER_ALARMS, NotificationListenerService.this.mRankingMap).sendToTarget();
                    }
                }
            } catch (RemoteException e2) {
                Log.w(NotificationListenerService.this.TAG, "onNotificationPosted: Error receiving StatusBarNotification", e2);
            }
        }

        public void onNotificationRemoved(IStatusBarNotificationHolder sbnHolder, NotificationRankingUpdate update) {
            try {
                StatusBarNotification sbn = sbnHolder.get();
                synchronized (NotificationListenerService.this.mLock) {
                    NotificationListenerService.this.applyUpdateLocked(update);
                    SomeArgs args = SomeArgs.obtain();
                    args.arg1 = sbn;
                    args.arg2 = NotificationListenerService.this.mRankingMap;
                    NotificationListenerService.this.mHandler.obtainMessage(NotificationListenerService.SUPPRESSED_EFFECT_SCREEN_ON, args).sendToTarget();
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
            NotificationListenerService.this.mHandler.obtainMessage(NotificationListenerService.INTERRUPTION_FILTER_NONE).sendToTarget();
        }

        public void onNotificationRankingUpdate(NotificationRankingUpdate update) throws RemoteException {
            synchronized (NotificationListenerService.this.mLock) {
                NotificationListenerService.this.applyUpdateLocked(update);
                NotificationListenerService.this.mHandler.obtainMessage(NotificationListenerService.INTERRUPTION_FILTER_ALARMS, NotificationListenerService.this.mRankingMap).sendToTarget();
            }
        }

        public void onListenerHintsChanged(int hints) throws RemoteException {
            NotificationListenerService.this.mHandler.obtainMessage(5, hints, NotificationListenerService.TRIM_FULL).sendToTarget();
        }

        public void onInterruptionFilterChanged(int interruptionFilter) throws RemoteException {
            NotificationListenerService.this.mHandler.obtainMessage(6, interruptionFilter, NotificationListenerService.TRIM_FULL).sendToTarget();
        }

        public void onNotificationEnqueued(IStatusBarNotificationHolder notificationHolder, int importance, boolean user) throws RemoteException {
        }

        public void onNotificationVisibilityChanged(String key, long time, boolean visible) throws RemoteException {
        }

        public void onNotificationClick(String key, long time) throws RemoteException {
        }

        public void onNotificationActionClick(String key, long time, int actionIndex) throws RemoteException {
        }

        public void onNotificationRemovedReason(String key, long time, int reason) throws RemoteException {
        }
    }

    public static class Ranking {
        public static final int IMPORTANCE_DEFAULT = 3;
        public static final int IMPORTANCE_HIGH = 4;
        public static final int IMPORTANCE_LOW = 2;
        public static final int IMPORTANCE_MAX = 5;
        public static final int IMPORTANCE_MIN = 1;
        public static final int IMPORTANCE_NONE = 0;
        public static final int IMPORTANCE_UNSPECIFIED = -1000;
        public static final int VISIBILITY_NO_OVERRIDE = -1000;
        private int mImportance;
        private CharSequence mImportanceExplanation;
        private boolean mIsAmbient;
        private String mKey;
        private boolean mMatchesInterruptionFilter;
        private String mOverrideGroupKey;
        private int mRank;
        private int mSuppressedVisualEffects;
        private int mVisibilityOverride;

        public Ranking() {
            this.mRank = -1;
        }

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

        private void populate(String key, int rank, boolean matchesInterruptionFilter, int visibilityOverride, int suppressedVisualEffects, int importance, CharSequence explanation, String overrideGroupKey) {
            this.mKey = key;
            this.mRank = rank;
            this.mIsAmbient = importance < IMPORTANCE_LOW;
            this.mMatchesInterruptionFilter = matchesInterruptionFilter;
            this.mVisibilityOverride = visibilityOverride;
            this.mSuppressedVisualEffects = suppressedVisualEffects;
            this.mImportance = importance;
            this.mImportanceExplanation = explanation;
            this.mOverrideGroupKey = overrideGroupKey;
        }

        public static String importanceToString(int importance) {
            switch (importance) {
                case VISIBILITY_NO_OVERRIDE /*-1000*/:
                    return "UNSPECIFIED";
                case IMPORTANCE_NONE /*0*/:
                    return KeyProperties.DIGEST_NONE;
                case IMPORTANCE_MIN /*1*/:
                    return "MIN";
                case IMPORTANCE_LOW /*2*/:
                    return "LOW";
                case IMPORTANCE_DEFAULT /*3*/:
                    return "DEFAULT";
                case IMPORTANCE_HIGH /*4*/:
                    return "HIGH";
                case IMPORTANCE_MAX /*5*/:
                    return "MAX";
                default:
                    return "UNKNOWN(" + String.valueOf(importance) + ")";
            }
        }
    }

    public static class RankingMap implements Parcelable {
        public static final Creator<RankingMap> CREATOR = null;
        private ArrayMap<String, Integer> mImportance;
        private ArrayMap<String, String> mImportanceExplanation;
        private ArraySet<Object> mIntercepted;
        private ArrayMap<String, String> mOverrideGroupKeys;
        private final NotificationRankingUpdate mRankingUpdate;
        private ArrayMap<String, Integer> mRanks;
        private ArrayMap<String, Integer> mSuppressedVisualEffects;
        private ArrayMap<String, Integer> mVisibilityOverrides;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.service.notification.NotificationListenerService.RankingMap.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.service.notification.NotificationListenerService.RankingMap.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.service.notification.NotificationListenerService.RankingMap.<clinit>():void");
        }

        private RankingMap(NotificationRankingUpdate rankingUpdate) {
            this.mRankingUpdate = rankingUpdate;
        }

        public String[] getOrderedKeys() {
            return this.mRankingUpdate.getOrderedKeys();
        }

        public boolean getRanking(String key, Ranking outRanking) {
            boolean z;
            int rank = getRank(key);
            if (isIntercepted(key)) {
                z = false;
            } else {
                z = true;
            }
            outRanking.populate(key, rank, z, getVisibilityOverride(key), getSuppressedVisualEffects(key), getImportance(key), getImportanceExplanation(key), getOverrideGroupKey(key));
            if (rank >= 0) {
                return true;
            }
            return false;
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
                return Ranking.VISIBILITY_NO_OVERRIDE;
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
                return NotificationListenerService.TRIM_FULL;
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
                return NotificationListenerService.INTERRUPTION_FILTER_NONE;
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

        private void buildRanksLocked() {
            String[] orderedKeys = this.mRankingUpdate.getOrderedKeys();
            this.mRanks = new ArrayMap(orderedKeys.length);
            for (int i = NotificationListenerService.TRIM_FULL; i < orderedKeys.length; i += NotificationListenerService.TRIM_LIGHT) {
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
            for (int i = NotificationListenerService.TRIM_FULL; i < orderedKeys.length; i += NotificationListenerService.TRIM_LIGHT) {
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

        public int describeContents() {
            return NotificationListenerService.TRIM_FULL;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.mRankingUpdate, flags);
        }
    }

    public NotificationListenerService() {
        this.TAG = NotificationListenerService.class.getSimpleName() + "[" + getClass().getSimpleName() + "]";
        this.mLock = new Object();
        this.mWrapper = null;
        this.isConnected = false;
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

    public void onListenerConnected() {
    }

    public void onListenerDisconnected() {
    }

    public void onNotificationRankingUpdate(RankingMap rankingMap) {
    }

    public void onListenerHintsChanged(int hints) {
    }

    public void onInterruptionFilterChanged(int interruptionFilter) {
    }

    protected final INotificationManager getNotificationInterface() {
        if (this.mNoMan == null) {
            this.mNoMan = INotificationManager.Stub.asInterface(ServiceManager.getService(Context.NOTIFICATION_SERVICE));
        }
        return this.mNoMan;
    }

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
                INotificationManager notificationInterface = getNotificationInterface();
                INotificationListener iNotificationListener = this.mWrapper;
                String[] strArr = new String[TRIM_LIGHT];
                strArr[TRIM_FULL] = key;
                notificationInterface.cancelNotificationsFromListener(iNotificationListener, strArr);
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

    public final void setNotificationsShown(String[] keys) {
        if (isBound()) {
            try {
                getNotificationInterface().setNotificationsShownFromListener(this.mWrapper, keys);
            } catch (RemoteException ex) {
                Log.v(this.TAG, "Unable to contact notification manager", ex);
            }
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
        return getActiveNotifications(null, TRIM_FULL);
    }

    public StatusBarNotification[] getActiveNotifications(int trim) {
        return getActiveNotifications(null, trim);
    }

    public StatusBarNotification[] getActiveNotifications(String[] keys) {
        return getActiveNotifications(keys, TRIM_FULL);
    }

    public StatusBarNotification[] getActiveNotifications(String[] keys, int trim) {
        if (!isBound()) {
            return null;
        }
        try {
            List<StatusBarNotification> list = getNotificationInterface().getActiveNotificationsFromListener(this.mWrapper, keys, trim).getList();
            ArrayList corruptNotifications = null;
            int N = list.size();
            for (int i = TRIM_FULL; i < N; i += TRIM_LIGHT) {
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
                    Log.w(this.TAG, "onNotificationPosted: can't rebuild notification from " + sbn.getPackageName());
                }
            }
            if (corruptNotifications != null) {
                list.removeAll(corruptNotifications);
            }
            return (StatusBarNotification[]) list.toArray(new StatusBarNotification[list.size()]);
        } catch (RemoteException ex) {
            Log.v(this.TAG, "Unable to contact notification manager", ex);
            return null;
        }
    }

    public final int getCurrentListenerHints() {
        if (!isBound()) {
            return TRIM_FULL;
        }
        try {
            return getNotificationInterface().getHintsFromListener(this.mWrapper);
        } catch (RemoteException ex) {
            Log.v(this.TAG, "Unable to contact notification manager", ex);
            return TRIM_FULL;
        }
    }

    public final int getCurrentInterruptionFilter() {
        if (!isBound()) {
            return TRIM_FULL;
        }
        try {
            return getNotificationInterface().getInterruptionFilterFromListener(this.mWrapper);
        } catch (RemoteException ex) {
            Log.v(this.TAG, "Unable to contact notification manager", ex);
            return TRIM_FULL;
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
            INotificationManager.Stub.asInterface(ServiceManager.getService(Context.NOTIFICATION_SERVICE)).requestBindListener(componentName);
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
        if (smallIcon != null && smallIcon.getType() == SUPPRESSED_EFFECT_SCREEN_ON) {
            n.extras.putInt(Notification.EXTRA_SMALL_ICON, smallIcon.getResId());
            n.icon = smallIcon.getResId();
        }
        if (largeIcon != null) {
            Drawable d = largeIcon.loadDrawable(getContext());
            if (d != null && (d instanceof BitmapDrawable)) {
                Bitmap largeIconBits = ((BitmapDrawable) d).getBitmap();
                n.extras.putParcelable(Notification.EXTRA_LARGE_ICON, largeIconBits);
                n.largeIcon = largeIconBits;
            }
        }
    }

    private void maybePopulateRemoteViews(Notification notification) {
        if (getContext().getApplicationInfo().targetSdkVersion < 24) {
            Builder builder = Builder.recoverBuilder(getContext(), notification);
            RemoteViews content = builder.createContentView();
            RemoteViews big = builder.createBigContentView();
            RemoteViews headsUp = builder.createHeadsUpContentView();
            notification.contentView = content;
            notification.bigContentView = big;
            notification.headsUpContentView = headsUp;
        }
    }

    private void applyUpdateLocked(NotificationRankingUpdate update) {
        this.mRankingMap = new RankingMap(null);
    }

    protected Context getContext() {
        if (this.mSystemContext != null) {
            return this.mSystemContext;
        }
        return this;
    }
}
