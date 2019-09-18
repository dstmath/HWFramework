package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.Objects;

public final class CellIdentityTdscdma extends CellIdentity {
    public static final Parcelable.Creator<CellIdentityTdscdma> CREATOR = new Parcelable.Creator<CellIdentityTdscdma>() {
        public CellIdentityTdscdma createFromParcel(Parcel in) {
            in.readInt();
            return CellIdentityTdscdma.createFromParcelBody(in);
        }

        public CellIdentityTdscdma[] newArray(int size) {
            return new CellIdentityTdscdma[size];
        }
    };
    private static final boolean DBG = false;
    private static final String TAG = CellIdentityTdscdma.class.getSimpleName();
    private final int mCid;
    private final int mCpid;
    private final int mLac;

    public CellIdentityTdscdma() {
        super(TAG, 5, null, null, null, null);
        this.mLac = Integer.MAX_VALUE;
        this.mCid = Integer.MAX_VALUE;
        this.mCpid = Integer.MAX_VALUE;
    }

    public CellIdentityTdscdma(int mcc, int mnc, int lac, int cid, int cpid) {
        this(String.valueOf(mcc), String.valueOf(mnc), lac, cid, cpid, null, null);
    }

    public CellIdentityTdscdma(String mcc, String mnc, int lac, int cid, int cpid) {
        super(TAG, 5, mcc, mnc, null, null);
        this.mLac = lac;
        this.mCid = cid;
        this.mCpid = cpid;
    }

    public CellIdentityTdscdma(String mcc, String mnc, int lac, int cid, int cpid, String alphal, String alphas) {
        super(TAG, 5, mcc, mnc, alphal, alphas);
        this.mLac = lac;
        this.mCid = cid;
        this.mCpid = cpid;
    }

    private CellIdentityTdscdma(CellIdentityTdscdma cid) {
        this(cid.mMccStr, cid.mMncStr, cid.mLac, cid.mCid, cid.mCpid, cid.mAlphaLong, cid.mAlphaShort);
    }

    /* access modifiers changed from: package-private */
    public CellIdentityTdscdma copy() {
        return new CellIdentityTdscdma(this);
    }

    public String getMccString() {
        return this.mMccStr;
    }

    public String getMncString() {
        return this.mMncStr;
    }

    public int getLac() {
        return this.mLac;
    }

    public int getCid() {
        return this.mCid;
    }

    public int getCpid() {
        return this.mCpid;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mLac), Integer.valueOf(this.mCid), Integer.valueOf(this.mCpid), Integer.valueOf(super.hashCode())});
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (!(other instanceof CellIdentityTdscdma)) {
            return false;
        }
        CellIdentityTdscdma o = (CellIdentityTdscdma) other;
        if (!TextUtils.equals(this.mMccStr, o.mMccStr) || !TextUtils.equals(this.mMncStr, o.mMncStr) || this.mLac != o.mLac || this.mCid != o.mCid || this.mCpid != o.mCpid || !super.equals(other)) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return TAG + ":{ mMcc=" + this.mMccStr + " mMnc=" + this.mMncStr + " mLac=" + "***" + " mCid=" + "***" + " mCpid=" + this.mCpid + " mAlphaLong=" + this.mAlphaLong + " mAlphaShort=" + this.mAlphaShort + "}";
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, 5);
        dest.writeInt(this.mLac);
        dest.writeInt(this.mCid);
        dest.writeInt(this.mCpid);
    }

    private CellIdentityTdscdma(Parcel in) {
        super(TAG, 5, in);
        this.mLac = in.readInt();
        this.mCid = in.readInt();
        this.mCpid = in.readInt();
    }

    protected static CellIdentityTdscdma createFromParcelBody(Parcel in) {
        return new CellIdentityTdscdma(in);
    }
}
