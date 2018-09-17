package android.app.job;

import android.content.ClipData;
import android.content.ComponentName;
import android.net.Uri;
import android.os.BaseBundle;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PersistableBundle;
import android.util.Log;
import android.util.TimeUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class JobInfo implements Parcelable {
    public static final int BACKOFF_POLICY_EXPONENTIAL = 1;
    public static final int BACKOFF_POLICY_LINEAR = 0;
    public static final int CONSTRAINT_FLAG_BATTERY_NOT_LOW = 2;
    public static final int CONSTRAINT_FLAG_CHARGING = 1;
    public static final int CONSTRAINT_FLAG_DEVICE_IDLE = 4;
    public static final int CONSTRAINT_FLAG_STORAGE_NOT_LOW = 8;
    public static final Creator<JobInfo> CREATOR = new Creator<JobInfo>() {
        public JobInfo createFromParcel(Parcel in) {
            return new JobInfo(in, null);
        }

        public JobInfo[] newArray(int size) {
            return new JobInfo[size];
        }
    };
    public static final int DEFAULT_BACKOFF_POLICY = 1;
    public static final long DEFAULT_INITIAL_BACKOFF_MILLIS = 30000;
    public static final int FLAG_WILL_BE_FOREGROUND = 1;
    public static final long MAX_BACKOFF_DELAY_MILLIS = 18000000;
    public static final long MIN_BACKOFF_MILLIS = 10000;
    private static final long MIN_FLEX_MILLIS = 300000;
    private static final long MIN_PERIOD_MILLIS = 900000;
    public static final int NETWORK_TYPE_ANY = 1;
    public static final int NETWORK_TYPE_METERED = 4;
    public static final int NETWORK_TYPE_NONE = 0;
    public static final int NETWORK_TYPE_NOT_ROAMING = 3;
    public static final int NETWORK_TYPE_UNMETERED = 2;
    public static final int PRIORITY_ADJ_ALWAYS_RUNNING = -80;
    public static final int PRIORITY_ADJ_OFTEN_RUNNING = -40;
    public static final int PRIORITY_DEFAULT = 0;
    public static final int PRIORITY_FOREGROUND_APP = 30;
    public static final int PRIORITY_SYNC_EXPEDITED = 10;
    public static final int PRIORITY_SYNC_INITIALIZATION = 20;
    public static final int PRIORITY_TOP_APP = 40;
    private static String TAG = "JobInfo";
    private final int backoffPolicy;
    private final ClipData clipData;
    private final int clipGrantFlags;
    private final int constraintFlags;
    private final PersistableBundle extras;
    private final int flags;
    private final long flexMillis;
    private final boolean hasEarlyConstraint;
    private final boolean hasLateConstraint;
    private final long initialBackoffMillis;
    private final long intervalMillis;
    private final boolean isPeriodic;
    private final boolean isPersisted;
    private final int jobId;
    private final long maxExecutionDelayMillis;
    private final long minLatencyMillis;
    private final int networkType;
    private final int priority;
    private final ComponentName service;
    private final Bundle transientExtras;
    private final long triggerContentMaxDelay;
    private final long triggerContentUpdateDelay;
    private final TriggerContentUri[] triggerContentUris;

    public static final class Builder {
        private int mBackoffPolicy = 1;
        private boolean mBackoffPolicySet = false;
        private ClipData mClipData;
        private int mClipGrantFlags;
        private int mConstraintFlags;
        private PersistableBundle mExtras = PersistableBundle.EMPTY;
        private int mFlags;
        private long mFlexMillis;
        private boolean mHasEarlyConstraint;
        private boolean mHasLateConstraint;
        private long mInitialBackoffMillis = JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS;
        private long mIntervalMillis;
        private boolean mIsPeriodic;
        private boolean mIsPersisted;
        private final int mJobId;
        private final ComponentName mJobService;
        private long mMaxExecutionDelayMillis;
        private long mMinLatencyMillis;
        private int mNetworkType;
        private int mPriority = 0;
        private Bundle mTransientExtras = Bundle.EMPTY;
        private long mTriggerContentMaxDelay = -1;
        private long mTriggerContentUpdateDelay = -1;
        private ArrayList<TriggerContentUri> mTriggerContentUris;

        public Builder(int jobId, ComponentName jobService) {
            this.mJobService = jobService;
            this.mJobId = jobId;
        }

        public Builder setPriority(int priority) {
            this.mPriority = priority;
            return this;
        }

        public Builder setFlags(int flags) {
            this.mFlags = flags;
            return this;
        }

        public Builder setExtras(PersistableBundle extras) {
            this.mExtras = extras;
            return this;
        }

        public Builder setTransientExtras(Bundle extras) {
            this.mTransientExtras = extras;
            return this;
        }

        public Builder setClipData(ClipData clip, int grantFlags) {
            this.mClipData = clip;
            this.mClipGrantFlags = grantFlags;
            return this;
        }

        public Builder setRequiredNetworkType(int networkType) {
            this.mNetworkType = networkType;
            return this;
        }

        public Builder setRequiresCharging(boolean requiresCharging) {
            this.mConstraintFlags = (requiresCharging ? 1 : 0) | (this.mConstraintFlags & -2);
            return this;
        }

        public Builder setRequiresBatteryNotLow(boolean batteryNotLow) {
            this.mConstraintFlags = (batteryNotLow ? 2 : 0) | (this.mConstraintFlags & -3);
            return this;
        }

        public Builder setRequiresDeviceIdle(boolean requiresDeviceIdle) {
            this.mConstraintFlags = (requiresDeviceIdle ? 4 : 0) | (this.mConstraintFlags & -5);
            return this;
        }

        public Builder setRequiresStorageNotLow(boolean storageNotLow) {
            this.mConstraintFlags = (storageNotLow ? 8 : 0) | (this.mConstraintFlags & -9);
            return this;
        }

        public Builder addTriggerContentUri(TriggerContentUri uri) {
            if (this.mTriggerContentUris == null) {
                this.mTriggerContentUris = new ArrayList();
            }
            this.mTriggerContentUris.add(uri);
            return this;
        }

        public Builder setTriggerContentUpdateDelay(long durationMs) {
            this.mTriggerContentUpdateDelay = durationMs;
            return this;
        }

        public Builder setTriggerContentMaxDelay(long durationMs) {
            this.mTriggerContentMaxDelay = durationMs;
            return this;
        }

        public Builder setPeriodic(long intervalMillis) {
            return setPeriodic(intervalMillis, intervalMillis);
        }

        public Builder setPeriodic(long intervalMillis, long flexMillis) {
            this.mIsPeriodic = true;
            this.mIntervalMillis = intervalMillis;
            this.mFlexMillis = flexMillis;
            this.mHasLateConstraint = true;
            this.mHasEarlyConstraint = true;
            return this;
        }

        public Builder setMinimumLatency(long minLatencyMillis) {
            this.mMinLatencyMillis = minLatencyMillis;
            this.mHasEarlyConstraint = true;
            return this;
        }

        public Builder setOverrideDeadline(long maxExecutionDelayMillis) {
            this.mMaxExecutionDelayMillis = maxExecutionDelayMillis;
            this.mHasLateConstraint = true;
            return this;
        }

        public Builder setBackoffCriteria(long initialBackoffMillis, int backoffPolicy) {
            this.mBackoffPolicySet = true;
            this.mInitialBackoffMillis = initialBackoffMillis;
            this.mBackoffPolicy = backoffPolicy;
            return this;
        }

        public Builder setPersisted(boolean isPersisted) {
            this.mIsPersisted = isPersisted;
            return this;
        }

        public JobInfo build() {
            if (!this.mHasEarlyConstraint && (this.mHasLateConstraint ^ 1) != 0 && this.mConstraintFlags == 0 && this.mNetworkType == 0 && this.mTriggerContentUris == null) {
                throw new IllegalArgumentException("You're trying to build a job with no constraints, this is not allowed.");
            }
            if (this.mIsPeriodic) {
                if (this.mMaxExecutionDelayMillis != 0) {
                    throw new IllegalArgumentException("Can't call setOverrideDeadline() on a periodic job.");
                } else if (this.mMinLatencyMillis != 0) {
                    throw new IllegalArgumentException("Can't call setMinimumLatency() on a periodic job");
                } else if (this.mTriggerContentUris != null) {
                    throw new IllegalArgumentException("Can't call addTriggerContentUri() on a periodic job");
                }
            }
            if (this.mIsPersisted) {
                if (this.mTriggerContentUris != null) {
                    throw new IllegalArgumentException("Can't call addTriggerContentUri() on a persisted job");
                } else if (!this.mTransientExtras.isEmpty()) {
                    throw new IllegalArgumentException("Can't call setTransientExtras() on a persisted job");
                } else if (this.mClipData != null) {
                    throw new IllegalArgumentException("Can't call setClipData() on a persisted job");
                }
            }
            if (!this.mBackoffPolicySet || (this.mConstraintFlags & 4) == 0) {
                JobInfo job = new JobInfo(this, null);
                if (job.isPeriodic()) {
                    StringBuilder builder;
                    if (job.intervalMillis != job.getIntervalMillis()) {
                        builder = new StringBuilder();
                        builder.append("Specified interval for ").append(String.valueOf(this.mJobId)).append(" is ");
                        TimeUtils.formatDuration(this.mIntervalMillis, builder);
                        builder.append(". Clamped to ");
                        TimeUtils.formatDuration(job.getIntervalMillis(), builder);
                        Log.w(JobInfo.TAG, builder.toString());
                    }
                    if (job.flexMillis != job.getFlexMillis()) {
                        builder = new StringBuilder();
                        builder.append("Specified flex for ").append(String.valueOf(this.mJobId)).append(" is ");
                        TimeUtils.formatDuration(this.mFlexMillis, builder);
                        builder.append(". Clamped to ");
                        TimeUtils.formatDuration(job.getFlexMillis(), builder);
                        Log.w(JobInfo.TAG, builder.toString());
                    }
                }
                return job;
            }
            throw new IllegalArgumentException("An idle mode job will not respect any back-off policy, so calling setBackoffCriteria with setRequiresDeviceIdle is an error.");
        }
    }

    public static final class TriggerContentUri implements Parcelable {
        public static final Creator<TriggerContentUri> CREATOR = new Creator<TriggerContentUri>() {
            public TriggerContentUri createFromParcel(Parcel in) {
                return new TriggerContentUri(in, null);
            }

            public TriggerContentUri[] newArray(int size) {
                return new TriggerContentUri[size];
            }
        };
        public static final int FLAG_NOTIFY_FOR_DESCENDANTS = 1;
        private final int mFlags;
        private final Uri mUri;

        public TriggerContentUri(Uri uri, int flags) {
            this.mUri = uri;
            this.mFlags = flags;
        }

        public Uri getUri() {
            return this.mUri;
        }

        public int getFlags() {
            return this.mFlags;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof TriggerContentUri)) {
                return false;
            }
            TriggerContentUri t = (TriggerContentUri) o;
            if (Objects.equals(t.mUri, this.mUri) && t.mFlags == this.mFlags) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return (this.mUri == null ? 0 : this.mUri.hashCode()) ^ this.mFlags;
        }

        private TriggerContentUri(Parcel in) {
            this.mUri = (Uri) Uri.CREATOR.createFromParcel(in);
            this.mFlags = in.readInt();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            this.mUri.writeToParcel(out, flags);
            out.writeInt(this.mFlags);
        }
    }

    public static final long getMinPeriodMillis() {
        return 900000;
    }

    public static final long getMinFlexMillis() {
        return MIN_FLEX_MILLIS;
    }

    public static final long getMinBackoffMillis() {
        return MIN_BACKOFF_MILLIS;
    }

    public int getId() {
        return this.jobId;
    }

    public PersistableBundle getExtras() {
        return this.extras;
    }

    public Bundle getTransientExtras() {
        return this.transientExtras;
    }

    public ClipData getClipData() {
        return this.clipData;
    }

    public int getClipGrantFlags() {
        return this.clipGrantFlags;
    }

    public ComponentName getService() {
        return this.service;
    }

    public int getPriority() {
        return this.priority;
    }

    public int getFlags() {
        return this.flags;
    }

    public boolean isRequireCharging() {
        return (this.constraintFlags & 1) != 0;
    }

    public boolean isRequireBatteryNotLow() {
        return (this.constraintFlags & 2) != 0;
    }

    public boolean isRequireDeviceIdle() {
        return (this.constraintFlags & 4) != 0;
    }

    public boolean isRequireStorageNotLow() {
        return (this.constraintFlags & 8) != 0;
    }

    public int getConstraintFlags() {
        return this.constraintFlags;
    }

    public TriggerContentUri[] getTriggerContentUris() {
        return this.triggerContentUris;
    }

    public long getTriggerContentUpdateDelay() {
        return this.triggerContentUpdateDelay;
    }

    public long getTriggerContentMaxDelay() {
        return this.triggerContentMaxDelay;
    }

    public int getNetworkType() {
        return this.networkType;
    }

    public long getMinLatencyMillis() {
        return this.minLatencyMillis;
    }

    public long getMaxExecutionDelayMillis() {
        return this.maxExecutionDelayMillis;
    }

    public boolean isPeriodic() {
        return this.isPeriodic;
    }

    public boolean isPersisted() {
        return this.isPersisted;
    }

    public long getIntervalMillis() {
        long minInterval = getMinPeriodMillis();
        return this.intervalMillis >= minInterval ? this.intervalMillis : minInterval;
    }

    public long getFlexMillis() {
        long interval = getIntervalMillis();
        long clampedFlex = Math.max(this.flexMillis, Math.max((5 * interval) / 100, getMinFlexMillis()));
        return clampedFlex <= interval ? clampedFlex : interval;
    }

    public long getInitialBackoffMillis() {
        long minBackoff = getMinBackoffMillis();
        return this.initialBackoffMillis >= minBackoff ? this.initialBackoffMillis : minBackoff;
    }

    public int getBackoffPolicy() {
        return this.backoffPolicy;
    }

    public boolean hasEarlyConstraint() {
        return this.hasEarlyConstraint;
    }

    public boolean hasLateConstraint() {
        return this.hasLateConstraint;
    }

    private static boolean kindofEqualsBundle(BaseBundle a, BaseBundle b) {
        if (a != b) {
            return a != null ? a.kindofEquals(b) : false;
        } else {
            return true;
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof JobInfo)) {
            return false;
        }
        JobInfo j = (JobInfo) o;
        if (this.jobId == j.jobId && kindofEqualsBundle(this.extras, j.extras) && kindofEqualsBundle(this.transientExtras, j.transientExtras) && this.clipData == j.clipData && this.clipGrantFlags == j.clipGrantFlags && Objects.equals(this.service, j.service) && this.constraintFlags == j.constraintFlags && Arrays.equals(this.triggerContentUris, j.triggerContentUris) && this.triggerContentUpdateDelay == j.triggerContentUpdateDelay && this.triggerContentMaxDelay == j.triggerContentMaxDelay && this.hasEarlyConstraint == j.hasEarlyConstraint && this.hasLateConstraint == j.hasLateConstraint && this.networkType == j.networkType && this.minLatencyMillis == j.minLatencyMillis && this.maxExecutionDelayMillis == j.maxExecutionDelayMillis && this.isPeriodic == j.isPeriodic && this.isPersisted == j.isPersisted && this.intervalMillis == j.intervalMillis && this.flexMillis == j.flexMillis && this.initialBackoffMillis == j.initialBackoffMillis && this.backoffPolicy == j.backoffPolicy && this.priority == j.priority && this.flags == j.flags) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int hashCode = this.jobId;
        if (this.extras != null) {
            hashCode = (hashCode * 31) + this.extras.hashCode();
        }
        if (this.transientExtras != null) {
            hashCode = (hashCode * 31) + this.transientExtras.hashCode();
        }
        if (this.clipData != null) {
            hashCode = (hashCode * 31) + this.clipData.hashCode();
        }
        hashCode = (hashCode * 31) + this.clipGrantFlags;
        if (this.service != null) {
            hashCode = (hashCode * 31) + this.service.hashCode();
        }
        hashCode = (hashCode * 31) + this.constraintFlags;
        if (this.triggerContentUris != null) {
            hashCode = (hashCode * 31) + Arrays.hashCode(this.triggerContentUris);
        }
        return (((((((((((((((((((((((((((((hashCode * 31) + Long.hashCode(this.triggerContentUpdateDelay)) * 31) + Long.hashCode(this.triggerContentMaxDelay)) * 31) + Boolean.hashCode(this.hasEarlyConstraint)) * 31) + Boolean.hashCode(this.hasLateConstraint)) * 31) + this.networkType) * 31) + Long.hashCode(this.minLatencyMillis)) * 31) + Long.hashCode(this.maxExecutionDelayMillis)) * 31) + Boolean.hashCode(this.isPeriodic)) * 31) + Boolean.hashCode(this.isPersisted)) * 31) + Long.hashCode(this.intervalMillis)) * 31) + Long.hashCode(this.flexMillis)) * 31) + Long.hashCode(this.initialBackoffMillis)) * 31) + this.backoffPolicy) * 31) + this.priority) * 31) + this.flags;
    }

    private JobInfo(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.jobId = in.readInt();
        this.extras = in.readPersistableBundle();
        this.transientExtras = in.readBundle();
        if (in.readInt() != 0) {
            this.clipData = (ClipData) ClipData.CREATOR.createFromParcel(in);
            this.clipGrantFlags = in.readInt();
        } else {
            this.clipData = null;
            this.clipGrantFlags = 0;
        }
        this.service = (ComponentName) in.readParcelable(null);
        this.constraintFlags = in.readInt();
        this.triggerContentUris = (TriggerContentUri[]) in.createTypedArray(TriggerContentUri.CREATOR);
        this.triggerContentUpdateDelay = in.readLong();
        this.triggerContentMaxDelay = in.readLong();
        this.networkType = in.readInt();
        this.minLatencyMillis = in.readLong();
        this.maxExecutionDelayMillis = in.readLong();
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.isPeriodic = z;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.isPersisted = z;
        this.intervalMillis = in.readLong();
        this.flexMillis = in.readLong();
        this.initialBackoffMillis = in.readLong();
        this.backoffPolicy = in.readInt();
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.hasEarlyConstraint = z;
        if (in.readInt() != 1) {
            z2 = false;
        }
        this.hasLateConstraint = z2;
        this.priority = in.readInt();
        this.flags = in.readInt();
    }

    private JobInfo(Builder b) {
        TriggerContentUri[] triggerContentUriArr = null;
        this.jobId = b.mJobId;
        this.extras = b.mExtras.deepCopy();
        this.transientExtras = b.mTransientExtras.deepCopy();
        this.clipData = b.mClipData;
        this.clipGrantFlags = b.mClipGrantFlags;
        this.service = b.mJobService;
        this.constraintFlags = b.mConstraintFlags;
        if (b.mTriggerContentUris != null) {
            triggerContentUriArr = (TriggerContentUri[]) b.mTriggerContentUris.toArray(new TriggerContentUri[b.mTriggerContentUris.size()]);
        }
        this.triggerContentUris = triggerContentUriArr;
        this.triggerContentUpdateDelay = b.mTriggerContentUpdateDelay;
        this.triggerContentMaxDelay = b.mTriggerContentMaxDelay;
        this.networkType = b.mNetworkType;
        this.minLatencyMillis = b.mMinLatencyMillis;
        this.maxExecutionDelayMillis = b.mMaxExecutionDelayMillis;
        this.isPeriodic = b.mIsPeriodic;
        this.isPersisted = b.mIsPersisted;
        this.intervalMillis = b.mIntervalMillis;
        this.flexMillis = b.mFlexMillis;
        this.initialBackoffMillis = b.mInitialBackoffMillis;
        this.backoffPolicy = b.mBackoffPolicy;
        this.hasEarlyConstraint = b.mHasEarlyConstraint;
        this.hasLateConstraint = b.mHasLateConstraint;
        this.priority = b.mPriority;
        this.flags = b.mFlags;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = 1;
        out.writeInt(this.jobId);
        out.writePersistableBundle(this.extras);
        out.writeBundle(this.transientExtras);
        if (this.clipData != null) {
            out.writeInt(1);
            this.clipData.writeToParcel(out, flags);
            out.writeInt(this.clipGrantFlags);
        } else {
            out.writeInt(0);
        }
        out.writeParcelable(this.service, flags);
        out.writeInt(this.constraintFlags);
        out.writeTypedArray(this.triggerContentUris, flags);
        out.writeLong(this.triggerContentUpdateDelay);
        out.writeLong(this.triggerContentMaxDelay);
        out.writeInt(this.networkType);
        out.writeLong(this.minLatencyMillis);
        out.writeLong(this.maxExecutionDelayMillis);
        if (this.isPeriodic) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (this.isPersisted) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        out.writeLong(this.intervalMillis);
        out.writeLong(this.flexMillis);
        out.writeLong(this.initialBackoffMillis);
        out.writeInt(this.backoffPolicy);
        if (this.hasEarlyConstraint) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (!this.hasLateConstraint) {
            i2 = 0;
        }
        out.writeInt(i2);
        out.writeInt(this.priority);
        out.writeInt(this.flags);
    }

    public String toString() {
        return "(job:" + this.jobId + "/" + this.service.flattenToShortString() + ")";
    }
}
