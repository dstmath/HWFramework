package android.app.timezone;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class RulesState implements Parcelable {
    private static final byte BYTE_FALSE = 0;
    private static final byte BYTE_TRUE = 1;
    public static final Parcelable.Creator<RulesState> CREATOR = new Parcelable.Creator<RulesState>() {
        /* class android.app.timezone.RulesState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RulesState createFromParcel(Parcel in) {
            return RulesState.createFromParcel(in);
        }

        @Override // android.os.Parcelable.Creator
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
    private final String mBaseRulesVersion;
    private final DistroFormatVersion mDistroFormatVersionSupported;
    private final int mDistroStatus;
    private final DistroRulesVersion mInstalledDistroRulesVersion;
    private final boolean mOperationInProgress;
    private final DistroRulesVersion mStagedDistroRulesVersion;
    private final int mStagedOperationType;

    @Retention(RetentionPolicy.SOURCE)
    private @interface DistroStatus {
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface StagedOperationType {
    }

    public RulesState(String baseRulesVersion, DistroFormatVersion distroFormatVersionSupported, boolean operationInProgress, int stagedOperationType, DistroRulesVersion stagedDistroRulesVersion, int distroStatus, DistroRulesVersion installedDistroRulesVersion) {
        this.mBaseRulesVersion = Utils.validateRulesVersion("baseRulesVersion", baseRulesVersion);
        this.mDistroFormatVersionSupported = (DistroFormatVersion) Utils.validateNotNull("distroFormatVersionSupported", distroFormatVersionSupported);
        this.mOperationInProgress = operationInProgress;
        if (!operationInProgress || stagedOperationType == 0) {
            this.mStagedOperationType = validateStagedOperation(stagedOperationType);
            boolean z = true;
            this.mStagedDistroRulesVersion = (DistroRulesVersion) Utils.validateConditionalNull(this.mStagedOperationType == 3, "stagedDistroRulesVersion", stagedDistroRulesVersion);
            this.mDistroStatus = validateDistroStatus(distroStatus);
            this.mInstalledDistroRulesVersion = (DistroRulesVersion) Utils.validateConditionalNull(this.mDistroStatus != 2 ? false : z, "installedDistroRulesVersion", installedDistroRulesVersion);
            return;
        }
        throw new IllegalArgumentException("stagedOperationType != STAGED_OPERATION_UNKNOWN");
    }

    public String getBaseRulesVersion() {
        return this.mBaseRulesVersion;
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

    public boolean isBaseVersionNewerThan(DistroRulesVersion distroRulesVersion) {
        return this.mBaseRulesVersion.compareTo(distroRulesVersion.getRulesVersion()) > 0;
    }

    /* access modifiers changed from: private */
    public static RulesState createFromParcel(Parcel in) {
        return new RulesState(in.readString(), (DistroFormatVersion) in.readParcelable(null), in.readByte() == 1, in.readByte(), (DistroRulesVersion) in.readParcelable(null), in.readByte(), (DistroRulesVersion) in.readParcelable(null));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mBaseRulesVersion);
        out.writeParcelable(this.mDistroFormatVersionSupported, 0);
        out.writeByte(this.mOperationInProgress ? (byte) 1 : 0);
        out.writeByte((byte) this.mStagedOperationType);
        out.writeParcelable(this.mStagedDistroRulesVersion, 0);
        out.writeByte((byte) this.mDistroStatus);
        out.writeParcelable(this.mInstalledDistroRulesVersion, 0);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RulesState that = (RulesState) o;
        if (this.mOperationInProgress != that.mOperationInProgress || this.mStagedOperationType != that.mStagedOperationType || this.mDistroStatus != that.mDistroStatus || !this.mBaseRulesVersion.equals(that.mBaseRulesVersion) || !this.mDistroFormatVersionSupported.equals(that.mDistroFormatVersionSupported)) {
            return false;
        }
        DistroRulesVersion distroRulesVersion = this.mStagedDistroRulesVersion;
        if (distroRulesVersion == null ? that.mStagedDistroRulesVersion != null : !distroRulesVersion.equals(that.mStagedDistroRulesVersion)) {
            return false;
        }
        DistroRulesVersion distroRulesVersion2 = this.mInstalledDistroRulesVersion;
        if (distroRulesVersion2 != null) {
            return distroRulesVersion2.equals(that.mInstalledDistroRulesVersion);
        }
        if (that.mInstalledDistroRulesVersion == null) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int i;
        int result = ((((((this.mBaseRulesVersion.hashCode() * 31) + this.mDistroFormatVersionSupported.hashCode()) * 31) + (this.mOperationInProgress ? 1 : 0)) * 31) + this.mStagedOperationType) * 31;
        DistroRulesVersion distroRulesVersion = this.mStagedDistroRulesVersion;
        int i2 = 0;
        if (distroRulesVersion != null) {
            i = distroRulesVersion.hashCode();
        } else {
            i = 0;
        }
        int result2 = (((result + i) * 31) + this.mDistroStatus) * 31;
        DistroRulesVersion distroRulesVersion2 = this.mInstalledDistroRulesVersion;
        if (distroRulesVersion2 != null) {
            i2 = distroRulesVersion2.hashCode();
        }
        return result2 + i2;
    }

    public String toString() {
        return "RulesState{mBaseRulesVersion='" + this.mBaseRulesVersion + DateFormat.QUOTE + ", mDistroFormatVersionSupported=" + this.mDistroFormatVersionSupported + ", mOperationInProgress=" + this.mOperationInProgress + ", mStagedOperationType=" + this.mStagedOperationType + ", mStagedDistroRulesVersion=" + this.mStagedDistroRulesVersion + ", mDistroStatus=" + this.mDistroStatus + ", mInstalledDistroRulesVersion=" + this.mInstalledDistroRulesVersion + '}';
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
