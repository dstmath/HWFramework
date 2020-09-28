package android.app;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ProfilerInfo implements Parcelable {
    public static final Parcelable.Creator<ProfilerInfo> CREATOR = new Parcelable.Creator<ProfilerInfo>() {
        /* class android.app.ProfilerInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ProfilerInfo createFromParcel(Parcel in) {
            return new ProfilerInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public ProfilerInfo[] newArray(int size) {
            return new ProfilerInfo[size];
        }
    };
    private static final String TAG = "ProfilerInfo";
    public final String agent;
    public final boolean attachAgentDuringBind;
    public final boolean autoStopProfiler;
    public ParcelFileDescriptor profileFd;
    public final String profileFile;
    public final int samplingInterval;
    public final boolean streamingOutput;

    public ProfilerInfo(String filename, ParcelFileDescriptor fd, int interval, boolean autoStop, boolean streaming, String agent2, boolean attachAgentDuringBind2) {
        this.profileFile = filename;
        this.profileFd = fd;
        this.samplingInterval = interval;
        this.autoStopProfiler = autoStop;
        this.streamingOutput = streaming;
        this.agent = agent2;
        this.attachAgentDuringBind = attachAgentDuringBind2;
    }

    public ProfilerInfo(ProfilerInfo in) {
        this.profileFile = in.profileFile;
        this.profileFd = in.profileFd;
        this.samplingInterval = in.samplingInterval;
        this.autoStopProfiler = in.autoStopProfiler;
        this.streamingOutput = in.streamingOutput;
        this.agent = in.agent;
        this.attachAgentDuringBind = in.attachAgentDuringBind;
    }

    public ProfilerInfo setAgent(String agent2, boolean attachAgentDuringBind2) {
        return new ProfilerInfo(this.profileFile, this.profileFd, this.samplingInterval, this.autoStopProfiler, this.streamingOutput, agent2, attachAgentDuringBind2);
    }

    public void closeFd() {
        ParcelFileDescriptor parcelFileDescriptor = this.profileFd;
        if (parcelFileDescriptor != null) {
            try {
                parcelFileDescriptor.close();
            } catch (IOException e) {
                Slog.w(TAG, "Failure closing profile fd", e);
            }
            this.profileFd = null;
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        ParcelFileDescriptor parcelFileDescriptor = this.profileFd;
        if (parcelFileDescriptor != null) {
            return parcelFileDescriptor.describeContents();
        }
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.profileFile);
        if (this.profileFd != null) {
            out.writeInt(1);
            this.profileFd.writeToParcel(out, flags);
        } else {
            out.writeInt(0);
        }
        out.writeInt(this.samplingInterval);
        out.writeInt(this.autoStopProfiler ? 1 : 0);
        out.writeInt(this.streamingOutput ? 1 : 0);
        out.writeString(this.agent);
        out.writeBoolean(this.attachAgentDuringBind);
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1138166333441L, this.profileFile);
        ParcelFileDescriptor parcelFileDescriptor = this.profileFd;
        if (parcelFileDescriptor != null) {
            proto.write(1120986464258L, parcelFileDescriptor.getFd());
        }
        proto.write(1120986464259L, this.samplingInterval);
        proto.write(1133871366148L, this.autoStopProfiler);
        proto.write(1133871366149L, this.streamingOutput);
        proto.write(1138166333446L, this.agent);
        proto.end(token);
    }

    private ProfilerInfo(Parcel in) {
        this.profileFile = in.readString();
        this.profileFd = in.readInt() != 0 ? ParcelFileDescriptor.CREATOR.createFromParcel(in) : null;
        this.samplingInterval = in.readInt();
        boolean z = true;
        this.autoStopProfiler = in.readInt() != 0;
        this.streamingOutput = in.readInt() == 0 ? false : z;
        this.agent = in.readString();
        this.attachAgentDuringBind = in.readBoolean();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProfilerInfo other = (ProfilerInfo) o;
        if (Objects.equals(this.profileFile, other.profileFile) && this.autoStopProfiler == other.autoStopProfiler && this.samplingInterval == other.samplingInterval && this.streamingOutput == other.streamingOutput && Objects.equals(this.agent, other.agent)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (((((((((17 * 31) + Objects.hashCode(this.profileFile)) * 31) + this.samplingInterval) * 31) + (this.autoStopProfiler ? 1 : 0)) * 31) + (this.streamingOutput ? 1 : 0)) * 31) + Objects.hashCode(this.agent);
    }
}
