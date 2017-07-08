package android.app.job;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PersistableBundle;
import android.util.Log;
import android.util.TimeUtils;
import java.util.ArrayList;
import java.util.Objects;

public class JobInfo implements Parcelable {
    public static final int BACKOFF_POLICY_EXPONENTIAL = 1;
    public static final int BACKOFF_POLICY_LINEAR = 0;
    public static final Creator<JobInfo> CREATOR = null;
    public static final int DEFAULT_BACKOFF_POLICY = 1;
    public static final long DEFAULT_INITIAL_BACKOFF_MILLIS = 30000;
    public static final int FLAG_WILL_BE_FOREGROUND = 1;
    public static final long MAX_BACKOFF_DELAY_MILLIS = 18000000;
    private static final long MIN_FLEX_MILLIS = 300000;
    private static final long MIN_PERIOD_MILLIS = 900000;
    public static final int NETWORK_TYPE_ANY = 1;
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
    private static String TAG;
    private final int backoffPolicy;
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
    private final boolean requireCharging;
    private final boolean requireDeviceIdle;
    private final ComponentName service;
    private final long triggerContentMaxDelay;
    private final long triggerContentUpdateDelay;
    private final TriggerContentUri[] triggerContentUris;

    public static final class Builder {
        private int mBackoffPolicy;
        private boolean mBackoffPolicySet;
        private PersistableBundle mExtras;
        private int mFlags;
        private long mFlexMillis;
        private boolean mHasEarlyConstraint;
        private boolean mHasLateConstraint;
        private long mInitialBackoffMillis;
        private long mIntervalMillis;
        private boolean mIsPeriodic;
        private boolean mIsPersisted;
        private final int mJobId;
        private final ComponentName mJobService;
        private long mMaxExecutionDelayMillis;
        private long mMinLatencyMillis;
        private int mNetworkType;
        private int mPriority;
        private boolean mRequiresCharging;
        private boolean mRequiresDeviceIdle;
        private long mTriggerContentMaxDelay;
        private long mTriggerContentUpdateDelay;
        private ArrayList<TriggerContentUri> mTriggerContentUris;

        public Builder(int jobId, ComponentName jobService) {
            this.mExtras = PersistableBundle.EMPTY;
            this.mPriority = JobInfo.PRIORITY_DEFAULT;
            this.mTriggerContentUpdateDelay = -1;
            this.mTriggerContentMaxDelay = -1;
            this.mInitialBackoffMillis = JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS;
            this.mBackoffPolicy = JobInfo.NETWORK_TYPE_ANY;
            this.mBackoffPolicySet = false;
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

        public Builder setRequiredNetworkType(int networkType) {
            this.mNetworkType = networkType;
            return this;
        }

        public Builder setRequiresCharging(boolean requiresCharging) {
            this.mRequiresCharging = requiresCharging;
            return this;
        }

        public Builder setRequiresDeviceIdle(boolean requiresDeviceIdle) {
            this.mRequiresDeviceIdle = requiresDeviceIdle;
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
            if (this.mHasEarlyConstraint || this.mHasLateConstraint || this.mRequiresCharging || this.mRequiresDeviceIdle || this.mNetworkType != 0 || this.mTriggerContentUris != null) {
                this.mExtras = new PersistableBundle(this.mExtras);
                if (this.mIsPeriodic && this.mMaxExecutionDelayMillis != 0) {
                    throw new IllegalArgumentException("Can't call setOverrideDeadline() on a periodic job.");
                } else if (this.mIsPeriodic && this.mMinLatencyMillis != 0) {
                    throw new IllegalArgumentException("Can't call setMinimumLatency() on a periodic job");
                } else if (this.mIsPeriodic && this.mTriggerContentUris != null) {
                    throw new IllegalArgumentException("Can't call addTriggerContentUri() on a periodic job");
                } else if (this.mIsPersisted && this.mTriggerContentUris != null) {
                    throw new IllegalArgumentException("Can't call addTriggerContentUri() on a persisted job");
                } else if (this.mBackoffPolicySet && this.mRequiresDeviceIdle) {
                    throw new IllegalArgumentException("An idle mode job will not respect any back-off policy, so calling setBackoffCriteria with setRequiresDeviceIdle is an error.");
                } else {
                    JobInfo job = new JobInfo();
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
            }
            throw new IllegalArgumentException("You're trying to build a job with no constraints, this is not allowed.");
        }
    }

    public static final class TriggerContentUri implements Parcelable {
        public static final Creator<TriggerContentUri> CREATOR = null;
        public static final int FLAG_NOTIFY_FOR_DESCENDANTS = 1;
        private final int mFlags;
        private final Uri mUri;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.job.JobInfo.TriggerContentUri.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.job.JobInfo.TriggerContentUri.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.app.job.JobInfo.TriggerContentUri.<clinit>():void");
        }

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
            return (this.mUri == null ? JobInfo.PRIORITY_DEFAULT : this.mUri.hashCode()) ^ this.mFlags;
        }

        private TriggerContentUri(Parcel in) {
            this.mUri = (Uri) Uri.CREATOR.createFromParcel(in);
            this.mFlags = in.readInt();
        }

        public int describeContents() {
            return JobInfo.PRIORITY_DEFAULT;
        }

        public void writeToParcel(Parcel out, int flags) {
            this.mUri.writeToParcel(out, flags);
            out.writeInt(this.mFlags);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.job.JobInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.job.JobInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.job.JobInfo.<clinit>():void");
    }

    /* synthetic */ JobInfo(Builder b, JobInfo jobInfo) {
        this(b);
    }

    /* synthetic */ JobInfo(Parcel in, JobInfo jobInfo) {
        this(in);
    }

    public static final long getMinPeriodMillis() {
        return MIN_PERIOD_MILLIS;
    }

    public static final long getMinFlexMillis() {
        return MIN_FLEX_MILLIS;
    }

    public int getId() {
        return this.jobId;
    }

    public PersistableBundle getExtras() {
        return this.extras;
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
        return this.requireCharging;
    }

    public boolean isRequireDeviceIdle() {
        return this.requireDeviceIdle;
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
        return this.intervalMillis >= getMinPeriodMillis() ? this.intervalMillis : getMinPeriodMillis();
    }

    public long getFlexMillis() {
        long interval = getIntervalMillis();
        long clampedFlex = Math.max(this.flexMillis, Math.max((5 * interval) / 100, getMinFlexMillis()));
        return clampedFlex <= interval ? clampedFlex : interval;
    }

    public long getInitialBackoffMillis() {
        return this.initialBackoffMillis;
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

    private JobInfo(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.jobId = in.readInt();
        this.extras = in.readPersistableBundle();
        this.service = (ComponentName) in.readParcelable(null);
        if (in.readInt() == NETWORK_TYPE_ANY) {
            z = true;
        } else {
            z = false;
        }
        this.requireCharging = z;
        if (in.readInt() == NETWORK_TYPE_ANY) {
            z = true;
        } else {
            z = false;
        }
        this.requireDeviceIdle = z;
        this.triggerContentUris = (TriggerContentUri[]) in.createTypedArray(TriggerContentUri.CREATOR);
        this.triggerContentUpdateDelay = in.readLong();
        this.triggerContentMaxDelay = in.readLong();
        this.networkType = in.readInt();
        this.minLatencyMillis = in.readLong();
        this.maxExecutionDelayMillis = in.readLong();
        if (in.readInt() == NETWORK_TYPE_ANY) {
            z = true;
        } else {
            z = false;
        }
        this.isPeriodic = z;
        if (in.readInt() == NETWORK_TYPE_ANY) {
            z = true;
        } else {
            z = false;
        }
        this.isPersisted = z;
        this.intervalMillis = in.readLong();
        this.flexMillis = in.readLong();
        this.initialBackoffMillis = in.readLong();
        this.backoffPolicy = in.readInt();
        if (in.readInt() == NETWORK_TYPE_ANY) {
            z = true;
        } else {
            z = false;
        }
        this.hasEarlyConstraint = z;
        if (in.readInt() != NETWORK_TYPE_ANY) {
            z2 = false;
        }
        this.hasLateConstraint = z2;
        this.priority = in.readInt();
        this.flags = in.readInt();
    }

    private JobInfo(Builder b) {
        TriggerContentUri[] triggerContentUriArr = null;
        this.jobId = b.mJobId;
        this.extras = b.mExtras;
        this.service = b.mJobService;
        this.requireCharging = b.mRequiresCharging;
        this.requireDeviceIdle = b.mRequiresDeviceIdle;
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
        return PRIORITY_DEFAULT;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = NETWORK_TYPE_ANY;
        out.writeInt(this.jobId);
        out.writePersistableBundle(this.extras);
        out.writeParcelable(this.service, flags);
        if (this.requireCharging) {
            i = NETWORK_TYPE_ANY;
        } else {
            i = PRIORITY_DEFAULT;
        }
        out.writeInt(i);
        if (this.requireDeviceIdle) {
            i = NETWORK_TYPE_ANY;
        } else {
            i = PRIORITY_DEFAULT;
        }
        out.writeInt(i);
        out.writeTypedArray(this.triggerContentUris, flags);
        out.writeLong(this.triggerContentUpdateDelay);
        out.writeLong(this.triggerContentMaxDelay);
        out.writeInt(this.networkType);
        out.writeLong(this.minLatencyMillis);
        out.writeLong(this.maxExecutionDelayMillis);
        if (this.isPeriodic) {
            i = NETWORK_TYPE_ANY;
        } else {
            i = PRIORITY_DEFAULT;
        }
        out.writeInt(i);
        if (this.isPersisted) {
            i = NETWORK_TYPE_ANY;
        } else {
            i = PRIORITY_DEFAULT;
        }
        out.writeInt(i);
        out.writeLong(this.intervalMillis);
        out.writeLong(this.flexMillis);
        out.writeLong(this.initialBackoffMillis);
        out.writeInt(this.backoffPolicy);
        if (this.hasEarlyConstraint) {
            i = NETWORK_TYPE_ANY;
        } else {
            i = PRIORITY_DEFAULT;
        }
        out.writeInt(i);
        if (!this.hasLateConstraint) {
            i2 = PRIORITY_DEFAULT;
        }
        out.writeInt(i2);
        out.writeInt(this.priority);
        out.writeInt(this.flags);
    }

    public String toString() {
        return "(job:" + this.jobId + "/" + this.service.flattenToShortString() + ")";
    }
}
