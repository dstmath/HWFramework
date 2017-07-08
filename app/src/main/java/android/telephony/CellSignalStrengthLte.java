package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.os.HwBootFail;
import com.huawei.pgmng.log.LogPower;

public final class CellSignalStrengthLte extends CellSignalStrength implements Parcelable {
    public static final Creator<CellSignalStrengthLte> CREATOR = null;
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellSignalStrengthLte";
    private int mCqi;
    private int mRsrp;
    private int mRsrq;
    private int mRssnr;
    private int mSignalStrength;
    private int mTimingAdvance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.CellSignalStrengthLte.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.CellSignalStrengthLte.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.CellSignalStrengthLte.<clinit>():void");
    }

    public CellSignalStrengthLte() {
        setDefaultValues();
    }

    public CellSignalStrengthLte(int signalStrength, int rsrp, int rsrq, int rssnr, int cqi, int timingAdvance) {
        initialize(signalStrength, rsrp, rsrq, rssnr, cqi, timingAdvance);
    }

    public CellSignalStrengthLte(CellSignalStrengthLte s) {
        copyFrom(s);
    }

    public void initialize(int lteSignalStrength, int rsrp, int rsrq, int rssnr, int cqi, int timingAdvance) {
        this.mSignalStrength = lteSignalStrength;
        this.mRsrp = rsrp;
        this.mRsrq = rsrq;
        this.mRssnr = rssnr;
        this.mCqi = cqi;
        this.mTimingAdvance = timingAdvance;
    }

    public void initialize(SignalStrength ss, int timingAdvance) {
        this.mSignalStrength = ss.getLteSignalStrength();
        this.mRsrp = ss.getLteRsrp();
        this.mRsrq = ss.getLteRsrq();
        this.mRssnr = ss.getLteRssnr();
        this.mCqi = ss.getLteCqi();
        this.mTimingAdvance = timingAdvance;
    }

    protected void copyFrom(CellSignalStrengthLte s) {
        this.mSignalStrength = s.mSignalStrength;
        this.mRsrp = s.mRsrp;
        this.mRsrq = s.mRsrq;
        this.mRssnr = s.mRssnr;
        this.mCqi = s.mCqi;
        this.mTimingAdvance = s.mTimingAdvance;
    }

    public CellSignalStrengthLte copy() {
        return new CellSignalStrengthLte(this);
    }

    public void setDefaultValues() {
        this.mSignalStrength = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mRsrp = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mRsrq = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mRssnr = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mCqi = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mTimingAdvance = HwBootFail.STAGE_BOOT_SUCCESS;
    }

    public int getLevel() {
        int levelRsrp;
        int levelRssnr;
        if (this.mRsrp == HwBootFail.STAGE_BOOT_SUCCESS) {
            levelRsrp = 0;
        } else if (this.mRsrp >= -95) {
            levelRsrp = 4;
        } else if (this.mRsrp >= -105) {
            levelRsrp = 3;
        } else if (this.mRsrp >= -115) {
            levelRsrp = 2;
        } else {
            levelRsrp = 1;
        }
        if (this.mRssnr == HwBootFail.STAGE_BOOT_SUCCESS) {
            levelRssnr = 0;
        } else if (this.mRssnr >= 45) {
            levelRssnr = 4;
        } else if (this.mRssnr >= 10) {
            levelRssnr = 3;
        } else if (this.mRssnr >= -30) {
            levelRssnr = 2;
        } else {
            levelRssnr = 1;
        }
        if (this.mRsrp == HwBootFail.STAGE_BOOT_SUCCESS) {
            return levelRssnr;
        }
        if (this.mRssnr == HwBootFail.STAGE_BOOT_SUCCESS) {
            return levelRsrp;
        }
        return levelRssnr < levelRsrp ? levelRssnr : levelRsrp;
    }

    public int getRsrq() {
        return this.mRsrq;
    }

    public int getRssnr() {
        return this.mRssnr;
    }

    public int getDbm() {
        return this.mRsrp;
    }

    public int getAsuLevel() {
        int lteDbm = getDbm();
        if (lteDbm == HwBootFail.STAGE_BOOT_SUCCESS) {
            return 99;
        }
        if (lteDbm <= -140) {
            return 0;
        }
        if (lteDbm >= -43) {
            return 97;
        }
        return lteDbm + LogPower.MUSIC_AUDIO_PLAY;
    }

    public int getTimingAdvance() {
        return this.mTimingAdvance;
    }

    public int hashCode() {
        return (((((this.mSignalStrength * 31) + (this.mRsrp * 31)) + (this.mRsrq * 31)) + (this.mRssnr * 31)) + (this.mCqi * 31)) + (this.mTimingAdvance * 31);
    }

    public boolean equals(Object o) {
        boolean z = DBG;
        try {
            CellSignalStrengthLte s = (CellSignalStrengthLte) o;
            if (o == null) {
                return DBG;
            }
            if (this.mSignalStrength == s.mSignalStrength && this.mRsrp == s.mRsrp && this.mRsrq == s.mRsrq && this.mRssnr == s.mRssnr && this.mCqi == s.mCqi && this.mTimingAdvance == s.mTimingAdvance) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return DBG;
        }
    }

    public String toString() {
        return "CellSignalStrengthLte: ss=" + this.mSignalStrength + " rsrp=" + this.mRsrp + " rsrq=" + this.mRsrq + " rssnr=" + this.mRssnr + " cqi=" + this.mCqi + " ta=" + this.mTimingAdvance;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = -1;
        dest.writeInt(this.mSignalStrength);
        int i3 = this.mRsrp;
        if (this.mRsrp != HwBootFail.STAGE_BOOT_SUCCESS) {
            i = -1;
        } else {
            i = 1;
        }
        dest.writeInt(i * i3);
        i = this.mRsrq;
        if (this.mRsrq == HwBootFail.STAGE_BOOT_SUCCESS) {
            i2 = 1;
        }
        dest.writeInt(i * i2);
        dest.writeInt(this.mRssnr);
        dest.writeInt(this.mCqi);
        dest.writeInt(this.mTimingAdvance);
    }

    private CellSignalStrengthLte(Parcel in) {
        this.mSignalStrength = in.readInt();
        this.mRsrp = in.readInt();
        if (this.mRsrp != HwBootFail.STAGE_BOOT_SUCCESS) {
            this.mRsrp *= -1;
        }
        this.mRsrq = in.readInt();
        if (this.mRsrq != HwBootFail.STAGE_BOOT_SUCCESS) {
            this.mRsrq *= -1;
        }
        this.mRssnr = in.readInt();
        this.mCqi = in.readInt();
        this.mTimingAdvance = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
