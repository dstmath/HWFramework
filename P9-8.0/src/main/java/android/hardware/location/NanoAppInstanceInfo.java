package android.hardware.location;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import libcore.util.EmptyArray;

public class NanoAppInstanceInfo {
    public static final Creator<NanoAppInstanceInfo> CREATOR = new Creator<NanoAppInstanceInfo>() {
        public NanoAppInstanceInfo createFromParcel(Parcel in) {
            return new NanoAppInstanceInfo(in, null);
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

    /* synthetic */ NanoAppInstanceInfo(Parcel in, NanoAppInstanceInfo -this1) {
        this(in);
    }

    public NanoAppInstanceInfo() {
        this.mNeededSensors = EmptyArray.INT;
        this.mOutputEvents = EmptyArray.INT;
    }

    public String getPublisher() {
        return this.mPublisher;
    }

    public void setPublisher(String publisher) {
        this.mPublisher = publisher;
    }

    public String getName() {
        return this.mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public long getAppId() {
        return this.mAppId;
    }

    public void setAppId(long appId) {
        this.mAppId = appId;
    }

    public int getAppVersion() {
        return this.mAppVersion;
    }

    public void setAppVersion(int appVersion) {
        this.mAppVersion = appVersion;
    }

    public int getNeededReadMemBytes() {
        return this.mNeededReadMemBytes;
    }

    public void setNeededReadMemBytes(int neededReadMemBytes) {
        this.mNeededReadMemBytes = neededReadMemBytes;
    }

    public int getNeededWriteMemBytes() {
        return this.mNeededWriteMemBytes;
    }

    public void setNeededWriteMemBytes(int neededWriteMemBytes) {
        this.mNeededWriteMemBytes = neededWriteMemBytes;
    }

    public int getNeededExecMemBytes() {
        return this.mNeededExecMemBytes;
    }

    public void setNeededExecMemBytes(int neededExecMemBytes) {
        this.mNeededExecMemBytes = neededExecMemBytes;
    }

    public int[] getNeededSensors() {
        return this.mNeededSensors;
    }

    public void setNeededSensors(int[] neededSensors) {
        if (neededSensors == null) {
            neededSensors = EmptyArray.INT;
        }
        this.mNeededSensors = neededSensors;
    }

    public int[] getOutputEvents() {
        return this.mOutputEvents;
    }

    public void setOutputEvents(int[] outputEvents) {
        if (outputEvents == null) {
            outputEvents = EmptyArray.INT;
        }
        this.mOutputEvents = outputEvents;
    }

    public int getContexthubId() {
        return this.mContexthubId;
    }

    public void setContexthubId(int contexthubId) {
        this.mContexthubId = contexthubId;
    }

    public int getHandle() {
        return this.mHandle;
    }

    public void setHandle(int handle) {
        this.mHandle = handle;
    }

    private NanoAppInstanceInfo(Parcel in) {
        this.mPublisher = in.readString();
        this.mName = in.readString();
        this.mAppId = in.readLong();
        this.mAppVersion = in.readInt();
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
        return (((("handle : " + this.mHandle) + ", Id : 0x" + Long.toHexString(this.mAppId)) + ", Version : " + this.mAppVersion) + ", Name : " + this.mName) + ", Publisher : " + this.mPublisher;
    }
}
