package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class CellInfoGsm extends CellInfo implements Parcelable {
    public static final Creator<CellInfoGsm> CREATOR = null;
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellInfoGsm";
    private CellIdentityGsm mCellIdentityGsm;
    private CellSignalStrengthGsm mCellSignalStrengthGsm;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.CellInfoGsm.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.CellInfoGsm.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.CellInfoGsm.<clinit>():void");
    }

    public CellInfoGsm() {
        this.mCellIdentityGsm = new CellIdentityGsm();
        this.mCellSignalStrengthGsm = new CellSignalStrengthGsm();
    }

    public CellInfoGsm(CellInfoGsm ci) {
        super((CellInfo) ci);
        this.mCellIdentityGsm = ci.mCellIdentityGsm.copy();
        this.mCellSignalStrengthGsm = ci.mCellSignalStrengthGsm.copy();
    }

    public CellIdentityGsm getCellIdentity() {
        return this.mCellIdentityGsm;
    }

    public void setCellIdentity(CellIdentityGsm cid) {
        this.mCellIdentityGsm = cid;
    }

    public CellSignalStrengthGsm getCellSignalStrength() {
        return this.mCellSignalStrengthGsm;
    }

    public void setCellSignalStrength(CellSignalStrengthGsm css) {
        this.mCellSignalStrengthGsm = css;
    }

    public int hashCode() {
        return (super.hashCode() + this.mCellIdentityGsm.hashCode()) + this.mCellSignalStrengthGsm.hashCode();
    }

    public boolean equals(Object other) {
        boolean z = DBG;
        if (!super.equals(other)) {
            return DBG;
        }
        try {
            CellInfoGsm o = (CellInfoGsm) other;
            if (this.mCellIdentityGsm.equals(o.mCellIdentityGsm)) {
                z = this.mCellSignalStrengthGsm.equals(o.mCellSignalStrengthGsm);
            }
            return z;
        } catch (ClassCastException e) {
            return DBG;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CellInfoGsm:{");
        sb.append(super.toString());
        sb.append(" ").append(this.mCellIdentityGsm);
        sb.append(" ").append(this.mCellSignalStrengthGsm);
        sb.append("}");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags, 1);
        this.mCellIdentityGsm.writeToParcel(dest, flags);
        this.mCellSignalStrengthGsm.writeToParcel(dest, flags);
    }

    private CellInfoGsm(Parcel in) {
        super(in);
        this.mCellIdentityGsm = (CellIdentityGsm) CellIdentityGsm.CREATOR.createFromParcel(in);
        this.mCellSignalStrengthGsm = (CellSignalStrengthGsm) CellSignalStrengthGsm.CREATOR.createFromParcel(in);
    }

    protected static CellInfoGsm createFromParcelBody(Parcel in) {
        return new CellInfoGsm(in);
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
