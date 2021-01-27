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
        DistroVersion distroVersion2 = this.distroVersion;
        if (distroVersion2 != null) {
            return distroVersion2.equals(that.distroVersion);
        }
        if (that.distroVersion == null) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int i = (this.isUninstall ? 1 : 0) * 31;
        DistroVersion distroVersion2 = this.distroVersion;
        return i + (distroVersion2 != null ? distroVersion2.hashCode() : 0);
    }

    public String toString() {
        return "StagedDistroOperation{isUninstall=" + this.isUninstall + ", distroVersion=" + this.distroVersion + '}';
    }
}
