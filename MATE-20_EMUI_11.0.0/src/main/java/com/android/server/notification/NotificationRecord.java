package com.android.server.notification;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.content.ContentProvider;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioSystem;
import android.metrics.LogMaker;
import android.net.Uri;
import android.net.util.NetworkConstants;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.notification.Adjustment;
import android.service.notification.NotificationListenerService;
import android.service.notification.NotificationStats;
import android.service.notification.SnoozeCriterion;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.widget.RemoteViews;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.notification.NotificationUsageStats;
import com.android.server.uri.UriGrantsManagerInternal;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class NotificationRecord {
    static final boolean DBG = Log.isLoggable(TAG, 3);
    private static final int MAX_SOUND_DELAY_MS = 2000;
    static final String TAG = "NotificationRecord";
    boolean isCanceled;
    public boolean isUpdate;
    private String mAdjustmentIssuer;
    private final List<Adjustment> mAdjustments;
    private boolean mAllowBubble;
    IActivityManager mAm;
    private int mAssistantImportance = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
    private AudioAttributes mAttributes;
    private int mAuthoritativeRank;
    private NotificationChannel mChannel;
    private float mContactAffinity;
    private final Context mContext;
    private long mCreationTimeMs;
    private int mCriticality = 2;
    private boolean mEditChoicesBeforeSending;
    private String mGlobalSortKey;
    private ArraySet<Uri> mGrantableUris;
    private boolean mHasSeenSmartReplies;
    private boolean mHidden;
    private int mImportance = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
    private int mImportanceExplanationCode = 0;
    private int mInitialImportanceExplanationCode = 0;
    private boolean mIntercept;
    private long mInterruptionTimeMs;
    private boolean mIsAppImportanceLocked;
    private boolean mIsInterruptive;
    private long mLastAudiblyAlertedMs;
    private long mLastIntrusive;
    private Light mLight;
    private int mNumberOfSmartActionsAdded;
    private int mNumberOfSmartRepliesAdded;
    final int mOriginalFlags;
    private int mPackagePriority;
    private int mPackageVisibility;
    private ArrayList<String> mPeopleOverride;
    private boolean mPreChannelsNotification = true;
    private long mPushLogPowerTimeMs;
    private long mRankingTimeMs;
    private boolean mRecentlyIntrusive;
    private boolean mRecordedInterruption;
    private boolean mShowBadge;
    private ArrayList<CharSequence> mSmartReplies;
    private ArrayList<SnoozeCriterion> mSnoozeCriteria;
    private Uri mSound;
    private final NotificationStats mStats;
    private boolean mSuggestionsGeneratedByAssistant;
    private int mSuppressedVisualEffects = 0;
    private ArrayList<Notification.Action> mSystemGeneratedSmartActions;
    private int mSystemImportance = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
    final int mTargetSdkVersion;
    private boolean mTextChanged;
    UriGrantsManagerInternal mUgmInternal;
    @VisibleForTesting
    final long mUpdateTimeMs;
    private String mUserExplanation;
    private int mUserSentiment;
    private long[] mVibration;
    private long mVisibleSinceMs;
    IBinder permissionOwner;
    public final StatusBarNotification sbn;
    NotificationUsageStats.SingleNotificationStats stats;

    public NotificationRecord(Context context, StatusBarNotification sbn2, NotificationChannel channel) {
        this.sbn = sbn2;
        this.mTargetSdkVersion = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getPackageTargetSdkVersion(sbn2.getPackageName());
        this.mAm = ActivityManager.getService();
        this.mUgmInternal = (UriGrantsManagerInternal) LocalServices.getService(UriGrantsManagerInternal.class);
        this.mOriginalFlags = sbn2.getNotification().flags;
        this.mRankingTimeMs = calculateRankingTimeMs(0);
        this.mCreationTimeMs = sbn2.getPostTime();
        long j = this.mCreationTimeMs;
        this.mUpdateTimeMs = j;
        this.mPushLogPowerTimeMs = j;
        this.mInterruptionTimeMs = j;
        this.mContext = context;
        this.stats = new NotificationUsageStats.SingleNotificationStats();
        this.mChannel = channel;
        this.mPreChannelsNotification = isPreChannelsNotification();
        this.mSound = calculateSound();
        this.mVibration = calculateVibration();
        this.mAttributes = calculateAttributes();
        this.mImportance = calculateInitialImportance();
        this.mLight = calculateLights();
        this.mAdjustments = new ArrayList();
        this.mStats = new NotificationStats();
        calculateUserSentiment();
        calculateGrantableUris();
    }

    private boolean isPreChannelsNotification() {
        if (!"miscellaneous".equals(getChannel().getId()) || this.mTargetSdkVersion >= 26) {
            return false;
        }
        return true;
    }

    private Uri calculateSound() {
        Notification n = this.sbn.getNotification();
        if (this.mContext.getPackageManager().hasSystemFeature("android.software.leanback")) {
            return null;
        }
        Uri sound = this.mChannel.getSound();
        if (!this.mPreChannelsNotification) {
            return sound;
        }
        boolean useDefaultSound = true;
        if ((n.defaults & 1) == 0) {
            useDefaultSound = false;
        }
        if (useDefaultSound) {
            return Settings.System.DEFAULT_NOTIFICATION_URI;
        }
        return n.sound;
    }

    private Light calculateLights() {
        int channelLightColor;
        Light light;
        int defaultLightColor = this.mContext.getResources().getColor(17170728);
        int defaultLightOn = this.mContext.getResources().getInteger(17694778);
        int defaultLightOff = this.mContext.getResources().getInteger(17694777);
        if (getChannel().getLightColor() != 0) {
            channelLightColor = getChannel().getLightColor();
        } else {
            channelLightColor = defaultLightColor;
        }
        if (getChannel().shouldShowLights()) {
            light = new Light(channelLightColor, defaultLightOn, defaultLightOff);
        } else {
            light = null;
        }
        if (!this.mPreChannelsNotification || (getChannel().getUserLockedFields() & 8) != 0) {
            return light;
        }
        Notification notification = this.sbn.getNotification();
        if ((notification.flags & 1) == 0) {
            return null;
        }
        Light light2 = new Light(notification.ledARGB, notification.ledOnMS, notification.ledOffMS);
        if ((notification.defaults & 4) != 0) {
            return new Light(defaultLightColor, defaultLightOn, defaultLightOff);
        }
        return light2;
    }

    private long[] calculateVibration() {
        long[] vibration;
        long[] defaultVibration = NotificationManagerService.getLongArray(this.mContext.getResources(), 17236004, 17, NotificationManagerService.DEFAULT_VIBRATE_PATTERN);
        if (getChannel().shouldVibrate()) {
            vibration = getChannel().getVibrationPattern() == null ? defaultVibration : getChannel().getVibrationPattern();
        } else {
            vibration = null;
        }
        if (!this.mPreChannelsNotification || (getChannel().getUserLockedFields() & 16) != 0) {
            return vibration;
        }
        Notification notification = this.sbn.getNotification();
        if ((notification.defaults & 2) != 0) {
            return defaultVibration;
        }
        return notification.vibrate;
    }

    private AudioAttributes calculateAttributes() {
        Notification n = this.sbn.getNotification();
        AudioAttributes attributes = getChannel().getAudioAttributes();
        if (attributes == null) {
            attributes = Notification.AUDIO_ATTRIBUTES_DEFAULT;
        }
        if (!this.mPreChannelsNotification || (getChannel().getUserLockedFields() & 32) != 0) {
            return attributes;
        }
        if (n.audioAttributes != null) {
            return n.audioAttributes;
        }
        if (n.audioStreamType >= 0 && n.audioStreamType < AudioSystem.getNumStreamTypes()) {
            return new AudioAttributes.Builder().setInternalLegacyStreamType(n.audioStreamType).build();
        }
        if (n.audioStreamType == -1) {
            return attributes;
        }
        Log.w(TAG, String.format("Invalid stream type: %d", Integer.valueOf(n.audioStreamType)));
        return attributes;
    }

    private int calculateInitialImportance() {
        int i;
        Notification n = this.sbn.getNotification();
        int importance = getChannel().getImportance();
        boolean z = true;
        if (getChannel().hasUserSetImportance()) {
            i = 2;
        } else {
            i = 1;
        }
        this.mInitialImportanceExplanationCode = i;
        if ((n.flags & 128) != 0) {
            n.priority = 2;
        }
        int requestedImportance = 3;
        n.priority = NotificationManagerService.clamp(n.priority, -2, 2);
        int i2 = n.priority;
        if (i2 == -2) {
            requestedImportance = 1;
        } else if (i2 == -1) {
            requestedImportance = 2;
        } else if (i2 == 0) {
            requestedImportance = 3;
        } else if (i2 == 1 || i2 == 2) {
            requestedImportance = 4;
        }
        NotificationUsageStats.SingleNotificationStats singleNotificationStats = this.stats;
        singleNotificationStats.requestedImportance = requestedImportance;
        if (this.mSound == null && this.mVibration == null) {
            z = false;
        }
        singleNotificationStats.isNoisy = z;
        if (this.mPreChannelsNotification && (importance == -1000 || !getChannel().hasUserSetImportance())) {
            if (!this.stats.isNoisy && requestedImportance > 2) {
                requestedImportance = 2;
            }
            if (this.stats.isNoisy && requestedImportance < 3) {
                requestedImportance = 3;
            }
            if (n.fullScreenIntent != null) {
                requestedImportance = 4;
            }
            importance = requestedImportance;
            this.mInitialImportanceExplanationCode = 5;
            Log.i(TAG, "Use default channel, The importance is:" + importance + ",key=" + this.sbn.getKey());
        }
        this.stats.naturalImportance = importance;
        return importance;
    }

    public void copyRankingInformation(NotificationRecord previous) {
        this.mContactAffinity = previous.mContactAffinity;
        this.mRecentlyIntrusive = previous.mRecentlyIntrusive;
        this.mPackagePriority = previous.mPackagePriority;
        this.mPackageVisibility = previous.mPackageVisibility;
        this.mIntercept = previous.mIntercept;
        this.mHidden = previous.mHidden;
        this.mRankingTimeMs = calculateRankingTimeMs(previous.getRankingTimeMs());
        this.mCreationTimeMs = previous.mCreationTimeMs;
        this.mVisibleSinceMs = previous.mVisibleSinceMs;
        this.mPushLogPowerTimeMs = previous.mPushLogPowerTimeMs;
        if (previous.sbn.getOverrideGroupKey() != null && !this.sbn.isAppGroup()) {
            this.sbn.setOverrideGroupKey(previous.sbn.getOverrideGroupKey());
        }
    }

    public Notification getNotification() {
        return this.sbn.getNotification();
    }

    public int getFlags() {
        return this.sbn.getNotification().flags;
    }

    public UserHandle getUser() {
        return this.sbn.getUser();
    }

    public String getKey() {
        return this.sbn.getKey();
    }

    public int getUserId() {
        return this.sbn.getUserId();
    }

    public int getUid() {
        return this.sbn.getUid();
    }

    /* access modifiers changed from: package-private */
    public void dump(ProtoOutputStream proto, long fieldId, boolean redact, int state) {
        long token = proto.start(fieldId);
        proto.write(1138166333441L, this.sbn.getKey());
        proto.write(1159641169922L, state);
        if (getChannel() != null) {
            proto.write(1138166333444L, getChannel().getId());
        }
        boolean z = true;
        proto.write(1133871366152L, getLight() != null);
        if (getVibration() == null) {
            z = false;
        }
        proto.write(1133871366151L, z);
        proto.write(1120986464259L, this.sbn.getNotification().flags);
        proto.write(1138166333449L, getGroupKey());
        proto.write(1172526071818L, getImportance());
        if (getSound() != null) {
            proto.write(1138166333445L, getSound().toString());
        }
        if (getAudioAttributes() != null) {
            getAudioAttributes().writeToProto(proto, 1146756268038L);
        }
        proto.write(1138166333451L, this.sbn.getPackageName());
        proto.write(1138166333452L, this.sbn.getOpPkg());
        proto.end(token);
    }

    /* access modifiers changed from: package-private */
    public String formatRemoteViews(RemoteViews rv) {
        if (rv == null) {
            return "null";
        }
        return String.format("%s/0x%08x (%d bytes): %s", rv.getPackage(), Integer.valueOf(rv.getLayoutId()), Integer.valueOf(rv.estimateMemoryUsage()), rv.toString());
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix, Context baseContext, boolean redact) {
        String str;
        Notification notification = this.sbn.getNotification();
        Icon icon = notification.getSmallIcon();
        String iconStr = String.valueOf(icon);
        int i = 2;
        if (icon != null && icon.getType() == 2) {
            iconStr = iconStr + " / " + idDebugString(baseContext, icon.getResPackage(), icon.getResId());
        }
        pw.println(prefix + this);
        String prefix2 = prefix + "  ";
        pw.println(prefix2 + "uid=" + this.sbn.getUid() + " userId=" + this.sbn.getUserId());
        StringBuilder sb = new StringBuilder();
        sb.append(prefix2);
        sb.append("opPkg=");
        sb.append(this.sbn.getOpPkg());
        pw.println(sb.toString());
        pw.println(prefix2 + "icon=" + iconStr);
        pw.println(prefix2 + "flags=0x" + Integer.toHexString(notification.flags));
        pw.println(prefix2 + "pri=" + notification.priority);
        pw.println(prefix2 + "key=" + this.sbn.getKey());
        pw.println(prefix2 + "seen=" + this.mStats.hasSeen());
        pw.println(prefix2 + "groupKey=" + getGroupKey());
        pw.println(prefix2 + "fullscreenIntent=" + notification.fullScreenIntent);
        pw.println(prefix2 + "contentIntent=" + notification.contentIntent);
        pw.println(prefix2 + "deleteIntent=" + notification.deleteIntent);
        pw.println(prefix2 + "number=" + notification.number);
        pw.println(prefix2 + "groupAlertBehavior=" + notification.getGroupAlertBehavior());
        StringBuilder sb2 = new StringBuilder();
        sb2.append(prefix2);
        sb2.append("tickerText=");
        pw.print(sb2.toString());
        char c = 0;
        if (!TextUtils.isEmpty(notification.tickerText)) {
            String ticker = notification.tickerText.toString();
            if (redact) {
                pw.print(ticker.length() > 16 ? ticker.substring(0, 8) : "");
                pw.println("...");
            } else {
                pw.println(ticker);
            }
        } else {
            pw.println("null");
        }
        pw.println(prefix2 + "contentView=" + formatRemoteViews(notification.contentView));
        pw.println(prefix2 + "bigContentView=" + formatRemoteViews(notification.bigContentView));
        pw.println(prefix2 + "headsUpContentView=" + formatRemoteViews(notification.headsUpContentView));
        StringBuilder sb3 = new StringBuilder();
        sb3.append(prefix2);
        char c2 = 1;
        sb3.append(String.format("color=0x%08x", Integer.valueOf(notification.color)));
        pw.print(sb3.toString());
        pw.println(prefix2 + "timeout=" + TimeUtils.formatForLogging(notification.getTimeoutAfter()));
        if (notification.actions != null && notification.actions.length > 0) {
            pw.println(prefix2 + "actions={");
            int N = notification.actions.length;
            int i2 = 0;
            while (i2 < N) {
                Notification.Action action = notification.actions[i2];
                if (action != null) {
                    Object[] objArr = new Object[4];
                    objArr[0] = prefix2;
                    objArr[c2] = Integer.valueOf(i2);
                    objArr[2] = action.title;
                    if (action.actionIntent == null) {
                        str = "null";
                    } else {
                        str = action.actionIntent.toString();
                    }
                    objArr[3] = str;
                    pw.println(String.format("%s    [%d] \"%s\" -> %s", objArr));
                }
                i2++;
                c2 = 1;
            }
            pw.println(prefix2 + "  }");
        }
        if (notification.extras != null && notification.extras.size() > 0) {
            pw.println(prefix2 + "extras={");
            for (String key : notification.extras.keySet()) {
                pw.print(prefix2 + "    " + key + "=");
                Object val = notification.extras.get(key);
                if (val == null) {
                    pw.println("null");
                } else {
                    pw.print(val.getClass().getSimpleName());
                    if (!redact || (!(val instanceof CharSequence) && !(val instanceof String))) {
                        if (val instanceof Bitmap) {
                            Object[] objArr2 = new Object[i];
                            objArr2[c] = Integer.valueOf(((Bitmap) val).getWidth());
                            objArr2[1] = Integer.valueOf(((Bitmap) val).getHeight());
                            pw.print(String.format(" (%dx%d)", objArr2));
                        } else if (val.getClass().isArray()) {
                            int N2 = Array.getLength(val);
                            pw.print(" (" + N2 + ")");
                            if (!redact) {
                                for (int j = 0; j < N2; j++) {
                                    pw.println();
                                    pw.print(String.format("%s      [%d] %s", prefix2, Integer.valueOf(j), String.valueOf(Array.get(val, j))));
                                }
                            }
                        } else {
                            pw.print(" (" + String.valueOf(val) + ")");
                        }
                    }
                    pw.println();
                }
                i = 2;
                c = 0;
            }
            pw.println(prefix2 + "}");
        }
        pw.println(prefix2 + "stats=" + this.stats.toString());
        pw.println(prefix2 + "mContactAffinity=" + this.mContactAffinity);
        pw.println(prefix2 + "mRecentlyIntrusive=" + this.mRecentlyIntrusive);
        pw.println(prefix2 + "mPackagePriority=" + this.mPackagePriority);
        pw.println(prefix2 + "mPackageVisibility=" + this.mPackageVisibility);
        pw.println(prefix2 + "mSystemImportance=" + NotificationListenerService.Ranking.importanceToString(this.mSystemImportance));
        pw.println(prefix2 + "mAsstImportance=" + NotificationListenerService.Ranking.importanceToString(this.mAssistantImportance));
        pw.println(prefix2 + "mImportance=" + NotificationListenerService.Ranking.importanceToString(this.mImportance));
        pw.println(prefix2 + "mImportanceExplanation=" + ((Object) getImportanceExplanation()));
        pw.println(prefix2 + "mIsAppImportanceLocked=" + this.mIsAppImportanceLocked);
        pw.println(prefix2 + "mIntercept=" + this.mIntercept);
        pw.println(prefix2 + "mHidden==" + this.mHidden);
        pw.println(prefix2 + "mGlobalSortKey=" + this.mGlobalSortKey);
        pw.println(prefix2 + "mRankingTimeMs=" + this.mRankingTimeMs);
        pw.println(prefix2 + "mCreationTimeMs=" + this.mCreationTimeMs);
        pw.println(prefix2 + "mVisibleSinceMs=" + this.mVisibleSinceMs);
        pw.println(prefix2 + "mUpdateTimeMs=" + this.mUpdateTimeMs);
        pw.println(prefix2 + "mInterruptionTimeMs=" + this.mInterruptionTimeMs);
        pw.println(prefix2 + "mSuppressedVisualEffects= " + this.mSuppressedVisualEffects);
        if (this.mPreChannelsNotification) {
            pw.println(prefix2 + String.format("defaults=0x%08x flags=0x%08x", Integer.valueOf(notification.defaults), Integer.valueOf(notification.flags)));
            pw.println(prefix2 + "n.sound=" + notification.sound);
            pw.println(prefix2 + "n.audioStreamType=" + notification.audioStreamType);
            pw.println(prefix2 + "n.audioAttributes=" + notification.audioAttributes);
            StringBuilder sb4 = new StringBuilder();
            sb4.append(prefix2);
            sb4.append(String.format("  led=0x%08x onMs=%d offMs=%d", Integer.valueOf(notification.ledARGB), Integer.valueOf(notification.ledOnMS), Integer.valueOf(notification.ledOffMS)));
            pw.println(sb4.toString());
            pw.println(prefix2 + "vibrate=" + Arrays.toString(notification.vibrate));
        }
        pw.println(prefix2 + "mSound= " + this.mSound);
        pw.println(prefix2 + "mVibration= " + this.mVibration);
        pw.println(prefix2 + "mAttributes= " + this.mAttributes);
        pw.println(prefix2 + "mLight= " + this.mLight);
        pw.println(prefix2 + "mShowBadge=" + this.mShowBadge);
        pw.println(prefix2 + "mColorized=" + notification.isColorized());
        pw.println(prefix2 + "mIsInterruptive=" + this.mIsInterruptive);
        pw.println(prefix2 + "effectiveNotificationChannel=" + getChannel());
        if (getPeopleOverride() != null) {
            pw.println(prefix2 + "overridePeople= " + TextUtils.join(",", getPeopleOverride()));
        }
        if (getSnoozeCriteria() != null) {
            pw.println(prefix2 + "snoozeCriteria=" + TextUtils.join(",", getSnoozeCriteria()));
        }
        pw.println(prefix2 + "mAdjustments=" + this.mAdjustments);
    }

    static String idDebugString(Context baseContext, String packageName, int id) {
        Context c;
        if (packageName != null) {
            try {
                c = baseContext.createPackageContext(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                c = baseContext;
            }
        } else {
            c = baseContext;
        }
        try {
            return c.getResources().getResourceName(id);
        } catch (Resources.NotFoundException e2) {
            return "<name unknown>";
        }
    }

    public final String toString() {
        return String.format("NotificationRecord(0x%08x: pkg=%s user=%s id=%d tag=%s importance=%d key=%sappImportanceLocked=%s: %s)", Integer.valueOf(System.identityHashCode(this)), this.sbn.getPackageName(), this.sbn.getUser(), Integer.valueOf(this.sbn.getId()), this.sbn.getTag(), Integer.valueOf(this.mImportance), this.sbn.getKey(), Boolean.valueOf(this.mIsAppImportanceLocked), this.sbn.getNotification());
    }

    public boolean hasAdjustment(String key) {
        synchronized (this.mAdjustments) {
            for (Adjustment adjustment : this.mAdjustments) {
                if (adjustment.getSignals().containsKey(key)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void addAdjustment(Adjustment adjustment) {
        synchronized (this.mAdjustments) {
            this.mAdjustments.add(adjustment);
        }
    }

    public void applyAdjustments() {
        System.currentTimeMillis();
        synchronized (this.mAdjustments) {
            for (Adjustment adjustment : this.mAdjustments) {
                Bundle signals = adjustment.getSignals();
                if (signals.containsKey("key_people")) {
                    setPeopleOverride(adjustment.getSignals().getStringArrayList("key_people"));
                }
                if (signals.containsKey("key_snooze_criteria")) {
                    setSnoozeCriteria(adjustment.getSignals().getParcelableArrayList("key_snooze_criteria"));
                }
                if (signals.containsKey("key_group_key")) {
                    setOverrideGroupKey(adjustment.getSignals().getString("key_group_key"));
                }
                if (signals.containsKey("key_user_sentiment") && !this.mIsAppImportanceLocked && (getChannel().getUserLockedFields() & 4) == 0) {
                    setUserSentiment(adjustment.getSignals().getInt("key_user_sentiment", 0));
                }
                if (signals.containsKey("key_contextual_actions")) {
                    setSystemGeneratedSmartActions(signals.getParcelableArrayList("key_contextual_actions"));
                }
                if (signals.containsKey("key_text_replies")) {
                    setSmartReplies(signals.getCharSequenceArrayList("key_text_replies"));
                }
                if (signals.containsKey("key_importance")) {
                    setAssistantImportance(Math.min(4, Math.max((int) JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE, signals.getInt("key_importance"))));
                }
                if (!signals.isEmpty() && adjustment.getIssuer() != null) {
                    this.mAdjustmentIssuer = adjustment.getIssuer();
                }
            }
            this.mAdjustments.clear();
        }
    }

    public void setIsAppImportanceLocked(boolean isAppImportanceLocked) {
        this.mIsAppImportanceLocked = isAppImportanceLocked;
        calculateUserSentiment();
    }

    public void setContactAffinity(float contactAffinity) {
        this.mContactAffinity = contactAffinity;
    }

    public float getContactAffinity() {
        return this.mContactAffinity;
    }

    public void setRecentlyIntrusive(boolean recentlyIntrusive) {
        this.mRecentlyIntrusive = recentlyIntrusive;
        if (recentlyIntrusive) {
            this.mLastIntrusive = System.currentTimeMillis();
        }
    }

    public boolean isRecentlyIntrusive() {
        return this.mRecentlyIntrusive;
    }

    public long getLastIntrusive() {
        return this.mLastIntrusive;
    }

    public void setPackagePriority(int packagePriority) {
        this.mPackagePriority = packagePriority;
    }

    public int getPackagePriority() {
        return this.mPackagePriority;
    }

    public void setPackageVisibilityOverride(int packageVisibility) {
        this.mPackageVisibility = packageVisibility;
    }

    public int getPackageVisibilityOverride() {
        return this.mPackageVisibility;
    }

    private String getUserExplanation() {
        if (this.mUserExplanation == null) {
            this.mUserExplanation = this.mContext.getResources().getString(17040294);
        }
        return this.mUserExplanation;
    }

    public void setSystemImportance(int importance) {
        this.mSystemImportance = importance;
        calculateImportance();
    }

    public void setAssistantImportance(int importance) {
        this.mAssistantImportance = importance;
    }

    public int getAssistantImportance() {
        return this.mAssistantImportance;
    }

    /* access modifiers changed from: protected */
    public void calculateImportance() {
        this.mImportance = calculateInitialImportance();
        this.mImportanceExplanationCode = this.mInitialImportanceExplanationCode;
        if (!getChannel().hasUserSetImportance() && this.mAssistantImportance != -1000 && !getChannel().isImportanceLockedByOEM() && !getChannel().isImportanceLockedByCriticalDeviceFunction()) {
            this.mImportance = this.mAssistantImportance;
            this.mImportanceExplanationCode = 3;
        }
        int i = this.mSystemImportance;
        if (i != -1000) {
            this.mImportance = i;
            this.mImportanceExplanationCode = 4;
        }
    }

    public int getImportance() {
        return this.mImportance;
    }

    public CharSequence getImportanceExplanation() {
        int i = this.mImportanceExplanationCode;
        if (i == 0) {
            return null;
        }
        if (i == 1) {
            return "app";
        }
        if (i == 2) {
            return "user";
        }
        if (i == 3) {
            return "asst";
        }
        if (i == 4) {
            return "system";
        }
        if (i != 5) {
            return null;
        }
        return "app";
    }

    public boolean setIntercepted(boolean intercept) {
        this.mIntercept = intercept;
        return this.mIntercept;
    }

    public void setCriticality(int criticality) {
        this.mCriticality = criticality;
    }

    public int getCriticality() {
        return this.mCriticality;
    }

    public boolean isIntercepted() {
        return this.mIntercept;
    }

    public boolean isNewEnoughForAlerting(long now) {
        return getFreshnessMs(now) <= 2000;
    }

    public void setHidden(boolean hidden) {
        this.mHidden = hidden;
    }

    public boolean isHidden() {
        return this.mHidden;
    }

    public void setSuppressedVisualEffects(int effects) {
        this.mSuppressedVisualEffects = effects;
    }

    public int getSuppressedVisualEffects() {
        return this.mSuppressedVisualEffects;
    }

    public boolean isCategory(String category) {
        return Objects.equals(getNotification().category, category);
    }

    public boolean isAudioAttributesUsage(int usage) {
        AudioAttributes audioAttributes = this.mAttributes;
        return audioAttributes != null && audioAttributes.getUsage() == usage;
    }

    public long getRankingTimeMs() {
        return this.mRankingTimeMs;
    }

    public int getFreshnessMs(long now) {
        return (int) (now - this.mUpdateTimeMs);
    }

    public int getLifespanMs(long now) {
        return (int) (now - this.mCreationTimeMs);
    }

    public int getExposureMs(long now) {
        long j = this.mVisibleSinceMs;
        if (j == 0) {
            return 0;
        }
        return (int) (now - j);
    }

    public int getInterruptionMs(long now) {
        return (int) (now - this.mInterruptionTimeMs);
    }

    /* access modifiers changed from: protected */
    public long getPushLogPowerTimeMs(long now) {
        return now - this.mPushLogPowerTimeMs;
    }

    /* access modifiers changed from: protected */
    public void setPushLogPowerTimeMs(long timeMs) {
        this.mPushLogPowerTimeMs = timeMs;
    }

    public void setVisibility(boolean visible, int rank, int count) {
        long now = System.currentTimeMillis();
        this.mVisibleSinceMs = visible ? now : this.mVisibleSinceMs;
        this.stats.onVisibilityChanged(visible);
        MetricsLogger.action(getLogMaker(now).setCategory(128).setType(visible ? 1 : 2).addTaggedData(798, Integer.valueOf(rank)).addTaggedData(1395, Integer.valueOf(count)));
        if (visible) {
            setSeen();
            MetricsLogger.histogram(this.mContext, "note_freshness", getFreshnessMs(now));
        }
        EventLogTags.writeNotificationVisibility(getKey(), visible ? 1 : 0, getLifespanMs(now), getFreshnessMs(now), 0, rank);
    }

    private long calculateRankingTimeMs(long previousRankingTimeMs) {
        Notification n = getNotification();
        if (n.when != 0 && n.when <= this.sbn.getPostTime()) {
            return n.when;
        }
        if (previousRankingTimeMs > 0) {
            return previousRankingTimeMs;
        }
        return this.sbn.getPostTime();
    }

    public void setGlobalSortKey(String globalSortKey) {
        this.mGlobalSortKey = globalSortKey;
    }

    public String getGlobalSortKey() {
        return this.mGlobalSortKey;
    }

    public boolean isSeen() {
        return this.mStats.hasSeen();
    }

    public void setSeen() {
        this.mStats.setSeen();
        if (this.mTextChanged) {
            setInterruptive(true);
        }
    }

    public void setAuthoritativeRank(int authoritativeRank) {
        this.mAuthoritativeRank = authoritativeRank;
    }

    public int getAuthoritativeRank() {
        return this.mAuthoritativeRank;
    }

    public String getGroupKey() {
        return this.sbn.getGroupKey();
    }

    public void setOverrideGroupKey(String overrideGroupKey) {
        this.sbn.setOverrideGroupKey(overrideGroupKey);
    }

    public NotificationChannel getChannel() {
        return this.mChannel;
    }

    public boolean getIsAppImportanceLocked() {
        return this.mIsAppImportanceLocked;
    }

    /* access modifiers changed from: protected */
    public void updateNotificationChannel(NotificationChannel channel) {
        if (channel != null) {
            this.mChannel = channel;
            calculateImportance();
            calculateUserSentiment();
        }
    }

    public void setShowBadge(boolean showBadge) {
        this.mShowBadge = showBadge;
    }

    public boolean canBubble() {
        return this.mAllowBubble;
    }

    public void setAllowBubble(boolean allow) {
        this.mAllowBubble = allow;
    }

    public boolean canShowBadge() {
        return this.mShowBadge;
    }

    public Light getLight() {
        return this.mLight;
    }

    public Uri getSound() {
        return this.mSound;
    }

    public long[] getVibration() {
        return this.mVibration;
    }

    public AudioAttributes getAudioAttributes() {
        return this.mAttributes;
    }

    public ArrayList<String> getPeopleOverride() {
        return this.mPeopleOverride;
    }

    public void setInterruptive(boolean interruptive) {
        this.mIsInterruptive = interruptive;
        long now = System.currentTimeMillis();
        this.mInterruptionTimeMs = interruptive ? now : this.mInterruptionTimeMs;
        if (interruptive) {
            MetricsLogger.action(getLogMaker().setCategory(1501).setType(1).addTaggedData((int) NetworkConstants.ETHER_MTU, Integer.valueOf(getInterruptionMs(now))));
            MetricsLogger.histogram(this.mContext, "note_interruptive", getInterruptionMs(now));
        }
    }

    public void setAudiblyAlerted(boolean audiblyAlerted) {
        this.mLastAudiblyAlertedMs = audiblyAlerted ? System.currentTimeMillis() : -1;
    }

    public void setTextChanged(boolean textChanged) {
        this.mTextChanged = textChanged;
    }

    public void setRecordedInterruption(boolean recorded) {
        this.mRecordedInterruption = recorded;
    }

    public boolean hasRecordedInterruption() {
        return this.mRecordedInterruption;
    }

    public boolean isInterruptive() {
        return this.mIsInterruptive;
    }

    public long getLastAudiblyAlertedMs() {
        return this.mLastAudiblyAlertedMs;
    }

    /* access modifiers changed from: protected */
    public void setPeopleOverride(ArrayList<String> people) {
        this.mPeopleOverride = people;
    }

    public ArrayList<SnoozeCriterion> getSnoozeCriteria() {
        return this.mSnoozeCriteria;
    }

    /* access modifiers changed from: protected */
    public void setSnoozeCriteria(ArrayList<SnoozeCriterion> snoozeCriteria) {
        this.mSnoozeCriteria = snoozeCriteria;
    }

    private void calculateUserSentiment() {
        if ((getChannel().getUserLockedFields() & 4) != 0 || this.mIsAppImportanceLocked) {
            this.mUserSentiment = 1;
        }
    }

    private void setUserSentiment(int userSentiment) {
        this.mUserSentiment = userSentiment;
    }

    public int getUserSentiment() {
        return this.mUserSentiment;
    }

    public NotificationStats getStats() {
        return this.mStats;
    }

    public void recordExpanded() {
        this.mStats.setExpanded();
    }

    public void recordDirectReplied() {
        this.mStats.setDirectReplied();
    }

    public void recordDismissalSurface(int surface) {
        this.mStats.setDismissalSurface(surface);
    }

    public void recordDismissalSentiment(int sentiment) {
        this.mStats.setDismissalSentiment(sentiment);
    }

    public void recordSnoozed() {
        this.mStats.setSnoozed();
    }

    public void recordViewedSettings() {
        this.mStats.setViewedSettings();
    }

    public void setNumSmartRepliesAdded(int noReplies) {
        this.mNumberOfSmartRepliesAdded = noReplies;
    }

    public int getNumSmartRepliesAdded() {
        return this.mNumberOfSmartRepliesAdded;
    }

    public void setNumSmartActionsAdded(int noActions) {
        this.mNumberOfSmartActionsAdded = noActions;
    }

    public int getNumSmartActionsAdded() {
        return this.mNumberOfSmartActionsAdded;
    }

    public void setSuggestionsGeneratedByAssistant(boolean generatedByAssistant) {
        this.mSuggestionsGeneratedByAssistant = generatedByAssistant;
    }

    public boolean getSuggestionsGeneratedByAssistant() {
        return this.mSuggestionsGeneratedByAssistant;
    }

    public boolean getEditChoicesBeforeSending() {
        return this.mEditChoicesBeforeSending;
    }

    public void setEditChoicesBeforeSending(boolean editChoicesBeforeSending) {
        this.mEditChoicesBeforeSending = editChoicesBeforeSending;
    }

    public boolean hasSeenSmartReplies() {
        return this.mHasSeenSmartReplies;
    }

    public void setSeenSmartReplies(boolean hasSeenSmartReplies) {
        this.mHasSeenSmartReplies = hasSeenSmartReplies;
    }

    public boolean hasBeenVisiblyExpanded() {
        return this.stats.hasBeenVisiblyExpanded();
    }

    public void setSystemGeneratedSmartActions(ArrayList<Notification.Action> systemGeneratedSmartActions) {
        this.mSystemGeneratedSmartActions = systemGeneratedSmartActions;
    }

    public ArrayList<Notification.Action> getSystemGeneratedSmartActions() {
        return this.mSystemGeneratedSmartActions;
    }

    public void setSmartReplies(ArrayList<CharSequence> smartReplies) {
        this.mSmartReplies = smartReplies;
    }

    public ArrayList<CharSequence> getSmartReplies() {
        return this.mSmartReplies;
    }

    public boolean isProxied() {
        return !Objects.equals(this.sbn.getPackageName(), this.sbn.getOpPkg());
    }

    public ArraySet<Uri> getGrantableUris() {
        return this.mGrantableUris;
    }

    /* access modifiers changed from: protected */
    public void calculateGrantableUris() {
        NotificationChannel channel;
        Notification notification = getNotification();
        notification.visitUris(new Consumer() {
            /* class com.android.server.notification.$$Lambda$NotificationRecord$XgkrZGcjOHPHem34oE9qLGy3siA */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                NotificationRecord.this.lambda$calculateGrantableUris$0$NotificationRecord((Uri) obj);
            }
        });
        if (notification.getChannelId() != null && (channel = getChannel()) != null) {
            visitGrantableUri(channel.getSound(), (channel.getUserLockedFields() & 32) != 0);
        }
    }

    public /* synthetic */ void lambda$calculateGrantableUris$0$NotificationRecord(Uri uri) {
        visitGrantableUri(uri, false);
    }

    private void visitGrantableUri(Uri uri, boolean userOverriddenUri) {
        int sourceUid;
        if (uri != null && "content".equals(uri.getScheme()) && (sourceUid = this.sbn.getUid()) != 1000) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mUgmInternal.checkGrantUriPermission(sourceUid, (String) null, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(sourceUid)));
                if (this.mGrantableUris == null) {
                    this.mGrantableUris = new ArraySet<>();
                }
                this.mGrantableUris.add(uri);
            } catch (SecurityException e) {
                if (!userOverriddenUri) {
                    if (this.mTargetSdkVersion < 28) {
                        Log.w(TAG, "Ignoring " + uri + " from " + sourceUid + ": " + e.getMessage());
                    } else {
                        throw e;
                    }
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
            Binder.restoreCallingIdentity(ident);
        }
    }

    public LogMaker getLogMaker(long now) {
        LogMaker lm = this.sbn.getLogMaker().addTaggedData(858, Integer.valueOf(this.mImportance)).addTaggedData(793, Integer.valueOf(getLifespanMs(now))).addTaggedData(795, Integer.valueOf(getFreshnessMs(now))).addTaggedData(794, Integer.valueOf(getExposureMs(now))).addTaggedData((int) NetworkConstants.ETHER_MTU, Integer.valueOf(getInterruptionMs(now)));
        int i = this.mImportanceExplanationCode;
        if (i != 0) {
            lm.addTaggedData(1688, Integer.valueOf(i));
            int i2 = this.mImportanceExplanationCode;
            if ((i2 == 3 || i2 == 4) && this.stats.naturalImportance != -1000) {
                lm.addTaggedData(1690, Integer.valueOf(this.mInitialImportanceExplanationCode));
                lm.addTaggedData(1689, Integer.valueOf(this.stats.naturalImportance));
            }
        }
        int i3 = this.mAssistantImportance;
        if (i3 != -1000) {
            lm.addTaggedData(1691, Integer.valueOf(i3));
        }
        String str = this.mAdjustmentIssuer;
        if (str != null) {
            lm.addTaggedData(1742, Integer.valueOf(str.hashCode()));
        }
        return lm;
    }

    public LogMaker getLogMaker() {
        return getLogMaker(System.currentTimeMillis());
    }

    public LogMaker getItemLogMaker() {
        return getLogMaker().setCategory(128);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static final class Light {
        public final int color;
        public final int offMs;
        public final int onMs;

        public Light(int color2, int onMs2, int offMs2) {
            this.color = color2;
            this.onMs = onMs2;
            this.offMs = offMs2;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Light light = (Light) o;
            if (this.color != light.color || this.onMs != light.onMs) {
                return false;
            }
            if (this.offMs == light.offMs) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (((this.color * 31) + this.onMs) * 31) + this.offMs;
        }

        public String toString() {
            return "Light{color=" + this.color + ", onMs=" + this.onMs + ", offMs=" + this.offMs + '}';
        }
    }
}
