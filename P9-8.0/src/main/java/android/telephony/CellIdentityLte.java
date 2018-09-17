package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public final class CellIdentityLte implements Parcelable {
    public static final Creator<CellIdentityLte> CREATOR = new Creator<CellIdentityLte>() {
        public CellIdentityLte createFromParcel(Parcel in) {
            return new CellIdentityLte(in, null);
        }

        public CellIdentityLte[] newArray(int size) {
            return new CellIdentityLte[size];
        }
    };
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellIdentityLte";
    private final int mCi;
    private final int mEarfcn;
    private final int mMcc;
    private final int mMnc;
    private final int mPci;
    private final int mTac;

    /* synthetic */ CellIdentityLte(Parcel in, CellIdentityLte -this1) {
        this(in);
    }

    public CellIdentityLte() {
        this.mMcc = Integer.MAX_VALUE;
        this.mMnc = Integer.MAX_VALUE;
        this.mCi = Integer.MAX_VALUE;
        this.mPci = Integer.MAX_VALUE;
        this.mTac = Integer.MAX_VALUE;
        this.mEarfcn = Integer.MAX_VALUE;
    }

    public CellIdentityLte(int mcc, int mnc, int ci, int pci, int tac) {
        this(mcc, mnc, ci, pci, tac, Integer.MAX_VALUE);
    }

    public CellIdentityLte(int mcc, int mnc, int ci, int pci, int tac, int earfcn) {
        this.mMcc = mcc;
        this.mMnc = mnc;
        this.mCi = ci;
        this.mPci = pci;
        this.mTac = tac;
        this.mEarfcn = earfcn;
    }

    private CellIdentityLte(CellIdentityLte cid) {
        this.mMcc = cid.mMcc;
        this.mMnc = cid.mMnc;
        this.mCi = cid.mCi;
        this.mPci = cid.mPci;
        this.mTac = cid.mTac;
        this.mEarfcn = cid.mEarfcn;
    }

    CellIdentityLte copy() {
        return new CellIdentityLte(this);
    }

    public int getMcc() {
        return this.mMcc;
    }

    public int getMnc() {
        return this.mMnc;
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

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mMcc), Integer.valueOf(this.mMnc), Integer.valueOf(this.mCi), Integer.valueOf(this.mPci), Integer.valueOf(this.mTac)});
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
        if (this.mMcc != o.mMcc || this.mMnc != o.mMnc || this.mCi != o.mCi || this.mPci != o.mPci || this.mTac != o.mTac) {
            z = false;
        } else if (this.mEarfcn != o.mEarfcn) {
            z = false;
        }
        return z;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("CellIdentityLte:{");
        sb.append(" mMcc=");
        sb.append(this.mMcc);
        sb.append(" mMnc=");
        sb.append(this.mMnc);
        sb.append(" mPci=");
        sb.append(this.mPci);
        sb.append(" mEarfcn=");
        sb.append(this.mEarfcn);
        sb.append("}");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mMcc);
        dest.writeInt(this.mMnc);
        dest.writeInt(this.mCi);
        dest.writeInt(this.mPci);
        dest.writeInt(this.mTac);
        dest.writeInt(this.mEarfcn);
    }

    private CellIdentityLte(Parcel in) {
        this.mMcc = in.readInt();
        this.mMnc = in.readInt();
        this.mCi = in.readInt();
        this.mPci = in.readInt();
        this.mTac = in.readInt();
        this.mEarfcn = in.readInt();
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
