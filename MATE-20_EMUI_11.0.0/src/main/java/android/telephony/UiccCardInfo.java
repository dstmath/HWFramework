package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public final class UiccCardInfo implements Parcelable {
    public static final Parcelable.Creator<UiccCardInfo> CREATOR = new Parcelable.Creator<UiccCardInfo>() {
        /* class android.telephony.UiccCardInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UiccCardInfo createFromParcel(Parcel in) {
            return new UiccCardInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public UiccCardInfo[] newArray(int size) {
            return new UiccCardInfo[size];
        }
    };
    private final int mCardId;
    private final String mEid;
    private final String mIccId;
    private final boolean mIsEuicc;
    private final boolean mIsRemovable;
    private final int mSlotIndex;

    private UiccCardInfo(Parcel in) {
        boolean z = true;
        this.mIsEuicc = in.readByte() != 0;
        this.mCardId = in.readInt();
        this.mEid = in.readString();
        this.mIccId = in.readString();
        this.mSlotIndex = in.readInt();
        this.mIsRemovable = in.readByte() == 0 ? false : z;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.mIsEuicc ? (byte) 1 : 0);
        dest.writeInt(this.mCardId);
        dest.writeString(this.mEid);
        dest.writeString(this.mIccId);
        dest.writeInt(this.mSlotIndex);
        dest.writeByte(this.mIsRemovable ? (byte) 1 : 0);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public UiccCardInfo(boolean isEuicc, int cardId, String eid, String iccId, int slotIndex, boolean isRemovable) {
        this.mIsEuicc = isEuicc;
        this.mCardId = cardId;
        this.mEid = eid;
        this.mIccId = iccId;
        this.mSlotIndex = slotIndex;
        this.mIsRemovable = isRemovable;
    }

    public boolean isEuicc() {
        return this.mIsEuicc;
    }

    public int getCardId() {
        return this.mCardId;
    }

    public String getEid() {
        if (!this.mIsEuicc) {
            return null;
        }
        return this.mEid;
    }

    public String getIccId() {
        return this.mIccId;
    }

    public int getSlotIndex() {
        return this.mSlotIndex;
    }

    public UiccCardInfo getUnprivileged() {
        return new UiccCardInfo(this.mIsEuicc, this.mCardId, null, null, this.mSlotIndex, this.mIsRemovable);
    }

    public boolean isRemovable() {
        return this.mIsRemovable;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UiccCardInfo that = (UiccCardInfo) obj;
        if (this.mIsEuicc == that.mIsEuicc && this.mCardId == that.mCardId && Objects.equals(this.mEid, that.mEid) && Objects.equals(this.mIccId, that.mIccId) && this.mSlotIndex == that.mSlotIndex && this.mIsRemovable == that.mIsRemovable) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Boolean.valueOf(this.mIsEuicc), Integer.valueOf(this.mCardId), this.mEid, this.mIccId, Integer.valueOf(this.mSlotIndex), Boolean.valueOf(this.mIsRemovable));
    }

    public String toString() {
        return "UiccCardInfo (mIsEuicc=" + this.mIsEuicc + ", mCardId=" + this.mCardId + ", mEid=" + this.mEid + ", mIccId=" + this.mIccId + ", mSlotIndex=" + this.mSlotIndex + ", mIsRemovable=" + this.mIsRemovable + ")";
    }
}
