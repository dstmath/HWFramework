package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.gsm.GsmCellLocation;
import java.util.Objects;

public final class CellIdentityNr extends CellIdentity {
    public static final Parcelable.Creator<CellIdentityNr> CREATOR = new Parcelable.Creator<CellIdentityNr>() {
        /* class android.telephony.CellIdentityNr.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CellIdentityNr createFromParcel(Parcel in) {
            in.readInt();
            return CellIdentityNr.createFromParcelBody(in);
        }

        @Override // android.os.Parcelable.Creator
        public CellIdentityNr[] newArray(int size) {
            return new CellIdentityNr[size];
        }
    };
    private static final int MAX_HWTAC = 16777215;
    private static final long MAX_NCI = 68719476735L;
    private static final int MAX_NRARFCN = 3279165;
    private static final int MAX_PCI = 1007;
    private static final int MAX_TAC = 65535;
    private static final String TAG = "CellIdentityNr";
    private final int mHwTac;
    private final long mNci;
    private final int mNrArfcn;
    private final int mPci;
    private final int mTac;

    public CellIdentityNr(int pci, int tac, int nrArfcn, String mccStr, String mncStr, long nci, String alphal, String alphas) {
        super(TAG, 6, mccStr, mncStr, alphal, alphas);
        this.mPci = inRangeOrUnavailable(pci, 0, 1007);
        this.mTac = inRangeOrUnavailable(tac, 0, 65535);
        this.mHwTac = inRangeOrUnavailable(tac, 0, 16777215);
        this.mNrArfcn = inRangeOrUnavailable(nrArfcn, 0, (int) MAX_NRARFCN);
        this.mNci = inRangeOrUnavailable(nci, 0, (long) MAX_NCI);
    }

    public CellIdentityNr(android.hardware.radio.V1_4.CellIdentityNr cid) {
        this(cid.pci, cid.tac, cid.nrarfcn, cid.mcc, cid.mnc, cid.nci, cid.operatorNames.alphaLong, cid.operatorNames.alphaShort);
    }

    public CellIdentityNr sanitizeLocationInfo() {
        return new CellIdentityNr(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, this.mMccStr, this.mMncStr, 2147483647L, this.mAlphaLong, this.mAlphaShort);
    }

    @Override // android.telephony.CellIdentity
    public CellLocation asCellLocation() {
        GsmCellLocation cl = new GsmCellLocation();
        int tac = this.mHwTac;
        if (tac == Integer.MAX_VALUE) {
            tac = -1;
        }
        long cid = this.mNci;
        if (cid == Long.MAX_VALUE) {
            cid = -1;
        }
        cl.setNrLacAndCid(tac, cid);
        cl.setPsc(0);
        return cl;
    }

    @Override // android.telephony.CellIdentity
    public int hashCode() {
        return Objects.hash(Integer.valueOf(super.hashCode()), Integer.valueOf(this.mPci), Integer.valueOf(this.mTac), Integer.valueOf(this.mNrArfcn), Long.valueOf(this.mNci));
    }

    @Override // android.telephony.CellIdentity
    public boolean equals(Object other) {
        if (!(other instanceof CellIdentityNr)) {
            return false;
        }
        CellIdentityNr o = (CellIdentityNr) other;
        if (super.equals(o) && this.mPci == o.mPci && this.mTac == o.mTac && this.mNrArfcn == o.mNrArfcn && this.mNci == o.mNci) {
            return true;
        }
        return false;
    }

    public long getNci() {
        return this.mNci;
    }

    public int getNrarfcn() {
        return this.mNrArfcn;
    }

    public int getPci() {
        return this.mPci;
    }

    public int getTac() {
        return this.mTac;
    }

    public int getHwTac() {
        return this.mHwTac;
    }

    @Override // android.telephony.CellIdentity
    public String getMccString() {
        return this.mMccStr;
    }

    @Override // android.telephony.CellIdentity
    public String getMncString() {
        return this.mMncStr;
    }

    public String toString() {
        return "CellIdentityNr:{ mPci = " + this.mPci + " mTac = *** mNrArfcn = " + this.mNrArfcn + " mMcc = " + this.mMccStr + " mMnc = " + this.mMncStr + " mNci = *** mAlphaLong = " + this.mAlphaLong + " mAlphaShort = " + this.mAlphaShort + " }";
    }

    @Override // android.telephony.CellIdentity, android.os.Parcelable
    public void writeToParcel(Parcel dest, int type) {
        super.writeToParcel(dest, 6);
        dest.writeInt(this.mPci);
        dest.writeInt(this.mTac);
        dest.writeInt(this.mNrArfcn);
        dest.writeLong(this.mNci);
        dest.writeInt(this.mHwTac);
    }

    private CellIdentityNr(Parcel in) {
        super(TAG, 6, in);
        this.mPci = in.readInt();
        this.mTac = in.readInt();
        this.mNrArfcn = in.readInt();
        this.mNci = in.readLong();
        this.mHwTac = in.readInt();
    }

    protected static CellIdentityNr createFromParcelBody(Parcel in) {
        return new CellIdentityNr(in);
    }
}
