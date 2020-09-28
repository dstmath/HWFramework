package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class CellConfigLte implements Parcelable {
    public static final Parcelable.Creator<CellConfigLte> CREATOR = new Parcelable.Creator<CellConfigLte>() {
        /* class android.telephony.CellConfigLte.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CellConfigLte createFromParcel(Parcel in) {
            return new CellConfigLte(in);
        }

        @Override // android.os.Parcelable.Creator
        public CellConfigLte[] newArray(int size) {
            return new CellConfigLte[0];
        }
    };
    private final boolean mIsEndcAvailable;

    public CellConfigLte() {
        this.mIsEndcAvailable = false;
    }

    public CellConfigLte(android.hardware.radio.V1_4.CellConfigLte cellConfig) {
        this.mIsEndcAvailable = cellConfig.isEndcAvailable;
    }

    public CellConfigLte(boolean isEndcAvailable) {
        this.mIsEndcAvailable = isEndcAvailable;
    }

    public CellConfigLte(CellConfigLte config) {
        this.mIsEndcAvailable = config.mIsEndcAvailable;
    }

    /* access modifiers changed from: package-private */
    public boolean isEndcAvailable() {
        return this.mIsEndcAvailable;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public int hashCode() {
        return Objects.hash(Boolean.valueOf(this.mIsEndcAvailable));
    }

    public boolean equals(Object other) {
        if ((other instanceof CellConfigLte) && this.mIsEndcAvailable == ((CellConfigLte) other).mIsEndcAvailable) {
            return true;
        }
        return false;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBoolean(this.mIsEndcAvailable);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append(" :{");
        sb.append(" isEndcAvailable = " + this.mIsEndcAvailable);
        sb.append(" }");
        return sb.toString();
    }

    private CellConfigLte(Parcel in) {
        this.mIsEndcAvailable = in.readBoolean();
    }
}
