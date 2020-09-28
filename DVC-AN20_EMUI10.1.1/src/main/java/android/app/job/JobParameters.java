package android.app.job;

import android.annotation.UnsupportedAppUsage;
import android.app.job.IJobCallback;
import android.content.ClipData;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.os.RemoteException;
import com.android.internal.location.GpsNetInitiatedHandler;

public class JobParameters implements Parcelable {
    public static final Parcelable.Creator<JobParameters> CREATOR = new Parcelable.Creator<JobParameters>() {
        /* class android.app.job.JobParameters.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public JobParameters createFromParcel(Parcel in) {
            return new JobParameters(in);
        }

        @Override // android.os.Parcelable.Creator
        public JobParameters[] newArray(int size) {
            return new JobParameters[size];
        }
    };
    public static final int REASON_CANCELED = 0;
    public static final int REASON_CONSTRAINTS_NOT_SATISFIED = 1;
    public static final int REASON_DEVICE_IDLE = 4;
    public static final int REASON_DEVICE_THERMAL = 5;
    public static final int REASON_PREEMPT = 2;
    public static final int REASON_TIMEOUT = 3;
    @UnsupportedAppUsage
    private final IBinder callback;
    private final ClipData clipData;
    private final int clipGrantFlags;
    private String debugStopReason;
    private final PersistableBundle extras;
    @UnsupportedAppUsage
    private final int jobId;
    private final String[] mTriggeredContentAuthorities;
    private final Uri[] mTriggeredContentUris;
    private final Network network;
    private final boolean overrideDeadlineExpired;
    private int stopReason;
    private final Bundle transientExtras;

    public static String getReasonName(int reason) {
        if (reason == 0) {
            return "canceled";
        }
        if (reason == 1) {
            return "constraints";
        }
        if (reason == 2) {
            return "preempt";
        }
        if (reason == 3) {
            return GpsNetInitiatedHandler.NI_INTENT_KEY_TIMEOUT;
        }
        if (reason == 4) {
            return "device_idle";
        }
        return "unknown:" + reason;
    }

    public JobParameters(IBinder callback2, int jobId2, PersistableBundle extras2, Bundle transientExtras2, ClipData clipData2, int clipGrantFlags2, boolean overrideDeadlineExpired2, Uri[] triggeredContentUris, String[] triggeredContentAuthorities, Network network2) {
        this.jobId = jobId2;
        this.extras = extras2;
        this.transientExtras = transientExtras2;
        this.clipData = clipData2;
        this.clipGrantFlags = clipGrantFlags2;
        this.callback = callback2;
        this.overrideDeadlineExpired = overrideDeadlineExpired2;
        this.mTriggeredContentUris = triggeredContentUris;
        this.mTriggeredContentAuthorities = triggeredContentAuthorities;
        this.network = network2;
    }

    public int getJobId() {
        return this.jobId;
    }

    public int getStopReason() {
        return this.stopReason;
    }

    public String getDebugStopReason() {
        return this.debugStopReason;
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

    public Network getNetwork() {
        return this.network;
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

    @UnsupportedAppUsage
    public IJobCallback getCallback() {
        return IJobCallback.Stub.asInterface(this.callback);
    }

    private JobParameters(Parcel in) {
        this.jobId = in.readInt();
        this.extras = in.readPersistableBundle();
        this.transientExtras = in.readBundle();
        boolean z = false;
        if (in.readInt() != 0) {
            this.clipData = ClipData.CREATOR.createFromParcel(in);
            this.clipGrantFlags = in.readInt();
        } else {
            this.clipData = null;
            this.clipGrantFlags = 0;
        }
        this.callback = in.readStrongBinder();
        this.overrideDeadlineExpired = in.readInt() == 1 ? true : z;
        this.mTriggeredContentUris = (Uri[]) in.createTypedArray(Uri.CREATOR);
        this.mTriggeredContentAuthorities = in.createStringArray();
        if (in.readInt() != 0) {
            this.network = Network.CREATOR.createFromParcel(in);
        } else {
            this.network = null;
        }
        this.stopReason = in.readInt();
        this.debugStopReason = in.readString();
    }

    public void setStopReason(int reason, String debugStopReason2) {
        this.stopReason = reason;
        this.debugStopReason = debugStopReason2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
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
        dest.writeInt(this.overrideDeadlineExpired ? 1 : 0);
        dest.writeTypedArray(this.mTriggeredContentUris, flags);
        dest.writeStringArray(this.mTriggeredContentAuthorities);
        if (this.network != null) {
            dest.writeInt(1);
            this.network.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.stopReason);
        dest.writeString(this.debugStopReason);
    }
}
