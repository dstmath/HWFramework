package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public final class CellIdentityGsm implements Parcelable {
    public static final Creator<CellIdentityGsm> CREATOR = new Creator<CellIdentityGsm>() {
        public CellIdentityGsm createFromParcel(Parcel in) {
            return new CellIdentityGsm(in, null);
        }

        public CellIdentityGsm[] newArray(int size) {
            return new CellIdentityGsm[size];
        }
    };
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellIdentityGsm";
    private final int mArfcn;
    private final int mBsic;
    private final int mCid;
    private final int mLac;
    private final int mMcc;
    private final int mMnc;

    /* synthetic */ CellIdentityGsm(Parcel in, CellIdentityGsm -this1) {
        this(in);
    }

    public CellIdentityGsm() {
        this.mMcc = Integer.MAX_VALUE;
        this.mMnc = Integer.MAX_VALUE;
        this.mLac = Integer.MAX_VALUE;
        this.mCid = Integer.MAX_VALUE;
        this.mArfcn = Integer.MAX_VALUE;
        this.mBsic = Integer.MAX_VALUE;
    }

    public CellIdentityGsm(int mcc, int mnc, int lac, int cid) {
        this(mcc, mnc, lac, cid, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public CellIdentityGsm(int mcc, int mnc, int lac, int cid, int arfcn, int bsic) {
        this.mMcc = mcc;
        this.mMnc = mnc;
        this.mLac = lac;
        this.mCid = cid;
        this.mArfcn = arfcn;
        this.mBsic = bsic;
    }

    private CellIdentityGsm(CellIdentityGsm cid) {
        this.mMcc = cid.mMcc;
        this.mMnc = cid.mMnc;
        this.mLac = cid.mLac;
        this.mCid = cid.mCid;
        this.mArfcn = cid.mArfcn;
        this.mBsic = cid.mBsic;
    }

    CellIdentityGsm copy() {
        return new CellIdentityGsm(this);
    }

    public int getMcc() {
        return this.mMcc;
    }

    public int getMnc() {
        return this.mMnc;
    }

    public int getLac() {
        return this.mLac;
    }

    public int getCid() {
        return this.mCid;
    }

    public int getArfcn() {
        return this.mArfcn;
    }

    public int getBsic() {
        return this.mBsic;
    }

    @Deprecated
    public int getPsc() {
        return Integer.MAX_VALUE;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mMcc), Integer.valueOf(this.mMnc), Integer.valueOf(this.mLac), Integer.valueOf(this.mCid)});
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (!(other instanceof CellIdentityGsm)) {
            return false;
        }
        CellIdentityGsm o = (CellIdentityGsm) other;
        if (this.mMcc != o.mMcc || this.mMnc != o.mMnc || this.mLac != o.mLac || this.mCid != o.mCid || this.mArfcn != o.mArfcn) {
            z = false;
        } else if (this.mBsic != o.mBsic) {
            z = false;
        }
        return z;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("CellIdentityGsm:{");
        sb.append(" mMcc=").append(this.mMcc);
        sb.append(" mMnc=").append(this.mMnc);
        sb.append(" mArfcn=").append(this.mArfcn);
        sb.append(" mBsic=").append("0x").append(Integer.toHexString(this.mBsic));
        sb.append("}");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mMcc);
        dest.writeInt(this.mMnc);
        dest.writeInt(this.mLac);
        dest.writeInt(this.mCid);
        dest.writeInt(this.mArfcn);
        dest.writeInt(this.mBsic);
    }

    private CellIdentityGsm(Parcel in) {
        this.mMcc = in.readInt();
        this.mMnc = in.readInt();
        this.mLac = in.readInt();
        this.mCid = in.readInt();
        this.mArfcn = in.readInt();
        int bsic = in.readInt();
        if (bsic == 255) {
            bsic = Integer.MAX_VALUE;
        }
        this.mBsic = bsic;
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
