package com.android.server.timezone;

final class PackageVersions {
    final long mDataAppVersion;
    final long mUpdateAppVersion;

    PackageVersions(long updateAppVersion, long dataAppVersion) {
        this.mUpdateAppVersion = updateAppVersion;
        this.mDataAppVersion = dataAppVersion;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PackageVersions that = (PackageVersions) o;
        if (this.mUpdateAppVersion != that.mUpdateAppVersion) {
            return false;
        }
        if (this.mDataAppVersion != that.mDataAppVersion) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (31 * Long.hashCode(this.mUpdateAppVersion)) + Long.hashCode(this.mDataAppVersion);
    }

    public String toString() {
        return "PackageVersions{mUpdateAppVersion=" + this.mUpdateAppVersion + ", mDataAppVersion=" + this.mDataAppVersion + '}';
    }
}
