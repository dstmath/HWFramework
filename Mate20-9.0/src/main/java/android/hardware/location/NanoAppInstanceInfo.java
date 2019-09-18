package android.hardware.location;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import libcore.util.EmptyArray;

@SystemApi
@Deprecated
public class NanoAppInstanceInfo implements Parcelable {
    public static final Parcelable.Creator<NanoAppInstanceInfo> CREATOR = new Parcelable.Creator<NanoAppInstanceInfo>() {
        public NanoAppInstanceInfo createFromParcel(Parcel in) {
            return new NanoAppInstanceInfo(in);
        }

        public NanoAppInstanceInfo[] newArray(int size) {
            return new NanoAppInstanceInfo[size];
        }
    };
    private long mAppId;
    private int mAppVersion;
    private int mContexthubId;
    private int mHandle;
    private String mName;
    private int mNeededExecMemBytes;
    private int mNeededReadMemBytes;
    private int[] mNeededSensors;
    private int mNeededWriteMemBytes;
    private int[] mOutputEvents;
    private String mPublisher;

    public NanoAppInstanceInfo() {
        this.mPublisher = "Unknown";
        this.mName = "Unknown";
        this.mNeededReadMemBytes = 0;
        this.mNeededWriteMemBytes = 0;
        this.mNeededExecMemBytes = 0;
        this.mNeededSensors = EmptyArray.INT;
        this.mOutputEvents = EmptyArray.INT;
    }

    public NanoAppInstanceInfo(int handle, long appId, int appVersion, int contextHubId) {
        this.mPublisher = "Unknown";
        this.mName = "Unknown";
        this.mNeededReadMemBytes = 0;
        this.mNeededWriteMemBytes = 0;
        this.mNeededExecMemBytes = 0;
        this.mNeededSensors = EmptyArray.INT;
        this.mOutputEvents = EmptyArray.INT;
        this.mHandle = handle;
        this.mAppId = appId;
        this.mAppVersion = appVersion;
        this.mContexthubId = contextHubId;
    }

    public String getPublisher() {
        return this.mPublisher;
    }

    public String getName() {
        return this.mName;
    }

    public long getAppId() {
        return this.mAppId;
    }

    public int getAppVersion() {
        return this.mAppVersion;
    }

    public int getNeededReadMemBytes() {
        return this.mNeededReadMemBytes;
    }

    public int getNeededWriteMemBytes() {
        return this.mNeededWriteMemBytes;
    }

    public int getNeededExecMemBytes() {
        return this.mNeededExecMemBytes;
    }

    public int[] getNeededSensors() {
        return this.mNeededSensors;
    }

    public int[] getOutputEvents() {
        return this.mOutputEvents;
    }

    public int getContexthubId() {
        return this.mContexthubId;
    }

    public int getHandle() {
        return this.mHandle;
    }

    private NanoAppInstanceInfo(Parcel in) {
        this.mPublisher = "Unknown";
        this.mName = "Unknown";
        this.mNeededReadMemBytes = 0;
        this.mNeededWriteMemBytes = 0;
        this.mNeededExecMemBytes = 0;
        this.mNeededSensors = EmptyArray.INT;
        this.mOutputEvents = EmptyArray.INT;
        this.mPublisher = in.readString();
        this.mName = in.readString();
        this.mHandle = in.readInt();
        this.mAppId = in.readLong();
        this.mAppVersion = in.readInt();
        this.mContexthubId = in.readInt();
        this.mNeededReadMemBytes = in.readInt();
        this.mNeededWriteMemBytes = in.readInt();
        this.mNeededExecMemBytes = in.readInt();
        this.mNeededSensors = new int[in.readInt()];
        in.readIntArray(this.mNeededSensors);
        this.mOutputEvents = new int[in.readInt()];
        in.readIntArray(this.mOutputEvents);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mPublisher);
        out.writeString(this.mName);
        out.writeInt(this.mHandle);
        out.writeLong(this.mAppId);
        out.writeInt(this.mAppVersion);
        out.writeInt(this.mContexthubId);
        out.writeInt(this.mNeededReadMemBytes);
        out.writeInt(this.mNeededWriteMemBytes);
        out.writeInt(this.mNeededExecMemBytes);
        out.writeInt(this.mNeededSensors.length);
        out.writeIntArray(this.mNeededSensors);
        out.writeInt(this.mOutputEvents.length);
        out.writeIntArray(this.mOutputEvents);
    }

    public String toString() {
        String retVal = "handle : " + this.mHandle;
        String retVal2 = retVal + ", Id : 0x" + Long.toHexString(this.mAppId);
        return retVal2 + ", Version : 0x" + Integer.toHexString(this.mAppVersion);
    }
}
