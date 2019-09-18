package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.Objects;

public final class CellIdentityNr extends CellIdentity {
    public static final Parcelable.Creator<CellIdentityNr> CREATOR = new Parcelable.Creator<CellIdentityNr>() {
        public CellIdentityNr createFromParcel(Parcel in) {
            in.readInt();
            return CellIdentityNr.createFromParcelBody(in);
        }

        public CellIdentityNr[] newArray(int size) {
            return new CellIdentityNr[size];
        }
    };
    private static final boolean DBG = false;
    private static final String TAG = CellIdentityNr.class.getSimpleName();
    private final int mBandwidth;
    private final int mCi;
    private final int mNrArfcn;
    private final int mPci;
    private final int mTac;

    public CellIdentityNr() {
        super(TAG, 6, null, null, null, null);
        this.mCi = Integer.MAX_VALUE;
        this.mPci = Integer.MAX_VALUE;
        this.mTac = Integer.MAX_VALUE;
        this.mNrArfcn = Integer.MAX_VALUE;
        this.mBandwidth = Integer.MAX_VALUE;
    }

    public CellIdentityNr(int mcc, int mnc, int ci, int pci, int tac) {
        this(ci, pci, tac, Integer.MAX_VALUE, Integer.MAX_VALUE, String.valueOf(mcc), String.valueOf(mnc), null, null);
    }

    public CellIdentityNr(int mcc, int mnc, int ci, int pci, int tac, int nrArfc) {
        this(ci, pci, tac, nrArfc, Integer.MAX_VALUE, String.valueOf(mcc), String.valueOf(mnc), null, null);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CellIdentityNr(int ci, int pci, int tac, int nrArfcn, int bandwidth, String mccStr, String mncStr, String alphal, String alphas) {
        super(TAG, 6, mccStr, mncStr, alphal, alphas);
        this.mCi = ci;
        this.mPci = pci;
        this.mTac = tac;
        this.mNrArfcn = nrArfcn;
        this.mBandwidth = bandwidth;
    }

    private CellIdentityNr(CellIdentityNr cid) {
        this(cid.mCi, cid.mPci, cid.mTac, cid.mNrArfcn, cid.mBandwidth, cid.mMccStr, cid.mMncStr, cid.mAlphaLong, cid.mAlphaShort);
    }

    /* access modifiers changed from: package-private */
    public CellIdentityNr copy() {
        return new CellIdentityNr(this);
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

    public int getCi() {
        return this.mCi;
    }

    public int getPci() {
        return this.mPci;
    }

    public int getTac() {
        return this.mTac;
    }

    public int getEarfcn() {
        return this.mNrArfcn;
    }

    public int getBandwidth() {
        return this.mBandwidth;
    }

    public String getMccString() {
        return this.mMccStr;
    }

    public String getMncString() {
        return this.mMncStr;
    }

    public String getMobileNetworkOperator() {
        if (this.mMccStr == null || this.mMncStr == null) {
            return null;
        }
        return this.mMccStr + this.mMncStr;
    }

    public int getChannelNumber() {
        return this.mNrArfcn;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mCi), Integer.valueOf(this.mPci), Integer.valueOf(this.mTac), Integer.valueOf(super.hashCode())});
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (!(other instanceof CellIdentityNr)) {
            return false;
        }
        CellIdentityNr o = (CellIdentityNr) other;
        if (!(this.mCi == o.mCi && this.mPci == o.mPci && this.mTac == o.mTac && this.mNrArfcn == o.mNrArfcn && this.mBandwidth == o.mBandwidth && TextUtils.equals(this.mMccStr, o.mMccStr) && TextUtils.equals(this.mMncStr, o.mMncStr) && super.equals(other))) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return TAG + ":{ mPci=" + this.mPci + " mNrArfcn=" + this.mNrArfcn + " mBandwidth=" + this.mBandwidth + " mMcc=" + this.mMccStr + " mMnc=" + this.mMncStr + " mAlphaLong=" + this.mAlphaLong + " mAlphaShort=" + this.mAlphaShort + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, 6);
        dest.writeInt(this.mCi);
        dest.writeInt(this.mPci);
        dest.writeInt(this.mTac);
        dest.writeInt(this.mNrArfcn);
        dest.writeInt(this.mBandwidth);
    }

    private CellIdentityNr(Parcel in) {
        super(TAG, 6, in);
        this.mCi = in.readInt();
        this.mPci = in.readInt();
        this.mTac = in.readInt();
        this.mNrArfcn = in.readInt();
        this.mBandwidth = in.readInt();
    }

    protected static CellIdentityNr createFromParcelBody(Parcel in) {
        return new CellIdentityNr(in);
    }
}
