package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.os.HwBootFail;
import java.util.Objects;

public final class CellIdentityWcdma implements Parcelable {
    public static final Creator<CellIdentityWcdma> CREATOR = null;
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellIdentityWcdma";
    private final int mCid;
    private final int mLac;
    private final int mMcc;
    private final int mMnc;
    private final int mPsc;
    private final int mUarfcn;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.CellIdentityWcdma.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.CellIdentityWcdma.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.CellIdentityWcdma.<clinit>():void");
    }

    public CellIdentityWcdma() {
        this.mMcc = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mMnc = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mLac = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mCid = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mPsc = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mUarfcn = HwBootFail.STAGE_BOOT_SUCCESS;
    }

    public CellIdentityWcdma(int mcc, int mnc, int lac, int cid, int psc) {
        this(mcc, mnc, lac, cid, psc, HwBootFail.STAGE_BOOT_SUCCESS);
    }

    public CellIdentityWcdma(int mcc, int mnc, int lac, int cid, int psc, int uarfcn) {
        this.mMcc = mcc;
        this.mMnc = mnc;
        this.mLac = lac;
        this.mCid = cid;
        this.mPsc = psc;
        this.mUarfcn = uarfcn;
    }

    private CellIdentityWcdma(CellIdentityWcdma cid) {
        this.mMcc = cid.mMcc;
        this.mMnc = cid.mMnc;
        this.mLac = cid.mLac;
        this.mCid = cid.mCid;
        this.mPsc = cid.mPsc;
        this.mUarfcn = cid.mUarfcn;
    }

    CellIdentityWcdma copy() {
        return new CellIdentityWcdma(this);
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

    public int getPsc() {
        return this.mPsc;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mMcc), Integer.valueOf(this.mMnc), Integer.valueOf(this.mLac), Integer.valueOf(this.mCid), Integer.valueOf(this.mPsc)});
    }

    public int getUarfcn() {
        return this.mUarfcn;
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (!(other instanceof CellIdentityWcdma)) {
            return DBG;
        }
        CellIdentityWcdma o = (CellIdentityWcdma) other;
        if (this.mMcc != o.mMcc || this.mMnc != o.mMnc || this.mLac != o.mLac || this.mCid != o.mCid || this.mPsc != o.mPsc) {
            z = DBG;
        } else if (this.mUarfcn != o.mUarfcn) {
            z = DBG;
        }
        return z;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("CellIdentityWcdma:{");
        sb.append(" mMcc=").append(this.mMcc);
        sb.append(" mMnc=").append(this.mMnc);
        sb.append(" mLac=").append(this.mLac);
        sb.append(" mCid=").append(this.mCid);
        sb.append(" mPsc=").append(this.mPsc);
        sb.append(" mUarfcn=").append(this.mUarfcn);
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
        dest.writeInt(this.mPsc);
        dest.writeInt(this.mUarfcn);
    }

    private CellIdentityWcdma(Parcel in) {
        this.mMcc = in.readInt();
        this.mMnc = in.readInt();
        this.mLac = in.readInt();
        this.mCid = in.readInt();
        this.mPsc = in.readInt();
        this.mUarfcn = in.readInt();
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
