package android.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import java.util.Objects;

public final class CellIdentityWcdma extends CellIdentity {
    public static final Parcelable.Creator<CellIdentityWcdma> CREATOR = new Parcelable.Creator<CellIdentityWcdma>() {
        /* class android.telephony.CellIdentityWcdma.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CellIdentityWcdma createFromParcel(Parcel in) {
            in.readInt();
            return CellIdentityWcdma.createFromParcelBody(in);
        }

        @Override // android.os.Parcelable.Creator
        public CellIdentityWcdma[] newArray(int size) {
            return new CellIdentityWcdma[size];
        }
    };
    private static final boolean DBG = false;
    private static final int MAX_CID = 268435455;
    private static final int MAX_LAC = 65535;
    private static final int MAX_PSC = 511;
    private static final int MAX_UARFCN = 16383;
    private static final String TAG = CellIdentityWcdma.class.getSimpleName();
    private final int mCid;
    private final int mLac;
    private final int mPsc;
    @UnsupportedAppUsage
    private final int mUarfcn;

    public CellIdentityWcdma() {
        super(TAG, 4, null, null, null, null);
        this.mLac = Integer.MAX_VALUE;
        this.mCid = Integer.MAX_VALUE;
        this.mPsc = Integer.MAX_VALUE;
        this.mUarfcn = Integer.MAX_VALUE;
    }

    public CellIdentityWcdma(int lac, int cid, int psc, int uarfcn, String mccStr, String mncStr, String alphal, String alphas) {
        super(TAG, 4, mccStr, mncStr, alphal, alphas);
        this.mLac = inRangeOrUnavailable(lac, 0, 65535);
        this.mCid = inRangeOrUnavailable(cid, 0, 268435455);
        this.mPsc = inRangeOrUnavailable(psc, 0, 511);
        this.mUarfcn = inRangeOrUnavailable(uarfcn, 0, (int) MAX_UARFCN);
    }

    public CellIdentityWcdma(android.hardware.radio.V1_0.CellIdentityWcdma cid) {
        this(cid.lac, cid.cid, cid.psc, cid.uarfcn, cid.mcc, cid.mnc, "", "");
    }

    public CellIdentityWcdma(android.hardware.radio.V1_2.CellIdentityWcdma cid) {
        this(cid.base.lac, cid.base.cid, cid.base.psc, cid.base.uarfcn, cid.base.mcc, cid.base.mnc, cid.operatorNames.alphaLong, cid.operatorNames.alphaShort);
    }

    private CellIdentityWcdma(CellIdentityWcdma cid) {
        this(cid.mLac, cid.mCid, cid.mPsc, cid.mUarfcn, cid.mMccStr, cid.mMncStr, cid.mAlphaLong, cid.mAlphaShort);
    }

    public CellIdentityWcdma sanitizeLocationInfo() {
        return new CellIdentityWcdma(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, this.mMccStr, this.mMncStr, this.mAlphaLong, this.mAlphaShort);
    }

    /* access modifiers changed from: package-private */
    public CellIdentityWcdma copy() {
        return new CellIdentityWcdma(this);
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

    public int getPsc() {
        return this.mPsc;
    }

    @Override // android.telephony.CellIdentity
    public String getMccString() {
        return this.mMccStr;
    }

    @Override // android.telephony.CellIdentity
    public String getMncString() {
        return this.mMncStr;
    }

    public String getMobileNetworkOperator() {
        if (this.mMccStr == null || this.mMncStr == null) {
            return null;
        }
        return this.mMccStr + this.mMncStr;
    }

    @Override // android.telephony.CellIdentity
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mLac), Integer.valueOf(this.mCid), Integer.valueOf(this.mPsc), Integer.valueOf(super.hashCode()));
    }

    public int getUarfcn() {
        return this.mUarfcn;
    }

    @Override // android.telephony.CellIdentity
    public int getChannelNumber() {
        return this.mUarfcn;
    }

    @Override // android.telephony.CellIdentity
    public GsmCellLocation asCellLocation() {
        GsmCellLocation cl = new GsmCellLocation();
        int lac = this.mLac;
        int psc = -1;
        if (lac == Integer.MAX_VALUE) {
            lac = -1;
        }
        int cid = this.mCid;
        if (cid == Integer.MAX_VALUE) {
            cid = -1;
        }
        int i = this.mPsc;
        if (i != Integer.MAX_VALUE) {
            psc = i;
        }
        cl.setLacAndCid(lac, cid);
        cl.setPsc(psc);
        return cl;
    }

    @Override // android.telephony.CellIdentity
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CellIdentityWcdma)) {
            return false;
        }
        CellIdentityWcdma o = (CellIdentityWcdma) other;
        if (this.mLac == o.mLac && this.mCid == o.mCid && this.mPsc == o.mPsc && this.mUarfcn == o.mUarfcn && TextUtils.equals(this.mMccStr, o.mMccStr) && TextUtils.equals(this.mMncStr, o.mMncStr) && super.equals(other)) {
            return true;
        }
        return false;
    }

    public String toString() {
        return TAG + ":{ mPsc=" + this.mPsc + " mUarfcn=" + this.mUarfcn + " mMcc=" + this.mMccStr + " mMnc=" + this.mMncStr + " mAlphaLong=" + this.mAlphaLong + " mAlphaShort=" + this.mAlphaShort + "}";
    }

    @Override // android.telephony.CellIdentity, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.telephony.CellIdentity, android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, 4);
        dest.writeInt(this.mLac);
        dest.writeInt(this.mCid);
        dest.writeInt(this.mPsc);
        dest.writeInt(this.mUarfcn);
    }

    private CellIdentityWcdma(Parcel in) {
        super(TAG, 4, in);
        this.mLac = in.readInt();
        this.mCid = in.readInt();
        this.mPsc = in.readInt();
        this.mUarfcn = in.readInt();
    }

    protected static CellIdentityWcdma createFromParcelBody(Parcel in) {
        return new CellIdentityWcdma(in);
    }
}
