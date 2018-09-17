package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class CellInfoWcdma extends CellInfo implements Parcelable {
    public static final Creator<CellInfoWcdma> CREATOR = null;
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellInfoWcdma";
    private CellIdentityWcdma mCellIdentityWcdma;
    private CellSignalStrengthWcdma mCellSignalStrengthWcdma;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.CellInfoWcdma.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.CellInfoWcdma.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.CellInfoWcdma.<clinit>():void");
    }

    public CellInfoWcdma() {
        this.mCellIdentityWcdma = new CellIdentityWcdma();
        this.mCellSignalStrengthWcdma = new CellSignalStrengthWcdma();
    }

    public CellInfoWcdma(CellInfoWcdma ci) {
        super((CellInfo) ci);
        this.mCellIdentityWcdma = ci.mCellIdentityWcdma.copy();
        this.mCellSignalStrengthWcdma = ci.mCellSignalStrengthWcdma.copy();
    }

    public CellIdentityWcdma getCellIdentity() {
        return this.mCellIdentityWcdma;
    }

    public void setCellIdentity(CellIdentityWcdma cid) {
        this.mCellIdentityWcdma = cid;
    }

    public CellSignalStrengthWcdma getCellSignalStrength() {
        return this.mCellSignalStrengthWcdma;
    }

    public void setCellSignalStrength(CellSignalStrengthWcdma css) {
        this.mCellSignalStrengthWcdma = css;
    }

    public int hashCode() {
        return (super.hashCode() + this.mCellIdentityWcdma.hashCode()) + this.mCellSignalStrengthWcdma.hashCode();
    }

    public boolean equals(Object other) {
        boolean z = DBG;
        if (!super.equals(other)) {
            return DBG;
        }
        try {
            CellInfoWcdma o = (CellInfoWcdma) other;
            if (this.mCellIdentityWcdma.equals(o.mCellIdentityWcdma)) {
                z = this.mCellSignalStrengthWcdma.equals(o.mCellSignalStrengthWcdma);
            }
            return z;
        } catch (ClassCastException e) {
            return DBG;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CellInfoWcdma:{");
        sb.append(super.toString());
        sb.append(" ").append(this.mCellIdentityWcdma);
        sb.append(" ").append(this.mCellSignalStrengthWcdma);
        sb.append("}");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags, 4);
        this.mCellIdentityWcdma.writeToParcel(dest, flags);
        this.mCellSignalStrengthWcdma.writeToParcel(dest, flags);
    }

    private CellInfoWcdma(Parcel in) {
        super(in);
        this.mCellIdentityWcdma = (CellIdentityWcdma) CellIdentityWcdma.CREATOR.createFromParcel(in);
        this.mCellSignalStrengthWcdma = (CellSignalStrengthWcdma) CellSignalStrengthWcdma.CREATOR.createFromParcel(in);
    }

    protected static CellInfoWcdma createFromParcelBody(Parcel in) {
        return new CellInfoWcdma(in);
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
