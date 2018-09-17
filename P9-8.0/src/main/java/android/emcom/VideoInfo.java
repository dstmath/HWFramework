package android.emcom;

import android.emcom.SmartcareInfos.SmartcareBaseInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class VideoInfo extends SmartcareBaseInfo implements Parcelable {
    public static final Creator<VideoInfo> CREATOR = new Creator<VideoInfo>() {
        public VideoInfo createFromParcel(Parcel in) {
            return new VideoInfo(in);
        }

        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }
    };
    public String appName;
    public int fullDelay;
    public String hostName;
    public boolean result;
    public int srDelay;
    public int streamDur;
    public short times;
    public int totalLen;
    public byte uVMos;
    public int videoDataRate;
    public int videoEndTime;
    public int videoStartDate;
    public int videoStartTime;
    public byte videoTerminateFlag;

    public void addToInfos(SmartcareInfos cis) {
        super.addToInfos(cis);
        cis.videoInfo = this;
    }

    public VideoInfo(Parcel in) {
        boolean z = false;
        this.appName = in.readString();
        this.srDelay = in.readInt();
        this.fullDelay = in.readInt();
        this.times = (short) (in.readInt() & 65535);
        this.totalLen = in.readInt();
        this.streamDur = in.readInt();
        this.videoDataRate = in.readInt();
        this.videoTerminateFlag = (byte) in.readInt();
        this.uVMos = (byte) in.readInt();
        this.hostName = in.readString();
        this.videoStartDate = in.readInt();
        this.videoStartTime = in.readInt();
        this.videoEndTime = in.readInt();
        if (in.readInt() != 0) {
            z = true;
        }
        this.result = z;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.appName);
        dest.writeInt(this.srDelay);
        dest.writeInt(this.fullDelay);
        dest.writeInt(this.times);
        dest.writeInt(this.totalLen);
        dest.writeInt(this.streamDur);
        dest.writeInt(this.videoDataRate);
        dest.writeInt(this.videoTerminateFlag);
        dest.writeInt(this.uVMos);
        dest.writeString(this.hostName);
        dest.writeInt(this.videoStartDate);
        dest.writeInt(this.videoStartTime);
        dest.writeInt(this.videoEndTime);
        dest.writeInt(this.result ? 1 : 0);
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        boolean z = false;
        this.appName = in.readString();
        this.srDelay = (short) in.readInt();
        this.fullDelay = (short) in.readInt();
        this.times = (short) ((byte) (in.readInt() & 255));
        this.totalLen = (short) in.readInt();
        this.streamDur = (short) in.readInt();
        this.videoDataRate = in.readInt();
        this.videoTerminateFlag = (byte) in.readInt();
        this.uVMos = (byte) in.readInt();
        this.hostName = in.readString();
        this.videoStartDate = in.readInt();
        this.videoStartTime = in.readInt();
        this.videoEndTime = in.readInt();
        if (in.readInt() != 0) {
            z = true;
        }
        this.result = z;
    }

    public final void copyFrom(VideoInfo o) {
        this.appName = o.appName;
        this.srDelay = o.srDelay;
        this.fullDelay = o.fullDelay;
        this.times = o.times;
        this.totalLen = o.totalLen;
        this.streamDur = o.streamDur;
        this.videoDataRate = o.videoDataRate;
        this.videoTerminateFlag = o.videoTerminateFlag;
        this.uVMos = o.uVMos;
        this.hostName = o.hostName;
        this.videoStartDate = o.videoStartDate;
        this.videoStartTime = o.videoStartTime;
        this.videoEndTime = o.videoEndTime;
        this.result = o.result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("hash: ");
        sb.append(hashCode());
        sb.append(",app: ");
        sb.append(this.appName);
        sb.append(",srDelay: ");
        sb.append(this.srDelay);
        sb.append(",fullDelay: ");
        sb.append(this.fullDelay);
        sb.append(",times: ");
        sb.append(this.times);
        sb.append(",totalLen: ");
        sb.append(this.totalLen);
        sb.append(",streamDur: ");
        sb.append(this.streamDur);
        sb.append(",videoDataRate: ");
        sb.append(this.videoDataRate);
        sb.append(",videoTerminateFlag: ");
        sb.append(this.videoTerminateFlag);
        sb.append(",uVMos: ");
        sb.append(this.uVMos);
        sb.append(",hostName: ");
        sb.append(this.hostName);
        sb.append(",videoStartDate: ");
        sb.append(this.videoStartDate);
        sb.append(",videoStartTime: ");
        sb.append(this.videoStartTime);
        sb.append(",videoEndTime: ");
        sb.append(this.videoEndTime);
        sb.append(",result: ");
        sb.append(this.result);
        sb.append(",");
        return sb.toString();
    }
}
