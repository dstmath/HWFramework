package android.app.job;

import android.app.job.IJobCallback.Stub;
import android.net.Uri;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PersistableBundle;

public class JobParameters implements Parcelable {
    public static final Creator<JobParameters> CREATOR = null;
    public static final int REASON_CANCELED = 0;
    public static final int REASON_CONSTRAINTS_NOT_SATISFIED = 1;
    public static final int REASON_DEVICE_IDLE = 4;
    public static final int REASON_PREEMPT = 2;
    public static final int REASON_TIMEOUT = 3;
    private final IBinder callback;
    private final PersistableBundle extras;
    private final int jobId;
    private final String[] mTriggeredContentAuthorities;
    private final Uri[] mTriggeredContentUris;
    private final boolean overrideDeadlineExpired;
    private int stopReason;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.job.JobParameters.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.job.JobParameters.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.job.JobParameters.<clinit>():void");
    }

    public JobParameters(IBinder callback, int jobId, PersistableBundle extras, boolean overrideDeadlineExpired, Uri[] triggeredContentUris, String[] triggeredContentAuthorities) {
        this.jobId = jobId;
        this.extras = extras;
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

    public boolean isOverrideDeadlineExpired() {
        return this.overrideDeadlineExpired;
    }

    public Uri[] getTriggeredContentUris() {
        return this.mTriggeredContentUris;
    }

    public String[] getTriggeredContentAuthorities() {
        return this.mTriggeredContentAuthorities;
    }

    public IJobCallback getCallback() {
        return Stub.asInterface(this.callback);
    }

    private JobParameters(Parcel in) {
        boolean z = true;
        this.jobId = in.readInt();
        this.extras = in.readPersistableBundle();
        this.callback = in.readStrongBinder();
        if (in.readInt() != REASON_CONSTRAINTS_NOT_SATISFIED) {
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
        return REASON_CANCELED;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.jobId);
        dest.writePersistableBundle(this.extras);
        dest.writeStrongBinder(this.callback);
        dest.writeInt(this.overrideDeadlineExpired ? REASON_CONSTRAINTS_NOT_SATISFIED : REASON_CANCELED);
        dest.writeTypedArray(this.mTriggeredContentUris, flags);
        dest.writeStringArray(this.mTriggeredContentAuthorities);
        dest.writeInt(this.stopReason);
    }
}
