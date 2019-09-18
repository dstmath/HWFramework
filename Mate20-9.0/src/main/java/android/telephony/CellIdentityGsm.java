package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.Objects;

public final class CellIdentityGsm extends CellIdentity {
    public static final Parcelable.Creator<CellIdentityGsm> CREATOR = new Parcelable.Creator<CellIdentityGsm>() {
        public CellIdentityGsm createFromParcel(Parcel in) {
            in.readInt();
            return CellIdentityGsm.createFromParcelBody(in);
        }

        public CellIdentityGsm[] newArray(int size) {
            return new CellIdentityGsm[size];
        }
    };
    private static final boolean DBG = false;
    private static final String TAG = CellIdentityGsm.class.getSimpleName();
    private final int mArfcn;
    private final int mBsic;
    private final int mCid;
    private final int mLac;

    public CellIdentityGsm() {
        super(TAG, 1, null, null, null, null);
        this.mLac = Integer.MAX_VALUE;
        this.mCid = Integer.MAX_VALUE;
        this.mArfcn = Integer.MAX_VALUE;
        this.mBsic = Integer.MAX_VALUE;
    }

    public CellIdentityGsm(int mcc, int mnc, int lac, int cid) {
        this(lac, cid, Integer.MAX_VALUE, Integer.MAX_VALUE, String.valueOf(mcc), String.valueOf(mnc), null, null);
    }

    public CellIdentityGsm(int mcc, int mnc, int lac, int cid, int arfcn, int bsic) {
        this(lac, cid, arfcn, bsic, String.valueOf(mcc), String.valueOf(mnc), null, null);
    }

    public CellIdentityGsm(int lac, int cid, int arfcn, int bsic, String mccStr, String mncStr, String alphal, String alphas) {
        super(TAG, 1, mccStr, mncStr, alphal, alphas);
        this.mLac = lac;
        this.mCid = cid;
        this.mArfcn = arfcn;
        this.mBsic = bsic == 255 ? Integer.MAX_VALUE : bsic;
    }

    private CellIdentityGsm(CellIdentityGsm cid) {
        this(cid.mLac, cid.mCid, cid.mArfcn, cid.mBsic, cid.mMccStr, cid.mMncStr, cid.mAlphaLong, cid.mAlphaShort);
    }

    /* access modifiers changed from: package-private */
    public CellIdentityGsm copy() {
        return new CellIdentityGsm(this);
    }

    @Deprecated
    public int getMcc() {
        if (this.mMccStr != null) {
            return Integer.valueOf(this.mMccStr).intValue();
        }
        return Integer.MAX_VALUE;
    }

    @Deprecated
    public int getMnc() {
        if (this.mMncStr != null) {
            return Integer.valueOf(this.mMncStr).intValue();
        }
        return Integer.MAX_VALUE;
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

    public String getMobileNetworkOperator() {
        if (this.mMccStr == null || this.mMncStr == null) {
            return null;
        }
        return this.mMccStr + this.mMncStr;
    }

    public String getMccString() {
        return this.mMccStr;
    }

    public String getMncString() {
        return this.mMncStr;
    }

    public int getChannelNumber() {
        return this.mArfcn;
    }

    @Deprecated
    public int getPsc() {
        return Integer.MAX_VALUE;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mLac), Integer.valueOf(this.mCid), Integer.valueOf(super.hashCode())});
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
        if (!(this.mLac == o.mLac && this.mCid == o.mCid && this.mArfcn == o.mArfcn && this.mBsic == o.mBsic && TextUtils.equals(this.mMccStr, o.mMccStr) && TextUtils.equals(this.mMncStr, o.mMncStr) && super.equals(other))) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return TAG + ":{  mArfcn=" + this.mArfcn + " mBsic=" + "0x" + Integer.toHexString(this.mBsic) + " mMcc=" + this.mMccStr + " mMnc=" + this.mMncStr + " mAlphaLong=" + this.mAlphaLong + " mAlphaShort=" + this.mAlphaShort + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, 1);
        dest.writeInt(this.mLac);
        dest.writeInt(this.mCid);
        dest.writeInt(this.mArfcn);
        dest.writeInt(this.mBsic);
    }

    private CellIdentityGsm(Parcel in) {
        super(TAG, 1, in);
        this.mLac = in.readInt();
        this.mCid = in.readInt();
        this.mArfcn = in.readInt();
        this.mBsic = in.readInt();
    }

    protected static CellIdentityGsm createFromParcelBody(Parcel in) {
        return new CellIdentityGsm(in);
    }
}
