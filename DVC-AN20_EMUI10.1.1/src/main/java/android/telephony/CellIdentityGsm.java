package android.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import java.util.Objects;

public final class CellIdentityGsm extends CellIdentity {
    public static final Parcelable.Creator<CellIdentityGsm> CREATOR = new Parcelable.Creator<CellIdentityGsm>() {
        /* class android.telephony.CellIdentityGsm.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CellIdentityGsm createFromParcel(Parcel in) {
            in.readInt();
            return CellIdentityGsm.createFromParcelBody(in);
        }

        @Override // android.os.Parcelable.Creator
        public CellIdentityGsm[] newArray(int size) {
            return new CellIdentityGsm[size];
        }
    };
    private static final boolean DBG = false;
    private static final int MAX_ARFCN = 65535;
    private static final int MAX_BSIC = 63;
    private static final int MAX_CID = 65535;
    private static final int MAX_LAC = 65535;
    private static final String TAG = CellIdentityGsm.class.getSimpleName();
    private final int mArfcn;
    private final int mBsic;
    private final int mCid;
    private final int mLac;

    @UnsupportedAppUsage
    public CellIdentityGsm() {
        super(TAG, 1, null, null, null, null);
        this.mLac = Integer.MAX_VALUE;
        this.mCid = Integer.MAX_VALUE;
        this.mArfcn = Integer.MAX_VALUE;
        this.mBsic = Integer.MAX_VALUE;
    }

    public CellIdentityGsm(int lac, int cid, int arfcn, int bsic, String mccStr, String mncStr, String alphal, String alphas) {
        super(TAG, 1, mccStr, mncStr, alphal, alphas);
        this.mLac = inRangeOrUnavailable(lac, 0, 65535);
        this.mCid = inRangeOrUnavailable(cid, 0, 65535);
        this.mArfcn = inRangeOrUnavailable(arfcn, 0, 65535);
        this.mBsic = inRangeOrUnavailable(bsic, 0, 63);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public CellIdentityGsm(android.hardware.radio.V1_0.CellIdentityGsm cid) {
        this(cid.lac, cid.cid, cid.arfcn, cid.bsic == -1 ? 2147483647 : cid.bsic, cid.mcc, cid.mnc, "", "");
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public CellIdentityGsm(android.hardware.radio.V1_2.CellIdentityGsm cid) {
        this(cid.base.lac, cid.base.cid, cid.base.arfcn, cid.base.bsic == -1 ? 2147483647 : cid.base.bsic, cid.base.mcc, cid.base.mnc, cid.operatorNames.alphaLong, cid.operatorNames.alphaShort);
    }

    private CellIdentityGsm(CellIdentityGsm cid) {
        this(cid.mLac, cid.mCid, cid.mArfcn, cid.mBsic, cid.mMccStr, cid.mMncStr, cid.mAlphaLong, cid.mAlphaShort);
    }

    /* access modifiers changed from: package-private */
    public CellIdentityGsm copy() {
        return new CellIdentityGsm(this);
    }

    public CellIdentityGsm sanitizeLocationInfo() {
        return new CellIdentityGsm(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, this.mMccStr, this.mMncStr, this.mAlphaLong, this.mAlphaShort);
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

    @Override // android.telephony.CellIdentity
    public String getMccString() {
        return this.mMccStr;
    }

    @Override // android.telephony.CellIdentity
    public String getMncString() {
        return this.mMncStr;
    }

    @Override // android.telephony.CellIdentity
    public int getChannelNumber() {
        return this.mArfcn;
    }

    @Deprecated
    public int getPsc() {
        return Integer.MAX_VALUE;
    }

    @Override // android.telephony.CellIdentity
    public GsmCellLocation asCellLocation() {
        GsmCellLocation cl = new GsmCellLocation();
        int lac = this.mLac;
        if (lac == Integer.MAX_VALUE) {
            lac = -1;
        }
        int cid = this.mCid;
        if (cid == Integer.MAX_VALUE) {
            cid = -1;
        }
        cl.setLacAndCid(lac, cid);
        cl.setPsc(-1);
        return cl;
    }

    @Override // android.telephony.CellIdentity
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mLac), Integer.valueOf(this.mCid), Integer.valueOf(super.hashCode()));
    }

    @Override // android.telephony.CellIdentity
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CellIdentityGsm)) {
            return false;
        }
        CellIdentityGsm o = (CellIdentityGsm) other;
        if (this.mLac == o.mLac && this.mCid == o.mCid && this.mArfcn == o.mArfcn && this.mBsic == o.mBsic && TextUtils.equals(this.mMccStr, o.mMccStr) && TextUtils.equals(this.mMncStr, o.mMncStr) && super.equals(other)) {
            return true;
        }
        return false;
    }

    public String toString() {
        return TAG + ":{  mArfcn=" + this.mArfcn + " mBsic=" + "0x" + Integer.toHexString(this.mBsic) + " mMcc=" + this.mMccStr + " mMnc=" + this.mMncStr + " mAlphaLong=" + this.mAlphaLong + " mAlphaShort=" + this.mAlphaShort + "}";
    }

    @Override // android.os.Parcelable, android.telephony.CellIdentity
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable, android.telephony.CellIdentity
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
