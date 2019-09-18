package android.hardware.location;

import android.annotation.SystemApi;
import android.hardware.contexthub.V1_0.ContextHub;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;

@SystemApi
public class ContextHubInfo implements Parcelable {
    public static final Parcelable.Creator<ContextHubInfo> CREATOR = new Parcelable.Creator<ContextHubInfo>() {
        public ContextHubInfo createFromParcel(Parcel in) {
            return new ContextHubInfo(in);
        }

        public ContextHubInfo[] newArray(int size) {
            return new ContextHubInfo[size];
        }
    };
    private byte mChreApiMajorVersion;
    private byte mChreApiMinorVersion;
    private short mChrePatchVersion;
    private long mChrePlatformId;
    private int mId;
    private int mMaxPacketLengthBytes;
    private MemoryRegion[] mMemoryRegions;
    private String mName;
    private float mPeakMips;
    private float mPeakPowerDrawMw;
    private int mPlatformVersion;
    private float mSleepPowerDrawMw;
    private float mStoppedPowerDrawMw;
    private int[] mSupportedSensors;
    private String mToolchain;
    private int mToolchainVersion;
    private String mVendor;

    public ContextHubInfo() {
    }

    public ContextHubInfo(ContextHub contextHub) {
        this.mId = contextHub.hubId;
        this.mName = contextHub.name;
        this.mVendor = contextHub.vendor;
        this.mToolchain = contextHub.toolchain;
        this.mPlatformVersion = contextHub.platformVersion;
        this.mToolchainVersion = contextHub.toolchainVersion;
        this.mPeakMips = contextHub.peakMips;
        this.mStoppedPowerDrawMw = contextHub.stoppedPowerDrawMw;
        this.mSleepPowerDrawMw = contextHub.sleepPowerDrawMw;
        this.mPeakPowerDrawMw = contextHub.peakPowerDrawMw;
        this.mMaxPacketLengthBytes = contextHub.maxSupportedMsgLen;
        this.mChrePlatformId = contextHub.chrePlatformId;
        this.mChreApiMajorVersion = contextHub.chreApiMajorVersion;
        this.mChreApiMinorVersion = contextHub.chreApiMinorVersion;
        this.mChrePatchVersion = contextHub.chrePatchVersion;
        this.mSupportedSensors = new int[0];
        this.mMemoryRegions = new MemoryRegion[0];
    }

    public int getMaxPacketLengthBytes() {
        return this.mMaxPacketLengthBytes;
    }

    public int getId() {
        return this.mId;
    }

    public String getName() {
        return this.mName;
    }

    public String getVendor() {
        return this.mVendor;
    }

    public String getToolchain() {
        return this.mToolchain;
    }

    public int getPlatformVersion() {
        return this.mPlatformVersion;
    }

    public int getStaticSwVersion() {
        return (this.mChreApiMajorVersion << 24) | (this.mChreApiMinorVersion << 16) | this.mChrePatchVersion;
    }

    public int getToolchainVersion() {
        return this.mToolchainVersion;
    }

    public float getPeakMips() {
        return this.mPeakMips;
    }

    public float getStoppedPowerDrawMw() {
        return this.mStoppedPowerDrawMw;
    }

    public float getSleepPowerDrawMw() {
        return this.mSleepPowerDrawMw;
    }

    public float getPeakPowerDrawMw() {
        return this.mPeakPowerDrawMw;
    }

    public int[] getSupportedSensors() {
        return Arrays.copyOf(this.mSupportedSensors, this.mSupportedSensors.length);
    }

    public MemoryRegion[] getMemoryRegions() {
        return (MemoryRegion[]) Arrays.copyOf(this.mMemoryRegions, this.mMemoryRegions.length);
    }

    public long getChrePlatformId() {
        return this.mChrePlatformId;
    }

    public byte getChreApiMajorVersion() {
        return this.mChreApiMajorVersion;
    }

    public byte getChreApiMinorVersion() {
        return this.mChreApiMinorVersion;
    }

    public short getChrePatchVersion() {
        return this.mChrePatchVersion;
    }

    public String toString() {
        String retVal = "" + "ID/handle : " + this.mId;
        String retVal2 = retVal + ", Name : " + this.mName;
        String retVal3 = retVal2 + "\n\tVendor : " + this.mVendor;
        String retVal4 = retVal3 + ", Toolchain : " + this.mToolchain;
        String retVal5 = retVal4 + ", Toolchain version: 0x" + Integer.toHexString(this.mToolchainVersion);
        String retVal6 = retVal5 + "\n\tPlatformVersion : 0x" + Integer.toHexString(this.mPlatformVersion);
        String retVal7 = retVal6 + ", SwVersion : " + this.mChreApiMajorVersion + "." + this.mChreApiMinorVersion + "." + this.mChrePatchVersion;
        String retVal8 = retVal7 + ", CHRE platform ID: 0x" + Long.toHexString(this.mChrePlatformId);
        String retVal9 = retVal8 + "\n\tPeakMips : " + this.mPeakMips;
        String retVal10 = retVal9 + ", StoppedPowerDraw : " + this.mStoppedPowerDrawMw + " mW";
        String retVal11 = retVal10 + ", PeakPowerDraw : " + this.mPeakPowerDrawMw + " mW";
        return retVal11 + ", MaxPacketLength : " + this.mMaxPacketLengthBytes + " Bytes";
    }

    private ContextHubInfo(Parcel in) {
        this.mId = in.readInt();
        this.mName = in.readString();
        this.mVendor = in.readString();
        this.mToolchain = in.readString();
        this.mPlatformVersion = in.readInt();
        this.mToolchainVersion = in.readInt();
        this.mPeakMips = in.readFloat();
        this.mStoppedPowerDrawMw = in.readFloat();
        this.mSleepPowerDrawMw = in.readFloat();
        this.mPeakPowerDrawMw = in.readFloat();
        this.mMaxPacketLengthBytes = in.readInt();
        this.mChrePlatformId = in.readLong();
        this.mChreApiMajorVersion = in.readByte();
        this.mChreApiMinorVersion = in.readByte();
        this.mChrePatchVersion = (short) in.readInt();
        this.mSupportedSensors = new int[in.readInt()];
        in.readIntArray(this.mSupportedSensors);
        this.mMemoryRegions = (MemoryRegion[]) in.createTypedArray(MemoryRegion.CREATOR);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mId);
        out.writeString(this.mName);
        out.writeString(this.mVendor);
        out.writeString(this.mToolchain);
        out.writeInt(this.mPlatformVersion);
        out.writeInt(this.mToolchainVersion);
        out.writeFloat(this.mPeakMips);
        out.writeFloat(this.mStoppedPowerDrawMw);
        out.writeFloat(this.mSleepPowerDrawMw);
        out.writeFloat(this.mPeakPowerDrawMw);
        out.writeInt(this.mMaxPacketLengthBytes);
        out.writeLong(this.mChrePlatformId);
        out.writeByte(this.mChreApiMajorVersion);
        out.writeByte(this.mChreApiMinorVersion);
        out.writeInt(this.mChrePatchVersion);
        out.writeInt(this.mSupportedSensors.length);
        out.writeIntArray(this.mSupportedSensors);
        out.writeTypedArray(this.mMemoryRegions, flags);
    }
}
