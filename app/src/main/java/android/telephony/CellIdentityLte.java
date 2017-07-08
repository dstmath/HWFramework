package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.os.HwBootFail;
import java.util.Objects;

public final class CellIdentityLte implements Parcelable {
    public static final Creator<CellIdentityLte> CREATOR = null;
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellIdentityLte";
    private final int mCi;
    private final int mEarfcn;
    private final int mMcc;
    private final int mMnc;
    private final int mPci;
    private final int mTac;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.CellIdentityLte.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.CellIdentityLte.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.CellIdentityLte.<clinit>():void");
    }

    public CellIdentityLte() {
        this.mMcc = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mMnc = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mCi = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mPci = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mTac = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mEarfcn = HwBootFail.STAGE_BOOT_SUCCESS;
    }

    public CellIdentityLte(int mcc, int mnc, int ci, int pci, int tac) {
        this(mcc, mnc, ci, pci, tac, HwBootFail.STAGE_BOOT_SUCCESS);
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
            return DBG;
        }
        CellIdentityLte o = (CellIdentityLte) other;
        if (this.mMcc != o.mMcc || this.mMnc != o.mMnc || this.mCi != o.mCi || this.mPci != o.mPci || this.mTac != o.mTac) {
            z = DBG;
        } else if (this.mEarfcn != o.mEarfcn) {
            z = DBG;
        }
        return z;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("CellIdentityLte:{");
        sb.append(" mMcc=");
        sb.append(this.mMcc);
        sb.append(" mMnc=");
        sb.append(this.mMnc);
        sb.append(" mCi=");
        sb.append(this.mCi);
        sb.append(" mPci=");
        sb.append(this.mPci);
        sb.append(" mTac=");
        sb.append(this.mTac);
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
