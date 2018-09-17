package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class CellInfoCdma extends CellInfo implements Parcelable {
    public static final Creator<CellInfoCdma> CREATOR = null;
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellInfoCdma";
    private CellIdentityCdma mCellIdentityCdma;
    private CellSignalStrengthCdma mCellSignalStrengthCdma;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.CellInfoCdma.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.CellInfoCdma.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.CellInfoCdma.<clinit>():void");
    }

    public CellInfoCdma() {
        this.mCellIdentityCdma = new CellIdentityCdma();
        this.mCellSignalStrengthCdma = new CellSignalStrengthCdma();
    }

    public CellInfoCdma(CellInfoCdma ci) {
        super((CellInfo) ci);
        this.mCellIdentityCdma = ci.mCellIdentityCdma.copy();
        this.mCellSignalStrengthCdma = ci.mCellSignalStrengthCdma.copy();
    }

    public CellIdentityCdma getCellIdentity() {
        return this.mCellIdentityCdma;
    }

    public void setCellIdentity(CellIdentityCdma cid) {
        this.mCellIdentityCdma = cid;
    }

    public CellSignalStrengthCdma getCellSignalStrength() {
        return this.mCellSignalStrengthCdma;
    }

    public void setCellSignalStrength(CellSignalStrengthCdma css) {
        this.mCellSignalStrengthCdma = css;
    }

    public int hashCode() {
        return (super.hashCode() + this.mCellIdentityCdma.hashCode()) + this.mCellSignalStrengthCdma.hashCode();
    }

    public boolean equals(Object other) {
        boolean z = DBG;
        if (!super.equals(other)) {
            return DBG;
        }
        try {
            CellInfoCdma o = (CellInfoCdma) other;
            if (this.mCellIdentityCdma.equals(o.mCellIdentityCdma)) {
                z = this.mCellSignalStrengthCdma.equals(o.mCellSignalStrengthCdma);
            }
            return z;
        } catch (ClassCastException e) {
            return DBG;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CellInfoCdma:{");
        sb.append(super.toString());
        sb.append(" ").append(this.mCellIdentityCdma);
        sb.append(" ").append(this.mCellSignalStrengthCdma);
        sb.append("}");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags, 2);
        this.mCellIdentityCdma.writeToParcel(dest, flags);
        this.mCellSignalStrengthCdma.writeToParcel(dest, flags);
    }

    private CellInfoCdma(Parcel in) {
        super(in);
        this.mCellIdentityCdma = (CellIdentityCdma) CellIdentityCdma.CREATOR.createFromParcel(in);
        this.mCellSignalStrengthCdma = (CellSignalStrengthCdma) CellSignalStrengthCdma.CREATOR.createFromParcel(in);
    }

    protected static CellInfoCdma createFromParcelBody(Parcel in) {
        return new CellInfoCdma(in);
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
