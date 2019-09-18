package android.emcom;

import android.os.Parcel;
import android.os.Parcelable;
import huawei.android.hwcolorpicker.HwColorPicker;

public class VideoInfo implements Parcelable {
    public static final Parcelable.Creator<VideoInfo> CREATOR = new Parcelable.Creator<VideoInfo>() {
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

    public VideoInfo() {
    }

    public VideoInfo(Parcel in) {
        this.appName = in.readString();
        this.srDelay = in.readInt();
        this.fullDelay = in.readInt();
        this.times = (short) (in.readInt() & HwColorPicker.MASK_RESULT_INDEX);
        this.totalLen = in.readInt();
        this.streamDur = in.readInt();
        this.videoDataRate = in.readInt();
        this.videoTerminateFlag = (byte) in.readInt();
        this.uVMos = (byte) in.readInt();
        this.hostName = in.readString();
        this.videoStartDate = in.readInt();
        this.videoStartTime = in.readInt();
        this.videoEndTime = in.readInt();
        this.result = in.readInt() != 0;
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
        this.result = in.readInt() != 0;
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
        return "hash: " + hashCode() + ",app: " + this.appName + ",srDelay: " + this.srDelay + ",fullDelay: " + this.fullDelay + ",times: " + this.times + ",totalLen: " + this.totalLen + ",streamDur: " + this.streamDur + ",videoDataRate: " + this.videoDataRate + ",videoTerminateFlag: " + this.videoTerminateFlag + ",uVMos: " + this.uVMos + ",videoStartDate: " + this.videoStartDate + ",videoStartTime: " + this.videoStartTime + ",videoEndTime: " + this.videoEndTime + ",result: " + this.result;
    }

    public void recycle() {
        this.appName = "";
        this.srDelay = -1;
        this.fullDelay = -1;
        this.times = -1;
        this.totalLen = -1;
        this.streamDur = -1;
        this.videoDataRate = -1;
        this.videoTerminateFlag = 1;
        this.uVMos = 0;
        this.hostName = "";
        this.videoStartDate = 0;
        this.videoStartTime = 0;
        this.videoEndTime = 0;
        this.result = true;
    }
}
