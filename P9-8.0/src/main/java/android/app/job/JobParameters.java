package android.app.job;

import android.app.job.IJobCallback.Stub;
import android.content.ClipData;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PersistableBundle;
import android.os.RemoteException;

public class JobParameters implements Parcelable {
    public static final Creator<JobParameters> CREATOR = new Creator<JobParameters>() {
        public JobParameters createFromParcel(Parcel in) {
            return new JobParameters(in, null);
        }

        public JobParameters[] newArray(int size) {
            return new JobParameters[size];
        }
    };
    public static final int REASON_CANCELED = 0;
    public static final int REASON_CONSTRAINTS_NOT_SATISFIED = 1;
    public static final int REASON_DEVICE_IDLE = 4;
    public static final int REASON_PREEMPT = 2;
    public static final int REASON_TIMEOUT = 3;
    private final IBinder callback;
    private final ClipData clipData;
    private final int clipGrantFlags;
    private final PersistableBundle extras;
    private final int jobId;
    private final String[] mTriggeredContentAuthorities;
    private final Uri[] mTriggeredContentUris;
    private final boolean overrideDeadlineExpired;
    private int stopReason;
    private final Bundle transientExtras;

    /* synthetic */ JobParameters(Parcel in, JobParameters -this1) {
        this(in);
    }

    public JobParameters(IBinder callback, int jobId, PersistableBundle extras, Bundle transientExtras, ClipData clipData, int clipGrantFlags, boolean overrideDeadlineExpired, Uri[] triggeredContentUris, String[] triggeredContentAuthorities) {
        this.jobId = jobId;
        this.extras = extras;
        this.transientExtras = transientExtras;
        this.clipData = clipData;
        this.clipGrantFlags = clipGrantFlags;
        this.callback = callback;
        this.overrideDeadlineExpired = overrideDeadlineExpired;
        this.mTriggeredContentUris = triggeredContentUris;
        this.mTriggeredContentAuthorities = triggeredContentAuthorities;
    }

    public int getJobId() {
        return this.jobId;
    }

    public int getStopReason() {
        return this.stopReason;
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

    public boolean isOverrideDeadlineExpired() {
        return this.overrideDeadlineExpired;
    }

    public Uri[] getTriggeredContentUris() {
        return this.mTriggeredContentUris;
    }

    public String[] getTriggeredContentAuthorities() {
        return this.mTriggeredContentAuthorities;
    }

    public JobWorkItem dequeueWork() {
        try {
            return getCallback().dequeueWork(getJobId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void completeWork(JobWorkItem work) {
        try {
            if (!getCallback().completeWork(getJobId(), work.getWorkId())) {
                throw new IllegalArgumentException("Given work is not active: " + work);
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public IJobCallback getCallback() {
        return Stub.asInterface(this.callback);
    }

    private JobParameters(Parcel in) {
        boolean z;
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
        this.callback = in.readStrongBinder();
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.overrideDeadlineExpired = z;
        this.mTriggeredContentUris = (Uri[]) in.createTypedArray(Uri.CREATOR);
        this.mTriggeredContentAuthorities = in.createStringArray();
        this.stopReason = in.readInt();
    }

    public void setStopReason(int reason) {
        this.stopReason = reason;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        dest.writeInt(this.jobId);
        dest.writePersistableBundle(this.extras);
        dest.writeBundle(this.transientExtras);
        if (this.clipData != null) {
            dest.writeInt(1);
            this.clipData.writeToParcel(dest, flags);
            dest.writeInt(this.clipGrantFlags);
        } else {
            dest.writeInt(0);
        }
        dest.writeStrongBinder(this.callback);
        if (!this.overrideDeadlineExpired) {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeTypedArray(this.mTriggeredContentUris, flags);
        dest.writeStringArray(this.mTriggeredContentAuthorities);
        dest.writeInt(this.stopReason);
    }
}
