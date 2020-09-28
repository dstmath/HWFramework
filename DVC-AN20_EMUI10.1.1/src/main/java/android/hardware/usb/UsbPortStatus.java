package android.hardware.usb;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.annotations.Immutable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SystemApi
@Immutable
public final class UsbPortStatus implements Parcelable {
    public static final int CONTAMINANT_DETECTION_DETECTED = 3;
    public static final int CONTAMINANT_DETECTION_DISABLED = 1;
    public static final int CONTAMINANT_DETECTION_NOT_DETECTED = 2;
    public static final int CONTAMINANT_DETECTION_NOT_SUPPORTED = 0;
    public static final int CONTAMINANT_PROTECTION_DISABLED = 8;
    public static final int CONTAMINANT_PROTECTION_FORCE_DISABLE = 4;
    public static final int CONTAMINANT_PROTECTION_NONE = 0;
    public static final int CONTAMINANT_PROTECTION_SINK = 1;
    public static final int CONTAMINANT_PROTECTION_SOURCE = 2;
    public static final Parcelable.Creator<UsbPortStatus> CREATOR = new Parcelable.Creator<UsbPortStatus>() {
        /* class android.hardware.usb.UsbPortStatus.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UsbPortStatus createFromParcel(Parcel in) {
            return new UsbPortStatus(in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public UsbPortStatus[] newArray(int size) {
            return new UsbPortStatus[size];
        }
    };
    public static final int DATA_ROLE_DEVICE = 2;
    public static final int DATA_ROLE_HOST = 1;
    public static final int DATA_ROLE_NONE = 0;
    public static final int MODE_AUDIO_ACCESSORY = 4;
    public static final int MODE_DEBUG_ACCESSORY = 8;
    public static final int MODE_DFP = 2;
    public static final int MODE_DUAL = 3;
    public static final int MODE_NONE = 0;
    public static final int MODE_UFP = 1;
    public static final int POWER_ROLE_NONE = 0;
    public static final int POWER_ROLE_SINK = 2;
    public static final int POWER_ROLE_SOURCE = 1;
    private final int mContaminantDetectionStatus;
    private final int mContaminantProtectionStatus;
    private final int mCurrentDataRole;
    private final int mCurrentMode;
    private final int mCurrentPowerRole;
    private final int mSupportedRoleCombinations;

    @Retention(RetentionPolicy.SOURCE)
    @interface ContaminantDetectionStatus {
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface ContaminantProtectionStatus {
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface UsbDataRole {
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface UsbPortMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface UsbPowerRole {
    }

    public UsbPortStatus(int currentMode, int currentPowerRole, int currentDataRole, int supportedRoleCombinations, int contaminantProtectionStatus, int contaminantDetectionStatus) {
        this.mCurrentMode = currentMode;
        this.mCurrentPowerRole = currentPowerRole;
        this.mCurrentDataRole = currentDataRole;
        this.mSupportedRoleCombinations = supportedRoleCombinations;
        this.mContaminantProtectionStatus = contaminantProtectionStatus;
        this.mContaminantDetectionStatus = contaminantDetectionStatus;
    }

    public boolean isConnected() {
        return this.mCurrentMode != 0;
    }

    public int getCurrentMode() {
        return this.mCurrentMode;
    }

    public int getCurrentPowerRole() {
        return this.mCurrentPowerRole;
    }

    public int getCurrentDataRole() {
        return this.mCurrentDataRole;
    }

    public boolean isRoleCombinationSupported(int powerRole, int dataRole) {
        return (this.mSupportedRoleCombinations & UsbPort.combineRolesAsBit(powerRole, dataRole)) != 0;
    }

    public int getSupportedRoleCombinations() {
        return this.mSupportedRoleCombinations;
    }

    public int getContaminantDetectionStatus() {
        return this.mContaminantDetectionStatus;
    }

    public int getContaminantProtectionStatus() {
        return this.mContaminantProtectionStatus;
    }

    public String toString() {
        return "UsbPortStatus{connected=" + isConnected() + ", currentMode=" + UsbPort.modeToString(this.mCurrentMode) + ", currentPowerRole=" + UsbPort.powerRoleToString(this.mCurrentPowerRole) + ", currentDataRole=" + UsbPort.dataRoleToString(this.mCurrentDataRole) + ", supportedRoleCombinations=" + UsbPort.roleCombinationsToString(this.mSupportedRoleCombinations) + ", contaminantDetectionStatus=" + getContaminantDetectionStatus() + ", contaminantProtectionStatus=" + getContaminantProtectionStatus() + "}";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCurrentMode);
        dest.writeInt(this.mCurrentPowerRole);
        dest.writeInt(this.mCurrentDataRole);
        dest.writeInt(this.mSupportedRoleCombinations);
        dest.writeInt(this.mContaminantProtectionStatus);
        dest.writeInt(this.mContaminantDetectionStatus);
    }
}
