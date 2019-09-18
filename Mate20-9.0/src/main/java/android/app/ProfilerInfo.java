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
        public ProfilerInfo createFromParcel(Parcel in) {
            return new ProfilerInfo(in);
        }

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
        ProfilerInfo profilerInfo = new ProfilerInfo(this.profileFile, this.profileFd, this.samplingInterval, this.autoStopProfiler, this.streamingOutput, agent2, attachAgentDuringBind2);
        return profilerInfo;
    }

    public void closeFd() {
        if (this.profileFd != null) {
            try {
                this.profileFd.close();
            } catch (IOException e) {
                Slog.w(TAG, "Failure closing profile fd", e);
            }
            this.profileFd = null;
        }
    }

    public int describeContents() {
        if (this.profileFd != null) {
            return this.profileFd.describeContents();
        }
        return 0;
    }

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
        if (this.profileFd != null) {
            proto.write(1120986464258L, this.profileFd.getFd());
        }
        proto.write(1120986464259L, this.samplingInterval);
        proto.write(1133871366148L, this.autoStopProfiler);
        proto.write(1133871366149L, this.streamingOutput);
        proto.write(1138166333446L, this.agent);
        proto.end(token);
    }

    private ProfilerInfo(Parcel in) {
        this.profileFile = in.readString();
        this.profileFd = in.readInt() != 0 ? (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(in) : null;
        this.samplingInterval = in.readInt();
        boolean z = false;
        this.autoStopProfiler = in.readInt() != 0;
        this.streamingOutput = in.readInt() != 0 ? true : z;
        this.agent = in.readString();
        this.attachAgentDuringBind = in.readBoolean();
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProfilerInfo other = (ProfilerInfo) o;
        if (!(Objects.equals(this.profileFile, other.profileFile) && this.autoStopProfiler == other.autoStopProfiler && this.samplingInterval == other.samplingInterval && this.streamingOutput == other.streamingOutput && Objects.equals(this.agent, other.agent))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (31 * ((31 * ((31 * ((31 * ((31 * 17) + Objects.hashCode(this.profileFile))) + this.samplingInterval)) + (this.autoStopProfiler ? 1 : 0))) + (this.streamingOutput ? 1 : 0))) + Objects.hashCode(this.agent);
    }
}
