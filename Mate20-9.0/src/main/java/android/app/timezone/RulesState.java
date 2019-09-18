package android.app.timezone;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class RulesState implements Parcelable {
    private static final byte BYTE_FALSE = 0;
    private static final byte BYTE_TRUE = 1;
    public static final Parcelable.Creator<RulesState> CREATOR = new Parcelable.Creator<RulesState>() {
        public RulesState createFromParcel(Parcel in) {
            return RulesState.createFromParcel(in);
        }

        public RulesState[] newArray(int size) {
            return new RulesState[size];
        }
    };
    public static final int DISTRO_STATUS_INSTALLED = 2;
    public static final int DISTRO_STATUS_NONE = 1;
    public static final int DISTRO_STATUS_UNKNOWN = 0;
    public static final int STAGED_OPERATION_INSTALL = 3;
    public static final int STAGED_OPERATION_NONE = 1;
    public static final int STAGED_OPERATION_UNINSTALL = 2;
    public static final int STAGED_OPERATION_UNKNOWN = 0;
    private final DistroFormatVersion mDistroFormatVersionSupported;
    private final int mDistroStatus;
    private final DistroRulesVersion mInstalledDistroRulesVersion;
    private final boolean mOperationInProgress;
    private final DistroRulesVersion mStagedDistroRulesVersion;
    private final int mStagedOperationType;
    private final String mSystemRulesVersion;

    @Retention(RetentionPolicy.SOURCE)
    private @interface DistroStatus {
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface StagedOperationType {
    }

    public RulesState(String systemRulesVersion, DistroFormatVersion distroFormatVersionSupported, boolean operationInProgress, int stagedOperationType, DistroRulesVersion stagedDistroRulesVersion, int distroStatus, DistroRulesVersion installedDistroRulesVersion) {
        this.mSystemRulesVersion = Utils.validateRulesVersion("systemRulesVersion", systemRulesVersion);
        this.mDistroFormatVersionSupported = (DistroFormatVersion) Utils.validateNotNull("distroFormatVersionSupported", distroFormatVersionSupported);
        this.mOperationInProgress = operationInProgress;
        if (!operationInProgress || stagedOperationType == 0) {
            this.mStagedOperationType = validateStagedOperation(stagedOperationType);
            boolean z = false;
            this.mStagedDistroRulesVersion = (DistroRulesVersion) Utils.validateConditionalNull(this.mStagedOperationType == 3, "stagedDistroRulesVersion", stagedDistroRulesVersion);
            this.mDistroStatus = validateDistroStatus(distroStatus);
            this.mInstalledDistroRulesVersion = (DistroRulesVersion) Utils.validateConditionalNull(this.mDistroStatus == 2 ? true : z, "installedDistroRulesVersion", installedDistroRulesVersion);
            return;
        }
        throw new IllegalArgumentException("stagedOperationType != STAGED_OPERATION_UNKNOWN");
    }

    public String getSystemRulesVersion() {
        return this.mSystemRulesVersion;
    }

    public boolean isOperationInProgress() {
        return this.mOperationInProgress;
    }

    public int getStagedOperationType() {
        return this.mStagedOperationType;
    }

    public DistroRulesVersion getStagedDistroRulesVersion() {
        return this.mStagedDistroRulesVersion;
    }

    public int getDistroStatus() {
        return this.mDistroStatus;
    }

    public DistroRulesVersion getInstalledDistroRulesVersion() {
        return this.mInstalledDistroRulesVersion;
    }

    public boolean isDistroFormatVersionSupported(DistroFormatVersion distroFormatVersion) {
        return this.mDistroFormatVersionSupported.supports(distroFormatVersion);
    }

    public boolean isSystemVersionNewerThan(DistroRulesVersion distroRulesVersion) {
        return this.mSystemRulesVersion.compareTo(distroRulesVersion.getRulesVersion()) > 0;
    }

    /* access modifiers changed from: private */
    public static RulesState createFromParcel(Parcel in) {
        RulesState rulesState = new RulesState(in.readString(), (DistroFormatVersion) in.readParcelable(null), in.readByte() == 1, in.readByte(), (DistroRulesVersion) in.readParcelable(null), in.readByte(), (DistroRulesVersion) in.readParcelable(null));
        return rulesState;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mSystemRulesVersion);
        out.writeParcelable(this.mDistroFormatVersionSupported, 0);
        out.writeByte(this.mOperationInProgress ? (byte) 1 : 0);
        out.writeByte((byte) this.mStagedOperationType);
        out.writeParcelable(this.mStagedDistroRulesVersion, 0);
        out.writeByte((byte) this.mDistroStatus);
        out.writeParcelable(this.mInstalledDistroRulesVersion, 0);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RulesState that = (RulesState) o;
        if (this.mOperationInProgress != that.mOperationInProgress || this.mStagedOperationType != that.mStagedOperationType || this.mDistroStatus != that.mDistroStatus || !this.mSystemRulesVersion.equals(that.mSystemRulesVersion) || !this.mDistroFormatVersionSupported.equals(that.mDistroFormatVersionSupported)) {
            return false;
        }
        if (this.mStagedDistroRulesVersion == null ? that.mStagedDistroRulesVersion != null : !this.mStagedDistroRulesVersion.equals(that.mStagedDistroRulesVersion)) {
            return false;
        }
        if (this.mInstalledDistroRulesVersion != null) {
            z = this.mInstalledDistroRulesVersion.equals(that.mInstalledDistroRulesVersion);
        } else if (that.mInstalledDistroRulesVersion != null) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int i;
        int result = 31 * ((31 * ((31 * ((31 * this.mSystemRulesVersion.hashCode()) + this.mDistroFormatVersionSupported.hashCode())) + (this.mOperationInProgress ? 1 : 0))) + this.mStagedOperationType);
        int i2 = 0;
        if (this.mStagedDistroRulesVersion != null) {
            i = this.mStagedDistroRulesVersion.hashCode();
        } else {
            i = 0;
        }
        int i3 = 31 * ((31 * (result + i)) + this.mDistroStatus);
        if (this.mInstalledDistroRulesVersion != null) {
            i2 = this.mInstalledDistroRulesVersion.hashCode();
        }
        return i3 + i2;
    }

    public String toString() {
        return "RulesState{mSystemRulesVersion='" + this.mSystemRulesVersion + '\'' + ", mDistroFormatVersionSupported=" + this.mDistroFormatVersionSupported + ", mOperationInProgress=" + this.mOperationInProgress + ", mStagedOperationType=" + this.mStagedOperationType + ", mStagedDistroRulesVersion=" + this.mStagedDistroRulesVersion + ", mDistroStatus=" + this.mDistroStatus + ", mInstalledDistroRulesVersion=" + this.mInstalledDistroRulesVersion + '}';
    }

    private static int validateStagedOperation(int stagedOperationType) {
        if (stagedOperationType >= 0 && stagedOperationType <= 3) {
            return stagedOperationType;
        }
        throw new IllegalArgumentException("Unknown operation type=" + stagedOperationType);
    }

    private static int validateDistroStatus(int distroStatus) {
        if (distroStatus >= 0 && distroStatus <= 2) {
            return distroStatus;
        }
        throw new IllegalArgumentException("Unknown distro status=" + distroStatus);
    }
}
