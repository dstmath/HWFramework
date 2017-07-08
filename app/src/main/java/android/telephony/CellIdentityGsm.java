package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.os.HwBootFail;
import java.util.Objects;

public final class CellIdentityGsm implements Parcelable {
    public static final Creator<CellIdentityGsm> CREATOR = null;
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellIdentityGsm";
    private final int mArfcn;
    private final int mBsic;
    private final int mCid;
    private final int mLac;
    private final int mMcc;
    private final int mMnc;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.CellIdentityGsm.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.CellIdentityGsm.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.CellIdentityGsm.<clinit>():void");
    }

    public CellIdentityGsm() {
        this.mMcc = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mMnc = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mLac = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mCid = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mArfcn = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mBsic = HwBootFail.STAGE_BOOT_SUCCESS;
    }

    public CellIdentityGsm(int mcc, int mnc, int lac, int cid) {
        this(mcc, mnc, lac, cid, HwBootFail.STAGE_BOOT_SUCCESS, HwBootFail.STAGE_BOOT_SUCCESS);
    }

    public CellIdentityGsm(int mcc, int mnc, int lac, int cid, int arfcn, int bsic) {
        this.mMcc = mcc;
        this.mMnc = mnc;
        this.mLac = lac;
        this.mCid = cid;
        this.mArfcn = arfcn;
        this.mBsic = bsic;
    }

    private CellIdentityGsm(CellIdentityGsm cid) {
        this.mMcc = cid.mMcc;
        this.mMnc = cid.mMnc;
        this.mLac = cid.mLac;
        this.mCid = cid.mCid;
        this.mArfcn = cid.mArfcn;
        this.mBsic = cid.mBsic;
    }

    CellIdentityGsm copy() {
        return new CellIdentityGsm(this);
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

    public int getArfcn() {
        return this.mArfcn;
    }

    public int getBsic() {
        return this.mBsic;
    }

    @Deprecated
    public int getPsc() {
        return HwBootFail.STAGE_BOOT_SUCCESS;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mMcc), Integer.valueOf(this.mMnc), Integer.valueOf(this.mLac), Integer.valueOf(this.mCid)});
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (!(other instanceof CellIdentityGsm)) {
            return DBG;
        }
        CellIdentityGsm o = (CellIdentityGsm) other;
        if (this.mMcc != o.mMcc || this.mMnc != o.mMnc || this.mLac != o.mLac || this.mCid != o.mCid || this.mArfcn != o.mArfcn) {
            z = DBG;
        } else if (this.mBsic != o.mBsic) {
            z = DBG;
        }
        return z;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("CellIdentityGsm:{");
        sb.append(" mMcc=").append(this.mMcc);
        sb.append(" mMnc=").append(this.mMnc);
        sb.append(" mLac=").append(this.mLac);
        sb.append(" mCid=").append(this.mCid);
        sb.append(" mArfcn=").append(this.mArfcn);
        sb.append(" mBsic=").append("0x").append(Integer.toHexString(this.mBsic));
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
        dest.writeInt(this.mArfcn);
        dest.writeInt(this.mBsic);
    }

    private CellIdentityGsm(Parcel in) {
        this.mMcc = in.readInt();
        this.mMnc = in.readInt();
        this.mLac = in.readInt();
        this.mCid = in.readInt();
        this.mArfcn = in.readInt();
        int bsic = in.readInt();
        if (bsic == MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) {
            bsic = HwBootFail.STAGE_BOOT_SUCCESS;
        }
        this.mBsic = bsic;
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
