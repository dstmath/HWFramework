package com.android.server.timezone;

final class PackageVersions {
    final long mDataAppVersion;
    final long mUpdateAppVersion;

    PackageVersions(long updateAppVersion, long dataAppVersion) {
        this.mUpdateAppVersion = updateAppVersion;
        this.mDataAppVersion = dataAppVersion;
    }

    public boolean equals(Object o) {
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
        if (this.mDataAppVersion == that.mDataAppVersion) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (Long.hashCode(this.mUpdateAppVersion) * 31) + Long.hashCode(this.mDataAppVersion);
    }

    public String toString() {
        return "PackageVersions{mUpdateAppVersion=" + this.mUpdateAppVersion + ", mDataAppVersion=" + this.mDataAppVersion + '}';
    }
}
