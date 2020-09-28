package android.app.timezone;

import android.os.Parcel;
import android.os.Parcelable;

public final class DistroFormatVersion implements Parcelable {
    public static final Parcelable.Creator<DistroFormatVersion> CREATOR = new Parcelable.Creator<DistroFormatVersion>() {
        /* class android.app.timezone.DistroFormatVersion.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DistroFormatVersion createFromParcel(Parcel in) {
            return new DistroFormatVersion(in.readInt(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public DistroFormatVersion[] newArray(int size) {
            return new DistroFormatVersion[size];
        }
    };
    private final int mMajorVersion;
    private final int mMinorVersion;

    public DistroFormatVersion(int majorVersion, int minorVersion) {
        this.mMajorVersion = Utils.validateVersion("major", majorVersion);
        this.mMinorVersion = Utils.validateVersion("minor", minorVersion);
    }

    public int getMajorVersion() {
        return this.mMajorVersion;
    }

    public int getMinorVersion() {
        return this.mMinorVersion;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mMajorVersion);
        out.writeInt(this.mMinorVersion);
    }

    public boolean supports(DistroFormatVersion distroFormatVersion) {
        return this.mMajorVersion == distroFormatVersion.mMajorVersion && this.mMinorVersion <= distroFormatVersion.mMinorVersion;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DistroFormatVersion that = (DistroFormatVersion) o;
        if (this.mMajorVersion != that.mMajorVersion) {
            return false;
        }
        if (this.mMinorVersion == that.mMinorVersion) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (this.mMajorVersion * 31) + this.mMinorVersion;
    }

    public String toString() {
        return "DistroFormatVersion{mMajorVersion=" + this.mMajorVersion + ", mMinorVersion=" + this.mMinorVersion + '}';
    }
}
