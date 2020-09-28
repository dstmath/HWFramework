package android.hardware.location;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.android.internal.util.Preconditions;

@SystemApi
@Deprecated
public class NanoApp implements Parcelable {
    public static final Parcelable.Creator<NanoApp> CREATOR = new Parcelable.Creator<NanoApp>() {
        /* class android.hardware.location.NanoApp.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NanoApp createFromParcel(Parcel in) {
            return new NanoApp(in);
        }

        @Override // android.os.Parcelable.Creator
        public NanoApp[] newArray(int size) {
            return new NanoApp[size];
        }
    };
    private final String TAG;
    private final String UNKNOWN;
    private byte[] mAppBinary;
    private long mAppId;
    private boolean mAppIdSet;
    private int mAppVersion;
    private String mName;
    private int mNeededExecMemBytes;
    private int mNeededReadMemBytes;
    private int[] mNeededSensors;
    private int mNeededWriteMemBytes;
    private int[] mOutputEvents;
    private String mPublisher;

    public NanoApp() {
        this(0L, (byte[]) null);
        this.mAppIdSet = false;
    }

    @Deprecated
    public NanoApp(int appId, byte[] appBinary) {
        this.TAG = "NanoApp";
        this.UNKNOWN = "Unknown";
        Log.w("NanoApp", "NanoApp(int, byte[]) is deprecated, please use NanoApp(long, byte[]) instead.");
    }

    public NanoApp(long appId, byte[] appBinary) {
        this.TAG = "NanoApp";
        this.UNKNOWN = "Unknown";
        this.mPublisher = "Unknown";
        this.mName = "Unknown";
        this.mAppId = appId;
        this.mAppIdSet = true;
        this.mAppVersion = 0;
        this.mNeededReadMemBytes = 0;
        this.mNeededWriteMemBytes = 0;
        this.mNeededExecMemBytes = 0;
        this.mNeededSensors = new int[0];
        this.mOutputEvents = new int[0];
        this.mAppBinary = appBinary;
    }

    public void setPublisher(String publisher) {
        this.mPublisher = publisher;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setAppId(long appId) {
        this.mAppId = appId;
        this.mAppIdSet = true;
    }

    public void setAppVersion(int appVersion) {
        this.mAppVersion = appVersion;
    }

    public void setNeededReadMemBytes(int neededReadMemBytes) {
        this.mNeededReadMemBytes = neededReadMemBytes;
    }

    public void setNeededWriteMemBytes(int neededWriteMemBytes) {
        this.mNeededWriteMemBytes = neededWriteMemBytes;
    }

    public void setNeededExecMemBytes(int neededExecMemBytes) {
        this.mNeededExecMemBytes = neededExecMemBytes;
    }

    public void setNeededSensors(int[] neededSensors) {
        Preconditions.checkNotNull(neededSensors, "neededSensors must not be null");
        this.mNeededSensors = neededSensors;
    }

    public void setOutputEvents(int[] outputEvents) {
        Preconditions.checkNotNull(outputEvents, "outputEvents must not be null");
        this.mOutputEvents = outputEvents;
    }

    public void setAppBinary(byte[] appBinary) {
        Preconditions.checkNotNull(appBinary, "appBinary must not be null");
        this.mAppBinary = appBinary;
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

    public byte[] getAppBinary() {
        return this.mAppBinary;
    }

    private NanoApp(Parcel in) {
        this.TAG = "NanoApp";
        this.UNKNOWN = "Unknown";
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
        this.mAppBinary = new byte[in.readInt()];
        in.readByteArray(this.mAppBinary);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        if (this.mAppBinary == null) {
            throw new IllegalStateException("Must set non-null AppBinary for nanoapp " + this.mName);
        } else if (this.mAppIdSet) {
            out.writeString(this.mPublisher);
            out.writeString(this.mName);
            out.writeLong(this.mAppId);
            out.writeInt(this.mAppVersion);
            out.writeInt(this.mNeededReadMemBytes);
            out.writeInt(this.mNeededWriteMemBytes);
            out.writeInt(this.mNeededExecMemBytes);
            out.writeInt(this.mNeededSensors.length);
            out.writeIntArray(this.mNeededSensors);
            out.writeInt(this.mOutputEvents.length);
            out.writeIntArray(this.mOutputEvents);
            out.writeInt(this.mAppBinary.length);
            out.writeByteArray(this.mAppBinary);
        } else {
            throw new IllegalStateException("Must set AppId for nanoapp " + this.mName);
        }
    }

    public String toString() {
        return ((("Id : " + this.mAppId) + ", Version : " + this.mAppVersion) + ", Name : " + this.mName) + ", Publisher : " + this.mPublisher;
    }
}
