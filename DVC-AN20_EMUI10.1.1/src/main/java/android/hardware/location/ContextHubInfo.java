package android.hardware.location;

import android.annotation.SystemApi;
import android.hardware.contexthub.V1_0.ContextHub;
import android.net.wifi.WifiScanner;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;

@SystemApi
public class ContextHubInfo implements Parcelable {
    public static final Parcelable.Creator<ContextHubInfo> CREATOR = new Parcelable.Creator<ContextHubInfo>() {
        /* class android.hardware.location.ContextHubInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ContextHubInfo createFromParcel(Parcel in) {
            return new ContextHubInfo(in);
        }

        @Override // android.os.Parcelable.Creator
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
        this.mVendor = contextHub.f1vendor;
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
        return (this.mChreApiMajorVersion << 24) | (this.mChreApiMinorVersion << WifiScanner.PnoSettings.PnoNetwork.FLAG_SAME_NETWORK) | this.mChrePatchVersion;
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
        int[] iArr = this.mSupportedSensors;
        return Arrays.copyOf(iArr, iArr.length);
    }

    public MemoryRegion[] getMemoryRegions() {
        MemoryRegion[] memoryRegionArr = this.mMemoryRegions;
        return (MemoryRegion[]) Arrays.copyOf(memoryRegionArr, memoryRegionArr.length);
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
        return ((((((((((("" + "ID/handle : " + this.mId) + ", Name : " + this.mName) + "\n\tVendor : " + this.mVendor) + ", Toolchain : " + this.mToolchain) + ", Toolchain version: 0x" + Integer.toHexString(this.mToolchainVersion)) + "\n\tPlatformVersion : 0x" + Integer.toHexString(this.mPlatformVersion)) + ", SwVersion : " + ((int) this.mChreApiMajorVersion) + "." + ((int) this.mChreApiMinorVersion) + "." + ((int) this.mChrePatchVersion)) + ", CHRE platform ID: 0x" + Long.toHexString(this.mChrePlatformId)) + "\n\tPeakMips : " + this.mPeakMips) + ", StoppedPowerDraw : " + this.mStoppedPowerDrawMw + " mW") + ", PeakPowerDraw : " + this.mPeakPowerDrawMw + " mW") + ", MaxPacketLength : " + this.mMaxPacketLengthBytes + " Bytes";
    }

    public boolean equals(Object object) {
        boolean isEqual = true;
        if (object == this) {
            return true;
        }
        if (!(object instanceof ContextHubInfo)) {
            return false;
        }
        ContextHubInfo other = (ContextHubInfo) object;
        if (!(other.getId() == this.mId && other.getName().equals(this.mName) && other.getVendor().equals(this.mVendor) && other.getToolchain().equals(this.mToolchain) && other.getToolchainVersion() == this.mToolchainVersion && other.getStaticSwVersion() == getStaticSwVersion() && other.getChrePlatformId() == this.mChrePlatformId && other.getPeakMips() == this.mPeakMips && other.getStoppedPowerDrawMw() == this.mStoppedPowerDrawMw && other.getSleepPowerDrawMw() == this.mSleepPowerDrawMw && other.getPeakPowerDrawMw() == this.mPeakPowerDrawMw && other.getMaxPacketLengthBytes() == this.mMaxPacketLengthBytes && Arrays.equals(other.getSupportedSensors(), this.mSupportedSensors) && Arrays.equals(other.getMemoryRegions(), this.mMemoryRegions))) {
            isEqual = false;
        }
        return isEqual;
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
