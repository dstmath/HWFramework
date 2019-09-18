package android.telephony;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

@SystemApi
public class UiccSlotInfo implements Parcelable {
    public static final int CARD_STATE_INFO_ABSENT = 1;
    public static final int CARD_STATE_INFO_ERROR = 3;
    public static final int CARD_STATE_INFO_PRESENT = 2;
    public static final int CARD_STATE_INFO_RESTRICTED = 4;
    public static final Parcelable.Creator<UiccSlotInfo> CREATOR = new Parcelable.Creator<UiccSlotInfo>() {
        public UiccSlotInfo createFromParcel(Parcel in) {
            return new UiccSlotInfo(in);
        }

        public UiccSlotInfo[] newArray(int size) {
            return new UiccSlotInfo[size];
        }
    };
    private final String mCardId;
    private final int mCardStateInfo;
    private final boolean mIsActive;
    private final boolean mIsEuicc;
    private final boolean mIsExtendedApduSupported;
    private final int mLogicalSlotIdx;

    @Retention(RetentionPolicy.SOURCE)
    public @interface CardStateInfo {
    }

    private UiccSlotInfo(Parcel in) {
        boolean z = false;
        this.mIsActive = in.readByte() != 0;
        this.mIsEuicc = in.readByte() != 0;
        this.mCardId = in.readString();
        this.mCardStateInfo = in.readInt();
        this.mLogicalSlotIdx = in.readInt();
        this.mIsExtendedApduSupported = in.readByte() != 0 ? true : z;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.mIsActive ? (byte) 1 : 0);
        dest.writeByte(this.mIsEuicc ? (byte) 1 : 0);
        dest.writeString(this.mCardId);
        dest.writeInt(this.mCardStateInfo);
        dest.writeInt(this.mLogicalSlotIdx);
        dest.writeByte(this.mIsExtendedApduSupported ? (byte) 1 : 0);
    }

    public int describeContents() {
        return 0;
    }

    public UiccSlotInfo(boolean isActive, boolean isEuicc, String cardId, int cardStateInfo, int logicalSlotIdx, boolean isExtendedApduSupported) {
        this.mIsActive = isActive;
        this.mIsEuicc = isEuicc;
        this.mCardId = cardId;
        this.mCardStateInfo = cardStateInfo;
        this.mLogicalSlotIdx = logicalSlotIdx;
        this.mIsExtendedApduSupported = isExtendedApduSupported;
    }

    public boolean getIsActive() {
        return this.mIsActive;
    }

    public boolean getIsEuicc() {
        return this.mIsEuicc;
    }

    public String getCardId() {
        return this.mCardId;
    }

    public int getCardStateInfo() {
        return this.mCardStateInfo;
    }

    public int getLogicalSlotIdx() {
        return this.mLogicalSlotIdx;
    }

    public boolean getIsExtendedApduSupported() {
        return this.mIsExtendedApduSupported;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UiccSlotInfo that = (UiccSlotInfo) obj;
        if (!(this.mIsActive == that.mIsActive && this.mIsEuicc == that.mIsEuicc && Objects.equals(this.mCardId, that.mCardId) && this.mCardStateInfo == that.mCardStateInfo && this.mLogicalSlotIdx == that.mLogicalSlotIdx && this.mIsExtendedApduSupported == that.mIsExtendedApduSupported)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (31 * ((31 * ((31 * ((31 * ((31 * ((31 * 1) + (this.mIsActive ? 1 : 0))) + (this.mIsEuicc ? 1 : 0))) + Objects.hashCode(this.mCardId))) + this.mCardStateInfo)) + this.mLogicalSlotIdx)) + (this.mIsExtendedApduSupported ? 1 : 0);
    }

    public String toString() {
        return "UiccSlotInfo (mIsActive=" + this.mIsActive + ", mIsEuicc=" + this.mIsEuicc + ", mCardId=" + this.mCardId + ", cardState=" + this.mCardStateInfo + ", phoneId=" + this.mLogicalSlotIdx + ", mIsExtendedApduSupported=" + this.mIsExtendedApduSupported + ")";
    }
}
