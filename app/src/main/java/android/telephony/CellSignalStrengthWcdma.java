package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.os.HwBootFail;

public final class CellSignalStrengthWcdma extends CellSignalStrength implements Parcelable {
    public static final Creator<CellSignalStrengthWcdma> CREATOR = null;
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellSignalStrengthWcdma";
    private static final int WCDMA_SIGNAL_STRENGTH_GOOD = 8;
    private static final int WCDMA_SIGNAL_STRENGTH_GREAT = 12;
    private static final int WCDMA_SIGNAL_STRENGTH_MODERATE = 5;
    private int mBitErrorRate;
    private int mSignalStrength;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.CellSignalStrengthWcdma.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.CellSignalStrengthWcdma.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.CellSignalStrengthWcdma.<clinit>():void");
    }

    public CellSignalStrengthWcdma() {
        setDefaultValues();
    }

    public CellSignalStrengthWcdma(int ss, int ber) {
        initialize(ss, ber);
    }

    public CellSignalStrengthWcdma(CellSignalStrengthWcdma s) {
        copyFrom(s);
    }

    public void initialize(int ss, int ber) {
        this.mSignalStrength = ss;
        this.mBitErrorRate = ber;
    }

    protected void copyFrom(CellSignalStrengthWcdma s) {
        this.mSignalStrength = s.mSignalStrength;
        this.mBitErrorRate = s.mBitErrorRate;
    }

    public CellSignalStrengthWcdma copy() {
        return new CellSignalStrengthWcdma(this);
    }

    public void setDefaultValues() {
        this.mSignalStrength = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mBitErrorRate = HwBootFail.STAGE_BOOT_SUCCESS;
    }

    public int getLevel() {
        int asu = this.mSignalStrength;
        if (asu <= 2 || asu == 99) {
            return 0;
        }
        if (asu >= WCDMA_SIGNAL_STRENGTH_GREAT) {
            return 4;
        }
        if (asu >= WCDMA_SIGNAL_STRENGTH_GOOD) {
            return 3;
        }
        if (asu >= WCDMA_SIGNAL_STRENGTH_MODERATE) {
            return 2;
        }
        return 1;
    }

    public int getDbm() {
        int asu;
        int level = this.mSignalStrength;
        if (level == 99) {
            asu = HwBootFail.STAGE_BOOT_SUCCESS;
        } else {
            asu = level;
        }
        if (asu != HwBootFail.STAGE_BOOT_SUCCESS) {
            return (asu * 2) - 113;
        }
        return HwBootFail.STAGE_BOOT_SUCCESS;
    }

    public int getAsuLevel() {
        return this.mSignalStrength;
    }

    public int hashCode() {
        return (this.mSignalStrength * 31) + (this.mBitErrorRate * 31);
    }

    public boolean equals(Object o) {
        boolean z = DBG;
        try {
            CellSignalStrengthWcdma s = (CellSignalStrengthWcdma) o;
            if (o == null) {
                return DBG;
            }
            if (this.mSignalStrength == s.mSignalStrength && this.mBitErrorRate == s.mBitErrorRate) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return DBG;
        }
    }

    public String toString() {
        return "CellSignalStrengthWcdma: ss=" + this.mSignalStrength + " ber=" + this.mBitErrorRate;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSignalStrength);
        dest.writeInt(this.mBitErrorRate);
    }

    private CellSignalStrengthWcdma(Parcel in) {
        this.mSignalStrength = in.readInt();
        this.mBitErrorRate = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
