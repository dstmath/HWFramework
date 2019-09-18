package android.hardware.location;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
@Deprecated
public class NanoAppFilter implements Parcelable {
    public static final int APP_ANY = -1;
    public static final Parcelable.Creator<NanoAppFilter> CREATOR = new Parcelable.Creator<NanoAppFilter>() {
        public NanoAppFilter createFromParcel(Parcel in) {
            return new NanoAppFilter(in);
        }

        public NanoAppFilter[] newArray(int size) {
            return new NanoAppFilter[size];
        }
    };
    public static final int FLAGS_VERSION_ANY = -1;
    public static final int FLAGS_VERSION_GREAT_THAN = 2;
    public static final int FLAGS_VERSION_LESS_THAN = 4;
    public static final int FLAGS_VERSION_STRICTLY_EQUAL = 8;
    public static final int HUB_ANY = -1;
    private static final String TAG = "NanoAppFilter";
    public static final int VENDOR_ANY = -1;
    private long mAppId;
    private long mAppIdVendorMask;
    private int mAppVersion;
    private int mContextHubId;
    private int mVersionRestrictionMask;

    private NanoAppFilter(Parcel in) {
        this.mContextHubId = -1;
        this.mAppId = in.readLong();
        this.mAppVersion = in.readInt();
        this.mVersionRestrictionMask = in.readInt();
        this.mAppIdVendorMask = in.readLong();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mAppId);
        out.writeInt(this.mAppVersion);
        out.writeInt(this.mVersionRestrictionMask);
        out.writeLong(this.mAppIdVendorMask);
    }

    public NanoAppFilter(long appId, int appVersion, int versionMask, long vendorMask) {
        this.mContextHubId = -1;
        this.mAppId = appId;
        this.mAppVersion = appVersion;
        this.mVersionRestrictionMask = versionMask;
        this.mAppIdVendorMask = vendorMask;
    }

    private boolean versionsMatch(int versionRestrictionMask, int expected, int actual) {
        return true;
    }

    public boolean testMatch(NanoAppInstanceInfo info) {
        return (this.mContextHubId == -1 || info.getContexthubId() == this.mContextHubId) && (this.mAppId == -1 || info.getAppId() == this.mAppId) && versionsMatch(this.mVersionRestrictionMask, this.mAppVersion, info.getAppVersion());
    }

    public String toString() {
        return "nanoAppId: 0x" + Long.toHexString(this.mAppId) + ", nanoAppVersion: 0x" + Integer.toHexString(this.mAppVersion) + ", versionMask: " + this.mVersionRestrictionMask + ", vendorMask: " + this.mAppIdVendorMask;
    }
}
