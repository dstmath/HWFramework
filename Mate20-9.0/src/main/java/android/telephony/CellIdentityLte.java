package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.Objects;

public final class CellIdentityLte extends CellIdentity {
    public static final Parcelable.Creator<CellIdentityLte> CREATOR = new Parcelable.Creator<CellIdentityLte>() {
        public CellIdentityLte createFromParcel(Parcel in) {
            in.readInt();
            return CellIdentityLte.createFromParcelBody(in);
        }

        public CellIdentityLte[] newArray(int size) {
            return new CellIdentityLte[size];
        }
    };
    private static final boolean DBG = false;
    private static final String TAG = CellIdentityLte.class.getSimpleName();
    private final int mBandwidth;
    private final int mCi;
    private final int mEarfcn;
    private final int mPci;
    private final int mTac;

    public CellIdentityLte() {
        super(TAG, 3, null, null, null, null);
        this.mCi = Integer.MAX_VALUE;
        this.mPci = Integer.MAX_VALUE;
        this.mTac = Integer.MAX_VALUE;
        this.mEarfcn = Integer.MAX_VALUE;
        this.mBandwidth = Integer.MAX_VALUE;
    }

    public CellIdentityLte(int mcc, int mnc, int ci, int pci, int tac) {
        this(ci, pci, tac, Integer.MAX_VALUE, Integer.MAX_VALUE, String.valueOf(mcc), String.valueOf(mnc), null, null);
    }

    public CellIdentityLte(int mcc, int mnc, int ci, int pci, int tac, int earfcn) {
        this(ci, pci, tac, earfcn, Integer.MAX_VALUE, String.valueOf(mcc), String.valueOf(mnc), null, null);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CellIdentityLte(int ci, int pci, int tac, int earfcn, int bandwidth, String mccStr, String mncStr, String alphal, String alphas) {
        super(TAG, 3, mccStr, mncStr, alphal, alphas);
        this.mCi = ci;
        this.mPci = pci;
        this.mTac = tac;
        this.mEarfcn = earfcn;
        this.mBandwidth = bandwidth;
    }

    private CellIdentityLte(CellIdentityLte cid) {
        this(cid.mCi, cid.mPci, cid.mTac, cid.mEarfcn, cid.mBandwidth, cid.mMccStr, cid.mMncStr, cid.mAlphaLong, cid.mAlphaShort);
    }

    /* access modifiers changed from: package-private */
    public CellIdentityLte copy() {
        return new CellIdentityLte(this);
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
        return this.mEarfcn;
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
        return this.mEarfcn;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mCi), Integer.valueOf(this.mPci), Integer.valueOf(this.mTac), Integer.valueOf(super.hashCode())});
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (!(other instanceof CellIdentityLte)) {
            return false;
        }
        CellIdentityLte o = (CellIdentityLte) other;
        if (!(this.mCi == o.mCi && this.mPci == o.mPci && this.mTac == o.mTac && this.mEarfcn == o.mEarfcn && this.mBandwidth == o.mBandwidth && TextUtils.equals(this.mMccStr, o.mMccStr) && TextUtils.equals(this.mMncStr, o.mMncStr) && super.equals(other))) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return TAG + ":{ mPci=" + this.mPci + " mEarfcn=" + this.mEarfcn + " mBandwidth=" + this.mBandwidth + " mMcc=" + this.mMccStr + " mMnc=" + this.mMncStr + " mAlphaLong=" + this.mAlphaLong + " mAlphaShort=" + this.mAlphaShort + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, 3);
        dest.writeInt(this.mCi);
        dest.writeInt(this.mPci);
        dest.writeInt(this.mTac);
        dest.writeInt(this.mEarfcn);
        dest.writeInt(this.mBandwidth);
    }

    private CellIdentityLte(Parcel in) {
        super(TAG, 3, in);
        this.mCi = in.readInt();
        this.mPci = in.readInt();
        this.mTac = in.readInt();
        this.mEarfcn = in.readInt();
        this.mBandwidth = in.readInt();
    }

    protected static CellIdentityLte createFromParcelBody(Parcel in) {
        return new CellIdentityLte(in);
    }
}
