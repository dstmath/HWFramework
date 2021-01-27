package android.emcom;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.android.os.storage.StorageManagerExt;

public class VideoInfo implements Parcelable {
    public static final Parcelable.Creator<VideoInfo> CREATOR = new Parcelable.Creator<VideoInfo>() {
        /* class android.emcom.VideoInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VideoInfo createFromParcel(Parcel pa) {
            return new VideoInfo(pa);
        }

        @Override // android.os.Parcelable.Creator
        public VideoInfo[] newArray(int size) {
            if (size > 0) {
                return new VideoInfo[size];
            }
            return new VideoInfo[0];
        }
    };
    private static final int DUR_DEFAULT = -1;
    private static final byte FLAG_DEFAULT = 1;
    private static final int FULL_DELAY_DEFAULT = -1;
    private static final int LEN_DEFAULT = -1;
    private static final int RATE_DEFAULT = -1;
    private static final int SR_DEFAULT = -1;
    private static final int TIME_AND_FF = 255;
    private static final int TIME_AND_FFFF = 65535;
    private static final short TIME_DEFAULT = -1;
    private static final int WRITE_SUCC = 1;
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

    public VideoInfo(Parcel pa) {
        if (pa != null) {
            this.appName = pa.readString();
            this.srDelay = pa.readInt();
            this.fullDelay = pa.readInt();
            this.times = (short) (pa.readInt() & TIME_AND_FFFF);
            this.totalLen = pa.readInt();
            this.streamDur = pa.readInt();
            this.videoDataRate = pa.readInt();
            this.videoTerminateFlag = (byte) pa.readInt();
            this.uVMos = (byte) pa.readInt();
            this.hostName = pa.readString();
            this.videoStartDate = pa.readInt();
            this.videoStartTime = pa.readInt();
            this.videoEndTime = pa.readInt();
            this.result = pa.readInt() != 0;
        }
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel pa, int flags) {
        if (pa != null) {
            pa.writeString(this.appName);
            pa.writeInt(this.srDelay);
            pa.writeInt(this.fullDelay);
            pa.writeInt(this.times);
            pa.writeInt(this.totalLen);
            pa.writeInt(this.streamDur);
            pa.writeInt(this.videoDataRate);
            pa.writeInt(this.videoTerminateFlag);
            pa.writeInt(this.uVMos);
            pa.writeString(this.hostName);
            pa.writeInt(this.videoStartDate);
            pa.writeInt(this.videoStartTime);
            pa.writeInt(this.videoEndTime);
            if (this.result) {
                pa.writeInt(1);
            } else {
                pa.writeInt(0);
            }
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel pa) {
        if (pa != null) {
            this.appName = pa.readString();
            this.srDelay = (short) pa.readInt();
            this.fullDelay = (short) pa.readInt();
            this.times = (short) ((byte) (pa.readInt() & 255));
            this.totalLen = (short) pa.readInt();
            this.streamDur = (short) pa.readInt();
            this.videoDataRate = pa.readInt();
            this.videoTerminateFlag = (byte) pa.readInt();
            this.uVMos = (byte) pa.readInt();
            this.hostName = pa.readString();
            this.videoStartDate = pa.readInt();
            this.videoStartTime = pa.readInt();
            this.videoEndTime = pa.readInt();
            this.result = pa.readInt() != 0;
        }
    }

    public final void copyFrom(VideoInfo vi) {
        if (vi != null) {
            this.appName = vi.appName;
            this.srDelay = vi.srDelay;
            this.fullDelay = vi.fullDelay;
            this.times = vi.times;
            this.totalLen = vi.totalLen;
            this.streamDur = vi.streamDur;
            this.videoDataRate = vi.videoDataRate;
            this.videoTerminateFlag = vi.videoTerminateFlag;
            this.uVMos = vi.uVMos;
            this.hostName = vi.hostName;
            this.videoStartDate = vi.videoStartDate;
            this.videoStartTime = vi.videoStartTime;
            this.videoEndTime = vi.videoEndTime;
            this.result = vi.result;
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return "hash: " + hashCode() + ",app: " + this.appName + ",srDelay: " + this.srDelay + ",fullDelay: " + this.fullDelay + ",times: " + ((int) this.times) + ",totalLen: " + this.totalLen + ",streamDur: " + this.streamDur + ",videoDataRate: " + this.videoDataRate + ",videoTerminateFlag: " + ((int) this.videoTerminateFlag) + ",uVMos: " + ((int) this.uVMos) + ",videoStartDate: " + this.videoStartDate + ",videoStartTime: " + this.videoStartTime + ",videoEndTime: " + this.videoEndTime + ",result: " + this.result;
    }

    public void recycle() {
        this.appName = StorageManagerExt.INVALID_KEY_DESC;
        this.srDelay = -1;
        this.fullDelay = -1;
        this.times = TIME_DEFAULT;
        this.totalLen = -1;
        this.streamDur = -1;
        this.videoDataRate = -1;
        this.videoTerminateFlag = FLAG_DEFAULT;
        this.uVMos = 0;
        this.hostName = StorageManagerExt.INVALID_KEY_DESC;
        this.videoStartDate = 0;
        this.videoStartTime = 0;
        this.videoEndTime = 0;
        this.result = true;
    }
}
