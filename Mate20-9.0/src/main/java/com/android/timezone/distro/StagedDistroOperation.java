package com.android.timezone.distro;

public class StagedDistroOperation {
    private static final StagedDistroOperation UNINSTALL_STAGED = new StagedDistroOperation(true, null);
    public final DistroVersion distroVersion;
    public final boolean isUninstall;

    private StagedDistroOperation(boolean isUninstall2, DistroVersion distroVersion2) {
        this.isUninstall = isUninstall2;
        this.distroVersion = distroVersion2;
    }

    public static StagedDistroOperation install(DistroVersion distroVersion2) {
        if (distroVersion2 != null) {
            return new StagedDistroOperation(false, distroVersion2);
        }
        throw new NullPointerException("distroVersion==null");
    }

    public static StagedDistroOperation uninstall() {
        return UNINSTALL_STAGED;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StagedDistroOperation that = (StagedDistroOperation) o;
        if (this.isUninstall != that.isUninstall) {
            return false;
        }
        if (this.distroVersion != null) {
            z = this.distroVersion.equals(that.distroVersion);
        } else if (that.distroVersion != null) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (true * ((int) this.isUninstall)) + (this.distroVersion != null ? this.distroVersion.hashCode() : 0);
    }

    public String toString() {
        return "StagedDistroOperation{isUninstall=" + this.isUninstall + ", distroVersion=" + this.distroVersion + '}';
    }
}
