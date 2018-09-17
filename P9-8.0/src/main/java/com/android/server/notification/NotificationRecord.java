package com.android.server.notification;

import android.app.Notification;
import android.app.Notification.Action;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.media.AudioSystem;
import android.metrics.LogMaker;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.service.notification.NotificationListenerService.Ranking;
import android.service.notification.SnoozeCriterion;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.widget.RemoteViews;
import com.android.internal.logging.MetricsLogger;
import com.android.server.EventLogTags;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.notification.NotificationUsageStats.SingleNotificationStats;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class NotificationRecord {
    static final boolean DBG = Log.isLoggable(TAG, 3);
    private static final int MAX_LOGTAG_LENGTH = 35;
    static final String TAG = "NotificationRecord";
    boolean isCanceled;
    public boolean isUpdate;
    private AudioAttributes mAttributes;
    private int mAuthoritativeRank;
    private NotificationChannel mChannel;
    private String mChannelIdLogTag;
    private float mContactAffinity;
    private final Context mContext;
    private long mCreationTimeMs;
    private String mGlobalSortKey;
    private String mGroupLogTag;
    private int mImportance = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
    private CharSequence mImportanceExplanation = null;
    private boolean mIntercept;
    boolean mIsSeen;
    private long mLastIntrusive;
    private Light mLight;
    private LogMaker mLogMaker;
    final int mOriginalFlags;
    private int mPackagePriority;
    private int mPackageVisibility;
    private String mPeopleExplanation;
    private ArrayList<String> mPeopleOverride;
    private boolean mPreChannelsNotification = true;
    private long mPushLogPowerTimeMs;
    private long mRankingTimeMs;
    private boolean mRecentlyIntrusive;
    private boolean mShowBadge;
    private ArrayList<SnoozeCriterion> mSnoozeCriteria;
    private Uri mSound;
    private int mSuppressedVisualEffects = 0;
    private long mUpdateTimeMs;
    private String mUserExplanation;
    private int mUserImportance = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
    private long[] mVibration;
    private long mVisibleSinceMs;
    public final StatusBarNotification sbn;
    SingleNotificationStats stats;

    static final class Light {
        public final int color;
        public final int offMs;
        public final int onMs;

        public Light(int color, int onMs, int offMs) {
            this.color = color;
            this.onMs = onMs;
            this.offMs = offMs;
        }

        public boolean equals(Object o) {
            boolean z = true;
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
            if (this.offMs != light.offMs) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return (((this.color * 31) + this.onMs) * 31) + this.offMs;
        }

        public String toString() {
            return "Light{color=" + this.color + ", onMs=" + this.onMs + ", offMs=" + this.offMs + '}';
        }
    }

    public NotificationRecord(Context context, StatusBarNotification sbn, NotificationChannel channel) {
        this.sbn = sbn;
        this.mOriginalFlags = sbn.getNotification().flags;
        this.mRankingTimeMs = calculateRankingTimeMs(0);
        this.mCreationTimeMs = sbn.getPostTime();
        this.mUpdateTimeMs = this.mCreationTimeMs;
        this.mPushLogPowerTimeMs = this.mCreationTimeMs;
        this.mContext = context;
        this.stats = new SingleNotificationStats();
        this.mChannel = channel;
        this.mPreChannelsNotification = isPreChannelsNotification();
        this.mSound = calculateSound();
        this.mVibration = calculateVibration();
        this.mAttributes = calculateAttributes();
        this.mImportance = calculateImportance();
        this.mLight = calculateLights();
    }

    private boolean isPreChannelsNotification() {
        try {
            if ("miscellaneous".equals(getChannel().getId()) && this.mContext.getPackageManager().getApplicationInfoAsUser(this.sbn.getPackageName(), 0, UserHandle.getUserId(this.sbn.getUid())).targetSdkVersion < 26) {
                return true;
            }
        } catch (NameNotFoundException e) {
            Slog.e(TAG, "Can't find package", e);
        }
        return false;
    }

    private Uri calculateSound() {
        Notification n = this.sbn.getNotification();
        if (this.mContext.getPackageManager().hasSystemFeature("android.software.leanback")) {
            return null;
        }
        Uri sound = this.mChannel.getSound();
        if (this.mPreChannelsNotification && (getChannel().getUserLockedFields() & 32) == 0) {
            sound = (n.defaults & 1) != 0 ? System.DEFAULT_NOTIFICATION_URI : n.sound;
        }
        return sound;
    }

    private Light calculateLights() {
        int channelLightColor;
        Light light;
        int defaultLightColor = this.mContext.getResources().getColor(17170524);
        int defaultLightOn = this.mContext.getResources().getInteger(17694770);
        int defaultLightOff = this.mContext.getResources().getInteger(17694769);
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
        light = new Light(notification.ledARGB, notification.ledOnMS, notification.ledOffMS);
        if ((notification.defaults & 4) != 0) {
            return new Light(defaultLightColor, defaultLightOn, defaultLightOff);
        }
        return light;
    }

    private long[] calculateVibration() {
        long[] defaultVibration = NotificationManagerService.getLongArray(this.mContext.getResources(), 17236000, 17, NotificationManagerService.DEFAULT_VIBRATE_PATTERN);
        long[] vibration = getChannel().shouldVibrate() ? getChannel().getVibrationPattern() == null ? defaultVibration : getChannel().getVibrationPattern() : null;
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
            return new Builder().setInternalLegacyStreamType(n.audioStreamType).build();
        }
        if (n.audioStreamType == -1) {
            return attributes;
        }
        Log.w(TAG, String.format("Invalid stream type: %d", new Object[]{Integer.valueOf(n.audioStreamType)}));
        return attributes;
    }

    private int calculateImportance() {
        boolean z = true;
        Notification n = this.sbn.getNotification();
        int importance = getChannel().getImportance();
        int requestedImportance = 3;
        if ((n.flags & 128) != 0) {
            n.priority = 2;
        }
        n.priority = NotificationManagerService.clamp(n.priority, -2, 2);
        switch (n.priority) {
            case -2:
                requestedImportance = 1;
                break;
            case -1:
                requestedImportance = 2;
                break;
            case 0:
                requestedImportance = 3;
                break;
            case 1:
            case 2:
                requestedImportance = 4;
                break;
        }
        this.stats.requestedImportance = requestedImportance;
        SingleNotificationStats singleNotificationStats = this.stats;
        if (this.mSound == null && this.mVibration == null) {
            z = false;
        }
        singleNotificationStats.isNoisy = z;
        Boolean isDefaultChannel = Boolean.valueOf("miscellaneous".equals(getChannel().getId()));
        if (this.mPreChannelsNotification && (importance == JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE || isDefaultChannel.booleanValue() || (getChannel().getUserLockedFields() & 4) == 0)) {
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
            Log.w(TAG, "Use default channel, The importance is:" + importance);
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
        this.mRankingTimeMs = calculateRankingTimeMs(previous.getRankingTimeMs());
        this.mCreationTimeMs = previous.mCreationTimeMs;
        this.mVisibleSinceMs = previous.mVisibleSinceMs;
        this.mPushLogPowerTimeMs = previous.mPushLogPowerTimeMs;
        if (previous.sbn.getOverrideGroupKey() != null && (this.sbn.isAppGroup() ^ 1) != 0 && needOverrideGroupKey(previous)) {
            this.sbn.setOverrideGroupKey(previous.sbn.getOverrideGroupKey());
        }
    }

    private boolean needOverrideGroupKey(NotificationRecord previous) {
        Bundle newBundle = this.sbn.getNotification().extras;
        Bundle preBundle = previous.sbn.getNotification().extras;
        if (newBundle == null || !newBundle.containsKey("hw_btw") || preBundle == null || !preBundle.containsKey("hw_btw") || newBundle.getBoolean("hw_btw") == preBundle.getBoolean("hw_btw")) {
            return true;
        }
        return false;
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

    void dump(ProtoOutputStream proto, boolean redact) {
        boolean z = true;
        proto.write(1159641169921L, this.sbn.getKey());
        if (getChannel() != null) {
            proto.write(1159641169924L, getChannel().getId());
        }
        proto.write(1155346202632L, getLight() != null);
        if (getVibration() == null) {
            z = false;
        }
        proto.write(1155346202631L, z);
        proto.write(1112396529667L, this.sbn.getNotification().flags);
        proto.write(1159641169929L, getGroupKey());
        proto.write(1112396529674L, getImportance());
        if (getSound() != null) {
            proto.write(1159641169925L, getSound().toString());
        }
        if (getAudioAttributes() != null) {
            proto.write(1112396529670L, getAudioAttributes().getUsage());
        }
    }

    String formatRemoteViews(RemoteViews rv) {
        if (rv == null) {
            return "null";
        }
        return String.format("%s/0x%08x (%d bytes): %s", new Object[]{rv.getPackage(), Integer.valueOf(rv.getLayoutId()), Integer.valueOf(rv.estimateMemoryUsage()), rv.toString()});
    }

    void dump(PrintWriter pw, String prefix, Context baseContext, boolean redact) {
        int N;
        Notification notification = this.sbn.getNotification();
        Icon icon = notification.getSmallIcon();
        String iconStr = String.valueOf(icon);
        if (icon != null && icon.getType() == 2) {
            iconStr = iconStr + " / " + idDebugString(baseContext, icon.getResPackage(), icon.getResId());
        }
        pw.println(prefix + this);
        prefix = prefix + "  ";
        pw.println(prefix + "uid=" + this.sbn.getUid() + " userId=" + this.sbn.getUserId());
        pw.println(prefix + "icon=" + iconStr);
        pw.println(prefix + "pri=" + notification.priority);
        pw.println(prefix + "key=" + this.sbn.getKey());
        pw.println(prefix + "seen=" + this.mIsSeen);
        pw.println(prefix + "groupKey=" + getGroupKey());
        pw.println(prefix + "fullscreenIntent=" + notification.fullScreenIntent);
        pw.println(prefix + "contentIntent=" + notification.contentIntent);
        pw.println(prefix + "deleteIntent=" + notification.deleteIntent);
        pw.print(prefix + "tickerText=");
        if (TextUtils.isEmpty(notification.tickerText)) {
            pw.println("null");
        } else {
            String ticker = notification.tickerText.toString();
            if (redact) {
                pw.print(ticker.length() > 16 ? ticker.substring(0, 8) : "");
                pw.println("...");
            } else {
                pw.println(ticker);
            }
        }
        pw.println(prefix + "contentView=" + formatRemoteViews(notification.contentView));
        pw.println(prefix + "bigContentView=" + formatRemoteViews(notification.bigContentView));
        pw.println(prefix + "headsUpContentView=" + formatRemoteViews(notification.headsUpContentView));
        pw.print(prefix + String.format("color=0x%08x", new Object[]{Integer.valueOf(notification.color)}));
        pw.println(prefix + "timeout=" + TimeUtils.formatForLogging(notification.getTimeoutAfter()));
        if (notification.actions != null && notification.actions.length > 0) {
            pw.println(prefix + "actions={");
            N = notification.actions.length;
            for (int i = 0; i < N; i++) {
                Action action = notification.actions[i];
                if (action != null) {
                    String str = "%s    [%d] \"%s\" -> %s";
                    Object[] objArr = new Object[4];
                    objArr[0] = prefix;
                    objArr[1] = Integer.valueOf(i);
                    objArr[2] = action.title;
                    objArr[3] = action.actionIntent == null ? "null" : action.actionIntent.toString();
                    pw.println(String.format(str, objArr));
                }
            }
            pw.println(prefix + "  }");
        }
        if (notification.extras != null && notification.extras.size() > 0) {
            pw.println(prefix + "extras={");
            for (String key : notification.extras.keySet()) {
                pw.print(prefix + "    " + key + "=");
                Object val = notification.extras.get(key);
                if (val == null) {
                    pw.println("null");
                } else {
                    pw.print(val.getClass().getSimpleName());
                    if (!(redact && ((val instanceof CharSequence) || (val instanceof String)))) {
                        if (val instanceof Bitmap) {
                            pw.print(String.format(" (%dx%d)", new Object[]{Integer.valueOf(((Bitmap) val).getWidth()), Integer.valueOf(((Bitmap) val).getHeight())}));
                        } else if (val.getClass().isArray()) {
                            N = Array.getLength(val);
                            pw.print(" (" + N + ")");
                            if (!redact) {
                                for (int j = 0; j < N; j++) {
                                    pw.println();
                                    pw.print(String.format("%s      [%d] %s", new Object[]{prefix, Integer.valueOf(j), String.valueOf(Array.get(val, j))}));
                                }
                            }
                        } else {
                            pw.print(" (" + String.valueOf(val) + ")");
                        }
                    }
                    pw.println();
                }
            }
            pw.println(prefix + "}");
        }
        pw.println(prefix + "stats=" + this.stats.toString());
        pw.println(prefix + "mContactAffinity=" + this.mContactAffinity);
        pw.println(prefix + "mRecentlyIntrusive=" + this.mRecentlyIntrusive);
        pw.println(prefix + "mPackagePriority=" + this.mPackagePriority);
        pw.println(prefix + "mPackageVisibility=" + this.mPackageVisibility);
        pw.println(prefix + "mUserImportance=" + Ranking.importanceToString(this.mUserImportance));
        pw.println(prefix + "mImportance=" + Ranking.importanceToString(this.mImportance));
        pw.println(prefix + "mImportanceExplanation=" + this.mImportanceExplanation);
        pw.println(prefix + "mIntercept=" + this.mIntercept);
        pw.println(prefix + "mGlobalSortKey=" + this.mGlobalSortKey);
        pw.println(prefix + "mRankingTimeMs=" + this.mRankingTimeMs);
        pw.println(prefix + "mCreationTimeMs=" + this.mCreationTimeMs);
        pw.println(prefix + "mVisibleSinceMs=" + this.mVisibleSinceMs);
        pw.println(prefix + "mUpdateTimeMs=" + this.mUpdateTimeMs);
        pw.println(prefix + "mSuppressedVisualEffects= " + this.mSuppressedVisualEffects);
        if (this.mPreChannelsNotification) {
            pw.println(prefix + String.format("defaults=0x%08x flags=0x%08x", new Object[]{Integer.valueOf(notification.defaults), Integer.valueOf(notification.flags)}));
            pw.println(prefix + "n.sound=" + notification.sound);
            pw.println(prefix + "n.audioStreamType=" + notification.audioStreamType);
            pw.println(prefix + "n.audioAttributes=" + notification.audioAttributes);
            pw.println(prefix + String.format("  led=0x%08x onMs=%d offMs=%d", new Object[]{Integer.valueOf(notification.ledARGB), Integer.valueOf(notification.ledOnMS), Integer.valueOf(notification.ledOffMS)}));
            pw.println(prefix + "vibrate=" + Arrays.toString(notification.vibrate));
        }
        pw.println(prefix + "mSound= " + this.mSound);
        pw.println(prefix + "mVibration= " + this.mVibration);
        pw.println(prefix + "mAttributes= " + this.mAttributes);
        pw.println(prefix + "mLight= " + this.mLight);
        pw.println(prefix + "mShowBadge=" + this.mShowBadge);
        pw.println(prefix + "effectiveNotificationChannel=" + getChannel());
        if (getPeopleOverride() != null) {
            pw.println(prefix + "overridePeople= " + TextUtils.join(",", getPeopleOverride()));
        }
        if (getSnoozeCriteria() != null) {
            pw.println(prefix + "snoozeCriteria=" + TextUtils.join(",", getSnoozeCriteria()));
        }
    }

    static String idDebugString(Context baseContext, String packageName, int id) {
        Context c;
        if (packageName != null) {
            try {
                c = baseContext.createPackageContext(packageName, 0);
            } catch (NameNotFoundException e) {
                c = baseContext;
            }
        } else {
            c = baseContext;
        }
        try {
            return c.getResources().getResourceName(id);
        } catch (NotFoundException e2) {
            return "<name unknown>";
        }
    }

    public final String toString() {
        return String.format("NotificationRecord(0x%08x: pkg=%s user=%s id=%d tag=%s importance=%d key=%s channel=%s: %s)", new Object[]{Integer.valueOf(System.identityHashCode(this)), this.sbn.getPackageName(), this.sbn.getUser(), Integer.valueOf(this.sbn.getId()), this.sbn.getTag(), Integer.valueOf(this.mImportance), this.sbn.getKey(), getChannel().getId(), this.sbn.getNotification()});
    }

    public void setContactAffinity(float contactAffinity) {
        this.mContactAffinity = contactAffinity;
        if (this.mImportance < 3 && this.mContactAffinity > 0.5f) {
            setImportance(3, getPeopleExplanation());
        }
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

    public void setUserImportance(int importance) {
        this.mUserImportance = importance;
        applyUserImportance();
    }

    private String getUserExplanation() {
        if (this.mUserExplanation == null) {
            this.mUserExplanation = this.mContext.getResources().getString(17040151);
        }
        return this.mUserExplanation;
    }

    private String getPeopleExplanation() {
        if (this.mPeopleExplanation == null) {
            this.mPeopleExplanation = this.mContext.getResources().getString(17040150);
        }
        return this.mPeopleExplanation;
    }

    private void applyUserImportance() {
        if (this.mUserImportance != JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE) {
            this.mImportance = this.mUserImportance;
            this.mImportanceExplanation = getUserExplanation();
        }
    }

    public int getUserImportance() {
        return this.mUserImportance;
    }

    public void setImportance(int importance, CharSequence explanation) {
        if (importance != JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE) {
            this.mImportance = importance;
            this.mImportanceExplanation = explanation;
        }
        applyUserImportance();
    }

    public int getImportance() {
        return this.mImportance;
    }

    public CharSequence getImportanceExplanation() {
        return this.mImportanceExplanation;
    }

    public boolean setIntercepted(boolean intercept) {
        this.mIntercept = intercept;
        return this.mIntercept;
    }

    public boolean isIntercepted() {
        return this.mIntercept;
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

    public boolean isAudioStream(int stream) {
        return getNotification().audioStreamType == stream;
    }

    public boolean isAudioAttributesUsage(int usage) {
        AudioAttributes attributes = getNotification().audioAttributes;
        if (attributes == null || attributes.getUsage() != usage) {
            return false;
        }
        return true;
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
        return this.mVisibleSinceMs == 0 ? 0 : (int) (now - this.mVisibleSinceMs);
    }

    protected long getPushLogPowerTimeMs(long now) {
        return now - this.mPushLogPowerTimeMs;
    }

    protected void setPushLogPowerTimeMs(long timeMs) {
        this.mPushLogPowerTimeMs = timeMs;
    }

    public void setVisibility(boolean visible, int rank) {
        int i;
        long now = System.currentTimeMillis();
        this.mVisibleSinceMs = visible ? now : this.mVisibleSinceMs;
        this.stats.onVisibilityChanged(visible);
        MetricsLogger.action(getLogMaker(now).setCategory(128).setType(visible ? 1 : 2).addTaggedData(798, Integer.valueOf(rank)));
        if (visible) {
            MetricsLogger.histogram(this.mContext, "note_freshness", getFreshnessMs(now));
        }
        String key = getKey();
        if (visible) {
            i = 1;
        } else {
            i = 0;
        }
        EventLogTags.writeNotificationVisibility(key, i, getLifespanMs(now), getFreshnessMs(now), 0, rank);
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
        return this.mIsSeen;
    }

    public void setSeen() {
        this.mIsSeen = true;
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
        this.mGroupLogTag = null;
    }

    private String getGroupLogTag() {
        if (this.mGroupLogTag == null) {
            this.mGroupLogTag = shortenTag(this.sbn.getGroup());
        }
        return this.mGroupLogTag;
    }

    private String getChannelIdLogTag() {
        if (this.mChannelIdLogTag == null) {
            this.mChannelIdLogTag = shortenTag(this.mChannel.getId());
        }
        return this.mChannelIdLogTag;
    }

    private String shortenTag(String longTag) {
        if (longTag == null) {
            return null;
        }
        if (longTag.length() < 35) {
            return longTag;
        }
        return longTag.substring(0, 27) + "-" + Integer.toHexString(longTag.hashCode());
    }

    public boolean isImportanceFromUser() {
        return this.mImportance == this.mUserImportance;
    }

    public NotificationChannel getChannel() {
        return this.mChannel;
    }

    protected void updateNotificationChannel(NotificationChannel channel) {
        if (channel != null) {
            this.mChannel = channel;
            calculateImportance();
        }
    }

    public void setShowBadge(boolean showBadge) {
        this.mShowBadge = showBadge;
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

    protected void setPeopleOverride(ArrayList<String> people) {
        this.mPeopleOverride = people;
    }

    public ArrayList<SnoozeCriterion> getSnoozeCriteria() {
        return this.mSnoozeCriteria;
    }

    protected void setSnoozeCriteria(ArrayList<SnoozeCriterion> snoozeCriteria) {
        this.mSnoozeCriteria = snoozeCriteria;
    }

    public LogMaker getLogMaker(long now) {
        int i = 0;
        if (this.mLogMaker == null) {
            this.mLogMaker = new LogMaker(0).setPackageName(this.sbn.getPackageName()).addTaggedData(796, Integer.valueOf(this.sbn.getId())).addTaggedData(797, this.sbn.getTag()).addTaggedData(857, getChannelIdLogTag());
        }
        LogMaker addTaggedData = this.mLogMaker.clearCategory().clearType().clearSubtype().clearTaggedData(798).addTaggedData(858, Integer.valueOf(this.mImportance)).addTaggedData(946, getGroupLogTag());
        if (this.sbn.getNotification().isGroupSummary()) {
            i = 1;
        }
        return addTaggedData.addTaggedData(947, Integer.valueOf(i)).addTaggedData(793, Integer.valueOf(getLifespanMs(now))).addTaggedData(795, Integer.valueOf(getFreshnessMs(now))).addTaggedData(794, Integer.valueOf(getExposureMs(now)));
    }

    public LogMaker getLogMaker() {
        return getLogMaker(System.currentTimeMillis());
    }
}
