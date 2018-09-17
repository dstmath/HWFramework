package com.android.server.notification;

import android.app.Notification;
import android.app.Notification.Action;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService.Ranking;
import android.service.notification.StatusBarNotification;
import com.android.server.EventLogTags;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.notification.NotificationUsageStats.SingleNotificationStats;
import com.android.server.vr.EnabledComponentsObserver;
import com.android.server.wm.AppTransition;
import com.android.server.wm.WindowState;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

public final class NotificationRecord {
    static final boolean DBG = false;
    static final String TAG = "NotificationRecord";
    boolean isCanceled;
    public boolean isUpdate;
    private int mAuthoritativeRank;
    private float mContactAffinity;
    private final Context mContext;
    private long mCreationTimeMs;
    private String mGlobalSortKey;
    private int mImportance;
    private CharSequence mImportanceExplanation;
    private boolean mIntercept;
    boolean mIsSeen;
    final int mOriginalFlags;
    private int mPackagePriority;
    private int mPackageVisibility;
    private String mPeopleExplanation;
    private long mRankingTimeMs;
    private boolean mRecentlyIntrusive;
    private int mSuppressedVisualEffects;
    private long mUpdateTimeMs;
    private String mUserExplanation;
    private int mUserImportance;
    private long mVisibleSinceMs;
    public final StatusBarNotification sbn;
    SingleNotificationStats stats;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.notification.NotificationRecord.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.notification.NotificationRecord.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.NotificationRecord.<clinit>():void");
    }

    public NotificationRecord(Context context, StatusBarNotification sbn) {
        this.mUserImportance = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
        this.mImportance = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
        this.mImportanceExplanation = null;
        this.mSuppressedVisualEffects = 0;
        this.sbn = sbn;
        this.mOriginalFlags = sbn.getNotification().flags;
        this.mRankingTimeMs = calculateRankingTimeMs(0);
        this.mCreationTimeMs = sbn.getPostTime();
        this.mUpdateTimeMs = this.mCreationTimeMs;
        this.mContext = context;
        this.stats = new SingleNotificationStats();
        this.mImportance = defaultImportance();
    }

    private int defaultImportance() {
        Notification n = this.sbn.getNotification();
        int importance = 3;
        if ((n.flags & DumpState.DUMP_PACKAGES) != 0) {
            n.priority = 2;
        }
        switch (n.priority) {
            case EnabledComponentsObserver.NOT_INSTALLED /*-2*/:
                importance = 1;
                break;
            case AppTransition.TRANSIT_UNSET /*-1*/:
                importance = 2;
                break;
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                importance = 3;
                break;
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                importance = 4;
                break;
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                importance = 5;
                break;
        }
        this.stats.requestedImportance = importance;
        boolean isNoisy = ((n.defaults & 1) == 0 && (n.defaults & 2) == 0 && n.sound == null) ? n.vibrate != null ? true : DBG : true;
        this.stats.isNoisy = isNoisy;
        if (!isNoisy && importance > 2) {
            importance = 2;
        }
        if (isNoisy && importance < 3) {
            importance = 3;
        }
        if (n.fullScreenIntent != null) {
            importance = 5;
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

    void dump(PrintWriter pw, String prefix, Context baseContext, boolean redact) {
        int N;
        Notification notification = this.sbn.getNotification();
        Icon icon = notification.getSmallIcon();
        String iconStr = String.valueOf(icon);
        if (icon != null && icon.getType() == 2) {
            iconStr = iconStr + " / " + idDebugString(baseContext, icon.getResPackage(), icon.getResId());
        }
        pw.println(prefix + this);
        pw.println(prefix + "  uid=" + this.sbn.getUid() + " userId=" + this.sbn.getUserId());
        pw.println(prefix + "  icon=" + iconStr);
        pw.println(prefix + "  pri=" + notification.priority);
        pw.println(prefix + "  key=" + this.sbn.getKey());
        pw.println(prefix + "  seen=" + this.mIsSeen);
        pw.println(prefix + "  groupKey=" + getGroupKey());
        pw.println(prefix + "  contentIntent=" + notification.contentIntent);
        pw.println(prefix + "  deleteIntent=" + notification.deleteIntent);
        pw.println(prefix + "  tickerText=" + notification.tickerText);
        pw.println(prefix + "  contentView=" + notification.contentView);
        pw.println(prefix + String.format("  defaults=0x%08x flags=0x%08x", new Object[]{Integer.valueOf(notification.defaults), Integer.valueOf(notification.flags)}));
        pw.println(prefix + "  sound=" + notification.sound);
        pw.println(prefix + "  audioStreamType=" + notification.audioStreamType);
        pw.println(prefix + "  audioAttributes=" + notification.audioAttributes);
        pw.println(prefix + String.format("  color=0x%08x", new Object[]{Integer.valueOf(notification.color)}));
        pw.println(prefix + "  vibrate=" + Arrays.toString(notification.vibrate));
        pw.println(prefix + String.format("  led=0x%08x onMs=%d offMs=%d", new Object[]{Integer.valueOf(notification.ledARGB), Integer.valueOf(notification.ledOnMS), Integer.valueOf(notification.ledOffMS)}));
        if (notification.actions != null && notification.actions.length > 0) {
            pw.println(prefix + "  actions={");
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
            pw.println(prefix + "  extras={");
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
            pw.println(prefix + "  }");
        }
        pw.println(prefix + "  stats=" + this.stats.toString());
        pw.println(prefix + "  mContactAffinity=" + this.mContactAffinity);
        pw.println(prefix + "  mRecentlyIntrusive=" + this.mRecentlyIntrusive);
        pw.println(prefix + "  mPackagePriority=" + this.mPackagePriority);
        pw.println(prefix + "  mPackageVisibility=" + this.mPackageVisibility);
        pw.println(prefix + "  mUserImportance=" + Ranking.importanceToString(this.mUserImportance));
        pw.println(prefix + "  mImportance=" + Ranking.importanceToString(this.mImportance));
        pw.println(prefix + "  mImportanceExplanation=" + this.mImportanceExplanation);
        pw.println(prefix + "  mIntercept=" + this.mIntercept);
        pw.println(prefix + "  mGlobalSortKey=" + this.mGlobalSortKey);
        pw.println(prefix + "  mRankingTimeMs=" + this.mRankingTimeMs);
        pw.println(prefix + "  mCreationTimeMs=" + this.mCreationTimeMs);
        pw.println(prefix + "  mVisibleSinceMs=" + this.mVisibleSinceMs);
        pw.println(prefix + "  mUpdateTimeMs=" + this.mUpdateTimeMs);
        pw.println(prefix + "  mSuppressedVisualEffects= " + this.mSuppressedVisualEffects);
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
        return String.format("NotificationRecord(0x%08x: pkg=%s user=%s id=%d tag=%s importance=%d key=%s: %s)", new Object[]{Integer.valueOf(System.identityHashCode(this)), this.sbn.getPackageName(), this.sbn.getUser(), Integer.valueOf(this.sbn.getId()), this.sbn.getTag(), Integer.valueOf(this.mImportance), this.sbn.getKey(), this.sbn.getNotification()});
    }

    public void setContactAffinity(float contactAffinity) {
        this.mContactAffinity = contactAffinity;
        if (this.mImportance < 3 && this.mContactAffinity > TaskPositioner.RESIZING_HINT_ALPHA) {
            setImportance(3, getPeopleExplanation());
        }
    }

    public float getContactAffinity() {
        return this.mContactAffinity;
    }

    public void setRecentlyIntrusive(boolean recentlyIntrusive) {
        this.mRecentlyIntrusive = recentlyIntrusive;
    }

    public boolean isRecentlyIntrusive() {
        return this.mRecentlyIntrusive;
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
            this.mUserExplanation = this.mContext.getString(17040833);
        }
        return this.mUserExplanation;
    }

    private String getPeopleExplanation() {
        if (this.mPeopleExplanation == null) {
            this.mPeopleExplanation = this.mContext.getString(17040834);
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
        return getNotification().audioStreamType == stream ? true : DBG;
    }

    public boolean isAudioAttributesUsage(int usage) {
        AudioAttributes attributes = getNotification().audioAttributes;
        if (attributes == null || attributes.getUsage() != usage) {
            return DBG;
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

    public void setVisibility(boolean visible, int rank) {
        int i;
        long now = System.currentTimeMillis();
        this.mVisibleSinceMs = visible ? now : this.mVisibleSinceMs;
        this.stats.onVisibilityChanged(visible);
        String key = getKey();
        if (visible) {
            i = 1;
        } else {
            i = 0;
        }
        EventLogTags.writeNotificationVisibility(key, i, (int) (now - this.mCreationTimeMs), (int) (now - this.mUpdateTimeMs), 0, rank);
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

    public boolean isImportanceFromUser() {
        return this.mImportance == this.mUserImportance ? true : DBG;
    }
}
